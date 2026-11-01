package net.satisfy.foundation.storage;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

/** Renders the items of one storage type. Pose stack is already rotated to the facing. */
public interface StorageTypeRenderer {
    void render(StorageBlockEntity var1, PoseStack var2, MultiBufferSource var3, NonNullList<ItemStack> var4);
}
