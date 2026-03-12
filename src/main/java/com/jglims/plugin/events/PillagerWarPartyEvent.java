package com.jglims.plugin.events;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.enchantments.CustomEnchantManager;
import com.jglims.plugin.enchantments.EnchantmentType;
import com.jglims.plugin.legendary.LegendaryTier;
import com.jglims.plugin.legendary.LegendaryWeapon;
import com.jglims.plugin.legendary.LegendaryWeaponManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Pillager War Party Event.
 * v3.2.0 Phase 10 - Reduced Captain HP to 300, added legendary weapon drops
 * and custom enchanted book drops for captain and regular mobs.
 */
public class PillagerWarPartyEvent implements Listener {

    private final JGlimsPlugin plugin;
    private final EventManager eventManager;
    private final Random random = new Random();

    private boolean active = false;
    private World currentWorld;
    private LivingEntity captain;
    private final List<LivingEntity> partyMembers = new ArrayList<>();
    private final Set<UUID> trackedPartyMobs = new HashSet<>();
    private int ticksElapsed;
    private static final int DURATION_TICKS = 9600; // 8 minutes
    private static final int REINFORCE_INTERVAL = 1200; // 60 seconds

    // Weapon pools for captain drops
    private static final LegendaryWeapon[] CAPTAIN_POOL = {
            LegendaryWeapon.BERSERKERS_GREATAXE, LegendaryWeapon.BLACK_IRON_GREATSWORD,
            LegendaryWeapon.CALAMITY_BLADE, LegendaryWeapon.DEMONS_BLOOD_BLADE,
            LegendaryWeapon.GRAND_CLAYMORE, LegendaryWeapon.EMERALD_GREATCLEAVER,
            LegendaryWeapon.SOLSTICE
    };

    // Enchantment pool for book drops
    private static final EnchantmentType[] BOOK_POOL = {
            EnchantmentType.VAMPIRISM, EnchantmentType.BERSERKER, EnchantmentType.BLEED,
            EnchantmentType.CHAIN_LIGHTNING, EnchantmentType.LIFESTEAL, EnchantmentType.CLEAVE,
            EnchantmentType.GUILLOTINE, EnchantmentType.WRATH, EnchantmentType.FROSTBITE_BLADE,
            EnchantmentType.VENOMSTRIKE, EnchantmentType.THUNDERLORD, EnchantmentType.SOULBOUND,
            EnchantmentType.FORTIFICATION, EnchantmentType.DEFLECTION, EnchantmentType.DODGE
    };

    public PillagerWarPartyEvent(JGlimsPlugin plugin, EventManager eventManager) {
        this.plugin = plugin;
        this.eventManager = eventManager;
    }

    public void start(World world) {
        if (active) return;
        active = true;
        currentWorld = world;
        ticksElapsed = 0;
        partyMembers.clear();
        trackedPartyMobs.clear();

        eventManager.broadcastEvent(world, "\u2694 PILLAGER WAR PARTY \u2694",
            "A warband marches across the land!", TextColor.color(120, 60, 30));

        spawnInitialParty();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!active || currentWorld.getPlayers().isEmpty()) {
                    endEvent(false);
                    cancel();
                    return;
                }
                ticksElapsed += 20;

                doPartyAI();
                doAmbientEffects();

                if (ticksElapsed % REINFORCE_INTERVAL == 0 && ticksElapsed < DURATION_TICKS - 1200) {
                    spawnReinforcements();
                }

                if (ticksElapsed >= DURATION_TICKS) {
                    endEvent(true);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void spawnInitialParty() {
        List<Player> players = currentWorld.getPlayers();
        if (players.isEmpty()) return;
        Player target = players.get(random.nextInt(players.size()));
        Location center = findSafeSpawn(target.getLocation(), 40, 60);

        captain = spawnCaptain(center);

        for (int i = 0; i < 6; i++) {
            Location loc = center.clone().add(random.nextGaussian() * 5, 0, random.nextGaussian() * 5);
            loc.setY(currentWorld.getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()) + 1);
            Pillager p = currentWorld.spawn(loc, Pillager.class);
            enhancePillager(p, 60, 12);
            p.customName(Component.text("War Raider", NamedTextColor.DARK_RED));
            p.setCustomNameVisible(true);
            partyMembers.add(p);
            trackedPartyMobs.add(p.getUniqueId());
        }

        for (int i = 0; i < 2; i++) {
            Location loc = center.clone().add(random.nextGaussian() * 6, 0, random.nextGaussian() * 6);
            loc.setY(currentWorld.getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()) + 1);
            Ravager r = currentWorld.spawn(loc, Ravager.class);
            if (r.getAttribute(Attribute.MAX_HEALTH) != null) {
                r.getAttribute(Attribute.MAX_HEALTH).setBaseValue(200);
                r.setHealth(200);
            }
            r.customName(Component.text("War Ravager", TextColor.color(180, 50, 20)));
            r.setCustomNameVisible(true);
            partyMembers.add(r);
            trackedPartyMobs.add(r.getUniqueId());
        }

