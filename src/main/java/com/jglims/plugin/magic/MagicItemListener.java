package com.jglims.plugin.magic;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomBossEntity;
import com.jglims.plugin.custommobs.CustomMobEntity;
import com.jglims.plugin.custommobs.CustomMobManager;
import com.jglims.plugin.custommobs.MobCategory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Handles all magic item interactions. Currently implements the Wand of Wands
 * with its three spells: Stupefy, Wingardium Leviosa, and Avada Kedavra.
 */
public class MagicItemListener implements Listener {

    private final JGlimsPlugin plugin;
    private final MagicItemManager magicManager;

    private final NamespacedKey keySpellType;

    private static final String SPELL_STUPEFY = "stupefy";
    private static final String SPELL_LEVIOSA = "leviosa";
    private static final String SPELL_AVADA = "avada_kedavra";

    public MagicItemListener(JGlimsPlugin plugin, MagicItemManager magicManager) {
        this.plugin = plugin;
        this.magicManager = magicManager;
        this.keySpellType = new NamespacedKey(plugin, "spell_type");
    }

    // ── Wand Interaction ───────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!magicManager.isWandOfWands(item)) return;

        Action action = event.getAction();

        // Left click → Stupefy
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
            castStudefy(player);
            return;
        }

        // Right click
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            if (player.isSneaking()) {
                castAvadaKedavra(player);
            } else {
                castLeviosa(player);
            }
        }
    }

    // ── Stupefy (Left Click) ───────────────────────────────────────────

    /**
     * Fires a red projectile dealing 8 damage. 1-second cooldown.
     */
    private void castStudefy(Player player) {
        if (!magicManager.isReady(player.getUniqueId(), SPELL_STUPEFY)) {
            double remaining = magicManager.getRemainingCooldown(player.getUniqueId(), SPELL_STUPEFY);
            player.sendActionBar(Component.text(String.format("Stupefy: %.1fs", remaining),
                    NamedTextColor.RED));
            return;
        }

        magicManager.setCooldown(player.getUniqueId(), SPELL_STUPEFY, 1.0);

        Snowball projectile = player.launchProjectile(Snowball.class);
        projectile.setVelocity(player.getLocation().getDirection().multiply(2.5));
        projectile.getPersistentDataContainer().set(keySpellType, PersistentDataType.STRING, SPELL_STUPEFY);

        // Red particle trail
        trailProjectile(projectile, Particle.DUST, new Particle.DustOptions(Color.RED, 1.2f));

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.5f);
    }

    // ── Wingardium Leviosa (Right Click) ───────────────────────────────

    /**
     * Fires a golden projectile that levitates the target for 6 seconds.
     * Deals 4 damage. 10-second cooldown.
     */
    private void castLeviosa(Player player) {
        if (!magicManager.isReady(player.getUniqueId(), SPELL_LEVIOSA)) {
            double remaining = magicManager.getRemainingCooldown(player.getUniqueId(), SPELL_LEVIOSA);
            player.sendActionBar(Component.text(String.format("Leviosa: %.1fs", remaining),
                    NamedTextColor.GOLD));
            return;
        }

        magicManager.setCooldown(player.getUniqueId(), SPELL_LEVIOSA, 10.0);

        Snowball projectile = player.launchProjectile(Snowball.class);
        projectile.setVelocity(player.getLocation().getDirection().multiply(2.0));
        projectile.getPersistentDataContainer().set(keySpellType, PersistentDataType.STRING, SPELL_LEVIOSA);

        trailProjectile(projectile, Particle.DUST, new Particle.DustOptions(Color.YELLOW, 1.0f));

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.2f);
    }

    // ── Avada Kedavra (Crouch + Right Click) ───────────────────────────

    /**
     * Fires a green death bolt. Damage scaling based on target type and HP.
     * 30-second cooldown.
     */
    private void castAvadaKedavra(Player player) {
        if (!magicManager.isReady(player.getUniqueId(), SPELL_AVADA)) {
            double remaining = magicManager.getRemainingCooldown(player.getUniqueId(), SPELL_AVADA);
            player.sendActionBar(Component.text(String.format("Avada Kedavra: %.1fs", remaining),
                    NamedTextColor.GREEN));
            return;
        }

        magicManager.setCooldown(player.getUniqueId(), SPELL_AVADA, 30.0);

        Snowball projectile = player.launchProjectile(Snowball.class);
        projectile.setVelocity(player.getLocation().getDirection().multiply(3.0));
        projectile.getPersistentDataContainer().set(keySpellType, PersistentDataType.STRING, SPELL_AVADA);

        trailProjectile(projectile, Particle.DUST, new Particle.DustOptions(Color.fromRGB(0, 200, 0), 1.5f));

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1.0f, 0.5f);
    }

    // ── Projectile Hit ─────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball snowball)) return;
        if (!snowball.getPersistentDataContainer().has(keySpellType)) return;
        if (!(snowball.getShooter() instanceof Player caster)) return;

        String spell = snowball.getPersistentDataContainer().get(keySpellType, PersistentDataType.STRING);
        Entity hitEntity = event.getHitEntity();

        if (hitEntity instanceof LivingEntity target) {
            switch (spell) {
                case SPELL_STUPEFY -> handleStupefyHit(caster, target, snowball);
                case SPELL_LEVIOSA -> handleLeviosaHit(caster, target, snowball);
                case SPELL_AVADA -> handleAvadaHit(caster, target, snowball);
            }
        }

        // Impact particles at hit location
        Location impactLoc = hitEntity != null ? hitEntity.getLocation() : snowball.getLocation();
        switch (spell) {
            case SPELL_STUPEFY -> impactLoc.getWorld().spawnParticle(Particle.DUST,
                    impactLoc, 15, 0.3, 0.3, 0.3, 0,
                    new Particle.DustOptions(Color.RED, 1.5f));
            case SPELL_LEVIOSA -> impactLoc.getWorld().spawnParticle(Particle.DUST,
                    impactLoc, 15, 0.3, 0.3, 0.3, 0,
                    new Particle.DustOptions(Color.YELLOW, 1.5f));
            case SPELL_AVADA -> {
                impactLoc.getWorld().spawnParticle(Particle.DUST,
                        impactLoc, 30, 0.5, 0.5, 0.5, 0,
                        new Particle.DustOptions(Color.fromRGB(0, 200, 0), 2.0f));
                impactLoc.getWorld().playSound(impactLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.5f);
            }
        }
    }

    private void handleStupefyHit(Player caster, LivingEntity target, Snowball snowball) {
        target.damage(8.0, caster);
    }

    private void handleLeviosaHit(Player caster, LivingEntity target, Snowball snowball) {
        target.damage(4.0, caster);
        target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 120, 0, false, true));
    }

    private void handleAvadaHit(Player caster, LivingEntity target, Snowball snowball) {
        boolean isBoss = false;
        double maxHp = target.getMaxHealth();
        double currentHp = target.getHealth();
        double hpPercent = currentHp / maxHp;

        // Check if target is a custom boss
        CustomMobManager mobManager = plugin.getCustomMobManager();
        if (mobManager != null) {
            CustomMobEntity mob = mobManager.getByEntity(target);
            if (mob != null) {
                MobCategory cat = mob.getMobType().getCategory();
                if (cat == MobCategory.BOSS || cat == MobCategory.WORLD_BOSS
                        || cat == MobCategory.EVENT_BOSS) {
                    isBoss = true;
                }
            }
        }

        if (isBoss) {
            // Bosses: exactly 10% max HP
            target.damage(maxHp * 0.10, caster);
        } else if (target instanceof Player playerTarget) {
            // Players below 30%: instant kill; above: 40% max HP
            if (hpPercent <= 0.30) {
                playerTarget.setHealth(0);
            } else {
                target.damage(maxHp * 0.40, caster);
            }
        } else {
            // Non-boss mobs below 50%: instant kill; above: 60% max HP
            if (hpPercent <= 0.50) {
                target.setHealth(0);
            } else {
                target.damage(maxHp * 0.60, caster);
            }
        }
    }

    // ── Utility ────────────────────────────────────────────────────────

    /**
     * Spawns a particle trail behind a projectile until it hits something or expires.
     */
    private void trailProjectile(Projectile projectile, Particle particle, Particle.DustOptions dust) {
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (projectile.isDead() || !projectile.isValid() || ticks > 60) {
                    cancel();
                    return;
                }
                projectile.getWorld().spawnParticle(particle, projectile.getLocation(),
                        3, 0.05, 0.05, 0.05, 0, dust);
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
