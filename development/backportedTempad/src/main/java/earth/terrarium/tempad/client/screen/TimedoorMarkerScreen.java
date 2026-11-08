// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/client/screen/TimedoorMarkerScreen.kt.
// Translated to Java for Forge 1.19.2 on the client/ui widget kit (TP-D4): Resourceful Lib's BaseCursorScreen ->
// client/ui/KitScreen; the Olympus column (gap 4), FrameLayout and rows are placed by hand with the same sizes; the
// Olympus widgets keep their default Olympus look (text box, colour picker, switch).
package earth.terrarium.tempad.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import earth.terrarium.tempad.client.TempadUI;
import earth.terrarium.tempad.client.screen.tempad.NewLocationScreen;
import earth.terrarium.tempad.client.state.MutableState;
import earth.terrarium.tempad.client.ui.Button;
import earth.terrarium.tempad.client.ui.ColorPicker;
import earth.terrarium.tempad.client.ui.KitScreen;
import earth.terrarium.tempad.client.ui.Label;
import earth.terrarium.tempad.client.ui.Renderers;
import earth.terrarium.tempad.client.ui.Sprites;
import earth.terrarium.tempad.client.ui.TextBox;
import earth.terrarium.tempad.client.ui.WidgetRenderer;
import earth.terrarium.tempad.client.ui.WidgetSprites;
import earth.terrarium.tempad.common.network.c2s.UpdateAnchorPacket;
import earth.terrarium.tempad.common.registries.ModNetworking;
import earth.terrarium.tempad.common.utils.Color;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TimedoorMarkerScreen extends KitScreen {
    static final Component publicAccess = Component.translatable("screen.tempad.marker.public_access");

    /** Olympus UIConstants.MODAL / MODAL_HEADER, SWITCH_ON / SWITCH (Widgets.toggle). */
    private static final ResourceLocation MODAL = Button.olympus("modal/modal");
    private static final ResourceLocation MODAL_HEADER = Button.olympus("modal/modal_header");
    private static final WidgetSprites SWITCH_ON = new WidgetSprites(Button.olympus("buttons/switch/on/normal"), Button.olympus("buttons/switch/on/disabled"), Button.olympus("buttons/switch/on/hovered"));
    private static final WidgetSprites SWITCH = new WidgetSprites(Button.olympus("buttons/switch/off/normal"), Button.olympus("buttons/switch/off/disabled"), Button.olympus("buttons/switch/off/hovered"));

    /** Olympus MinecraftColors. */
    static final int GRAY = 0xAAAAAA;
    static final int DARK_GRAY = 0x555555;
    static final int WHITE = 0xFFFFFF;
    static final int GOLD = 0xFFAA00;
    static final int RED = 0xFF5555;

    public final BlockPos pos;
    public final MutableState<String> name;
    public final MutableState<Color> color;
    public final MutableState<Boolean> booleanAccess;
    public final MutableState<Boolean> locked;

    protected int bgHeight = 0;
    protected int bgWidth = 150;

    public TimedoorMarkerScreen(BlockPos pos, String name, Color color, boolean access, boolean locked) {
        super(Component.translatable("screen.tempad.marker"));
        this.pos = pos;
        this.name = MutableState.of(name);
        this.color = MutableState.of(color);
        this.booleanAccess = MutableState.of(access);
        this.locked = MutableState.of(locked);
    }

    /** TextWidget.configure(): grey with a shadow. */
    protected Label text(Component text) {
        return new Label(text, font).setColor(GRAY);
    }

    /** Places a column child at (x, y) and registers it; returns its height. */
    protected int place(AbstractWidget widget, int x, int y) {
        widget.x = x;
        widget.y = y;
        addRenderableWidget(widget);
        return widget.getHeight();
    }

    @Override
    protected void init() {
        super.init();

        // The column is built first so its height is known, then placed at the centred background (as upstream).
        Label title = new Label(this.title, font).setColor(WHITE);
        Button lock = new Button().withTexture(null)
                .withRenderer(locked.withRenderer(isLocked -> Renderers.withColors(
                        Renderers.icon(isLocked ? TempadUI.lockIcon : TempadUI.unlockIcon).withShadow(), DARK_GRAY, WHITE, GOLD).withCentered(7, 7)))
                .withCallback(() -> locked.set(!locked.getValue()));
        lock.withSize(7);
        Button close = new Button().withTexture(null)
                .withRenderer(Renderers.withColors(Renderers.icon(TempadUI.xIcon).withShadow(), DARK_GRAY, WHITE, RED).withCentered(7, 7))
                .withCallback(this::onClose);
        close.withSize(7);

        Label nameLabel = text(NewLocationScreen.NAME_FIELD);
        TextBox nameInput = new TextBox(name).withSize(100, 20);
        Label colorLabel = text(NewLocationScreen.COLOR_FIELD);
        Button picker = ColorPicker.button(color, false, it -> it.withSize(20, 20), overlay -> {
        });
        TextBox colorInput = ColorPicker.input(color, it -> it.withSize(80, 20));

        int accessHeight = accessFieldHeight();
        // Column (gap 4): frame 10, spacer 0, name label 9, name input 20, colour label 9, colour row 20, access field.
        int columnHeight = 10 + 4 + 0 + 4 + 9 + 4 + 20 + 4 + 9 + 4 + 20 + 4 + accessHeight;
        bgHeight = columnHeight + 12;
        int left = (width - bgWidth) / 2 + 5;
        int y = (height - bgHeight) / 2 + 5;

        // FrameLayout(bgWidth - 10, 10): title top left, [lock, x] (gap 2) top right.
        place(title, left, y);
        int rowRight = left + bgWidth - 10;
        place(lock, rowRight - 7 - 2 - 7, y);
        place(close, rowRight - 7, y);
        y += 10 + 4 + 0 + 4;

        y += place(nameLabel, left, y) + 4;
        y += place(nameInput, left, y) + 4;
        y += place(colorLabel, left, y) + 4;
        place(picker, left, y);
        place(colorInput, left + 20, y);
        y += 20 + 4;

        setupAccessField(left, y);
    }

    /** Height of the widgets {@link #setupAccessField} adds (the column needs it before placing). */
    protected int accessFieldHeight() {
        return 12;
    }

    /** Upstream setupAccessField: a row of the "public access" label (padding right 4) and a 22 x 12 switch, centred vertically. */
    protected void setupAccessField(int x, int y) {
        Label label = text(publicAccess);
        place(label, x, y + (12 - label.getHeight()) / 2);
        WidgetRenderer on = Renderers.sprite(SWITCH_ON);
        WidgetRenderer off = Renderers.sprite(SWITCH);
        Button toggle = new Button().withTexture(null)
                .withRenderer((pose, ctx, pt) -> (booleanAccess.get() ? on : off).render(pose, ctx, pt))
                .withCallback(() -> booleanAccess.set(!booleanAccess.get()));
        toggle.withSize(22, 12);
        place(toggle, x + label.getWidth() + 4, y);
    }

    @Override
    public void renderBackground(PoseStack pose) {
        super.renderBackground(pose);
        RenderSystem.enableBlend();
        Sprites.blit(pose, MODAL, (width - bgWidth) / 2, (height - bgHeight) / 2, bgWidth, bgHeight);
        Sprites.blit(pose, MODAL_HEADER, (width - bgWidth) / 2 + 1, (height - bgHeight) / 2 + 1, bgWidth - 2, 15);
    }

    @Override
    public void onClose() {
        super.onClose();
        sync();
    }

    protected void sync() {
        ModNetworking.sendToServer(new UpdateAnchorPacket(pos, color.getValue(), name.getValue(), booleanAccess.get(), locked.get()));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
