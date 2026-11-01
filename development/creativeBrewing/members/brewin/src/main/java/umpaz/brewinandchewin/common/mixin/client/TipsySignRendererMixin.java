package umpaz.brewinandchewin.common.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.function.Function;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import umpaz.brewinandchewin.client.utility.BnCClientTextUtils;
import vectorwing.farmersdelight.client.renderer.CanvasSignRenderer;

/**
 * Port: 1.19.2 signs cache their split lines (SignBlockEntity#getRenderMessages). While the viewer is Tipsy enough the
 * lines are built fresh each frame from scrambled text and the cache is left alone, so signs read normally again once
 * the effect drops.
 */
public class TipsySignRendererMixin {
   static FormattedCharSequence[] brewinandchewin$scrambled(SignBlockEntity sign, boolean filtered, Function<Component, FormattedCharSequence> splitter) {
      FormattedCharSequence[] lines = new FormattedCharSequence[4];
      for (int i = 0; i < 4; i++) {
         lines[i] = splitter.apply(BnCClientTextUtils.signLine(sign.getMessage(i, filtered)));
      }
      return lines;
   }

   @Mixin(SignRenderer.class)
   public static class TipsySignRenderMixin {
      @WrapOperation(
         method = "render(Lnet/minecraft/world/level/block/entity/SignBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
         at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/SignBlockEntity;getRenderMessages(ZLjava/util/function/Function;)[Lnet/minecraft/util/FormattedCharSequence;")
      )
      private FormattedCharSequence[] brewinandchewin$renderSignText(
         SignBlockEntity sign, boolean filtered, Function<Component, FormattedCharSequence> splitter, Operation<FormattedCharSequence[]> original
      ) {
         return BnCClientTextUtils.shouldScrambleSigns() ? brewinandchewin$scrambled(sign, filtered, splitter) : original.call(sign, filtered, splitter);
      }
   }

   @Mixin(CanvasSignRenderer.class)
   public static class TipsyCanvasSignRenderMixin {
      @WrapOperation(
         method = "render(Lnet/minecraft/world/level/block/entity/SignBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
         at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/SignBlockEntity;getRenderMessages(ZLjava/util/function/Function;)[Lnet/minecraft/util/FormattedCharSequence;")
      )
      private FormattedCharSequence[] brewinandchewin$renderSignText(
         SignBlockEntity sign, boolean filtered, Function<Component, FormattedCharSequence> splitter, Operation<FormattedCharSequence[]> original
      ) {
         return BnCClientTextUtils.shouldScrambleSigns() ? brewinandchewin$scrambled(sign, filtered, splitter) : original.call(sign, filtered, splitter);
      }
   }
}
