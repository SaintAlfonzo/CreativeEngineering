// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/client/screen/ChronomarkScreen.kt.
// Translated to Java for Forge 1.19.2 on the client/ui widget kit (TP-D4); Olympus' default dropdown, doubleInput and
// intInput look (Olympus text box sprites, grey text) kept.
package earth.terrarium.tempad.client.screen;

import earth.terrarium.tempad.api.player_access.PlayerAccessApi;
import earth.terrarium.tempad.client.TempadUI;
import earth.terrarium.tempad.client.state.MutableState;
import earth.terrarium.tempad.client.ui.Button;
import earth.terrarium.tempad.client.ui.Dropdown;
import earth.terrarium.tempad.client.ui.DropdownState;
import earth.terrarium.tempad.client.ui.Label;
import earth.terrarium.tempad.client.ui.Renderers;
import earth.terrarium.tempad.client.ui.TextBox;
import earth.terrarium.tempad.client.ui.WidgetRenderer;
import earth.terrarium.tempad.common.network.c2s.UpdateChronomarkPacket;
import earth.terrarium.tempad.common.registries.ModNetworking;
import earth.terrarium.tempad.common.utils.Color;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class ChronomarkScreen extends TimedoorMarkerScreen {
    static final Component accessField = Component.translatable("screen.tempad.marker.access_field");

    public final List<ResourceLocation> accessOptions;
    public final MutableState<Float> yOffset;
    public final MutableState<Integer> angle;
    public final DropdownState<ResourceLocation> multiAccess;

    public ChronomarkScreen(BlockPos pos, String name, Color color, List<ResourceLocation> accessOptions, ResourceLocation access, boolean locked,
                            float yOffset, int angle) {
        super(pos, name, color, false, locked);
        this.accessOptions = accessOptions;
        this.yOffset = MutableState.of(yOffset);
        this.angle = MutableState.of(angle);
        this.multiAccess = DropdownState.of(access);
        this.bgWidth = 215;
    }

    private static Component accessName(ResourceLocation id) {
        return Component.translatable(Util.makeDescriptionId("access", id));
    }

    /** Olympus Widgets.doubleInput / intInput with their default look over the Tempad number input behaviour. */
    private static void olympusStyle(TextBox box) {
        box.withTexture(TextBox.OLYMPUS_TEXTBOX);
        box.withTextColor(GRAY);
        box.withSize(100, 20);
    }

    @Override
    protected void init() {
        super.init();
        // Column (gap 4) at (bg left + 110, bg top + 23): y offset label and input, angle label and input.
        int x = (width - bgWidth) / 2 + 110;
        int y = (height - bgHeight) / 2 + 23;
        y += place(new Label(Component.translatable("screen.tempad.marker.yoffset"), font).setColor(GRAY), x, y) + 4;
        y += place(TempadUI.floatInput(yOffset, ChronomarkScreen::olympusStyle), x, y) + 4;
        y += place(new Label(Component.translatable("screen.tempad.marker.angle"), font).setColor(GRAY), x, y) + 4;
        place(TempadUI.intInput(angle, ChronomarkScreen::olympusStyle), x, y);
    }

    @Override
    protected void sync() {
        ModNetworking.sendToServer(new UpdateChronomarkPacket(pos, color.getValue(), name.getValue(), multiAccess.get(), locked.get(), yOffset.get(), angle.get()));
    }

    @Override
    protected int accessFieldHeight() {
        return 9 + 4 + 20;
    }

    /** Upstream: the access label, then a dropdown (100 x 20, list opening TOP_RIGHT) of the options plus "private". */
    @Override
    protected void setupAccessField(int x, int y) {
        y += place(text(accessField), x, y) + 4;
        List<ResourceLocation> options = new ArrayList<>(accessOptions);
        options.add(PlayerAccessApi.NO_ACCESS);
        Button dropdown = Dropdown.create(multiAccess, options, ChronomarkScreen::accessName, it -> {
            it.withSize(100, 20);
            it.withRenderer(multiAccess.withRenderer((value, open) -> (WidgetRenderer) (value == null
                    ? Renderers.ellipsisWithChevron(open) : Renderers.textWithChevron(accessName(value), open))).withPadding(4, 6));
        }, Dropdown::withTopRightAlignment);
        place(dropdown, x, y);
    }
}
