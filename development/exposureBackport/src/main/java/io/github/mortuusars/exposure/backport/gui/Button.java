package io.github.mortuusars.exposure.backport.gui;

import java.util.function.Supplier;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

/** Backport of the 1.19.3+ {@code Button} (builder, narration supplier, tooltip). */
public class Button extends AbstractButton {
    public static final int SMALL_WIDTH = 120;
    public static final int DEFAULT_WIDTH = 150;
    public static final int DEFAULT_HEIGHT = 20;
    protected static final CreateNarration DEFAULT_NARRATION = Supplier::get;

    protected final OnPress onPress;
    protected final CreateNarration createNarration;

    public static Builder builder(Component message, OnPress onPress) {
        return new Builder(message, onPress);
    }

    protected Button(int x, int y, int width, int height, Component message, OnPress onPress, CreateNarration createNarration) {
        super(x, y, width, height, message);
        this.onPress = onPress;
        this.createNarration = createNarration;
    }

    @Override
    public void onPress() {
        this.onPress.onPress(this);
    }

    @Override
    protected MutableComponent createNarrationMessage() {
        return this.createNarration.createNarrationMessage(super::createNarrationMessage);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    public static class Builder {
        private final Component message;
        private final OnPress onPress;
        @Nullable
        private Tooltip tooltip;
        private int x;
        private int y;
        private int width = 150;
        private int height = 20;
        private CreateNarration createNarration = Button.DEFAULT_NARRATION;

        public Builder(Component message, OnPress onPress) {
            this.message = message;
            this.onPress = onPress;
        }

        public Builder pos(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder bounds(int x, int y, int width, int height) {
            return this.pos(x, y).size(width, height);
        }

        public Builder tooltip(@Nullable Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public Builder createNarration(CreateNarration createNarration) {
            this.createNarration = createNarration;
            return this;
        }

        public Button build() {
            Button button = new Button(this.x, this.y, this.width, this.height, this.message, this.onPress, this.createNarration);
            button.setTooltip(this.tooltip);
            return button;
        }
    }

    @FunctionalInterface
    public interface CreateNarration {
        MutableComponent createNarrationMessage(Supplier<MutableComponent> defaultNarration);
    }

    @FunctionalInterface
    public interface OnPress {
        void onPress(Button button);
    }
}
