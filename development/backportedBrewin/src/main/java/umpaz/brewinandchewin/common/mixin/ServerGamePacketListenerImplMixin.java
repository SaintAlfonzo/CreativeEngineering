package umpaz.brewinandchewin.common.mixin;

import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.network.clientbound.MakeNextPlayerChatTipsyClientboundPacket;
import umpaz.brewinandchewin.common.registry.BnCEffects;

/**
 * Port (alcohol.md 4.3): before a Tipsy player's chat goes out, every player gets the sender's Tipsy level and one shared
 * seed. The message itself, its signature and the server log stay untouched; clients decide whether to scramble.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
   @Shadow
   public ServerPlayer player;

   @Inject(method = "broadcastChatMessage", at = @At("HEAD"))
   private void brewinandchewin$announceTipsyChat(PlayerChatMessage message, CallbackInfo ci) {
      MobEffectInstance tipsy = this.player.getEffect(BnCEffects.TIPSY);
      if (tipsy != null && this.player.getServer() != null) {
         long randomSeed = this.player.getRandom().nextLong();
         for (ServerPlayer otherPlayer : this.player.getServer().getPlayerList().getPlayers()) {
            BrewinAndChewin.getHelper().sendClientbound(otherPlayer, new MakeNextPlayerChatTipsyClientboundPacket(tipsy.getAmplifier(), randomSeed, 0));
         }
      }
   }
}
