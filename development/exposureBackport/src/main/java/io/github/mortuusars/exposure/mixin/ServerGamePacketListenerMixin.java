package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.inventory.ItemRenameMenu;
import io.github.mortuusars.exposure.world.item.BrokenInterplanarProjectorItem;
import io.github.mortuusars.exposure.world.item.InterplanarProjectorItem;
import net.minecraft.SharedConstants;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerMixin {
   @Shadow
   public ServerPlayer player;

   @Inject(method = "handleRenameItem(Lnet/minecraft/network/protocol/game/ServerboundRenameItemPacket;)V", at = @At("HEAD"), cancellable = true)
   private void handleRename(ServerboundRenameItemPacket packet, CallbackInfo ci) {
      PacketUtils.ensureRunningOnSameThread(packet, (ServerGamePacketListenerImpl)(Object)this, this.player.getLevel());
      if (this.player.containerMenu instanceof ItemRenameMenu itemRenameMenu) {
         if (!itemRenameMenu.stillValid(this.player)) {
            Exposure.LOGGER.debug("Player {} interacted with invalid menu {}", this.player, itemRenameMenu);
         } else {
            itemRenameMenu.setItemName(packet.getName());
         }

         ci.cancel();
      } else if (this.player.containerMenu instanceof AnvilMenu anvilMenu && exposure$allowsLongerName(anvilMenu)) {
         // Port of upstream's AnvilMenuMixin: 1.19.2 applies the 50 character limit here, not in AnvilMenu.
         if (anvilMenu.stillValid(this.player)) {
            String filtered = SharedConstants.filterText(packet.getName());
            if (filtered.length() <= 150) {
               anvilMenu.setItemName(filtered);
            }
         }

         ci.cancel();
      }
   }

   private static boolean exposure$allowsLongerName(AnvilMenu menu) {
      if (!Config.Server.SPEC.isLoaded() || !Config.Server.INTERPLANAR_PROJECTOR_LARGER_RENAMING_LIMIT.get()) {
         return false;
      }
      Item input = menu.getSlot(0).getItem().getItem();
      return input instanceof InterplanarProjectorItem || input instanceof BrokenInterplanarProjectorItem;
   }
}
