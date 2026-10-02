package io.github.mortuusars.exposure.world.inventory;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.backport.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.network.FriendlyByteBuf;

public class ItemRenameMenu extends AbstractContainerMenu {
   public static final int MAX_NAME_LENGTH = 50;
   public static final int APPLY_BUTTON_ID = 0;
   protected final Inventory playerInventory;
   protected final int slot;
   protected final Item item;
   protected String itemName;

   public ItemRenameMenu(int containerId, Inventory playerInventory, int slot) {
      super(Exposure.MenuTypes.ITEM_RENAME.get(), containerId);
      this.playerInventory = playerInventory;
      this.slot = slot;
      ItemStack itemStack = playerInventory.getItem(slot);
      Preconditions.checkArgument(!itemStack.isEmpty(), "Cannot rename empty item in slot " + slot);
      this.item = itemStack.getItem();
      this.itemName = itemStack.getHoverName().getString();
      SimpleContainer container = new SimpleContainer(new ItemStack[]{itemStack});
      this.addSlot(new Slot(container, 0, 4, 16) {
         public boolean mayPlace(ItemStack stack) {
            return false;
         }

         public boolean mayPickup(Player player) {
            return false;
         }

         public boolean isHighlightable() {
            return false;
         }
      });
   }

   public static ItemRenameMenu fromBuffer(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
      return new ItemRenameMenu(containerId, playerInventory, buffer.readInt());
   }

   public Player getPlayer() {
      return this.playerInventory.player;
   }

   @NotNull
   public ItemStack quickMoveStack(Player player, int index) {
      return ItemStack.EMPTY;
   }

   public boolean stillValid(Player player) {
      return this.item.getClass().isAssignableFrom(player.getInventory().getItem(this.slot).getItem().getClass());
   }

   public void updateResult() {
      ItemStack itemStack = this.getSlot(0).getItem();
      ItemStack itemStack2 = itemStack.copy();
      if (org.apache.commons.lang3.StringUtils.isBlank(this.itemName)) {
         DataComponents.CUSTOM_NAME.remove(itemStack2);
      } else if (!this.itemName.equals(itemStack.getHoverName().getString())) {
         DataComponents.CUSTOM_NAME.set(itemStack2, Component.literal(this.itemName));
      }

      this.getSlot(0).set(itemStack2);
      this.broadcastChanges();
   }

   public String getItemName() {
      return this.itemName;
   }

   public boolean setItemName(String itemName) {
      String string = this.validateName(itemName);
      if (string != null && string.equals(itemName)) {
         this.itemName = string;
         this.updateResult();
         return true;
      } else {
         return false;
      }
   }

   @Nullable
   public String validateName(String itemName) {
      String string = net.minecraft.SharedConstants.filterText(itemName);
      return string.length() <= 50 ? string : null;
   }

   public boolean clickMenuButton(Player player, int id) {
      if (id == 0 && !this.getSlot(0).getItem().getHoverName().getString().equals(this.playerInventory.getItem(this.slot).getHoverName().getString())) {
         this.playerInventory.setItem(this.slot, this.getSlot(0).getItem());
         player.level.playSound(player, player, Exposure.SoundEvents.WRITE.get(), SoundSource.PLAYERS, 0.8F, 1.0F);
      }

      return super.clickMenuButton(player, id);
   }
}
