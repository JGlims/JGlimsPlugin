package com.jglims.plugin.crafting;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.DropItemManager;
import com.jglims.plugin.enchantments.CustomEnchantManager;
import com.jglims.plugin.enchantments.EnchantmentType;
import com.jglims.plugin.weapons.SickleManager;
import com.jglims.plugin.weapons.SpearManager;
import com.jglims.plugin.legendary.InfinityStoneManager;
import com.jglims.plugin.legendary.LegendaryArmorManager;
import com.jglims.plugin.legendary.LegendaryArmorSet;
import com.jglims.plugin.legendary.InfinityGauntletManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class RecipeManager implements Listener {

    private final JGlimsPlugin plugin;
    private final SickleManager sickleManager;
    private final SpearManager spearManager;
    private final InfinityStoneManager infinityStoneManager;
    private final InfinityGauntletManager infinityGauntletManager;
    private final LegendaryArmorManager legendaryArmorManager;
    private CraftedItemManager craftedItemManager;
    private DropItemManager dropItemManager;

    public void setCraftedItemManager(CraftedItemManager m) { this.craftedItemManager = m; }
    public void setDropItemManager(DropItemManager m) { this.dropItemManager = m; }

    public RecipeManager(JGlimsPlugin plugin, SickleManager sickleManager,
                         SpearManager spearManager,
                         InfinityStoneManager infinityStoneManager, InfinityGauntletManager infinityGauntletManager,
                         LegendaryArmorManager legendaryArmorManager) {
        this.plugin = plugin;
        this.sickleManager = sickleManager;
        this.spearManager = spearManager;
        this.infinityStoneManager = infinityStoneManager;
        this.infinityGauntletManager = infinityGauntletManager;
        this.legendaryArmorManager = legendaryArmorManager;
    }

    public void registerAllRecipes() {
        registerEyeOfEnder();
        registerTotemOfUndying();
        registerEnchantedGoldenApple();
        registerBlessings();
        registerBestBuddies();

        registerSickles();

        registerInfinityGauntlet();
        registerCraftableArmor();
        registerCraftedItems();

        plugin.getLogger().info("All custom crafting recipes registered (v1.5.0 — added 12 crafted item recipes).");
    }

    /**
     * Registers the 12 new crafted items from CraftedItemManager.
     * Uses DropItemManager outputs as ingredients where possible, so
     * players get a reason to hunt the custom mobs.
     */
    private void registerCraftedItems() {
        if (craftedItemManager == null) {
            plugin.getLogger().warning("CraftedItemManager not set — skipping crafted item recipes");
            return;
        }
        // 1. Blood Chalice: bowl + 5 vampire blood (shaped)
        ShapedRecipe chalice = new ShapedRecipe(new NamespacedKey(plugin, "blood_chalice"),
                craftedItemManager.createBloodChalice());
        chalice.shape("RBR", "RCR", "RRR");
        chalice.setIngredient('R', Material.REDSTONE);
        chalice.setIngredient('C', Material.BOWL);
        chalice.setIngredient('B', Material.NETHER_STAR);
        plugin.getServer().addRecipe(chalice);

        // 2. Fang Dagger: basilisk fang (bone placeholder) + iron
        ShapedRecipe dagger = new ShapedRecipe(new NamespacedKey(plugin, "fang_dagger"),
                craftedItemManager.createFangDagger());
        dagger.shape(" F ", " I ", " B ");
        dagger.setIngredient('F', Material.BONE);  // any bone (basilisk fangs are bones too)
        dagger.setIngredient('I', Material.IRON_INGOT);
        dagger.setIngredient('B', Material.STICK);
        plugin.getServer().addRecipe(dagger);

        // 3-6. Troll Leather armor set (leather + troll hide)
        ShapedRecipe tHelmet = new ShapedRecipe(new NamespacedKey(plugin, "troll_helmet"),
                craftedItemManager.createTrollHelmet());
        tHelmet.shape("LLL", "L L", "   ");
        tHelmet.setIngredient('L', Material.LEATHER);
        plugin.getServer().addRecipe(tHelmet);

        ShapedRecipe tChest = new ShapedRecipe(new NamespacedKey(plugin, "troll_chestplate"),
                craftedItemManager.createTrollChestplate());
        tChest.shape("L L", "LLL", "LLL");
        tChest.setIngredient('L', Material.LEATHER);
        plugin.getServer().addRecipe(tChest);

        ShapedRecipe tLegs = new ShapedRecipe(new NamespacedKey(plugin, "troll_leggings"),
                craftedItemManager.createTrollLeggings());
        tLegs.shape("LLL", "L L", "L L");
        tLegs.setIngredient('L', Material.LEATHER);
        plugin.getServer().addRecipe(tLegs);

        ShapedRecipe tBoots = new ShapedRecipe(new NamespacedKey(plugin, "troll_boots"),
                craftedItemManager.createTrollBoots());
        tBoots.shape("   ", "L L", "L L");
        tBoots.setIngredient('L', Material.LEATHER);
        plugin.getServer().addRecipe(tBoots);

        // 7. Lunar Blade: iron nugget (lunar fragment) + diamond sword
        ShapedRecipe lunar = new ShapedRecipe(new NamespacedKey(plugin, "lunar_blade"),
                craftedItemManager.createLunarBlade());
        lunar.shape(" F ", " F ", " S ");
        lunar.setIngredient('F', Material.IRON_NUGGET);  // lunar fragment base
        lunar.setIngredient('S', Material.STICK);
        plugin.getServer().addRecipe(lunar);

        // 8. Raptor Gauntlet: flint (raptor claw) + iron sword
        ShapedRecipe gauntlet = new ShapedRecipe(new NamespacedKey(plugin, "raptor_gauntlet"),
                craftedItemManager.createRaptorGauntlet());
        gauntlet.shape("FIF", " I ", " S ");
        gauntlet.setIngredient('F', Material.FLINT);
        gauntlet.setIngredient('I', Material.IRON_INGOT);
        gauntlet.setIngredient('S', Material.STICK);
        plugin.getServer().addRecipe(gauntlet);

        // 9. Shark Tooth Necklace: flint (shark tooth) + gold
        ShapedRecipe necklace = new ShapedRecipe(new NamespacedKey(plugin, "shark_tooth_necklace"),
                craftedItemManager.createSharkToothNecklace());
        necklace.shape("GGG", "G G", " F ");
        necklace.setIngredient('G', Material.GOLD_NUGGET);
        necklace.setIngredient('F', Material.FLINT);
        plugin.getServer().addRecipe(necklace);

        // 10. Tremor Shield: iron nuggets (tremor scale) + shield
        ShapedRecipe tShield = new ShapedRecipe(new NamespacedKey(plugin, "tremor_shield"),
                craftedItemManager.createTremorShield());
        tShield.shape("NNN", "NSN", " N ");
        tShield.setIngredient('N', Material.IRON_NUGGET);
        tShield.setIngredient('S', Material.SHIELD);
        plugin.getServer().addRecipe(tShield);

        // 11. Void Scepter: ender pearl (void essence) + blaze rod
        ShapedRecipe scepter = new ShapedRecipe(new NamespacedKey(plugin, "void_scepter"),
                craftedItemManager.createVoidScepter());
        scepter.shape(" E ", " R ", " E ");
        scepter.setIngredient('E', Material.ENDER_PEARL);
        scepter.setIngredient('R', Material.BLAZE_ROD);
        plugin.getServer().addRecipe(scepter);

        // 12. Soul Lantern item: lapis (soul fragment) + lantern
        ShapedRecipe lantern = new ShapedRecipe(new NamespacedKey(plugin, "soul_lantern_item"),
                craftedItemManager.createSoulLanternItem());
        lantern.shape(" L ", "LSL", " L ");
        lantern.setIngredient('L', Material.LAPIS_LAZULI);
        lantern.setIngredient('S', Material.LANTERN);
        plugin.getServer().addRecipe(lantern);

        // 13. Dinosaur Bone Bow: bone (horn) + bow
        ShapedRecipe dBow = new ShapedRecipe(new NamespacedKey(plugin, "dinosaur_bone_bow"),
                craftedItemManager.createDinosaurBoneBow());
        dBow.shape(" BS", "B S", " BS");
        dBow.setIngredient('B', Material.BONE);
        dBow.setIngredient('S', Material.STRING);
        plugin.getServer().addRecipe(dBow);

        // 14. Crimson Elixir: redstone (vampire blood) + potion
        ShapedRecipe elixir = new ShapedRecipe(new NamespacedKey(plugin, "crimson_elixir"),
                craftedItemManager.createCrimsonElixir());
        elixir.shape(" R ", "RPR", " R ");
        elixir.setIngredient('R', Material.REDSTONE);
        elixir.setIngredient('P', Material.GLASS_BOTTLE);
        plugin.getServer().addRecipe(elixir);

        // 15. Nightwalker Cloak: leather (spinosaurus sail) + leather chestplate
        ShapedRecipe cloak = new ShapedRecipe(new NamespacedKey(plugin, "nightwalker_cloak"),
                craftedItemManager.createNightwalkerCloak());
        cloak.shape("LLL", "LCL", "LLL");
        cloak.setIngredient('L', Material.LEATHER);
        cloak.setIngredient('C', Material.LEATHER_CHESTPLATE);
        plugin.getServer().addRecipe(cloak);
    }

    private void registerEyeOfEnder() {
        NamespacedKey key = new NamespacedKey(plugin, "custom_ender_eye");
        ShapedRecipe recipe = new ShapedRecipe(key, new ItemStack(Material.ENDER_EYE, 1));
        recipe.shape("GPG", "EYB", "GWG");
        recipe.setIngredient('G', Material.GHAST_TEAR);
        recipe.setIngredient('P', Material.PRISMARINE_SHARD);
        recipe.setIngredient('E', Material.ECHO_SHARD);
        recipe.setIngredient('Y', Material.ENDER_PEARL);
        recipe.setIngredient('B', Material.BLAZE_POWDER);
        recipe.setIngredient('W', Material.WIND_CHARGE);
        plugin.getServer().addRecipe(recipe);
    }

    private void registerTotemOfUndying() {
        NamespacedKey key = new NamespacedKey(plugin, "custom_totem");
        ShapedRecipe recipe = new ShapedRecipe(key, new ItemStack(Material.TOTEM_OF_UNDYING, 1));
        recipe.shape("NNN", "EGE", "NNN");
        recipe.setIngredient('N', Material.GOLD_NUGGET);
        recipe.setIngredient('E', Material.EMERALD);
        recipe.setIngredient('G', Material.GOLD_INGOT);
        plugin.getServer().addRecipe(recipe);
    }

    private void registerEnchantedGoldenApple() {
        NamespacedKey key = new NamespacedKey(plugin, "custom_enchanted_golden_apple");
        ShapedRecipe recipe = new ShapedRecipe(key, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1));
        recipe.shape("GTG", "TAT", "GTG");
        recipe.setIngredient('G', Material.GOLD_BLOCK);
        recipe.setIngredient('T', Material.TORCHFLOWER);
        recipe.setIngredient('A', Material.APPLE);
        plugin.getServer().addRecipe(recipe);
    }

    private void registerBlessings() {
        registerBlessing("c_bless", Material.MELON_SLICE, Material.GLISTERING_MELON_SLICE, "C's Bless", NamedTextColor.GOLD);
        registerBlessing("ami_bless", Material.CARROT, Material.GOLDEN_CARROT, "Ami's Bless", NamedTextColor.RED);
        registerBlessing("la_bless", Material.APPLE, Material.GOLDEN_APPLE, "La's Bless", NamedTextColor.BLUE);
    }

    private void registerBlessing(String id, Material bottomCenter, Material resultMaterial, String displayName, NamedTextColor color) {
        NamespacedKey key = new NamespacedKey(plugin, id);
        ItemStack result = new ItemStack(resultMaterial, 1);
        ItemMeta meta = result.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(displayName, color).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Right-click to consume", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Permanent stat boost", NamedTextColor.DARK_PURPLE).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, id + "_item"), PersistentDataType.BYTE, (byte) 1);
            result.setItemMeta(meta);
        }
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape("GTG", "DSd", "gBg");
        recipe.setIngredient('G', Material.GOLD_BLOCK);
        recipe.setIngredient('T', Material.TORCHFLOWER);
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('S', Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
        recipe.setIngredient('d', Material.DIAMOND);
        recipe.setIngredient('g', Material.GOLD_INGOT);
        recipe.setIngredient('B', bottomCenter);
        plugin.getServer().addRecipe(recipe);
    }

    private void registerBestBuddies() {
        NamespacedKey key = new NamespacedKey(plugin, "best_buddies");
        ItemStack result = new ItemStack(Material.WOLF_ARMOR, 1);
        ItemMeta meta = result.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Best Buddies Armor", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Best Buddies I", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Wolf takes 95% less damage", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Wolf deals 95% less damage", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Wolf gets Regen II", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            CustomEnchantManager enchantManager = plugin.getEnchantManager();
            meta.getPersistentDataContainer().set(enchantManager.getKey(EnchantmentType.BEST_BUDDIES), PersistentDataType.INTEGER, 1);
            result.setItemMeta(meta);
        }
        ShapelessRecipe recipe = new ShapelessRecipe(key, result);
        recipe.addIngredient(Material.BONE);
        recipe.addIngredient(Material.DIAMOND);
        plugin.getServer().addRecipe(recipe);
    }

    private void registerSickles() {
        for (Material hoeMat : sickleManager.getSickleTiers()) {
            Material ingredient = sickleManager.getSickleIngredient(hoeMat);
            if (ingredient == null) continue;
            ItemStack sickle = sickleManager.createSickle(hoeMat);
            if (sickle == null) continue;
            String tierName = hoeMat.name().replace("_HOE", "").toLowerCase();
            NamespacedKey key = new NamespacedKey(plugin, "sickle_" + tierName);
            ShapedRecipe recipe = new ShapedRecipe(key, sickle);
            recipe.shape(" I ", " H ", "   ");
            recipe.setIngredient('I', ingredient);
            recipe.setIngredient('H', hoeMat);
            plugin.getServer().addRecipe(recipe);
        }
    }

    // =================== INFINITY GAUNTLET (Phase 22) ===================
    private void registerInfinityGauntlet() {
        if (infinityStoneManager == null || infinityGauntletManager == null) return;

        // Infinity Gauntlet = Thanos Glove + all 6 finished Infinity Stones
        // Layout:
        //  P T S    (Power, Time, Space)
        //  R G M    (Reality, Glove, Mind)
        //  . O .    (., sOul, .)
        NamespacedKey key = new NamespacedKey(plugin, "infinity_gauntlet");
        ItemStack gauntlet = infinityGauntletManager.createInfinityGauntlet();
        if (gauntlet == null) return;

        ShapedRecipe recipe = new ShapedRecipe(key, gauntlet);
        recipe.shape("PTS", "RGM", " O ");

        // Each ingredient is a finished Infinity Stone (custom items checked via onPrepareCraft)
        // We use placeholder materials here — the actual validation happens in onPrepareCraft
        recipe.setIngredient('P', Material.CRYING_OBSIDIAN);   // Power Stone placeholder
        recipe.setIngredient('T', Material.CLOCK);             // Time Stone placeholder
        recipe.setIngredient('S', Material.ENDER_EYE);         // Space Stone placeholder
        recipe.setIngredient('R', Material.REDSTONE_BLOCK);    // Reality Stone placeholder
        recipe.setIngredient('G', Material.NETHERITE_INGOT);   // Glove placeholder (center)
        recipe.setIngredient('M', Material.LAPIS_BLOCK);       // Mind Stone placeholder
        recipe.setIngredient('O', Material.GHAST_TEAR);        // Soul Stone placeholder

        plugin.getServer().addRecipe(recipe);
        plugin.getLogger().info("Registered Infinity Gauntlet crafting recipe.");
    }
    // =================== CRAFTABLE ARMOR (Phase 15) ===================
    private void registerCraftableArmor() {
        if (legendaryArmorManager == null) return;
        // Reinforced Leather: Leather + String
        registerArmorSet("reinforced_leather", LegendaryArmorSet.REINFORCED_LEATHER, Material.LEATHER, Material.STRING);
        // Copper Armor: Copper Ingot + Chain
        registerArmorSet("copper_armor", LegendaryArmorSet.COPPER_ARMOR, Material.COPPER_INGOT, Material.IRON_BARS);
        // Chainmail Reinforced: Iron Nugget + Chain
        registerArmorSet("chainmail_reinforced", LegendaryArmorSet.CHAINMAIL_REINFORCED, Material.IRON_NUGGET, Material.IRON_BARS);
        // Amethyst Armor: Amethyst Shard + Iron Ingot
        registerArmorSet("amethyst_armor", LegendaryArmorSet.AMETHYST_ARMOR, Material.AMETHYST_SHARD, Material.IRON_INGOT);
        // Bone Armor: Bone + Iron Ingot
        registerArmorSet("bone_armor", LegendaryArmorSet.BONE_ARMOR, Material.BONE, Material.IRON_INGOT);
        // Sculk Armor: Sculk + Echo Shard
        registerArmorSet("sculk_armor", LegendaryArmorSet.SCULK_ARMOR, Material.SCULK, Material.ECHO_SHARD);
        plugin.getLogger().info("Registered 6 craftable armor sets (24 recipes).");
    }

    private void registerArmorSet(String prefix, LegendaryArmorSet set, Material primary, Material accent) {
        // Helmet: APA / P P
        ItemStack helmet = legendaryArmorManager.createArmorPiece(set, LegendaryArmorSet.ArmorSlot.HELMET);
        NamespacedKey hKey = new NamespacedKey(plugin, prefix + "_helmet");
        ShapedRecipe hRecipe = new ShapedRecipe(hKey, helmet);
        hRecipe.shape("APA", "P P");
        hRecipe.setIngredient('A', accent);
        hRecipe.setIngredient('P', primary);
        plugin.getServer().addRecipe(hRecipe);

        // Chestplate: P P / APA / PPP
        ItemStack chest = legendaryArmorManager.createArmorPiece(set, LegendaryArmorSet.ArmorSlot.CHESTPLATE);
        NamespacedKey cKey = new NamespacedKey(plugin, prefix + "_chestplate");
        ShapedRecipe cRecipe = new ShapedRecipe(cKey, chest);
        cRecipe.shape("P P", "APA", "PPP");
        cRecipe.setIngredient('A', accent);
        cRecipe.setIngredient('P', primary);
        plugin.getServer().addRecipe(cRecipe);

        // Leggings: APA / P P / P P
        ItemStack legs = legendaryArmorManager.createArmorPiece(set, LegendaryArmorSet.ArmorSlot.LEGGINGS);
        NamespacedKey lKey = new NamespacedKey(plugin, prefix + "_leggings");
        ShapedRecipe lRecipe = new ShapedRecipe(lKey, legs);
        lRecipe.shape("APA", "P P", "P P");
        lRecipe.setIngredient('A', accent);
        lRecipe.setIngredient('P', primary);
        plugin.getServer().addRecipe(lRecipe);

        // Boots: A A / P P
        ItemStack boots = legendaryArmorManager.createArmorPiece(set, LegendaryArmorSet.ArmorSlot.BOOTS);
        NamespacedKey bKey = new NamespacedKey(plugin, prefix + "_boots");
        ShapedRecipe bRecipe = new ShapedRecipe(bKey, boots);
        bRecipe.shape("A A", "P P");
        bRecipe.setIngredient('A', accent);
        bRecipe.setIngredient('P', primary);
        plugin.getServer().addRecipe(bRecipe);
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (event.getRecipe() == null) return;
        ItemStack[] matrix = event.getInventory().getMatrix();

        // --- 1. Block custom weapons from non-plugin recipes ---
        for (ItemStack ingredient : matrix) {
            if (ingredient != null && isCustomWeapon(ingredient)) {
                if (event.getRecipe() instanceof ShapedRecipe shaped) {
                    if (shaped.getKey().getNamespace().equals(plugin.getName().toLowerCase())) continue;
                }
                if (event.getRecipe() instanceof ShapelessRecipe shapeless) {
                    if (shapeless.getKey().getNamespace().equals(plugin.getName().toLowerCase())) continue;
                }
                event.getInventory().setResult(null);
                return;
            }
        }

        // --- 2. Block sickle-to-sickle re-crafting ---
        if (event.getRecipe() instanceof ShapedRecipe shaped) {
            String rKey = shaped.getKey().getKey();
            if (rKey.startsWith("sickle_")) {
                ItemStack center = matrix.length >= 5 ? matrix[4] : null;
                if (center != null && isCustomWeapon(center)) {
                    event.getInventory().setResult(null);
                    return;
                }
            }
        }
    }

    private boolean isCustomWeapon(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return sickleManager.isSickle(item);
    }
}
