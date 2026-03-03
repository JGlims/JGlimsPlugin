package com.jglims.plugin.utility;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Material;
import org.bukkit.entity.Breeze;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.ElderGuardian;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.LivingEntity;
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

        LivingEntity entity = event.getEntity();
        List<ItemStack> drops = event.getDrops();

        // Guardian: boosted Prismarine Shard drops
        if (entity instanceof Guardian && !(entity instanceof ElderGuardian)) {
            boostDrop(drops, Material.PRISMARINE_SHARD,
                config.getGuardianPrismarineMin(), config.getGuardianPrismarineMax());
        }

        // Elder Guardian: boosted Prismarine Shard drops
        if (entity instanceof ElderGuardian) {
            boostDrop(drops, Material.PRISMARINE_SHARD,
                config.getElderGuardianPrismarineMin(), config.getElderGuardianPrismarineMax());
        }

        // Ghast: boosted Ghast Tear drops
        if (entity instanceof Ghast) {
            boostDrop(drops, Material.GHAST_TEAR,
                config.getGhastTearMin(), config.getGhastTearMax());
        }

        // Drowned: 35% chance to drop trident (NEW v1.1.0)
        if (entity instanceof Drowned) {
            boolean hasTrident = false;
            for (ItemStack item : drops) {
                if (item != null && item.getType() == Material.TRIDENT) {
                    hasTrident = true;
                    break;
                }
            }
            if (!hasTrident && ThreadLocalRandom.current().nextDouble() < config.getTridentDropChance()) {
                drops.add(new ItemStack(Material.TRIDENT, 1));
            }
        }

        // Breeze: boosted Wind Charge drops (NEW v1.1.0)
        if (entity instanceof Breeze) {
            boostDrop(drops, Material.WIND_CHARGE,
                config.getBreezeWindChargeMin(), config.getBreezeWindChargeMax());
        }
    }

    private void boostDrop(List<ItemStack> drops, Material material, int min, int max) {
        Iterator<ItemStack> it = drops.iterator();
        while (it.hasNext()) {
            ItemStack item = it.next();
            if (item != null && item.getType() == material) {
                it.remove();
            }
        }

        int amount = ThreadLocalRandom.current().nextInt(min, max + 1);
        if (amount > 0) {
            drops.add(new ItemStack(material, amount));
        }
    }
}
