package com.jglims.plugin.guilds;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.config.ConfigManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class GuildManager {

    private final JGlimsPlugin plugin;
    private final ConfigManager config;
    private final File guildFile;
    private YamlConfiguration guildConfig;

    // guildName -> set of member UUIDs
    private final Map<String, Set<UUID>> guilds = new HashMap<>();
    // guildName -> leader UUID
    private final Map<String, UUID> leaders = new HashMap<>();
    // player UUID -> guildName
    private final Map<UUID, String> playerGuild = new HashMap<>();
    // Pending invites: player UUID -> guildName
    private final Map<UUID, String> pendingInvites = new HashMap<>();

    public GuildManager(JGlimsPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
        this.guildFile = new File(plugin.getDataFolder(), "guilds.yml");
        loadGuilds();
    }

    // ========================================================================
    // PERSISTENCE
    // ========================================================================
    private void loadGuilds() {
        if (!guildFile.exists()) {
            guildConfig = new YamlConfiguration();
            return;
        }
        guildConfig = YamlConfiguration.loadConfiguration(guildFile);

        ConfigurationSection section = guildConfig.getConfigurationSection("guilds");
        if (section == null) return;

        for (String guildName : section.getKeys(false)) {
            ConfigurationSection gs = section.getConfigurationSection(guildName);
            if (gs == null) continue;

            UUID leader = UUID.fromString(gs.getString("leader", ""));
            leaders.put(guildName, leader);

            Set<UUID> members = new HashSet<>();
            List<String> memberList = gs.getStringList("members");
            for (String s : memberList) {
                UUID uuid = UUID.fromString(s);
                members.add(uuid);
                playerGuild.put(uuid, guildName);
            }
            guilds.put(guildName, members);
        }

        plugin.getLogger().info("Loaded " + guilds.size() + " guilds.");
    }

    private void saveGuilds() {
        guildConfig = new YamlConfiguration();
        for (Map.Entry<String, Set<UUID>> entry : guilds.entrySet()) {
            String guildName = entry.getKey();
            String path = "guilds." + guildName;
            guildConfig.set(path + ".leader", leaders.get(guildName).toString());
            List<String> memberList = new ArrayList<>();
            for (UUID uuid : entry.getValue()) {
                memberList.add(uuid.toString());
            }
            guildConfig.set(path + ".members", memberList);
        }
        try {
            guildConfig.save(guildFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save guilds.yml: " + e.getMessage());
        }
    }

    // ========================================================================
    // GUILD OPERATIONS
    // ========================================================================
    public boolean createGuild(Player player, String name) {
        if (!config.isGuildsEnabled()) {
            player.sendMessage(Component.text("Guilds are disabled.", NamedTextColor.RED));
            return false;
        }
        if (playerGuild.containsKey(player.getUniqueId())) {
            player.sendMessage(Component.text("You are already in a guild! Leave first.", NamedTextColor.RED));
            return false;
        }
        if (guilds.containsKey(name.toLowerCase())) {
            player.sendMessage(Component.text("A guild with that name already exists!", NamedTextColor.RED));
            return false;
        }
        if (name.length() < 3 || name.length() > 16) {
            player.sendMessage(Component.text("Guild name must be 3-16 characters.", NamedTextColor.RED));
            return false;
        }

        String key = name.toLowerCase();
        Set<UUID> members = new HashSet<>();
        members.add(player.getUniqueId());
        guilds.put(key, members);
        leaders.put(key, player.getUniqueId());
        playerGuild.put(player.getUniqueId(), key);

        saveGuilds();
        player.sendMessage(Component.text("Guild '" + name + "' created! You are the leader.", NamedTextColor.GREEN));
        return true;
    }

    public boolean invitePlayer(Player inviter, String targetName) {
        if (!config.isGuildsEnabled()) return false;

        String guild = playerGuild.get(inviter.getUniqueId());
        if (guild == null) {
            inviter.sendMessage(Component.text("You are not in a guild!", NamedTextColor.RED));
            return false;
        }
        if (!leaders.get(guild).equals(inviter.getUniqueId())) {
            inviter.sendMessage(Component.text("Only the guild leader can invite players!", NamedTextColor.RED));
            return false;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            inviter.sendMessage(Component.text("Player not found or not online!", NamedTextColor.RED));
            return false;
        }
        if (playerGuild.containsKey(target.getUniqueId())) {
            inviter.sendMessage(Component.text("That player is already in a guild!", NamedTextColor.RED));
            return false;
        }

        Set<UUID> members = guilds.get(guild);
        if (members.size() >= config.getGuildsMaxMembers()) {
            inviter.sendMessage(Component.text("Your guild is full! (" + config.getGuildsMaxMembers() + " max)", NamedTextColor.RED));
            return false;
        }

        pendingInvites.put(target.getUniqueId(), guild);
        inviter.sendMessage(Component.text("Invited " + target.getName() + " to your guild.", NamedTextColor.GREEN));
        target.sendMessage(Component.text("You have been invited to guild '" + guild + "'! Type /guild join to accept.", NamedTextColor.GOLD));
        return true;
    }

    public boolean joinGuild(Player player) {
        if (!config.isGuildsEnabled()) return false;

        String guild = pendingInvites.remove(player.getUniqueId());
        if (guild == null) {
            player.sendMessage(Component.text("You have no pending guild invites!", NamedTextColor.RED));
            return false;
        }
        if (playerGuild.containsKey(player.getUniqueId())) {
            player.sendMessage(Component.text("You are already in a guild!", NamedTextColor.RED));
            return false;
        }

        Set<UUID> members = guilds.get(guild);
        if (members == null) {
            player.sendMessage(Component.text("That guild no longer exists!", NamedTextColor.RED));
            return false;
        }

        members.add(player.getUniqueId());
        playerGuild.put(player.getUniqueId(), guild);
        saveGuilds();

        player.sendMessage(Component.text("You joined guild '" + guild + "'!", NamedTextColor.GREEN));

        // Notify guild members
        for (UUID uuid : members) {
            Player member = Bukkit.getPlayer(uuid);
            if (member != null && member.isOnline() && !member.equals(player)) {
                member.sendMessage(Component.text(player.getName() + " has joined the guild!", NamedTextColor.GREEN));
            }
        }
        return true;
    }

    public boolean leaveGuild(Player player) {
        if (!config.isGuildsEnabled()) return false;

        String guild = playerGuild.get(player.getUniqueId());
        if (guild == null) {
            player.sendMessage(Component.text("You are not in a guild!", NamedTextColor.RED));
            return false;
        }

        // Leader cannot leave — must disband
        if (leaders.get(guild).equals(player.getUniqueId())) {
            player.sendMessage(Component.text("You are the leader! Use /guild disband to disband.", NamedTextColor.RED));
            return false;
        }

        guilds.get(guild).remove(player.getUniqueId());
        playerGuild.remove(player.getUniqueId());
        saveGuilds();

        player.sendMessage(Component.text("You left guild '" + guild + "'.", NamedTextColor.YELLOW));
        return true;
    }

    public boolean kickPlayer(Player leader, String targetName) {
        if (!config.isGuildsEnabled()) return false;

        String guild = playerGuild.get(leader.getUniqueId());
        if (guild == null || !leaders.get(guild).equals(leader.getUniqueId())) {
            leader.sendMessage(Component.text("Only the guild leader can kick players!", NamedTextColor.RED));
            return false;
        }

        Player target = Bukkit.getPlayer(targetName);
        UUID targetUUID = target != null ? target.getUniqueId() : null;

        // Try to find by name in the guild if player is offline
        if (targetUUID == null) {
            for (UUID uuid : guilds.get(guild)) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null && p.getName().equalsIgnoreCase(targetName)) {
                    targetUUID = uuid;
                    break;
                }
            }
        }

        if (targetUUID == null || !guilds.get(guild).contains(targetUUID)) {
            leader.sendMessage(Component.text("Player not found in your guild!", NamedTextColor.RED));
            return false;
        }

        if (targetUUID.equals(leader.getUniqueId())) {
            leader.sendMessage(Component.text("You cannot kick yourself!", NamedTextColor.RED));
            return false;
        }

        guilds.get(guild).remove(targetUUID);
        playerGuild.remove(targetUUID);
        saveGuilds();

        leader.sendMessage(Component.text("Kicked " + targetName + " from the guild.", NamedTextColor.YELLOW));
        if (target != null && target.isOnline()) {
            target.sendMessage(Component.text("You have been kicked from guild '" + guild + "'.", NamedTextColor.RED));
        }
        return true;
    }

    public boolean disbandGuild(Player leader) {
        if (!config.isGuildsEnabled()) return false;

        String guild = playerGuild.get(leader.getUniqueId());
        if (guild == null || !leaders.get(guild).equals(leader.getUniqueId())) {
            leader.sendMessage(Component.text("Only the guild leader can disband!", NamedTextColor.RED));
            return false;
        }

        Set<UUID> members = guilds.remove(guild);
        leaders.remove(guild);
        if (members != null) {
            for (UUID uuid : members) {
                playerGuild.remove(uuid);
                Player member = Bukkit.getPlayer(uuid);
                if (member != null && member.isOnline()) {
                    member.sendMessage(Component.text("Guild '" + guild + "' has been disbanded.", NamedTextColor.RED));
                }
            }
        }

        saveGuilds();
        return true;
    }

    public void showGuildInfo(Player player) {
        String guild = playerGuild.get(player.getUniqueId());
        if (guild == null) {
            player.sendMessage(Component.text("You are not in a guild.", NamedTextColor.RED));
            return;
        }

        Set<UUID> members = guilds.get(guild);
        UUID leaderUUID = leaders.get(guild);

        player.sendMessage(Component.text("=== Guild: " + guild + " ===", NamedTextColor.GOLD));
        player.sendMessage(Component.text("Members (" + members.size() + "/" + config.getGuildsMaxMembers() + "):", NamedTextColor.YELLOW));

        for (UUID uuid : members) {
            String name = Bukkit.getOfflinePlayer(uuid).getName();
            if (name == null) name = uuid.toString().substring(0, 8);
            boolean isLeader = uuid.equals(leaderUUID);
            boolean online = Bukkit.getPlayer(uuid) != null;
            player.sendMessage(Component.text(
                " " + (isLeader ? "[Leader] " : "") + name + (online ? " (online)" : " (offline)"),
                online ? NamedTextColor.GREEN : NamedTextColor.GRAY));
        }
    }

    public void listGuilds(Player player) {
        if (guilds.isEmpty()) {
            player.sendMessage(Component.text("No guilds exist yet.", NamedTextColor.GRAY));
            return;
        }

        player.sendMessage(Component.text("=== Guilds ===", NamedTextColor.GOLD));
        for (Map.Entry<String, Set<UUID>> entry : guilds.entrySet()) {
            String leaderName = Bukkit.getOfflinePlayer(leaders.get(entry.getKey())).getName();
            player.sendMessage(Component.text(
                " " + entry.getKey() + " - " + entry.getValue().size() + " members (Leader: " + leaderName + ")",
                NamedTextColor.YELLOW));
        }
    }

    // ========================================================================
    // QUERY METHODS
    // ========================================================================
    public boolean areInSameGuild(UUID player1, UUID player2) {
        String guild1 = playerGuild.get(player1);
        String guild2 = playerGuild.get(player2);
        return guild1 != null && guild1.equals(guild2);
    }

    public boolean isInGuild(UUID player) {
        return playerGuild.containsKey(player);
    }

    public String getPlayerGuild(UUID player) {
        return playerGuild.get(player);
    }
}
