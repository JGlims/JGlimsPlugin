package com.jglims.plugin.custommobs;

import com.jglims.plugin.JGlimsPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

/**
 * Global event listener that routes Bukkit events to the correct
 * {@link CustomMobEntity} instance. Handles damage, death, interaction,
 * chunk unload, and world unload for all custom mobs.
 */
public class CustomMobListener implements Listener {

    private final JGlimsPlugin plugin;
    private final CustomMobManager manager;

    public CustomMobListener(JGlimsPlugin plugin, CustomMobManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    // ── Damage Routing ─────────────────────────────────────────────────

    /**
     * Routes damage from players to custom mobs through the custom damage system.
     * Cancels vanilla damage and applies it via {@link CustomMobEntity#damage(double, Player)}.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damaged = event.getEntity();
        CustomMobEntity mob = manager.getByEntity(damaged);
        if (mob == null || !mob.isAlive()) return;

        // Route damage through custom system
        Player attacker = null;
        if (event.getDamager() instanceof Player p) {
            attacker = p;
        }

        // Cancel vanilla damage — we handle it ourselves
        event.setCancelled(true);
        mob.damage(event.getFinalDamage(), attacker);
    }

    /**
     * Prevents custom mobs from taking environmental damage (unless specifically allowed).
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) return; // Handled above
        Entity damaged = event.getEntity();
        CustomMobEntity mob = manager.getByEntity(damaged);
        if (mob == null || !mob.isAlive()) return;

        // NPCs are always invulnerable
        if (mob.getMobType().getCategory() == MobCategory.NPC) {
            event.setCancelled(true);
            return;
        }

        // Allow fall damage for non-boss mobs, cancel fire/lava/void for bosses
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (mob instanceof CustomBossEntity) {
            if (cause == EntityDamageEvent.DamageCause.FALL
                    || cause == EntityDamageEvent.DamageCause.FIRE
                    || cause == EntityDamageEvent.DamageCause.FIRE_TICK
                    || cause == EntityDamageEvent.DamageCause.LAVA
                    || cause == EntityDamageEvent.DamageCause.VOID
                    || cause == EntityDamageEvent.DamageCause.DROWNING
                    || cause == EntityDamageEvent.DamageCause.SUFFOCATION) {
                event.setCancelled(true);
            }
        }
    }

    // ── Death ──────────────────────────────────────────────────────────

    /**
     * Intercepts vanilla death events for custom mobs. The custom mob system
     * handles death internally, so we cancel the vanilla death to prevent
     * double drops and vanilla death animations.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        CustomMobEntity mob = manager.getByEntity(entity);
        if (mob == null) return;

        // Clear vanilla drops — custom system handles drops
        event.getDrops().clear();
        event.setDroppedExp(0);

        // If the mob is still alive in our system, trigger death
        if (mob.isAlive()) {
            Player killer = entity.getKiller();
            mob.die(killer);
        }
    }

    // ── Interaction ────────────────────────────────────────────────────

    /**
     * Routes right-click interactions to the appropriate custom mob.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity clicked = event.getRightClicked();
        CustomMobEntity mob = manager.getByEntity(clicked);
        if (mob == null || !mob.isAlive()) return;

        event.setCancelled(true);
        mob.onInteract(event.getPlayer());
    }

    // ── Chunk / World Unload ───────────────────────────────────────────

    /**
     * Handles chunk unloads — despawns custom mob models to prevent ghost entities.
     * The mob data is preserved for respawning when the chunk reloads.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            CustomMobEntity mob = manager.getByEntity(entity);
            if (mob != null && mob.isAlive()) {
                // Close the tracker to prevent ghost models
                if (mob.getTracker() != null) {
                    try { mob.getTracker().close(); } catch (Exception ignored) {}
                }
            }
        }
    }

    /**
     * Handles world unloads — cleanly despawns all custom mobs in the world.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldUnload(WorldUnloadEvent event) {
        manager.despawnInWorld(event.getWorld().getName());
    }
}
