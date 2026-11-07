// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/client/screen/guide/KnowledgeScreen.kt. Translated to Java for Forge
// 1.19.2 on the widget kit (TP-D4): Olympus LayoutWidget + ClearableGridLayout -> ContentList (a ScrollList with upstream's
// width callbacks and row spacing), Widgets.textarea -> TextArea, Widgets.text / StringWidget -> text-only Buttons,
// Layouts.columns(3) inside a LayoutWidget -> ItemGrid, SpacerElement -> Spacer. TickingScreen's ticker list is a plain
// list here (the companion helpers take the list to add to). The text is upstream's lang, read at runtime (GuideLang).
package earth.terrarium.tempad.client.screen.guide;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.client.TempadUI;
import earth.terrarium.tempad.client.state.MutableState;
import earth.terrarium.tempad.client.ui.Button;
import earth.terrarium.tempad.client.ui.Gfx;
import earth.terrarium.tempad.client.ui.KitScreen;
import earth.terrarium.tempad.client.ui.KitWidget;
import earth.terrarium.tempad.client.ui.Renderers;
import earth.terrarium.tempad.client.ui.ScrollList;
import earth.terrarium.tempad.client.ui.Spacer;
import earth.terrarium.tempad.client.ui.Sprites;
import earth.terrarium.tempad.client.ui.TextArea;
import earth.terrarium.tempad.client.ui.TooltipSource;
import earth.terrarium.tempad.client.widgets.CraftingRecipeWidget;
import earth.terrarium.tempad.common.config.CommonConfigCache;
import earth.terrarium.tempad.common.registries.ModEntities;
import earth.terrarium.tempad.common.registries.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class KnowledgeScreen extends KitScreen {
    /** Upstream {@code (ClearableGridLayout) -> Unit}: fills the description panel. */
    @FunctionalInterface
    public interface Page extends Consumer<ContentList> {
    }

    /** Chapter headers collapsed in the table of contents (shared by the handheld guide and the Tempad app, as upstream). */
    public static final Set<Component> collapsed = new HashSet<>();

    public static final Page defaultDescription = list -> {
        Button image = new Button().withTexture(null)
                .withRenderer(Renderers.icon(Tempad.id("guide/images/tva")).withColor(Gfx.WHITE).withCentered(64, 32));
        image.active = false;
        image.setSize(98, 32);
        list.add(image);
        list.add(new TextArea(GuideLang.credits(), 98).setColor(Tempad.ORANGE.getValue()).alignCenter());
        list.add(new Spacer(1, 4));
        list.add(new TextArea(GuideLang.travelAdvisory(), 98).setColor(Tempad.ORANGE.getValue()).alignCenter().scale(0.25f));
    };

    // --- upstream companion object ---

    public static Map<Component, Map<ResourceLocation, Page>> createChapters(List<Runnable> tickers, Predicate<Style> clicks) {
        Map<Component, Map<ResourceLocation, Page>> chapters = new LinkedHashMap<>();

        Map<ResourceLocation, Page> iron = new LinkedHashMap<>();
        iron.put(ModItems.chrononCell.getId(), it -> {
            it.rowSpacing(4);
            it.add(paragraph(GuideLang.cellOverview(), clicks));
            it.add(title(GuideLang.crafting()));
            it.add(paragraph(GuideLang.cellCrafting(), clicks));
            addRecipe(it, tickers, Tempad.id("chronon_cell"));
            it.add(title(GuideLang.usage()));
            it.add(paragraph(GuideLang.cellUsage(), clicks));
        });
        iron.put(ModItems.chrononGenerator.getId(), it -> {
            it.rowSpacing(4);
            it.add(paragraph(GuideLang.chrononGenOverview(), clicks));
            it.add(title(GuideLang.crafting()));
            it.add(paragraph(GuideLang.chrononGenCrafting(), clicks));
            addRecipe(it, tickers, Tempad.id("chronon_generator"));
            it.add(title(GuideLang.usage()));
            it.add(paragraph(GuideLang.chrononGenUsage(), clicks));
            if (CommonConfigCache.ChrononGenerator.capacity() > 0) {
                it.add(paragraph(GuideLang.chrononGenStorage(), clicks));
            }
        });
        iron.put(ModItems.locationCard.getId(), it -> {
            it.rowSpacing(4);
            it.add(paragraph(GuideLang.cardOverview(), clicks));
            it.add(title(GuideLang.crafting()));
            it.add(paragraph(GuideLang.cardCrafting(), clicks));
            // Upstream asks for tempad:cheap_card; the recipe is tempad:card_cheap, so no grid is shown (kept).
            addRecipe(it, tickers, Tempad.id("cheap_card"));
            it.add(title(GuideLang.usage()));
            it.add(paragraph(GuideLang.cardUsage(), clicks));
            it.add(paragraph(GuideLang.cardUsage2(), clicks));
            it.add(paragraph(GuideLang.cardUsage3(), clicks));
        });
        iron.put(ModItems.cardWallet.getId(), it -> {
            it.rowSpacing(4);
            it.add(paragraph(GuideLang.walletOverview(), clicks));
            it.add(title(GuideLang.crafting()));
            it.add(paragraph(GuideLang.walletCrafting(), clicks));
            addRecipe(it, tickers, Tempad.id("card_wallet"));
            it.add(title(GuideLang.usage()));
            it.add(paragraph(GuideLang.walletUsage(), clicks));
            it.add(paragraph(GuideLang.walletUsage2(), clicks));
            it.add(paragraph(GuideLang.walletUsage3(), clicks));
        });
        iron.put(ModItems.timedoorMarker.getId(), it -> {
            it.rowSpacing(4);
            it.add(paragraph(GuideLang.markerOverview(), clicks));
            it.add(title(GuideLang.crafting()));
            it.add(paragraph(GuideLang.markerCrafting(), clicks));
            addRecipe(it, tickers, Tempad.id("timedoor_marker"));
            it.add(title(GuideLang.usage()));
            it.add(paragraph(GuideLang.markerUsage(), clicks));
            it.add(paragraph(GuideLang.markerUsage2(), clicks));
        });
        iron.put(ModItems.timedoorProjector.getId(), it -> {
            it.rowSpacing(4);
            it.add(paragraph(GuideLang.projectorOverview(), clicks));
            it.add(title(GuideLang.crafting()));
            it.add(paragraph(GuideLang.projectorCrafting(), clicks));
            addRecipe(it, tickers, Tempad.id("timedoor_projector"));
            it.add(title(GuideLang.usage()));
            it.add(paragraph(GuideLang.projectorUsage(), clicks));
            it.add(paragraph(GuideLang.projectorUsage2(), clicks));
            it.add(title(ModEntities.timedoor.get().getDescription()));
            it.add(paragraph(GuideLang.projectorTimedoors(), clicks));
            it.add(paragraph(GuideLang.projectorTimedoors2(), clicks));
        });
        iron.put(ModItems.locationBroadcaster.getId(), it -> {
            it.rowSpacing(4);
            it.add(paragraph(GuideLang.locationBroadcastersOverview(), clicks));
            it.add(title(GuideLang.crafting()));
            it.add(paragraph(GuideLang.locationBroadcastersCrafting(), clicks));
            addRecipe(it, tickers, Tempad.id("location_broadcaster"));
            it.add(title(GuideLang.usage()));
            it.add(paragraph(GuideLang.locationBroadcastersUsage(), clicks));
            it.add(paragraph(GuideLang.locationBroadcastersUsage2(), clicks));
        });
        chapters.put(GuideLang.iron(), iron);

        Map<ResourceLocation, Page> steel = new LinkedHashMap<>();
        steel.put(ModItems.timeSteel.getId(), it -> {
            it.rowSpacing(4);
            it.add(paragraph(GuideLang.timeSteelOverview(), clicks));
            it.add(title(GuideLang.crafting()));
            it.add(paragraph(GuideLang.timeSteelCrafting(), clicks));
            CraftingRecipeWidget recipe = recipe(tickers, Tempad.id("time_steel_shaped"));
            if (recipe == null) recipe = recipe(tickers, Tempad.id("time_steel_shapeless"));
            if (recipe != null) it.add(recipe);
            it.add(paragraph(GuideLang.timeSteelCrafting2(), clicks));
        });
        steel.put(ModItems.chrononBattery.getId(), it -> {
            it.rowSpacing(4);
            it.add(paragraph(GuideLang.chrononBatteryOverview(), clicks));
            it.add(title(GuideLang.crafting()));
            it.add(paragraph(GuideLang.chrononBatteryCrafting(), clicks));
            addRecipe(it, tickers, Tempad.id("chronon_battery"));
            it.add(title(GuideLang.usage()));
            it.add(paragraph(GuideLang.chrononBatteryUsage(), clicks));
        });
        steel.put(ModItems.chronometer.getId(), it -> {
            it.rowSpacing(4);
            it.add(paragraph(GuideLang.chronometerOverview(), clicks));
            it.add(title(GuideLang.crafting()));
            it.add(paragraph(GuideLang.chronometerCrafting(), clicks));
            addRecipe(it, tickers, Tempad.id("chronometer"));
            it.add(title(GuideLang.usage()));
            it.add(paragraph(GuideLang.chronometerUsage(), clicks));
            if (CommonConfigCache.Chronometer.capacity() > 0) {
                it.add(paragraph(GuideLang.chronometerInternalStorage(), clicks));
            }
        });
        steel.put(ModItems.chronomark.getId(), it -> {
            it.rowSpacing(4);
            it.add(paragraph(GuideLang.chronomarkOverview(), clicks));
            it.add(title(GuideLang.crafting()));
            it.add(paragraph(GuideLang.chronomarkCrafting(), clicks));
            addRecipe(it, tickers, Tempad.id("chronomark"));
            it.add(title(GuideLang.usage()));
            it.add(paragraph(GuideLang.chronomarkUsage(), clicks));
            it.add(paragraph(GuideLang.chronomarkUsage2(), clicks));
        });
        steel.put(ModItems.tempad.getId(), it -> {
            it.rowSpacing(4);
            it.add(paragraph(GuideLang.tempadOverview(), clicks));
            it.add(title(GuideLang.crafting()));
            it.add(paragraph(GuideLang.tempadCrafting(), clicks));
            addRecipe(it, tickers, Tempad.id("tempad"));
            it.add(title(GuideLang.usage()));
            it.add(paragraph(GuideLang.tempadUsage(), clicks));
            it.add(paragraph(GuideLang.tempadUsage2(), clicks));
            it.add(title(ModEntities.timedoor.get().getDescription()));
            it.add(paragraph(GuideLang.tempadTimedoors(), clicks));
            it.add(paragraph(GuideLang.tempadTimedoors2(), clicks));
            it.add(title(GuideLang.tempadApps()));
            it.add(subtitle(Component.translatable("app.tempad.teleport")));
            it.add(paragraph(GuideLang.tempadAppTeleport(), clicks));
            it.add(paragraph(GuideLang.tempadAppTeleport2(), clicks));
            it.add(paragraph(GuideLang.tempadAppTeleport3(), clicks));
            it.add(subtitle(Component.translatable("app.tempad.new_location")));
            it.add(paragraph(GuideLang.tempadAppNewLocation(), clicks));
            it.add(subtitle(Component.translatable("app.tempad.travel_timeline")));
            it.add(paragraph(GuideLang.tempadAppTravelTimeline(), clicks));
            it.add(subtitle(Component.translatable("app.tempad.settings")));
            it.add(paragraph(GuideLang.tempadAppSettings(), clicks));
            it.add(subtitle(Component.translatable("app.tempad.portal_setup")));
            it.add(paragraph(GuideLang.tempadAppPortalSetup(), clicks));
        });
        steel.put(ModItems.workstation.getId(), it -> {
            it.rowSpacing(4);
            it.add(paragraph(GuideLang.workstationOverview(), clicks));
            it.add(title(GuideLang.crafting()));
            it.add(paragraph(GuideLang.workstationCrafting(), clicks));
            addRecipe(it, tickers, Tempad.id("workstation"));
            it.add(title(GuideLang.usage()));
            it.add(paragraph(GuideLang.workstationUsage(), clicks));
            it.add(paragraph(GuideLang.workstationUsage2(), clicks));
            it.add(paragraph(GuideLang.workstationUsage3(), clicks));
        });
        steel.put(ModItems.timeTwister.getId(), it -> {
            it.rowSpacing(4);
            it.add(paragraph(GuideLang.timeTwisterOverview(), clicks));
            it.add(title(GuideLang.crafting()));
            it.add(paragraph(GuideLang.timeTwisterCrafting(), clicks));
            addRecipe(it, tickers, Tempad.id("time_twister"));
            it.add(title(GuideLang.usage()));
            it.add(paragraph(GuideLang.timeTwisterUsage(), clicks));
            it.add(paragraph(GuideLang.timeTwisterUsage2(), clicks));
            it.add(paragraph(GuideLang.timeTwisterUsage3(), clicks));
        });
        steel.put(ModItems.screeningDevice.getId(), it -> {
            it.rowSpacing(4);
            it.add(paragraph(GuideLang.screeningDeviceOverview(), clicks));
            it.add(title(GuideLang.crafting()));
            it.add(paragraph(GuideLang.screeningDeviceCrafting(), clicks));
            addRecipe(it, tickers, Tempad.id("screening_device"));
            it.add(title(GuideLang.usage()));
            it.add(paragraph(GuideLang.screeningDeviceUsage(), clicks));
        });
        steel.put(ModItems.metronome.getId(), it -> {
            it.rowSpacing(4);
            it.add(paragraph(GuideLang.metronomesOverview(), clicks));
            it.add(title(GuideLang.crafting()));
            it.add(paragraph(GuideLang.metronomesCrafting(), clicks));
            addRecipe(it, tickers, Tempad.id("metronome"));
            it.add(title(GuideLang.usage()));
            it.add(paragraph(GuideLang.metronomesUsage(), clicks));
            it.add(paragraph(GuideLang.metronomesUsage2(), clicks));
            if (CommonConfigCache.Metronome.scaleGeneration()) {
                it.add(paragraph(GuideLang.metronomesMultiCharge(), clicks));
            } else {
                it.add(paragraph(GuideLang.metronomesNoMultiCharge(), clicks));
            }
        });
        chapters.put(GuideLang.steel(), steel);
        return chapters;
    }

    /** Upstream {@code TickingScreen.recipe(id)}: the crafting grid of a client-known crafting recipe, ticked by the screen. */
    @Nullable
    public static CraftingRecipeWidget recipe(List<Runnable> tickers, ResourceLocation id) {
        CraftingRecipeWidget recipe = CraftingRecipeWidget.create(id);
        if (recipe != null) tickers.add(recipe::tick);
        return recipe;
    }

    private static void addRecipe(ContentList list, List<Runnable> tickers, ResourceLocation id) {
        CraftingRecipeWidget recipe = recipe(tickers, id);
        if (recipe != null) list.add(recipe);
    }

    /** Upstream {@code closestPercent}: the scale nearest to percent that lands on whole screen pixels. */
    public static float closestPercent(float percent) {
        Minecraft minecraft = Minecraft.getInstance();
        int scale = minecraft.options.guiScale().get();
        if (scale <= 0) scale = minecraft.getWindow().calculateScale(0, minecraft.isEnforceUnicode());
        return (float) Math.ceil(scale * percent) / scale;
    }

    public static TextArea paragraph(Component text, Predicate<Style> clicks) {
        return new TextArea(text, 98).scale(closestPercent(0.7f)).alignLeft().setColor(Tempad.ORANGE.getValue()).clickActionCallback(clicks);
    }

    /** Upstream {@code title}: one underlined line (the text's own contents, without its style), left aligned, orange. */
    public static Button title(Component text) {
        Component line = MutableComponent.create(text.getContents()).withStyle(ChatFormatting.UNDERLINE);
        Button widget = new Button().withTexture(null)
                .withRenderer(Renderers.text(line).withColor(Tempad.ORANGE.getValue()).withLeftAlignment());
        widget.active = false;
        widget.setSize(Minecraft.getInstance().font.width(line), 9);
        return widget;
    }

    public static TextArea subtitle(Component text) {
        return new TextArea(text, 98).scale(closestPercent(0.8f)).alignLeft().setColor(Tempad.ORANGE.getValue());
    }

    public static void initTableOfContents(Map<Component, Map<ResourceLocation, Page>> chapters, ContentList tableOfContents,
                                           MutableState<ItemStack> selected, Consumer<Component> titleSetter, Consumer<Page> descSetter) {
        tableOfContents.clear();
        for (Map.Entry<Component, Map<ResourceLocation, Page>> chapter : chapters.entrySet()) {
            Component category = chapter.getKey();
            Button header = new Button().withTexture(null)
                    .withRenderer((pose, ctx, partialTick) -> {
                        Gfx.fill(pose, ctx.x, ctx.y, ctx.x + ctx.width, ctx.y + ctx.height, 0x3aff6f00);
                        TempadUI.colored(Renderers.textWithChevron(category, !collapsed.contains(category))).withPadding(2).render(pose, ctx, partialTick);
                    })
                    .withCallback(() -> {
                        if (collapsed.contains(category)) {
                            collapsed.remove(category);
                        } else {
                            collapsed.add(category);
                        }
                        initTableOfContents(chapters, tableOfContents, selected, titleSetter, descSetter);
                    });
            header.setSize(72, 12);
            tableOfContents.add(header);
            if (collapsed.contains(category)) {
                tableOfContents.add(new Spacer(72, 2));
                continue;
            }
            ItemGrid grid = new ItemGrid(3);
            for (Map.Entry<ResourceLocation, Page> entry : chapter.getValue().entrySet()) {
                ResourceLocation id = entry.getKey();
                ItemStack stack = ForgeRegistries.ITEMS.getValue(id).getDefaultInstance();
                Button button = new Button().withTexture(null);
                button.withRenderer((pose, ctx, partialTick) -> {
                    ItemStack current = selected.getValue();
                    boolean isSelected = current != null && id.equals(ForgeRegistries.ITEMS.getKey(current.getItem()));
                    if (isSelected) {
                        Gfx.fill(pose, ctx.x, ctx.y, ctx.x + ctx.width, ctx.y + ctx.height, Tempad.ORANGE.getValue());
                    } else if (ctx.widget.isHoveredOrFocused()) {
                        Gfx.fill(pose, ctx.x, ctx.y, ctx.x + ctx.width, ctx.y + ctx.height, 0x3aff6f00);
                    }
                    Minecraft.getInstance().getItemRenderer().renderGuiItem(stack, ctx.x + 1, ctx.y + 1);
                });
                button.withCallback(() -> {
                    selected.setValue(stack);
                    titleSetter.accept(stack.getHoverName());
                    descSetter.accept(entry.getValue());
                });
                button.setSize(18, 18);
                grid.add(button);
            }
            tableOfContents.add(grid);
            tableOfContents.add(new Spacer(72, 2));
        }
    }

    /** The upstream table of contents panel: element background, scrollbar always shown, buttons wider than an item fill it. */
    public static ContentList tableOfContents(int x, int y) {
        ContentList list = new ContentList((widget, child) -> {
            if (child.getWidth() > 18 && child instanceof Button) child.setWidth(widget.getViewWidth() - 4);
        });
        list.withScrollableY(Boolean.TRUE).withScrollbarYRenderer(TempadUI.scrollbarYRenderer)
                .withTexture(TempadUI.element.get(true, false)).withOverscrollY(2).withContentMargin(1);
        list.withPosition(x, y).withSize(72, 94);
        return list;
    }

    /** The upstream description panel: element background, scrollbar when needed, every row as wide as the view less 6. */
    public static ContentList descriptionPanel(int x, int y) {
        ContentList list = new ContentList((widget, child) -> child.setWidth(widget.getViewWidth() - 6));
        list.withScrollableY(null).withScrollbarYRenderer(TempadUI.scrollbarYRenderer)
                .withTexture(TempadUI.element.get(true, false)).withOverscrollY(3).withContentMargin(1);
        list.withPosition(x, y).withSize(116, 82);
        return list;
    }

    /** The page title: upstream's StringWidget (one shadowed line, left aligned, orange) showing the current title. */
    public static Button titleLine(java.util.function.Supplier<Component> title, int x, int y) {
        Button widget = new Button().withTexture(null)
                .withRenderer((pose, ctx, pt) -> Renderers.text(title.get()).withColor(Tempad.ORANGE.getValue()).withShadow().withLeftAlignment().render(pose, ctx, pt));
        widget.active = false;
        widget.withSize(116, 10).withPosition(x, y);
        return widget;
    }

    /** Upstream {@code handleComponentClicked}: a CHANGE_PAGE link opens that item's page. */
    @Nullable
    public static ResourceLocation pageLink(@Nullable Style style) {
        if (style == null || style.getClickEvent() == null || style.getClickEvent().getAction() != ClickEvent.Action.CHANGE_PAGE) return null;
        return ResourceLocation.tryParse(style.getClickEvent().getValue());
    }

    // --- the handheld guide (knowledge projector) ---

    private int leftPos = 0;
    private int topPos = 0;
    private final int imageWidth = 198;
    private final int imageHeight = 118;
    private Component currentTitle = GuideLang.overview();
    @Nullable
    private ContentList descriptionWidget;
    private Page description = defaultDescription;
    private final MutableState<ItemStack> selected = MutableState.of(null);
    private final List<Runnable> tickers = new ArrayList<>();
    private final Map<Component, Map<ResourceLocation, Page>> chapters = createChapters(tickers, this::handleComponentClicked);

    public KnowledgeScreen() {
        super(ModItems.knowledgeProjector.get().getDescription());
    }

    private void setDescription(Page value) {
        description = value;
        if (descriptionWidget != null) {
            descriptionWidget.clear();
            value.accept(descriptionWidget);
            descriptionWidget.withScrollY(-3);
        }
    }

    @Override
    protected void init() {
        super.init();
        leftPos = (width - imageWidth) / 2;
        topPos = (height - imageHeight) / 2;
        Component heading = ModItems.knowledgeProjector.get().getDescription();
        Button header = new Button().withTexture(null)
                .withRenderer(Renderers.text(heading).withColor(Tempad.ORANGE.getValue()).withShadow().withLeftAlignment());
        header.active = false;
        header.withSize(font.width(heading), 9).withPosition(leftPos + 4, topPos + 6);
        addRenderableWidget(header);

        ContentList tableOfContents = addRenderableWidget(tableOfContents(leftPos + 4, topPos + 20));
        addRenderableWidget(titleLine(() -> currentTitle, leftPos + 78, topPos + 22));
        descriptionWidget = addRenderableWidget(descriptionPanel(leftPos + 78, topPos + 32));

        initTableOfContents(chapters, tableOfContents, selected, title -> currentTitle = title, this::setDescription);

        description.accept(descriptionWidget);
    }

    @Override
    public void renderBackground(PoseStack pose) {
        super.renderBackground(pose);
        RenderSystem.enableBlend();
        Sprites.blit(pose, TempadUI.element.get(true, false), leftPos, topPos, imageWidth, imageHeight);
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        RenderSystem.enableBlend();
        super.render(pose, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean handleComponentClicked(@Nullable Style style) {
        ResourceLocation item = pageLink(style);
        if (item == null) return false;
        ItemStack stack = ForgeRegistries.ITEMS.getValue(item).getDefaultInstance();
        currentTitle = stack.getHoverName();
        selected.setValue(stack);
        for (Map<ResourceLocation, Page> entries : chapters.values()) {
            Page newDesc = entries.get(item);
            if (newDesc == null) continue;
            setDescription(newDesc);
        }
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        for (Runnable function : List.copyOf(tickers)) function.run();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // --- layout pieces ---

    /**
     * Olympus LayoutWidget over a one-column ClearableGridLayout as the guide uses it: the kit's ScrollList plus the
     * width callback (applied before every layout, so it follows the scrollbar appearing) and rowSpacing.
     */
    public static class ContentList extends ScrollList {
        private final BiConsumer<ContentList, AbstractWidget> widthCallback;
        private final List<AbstractWidget> children = new ArrayList<>();
        private int rowSpacing;

        public ContentList(BiConsumer<ContentList, AbstractWidget> widthCallback) {
            this.widthCallback = widthCallback;
        }

        /** ClearableGridLayout.rowSpacing: a gap between consecutive rows (kept after clear(), as upstream's layout). */
        public ContentList rowSpacing(int spacing) {
            this.rowSpacing = spacing;
            return this;
        }

        @Override
        public <W extends AbstractWidget> W add(W widget) {
            if (rowSpacing > 0 && !children.isEmpty()) {
                Spacer gap = new Spacer(0, rowSpacing);
                children.add(gap);
                super.add(gap);
            }
            children.add(widget);
            return super.add(widget);
        }

        @Override
        public void clear() {
            children.clear();
            super.clear();
        }

        private void applyWidths() {
            for (AbstractWidget child : children) widthCallback.accept(this, child);
        }

        @Override
        protected void renderWidget(PoseStack pose, int mouseX, int mouseY, float partialTick) {
            applyWidths();
            super.renderWidget(pose, mouseX, mouseY, partialTick);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            applyWidths();
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    /** Layouts.columns(n) of item buttons inside a LayoutWidget with the element texture and a 1 px content margin. */
    public static class ItemGrid extends KitWidget {
        private final int columns;
        private final List<Button> buttons = new ArrayList<>();
        @Nullable
        private Button clicked;

        public ItemGrid(int columns) {
            super(2, 2);
            this.columns = columns;
        }

        public void add(Button button) {
            buttons.add(button);
            int cols = Math.min(buttons.size(), columns);
            int rows = (buttons.size() + columns - 1) / columns;
            setSize(cols * 18 + 2, rows * 18 + 2);
        }

        private void arrange() {
            for (int i = 0; i < buttons.size(); i++) {
                buttons.get(i).setPosition(x + 1 + (i % columns) * 18, y + 1 + (i / columns) * 18);
            }
        }

        @Override
        protected void renderWidget(PoseStack pose, int mouseX, int mouseY, float partialTick) {
            arrange();
            RenderSystem.enableBlend();
            Sprites.blit(pose, TempadUI.element.get(true, false), x, y, width, height);
            for (Button button : buttons) button.render(pose, mouseX, mouseY, partialTick);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            arrange();
            for (Button child : buttons) {
                if (child.mouseClicked(mouseX, mouseY, button)) {
                    clicked = child;
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            Button child = clicked;
            clicked = null;
            return child != null && child.mouseReleased(mouseX, mouseY, button);
        }

        @Nullable
        @Override
        public Component tooltipAt(double mouseX, double mouseY) {
            for (Button child : buttons) {
                Component tooltip = ((TooltipSource) child).tooltipAt(mouseX, mouseY);
                if (tooltip != null) return tooltip;
            }
            return null;
        }
    }
}
