// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/client/screen/tempad/TeleportScreen.kt.
// Translated to Java for Forge 1.19.2.
package earth.terrarium.tempad.client.screen.tempad;

import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.api.locations.NamedGlobalVec3;
import earth.terrarium.tempad.api.locations.TempadLocations;
import earth.terrarium.tempad.client.TempadUI;
import earth.terrarium.tempad.client.state.AppearanceState;
import earth.terrarium.tempad.client.state.MutableState;
import earth.terrarium.tempad.client.ui.Button;
import earth.terrarium.tempad.client.ui.Dropdown;
import earth.terrarium.tempad.client.ui.DropdownState;
import earth.terrarium.tempad.client.ui.Gfx;
import earth.terrarium.tempad.client.ui.Label;
import earth.terrarium.tempad.client.ui.Renderers;
import earth.terrarium.tempad.client.ui.ScrollList;
import earth.terrarium.tempad.client.ui.TextBox;
import earth.terrarium.tempad.client.ui.DeleteModal;
import earth.terrarium.tempad.client.ui.WidgetRenderer;
import earth.terrarium.tempad.common.config.ClientConfig;
import earth.terrarium.tempad.common.data.FavoriteLocationAttachment;
import earth.terrarium.tempad.common.network.c2s.DeleteLocationPacket;
import earth.terrarium.tempad.common.network.c2s.OpenTimedoorPacket;
import earth.terrarium.tempad.common.network.c2s.SetFavoritePacket;
import earth.terrarium.tempad.common.network.c2s.WriteToCardPacket;
import earth.terrarium.tempad.common.registries.ModMenus.TeleportMenu;
import earth.terrarium.tempad.common.registries.ModNetworking;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class TeleportScreen extends AbstractTempadScreen<TeleportMenu> {
    public static final Component teleportText = Component.translatable("app." + Tempad.MOD_ID + ".teleport");
    public static final Component noSelectionText = Component.translatable("app." + Tempad.MOD_ID + ".teleport.no_selection");
    public static final Component deleteText = Component.translatable("app." + Tempad.MOD_ID + ".teleport.delete");
    public static final Map<Component, MutableState<Boolean>> sectionVisibility = new HashMap<>();
    public static final DropdownState<Sorting> sortMode = DropdownState.of(initialSort());

    private static Sorting initialSort() {
        try {
            return Sorting.valueOf(ClientConfig.sortingMode());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Sorting.Dimension;
        }
    }

    @Nullable
    private Sorting.Entry selected;
    private final MutableState<String> search;
    private final Map<ResourceLocation, Map<UUID, NamedGlobalVec3>> locations = new LinkedHashMap<>();
    private final AppearanceState pinButton = new AppearanceState();
    private final AppearanceState deleteButton = new AppearanceState();
    private final AppearanceState teleportBtn = new AppearanceState();
    @Nullable
    private FavoriteLocationAttachment favorite;
    private ScrollList locationList;
    private ScrollList infoTextWidget;
    @Nullable
    private Button sortButton;

    public TeleportScreen(TeleportMenu menu, Inventory inv, Component title) {
        super(null, menu, inv, title);
        menu.appContent.locations.forEach((provider, map) -> locations.put(provider, new LinkedHashMap<>(map)));
        favorite = menu.appContent.favoriteLocation;
        pinButton.setVisible(false);
        deleteButton.setVisible(false);
        teleportBtn.setActive(false);
        search = MutableState.of("", s -> updateLocationPanel());
    }

    private void setSelected(@Nullable Sorting.Entry value) {
        selected = value;
        teleportBtn.setActive(value != null);
        pinButton.setVisible(value != null);
        deleteButton.setVisible(value != null && TempadLocations.deletable().contains(value.provider()));
    }

    private boolean isSelectedPinned() {
        return selected != null && favorite != null && favorite.matches(selected.provider(), selected.id());
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
            // sortedBy(name, lowercase) then a stable sortedBy(not favourite): the favourite first.
            filtered.sort(Comparator.comparing(e -> e.pos().name().getString().toLowerCase(Locale.ROOT)));
            filtered.sort(Comparator.comparing(e -> favorite != null && !favorite.matches(e.provider(), e.id())));
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
                    boolean isFavorite = favorite != null && favorite.matches(entry.provider(), entry.id());
                    Gfx.fill(pose, ctx.x, ctx.y, ctx.x + ctx.width, ctx.y + ctx.height, isSelected ? Tempad.ORANGE.getValue() : 0x3aff6f00);
                    int color = isSelected ? Gfx.BLACK : isHovered ? Tempad.HIGHLIGHTED_ORANGE.getValue() : Tempad.ORANGE.getValue();
                    if (isFavorite) {
                        Renderers.textWithIcon(entry.pos().name(), Tempad.id("icons/mini/pin")).withColor(color).withIconSize(7).withTextLeftIconLeft().render(pose, ctx, 0f);
                    } else {
                        Renderers.text(entry.pos().name()).withColor(color).withLeftAlignment().withPadding(0, 0, 0, 2).render(pose, ctx, 0f);
                    }
                };
                Button button = new Button().withTexture(null).withRenderer(renderer.withPadding(0, 2)).withCallback(() -> {
                    setSelected(entry);
                    updateInfoPanel();
                });
                button.withSize(110, 12);
                list.add(button);
            }
        }
    }

    private void initInfoPanel(ScrollList list) {
        list.clear();
        Sorting.Entry sel = selected;
        if (sel == null) {
            list.add(Label.multiline(62, noSelectionText).setColor(Tempad.ORANGE.getValue()).alignLeft());
        } else {
            list.add(new Label(sel.pos().dimensionText(), font).setColor(Tempad.ORANGE.getValue()));
            list.add(new Label(Component.literal("X: " + sel.pos().x()), font).setColor(Tempad.ORANGE.getValue()));
            list.add(new Label(Component.literal("Y: " + sel.pos().y()), font).setColor(Tempad.ORANGE.getValue()));
            list.add(new Label(Component.literal("Z: " + sel.pos().z()), font).setColor(Tempad.ORANGE.getValue()));
        }
    }

    public void updateLocationPanel() {
        if (locationList == null) return;
        locationList.clear();
        initLocationPanel(locationList);
        locationList.withScrollY(locationList.getYScroll());
    }

    public void updateInfoPanel() {
        if (infoTextWidget == null) return;
        initInfoPanel(infoTextWidget);
    }

    private void openDeleteModal() {
        DeleteModal.open(Component.translatable("app.tempad.teleport.delete_modal"), Component.translatable("app.tempad.teleport.delete_modal.description"), () -> {
            if (minecraft == null || selected == null) return;
            Sorting.Entry sel = selected;
            ModNetworking.sendToServer(new DeleteLocationPacket(menu.ctxHolder, sel.provider(), sel.id()));
            Map<UUID, NamedGlobalVec3> map = locations.get(sel.provider());
            if (map != null) map.remove(sel.id());
            setSelected(null);
            updateLocationPanel();
            updateInfoPanel();
        });
    }

    private Component pinTooltip() {
        return Component.translatable("app.tempad.teleport." + (isSelectedPinned() ? "unpin" : "pin"));
    }

    @Override
    protected void init() {
        super.init();

        TextBox searchBox = new TextBox(search).withTexture(TempadUI.element).withTextColor(Tempad.ORANGE.getValue())
                .withPlaceholder("Search...").withPlaceholderColor(Tempad.DARK_ORANGE.getValue())
                .withPosition(localLeft + 4, localTop + 20).withSize(94, 16);
        addRenderableWidget(searchBox);

        sortButton = Dropdown.create(sortMode, List.of(Sorting.values()), Sorting::toComponent, it -> {
            it.withPosition(localLeft + 100, localTop + 20);
            it.withSize(16, 16);
            it.withTexture(TempadUI.button);
            it.withRenderer(sortMode.withRenderer((mode, open) -> Renderers.icon(Tempad.id("icons/" + mode.toString().toLowerCase(Locale.ROOT))).withColor(Gfx.BLACK).withCentered(12, 12)));
            it.withTooltip(sortMode.get().toComponent());
        }, it -> {
            it.withSize(100, 64);
            it.withCallback(mode -> {
                ClientConfig.setSortingMode(mode.name());
                updateLocationPanel();
                if (sortButton != null) sortButton.withTooltip(mode.toComponent());
            });
            TempadUI.style(it, Sorting::toComponent);
        });
        addRenderableWidget(sortButton);

        locationList = new ScrollList().withContentFillWidth().withScrollableY(null).withScrollbarYRenderer(TempadUI.scrollbarYRenderer)
                .withTexture(TempadUI.element.get(true, false)).withOverscrollY(2).withContentMargin(1);
        locationList.withPosition(localLeft + 4, localTop + 38).withSize(112, 76);
        addRenderableWidget(locationList);
        updateLocationPanel();

        infoTextWidget = new ScrollList().withTexture(Tempad.id("element/normal")).withContentMargin(6);
        infoTextWidget.withPosition(localLeft + 118, localTop + 20).withSize(76, 76);
        addRenderableWidget(infoTextWidget);
        updateInfoPanel();

        // FrameLayout(74 x 74) at (119, 22): [delete, pin] row (spacing 2) bottom right, padding 4.
        int rowX = localLeft + 119 + 74 - 4 - (7 + 2 + 7);
        int rowY = localTop + 22 + 74 - 4 - 7;
        Button delete = new Button().withTexture(null)
                .withRenderer(TempadUI.colored(Renderers.icon(Tempad.id("icons/mini/x"))).withCentered(7, 7))
                .withCallback(this::openDeleteModal);
        delete.withSize(7, 7).withTooltip(deleteText);
        delete.setPosition(rowX, rowY);
        addRenderableWidget(deleteButton.add(delete));

        Button pin = new Button().withTexture(null);
        pin.withSize(7, 7);
        pin.withRenderer((pose, ctx, pt) -> TempadUI.colored(Renderers.icon(Tempad.id("icons/mini/" + (isSelectedPinned() ? "unpin" : "pin")))).withCentered(7, 7).render(pose, ctx, pt));
        pin.withCallback(() -> {
            if (minecraft == null || selected == null) return;
            Sorting.Entry sel = selected;
            if (favorite != null && favorite.matches(sel.provider(), sel.id())) {
                ModNetworking.sendToServer(new SetFavoritePacket(null));
                favorite = null;
            } else {
                ModNetworking.sendToServer(new SetFavoritePacket(sel.provider(), sel.id()));
                favorite = new FavoriteLocationAttachment(sel.provider(), sel.id());
            }
            updateLocationPanel();
            pin.withTooltip(pinTooltip());
        });
        pin.withTooltip(pinTooltip());
        pin.setPosition(rowX + 7 + 2, rowY);
        addRenderableWidget(pinButton.add(pin));

        Button teleport = new Button().withTexture(TempadUI.button)
                .withRenderer(Renderers.text(teleportText).withColor(Gfx.BLACK))
                .withCallback(() -> {
                    if (minecraft == null || selected == null) return;
                    ModNetworking.sendToServer(new OpenTimedoorPacket(selected.provider(), selected.id(), menu.ctxHolder));
                    this.onClose();
                });
        teleport.withSize(76, 16).withPosition(localLeft + 118, localTop + 98);
        addRenderableWidget(teleportBtn.add(teleport));
    }

    /** The info panel, the drop target for a carried location card. */
    private boolean overInfoPanel(double mouseX, double mouseY) {
        return mouseX >= localLeft + 118 && mouseX <= localLeft + 193 && mouseY >= localTop + 20 && mouseY <= localTop + 95;
    }

    /**
     * Clicking the info panel while carrying an item writes the selected location to it (the server only acts on a
     * location card). Skipped while a popup is open: upstream's overlays are separate screens that take the click.
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (popups().isEmpty() && overInfoPanel(mouseX, mouseY) && !menu.getCarried().isEmpty() && selected != null) {
            ModNetworking.sendToServer(new WriteToCardPacket(selected.provider(), selected.id(), menu.ctxHolder));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        super.render(pose, mouseX, mouseY, partialTick);
        if (popups().isEmpty() && overInfoPanel(mouseX, mouseY) && !menu.getCarried().isEmpty() && selected != null) {
            // GuiGraphics.renderOutline(x, y, 76, 76): four 1 px edges.
            int x = localLeft + 118, y = localTop + 20, w = 76, h = 76, color = Tempad.HIGHLIGHTED_ORANGE.getValue();
            Gfx.fill(pose, x, y, x + w, y + 1, color);
            Gfx.fill(pose, x, y + h - 1, x + w, y + h, color);
            Gfx.fill(pose, x, y + 1, x + 1, y + h - 1, color);
            Gfx.fill(pose, x + w - 1, y + 1, x + w, y + h - 1, color);
        }
    }
}
