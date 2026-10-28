package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.worldgen.FirstTickWater;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

/**
 * Every block drop goes through the private {@code Block.popResource(Level, Supplier, ItemStack)} (m_152440_): water's
 * {@code beforeDestroyingBlock} and {@code destroyBlock} of plants losing support both end here. HEAD skips the drop while
 * a settling fluid tick runs ({@link FirstTickWater}).
 */
@Mixin(value = Block.class, remap = false)
public abstract class BlockPopResourceSettleMixin {

    @Inject(method = "m_152440_", at = @At("HEAD"), cancellable = true, remap = false)
    private static void creativeengineering$skipSettleDrop(Level level, Supplier<ItemEntity> entity, ItemStack stack,
                                                          CallbackInfo ci) {
        if (FirstTickWater.suppressDrops()) ci.cancel();
    }
}
