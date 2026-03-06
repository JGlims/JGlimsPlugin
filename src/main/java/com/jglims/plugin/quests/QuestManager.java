package com.jglims.plugin.quests;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.legendary.LegendaryTier;
import com.jglims.plugin.legendary.LegendaryWeapon;
import com.jglims.plugin.legendary.LegendaryWeaponManager;
import com.jglims.plugin.powerups.PowerUpManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Quest Manager — NPC quest villagers with 6 themed quest lines.
 * Each line has 3 stages with escalating requirements and rewards.
 * Quest progress is stored in the player's PersistentDataContainer.
 */
public class QuestManager implements Listener {

    private final JGlimsPlugin plugin;
    private final Random random = new Random();

    // PDC keys
    private final NamespacedKey KEY_QUEST_NPC;
    private final NamespacedKey KEY_QUEST_NPC_TYPE;

    // Player quest progress keys (one per quest line)
    private final NamespacedKey KEY_HUNTER_STAGE;
    private final NamespacedKey KEY_HUNTER_PROGRESS;
    private final NamespacedKey KEY_MINER_STAGE;
    private final NamespacedKey KEY_MINER_PROGRESS;
    private final NamespacedKey KEY_EXPLORER_STAGE;
    private final NamespacedKey KEY_EXPLORER_PROGRESS;
    private final NamespacedKey KEY_NETHER_STAGE;
    private final NamespacedKey KEY_NETHER_PROGRESS;
    private final NamespacedKey KEY_DRAGON_STAGE;
    private final NamespacedKey KEY_DRAGON_PROGRESS;
    private final NamespacedKey KEY_ABYSSAL_STAGE;
    private final NamespacedKey KEY_ABYSSAL_PROGRESS;

    // Track active quest NPCs to prevent duplicates
    private final Set<UUID> activeQuestNPCs = new HashSet<>();

    // Quest line definitions
    public enum QuestLine {
        HUNTER("Hunter's Path", "Artemis the Hunter", TextColor.color(200, 150, 50),
            new String[]{"Kill 30 hostile mobs", "Kill 10 King mobs", "Kill 3 Roaming Bosses"},
            new int[]{30, 10, 3}),
        MINER("Miner's Depths", "Dorin the Miner", TextColor.color(100, 200, 255),
            new String[]{"Mine 128 diamond ore", "Mine 32 ancient debris", "Mine 64 deepslate emerald ore"},
            new int[]{128, 32, 64}),
        EXPLORER("Explorer's Journey", "Magellan the Explorer", TextColor.color(100, 255, 150),
            new String[]{"Visit all 3 dimensions", "Find 5 custom structures", "Walk 50,000 blocks"},
            new int[]{3, 5, 50000}),
        NETHER("Nether Trials", "Pyralis the Flameborn", TextColor.color(255, 100, 30),
            new String[]{"Kill 50 Nether mobs", "Survive a Nether event", "Kill both Nether roaming bosses"},
            new int[]{50, 1, 2}),
        DRAGON("Dragon Slayer", "Voss the Dragonheart", TextColor.color(180, 0, 220),
            new String[]{"Kill 100 Endermen", "Kill the Ender Dragon", "Complete an End Rift event"},
            new int[]{100, 1, 1}),
        ABYSSAL("Abyssal Descent", "Nihilus the Void-Touched", TextColor.color(170, 0, 0),
            new String[]{"Enter the Abyss dimension", "Kill 50 mobs in the Abyss", "Defeat the Abyssal Leviathan"},
            new int[]{1, 50, 1});

        public final String displayName;
        public final String npcName;
        public final TextColor color;
        public final String[] stageDescriptions;
        public final int[] stageRequirements;

        QuestLine(String displayName, String npcName, TextColor color, String[] stageDescriptions, int[] stageRequirements) {
            this.displayName = displayName;
            this.npcName = npcName;
            this.color = color;
            this.stageDescriptions = stageDescriptions;
            this.stageRequirements = stageRequirements;
        }
    }

