package com.SaintAlfonzo.creativeequipment;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.SaintAlfonzo.creativeequipment.shield.ElementalKind;
import com.SaintAlfonzo.creativeequipment.shield.ShieldConfig;
import com.SaintAlfonzo.creativeequipment.shield.ShieldGroup;
import com.SaintAlfonzo.creativeequipment.shield.ShieldTickGate;
import com.SaintAlfonzo.creativeequipment.shield.ShieldTier;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Creative Equipment's COMMON config, {@code config/creativeequipment-common.toml}. The sections moved out of
 * patcherMod's creativeengineering-common.toml on 2026-11-04 (creative_split.md 7.6, wave 7) with keys, comments,
 * defaults and field names unchanged: {@code [dragon_gear]}, {@code [air_brake]}, {@code [stepstool]},
 * {@code [crafting]} (the felsic assembly values), {@code [armor_enchantability]}, {@code [shields]},
 * {@code [bonemeal_charm]}, {@code [bottled_air]} and {@code [backpackSkins]}. The first boot with Equipment copies a
 * player's values over ({@code swap.ConfigRename.carrySections}, called from {@link CreativeEquipment}). The static
 * field names are CEConfig's, so {@code global.NCH.cfg(..., 'CRAFTING.FELSIC_SWORD')} finds them once the scripts
 * register this class. {@code [damage_tags]} stays in Common with the damage tag framework (Creative Brewing reads it).
 */
public final class CEQConfig {

    private CEQConfig() {}

    public static final ForgeConfigSpec SPEC;
    public static final DragonGear DRAGON_GEAR;
    public static final AirBrake AIR_BRAKE;
    public static final Stepstool STEPSTOOL;
    public static final Crafting CRAFTING;
    public static final ArmorEnchant ARMOR_ENCHANT;
    public static final Shields SHIELDS;
    public static final BonemealCharm BONEMEAL_CHARM;
    public static final BottledAir BOTTLED_AIR;
    public static final BackpackSkins BACKPACK_SKINS;
    public static final Tooltips TOOLTIPS;

    /** The toml tables carried over from creativeengineering-common.toml. */
    public static final List<String> SECTIONS = List.of("dragon_gear", "air_brake", "stepstool", "crafting",
            "armor_enchantability", "shields", "bonemeal_charm", "bottled_air", "backpackSkins");

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        // Same order as in CEConfig before the carve.
        DRAGON_GEAR = new DragonGear(builder);
        AIR_BRAKE = new AirBrake(builder);
        STEPSTOOL = new Stepstool(builder);
        CRAFTING = new Crafting(builder);
        ARMOR_ENCHANT = new ArmorEnchant(builder);
        SHIELDS = new Shields(builder);
        BONEMEAL_CHARM = new BonemealCharm(builder);
        BOTTLED_AIR = new BottledAir(builder);
        BACKPACK_SKINS = new BackpackSkins(builder);
        TOOLTIPS = new Tooltips(builder);
        SPEC = builder.build();
    }

    /** Curios "Slot:" tooltip line (compat/CuriosSlotTooltip; moved from Creative Common's [tooltips] 2026-11-04). */
    public static final class Tooltips {
        public final ForgeConfigSpec.BooleanValue curiosSlotLine;

        private Tooltips(ForgeConfigSpec.Builder b) {
            b.comment("Item tooltips (client side).").push("tooltips");
            curiosSlotLine = b.comment(
                    "Every item that fits a Curios slot shows Curios' \"Slot:\" line, also when the item's own mod",
                    "hides or replaces it. Off: only the lines Curios and the item's mod add."
            ).define("curiosSlotLine", true);
            b.pop();
        }
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
            String magicResistance = "creativebrewing:magic_resistance"; // Creative Brewing's effect (creativeengineering: up to 2026-11-04)
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
                                "creativecommon:burn_tick damage tag (on fire, in fire, campfires, braziers, bonfires,",
                                "magma blocks, stoves). Default: netherite and above. Lava and fan fire are not in it."};
                        case CONTACT -> new String[] {
                                "Whether this tier may block thorn ticks at all: the creativecommon:contact_tick",
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
}
