package com.jglims.plugin.utility;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Material;
import org.bukkit.entity.ElderGuardian;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Guardian;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.config.ConfigManager;

public class DropRateListener implements Listener {

    private final JGlimsPlugin plugin;
    private final ConfigManager config;

    public DropRateListener(JGlimsPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!config.isDropRateBoosterEnabled()) return;

        // --- Guardian: boosted Prismarine Shard drops ---
        if (event.getEntity() instanceof Guardian && !(event.getEntity() instanceof ElderGuardian)) {
            boostDrop(event.getDrops(), Material.PRISMARINE_SHARD,
                config.getGuardianPrismarineMin(), config.getGuardianPrismarineMax());
        }

        // --- Elder Guardian: boosted Prismarine Shard drops ---
        if (event.getEntity() instanceof ElderGuardian) {
            boostDrop(event.getDrops(), Material.PRISMARINE_SHARD,
                config.getElderGuardianPrismarineMin(), config.getElderGuardianPrismarineMax());
        }

        // --- Ghast: boosted Ghast Tear drops ---
        if (event.getEntity() instanceof Ghast) {
            boostDrop(event.getDrops(), Material.GHAST_TEAR,
                config.getGhastTearMin(), config.getGhastTearMax());
        }
    }

    /**
     * Ensures the drop list contains at least min and at most max of the specified material.
     * Removes existing drops of that material and replaces with the boosted amount.
     */
    private void boostDrop(List<ItemStack> drops, Material material, int min, int max) {
        // Remove existing drops of this material
        Iterator<ItemStack> it = drops.iterator();
        while (it.hasNext()) {
            ItemStack item = it.next();
            if (item != null && item.getType() == material) {
                it.remove();
            }
        }

        // Add boosted amount
        int amount = ThreadLocalRandom.current().nextInt(min, max + 1);
        if (amount > 0) {
            drops.add(new ItemStack(material, amount));
        }
    }
}