    public QuestManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        KEY_QUEST_NPC = new NamespacedKey(plugin, "quest_npc");
        KEY_QUEST_NPC_TYPE = new NamespacedKey(plugin, "quest_npc_type");
        KEY_HUNTER_STAGE = new NamespacedKey(plugin, "quest_hunter_stage");
        KEY_HUNTER_PROGRESS = new NamespacedKey(plugin, "quest_hunter_progress");
        KEY_MINER_STAGE = new NamespacedKey(plugin, "quest_miner_stage");
        KEY_MINER_PROGRESS = new NamespacedKey(plugin, "quest_miner_progress");
        KEY_EXPLORER_STAGE = new NamespacedKey(plugin, "quest_explorer_stage");
        KEY_EXPLORER_PROGRESS = new NamespacedKey(plugin, "quest_explorer_progress");
        KEY_NETHER_STAGE = new NamespacedKey(plugin, "quest_nether_stage");
        KEY_NETHER_PROGRESS = new NamespacedKey(plugin, "quest_nether_progress");
        KEY_DRAGON_STAGE = new NamespacedKey(plugin, "quest_dragon_stage");
        KEY_DRAGON_PROGRESS = new NamespacedKey(plugin, "quest_dragon_progress");
        KEY_ABYSSAL_STAGE = new NamespacedKey(plugin, "quest_abyssal_stage");
        KEY_ABYSSAL_PROGRESS = new NamespacedKey(plugin, "quest_abyssal_progress");
    }

    /**
     * Start the scheduler that periodically spawns quest NPCs near players.
     */
    public void startScheduler() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Clean up dead/invalid NPCs
                activeQuestNPCs.removeIf(uuid -> {
                    Entity e = plugin.getServer().getEntity(uuid);
                    return e == null || e.isDead() || !e.isValid();
                });
                // Try to spawn a quest villager near a random player
                List<Player> allPlayers = new ArrayList<>(plugin.getServer().getOnlinePlayers());
                if (allPlayers.isEmpty()) return;
                Player target = allPlayers.get(random.nextInt(allPlayers.size()));
                if (activeQuestNPCs.size() >= 12) return; // Cap at 12 active NPCs
                if (random.nextDouble() < 0.40) spawnRandomQuestNPC(target);
            }
        }.runTaskTimer(plugin, 6000L, 6000L); // Every 5 minutes

        plugin.getLogger().info("Quest NPC scheduler started (6 quest lines).");
    }

    /**
     * Spawn a quest NPC at a structure's boss spawn location.
     * Called by StructureBossManager after boss is defeated.
     */
    public void spawnQuestNPCAtStructure(Location loc, QuestLine questLine) {
        spawnQuestNPC(loc, questLine);
    }

    private void spawnRandomQuestNPC(Player nearPlayer) {
        QuestLine[] lines = QuestLine.values();
        QuestLine line = lines[random.nextInt(lines.length)];
        Location loc = nearPlayer.getLocation().add(random.nextInt(20) - 10, 0, random.nextInt(20) - 10);
        loc.setY(loc.getWorld().getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()) + 1);
        // Filter by dimension
        World.Environment env = loc.getWorld().getEnvironment();
        if (line == QuestLine.NETHER && env != World.Environment.NETHER) line = QuestLine.HUNTER;
        if (line == QuestLine.DRAGON && env != World.Environment.THE_END) line = QuestLine.EXPLORER;
        if (line == QuestLine.ABYSSAL && !loc.getWorld().getName().equalsIgnoreCase("world_abyss")) line = QuestLine.MINER;
        spawnQuestNPC(loc, line);
    }

    private void spawnQuestNPC(Location loc, QuestLine line) {
        Villager npc = loc.getWorld().spawn(loc, Villager.class);
        npc.customName(Component.text(line.npcName, line.color).decorate(TextDecoration.BOLD));
        npc.setCustomNameVisible(true);
        npc.setAI(false);
        npc.setInvulnerable(true);
        npc.setPersistent(true);
        npc.setRemoveWhenFarAway(false);
        npc.setSilent(true);
        npc.setAdult();
        // Set profession based on quest type
        switch (line) {
            case HUNTER -> npc.setProfession(Villager.Profession.WEAPONSMITH);
            case MINER -> npc.setProfession(Villager.Profession.TOOLSMITH);
            case EXPLORER -> npc.setProfession(Villager.Profession.CARTOGRAPHER);
            case NETHER -> npc.setProfession(Villager.Profession.CLERIC);
            case DRAGON -> npc.setProfession(Villager.Profession.LIBRARIAN);
            case ABYSSAL -> npc.setProfession(Villager.Profession.NITWIT);
        }
        npc.setVillagerLevel(5);
        npc.getPersistentDataContainer().set(KEY_QUEST_NPC, PersistentDataType.BYTE, (byte) 1);
        npc.getPersistentDataContainer().set(KEY_QUEST_NPC_TYPE, PersistentDataType.STRING, line.name());
        npc.setGlowing(true);
        activeQuestNPCs.add(npc.getUniqueId());

        // Despawn after 20 minutes
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!npc.isDead() && npc.isValid()) {
                    activeQuestNPCs.remove(npc.getUniqueId());
                    npc.getWorld().spawnParticle(Particle.PORTAL, npc.getLocation(), 30, 0.5, 1, 0.5);
                    npc.remove();
                }
            }
        }.runTaskLater(plugin, 24000L);
    }

    // ══════════════════════════════════════════════════════════════════
    // INTERACTION — Right-click quest NPC
    // ══════════════════════════════════════════════════════════════════

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Villager villager)) return;
        if (!villager.getPersistentDataContainer().has(KEY_QUEST_NPC, PersistentDataType.BYTE)) return;
        event.setCancelled(true);

        String typeStr = villager.getPersistentDataContainer().get(KEY_QUEST_NPC_TYPE, PersistentDataType.STRING);
        if (typeStr == null) return;
        QuestLine line;
        try { line = QuestLine.valueOf(typeStr); } catch (Exception e) { return; }

        Player player = event.getPlayer();
        handleQuestInteraction(player, line);
    }

    private void handleQuestInteraction(Player player, QuestLine line) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        NamespacedKey stageKey = getStageKey(line);
        NamespacedKey progressKey = getProgressKey(line);

        int stage = pdc.getOrDefault(stageKey, PersistentDataType.INTEGER, 0);
        int progress = pdc.getOrDefault(progressKey, PersistentDataType.INTEGER, 0);

        if (stage >= 3) {
            // Quest complete
            player.sendMessage(Component.text(""));
            player.sendMessage(Component.text("\u2605 ", NamedTextColor.GOLD)
                .append(Component.text(line.displayName, line.color).decorate(TextDecoration.BOLD))
                .append(Component.text(" — COMPLETED!", NamedTextColor.GREEN).decorate(TextDecoration.BOLD)));
            player.sendMessage(Component.text("  You have already completed this quest line!", NamedTextColor.GRAY));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1.0f, 1.0f);
            return;
        }

        int required = line.stageRequirements[stage];
        String desc = line.stageDescriptions[stage];

        if (stage == 0 && progress == 0) {
            // New quest — accept
            pdc.set(stageKey, PersistentDataType.INTEGER, 0);
            pdc.set(progressKey, PersistentDataType.INTEGER, 0);
            player.sendMessage(Component.text(""));
            player.sendMessage(Component.text("\u2605 Quest Accepted: ", NamedTextColor.GOLD)
                .append(Component.text(line.displayName, line.color).decorate(TextDecoration.BOLD)));
            player.sendMessage(Component.text("  Stage 1: ", NamedTextColor.YELLOW)
                .append(Component.text(desc, NamedTextColor.WHITE)));
            player.sendMessage(Component.text("  Progress: ", NamedTextColor.GRAY)
                .append(Component.text("0/" + required, NamedTextColor.AQUA)));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_TRADE, 1.0f, 1.2f);
            player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0, 2, 0), 15, 0.5, 0.5, 0.5);
            return;
        }

        if (progress >= required) {
            // Complete current stage, advance
            int newStage = stage + 1;
            pdc.set(stageKey, PersistentDataType.INTEGER, newStage);
            pdc.set(progressKey, PersistentDataType.INTEGER, 0);

            // Give stage rewards
            giveStageReward(player, line, stage);

            if (newStage >= 3) {
                // Quest line complete! Give final reward
                giveFinalReward(player, line);
                player.sendMessage(Component.text(""));
                player.sendMessage(Component.text("\u2605 ", NamedTextColor.GOLD)
                    .append(Component.text(line.displayName, line.color).decorate(TextDecoration.BOLD))
                    .append(Component.text(" — QUEST COMPLETE!", NamedTextColor.GREEN).decorate(TextDecoration.BOLD)));
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 60, 1, 2, 1);
            } else {
                String nextDesc = line.stageDescriptions[newStage];
                int nextReq = line.stageRequirements[newStage];
                player.sendMessage(Component.text(""));
                player.sendMessage(Component.text("\u2605 Stage Complete! ", NamedTextColor.GREEN)
                    .append(Component.text(line.displayName, line.color)));
                player.sendMessage(Component.text("  Next Stage: ", NamedTextColor.YELLOW)
                    .append(Component.text(nextDesc, NamedTextColor.WHITE)));
                player.sendMessage(Component.text("  Progress: ", NamedTextColor.GRAY)
                    .append(Component.text("0/" + nextReq, NamedTextColor.AQUA)));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
                player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0, 2, 0), 30, 1, 1, 1);
            }
        } else {
            // Show progress
            player.sendMessage(Component.text(""));
            player.sendMessage(Component.text("\u2605 ", NamedTextColor.GOLD)
                .append(Component.text(line.displayName, line.color).decorate(TextDecoration.BOLD))
                .append(Component.text(" — Stage " + (stage + 1) + "/3", NamedTextColor.GRAY)));
            player.sendMessage(Component.text("  Task: ", NamedTextColor.YELLOW)
                .append(Component.text(desc, NamedTextColor.WHITE)));
            player.sendMessage(Component.text("  Progress: ", NamedTextColor.GRAY)
                .append(Component.text(progress + "/" + required, NamedTextColor.AQUA)));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 1.0f, 1.0f);
        }
    }

    private void giveStageReward(Player player, QuestLine line, int completedStage) {
        PowerUpManager pum = plugin.getPowerUpManager();
        switch (completedStage) {
            case 0 -> {
                // Stage 1 rewards: diamonds + soul fragments
                player.getInventory().addItem(new ItemStack(Material.DIAMOND, 8 + random.nextInt(8)));
                for (int i = 0; i < 2; i++) player.getInventory().addItem(pum.createSoulFragment());
                player.sendMessage(Component.text("  Reward: 8-15 Diamonds + 2 Soul Fragments", NamedTextColor.GOLD));
            }
            case 1 -> {
                // Stage 2 rewards: better materials + heart crystal
                player.getInventory().addItem(new ItemStack(Material.DIAMOND, 16 + random.nextInt(16)));
                player.getInventory().addItem(pum.createHeartCrystal());
                for (int i = 0; i < 3; i++) player.getInventory().addItem(pum.createSoulFragment());
                player.sendMessage(Component.text("  Reward: 16-31 Diamonds + Heart Crystal + 3 Soul Fragments", NamedTextColor.GOLD));
            }
        }
    }

    private void giveFinalReward(Player player, QuestLine line) {
        PowerUpManager pum = plugin.getPowerUpManager();
        LegendaryWeaponManager wm = plugin.getLegendaryWeaponManager();

        switch (line) {
            case HUNTER -> {
                dropRandomWeapon(player, LegendaryTier.EPIC);
                for (int i = 0; i < 5; i++) player.getInventory().addItem(pum.createHeartCrystal());
                player.sendMessage(Component.text("  Final Reward: EPIC Weapon + 5 Heart Crystals!", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
            }
            case MINER -> {
                dropRandomWeapon(player, LegendaryTier.MYTHIC);
                for (int i = 0; i < 10; i++) player.getInventory().addItem(pum.createSoulFragment());
                player.sendMessage(Component.text("  Final Reward: MYTHIC Weapon + 10 Soul Fragments!", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
            }
            case EXPLORER -> {
                player.getInventory().addItem(pum.createKeepInventorer());
                for (int i = 0; i < 3; i++) player.getInventory().addItem(pum.createPhoenixFeather());
                player.sendMessage(Component.text("  Final Reward: KeepInventorer + 3 Phoenix Feathers!", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
            }
            case NETHER -> {
                dropRandomWeapon(player, LegendaryTier.MYTHIC);
                player.getInventory().addItem(pum.createTitanResolve());
                player.sendMessage(Component.text("  Final Reward: MYTHIC Weapon + Titan's Resolve!", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
            }
            case DRAGON -> {
                dropRandomWeapon(player, LegendaryTier.MYTHIC);
                for (int i = 0; i < 5; i++) player.getInventory().addItem(pum.createHeartCrystal());
                player.sendMessage(Component.text("  Final Reward: MYTHIC Weapon + 5 Heart Crystals!", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
            }
            case ABYSSAL -> {
                dropRandomWeapon(player, LegendaryTier.ABYSSAL);
                player.sendMessage(Component.text("  Final Reward: ABYSSAL Weapon!", TextColor.color(170, 0, 0)).decorate(TextDecoration.BOLD));
            }
        }
    }

    private void dropRandomWeapon(Player player, LegendaryTier tier) {
        LegendaryWeapon[] pool = LegendaryWeapon.byTier(tier);
        if (pool.length == 0) return;
        LegendaryWeapon weapon = pool[random.nextInt(pool.length)];
        ItemStack item = plugin.getLegendaryWeaponManager().createWeapon(weapon);
        if (item != null) {
            player.getInventory().addItem(item);
            player.sendMessage(Component.text("  \u2694 ", NamedTextColor.GOLD)
                .append(Component.text("Received: " + weapon.getDisplayName(), tier.getColor()).decorate(TextDecoration.BOLD)));
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // QUEST PROGRESS TRACKING — Called from events
    // ══════════════════════════════════════════════════════════════════

    /**
     * Called when a player kills a hostile mob.
     * Advances: HUNTER stage 0, NETHER stage 0, DRAGON stage 0, ABYSSAL stage 1
     */
    public void onHostileMobKill(Player player, LivingEntity entity) {
        World.Environment env = entity.getWorld().getEnvironment();
        boolean isAbyss = entity.getWorld().getName().equalsIgnoreCase("world_abyss");

        // Hunter — kill hostile mobs (any)
        incrementProgress(player, QuestLine.HUNTER, 0);

        // Nether — kill Nether mobs
        if (env == World.Environment.NETHER) {
            incrementProgress(player, QuestLine.NETHER, 0);
        }

        // Dragon — kill Endermen
        if (entity instanceof Enderman) {
            incrementProgress(player, QuestLine.DRAGON, 0);
        }

        // Abyssal — kill mobs in Abyss
        if (isAbyss) {
            incrementProgress(player, QuestLine.ABYSSAL, 1);
        }
    }

    /**
     * Called when a player kills a King mob.
     */
    public void onKingMobKill(Player player) {
        incrementProgress(player, QuestLine.HUNTER, 1);
    }

    /**
     * Called when a player kills a roaming boss.
     */
    public void onRoamingBossKill(Player player, String bossType) {
        incrementProgress(player, QuestLine.HUNTER, 2);
        // Nether bosses
        if ("HELLFIRE_DRAKE".equals(bossType)) {
            incrementProgress(player, QuestLine.NETHER, 2);
        }
        // Abyssal Leviathan
        if ("ABYSSAL_LEVIATHAN".equals(bossType)) {
            incrementProgress(player, QuestLine.ABYSSAL, 2);
        }
    }

    /**
     * Called when the Ender Dragon is killed.
     */
    public void onEnderDragonKill(Player player) {
        incrementProgress(player, QuestLine.DRAGON, 1);
    }

    /**
     * Called when an End Rift event completes.
     */
    public void onEndRiftComplete(Player player) {
        incrementProgress(player, QuestLine.DRAGON, 2);
    }

    /**
     * Called when a Nether event completes near a player.
     */
    public void onNetherEventComplete(Player player) {
        incrementProgress(player, QuestLine.NETHER, 1);
    }

    /**
     * Called when a player enters a dimension.
     */
    public void onDimensionVisit(Player player, World.Environment env) {
        incrementProgress(player, QuestLine.EXPLORER, 0);
    }

    /**
     * Called when a player is near a generated structure.
     */
    public void onStructureDiscovery(Player player) {
        incrementProgress(player, QuestLine.EXPLORER, 1);
    }

    /**
     * Called when a player enters the Abyss.
     */
    public void onAbyssEntry(Player player) {
        incrementProgress(player, QuestLine.ABYSSAL, 0);
    }

    /**
     * Called when a player mines diamond ore or similar.
     */
    public void onOreMine(Player player, Material blockType) {
        if (blockType == Material.DIAMOND_ORE || blockType == Material.DEEPSLATE_DIAMOND_ORE) {
            incrementProgress(player, QuestLine.MINER, 0);
        }
        if (blockType == Material.ANCIENT_DEBRIS) {
            incrementProgress(player, QuestLine.MINER, 1);
        }
        if (blockType == Material.DEEPSLATE_EMERALD_ORE || blockType == Material.EMERALD_ORE) {
            incrementProgress(player, QuestLine.MINER, 2);
        }
    }

    /**
     * Called periodically to track distance walked.
     */
    public void onPlayerMove(Player player, double distance) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        int stage = pdc.getOrDefault(KEY_EXPLORER_STAGE, PersistentDataType.INTEGER, 0);
        if (stage == 2) {
            int progress = pdc.getOrDefault(KEY_EXPLORER_PROGRESS, PersistentDataType.INTEGER, 0);
            int newProgress = progress + (int) distance;
            pdc.set(KEY_EXPLORER_PROGRESS, PersistentDataType.INTEGER, newProgress);
        }
    }

    private void incrementProgress(Player player, QuestLine line, int forStage) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        NamespacedKey stageKey = getStageKey(line);
        NamespacedKey progressKey = getProgressKey(line);
        int currentStage = pdc.getOrDefault(stageKey, PersistentDataType.INTEGER, -1);
        if (currentStage == -1) return; // Quest not started
        if (currentStage != forStage) return; // Wrong stage
        int progress = pdc.getOrDefault(progressKey, PersistentDataType.INTEGER, 0);
        progress++;
        pdc.set(progressKey, PersistentDataType.INTEGER, progress);
        int required = line.stageRequirements[forStage];
        if (progress == required) {
            player.sendMessage(Component.text("\u2605 ", NamedTextColor.GOLD)
                .append(Component.text(line.displayName + " — Stage " + (forStage + 1) + " Complete!", line.color))
                .append(Component.text(" Talk to " + line.npcName + " to claim rewards.", NamedTextColor.GRAY)));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
        } else if (progress % Math.max(1, required / 4) == 0) {
            // Progress milestone notification (every 25%)
            player.sendMessage(Component.text("\u2605 ", NamedTextColor.GOLD)
                .append(Component.text(line.displayName, line.color))
                .append(Component.text(" — " + progress + "/" + required, NamedTextColor.GRAY)));
        }
    }

    private NamespacedKey getStageKey(QuestLine line) {
        return switch (line) {
            case HUNTER -> KEY_HUNTER_STAGE;
            case MINER -> KEY_MINER_STAGE;
            case EXPLORER -> KEY_EXPLORER_STAGE;
            case NETHER -> KEY_NETHER_STAGE;
            case DRAGON -> KEY_DRAGON_STAGE;
            case ABYSSAL -> KEY_ABYSSAL_STAGE;
        };
    }

    private NamespacedKey getProgressKey(QuestLine line) {
        return switch (line) {
            case HUNTER -> KEY_HUNTER_PROGRESS;
            case MINER -> KEY_MINER_PROGRESS;
            case EXPLORER -> KEY_EXPLORER_PROGRESS;
            case NETHER -> KEY_NETHER_PROGRESS;
            case DRAGON -> KEY_DRAGON_PROGRESS;
            case ABYSSAL -> KEY_ABYSSAL_PROGRESS;
        };
    }

    /**
     * Show all quest progress for a player. Called from /jglims quests.
     */
    public void showQuestProgress(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        player.sendMessage(Component.text("=== Quest Progress ===", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        for (QuestLine line : QuestLine.values()) {
            NamespacedKey stageKey = getStageKey(line);
            NamespacedKey progressKey = getProgressKey(line);
            int stage = pdc.getOrDefault(stageKey, PersistentDataType.INTEGER, -1);
            int progress = pdc.getOrDefault(progressKey, PersistentDataType.INTEGER, 0);
            if (stage == -1) {
                player.sendMessage(Component.text("  " + line.displayName + ": ", line.color)
                    .append(Component.text("Not started", NamedTextColor.DARK_GRAY)));
            } else if (stage >= 3) {
                player.sendMessage(Component.text("  " + line.displayName + ": ", line.color)
                    .append(Component.text("COMPLETED!", NamedTextColor.GREEN).decorate(TextDecoration.BOLD)));
            } else {
                int required = line.stageRequirements[stage];
                player.sendMessage(Component.text("  " + line.displayName + ": ", line.color)
                    .append(Component.text("Stage " + (stage + 1) + " — " + progress + "/" + required, NamedTextColor.AQUA))
                    .append(Component.text(" (" + line.stageDescriptions[stage] + ")", NamedTextColor.GRAY)));
            }
        }
    }

    public NamespacedKey getKeyQuestNpc() { return KEY_QUEST_NPC; }
}