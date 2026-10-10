package com.SaintAlfonzo.creativeengineering.shield;

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;

/**
 * The eight tier rungs, in ladder order. Seven of them (copper..dragon) are dedicated new items
 * ({@code creativeengineering:<id>_shield}, registered by {@link ShieldItems} for every tier
 * EXCEPT {@link #IRON}). IRON is folded into the player's REAL vanilla shield ({@code Items.SHIELD})
 * instead of spawning an 8th new item - see {@link #of(ItemStack)}, the single place that decides
 * "what tier (if any) is this stack", used everywhere a hit needs to know it (`ShieldBlocking`,
 * `ShieldBlockHandler`) instead of an {@code instanceof TierShieldItem} check. Durability
 * ({@code mixin/VanillaShieldDurabilityMixin}) and anvil repair material
 * ({@code mixin/VanillaShieldRepairMixin}) are config-driven for it too, the same as every other
 * tier; only its rendering stays vanilla's own (the seven dedicated items have their own textures,
 * {@code shield/client/TierShieldMaterials}).
 *
 * <p>Everything here is only a DEFAULT: durability, repair item, the physical / elemental / magic
 * reduction and durability cost, and the on-block effects are all live config
 * ({@code CEConfig.Shields}, {@code [shields.<id>]} in creativeengineering-common.toml). A tier
 * can also default straight into the elemental fire/electric/cold/chemical breakdown instead of
 * the blanket elemental percentage by giving the "elem" constructor argument an {@code int[]}
 * instead of a plain {@code int} - see {@link #defaultElementalBreakdown} - which IRON does
 * (fire 25, the rest 0), as do COPPER, DIAMOND and NETHERITE; GOLD, ECHO, FELSIC and DRAGON default
 * to blanket mode (all four at -1). Either is only an initial value config can then override.
 *
 * <p>These defaults only fill a freshly generated toml. The pack ships its own tuned
 * {@code creativeengineering-common.toml}, which is what actually plays ({@code tools/shield_check.py}
 * prints both and where they differ).
 *
 * <p>Default rationale (placeholders - tune in config):
 * <ul>
 *   <li><b>Durability</b>: diamond -> dragon match the same tier's sword durability
 *       ({@code kubejs/startup_scripts/func/tune_tools.js}). Copper is vanilla's shield (336)
 *       scaled by the copper tool tier (180 vs iron's 250); gold is half of that scale because a
 *       tool-proportional ~43 would be unusable. IRON's own 336 is vanilla's real, unmodified
 *       shield durability, kept as the starting default now that it's actually enforced.</li>
 *   <li><b>Reduction</b>: physical scales up by tier (copper 40% -> felsic 95%; the shipped
 *       toml has 20% -> 80%); only DRAGON blocks physical hits at vanilla's full 100%. ELEMENTAL and MAGIC - which a plain vanilla
 *       shield could never block at all before this feature existed - are separate columns.</li>
 * </ul>
 */
public enum ShieldTier { //TODO: ajdust kbthreshold to be more reasonable, add variable shield cooldown to each shield 
   
    //                                                                    fire/electric/cold/chemical
    //                                                                     new int[]{25, 30, 35, 40}

    //          id            durability  repair item                phys  elem                   magic fireproof     rarity      kbThreshold
    //  Blocker effect                    Attacker effect
    COPPER("copper",         242, "minecraft:copper_ingot",           40,  new int[]{0,50,0,0},    0, false, Rarity.COMMON, 1),
    GOLD("gold",             168, "minecraft:gold_ingot",             50,  30,                    30, false, Rarity.COMMON, 2),
    IRON("iron",             336, "minecraft:iron_ingot",             60,  new int[]{25,0,0,0},    0, false, Rarity.COMMON, 3),
    DIAMOND("diamond",      1561, "minecraft:diamond",                70,  new int[]{50,0,0,0},   25, false, Rarity.RARE,   4),
    NETHERITE("netherite",  2031, "minecraft:netherite_ingot",        80,  new int[]{100,0,0,0},  50, true,  Rarity.RARE,   5),
    ECHO("echo",            2124, "create_deep_dark:echo_ingot",      90,  50,                    60, true,  Rarity.EPIC,   6),
    FELSIC("felsic",        2501, "felsic_gear:felsic_ingot",         95,  75,                    75, true,  Rarity.EPIC,   7),
    DRAGON("dragon",        3350, "dragonloot:dragon_scale",          100, 100,                  100, true,  Rarity.EPIC,   8,
        "minecraft:regeneration;100;0", "minecraft:poison;100;0");

