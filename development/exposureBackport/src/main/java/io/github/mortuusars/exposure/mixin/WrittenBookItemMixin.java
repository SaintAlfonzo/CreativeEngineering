package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.world.item.AlbumItem;
import io.github.mortuusars.exposure.world.item.SignedAlbumItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WrittenBookItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Port of upstream LecternBlockEntityMixin#onGetPageCount: 1.19.2's lectern counts pages with
 * {@code WrittenBookItem.getPageCount} instead of its own static method.
 */
@Mixin(WrittenBookItem.class)
public abstract class WrittenBookItemMixin {
   @Inject(method = "getPageCount(Lnet/minecraft/world/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
   private static void onGetPageCount(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
      if (stack.getItem() instanceof AlbumItem albumItem) {
         cir.setReturnValue(albumItem.getContent(stack).pages().size());
      } else if (stack.getItem() instanceof SignedAlbumItem signedAlbumItem) {
         cir.setReturnValue(signedAlbumItem.getContent(stack).pages().size());
      }
   }
}
