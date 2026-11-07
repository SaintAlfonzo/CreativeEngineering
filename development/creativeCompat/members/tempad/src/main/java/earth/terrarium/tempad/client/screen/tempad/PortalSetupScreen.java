// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/client/screen/tempad/PortalSetupScreen.kt.
// Translated to Java for Forge 1.19.2 on the client/ui widget kit (TP-D4). Olympus' column / row layouts are placed by
// hand with the same sizes and 2 px gaps.
package earth.terrarium.tempad.client.screen.tempad;

import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.api.locations.IndirectLocation;
import earth.terrarium.tempad.api.locations.NamedGlobalVec3;
import earth.terrarium.tempad.client.TempadUI;
import earth.terrarium.tempad.client.state.MutableState;
import earth.terrarium.tempad.client.ui.Button;
import earth.terrarium.tempad.client.ui.Gfx;
import earth.terrarium.tempad.client.ui.Label;
import earth.terrarium.tempad.client.ui.Renderers;
import earth.terrarium.tempad.client.ui.ScrollList;
import earth.terrarium.tempad.client.ui.TextBox;
import earth.terrarium.tempad.client.ui.WidgetRenderer;
import earth.terrarium.tempad.common.data.PortalPlacementComponent;
import earth.terrarium.tempad.common.network.c2s.SyncPortalSettingsPacket;
import earth.terrarium.tempad.common.registries.ModComponents;
import earth.terrarium.tempad.common.registries.ModMenus;
import earth.terrarium.tempad.common.registries.ModNetworking;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static earth.terrarium.tempad.client.screen.tempad.TeleportScreen.sectionVisibility;
import static earth.terrarium.tempad.client.screen.tempad.TeleportScreen.sortMode;

public class PortalSetupScreen extends AbstractTempadScreen<ModMenus.PortalSetupMenu> {
    private record Selection(ResourceLocation provider, UUID id) {
    }

    public final MutableState<Float> xOffset;
    public final MutableState<Float> yOffset;
    public final MutableState<Float> zOffset;
    public final MutableState<Integer> angle;
    public final MutableState<Boolean> isUpright;
    @Nullable
    private Selection selected;
    public final MutableState<String> search;
    private ScrollList locationList;
    private final Map<ResourceLocation, Map<UUID, NamedGlobalVec3>> locations = new LinkedHashMap<>();

    public PortalSetupScreen(ModMenus.PortalSetupMenu menu, Inventory inv, Component title) {
        super(null, menu, inv, title);
        PortalPlacementComponent offset = ModComponents.getPortalOffset(menu.ctx.getStack());
        xOffset = MutableState.of(offset.leftRight());
        yOffset = MutableState.of(offset.upDown());
        zOffset = MutableState.of(offset.forwardBack());
        angle = MutableState.of(offset.angle());
        isUpright = MutableState.of(offset.isUpright());
        IndirectLocation selectedPos = ModComponents.getSelectedPos(menu.ctx.getStack());
        selected = selectedPos == null ? null : new Selection(selectedPos.provider(), selectedPos.id());
        search = MutableState.of("", s -> updateLocationPanel());
        menu.appContent.locations.forEach((provider, map) -> locations.put(provider, new LinkedHashMap<>(map)));
    }

    private Label text(String key, int width, int height) {
        return new Label(0, 0, width, height, Component.translatable(key), font).setColor(Tempad.ORANGE.getValue()).alignLeft();
    }

    /** Places a Layouts.row (gap 2) at (x, y); returns its height. */
    private int row(int x, int y, AbstractWidget... children) {
        int height = 0;
        for (AbstractWidget child : children) {
            child.x = x;
            child.y = y;
            x += child.getWidth() + 2;
            height = Math.max(height, child.getHeight());
            addRenderableWidget(child);
        }
        return height;
    }

