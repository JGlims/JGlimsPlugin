package com.jglims.plugin.abyss;

import com.jglims.plugin.JGlimsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import java.time.Duration;
import java.util.*;

public class AbyssDimensionManager implements Listener {
    private static final String ABYSS_WORLD_NAME = "world_abyss";
    private final JGlimsPlugin plugin;
    private World abyssWorld;
    private boolean citadelBuilt = false;
    // Active portal: store frame interior block locations + world
    private final Map<UUID, Set<String>> activePortalBlocks = new HashMap<>();
    private final Set<UUID> teleportCooldown = new HashSet<>();

    public AbyssDimensionManager(JGlimsPlugin plugin) { this.plugin = plugin; }

    public void initialize() {
        abyssWorld = Bukkit.getWorld(ABYSS_WORLD_NAME);
        boolean freshWorld = (abyssWorld == null);
        if (freshWorld) {
            plugin.getLogger().info("[Abyss] Creating dimension: " + ABYSS_WORLD_NAME);
            WorldCreator creator = new WorldCreator(ABYSS_WORLD_NAME);
            creator.environment(World.Environment.THE_END);
            creator.generator(new AbyssChunkGenerator());
            abyssWorld = creator.createWorld();
        } else {
            plugin.getLogger().info("[Abyss] Loaded existing dimension: " + ABYSS_WORLD_NAME);
        }
        if (abyssWorld != null) {
            abyssWorld.setDifficulty(Difficulty.HARD);
            abyssWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            abyssWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            abyssWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            abyssWorld.setGameRule(GameRule.KEEP_INVENTORY, true);
            abyssWorld.setGameRule(GameRule.MOB_GRIEFING, false);
            abyssWorld.setGameRule(GameRule.DO_FIRE_TICK, false);
            abyssWorld.setTime(18000);
            new BukkitRunnable() {
                @Override public void run() {
                    if (abyssWorld == null) return;
                    for (EnderDragon d : abyssWorld.getEntitiesByClass(EnderDragon.class)) {
                        if (d.getCustomName() == null) d.remove();
                    }
                }
            }.runTaskTimer(plugin, 100L, 100L);
            if (freshWorld) {
                plugin.getLogger().info("[Abyss] Building Abyssal Citadel on fresh world...");
                new AbyssCitadelBuilder(plugin, abyssWorld).build();
                citadelBuilt = true;
                plugin.getLogger().info("[Abyss] Citadel construction complete.");
            } else { citadelBuilt = true; }
        }
    }

