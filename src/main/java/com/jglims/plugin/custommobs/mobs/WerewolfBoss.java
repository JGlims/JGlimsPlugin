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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Whulvk Werewolf Lycan - Night-only werewolf boss. HP 800.
 * 2 phases (40%). Speed II passive. Fast claw combos, pounce,
 * howl summons, and beast mode with regen and bleeding DOT.
 */
public class WerewolfBoss extends CustomWorldBoss {

    private final Random random = new Random();

    /** Tracks bleeding stacks per player UUID. Max 3 stacks. */
    private final Map<UUID, Integer> bleedingStacks = new HashMap<>();
    private final Map<UUID, Long> bleedingTimers = new HashMap<>();

    public WerewolfBoss(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.WHULVK_WEREWOLF);
        phaseThresholds = new double[]{0.40};
        aggroRadius = 35.0;
        arenaRadius = 40.0;
        announceRadius = 100.0;
    }

    @Override
    protected void onSpawn() {
        super.onSpawn();
        if (hitboxEntity != null) {
            Location loc = hitboxEntity.getLocation();
            loc.getWorld().playSound(loc, Sound.ENTITY_WOLF_AMBIENT, 3.0f, 0.5f);
            // Speed II passive
            hitboxEntity.addPotionEffect(new PotionEffect(
                    PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
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

        // Process bleeding ticks
        processBleedingTicks();

        // Phase 2 regen (2 HP/s = 4 HP per combat tick cycle at rate 2)
        if (currentPhase >= 1 && combatTicks % 5 == 0) {
            double maxHp = mobType.getMaxHealth();
            double newHp = Math.min(hitboxEntity.getHealth() + 2.0, maxHp);
            hitboxEntity.setHealth(newHp);
        }

        switch (currentPhase) {
            case 0 -> phase1Tick(target, distSq);
            default -> phase2Tick(target, distSq);
        }
    }

    private void phase1Tick(Player target, double distSq) {
        // Fast claw combos
        if (distSq < 12 && isAttackReady("claw_combo", 25)) {
            useAttack("claw_combo");
            performClawCombo(target);
        }

        // Pounce (10 blocks)
        if (distSq > 16 && distSq < 100 && isAttackReady("pounce", 60)) {
            useAttack("pounce");
            performPounce(target);
        }

        // Howl (summons 4 wolves)
        if (isAttackReady("howl", 150)) {
            useAttack("howl");
            performHowl();
        }
    }

    private void phase2Tick(Player target, double distSq) {
        // Beast mode - faster attacks, bleeding DOT

        // Fast claw combos with bleeding
        if (distSq < 12 && isAttackReady("claw_combo", 16)) {
            useAttack("claw_combo");
            performBeastClawCombo(target);
        }

        // Faster pounce
        if (distSq > 16 && distSq < 100 && isAttackReady("pounce", 40)) {
            useAttack("pounce");
            performPounce(target);
        }

        // Howl (more wolves)
        if (isAttackReady("howl", 100)) {
            useAttack("howl");
            performHowl();
        }

        // Savage lunge - new phase 2 attack
        if (distSq > 9 && distSq < 49 && isAttackReady("savage_lunge", 50)) {
            useAttack("savage_lunge");
            performSavageLunge(target);
        }
    }

    // ── Attack Implementations ────────────────────────────────────────

    private void performClawCombo(Player target) {
        playAnimation("claw_attack", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 1.2f);

        // 3 fast hits
        for (int i = 0; i < 3; i++) {
            final int hit = i;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!alive || hitboxEntity == null) return;
                    if (hitboxEntity.getLocation().distanceSquared(target.getLocation()) > 16) return;
                    target.damage(6.0, hitboxEntity);
                    hitboxEntity.getWorld().spawnParticle(Particle.SWEEP_ATTACK,
                            target.getLocation().add(0, 1, 0), 1, 0, 0, 0, 0);
                }
            }.runTaskLater(plugin, (long) i * 4);
        }
    }

    private void performBeastClawCombo(Player target) {
        playAnimation("beast_claw", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.5f, 1.0f);

        // 3 fast hits with bleeding
        for (int i = 0; i < 3; i++) {
            final int hit = i;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!alive || hitboxEntity == null) return;
                    if (hitboxEntity.getLocation().distanceSquared(target.getLocation()) > 16) return;
                    target.damage(8.0, hitboxEntity);
                    applyBleeding(target);
                    hitboxEntity.getWorld().spawnParticle(Particle.SWEEP_ATTACK,
                            target.getLocation().add(0, 1, 0), 1, 0, 0, 0, 0);
                    target.getWorld().spawnParticle(Particle.DUST,
                            target.getLocation().add(0, 1, 0), 5, 0.2, 0.2, 0.2, 0,
                            new Particle.DustOptions(Color.RED, 1.0f));
                }
            }.runTaskLater(plugin, (long) i * 3);
        }
    }

    private void performPounce(Player target) {
        playAnimation("pounce", AnimationIterator.Type.PLAY_ONCE);
        Location start = hitboxEntity.getLocation();
        hitboxEntity.getWorld().playSound(start, Sound.ENTITY_WOLF_AMBIENT, 1.5f, 0.5f);

        // Launch toward target
        Vector dir = target.getLocation().toVector().subtract(start.toVector()).normalize();
        Location dest = target.getLocation();
        moveTo(dest);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!alive || hitboxEntity == null) return;
                for (Player p : findNearbyPlayers(3)) {
                    p.damage(15.0, hitboxEntity);
                    p.setVelocity(new Vector(0, 0.5, 0).add(
                            dir.clone().multiply(0.5)));
                }
                hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                        Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.5f, 0.8f);
            }
        }.runTaskLater(plugin, 5L);
    }

    private void performHowl() {
        playAnimation("howl", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_WOLF_AMBIENT, 3.0f, 0.6f);

        for (Player p : findNearbyPlayers(aggroRadius)) {
            p.sendMessage(Component.text("The Werewolf howls, summoning its pack!",
                    NamedTextColor.RED).decorate(TextDecoration.ITALIC));
        }

        int wolfCount = currentPhase >= 1 ? 6 : 4;
        for (int i = 0; i < wolfCount; i++) {
            Location spawnLoc = loc.clone().add(
                    random.nextGaussian() * 5, 0, random.nextGaussian() * 5);
            Wolf wolf = (Wolf) loc.getWorld().spawnEntity(spawnLoc, EntityType.WOLF);
            wolf.setAngry(true);
            wolf.customName(Component.text("Pack Wolf", NamedTextColor.DARK_RED));
        }
    }

    private void performSavageLunge(Player target) {
        playAnimation("lunge", AnimationIterator.Type.PLAY_ONCE);
        Location dest = target.getLocation();
        moveTo(dest);

        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.ENTITY_RAVAGER_ROAR, 1.5f, 1.0f);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!alive || hitboxEntity == null) return;
                target.damage(20.0, hitboxEntity);
                applyBleeding(target);
                applyBleeding(target); // Double stack
                target.getWorld().spawnParticle(Particle.DUST,
                        target.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0,
                        new Particle.DustOptions(Color.RED, 1.5f));
            }
        }.runTaskLater(plugin, 3L);
    }

    // ── Bleeding System ───────────────────────────────────────────────

    private void applyBleeding(Player target) {
        UUID uuid = target.getUniqueId();
        int stacks = bleedingStacks.getOrDefault(uuid, 0);
        if (stacks < 3) {
            bleedingStacks.put(uuid, stacks + 1);
            bleedingTimers.put(uuid, combatTicks);
            target.sendMessage(Component.text("Bleeding! (" + (stacks + 1) + "/3 stacks)",
                    NamedTextColor.RED));
        }
    }

    private void processBleedingTicks() {
        Iterator<Map.Entry<UUID, Integer>> it = bleedingStacks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> entry = it.next();
            UUID uuid = entry.getKey();
            int stacks = entry.getValue();
            long startTick = bleedingTimers.getOrDefault(uuid, 0L);

            // Bleeding lasts 5 seconds (50 ticks at rate 2 = 25 cycles)
            if (combatTicks - startTick > 25) {
                // Decay 1 stack per 5 seconds
                if (stacks > 1) {
                    entry.setValue(stacks - 1);
                    bleedingTimers.put(uuid, combatTicks);
                } else {
                    it.remove();
                    bleedingTimers.remove(uuid);
                }
                continue;
            }

            // Deal 1 damage per second per stack (every 10 combat ticks)
            if (combatTicks % 5 == 0) {
                Player p = plugin.getServer().getPlayer(uuid);
                if (p != null && p.isOnline()) {
                    p.damage(stacks * 1.0);
                    p.getWorld().spawnParticle(Particle.DUST,
                            p.getLocation().add(0, 1, 0), 3, 0.2, 0.3, 0.2, 0,
                            new Particle.DustOptions(Color.RED, 0.8f));
                }
            }
        }
    }

    // ── Phase Transitions ─────────────────────────────────────────────

    @Override
    protected void onPhaseTransition(int oldPhase, int newPhase) {
        super.onPhaseTransition(oldPhase, newPhase);
        if (newPhase == 1) {
            enraged = true;
            playAnimation("transform", AnimationIterator.Type.PLAY_ONCE);
            Location loc = hitboxEntity.getLocation();
            loc.getWorld().playSound(loc, Sound.ENTITY_WOLF_AMBIENT, 3.0f, 0.3f);
            loc.getWorld().spawnParticle(Particle.DUST, loc, 50, 2, 2, 2, 0,
                    new Particle.DustOptions(Color.RED, 2.0f));

            // Speed III in beast mode
            hitboxEntity.addPotionEffect(new PotionEffect(
                    PotionEffectType.SPEED, Integer.MAX_VALUE, 2, false, false));

            for (Player p : findNearbyPlayers(aggroRadius)) {
                p.sendMessage(Component.text("The Werewolf enters Beast Mode!",
                        NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
            }
        }
    }

    // ── Death + Drops ─────────────────────────────────────────────────

    @Override
    protected void onDeath(Player killer) {
        super.onDeath(killer);
        bleedingStacks.clear();
        bleedingTimers.clear();
        if (hitboxEntity != null) {
            Location loc = hitboxEntity.getLocation();
            loc.getWorld().playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.0f, 0.9f);
            loc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 100, 2, 2, 2, 0.3);
        }

        for (Player p : findNearbyPlayers(100)) {
            p.sendMessage(Component.text("The Werewolf has been slain!",
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

        // 24 diamonds
        drops.add(new ItemStack(Material.DIAMOND, 24));

        return drops;
    }
}
