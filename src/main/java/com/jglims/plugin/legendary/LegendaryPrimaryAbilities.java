package com.jglims.plugin.legendary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
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
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextColor;

/**
 * LegendaryPrimaryAbilities - all 59 primary (right-click) ability implementations.
 * Phase 8d split from LegendaryAbilityListener.java (rule A.17).
 */
final class LegendaryPrimaryAbilities {

    private final LegendaryAbilityContext ctx;

    LegendaryPrimaryAbilities(LegendaryAbilityContext ctx) {
        this.ctx = ctx;
    }

    // â”€â”€ #1 OCEAN'S RAGE: Tidal Crash â€” 6-block AoE water blast, 15 dmg â”€â”€
    void rcOceansRage(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.ENTITY_GENERIC_SPLASH, 2.0f, 0.6f);
        p.getWorld().spawnParticle(Particle.SPLASH, c.clone().add(0, 1, 0), 100, 3, 2, 3, 0.5);
        p.getWorld().spawnParticle(Particle.BUBBLE, c.clone().add(0, 1, 0), 50, 3, 2, 3, 0.3);
        for (LivingEntity e : ctx.getNearbyEnemies(c, 6.0, p)) {
            ctx.dealDamage(p, e, 15.0);
            Vector kb = e.getLocation().toVector().subtract(c.toVector()).normalize().multiply(1.5).setY(0.6);
            e.setVelocity(kb);
            e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
        }
        p.sendActionBar(Component.text("\u2248 Tidal Crash! \u2248", NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
    }

    // â”€â”€ #2 AQUATIC SACRED BLADE: Aqua Heal â€” 6 hearts + Conduit Power 30s â”€â”€
    void rcAquaticSacredBlade(Player p) {
        double maxHp = p.getAttribute(Attribute.MAX_HEALTH).getValue();
        p.setHealth(Math.min(maxHp, p.getHealth() + 12.0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, 600, 0));
        p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.5f, 1.2f);
        p.getWorld().spawnParticle(Particle.HEART, p.getLocation().add(0, 2, 0), 10, 0.5, 0.5, 0.5, 0);
        p.getWorld().spawnParticle(Particle.BUBBLE, p.getLocation().add(0, 1, 0), 30, 1, 1, 1, 0.1);
        p.sendActionBar(Component.text("\u2764 Aqua Heal! +6 hearts + Conduit Power", NamedTextColor.AQUA));
    }

    // â”€â”€ #3 TRUE EXCALIBUR: Holy Smite â€” Lightning + 20 dmg AoE 5 blocks â”€â”€
    void rcTrueExcalibur(Player p) {
        Location c = p.getLocation().add(p.getLocation().getDirection().multiply(3));
        p.getWorld().strikeLightningEffect(c);
        p.playSound(c, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 1.0f);
        p.getWorld().spawnParticle(Particle.END_ROD, c.clone().add(0, 1, 0), 50, 2, 3, 2, 0.1);
        for (LivingEntity e : ctx.getNearbyEnemies(c, 5.0, p)) {
            ctx.dealDamage(p, e, 20.0);
        }
        p.sendActionBar(Component.text("\u2694 HOLY SMITE! \u2694", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
    }

    // â”€â”€ #4 REQUIEM: Soul Devour â€” Drain 8 hearts, heal self â”€â”€
    void rcRequiemNinthAbyss(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 5.0);
        if (target == null) { p.sendActionBar(Component.text("No target in range!", NamedTextColor.RED)); return; }
        ctx.dealDamage(p, target, 16.0);
        double maxHp = p.getAttribute(Attribute.MAX_HEALTH).getValue();
        p.setHealth(Math.min(maxHp, p.getHealth() + 16.0));
        p.playSound(p.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 1.0f, 0.5f);
        p.getWorld().spawnParticle(Particle.SOUL, target.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.05);
        p.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, p.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0);
        p.sendActionBar(Component.text("\u2620 Soul Devoured! +8 hearts", NamedTextColor.DARK_PURPLE));
    }

