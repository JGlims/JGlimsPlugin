package com.jglims.plugin.weapons;

import com.jglims.plugin.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class BattleShovelManager implements Listener {

    private final JavaPlugin plugin;
    private final ConfigManager config;

    // PDC keys
    private final NamespacedKey battleShovelKey;

    // Materials that shovels can path-make on (we block this for battle shovels)
    private static final Set<Material> PATH_BLOCKS = EnumSet.of(
            Material.DIRT, Material.GRASS_BLOCK, Material.DIRT_PATH,
            Material.COARSE_DIRT, Material.ROOTED_DIRT, Material.PODZOL,
            Material.MYCELIUM, Material.FARMLAND, Material.MUD,
            Material.MUDDY_MANGROVE_ROOTS
    );

    // Shovel tier mappings
    public enum ShovelTier {
        WOODEN(Material.WOODEN_SHOVEL, Material.OAK_PLANKS, 2.5, -3.0),
        STONE(Material.STONE_SHOVEL, Material.COBBLESTONE, 3.5, -3.0),
        COPPER(Material.COPPER_SHOVEL, Material.COPPER_INGOT, 3.5, -3.0),
        GOLDEN(Material.GOLDEN_SHOVEL, Material.GOLD_INGOT, 2.5, -3.0),
        IRON(Material.IRON_SHOVEL, Material.IRON_INGOT, 4.5, -3.0),
        DIAMOND(Material.DIAMOND_SHOVEL, Material.DIAMOND, 5.5, -3.0),
        NETHERITE(Material.NETHERITE_SHOVEL, Material.NETHERITE_INGOT, 6.5, -3.0);

        private final Material shovelMaterial;
        private final Material ingredient;
        private final double baseDamage;
        private final double baseSpeedModifier;

        ShovelTier(Material shovelMaterial, Material ingredient, double baseDamage, double baseSpeedModifier) {
            this.shovelMaterial = shovelMaterial;
            this.ingredient = ingredient;
            this.baseDamage = baseDamage;
            this.baseSpeedModifier = baseSpeedModifier;
        }

        public Material getShovelMaterial() { return shovelMaterial; }
        public Material getIngredient() { return ingredient; }
        public double getBaseDamage() { return baseDamage; }
        public double getBaseSpeedModifier() { return baseSpeedModifier; }

        public static ShovelTier fromMaterial(Material mat) {
            for (ShovelTier tier : values()) {
                if (tier.shovelMaterial == mat) return tier;
            }
            return null;
        }
    }

    // Battle shovel damage: shovel base + 1.5
    private static final Map<ShovelTier, Double> BATTLE_DAMAGE = new EnumMap<>(ShovelTier.class);
    static {
        BATTLE_DAMAGE.put(ShovelTier.WOODEN, 4.0);
        BATTLE_DAMAGE.put(ShovelTier.STONE, 5.0);
        BATTLE_DAMAGE.put(ShovelTier.COPPER, 5.0);
        BATTLE_DAMAGE.put(ShovelTier.GOLDEN, 4.0);
        BATTLE_DAMAGE.put(ShovelTier.IRON, 6.0);
        BATTLE_DAMAGE.put(ShovelTier.DIAMOND, 7.0);
        BATTLE_DAMAGE.put(ShovelTier.NETHERITE, 8.0);
    }

    // Battle shovel attack speed: slightly faster than normal shovel
    private static final double BATTLE_SPEED_MODIFIER = -2.8; // = 1.2 attack speed

    public BattleShovelManager(JavaPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
        this.battleShovelKey = new NamespacedKey(plugin, "battle_shovel");
    }

    /**
     * Creates a Battle Shovel from a base shovel material.
     */
    public ItemStack createBattleShovel(ShovelTier tier) {
        ItemStack item = new ItemStack(tier.getShovelMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        // Set PDC marker
        meta.getPersistentDataContainer().set(battleShovelKey, PersistentDataType.BOOLEAN, true);

        // Custom name
        String tierName = tier.name().charAt(0) + tier.name().substring(1).toLowerCase();
        meta.setDisplayName("§6⚔ " + tierName + " Battle Shovel");

        // Lore
        double damage = BATTLE_DAMAGE.getOrDefault(tier, 5.0);
        List<String> lore = new ArrayList<>();
        lore.add("§7Battle Weapon");
        lore.add("§f⚔ Attack Damage: §a" + damage);
        lore.add("§f⚡ Attack Speed: §b1.2");
        lore.add("§c✘ Path-making disabled");
        lore.add("");
        lore.add("§7A shovel reforged for combat.");
        lore.add("§7Loses its ability to create paths,");
        lore.add("§7but strikes with devastating force.");
        meta.setLore(lore);

        // Attribute modifiers - clear defaults and set custom
        meta.removeAttributeModifier(Attribute.ATTACK_DAMAGE);
        meta.removeAttributeModifier(Attribute.ATTACK_SPEED);

        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE,
                new AttributeModifier(
                        new NamespacedKey(plugin, "battle_shovel_damage"),
                        damage - 1.0, // minus 1 because base player damage is 1
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.MAINHAND
                ));
        meta.addAttributeModifier(Attribute.ATTACK_SPEED,
                new AttributeModifier(
                        new NamespacedKey(plugin, "battle_shovel_speed"),
                        BATTLE_SPEED_MODIFIER,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.MAINHAND
                ));

        // Enchantment glint
        meta.setEnchantmentGlintOverride(true);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Checks if an item is a battle shovel.
     */
    public boolean isBattleShovel(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer()
                .getOrDefault(battleShovelKey, PersistentDataType.BOOLEAN, false);
    }

    /**
     * Gets the tier of a shovel by its material.
     */
    public ShovelTier getTier(ItemStack item) {
        if (item == null) return null;
        return ShovelTier.fromMaterial(item.getType());
    }

    /**
     * Checks if a material is a shovel.
     */
    public static boolean isShovelMaterial(Material mat) {
        return ShovelTier.fromMaterial(mat) != null;
    }

    /**
     * Returns the ingredient material for a given shovel tier.
     */
    public Material getIngredient(ShovelTier tier) {
        return tier.getIngredient();
    }

    public NamespacedKey getBattleShovelKey() {
        return battleShovelKey;
    }

    // ==========================================
    // EVENT: Block right-click path creation
    // ==========================================

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack hand = event.getItem();
        if (hand == null) return;
        if (!isBattleShovel(hand)) return;

        // Block path creation on dirt-type blocks
        if (event.getClickedBlock() != null && PATH_BLOCKS.contains(event.getClickedBlock().getType())) {
            event.setCancelled(true);
        }
    }
}
