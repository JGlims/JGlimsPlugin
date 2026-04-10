package com.jglims.plugin.custommobs.mobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.CustomMobType;
import com.jglims.plugin.custommobs.CustomNpcEntity;
import com.jglims.plugin.legendary.LegendaryTier;
import com.jglims.plugin.legendary.LegendaryWeapon;
import com.jglims.plugin.legendary.LegendaryWeaponManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Random;

/**
 * Malgosha NPC - Nether trader found near bastions and fortresses.
 * Sells everything Piglins barter for, plus fire resistance potions,
 * blaze powder, nether wart, and occasionally rare legendary weapons.
 */
public class MalgoshaNpc extends CustomNpcEntity {

    private static final TextColor NETHER_RED = TextColor.color(200, 60, 30);
    private static final TextColor MOLTEN_GOLD = TextColor.color(255, 180, 40);
    private final Random random = new Random();

    public MalgoshaNpc(JGlimsPlugin plugin) {
        super(plugin, CustomMobType.MALGOSHA_NPC);
        this.tradeRefreshDays = 5;

        dialogueLines.add(Component.text("Heh... another mortal in my domain.", NETHER_RED));
        dialogueLines.add(Component.text("The Piglins think they own this place. Fools.", NamedTextColor.WHITE));
        dialogueLines.add(Component.text("I've been trading here since before the Nether King rose.", NETHER_RED));
        dialogueLines.add(Component.text("Gold is gold, but I have things the brutes never could.", NamedTextColor.WHITE));
        dialogueLines.add(Component.text("If you have gold blocks... I might show you the real goods.", MOLTEN_GOLD)
                .decorate(TextDecoration.ITALIC));
    }

    @Override
    protected void initializeTrades() {
        // --- Standard Piglin barter items (gold ingots as currency) ---
        addTrade(new ItemStack(Material.GOLD_INGOT, 4), new ItemStack(Material.OBSIDIAN, 2), 8);
        addTrade(new ItemStack(Material.GOLD_INGOT, 4), new ItemStack(Material.CRYING_OBSIDIAN, 3), 8);
        addTrade(new ItemStack(Material.GOLD_INGOT, 6), new ItemStack(Material.ENDER_PEARL, 4), 5);
        addTrade(new ItemStack(Material.GOLD_INGOT, 3), new ItemStack(Material.STRING, 8), 10);
        addTrade(new ItemStack(Material.GOLD_INGOT, 4), new ItemStack(Material.QUARTZ, 16), 10);
        addTrade(new ItemStack(Material.GOLD_INGOT, 3), new ItemStack(Material.IRON_NUGGET, 16), 10);
        addTrade(new ItemStack(Material.GOLD_INGOT, 4), new ItemStack(Material.SPECTRAL_ARROW, 12), 8);
        addTrade(new ItemStack(Material.GOLD_INGOT, 6), new ItemStack(Material.FIRE_CHARGE, 4), 8);
        addTrade(new ItemStack(Material.GOLD_INGOT, 5), new ItemStack(Material.GRAVEL, 16), 10);
        addTrade(new ItemStack(Material.GOLD_INGOT, 4), new ItemStack(Material.BLACKSTONE, 16), 10);
        addTrade(new ItemStack(Material.GOLD_INGOT, 8), new ItemStack(Material.SOUL_SAND, 8), 5);
        addTrade(new ItemStack(Material.GOLD_INGOT, 6), new ItemStack(Material.NETHER_BRICK, 16), 8);

        // --- Premium items: blaze powder, nether wart, fire resistance ---
        addTrade(new ItemStack(Material.GOLD_INGOT, 8), new ItemStack(Material.BLAZE_POWDER, 4), 5);
        addTrade(new ItemStack(Material.GOLD_INGOT, 6), new ItemStack(Material.NETHER_WART, 8), 5);
        addTrade(new ItemStack(Material.GOLD_INGOT, 8), new ItemStack(Material.MAGMA_CREAM, 4), 5);

        // Fire Resistance Potion (8:00)
        ItemStack fireResPotion = new ItemStack(Material.POTION);
        PotionMeta potionMeta = (PotionMeta) fireResPotion.getItemMeta();
        potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 9600, 0), true);
        potionMeta.displayName(Component.text("Potion of Fire Resistance", NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));
        fireResPotion.setItemMeta(potionMeta);
        addTrade(new ItemStack(Material.GOLD_INGOT, 12), fireResPotion, 3);

        // Splash Fire Resistance
        ItemStack splashFireRes = new ItemStack(Material.SPLASH_POTION);
        PotionMeta splashMeta = (PotionMeta) splashFireRes.getItemMeta();
        splashMeta.addCustomEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 4800, 0), true);
        splashMeta.displayName(Component.text("Splash Potion of Fire Resistance", NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));
        splashFireRes.setItemMeta(splashMeta);
        addTrade(new ItemStack(Material.GOLD_INGOT, 16), splashFireRes, 3);

        // Gilded Blackstone
        addTrade(new ItemStack(Material.GOLD_INGOT, 12), new ItemStack(Material.GILDED_BLACKSTONE, 8), 3);

        // --- Occasional RARE legendary weapon (very expensive: 32 gold blocks) ---
        LegendaryWeaponManager wm = JGlimsPlugin.getInstance().getLegendaryWeaponManager();
        LegendaryWeapon[] rareWeapons = LegendaryWeapon.byTier(LegendaryTier.RARE);
        if (rareWeapons.length > 0) {
            LegendaryWeapon chosen = rareWeapons[random.nextInt(rareWeapons.length)];
            addTrade(
                    new ItemStack(Material.GOLD_BLOCK, 32),
                    new ItemStack(Material.NETHERITE_INGOT, 1),
                    wm.createWeapon(chosen),
                    1
            );
        }
    }
}
