package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobType;
import com.jglims.plugin.custommobs.CustomWorldBoss;
import kr.toxicity.model.api.animation.AnimationIterator;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Winged Unicorn - Dual mob: boss (HP 300) or mount (HP 80).
 * If player approaches with armor -> boss fight.
 * Without armor + bare hands -> peaceful, can feed golden apple to mount and tame.
 * As mount: fastest horse, Slow Falling on jumps.
 * Special ability: rainbow dash (speed burst + rainbow particles, 8s CD).
 */
public class WingedUnicornMob extends CustomWorldBoss {

    private boolean peaceful = false;
    private boolean tameable = false;
    private boolean tamed = false;
    private UUID ownerUUID;
    private boolean saddled = false;
    private boolean goldenAppleFed = false;
    private long rideTicks = 0;
    private long lastSpecialAbilityTick = 0;
    private static final long SPECIAL_COOLDOWN = 160L; // 8 seconds
    private long lastAttackTick = 0;

    public WingedUnicornMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.WINGED_UNICORN);
        this.aggroRadius = 30.0;
        setPhaseThresholds(0.5, 0.25);
    }

    @Override
    protected void onSpawn() {
        super.onSpawn();
        peaceful = false;
        tameable = false;
    }

    @Override
    protected void onTick() {
        if (hitboxEntity == null || !alive) return;

        if (tamed) {
            tamedTick();
            return;
        }

        if (peaceful) {
            // Peaceful mode - sparkle particles
            hitboxEntity.getWorld().spawnParticle(Particle.END_ROD,
                    hitboxEntity.getLocation().add(0, 2, 0), 2, 0.5, 0.5, 0.5, 0.01);
            return;
        }

        // Check approaching players for dual behavior
        Player nearest = findNearestPlayer(aggroRadius);
        if (nearest != null && !inCombat) {
            if (isWearingArmor(nearest)) {
                // Boss mode
                super.onTick();
            } else if (nearest.getInventory().getItemInMainHand().getType() == Material.AIR) {
                // Peaceful mode
                peaceful = true;
                tameable = true;
                if (bossBar != null) {
                    nearest.hideBossBar(bossBar);
                }
            } else {
                super.onTick();
            }
        } else {
            super.onTick();
        }
    }

    private void tamedTick() {
        rideTicks++;
        Player owner = getOwner();
        if (owner == null) return;

        // Follow owner if not mounted
        if (hitboxEntity.getPassengers().isEmpty()) {
            double dist = hitboxEntity.getLocation().distanceSquared(owner.getLocation());
            if (dist > 400) {
                hitboxEntity.teleport(owner.getLocation().add(2, 0, 2));
            } else if (dist > 16 && hitboxEntity instanceof Mob mob) {
                mob.getPathfinder().moveTo(owner.getLocation(), 1.5);
            }
        }

        // Sparkle particles when ridden
        if (!hitboxEntity.getPassengers().isEmpty()) {
            if (hitboxEntity.getVelocity().lengthSquared() > 0.01) {
                hitboxEntity.getWorld().spawnParticle(Particle.END_ROD,
                        hitboxEntity.getLocation().add(0, 1, 0), 5, 0.5, 0.3, 0.5, 0.03);
            }
        }
    }

    @Override
    protected void combatTick() {
        Player target = findNearestPlayer(aggroRadius);
        if (target == null) return;

        if (hitboxEntity instanceof Mob mob) {
            mob.setTarget(target);
        }

        double dist = hitboxEntity.getLocation().distanceSquared(target.getLocation());

        // Dive attack
        if (dist > 64 && isAttackReady("dive", 100)) {
            useAttack("dive");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            Vector dir = target.getLocation().toVector()
                    .subtract(hitboxEntity.getLocation().toVector()).normalize().multiply(1.8);
            dir.setY(-0.5);
            hitboxEntity.setVelocity(dir);
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_HORSE_GALLOP, 2.0f, 1.5f);
        }

        // Horn strike melee
        if (dist < 12 && isAttackReady("horn", 30)) {
            useAttack("horn");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            target.damage(12, hitboxEntity);
            target.setVelocity(hitboxEntity.getLocation().getDirection().normalize().multiply(1.2));
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_HORSE_ANGRY, 1.5f, 1.2f);
        }

        // Wing buffet AoE
        if (dist < 25 && isAttackReady("wing_buffet", 80)) {
            useAttack("wing_buffet");
            playAnimation("attack", AnimationIterator.Type.PLAY_ONCE);
            for (Player p : findNearbyPlayers(5)) {
                Vector knockback = p.getLocation().toVector()
                        .subtract(hitboxEntity.getLocation().toVector()).normalize().multiply(1.5);
                knockback.setY(0.8);
                p.setVelocity(knockback);
                p.damage(8, hitboxEntity);
            }
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_ENDER_DRAGON_FLAP, 2.0f, 1.0f);
        }
    }

    @Override
    public void onInteract(Player player) {
        if (tamed) {
            if (!isOwner(player)) {
                player.sendMessage(Component.text("This is not your mount!", NamedTextColor.RED));
                return;
            }
            if (player.isSneaking()) {
                // Toggle sit
                return;
            }
            if (saddled) {
                if (hitboxEntity.getPassengers().isEmpty()) {
                    hitboxEntity.addPassenger(player);
                    playAnimation("walk_mounted", AnimationIterator.Type.LOOP);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 100, 0, true, false));
                }
            }
            return;
        }

        if (!peaceful || !tameable) return;

        // Feed golden apple to start taming
        if (!goldenAppleFed) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand.getType() == Material.GOLDEN_APPLE) {
                hand.setAmount(hand.getAmount() - 1);
                goldenAppleFed = true;
                hitboxEntity.getWorld().spawnParticle(Particle.HEART,
                        hitboxEntity.getLocation().add(0, 2, 0), 10, 0.5, 0.5, 0.5, 0);
                hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                        Sound.ENTITY_HORSE_EAT, 1.0f, 1.3f);
                player.sendMessage(Component.text("The Winged Unicorn accepts! Try mounting it.",
                        NamedTextColor.LIGHT_PURPLE));
            } else {
                player.sendMessage(Component.text("Offer a golden apple with bare hands and no armor.",
                        NamedTextColor.GRAY));
            }
            return;
        }

        // Mount to tame
        if (!player.isSprinting() && hitboxEntity.getPassengers().isEmpty()) {
            hitboxEntity.addPassenger(player);
            tame(player);
        }
    }

    private void tame(Player player) {
        tamed = true;
        ownerUUID = player.getUniqueId();
        saddled = true;
        inCombat = false;

        // Reduce HP to mount HP
        Objects.requireNonNull(hitboxEntity.getAttribute(
                org.bukkit.attribute.Attribute.MAX_HEALTH)).setBaseValue(80);
        hitboxEntity.setHealth(80);

        hitboxEntity.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "custom_mob_owner"),
                PersistentDataType.STRING, ownerUUID.toString());
        hitboxEntity.customName(Component.text(player.getName() + "'s Winged Unicorn",
                NamedTextColor.LIGHT_PURPLE));

        if (bossBar != null) {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                p.hideBossBar(bossBar);
            }
        }

        hitboxEntity.getWorld().spawnParticle(Particle.HEART,
                hitboxEntity.getLocation().add(0, 2, 0), 20, 1, 1, 1, 0);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.ENTITY_HORSE_AMBIENT, 1.5f, 1.5f);
        player.sendMessage(Component.text("You have tamed the Winged Unicorn!", NamedTextColor.GREEN));
    }

    /**
     * Rainbow Dash special ability - speed burst with rainbow particles.
     */
    public void triggerSpecialAbility(Player rider) {
        if (rideTicks - lastSpecialAbilityTick < SPECIAL_COOLDOWN) {
            long remaining = (SPECIAL_COOLDOWN - (rideTicks - lastSpecialAbilityTick)) / 20;
            rider.sendMessage(Component.text("Rainbow Dash on cooldown! " + remaining + "s",
                    NamedTextColor.RED));
            return;
        }
        lastSpecialAbilityTick = rideTicks;

        // Speed burst
        Vector dir = rider.getLocation().getDirection().normalize().multiply(2.5);
        dir.setY(0.3);
        hitboxEntity.setVelocity(dir);
        rider.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 60, 0, true, false));

        // Rainbow particle trail
        new BukkitRunnable() {
            int ticks = 0;
            final Particle.DustOptions[] rainbow = {
                    new Particle.DustOptions(Color.RED, 1.5f),
                    new Particle.DustOptions(Color.ORANGE, 1.5f),
                    new Particle.DustOptions(Color.YELLOW, 1.5f),
                    new Particle.DustOptions(Color.GREEN, 1.5f),
                    new Particle.DustOptions(Color.BLUE, 1.5f),
                    new Particle.DustOptions(Color.PURPLE, 1.5f)
            };

            @Override
            public void run() {
                if (ticks++ > 40 || hitboxEntity == null || !alive) {
                    cancel();
                    return;
                }
                Location loc = hitboxEntity.getLocation();
                for (Particle.DustOptions dust : rainbow) {
                    loc.getWorld().spawnParticle(Particle.DUST, loc.add(0, 1, 0),
                            3, 0.3, 0.3, 0.3, 0, dust);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);

        rider.sendMessage(Component.text("Rainbow Dash!", NamedTextColor.LIGHT_PURPLE)
                .decorate(TextDecoration.BOLD));
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 2.0f, 1.5f);
    }

    @Override
    protected BossBar.Color getBossBarColor() {
        return BossBar.Color.PURPLE;
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        if (tamed) return Collections.emptyList();
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(Material.DIAMOND, 4 + (int) (Math.random() * 4)));
        drops.add(new ItemStack(Material.GOLDEN_APPLE, 2));
        return drops;
    }

    private boolean isWearingArmor(Player player) {
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor != null && armor.getType() != Material.AIR) return true;
        }
        return false;
    }

    private boolean isOwner(Player player) {
        return ownerUUID != null && ownerUUID.equals(player.getUniqueId());
    }

    private Player getOwner() {
        if (ownerUUID == null) return null;
        return plugin.getServer().getPlayer(ownerUUID);
    }
}
