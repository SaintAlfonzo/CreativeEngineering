package com.SaintAlfonzo.creativeengineering;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.SaintAlfonzo.creativeengineering.shield.ElementalKind;
import com.SaintAlfonzo.creativeengineering.shield.ShieldConfig;
import com.SaintAlfonzo.creativeengineering.shield.ShieldGroup;
import com.SaintAlfonzo.creativeengineering.shield.ShieldTickGate;
import com.SaintAlfonzo.creativeengineering.shield.ShieldTier;
import com.SaintAlfonzo.creativeengineering.surface.SurfaceTier;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Central Forge-native config spec for {@code creativeengineering}, backing
 * {@code config/creativeengineering-common.toml}. Unlike the KubeJS {@code global.config}
 * JSON framework, values defined here show up in Create's in-game Configs screen
 * (reachable from the pause menu), are editable live, and hot-reload without a restart.
 *
 * <p>Registered once, in {@link CreativeEngineering}'s constructor - registration must
 * happen during mod construction, since Forge loads every mod's COMMON/CLIENT config
 * immediately after construction finishes for every mod (any later, e.g. from
 * {@code FMLCommonSetupEvent}, is silently too late and the file is never read).
 */
public final class CEConfig {

    private CEConfig() {}

    public enum PullDirection { TOWARD, AWAY }

