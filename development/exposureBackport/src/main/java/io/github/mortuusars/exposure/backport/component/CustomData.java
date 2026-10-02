package io.github.mortuusars.exposure.backport.component;

import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;

/** Backport of 1.20.5+ {@code net.minecraft.world.item.component.CustomData}: an immutable view of a compound tag. */
public final class CustomData {
    public static final CustomData EMPTY = new CustomData(new CompoundTag());

    private final CompoundTag tag;

    private CustomData(CompoundTag tag) {
        this.tag = tag;
    }

    public static CustomData of(CompoundTag tag) {
        return new CustomData(tag.copy());
    }

    public boolean contains(String key) {
        return tag.contains(key);
    }

    public boolean isEmpty() {
        return tag.isEmpty();
    }

    public CompoundTag copyTag() {
        return tag.copy();
    }

    /** The backing tag: don't modify it. */
    public CompoundTag getUnsafe() {
        return tag;
    }

    public CustomData update(Consumer<CompoundTag> updater) {
        CompoundTag copy = tag.copy();
        updater.accept(copy);
        return new CustomData(copy);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CustomData other && tag.equals(other.tag);
    }

    @Override
    public int hashCode() {
        return tag.hashCode();
    }

    @Override
    public String toString() {
        return tag.toString();
    }
}
