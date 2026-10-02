package io.github.mortuusars.exposure.backport.gui;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

/** Backport of 1.19.3+ {@code net.minecraft.client.gui.components.Tooltip}: a widget's hover text. */
public class Tooltip {
    private static final int MAX_WIDTH = 170;
    private final Component message;
    @Nullable
    private final Component narration;
    @Nullable
    private List<FormattedCharSequence> cachedLines;

    private Tooltip(Component message, @Nullable Component narration) {
        this.message = message;
        this.narration = narration;
    }

    public static Tooltip create(Component message, @Nullable Component narration) {
        return new Tooltip(message, narration);
    }

    public static Tooltip create(Component message) {
        return new Tooltip(message, message);
    }

    public Component getMessage() {
        return message;
    }

    @Nullable
    public Component getNarration() {
        return narration;
    }

    public List<FormattedCharSequence> toCharSequence(Minecraft minecraft) {
        if (cachedLines == null) {
            cachedLines = minecraft.font.split(message, MAX_WIDTH);
        }
        return cachedLines;
    }
}
