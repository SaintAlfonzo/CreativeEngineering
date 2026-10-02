package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.event.ServerEvents;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Port: the camera set/remove overrides and the tick hook of upstream's version are in {@link PlayerMixin}. */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
   @Inject(method = "drop(Z)Z", at = @At("HEAD"))
   void onDrop(boolean dropStack, CallbackInfoReturnable<Boolean> cir) {
      ServerEvents.itemDrop((ServerPlayer)(Object)this);
   }
}
