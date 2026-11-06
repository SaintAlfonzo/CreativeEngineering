package com.SaintAlfonzo.creativeequipment.shield;

import com.SaintAlfonzo.creativeequipment.CEQConfig;

import java.util.List;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * The reduction / wear / on-block-effect tooltip lines shared by every tier shield: the seven
 * dedicated items via {@code TierShieldItem#appendHoverText}, and vanilla's real
 * {@code minecraft:shield} (folded in as {@link ShieldTier#IRON}) via {@code IronShieldTooltip},
 * since that one has no {@code appendHoverText} override of its own to piggyback on and goes
 * through {@code ItemTooltipEvent} instead. Pulled out to one place so both paths show identical
 * lines for identical config, rather than two copies of the same loop drifting apart.
 *
 * <p>By default only the non-zero defenses are shown, to keep the compact tooltip short. Holding
 * shift (or toggling advanced tooltips, F3+H) switches to the full stat block: every
 * reduction/wear line regardless of value, plus fireproof, tick-gate blocking and knockback
 * threshold, none of which the compact view shows at all. {@code appendLines} is called fresh on every tooltip
 * frame, so this reacts live to the shift key the same way vanilla's own shift-tooltips do.
 */
final class ShieldTooltip {

    private ShieldTooltip() {}

    /** Display order of the per-element lines (not ElementalKind's declaration order, which classify() depends on). */
    private static final ElementalKind[] ELEMENT_ORDER = {
            ElementalKind.COLD, ElementalKind.CHEMICAL, ElementalKind.FIRE, ElementalKind.ELECTRIC};

    // Percentage colour ladder, same breakpoints for every damage type, mirroring the tooltip
    // colour dict in kubejs/assets/creativecommon/kubejs/_lib/tooltips.js (white / col.uncm / col.rare / col.epic).
    // Picked so the current config's non-zero, sub-100 lines split ~1/3 into each of the lower
    // three (9/9/9 as of 2026-09-26); epic is reserved for full 100% blocking.
    private static final int UNCOMMON_AT = 40;
    private static final int RARE_AT = 60;

    static void appendLines(ShieldTier tier, TooltipFlag flag, List<Component> tooltip) {
        boolean full = flag.isAdvanced() || Screen.hasShiftDown();
        for (ShieldGroup g : ShieldGroup.values()) {
            int wear = ShieldConfig.durabilityCost(tier, g);
            if (g == ShieldGroup.ELEMENTAL && ShieldConfig.elementalBreakdownActive(tier)) {
                // Array mode: one line per element - non-zero only, unless `full`, never the
                // blanket "Elemental: X%" line (reduction.elemental is ignored entirely once any
                // of the four is set - see CEQConfig's elemental_breakdown comment).
                for (ElementalKind k : ELEMENT_ORDER) {
                    int pct = ShieldConfig.elementalReduction(tier, k);
                    if (pct > 0 || full) {
                        tooltip.add(reductionLine(k.label(), pct, wear));
                    }
                }
                continue;
            }
            int pct = ShieldConfig.reduction(tier, g);
            if (pct <= 0 && !full) {
                continue;
            }
            if (g == ShieldGroup.ELEMENTAL && full && pct > 0) {
                // Blanket mode, expanded: spell the one number out per element so the full view
                // reads the same whichever mode the tier is in.
                for (ElementalKind k : ELEMENT_ORDER) {
                    tooltip.add(reductionLine(k.label(), pct, wear));
                }
                continue;
            }
            tooltip.add(reductionLine(g.label(), pct, wear));
        }
        if (full) {
            tooltip.add(Component.literal("§7Fireproof: §f" + (tier.fireproof() ? "Yes" : "No")));
            for (ShieldTickGate gate : ShieldTickGate.values()) {
                tooltip.add(Component.literal("§7" + gate.label() + ": §f"
                        + (ShieldConfig.blocksTicks(tier, gate) ? "Yes" : "No")));
            }
            tooltip.add(Component.literal("§7Knockback threshold: §f"
                    + formatThreshold(ShieldConfig.knockbackThreshold(tier))));
        }
        effectLines(tooltip, "§7On block, you gain:", ShieldConfig.shielderEffects(tier));
        effectLines(tooltip, "§7On block, attacker gets:", ShieldConfig.attackerEffects(tier));
        if (!full) {
            tooltip.add(Component.literal("§7Hold [SHIFT] for full stats"));
        }
    }

    private static Component reductionLine(String label, int pct, int wear) {
        return Component.literal("§7" + label + ": " + pctColor(pct) + pct + "%"
                + (wear == 100 ? "" : " §8(" + wear + "% wear)"));
    }

    private static String pctColor(int pct) {
        if (pct >= 100) {
            return "§5"; // dark_purple (epic)
        }
        if (pct >= RARE_AT) {
            return "§1"; // dark_blue (rare)
        }
        if (pct >= UNCOMMON_AT) {
            return "§a"; // green (uncommon)
        }
        return "§f"; // white
    }

    /** Whole numbers print without a decimal (the common case - every current default is a whole point). */
    private static String formatThreshold(float value) {
        return value == Math.floor(value) && !Float.isInfinite(value)
                ? String.valueOf((int) value)
                : String.valueOf(value);
    }

    private static void effectLines(List<Component> tooltip, String header, String raw) {
        List<ShieldEffects.Spec> specs = ShieldEffects.parse(raw);
        if (specs.isEmpty()) {
            return;
        }
        tooltip.add(Component.literal(header));
        for (ShieldEffects.Spec spec : specs) {
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(spec.effect());
            String name = effect == null ? spec.effect().toString() : effect.getDisplayName().getString();
            StringBuilder sb = new StringBuilder("§9 ").append(name);
            if (spec.amplifier() > 0) {
                sb.append(' ').append(spec.amplifier() + 1);
            }
            sb.append(" (").append(spec.duration() / 20).append("s)");
            if (!spec.categories().isEmpty()) {
                sb.append(" §8[");
                boolean first = true;
                for (ShieldCategory c : spec.categories()) {
                    sb.append(first ? "" : "|").append(c.key());
                    first = false;
                }
                sb.append(']');
            }
            tooltip.add(Component.literal(sb.toString()));
        }
    }
}
