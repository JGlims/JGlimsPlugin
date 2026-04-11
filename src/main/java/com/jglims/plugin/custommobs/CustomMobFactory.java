package com.jglims.plugin.custommobs;

import com.jglims.plugin.JGlimsPlugin;
import com.jglims.plugin.custommobs.mobs.*;

/**
 * Factory that creates the correct {@link CustomMobEntity} subclass for a given
 * {@link CustomMobType}. Each mob type is mapped to its concrete implementation
 * in the {@code com.jglims.plugin.custommobs.mobs} package.
 */
public final class CustomMobFactory {

    private CustomMobFactory() {}

    /**
     * Creates a new instance of the custom mob for the given type.
     *
     * @param plugin the plugin instance
     * @param type   the mob type to create
     * @return a new CustomMobEntity subclass instance
     */
    public static CustomMobEntity create(JGlimsPlugin plugin, CustomMobType type) {
        if (type == null) return null;

        return switch (type) {
            // ── World Bosses ───────────────────────────────────────────
            case THE_WARRIOR -> new TheWarriorBoss(plugin);
            case KING_GLEEOK -> new KingGleeokBoss(plugin);
            case GODZILLA -> new GodzillaBoss(plugin);
            case GHIDORAH -> new GhidorahBoss(plugin);
            case NETHER_KING -> new NetherKingBoss(plugin);
            case PITLORD -> new PitlordBoss(plugin);
            case ILLIDAN -> new IllidanBoss(plugin);
            case WHULVK_WEREWOLF -> new WerewolfBoss(plugin);
            case JAVION_DRAGONKIN -> new JavionDragonkinBoss(plugin);
            case OGRIN_GIANT -> new OgrinGiantBoss(plugin);
            case FROSTMAW -> new FrostmawBoss(plugin);
            case PROTECTOR_OF_FORGE -> new ProtectorOfForgeBoss(plugin);
            case WILDFIRE -> new WildfireBoss(plugin);
            case SHADOWSAIL -> new ShadowsailBoss(plugin);
            case INVADERLING_COMMANDER -> new InvaderlingCommanderBoss(plugin);
            case T_REX -> new TRexBoss(plugin);
            case DINOBOT -> new DinobotBoss(plugin);
            case PARASAUROLOPHUS -> new ParasaurolophusBoss(plugin);

            // ── Boss / Event Boss ──────────────────────────────────────
            case SKELETON_DRAGON -> new SkeletonDragonBoss(plugin);
            case REALISTIC_DRAGON -> new RealisticDragonBoss(plugin);
            case RIPPER_ZOMBIE -> new RipperZombieBoss(plugin);
            case WITHER_STORM -> new WitherStormBoss(plugin);
            case MUSHROOM_MONSTROSITY -> new MushroomMonstrosityBoss(plugin);
            case WENDIGO -> new WendigoBoss(plugin);
            case TRIAL_CHAMBER_DEFENDER -> new TrialChamberDefenderBoss(plugin);
            case MUTANT_ZOMBIE -> new MutantZombieBoss(plugin);
            case WINGED_UNICORN -> new WingedUnicornMob(plugin);

            // ── Hostile Mobs ───────────────────────────────────────────
            case BLUE_TROLL -> new BlueTrollMob(plugin);
            case LOW_POLY_TROLL -> new LowPolyTrollMob(plugin);
            case ANGLER -> new AnglerMob(plugin);
            case NECROMANCER -> new NecromancerMob(plugin);
            case DEMON_GUY -> new DemonGuyMob(plugin);
            case PRISMAMORPHA -> new PrismamorphaMob(plugin);
            case REDSTONE_GOLEM -> new RedstoneGolemMob(plugin);
            case GENERAL_PIGLIN -> new GeneralPiglinMob(plugin);
            case BASALT_GOLEM -> new BasaltGolemMob(plugin);
            case CATACOMBS_GOLEM -> new CatacombsGolemMob(plugin);
            case THE_KEEPER -> new TheKeeperMob(plugin);
            case ANCIENT_RUNIC_PORTAL -> new AncientRunicPortalMob(plugin);
            case SOUL_STEALER -> new SoulStealerMob(plugin);
            case INVADERLING_SOLDIER -> new InvaderlingSoldierMob(plugin);
            case INVADERLING_RIDER -> new InvaderlingRiderMob(plugin);
            case INVADERLING_ARCHER -> new InvaderlingArcherMob(plugin);
            case SPINOSAURUS -> new SpinosaurusMob(plugin);
            case TREMORSAURUS -> new TremorsaurusMob(plugin);
            case VELOCIRAPTOR -> new VelociraptorMob(plugin);
            case BASILISK -> new BasiliskMob(plugin);

            // ── Passive / Neutral / Rideable ───────────────────────────
            case AXOLOTL_DRAGON -> new AxolotlDragonMob(plugin);
            case GRIZZLY_BEAR -> new GrizzlyBearMob(plugin);
            case GRASS_FATHER -> new GrassFatherMob(plugin);
            case UNICORN -> new UnicornMob(plugin);
            case OVERGROWN_UNICORN -> new OvergrownUnicornMob(plugin);
            case LEATHERN_DRAKE -> new LeathernDrakeMob(plugin);
            case AERGIA_WINGWALKER -> new AergiaWingwalkerMob(plugin);
            case DEMONIC_WINGWALKER -> new DemonicWingwalkerMob(plugin);
            case ICE_WYVERN -> new IceWyvernMob(plugin);
            case SUBTERRANODON -> new SubterrandonMob(plugin);
            case STEGOSAURUS -> new StegosaurusMob(plugin);
            case GROTTOCERATOPS -> new GrottoceratopsMob(plugin);

            // ── Constructible ──────────────────────────────────────────
            case OBSERVER_GOLEM -> new ObserverGolemMob(plugin);
            case STONE_GOLEM -> new StoneGolemMob(plugin);
            case MUTANT_IRON_GOLEM -> new MutantIronGolemMob(plugin);

            // ── NPCs ───────────────────────────────────────────────────
            case JAPANESE_DRAGON_NPC -> new JapaneseDragonNpc(plugin);
            case STEVE_NPC -> new SteveNpc(plugin);
            case MALGOSHA_NPC -> new MalgoshaNpc(plugin);
            case GARRET_NPC -> new GarretNpc(plugin);
            case NATALIE_NPC -> new NatalieNpc(plugin);
            case SWEETNESS_COPPER_GOLEM -> new SweetnessCopperGolemNpc(plugin);
            case GLARE_NPC -> new GlareNpc(plugin);
            case ANGEL_NPC -> new AngelNpc(plugin);
            case CENTAUR_NPC -> new CentaurNpc(plugin);
            case ARQUIMAGE_NPC -> new ArquimageNpc(plugin);
        };
    }
}
