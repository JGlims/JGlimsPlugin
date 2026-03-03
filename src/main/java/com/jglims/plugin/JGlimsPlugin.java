package com.jglims.plugin;

import com.jglims.plugin.config.ConfigManager;
import com.jglims.plugin.enchantments.CustomEnchantManager;
import com.jglims.plugin.enchantments.AnvilRecipeListener;
import com.jglims.plugin.enchantments.EnchantmentEffectListener;
import com.jglims.plugin.enchantments.SoulboundListener;
import com.jglims.plugin.weapons.*;
import com.jglims.plugin.crafting.RecipeManager;
import com.jglims.plugin.crafting.VanillaRecipeRemover;
import com.jglims.plugin.blessings.BlessingManager;
import com.jglims.plugin.blessings.BlessingListener;
import com.jglims.plugin.mobs.MobDifficultyManager;
import com.jglims.plugin.mobs.BiomeMultipliers;
import com.jglims.plugin.mobs.BossEnhancer;
import com.jglims.plugin.mobs.KingMobManager;
import com.jglims.plugin.mobs.BloodMoonManager;
import com.jglims.plugin.guilds.GuildManager;
import com.jglims.plugin.guilds.GuildListener;
import com.jglims.plugin.utility.*;

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
    private BattleMaceManager battleMaceManager;
    private SuperToolManager superToolManager;
    private RecipeManager recipeManager;
    private MobDifficultyManager mobDifficultyManager;
    private KingMobManager kingMobManager;
    private WeaponMasteryManager weaponMasteryManager;
    private BloodMoonManager bloodMoonManager;
    private GuildManager guildManager;

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
        battleMaceManager = new BattleMaceManager(this);
        superToolManager = new SuperToolManager(this);

        // 5. Crafting recipes (now 6-arg constructor)
        recipeManager = new RecipeManager(this, sickleManager, battleAxeManager, battleBowManager, battleMaceManager, superToolManager);
        recipeManager.registerAllRecipes();

        // 6. Remove vanilla recipes we replace
        VanillaRecipeRemover.remove(this);

        // 7. Mob difficulty
        mobDifficultyManager = new MobDifficultyManager(this, configManager);

        // 8. King mob system
        kingMobManager = new KingMobManager(this, configManager);

        // 9. Weapon mastery (NEW v1.2.0)
        weaponMasteryManager = new WeaponMasteryManager(this, configManager);

        // 10. Blood moon (NEW v1.2.0)
        bloodMoonManager = new BloodMoonManager(this, configManager);

        // 11. Guild system (NEW v1.2.0)
        guildManager = new GuildManager(this, configManager);

        // 12. Register all event listeners
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
        pm.registerEvents(weaponMasteryManager, this);
        pm.registerEvents(bloodMoonManager, this);
        pm.registerEvents(new GuildListener(this, guildManager), this);
        pm.registerEvents(new WeaponAbilityListener(this, enchantManager, superToolManager,
            sickleManager, battleAxeManager, battleBowManager, battleMaceManager), this);

        // 13. Pale Garden fog
        if (configManager.isPaleGardenFogEnabled()) {
            new PaleGardenFogTask(this).start(configManager.getPaleGardenFogInterval());
        }

        // 14. Blood Moon scheduler
        if (configManager.isBloodMoonEnabled()) {
            bloodMoonManager.startScheduler();
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
        // /guild command
        if (command.getName().equalsIgnoreCase("guild")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("Only players can use guild commands.", NamedTextColor.RED));
                return true;
            }
            if (args.length == 0) {
                player.sendMessage(Component.text("Usage: /guild <create|invite|join|leave|kick|disband|info|list>", NamedTextColor.YELLOW));
                return true;
            }
            switch (args[0].toLowerCase()) {
                case "create" -> {
                    if (args.length < 2) { player.sendMessage(Component.text("Usage: /guild create <name>", NamedTextColor.RED)); return true; }
                    guildManager.createGuild(player, args[1]);
                }
                case "invite" -> {
                    if (args.length < 2) { player.sendMessage(Component.text("Usage: /guild invite <player>", NamedTextColor.RED)); return true; }
                    guildManager.invitePlayer(player, args[1]);
                }
                case "join" -> guildManager.joinGuild(player);
                case "leave" -> guildManager.leaveGuild(player);
                case "kick" -> {
                    if (args.length < 2) { player.sendMessage(Component.text("Usage: /guild kick <player>", NamedTextColor.RED)); return true; }
                    guildManager.kickPlayer(player, args[1]);
                }
                case "disband" -> guildManager.disbandGuild(player);
                case "info" -> guildManager.showGuildInfo(player);
                case "list" -> guildManager.listGuilds(player);
                default -> player.sendMessage(Component.text("Unknown subcommand.", NamedTextColor.RED));
            }
            return true;
        }

        // /jglims command
        if (!command.getName().equalsIgnoreCase("jglims")) return false;
        if (args.length == 0) {
            sender.sendMessage(Component.text("JGlimsPlugin v" + getDescription().getVersion(), NamedTextColor.GOLD));
            sender.sendMessage(Component.text("Usage: /jglims <reload|stats|enchants|sort|mastery>", NamedTextColor.YELLOW));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reload" -> {
                configManager.loadConfig();
                sender.sendMessage(Component.text("Config reloaded!", NamedTextColor.GREEN));
            }
            case "stats" -> {
                if (args.length < 2) { sender.sendMessage(Component.text("Usage: /jglims stats <player>", NamedTextColor.RED)); return true; }
                Player target = getServer().getPlayer(args[1]);
                if (target == null) { sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED)); return true; }
                blessingManager.showStats(sender, target);
            }
            case "enchants" -> enchantManager.listEnchantments(sender);
            case "sort" -> sender.sendMessage(Component.text("Inventory sorting is always active. Shift-click an empty slot in any container to sort!", NamedTextColor.GREEN));
            case "mastery" -> {
                if (sender instanceof Player player) weaponMasteryManager.showMastery(player);
                else sender.sendMessage(Component.text("Only players can view mastery.", NamedTextColor.RED));
            }
            default -> sender.sendMessage(Component.text("Unknown subcommand. Use: reload, stats, enchants, sort, mastery", NamedTextColor.RED));
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
    public BattleMaceManager getBattleMaceManager() { return battleMaceManager; }
    public SuperToolManager getSuperToolManager() { return superToolManager; }
    public KingMobManager getKingMobManager() { return kingMobManager; }
    public WeaponMasteryManager getWeaponMasteryManager() { return weaponMasteryManager; }
    public BloodMoonManager getBloodMoonManager() { return bloodMoonManager; }
    public GuildManager getGuildManager() { return guildManager; }
}
