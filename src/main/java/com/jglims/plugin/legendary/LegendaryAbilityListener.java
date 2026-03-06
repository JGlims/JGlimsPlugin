package com.jglims.plugin.legendary;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.config.ConfigManager;
import com.jglims.plugin.guilds.GuildManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * LegendaryAbilityListener - orchestrator for all 59 legendary weapon abilities.
 * Delegates to LegendaryPrimaryAbilities and LegendaryAltAbilities.
 * v3.1.0 - Phase 8d split (rule A.17: max 120 KB per file).
 */
public class LegendaryAbilityListener implements Listener {

    private final LegendaryAbilityContext ctx;
    private final LegendaryWeaponManager weaponManager;
    private final LegendaryPrimaryAbilities primary;
    private final LegendaryAltAbilities alt;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public LegendaryAbilityListener(JGlimsPlugin plugin, ConfigManager config,
                                     LegendaryWeaponManager weaponManager,
                                     GuildManager guildManager) {
        this.ctx = new LegendaryAbilityContext(plugin, config, weaponManager, guildManager);
        this.weaponManager = weaponManager;
        this.primary = new LegendaryPrimaryAbilities(ctx);
        this.alt = new LegendaryAltAbilities(ctx);
    }

    private boolean isOnCooldown(Player player, String abilityName) {
        Map<String, Long> pc = cooldowns.get(player.getUniqueId());
        if (pc == null) return false;
        Long expiry = pc.get(abilityName);
        if (expiry == null) return false;
        if (System.currentTimeMillis() >= expiry) { pc.remove(abilityName); return false; }
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

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        LegendaryWeapon weapon = weaponManager.identify(item);
        if (weapon == null) return;
        if (player.isSneaking()) {
            handleAltAbility(player, weapon);
        } else {
            handlePrimaryAbility(player, weapon);
        }
        event.setCancelled(true);
    }

