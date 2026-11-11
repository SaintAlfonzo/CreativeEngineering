package com.SaintAlfonzo.creativelabels.client;

import com.SaintAlfonzo.creativelabels.item.NoteColours;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;

/**
 * The dyed-icon ramp (sticky_notes.md 3.11, BRIEF 4 "IconDye ramp"): every opaque pixel of a rendered item icon is
 * replaced by a colour between the dye's dark and light tones chosen by the pixel's luminance, so the icon keeps
 * its shading but reads in one colour. Light = {@link DyeColor#getFireworkColor()}, dark =
 * {@link DyeColor#getTextColor()}; when the text colour is not meaningfully darker than the firework colour (white,
 * and any dye where the two are equal) the dark end is the light one scaled by {@link #DARKEN}.
 *
 * <p>The pixel maths works on {@link NativeImage}'s packed ABGR ints (R in the low byte) through plain bit ops, so
 * {@link #dyePixel} and {@link #ramp} are testable without LWJGL; {@link #apply} is the loop over a real image.
 * Our own code, not Labels' palette classes.
 */
public final class IconDye {

    /** Dark end = light * DARKEN when the dye's text colour gives no usable dark tone. */
    public static final float DARKEN = 0.6f;
    /** The text colour counts as "dark" only when its luminance is under this share of the light tone's. */
    public static final float DARK_RATIO = 0.85f;

    private IconDye() {}

    /** The light end of the ramp: the dye's firework colour (RGB). */
    public static int light(DyeColor dye) {
        return dye.getFireworkColor() & 0xFFFFFF;
    }

    /** The dark end of the ramp: the dye's text colour when it is darker, else the light end scaled by {@link #DARKEN}. */
    public static int dark(DyeColor dye) {
        int light = light(dye);
        int text = dye.getTextColor() & 0xFFFFFF;
        if (text != light && NoteColours.luminance(text) < NoteColours.luminance(light) * DARK_RATIO) return text;
        return scale(light, DARKEN);
    }

    /** RGB scaled per channel. */
    public static int scale(int rgb, float f) {
        int r = Mth.clamp(Math.round((rgb >> 16 & 0xFF) * f), 0, 255);
        int g = Mth.clamp(Math.round((rgb >> 8 & 0xFF) * f), 0, 255);
        int b = Mth.clamp(Math.round((rgb & 0xFF) * f), 0, 255);
        return r << 16 | g << 8 | b;
    }

    /** The ramp colour for an RGB pixel: lerp(dark, light) by the pixel's luminance (0 = dark, 1 = light). */
    public static int ramp(int rgb, int dark, int light) {
        float t = Mth.clamp(NoteColours.luminance(rgb), 0f, 1f);
        int r = Math.round(Mth.lerp(t, dark >> 16 & 0xFF, light >> 16 & 0xFF));
        int g = Math.round(Mth.lerp(t, dark >> 8 & 0xFF, light >> 8 & 0xFF));
        int b = Math.round(Mth.lerp(t, dark & 0xFF, light & 0xFF));
        return r << 16 | g << 8 | b;
    }

    /** One packed ABGR pixel (NativeImage layout: A in the high byte, R in the low byte) through the ramp; alpha kept. */
    public static int dyePixel(int abgr, int dark, int light) {
        int a = abgr >>> 24;
        if (a == 0) return abgr;
        int r = abgr & 0xFF, g = abgr >> 8 & 0xFF, b = abgr >> 16 & 0xFF;
        int out = ramp(r << 16 | g << 8 | b, dark, light);
        return a << 24 | (out & 0xFF) << 16 | (out >> 8 & 0xFF) << 8 | (out >> 16 & 0xFF);
    }

    /** Dyes every pixel of the image in place (render thread, after {@code download()}, before {@code upload()}). */
    public static void apply(NativeImage image, DyeColor dye) {
        int dark = dark(dye), light = light(dye);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int p = image.getPixelRGBA(x, y);
                int q = dyePixel(p, dark, light);
                if (q != p) image.setPixelRGBA(x, y, q);
            }
        }
    }
}
