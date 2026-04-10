package com.jglims.plugin.legendary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
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
 * LegendaryPrimaryAbilities - all 63 primary (right-click) ability implementations.
 * Rewritten with multi-stage visual effects, wind-ups, and tier-scaled spectacle.
 *
 * Particle budgets: COMMON 10, RARE 25, EPIC 50, MYTHIC 100, ABYSSAL 200.
 * Tier damage: COMMON 10-15, RARE 15-20, EPIC 20-30, MYTHIC 25-40, ABYSSAL 35-60.
 */
final class LegendaryPrimaryAbilities {

    private final LegendaryAbilityContext ctx;

    LegendaryPrimaryAbilities(LegendaryAbilityContext ctx) {
        this.ctx = ctx;
    }

    // =====================================================================
    //  COMMON TIER (20 weapons) - 10 particle budget, 10-15 damage
    // =====================================================================

    // #1 AMETHYST SHURIKEN: Shuriken Barrage - 5 purple spinning projectiles
    void rcAmethystShuriken(Player p) {
        Location origin = p.getEyeLocation();
        Vector baseDir = origin.getDirection().normalize();
        // Wind-up: crystalline charge
        p.playSound(origin, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.5f, 2.0f);
        spawnRing(origin, 0.5, 6, Particle.DUST, dustOpt(180, 80, 255, 1.0f), p);
        // Fire 5 shurikens in a fan
        for (int i = -2; i <= 2; i++) {
            double angle = Math.toRadians(i * 14);
            Vector dir = ctx.rotateY(baseDir.clone(), angle).normalize();
            final Vector fDir = dir;
            new BukkitRunnable() {
                Location loc = origin.clone();
                int ticks = 0;
                @Override public void run() {
                    if (ticks > 18) { cancel(); return; }
                    loc.add(fDir.clone().multiply(1.2));
                    // Spinning trail
                    double spin = ticks * 0.8;
                    Location sparkL = loc.clone().add(Math.sin(spin) * 0.3, Math.cos(spin) * 0.3, 0);
                    p.getWorld().spawnParticle(Particle.DUST, sparkL, 2, 0.05, 0.05, 0.05, 0,
                            dustOpt(200, 100, 255, 1.2f));
                    for (LivingEntity e : ctx.getNearbyEnemies(loc, 1.0, p)) {
                        ctx.dealDamage(p, e, 12.0);
                        e.getWorld().spawnParticle(Particle.CRIT, e.getLocation().add(0, 1, 0), 5, 0.2, 0.2, 0.2, 0.1);
                        p.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.8f, 1.8f);
                        cancel(); return;
                    }
                    ticks++;
                }
            }.runTaskTimer(ctx.plugin, 0L, 1L);
        }
        p.sendActionBar(Component.text("\u25C6 Shuriken Barrage!", NamedTextColor.LIGHT_PURPLE));
    }

    // #2 GRAVESCEPTER: Grave Rise - summon 2 undead servants from below
    void rcGravescepter(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1.5f, 0.5f);
        // Wind-up: ground cracks
        p.getWorld().spawnParticle(Particle.BLOCK, c, 8, 1.5, 0.2, 1.5, 0.1, Material.SOUL_SOIL.createBlockData());
        p.getWorld().spawnParticle(Particle.SOUL, c.clone().add(0, 0.3, 0), 6, 1, 0.2, 1, 0.02);
        for (int i = 0; i < 2; i++) {
            Location spawnLoc = c.clone().add(Math.random() * 3 - 1.5, 0, Math.random() * 3 - 1.5);
            // Rising particles before spawn
            final int idx = i;
            Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
                spawnLoc.getWorld().spawnParticle(Particle.SOUL, spawnLoc.clone().add(0, 0.5, 0), 5, 0.2, 0.5, 0.2, 0.03);
                Zombie zombie = (Zombie) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);
                zombie.customName(Component.text(p.getName() + "'s Servant", NamedTextColor.GRAY));
                zombie.setCustomNameVisible(true);
                zombie.setBaby(false);
                zombie.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
                zombie.setTarget(ctx.getTargetEntity(p, 15.0));
                ctx.trackSummon(p, zombie);
                Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> { if (!zombie.isDead()) zombie.remove(); }, 240L);
            }, (idx + 1) * 8L);
        }
        // AoE fear damage on nearby
        for (LivingEntity e : ctx.getNearbyEnemies(c, 4.0, p)) {
            ctx.dealDamage(p, e, 10.0);
        }
        p.sendActionBar(Component.text("\u2620 Grave Rise! Undead summoned!", NamedTextColor.DARK_GRAY));
    }

    // #3 LYCANBANE: Silver Strike - gleaming silver slash that purges buffs
    void rcLycanbane(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 5.0);
        if (target == null) { p.sendActionBar(Component.text("No target in range!", NamedTextColor.RED)); return; }
        // Wind-up: silver gleam on blade
        p.playSound(p.getLocation(), Sound.ENTITY_WOLF_AMBIENT, 1.0f, 1.5f);
        p.getWorld().spawnParticle(Particle.DUST, p.getLocation().add(0, 1.5, 0), 6, 0.2, 0.3, 0.2, 0,
                dustOpt(220, 220, 240, 1.5f));
        // Impact: silver burst + purge
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            ctx.dealDamage(p, target, 14.0);
            for (PotionEffect eff : target.getActivePotionEffects()) {
                if (eff.getType().equals(PotionEffectType.STRENGTH) || eff.getType().equals(PotionEffectType.SPEED)
                        || eff.getType().equals(PotionEffectType.REGENERATION) || eff.getType().equals(PotionEffectType.RESISTANCE)) {
                    target.removePotionEffect(eff.getType());
                }
            }
            target.getWorld().spawnParticle(Particle.END_ROD, target.getLocation().add(0, 1, 0), 8, 0.3, 0.5, 0.3, 0.05);
            p.playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.5f, 1.2f);
        }, 4L);
        p.sendActionBar(Component.text("\u2694 Silver Strike! Buffs purged!", NamedTextColor.WHITE));
    }

    // #4 GLOOMSTEEL KATANA: Quick Draw - instant dash + slash
    void rcGloomsteelKatana(Player p) {
        Location origin = p.getLocation();
        // Wind-up: blade gleam
        p.playSound(origin, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2.0f, 2.0f);
        p.getWorld().spawnParticle(Particle.DUST, origin.clone().add(0, 1, 0), 4, 0.1, 0.3, 0.1, 0,
                dustOpt(80, 80, 100, 1.0f));
        // Dash forward
        p.setVelocity(origin.getDirection().multiply(2.2));
        // Impact after dash
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            Location end = p.getLocation();
            p.getWorld().spawnParticle(Particle.SWEEP_ATTACK, end.clone().add(0, 1, 0), 3, 0.5, 0.3, 0.5, 0);
            p.getWorld().spawnParticle(Particle.SMOKE, origin, 6, 0.3, 0.5, 0.3, 0.02);
            p.playSound(end, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.8f);
            for (LivingEntity e : ctx.getNearbyEnemies(end, 2.5, p)) {
                ctx.dealDamage(p, e, 12.0);
            }
        }, 4L);
        p.sendActionBar(Component.text("\u26A1 Quick Draw!", NamedTextColor.DARK_GRAY));
    }

    // #5 VIRIDIAN CLEAVER: Verdant Slam - nature-infused ground pound
    void rcViridianCleaver(Player p) {
        Location c = p.getLocation();
        // Wind-up: vines coil around weapon
        p.playSound(c, Sound.BLOCK_GRASS_BREAK, 1.5f, 0.7f);
        p.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, c.clone().add(0, 1.5, 0), 5, 0.3, 0.3, 0.3, 0);
        // Impact: ground erupts with nature
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            p.playSound(c, Sound.ITEM_MACE_SMASH_GROUND, 1.5f, 0.9f);
            p.getWorld().spawnParticle(Particle.COMPOSTER, c, 8, 2.5, 0.5, 2.5, 0.1);
            p.getWorld().spawnParticle(Particle.BLOCK, c, 6, 2, 0.3, 2, 0.2, Material.MOSS_BLOCK.createBlockData());
            for (LivingEntity e : ctx.getNearbyEnemies(c, 5.0, p)) {
                ctx.dealDamage(p, e, 13.0);
                e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
            }
        }, 5L);
        p.sendActionBar(Component.text("\u2618 Verdant Slam!", NamedTextColor.GREEN));
    }

    // #6 CRESCENT EDGE: Lunar Cleave - silver crescent arc
    void rcCrescentEdge(Player p) {
        Location c = p.getLocation();
        Vector facing = c.getDirection().normalize();
        // Wind-up: crescent glow
        p.playSound(c, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.2f, 1.5f);
        // Arc sweep particles
        for (int i = -3; i <= 3; i++) {
            double angle = Math.toRadians(i * 25);
            Vector dir = ctx.rotateY(facing.clone(), angle).normalize().multiply(3.5);
            Location point = c.clone().add(dir).add(0, 1, 0);
            p.getWorld().spawnParticle(Particle.DUST, point, 2, 0.1, 0.1, 0.1, 0,
                    dustOpt(220, 220, 180, 1.3f));
        }
        p.playSound(c, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 1.0f);
        for (LivingEntity e : ctx.getNearbyEnemies(c, 6.0, p)) {
            Vector toE = e.getLocation().toVector().subtract(c.toVector()).normalize();
            if (facing.dot(toE) > 0) {
                ctx.dealDamage(p, e, 11.0);
                e.getWorld().spawnParticle(Particle.SWEEP_ATTACK, e.getLocation().add(0, 1, 0), 1, 0, 0, 0, 0);
            }
        }
        p.sendActionBar(Component.text("\u263D Lunar Cleave!", NamedTextColor.YELLOW));
    }

    // #7 GRAVECLEAVER: Bone Shatter - crushing blow that weakens armor
    void rcGravecleaver(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 5.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        // Wind-up: weapon trembles
        p.playSound(p.getLocation(), Sound.ENTITY_SKELETON_HURT, 1.0f, 0.6f);
        p.getWorld().spawnParticle(Particle.DUST, p.getLocation().add(0, 1.5, 0), 4, 0.2, 0.2, 0.2, 0,
                dustOpt(200, 200, 180, 1.0f));
        // Impact: bone fragments explode
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            ctx.dealDamage(p, target, 15.0);
            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 1));
            p.playSound(target.getLocation(), Sound.ENTITY_SKELETON_HURT, 2.0f, 0.4f);
            target.getWorld().spawnParticle(Particle.BLOCK, target.getLocation().add(0, 1, 0), 8, 0.3, 0.5, 0.3, 0.2,
                    Material.BONE_BLOCK.createBlockData());
            target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 5, 0.2, 0.3, 0.2, 0.1);
        }, 5L);
        p.sendActionBar(Component.text("\u2620 Bone Shatter! Armor weakened!", NamedTextColor.GRAY));
    }

    // #8 AMETHYST GREATBLADE: Crystal Burst - amethyst shards erupt upward
    void rcAmethystGreatblade(Player p) {
        Location c = p.getLocation();
        // Wind-up: crystal hum
        p.playSound(c, Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 1.5f, 1.2f);
        p.getWorld().spawnParticle(Particle.DUST, c.clone().add(0, 0.5, 0), 4, 0.5, 0.1, 0.5, 0,
                dustOpt(180, 80, 255, 1.0f));
        // Impact: crystal eruption
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            p.playSound(c, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 2.0f, 0.8f);
            p.getWorld().spawnParticle(Particle.END_ROD, c.clone().add(0, 1, 0), 8, 2, 2, 2, 0.08);
            for (LivingEntity e : ctx.getNearbyEnemies(c, 4.0, p)) {
                ctx.dealDamage(p, e, 11.0);
                e.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 30, 1));
                e.getWorld().spawnParticle(Particle.DUST, e.getLocation().add(0, 1, 0), 3, 0.2, 0.3, 0.2, 0,
                        dustOpt(200, 100, 255, 1.5f));
            }
        }, 4L);
        p.sendActionBar(Component.text("\u2666 Crystal Burst!", NamedTextColor.LIGHT_PURPLE));
    }

    // #9 FLAMBERGE: Flame Wave - rippling fire wave forward
    void rcFlamberge(Player p) {
        Vector dir = p.getLocation().getDirection().normalize();
        Location start = p.getLocation().add(0, 0.5, 0);
        p.playSound(start, Sound.ITEM_FIRECHARGE_USE, 1.5f, 1.0f);
        // Fire wave travels forward
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick > 8) { cancel(); return; }
                Location point = start.clone().add(dir.clone().multiply(tick));
                p.getWorld().spawnParticle(Particle.FLAME, point, 3, 0.4, 0.2, 0.4, 0.02);
                if (tick == 0) p.getWorld().spawnParticle(Particle.LAVA, point, 2, 0.2, 0.1, 0.2, 0);
                for (LivingEntity e : ctx.getNearbyEnemies(point, 1.5, p)) {
                    ctx.dealDamage(p, e, 12.0);
                    e.setFireTicks(60);
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2600 Flame Wave!", NamedTextColor.RED));
    }

    // #10 CRYSTAL FROSTBLADE: Frost Spike - ice lance projectile that freezes
    void rcCrystalFrostblade(Player p) {
        Vector dir = p.getLocation().getDirection().normalize();
        Location start = p.getEyeLocation();
        // Wind-up: frost crackle
        p.playSound(start, Sound.BLOCK_GLASS_BREAK, 1.2f, 1.8f);
        p.getWorld().spawnParticle(Particle.SNOWFLAKE, start, 4, 0.2, 0.2, 0.2, 0);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick > 10) { cancel(); return; }
                Location point = start.clone().add(dir.clone().multiply(tick * 1.2));
                p.getWorld().spawnParticle(Particle.SNOWFLAKE, point, 3, 0.1, 0.1, 0.1, 0);
                p.getWorld().spawnParticle(Particle.DUST, point, 2, 0.05, 0.05, 0.05, 0,
                        dustOpt(150, 220, 255, 1.2f));
                for (LivingEntity e : ctx.getNearbyEnemies(point, 1.3, p)) {
                    ctx.dealDamage(p, e, 12.0);
                    e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 2));
                    e.getWorld().spawnParticle(Particle.SNOWFLAKE, e.getLocation().add(0, 1, 0), 6, 0.3, 0.5, 0.3, 0);
                    p.playSound(e.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.5f);
                    cancel(); return;
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 2L, 1L);
        p.sendActionBar(Component.text("\u2744 Frost Spike!", NamedTextColor.AQUA));
    }

    // #11 DEMONSLAYER: Holy Rend - holy slash with bonus vs undead
    void rcDemonslayer(Player p) {
        Location c = p.getLocation();
        // Wind-up: golden glow
        p.playSound(c, Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 2.0f);
        p.getWorld().spawnParticle(Particle.DUST, c.clone().add(0, 1.5, 0), 5, 0.2, 0.3, 0.2, 0,
                dustOpt(255, 255, 150, 1.2f));
        // Impact
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            p.playSound(c, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.5f, 1.0f);
            p.getWorld().spawnParticle(Particle.END_ROD, c.clone().add(0, 1, 0), 6, 2, 1, 2, 0.05);
            for (LivingEntity e : ctx.getNearbyEnemies(c, 5.0, p)) {
                boolean undead = isUndeadType(e.getType());
                double dmg = undead ? 15.0 : 10.0;
                ctx.dealDamage(p, e, dmg);
                if (undead) {
                    e.getWorld().spawnParticle(Particle.DUST, e.getLocation().add(0, 1, 0), 4, 0.2, 0.3, 0.2, 0,
                            dustOpt(255, 255, 100, 2.0f));
                }
            }
        }, 4L);
        p.sendActionBar(Component.text("\u2694 Holy Rend! +50% vs Undead", NamedTextColor.YELLOW));
    }

    // #12 VENGEANCE: Retribution - store incoming damage for 8s, release as AoE
    void rcVengeance(Player p) {
        UUID uid = p.getUniqueId();
        ctx.retributionDamageStored.put(uid, 0.0);
        ctx.retributionExpiry.put(uid, System.currentTimeMillis() + 8000L);
        // Wind-up: vengeful aura
        p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.8f, 1.5f);
        p.getWorld().spawnParticle(Particle.DUST, p.getLocation().add(0, 1, 0), 6, 0.5, 0.8, 0.5, 0,
                dustOpt(200, 50, 50, 1.5f));
        // Pulsing indicator while active
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 40 || !ctx.retributionDamageStored.containsKey(uid)) { cancel(); return; }
                if (tick % 10 == 0) {
                    p.getWorld().spawnParticle(Particle.DUST, p.getLocation().add(0, 1, 0), 3, 0.4, 0.6, 0.4, 0,
                            dustOpt(255, 50, 50, 1.0f));
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 4L);
        // Release after 8s
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            double stored = ctx.retributionDamageStored.getOrDefault(uid, 0.0);
            double release = Math.max(10.0, stored * 1.5);
            ctx.retributionDamageStored.remove(uid);
            ctx.retributionExpiry.remove(uid);
            Location loc = p.getLocation();
            p.getWorld().spawnParticle(Particle.DUST, loc.clone().add(0, 1, 0), 10, 2, 1.5, 2, 0,
                    dustOpt(255, 0, 0, 2.5f));
            p.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 1.0f);
            for (LivingEntity e : ctx.getNearbyEnemies(loc, 6.0, p)) {
                ctx.dealDamage(p, e, release);
            }
            p.sendActionBar(Component.text("\u2694 Retribution released! " + String.format("%.0f", release) + " dmg!", NamedTextColor.RED).decorate(TextDecoration.BOLD));
        }, 160L);
        p.sendActionBar(Component.text("\u2694 Retribution active! Absorbing damage...", NamedTextColor.RED));
    }

    // #13 OCULUS: All-Seeing Strike - teleport behind target and strike
    void rcOculus(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 10.0);
        if (target == null) { p.sendActionBar(Component.text("No target within 10 blocks!", NamedTextColor.RED)); return; }
        Location origin = p.getLocation();
        // Wind-up: eye opens
        p.getWorld().spawnParticle(Particle.DUST, p.getEyeLocation().add(0, 0.5, 0), 4, 0.1, 0.1, 0.1, 0,
                dustOpt(200, 100, 255, 1.5f));
        p.playSound(origin, Sound.ENTITY_ENDERMAN_TELEPORT, 1.2f, 1.5f);
        // Teleport behind target
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            p.getWorld().spawnParticle(Particle.REVERSE_PORTAL, origin, 8, 0.3, 0.8, 0.3, 0.05);
            Location behind = target.getLocation().subtract(target.getLocation().getDirection().normalize());
            behind.setYaw(target.getLocation().getYaw());
            p.teleport(behind);
            ctx.dealDamage(p, target, 13.0);
            p.getWorld().spawnParticle(Particle.REVERSE_PORTAL, p.getLocation(), 6, 0.3, 0.8, 0.3, 0.05);
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.5f, 1.2f);
        }, 3L);
        p.sendActionBar(Component.text("\u2609 All-Seeing Strike!", NamedTextColor.LIGHT_PURPLE));
    }

    // #14 ANCIENT GREATSLAB: Seismic Slam - massive stone impact launches enemies
    void rcAncientGreatslab(Player p) {
        Location c = p.getLocation();
        // Wind-up: raise slab
        p.playSound(c, Sound.BLOCK_STONE_BREAK, 1.5f, 0.6f);
        p.getWorld().spawnParticle(Particle.DUST, c.clone().add(0, 2, 0), 4, 0.3, 0.3, 0.3, 0,
                dustOpt(130, 130, 130, 2.0f));
        // Impact: seismic wave
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            p.playSound(c, Sound.ITEM_MACE_SMASH_GROUND, 2.0f, 0.7f);
            p.getWorld().spawnParticle(Particle.BLOCK, c, 8, 3, 0.5, 3, 0.3, Material.STONE.createBlockData());
            for (LivingEntity e : ctx.getNearbyEnemies(c, 6.0, p)) {
                ctx.dealDamage(p, e, 13.0);
                e.setVelocity(new Vector(0, 1.0, 0));
            }
        }, 6L);
        p.sendActionBar(Component.text("\u2694 Seismic Slam!", NamedTextColor.GRAY));
    }

    // #15 NEPTUNE'S FANG: Riptide Slash - piercing water lance
    void rcNeptunesFang(Player p) {
        Vector dir = p.getLocation().getDirection().normalize();
        Location start = p.getEyeLocation();
        p.playSound(start, Sound.ITEM_TRIDENT_RIPTIDE_3, 1.2f, 1.2f);
        // Water lance pierces through
        new BukkitRunnable() {
            int tick = 0;
            int hits = 0;
            @Override public void run() {
                if (tick > 12 || hits >= 4) { cancel(); return; }
                Location point = start.clone().add(dir.clone().multiply(tick));
                p.getWorld().spawnParticle(Particle.SPLASH, point, 3, 0.15, 0.15, 0.15, 0);
                p.getWorld().spawnParticle(Particle.DUST, point, 2, 0.1, 0.1, 0.1, 0,
                        dustOpt(50, 150, 220, 1.0f));
                for (LivingEntity e : ctx.getNearbyEnemies(point, 1.3, p)) {
                    ctx.dealDamage(p, e, 10.0);
                    e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
                    hits++;
                    if (hits >= 4) { cancel(); return; }
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2263 Riptide Slash!", NamedTextColor.AQUA));
    }

    // #16 TIDECALLER: Tidal Spear - water burst + conduit buff
    void rcTidecaller(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 8.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        // Wind-up: water swirls around weapon
        p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_SPLASH, 1.0f, 1.2f);
        p.getWorld().spawnParticle(Particle.SPLASH, p.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0);
        // Impact
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            ctx.dealDamage(p, target, 12.0);
            Vector kb = target.getLocation().toVector().subtract(p.getLocation().toVector()).normalize().multiply(2.0).setY(0.5);
            target.setVelocity(kb);
            target.getWorld().spawnParticle(Particle.SPLASH, target.getLocation(), 8, 0.5, 0.5, 0.5, 0.1);
            p.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, 200, 0));
            p.playSound(target.getLocation(), Sound.ITEM_TRIDENT_THROW, 1.5f, 0.8f);
        }, 4L);
        p.sendActionBar(Component.text("\u2263 Tidal Spear! + Conduit Power", NamedTextColor.AQUA));
    }

    // #17 STORMFORK: Lightning Javelin - summon lightning on target
    void rcStormfork(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 10.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        // Wind-up: static charge
        p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 2.0f);
        p.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, p.getLocation().add(0, 1.5, 0), 5, 0.3, 0.3, 0.3, 0.05);
        // Impact: lightning strike
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            p.getWorld().strikeLightningEffect(target.getLocation());
            ctx.dealDamage(p, target, 14.0);
            p.playSound(target.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 1.0f);
            for (LivingEntity e : ctx.getNearbyEnemies(target.getLocation(), 3.0, p)) {
                if (e != target) ctx.dealDamage(p, e, 7.0);
            }
        }, 5L);
        p.sendActionBar(Component.text("\u26A1 Lightning Javelin!", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
    }

    // #18 JADE REAPER: Jade Crescent - wide emerald sweep
    void rcJadeReaper(Player p) {
        Location c = p.getLocation();
        Vector facing = c.getDirection().normalize();
        // Wind-up: jade glow
        p.playSound(c, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 0.8f);
        p.getWorld().spawnParticle(Particle.DUST, c.clone().add(0, 1, 0), 4, 0.2, 0.3, 0.2, 0,
                dustOpt(80, 200, 80, 1.2f));
        // Sweep
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            p.playSound(c, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 0.8f);
            // Arc particles
            for (int i = -4; i <= 4; i++) {
                double angle = Math.toRadians(i * 20);
                Vector dir = ctx.rotateY(facing.clone(), angle).normalize().multiply(4);
                Location point = c.clone().add(dir).add(0, 1, 0);
                p.getWorld().spawnParticle(Particle.COMPOSTER, point, 1, 0.1, 0.1, 0.1, 0);
            }
            for (LivingEntity e : ctx.getNearbyEnemies(c, 7.0, p)) {
                Vector toE = e.getLocation().toVector().subtract(c.toVector()).normalize();
                if (facing.dot(toE) > 0) {
                    ctx.dealDamage(p, e, 11.0);
                    e.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0));
                }
            }
        }, 3L);
        p.sendActionBar(Component.text("\u2618 Jade Crescent!", NamedTextColor.GREEN));
    }

    // #19 VINDICATOR: Executioner's Chop - bonus damage based on missing health
    void rcVindicator(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 5.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        // Wind-up: raise axe
        p.playSound(p.getLocation(), Sound.ENTITY_VINDICATOR_AMBIENT, 1.0f, 0.8f);
        p.getWorld().spawnParticle(Particle.DUST, p.getLocation().add(0, 2, 0), 3, 0.1, 0.2, 0.1, 0,
                dustOpt(180, 40, 40, 1.5f));
        // Execute
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            double maxHp = 20.0;
            if (target.getAttribute(Attribute.MAX_HEALTH) != null) maxHp = target.getAttribute(Attribute.MAX_HEALTH).getValue();
            double missing = maxHp - target.getHealth();
            double bonus = Math.min(5.0, missing / 4.0);
            double totalDmg = 10.0 + bonus;
            ctx.dealDamage(p, target, totalDmg);
            p.playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 2.0f, 0.8f);
            target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 8, 0.3, 0.3, 0.3, 0.2);
            target.getWorld().spawnParticle(Particle.DUST, target.getLocation().add(0, 1, 0), 3, 0.2, 0.3, 0.2, 0,
                    dustOpt(200, 0, 0, 2.0f));
        }, 5L);
        p.sendActionBar(Component.text("\u2694 Executioner's Chop! +" + "bonus dmg!", NamedTextColor.DARK_RED));
    }

    // #20 SPIDER FANG: Web Trap - web projectile that roots and poisons
    void rcSpiderFang(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 8.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        // Wind-up: spider hiss
        p.playSound(p.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 1.5f, 0.5f);
        // Web projectile
        Vector dir = target.getLocation().add(0, 1, 0).toVector().subtract(p.getEyeLocation().toVector()).normalize();
        new BukkitRunnable() {
            Location loc = p.getEyeLocation().clone();
            int ticks = 0;
            @Override public void run() {
                if (ticks > 15) { cancel(); return; }
                loc.add(dir.clone().multiply(1.5));
                p.getWorld().spawnParticle(Particle.BLOCK, loc, 2, 0.1, 0.1, 0.1, 0, Material.COBWEB.createBlockData());
                for (LivingEntity e : ctx.getNearbyEnemies(loc, 1.5, p)) {
                    e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 127));
                    e.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 1));
                    ctx.dealDamage(p, e, 10.0);
                    e.getWorld().spawnParticle(Particle.BLOCK, e.getLocation().add(0, 1, 0), 8, 0.3, 0.5, 0.3, 0.1,
                            Material.COBWEB.createBlockData());
                    p.playSound(e.getLocation(), Sound.ENTITY_SPIDER_HURT, 1.0f, 0.8f);
                    cancel(); return;
                }
                ticks++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2620 Web Trap! Root + Poison!", NamedTextColor.DARK_GREEN));
    }

    // =====================================================================
    //  RARE TIER (8 weapons) - 25 particle budget, 15-20 damage
    // =====================================================================

    // #21 OCEAN'S RAGE: Stormbringer - tidal wave AoE with lightning
    void rcOceansRage(Player p) {
        Location c = p.getLocation();
        // Wind-up: storm clouds gather
        p.playSound(c, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8f, 1.5f);
        p.getWorld().spawnParticle(Particle.DUST, c.clone().add(0, 3, 0), 8, 2, 0.5, 2, 0,
                dustOpt(40, 40, 80, 2.0f));
        // Impact: tidal crash + lightning
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            p.playSound(c, Sound.ENTITY_GENERIC_SPLASH, 2.0f, 0.6f);
            p.getWorld().spawnParticle(Particle.SPLASH, c.clone().add(0, 1, 0), 15, 3, 2, 3, 0.5);
            p.getWorld().spawnParticle(Particle.BUBBLE, c.clone().add(0, 1, 0), 10, 3, 1, 3, 0.2);
            p.getWorld().strikeLightningEffect(c.clone().add(2, 0, 2));
            for (LivingEntity e : ctx.getNearbyEnemies(c, 7.0, p)) {
                ctx.dealDamage(p, e, 17.0);
                Vector kb = e.getLocation().toVector().subtract(c.toVector()).normalize().multiply(1.5).setY(0.6);
                e.setVelocity(kb);
                e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
            }
        }, 8L);
        p.sendActionBar(Component.text("\u2248 Stormbringer! \u2248", NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
    }

    // #22 AQUATIC SACRED BLADE: Aqua Heal - healing waters + conduit
    void rcAquaticSacredBlade(Player p) {
        // Wind-up: water spirals upward
        p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.5f);
        spawnRing(p.getLocation().add(0, 0.5, 0), 1.5, 8, Particle.SPLASH, null, p);
        // Heal phase
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            double maxHp = p.getAttribute(Attribute.MAX_HEALTH).getValue();
            p.setHealth(Math.min(maxHp, p.getHealth() + 12.0));
            p.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, 600, 0));
            p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.5f, 1.2f);
            p.getWorld().spawnParticle(Particle.HEART, p.getLocation().add(0, 2, 0), 6, 0.5, 0.5, 0.5, 0);
            p.getWorld().spawnParticle(Particle.DUST, p.getLocation().add(0, 1, 0), 15, 1, 1, 1, 0,
                    dustOpt(80, 180, 255, 1.5f));
            // Also damage nearby
            for (LivingEntity e : ctx.getNearbyEnemies(p.getLocation(), 5.0, p)) {
                ctx.dealDamage(p, e, 15.0);
            }
        }, 6L);
        p.sendActionBar(Component.text("\u2764 Aqua Heal! +6 hearts + Conduit Power", NamedTextColor.AQUA));
    }

    // #23 ROYAL CHAKRAM: Chakram Throw - bouncing golden disc
    void rcRoyalChakram(Player p) {
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 1.5f);
        List<LivingEntity> targets = ctx.getNearbyEnemies(p.getLocation(), 12.0, p);
        int maxBounces = Math.min(5, targets.size());
        if (maxBounces == 0) { p.sendActionBar(Component.text("No targets!", NamedTextColor.RED)); return; }
        // Chain bounce through enemies
        for (int i = 0; i < maxBounces; i++) {
            LivingEntity t = targets.get(i);
            final int delay = i * 6;
            final int bounceNum = i;
            Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
                if (t.isDead()) return;
                ctx.dealDamage(p, t, 16.0);
                // Golden disc impact
                t.getWorld().spawnParticle(Particle.DUST, t.getLocation().add(0, 1, 0), 8, 0.3, 0.3, 0.3, 0,
                        dustOpt(255, 215, 0, 1.5f));
                t.getWorld().spawnParticle(Particle.CRIT, t.getLocation().add(0, 1, 0), 5, 0.2, 0.2, 0.2, 0.1);
                p.playSound(t.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.5f + (bounceNum * 0.1f));
            }, delay);
        }
        p.sendActionBar(Component.text("\u25C6 Chakram Throw! " + maxBounces + " bounces!", NamedTextColor.GOLD));
    }

    // #24 ACIDIC CLEAVER: Acid Splash - corrosive cone with lingering poison
    void rcAcidicCleaver(Player p) {
        Location c = p.getLocation().add(p.getLocation().getDirection().multiply(3));
        // Wind-up: acid drips
        p.playSound(c, Sound.ENTITY_LLAMA_SPIT, 1.2f, 0.5f);
        p.getWorld().spawnParticle(Particle.DUST, p.getLocation().add(0, 1, 0), 5, 0.2, 0.3, 0.2, 0,
                dustOpt(100, 200, 0, 1.0f));
        // Splash
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            p.playSound(c, Sound.ENTITY_LLAMA_SPIT, 1.5f, 0.3f);
            p.getWorld().spawnParticle(Particle.ITEM_SLIME, c, 15, 2.5, 1, 2.5, 0.1);
            p.getWorld().spawnParticle(Particle.DUST, c, 10, 2, 1, 2, 0,
                    dustOpt(120, 220, 0, 1.5f));
            for (LivingEntity e : ctx.getNearbyEnemies(c, 5.0, p)) {
                ctx.dealDamage(p, e, 16.0);
                e.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 160, 2));
                e.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));
            }
        }, 4L);
        p.sendActionBar(Component.text("\u2620 Acid Splash! Poison III!", NamedTextColor.GREEN));
    }

    // #25 MURAMASA: Crimson Flash - blood-red dash through enemies
    void rcMuramasa(Player p) {
        Location origin = p.getLocation();
        // Wind-up: blade glows red
        p.playSound(origin, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 2.0f);
        p.getWorld().spawnParticle(Particle.DUST, origin.clone().add(0, 1, 0), 8, 0.2, 0.3, 0.2, 0,
                dustOpt(200, 0, 30, 1.2f));
        // Dash
        p.setVelocity(origin.getDirection().multiply(2.8));
        Set<UUID> hitSet = new HashSet<>();
        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (ticks >= 10) { cancel(); return; }
                Location loc = p.getLocation();
                p.getWorld().spawnParticle(Particle.DUST, loc.clone().add(0, 1, 0), 6, 0.4, 0.4, 0.4, 0,
                        dustOpt(200, 0, 0, 1.5f));
                p.getWorld().spawnParticle(Particle.SWEEP_ATTACK, loc.clone().add(0, 1, 0), 1, 0, 0, 0, 0);
                for (LivingEntity e : ctx.getNearbyEnemies(loc, 2.5, p)) {
                    if (hitSet.add(e.getUniqueId())) {
                        ctx.dealDamage(p, e, 18.0);
                        e.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, e.getLocation().add(0, 1, 0), 5, 0.2, 0.3, 0.2, 0);
                    }
                }
                ticks++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u26A1 Crimson Flash!", NamedTextColor.RED).decorate(TextDecoration.BOLD));
    }

    // #26 WINDREAPER: Gale Slash - cyclone blast that scatters enemies
    void rcWindreaper(Player p) {
        Location c = p.getLocation();
        // Wind-up: air swirls
        p.playSound(c, Sound.ENTITY_BREEZE_INHALE, 1.5f, 1.0f);
        spawnRing(c.clone().add(0, 1, 0), 1.0, 6, Particle.CLOUD, null, p);
        // Blast
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            Location target = c.clone().add(c.getDirection().multiply(4));
            p.playSound(target, Sound.ENTITY_BREEZE_SHOOT, 2.0f, 0.8f);
            p.getWorld().spawnParticle(Particle.CLOUD, target, 15, 3, 2, 3, 0.15);
            p.getWorld().spawnParticle(Particle.SWEEP_ATTACK, target, 5, 2, 1, 2, 0);
            for (LivingEntity e : ctx.getNearbyEnemies(target, 8.0, p)) {
                ctx.dealDamage(p, e, 16.0);
                Vector kb = e.getLocation().toVector().subtract(p.getLocation().toVector()).normalize().multiply(2.5).setY(0.8);
                e.setVelocity(kb);
            }
        }, 6L);
        p.sendActionBar(Component.text("\u2601 Gale Slash!", NamedTextColor.WHITE));
    }

    // #27 MOONLIGHT: Lunar Beam - radiant silver beam
    void rcMoonlight(Player p) {
        Vector dir = p.getLocation().getDirection().normalize();
        Location start = p.getEyeLocation();
        // Wind-up: moon glow
        p.playSound(start, Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1.8f);
        p.getWorld().spawnParticle(Particle.DUST, start, 6, 0.2, 0.2, 0.2, 0,
                dustOpt(200, 200, 255, 1.5f));
        // Beam phase
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick > 15) { cancel(); return; }
                Location point = start.clone().add(dir.clone().multiply(tick));
                p.getWorld().spawnParticle(Particle.END_ROD, point, 4, 0.1, 0.1, 0.1, 0);
                p.getWorld().spawnParticle(Particle.DUST, point, 3, 0.15, 0.15, 0.15, 0,
                        dustOpt(180, 180, 255, 1.3f));
                for (LivingEntity e : ctx.getNearbyEnemies(point, 1.5, p)) {
                    ctx.dealDamage(p, e, 18.0);
                    e.getWorld().spawnParticle(Particle.ENCHANT, e.getLocation().add(0, 1, 0), 8, 0.3, 0.5, 0.3, 0.1);
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 3L, 1L);
        p.sendActionBar(Component.text("\u263D Lunar Beam!", NamedTextColor.YELLOW));
    }

    // #28 TALONBRAND: Talon Strike - triple raptor combo
    void rcTalonbrand(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 5.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        // Wind-up: predatory screech
        p.playSound(p.getLocation(), Sound.ENTITY_PARROT_IMITATE_PHANTOM, 1.0f, 1.5f);
        p.getWorld().spawnParticle(Particle.DUST, p.getLocation().add(0, 1.5, 0), 5, 0.2, 0.2, 0.2, 0,
                dustOpt(180, 50, 50, 1.0f));
        // 3-hit combo with escalating damage
        for (int i = 0; i < 3; i++) {
            final double dmg = 5.0 + i * 2.0; // 5, 7, 9 = 21 total
            final int idx = i;
            Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
                if (target.isDead()) return;
                ctx.dealDamage(p, target, dmg);
                target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 5, 0.2, 0.2, 0.2, 0.1);
                target.getWorld().spawnParticle(Particle.DUST, target.getLocation().add(0, 1, 0), 3, 0.2, 0.3, 0.2, 0,
                        dustOpt(200, 50 + idx * 30, 50, 1.2f));
                p.playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.5f + (idx * 0.2f));
            }, i * 5L);
        }
        p.sendActionBar(Component.text("\u2694 Talon Strike! x3 combo!", NamedTextColor.DARK_RED));
    }

    // =====================================================================
    //  EPIC TIER (7 weapons) - 50 particle budget, 20-30 damage
    // =====================================================================

    // #29 BERSERKER'S GREATAXE: Berserker Slam - devastating ground pound with shockwave
    void rcBerserkersGreataxe(Player p) {
        Location c = p.getLocation();
        // Wind-up: berserker roar + charge
        p.playSound(c, Sound.ENTITY_RAVAGER_ROAR, 1.5f, 1.2f);
        p.getWorld().spawnParticle(Particle.DUST, c.clone().add(0, 2, 0), 10, 0.4, 0.5, 0.4, 0,
                dustOpt(200, 50, 30, 2.0f));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 10, 2)); // brief pause
        // Impact: massive slam
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            p.playSound(c, Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.7f);
            // Expanding shockwave ring
            for (int ring = 0; ring < 3; ring++) {
                final double radius = 2.0 + ring * 2.5;
                Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
                    spawnRing(c, radius, 12, Particle.DUST, dustOpt(200, 80, 30, 2.0f), p);
                    c.getWorld().spawnParticle(Particle.BLOCK, c, 8, radius, 0.3, radius, 0.2, Material.NETHERRACK.createBlockData());
                }, ring * 2L);
            }
            p.getWorld().spawnParticle(Particle.EXPLOSION, c, 2, 1, 0.5, 1, 0);
            for (LivingEntity e : ctx.getNearbyEnemies(c, 8.0, p)) {
                ctx.dealDamage(p, e, 25.0);
                e.setVelocity(e.getLocation().toVector().subtract(c.toVector()).normalize().multiply(1.5).setY(1.2));
            }
        }, 10L);
        p.sendActionBar(Component.text("\u2620 BERSERKER SLAM! \u2620", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
    }

    // #30 BLACK IRON GREATSWORD: Dark Slash - shadow beam that cuts through everything
    void rcBlackIronGreatsword(Player p) {
        Vector dir = p.getLocation().getDirection().normalize();
        Location start = p.getEyeLocation();
        // Wind-up: darkness gathers
        p.playSound(start, Sound.ENTITY_WARDEN_HEARTBEAT, 1.5f, 0.8f);
        p.getWorld().spawnParticle(Particle.DUST, start, 8, 0.3, 0.3, 0.3, 0,
                dustOpt(20, 0, 40, 2.0f));
        // Dark slash beam
        new BukkitRunnable() {
            int tick = 0;
            Set<UUID> hitSet = new HashSet<>();
            @Override public void run() {
                if (tick > 12) { cancel(); return; }
                Location point = start.clone().add(dir.clone().multiply(tick));
                p.getWorld().spawnParticle(Particle.SOUL, point, 5, 0.2, 0.2, 0.2, 0.02);
                p.getWorld().spawnParticle(Particle.DUST, point, 6, 0.3, 0.3, 0.3, 0,
                        dustOpt(30, 0, 50, 2.0f));
                p.getWorld().spawnParticle(Particle.SMOKE, point, 3, 0.1, 0.1, 0.1, 0.01);
                for (LivingEntity e : ctx.getNearbyEnemies(point, 1.8, p)) {
                    if (hitSet.add(e.getUniqueId())) {
                        ctx.dealDamage(p, e, 22.0);
                        e.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 30, 0));
                    }
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 5L, 1L);
        p.playSound(start, Sound.ENTITY_WARDEN_SONIC_BOOM, 0.8f, 1.5f);
        p.sendActionBar(Component.text("\u2694 Dark Slash!", NamedTextColor.DARK_GRAY).decorate(TextDecoration.BOLD));
    }

    // #31 SOLSTICE: Solar Flare - blinding sun explosion
    void rcSolstice(Player p) {
        Location c = p.getLocation();
        // Wind-up: sun rises overhead
        p.playSound(c, Sound.BLOCK_BEACON_ACTIVATE, 1.5f, 1.0f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 10) { cancel(); return; }
                double y = 1.0 + tick * 0.3;
                p.getWorld().spawnParticle(Particle.DUST, c.clone().add(0, y, 0), 5, 0.3, 0.3, 0.3, 0,
                        dustOpt(255, 200, 50, 1.5f + tick * 0.1f));
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        // Explosion
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            p.playSound(c, Sound.ENTITY_BLAZE_SHOOT, 2.0f, 0.6f);
            p.getWorld().spawnParticle(Particle.FLAME, c.clone().add(0, 2, 0), 30, 5, 3, 5, 0.1);
            p.getWorld().spawnParticle(Particle.DUST, c.clone().add(0, 2, 0), 20, 4, 2, 4, 0,
                    dustOpt(255, 255, 100, 3.0f));
            for (LivingEntity e : ctx.getNearbyEnemies(c, 8.0, p)) {
                ctx.dealDamage(p, e, 22.0);
                e.setFireTicks(80);
                if (e instanceof Player) e.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
            }
        }, 12L);
        p.sendActionBar(Component.text("\u2600 Solar Flare!", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // #32 GRAND CLAYMORE: Titan Swing - massive 180-arc with shockwave
    void rcGrandClaymore(Player p) {
        Location c = p.getLocation();
        Vector facing = c.getDirection().normalize();
        // Wind-up: heavy weapon windup
        p.playSound(c, Sound.ENTITY_IRON_GOLEM_ATTACK, 1.5f, 0.7f);
        p.getWorld().spawnParticle(Particle.DUST, c.clone().add(0, 1.5, 0), 8, 0.4, 0.4, 0.4, 0,
                dustOpt(200, 200, 220, 2.0f));
        // Swing
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            p.playSound(c, Sound.ITEM_MACE_SMASH_GROUND, 2.0f, 0.9f);
            // Sweep arc particles
            for (int i = -5; i <= 5; i++) {
                double angle = Math.toRadians(i * 16);
                Vector dir = ctx.rotateY(facing.clone(), angle).normalize().multiply(5);
                Location point = c.clone().add(dir).add(0, 1, 0);
                p.getWorld().spawnParticle(Particle.SWEEP_ATTACK, point, 2, 0.2, 0.2, 0.2, 0);
                p.getWorld().spawnParticle(Particle.DUST, point, 2, 0.1, 0.1, 0.1, 0,
                        dustOpt(200, 200, 220, 1.5f));
            }
            for (LivingEntity e : ctx.getNearbyEnemies(c, 10.0, p)) {
                Vector toEnemy = e.getLocation().toVector().subtract(c.toVector()).normalize();
                if (facing.dot(toEnemy) > 0) {
                    ctx.dealDamage(p, e, 25.0);
                    e.setVelocity(toEnemy.multiply(2.5).setY(0.6));
                }
            }
        }, 8L);
        p.sendActionBar(Component.text("\u2694 TITAN SWING! \u2694", NamedTextColor.WHITE).decorate(TextDecoration.BOLD));
    }

    // #33 CALAMITY BLADE: Cataclysm - earth-shattering AoE with quake effect
    void rcCalamityBlade(Player p) {
        Location c = p.getLocation();
        // Wind-up: ominous rumble
        p.playSound(c, Sound.ENTITY_WARDEN_EMERGE, 1.2f, 0.8f);
        p.getWorld().spawnParticle(Particle.DUST, c.clone().add(0, 1, 0), 10, 0.5, 0.8, 0.5, 0,
                dustOpt(80, 0, 0, 2.0f));
        // Cataclysm
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            p.playSound(c, Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.5f);
            p.getWorld().spawnParticle(Particle.BLOCK, c, 20, 3, 1, 3, 0.5, Material.STONE.createBlockData());
            p.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, c.clone().add(0, 1, 0), 15, 3, 2, 3, 0.03);
            p.getWorld().spawnParticle(Particle.DUST, c, 15, 3, 1, 3, 0,
                    dustOpt(120, 20, 20, 2.5f));
            for (LivingEntity e : ctx.getNearbyEnemies(c, 7.0, p)) {
                ctx.dealDamage(p, e, 24.0);
                e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 2));
                e.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 60, 1));
            }
        }, 10L);
        p.sendActionBar(Component.text("\u2622 Cataclysm!", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
    }

    // #34 EMERALD GREATCLEAVER: Emerald Storm - emerald shards rain down
    void rcEmeraldGreatcleaver(Player p) {
        Location c = p.getLocation();
        // Wind-up: gems charge
        p.playSound(c, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 1.5f, 0.8f);
        spawnRing(c.clone().add(0, 3, 0), 2.0, 8, Particle.DUST, dustOpt(50, 200, 80, 1.5f), p);
        // Storm
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            p.playSound(c, Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 2.0f, 0.6f);
            p.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, c.clone().add(0, 1, 0), 20, 3, 2, 3, 0);
            p.getWorld().spawnParticle(Particle.DUST, c.clone().add(0, 2, 0), 25, 3, 2, 3, 0,
                    dustOpt(50, 220, 80, 2.0f));
            p.getWorld().spawnParticle(Particle.COMPOSTER, c, 10, 3, 1, 3, 0.1);
            for (LivingEntity e : ctx.getNearbyEnemies(c, 7.0, p)) {
                ctx.dealDamage(p, e, 23.0);
                e.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 1));
            }
        }, 8L);
        p.sendActionBar(Component.text("\u2666 Emerald Storm!", NamedTextColor.GREEN).decorate(TextDecoration.BOLD));
    }

    // #35 DEMON'S BLOOD BLADE: Blood Rite - sacrifice health for devastating damage
    void rcDemonsBloodBlade(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 5.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        if (p.getHealth() <= 6.0) { p.sendActionBar(Component.text("Not enough health!", NamedTextColor.RED)); return; }
        // Wind-up: blood sacrifice
        p.playSound(p.getLocation(), Sound.ENTITY_WITHER_HURT, 1.0f, 0.5f);
        p.getWorld().spawnParticle(Particle.DUST, p.getLocation().add(0, 1, 0), 10, 0.5, 0.8, 0.5, 0,
                dustOpt(139, 0, 0, 2.0f));
        // Sacrifice and strike
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            p.setHealth(p.getHealth() - 6.0);
            ctx.dealDamage(p, target, 30.0);
            p.playSound(target.getLocation(), Sound.ENTITY_WITHER_HURT, 2.0f, 0.3f);
            target.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, target.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.1);
            target.getWorld().spawnParticle(Particle.DUST, target.getLocation().add(0, 1, 0), 20, 0.5, 0.8, 0.5, 0,
                    dustOpt(180, 0, 0, 2.5f));
            // Blood splatter around
            spawnRing(target.getLocation().add(0, 0.5, 0), 1.5, 8, Particle.DUST, dustOpt(139, 0, 0, 1.5f), p);
        }, 6L);
        p.sendActionBar(Component.text("\u2620 Blood Rite! -3 hearts \u2192 30 damage!", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
    }

    // =====================================================================
    //  MYTHIC TIER (24 weapons) - 100 particle budget, 25-40 damage
    // =====================================================================

    // #36 TRUE EXCALIBUR: Holy Smite - heavenly lightning + radiant burst
    void rcTrueExcalibur(Player p) {
        Location c = p.getLocation();
        // Wind-up: holy aura builds
        p.playSound(c, Sound.BLOCK_BEACON_ACTIVATE, 1.5f, 1.0f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 15) { cancel(); return; }
                double radius = 0.5 + tick * 0.15;
                spawnRing(p.getLocation().add(0, 0.5 + tick * 0.15, 0), radius, 6, Particle.END_ROD, null, p);
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        // Smite
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            Location target = c.clone().add(c.getDirection().multiply(4));
            p.getWorld().strikeLightningEffect(target);
            p.playSound(target, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 1.0f);
            p.getWorld().spawnParticle(Particle.END_ROD, target.clone().add(0, 2, 0), 40, 2, 3, 2, 0.1);
            p.getWorld().spawnParticle(Particle.DUST, target.clone().add(0, 1, 0), 30, 3, 2, 3, 0,
                    dustOpt(255, 255, 200, 2.5f));
            p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, target, 30, 2, 2, 2, 0.3);
            for (LivingEntity e : ctx.getNearbyEnemies(target, 6.0, p)) {
                ctx.dealDamage(p, e, 30.0);
            }
        }, 15L);
        p.sendActionBar(Component.text("\u2694 HOLY SMITE! \u2694", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
    }

    // #37 REQUIEM OF NINTH ABYSS: Soul Devour - drain life from target with soul vortex
    void rcRequiemNinthAbyss(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 6.0);
        if (target == null) { p.sendActionBar(Component.text("No target in range!", NamedTextColor.RED)); return; }
        // Wind-up: dark vortex
        p.playSound(p.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 1.5f, 0.3f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 12 || target.isDead()) { cancel(); return; }
                double angle = tick * 0.6;
                double r = 2.0 - tick * 0.15;
                Location orbit = target.getLocation().add(Math.cos(angle) * r, 1 + tick * 0.1, Math.sin(angle) * r);
                p.getWorld().spawnParticle(Particle.SOUL, orbit, 3, 0.05, 0.05, 0.05, 0.01);
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        // Devour
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            if (target.isDead()) return;
            ctx.dealDamage(p, target, 28.0);
            double maxHp = p.getAttribute(Attribute.MAX_HEALTH).getValue();
            p.setHealth(Math.min(maxHp, p.getHealth() + 14.0));
            p.playSound(target.getLocation(), Sound.ENTITY_WITHER_HURT, 1.5f, 0.5f);
            target.getWorld().spawnParticle(Particle.SOUL, target.getLocation().add(0, 1, 0), 30, 0.5, 0.8, 0.5, 0.08);
            target.getWorld().spawnParticle(Particle.SCULK_CHARGE_POP, target.getLocation().add(0, 1, 0), 20, 0.4, 0.5, 0.4, 0.03);
            // Soul trail from target to player
            drawParticleLine(target.getLocation().add(0, 1, 0), p.getLocation().add(0, 1, 0), Particle.SOUL, 3, 0.1, p);
        }, 14L);
        p.sendActionBar(Component.text("\u2620 Soul Devoured! +7 hearts!", NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
    }

    // #38 PHOENIX'S GRACE: Phoenix Strike - fiery rebirth slam
    void rcPhoenixsGrace(Player p) {
        Location c = p.getLocation();
        // Wind-up: phoenix wings unfurl
        p.playSound(c, Sound.ENTITY_BLAZE_SHOOT, 1.5f, 1.2f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 12) { cancel(); return; }
                // Wing-like flame arcs
                double angle = Math.toRadians(tick * 15);
                Location left = c.clone().add(-Math.cos(angle) * 2, 1 + Math.sin(angle), -Math.sin(angle));
                Location right = c.clone().add(Math.cos(angle) * 2, 1 + Math.sin(angle), Math.sin(angle));
                p.getWorld().spawnParticle(Particle.FLAME, left, 3, 0.1, 0.1, 0.1, 0.01);
                p.getWorld().spawnParticle(Particle.FLAME, right, 3, 0.1, 0.1, 0.1, 0.01);
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        // Explosion
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            p.playSound(c, Sound.ENTITY_BLAZE_SHOOT, 2.0f, 0.6f);
            p.getWorld().spawnParticle(Particle.FLAME, c.clone().add(0, 1, 0), 50, 4, 2, 4, 0.12);
            p.getWorld().spawnParticle(Particle.LAVA, c, 20, 3, 1, 3, 0);
            p.getWorld().spawnParticle(Particle.DUST, c.clone().add(0, 2, 0), 30, 3, 2, 3, 0,
                    dustOpt(255, 150, 0, 2.5f));
            for (LivingEntity e : ctx.getNearbyEnemies(c, 7.0, p)) {
                ctx.dealDamage(p, e, 28.0);
                e.setFireTicks(120);
            }
        }, 14L);
        p.sendActionBar(Component.text("\u2600 Phoenix Strike!", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // #39 SOUL COLLECTOR: Soul Harvest - collect souls from kills
    void rcSoulCollector(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 5.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        int souls = ctx.soulCount.getOrDefault(p.getUniqueId(), 0);
        double bonusDmg = souls * 5.0;
        double totalDmg = 25.0 + bonusDmg;
        // Wind-up: soul energy gathers
        p.playSound(p.getLocation(), Sound.ENTITY_VEX_CHARGE, 1.0f, 0.5f);
        for (int i = 0; i < souls; i++) {
            double angle = (2 * Math.PI / Math.max(1, souls)) * i;
            Location orbLoc = p.getLocation().add(Math.cos(angle) * 1.5, 1.5, Math.sin(angle) * 1.5);
            p.getWorld().spawnParticle(Particle.SOUL, orbLoc, 4, 0.1, 0.1, 0.1, 0.01);
        }
        // Strike
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            if (target.isDead()) return;
            ctx.dealDamage(p, target, totalDmg);
            target.getWorld().spawnParticle(Particle.SOUL, target.getLocation().add(0, 1, 0), 20, 0.4, 0.6, 0.4, 0.05);
            if (target.isDead() || target.getHealth() <= 0) {
                if (souls < 5) {
                    ctx.soulCount.put(p.getUniqueId(), souls + 1);
                    p.playSound(p.getLocation(), Sound.ENTITY_VEX_DEATH, 1.0f, 0.5f);
                    p.sendActionBar(Component.text("\u2620 Soul captured! (" + (souls + 1) + "/5) +" + String.format("%.0f", bonusDmg) + " bonus", NamedTextColor.DARK_PURPLE));
                }
            }
        }, 5L);
        p.sendActionBar(Component.text("\u2620 Soul Harvest! " + souls + " souls empowering...", NamedTextColor.DARK_PURPLE));
    }

    // #40 VALHAKYRA: Valkyrie Dive - leap into sky then slam down
    void rcValhakyra(Player p) {
        Location origin = p.getLocation();
        // Launch
        p.setVelocity(new Vector(0, 2.8, 0));
        p.playSound(origin, Sound.ENTITY_ENDER_DRAGON_FLAP, 2.0f, 1.2f);
        // Trail during ascent
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 15) { cancel(); return; }
                p.getWorld().spawnParticle(Particle.CLOUD, p.getLocation(), 5, 0.3, 0.1, 0.3, 0.02);
                p.getWorld().spawnParticle(Particle.DUST, p.getLocation(), 3, 0.2, 0.2, 0.2, 0,
                        dustOpt(255, 215, 0, 1.5f));
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        // Slam down
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            Location landing = p.getLocation();
            p.playSound(landing, Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.6f);
            p.getWorld().spawnParticle(Particle.EXPLOSION, landing, 3, 1, 0.5, 1, 0);
            p.getWorld().spawnParticle(Particle.CLOUD, landing, 30, 3, 1, 3, 0.1);
            spawnRing(landing, 4.0, 16, Particle.DUST, dustOpt(255, 215, 0, 2.0f), p);
            for (LivingEntity e : ctx.getNearbyEnemies(landing, 7.0, p)) {
                ctx.dealDamage(p, e, 30.0);
                e.setVelocity(e.getLocation().toVector().subtract(landing.toVector()).normalize().multiply(1.5).setY(1.5));
            }
        }, 25L);
        p.sendActionBar(Component.text("\u2694 VALKYRIE DIVE! \u2694", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // #41 PHANTOMGUARD: Spectral Cleave - ghost blade passes through blocks
    void rcPhantomguard(Player p) {
        Vector dir = p.getLocation().getDirection().normalize();
        Location start = p.getEyeLocation();
        // Wind-up: spectral energy
        p.playSound(start, Sound.ENTITY_VEX_CHARGE, 1.5f, 0.5f);
        p.getWorld().spawnParticle(Particle.DUST, start, 10, 0.3, 0.3, 0.3, 0,
                dustOpt(150, 150, 200, 1.5f));
        // Spectral wave
        new BukkitRunnable() {
            int tick = 0;
            Set<UUID> hitSet = new HashSet<>();
            @Override public void run() {
                if (tick > 12) { cancel(); return; }
                Location point = start.clone().add(dir.clone().multiply(tick));
                p.getWorld().spawnParticle(Particle.REVERSE_PORTAL, point, 8, 0.3, 0.3, 0.3, 0.05);
                p.getWorld().spawnParticle(Particle.DUST, point, 5, 0.2, 0.2, 0.2, 0,
                        dustOpt(100, 100, 180, 1.5f));
                for (Entity e : point.getWorld().getNearbyEntities(point, 1.8, 1.8, 1.8)) {
                    if (e instanceof LivingEntity le && !(e instanceof ArmorStand) && e != p) {
                        if (le instanceof Player tp && ctx.guildManager.areInSameGuild(p.getUniqueId(), tp.getUniqueId())) continue;
                        if (hitSet.add(le.getUniqueId())) {
                            ctx.dealDamage(p, le, 28.0);
                            le.getWorld().spawnParticle(Particle.DUST, le.getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0,
                                    dustOpt(150, 100, 255, 2.0f));
                        }
                    }
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 3L, 1L);
        p.sendActionBar(Component.text("\u2694 Spectral Cleave!", NamedTextColor.GRAY).decorate(TextDecoration.BOLD));
    }

    // #42 ZENITH: Final Judgment - 360 divine explosion
    void rcZenith(Player p) {
        Location c = p.getLocation();
        // Wind-up: ascending holy rings
        p.playSound(c, Sound.BLOCK_BEACON_ACTIVATE, 2.0f, 0.5f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 15) { cancel(); return; }
                double y = tick * 0.3;
                double radius = 3.0 - tick * 0.15;
                spawnRing(c.clone().add(0, y, 0), radius, 8, Particle.END_ROD, null, p);
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        // Judgment
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            p.playSound(c, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 1.0f);
            p.playSound(c, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 0.5f);
            p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, c.clone().add(0, 1, 0), 60, 4, 3, 4, 0.8);
            p.getWorld().spawnParticle(Particle.END_ROD, c.clone().add(0, 1, 0), 40, 4, 4, 4, 0.1);
            for (LivingEntity e : ctx.getNearbyEnemies(c, 8.0, p)) {
                ctx.dealDamage(p, e, 35.0);
                Vector kb = e.getLocation().toVector().subtract(c.toVector()).normalize().multiply(2.0).setY(1.0);
                e.setVelocity(kb);
            }
        }, 18L);
        p.sendActionBar(Component.text("\u2726 FINAL JUDGMENT! \u2726", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // #43 DRAGON SWORD: Dragon Breath - cone of dragonfire
    void rcDragonSword(Player p) {
        Vector dir = p.getLocation().getDirection().normalize();
        Location start = p.getEyeLocation();
        // Wind-up: inhale
        p.playSound(start, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.5f);
        p.getWorld().spawnParticle(Particle.SMOKE, start, 10, 0.2, 0.2, 0.2, 0.03);
        // Breath cone
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick > 15) { cancel(); return; }
                double spread = tick * 0.12;
                Location point = start.clone().add(dir.clone().multiply(tick));
                p.getWorld().spawnParticle(Particle.FLAME, point, 6, spread, spread, spread, 0.03);
                p.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, point, 4, spread * 0.5, spread * 0.5, spread * 0.5, 0.02);
                for (LivingEntity e : ctx.getNearbyEnemies(point, 1.5 + spread, p)) {
                    ctx.dealDamage(p, e, 26.0);
                    e.setFireTicks(120);
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 5L, 1L);
        p.sendActionBar(Component.text("\u2620 Dragon Breath!", NamedTextColor.RED).decorate(TextDecoration.BOLD));
    }

    // #44 NOCTURNE: Shadow Slash - darkness blade with blindness
    void rcNocturne(Player p) {
        Vector dir = p.getLocation().getDirection().normalize();
        Location start = p.getEyeLocation();
        // Wind-up: shadows coalesce
        p.playSound(start, Sound.ENTITY_PHANTOM_BITE, 1.5f, 0.5f);
        p.getWorld().spawnParticle(Particle.SMOKE, start, 15, 0.3, 0.3, 0.3, 0.02);
        // Shadow slash line
        new BukkitRunnable() {
            int tick = 0;
            Set<UUID> hitSet = new HashSet<>();
            @Override public void run() {
                if (tick > 10) { cancel(); return; }
                Location point = start.clone().add(dir.clone().multiply(tick));
                p.getWorld().spawnParticle(Particle.DUST, point, 8, 0.3, 0.3, 0.3, 0,
                        dustOpt(20, 0, 30, 2.0f));
                p.getWorld().spawnParticle(Particle.SMOKE, point, 4, 0.1, 0.1, 0.1, 0.01);
                for (LivingEntity e : ctx.getNearbyEnemies(point, 1.8, p)) {
                    if (hitSet.add(e.getUniqueId())) {
                        ctx.dealDamage(p, e, 26.0);
                        e.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
                        e.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 40, 0));
                    }
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 4L, 1L);
        p.sendActionBar(Component.text("\u263D Shadow Slash!", NamedTextColor.DARK_GRAY).decorate(TextDecoration.BOLD));
    }

    // #45 DIVINE AXE RHITTA: Cruel Sun - massive solar sphere
    void rcDivineAxeRhitta(Player p) {
        Location c = p.getLocation();
        // Wind-up: sun forms above player
        p.playSound(c, Sound.ENTITY_BLAZE_SHOOT, 2.0f, 0.4f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 20) { cancel(); return; }
                Location sun = c.clone().add(0, 4 + tick * 0.1, 0);
                double size = 1.0 + tick * 0.1;
                p.getWorld().spawnParticle(Particle.DUST, sun, 8, size * 0.3, size * 0.3, size * 0.3, 0,
                        dustOpt(255, 200, 50, (float) size));
                p.getWorld().spawnParticle(Particle.FLAME, sun, 3, size * 0.2, size * 0.2, size * 0.2, 0.01);
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        // Drop the sun
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            p.playSound(c, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 0.6f);
            p.getWorld().spawnParticle(Particle.FLAME, c.clone().add(0, 3, 0), 60, 5, 3, 5, 0.15);
            p.getWorld().spawnParticle(Particle.LAVA, c.clone().add(0, 2, 0), 30, 4, 2, 4, 0);
            p.getWorld().spawnParticle(Particle.DUST, c.clone().add(0, 2, 0), 30, 4, 2, 4, 0,
                    dustOpt(255, 165, 0, 3.0f));
            for (LivingEntity e : ctx.getNearbyEnemies(c, 10.0, p)) {
                ctx.dealDamage(p, e, 32.0);
                e.setFireTicks(200);
                e.setVelocity(e.getLocation().toVector().subtract(c.toVector()).normalize().multiply(1.5).setY(0.8));
            }
        }, 22L);
        p.sendActionBar(Component.text("\u2600 CRUEL SUN! \u2600", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // #46 YORU: World's Strongest Slash - 20-block ultra-range cleave
    void rcYoru(Player p) {
        Vector dir = p.getLocation().getDirection().normalize();
        Location start = p.getEyeLocation();
        // Wind-up: dark energy condenses on blade
        p.playSound(start, Sound.ENTITY_WARDEN_SONIC_BOOM, 0.8f, 1.5f);
        p.getWorld().spawnParticle(Particle.DUST, start, 15, 0.3, 0.3, 0.3, 0,
                dustOpt(20, 0, 50, 2.5f));
        // Massive slash wave
        new BukkitRunnable() {
            int tick = 0;
            Set<UUID> hitSet = new HashSet<>();
            @Override public void run() {
                if (tick > 20) { cancel(); return; }
                Location point = start.clone().add(dir.clone().multiply(tick));
                p.getWorld().spawnParticle(Particle.SWEEP_ATTACK, point, 3, 0.3, 0.3, 0.3, 0);
                p.getWorld().spawnParticle(Particle.DUST, point, 6, 0.4, 0.4, 0.4, 0,
                        dustOpt(20, 0, 50, 2.5f));
                for (LivingEntity e : ctx.getNearbyEnemies(point, 2.0, p)) {
                    if (hitSet.add(e.getUniqueId())) {
                        ctx.dealDamage(p, e, 33.0);
                        e.getWorld().spawnParticle(Particle.DUST, e.getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0,
                                dustOpt(50, 0, 80, 2.0f));
                    }
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 5L, 1L);
        p.sendActionBar(Component.text("\u2694 WORLD'S STRONGEST SLASH! \u2694", NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
    }

    // #47 TENGEN'S BLADE: Sound Breathing - rapid 8-slash combo
    void rcTengensBlade(Player p) {
        Location c = p.getLocation().add(p.getLocation().getDirection().multiply(3));
        // Wind-up: musical charge
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.5f, 2.0f);
        p.getWorld().spawnParticle(Particle.NOTE, p.getLocation().add(0, 2, 0), 5, 0.3, 0.3, 0.3, 0.5);
        // 8 slashes with musical notes
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 8) {
                    // Final impact
                    p.getWorld().spawnParticle(Particle.DUST, c, 15, 1, 1, 1, 0,
                            dustOpt(255, 100, 200, 2.0f));
                    cancel(); return;
                }
                double angle = tick * Math.PI * 0.25;
                Location slash = c.clone().add(Math.sin(angle) * 1.5, 1, Math.cos(angle) * 1.5);
                p.getWorld().spawnParticle(Particle.SWEEP_ATTACK, slash, 2, 0.3, 0.3, 0.3, 0);
                p.getWorld().spawnParticle(Particle.NOTE, slash, 2, 0.5, 0.5, 0.5, 1);
                p.getWorld().spawnParticle(Particle.DUST, slash, 3, 0.2, 0.2, 0.2, 0,
                        dustOpt(255, 100 + tick * 20, 200, 1.0f));
                for (LivingEntity e : ctx.getNearbyEnemies(c, 4.0, p)) {
                    ctx.dealDamage(p, e, 4.0); // 4 x 8 = 32 total
                }
                p.playSound(c, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f, 1.5f + (tick * 0.1f));
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 3L, 2L);
        p.sendActionBar(Component.text("\u266B Sound Breathing! \u266B", NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD));
    }

    // #48 EDGE OF ASTRAL PLANE: Astral Rend - teleport + dimensional slash
    void rcEdgeAstralPlane(Player p) {
        Location origin = p.getLocation();
        Vector dir = origin.getDirection().normalize();
        // Wind-up: reality cracks
        p.playSound(origin, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
        p.getWorld().spawnParticle(Particle.REVERSE_PORTAL, origin.clone().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.1);
        // Teleport + slash
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            Location target = origin.clone().add(dir.multiply(8));
            target.setY(Math.max(target.getY(), p.getWorld().getHighestBlockYAt(target) + 1));
            p.teleport(target);
            p.playSound(target, Sound.ENTITY_ENDERMAN_TELEPORT, 2.0f, 0.8f);
            p.getWorld().spawnParticle(Particle.END_ROD, target.clone().add(0, 1, 0), 40, 2, 2, 2, 0.1);
            p.getWorld().spawnParticle(Particle.REVERSE_PORTAL, target.clone().add(0, 1, 0), 30, 1.5, 1.5, 1.5, 0.15);
            p.getWorld().spawnParticle(Particle.DUST, target.clone().add(0, 1, 0), 20, 2, 2, 2, 0,
                    dustOpt(150, 100, 255, 2.0f));
            for (LivingEntity e : ctx.getNearbyEnemies(target, 4.0, p)) {
                ctx.dealDamage(p, e, 30.0);
            }
        }, 6L);
        p.sendActionBar(Component.text("\u2727 Astral Rend! \u2727", NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD));
    }

    // #49 FALLEN GOD'S SPEAR: Divine Impale - single target massive damage + launch
    void rcFallenGodsSpear(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 8.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        // Wind-up: golden spear materializes
        p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 1.0f, 1.5f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 10) { cancel(); return; }
                Location trail = p.getLocation().add(0, 2 + tick * 0.2, 0);
                p.getWorld().spawnParticle(Particle.DUST, trail, 5, 0.1, 0.3, 0.1, 0,
                        dustOpt(255, 220, 100, 1.5f));
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        // Impale
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            if (target.isDead()) return;
            ctx.dealDamage(p, target, 35.0);
            target.setVelocity(new Vector(0, 3.0, 0));
            p.playSound(target.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 2.0f, 1.0f);
            target.getWorld().spawnParticle(Particle.END_ROD, target.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.2);
            target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 25, 0.5, 0.5, 0.5, 0.3);
            target.getWorld().spawnParticle(Particle.DUST, target.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0,
                    dustOpt(255, 220, 100, 2.5f));
        }, 12L);
        p.sendActionBar(Component.text("\u2694 DIVINE IMPALE! \u2694", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
    }

    // #50 NATURE SWORD: Gaia's Wrath - roots and thorns erupt
    void rcNatureSword(Player p) {
        Location c = p.getLocation();
        // Wind-up: earth trembles
        p.playSound(c, Sound.BLOCK_GRASS_BREAK, 2.0f, 0.5f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 10) { cancel(); return; }
                double radius = tick * 0.8;
                spawnRing(c.clone().add(0, 0.3, 0), radius, 6, Particle.COMPOSTER, null, p);
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 2L);
        // Eruption
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            p.playSound(c, Sound.BLOCK_GRASS_BREAK, 2.0f, 0.3f);
            p.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, c.clone().add(0, 0.5, 0), 40, 4, 0.5, 4, 0);
            p.getWorld().spawnParticle(Particle.DUST, c.clone().add(0, 1, 0), 30, 4, 1, 4, 0,
                    dustOpt(50, 180, 50, 2.0f));
            p.getWorld().spawnParticle(Particle.BLOCK, c, 20, 4, 0.5, 4, 0.1, Material.OAK_LEAVES.createBlockData());
            for (LivingEntity e : ctx.getNearbyEnemies(c, 8.0, p)) {
                ctx.dealDamage(p, e, 28.0);
                e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 3));
                e.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
            }
        }, 20L);
        p.sendActionBar(Component.text("\u2618 Gaia's Wrath! \u2618", NamedTextColor.GREEN).decorate(TextDecoration.BOLD));
    }

    // #51 HEAVENLY PARTISAN: Holy Lance - beam of light that heals allies
    void rcHeavenlyPartisan(Player p) {
        Vector dir = p.getLocation().getDirection().normalize();
        Location start = p.getEyeLocation();
        // Wind-up: holy charge
        p.playSound(start, Sound.BLOCK_BEACON_ACTIVATE, 2.0f, 1.5f);
        p.getWorld().spawnParticle(Particle.DUST, start, 10, 0.3, 0.3, 0.3, 0,
                dustOpt(255, 255, 200, 1.5f));
        // Holy beam
        new BukkitRunnable() {
            int tick = 0;
            Set<UUID> hitSet = new HashSet<>();
            @Override public void run() {
                if (tick > 15) { cancel(); return; }
                Location point = start.clone().add(dir.clone().multiply(tick));
                p.getWorld().spawnParticle(Particle.END_ROD, point, 5, 0.2, 0.2, 0.2, 0);
                p.getWorld().spawnParticle(Particle.DUST, point, 5, 0.15, 0.15, 0.15, 0,
                        dustOpt(255, 255, 200, 1.5f));
                for (LivingEntity e : ctx.getNearbyEnemies(point, 1.5, p)) {
                    if (hitSet.add(e.getUniqueId())) {
                        ctx.dealDamage(p, e, 28.0);
                    }
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 5L, 1L);
        // Heal allies
        for (Entity e : p.getLocation().getWorld().getNearbyEntities(p.getLocation(), 8, 8, 8)) {
            if (e instanceof Player ally && ally != p && ctx.guildManager.areInSameGuild(p.getUniqueId(), ally.getUniqueId())) {
                double max = ally.getAttribute(Attribute.MAX_HEALTH).getValue();
                ally.setHealth(Math.min(max, ally.getHealth() + 8.0));
                ally.getWorld().spawnParticle(Particle.HEART, ally.getLocation().add(0, 2, 0), 5, 0.3, 0.3, 0.3, 0);
            }
        }
        p.sendActionBar(Component.text("\u2694 Holy Lance! \u2694", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
    }

    // #52 SOUL DEVOURER: Soul Rip - drain health from target
    void rcSoulDevourer(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 6.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        // Wind-up: soul tendrils reach out
        p.playSound(p.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 1.5f, 0.4f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 10 || target.isDead()) { cancel(); return; }
                // Soul trail from target to player
                drawParticleLine(target.getLocation().add(0, 1, 0), p.getLocation().add(0, 1, 0),
                        Particle.SOUL, 2, 0.05, p);
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 2L);
        // Rip
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            if (target.isDead()) return;
            ctx.dealDamage(p, target, 30.0);
            double max = p.getAttribute(Attribute.MAX_HEALTH).getValue();
            p.setHealth(Math.min(max, p.getHealth() + 15.0));
            target.getWorld().spawnParticle(Particle.SOUL, target.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
            target.getWorld().spawnParticle(Particle.SCULK_CHARGE_POP, target.getLocation().add(0, 1, 0), 20, 0.4, 0.5, 0.4, 0.03);
            p.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, p.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0);
        }, 12L);
        p.sendActionBar(Component.text("\u2620 Soul Rip! +7.5 hearts!", NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
    }

    // #53 MJOLNIR: Thunderstrike - triple lightning + electric AoE
    void rcMjolnir(Player p) {
        Location target = p.getLocation().add(p.getLocation().getDirection().multiply(6));
        // Wind-up: electric charge on hammer
        p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 2.0f);
        p.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, p.getLocation().add(0, 2, 0), 15, 0.3, 0.3, 0.3, 0.1);
        // Throw
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            p.getWorld().strikeLightningEffect(target);
            p.getWorld().strikeLightningEffect(target.clone().add(2, 0, 0));
            p.getWorld().strikeLightningEffect(target.clone().add(-2, 0, 0));
            p.playSound(target, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 0.7f);
            p.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, target.clone().add(0, 1, 0), 50, 3, 2, 3, 0.2);
            p.getWorld().spawnParticle(Particle.DUST, target.clone().add(0, 1, 0), 30, 3, 2, 3, 0,
                    dustOpt(100, 200, 255, 2.0f));
            for (LivingEntity e : ctx.getNearbyEnemies(target, 7.0, p)) {
                ctx.dealDamage(p, e, 32.0);
                e.setVelocity(new Vector(0, 1.0, 0));
            }
        }, 8L);
        p.sendActionBar(Component.text("\u26A1 THUNDERSTRIKE! \u26A1", NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
    }

    // #54 THOUSAND DEMON DAGGERS: Demon Barrage - fan of 12 hellfire daggers
    void rcThousandDemonDaggers(Player p) {
        Vector baseDir = p.getLocation().getDirection().normalize();
        // Wind-up: demonic energy
        p.playSound(p.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.5f, 1.5f);
        p.getWorld().spawnParticle(Particle.DUST, p.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0,
                dustOpt(200, 50, 0, 1.5f));
        // 12 daggers in fan
        for (int i = -5; i <= 6; i++) {
            double angle = Math.toRadians(i * 8);
            Vector dir = ctx.rotateY(baseDir.clone(), angle).normalize();
            final Vector fDir = dir;
            final int delay = Math.abs(i);
            Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
                new BukkitRunnable() {
                    Location loc = p.getEyeLocation().clone();
                    int ticks = 0;
                    @Override public void run() {
                        if (ticks > 14) { cancel(); return; }
                        loc.add(fDir.clone().multiply(1.3));
                        p.getWorld().spawnParticle(Particle.DUST, loc, 2, 0.05, 0.05, 0.05, 0,
                                dustOpt(200, 50, 0, 0.8f));
                        p.getWorld().spawnParticle(Particle.SMALL_FLAME, loc, 1, 0, 0, 0, 0);
                        for (LivingEntity e : ctx.getNearbyEnemies(loc, 0.8, p)) {
                            ctx.dealDamage(p, e, 3.0);
                            e.setFireTicks(40);
                            cancel(); return;
                        }
                        ticks++;
                    }
                }.runTaskTimer(ctx.plugin, 0L, 1L);
            }, delay);
        }
        p.sendActionBar(Component.text("\u2620 Demon Barrage!", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
    }

    // #55 STAR EDGE: Cosmic Slash - starlight beam that launches to sky
    void rcStarEdge(Player p) {
        Vector dir = p.getLocation().getDirection().normalize();
        Location start = p.getEyeLocation();
        // Wind-up: cosmic energy gathers
        p.playSound(start, Sound.BLOCK_BEACON_DEACTIVATE, 2.0f, 2.0f);
        p.getWorld().spawnParticle(Particle.DUST, start, 15, 0.3, 0.3, 0.3, 0,
                dustOpt(200, 200, 255, 1.5f));
        // Cosmic beam
        new BukkitRunnable() {
            int tick = 0;
            Set<UUID> hitSet = new HashSet<>();
            @Override public void run() {
                if (tick > 14) { cancel(); return; }
                Location point = start.clone().add(dir.clone().multiply(tick));
                p.getWorld().spawnParticle(Particle.END_ROD, point, 6, 0.3, 0.3, 0.3, 0.05);
                p.getWorld().spawnParticle(Particle.DUST, point, 5, 0.2, 0.2, 0.2, 0,
                        dustOpt(200, 200, 255, 2.0f));
                for (LivingEntity e : ctx.getNearbyEnemies(point, 2.0, p)) {
                    if (hitSet.add(e.getUniqueId())) {
                        ctx.dealDamage(p, e, 30.0);
                        e.setVelocity(new Vector(0, 3.0, 0));
                        e.getWorld().spawnParticle(Particle.DUST, e.getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0,
                                dustOpt(255, 255, 255, 2.5f));
                    }
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 5L, 1L);
        p.sendActionBar(Component.text("\u2726 Cosmic Slash! \u2726", NamedTextColor.WHITE).decorate(TextDecoration.BOLD));
    }

    // #56 RIVERS OF BLOOD: Corpse Piler - rapid 6-dash blood combo
    void rcRiversOfBlood(Player p) {
        // Wind-up: blade draws blood
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2.0f, 1.8f);
        p.getWorld().spawnParticle(Particle.DUST, p.getLocation().add(0, 1, 0), 8, 0.3, 0.5, 0.3, 0,
                dustOpt(180, 0, 0, 1.5f));
        // 6 rapid dashes
        new BukkitRunnable() {
            int dash = 0;
            @Override public void run() {
                if (dash >= 6) { cancel(); return; }
                p.setVelocity(p.getLocation().getDirection().multiply(1.3));
                Location loc = p.getLocation();
                p.getWorld().spawnParticle(Particle.DUST, loc.clone().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0,
                        dustOpt(180, 0, 0, 1.5f));
                p.getWorld().spawnParticle(Particle.SWEEP_ATTACK, loc.clone().add(0, 1, 0), 2, 0.3, 0.3, 0.3, 0);
                for (LivingEntity e : ctx.getNearbyEnemies(loc, 3.0, p)) {
                    ctx.dealDamage(p, e, 5.0); // 5 x 6 = 30 total
                    e.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, e.getLocation().add(0, 1, 0), 3, 0.2, 0.2, 0.2, 0);
                }
                p.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 1.5f + (dash * 0.15f));
                dash++;
            }
        }.runTaskTimer(ctx.plugin, 3L, 3L);
        p.sendActionBar(Component.text("\u2620 Corpse Piler!", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
    }

    // #57 DRAGON SLAYING BLADE: Dragon Pierce - massive single-target (2x vs dragons)
    void rcDragonSlayingBlade(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 6.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        // Wind-up: dragonslayer glow
        p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 1.0f, 1.5f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 10) { cancel(); return; }
                p.getWorld().spawnParticle(Particle.DUST, p.getLocation().add(0, 1.5 + tick * 0.1, 0), 5, 0.2, 0.2, 0.2, 0,
                        dustOpt(255, 200, 50, 1.5f + tick * 0.1f));
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        // Pierce
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            if (target.isDead()) return;
            double dmg = target.getType() == EntityType.ENDER_DRAGON ? 70.0 : 35.0;
            ctx.dealDamage(p, target, dmg);
            p.playSound(target.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 2.0f, 1.2f);
            target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 40, 0.5, 1, 0.5, 0.3);
            target.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, target.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.2);
            target.getWorld().spawnParticle(Particle.DUST, target.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0,
                    dustOpt(255, 200, 50, 3.0f));
        }, 12L);
        p.sendActionBar(Component.text("\u2694 DRAGON PIERCE! \u2694", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // #58 STOP SIGN: Full Stop - stuns everything in AoE
    void rcStopSign(Player p) {
        Location c = p.getLocation();
        // Wind-up: red warning flash
        p.playSound(c, Sound.BLOCK_ANVIL_LAND, 2.0f, 0.3f);
        p.getWorld().spawnParticle(Particle.DUST, c.clone().add(0, 2, 0), 15, 1, 1, 1, 0,
                dustOpt(255, 0, 0, 3.0f));
        // FULL STOP
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            p.playSound(c, Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.5f);
            p.getWorld().spawnParticle(Particle.DUST, c.clone().add(0, 1, 0), 40, 3, 2, 3, 0,
                    dustOpt(255, 0, 0, 3.0f));
            p.getWorld().spawnParticle(Particle.EXPLOSION, c, 2, 1, 0.5, 1, 0);
            spawnRing(c, 5.0, 16, Particle.DUST, dustOpt(255, 255, 255, 2.0f), p);
            for (LivingEntity e : ctx.getNearbyEnemies(c, 6.0, p)) {
                ctx.dealDamage(p, e, 25.0);
                e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 255));
                e.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 60, 255));
                e.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
            }
        }, 8L);
        p.sendActionBar(Component.text("\u26D4 FULL STOP! \u26D4", NamedTextColor.RED).decorate(TextDecoration.BOLD));
    }

    // #59 CREATION SPLITTER: Reality Cleave - cross-shaped dimensional rift
    void rcCreationSplitter(Player p) {
        Location c = p.getLocation();
        // Wind-up: reality warps
        p.playSound(c, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 2.0f, 0.3f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 15) { cancel(); return; }
                p.getWorld().spawnParticle(Particle.REVERSE_PORTAL, p.getLocation().add(0, 1, 0),
                        5 + tick * 2, 0.5 + tick * 0.05, 0.5 + tick * 0.05, 0.5 + tick * 0.05, 0.1);
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        // Cleave
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            p.playSound(c, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 0.5f);
            p.playSound(c, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 0.3f);
            // Cross pattern
            for (int i = -10; i <= 10; i++) {
                Location l1 = c.clone().add(i, 1, 0);
                Location l2 = c.clone().add(0, 1, i);
                p.getWorld().spawnParticle(Particle.END_ROD, l1, 2, 0.1, 0.1, 0.1, 0);
                p.getWorld().spawnParticle(Particle.END_ROD, l2, 2, 0.1, 0.1, 0.1, 0);
                p.getWorld().spawnParticle(Particle.DUST, l1, 2, 0.05, 0.05, 0.05, 0,
                        dustOpt(200, 150, 255, 2.0f));
                p.getWorld().spawnParticle(Particle.DUST, l2, 2, 0.05, 0.05, 0.05, 0,
                        dustOpt(200, 150, 255, 2.0f));
            }
            p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, c.clone().add(0, 2, 0), 50, 2, 2, 2, 0.5);
            for (LivingEntity e : ctx.getNearbyEnemies(c, 10.0, p)) {
                ctx.dealDamage(p, e, 35.0);
                e.setVelocity(e.getLocation().toVector().subtract(c.toVector()).normalize().multiply(2.0).setY(1.0));
            }
        }, 18L);
        p.sendActionBar(Component.text("\u2726 REALITY CLEAVE! \u2726", NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD));
    }

    // =====================================================================
    //  ABYSSAL TIER (4 weapons) - 200 particle budget, 35-60 damage
    // =====================================================================

    // #60 REQUIEM AWAKENED: Abyssal Devour - massive soul drain AoE with chain explosions
    void rcRequiemAwakened(Player p) {
        Location c = p.getLocation();
        // Wind-up phase: abyssal vortex forming
        p.playSound(c, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 0.3f);
        p.playSound(c, Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 20) { cancel(); return; }
                double radius = 6.0 - tick * 0.2;
                int points = 12 + tick;
                for (int i = 0; i < points; i++) {
                    double angle = Math.toRadians((360.0 / points) * i) + tick * 0.3;
                    Location ring = c.clone().add(Math.cos(angle) * radius, 0.5 + tick * 0.1, Math.sin(angle) * radius);
                    p.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, ring, 2, 0.05, 0.1, 0.05, 0.01);
                }
                p.getWorld().spawnParticle(Particle.SCULK_CHARGE_POP, c.clone().add(0, 1, 0), tick, 1, 0.5, 1, 0.03);
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        // Devour
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            p.playSound(c, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 0.3f);
            p.getWorld().spawnParticle(Particle.SOUL, c.clone().add(0, 1, 0), 80, 6, 3, 6, 0.1);
            p.getWorld().spawnParticle(Particle.SCULK_CHARGE_POP, c.clone().add(0, 1, 0), 60, 6, 2, 6, 0.05);
            p.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, c.clone().add(0, 0.5, 0), 60, 6, 1, 6, 0.05);
            double totalHealed = 0;
            List<LivingEntity> enemies = ctx.getNearbyEnemies(c, 12.0, p);
            for (LivingEntity e : enemies) {
                ctx.dealDamage(p, e, 50.0);
                e.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 200, 2));
                e.getWorld().spawnParticle(Particle.SOUL, e.getLocation().add(0, 1, 0), 15, 0.5, 0.8, 0.5, 0.05);
                totalHealed += 10.0;
                // Chain explosion on kill
                if (e.getHealth() <= 0 || e.isDead()) {
                    Location deathLoc = e.getLocation();
                    Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
                        deathLoc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, deathLoc.clone().add(0, 1, 0), 40, 2.5, 1.5, 2.5, 0.1);
                        deathLoc.getWorld().spawnParticle(Particle.EXPLOSION, deathLoc, 2, 1, 1, 1, 0);
                        deathLoc.getWorld().playSound(deathLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);
                        for (LivingEntity nearby : ctx.getNearbyEnemies(deathLoc, 5.0, p)) {
                            ctx.dealDamage(p, nearby, 20.0);
                        }
                    }, 5L);
                }
            }
            double maxHp = p.getAttribute(Attribute.MAX_HEALTH).getValue();
            p.setHealth(Math.min(maxHp, p.getHealth() + Math.min(totalHealed, maxHp)));
        }, 22L);
        p.sendActionBar(Component.text("\u2620 ABYSSAL DEVOUR! Souls consumed!", TextColor.color(170, 0, 0)).decorate(TextDecoration.BOLD));
    }

    // #61 EXCALIBUR AWAKENED: Divine Annihilation - ultimate holy devastation
    void rcExcaliburAwakened(Player p) {
        Location c = p.getLocation();
        // Wind-up: divine ascension
        p.playSound(c, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.5f, 1.0f);
        p.playSound(c, Sound.BLOCK_BEACON_ACTIVATE, 2.0f, 0.5f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 20) { cancel(); return; }
                double y = tick * 0.2;
                for (int ring = 0; ring < 3; ring++) {
                    double radius = 2.0 + ring;
                    double rY = y + ring * 0.3;
                    spawnRing(c.clone().add(0, rY, 0), radius, 8, Particle.END_ROD, null, p);
                }
                p.getWorld().spawnParticle(Particle.DUST, c.clone().add(0, y + 2, 0), 5, 0.5, 0.5, 0.5, 0,
                        dustOpt(255, 255, 200, 2.0f));
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        // Holy beam phase
        Vector dir = p.getLocation().getDirection().normalize();
        Location start = p.getEyeLocation();
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            p.playSound(c, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 0.5f);
            Set<UUID> hit = new HashSet<>();
            new BukkitRunnable() {
                int tick = 0;
                @Override public void run() {
                    if (tick > 15) {
                        // Final holy explosion
                        Location end = start.clone().add(dir.clone().multiply(15));
                        end.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, end, 60, 5, 3, 5, 0.5);
                        end.getWorld().spawnParticle(Particle.END_ROD, end, 40, 5, 4, 5, 0.3);
                        end.getWorld().spawnParticle(Particle.DUST, end, 40, 4, 3, 4, 0,
                                dustOpt(255, 255, 200, 3.0f));
                        end.getWorld().playSound(end, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.5f);
                        for (LivingEntity e : ctx.getNearbyEnemies(end, 10.0, p)) {
                            ctx.dealDamage(p, e, 35.0);
                        }
                        cancel(); return;
                    }
                    Location point = start.clone().add(dir.clone().multiply(tick));
                    p.getWorld().spawnParticle(Particle.END_ROD, point, 12, 0.3, 0.3, 0.3, 0.05);
                    p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, point, 8, 0.2, 0.2, 0.2, 0.02);
                    p.getWorld().spawnParticle(Particle.DUST, point, 8, 0.3, 0.3, 0.3, 0,
                            dustOpt(255, 255, 150, 2.0f));
                    for (LivingEntity e : ctx.getNearbyEnemies(point, 2.5, p)) {
                        if (hit.size() >= 8) break;
                        if (hit.add(e.getUniqueId())) {
                            ctx.dealDamage(p, e, 45.0);
                            e.getWorld().strikeLightningEffect(e.getLocation());
                            double maxHp = p.getAttribute(Attribute.MAX_HEALTH).getValue();
                            p.setHealth(Math.min(maxHp, p.getHealth() + 4.0));
                        }
                    }
                    tick++;
                }
            }.runTaskTimer(ctx.plugin, 0L, 1L);
        }, 22L);
        p.sendActionBar(Component.text("\u2726 DIVINE ANNIHILATION! \u2726", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // #62 CREATION SPLITTER AWAKENED: Reality Shatter - 3s charge then devastating beam
    void rcCreationSplitterAwakened(Player p) {
        Location c = p.getLocation();
        // Charge phase: root + escalating particles
        p.playSound(c, Sound.BLOCK_BEACON_ACTIVATE, 2.0f, 0.3f);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 127));
//         p.setGlowing(true); // REMOVED: no glowing on mobs
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 60) { cancel(); return; }
                int particleCount = 5 + (tick * 3);
                double radius = 0.5 + (tick * 0.03);
                p.getWorld().spawnParticle(Particle.REVERSE_PORTAL, p.getLocation().add(0, 1, 0),
                        particleCount, radius, radius, radius, 0.1);
                p.getWorld().spawnParticle(Particle.SCULK_CHARGE_POP, p.getLocation().add(0, 1.5, 0),
                        tick / 3, 0.3, 0.5, 0.3, 0.05);
                p.getWorld().spawnParticle(Particle.DUST, p.getLocation().add(0, 1, 0), tick / 4,
                        radius, radius, radius, 0, dustOpt(150, 0, 200, 1.5f + tick * 0.02f));
                if (tick % 20 == 0) {
                    p.playSound(p.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1.5f, 0.5f + (tick * 0.015f));
                    spawnRing(p.getLocation().add(0, 0.5, 0), 3.0 - tick * 0.04, 12, Particle.END_ROD, null, p);
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        // Release after 3 seconds
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            p.setGlowing(false);
            p.removePotionEffect(PotionEffectType.SLOWNESS);
            Vector dir = p.getLocation().getDirection().normalize();
            Location start = p.getEyeLocation();
            p.playSound(c, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 0.5f);
            p.playSound(c, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.3f);
            final Set<UUID> launched = new HashSet<>();
            new BukkitRunnable() {
                int tick = 0;
                @Override public void run() {
                    if (tick > 15) { cancel(); return; }
                    Location point = start.clone().add(dir.clone().multiply(tick));
                    p.getWorld().spawnParticle(Particle.REVERSE_PORTAL, point, 25, 0.5, 0.5, 0.5, 0.2);
                    p.getWorld().spawnParticle(Particle.SCULK_CHARGE_POP, point, 15, 0.3, 0.3, 0.3, 0.1);
                    p.getWorld().spawnParticle(Particle.SONIC_BOOM, point, 1, 0, 0, 0, 0);
                    p.getWorld().spawnParticle(Particle.DUST, point, 10, 0.5, 0.5, 0.5, 0,
                            dustOpt(180, 0, 255, 3.0f));
                    for (LivingEntity e : ctx.getNearbyEnemies(point, 2.5, p)) {
                        if (launched.add(e.getUniqueId())) {
                            e.damage(60.0); // true damage
                            e.setVelocity(new Vector(0, 4.0, 0));
                            e.getWorld().spawnParticle(Particle.REVERSE_PORTAL, e.getLocation(), 20, 0.5, 1, 0.5, 0.2);
                        }
                    }
                    tick++;
                }
            }.runTaskTimer(ctx.plugin, 0L, 1L);
            // Collapse phase
            Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
                Location collapse = start.clone().add(dir.clone().multiply(8));
                collapse.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, collapse, 2, 3, 2, 3, 0);
                collapse.getWorld().spawnParticle(Particle.REVERSE_PORTAL, collapse, 60, 5, 5, 5, 0.5);
                collapse.getWorld().spawnParticle(Particle.DUST, collapse, 40, 4, 3, 4, 0,
                        dustOpt(100, 0, 180, 4.0f));
                collapse.getWorld().playSound(collapse, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.3f);
                for (LivingEntity e : ctx.getNearbyEnemies(collapse, 8.0, p)) {
                    ctx.dealDamage(p, e, 40.0);
                }
            }, 40L);
        }, 60L);
        p.sendActionBar(Component.text("\u2620 CHARGING REALITY SHATTER... \u2620", TextColor.color(170, 0, 0)).decorate(TextDecoration.BOLD));
    }

    // #63 WHISPERWIND AWAKENED: Silent Storm - 30 invisible wind blades
    void rcWhisperwindAwakened(Player p) {
        Location c = p.getLocation();
        // Wind-up: eerie silence + wind gathering
        p.playSound(c, Sound.ENTITY_BREEZE_INHALE, 2.0f, 0.3f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 15) { cancel(); return; }
                double radius = 4.0 - tick * 0.2;
                spawnRing(c.clone().add(0, 1, 0), radius, 8, Particle.CLOUD, null, p);
                p.getWorld().spawnParticle(Particle.DUST, c.clone().add(0, 1, 0), tick, 2, 1, 2, 0,
                        dustOpt(200, 200, 255, 1.0f));
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        // Unleash blades
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            p.playSound(c, Sound.ENTITY_BREEZE_SHOOT, 2.0f, 0.3f);
            p.playSound(c, Sound.ENTITY_BREEZE_WIND_BURST, 2.0f, 0.5f);
            p.getWorld().spawnParticle(Particle.CLOUD, c.clone().add(0, 1, 0), 40, 2, 1, 2, 0.3);
            Vector baseDir = p.getLocation().getDirection().normalize();
            for (int i = 0; i < 30; i++) {
                double spreadAngle = Math.toRadians((i - 15) * 2.0);
                double verticalOffset = (Math.random() - 0.5) * 0.3;
                Vector bladeDir = ctx.rotateY(baseDir.clone(), spreadAngle).setY(baseDir.getY() + verticalOffset).normalize();
                final Vector fDir = bladeDir;
                final int delay = i / 3;
                Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
                    new BukkitRunnable() {
                        Location loc = p.getEyeLocation().clone();
                        int ticks = 0;
                        @Override public void run() {
                            if (ticks > 20) { cancel(); return; }
                            loc.add(fDir.clone().multiply(1.5));
                            p.getWorld().spawnParticle(Particle.CLOUD, loc, 2, 0.05, 0.05, 0.05, 0);
                            p.getWorld().spawnParticle(Particle.SWEEP_ATTACK, loc, 1, 0, 0, 0, 0);
                            p.getWorld().spawnParticle(Particle.DUST, loc, 2, 0.05, 0.05, 0.05, 0,
                                    dustOpt(180, 220, 255, 1.0f));
                            for (LivingEntity e : ctx.getNearbyEnemies(loc, 1.5, p)) {
                                ctx.dealDamage(p, e, 10.0);
                                e.getWorld().spawnParticle(Particle.CLOUD, e.getLocation().add(0, 1, 0), 5, 0.3, 0.5, 0.3, 0.05);
                            }
                            ticks++;
                        }
                    }.runTaskTimer(ctx.plugin, 0L, 1L);
                }, delay);
            }
        }, 16L);
        p.sendActionBar(Component.text("\u2601 SILENT STORM! 30 blades unleashed!", TextColor.color(200, 200, 255)).decorate(TextDecoration.BOLD));
    }

    // =====================================================================
    //  HELPER METHODS
    // =====================================================================

    /** Create DustOptions with RGB color and size. */
    private Particle.DustOptions dustOpt(int r, int g, int b, float size) {
        return new Particle.DustOptions(Color.fromRGB(r, g, b), size);
    }

    /** Spawn a horizontal ring of particles at the given location. */
    private void spawnRing(Location center, double radius, int points, Particle particle,
                           Particle.DustOptions dustOptions, Player source) {
        for (int i = 0; i < points; i++) {
            double angle = (2 * Math.PI / points) * i;
            Location point = center.clone().add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
            if (dustOptions != null) {
                source.getWorld().spawnParticle(particle, point, 1, 0, 0, 0, 0, dustOptions);
            } else {
                source.getWorld().spawnParticle(particle, point, 1, 0, 0, 0, 0);
            }
        }
    }

    /** Draw a particle line between two points. */
    private void drawParticleLine(Location from, Location to, Particle particle, int perStep, double spread, Player source) {
        Vector direction = to.toVector().subtract(from.toVector());
        double distance = direction.length();
        direction.normalize();
        for (double d = 0; d < distance; d += 0.5) {
            Location point = from.clone().add(direction.clone().multiply(d));
            source.getWorld().spawnParticle(particle, point, perStep, spread, spread, spread, 0.01);
        }
    }

    /** Check if an entity type is undead. */
    private boolean isUndeadType(EntityType type) {
        return type == EntityType.ZOMBIE || type == EntityType.SKELETON
                || type == EntityType.WITHER_SKELETON || type == EntityType.PHANTOM
                || type == EntityType.DROWNED || type == EntityType.HUSK
                || type == EntityType.STRAY || type == EntityType.ZOMBIFIED_PIGLIN
                || type == EntityType.ZOGLIN || type == EntityType.WITHER;
    }
}
