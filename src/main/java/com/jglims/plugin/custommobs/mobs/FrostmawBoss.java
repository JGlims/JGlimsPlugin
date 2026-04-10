package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobType;
import com.jglims.plugin.custommobs.CustomWorldBoss;
import com.jglims.plugin.legendary.LegendaryTier;
import com.jglims.plugin.legendary.LegendaryWeapon;
import com.jglims.plugin.legendary.LegendaryWeaponManager;
import kr.toxicity.model.api.animation.AnimationIterator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Frostmaw - Ice beast with freeze mechanics. HP 700.
 * FREEZE system: attacks add freeze stacks, 5 stacks = frozen 3s.
 * Stacks decay 1 per 5s. Frost breath, ice slam, blizzard, icicle rain.
 */
public class FrostmawBoss extends CustomWorldBoss {

    private final Random random = new Random();

    /** Freeze stacks per player. At 5 stacks, player is frozen for 3s. */
    private final Map<UUID, Integer> freezeStacks = new HashMap<>();
    private final Map<UUID, Long> lastStackDecay = new HashMap<>();

    private boolean blizzardActive = false;

    public FrostmawBoss(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.FROSTMAW);
        phaseThresholds = new double[]{0.50, 0.20};
        aggroRadius = 35.0;
        arenaRadius = 40.0;
        announceRadius = 100.0;
    }

    @Override
    protected void onSpawn() {
        super.onSpawn();
        if (hitboxEntity != null) {
            Location loc = hitboxEntity.getLocation();
            loc.getWorld().playSound(loc, Sound.ENTITY_POLAR_BEAR_WARNING, 2.0f, 0.5f);
            loc.getWorld().spawnParticle(Particle.SNOWFLAKE, loc, 50, 3, 3, 3, 0.1);
        }
    }

    // ── Combat Logic ──────────────────────────────────────────────────

    @Override
    protected void combatTick() {
        if (hitboxEntity == null) return;

        Player target = findNearestPlayer(aggroRadius);
        if (target == null) return;

        if (hitboxEntity instanceof Mob mob) {
            mob.setTarget(target);
        }

        double distSq = hitboxEntity.getLocation().distanceSquared(target.getLocation());

        // Decay freeze stacks
        processStackDecay();

        switch (currentPhase) {
            case 0 -> phase1Tick(target, distSq);
            case 1 -> phase2Tick(target, distSq);
            default -> phase3Tick(target, distSq);
        }
    }

    private void phase1Tick(Player target, double distSq) {
        // Frost breath (2 stacks)
        if (distSq < 100 && isAttackReady("frost_breath", 50)) {
            useAttack("frost_breath");
            performFrostBreath(target);
        }

        // Ice slam (1 stack, melee)
        if (distSq < 16 && isAttackReady("ice_slam", 35)) {
            useAttack("ice_slam");
            performIceSlam(target);
        }
    }

    private void phase2Tick(Player target, double distSq) {
        phase1Tick(target, distSq);

        // Blizzard (1 stack/s for 4s)
        if (!blizzardActive && isAttackReady("blizzard", 120)) {
            useAttack("blizzard");
            performBlizzard();
        }

        // Faster frost breath
        if (distSq < 100 && isAttackReady("frost_breath", 35)) {
            useAttack("frost_breath");
            performFrostBreath(target);
        }
    }

    private void phase3Tick(Player target, double distSq) {
        // All attacks + icicle rain
        if (isAttackReady("icicle_rain", 80)) {
            useAttack("icicle_rain");
            performIcicleRain();
        }

        if (!blizzardActive && isAttackReady("blizzard", 90)) {
            useAttack("blizzard");
            performBlizzard();
        }

        if (distSq < 100 && isAttackReady("frost_breath", 25)) {
            useAttack("frost_breath");
            performFrostBreath(target);
        }

        if (distSq < 16 && isAttackReady("ice_slam", 25)) {
            useAttack("ice_slam");
            performIceSlam(target);
        }
    }

    // ── Attack Implementations ────────────────────────────────────────

    private void performFrostBreath(Player target) {
        playAnimation("frost_breath", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        Vector dir = target.getLocation().toVector().subtract(loc.toVector()).normalize();
        loc.getWorld().playSound(loc, Sound.ENTITY_SNOW_GOLEM_SHOOT, 2.0f, 0.5f);

        // Cone of frost particles
        for (int i = 1; i <= 10; i++) {
            Location pLoc = loc.clone().add(dir.clone().multiply(i)).add(0, 1.5, 0);
            loc.getWorld().spawnParticle(Particle.SNOWFLAKE, pLoc, 12, 0.5, 0.5, 0.5, 0.03);
            loc.getWorld().spawnParticle(Particle.BLOCK, pLoc, 5, 0.3, 0.3, 0.3, 0,
                    Material.PACKED_ICE.createBlockData());
        }

        for (Player p : findNearbyPlayers(10)) {
            Vector toPlayer = p.getLocation().toVector().subtract(loc.toVector()).normalize();
            if (toPlayer.dot(dir) > 0.4) {
                p.damage(12.0, hitboxEntity);
                addFreezeStacks(p, 2);
                p.setFreezeTicks(Math.min(p.getFreezeTicks() + 40, p.getMaxFreezeTicks()));
            }
        }
    }

    private void performIceSlam(Player target) {
        playAnimation("ice_slam", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        loc.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 2.0f, 0.5f);
        loc.getWorld().spawnParticle(Particle.BLOCK, loc, 30, 1.5, 0.3, 1.5, 0,
                Material.BLUE_ICE.createBlockData());

        for (Player p : findNearbyPlayers(4)) {
            p.damage(16.0, hitboxEntity);
            addFreezeStacks(p, 1);
            Vector kb = p.getLocation().toVector()
                    .subtract(loc.toVector()).normalize().multiply(1.0).setY(0.6);
            p.setVelocity(kb);
        }
    }

    private void performBlizzard() {
        blizzardActive = true;
        playAnimation("blizzard", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 2.0f, 1.5f);

        for (Player p : findNearbyPlayers(aggroRadius)) {
            p.sendMessage(Component.text("Frostmaw summons a blizzard!",
                    NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
        }

        // 4 seconds of blizzard, 1 stack per second
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!alive || hitboxEntity == null) { cancel(); blizzardActive = false; return; }
                ticks += 2;
                Location center = hitboxEntity.getLocation();

                // Blizzard particles
                center.getWorld().spawnParticle(Particle.SNOWFLAKE, center, 40, 8, 4, 8, 0.1);
                center.getWorld().spawnParticle(Particle.CLOUD, center, 15, 6, 3, 6, 0.05);

                // Every second (10 ticks at rate 2 = 5 cycles)
                if (ticks % 10 == 0) {
                    for (Player p : findNearbyPlayers(10)) {
                        p.damage(4.0, hitboxEntity);
                        addFreezeStacks(p, 1);
                        p.setFreezeTicks(Math.min(p.getFreezeTicks() + 20, p.getMaxFreezeTicks()));
                    }
                }

                if (ticks >= 40) { // 4 seconds
                    cancel();
                    blizzardActive = false;
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void performIcicleRain() {
        playAnimation("roar", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        loc.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 2.0f, 1.2f);

        for (Player p : findNearbyPlayers(aggroRadius)) {
            p.sendMessage(Component.text("Icicles rain from above!",
                    NamedTextColor.AQUA).decorate(TextDecoration.ITALIC));
        }

        // Rain icicles over 3 seconds
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!alive || hitboxEntity == null) { cancel(); return; }
                ticks += 2;

                // Spawn icicle at random position near a player
                List<Player> nearby = findNearbyPlayers(15);
                if (!nearby.isEmpty()) {
                    Player victim = nearby.get(random.nextInt(nearby.size()));
                    Location icicleLoc = victim.getLocation().clone().add(
                            random.nextGaussian() * 2, 0, random.nextGaussian() * 2);

                    // Warning marker
                    icicleLoc.getWorld().spawnParticle(Particle.DUST, icicleLoc.clone().add(0, 5, 0),
                            10, 0.3, 2, 0.3, 0,
                            new Particle.DustOptions(Color.fromRGB(150, 200, 255), 1.5f));

                    // Impact after short delay
                    final Location impactLoc = icicleLoc.clone();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            impactLoc.getWorld().spawnParticle(Particle.BLOCK, impactLoc, 15, 0.3, 0.3, 0.3, 0,
                                    Material.PACKED_ICE.createBlockData());
                            impactLoc.getWorld().playSound(impactLoc, Sound.BLOCK_GLASS_BREAK, 1.0f, 1.5f);
                            for (Player p : findNearbyPlayers(aggroRadius)) {
                                if (p.getLocation().distanceSquared(impactLoc) < 4) {
                                    p.damage(10.0, hitboxEntity);
                                    addFreezeStacks(p, 1);
                                }
                            }
                        }
                    }.runTaskLater(plugin, 10L);
                }

                if (ticks >= 30) cancel();
            }
        }.runTaskTimer(plugin, 0L, 4L);
    }

    // ── Freeze Stack System ───────────────────────────────────────────

    private void addFreezeStacks(Player player, int amount) {
        UUID uuid = player.getUniqueId();
        int current = freezeStacks.getOrDefault(uuid, 0);
        int newStacks = current + amount;

        if (newStacks >= 5) {
            // FROZEN for 3 seconds
            freezeStacks.put(uuid, 0);
            freezePlayer(player);
        } else {
            freezeStacks.put(uuid, newStacks);
            lastStackDecay.putIfAbsent(uuid, combatTicks);
            player.sendActionBar(Component.text("Freeze: " + newStacks + "/5",
                    NamedTextColor.AQUA));
        }
    }

    private void freezePlayer(Player player) {
        player.sendMessage(Component.text("You are FROZEN!",
                NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.5f, 0.5f);
        player.getWorld().spawnParticle(Particle.BLOCK, player.getLocation(), 30, 0.5, 1, 0.5, 0,
                Material.BLUE_ICE.createBlockData());

        // Stun effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 100)); // Immobile
        player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 60, 100));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 60, 250)); // Prevent jumping (negative)

        // Frozen particles for duration
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                ticks += 5;
                if (ticks > 60 || !player.isOnline()) { cancel(); return; }
                player.getWorld().spawnParticle(Particle.SNOWFLAKE,
                        player.getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0.02);
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    private void processStackDecay() {
        // Decay 1 stack every 5 seconds (25 combat ticks at rate 2)
        Iterator<Map.Entry<UUID, Integer>> it = freezeStacks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> entry = it.next();
            UUID uuid = entry.getKey();
            long lastDecay = lastStackDecay.getOrDefault(uuid, combatTicks);

            if (combatTicks - lastDecay >= 25) {
                int stacks = entry.getValue() - 1;
                if (stacks <= 0) {
                    it.remove();
                    lastStackDecay.remove(uuid);
                } else {
                    entry.setValue(stacks);
                    lastStackDecay.put(uuid, combatTicks);
                }
            }
        }
    }

    // ── Phase Transitions ─────────────────────────────────────────────

    @Override
    protected void onPhaseTransition(int oldPhase, int newPhase) {
        super.onPhaseTransition(oldPhase, newPhase);
        Location loc = hitboxEntity.getLocation();

        if (newPhase == 1) {
            loc.getWorld().playSound(loc, Sound.ENTITY_POLAR_BEAR_WARNING, 2.0f, 0.4f);
            loc.getWorld().spawnParticle(Particle.SNOWFLAKE, loc, 80, 4, 4, 4, 0.2);
            for (Player p : findNearbyPlayers(aggroRadius)) {
                p.sendMessage(Component.text("Frostmaw howls, summoning a blizzard!",
                        NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
            }
        } else if (newPhase == 2) {
            enraged = true;
            playAnimation("enrage", AnimationIterator.Type.PLAY_ONCE);
            loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.7f);
            loc.getWorld().spawnParticle(Particle.SNOWFLAKE, loc, 150, 5, 5, 5, 0.3);
            for (Player p : findNearbyPlayers(aggroRadius)) {
                p.sendMessage(Component.text("Frostmaw unleashes an ice storm!",
                        NamedTextColor.DARK_AQUA).decorate(TextDecoration.BOLD));
            }
        }
    }

    // ── Death + Drops ─────────────────────────────────────────────────

    @Override
    protected void onDeath(Player killer) {
        super.onDeath(killer);
        freezeStacks.clear();
        lastStackDecay.clear();
        blizzardActive = false;

        if (hitboxEntity != null) {
            Location loc = hitboxEntity.getLocation();
            loc.getWorld().playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.0f, 0.9f);
            loc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 120, 3, 3, 3, 0.4);
            loc.getWorld().spawnParticle(Particle.SNOWFLAKE, loc, 80, 3, 3, 3, 0.2);
        }

        for (Player p : findNearbyPlayers(100)) {
            p.sendMessage(Component.text("Frostmaw has been defeated!",
                    NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        }

        if (killer != null) {
            killer.giveExp(2500);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();

        // 1 MYTHIC legendary weapon
        LegendaryWeapon[] mythicWeapons = LegendaryWeapon.byTier(LegendaryTier.MYTHIC);
        if (mythicWeapons.length > 0) {
            LegendaryWeaponManager wm = JGlimsPlugin.getInstance().getLegendaryWeaponManager();
            drops.add(wm.createWeapon(mythicWeapons[random.nextInt(mythicWeapons.length)]));
        }

        // Blue ice x32
        drops.add(new ItemStack(Material.BLUE_ICE, 32));

        // Packed ice x64
        drops.add(new ItemStack(Material.PACKED_ICE, 64));

        return drops;
    }
}