    // â”€â”€ #5 ROYAL CHAKRAM: Chakram Throw â€” bounces 4 targets, 8 dmg each â”€â”€
    void rcRoyalChakram(Player p) {
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 1.5f);
        List<LivingEntity> targets = ctx.getNearbyEnemies(p.getLocation(), 10.0, p);
        int hits = Math.min(4, targets.size());
        for (int i = 0; i < hits; i++) {
            LivingEntity t = targets.get(i);
            final int delay = i * 5;
            Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
                ctx.dealDamage(p, t, 8.0);
                t.getWorld().spawnParticle(Particle.CRIT, t.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.1);
                t.getWorld().playSound(t.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.8f);
            }, delay);
        }
        p.sendActionBar(Component.text("\u25C6 Chakram Throw! " + hits + " targets hit", NamedTextColor.GOLD));
    }

    // â”€â”€ #6 BERSERKER'S GREATAXE: Berserker Slam â€” 8-block AoE, 18 dmg â”€â”€
    void rcBerserkersGreataxe(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.7f);
        p.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, c, 2, 0, 0, 0, 0);
        p.getWorld().spawnParticle(Particle.BLOCK, c, 40, 4, 1, 4, 0.5, Material.NETHERRACK.createBlockData());
        for (LivingEntity e : ctx.getNearbyEnemies(c, 8.0, p)) {
            ctx.dealDamage(p, e, 18.0);
            e.setVelocity(new Vector(0, 1.2, 0));
        }
        p.sendActionBar(Component.text("\u2620 BERSERKER SLAM! \u2620", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
    }

    // â”€â”€ #7 ACIDIC CLEAVER: Acid Splash â€” 5-block cone, Poison III 8s â”€â”€
    void rcAcidicCleaver(Player p) {
        Location c = p.getLocation().add(p.getLocation().getDirection().multiply(3));
        p.playSound(c, Sound.ENTITY_LLAMA_SPIT, 1.5f, 0.5f);
        p.getWorld().spawnParticle(Particle.ITEM_SLIME, c, 40, 2.5, 1, 2.5, 0.1);
        for (LivingEntity e : ctx.getNearbyEnemies(c, 5.0, p)) {
            ctx.dealDamage(p, e, 10.0);
            e.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 160, 2));
        }
        p.sendActionBar(Component.text("\u2620 Acid Splash! Poison III applied", NamedTextColor.GREEN));
    }

    // â”€â”€ #8 BLACK IRON GREATSWORD: Dark Slash â€” 10-block line, 16 dmg â”€â”€
    void rcBlackIronGreatsword(Player p) {
        Vector dir = p.getLocation().getDirection().normalize();
        Location start = p.getLocation().add(0, 1, 0);
        p.playSound(start, Sound.ENTITY_WARDEN_SONIC_BOOM, 0.8f, 1.5f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick > 10) { cancel(); return; }
                Location point = start.clone().add(dir.clone().multiply(tick));
                p.getWorld().spawnParticle(Particle.SOUL, point, 5, 0.2, 0.2, 0.2, 0.02);
                p.getWorld().spawnParticle(Particle.SMOKE, point, 3, 0.1, 0.1, 0.1, 0.01);
                for (LivingEntity e : ctx.getNearbyEnemies(point, 1.5, p)) {
                    ctx.dealDamage(p, e, 16.0);
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2694 Dark Slash!", NamedTextColor.DARK_GRAY).decorate(TextDecoration.BOLD));
    }

    // â”€â”€ #9 MURAMASA: Crimson Flash â€” 8-block dash, 12 dmg along path â”€â”€
    void rcMuramasa(Player p) {
        p.setVelocity(p.getLocation().getDirection().multiply(2.5));
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2.0f, 1.5f);
        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (ticks >= 8) { cancel(); return; }
                Location loc = p.getLocation();
                p.getWorld().spawnParticle(Particle.DUST, loc.add(0, 1, 0), 8, 0.4, 0.4, 0.4, 0,
                        new Particle.DustOptions(Color.fromRGB(200, 0, 0), 1.5f));
                for (LivingEntity e : ctx.getNearbyEnemies(loc, 2.5, p)) {
                    ctx.dealDamage(p, e, 12.0);
                }
                ticks++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u26A1 Crimson Flash!", NamedTextColor.RED).decorate(TextDecoration.BOLD));
    }

    // â”€â”€ #10 PHOENIX'S GRACE: Phoenix Strike â€” Fire AoE 6b, 14 dmg + Fire â”€â”€
    void rcPhoenixsGrace(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.ENTITY_BLAZE_SHOOT, 2.0f, 0.8f);
        p.getWorld().spawnParticle(Particle.FLAME, c.add(0, 1, 0), 80, 3, 2, 3, 0.1);
        p.getWorld().spawnParticle(Particle.LAVA, c, 20, 3, 1, 3, 0);
        for (LivingEntity e : ctx.getNearbyEnemies(c, 6.0, p)) {
            ctx.dealDamage(p, e, 14.0);
            e.setFireTicks(120);
        }
        p.sendActionBar(Component.text("\u2600 Phoenix Strike!", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // â”€â”€ #11 SOUL COLLECTOR: Soul Harvest â€” kill stores soul, +10 bonus â”€â”€
    void rcSoulCollector(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 5.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        ctx.dealDamage(p, target, 14.0);
        if (target.isDead() || target.getHealth() <= 0) {
            int souls = ctx.soulCount.getOrDefault(p.getUniqueId(), 0);
            if (souls < 5) {
                ctx.soulCount.put(p.getUniqueId(), souls + 1);
                p.sendActionBar(Component.text("\u2620 Soul captured! (" + (souls + 1) + "/5)", NamedTextColor.DARK_PURPLE));
                p.playSound(p.getLocation(), Sound.ENTITY_VEX_DEATH, 1.0f, 0.5f);
            }
        }
        p.getWorld().spawnParticle(Particle.SOUL, target.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.05);
    }

    // â”€â”€ #12 AMETHYST SHURIKEN: Shuriken Barrage â€” 5 projectiles, 7 dmg â”€â”€
    void rcAmethystShuriken(Player p) {
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 2.0f);
        Vector baseDir = p.getLocation().getDirection().normalize();
        for (int i = -2; i <= 2; i++) {
            double angle = Math.toRadians(i * 12);
            Vector dir = ctx.rotateY(baseDir.clone(), angle);
            final Vector fDir = dir;
            new BukkitRunnable() {
                Location loc = p.getEyeLocation().clone();
                int ticks = 0;
                @Override public void run() {
                    if (ticks > 15) { cancel(); return; }
                    loc.add(fDir);
                    p.getWorld().spawnParticle(Particle.END_ROD, loc, 2, 0.05, 0.05, 0.05, 0);
                    for (LivingEntity e : ctx.getNearbyEnemies(loc, 1.0, p)) {
                        ctx.dealDamage(p, e, 7.0);
                        cancel();
                        return;
                    }
                    ticks++;
                }
            }.runTaskTimer(ctx.plugin, 0L, 1L);
        }
        p.sendActionBar(Component.text("\u25C6 Shuriken Barrage!", NamedTextColor.LIGHT_PURPLE));
    }

    // â”€â”€ #13 VALHAKYRA: Valkyrie Dive â€” Leap 10 up, slam 20 dmg AoE â”€â”€
    void rcValhakyra(Player p) {
        p.setVelocity(new Vector(0, 2.5, 0));
        p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 2.0f, 1.2f);
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            Location landing = p.getLocation();
            p.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, landing, 2, 0, 0, 0, 0);
            p.getWorld().spawnParticle(Particle.CLOUD, landing, 25, 3, 1, 3, 0.1);
            p.playSound(landing, Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.6f);
            for (LivingEntity e : ctx.getNearbyEnemies(landing, 6.0, p)) {
                ctx.dealDamage(p, e, 20.0);
                e.setVelocity(new Vector(0, 1.5, 0));
            }
        }, 25L);
        p.sendActionBar(Component.text("\u2694 VALKYRIE DIVE! \u2694", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // â”€â”€ #14 WINDREAPER: Gale Slash â€” 8-block wind cone, 12 dmg + knockback â”€â”€
    void rcWindreaper(Player p) {
        Location c = p.getLocation().add(p.getLocation().getDirection().multiply(4));
        p.playSound(c, Sound.ENTITY_BREEZE_SHOOT, 2.0f, 0.8f);
        p.getWorld().spawnParticle(Particle.CLOUD, c, 40, 3, 2, 3, 0.2);
        for (LivingEntity e : ctx.getNearbyEnemies(c, 8.0, p)) {
            ctx.dealDamage(p, e, 12.0);
            Vector kb = e.getLocation().toVector().subtract(p.getLocation().toVector()).normalize().multiply(2.5).setY(0.8);
            e.setVelocity(kb);
        }
        p.sendActionBar(Component.text("\u2601 Gale Slash!", NamedTextColor.WHITE));
    }

    // â”€â”€ #15 PHANTOMGUARD: Spectral Cleave â€” through blocks, 10-block line, 14 dmg â”€â”€
    void rcPhantomguard(Player p) {
        Vector dir = p.getLocation().getDirection().normalize();
        Location start = p.getEyeLocation();
        p.playSound(start, Sound.ENTITY_VEX_CHARGE, 1.5f, 0.5f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick > 10) { cancel(); return; }
                Location point = start.clone().add(dir.clone().multiply(tick));
                p.getWorld().spawnParticle(Particle.REVERSE_PORTAL, point, 8, 0.3, 0.3, 0.3, 0.05);
                for (Entity e : point.getWorld().getNearbyEntities(point, 1.5, 1.5, 1.5)) {
                    if (e instanceof LivingEntity le && !(e instanceof ArmorStand) && e != p) {
                        if (le instanceof Player tp && ctx.guildManager.areInSameGuild(p.getUniqueId(), tp.getUniqueId())) continue;
                        ctx.dealDamage(p, le, 14.0);
                    }
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2694 Spectral Cleave!", NamedTextColor.GRAY));
    }

    // â”€â”€ #16 MOONLIGHT: Lunar Beam â€” 15-block ranged beam, 16 dmg â”€â”€
    void rcMoonlight(Player p) {
        Vector dir = p.getLocation().getDirection().normalize();
        Location start = p.getEyeLocation();
        p.playSound(start, Sound.BLOCK_BEACON_DEACTIVATE, 1.5f, 1.5f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick > 15) { cancel(); return; }
                Location point = start.clone().add(dir.clone().multiply(tick));
                p.getWorld().spawnParticle(Particle.END_ROD, point, 5, 0.1, 0.1, 0.1, 0);
                p.getWorld().spawnParticle(Particle.ENCHANT, point, 3, 0.2, 0.2, 0.2, 0.1);
                for (LivingEntity e : ctx.getNearbyEnemies(point, 1.5, p)) {
                    ctx.dealDamage(p, e, 16.0);
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u263D Lunar Beam!", NamedTextColor.YELLOW));
    }

    // â”€â”€ #17 ZENITH: Final Judgment â€” 360 AoE 8 blocks, 22 dmg â”€â”€
    void rcZenith(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 1.0f);
        p.playSound(c, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 0.5f);
        p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, c.add(0, 1, 0), 200, 4, 3, 4, 0.8);
        p.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, c, 2, 2, 0, 2, 0);
        for (LivingEntity e : ctx.getNearbyEnemies(c, 8.0, p)) {
            ctx.dealDamage(p, e, 22.0);
            Vector kb = e.getLocation().toVector().subtract(c.toVector()).normalize().multiply(2.0).setY(1.0);
            e.setVelocity(kb);
        }
        p.sendActionBar(Component.text("\u2726 FINAL JUDGMENT! \u2726", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // â”€â”€ #18 SOLSTICE: Solar Flare â€” 10-block fire AoE, 15 dmg + blindness â”€â”€
    void rcSolstice(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.ENTITY_BLAZE_SHOOT, 2.0f, 0.6f);
        p.getWorld().spawnParticle(Particle.FLAME, c.add(0, 1, 0), 100, 5, 3, 5, 0.1);
        p.getWorld().spawnParticle(Particle.LAVA, c, 30, 5, 1, 5, 0);
        for (LivingEntity e : ctx.getNearbyEnemies(c, 10.0, p)) {
            ctx.dealDamage(p, e, 15.0);
            e.setFireTicks(60);
            if (e instanceof Player) e.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
        }
        p.sendActionBar(Component.text("\u2600 Solar Flare!", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // â”€â”€ #19 GRAND CLAYMORE: Titan Swing â€” 180 arc, 10-block, 18 dmg â”€â”€
    void rcGrandClaymore(Player p) {
        Location c = p.getLocation();
        Vector facing = c.getDirection().normalize();
        p.playSound(c, Sound.ITEM_MACE_SMASH_GROUND, 2.0f, 0.9f);
        p.getWorld().spawnParticle(Particle.SWEEP_ATTACK, c.add(0, 1, 0), 30, 5, 1, 5, 0);
        for (LivingEntity e : ctx.getNearbyEnemies(c, 10.0, p)) {
            Vector toEnemy = e.getLocation().toVector().subtract(c.toVector()).normalize();
            if (facing.dot(toEnemy) > 0) { // in front (180 arc)
                ctx.dealDamage(p, e, 18.0);
                Vector kb = toEnemy.multiply(2.5).setY(0.6);
                e.setVelocity(kb);
            }
        }
        p.sendActionBar(Component.text("\u2694 TITAN SWING! \u2694", NamedTextColor.WHITE).decorate(TextDecoration.BOLD));
    }

    // â”€â”€ #20 CALAMITY BLADE: Cataclysm â€” 6-block AoE, 14 dmg + slowness â”€â”€
    void rcCalamityBlade(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.ENTITY_WARDEN_EMERGE, 1.5f, 0.8f);
        p.getWorld().spawnParticle(Particle.BLOCK, c, 40, 3, 2, 3, 0.5, Material.STONE.createBlockData());
        p.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, c.add(0, 1, 0), 30, 3, 2, 3, 0.05);
        for (LivingEntity e : ctx.getNearbyEnemies(c, 6.0, p)) {
            ctx.dealDamage(p, e, 14.0);
            e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 2));
        }
        p.sendActionBar(Component.text("\u2622 Cataclysm!", NamedTextColor.DARK_RED));
    }

    // â”€â”€ #21 DRAGON SWORD: Dragon Breath â€” 8-block fire cone, 12 dmg â”€â”€
    void rcDragonSword(Player p) {
        Location c = p.getLocation().add(p.getLocation().getDirection().multiply(4));
        p.playSound(c, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.5f);
        p.getWorld().spawnParticle(Particle.FLAME, c, 30, 4, 2, 4, 0.1);
        p.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, c, 30, 3, 1, 3, 0.05);
        for (LivingEntity e : ctx.getNearbyEnemies(c, 8.0, p)) {
            ctx.dealDamage(p, e, 12.0);
            e.setFireTicks(80);
        }
        p.sendActionBar(Component.text("\u2620 Dragon Breath!", NamedTextColor.RED));
    }

    // â”€â”€ #22 TALONBRAND: Talon Strike â€” Triple combo, 8 dmg x3 â”€â”€
    void rcTalonbrand(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 5.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.5f, 1.5f);
        for (int i = 0; i < 3; i++) {
            Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
                if (!target.isDead()) {
                    ctx.dealDamage(p, target, 8.0);
                    target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 8, 0.2, 0.2, 0.2, 0.1);
                    p.playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.8f);
                }
            }, i * 5L);
        }
        p.sendActionBar(Component.text("\u2694 Talon Strike! x3 combo", NamedTextColor.DARK_RED));
    }

    // â”€â”€ #23 EMERALD GREATCLEAVER: Emerald Storm â€” 6-block AoE, 14 dmg + Poison â”€â”€
    void rcEmeraldGreatcleaver(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 2.0f, 0.8f);
        p.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, c.add(0, 1, 0), 60, 3, 2, 3, 0);
        p.getWorld().spawnParticle(Particle.COMPOSTER, c, 30, 3, 1, 3, 0.1);
        for (LivingEntity e : ctx.getNearbyEnemies(c, 6.0, p)) {
            ctx.dealDamage(p, e, 14.0);
            e.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 1));
        }
        p.sendActionBar(Component.text("\u2666 Emerald Storm!", NamedTextColor.GREEN));
    }

    // â”€â”€ #24 DEMON'S BLOOD BLADE: Blood Rite â€” Sacrifice 3 hearts, 25 dmg â”€â”€
    void rcDemonsBloodBlade(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 5.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        if (p.getHealth() <= 6.0) { p.sendActionBar(Component.text("Not enough health!", NamedTextColor.RED)); return; }
        p.setHealth(p.getHealth() - 6.0);
        ctx.dealDamage(p, target, 25.0);
        p.playSound(p.getLocation(), Sound.ENTITY_WITHER_HURT, 1.5f, 0.5f);
        p.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, target.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.1);
        p.getWorld().spawnParticle(Particle.DUST, p.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0,
                new Particle.DustOptions(Color.fromRGB(139, 0, 0), 2.0f));
        p.sendActionBar(Component.text("\u2620 Blood Rite! -3 hearts \u2192 25 damage!", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  UNCOMMON TIER RIGHT-CLICK ABILITIES (20)
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    // â”€â”€ #25 NOCTURNE: Shadow Slash â€” 6-block line, 10 dmg + Blindness â”€â”€
    void rcNocturne(Player p) {
        Vector dir = p.getLocation().getDirection().normalize();
        Location start = p.getEyeLocation();
        p.playSound(start, Sound.ENTITY_PHANTOM_BITE, 1.5f, 0.5f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick > 6) { cancel(); return; }
                Location point = start.clone().add(dir.clone().multiply(tick));
                p.getWorld().spawnParticle(Particle.SMOKE, point, 8, 0.2, 0.2, 0.2, 0.02);
                p.getWorld().spawnParticle(Particle.SOUL, point, 3, 0.1, 0.1, 0.1, 0.01);
                for (LivingEntity e : ctx.getNearbyEnemies(point, 1.5, p)) {
                    ctx.dealDamage(p, e, 10.0);
                    e.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u263D Shadow Slash!", NamedTextColor.DARK_GRAY));
    }

    // â”€â”€ #26 GRAVESCEPTER: Grave Rise â€” Summon 2 zombie allies, 12s â”€â”€
    void rcGravescepter(Player p) {
        p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1.5f, 0.5f);
        for (int i = 0; i < 2; i++) {
            Location spawnLoc = p.getLocation().add(Math.random() * 2 - 1, 0, Math.random() * 2 - 1);
            Zombie zombie = (Zombie) p.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);
            zombie.customName(Component.text(p.getName() + "'s Servant", NamedTextColor.GRAY));
            zombie.setCustomNameVisible(true);
            zombie.setBaby(false);
            zombie.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
            zombie.setTarget(ctx.getTargetEntity(p, 15.0));
            p.getWorld().spawnParticle(Particle.SOUL, spawnLoc.add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0.03);
            Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> { if (!zombie.isDead()) zombie.remove(); }, 240L);
        }
        p.sendActionBar(Component.text("\u2620 Grave Rise! 2 undead summoned", NamedTextColor.DARK_GRAY));
    }

    // â”€â”€ #27 LYCANBANE: Silver Strike â€” 14 dmg + clears buffs â”€â”€
    void rcLycanbane(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 5.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        ctx.dealDamage(p, target, 14.0);
        for (PotionEffect effect : target.getActivePotionEffects()) {
            if (effect.getType().equals(PotionEffectType.STRENGTH) || effect.getType().equals(PotionEffectType.SPEED)
                    || effect.getType().equals(PotionEffectType.REGENERATION) || effect.getType().equals(PotionEffectType.RESISTANCE)) {
                target.removePotionEffect(effect.getType());
            }
        }
        p.playSound(p.getLocation(), Sound.ENTITY_WOLF_AMBIENT, 1.5f, 1.2f);
        target.getWorld().spawnParticle(Particle.END_ROD, target.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.05);
        p.sendActionBar(Component.text("\u2694 Silver Strike! Buffs purged!", NamedTextColor.WHITE));
    }

    // â”€â”€ #28 GLOOMSTEEL KATANA: Quick Draw â€” 5-block dash + 10 dmg â”€â”€
    void rcGloomsteelKatana(Player p) {
        p.setVelocity(p.getLocation().getDirection().multiply(2.0));
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2.0f, 2.0f);
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            for (LivingEntity e : ctx.getNearbyEnemies(p.getLocation(), 2.5, p)) {
                ctx.dealDamage(p, e, 10.0);
                e.getWorld().spawnParticle(Particle.SWEEP_ATTACK, e.getLocation().add(0, 1, 0), 3, 0, 0, 0, 0);
            }
        }, 4L);
        p.sendActionBar(Component.text("\u26A1 Quick Draw!", NamedTextColor.DARK_GRAY));
    }

    // â”€â”€ #29 VIRIDIAN CLEAVER: Verdant Slam â€” 5-block AoE, 12 dmg + Slow â”€â”€
    void rcViridianCleaver(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.ITEM_MACE_SMASH_GROUND, 1.5f, 0.9f);
        p.getWorld().spawnParticle(Particle.COMPOSTER, c, 25, 2.5, 1, 2.5, 0.1);
        for (LivingEntity e : ctx.getNearbyEnemies(c, 5.0, p)) {
            ctx.dealDamage(p, e, 12.0);
            e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
        }
        p.sendActionBar(Component.text("\u2618 Verdant Slam!", NamedTextColor.GREEN));
    }

    // â”€â”€ #30 CRESCENT EDGE: Lunar Cleave â€” 180 arc 6 blocks, 10 dmg â”€â”€
    void rcCrescentEdge(Player p) {
        Location c = p.getLocation();
        Vector facing = c.getDirection().normalize();
        p.playSound(c, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 1.0f);
        p.getWorld().spawnParticle(Particle.SWEEP_ATTACK, c.add(0, 1, 0), 20, 3, 1, 3, 0);
        for (LivingEntity e : ctx.getNearbyEnemies(c, 6.0, p)) {
            Vector toE = e.getLocation().toVector().subtract(c.toVector()).normalize();
            if (facing.dot(toE) > 0) ctx.dealDamage(p, e, 10.0);
        }
        p.sendActionBar(Component.text("\u263D Lunar Cleave!", NamedTextColor.YELLOW));
    }

    // â”€â”€ #31 GRAVECLEAVER: Bone Shatter â€” 15 dmg + armor reduction â”€â”€
    void rcGravecleaver(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 5.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        ctx.dealDamage(p, target, 15.0);
        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 1));
        p.playSound(p.getLocation(), Sound.ENTITY_SKELETON_HURT, 1.5f, 0.5f);
        target.getWorld().spawnParticle(Particle.BLOCK, target.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.1, Material.BONE_BLOCK.createBlockData());
        p.sendActionBar(Component.text("\u2620 Bone Shatter! Armor weakened!", NamedTextColor.GRAY));
    }

    // â”€â”€ #32 AMETHYST GREATBLADE: Crystal Burst â€” 4-block AoE, 9 dmg + Levitation â”€â”€
    void rcAmethystGreatblade(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 2.0f, 1.0f);
        p.getWorld().spawnParticle(Particle.END_ROD, c.add(0, 1, 0), 40, 2, 2, 2, 0.1);
        for (LivingEntity e : ctx.getNearbyEnemies(c, 4.0, p)) {
            ctx.dealDamage(p, e, 9.0);
            e.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 40, 1));
        }
        p.sendActionBar(Component.text("\u2666 Crystal Burst!", NamedTextColor.LIGHT_PURPLE));
    }

    // â”€â”€ #33 FLAMBERGE: Flame Wave â€” 6-block cone, 10 dmg + Fire â”€â”€
    void rcFlamberge(Player p) {
        Location c = p.getLocation().add(p.getLocation().getDirection().multiply(3));
        p.playSound(c, Sound.ENTITY_BLAZE_SHOOT, 1.5f, 1.0f);
        p.getWorld().spawnParticle(Particle.FLAME, c, 40, 3, 1, 3, 0.1);
        for (LivingEntity e : ctx.getNearbyEnemies(c, 6.0, p)) {
            ctx.dealDamage(p, e, 10.0);
            e.setFireTicks(80);
        }
        p.sendActionBar(Component.text("\u2600 Flame Wave!", NamedTextColor.RED));
    }

    // â”€â”€ #34 CRYSTAL FROSTBLADE: Frost Spike â€” 8-block projectile, 10 dmg + Slow â”€â”€
    void rcCrystalFrostblade(Player p) {
        Vector dir = p.getLocation().getDirection().normalize();
        Location start = p.getEyeLocation();
        p.playSound(start, Sound.BLOCK_GLASS_BREAK, 1.5f, 1.5f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick > 8) { cancel(); return; }
                Location point = start.clone().add(dir.clone().multiply(tick));
                p.getWorld().spawnParticle(Particle.SNOWFLAKE, point, 5, 0.1, 0.1, 0.1, 0);
                for (LivingEntity e : ctx.getNearbyEnemies(point, 1.5, p)) {
                    ctx.dealDamage(p, e, 10.0);
                    e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
                    cancel();
                    return;
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2744 Frost Spike!", NamedTextColor.AQUA));
    }

    // â”€â”€ #35 DEMONSLAYER: Holy Rend â€” +50% to Undead, 14 normal / 21 undead â”€â”€
    void rcDemonslayer(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.5f, 1.0f);
        p.getWorld().spawnParticle(Particle.END_ROD, c.add(0, 1, 0), 20, 2, 1, 2, 0.05);
        for (LivingEntity e : ctx.getNearbyEnemies(c, 5.0, p)) {
            boolean undead = e.getType() == EntityType.ZOMBIE || e.getType() == EntityType.SKELETON
                    || e.getType() == EntityType.WITHER_SKELETON || e.getType() == EntityType.PHANTOM
                    || e.getType() == EntityType.DROWNED || e.getType() == EntityType.HUSK
                    || e.getType() == EntityType.STRAY || e.getType() == EntityType.ZOMBIFIED_PIGLIN
                    || e.getType() == EntityType.ZOGLIN || e.getType() == EntityType.WITHER;
            ctx.dealDamage(p, e, undead ? 21.0 : 14.0);
        }
        p.sendActionBar(Component.text("\u2694 Holy Rend! +50% vs Undead", NamedTextColor.YELLOW));
    }

    // â”€â”€ #36 VENGEANCE: Retribution â€” Store damage for 8s, release as AoE â”€â”€
    void rcVengeance(Player p) {
        UUID uid = p.getUniqueId();
        ctx.retributionDamageStored.put(uid, 0.0);
        ctx.retributionExpiry.put(uid, System.currentTimeMillis() + 8000L);
        p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.8f, 1.5f);
        p.sendActionBar(Component.text("\u2694 Retribution active! Taking damage for 8s...", NamedTextColor.RED));
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            double stored = ctx.retributionDamageStored.getOrDefault(uid, 0.0);
            double release = stored * 1.5;
            ctx.retributionDamageStored.remove(uid);
            ctx.retributionExpiry.remove(uid);
            if (release > 0) {
                for (LivingEntity e : ctx.getNearbyEnemies(p.getLocation(), 6.0, p)) {
                    ctx.dealDamage(p, e, release);
                }
                p.getWorld().spawnParticle(Particle.EXPLOSION, p.getLocation().add(0, 1, 0), 5, 2, 1, 2, 0);
                p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 1.0f);
                p.sendActionBar(Component.text("\u2694 Retribution released! " + String.format("%.0f", release) + " damage!", NamedTextColor.RED).decorate(TextDecoration.BOLD));
            }
        }, 160L);
    }

    // â”€â”€ #37 OCULUS: All-Seeing Strike â€” teleport to nearest, 12 dmg â”€â”€
    void rcOculus(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 10.0);
        if (target == null) { p.sendActionBar(Component.text("No target within 10 blocks!", NamedTextColor.RED)); return; }
        p.getWorld().spawnParticle(Particle.REVERSE_PORTAL, p.getLocation(), 20, 0.5, 1, 0.5, 0.1);
        p.teleport(target.getLocation().subtract(target.getLocation().getDirection().normalize()));
        ctx.dealDamage(p, target, 12.0);
        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 1.0f);
        p.getWorld().spawnParticle(Particle.REVERSE_PORTAL, p.getLocation(), 20, 0.5, 1, 0.5, 0.1);
        p.sendActionBar(Component.text("\u2609 All-Seeing Strike!", NamedTextColor.LIGHT_PURPLE));
    }

    // â”€â”€ #38 ANCIENT GREATSLAB: Seismic Slam â€” 6-block AoE, 11 dmg + launch â”€â”€
    void rcAncientGreatslab(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.ITEM_MACE_SMASH_GROUND, 2.0f, 0.7f);
        p.getWorld().spawnParticle(Particle.BLOCK, c, 30, 3, 1, 3, 0.3, Material.STONE.createBlockData());
        for (LivingEntity e : ctx.getNearbyEnemies(c, 6.0, p)) {
            ctx.dealDamage(p, e, 11.0);
            e.setVelocity(new Vector(0, 1.0, 0));
        }
        p.sendActionBar(Component.text("\u2694 Seismic Slam!", NamedTextColor.GRAY));
    }

    // â”€â”€ #39 NEPTUNE'S FANG: Riptide Slash â€” pierce 4 entities, 8 dmg each â”€â”€
    void rcNeptunesFang(Player p) {
        Vector dir = p.getLocation().getDirection().normalize();
        Location start = p.getEyeLocation();
        p.playSound(start, Sound.ITEM_TRIDENT_RIPTIDE_3, 1.5f, 1.0f);
        new BukkitRunnable() {
            int tick = 0;
            int hits = 0;
            @Override public void run() {
                if (tick > 12 || hits >= 4) { cancel(); return; }
                Location point = start.clone().add(dir.clone().multiply(tick));
                p.getWorld().spawnParticle(Particle.BUBBLE, point, 5, 0.2, 0.2, 0.2, 0.05);
                p.getWorld().spawnParticle(Particle.SPLASH, point, 3, 0.1, 0.1, 0.1, 0);
                for (LivingEntity e : ctx.getNearbyEnemies(point, 1.5, p)) {
                    ctx.dealDamage(p, e, 8.0);
                    e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
                    hits++;
                    if (hits >= 4) { cancel(); return; }
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2263 Riptide Slash!", NamedTextColor.AQUA));
    }

    // â”€â”€ #40 TIDECALLER: Tidal Spear â€” 12 dmg + knockback + Conduit â”€â”€
    void rcTidecaller(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 8.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        ctx.dealDamage(p, target, 12.0);
        Vector kb = target.getLocation().toVector().subtract(p.getLocation().toVector()).normalize().multiply(2.0).setY(0.5);
        target.setVelocity(kb);
        p.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, 200, 0));
        p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_THROW, 1.5f, 0.8f);
        p.getWorld().spawnParticle(Particle.SPLASH, target.getLocation(), 30, 1, 1, 1, 0.1);
        p.sendActionBar(Component.text("\u2263 Tidal Spear! + Conduit Power", NamedTextColor.AQUA));
    }

    // â”€â”€ #41 STORMFORK: Lightning Javelin â€” lightning on impact, 14 dmg + AoE â”€â”€
    void rcStormfork(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 10.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        p.getWorld().strikeLightningEffect(target.getLocation());
        ctx.dealDamage(p, target, 14.0);
        for (LivingEntity e : ctx.getNearbyEnemies(target.getLocation(), 3.0, p)) {
            if (e != target) ctx.dealDamage(p, e, 7.0);
        }
        p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 1.0f);
        p.sendActionBar(Component.text("\u26A1 Lightning Javelin!", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
    }

    // â”€â”€ #42 JADE REAPER: Jade Crescent â€” 180 sweep 7b, 10 dmg + Poison â”€â”€
    void rcJadeReaper(Player p) {
        Location c = p.getLocation();
        Vector facing = c.getDirection().normalize();
        p.playSound(c, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 0.8f);
        p.getWorld().spawnParticle(Particle.COMPOSTER, c.add(0, 1, 0), 30, 3.5, 1, 3.5, 0);
        for (LivingEntity e : ctx.getNearbyEnemies(c, 7.0, p)) {
            Vector toE = e.getLocation().toVector().subtract(c.toVector()).normalize();
            if (facing.dot(toE) > 0) {
                ctx.dealDamage(p, e, 10.0);
                e.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 0));
            }
        }
        p.sendActionBar(Component.text("\u2618 Jade Crescent!", NamedTextColor.GREEN));
    }

    // â”€â”€ #43 VINDICATOR: Executioner's Chop â€” +1 dmg per missing heart (max +10) â”€â”€
    void rcVindicator(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 5.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        double maxHp = 20.0;
        if (target.getAttribute(Attribute.MAX_HEALTH) != null) maxHp = target.getAttribute(Attribute.MAX_HEALTH).getValue();
        double missing = maxHp - target.getHealth();
        double bonus = Math.min(10.0, missing / 2.0);
        double totalDmg = 11.0 + bonus;
        ctx.dealDamage(p, target, totalDmg);
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 2.0f, 0.8f);
        target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 15, 0.3, 0.3, 0.3, 0.2);
        p.sendActionBar(Component.text("\u2694 Executioner's Chop! +" + String.format("%.0f", bonus) + " bonus dmg", NamedTextColor.DARK_RED));
    }

    // â”€â”€ #44 SPIDER FANG: Web Trap â€” cobweb projectile, root 3s + Poison â”€â”€
    void rcSpiderFang(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 8.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 127)); // root
        target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 1));
        p.playSound(p.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 1.5f, 0.5f);
        target.getWorld().spawnParticle(Particle.BLOCK, target.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.1, Material.COBWEB.createBlockData());
        p.sendActionBar(Component.text("\u2620 Web Trap! Target rooted + poisoned", NamedTextColor.DARK_GREEN));
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  HOLD ABILITIES â€” ALL 44 WEAPONS

    // #45 DIVINE AXE RHITTA: Cruel Sun - massive fire AoE 10 blocks, 24 dmg
    void rcDivineAxeRhitta(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.ENTITY_BLAZE_SHOOT, 2.0f, 0.4f);
        p.playSound(c, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.5f, 0.6f);
        p.getWorld().spawnParticle(Particle.FLAME, c.clone().add(0, 3, 0), 100, 5, 3, 5, 0.15);
        p.getWorld().spawnParticle(Particle.LAVA, c.clone().add(0, 2, 0), 50, 4, 2, 4, 0);
        p.getWorld().spawnParticle(Particle.DUST, c.clone().add(0, 5, 0), 30, 3, 1, 3, 0, new Particle.DustOptions(Color.fromRGB(255, 165, 0), 3.0f));
        for (LivingEntity e : ctx.getNearbyEnemies(c, 10.0, p)) {
            ctx.dealDamage(p, e, 24.0);
            e.setFireTicks(200);
            e.setVelocity(e.getLocation().toVector().subtract(c.toVector()).normalize().multiply(1.5).setY(0.8));
        }
        p.sendActionBar(Component.text("\u2600 CRUEL SUN! \u2600", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // #46 YORU: World's Strongest Slash - 20-block line, 22 dmg, cuts through everything
    void rcYoru(Player p) {
        Vector dir = p.getLocation().getDirection().normalize();
        Location start = p.getEyeLocation();
        p.playSound(start, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 1.2f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick > 20) { cancel(); return; }
                Location point = start.clone().add(dir.clone().multiply(tick));
                p.getWorld().spawnParticle(Particle.SWEEP_ATTACK, point, 5, 0.3, 0.3, 0.3, 0);
                p.getWorld().spawnParticle(Particle.DUST, point, 8, 0.2, 0.2, 0.2, 0, new Particle.DustOptions(Color.fromRGB(20, 0, 50), 2.0f));
                for (LivingEntity e : ctx.getNearbyEnemies(point, 2.0, p)) { ctx.dealDamage(p, e, 22.0); }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2694 WORLD'S STRONGEST SLASH! \u2694", NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
    }

    // #47 TENGEN'S BLADE: Sound Breathing - 8 rapid slashes in cone, 4 dmg each
    void rcTengensBlade(Player p) {
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2.0f, 2.0f);
        Location c = p.getLocation().add(p.getLocation().getDirection().multiply(3));
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 8) { cancel(); return; }
                p.getWorld().spawnParticle(Particle.SWEEP_ATTACK, c.clone().add(Math.sin(tick * 0.8), 1, Math.cos(tick * 0.8)), 3, 0.5, 0.5, 0.5, 0);
                p.getWorld().spawnParticle(Particle.NOTE, c.clone().add(0, 2, 0), 5, 1, 0.5, 1, 1);
                for (LivingEntity e : ctx.getNearbyEnemies(c, 5.0, p)) { ctx.dealDamage(p, e, 4.0); }
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.5f, 1.5f + (tick * 0.1f));
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 2L);
        p.sendActionBar(Component.text("\u266B Sound Breathing! \u266B", NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD));
    }

    // #48 EDGE OF THE ASTRAL PLANE: Astral Rend - teleport 8 blocks forward, 20 dmg slash
    void rcEdgeAstralPlane(Player p) {
        Location origin = p.getLocation();
        Vector dir = origin.getDirection().normalize();
        Location target = origin.clone().add(dir.multiply(8));
        target.setY(p.getWorld().getHighestBlockYAt(target) + 1);
        p.teleport(target);
        p.playSound(target, Sound.ENTITY_ENDERMAN_TELEPORT, 2.0f, 0.8f);
        p.getWorld().spawnParticle(Particle.REVERSE_PORTAL, origin.add(0, 1, 0), 50, 0.5, 1, 0.5, 0.1);
        p.getWorld().spawnParticle(Particle.END_ROD, target.add(0, 1, 0), 80, 2, 2, 2, 0.1);
        for (LivingEntity e : ctx.getNearbyEnemies(target, 4.0, p)) { ctx.dealDamage(p, e, 20.0); }
        p.sendActionBar(Component.text("\u2727 Astral Rend! \u2727", NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD));
    }

    // #49 FALLEN GOD'S SPEAR: Divine Impale - single target 30 dmg + launch
    void rcFallenGodsSpear(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 8.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        ctx.dealDamage(p, target, 30.0);
        target.setVelocity(new Vector(0, 2.5, 0));
        p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 2.0f, 1.0f);
        p.getWorld().spawnParticle(Particle.END_ROD, target.getLocation().add(0, 1, 0), 60, 0.5, 1, 0.5, 0.2);
        p.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 40, 0.5, 0.5, 0.5, 0.3);
        p.sendActionBar(Component.text("\u2694 DIVINE IMPALE! \u2694", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
    }

    // #50 NATURE SWORD: Gaia's Wrath - 8-block AoE, 18 dmg + poison + roots (slowness IV)
    void rcNatureSword(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.BLOCK_GRASS_BREAK, 2.0f, 0.5f);
        p.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, c.add(0, 0.5, 0), 80, 4, 0.5, 4, 0);
        p.getWorld().spawnParticle(Particle.COMPOSTER, c, 40, 4, 1, 4, 0);
        for (LivingEntity e : ctx.getNearbyEnemies(c, 8.0, p)) {
            ctx.dealDamage(p, e, 18.0);
            e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 3));
            e.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
        }
        p.sendActionBar(Component.text("\u2618 Gaia's Wrath! \u2618", NamedTextColor.GREEN).decorate(TextDecoration.BOLD));
    }

    // #51 HEAVENLY PARTISAN: Holy Lance - 15-block beam, 20 dmg, heals allies 4 hearts
    void rcHeavenlyPartisan(Player p) {
        Vector dir = p.getLocation().getDirection().normalize();
        Location start = p.getEyeLocation();
        p.playSound(start, Sound.BLOCK_BEACON_ACTIVATE, 2.0f, 1.5f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick > 15) { cancel(); return; }
                Location point = start.clone().add(dir.clone().multiply(tick));
                p.getWorld().spawnParticle(Particle.END_ROD, point, 8, 0.2, 0.2, 0.2, 0);
                p.getWorld().spawnParticle(Particle.DUST, point, 5, 0.1, 0.1, 0.1, 0, new Particle.DustOptions(Color.fromRGB(255, 255, 200), 1.5f));
                for (LivingEntity e : ctx.getNearbyEnemies(point, 1.5, p)) { ctx.dealDamage(p, e, 20.0); }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        for (Entity e : p.getLocation().getWorld().getNearbyEntities(p.getLocation(), 8, 8, 8)) {
            if (e instanceof Player ally && ally != p && ctx.guildManager.areInSameGuild(p.getUniqueId(), ally.getUniqueId())) {
                double max = ally.getAttribute(Attribute.MAX_HEALTH).getValue();
                ally.setHealth(Math.min(max, ally.getHealth() + 8.0));
                ally.getWorld().spawnParticle(Particle.HEART, ally.getLocation().add(0, 2, 0), 5, 0.3, 0.3, 0.3, 0);
            }
        }
        p.sendActionBar(Component.text("\u2694 Holy Lance! \u2694", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
    }

    // #52 SOUL DEVOURER: Soul Rip - drain 20 dmg from target, heal 50%
    void rcSoulDevourer(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 6.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        ctx.dealDamage(p, target, 20.0);
        double heal = 10.0;
        double max = p.getAttribute(Attribute.MAX_HEALTH).getValue();
        p.setHealth(Math.min(max, p.getHealth() + heal));
        p.playSound(p.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 1.5f, 0.4f);
        p.getWorld().spawnParticle(Particle.SOUL, target.getLocation().add(0, 1, 0), 60, 0.5, 1, 0.5, 0.1);
        p.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, p.getLocation().add(0, 1, 0), 15, 0.3, 0.3, 0.3, 0);
        p.sendActionBar(Component.text("\u2620 Soul Rip! +5 hearts", NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
    }

    // #53 MJOLNIR: Thunderstrike - throw lightning at target location, 22 dmg AoE 6 blocks
    void rcMjolnir(Player p) {
        Location target = p.getLocation().add(p.getLocation().getDirection().multiply(6));
        p.getWorld().strikeLightningEffect(target);
        p.getWorld().strikeLightningEffect(target.clone().add(2, 0, 0));
        p.getWorld().strikeLightningEffect(target.clone().add(-2, 0, 0));
        p.playSound(target, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 0.7f);
        p.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, target.add(0, 1, 0), 80, 3, 2, 3, 0.2);
        for (LivingEntity e : ctx.getNearbyEnemies(target, 6.0, p)) {
            ctx.dealDamage(p, e, 22.0);
            e.setVelocity(new Vector(0, 1.0, 0));
        }
        p.sendActionBar(Component.text("\u26A1 THUNDERSTRIKE! \u26A1", NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
    }

    // #54 THOUSAND DEMON DAGGERS: Demon Barrage - 12 projectiles, 3 dmg each
    void rcThousandDemonDaggers(Player p) {
        p.playSound(p.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.5f, 1.5f);
        Vector baseDir = p.getLocation().getDirection().normalize();
        for (int i = -5; i <= 6; i++) {
            double angle = Math.toRadians(i * 8);
            Vector dir = ctx.rotateY(baseDir.clone(), angle);
            final Vector fDir = dir;
            new BukkitRunnable() {
                Location loc = p.getEyeLocation().clone();
                int ticks = 0;
                @Override public void run() {
                    if (ticks > 12) { cancel(); return; }
                    loc.add(fDir);
                    p.getWorld().spawnParticle(Particle.DUST, loc, 2, 0.05, 0.05, 0.05, 0, new Particle.DustOptions(Color.fromRGB(180, 0, 0), 0.8f));
                    for (LivingEntity e : ctx.getNearbyEnemies(loc, 0.8, p)) {
                        ctx.dealDamage(p, e, 3.0);
                        e.setFireTicks(40);
                        cancel(); return;
                    }
                    ticks++;
                }
            }.runTaskTimer(ctx.plugin, i + 6, 1L);
        }
        p.sendActionBar(Component.text("\u2620 Demon Barrage!", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
    }

    // #55 STAR EDGE: Cosmic Slash - 12-block line beam, 22 dmg, launches targets to sky
    void rcStarEdge(Player p) {
        Vector dir = p.getLocation().getDirection().normalize();
        Location start = p.getEyeLocation();
        p.playSound(start, Sound.BLOCK_BEACON_DEACTIVATE, 2.0f, 2.0f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick > 12) { cancel(); return; }
                Location point = start.clone().add(dir.clone().multiply(tick));
                p.getWorld().spawnParticle(Particle.END_ROD, point, 10, 0.3, 0.3, 0.3, 0.05);
                p.getWorld().spawnParticle(Particle.DUST, point, 5, 0.1, 0.1, 0.1, 0, new Particle.DustOptions(Color.fromRGB(200, 200, 255), 2.0f));
                for (LivingEntity e : ctx.getNearbyEnemies(point, 2.0, p)) {
                    ctx.dealDamage(p, e, 22.0);
                    e.setVelocity(new Vector(0, 3.0, 0));
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2726 Cosmic Slash! \u2726", NamedTextColor.WHITE).decorate(TextDecoration.BOLD));
    }

    // #56 RIVERS OF BLOOD: Corpse Piler - 6 rapid dashes in sequence, 5 dmg each
    void rcRiversOfBlood(Player p) {
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2.0f, 1.8f);
        new BukkitRunnable() {
            int dash = 0;
            @Override public void run() {
                if (dash >= 6) { cancel(); return; }
                p.setVelocity(p.getLocation().getDirection().multiply(1.2));
                Location loc = p.getLocation();
                p.getWorld().spawnParticle(Particle.DUST, loc.add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0, new Particle.DustOptions(Color.fromRGB(180, 0, 0), 1.5f));
                p.getWorld().spawnParticle(Particle.SWEEP_ATTACK, loc, 3, 0.3, 0.3, 0.3, 0);
                for (LivingEntity e : ctx.getNearbyEnemies(loc, 3.0, p)) { ctx.dealDamage(p, e, 5.0); }
                p.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 1.5f + (dash * 0.15f));
                dash++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 3L);
        p.sendActionBar(Component.text("\u2620 Corpse Piler!", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
    }

    // #57 DRAGON SLAYING BLADE: Dragon Pierce - massive single-target 35 dmg (2x vs dragon-type)
    void rcDragonSlayingBlade(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 6.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        double dmg = 35.0;
        if (target.getType() == EntityType.ENDER_DRAGON) dmg = 70.0;
        ctx.dealDamage(p, target, dmg);
        p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 2.0f, 1.2f);
        p.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 80, 0.5, 1, 0.5, 0.3);
        p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, target.getLocation().add(0, 1, 0), 40, 0.5, 1, 0.5, 0.2);
        p.sendActionBar(Component.text("\u2694 DRAGON PIERCE! \u2694", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // #58 STOP SIGN: Full Stop - stuns all enemies in 6-block radius for 3s, 15 dmg
    void rcStopSign(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.5f);
        p.getWorld().spawnParticle(Particle.DUST, c.add(0, 1, 0), 60, 3, 2, 3, 0, new Particle.DustOptions(Color.fromRGB(255, 0, 0), 3.0f));
        p.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, c, 2, 0, 0, 0, 0);
        for (LivingEntity e : ctx.getNearbyEnemies(c, 6.0, p)) {
            ctx.dealDamage(p, e, 15.0);
            e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 255));
            e.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 60, 255));
            e.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
        }
        p.sendActionBar(Component.text("\u26D4 FULL STOP! \u26D4", NamedTextColor.RED).decorate(TextDecoration.BOLD));
    }

    // #59 CREATION SPLITTER: Reality Cleave - 10-block cross AoE, 25 dmg
    void rcCreationSplitter(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 0.5f);
        p.playSound(c, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 0.3f);
        for (int i = -10; i <= 10; i++) {
            Location l1 = c.clone().add(i, 1, 0);
            Location l2 = c.clone().add(0, 1, i);
            p.getWorld().spawnParticle(Particle.END_ROD, l1, 3, 0.1, 0.1, 0.1, 0);
            p.getWorld().spawnParticle(Particle.END_ROD, l2, 3, 0.1, 0.1, 0.1, 0);
        }
        p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, c.add(0, 2, 0), 100, 2, 2, 2, 0.5);
        for (LivingEntity e : ctx.getNearbyEnemies(c, 10.0, p)) {
            ctx.dealDamage(p, e, 25.0);
            e.setVelocity(e.getLocation().toVector().subtract(c.toVector()).normalize().multiply(2.0).setY(1.0));
        }
        p.sendActionBar(Component.text("\u2726 REALITY CLEAVE! \u2726", NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD));
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  ABYSSAL TIER PRIMARY ABILITIES (4) â€” 200 particles per burst
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    // â”€â”€ #60 REQUIEM AWAKENED: Abyssal Devour â€” 12-block AoE soul drain, 40 dmg, full lifesteal, chain explosions â”€â”€
    void rcRequiemAwakened(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 0.3f);
        p.playSound(c, Sound.ENTITY_WITHER_SPAWN, 1.5f, 0.5f);
        // Initial soul-fire ring
        for (int i = 0; i < 36; i++) {
            double angle = Math.toRadians(i * 10);
            Location ring = c.clone().add(Math.cos(angle) * 6, 1, Math.sin(angle) * 6);
            p.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, ring, 6, 0.1, 0.3, 0.1, 0.02);
        }
        p.getWorld().spawnParticle(Particle.SOUL, c.clone().add(0, 1, 0), 100, 6, 3, 6, 0.1);
        p.getWorld().spawnParticle(Particle.SCULK_CHARGE_POP, c.clone().add(0, 1, 0), 100, 6, 2, 6, 0.05);
        double totalHealed = 0;
        List<LivingEntity> enemies = ctx.getNearbyEnemies(c, 12.0, p);
        for (LivingEntity e : enemies) {
            ctx.dealDamage(p, e, 40.0);
            e.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 200, 2));
            e.getWorld().spawnParticle(Particle.SOUL, e.getLocation().add(0, 1, 0), 20, 0.5, 0.8, 0.5, 0.05);
            totalHealed += 40.0;
            // Chain explosion on kill
            if (e.getHealth() <= 0 || e.isDead()) {
                Location deathLoc = e.getLocation();
                Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
                    deathLoc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, deathLoc.add(0, 1, 0), 50, 2.5, 1.5, 2.5, 0.1);
                    deathLoc.getWorld().spawnParticle(Particle.EXPLOSION, deathLoc, 3, 1, 1, 1, 0);
                    deathLoc.getWorld().playSound(deathLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);
                    for (LivingEntity nearby : ctx.getNearbyEnemies(deathLoc, 5.0, p)) {
                        ctx.dealDamage(p, nearby, 15.0);
                    }
                }, 5L);
            }
        }
        double maxHp = p.getAttribute(Attribute.MAX_HEALTH).getValue();
        p.setHealth(Math.min(maxHp, p.getHealth() + Math.min(totalHealed, maxHp)));
        p.sendActionBar(Component.text("\u2620 ABYSSAL DEVOUR! " + enemies.size() + " souls consumed!", TextColor.color(170, 0, 0)).decorate(TextDecoration.BOLD));
    }

    // â”€â”€ #61 EXCALIBUR AWAKENED: Divine Annihilation â€” 15-block chain beam, 35 dmg Ã— 8 targets, lightning, heals 4/hit, holy explosion â”€â”€
    void rcExcaliburAwakened(Player p) {
        p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 0.5f);
        p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.5f, 1.0f);
        Vector dir = p.getLocation().getDirection().normalize();
        Location start = p.getEyeLocation();
        // Holy beam phase
        new BukkitRunnable() {
            int tick = 0;
            final java.util.Set<java.util.UUID> hit = new java.util.HashSet<>();
            @Override public void run() {
                if (tick > 15) {
                    // Final holy explosion at beam end
                    Location end = start.clone().add(dir.clone().multiply(15));
                    end.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, end, 30, 5, 3, 5, 0.5);
                    end.getWorld().spawnParticle(Particle.END_ROD, end, 25, 5, 4, 5, 0.3);
                    end.getWorld().playSound(end, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.5f);
                    for (LivingEntity e : ctx.getNearbyEnemies(end, 10.0, p)) {
                        ctx.dealDamage(p, e, 25.0);
                    }
                    cancel();
                    return;
                }
                Location point = start.clone().add(dir.clone().multiply(tick));
                p.getWorld().spawnParticle(Particle.END_ROD, point, 15, 0.3, 0.3, 0.3, 0.05);
                p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, point, 10, 0.2, 0.2, 0.2, 0.02);
                for (LivingEntity e : ctx.getNearbyEnemies(point, 2.5, p)) {
                    if (hit.size() >= 8) break;
                    if (hit.add(e.getUniqueId())) {
                        ctx.dealDamage(p, e, 35.0);
                        e.getWorld().strikeLightningEffect(e.getLocation());
                        double maxHp = p.getAttribute(Attribute.MAX_HEALTH).getValue();
                        p.setHealth(Math.min(maxHp, p.getHealth() + 4.0));
                        e.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, e.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.2);
                    }
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2726 DIVINE ANNIHILATION! \u2726", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // â”€â”€ #62 CREATION SPLITTER AWAKENED: Reality Shatter â€” 3s charge, 80 true dmg 15-block line, launch + collapse â”€â”€
    void rcCreationSplitterAwakened(Player p) {
        p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 2.0f, 0.3f);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 127)); // root during charge
        p.setGlowing(true);
        // Charge phase â€” escalating particles
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 60) { cancel(); return; }
                int particleCount = 5 + (tick * 3);
                double radius = 0.5 + (tick * 0.03);
                p.getWorld().spawnParticle(Particle.REVERSE_PORTAL, p.getLocation().add(0, 1, 0), particleCount, radius, radius, radius, 0.1);
                p.getWorld().spawnParticle(Particle.SCULK_CHARGE_POP, p.getLocation().add(0, 1.5, 0), tick / 3, 0.3, 0.5, 0.3, 0.05);
                if (tick % 20 == 0) p.playSound(p.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1.5f, 0.5f + (tick * 0.015f));
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        // Release after 3 seconds
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            p.setGlowing(false);
            p.removePotionEffect(PotionEffectType.SLOWNESS);
            Vector dir = p.getLocation().getDirection().normalize();
            Location start = p.getEyeLocation();
            p.playSound(p.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 0.5f);
            p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.3f);
            final java.util.Set<java.util.UUID> launched = new java.util.HashSet<>();
            new BukkitRunnable() {
                int tick = 0;
                @Override public void run() {
                    if (tick > 15) { cancel(); return; }
                    Location point = start.clone().add(dir.clone().multiply(tick));
                    p.getWorld().spawnParticle(Particle.REVERSE_PORTAL, point, 30, 0.5, 0.5, 0.5, 0.2);
                    p.getWorld().spawnParticle(Particle.SCULK_CHARGE_POP, point, 20, 0.3, 0.3, 0.3, 0.1);
                    p.getWorld().spawnParticle(Particle.SONIC_BOOM, point, 1, 0, 0, 0, 0);
                    for (LivingEntity e : ctx.getNearbyEnemies(point, 2.5, p)) {
                        if (launched.add(e.getUniqueId())) {
                            e.damage(80.0); // true damage, bypasses armor
                            e.setVelocity(new Vector(0, 4.0, 0)); // launch 20 blocks up
                            e.getWorld().spawnParticle(Particle.REVERSE_PORTAL, e.getLocation(), 30, 0.5, 1, 0.5, 0.2);
                        }
                    }
                    tick++;
                }
            }.runTaskTimer(ctx.plugin, 0L, 1L);
            // Collapse phase after 2 seconds
            Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
                Location collapse = start.clone().add(dir.clone().multiply(8));
                collapse.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, collapse, 2, 3, 2, 3, 0);
                collapse.getWorld().spawnParticle(Particle.REVERSE_PORTAL, collapse, 30, 5, 5, 5, 0.5);
                collapse.getWorld().playSound(collapse, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.3f);
                for (LivingEntity e : ctx.getNearbyEnemies(collapse, 8.0, p)) {
                    ctx.dealDamage(p, e, 30.0);
                }
            }, 40L);
        }, 60L);
        p.sendActionBar(Component.text("\u2620 CHARGING REALITY SHATTER... \u2620", TextColor.color(170, 0, 0)).decorate(TextDecoration.BOLD));
    }

    // â”€â”€ #63 WHISPERWIND AWAKENED: Silent Storm â€” 30 invisible wind blades in 60 cone, 10 dmg each, pass through everything â”€â”€
    void rcWhisperwindAwakened(Player p) {
        p.playSound(p.getLocation(), Sound.ENTITY_BREEZE_SHOOT, 2.0f, 0.3f);
        p.playSound(p.getLocation(), Sound.ENTITY_BREEZE_WIND_BURST, 1.5f, 0.5f);
        Vector baseDir = p.getLocation().getDirection().normalize();
        for (int i = 0; i < 30; i++) {
            double spreadAngle = Math.toRadians((i - 15) * 2.0); // 60 degree cone
            double verticalOffset = (Math.random() - 0.5) * 0.3;
            Vector bladeDir = ctx.rotateY(baseDir.clone(), spreadAngle).setY(baseDir.getY() + verticalOffset).normalize();
            final Vector fDir = bladeDir;
            final int delay = i / 3; // stagger slightly
            Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
                new BukkitRunnable() {
                    Location loc = p.getEyeLocation().clone();
                    int ticks = 0;
                    @Override public void run() {
                        if (ticks > 20) { cancel(); return; }
                        loc.add(fDir.clone().multiply(1.5));
                        p.getWorld().spawnParticle(Particle.CLOUD, loc, 3, 0.05, 0.05, 0.05, 0);
                        p.getWorld().spawnParticle(Particle.SWEEP_ATTACK, loc, 1, 0, 0, 0, 0);
                        for (LivingEntity e : ctx.getNearbyEnemies(loc, 1.5, p)) {
                            ctx.dealDamage(p, e, 10.0);
                            e.getWorld().spawnParticle(Particle.CLOUD, e.getLocation().add(0, 1, 0), 8, 0.3, 0.5, 0.3, 0.05);
                        }
                        ticks++;
                    }
                }.runTaskTimer(ctx.plugin, 0L, 1L);
            }, delay);
        }
        p.getWorld().spawnParticle(Particle.CLOUD, p.getLocation().add(0, 1, 0), 60, 2, 1, 2, 0.3);
        p.sendActionBar(Component.text("\u2601 SILENT STORM! 30 blades unleashed!", TextColor.color(200, 200, 255)).decorate(TextDecoration.BOLD));
    }
}