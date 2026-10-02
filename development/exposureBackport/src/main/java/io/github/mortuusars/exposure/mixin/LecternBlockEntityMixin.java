package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.world.item.AlbumItem;
import io.github.mortuusars.exposure.world.item.SignedAlbumItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LecternBlockEntity.class})
public abstract class LecternBlockEntityMixin {
   @Shadow
   public abstract ItemStack getBook();

   @Inject(
      method = {"hasBook()Z"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onHasBook(CallbackInfoReturnable<Boolean> cir) {
      if (this.getBook().getItem() instanceof AlbumItem || this.getBook().getItem() instanceof SignedAlbumItem) {
         cir.setReturnValue(true);
      }
   }
}
