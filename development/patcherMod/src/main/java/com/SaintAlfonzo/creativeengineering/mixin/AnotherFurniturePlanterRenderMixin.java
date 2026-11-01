package com.SaintAlfonzo.creativeengineering.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.SaintAlfonzo.creativeengineering.compat.PlantBoxModelsClient;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Another Furniture's planter box draws each plant with its Supplementaries flower box model when it has one (crops,
 * seeds, cactus, chorus, nether wart, tall flowers, glow berries, glow lichen), the same look as the Supplementaries
 * box; other plants draw as before ({@link PlantBoxModelsClient.renderSpecial}; flower_pots.md 3.2).
 */
@Pseudo
@Mixin(targets = "com.starfish_studios.another_furniture.client.renderer.blockentity.PlanterBoxRenderer", remap = false)
public abstract class AnotherFurniturePlanterRenderMixin {

    @Redirect(method = "render(Lcom/starfish_studios/another_furniture/block/entity/PlanterBoxBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            remap = false,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;m_110912_(Lnet/minecraft/world/level/block/state/BlockState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"))
    private void creativeengineering$plant(BlockRenderDispatcher dispatcher, BlockState state, PoseStack pose,
            MultiBufferSource buffers, int light, int overlay) {
        if (!PlantBoxModelsClient.renderSpecial(state, pose, buffers, light, overlay)) {
            dispatcher.renderSingleBlock(state, pose, buffers, light, overlay);
        }
    }
}
