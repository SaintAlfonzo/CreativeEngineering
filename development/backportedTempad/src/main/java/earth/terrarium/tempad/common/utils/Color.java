// Port glue (ours, GPLv3): stands in for com.teamresourceful.resourcefullib.common.color.Color, which upstream
// Tempad 3.0.4 uses everywhere (Resourceful Lib 1.21 is not available on 1.19.2, TP-D4).
package earth.terrarium.tempad.common.utils;

import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

/**
 * An ARGB colour with the two special values upstream relies on: {@link #DEFAULT} (white, "no colour chosen") and
 * {@link #RAINBOW} (animated on the client; {@link #getValue()} returns the current rainbow colour set by
 * {@link #setRainbowValue(int)}, which the client side updates every frame or tick).
 */
public final class Color {
    public static final Color DEFAULT = new Color(0xFFFFFFFF, true, false);
    public static final Color RAINBOW = new Color(0xFFFF0000, false, true);

    private static volatile int rainbowValue = 0xFFFF0000;

    private final int value;
    private final boolean isDefault;
    private final boolean isRainbow;

    private Color(int value, boolean isDefault, boolean isRainbow) {
        this.value = value;
        this.isDefault = isDefault;
        this.isRainbow = isRainbow;
    }

    public Color(int argb) {
        this(argb, false, false);
    }

    public Color(int r, int g, int b, int a) {
        this(((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF));
    }

    /** Client hook: the current rainbow colour (set every client tick by TempadClient, as Resourceful Lib does). */
    public static void setRainbowValue(int argb) {
        rainbowValue = argb;
    }

    public int getValue() {
        return isRainbow ? rainbowValue : value;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public boolean isRainbow() {
        return isRainbow;
    }

    public int getIntRed() {
        return (getValue() >> 16) & 0xFF;
    }

    public int getIntGreen() {
        return (getValue() >> 8) & 0xFF;
    }

    public int getIntBlue() {
        return getValue() & 0xFF;
    }

    public int getIntAlpha() {
        return (getValue() >>> 24) & 0xFF;
    }

    public Color withAlpha(int alpha) {
        return new Color(getIntRed(), getIntGreen(), getIntBlue(), alpha);
    }

    public Color darken(float factor) {
        return new Color((int) (getIntRed() * factor), (int) (getIntGreen() * factor), (int) (getIntBlue() * factor), getIntAlpha());
    }

    // NBT: an int for a plain colour, the strings "default" / "rainbow" for the special values.
    public Tag save() {
        if (isDefault) return StringTag.valueOf("default");
        if (isRainbow) return StringTag.valueOf("rainbow");
        return IntTag.valueOf(value);
    }

    public static Color load(Tag tag) {
        if (tag instanceof StringTag s) {
            return switch (s.getAsString()) {
                case "rainbow" -> RAINBOW;
                case "default" -> DEFAULT;
                default -> parseHex(s.getAsString());
            };
        }
        if (tag instanceof IntTag i) return new Color(i.getAsInt());
        return DEFAULT;
    }

    /**
     * Resourceful Lib's Color.parse as the colour text box uses it: "rainbow", "default", a colour name, or hex
     * ("#RRGGBB", "#AARRGGBB", "0x..."); anything else is {@link #DEFAULT}.
     */
    public static Color parse(String text) {
        String t = text.trim();
        if (t.equalsIgnoreCase("rainbow")) return RAINBOW;
        if (t.equalsIgnoreCase("default")) return DEFAULT;
        net.minecraft.ChatFormatting named = net.minecraft.ChatFormatting.getByName(t.toLowerCase(java.util.Locale.ROOT));
        if (named != null && named.getColor() != null) return new Color(0xFF000000 | named.getColor());
        if (t.startsWith("0x") || t.startsWith("0X")) t = t.substring(2);
        return parseHex(t);
    }

    /** "#RRGGBB", what the colour text box shows. */
    @Override
    public String toString() {
        if (isDefault) return "default";
        if (isRainbow) return "rainbow";
        return String.format("#%06X", value & 0xFFFFFF);
    }

    private static Color parseHex(String text) {
        try {
            String hex = text.startsWith("#") ? text.substring(1) : text;
            long parsed = Long.parseLong(hex, 16);
            return new Color(hex.length() <= 6 ? (int) (parsed | 0xFF000000L) : (int) parsed);
        } catch (NumberFormatException e) {
            return DEFAULT;
        }
    }

    // Network: upstream COLOR_BYTE_CODEC (byte 0 = default, 1 = rainbow, 2 = int follows).
    public void write(FriendlyByteBuf buf) {
        if (isDefault) {
            buf.writeByte(0);
        } else if (isRainbow) {
            buf.writeByte(1);
        } else {
            buf.writeByte(2);
            buf.writeInt(value);
        }
    }

    public static Color read(FriendlyByteBuf buf) {
        byte kind = buf.readByte();
        return switch (kind) {
            case 0 -> DEFAULT;
            case 1 -> RAINBOW;
            default -> new Color(buf.readInt());
        };
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Color c && c.value == value && c.isDefault == isDefault && c.isRainbow == isRainbow;
    }

    @Override
    public int hashCode() {
        return value * 31 + (isDefault ? 1 : 0) + (isRainbow ? 2 : 0);
    }
}