    @Override
    protected void init() {
        super.init();

        // Layouts.column().withGap(2) at (localLeft + 4, localTop + 20).
        int x = localLeft + 4;
        int y = localTop + 20;
        y += row(x, y, text("app.tempad.portal_setup.offset", 78, 11)) + 2;
        y += row(x, y, text("app.tempad.portal_setup.left_right", 40, 15).withTooltip(Component.translatable("app.tempad.portal_setup.left_right.desc")),
                TempadUI.floatInput(xOffset, it -> it.withSize(36, 15))) + 2;
        y += row(x, y, text("app.tempad.portal_setup.up_down", 40, 15).withTooltip(Component.translatable("app.tempad.portal_setup.up_down.desc")),
                TempadUI.floatInput(yOffset, it -> it.withSize(36, 15))) + 2;
        y += row(x, y, text("app.tempad.portal_setup.front_back", 40, 15).withTooltip(Component.translatable("app.tempad.portal_setup.front_back.desc")),
                TempadUI.floatInput(zOffset, it -> it.withSize(36, 15))) + 2;
        y += row(x, y, text("app.tempad.portal_setup.angle", 40, 15),
                TempadUI.intInput(angle, it -> it.withSize(36, 15))) + 2;
        row(x, y, text("app.tempad.portal_setup.upright", 56, 12), TempadUI.toggle(isUpright).withSize(20, 12));

        TextBox searchBox = new TextBox(search).withTexture(TempadUI.element).withTextColor(Tempad.ORANGE.getValue())
                .withPlaceholder("Search...").withPlaceholderColor(Tempad.DARK_ORANGE.getValue())
                .withPosition(localLeft + 84, localTop + 18).withSize(109, 16);
        addRenderableWidget(searchBox);

        locationList = new ScrollList().withContentFillWidth().withScrollableY(null).withScrollbarYRenderer(TempadUI.scrollbarYRenderer)
                .withTexture(Tempad.id("element/normal")).withOverscrollY(2).withContentMargin(1);
        locationList.withPosition(localLeft + 84, localTop + 36).withSize(109, 77);
        updateLocationPanel();
        addRenderableWidget(locationList);
    }

    public void updateLocationPanel() {
        if (locationList == null) return;
        locationList.clear();
        initLocationPanel(locationList);
        locationList.withScrollY(locationList.getYScroll());
    }

    private MutableState<Boolean> visibility(Component category) {
        return sectionVisibility.getOrDefault(category, MutableState.of(true));
    }

    private void initLocationPanel(ScrollList list) {
        String query = search.getValue().toLowerCase(Locale.ROOT);
        for (Map.Entry<Component, List<Sorting.Entry>> section : sortMode.get().reorganize(locations).entrySet()) {
            Component category = section.getKey();
            List<Sorting.Entry> filtered = new ArrayList<>(section.getValue().stream()
                    .filter(e -> e.pos().name().getString().toLowerCase(Locale.ROOT).contains(query)).toList());
            filtered.sort(Comparator.comparing(e -> e.pos().name().getString().toLowerCase(Locale.ROOT)));
            if (filtered.isEmpty()) continue;

            Button header = new Button().withTexture(null)
                    .withRenderer(visibility(category).withRenderer(enabled -> TempadUI.colored(Renderers.textWithChevron(category, enabled)).withPadding(0, 2, 0, 4)))
                    .withCallback(() -> {
                        MutableState<Boolean> state = visibility(category);
                        state.set(!state.get());
                        sectionVisibility.put(category, state);
                        updateLocationPanel();
                    });
            header.withSize(110, 12);
            list.add(header);

            if (!visibility(category).get()) continue;
            for (Sorting.Entry entry : filtered) {
                WidgetRenderer renderer = (pose, ctx, pt) -> {
                    boolean isSelected = selected != null && selected.id().equals(entry.id()) && selected.provider().equals(entry.provider());
                    boolean isHovered = ctx.widget.isHoveredOrFocused();
                    Gfx.fill(pose, ctx.x, ctx.y, ctx.x + ctx.width, ctx.y + ctx.height, isSelected ? Tempad.ORANGE.getValue() : 0x3aff6f00);
                    int color = isSelected ? Gfx.BLACK : isHovered ? Tempad.HIGHLIGHTED_ORANGE.getValue() : Tempad.ORANGE.getValue();
                    Renderers.text(entry.pos().name()).withColor(color).withLeftAlignment().withPadding(0, 0, 0, 2).render(pose, ctx, 0f);
                };
                Button button = new Button().withTexture(null).withRenderer(renderer.withPadding(0, 2))
                        .withCallback(() -> selected = new Selection(entry.provider(), entry.id()));
                button.withSize(110, 12);
                list.add(button);
            }
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        Selection sel = selected;
        ModNetworking.sendToServer(new SyncPortalSettingsPacket(xOffset.get(), yOffset.get(), zOffset.get(), angle.get(), isUpright.get(),
                sel == null ? null : sel.provider(), sel == null ? null : sel.id(), menu.ctxHolder));
    }
}
