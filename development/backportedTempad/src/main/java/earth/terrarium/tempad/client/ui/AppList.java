// Behaviour translated from Resourceful Lib's SelectionList (MIT, Terrarium) as used by Tempad 3.0.4's app bar, with
// Tempad's AppButton entry (src/main/kotlin/earth/terrarium/tempad/client/widgets/buttons/AppButton.kt).
package earth.terrarium.tempad.client.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import earth.terrarium.tempad.api.app.TempadApp;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AppList extends KitWidget {
    public record Entry(TempadApp<?> app, ResourceLocation appId) {
        WidgetSprites sprites() {
            return new WidgetSprites(new ResourceLocation(appId.getNamespace(), "app/" + appId.getPath() + "/normal"),
                    new ResourceLocation(appId.getNamespace(), "app/" + appId.getPath() + "/disabled"),
                    new ResourceLocation(appId.getNamespace(), "app/" + appId.getPath() + "/hover"));
        }

        Component title() {
            return Component.translatable(Util.makeDescriptionId("app", appId));
        }
    }

    private final int itemHeight;
    private final Consumer<Entry> onSelect;
    private final List<Entry> entries = new ArrayList<>();

    public AppList(int x, int y, int width, int height, int itemHeight, Consumer<Entry> onSelect) {
        super(width, height);
        this.x = x;
        this.y = y;
        this.itemHeight = itemHeight;
        this.onSelect = onSelect;
    }

    public void addEntry(Entry entry) {
        entries.add(entry);
    }

    @Nullable
    private Entry entryAt(double mouseX, double mouseY) {
        if (mouseX < x || mouseX >= x + width || mouseY < y || mouseY >= y + height) return null;
        int index = (int) ((mouseY - y) / itemHeight);
        return index >= 0 && index < entries.size() ? entries.get(index) : null;
    }

    @Override
    protected void renderWidget(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        var player = Minecraft.getInstance().player;
        Gfx.enableScissor(x, y, x + width, y + height);
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            int top = y + i * itemHeight;
            boolean hovered = isHovered && mouseY >= top && mouseY < top + itemHeight;
            Sprites.blit(pose, entry.sprites().get(player != null && entry.app().isEnabled(player), hovered), x + 1, top + 1, 14, 14);
        }
        Gfx.disableScissor();
    }

    @Nullable
    @Override
    public Component tooltipAt(double mouseX, double mouseY) {
        if (!visible || !isHovered) return null;
        Entry entry = entryAt(mouseX, mouseY);
        return entry == null ? null : entry.title();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Entry entry = entryAt(mouseX, mouseY);
        if (entry == null) return false;
        onSelect.accept(entry);
        return true;
    }
}
