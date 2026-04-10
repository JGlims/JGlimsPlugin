package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobType;
import com.jglims.plugin.custommobs.CustomNpcEntity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

/**
 * Glare NPC - Cave-dwelling trader found below Y=40.
 * Buys and sells ores. Buys iron/gold/copper/redstone/lapis at markup.
 * Sells diamonds/emeralds at reasonable prices.
 * Also sells torches, scaffolding, ladders.
 * On trade complete: marks nearby ores with particle indicators for 30s.
 */
public class GlareNpc extends CustomNpcEntity {

    private static final TextColor MOSS_GREEN = TextColor.color(100, 160, 80);
    private static final TextColor GLOW_YELLOW = TextColor.color(220, 255, 100);

    public GlareNpc(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.GLARE_NPC);
        this.tradeRefreshDays = 3;

        dialogueLines.add(Component.text("*rustling leaves* ... You found me!", MOSS_GREEN));
        dialogueLines.add(Component.text("I'm a Glare. I live in the dark, but I hate it.", NamedTextColor.WHITE));
        dialogueLines.add(Component.text("I know where every ore is hidden in these caves.", MOSS_GREEN));
        dialogueLines.add(Component.text("Trade with me and I'll show you where the good stuff is!", NamedTextColor.WHITE));
        dialogueLines.add(Component.text("Bring me your ores — I'll pay well. And I sell light!", GLOW_YELLOW)
                .decorate(TextDecoration.BOLD));
    }

    @Override
    protected void initializeTrades() {
        // ── Sell: Diamonds and Emeralds (reasonable prices) ──
        addTrade(new ItemStack(Material.IRON_INGOT, 32), new ItemStack(Material.DIAMOND, 2), 5);
        addTrade(new ItemStack(Material.GOLD_INGOT, 16), new ItemStack(Material.DIAMOND, 1), 5);
        addTrade(new ItemStack(Material.COPPER_INGOT, 64), new ItemStack(Material.EMERALD, 4), 5);

        // ── Buy: Ores at markup ──
        addTrade(new ItemStack(Material.RAW_IRON, 16), new ItemStack(Material.EMERALD, 6), 10);
        addTrade(new ItemStack(Material.RAW_GOLD, 8), new ItemStack(Material.EMERALD, 8), 8);
        addTrade(new ItemStack(Material.RAW_COPPER, 32), new ItemStack(Material.EMERALD, 4), 10);
        addTrade(new ItemStack(Material.REDSTONE, 32), new ItemStack(Material.EMERALD, 6), 10);
        addTrade(new ItemStack(Material.LAPIS_LAZULI, 16), new ItemStack(Material.EMERALD, 8), 8);
        addTrade(new ItemStack(Material.DIAMOND, 1), new ItemStack(Material.EMERALD, 8), 5);

        // ── Sell: Cave essentials ──
        addTrade(new ItemStack(Material.EMERALD, 1), new ItemStack(Material.TORCH, 64), 15);
        addTrade(new ItemStack(Material.EMERALD, 2), new ItemStack(Material.LANTERN, 16), 10);
        addTrade(new ItemStack(Material.EMERALD, 2), new ItemStack(Material.SCAFFOLDING, 32), 10);
        addTrade(new ItemStack(Material.EMERALD, 2), new ItemStack(Material.LADDER, 32), 10);
        addTrade(new ItemStack(Material.EMERALD, 1), new ItemStack(Material.GLOW_BERRIES, 16), 10);
        addTrade(new ItemStack(Material.EMERALD, 3), new ItemStack(Material.SPYGLASS, 1), 3);

        // ── Sell: Mining tools ──
        addTrade(new ItemStack(Material.EMERALD, 4), new ItemStack(Material.IRON_PICKAXE, 1), 5);
        addTrade(new ItemStack(Material.EMERALD, 12), new ItemStack(Material.DIAMOND_PICKAXE, 1), 2);

        // ── Sell: Useful cave blocks ──
        addTrade(new ItemStack(Material.EMERALD, 2), new ItemStack(Material.RAIL, 32), 8);
        addTrade(new ItemStack(Material.EMERALD, 4), new ItemStack(Material.POWERED_RAIL, 8), 5);
        addTrade(new ItemStack(Material.EMERALD, 3), new ItemStack(Material.TNT, 8), 5);
    }

    @Override
    public void onInteract(Player player) {
        super.onInteract(player);

        // After any interaction (including opening trades), highlight nearby ores
        if (dialogueShown.contains(player.getUniqueId())) {
            highlightNearbyOres(player);
        }
    }

    /**
     * Scans nearby blocks and creates particle indicators on ores for 30 seconds.
     */
    private void highlightNearbyOres(Player player) {
        if (hitboxEntity == null) return;

        Location center = hitboxEntity.getLocation();
        Set<Location> oreLocations = new HashSet<>();

        // Scan in a 12-block radius
        int radius = 12;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = center.getWorld().getBlockAt(
                            center.getBlockX() + x,
                            center.getBlockY() + y,
                            center.getBlockZ() + z
                    );
                    if (isOre(block.getType())) {
                        oreLocations.add(block.getLocation().add(0.5, 0.5, 0.5));
                    }
                }
            }
        }

        if (oreLocations.isEmpty()) return;

        player.sendMessage(Component.text("The Glare senses " + oreLocations.size() + " ore veins nearby!", GLOW_YELLOW));
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.5f);

        // Show particles for 30 seconds (600 ticks)
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 600 || !player.isOnline()) {
                    cancel();
                    return;
                }
                for (Location loc : oreLocations) {
                    if (loc.distanceSquared(player.getLocation()) < 400) { // within 20 blocks
                        player.spawnParticle(Particle.HAPPY_VILLAGER, loc, 2, 0.2, 0.2, 0.2);
                    }
                }
                ticks += 20;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private boolean isOre(Material type) {
        return switch (type) {
            case IRON_ORE, DEEPSLATE_IRON_ORE,
                 GOLD_ORE, DEEPSLATE_GOLD_ORE,
                 COPPER_ORE, DEEPSLATE_COPPER_ORE,
                 DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE,
                 EMERALD_ORE, DEEPSLATE_EMERALD_ORE,
                 LAPIS_ORE, DEEPSLATE_LAPIS_ORE,
                 REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE,
                 ANCIENT_DEBRIS -> true;
            default -> false;
        };
    }
}
