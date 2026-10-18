package net.satisfy.foundation.block;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/** Connection state of a {@link LineConnectingBlock}. */
public enum LineConnectingType implements StringRepresentable {
    NONE("none"),
    MIDDLE("middle"),
    LEFT("left"),
    RIGHT("right");

    private final String name;

    LineConnectingType(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }
}
