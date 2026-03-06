package com.jglims.plugin.events;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.legendary.LegendaryTier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.*;

/**
 * Pillager Siege Event.
 * Triggered by proximity to villages during night (4% chance per overworld check).
 * 12 escalating waves over 12 minutes.
 * Wave composition: Pillagers, Vindicators, Ravagers, Evokers, Witches.
 * Final boss: Siege Commander (700 HP Vindicator with ground slam + rally cry).
 * Drops EPIC/MYTHIC weapons.
 */
public class PillagerSiegeEvent implements Listener {

    private final JGlimsPlugin plugin;
    private final EventManager eventManager;
    private final Random random = new Random();

    private boolean active = false;
    private World currentWorld;
    private Location siegeCenter;
    private LivingEntity commander;
    private final List<LivingEntity> siegeMobs = new ArrayList<>();
    private int ticksElapsed;
    private int currentWave = 0;
    private static final int MAX_WAVES = 12;
    private static final int WAVE_INTERVAL = 1200; // 60 seconds between waves
    private static final int DURATION_TICKS = 14400; // 12 minutes
    private static final int BOSS_WAVE = 10; // Commander spawns on wave 10

    public PillagerSiegeEvent(JGlimsPlugin plugin, EventManager eventManager) {
        this.plugin = plugin;
        this.eventManager = eventManager;
    }

    public void start(World world) {
        if (active) return;
        active = true;
        currentWorld = world;
        ticksElapsed = 0;
        currentWave = 0;
        siegeMobs.clear();

        // Find a village-area player (or any player)
        List<Player> players = world.getPlayers();
        if (players.isEmpty()) { active = false; return; }
        Player target = players.get(random.nextInt(players.size()));
        siegeCenter = target.getLocation().clone();

        eventManager.broadcastEvent(world, "\u26A0 PILLAGER SIEGE \u26A0",
            "A massive pillager army approaches!", TextColor.color(100, 30, 30));

        // Build siege camp markers (banners around the perimeter)
        buildSiegeCamp();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!active || currentWorld.getPlayers().isEmpty()) {
                    endEvent(false);
                    cancel();
                    return;
                }
                ticksElapsed += 20;

                doAmbientEffects();

                // Spawn waves
                if (ticksElapsed % WAVE_INTERVAL == 0 && currentWave < MAX_WAVES) {
                    currentWave++;
                    spawnWave(currentWave);
                }

                // Commander AI
                if (commander != null && !commander.isDead()) {
                    doCommanderAI();
                }

                // Timeout
                if (ticksElapsed >= DURATION_TICKS) {
                    endEvent(true);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void buildSiegeCamp() {
        // Place ominous banners at 4 cardinal points (60 blocks out)
        Material banner = Material.WHITE_BANNER;
        int[][] offsets = {{60, 0}, {-60, 0}, {0, 60}, {0, -60}};
        for (int[] off : offsets) {
            int x = siegeCenter.getBlockX() + off[0];
            int z = siegeCenter.getBlockZ() + off[1];
            int y = currentWorld.getHighestBlockYAt(x, z) + 1;
            Block block = currentWorld.getBlockAt(x, y, z);
            if (block.getType().isAir()) {
                block.setType(banner, false);
            }
        }
    }

    private void spawnWave(int wave) {
        int pillagerCount = 4 + wave * 2;
        int vindicatorCount = wave >= 3 ? wave - 1 : 0;
        int ravagerCount = wave >= 5 ? (wave - 3) / 2 : 0;
        int evokerCount = wave >= 7 ? 1 + (wave - 7) / 2 : 0;
        int witchCount = wave >= 4 ? 1 + (wave - 4) / 3 : 0;

        // Spawn pillagers
        for (int i = 0; i < pillagerCount; i++) {
            Location loc = getSpawnLocation(50, 70);
            Pillager p = currentWorld.spawn(loc, Pillager.class);
            double hp = 40 + wave * 5;
            double dmg = 8 + wave;
            if (p.getAttribute(Attribute.MAX_HEALTH) != null) {
                p.getAttribute(Attribute.MAX_HEALTH).setBaseValue(hp);
                p.setHealth(hp);
            }
            if (p.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
                p.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(dmg);
            }
            p.customName(Component.text("Siege Pillager [W" + wave + "]", NamedTextColor.DARK_RED));
            p.setCustomNameVisible(true);
            p.getEquipment().setItemInMainHand(new ItemStack(Material.CROSSBOW));
            p.getEquipment().setItemInMainHandDropChance(0f);
            siegeMobs.add(p);
        }

        // Vindicators
        for (int i = 0; i < vindicatorCount; i++) {
            Location loc = getSpawnLocation(45, 65);
            Vindicator v = currentWorld.spawn(loc, Vindicator.class);
            double hp = 60 + wave * 8;
            double dmg = 12 + wave * 2;
            if (v.getAttribute(Attribute.MAX_HEALTH) != null) {
                v.getAttribute(Attribute.MAX_HEALTH).setBaseValue(hp);
                v.setHealth(hp);
            }
            if (v.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
                v.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(dmg);
            }
            v.customName(Component.text("Siege Vindicator [W" + wave + "]", TextColor.color(200, 50, 50)));
            v.setCustomNameVisible(true);
            v.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_AXE));
            v.getEquipment().setItemInMainHandDropChance(0f);
            siegeMobs.add(v);
        }

