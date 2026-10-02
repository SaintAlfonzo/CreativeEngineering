package io.github.mortuusars.exposure.world.inventory;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;

public class CameraOnStandAttachmentsMenu extends AbstractCameraAttachmentsMenu {
   protected final CameraStandEntity stand;

   public CameraOnStandAttachmentsMenu(int containerId, Inventory playerInventory, CameraStandEntity stand) {
      super(Exposure.MenuTypes.CAMERA_ON_STAND.get(), containerId, playerInventory, new CameraOnStandAttachmentsMenu.StandCameraAccess(stand));
      this.stand = stand;
   }

   @Override
   protected void onContainerChanged(Container c) {
      super.onContainerChanged(c);
      if (!this.stand.isClientSide()) {
         this.stand.forceUpdate();
      }
   }

   public boolean stillValid(Player player) {
      return this.stand.getCamera().getItem() instanceof CameraItem && this.stand.isInInteractionRange(player);
   }

   public static CameraOnStandAttachmentsMenu fromBuffer(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
      if (playerInventory.player.level.getEntity(buffer.readInt()) instanceof CameraStandEntity stand) {
         return new CameraOnStandAttachmentsMenu(containerId, playerInventory, stand);
      } else {
         throw new IllegalStateException("Cannot open attachments on stand: Camera Stand entity does not exist on client.");
      }
   }

   public static class StandCameraAccess implements AbstractCameraAttachmentsMenu.CameraAccess {
      protected final CameraStandEntity stand;

      public StandCameraAccess(CameraStandEntity stand) {
         this.stand = stand;
         Preconditions.checkState(
            this.getStack().getItem() instanceof CameraItem, "Failed to open access the camera. " + this.getStack() + " is not a CameraItem."
         );
      }

      @Override
      public ItemStack getStack() {
         return this.stand.getCamera();
      }
   }
}
