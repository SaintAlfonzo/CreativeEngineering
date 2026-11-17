package com.SaintAlfonzo.creativecompat.surface;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

/**
 * Which pass-through lines the WTHIT overlay shows for one block, and their text (plan_passthrough.md 6). Built from the
 * same {@link PassRule.Cell} movement uses ({@link PassThrough#cellAt}), so the overlay and the walk always agree;
 * {@link WthitRunSpeed} turns each {@link Line} into a translated component.
 *
 * <ul>
 *   <li>{@code Walk Through: 0.40× (Dense neighbors) on Farmland (Tilled)}: the block's walk value times the soil's
 *       speed; the soil is named only when its speed is not 1.00; "(Dense neighbors)" only when density lowered the
 *       walk value; vines add "while moving through", layer height blocks "3 of 8 layers". Hidden at 1.00 with
 *       {@code hideNormal}.</li>
 *   <li>{@code Fall Through: 0.20× (Dense neighbors)}, or {@code Fall Through: stopped} in a surrounded web. Hidden at
 *       1.00 with {@code hideNormal}.</li>
 *   <li>{@code Neighbors: 3 of 4} for tiers that count neighbours (of the shape's 4 side or 6 face cells).</li>
 * </ul>
 *
 * <p>Plain Java (no game classes) so the headless harness can test it.
 */
public final class PassThroughFormat {

    private PassThroughFormat() {}

    private static final double EPSILON = 1.0E-6;

    public enum Kind { WALK, FALL, NEIGHBOURS }

    /**
     * One overlay line.
     *
     * @param value       the combined walk, the fall multiplier, or the neighbour count
     * @param stopped     fall only: a surrounded web stops falling
     * @param dense       density lowered this value
     * @param whileMoving walk only: the tier slows only while moving horizontally (vines)
     * @param soil        walk only: name the soil (its speed is not 1.00)
     * @param of          neighbours: the shape's max (4 or 6); walk: the block's most layers (0 = not a layer block)
     * @param layers      walk only: the block's layers (layer height blocks)
     */
    public record Line(Kind kind, double value, boolean stopped, boolean dense, boolean whileMoving, boolean soil,
                       int of, int layers) {

        public Line(Kind kind, double value, boolean stopped, boolean dense, boolean whileMoving, boolean soil) {
            this(kind, value, stopped, dense, whileMoving, soil, 0, 0);
        }
    }

    /**
     * The lines for {@code cell} (values {@code v} of its tier) over a soil of speed {@code soil} (1.0 when there is no
     * soil or it has no speed).
     */
    public static List<Line> lines(PassRule.Cell cell, PassTier.Values v, double soil, boolean hideNormal) {
        return lines(cell, v, soil, hideNormal, 0, 0);
    }

    /** As above, for a layer height block with {@code layers} of {@code layersMax} (0, 0 for any other block). */
    public static List<Line> lines(PassRule.Cell cell, PassTier.Values v, double soil, boolean hideNormal, int layers,
                                   int layersMax) {
        List<Line> out = new ArrayList<>(3);
        double walk = cell.walk() * soil;
        if (!(hideNormal && isNormal(walk))) {
            out.add(new Line(Kind.WALK, walk, false, cell.walkDense(), v.walkOnlyMoving(), !isNormal(soil),
                    layersMax > 0 ? layersMax : 0, layersMax > 0 ? layers : 0));
        }
        if (cell.surrounded()) {
            out.add(new Line(Kind.FALL, 0.0, true, false, false, false));
        } else if (!(hideNormal && isNormal(cell.fall()))) {
            out.add(new Line(Kind.FALL, cell.fall(), false, cell.fallDense(), false, false));
        }
        if (PassRule.needsCount(v)) {
            out.add(new Line(Kind.NEIGHBOURS, cell.neighbours(), false, false, false, false, v.neighbours().max(), 0));
        }
        return out;
    }

    public static boolean isNormal(double factor) {
        return Math.abs(factor - 1.0) < EPSILON;
    }

    /** {@code 1.50×} or, with {@code percent}, {@code +50%} (also the Run Speed line's number, {@link RunSpeed#format}). */
    public static String number(double factor, boolean percent) {
        if (percent) {
            long p = Math.round((factor - 1.0) * 100.0);
            return (p > 0 ? "+" : "") + p + "%";
        }
        return String.format(Locale.ROOT, "%.2f×", factor);
    }

    /**
     * The English text of a line (en_us.json wording), for tests and docs. {@code soilName} / {@code soilTier} are the
     * soil block's and its surface tier's names (tier null = none).
     */
    public static String english(Line line, boolean percent, @Nullable String soilName, @Nullable String soilTier) {
        StringBuilder b = new StringBuilder();
        switch (line.kind()) {
            case WALK -> b.append("Walk Through: ").append(number(line.value(), percent));
            case FALL -> b.append("Fall Through: ").append(line.stopped() ? "stopped" : number(line.value(), percent));
            case NEIGHBOURS -> {
                return "Neighbors: " + (int) line.value() + " of " + line.of();
            }
        }
        List<String> notes = new ArrayList<>(2);
        if (line.dense()) notes.add("Dense neighbors");
        if (line.whileMoving()) notes.add("while moving through");
        if (line.kind() == Kind.WALK && line.of() > 0) notes.add(line.layers() + " of " + line.of() + " layers");
        if (!notes.isEmpty()) b.append(" (").append(String.join(", ", notes)).append(')');
        if (line.soil() && soilName != null) {
            b.append(" on ").append(soilName);
            if (soilTier != null) b.append(" (").append(soilTier).append(')');
        }
        return b.toString();
    }
}
