package com.SaintAlfonzo.creativedelight.mixin.cookingforblockheads;

import com.SaintAlfonzo.creativedelight.compat.cookingforblockheads.client.FridgeDouble;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.blay09.mods.cookingforblockheads.client.render.FridgeRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * The Cooking for Blockheads fridge door of a double fridge reaches the block edge on the joined side
 * (client/FridgeDouble, TODO cooking/kitchen #36). CfB draws the door (and the upper door half one block up) with the
 * fridge's lower block state; the strip is drawn in the same pose, so it swings with the door.
 */
@Mixin(value = FridgeRenderer.class, remap = false)
public abstract class CfbFridgeRendererMixin {

    @ModifyArg(method = "renderDoor", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;m_234379_(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;JI)V"),
            index = 1)
    private BakedModel ce$joinDoor(BlockAndTintGetter level, BakedModel model, BlockState state, BlockPos pos,
                                   PoseStack pose, VertexConsumer consumer, boolean checkSides, RandomSource random,
                                   long seed, int overlay) {
        return FridgeDouble.door(model, level, state, pos);
    }
}