    public ItemStack createAbyssalKey() {
        ItemStack key = new ItemStack(Material.ECHO_SHARD, 1);
        ItemMeta meta = key.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Abyssal Key", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("A key forged in the void.", NamedTextColor.GRAY));
            lore.add(Component.text("Use on a Purpur portal frame", NamedTextColor.GRAY));
            lore.add(Component.text("to open a gateway to the Abyss.", NamedTextColor.DARK_GRAY));
            lore.add(Component.empty());
            lore.add(Component.text("Abyssal Key", NamedTextColor.DARK_PURPLE));
            meta.lore(lore);
            meta.setCustomModelData(9001);
            key.setItemMeta(meta);
        }
        return key;
    }

    private boolean isAbyssalKey(ItemStack item) {
        if (item == null || item.getType() != Material.ECHO_SHARD) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == 9001;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block clicked = event.getClickedBlock();
        if (clicked == null || clicked.getType() != Material.PURPUR_BLOCK) return;
        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!isAbyssalKey(hand)) return;
        Location frameLoc = findPortalFrame(clicked.getLocation());
        if (frameLoc == null) {
            player.sendMessage(Component.text("The key resonates... but no valid portal frame is found.", NamedTextColor.DARK_PURPLE));
            return;
        }
        event.setCancelled(true);
        if (hand.getAmount() > 1) hand.setAmount(hand.getAmount() - 1);
        else player.getInventory().setItemInMainHand(null);
        activatePortal(frameLoc, player);
    }

    private Location findPortalFrame(Location loc) {
        World w = loc.getWorld(); int cx = loc.getBlockX(), cy = loc.getBlockY(), cz = loc.getBlockZ();
        for (int ox = -4; ox <= 1; ox++) for (int oy = -5; oy <= 0; oy++) {
            Location b = new Location(w, cx+ox, cy+oy, cz);
            if (checkFrame(b, true)) return b;
        }
        for (int oz = -4; oz <= 1; oz++) for (int oy = -5; oy <= 0; oy++) {
            Location b = new Location(w, cx, cy+oy, cz+oz);
            if (checkFrame(b, false)) return b;
        }
        return null;
    }

    private boolean checkFrame(Location base, boolean xAxis) {
        World w = base.getWorld(); int bx = base.getBlockX(), by = base.getBlockY(), bz = base.getBlockZ();
        if (xAxis) {
            for (int i = 0; i < 4; i++) { if (w.getBlockAt(bx+i,by,bz).getType() != Material.PURPUR_BLOCK) return false; if (w.getBlockAt(bx+i,by+4,bz).getType() != Material.PURPUR_BLOCK) return false; }
            for (int i = 0; i < 5; i++) { if (w.getBlockAt(bx,by+i,bz).getType() != Material.PURPUR_BLOCK) return false; if (w.getBlockAt(bx+3,by+i,bz).getType() != Material.PURPUR_BLOCK) return false; }
            for (int x = 1; x <= 2; x++) for (int y = 1; y <= 3; y++) { Material m = w.getBlockAt(bx+x,by+y,bz).getType(); if (!m.isAir()) return false; }
        } else {
            for (int i = 0; i < 4; i++) { if (w.getBlockAt(bx,by,bz+i).getType() != Material.PURPUR_BLOCK) return false; if (w.getBlockAt(bx,by+4,bz+i).getType() != Material.PURPUR_BLOCK) return false; }
            for (int i = 0; i < 5; i++) { if (w.getBlockAt(bx,by+i,bz).getType() != Material.PURPUR_BLOCK) return false; if (w.getBlockAt(bx,by+i,bz+3).getType() != Material.PURPUR_BLOCK) return false; }
            for (int z = 1; z <= 2; z++) for (int y = 1; y <= 3; y++) { Material m = w.getBlockAt(bx,by+y,bz+z).getType(); if (!m.isAir()) return false; }
        }
        return true;
    }

    private void activatePortal(Location frameLoc, Player player) {
        World w = frameLoc.getWorld(); int bx = frameLoc.getBlockX(), by = frameLoc.getBlockY(), bz = frameLoc.getBlockZ();
        boolean xAxis = checkFrame(frameLoc, true);
        Set<String> blockKeys = new HashSet<>();
        // Fill interior with END_GATEWAY blocks (visual, no vanilla teleport behavior for walk-through)
        if (xAxis) {
            for (int x = 1; x <= 2; x++) for (int y = 1; y <= 3; y++) {
                Block b = w.getBlockAt(bx+x, by+y, bz);
                b.setType(Material.END_GATEWAY, false);
                blockKeys.add((bx+x)+","+(by+y)+","+bz);
            }
        } else {
            for (int z = 1; z <= 2; z++) for (int y = 1; y <= 3; y++) {
                Block b = w.getBlockAt(bx, by+y, bz+z);
                b.setType(Material.END_GATEWAY, false);
                blockKeys.add(bx+","+(by+y)+","+(bz+z));
            }
        }
        UUID wid = w.getUID();
        activePortalBlocks.put(wid, blockKeys);
        Location center = frameLoc.clone().add(xAxis ? 1.5 : 0.5, 2.5, xAxis ? 0.5 : 1.5);
        w.playSound(center, Sound.BLOCK_END_PORTAL_SPAWN, 1.5f, 0.5f);
        w.spawnParticle(Particle.PORTAL, center, 200, 1, 2, 1, 0.5);
        w.spawnParticle(Particle.REVERSE_PORTAL, center, 100, 1, 2, 1, 0.3);
        player.sendMessage(Component.text("The Abyssal Gateway opens...", NamedTextColor.DARK_PURPLE, TextDecoration.ITALIC));
        // Particle loop for 30s
        final Set<String> keys = blockKeys;
        new BukkitRunnable() { int ticks = 0;
            @Override public void run() {
                ticks++;
                if (ticks > 600) { // 30 seconds
                    for (String k : keys) { String[] p = k.split(","); Block b = w.getBlockAt(Integer.parseInt(p[0]),Integer.parseInt(p[1]),Integer.parseInt(p[2])); if (b.getType() == Material.END_GATEWAY) b.setType(Material.AIR); }
                    activePortalBlocks.remove(wid);
                    w.playSound(center, Sound.BLOCK_PORTAL_TRAVEL, 0.8f, 1.5f);
                    cancel(); return;
                }
                if (ticks % 5 == 0) { w.spawnParticle(Particle.PORTAL, center, 30, 0.5, 1.5, 0.5, 0.3); w.spawnParticle(Particle.REVERSE_PORTAL, center, 15, 0.5, 1.5, 0.5, 0.2); }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;
        Player player = event.getPlayer();
        if (player.getWorld().getName().equals(ABYSS_WORLD_NAME)) return;
        if (teleportCooldown.contains(player.getUniqueId())) return;
        Location to = event.getTo();
        String key = to.getBlockX()+","+to.getBlockY()+","+to.getBlockZ();
        UUID wid = player.getWorld().getUID();
        Set<String> portals = activePortalBlocks.get(wid);
        if (portals == null || !portals.contains(key)) return;
        teleportCooldown.add(player.getUniqueId());
        teleportToAbyss(player);
        new BukkitRunnable() { @Override public void run() { teleportCooldown.remove(player.getUniqueId()); } }.runTaskLater(plugin, 100L);
    }

    public void teleportToAbyss(Player player) {
        if (abyssWorld == null) { player.sendMessage(Component.text("The Abyss is not yet ready.", NamedTextColor.RED)); return; }
        int safeY = findSafeY(abyssWorld, 0, 85);
        Location dest = new Location(abyssWorld, 0.5, safeY, 85.5, 0f, 0f);
        player.teleport(dest);
        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(1000));
        player.showTitle(Title.title(Component.text("THE ABYSS", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD), Component.text("Abandon all hope...", NamedTextColor.GRAY, TextDecoration.ITALIC), times));
        player.playSound(dest, Sound.AMBIENT_CAVE, 1.0f, 0.5f);
        player.playSound(dest, Sound.ENTITY_ENDER_DRAGON_AMBIENT, 0.5f, 0.5f);
        new BukkitRunnable() { @Override public void run() { if (abyssWorld == null) return; for (EnderDragon d : abyssWorld.getEntitiesByClass(EnderDragon.class)) if (d.getCustomName() == null) d.remove(); } }.runTaskLater(plugin, 40L);
    }

    public int findSafeY(World world, int x, int z) {
        for (int y = 120; y > 1; y--) { Block b = world.getBlockAt(x,y,z); Block a1 = world.getBlockAt(x,y+1,z); Block a2 = world.getBlockAt(x,y+2,z); if (b.getType().isSolid() && !a1.getType().isSolid() && !a2.getType().isSolid()) return y+1; }
        return 60;
    }
    public World getAbyssWorld() { return abyssWorld; }
    public JGlimsPlugin getPlugin() { return plugin; }
    public boolean isCitadelBuilt() { return citadelBuilt; }
}