        for (int i = 0; i < 2; i++) {
            Location loc = center.clone().add(random.nextGaussian() * 4, 0, random.nextGaussian() * 4);
            loc.setY(currentWorld.getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()) + 1);
            Vindicator v = currentWorld.spawn(loc, Vindicator.class);
            enhanceVindicator(v, 80, 16);
            v.customName(Component.text("War Berserker", TextColor.color(200, 40, 40)));
            v.setCustomNameVisible(true);
            partyMembers.add(v);
            trackedPartyMobs.add(v.getUniqueId());
        }

        Location evoLoc = center.clone().add(random.nextGaussian() * 3, 0, random.nextGaussian() * 3);
        evoLoc.setY(currentWorld.getHighestBlockYAt(evoLoc.getBlockX(), evoLoc.getBlockZ()) + 1);
        Evoker evoker = currentWorld.spawn(evoLoc, Evoker.class);
        if (evoker.getAttribute(Attribute.MAX_HEALTH) != null) {
            evoker.getAttribute(Attribute.MAX_HEALTH).setBaseValue(100);
            evoker.setHealth(100);
        }
        evoker.customName(Component.text("War Mage", TextColor.color(100, 0, 150)));
        evoker.setCustomNameVisible(true);
        partyMembers.add(evoker);
        trackedPartyMobs.add(evoker.getUniqueId());
    }

    private LivingEntity spawnCaptain(Location center) {
        Location loc = center.clone();
        loc.setY(currentWorld.getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()) + 1);
        Vindicator cap = currentWorld.spawn(loc, Vindicator.class);
        // Reduced HP from 500 to 300
        eventManager.configureBoss(cap, "\u2694 War Captain \u2694", 300,
            TextColor.color(180, 30, 30));
        eventManager.tagEventBoss(cap, "PILLAGER_WAR_PARTY");
        cap.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
        cap.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 1, false, false));
        cap.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 2, false, false));
        if (cap.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
            cap.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(18);
        }
        cap.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_AXE));
        cap.getEquipment().setHelmet(new ItemStack(Material.NETHERITE_HELMET));
        cap.getEquipment().setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
        cap.getEquipment().setItemInMainHandDropChance(0f);
        cap.getEquipment().setHelmetDropChance(0f);
        cap.getEquipment().setChestplateDropChance(0f);
        partyMembers.add(cap);
        return cap;
    }

    private void spawnReinforcements() {
        if (captain == null || captain.isDead()) return;
        Location loc = captain.getLocation();

        for (int i = 0; i < 3; i++) {
            Location spawn = loc.clone().add(random.nextGaussian() * 8, 0, random.nextGaussian() * 8);
            spawn.setY(currentWorld.getHighestBlockYAt(spawn.getBlockX(), spawn.getBlockZ()) + 1);
            Pillager p = currentWorld.spawn(spawn, Pillager.class);
            enhancePillager(p, 50, 10);
            p.customName(Component.text("War Reinforcement", NamedTextColor.GRAY));
            p.setCustomNameVisible(true);
            partyMembers.add(p);
            trackedPartyMobs.add(p.getUniqueId());
        }

        if (random.nextDouble() < 0.4) {
            Location spawn = loc.clone().add(random.nextGaussian() * 6, 0, random.nextGaussian() * 6);
            spawn.setY(currentWorld.getHighestBlockYAt(spawn.getBlockX(), spawn.getBlockZ()) + 1);
            Ravager r = currentWorld.spawn(spawn, Ravager.class);
            if (r.getAttribute(Attribute.MAX_HEALTH) != null) {
                r.getAttribute(Attribute.MAX_HEALTH).setBaseValue(150);
                r.setHealth(150);
            }
            r.customName(Component.text("War Beast", TextColor.color(160, 60, 20)));
            r.setCustomNameVisible(true);
            partyMembers.add(r);
            trackedPartyMobs.add(r.getUniqueId());
        }

        for (Player p : currentWorld.getPlayers()) {
            p.sendMessage(Component.text("\u2694 Pillager reinforcements arrive!", NamedTextColor.DARK_RED));
            p.playSound(p.getLocation(), Sound.EVENT_RAID_HORN, 0.8f, 1.2f);
        }
    }

    private void doPartyAI() {
        if (captain != null && !captain.isDead()) {
            double hpPercent = captain.getHealth() / captain.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
            if (hpPercent <= 0.5 && !captain.getScoreboardTags().contains("phase2")) {
                captain.addScoreboardTag("phase2");
                captain.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2, false, false));
                captain.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 3, false, false));
                captain.customName(Component.text("\u2694 Enraged War Captain \u2694",
                    TextColor.color(255, 20, 20)).decorate(TextDecoration.BOLD));
                for (Player p : currentWorld.getPlayers()) {
                    p.sendMessage(Component.text("\u2620 The War Captain enters a frenzy!",
                        TextColor.color(255, 50, 50)).decorate(TextDecoration.BOLD));
                    p.playSound(p.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 1.0f, 0.6f);
                }
            }

            Location bLoc = captain.getLocation();
            currentWorld.spawnParticle(Particle.ANGRY_VILLAGER, bLoc.add(0, 2, 0), 5, 1, 0.5, 1);
        }

        partyMembers.removeIf(e -> e == null || e.isDead());
    }

    private void doAmbientEffects() {
        if (captain == null || captain.isDead()) return;
        if (ticksElapsed % 200 == 0) {
            for (Player p : currentWorld.getPlayers()) {
                if (p.getLocation().distanceSquared(captain.getLocation()) < 10000) {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.6f, 0.5f);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!active) return;
        LivingEntity entity = event.getEntity();

        // Regular war party mob drops (non-boss)
        if (trackedPartyMobs.contains(entity.getUniqueId()) && !eventManager.isEventBoss(entity)) {
            trackedPartyMobs.remove(entity.getUniqueId());
            ThreadLocalRandom rand = ThreadLocalRandom.current();
            // 15% chance to drop a custom enchanted book
            if (rand.nextDouble() < 0.15) {
                dropCustomEnchantBook(entity.getLocation(), 1, 2);
            }
            // 8% chance to drop a COMMON legendary weapon
            if (rand.nextDouble() < 0.08) {
                eventManager.dropEventWeapons(entity.getLocation(), LegendaryTier.COMMON, 1);
            }
            return;
        }

        // Boss (Captain) drops
        if (!eventManager.isEventBoss(entity)) return;
        String type = eventManager.getEventType(entity);
        if (!"PILLAGER_WAR_PARTY".equals(type)) return;

        Location loc = entity.getLocation();
        ThreadLocalRandom rand = ThreadLocalRandom.current();

        // Drop 1-2 EPIC legendary weapons from the captain pool
        int weaponCount = 1 + rand.nextInt(2);
        LegendaryWeaponManager wm = plugin.getLegendaryWeaponManager();
        List<LegendaryWeapon> pool = new ArrayList<>(Arrays.asList(CAPTAIN_POOL));
        for (int i = 0; i < weaponCount && !pool.isEmpty(); i++) {
            LegendaryWeapon weapon = pool.remove(rand.nextInt(pool.size()));
            ItemStack item = wm.createWeapon(weapon);
            if (item != null) {
                loc.getWorld().dropItemNaturally(loc, item);
            }
        }

        // Drop 1-2 custom enchanted books (level 2-4)
        int bookCount = 1 + rand.nextInt(2);
        for (int i = 0; i < bookCount; i++) {
            dropCustomEnchantBook(loc, 2, 4);
        }

        eventManager.dropMiscLoot(loc, 8, 16);

        // Thematic drops
        loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.EMERALD, 10 + rand.nextInt(20)));
        loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.CROSSBOW, 1));
        if (rand.nextDouble() < 0.4) {
            loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.TOTEM_OF_UNDYING, 1));
        }

        // Death VFX
        currentWorld.spawnParticle(Particle.EXPLOSION, loc, 5, 2, 2, 2);
        currentWorld.spawnParticle(Particle.SMOKE, loc, 25, 3, 3, 3, 0.05);
        currentWorld.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.7f);

        for (Player p : currentWorld.getPlayers()) {
            p.sendMessage(Component.text("\u2694 The War Captain has fallen! The warband scatters!",
                NamedTextColor.GREEN).decorate(TextDecoration.BOLD));
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (LivingEntity m : partyMembers) {
                    if (m != null && !m.isDead()) m.remove();
                }
                partyMembers.clear();
                trackedPartyMobs.clear();
            }
        }.runTaskLater(plugin, 200L);

        endEvent(false);
    }

    /**
     * Creates and drops a custom enchanted book at the given location.
     */
    private void dropCustomEnchantBook(Location loc, int minLevel, int maxLevel) {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        EnchantmentType enchant = BOOK_POOL[rand.nextInt(BOOK_POOL.length)];
        int level = Math.min(minLevel + rand.nextInt(maxLevel - minLevel + 1), enchant.getMaxLevel());

        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta == null) return;

        CustomEnchantManager cem = plugin.getEnchantManager();
        NamespacedKey key = cem.getKey(enchant);
        meta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.INTEGER, level);

        String enchantName = enchant.name().replace('_', ' ');
        StringBuilder sb = new StringBuilder();
        for (String word : enchantName.split(" ")) {
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase()).append(' ');
        }
        String displayEnchantName = sb.toString().trim();

        meta.displayName(Component.text(displayEnchantName + " " + toRoman(level), NamedTextColor.AQUA)
                .decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Custom Enchantment Book", NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Apply on Anvil", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        book.setItemMeta(meta);
        loc.getWorld().dropItemNaturally(loc, book);
    }

    private String toRoman(int num) {
        return switch (num) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(num);
        };
    }

    private void endEvent(boolean timeout) {
        if (!active) return;
        active = false;
        eventManager.endEvent(currentWorld);

        if (timeout) {
            for (LivingEntity m : partyMembers) {
                if (m != null && !m.isDead()) m.remove();
            }
            partyMembers.clear();
            trackedPartyMobs.clear();
            eventManager.broadcastEventEnd(currentWorld,
                "\u2694 The war party retreats into the wilderness...",
                TextColor.color(120, 120, 120));
        }

        plugin.getLogger().info("Pillager War Party event ended in " + currentWorld.getName());
    }

    private void enhancePillager(Pillager p, double hp, double damage) {
        if (p.getAttribute(Attribute.MAX_HEALTH) != null) {
            p.getAttribute(Attribute.MAX_HEALTH).setBaseValue(hp);
            p.setHealth(hp);
        }
        if (p.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
            p.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
        }
        p.getEquipment().setItemInMainHand(new ItemStack(Material.CROSSBOW));
        p.getEquipment().setItemInMainHandDropChance(0f);
    }

    private void enhanceVindicator(Vindicator v, double hp, double damage) {
        if (v.getAttribute(Attribute.MAX_HEALTH) != null) {
            v.getAttribute(Attribute.MAX_HEALTH).setBaseValue(hp);
            v.setHealth(hp);
        }
        if (v.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
            v.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
        }
        v.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_AXE));
        v.getEquipment().setItemInMainHandDropChance(0f);
    }

    private Location findSafeSpawn(Location playerLoc, int minDist, int maxDist) {
        for (int attempt = 0; attempt < 20; attempt++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double dist = minDist + random.nextDouble() * (maxDist - minDist);
            int x = playerLoc.getBlockX() + (int)(Math.cos(angle) * dist);
            int z = playerLoc.getBlockZ() + (int)(Math.sin(angle) * dist);
            int y = currentWorld.getHighestBlockYAt(x, z) + 1;
            Material surface = currentWorld.getBlockAt(x, y - 1, z).getType();
            if (!surface.isAir() && surface != Material.WATER && surface != Material.LAVA && y > 50 && y < 200) {
                return new Location(currentWorld, x + 0.5, y, z + 0.5);
            }
        }
        int x = playerLoc.getBlockX() + 50;
        int z = playerLoc.getBlockZ() + 50;
        int y = currentWorld.getHighestBlockYAt(x, z) + 1;
        return new Location(currentWorld, x + 0.5, y, z + 0.5);
    }

    public boolean isActive() { return active; }
}
