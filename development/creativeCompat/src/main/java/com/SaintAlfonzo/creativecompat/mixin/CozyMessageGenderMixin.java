package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.cats.CozyCats;
import net.cozy.cozysimprovedcats.util.MessageUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Cats have no gender ({@link CozyCats}): the cat stats chat skips its "Gender: Unknown" line. */
@Mixin(value = MessageUtil.class, remap = false)
public abstract class CozyMessageGenderMixin {

    @Inject(method = "send(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/network/chat/Component;Z)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private static void creativecompat$noGenderLine(Player player, Component message, boolean overlay, CallbackInfo ci) {
        if (CozyCats.isGenderLine(message)) {
            ci.cancel();
        }
    }
}
