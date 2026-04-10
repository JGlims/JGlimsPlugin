package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobType;
import com.jglims.plugin.custommobs.CustomWorldBoss;
import com.jglims.plugin.legendary.LegendaryTier;
import com.jglims.plugin.legendary.LegendaryWeapon;
import com.jglims.plugin.legendary.LegendaryWeaponManager;
import kr.toxicity.model.api.animation.AnimationIterator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Ogrin Giant - Humorous swamp boss. HP 400.
 * Single phase. Club slam, mud throw (Slowness II),
 * belly bump, and idle chat messages.
 */
public class OgrinGiantBoss extends CustomWorldBoss {

    private final Random random = new Random();

    private static final String[] IDLE_MESSAGES = {
            "Ogrin hungry... you look like food!",
            "Why tiny person poke Ogrin?!",
            "Ogrin smash now!",
            "You smell like onion... Ogrin LIKE onion!",
            "Ogrin was napping! You rude!",
            "Me no like being hit!",
            "Ogrin belly hurt... must be lunch time!",
            "Stop wiggling, food!",
            "Ogrin's mom said he special!",
            "Why you run? Ogrin just want hug!"
    };

    public OgrinGiantBoss(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.OGRIN_GIANT);
        phaseThresholds = new double[]{}; // No phase transitions
        aggroRadius = 25.0;
        arenaRadius = 30.0;
        announceRadius = 80.0;
    }

    @Override
    protected void onSpawn() {
        super.onSpawn();
        if (hitboxEntity != null) {
            Location loc = hitboxEntity.getLocation();
            loc.getWorld().playSound(loc, Sound.ENTITY_RAVAGER_ROAR, 1.5f, 0.5f);
        }
    }

    // ── Combat Logic ──────────────────────────────────────────────────

    @Override
    protected void combatTick() {
        if (hitboxEntity == null) return;

        Player target = findNearestPlayer(aggroRadius);
        if (target == null) return;

        if (hitboxEntity instanceof Mob mob) {
            mob.setTarget(target);
        }

        double distSq = hitboxEntity.getLocation().distanceSquared(target.getLocation());

        // Idle chat messages
        if (combatTicks % 100 == 0 && random.nextDouble() < 0.4) {
            String msg = IDLE_MESSAGES[random.nextInt(IDLE_MESSAGES.length)];
            for (Player p : findNearbyPlayers(aggroRadius)) {
                p.sendMessage(Component.text("[Ogrin] ", NamedTextColor.GREEN)
                        .append(Component.text(msg, NamedTextColor.YELLOW)));
            }
        }

        // Club slam
        if (distSq < 20 && isAttackReady("club_slam", 35)) {
            useAttack("club_slam");
            performClubSlam(target);
        }

        // Mud throw (ranged, Slowness II)
        if (distSq > 16 && distSq < 225 && isAttackReady("mud_throw", 50)) {
            useAttack("mud_throw");
            performMudThrow(target);
        }

        // Belly bump (close range AoE)
        if (distSq < 12 && isAttackReady("belly_bump", 60)) {
            useAttack("belly_bump");
            performBellyBump();
        }

        // Ground stomp
        if (isAttackReady("stomp", 70)) {
            useAttack("stomp");
            performStomp();
        }
    }

    // ── Attack Implementations ────────────────────────────────────────

    private void performClubSlam(Player target) {
        playAnimation("club_slam", AnimationIterator.Type.PLAY_ONCE);
        hitboxEntity.getWorld().playSound(hitboxEntity.getLocation(),
                Sound.ENTITY_IRON_GOLEM_ATTACK, 1.5f, 0.5f);

        target.damage(14.0, hitboxEntity);
        target.setVelocity(new Vector(0, 0.5, 0));

        Location loc = target.getLocation();
        loc.getWorld().spawnParticle(Particle.BLOCK, loc, 20, 0.5, 0.2, 0.5, 0,
                Material.DIRT.createBlockData());

        // Humorous message
        if (random.nextDouble() < 0.3) {
            for (Player p : findNearbyPlayers(aggroRadius)) {
                p.sendMessage(Component.text("[Ogrin] ", NamedTextColor.GREEN)
                        .append(Component.text("BONK!", NamedTextColor.RED)
                                .decorate(TextDecoration.BOLD)));
            }
        }
    }

    private void performMudThrow(Player target) {
        playAnimation("throw", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_SLIME_SQUISH, 2.0f, 0.5f);

        // Mud particles toward target
        Vector dir = target.getLocation().toVector().subtract(loc.toVector()).normalize();
        for (int i = 1; i <= 8; i++) {
            Location pLoc = loc.clone().add(dir.clone().multiply(i * 1.5)).add(0, 1.5, 0);
            loc.getWorld().spawnParticle(Particle.BLOCK, pLoc, 10, 0.3, 0.3, 0.3, 0,
                    Material.MUD.createBlockData());
        }

        if (hitboxEntity.getLocation().distanceSquared(target.getLocation()) < 225) {
            target.damage(8.0, hitboxEntity);
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 1));
            target.getWorld().spawnParticle(Particle.BLOCK, target.getLocation(), 15, 0.3, 0.3, 0.3, 0,
                    Material.MUD.createBlockData());

            if (random.nextDouble() < 0.3) {
                target.sendMessage(Component.text("[Ogrin] ", NamedTextColor.GREEN)
                        .append(Component.text("Haha! Mud in face!", NamedTextColor.YELLOW)));
            }
        }
    }

    private void performBellyBump() {
        playAnimation("belly_bump", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 2.0f, 0.4f);
        loc.getWorld().playSound(loc, Sound.ENTITY_SLIME_SQUISH, 1.5f, 0.3f);

        for (Player p : findNearbyPlayers(4)) {
            p.damage(10.0, hitboxEntity);
            Vector kb = p.getLocation().toVector()
                    .subtract(loc.toVector()).normalize().multiply(2.5).setY(0.8);
            p.setVelocity(kb);
        }

        // Humorous
        for (Player p : findNearbyPlayers(aggroRadius)) {
            p.sendMessage(Component.text("[Ogrin] ", NamedTextColor.GREEN)
                    .append(Component.text("BELLY BUMP!", NamedTextColor.GOLD)
                            .decorate(TextDecoration.BOLD)));
        }
    }

    private void performStomp() {
        playAnimation("stomp", AnimationIterator.Type.PLAY_ONCE);
        Location loc = hitboxEntity.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_IRON_GOLEM_ATTACK, 2.0f, 0.3f);
        loc.getWorld().spawnParticle(Particle.BLOCK, loc, 30, 2, 0.3, 2, 0,
                Material.DIRT.createBlockData());

        for (Player p : findNearbyPlayers(5)) {
            p.damage(8.0, hitboxEntity);
            p.setVelocity(new Vector(0, 0.6, 0));
        }
    }

    // ── Death + Drops ─────────────────────────────────────────────────

    @Override
    protected void onDeath(Player killer) {
        super.onDeath(killer);
        if (hitboxEntity != null) {
            Location loc = hitboxEntity.getLocation();
            loc.getWorld().playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.5f, 1.0f);
            loc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 80, 2, 2, 2, 0.3);
        }

        for (Player p : findNearbyPlayers(80)) {
            p.sendMessage(Component.text("[Ogrin] ", NamedTextColor.GREEN)
                    .append(Component.text("Ogrin... just wanted... onion...", NamedTextColor.YELLOW)
                            .decorate(TextDecoration.ITALIC)));
            p.sendMessage(Component.text("Ogrin Giant has been defeated!",
                    NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        }

        if (killer != null) {
            killer.giveExp(1200);
        }
    }

    @Override
    protected List<ItemStack> getDrops(Player killer) {
        List<ItemStack> drops = new ArrayList<>();

        // 1 EPIC legendary weapon
        LegendaryWeapon[] epicWeapons = LegendaryWeapon.byTier(LegendaryTier.EPIC);
        if (epicWeapons.length > 0) {
            LegendaryWeaponManager wm = JGlimsPlugin.getInstance().getLegendaryWeaponManager();
            drops.add(wm.createWeapon(epicWeapons[random.nextInt(epicWeapons.length)]));
        }

        // Emeralds x16
        drops.add(new ItemStack(Material.EMERALD, 16));

        // Onion (renamed potato)
        ItemStack onion = new ItemStack(Material.POTATO, 1);
        ItemMeta meta = onion.getItemMeta();
        meta.displayName(Component.text("Ogrin's Onion", NamedTextColor.GREEN)
                .decorate(TextDecoration.ITALIC));
        meta.lore(List.of(
                Component.text("A pungent onion from Ogrin's pocket.", NamedTextColor.GRAY),
                Component.text("It smells terrible.", NamedTextColor.DARK_GRAY)
        ));
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "custom_item"), PersistentDataType.STRING, "ogrins_onion");
        onion.setItemMeta(meta);
        drops.add(onion);

        return drops;
    }
}
