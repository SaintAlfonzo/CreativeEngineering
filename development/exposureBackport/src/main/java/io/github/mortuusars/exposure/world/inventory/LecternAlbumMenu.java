package io.github.mortuusars.exposure.world.inventory;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import net.minecraft.network.FriendlyByteBuf;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;

public class LecternAlbumMenu extends AbstractContainerMenu {
   public static final int BUTTON_PREV_PAGE = 1;
   public static final int BUTTON_NEXT_PAGE = 2;
   public static final int BUTTON_TAKE_BOOK = 3;
   public static final int BUTTON_PAGE_JUMP_RANGE_START = 100;
   private static final int SLOT_COUNT = 1;
   private static final int DATA_COUNT = 1;
   protected final Container container;
   protected final ContainerData data;

   public LecternAlbumMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
      super(Exposure.MenuTypes.LECTERN_ALBUM.get(), containerId);
      this.container = container;
      this.data = data;
      checkContainerSize(container, 1);
      checkContainerDataCount(data, 1);
      this.addSlot(new Slot(container, 0, -999, -999) {
         public void setChanged() {
            super.setChanged();
            LecternAlbumMenu.this.slotsChanged(this.container);
         }
      });
      this.addDataSlots(data);
   }

   public LecternAlbumMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
      this(containerId, playerInventory, new SimpleContainer(1), new SimpleContainerData(1));
      ItemStack book = (ItemStack)ByteBufCodecs.ITEM_STACK.decode(buffer);
      this.container.setItem(0, book);
   }

   public ItemStack getBook() {
      return this.container.getItem(0);
   }

   public int getPage() {
      return this.data.get(0);
   }

   public void setData(int id, int data) {
      super.setData(id, data);
      this.broadcastChanges();
   }

   public boolean clickMenuButton(Player player, int id) {
      if (id >= 100) {
         int pageIndex = id - 100;
         this.setData(0, pageIndex);
         return true;
      } else {
         switch (id) {
            case 1: {
               int i = this.data.get(0);
               this.setData(0, i - 1);
               return true;
            }
            case 2: {
               int i = this.data.get(0);
               this.setData(0, i + 1);
               return true;
            }
            case 3:
               if (!player.mayBuild()) {
                  return false;
               }

               ItemStack itemStack = this.container.removeItemNoUpdate(0);
               this.container.setChanged();
               if (!player.getInventory().add(itemStack)) {
                  player.drop(itemStack, false);
               }

               return true;
            default:
               return false;
         }
      }
   }

   @NotNull
   public ItemStack quickMoveStack(Player player, int index) {
      return ItemStack.EMPTY;
   }

   public boolean stillValid(Player player) {
      return this.container.stillValid(player);
   }
}
