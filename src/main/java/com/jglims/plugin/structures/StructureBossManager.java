package com.jglims.plugin.structures;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.legendary.LegendaryWeapon;
import com.jglims.plugin.legendary.LegendaryWeaponManager;
import com.jglims.plugin.powerups.PowerUpManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

/**
 * Manages spawning and death handling of structure mini-bosses.
 * Plus Part 2: Added 10 new structure boss types.
 */
public class StructureBossManager implements Listener {

    private final JGlimsPlugin plugin;
    private final NamespacedKey KEY_STRUCTURE_BOSS;
    private final NamespacedKey KEY_BOSS_STRUCTURE_TYPE;
    private final Random random = new Random();

    public StructureBossManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        this.KEY_STRUCTURE_BOSS = new NamespacedKey(plugin, "structure_boss");
        this.KEY_BOSS_STRUCTURE_TYPE = new NamespacedKey(plugin, "boss_structure_type");
    }

    /**
     * Spawn a mini-boss for the given structure type at the specified location.
     */
    public void spawnStructureBoss(StructureType type, Location location) {
        LivingEntity boss = switch (type) {
            // ── Original Overworld ──
            case RUINED_COLOSSEUM -> spawnBoss(location, IronGolem.class, type);
            case DRUIDS_GROVE -> spawnBoss(location, Zombie.class, type);
            case SHREK_HOUSE -> spawnBoss(location, IronGolem.class, type);
            case MAGE_TOWER -> spawnBoss(location, Evoker.class, type);
            case GIGANTIC_CASTLE -> spawnBoss(location, Vindicator.class, type);
            case FORTRESS -> spawnBoss(location, PiglinBrute.class, type);
            case CAMPING_LARGE -> spawnBoss(location, Pillager.class, type);
            case WITCH_HOUSE_SWAMP -> spawnBoss(location, Witch.class, type);
            case WITCH_HOUSE_FOREST -> spawnBoss(location, Witch.class, type);
            case ABANDONED_HOUSE -> spawnBoss(location, Phantom.class, type);
            case VOLCANO -> spawnBoss(location, MagmaCube.class, type);
            case ANCIENT_TEMPLE -> spawnBoss(location, Husk.class, type);
            case DUNGEON_DEEP -> spawnBoss(location, Zombie.class, type);
            case THANOS_TEMPLE -> spawnBoss(location, IronGolem.class, type);
            case PILLAGER_FORTRESS -> spawnBoss(location, Vindicator.class, type);
            case PILLAGER_AIRSHIP -> spawnBoss(location, Pillager.class, type);
            // ── NEW Overworld (Plus Part 2) ──
            case FROST_DUNGEON -> spawnFrostWarden(location, type);
            case BANDIT_HIDEOUT -> spawnBoss(location, Pillager.class, type);
            case SUNKEN_RUINS -> spawnDrownedWarlord(location, type);
            case CURSED_GRAVEYARD -> spawnGraveRevenant(location, type);
            case SKY_ALTAR -> spawnBoss(location, Evoker.class, type);
            // ── Original Nether ──
            case CRIMSON_CITADEL -> spawnBoss(location, Hoglin.class, type);
            case SOUL_SANCTUM -> spawnBoss(location, WitherSkeleton.class, type);
            case BASALT_SPIRE -> spawnBoss(location, IronGolem.class, type);
            case NETHER_DUNGEON -> spawnBoss(location, Blaze.class, type);
            case PIGLIN_PALACE -> spawnBoss(location, PiglinBrute.class, type);
            // ── NEW Nether (Plus Part 2) ──
            case WITHER_SANCTUM -> spawnWitherPriest(location, type);
            case BLAZE_COLOSSEUM -> spawnInfernalChampion(location, type);
            // ── Original End ──
            case VOID_SHRINE -> spawnBoss(location, Enderman.class, type);
            case ENDER_MONASTERY -> spawnBoss(location, Enderman.class, type);
            // ── NEW Abyss (Plus Part 2) ──
            case ABYSSAL_CASTLE -> spawnAbyssalOverlord(location, type);
            case VOID_NEXUS -> spawnVoidArbiter(location, type);
            case SHATTERED_CATHEDRAL -> spawnFallenArchbishop(location, type);
            default -> null;
        };

        if (boss != null) {
            plugin.getLogger().info("Spawned " + type.getBossName() + " at " +
                location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
        }
    }

    // ── Generic boss spawner ──

    private <T extends LivingEntity> LivingEntity spawnBoss(Location loc, Class<T> entityClass, StructureType type) {
        T entity = loc.getWorld().spawn(loc, entityClass);
        configureBoss(entity, type);
        return entity;
    }

    private void configureBoss(LivingEntity entity, StructureType type) {
        TextColor tierColor = type.getLootTier().getColor();
        entity.customName(Component.text(type.getBossName(), tierColor).decorate(TextDecoration.BOLD));
        entity.setCustomNameVisible(true);

        if (entity.getAttribute(Attribute.MAX_HEALTH) != null) {
            entity.getAttribute(Attribute.MAX_HEALTH).setBaseValue(type.getBossBaseHP());
            entity.setHealth(type.getBossBaseHP());
        }

        entity.setPersistent(true);
        entity.setRemoveWhenFarAway(false);

        entity.getPersistentDataContainer().set(KEY_STRUCTURE_BOSS, PersistentDataType.INTEGER, 1);
        entity.getPersistentDataContainer().set(KEY_BOSS_STRUCTURE_TYPE, PersistentDataType.STRING, type.name());
    }

    // ── Specialized boss spawners (Plus Part 2) ──

    private LivingEntity spawnFrostWarden(Location loc, StructureType type) {
        Stray entity = loc.getWorld().spawn(loc, Stray.class);
        configureBoss(entity, type);
        // Frost aura: permanent Slowness I to self (visual cue), Resistance I
        entity.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 1, false, true));
        entity.setGlowing(true);
        return entity;
    }

    private LivingEntity spawnDrownedWarlord(Location loc, StructureType type) {
        Drowned entity = loc.getWorld().spawn(loc, Drowned.class);
        configureBoss(entity, type);
        // Give trident
        entity.getEquipment().setItemInMainHand(new ItemStack(Material.TRIDENT));
        entity.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
        entity.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 0, false, false));
        entity.setGlowing(true);
        return entity;
    }

    private LivingEntity spawnGraveRevenant(Location loc, StructureType type) {
        Zombie entity = loc.getWorld().spawn(loc, Zombie.class);
        configureBoss(entity, type);
        entity.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
        entity.getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
        entity.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1, false, true));
        entity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
        entity.setGlowing(true);
        return entity;
    }

    private LivingEntity spawnWitherPriest(Location loc, StructureType type) {
        WitherSkeleton entity = loc.getWorld().spawn(loc, WitherSkeleton.class);
        configureBoss(entity, type);
        entity.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
        entity.getEquipment().setHelmet(new ItemStack(Material.NETHERITE_HELMET));
        entity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
        entity.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1, false, true));
        entity.setGlowing(true);
        return entity;
    }

    private LivingEntity spawnInfernalChampion(Location loc, StructureType type) {
        Blaze entity = loc.getWorld().spawn(loc, Blaze.class);
        configureBoss(entity, type);
        entity.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 2, false, true));
        entity.setGlowing(true);
        return entity;
    }

    private LivingEntity spawnAbyssalOverlord(Location loc, StructureType type) {
        // Wither Skeleton with extreme stats for Abyss
        WitherSkeleton entity = loc.getWorld().spawn(loc, WitherSkeleton.class);
        configureBoss(entity, type);
        entity.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
        entity.getEquipment().setHelmet(new ItemStack(Material.NETHERITE_HELMET));
        entity.getEquipment().setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
        entity.getEquipment().setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
        entity.getEquipment().setBoots(new ItemStack(Material.NETHERITE_BOOTS));
        entity.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 3, false, true));
        entity.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 2, false, true));
        entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, true));
        entity.setGlowing(true);
        return entity;
    }

    private LivingEntity spawnVoidArbiter(Location loc, StructureType type) {
        Enderman entity = loc.getWorld().spawn(loc, Enderman.class);
        configureBoss(entity, type);
        entity.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 2, false, true));
        entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2, false, true));
        entity.setGlowing(true);
        return entity;
    }

    private LivingEntity spawnFallenArchbishop(Location loc, StructureType type) {
        Evoker entity = loc.getWorld().spawn(loc, Evoker.class);
        configureBoss(entity, type);
        entity.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 2, false, true));
        entity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 1, false, true));
        entity.setGlowing(true);
        return entity;
    }

    // ── Death handling ──

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getPersistentDataContainer().getOrDefault(KEY_STRUCTURE_BOSS, PersistentDataType.INTEGER, 0) != 1) return;

        String typeName = entity.getPersistentDataContainer().get(KEY_BOSS_STRUCTURE_TYPE, PersistentDataType.STRING);
        if (typeName == null) return;

        StructureType type;
        try { type = StructureType.valueOf(typeName); } catch (Exception e) { return; }

        PowerUpManager pum = plugin.getPowerUpManager();
        Location loc = entity.getLocation();

        // Heart Crystal: 10% from any mini-boss, 25% from ABYSSAL
        double heartChance = type.getLootTier() == com.jglims.plugin.legendary.LegendaryTier.ABYSSAL ? 0.25 : 0.10;
        if (random.nextDouble() < heartChance) {
            loc.getWorld().dropItemNaturally(loc, pum.createHeartCrystal());
        }

        // Soul Fragments: 15% chance (1-3), 30% for ABYSSAL (2-5)
        double soulChance = type.getLootTier() == com.jglims.plugin.legendary.LegendaryTier.ABYSSAL ? 0.30 : 0.15;
        if (random.nextDouble() < soulChance) {
            int count = type.getLootTier() == com.jglims.plugin.legendary.LegendaryTier.ABYSSAL
                ? 2 + random.nextInt(4) : 1 + random.nextInt(3);
            for (int i = 0; i < count; i++) {
                loc.getWorld().dropItemNaturally(loc, pum.createSoulFragment());
            }
        }

        // Legendary weapon from tier: 30%, 50% for ABYSSAL
        LegendaryWeaponManager wm = plugin.getLegendaryWeaponManager();
        LegendaryWeapon[] pool = LegendaryWeapon.byTier(type.getLootTier());
        double weaponChance = type.getLootTier() == com.jglims.plugin.legendary.LegendaryTier.ABYSSAL ? 0.50 : 0.30;
        if (pool.length > 0 && random.nextDouble() < weaponChance) {
            LegendaryWeapon weapon = pool[random.nextInt(pool.length)];
            ItemStack weaponItem = wm.createWeapon(weapon);
            if (weaponItem != null) {
                loc.getWorld().dropItemNaturally(loc, weaponItem);

                Component msg = Component.text(type.getBossName(), type.getLootTier().getColor()).decorate(TextDecoration.BOLD)
                    .append(Component.text(" dropped ", NamedTextColor.GRAY))
                    .append(Component.text(weapon.getDisplayName(), type.getLootTier().getColor()).decorate(TextDecoration.BOLD))
                    .append(Component.text("!", NamedTextColor.GRAY));
                for (Player p : loc.getNearbyPlayers(50)) {
                    p.sendMessage(msg);
                    p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.2f);
                }
            }
        }

        // Effects
        loc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc.add(0, 1, 0), 40, 1, 1, 1);
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.6f, 1.5f);

        // Diamonds: scaled by tier
        int baseDiamonds = switch (type.getLootTier()) {
            case COMMON -> 2;
            case RARE -> 3;
            case EPIC -> 5;
            case MYTHIC -> 8;
            case ABYSSAL -> 12;
        };
        int diamonds = baseDiamonds + random.nextInt(5);
        event.getDrops().add(new ItemStack(Material.DIAMOND, diamonds));

        // ABYSSAL bosses also drop Netherite
        if (type.getLootTier() == com.jglims.plugin.legendary.LegendaryTier.ABYSSAL) {
            event.getDrops().add(new ItemStack(Material.NETHERITE_INGOT, 1 + random.nextInt(3)));
        }

        plugin.getLogger().info(type.getBossName() + " was defeated. Dropped " + diamonds + " diamonds.");
    }

    public NamespacedKey getKeyStructureBoss() { return KEY_STRUCTURE_BOSS; }
}