package io.github.mortuusars.exposure.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Port of upstream ItemInHandRendererMixin's shadows: the photograph rendering itself runs from Forge's
 * RenderHandEvent (forge.client.FirstPersonPhotographRenderer).
 */
@Mixin(ItemInHandRenderer.class)
public interface ItemInHandRendererAccessor {
   @Accessor("mainHandItem")
   ItemStack exposure$getMainHandItem();

   @Accessor("offHandItem")
   ItemStack exposure$getOffHandItem();

   @Invoker("renderPlayerArm")
   void exposure$renderPlayerArm(PoseStack poseStack, MultiBufferSource buffer, int packedLight, float equippedProgress, float swingProgress, HumanoidArm side);

   @Invoker("calculateMapTilt")
   float exposure$calculateMapTilt(float pitch);

   @Invoker("renderMapHand")
   void exposure$renderMapHand(PoseStack poseStack, MultiBufferSource buffer, int packedLight, HumanoidArm side);
}
