package com.jglims.plugin.legendary;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * LegendaryAltAbilities - all 63 alternate (crouch+right-click) ability implementations.
 * Rewritten with visually spectacular effects per tier.
 * Particle budgets: COMMON 10, RARE 25, EPIC 50, MYTHIC 100, ABYSSAL 200.
 */
final class LegendaryAltAbilities {

    private final LegendaryAbilityContext ctx;

    LegendaryAltAbilities(LegendaryAbilityContext ctx) {
        this.ctx = ctx;
    }

    // ================================================================
    //  COMMON TIER (20 weapons) - Simple shields, heals, dashes. 3-5s.
    //  Particle budget: 10 per burst
    // ================================================================

    // #1 AMETHYST SHURIKEN: Shadow Step - short-range blink behind target with brief invisibility
    void holdAmethystShuriken(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 10.0);
        if (target == null) {
            // No target: blink forward 6 blocks
            Location dest = p.getLocation().add(p.getLocation().getDirection().normalize().multiply(6));
            dest.setY(p.getWorld().getHighestBlockYAt(dest) + 1);
            spawnDustLine(p.getLocation(), dest, Color.fromRGB(160, 32, 240), 10, p.getWorld());
            p.teleport(dest);
            p.playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.8f);
            p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 40, 0));
            p.sendActionBar(Component.text("\u2694 Shadow Step!", NamedTextColor.DARK_PURPLE));
            return;
        }
        Location behind = target.getLocation().subtract(target.getLocation().getDirection().normalize().multiply(2));
        behind.setDirection(target.getLocation().toVector().subtract(behind.toVector()));
        spawnDustRing(p.getLocation(), 1.0, Color.fromRGB(160, 32, 240), 10, p.getWorld());
        p.teleport(behind);
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 40, 0));
        p.playSound(behind, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.8f);
        p.getWorld().spawnParticle(Particle.SMOKE, behind, 8, 0.3, 0.5, 0.3, 0.02);
        p.sendActionBar(Component.text("\u2694 Shadow Step! Behind target!", NamedTextColor.DARK_PURPLE));
    }

    // #2 GRAVESCEPTER: Death's Grasp - root nearby enemies for 3s + slow drain
    void holdGravescepter(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.ENTITY_WITHER_SKELETON_AMBIENT, 1.5f, 0.5f);
        List<LivingEntity> enemies = ctx.getNearbyEnemies(c, 6.0, p);
        for (LivingEntity e : enemies) {
            e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 127));
            e.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 0));
        }
        spawnDustRing(c, 6.0, Color.fromRGB(64, 64, 64), 10, p.getWorld());
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 60 || !p.isOnline()) { cancel(); return; }
                if (tick % 10 == 0) {
                    for (LivingEntity e : enemies) {
                        if (e.isDead()) continue;
                        p.getWorld().spawnParticle(Particle.DUST, e.getLocation().add(0, 0.2, 0), 3,
                                0.3, 0.05, 0.3, 0, new Particle.DustOptions(Color.fromRGB(50, 50, 50), 1.5f));
                    }
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2620 Death's Grasp! Enemies rooted!", NamedTextColor.DARK_GRAY));
    }

    // #3 LYCANBANE: Hunter's Sense - 5s night vision + glow nearby enemies + speed boost
    void holdLycanbane(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 100, 0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1));
        p.playSound(p.getLocation(), Sound.ENTITY_WOLF_AMBIENT, 1.5f, 1.2f);
        List<LivingEntity> enemies = ctx.getNearbyEnemies(p.getLocation(), 20.0, p);
        for (LivingEntity e : enemies) {
//             e.setGlowing(true); // REMOVED: no glowing on mobs
            Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> e.setGlowing(false), 100L);
        }
        spawnDustRing(p.getLocation(), 2.0, Color.fromRGB(192, 192, 192), 10, p.getWorld());
        p.sendActionBar(Component.text("\u26C5 Hunter's Sense! Enemies revealed!", NamedTextColor.GRAY));
    }

    // #4 GLOOMSTEEL KATANA: Shadow Stance - 4s 25% dodge chance + counter damage
    void holdGloomsteelKatana(Player p) {
        UUID uid = p.getUniqueId();
        ctx.shadowStanceActive.put(uid, true);
        ctx.shadowStanceExpiry.put(uid, System.currentTimeMillis() + 4000L);
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 0.5f);
        p.getWorld().spawnParticle(Particle.DUST, p.getLocation().add(0, 1, 0), 10,
                0.5, 0.8, 0.5, 0, new Particle.DustOptions(Color.fromRGB(40, 40, 60), 1.2f));
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 80 || !p.isOnline()) {
                    ctx.shadowStanceActive.put(uid, false);
                    cancel(); return;
                }
                if (tick % 4 == 0) {
                    double angle = tick * 0.3;
                    Location trail = p.getLocation().add(Math.cos(angle) * 0.8, 1.0, Math.sin(angle) * 0.8);
                    p.getWorld().spawnParticle(Particle.DUST, trail, 2, 0.05, 0.1, 0.05, 0,
                            new Particle.DustOptions(Color.fromRGB(30, 30, 50), 0.8f));
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2694 Shadow Stance! 25% dodge for 4s", NamedTextColor.DARK_GRAY));
    }

    // #5 VIRIDIAN CLEAVER: Overgrowth - heal 3 hearts + regen I 5s + small vine particle ring
    void holdViridianCleaver(Player p) {
        double max = p.getAttribute(Attribute.MAX_HEALTH).getValue();
        p.setHealth(Math.min(max, p.getHealth() + 6.0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 0));
        p.playSound(p.getLocation(), Sound.BLOCK_GRASS_BREAK, 1.5f, 1.0f);
        spawnDustRing(p.getLocation().add(0, 0.3, 0), 2.0, Color.fromRGB(34, 139, 34), 10, p.getWorld());
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 40 || !p.isOnline()) { cancel(); return; }
                if (tick % 10 == 0) {
                    p.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, p.getLocation().add(0, 1, 0), 3, 0.5, 0.5, 0.5, 0);
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2618 Overgrowth! Healed + Regen", NamedTextColor.GREEN));
    }

    // #6 CRESCENT EDGE: Crescent Guard - 3s parry window, reflects next melee hit
    void holdCrescentEdge(Player p) {
        ctx.crescentParryActive.put(p.getUniqueId(), true);
        p.playSound(p.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.5f, 1.5f);
        spawnDustArc(p, 1.5, Color.fromRGB(220, 220, 240), 10);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 60 || !ctx.crescentParryActive.getOrDefault(p.getUniqueId(), false)) {
                    ctx.crescentParryActive.put(p.getUniqueId(), false);
                    cancel(); return;
                }
                if (tick % 5 == 0) {
                    Location front = p.getEyeLocation().add(p.getLocation().getDirection().multiply(0.8));
                    p.getWorld().spawnParticle(Particle.DUST, front, 2, 0.2, 0.3, 0.2, 0,
                            new Particle.DustOptions(Color.fromRGB(200, 200, 255), 1.0f));
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u263D Crescent Guard! Parry next hit!", NamedTextColor.WHITE));
    }

    // #7 GRAVECLEAVER: Undying Rage - 5s: cannot die (min 1 HP) + Strength I
    void holdGravecleaver(Player p) {
        UUID uid = p.getUniqueId();
        ctx.undyingRageActive.put(uid, true);
        ctx.undyingRageExpiry.put(uid, System.currentTimeMillis() + 5000L);
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100, 0));
        p.playSound(p.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 1.5f, 0.6f);
        p.getWorld().spawnParticle(Particle.DUST, p.getLocation().add(0, 1, 0), 10,
                0.5, 0.8, 0.5, 0, new Particle.DustOptions(Color.fromRGB(180, 0, 0), 1.5f));
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 100 || !p.isOnline()) {
                    ctx.undyingRageActive.put(uid, false);
                    cancel(); return;
                }
                if (tick % 10 == 0) {
                    p.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, p.getLocation().add(0, 1.5, 0), 2, 0.3, 0.3, 0.3, 0);
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2620 Undying Rage! Cannot die for 5s!", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
    }

    // #8 AMETHYST GREATBLADE: Gem Resonance - pulse that gives Resistance I 5s + heals 2 hearts
    void holdAmethystGreatblade(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 0));
        double max = p.getAttribute(Attribute.MAX_HEALTH).getValue();
        p.setHealth(Math.min(max, p.getHealth() + 4.0));
        p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 2.0f, 1.0f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 60 || !p.isOnline()) { cancel(); return; }
                if (tick % 6 == 0) {
                    double radius = 1.0 + (tick / 60.0) * 2.0;
                    spawnDustRing(p.getLocation().add(0, 0.5, 0), radius, Color.fromRGB(180, 100, 255), 8, p.getWorld());
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2726 Gem Resonance! Shield + Heal", NamedTextColor.LIGHT_PURPLE));
    }

    // #9 FLAMBERGE: Ember Shield - 5s: melee attackers get set on fire + take 4 dmg
    void holdFlamberge(Player p) {
        UUID uid = p.getUniqueId();
        ctx.emberShieldActive.put(uid, true);
        ctx.emberShieldExpiry.put(uid, System.currentTimeMillis() + 5000L);
        p.playSound(p.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.5f, 1.0f);
        p.getWorld().spawnParticle(Particle.FLAME, p.getLocation().add(0, 1, 0), 10, 0.5, 0.8, 0.5, 0.02);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 100 || !p.isOnline()) {
                    ctx.emberShieldActive.put(uid, false);
                    cancel(); return;
                }
                if (tick % 5 == 0) {
                    double angle = tick * 0.4;
                    Location flame = p.getLocation().add(Math.cos(angle) * 1.0, 1.0, Math.sin(angle) * 1.0);
                    p.getWorld().spawnParticle(Particle.FLAME, flame, 1, 0, 0.1, 0, 0.01);
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2600 Ember Shield! Attackers burn!", NamedTextColor.GOLD));
    }

    // #10 CRYSTAL FROSTBLADE: Permafrost - freeze ground 4-block radius, slowing enemies + Frost Walker
    void holdCrystalFrostblade(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.BLOCK_GLASS_BREAK, 1.5f, 1.5f);
        List<LivingEntity> enemies = ctx.getNearbyEnemies(c, 4.0, p);
        for (LivingEntity e : enemies) {
            e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 2));
            e.setFreezeTicks(100);
        }
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 0));
        spawnDustRing(c, 4.0, Color.fromRGB(150, 220, 255), 10, p.getWorld());
        p.getWorld().spawnParticle(Particle.SNOWFLAKE, c.add(0, 0.5, 0), 10, 2, 0.3, 2, 0.01);
        p.sendActionBar(Component.text("\u2744 Permafrost! Enemies frozen!", NamedTextColor.AQUA));
    }

    // #11 DEMONSLAYER: Purifying Aura - 5s aura that heals player and damages undead nearby
    void holdDemonslayer(Player p) {
        p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.5f);
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 100 || !p.isOnline()) { cancel(); return; }
                if (tick % 20 == 0) {
                    for (LivingEntity e : ctx.getNearbyEnemies(p.getLocation(), 5.0, p)) {
                        if (isUndead(e)) {
                            ctx.dealDamage(p, e, 6.0);
                            e.getWorld().spawnParticle(Particle.DUST, e.getLocation().add(0, 1, 0), 3,
                                    0.2, 0.3, 0.2, 0, new Particle.DustOptions(Color.fromRGB(255, 255, 200), 1.0f));
                        }
                    }
                }
                if (tick % 8 == 0) {
                    spawnDustRing(p.getLocation().add(0, 0.3, 0), 2.5, Color.fromRGB(255, 255, 180), 6, p.getWorld());
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2726 Purifying Aura! Undead beware!", NamedTextColor.YELLOW));
    }

    // #12 VENGEANCE: Grudge Mark - mark target for 20% bonus damage for 10s
    void holdVengeance(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 15.0);
        if (target == null) { p.sendActionBar(Component.text("No target in sight!", NamedTextColor.RED)); return; }
        UUID uid = p.getUniqueId();
        ctx.grudgeTargets.put(uid, target.getUniqueId());
        ctx.grudgeExpiry.put(uid, System.currentTimeMillis() + 10000L);
        p.playSound(p.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.0f, 1.5f);
