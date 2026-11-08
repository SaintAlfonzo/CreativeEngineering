// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/client/TempadUI.kt. Translated to Java for Forge 1.19.2.
package earth.terrarium.tempad.client;

import com.mojang.blaze3d.vertex.PoseStack;
import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.client.ui.Button;
import earth.terrarium.tempad.client.ui.Ctx;
import earth.terrarium.tempad.client.ui.Dropdown;
import earth.terrarium.tempad.client.ui.Gfx;
import earth.terrarium.tempad.client.ui.Renderers;
import earth.terrarium.tempad.client.ui.ScrollList;
import earth.terrarium.tempad.client.ui.Sprites;
import earth.terrarium.tempad.client.ui.State;
import earth.terrarium.tempad.client.ui.TextBox;
import earth.terrarium.tempad.client.ui.WidgetRenderer;
import earth.terrarium.tempad.client.ui.WidgetSprites;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public final class TempadUI {
    public static final WidgetSprites button = new WidgetSprites(Tempad.id("button/normal"), Tempad.id("button/disabled"), Tempad.id("button/hover"));
    public static final WidgetSprites steelButton = new WidgetSprites(Tempad.id("button/steel/normal"), Tempad.id("button/steel/hover"));
    public static final WidgetSprites element = new WidgetSprites(Tempad.id("element/normal"), Tempad.id("element/disabled"), Tempad.id("element/hover"));
    public static final ResourceLocation modal = Tempad.id("list/modal");
    public static final WidgetSprites toggleEnabled = new WidgetSprites(Tempad.id("toggle/enabled/normal"), Tempad.id("toggle/enabled/disabled"), Tempad.id("toggle/enabled/hover"));
    public static final WidgetSprites toggleDisabled = new WidgetSprites(Tempad.id("toggle/disabled/normal"), Tempad.id("toggle/disabled/disabled"), Tempad.id("toggle/disabled/hover"));
    public static final ResourceLocation powerBg = Tempad.id("power/background");
    public static final ResourceLocation powerBar = Tempad.id("power/overlay");
    public static final ResourceLocation powerVert = Tempad.id("power/overlay_vertical");
    public static final ResourceLocation lockIcon = Tempad.id("icons/mini/lock");
    public static final ResourceLocation unlockIcon = Tempad.id("icons/mini/unlock");
    public static final ResourceLocation xIcon = Tempad.id("icons/mini/x");

    private TempadUI() {
    }

    public static final ScrollList.ScrollbarRenderer scrollbarYRenderer = (PoseStack pose, ScrollList widget, Ctx ctx, float pt) -> {
        int scrollHeight = Math.max(0, Math.min(ctx.height,
                (int) (ctx.height * ((float) ctx.height / widget.getContentHeight())) + (widget.getViewHeight() - ctx.height)));
        int scrollY = (int) ((float) (widget.getYScroll() + widget.getOverscrollY()) / widget.getContentHeight() * ctx.height);
        Gfx.fill(pose, ctx.x - 2, ctx.y - 2, ctx.x + ctx.width + 2, ctx.y + widget.getHeight() - 2, 0x4aff6f00);
        Gfx.fill(pose, ctx.x + 2, ctx.y, ctx.x + ctx.width - 2, ctx.y + widget.getHeight() - 6, Tempad.ORANGE.getValue());
        Sprites.blit(pose, Tempad.id("button/normal"), ctx.x, ctx.y + scrollY, ctx.width, scrollHeight);
    };

    /** colored(): DARK_ORANGE when inactive, ORANGE, HIGHLIGHTED_ORANGE when hovered. */
    public static WidgetRenderer colored(Renderers.Colorable renderer) {
        return Renderers.withColors(renderer, Tempad.DARK_ORANGE.getValue(), Tempad.ORANGE.getValue(), Tempad.HIGHLIGHTED_ORANGE.getValue());
    }

    public static WidgetRenderer selectableColored(Renderers.Colorable renderer, BooleanSupplier selected) {
        return (pose, ctx, pt) -> {
            if (selected.getAsBoolean()) {
                renderer.withColor(Gfx.BLACK);
                renderer.render(pose, ctx, pt);
            } else {
                colored(renderer).render(pose, ctx, pt);
            }
        };
    }

    /** toggle(state): a switch drawn with the toggle sprites; a click flips the state (Olympus StateUtils.booleanToggle). */
    public static Button toggle(State<Boolean> state) {
        WidgetRenderer onRenderer = Renderers.sprite(toggleEnabled);
        WidgetRenderer offRenderer = Renderers.sprite(toggleDisabled);
        return new Button().withCallback(() -> state.set(!state.get())).withTexture(null)
                .withRenderer((pose, ctx, pt) -> (state.get() ? onRenderer : offRenderer).render(pose, ctx, pt));
    }

    /** intInput(state): Olympus Widgets.intInput (a doubleInput over the int state) in the Tempad style. */
    public static TextBox intInput(State<Integer> state, Consumer<TextBox> modify) {
        return numberInput(state.map(Integer::doubleValue, Double::intValue), modify);
    }

    /** floatInput(state): Olympus Widgets.doubleInput over the float state in the Tempad style. */
    public static TextBox floatInput(State<Float> state, Consumer<TextBox> modify) {
        return numberInput(state.map(Float::doubleValue, Double::floatValue), modify);
    }

    /**
     * Fix (TODO gameplay/tnt-and-portal #9 item 5): the start value is formatted with a fixed locale (dot decimal, no
     * grouping), the same format Double.parseDouble and the input filter accept. Upstream uses the system locale, so
     * under a comma-decimal locale "-2,5" is shown and every edit of it is refused.
     */
    static String formatNumber(double value) {
        NumberFormat format = NumberFormat.getInstance(Locale.ROOT);
        format.setGroupingUsed(false);
        format.setMaximumFractionDigits(3);
        return format.format(value);
    }

    /**
     * Olympus 1.0.18 Widgets.doubleInput: the box keeps the typed text; a parsable value is written through, an empty
     * box or a lone "-" writes 0; edits that would not parse are refused.
     */
    private static TextBox numberInput(State<Double> state, Consumer<TextBox> modify) {
        TextBox box = new TextBox(new State<>() {
            private String temp = formatNumber(state.get());

            @Override
            public void set(String value) {
                value = value.trim();
                if (!value.isEmpty() && !value.equals("-")) {
                    try {
                        state.set(Double.parseDouble(value));
                        temp = value;
                    } catch (NumberFormatException e) {
                        state.set(0.0);
                        temp = "";
                    }
                } else {
                    state.set(0.0);
                    temp = value;
                }
            }

            @Override
            public String get() {
                return temp;
            }
        });
        box.setFilter(s -> {
            if (s.isEmpty() || s.equals("-")) return true;
            try {
                Double.parseDouble(s);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        });
        box.withTexture(element);
        box.withTextColor(Tempad.ORANGE.getValue());
        modify.accept(box);
        return box;
    }

    /** DropdownBuilder.style(converter). */
    public static <T> void style(Dropdown<T> dropdown, Function<T, Component> converter) {
        dropdown.withTexture(modal);
        dropdown.withEntryHeight(14);
        dropdown.withEntrySprites(null);
        dropdown.withEntryRenderer(entry -> colored(Renderers.text(converter.apply(entry)).withLeftAlignment()).withPadding(0, 3));
    }

    // borrowed from stack overflow (upstream comment): https://stackoverflow.com/questions/4753251
    private static final String[] c = {"kC", "mC", "bC", "tC"};

    private static String coolFormat(double n, int iteration) {
        if (iteration == 0 && n < 1000) return (int) n + "C";
        double d = ((long) n / 100) / 10.0;
        boolean isRound = (d * 10) % 10 == 0.0;
        if (d < 1000) {
            String number = (d > 99.9 || isRound || d > 9.99) ? String.valueOf((int) d * 10 / 10) : d + "";
            return number + c[iteration];
        }
        return coolFormat(d, iteration + 1);
    }

    public static void renderEnergyBar(PoseStack pose, Font font, int x, int y, int power, int maxPower) {
        Sprites.blit(pose, powerBg, x, y, 74, 13);
        int uWidth = maxPower == 0 ? 0 : Math.round(Math.max(0f, Math.min(1f, (float) power / maxPower)) * 72);
        Sprites.blit(pose, powerBar, 72, 11, 0, 0, x + 1, y + 1, uWidth, 11);
        Component text;
        if (power == -1) {
            text = Component.translatable("item.tempad.creative_chronometer.infinite");
        } else if (Screen.hasShiftDown() || (Minecraft.getInstance().player != null && Minecraft.getInstance().player.isShiftKeyDown())) {
            text = Component.literal(NumberFormat.getInstance().format(power) + "C");
        } else {
            text = Component.literal(coolFormat(power, 0) + "/" + coolFormat(maxPower, 0));
        }
        int xOffset = (74 - font.width(text)) / 2;
        Gfx.enableScissor(x + 1, y + 1, x + 1 + uWidth, y + 12);
        font.draw(pose, text, x + xOffset, y + 3, 0xFF000000);
        Gfx.disableScissor();
        Gfx.enableScissor(x + 1 + uWidth, y + 1, x + 73, y + 12);
        Integer gold = ChatFormatting.GOLD.getColor();
        font.draw(pose, text, x + xOffset, y + 3, gold == null ? 0 : gold);
        Gfx.disableScissor();
    }
}