        // Ravagers
        for (int i = 0; i < ravagerCount; i++) {
            Location loc = getSpawnLocation(50, 70);
            Ravager r = currentWorld.spawn(loc, Ravager.class);
            double hp = 150 + wave * 15;
            if (r.getAttribute(Attribute.MAX_HEALTH) != null) {
                r.getAttribute(Attribute.MAX_HEALTH).setBaseValue(hp);
                r.setHealth(hp);
            }
            r.customName(Component.text("Siege Beast [W" + wave + "]", TextColor.color(140, 40, 20)));
            r.setCustomNameVisible(true);
            siegeMobs.add(r);
        }

        // Evokers
        for (int i = 0; i < evokerCount; i++) {
            Location loc = getSpawnLocation(55, 70);
            Evoker e = currentWorld.spawn(loc, Evoker.class);
            double hp = 80 + wave * 5;
            if (e.getAttribute(Attribute.MAX_HEALTH) != null) {
                e.getAttribute(Attribute.MAX_HEALTH).setBaseValue(hp);
                e.setHealth(hp);
            }
            e.customName(Component.text("Siege Sorcerer [W" + wave + "]", TextColor.color(130, 0, 170)));
            e.setCustomNameVisible(true);
            siegeMobs.add(e);
        }

        // Witches
        for (int i = 0; i < witchCount; i++) {
            Location loc = getSpawnLocation(55, 70);
            Witch w = currentWorld.spawn(loc, Witch.class);
            double hp = 60 + wave * 4;
            if (w.getAttribute(Attribute.MAX_HEALTH) != null) {
                w.getAttribute(Attribute.MAX_HEALTH).setBaseValue(hp);
                w.setHealth(hp);
            }
            w.customName(Component.text("Siege Witch [W" + wave + "]", TextColor.color(80, 160, 80)));
            w.setCustomNameVisible(true);
            siegeMobs.add(w);
        }

        // Spawn Commander on BOSS_WAVE
        if (wave == BOSS_WAVE && (commander == null || commander.isDead())) {
            spawnCommander();
        }

        // Wave announcement
        TextColor waveColor = wave >= BOSS_WAVE ? TextColor.color(255, 50, 50) : TextColor.color(200, 80, 40);
        for (Player p : currentWorld.getPlayers()) {
            p.sendMessage(Component.text("\u26A0 Siege Wave " + wave + "/" + MAX_WAVES + " — ",  waveColor)
                .append(Component.text(pillagerCount + " pillagers", NamedTextColor.DARK_RED))
                .append(Component.text(vindicatorCount > 0 ? ", " + vindicatorCount + " vindicators" : "", NamedTextColor.RED))
                .append(Component.text(ravagerCount > 0 ? ", " + ravagerCount + " ravagers" : "", TextColor.color(180, 60, 20)))
                .append(Component.text(evokerCount > 0 ? ", " + evokerCount + " evokers" : "", TextColor.color(130, 0, 170)))
                .append(Component.text(witchCount > 0 ? ", " + witchCount + " witches" : "", TextColor.color(80, 160, 80))));
            p.playSound(p.getLocation(), Sound.EVENT_RAID_HORN, 1.0f, 0.8f + wave * 0.05f);
        }

