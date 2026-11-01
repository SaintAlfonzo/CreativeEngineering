package umpaz.brewinandchewin.common.mixin;

import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.MessageArgument.ChatMessage;
import net.minecraft.server.commands.MsgCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.network.clientbound.MakeNextPlayerChatTipsyClientboundPacket;
import umpaz.brewinandchewin.common.registry.BnCEffects;

/**
 * /msg from a Tipsy player: each target gets the level and seed before its copy; the sender's client keeps them for one
 * echo per target (clearDelayAmount). Port: 1.19.2 sends the messages from a lambda, so this runs once at HEAD.
 */
@Mixin(MsgCommand.class)
public class MsgCommandMixin {
   @Inject(method = "sendMessage", at = @At("HEAD"))
   private static void brewinandchewin$announceTipsyMessage(
      CommandSourceStack source, Collection<ServerPlayer> targets, ChatMessage message, CallbackInfoReturnable<Integer> cir
   ) {
      ServerPlayer sender = source.getPlayer();
      MobEffectInstance tipsy = sender == null ? null : sender.getEffect(BnCEffects.TIPSY);
      if (tipsy == null || targets.isEmpty()) {
         return;
      }
      long seed = sender.getRandom().nextLong();
      BrewinAndChewin.getHelper().sendClientbound(sender, new MakeNextPlayerChatTipsyClientboundPacket(tipsy.getAmplifier(), seed, targets.size() - 1));
      for (ServerPlayer target : targets) {
         if (target != sender) {
            BrewinAndChewin.getHelper().sendClientbound(target, new MakeNextPlayerChatTipsyClientboundPacket(tipsy.getAmplifier(), seed, 0));
         }
      }
   }
}
