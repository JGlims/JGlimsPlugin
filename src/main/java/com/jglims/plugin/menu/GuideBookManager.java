package com.jglims.plugin.menu;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.legendary.*;
import com.jglims.plugin.enchantments.EnchantmentType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * GuideBookManager - Livro-guia completo do servidor em portugues.
 * Dado automaticamente no primeiro login e via /guia.
 * Volumes: 1 (Basico + Armas), 2 (Armaduras + Encantamentos), 3 (Sistemas + Bosses).
 */
public class GuideBookManager implements Listener {

    private final JGlimsPlugin plugin;
    private final NamespacedKey KEY_HAS_GUIDE;

    public GuideBookManager(JGlimsPlugin plugin) {
        this.plugin = plugin;
        KEY_HAS_GUIDE = new NamespacedKey(plugin, "received_guide_v2");
    }

    @EventHandler
    public void onFirstJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        if (!pdc.has(KEY_HAS_GUIDE, PersistentDataType.BYTE)) {
            giveAllVolumes(player);
            pdc.set(KEY_HAS_GUIDE, PersistentDataType.BYTE, (byte) 1);
        }
    }

    public void giveAllVolumes(Player player) {
        player.getInventory().addItem(buildVolume1());
        player.getInventory().addItem(buildVolume2());
        player.getInventory().addItem(buildVolume3());
        player.sendMessage(Component.text("Voce recebeu o Guia do JGlims! Sao 3 volumes.", NamedTextColor.GOLD));
    }

    // ========================================
    // VOLUME 1: Bem-vindo + Armas Lendarias
    // ========================================
    private ItemStack buildVolume1() {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle("\u00a76Guia JGlims \u00a77Vol. 1");
        meta.setAuthor("JGlims");
        meta.setGeneration(BookMeta.Generation.ORIGINAL);
        List<Component> pages = new ArrayList<>();

        // Page 1 - Welcome
        pages.add(buildPage(
                "\u00a76\u00a7l=== Guia JGlims ===\n\n" +
                "\u00a70Fala, aventureiro! Bem-vindo ao servidor.\n\n" +
                "Esse guia tem tudo que tu precisa saber: armas, armaduras, encantamentos, bosses, eventos e mais.\n\n" +
                "\u00a77Sao 3 volumes, entao bora la!"
        ));

        // Page 2 - Tier System
        pages.add(buildPage(
                "\u00a76\u00a7lSistema de Tiers\n\n" +
                "\u00a7fCOMMON \u00a77- Dano 10-13\n" +
                "\u00a7aRARE \u00a77- Dano 12-15\n" +
                "\u00a75EPIC \u00a77- Dano 14-17\n" +
                "\u00a76MYTHIC \u00a77- Dano 16-22\n" +
                "\u00a74ABYSSAL \u00a77- Dano 22-30\n\n" +
                "\u00a70Nao tem progressao obrigatoria. Tu pode achar qualquer arma a qualquer momento."
        ));

        // Page 3 - How abilities work
        pages.add(buildPage(
                "\u00a76\u00a7lHabilidades\n\n" +
                "\u00a70Toda arma lendaria tem 2 habilidades:\n\n" +
                "\u00a7eClick direito\u00a70 = Habilidade primaria\n\n" +
                "\u00a7eAgachar + Click direito\u00a70 = Habilidade alternativa\n\n" +
                "\u00a77Cada uma tem cooldown separado. Quanto mais forte o tier, maior o cooldown."
        ));

        // Page 4 - Where to find weapons
        pages.add(buildPage(
                "\u00a76\u00a7lOnde achar armas?\n\n" +
                "\u00a7fCOMMON\u00a70: Estruturas do overworld, mobs hostis (raro)\n\n" +
                "\u00a7aRARE\u00a70: Nether, Elder Guardian, Blood Moon\n\n" +
                "\u00a75EPIC\u00a70: Warden, Wither, Blood Moon King\n\n" +
                "\u00a76MYTHIC\u00a70: Ender Dragon, End Cities, End Rift\n\n" +
                "\u00a74ABYSSAL\u00a70: Abyss Dragon"
        ));

        // Pages 5-12 - COMMON weapons
        pages.add(buildPage(
                "\u00a76\u00a7l--- Armas COMMON ---\n\n" +
                "\u00a7fAmethyst Shuriken\u00a70 (Espada, DMG 11)\n" +
                "  Shuriken Barrage / Shadow Step\n\n" +
                "\u00a7fGravescepter\u00a70 (Espada, DMG 11)\n" +
                "  Grave Rise / Death's Grasp\n\n" +
                "\u00a7fLycanbane\u00a70 (Espada, DMG 12)\n" +
                "  Silver Strike / Hunter's Sense"
        ));

        pages.add(buildPage(
                "\u00a7fGloomsteel Katana\u00a70 (Espada, DMG 11)\n" +
                "  Quick Draw / Shadow Stance\n\n" +
                "\u00a7fViridian Cleaver\u00a70 (Machado, DMG 13)\n" +
                "  Verdant Slam / Overgrowth\n\n" +
                "\u00a7fCrescent Edge\u00a70 (Machado, DMG 12)\n" +
                "  Lunar Cleave / Crescent Guard\n\n" +
                "\u00a7fGravecleaver\u00a70 (Espada, DMG 12)\n" +
                "  Bone Shatter / Undying Rage"
        ));

        pages.add(buildPage(
                "\u00a7fAmethyst Greatblade\u00a70 (Espada, DMG 11)\n" +
                "  Crystal Burst / Gem Resonance\n\n" +
                "\u00a7fFlamberge\u00a70 (Espada, DMG 12)\n" +
                "  Flame Wave / Ember Shield\n\n" +
                "\u00a7fCrystal Frostblade\u00a70 (Espada, DMG 11)\n" +
                "  Frost Spike / Permafrost\n\n" +
                "\u00a7fDemonslayer\u00a70 (Espada, DMG 13)\n" +
                "  Holy Rend / Purifying Aura"
        ));

        pages.add(buildPage(
                "\u00a7fVengeance\u00a70 (Espada, DMG 10)\n" +
                "  Retribution / Grudge Mark\n\n" +
                "\u00a7fOculus\u00a70 (Espada, DMG 11)\n" +
                "  All-Seeing Strike / Third Eye\n\n" +
                "\u00a7fAncient Greatslab\u00a70 (Espada, DMG 13)\n" +
                "  Seismic Slam / Stone Skin\n\n" +
                "\u00a7fNeptune's Fang\u00a70 (Tridente, DMG 12)\n" +
                "  Riptide Slash / Maelstrom"
        ));

        pages.add(buildPage(
                "\u00a7fTidecaller\u00a70 (Tridente, DMG 11)\n" +
                "  Tidal Spear / Depth Ward\n\n" +
                "\u00a7fStormfork\u00a70 (Tridente, DMG 13)\n" +
                "  Lightning Javelin / Thunder Shield\n\n" +
                "\u00a7fJade Reaper\u00a70 (Foice, DMG 12)\n" +
                "  Jade Crescent / Emerald Harvest\n\n" +
                "\u00a7fVindicator\u00a70 (Machado, DMG 11)\n" +
                "  Executioner's Chop / Rally Cry"
        ));

        pages.add(buildPage(
                "\u00a7fSpider Fang\u00a70 (Espada, DMG 10)\n" +
                "  Web Trap / Wall Crawler\n\n" +
                "\u00a76\u00a7l--- Armas RARE ---\n\n" +
                "\u00a7aOcean's Rage\u00a70 (Tridente, DMG 14)\n" +
                "  Stormbringer / Riptide Surge\n\n" +
                "\u00a7aAquatic Sacred Blade\u00a70 (Espada, DMG 13)\n" +
                "  Aqua Heal / Depth Pressure"
        ));

        pages.add(buildPage(
                "\u00a7aRoyal Chakram\u00a70 (Espada, DMG 12)\n" +
                "  Chakram Throw / Spinning Shield\n\n" +
                "\u00a7aAcidic Cleaver\u00a70 (Machado, DMG 14)\n" +
                "  Acid Splash / Corrosive Aura\n\n" +
                "\u00a7aMuramasa\u00a70 (Espada, DMG 13)\n" +
                "  Crimson Flash / Bloodlust\n\n" +
                "\u00a7aWindreaper\u00a70 (Espada, DMG 13)\n" +
                "  Gale Slash / Cyclone"
        ));

        pages.add(buildPage(
                "\u00a7aMoonlight\u00a70 (Espada, DMG 13)\n" +
                "  Lunar Beam / Eclipse\n\n" +
                "\u00a7aTalonbrand\u00a70 (Espada, DMG 13)\n" +
                "  Talon Strike / Predator's Mark\n\n" +
                "\u00a76\u00a7l--- Armas EPIC ---\n\n" +
                "\u00a75Berserker's Greataxe\u00a70 (Machado, DMG 17)\n" +
                "  Berserker Slam / Blood Rage"
        ));

        pages.add(buildPage(
                "\u00a75Black Iron Greatsword\u00a70 (Espada, DMG 15)\n" +
                "  Dark Slash / Iron Fortress\n\n" +
                "\u00a75Solstice\u00a70 (Espada, DMG 14)\n" +
                "  Solar Flare / Daybreak\n\n" +
                "\u00a75Grand Claymore\u00a70 (Espada, DMG 16)\n" +
                "  Titan Swing / Colossus Stance\n\n" +
                "\u00a75Calamity Blade\u00a70 (Machado, DMG 15)\n" +
                "  Cataclysm / Doomsday"
        ));

        pages.add(buildPage(
                "\u00a75Emerald Greatcleaver\u00a70 (Machado, DMG 16)\n" +
                "  Emerald Storm / Gem Barrier\n\n" +
                "\u00a75Demon's Blood Blade\u00a70 (Espada, DMG 15)\n" +
                "  Blood Rite / Demonic Form\n\n" +
                "\u00a77Continua no proximo volume..."
        ));

        // MYTHIC weapons (split across several pages)
        pages.add(buildPage(
                "\u00a76\u00a7l--- Armas MYTHIC ---\n\n" +
                "\u00a76True Excalibur\u00a70 (Espada, DMG 20)\n" +
                "  Holy Smite / Divine Shield\n\n" +
                "\u00a76Requiem of the Ninth Abyss\u00a70 (Espada, DMG 20)\n" +
                "  Soul Devour / Abyss Gate\n\n" +
                "\u00a76Phoenix's Grace\u00a70 (Machado, DMG 20)\n" +
                "  Phoenix Strike / Rebirth Flame"
        ));

        pages.add(buildPage(
                "\u00a76Soul Collector\u00a70 (Espada, DMG 19)\n" +
                "  Soul Harvest / Spirit Army\n\n" +
                "\u00a76Valhakyra\u00a70 (Espada, DMG 18)\n" +
                "  Valkyrie Dive / Wings of Valor\n\n" +
                "\u00a76Phantomguard\u00a70 (Espada, DMG 19)\n" +
                "  Spectral Cleave / Phase Shift\n\n" +
                "\u00a76Zenith\u00a70 (Espada, DMG 22)\n" +
                "  Final Judgment / Ascension"
        ));

        pages.add(buildPage(
                "\u00a76Dragon Sword\u00a70 (Espada, DMG 18)\n" +
                "  Dragon Breath / Draconic Roar\n\n" +
                "\u00a76Nocturne\u00a70 (Espada, DMG 18)\n" +
                "  Shadow Slash / Night Cloak\n\n" +
                "\u00a76Divine Axe Rhitta\u00a70 (Machado, DMG 22)\n" +
                "  Cruel Sun / Sunshine\n\n" +
                "\u00a76Yoru\u00a70 (Espada, DMG 20)\n" +
                "  World's Strongest Slash / Dark Mirror"
        ));

        pages.add(buildPage(
                "\u00a76Tengen's Blade\u00a70 (Espada, DMG 19)\n" +
                "  Sound Breathing / Constant Flux\n\n" +
                "\u00a76Edge of the Astral Plane\u00a70 (Espada, DMG 21)\n" +
                "  Astral Rend / Planar Shift\n\n" +
                "\u00a76Fallen God's Spear\u00a70 (Espada, DMG 20)\n" +
                "  Divine Impale / Heaven's Fall\n\n" +
                "\u00a76Nature Sword\u00a70 (Espada, DMG 18)\n" +
                "  Gaia's Wrath / Overgrowth Surge"
        ));

        pages.add(buildPage(
                "\u00a76Heavenly Partisan\u00a70 (Espada, DMG 19)\n" +
                "  Holy Lance / Celestial Judgment\n\n" +
                "\u00a76Soul Devourer\u00a70 (Espada, DMG 20)\n" +
                "  Soul Rip / Devouring Maw\n\n" +
                "\u00a76Mjolnir\u00a70 (Mace, DMG 22)\n" +
                "  Thunderstrike / Bifrost Slam\n\n" +
                "\u00a76Thousand Demon Daggers\u00a70 (Espada, DMG 18)\n" +
                "  Demon Barrage / Infernal Dance"
        ));

        pages.add(buildPage(
                "\u00a76Star Edge\u00a70 (Espada, DMG 20)\n" +
                "  Cosmic Slash / Supernova\n\n" +
                "\u00a76Rivers of Blood\u00a70 (Espada, DMG 19)\n" +
                "  Corpse Piler / Blood Tsunami\n\n" +
                "\u00a76Dragon Slaying Blade\u00a70 (Espada, DMG 20)\n" +
                "  Dragon Pierce / Slayer's Fury\n\n" +
                "\u00a76Stop Sign\u00a70 (Machado, DMG 18)\n" +
                "  Full Stop / Road Rage"
        ));

        pages.add(buildPage(
                "\u00a76Creation Splitter\u00a70 (Espada, DMG 22)\n" +
                "  Reality Cleave / Genesis Break\n\n" +
                "\u00a74\u00a7l--- Armas ABYSSAL ---\n\n" +
                "\u00a74Requiem Awakened\u00a70 (Espada, DMG 28)\n" +
                "  Abyssal Devour / Void Collapse\n\n" +
                "\u00a74True Excalibur Awakened\u00a70 (Espada, DMG 26)\n" +
                "  Divine Annihilation / Sacred Realm"
        ));

        pages.add(buildPage(
                "\u00a74Creation Splitter Awakened\u00a70 (Espada, DMG 30)\n" +
                "  Reality Shatter / Big Bang\n\n" +
                "\u00a74Whisperwind Awakened\u00a70 (Espada, DMG 24)\n" +
                "  Silent Storm / Phantom Cyclone\n\n" +
                "\u00a77As armas Abyssal so dropam do Abyss Dragon na dimensao do Abyss. Boa sorte!"
        ));

        for (Component page : pages) { meta.addPages(page); }
        book.setItemMeta(meta);
        return book;
    }

    // ========================================
    // VOLUME 2: Armaduras + Encantamentos
    // ========================================
    private ItemStack buildVolume2() {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle("\u00a76Guia JGlims \u00a77Vol. 2");
        meta.setAuthor("JGlims");
        meta.setGeneration(BookMeta.Generation.ORIGINAL);
        List<Component> pages = new ArrayList<>();

        pages.add(buildPage(
                "\u00a76\u00a7l=== Vol. 2 ===\n" +
                "\u00a70Armaduras e Encantamentos\n\n" +
                "\u00a76\u00a7l--- Armaduras ---\n\n" +
                "\u00a70O servidor tem 13 sets de armadura. 6 sao craftaveis e 7 sao drops de bosses e estruturas."
        ));

        // Craftable armors
        pages.add(buildPage(
                "\u00a7f\u00a7lReinforced Leather\u00a70 (COMMON)\n" +
                "Defesa: 12 | Couro reforced\n" +
                "Bonus: Speed I, +10% dodge, +2 HP\n\n" +
                "\u00a7f\u00a7lCopper Armor\u00a70 (COMMON)\n" +
                "Defesa: 14 | Malha de cobre\n" +
                "Bonus: Raio cai em voce sem dano + Speed II\n\n" +
                "\u00a7f\u00a7lChainmail Reinforced\u00a70 (COMMON)\n" +
                "Defesa: 15 | Malha reforcada\n" +
                "Bonus: +15% knockback resist"
        ));

        pages.add(buildPage(
                "\u00a7f\u00a7lAmethyst Armor\u00a70 (COMMON)\n" +
                "Defesa: 16 | Ferro + ametista\n" +
                "Bonus: +20% XP, cura quando low HP\n\n" +
                "\u00a7f\u00a7lBone Armor\u00a70 (COMMON)\n" +
                "Defesa: 16 | Ossos\n" +
                "Bonus: Undead te ignoram!\n\n" +
                "\u00a7a\u00a7lSculk Armor\u00a70 (RARE)\n" +
                "Defesa: 18 | Ferro + sculk\n" +
                "Bonus: Ve entidades por paredes, passos silenciosos"
        ));

        // Legendary armors
        pages.add(buildPage(
                "\u00a7a\u00a7lShadow Stalker\u00a70 (RARE)\n" +
                "Defesa: 24 | Diamante\n" +
                "Bonus: Invisivel agachado, +50% sneak dmg\n\n" +
                "\u00a75\u00a7lBlood Moon\u00a70 (EPIC)\n" +
                "Defesa: 30 | Netherite\n" +
                "Bonus: 8% lifesteal, +4 HP por kill\n\n" +
                "\u00a75\u00a7lNature's Embrace\u00a70 (EPIC)\n" +
                "Defesa: 28 | Netherite\n" +
                "Bonus: Regen II em florestas, imune a veneno"
        ));

        pages.add(buildPage(
                "\u00a75\u00a7lFrost Warden\u00a70 (EPIC)\n" +
                "Defesa: 30 | Netherite\n" +
                "Bonus: Congela inimigos perto, imune a frio\n\n" +
                "\u00a76\u00a7lVoid Walker\u00a70 (MYTHIC)\n" +
                "Defesa: 36 | Netherite\n" +
                "Bonus: Teleporte agachado+pulo, sem dano de void\n\n" +
                "\u00a76\u00a7lDragon Knight\u00a70 (MYTHIC)\n" +
                "Defesa: 40 | Netherite\n" +
                "Bonus: +35% dmg vs dragoes, Dragon Roar AoE"
        ));

        pages.add(buildPage(
                "\u00a74\u00a7lAbyssal Plate\u00a70 (ABYSSAL)\n" +
                "Defesa: 50 | Netherite\n" +
                "Bonus: +40% dano, imune Wither/Veneno, thorns 12\n" +
                "Drop: Abyss Dragon\n\n" +
                "\u00a77A armadura mais forte do servidor. So quem encarar o Abyss Dragon vai ter essa."
        ));

        // Enchantments
        pages.add(buildPage(
                "\u00a76\u00a7l--- Encantamentos ---\n\n" +
                "\u00a70O plugin tem 64 encantamentos custom! Aqui vao os principais:\n\n" +
                "\u00a7e\u00a7lEspadas:\u00a70\n" +
                "Lifesteal, Venom, Thunder Strike, Frost Aspect, Execute, Berserk, Whirlwind, Vampiric"
        ));

        pages.add(buildPage(
                "\u00a7e\u00a7lMachados:\u00a70\n" +
                "Timber, Lumberjack, Splitter, Cleave, Skull Splitter, Devastating\n\n" +
                "\u00a7e\u00a7lPicaretas:\u00a70\n" +
                "Vein Miner, Blast Mining, Auto Smelt, Ore Magnet, Spelunker, Shatter\n\n" +
                "\u00a7e\u00a7lPas:\u00a70\n" +
                "Tunnel Bore, Excavate, Path Maker, Magnetic, Earthmover"
        ));

        pages.add(buildPage(
                "\u00a7e\u00a7lArcos:\u00a70\n" +
                "Explosive Arrow, Homing, Multishot+, Poison Arrow, Frost Arrow, Sniper\n\n" +
                "\u00a7e\u00a7lBestas:\u00a70\n" +
                "Rapid Fire, Grapple, Chain Lightning, Volley, Piercing+\n\n" +
                "\u00a7e\u00a7lTridentes:\u00a70\n" +
                "Tempest, Lightning Rod, Riptide+, Aqua Affinity, Sea's Blessing"
        ));

        pages.add(buildPage(
                "\u00a7e\u00a7lArmadura:\u00a70\n" +
                "Thorns+, Fire Walker, Molten Core, Last Stand, Dodge, Regen, Absorption, Vitality\n\n" +
                "\u00a7e\u00a7lElytra:\u00a70\n" +
                "Rocket Boost, Wind Rider, Featherfall+\n\n" +
                "\u00a7e\u00a7lMaces:\u00a70\n" +
                "Earthquake, Stun, Graviton, Pulverize"
        ));

        pages.add(buildPage(
                "\u00a7e\u00a7lUniversais:\u00a70\n" +
                "Soulbound (mantem no death), Mending+\n\n" +
                "\u00a76\u00a7lComo encantar?\u00a70\n\n" +
                "Use a \u00a7eBigorna\u00a70 pra combinar encantamentos custom. Junta dois itens com o mesmo encantamento pra subir de nivel.\n\n" +
                "\u00a77Use /jglims enchants pra ver a lista completa!"
        ));

        // Recipes hint
        pages.add(buildPage(
                "\u00a76\u00a7l--- Receitas ---\n\n" +
                "\u00a70As armaduras craftaveis usam o padrao normal de craft do Minecraft mas com materiais diferentes:\n\n" +
                "\u00a7fReinforced Leather\u00a70: Couro + Pepitas de Ferro\n" +
                "\u00a7fCopper\u00a70: Lingotes de Cobre\n" +
                "\u00a7fChainmail Reinforced\u00a70: Correntes + Ferro\n" +
                "\u00a7fAmethyst\u00a70: Fragmentos de Ametista + Ferro\n" +
                "\u00a7fBone\u00a70: Ossos + Linha"
        ));

        for (Component page : pages) { meta.addPages(page); }
        book.setItemMeta(meta);
        return book;
    }

    // ========================================
    // VOLUME 3: Sistemas, Bosses, Eventos
    // ========================================
    private ItemStack buildVolume3() {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle("\u00a76Guia JGlims \u00a77Vol. 3");
        meta.setAuthor("JGlims");
        meta.setGeneration(BookMeta.Generation.ORIGINAL);
        List<Component> pages = new ArrayList<>();

        // Power-Ups
        pages.add(buildPage(
                "\u00a76\u00a7l=== Vol. 3 ===\n" +
                "\u00a70Sistemas, Bosses e Eventos\n\n" +
                "\u00a76\u00a7l--- Power-Ups ---\n\n" +
                "\u00a7cHeart Crystal\u00a70: +1 coracao permanente (max 40)\n\n" +
                "\u00a7bSoul Fragment\u00a70: +1% dano permanente (max 100)\n\n" +
                "\u00a7eTitan's Resolve\u00a70: +10% knockback resist (max 5)"
        ));

        pages.add(buildPage(
                "\u00a76Phoenix Feather\u00a70: Revive automatico (consumido no death)\n\n" +
                "\u00a7aVitality Shard\u00a70: +5% reducao de dano (max 10)\n\n" +
                "\u00a74Berserker Mark\u00a70: +3% velocidade (max 10)\n\n" +
                "\u00a7dKeepInventorer\u00a70: Keep inventory PERMANENTE. Compra com o Quest Villager por 64 diamantes + 32 esmeraldas."
        ));

        // Bosses
        pages.add(buildPage(
                "\u00a76\u00a7l--- Bosses ---\n\n" +
                "\u00a7eElder Guardian\u00a70\n" +
                "HP: 320 | Drop: Ocean's Rage\n\n" +
                "\u00a7eWither\u00a70\n" +
                "HP: 900 | Drop: 1-2 armas EPIC\n(Berserker Greataxe, Black Iron, Calamity, Demon's Blood)\n\n" +
                "\u00a7eWarden\u00a70\n" +
                "HP: 500 | Drop: 1-2 armas EPIC/MYTHIC + Titan's Resolve (20%)"
        ));

        pages.add(buildPage(
                "\u00a7eEnder Dragon\u00a70\n" +
                "HP: 1000 | Drop: 2-3 MYTHIC + Abyssal Key (100%)\n" +
                "Pool: Phoenix's Grace, Divine Axe, Edge of Astral Plane, Heavenly Partisan, Mjolnir, Rivers of Blood, Nocturne\n\n" +
                "\u00a74Abyss Dragon\u00a70\n" +
                "HP: 2000 | Drop: 2-3 ABYSSAL + armadura Abyssal Plate"
        ));

        // Events
        pages.add(buildPage(
                "\u00a76\u00a7l--- Eventos ---\n\n" +
                "\u00a7c\u00a7lBlood Moon\u00a70\n" +
                "15% chance por noite. Ceu vermelho, mobs +50% HP e +30% dano. A cada 10 mobs, spawna um Blood Moon Boss (6000 HP).\n" +
                "Drop: diamantes + arma RARE + chance EPIC.\n\n" +
                "\u00a77Segredo: durante Blood Moon, mobs tem 0.1% chance de dropar um fragmento de Pedra do Infinito..."
        ));

        pages.add(buildPage(
                "\u00a75\u00a7lEnd Rift\u00a70\n" +
                "10% chance quando Ender Dragon morre. Portal roxo gigante aparece perto do spawn. Waves de Endermen + Shulkers por 10 min.\n" +
                "Boss final: End Rift Dragon (600 HP)\n" +
                "Drop: 1-2 armas MYTHIC do pool do End Rift\n\n" +
                "\u00a7e\u00a7lNether Storm\u00a70\n" +
                "10% chance no Nether. Enxame de Ghasts + Blazes. Boss: Infernal Overlord."
        ));

        pages.add(buildPage(
                "\u00a7e\u00a7lPiglin Uprising\u00a70\n" +
                "8% chance no Nether. Exercito de Piglins. Boss: Piglin Emperor (500 HP).\n\n" +
                "\u00a75\u00a7lVoid Collapse\u00a70\n" +
                "5% chance no End. Tentaculos do void atacam de baixo. Boss: Void Leviathan (500 HP).\n\n" +
                "\u00a77Todos os eventos podem dar objetivos de quest!"
        ));

        // Mastery
        pages.add(buildPage(
                "\u00a76\u00a7l--- Maestria ---\n\n" +
                "\u00a70Quanto mais tu mata com um tipo de arma, mais bonus ganha:\n\n" +
                "\u00a7fNovato\u00a70: 0 kills (+1%)\n" +
                "\u00a7aLutador\u00a70: 100 kills (+5%)\n" +
                "\u00a7eLutador Exp.\u00a70: 500 kills (+10%)\n" +
                "\u00a76Mestre\u00a70: 1000 kills (+15%)\n\n" +
                "\u00a77Use /jglims mastery pra ver seu progresso!"
        ));

        // Boss Mastery
        pages.add(buildPage(
                "\u00a76\u00a7l--- Titulos de Boss ---\n\n" +
                "\u00a70Deu dano num boss antes dele morrer? Ganha titulo permanente:\n\n" +
                "Wither Slayer: +5% resist\n" +
                "Guardian Slayer: +7% resist\n" +
                "Warden Slayer: +10% resist\n" +
                "Dragon Slayer: +15% resist\n" +
                "Abyssal Conqueror: +20% resist\n" +
                "\u00a76God Slayer\u00a70: Mata todos os 5 = +25% resist + 20% dano!"
        ));

        // Infinity Gauntlet
        pages.add(buildPage(
                "\u00a76\u00a7l--- Manopla do Infinito ---\n\n" +
                "\u00a70Passo 1: Derrota o Thanos no Thanos Temple (estrutura rara no Badlands). Drop: Thanos Glove.\n\n" +
                "Passo 2: Coleta 6 fragmentos coloridos durante Blood Moons (0.1% chance por mob). Item secreto!\n\n" +
                "Passo 3: Combina cada fragmento + Nether Star na bigorna = Pedra do Infinito."
        ));

        pages.add(buildPage(
                "Passo 4: Craft shapeless - Thanos Glove + 6 Pedras do Infinito = Manopla do Infinito!\n\n" +
                "\u00a7ePoder:\u00a70 Click direito = mata 50% de TODOS os mobs hostis carregados na dimensao. Cooldown: 5 minutos.\n\n" +
                "\u00a77Bosses e mini-bosses sao imunes ao estalo."
        ));

        // Guilds
        pages.add(buildPage(
                "\u00a76\u00a7l--- Guildas ---\n\n" +
                "\u00a70Cria tua propria guilda!\n\n" +
                "/guild create <nome>\n" +
                "/guild invite <player>\n" +
                "/guild join\n" +
                "/guild leave\n" +
                "/guild info\n" +
                "/guild list\n\n" +
                "\u00a77Membros da mesma guilda nao se machucam (friendly fire off)."
        ));

        // Quests & NPC Wizard
        pages.add(buildPage(
                "\u00a76\u00a7l--- Quests ---\n\n" +
                "\u00a70Procura o \u00a76Questmaster\u00a70 nas Ultra Villages ou usa /questvillager.\n\n" +
                "Ele da missoes como:\n" +
                "- Matar 50 zombies\n" +
                "- Sobreviver a Blood Moon\n" +
                "- Derrotar o End Rift Dragon\n" +
                "- Coletar 3 fragmentos de Infinity\n\n" +
                "Recompensas: armas, power-ups, titulos!"
        ));

        pages.add(buildPage(
                "\u00a76\u00a7l--- Archmage ---\n\n" +
                "\u00a70O \u00a75Archmage\u00a70 aparece em Mage Towers e Ultra Villages. Ele vende:\n\n" +
                "- Armas MYTHIC exclusivas (48 diamantes + 16 Nether Stars)\n" +
                "- Heart Crystal (32 diamantes + 8 esmeraldas)\n" +
                "- Blessing Crystals (16 diamantes + 4 esmeraldas)\n" +
                "- Livros encantados custom\n" +
                "- Soul Fragments e Phoenix Feather"
        ));

        // Abyss Dimension
        pages.add(buildPage(
                "\u00a76\u00a7l--- Dimensao Abyss ---\n\n" +
                "\u00a70O Ender Dragon dropa a \u00a74Abyssal Key\u00a70 (100%). Usa ela pra ativar o portal:\n\n" +
                "Constroi um frame de End Stone (4x5, igual ao Nether portal). Click direito com a key.\n\n" +
                "La dentro: ilha flutuante gigante com arvores roxas, mobs Abyssal (2x HP), e um castelo no centro."
        ));

        pages.add(buildPage(
                "\u00a74\u00a7lAbyss Dragon\u00a70\n" +
                "HP: 2000 | Sem End Crystals!\n\n" +
                "Ataques:\n" +
                "- Void Breath (Wither III + Blindness)\n" +
                "- Ground Slam (AoE 10 blocos)\n" +
                "- Invoca Abyssal Endermen\n" +
                "- Enrage a 25% HP (2x velocidade)\n\n" +
                "Drop: armas ABYSSAL + Abyssal Plate + 10 Heart Crystals + 50 Soul Fragments"
        ));

        // Structures hint
        pages.add(buildPage(
                "\u00a76\u00a7l--- Estruturas ---\n\n" +
                "\u00a70O mundo ta cheio de estruturas custom! Algumas delas:\n\n" +
                "Ruined Colosseum, Druid's Grove, Shrek House, Mage Tower, Gigantic Castle, Fortress, Volcano, Ancient Temple...\n\n" +
                "\u00a77Cada uma tem mini-boss e bau com loot. Explora o mundo!"
        ));

        // Commands summary
        pages.add(buildPage(
                "\u00a76\u00a7l--- Comandos ---\n\n" +
                "/jglims menu - Menu criativo\n" +
                "/jglims help - Ajuda\n" +
                "/jglims mastery - Maestria\n" +
                "/jglims enchants - Encantamentos\n" +
                "/jglims bosstitles - Titulos\n" +
                "/jglims quests - Missoes\n" +
                "/guild - Guildas\n" +
                "/guia - Receber este guia\n\n" +
                "\u00a77Bom jogo! - JGlims"
        ));

        for (Component page : pages) { meta.addPages(page); }
        book.setItemMeta(meta);
        return book;
    }

    // ========== HELPER ==========
    private Component buildPage(String legacyText) {
        // Use legacy section-sign formatting for books (most compatible)
        return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                .legacySection().deserialize(legacyText);
    }
}