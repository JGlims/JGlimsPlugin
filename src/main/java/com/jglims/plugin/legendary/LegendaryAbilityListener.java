package com.jglims.plugin.legendary;

import java.util.ArrayList;
import java.util.HashMap;
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
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.config.ConfigManager;
import com.jglims.plugin.guilds.GuildManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * LegendaryAbilityListener — handles all 44 legendary weapon abilities.
 *
 * Two ability types per weapon:
 *   1. PRIMARY ability: right-click (rule A.10)
 *   2. ALTERNATE ability: crouch + right-click (rule A.10)
 *
 * v3.0.0 — Phase 8b/8c rewrite. Input: RC=primary, Crouch+RC=alternate.
 */
public class LegendaryAbilityListener implements Listener {

    private final JGlimsPlugin plugin;
    private final ConfigManager config;
    private final LegendaryWeaponManager weaponManager;
    private final GuildManager guildManager;

    // Cooldown tracking: player UUID -> ability name -> expiry timestamp
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    // Active buff tracking for various abilities
    private final Map<UUID, Integer> bloodlustStacks = new HashMap<>(); // Muramasa
    private final Map<UUID, Long> bloodlustExpiry = new HashMap<>();
    private final Map<UUID, Double> retributionDamageStored = new HashMap<>(); // Vengeance
    private final Map<UUID, Long> retributionExpiry = new HashMap<>();
    private final Map<UUID, Integer> soulCount = new HashMap<>(); // Soul Collector
    private final Map<UUID, UUID> grudgeTargets = new HashMap<>(); // Vengeance hold
    private final Map<UUID, Long> grudgeExpiry = new HashMap<>();
    private final Map<UUID, Boolean> rebornReady = new HashMap<>(); // Phoenix's Grace
    private final Map<UUID, Long> rebornExpiry = new HashMap<>();
    private final Map<UUID, Boolean> phaseShiftActive = new HashMap<>(); // Phantomguard
    private final Map<UUID, Boolean> undyingRageActive = new HashMap<>(); // Gravecleaver
    private final Map<UUID, Long> undyingRageExpiry = new HashMap<>();
    private final Map<UUID, Boolean> crescentParryActive = new HashMap<>(); // Crescent Edge
    private final Map<UUID, Boolean> emberShieldActive = new HashMap<>(); // Flamberge
    private final Map<UUID, Long> emberShieldExpiry = new HashMap<>();
    private final Map<UUID, Boolean> thunderShieldActive = new HashMap<>(); // Stormfork
    private final Map<UUID, Long> thunderShieldExpiry = new HashMap<>();
    private final Map<UUID, UUID> predatorMarkTarget = new HashMap<>(); // Talonbrand
    private final Map<UUID, Long> predatorMarkExpiry = new HashMap<>();
    private final Map<UUID, Integer> gemBarrierCharges = new HashMap<>(); // Emerald Greatcleaver
    private final Map<UUID, Long> gemBarrierExpiry = new HashMap<>();
    private final Map<UUID, Boolean> shadowStanceActive = new HashMap<>(); // Gloomsteel Katana
    private final Map<UUID, Long> shadowStanceExpiry = new HashMap<>();

    public LegendaryAbilityListener(JGlimsPlugin plugin, ConfigManager config,
                                     LegendaryWeaponManager weaponManager,
                                     GuildManager guildManager) {
        this.plugin = plugin;
        this.config = config;
        this.weaponManager = weaponManager;
        this.guildManager = guildManager;
    }

    // ════════════════════════════════════════════════════════════════
    // COOLDOWN UTILITIES
    // ════════════════════════════════════════════════════════════════

    private boolean isOnCooldown(Player player, String abilityName) {
        Map<String, Long> pc = cooldowns.get(player.getUniqueId());
        if (pc == null) return false;
        Long expiry = pc.get(abilityName);
        if (expiry == null) return false;
        if (System.currentTimeMillis() >= expiry) {
            pc.remove(abilityName);
            return false;
        }
        return true;
    }

    private long getRemainingCooldownMs(Player player, String abilityName) {
        Map<String, Long> pc = cooldowns.get(player.getUniqueId());
        if (pc == null) return 0;
        Long expiry = pc.get(abilityName);
        if (expiry == null) return 0;
        return Math.max(0, expiry - System.currentTimeMillis());
    }

    private void setCooldown(Player player, String abilityName, int seconds) {
        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                .put(abilityName, System.currentTimeMillis() + (seconds * 1000L));
    }

    private void sendCooldownMsg(Player player, String abilityName) {
        double sec = getRemainingCooldownMs(player, abilityName) / 1000.0;
        player.sendActionBar(Component.text(abilityName + " on cooldown: ", NamedTextColor.RED)
                .append(Component.text(String.format("%.1fs", sec), NamedTextColor.YELLOW)));
    }

    // ════════════════════════════════════════════════════════════════
    // ENEMY DETECTION (guild-aware)
    // ════════════════════════════════════════════════════════════════

    private List<LivingEntity> getNearbyEnemies(Location center, double radius, Player exclude) {
        List<LivingEntity> enemies = new ArrayList<>();
        for (Entity e : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (!(e instanceof LivingEntity le) || e instanceof ArmorStand || e == exclude) continue;
            if (le.getLocation().distanceSquared(center) > radius * radius) continue;
            if (le instanceof Player target) {
                if (target.getGameMode() == GameMode.CREATIVE || target.getGameMode() == GameMode.SPECTATOR) continue;
                if (exclude != null && guildManager.areInSameGuild(exclude.getUniqueId(), target.getUniqueId())) continue;
            }
            enemies.add(le);
        }
        return enemies;
    }

    private void dealDamage(Player attacker, LivingEntity target, double damage) {
        target.damage(damage, attacker);
    }

    // ════════════════════════════════════════════════════════════════
    // MAIN EVENT: Right-click → dispatch to ability
    // ════════════════════════════════════════════════════════════════

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        LegendaryWeapon weapon = weaponManager.identify(item);
        if (weapon == null) return;

