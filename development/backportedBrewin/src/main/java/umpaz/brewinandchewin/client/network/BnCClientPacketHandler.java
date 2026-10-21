package umpaz.brewinandchewin.client.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.gui.KegScreen;
import umpaz.brewinandchewin.client.utility.BnCClientTextUtils;
import umpaz.brewinandchewin.common.attachment.RagingAttachment;
import umpaz.brewinandchewin.common.attachment.TipsyHeartsAttachment;
import umpaz.brewinandchewin.common.network.clientbound.MakeNextPlayerChatTipsyClientboundPacket;
import umpaz.brewinandchewin.common.network.clientbound.SyncNumbedHeartsClientboundPacket;
import umpaz.brewinandchewin.common.network.clientbound.SyncRagingStacksClientboundPacket;

/**
 * Port: the client halves of upstream's clientbound packet handle() methods. Runs on the client main thread. Upstream's
 * SendRecipeBookValues packet is gone: 1.19.2's own recipe packet already carries every RecipeBookType's settings.
 */
public final class BnCClientPacketHandler {
   private BnCClientPacketHandler() {
   }

   public static void clearKegFluidContainerComponents() {
      KegScreen.clearFluidContainerComponents();
   }

   public static void makeNextPlayerChatTipsy(MakeNextPlayerChatTipsyClientboundPacket packet) {
      BnCClientTextUtils.tipsyMessageLevel = packet.level();
      BnCClientTextUtils.randomSeed = packet.randomSeed();
      BnCClientTextUtils.clearDelayAmount = packet.clearDelayAmount();
      BnCClientTextUtils.generatedRandom = true;
   }

   public static void syncNumbedHearts(SyncNumbedHeartsClientboundPacket packet) {
      if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getEntity(packet.entityId()) instanceof LivingEntity living) {
         BrewinAndChewin.getHelper()
            .setTipsyHeartsAttachment(living, packet.numbedHealth() < 1.0E-5F ? null : new TipsyHeartsAttachment(packet.numbedHealth(), packet.ticksUntilDamage()));
      }
   }

   public static void syncRagingStacks(SyncRagingStacksClientboundPacket packet) {
      if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getEntity(packet.entityId()) instanceof LivingEntity living) {
         BrewinAndChewin.getHelper().setRagingAttachment(living, packet.stacks().map(i -> new RagingAttachment(i, 0)).orElse(null));
      }
   }
}
