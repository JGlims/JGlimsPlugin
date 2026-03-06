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

/**
 * LegendaryAltAbilities - all 59 alternate (crouch+right-click) ability implementations.
 * Phase 8d split from LegendaryAbilityListener.java (rule A.17).
 */
final class LegendaryAltAbilities {

    private final LegendaryAbilityContext ctx;

    LegendaryAltAbilities(LegendaryAbilityContext ctx) {
        this.ctx = ctx;
    }

    // ── #1 OCEAN'S RAGE: Riptide Surge — launch forward in water ──
    void holdOceansRage(Player p) {
        p.setVelocity(p.getLocation().getDirection().multiply(3.0));
        p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_3, 2.0f, 1.0f);
        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (ticks > 20) { cancel(); return; }
                p.getWorld().spawnParticle(Particle.SPLASH, p.getLocation(), 10, 0.3, 0.3, 0.3, 0.1);
                p.getWorld().spawnParticle(Particle.BUBBLE, p.getLocation(), 5, 0.2, 0.2, 0.2, 0.05);
                for (LivingEntity e : ctx.getNearbyEnemies(p.getLocation(), 2.0, p)) ctx.dealDamage(p, e, 8.0);
                ticks++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2248 RIPTIDE SURGE! \u2248", NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
    }

    // ── #2 AQUATIC SACRED BLADE: Depth Pressure — 10-block Slowness + Mining Fatigue ──
    void holdAquaticSacredBlade(Player p) {
        for (LivingEntity e : ctx.getNearbyEnemies(p.getLocation(), 10.0, p)) {
            e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 120, 2));
            e.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 120, 0));
        }
        p.playSound(p.getLocation(), Sound.AMBIENT_UNDERWATER_ENTER, 2.0f, 0.5f);
        p.getWorld().spawnParticle(Particle.BUBBLE, p.getLocation().add(0, 1, 0), 60, 5, 3, 5, 0.1);
        p.sendActionBar(Component.text("\u2248 Depth Pressure! Enemies weakened", NamedTextColor.DARK_AQUA));
    }

    // ── #3 TRUE EXCALIBUR: Divine Shield — 5s invulnerability + Strength II ──
    void holdTrueExcalibur(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 4)); // Resistance V = invulnerable
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100, 1));
        p.playSound(p.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.2f);
        p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, p.getLocation(), 50, 1, 2, 1, 0.3);
        p.sendActionBar(Component.text("\u2726 DIVINE SHIELD! 5s invulnerable + Strength II", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // ── #4 REQUIEM: Abyss Gate — Summon 3 wither skeletons for 15s ──
    void holdRequiemNinthAbyss(Player p) {
        p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.8f, 1.5f);
        for (int i = 0; i < 3; i++) {
            Location spawnLoc = p.getLocation().add(Math.random() * 3 - 1.5, 0, Math.random() * 3 - 1.5);
            WitherSkeleton ws = (WitherSkeleton) p.getWorld().spawnEntity(spawnLoc, EntityType.WITHER_SKELETON);
            ws.customName(Component.text("Abyss Servant", NamedTextColor.DARK_PURPLE));
            ws.setCustomNameVisible(true);
            ws.getEquipment().setItemInMainHand(new ItemStack(Material.STONE_SWORD));
            ws.setTarget(ctx.getTargetEntity(p, 15.0));
            p.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, spawnLoc, 10, 0.3, 0.5, 0.3, 0.03);
            Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> { if (!ws.isDead()) ws.remove(); }, 300L);
        }
        p.sendActionBar(Component.text("\u2620 ABYSS GATE! 3 Wither Skeletons summoned", NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
    }

    // ── #5 ROYAL CHAKRAM: Spinning Shield — 3s projectile deflection + 50% DR ──
    void holdRoyalChakram(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 2)); // ~50% DR
        p.playSound(p.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.5f, 1.2f);
        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (ticks > 60) { cancel(); return; }
                double angle = ticks * 0.3;
                Location loc = p.getLocation().add(Math.cos(angle) * 1.5, 1, Math.sin(angle) * 1.5);
                p.getWorld().spawnParticle(Particle.CRIT, loc, 3, 0.1, 0.1, 0.1, 0);
                ticks++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u25C6 Spinning Shield! 3s defense", NamedTextColor.GOLD));
    }

    // ── #6 BERSERKER'S GREATAXE: Blood Rage — +50% dmg, +30% speed, -30% def 10s ──
    void holdBerserkersGreataxe(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 2));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
        p.playSound(p.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 2.0f, 0.8f);
        p.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, p.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.1);
        p.sendActionBar(Component.text("\u2620 BLOOD RAGE! +Str III +Speed II for 10s", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
    }

    // ── #7 ACIDIC CLEAVER: Corrosive Aura — 6-block 1 heart/s for 8s ──
    void holdAcidicCleaver(Player p) {
        p.playSound(p.getLocation(), Sound.ENTITY_LLAMA_SPIT, 1.0f, 0.3f);
        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (ticks >= 160 || !p.isOnline()) { cancel(); return; }
                if (ticks % 20 == 0) {
                    for (LivingEntity e : ctx.getNearbyEnemies(p.getLocation(), 6.0, p)) {
                        e.damage(2.0);
                        e.getWorld().spawnParticle(Particle.ITEM_SLIME, e.getLocation().add(0, 1, 0), 5, 0.2, 0.2, 0.2, 0);
                    }
                }
                if (ticks % 5 == 0) {
                    p.getWorld().spawnParticle(Particle.ITEM_SLIME, p.getLocation().add(0, 0.5, 0), 8, 3, 0.5, 3, 0);
                }
                ticks++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2622 Corrosive Aura! 8s drain", NamedTextColor.GREEN));
    }

    // ── #8 BLACK IRON GREATSWORD: Iron Fortress — Absorption IV + Resistance II 8s ──
    void holdBlackIronGreatsword(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 160, 3));
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 160, 1));
        p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 2.0f, 0.8f);
        p.getWorld().spawnParticle(Particle.BLOCK, p.getLocation(), 30, 1, 1, 1, 0.1, Material.IRON_BLOCK.createBlockData());
        p.sendActionBar(Component.text("\u2694 IRON FORTRESS! Absorption IV + Resistance II", NamedTextColor.DARK_GRAY).decorate(TextDecoration.BOLD));
    }

    // ── #9 MURAMASA: Bloodlust — each kill +2 dmg, stacks 5x, 20s ──
    void holdMuramasa(Player p) {
        UUID uid = p.getUniqueId();
        ctx.bloodlustStacks.put(uid, 0);
        ctx.bloodlustExpiry.put(uid, System.currentTimeMillis() + 20000L);
        p.playSound(p.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1.5f, 1.5f);
        p.sendActionBar(Component.text("\u2620 Bloodlust active! Kill to stack damage", NamedTextColor.RED));
    }

    // ── #10 PHOENIX'S GRACE: Rebirth Flame — revive at 50% HP within 60s ──
    void holdPhoenixsGrace(Player p) {
        UUID uid = p.getUniqueId();
        ctx.rebornReady.put(uid, true);
        ctx.rebornExpiry.put(uid, System.currentTimeMillis() + 60000L);
        p.playSound(p.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 1.5f, 1.0f);
        p.getWorld().spawnParticle(Particle.FLAME, p.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.05);
        p.sendActionBar(Component.text("\u2600 Rebirth Flame ready! You will revive once within 60s", NamedTextColor.GOLD));
    }

    // ── #11 SOUL COLLECTOR: Spirit Army — release stored souls as projectiles ──
    void holdSoulCollector(Player p) {
        int souls = ctx.soulCount.getOrDefault(p.getUniqueId(), 0);
        if (souls <= 0) { p.sendActionBar(Component.text("No souls stored! Kill enemies first", NamedTextColor.RED)); return; }
        ctx.soulCount.put(p.getUniqueId(), 0);
        Vector dir = p.getLocation().getDirection().normalize();
        p.playSound(p.getLocation(), Sound.ENTITY_VEX_CHARGE, 1.5f, 0.5f);
        for (int i = 0; i < souls; i++) {
            final int delay = i * 4;
            Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
                Location start = p.getEyeLocation();
                new BukkitRunnable() {
                    Location loc = start.clone();
                    int tick = 0;
                    @Override public void run() {
                        if (tick > 15) { cancel(); return; }
                        loc.add(dir);
                        p.getWorld().spawnParticle(Particle.SOUL, loc, 3, 0.1, 0.1, 0.1, 0.01);
                        for (LivingEntity e : ctx.getNearbyEnemies(loc, 1.5, p)) {
                            ctx.dealDamage(p, e, 6.0);
                            cancel();
                            return;
                        }
                        tick++;
                    }
                }.runTaskTimer(ctx.plugin, 0L, 1L);
            }, delay);
        }
        p.sendActionBar(Component.text("\u2620 Spirit Army! " + souls + " souls released", NamedTextColor.DARK_PURPLE));
    }

    // ── #12 AMETHYST SHURIKEN: Shadow Step — teleport behind target + crit ──
    void holdAmethystShuriken(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 10.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        Location behind = target.getLocation().subtract(target.getLocation().getDirection().normalize().multiply(2));
        behind.setDirection(target.getLocation().toVector().subtract(behind.toVector()));
        p.getWorld().spawnParticle(Particle.SMOKE, p.getLocation(), 20, 0.5, 1, 0.5, 0.05);
        p.teleport(behind);
        ctx.dealDamage(p, target, p.getInventory().getItemInMainHand().getType() == Material.DIAMOND_SWORD ? 22.0 : 22.0);
        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 1.5f);
        p.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.2);
        p.sendActionBar(Component.text("\u2694 Shadow Step! Critical hit!", NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
    }

    // ── #13 VALHAKYRA: Wings of Valor — 8s slow-fall + Strength I ──
    void holdValhakyra(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 160, 0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 160, 0));
        p.setVelocity(new Vector(0, 1.0, 0));
        p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.5f, 1.5f);
        p.sendActionBar(Component.text("\u2694 Wings of Valor! Slow-fall + Strength I", NamedTextColor.GOLD));
    }

    // ── #14 WINDREAPER: Cyclone — 4s tornado pulls enemies + 4 dmg/s ──
    void holdWindreaper(Player p) {
        Location center = p.getLocation().clone();
        p.playSound(center, Sound.ENTITY_BREEZE_WIND_BURST, 2.0f, 0.5f);
        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (ticks >= 80 || !p.isOnline()) { cancel(); return; }
                for (int i = 0; i < 20; i++) {
                    double angle = (2 * Math.PI / 20) * i + ticks * 0.2;
                    double r = 3.0 + Math.sin(ticks * 0.1) * 0.5;
                    Location loc = center.clone().add(Math.cos(angle) * r, 0.5 + (ticks % 20) * 0.1, Math.sin(angle) * r);
                    p.getWorld().spawnParticle(Particle.CLOUD, loc, 1, 0, 0, 0, 0);
                }
                if (ticks % 20 == 0) {
                    for (LivingEntity e : ctx.getNearbyEnemies(center, 5.0, p)) {
                        e.damage(4.0);
                        Vector pull = center.toVector().subtract(e.getLocation().toVector()).normalize().multiply(0.5);
                        e.setVelocity(e.getVelocity().add(pull));
                    }
                }
                ticks++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2601 Cyclone! 4s tornado", NamedTextColor.WHITE));
    }

    // ── #15 PHANTOMGUARD: Phase Shift — 3s intangibility ──
    void holdPhantomguard(Player p) {
        ctx.phaseShiftActive.put(p.getUniqueId(), true);
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 4)); // invuln
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 0));
        p.playSound(p.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1.5f, 0.5f);
        p.getWorld().spawnParticle(Particle.REVERSE_PORTAL, p.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            ctx.phaseShiftActive.put(p.getUniqueId(), false);
            p.sendActionBar(Component.text("Phase Shift ended", NamedTextColor.GRAY));
        }, 60L);
        p.sendActionBar(Component.text("\u2620 PHASE SHIFT! 3s intangible", NamedTextColor.GRAY).decorate(TextDecoration.BOLD));
    }

    // ── #16 MOONLIGHT: Eclipse — 6s Blindness + Weakness to enemies in 15 blocks ──
    void holdMoonlight(Player p) {
        for (LivingEntity e : ctx.getNearbyEnemies(p.getLocation(), 15.0, p)) {
            e.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 120, 0));
            e.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 120, 1));
        }
        p.playSound(p.getLocation(), Sound.AMBIENT_CAVE, 2.0f, 0.3f);
        p.getWorld().spawnParticle(Particle.SMOKE, p.getLocation().add(0, 3, 0), 80, 7, 3, 7, 0.01);
        p.sendActionBar(Component.text("\u263D ECLIPSE! All enemies blinded + weakened", NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
    }

    // ── #17 ZENITH: Ascension — 10s Flight + 50% damage ──
    void holdZenith(Player p) {
        p.setAllowFlight(true);
        p.setFlying(true);
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 2));
        p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 2.0f, 1.0f);
        p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, p.getLocation(), 50, 1, 2, 1, 0.5);
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            if (p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR) {
                p.setFlying(false);
                p.setAllowFlight(false);
            }
            p.sendActionBar(Component.text("Ascension ended", NamedTextColor.GRAY));
        }, 200L);
        p.sendActionBar(Component.text("\u2726 ASCENSION! 10s Flight + Str III", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // ── #18 SOLSTICE: Daybreak — Remove negatives + Regen IV 6s ──
    void holdSolstice(Player p) {
        for (PotionEffect eff : p.getActivePotionEffects()) {
            PotionEffectType t = eff.getType();
            if (t.equals(PotionEffectType.POISON) || t.equals(PotionEffectType.WITHER)
                    || t.equals(PotionEffectType.SLOWNESS) || t.equals(PotionEffectType.WEAKNESS)
                    || t.equals(PotionEffectType.BLINDNESS) || t.equals(PotionEffectType.MINING_FATIGUE)
                    || t.equals(PotionEffectType.HUNGER) || t.equals(PotionEffectType.NAUSEA)) {
                p.removePotionEffect(t);
            }
        }
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 120, 3));
        p.playSound(p.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.5f, 1.5f);
        p.getWorld().spawnParticle(Particle.END_ROD, p.getLocation().add(0, 1, 0), 30, 1, 2, 1, 0.05);
        p.sendActionBar(Component.text("\u2600 Daybreak! Cleansed + Regen IV", NamedTextColor.YELLOW));
    }

    // ── #19 GRAND CLAYMORE: Colossus Stance — 6s KB-immune, +range, +40% dmg ──
    void holdGrandClaymore(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 120, 1));
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 120, 1));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 120, 0)); // rooted feel
        p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 2.0f, 0.6f);
        p.getWorld().spawnParticle(Particle.BLOCK, p.getLocation(), 30, 1, 0, 1, 0.1, Material.IRON_BLOCK.createBlockData());
        p.sendActionBar(Component.text("\u2694 COLOSSUS STANCE! 6s KB-immune + Str II", NamedTextColor.WHITE).decorate(TextDecoration.BOLD));
    }

    // ── #20 CALAMITY BLADE: Doomsday — 8s double damage, -1 heart/s ──
    void holdCalamityBlade(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 160, 3));
        p.playSound(p.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 1.5f, 0.5f);
        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (ticks >= 160 || !p.isOnline()) { cancel(); return; }
                if (ticks % 20 == 0 && p.getHealth() > 2.0) {
                    p.setHealth(p.getHealth() - 2.0);
                    p.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, p.getLocation().add(0, 1, 0), 3, 0.2, 0.2, 0.2, 0);
                }
                ticks++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2622 DOOMSDAY! 8s Str IV but losing 1 heart/s", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
    }

    // ── #21 DRAGON SWORD: Draconic Roar — 8-block Fear + Weakness ──
    void holdDragonSword(Player p) {
        for (LivingEntity e : ctx.getNearbyEnemies(p.getLocation(), 8.0, p)) {
            Vector away = e.getLocation().toVector().subtract(p.getLocation().toVector()).normalize().multiply(1.5).setY(0.3);
            e.setVelocity(away);
            e.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));
        }
        p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.8f);
        p.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, p.getLocation().add(0, 1, 0), 40, 4, 2, 4, 0.05);
        p.sendActionBar(Component.text("\u2620 DRACONIC ROAR! Enemies fleeing", NamedTextColor.RED).decorate(TextDecoration.BOLD));
    }

    // ── #22 TALONBRAND: Predator's Mark — Target takes +30% for 10s ──
    void holdTalonbrand(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 10.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        ctx.predatorMarkTarget.put(p.getUniqueId(), target.getUniqueId());
        ctx.predatorMarkExpiry.put(p.getUniqueId(), System.currentTimeMillis() + 10000L);
        target.setGlowing(true);
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> target.setGlowing(false), 200L);
        p.playSound(p.getLocation(), Sound.ENTITY_WOLF_GROWL, 1.5f, 0.5f);
        p.sendActionBar(Component.text("\u2694 Predator's Mark! Target takes +30% for 10s", NamedTextColor.DARK_RED));
    }

    // ── #23 EMERALD GREATCLEAVER: Gem Barrier — Absorb next 3 hits, 15s ──
    void holdEmeraldGreatcleaver(Player p) {
        ctx.gemBarrierCharges.put(p.getUniqueId(), 3);
        ctx.gemBarrierExpiry.put(p.getUniqueId(), System.currentTimeMillis() + 15000L);
        p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 2.0f, 1.0f);
        p.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, p.getLocation().add(0, 1, 0), 30, 1, 1, 1, 0);
        p.sendActionBar(Component.text("\u2666 Gem Barrier! 3 hit charges for 15s", NamedTextColor.GREEN));
    }

    // ── #24 DEMON'S BLOOD BLADE: Demonic Form — 10s +60% dmg + fire trail, -50% def ──
    void holdDemonsBloodBlade(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 3));
        p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.5f);
        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (ticks >= 200 || !p.isOnline()) { cancel(); return; }
                if (ticks % 3 == 0) {
                    p.getWorld().spawnParticle(Particle.FLAME, p.getLocation(), 5, 0.3, 0, 0.3, 0.02);
                    p.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, p.getLocation(), 3, 0.2, 0, 0.2, 0.01);
                }
                ticks++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2620 DEMONIC FORM! 10s Str IV + fire trail", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
    }

    // ═══════════ UNCOMMON HOLD ABILITIES (20) ═══════════

    // ── #25 NOCTURNE: Night Cloak — 6s Invis + next hit +8 bonus ──
    void holdNocturne(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 120, 0));
        p.playSound(p.getLocation(), Sound.ENTITY_PHANTOM_AMBIENT, 1.0f, 0.5f);
        p.getWorld().spawnParticle(Particle.SMOKE, p.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.02);
        p.sendActionBar(Component.text("\u263D Night Cloak! Invisible + next hit +8 dmg", NamedTextColor.DARK_GRAY));
    }

    // ── #26 GRAVESCEPTER: Death's Grasp — Root target 3s ──
    void holdGravescepter(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 8.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 127));
        p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1.0f, 0.5f);
        target.getWorld().spawnParticle(Particle.SOUL, target.getLocation(), 20, 0.3, 0.5, 0.3, 0.03);
        p.sendActionBar(Component.text("\u2620 Death's Grasp! Target rooted 3s", NamedTextColor.DARK_GRAY));
    }

    // ── #27 LYCANBANE: Hunter's Sense — 10s Glowing on all within 20 blocks ──
    void holdLycanbane(Player p) {
        List<LivingEntity> entities = ctx.getNearbyEnemies(p.getLocation(), 20.0, p);
        for (LivingEntity e : entities) {
            e.setGlowing(true);
            Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> e.setGlowing(false), 200L);
        }
        p.playSound(p.getLocation(), Sound.ENTITY_WOLF_AMBIENT, 2.0f, 0.8f);
        p.sendActionBar(Component.text("\u2694 Hunter's Sense! " + entities.size() + " entities revealed", NamedTextColor.WHITE));
    }

    // ── #28 GLOOMSTEEL KATANA: Shadow Stance — 5s 25% dodge ──
    void holdGloomsteelKatana(Player p) {
        ctx.shadowStanceActive.put(p.getUniqueId(), true);
        ctx.shadowStanceExpiry.put(p.getUniqueId(), System.currentTimeMillis() + 5000L);
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 2.0f);
        p.getWorld().spawnParticle(Particle.SMOKE, p.getLocation().add(0, 1, 0), 15, 0.5, 1, 0.5, 0.02);
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> ctx.shadowStanceActive.put(p.getUniqueId(), false), 100L);
        p.sendActionBar(Component.text("\u2694 Shadow Stance! 25% dodge for 5s", NamedTextColor.DARK_GRAY));
    }

    // ── #29 VIRIDIAN CLEAVER: Overgrowth — vine DoT + root ──
    void holdViridianCleaver(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 8.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 127)); // root 2s
        p.playSound(p.getLocation(), Sound.BLOCK_AZALEA_LEAVES_BREAK, 1.5f, 0.8f);
        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (ticks >= 6 || target.isDead()) { cancel(); return; }
                target.damage(2.0);
                target.getWorld().spawnParticle(Particle.COMPOSTER, target.getLocation().add(0, 1, 0), 8, 0.3, 0.3, 0.3, 0);
                ticks++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 20L);
        p.sendActionBar(Component.text("\u2618 Overgrowth! Target rooted + DoT", NamedTextColor.GREEN));
    }

    // ── #30 CRESCENT EDGE: Crescent Guard — parry next melee hit, reflect 100% ──
    void holdCrescentEdge(Player p) {
        ctx.crescentParryActive.put(p.getUniqueId(), true);
        p.playSound(p.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.5f, 0.8f);
        p.getWorld().spawnParticle(Particle.ENCHANT, p.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.1);
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            if (ctx.crescentParryActive.getOrDefault(p.getUniqueId(), false)) {
                                ctx.crescentParryActive.put(p.getUniqueId(), false);
                p.sendActionBar(Component.text("Crescent Guard expired", NamedTextColor.GRAY));
            }
        }, 80L); // 4s window
        p.sendActionBar(Component.text("\u263D Crescent Guard! Parry next hit", NamedTextColor.YELLOW));
    }

    // ── #31 GRAVECLEAVER: Undying Rage — 8s: survive lethal at 1 HP once ──
    void holdGravecleaver(Player p) {
        UUID uid = p.getUniqueId();
        ctx.undyingRageActive.put(uid, true);
        ctx.undyingRageExpiry.put(uid, System.currentTimeMillis() + 8000L);
        p.playSound(p.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 1.5f, 0.5f);
        p.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, p.getLocation().add(0, 1, 0), 15, 0.5, 1, 0.5, 0.1);
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            if (ctx.undyingRageActive.getOrDefault(uid, false)) {
                ctx.undyingRageActive.put(uid, false);
                p.sendActionBar(Component.text("Undying Rage expired", NamedTextColor.GRAY));
            }
        }, 160L);
        p.sendActionBar(Component.text("\u2620 UNDYING RAGE! Survive lethal hit once for 8s", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
    }

    // ── #32 AMETHYST GREATBLADE: Gem Resonance — 8s Strength I to allies in 10 blocks ──
    void holdAmethystGreatblade(Player p) {
        p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 2.0f, 1.2f);
        p.getWorld().spawnParticle(Particle.END_ROD, p.getLocation().add(0, 1, 0), 30, 5, 2, 5, 0.02);
        for (Entity e : p.getWorld().getNearbyEntities(p.getLocation(), 10, 10, 10)) {
            if (e instanceof Player ally && !ally.getUniqueId().equals(p.getUniqueId())) {
                if (ctx.guildManager.areInSameGuild(p.getUniqueId(), ally.getUniqueId())) {
                    ally.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 160, 0));
                    ally.sendActionBar(Component.text("\u2666 Gem Resonance from " + p.getName() + "!", NamedTextColor.LIGHT_PURPLE));
                }
            }
        }
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 160, 0));
        p.sendActionBar(Component.text("\u2666 Gem Resonance! Strength I to nearby allies", NamedTextColor.LIGHT_PURPLE));
    }

    // ── #33 FLAMBERGE: Ember Shield — 5s: attackers take 4 fire dmg ──
    void holdFlamberge(Player p) {
        UUID uid = p.getUniqueId();
        ctx.emberShieldActive.put(uid, true);
        ctx.emberShieldExpiry.put(uid, System.currentTimeMillis() + 5000L);
        p.playSound(p.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 1.5f, 1.0f);
        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (ticks >= 100 || !p.isOnline()) {
                    ctx.emberShieldActive.put(uid, false);
                    cancel();
                    return;
                }
                if (ticks % 5 == 0) {
                    double angle = ticks * 0.3;
                    Location loc = p.getLocation().add(Math.cos(angle) * 1.2, 1, Math.sin(angle) * 1.2);
                    p.getWorld().spawnParticle(Particle.FLAME, loc, 2, 0.05, 0.05, 0.05, 0);
                }
                ticks++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2600 Ember Shield! 5s fire reflect", NamedTextColor.RED));
    }

    // ── #34 CRYSTAL FROSTBLADE: Permafrost — 5-block AoE Slow II + Mining Fatigue 6s ──
    void holdCrystalFrostblade(Player p) {
        p.playSound(p.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.5f, 0.5f);
        p.getWorld().spawnParticle(Particle.SNOWFLAKE, p.getLocation().add(0, 1, 0), 50, 2.5, 1.5, 2.5, 0.02);
        for (LivingEntity e : ctx.getNearbyEnemies(p.getLocation(), 5.0, p)) {
            e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 120, 1));
            e.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 120, 0));
            e.getWorld().spawnParticle(Particle.SNOWFLAKE, e.getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0);
        }
        p.sendActionBar(Component.text("\u2744 Permafrost! Enemies frozen", NamedTextColor.AQUA));
    }

    // ── #35 DEMONSLAYER: Purifying Aura — 6s: undead in 8 blocks take 2 dmg/s ──
    void holdDemonslayer(Player p) {
        p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.5f, 1.5f);
        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (ticks >= 120 || !p.isOnline()) { cancel(); return; }
                if (ticks % 20 == 0) {
                    for (LivingEntity e : ctx.getNearbyEnemies(p.getLocation(), 8.0, p)) {
                        boolean undead = e.getType() == EntityType.ZOMBIE || e.getType() == EntityType.SKELETON
                                || e.getType() == EntityType.WITHER_SKELETON || e.getType() == EntityType.PHANTOM
                                || e.getType() == EntityType.DROWNED || e.getType() == EntityType.HUSK
                                || e.getType() == EntityType.STRAY || e.getType() == EntityType.ZOMBIFIED_PIGLIN
                                || e.getType() == EntityType.ZOGLIN || e.getType() == EntityType.WITHER;
                        if (undead) {
                            e.damage(4.0);
                            e.getWorld().spawnParticle(Particle.END_ROD, e.getLocation().add(0, 1, 0), 5, 0.2, 0.3, 0.2, 0.02);
                        }
                    }
                }
                if (ticks % 10 == 0) {
                    p.getWorld().spawnParticle(Particle.END_ROD, p.getLocation().add(0, 0.5, 0), 10, 4, 0.5, 4, 0.01);
                }
                ticks++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2694 Purifying Aura! 6s undead burn", NamedTextColor.YELLOW));
    }

    // ── #36 VENGEANCE: Grudge Mark — Target takes +20% dmg from you for 15s ──
    void holdVengeance(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 10.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        UUID uid = p.getUniqueId();
        ctx.grudgeTargets.put(uid, target.getUniqueId());
        ctx.grudgeExpiry.put(uid, System.currentTimeMillis() + 15000L);
        target.setGlowing(true);
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            target.setGlowing(false);
            ctx.grudgeTargets.remove(uid);
            ctx.grudgeExpiry.remove(uid);
        }, 300L);
        p.playSound(p.getLocation(), Sound.ENTITY_WARDEN_ANGRY, 1.0f, 1.5f);
        target.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, target.getLocation().add(0, 2, 0), 10, 0.3, 0.3, 0.3, 0);
        p.sendActionBar(Component.text("\u2694 Grudge Mark! +20% dmg to target for 15s", NamedTextColor.RED));
    }

    // ── #37 OCULUS: Third Eye — 10s Glowing on all entities in 30 blocks ──
    void holdOculus(Player p) {
        List<LivingEntity> entities = ctx.getNearbyEnemies(p.getLocation(), 30.0, p);
        for (LivingEntity e : entities) {
            e.setGlowing(true);
            Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> e.setGlowing(false), 200L);
        }
        p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 200, 0));
        p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 2.0f);
        p.getWorld().spawnParticle(Particle.ENCHANT, p.getLocation().add(0, 2, 0), 40, 2, 2, 2, 0.5);
        p.sendActionBar(Component.text("\u2609 Third Eye! " + entities.size() + " entities revealed + Night Vision", NamedTextColor.LIGHT_PURPLE));
    }

    // ── #38 ANCIENT GREATSLAB: Stone Skin — 6s Resistance II + KB-immune ──
    void holdAncientGreatslab(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 120, 1));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 120, 0)); // weight feel
        p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 0.5f);
        p.getWorld().spawnParticle(Particle.BLOCK, p.getLocation(), 40, 1, 1, 1, 0.1, Material.STONE.createBlockData());
        p.sendActionBar(Component.text("\u2694 Stone Skin! 6s Resistance II", NamedTextColor.GRAY));
    }

    // ── #39 NEPTUNE'S FANG: Maelstrom — 6-block water vortex, pull + 3 dmg/s 5s ──
    void holdNeptunesFang(Player p) {
        Location center = p.getLocation().clone();
        p.playSound(center, Sound.ENTITY_GENERIC_SPLASH, 2.0f, 0.5f);
        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (ticks >= 100 || !p.isOnline()) { cancel(); return; }
                // Vortex particles
                for (int i = 0; i < 12; i++) {
                    double angle = (2 * Math.PI / 12) * i + ticks * 0.15;
                    double r = 3.0 + Math.sin(ticks * 0.1) * 1.0;
                    double y = 0.3 + (ticks % 40) * 0.05;
                    Location loc = center.clone().add(Math.cos(angle) * r, y, Math.sin(angle) * r);
                    p.getWorld().spawnParticle(Particle.SPLASH, loc, 2, 0.1, 0.1, 0.1, 0);
                    p.getWorld().spawnParticle(Particle.BUBBLE, loc, 1, 0.1, 0.1, 0.1, 0);
                }
                // Damage + pull every second
                if (ticks % 20 == 0) {
                    for (LivingEntity e : ctx.getNearbyEnemies(center, 6.0, p)) {
                        e.damage(6.0);
                        Vector pull = center.toVector().subtract(e.getLocation().toVector()).normalize().multiply(0.6).setY(0.2);
                        e.setVelocity(e.getVelocity().add(pull));
                    }
                    p.getWorld().playSound(center, Sound.ENTITY_GENERIC_SPLASH, 1.0f, 0.8f);
                }
                ticks++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2248 MAELSTROM! 5s water vortex", NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
    }

    // ── #40 TIDECALLER: Depth Ward — 8s Dolphin's Grace + Respiration + Drowned-immune ──
    void holdTidecaller(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 160, 0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 160, 0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, 160, 0));
        p.playSound(p.getLocation(), Sound.AMBIENT_UNDERWATER_ENTER, 1.5f, 1.0f);
        p.getWorld().spawnParticle(Particle.BUBBLE, p.getLocation().add(0, 1, 0), 40, 1, 2, 1, 0.1);
        p.sendActionBar(Component.text("\u2248 Depth Ward! 8s Dolphin's Grace + Water Breathing", NamedTextColor.AQUA));
    }

    // ── #41 STORMFORK: Thunder Shield — 6s: attackers get lightning ──
    void holdStormfork(Player p) {
        UUID uid = p.getUniqueId();
        ctx.thunderShieldActive.put(uid, true);
        ctx.thunderShieldExpiry.put(uid, System.currentTimeMillis() + 6000L);
        p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.5f, 1.5f);
        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (ticks >= 120 || !p.isOnline()) {
                    ctx.thunderShieldActive.put(uid, false);
                    cancel();
                    return;
                }
                if (ticks % 10 == 0) {
                    double angle = ticks * 0.3;
                    Location loc = p.getLocation().add(Math.cos(angle) * 1.5, 1.5, Math.sin(angle) * 1.5);
                    p.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, loc, 3, 0.1, 0.1, 0.1, 0);
                }
                ticks++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u26A1 Thunder Shield! 6s lightning counter", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
    }

    // ── #42 JADE REAPER: Emerald Harvest — 10s: kills drop 1-3 emeralds ──
    void holdJadeReaper(Player p) {
        p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.5f, 0.8f);
        p.getWorld().spawnParticle(Particle.COMPOSTER, p.getLocation().add(0, 1, 0), 20, 1, 1, 1, 0.05);
        // Store activation time — checked in a kill listener
        // For simplicity, use a potion effect as a marker
        p.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 200, 0));
        p.sendActionBar(Component.text("\u2618 Emerald Harvest! Kills drop emeralds for 10s", NamedTextColor.GREEN));
    }

    // ── #43 VINDICATOR: Rally Cry — 6s: allies in 8 blocks get Speed I + Strength I ──
    void holdVindicator(Player p) {
        p.playSound(p.getLocation(), Sound.ENTITY_RAVAGER_CELEBRATE, 2.0f, 1.2f);
        p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, p.getLocation().add(0, 1, 0), 20, 2, 2, 2, 0.1);
        for (Entity e : p.getWorld().getNearbyEntities(p.getLocation(), 8, 8, 8)) {
            if (e instanceof Player ally) {
                if (ally.getUniqueId().equals(p.getUniqueId()) ||
                        ctx.guildManager.areInSameGuild(p.getUniqueId(), ally.getUniqueId())) {
                    ally.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 120, 0));
                    ally.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 120, 0));
                    if (!ally.getUniqueId().equals(p.getUniqueId())) {
                        ally.sendActionBar(Component.text("\u2694 Rally Cry from " + p.getName() + "!", NamedTextColor.GOLD));
                    }
                }
            }
        }
        p.sendActionBar(Component.text("\u2694 RALLY CRY! Allies buffed for 6s", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // ── #44 SPIDER FANG: Wall Crawler — 8s levitation near blocks + Night Vision ──
    void holdSpiderFang(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 160, 0));
        p.playSound(p.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 1.5f, 1.0f);
        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (ticks >= 160 || !p.isOnline()) { cancel(); return; }
                // Check if player is against a wall
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
                    // Hold player in place against wall (anti-gravity)
                    p.setVelocity(new Vector(0, 0.05, 0));
                    if (ticks % 5 == 0) {
                        p.getWorld().spawnParticle(Particle.BLOCK, feet, 3, 0.2, 0, 0.2, 0, Material.COBWEB.createBlockData());
                    }
                }
                ticks++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2620 Wall Crawler! 8s wall-climb (sneak near walls) + Night Vision", NamedTextColor.DARK_GREEN));
    }

    // ════════════════════════════════════════════════════════════════
    //  HELPER METHODS
    // ════════════════════════════════════════════════════════════════

    // #45 DIVINE AXE RHITTA ALT: Sunshine - buff: +50% damage for 10s, fire aura
    void altDivineAxeRhitta(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 1));
        p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 200, 0));
        p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 2.0f, 1.0f);
        p.getWorld().spawnParticle(Particle.FLAME, p.getLocation().add(0, 1, 0), 80, 1.5, 2, 1.5, 0.1);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 200 || !p.isOnline()) { cancel(); return; }
                if (tick % 5 == 0) p.getWorld().spawnParticle(Particle.FLAME, p.getLocation().add(0, 1, 0), 5, 0.5, 0.5, 0.5, 0.02);
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2600 SUNSHINE! +Strength II for 10s", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // #46 YORU ALT: Dark Mirror - teleport behind target, 15 dmg backstab
    void altYoru(Player p) {
        LivingEntity target = ctx.getTargetEntity(p, 10.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        Location behind = target.getLocation().clone().add(target.getLocation().getDirection().normalize().multiply(-2));
        behind.setYaw(target.getLocation().getYaw());
        behind.setPitch(0);
        p.teleport(behind);
        ctx.dealDamage(p, target, 15.0);
        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 2.0f, 0.5f);
        p.getWorld().spawnParticle(Particle.SMOKE, p.getLocation().add(0, 1, 0), 40, 0.5, 1, 0.5, 0.05);
        p.sendActionBar(Component.text("\u2694 Dark Mirror! Backstab!", NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
    }

    // #47 TENGEN'S BLADE ALT: Constant Flux - speed III + haste II for 12s
    void altTengensBlade(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 240, 2));
        p.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 240, 1));
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 2.0f, 2.0f);
        p.getWorld().spawnParticle(Particle.NOTE, p.getLocation().add(0, 2, 0), 30, 1, 0.5, 1, 1);
        p.sendActionBar(Component.text("\u266B Constant Flux! Speed III + Haste II", NamedTextColor.LIGHT_PURPLE));
    }

    // #48 EDGE OF THE ASTRAL PLANE ALT: Planar Shift - phase through 10 blocks, invulnerable
    void altEdgeAstralPlane(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 3));
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 4));
        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 1.5f);
        p.getWorld().spawnParticle(Particle.REVERSE_PORTAL, p.getLocation().add(0, 1, 0), 60, 0.5, 1, 0.5, 0.1);
        p.sendActionBar(Component.text("\u2727 Planar Shift! 3s invulnerability", NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD));
    }

    // #49 FALLEN GOD'S SPEAR ALT: Heaven's Fall - leap up + slam, 20 dmg AoE 8 blocks
    void altFallenGodsSpear(Player p) {
        p.setVelocity(new Vector(0, 3.0, 0));
        p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 2.0f, 0.8f);
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            Location land = p.getLocation();
            p.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, land, 5, 0, 0, 0, 0);
            p.getWorld().spawnParticle(Particle.END_ROD, land, 80, 4, 1, 4, 0.2);
            p.playSound(land, Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.5f);
            for (LivingEntity e : ctx.getNearbyEnemies(land, 8.0, p)) {
                ctx.dealDamage(p, e, 20.0);
                e.setVelocity(e.getLocation().toVector().subtract(land.toVector()).normalize().multiply(2.0).setY(1.2));
            }
        }, 30L);
        p.sendActionBar(Component.text("\u2694 Heaven's Fall! \u2694", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
    }

    // #50 NATURE SWORD ALT: Overgrowth Surge - heal 8 hearts + regen III 10s + ally heal
    void altNatureSword(Player p) {
        double max = p.getAttribute(Attribute.MAX_HEALTH).getValue();
        p.setHealth(Math.min(max, p.getHealth() + 16.0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 2));
        p.playSound(p.getLocation(), Sound.BLOCK_GRASS_BREAK, 2.0f, 1.2f);
        p.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, p.getLocation().add(0, 1, 0), 60, 2, 1, 2, 0);
        p.getWorld().spawnParticle(Particle.HEART, p.getLocation().add(0, 2, 0), 15, 1, 0.5, 1, 0);
        for (Entity e : p.getLocation().getWorld().getNearbyEntities(p.getLocation(), 6, 6, 6)) {
            if (e instanceof Player ally && ally != p && ctx.guildManager.areInSameGuild(p.getUniqueId(), ally.getUniqueId())) {
                double amax = ally.getAttribute(Attribute.MAX_HEALTH).getValue();
                ally.setHealth(Math.min(amax, ally.getHealth() + 8.0));
                ally.getWorld().spawnParticle(Particle.HEART, ally.getLocation().add(0, 2, 0), 5, 0.3, 0.3, 0.3, 0);
            }
        }
        p.sendActionBar(Component.text("\u2618 Overgrowth Surge! Full heal + Regen III", NamedTextColor.GREEN).decorate(TextDecoration.BOLD));
    }

    // #51 HEAVENLY PARTISAN ALT: Celestial Judgment - smite 3 lightning bolts on nearest enemies
    void altHeavenlyPartisan(Player p) {
        List<LivingEntity> targets = ctx.getNearbyEnemies(p.getLocation(), 12.0, p);
        int strikes = Math.min(3, targets.size());
        for (int i = 0; i < strikes; i++) {
            LivingEntity t = targets.get(i);
            final int delay = i * 10;
            Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
                p.getWorld().strikeLightningEffect(t.getLocation());
                ctx.dealDamage(p, t, 18.0);
                t.getWorld().spawnParticle(Particle.END_ROD, t.getLocation().add(0, 1, 0), 30, 0.5, 2, 0.5, 0.1);
            }, delay);
        }
        p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 0.8f);
        p.sendActionBar(Component.text("\u2694 Celestial Judgment! " + strikes + " smites", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
    }

    // #52 SOUL DEVOURER ALT: Devouring Maw - AoE drain 12 dmg, heal per enemy hit
    void altSoulDevourer(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.ENTITY_WITHER_HURT, 1.5f, 0.5f);
        p.getWorld().spawnParticle(Particle.SOUL, c.add(0, 1, 0), 80, 4, 2, 4, 0.05);
        int hits = 0;
        for (LivingEntity e : ctx.getNearbyEnemies(c, 6.0, p)) {
            ctx.dealDamage(p, e, 12.0);
            hits++;
        }
        double heal = hits * 4.0;
        double max = p.getAttribute(Attribute.MAX_HEALTH).getValue();
        p.setHealth(Math.min(max, p.getHealth() + heal));
        p.sendActionBar(Component.text("\u2620 Devouring Maw! Healed " + (hits * 2) + " hearts", NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
    }

    // #53 MJOLNIR ALT: Bifrost Slam - leap + triple lightning on landing
    void altMjolnir(Player p) {
        p.setVelocity(new Vector(0, 2.5, 0).add(p.getLocation().getDirection().multiply(1.0)));
        p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 2.0f, 0.8f);
        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            Location land = p.getLocation();
            for (int i = 0; i < 3; i++) {
                Location strike = land.clone().add(Math.sin(i * 2.1) * 3, 0, Math.cos(i * 2.1) * 3);
                p.getWorld().strikeLightningEffect(strike);
            }
            p.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, land, 100, 4, 1, 4, 0.3);
            p.playSound(land, Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.6f);
            for (LivingEntity e : ctx.getNearbyEnemies(land, 8.0, p)) {
                ctx.dealDamage(p, e, 20.0);
                e.setVelocity(new Vector(0, 1.5, 0));
            }
        }, 25L);
        p.sendActionBar(Component.text("\u26A1 BIFROST SLAM! \u26A1", NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
    }

    // #54 THOUSAND DEMON DAGGERS ALT: Infernal Dance - fire spin 6s, 4 dmg/tick to nearby
    void altThousandDemonDaggers(Player p) {
        p.playSound(p.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 2.0f, 1.0f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 120 || !p.isOnline()) { cancel(); return; }
                if (tick % 10 == 0) {
                    Location c = p.getLocation();
                    double angle = tick * 0.3;
                    for (int i = 0; i < 4; i++) {
                        double a = angle + (i * Math.PI / 2);
                        Location pl = c.clone().add(Math.cos(a) * 2.5, 1, Math.sin(a) * 2.5);
                        p.getWorld().spawnParticle(Particle.FLAME, pl, 3, 0.1, 0.1, 0.1, 0.02);
                    }
                    for (LivingEntity e : ctx.getNearbyEnemies(c, 3.5, p)) {
                        ctx.dealDamage(p, e, 4.0);
                        e.setFireTicks(20);
                    }
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2620 Infernal Dance! 6s fire spin", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
    }

    // #55 STAR EDGE ALT: Supernova - massive 12-block AoE, 18 dmg + blindness
    void altStarEdge(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
        p.getWorld().spawnParticle(Particle.END_ROD, c.add(0, 2, 0), 100, 6, 3, 6, 0.3);
        p.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, c, 3, 0, 0, 0, 0);
        for (LivingEntity e : ctx.getNearbyEnemies(c, 12.0, p)) {
            ctx.dealDamage(p, e, 18.0);
            e.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
            e.setVelocity(e.getLocation().toVector().subtract(c.toVector()).normalize().multiply(2.5).setY(1.0));
        }
        p.sendActionBar(Component.text("\u2726 SUPERNOVA! \u2726", NamedTextColor.WHITE).decorate(TextDecoration.BOLD));
    }

    // #56 RIVERS OF BLOOD ALT: Blood Tsunami - wave 10 blocks, 16 dmg + wither
    void altRiversOfBlood(Player p) {
        Vector dir = p.getLocation().getDirection().normalize();
        Location start = p.getLocation();
        p.playSound(start, Sound.ENTITY_GENERIC_SPLASH, 2.0f, 0.4f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick > 10) { cancel(); return; }
                Location point = start.clone().add(dir.clone().multiply(tick));
                p.getWorld().spawnParticle(Particle.DUST, point.clone().add(0, 1, 0), 20, 1.5, 1, 1.5, 0, new Particle.DustOptions(Color.fromRGB(139, 0, 0), 2.0f));
                for (LivingEntity e : ctx.getNearbyEnemies(point, 3.0, p)) {
                    ctx.dealDamage(p, e, 16.0);
                    e.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 80, 1));
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 2L);
        p.sendActionBar(Component.text("\u2620 Blood Tsunami!", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
    }

    // #57 DRAGON SLAYING BLADE ALT: Slayer's Fury - 10s buff: +30% damage, +resist II
    void altDragonSlayingBlade(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 1));
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 1));
        p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.5f);
        p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, p.getLocation().add(0, 1, 0), 50, 1, 1.5, 1, 0.3);
        p.sendActionBar(Component.text("\u2694 Slayer's Fury! Strength II + Resistance II 10s", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // #58 STOP SIGN ALT: Road Rage - charge forward 10 blocks, stun + 12 dmg along path
    void altStopSign(Player p) {
        p.setVelocity(p.getLocation().getDirection().multiply(3.0));
        p.playSound(p.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 2.0f, 0.8f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 10) { cancel(); return; }
                Location loc = p.getLocation();
                p.getWorld().spawnParticle(Particle.DUST, loc.add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0, new Particle.DustOptions(Color.fromRGB(255, 0, 0), 2.0f));
                for (LivingEntity e : ctx.getNearbyEnemies(loc, 2.5, p)) {
                    ctx.dealDamage(p, e, 12.0);
                    e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 255));
                }
                tick++;
            }
        }.runTaskTimer(ctx.plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u26D4 Road Rage! \u26D4", NamedTextColor.RED).decorate(TextDecoration.BOLD));
    }

    // #59 CREATION SPLITTER ALT: Genesis Break - massive explosion, 30 dmg, 15-block AoE
    void altCreationSplitter(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 0.3f);
        p.playSound(c, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.3f);
        p.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, c.add(0, 2, 0), 8, 3, 2, 3, 0);
        p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, c, 100, 7, 3, 7, 0.8);
        p.getWorld().spawnParticle(Particle.END_ROD, c, 80, 7, 5, 7, 0.2);
        for (LivingEntity e : ctx.getNearbyEnemies(c, 15.0, p)) {
            ctx.dealDamage(p, e, 30.0);
            e.setVelocity(e.getLocation().toVector().subtract(c.toVector()).normalize().multiply(3.0).setY(1.5));
        }
        p.sendActionBar(Component.text("\u2726 GENESIS BREAK! \u2726", NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD));
    }
}