package io.github.mortuusars.exposure.world.inventory;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.CameraId;
import io.github.mortuusars.exposure.world.inventory.slot.FilteredSlot;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import net.minecraft.network.FriendlyByteBuf;

public class CameraInHandAttachmentsMenu extends AbstractCameraAttachmentsMenu {
   protected final int cameraSlotIndex;
   protected final boolean openedFromGui;

   public CameraInHandAttachmentsMenu(int containerId, Inventory playerInventory, int cameraSlotIndex, boolean openedFromGui) {
      super(
         Exposure.MenuTypes.CAMERA_IN_HAND.get(),
         containerId,
         playerInventory,
         new CameraInHandAttachmentsMenu.InventoryCameraAccess(playerInventory, cameraSlotIndex)
      );
      this.cameraSlotIndex = cameraSlotIndex;
      this.openedFromGui = openedFromGui;
   }

   public boolean isOpenedFromGui() {
      return this.openedFromGui;
   }

   @Override
   protected void addPlayerSlots(Inventory playerInventory) {
      for (int row = 0; row < 3; row++) {
         for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(playerInventory, column + row * 9 + 9, column * 18 + 8, 103 + row * 18) {
               public boolean mayPickup(@NotNull Player player) {
                  return super.mayPickup(player) && this.getContainerSlot() != CameraInHandAttachmentsMenu.this.cameraSlotIndex;
               }

               public boolean isActive() {
                  return this.getContainerSlot() != CameraInHandAttachmentsMenu.this.cameraSlotIndex;
               }

               public boolean isHighlightable() {
                  return this.getContainerSlot() != CameraInHandAttachmentsMenu.this.cameraSlotIndex;
               }
            });
         }
      }

      for (int slot = 0; slot < 9; slot++) {
         int finalSlot = slot;
         this.addSlot(new Slot(playerInventory, finalSlot, slot * 18 + 8, 161) {
            public boolean mayPickup(@NotNull Player player) {
               return super.mayPickup(player) && this.getContainerSlot() != CameraInHandAttachmentsMenu.this.cameraSlotIndex;
            }

            public boolean isActive() {
               return this.getContainerSlot() != CameraInHandAttachmentsMenu.this.cameraSlotIndex;
            }

            public boolean isHighlightable() {
               return this.getContainerSlot() != CameraInHandAttachmentsMenu.this.cameraSlotIndex;
            }
         });
      }
   }

   @Override
   protected void onContainerChanged(Container c) {
      super.onContainerChanged(c);
      if (!this.player.level.isClientSide() && this.player.isCreative()) {
         this.player.getInventory().setItem(this.cameraSlotIndex, this.getCameraStack());
      }
   }

   @Override
   protected void onItemInSlotChanged(FilteredSlot.SlotChangedArgs args) {
      super.onItemInSlotChanged(args);
      if (!this.player.level.isClientSide() && this.player.isCreative()) {
         this.player.getInventory().setItem(this.cameraSlotIndex, this.getCameraStack());
      }
   }

   public void clicked(int slotId, int button, ClickType clickType, Player player) {
      if (clickType != ClickType.SWAP || this.cameraSlotIndex != button) {
         super.clicked(slotId, button, clickType, player);
      }
   }

   @Override
   public void removed(Player player) {
      super.removed(player);
      player.inventoryMenu.resumeRemoteUpdates();
   }

   public boolean stillValid(@NotNull Player player) {
      return CameraId.ofStack(this.getCameraStack()).matches(player.getInventory().getItem(this.cameraSlotIndex));
   }

   public static CameraInHandAttachmentsMenu fromBuffer(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
      return new CameraInHandAttachmentsMenu(containerId, playerInventory, buffer.readInt(), buffer.readBoolean());
   }

   public static class InventoryCameraAccess implements AbstractCameraAttachmentsMenu.CameraAccess {
      protected final Inventory inventory;
      protected final int slot;

      public InventoryCameraAccess(Inventory inventory, int slot) {
         this.inventory = inventory;
         this.slot = slot;
         Preconditions.checkState(
            this.getStack().getItem() instanceof CameraItem, "Failed to open access the camera. " + this.getStack() + " is not a CameraItem."
         );
      }

      @Override
      public ItemStack getStack() {
         return this.inventory.getItem(this.slot);
      }
   }
}
