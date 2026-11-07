// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/client/screen/tempad/SettingsScreen.kt.
// Translated to Java for Forge 1.19.2 (Olympus ListWidget of labelled rows: rows of 20 with a gap of 2, label left
// and vertically centred, dropdown right).
package earth.terrarium.tempad.client.screen.tempad;

import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.api.app.AppRegistry;
import earth.terrarium.tempad.api.macro.MacroRegistry;
import earth.terrarium.tempad.client.TempadUI;
import earth.terrarium.tempad.client.ui.Button;
import earth.terrarium.tempad.client.ui.Dropdown;
import earth.terrarium.tempad.client.ui.DropdownState;
import earth.terrarium.tempad.client.ui.Gfx;
import earth.terrarium.tempad.client.ui.Label;
import earth.terrarium.tempad.client.ui.Renderers;
import earth.terrarium.tempad.client.ui.WidgetRenderer;
import earth.terrarium.tempad.common.network.c2s.SaveSettingsPacket;
import earth.terrarium.tempad.common.registries.ModComponents;
import earth.terrarium.tempad.common.registries.ModMenus;
import earth.terrarium.tempad.common.registries.ModNetworking;
import net.minecraft.Util;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public class SettingsScreen extends AbstractTempadScreen<ModMenus.SettingsMenu> {
    private final DropdownState<ResourceLocation> defaultMacro;
    private final DropdownState<ResourceLocation> defaultApp;

    public SettingsScreen(ModMenus.SettingsMenu menu, Inventory inv, Component title) {
        super(null, menu, inv, title);
        this.defaultMacro = DropdownState.of(ModComponents.getDefaultMacro(menu.ctx.getStack()));
        this.defaultApp = DropdownState.of(ModComponents.getDefaultApp(menu.ctx.getStack()));
    }

    private static Component key(String type, ResourceLocation id) {
        return Component.translatable(Util.makeDescriptionId(type, id));
    }

    private Button dropdown(DropdownState<ResourceLocation> state, List<ResourceLocation> options, String type) {
        return Dropdown.create(state, options, id -> key(type, id), it -> {
            it.withTexture(TempadUI.button);
            it.withSize(80, 20);
            it.withRenderer(state.withRenderer((value, enabled) -> {
                Renderers.TextWithIconRenderer r = value != null ? Renderers.textWithChevron(key(type, value), enabled) : Renderers.ellipsisWithChevron(enabled);
                return (WidgetRenderer) r.withColor(Gfx.BLACK);
            }).withPadding(0, 4));
        }, d -> TempadUI.style(d, id -> key(type, id)));
    }

    private void row(int index, String labelKey, Button widget) {
        int rowY = localTop + 20 + index * (20 + 2);
        Component text = Component.translatable("app." + Tempad.MOD_ID + "." + labelKey);
        Label label = new Label(text, font).setColor(Tempad.ORANGE.getValue());
        label.setPosition(localLeft + 4, rowY + (widget.getHeight() - label.getHeight()) / 2);
        addRenderableWidget(label);
        widget.setPosition(localLeft + 4 + 190 - widget.getWidth(), rowY);
        addRenderableWidget(widget);
    }

    @Override
    protected void init() {
        super.init();
        row(0, "settings.default_macro", dropdown(defaultMacro, new ArrayList<>(MacroRegistry.getIds()), "macro"));
        row(1, "settings.default_app", dropdown(defaultApp, new ArrayList<>(AppRegistry.getIds()), "app"));

        Button done = new Button().withTexture(TempadUI.button)
                .withRenderer(Renderers.text(CommonComponents.GUI_DONE).withColor(Gfx.BLACK))
                .withCallback(() -> ModNetworking.sendToServer(new SaveSettingsPacket(menu.ctxHolder, defaultApp.get(), defaultMacro.get())));
        done.withSize(font.width(CommonComponents.GUI_DONE) + 12, 16);
        done.setPosition(localLeft + 198 - 4 - done.getWidth(), localTop + 118 - 4 - 16);
        addRenderableWidget(done);
    }
}
