package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobType;
import com.jglims.plugin.custommobs.RideableMobEntity;
import kr.toxicity.model.api.animation.AnimationIterator;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Demonic Wingwalker - Nether-themed dual boss/mount. HP 700 as boss, 180 as mount.
 * Same dual mechanic as Aergia but stronger. Hellfire breath, demonic charge, soul drain.
 * Special: hellfire ball (10 dmg 4-block AoE, fire, 5s CD). Fire-immune when tamed.
 */
public class DemonicWingwalkerMob extends RideableMobEntity {

    private boolean bossMode = false;
    private boolean peaceful = false;
    private boolean calmed = false;
    private long calmRideStartTick = -1;
    private static final long CALM_RIDE_TICKS = 1200L;
    private boolean cookedBeefFed = false;

    private BossBar bossBar;
    private boolean inCombat = false;
    private long combatTicks = 0;
    private final Map<String, Long> attackCooldowns = new HashMap<>();

    public DemonicWingwalkerMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.DEMONIC_WINGWALKER);
        this.canFly = true;
        this.mountSpeed = 0.28;
        this.jumpStrength = 1.8;
        this.maxRiders = 1;
        this.tamingRequirement = 1.0;
        this.fallDamageReduction = 0.0;
        this.specialAbilityCooldownTicks = 100L; // 5s
    }

    @Override
    protected void onSpawn() {
        playAnimation("idle", AnimationIterator.Type.LOOP);
        bossBar = BossBar.bossBar(
                Component.text("Demonic Wingwalker", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD),
                1.0f, BossBar.Color.RED, BossBar.Overlay.NOTCHED_10);
    }

    @Override
    protected void onTick() {
        super.onTick();
        if (hitboxEntity == null || !alive) return;

        // Fire immunity when tamed
        if (tamed) {
            hitboxEntity.setFireTicks(0);
            tamedTick();
            return;
        }

        if (calmRideStartTick > 0 && !hitboxEntity.getPassengers().isEmpty()) {
            long ridden = rideTicks - calmRideStartTick;
            if (ridden >= CALM_RIDE_TICKS) {
                calmed = true;
                calmRideStartTick = -1;
                for (Entity passenger : hitboxEntity.getPassengers()) {
                    if (passenger instanceof Player p) {
                        p.sendMessage(Component.text("The Demonic Wingwalker submits! Feed it cooked beef.",
                                NamedTextColor.RED));
                    }
                }
            }
            return;
        }

        Player nearest = findNearestPlayer(35);
        if (nearest == null) {
            if (inCombat) leaveCombat();
            return;
        }

        if (!inCombat && !peaceful) {
            if (isWearingArmor(nearest)) {
                enterBossMode(nearest);
            } else if (nearest.getInventory().getItemInMainHand().getType() == Material.AIR) {
                peaceful = true;
            }
        }

        if (bossMode && inCombat) {
            combatTicks++;
            bossCombatTick();
            updateBossBar();
        }
    }

    private void tamedTick() {
        Player owner = getOwner();
        if (owner == null) return;
        if (hitboxEntity.getPassengers().isEmpty()) {
            double dist = hitboxEntity.getLocation().distanceSquared(owner.getLocation());
            if (dist > 400) {
                hitboxEntity.teleport(owner.getLocation().add(2, 0, 2));
            } else if (dist > 20 && hitboxEntity instanceof Mob mob) {
                mob.getPathfinder().moveTo(owner.getLocation(), 1.3);
            }
        }
        // Nether particles
        hitboxEntity.getWorld().spawnParticle(Particle.FLAME,
                hitboxEntity.getLocation().add(0, 1, 0), 2, 0.3, 0.2, 0.3, 0.01);
    }

    private void enterBossMode(Player target) {
        bossMode = true;
        inCombat = true;
        combatTicks = 0;
        for (Player p : findNearbyPlayers(50)) {
            p.showBossBar(bossBar);
            p.sendMessage(Component.text("The Demonic Wingwalker roars with hellfire!", NamedTextColor.DARK_RED)
                    .decorate(TextDecoration.BOLD));
        }
        playAnimation("walk", AnimationIterator.Type.LOOP);
    }

    private void leaveCombat() {
        inCombat = false;
        bossMode = false;
        combatTicks = 0;
        if (bossBar != null) {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                p.hideBossBar(bossBar);
            }
        }
        if (hitboxEntity != null) {
            hitboxEntity.setHealth(mobType.getMaxHealth());
        }
        playAnimation("idle", AnimationIterator.Type.LOOP);
    }

    private void bossCombatTick() {
        Player target = findNearestPlayer(45);
        if (target == null) {
            leaveCombat();
            return;
        }

        if (hitboxEntity instanceof Mob mob) {
            mob.setTarget(target);
        }

        double dist = hitboxEntity.getLocation().distanceSquared(target.getLocation());

        // Hellfire Breath - cone AoE
        if (dist < 100 && isAttackReady("hellfire_breath", 60)) {
            markAttack("hellfire_breath");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            Vector dir = target.getLocation().toVector()
                    .subtract(hitboxEntity.getLocation().toVector()).normalize();
            for (Player p : findNearbyPlayers(8)) {
                Vector toPlayer = p.getLocation().toVector()
                        .subtract(hitboxEntity.getLocation().toVector()).normalize();
                if (dir.dot(toPlayer) > 0.5) { // Within cone
                    p.damage(12, hitboxEntity);
                    p.setFireTicks(80);
                }
            }
            Location flameStart = hitboxEntity.getLocation().add(0, 1.5, 0);
            for (double d = 0; d < 8; d += 0.5) {
                Location flameLoc = flameStart.clone().add(dir.clone().multiply(d));
                hitboxEntity.getWorld().spawnParticle(Particle.FLAME, flameLoc, 5, 0.3, 0.3, 0.3, 0.02);
                hitboxEntity.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, flameLoc, 3, 0.2, 0.2, 0.2, 0.01);
            }
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_BLAZE_SHOOT, 2.0f, 0.5f);
        }

        // Demonic Charge
        if (dist > 49 && dist < 256 && isAttackReady("demonic_charge", 80)) {
            markAttack("demonic_charge");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            Vector charge = target.getLocation().toVector()
                    .subtract(hitboxEntity.getLocation().toVector()).normalize().multiply(2.5);
            charge.setY(0.2);
            hitboxEntity.setVelocity(charge);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_WITHER_SHOOT, 1.5f, 0.7f);
        }

        // Soul Drain - lifesteal
        if (dist < 36 && isAttackReady("soul_drain", 100)) {
            markAttack("soul_drain");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            target.damage(15, hitboxEntity);
            target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 1));
            double healAmount = Math.min(15, mobType.getMaxHealth() - hitboxEntity.getHealth());
            hitboxEntity.setHealth(hitboxEntity.getHealth() + healAmount);
            hitboxEntity.getWorld().spawnParticle(Particle.SOUL,
                    target.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0.05);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_WITHER_AMBIENT, 1.0f, 1.5f);
        }

        // Basic melee
        if (dist < 9 && isAttackReady("melee", 20)) {
            markAttack("melee");
            target.damage(18, hitboxEntity);
            target.setFireTicks(40);
        }
    }

    @Override
    public void onInteract(Player player) {
        if (tamed) {
            super.onInteract(player);
            return;
        }

        if (bossMode || inCombat) return;

        if (!peaceful) {
            player.sendMessage(Component.text("Remove all armor and approach with empty hands!",
                    NamedTextColor.YELLOW));
            return;
        }

        if (!calmed) {
            if (hitboxEntity.getPassengers().isEmpty()) {
                hitboxEntity.addPassenger(player);
                calmRideStartTick = rideTicks;
                player.sendMessage(Component.text("Hold on tight! Ride for 60 seconds to break it.",
                        NamedTextColor.RED));
            }
            return;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!cookedBeefFed) {
            if (hand.getType() == Material.COOKED_BEEF) {
                hand.setAmount(hand.getAmount() - 1);
                cookedBeefFed = true;
                hitboxEntity.getWorld().spawnParticle(Particle.HEART,
                        hitboxEntity.getLocation().add(0, 2, 0), 10, 0.5, 0.5, 0.5, 0);
                player.sendMessage(Component.text("Now apply a saddle!", NamedTextColor.GREEN));
            } else {
                player.sendMessage(Component.text("Feed it cooked beef.", NamedTextColor.GRAY));
            }
            return;
        }

        if (hand.getType() == Material.SADDLE) {
            hand.setAmount(hand.getAmount() - 1);
            saddled = true;
            Objects.requireNonNull(hitboxEntity.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(180);
            hitboxEntity.setHealth(180);
            addTamingProgress(tamingRequirement, player);
            if (bossBar != null) {
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    p.hideBossBar(bossBar);
                }
            }
        }
    }

    @Override
    protected void onTamed(Player player) {
        if (hitboxEntity != null) {
            hitboxEntity.customName(Component.text(player.getName() + "'s Demonic Wingwalker",
                    NamedTextColor.DARK_RED));
        }
        bossMode = false;
        inCombat = false;
    }

    /**
     * Hellfire Ball - 10 dmg, 4-block AoE, sets fire.
     */
    @Override
    protected void onSpecialAbility(Player rider) {
        if (hitboxEntity == null) return;

        Location loc = hitboxEntity.getLocation().add(0, 1.5, 0);
        Vector dir = rider.getLocation().getDirection().normalize();

        Fireball fireball = hitboxEntity.getWorld().spawn(
                loc.add(dir.multiply(2)), Fireball.class);
        fireball.setDirection(dir.multiply(0.6));
        fireball.setYield(0);
        fireball.setIsIncendiary(false);
        fireball.setShooter(hitboxEntity);

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ > 60 || fireball.isDead()) {
                    cancel();
                    if (!fireball.isDead()) fireball.remove();
                    Location impact = fireball.getLocation();
                    for (Entity e : impact.getWorld().getNearbyEntities(impact, 4, 4, 4)) {
                        if (e instanceof LivingEntity le && !(e instanceof Player p && isOwner(p))
                                && e != hitboxEntity) {
                            le.damage(10, hitboxEntity);
                            le.setFireTicks(80);
                        }
                    }
                    impact.getWorld().spawnParticle(Particle.FLAME, impact, 30, 2, 2, 2, 0.1);
                    impact.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, impact, 20, 2, 2, 2, 0.05);
                    impact.getWorld().playSound(impact, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.8f);
                    return;
                }
                fireball.getWorld().spawnParticle(Particle.FLAME,
                        fireball.getLocation(), 3, 0.2, 0.2, 0.2, 0.01);
            }
        }.runTaskTimer(plugin, 1L, 1L);

        hitboxEntity.getWorld().playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 2.0f, 0.5f);
        rider.sendMessage(Component.text("Hellfire!", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
    }

    @Override
    protected void onDamage(double amount, Player source) {
        playAnimation("hurt", AnimationIterator.Type.PLAY_ONCE);
        if (source != null && !tamed && !inCombat) {
            enterBossMode(source);
        }
        updateBossBar();
    }

    @Override
    protected void onDeath(Player killer) {
        if (bossBar != null) {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                p.hideBossBar(bossBar);
            }
        }
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_WITHER_DEATH, 0.8f, 1.2f);
        }
        if (killer != null) {
            killer.giveExp(150);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        if (tamed) return Collections.emptyList();
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(Material.BLAZE_ROD, 4 + (int) (Math.random() * 4)));
        drops.add(new ItemStack(Material.NETHER_STAR, 1));
        drops.add(new ItemStack(Material.DIAMOND, 3 + (int) (Math.random() * 4)));

        com.jglims.plugin.legendary.LegendaryWeapon[] weapons =
                com.jglims.plugin.legendary.LegendaryWeapon.byTier(
                        com.jglims.plugin.legendary.LegendaryTier.EPIC);
        if (weapons.length > 0) {
            com.jglims.plugin.legendary.LegendaryWeaponManager wm =
                    JGlimsPlugin.getInstance().getLegendaryWeaponManager();
            drops.add(wm.createWeapon(weapons[new Random().nextInt(weapons.length)]));
        }
        return drops;
    }

    private void updateBossBar() {
        if (bossBar == null || hitboxEntity == null) return;
        float progress = (float) (hitboxEntity.getHealth() / mobType.getMaxHealth());
        bossBar.progress(Math.max(0f, Math.min(1f, progress)));
    }

    private boolean isAttackReady(String name, long cooldown) {
        Long last = attackCooldowns.get(name);
        return last == null || (combatTicks - last) >= cooldown;
    }

    private void markAttack(String name) {
        attackCooldowns.put(name, combatTicks);
    }

    private boolean isWearingArmor(Player player) {
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor != null && armor.getType() != Material.AIR) return true;
        }
        return false;
    }

    private Player getOwner() {
        if (ownerUUID == null) return null;
        return plugin.getServer().getPlayer(ownerUUID);
    }

    @Override
    protected long getTickRate() {
        return 4L;
    }
}
