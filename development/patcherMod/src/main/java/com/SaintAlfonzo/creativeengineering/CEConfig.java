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

    /** How the WTHIT Run Speed line writes a multiplier: 1.50× or +50%. */
    public enum SpeedFormat { MULTIPLIER, PERCENT }

    public static final ForgeConfigSpec COMMON_SPEC;
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
    public static final Tooltips TOOLTIPS;
    public static final Changelog CHANGELOG;
    public static final Shields SHIELDS;
    public static final DamageTagConfig DAMAGE_TAGS;
    public static final BonemealCharm BONEMEAL_CHARM;
    public static final BottledAir BOTTLED_AIR;
    public static final Coaster COASTER;
    public static final Wthit WTHIT;
    public static final Perf PERF;
    public static final Surface SURFACE;
    public static final VeinUndo VEIN_UNDO;
    public static final BackpackSkins BACKPACK_SKINS;
    public static final WanderingTrader WANDERING_TRADER;
    public static final Advancements ADVANCEMENTS;
    public static final ModNames MOD_NAMES;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        // [forgiving_void], [leaf_decay], [raids], [worldgen], [named_mobs] and the [perf] mob cap keys:
        // creativeworldgen-common.toml (Creative Worldgen, 2026-11-02)
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
        TOOLTIPS = new Tooltips(builder);
        CHANGELOG = new Changelog(builder);
        SHIELDS = new Shields(builder);
        DAMAGE_TAGS = new DamageTagConfig(builder);
        BONEMEAL_CHARM = new BonemealCharm(builder);
        // [leaf_piles], [moss], [vine_shearing], [campfire], [monster_box], [trash_slot]: creativecompat-common.toml
        // (Creative Compat, 2026-11-01)
        BOTTLED_AIR = new BottledAir(builder);
        COASTER = new Coaster(builder);
        WTHIT = new Wthit(builder);
        PERF = new Perf(builder);
        // [seasons], [crop_seasons], [time]: creativecalendar-common.toml (Creative Calendar, 2026-11-01)
        SURFACE = new Surface(builder);
        VEIN_UNDO = new VeinUndo(builder);
        BACKPACK_SKINS = new BackpackSkins(builder);
        WANDERING_TRADER = new WanderingTrader(builder);
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

        private Perf(ForgeConfigSpec.Builder b) {
            b.comment("Performance throttles for other mods' per-tick work. See feature_writeup/perf.md section 7.")
                    .push("perf");

            // troughMissCooldownTicks: creativedelight-common.toml [perf] (Creative Delight, 2026-11-01)

            // seasonsMeltIntervalTicks: creativecalendar-common.toml [perf] (Creative Calendar, 2026-11-01)

            // mobCapLevels, mobCapForceLevel, mobCapLevel1/2Chunks, mobCapHysteresisChunks, mobCapLevel1/2Monster,
            // mobCapScaledCategories, spawnChunksLevel1/2, spawnBandLevel1/2, spawnBandCategories:
            // creativeworldgen-common.toml [perf] (Creative Worldgen, 2026-11-02)

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

    /**
     * Item tooltip dials (client side; a server ignores them): {@code mixin/ScreenTooltipTopMixin} and
     * {@code compat/CuriosSlotTooltip} (feature_writeup/tooltips.md 3).
     */
    public static final class Tooltips {
        public final ForgeConfigSpec.BooleanValue keepTopOnScreen;
        public final ForgeConfigSpec.BooleanValue curiosSlotLine;

        private Tooltips(ForgeConfigSpec.Builder b) {
            b.comment("Item tooltips (client side).").push("tooltips");

            keepTopOnScreen = b.comment(
                    "A tooltip taller than the screen starts at the top and runs off the bottom (vanilla keeps the",
                    "bottom on screen and cuts off the name). Takes effect on the next frame."
            ).define("keepTopOnScreen", true);

            curiosSlotLine = b.comment(
                    "Every item that fits a Curios slot shows Curios' \"Slot:\" line, also when the item's own mod",
                    "hides or replaces it. Off: only the lines Curios and the item's mod add."
            ).define("curiosSlotLine", true);

            b.pop(); // tooltips
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
