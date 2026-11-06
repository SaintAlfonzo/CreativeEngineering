package com.SaintAlfonzo.creativedelight.mixin.cookingforblockheads;

import com.SaintAlfonzo.creativedelight.compat.cookingforblockheads.client.OvenRaise;
import net.blay09.mods.cookingforblockheads.client.render.OvenRenderer;
import net.minecraft.client.resources.model.BakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * The Cooking for Blockheads oven renderer follows the raised oven model (client/OvenRaise): the door and its handle,
 * which Balm bakes outside the model map, go through the same transform (door 1 px taller, hinge unchanged), and the
 * four tools on the cook surface sit 1 px higher.
 */
@Mixin(value = OvenRenderer.class, remap = false)
public abstract class CfbOvenRendererMixin {

    private static final String RENDER = "render(Lnet/blay09/mods/cookingforblockheads/tile/OvenBlockEntity;"
            + "FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V";

    @ModifyArg(method = RENDER, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;m_234379_(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;JI)V"),
            index = 1)
    private BakedModel ce$raiseDoor(BakedModel model) {
        return OvenRaise.door(model);
    }

    @ModifyConstant(method = RENDER, constant = @Constant(doubleValue = 1.05))
    private double ce$raiseTools(double y) {
        return y + OvenRaise.PIXEL;
    }
}
