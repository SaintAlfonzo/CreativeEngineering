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
    public static final Stepstool STEPSTOOL;
    public static final Crafting CRAFTING;
    public static final Botania BOTANIA;
    public static final ArmorEnchant ARMOR_ENCHANT;
    public static final Shrapnel SHRAPNEL;
    public static final ArmorOverflow ARMOR_OVERFLOW;
    public static final Jei JEI;
    public static final GuiTitle GUI_TITLE;
    public static final Toasts TOASTS;
    public static final Changelog CHANGELOG;
    public static final Shields SHIELDS;
    public static final DamageTagConfig DAMAGE_TAGS;
    public static final BonemealCharm BONEMEAL_CHARM;
    public static final LeafDecay LEAF_DECAY;
    public static final Raids RAIDS;
    public static final BottledAir BOTTLED_AIR;
    public static final Coaster COASTER;
    public static final Wthit WTHIT;
    public static final Perf PERF;
    public static final Surface SURFACE;
    public static final Worldgen WORLDGEN;
    public static final VeinUndo VEIN_UNDO;
    public static final BackpackSkins BACKPACK_SKINS;
    public static final WanderingTrader WANDERING_TRADER;
    public static final NamedMobs NAMED_MOBS;
    public static final Advancements ADVANCEMENTS;
    public static final ModNames MOD_NAMES;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        FORGIVING_VOID = new ForgivingVoid(builder);
        DRAGON_GEAR = new DragonGear(builder);
        AIR_BRAKE = new AirBrake(builder);
        STEPSTOOL = new Stepstool(builder);
        CRAFTING = new Crafting(builder);
        BOTANIA = new Botania(builder);
        ARMOR_ENCHANT = new ArmorEnchant(builder);
        SHRAPNEL = new Shrapnel(builder);
        ARMOR_OVERFLOW = new ArmorOverflow(builder);
        JEI = new Jei(builder);
        GUI_TITLE = new GuiTitle(builder);
        TOASTS = new Toasts(builder);
        CHANGELOG = new Changelog(builder);
        SHIELDS = new Shields(builder);
        DAMAGE_TAGS = new DamageTagConfig(builder);
        BONEMEAL_CHARM = new BonemealCharm(builder);
        LEAF_DECAY = new LeafDecay(builder);
        // [leaf_piles], [moss], [vine_shearing], [campfire], [monster_box], [trash_slot]: creativecompat-common.toml
        // (Creative Compat, 2026-11-01)
        RAIDS = new Raids(builder);
        BOTTLED_AIR = new BottledAir(builder);
        COASTER = new Coaster(builder);
        WTHIT = new Wthit(builder);
        PERF = new Perf(builder);
        // [seasons], [crop_seasons], [time]: creativecalendar-common.toml (Creative Calendar, 2026-11-01)
        SURFACE = new Surface(builder);
        WORLDGEN = new Worldgen(builder);
        VEIN_UNDO = new VeinUndo(builder);
        BACKPACK_SKINS = new BackpackSkins(builder);
        WANDERING_TRADER = new WanderingTrader(builder);
        NAMED_MOBS = new NamedMobs(builder);
        ADVANCEMENTS = new Advancements(builder);
        MOD_NAMES = new ModNames(builder);
        COMMON_SPEC = builder.build();
    }

    /** Backpack skins (backpackskin/ package, feature_writeup/backpack_skins.md). */
    public static final class BackpackSkins {
        public final ForgeConfigSpec.BooleanValue allowEquip;

        private BackpackSkins(ForgeConfigSpec.Builder b) {
            b.comment("Sophisticated Backpacks skins: a block smithed onto a backpack changes how it looks.")
                    .push("backpackSkins");
            allowEquip = b.comment(
                    "Off: a skinned backpack cannot go in the chest slot or a Curios slot (so it is never worn).",
                    "On: it can, and the skin block is drawn on the back."
            ).define("allowEquip", false);
            b.pop();
        }
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
        public final ForgeConfigSpec.DoubleValue armorCap;

        private DragonGear(ForgeConfigSpec.Builder b) {
            b.push("dragon_gear");

            armorCap = b.comment("Most armor points an entity can have (mixin.ArmorCapMixin). Vanilla is 30; a full dragon set is 36.",
                            "Damage reduction still tops out at 20 effective armor (80 %); armor past 20 holds that",
                            "cap against bigger hits.")
                    .defineInRange("armorCap", 50.0, 30.0, 1024.0);

            b.push("autorepair");

            autorepairEnabled = b.comment("Passive slow self-repair for dragon-tier gear while worn/held.")
                    .define("enabled", true);
            autorepairIntervalTicks = b.comment("Ticks between repair pulses (20 ticks = 1 second).")
                    .defineInRange("intervalTicks", 200, 1, Integer.MAX_VALUE);
            autorepairAmountPerInterval = b.comment("Durability restored per pulse.")
                    .defineInRange("amountPerInterval", 50, 0, Integer.MAX_VALUE);

            b.pop().pop();
        }
    }

    /** Dials for the stepstools worn in the Curios feet slot ({@code stepstool.Stepstool}; feature_writeup/stepstool.md). */
    public static final class Stepstool {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.BooleanValue sprintJump;
        public final ForgeConfigSpec.IntValue holdTicks;
        public final ForgeConfigSpec.DoubleValue maxDrift;
        public final ForgeConfigSpec.BooleanValue stepOffWhenHurt;

        private Stepstool(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Stepstools: a stool in the Curios feet slot. Standing still and holding the step-up keys stands the",
                    "player on it, a block higher; moving, crouching or jumping steps off."
            ).push("stepstool");

            enabled = b.define("enabled", true);
            sprintJump = b.comment("Client: holding Sprint + Jump steps up. The \"Step Up (Stepstool)\" key in Controls works too",
                    "(unbound by default); set this false to use only that key. While false, Sprint + Jump jumps as usual.")
                    .define("sprintJump", true);
            holdTicks = b.comment("Client: ticks the step-up keys must be held before stepping up (20 = 1 second).")
                    .defineInRange("holdTicks", 5, 0, 100);
            maxDrift = b.comment("Blocks the player may drift sideways from the stool's centre (knockback, pistons) before stepping off.")
                    .defineInRange("maxDrift", 0.3, 0.05, 0.5);
            stepOffWhenHurt = b.comment("Taking damage knocks the player off the stool.")
                    .define("stepOffWhenHurt", true);

            b.pop(); // stepstool
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
                    "Read each time the item is used, so a config reload applies it."
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
        public final FelsicPiece FELSIC_SHIELD;

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
            FELSIC_SHIELD = new FelsicPiece(b, "felsic_shield", magicResistance, 250, 1, 8);
            b.pop();
        }

        /**
         * Tuning for one felsic_gear piece's sequenced-assembly recipe
         * (kubejs/server_scripts/equipment/end_gear.js, {@code R.cfg(..., 'CRAFTING.FELSIC_<PIECE>')}).
         * One instance per piece rather than one class per piece - same 5 fields, only the toml
         * section name and defaults differ.
         */
        public static final class FelsicPiece {
            public final ForgeConfigSpec.ConfigValue<String> potion;
            public final ForgeConfigSpec.IntValue mb;
            public final ForgeConfigSpec.IntValue reachMb;
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
                mb = b.comment("mB of potion fluid (the potion above) used in the last filling step.")
                        .defineInRange("potionMb", defaultMb, 1, 10000);
                // Default = the piece's potionMb, the amount both fills used before the split (gear_tuning.md F2).
                reachMb = b.comment("mB of Long Reach potion fluid used in the first filling step.")
                        .defineInRange("reachPotionMb", defaultMb, 1, 10000);
                loops = b.comment("Number of times the sequenced assembly repeats.")
                        .defineInRange("loops", defaultLoops, 1, 64);
                blows = b.comment("Hammer blows required by the hammering step.")
                        .defineInRange("hammerBlows", defaultBlows, 0, 64);

                b.pop();
            }
        }
    }

    /** Botania tweaks ({@code compat/TrinketCaseRules} and the {@code TrinketCase*Mixin}s). */
    public static final class Botania {
        public final ForgeConfigSpec.BooleanValue trinketCaseAnyItem;

        private Botania(ForgeConfigSpec.Builder b) {
            b.comment("Botania tweaks.").push("botania");

            trinketCaseAnyItem = b.comment(
                    "Let the Trinket Case hold any item, not just trinkets, rods and mana items.",
                    "Other trinket cases and items that can't go inside container items (shulker",
                    "boxes) are still refused. Only real trinkets equip from the case. Restart to apply."
            ).define("trinketCaseAnyItem", true);

            b.pop(); // botania
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
     * consumed by Creative Compat's {@code itemphysic.ItemBurnTransform} EXPLODE rule (through {@code util.BurnBursts})
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
     * Leaf life range past vanilla's 7 ({@code block.LeafDecayRange}, {@code mixin/LeavesDecayRangeMixin});
     * feature_writeup/mob_block_tweaks.md 2.5. Read on every decay check.
     */
    public static final class LeafDecay {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.BooleanValue onlyWhenDisturbed;
        public final ForgeConfigSpec.IntValue maxDistance;
        public final ForgeConfigSpec.IntValue maxSearchBlocks;
        public final ForgeConfigSpec.IntValue decayMinTicks;
        public final ForgeConfigSpec.IntValue decayMaxTicks;
        public final ForgeConfigSpec.BooleanValue leafPiles;

        private LeafDecay(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Natural (not player-placed) leaves decay when no log is close enough. Vanilla allows 6 blocks of",
                    "leaf path to a log and decays on slow random ticks; big generated trees have outer leaves past",
                    "that, which fell off on their own. Here a leaf decays shortly after a block next to it changes",
                    "(Fast Leaf Decay, ported in), and only if a search outward through the other far-out leaves",
                    "finds no log within maxDistance. Separated Leaves' rules (which leaves count which logs) are",
                    "kept. Server side."
            ).push("leaf_decay");

            enabled = b.comment("Master on/off switch. Off = vanilla decay (range and slow random-tick timing).")
                    .define("enabled", true);

            onlyWhenDisturbed = b.comment(
                    "true: random ticks never decay a natural leaf. A leaf only decays after a block next to it",
                    "changes: when its log is cut it rechecks 4-11 ticks later, and a far-out leaf rechecks when a",
                    "neighbour changes (so a cut tree's canopy falls away in a cascade). Trees from worldgen and",
                    "saplings are placed without neighbour updates, so an untouched tree keeps every leaf, even ones",
                    "past maxDistance or touching only diagonally. Cheaper than false: the range search runs only on",
                    "a change. false: vanilla timing (random ticks), with the maxDistance range."
            ).define("onlyWhenDisturbed", true);

            maxDistance = b.comment(
                    "Longest leaf path a natural leaf may be from a log and still live once it is checked, counted in",
                    "steps through leaves (a leaf touching the log is 1). Vanilla is 6; 6 here = vanilla range. Keeps a",
                    "big tree whole when something next to it changes (snow, vines, a placed block). Cost: a",
                    "chopped tree whose canopy touches a standing tree of the same kind keeps leaves out to this",
                    "range from the standing tree."
            ).defineInRange("maxDistance", 12, 6, 32);

            maxSearchBlocks = b.comment(
                    "Most leaf blocks one search may look at before giving up (the leaf then decays as in",
                    "vanilla). Bounds the cost in very large canopies; at maxDistance 12 a search rarely tops a",
                    "few hundred."
            ).defineInRange("maxSearchBlocks", 4096, 64, 65536);

            decayMinTicks = b.comment(
                    "Fast decay delay: a leaf whose log was cut, or that sits next to a block that just became air,",
                    "is checked a random number of ticks later between decayMinTicks and decayMaxTicks (Fast Leaf",
                    "Decay's MinimumDecayTime / MaximumDecayTime, 4 and 11). Spreads a felled canopy over a moment."
            ).defineInRange("decayMinTicks", 4, 1, 1200);
            decayMaxTicks = b.comment("Upper end of the fast decay delay (raised to decayMinTicks if lower).")
                    .defineInRange("decayMaxTicks", 11, 1, 1200);

            leafPiles = b.comment(
                    "A leaf that decays may leave an Immersive Weathering leaf pile in its place (which then falls),",
                    "as it did with Fast Leaf Decay installed. Chance and blacklist are IW's own [leaf_piles]",
                    "spawn_entity_from_decay / leaf_piles_blacklist. false: decayed leaves only drop their items."
            ).define("leafPiles", true);

            b.pop(); // leaf_decay
        }
    }

    /** Glowing Illagers port ({@code mixin/RaidGlowingMixin}). */
    public static final class Raids {
        public final ForgeConfigSpec.BooleanValue glowingRaiders;
        public final ForgeConfigSpec.IntValue glowingAmplifier;

        private Raids(ForgeConfigSpec.Builder b) {
            b.comment("Raids (server side).").push("raids");

            glowingRaiders = b.comment("Every raider a raid spawns glows for good (Glowing Illagers port).")
                    .define("glowingRaiders", true);
            glowingAmplifier = b.comment("Glowing amplifier (the mod used 1 = Glowing II; the level changes nothing for Glowing).")
                    .defineInRange("glowingAmplifier", 1, 0, 255);

            b.pop(); // raids
        }
    }

    /** Bottled Air port ({@code item.BottledAir}). */
    public static final class BottledAir {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.IntValue airPerBottle;
        public final ForgeConfigSpec.BooleanValue noDrinkingUnderwater;
        public final ForgeConfigSpec.BooleanValue fireEvaporates;
        public final ForgeConfigSpec.DoubleValue bottleBreakChance;

        private BottledAir(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Bottled Air port: right-click a glass bottle in water to breathe from it. The fire sources that",
                    "evaporate the filled bottle in the other hand are #creativeengineering:bottled_air/fire_sources."
            ).push("bottled_air");

            enabled = b.define("enabled", true);
            airPerBottle = b.comment("Air a bottle gives (vanilla's full supply is 300).").defineInRange("airPerBottle", 150, 0, 1000);
            noDrinkingUnderwater = b.comment("A water bottle can't be drunk while under water (else it is endless air).")
                    .define("noDrinkingUnderwater", true);
            fireEvaporates = b.comment("A fire source in the other hand evaporates the water bottle the fill made, giving the glass bottle back.")
                    .define("fireEvaporates", true);
            bottleBreakChance = b.comment("Chance the glass bottle breaks when the water is evaporated.")
                    .defineInRange("bottleBreakChance", 0.5, 0.0, 1.0);

            b.pop(); // bottled_air
        }
    }

    /**
     * Throttles for third-party mod code that the server profile (feature_writeup/perf.md section 7) found
     * hot. Each one is a mixin in {@code mixin/} that skips or spaces out work the mod does every tick;
     * the comments say what is traded away. Read live, no rejoin needed.
     */
    public static final class Perf {
        public final ForgeConfigSpec.BooleanValue gravityLimitDimensions;
        public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> gravityDimensions;
        public final ForgeConfigSpec.IntValue mountEffectIntervalTicks;
        public final ForgeConfigSpec.BooleanValue mobCapLevels;
        public final ForgeConfigSpec.IntValue mobCapForceLevel;
        public final ForgeConfigSpec.IntValue mobCapLevel1Chunks;
        public final ForgeConfigSpec.IntValue mobCapLevel2Chunks;
        public final ForgeConfigSpec.IntValue mobCapHysteresisChunks;
        public final ForgeConfigSpec.IntValue mobCapLevel1Monster;
        public final ForgeConfigSpec.IntValue mobCapLevel2Monster;
        public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> mobCapScaledCategories;
        public final ForgeConfigSpec.IntValue spawnChunksLevel1;
        public final ForgeConfigSpec.IntValue spawnChunksLevel2;
        public final ForgeConfigSpec.IntValue spawnBandLevel1;
        public final ForgeConfigSpec.IntValue spawnBandLevel2;
        public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> spawnBandCategories;

        private Perf(ForgeConfigSpec.Builder b) {
            b.comment("Performance throttles for other mods' per-tick work. See feature_writeup/perf.md section 7.")
                    .push("perf");

            // troughMissCooldownTicks: creativedelight-common.toml [perf] (Creative Delight, 2026-11-01)

            // seasonsMeltIntervalTicks: creativecalendar-common.toml [perf] (Creative Calendar, 2026-11-01)

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

            mobCapLevels = b.comment(
                    "Mob cap levels (perf.md section 17). Vanilla's cap grows with the spawnable chunks (the chunks",
                    "within 8 chunks of any player, 289 for one player alone), so spread-out players each add a",
                    "whole monster cap. On: above the chunk counts below, the per-chunk caps drop to level 1 or 2.",
                    "Lowering a cap removes no mobs; it only stops new spawns until the count falls below it.",
                    "Off: vanilla caps."
            ).define("mobCapLevels", true);
            mobCapForceLevel = b.comment(
                    "Force a mob cap level in every dimension (0, 1 or 2) for testing. -1 = pick it from the",
                    "spawnable chunk count."
            ).defineInRange("mobCapForceLevel", -1, -1, 2);
            mobCapLevel1Chunks = b.comment(
                    "Level 1 starts above this many spawnable chunks in a dimension (430 = about 1.5 player areas)."
            ).defineInRange("mobCapLevel1Chunks", 430, 0, 100000);
            mobCapLevel2Chunks = b.comment(
                    "Level 2 starts above this many spawnable chunks (870 = about 3 player areas)."
            ).defineInRange("mobCapLevel2Chunks", 870, 0, 100000);
            mobCapHysteresisChunks = b.comment(
                    "A level is only left when the chunk count falls this far below its threshold, so it does not",
                    "flip back and forth while a player walks along the edge."
            ).defineInRange("mobCapHysteresisChunks", 30, 0, 10000);
            mobCapLevel1Monster = b.comment(
                    "Monster cap at level 1 (vanilla 70). The other scaled categories get the same fraction."
            ).defineInRange("mobCapLevel1Monster", 50, 1, 70);
            mobCapLevel2Monster = b.comment(
                    "Monster cap at level 2 (vanilla 70)."
            ).defineInRange("mobCapLevel2Monster", 25, 1, 70);
            mobCapScaledCategories = b.comment(
                    "Spawn categories whose cap is scaled at levels 1 and 2. creature is left out on purpose:",
                    "animals are persistent and their herds are placed at chunk generation."
            ).defineList("mobCapScaledCategories", java.util.List.of("monster", "ambient", "water_creature",
                    "water_ambient", "underground_water_creature", "axolotls"), o -> o instanceof String);
            spawnChunksLevel1 = b.comment(
                    "At level 1, the scaled categories above only spawn in chunks within this many chunks of a",
                    "player (vanilla 8), so mobs spawn closer in and fewer chunks are rolled. 0 = vanilla."
            ).defineInRange("spawnChunksLevel1", 6, 0, 8);
            spawnChunksLevel2 = b.comment(
                    "The same spawn radius at level 2. 0 = vanilla."
            ).defineInRange("spawnChunksLevel2", 5, 0, 8);
            spawnBandLevel1 = b.comment(
                    "At level 1, natural spawns of the categories below must be within this many blocks above or",
                    "below the nearest player, so caves far below a surface player do not fill the cap.",
                    "0 = no limit."
            ).defineInRange("spawnBandLevel1", 64, 0, 512);
            spawnBandLevel2 = b.comment(
                    "The same vertical band at level 2. 0 = no limit."
            ).defineInRange("spawnBandLevel2", 32, 0, 512);
            spawnBandCategories = b.comment(
                    "Spawn categories the vertical band applies to."
            ).defineList("spawnBandCategories", java.util.List.of("monster"), o -> o instanceof String);

            b.pop(); // perf
        }
    }

    // [kitchen] (counter-top offsets) moved to Creative Delight's blockheadsAdditions config (2026-10-31).

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
                    "/cedumpicons + tools/wthit_icon_audit.py list every block the icon applies to."
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
                    "sea pickles. Rules in assets/creativeengineering/state_lines.json (override from kubejs/assets)."
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
                    "(yellow) when something else drops (block tags creativeengineering:wthit/needs_silk_touch and",
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

    // Season types ([seasons]), crop seasons ([crop_seasons]) and the [time] master: Creative Calendar's
    // CalendarConfig (creativecalendar-common.toml), moved with their keys and comments on 2026-11-01.

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
                    "balloons. Takes effect for the next toast."
            ).define("tutorial", false);

            advancement = b.comment(
                    "Show advancement toasts (vanilla's \"Advancement Made!\" and Patchouli's book-unlocked toast).",
                    "The pack loads no advancements ([advancements] enabled), so this only matters if that is on."
            ).define("advancement", false);

            b.pop(); // toasts
        }
    }

    /** Player changelog, "What's New" ({@code changelog/} package, feature_writeup/changelog.md). */
    public static final class Changelog {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.BooleanValue titleButton;
        public final ForgeConfigSpec.BooleanValue pauseButton;
        public final ForgeConfigSpec.BooleanValue updatePopup;

        private Changelog(ForgeConfigSpec.Builder b) {
            b.comment("Player changelog, \"What's New\" (client side). The notes are the pack's",
                    "assets/creativeengineering/changelog/<version>.md files. Takes effect the next time a menu opens."
            ).push("changelog");

            enabled = b.comment("Master switch: off removes the book buttons and the update popup.")
                    .define("enabled", true);
            titleButton = b.comment("Book button on the title screen, right of Singleplayer.")
                    .define("titleButton", true);
            pauseButton = b.comment("Book button on the pause menu, right of Back to Game.")
                    .define("pauseButton", true);
            updatePopup = b.comment(
                    "Speech bubble beside the book button while the newest version's notes are unread. Opening the",
                    "changelog once acknowledges it (config/creativeengineering/changelog_seen.txt)."
            ).define("updatePopup", true);

            b.pop(); // changelog
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

    /** Which structure starts the earth-fill check logs ({@code worldgen/EarthFill}). */
    public enum EarthFillLog { OFF, CANCELLED, ALL }

    /** [worldgen.ocean_geodes] floorPatch: what happens to an ocean geode that reaches the sea floor. */
    public enum GeodeFloor { OFF, CUT, SWAP }

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
        public final OceanGeodes oceanGeodes;
        public final PlacementPriorityCfg placementPriority;
        public final LocateCfg locate;
        public final DistantCfg distant;
        public final FirstTickWaterCfg firstTickWater;

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
            oceanGeodes = new OceanGeodes(b);
            placementPriority = new PlacementPriorityCfg(b);
            locate = new LocateCfg(b);
            distant = new DistantCfg(b);
            firstTickWater = new FirstTickWaterCfg(b);
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
        public final ForgeConfigSpec.BooleanValue mushroomSpawns;
        public final ForgeConfigSpec.BooleanValue wildFlowers;
        public final ForgeConfigSpec.BooleanValue mobSpawns;
        public final ForgeConfigSpec.BooleanValue beeNests;
        public final ForgeConfigSpec.BooleanValue oceanGeodes;
        public final ForgeConfigSpec.BooleanValue placementPriority;
        public final ForgeConfigSpec.BooleanValue templateBlockSwaps;
        public final ForgeConfigSpec.BooleanValue spawnBoxes;
        public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> removeSpawners;
        public final ForgeConfigSpec.BooleanValue permafrostMerge;
        public final ForgeConfigSpec.BooleanValue mulberrySapling;
        public final ForgeConfigSpec.BooleanValue firstTickWater;
        public final ForgeConfigSpec.BooleanValue holdGeneratedFallingBlocks;
        public final ForgeConfigSpec.BooleanValue dimensionFilter;
        public final ForgeConfigSpec.BooleanValue sharedTerrainCache;

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
            mushroomSpawns = b.comment("mushroom_spawns.json: wild mushrooms, colonies and huge Fungi Delight mushrooms at the surface and in caves",
                    "(kubejs worldgen/mushroom_spawns.js). Off: mushrooms as their mods ship them. Rejoin to apply; only new chunks change.")
                    .define("mushroomSpawns", true);
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
            spawnBoxes = b.comment("spawn_boxes.json (patcherMod StructureSpawnBoxes): pillager and witch structures respawn their own mob mix",
                    "inside a spawn box, members of a meta structure keep their own boxes (or take the meta's), and box evokers drop no",
                    "totem. Off: only the mods' own spawn_overrides (inside metas each part keeps its own either way). Applies at once, old chunks too.")
                    .define("spawnBoxes", true);
            removeSpawners = b.comment("Mob spawners of these entities are removed from structure templates when the template loads (patcherMod",
                    "TemplateSpawnerFilter); the spawn boxes take their place. A template that loses one also loses its spawners with",
                    "no mob. Empty list: every spawner stays. Rejoin; new chunks only.")
                    .defineList("removeSpawners", java.util.List.of("minecraft:pillager"), o -> o instanceof String);
            permafrostMerge = b.comment("Quark's frozen peaks cave style lays Immersive Weathering's permafrost on floors, ceilings and pillars",
                    "instead of Quark's own block (renamed Froststone; patcherMod QuarkPermafrostStyleMixin). Walls stay packed ice.",
                    "Off: Quark's block. New chunks only.")
                    .define("permafrostMerge", true);
            mulberrySapling = b.comment("Upgrade Aquatic's river sapling (Mulberry Sapling) grows the datapack's upgrade_aquatic:river_tree, the oak",
                    "trunk tree that worldgen places by rivers (patcherMod MulberrySapling). Off: UA's built-in tree with river logs,",
                    "which drop oak logs. Applies at once.")
                    .define("mulberrySapling", true);
            firstTickWater = b.comment("Generated water that first flows when its chunk starts ticking (aquifer edges, cave springs) breaks the",
                    "plants, carpets and torches in its way without dropping items ([worldgen.first_tick_water]; patcherMod FirstTickWater).",
                    "Off: vanilla, they drop as items. Applies at once.")
                    .define("firstTickWater", true);
            holdGeneratedFallingBlocks = b.comment("Sand, gravel and other falling blocks left floating by generation stay put when their chunk first",
                    "ticks, until a later block update next to them (patcherMod GeneratedFallingBlocks). Covers the chunk's",
                    "post-processing, generated water settling in the [worldgen.first_tick_water] window and ticks scheduled during",
                    "generation. Off: vanilla, they fall on the first tick (in the outer ring of loaded chunks the falling entity never",
                    "lands and stays saved). Applies at once.")
                    .define("holdGeneratedFallingBlocks", true);
            dimensionFilter = b.comment("A structure that cannot start in any biome of a dimension is not tried there (patcherMod DimensionFilter):",
                    "no generation point, terrain reads or jigsaw assembly for Nether and End structures in the Overworld.",
                    "Same structures either way, only faster. Off: every structure set is tried in every dimension, as vanilla 1.19.2.")
                    .define("dimensionFilter", true);
            sharedTerrainCache = b.comment("Earth and water fill share one bounded cache of predicted terrain columns across starts, placement",
                    "priority predictions, meta retries and the real start of a chunk (patcherMod EarthFill). Same results either way,",
                    "only faster. Off: each start reads its own columns.")
                    .define("sharedTerrainCache", true);
            b.pop(); // systems
        }
    }

    /** Testing knobs that are wrong for a real world. */
    public static final class WorldgenDebug {
        public final ForgeConfigSpec.DoubleValue structureDensity;
        public final ForgeConfigSpec.BooleanValue testOverrides;

        private WorldgenDebug(ForgeConfigSpec.Builder b) {
            b.comment("For testing only; leave at the defaults for play.").push("debug");
            structureDensity = b.comment(
                    "Multiplies how often EVERY structure spawns (natural generation, every set, grouped or not). 1 = off.",
                    "Below 1: each set's frequency is multiplied (exact, spacing and gaps unchanged). Above 1: grids shrink",
                    "by sqrt(x) and the frequency absorbs the rounding, so the rate is exact until a grid reaches its",
                    "minimum (spacing = separation + 1); separations shrink with the grid. structure_tag_report.md shows",
                    "the result. Rejoin to apply; only new chunks change."
            ).defineInRange("structureDensity", 1.0, 0.01, 100.0);
            testOverrides = b.comment(
                    "Scratch servers only. On: kubejs/generation_scripts/worldgen/structure_test_overrides.json loosens soft",
                    "structure rules so rare structures can be found and tested (per structure frequency, weight, meta share;",
                    "group spacing and separation). Biomes, fits, surface checks, peaceful radius, priority and earth/water",
                    "fill never change. The log and structure_tag_report.md say TEST OVERRIDES ACTIVE. Rejoin to apply;",
                    "only new chunks change."
            ).define("testOverrides", false);
            b.pop(); // debug
        }
    }

    /** {@code worldgen/PoolTuner} constants. */
    public static final class PoolTuning {
        public final ForgeConfigSpec.IntValue maxFractionScale;
        public final ForgeConfigSpec.BooleanValue canonicalOrder;

        private PoolTuning(ForgeConfigSpec.Builder b) {
            b.push("pools");
            maxFractionScale = b.comment(
                    "Fractional piece weights are made whole by scaling the pool's list by up to this factor. Jigsaw",
                    "tries every copy of a piece in a pool, so a bigger factor is more exact but slower to fail."
            ).defineInRange("maxFractionScale", 10, 1, 100);
            canonicalOrder = b.comment(
                    "Sort every pool's pieces by template id at world load. Mods that add pieces in code leave the pools",
                    "in an order that changes from one server start to the next, and jigsaw picks by position in that",
                    "list, so the same seed built different villages after each restart. Sorted, a seed gives the same",
                    "layouts every time (not vanilla's: new chunks only). false = keep the order the mods left."
            ).define("canonicalOrder", true);
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
        public final ForgeConfigSpec.DoubleValue maxWaterSurplus;
        public final ForgeConfigSpec.BooleanValue footprintAllPieces;
        public final ForgeConfigSpec.IntValue beardReach;
        public final ForgeConfigSpec.IntValue buryReach;
        public final ForgeConfigSpec.IntValue maxMoundHeight;
        public final ForgeConfigSpec.BooleanValue beardFootprint;
        public final ForgeConfigSpec.BooleanValue prune;
        public final ForgeConfigSpec.IntValue pruneMaxHanging;
        public final ForgeConfigSpec.IntValue pruneMaxMound;
        public final ForgeConfigSpec.IntValue pruneMinWetColumns;
        public final ForgeConfigSpec.BooleanValue pruneWetJigsaws;
        public final ForgeConfigSpec.IntValue footprintMinWater;
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
            maxWaterSurplus = b.comment(
                    "The footprint's water share, checked after the limits: the bounding box of every piece with earth, in the",
                    "layer the structure was set onto the surface with, and (footprintAllPieces) every other piece's box where",
                    "the surface lies within its height. Water there (filled by its earth or the beard, or left",
                    "standing) against dry ground there (replaced or left alone): the structure is cancelled when the water",
                    "outnumbers the ground by more than this share. 0.5 = water more than 50% over ground (60:40 passes, 61:39",
                    "doesn't), 0 = more water than ground, -1 = off. Catches camps on islands and ruins reaching out over a lake,",
                    "whose beard fills the shallow water so maxOverWater never sees it. A spot under more than baseLayers",
                    "blocks of terrain (underground structures, aquifers) counts as neither. Not for structures in",
                    "creativeengineering:water_fill/structures; an override with every limit -1 skips it too. Default 0",
                    "(2026-10-30, was 0.5): the strictest setting, any start standing more on water than on ground goes."
            ).defineInRange("maxWaterSurplus", 0.0, -1.0, 1000.0);
            footprintAllPieces = b.comment(
                    "The footprint water share also judges pieces with no earth in their base: plank, wool, sandstone or brick",
                    "floors, and pieces that are not jigsaw or template pieces (vanilla desert and jungle pyramids, igloos).",
                    "A column of such a piece counts when the predicted surface (its topmost block) lies within the piece's",
                    "height (from baseLayers below its bottom to its top): water when that surface is water, ground when it",
                    "is solid. Pieces buried under the surface or floating above it count as neither. Village streets that",
                    "follow the terrain never count. Off: only pieces with earth make the footprint (before 2026-10-30)."
            ).define("footprintAllPieces", true);
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
            beardFootprint = b.comment(
                    "Mound columns also under the built bottom of a bearded piece, not only under its earth: a stone or",
                    "plank floor over a cliff edge stands on a beard pillar the earth count never sees. Every column with a",
                    "solid block in a rigid piece's baseLayers (trimmed columns left out) whose ground lies more than",
                    "maxMoundHeight below it counts toward the mound limit; a start with no earth at all is judged as small.",
                    "Whole-start check only (pruning still judges earth). tools/earth_fill_sim.sh <seed> <n> footprint",
                    "measured 2026-10-30: 23% of vanilla outposts and 2 to 18% of villages would newly cross their mound",
                    "limit. Off until calibrated on a log = ALL scratch run."
            ).define("beardFootprint", false);
            prune = b.comment(
                    "For structures in the structure tag creativeengineering:earth_fill/prune (villages): before the whole start",
                    "is judged, each jigsaw piece but the first is judged alone, and one past pruneMaxHanging or pruneMaxMound",
                    "is dropped with every piece attached below it (a house over deep water with its villagers). So is a piece",
                    "whose own footprint is too wet (maxWaterSurplus) with at least pruneMinWetColumns water columns. The street",
                    "that led to it stays as a dead end. Then the rest is judged as usual. Off: the whole start is judged (and pruneWetJigsaws is off too)."
            ).define("prune", true);
            pruneMaxHanging = b.comment(
                    "Earth blocks of one piece over water or air the beard won't fill before that piece is dropped. -1 = never."
            ).defineInRange("pruneMaxHanging", 8, -1, 100000);
            pruneMaxMound = b.comment(
                    "Mound columns of one piece before that piece is dropped. -1 = never."
            ).defineInRange("pruneMaxMound", 4, -1, 100000);
            pruneMinWetColumns = b.comment(
                    "Water columns a piece's own footprint needs before its water share can drop it (so a well or a hut over",
                    "a small pond stays). -1 = never drop a piece for its water share."
            ).defineInRange("pruneMinWetColumns", 16, -1, 100000);
            pruneWetJigsaws = b.comment(
                    "With prune on, every other jigsaw structure on the surface (camps, outposts, ruins, towers; meta",
                    "structure parts and anchors too) drops a piece whose own footprint is too wet the same way, with every",
                    "piece attached below it; the piece limits above stay for villages. The first piece is never dropped,",
                    "and the rest is then judged as usual. Not for creativeengineering:water_fill/structures,",
                    "creativeengineering:earth_fill/wet_ok or the structure tag creativeengineering:earth_fill/no_prune",
                    "(structures whose pieces only make sense together). Off: only villages are pruned (before 2026-10-30)."
            ).define("pruneWetJigsaws", true);
            footprintMinWater = b.comment(
                    "Water columns a whole start's footprint needs before its water share (maxWaterSurplus) can cancel it,",
                    "so a cliff-edge start whose layer is mostly air doesn't drop on one wet column. 0 = any (before",
                    "2026-10-30)."
            ).defineInRange("footprintMinWater", 9, 0, 100000);
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
            ).defineInRange("sampleGrid", 4, 1, 8);
            log = b.comment(
                    "CANCELLED: log each structure that is cancelled, and each pruned village or meta part dropped (id, chunk,",
                    "counts, limits). ALL: log every checked structure that has earth in its base or (footprintAllPieces) a",
                    "surface piece. For tuning the limits."
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
        public final ForgeConfigSpec.IntValue lowestFloorY;
        public final ForgeConfigSpec.IntValue clearance;
        public final ForgeConfigSpec.IntValue spread;
        public final ForgeConfigSpec.DoubleValue stayShare;
        public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> untouched;
        public final ForgeConfigSpec.EnumValue<GeodeFloor> floorPatch;

        private OceanGeodes(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Geodes whose column is an ocean (the biome at the sea floor is #minecraft:is_ocean) are moved down before",
                    "they generate, so they stay under the deepest sea floor the world can have. A moved geode's top (its",
                    "origin + the geode's own maxGenOffset) ends at least clearance blocks under lowestFloorY; geodes already",
                    "that deep stay where they are. Which geodes stay and how far each spreads is a fixed roll per position."
            ).push("ocean_geodes");
            lowestFloorY = b.comment("The lowest sea floor y any ocean in the pack generates (ST-133: Terralith deep oceans near y 0;",
                    "Tectonic deeper_oceans can go lower).")
                    .defineInRange("lowestFloorY", -8, -64, 64);
            clearance = b.comment("Blocks of stone kept between a moved geode's top and lowestFloorY.")
                    .defineInRange("clearance", 2, 0, 16);
            spread = b.comment("Moved geodes land up to this many blocks under the highest allowed origin (a roll per position),",
                    "so they don't all sit on one layer.")
                    .defineInRange("spread", 8, 0, 32);
            stayShare = b.comment("Share of geodes under oceans that keep their own height (0.1 = 1 in 10).")
                    .defineInRange("stayShare", 0.1, 0.0, 1.0);
            untouched = b.comment("Geodes whose inner layer is one of these blocks are left at their own height (Geode Plus'",
                    "prismarine geode belongs on the sea floor).")
                    .defineList("untouched", java.util.List.of("geode:budding_prismarine"), o -> o instanceof String);
            floorPatch = b.comment("A geode under an ocean that still reaches the sea floor (the stayShare ones; not the untouched",
                    "ones). SWAP: it keeps its shape, and its outer and middle layer blocks from the floor up become the floor",
                    "block that column had before (sand, gravel, clay), so it reads as a mound of sea floor with the opening",
                    "into its crystals. CUT: it is cut flush with the floor (its blocks above the floor become water) and the",
                    "rim left on the surface becomes the floor block. OFF: it sticks out as generated.")
                    .defineEnum("floorPatch", GeodeFloor.SWAP);
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
                    "too, and so does the server stopping. 0 = no deadline. Applies to new searches immediately.",
                    "Tuned 2026-10-30 (user): 300 [120], so the fortified villages' meta camps and ruins can answer.")
                    .defineInRange("searchTimeoutSeconds", 300, 0, 3600);
            b.pop();
        }
    }

    /**
     * Distant Horizons' own world generation (its throwaway LOD chunks, never real chunks): the bottom of the world
     * is not generated; oceans get a fake seabed instead. See feature_writeup/locate.md 3.7.
     */
    /** Settling window of {@code worldgen.FirstTickWater} ([worldgen.systems] firstTickWater switches it). */
    public static final class FirstTickWaterCfg {
        public final ForgeConfigSpec.IntValue windowTicks;

        private FirstTickWaterCfg(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Generated water flows for the first time when its chunk starts ticking. For windowTicks after that, that",
                    "water and every block it spreads to drop no items when the flow breaks something."
            ).push("first_tick_water");
            windowTicks = b.comment("Length of the window in ticks (20 = 1 s), counted from the chunk's first tick and refreshed",
                    "each time the water spreads to a block. 0: no window (vanilla drops). Applies at once.")
                    .defineInRange("windowTicks", 600, 0, 72000);
            b.pop(); // first_tick_water
        }
    }

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
        public final ForgeConfigSpec.BooleanValue avoidCoasts;
        public final ForgeConfigSpec.BooleanValue keepShortParts;

        private MetaStructures(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Meta structures: one structure start made of other structures (an anchor such as a village, plus",
                    "satellites such as towers around it) and placed features (driftwood on the sea). Every part keeps",
                    "within 7 chunks of the start so the whole thing generates; a part that can't fit (biome, room)",
                    "is retried elsewhere; a part short of its minimum count keeps what fitted (keepShortParts).",
                    "A meta with an anchor only takes a share of the slots its anchor already won, so no structure's rate",
                    "changes. /locate finds a meta by its id and a structure inside one by the structure's own id."
            ).push("meta_structures");
            log = b.comment(
                    "Log every meta structure start that is assembled: its anchor, each part and how many of each",
                    "fitted, or which part fell short (the slot is then the plain anchor)."
            ).define("log", false);
            avoidCoasts = b.comment(
                    "Keep meta structures out of coasts and rivers (the biome tag creativeengineering:coast_river: beaches,",
                    "shores, rivers; kubejs biome_fits.json biome_tags). No part may start there or reach there with its box",
                    "(centre or a corner): a satellite tries another spot, an anchor there leaves the slot to the plain anchor,",
                    "which still spawns as it would alone. Metas in the structure tag creativeengineering:meta/coast_ok (fleets,",
                    "flotillas, wrecks, sunken ruins) are not affected. New chunks only."
            ).define("avoidCoasts", true);
            keepShortParts = b.comment(
                    "A part that fits fewer times than its minimum count (a satellite near a coast, on a cliff) is kept with",
                    "what fitted instead of failing the whole meta; earth fill likewise drops an offending satellite and",
                    "prunes village parts instead of cancelling the meta. The meta only fails when no structure besides the",
                    "anchor is left (the slot is then the plain anchor). Off: a short part fails the meta (before 2026-10-30)."
            ).define("keepShortParts", true);
            b.pop(); // meta_structures
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
     * Dials for {@code trader.WanderingTraderTweaks} (Wandering Trader wait and offer counts, through
     * {@code mixin.WanderingTraderSpawnerMixin} / {@code mixin.WanderingTraderTradesMixin}). The Wandering
     * Winemaker's spawner is the Vinery port's own (vinery-common.toml [trader]). What the pools hold is
     * set in KubeJS ({@code kubejs/server_scripts/economy/wandering_trades.js}).
     */
    public static final class WanderingTrader {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.IntValue traderIntervalTicks;
        public final ForgeConfigSpec.IntValue commonTrades;
        public final ForgeConfigSpec.IntValue rareTrades;

        private WanderingTrader(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Wandering Trader spawning and offer counts (the Wandering Winemaker: vinery-common.toml [trader]).",
                    "The spawner rolls once per interval, counted in server ticks (20 = 1 second) while the",
                    "overworld runs with doMobSpawning on. Day length (Better Days), sleeping and /time do not",
                    "change it. A roll is vanilla's: 25/50/75% chance (rising until a spawn), then 1 in 10, then",
                    "a free spot near a random player; on average about 14 rolls per spawn.",
                    "/gamerule doTraderSpawning false stops it and the Winemaker."
            ).push("wandering_trader");

            enabled = b.comment("Off = vanilla Wandering Trader (24000-tick interval, 5 + 1 offers).")
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

            b.pop(); // wandering_trader
        }
    }

    /** Named mobs ({@code mobs.NamedMobs}, feature_writeup/mob_block_tweaks.md section 2.6). Read live. */
    public static final class NamedMobs {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.DoubleValue chance;
        public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> mobs;
        public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> spawnReasons;
        public final ForgeConfigSpec.BooleanValue announceDeaths;
        public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> fallbackNames;

        private NamedMobs(ForgeConfigSpec.Builder b) {
            b.comment(
                    "A listed mob that spawns for one of the listed reasons has a small chance to get a random name",
                    "(Minecraft Comes Alive's citizen names, else fallbackNames). Its death is announced in chat.",
                    "Mobs already named are left alone; bred, egg, spawner and command mobs never roll."
            ).push("named_mobs");

            enabled = b.comment("Off: no new names. Mobs named earlier keep their name and announcement.")
                    .define("enabled", true);
            chance = b.comment("Chance per eligible spawn. 0.01 = 1 in 100.")
                    .defineInRange("chance", 0.01, 0.0, 1.0);
            mobs = b.comment("Entity ids, or #entity type tags, that can be named.")
                    .defineList("mobs", java.util.List.of("minecraft:cow", "minecraft:mooshroom", "minecraft:pig",
                            "minecraft:sheep", "minecraft:chicken", "minecraft:goat"), o -> o instanceof String s && !s.isBlank());
            spawnReasons = b.comment("Spawn reasons that roll: NATURAL, CHUNK_GENERATION, STRUCTURE (see MobSpawnType for the rest).")
                    .defineList("spawnReasons", java.util.List.of("NATURAL", "CHUNK_GENERATION", "STRUCTURE"), o -> o instanceof String);
            announceDeaths = b.comment("Every player sees a named mob's death message (\"Bessie the Cow was slain by Steve\").",
                    "Follows the showDeathMessages gamerule.")
                    .define("announceDeaths", true);
            fallbackNames = b.comment("Names used when Minecraft Comes Alive is missing or its name lists are not loaded.")
                    .defineList("fallbackNames", java.util.List.of("Bessie", "Clover", "Daisy", "Biscuit", "Maple", "Pepper",
                            "Hazel", "Barnaby", "Rosie", "Winston", "Mabel", "Otis", "Juniper", "Gus", "Poppy", "Waffles"),
                            o -> o instanceof String s && !s.isBlank());

            b.pop(); // named_mobs
        }
    }

    /** {@code mixin/ServerAdvancementManagerMixin}: the pack has no advancements at all (user, 2026-10-18). */
    public static final class Advancements {
        public final ForgeConfigSpec.BooleanValue enabled;

        private Advancements(ForgeConfigSpec.Builder b) {
            b.comment("Advancements (vanilla, every mod, every datapack).").push("advancements");
            enabled = b.comment(
                    "false (the pack's setting): none are loaded, so none show, toast or give rewards.",
                    "Read when data loads: a change applies on the next /reload or world start."
            ).define("enabled", false);
            b.pop();
        }
    }

    /** Mod display names ({@code util.ModDisplayNames}): what JEI, WTHIT and the Mods screen call a mod. */
    public static final class ModNames {
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> overrides;

        private ModNames(ForgeConfigSpec.Builder b) {
            b.comment("Mod display names (client side: JEI's mod line and @ search, WTHIT, the Mods screen).").push("mod_names");

            overrides = b.comment(
                    "\"modid=Name\" entries replacing the name from the mod's mods.toml. Applied at startup and when this",
                    "file is saved; JEI and WTHIT keep the name they read first, so a change shows after a restart.",
                    "Removing an entry gives the mod its own name back. The mod id stays the same (JEI @<modid> still works).",
                    "Pack style (feature_writeup/names.md 12): words spaced, possessive apostrophes, '&' for and / 'n',",
                    "Title Case, Create addons as \"Create: <name>\", no author handles, the pack's own mods end in (Alfonzo)."
            ).defineList("overrides", List.of(
                    "abnormals_delight=Abnormals' Delight",
                    "apexcore=Apex Core",
                    "appleskin=Apple Skin",
                    "astikorcarts=Astikor Carts",
                    "autoreglib=Auto Reg Lib",
                    "baby_mobs=Baby Mobs (Alfonzo)",
                    "betterdays=Better Days",
                    "betterf3=Better F3",
                    "boatview360=Boat View 360",
                    "bobberdetector=Create: Bobber Detector",
                    "brewinandchewin=Brewin' & Chewin' Backport (Alfonzo)",
                    "canned_goods=Canned Goods",
                    "cb_microblock=CB Microblock",
                    "cb_multipart=CB Multipart",
                    "cb_multipart_minecraft=CB Multipart Minecraft",
                    "cccbridge=Create: CC Bridge",
                    "chefsdelight=Chef's Delight",
                    "cherisheditems=Cherished Items",
                    "codechickenlib=Code Chicken Lib",
                    "compressedcreativity=Create: Compressed Creativity",
                    "conditional_mixin=Conditional Mixin",
                    "cookingforblockheads=Cooking for Blockheads",
                    "corpsecurioscompat=Corpse Curios Compat",
                    "cosmeticarmorreworked=Cosmetic Armor Reworked",
                    "create__the_salt=Create: The Salt",
                    "create_compressed=Create: Compressed",
                    "create_confectionery=Create: Confectionery",
                    "create_crush_everything=Create: Recycling",
                    "create_enchantment_industry=Create: Enchantment Industry",
                    "create_mechanical_extruder=Create: Mechanical Extruder",
                    "create_questing=Create: Questing",
                    "create_things_and_misc=Create: Things & Misc",
                    "createaddition=Create: Crafts & Additions",
                    "createappliedkinetics=Create: Applied Kinetics",
                    "createdeco=Create: Deco",
                    "creategbd=Create: Guardian Beam Defense",
                    "creategoggles=Create: Goggles",
                    "createliquidfuel=Create: Liquid Fuel",
                    "creative_labels=Creative Labels (Alfonzo)",
                    "creativecore=Creative Core",
                    "creativeengineering=Creative Engineering (Alfonzo)",
                    "ctm=Connected Textures Mod",
                    "darkpaintings=Dark Paintings",
                    "deathknell=Death Knell",
                    "design_decor=Create: Design & Decor",
                    "do_a_barrel_roll=Do a Barrel Roll",
                    "dont_void_arrows=Don't Void Arrows",
                    "dragonloot=Dragon Loot",
                    "dummmmmmy=Target Dummy",
                    "enchdesc=Enchantment Descriptions",
                    "enderstorage=Ender Storage",
                    "entityculling=Entity Culling",
                    "everythingcopper=Everything Is Copper",
                    "exposure=Exposure Backport & Extras (Alfonzo)",
                    "exposure_catalog=Exposure Catalog Backport (Alfonzo)",
                    "exposure_expanded=Exposure: Expanded Backport (Alfonzo)",
                    "exposure_extras=Exposure Extras (Alfonzo)",
                    "exposure_polaroid=Exposure Polaroid Backport (Alfonzo)",
                    "extendedgears=Create: Extended Cogwheels",
                    "farmers_structures=Farmer's Structures",
                    "fastfurnace=Fast Furnace",
                    "fireflybushport=Firefly Bush Port",
                    "fishontheline=Fish on the Line",
                    "fortuneonneterite_i_ig_z=Fortune on Netherite",
                    "foundation=Foundation Vinery Backport (Alfonzo)",
                    "framedblocks=Framed Blocks",
                    "geckolib3=Gecko Lib",
                    "hobbit_house_=Hobbit House",
                    "hopour=Hopo Better Underwater Ruins",
                    "immediatelyfast=Immediately Fast",
                    "incubation_additions=Incubation Additions (Alfonzo)",
                    "ipo=Improved Pillager Outpost",
                    "itemphysic=Item Physic",
                    "just_enough_beacons=Just Enough Beacons",
                    "kotlinforforge=Kotlin for Forge",
                    "kubejsdelight=KubeJS Delight",
                    "lightmansdiscord=Lightman's Discord Integration",
                    "littlecontraptions=Create: Little Contraptions",
                    "minecraft_style_paintings=Minecraft Style Paintings",
                    "mixinsquared=Mixin Squared",
                    "more_mooblooms=More Mooblooms (Alfonzo)",
                    "netherportalfix=Nether Portal Fix",
                    "nocubescreateexp=Create: Compact Exp",
                    "notenoughanimations=Not Enough Animations",
                    "old_swamp_fisherman_house=Old Swamp Fisherman House",
                    "oreexcavation=Ore Excavation",
                    "portablewardrobe=Portable Wardrobe",
                    "projectred_core=Project Red Core",
                    "projectred_fabrication=Project Red Fabrication",
                    "projectred_illumination=Project Red Illumination",
                    "projectred_integration=Project Red Integration",
                    "projectred_transmission=Project Red Transmission",
                    "railways=Create: Steam & Rails",
                    "restapi=REST API",
                    "sawmillhouse=Sawmill House",
                    "seasonhud=Season HUD",
                    "simplehats=Simple Hats",
                    "sliceanddice=Create: Slice & Dice",
                    "spectrelib=Spectre Lib",
                    "supermartijn642configlib=Config Library",
                    "supermartijn642corelib=Core Lib",
                    "swingthroughgrass=Swing Through Grass",
                    "t_and_t=Towns & Towers",
                    "takesapillage=It Takes a Pillage",
                    "tmted=Engineer's Delight",
                    "transition=Transition",
                    "trashslot=Trash Slot",
                    "treechop=HT's Tree Chop",
                    "villagersellanimals=Villagers Sell Animals",
                    "vinery=Let's Do Vinery Backport (Alfonzo)",
                    "w2w2=Waystones 2 Waypoints 2",
                    "waila=Waila",
                    "whitenoise=White Noise",
                    "worldedit=World Edit",
                    "wthit=WTHIT",
                    "xaerolib=Xaero Lib",
                    "xaerozoomout=Xaero Zoomout"
            ), o -> o instanceof String str && str.indexOf('=') > 0);

            b.pop(); // mod_names
        }
    }

}
