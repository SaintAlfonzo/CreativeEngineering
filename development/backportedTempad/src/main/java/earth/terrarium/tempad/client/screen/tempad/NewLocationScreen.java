// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/client/screen/tempad/NewLocationScreen.kt.
// Translated to Java for Forge 1.19.2; the vanilla FrameLayout / LinearLayout placement is worked out by hand.
package earth.terrarium.tempad.client.screen.tempad;

import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.client.TempadUI;
import earth.terrarium.tempad.client.state.AppearanceState;
import earth.terrarium.tempad.client.state.MutableState;
import earth.terrarium.tempad.client.ui.Button;
import earth.terrarium.tempad.client.ui.ColorPicker;
import earth.terrarium.tempad.client.ui.Gfx;
import earth.terrarium.tempad.client.ui.Label;
import earth.terrarium.tempad.client.ui.MapWidget;
import earth.terrarium.tempad.client.ui.Renderers;
import earth.terrarium.tempad.client.ui.TextBox;
import earth.terrarium.tempad.common.network.c2s.CreateLocationPacket;
import earth.terrarium.tempad.common.registries.ModMenus;
import earth.terrarium.tempad.common.registries.ModNetworking;
import earth.terrarium.tempad.common.utils.Color;
import net.minecraft.Util;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class NewLocationScreen extends AbstractTempadScreen<ModMenus.NewLocationMenu> {
    public static final Component NAME_FIELD = Component.translatable("app." + Tempad.MOD_ID + ".new_location.name");
    public static final Component COLOR_FIELD = Component.translatable("app." + Tempad.MOD_ID + ".new_location.color");

    public static List<Component> getEntries(Vec3i pos) {
        return List.of(Component.literal("X: " + pos.getX()), Component.literal("Y: " + pos.getY()), Component.literal("Z: " + pos.getZ()));
    }

    private final MutableState<Color> currentColor = MutableState.of(new Color(0xff6f00));
    private final AppearanceState doneBtn = new AppearanceState();
    private final MutableState<String> textState;
    private final MutableState<MapWidget.MapRenderer> mapState = MutableState.of(null);

    public NewLocationScreen(ModMenus.NewLocationMenu menu, Inventory inv, Component title) {
        super(null, menu, inv, title);
        doneBtn.setActive(false);
        textState = MutableState.of("", text -> doneBtn.setActive(!text.isBlank()));
    }

    @Override
    protected void init() {
        super.init();

        MapWidget map = new MapWidget(mapState).withTexture(TempadUI.element.get(true, false));
        map.withPosition(localLeft + 100, localTop + 20).withSize(94);
        addRenderableOnly(map);

        // FrameLayout(101, 21, 92 x 92): dimension name top left, X/Y/Z column bottom right, padding 4.
        int frameX = localLeft + 101, frameY = localTop + 21;
        if (minecraft.player != null) {
            Component dimension = Component.translatable(Util.makeDescriptionId("dimension", minecraft.player.level.dimension().location()));
            Label dimLabel = new Label(dimension, font).setColor(Tempad.ORANGE.getValue());
            dimLabel.setPosition(frameX + 4, frameY + 4);
            addRenderableWidget(dimLabel);

            List<Component> entries = getEntries(minecraft.player.blockPosition());
            int columnWidth = entries.stream().mapToInt(font::width).max().orElse(0);
            int columnHeight = entries.size() * 9 + (entries.size() - 1) * 2;
            int columnRight = frameX + 92 - 4;
            int y = frameY + 92 - 4 - columnHeight;
            for (Component entry : entries) {
                Label label = new Label(entry, font).setColor(Tempad.ORANGE.getValue());
                label.setPosition(columnRight - columnWidth + (columnWidth - label.getWidth()), y);
                addRenderableWidget(label);
                y += 9 + 2;
            }
        }

        // Vertical LinearLayout (spacing 4) centred in (4, 20, 94 x 78): colour label, [picker, hex input], name label, name input.
        int left = localLeft + 4;
        int top = localTop + 20 + (78 - (8 + 4 + 16 + 4 + 8 + 4 + 16)) / 2;
        Label colorLabel = new Label(left, top, 100, 8, COLOR_FIELD, font).setColor(Tempad.ORANGE.getValue()).alignLeft();
        addRenderableWidget(colorLabel);

        Button picker = ColorPicker.button(currentColor, false, btn -> {
            btn.withSize(16, 16);
            btn.withTexture(TempadUI.button);
            btn.withRenderer(currentColor.withRenderer(color -> Renderers.solid().withoutAlpha().withColor(color.getValue()).withPadding(2)));
        }, overlay -> {
            overlay.withBackground(TempadUI.modal);
            overlay.withInset(TempadUI.element.get(true, false));
            overlay.withEyedropperSettings(it -> {
                it.withSize(16);
                it.withTexture(TempadUI.button);
                it.withRenderer(Renderers.icon(Renderers.EYE_DROPPER).withColor(Gfx.BLACK).withCentered(12, 12));
            });
            overlay.withDropdownButtonSettings((btn, state) -> {
                btn.withSize(82, 16);
                btn.withRenderer(state.withRenderer((preset, isOpen) -> TempadUI.colored(Renderers.textWithChevron(preset.title(), isOpen))));
            });
            overlay.withDropdownSettings((dropdown, state) -> {
                dropdown.withTexture(TempadUI.modal);
                dropdown.withEntrySprites(null);
                dropdown.withEntryHeight(14);
                dropdown.withEntryRenderer(preset -> TempadUI.colored(Renderers.text(preset.title()).withLeftAlignment()).withPadding(0, 3));
            });
        });
        picker.setPosition(left, top + 8 + 4);
        addRenderableWidget(picker);

        TextBox colorInput = ColorPicker.input(currentColor, it -> {
            it.withSize(76, 16);
            it.withTexture(TempadUI.element);
            it.withTextColor(Tempad.ORANGE.getValue());
        });
        colorInput.withPosition(left + 16 + 2, top + 8 + 4);
        addRenderableWidget(colorInput);

        Label nameLabel = new Label(left, top + 8 + 4 + 16 + 4, 100, 8, NAME_FIELD, font).setColor(Tempad.ORANGE.getValue()).alignLeft();
        addRenderableWidget(nameLabel);

        TextBox nameInput = new TextBox(textState).withSize(94, 16).withTexture(TempadUI.element).withTextColor(Tempad.ORANGE.getValue()).withMaxLength(32);
        nameInput.withPosition(left, top + 8 + 4 + 16 + 4 + 8 + 4);
        addRenderableWidget(nameInput);

        Button done = new Button().withTexture(TempadUI.button)
                .withRenderer(Renderers.text(CommonComponents.GUI_DONE).withColor(Gfx.BLACK))
                .withCallback(() -> {
                    ModNetworking.sendToServer(new CreateLocationPacket(textState.getValue(), currentColor.getValue(), menu.ctxHolder));
                    this.onClose();
                });
        done.withSize(48, 16).withPosition(localLeft + 98 - 48, localTop + 98);
        addRenderableWidget(doneBtn.add(done));
    }
}
