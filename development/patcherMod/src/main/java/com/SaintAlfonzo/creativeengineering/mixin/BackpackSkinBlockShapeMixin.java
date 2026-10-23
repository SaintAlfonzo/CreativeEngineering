package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.backpackskin.BackpackSkins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlock;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** A placed skinned backpack uses the skin block's shape for both outline and collision. */
@Mixin(value = BackpackBlock.class, remap = false)
public abstract class BackpackSkinBlockShapeMixin {

    @Inject(method = "m_5940_", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private void creativeengineering$skinShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx,
            CallbackInfoReturnable<VoxelShape> cir) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof BackpackBlockEntity bpe)) return;
        Block skin = BackpackSkins.getSkin(bpe.getBackpackWrapper().getBackpack());
        if (skin != null) cir.setReturnValue(BackpackSkins.skinState(skin, state).getShape(level, pos, ctx));
    }
}