        // Clean dead mobs from tracking
        siegeMobs.removeIf(e2 -> e2 == null || e2.isDead());
    }

    private void spawnCommander() {
        Location loc = getSpawnLocation(40, 55);
        Vindicator cmd = currentWorld.spawn(loc, Vindicator.class);
        eventManager.configureBoss(cmd, "\u2620 Siege Commander \u2620", 700,
            TextColor.color(220, 20, 20));
        eventManager.tagEventBoss(cmd, "PILLAGER_SIEGE");
        cmd.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
        cmd.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 2, false, false));
        cmd.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 3, false, false));
        cmd.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
        if (cmd.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
            cmd.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(25);
        }
        if (cmd.getAttribute(Attribute.ARMOR) != null) {
            cmd.getAttribute(Attribute.ARMOR).setBaseValue(20);
        }
        cmd.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_AXE));
        cmd.getEquipment().setHelmet(new ItemStack(Material.NETHERITE_HELMET));
        cmd.getEquipment().setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
        cmd.getEquipment().setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
        cmd.getEquipment().setBoots(new ItemStack(Material.NETHERITE_BOOTS));
        cmd.getEquipment().setItemInMainHandDropChance(0f);
        cmd.getEquipment().setHelmetDropChance(0f);
        cmd.getEquipment().setChestplateDropChance(0f);
        cmd.getEquipment().setLeggingsDropChance(0f);
        cmd.getEquipment().setBootsDropChance(0f);
        commander = cmd;
        siegeMobs.add(cmd);

        // Commander entrance
        for (Player p : currentWorld.getPlayers()) {
            p.showTitle(Title.title(
                Component.text("\u2620 SIEGE COMMANDER \u2620", TextColor.color(220, 20, 20))
                    .decorate(TextDecoration.BOLD),
                Component.text("The army has a leader!", NamedTextColor.GRAY),
                Title.Times.times(Duration.ofMillis(300), Duration.ofSeconds(3), Duration.ofMillis(500))));
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.8f, 0.5f);
        }
    }

    private void doCommanderAI() {
        double hpPercent = commander.getHealth() / commander.getAttribute(Attribute.MAX_HEALTH).getBaseValue();

        // Ground Slam — every 8 seconds, damages and launches nearby players
        if (commander.getTicksLived() % 160 == 0) {
            Location cLoc = commander.getLocation();
            currentWorld.spawnParticle(Particle.EXPLOSION, cLoc, 3, 2, 0.5, 2);
            currentWorld.playSound(cLoc, Sound.ENTITY_IRON_GOLEM_ATTACK, 1.5f, 0.5f);
            for (Player p : cLoc.getNearbyPlayers(6)) {
                p.damage(12.0, commander);
                p.setVelocity(p.getLocation().subtract(cLoc).toVector().normalize().multiply(1.5).setY(0.6));
            }
        }

        // Rally Cry — at 60% and 30% HP, heals and buffs nearby siege mobs
        if (hpPercent <= 0.6 && !commander.getScoreboardTags().contains("rally1")) {
            commander.addScoreboardTag("rally1");
            doRallyCry("The Commander rallies the troops!", 1);
        }
        if (hpPercent <= 0.3 && !commander.getScoreboardTags().contains("rally2")) {
            commander.addScoreboardTag("rally2");
            doRallyCry("The Commander unleashes a desperate war cry!", 2);
            // Phase 3: extra speed and damage
            commander.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 3, false, false));
            commander.customName(Component.text("\u2620 ENRAGED Siege Commander \u2620",
                TextColor.color(255, 0, 0)).decorate(TextDecoration.BOLD));
        }

        // Particle aura
        Location bLoc = commander.getLocation().add(0, 1.5, 0);
        currentWorld.spawnParticle(Particle.ANGRY_VILLAGER, bLoc, 3, 0.8, 0.5, 0.8);
        currentWorld.spawnParticle(Particle.FLAME, bLoc, 5, 0.5, 0.3, 0.5, 0.02);
    }

    private void doRallyCry(String message, int buffLevel) {
        Location cLoc = commander.getLocation();
        // Buff nearby siege mobs
        for (LivingEntity mob : siegeMobs) {
            if (mob == null || mob.isDead() || mob == commander) continue;
            if (mob.getLocation().distanceSquared(cLoc) < 900) { // 30 blocks
                mob.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 600, buffLevel, false, false));
                mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, buffLevel, false, false));
                mob.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, buffLevel, false, false));
            }
        }
        // VFX
        currentWorld.spawnParticle(Particle.SONIC_BOOM, cLoc, 1, 0, 0, 0);
        currentWorld.playSound(cLoc, Sound.ENTITY_RAVAGER_ROAR, 2.0f, 0.4f);
        for (Player p : currentWorld.getPlayers()) {
            p.sendMessage(Component.text("\u26A0 " + message, TextColor.color(255, 80, 30))
                .decorate(TextDecoration.BOLD));
        }
    }

    private void doAmbientEffects() {
        // Distant battle sounds
        if (ticksElapsed % 100 == 0) {
            for (Player p : currentWorld.getPlayers()) {
                if (p.getLocation().distanceSquared(siegeCenter) < 40000) { // 200 blocks
                    p.playSound(p.getLocation(), Sound.ENTITY_PILLAGER_AMBIENT, 0.3f, 0.8f);
                }
            }
        }
        // Clean dead mobs
        siegeMobs.removeIf(e -> e == null || e.isDead());
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        if (!active) return;
        LivingEntity entity = event.getEntity();
        if (!eventManager.isEventBoss(entity)) return;
        String type = eventManager.getEventType(entity);
        if (!"PILLAGER_SIEGE".equals(type)) return;

        Location loc = entity.getLocation();

        // Drop MYTHIC weapons (1) + EPIC weapons (1-2)
        eventManager.dropEventWeapons(loc, LegendaryTier.MYTHIC, 1);
        eventManager.dropEventWeapons(loc, LegendaryTier.EPIC, 1 + random.nextInt(2));
        eventManager.dropMiscLoot(loc, 10, 20);

        // Thematic drops
        loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.EMERALD_BLOCK, 3 + random.nextInt(5)));
        loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.TOTEM_OF_UNDYING, 1 + random.nextInt(2)));
        loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.EXPERIENCE_BOTTLE, 16 + random.nextInt(16)));
        if (random.nextDouble() < 0.15) {
            loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.NETHER_STAR, 1));
        }

        // Death VFX
        currentWorld.spawnParticle(Particle.EXPLOSION_EMITTER, loc, 3, 1, 1, 1);
        currentWorld.spawnParticle(Particle.SMOKE, loc, 80, 4, 4, 4, 0.1);
        currentWorld.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
        currentWorld.strikeLightningEffect(loc);

        // Announce
        for (Player p : currentWorld.getPlayers()) {
            p.showTitle(Title.title(
                Component.text("\u2694 SIEGE DEFEATED \u2694", NamedTextColor.GREEN)
                    .decorate(TextDecoration.BOLD),
                Component.text("The Siege Commander has been slain!", NamedTextColor.GRAY),
                Title.Times.times(Duration.ofMillis(300), Duration.ofSeconds(3), Duration.ofMillis(500))));
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }

        // Retreat remaining mobs
        new BukkitRunnable() {
            @Override
            public void run() {
                for (LivingEntity m : siegeMobs) {
                    if (m != null && !m.isDead()) m.remove();
                }
                siegeMobs.clear();
            }
        }.runTaskLater(plugin, 200L);

        endEvent(false);
    }

    private void endEvent(boolean timeout) {
        if (!active) return;
        active = false;
        eventManager.endEvent(currentWorld);

        if (timeout) {
            for (LivingEntity m : siegeMobs) {
                if (m != null && !m.isDead()) m.remove();
            }
            siegeMobs.clear();
            eventManager.broadcastEventEnd(currentWorld,
                "\u26A0 The siege force retreats... for now.",
                TextColor.color(160, 100, 30));
        }

        plugin.getLogger().info("Pillager Siege event ended in " + currentWorld.getName());
    }

    private Location getSpawnLocation(int minDist, int maxDist) {
        for (int attempt = 0; attempt < 15; attempt++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double dist = minDist + random.nextDouble() * (maxDist - minDist);
            int x = siegeCenter.getBlockX() + (int)(Math.cos(angle) * dist);
            int z = siegeCenter.getBlockZ() + (int)(Math.sin(angle) * dist);
            int y = currentWorld.getHighestBlockYAt(x, z) + 1;
            Material surface = currentWorld.getBlockAt(x, y - 1, z).getType();
            if (!surface.isAir() && surface != Material.WATER && surface != Material.LAVA) {
                return new Location(currentWorld, x + 0.5, y, z + 0.5);
            }
        }
        return siegeCenter.clone().add(55, 0, 55);
    }

    public boolean isActive() { return active; }
}