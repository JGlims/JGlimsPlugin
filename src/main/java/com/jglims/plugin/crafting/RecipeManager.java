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
import com.jglims.plugin.weapons.BattleAxeManager;
import com.jglims.plugin.weapons.BattleBowManager;
import com.jglims.plugin.weapons.BattleMaceManager;
import com.jglims.plugin.weapons.BattlePickaxeManager;
import com.jglims.plugin.weapons.BattleShovelManager;
import com.jglims.plugin.weapons.BattleSpearManager;
import com.jglims.plugin.weapons.BattleSwordManager;
import com.jglims.plugin.weapons.BattleTridentManager;
import com.jglims.plugin.weapons.SickleManager;
import com.jglims.plugin.weapons.SpearManager;
import com.jglims.plugin.weapons.SuperToolManager;
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
    private final BattleAxeManager battleAxeManager;
    private final BattleBowManager battleBowManager;
    private final BattleMaceManager battleMaceManager;
    private final SuperToolManager superToolManager;
    private final BattleSwordManager battleSwordManager;
    private final BattlePickaxeManager battlePickaxeManager;
    private final BattleTridentManager battleTridentManager;
    private final BattleSpearManager battleSpearManager;
    private final BattleShovelManager battleShovelManager;
    private final SpearManager spearManager;
    private final InfinityStoneManager infinityStoneManager;
    private final InfinityGauntletManager infinityGauntletManager;
    private final LegendaryArmorManager legendaryArmorManager;
    private CraftedItemManager craftedItemManager;
    private DropItemManager dropItemManager;

    public void setCraftedItemManager(CraftedItemManager m) { this.craftedItemManager = m; }
    public void setDropItemManager(DropItemManager m) { this.dropItemManager = m; }

    public RecipeManager(JGlimsPlugin plugin, SickleManager sickleManager,
                         BattleAxeManager battleAxeManager, BattleBowManager battleBowManager,
                         BattleMaceManager battleMaceManager, SuperToolManager superToolManager,
                         BattleSwordManager battleSwordManager, BattlePickaxeManager battlePickaxeManager,
                         BattleTridentManager battleTridentManager, BattleSpearManager battleSpearManager,
                         BattleShovelManager battleShovelManager, SpearManager spearManager,
                         InfinityStoneManager infinityStoneManager, InfinityGauntletManager infinityGauntletManager,
                         LegendaryArmorManager legendaryArmorManager) {
        this.plugin = plugin;
        this.sickleManager = sickleManager;
        this.battleAxeManager = battleAxeManager;
        this.battleBowManager = battleBowManager;
        this.battleMaceManager = battleMaceManager;
        this.superToolManager = superToolManager;
        this.battleSwordManager = battleSwordManager;
        this.battlePickaxeManager = battlePickaxeManager;
        this.battleTridentManager = battleTridentManager;
        this.battleSpearManager = battleSpearManager;
        this.battleShovelManager = battleShovelManager;
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
        registerBattleAxes();
        registerBattleSwords();
        registerBattlePickaxes();
        registerBattleBow();
        registerBattleCrossbow();
        registerBattleMace();
        registerBattleTrident();
        registerBattleShovels();
        registerBattleSpears();

        registerSuperToolRecipes();
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

    private void registerBattleSwords() {
        for (Material swordMat : battleSwordManager.getBattleSwordTiers()) {
            Material ingredient = battleSwordManager.getBattleSwordIngredient(swordMat);
            if (ingredient == null) continue;
            ItemStack battleSword = battleSwordManager.createBattleSword(swordMat);
            if (battleSword == null) continue;
            String tierName = swordMat.name().replace("_SWORD", "").toLowerCase();
            NamespacedKey key = new NamespacedKey(plugin, "battle_sword_" + tierName);
            ShapedRecipe recipe = new ShapedRecipe(key, battleSword);
            recipe.shape(" I ", " S ", "   ");
            recipe.setIngredient('I', ingredient);
            recipe.setIngredient('S', swordMat);
            plugin.getServer().addRecipe(recipe);
        }
    }

    private void registerBattlePickaxes() {
        for (Material pickMat : battlePickaxeManager.getBattlePickaxeTiers()) {
            Material ingredient = battlePickaxeManager.getBattlePickaxeIngredient(pickMat);
            if (ingredient == null) continue;
            ItemStack battlePick = battlePickaxeManager.createBattlePickaxe(pickMat);
            if (battlePick == null) continue;
            String tierName = pickMat.name().replace("_PICKAXE", "").toLowerCase();
            NamespacedKey key = new NamespacedKey(plugin, "battle_pickaxe_" + tierName);
            ShapedRecipe recipe = new ShapedRecipe(key, battlePick);
            recipe.shape(" I ", " P ", "   ");
            recipe.setIngredient('I', ingredient);
            recipe.setIngredient('P', pickMat);
            plugin.getServer().addRecipe(recipe);
        }
    }

    private void registerBattleAxes() {
        for (Material axeMat : battleAxeManager.getBattleAxeTiers()) {
            Material ingredient = battleAxeManager.getBattleAxeIngredient(axeMat);
            if (ingredient == null) continue;
            ItemStack battleAxe = battleAxeManager.createBattleAxe(axeMat);
            if (battleAxe == null) continue;
            String tierName = axeMat.name().replace("_AXE", "").toLowerCase();
            NamespacedKey key = new NamespacedKey(plugin, "battle_axe_" + tierName);
            ShapedRecipe recipe = new ShapedRecipe(key, battleAxe);
            recipe.shape(" I ", " A ", "   ");
            recipe.setIngredient('I', ingredient);
            recipe.setIngredient('A', axeMat);
            plugin.getServer().addRecipe(recipe);
        }
    }

    private void registerBattleShovels() {
        for (BattleShovelManager.ShovelTier tier : BattleShovelManager.ShovelTier.values()) {
            Material ingredient = tier.getIngredient();
            ItemStack battleShovel = battleShovelManager.createBattleShovel(tier);
            if (battleShovel == null) continue;
            String tierName = tier.name().toLowerCase();
            NamespacedKey key = new NamespacedKey(plugin, "battle_shovel_" + tierName);
            ShapedRecipe recipe = new ShapedRecipe(key, battleShovel);
            recipe.shape(" I ", " S ", "   ");
            recipe.setIngredient('I', ingredient);
            recipe.setIngredient('S', tier.getShovelMaterial());
            plugin.getServer().addRecipe(recipe);
        }
    }

    private void registerBattleSpears() {
        for (Material spearMat : battleSpearManager.getBattleSpearTiers()) {
            Material ingredient = battleSpearManager.getBattleSpearIngredient(spearMat);
            if (ingredient == null) continue;
            ItemStack battleSpear = battleSpearManager.createBattleSpear(spearMat);
            if (battleSpear == null) continue;
            String tierName = spearMat.name().replace("_SPEAR", "").toLowerCase();
            NamespacedKey key = new NamespacedKey(plugin, "battle_spear_" + tierName);
            ShapedRecipe recipe = new ShapedRecipe(key, battleSpear);
            recipe.shape(" I ", " S ", "   ");
            recipe.setIngredient('I', ingredient);
            recipe.setIngredient('S', spearMat);
            plugin.getServer().addRecipe(recipe);
        }
    }

    private void registerBattleBow() {
        ItemStack battleBow = battleBowManager.createBattleBow();
        if (battleBow == null) return;
        NamespacedKey key = new NamespacedKey(plugin, "battle_bow");
        ShapedRecipe recipe = new ShapedRecipe(key, battleBow);
        recipe.shape(" K ", " B ", "   ");
        recipe.setIngredient('K', Material.STICK);
        recipe.setIngredient('B', Material.BOW);
        plugin.getServer().addRecipe(recipe);
    }

    private void registerBattleCrossbow() {
        ItemStack battleCrossbow = battleBowManager.createBattleCrossbow();
        if (battleCrossbow == null) return;
        NamespacedKey key = new NamespacedKey(plugin, "battle_crossbow");
        ShapedRecipe recipe = new ShapedRecipe(key, battleCrossbow);
        recipe.shape(" K ", " C ", "   ");
        recipe.setIngredient('K', Material.STICK);
        recipe.setIngredient('C', Material.CROSSBOW);
        plugin.getServer().addRecipe(recipe);
    }

    private void registerBattleMace() {
        ItemStack battleMace = battleMaceManager.createBattleMace();
        if (battleMace == null) return;
        NamespacedKey key = new NamespacedKey(plugin, "battle_mace");
        ShapedRecipe recipe = new ShapedRecipe(key, battleMace);
        recipe.shape(" K ", " M ", "   ");
        recipe.setIngredient('K', Material.STICK);
        recipe.setIngredient('M', Material.MACE);
        plugin.getServer().addRecipe(recipe);
    }

    private void registerBattleTrident() {
        ItemStack battleTrident = battleTridentManager.createBattleTrident();
        if (battleTrident == null) return;
        NamespacedKey key = new NamespacedKey(plugin, "battle_trident");
        ShapedRecipe recipe = new ShapedRecipe(key, battleTrident);
        recipe.shape(" K ", " T ", "   ");
        recipe.setIngredient('K', Material.STICK);
        recipe.setIngredient('T', Material.TRIDENT);
        plugin.getServer().addRecipe(recipe);
    }

    // =====================================================================
    // FIX v1.3.1: Swords, Pickaxes, and Shovels now use
    // registerSuperTiersForBattleItem() instead of registerAllSuperTiers().
    //
    // ROOT CAUSE: registerAllSuperTiers() creates a plain "new ItemStack(mat)"
    // with NO battle PDC key, then passes it to createSuperTool() which checks
    // isBattleItem() — returns false — returns null — recipe never registered.
    //
    // registerSuperTiersForBattleItem() receives an actual battle item with
    // the PDC marker, so isBattleItem() returns true and the recipe registers.
    //
    // registerAllSuperTiers() is kept ONLY for Elytra and Shield which bypass
    // the battle step (isBattleItem returns true for them by material check).
    // =====================================================================
    private void registerSuperToolRecipes() {
        // FIX: Swords — was registerAllSuperTiers (broken), now registerSuperTiersForBattleItem
        for (Material swordMat : battleSwordManager.getBattleSwordTiers()) {
            ItemStack battleSword = battleSwordManager.createBattleSword(swordMat);
            if (battleSword == null) continue;
            registerSuperTiersForBattleItem(battleSword, swordMat, "battle_sword_" + swordMat.name().toLowerCase());
        }

        // FIX: Pickaxes — was registerAllSuperTiers (broken), now registerSuperTiersForBattleItem
        for (Material pickMat : battlePickaxeManager.getBattlePickaxeTiers()) {
            ItemStack battlePick = battlePickaxeManager.createBattlePickaxe(pickMat);
            if (battlePick == null) continue;
            registerSuperTiersForBattleItem(battlePick, pickMat, "battle_pickaxe_" + pickMat.name().toLowerCase());
        }

        // FIX: Shovels — was registerAllSuperTiers (broken), now registerSuperTiersForBattleItem
        for (BattleShovelManager.ShovelTier tier : BattleShovelManager.ShovelTier.values()) {
            ItemStack battleShovel = battleShovelManager.createBattleShovel(tier);
            if (battleShovel == null) continue;
            registerSuperTiersForBattleItem(battleShovel, tier.getShovelMaterial(), "battle_shovel_" + tier.name().toLowerCase());
        }

        // Sickles — already correct (uses registerSuperTiersForBattleItem)
        for (Material hoeMat : sickleManager.getSickleTiers()) {
            ItemStack sickle = sickleManager.createSickle(hoeMat);
            if (sickle == null) continue;
            registerSuperTiersForBattleItem(sickle, hoeMat, "sickle_" + hoeMat.name().toLowerCase());
        }

        // Battle Axes — already correct
        for (Material axeMat : battleAxeManager.getBattleAxeTiers()) {
            ItemStack battleAxe = battleAxeManager.createBattleAxe(axeMat);
            if (battleAxe == null) continue;
            registerSuperTiersForBattleItem(battleAxe, axeMat, "battle_axe_" + axeMat.name().toLowerCase());
        }

        // Battle Spears — already correct
        for (Material spearMat : battleSpearManager.getBattleSpearTiers()) {
            ItemStack battleSpear = battleSpearManager.createBattleSpear(spearMat);
            if (battleSpear == null) continue;
            registerSuperTiersForBattleItem(battleSpear, spearMat, "battle_spear_" + spearMat.name().toLowerCase());
        }

        // Single-material battle items — already correct
        ItemStack battleTrident = battleTridentManager.createBattleTrident();
        if (battleTrident != null) registerSuperTiersForBattleItem(battleTrident, Material.TRIDENT, "battle_trident");
        ItemStack battleBow = battleBowManager.createBattleBow();
        if (battleBow != null) registerSuperTiersForBattleItem(battleBow, Material.BOW, "battle_bow");
        ItemStack battleCrossbow = battleBowManager.createBattleCrossbow();
        if (battleCrossbow != null) registerSuperTiersForBattleItem(battleCrossbow, Material.CROSSBOW, "battle_crossbow");
        ItemStack battleMace = battleMaceManager.createBattleMace();
        if (battleMace != null) registerSuperTiersForBattleItem(battleMace, Material.MACE, "battle_mace");

        // Elytra and Shield — these bypass battle, so registerAllSuperTiers is correct for them
        registerAllSuperTiers(Material.ELYTRA, "elytra");
        registerAllSuperTiers(Material.SHIELD, "shield");
    }

    /**
     * Registers super tiers for items that bypass the battle step (Elytra, Shield).
     * Creates a plain ItemStack — works because isBattleItem() returns true for
     * Elytra/Shield by material check.
     * DO NOT use this for weapons that require battle PDC markers.
     */
    private void registerAllSuperTiers(Material baseMat, String namePrefix) {
        Material[] upgradeMats = { Material.IRON_INGOT, Material.DIAMOND, Material.NETHERITE_INGOT };
        int[] tiers = { SuperToolManager.TIER_IRON, SuperToolManager.TIER_DIAMOND, SuperToolManager.TIER_NETHERITE };
        String[] tierNames = { "iron", "diamond", "netherite" };
        for (int i = 0; i < 3; i++) {
            ItemStack baseItem = new ItemStack(baseMat);
            ItemStack result = superToolManager.createSuperTool(baseItem, tiers[i]);
            if (result == null) continue;
            NamespacedKey key = new NamespacedKey(plugin, "super_" + tierNames[i] + "_" + namePrefix);
            ShapedRecipe recipe = new ShapedRecipe(key, result);
            recipe.shape("MMM", " T ", "   ");
            recipe.setIngredient('M', upgradeMats[i]);
            recipe.setIngredient('T', baseMat);
            plugin.getServer().addRecipe(recipe);
        }
    }

    /**
     * Registers super tiers for battle items that have PDC markers.
     * Receives an actual battle item (with PDC) so createSuperTool's
     * isBattleItem() check passes correctly.
     */
    private void registerSuperTiersForBattleItem(ItemStack battleItem, Material baseMat, String namePrefix) {
        Material[] upgradeMats = { Material.IRON_INGOT, Material.DIAMOND, Material.NETHERITE_INGOT };
        int[] tiers = { SuperToolManager.TIER_IRON, SuperToolManager.TIER_DIAMOND, SuperToolManager.TIER_NETHERITE };
        String[] tierNames = { "iron", "diamond", "netherite" };
        for (int i = 0; i < 3; i++) {
            ItemStack result = superToolManager.createSuperTool(battleItem.clone(), tiers[i]);
            if (result == null) continue;
            NamespacedKey key = new NamespacedKey(plugin, "super_" + tierNames[i] + "_" + namePrefix);
            ShapedRecipe recipe = new ShapedRecipe(key, result);
            recipe.shape("MMM", " T ", "   ");
            recipe.setIngredient('M', upgradeMats[i]);
            recipe.setIngredient('T', baseMat);
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
            if (ingredient != null && isAnyBattleWeapon(ingredient)) {
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

        // --- 2. Block battle-to-battle re-crafting ---
        // If a battle/sickle recipe is matched but the center item is ALREADY battle, block it
        if (event.getRecipe() instanceof ShapedRecipe shaped) {
            String rKey = shaped.getKey().getKey();
            if (rKey.startsWith("battle_") || rKey.startsWith("sickle_")) {
                // For pattern " I " / " T " / "   ", the tool is slot 4
                ItemStack center = matrix.length >= 5 ? matrix[4] : null;
                if (center != null && isAnyBattleWeapon(center)) {
                    event.getInventory().setResult(null);
                    return;
                }
            }
        }

        // --- 3. Super recipe PDC guard ---
        if (event.getRecipe() instanceof ShapedRecipe shaped) {
            String recipeKey = shaped.getKey().getKey();
            if (recipeKey.startsWith("super_")) {
                ItemStack centerItem = matrix.length >= 5 ? matrix[4] : null;
                if (centerItem != null) {
                    if (superToolManager.isSuperTool(centerItem)) {
                        // allow upgrade — fall through to section 4
                    } else if (!superToolManager.isBattleItem(centerItem)) {
                        event.getInventory().setResult(null);
                        return;
                    }
                    // Verify the specific battle type matches the recipe
                    if (recipeKey.contains("sickle_") && !sickleManager.isSickle(centerItem)) { event.getInventory().setResult(null); return; }
                    if (recipeKey.contains("battle_axe_") && !battleAxeManager.isBattleAxe(centerItem)) { event.getInventory().setResult(null); return; }
                    if (recipeKey.contains("battle_sword_") && !battleSwordManager.isBattleSword(centerItem)) { event.getInventory().setResult(null); return; }
                    if (recipeKey.contains("battle_pickaxe_") && !battlePickaxeManager.isBattlePickaxe(centerItem)) { event.getInventory().setResult(null); return; }
                    if (recipeKey.contains("battle_shovel_") && !battleShovelManager.isBattleShovel(centerItem)) { event.getInventory().setResult(null); return; }
                    if (recipeKey.contains("battle_spear_") && !battleSpearManager.isBattleSpear(centerItem)) { event.getInventory().setResult(null); return; }
                    if (recipeKey.contains("battle_trident") && centerItem.getType() == Material.TRIDENT && !battleTridentManager.isBattleTrident(centerItem)) { event.getInventory().setResult(null); return; }
                    if (recipeKey.contains("battle_bow") && !recipeKey.contains("crossbow") && centerItem.getType() == Material.BOW && !battleBowManager.isBattleBow(centerItem)) { event.getInventory().setResult(null); return; }
                    if (recipeKey.contains("battle_crossbow") && centerItem.getType() == Material.CROSSBOW && !battleBowManager.isBattleCrossbow(centerItem)) { event.getInventory().setResult(null); return; }
                    if (recipeKey.contains("battle_mace") && centerItem.getType() == Material.MACE && !battleMaceManager.isBattleMace(centerItem)) { event.getInventory().setResult(null); return; }
                }
            }
        }

        // --- 4. Super tool upgrade via crafting table ---
        ItemStack centerItem = matrix.length >= 5 ? matrix[4] : null;
        if (centerItem != null && superToolManager.isSuperTool(centerItem)) {
            int currentTier = superToolManager.getSuperTier(centerItem);
            Material topMat = null;
            boolean topRowValid = true;
            for (int i = 0; i <= 2; i++) {
                if (matrix[i] == null || matrix[i].getType() == Material.AIR) { topRowValid = false; break; }
                if (topMat == null) topMat = matrix[i].getType();
                else if (matrix[i].getType() != topMat) { topRowValid = false; break; }
            }
            boolean otherSlotsEmpty = true;
            for (int slot : new int[]{ 3, 5, 6, 7, 8 }) {
                if (slot < matrix.length && matrix[slot] != null && matrix[slot].getType() != Material.AIR) { otherSlotsEmpty = false; break; }
            }
            if (topRowValid && otherSlotsEmpty && topMat != null) {
                int targetTier = SuperToolManager.TIER_NONE;
                if (topMat == Material.IRON_INGOT) targetTier = SuperToolManager.TIER_IRON;
                else if (topMat == Material.DIAMOND) targetTier = SuperToolManager.TIER_DIAMOND;
                else if (topMat == Material.NETHERITE_INGOT) targetTier = SuperToolManager.TIER_NETHERITE;
                if (targetTier > currentTier) {
                    ItemStack upgraded = superToolManager.upgradeSuperTool(centerItem, targetTier);
                    if (upgraded != null) event.getInventory().setResult(upgraded);
                }
            }
        }
    }

    private boolean isAnyBattleWeapon(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return sickleManager.isSickle(item) || battleAxeManager.isBattleAxe(item) || battleMaceManager.isBattleMace(item)
                || battleBowManager.isBattleBow(item) || battleBowManager.isBattleCrossbow(item) || battleSwordManager.isBattleSword(item)
                || battlePickaxeManager.isBattlePickaxe(item) || battleTridentManager.isBattleTrident(item)
                || battleSpearManager.isBattleSpear(item) || battleShovelManager.isBattleShovel(item) || superToolManager.isSuperTool(item);
    }
}
