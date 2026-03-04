package com.jglims.plugin.weapons;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import com.jglims.plugin.config.ConfigManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class BattleShovelManager implements Listener {

    private final JavaPlugin plugin;
    private final ConfigManager config;
    private final NamespacedKey battleShovelKey;

    private static final Set<Material> PATH_BLOCKS = EnumSet.of(
            Material.DIRT, Material.GRASS_BLOCK, Material.DIRT_PATH,
            Material.COARSE_DIRT, Material.ROOTED_DIRT, Material.PODZOL,
            Material.MYCELIUM, Material.FARMLAND, Material.MUD,
            Material.MUDDY_MANGROVE_ROOTS
    );

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

    private static final double BATTLE_SPEED_MODIFIER = -2.8; // 1.2 attack speed

    public BattleShovelManager(JavaPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
        this.battleShovelKey = new NamespacedKey(plugin, "battle_shovel");
    }

    public ItemStack createBattleShovel(ShovelTier tier) {
        ItemStack item = new ItemStack(tier.getShovelMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.getPersistentDataContainer().set(battleShovelKey, PersistentDataType.BOOLEAN, true);

        String tierName = tier.name().charAt(0) + tier.name().substring(1).toLowerCase();
        NamedTextColor nameColor = tier == ShovelTier.NETHERITE ? NamedTextColor.DARK_RED : NamedTextColor.WHITE;
        meta.displayName(Component.text(tierName + " Battle Shovel", nameColor)
                .decoration(TextDecoration.ITALIC, false));

        double vanillaDamage = tier.getBaseDamage();
        double battleDamage = BATTLE_DAMAGE.getOrDefault(tier, 5.0);
        double battleBonus = battleDamage - vanillaDamage;

        // Set lore — uniform pattern
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Custom Weapon", NamedTextColor.DARK_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Attack Damage: ", NamedTextColor.GRAY)
                .append(Component.text(String.format("%.1f", vanillaDamage), NamedTextColor.GREEN))
                .append(Component.text(" +", NamedTextColor.GRAY))
                .append(Component.text(String.format("%.1f", battleBonus), NamedTextColor.YELLOW))
                .append(Component.text(" = ", NamedTextColor.GRAY))
                .append(Component.text(String.format("%.0f", battleDamage), NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Attack Speed: ", NamedTextColor.GRAY)
                .append(Component.text("1.2", NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Path-making disabled", NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("\u25C6 Diamond: Earthen Wall", NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("\u25C6 Netherite: Tectonic Upheaval", NamedTextColor.DARK_RED)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Can be upgraded to Super", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        meta.removeAttributeModifier(Attribute.ATTACK_DAMAGE);
        meta.removeAttributeModifier(Attribute.ATTACK_SPEED);

        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE,
                new AttributeModifier(
                        new NamespacedKey(plugin, "battle_shovel_damage"),
                        battleDamage - 1.0,
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

        meta.setEnchantmentGlintOverride(true);

        item.setItemMeta(meta);
        return item;
    }

    public boolean isBattleShovel(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer()
                .getOrDefault(battleShovelKey, PersistentDataType.BOOLEAN, false);
    }

    public ShovelTier getTier(ItemStack item) {
        if (item == null) return null;
        return ShovelTier.fromMaterial(item.getType());
    }

    public static boolean isShovelMaterial(Material mat) {
        return ShovelTier.fromMaterial(mat) != null;
    }

    public Material getIngredient(ShovelTier tier) {
        return tier.getIngredient();
    }

    public NamespacedKey getBattleShovelKey() {
        return battleShovelKey;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack hand = event.getItem();
        if (hand == null) return;
        if (!isBattleShovel(hand)) return;
        if (event.getClickedBlock() != null && PATH_BLOCKS.contains(event.getClickedBlock().getType())) {
            event.setCancelled(true);
        }
    }
}
