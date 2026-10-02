package io.github.mortuusars.exposure.world.inventory;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.block.entity.LightroomBlockEntity;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.item.DevelopedFilmItem;
import io.github.mortuusars.exposure.world.item.FilmItem;
import io.github.mortuusars.exposure.world.lightroom.PrintingMode;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.mortuusars.exposure.backport.util.Backport;

public class LightroomMenu extends AbstractContainerMenu {
   public static final int PRINT_BUTTON_ID = 0;
   public static final int PRINT_CREATIVE_BUTTON_ID = 1;
   public static final int PREVIOUS_FRAME_BUTTON_ID = 2;
   public static final int NEXT_FRAME_BUTTON_ID = 3;
   public static final int TOGGLE_PROCESS_BUTTON_ID = 4;
   private final LightroomBlockEntity lightroomBlockEntity;
   private final ContainerData data;
   private List<Frame> frames = Collections.emptyList();

   public LightroomMenu(int containerId, Inventory playerInventory, LightroomBlockEntity blockEntity, ContainerData containerData) {
      super(Exposure.MenuTypes.LIGHTROOM.get(), containerId);
      this.lightroomBlockEntity = blockEntity;
      this.data = containerData;
      this.addSlot(
         new Slot(blockEntity, 0, -20, 42) {
            public boolean mayPlace(ItemStack stack) {
               return blockEntity.isItemValidForSlot(0, stack);
            }

            public void setChanged() {
               LightroomMenu.this.frames = this.getItem().getItem() instanceof DevelopedFilmItem developedFilm
                  ? developedFilm.getStoredFrames(this.getItem())
                  : Collections.emptyList();
               if (LightroomMenu.this.lightroomBlockEntity.getLevel() != null && !LightroomMenu.this.lightroomBlockEntity.getLevel().isClientSide) {
                  LightroomMenu.this.data.set(2, 0);
               }

               super.setChanged();
            }
         }
      );
      this.addSlot(new Slot(blockEntity, 1, 8, 92) {
         public boolean mayPlace(ItemStack stack) {
            return blockEntity.isItemValidForSlot(1, stack);
         }
      });
      this.addSlot(new Slot(blockEntity, 2, 42, 92) {
         public boolean mayPlace(ItemStack stack) {
            return blockEntity.isItemValidForSlot(2, stack);
         }
      });
      this.addSlot(new Slot(blockEntity, 3, 60, 92) {
         public boolean mayPlace(ItemStack stack) {
            return blockEntity.isItemValidForSlot(3, stack);
         }
      });
      this.addSlot(new Slot(blockEntity, 4, 78, 92) {
         public boolean mayPlace(ItemStack stack) {
            return blockEntity.isItemValidForSlot(4, stack);
         }
      });
      this.addSlot(new Slot(blockEntity, 5, 96, 92) {
         public boolean mayPlace(ItemStack stack) {
            return blockEntity.isItemValidForSlot(5, stack);
         }
      });
      this.addSlot(new Slot(blockEntity, 6, 148, 92) {
         public boolean mayPlace(@NotNull ItemStack stack) {
            return false;
         }

         public void onTake(@NotNull Player player, @NotNull ItemStack pStack) {
            super.onTake(player, pStack);
            blockEntity.dropStoredExperience(player);
         }

         public void onQuickCraft(@NotNull ItemStack oldStackIn, @NotNull ItemStack newStackIn) {
            super.onQuickCraft(oldStackIn, newStackIn);
            blockEntity.dropStoredExperience(playerInventory.player);
         }
      });

      for (int row = 0; row < 3; row++) {
         for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 127 + row * 18));
         }
      }

      for (int index = 0; index < 9; index++) {
         this.addSlot(new Slot(playerInventory, index, 8 + index * 18, 185));
      }

      this.addDataSlots(this.data);
   }

   public static LightroomMenu fromBuffer(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
      return new LightroomMenu(containerId, playerInventory, getBlockEntity(playerInventory, buffer), new SimpleContainerData(3));
   }

   public LightroomBlockEntity getBlockEntity() {
      return this.lightroomBlockEntity;
   }

   public ContainerData getData() {
      return this.data;
   }

   public List<Frame> getExposedFrames() {
      return this.frames;
   }

   @Nullable
   public Frame getFrameByIndex(int index) {
      return index >= 0 && index < this.getExposedFrames().size() ? this.getExposedFrames().get(index) : null;
   }

   public int getSelectedFrame() {
      return this.data.get(2);
   }

   public boolean isPrinting() {
      return this.data.get(1) > 0;
   }

   public int getTotalFramesCount() {
      ItemStack filmStack = this.getBlockEntity().getItem(0);
      return !filmStack.isEmpty() && filmStack.getItem() instanceof FilmItem filmItem ? filmItem.getStoredFramesCount(filmStack) : 0;
   }

   public boolean canChangeProcess() {
      return this.getBlockEntity().isRefracted();
   }

   public boolean clickMenuButton(Player player, int buttonId) {
      if (buttonId == 0) {
         if (!player.level.isClientSide()) {
            this.getBlockEntity().startPrintingProcess(false);
         }

         return true;
      } else if (buttonId == 1) {
         if (player.isCreative() && !player.level.isClientSide()) {
            this.getBlockEntity().printFrameInCreative();
         }

         return true;
      } else if (buttonId == 2 || buttonId == 3) {
         int framesCount = this.getTotalFramesCount();
         if (framesCount > 0 && !player.level.isClientSide()) {
            int selectedFrame = this.data.get(2);
            int change = buttonId == 3 ? 1 : -1;
            selectedFrame += change;
            selectedFrame = Mth.clamp(selectedFrame, 0, framesCount - 1);
            this.data.set(2, selectedFrame);
         }

         return true;
      } else if (buttonId == 4) {
         PrintingMode currentProcess = this.getBlockEntity().getActualPrintingMode();
         this.getBlockEntity().setPrintMode(currentProcess.cycle());
         return true;
      } else {
         return false;
      }
   }

   @NotNull
   public ItemStack quickMoveStack(@NotNull Player player, int index) {
      Slot slot = (Slot)this.slots.get(index);
      ItemStack clickedStack = slot.getItem();
      ItemStack returnedStack = clickedStack.copy();
      if (index < 7) {
         if (!this.moveItemStackTo(clickedStack, 7, this.slots.size(), true)) {
            return ItemStack.EMPTY;
         }

         if (index == 6) {
            slot.onQuickCraft(clickedStack, returnedStack);
         }
      } else if (index < this.slots.size() && !this.moveItemStackTo(clickedStack, 0, 7, false)) {
         return ItemStack.EMPTY;
      }

      if (clickedStack.isEmpty()) {
         slot.set(ItemStack.EMPTY);
      } else {
         slot.setChanged();
      }

      return returnedStack;
   }

   protected boolean moveItemStackTo(ItemStack movedStack, int startIndex, int endIndex, boolean reverseDirection) {
      boolean hasRemainder = false;
      int i = startIndex;
      if (reverseDirection) {
         i = endIndex - 1;
      }

      if (movedStack.isStackable()) {
         while (!movedStack.isEmpty() && (!reverseDirection ? i < endIndex : i >= startIndex)) {
            Slot slot = (Slot)this.slots.get(i);
            ItemStack slotStack = slot.getItem();
            if (!slotStack.isEmpty() && Backport.isSameItemSameComponents(movedStack, slotStack)) {
               int j = slotStack.getCount() + movedStack.getCount();
               int maxSize;
               if (j <= (maxSize = Math.min(slot.getMaxStackSize(), movedStack.getMaxStackSize()))) {
                  movedStack.setCount(0);
                  slotStack.setCount(j);
                  slot.setChanged();
                  hasRemainder = true;
               } else if (slotStack.getCount() < maxSize) {
                  movedStack.shrink(maxSize - slotStack.getCount());
                  slotStack.setCount(maxSize);
                  slot.setChanged();
                  hasRemainder = true;
               }
            }

            if (reverseDirection) {
               i--;
            } else {
               i++;
            }
         }
      }

      if (!movedStack.isEmpty()) {
         i = reverseDirection ? endIndex - 1 : startIndex;

         while (!reverseDirection ? i < endIndex : i >= startIndex) {
            Slot slot1 = (Slot)this.slots.get(i);
            ItemStack itemmovedStack1 = slot1.getItem();
            if (itemmovedStack1.isEmpty() && slot1.mayPlace(movedStack)) {
               if (movedStack.getCount() > slot1.getMaxStackSize()) {
                  slot1.set(movedStack.split(slot1.getMaxStackSize()));
               } else {
                  slot1.set(movedStack.split(movedStack.getCount()));
               }

               slot1.setChanged();
               hasRemainder = true;
               break;
            }

            if (reverseDirection) {
               i--;
            } else {
               i++;
            }
         }
      }

      return hasRemainder;
   }

   public boolean stillValid(@NotNull Player player) {
      return this.lightroomBlockEntity.stillValid(player);
   }

   private static LightroomBlockEntity getBlockEntity(Inventory playerInventory, FriendlyByteBuf data) {
      Objects.requireNonNull(playerInventory, "playerInventory cannot be null");
      Objects.requireNonNull(data, "data cannot be null");
      BlockEntity blockEntityAtPos = playerInventory.player.level.getBlockEntity(data.readBlockPos());
      if (blockEntityAtPos instanceof LightroomBlockEntity) {
         return (LightroomBlockEntity)blockEntityAtPos;
      } else {
         throw new IllegalStateException("Block entity is not correct! " + blockEntityAtPos);
      }
   }
}