    private void handlePrimaryAbility(Player player, LegendaryWeapon weapon) {
        String name = weapon.getPrimaryAbilityName();
        int cd = weapon.getPrimaryCooldown();
        if (isOnCooldown(player, name)) { sendCooldownMsg(player, name); return; }
        setCooldown(player, name, cd);
        switch (weapon) {
            case OCEANS_RAGE -> primary.rcOceansRage(player);
            case AQUATIC_SACRED_BLADE -> primary.rcAquaticSacredBlade(player);
            case TRUE_EXCALIBUR -> primary.rcTrueExcalibur(player);
            case REQUIEM_NINTH_ABYSS -> primary.rcRequiemNinthAbyss(player);
            case ROYAL_CHAKRAM -> primary.rcRoyalChakram(player);
            case BERSERKERS_GREATAXE -> primary.rcBerserkersGreataxe(player);
            case ACIDIC_CLEAVER -> primary.rcAcidicCleaver(player);
            case BLACK_IRON_GREATSWORD -> primary.rcBlackIronGreatsword(player);
            case MURAMASA -> primary.rcMuramasa(player);
            case PHOENIXS_GRACE -> primary.rcPhoenixsGrace(player);
            case SOUL_COLLECTOR -> primary.rcSoulCollector(player);
            case AMETHYST_SHURIKEN -> primary.rcAmethystShuriken(player);
            case VALHAKYRA -> primary.rcValhakyra(player);
            case WINDREAPER -> primary.rcWindreaper(player);
            case PHANTOMGUARD -> primary.rcPhantomguard(player);
            case MOONLIGHT -> primary.rcMoonlight(player);
            case ZENITH -> primary.rcZenith(player);
            case SOLSTICE -> primary.rcSolstice(player);
            case GRAND_CLAYMORE -> primary.rcGrandClaymore(player);
            case CALAMITY_BLADE -> primary.rcCalamityBlade(player);
            case DRAGON_SWORD -> primary.rcDragonSword(player);
            case TALONBRAND -> primary.rcTalonbrand(player);
            case EMERALD_GREATCLEAVER -> primary.rcEmeraldGreatcleaver(player);
            case DEMONS_BLOOD_BLADE -> primary.rcDemonsBloodBlade(player);
            case NOCTURNE -> primary.rcNocturne(player);
            case GRAVESCEPTER -> primary.rcGravescepter(player);
            case LYCANBANE -> primary.rcLycanbane(player);
            case GLOOMSTEEL_KATANA -> primary.rcGloomsteelKatana(player);
            case VIRIDIAN_CLEAVER -> primary.rcViridianCleaver(player);
            case CRESCENT_EDGE -> primary.rcCrescentEdge(player);
            case GRAVECLEAVER -> primary.rcGravecleaver(player);
            case AMETHYST_GREATBLADE -> primary.rcAmethystGreatblade(player);
            case FLAMBERGE -> primary.rcFlamberge(player);
            case CRYSTAL_FROSTBLADE -> primary.rcCrystalFrostblade(player);
            case DEMONSLAYER -> primary.rcDemonslayer(player);
            case VENGEANCE -> primary.rcVengeance(player);
            case OCULUS -> primary.rcOculus(player);
            case ANCIENT_GREATSLAB -> primary.rcAncientGreatslab(player);
            case NEPTUNES_FANG -> primary.rcNeptunesFang(player);
            case TIDECALLER -> primary.rcTidecaller(player);
            case STORMFORK -> primary.rcStormfork(player);
            case JADE_REAPER -> primary.rcJadeReaper(player);
            case VINDICATOR -> primary.rcVindicator(player);
            case SPIDER_FANG -> primary.rcSpiderFang(player);
            case DIVINE_AXE_RHITTA -> primary.rcDivineAxeRhitta(player);
            case YORU -> primary.rcYoru(player);
            case TENGENS_BLADE -> primary.rcTengensBlade(player);
            case EDGE_ASTRAL_PLANE -> primary.rcEdgeAstralPlane(player);
            case FALLEN_GODS_SPEAR -> primary.rcFallenGodsSpear(player);
            case NATURE_SWORD -> primary.rcNatureSword(player);
            case HEAVENLY_PARTISAN -> primary.rcHeavenlyPartisan(player);
            case SOUL_DEVOURER -> primary.rcSoulDevourer(player);
            case MJOLNIR -> primary.rcMjolnir(player);
            case THOUSAND_DEMON_DAGGERS -> primary.rcThousandDemonDaggers(player);
            case STAR_EDGE -> primary.rcStarEdge(player);
            case RIVERS_OF_BLOOD -> primary.rcRiversOfBlood(player);
            case DRAGON_SLAYING_BLADE -> primary.rcDragonSlayingBlade(player);
            case STOP_SIGN -> primary.rcStopSign(player);
            case CREATION_SPLITTER -> primary.rcCreationSplitter(player);
        }
    }

