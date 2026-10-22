package com.SaintAlfonzo.creativeengineering.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import java.util.Random;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.JarBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.common.block.tiles.JarBlockTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixed jars ({@code SupplementariesJarTileMixin}): a placed jar draws each slot's own item as a layer. Supplementaries
 * draws {@code getDisplayedItem()} (slot 0) once per count; here its count reads 0 so that loop draws nothing, and
 * the HEAD inject draws the layers with the same transforms and the same per-position random turn of each layer.
 */
@Mixin(value = JarBlockTileRenderer.class, remap = false)
public abstract class SupplementariesJarRendererMixin {

    @Inject(method = "render(Lnet/mehvahdjukaar/supplementaries/common/block/tiles/JarBlockTile;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At("HEAD"), remap = false)
    private void creativeengineering$renderLayers(JarBlockTile tile, float partialTicks, PoseStack pose,
                                                  MultiBufferSource buffer, int light, int overlay, CallbackInfo ci) {
        Container jar = (Container) tile;
        if (jar.isEmpty()) {
            return;
        }
        Random rand = new Random(tile.getBlockPos().asLong());
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(RotHlpr.XN90);
        pose.translate(0.0, 0.0, -0.5);
        float scale = 0.5714286F;
        pose.scale(scale, scale, scale);
        for (int i = 0; i < jar.getContainerSize(); i++) {
            ItemStack stack = jar.getItem(i);
            for (int n = 0; n < stack.getCount(); n++) {
                pose.mulPose(Vector3f.ZP.rotationDegrees(rand.nextInt(16) * 22.5F));
                pose.translate(0.0, 0.0, 1.0F / (16.0F * scale));
                BakedModel model = itemRenderer.getModel(stack, tile.getLevel(), null, 0);
                itemRenderer.render(stack, ItemTransforms.TransformType.FIXED, true, pose, buffer, light, overlay, model);
            }
        }
        pose.popPose();
    }

    @Redirect(method = "render(Lnet/mehvahdjukaar/supplementaries/common/block/tiles/JarBlockTile;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;m_41613_()I"), remap = false)
    private int creativeengineering$skipOwnLoop(ItemStack stack) {
        return 0;
    }
}
