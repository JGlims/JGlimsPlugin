package com.jglims.plugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.jglims.plugin.abyss.AbyssDimensionManager;
import com.jglims.plugin.abyss.AbyssDragonBoss;
import com.jglims.plugin.blessings.BlessingListener;
import com.jglims.plugin.blessings.BlessingManager;
import com.jglims.plugin.config.ConfigManager;
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
import com.jglims.plugin.structures.StructureManager;
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
    private LegendaryArmorManager legendaryArmorManager;
    private RecipeManager recipeManager;
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

        recipeManager = new RecipeManager(this, sickleManager, battleAxeManager,
                battleBowManager, battleMaceManager, superToolManager,
                battleSwordManager, battlePickaxeManager, battleTridentManager,
                battleSpearManager, battleShovelManager, spearManager,
                infinityStoneManager, infinityGauntletManager);
        recipeManager.registerAllRecipes();
        VanillaRecipeRemover.remove(this);

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
        pm.registerEvents(battleAxeManager, this);
        pm.registerEvents(battleShovelManager, this);
        pm.registerEvents(superToolManager, this);
        pm.registerEvents(recipeManager, this);
        pm.registerEvents(weaponMasteryManager, this);
        pm.registerEvents(bloodMoonManager, this);
        pm.registerEvents(new GuildListener(this, guildManager), this);
        pm.registerEvents(bossMasteryManager, this);
        pm.registerEvents(roamingBossManager, this);
        pm.registerEvents(new WeaponAbilityListener(this, configManager, enchantManager, superToolManager, spearManager, battleShovelManager, guildManager), this);
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
        getLogger().info("Roaming boss system loaded (The Watcher, Hellfire Drake).");

        if (configManager.isPaleGardenFogEnabled()) new PaleGardenFogTask(this).start(configManager.getPaleGardenFogCheckInterval());
        if (configManager.isBloodMoonEnabled()) bloodMoonManager.startScheduler();
        roamingBossManager.startScheduler();

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
            sender.sendMessage(Component.text("Usage: /jglims <reload|stats|enchants|sort|mastery|legendary|armor|powerup|bosstitles|gauntlet|help>", NamedTextColor.YELLOW));
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
                sender.sendMessage(Component.text("/jglims gauntlet <glove|gauntlet|stone> [type]", NamedTextColor.YELLOW).append(Component.text(" - Give Infinity items (OP)", NamedTextColor.GRAY)));
            }
            default -> sender.sendMessage(Component.text("Unknown subcommand. Use: reload, stats, enchants, sort, mastery, legendary, armor, powerup, bosstitles, gauntlet, help", NamedTextColor.RED));
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
            sender.sendMessage(Component.text("Usage: /jglims powerup <heart|soul|titan|phoenix|keep|stats> [player]", NamedTextColor.YELLOW));
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
            default -> null;
        };
        if (powerUp == null) {
            sender.sendMessage(Component.text("Unknown power-up type: " + args[1], NamedTextColor.RED));
            sender.sendMessage(Component.text("Types: heart, soul, titan, phoenix, keep, stats", NamedTextColor.YELLOW));
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
}