    private void handleAltAbility(Player player, LegendaryWeapon weapon) {
        String name = weapon.getAltAbilityName();
        int cd = weapon.getAltCooldown();
        if (isOnCooldown(player, name)) { sendCooldownMsg(player, name); return; }
        setCooldown(player, name, cd);
        switch (weapon) {
            case OCEANS_RAGE -> alt.holdOceansRage(player);
            case AQUATIC_SACRED_BLADE -> alt.holdAquaticSacredBlade(player);
            case TRUE_EXCALIBUR -> alt.holdTrueExcalibur(player);
            case REQUIEM_NINTH_ABYSS -> alt.holdRequiemNinthAbyss(player);
            case ROYAL_CHAKRAM -> alt.holdRoyalChakram(player);
            case BERSERKERS_GREATAXE -> alt.holdBerserkersGreataxe(player);
            case ACIDIC_CLEAVER -> alt.holdAcidicCleaver(player);
            case BLACK_IRON_GREATSWORD -> alt.holdBlackIronGreatsword(player);
            case MURAMASA -> alt.holdMuramasa(player);
            case PHOENIXS_GRACE -> alt.holdPhoenixsGrace(player);
            case SOUL_COLLECTOR -> alt.holdSoulCollector(player);
            case AMETHYST_SHURIKEN -> alt.holdAmethystShuriken(player);
            case VALHAKYRA -> alt.holdValhakyra(player);
            case WINDREAPER -> alt.holdWindreaper(player);
            case PHANTOMGUARD -> alt.holdPhantomguard(player);
            case MOONLIGHT -> alt.holdMoonlight(player);
            case ZENITH -> alt.holdZenith(player);
            case SOLSTICE -> alt.holdSolstice(player);
            case GRAND_CLAYMORE -> alt.holdGrandClaymore(player);
            case CALAMITY_BLADE -> alt.holdCalamityBlade(player);
            case DRAGON_SWORD -> alt.holdDragonSword(player);
            case TALONBRAND -> alt.holdTalonbrand(player);
            case EMERALD_GREATCLEAVER -> alt.holdEmeraldGreatcleaver(player);
            case DEMONS_BLOOD_BLADE -> alt.holdDemonsBloodBlade(player);
            case NOCTURNE -> alt.holdNocturne(player);
            case GRAVESCEPTER -> alt.holdGravescepter(player);
            case LYCANBANE -> alt.holdLycanbane(player);
            case GLOOMSTEEL_KATANA -> alt.holdGloomsteelKatana(player);
            case VIRIDIAN_CLEAVER -> alt.holdViridianCleaver(player);
            case CRESCENT_EDGE -> alt.holdCrescentEdge(player);
            case GRAVECLEAVER -> alt.holdGravecleaver(player);
            case AMETHYST_GREATBLADE -> alt.holdAmethystGreatblade(player);
            case FLAMBERGE -> alt.holdFlamberge(player);
            case CRYSTAL_FROSTBLADE -> alt.holdCrystalFrostblade(player);
            case DEMONSLAYER -> alt.holdDemonslayer(player);
            case VENGEANCE -> alt.holdVengeance(player);
            case OCULUS -> alt.holdOculus(player);
            case ANCIENT_GREATSLAB -> alt.holdAncientGreatslab(player);
            case NEPTUNES_FANG -> alt.holdNeptunesFang(player);
            case TIDECALLER -> alt.holdTidecaller(player);
            case STORMFORK -> alt.holdStormfork(player);
            case JADE_REAPER -> alt.holdJadeReaper(player);
            case VINDICATOR -> alt.holdVindicator(player);
            case SPIDER_FANG -> alt.holdSpiderFang(player);
            case DIVINE_AXE_RHITTA -> alt.altDivineAxeRhitta(player);
            case YORU -> alt.altYoru(player);
            case TENGENS_BLADE -> alt.altTengensBlade(player);
            case EDGE_ASTRAL_PLANE -> alt.altEdgeAstralPlane(player);
            case FALLEN_GODS_SPEAR -> alt.altFallenGodsSpear(player);
            case NATURE_SWORD -> alt.altNatureSword(player);
            case HEAVENLY_PARTISAN -> alt.altHeavenlyPartisan(player);
            case SOUL_DEVOURER -> alt.altSoulDevourer(player);
            case MJOLNIR -> alt.altMjolnir(player);
            case THOUSAND_DEMON_DAGGERS -> alt.altThousandDemonDaggers(player);
            case STAR_EDGE -> alt.altStarEdge(player);
            case RIVERS_OF_BLOOD -> alt.altRiversOfBlood(player);
            case DRAGON_SLAYING_BLADE -> alt.altDragonSlayingBlade(player);
            case STOP_SIGN -> alt.altStopSign(player);
            case CREATION_SPLITTER -> alt.altCreationSplitter(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker && event.getEntity() instanceof LivingEntity target) {
            UUID uid = attacker.getUniqueId();
            if (ctx.bloodlustStacks.containsKey(uid) && ctx.bloodlustExpiry.getOrDefault(uid, 0L) > System.currentTimeMillis()) {
                event.setDamage(event.getDamage() + (ctx.bloodlustStacks.get(uid) * 2.0));
            }
            if (ctx.grudgeTargets.containsKey(uid) && ctx.grudgeExpiry.getOrDefault(uid, 0L) > System.currentTimeMillis()) {
                if (target.getUniqueId().equals(ctx.grudgeTargets.get(uid))) {
                    event.setDamage(event.getDamage() * 1.2);
                }
            }
            if (ctx.predatorMarkTarget.containsKey(uid) && ctx.predatorMarkExpiry.getOrDefault(uid, 0L) > System.currentTimeMillis()) {
                if (target.getUniqueId().equals(ctx.predatorMarkTarget.get(uid))) {
                    event.setDamage(event.getDamage() * 1.3);
                }
            }
        }
        if (event.getEntity() instanceof Player victim) {
            UUID uid = victim.getUniqueId();
            if (ctx.shadowStanceActive.getOrDefault(uid, false) && ctx.shadowStanceExpiry.getOrDefault(uid, 0L) > System.currentTimeMillis()) {
                if (Math.random() < 0.25) {
                    event.setCancelled(true);
                    victim.sendActionBar(Component.text("Shadow Dodge!", NamedTextColor.DARK_GRAY).decorate(TextDecoration.BOLD));
                    victim.getWorld().spawnParticle(Particle.SMOKE, victim.getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0.05);
                    return;
                }
            }
            if (ctx.crescentParryActive.getOrDefault(uid, false) && event.getDamager() instanceof LivingEntity attacker) {
                ctx.crescentParryActive.put(uid, false);
                double reflected = event.getDamage();
                event.setCancelled(true);
                attacker.damage(reflected, victim);
                victim.sendActionBar(Component.text("PARRIED! ", NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
                        .append(Component.text(String.format("%.0f dmg reflected!", reflected), NamedTextColor.WHITE)));
                victim.getWorld().spawnParticle(Particle.CRIT, victim.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.2);
                victim.playSound(victim.getLocation(), Sound.ITEM_SHIELD_BLOCK, 2.0f, 0.8f);
                return;
            }
            if (ctx.emberShieldActive.getOrDefault(uid, false) && ctx.emberShieldExpiry.getOrDefault(uid, 0L) > System.currentTimeMillis() && event.getDamager() instanceof LivingEntity attacker) {
                attacker.setFireTicks(60);
                attacker.damage(4.0, victim);
                victim.getWorld().spawnParticle(Particle.FLAME, attacker.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.05);
            }
            if (ctx.thunderShieldActive.getOrDefault(uid, false) && ctx.thunderShieldExpiry.getOrDefault(uid, 0L) > System.currentTimeMillis() && event.getDamager() instanceof LivingEntity attacker) {
                victim.getWorld().strikeLightningEffect(attacker.getLocation());
                attacker.damage(4.0, victim);
            }
            if (ctx.gemBarrierCharges.getOrDefault(uid, 0) > 0 && ctx.gemBarrierExpiry.getOrDefault(uid, 0L) > System.currentTimeMillis()) {
                event.setCancelled(true);
                ctx.gemBarrierCharges.merge(uid, -1, Integer::sum);
                int remaining = ctx.gemBarrierCharges.get(uid);
                victim.sendActionBar(Component.text("Gem Barrier absorbed hit! ", NamedTextColor.GREEN)
                        .append(Component.text(remaining + " charges left", NamedTextColor.YELLOW)));
                victim.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, victim.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0);
                victim.playSound(victim.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 2.0f, 1.2f);
                if (remaining <= 0) ctx.gemBarrierCharges.remove(uid);
                return;
            }
            if (ctx.retributionExpiry.containsKey(uid) && ctx.retributionExpiry.get(uid) > System.currentTimeMillis()) {
                ctx.retributionDamageStored.merge(uid, event.getDamage(), Double::sum);
            }
            if (ctx.undyingRageActive.getOrDefault(uid, false) && ctx.undyingRageExpiry.getOrDefault(uid, 0L) > System.currentTimeMillis()) {
                if (victim.getHealth() - event.getFinalDamage() <= 0) {
                    event.setCancelled(true);
                    victim.setHealth(1.0);
                    ctx.undyingRageActive.put(uid, false);
                    victim.sendActionBar(Component.text("UNDYING RAGE! ", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
                    victim.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, victim.getLocation(), 50, 0.5, 1, 0.5, 0.3);
                    victim.playSound(victim.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
                }
            }
        }
    }
}