// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/client/screen/tempad/KnowledgeAppScreen.kt. Translated to Java for
// Forge 1.19.2 on the widget kit (TP-D4): the guide (KnowledgeScreen's table of contents, title and description panels)
// inside the Tempad frame, for the Knowledge Repository app.
package earth.terrarium.tempad.client.screen.tempad;

import earth.terrarium.tempad.client.screen.guide.GuideLang;
import earth.terrarium.tempad.client.screen.guide.KnowledgeScreen;
import earth.terrarium.tempad.client.state.MutableState;
import earth.terrarium.tempad.common.registries.ModMenus;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KnowledgeAppScreen extends AbstractTempadScreen<ModMenus.KnowledgeMenu> {
    private Component currentTitle = GuideLang.overview();
    @Nullable
    private KnowledgeScreen.ContentList descriptionWidget;
    private KnowledgeScreen.Page description = KnowledgeScreen.defaultDescription;
    private final MutableState<ItemStack> selected = MutableState.of(null);
    private final List<Runnable> tickers = new ArrayList<>();
    private final Map<Component, Map<ResourceLocation, KnowledgeScreen.Page>> chapters = KnowledgeScreen.createChapters(tickers, this::handleComponentClicked);

    public KnowledgeAppScreen(ModMenus.KnowledgeMenu menu, Inventory inv, Component title) {
        super(null, menu, inv, title);
    }

    private void setDescription(KnowledgeScreen.Page value) {
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
        KnowledgeScreen.ContentList tableOfContents = addRenderableWidget(KnowledgeScreen.tableOfContents(localLeft + 4, localTop + 20));
        KnowledgeScreen.initTableOfContents(chapters, tableOfContents, selected, title -> currentTitle = title, this::setDescription);
        addRenderableWidget(KnowledgeScreen.titleLine(() -> currentTitle, localLeft + 78, localTop + 22));
        descriptionWidget = addRenderableWidget(KnowledgeScreen.descriptionPanel(localLeft + 78, localTop + 32));
        description.accept(descriptionWidget);
    }

    @Override
    public boolean handleComponentClicked(@Nullable Style style) {
        ResourceLocation item = KnowledgeScreen.pageLink(style);
        if (item == null) return false;
        ItemStack stack = ForgeRegistries.ITEMS.getValue(item).getDefaultInstance();
        currentTitle = stack.getHoverName();
        selected.setValue(stack);
        for (Map<ResourceLocation, KnowledgeScreen.Page> entries : chapters.values()) {
            KnowledgeScreen.Page newDesc = entries.get(item);
            if (newDesc == null) continue;
            setDescription(newDesc);
        }
        return true;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        for (Runnable function : List.copyOf(tickers)) function.run();
    }
}
