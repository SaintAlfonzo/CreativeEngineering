package com.SaintAlfonzo.creativecompat;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.SaintAlfonzo.creativecompat.surface.SurfaceTier;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Creative Compat's COMMON config, {@code config/creativecompat-common.toml}. The sections moved out of patcherMod's
 * creativeengineering-common.toml on 2026-11-01 with keys, comments and defaults unchanged; the first boot with
 * Compat copies a player's values over ({@code swap.ConfigRename.carrySections}, called from {@link CreativeCompat}).
 * {@code [shrapnel]}, {@code [surface]} and {@code [vein_undo]} came from Creative Engineering's file (same name,
 * creativeengineering-common.toml) on 2026-11-06 with the TNT shrapnel, walk speed and Ore Excavation undo code; keys,
 * comments and defaults unchanged, carried or merged the same way ({@link #ENGINEERING_SECTIONS}).
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
    public static final Shrapnel SHRAPNEL;
    public static final Surface SURFACE;
    public static final VeinUndo VEIN_UNDO;
    public static final Jei JEI;
    public static final GuiTitle GUI_TITLE;
    public static final Toasts TOASTS;
    public static final Tooltips TOOLTIPS;
    public static final Coaster COASTER;
    public static final Wthit WTHIT;
    public static final ImmersivePortals IMMERSIVE_PORTALS;
    public static final PortalGun PORTAL_GUN;

    /** Sections taken over from Creative Common's creativecommon-common.toml (interface carve, 2026-11-07). */
    public static final List<String> COMMON_SECTIONS = List.of("jei", "gui_title", "toasts", "tooltips", "coaster", "wthit");

    /** How the WTHIT Run Speed line writes a multiplier: 1.50x or +50%. */
    public enum SpeedFormat { MULTIPLIER, PERCENT }

    /**
     * The toml tables carried over from creativeengineering-common.toml, in order. {@code [cats]} (added 2026-11-01,
     * after the carve) and {@code [giant_squid]} (2026-11-02) are new in this file and are not carried. {@code [campfire]}
     * moved on to Creative Delight's file on 2026-11-04 (creative_split.md 7.9 step 1), which carries it from here.
     */
    public static final List<String> SECTIONS =
            List.of("leaf_piles", "moss", "vine_shearing", "monster_box", "trash_slot");

    /** The tables that came from Creative Engineering's creativeengineering-common.toml on 2026-11-06. */
    public static final List<String> ENGINEERING_SECTIONS = List.of("shrapnel", "surface", "vein_undo");

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
        SHRAPNEL = new Shrapnel(builder);
        SURFACE = new Surface(builder);
        VEIN_UNDO = new VeinUndo(builder);
        // from creativecommon-common.toml (interface carve, 2026-11-07)
        JEI = new Jei(builder);
        GUI_TITLE = new GuiTitle(builder);
        TOASTS = new Toasts(builder);
        TOOLTIPS = new Tooltips(builder);
        COASTER = new Coaster(builder);
        WTHIT = new Wthit(builder);
        // Immersive Portals and the Portal Gun (feature_writeup/plan_portal_changes.md, 2026-11-07)
        IMMERSIVE_PORTALS = new ImmersivePortals(builder);
        PORTAL_GUN = new PortalGun(builder);
        SPEC = builder.build();
    }

    private CompatConfig() {}

    /**
     * Immersive Weathering leaf piles: how tall a pile must be to slow you ({@code mixin/LeafPileSlowMixin},
     * read by the WTHIT Run Speed line in {@code surface.StuckInBlock} through {@code surface.LeafPileSlow}, set from it in {@link CreativeCompat}). Read on every entity-in-pile tick.
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

    /**
     * Dials for TNT shrapnel ({@code shrapnel.ShrapnelEntity} / {@code ShrapnelSpawner}) -
     * fired both when a primed TNT entity detonates ({@code shrapnel.TntShrapnelHandler}, a
     * plain Forge {@code ExplosionEvent.Detonate} listener) and when a dropped TNT item is
     * consumed by Creative Compat's {@code itemphysic.ItemBurnTransform} EXPLODE rule (through {@code util.BurnBursts})
     * ({@code global.itemPhysics.burnExplode('minecraft:tnt', ..., true)} in
     * {@code kubejs_startup/creativecompat/item_physics.js} opts a rule into shrapnel).
     */
    public static final class Shrapnel {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.IntValue count;
        public final ForgeConfigSpec.DoubleValue speed;
        public final ForgeConfigSpec.DoubleValue speedVariance;
        public final ForgeConfigSpec.DoubleValue verticalAngle;
        public final ForgeConfigSpec.DoubleValue maxDistance;
        public final ForgeConfigSpec.DoubleValue damage;
        public final ForgeConfigSpec.ConfigValue<String> item;

        private Shrapnel(ForgeConfigSpec.Builder b) {
            b.comment(
                    "TNT shrapnel: when a primed TNT entity or a burning TNT item explodes, it also",
                    "flings out a burst of fast, gravity-immune projectiles mostly along the horizontal",
                    "plane (with some vertical variation) that despawn once they've travelled far enough."
            ).push("shrapnel");

            enabled = b.comment("Master on/off switch for TNT shrapnel.")
                    .define("enabled", true);

            count = b.comment("Number of shrapnel projectiles spawned per TNT explosion, spread evenly around the full circle.")
                    .defineInRange("count", 24, 0, 512);

            speed = b.comment("Base shrapnel speed, in blocks/tick.")
                    .defineInRange("speed", 1.4, 0.0, 10.0);

            speedVariance = b.comment(
                    "Random speed variation as a fraction of the base speed (e.g. 0.25 = each piece",
                    "gets a random speed between 75% and 125% of speed)."
            ).defineInRange("speedVariance", 0.25, 0.0, 1.0);

            verticalAngle = b.comment(
                    "Max vertical launch angle in degrees, either side of dead-level (0 = perfectly",
                    "horizontal, 90 = could fire straight up/down). Each piece rolls an independent",
                    "random angle in [-verticalAngle, +verticalAngle]."
            ).defineInRange("verticalAngle", 15.0, 0.0, 90.0);

            maxDistance = b.comment("Distance in blocks a shrapnel piece travels before it despawns.")
                    .defineInRange("maxDistance", 12.0, 0.5, 128.0);

            damage = b.comment("Damage dealt to whatever a shrapnel piece hits.")
                    .defineInRange("damage", 3.0, 0.0, 100.0);

            item = b.comment("Item id rendered as the flying shrapnel piece.")
                    .define("item", "minecraft:iron_nugget");

            b.pop(); // shrapnel
        }
    }

    /**
     * Everything about walking on blocks ({@code surface/} package): BlockRunner's tier speeds,
     * sticky facing, camouflage and visual sinking, all under one {@code [surface]} section (one
     * folder in Create's Configs screen). {@code [surface.speed]} is generated from
     * {@link SurfaceTier}, so a new tier shows up here with no edits to this class.
     */
    public static final class Surface {
        public final Map<SurfaceTier, ForgeConfigSpec.DoubleValue> speed = new EnumMap<>(SurfaceTier.class);
        public final Sticky sticky;
        public final Camouflage camouflage;
        public final Sinking sinking;
        public final Wthit wthit;

        private Surface(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Walking on blocks: speed per surface tier (BlockRunner), sticky pistons, framed / copycat",
                    "blocks and visual sinking. Tier contents are the blockrunner:surface/* block tags",
                    "(kubejs blocktags.js). Keep this section the same on server and client, apart from",
                    "[surface.sinking], which is client-side only."
            ).push("surface");

            b.comment(
                    "Walk-speed multiplier per surface tier. 1.0 = normal speed, 1.5 = 50% faster, 0.5 = half.",
                    "These replace the tier entries in config/blockrunner.json, which now only holds one-off",
                    "block overrides. The server's values are sent to players. Saving the file re-applies",
                    "them straight away; you feel a change after stepping onto a new block."
            ).push("speed");
            for (SurfaceTier t : SurfaceTier.values()) {
                speed.put(t, b.comment(t.description()).defineInRange(t.key(), t.defaultSpeed(), 0.05, 4.0));
            }
            b.pop(); // speed

            sticky = new Sticky(b);
            camouflage = new Camouflage(b);
            sinking = new Sinking(b);
            wthit = new Wthit(b);

            b.pop(); // surface
        }

        /** Dials for {@code surface.StickyFacing}. */
        public static final class Sticky {
            public final ForgeConfigSpec.BooleanValue faceUpOnly;

            private Sticky(ForgeConfigSpec.Builder b) {
                b.comment("The sticky tier (sticky pistons, piston heads, sticky launchers).").push("sticky");

                faceUpOnly = b.comment(
                        "true: a sticky piston only slows you while its sticky face points up (a piston head",
                        "only while it's a sticky head). false: every block in the tier slows you whichever way it faces."
                ).define("faceUpOnly", true);

                b.pop(); // sticky
            }
        }

        /** Dials for {@code surface.CamoSpeed}. */
        public static final class Camouflage {
            public final ForgeConfigSpec.BooleanValue copyCamo;

            private Camouflage(ForgeConfigSpec.Builder b) {
                b.comment("Walk speed on framed / copycat blocks (FramedBlocks, Copycats+, Create copycats).")
                        .push("camouflage");

                copyCamo = b.comment(
                        "true: walk like the camo block (its own tier speed). Hybrid blocks holding several",
                        "materials use the one inserted most recently. false: the camouflage tier's flat speed.",
                        "Independent of [surface.sinking] copyCamo."
                ).define("copyCamo", true);

                b.pop(); // camouflage
            }
        }

        /** Dials for {@code surface.SinkingRender} (client-side, visual only). */
        public static final class Sinking {
            public final ForgeConfigSpec.IntValue depthPixels;
            public final ForgeConfigSpec.BooleanValue copyCamo;

            private Sinking(ForgeConfigSpec.Builder b) {
                b.comment(
                        "Visual soul-sand-style sinking: entities standing on a full block in the soft, dust,",
                        "spongy or very_sticky tier (#creativeengineering:sinking) are drawn lower, and so is the",
                        "camera. Collision, hitboxes, movement, mob AI and shadows are untouched."
                ).push("sinking");

                depthPixels = b.comment("How far the camera and entities appear to sink, in 1/16-block pixels (soul sand's real sink is 2). 0 = off.")
                        .defineInRange("depthPixels", 2, 0, 8);

                copyCamo = b.comment(
                        "true: full framed / copycat blocks (cubes, hybrid double slabs, anything with a full",
                        "block shape) sink like their camo, the newest one in a hybrid. Frames that aren't a",
                        "full block never sink, same as any other block. false: frames never sink.",
                        "Independent of [surface.camouflage] copyCamo."
                ).define("copyCamo", true);

                b.pop(); // sinking
            }
        }

        /** Dials for {@code surface.WthitRunSpeed} (client-side, display only). */
        public static final class Wthit {
            public final ForgeConfigSpec.BooleanValue enabled;
            public final ForgeConfigSpec.BooleanValue hideUntyped;
            public final ForgeConfigSpec.BooleanValue hideNormal;
            public final ForgeConfigSpec.BooleanValue showTier;
            public final ForgeConfigSpec.EnumValue<SpeedFormat> format;

            private Wthit(ForgeConfigSpec.Builder b) {
                b.comment(
                        "A 'Run Speed' line in WTHIT's look-at overlay: the speed you'd walk at on that block, by",
                        "the same rules as movement (camo and sticky facing included). Blocks you pass through",
                        "(plants) get no line unless they slow you from inside (cobwebs, berry bushes, leaf piles);",
                        "thin layers (carpet, 1-2 snow layers) show the block beneath, which is the one you walk on.",
                        "Client-side only; it reads BlockRunner's own speed table, no server traffic."
                ).push("wthit");

                enabled = b.comment("Master on/off switch for the line.")
                        .define("enabled", true);

                hideUntyped = b.comment(
                        "true: no line on blocks nothing gives a speed to: no surface tier, no blockrunner.json",
                        "entry, no speed of their own, and they don't slow you from inside. Those are always 1.0,",
                        "so this only matters with hideNormal = false: tiered 1.0 blocks (Glass, Dirt) still show."
                ).define("hideUntyped", true);

                hideNormal = b.comment("true: no line on any block at normal speed (1.0), tiered or not.")
                        .define("hideNormal", true);

                showTier = b.comment(
                        "true: name the surface tier after the value, e.g. 'Run Speed: 1.50\u00d7 (Asphalt)', and add",
                        "notes: 'from camo', 'sticky face not up', 'block below' (thin layers), 'own speed'",
                        "(soul sand), 'walking through' (cobwebs and other blocks that slow you from inside)."
                ).define("showTier", true);

                format = b.comment("MULTIPLIER: 1.50\u00d7 / 0.88\u00d7. PERCENT: +50% / -12%.")
                        .defineEnum("format", SpeedFormat.MULTIPLIER);

                b.pop(); // wthit
            }
        }
    }

    /** Rules on Ore Excavation's {@code /undo_excavation} ([[excavation.VeinUndo]]). */
    public static final class VeinUndo {
        public final ForgeConfigSpec.IntValue cooldownSeconds;
        public final ForgeConfigSpec.BooleanValue denyBlockEntities;

        private VeinUndo(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Ore Excavation's /undo_excavation. An undo is refused when the vein mine broke a block in",
                    "#creativeengineering:vein_undo_denied (ores, storage blocks, budding blocks, spawners, ancient",
                    "debris; filled by KubeJS). Server side."
            ).push("vein_undo");

            cooldownSeconds = b.comment("Seconds a player waits after a successful undo before the next one. 0 = none.")
                    .defineInRange("cooldownSeconds", 5, 0, 3600);

            denyBlockEntities = b.comment(
                    "Also refuse when the vein mine broke a block with a block entity (chests, furnaces...).",
                    "A restored chest keeps its unrolled loot table, so mine-and-undo would reroll structure loot."
            ).define("denyBlockEntities", true);

            b.pop();
        }
    }

    /**
     * Dials for {@code compat.jei.CraftableFirstRecipeLayoutList}, a backport of JEI 19's
     * "Show craftable recipes first" sorting to JEI 11.8.1 (port notes:
     * feature_writeup/jei_craftable_first_port.md), and for {@code compat.jei.PackIngredientOrder}, the pack's
     * order of the ingredient list (feature_writeup/jei_ordering.md). Client-side only; a server ignores it.
     */
    public static final class Jei {
        public final ForgeConfigSpec.BooleanValue craftableFirst;
        public final ForgeConfigSpec.BooleanValue packOrder;
        public final ForgeConfigSpec.BooleanValue orderReport;
        public final ForgeConfigSpec.BooleanValue seasoningCategory;
        public final ForgeConfigSpec.BooleanValue potionBucketCategory;
        public final ForgeConfigSpec.BooleanValue tippedArrowCategory;
        public final ForgeConfigSpec.BooleanValue canCategories;
        public final ForgeConfigSpec.EnumValue<FluidScale> fluidAmountScale;

        /** How JEI tanks turn a fluid amount into a fill height ({@code mixin.JeiFluidTankScaleMixin}). */
        public enum FluidScale { LOG, SQRT, LINEAR }

        private Jei(ForgeConfigSpec.Builder b) {
            b.comment("JEI recipe screen tweaks (client side).").push("jei");

            craftableFirst = b.comment(
                    "When the recipe screen is opened over a screen that can take recipe transfers (crafting table,",
                    "inventory, modded crafting GUIs), list the recipes whose + button is enabled first. The rest follow,",
                    "most-complete first (the share of inputs you already have). Other screens keep JEI's own order.",
                    "Takes effect the next time a recipe lookup opens."
            ).define("craftableFirst", true);

            packOrder = b.comment(
                    "Order JEI's ingredient list by the pack's definition (kubejs/startup_scripts/jei/order.js) and its",
                    "creativecompat:jei/... tags: by type (items, multiblocks, fluids, effects), then category, then",
                    "each category's own order. Ingredients with no place keep JEI's own order ([sorting] in",
                    "config/jei/jei-client.ini) after the placed ones of their type. Off: JEI's own order only.",
                    "Takes effect the next time JEI starts (rejoin or /reload). Default off (JEI's order); the pack sets true."
            ).define("packOrder", false);

            orderReport = b.comment(
                    "Write kubejs/exported/jei_order.json at every JEI start: every item, fluid, effect and multiblock with",
                    "its place in the pack order and whether JEI shows it (shown / hidden by the pack / hidden by a mod /",
                    "hidden in JEI's edit mode / never in JEI), plus problems in the definition. Render it with",
                    "python3 tools/jei_order_report.py. Off by default: it takes a moment and a few MB."
            ).define("orderReport", false);

            seasoningCategory = b.comment(
                    "Show seasoning crafts (Salt's food + salt, Create: The Salt's salting mixes, and crafting recipes",
                    "with ids creativeengineering:shapeless/season/...) on their own Seasoning page instead of the",
                    "Crafting and Mixing pages. Off: they stay where their mods put them.",
                    "Takes effect the next time JEI starts (rejoin or /reload)."
            ).define("seasoningCategory", true);

            potionBucketCategory = b.comment(
                    "Show the potion bucket recipes on one Potion Buckets page per potion and bucket: filling and",
                    "draining by hand, the Spout, the Item Drain and the Bottling Machine together. Off: they stay on the",
                    "Crafting, Spout Filling, Item Draining and Bottling Machine pages.",
                    "Takes effect the next time JEI starts (rejoin or /reload)."
            ).define("potionBucketCategory", true);

            tippedArrowCategory = b.comment(
                    "Show tipped arrow recipes on one Tipped Arrows page per potion: by hand (8 arrows + a lingering",
                    "potion), the Spout and the Bottling Machine together. Off: they stay on the Crafting, Spout Filling",
                    "and Bottling Machine pages.",
                    "Takes effect the next time JEI starts (rejoin or /reload)."
            ).define("tippedArrowCategory", true);

            canCategories = b.comment(
                    "Show can crafts on two pages of their own: Canning (food + an empty can -> a can) and Using Cans",
                    "(crafts that take a can). Off: they stay on the Crafting page.",
                    "Takes effect the next time JEI starts (rejoin or /reload)."
            ).define("canCategories", true);

            fluidAmountScale = b.comment(
                    "How full a fluid looks in JEI tanks (every mod's recipe pages and the pack's own) for its amount.",
                    "LOG: small amounts stay visible and 250, 1000 and 8000 mB look different. SQRT: a milder curve.",
                    "LINEAR: JEI's own (fill = amount / tank size, so small amounts are a sliver).",
                    "Takes effect at once."
            ).defineEnum("fluidAmountScale", FluidScale.LOG);

            b.pop(); // jei
        }
    }

    /**
     * Dials for {@code compat.guititle.TitlePlaques}: stretches a GUI resource pack's title plaque
     * (Engineers GUIs) to fit long container titles. Client-side only; a server ignores it.
     */
    public static final class GuiTitle {
        public final ForgeConfigSpec.BooleanValue stretchPlaque;
        public final ForgeConfigSpec.BooleanValue logOverflow;

        private GuiTitle(ForgeConfigSpec.Builder b) {
            b.comment("Container title plaques from GUI resource packs (client side).").push("gui_title");

            stretchPlaque = b.comment(
                    "When a container title is wider than the plaque the resource pack painted behind it, stretch the",
                    "plaque to fit. Titles wider than the whole GUI are cut short with \"...\". Only screens that draw",
                    "their title the vanilla way (chests, barrels, shulker boxes, hoppers, horses, dispensers, furnaces)."
            ).define("stretchPlaque", true);

            logOverflow = b.comment(
                    "Record every title that needed a stretch in kubejs/exported/gui_title_overflow.json",
                    "(screen class, texture, title and widths; kept across sessions)."
            ).define("logOverflow", true);

            b.pop(); // gui_title
        }
    }

    /**
     * Dials for {@code mixin/ToastFilterMixin}: drops whole families of toasts before they reach the
     * toast queue. Client-side only; a server ignores it. Covers every player, unlike Rubidium Extra's
     * own toggles (config/sodium-extra-options.json is a player file the deploy doesn't ship).
     */
    public static final class Toasts {
        public final ForgeConfigSpec.BooleanValue tutorial;
        public final ForgeConfigSpec.BooleanValue advancement;

        private Toasts(ForgeConfigSpec.Builder b) {
            b.comment("Pop-up toasts in the top-right corner (client side).").push("toasts");

            tutorial = b.comment(
                    "Show tutorial toasts: vanilla's (move, find a tree, punch, craft planks, open inventory,",
                    "social interactions, bundles), Exposure's camera/lightroom hints and Fairy Lights' Clippy",
                    "balloons. Takes effect for the next toast. Default on (vanilla); the pack sets false."
            ).define("tutorial", true);

            advancement = b.comment(
                    "Show advancement toasts (vanilla's \"Advancement Made!\" and Patchouli's book-unlocked toast).",
                    "Only matters while [advancements] enabled is on. Default on (vanilla); the pack sets false."
            ).define("advancement", true);

            b.pop(); // toasts
        }
    }

    /**
     * Item tooltip dials (client side; a server ignores them): {@code mixin/ScreenTooltipTopMixin} and
     * {@code compat/CuriosSlotTooltip} (feature_writeup/tooltips.md 3).
     */
    public static final class Tooltips {
        public final ForgeConfigSpec.BooleanValue keepTopOnScreen;

        private Tooltips(ForgeConfigSpec.Builder b) {
            b.comment("Item tooltips (client side).").push("tooltips");

            keepTopOnScreen = b.comment(
                    "A tooltip taller than the screen starts at the top and runs off the bottom (vanilla keeps the",
                    "bottom on screen and cuts off the name). Takes effect on the next frame."
            ).define("keepTopOnScreen", true);

            b.pop(); // tooltips
        }
    }

    /**
     * Brewin' and Chewin' coaster (the port) extras: {@code compat.WthitCoaster}. Which items go on a coaster is the
     * port's own {@code [coaster] taggedItemsOnly} in brewinandchewin-common.toml; feature_writeup/alcohol.md 12.
     */
    public static final class Coaster {
        public final ForgeConfigSpec.BooleanValue wthit;

        private Coaster(ForgeConfigSpec.Builder b) {
            b.comment("Brewin' and Chewin' coasters (client side).").push("coaster");

            wthit = b.comment(
                    "WTHIT lines on a coaster: 'Coaster: 3 / 4' ('(put away)' once the coaster itself was taken",
                    "back) and one line per item on it, in the order they went on."
            ).define("wthit", true);

            b.pop(); // coaster
        }
    }

    /**
     * The pack's WTHIT lines that have no feature section of their own (client side, display only):
     * {@code compat.WthitBlockModelIcon}, {@code compat.WthitHolderContents}, {@code compat.WthitHive},
     * {@code compat.WthitStateLines}, {@code compat.WthitCampfire}, {@code compat.WthitSheetmetalTank},
     * {@code compat.WthitKitchenStation}, {@code compat.WthitSheared}, {@code compat.WthitSilkTouch},
     * {@code compat.WthitCropSeasons}, {@code compat.WthitOperatorBlocks}. Every change to WTHIT's overlay has a switch here.
     * feature_writeup/wthit_lines.md. Each is also a WTHIT plugin toggle (config/waila/plugin_toggle.json5).
     */
    public static final class Wthit {
        public final ForgeConfigSpec.BooleanValue iconBlockModel;
        public final ForgeConfigSpec.BooleanValue iconForgePick;
        public final ForgeConfigSpec.BooleanValue iconSubstitutes;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> iconExclude;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> iconItems;
        public final ForgeConfigSpec.BooleanValue holderContents;
        public final ForgeConfigSpec.BooleanValue potContents;
        public final ForgeConfigSpec.BooleanValue holderPotionEffects;
        public final ForgeConfigSpec.BooleanValue holderSingleName;
        public final ForgeConfigSpec.BooleanValue hiveBees;
        public final ForgeConfigSpec.BooleanValue hiveBeesOnTop;
        public final ForgeConfigSpec.BooleanValue hiveHoneyMax;
        public final ForgeConfigSpec.BooleanValue stateLines;
        public final Map<String, ForgeConfigSpec.BooleanValue> stateLine = new java.util.LinkedHashMap<>();
        public final ForgeConfigSpec.BooleanValue campfireBurn;
        public final ForgeConfigSpec.BooleanValue campfireRain;
        public final ForgeConfigSpec.BooleanValue sheetmetalTank;
        public final ForgeConfigSpec.BooleanValue kitchenStation;
        public final ForgeConfigSpec.BooleanValue remainderBuffer;
        public final ForgeConfigSpec.BooleanValue sheared;
        public final ForgeConfigSpec.BooleanValue silkTouch;
        public final ForgeConfigSpec.BooleanValue silkTouchShowFalse;
        public final ForgeConfigSpec.BooleanValue operatorBlocks;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> operatorBlockList;
        public final ForgeConfigSpec.IntValue operatorBlockLevel;

        private Wthit(ForgeConfigSpec.Builder b) {
            b.comment("WTHIT look-at overlay lines (client side). See feature_writeup/wthit_lines.md.").push("wthit");

            iconBlockModel = b.comment(
                    "Block model icons: a block with no item (or whose item draws as the purple/black missing model)",
                    "shows its own block model in WTHIT's icon slot. Off = WTHIT's empty icon, Display Delight included."
            ).define("iconBlockModel", true);

            iconForgePick = b.comment(
                    "Block model icons: before drawing the model, ask Forge's hit-aware pick block (what middle-click",
                    "gives), so a block holding several things shows the part looked at (Supplementaries' skull pile)."
            ).define("iconForgePick", true);

            iconSubstitutes = b.comment("Block model icons: use the iconItems list below.").define("iconSubstitutes", true);

            iconExclude = b.comment(
                    "Block model icons: when the looked-at block has no item, or its item draws as the purple/black",
                    "missing model, WTHIT's icon slot shows the block's own model instead. Blocks listed here keep",
                    "WTHIT's normal icon. Entries: a namespace ('create') or a block id ('create:belt').",
                    "/cdevdumpicons + tools/wthit_icon_audit.py list every block the icon applies to."
            ).defineListAllowEmpty(List.of("iconExclude"), () -> List.of(), o -> o instanceof String s && !s.isBlank());

            iconItems = b.comment(
                    "Block model icons: an item to show for a block that has none and draws no model of its own",
                    "(block-entity rendered). Entries 'block=item'. D&D's powered flywheel is Create's flywheel."
            ).defineListAllowEmpty(List.of("iconItems"), () -> List.of("create_dd:powered_flywheel=create:flywheel"),
                    o -> o instanceof String s && s.contains("="));

            holderContents = b.comment(
                    "Supplementaries jars, goblets and cages: 'Contents: Honey (6 / 12)' (fluid in bottles, with the",
                    "jar's capacity; a goblet just 'Water'), 'Contents: Cookie (5 / 12)', or the mob inside."
            ).define("holderContents", true);

            potContents = b.comment(
                    "Flower pots, flower boxes and planter boxes: 'Contains: Poppy' (every plant inside, a box's plants",
                    "in slot order, '2 Poppy' for a double); a Twigs silt pot 'Rooted Dirt: Yes / No'. A Beautify hanging",
                    "pot standing on the ground is named 'Rich Pot'."
            ).define("potContents", true);

            holderPotionEffects = b.comment(
                    "Under a potion in a jar or goblet, one line per effect with its strength and length, as the",
                    "potion's own tooltip shows them ('Instant Health II', 'Regeneration (0:45)')."
            ).define("holderPotionEffects", true);

            holderSingleName = b.comment(
                    "A one-bottle holder (goblet) shows just the fluid ('Contents: Water'). Off = 'Water (1 / 1)'."
            ).define("holderSingleName", true);

            hiveBees = b.comment(
                    "Beehives and bee nests: 'Bees: 2 / 3'. The count comes from the server (WTHIT and the pack's",
                    "mod on both sides); without it the line is left out."
            ).define("hiveBees", true);

            hiveBeesOnTop = b.comment(
                    "The Bees line takes the place of WTHIT's occupant-names line ('2 Bee') and sits above Honey Level.",
                    "Off = WTHIT's names line stays and Bees goes at the end."
            ).define("hiveBeesOnTop", true);

            hiveHoneyMax = b.comment(
                    "Beehives and bee nests: WTHIT's 'Honey Level: 3' becomes 'Honey Level: 3 / 5'. Still follows",
                    "WTHIT's own 'Show Beehive Honey Level' option."
            ).define("hiveHoneyMax", true);

            stateLines = b.comment(
                    "Numbers kept in block state: 'Servings: 3 / 4' (feasts), 'Slices: 5 / 7' (cakes, pies), 'Eggs' and",
                    "'Hatching' (nests, turtle eggs), 'Level' (cauldrons WTHIT has no fluid bar for), anchor charges,",
                    "sea pickles. Rules in assets/creativecompat/state_lines.json (override from kubejs/assets)."
            ).define("stateLines", true);
            b.comment("One switch per state line (a label added by an overriding state_lines.json is always on).").push("state_lines");
            for (String[] line : new String[][] {
                    {"servings", "'Servings: 3 / 4' (feasts, Rustic Delight pancakes)"},
                    {"slices", "'Slices: 5 / 7' (cakes, pies, Brewin' cheese wheels and pizza)"},
                    {"eggs", "'Eggs: 3 / 6' (nests, turtle eggs)"},
                    {"hatch", "'Hatching: 1 / 2' (turtle, crocodile, terrapin eggs)"},
                    {"level", "'Level: 2 / 3' (cauldrons)"},
                    {"charges", "'Charges: 2 / 4' (respawn anchor)"},
                    {"pickles", "'Pickles: 3 / 4' (sea pickles)"}}) {
                stateLine.put(line[0], b.comment(line[1]).define(line[0], true));
            }
            b.pop(); // state_lines

            campfireBurn = b.comment(
                    "Lit campfires: Unlit Campfire's burn line ('Burns forever' / 'Burn time: 42s'). Server data."
            ).define("campfireBurn", true);

            campfireRain = b.comment(
                    "Lit plain campfires: the pack's rain rule ('Rain: Puts it out', 'Going out in 6s', 'Safe (fuel",
                    "block below)'). Server data."
            ).define("campfireRain", true);

            sheetmetalTank = b.comment(
                    "IE sheetmetal tanks: the tank's fluid bar on every part, not only the two fluid ports. Server side."
            ).define("sheetmetalTank", true);

            kitchenStation = b.comment(
                    "Applied Cooking's ME Kitchen Station: online / offline and the security terminal it is tied to."
            ).define("kitchenStation", true);

            remainderBuffer = b.comment(
                    "FD cooking pot, Farmer's Respite kettle and oven: 'Holding: 3 Glass Bottle, 1 Bucket' for the two",
                    "leftover slots, and 'Waiting: leftover slots full' while a craft waits for room ([cooking] stallWhenFull)."
            ).define("remainderBuffer", true);

            sheared = b.comment(
                    "Grass-like blocks cut with shears (fertile=false: grass, mycelium, lush grass, grass slabs):",
                    "'Sheared, prevents spreading and converting to dirt'. Sheared vines ([vine_shearing]) and Vinery's",
                    "sterilized jungle grape vine: 'Sheared, stops growing'. Weeping, twisting and cave vine and kelp",
                    "tips and Immersive Weathering ivy at their last age: 'Stopped growing'."
            ).define("sheared", true);

            silkTouch = b.comment(
                    "'Silk Touch Required' line: True (green) when nothing drops without Silk Touch, Drops Differently",
                    "(yellow) when something else drops (block tags creativecommon:wthit/needs_silk_touch and",
                    "wthit/silk_touch_differs, from tools/gen_silk_touch_blocks.py). Hidden while the main-hand tool has Silk Touch."
            ).define("silkTouch", true);
            silkTouchShowFalse = b.comment(
                    "Also show 'Silk Touch Required: False' (red) on every other block. Off by default."
            ).define("silkTouchShowFalse", false);

            // cropSeasons, cropOutOfSeason: creativecalendar-common.toml [wthit] (Creative Calendar, 2026-11-01)

            operatorBlocks = b.comment(
                    "WTHIT's blacklist hides barriers, structure voids and light blocks from everyone. With this on, players",
                    "with operatorBlockLevel see the blocks in operatorBlockList; everyone else still sees through them."
            ).define("operatorBlocks", true);

            operatorBlockList = b.comment("Blocks shown to operators only (block ids).")
                    .defineListAllowEmpty(List.of("operatorBlockList"),
                            () -> List.of("minecraft:barrier", "minecraft:structure_void", "minecraft:light"),
                            o -> o instanceof String s && !s.isBlank());

            operatorBlockLevel = b.comment(
                    "Permission level that sees them: 2 = operators who can use /give and command blocks (also single",
                    "player with cheats on), 4 = server owners only, 0 = every player."
            ).defineInRange("operatorBlockLevel", 2, 0, 4);

            b.pop(); // wthit
        }
    }

    /**
     * Immersive Portals rendering (client side; feature_writeup/plan_portal_changes.md 2 and 4). Mirror limits are read
     * by mixin/IPPortalRenderLimitsMixin through portals/PortalLimits, the fog by mixin/IPPortalFogMixin through
     * portals/PortalFog. Read every frame.
     */
    public static final class ImmersivePortals {
        public final ForgeConfigSpec.IntValue mirrorRenderDistance;
        public final ForgeConfigSpec.IntValue mirrorMaxBounces;
        public final ForgeConfigSpec.IntValue mirrorViewDistance;
        public final ForgeConfigSpec.IntValue portalGunViewDistance;
        public final ForgeConfigSpec.IntValue netherViewDistance;
        public final Fog nether;
        public final Fog portalGun;
        public final Fog mirror;

        private ImmersivePortals(ForgeConfigSpec.Builder b) {
            b.comment("Immersive Portals rendering (client side).").push("immersive_portals");

            mirrorRenderDistance = b.comment(
                    "Mirrors farther than this many blocks from the camera are not rendered through and show their",
                    "glass. 0 = no limit."
            ).defineInRange("mirrorRenderDistance", 12, 0, 512);
            mirrorMaxBounces = b.comment(
                    "Reflections drawn inside reflections when every portal in the chain is a mirror. Immersive",
                    "Portals' own maxPortalLayer still caps every portal (and stays the cap for gun portals)."
            ).defineInRange("mirrorMaxBounces", 4, 1, 16);
            mirrorViewDistance = b.comment(
                    "Blocks of world drawn inside a mirror, rounded up to whole chunks (a hard cut; the mirror fog",
                    "below fades it instead). 0 = the normal render distance."
            ).defineInRange("mirrorViewDistance", 32, 0, 512);
            portalGunViewDistance = b.comment(
                    "Blocks of world drawn through a Portal Gun portal, rounded up to whole chunks (a hard cut; the",
                    "portalGun fog below fades it). 0 = the normal render distance."
            ).defineInRange("portalGunViewDistance", 32, 0, 512);
            netherViewDistance = b.comment(
                    "Blocks of world drawn through a Nether portal, rounded up to whole chunks (a hard cut; the nether",
                    "fog below fades it). Immersive Portals loads the far side to its indirectLoadingRadiusCap (2 chunks",
                    "in this pack). 0 = the normal render distance."
            ).defineInRange("netherViewDistance", 32, 0, 512);

            b.comment(
                    "Fog drawn while looking through a portal, per portal type. Distances are blocks past the portal",
                    "plane: clear up to start, full fog at end. enabled = false keeps the normal fog. Keep nether.end at",
                    "netherViewDistance, portalGun.end at portalGunViewDistance and mirror.end at mirrorViewDistance,",
                    "where the world seen through stops."
            ).push("fog");
            nether = new Fog(b, "nether", "Nether portals.", true, 8, 32);
            portalGun = new Fog(b, "portalGun", "Portal Gun portals.", true, 16, 32);
            mirror = new Fog(b, "mirror", "Mirrors.", true, 16, 32);
            b.pop(); // fog

            b.pop(); // immersive_portals
        }

        public static final class Fog {
            public final ForgeConfigSpec.BooleanValue enabled;
            public final ForgeConfigSpec.IntValue start;
            public final ForgeConfigSpec.IntValue end;

            private Fog(ForgeConfigSpec.Builder b, String name, String what, boolean on, int start, int end) {
                b.comment(what).push(name);
                enabled = b.define("enabled", on);
                this.start = b.comment("Blocks past the portal plane where the fog starts.").defineInRange("start", start, 0, 1024);
                this.end = b.comment("Blocks past the portal plane where the fog is full (kept above start).")
                        .defineInRange("end", end, 1, 1024);
                b.pop();
            }
        }
    }

    /**
     * Portal Gun portals (feature_writeup/plan_portal_changes.md 3 and 3.1). opaquePortals is client side
     * (mixin/IPPortalRenderLimitsMixin, mixin/PortalGunRendererOpaqueMixin); despawn is server side
     * (portalgun/PortalGunDissolve).
     */
    public static final class PortalGun {
        public final ForgeConfigSpec.BooleanValue opaquePortals;
        public final ForgeConfigSpec.IntValue despawnDistance;
        public final ForgeConfigSpec.IntValue heartbeatSeconds;

        private PortalGun(ForgeConfigSpec.Builder b) {
            b.comment("Portal Gun portals.").push("portalgun");

            opaquePortals = b.comment(
                    "Client side. Gun portals are not rendered through: each shows a flat disc in its colour. Walking",
                    "through still works."
            ).define("opaquePortals", false);
            despawnDistance = b.comment(
                    "Server side. A gun portal dissolves when its owner is farther than this many blocks from it",
                    "(checked every heartbeat). Portals also dissolve when the owner logs out or changes dimension.",
                    "0 = no distance limit."
            ).defineInRange("despawnDistance", 128, 0, 100000);
            heartbeatSeconds = b.comment(
                    "Server side. Seconds between a gun portal's distance checks."
            ).defineInRange("heartbeatSeconds", 10, 1, 600);

            b.pop(); // portalgun
        }
    }
}
