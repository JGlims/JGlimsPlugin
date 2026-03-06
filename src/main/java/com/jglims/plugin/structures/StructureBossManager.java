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
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Evoker;
import org.bukkit.entity.Hoglin;
import org.bukkit.entity.Husk;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.PiglinBrute;
import org.bukkit.entity.Pillager;
import org.bukkit.entity.Player;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Vindicator;
import org.bukkit.entity.Witch;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Random;

/**
 * Manages spawning and death handling of structure mini-bosses.
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
            case CRIMSON_CITADEL -> spawnBoss(location, Hoglin.class, type);
            case SOUL_SANCTUM -> spawnBoss(location, WitherSkeleton.class, type);
            case BASALT_SPIRE -> spawnBoss(location, IronGolem.class, type);
            case NETHER_DUNGEON -> spawnBoss(location, Blaze.class, type);
            case PIGLIN_PALACE -> spawnBoss(location, PiglinBrute.class, type);
            case VOID_SHRINE -> spawnBoss(location, Enderman.class, type);
            case ENDER_MONASTERY -> spawnBoss(location, Enderman.class, type);
            default -> null;
        };

        if (boss != null) {
            plugin.getLogger().info("Spawned " + type.getBossName() + " at " +
                location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
        }
    }

    private <T extends LivingEntity> LivingEntity spawnBoss(Location loc, Class<T> entityClass, StructureType type) {
        T entity = loc.getWorld().spawn(loc, entityClass);

        // Set custom name
        TextColor tierColor = type.getLootTier().getColor();
        entity.customName(Component.text(type.getBossName(), tierColor).decorate(TextDecoration.BOLD));
        entity.setCustomNameVisible(true);

        // Set health
        if (entity.getAttribute(Attribute.MAX_HEALTH) != null) {
            entity.getAttribute(Attribute.MAX_HEALTH).setBaseValue(type.getBossBaseHP());
            entity.setHealth(type.getBossBaseHP());
        }

        // Prevent despawn
        entity.setPersistent(true);
        entity.setRemoveWhenFarAway(false);

        // Tag as structure boss via PDC
        entity.getPersistentDataContainer().set(KEY_STRUCTURE_BOSS, PersistentDataType.INTEGER, 1);
        entity.getPersistentDataContainer().set(KEY_BOSS_STRUCTURE_TYPE, PersistentDataType.STRING, type.name());

        return entity;
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getPersistentDataContainer().getOrDefault(KEY_STRUCTURE_BOSS, PersistentDataType.INTEGER, 0) != 1) return;

        String typeName = entity.getPersistentDataContainer().get(KEY_BOSS_STRUCTURE_TYPE, PersistentDataType.STRING);
        if (typeName == null) return;

        StructureType type;
        try { type = StructureType.valueOf(typeName); } catch (Exception e) { return; }

        // Drop power-ups
        PowerUpManager pum = plugin.getPowerUpManager();
        Location loc = entity.getLocation();

        // Heart Crystal: 10% from any mini-boss
        if (random.nextDouble() < 0.10) {
            loc.getWorld().dropItemNaturally(loc, pum.createHeartCrystal());
        }

        // Soul Fragments: 15% chance, 1-3 fragments
        if (random.nextDouble() < 0.15) {
            int count = 1 + random.nextInt(3);
            for (int i = 0; i < count; i++) {
                loc.getWorld().dropItemNaturally(loc, pum.createSoulFragment());
            }
        }

        // Legendary weapon from tier
        LegendaryWeaponManager wm = plugin.getLegendaryWeaponManager();
        LegendaryWeapon[] pool = LegendaryWeapon.byTier(type.getLootTier());
        if (pool.length > 0 && random.nextDouble() < 0.30) {
            LegendaryWeapon weapon = pool[random.nextInt(pool.length)];
            ItemStack weaponItem = wm.createWeapon(weapon);
            if (weaponItem != null) {
                loc.getWorld().dropItemNaturally(loc, weaponItem);

                // Announce to nearby players
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

        // Diamonds
        int diamonds = 3 + random.nextInt(5);
        event.getDrops().add(new ItemStack(Material.DIAMOND, diamonds));

        plugin.getLogger().info(type.getBossName() + " was defeated. Dropped " + diamonds + " diamonds.");
    }

    public NamespacedKey getKeyStructureBoss() { return KEY_STRUCTURE_BOSS; }
}