    /** How the WTHIT Run Speed line writes a multiplier: 1.50× or +50%. */
    public enum SpeedFormat { MULTIPLIER, PERCENT }

    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgivingVoid FORGIVING_VOID;
    public static final DragonGear DRAGON_GEAR;
    public static final AirBrake AIR_BRAKE;
    public static final Crafting CRAFTING;
    public static final Magic MAGIC;
    public static final ArmorEnchant ARMOR_ENCHANT;
    public static final Shrapnel SHRAPNEL;
    public static final ArmorOverflow ARMOR_OVERFLOW;
    public static final Jei JEI;
    public static final GuiTitle GUI_TITLE;
    public static final Shields SHIELDS;
    public static final DamageTagConfig DAMAGE_TAGS;
    public static final BonemealCharm BONEMEAL_CHARM;
    public static final CropGrowth CROP_GROWTH;
    public static final Canning CANNING;
    public static final Cooking COOKING;
    public static final Perf PERF;
    public static final Seasons SEASONS;
    public static final Surface SURFACE;
    public static final Worldgen WORLDGEN;
    public static final TrashSlot TRASH_SLOT;
    public static final WanderingTrader WANDERING_TRADER;
    public static final Debug DEBUG;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        FORGIVING_VOID = new ForgivingVoid(builder);
        DRAGON_GEAR = new DragonGear(builder);
        AIR_BRAKE = new AirBrake(builder);
        CRAFTING = new Crafting(builder);
        MAGIC = new Magic(builder);
        ARMOR_ENCHANT = new ArmorEnchant(builder);
        SHRAPNEL = new Shrapnel(builder);
        ARMOR_OVERFLOW = new ArmorOverflow(builder);
        JEI = new Jei(builder);
        GUI_TITLE = new GuiTitle(builder);
        SHIELDS = new Shields(builder);
        DAMAGE_TAGS = new DamageTagConfig(builder);
        BONEMEAL_CHARM = new BonemealCharm(builder);
        CROP_GROWTH = new CropGrowth(builder);
        CANNING = new Canning(builder);
        COOKING = new Cooking(builder);
        PERF = new Perf(builder);
        SEASONS = new Seasons(builder);
        SURFACE = new Surface(builder);
        WORLDGEN = new Worldgen(builder);
        TRASH_SLOT = new TrashSlot(builder);
        WANDERING_TRADER = new WanderingTrader(builder);
        DEBUG = new Debug(builder);
        COMMON_SPEC = builder.build();
    }

    /** Dials for {@code mixin/ForgivingVoidMixin}'s origin-pull behaviour. */
    public static final class ForgivingVoid {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.EnumValue<PullDirection> direction;
        public final ForgeConfigSpec.DoubleValue minDistance;
        public final ForgeConfigSpec.DoubleValue maxDistance;
        public final ForgeConfigSpec.DoubleValue defaultAnchorX;
        public final ForgeConfigSpec.DoubleValue defaultAnchorZ;
        public final ForgeConfigSpec.BooleanValue overworldOverride;
        public final ForgeConfigSpec.DoubleValue overworldAnchorX;
        public final ForgeConfigSpec.DoubleValue overworldAnchorZ;
        public final ForgeConfigSpec.BooleanValue netherOverride;
        public final ForgeConfigSpec.DoubleValue netherAnchorX;
        public final ForgeConfigSpec.DoubleValue netherAnchorZ;
        public final ForgeConfigSpec.BooleanValue endOverride;
        public final ForgeConfigSpec.DoubleValue endAnchorX;
        public final ForgeConfigSpec.DoubleValue endAnchorZ;

        private ForgivingVoid(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Forgiving Void catches players who fall out of the world; these dials control",
                    "how the landing spot is nudged each bounce so a void-fall loop can't run forever."
            ).push("forgiving_void");

            enabled = b.comment("Master on/off switch. When false, the landing spot is left untouched.")
                    .define("enabled", true);

            direction = b.comment(
                    "TOWARD pulls the landing spot closer to the anchor each bounce (default - kills fall loops).",
                    "AWAY pushes it further from the anchor instead."
            ).defineEnum("direction", PullDirection.TOWARD);

            minDistance = b.comment("Minimum blocks moved per bounce.")
                    .defineInRange("minDistance", 20.0, 0.0, 1.0E7);
            maxDistance = b.comment("Maximum blocks moved per bounce. Set equal to minDistance for no randomness.")
                    .defineInRange("maxDistance", 25.0, 0.0, 1.0E7);

            defaultAnchorX = b.comment("Anchor X for any dimension without an override below. Defaults to world center.")
                    .defineInRange("defaultAnchorX", 0.0, -3.0E7, 3.0E7);
            defaultAnchorZ = b.comment("Anchor Z for any dimension without an override below. Defaults to world center.")
                    .defineInRange("defaultAnchorZ", 0.0, -3.0E7, 3.0E7);

            b.push("overworldOverride");
            overworldOverride = b.comment("Use a separate anchor for the Overworld instead of the default anchor above.")
                    .define("enabled", false);
            overworldAnchorX = b.defineInRange("anchorX", 0.0, -3.0E7, 3.0E7);
            overworldAnchorZ = b.defineInRange("anchorZ", 0.0, -3.0E7, 3.0E7);
            b.pop();

            b.push("netherOverride");
            netherOverride = b.comment("Use a separate anchor for the Nether instead of the default anchor above.")
                    .define("enabled", false);
            netherAnchorX = b.defineInRange("anchorX", 0.0, -3.0E7, 3.0E7);
            netherAnchorZ = b.defineInRange("anchorZ", 0.0, -3.0E7, 3.0E7);
            b.pop();

            b.push("endOverride");
            endOverride = b.comment("Use a separate anchor for the End instead of the default anchor above.")
                    .define("enabled", false);
            endAnchorX = b.defineInRange("anchorX", 0.0, -3.0E7, 3.0E7);
            endAnchorZ = b.defineInRange("anchorZ", 0.0, -3.0E7, 3.0E7);
            b.pop();

            b.pop(); // forgiving_void
        }
    }

    /** Dials for dragon-gear passive autorepair (kubejs/server_scripts/equipment/dragon_autorepair.js). */
    public static final class DragonGear {
        public final ForgeConfigSpec.BooleanValue autorepairEnabled;
        public final ForgeConfigSpec.IntValue autorepairIntervalTicks;
        public final ForgeConfigSpec.IntValue autorepairAmountPerInterval;

        private DragonGear(ForgeConfigSpec.Builder b) {
            b.push("dragon_gear").push("autorepair");

            autorepairEnabled = b.comment("Passive slow self-repair for dragon-tier gear while worn/held.")
                    .define("enabled", true);
            autorepairIntervalTicks = b.comment("Ticks between repair pulses (20 ticks = 1 second).")
                    .defineInRange("intervalTicks", 200, 1, Integer.MAX_VALUE);
            autorepairAmountPerInterval = b.comment("Durability restored per pulse.")
                    .defineInRange("amountPerInterval", 50, 0, Integer.MAX_VALUE);

            b.pop().pop();
        }
    }

    /** Dials for the {@code creativeengineering:air_brake} elytra item ([[gear.AirBrakeHandler]]). */
    public static final class AirBrake {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.IntValue useDurationTicks;
        public final ForgeConfigSpec.BooleanValue requireFallFlying;
        public final ForgeConfigSpec.DoubleValue horizontalDecelerationPerTick;
        public final ForgeConfigSpec.DoubleValue minHorizontalSpeed;
        public final ForgeConfigSpec.DoubleValue upwardDecelerationPerTick;
        public final ForgeConfigSpec.DoubleValue minUpwardSpeed;
        public final ForgeConfigSpec.DoubleValue downwardSlowFactorPerTick;
        public final ForgeConfigSpec.DoubleValue terminalDownwardSpeed;
        public final ForgeConfigSpec.BooleanValue stallEnabled;
        public final ForgeConfigSpec.DoubleValue stallSpeedThreshold;

        private AirBrake(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Held-item elytra air brake: right-click and hold the air brake item (like drawing",
                    "a bow) while gliding to bleed off speed. Horizontal and upward motion decelerate",
                    "at a steady rate; downward motion is only slowed slightly, so it brakes forward",
                    "speed/climbs without turning your dive into a hover."
            ).push("air_brake");

            enabled = b.comment("Master on/off switch for the air brake's flight effect.")
                    .define("enabled", true);

            useDurationTicks = b.comment(
                    "How many ticks the item reports as its max use duration (bow-style hold-to-use).",
                    "The default is the same huge value vanilla's bow uses - effectively unlimited, so",
                    "the player releasing the mouse button is what actually ends the hold, not a timeout.",
                    "Only read once, when the item is registered - a running game needs a restart to see",
                    "a change here (not just the item's config-reload)."
            ).defineInRange("useDurationTicks", 72000, 1, Integer.MAX_VALUE);

            requireFallFlying = b.comment(
                    "true (default): the brake only slows the player while actually gliding (isFallFlying()).",
                    "false: it slows the player any time it's held and drawn, gliding or not."
            ).define("requireFallFlying", true);

            horizontalDecelerationPerTick = b.comment(
                    "Blocks/tick removed from horizontal speed (X/Z combined) each tick the brake is held.",
                    "Applied as a steady subtraction, not a percentage, so it brakes hard, fast flight",
                    "exactly as fast (in blocks/tick^2) as slow flight."
            ).defineInRange("horizontalDecelerationPerTick", 0.05, 0.0, 10.0);

            minHorizontalSpeed = b.comment("Horizontal speed the brake will never decelerate below.")
                    .defineInRange("minHorizontalSpeed", 0.0, 0.0, 10.0);

            upwardDecelerationPerTick = b.comment(
                    "Blocks/tick removed from upward (climbing) speed each tick the brake is held.",
                    "Only applies while vertical speed is positive - see downwardSlowFactorPerTick",
                    "for the (much gentler) falling case."
            ).defineInRange("upwardDecelerationPerTick", 0.05, 0.0, 10.0);

            minUpwardSpeed = b.comment("Upward speed the brake will never decelerate below (0 = can fully stop a climb).")
                    .defineInRange("minUpwardSpeed", 0.0, 0.0, 10.0);

            downwardSlowFactorPerTick = b.comment(
                    "Fraction of downward (falling) speed removed each tick the brake is held - e.g. 0.02",
                    "removes 2% of the current fall speed per tick. Deliberately gentle and multiplicative",
                    "(not the flat per-tick subtraction used above) so the brake trims a dive instead of",
                    "cancelling it outright."
            ).defineInRange("downwardSlowFactorPerTick", 0.02, 0.0, 1.0);

            terminalDownwardSpeed = b.comment(
                    "Hard cap on fall speed (blocks/tick, positive number) while the brake is held and",
                    "the player is falling - applied before downwardSlowFactorPerTick each tick. 0 disables",
                    "the cap entirely (downwardSlowFactorPerTick is then the only thing slowing a dive)."
            ).defineInRange("terminalDownwardSpeed", 3.0, 0.0, 10.0);

            stallEnabled = b.comment(
                    "Master on/off switch for the brake's stall effect: brake hard enough, for long enough,",
                    "and it drops you out of elytra flight entirely (a real air brake can't hold you in the",
                    "air below minimum glide speed). Independent of the deceleration knobs above - those",
                    "keep working even with this off."
            ).define("stallEnabled", true);

            stallSpeedThreshold = b.comment(
                    "Horizontal speed (blocks/tick, X/Z combined), AFTER this tick's deceleration, at or",
                    "below which the brake drops the player out of fall-flying. Checked every tick the brake",
                    "is held and decelerating, same as everything else above - only reachable at all if",
                    "minHorizontalSpeed is below this value."
            ).defineInRange("stallSpeedThreshold", 0.5, 0.0, 10.0);

            b.pop(); // air_brake
        }
    }

    /**
     * Framework for driving individual KubeJS recipe VALUES (not just on/off toggles) from
     * CEConfig. Each recipe that opts in gets its own nested section here; the KubeJS side
     * reads it via {@code Java.loadClass} the same way as {@link DragonGear}/{@link AirBrake}.
     * Recipes are baked at datapack-reload time ({@code ServerEvents.recipes}), so editing a
     * value here needs a {@code /reload} to reach an already-generated recipe - CEConfig's own
     * live-reload only refreshes the value on disk, not recipes already built from it.
     */
    public static final class Crafting {
        public final FelsicPiece FELSIC_HELMET;
        public final FelsicPiece FELSIC_CHESTPLATE;
        public final FelsicPiece FELSIC_LEGGINGS;
        public final FelsicPiece FELSIC_BOOTS;
        public final FelsicPiece FELSIC_SWORD;
        public final FelsicPiece FELSIC_PICKAXE;
        public final FelsicPiece FELSIC_AXE;
        public final FelsicPiece FELSIC_SHOVEL;
        public final FelsicPiece FELSIC_HOE;

        private Crafting(ForgeConfigSpec.Builder b) {
            b.push("crafting");
            String magicResistance = "creativeengineering:magic_resistance";
            FELSIC_HELMET = new FelsicPiece(b, "felsic_helmet", magicResistance, 500, 1, 5);
            FELSIC_CHESTPLATE = new FelsicPiece(b, "felsic_chestplate", magicResistance, 500, 1, 5);
            FELSIC_LEGGINGS = new FelsicPiece(b, "felsic_leggings", magicResistance, 500, 1, 5);
            FELSIC_BOOTS = new FelsicPiece(b, "felsic_boots", magicResistance, 500, 1, 5);
            FELSIC_SWORD = new FelsicPiece(b, "felsic_sword", magicResistance, 250, 1, 3);
            FELSIC_PICKAXE = new FelsicPiece(b, "felsic_pickaxe", magicResistance, 250, 1, 3);
            FELSIC_AXE = new FelsicPiece(b, "felsic_axe", magicResistance, 250, 1, 3);
            FELSIC_SHOVEL = new FelsicPiece(b, "felsic_shovel", magicResistance, 250, 1, 3);
            FELSIC_HOE = new FelsicPiece(b, "felsic_hoe", magicResistance, 250, 1, 3);
            b.pop();
        }

        /**
         * Tuning for one felsic_gear piece's sequenced-assembly recipe
         * (kubejs/server_scripts/equipment/end_gear.js, {@code R.cfg(..., 'CRAFTING.FELSIC_<PIECE>')}).
         * One instance per piece rather than one class per piece - same 4 fields, only the toml
         * section name and defaults differ.
         */
        public static final class FelsicPiece {
            public final ForgeConfigSpec.ConfigValue<String> potion;
            public final ForgeConfigSpec.IntValue mb;
            public final ForgeConfigSpec.IntValue loops;
            public final ForgeConfigSpec.IntValue blows;

            private FelsicPiece(ForgeConfigSpec.Builder b, String tomlSection, String defaultPotion,
                                 int defaultMb, int defaultLoops, int defaultBlows) {
                b.push(tomlSection);

                // Toml keys (labels shown in the Configs GUI) stay descriptive even though the
                // Java field names above are kept short for callers like R.cfg() in KubeJS.
                //
                // Create's Configs screen (SubMenuConfigScreen) has no built-in widget for a raw
                // ConfigValue<String> - it falls through to an "Impl missing" placeholder row.
                // SubMenuConfigScreenMixin + StringEntry (mixin/compat packages) patch in a text
                // field for exactly this case, so this field IS editable in-game despite the gap
                // in Create's own GUI.
                potion = b.comment("Potion effect id filled in during assembly.")
                        .define("potion", defaultPotion);
                mb = b.comment("mB of potion fluid used in the final filling step.")
                        .defineInRange("potionMb", defaultMb, 1, 10000);
                loops = b.comment("Number of times the sequenced assembly repeats.")
                        .defineInRange("loops", defaultLoops, 1, 64);
                blows = b.comment("Hammer blows required by the hammering step.")
                        .defineInRange("hammerBlows", defaultBlows, 0, 64);

                b.pop();
            }
        }
    }

    /** Dials for {@code potion/MagicResistanceEffect}'s damage-reduction curve. */
    public static final class Magic {
        public final ForgeConfigSpec.DoubleValue reductionPerLevel;

        private Magic(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Tuning for creativeengineering:magic_resistance's damage reduction against",
                    "magic damage sources (DamageSource#isMagic() - potion/instant damage, evoker",
                    "fangs, conduit, dragon's breath, etc)."
            ).push("magic_resistance");

            reductionPerLevel = b.comment(
                    "Damage reduction per effect amplifier level (amplifier 0 = level I), same",
                    "shape as vanilla Resistance. Total reduction is capped at 1.0 (full immunity)",
                    "regardless of amplifier."
            ).defineInRange("reductionPerLevel", 0.2, 0.0, 1.0);

            b.pop(); // magic_resistance
        }
    }

    /** Per-armor-material enchantment-value overrides ({@code mixin/ArmorMaterialEnchantabilityMixin}). */
    public static final class ArmorEnchant {
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> overrides;

        private ArmorEnchant(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Enchantment-value (enchantability) overrides for whole armor materials that",
                    "KubeJS's tune_armor.js can't reach (ArmorMaterial#getEnchantmentValue() has no",
                    "kjs$ accessor). Entries are \"materialName:value\" - materialName matches",
                    "ArmorMaterial#getName(), e.g. Felsic Gear and Create: Deep Dark's echo armor",
                    "each hand ArmorItem an anonymous material named \"felsic\" / \"echo_armor\".",
                    "Add a row here for more materials - no rebuild needed."
            ).push("armor_enchantability");

            overrides = b.defineList(
                    "overrides",
                    List.of("echo_armor:18", "felsic:21"),
                    o -> o instanceof String s && s.indexOf(':') > 0
            );

            b.pop(); // armor_enchantability
        }
    }

    /**
     * Dials for TNT shrapnel ({@code shrapnel.ShrapnelEntity} / {@code ShrapnelSpawner}) -
     * fired both when a primed TNT entity detonates ({@code shrapnel.TntShrapnelHandler}, a
     * plain Forge {@code ExplosionEvent.Detonate} listener) and when a dropped TNT item is
     * consumed by {@code itemphysic.ItemBurnTransform}'s EXPLODE rule
     * ({@code global.itemPhysics.burnExplode('minecraft:tnt', ..., true)} in
     * {@code kubejs/startup_scripts/func/item_physics.js} opts a rule into shrapnel).
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
     * Dials for {@code mixin.BonemealCharmMixin} - the "infinite bonemeal" trick Vinery's
     * Winemaker armor set does (a chance to refund the bone meal a
     * {@code net.minecraft.world.item.BoneMealItem#useOn} call just consumed), generalized onto
     * a single held item instead of a 4-piece armor set. Any item in
     * {@code #creativeengineering:bonemeal_charms} (populated in KubeJS -
     * {@code kubejs/server_scripts/tags/equipmenttags.js}) works from whichever hand ISN'T
     * holding the bone meal.
     */
    public static final class BonemealCharm {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.DoubleValue chance;
        public final ForgeConfigSpec.DoubleValue damageChance;
        public final ForgeConfigSpec.IntValue damagePerUse;

        private BonemealCharm(ForgeConfigSpec.Builder b) {
            b.comment(
                    "A charm item (see #creativeengineering:bonemeal_charms) held in the OTHER hand",
                    "from bone meal gives a chance to refund the bone meal that was just used, at",
                    "the cost of a chance to damage the charm itself."
            ).push("bonemeal_charm");

            enabled = b.comment("Master on/off switch.")
                    .define("enabled", true);

            chance = b.comment("Chance [0-1] to refund the bone meal used while a charm is held.")
                    .defineInRange("chance", 0.5, 0.0, 1.0);

            damageChance = b.comment("Chance [0-1] the charm takes durability damage per use (rolled independently of chance).")
                    .defineInRange("damageChance", 0.1, 0.0, 1.0);

            damagePerUse = b.comment("Durability damage dealt to the charm when damageChance hits.")
                    .defineInRange("damagePerUse", 1, 0, 100);

            b.pop(); // bonemeal_charm
        }
    }

    /**
     * Dials for {@code crops.WthitCropGrowth} (client-side, display only): the "Growth" line WTHIT
     * leaves off bushes, vines, hemp, tobacco and fruit-tree leaves.
     */
    public static final class CropGrowth {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.BooleanValue fallback;
        public final ForgeConfigSpec.BooleanValue showHarvestable;

        private CropGrowth(ForgeConfigSpec.Builder b) {
            b.comment(
                    "WTHIT shows 'Growth: 60%' / 'Growth: Mature' only for CropBlock subclasses plus a few vanilla",
                    "plants. This adds the same line, with the same wording and the same 'Crop progress' toggle in",
                    "WTHIT's own config, to every other growing plant. The blocks and their stages are in",
                    "assets/creativeengineering/crop_growth.json (override it from kubejs/assets/creativeengineering/).",
                    "Client-side only; reads the block state the client already has."
            ).push("crop_growth");

            enabled = b.comment("Master on/off switch for the added lines.")
                    .define("enabled", true);

            fallback = b.comment(
                    "true: a bush-type plant (BushBlock, not a sapling) with an integer 'age' that no rule in",
                    "crop_growth.json lists still gets a line, fully grown at its top age. Catches plants from",
                    "mods added later. false: only the plants in the file."
            ).define("fallback", true);

            showHarvestable = b.comment(
                    "true: plants that can be picked before they are fully grown (salmonberries from stage 3,",
                    "grapes from stage 2) read 'Growth: 75% (Harvestable)' until they finish. false: the",
                    "percentage only."
            ).define("showHarvestable", true);

            b.pop(); // crop_growth
        }
    }

    /**
     * Throttles for third-party mod code that the server profile (feature_writeup/perf.md section 7) found
     * hot. Each one is a mixin in {@code mixin/} that skips or spaces out work the mod does every tick;
     * the comments say what is traded away. Read live, no rejoin needed.
     */
    public static final class Perf {
        public final ForgeConfigSpec.IntValue troughMissCooldownTicks;
        public final ForgeConfigSpec.IntValue seasonsMeltIntervalTicks;
        public final ForgeConfigSpec.BooleanValue gravityLimitDimensions;
        public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> gravityDimensions;
        public final ForgeConfigSpec.IntValue mountEffectIntervalTicks;

        private Perf(ForgeConfigSpec.Builder b) {
            b.comment("Performance throttles for other mods' per-tick work. See feature_writeup/perf.md section 7.")
                    .push("perf");

            troughMissCooldownTicks = b.comment(
                    "Quark feeding trough: after an animal's trough search finds nothing, it does not search again for",
                    "this many ticks (600 = 30 s). Each search is a point-of-interest scan plus a ray cast, and a",
                    "hungry animal with no trough nearby repeated it every few ticks. Cost: a trough placed or",
                    "refilled next to an animal is noticed up to this long late. 0 = off (Quark's own behaviour)."
            ).defineInRange("troughMissCooldownTicks", 600, 0, 72000);

            seasonsMeltIntervalTicks = b.comment(
                    "Serene Seasons snow/ice melt: the per-chunk melt roll runs once every N ticks instead of every",
                    "tick, with its chance multiplied by N so the average melt rate is unchanged (a roll's chance",
                    "is capped at 1, so N above 4 melts slightly slower in summer). Walking every loaded chunk on",
                    "every tick was the cost. Cost: melting is a little lumpier. 1 = off."
            ).defineInRange("seasonsMeltIntervalTicks", 4, 1, 40);

            gravityLimitDimensions = b.comment(
                    "Create: Gravity runs its biome lookup and gravity-attribute update for every living entity in",
                    "every dimension on every tick. On: only entities in the dimensions below run it (entities that",
                    "still carry the low-gravity modifier from one run it once more to drop it). Off: the mod's own",
                    "behaviour."
            ).define("gravityLimitDimensions", true);
            gravityDimensions = b.comment(
                    "Dimensions Create: Gravity is allowed to run in. The pack's biome_configs lists only",
                    "#minecraft:is_end, so the End is enough. Add a dimension here if you add a gravity biome",
                    "outside the End."
            ).defineList("gravityDimensions", java.util.List.of("minecraft:the_end"), o -> o instanceof String);

            mountEffectIntervalTicks = b.comment(
                    "Ticks between passes that apply a worn horse armor's effects (dragon horse armor: Regeneration).",
                    "The pass walks every entity in the level, which cost too much every tick. Effects last",
                    "this + 40 ticks, so they stay on. A newly equipped armor starts working within one interval."
            ).defineInRange("mountEffectIntervalTicks", 100, 1, 1200);

            b.pop(); // perf
        }
    }

    /**
     * Remainder buffer on FD-style stations (cooking pot, Farmer's Respite kettle, oven): ingredient leftovers stay
     * in the station. {@code cooking.RemainderBuffer}; feature_writeup/remainder_buffer.md.
     */
    public static final class Cooking {
        public final ForgeConfigSpec.BooleanValue remainderBuffer;
        public final ForgeConfigSpec.BooleanValue stallWhenFull;

        private Cooking(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Leftover containers from ingredients (glass bottles, bowls, buckets) on the FD cooking pot, the",
                    "Farmer's Respite kettle and the oven. A leftover the waiting meal is served in goes to the",
                    "container slot; the rest goes to two leftover slots right of the output, which hoppers and",
                    "funnels on the output faces can empty."
            ).push("cooking");

            remainderBuffer = b.comment("Off = leftovers are thrown out of the station as in the original mods.")
                    .define("remainderBuffer", true);

            stallWhenFull = b.comment(
                    "On = a station whose leftover slots can't take a craft's leftovers waits (like a full output).",
                    "Off = it crafts anyway and throws what doesn't fit."
            ).define("stallWhenFull", true);

            b.pop(); // cooking
        }
    }

    /**
     * Dials for multi-serving cans ({@code food.CanServings}). How many servings a can holds is
     * NOT here: it comes from the {@code #creativeengineering:cans/servings/<n>} item tags, set in
     * KubeJS from {@code kubejs/generation_scripts/food/can_servings.json}.
     */
    public static final class Canning {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.DoubleValue emptyCanChance;
        public final ForgeConfigSpec.DoubleValue crushedCanChance;
        public final ForgeConfigSpec.ConfigValue<String> emptyCanItem;
        public final ForgeConfigSpec.ConfigValue<String> crushedCanItem;

        private Canning(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Cans in #creativeengineering:cans/servings/<n> hold n servings. Eating or crafting with",
                    "one takes a serving; the servings left show as a durability bar and a tooltip line. The",
                    "last serving leaves an empty can, a crushed can or nothing, rolled with the chances below."
            ).push("canning");

            enabled = b.comment("Off = every can behaves like the Canned Goods original (one serving, always an empty can).")
                    .define("enabled", true);

            emptyCanChance = b.comment("Chance [0-1] that an emptied can comes back as an empty can.")
                    .defineInRange("emptyCanChance", 0.7, 0.0, 1.0);

            crushedCanChance = b.comment(
                    "Chance [0-1] that it comes back as a crushed can instead. Whatever is left after both",
                    "chances gives nothing; if the two add up past 1, the crushed chance is cut to fit."
            ).defineInRange("crushedCanChance", 0.25, 0.0, 1.0);

            emptyCanItem = b.comment("Item id handed back as the empty can.")
                    .define("emptyCanItem", "canned_goods:empty_can");

            crushedCanItem = b.comment("Item id handed back as the crushed can.")
                    .define("crushedCanItem", "canned_goods:crushed_empty_can");

            b.pop(); // canning
        }
    }

    /**
     * Season types for Serene Seasons ({@code seasons/SeasonTypes}): arid, frozen and tropical biomes by
     * tag, and rivers/beaches that take on the land around them. feature_writeup/weather.md.
     */
    public static final class Seasons {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.IntValue adaptiveRadius;
        public final ForgeConfigSpec.IntValue snowLineY;

        private Seasons(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Season types on top of Serene Seasons. The biome lists are the tags creativeengineering:seasons/",
                    "arid, frozen, tropical and adaptive (kubejs/generation_scripts/worldgen/season_types.json).",
                    "Arid: no rain all year. Frozen: snow all year. Tropical: wet and dry seasons, savannas included.",
                    "Adaptive (rivers, beaches): each column behaves like the nearest land biome. SeasonHUD shows",
                    "'Arid' / 'Frozen' with the day of the year. Read live on both sides."
            ).push("seasons");

            enabled = b.comment("false: Serene Seasons' own rules everywhere, the tags are ignored.")
                    .define("enabled", true);

            adaptiveRadius = b.comment(
                    "How far (blocks) a river or beach column looks for land to copy, in rings of 8 directions",
                    "at 8, 16, 24, 32, 48, 64, ... blocks. The first ring that reaches land decides by majority.",
                    "Nothing found: the river or beach keeps its own biome."
            ).defineInRange("adaptiveRadius", 64, 8, 256);

            snowLineY = b.comment(
                    "The snow line (default 320, the overworld build height). Below it height alone never turns rain",
                    "into snow (vanilla cools 0.05 per 40 blocks above y80): a column snows only if its biome snows at",
                    "y80 in the current sub-season. At and above it temperate and cold biomes always snow, so falling",
                    "snow tells a flyer they are above build height. Frozen and permanent-season biomes keep their own",
                    "rules. Also the ceiling for the snow_seasons lists in season_types.json."
            ).defineInRange("snowLineY", 320, -64, 4096);

            b.pop(); // seasons
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

    /**
     * Dials for {@code compat.ArmorOverflowRenderer} - Detail Armor Bar (detailab) only ever
     * draws one row of 10 armor-point icons; this instead draws full extra rows of the same
     * textured/recolored icons stacked above the first row. Detailab's own fallback for overflow
     * - tiny 3px tick marks to the left of the bar - is permanently suppressed by
     * {@code mixin.ArmorBarRendererOverflowTickMixin} regardless of this section's settings, since
     * it visually conflicts with real rows and has no purpose once this feature exists.
     */
    public static final class ArmorOverflow {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.BooleanValue compact;
        public final ForgeConfigSpec.IntValue compactRowSpacing;

        private ArmorOverflow(ForgeConfigSpec.Builder b) {
            b.comment(
                    "No detailab-compatible armor overflow mod exists, so this adds one: extra rows",
                    "of the same registered armor-bar icons/colors stacked above the normal 10-icon row,",
                    "once a player has more than 20 armor points. Requires detailab; no-ops without it."
            ).push("armor_overflow");

            enabled = b.comment("Master on/off switch. When false, overflow past 20 armor points isn't shown at all.")
                    .define("enabled", true);

            compact = b.comment(
                    "false (default): overflow rows are spaced a full icon height apart, like separate rows.",
                    "true: rows are packed tighter (compactRowSpacing px apart) and tucked mostly behind",
                    "the row below - the row closer to the hotbar is drawn on top of the one above it."
            ).define("compact", false);

            compactRowSpacing = b.comment("Vertical pixels between overflow rows when compact is true (icons are 9px tall).")
                    .defineInRange("compactRowSpacing", 4, 1, 9);

            b.pop(); // armor_overflow
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
                    "creativeengineering:jei/... tags: by type (items, multiblocks, fluids, effects), then category, then",
                    "each category's own order. Ingredients with no place keep JEI's own order ([sorting] in",
                    "config/jei/jei-client.ini) after the placed ones of their type. Off: JEI's own order only.",
                    "Takes effect the next time JEI starts (rejoin or /reload)."
            ).define("packOrder", true);

            orderReport = b.comment(
                    "Write kubejs/exported/jei_order.json at every JEI start: every item, fluid, effect and multiblock with",
                    "its place in the pack order and whether JEI shows it (shown / hidden by the pack / hidden by a mod /",
                    "hidden in JEI's edit mode / never in JEI), plus problems in the definition. Render it with",
                    "python3 tools/jei_order_report.py. Off by default: it takes a moment and a few MB."
            ).define("orderReport", false);

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
     * Everything tunable about the seven tier shields ({@code shield/} package). The whole
     * section is generated by looping {@link ShieldTier} x {@link ShieldGroup} (physical /
     * elemental / magic), so a new tier or a new group shows up here (and in Create's Configs
     * screen) with no edits to this class. Read at runtime through {@code shield.ShieldConfig},
     * which supplies the enum defaults if the file isn't loaded yet.
     */
    public static final class Shields {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.DoubleValue wearThreshold;
        public final ForgeConfigSpec.IntValue disableCooldownTicks;
        public final ForgeConfigSpec.ConfigValue<String> unblockableEffectTicks;
        public final Map<ShieldGroup, ForgeConfigSpec.EnumValue<ShieldGroup.Facing>> facing =
                new EnumMap<>(ShieldGroup.class);
        public final Map<ShieldTier, TierCfg> tiers = new EnumMap<>(ShieldTier.class);

        private Shields(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Tier shields: copper / gold / iron / diamond / netherite / echo / felsic / dragon. 'iron' is",
                    "the vanilla minecraft:shield itself, tuned like the others. Immersive Engineering's shield",
                    "and Alex's Mobs' Shield of the Deep are NOT tier shields; only disableCooldownTicks below",
                    "applies to them.",
                    "",
                    "A raised tier shield sorts each incoming hit into one of three kinds and looks that",
                    "kind up under its own tier, each tuned independently:",
                    "  physical   melee, blunt/cutting machinery, piercing, firearms, explosions,",
                    "             projectiles and anything unclassified. (A blaze fireball counts as a",
                    "             projectile, so physical, not elemental; thorns counts as magic.)",
                    "  elemental  fire, cold, electric and chemical (acid/poison) damage - none of which",
                    "             a vanilla shield can block at all.",
                    "  magic      vanilla magic (poison / instant damage, potions, evoker fangs, thorns,",
                    "             warden boom) plus the pack's own psychic and life-drain damage.",
                    "Every one of those is a damage tag under data/creativeengineering/damage_tags/, most",
                    "of them explicit lists of message ids (so 'I don't see a magic.json' is fixed - every",
                    "vanilla-flagged kind now has its own visible, editable file, not just a hidden flag",
                    "check), 'elemental' and 'magic' being pure meta tags that only #include finer ones.",
                    "Reclassify a source by editing its finer tag; widen a whole shield column by editing",
                    "elemental.json or magic.json's #includes. See [damage_tags] below.",
                    "",
                    "'elemental' can also be tuned as four separate numbers (fire/electric/cold/chemical)",
                    "instead of one blanket percentage - see each tier's [shields.<tier>.elemental_breakdown].",
                    "For each kind, per tier:",
                    "  reduction        0-100. Percent of the hit the shield absorbs. 0 = the shield does not",
                    "                   block that kind at all (normal damage, no shield sound/knockback).",
                    "                   Anything under 100 lets the remainder through as ordinary damage.",
                    "  durabilityCost   percent of vanilla's shield wear (1 + floor(damage), on hits at or",
                    "                   above wearThreshold) taken for that kind. Fractions are rolled",
                    "                   randomly, so e.g. 50% halves the wear on average."
            ).push("shields");

            enabled = b.comment(
                    "Master switch. When false the tier shields fall back to plain vanilla-shield blocking",
                    "(100% of everything vanilla blocks, wear like a vanilla shield) - only their own durability",
                    "and repair item still apply."
            ).define("enabled", true);

            wearThreshold = b.comment(
                    "Hits below this much damage never wear a shield. 3.0 is vanilla's value. Continuous",
                    "sources (burning, poison ticks) are usually under it; lava and big hits are not."
            ).defineInRange("wearThreshold", 3.0, 0.0, 1000.0);

            disableCooldownTicks = b.comment(
                    "Ticks a raised shield is unusable after an attacker disables it: a player's axe, a mob's",
                    "axe (vindicators, piglin brutes; 25% chance per hit, more with Efficiency), Immersive",
                    "Engineering's shock upgrade and Alex's Mobs' void worm shot. Not tier-specific - it covers",
                    "every shield, tier or not. Vanilla hardcodes 100 (5 seconds); the pack uses 20 (1 second).",
                    "0 disables the cooldown outright."
            ).defineInRange("disableCooldownTicks", 20, 0, 12000);

            unblockableEffectTicks = b.comment(
                    "Potion effects whose periodic damage no shield may block, comma-separated effect ids.",
                    "Matched by the effect that is ticking on the shield holder, not by damage tag: poison",
                    "ticks use the same 'magic' damage source as Instant Damage, so a tag can't tell them",
                    "apart. The shield doesn't attempt the block (no shield sound, full damage). Instant",
                    "Damage and other one-off magic hits are unaffected. The default is every effect in the",
                    "pack that deals tick damage."
            ).define("unblockableEffectTicks", ShieldConfig.UNBLOCKABLE_EFFECT_TICKS_DEFAULT);

            b.comment(
                    "Per damage kind (physical / elemental / magic): how the raised shield's direction is checked.",
                    "  REQUIRED       vanilla: the source needs a position and must be in front of you.",
                    "  IF_POSITIONED  the front-arc test only applies when the source has a position;",
                    "                 positionless sources (lava, poison) are blocked from any direction.",
                    "  NONE           never checked - blocked from every direction."
            ).push("facing");
            for (ShieldGroup g : ShieldGroup.values()) {
                facing.put(g, b.defineEnum(g.key(), g.defaultFacing()));
            }
            b.pop(); // facing

            for (ShieldTier t : ShieldTier.values()) {
                tiers.put(t, new TierCfg(b, t));
            }

            b.pop(); // shields
        }

        /** One tier's section, {@code [shields.<tier id>]}. */
        public static final class TierCfg {
            public final ForgeConfigSpec.IntValue durability;
            public final ForgeConfigSpec.ConfigValue<String> repairItem;
            public final ForgeConfigSpec.IntValue knockbackThreshold;
            public final Map<ShieldTickGate, ForgeConfigSpec.BooleanValue> blocksTicks = new EnumMap<>(ShieldTickGate.class);
            public final ForgeConfigSpec.ConfigValue<String> shielderEffects;
            public final ForgeConfigSpec.ConfigValue<String> attackerEffects;
            public final Map<ShieldGroup, ForgeConfigSpec.IntValue> reduction = new EnumMap<>(ShieldGroup.class);
            public final Map<ShieldGroup, ForgeConfigSpec.IntValue> durabilityCost = new EnumMap<>(ShieldGroup.class);
            public final Map<ElementalKind, ForgeConfigSpec.IntValue> elementalReduction = new EnumMap<>(ElementalKind.class);

            private TierCfg(ForgeConfigSpec.Builder b, ShieldTier t) {
                b.push(t.id());

                durability = b.comment(
                        "Max durability. Live: changing it takes effect on existing shields immediately",
                        "(a shield already worn past the new maximum breaks on its next hit)."
                ).defineInRange("durability", t.defaultDurability(), 1, 1_000_000);

                repairItem = b.comment("Item id that repairs this shield on an anvil. Empty = not anvil-repairable.")
                        .define("repairItem", t.defaultRepairItem());

                knockbackThreshold = b.comment(
                        "Damage amount, AFTER this tier's own reduction, below which a blocked hit no longer",
                        "knocks YOU (the holder) back. The attacker's own recoil off the shield stays vanilla.",
                        "This only ever removes knockback, never adds it. 0 = never suppress (vanilla)."
                ).defineInRange("knockbackThreshold", t.defaultKnockbackThreshold(), 0, 1000);

                for (ShieldTickGate g : ShieldTickGate.values()) {
                    String[] what = switch (g) {
                        case BURN -> new String[] {
                                "Whether this tier may block burning and hot-block ticks at all: the",
                                "creativeengineering:burn_tick damage tag (on fire, in fire, campfires, braziers, bonfires,",
                                "magma blocks, stoves). Default: netherite and above. Lava and fan fire are not in it."};
                        case CONTACT -> new String[] {
                                "Whether this tier may block thorn ticks at all: the creativeengineering:contact_tick",
                                "damage tag (cactus, berry bushes, leaf piles, weeds, wither roots, beanstalk thorns,",
                                "barbed panes). Default: diamond and above."};
                    };
                    String[] comment = java.util.Arrays.copyOf(what, what.length + 2);
                    comment[what.length] = "false = the shield doesn't attempt the block (no shield sound, full damage). true = it";
                    comment[what.length + 1] = "blocks from any direction at the hit's normal percentage (a 1-damage tick is fully blocked).";
                    blocksTicks.put(g, b.comment(comment).define(g.configKey(), t.defaultBlocksTicks(g)));
                }

                shielderEffects = b.comment(
                        "Potion effects given to whoever is HOLDING the shield each time it blocks a hit.",
                        "Comma-separated entries of  effectId;durationTicks[;amplifier[;categories]]",
                        "  amplifier   0 = level I (default 0)",
                        "  categories  optional, '|'-separated: physical|elemental|magic, or the finer",
                        "              melee|explosion|projectile|other. The effect only fires for those",
                        "              kinds of hit. Omit = every kind.",
                        "Example: minecraft:resistance;100;0;physical, minecraft:fire_resistance;200",
                        "Unknown effect ids are skipped with one warning in the log."
                ).define("shielderEffects", t.defaultShielderEffects());

                attackerEffects = b.comment(
                        "Same format, but given to the ATTACKER of the blocked hit (the entity behind the",
                        "damage - a skeleton for its arrow). Skipped when the hit has no attacker (lava,",
                        "poison) or the attacker is the shield holder."
                ).define("attackerEffects", t.defaultAttackerEffects());

                b.comment("Percent (0-100) of the hit absorbed, per damage kind. 0 = not blocked. See the [shields] header.")
                        .push("reduction");
                for (ShieldGroup g : ShieldGroup.values()) {
                    reduction.put(g, b.defineInRange(g.key(), t.defaultReduction(g), 0, 100));
                }
                b.pop();

                b.comment(
                        "Optional per-element override of 'reduction.elemental' above. Leave every one of these",
                        "four at -1 (default) and this tier keeps using that single blanket percentage for every",
                        "fire/electric/cold/chemical hit alike (and the tooltip shows one 'Elemental: X%' line).",
                        "Set ANY ONE of these four to 0-100 and this tier switches to the breakdown instead:",
                        "'reduction.elemental' is then ignored ENTIRELY for this tier, each of the four blocks",
                        "only what it's set to here (a -1 or explicit 0 blocks nothing for that element), and the",
                        "tooltip shows one line per element THAT IS NON-ZERO ('Fire: 50%', say, with no Cold/",
                        "Electric/Chemical line at all if those are still -1 or 0). It's one or the other, not both.",
                        "Wear still comes from durabilityCost.elemental below either way - there's no per-element",
                        "wear split."
                ).push("elemental_breakdown");
                for (ElementalKind k : ElementalKind.values()) {
                    elementalReduction.put(k, b.defineInRange(k.key(), t.defaultElementalBreakdown(k), -1, 100));
                }
                b.pop(); // elemental_breakdown

                b.comment("Percent of vanilla's shield wear taken per damage kind (100 = vanilla, 0 = free, 200 = double).")
                        .push("durabilityCost");
                for (ShieldGroup g : ShieldGroup.values()) {
                    durabilityCost.put(g, b.defineInRange(g.key(), 100, 0, 1000));
                }
                b.pop();

                b.pop(); // tier id
            }
        }
    }

    /** Debug knob for the damage-tag layer ({@code damage/} package). */
    public static final class DamageTagConfig {
        public final ForgeConfigSpec.BooleanValue logSeenSources;

        private DamageTagConfig(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Damage tags classify every hit (fire, magic, electric, ...) so shields - and later armour -",
                    "can treat kinds of damage differently. Every tag is a data/creativeengineering/damage_tags/",
                    "<name>.json file, merged across datapacks and reloaded by /reload: most list message ids",
                    "directly (fire.json, magic.json, ... - these mirror vanilla's own damage flags, but as a",
                    "visible, editable file instead of a hidden check), and a couple are pure META tags that",
                    "only #include other tags (elemental.json = fire+cold+electric+chemical; physical.json =",
                    "melee+machinery+piercing+firearm+explosion+projectile). Shields read only these tags -",
                    "never a raw damage flag - so reclassifying a source, or moving a whole kind of damage",
                    "between shield columns, is always a data edit here, never Java."
            ).push("damage_tags");

            logSeenSources = b.comment(
                    "Discovery aid. When true, the first time each distinct damage source (message id + flags)",
                    "hurts anything on the server it is logged once as '[damage-tags] seen msgId=...'.",
                    "'tags=NONE' means no damage_tags file claims that source yet. Off by default.",
                    "Live: takes effect immediately."
            ).define("logSeenSources", false);

            b.pop(); // damage_tags
        }
    }

    /** How a structure's grass/dirt base is swapped for the local ground ({@code worldgen/GroundMatch}). */
    public enum GroundMode { TERRAIN, PALETTE, REMOVE }

    /** Where /ceplace puts each structure vertically ({@code worldgen/MassPlacer}). */
    public enum VerticalAlign { AUTO, KEEP, GROUND }

    /** Which structure starts the earth-fill check logs ({@code worldgen/EarthFill}). */
    public enum EarthFillLog { OFF, CANCELLED, ALL }

    /**
     * Every worldgen system's switch and every tuning constant that isn't per-structure data. Per-structure
     * and per-piece values stay in kubejs/generation_scripts/worldgen/*.json (hundreds of entries); this is
     * the layer above them, for testing: turn a system off, scale every structure's rate, tune the solvers.
     * Read through {@code worldgen/WorldgenConfig}, which falls back to the defaults before the file loads.
     * Most of it is read at world load (rejoin to apply); ground matching and /ceplace read it live.
     */
    public static final class Worldgen {
        public final WorldgenSystems systems;
        public final WorldgenDebug debug;
        public final PoolTuning pools;
        public final BiomeSolver biomeSolver;
        public final GroundMatch groundMatch;
        public final EarthFill earthFill;
        public final WaterFill waterFill;
        public final TerrainAdaptation terrainAdaptation;
        public final MetaStructures metaStructures;
        public final MassPlace massPlace;
        public final OceanGeodes oceanGeodes;
        public final PlacementPriorityCfg placementPriority;
        public final LocateCfg locate;
        public final DistantCfg distant;

        private Worldgen(ForgeConfigSpec.Builder b) {
            b.comment(
                    "World generation: switches per system, a debug rate multiplier, and the constants of the pack's",
                    "worldgen code. The per-structure, per-piece and per-biome tables are the JSON files in",
                    "kubejs/generation_scripts/worldgen/ (see feature_writeup/structure_tuning.md). Structure and",
                    "biome settings apply when a world loads (rejoin), and only to chunks generated afterwards."
            ).push("worldgen");
            systems = new WorldgenSystems(b);
            debug = new WorldgenDebug(b);
            pools = new PoolTuning(b);
            biomeSolver = new BiomeSolver(b);
            groundMatch = new GroundMatch(b);
            earthFill = new EarthFill(b);
            waterFill = new WaterFill(b);
            terrainAdaptation = new TerrainAdaptation(b);
            metaStructures = new MetaStructures(b);
            massPlace = new MassPlace(b);
            oceanGeodes = new OceanGeodes(b);
            placementPriority = new PlacementPriorityCfg(b);
            locate = new LocateCfg(b);
            distant = new DistantCfg(b);
            b.pop(); // worldgen
        }
    }

    /** One switch per worldgen system, so each can be A/B tested against the mods' own defaults. */
    public static final class WorldgenSystems {
        public final ForgeConfigSpec.BooleanValue structureGroups;
        public final ForgeConfigSpec.BooleanValue structureFrequencies;
        public final ForgeConfigSpec.BooleanValue structureBiomeRules;
        public final ForgeConfigSpec.BooleanValue structureBiomeFits;
        public final ForgeConfigSpec.BooleanValue poolTuning;
        public final ForgeConfigSpec.BooleanValue lootContainerSwaps;
        public final ForgeConfigSpec.BooleanValue biomeLayout;
        public final ForgeConfigSpec.BooleanValue deadPlacementFilter;
        public final ForgeConfigSpec.BooleanValue groundMatch;
        public final ForgeConfigSpec.BooleanValue earthFill;
        public final ForgeConfigSpec.BooleanValue waterFill;
        public final ForgeConfigSpec.BooleanValue terrainAdaptation;
        public final ForgeConfigSpec.BooleanValue templateTrim;
        public final ForgeConfigSpec.BooleanValue metaStructures;
        public final ForgeConfigSpec.BooleanValue structureReplacements;
        public final ForgeConfigSpec.BooleanValue wildCrops;
        public final ForgeConfigSpec.BooleanValue wildFlowers;
        public final ForgeConfigSpec.BooleanValue mobSpawns;
        public final ForgeConfigSpec.BooleanValue beeNests;
        public final ForgeConfigSpec.BooleanValue oceanGeodes;
        public final ForgeConfigSpec.BooleanValue placementPriority;
        public final ForgeConfigSpec.BooleanValue templateBlockSwaps;

        private WorldgenSystems(ForgeConfigSpec.Builder b) {
            b.comment("Switch a system off to see the mods' own behaviour. Off never deletes a config file.").push("systems");
            structureGroups = b.comment(
                    "structure_groups.json + group/* tags (kubejs worldgen/structure_overrides.js). Off: every structure",
                    "set as its mod ships it (debug.structureDensity still applies). Rejoin to apply."
            ).define("structureGroups", true);
            structureFrequencies = b.comment(
                    "The per-structure frequency roll (patcherMod StructureFrequencies). Off: every group member spawns",
                    "whenever it wins its slot, i.e. at its weight share only. Rejoin to apply."
            ).define("structureFrequencies", true);
            structureBiomeRules = b.comment(
                    "biome_rules in structure_groups.json (patcherMod StructureBiomeRules): only allowed structures start in",
                    "the rule's biomes (water: ships, wrecks, ocean ruins...). Off: every structure uses its own biome list. Rejoin."
            ).define("structureBiomeRules", true);
            structureBiomeFits = b.comment(
                    "biome_fits.json + the land/* structure tags (patcherMod StructureBiomeFits): a structure in a fit tag spawns in",
                    "its fit's biomes (land/oak: land/oak + land biomes) instead of its own list. Off: its own list. Rejoin; new chunks only."
            ).define("structureBiomeFits", true);
            poolTuning = b.comment("pool_config.json / pool_tags.json village-house weights (PoolTuner). Off: every pool at its first-seen weights. Rejoin.")
                    .define("poolTuning", true);
            lootContainerSwaps = b.comment("loot_container_swaps.json: modded loot containers placed by structures become vanilla ones for Lootr.")
                    .define("lootContainerSwaps", true);
            biomeLayout = b.comment("biome_config.json multipliers / replacements (BiomeLayoutTuner). Only affects worlds created afterwards.")
                    .define("biomeLayout", true);
            deadPlacementFilter = b.comment("Explorer maps and /locate skip structure sets at frequency 0 (without it they can hang the server).")
                    .define("deadPlacementFilter", true);
            groundMatch = b.comment("Swap a structure's grass/dirt base for the local ground where grass isn't native ([worldgen.ground_match]).")
                    .define("groundMatch", true);
            earthFill = b.comment("Cancel a structure whose foundation would need too much earth over water or air ([worldgen.earth_fill]).",
                    "New chunks only.")
                    .define("earthFill", true);
            waterFill = b.comment("Cancel a water structure whose water would replace too much dry ground or air ([worldgen.water_fill]).",
                    "New chunks only.")
                    .define("waterFill", true);
            terrainAdaptation = b.comment("The creativeengineering:terrain_adaptation/* structure tags replace a structure's own terrain adaptation",
                    "(none, bury, beard_thin, beard_box, or the pack's soft beard; [worldgen.terrain_adaptation]). Rejoin; new chunks only.")
                    .define("terrainAdaptation", true);
            templateTrim = b.comment("Structures in the creativeengineering:template_trim tag skip their template's air and bare pad (grass,",
                    "dirt, plants) outside the built columns, so the terrain around them stays ([worldgen.terrain_adaptation]). New chunks only.")
                    .define("templateTrim", true);
            metaStructures = b.comment("meta_structures.json: our structures built from other structures and features (a village with peaceful",
                    "towers, ruin clusters, pillager camps with siege weapons, a driftwood flotilla...; patcherMod MetaStructure, [worldgen.meta_structures]).",
                    "Off: none generate (their anchors spawn plain). Rejoin; new chunks only.")
                    .define("metaStructures", true);
            structureReplacements = b.comment("Group members with \"replaces\" + \"share\" in structure_groups.json take that share of another member's slots",
                    "(the vanilla swamp hut in YUNG's witch hut slots; patcherMod MetaSlots). Off: they never generate and the",
                    "structures they replace keep every slot. Rejoin; new chunks only.")
                    .define("structureReplacements", true);
            wildCrops = b.comment("wild_crops.json: which biomes each wild crop and fruit tree spawns in (kubejs worldgen/wild_crops.js).",
                    "Off: every crop as its mod ships it. Rejoin to apply; only new chunks change.")
                    .define("wildCrops", true);
            wildFlowers = b.comment("wild_flowers.json: flower biomes and rates, Botania colors per biome, Flower Patch blocks in wild flowers",
                    "(kubejs worldgen/wild_flowers.js). Off: every flower as its mod ships it. Rejoin to apply; only new chunks change.")
                    .define("wildFlowers", true);
            mobSpawns = b.comment("mob_spawns.json: per-biome mob spawn weights, additions and removals (kubejs worldgen/mob_spawns.js,",
                    "patcherMod SpawnTuning). Off: every mob spawns as its mod ships it. Rejoin to apply; existing worlds change too.")
                    .define("mobSpawns", true);
            beeNests = b.comment("bee_nests.json: per-biome bee nest chance on generated trees, plus extra bee-nest trees (kubejs",
                    "worldgen/bee_nests.js, patcherMod BeeNests). Off: nests as the mods ship them. Rejoin to apply; only new chunks change.")
                    .define("beeNests", true);
            oceanGeodes = b.comment("Geodes under oceans generate lower so fewer break through the sea floor ([worldgen.ocean_geodes];",
                    "patcherMod OceanGeodes: vanilla amethyst and every Geode Plus geode). Off: at their own height. New chunks only.")
                    .define("oceanGeodes", true);
            placementPriority = b.comment("Placement priority (structure_groups.json \"priority\"; patcherMod PlacementPriority): a structure whose",
                    "pieces would overlap a higher-ranked structure's never starts (settlements > pillagers > dungeons > houses",
                    "and camps > ruins), and companion villages around fortified towns. Off: structures may overlap, no",
                    "companions. Rejoin; new chunks only.")
                    .define("placementPriority", true);
            templateBlockSwaps = b.comment("meat_block_swaps.json (patcherMod TemplateBlockSwaps): marked blocks in structure templates are swapped",
                    "when the template loads (lone red glazed terracotta \"meat\" -> Architect's Palette salmon log). Off: templates as",
                    "the mods ship them. Rejoin; new chunks only.")
                    .define("templateBlockSwaps", true);
            b.pop(); // systems
        }
    }

    /** Testing knobs that are wrong for a real world. */
    public static final class WorldgenDebug {
        public final ForgeConfigSpec.DoubleValue structureDensity;

        private WorldgenDebug(ForgeConfigSpec.Builder b) {
            b.comment("For testing only; leave at the defaults for play.").push("debug");
            structureDensity = b.comment(
                    "Multiplies how often EVERY structure spawns (natural generation, every set, grouped or not). 1 = off.",
                    "Below 1: each set's frequency is multiplied (exact, spacing and gaps unchanged). Above 1: grids shrink",
                    "by sqrt(x) and the frequency absorbs the rounding, so the rate is exact until a grid reaches its",
                    "minimum (spacing = separation + 1); separations shrink with the grid. structure_tag_report.md shows",
                    "the result. Rejoin to apply; only new chunks change."
            ).defineInRange("structureDensity", 1.0, 0.01, 100.0);
            b.pop(); // debug
        }
    }

    /** {@code worldgen/PoolTuner} constants. */
    public static final class PoolTuning {
        public final ForgeConfigSpec.IntValue maxFractionScale;

        private PoolTuning(ForgeConfigSpec.Builder b) {
            b.push("pools");
            maxFractionScale = b.comment(
                    "Fractional piece weights are made whole by scaling the pool's list by up to this factor. Jigsaw",
                    "tries every copy of a piece in a pool, so a bigger factor is more exact but slower to fail."
            ).defineInRange("maxFractionScale", 10, 1, 100);
            b.pop(); // pools
        }
    }

    /** {@code worldgen/BiomeLayoutTuner} solver constants (they change the solve, so they're part of its cache key). */
    public static final class BiomeSolver {
        public final ForgeConfigSpec.IntValue maxCandidates;
        public final ForgeConfigSpec.IntValue rounds;

        private BiomeSolver(ForgeConfigSpec.Builder b) {
            b.push("biome_solver");
            maxCandidates = b.comment("Biomes considered per climate sample (the closest ones). More = slower, rarely different.")
                    .defineInRange("maxCandidates", 24, 2, 256);
            rounds = b.comment("Penalty-adjustment rounds. More = closer to the target shares, slower world creation.")
                    .defineInRange("rounds", 20, 1, 200);
            b.pop(); // biome_solver
        }
    }

    /**
     * Knobs for {@code worldgen/GroundMatch}: a structure template's base blocks (grass, dirt...) are
     * swapped for the ground actually around it when that ground isn't grass/dirt (a grassy ruin in
     * volcanic peaks sits on basalt instead). Which blocks count is block-tag driven (KubeJS
     * blocktags.js), so only the behaviour lives here. Read while chunks generate: already-generated
     * chunks never change. On/off: [worldgen.systems] groundMatch.
     */
    public static final class GroundMatch {
        public final ForgeConfigSpec.EnumValue<GroundMode> mode;
        public final ForgeConfigSpec.IntValue baseLayers;
        public final ForgeConfigSpec.DoubleValue nativeShare;
        public final ForgeConfigSpec.IntValue minSamples;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> families;
        public final ForgeConfigSpec.BooleanValue log;
        public final ForgeConfigSpec.IntValue centerStride;
        public final ForgeConfigSpec.IntValue neighbourStride;
        public final ForgeConfigSpec.IntValue subDepth;
        public final ForgeConfigSpec.IntValue maxDig;
        public final ForgeConfigSpec.IntValue maxPlantHeight;

        private GroundMatch(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Structure bases that don't match the ground. As a structure generates, grass/dirt in the bottom",
                    "layers of its templates is compared with the natural ground sampled around that chunk; where the",
                    "ground isn't grass/dirt (volcanic peaks, deserts, snowy slopes, badlands...) the base becomes that",
                    "ground instead. Blocks: tags creativeengineering:ground_match/* (kubejs blocktags.js). Exempt a",
                    "structure with the structure tag creativeengineering:ground_match/exempt. New chunks only."
            ).push("ground_match");

            mode = b.comment(
                    "TERRAIN: a base block whose spot already holds natural ground is skipped, so the terrain stays;",
                    "  the rest (base above the ground) is filled with blocks sampled from the local ground.",
                    "PALETTE: every base block becomes a sampled ground block.",
                    "REMOVE: base blocks are never placed (whatever is there stays, air included)."
            ).defineEnum("mode", GroundMode.TERRAIN);

            baseLayers = b.comment(
                    "How many template layers from the bottom count as the base. Family blocks higher up (roof",
                    "gardens, planters) are left alone."
            ).defineInRange("baseLayers", 3, 1, 16);

            nativeShare = b.comment(
                    "A family counts as native when at least this share of the sampled ground tops belong to it;",
                    "native chunks are left exactly as the template has them."
            ).defineInRange("nativeShare", 0.35, 0.0, 1.0);

            minSamples = b.comment(
                    "Fewest natural ground columns needed to decide (columns under structures are skipped). With",
                    "fewer, even after also sampling the 8 neighbouring chunks, the base is left as it is."
            ).defineInRange("minSamples", 16, 1, 1024);

            families = b.comment(
                    "Block tags, one per base family. A family block in a base is swapped where its family isn't",
                    "native. Add a tag here (and fill it in blocktags.js) to match another kind of base, e.g. sand."
            ).defineList("families", List.of("creativeengineering:ground_match/grass"),
                    o -> o instanceof String str && str.contains(":"));

            log = b.comment("Log each changed template (chunk, family share, palettes). For testing.")
                    .define("log", false);

            b.comment("How the natural ground is sampled.").push("sampling");
            centerStride = b.comment("Every Nth column of the chunk being placed is sampled (2 = 64 columns).")
                    .defineInRange("centerStride", 2, 1, 16);
            neighbourStride = b.comment("Every Nth column of each of the 8 neighbouring chunks (4 = 16 each).")
                    .defineInRange("neighbourStride", 4, 1, 16);
            subDepth = b.comment("Blocks sampled under each column's top block (the under-block palette).")
                    .defineInRange("subDepth", 3, 1, 16);
            maxDig = b.comment("How far down past leaves, logs and plants a column is searched for its ground.")
                    .defineInRange("maxDig", 24, 1, 128);
            maxPlantHeight = b.comment("Tallest plant (in blocks) removed from a swapped block.")
                    .defineInRange("maxPlantHeight", 4, 1, 16);
            b.pop(); // sampling

            b.pop(); // ground_match
        }
    }

    /**
     * Knobs for {@code worldgen/EarthFill}: a structure is cancelled before it is stored when the earth
     * blocks in the bottom layers of its templates would sit on water or in air, judged against the
     * terrain the generator predicts there. Which blocks are earth is a block tag (kubejs blocktags.js);
     * structures that float on purpose are in the structure tag creativeengineering:earth_fill/exempt
     * (kubejs worldgen/earth_fill.js). Read while chunks generate. On/off: [worldgen.systems] earthFill.
     */
    public static final class EarthFill {
        public final ForgeConfigSpec.IntValue maxOverWater;
        public final ForgeConfigSpec.IntValue beardReach;
        public final ForgeConfigSpec.IntValue buryReach;
        public final ForgeConfigSpec.IntValue maxMoundHeight;
        public final ForgeConfigSpec.BooleanValue prune;
        public final ForgeConfigSpec.IntValue pruneMaxHanging;
        public final ForgeConfigSpec.IntValue pruneMaxMound;
        public final ForgeConfigSpec.IntValue baseLayers;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> blocks;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> overrides;
        public final ForgeConfigSpec.IntValue sampleGrid;
        public final ForgeConfigSpec.EnumValue<EarthFillLog> log;
        public final EarthFillSizes sizes;

        private EarthFill(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Structures that would need a lot of earth to stand on. Just before a structure start is stored,",
                    "every earth block (tag below) in the bottom layers of its template pieces is compared with the",
                    "terrain the generator predicts at that spot. Earth in water counts toward maxOverWater, earth in air",
                    "toward the size's air limit, but only where the structure's beard (its terrain adaptation) won't",
                    "fill under it: ground deeper than beardReach (buryReach for bury), or always with no adaptation.",
                    "Columns whose ground lies more than maxMoundHeight below a piece count toward the size's mound",
                    "limit. Past any limit the whole structure is cancelled and its slot stays empty (a slot shared",
                    "with other structures tries the next pick). The limits scale with the structure's size (its earth",
                    "base block count, [worldgen.earth_fill.sizes]). Village streets that follow the terrain are never",
                    "counted. Exempt a structure with the structure tag creativeengineering:earth_fill/exempt. New",
                    "chunks only; /place structure is not checked."
            ).push("earth_fill");

            maxOverWater = b.comment(
                    "Most earth blocks that may stand in water the beard won't fill (a pad reaching out over a lake or the",
                    "sea deeper than beardReach). -1 = no limit, 0 = none at all. Vanilla villages set house floors into the",
                    "top water layer of rivers and coasts; their beard fills under them, so that doesn't count. Per",
                    "structure: see overrides."
            ).defineInRange("maxOverWater", 24, -1, 100000);
            beardReach = b.comment(
                    "How deep under a piece the beard (beard_thin, beard_box, the soft beard) still fills the ground, in",
                    "blocks between the piece's bottom and the ground (air and water both: the beard fills either). Earth",
                    "over a deeper gap hangs and counts; a shallower one is filled and doesn't. Measured with",
                    "tools/earth_fill_sim.sh beard on vanilla villages: the whole gap is filled 81% of the time at 6 blocks,",
                    "35% at 7."
            ).defineInRange("beardReach", 6, 0, 32);
            buryReach = b.comment(
                    "The same for bury (the structure sinks into the ground): filled 100% to 10 blocks, 27% at 11."
            ).defineInRange("buryReach", 10, 0, 32);
            maxMoundHeight = b.comment(
                    "A column whose ground lies more than this many blocks below a piece's bottom is a mound column:",
                    "the beard would build a tall pillar there, or can't reach the piece at all (an edge of a ravine, a",
                    "pad over a cliff). Each column counts once per structure, toward the size's mound limit."
            ).defineInRange("maxMoundHeight", 8, 0, 64);
            prune = b.comment(
                    "For structures in the structure tag creativeengineering:earth_fill/prune (villages): before the whole start",
                    "is judged, each jigsaw piece but the first is judged alone, and one past pruneMaxHanging or pruneMaxMound",
                    "is dropped with every piece attached below it (a house over deep water with its villagers). The street",
                    "that led to it stays as a dead end. Then the rest is judged as usual. Off: the whole start is judged."
            ).define("prune", true);
            pruneMaxHanging = b.comment(
                    "Earth blocks of one piece over water or air the beard won't fill before that piece is dropped. -1 = never."
            ).defineInRange("pruneMaxHanging", 8, -1, 100000);
            pruneMaxMound = b.comment(
                    "Mound columns of one piece before that piece is dropped. -1 = never."
            ).defineInRange("pruneMaxMound", 4, -1, 100000);
            baseLayers = b.comment(
                    "How many template layers from the bottom count as the foundation. Earth higher up (roof gardens,",
                    "planters) is not counted: above the ground everything is air."
            ).defineInRange("baseLayers", 3, 1, 16);
            blocks = b.comment(
                    "Block tags, one entry each, that count as earth. Fill the default in kubejs blocktags.js; add a tag",
                    "here to count another kind of block."
            ).defineList("blocks", List.of("creativeengineering:earth_fill/earth"),
                    o -> o instanceof String str && str.contains(":"));
            overrides = b.comment(
                    "Per-structure limits, one entry each: \"<structure id or #structure tag>=<water>,<air>[,<mound>]\",",
                    "e.g. \"philipsruins:ancient_ruins=0,16\" (never over water, at most 16 over air after the beard,",
                    "mound from its size) or \"#creativeengineering:type/village=-1,-1,-1\" (no limits). They replace the",
                    "size's limits. The first matching entry wins; -1 = no limit."
            ).defineList("overrides", List.of(), o -> o instanceof String str && str.contains("="));
            sampleGrid = b.comment(
                    "The predicted terrain is read once per N x N block cell instead of per column (cheaper, a little",
                    "coarser at cliffs). 1 = every column. [worldgen.water_fill] uses it too."
            ).defineInRange("sampleGrid", 2, 1, 8);
            log = b.comment(
                    "CANCELLED: log each structure that is cancelled (id, chunk, counts, limits). ALL: log every checked",
                    "structure that has any earth in its base. For tuning the limits."
            ).defineEnum("log", EarthFillLog.CANCELLED);
            sizes = new EarthFillSizes(b);
            b.pop(); // earth_fill
        }
    }

    /**
     * {@code [worldgen.earth_fill.sizes]}: earth-fill limits by the size of the structure, so a fish pond and a
     * fortified town aren't held to one number. The size is the start's own earth base block count.
     */
    public static final class EarthFillSizes {
        public final ForgeConfigSpec.ConfigValue<List<? extends Integer>> bounds;
        public final ForgeConfigSpec.ConfigValue<List<? extends Integer>> small;
        public final ForgeConfigSpec.ConfigValue<List<? extends Integer>> medium;
        public final ForgeConfigSpec.ConfigValue<List<? extends Integer>> large;
        public final ForgeConfigSpec.ConfigValue<List<? extends Integer>> huge;

        private EarthFillSizes(ForgeConfigSpec.Builder b) {
            b.comment(
                    "A structure's size is its earth base block count (each start counted as it generates, a meta",
                    "structure part by part). Each size has [maxOverAir, maxMound]: earth blocks over air after the beard,",
                    "and mound columns; -1 = no limit. First guesses, to calibrate with log = ALL."
            ).push("sizes");
            bounds = b.comment(
                    "Earth base blocks where medium, large and huge start: under the first is small, from the last on huge.")
                    .defineList("bounds", List.of(150, 600, 2000), o -> o instanceof Integer i && i > 0);
            small = b.comment("[maxOverAir, maxMound] for small structures (a well, a small ruin, a fish pond).")
                    .defineList("small", List.of(24, 8), o -> o instanceof Integer i && i >= -1);
            medium = b.comment("[maxOverAir, maxMound] for medium structures (a house, a camp, a tower).")
                    .defineList("medium", List.of(64, 24), o -> o instanceof Integer i && i >= -1);
            large = b.comment("[maxOverAir, maxMound] for large structures (a village, a fortified village, a mansion).")
                    .defineList("large", List.of(160, 64), o -> o instanceof Integer i && i >= -1);
            huge = b.comment("[maxOverAir, maxMound] for huge structures.")
                    .defineList("huge", List.of(320, 128), o -> o instanceof Integer i && i >= -1);
            b.pop(); // sizes
        }
    }

    /**
     * Knobs for the water half of {@code worldgen/EarthFill}: a structure in the structure tag
     * creativeengineering:water_fill/structures (kubejs worldgen/water_fill.js) is cancelled before it is
     * stored when the water its templates carry would replace too much dry ground or air, judged against
     * the same predicted terrain as earth fill (and its sampleGrid). On/off: [worldgen.systems] waterFill.
     */
    public static final class WaterFill {
        public final ForgeConfigSpec.IntValue maxIntoGround;
        public final ForgeConfigSpec.IntValue maxIntoAir;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> overrides;
        public final ForgeConfigSpec.EnumValue<EarthFillLog> log;

        private WaterFill(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Water structures that would flood the land. Just before a structure start in the structure tag",
                    "creativeengineering:water_fill/structures is stored, every water block of its template pieces (water,",
                    "waterlogged blocks, kelp, seagrass; all layers) is compared with the terrain the generator predicts",
                    "at that spot. Water in water is fine, and so is water replacing a sea, lake or river bed. Water",
                    "replacing dry ground counts toward maxIntoGround, water in air (above the sea surface or on land)",
                    "toward maxIntoAir; past either limit the whole structure is cancelled and its slot stays empty.",
                    "Uses [worldgen.earth_fill] sampleGrid. New chunks only; /place structure is not checked."
            ).push("water_fill");

            maxIntoGround = b.comment(
                    "Most water blocks that may replace dry ground (a ruin reaching onto a beach or into a hillside).",
                    "-1 = no limit, 0 = none at all. Per structure: see overrides."
            ).defineInRange("maxIntoGround", 32, -1, 100000);
            maxIntoAir = b.comment(
                    "Most water blocks that may land in air (water standing above the sea surface or on land, where it",
                    "spills). -1 = no limit, 0 = none at all. Per structure: see overrides."
            ).defineInRange("maxIntoAir", 16, -1, 100000);
            overrides = b.comment(
                    "Per-structure limits, one entry each: \"<structure id or #structure tag>=<ground>,<air>\". The default",
                    "lets the structures in creativeengineering:water_fill/ponds (fish ponds, a house on a pond) dig their",
                    "water into the ground and allows them 16 in air. The first matching entry wins; -1 = no limit.",
                    "Only structures in creativeengineering:water_fill/structures are checked at all."
            ).defineList("overrides", List.of("#creativeengineering:water_fill/ponds=-1,16"), o -> o instanceof String str && str.contains("="));
            log = b.comment(
                    "CANCELLED: log each structure that is cancelled (id, chunk, counts, limits). ALL: log every checked",
                    "structure that carries any water. For tuning the limits."
            ).defineEnum("log", EarthFillLog.CANCELLED);
            b.pop(); // water_fill
        }
    }

    /**
     * Knobs for {@code worldgen/TerrainAdaptation} and {@code worldgen/TemplateTrim}: which structures get which
     * terrain adaptation, and which are trimmed, is structure tags (kubejs worldgen/terrain_adaptation.js); only
     * the soft beard's width lives here. On/off: [worldgen.systems] terrainAdaptation and templateTrim.
     */
    public static final class TerrainAdaptation {
        public final ForgeConfigSpec.IntValue skirtBump;

        private TerrainAdaptation(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Terrain around structures. Vanilla's beard flattens the whole template bounding box and blends it out",
                    "over about 8 blocks around it, so a small structure with a flat pad or a lot of saved air leaves a flat",
                    "rectangle the size of its whole template in a curved hillside. Structures in the structure tag",
                    "creativeengineering:terrain_adaptation/soft get the soft beard instead: the same fill and carve, but",
                    "only under the template's built columns (pad and air columns left out when the structure is also in",
                    "creativeengineering:template_trim), blending out over a short round skirt. Tags: kubejs",
                    "worldgen/terrain_adaptation.js. New chunks only."
            ).push("terrain_adaptation");
            skirtBump = b.comment(
                    "Blocks added to the soft beard's minimum skirt of 4 (where its fill and carve fade out, measured from",
                    "the built footprint). 0 = the tightest blend; 4 = as wide as vanilla's beard (8), but round and",
                    "around the footprint; 8 = the most there is room for (12, vanilla's reach). Read per chunk."
            ).defineInRange("skirtBump", 0, 0, 8);
            b.pop(); // terrain_adaptation
        }
    }

    /** {@code /ceplace}: whole structures or raw templates laid out in rows, for inspecting them in a flat world. */
    /**
     * Knobs for {@code worldgen/MetaStructure}. The structures themselves are defined in
     * kubejs/generation_scripts/worldgen/meta_structures.json; on/off is [worldgen.systems] metaStructures.
     */
    /** worldgen/OceanGeodes: how far geodes under oceans are pushed down; on/off is [worldgen.systems] oceanGeodes. */
    public static final class OceanGeodes {
        public final ForgeConfigSpec.IntValue drop;
        public final ForgeConfigSpec.DoubleValue stayShare;
        public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> untouched;

        private OceanGeodes(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Geodes whose column is an ocean (the biome at the sea floor is #minecraft:is_ocean) are moved down before",
                    "they generate, so fewer of them poke through the sea floor. Which geodes stay is a fixed roll per position."
            ).push("ocean_geodes");
            drop = b.comment("Blocks a geode under an ocean is moved down.")
                    .defineInRange("drop", 10, 0, 64);
            stayShare = b.comment("Share of geodes under oceans that keep their own height (0.1 = 1 in 10).")
                    .defineInRange("stayShare", 0.1, 0.0, 1.0);
            untouched = b.comment("Geodes whose inner layer is one of these blocks are left at their own height (Geode Plus'",
                    "prismarine geode belongs on the sea floor).")
                    .defineList("untouched", java.util.List.of("geode:budding_prismarine"), o -> o instanceof String);
            b.pop();
        }
    }

    /** worldgen/PlacementPriority; on/off is [worldgen.systems] placementPriority. */
    public static final class PlacementPriorityCfg {
        public final ForgeConfigSpec.BooleanValue log;

        private PlacementPriorityCfg(ForgeConfigSpec.Builder b) {
            b.push("placement_priority");
            log = b.comment("Log every structure start dropped because a higher-ranked structure overlaps it, and every",
                    "companion village. For testing.")
                    .define("log", false);
            b.pop();
        }
    }

    /**
     * Structure searches (/locate, explorer maps, map trades, dolphins, Eyes of Ender; all through Async Locator):
     * the cheap "could it start here?" prediction, chunk loads for misses, StructureCheck thread safety, miss
     * logging and the /celocate batch and census commands. See feature_writeup/locate.md.
     */
    public static final class LocateCfg {
        public final ForgeConfigSpec.BooleanValue foldPlacement;
        public final ForgeConfigSpec.BooleanValue foldBiomes;
        public final ForgeConfigSpec.BooleanValue nonBlockingLoads;
        public final ForgeConfigSpec.BooleanValue threadSafeStructureCheck;
        public final ForgeConfigSpec.IntValue logMissesAtLeast;
        public final ForgeConfigSpec.IntValue batchRadius;
        public final ForgeConfigSpec.IntValue batchTimeoutSeconds;
        public final ForgeConfigSpec.IntValue censusRegionChunks;
        public final ForgeConfigSpec.IntValue loadTimeoutSeconds;
        public final ForgeConfigSpec.IntValue searchTimeoutSeconds;

        private LocateCfg(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Structure searches. A miss is a slot the cheap prediction called possible, so the search generated that",
                    "chunk's structure starts, and nothing was there. Each fold below makes the prediction say \"no\" earlier,",
                    "only where generation would certainly say no. Async Locator's thread count is its own server config",
                    "(serverconfig/asynclocator-server.toml, asyncLocatorThreads)."
            ).push("locate");
            foldPlacement = b.comment(
                    "Stage A: the structure set's own frequency and exclusion zone (vanilla checks both when generating but",
                    "not when searching). Exact. Applies to new searches immediately.")
                    .define("foldPlacement", true);
            foldBiomes = b.comment(
                    "Stage B: the biome at the structure's generation point, with the pack's biome rules and fits (vanilla",
                    "never tests the biome in the prediction). Exact. Applies to new searches immediately.")
                    .define("foldBiomes", true);
            nonBlockingLoads = b.comment(
                    "A search running off the server thread (Async Locator) waits for a miss's chunk itself instead of making",
                    "the server thread block until the chunk's structure starts exist. Off: vanilla's blocking load.")
                    .define("nonBlockingLoads", true);
            threadSafeStructureCheck = b.comment(
                    "Make vanilla's structure-presence cache safe for several search threads at once (needed before raising",
                    "asyncLocatorThreads above 1). Read when a dimension loads (rejoin to apply).")
                    .define("threadSafeStructureCheck", true);
            logMissesAtLeast = b.comment(
                    "Log one [locate] line for every search with at least this many misses (0 = every search, -1 = never).")
                    .defineInRange("logMissesAtLeast", 8, -1, Integer.MAX_VALUE);
            batchRadius = b.comment("Default search radius of /celocate batch, in chunks (/locate uses 100).")
                    .defineInRange("batchRadius", 100, 1, 1000);
            batchTimeoutSeconds = b.comment(
                    "A /celocate batch search still running after this long is reported as timed out and cancelled (Async",
                    "Locator never completes a search that threw).")
                    .defineInRange("batchTimeoutSeconds", 120, 5, 3600);
            censusRegionChunks = b.comment(
                    "/celocate census splits its square into regions this many chunks wide; each region is one task on",
                    "Async Locator's thread pool.")
                    .defineInRange("censusRegionChunks", 32, 4, 256);
            loadTimeoutSeconds = b.comment(
                    "A non-blocking miss load that hasn't finished after this long counts as a miss (protects search threads",
                    "from a stopping server).")
                    .defineInRange("loadTimeoutSeconds", 300, 10, 3600);
            searchTimeoutSeconds = b.comment(
                    "Any structure search (/locate, maps, Async Locator) still running after this long ends as not found and",
                    "logs a [locate] line with its counters; its thread is free again. /celocate stop ends running searches",
                    "too, and so does the server stopping. 0 = no deadline. Applies to new searches immediately.")
                    .defineInRange("searchTimeoutSeconds", 120, 0, 3600);
            b.pop();
        }
    }

    /**
     * Distant Horizons' own world generation (its throwaway LOD chunks, never real chunks): the bottom of the world
     * is not generated; oceans get a fake seabed instead. See feature_writeup/locate.md 3.7.
     */
    public static final class DistantCfg {
        public final ForgeConfigSpec.BooleanValue clip;
        public final ForgeConfigSpec.IntValue clipBelowY;
        public final ForgeConfigSpec.IntValue oceanFloorY;
        public final ForgeConfigSpec.IntValue deepOceanFloorY;
        public final ForgeConfigSpec.IntValue floorVariation;
        public final ForgeConfigSpec.IntValue floorScale;
        public final ForgeConfigSpec.BooleanValue skipFeatures;

        private DistantCfg(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Distant Horizons' distant generation only (chunks it generates for LODs and throws away). Below clipBelowY",
                    "no terrain noise is computed: land columns are filled with stone, and an ocean column whose block at",
                    "clipBelowY is water keeps water down to a fake sand/gravel floor that rolls around a per-biome height.",
                    "Chunks players walk in are never affected."
            ).push("distant");
            clip = b.comment("Clip Distant Horizons' generation below clipBelowY.")
                    .define("clip", true);
            clipBelowY = b.comment("No noise below this y (rounded down to a multiple of 16).")
                    .defineInRange("clipBelowY", 48, -64, 320);
            oceanFloorY = b.comment("Fake floor height in ocean biomes (capped just under clipBelowY).")
                    .defineInRange("oceanFloorY", 48, -64, 320);
            deepOceanFloorY = b.comment("Fake floor height in deep ocean biomes (#minecraft:is_deep_ocean).")
                    .defineInRange("deepOceanFloorY", 8, -64, 320);
            floorVariation = b.comment("How far the fake floor rolls above and below its height, in blocks.")
                    .defineInRange("floorVariation", 4, 0, 32);
            floorScale = b.comment("Width of the fake floor's rolls, in blocks (seeded from the world seed).")
                    .defineInRange("floorScale", 24, 4, 256);
            skipFeatures = b.comment("Skip placed features (ores, geodes, cave decoration) that start below clipBelowY.")
                    .define("skipFeatures", true);
            b.pop();
        }
    }

    public static final class MetaStructures {
        public final ForgeConfigSpec.BooleanValue log;

        private MetaStructures(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Meta structures: one structure start made of other structures (an anchor such as a village, plus",
                    "satellites such as towers around it) and placed features (driftwood on the sea). Every part keeps",
                    "within 7 chunks of the start so the whole thing generates; a part that can't fit (biome, room)",
                    "is retried elsewhere, and a meta structure short of a part's minimum count doesn't spawn at all.",
                    "A meta with an anchor only takes a share of the slots its anchor already won, so no structure's rate",
                    "changes. /locate finds a meta by its id and a structure inside one by the structure's own id."
            ).push("meta_structures");
            log = b.comment(
                    "Log every meta structure start that is assembled: its anchor, each part and how many of each",
                    "fitted, or which part fell short (the slot is then the plain anchor)."
            ).define("log", false);
            b.pop(); // meta_structures
        }
    }

    public static final class MassPlace {
        public final ForgeConfigSpec.IntValue gap;
        public final ForgeConfigSpec.IntValue rowGap;
        public final ForgeConfigSpec.BooleanValue labels;
        public final ForgeConfigSpec.EnumValue<VerticalAlign> verticalAlign;
        public final ForgeConfigSpec.IntValue autoAlignBand;
        public final ForgeConfigSpec.IntValue maxItems;
        public final ForgeConfigSpec.IntValue tickBudgetMs;

        private MassPlace(ForgeConfigSpec.Builder b) {
            b.comment(
                    "/ceplace structures <filter> and /ceplace templates <glob>: place every match in rows (one row per",
                    "type tag, group, mod or template folder), starting at your position and going +X, rows stacked +Z.",
                    "Meant for a flat world with Generate Structures off. /ceplace preview ... lists without placing."
            ).push("mass_place");
            gap = b.comment("Blocks between two structures in a row.").defineInRange("gap", 12, 0, 512);
            rowGap = b.comment("Blocks between rows (the aisle the labels stand in).").defineInRange("rowGap", 16, 4, 512);
            labels = b.comment("Put a sign with the id in front of each structure and at the start of each row.")
                    .define("labels", true);
            verticalAlign = b.comment(
                    "KEEP: each structure at the height it generated at here. GROUND: its bottom layer at ground level.",
                    "AUTO: GROUND only for structures that generated more than autoAlignBand blocks above or below the",
                    "ground (underground and sky structures); surface ones keep their height."
            ).defineEnum("verticalAlign", VerticalAlign.AUTO);
            autoAlignBand = b.comment("AUTO's tolerance, in blocks.").defineInRange("autoAlignBand", 8, 0, 256);
            maxItems = b.comment("Refuse a run with more matches than this (preview first, or narrow the filter).")
                    .defineInRange("maxItems", 1000, 1, 100000);
            tickBudgetMs = b.comment(
                    "Milliseconds of work per server tick. Each structure is laid out, then its chunks are generated and",
                    "built one at a time until the budget is used, so a big village takes several ticks instead of one",
                    "long one (a dedicated server kills a tick over 60 s). Higher = faster runs, choppier server."
            ).defineInRange("tickBudgetMs", 40, 5, 1000);
            b.pop(); // mass_place
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
     * Dials for {@code trader.WanderingTraderTweaks} (Wandering Trader wait and offer counts, through
     * {@code mixin.WanderingTraderSpawnerMixin} / {@code mixin.WanderingTraderTradesMixin}) and
     * {@code trader.WinemakerSpawner} (the Wandering Winemaker's own spawner). What the pools hold is
     * set in KubeJS ({@code kubejs/server_scripts/economy/wandering_trades.js}).
     */
    public static final class WanderingTrader {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.IntValue traderIntervalTicks;
        public final ForgeConfigSpec.IntValue commonTrades;
        public final ForgeConfigSpec.IntValue rareTrades;
        public final ForgeConfigSpec.BooleanValue winemakerEnabled;
        public final ForgeConfigSpec.IntValue winemakerIntervalTicks;

        private WanderingTrader(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Wandering Trader and Wandering Winemaker spawning, and the trader's offer counts.",
                    "Each spawner rolls once per interval, counted in server ticks (20 = 1 second) while the",
                    "overworld runs with doMobSpawning on. Day length (Better Days), sleeping and /time do not",
                    "change it. A roll is vanilla's: 25/50/75% chance (rising until a spawn), then 1 in 10, then",
                    "a free spot near a random player; on average about 14 rolls per spawn.",
                    "To stop both use /gamerule doTraderSpawning false."
            ).push("wandering_trader");

            enabled = b.comment("Off = vanilla Wandering Trader (24000-tick interval, 5 + 1 offers) and no Winemaker spawner.")
                    .define("enabled", true);

            traderIntervalTicks = b.comment(
                    "Ticks between Wandering Trader spawn rolls. Vanilla: 24000 (20 minutes). 96000 = 80 minutes.",
                    "A change takes effect after the wait already counting down."
            ).defineInRange("traderIntervalTicks", 96000, 1200, 100_000_000);

            commonTrades = b.comment(
                    "Offers drawn from the common pool. Vanilla: 5. Each offer is a different listing; a pool",
                    "smaller than this gives every listing once."
            ).defineInRange("commonTrades", 25, 0, 200);

            rareTrades = b.comment("Offers drawn from the rare pool, all different items. Vanilla: 1.")
                    .defineInRange("rareTrades", 5, 1, 50);

            winemakerEnabled = b.comment(
                    "Spawn Vinery's Wandering Winemaker from its own spawner, independent of the Wandering Trader.",
                    "Vinery's own swap (config/vinery/config.json5 wine_trader_chance) must be 0, or it still",
                    "turns that share of Wandering Trader spawns into Winemakers."
            ).define("winemakerEnabled", true);

            winemakerIntervalTicks = b.comment("Ticks between Wandering Winemaker spawn rolls.")
                    .defineInRange("winemakerIntervalTicks", 96000, 1200, 100_000_000);

            b.pop(); // wandering_trader
        }
    }

    /** Dev-instance knobs: the Python generator runner ({@code devtools/GeneratorRunner}, /cegen). */
    public static final class Debug {
        public final ForgeConfigSpec.BooleanValue runGeneratorsAtStartup;
        public final ForgeConfigSpec.ConfigValue<String> generatorTarget;
        public final ForgeConfigSpec.ConfigValue<String> python;
        public final ForgeConfigSpec.IntValue timeoutSeconds;
        public final ForgeConfigSpec.IntValue startupWaitSeconds;
        public final ForgeConfigSpec.BooleanValue stateSyncEnabled;
        public final ForgeConfigSpec.IntValue stateSyncMaxDetailLines;

        private Debug(ForgeConfigSpec.Builder b) {
            b.comment("Developer tools. Only meaningful in the dev instance (the one with tools/ and mods/ source).").push("debug");
            b.comment(
                    "The pack's generator scripts (tools/*.py: structure/biome/loot configs, effect descriptions,",
                    "pack manifest, reports) listed in tools/generators.json. /cegen run [target] runs them in game;",
                    "these knobs also let them run by themselves every time the game starts."
            ).push("generators");

            runGeneratorsAtStartup = b.comment(
                    "Debug mode. When true, the generators run in the background as the game starts (from common",
                    "setup), and loading waits for them before finishing, so the first world load already reads",
                    "fresh files. Results go to latest.log, logs/cegen/<id>.log and /cegen status. Off by default."
            ).define("runAtStartup", false);

            generatorTarget = b.comment(
                    "What the startup run and a bare /cegen run execute: 'all', a group or one generator id",
                    "from tools/generators.json."
            ).define("target", "core");

            python = b.comment("Python interpreter to run the scripts with (a name on PATH or a full path).")
                    .define("python", "python3");

            timeoutSeconds = b.comment("A script still running after this long is killed and reported as TIMEOUT.")
                    .defineInRange("timeoutSeconds", 300, 10, 3600);

            startupWaitSeconds = b.comment(
                    "How long the end of loading waits for the startup run. The core generators take about 10 s and",
                    "run alongside the rest of loading, so this rarely waits at all. 0 = never wait (files may then",
                    "change after the first world has loaded)."
            ).defineInRange("startupWaitSeconds", 120, 0, 3600);

            b.pop(); // generators

            b.comment(
                    "Block-state sync diagnostics, for ghost blocks that only appear on a dedicated server (the client",
                    "throws 'Error executing task on Client' because a block-update packet names a block state it does",
                    "not have, and drops the rest of that packet). Turn it on on the CLIENT, and on the SERVER to get its",
                    "half of the comparison."
            ).push("block_state_sync");

            stateSyncEnabled = b.comment(
                    "Debug mode. Client: logs every block-update packet containing a state id the client cannot resolve",
                    "(the raw id, the block position, the client's state count) and writes",
                    "logs/ce_block_state_ranges_client.txt. Dedicated server: writes logs/ce_block_state_ranges_server.txt",
                    "when it starts. tools/diff_block_state_ranges.py compares the two files and names the block."
            ).define("enabled", false);

            stateSyncMaxDetailLines = b.comment(
                    "Per-packet detail lines to log before falling back to one summary line every 500 bad packets.",
                    "Each distinct bad id is always logged the first time it is seen."
            ).defineInRange("maxDetailLines", 200, 0, 100000);

            b.pop(); // block_state_sync
            b.pop(); // debug
        }
    }
}
