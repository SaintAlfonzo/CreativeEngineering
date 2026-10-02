package io.github.mortuusars.exposure.world.inventory;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.util.supporter.Supporters;
import io.github.mortuusars.exposure.world.inventory.slot.FilteredSlot;
import io.github.mortuusars.exposure.world.item.camera.Attachment;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.mortuusars.exposure.backport.util.Vector2i;
import io.github.mortuusars.exposure.backport.util.Backport;

public abstract class AbstractCameraAttachmentsMenu extends AbstractContainerMenu {
   public static final int SKIN_REGULAR_BUTTON_ID = 100;
   public static final int SKIN_GOLD_BUTTON_ID = 101;
   protected final Player player;
   protected final AbstractCameraAttachmentsMenu.CameraAccess cameraAccess;
   protected final List<Attachment<?>> attachments;
   protected boolean clientContentsInitialized;

   protected AbstractCameraAttachmentsMenu(
      @Nullable MenuType<?> menuType, int containerId, Inventory playerInventory, AbstractCameraAttachmentsMenu.CameraAccess cameraAccess
   ) {
      super(menuType, containerId);
      this.player = playerInventory.player;
      this.cameraAccess = cameraAccess;
      this.attachments = cameraAccess.map((i, s) -> i.getAttachments());
      SimpleContainer container = this.createAttachmentsContainer();
      this.addAttachmentSlots(container);
      this.addPlayerSlots(playerInventory);
   }

   public AbstractCameraAttachmentsMenu.CameraAccess getCamera() {
      return this.cameraAccess;
   }

   protected ItemStack getCameraStack() {
      return this.getCamera().getStack();
   }

   @NotNull
   protected SimpleContainer createAttachmentsContainer() {
      ItemStack[] attachmentItems = this.attachments
         .stream()
         .map(attachment -> attachment.get(this.getCamera().getStack()).getCopy())
         .toArray(ItemStack[]::new);
      SimpleContainer container = new SimpleContainer(attachmentItems) {
         public int getMaxStackSize() {
            return 1;
         }
      };
      container.addListener(this::onContainerChanged);
      return container;
   }

   protected void onContainerChanged(Container c) {
      for (int slotId = 0; slotId < c.getContainerSize(); slotId++) {
         Attachment<?> attachment = this.attachments.get(slotId);
         attachment.set(this.getCamera().getStack(), c.getItem(slotId));
      }
   }

   public void initializeContents(int stateId, List<ItemStack> items, ItemStack carried) {
      this.clientContentsInitialized = false;
      super.initializeContents(stateId, items, carried);
      this.clientContentsInitialized = true;
   }

   protected void addAttachmentSlots(Container container) {
      Map<Attachment<?>, Vector2i> slotPositions = Map.of(
         Attachment.FILM,
         new Vector2i(13, 42),
         Attachment.FLASH,
         new Vector2i(147, 15),
         Attachment.LENS,
         new Vector2i(147, 43),
         Attachment.FILTER,
         new Vector2i(147, 71)
      );

      for (int index = 0; index < this.attachments.size(); index++) {
         Attachment<?> attachmentType = this.attachments.get(index);
         Vector2i pos = slotPositions.get(attachmentType);
         this.addSlot(new FilteredSlot(container, index, pos.x(), pos.y(), 1, this::onItemInSlotChanged, attachmentType.itemPredicate()));
      }
   }

   protected void addPlayerSlots(Inventory playerInventory) {
      for (int row = 0; row < 3; row++) {
         for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(playerInventory, column + row * 9 + 9, column * 18 + 8, 103 + row * 18));
         }
      }

      for (int slot = 0; slot < 9; slot++) {
         this.addSlot(new Slot(playerInventory, slot, slot * 18 + 8, 161));
      }
   }

   protected void onItemInSlotChanged(FilteredSlot.SlotChangedArgs args) {
      int slotId = args.slot().getSlotId();
      ItemStack newStack = args.newStack();
      Attachment<?> attachment = this.attachments.get(slotId);
      if (this.clientContentsInitialized) {
         if (!newStack.isEmpty()) {
            attachment.playInsertSoundSided(this.player);
         } else {
            attachment.playRemoveSoundSided(this.player);
         }

         this.getCamera().apply((item, stack) -> item.actionPerformed(stack, (CameraHolder)this.player));
      }
   }

   @NotNull
   public ItemStack quickMoveStack(@NotNull Player player, int slotIndex) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot clickedSlot = (Slot)this.slots.get(slotIndex);
      if (clickedSlot.hasItem()) {
         ItemStack slotStack = clickedSlot.getItem();
         itemstack = slotStack.copy();
         if (slotIndex < this.attachments.size()) {
            if (!this.moveItemStackTo(slotStack, this.attachments.size(), this.slots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(slotStack, 0, this.attachments.size(), false)) {
            return ItemStack.EMPTY;
         }

         if (slotStack.isEmpty()) {
            clickedSlot.set(ItemStack.EMPTY);
         } else {
            clickedSlot.setChanged();
         }
      }

      return itemstack;
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
            ItemStack movedStack1 = slot1.getItem();
            if (movedStack1.isEmpty() && slot1.mayPlace(movedStack)) {
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

   public boolean clickMenuButton(Player player, int id) {
      if (Supporters.hasAccessToGoldenSkin(player.getUUID())) {
         if (id == 100) {
            this.getCamera().apply((i, s) -> Exposure.DataComponents.CAMERA_GOLD.set(s, false));
            return true;
         }

         if (id == 101) {
            this.getCamera().apply((i, s) -> Exposure.DataComponents.CAMERA_GOLD.set(s, true));
            return true;
         }
      }

      return false;
   }

   public void removed(Player player) {
      super.removed(player);
      this.getCamera().apply((item, stack) -> item.setDisassembled(stack, false));
   }

   public interface CameraAccess {
      ItemStack getStack();

      default void apply(BiConsumer<CameraItem, ItemStack> consumer) {
         ItemStack camera = this.getStack();
         consumer.accept((CameraItem)camera.getItem(), camera);
      }

      default <T> T map(BiFunction<CameraItem, ItemStack, T> func) {
         ItemStack camera = this.getStack();
         return func.apply((CameraItem)camera.getItem(), camera);
      }
   }
}
