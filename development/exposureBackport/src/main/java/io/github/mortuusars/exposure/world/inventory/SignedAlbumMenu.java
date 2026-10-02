package io.github.mortuusars.exposure.world.inventory;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.util.PagingDirection;
import io.github.mortuusars.exposure.util.Side;
import io.github.mortuusars.exposure.world.item.AlbumItem;
import io.github.mortuusars.exposure.world.item.SignedAlbumItem;
import io.github.mortuusars.exposure.world.item.component.album.SignedAlbumPage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import net.minecraft.network.FriendlyByteBuf;

public class SignedAlbumMenu extends AbstractContainerMenu {
   public static final int PREVIOUS_PAGE_BUTTON = PagingDirection.PREVIOUS.ordinal();
   public static final int NEXT_PAGE_BUTTON = PagingDirection.NEXT.ordinal();
   protected final int albumSlot;
   protected final ItemStack albumStack;
   protected final SignedAlbumItem albumItem;
   protected DataSlot currentSpreadIndex = DataSlot.standalone();
   protected final Map<Integer, Consumer<Player>> buttonActions = new HashMap<Integer, Consumer<Player>>() {
      {
         this.put(
            Integer.valueOf(SignedAlbumMenu.PREVIOUS_PAGE_BUTTON),
            p -> SignedAlbumMenu.this.setCurrentSpreadIndex(Math.max(0, SignedAlbumMenu.this.getCurrentSpreadIndex() - 1))
         );
         this.put(
            Integer.valueOf(SignedAlbumMenu.NEXT_PAGE_BUTTON),
            p -> SignedAlbumMenu.this.setCurrentSpreadIndex(
                  Math.min((SignedAlbumMenu.this.getPages().size() - 1) / 2, SignedAlbumMenu.this.getCurrentSpreadIndex() + 1)
               )
         );
      }
   };

   public SignedAlbumMenu(int containerId, Inventory playerInventory, int albumSlot) {
      this(Exposure.MenuTypes.SIGNED_ALBUM.get(), containerId, playerInventory, albumSlot);
   }

   protected SignedAlbumMenu(MenuType<? extends AbstractContainerMenu> type, int containerId, Inventory playerInventory, int albumSlot) {
      super(type, containerId);
      this.albumSlot = albumSlot;
      this.albumStack = playerInventory.getItem(albumSlot);
      if (this.albumStack.getItem() instanceof SignedAlbumItem item) {
         this.albumItem = item;
         this.addDataSlot(this.currentSpreadIndex);
      } else {
         throw new IllegalStateException("Expected SignedAlbumItem in slot '" + albumSlot + "'. Got: " + this.albumStack);
      }
   }

   public int getAlbumSlot() {
      return this.albumSlot;
   }

   public List<SignedAlbumPage> getPages() {
      return this.albumItem.getContent(this.albumStack).pages();
   }

   public Optional<SignedAlbumPage> getPage(int pageIndex) {
      return pageIndex <= this.getPages().size() - 1 ? Optional.ofNullable(this.getPages().get(pageIndex)) : Optional.empty();
   }

   public Optional<SignedAlbumPage> getPage(Side side) {
      return this.getPage(this.getCurrentSpreadIndex() * 2 + side.getIndex());
   }

   public ItemStack getPhotograph(Side side) {
      return this.getPage(side).orElse(SignedAlbumPage.EMPTY).photograph();
   }

   public int getCurrentSpreadIndex() {
      return this.currentSpreadIndex.get();
   }

   public void setCurrentSpreadIndex(int spreadIndex) {
      this.currentSpreadIndex.set(spreadIndex);
   }

   public boolean clickMenuButton(Player player, int id) {
      Consumer<Player> buttonAction = this.buttonActions.get(id);
      if (buttonAction != null) {
         buttonAction.accept(player);
         return true;
      } else {
         return false;
      }
   }

   @NotNull
   public ItemStack quickMoveStack(Player player, int index) {
      return ItemStack.EMPTY;
   }

   public boolean stillValid(Player player) {
      return player.getInventory().getItem(this.albumSlot).getItem() instanceof AlbumItem;
   }

   public static SignedAlbumMenu fromBuffer(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
      return new SignedAlbumMenu(containerId, playerInventory, buffer.readVarInt());
   }
}
