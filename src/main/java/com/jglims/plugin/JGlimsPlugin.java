package com.jglims.plugin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.jglims.plugin.abyss.AbyssDimensionManager;
import com.jglims.plugin.abyss.AbyssDragonBoss;
import com.jglims.plugin.custommobs.CustomMobListener;
import com.jglims.plugin.custommobs.CustomMobManager;
import com.jglims.plugin.custommobs.CustomMobSpawnManager;
import com.jglims.plugin.custommobs.CustomMobType;
import com.jglims.plugin.custommobs.DropItemManager;
import com.jglims.plugin.dimensions.AetherDimensionManager;
import com.jglims.plugin.dimensions.DimensionPortalManager;
import com.jglims.plugin.dimensions.JurassicDimensionManager;
import com.jglims.plugin.dimensions.LunarDimensionManager;
import com.jglims.plugin.magic.MagicItemListener;
import com.jglims.plugin.magic.MagicItemManager;
import com.jglims.plugin.vampire.VampireAbilityListener;
import com.jglims.plugin.vampire.VampireAbilityManager;
import com.jglims.plugin.vampire.VampireListener;
import com.jglims.plugin.vampire.VampireManager;
import com.jglims.plugin.blessings.BlessingListener;
import com.jglims.plugin.blessings.BlessingManager;
import com.jglims.plugin.config.ConfigManager;
import com.jglims.plugin.crafting.CraftedItemListener;
import com.jglims.plugin.crafting.CraftedItemManager;
import com.jglims.plugin.crafting.RecipeManager;
import com.jglims.plugin.crafting.VanillaRecipeRemover;
import com.jglims.plugin.enchantments.AnvilRecipeListener;
import com.jglims.plugin.enchantments.CustomEnchantManager;
import com.jglims.plugin.enchantments.EnchantmentEffectListener;
import com.jglims.plugin.enchantments.SoulboundListener;
import com.jglims.plugin.events.EventManager;
import com.jglims.plugin.guilds.GuildListener;
import com.jglims.plugin.guilds.GuildManager;
import com.jglims.plugin.legendary.InfinityGauntletManager;
import com.jglims.plugin.legendary.InfinityStoneManager;
import com.jglims.plugin.legendary.LegendaryAbilityListener;
import com.jglims.plugin.legendary.LegendaryArmorListener;
import com.jglims.plugin.legendary.LegendaryArmorManager;
import com.jglims.plugin.legendary.LegendaryArmorSet;
import com.jglims.plugin.legendary.LegendaryLootListener;
import com.jglims.plugin.legendary.LegendaryTier;
import com.jglims.plugin.legendary.LegendaryWeapon;
import com.jglims.plugin.legendary.LegendaryWeaponManager;
import com.jglims.plugin.mobs.BloodMoonManager;
import com.jglims.plugin.mobs.BossMasteryManager;
import com.jglims.plugin.mobs.RoamingBossManager;
import com.jglims.plugin.mobs.BossEnhancer;
import com.jglims.plugin.mobs.KingMobManager;
import com.jglims.plugin.mobs.MobDifficultyManager;
import com.jglims.plugin.powerups.PowerUpListener;
import com.jglims.plugin.powerups.PowerUpManager;
import com.jglims.plugin.quests.QuestManager;
import com.jglims.plugin.quests.QuestProgressListener;
import com.jglims.plugin.quests.NpcWizardManager;
import com.jglims.plugin.structures.StructureManager;
import com.jglims.plugin.utility.BestBuddiesListener;
import com.jglims.plugin.utility.DropRateListener;
import com.jglims.plugin.utility.EnchantTransferListener;
import com.jglims.plugin.utility.InventorySortListener;
import com.jglims.plugin.utility.LootBoosterListener;
import com.jglims.plugin.utility.PaleGardenFogTask;
import com.jglims.plugin.utility.VillagerTradeListener;
import com.jglims.plugin.weapons.SickleManager;
import com.jglims.plugin.weapons.SpearManager;
import com.jglims.plugin.weapons.WeaponMasteryManager;