        if (player.isSneaking()) {
            // CROUCH + RIGHT-CLICK → Alternate ability (rule A.10)
            handleAltAbility(player, weapon);
            event.setCancelled(true);
        } else {
            // NON-SNEAK RIGHT-CLICK → Start hold timer for hold ability
            // REMOVED: old hold timer
            // RIGHT-CLICK → Primary ability (rule A.10)
            handlePrimaryAbility(player, weapon);
            event.setCancelled(true);
        }
    }


    // ════════════════════════════════════════════════════════════════
    // PRIMARY ABILITY DISPATCHER (Right-Click)
    // ════════════════════════════════════════════════════════════════

    private void handlePrimaryAbility(Player player, LegendaryWeapon weapon) {
        String name = weapon.getPrimaryAbilityName();
        int cd = weapon.getPrimaryCooldown();

        if (isOnCooldown(player, name)) { sendCooldownMsg(player, name); return; }
        setCooldown(player, name, cd);

        switch (weapon) {
            // ── MYTHIC/EPIC/RARE ────────────────────────────────
            case OCEANS_RAGE -> rcOceansRage(player);
            case AQUATIC_SACRED_BLADE -> rcAquaticSacredBlade(player);
            case TRUE_EXCALIBUR -> rcTrueExcalibur(player);
            case REQUIEM_NINTH_ABYSS -> rcRequiemNinthAbyss(player);
            case ROYAL_CHAKRAM -> rcRoyalChakram(player);
            case BERSERKERS_GREATAXE -> rcBerserkersGreataxe(player);
            case ACIDIC_CLEAVER -> rcAcidicCleaver(player);
            case BLACK_IRON_GREATSWORD -> rcBlackIronGreatsword(player);
            case MURAMASA -> rcMuramasa(player);
            case PHOENIXS_GRACE -> rcPhoenixsGrace(player);
            case SOUL_COLLECTOR -> rcSoulCollector(player);
            case AMETHYST_SHURIKEN -> rcAmethystShuriken(player);
            case VALHAKYRA -> rcValhakyra(player);
            case WINDREAPER -> rcWindreaper(player);
            case PHANTOMGUARD -> rcPhantomguard(player);
            case MOONLIGHT -> rcMoonlight(player);
            case ZENITH -> rcZenith(player);
            case SOLSTICE -> rcSolstice(player);
            case GRAND_CLAYMORE -> rcGrandClaymore(player);
            case CALAMITY_BLADE -> rcCalamityBlade(player);
            case DRAGON_SWORD -> rcDragonSword(player);
            case TALONBRAND -> rcTalonbrand(player);
            case EMERALD_GREATCLEAVER -> rcEmeraldGreatcleaver(player);
            case DEMONS_BLOOD_BLADE -> rcDemonsBloodBlade(player);
            // ── COMMON ─────────────────────────────────
            case NOCTURNE -> rcNocturne(player);
            case GRAVESCEPTER -> rcGravescepter(player);
            case LYCANBANE -> rcLycanbane(player);
            case GLOOMSTEEL_KATANA -> rcGloomsteelKatana(player);
            case VIRIDIAN_CLEAVER -> rcViridianCleaver(player);
            case CRESCENT_EDGE -> rcCrescentEdge(player);
            case GRAVECLEAVER -> rcGravecleaver(player);
            case AMETHYST_GREATBLADE -> rcAmethystGreatblade(player);
            case FLAMBERGE -> rcFlamberge(player);
            case CRYSTAL_FROSTBLADE -> rcCrystalFrostblade(player);
            case DEMONSLAYER -> rcDemonslayer(player);
            case VENGEANCE -> rcVengeance(player);
            case OCULUS -> rcOculus(player);
            case ANCIENT_GREATSLAB -> rcAncientGreatslab(player);
            case NEPTUNES_FANG -> rcNeptunesFang(player);
            case TIDECALLER -> rcTidecaller(player);
            case STORMFORK -> rcStormfork(player);
            case JADE_REAPER -> rcJadeReaper(player);
            case VINDICATOR -> rcVindicator(player);
            case SPIDER_FANG -> rcSpiderFang(player);
            case DIVINE_AXE_RHITTA -> rcDivineAxeRhitta(player);
            case YORU -> rcYoru(player);
            case TENGENS_BLADE -> rcTengensBlade(player);
            case EDGE_ASTRAL_PLANE -> rcEdgeAstralPlane(player);
            case FALLEN_GODS_SPEAR -> rcFallenGodsSpear(player);
            case NATURE_SWORD -> rcNatureSword(player);
            case HEAVENLY_PARTISAN -> rcHeavenlyPartisan(player);
            case SOUL_DEVOURER -> rcSoulDevourer(player);
            case MJOLNIR -> rcMjolnir(player);
            case THOUSAND_DEMON_DAGGERS -> rcThousandDemonDaggers(player);
            case STAR_EDGE -> rcStarEdge(player);
            case RIVERS_OF_BLOOD -> rcRiversOfBlood(player);
            case DRAGON_SLAYING_BLADE -> rcDragonSlayingBlade(player);
            case STOP_SIGN -> rcStopSign(player);
            case CREATION_SPLITTER -> rcCreationSplitter(player);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // ALTERNATE ABILITY DISPATCHER (Crouch + Right-Click)
    // ════════════════════════════════════════════════════════════════

    private void handleAltAbility(Player player, LegendaryWeapon weapon) {
        String name = weapon.getAltAbilityName();
        int cd = weapon.getAltCooldown();

        if (isOnCooldown(player, name)) { sendCooldownMsg(player, name); return; }
        setCooldown(player, name, cd);

        switch (weapon) {
            case OCEANS_RAGE -> holdOceansRage(player);
            case AQUATIC_SACRED_BLADE -> holdAquaticSacredBlade(player);
            case TRUE_EXCALIBUR -> holdTrueExcalibur(player);
            case REQUIEM_NINTH_ABYSS -> holdRequiemNinthAbyss(player);
            case ROYAL_CHAKRAM -> holdRoyalChakram(player);
            case BERSERKERS_GREATAXE -> holdBerserkersGreataxe(player);
            case ACIDIC_CLEAVER -> holdAcidicCleaver(player);
            case BLACK_IRON_GREATSWORD -> holdBlackIronGreatsword(player);
            case MURAMASA -> holdMuramasa(player);
            case PHOENIXS_GRACE -> holdPhoenixsGrace(player);
            case SOUL_COLLECTOR -> holdSoulCollector(player);
            case AMETHYST_SHURIKEN -> holdAmethystShuriken(player);
            case VALHAKYRA -> holdValhakyra(player);
            case WINDREAPER -> holdWindreaper(player);
            case PHANTOMGUARD -> holdPhantomguard(player);
            case MOONLIGHT -> holdMoonlight(player);
            case ZENITH -> holdZenith(player);
            case SOLSTICE -> holdSolstice(player);
            case GRAND_CLAYMORE -> holdGrandClaymore(player);
            case CALAMITY_BLADE -> holdCalamityBlade(player);
            case DRAGON_SWORD -> holdDragonSword(player);
            case TALONBRAND -> holdTalonbrand(player);
            case EMERALD_GREATCLEAVER -> holdEmeraldGreatcleaver(player);
            case DEMONS_BLOOD_BLADE -> holdDemonsBloodBlade(player);
            case NOCTURNE -> holdNocturne(player);
            case GRAVESCEPTER -> holdGravescepter(player);
            case LYCANBANE -> holdLycanbane(player);
            case GLOOMSTEEL_KATANA -> holdGloomsteelKatana(player);
            case VIRIDIAN_CLEAVER -> holdViridianCleaver(player);
            case CRESCENT_EDGE -> holdCrescentEdge(player);
            case GRAVECLEAVER -> holdGravecleaver(player);
            case AMETHYST_GREATBLADE -> holdAmethystGreatblade(player);
            case FLAMBERGE -> holdFlamberge(player);
            case CRYSTAL_FROSTBLADE -> holdCrystalFrostblade(player);
            case DEMONSLAYER -> holdDemonslayer(player);
            case VENGEANCE -> holdVengeance(player);
            case OCULUS -> holdOculus(player);
            case ANCIENT_GREATSLAB -> holdAncientGreatslab(player);
            case NEPTUNES_FANG -> holdNeptunesFang(player);
            case TIDECALLER -> holdTidecaller(player);
            case STORMFORK -> holdStormfork(player);
            case JADE_REAPER -> holdJadeReaper(player);
            case VINDICATOR -> holdVindicator(player);
            case SPIDER_FANG -> holdSpiderFang(player);
            case DIVINE_AXE_RHITTA -> altDivineAxeRhitta(player);
            case YORU -> altYoru(player);
            case TENGENS_BLADE -> altTengensBlade(player);
            case EDGE_ASTRAL_PLANE -> altEdgeAstralPlane(player);
            case FALLEN_GODS_SPEAR -> altFallenGodsSpear(player);
            case NATURE_SWORD -> altNatureSword(player);
            case HEAVENLY_PARTISAN -> altHeavenlyPartisan(player);
            case SOUL_DEVOURER -> altSoulDevourer(player);
            case MJOLNIR -> altMjolnir(player);
            case THOUSAND_DEMON_DAGGERS -> altThousandDemonDaggers(player);
            case STAR_EDGE -> altStarEdge(player);
            case RIVERS_OF_BLOOD -> altRiversOfBlood(player);
            case DRAGON_SLAYING_BLADE -> altDragonSlayingBlade(player);
            case STOP_SIGN -> altStopSign(player);
            case CREATION_SPLITTER -> altCreationSplitter(player);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // PASSIVE LISTENER: Damage modifiers for active buffs
    // ════════════════════════════════════════════════════════════════

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // --- Attacker is player: check offensive buffs ---
        if (event.getDamager() instanceof Player attacker && event.getEntity() instanceof LivingEntity target) {
            UUID uid = attacker.getUniqueId();

            // Muramasa Bloodlust stacks
            if (bloodlustStacks.containsKey(uid) && bloodlustExpiry.getOrDefault(uid, 0L) > System.currentTimeMillis()) {
                int stacks = bloodlustStacks.get(uid);
                event.setDamage(event.getDamage() + (stacks * 2.0));
            }

            // Vengeance Grudge Mark
            if (grudgeTargets.containsKey(uid) && grudgeExpiry.getOrDefault(uid, 0L) > System.currentTimeMillis()) {
                if (target.getUniqueId().equals(grudgeTargets.get(uid))) {
                    event.setDamage(event.getDamage() * 1.2);
                }
            }

            // Talonbrand Predator's Mark
            if (predatorMarkTarget.containsKey(uid) && predatorMarkExpiry.getOrDefault(uid, 0L) > System.currentTimeMillis()) {
                if (target.getUniqueId().equals(predatorMarkTarget.get(uid))) {
                    event.setDamage(event.getDamage() * 1.3);
                }
            }

            // Gloomsteel Katana Shadow Stance (25% dodge on incoming)
            // handled below in target section
        }

        // --- Target is player: check defensive buffs ---
        if (event.getEntity() instanceof Player victim) {
            UUID uid = victim.getUniqueId();

            // Shadow Stance dodge
            if (shadowStanceActive.getOrDefault(uid, false)
                    && shadowStanceExpiry.getOrDefault(uid, 0L) > System.currentTimeMillis()) {
                if (Math.random() < 0.25) {
                    event.setCancelled(true);
                    victim.sendActionBar(Component.text("Shadow Dodge!", NamedTextColor.DARK_GRAY).decorate(TextDecoration.BOLD));
                    victim.getWorld().spawnParticle(Particle.SMOKE, victim.getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0.05);
                    return;
                }
            }

            // Crescent Edge parry
            if (crescentParryActive.getOrDefault(uid, false) && event.getDamager() instanceof LivingEntity attacker) {
                crescentParryActive.put(uid, false);
                double reflected = event.getDamage();
                event.setCancelled(true);
                attacker.damage(reflected, victim);
                victim.sendActionBar(Component.text("PARRIED! ", NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
                        .append(Component.text(String.format("%.0f dmg reflected!", reflected), NamedTextColor.WHITE)));
                victim.getWorld().spawnParticle(Particle.CRIT, victim.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.2);
                victim.playSound(victim.getLocation(), Sound.ITEM_SHIELD_BLOCK, 2.0f, 0.8f);
                return;
            }

            // Ember Shield reflect
            if (emberShieldActive.getOrDefault(uid, false)
                    && emberShieldExpiry.getOrDefault(uid, 0L) > System.currentTimeMillis()
                    && event.getDamager() instanceof LivingEntity attacker) {
                attacker.setFireTicks(60);
                attacker.damage(4.0, victim);
                victim.getWorld().spawnParticle(Particle.FLAME, attacker.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.05);
            }

            // Thunder Shield (Stormfork)
            if (thunderShieldActive.getOrDefault(uid, false)
                    && thunderShieldExpiry.getOrDefault(uid, 0L) > System.currentTimeMillis()
                    && event.getDamager() instanceof LivingEntity attacker) {
                victim.getWorld().strikeLightningEffect(attacker.getLocation());
                attacker.damage(4.0, victim);
            }

            // Gem Barrier charges (Emerald Greatcleaver)
            if (gemBarrierCharges.getOrDefault(uid, 0) > 0
                    && gemBarrierExpiry.getOrDefault(uid, 0L) > System.currentTimeMillis()) {
                event.setCancelled(true);
                gemBarrierCharges.merge(uid, -1, Integer::sum);
                int remaining = gemBarrierCharges.get(uid);
                victim.sendActionBar(Component.text("Gem Barrier absorbed hit! ", NamedTextColor.GREEN)
                        .append(Component.text(remaining + " charges left", NamedTextColor.YELLOW)));
                victim.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, victim.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0);
                victim.playSound(victim.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 2.0f, 1.2f);
                if (remaining <= 0) gemBarrierCharges.remove(uid);
                return;
            }

            // Vengeance Retribution (store damage)
            if (retributionExpiry.containsKey(uid) && retributionExpiry.get(uid) > System.currentTimeMillis()) {
                retributionDamageStored.merge(uid, event.getDamage(), Double::sum);
            }

            // Undying Rage (Gravecleaver)
            if (undyingRageActive.getOrDefault(uid, false)
                    && undyingRageExpiry.getOrDefault(uid, 0L) > System.currentTimeMillis()) {
                if (victim.getHealth() - event.getFinalDamage() <= 0) {
                    event.setCancelled(true);
                    victim.setHealth(1.0);
                    undyingRageActive.put(uid, false);
                    victim.sendActionBar(Component.text("UNDYING RAGE! ", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
                    victim.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, victim.getLocation(), 50, 0.5, 1, 0.5, 0.3);
                    victim.playSound(victim.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
                }
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  RIGHT-CLICK ABILITIES — ALL 44 WEAPONS
    // ════════════════════════════════════════════════════════════════

    // ── #1 OCEAN'S RAGE: Tidal Crash — 6-block AoE water blast, 15 dmg ──
    private void rcOceansRage(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.ENTITY_GENERIC_SPLASH, 2.0f, 0.6f);
        p.getWorld().spawnParticle(Particle.SPLASH, c.clone().add(0, 1, 0), 100, 3, 2, 3, 0.5);
        p.getWorld().spawnParticle(Particle.BUBBLE, c.clone().add(0, 1, 0), 50, 3, 2, 3, 0.3);
        for (LivingEntity e : getNearbyEnemies(c, 6.0, p)) {
            dealDamage(p, e, 15.0);
            Vector kb = e.getLocation().toVector().subtract(c.toVector()).normalize().multiply(1.5).setY(0.6);
            e.setVelocity(kb);
            e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
        }
        p.sendActionBar(Component.text("\u2248 Tidal Crash! \u2248", NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
    }

    // ── #2 AQUATIC SACRED BLADE: Aqua Heal — 6 hearts + Conduit Power 30s ──
    private void rcAquaticSacredBlade(Player p) {
        double maxHp = p.getAttribute(Attribute.MAX_HEALTH).getValue();
        p.setHealth(Math.min(maxHp, p.getHealth() + 12.0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, 600, 0));
        p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.5f, 1.2f);
        p.getWorld().spawnParticle(Particle.HEART, p.getLocation().add(0, 2, 0), 10, 0.5, 0.5, 0.5, 0);
        p.getWorld().spawnParticle(Particle.BUBBLE, p.getLocation().add(0, 1, 0), 30, 1, 1, 1, 0.1);
        p.sendActionBar(Component.text("\u2764 Aqua Heal! +6 hearts + Conduit Power", NamedTextColor.AQUA));
    }

    // ── #3 TRUE EXCALIBUR: Holy Smite — Lightning + 20 dmg AoE 5 blocks ──
    private void rcTrueExcalibur(Player p) {
        Location c = p.getLocation().add(p.getLocation().getDirection().multiply(3));
        p.getWorld().strikeLightningEffect(c);
        p.playSound(c, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 1.0f);
        p.getWorld().spawnParticle(Particle.END_ROD, c.clone().add(0, 1, 0), 50, 2, 3, 2, 0.1);
        for (LivingEntity e : getNearbyEnemies(c, 5.0, p)) {
            dealDamage(p, e, 20.0);
        }
        p.sendActionBar(Component.text("\u2694 HOLY SMITE! \u2694", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
    }

    // ── #4 REQUIEM: Soul Devour — Drain 8 hearts, heal self ──
    private void rcRequiemNinthAbyss(Player p) {
        LivingEntity target = getTargetEntity(p, 5.0);
        if (target == null) { p.sendActionBar(Component.text("No target in range!", NamedTextColor.RED)); return; }
        dealDamage(p, target, 16.0);
        double maxHp = p.getAttribute(Attribute.MAX_HEALTH).getValue();
        p.setHealth(Math.min(maxHp, p.getHealth() + 16.0));
        p.playSound(p.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 1.0f, 0.5f);
        p.getWorld().spawnParticle(Particle.SOUL, target.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.05);
        p.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, p.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0);
        p.sendActionBar(Component.text("\u2620 Soul Devoured! +8 hearts", NamedTextColor.DARK_PURPLE));
    }

    // ── #5 ROYAL CHAKRAM: Chakram Throw — bounces 4 targets, 8 dmg each ──
    private void rcRoyalChakram(Player p) {
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 1.5f);
        List<LivingEntity> targets = getNearbyEnemies(p.getLocation(), 10.0, p);
        int hits = Math.min(4, targets.size());
        for (int i = 0; i < hits; i++) {
            LivingEntity t = targets.get(i);
            final int delay = i * 5;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                dealDamage(p, t, 8.0);
                t.getWorld().spawnParticle(Particle.CRIT, t.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.1);
                t.getWorld().playSound(t.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.8f);
            }, delay);
        }
        p.sendActionBar(Component.text("\u25C6 Chakram Throw! " + hits + " targets hit", NamedTextColor.GOLD));
    }

    // ── #6 BERSERKER'S GREATAXE: Berserker Slam — 8-block AoE, 18 dmg ──
    private void rcBerserkersGreataxe(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.7f);
        p.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, c, 3, 0, 0, 0, 0);
        p.getWorld().spawnParticle(Particle.BLOCK, c, 80, 4, 1, 4, 0.5, Material.NETHERRACK.createBlockData());
        for (LivingEntity e : getNearbyEnemies(c, 8.0, p)) {
            dealDamage(p, e, 18.0);
            e.setVelocity(new Vector(0, 1.2, 0));
        }
        p.sendActionBar(Component.text("\u2620 BERSERKER SLAM! \u2620", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
    }

    // ── #7 ACIDIC CLEAVER: Acid Splash — 5-block cone, Poison III 8s ──
    private void rcAcidicCleaver(Player p) {
        Location c = p.getLocation().add(p.getLocation().getDirection().multiply(3));
        p.playSound(c, Sound.ENTITY_LLAMA_SPIT, 1.5f, 0.5f);
        p.getWorld().spawnParticle(Particle.ITEM_SLIME, c, 40, 2.5, 1, 2.5, 0.1);
        for (LivingEntity e : getNearbyEnemies(c, 5.0, p)) {
            dealDamage(p, e, 10.0);
            e.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 160, 2));
        }
        p.sendActionBar(Component.text("\u2620 Acid Splash! Poison III applied", NamedTextColor.GREEN));
    }

    // ── #8 BLACK IRON GREATSWORD: Dark Slash — 10-block line, 16 dmg ──
    private void rcBlackIronGreatsword(Player p) {
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
                for (LivingEntity e : getNearbyEnemies(point, 1.5, p)) {
                    dealDamage(p, e, 16.0);
                }
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2694 Dark Slash!", NamedTextColor.DARK_GRAY).decorate(TextDecoration.BOLD));
    }

    // ── #9 MURAMASA: Crimson Flash — 8-block dash, 12 dmg along path ──
    private void rcMuramasa(Player p) {
        p.setVelocity(p.getLocation().getDirection().multiply(2.5));
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2.0f, 1.5f);
        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (ticks >= 8) { cancel(); return; }
                Location loc = p.getLocation();
                p.getWorld().spawnParticle(Particle.DUST, loc.add(0, 1, 0), 8, 0.4, 0.4, 0.4, 0,
                        new Particle.DustOptions(Color.fromRGB(200, 0, 0), 1.5f));
                for (LivingEntity e : getNearbyEnemies(loc, 2.5, p)) {
                    dealDamage(p, e, 12.0);
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u26A1 Crimson Flash!", NamedTextColor.RED).decorate(TextDecoration.BOLD));
    }

    // ── #10 PHOENIX'S GRACE: Phoenix Strike — Fire AoE 6b, 14 dmg + Fire ──
    private void rcPhoenixsGrace(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.ENTITY_BLAZE_SHOOT, 2.0f, 0.8f);
        p.getWorld().spawnParticle(Particle.FLAME, c.add(0, 1, 0), 80, 3, 2, 3, 0.1);
        p.getWorld().spawnParticle(Particle.LAVA, c, 20, 3, 1, 3, 0);
        for (LivingEntity e : getNearbyEnemies(c, 6.0, p)) {
            dealDamage(p, e, 14.0);
            e.setFireTicks(120);
        }
        p.sendActionBar(Component.text("\u2600 Phoenix Strike!", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // ── #11 SOUL COLLECTOR: Soul Harvest — kill stores soul, +10 bonus ──
    private void rcSoulCollector(Player p) {
        LivingEntity target = getTargetEntity(p, 5.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        dealDamage(p, target, 14.0);
        if (target.isDead() || target.getHealth() <= 0) {
            int souls = soulCount.getOrDefault(p.getUniqueId(), 0);
            if (souls < 5) {
                soulCount.put(p.getUniqueId(), souls + 1);
                p.sendActionBar(Component.text("\u2620 Soul captured! (" + (souls + 1) + "/5)", NamedTextColor.DARK_PURPLE));
                p.playSound(p.getLocation(), Sound.ENTITY_VEX_DEATH, 1.0f, 0.5f);
            }
        }
        p.getWorld().spawnParticle(Particle.SOUL, target.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.05);
    }

    // ── #12 AMETHYST SHURIKEN: Shuriken Barrage — 5 projectiles, 7 dmg ──
    private void rcAmethystShuriken(Player p) {
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 2.0f);
        Vector baseDir = p.getLocation().getDirection().normalize();
        for (int i = -2; i <= 2; i++) {
            double angle = Math.toRadians(i * 12);
            Vector dir = rotateY(baseDir.clone(), angle);
            final Vector fDir = dir;
            new BukkitRunnable() {
                Location loc = p.getEyeLocation().clone();
                int ticks = 0;
                @Override public void run() {
                    if (ticks > 15) { cancel(); return; }
                    loc.add(fDir);
                    p.getWorld().spawnParticle(Particle.END_ROD, loc, 2, 0.05, 0.05, 0.05, 0);
                    for (LivingEntity e : getNearbyEnemies(loc, 1.0, p)) {
                        dealDamage(p, e, 7.0);
                        cancel();
                        return;
                    }
                    ticks++;
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
        p.sendActionBar(Component.text("\u25C6 Shuriken Barrage!", NamedTextColor.LIGHT_PURPLE));
    }

    // ── #13 VALHAKYRA: Valkyrie Dive — Leap 10 up, slam 20 dmg AoE ──
    private void rcValhakyra(Player p) {
        p.setVelocity(new Vector(0, 2.5, 0));
        p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 2.0f, 1.2f);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Location landing = p.getLocation();
            p.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, landing, 3, 0, 0, 0, 0);
            p.getWorld().spawnParticle(Particle.CLOUD, landing, 50, 3, 1, 3, 0.1);
            p.playSound(landing, Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.6f);
            for (LivingEntity e : getNearbyEnemies(landing, 6.0, p)) {
                dealDamage(p, e, 20.0);
                e.setVelocity(new Vector(0, 1.5, 0));
            }
        }, 25L);
        p.sendActionBar(Component.text("\u2694 VALKYRIE DIVE! \u2694", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // ── #14 WINDREAPER: Gale Slash — 8-block wind cone, 12 dmg + knockback ──
    private void rcWindreaper(Player p) {
        Location c = p.getLocation().add(p.getLocation().getDirection().multiply(4));
        p.playSound(c, Sound.ENTITY_BREEZE_SHOOT, 2.0f, 0.8f);
        p.getWorld().spawnParticle(Particle.CLOUD, c, 40, 3, 2, 3, 0.2);
        for (LivingEntity e : getNearbyEnemies(c, 8.0, p)) {
            dealDamage(p, e, 12.0);
            Vector kb = e.getLocation().toVector().subtract(p.getLocation().toVector()).normalize().multiply(2.5).setY(0.8);
            e.setVelocity(kb);
        }
        p.sendActionBar(Component.text("\u2601 Gale Slash!", NamedTextColor.WHITE));
    }

    // ── #15 PHANTOMGUARD: Spectral Cleave — through blocks, 10-block line, 14 dmg ──
    private void rcPhantomguard(Player p) {
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
                        if (le instanceof Player tp && guildManager.areInSameGuild(p.getUniqueId(), tp.getUniqueId())) continue;
                        dealDamage(p, le, 14.0);
                    }
                }
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2694 Spectral Cleave!", NamedTextColor.GRAY));
    }

    // ── #16 MOONLIGHT: Lunar Beam — 15-block ranged beam, 16 dmg ──
    private void rcMoonlight(Player p) {
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
                for (LivingEntity e : getNearbyEnemies(point, 1.5, p)) {
                    dealDamage(p, e, 16.0);
                }
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u263D Lunar Beam!", NamedTextColor.YELLOW));
    }

    // ── #17 ZENITH: Final Judgment — 360 AoE 8 blocks, 22 dmg ──
    private void rcZenith(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 1.0f);
        p.playSound(c, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 0.5f);
        p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, c.add(0, 1, 0), 200, 4, 3, 4, 0.8);
        p.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, c, 5, 2, 0, 2, 0);
        for (LivingEntity e : getNearbyEnemies(c, 8.0, p)) {
            dealDamage(p, e, 22.0);
            Vector kb = e.getLocation().toVector().subtract(c.toVector()).normalize().multiply(2.0).setY(1.0);
            e.setVelocity(kb);
        }
        p.sendActionBar(Component.text("\u2726 FINAL JUDGMENT! \u2726", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // ── #18 SOLSTICE: Solar Flare — 10-block fire AoE, 15 dmg + blindness ──
    private void rcSolstice(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.ENTITY_BLAZE_SHOOT, 2.0f, 0.6f);
        p.getWorld().spawnParticle(Particle.FLAME, c.add(0, 1, 0), 100, 5, 3, 5, 0.1);
        p.getWorld().spawnParticle(Particle.LAVA, c, 30, 5, 1, 5, 0);
        for (LivingEntity e : getNearbyEnemies(c, 10.0, p)) {
            dealDamage(p, e, 15.0);
            e.setFireTicks(60);
            if (e instanceof Player) e.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
        }
        p.sendActionBar(Component.text("\u2600 Solar Flare!", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // ── #19 GRAND CLAYMORE: Titan Swing — 180 arc, 10-block, 18 dmg ──
    private void rcGrandClaymore(Player p) {
        Location c = p.getLocation();
        Vector facing = c.getDirection().normalize();
        p.playSound(c, Sound.ITEM_MACE_SMASH_GROUND, 2.0f, 0.9f);
        p.getWorld().spawnParticle(Particle.SWEEP_ATTACK, c.add(0, 1, 0), 30, 5, 1, 5, 0);
        for (LivingEntity e : getNearbyEnemies(c, 10.0, p)) {
            Vector toEnemy = e.getLocation().toVector().subtract(c.toVector()).normalize();
            if (facing.dot(toEnemy) > 0) { // in front (180 arc)
                dealDamage(p, e, 18.0);
                Vector kb = toEnemy.multiply(2.5).setY(0.6);
                e.setVelocity(kb);
            }
        }
        p.sendActionBar(Component.text("\u2694 TITAN SWING! \u2694", NamedTextColor.WHITE).decorate(TextDecoration.BOLD));
    }

    // ── #20 CALAMITY BLADE: Cataclysm — 6-block AoE, 14 dmg + slowness ──
    private void rcCalamityBlade(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.ENTITY_WARDEN_EMERGE, 1.5f, 0.8f);
        p.getWorld().spawnParticle(Particle.BLOCK, c, 80, 3, 2, 3, 0.5, Material.STONE.createBlockData());
        p.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, c.add(0, 1, 0), 30, 3, 2, 3, 0.05);
        for (LivingEntity e : getNearbyEnemies(c, 6.0, p)) {
            dealDamage(p, e, 14.0);
            e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 2));
        }
        p.sendActionBar(Component.text("\u2622 Cataclysm!", NamedTextColor.DARK_RED));
    }

    // ── #21 DRAGON SWORD: Dragon Breath — 8-block fire cone, 12 dmg ──
    private void rcDragonSword(Player p) {
        Location c = p.getLocation().add(p.getLocation().getDirection().multiply(4));
        p.playSound(c, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.5f);
        p.getWorld().spawnParticle(Particle.FLAME, c, 60, 4, 2, 4, 0.1);
        p.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, c, 30, 3, 1, 3, 0.05);
        for (LivingEntity e : getNearbyEnemies(c, 8.0, p)) {
            dealDamage(p, e, 12.0);
            e.setFireTicks(80);
        }
        p.sendActionBar(Component.text("\u2620 Dragon Breath!", NamedTextColor.RED));
    }

    // ── #22 TALONBRAND: Talon Strike — Triple combo, 8 dmg x3 ──
    private void rcTalonbrand(Player p) {
        LivingEntity target = getTargetEntity(p, 5.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.5f, 1.5f);
        for (int i = 0; i < 3; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!target.isDead()) {
                    dealDamage(p, target, 8.0);
                    target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 8, 0.2, 0.2, 0.2, 0.1);
                    p.playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.8f);
                }
            }, i * 5L);
        }
        p.sendActionBar(Component.text("\u2694 Talon Strike! x3 combo", NamedTextColor.DARK_RED));
    }

    // ── #23 EMERALD GREATCLEAVER: Emerald Storm — 6-block AoE, 14 dmg + Poison ──
    private void rcEmeraldGreatcleaver(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 2.0f, 0.8f);
        p.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, c.add(0, 1, 0), 60, 3, 2, 3, 0);
        p.getWorld().spawnParticle(Particle.COMPOSTER, c, 30, 3, 1, 3, 0.1);
        for (LivingEntity e : getNearbyEnemies(c, 6.0, p)) {
            dealDamage(p, e, 14.0);
            e.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 1));
        }
        p.sendActionBar(Component.text("\u2666 Emerald Storm!", NamedTextColor.GREEN));
    }

    // ── #24 DEMON'S BLOOD BLADE: Blood Rite — Sacrifice 3 hearts, 25 dmg ──
    private void rcDemonsBloodBlade(Player p) {
        LivingEntity target = getTargetEntity(p, 5.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        if (p.getHealth() <= 6.0) { p.sendActionBar(Component.text("Not enough health!", NamedTextColor.RED)); return; }
        p.setHealth(p.getHealth() - 6.0);
        dealDamage(p, target, 25.0);
        p.playSound(p.getLocation(), Sound.ENTITY_WITHER_HURT, 1.5f, 0.5f);
        p.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, target.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.1);
        p.getWorld().spawnParticle(Particle.DUST, p.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0,
                new Particle.DustOptions(Color.fromRGB(139, 0, 0), 2.0f));
        p.sendActionBar(Component.text("\u2620 Blood Rite! -3 hearts \u2192 25 damage!", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
    }

    // ════════════════════════════════════════════════════════════════
    //  UNCOMMON TIER RIGHT-CLICK ABILITIES (20)
    // ════════════════════════════════════════════════════════════════

    // ── #25 NOCTURNE: Shadow Slash — 6-block line, 10 dmg + Blindness ──
    private void rcNocturne(Player p) {
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
                for (LivingEntity e : getNearbyEnemies(point, 1.5, p)) {
                    dealDamage(p, e, 10.0);
                    e.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
                }
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u263D Shadow Slash!", NamedTextColor.DARK_GRAY));
    }

    // ── #26 GRAVESCEPTER: Grave Rise — Summon 2 zombie allies, 12s ──
    private void rcGravescepter(Player p) {
        p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1.5f, 0.5f);
        for (int i = 0; i < 2; i++) {
            Location spawnLoc = p.getLocation().add(Math.random() * 2 - 1, 0, Math.random() * 2 - 1);
            Zombie zombie = (Zombie) p.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);
            zombie.customName(Component.text(p.getName() + "'s Servant", NamedTextColor.GRAY));
            zombie.setCustomNameVisible(true);
            zombie.setBaby(false);
            zombie.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
            zombie.setTarget(getTargetEntity(p, 15.0));
            p.getWorld().spawnParticle(Particle.SOUL, spawnLoc.add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0.03);
            Bukkit.getScheduler().runTaskLater(plugin, () -> { if (!zombie.isDead()) zombie.remove(); }, 240L);
        }
        p.sendActionBar(Component.text("\u2620 Grave Rise! 2 undead summoned", NamedTextColor.DARK_GRAY));
    }

    // ── #27 LYCANBANE: Silver Strike — 14 dmg + clears buffs ──
    private void rcLycanbane(Player p) {
        LivingEntity target = getTargetEntity(p, 5.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        dealDamage(p, target, 14.0);
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

    // ── #28 GLOOMSTEEL KATANA: Quick Draw — 5-block dash + 10 dmg ──
    private void rcGloomsteelKatana(Player p) {
        p.setVelocity(p.getLocation().getDirection().multiply(2.0));
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2.0f, 2.0f);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (LivingEntity e : getNearbyEnemies(p.getLocation(), 2.5, p)) {
                dealDamage(p, e, 10.0);
                e.getWorld().spawnParticle(Particle.SWEEP_ATTACK, e.getLocation().add(0, 1, 0), 3, 0, 0, 0, 0);
            }
        }, 4L);
        p.sendActionBar(Component.text("\u26A1 Quick Draw!", NamedTextColor.DARK_GRAY));
    }

    // ── #29 VIRIDIAN CLEAVER: Verdant Slam — 5-block AoE, 12 dmg + Slow ──
    private void rcViridianCleaver(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.ITEM_MACE_SMASH_GROUND, 1.5f, 0.9f);
        p.getWorld().spawnParticle(Particle.COMPOSTER, c, 50, 2.5, 1, 2.5, 0.1);
        for (LivingEntity e : getNearbyEnemies(c, 5.0, p)) {
            dealDamage(p, e, 12.0);
            e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
        }
        p.sendActionBar(Component.text("\u2618 Verdant Slam!", NamedTextColor.GREEN));
    }

    // ── #30 CRESCENT EDGE: Lunar Cleave — 180 arc 6 blocks, 10 dmg ──
    private void rcCrescentEdge(Player p) {
        Location c = p.getLocation();
        Vector facing = c.getDirection().normalize();
        p.playSound(c, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 1.0f);
        p.getWorld().spawnParticle(Particle.SWEEP_ATTACK, c.add(0, 1, 0), 20, 3, 1, 3, 0);
        for (LivingEntity e : getNearbyEnemies(c, 6.0, p)) {
            Vector toE = e.getLocation().toVector().subtract(c.toVector()).normalize();
            if (facing.dot(toE) > 0) dealDamage(p, e, 10.0);
        }
        p.sendActionBar(Component.text("\u263D Lunar Cleave!", NamedTextColor.YELLOW));
    }

    // ── #31 GRAVECLEAVER: Bone Shatter — 15 dmg + armor reduction ──
    private void rcGravecleaver(Player p) {
        LivingEntity target = getTargetEntity(p, 5.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        dealDamage(p, target, 15.0);
        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 1));
        p.playSound(p.getLocation(), Sound.ENTITY_SKELETON_HURT, 1.5f, 0.5f);
        target.getWorld().spawnParticle(Particle.BLOCK, target.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.1, Material.BONE_BLOCK.createBlockData());
        p.sendActionBar(Component.text("\u2620 Bone Shatter! Armor weakened!", NamedTextColor.GRAY));
    }

    // ── #32 AMETHYST GREATBLADE: Crystal Burst — 4-block AoE, 9 dmg + Levitation ──
    private void rcAmethystGreatblade(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 2.0f, 1.0f);
        p.getWorld().spawnParticle(Particle.END_ROD, c.add(0, 1, 0), 40, 2, 2, 2, 0.1);
        for (LivingEntity e : getNearbyEnemies(c, 4.0, p)) {
            dealDamage(p, e, 9.0);
            e.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 40, 1));
        }
        p.sendActionBar(Component.text("\u2666 Crystal Burst!", NamedTextColor.LIGHT_PURPLE));
    }

    // ── #33 FLAMBERGE: Flame Wave — 6-block cone, 10 dmg + Fire ──
    private void rcFlamberge(Player p) {
        Location c = p.getLocation().add(p.getLocation().getDirection().multiply(3));
        p.playSound(c, Sound.ENTITY_BLAZE_SHOOT, 1.5f, 1.0f);
        p.getWorld().spawnParticle(Particle.FLAME, c, 40, 3, 1, 3, 0.1);
        for (LivingEntity e : getNearbyEnemies(c, 6.0, p)) {
            dealDamage(p, e, 10.0);
            e.setFireTicks(80);
        }
        p.sendActionBar(Component.text("\u2600 Flame Wave!", NamedTextColor.RED));
    }

    // ── #34 CRYSTAL FROSTBLADE: Frost Spike — 8-block projectile, 10 dmg + Slow ──
    private void rcCrystalFrostblade(Player p) {
        Vector dir = p.getLocation().getDirection().normalize();
        Location start = p.getEyeLocation();
        p.playSound(start, Sound.BLOCK_GLASS_BREAK, 1.5f, 1.5f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick > 8) { cancel(); return; }
                Location point = start.clone().add(dir.clone().multiply(tick));
                p.getWorld().spawnParticle(Particle.SNOWFLAKE, point, 5, 0.1, 0.1, 0.1, 0);
                for (LivingEntity e : getNearbyEnemies(point, 1.5, p)) {
                    dealDamage(p, e, 10.0);
                    e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
                    cancel();
                    return;
                }
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2744 Frost Spike!", NamedTextColor.AQUA));
    }

    // ── #35 DEMONSLAYER: Holy Rend — +50% to Undead, 14 normal / 21 undead ──
    private void rcDemonslayer(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.5f, 1.0f);
        p.getWorld().spawnParticle(Particle.END_ROD, c.add(0, 1, 0), 20, 2, 1, 2, 0.05);
        for (LivingEntity e : getNearbyEnemies(c, 5.0, p)) {
            boolean undead = e.getType() == EntityType.ZOMBIE || e.getType() == EntityType.SKELETON
                    || e.getType() == EntityType.WITHER_SKELETON || e.getType() == EntityType.PHANTOM
                    || e.getType() == EntityType.DROWNED || e.getType() == EntityType.HUSK
                    || e.getType() == EntityType.STRAY || e.getType() == EntityType.ZOMBIFIED_PIGLIN
                    || e.getType() == EntityType.ZOGLIN || e.getType() == EntityType.WITHER;
            dealDamage(p, e, undead ? 21.0 : 14.0);
        }
        p.sendActionBar(Component.text("\u2694 Holy Rend! +50% vs Undead", NamedTextColor.YELLOW));
    }

    // ── #36 VENGEANCE: Retribution — Store damage for 8s, release as AoE ──
    private void rcVengeance(Player p) {
        UUID uid = p.getUniqueId();
        retributionDamageStored.put(uid, 0.0);
        retributionExpiry.put(uid, System.currentTimeMillis() + 8000L);
        p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.8f, 1.5f);
        p.sendActionBar(Component.text("\u2694 Retribution active! Taking damage for 8s...", NamedTextColor.RED));
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            double stored = retributionDamageStored.getOrDefault(uid, 0.0);
            double release = stored * 1.5;
            retributionDamageStored.remove(uid);
            retributionExpiry.remove(uid);
            if (release > 0) {
                for (LivingEntity e : getNearbyEnemies(p.getLocation(), 6.0, p)) {
                    dealDamage(p, e, release);
                }
                p.getWorld().spawnParticle(Particle.EXPLOSION, p.getLocation().add(0, 1, 0), 5, 2, 1, 2, 0);
                p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 1.0f);
                p.sendActionBar(Component.text("\u2694 Retribution released! " + String.format("%.0f", release) + " damage!", NamedTextColor.RED).decorate(TextDecoration.BOLD));
            }
        }, 160L);
    }

    // ── #37 OCULUS: All-Seeing Strike — teleport to nearest, 12 dmg ──
    private void rcOculus(Player p) {
        LivingEntity target = getTargetEntity(p, 10.0);
        if (target == null) { p.sendActionBar(Component.text("No target within 10 blocks!", NamedTextColor.RED)); return; }
        p.getWorld().spawnParticle(Particle.REVERSE_PORTAL, p.getLocation(), 20, 0.5, 1, 0.5, 0.1);
        p.teleport(target.getLocation().subtract(target.getLocation().getDirection().normalize()));
        dealDamage(p, target, 12.0);
        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 1.0f);
        p.getWorld().spawnParticle(Particle.REVERSE_PORTAL, p.getLocation(), 20, 0.5, 1, 0.5, 0.1);
        p.sendActionBar(Component.text("\u2609 All-Seeing Strike!", NamedTextColor.LIGHT_PURPLE));
    }

    // ── #38 ANCIENT GREATSLAB: Seismic Slam — 6-block AoE, 11 dmg + launch ──
    private void rcAncientGreatslab(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.ITEM_MACE_SMASH_GROUND, 2.0f, 0.7f);
        p.getWorld().spawnParticle(Particle.BLOCK, c, 60, 3, 1, 3, 0.3, Material.STONE.createBlockData());
        for (LivingEntity e : getNearbyEnemies(c, 6.0, p)) {
            dealDamage(p, e, 11.0);
            e.setVelocity(new Vector(0, 1.0, 0));
        }
        p.sendActionBar(Component.text("\u2694 Seismic Slam!", NamedTextColor.GRAY));
    }

    // ── #39 NEPTUNE'S FANG: Riptide Slash — pierce 4 entities, 8 dmg each ──
    private void rcNeptunesFang(Player p) {
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
                for (LivingEntity e : getNearbyEnemies(point, 1.5, p)) {
                    dealDamage(p, e, 8.0);
                    e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
                    hits++;
                    if (hits >= 4) { cancel(); return; }
                }
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2263 Riptide Slash!", NamedTextColor.AQUA));
    }

    // ── #40 TIDECALLER: Tidal Spear — 12 dmg + knockback + Conduit ──
    private void rcTidecaller(Player p) {
        LivingEntity target = getTargetEntity(p, 8.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        dealDamage(p, target, 12.0);
        Vector kb = target.getLocation().toVector().subtract(p.getLocation().toVector()).normalize().multiply(2.0).setY(0.5);
        target.setVelocity(kb);
        p.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, 200, 0));
        p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_THROW, 1.5f, 0.8f);
        p.getWorld().spawnParticle(Particle.SPLASH, target.getLocation(), 30, 1, 1, 1, 0.1);
        p.sendActionBar(Component.text("\u2263 Tidal Spear! + Conduit Power", NamedTextColor.AQUA));
    }

    // ── #41 STORMFORK: Lightning Javelin — lightning on impact, 14 dmg + AoE ──
    private void rcStormfork(Player p) {
        LivingEntity target = getTargetEntity(p, 10.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        p.getWorld().strikeLightningEffect(target.getLocation());
        dealDamage(p, target, 14.0);
        for (LivingEntity e : getNearbyEnemies(target.getLocation(), 3.0, p)) {
            if (e != target) dealDamage(p, e, 7.0);
        }
        p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 1.0f);
        p.sendActionBar(Component.text("\u26A1 Lightning Javelin!", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
    }

    // ── #42 JADE REAPER: Jade Crescent — 180 sweep 7b, 10 dmg + Poison ──
    private void rcJadeReaper(Player p) {
        Location c = p.getLocation();
        Vector facing = c.getDirection().normalize();
        p.playSound(c, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 0.8f);
        p.getWorld().spawnParticle(Particle.COMPOSTER, c.add(0, 1, 0), 30, 3.5, 1, 3.5, 0);
        for (LivingEntity e : getNearbyEnemies(c, 7.0, p)) {
            Vector toE = e.getLocation().toVector().subtract(c.toVector()).normalize();
            if (facing.dot(toE) > 0) {
                dealDamage(p, e, 10.0);
                e.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 0));
            }
        }
        p.sendActionBar(Component.text("\u2618 Jade Crescent!", NamedTextColor.GREEN));
    }

    // ── #43 VINDICATOR: Executioner's Chop — +1 dmg per missing heart (max +10) ──
    private void rcVindicator(Player p) {
        LivingEntity target = getTargetEntity(p, 5.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        double maxHp = 20.0;
        if (target.getAttribute(Attribute.MAX_HEALTH) != null) maxHp = target.getAttribute(Attribute.MAX_HEALTH).getValue();
        double missing = maxHp - target.getHealth();
        double bonus = Math.min(10.0, missing / 2.0);
        double totalDmg = 11.0 + bonus;
        dealDamage(p, target, totalDmg);
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 2.0f, 0.8f);
        target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 15, 0.3, 0.3, 0.3, 0.2);
        p.sendActionBar(Component.text("\u2694 Executioner's Chop! +" + String.format("%.0f", bonus) + " bonus dmg", NamedTextColor.DARK_RED));
    }

    // ── #44 SPIDER FANG: Web Trap — cobweb projectile, root 3s + Poison ──
    private void rcSpiderFang(Player p) {
        LivingEntity target = getTargetEntity(p, 8.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 127)); // root
        target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 1));
        p.playSound(p.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 1.5f, 0.5f);
        target.getWorld().spawnParticle(Particle.BLOCK, target.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.1, Material.COBWEB.createBlockData());
        p.sendActionBar(Component.text("\u2620 Web Trap! Target rooted + poisoned", NamedTextColor.DARK_GREEN));
    }

    // ════════════════════════════════════════════════════════════════
    //  HOLD ABILITIES — ALL 44 WEAPONS
    // ════════════════════════════════════════════════════════════════

    // ── #1 OCEAN'S RAGE: Riptide Surge — launch forward in water ──
    private void holdOceansRage(Player p) {
        p.setVelocity(p.getLocation().getDirection().multiply(3.0));
        p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_3, 2.0f, 1.0f);
        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (ticks > 20) { cancel(); return; }
                p.getWorld().spawnParticle(Particle.SPLASH, p.getLocation(), 10, 0.3, 0.3, 0.3, 0.1);
                p.getWorld().spawnParticle(Particle.BUBBLE, p.getLocation(), 5, 0.2, 0.2, 0.2, 0.05);
                for (LivingEntity e : getNearbyEnemies(p.getLocation(), 2.0, p)) dealDamage(p, e, 8.0);
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2248 RIPTIDE SURGE! \u2248", NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
    }

    // ── #2 AQUATIC SACRED BLADE: Depth Pressure — 10-block Slowness + Mining Fatigue ──
    private void holdAquaticSacredBlade(Player p) {
        for (LivingEntity e : getNearbyEnemies(p.getLocation(), 10.0, p)) {
            e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 120, 2));
            e.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 120, 0));
        }
        p.playSound(p.getLocation(), Sound.AMBIENT_UNDERWATER_ENTER, 2.0f, 0.5f);
        p.getWorld().spawnParticle(Particle.BUBBLE, p.getLocation().add(0, 1, 0), 60, 5, 3, 5, 0.1);
        p.sendActionBar(Component.text("\u2248 Depth Pressure! Enemies weakened", NamedTextColor.DARK_AQUA));
    }

    // ── #3 TRUE EXCALIBUR: Divine Shield — 5s invulnerability + Strength II ──
    private void holdTrueExcalibur(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 4)); // Resistance V = invulnerable
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100, 1));
        p.playSound(p.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.2f);
        p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, p.getLocation(), 50, 1, 2, 1, 0.3);
        p.sendActionBar(Component.text("\u2726 DIVINE SHIELD! 5s invulnerable + Strength II", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // ── #4 REQUIEM: Abyss Gate — Summon 3 wither skeletons for 15s ──
    private void holdRequiemNinthAbyss(Player p) {
        p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.8f, 1.5f);
        for (int i = 0; i < 3; i++) {
            Location spawnLoc = p.getLocation().add(Math.random() * 3 - 1.5, 0, Math.random() * 3 - 1.5);
            WitherSkeleton ws = (WitherSkeleton) p.getWorld().spawnEntity(spawnLoc, EntityType.WITHER_SKELETON);
            ws.customName(Component.text("Abyss Servant", NamedTextColor.DARK_PURPLE));
            ws.setCustomNameVisible(true);
            ws.getEquipment().setItemInMainHand(new ItemStack(Material.STONE_SWORD));
            ws.setTarget(getTargetEntity(p, 15.0));
            p.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, spawnLoc, 10, 0.3, 0.5, 0.3, 0.03);
            Bukkit.getScheduler().runTaskLater(plugin, () -> { if (!ws.isDead()) ws.remove(); }, 300L);
        }
        p.sendActionBar(Component.text("\u2620 ABYSS GATE! 3 Wither Skeletons summoned", NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
    }

    // ── #5 ROYAL CHAKRAM: Spinning Shield — 3s projectile deflection + 50% DR ──
    private void holdRoyalChakram(Player p) {
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
        }.runTaskTimer(plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u25C6 Spinning Shield! 3s defense", NamedTextColor.GOLD));
    }

    // ── #6 BERSERKER'S GREATAXE: Blood Rage — +50% dmg, +30% speed, -30% def 10s ──
    private void holdBerserkersGreataxe(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 2));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
        p.playSound(p.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 2.0f, 0.8f);
        p.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, p.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.1);
        p.sendActionBar(Component.text("\u2620 BLOOD RAGE! +Str III +Speed II for 10s", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
    }

    // ── #7 ACIDIC CLEAVER: Corrosive Aura — 6-block 1 heart/s for 8s ──
    private void holdAcidicCleaver(Player p) {
        p.playSound(p.getLocation(), Sound.ENTITY_LLAMA_SPIT, 1.0f, 0.3f);
        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (ticks >= 160 || !p.isOnline()) { cancel(); return; }
                if (ticks % 20 == 0) {
                    for (LivingEntity e : getNearbyEnemies(p.getLocation(), 6.0, p)) {
                        e.damage(2.0);
                        e.getWorld().spawnParticle(Particle.ITEM_SLIME, e.getLocation().add(0, 1, 0), 5, 0.2, 0.2, 0.2, 0);
                    }
                }
                if (ticks % 5 == 0) {
                    p.getWorld().spawnParticle(Particle.ITEM_SLIME, p.getLocation().add(0, 0.5, 0), 8, 3, 0.5, 3, 0);
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2622 Corrosive Aura! 8s drain", NamedTextColor.GREEN));
    }

    // ── #8 BLACK IRON GREATSWORD: Iron Fortress — Absorption IV + Resistance II 8s ──
    private void holdBlackIronGreatsword(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 160, 3));
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 160, 1));
        p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 2.0f, 0.8f);
        p.getWorld().spawnParticle(Particle.BLOCK, p.getLocation(), 30, 1, 1, 1, 0.1, Material.IRON_BLOCK.createBlockData());
        p.sendActionBar(Component.text("\u2694 IRON FORTRESS! Absorption IV + Resistance II", NamedTextColor.DARK_GRAY).decorate(TextDecoration.BOLD));
    }

    // ── #9 MURAMASA: Bloodlust — each kill +2 dmg, stacks 5x, 20s ──
    private void holdMuramasa(Player p) {
        UUID uid = p.getUniqueId();
        bloodlustStacks.put(uid, 0);
        bloodlustExpiry.put(uid, System.currentTimeMillis() + 20000L);
        p.playSound(p.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1.5f, 1.5f);
        p.sendActionBar(Component.text("\u2620 Bloodlust active! Kill to stack damage", NamedTextColor.RED));
    }

    // ── #10 PHOENIX'S GRACE: Rebirth Flame — revive at 50% HP within 60s ──
    private void holdPhoenixsGrace(Player p) {
        UUID uid = p.getUniqueId();
        rebornReady.put(uid, true);
        rebornExpiry.put(uid, System.currentTimeMillis() + 60000L);
        p.playSound(p.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 1.5f, 1.0f);
        p.getWorld().spawnParticle(Particle.FLAME, p.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.05);
        p.sendActionBar(Component.text("\u2600 Rebirth Flame ready! You will revive once within 60s", NamedTextColor.GOLD));
    }

    // ── #11 SOUL COLLECTOR: Spirit Army — release stored souls as projectiles ──
    private void holdSoulCollector(Player p) {
        int souls = soulCount.getOrDefault(p.getUniqueId(), 0);
        if (souls <= 0) { p.sendActionBar(Component.text("No souls stored! Kill enemies first", NamedTextColor.RED)); return; }
        soulCount.put(p.getUniqueId(), 0);
        Vector dir = p.getLocation().getDirection().normalize();
        p.playSound(p.getLocation(), Sound.ENTITY_VEX_CHARGE, 1.5f, 0.5f);
        for (int i = 0; i < souls; i++) {
            final int delay = i * 4;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Location start = p.getEyeLocation();
                new BukkitRunnable() {
                    Location loc = start.clone();
                    int tick = 0;
                    @Override public void run() {
                        if (tick > 15) { cancel(); return; }
                        loc.add(dir);
                        p.getWorld().spawnParticle(Particle.SOUL, loc, 3, 0.1, 0.1, 0.1, 0.01);
                        for (LivingEntity e : getNearbyEnemies(loc, 1.5, p)) {
                            dealDamage(p, e, 6.0);
                            cancel();
                            return;
                        }
                        tick++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }, delay);
        }
        p.sendActionBar(Component.text("\u2620 Spirit Army! " + souls + " souls released", NamedTextColor.DARK_PURPLE));
    }

    // ── #12 AMETHYST SHURIKEN: Shadow Step — teleport behind target + crit ──
    private void holdAmethystShuriken(Player p) {
        LivingEntity target = getTargetEntity(p, 10.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        Location behind = target.getLocation().subtract(target.getLocation().getDirection().normalize().multiply(2));
        behind.setDirection(target.getLocation().toVector().subtract(behind.toVector()));
        p.getWorld().spawnParticle(Particle.SMOKE, p.getLocation(), 20, 0.5, 1, 0.5, 0.05);
        p.teleport(behind);
        dealDamage(p, target, p.getInventory().getItemInMainHand().getType() == Material.DIAMOND_SWORD ? 22.0 : 22.0);
        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 1.5f);
        p.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.2);
        p.sendActionBar(Component.text("\u2694 Shadow Step! Critical hit!", NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
    }

    // ── #13 VALHAKYRA: Wings of Valor — 8s slow-fall + Strength I ──
    private void holdValhakyra(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 160, 0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 160, 0));
        p.setVelocity(new Vector(0, 1.0, 0));
        p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.5f, 1.5f);
        p.sendActionBar(Component.text("\u2694 Wings of Valor! Slow-fall + Strength I", NamedTextColor.GOLD));
    }

    // ── #14 WINDREAPER: Cyclone — 4s tornado pulls enemies + 4 dmg/s ──
    private void holdWindreaper(Player p) {
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
                    for (LivingEntity e : getNearbyEnemies(center, 5.0, p)) {
                        e.damage(4.0);
                        Vector pull = center.toVector().subtract(e.getLocation().toVector()).normalize().multiply(0.5);
                        e.setVelocity(e.getVelocity().add(pull));
                    }
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2601 Cyclone! 4s tornado", NamedTextColor.WHITE));
    }

    // ── #15 PHANTOMGUARD: Phase Shift — 3s intangibility ──
    private void holdPhantomguard(Player p) {
        phaseShiftActive.put(p.getUniqueId(), true);
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 4)); // invuln
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 0));
        p.playSound(p.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1.5f, 0.5f);
        p.getWorld().spawnParticle(Particle.REVERSE_PORTAL, p.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            phaseShiftActive.put(p.getUniqueId(), false);
            p.sendActionBar(Component.text("Phase Shift ended", NamedTextColor.GRAY));
        }, 60L);
        p.sendActionBar(Component.text("\u2620 PHASE SHIFT! 3s intangible", NamedTextColor.GRAY).decorate(TextDecoration.BOLD));
    }

    // ── #16 MOONLIGHT: Eclipse — 6s Blindness + Weakness to enemies in 15 blocks ──
    private void holdMoonlight(Player p) {
        for (LivingEntity e : getNearbyEnemies(p.getLocation(), 15.0, p)) {
            e.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 120, 0));
            e.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 120, 1));
        }
        p.playSound(p.getLocation(), Sound.AMBIENT_CAVE, 2.0f, 0.3f);
        p.getWorld().spawnParticle(Particle.SMOKE, p.getLocation().add(0, 3, 0), 80, 7, 3, 7, 0.01);
        p.sendActionBar(Component.text("\u263D ECLIPSE! All enemies blinded + weakened", NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
    }

    // ── #17 ZENITH: Ascension — 10s Flight + 50% damage ──
    private void holdZenith(Player p) {
        p.setAllowFlight(true);
        p.setFlying(true);
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 2));
        p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 2.0f, 1.0f);
        p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, p.getLocation(), 50, 1, 2, 1, 0.5);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR) {
                p.setFlying(false);
                p.setAllowFlight(false);
            }
            p.sendActionBar(Component.text("Ascension ended", NamedTextColor.GRAY));
        }, 200L);
        p.sendActionBar(Component.text("\u2726 ASCENSION! 10s Flight + Str III", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // ── #18 SOLSTICE: Daybreak — Remove negatives + Regen IV 6s ──
    private void holdSolstice(Player p) {
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
    private void holdGrandClaymore(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 120, 1));
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 120, 1));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 120, 0)); // rooted feel
        p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 2.0f, 0.6f);
        p.getWorld().spawnParticle(Particle.BLOCK, p.getLocation(), 30, 1, 0, 1, 0.1, Material.IRON_BLOCK.createBlockData());
        p.sendActionBar(Component.text("\u2694 COLOSSUS STANCE! 6s KB-immune + Str II", NamedTextColor.WHITE).decorate(TextDecoration.BOLD));
    }

    // ── #20 CALAMITY BLADE: Doomsday — 8s double damage, -1 heart/s ──
    private void holdCalamityBlade(Player p) {
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
        }.runTaskTimer(plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2622 DOOMSDAY! 8s Str IV but losing 1 heart/s", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
    }

    // ── #21 DRAGON SWORD: Draconic Roar — 8-block Fear + Weakness ──
    private void holdDragonSword(Player p) {
        for (LivingEntity e : getNearbyEnemies(p.getLocation(), 8.0, p)) {
            Vector away = e.getLocation().toVector().subtract(p.getLocation().toVector()).normalize().multiply(1.5).setY(0.3);
            e.setVelocity(away);
            e.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));
        }
        p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.8f);
        p.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, p.getLocation().add(0, 1, 0), 40, 4, 2, 4, 0.05);
        p.sendActionBar(Component.text("\u2620 DRACONIC ROAR! Enemies fleeing", NamedTextColor.RED).decorate(TextDecoration.BOLD));
    }

    // ── #22 TALONBRAND: Predator's Mark — Target takes +30% for 10s ──
    private void holdTalonbrand(Player p) {
        LivingEntity target = getTargetEntity(p, 10.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        predatorMarkTarget.put(p.getUniqueId(), target.getUniqueId());
        predatorMarkExpiry.put(p.getUniqueId(), System.currentTimeMillis() + 10000L);
        target.setGlowing(true);
        Bukkit.getScheduler().runTaskLater(plugin, () -> target.setGlowing(false), 200L);
        p.playSound(p.getLocation(), Sound.ENTITY_WOLF_GROWL, 1.5f, 0.5f);
        p.sendActionBar(Component.text("\u2694 Predator's Mark! Target takes +30% for 10s", NamedTextColor.DARK_RED));
    }

    // ── #23 EMERALD GREATCLEAVER: Gem Barrier — Absorb next 3 hits, 15s ──
    private void holdEmeraldGreatcleaver(Player p) {
        gemBarrierCharges.put(p.getUniqueId(), 3);
        gemBarrierExpiry.put(p.getUniqueId(), System.currentTimeMillis() + 15000L);
        p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 2.0f, 1.0f);
        p.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, p.getLocation().add(0, 1, 0), 30, 1, 1, 1, 0);
        p.sendActionBar(Component.text("\u2666 Gem Barrier! 3 hit charges for 15s", NamedTextColor.GREEN));
    }

    // ── #24 DEMON'S BLOOD BLADE: Demonic Form — 10s +60% dmg + fire trail, -50% def ──
    private void holdDemonsBloodBlade(Player p) {
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
        }.runTaskTimer(plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2620 DEMONIC FORM! 10s Str IV + fire trail", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
    }

    // ═══════════ UNCOMMON HOLD ABILITIES (20) ═══════════

    // ── #25 NOCTURNE: Night Cloak — 6s Invis + next hit +8 bonus ──
    private void holdNocturne(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 120, 0));
        p.playSound(p.getLocation(), Sound.ENTITY_PHANTOM_AMBIENT, 1.0f, 0.5f);
        p.getWorld().spawnParticle(Particle.SMOKE, p.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.02);
        p.sendActionBar(Component.text("\u263D Night Cloak! Invisible + next hit +8 dmg", NamedTextColor.DARK_GRAY));
    }

    // ── #26 GRAVESCEPTER: Death's Grasp — Root target 3s ──
    private void holdGravescepter(Player p) {
        LivingEntity target = getTargetEntity(p, 8.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 127));
        p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1.0f, 0.5f);
        target.getWorld().spawnParticle(Particle.SOUL, target.getLocation(), 20, 0.3, 0.5, 0.3, 0.03);
        p.sendActionBar(Component.text("\u2620 Death's Grasp! Target rooted 3s", NamedTextColor.DARK_GRAY));
    }

    // ── #27 LYCANBANE: Hunter's Sense — 10s Glowing on all within 20 blocks ──
    private void holdLycanbane(Player p) {
        List<LivingEntity> entities = getNearbyEnemies(p.getLocation(), 20.0, p);
        for (LivingEntity e : entities) {
            e.setGlowing(true);
            Bukkit.getScheduler().runTaskLater(plugin, () -> e.setGlowing(false), 200L);
        }
        p.playSound(p.getLocation(), Sound.ENTITY_WOLF_AMBIENT, 2.0f, 0.8f);
        p.sendActionBar(Component.text("\u2694 Hunter's Sense! " + entities.size() + " entities revealed", NamedTextColor.WHITE));
    }

    // ── #28 GLOOMSTEEL KATANA: Shadow Stance — 5s 25% dodge ──
    private void holdGloomsteelKatana(Player p) {
        shadowStanceActive.put(p.getUniqueId(), true);
        shadowStanceExpiry.put(p.getUniqueId(), System.currentTimeMillis() + 5000L);
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 2.0f);
        p.getWorld().spawnParticle(Particle.SMOKE, p.getLocation().add(0, 1, 0), 15, 0.5, 1, 0.5, 0.02);
        Bukkit.getScheduler().runTaskLater(plugin, () -> shadowStanceActive.put(p.getUniqueId(), false), 100L);
        p.sendActionBar(Component.text("\u2694 Shadow Stance! 25% dodge for 5s", NamedTextColor.DARK_GRAY));
    }

    // ── #29 VIRIDIAN CLEAVER: Overgrowth — vine DoT + root ──
    private void holdViridianCleaver(Player p) {
        LivingEntity target = getTargetEntity(p, 8.0);
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
        }.runTaskTimer(plugin, 0L, 20L);
        p.sendActionBar(Component.text("\u2618 Overgrowth! Target rooted + DoT", NamedTextColor.GREEN));
    }

    // ── #30 CRESCENT EDGE: Crescent Guard — parry next melee hit, reflect 100% ──
    private void holdCrescentEdge(Player p) {
        crescentParryActive.put(p.getUniqueId(), true);
        p.playSound(p.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.5f, 0.8f);
        p.getWorld().spawnParticle(Particle.ENCHANT, p.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.1);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (crescentParryActive.getOrDefault(p.getUniqueId(), false)) {
                                crescentParryActive.put(p.getUniqueId(), false);
                p.sendActionBar(Component.text("Crescent Guard expired", NamedTextColor.GRAY));
            }
        }, 80L); // 4s window
        p.sendActionBar(Component.text("\u263D Crescent Guard! Parry next hit", NamedTextColor.YELLOW));
    }

    // ── #31 GRAVECLEAVER: Undying Rage — 8s: survive lethal at 1 HP once ──
    private void holdGravecleaver(Player p) {
        UUID uid = p.getUniqueId();
        undyingRageActive.put(uid, true);
        undyingRageExpiry.put(uid, System.currentTimeMillis() + 8000L);
        p.playSound(p.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 1.5f, 0.5f);
        p.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, p.getLocation().add(0, 1, 0), 15, 0.5, 1, 0.5, 0.1);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (undyingRageActive.getOrDefault(uid, false)) {
                undyingRageActive.put(uid, false);
                p.sendActionBar(Component.text("Undying Rage expired", NamedTextColor.GRAY));
            }
        }, 160L);
        p.sendActionBar(Component.text("\u2620 UNDYING RAGE! Survive lethal hit once for 8s", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
    }

    // ── #32 AMETHYST GREATBLADE: Gem Resonance — 8s Strength I to allies in 10 blocks ──
    private void holdAmethystGreatblade(Player p) {
        p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 2.0f, 1.2f);
        p.getWorld().spawnParticle(Particle.END_ROD, p.getLocation().add(0, 1, 0), 30, 5, 2, 5, 0.02);
        for (Entity e : p.getWorld().getNearbyEntities(p.getLocation(), 10, 10, 10)) {
            if (e instanceof Player ally && !ally.getUniqueId().equals(p.getUniqueId())) {
                if (guildManager.areInSameGuild(p.getUniqueId(), ally.getUniqueId())) {
                    ally.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 160, 0));
                    ally.sendActionBar(Component.text("\u2666 Gem Resonance from " + p.getName() + "!", NamedTextColor.LIGHT_PURPLE));
                }
            }
        }
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 160, 0));
        p.sendActionBar(Component.text("\u2666 Gem Resonance! Strength I to nearby allies", NamedTextColor.LIGHT_PURPLE));
    }

    // ── #33 FLAMBERGE: Ember Shield — 5s: attackers take 4 fire dmg ──
    private void holdFlamberge(Player p) {
        UUID uid = p.getUniqueId();
        emberShieldActive.put(uid, true);
        emberShieldExpiry.put(uid, System.currentTimeMillis() + 5000L);
        p.playSound(p.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 1.5f, 1.0f);
        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (ticks >= 100 || !p.isOnline()) {
                    emberShieldActive.put(uid, false);
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
        }.runTaskTimer(plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2600 Ember Shield! 5s fire reflect", NamedTextColor.RED));
    }

    // ── #34 CRYSTAL FROSTBLADE: Permafrost — 5-block AoE Slow II + Mining Fatigue 6s ──
    private void holdCrystalFrostblade(Player p) {
        p.playSound(p.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.5f, 0.5f);
        p.getWorld().spawnParticle(Particle.SNOWFLAKE, p.getLocation().add(0, 1, 0), 50, 2.5, 1.5, 2.5, 0.02);
        for (LivingEntity e : getNearbyEnemies(p.getLocation(), 5.0, p)) {
            e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 120, 1));
            e.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 120, 0));
            e.getWorld().spawnParticle(Particle.SNOWFLAKE, e.getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0);
        }
        p.sendActionBar(Component.text("\u2744 Permafrost! Enemies frozen", NamedTextColor.AQUA));
    }

    // ── #35 DEMONSLAYER: Purifying Aura — 6s: undead in 8 blocks take 2 dmg/s ──
    private void holdDemonslayer(Player p) {
        p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.5f, 1.5f);
        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (ticks >= 120 || !p.isOnline()) { cancel(); return; }
                if (ticks % 20 == 0) {
                    for (LivingEntity e : getNearbyEnemies(p.getLocation(), 8.0, p)) {
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
        }.runTaskTimer(plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2694 Purifying Aura! 6s undead burn", NamedTextColor.YELLOW));
    }

    // ── #36 VENGEANCE: Grudge Mark — Target takes +20% dmg from you for 15s ──
    private void holdVengeance(Player p) {
        LivingEntity target = getTargetEntity(p, 10.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        UUID uid = p.getUniqueId();
        grudgeTargets.put(uid, target.getUniqueId());
        grudgeExpiry.put(uid, System.currentTimeMillis() + 15000L);
        target.setGlowing(true);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            target.setGlowing(false);
            grudgeTargets.remove(uid);
            grudgeExpiry.remove(uid);
        }, 300L);
        p.playSound(p.getLocation(), Sound.ENTITY_WARDEN_ANGRY, 1.0f, 1.5f);
        target.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, target.getLocation().add(0, 2, 0), 10, 0.3, 0.3, 0.3, 0);
        p.sendActionBar(Component.text("\u2694 Grudge Mark! +20% dmg to target for 15s", NamedTextColor.RED));
    }

    // ── #37 OCULUS: Third Eye — 10s Glowing on all entities in 30 blocks ──
    private void holdOculus(Player p) {
        List<LivingEntity> entities = getNearbyEnemies(p.getLocation(), 30.0, p);
        for (LivingEntity e : entities) {
            e.setGlowing(true);
            Bukkit.getScheduler().runTaskLater(plugin, () -> e.setGlowing(false), 200L);
        }
        p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 200, 0));
        p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 2.0f);
        p.getWorld().spawnParticle(Particle.ENCHANT, p.getLocation().add(0, 2, 0), 40, 2, 2, 2, 0.5);
        p.sendActionBar(Component.text("\u2609 Third Eye! " + entities.size() + " entities revealed + Night Vision", NamedTextColor.LIGHT_PURPLE));
    }

    // ── #38 ANCIENT GREATSLAB: Stone Skin — 6s Resistance II + KB-immune ──
    private void holdAncientGreatslab(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 120, 1));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 120, 0)); // weight feel
        p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 0.5f);
        p.getWorld().spawnParticle(Particle.BLOCK, p.getLocation(), 40, 1, 1, 1, 0.1, Material.STONE.createBlockData());
        p.sendActionBar(Component.text("\u2694 Stone Skin! 6s Resistance II", NamedTextColor.GRAY));
    }

    // ── #39 NEPTUNE'S FANG: Maelstrom — 6-block water vortex, pull + 3 dmg/s 5s ──
    private void holdNeptunesFang(Player p) {
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
                    for (LivingEntity e : getNearbyEnemies(center, 6.0, p)) {
                        e.damage(6.0);
                        Vector pull = center.toVector().subtract(e.getLocation().toVector()).normalize().multiply(0.6).setY(0.2);
                        e.setVelocity(e.getVelocity().add(pull));
                    }
                    p.getWorld().playSound(center, Sound.ENTITY_GENERIC_SPLASH, 1.0f, 0.8f);
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2248 MAELSTROM! 5s water vortex", NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
    }

    // ── #40 TIDECALLER: Depth Ward — 8s Dolphin's Grace + Respiration + Drowned-immune ──
    private void holdTidecaller(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 160, 0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 160, 0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, 160, 0));
        p.playSound(p.getLocation(), Sound.AMBIENT_UNDERWATER_ENTER, 1.5f, 1.0f);
        p.getWorld().spawnParticle(Particle.BUBBLE, p.getLocation().add(0, 1, 0), 40, 1, 2, 1, 0.1);
        p.sendActionBar(Component.text("\u2248 Depth Ward! 8s Dolphin's Grace + Water Breathing", NamedTextColor.AQUA));
    }

    // ── #41 STORMFORK: Thunder Shield — 6s: attackers get lightning ──
    private void holdStormfork(Player p) {
        UUID uid = p.getUniqueId();
        thunderShieldActive.put(uid, true);
        thunderShieldExpiry.put(uid, System.currentTimeMillis() + 6000L);
        p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.5f, 1.5f);
        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (ticks >= 120 || !p.isOnline()) {
                    thunderShieldActive.put(uid, false);
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
        }.runTaskTimer(plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u26A1 Thunder Shield! 6s lightning counter", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
    }

    // ── #42 JADE REAPER: Emerald Harvest — 10s: kills drop 1-3 emeralds ──
    private void holdJadeReaper(Player p) {
        p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.5f, 0.8f);
        p.getWorld().spawnParticle(Particle.COMPOSTER, p.getLocation().add(0, 1, 0), 20, 1, 1, 1, 0.05);
        // Store activation time — checked in a kill listener
        // For simplicity, use a potion effect as a marker
        p.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 200, 0));
        p.sendActionBar(Component.text("\u2618 Emerald Harvest! Kills drop emeralds for 10s", NamedTextColor.GREEN));
    }

    // ── #43 VINDICATOR: Rally Cry — 6s: allies in 8 blocks get Speed I + Strength I ──
    private void holdVindicator(Player p) {
        p.playSound(p.getLocation(), Sound.ENTITY_RAVAGER_CELEBRATE, 2.0f, 1.2f);
        p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, p.getLocation().add(0, 1, 0), 20, 2, 2, 2, 0.1);
        for (Entity e : p.getWorld().getNearbyEntities(p.getLocation(), 8, 8, 8)) {
            if (e instanceof Player ally) {
                if (ally.getUniqueId().equals(p.getUniqueId()) ||
                        guildManager.areInSameGuild(p.getUniqueId(), ally.getUniqueId())) {
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
    private void holdSpiderFang(Player p) {
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
        }.runTaskTimer(plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2620 Wall Crawler! 8s wall-climb (sneak near walls) + Night Vision", NamedTextColor.DARK_GREEN));
    }

    // ════════════════════════════════════════════════════════════════
    //  HELPER METHODS
    // ════════════════════════════════════════════════════════════════

    /**
     * Gets the nearest living entity the player is looking at (ray-cast style).
     * Searches entities within the given range in the player's look direction.
     */
    private LivingEntity getTargetEntity(Player player, double range) {
        Vector dir = player.getLocation().getDirection().normalize();
        Location start = player.getEyeLocation();
        LivingEntity closest = null;
        double closestDist = range + 1;

        for (Entity e : player.getWorld().getNearbyEntities(player.getLocation(), range, range, range)) {
            if (!(e instanceof LivingEntity le) || e instanceof ArmorStand || e == player) continue;
            if (le instanceof Player target) {
                if (target.getGameMode() == GameMode.CREATIVE || target.getGameMode() == GameMode.SPECTATOR) continue;
                if (guildManager.areInSameGuild(player.getUniqueId(), target.getUniqueId())) continue;
            }

            // Check if entity is roughly in the direction the player is looking
            Vector toEntity = le.getLocation().add(0, 1, 0).toVector().subtract(start.toVector());
            double distance = toEntity.length();
            if (distance > range) continue;

            double dot = dir.dot(toEntity.normalize());
            if (dot > 0.85) { // within ~30-degree cone
                if (distance < closestDist) {
                    closestDist = distance;
                    closest = le;
                }
            }
        }
        return closest;
    }

    /**
     * Rotates a vector around the Y axis by the given angle in radians.
     */
    private Vector rotateY(Vector v, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x = v.getX() * cos - v.getZ() * sin;
        double z = v.getX() * sin + v.getZ() * cos;
        return v.setX(x).setZ(z);
    }

    // ================================================================
    // PRIMARY ABILITIES - NEW MYTHIC WEAPONS #45-59 (Phase 9)
    // ================================================================

    // #45 DIVINE AXE RHITTA: Cruel Sun - massive fire AoE 10 blocks, 24 dmg
    private void rcDivineAxeRhitta(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.ENTITY_BLAZE_SHOOT, 2.0f, 0.4f);
        p.playSound(c, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.5f, 0.6f);
        p.getWorld().spawnParticle(Particle.FLAME, c.clone().add(0, 3, 0), 100, 5, 3, 5, 0.15);
        p.getWorld().spawnParticle(Particle.LAVA, c.clone().add(0, 2, 0), 50, 4, 2, 4, 0);
        p.getWorld().spawnParticle(Particle.DUST, c.clone().add(0, 5, 0), 30, 3, 1, 3, 0, new Particle.DustOptions(Color.fromRGB(255, 165, 0), 3.0f));
        for (LivingEntity e : getNearbyEnemies(c, 10.0, p)) {
            dealDamage(p, e, 24.0);
            e.setFireTicks(200);
            e.setVelocity(e.getLocation().toVector().subtract(c.toVector()).normalize().multiply(1.5).setY(0.8));
        }
        p.sendActionBar(Component.text("\u2600 CRUEL SUN! \u2600", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // #46 YORU: World's Strongest Slash - 20-block line, 22 dmg, cuts through everything
    private void rcYoru(Player p) {
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
                for (LivingEntity e : getNearbyEnemies(point, 2.0, p)) { dealDamage(p, e, 22.0); }
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2694 WORLD'S STRONGEST SLASH! \u2694", NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
    }

    // #47 TENGEN'S BLADE: Sound Breathing - 8 rapid slashes in cone, 4 dmg each
    private void rcTengensBlade(Player p) {
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2.0f, 2.0f);
        Location c = p.getLocation().add(p.getLocation().getDirection().multiply(3));
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 8) { cancel(); return; }
                p.getWorld().spawnParticle(Particle.SWEEP_ATTACK, c.clone().add(Math.sin(tick * 0.8), 1, Math.cos(tick * 0.8)), 3, 0.5, 0.5, 0.5, 0);
                p.getWorld().spawnParticle(Particle.NOTE, c.clone().add(0, 2, 0), 5, 1, 0.5, 1, 1);
                for (LivingEntity e : getNearbyEnemies(c, 5.0, p)) { dealDamage(p, e, 4.0); }
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.5f, 1.5f + (tick * 0.1f));
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
        p.sendActionBar(Component.text("\u266B Sound Breathing! \u266B", NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD));
    }

    // #48 EDGE OF THE ASTRAL PLANE: Astral Rend - teleport 8 blocks forward, 20 dmg slash
    private void rcEdgeAstralPlane(Player p) {
        Location origin = p.getLocation();
        Vector dir = origin.getDirection().normalize();
        Location target = origin.clone().add(dir.multiply(8));
        target.setY(p.getWorld().getHighestBlockYAt(target) + 1);
        p.teleport(target);
        p.playSound(target, Sound.ENTITY_ENDERMAN_TELEPORT, 2.0f, 0.8f);
        p.getWorld().spawnParticle(Particle.REVERSE_PORTAL, origin.add(0, 1, 0), 50, 0.5, 1, 0.5, 0.1);
        p.getWorld().spawnParticle(Particle.END_ROD, target.add(0, 1, 0), 80, 2, 2, 2, 0.1);
        for (LivingEntity e : getNearbyEnemies(target, 4.0, p)) { dealDamage(p, e, 20.0); }
        p.sendActionBar(Component.text("\u2727 Astral Rend! \u2727", NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD));
    }

    // #49 FALLEN GOD'S SPEAR: Divine Impale - single target 30 dmg + launch
    private void rcFallenGodsSpear(Player p) {
        LivingEntity target = getTargetEntity(p, 8.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        dealDamage(p, target, 30.0);
        target.setVelocity(new Vector(0, 2.5, 0));
        p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 2.0f, 1.0f);
        p.getWorld().spawnParticle(Particle.END_ROD, target.getLocation().add(0, 1, 0), 60, 0.5, 1, 0.5, 0.2);
        p.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 40, 0.5, 0.5, 0.5, 0.3);
        p.sendActionBar(Component.text("\u2694 DIVINE IMPALE! \u2694", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
    }

    // #50 NATURE SWORD: Gaia's Wrath - 8-block AoE, 18 dmg + poison + roots (slowness IV)
    private void rcNatureSword(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.BLOCK_GRASS_BREAK, 2.0f, 0.5f);
        p.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, c.add(0, 0.5, 0), 80, 4, 0.5, 4, 0);
        p.getWorld().spawnParticle(Particle.COMPOSTER, c, 40, 4, 1, 4, 0);
        for (LivingEntity e : getNearbyEnemies(c, 8.0, p)) {
            dealDamage(p, e, 18.0);
            e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 3));
            e.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
        }
        p.sendActionBar(Component.text("\u2618 Gaia's Wrath! \u2618", NamedTextColor.GREEN).decorate(TextDecoration.BOLD));
    }

    // #51 HEAVENLY PARTISAN: Holy Lance - 15-block beam, 20 dmg, heals allies 4 hearts
    private void rcHeavenlyPartisan(Player p) {
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
                for (LivingEntity e : getNearbyEnemies(point, 1.5, p)) { dealDamage(p, e, 20.0); }
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        for (Entity e : p.getLocation().getWorld().getNearbyEntities(p.getLocation(), 8, 8, 8)) {
            if (e instanceof Player ally && ally != p && guildManager.areInSameGuild(p.getUniqueId(), ally.getUniqueId())) {
                double max = ally.getAttribute(Attribute.MAX_HEALTH).getValue();
                ally.setHealth(Math.min(max, ally.getHealth() + 8.0));
                ally.getWorld().spawnParticle(Particle.HEART, ally.getLocation().add(0, 2, 0), 5, 0.3, 0.3, 0.3, 0);
            }
        }
        p.sendActionBar(Component.text("\u2694 Holy Lance! \u2694", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
    }

    // #52 SOUL DEVOURER: Soul Rip - drain 20 dmg from target, heal 50%
    private void rcSoulDevourer(Player p) {
        LivingEntity target = getTargetEntity(p, 6.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        dealDamage(p, target, 20.0);
        double heal = 10.0;
        double max = p.getAttribute(Attribute.MAX_HEALTH).getValue();
        p.setHealth(Math.min(max, p.getHealth() + heal));
        p.playSound(p.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 1.5f, 0.4f);
        p.getWorld().spawnParticle(Particle.SOUL, target.getLocation().add(0, 1, 0), 60, 0.5, 1, 0.5, 0.1);
        p.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, p.getLocation().add(0, 1, 0), 15, 0.3, 0.3, 0.3, 0);
        p.sendActionBar(Component.text("\u2620 Soul Rip! +5 hearts", NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
    }

    // #53 MJOLNIR: Thunderstrike - throw lightning at target location, 22 dmg AoE 6 blocks
    private void rcMjolnir(Player p) {
        Location target = p.getLocation().add(p.getLocation().getDirection().multiply(6));
        p.getWorld().strikeLightningEffect(target);
        p.getWorld().strikeLightningEffect(target.clone().add(2, 0, 0));
        p.getWorld().strikeLightningEffect(target.clone().add(-2, 0, 0));
        p.playSound(target, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 0.7f);
        p.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, target.add(0, 1, 0), 80, 3, 2, 3, 0.2);
        for (LivingEntity e : getNearbyEnemies(target, 6.0, p)) {
            dealDamage(p, e, 22.0);
            e.setVelocity(new Vector(0, 1.0, 0));
        }
        p.sendActionBar(Component.text("\u26A1 THUNDERSTRIKE! \u26A1", NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
    }

    // #54 THOUSAND DEMON DAGGERS: Demon Barrage - 12 projectiles, 3 dmg each
    private void rcThousandDemonDaggers(Player p) {
        p.playSound(p.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.5f, 1.5f);
        Vector baseDir = p.getLocation().getDirection().normalize();
        for (int i = -5; i <= 6; i++) {
            double angle = Math.toRadians(i * 8);
            Vector dir = rotateY(baseDir.clone(), angle);
            final Vector fDir = dir;
            new BukkitRunnable() {
                Location loc = p.getEyeLocation().clone();
                int ticks = 0;
                @Override public void run() {
                    if (ticks > 12) { cancel(); return; }
                    loc.add(fDir);
                    p.getWorld().spawnParticle(Particle.DUST, loc, 2, 0.05, 0.05, 0.05, 0, new Particle.DustOptions(Color.fromRGB(180, 0, 0), 0.8f));
                    for (LivingEntity e : getNearbyEnemies(loc, 0.8, p)) {
                        dealDamage(p, e, 3.0);
                        e.setFireTicks(40);
                        cancel(); return;
                    }
                    ticks++;
                }
            }.runTaskTimer(plugin, i + 6, 1L);
        }
        p.sendActionBar(Component.text("\u2620 Demon Barrage!", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
    }

    // #55 STAR EDGE: Cosmic Slash - 12-block line beam, 22 dmg, launches targets to sky
    private void rcStarEdge(Player p) {
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
                for (LivingEntity e : getNearbyEnemies(point, 2.0, p)) {
                    dealDamage(p, e, 22.0);
                    e.setVelocity(new Vector(0, 3.0, 0));
                }
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2726 Cosmic Slash! \u2726", NamedTextColor.WHITE).decorate(TextDecoration.BOLD));
    }

    // #56 RIVERS OF BLOOD: Corpse Piler - 6 rapid dashes in sequence, 5 dmg each
    private void rcRiversOfBlood(Player p) {
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2.0f, 1.8f);
        new BukkitRunnable() {
            int dash = 0;
            @Override public void run() {
                if (dash >= 6) { cancel(); return; }
                p.setVelocity(p.getLocation().getDirection().multiply(1.2));
                Location loc = p.getLocation();
                p.getWorld().spawnParticle(Particle.DUST, loc.add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0, new Particle.DustOptions(Color.fromRGB(180, 0, 0), 1.5f));
                p.getWorld().spawnParticle(Particle.SWEEP_ATTACK, loc, 3, 0.3, 0.3, 0.3, 0);
                for (LivingEntity e : getNearbyEnemies(loc, 3.0, p)) { dealDamage(p, e, 5.0); }
                p.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 1.5f + (dash * 0.15f));
                dash++;
            }
        }.runTaskTimer(plugin, 0L, 3L);
        p.sendActionBar(Component.text("\u2620 Corpse Piler!", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
    }

    // #57 DRAGON SLAYING BLADE: Dragon Pierce - massive single-target 35 dmg (2x vs dragon-type)
    private void rcDragonSlayingBlade(Player p) {
        LivingEntity target = getTargetEntity(p, 6.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        double dmg = 35.0;
        if (target.getType() == EntityType.ENDER_DRAGON) dmg = 70.0;
        dealDamage(p, target, dmg);
        p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 2.0f, 1.2f);
        p.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 80, 0.5, 1, 0.5, 0.3);
        p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, target.getLocation().add(0, 1, 0), 40, 0.5, 1, 0.5, 0.2);
        p.sendActionBar(Component.text("\u2694 DRAGON PIERCE! \u2694", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // #58 STOP SIGN: Full Stop - stuns all enemies in 6-block radius for 3s, 15 dmg
    private void rcStopSign(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.5f);
        p.getWorld().spawnParticle(Particle.DUST, c.add(0, 1, 0), 60, 3, 2, 3, 0, new Particle.DustOptions(Color.fromRGB(255, 0, 0), 3.0f));
        p.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, c, 2, 0, 0, 0, 0);
        for (LivingEntity e : getNearbyEnemies(c, 6.0, p)) {
            dealDamage(p, e, 15.0);
            e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 255));
            e.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 60, 255));
            e.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
        }
        p.sendActionBar(Component.text("\u26D4 FULL STOP! \u26D4", NamedTextColor.RED).decorate(TextDecoration.BOLD));
    }

    // #59 CREATION SPLITTER: Reality Cleave - 10-block cross AoE, 25 dmg
    private void rcCreationSplitter(Player p) {
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
        for (LivingEntity e : getNearbyEnemies(c, 10.0, p)) {
            dealDamage(p, e, 25.0);
            e.setVelocity(e.getLocation().toVector().subtract(c.toVector()).normalize().multiply(2.0).setY(1.0));
        }
        p.sendActionBar(Component.text("\u2726 REALITY CLEAVE! \u2726", NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD));
    }

    // ================================================================
    // ALTERNATE ABILITIES - NEW MYTHIC WEAPONS #45-59 (Phase 9)
    // ================================================================

    // #45 DIVINE AXE RHITTA ALT: Sunshine - buff: +50% damage for 10s, fire aura
    private void altDivineAxeRhitta(Player p) {
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
        }.runTaskTimer(plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2600 SUNSHINE! +Strength II for 10s", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // #46 YORU ALT: Dark Mirror - teleport behind target, 15 dmg backstab
    private void altYoru(Player p) {
        LivingEntity target = getTargetEntity(p, 10.0);
        if (target == null) { p.sendActionBar(Component.text("No target!", NamedTextColor.RED)); return; }
        Location behind = target.getLocation().clone().add(target.getLocation().getDirection().normalize().multiply(-2));
        behind.setYaw(target.getLocation().getYaw());
        behind.setPitch(0);
        p.teleport(behind);
        dealDamage(p, target, 15.0);
        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 2.0f, 0.5f);
        p.getWorld().spawnParticle(Particle.SMOKE, p.getLocation().add(0, 1, 0), 40, 0.5, 1, 0.5, 0.05);
        p.sendActionBar(Component.text("\u2694 Dark Mirror! Backstab!", NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
    }

    // #47 TENGEN'S BLADE ALT: Constant Flux - speed III + haste II for 12s
    private void altTengensBlade(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 240, 2));
        p.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 240, 1));
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 2.0f, 2.0f);
        p.getWorld().spawnParticle(Particle.NOTE, p.getLocation().add(0, 2, 0), 30, 1, 0.5, 1, 1);
        p.sendActionBar(Component.text("\u266B Constant Flux! Speed III + Haste II", NamedTextColor.LIGHT_PURPLE));
    }

    // #48 EDGE OF THE ASTRAL PLANE ALT: Planar Shift - phase through 10 blocks, invulnerable
    private void altEdgeAstralPlane(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 3));
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 4));
        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 1.5f);
        p.getWorld().spawnParticle(Particle.REVERSE_PORTAL, p.getLocation().add(0, 1, 0), 60, 0.5, 1, 0.5, 0.1);
        p.sendActionBar(Component.text("\u2727 Planar Shift! 3s invulnerability", NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD));
    }

    // #49 FALLEN GOD'S SPEAR ALT: Heaven's Fall - leap up + slam, 20 dmg AoE 8 blocks
    private void altFallenGodsSpear(Player p) {
        p.setVelocity(new Vector(0, 3.0, 0));
        p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 2.0f, 0.8f);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Location land = p.getLocation();
            p.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, land, 5, 0, 0, 0, 0);
            p.getWorld().spawnParticle(Particle.END_ROD, land, 80, 4, 1, 4, 0.2);
            p.playSound(land, Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.5f);
            for (LivingEntity e : getNearbyEnemies(land, 8.0, p)) {
                dealDamage(p, e, 20.0);
                e.setVelocity(e.getLocation().toVector().subtract(land.toVector()).normalize().multiply(2.0).setY(1.2));
            }
        }, 30L);
        p.sendActionBar(Component.text("\u2694 Heaven's Fall! \u2694", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
    }

    // #50 NATURE SWORD ALT: Overgrowth Surge - heal 8 hearts + regen III 10s + ally heal
    private void altNatureSword(Player p) {
        double max = p.getAttribute(Attribute.MAX_HEALTH).getValue();
        p.setHealth(Math.min(max, p.getHealth() + 16.0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 2));
        p.playSound(p.getLocation(), Sound.BLOCK_GRASS_BREAK, 2.0f, 1.2f);
        p.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, p.getLocation().add(0, 1, 0), 60, 2, 1, 2, 0);
        p.getWorld().spawnParticle(Particle.HEART, p.getLocation().add(0, 2, 0), 15, 1, 0.5, 1, 0);
        for (Entity e : p.getLocation().getWorld().getNearbyEntities(p.getLocation(), 6, 6, 6)) {
            if (e instanceof Player ally && ally != p && guildManager.areInSameGuild(p.getUniqueId(), ally.getUniqueId())) {
                double amax = ally.getAttribute(Attribute.MAX_HEALTH).getValue();
                ally.setHealth(Math.min(amax, ally.getHealth() + 8.0));
                ally.getWorld().spawnParticle(Particle.HEART, ally.getLocation().add(0, 2, 0), 5, 0.3, 0.3, 0.3, 0);
            }
        }
        p.sendActionBar(Component.text("\u2618 Overgrowth Surge! Full heal + Regen III", NamedTextColor.GREEN).decorate(TextDecoration.BOLD));
    }

    // #51 HEAVENLY PARTISAN ALT: Celestial Judgment - smite 3 lightning bolts on nearest enemies
    private void altHeavenlyPartisan(Player p) {
        List<LivingEntity> targets = getNearbyEnemies(p.getLocation(), 12.0, p);
        int strikes = Math.min(3, targets.size());
        for (int i = 0; i < strikes; i++) {
            LivingEntity t = targets.get(i);
            final int delay = i * 10;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                p.getWorld().strikeLightningEffect(t.getLocation());
                dealDamage(p, t, 18.0);
                t.getWorld().spawnParticle(Particle.END_ROD, t.getLocation().add(0, 1, 0), 30, 0.5, 2, 0.5, 0.1);
            }, delay);
        }
        p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 0.8f);
        p.sendActionBar(Component.text("\u2694 Celestial Judgment! " + strikes + " smites", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
    }

    // #52 SOUL DEVOURER ALT: Devouring Maw - AoE drain 12 dmg, heal per enemy hit
    private void altSoulDevourer(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.ENTITY_WITHER_HURT, 1.5f, 0.5f);
        p.getWorld().spawnParticle(Particle.SOUL, c.add(0, 1, 0), 80, 4, 2, 4, 0.05);
        int hits = 0;
        for (LivingEntity e : getNearbyEnemies(c, 6.0, p)) {
            dealDamage(p, e, 12.0);
            hits++;
        }
        double heal = hits * 4.0;
        double max = p.getAttribute(Attribute.MAX_HEALTH).getValue();
        p.setHealth(Math.min(max, p.getHealth() + heal));
        p.sendActionBar(Component.text("\u2620 Devouring Maw! Healed " + (hits * 2) + " hearts", NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
    }

    // #53 MJOLNIR ALT: Bifrost Slam - leap + triple lightning on landing
    private void altMjolnir(Player p) {
        p.setVelocity(new Vector(0, 2.5, 0).add(p.getLocation().getDirection().multiply(1.0)));
        p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 2.0f, 0.8f);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Location land = p.getLocation();
            for (int i = 0; i < 3; i++) {
                Location strike = land.clone().add(Math.sin(i * 2.1) * 3, 0, Math.cos(i * 2.1) * 3);
                p.getWorld().strikeLightningEffect(strike);
            }
            p.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, land, 100, 4, 1, 4, 0.3);
            p.playSound(land, Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.6f);
            for (LivingEntity e : getNearbyEnemies(land, 8.0, p)) {
                dealDamage(p, e, 20.0);
                e.setVelocity(new Vector(0, 1.5, 0));
            }
        }, 25L);
        p.sendActionBar(Component.text("\u26A1 BIFROST SLAM! \u26A1", NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
    }

    // #54 THOUSAND DEMON DAGGERS ALT: Infernal Dance - fire spin 6s, 4 dmg/tick to nearby
    private void altThousandDemonDaggers(Player p) {
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
                    for (LivingEntity e : getNearbyEnemies(c, 3.5, p)) {
                        dealDamage(p, e, 4.0);
                        e.setFireTicks(20);
                    }
                }
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u2620 Infernal Dance! 6s fire spin", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
    }

    // #55 STAR EDGE ALT: Supernova - massive 12-block AoE, 18 dmg + blindness
    private void altStarEdge(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
        p.getWorld().spawnParticle(Particle.END_ROD, c.add(0, 2, 0), 100, 6, 3, 6, 0.3);
        p.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, c, 3, 0, 0, 0, 0);
        for (LivingEntity e : getNearbyEnemies(c, 12.0, p)) {
            dealDamage(p, e, 18.0);
            e.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
            e.setVelocity(e.getLocation().toVector().subtract(c.toVector()).normalize().multiply(2.5).setY(1.0));
        }
        p.sendActionBar(Component.text("\u2726 SUPERNOVA! \u2726", NamedTextColor.WHITE).decorate(TextDecoration.BOLD));
    }

    // #56 RIVERS OF BLOOD ALT: Blood Tsunami - wave 10 blocks, 16 dmg + wither
    private void altRiversOfBlood(Player p) {
        Vector dir = p.getLocation().getDirection().normalize();
        Location start = p.getLocation();
        p.playSound(start, Sound.ENTITY_GENERIC_SPLASH, 2.0f, 0.4f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick > 10) { cancel(); return; }
                Location point = start.clone().add(dir.clone().multiply(tick));
                p.getWorld().spawnParticle(Particle.DUST, point.clone().add(0, 1, 0), 20, 1.5, 1, 1.5, 0, new Particle.DustOptions(Color.fromRGB(139, 0, 0), 2.0f));
                for (LivingEntity e : getNearbyEnemies(point, 3.0, p)) {
                    dealDamage(p, e, 16.0);
                    e.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 80, 1));
                }
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
        p.sendActionBar(Component.text("\u2620 Blood Tsunami!", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
    }

    // #57 DRAGON SLAYING BLADE ALT: Slayer's Fury - 10s buff: +30% damage, +resist II
    private void altDragonSlayingBlade(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 1));
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 1));
        p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.5f);
        p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, p.getLocation().add(0, 1, 0), 50, 1, 1.5, 1, 0.3);
        p.sendActionBar(Component.text("\u2694 Slayer's Fury! Strength II + Resistance II 10s", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
    }

    // #58 STOP SIGN ALT: Road Rage - charge forward 10 blocks, stun + 12 dmg along path
    private void altStopSign(Player p) {
        p.setVelocity(p.getLocation().getDirection().multiply(3.0));
        p.playSound(p.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 2.0f, 0.8f);
        new BukkitRunnable() {
            int tick = 0;
            @Override public void run() {
                if (tick >= 10) { cancel(); return; }
                Location loc = p.getLocation();
                p.getWorld().spawnParticle(Particle.DUST, loc.add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0, new Particle.DustOptions(Color.fromRGB(255, 0, 0), 2.0f));
                for (LivingEntity e : getNearbyEnemies(loc, 2.5, p)) {
                    dealDamage(p, e, 12.0);
                    e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 255));
                }
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        p.sendActionBar(Component.text("\u26D4 Road Rage! \u26D4", NamedTextColor.RED).decorate(TextDecoration.BOLD));
    }

    // #59 CREATION SPLITTER ALT: Genesis Break - massive explosion, 30 dmg, 15-block AoE
    private void altCreationSplitter(Player p) {
        Location c = p.getLocation();
        p.playSound(c, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 0.3f);
        p.playSound(c, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.3f);
        p.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, c.add(0, 2, 0), 8, 3, 2, 3, 0);
        p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, c, 100, 7, 3, 7, 0.8);
        p.getWorld().spawnParticle(Particle.END_ROD, c, 80, 7, 5, 7, 0.2);
        for (LivingEntity e : getNearbyEnemies(c, 15.0, p)) {
            dealDamage(p, e, 30.0);
            e.setVelocity(e.getLocation().toVector().subtract(c.toVector()).normalize().multiply(3.0).setY(1.5));
        }
        p.sendActionBar(Component.text("\u2726 GENESIS BREAK! \u2726", NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD));
    }
}