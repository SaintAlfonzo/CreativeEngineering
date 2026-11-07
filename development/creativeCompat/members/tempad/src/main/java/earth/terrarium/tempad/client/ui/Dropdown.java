// Behaviour translated from Olympus 1.0.18 (MIT, Terrarium): earth.terrarium.olympus.client.components.dropdown.DropdownBuilder,
// Widgets.dropdown and ui.context.ContextMenu (the option list, drawn as a Popup layer).
package earth.terrarium.tempad.client.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class Dropdown<T> {
    public static final ResourceLocation OLYMPUS_LIST_BG = Button.olympus("lists/background");
    public static final WidgetSprites OLYMPUS_LIST_ENTRY = new WidgetSprites(Button.olympus("lists/entry/normal"), Button.olympus("lists/entry/normal"), Button.olympus("lists/entry/hovered"));

    private final DropdownState<T> state;
    private final List<T> options = new ArrayList<>();
    private ResourceLocation background = OLYMPUS_LIST_BG;
    @Nullable
    private WidgetSprites entrySprites = OLYMPUS_LIST_ENTRY;
    private Function<T, WidgetRenderer> entryRenderer;
    private Consumer<T> action = t -> {
    };
    private int width = -1;
    private int height = 150;
    private int entryHeight = 20;
    private boolean topRight;

    private Dropdown(DropdownState<T> state, Function<T, Component> names) {
        this.state = state;
        this.entryRenderer = t -> Renderers.text(names.apply(t));
    }

    /** Olympus Widgets.dropdown(state, options, optionText, buttonFactory, dropdownFactory). */
    public static <T> Button create(DropdownState<T> state, List<T> options, Function<T, Component> names, Consumer<Button> buttonFactory, Consumer<Dropdown<T>> dropdownFactory) {
        Button button = new Button();
        buttonFactory.accept(button);
        state.setButton(button);
        Dropdown<T> dropdown = new Dropdown<>(state, names);
        dropdown.options.addAll(options);
        dropdownFactory.accept(dropdown);
        return dropdown.build();
    }

    public Dropdown<T> withTexture(ResourceLocation background) {
        this.background = background;
        return this;
    }

    public Dropdown<T> withEntrySprites(@Nullable WidgetSprites sprites) {
        this.entrySprites = sprites;
        return this;
    }

    public Dropdown<T> withEntryHeight(int height) {
        this.entryHeight = height;
        return this;
    }

    public Dropdown<T> withEntryRenderer(Function<T, WidgetRenderer> renderer) {
        this.entryRenderer = renderer;
        return this;
    }

    public Dropdown<T> withCallback(Consumer<T> action) {
        this.action = action;
        return this;
    }

    /** DropdownBuilder.withAlignment(OverlayAlignment.TOP_RIGHT): the list opens above the button, right edges aligned. */
    public Dropdown<T> withTopRightAlignment() {
        this.topRight = true;
        return this;
    }

    /** DropdownBuilder.withSize(width, maxHeight). */
    public Dropdown<T> withSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    private Button build() {
        Button button = state.getButton();
        return button.withCallback(() -> {
            int entryWidth = width == -1 ? button.getWidth() : width;
            state.setOpened(true);
            List<Button> entries = new ArrayList<>();
            for (T option : options) {
                Button entry = new Button().withTexture(entrySprites).withRenderer(entryRenderer.apply(option)).withCallback(() -> {
                    state.set(option);
                    action.accept(option);
                });
                entry.withSize(entryWidth, entryHeight);
                entries.add(entry);
            }
            Popup.open(new ContextMenu(button, entries, entryWidth, height, background, topRight, () -> state.setOpened(false)));
        });
    }

    /**
     * Olympus ContextMenu: the list under the button (OverlayAlignment.BOTTOM_LEFT, the default) or above it with the
     * right edges aligned (TOP_RIGHT), closed by any click.
     */
    static final class ContextMenu extends PopupBase {
        private final List<Button> entries;
        private final ResourceLocation texture;
        private final Runnable onClose;
        private final int x, y, width, height, listHeight, contentHeight;
        private double scroll;

        ContextMenu(Button anchor, List<Button> entries, int maxWidth, int maxHeight, ResourceLocation texture, boolean topRight, Runnable onClose) {
            this.entries = entries;
            this.texture = texture;
            this.onClose = onClose;
            int contentWidth = entries.stream().mapToInt(Button::getWidth).max().orElse(0);
            this.contentHeight = entries.stream().mapToInt(Button::getHeight).sum();
            this.width = maxWidth > 0 ? Math.min(maxWidth, contentWidth + 4) : contentWidth + 4;
            this.height = maxHeight > 0 ? Math.min(maxHeight, contentHeight + 3) : contentHeight + 3;
            this.listHeight = height - 3 - (maxHeight > 0 && maxHeight < contentHeight ? 1 : 0);
            this.x = topRight ? anchor.x + anchor.getWidth() - width : anchor.x;
            this.y = topRight ? anchor.y - height : anchor.y + anchor.getHeight();
            for (Button entry : entries) {
                entry.setWidth(width - 4);
                add(entry);
            }
        }

        private void layout() {
            int ey = y + 2 - (int) scroll;
            for (Button entry : entries) {
                entry.setPosition(x + 2, ey);
                ey += entry.getHeight();
            }
        }

        @Override
        public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
            RenderSystem.enableBlend();
            Sprites.blit(pose, texture, x, y, width, height);
            layout();
            Gfx.enableScissor(x + 2, y + 2, x + width - 2, y + 2 + listHeight);
            renderWidgets(pose, mouseX, mouseY, partialTick);
            Gfx.disableScissor();
        }

        private boolean overList(double mx, double my) {
            return mx >= x + 2 && mx < x + width - 2 && my >= y + 2 && my < y + 2 + listHeight;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (overList(mouseX, mouseY)) {
                layout();
                clickWidgets(mouseX, mouseY, button);
            }
            close();
            return true;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
            scroll = Math.max(0, Math.min(Math.max(0, contentHeight - listHeight), scroll - delta * 10));
            return true;
        }

        @Override
        public void onClosed() {
            onClose.run();
        }
    }
}