    private final String id;
    private final int defaultDurability;
    private final String defaultRepairItem;
    private final int defaultPhysical;
    private final int defaultElemental;
    private final int defaultMagic;
    private final boolean fireproof;
    private final Rarity rarity;
    private final int defaultKnockbackThreshold;
    private final String defaultShielderEffects;
    private final String defaultAttackerEffects;
    private final int defaultFire;
    private final int defaultElectric;
    private final int defaultCold;
    private final int defaultChemical;

    /** Blanket mode, no on-block effects: the "elem" slot is one 0-100 number, same as physical/magic. */
    ShieldTier(String id, int defaultDurability, String defaultRepairItem,
               int defaultPhysical, int defaultElemental, int defaultMagic, boolean fireproof, Rarity rarity,
               int defaultKnockbackThreshold) {
        this(id, defaultDurability, defaultRepairItem, defaultPhysical, defaultElemental, defaultMagic,
                fireproof, rarity, defaultKnockbackThreshold, "", "");
    }

    /** Blanket mode, with on-block effects (only {@link #DRAGON} uses this one). */
    ShieldTier(String id, int defaultDurability, String defaultRepairItem,
               int defaultPhysical, int defaultElemental, int defaultMagic, boolean fireproof, Rarity rarity,
               int defaultKnockbackThreshold, String defaultShielderEffects, String defaultAttackerEffects) {
        // -1,-1,-1,-1: blanket mode - every tier using an int here (everyone except IRON) stays
        // on the single defaultElemental percentage; see the int[]-taking overloads below and
        // ShieldConfig#elementalOverrideRaw for the other half of this "-1 = unset" contract.
        this.id = id;
        this.defaultDurability = defaultDurability;
        this.defaultRepairItem = defaultRepairItem;
        this.defaultPhysical = defaultPhysical;
        this.defaultElemental = defaultElemental;
        this.defaultMagic = defaultMagic;
        this.fireproof = fireproof;
        this.rarity = rarity;
        this.defaultKnockbackThreshold = defaultKnockbackThreshold;
        this.defaultShielderEffects = defaultShielderEffects;
        this.defaultAttackerEffects = defaultAttackerEffects;
        this.defaultFire = -1;
        this.defaultElectric = -1;
        this.defaultCold = -1;
        this.defaultChemical = -1;
    }

    /**
     * Breakdown mode, no on-block effects: the SAME "elem" slot instead takes a 4-element
     * {@code {fire, electric, cold, chemical}} array (that order - {@link ElementalKind}'s
     * declaration order) in place of one blanket number - what {@link #IRON} uses. A tier that
     * wants breakdown defaults writes an {@code int[]} literal here instead of a plain {@code int};
     * nothing about the constructor call otherwise changes shape or grows a trailing tail of ints.
     */
    ShieldTier(String id, int defaultDurability, String defaultRepairItem,
               int defaultPhysical, int[] defaultElementalBreakdown, int defaultMagic, boolean fireproof, Rarity rarity,
               int defaultKnockbackThreshold) {
        this(id, defaultDurability, defaultRepairItem, defaultPhysical, defaultElementalBreakdown, defaultMagic,
                fireproof, rarity, defaultKnockbackThreshold, "", "");
    }

    /** Breakdown mode, with on-block effects (no current tier needs both, but nothing stops one). */
    ShieldTier(String id, int defaultDurability, String defaultRepairItem,
               int defaultPhysical, int[] defaultElementalBreakdown, int defaultMagic, boolean fireproof, Rarity rarity,
               int defaultKnockbackThreshold, String defaultShielderEffects, String defaultAttackerEffects) {
        this.id = id;
        this.defaultDurability = defaultDurability;
        this.defaultRepairItem = defaultRepairItem;
        this.defaultPhysical = defaultPhysical;
        // The blanket value is irrelevant once a breakdown array is given at all (ShieldConfig's
        // "any one of the four is set -> array mode, blanket ignored entirely" rule), so there is
        // nothing meaningful to put here; 0 documents "not used".
        this.defaultElemental = 0;
        this.defaultMagic = defaultMagic;
        this.fireproof = fireproof;
        this.rarity = rarity;
        this.defaultKnockbackThreshold = defaultKnockbackThreshold;
        this.defaultShielderEffects = defaultShielderEffects;
        this.defaultAttackerEffects = defaultAttackerEffects;
        if (defaultElementalBreakdown.length != ElementalKind.values().length) {
            throw new IllegalArgumentException(id + ": elemental breakdown array must have exactly "
                    + ElementalKind.values().length + " values (fire, electric, cold, chemical), got "
                    + defaultElementalBreakdown.length);
        }
        this.defaultFire = defaultElementalBreakdown[0];
        this.defaultElectric = defaultElementalBreakdown[1];
        this.defaultCold = defaultElementalBreakdown[2];
        this.defaultChemical = defaultElementalBreakdown[3];
    }

