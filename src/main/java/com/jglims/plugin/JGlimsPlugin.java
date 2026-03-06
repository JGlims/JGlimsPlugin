package com.jglims.plugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.jglims.plugin.blessings.BlessingListener;
import com.jglims.plugin.blessings.BlessingManager;
import com.jglims.plugin.config.ConfigManager;
import com.jglims.plugin.crafting.RecipeManager;
import com.jglims.plugin.crafting.VanillaRecipeRemover;
import com.jglims.plugin.enchantments.AnvilRecipeListener;
import com.jglims.plugin.enchantments.CustomEnchantManager;
import com.jglims.plugin.enchantments.EnchantmentEffectListener;
import com.jglims.plugin.enchantments.SoulboundListener;
import com.jglims.plugin.guilds.GuildListener;
import com.jglims.plugin.guilds.GuildManager;
import com.jglims.plugin.legendary.LegendaryAbilityListener;
import com.jglims.plugin.legendary.LegendaryLootListener;
import com.jglims.plugin.legendary.LegendaryTier;
import com.jglims.plugin.legendary.LegendaryWeapon;
import com.jglims.plugin.legendary.LegendaryWeaponManager;
import com.jglims.plugin.mobs.BloodMoonManager;
import com.jglims.plugin.mobs.BossEnhancer;
import com.jglims.plugin.mobs.KingMobManager;
import com.jglims.plugin.mobs.MobDifficultyManager;
import com.jglims.plugin.utility.BestBuddiesListener;
import com.jglims.plugin.utility.DropRateListener;
import com.jglims.plugin.utility.EnchantTransferListener;
import com.jglims.plugin.utility.InventorySortListener;
import com.jglims.plugin.utility.LootBoosterListener;
import com.jglims.plugin.utility.PaleGardenFogTask;
import com.jglims.plugin.utility.VillagerTradeListener;
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
import com.jglims.plugin.weapons.WeaponAbilityListener;
import com.jglims.plugin.weapons.WeaponMasteryManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class JGlimsPlugin extends JavaPlugin {

    private static JGlimsPlugin instance;
    private ConfigManager configManager;
    private CustomEnchantManager enchantManager;
    private BlessingManager blessingManager;
    private SickleManager sickleManager;
    private BattleAxeManager battleAxeManager;
    private BattleBowManager battleBowManager;
    private BattleMaceManager battleMaceManager;
    private BattleShovelManager battleShovelManager;
    private BattleSwordManager battleSwordManager;
    private BattlePickaxeManager battlePickaxeManager;
    private BattleTridentManager battleTridentManager;
    private BattleSpearManager battleSpearManager;
    private SpearManager spearManager;
    private SuperToolManager superToolManager;
    private LegendaryWeaponManager legendaryWeaponManager;
    private RecipeManager recipeManager;
    private MobDifficultyManager mobDifficultyManager;
    private KingMobManager kingMobManager;
    private BloodMoonManager bloodMoonManager;
    private WeaponMasteryManager weaponMasteryManager;
    private GuildManager guildManager;

    @Override
    public void onEnable() {
        instance = this;
        long start = System.currentTimeMillis();

        configManager = new ConfigManager(this);
        configManager.loadConfig();
        enchantManager = new CustomEnchantManager(this);
        blessingManager = new BlessingManager(this);

        sickleManager = new SickleManager(this);
        battleAxeManager = new BattleAxeManager(this);
        battleBowManager = new BattleBowManager(this);
        battleMaceManager = new BattleMaceManager(this);
        battleSwordManager = new BattleSwordManager(this);
        battlePickaxeManager = new BattlePickaxeManager(this);
        battleTridentManager = new BattleTridentManager(this);
        battleSpearManager = new BattleSpearManager(this);
        superToolManager = new SuperToolManager(this);
        spearManager = new SpearManager(this, configManager);
        battleShovelManager = new BattleShovelManager(this, configManager);

        legendaryWeaponManager = new LegendaryWeaponManager(this);

        recipeManager = new RecipeManager(this, sickleManager, battleAxeManager,
                battleBowManager, battleMaceManager, superToolManager,
                battleSwordManager, battlePickaxeManager, battleTridentManager,
                battleSpearManager, battleShovelManager, spearManager);
        recipeManager.registerAllRecipes();
        VanillaRecipeRemover.remove(this);

        mobDifficultyManager = new MobDifficultyManager(this, configManager);
        kingMobManager = new KingMobManager(this, configManager);
        bloodMoonManager = new BloodMoonManager(this, configManager);
        weaponMasteryManager = new WeaponMasteryManager(this, configManager);
        guildManager = new GuildManager(this, configManager);

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new AnvilRecipeListener(this, enchantManager, legendaryWeaponManager), this);
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
        pm.registerEvents(battleShovelManager, this);
        pm.registerEvents(superToolManager, this);
        pm.registerEvents(recipeManager, this);
        pm.registerEvents(weaponMasteryManager, this);
        pm.registerEvents(bloodMoonManager, this);
        pm.registerEvents(new GuildListener(this, guildManager), this);
        pm.registerEvents(new WeaponAbilityListener(this, configManager, enchantManager, superToolManager, spearManager, battleShovelManager, guildManager), this);
        pm.registerEvents(new BestBuddiesListener(this, configManager), this);
        pm.registerEvents(new LegendaryAbilityListener(this, configManager, legendaryWeaponManager, guildManager), this);
        pm.registerEvents(new LegendaryLootListener(this, legendaryWeaponManager, bloodMoonManager), this);

        for (LegendaryTier tier : LegendaryTier.values()) {
            LegendaryWeapon[] tierWeapons = LegendaryWeapon.byTier(tier);
            if (tierWeapons.length > 0) getLogger().info("  " + tier.getId() + ": " + tierWeapons.length + " weapons");
        }
        getLogger().info("Legendary weapon system loaded: " + LegendaryWeapon.values().length + " weapons across " + LegendaryTier.values().length + " tiers.");

        if (configManager.isPaleGardenFogEnabled()) new PaleGardenFogTask(this).start(configManager.getPaleGardenFogCheckInterval());
        if (configManager.isBloodMoonEnabled()) bloodMoonManager.startScheduler();

        long elapsed = System.currentTimeMillis() - start;
        getLogger().info("JGlimsPlugin v" + getDescription().getVersion() + " enabled in " + elapsed + "ms!");
    }

    @Override
    public void onDisable() { getLogger().info("JGlimsPlugin disabled."); }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("guild")) {
            if (!(sender instanceof Player player)) { sender.sendMessage(Component.text("Only players can use guild commands.", NamedTextColor.RED)); return true; }
            if (args.length == 0) { player.sendMessage(Component.text("Usage: /guild <create|invite|join|leave|kick|disband|info|list>", NamedTextColor.YELLOW)); return true; }
            switch (args[0].toLowerCase()) {
                case "create" -> { if (args.length < 2) { player.sendMessage(Component.text("Usage: /guild create <name>", NamedTextColor.RED)); return true; } guildManager.createGuild(player, args[1]); }
                case "invite" -> { if (args.length < 2) { player.sendMessage(Component.text("Usage: /guild invite <player>", NamedTextColor.RED)); return true; } guildManager.invitePlayer(player, args[1]); }
                case "join" -> guildManager.joinGuild(player);
                case "leave" -> guildManager.leaveGuild(player);
                case "kick" -> { if (args.length < 2) { player.sendMessage(Component.text("Usage: /guild kick <player>", NamedTextColor.RED)); return true; } guildManager.kickPlayer(player, args[1]); }
                case "disband" -> guildManager.disbandGuild(player);
                case "info" -> guildManager.showGuildInfo(player);
                case "list" -> guildManager.listGuilds(player);
                default -> player.sendMessage(Component.text("Unknown subcommand.", NamedTextColor.RED));
            }
            return true;
        }
        if (!command.getName().equalsIgnoreCase("jglims")) return false;
        if (args.length == 0) {
            sender.sendMessage(Component.text("JGlimsPlugin v" + getDescription().getVersion(), NamedTextColor.GOLD));
            sender.sendMessage(Component.text("Usage: /jglims <reload|stats|enchants|sort|mastery|legendary|help>", NamedTextColor.YELLOW));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reload" -> { configManager.loadConfig(); sender.sendMessage(Component.text("Config reloaded!", NamedTextColor.GREEN)); }
            case "stats" -> { if (args.length < 2) { sender.sendMessage(Component.text("Usage: /jglims stats <player>", NamedTextColor.RED)); return true; } Player target = getServer().getPlayer(args[1]); if (target == null) { sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED)); return true; } blessingManager.showStats(sender, target); }
            case "enchants" -> enchantManager.listEnchantments(sender);
            case "sort" -> sender.sendMessage(Component.text("Inventory sorting is always active. Shift-click an empty slot in any container to sort!", NamedTextColor.GREEN));
            case "mastery" -> { if (sender instanceof Player player) weaponMasteryManager.showMastery(player); else sender.sendMessage(Component.text("Only players can view mastery.", NamedTextColor.RED)); }
            case "legendary" -> handleLegendaryCommand(sender, args);
            case "help" -> {
                sender.sendMessage(Component.text("=== JGlimsPlugin Commands ===", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
                sender.sendMessage(Component.text("/jglims reload", NamedTextColor.YELLOW).append(Component.text(" - Reload config", NamedTextColor.GRAY)));
                sender.sendMessage(Component.text("/jglims stats <player>", NamedTextColor.YELLOW).append(Component.text(" - Show blessing stats", NamedTextColor.GRAY)));
                sender.sendMessage(Component.text("/jglims enchants", NamedTextColor.YELLOW).append(Component.text(" - List custom enchantments", NamedTextColor.GRAY)));
                sender.sendMessage(Component.text("/jglims sort", NamedTextColor.YELLOW).append(Component.text(" - Sorting info", NamedTextColor.GRAY)));
                sender.sendMessage(Component.text("/jglims mastery", NamedTextColor.YELLOW).append(Component.text(" - Weapon mastery progress", NamedTextColor.GRAY)));
                sender.sendMessage(Component.text("/jglims legendary <id|list|tier>", NamedTextColor.YELLOW).append(Component.text(" - Give/list legendary weapons (OP)", NamedTextColor.GRAY)));
            }
            default -> sender.sendMessage(Component.text("Unknown subcommand. Use: reload, stats, enchants, sort, mastery, legendary, help", NamedTextColor.RED));
        }
        return true;
    }

    private void handleLegendaryCommand(CommandSender sender, String[] args) {
        if (!sender.isOp()) { sender.sendMessage(Component.text("You need OP to use this command.", NamedTextColor.RED)); return; }
        if (args.length < 2 || args[1].equalsIgnoreCase("list")) {
            sender.sendMessage(Component.text("=== Legendary Weapons (" + LegendaryWeapon.values().length + ") ===", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
            for (LegendaryTier tier : LegendaryTier.values()) {
                LegendaryWeapon[] weapons = LegendaryWeapon.byTier(tier);
                if (weapons.length == 0) continue;
                sender.sendMessage(Component.text("-- " + tier.getId() + " (" + weapons.length + ") --", tier.getColor()).decorate(TextDecoration.BOLD));
                for (LegendaryWeapon w : weapons) {
                    sender.sendMessage(Component.text("  " + w.getId(), tier.getColor()).append(Component.text("  " + w.getDisplayName() + " (DMG " + w.getBaseDamage() + ")", NamedTextColor.GRAY)));
                }
            }
            sender.sendMessage(Component.text("Use: /jglims legendary <id>", NamedTextColor.YELLOW));
            return;
        }
        if (args[1].equalsIgnoreCase("tier")) {
            if (args.length < 3) { sender.sendMessage(Component.text("Usage: /jglims legendary tier <COMMON|RARE|EPIC|MYTHIC|ABYSSAL>", NamedTextColor.RED)); return; }
            LegendaryTier tier = LegendaryTier.fromId(args[2]);
            if (tier == null) { sender.sendMessage(Component.text("Unknown tier: " + args[2], NamedTextColor.RED)); return; }
            LegendaryWeapon[] weapons = LegendaryWeapon.byTier(tier);
            sender.sendMessage(Component.text("=== " + tier.getId() + " Weapons (" + weapons.length + ") ===", tier.getColor()).decorate(TextDecoration.BOLD));
            for (LegendaryWeapon w : weapons) {
                sender.sendMessage(Component.text("  " + w.getId(), tier.getColor()).append(Component.text("  " + w.getDisplayName() + " (DMG " + w.getBaseDamage() + ")", NamedTextColor.GRAY)));
            }
            return;
        }
        if (!(sender instanceof Player player)) { sender.sendMessage(Component.text("Only players can receive legendary weapons.", NamedTextColor.RED)); return; }
        String weaponId = args[1].toLowerCase();
        LegendaryWeapon weapon = LegendaryWeapon.fromId(weaponId);
        if (weapon == null) { sender.sendMessage(Component.text("Unknown legendary ID: " + weaponId, NamedTextColor.RED)); sender.sendMessage(Component.text("Use /jglims legendary list to see all IDs.", NamedTextColor.YELLOW)); return; }
        ItemStack item = legendaryWeaponManager.createWeapon(weapon);
        if (item == null) { sender.sendMessage(Component.text("Failed to create legendary weapon!", NamedTextColor.RED)); return; }
        player.getInventory().addItem(item);
        TextColor tierColor = weapon.getTier().getColor();
        player.sendMessage(Component.text("Received: ", NamedTextColor.GREEN).append(Component.text("[" + weapon.getTier().getId() + "] ", tierColor)).append(Component.text(weapon.getDisplayName(), tierColor).decorate(TextDecoration.BOLD)));
    }

    public static JGlimsPlugin getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public CustomEnchantManager getEnchantManager() { return enchantManager; }
    public BlessingManager getBlessingManager() { return blessingManager; }
    public SickleManager getSickleManager() { return sickleManager; }
    public BattleAxeManager getBattleAxeManager() { return battleAxeManager; }
    public BattleBowManager getBattleBowManager() { return battleBowManager; }
    public BattleMaceManager getBattleMaceManager() { return battleMaceManager; }
    public BattleShovelManager getBattleShovelManager() { return battleShovelManager; }
    public BattleSwordManager getBattleSwordManager() { return battleSwordManager; }
    public BattlePickaxeManager getBattlePickaxeManager() { return battlePickaxeManager; }
    public BattleTridentManager getBattleTridentManager() { return battleTridentManager; }
    public BattleSpearManager getBattleSpearManager() { return battleSpearManager; }
    public SpearManager getSpearManager() { return spearManager; }
    public SuperToolManager getSuperToolManager() { return superToolManager; }
    public LegendaryWeaponManager getLegendaryWeaponManager() { return legendaryWeaponManager; }
    public KingMobManager getKingMobManager() { return kingMobManager; }
    public WeaponMasteryManager getWeaponMasteryManager() { return weaponMasteryManager; }
    public BloodMoonManager getBloodMoonManager() { return bloodMoonManager; }
    public GuildManager getGuildManager() { return guildManager; }
}