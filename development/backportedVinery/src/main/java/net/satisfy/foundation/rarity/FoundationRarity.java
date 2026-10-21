package net.satisfy.foundation.rarity;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.ArrayList;
import java.util.List;

/**
 * Item rarity that isn't bound to the vanilla enum. A rarity is either one solid color, a gradient over the
 * name, or an animated gradient that slowly scrolls through its colors.
 */
public final class FoundationRarity {
    private static final float ANIMATION_PERIOD_MS = 8000F;

    public static final FoundationRarity COMMON = solid("common", 0xAAAAAA);
    public static final FoundationRarity UNCOMMON = solid("uncommon", 0x78C878);
    public static final FoundationRarity RARE = solid("rare", 0x6EA8D8);
    public static final FoundationRarity EPIC = solid("epic", 0xA77BCB);
    public static final FoundationRarity MYTHIC = gradient("mythic", 0xFFD166, 0xE58B3A);
    public static final FoundationRarity LEGENDARY = gradient("legendary", 0xDFAE45, 0xFFF0A6, 0xFFF8DC, 0xFFF0A6, 0xDFAE45);
    public static final FoundationRarity CELESTIAL = gradient("celestial", 0x72C9E8, 0xFFFFFF, 0xB99AFF);
    public static final FoundationRarity DIVINE = gradient("divine", 0xFFF8E1, 0xFFD166, 0xE9A4C6, 0xFFF8E1);
    public static final FoundationRarity ETERNAL = animated("eternal", 0xFFB3B3, 0xFFE29A, 0xB8F2C8, 0xA9D8FF, 0xD6B3FF);

    private final String id;
    private final int[] colors;
    private final boolean animated;

    private FoundationRarity(String id, boolean animated, int... colors) {
        if (colors.length == 0) {
            throw new IllegalArgumentException("rarity needs at least one color");
        }
        this.id = id;
        this.animated = animated;
        this.colors = colors;
    }

    public static FoundationRarity solid(String id, int color) {
        return new FoundationRarity(id, false, color);
    }

    /** Colors are spread evenly over the name from first to last. */
    public static FoundationRarity gradient(String id, int... colors) {
        return new FoundationRarity(id, false, colors);
    }

    /** Colors loop around the name and scroll over time. */
    public static FoundationRarity animated(String id, int... colors) {
        return new FoundationRarity(id, true, colors);
    }

    public String id() {
        return id;
    }

    public boolean isAnimated() {
        return animated;
    }

    /** Label key, {@code rarity.<namespace>.<path>}; ids without a namespace belong to foundation. */
    public String translationKey() {
        int colon = id.indexOf(':');
        return colon < 0 ? "rarity.foundation." + id : "rarity." + id.substring(0, colon) + "." + id.substring(colon + 1);
    }

    /** First color, used when only one color fits, e.g. for borders or labels. */
    public int baseColor() {
        return colors[0];
    }

    /** Color at {@code t} in [0, 1) along the gradient, looping when animated. */
    public int colorAt(float t) {
        if (colors.length == 1) {
            return colors[0];
        }
        float pos;
        if (animated) {
            pos = ((t % 1F) + 1F) % 1F * colors.length;
            int from = (int) pos % colors.length;
            return lerp(colors[from], colors[(from + 1) % colors.length], pos - (int) pos);
        }
        pos = Math.max(0F, Math.min(1F, t)) * (colors.length - 1);
        int from = Math.min((int) pos, colors.length - 2);
        return lerp(colors[from], colors[from + 1], pos - from);
    }

    /** Re-colors {@code text} letter by letter, keeping bold/italic/etc. of the original. */
    public MutableComponent apply(Component text) {
        if (colors.length == 1) {
            return text.copy().withStyle(style -> style.withColor(TextColor.fromRgb(colors[0])));
        }
        List<Character> chars = new ArrayList<>();
        List<Style> styles = new ArrayList<>();
        text.visit((style, content) -> {
            for (int i = 0; i < content.length(); i++) {
                char c = content.charAt(i);
                if (c == '§') {
                    i++;
                    continue;
                }
                chars.add(c);
                styles.add(style);
            }
            return java.util.Optional.empty();
        }, Style.EMPTY);

        int length = chars.size();
        float shift = animated ? (Util.getMillis() % (long) ANIMATION_PERIOD_MS) / ANIMATION_PERIOD_MS : 0F;
        MutableComponent result = Component.empty();
        for (int i = 0; i < length; i++) {
            float t = length <= 1 ? 0F : (float) i / (animated ? length : length - 1);
            int color = colorAt(animated ? t - shift : t);
            result.append(Component.literal(String.valueOf(chars.get(i))).withStyle(styles.get(i).withColor(TextColor.fromRgb(color))));
        }
        return result;
    }

    private static int lerp(int a, int b, float t) {
        int r = (int) (((a >> 16) & 0xFF) + ((((b >> 16) & 0xFF) - ((a >> 16) & 0xFF)) * t));
        int g = (int) (((a >> 8) & 0xFF) + ((((b >> 8) & 0xFF) - ((a >> 8) & 0xFF)) * t));
        int bl = (int) ((a & 0xFF) + (((b & 0xFF) - (a & 0xFF)) * t));
        return (r << 16) | (g << 8) | bl;
    }
}
