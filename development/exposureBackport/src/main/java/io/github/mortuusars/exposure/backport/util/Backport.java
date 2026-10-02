package io.github.mortuusars.exposure.backport.util;

import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;

/**
 * Vanilla 1.19.3-1.21 methods that 1.19.2 lacks, as statics. Common code: nothing here may touch client classes
 * (even a LocalPlayer passed as a Player makes the verifier load it on a dedicated server) - those go in
 * {@link io.github.mortuusars.exposure.backport.client.ClientBackport}. tools/port_upstream.py rewrites upstream's instance
 * calls to these ({@code stack.copyWithCount(n)} -> {@code Backport.copyWithCount(stack, n)}), so each keeps
 * vanilla's 1.21 behaviour.
 */
public final class Backport {
    private Backport() {
    }

    // ---- ItemStack ----

    /** {@code stack.copyWithCount(count)} */
    public static ItemStack copyWithCount(ItemStack stack, int count) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = stack.copy();
        copy.setCount(count);
        return copy;
    }

    /** {@code stack.transmuteCopy(item)}: same count and data, different item. */
    public static ItemStack transmuteCopy(ItemStack stack, ItemLike item) {
        return transmuteCopy(stack, item, stack.getCount());
    }

    public static ItemStack transmuteCopy(ItemStack stack, ItemLike item, int count) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = new ItemStack(item, count);
        if (stack.getTag() != null) {
            result.setTag(stack.getTag().copy());
        }
        return result;
    }

    /** {@code ItemStack.isSameItemSameComponents(a, b)} */
    public static boolean isSameItemSameComponents(ItemStack a, ItemStack b) {
        return ItemStack.isSameItemSameTags(a, b);
    }

    /** {@code ItemStack.hashItemAndComponents(stack)} */
    public static int hashItemAndComponents(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        return 31 * stack.getItem().hashCode() + Objects.hashCode(stack.getTag());
    }

    /** {@code ItemStack.listMatches(a, b)}: same size, and pairwise same count, item and data. */
    public static boolean listMatches(List<ItemStack> a, List<ItemStack> b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            if (!ItemStack.matches(a.get(i), b.get(i))) {
                return false;
            }
        }
        return true;
    }

    /** {@code ItemStack.hashStackList(list)} */
    public static int hashStackList(List<ItemStack> list) {
        int hash = 0;
        for (ItemStack stack : list) {
            hash = hash * 31 + (stack.isEmpty() ? 0 : 31 * hashItemAndComponents(stack) + stack.getCount());
        }
        return hash;
    }

    /** {@code ItemStack.parse(registries, tag)}: empty Optional for an empty/unreadable stack. */
    public static java.util.Optional<ItemStack> parseItemStack(net.minecraft.nbt.Tag tag) {
        if (!(tag instanceof net.minecraft.nbt.CompoundTag compound)) {
            return java.util.Optional.empty();
        }
        ItemStack stack = ItemStack.of(compound);
        return stack.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(stack);
    }

    /** {@code ItemStack.parseOptional(registries, tag)} */
    public static ItemStack parseItemStackOptional(net.minecraft.nbt.CompoundTag tag) {
        return tag.isEmpty() ? ItemStack.EMPTY : ItemStack.of(tag);
    }

    /** {@code aabb.distanceToSqr(point)} (1.20.5+): squared distance from the point to the box, 0 inside. */
    public static double distanceToSqr(net.minecraft.world.phys.AABB box, net.minecraft.world.phys.Vec3 point) {
        double dx = Math.max(Math.max(box.minX - point.x, point.x - box.maxX), 0.0);
        double dy = Math.max(Math.max(box.minY - point.y, point.y - box.maxY), 0.0);
        double dz = Math.max(Math.max(box.minZ - point.z, point.z - box.maxZ), 0.0);
        return dx * dx + dy * dy + dz * dz;
    }

    // ---- Entity ----

    /**
     * {@code entityData.set(key, value, force)} (1.20.2+). With {@code force}, the value is re-sent to clients even when
     * it's the same instance - needed for an ItemStack mutated in place (1.19.2's set() compares by identity and
     * skips it).
     */
    public static <T> void setEntityData(net.minecraft.network.syncher.SynchedEntityData data,
                                         net.minecraft.network.syncher.EntityDataAccessor<T> key, T value, boolean force) {
        io.github.mortuusars.exposure.mixin.SynchedEntityDataAccessor accessor =
                (io.github.mortuusars.exposure.mixin.SynchedEntityDataAccessor) data;
        net.minecraft.network.syncher.SynchedEntityData.DataItem<T> item = accessor.invokeGetItem(key);
        if (force || !Objects.equals(value, item.getValue())) {
            item.setValue(value);
            accessor.getEntity().onSyncedDataUpdated(key);
            item.setDirty(true);
            accessor.setDirty(true);
        }
    }

    // ---- misc ----

    /** {@code Mth.lerpInt(delta, start, end)} (1.19.4+) */
    public static int lerpInt(float delta, int start, int end) {
        return start + net.minecraft.util.Mth.floor(delta * (float) (end - start));
    }

    /** {@code list.getLast()} (Java 21 SequencedCollection) */
    public static <T> T getLast(List<T> list) {
        if (list.isEmpty()) {
            throw new java.util.NoSuchElementException();
        }
        return list.get(list.size() - 1);
    }


    /** {@code dyeColor.getTextureDiffuseColor()}: 1.20.5+ returns opaque ARGB (1.19.2 has the float[] RGB). */
    public static int getTextureDiffuseColor(DyeColor color) {
        float[] rgb = color.getTextureDiffuseColors();
        return 0xFF000000 | ((int) (rgb[0] * 255.0F) << 16) | ((int) (rgb[1] * 255.0F) << 8) | (int) (rgb[2] * 255.0F);
    }

    /** {@code biome.getPrecipitationAt(pos)} (1.19.4+) */
    public static Biome.Precipitation getPrecipitationAt(Biome biome, BlockPos pos) {
        if (biome.getPrecipitation() == Biome.Precipitation.NONE) {
            return Biome.Precipitation.NONE;
        }
        return biome.coldEnoughToSnow(pos) ? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN;
    }
}
