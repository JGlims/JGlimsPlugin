package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobType;
import com.jglims.plugin.custommobs.RideableMobEntity;
import kr.toxicity.model.api.animation.AnimationIterator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Overgrown Unicorn - Swamp unicorn, poison immune. Plants flowers where it
 * walks (every 10 blocks on grass). Same taming mechanic as regular Unicorn.
 */
public class OvergrownUnicornMob extends RideableMobEntity {

    private boolean goldenAppleFed = false;
    private Location lastFlowerLocation;
    private int blocksTraveled = 0;

    private static final Material[] FLOWERS = {
            Material.DANDELION, Material.POPPY, Material.BLUE_ORCHID,
            Material.ALLIUM, Material.AZURE_BLUET, Material.LILY_OF_THE_VALLEY,
            Material.CORNFLOWER, Material.OXEYE_DAISY
    };

    public OvergrownUnicornMob(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.OVERGROWN_UNICORN);
        this.canFly = false;
        this.mountSpeed = 0.25;
        this.jumpStrength = 1.3;
        this.maxRiders = 1;
        this.tamingRequirement = 1.0;
        this.fallDamageReduction = 0.0;
    }

    @Override
    protected void onSpawn() {
        playAnimation("idle", AnimationIterator.Type.LOOP);
        if (hitboxEntity != null) {
            lastFlowerLocation = hitboxEntity.getLocation().clone();
        }
    }

    @Override
    protected void onTick() {
        super.onTick();
        if (hitboxEntity == null || !alive) return;

        // Track distance traveled and plant flowers
        Location current = hitboxEntity.getLocation();
        if (lastFlowerLocation != null) {
            double dist = current.distance(lastFlowerLocation);
            if (dist >= 10) {
                lastFlowerLocation = current.clone();
                plantFlower(current);
            }
        }

        // Poison immunity
        if (hitboxEntity.hasPotionEffect(org.bukkit.potion.PotionEffectType.POISON)) {
            hitboxEntity.removePotionEffect(org.bukkit.potion.PotionEffectType.POISON);
        }

        // Vine/flower particles when walking
        if (hitboxEntity.getVelocity().lengthSquared() > 0.005) {
            hitboxEntity.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                    current.add(0, 0.3, 0), 1, 0.3, 0.1, 0.3, 0);
        }
    }

    private void plantFlower(Location loc) {
        Block ground = loc.getWorld().getHighestBlockAt(loc);
        Block above = ground.getRelative(0, 1, 0);
        if (ground.getType() == Material.GRASS_BLOCK && above.getType() == Material.AIR) {
            Random rand = new Random();
            above.setType(FLOWERS[rand.nextInt(FLOWERS.length)]);
            loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                    above.getLocation().add(0.5, 0.5, 0.5), 5, 0.3, 0.3, 0.3, 0);
        }
    }

    @Override
    public void onInteract(Player player) {
        if (tamed) {
            super.onInteract(player);
            return;
        }

        // Feed golden apple
        if (!goldenAppleFed) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand.getType() == Material.GOLDEN_APPLE) {
                hand.setAmount(hand.getAmount() - 1);
                goldenAppleFed = true;
                hitboxEntity.getWorld().spawnParticle(Particle.HEART,
                        hitboxEntity.getLocation().add(0, 1.5, 0), 8, 0.5, 0.3, 0.5, 0);
                hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                        Sound.ENTITY_HORSE_EAT, 1.0f, 1.0f);
                player.sendMessage(Component.text("The Overgrown Unicorn appreciates the offering! Mount it.",
                        NamedTextColor.GREEN));
            } else {
                player.sendMessage(Component.text("Try offering a golden apple.", NamedTextColor.GRAY));
            }
            return;
        }

        // Mount to tame
        if (!player.isSprinting()) {
            addTamingProgress(1.0, player);
            if (tamed) {
                saddled = true;
            }
        } else {
            player.sendMessage(Component.text("Approach slowly!", NamedTextColor.YELLOW));
        }
    }

    @Override
    protected void onTamed(Player player) {
        if (hitboxEntity != null) {
            hitboxEntity.customName(Component.text(player.getName() + "'s Overgrown Unicorn",
                    NamedTextColor.GREEN));
        }
    }

    @Override
    protected void onMount(Player player) {
        playAnimation("walk_mounted", AnimationIterator.Type.LOOP);
    }

    @Override
    protected void onDismount(Player player) {
        playAnimation("idle", AnimationIterator.Type.LOOP);
    }

    @Override
    protected void onDamage(double amount, Player source) {
        playAnimation("hurt", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.ENTITY_HORSE_HURT, 1.0f, 0.9f);
    }

    @Override
    protected void onDeath(Player killer) {
        if (hitboxEntity != null) {
            hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                    Sound.ENTITY_HORSE_DEATH, 1.0f, 0.9f);
            hitboxEntity.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                    hitboxEntity.getLocation().add(0, 1, 0), 30, 1.5, 1, 1.5, 0);
        }
        if (killer != null) {
            killer.giveExp(25);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(Material.LEATHER, 2));
        Random rand = new Random();
        for (int i = 0; i < 2 + rand.nextInt(3); i++) {
            drops.add(new ItemStack(FLOWERS[rand.nextInt(FLOWERS.length)], 1));
        }
        return drops;
    }

    @Override
    protected long getTickRate() {
        return 5L;
    }
}
