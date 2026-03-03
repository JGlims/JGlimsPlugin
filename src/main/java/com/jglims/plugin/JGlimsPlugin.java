package com.jglims.plugin;

import com.jglims.plugin.config.ConfigManager;
import com.jglims.plugin.enchantments.CustomEnchantManager;
import com.jglims.plugin.enchantments.AnvilRecipeListener;
import com.jglims.plugin.enchantments.EnchantmentEffectListener;
import com.jglims.plugin.enchantments.SoulboundListener;
import com.jglims.plugin.weapons.SickleManager;
import com.jglims.plugin.weapons.BattleAxeManager;
import com.jglims.plugin.weapons.BattleBowManager;
import com.jglims.plugin.weapons.SuperToolManager;
import com.jglims.plugin.weapons.WeaponAbilityListener;
import com.jglims.plugin.crafting.RecipeManager;
import com.jglims.plugin.crafting.VanillaRecipeRemover;
import com.jglims.plugin.blessings.BlessingManager;
import com.jglims.plugin.blessings.BlessingListener;
import com.jglims.plugin.mobs.MobDifficultyManager;
import com.jglims.plugin.mobs.BiomeMultipliers;
import com.jglims.plugin.mobs.BossEnhancer;
import com.jglims.plugin.mobs.KingMobManager;
import com.jglims.plugin.utility.InventorySortListener;
import com.jglims.plugin.utility.EnchantTransferListener;
import com.jglims.plugin.utility.LootBoosterListener;
import com.jglims.plugin.utility.DropRateListener;
import com.jglims.plugin.utility.VillagerTradeListener;
import com.jglims.plugin.utility.PaleGardenFogTask;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class JGlimsPlugin extends JavaPlugin {

    private static JGlimsPlugin instance;
    private ConfigManager configManager;
    private CustomEnchantManager enchantManager;
    private BlessingManager blessingManager;
    private SickleManager sickleManager;
    private BattleAxeManager battleAxeManager;
    private BattleBowManager battleBowManager;
    private SuperToolManager superToolManager;
    private RecipeManager recipeManager;
    private MobDifficultyManager mobDifficultyManager;
    private KingMobManager kingMobManager;

    @Override
    public void onEnable() {
        instance = this;
        long start = System.currentTimeMillis();

        // 1. Config
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        // 2. Enchantment system
        enchantManager = new CustomEnchantManager(this);

        // 3. Blessing system
        blessingManager = new BlessingManager(this);

        // 4. Weapon systems
        sickleManager = new SickleManager(this);
        battleAxeManager = new BattleAxeManager(this);
        battleBowManager = new BattleBowManager(this);
        superToolManager = new SuperToolManager(this);

        // 5. Crafting recipes
        recipeManager = new RecipeManager(this, sickleManager, battleAxeManager, battleBowManager, superToolManager);
        recipeManager.registerAllRecipes();

        // 6. Remove vanilla recipes we replace
        VanillaRecipeRemover.remove(this);

        // 7. Mob difficulty
        mobDifficultyManager = new MobDifficultyManager(this, configManager);

        // 8. King mob system
        kingMobManager = new KingMobManager(this, configManager);

        // 9. Register all event listeners
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new AnvilRecipeListener(this, enchantManager), this);
        pm.registerEvents(new EnchantmentEffectListener(this, enchantManager), this);
        pm.registerEvents(new SoulboundListener(this, enchantManager), this);
        pm.registerEvents(new BlessingListener(this, blessingManager), this);
        pm.registerEvents(mobDifficultyManager, this);
        pm.registerEvents(new BossEnhancer(this, configManager), this);
        pm.registerEvents(kingMobManager, this);
        pm.registerEvents(new InventorySortListener(this), this);
        pm.registerEvents(new EnchantTransferListener(this, enchantManager), this);
        pm.registerEvents(new LootBoosterListener(this, configManager), this);
        pm.registerEvents(new DropRateListener(this, configManager), this);
        pm.registerEvents(new VillagerTradeListener(this, configManager), this);
        pm.registerEvents(sickleManager, this);
        pm.registerEvents(battleAxeManager, this);
        pm.registerEvents(superToolManager, this);
        pm.registerEvents(recipeManager, this);
        pm.registerEvents(new WeaponAbilityListener(this, enchantManager, superToolManager,
            sickleManager, battleAxeManager, battleBowManager), this);

        // 10. Pale Garden fog scheduled task
        if (configManager.isPaleGardenFogEnabled()) {
            new PaleGardenFogTask(this).start(configManager.getPaleGardenFogInterval());
        }

        long elapsed = System.currentTimeMillis() - start;
        getLogger().info("JGlimsPlugin v" + getDescription().getVersion() + " enabled in " + elapsed + "ms!");
    }

    @Override
    public void onDisable() {
        getLogger().info("JGlimsPlugin disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("jglims")) return false;

        if (args.length == 0) {
            sender.sendMessage(Component.text("JGlimsPlugin v" + getDescription().getVersion(), NamedTextColor.GOLD));
            sender.sendMessage(Component.text("Usage: /jglims <reload|stats|enchants|sort>", NamedTextColor.YELLOW));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                configManager.loadConfig();
                sender.sendMessage(Component.text("Config reloaded!", NamedTextColor.GREEN));
            }
            case "stats" -> {
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /jglims stats <player>", NamedTextColor.RED));
                    return true;
                }
                Player target = getServer().getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                    return true;
                }
                blessingManager.showStats(sender, target);
            }
            case "enchants" -> {
                enchantManager.listEnchantments(sender);
            }
            case "sort" -> {
                sender.sendMessage(Component.text("Inventory sorting is always active. Shift-click an empty slot in any container to sort!", NamedTextColor.GREEN));
            }
            default -> {
                sender.sendMessage(Component.text("Unknown subcommand. Use: reload, stats, enchants, sort", NamedTextColor.RED));
            }
        }
        return true;
    }

    public static JGlimsPlugin getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public CustomEnchantManager getEnchantManager() { return enchantManager; }
    public BlessingManager getBlessingManager() { return blessingManager; }
    public SickleManager getSickleManager() { return sickleManager; }
    public BattleAxeManager getBattleAxeManager() { return battleAxeManager; }
    public BattleBowManager getBattleBowManager() { return battleBowManager; }
    public SuperToolManager getSuperToolManager() { return superToolManager; }
    public KingMobManager getKingMobManager() { return kingMobManager; }
}