//         target.setGlowing(true); // REMOVED: no glowing on mobs
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> target.setGlowing(false), 200L);
        p.getWorld().spawnParticle(Particle.DUST, target.getLocation().add(0, 2.2, 0), 10,
                0.3, 0.1, 0.3, 0, new Particle.DustOptions(Color.fromRGB(200, 0, 0), 1.5f));
        p.sendActionBar(Component.text("\u2620 Grudge Mark! +20% damage to target!", NamedTextColor.RED));
    }

    // #13 OCULUS: Third Eye - 5s: see invisible, glow all enemies 15 blocks, slow time (slow enemies)
    void holdOculus(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 100, 0));
        p.playSound(p.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_AMBIENT, 1.0f, 2.0f);
        List<LivingEntity> enemies = ctx.getNearbyEnemies(p.getLocation(), 15.0, p);
        for (LivingEntity e : enemies) {
//             e.setGlowing(true); // REMOVED: no glowing on mobs
            e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1));
            Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> e.setGlowing(false), 100L);
        }
        p.getWorld().spawnParticle(Particle.DUST, p.getLocation().add(0, 2.2, 0), 8,
                0.2, 0.1, 0.2, 0, new Particle.DustOptions(Color.fromRGB(0, 255, 255), 1.8f));
        p.sendActionBar(Component.text("\u25C9 Third Eye! All enemies revealed!", NamedTextColor.AQUA));
    }

    // #14 ANCIENT GREATSLAB: Stone Skin - Resistance II 5s + Slowness I (tank mode)
    void holdAncientGreatslab(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 1));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 0));
        p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 0.5f);
        p.getWorld().spawnParticle(Particle.BLOCK, p.getLocation(), 10, 0.5, 0.8, 0.5, 0.1,
                Material.STONE.createBlockData());
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 100 || !p.isOnline()) { cancel(); return; }
                if (tick % 15 == 0) {
                    p.getWorld().spawnParticle(Particle.DUST, p.getLocation().add(0, 0.5, 0), 4,
                            0.4, 0.1, 0.4, 0, new Particle.DustOptions(Color.fromRGB(128, 128, 128), 1.5f));
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2588 Stone Skin! Resistance II active!", NamedTextColor.GRAY));
    }

    // #15 NEPTUNE'S FANG: Maelstrom - small water vortex that pulls enemies in 5 blocks
    void holdNeptunesFang(Player p) {
        Location center = p.getLocation().add(p.getLocation().getDirection().multiply(4));
        p.playSound(center, Sound.ENTITY_GENERIC_SPLASH, 1.5f, 0.8f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 80 || !p.isOnline()) { cancel(); return; }
                if (tick % 2 == 0) {
                    double angle = tick * 0.5;
                    double r = 2.0 + Math.sin(tick * 0.1) * 0.5;
                    Location swirl = center.clone().add(Math.cos(angle) * r, 0.3, Math.sin(angle) * r);
                    p.getWorld().spawnParticle(Particle.DUST, swirl, 2, 0.1, 0.1, 0.1, 0,
                            new Particle.DustOptions(Color.fromRGB(0, 100, 200), 1.2f));
                }
                if (tick % 20 == 0) {
                    for (LivingEntity e : ctx.getNearbyEnemies(center, 5.0, p)) {
                        Vector pull = center.toVector().subtract(e.getLocation().toVector()).normalize().multiply(0.6);
                        e.setVelocity(e.getVelocity().add(pull));
                        e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 1));
                    }
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2248 Maelstrom! Enemies pulled in!", NamedTextColor.DARK_AQUA));
    }

    // #16 TIDECALLER: Depth Ward - 4s water shield absorbing 30% damage + bubble visuals
    void holdTidecaller(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 80, 1));
        p.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 200, 0));
        p.playSound(p.getLocation(), Sound.AMBIENT_UNDERWATER_ENTER, 1.5f, 1.0f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 80 || !p.isOnline()) { cancel(); return; }
                if (tick % 4 == 0) {
                    double angle = tick * 0.4;
                    Location bubble = p.getLocation().add(Math.cos(angle) * 1.2, 0.5 + Math.random(), Math.sin(angle) * 1.2);
                    p.getWorld().spawnParticle(Particle.BUBBLE_POP, bubble, 2, 0.1, 0.1, 0.1, 0);
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2248 Depth Ward! Water shield active!", NamedTextColor.AQUA));
    }

    // #17 STORMFORK: Thunder Shield - 5s: lightning strikes melee attackers
    void holdStormfork(Player p) {
        UUID uid = p.getUniqueId();
        ctx.thunderShieldActive.put(uid, true);
        ctx.thunderShieldExpiry.put(uid, System.currentTimeMillis() + 5000L);
        p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8f, 1.5f);
        p.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, p.getLocation().add(0, 1.5, 0), 10, 0.5, 0.5, 0.5, 0.1);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 100 || !p.isOnline()) {
                    ctx.thunderShieldActive.put(uid, false);
                    cancel(); return;
                }
                if (tick % 8 == 0) {
                    Location spark = p.getLocation().add(
                            Math.random() * 1.6 - 0.8, 1.0 + Math.random(), Math.random() * 1.6 - 0.8);
                    p.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, spark, 1, 0, 0, 0, 0);
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u26A1 Thunder Shield! Attackers get shocked!", NamedTextColor.YELLOW));
    }

    // #18 JADE REAPER: Emerald Harvest - lifesteal aura 4s, heal 1 heart per enemy nearby each sec
    void holdJadeReaper(Player p) {
        p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 1.5f, 0.8f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 80 || !p.isOnline()) { cancel(); return; }
                if (tick % 20 == 0) {
                    List<LivingEntity> enemies = ctx.getNearbyEnemies(p.getLocation(), 5.0, p);
                    double heal = enemies.size() * 2.0;
                    double max = p.getAttribute(Attribute.MAX_HEALTH).getValue();
                    p.setHealth(Math.min(max, p.getHealth() + heal));
                    for (LivingEntity e : enemies) {
                        spawnDustLine(e.getLocation().add(0, 1, 0), p.getLocation().add(0, 1, 0),
                                Color.fromRGB(0, 200, 80), 4, p.getWorld());
                    }
                }
                if (tick % 8 == 0) {
                    spawnDustRing(p.getLocation().add(0, 0.2, 0), 2.5, Color.fromRGB(0, 180, 60), 6, p.getWorld());
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2618 Emerald Harvest! Life drain active!", NamedTextColor.GREEN));
    }

    // #19 VINDICATOR: Rally Cry - give Speed I + Resistance I to all guild allies within 10 blocks for 5s
    void holdVindicator(Player p) {
        p.playSound(p.getLocation(), Sound.EVENT_RAID_HORN, 1.5f, 1.2f);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 0));
        spawnDustRing(p.getLocation(), 3.0, Color.fromRGB(200, 200, 50), 10, p.getWorld());
        for (Entity e : p.getWorld().getNearbyEntities(p.getLocation(), 10, 10, 10)) {
            if (e instanceof Player ally && ally != p &&
                    ctx.guildManager.areInSameGuild(p.getUniqueId(), ally.getUniqueId())) {
                ally.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 0));
                ally.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 0));
                ally.sendActionBar(Component.text("Rally Cry! Buffed!", NamedTextColor.GOLD));
            }
        }
        p.sendActionBar(Component.text("\u2694 Rally Cry! Allies buffed!", NamedTextColor.GOLD));
    }

    // #20 SPIDER FANG: Wall Crawler - 5s wall climb + Night Vision
    void holdSpiderFang(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 100, 0));
        p.playSound(p.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 1.5f, 1.0f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 100 || !p.isOnline()) { cancel(); return; }
                Location feet = p.getLocation();
                boolean nearWall = false;
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dz == 0) continue;
                        Block adjacent = feet.clone().add(dx, 0, dz).getBlock();
                        if (adjacent.getType().isSolid()) { nearWall = true; break; }
                    }
                    if (nearWall) break;
                }
                if (nearWall && p.isSneaking()) {
                    p.setVelocity(new Vector(0, 0.08, 0));
                    if (tick % 5 == 0) {
                        p.getWorld().spawnParticle(Particle.DUST, feet.add(0, 0.5, 0), 2,
                                0.2, 0, 0.2, 0, new Particle.DustOptions(Color.fromRGB(80, 80, 80), 0.6f));
                    }
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2620 Wall Crawler! Sneak near walls to climb!", NamedTextColor.DARK_GREEN));
    }

    // ================================================================
    //  RARE TIER (8 weapons) - Buff zones, counters, mobility. 5-8s.
    //  Particle budget: 25 per burst
    // ================================================================

    // #21 OCEAN'S RAGE: Riptide Surge - launch forward through water trail, damaging enemies in path
    void holdOceansRage(Player p) {
        Vector dir = p.getLocation().getDirection().normalize().multiply(2.5);
        p.setVelocity(dir);
        p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_3, 2.0f, 1.0f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick > 25) { cancel(); return; }
                Location loc = p.getLocation();
                p.getWorld().spawnParticle(Particle.DUST, loc, 8, 0.4, 0.6, 0.4, 0,
                        new Particle.DustOptions(Color.fromRGB(0, 120, 255), 1.5f));
                p.getWorld().spawnParticle(Particle.SPLASH, loc, 5, 0.3, 0.3, 0.3, 0.05);
                for (LivingEntity e : ctx.getNearbyEnemies(loc, 2.0, p)) {
                    ctx.dealDamage(p, e, 8.0);
                    e.setVelocity(dir.clone().normalize().multiply(1.5).setY(0.5));
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2248 RIPTIDE SURGE!", NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
    }

    // #22 AQUATIC SACRED BLADE: Depth Pressure - 6s zone: enemies get Slowness + Mining Fatigue + suffocate visual
    void holdAquaticSacredBlade(Player p) {
        Location center = p.getLocation().clone();
        p.playSound(center, Sound.AMBIENT_UNDERWATER_ENTER, 2.0f, 0.5f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 120 || !p.isOnline()) { cancel(); return; }
                if (tick % 3 == 0) {
                    double angle = tick * 0.2;
                    for (int i = 0; i < 3; i++) {
                        double a = angle + i * (Math.PI * 2 / 3);
                        Location ring = center.clone().add(Math.cos(a) * 5, 0.5, Math.sin(a) * 5);
                        p.getWorld().spawnParticle(Particle.DUST, ring, 3, 0.2, 0.3, 0.2, 0,
                                new Particle.DustOptions(Color.fromRGB(0, 50, 120), 1.5f));
                    }
                }
                if (tick % 20 == 0) {
                    for (LivingEntity e : ctx.getNearbyEnemies(center, 8.0, p)) {
                        e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 2));
                        e.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 30, 1));
                        p.getWorld().spawnParticle(Particle.BUBBLE_POP, e.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0);
                    }
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2248 Depth Pressure! Enemies crushed!", NamedTextColor.DARK_AQUA));
    }

    // #23 ROYAL CHAKRAM: Spinning Shield - 5s orbiting chakram deflecting projectiles + Resistance I
    void holdRoyalChakram(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 0));
        p.playSound(p.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.5f, 1.2f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick > 100) { cancel(); return; }
                for (int i = 0; i < 3; i++) {
                    double angle = tick * 0.25 + i * (Math.PI * 2 / 3);
                    Location orb = p.getLocation().add(Math.cos(angle) * 1.8, 1.0, Math.sin(angle) * 1.8);
                    p.getWorld().spawnParticle(Particle.DUST, orb, 3, 0.05, 0.05, 0.05, 0,
                            new Particle.DustOptions(Color.fromRGB(255, 215, 0), 1.2f));
                    p.getWorld().spawnParticle(Particle.CRIT, orb, 1, 0, 0, 0, 0);
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u25C6 Spinning Shield! 5s defense!", NamedTextColor.GOLD));
    }

    // #24 ACIDIC CLEAVER: Corrosive Aura - 8s acid cloud, enemies take 2 dmg/s + Weakness
    void holdAcidicCleaver(Player p) {
        p.playSound(p.getLocation(), Sound.ENTITY_LLAMA_SPIT, 1.0f, 0.3f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 160 || !p.isOnline()) { cancel(); return; }
                if (tick % 4 == 0) {
                    double angle = tick * 0.3;
                    for (int i = 0; i < 2; i++) {
                        double a = angle + i * Math.PI;
                        Location cloud = p.getLocation().add(Math.cos(a) * 3, 0.5 + Math.random() * 1.5, Math.sin(a) * 3);
                        p.getWorld().spawnParticle(Particle.DUST, cloud, 4, 0.5, 0.3, 0.5, 0,
                                new Particle.DustOptions(Color.fromRGB(80, 200, 20), 1.5f));
                    }
                }
                if (tick % 20 == 0) {
                    for (LivingEntity e : ctx.getNearbyEnemies(p.getLocation(), 6.0, p)) {
                        ctx.dealDamage(p, e, 2.0);
                        e.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 30, 0));
                        p.getWorld().spawnParticle(Particle.ITEM_SLIME, e.getLocation().add(0, 1, 0), 3, 0.2, 0.2, 0.2, 0);
                    }
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2622 Corrosive Aura! Acid cloud active!", NamedTextColor.GREEN));
    }

    // #25 MURAMASA: Bloodlust - 8s: each kill stacks +2 damage, max 5 stacks
    void holdMuramasa(Player p) {
        UUID uid = p.getUniqueId();
        ctx.bloodlustStacks.put(uid, 0);
        ctx.bloodlustExpiry.put(uid, System.currentTimeMillis() + 8000L);
        p.playSound(p.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1.5f, 1.5f);
        p.getWorld().spawnParticle(Particle.DUST, p.getLocation().add(0, 1, 0), 15,
                0.4, 0.6, 0.4, 0, new Particle.DustOptions(Color.fromRGB(180, 0, 0), 1.2f));
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                long now = System.currentTimeMillis();
                if (tick >= 160 || !p.isOnline() || ctx.bloodlustExpiry.getOrDefault(uid, 0L) < now) {
                    ctx.bloodlustStacks.remove(uid);
                    cancel(); return;
                }
                if (tick % 10 == 0) {
                    int stacks = ctx.bloodlustStacks.getOrDefault(uid, 0);
                    float red = Math.min(1.0f, 0.3f + stacks * 0.14f);
                    p.getWorld().spawnParticle(Particle.DUST, p.getLocation().add(0, 1.5, 0),
                            2 + stacks, 0.3, 0.3, 0.3, 0,
                            new Particle.DustOptions(Color.fromRGB((int)(red * 255), 0, 0), 0.8f + stacks * 0.2f));
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2620 Bloodlust! Kill to stack damage!", NamedTextColor.RED));
    }

    // #26 WINDREAPER: Cyclone - 6s tornado that pulls enemies + damages them
    void holdWindreaper(Player p) {
        Location center = p.getLocation().clone();
        p.playSound(center, Sound.ENTITY_BREEZE_WIND_BURST, 2.0f, 0.5f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 120 || !p.isOnline()) { cancel(); return; }
                // Spiral particles
                for (int i = 0; i < 8; i++) {
                    double angle = tick * 0.3 + i * (Math.PI / 4);
                    double r = 2.5 + Math.sin(tick * 0.08) * 0.8;
                    double y = (tick % 30) * 0.15;
                    Location swirl = center.clone().add(Math.cos(angle) * r, 0.3 + y, Math.sin(angle) * r);
                    p.getWorld().spawnParticle(Particle.DUST, swirl, 2, 0.1, 0.1, 0.1, 0,
                            new Particle.DustOptions(Color.fromRGB(200, 200, 200), 1.0f));
                }
                if (tick % 20 == 0) {
                    for (LivingEntity e : ctx.getNearbyEnemies(center, 5.0, p)) {
                        ctx.dealDamage(p, e, 4.0);
                        Vector pull = center.toVector().subtract(e.getLocation().toVector()).normalize().multiply(0.6);
                        e.setVelocity(e.getVelocity().add(pull.setY(0.3)));
                    }
                    p.getWorld().playSound(center, Sound.ENTITY_BREEZE_SHOOT, 0.8f, 1.0f);
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2601 Cyclone! Enemies pulled in!", NamedTextColor.WHITE));
    }

    // #27 MOONLIGHT: Eclipse - 6s darkness dome: Blindness + Weakness on enemies, Night Vision for self
    void holdMoonlight(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 120, 0));
        Location center = p.getLocation().clone();
        p.playSound(center, Sound.AMBIENT_CAVE, 2.0f, 0.3f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 120 || !p.isOnline()) { cancel(); return; }
                // Dark dome ring
                if (tick % 3 == 0) {
                    for (int i = 0; i < 6; i++) {
                        double angle = tick * 0.1 + i * (Math.PI / 3);
                        Location ring = center.clone().add(Math.cos(angle) * 8, 0.5, Math.sin(angle) * 8);
                        p.getWorld().spawnParticle(Particle.DUST, ring, 2, 0.2, 0.3, 0.2, 0,
                                new Particle.DustOptions(Color.fromRGB(20, 0, 40), 2.0f));
                    }
                }
                if (tick % 20 == 0) {
                    for (LivingEntity e : ctx.getNearbyEnemies(center, 10.0, p)) {
                        e.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 30, 0));
                        e.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 30, 1));
                    }
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u263D ECLIPSE! Darkness descends!", NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
    }

    // #28 TALONBRAND: Predator's Mark - mark a target for 8s, +30% damage, reveals their location
    void holdTalonbrand(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 15.0);
        if (target == null) { p.sendActionBar(Component.text("No prey in sight!", NamedTextColor.RED)); return; }
        UUID uid = p.getUniqueId();
        ctx.predatorMarkTarget.put(uid, target.getUniqueId());
        ctx.predatorMarkExpiry.put(uid, System.currentTimeMillis() + 8000L);
        p.playSound(p.getLocation(), Sound.ENTITY_PHANTOM_BITE, 1.5f, 1.0f);
