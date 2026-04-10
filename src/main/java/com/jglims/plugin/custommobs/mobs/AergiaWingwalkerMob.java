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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Aergia Wingwalker - Dual boss/mount. HP 500 as boss, 120 as mount.
 * If player approaches wearing ANY armor -> hostile boss (wind slash, aerial charge, feather storm).
 * If NO armor + bare hands -> peaceful. Ride 60s to calm. Feed cooked beef + saddle -> tamed.
 * Can fly. Special: fireball (8 dmg 3-block AoE, 5s CD).
 */
public class AergiaWingwalkerMob extends RideableMobEntity {

    // Dual state
    private boolean bossMode = false;
    private boolean peaceful = false;
    private boolean calmed = false;
    private long calmRideStartTick = -1;
    private static final long CALM_RIDE_TICKS = 1200L; // 60 seconds
    private boolean cookedBeefFed = false;

    // Boss bar
    private BossBar bossBar;
    private boolean inCombat = false;
    private long combatTicks = 0;
    private final Map<String, Long> attackCooldowns = new HashMap<>();

    public AergiaWingwalkerMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.AERGIA_WINGWALKER);
        this.canFly = true;
        this.mountSpeed = 0.25;
        this.jumpStrength = 1.5;
        this.maxRiders = 1;
        this.tamingRequirement = 1.0;
        this.fallDamageReduction = 0.0;
        this.specialAbilityCooldownTicks = 100L; // 5s CD
    }

    @Override
    protected void onSpawn() {
        playAnimation("idle", AnimationIterator.Type.LOOP);
        bossBar = BossBar.bossBar(
                Component.text("Aergia Wingwalker", NamedTextColor.AQUA).decorate(TextDecoration.BOLD),
                1.0f, BossBar.Color.BLUE, BossBar.Overlay.NOTCHED_10);
    }

    @Override
    protected void onTick() {
        super.onTick();
        if (hitboxEntity == null || !alive) return;

        if (tamed) {
            tamedTick();
            return;
        }

        // Calming ride in progress
        if (calmRideStartTick > 0 && !hitboxEntity.getPassengers().isEmpty()) {
            long ridden = rideTicks - calmRideStartTick;
            if (ridden >= CALM_RIDE_TICKS) {
                calmed = true;
                calmRideStartTick = -1;
                for (Entity passenger : hitboxEntity.getPassengers()) {
                    if (passenger instanceof Player p) {
                        p.sendMessage(Component.text("The Wingwalker has calmed down! Feed it cooked beef.",
                                NamedTextColor.GREEN));
                    }
                }
            }
            return;
        }

        // Detect approaching players for dual behavior
        Player nearest = findNearestPlayer(30);
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
                mob.getPathfinder().moveTo(owner.getLocation(), 1.2);
            }
        }
    }

    private void enterBossMode(Player target) {
        bossMode = true;
        inCombat = true;
        combatTicks = 0;
        for (Player p : findNearbyPlayers(50)) {
            p.showBossBar(bossBar);
            p.sendMessage(Component.text("The Aergia Wingwalker attacks!", NamedTextColor.RED)
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
        // Heal to full
        if (hitboxEntity != null) {
            hitboxEntity.setHealth(mobType.getMaxHealth());
        }
        playAnimation("idle", AnimationIterator.Type.LOOP);
    }

    private void bossCombatTick() {
        Player target = findNearestPlayer(40);
        if (target == null) {
            leaveCombat();
            return;
        }

        if (hitboxEntity instanceof Mob mob) {
            mob.setTarget(target);
        }

        double dist = hitboxEntity.getLocation().distanceSquared(target.getLocation());

        // Wind Slash - ranged
        if (dist > 16 && dist < 400 && isAttackReady("wind_slash", 40)) {
            markAttack("wind_slash");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            Vector dir = target.getLocation().toVector()
                    .subtract(hitboxEntity.getLocation().toVector()).normalize().multiply(1.5);
            target.setVelocity(dir.setY(0.5));
            target.damage(10, hitboxEntity);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_PHANTOM_FLAP, 2.0f, 1.5f);
        }

        // Aerial Charge
        if (dist > 36 && dist < 225 && isAttackReady("aerial_charge", 80)) {
            markAttack("aerial_charge");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            Vector charge = target.getLocation().toVector()
                    .subtract(hitboxEntity.getLocation().toVector()).normalize().multiply(2.0);
            charge.setY(0.3);
            hitboxEntity.setVelocity(charge);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_PHANTOM_SWOOP, 2.0f, 1.0f);
            // Damage on next close tick handled by melee below
        }

        // Feather Storm AoE
        if (dist < 100 && isAttackReady("feather_storm", 120)) {
            markAttack("feather_storm");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            for (Player p : findNearbyPlayers(8)) {
                p.damage(7, hitboxEntity);
                Vector knock = p.getLocation().toVector()
                        .subtract(hitboxEntity.getLocation().toVector()).normalize().multiply(0.8);
                knock.setY(0.6);
                p.setVelocity(knock);
            }
            hitboxEntity.getWorld().spawnParticle(Particle.CLOUD,
                    hitboxEntity.getLocation().add(0, 2, 0), 40, 4, 2, 4, 0.1);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_ENDER_DRAGON_FLAP, 2.0f, 1.2f);
        }

        // Melee swipe
        if (dist < 9 && isAttackReady("melee", 25)) {
            markAttack("melee");
            target.damage(14, hitboxEntity);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_PHANTOM_BITE, 1.0f, 0.8f);
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
            player.sendMessage(Component.text("Remove your armor and approach with bare hands!",
                    NamedTextColor.YELLOW));
            return;
        }

        if (!calmed) {
            // Mount to calm
            if (hitboxEntity.getPassengers().isEmpty()) {
                hitboxEntity.addPassenger(player);
                calmRideStartTick = rideTicks;
                player.sendMessage(Component.text("Hold on! Ride for 60 seconds to calm the Wingwalker.",
                        NamedTextColor.AQUA));
            }
            return;
        }

        // Calmed - feed cooked beef then saddle
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!cookedBeefFed) {
            if (hand.getType() == Material.COOKED_BEEF) {
                hand.setAmount(hand.getAmount() - 1);
                cookedBeefFed = true;
                hitboxEntity.getWorld().spawnParticle(Particle.HEART,
                        hitboxEntity.getLocation().add(0, 2, 0), 10, 0.5, 0.5, 0.5, 0);
                player.sendMessage(Component.text("Now apply a saddle!", NamedTextColor.GREEN));
            } else {
                player.sendMessage(Component.text("Feed it cooked beef to bond.", NamedTextColor.GRAY));
            }
            return;
        }

        if (hand.getType() == Material.SADDLE) {
            hand.setAmount(hand.getAmount() - 1);
            saddled = true;

            // Reduce HP to mount HP
            Objects.requireNonNull(hitboxEntity.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(120);
            hitboxEntity.setHealth(120);

            addTamingProgress(tamingRequirement, player);

            if (bossBar != null) {
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    p.hideBossBar(bossBar);
                }
            }
        } else {
            player.sendMessage(Component.text("Apply a saddle to finish taming!", NamedTextColor.GRAY));
        }
    }

    @Override
    protected void onTamed(Player player) {
        if (hitboxEntity != null) {
            hitboxEntity.customName(Component.text(player.getName() + "'s Wingwalker",
                    NamedTextColor.AQUA));
        }
        bossMode = false;
        inCombat = false;
    }

    /**
     * Special ability: Fireball (8 dmg, 3-block AoE, 5s CD).
     */
    @Override
    protected void onSpecialAbility(Player rider) {
        if (hitboxEntity == null) return;

        Location loc = hitboxEntity.getLocation().add(0, 1, 0);
        Vector dir = rider.getLocation().getDirection().normalize();

        // Launch fireball
        Fireball fireball = hitboxEntity.getWorld().spawn(
                loc.add(dir.multiply(2)), Fireball.class);
        fireball.setDirection(dir.multiply(0.5));
        fireball.setYield(0);
        fireball.setIsIncendiary(false);
        fireball.setShooter(hitboxEntity);

        // Custom AoE on impact
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ > 60 || fireball.isDead()) {
                    cancel();
                    if (!fireball.isDead()) fireball.remove();
                    Location impact = fireball.isDead() ? fireball.getLocation() : hitboxEntity.getLocation();
                    for (Entity e : impact.getWorld().getNearbyEntities(impact, 3, 3, 3)) {
                        if (e instanceof LivingEntity le && !(e instanceof Player p && isOwner(p))
                                && e != hitboxEntity) {
                            le.damage(8, hitboxEntity);
                        }
                    }
                    impact.getWorld().spawnParticle(Particle.EXPLOSION, impact, 3, 1, 1, 1, 0);
                    return;
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);

        hitboxEntity.getWorld().playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 1.5f, 1.0f);
        rider.sendMessage(Component.text("Fireball!", NamedTextColor.GOLD));
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
                    Sound.ENTITY_ENDER_DRAGON_DEATH, 0.6f, 1.5f);
        }
        if (killer != null) {
            killer.giveExp(100);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        if (tamed) return Collections.emptyList();
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(Material.FEATHER, 8 + (int) (Math.random() * 8)));
        drops.add(new ItemStack(Material.DIAMOND, 2 + (int) (Math.random() * 3)));

        // EPIC legendary drop
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