import com.jglims.plugin.menu.CreativeMenuManager;
import com.jglims.plugin.menu.GuideBookManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class JGlimsPlugin extends JavaPlugin implements TabCompleter, Listener {

    private static JGlimsPlugin instance;
    private ConfigManager configManager;
    private CustomEnchantManager enchantManager;
    private BlessingManager blessingManager;
    private SickleManager sickleManager;
    private SpearManager spearManager;
    private LegendaryWeaponManager legendaryWeaponManager;
    private LegendaryArmorManager legendaryArmorManager;
    private CreativeMenuManager creativeMenuManager;
    private GuideBookManager guideBookManager;
    private RecipeManager recipeManager;
    private CraftedItemManager craftedItemManager;
    private CraftedItemListener craftedItemListener;
    private MobDifficultyManager mobDifficultyManager;
    private KingMobManager kingMobManager;
    private BloodMoonManager bloodMoonManager;
    private WeaponMasteryManager weaponMasteryManager;
    private GuildManager guildManager;
    private BossMasteryManager bossMasteryManager;
    private RoamingBossManager roamingBossManager;
    private PowerUpManager powerUpManager;
    private EventManager eventManager;
    private StructureManager structureManager;
    private AbyssDimensionManager abyssDimensionManager;
    private AbyssDragonBoss abyssDragonBoss;
    private InfinityStoneManager infinityStoneManager;
    private InfinityGauntletManager infinityGauntletManager;
    private QuestManager questManager;
    private NpcWizardManager npcWizardManager;
    private CustomMobManager customMobManager;
    private CustomMobSpawnManager customMobSpawnManager;
    private DropItemManager dropItemManager;
    private VampireManager vampireManager;
    private VampireAbilityManager vampireAbilityManager;
    private com.jglims.plugin.werewolf.WerewolfManager werewolfManager;
    private com.jglims.plugin.werewolf.WerewolfListener werewolfListener;
    private MagicItemManager magicItemManager;
    private DimensionPortalManager dimensionPortalManager;
    private AetherDimensionManager aetherDimensionManager;
    private LunarDimensionManager lunarDimensionManager;
    private JurassicDimensionManager jurassicDimensionManager;

    @Override
    public void onEnable() {
        instance = this;
        long start = System.currentTimeMillis();

        configManager = new ConfigManager(this);
        configManager.loadConfig();
        enchantManager = new CustomEnchantManager(this);
        blessingManager = new BlessingManager(this);

        sickleManager = new SickleManager(this);
        spearManager = new SpearManager(this, configManager);

        legendaryWeaponManager = new LegendaryWeaponManager(this);
        legendaryArmorManager = new LegendaryArmorManager(this);
        infinityStoneManager = new InfinityStoneManager(this);
        infinityGauntletManager = new InfinityGauntletManager(this, infinityStoneManager);
        powerUpManager = new PowerUpManager(this);
        structureManager = new StructureManager(this);
        abyssDimensionManager = new AbyssDimensionManager(this);
        abyssDimensionManager.initialize();
        abyssDragonBoss = new AbyssDragonBoss(this, abyssDimensionManager);
        eventManager = new EventManager(this);
        eventManager.initialize();
        customMobManager = new CustomMobManager(this);
        customMobSpawnManager = new CustomMobSpawnManager(this, customMobManager);
        dropItemManager = new DropItemManager(this);
        vampireManager = new VampireManager(this);
        vampireAbilityManager = new VampireAbilityManager(this, vampireManager);
        werewolfManager = new com.jglims.plugin.werewolf.WerewolfManager(this);
        werewolfListener = new com.jglims.plugin.werewolf.WerewolfListener(this, werewolfManager);
        magicItemManager = new MagicItemManager(this);
        dimensionPortalManager = new DimensionPortalManager(this);
        registerDimensionPortals();
        aetherDimensionManager = new AetherDimensionManager(this);
        aetherDimensionManager.initialize();
        lunarDimensionManager = new LunarDimensionManager(this);
        lunarDimensionManager.initialize();
        jurassicDimensionManager = new JurassicDimensionManager(this);
        jurassicDimensionManager.initialize();
        questManager = new QuestManager(this);
        npcWizardManager = new NpcWizardManager(this);
        creativeMenuManager = new CreativeMenuManager(this);
        guideBookManager = new GuideBookManager(this);

        craftedItemManager = new CraftedItemManager(this);
        recipeManager = new RecipeManager(this, sickleManager, spearManager,
                infinityStoneManager, infinityGauntletManager, legendaryArmorManager);
        recipeManager.setCraftedItemManager(craftedItemManager);
        recipeManager.setDropItemManager(dropItemManager);
        recipeManager.registerAllRecipes();
        VanillaRecipeRemover.remove(this);
        craftedItemListener = new CraftedItemListener(this, craftedItemManager, vampireManager);

        mobDifficultyManager = new MobDifficultyManager(this, configManager);
        kingMobManager = new KingMobManager(this, configManager);
        bloodMoonManager = new BloodMoonManager(this, configManager);
        weaponMasteryManager = new WeaponMasteryManager(this, configManager);
        guildManager = new GuildManager(this, configManager);
        bossMasteryManager = new BossMasteryManager(this);
        roamingBossManager = new RoamingBossManager(this);

        PluginManager pm = getServer().getPluginManager();
        AnvilRecipeListener anvilListener = new AnvilRecipeListener(this, enchantManager, legendaryWeaponManager);
        if (infinityStoneManager != null) anvilListener.setInfinityStoneManager(infinityStoneManager);
        pm.registerEvents(anvilListener, this);
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
        pm.registerEvents(recipeManager, this);
        pm.registerEvents(weaponMasteryManager, this);
        pm.registerEvents(bloodMoonManager, this);
        pm.registerEvents(new GuildListener(this, guildManager), this);
        pm.registerEvents(bossMasteryManager, this);
        pm.registerEvents(roamingBossManager, this);
        pm.registerEvents(new BestBuddiesListener(this, configManager), this);
        pm.registerEvents(new LegendaryAbilityListener(this, configManager, legendaryWeaponManager, guildManager), this);
        pm.registerEvents(new LegendaryLootListener(this, legendaryWeaponManager, bloodMoonManager), this);
        pm.registerEvents(new LegendaryArmorListener(this, legendaryArmorManager), this);
        pm.registerEvents(new PowerUpListener(this, powerUpManager), this);
        pm.registerEvents(structureManager, this);
        pm.registerEvents(structureManager.getBossManager(), this);
        pm.registerEvents(eventManager.getNetherStorm(), this);
        pm.registerEvents(eventManager.getPiglinUprising(), this);
        pm.registerEvents(eventManager.getVoidCollapse(), this);
        pm.registerEvents(abyssDimensionManager, this);
        pm.registerEvents(abyssDragonBoss, this);
        pm.registerEvents(eventManager.getPillagerWarParty(), this);
        pm.registerEvents(eventManager.getPillagerSiege(), this);
        pm.registerEvents(eventManager.getEndRift(), this);
        pm.registerEvents(infinityGauntletManager, this);
        pm.registerEvents(questManager, this);
        pm.registerEvents(new QuestProgressListener(this, questManager), this);
        pm.registerEvents(npcWizardManager, this);
        pm.registerEvents(new CustomMobListener(this, customMobManager), this);
        VampireListener vampireListener = new VampireListener(this, vampireManager);
        pm.registerEvents(vampireListener, this);
        vampireListener.startDayNightCycle();
        pm.registerEvents(new VampireAbilityListener(this, vampireManager, vampireAbilityManager), this);
        pm.registerEvents(werewolfListener, this);
        werewolfListener.startShadowWolfTicker();
        pm.registerEvents(craftedItemListener, this);
        pm.registerEvents(new MagicItemListener(this, magicItemManager), this);
        pm.registerEvents(dimensionPortalManager, this);
        pm.registerEvents(aetherDimensionManager, this);
        pm.registerEvents(lunarDimensionManager, this);
        pm.registerEvents(jurassicDimensionManager, this);
        pm.registerEvents(creativeMenuManager, this);
        pm.registerEvents(guideBookManager, this);

        getLogger().info("Legendary armor system loaded: " + LegendaryArmorSet.values().length + " sets.");
        for (LegendaryTier tier : LegendaryTier.values()) {
            LegendaryWeapon[] tierWeapons = LegendaryWeapon.byTier(tier);
            if (tierWeapons.length > 0) getLogger().info("  " + tier.getId() + ": " + tierWeapons.length + " weapons");
        }
        getLogger().info("Legendary weapon system loaded: " + LegendaryWeapon.values().length + " weapons across " + LegendaryTier.values().length + " tiers.");
        getLogger().info("Infinity Stone system loaded: " + InfinityStoneManager.StoneType.values().length + " stones.");
        getLogger().info("Infinity Gauntlet system loaded.");
        getLogger().info("Power-up system loaded.");
        getLogger().info("Boss mastery title system loaded.");
        getLogger().info("Structure generation system loaded (" + com.jglims.plugin.structures.StructureType.values().length + " structure types).");
        getLogger().info("Event system loaded: Nether Storm, Piglin Uprising, Void Collapse, End Rift.");
        getLogger().info("Roaming boss system loaded (The Watcher, Hellfire Drake, Frostbound Colossus, Jungle Predator, End Wraith, Abyssal Leviathan).");
        getLogger().info("Quest system loaded (6 quest lines, NPC Wizard shop).");

        if (configManager.isPaleGardenFogEnabled()) new PaleGardenFogTask(this).start(configManager.getPaleGardenFogCheckInterval());
        if (configManager.isBloodMoonEnabled()) bloodMoonManager.startScheduler();
        roamingBossManager.startScheduler();
        customMobSpawnManager.startScheduler();
        questManager.startScheduler();
        npcWizardManager.startScheduler();

        // Tab completion for /jglims
        if (getCommand("jglims") != null) {
            getCommand("jglims").setTabCompleter(this);
        }

        // First-join spawn guard: if a player hasn't played before, force them
        // to the Overworld's spawn point. Previously, a race condition during
        // plugin init could cause first-join players to land in the Jurassic
        // dimension instead of the Overworld.
        getServer().getPluginManager().registerEvents(this, this);

        long elapsed = System.currentTimeMillis() - start;
        getLogger().info("JGlimsPlugin v" + getDescription().getVersion() + " enabled in " + elapsed + "ms!");
    }

    @Override
    public void onDisable() {
        if (customMobManager != null) customMobManager.despawnAll();
        if (vampireManager != null) vampireManager.saveAll();
        if (werewolfManager != null) werewolfManager.cleanupAll();
        getLogger().info("JGlimsPlugin disabled.");
    }

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
        if (command.getName().equalsIgnoreCase("guia")) {
            if (!(sender instanceof Player player)) { sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED)); return true; }
            guideBookManager.giveAllVolumes(player);
            return true;
        }
        if (!command.getName().equalsIgnoreCase("jglims")) return false;
        if (args.length == 0) {
            sender.sendMessage(Component.text("JGlimsPlugin v" + getDescription().getVersion(), NamedTextColor.GOLD));
            sender.sendMessage(Component.text("Usage: /jglims <reload|stats|enchants|sort|mastery|legendary|armor|powerup|bosstitles|gauntlet|abyss|menu|guia|help>", NamedTextColor.YELLOW));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reload" -> { configManager.loadConfig(); sender.sendMessage(Component.text("Config reloaded!", NamedTextColor.GREEN)); }
            case "stats" -> { if (args.length < 2) { sender.sendMessage(Component.text("Usage: /jglims stats <player>", NamedTextColor.RED)); return true; } Player target = getServer().getPlayer(args[1]); if (target == null) { sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED)); return true; } blessingManager.showStats(sender, target); }
            case "enchants" -> enchantManager.listEnchantments(sender);
            case "sort" -> sender.sendMessage(Component.text("Inventory sorting is always active. Shift-click an empty slot in any container to sort!", NamedTextColor.GREEN));
            case "mastery" -> { if (sender instanceof Player player) weaponMasteryManager.showMastery(player); else sender.sendMessage(Component.text("Only players can view mastery.", NamedTextColor.RED)); }
            case "legendary" -> handleLegendaryCommand(sender, args);
            case "armor" -> handleArmorCommand(sender, args);
            case "powerup" -> handlePowerUpCommand(sender, args);
            case "bosstitles" -> { if (sender instanceof Player player) bossMasteryManager.showBossTitles(player); else sender.sendMessage(Component.text("Only players can view boss titles.", NamedTextColor.RED)); }
            case "gauntlet" -> handleGauntletCommand(sender, args);
            case "abyss" -> handleAbyssCommand(sender, args);
            case "locate" -> handleLocateCommand(sender, args);
            case "tp" -> handleTpCommand(sender, args);
            case "spawn" -> handleSpawnCommand(sender, args);
            case "boss" -> handleBossCommand(sender, args);
            case "werewolf" -> handleWerewolfCommand(sender, args);
            case "testmob" -> handleTestMobCommand(sender, args);
            case "testmodel" -> handleTestModelCommand(sender, args);
            case "help" -> {
                sender.sendMessage(Component.text("=== JGlimsPlugin Commands ===", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
                sender.sendMessage(Component.text("/jglims reload", NamedTextColor.YELLOW).append(Component.text(" - Reload config", NamedTextColor.GRAY)));
                sender.sendMessage(Component.text("/jglims stats <player>", NamedTextColor.YELLOW).append(Component.text(" - Show blessing stats", NamedTextColor.GRAY)));
                sender.sendMessage(Component.text("/jglims enchants", NamedTextColor.YELLOW).append(Component.text(" - List custom enchantments", NamedTextColor.GRAY)));
                sender.sendMessage(Component.text("/jglims sort", NamedTextColor.YELLOW).append(Component.text(" - Sorting info", NamedTextColor.GRAY)));
                sender.sendMessage(Component.text("/jglims mastery", NamedTextColor.YELLOW).append(Component.text(" - Weapon mastery progress", NamedTextColor.GRAY)));
                sender.sendMessage(Component.text("/jglims legendary <id|list|tier>", NamedTextColor.YELLOW).append(Component.text(" - Give/list legendary weapons (OP)", NamedTextColor.GRAY)));
                sender.sendMessage(Component.text("/jglims armor <set|list> [slot]", NamedTextColor.YELLOW).append(Component.text(" - Give/list legendary armor (OP)", NamedTextColor.GRAY)));
                sender.sendMessage(Component.text("/jglims powerup <type|stats> [player]", NamedTextColor.YELLOW).append(Component.text(" - Give power-ups / view stats (OP)", NamedTextColor.GRAY)));
                sender.sendMessage(Component.text("/jglims bosstitles", NamedTextColor.YELLOW).append(Component.text(" - View boss mastery titles", NamedTextColor.GRAY)));
                sender.sendMessage(Component.text("/jglims quests", NamedTextColor.YELLOW).append(Component.text(" - View quest progress", NamedTextColor.GRAY)));
                sender.sendMessage(Component.text("/jglims menu", NamedTextColor.YELLOW).append(Component.text(" - Creative item menu (GUI)", NamedTextColor.GRAY)));
                sender.sendMessage(Component.text("/jglims guia", NamedTextColor.YELLOW).append(Component.text(" - Receive guide books (PT-BR)", NamedTextColor.GRAY)));
                sender.sendMessage(Component.text("/jglims gauntlet <glove|gauntlet|stone> [type]", NamedTextColor.YELLOW).append(Component.text(" - Give Infinity items (OP)", NamedTextColor.GRAY)));
                sender.sendMessage(Component.text("/jglims abyss <key|tp>", NamedTextColor.YELLOW).append(Component.text(" - Abyss dimension commands (OP)", NamedTextColor.GRAY)));
                sender.sendMessage(Component.text("/jglims locate <structure>", NamedTextColor.YELLOW).append(Component.text(" - Force-build structure at your location (OP)", NamedTextColor.GRAY)));
                sender.sendMessage(Component.text("/jglims tp <dimension>", NamedTextColor.YELLOW).append(Component.text(" - Teleport to a custom dimension (OP)", NamedTextColor.GRAY)));
                sender.sendMessage(Component.text("/jglims spawn <mob> [amount]", NamedTextColor.YELLOW).append(Component.text(" - Spawn a custom mob (OP)", NamedTextColor.GRAY)));
                sender.sendMessage(Component.text("/jglims boss <name>", NamedTextColor.YELLOW).append(Component.text(" - Spawn a specific boss (OP)", NamedTextColor.GRAY)));
                sender.sendMessage(Component.text("/jglims testmodel <modelName>", NamedTextColor.YELLOW).append(Component.text(" - Debug: attach BetterModel to armor stand (OP)", NamedTextColor.GRAY)));
                sender.sendMessage(Component.text("/jglims testmob <MOB_TYPE>", NamedTextColor.YELLOW).append(Component.text(" - Debug: spawn visible zombie + model (OP)", NamedTextColor.GRAY)));
            }
            case "vampire" -> handleVampireCommand(sender, args);
            case "quests" -> { if (sender instanceof Player player) questManager.showQuestProgress(player); else sender.sendMessage(Component.text("Only players can view quests.", NamedTextColor.RED)); }
            case "menu" -> { if (sender instanceof Player player) creativeMenuManager.openMainMenu(player); else sender.sendMessage(Component.text("Only players can open the menu.", NamedTextColor.RED)); }
            case "guia" -> { if (sender instanceof Player player) guideBookManager.giveAllVolumes(player); else sender.sendMessage(Component.text("Only players can receive the guide.", NamedTextColor.RED)); }
            default -> sender.sendMessage(Component.text("Unknown subcommand. Use: reload, stats, enchants, sort, mastery, legendary, armor, powerup, bosstitles, gauntlet, abyss, vampire, menu, guia, quests, locate, tp, spawn, boss, help", NamedTextColor.RED));
        }
        return true;
    }

    private void handleGauntletCommand(CommandSender sender, String[] args) {
        if (!sender.isOp()) { sender.sendMessage(Component.text("You need OP to use this command.", NamedTextColor.RED)); return; }
        if (!(sender instanceof Player player)) { sender.sendMessage(Component.text("Only players can receive items.", NamedTextColor.RED)); return; }
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /jglims gauntlet <glove|gauntlet|stone <type>|fragment <type>>", NamedTextColor.YELLOW));
            sender.sendMessage(Component.text("Stone types: power, space, reality, soul, time, mind", NamedTextColor.GRAY));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "glove" -> {
                player.getInventory().addItem(infinityGauntletManager.createThanosGlove());
                player.sendMessage(Component.text("Received: ", NamedTextColor.GREEN)
                        .append(Component.text("Thanos Glove", TextColor.color(200, 150, 50)).decorate(TextDecoration.BOLD)));
            }
            case "gauntlet" -> {
                player.getInventory().addItem(infinityGauntletManager.createInfinityGauntlet());
                player.sendMessage(Component.text("Received: ", NamedTextColor.GREEN)
                        .append(Component.text("Infinity Gauntlet", TextColor.color(255, 215, 0)).decorate(TextDecoration.BOLD)));
            }
            case "stone" -> {
                if (args.length < 3) { sender.sendMessage(Component.text("Specify stone type: power, space, reality, soul, time, mind", NamedTextColor.RED)); return; }
                InfinityStoneManager.StoneType type = getStoneTypeFromArg(args[2]);
                if (type == null) { sender.sendMessage(Component.text("Unknown stone type: " + args[2], NamedTextColor.RED)); return; }
                player.getInventory().addItem(infinityStoneManager.createFinishedStone(type));
                player.sendMessage(Component.text("Received: ", NamedTextColor.GREEN)
                        .append(Component.text(type.getDisplayName(), type.getColor()).decorate(TextDecoration.BOLD)));
            }
            case "fragment" -> {
                if (args.length < 3) { sender.sendMessage(Component.text("Specify stone type: power, space, reality, soul, time, mind", NamedTextColor.RED)); return; }
                InfinityStoneManager.StoneType type = getStoneTypeFromArg(args[2]);
                if (type == null) { sender.sendMessage(Component.text("Unknown stone type: " + args[2], NamedTextColor.RED)); return; }
                player.getInventory().addItem(infinityStoneManager.createFragment(type));
                player.sendMessage(Component.text("Received: ", NamedTextColor.GREEN)
                        .append(Component.text(type.getDisplayName() + " Fragment", type.getColor()).decorate(TextDecoration.BOLD)));
            }
            default -> sender.sendMessage(Component.text("Unknown sub: glove, gauntlet, stone <type>, fragment <type>", NamedTextColor.RED));
        }
    }

    private InfinityStoneManager.StoneType getStoneTypeFromArg(String arg) {
        for (InfinityStoneManager.StoneType t : InfinityStoneManager.StoneType.values()) {
            if (t.getId().equalsIgnoreCase(arg)) return t;
        }
        return null;
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

    private void handleArmorCommand(CommandSender sender, String[] args) {
        if (!sender.isOp()) { sender.sendMessage(Component.text("You need OP to use this command.", NamedTextColor.RED)); return; }
        if (args.length < 2 || args[1].equalsIgnoreCase("list")) {
            sender.sendMessage(Component.text("=== Legendary Armor Sets (" + LegendaryArmorSet.values().length + ") ===", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
            for (LegendaryArmorSet set : LegendaryArmorSet.values()) {
                sender.sendMessage(Component.text("  " + set.getId(), set.getTier().getColor())
                        .append(Component.text("  " + set.getDisplayName() + " [" + set.getTier().getId() + "] Def:" + set.getTotalDefense(), NamedTextColor.GRAY)));
            }
            sender.sendMessage(Component.text("Use: /jglims armor <setId> [helmet|chestplate|leggings|boots|all]", NamedTextColor.YELLOW));
            return;
        }
        if (!(sender instanceof Player player)) { sender.sendMessage(Component.text("Only players can receive armor.", NamedTextColor.RED)); return; }
        LegendaryArmorSet set = LegendaryArmorSet.fromId(args[1].toLowerCase());
        if (set == null) { sender.sendMessage(Component.text("Unknown armor set: " + args[1], NamedTextColor.RED)); return; }
        String slotArg = args.length >= 3 ? args[2].toLowerCase() : "all";
        if (slotArg.equals("all")) {
            for (LegendaryArmorSet.ArmorSlot slot : LegendaryArmorSet.ArmorSlot.values()) {
                player.getInventory().addItem(legendaryArmorManager.createArmorPiece(set, slot));
            }
            player.sendMessage(Component.text("Received full ", NamedTextColor.GREEN)
                    .append(Component.text(set.getDisplayName(), set.getTier().getColor()).decorate(TextDecoration.BOLD))
                    .append(Component.text(" set!", NamedTextColor.GREEN)));
        } else {
            LegendaryArmorSet.ArmorSlot slot;
            try { slot = LegendaryArmorSet.ArmorSlot.valueOf(slotArg.toUpperCase()); }
            catch (Exception e) { sender.sendMessage(Component.text("Unknown slot: " + slotArg + ". Use: helmet, chestplate, leggings, boots, all", NamedTextColor.RED)); return; }
            player.getInventory().addItem(legendaryArmorManager.createArmorPiece(set, slot));
            player.sendMessage(Component.text("Received ", NamedTextColor.GREEN)
                    .append(Component.text(set.getDisplayName() + " " + slot.name(), set.getTier().getColor()).decorate(TextDecoration.BOLD)));
        }
    }

    private void handlePowerUpCommand(CommandSender sender, String[] args) {
        if (!sender.isOp()) { sender.sendMessage(Component.text("You need OP to use this command.", NamedTextColor.RED)); return; }
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /jglims powerup <heart|soul|titan|phoenix|keep|vitality|berserker|stats> [player]", NamedTextColor.YELLOW));
            return;
        }
        if (args[1].equalsIgnoreCase("stats")) {
            Player target;
            if (args.length >= 3) {
                target = getServer().getPlayer(args[2]);
                if (target == null) { sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED)); return; }
            } else if (sender instanceof Player p) { target = p; }
            else { sender.sendMessage(Component.text("Usage: /jglims powerup stats <player>", NamedTextColor.RED)); return; }
            powerUpManager.showPowerUpStats(target);
            return;
        }
        Player target;
        if (args.length >= 3) {
            target = getServer().getPlayer(args[2]);
            if (target == null) { sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED)); return; }
        } else if (sender instanceof Player p) { target = p; }
        else { sender.sendMessage(Component.text("Usage: /jglims powerup <type> <player>", NamedTextColor.RED)); return; }
        ItemStack powerUp = switch (args[1].toLowerCase()) {
            case "heart" -> powerUpManager.createHeartCrystal();
            case "soul" -> powerUpManager.createSoulFragment();
            case "titan" -> powerUpManager.createTitanResolve();
            case "phoenix" -> powerUpManager.createPhoenixFeather();
            case "keep" -> powerUpManager.createKeepInventorer();
            case "vitality" -> powerUpManager.createVitalityShard();
            case "berserker" -> powerUpManager.createBerserkerMark();
            default -> null;
        };
        if (powerUp == null) {
            sender.sendMessage(Component.text("Unknown power-up type: " + args[1], NamedTextColor.RED));
            sender.sendMessage(Component.text("Types: heart, soul, titan, phoenix, keep, vitality, berserker, stats", NamedTextColor.YELLOW));
            return;
        }
        target.getInventory().addItem(powerUp);
        sender.sendMessage(Component.text("Gave power-up to " + target.getName() + "!", NamedTextColor.GREEN));
    }

    public static JGlimsPlugin getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public CustomEnchantManager getEnchantManager() { return enchantManager; }
    public BlessingManager getBlessingManager() { return blessingManager; }
    public SickleManager getSickleManager() { return sickleManager; }
    public SpearManager getSpearManager() { return spearManager; }
    public LegendaryWeaponManager getLegendaryWeaponManager() { return legendaryWeaponManager; }
    public LegendaryArmorManager getLegendaryArmorManager() { return legendaryArmorManager; }
    public KingMobManager getKingMobManager() { return kingMobManager; }
    public WeaponMasteryManager getWeaponMasteryManager() { return weaponMasteryManager; }
    public BloodMoonManager getBloodMoonManager() { return bloodMoonManager; }
    public GuildManager getGuildManager() { return guildManager; }
    public PowerUpManager getPowerUpManager() { return powerUpManager; }
    public BossMasteryManager getBossMasteryManager() { return bossMasteryManager; }
    public EventManager getEventManager() { return eventManager; }
    public StructureManager getStructureManager() { return structureManager; }
    public AbyssDimensionManager getAbyssDimensionManager() { return abyssDimensionManager; }
    public AbyssDragonBoss getAbyssDragonBoss() { return abyssDragonBoss; }
    public InfinityStoneManager getInfinityStoneManager() { return infinityStoneManager; }
    public InfinityGauntletManager getInfinityGauntletManager() { return infinityGauntletManager; }
    public RoamingBossManager getRoamingBossManager() { return roamingBossManager; }
    public QuestManager getQuestManager() { return questManager; }
    public NpcWizardManager getNpcWizardManager() { return npcWizardManager; }
    public CustomMobManager getCustomMobManager() { return customMobManager; }
    public DropItemManager getDropItemManager() { return dropItemManager; }
    public CraftedItemManager getCraftedItemManager() { return craftedItemManager; }
    public CraftedItemListener getCraftedItemListener() { return craftedItemListener; }
    public VampireManager getVampireManager() { return vampireManager; }
    public com.jglims.plugin.werewolf.WerewolfManager getWerewolfManager() { return werewolfManager; }
    public VampireAbilityManager getVampireAbilityManager() { return vampireAbilityManager; }
    public MagicItemManager getMagicItemManager() { return magicItemManager; }
    public DimensionPortalManager getDimensionPortalManager() { return dimensionPortalManager; }
    public AetherDimensionManager getAetherDimensionManager() { return aetherDimensionManager; }
    public LunarDimensionManager getLunarDimensionManager() { return lunarDimensionManager; }
    public JurassicDimensionManager getJurassicDimensionManager() { return jurassicDimensionManager; }

    /**
     * Registers all dimension portal definitions.
     */
    private void registerDimensionPortals() {
        dimensionPortalManager.registerPortal(new DimensionPortalManager.PortalDefinition(
                "aether", Material.GLOWSTONE, Material.WATER_BUCKET, Material.LIGHT_BLUE_STAINED_GLASS,
                "world_aether", TextColor.color(100, 200, 255), "Aether"));
        dimensionPortalManager.registerPortal(new DimensionPortalManager.PortalDefinition(
                "lunar", Material.END_STONE, Material.ENDER_EYE, Material.GRAY_STAINED_GLASS,
                "world_lunar", TextColor.color(180, 180, 200), "Lunar"));
        dimensionPortalManager.registerPortal(new DimensionPortalManager.PortalDefinition(
                "jurassic", Material.BONE_BLOCK, Material.FLINT_AND_STEEL, Material.GREEN_STAINED_GLASS,
                "world_jurassic", TextColor.color(100, 180, 60), "Jurassic"));
    }
    public CustomMobSpawnManager getCustomMobSpawnManager() { return customMobSpawnManager; }
    public CreativeMenuManager getCreativeMenuManager() { return creativeMenuManager; }
    public GuideBookManager getGuideBookManager() { return guideBookManager; }

    private void handleAbyssCommand(CommandSender sender, String[] args) {
        if (!sender.isOp()) { sender.sendMessage(Component.text("You need OP to use this command.", NamedTextColor.RED)); return; }
        if (!(sender instanceof Player player)) { sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED)); return; }
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /jglims abyss <key|tp|boss>", NamedTextColor.YELLOW));
            sender.sendMessage(Component.text("  key - Get an Abyssal Key", NamedTextColor.GRAY));
            sender.sendMessage(Component.text("  tp   - Teleport directly to the Abyss", NamedTextColor.GRAY));
            sender.sendMessage(Component.text("  boss - Manually summon the Abyssal Dragon (must be in Abyss)", NamedTextColor.GRAY));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "key" -> {
                player.getInventory().addItem(abyssDimensionManager.createAbyssalKey());
                player.sendMessage(Component.text("Received: ", NamedTextColor.GREEN)
                        .append(Component.text("Abyssal Key", TextColor.color(170, 0, 0)).decorate(TextDecoration.BOLD)));
            }
            case "boss" -> {
                World abyssW = abyssDimensionManager.getAbyssWorld();
                if (abyssW == null) { player.sendMessage(Component.text("Abyss world not loaded!", NamedTextColor.RED)); return; }
                if (!player.getWorld().getName().equals("world_abyss")) {
                    player.sendMessage(Component.text("You must be in the Abyss to summon the dragon!", NamedTextColor.RED));
                    return;
                }
                abyssDragonBoss.manualTrigger(player);
            }
            case "tp" -> {
                World abyss = abyssDimensionManager.getAbyssWorld();
                if (abyss == null) { player.sendMessage(Component.text("Abyss world not loaded!", NamedTextColor.RED)); return; }
                // Land on the grand gate path outside the south wall (z=85)
                int safeY = 60;
                for (int sy = 120; sy > 30; sy--) {
                    org.bukkit.Material belowMat = abyss.getBlockAt(0, sy - 1, 115).getType();
                    org.bukkit.Material atMat = abyss.getBlockAt(0, sy, 115).getType();
                    org.bukkit.Material aboveMat = abyss.getBlockAt(0, sy + 1, 115).getType();
                    if (belowMat.isSolid() && !atMat.isSolid() && !aboveMat.isSolid()) {
                        safeY = sy;
                        break;
                    }
                }
                Location dest = new Location(abyss, 0.5, safeY, 115.5);
                dest.setYaw(0);
                player.teleport(dest);
                player.sendMessage(Component.text("Teleported to the Abyss citadel gate.", TextColor.color(170, 0, 0)));
                dest.setYaw(180f);
            }
            default -> sender.sendMessage(Component.text("Usage: /jglims abyss <key|tp|boss>", NamedTextColor.YELLOW));
        }
    }

    private void handleVampireCommand(CommandSender sender, String[] args) {
        if (!sender.isOp()) { sender.sendMessage(Component.text("You need OP to use this command.", NamedTextColor.RED)); return; }
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /jglims vampire <player> <set|remove|info|reset|essence>", NamedTextColor.YELLOW));
            return;
        }
        Player target = getServer().getPlayer(args[1]);
        if (target == null) { sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED)); return; }
        switch (args[2].toLowerCase()) {
            case "set" -> {
                if (args.length < 4) { sender.sendMessage(Component.text("Usage: /jglims vampire <player> set <FLEDGLING|VAMPIRE|ELDER_VAMPIRE|VAMPIRE_LORD|DRACULA>", NamedTextColor.RED)); return; }
                try {
                    com.jglims.plugin.vampire.VampireLevel level = com.jglims.plugin.vampire.VampireLevel.valueOf(args[3].toUpperCase());
                    com.jglims.plugin.vampire.VampireState state = vampireManager.getOrCreateState(target.getUniqueId());
                    if (!state.isVampire()) { state.setVampire(true); }
                    state.setLevel(level);
                    state.recalculateLevel();
                    vampireManager.save(state);
                    sender.sendMessage(Component.text("Set " + target.getName() + " to " + level.getDisplayName(), NamedTextColor.GREEN));
                } catch (IllegalArgumentException e) { sender.sendMessage(Component.text("Invalid level! Use: FLEDGLING, VAMPIRE, ELDER_VAMPIRE, VAMPIRE_LORD, DRACULA", NamedTextColor.RED)); }
            }
            case "remove" -> {
                vampireManager.removeVampirism(target);
                sender.sendMessage(Component.text("Removed vampirism from " + target.getName(), NamedTextColor.GREEN));
            }
            case "info" -> vampireManager.showVampireInfo(target, sender);
            case "reset" -> {
                com.jglims.plugin.vampire.VampireState state = vampireManager.getOrCreateState(target.getUniqueId());
                state.setBloodConsumed(0);
                state.setEvolversConsumed(0);
                state.setSuperBloodConsumed(0);
                state.setLevel(com.jglims.plugin.vampire.VampireLevel.FLEDGLING);
                state.recalculateLevel();
                vampireManager.save(state);
                sender.sendMessage(Component.text("Reset vampire progression for " + target.getName(), NamedTextColor.GREEN));
            }
            case "essence" -> {
                target.getInventory().addItem(vampireManager.createVampireEssence());
                sender.sendMessage(Component.text("Gave Vampire Essence to " + target.getName(), NamedTextColor.GREEN));
            }
            default -> sender.sendMessage(Component.text("Unknown sub: set, remove, info, reset, essence", NamedTextColor.RED));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  NEW ADMIN COMMANDS — locate, tp, spawn, boss
    // ═══════════════════════════════════════════════════════════════

    private void handleLocateCommand(CommandSender sender, String[] args) {
        if (!sender.isOp()) { sender.sendMessage(Component.text("You need OP to use this command.", NamedTextColor.RED)); return; }
        if (!(sender instanceof Player player)) { sender.sendMessage(Component.text("Only players can use /jglims locate.", NamedTextColor.RED)); return; }
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /jglims locate <structure>", NamedTextColor.YELLOW));
            sender.sendMessage(Component.text("Example: /jglims locate BONE_ARENA", NamedTextColor.GRAY));
            return;
        }
        com.jglims.plugin.structures.StructureType type;
        try {
            type = com.jglims.plugin.structures.StructureType.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException ex) {
            sender.sendMessage(Component.text("Unknown structure type: " + args[1], NamedTextColor.RED));
            sender.sendMessage(Component.text("Use /jglims locate <TAB> to see options.", NamedTextColor.GRAY));
            return;
        }
        // Force-build the structure 30 blocks in front of the player
        org.bukkit.util.Vector dir = player.getLocation().getDirection().setY(0).normalize();
        Location origin = player.getLocation().clone().add(dir.multiply(30));
        origin.setY(player.getWorld().getHighestBlockYAt(origin.getBlockX(), origin.getBlockZ()) + 1);
        structureManager.forceBuild(type, origin);
        player.sendMessage(Component.text("✦ Built " + type.getDisplayName() + " at "
                + origin.getBlockX() + ", " + origin.getBlockY() + ", " + origin.getBlockZ(), NamedTextColor.GREEN));
        player.sendMessage(Component.text("Teleporting you there...", NamedTextColor.GRAY));
        player.teleport(origin.add(0, 2, 0));
    }

    private void handleTpCommand(CommandSender sender, String[] args) {
        if (!sender.isOp()) { sender.sendMessage(Component.text("You need OP to use this command.", NamedTextColor.RED)); return; }
        if (!(sender instanceof Player player)) { sender.sendMessage(Component.text("Only players can use /jglims tp.", NamedTextColor.RED)); return; }
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /jglims tp <overworld|nether|end|abyss|aether|lunar|jurassic>", NamedTextColor.YELLOW));
            return;
        }
        String target = args[1].toLowerCase();
        String worldName = switch (target) {
            case "overworld", "world" -> "world";
            case "nether" -> "world_nether";
            case "end", "the_end" -> "world_the_end";
            case "abyss" -> "world_abyss";
            case "aether" -> "world_aether";
            case "lunar", "moon" -> "world_lunar";
            case "jurassic", "dino" -> "world_jurassic";
            default -> null;
        };
        if (worldName == null) {
            sender.sendMessage(Component.text("Unknown dimension: " + args[1], NamedTextColor.RED));
            return;
        }
        World world = getServer().getWorld(worldName);
        if (world == null) {
            sender.sendMessage(Component.text("World '" + worldName + "' is not loaded.", NamedTextColor.RED));
            return;
        }
        Location spawn = world.getSpawnLocation();
        // Snap to safe Y
        int safeY = world.getHighestBlockYAt(spawn.getBlockX(), spawn.getBlockZ());
        spawn.setY(safeY + 1);
        player.teleport(spawn);
        player.sendMessage(Component.text("✦ Teleported to " + target + " (" + worldName + ")", NamedTextColor.GREEN));
    }

    private void handleSpawnCommand(CommandSender sender, String[] args) {
        if (!sender.isOp()) { sender.sendMessage(Component.text("You need OP to use this command.", NamedTextColor.RED)); return; }
        if (!(sender instanceof Player player)) { sender.sendMessage(Component.text("Only players can use /jglims spawn.", NamedTextColor.RED)); return; }
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /jglims spawn <mob> [amount]", NamedTextColor.YELLOW));
            sender.sendMessage(Component.text("Example: /jglims spawn T_REX 1", NamedTextColor.GRAY));
            return;
        }
        com.jglims.plugin.custommobs.CustomMobType mobType;
        try {
            mobType = com.jglims.plugin.custommobs.CustomMobType.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException ex) {
            sender.sendMessage(Component.text("Unknown mob type: " + args[1], NamedTextColor.RED));
            sender.sendMessage(Component.text("Use /jglims spawn <TAB> to see options.", NamedTextColor.GRAY));
            return;
        }
        int amount = 1;
        if (args.length >= 3) {
            try { amount = Math.max(1, Math.min(20, Integer.parseInt(args[2]))); }
            catch (NumberFormatException ignored) {}
        }
        // Spawn at where the player is looking (up to 10 blocks away)
        org.bukkit.block.Block target = player.getTargetBlockExact(10);
        Location loc = target != null ? target.getLocation().add(0.5, 1, 0.5) : player.getLocation();
        int spawned = 0;
        for (int i = 0; i < amount; i++) {
            Location spawnLoc = loc.clone().add((Math.random() - 0.5) * 2, 0, (Math.random() - 0.5) * 2);
            if (customMobManager.spawnMob(mobType, spawnLoc) != null) spawned++;
        }
        player.sendMessage(Component.text("✦ Spawned " + spawned + "× " + mobType.getDisplayName(), NamedTextColor.GREEN));
    }

    private void handleBossCommand(CommandSender sender, String[] args) {
        if (!sender.isOp()) { sender.sendMessage(Component.text("You need OP to use this command.", NamedTextColor.RED)); return; }
        if (!(sender instanceof Player player)) { sender.sendMessage(Component.text("Only players can use /jglims boss.", NamedTextColor.RED)); return; }
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /jglims boss <name>", NamedTextColor.YELLOW));
            sender.sendMessage(Component.text("Bosses: THE_WARRIOR, KING_GLEEOK, GODZILLA, GHIDORAH, NETHER_KING, PITLORD, ILLIDAN,", NamedTextColor.GRAY));
            sender.sendMessage(Component.text("        WHULVK_WEREWOLF, JAVION_DRAGONKIN, OGRIN_GIANT, FROSTMAW, PROTECTOR_OF_FORGE,", NamedTextColor.GRAY));
            sender.sendMessage(Component.text("        INVADERLING_COMMANDER, SKELETON_DRAGON, REALISTIC_DRAGON, RIPPER_ZOMBIE,", NamedTextColor.GRAY));
            sender.sendMessage(Component.text("        WITHER_STORM, T_REX, DINOBOT, PARASAUROLOPHUS, ABYSS_DRAGON, MUSHROOM_MONSTROSITY", NamedTextColor.GRAY));
            return;
        }
        String bossName = args[1].toUpperCase();
        // Abyss Dragon is its own summon path
        if (bossName.equals("ABYSS_DRAGON")) {
            if (abyssDragonBoss != null) {
                abyssDragonBoss.manualTrigger(player);
                player.sendMessage(Component.text("✦ Triggered Abyss Dragon fight", NamedTextColor.DARK_PURPLE));
                return;
            }
        }
        com.jglims.plugin.custommobs.CustomMobType mobType;
        try {
            mobType = com.jglims.plugin.custommobs.CustomMobType.valueOf(bossName);
        } catch (IllegalArgumentException ex) {
            sender.sendMessage(Component.text("Unknown boss: " + args[1], NamedTextColor.RED));
            return;
        }
        // Verify it's actually a boss category
        if (mobType.getCategory() != com.jglims.plugin.custommobs.MobCategory.WORLD_BOSS
                && mobType.getCategory() != com.jglims.plugin.custommobs.MobCategory.EVENT_BOSS
                && mobType.getCategory() != com.jglims.plugin.custommobs.MobCategory.BOSS) {
            sender.sendMessage(Component.text(mobType.getDisplayName() + " is not a boss (category: "
                    + mobType.getCategory() + "). Use /jglims spawn instead.", NamedTextColor.YELLOW));
            return;
        }
        org.bukkit.block.Block target = player.getTargetBlockExact(15);
        Location loc = target != null ? target.getLocation().add(0.5, 1, 0.5) : player.getLocation();
        com.jglims.plugin.custommobs.CustomMobEntity spawned = customMobManager.spawnMob(mobType, loc);
        if (spawned != null) {
            player.sendMessage(Component.text("✦ Spawned boss: " + mobType.getDisplayName(), NamedTextColor.GOLD));
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.8f);
        } else {
            player.sendMessage(Component.text("Failed to spawn " + mobType.getDisplayName(), NamedTextColor.RED));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  WEREWOLF ADMIN COMMAND
    // ═══════════════════════════════════════════════════════════════

    private void handleWerewolfCommand(CommandSender sender, String[] args) {
        if (!sender.isOp()) { sender.sendMessage(Component.text("You need OP.", NamedTextColor.RED)); return; }
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /jglims werewolf <blood|infect|ability> [player]", NamedTextColor.YELLOW));
            return;
        }
        if (werewolfManager == null) {
            sender.sendMessage(Component.text("Werewolf system not loaded.", NamedTextColor.RED));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "blood" -> {
                if (!(sender instanceof Player p)) { sender.sendMessage(Component.text("Player only.", NamedTextColor.RED)); return; }
                p.getInventory().addItem(werewolfManager.createWerewolfBlood());
                p.sendMessage(Component.text("Received: Werewolf Blood", NamedTextColor.DARK_RED));
            }
            case "infect" -> {
                Player target = (args.length >= 3) ? getServer().getPlayer(args[2])
                        : (sender instanceof Player sp ? sp : null);
                if (target == null) { sender.sendMessage(Component.text("Player not found.", NamedTextColor.RED)); return; }
                werewolfManager.infect(target);
                sender.sendMessage(Component.text("Infected " + target.getName() + " with the werewolf curse.", NamedTextColor.GREEN));
            }
            case "ability" -> {
                if (!(sender instanceof Player p)) { sender.sendMessage(Component.text("Player only.", NamedTextColor.RED)); return; }
                p.getInventory().addItem(werewolfManager.createWolfFormItem());
                p.sendMessage(Component.text("Received: Wolf Form ability", NamedTextColor.DARK_RED));
            }
            default -> sender.sendMessage(Component.text("Unknown werewolf sub: blood, infect, ability", NamedTextColor.RED));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  DEBUG / ISOLATION TEST COMMANDS (for BetterModel troubleshooting)
    // ═══════════════════════════════════════════════════════════════

    /**
     * /jglims testmodel <modelName> — attach a BetterModel renderer to a
     * fresh ArmorStand at the player's crosshair. No CustomMobEntity, no
     * invisibility, no scale attribute — the most isolated render test.
     * Verbose console logging at every step so we can see exactly where
     * (if) it fails.
     */
    private void handleTestModelCommand(CommandSender sender, String[] args) {
        if (!sender.isOp()) { sender.sendMessage(Component.text("You need OP to use this command.", NamedTextColor.RED)); return; }
        if (!(sender instanceof Player player)) { sender.sendMessage(Component.text("Only players.", NamedTextColor.RED)); return; }
        if (args.length < 2) { player.sendMessage(Component.text("Usage: /jglims testmodel <model_name>", NamedTextColor.YELLOW)); return; }
        String modelName = args[1];
        getLogger().info("[TestModel] ── START: model='" + modelName + "' requester=" + player.getName() + " ──");

        Location loc;
        try {
            org.bukkit.block.Block target = player.getTargetBlockExact(30);
            loc = (target != null ? target.getLocation().add(0.5, 1.0, 0.5) : player.getLocation());
        } catch (Exception e) {
            loc = player.getLocation();
        }
        getLogger().info("[TestModel] Spawning ArmorStand at " + loc.getWorld().getName() + " ["
                + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + "]");

        ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setInvulnerable(true);
        stand.setGravity(false);
        stand.setBasePlate(false);
        stand.setSmall(false);
        stand.customName(Component.text("TestModel: " + modelName, NamedTextColor.AQUA));
        stand.setCustomNameVisible(true);
        getLogger().info("[TestModel] ArmorStand spawned, UUID=" + stand.getUniqueId());

        if (!Bukkit.getPluginManager().isPluginEnabled("BetterModel")) {
            getLogger().warning("[TestModel] BetterModel plugin NOT enabled — aborting.");
            player.sendMessage(Component.text("BetterModel not loaded!", NamedTextColor.RED));
            return;
        }
        try {
            java.util.Optional<kr.toxicity.model.api.data.renderer.ModelRenderer> renderer =
                    kr.toxicity.model.api.BetterModel.model(modelName);
            if (renderer.isEmpty()) {
                getLogger().warning("[TestModel] BetterModel.model('" + modelName + "') returned empty — model NOT LOADED server-side.");
                player.sendMessage(Component.text("Model not found: " + modelName, NamedTextColor.RED));
                return;
            }
            getLogger().info("[TestModel] BetterModel.model() returned renderer — attaching...");
            kr.toxicity.model.api.tracker.EntityTracker tracker = renderer.get().getOrCreate(
                    kr.toxicity.model.api.bukkit.platform.BukkitAdapter.adapt(stand));
            getLogger().info("[TestModel] EntityTracker created: " + (tracker != null ? "OK" : "NULL"));

            try {
                tracker.animate("idle", kr.toxicity.model.api.animation.AnimationModifier.builder()
                        .type(kr.toxicity.model.api.animation.AnimationIterator.Type.LOOP).build());
                getLogger().info("[TestModel] Started 'idle' animation.");
            } catch (Exception animEx) {
                getLogger().info("[TestModel] No 'idle' animation for model — " + animEx.getMessage());
            }

            player.sendMessage(Component.text("TestModel: armor stand + model '" + modelName
                    + "' spawned. If it renders OK, the CustomMobEntity pipeline is the issue.", NamedTextColor.GREEN));
            getLogger().info("[TestModel] ── END SUCCESS ──");
        } catch (Throwable t) {
            getLogger().severe("[TestModel] Exception: " + t.getClass().getSimpleName() + ": " + t.getMessage());
            t.printStackTrace();
            player.sendMessage(Component.text("Exception: " + t.getMessage(), NamedTextColor.RED));
        }
    }

    /**
     * /jglims testmob <MOB_TYPE> — spawn a zombie at the player's crosshair
     * and attach the MobType's model WITHOUT setInvisible() and WITHOUT
     * scale-attribute changes. Isolates invisibility+scale as variables.
     */
    private void handleTestMobCommand(CommandSender sender, String[] args) {
        if (!sender.isOp()) { sender.sendMessage(Component.text("You need OP to use this command.", NamedTextColor.RED)); return; }
        if (!(sender instanceof Player player)) { sender.sendMessage(Component.text("Only players.", NamedTextColor.RED)); return; }
        if (args.length < 2) { player.sendMessage(Component.text("Usage: /jglims testmob <MOB_TYPE>", NamedTextColor.YELLOW)); return; }
        CustomMobType type;
        try { type = CustomMobType.valueOf(args[1].toUpperCase()); }
        catch (Exception e) { player.sendMessage(Component.text("Unknown MOB_TYPE: " + args[1], NamedTextColor.RED)); return; }

        getLogger().info("[TestMob] ── START: type=" + type.name() + " model=" + type.getModelName() + " ──");

        Location loc;
        try {
            org.bukkit.block.Block target = player.getTargetBlockExact(30);
            loc = (target != null ? target.getLocation().add(0.5, 1.0, 0.5) : player.getLocation());
        } catch (Exception e) {
            loc = player.getLocation();
        }
        getLogger().info("[TestMob] Spawning base entity " + type.getBaseEntityType()
                + " at " + loc.getWorld().getName() + " [" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + "]");

        LivingEntity hitbox = (LivingEntity) loc.getWorld().spawnEntity(loc, type.getBaseEntityType());
        // DELIBERATELY do not call setInvisible(true), do not touch SCALE attribute.
        hitbox.setInvulnerable(true);
        hitbox.setSilent(true);
        hitbox.customName(Component.text("TestMob: " + type.name(), NamedTextColor.AQUA));
        hitbox.setCustomNameVisible(true);
        if (hitbox.getEquipment() != null) hitbox.getEquipment().clear();
        getLogger().info("[TestMob] Hitbox entity spawned, UUID=" + hitbox.getUniqueId()
                + " invisible=" + hitbox.isInvisible() + " (expected false)");

        if (!Bukkit.getPluginManager().isPluginEnabled("BetterModel")) {
            getLogger().warning("[TestMob] BetterModel plugin NOT enabled.");
            player.sendMessage(Component.text("BetterModel not loaded!", NamedTextColor.RED));
            return;
        }
        try {
            java.util.Optional<kr.toxicity.model.api.data.renderer.ModelRenderer> renderer =
                    kr.toxicity.model.api.BetterModel.model(type.getModelName());
            if (renderer.isEmpty()) {
                getLogger().warning("[TestMob] Model '" + type.getModelName() + "' NOT LOADED server-side.");
                player.sendMessage(Component.text("Model not found: " + type.getModelName(), NamedTextColor.RED));
                return;
            }
            getLogger().info("[TestMob] Model resolved — attaching tracker...");
            kr.toxicity.model.api.tracker.EntityTracker tracker = renderer.get().getOrCreate(
                    kr.toxicity.model.api.bukkit.platform.BukkitAdapter.adapt(hitbox));
            getLogger().info("[TestMob] EntityTracker created: " + (tracker != null ? "OK" : "NULL"));
            try {
                tracker.animate("idle", kr.toxicity.model.api.animation.AnimationModifier.builder()
                        .type(kr.toxicity.model.api.animation.AnimationIterator.Type.LOOP).build());
                getLogger().info("[TestMob] Playing 'idle'.");
            } catch (Exception animEx) {
                getLogger().info("[TestMob] idle animation missing — " + animEx.getMessage());
            }
            player.sendMessage(Component.text("TestMob spawned with model '" + type.getModelName()
                    + "'. Base=" + type.getBaseEntityType() + ", no invisibility, no scale.", NamedTextColor.GREEN));
            getLogger().info("[TestMob] ── END SUCCESS ──");
        } catch (Throwable t) {
            getLogger().severe("[TestMob] Exception: " + t.getClass().getSimpleName() + ": " + t.getMessage());
            t.printStackTrace();
            player.sendMessage(Component.text("Exception: " + t.getMessage(), NamedTextColor.RED));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  TAB COMPLETION
    // ═══════════════════════════════════════════════════════════════

    private static final java.util.List<String> SUBCOMMANDS = java.util.List.of(
            "reload", "stats", "enchants", "sort", "mastery", "legendary", "armor", "powerup",
            "bosstitles", "quests", "gauntlet", "abyss", "vampire", "menu", "guia", "help",
            "locate", "tp", "spawn", "boss", "werewolf", "testmob", "testmodel"
    );

    private static final java.util.List<String> DIMENSIONS = java.util.List.of(
            "overworld", "nether", "end", "abyss", "aether", "lunar", "jurassic"
    );

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("jglims")) return java.util.Collections.emptyList();
        if (args.length == 1) {
            return filterStarts(SUBCOMMANDS, args[0]);
        }
        if (args.length == 2) {
            return switch (args[0].toLowerCase()) {
                case "locate" -> filterStarts(
                        java.util.Arrays.stream(com.jglims.plugin.structures.StructureType.values())
                                .map(Enum::name).toList(),
                        args[1]);
                case "tp" -> filterStarts(DIMENSIONS, args[1]);
                case "spawn" -> filterStarts(
                        java.util.Arrays.stream(com.jglims.plugin.custommobs.CustomMobType.values())
                                .map(Enum::name).toList(),
                        args[1]);
                case "boss" -> {
                    java.util.List<String> bosses = new java.util.ArrayList<>();
                    for (com.jglims.plugin.custommobs.CustomMobType mt :
                            com.jglims.plugin.custommobs.CustomMobType.values()) {
                        com.jglims.plugin.custommobs.MobCategory cat = mt.getCategory();
                        if (cat == com.jglims.plugin.custommobs.MobCategory.WORLD_BOSS
                                || cat == com.jglims.plugin.custommobs.MobCategory.EVENT_BOSS
                                || cat == com.jglims.plugin.custommobs.MobCategory.BOSS) {
                            bosses.add(mt.name());
                        }
                    }
                    bosses.add("ABYSS_DRAGON");
                    yield filterStarts(bosses, args[1]);
                }
                case "vampire" -> {
                    java.util.List<String> names = new java.util.ArrayList<>();
                    for (Player p : getServer().getOnlinePlayers()) names.add(p.getName());
                    yield filterStarts(names, args[1]);
                }
                case "gauntlet" -> filterStarts(java.util.List.of("glove", "gauntlet", "stone", "fragment"), args[1]);
                case "abyss" -> filterStarts(java.util.List.of("key", "tp", "boss"), args[1]);
                case "powerup" -> filterStarts(java.util.List.of("heart", "soul", "titan", "phoenix", "keep", "vitality", "berserker", "stats"), args[1]);
                case "legendary" -> filterStarts(java.util.List.of("list", "COMMON", "RARE", "EPIC", "MYTHIC", "ABYSSAL"), args[1]);
                case "armor" -> filterStarts(java.util.List.of("list", "set"), args[1]);
                case "testmob" -> filterStarts(
                        java.util.Arrays.stream(com.jglims.plugin.custommobs.CustomMobType.values())
                                .map(Enum::name).toList(),
                        args[1]);
                case "testmodel" -> filterStarts(
                        java.util.Arrays.stream(com.jglims.plugin.custommobs.CustomMobType.values())
                                .map(com.jglims.plugin.custommobs.CustomMobType::getModelName)
                                .distinct().toList(),
                        args[1]);
                default -> java.util.Collections.emptyList();
            };
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("spawn")) {
            return filterStarts(java.util.List.of("1", "3", "5", "10"), args[2]);
        }
        return java.util.Collections.emptyList();
    }

    private static java.util.List<String> filterStarts(java.util.List<String> options, String prefix) {
        String p = prefix.toLowerCase();
        java.util.List<String> out = new java.util.ArrayList<>();
        for (String o : options) {
            if (o.toLowerCase().startsWith(p)) out.add(o);
        }
        return out;
    }

    /**
     * Forces new players to spawn in the Overworld. This is a safety net for
     * the race condition between the plugin's dimension init and Paper's
     * default-world selection — without it, first-join players occasionally
     * land in the Jurassic dimension instead of the Overworld.
     */
    @EventHandler
    public void onFirstJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Force ALL players joining to the overworld — not just first-timers.
        // This prevents the Jurassic dimension from capturing players' spawn.
        World overworld = getServer().getWorld("world");
        if (overworld == null) return;
        if (!player.getWorld().getName().equals("world")) {
            getServer().getScheduler().runTask(this, () ->
                    player.teleport(overworld.getSpawnLocation()));
            getLogger().info("[JoinGuard] Moved " + player.getName() + " from "
                    + player.getWorld().getName() + " to overworld spawn.");
        }
    }

    /**
     * Forces death-respawn to the Overworld. Without this, players who die in
     * custom dimensions respawn at the custom dimension's world spawn instead
     * of the Overworld, because Paper stores the respawn location per-player
     * and defaults to the world the player is in.
     */
    @EventHandler
    public void onRespawn(org.bukkit.event.player.PlayerRespawnEvent event) {
        Location respawnLoc = event.getRespawnLocation();
        if (respawnLoc.getWorld() == null) return;
        String worldName = respawnLoc.getWorld().getName();
        // If respawning in a custom dimension (anything except overworld/nether/end),
        // redirect to the Overworld spawn.
        if (!worldName.equals("world") && !worldName.equals("world_nether")
                && !worldName.equals("world_the_end")) {
            World overworld = getServer().getWorld("world");
            if (overworld != null) {
                event.setRespawnLocation(overworld.getSpawnLocation());
                getLogger().info("[RespawnGuard] Redirected " + event.getPlayer().getName()
                        + " from " + worldName + " to overworld.");
            }
        }
    }
}