//         target.setGlowing(true); // REMOVED: no glowing on mobs
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 160, 1));
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 160 || target.isDead() || !p.isOnline()) {
                    target.setGlowing(false);
                    cancel(); return;
                }
                if (tick % 10 == 0) {
                    p.getWorld().spawnParticle(Particle.DUST, target.getLocation().add(0, 2.5, 0), 5,
                            0.1, 0.1, 0.1, 0, new Particle.DustOptions(Color.fromRGB(255, 50, 0), 1.5f));
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2694 Predator's Mark! +30% to marked prey!", NamedTextColor.RED));
    }

    // ================================================================
    //  EPIC TIER (7 weapons) - Transformations, terrain control. 6-10s.
    //  Particle budget: 50 per burst
    // ================================================================

    // #29 BERSERKER'S GREATAXE: Blood Rage - 8s: +Strength III, +Speed II, lifesteal on hit, red aura
    void holdBerserkersGreataxe(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 160, 2));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 160, 1));
        p.playSound(p.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 2.0f, 0.6f);
        p.getWorld().spawnParticle(Particle.DUST, p.getLocation().add(0, 1.5, 0), 30,
                1.0, 1.5, 1.0, 0, new Particle.DustOptions(Color.fromRGB(200, 0, 0), 2.0f));
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 160 || !p.isOnline()) { cancel(); return; }
                // Pulsing red aura
                if (tick % 4 == 0) {
                    double pulse = 1.0 + Math.sin(tick * 0.15) * 0.3;
                    for (int i = 0; i < 4; i++) {
                        double angle = tick * 0.2 + i * (Math.PI / 2);
                        Location flame = p.getLocation().add(Math.cos(angle) * pulse, 0.8 + Math.random() * 1.2, Math.sin(angle) * pulse);
                        p.getWorld().spawnParticle(Particle.DUST, flame, 2, 0.05, 0.1, 0.05, 0,
                                new Particle.DustOptions(Color.fromRGB(180, 0, 0), 1.0f));
                    }
                }
                if (tick % 10 == 0) {
                    p.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, p.getLocation().add(0, 2, 0), 3, 0.3, 0.3, 0.3, 0);
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2620 BLOOD RAGE! Unstoppable fury!", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
    }

    // #30 BLACK IRON GREATSWORD: Iron Fortress - 8s: Absorption IV + Resistance II + knockback resistance
    void holdBlackIronGreatsword(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 160, 3));
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 160, 1));
        p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 2.0f, 0.8f);
        // Iron block barrier visual
        Location c = p.getLocation();
        for (int i = 0; i < 8; i++) {
            double angle = i * (Math.PI / 4);
            Location pillar = c.clone().add(Math.cos(angle) * 2.5, 0, Math.sin(angle) * 2.5);
            for (int h = 0; h < 3; h++) {
                p.getWorld().spawnParticle(Particle.DUST, pillar.clone().add(0, h * 0.8, 0), 4,
                        0.1, 0.1, 0.1, 0, new Particle.DustOptions(Color.fromRGB(60, 60, 70), 2.0f));
            }
        }
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 160 || !p.isOnline()) { cancel(); return; }
                if (tick % 10 == 0) {
                    spawnDustRing(p.getLocation().add(0, 0.3, 0), 2.5, Color.fromRGB(60, 60, 70), 12, p.getWorld());
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2694 IRON FORTRESS! Maximum defense!", NamedTextColor.DARK_GRAY).decorate(TextDecoration.BOLD));
    }

    // #31 SOLSTICE: Daybreak - cleanse all debuffs + Regen IV 6s + speed burst + sunbeam visual
    void holdSolstice(Player p) {
        // Remove negative effects
        for (PotionEffect effect : p.getActivePotionEffects()) {
            PotionEffectType type = effect.getType();
            if (isNegativeEffect(type)) {
                p.removePotionEffect(type);
            }
        }
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 120, 3));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1));
        p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.5f);
        // Sunbeam from above
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 40 || !p.isOnline()) { cancel(); return; }
                Location beam = p.getLocation().add(0, 8 - tick * 0.2, 0);
                p.getWorld().spawnParticle(Particle.DUST, beam, 8, 0.3, 0.1, 0.3, 0,
                        new Particle.DustOptions(Color.fromRGB(255, 230, 100), 2.0f));
                p.getWorld().spawnParticle(Particle.END_ROD, beam, 3, 0.2, 0.5, 0.2, 0.02);
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        spawnDustRing(p.getLocation(), 3.0, Color.fromRGB(255, 200, 50), 25, p.getWorld());
        p.sendActionBar(Component.text("\u2600 DAYBREAK! Cleansed and renewed!", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // #32 GRAND CLAYMORE: Colossus Stance - 8s: massive size feel, Resistance III + Strength II + Slowness II
    void holdGrandClaymore(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 160, 2));
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 160, 1));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 160, 1));
        p.playSound(p.getLocation(), Sound.ENTITY_IRON_GOLEM_DAMAGE, 2.0f, 0.5f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 160 || !p.isOnline()) { cancel(); return; }
                // Giant aura effect
                if (tick % 5 == 0) {
                    for (int i = 0; i < 6; i++) {
                        double angle = i * (Math.PI / 3) + tick * 0.05;
                        Location aura = p.getLocation().add(Math.cos(angle) * 2.0, 0.2 + (i % 3) * 0.8, Math.sin(angle) * 2.0);
                        p.getWorld().spawnParticle(Particle.DUST, aura, 3, 0.1, 0.1, 0.1, 0,
                                new Particle.DustOptions(Color.fromRGB(150, 150, 170), 2.5f));
                    }
                }
                // Ground crack effect
                if (tick % 20 == 0) {
                    p.getWorld().spawnParticle(Particle.BLOCK, p.getLocation(), 10, 1.5, 0.1, 1.5, 0,
                            Material.STONE.createBlockData());
                    p.playSound(p.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 0.5f, 0.5f);
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2588 COLOSSUS STANCE! Immovable titan!", NamedTextColor.GRAY).decorate(TextDecoration.BOLD));
    }

    // #33 CALAMITY BLADE: Doomsday - 8s aura: enemies within 8 blocks get Wither + Weakness, ominous visuals
    void holdCalamityBlade(Player p) {
        p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.8f, 0.3f);
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 160, 1));
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 160 || !p.isOnline()) { cancel(); return; }
                // Ominous dark ring
                if (tick % 3 == 0) {
                    for (int i = 0; i < 8; i++) {
                        double angle = tick * 0.15 + i * (Math.PI / 4);
                        double r = 4.0 + Math.sin(tick * 0.05) * 1.5;
                        Location doom = p.getLocation().add(Math.cos(angle) * r, 0.5 + Math.sin(tick * 0.1 + i) * 1.0, Math.sin(angle) * r);
                        p.getWorld().spawnParticle(Particle.DUST, doom, 2, 0.1, 0.1, 0.1, 0,
                                new Particle.DustOptions(Color.fromRGB(80, 0, 0), 1.5f));
                    }
                }
                if (tick % 20 == 0) {
                    for (LivingEntity e : ctx.getNearbyEnemies(p.getLocation(), 8.0, p)) {
                        e.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 30, 1));
                        e.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 30, 1));
                    }
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2620 DOOMSDAY! Calamity surrounds you!", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
    }

    // #34 EMERALD GREATCLEAVER: Gem Barrier - 3 charges absorbing hits, lasts 10s
    void holdEmeraldGreatcleaver(Player p) {
        UUID uid = p.getUniqueId();
        ctx.gemBarrierCharges.put(uid, 3);
        ctx.gemBarrierExpiry.put(uid, System.currentTimeMillis() + 10000L);
        p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 2.0f, 0.8f);
        // Emerald gems orbiting
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                int charges = ctx.gemBarrierCharges.getOrDefault(uid, 0);
                if (tick >= 200 || !p.isOnline() || charges <= 0) {
                    ctx.gemBarrierCharges.remove(uid);
                    cancel(); return;
                }
                for (int i = 0; i < charges; i++) {
                    double angle = tick * 0.15 + i * (Math.PI * 2.0 / charges);
                    Location gem = p.getLocation().add(Math.cos(angle) * 1.5, 1.2, Math.sin(angle) * 1.5);
                    p.getWorld().spawnParticle(Particle.DUST, gem, 3, 0.05, 0.05, 0.05, 0,
                            new Particle.DustOptions(Color.fromRGB(0, 200, 50), 1.5f));
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 2L);
        p.sendActionBar(Component.text("\u2726 Gem Barrier! 3 charges absorbing hits!", NamedTextColor.GREEN).decorate(TextDecoration.BOLD));
    }

    // #35 DEMON'S BLOOD BLADE: Demonic Form - 10s: transform with Strength II + Fire Resist + thorns aura
    void holdDemonsBloodBlade(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 1));
        p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 200, 0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 0));
        p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.6f, 2.0f);
        p.getWorld().spawnParticle(Particle.DUST, p.getLocation().add(0, 1.5, 0), 40,
                1.0, 1.5, 1.0, 0, new Particle.DustOptions(Color.fromRGB(120, 0, 0), 2.0f));
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 200 || !p.isOnline()) { cancel(); return; }
                // Demonic flames + dark particles
                if (tick % 3 == 0) {
                    double angle = tick * 0.25;
                    Location horn1 = p.getLocation().add(Math.cos(angle) * 0.4, 2.2, Math.sin(angle) * 0.4);
                    Location horn2 = p.getLocation().add(Math.cos(angle + Math.PI) * 0.4, 2.2, Math.sin(angle + Math.PI) * 0.4);
                    p.getWorld().spawnParticle(Particle.DUST, horn1, 2, 0.02, 0.05, 0.02, 0,
                            new Particle.DustOptions(Color.fromRGB(200, 30, 0), 0.8f));
                    p.getWorld().spawnParticle(Particle.DUST, horn2, 2, 0.02, 0.05, 0.02, 0,
                            new Particle.DustOptions(Color.fromRGB(200, 30, 0), 0.8f));
                    p.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, p.getLocation().add(0, 0.5, 0), 1, 0.4, 0.2, 0.4, 0.01);
                }
                // Thorns: damage nearby enemies periodically
                if (tick % 40 == 0) {
                    for (LivingEntity e : ctx.getNearbyEnemies(p.getLocation(), 3.0, p)) {
                        ctx.dealDamage(p, e, 4.0);
                        e.setFireTicks(40);
                    }
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2620 DEMONIC FORM! Power unleashed!", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
    }

    // ================================================================
    //  MYTHIC TIER (24 weapons) - Spectacular. 8-15s.
    //  Particle budget: 100 per burst
    // ================================================================

    // #36 TRUE EXCALIBUR: Divine Shield - 8s invulnerability + golden dome + Strength II
    void holdTrueExcalibur(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 160, 4));
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 160, 1));
        p.playSound(p.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.2f);
        p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, p.getLocation(), 50, 1, 2, 1, 0.5);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 160 || !p.isOnline()) { cancel(); return; }
                // Golden dome
                if (tick % 2 == 0) {
                    for (int i = 0; i < 12; i++) {
                        double angle = tick * 0.1 + i * (Math.PI / 6);
                        double y = Math.sin(angle * 2 + tick * 0.05) * 1.5 + 1.5;
                        Location dome = p.getLocation().add(Math.cos(angle) * 3.0, y, Math.sin(angle) * 3.0);
                        p.getWorld().spawnParticle(Particle.DUST, dome, 2, 0.05, 0.05, 0.05, 0,
                                new Particle.DustOptions(Color.fromRGB(255, 215, 0), 1.5f));
                    }
                }
                // Holy cross on ground
                if (tick % 10 == 0) {
                    for (double d = 0; d < 3; d += 0.5) {
                        double a = tick * 0.02;
                        p.getWorld().spawnParticle(Particle.END_ROD, p.getLocation().add(Math.cos(a) * d, 0.1, Math.sin(a) * d), 1, 0, 0, 0, 0);
                        p.getWorld().spawnParticle(Particle.END_ROD, p.getLocation().add(Math.cos(a + Math.PI / 2) * d, 0.1, Math.sin(a + Math.PI / 2) * d), 1, 0, 0, 0, 0);
                    }
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2726 DIVINE SHIELD! 8s invulnerable!", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // #37 REQUIEM: Abyss Gate - summon 4 wither skeletons for 15s that hunt enemies
    void holdRequiemNinthAbyss(Player p) {
        p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.8f, 1.5f);
        LivingEntity mainTarget = ctx.getTargetEntity(p, 20.0);
        for (int i = 0; i < 4; i++) {
            Location spawnLoc = p.getLocation().add(Math.cos(i * Math.PI / 2) * 2, 0, Math.sin(i * Math.PI / 2) * 2);
            p.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, spawnLoc, 15, 0.3, 0.8, 0.3, 0.03);
            WitherSkeleton ws = (WitherSkeleton) p.getWorld().spawnEntity(spawnLoc, EntityType.WITHER_SKELETON);
            ws.customName(Component.text("Abyss Servant", NamedTextColor.DARK_PURPLE));
            ws.setCustomNameVisible(true);
            ws.getEquipment().setItemInMainHand(new ItemStack(Material.STONE_SWORD));
            if (mainTarget != null) ws.setTarget(mainTarget);
            ctx.trackSummon(p, ws);
            final Entity servant = ws;
            Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> { if (!servant.isDead()) servant.remove(); }, 300L);
        }
        // Portal visual
        spawnDustRing(p.getLocation(), 3.0, Color.fromRGB(80, 0, 120), 30, p.getWorld());
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 40) { cancel(); return; }
                spawnDustRing(p.getLocation().add(0, tick * 0.05, 0), 3.0 - tick * 0.05, Color.fromRGB(100, 0, 150), 10, p.getWorld());
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2620 ABYSS GATE! Servants arise!", NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
    }

    // #38 PHOENIX'S GRACE: Rebirth Flame - auto-revive at 50% HP within 60s + fire wings
    void holdPhoenixsGrace(Player p) {
        UUID uid = p.getUniqueId();
        ctx.rebornReady.put(uid, true);
        ctx.rebornExpiry.put(uid, System.currentTimeMillis() + 60000L);
        p.playSound(p.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 1.5f, 1.0f);
        p.getWorld().spawnParticle(Particle.FLAME, p.getLocation().add(0, 1, 0), 50, 0.5, 1, 0.5, 0.08);
        // Fire wings animation
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 60 || !p.isOnline() || !ctx.rebornReady.getOrDefault(uid, false)) { cancel(); return; }
                if (tick % 5 == 0) {
                    Location back = p.getLocation().add(0, 1.2, 0);
                    Vector right = ctx.rotateY(p.getLocation().getDirection().normalize(), Math.PI / 2).multiply(0.8);
                    for (int w = 0; w < 5; w++) {
                        double spread = (w + 1) * 0.3;
                        Location wing1 = back.clone().add(right.clone().multiply(spread)).add(0, w * 0.15, 0);
                        Location wing2 = back.clone().subtract(right.clone().multiply(spread)).add(0, w * 0.15, 0);
                        p.getWorld().spawnParticle(Particle.DUST, wing1, 2, 0.05, 0.05, 0.05, 0,
                                new Particle.DustOptions(Color.fromRGB(255, 150 + w * 20, 0), 1.0f));
                        p.getWorld().spawnParticle(Particle.DUST, wing2, 2, 0.05, 0.05, 0.05, 0,
                                new Particle.DustOptions(Color.fromRGB(255, 150 + w * 20, 0), 1.0f));
                    }
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2600 REBIRTH FLAME! Auto-revive ready!", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // #39 SOUL COLLECTOR: Spirit Army - release stored souls as homing projectiles
    void holdSoulCollector(Player p) {
        int souls = ctx.soulCount.getOrDefault(p.getUniqueId(), 0);
        if (souls <= 0) { p.sendActionBar(Component.text("No souls stored! Kill enemies first.", NamedTextColor.RED)); return; }
        int toRelease = Math.min(souls, 8);
        ctx.soulCount.put(p.getUniqueId(), souls - toRelease);
        p.playSound(p.getLocation(), Sound.ENTITY_VEX_CHARGE, 1.5f, 0.5f);
        for (int i = 0; i < toRelease; i++) {
            final int delay = i * 4;
            Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
                Vector dir = p.getLocation().getDirection().normalize();
                Location start = p.getEyeLocation();
                new BukkitRunnable() {
                    Location loc = start.clone();
                    int t = 0;
                    @Override public void run() {
                        if (t > 20) { cancel(); return; }
                        loc.add(dir);
                        p.getWorld().spawnParticle(Particle.SOUL, loc, 5, 0.1, 0.1, 0.1, 0.01);
                        p.getWorld().spawnParticle(Particle.DUST, loc, 3, 0.1, 0.1, 0.1, 0,
                                new Particle.DustOptions(Color.fromRGB(100, 200, 255), 0.8f));
                        for (LivingEntity e : ctx.getNearbyEnemies(loc, 1.5, p)) {
                            ctx.dealDamage(p, e, 8.0);
                            cancel(); return;
                        }
                        t++;
                    }
                }.runTaskTimer(ctx.plugin, 0L, 1L);
            }, delay);
        }
        p.sendActionBar(Component.text("\u2620 SPIRIT ARMY! " + toRelease + " souls released!", NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
    }

    // #40 VALHAKYRA: Wings of Valor - 10s flight + Strength I + golden wing trail
    void holdValhakyra(Player p) {
        p.setAllowFlight(true);
        p.setFlying(true);
        p.setVelocity(new Vector(0, 1.2, 0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 0));
        p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.5f, 1.5f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 200 || !p.isOnline()) {
                    if (p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR) {
                        p.setFlying(false);
                        p.setAllowFlight(false);
                    }
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 60, 0));
                    cancel(); return;
                }
                if (tick % 3 == 0 && p.isFlying()) {
                    Location back = p.getLocation().add(0, 1.0, 0);
                    Vector right = ctx.rotateY(p.getLocation().getDirection().normalize(), Math.PI / 2).multiply(0.6);
                    for (int w = 0; w < 4; w++) {
                        Location w1 = back.clone().add(right.clone().multiply(w + 1)).add(0, -w * 0.1, 0);
                        Location w2 = back.clone().subtract(right.clone().multiply(w + 1)).add(0, -w * 0.1, 0);
                        p.getWorld().spawnParticle(Particle.DUST, w1, 2, 0.05, 0.05, 0.05, 0,
                                new Particle.DustOptions(Color.fromRGB(255, 215, 0), 1.2f));
                        p.getWorld().spawnParticle(Particle.DUST, w2, 2, 0.05, 0.05, 0.05, 0,
                                new Particle.DustOptions(Color.fromRGB(255, 215, 0), 1.2f));
                    }
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2694 WINGS OF VALOR! 10s flight!", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // #41 PHANTOMGUARD: Phase Shift - 5s intangibility + invisibility + speed
    void holdPhantomguard(Player p) {
        UUID uid = p.getUniqueId();
        ctx.phaseShiftActive.put(uid, true);
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 4));
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 2));
        p.playSound(p.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1.5f, 0.5f);
        p.getWorld().spawnParticle(Particle.REVERSE_PORTAL, p.getLocation().add(0, 1, 0), 50, 0.5, 1, 0.5, 0.1);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 100 || !p.isOnline()) {
                    ctx.phaseShiftActive.put(uid, false);
                    p.sendActionBar(Component.text("Phase Shift ended.", NamedTextColor.GRAY));
                    cancel(); return;
                }
                if (tick % 3 == 0) {
                    p.getWorld().spawnParticle(Particle.DUST, p.getLocation().add(0, 1, 0), 5,
                            0.4, 0.8, 0.4, 0, new Particle.DustOptions(Color.fromRGB(150, 150, 200), 0.6f));
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2620 PHASE SHIFT! 5s intangible!", NamedTextColor.GRAY).decorate(TextDecoration.BOLD));
    }

    // #42 ZENITH: Ascension - 12s flight + Strength III + golden aura
    void holdZenith(Player p) {
        p.setAllowFlight(true);
        p.setFlying(true);
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 240, 2));
        p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.0f, 1.0f);
        p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, p.getLocation(), 60, 1, 2, 1, 0.8);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 240 || !p.isOnline()) {
                    if (p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR) {
                        p.setFlying(false);
                        p.setAllowFlight(false);
                    }
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 80, 0));
                    cancel(); return;
                }
                // Ascending helix
                if (tick % 2 == 0) {
                    double a1 = tick * 0.2;
                    double a2 = a1 + Math.PI;
                    Location h1 = p.getLocation().add(Math.cos(a1) * 1.0, 0.5, Math.sin(a1) * 1.0);
                    Location h2 = p.getLocation().add(Math.cos(a2) * 1.0, 0.5, Math.sin(a2) * 1.0);
                    p.getWorld().spawnParticle(Particle.DUST, h1, 3, 0, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(255, 215, 0), 1.2f));
                    p.getWorld().spawnParticle(Particle.DUST, h2, 3, 0, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(255, 255, 200), 1.2f));
                    p.getWorld().spawnParticle(Particle.END_ROD, p.getLocation().add(0, -0.5, 0), 2, 0.3, 0, 0.3, 0.01);
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2726 ASCENSION! 12s Flight + Str III!", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // #43 DRAGON SWORD: Draconic Roar - 10s: fear enemies 8 blocks + fire breath cone each 2s
    void holdDragonSword(Player p) {
        p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.8f);
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 1));
        // Initial fear
        for (LivingEntity e : ctx.getNearbyEnemies(p.getLocation(), 8.0, p)) {
            Vector away = e.getLocation().toVector().subtract(p.getLocation().toVector()).normalize().multiply(1.5);
            e.setVelocity(away.setY(0.3));
            e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
        }
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 200 || !p.isOnline()) { cancel(); return; }
                // Fire aura
                if (tick % 4 == 0) {
                    double angle = tick * 0.2;
                    for (int i = 0; i < 3; i++) {
                        double a = angle + i * (Math.PI * 2 / 3);
                        Location f = p.getLocation().add(Math.cos(a) * 1.5, 0.8 + Math.random() * 0.5, Math.sin(a) * 1.5);
                        p.getWorld().spawnParticle(Particle.DUST, f, 3, 0.05, 0.1, 0.05, 0,
                                new Particle.DustOptions(Color.fromRGB(255, 100 + (int)(Math.random() * 100), 0), 1.2f));
                    }
                }
                // Fire breath cone every 2 seconds
                if (tick % 40 == 0 && tick > 0) {
                    Vector dir = p.getLocation().getDirection().normalize();
                    for (int d = 1; d <= 5; d++) {
                        Location cone = p.getEyeLocation().add(dir.clone().multiply(d));
                        p.getWorld().spawnParticle(Particle.FLAME, cone, 5 + d * 3, d * 0.3, d * 0.3, d * 0.3, 0.02);
                        for (LivingEntity e : ctx.getNearbyEnemies(cone, 1.0 + d * 0.3, p)) {
                            ctx.dealDamage(p, e, 6.0);
                            e.setFireTicks(40);
                        }
                    }
                    p.playSound(p.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 0.5f);
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2620 DRACONIC ROAR! Enemies tremble!", NamedTextColor.RED).decorate(TextDecoration.BOLD));
    }

    // #44 NOCTURNE: Night Cloak - 10s: invisibility + 50% speed + backstab bonus
    void holdNocturne(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
        p.playSound(p.getLocation(), Sound.AMBIENT_CAVE, 1.0f, 1.5f);
        p.getWorld().spawnParticle(Particle.SMOKE, p.getLocation().add(0, 1, 0), 40, 0.5, 1, 0.5, 0.03);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 200 || !p.isOnline()) { cancel(); return; }
                if (tick % 5 == 0) {
                    p.getWorld().spawnParticle(Particle.DUST, p.getLocation().add(0, 0.5, 0), 4,
                            0.5, 0.2, 0.5, 0, new Particle.DustOptions(Color.fromRGB(20, 0, 40), 0.6f));
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u263D NIGHT CLOAK! Vanish into darkness!", NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
    }

    // #45 DIVINE AXE RHITTA: Sunshine - 10s: Strength II + Fire Resist + blazing sun aura
    void altDivineAxeRhitta(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 1));
        p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 200, 0));
        p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 2.0f, 1.0f);
        p.getWorld().spawnParticle(Particle.FLAME, p.getLocation().add(0, 1, 0), 80, 1.5, 2, 1.5, 0.1);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 200 || !p.isOnline()) { cancel(); return; }
                // Mini sun above head
                if (tick % 3 == 0) {
                    Location sun = p.getLocation().add(0, 3, 0);
                    for (int i = 0; i < 8; i++) {
                        double angle = tick * 0.15 + i * (Math.PI / 4);
                        double r = 0.5 + Math.sin(tick * 0.1) * 0.2;
                        Location ray = sun.clone().add(Math.cos(angle) * r, Math.sin(angle * 2) * 0.3, Math.sin(angle) * r);
                        p.getWorld().spawnParticle(Particle.DUST, ray, 2, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(255, 200, 50), 1.5f));
                    }
                    // Heat shimmer
                    p.getWorld().spawnParticle(Particle.FLAME, p.getLocation().add(0, 0.5, 0), 2, 0.8, 0.1, 0.8, 0.01);
                }
                // Burn enemies nearby every 2s
                if (tick % 40 == 0) {
                    for (LivingEntity e : ctx.getNearbyEnemies(p.getLocation(), 5.0, p)) {
                        e.setFireTicks(40);
                        ctx.dealDamage(p, e, 4.0);
                    }
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2600 SUNSHINE! Blazing power!", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // #46 YORU: Dark Mirror - teleport behind target + brief shadow clone + backstab
    void altYoru(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 15.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        Location origin = p.getLocation().clone();
        Location behind = target.getLocation().clone().add(target.getLocation().getDirection().normalize().multiply(-2));
        behind.setDirection(target.getLocation().toVector().subtract(behind.toVector()));
        // Shadow clone at old position
        p.getWorld().spawnParticle(Particle.DUST, origin.add(0, 1, 0), 40, 0.3, 0.8, 0.3, 0,
                new Particle.DustOptions(Color.fromRGB(20, 0, 40), 1.5f));
        p.teleport(behind);
        ctx.dealDamage(p, target, 18.0);
        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 2.0f, 0.5f);
        // Slash visual
        p.getWorld().spawnParticle(Particle.SWEEP_ATTACK, target.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0);
        p.getWorld().spawnParticle(Particle.DUST, target.getLocation().add(0, 1, 0), 20, 0.5, 0.8, 0.5, 0,
                new Particle.DustOptions(Color.fromRGB(30, 0, 50), 1.2f));
        p.sendActionBar(Component.text("\u2694 DARK MIRROR! Backstab!", NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
    }

    // #47 TENGEN'S BLADE: Constant Flux - 12s: Speed III + Haste II + musical note trail
    void altTengensBlade(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 240, 2));
        p.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 240, 1));
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 2.0f, 2.0f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 240 || !p.isOnline()) { cancel(); return; }
                if (tick % 4 == 0) {
                    Location trail = p.getLocation().add(0, 1.5, 0);
                    p.getWorld().spawnParticle(Particle.NOTE, trail, 3, 0.5, 0.3, 0.5, 0.5);
                    // Colored sound wave rings
                    double angle = tick * 0.3;
                    int r = (int)(127 + Math.sin(tick * 0.1) * 128);
                    int g = (int)(127 + Math.sin(tick * 0.1 + 2) * 128);
                    int b = (int)(127 + Math.sin(tick * 0.1 + 4) * 128);
                    Location wave = p.getLocation().add(Math.cos(angle) * 1.2, 1.0, Math.sin(angle) * 1.2);
                    p.getWorld().spawnParticle(Particle.DUST, wave, 3, 0.05, 0.05, 0.05, 0,
                            new Particle.DustOptions(Color.fromRGB(Math.abs(r) % 256, Math.abs(g) % 256, Math.abs(b) % 256), 0.8f));
                }
                if (tick % 40 == 0) {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 0.8f + (float)(Math.random() * 1.2));
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u266B CONSTANT FLUX! Speed III + Haste II!", NamedTextColor.LIGHT_PURPLE));
    }

    // #48 EDGE OF THE ASTRAL PLANE: Planar Shift - 5s: phase through blocks, invulnerable, Speed IV, astral trail
    void altEdgeAstralPlane(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 3));
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 4));
        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 1.5f);
        p.getWorld().spawnParticle(Particle.REVERSE_PORTAL, p.getLocation().add(0, 1, 0), 60, 0.5, 1, 0.5, 0.15);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 100 || !p.isOnline()) { cancel(); return; }
                if (tick % 2 == 0) {
                    for (int i = 0; i < 6; i++) {
                        double angle = tick * 0.3 + i * (Math.PI / 3);
                        Location star = p.getLocation().add(Math.cos(angle) * 1.5, 0.8 + Math.sin(tick * 0.2 + i) * 0.5, Math.sin(angle) * 1.5);
                        p.getWorld().spawnParticle(Particle.DUST, star, 2, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(180, 100, 255), 0.8f));
                    }
                    p.getWorld().spawnParticle(Particle.END_ROD, p.getLocation().add(0, 0.5, 0), 2, 0.2, 0.1, 0.2, 0.02);
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2727 PLANAR SHIFT! Between dimensions!", NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD));
    }

    // #49 FALLEN GOD'S SPEAR: Heaven's Fall - leap up + divine slam, 20 dmg AoE + holy fire
    void altFallenGodsSpear(Player p) {
        p.setVelocity(new Vector(0, 3.0, 0));
        p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 2.0f, 0.8f);
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 4));
        // Trail while ascending
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 25) { cancel(); return; }
                p.getWorld().spawnParticle(Particle.DUST, p.getLocation(), 5, 0.2, 0.1, 0.2, 0,
                        new Particle.DustOptions(Color.fromRGB(255, 230, 150), 1.5f));
                p.getWorld().spawnParticle(Particle.END_ROD, p.getLocation(), 3, 0.1, 0.3, 0.1, 0.02);
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        // Impact
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            Location land = p.getLocation();
            p.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, land, 2, 0, 0, 0, 0);
            spawnDustRing(land, 8.0, Color.fromRGB(255, 215, 0), 40, p.getWorld());
            p.getWorld().spawnParticle(Particle.END_ROD, land.clone().add(0, 1, 0), 40, 4, 1, 4, 0.2);
            p.playSound(land, Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.5f);
            for (LivingEntity e : ctx.getNearbyEnemies(land, 8.0, p)) {
                ctx.dealDamage(p, e, 20.0);
                e.setVelocity(e.getLocation().toVector().subtract(land.toVector()).normalize().multiply(2.0).setY(1.0));
                e.setFireTicks(60);
            }
        }, 30L);
        p.sendActionBar(Component.text("\u2694 HEAVEN'S FALL!", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
    }

    // #50 NATURE SWORD: Overgrowth Surge - heal 8 hearts + Regen III 10s + ally heal + vine burst
    void altNatureSword(Player p) {
        double max = p.getAttribute(Attribute.MAX_HEALTH).getValue();
        p.setHealth(Math.min(max, p.getHealth() + 16.0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 2));
        p.playSound(p.getLocation(), Sound.BLOCK_GRASS_BREAK, 2.0f, 1.2f);
        // Expanding vine ring
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 30) { cancel(); return; }
                double r = tick * 0.3;
                spawnDustRing(p.getLocation().add(0, 0.3, 0), r, Color.fromRGB(30, 180, 30), 15, p.getWorld());
                p.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, p.getLocation().add(0, 1, 0), 5, r * 0.3, 0.5, r * 0.3, 0);
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        // Heal allies
        for (Entity e : p.getWorld().getNearbyEntities(p.getLocation(), 8, 8, 8)) {
            if (e instanceof Player ally && ally != p && ctx.guildManager.areInSameGuild(p.getUniqueId(), ally.getUniqueId())) {
                double amax = ally.getAttribute(Attribute.MAX_HEALTH).getValue();
                ally.setHealth(Math.min(amax, ally.getHealth() + 10.0));
                ally.getWorld().spawnParticle(Particle.HEART, ally.getLocation().add(0, 2, 0), 5, 0.3, 0.3, 0.3, 0);
            }
        }
        p.sendActionBar(Component.text("\u2618 OVERGROWTH SURGE! Massive heal!", NamedTextColor.GREEN).decorate(TextDecoration.BOLD));
    }

    // #51 HEAVENLY PARTISAN: Celestial Judgment - 3 targeted lightning smites on nearest enemies
    void altHeavenlyPartisan(Player p) {
        List<LivingEntity> targets = ctx.getNearbyEnemies(p.getLocation(), 15.0, p);
        int strikes = Math.min(3, targets.size());
        if (strikes == 0) { p.sendActionBar(Component.text("No enemies nearby!", NamedTextColor.RED)); return; }
        p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 0.8f);
        for (int i = 0; i < strikes; i++) {
            LivingEntity t = targets.get(i);
            final int delay = i * 15;
            Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
                if (t.isDead()) return;
                // Pre-strike warning ring
                spawnDustRing(t.getLocation(), 2.0, Color.fromRGB(255, 255, 150), 15, p.getWorld());
                Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
                    if (t.isDead()) return;
                    p.getWorld().strikeLightningEffect(t.getLocation());
                    ctx.dealDamage(p, t, 18.0);
                    p.getWorld().spawnParticle(Particle.END_ROD, t.getLocation().add(0, 1, 0), 30, 0.5, 2, 0.5, 0.1);
                }, 10L);
            }, delay);
        }
        p.sendActionBar(Component.text("\u2694 CELESTIAL JUDGMENT! " + strikes + " divine smites!", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
    }

    // #52 SOUL DEVOURER: Devouring Maw - AoE soul drain 12 dmg + heal per enemy + dark vortex
    void altSoulDevourer(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.ENTITY_WITHER_HURT, 1.5f, 0.5f);
        // Spiraling vortex
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 20) { cancel(); return; }
                for (int i = 0; i < 12; i++) {
                    double angle = tick * 0.5 + i * (Math.PI / 6);
                    double r = 6.0 - tick * 0.25;
                    Location vortex = c.clone().add(Math.cos(angle) * r, 0.5 + tick * 0.1, Math.sin(angle) * r);
                    p.getWorld().spawnParticle(Particle.DUST, vortex, 3, 0.1, 0.1, 0.1, 0,
                            new Particle.DustOptions(Color.fromRGB(50, 0, 80), 1.5f));
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        // Damage + heal after brief delay
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            p.getWorld().spawnParticle(Particle.SOUL, c.clone().add(0, 1, 0), 60, 3, 1, 3, 0.05);
            int hits = 0;
            for (LivingEntity e : ctx.getNearbyEnemies(c, 6.0, p)) {
                ctx.dealDamage(p, e, 12.0);
                hits++;
                spawnDustLine(e.getLocation().add(0, 1, 0), c.clone().add(0, 1, 0), Color.fromRGB(100, 0, 150), 8, p.getWorld());
            }
            double heal = hits * 4.0;
            double max = p.getAttribute(Attribute.MAX_HEALTH).getValue();
            p.setHealth(Math.min(max, p.getHealth() + heal));
            p.sendActionBar(Component.text("\u2620 DEVOURING MAW! Devoured " + hits + " souls!", NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
        }, 20L);
        p.sendActionBar(Component.text("\u2620 Devouring Maw charging...", NamedTextColor.DARK_PURPLE));
    }

    // #53 MJOLNIR: Bifrost Slam - leap + triple rainbow lightning on landing
    void altMjolnir(Player p) {
        p.setVelocity(new Vector(0, 2.5, 0).add(p.getLocation().getDirection().multiply(1.0)));
        p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 2.0f, 0.8f);
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 4));
        // Rainbow trail while airborne
        new BukkitRunnable() {
            int tick = 0;
            final Color[] rainbow = { Color.RED, Color.ORANGE, Color.YELLOW, Color.LIME, Color.AQUA, Color.BLUE, Color.PURPLE };
            @Override public void run() {
                if (tick >= 25) { cancel(); return; }
                Color c = rainbow[tick % rainbow.length];
                p.getWorld().spawnParticle(Particle.DUST, p.getLocation(), 8, 0.3, 0.1, 0.3, 0,
                        new Particle.DustOptions(c, 1.5f));
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        // Impact
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            Location land = p.getLocation();
            for (int i = 0; i < 3; i++) {
                Location strike = land.clone().add(Math.sin(i * 2.1) * 4, 0, Math.cos(i * 2.1) * 4);
                p.getWorld().strikeLightningEffect(strike);
            }
            p.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, land, 80, 5, 1, 5, 0.3);
            spawnDustRing(land, 6.0, Color.fromRGB(100, 200, 255), 40, p.getWorld());
            p.playSound(land, Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.6f);
            for (LivingEntity e : ctx.getNearbyEnemies(land, 8.0, p)) {
                ctx.dealDamage(p, e, 22.0);
                e.setVelocity(e.getLocation().toVector().subtract(land.toVector()).normalize().multiply(2.0).setY(1.5));
            }
        }, 25L);
        p.sendActionBar(Component.text("\u26A1 BIFROST SLAM! \u26A1", NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
    }

    // #54 THOUSAND DEMON DAGGERS: Infernal Dance - 8s fire spin dealing 4 dmg/s to nearby enemies
    void altThousandDemonDaggers(Player p) {
        p.playSound(p.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 2.0f, 1.0f);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 160, 1));
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 160 || !p.isOnline()) { cancel(); return; }
                // Spinning dagger ring
                if (tick % 2 == 0) {
                    for (int i = 0; i < 6; i++) {
                        double angle = tick * 0.35 + i * (Math.PI / 3);
                        double r = 2.5 + Math.sin(tick * 0.1) * 0.5;
                        Location dagger = p.getLocation().add(Math.cos(angle) * r, 0.8 + Math.sin(angle * 2 + tick * 0.1) * 0.3, Math.sin(angle) * r);
                        p.getWorld().spawnParticle(Particle.DUST, dagger, 3, 0.02, 0.02, 0.02, 0,
                                new Particle.DustOptions(Color.fromRGB(200, 50, 0), 1.0f));
                        p.getWorld().spawnParticle(Particle.FLAME, dagger, 1, 0, 0, 0, 0.01);
                    }
                }
                if (tick % 20 == 0) {
                    for (LivingEntity e : ctx.getNearbyEnemies(p.getLocation(), 3.5, p)) {
                        ctx.dealDamage(p, e, 4.0);
                        e.setFireTicks(30);
                    }
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2620 INFERNAL DANCE! Fire daggers spin!", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
    }

    // #55 STAR EDGE: Supernova - massive 12-block AoE burst + Blindness + knockback + starfield
    void altStarEdge(Player p) {
        Location c = p.getLocation().clone();
        p.playSound(c, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
        // Expanding star burst
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 15) { cancel(); return; }
                double r = tick * 0.8;
                for (int i = 0; i < 20; i++) {
                    double angle = i * (Math.PI / 10) + tick * 0.2;
                    double y = 1.0 + Math.sin(angle * 3) * 1.0;
                    Location star = c.clone().add(Math.cos(angle) * r, y, Math.sin(angle) * r);
                    p.getWorld().spawnParticle(Particle.DUST, star, 3, 0.1, 0.1, 0.1, 0,
                            new Particle.DustOptions(Color.fromRGB(255, 255, 200), 1.5f));
                }
                if (tick == 5) {
                    p.getWorld().spawnParticle(Particle.END_ROD, c.clone().add(0, 2, 0), 80, 6, 3, 6, 0.3);
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        // Delayed damage
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            for (LivingEntity e : ctx.getNearbyEnemies(c, 12.0, p)) {
                ctx.dealDamage(p, e, 18.0);
                e.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
                e.setVelocity(e.getLocation().toVector().subtract(c.toVector()).normalize().multiply(2.5).setY(1.0));
            }
        }, 5L);
        p.sendActionBar(Component.text("\u2726 SUPERNOVA! \u2726", NamedTextColor.WHITE).decorate(TextDecoration.BOLD));
    }

    // #56 RIVERS OF BLOOD: Blood Tsunami - advancing wave 10 blocks, 16 dmg + Wither
    void altRiversOfBlood(Player p) {
        Vector dir = p.getLocation().getDirection().normalize();
        Location start = p.getLocation().clone();
        p.playSound(start, Sound.ENTITY_GENERIC_SPLASH, 2.0f, 0.4f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick > 12) { cancel(); return; }
                Location point = start.clone().add(dir.clone().multiply(tick));
                // Blood wave wall
                for (int i = 0; i < 5; i++) {
                    Vector side = ctx.rotateY(dir.clone(), Math.PI / 2).multiply(i * 0.5 - 1.0);
                    for (int h = 0; h < 3; h++) {
                        Location wave = point.clone().add(side).add(0, h * 0.5 + 0.3, 0);
                        p.getWorld().spawnParticle(Particle.DUST, wave, 3, 0.1, 0.1, 0.1, 0,
                                new Particle.DustOptions(Color.fromRGB(139, 0, 0), 2.0f));
                    }
                }
                for (LivingEntity e : ctx.getNearbyEnemies(point, 3.0, p)) {
                    ctx.dealDamage(p, e, 16.0);
                    e.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 80, 1));
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 2L);
        p.sendActionBar(Component.text("\u2620 BLOOD TSUNAMI!", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
    }

    // #57 DRAGON SLAYING BLADE: Slayer's Fury - 10s: Strength II + Resistance II + dragon-scale aura
    void altDragonSlayingBlade(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 1));
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 1));
        p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.5f);
        p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, p.getLocation().add(0, 1, 0), 50, 1, 1.5, 1, 0.3);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 200 || !p.isOnline()) { cancel(); return; }
                if (tick % 4 == 0) {
                    // Dragon scale shimmer
                    for (int i = 0; i < 4; i++) {
                        double angle = tick * 0.15 + i * (Math.PI / 2);
                        Location scale = p.getLocation().add(Math.cos(angle) * 1.2, 0.5 + i * 0.3, Math.sin(angle) * 1.2);
                        p.getWorld().spawnParticle(Particle.DUST, scale, 2, 0.05, 0.05, 0.05, 0,
                                new Particle.DustOptions(Color.fromRGB(50, 200, 100), 1.0f));
                    }
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2694 SLAYER'S FURY! Dragon power!", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // #58 STOP SIGN: Road Rage - charge forward 10 blocks, stun + 12 dmg, red blur
    void altStopSign(Player p) {
        p.setVelocity(p.getLocation().getDirection().multiply(3.0));
        p.playSound(p.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 2.0f, 0.8f);
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20, 3));
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 15) { cancel(); return; }
                Location loc = p.getLocation();
                // Red speed trail
                p.getWorld().spawnParticle(Particle.DUST, loc.clone().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0,
                        new Particle.DustOptions(Color.fromRGB(255, 0, 0), 2.0f));
                for (LivingEntity e : ctx.getNearbyEnemies(loc, 2.5, p)) {
                    ctx.dealDamage(p, e, 12.0);
                    e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 127));
                    e.setVelocity(new Vector(0, 0.8, 0));
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u26D4 ROAD RAGE! \u26D4", NamedTextColor.RED).decorate(TextDecoration.BOLD));
    }

    // #59 CREATION SPLITTER: Genesis Break - massive 15-block AoE, 30 dmg, reality-crack visual
    void altCreationSplitter(Player p) {
        Location c = p.getLocation().clone();
        p.playSound(c, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 0.3f);
        p.playSound(c, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.3f);
        // Reality crack expanding outward
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 20) { cancel(); return; }
                double r = tick * 0.75;
                for (int i = 0; i < 24; i++) {
                    double angle = i * (Math.PI / 12) + tick * 0.1;
                    Location crack = c.clone().add(Math.cos(angle) * r, 0.5 + Math.random() * 2, Math.sin(angle) * r);
                    p.getWorld().spawnParticle(Particle.DUST, crack, 3, 0.1, 0.2, 0.1, 0,
                            new Particle.DustOptions(Color.fromRGB(255, 100, 255), 2.0f));
                }
                if (tick % 4 == 0) {
                    p.getWorld().spawnParticle(Particle.END_ROD, c.clone().add(0, 3, 0), 15, r * 0.5, 2, r * 0.5, 0.2);
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        // Damage pulse
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            p.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, c.clone().add(0, 2, 0), 5, 3, 2, 3, 0);
            for (LivingEntity e : ctx.getNearbyEnemies(c, 15.0, p)) {
                ctx.dealDamage(p, e, 30.0);
                e.setVelocity(e.getLocation().toVector().subtract(c.toVector()).normalize().multiply(3.0).setY(1.5));
            }
        }, 10L);
        p.sendActionBar(Component.text("\u2726 GENESIS BREAK! Reality shatters!", NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD));
    }

    // ================================================================
    //  ABYSSAL TIER (4 weapons) - Reality-warping, domain control. 10-20s.
    //  Particle budget: 200 per burst
    // ================================================================

    // #60 REQUIEM AWAKENED: Void Collapse - 15-block singularity, 10s pull + DoT, 60 dmg final explosion
    void altRequiemAwakened(Player p) {
        Location target = p.getLocation().add(p.getLocation().getDirection().multiply(8));
        final Location center = target.clone();
        p.playSound(center, Sound.ENTITY_WARDEN_EMERGE, 2.0f, 0.3f);
        p.playSound(center, Sound.BLOCK_PORTAL_TRIGGER, 1.5f, 0.3f);
        p.getWorld().spawnParticle(Particle.REVERSE_PORTAL, center, 80, 2, 2, 2, 0.5);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 200) {
                    // Final collapse - massive explosion
                    center.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, center, 3, 0, 0, 0, 0);
                    center.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, center, 80, 8, 5, 8, 0.4);
                    center.getWorld().spawnParticle(Particle.REVERSE_PORTAL, center, 100, 10, 5, 10, 0.8);
                    center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.3f);
                    center.getWorld().playSound(center, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 0.3f);
                    for (LivingEntity e : ctx.getNearbyEnemies(center, 15.0, p)) {
                        ctx.dealDamage(p, e, 60.0);
                        e.setVelocity(e.getLocation().toVector().subtract(center.toVector()).normalize().multiply(4.0).setY(2.0));
                    }
                    cancel();
                    return;
                }
                // Collapsing vortex
                double progress = tick / 200.0;
                double vortexRadius = 8.0 * (1.0 - progress * 0.8);
                for (int i = 0; i < 30; i++) {
                    double angle = Math.toRadians(i * 12 + tick * 4);
                    double r = vortexRadius * (1.0 - (i / 30.0) * 0.5);
                    double y = Math.sin(tick * 0.08 + i * 0.3) * (2.0 + progress * 2.0);
                    Location vortex = center.clone().add(Math.cos(angle) * r, 1 + y, Math.sin(angle) * r);
                    int red = (int)(50 + progress * 150);
                    p.getWorld().spawnParticle(Particle.DUST, vortex, 2, 0.1, 0.1, 0.1, 0,
                            new Particle.DustOptions(Color.fromRGB(red, 0, (int)(120 - progress * 80)), 1.5f));
                }
                // Central dark core
                if (tick % 3 == 0) {
                    p.getWorld().spawnParticle(Particle.SCULK_CHARGE_POP, center, 10, 1.5, 1.5, 1.5, 0.05);
                    p.getWorld().spawnParticle(Particle.REVERSE_PORTAL, center, 5, 0.5, 0.5, 0.5, 0.1);
                }
                // Pull + damage every second
                if (tick % 20 == 0) {
                    for (LivingEntity e : ctx.getNearbyEnemies(center, 15.0, p)) {
                        double pullForce = 0.5 + progress * 0.8;
                        Vector pull = center.toVector().subtract(e.getLocation().toVector()).normalize().multiply(pullForce);
                        e.setVelocity(e.getVelocity().add(pull));
                        e.damage(8.0, p);
                        e.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 40, 0));
                        e.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 40, 1));
                    }
                    center.getWorld().playSound(center, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.3f + (float)(progress * 0.5));
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2620 VOID COLLAPSE! Reality tears apart!", TextColor.color(170, 0, 0)).decorate(TextDecoration.BOLD));
    }

    // #61 EXCALIBUR AWAKENED: Sacred Realm - 15s golden sanctuary dome, allies buffed, enemies burned
    void altExcaliburAwakened(Player p) {
        Location center = p.getLocation().clone();
        p.playSound(center, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.0f, 0.8f);
        p.playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 2.0f, 1.0f);
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 300, 4));
        p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, center, 100, 3, 3, 3, 0.5);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 300 || !p.isOnline()) { cancel(); return; }
                // Golden dome wall
                if (tick % 2 == 0) {
                    for (int i = 0; i < 36; i++) {
                        double angle = Math.toRadians(i * 10 + tick * 2);
                        double height = Math.sin(Math.toRadians(i * 10)) * 5 + 3;
                        Location wall = center.clone().add(Math.cos(angle) * 12, height, Math.sin(angle) * 12);
                        p.getWorld().spawnParticle(Particle.DUST, wall, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(255, 215, 0), 2.0f));
                    }
                }
                // Rotating holy symbol on ground
                if (tick % 3 == 0) {
                    double symbolAngle = tick * 0.03;
                    for (int arm = 0; arm < 4; arm++) {
                        double a = symbolAngle + arm * (Math.PI / 2);
                        for (double d = 2; d < 10; d += 1.0) {
                            Location sym = center.clone().add(Math.cos(a) * d, 0.1, Math.sin(a) * d);
                            p.getWorld().spawnParticle(Particle.END_ROD, sym, 1, 0, 0, 0, 0);
                        }
                    }
                }
                // Pillar of light from center
                if (tick % 5 == 0) {
                    for (int h = 0; h < 10; h++) {
                        p.getWorld().spawnParticle(Particle.DUST, center.clone().add(0, h, 0), 3, 0.2, 0.1, 0.2, 0,
                                new Particle.DustOptions(Color.fromRGB(255, 255, 200), 1.5f));
                    }
                }
                // Buff allies + damage enemies every second
                if (tick % 20 == 0) {
                    for (Entity entity : center.getWorld().getNearbyEntities(center, 12, 12, 12)) {
                        if (entity instanceof Player ally && !ally.getUniqueId().equals(p.getUniqueId())) {
                            if (ctx.guildManager.areInSameGuild(p.getUniqueId(), ally.getUniqueId())) {
                                ally.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 30, 3));
                                ally.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 30, 3));
                                ally.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 30, 1));
                            }
                        }
                    }
                    for (LivingEntity e : ctx.getNearbyEnemies(center, 12.0, p)) {
                        e.damage(8.0, p);
                        e.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 30, 2));
                        e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 2));
                        e.setFireTicks(30);
                    }
                    center.getWorld().playSound(center, Sound.BLOCK_BEACON_AMBIENT, 1.5f, 1.5f);
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2726 SACRED REALM! Divine sanctuary!", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // #62 CREATION SPLITTER AWAKENED: Big Bang - 5s channel, 20-block 100 dmg explosion, massive knockback
    void altCreationSplitterAwakened(Player p) {
        p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 2.0f, 0.3f);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 127));
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 4));
//         p.setGlowing(true); // REMOVED: no glowing on mobs
        // 5-second ascending charge with escalating visuals
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 100) { cancel(); return; }
                double progress = tick / 100.0;
                int particles = (int)(10 + progress * 190);
                double radius = 1 + progress * 12;
                // Expanding particle sphere
                for (int i = 0; i < Math.min(particles / 5, 40); i++) {
                    double phi = Math.random() * Math.PI * 2;
                    double theta = Math.random() * Math.PI;
                    double r = radius * Math.random();
                    Location pt = p.getLocation().add(
                            Math.sin(theta) * Math.cos(phi) * r,
                            1 + progress * 4 + Math.cos(theta) * r * 0.5,
                            Math.sin(theta) * Math.sin(phi) * r);
                    int red = (int)(255 * progress);
                    int green = (int)(200 * (1 - progress));
                    p.getWorld().spawnParticle(Particle.DUST, pt, 2, 0, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(Math.max(50, red), Math.max(50, green), 50), 1.5f + (float)progress));
                }
                // Warning ring on ground
                if (tick % 2 == 0) {
                    for (int i = 0; i < 72; i++) {
                        double angle = Math.toRadians(i * 5);
                        Location ring = p.getLocation().add(Math.cos(angle) * 20, 0.1, Math.sin(angle) * 20);
                        float pulse = (float)(1.0 + Math.sin(tick * 0.3) * 0.5);
                        p.getWorld().spawnParticle(Particle.DUST, ring, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(255, 50, 50), pulse * 2));
                    }
                }
                if (tick % 20 == 0) {
                    p.playSound(p.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 2.0f, 0.5f + (float)(progress * 1.5));
                    p.getWorld().spawnParticle(Particle.FLASH, p.getLocation().add(0, 1, 0), 1, 0, 0, 0, 0);
                }
                // Ascending helix
                double h1Angle = tick * 0.3;
                double h2Angle = h1Angle + Math.PI;
                double hRadius = 2 + progress * 4;
                p.getWorld().spawnParticle(Particle.END_ROD,
                        p.getLocation().add(Math.cos(h1Angle) * hRadius, 1 + progress * 5, Math.sin(h1Angle) * hRadius),
                        3, 0, 0, 0, 0.02);
                p.getWorld().spawnParticle(Particle.END_ROD,
                        p.getLocation().add(Math.cos(h2Angle) * hRadius, 1 + progress * 5, Math.sin(h2Angle) * hRadius),
                        3, 0, 0, 0, 0.02);
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        // THE BIG BANG detonation after 5 seconds
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            p.setGlowing(false);
            p.removePotionEffect(PotionEffectType.SLOWNESS);
            Location blast = p.getLocation();
            // Multi-stage explosion
            blast.getWorld().spawnParticle(Particle.FLASH, blast.clone().add(0, 2, 0), 5, 0, 0, 0, 0);
            blast.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, blast.clone().add(0, 3, 0), 10, 5, 3, 5, 0);
            blast.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, blast.clone().add(0, 4, 0), 200, 12, 10, 12, 1.0);
            blast.getWorld().spawnParticle(Particle.END_ROD, blast.clone().add(0, 5, 0), 200, 15, 12, 15, 0.5);
            // Rainbow dust expanding ring
            Color[] colors = { Color.RED, Color.ORANGE, Color.YELLOW, Color.LIME, Color.AQUA, Color.BLUE, Color.PURPLE };
            for (int c = 0; c < colors.length; c++) {
                final int ci = c;
                Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
                    double r = 4 + ci * 2.5;
                    for (int i = 0; i < 36; i++) {
                        double angle = Math.toRadians(i * 10);
                        Location ring = blast.clone().add(Math.cos(angle) * r, 1 + ci * 0.3, Math.sin(angle) * r);
                        blast.getWorld().spawnParticle(Particle.DUST, ring, 3, 0, 0, 0, 0,
                                new Particle.DustOptions(colors[ci], 3.0f));
                    }
                }, ci * 2L);
            }
            blast.getWorld().playSound(blast, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.3f);
            blast.getWorld().playSound(blast, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 0.3f);
            blast.getWorld().playSound(blast, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 0.3f);
            for (LivingEntity e : ctx.getNearbyEnemies(blast, 20.0, p)) {
                ctx.dealDamage(p, e, 100.0);
                Vector kb = e.getLocation().toVector().subtract(blast.toVector()).normalize().multiply(8.0).setY(5.0);
                e.setVelocity(kb);
                e.setFireTicks(200);
            }
            p.sendActionBar(Component.text("\u2726 BIG BANG!!! \u2726", TextColor.color(255, 215, 0)).decorate(TextDecoration.BOLD));
        }, 100L);
        p.sendActionBar(Component.text("\u2726 CHANNELING BIG BANG... DO NOT MOVE! \u2726", TextColor.color(255, 100, 0)).decorate(TextDecoration.BOLD));
    }

    // #63 WHISPERWIND AWAKENED: Phantom Cyclone - 12s homing tornado, 10 dmg/s, pulls from 20 blocks
    void altWhisperwindAwakened(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 240, 2));
        Location tornadoLoc = p.getLocation().add(p.getLocation().getDirection().multiply(5));
        p.playSound(tornadoLoc, Sound.ENTITY_BREEZE_WIND_BURST, 2.0f, 0.3f);
        p.playSound(tornadoLoc, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.5f, 0.5f);
        new BukkitRunnable() {
            int tick = 0;
            Location loc = tornadoLoc.clone();
            @Override public void run() {
                if (tick >= 240) {
                    // Final dispersal
                    loc.getWorld().spawnParticle(Particle.CLOUD, loc, 80, 5, 8, 5, 0.4);
                    loc.getWorld().playSound(loc, Sound.ENTITY_BREEZE_WIND_BURST, 1.5f, 0.5f);
                    cancel();
                    return;
                }
                // Home toward nearest enemy
                LivingEntity nearest = null;
                double nearestDist = 30;
                for (LivingEntity e : ctx.getNearbyEnemies(loc, 30.0, p)) {
                    double d = e.getLocation().distance(loc);
                    if (d < nearestDist) { nearestDist = d; nearest = e; }
                }
                if (nearest != null) {
                    Vector toTarget = nearest.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(0.4);
                    loc.add(toTarget);
                }
                // Spiral tornado column
                for (int h = 0; h < 12; h++) {
                    double angle = Math.toRadians(tick * 10 + h * 30);
                    double r = 1.0 + h * 0.35;
                    Location spiral = loc.clone().add(Math.cos(angle) * r, h * 0.5, Math.sin(angle) * r);
                    p.getWorld().spawnParticle(Particle.DUST, spiral, 3, 0.1, 0.1, 0.1, 0,
                            new Particle.DustOptions(Color.fromRGB(200 - h * 10, 200 - h * 10, 255), 1.5f));
                    if (h % 3 == 0) {
                        p.getWorld().spawnParticle(Particle.CLOUD, spiral, 1, 0.1, 0.1, 0.1, 0);
                    }
                }
                // Ground debris
                if (tick % 3 == 0) {
                    p.getWorld().spawnParticle(Particle.SWEEP_ATTACK, loc.clone().add(0, 0.5, 0), 2, 1.0, 0.1, 1.0, 0);
                }
                // Lightning flickers
                if (tick % 30 == 0) {
                    p.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, loc.clone().add(0, 4, 0), 15, 1.5, 2, 1.5, 0.2);
                    loc.getWorld().playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.5f);
                }
                // Damage + effects every second
                if (tick % 20 == 0) {
                    for (LivingEntity e : ctx.getNearbyEnemies(loc, 12.0, p)) {
                        e.damage(10.0, p);
                        e.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 25, 1));
                        e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 25, 2));
                        Vector pull = loc.toVector().subtract(e.getLocation().toVector()).normalize().multiply(1.2);
                        e.setVelocity(e.getVelocity().add(pull));
                    }
                    // Long range pull
                    for (LivingEntity e : ctx.getNearbyEnemies(loc, 20.0, p)) {
                        Vector pull = loc.toVector().subtract(e.getLocation().toVector()).normalize().multiply(0.5);
                        e.setVelocity(e.getVelocity().add(pull));
                    }
                    loc.getWorld().playSound(loc, Sound.ENTITY_BREEZE_SHOOT, 1.0f, 0.5f);
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2601 PHANTOM CYCLONE! The storm hunts!", TextColor.color(200, 200, 255)).decorate(TextDecoration.BOLD));
    }

    // ================================================================
    //  HELPER METHODS
    // ================================================================

    /** Spawn a horizontal dust ring at the given center and radius. */
    private void spawnDustRing(Location center, double radius, Color color, int count, World world) {
        double step = Math.PI * 2 / count;
        for (int i = 0; i < count; i++) {
            double angle = i * step;
            Location point = center.clone().add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
            world.spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0,
                    new Particle.DustOptions(color, 1.2f));
        }
    }

    /** Spawn a dust line between two points. */
    private void spawnDustLine(Location from, Location to, Color color, int points, World world) {
        Vector dir = to.toVector().subtract(from.toVector());
        double length = dir.length();
        dir.normalize();
        double step = length / points;
        for (int i = 0; i <= points; i++) {
            Location point = from.clone().add(dir.clone().multiply(i * step));
            world.spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0,
                    new Particle.DustOptions(color, 1.0f));
        }
    }

    /** Spawn a dust arc in front of the player. */
    private void spawnDustArc(Player p, double radius, Color color, int count) {
        Vector forward = p.getLocation().getDirection().normalize();
        double baseAngle = Math.atan2(forward.getZ(), forward.getX());
        double arcSpread = Math.PI * 0.6;
        double step = arcSpread / count;
        Location center = p.getLocation().add(0, 1.0, 0);
        for (int i = 0; i < count; i++) {
            double angle = baseAngle - arcSpread / 2 + i * step;
            Location point = center.clone().add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
            p.getWorld().spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0,
                    new Particle.DustOptions(color, 1.2f));
        }
    }

    /** Check if entity type is undead. */
    private boolean isUndead(LivingEntity e) {
        return e instanceof Zombie || e instanceof org.bukkit.entity.Skeleton
                || e instanceof org.bukkit.entity.Wither || e instanceof org.bukkit.entity.Phantom
                || e instanceof org.bukkit.entity.Drowned || e instanceof org.bukkit.entity.Stray
                || e instanceof org.bukkit.entity.Zoglin;
    }

    /** Check if a potion effect type is negative/debuff. */
    private boolean isNegativeEffect(PotionEffectType type) {
        return type.equals(PotionEffectType.POISON) || type.equals(PotionEffectType.WITHER)
                || type.equals(PotionEffectType.BLINDNESS) || type.equals(PotionEffectType.WEAKNESS)
                || type.equals(PotionEffectType.SLOWNESS) || type.equals(PotionEffectType.MINING_FATIGUE)
                || type.equals(PotionEffectType.NAUSEA) || type.equals(PotionEffectType.HUNGER)
                || type.equals(PotionEffectType.DARKNESS) || type.equals(PotionEffectType.LEVITATION);
    }
}
