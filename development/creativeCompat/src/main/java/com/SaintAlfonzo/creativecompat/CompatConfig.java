package com.SaintAlfonzo.creativecompat;

import java.util.List;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Creative Compat's COMMON config, {@code config/creativecompat-common.toml}. The sections moved out of patcherMod's
 * creativeengineering-common.toml on 2026-11-01 with keys, comments and defaults unchanged; the first boot with
 * Compat copies a player's values over ({@code swap.ConfigRename.carrySections}, called from {@link CreativeCompat}).
 */
public final class CompatConfig {

    public static final ForgeConfigSpec SPEC;
    public static final LeafPiles LEAF_PILES;
    public static final Moss MOSS;
    public static final VineShearing VINE_SHEARING;
    public static final MonsterBox MONSTER_BOX;
    public static final TrashSlot TRASH_SLOT;
    public static final Cats CATS;
    public static final GiantSquid GIANT_SQUID;
    public static final Paintings PAINTINGS;

    /**
     * The toml tables carried over from creativeengineering-common.toml, in order. {@code [cats]} (added 2026-11-01,
     * after the carve) and {@code [giant_squid]} (2026-11-02) are new in this file and are not carried. {@code [campfire]}
     * moved on to Creative Delight's file on 2026-11-04 (creative_split.md 7.9 step 1), which carries it from here.
     */
    public static final List<String> SECTIONS =
            List.of("leaf_piles", "moss", "vine_shearing", "monster_box", "trash_slot");

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        LEAF_PILES = new LeafPiles(builder);
        MOSS = new Moss(builder);
        VINE_SHEARING = new VineShearing(builder);
        MONSTER_BOX = new MonsterBox(builder);
        TRASH_SLOT = new TrashSlot(builder);
        CATS = new Cats(builder);
        GIANT_SQUID = new GiantSquid(builder);
        PAINTINGS = new Paintings(builder);
        SPEC = builder.build();
    }

    private CompatConfig() {}

    /**
     * Immersive Weathering leaf piles: how tall a pile must be to slow you ({@code mixin/LeafPileSlowMixin},
     * read by the WTHIT Run Speed line in Creative Engineering's {@code surface.StuckInBlock}, which sets {@code surface.LeafPileSlow} from it). Read on every entity-in-pile tick.
     */
    public static final class LeafPiles {
        public final ForgeConfigSpec.IntValue slowFromLayers;

        private LeafPiles(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Immersive Weathering leaf piles (1-8 layers). Their slowdown is still IW's own (on/off: IW",
                    "leaf_piles_slow; Depth Strider, foxes and bees walk through); this sets the height it starts at."
            ).push("leaf_piles");

            slowFromLayers = b.comment(
                    "Layers a leaf pile needs before it slows walking, and before WTHIT shows a Run Speed line for it.",
                    "IW's own rule is 4 (more than 3 layers). 9 = piles never slow. Movement is worked out on the",
                    "client too, so keep this the same on the server and every client."
            ).defineInRange("slowFromLayers", 4, 1, 9);

            b.pop(); // leaf_piles
        }
    }

    /** Biome-tinted moss block and moss carpet ({@code block.MossTint}). Client side only. */
    public static final class Moss {
        public final ForgeConfigSpec.BooleanValue biomeTint;
        public final ForgeConfigSpec.DoubleValue tintStrength;

        private Moss(ForgeConfigSpec.Builder b) {
            b.comment("Moss block and moss carpet colour (client only; the server ignores this section).").push("moss");

            biomeTint = b.comment(
                    "On: moss block and moss carpet take the biome's grass colour, like grass blocks.",
                    "Off: vanilla's fixed green moss. Read when models load: press F3+T or restart after changing it."
            ).define("biomeTint", true);

            tintStrength = b.comment(
                    "How far moss follows the grass colour. 1.0 = shifts as much as grass does from Lush Caves",
                    "grass (where moss keeps its vanilla green); 0.0 = vanilla green everywhere. Chunks pick up",
                    "a change when they re-render (F3+A)."
            ).defineInRange("tintStrength", 1.0, 0.0, 1.0);

            b.pop(); // moss
        }
    }

    /**
     * Shears stop vines growing ({@code block.VineShearing}, {@code mixin/VineBlockShearMixin},
     * {@code block.VineShearingClient}). The sheared state itself is always registered.
     */
    public static final class VineShearing {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.BooleanValue cutBelow;
        public final ForgeConfigSpec.BooleanValue trimmedLook;

        private VineShearing(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Shears on a modded vine (Floral Enchantment's flower vines, Botania's solid vine) stop it growing,",
                    "as Quark's Shear Vines does for the vanilla vine. Shears on the tip of a weeping, twisting or cave",
                    "vine or kelp stop it growing longer (1.20's behaviour)."
            ).push("vine_shearing");

            enabled = b.comment("Shears stop vines growing. Off: shears do nothing to vines (vines already sheared stay so).")
                    .define("enabled", true);
            cutBelow = b.comment("Shearing a modded vine also removes the same vine hanging below it, as Quark does.")
                    .define("cutBelow", true);
            trimmedLook = b.comment(
                    "Client: a sheared modded vine is drawn with its bottom trimmed, like Quark's cut vine. Read when",
                    "models load: press F3+T or restart after changing it."
            ).define("trimmedLook", true);

            b.pop(); // vine_shearing
        }
    }

    /** Quark monster boxes as vaults ({@code mobs.MonsterBoxVault}, feature_writeup/mob_block_tweaks.md section 2.9). */
    public static final class MonsterBox {
        public final ForgeConfigSpec.BooleanValue vaultMode;
        public final ForgeConfigSpec.IntValue cooldownTicks;
        public final ForgeConfigSpec.ConfigValue<String> rewardTable;
        public final ForgeConfigSpec.BooleanValue rewardSingleCount;
        public final ForgeConfigSpec.IntValue leashRange;
        public final ForgeConfigSpec.IntValue checkIntervalTicks;

        private MonsterBox(ForgeConfigSpec.Builder b) {
            b.comment("Quark monster boxes (server side).").push("monster_box");

            vaultMode = b.comment(
                    "true: a box stays after it spawns its wave, ejects one reward when the wave is gone, then waits",
                    "cooldownTicks before it can trigger again; it only breaks while sneaking, and Create drills skip it.",
                    "false: Quark's own box (breaks itself after spawning)."
            ).define("vaultMode", true);
            cooldownTicks = b.comment("Game ticks between the reward and the box re-arming (72000 = 1 hour).")
                    .defineInRange("cooldownTicks", 72000, 20, Integer.MAX_VALUE);
            rewardTable = b.comment("Loot table rolled once for the reward; one stack of the result is ejected.")
                    .define("rewardTable", "creativeengineering:misc/monster_box_reward");
            rewardSingleCount = b.comment("true: the ejected stack is cut to 1 item; false: the stack as the table rolled it.")
                    .define("rewardSingleCount", false);
            leashRange = b.comment("A wave mob farther than this many blocks from its box counts as gone.")
                    .defineInRange("leashRange", 24, 4, 256);
            checkIntervalTicks = b.comment("Ticks between checks of an active box's wave.")
                    .defineInRange("checkIntervalTicks", 20, 1, 200);

            b.pop(); // monster_box
        }
    }

    /**
     * Which screens show TrashSlot's trash slot by default, read on the client by
     * {@code compat.TrashSlotCompat}. TrashSlot itself only turns the slot on for the player
     * inventory, crafting table and chests; everything else starts hidden until the player presses
     * the toggle key. A screen listed here is switched on once per client (the first time it opens
     * after being listed), so a player's later toggle still sticks.
     */
    public static final class TrashSlot {
        /** Vanilla workstations, both inventories, chests and Sophisticated Backpacks. */
        public static final List<String> DEFAULT_SCREENS = List.of(
                "net.minecraft.client.gui.screens.inventory.InventoryScreen",
                "net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen",
                "net.minecraft.client.gui.screens.inventory.ContainerScreen",
                "net.minecraft.client.gui.screens.inventory.CraftingScreen",
                "net.minecraft.client.gui.screens.inventory.FurnaceScreen",
                "net.minecraft.client.gui.screens.inventory.BlastFurnaceScreen",
                "net.minecraft.client.gui.screens.inventory.SmokerScreen",
                "net.minecraft.client.gui.screens.inventory.AnvilScreen",
                "net.minecraft.client.gui.screens.inventory.SmithingScreen",
                "net.minecraft.client.gui.screens.inventory.GrindstoneScreen",
                "net.minecraft.client.gui.screens.inventory.StonecutterScreen",
                "net.minecraft.client.gui.screens.inventory.LoomScreen",
                "net.minecraft.client.gui.screens.inventory.CartographyTableScreen",
                "net.minecraft.client.gui.screens.inventory.EnchantmentScreen",
                "net.minecraft.client.gui.screens.inventory.BrewingStandScreen",
                "net.p3pp3rf1y.sophisticatedbackpacks.client.gui.BackpackScreen"
        );

        /**
         * Starting spots that differ from TrashSlot's (under the panel's bottom-right corner).
         * Creative: right edge, bottom, because the bottom-right spot is the Survival Inventory tab.
         * Backpacks: anchored to the bottom-right corner itself, so the spot follows the panel at
         * every backpack size.
         */
        public static final List<String> DEFAULT_POSITIONS = List.of(
                "net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen|1.0|1.0|0|-28",
                "net.p3pp3rf1y.sophisticatedbackpacks.client.gui.BackpackScreen|1.0|1.0|-28|0"
        );

        public final ForgeConfigSpec.ConfigValue<List<? extends String>> enabledScreens;
        public final ForgeConfigSpec.BooleanValue allContainers;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> excludedScreens;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> positions;
        public final ForgeConfigSpec.BooleanValue splitBackpacksByTier;

        private TrashSlot(ForgeConfigSpec.Builder b) {
            b.comment(
                    "TrashSlot defaults (client side). Screens are full Java class names; F3 can't show them,",
                    "but TrashSlotSaveState.json in the game folder lists every screen opened so far."
            ).push("trash_slot");

            enabledScreens = b.comment(
                    "Screens that show the trash slot by default. Each one is switched on once per client, the",
                    "first time it opens after being added here; pressing the toggle key afterwards still sticks."
            ).defineList("enabledScreens", DEFAULT_SCREENS, o -> o instanceof String);

            allContainers = b.comment(
                    "true: every other container screen (modded machines, storage, ...) also shows it by default,",
                    "except the ones in excludedScreens. false: only enabledScreens do."
            ).define("allContainers", false);

            excludedScreens = b.comment("Never switched on by allContainers.")
                    .defineList("excludedScreens", List.of(
                            "net.p3pp3rf1y.sophisticatedbackpacks.client.gui.BackpackSettingsScreen"
                    ), o -> o instanceof String);

            positions = b.comment(
                    "Starting position for a screen the player hasn't opened yet: 'class|anchorX|anchorY|x|y'.",
                    "The anchor is a fraction of the panel (0.5 = centre, 1.0 = right/bottom edge) and x/y are",
                    "pixels from it to the slot's top-left; the slot is 28x28. Screens not listed start under the",
                    "panel's bottom-right corner. Dragging the slot in game overrides this."
            ).defineList("positions", DEFAULT_POSITIONS, o -> o instanceof String);

            splitBackpacksByTier = b.comment(
                    "true: each Sophisticated Backpacks tier (leather, copper, iron, gold, diamond, netherite)",
                    "remembers its own trash slot position and on/off state. false: all backpacks share one."
            ).define("splitBackpacksByTier", true);

            b.pop(); // trash_slot
        }
    }

    /**
     * Pet cats with Domestication Innovation's Feather on a Stick ({@code cats.CatFeather}, feature_writeup/cozy_cats.md
     * section 10). Server side; read live.
     */
    public static final class Cats {
        public final ForgeConfigSpec.BooleanValue featherHooksMobs;
        public final ForgeConfigSpec.BooleanValue hookHostile;
        public final ForgeConfigSpec.BooleanValue hookNeutralAndPassive;
        public final ForgeConfigSpec.BooleanValue hookPlayers;
        public final ForgeConfigSpec.IntValue buffSeconds;
        public final ForgeConfigSpec.IntValue buffLevel;

        private Cats(ForgeConfigSpec.Builder b) {
            b.comment("Pet cats and the Feather on a Stick (server side).").push("cats");

            featherHooksMobs = b.comment(
                    "true: a cast Feather on a Stick sticks to a mob it hits, like a fishing hook (reeling in lets go, no",
                    "pull), and the caster's tamed cats nearby attack the hooked mob while it stays hooked. Sitting cats and",
                    "Cozy's Improved Cats cats in Stay mode do not join. false: the feather passes through mobs (Domestication",
                    "Innovation's own behaviour)."
            ).define("featherHooksMobs", true);
            hookHostile = b.comment("Hostile mobs (monsters that are not neutral) can be hooked.")
                    .define("hookHostile", true);
            hookNeutralAndPassive = b.comment(
                    "Peaceful and neutral mobs (animals, villagers, iron golems, wolves, endermen, zombified piglins, ...) can",
                    "be hooked. The caster's own pets never can."
            ).define("hookNeutralAndPassive", false);
            hookPlayers = b.comment(
                    "Players other than the caster can be hooked, and the caster's cats attack them. Independent of the",
                    "server's pvp setting."
            ).define("hookPlayers", false);
            buffSeconds = b.comment(
                    "A cat that lands an attack on a mob hooked by its owner's feather gets Strength, Resistance and",
                    "Regeneration for this many seconds (0: no effects)."
            ).defineInRange("buffSeconds", 10, 0, 600);
            buffLevel = b.comment("Level of those three effects (3 = III).")
                    .defineInRange("buffLevel", 3, 1, 10);

            b.pop(); // cats
        }
    }

    /**
     * Alex's Mobs giant squid fleeing cachalot whales ({@code mixin/AmGiantSquidAvoidWhalesMixin}, feature_writeup/
     * mob_block_tweaks.md section 2.10). Alex's Mobs flees from any whale within 50 blocks and keeps fleeing while
     * one is within 60. Server side; read each time the goal checks.
     */
    public static final class GiantSquid {
        public final ForgeConfigSpec.IntValue whaleFleeStart;
        public final ForgeConfigSpec.IntValue whaleFleeStop;

        private GiantSquid(ForgeConfigSpec.Builder b) {
            b.comment("Alex's Mobs giant squid (server side).").push("giant_squid");

            whaleFleeStart = b.comment(
                    "A giant squid starts fleeing a cachalot whale within this many blocks (Alex's Mobs: 50)."
            ).defineInRange("whaleFleeStart", 16, 1, 64);
            whaleFleeStop = b.comment(
                    "It keeps fleeing while that whale is within this many blocks (Alex's Mobs: 60). Kept at least",
                    "whaleFleeStart."
            ).defineInRange("whaleFleeStop", 24, 1, 96);

            b.pop(); // giant_squid
        }

        /** The start radius, or Alex's Mobs' 50 before the config loads. */
        public static float start() {
            return SPEC.isLoaded() ? GIANT_SQUID.whaleFleeStart.get() : 50.0F;
        }

        /** The stop radius (never below the start), or Alex's Mobs' 60 before the config loads. */
        public static float stop() {
            return SPEC.isLoaded() ? Math.max(GIANT_SQUID.whaleFleeStop.get(), GIANT_SQUID.whaleFleeStart.get()) : 60.0F;
        }
    }

    /**
     * Immersive Paintings' Datapacks page entries for modded paintings, built from the installed jars
     * ({@code paintings/PaintingPack}, feature_writeup/immersive_paintings.md). Read on each data load (world start,
     * /reload); a changed list is rebuilt then.
     */
    public static final class Paintings {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> extraSources;

        private Paintings(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Immersive Paintings: every modded painting variant is offered on its Datapacks page, built from the",
                    "art in the installed mod jars when the world's data loads (server side). Vanilla paintings come from",
                    "the pack's own datapack, not from here."
            ).push("paintings");

            enabled = b.comment("Build the modded paintings set.").define("enabled", true);
            extraSources = b.comment(
                    "More places to read painting art from (assets/<mod>/textures/painting/<id>.png and the en_us lang),",
                    "after the installed mods; a later entry wins for the same painting. Paths are relative to the game",
                    "folder (or absolute). Each entry is a jar or zip (a painting mod, a resource pack that retextures",
                    "paintings), a folder with assets/ in it, or a folder of jars and zips.",
                    "Example: [\"resourcepacks/My Paintings.zip\", \"painting_jars\"]"
            ).defineListAllowEmpty(List.of("extraSources"), List::of, o -> o instanceof String);

            b.pop(); // paintings
        }

        /** On, or true before the config loads. */
        public static boolean enabled() {
            return !SPEC.isLoaded() || PAINTINGS.enabled.get();
        }

        /** The extra sources, or none before the config loads. */
        public static List<String> extraSources() {
            if (!SPEC.isLoaded()) return List.of();
            return PAINTINGS.extraSources.get().stream().map(String::valueOf).toList();
        }
    }
}
