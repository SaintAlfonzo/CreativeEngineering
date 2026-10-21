package com.SaintAlfonzo.creative_labels.item;

import com.SaintAlfonzo.creative_labels.block.NoteData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Note colours (sticky_notes.md 3.6, BRIEF 5): leather-style dye mixing and the four special looks. Pure maths, used
 * by both sides; the special ingredients are looked up by id when asked, never at class init.
 */
public final class NoteColours {

    /** Sticky It's plain paper, used for a plain note's tooltip text colour. */
    public static final int PLAIN_YELLOW = 0xF2E26B;

    /**
     * A special look: its NBT name, the item-model {@code creative_labels:look} value (2..5) and the crafting
     * ingredient (plain notes + 1 of it).
     */
    public record Special(String name, int look, ResourceLocation ingredient) {
        public String nameKey() {
            return "creative_labels.special." + name;
        }

        /** The ingredient item (registry lookup at call time). */
        @Nullable
        public Item ingredientItem() {
            return ForgeRegistries.ITEMS.getValue(ingredient);
        }
    }

    public static final Special INVERTED = new Special("inverted", 2, new ResourceLocation("minecraft", "ink_sac"));
    public static final Special SMILE = new Special("smile", 3, new ResourceLocation("minecraft", "glow_berries"));
    public static final Special CREEPER = new Special("creeper", 4, new ResourceLocation("minecraft", "gunpowder"));
    public static final Special GOLD = new Special("gold", 5, new ResourceLocation("minecraft", "gold_nugget"));
    public static final List<Special> SPECIALS = List.of(INVERTED, SMILE, CREEPER, GOLD);

    /** Item-model look values: 0 plain, 1 dyed, 2..5 the specials (in {@link #SPECIALS} order). */
    public static final int LOOK_PLAIN = 0, LOOK_DYED = 1;

    private NoteColours() {}

    @Nullable
    public static Special special(@Nullable String name) {
        if (name == null) return null;
        for (Special s : SPECIALS) if (s.name.equals(name)) return s;
        return null;
    }

    public static boolean isSpecial(@Nullable String name) {
        return special(name) != null;
    }

    /** The special an ingredient item makes, or null. */
    @Nullable
    public static Special specialFor(Item ingredient) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(ingredient);
        if (id == null) return null;
        for (Special s : SPECIALS) if (s.ingredient.equals(id)) return s;
        return null;
    }

    /** The item-model look value of a note with this colour and special. */
    public static int look(int color, @Nullable String special) {
        Special s = special(special);
        if (s != null) return s.look;
        return color == NoteData.NO_COLOR ? LOOK_PLAIN : LOOK_DYED;
    }

    /**
     * Vanilla's {@code DyeableLeatherItem.dyeArmor} maths: the old colour (if any) and each dye are averaged per
     * channel, then scaled so the result is as bright as the brightest input. {@code oldColor} =
     * {@link NoteData#NO_COLOR} for an undyed note.
     */
    public static int mix(int oldColor, DyeColor... dyes) {
        return mix(oldColor, List.of(dyes));
    }

    public static int mix(int oldColor, List<DyeColor> dyes) {
        int[] sum = new int[3];
        int maxSum = 0;
        int n = 0;
        if (oldColor != NoteData.NO_COLOR) {
            float r = (float) (oldColor >> 16 & 255) / 255.0F;
            float g = (float) (oldColor >> 8 & 255) / 255.0F;
            float b = (float) (oldColor & 255) / 255.0F;
            maxSum += (int) (Math.max(r, Math.max(g, b)) * 255.0F);
            sum[0] += (int) (r * 255.0F);
            sum[1] += (int) (g * 255.0F);
            sum[2] += (int) (b * 255.0F);
            n++;
        }
        for (DyeColor dye : dyes) {
            float[] c = dye.getTextureDiffuseColors();
            int r = (int) (c[0] * 255.0F);
            int g = (int) (c[1] * 255.0F);
            int b = (int) (c[2] * 255.0F);
            maxSum += Math.max(r, Math.max(g, b));
            sum[0] += r;
            sum[1] += g;
            sum[2] += b;
            n++;
        }
        if (n == 0) return oldColor;
        int r = sum[0] / n;
        int g = sum[1] / n;
        int b = sum[2] / n;
        float avgMax = (float) maxSum / (float) n;
        float curMax = (float) Math.max(r, Math.max(g, b));
        r = (int) ((float) r * avgMax / curMax);
        g = (int) ((float) g * avgMax / curMax);
        b = (int) ((float) b * avgMax / curMax);
        return (r << 16) | (g << 8) | b;
    }

    /** The single-dye colour of a DyeColor (what one dye on a plain note gives). */
    public static int of(DyeColor dye) {
        return mix(NoteData.NO_COLOR, dye);
    }

    /** The DyeColor whose single-dye colour is nearest (redmean distance) to an RGB; for names in WTHIT. */
    public static DyeColor nearestDye(int rgb) {
        DyeColor best = DyeColor.WHITE;
        double bestDist = Double.MAX_VALUE;
        for (DyeColor dye : DyeColor.values()) {
            double d = distance(rgb, of(dye));
            if (d < bestDist) {
                bestDist = d;
                best = dye;
            }
        }
        return best;
    }

    /** Redmean colour distance. */
    public static double distance(int a, int b) {
        int r1 = a >> 16 & 255, g1 = a >> 8 & 255, b1 = a & 255;
        int r2 = b >> 16 & 255, g2 = b >> 8 & 255, b2 = b & 255;
        double rm = (r1 + r2) / 2.0;
        int dr = r1 - r2, dg = g1 - g2, db = b1 - b2;
        return Math.sqrt((2 + rm / 256) * dr * dr + 4 * dg * dg + (2 + (255 - rm) / 256) * db * db);
    }

    public static String hex(int rgb) {
        return String.format("#%06X", rgb & 0xFFFFFF);
    }

    /** Relative luminance 0..1 of an RGB (for greyscale bases and the icon dye ramp). */
    public static float luminance(int rgb) {
        return (0.2126f * (rgb >> 16 & 255) + 0.7152f * (rgb >> 8 & 255) + 0.0722f * (rgb & 255)) / 255f;
    }
}