    /** Registry-name prefix ({@code creativeengineering:<id>_shield}) and toml section name. */
    public String id() {
        return id;
    }

    public int defaultDurability() {
        return defaultDurability;
    }

    public String defaultRepairItem() {
        return defaultRepairItem;
    }

    /** Item-entity survives fire/lava (matches vanilla netherite gear and the pack's echo/felsic/dragon tools). */
    public boolean fireproof() {
        return fireproof;
    }

    public Rarity rarity() {
        return rarity;
    }

    /**
     * Default damage amount (after this tier's reduction) below which a blocked hit no longer
     * knocks the HOLDER back ({@link ShieldBlockHandler#onKnockback}); the attacker's own recoil
     * off the shield is left vanilla. Placeholder ladder: 1 for copper, +1 per tier up to 8 for dragon.
     */
    public int defaultKnockbackThreshold() {
        return defaultKnockbackThreshold;
    }

    /**
     * Default for {@code [shields.<tier>].<gate key>}: whether this tier may block {@code gate}'s ticks at
     * all ({@link ShieldTickGate}). Ladder rules: burning and hot blocks from NETHERITE up, thorns and
     * barbed panes from DIAMOND up. Below that the shield doesn't even attempt the block - no shield
     * sound, full damage - whatever its percentages say.
     */
    public boolean defaultBlocksTicks(ShieldTickGate gate) {
        return switch (gate) {
            case BURN -> ordinal() >= NETHERITE.ordinal();
            case CONTACT -> ordinal() >= DIAMOND.ordinal();
        };
    }

    /** Default on-block effects for the holder, in the {@code ShieldEffects} string format. */
    public String defaultShielderEffects() {
        return defaultShielderEffects;
    }

    /** Default on-block effects for the attacker, in the {@code ShieldEffects} string format. */
    public String defaultAttackerEffects() {
        return defaultAttackerEffects;
    }

    /** Default percent of a hit this tier absorbs for {@code group}; 0 = not blocked at all. */
    public int defaultReduction(ShieldGroup group) {
        return switch (group) {
            case PHYSICAL -> defaultPhysical;
            case ELEMENTAL -> defaultElemental;
            case MAGIC -> defaultMagic;
            // A group added later starts as "not blocked" until someone configures it.
            default -> 0;
        };
    }

    /**
     * Default per-element override for {@code kind} under {@link ShieldGroup#ELEMENTAL}; -1 means
     * "no default, stay in blanket mode" (true for every tier above right now - none has opted
     * in). This is what {@code CEConfig} seeds each tier's {@code elemental_breakdown} toml
     * entries with, and what {@code ShieldConfig} falls back to before the config file loads -
     * the same -1 sentinel and the same source of truth as {@link #defaultReduction}, not a
     * second, independently-hardcoded default living in the config layer.
     */
    public int defaultElementalBreakdown(ElementalKind kind) {
        return switch (kind) {
            case FIRE -> defaultFire;
            case ELECTRIC -> defaultElectric;
            case COLD -> defaultCold;
            case CHEMICAL -> defaultChemical;
        };
    }

    /**
     * The tier {@code stack} shields as, or {@code null} if it isn't a shield this feature knows
     * about at all (a plain item, Immersive Engineering's shield, ...). This is the ONE place that
     * decides "is this a tier shield" - {@code ShieldBlocking}/{@code ShieldBlockHandler} call this
     * instead of an {@code instanceof TierShieldItem} check, which is what lets {@link #IRON} (the
     * player's real {@code Items.SHIELD}, not a {@link TierShieldItem}) participate too.
     */
    @Nullable
    public static ShieldTier of(ItemStack stack) {
        if (stack.getItem() instanceof TierShieldItem item) {
            return item.tier();
        }
        return stack.getItem() == Items.SHIELD ? IRON : null;
    }
}
