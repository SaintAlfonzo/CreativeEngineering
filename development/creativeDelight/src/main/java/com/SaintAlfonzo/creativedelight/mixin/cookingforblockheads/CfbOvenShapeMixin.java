package com.SaintAlfonzo.creativedelight.mixin.cookingforblockheads;

import net.blay09.mods.cookingforblockheads.block.BlockKitchen;
import net.blay09.mods.cookingforblockheads.block.OvenBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Cooking for Blockheads oven's cook surface is drawn at the top of the block (client/OvenRaise), so its outline and
 * collision box go to 16 px as well (CfB's kitchen box stops at 15). Other kitchen blocks keep CfB's box.
 */
@Mixin(value = BlockKitchen.class, remap = false)
public abstract class CfbOvenShapeMixin {

    private static final Map<VoxelShape, VoxelShape> CE$RAISED = new ConcurrentHashMap<>();

    @Inject(method = "m_5940_", at = @At("RETURN"), cancellable = true)
    private void ce$raiseOven(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context,
                              CallbackInfoReturnable<VoxelShape> cir) {
        if (!((Object) this instanceof OvenBlock)) {
            return;
        }
        VoxelShape shape = cir.getReturnValue();
        if (shape.isEmpty()) {
            return;
        }
        cir.setReturnValue(CE$RAISED.computeIfAbsent(shape, s -> {
            AABB box = s.bounds();
            return box.maxY >= 1.0 ? s : Shapes.create(box.minX, box.minY, box.minZ, box.maxX, 1.0, box.maxZ);
        }));
    }
}
