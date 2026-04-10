package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobType;
import com.jglims.plugin.custommobs.RideableMobEntity;
import kr.toxicity.model.api.animation.AnimationIterator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Subterranodon - Flying dinosaur. Tameable by throwing raw fish near it,
 * when it lands to eat, approach and mount with saddle.
 * 1 rider, basic flight, no special ability, no storage.
 */
public class SubterrandonMob extends RideableMobEntity {

    private boolean fishEaten = false;
    private boolean landed = false;

    public SubterrandonMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.SUBTERRANODON);
        this.canFly = true;
        this.mountSpeed = 0.2;
        this.jumpStrength = 0.8;
        this.maxRiders = 1;
        this.chestSlots = 0;
        this.tamingRequirement = 1.0;
        this.fallDamageReduction = 0.0;
    }

    @Override
    protected void onSpawn() {
        playAnimation("idle", AnimationIterator.Type.LOOP);
    }

    @Override
    protected void onTick() {
        super.onTick();
        if (hitboxEntity == null || !alive) return;

        // Check for nearby dropped raw fish
        if (!tamed && !fishEaten) {
            for (Entity entity : hitboxEntity.getNearbyEntities(6, 4, 6)) {
                if (entity instanceof Item item) {
                    ItemStack stack = item.getItemStack();
                    if (stack.getType() == Material.COD || stack.getType() == Material.SALMON
                            || stack.getType() == Material.TROPICAL_FISH) {
                        // Fly toward fish and eat
                        if (hitboxEntity instanceof Mob mob) {
                            mob.getPathfinder().moveTo(item.getLocation(), 1.5);
                        }
                        double dist = hitboxEntity.getLocation().distanceSquared(item.getLocation());
                        if (dist < 4) {
                            stack.setAmount(stack.getAmount() - 1);
                            if (stack.getAmount() <= 0) item.remove();
                            fishEaten = true;
                            landed = true;
                            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                                    Sound.ENTITY_GENERIC_EAT, 1.0f, 1.2f);
                            hitboxEntity.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                                    hitboxEntity.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0);
                            for (Player p : findNearbyPlayers(10)) {
                                p.sendMessage(Component.text(
                                        "The Subterranodon lands to eat! Approach with a saddle.",
                                        NamedTextColor.GOLD));
                            }
                            // Stop flying, land
                            if (hitboxEntity instanceof Mob mob) {
                                mob.setAI(false);
                            }
                        }
                        break;
                    }
                }
            }
        }

        // Follow owner if tamed
        if (tamed && hitboxEntity.getPassengers().isEmpty()) {
            Player owner = getOwner();
            if (owner != null) {
                double dist = hitboxEntity.getLocation().distanceSquared(owner.getLocation());
                if (dist > 400) {
                    hitboxEntity.teleport(owner.getLocation().add(1, 1, 1));
                } else if (dist > 25 && hitboxEntity instanceof Mob mob) {
                    mob.getPathfinder().moveTo(owner.getLocation(), 1.0);
                }
            }
        }
    }

    @Override
    public void onInteract(Player player) {
        if (tamed) {
            super.onInteract(player);
            return;
        }

        if (!fishEaten || !landed) {
            player.sendMessage(Component.text("Throw raw fish near it first!", NamedTextColor.GRAY));
            return;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() == Material.SADDLE) {
            hand.setAmount(hand.getAmount() - 1);
            saddled = true;
            if (hitboxEntity instanceof Mob mob) {
                mob.setAI(true);
            }
            addTamingProgress(tamingRequirement, player);
        } else {
            player.sendMessage(Component.text("Apply a saddle to tame!", NamedTextColor.GOLD));
        }
    }

    @Override
    protected void onTamed(Player player) {
        if (hitboxEntity != null) {
            hitboxEntity.customName(Component.text(player.getName() + "'s Subterranodon",
                    NamedTextColor.GOLD));
        }
    }

    @Override
    protected void onMount(Player player) {
        playAnimation("fly_mounted", AnimationIterator.Type.LOOP);
        player.sendMessage(Component.text("You take to the skies!", NamedTextColor.GOLD));
    }

    @Override
    protected void onDismount(Player player) {
        playAnimation("idle", AnimationIterator.Type.LOOP);
    }

    @Override
    protected void onDamage(double amount, Player source) {
        playAnimation("hurt", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.ENTITY_PARROT_HURT, 1.0f, 0.6f);
    }

    @Override
    protected void onDeath(Player killer) {
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_PARROT_DEATH, 1.0f, 0.6f);
        }
        if (killer != null) {
            killer.giveExp(15);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(Material.BONE, 1 + (int) (Math.random() * 2)));
        drops.add(new ItemStack(Material.FEATHER, 2 + (int) (Math.random() * 3)));
        return drops;
    }

    private Player getOwner() {
        if (ownerUUID == null) return null;
        return plugin.getServer().getPlayer(ownerUUID);
    }

    @Override
    protected long getTickRate() {
        return 5L;
    }
}
