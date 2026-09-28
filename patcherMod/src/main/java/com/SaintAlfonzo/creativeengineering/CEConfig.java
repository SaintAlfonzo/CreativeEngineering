package com.SaintAlfonzo.creativeengineering;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.SaintAlfonzo.creativeengineering.shield.ElementalKind;
import com.SaintAlfonzo.creativeengineering.shield.ShieldGroup;
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
    public static final Shields SHIELDS;
    public static final DamageTagConfig DAMAGE_TAGS;
    public static final BonemealCharm BONEMEAL_CHARM;
    public static final Surface SURFACE;
    public static final Worldgen WORLDGEN;
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
        SHIELDS = new Shields(builder);
        DAMAGE_TAGS = new DamageTagConfig(builder);
        BONEMEAL_CHARM = new BonemealCharm(builder);
        SURFACE = new Surface(builder);
        WORLDGEN = new Worldgen(builder);
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
        public final Map<ShieldGroup, ForgeConfigSpec.EnumValue<ShieldGroup.Facing>> facing =
                new EnumMap<>(ShieldGroup.class);
        public final Map<ShieldTier, TierCfg> tiers = new EnumMap<>(ShieldTier.class);

        private Shields(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Tier shields (copper / gold / diamond / netherite / echo / felsic / dragon). Vanilla's",
                    "shield and Immersive Engineering's shield are NOT affected by anything in here.",
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
                    "Ticks a shield is unusable after an attacker disables it (a vanilla axe hit by default;",
                    "Immersive Engineering's shock upgrade and Alex's Mobs' void worm shot trigger the same",
                    "mechanic directly). This is a Player-level vanilla mechanic, NOT tier-specific - unlike",
                    "everything else in [shields], it also applies to the plain vanilla shield and Immersive",
                    "Engineering's shield. 20 (1 second) is vanilla's hardcoded value; lower it to let",
                    "blocking recover faster against fast/continuous attackers without removing the",
                    "punish-for-turtling mechanic entirely. 0 disables the cooldown outright."
            ).defineInRange("disableCooldownTicks", 20, 0, 12000);

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
                        "Damage amount, AFTER this tier's own reduction, below which a blocked melee hit no",
                        "longer knocks its attacker back (vanilla always applies that recoil; this only ever",
                        "removes it, never adds knockback to a hit that wouldn't already have had it). 0 = never",
                        "suppress (always knock back, vanilla behaviour)."
                ).defineInRange("knockbackThreshold", t.defaultKnockbackThreshold(), 0, 1000);

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
        public final MassPlace massPlace;

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
            massPlace = new MassPlace(b);
            b.pop(); // worldgen
        }
    }

    /** One switch per worldgen system, so each can be A/B tested against the mods' own defaults. */
    public static final class WorldgenSystems {
        public final ForgeConfigSpec.BooleanValue structureGroups;
        public final ForgeConfigSpec.BooleanValue structureFrequencies;
        public final ForgeConfigSpec.BooleanValue poolTuning;
        public final ForgeConfigSpec.BooleanValue lootContainerSwaps;
        public final ForgeConfigSpec.BooleanValue biomeLayout;
        public final ForgeConfigSpec.BooleanValue biomeMerges;
        public final ForgeConfigSpec.BooleanValue deadPlacementFilter;
        public final ForgeConfigSpec.BooleanValue groundMatch;

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
            poolTuning = b.comment("pool_config.json / pool_tags.json village-house weights (PoolTuner). Off: every pool at its first-seen weights. Rejoin.")
                    .define("poolTuning", true);
            lootContainerSwaps = b.comment("loot_container_swaps.json: modded loot containers placed by structures become vanilla ones for Lootr.")
                    .define("lootContainerSwaps", true);
            biomeLayout = b.comment("biome_config.json multipliers / replacements (BiomeLayoutTuner). Only affects worlds created afterwards.")
                    .define("biomeLayout", true);
            biomeMerges = b.comment("biome_merges.json: extra biomes in structure biome tags (kubejs worldgen/biome_merges.js). /reload or rejoin.")
                    .define("biomeMerges", true);
            deadPlacementFilter = b.comment("Explorer maps and /locate skip structure sets at frequency 0 (without it they can hang the server).")
                    .define("deadPlacementFilter", true);
            groundMatch = b.comment("Swap a structure's grass/dirt base for the local ground where grass isn't native ([worldgen.ground_match]).")
                    .define("groundMatch", true);
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

    /** {@code /ceplace}: whole structures or raw templates laid out in rows, for inspecting them in a flat world. */
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

    /** Dev-instance knobs: the Python generator runner ({@code devtools/GeneratorRunner}, /cegen). */
    public static final class Debug {
        public final ForgeConfigSpec.BooleanValue runGeneratorsAtStartup;
        public final ForgeConfigSpec.ConfigValue<String> generatorTarget;
        public final ForgeConfigSpec.ConfigValue<String> python;
        public final ForgeConfigSpec.IntValue timeoutSeconds;
        public final ForgeConfigSpec.IntValue startupWaitSeconds;

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
            b.pop(); // debug
        }
    }
}
