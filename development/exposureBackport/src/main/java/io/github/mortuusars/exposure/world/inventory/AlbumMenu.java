package io.github.mortuusars.exposure.world.inventory;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.serverbound.AlbumSignC2SP;
import io.github.mortuusars.exposure.util.PagingDirection;
import io.github.mortuusars.exposure.util.Side;
import io.github.mortuusars.exposure.world.inventory.slot.AlbumPhotographSlot;
import io.github.mortuusars.exposure.world.inventory.slot.AlbumPlayerInventorySlot;
import io.github.mortuusars.exposure.world.item.AlbumItem;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.item.component.album.AlbumPage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.network.FriendlyByteBuf;

public class AlbumMenu extends AbstractContainerMenu {
   public static final int CANCEL_ADDING_PHOTO_BUTTON = -1;
   public static final int PREVIOUS_PAGE_BUTTON = PagingDirection.PREVIOUS.ordinal();
   public static final int NEXT_PAGE_BUTTON = PagingDirection.NEXT.ordinal();
   public static final int LEFT_PAGE_PHOTO_BUTTON = 2;
   public static final int RIGHT_PAGE_PHOTO_BUTTON = 3;
   public static final int ENTER_SIGN_MODE_BUTTON = 4;
   public static final int SIGN_BUTTON = 5;
   public static final int CANCEL_SIGNING_BUTTON = 6;
   protected final int albumSlot;
   protected final ItemStack albumStack;
   protected final AlbumItem albumItem;
   protected final List<AlbumPhotographSlot> photographSlots = new ArrayList<>();
   protected final List<AlbumPlayerInventorySlot> playerInventorySlots = new ArrayList<>();
   protected DataSlot currentSpreadIndex = DataSlot.standalone();
   @Nullable
   protected Side sideBeingAddedTo = null;
   protected boolean signing;
   protected String title = "";
   protected final Map<Integer, Consumer<Player>> buttonActions = new HashMap<Integer, Consumer<Player>>() {
      {
         this.put(Integer.valueOf(-1), p -> {
            AlbumMenu.this.sideBeingAddedTo = null;
            if (!AlbumMenu.this.getCarried().isEmpty()) {
               p.getInventory().placeItemBackInInventory(AlbumMenu.this.getCarried());
               AlbumMenu.this.setCarried(ItemStack.EMPTY);
            }

            AlbumMenu.this.updatePlayerInventorySlots();
         });
         this.put(Integer.valueOf(AlbumMenu.PREVIOUS_PAGE_BUTTON), p -> {
            AlbumMenu.this.clickMenuButton(p, -1);
            AlbumMenu.this.setCurrentSpreadIndex(Math.max(0, AlbumMenu.this.getCurrentSpreadIndex() - 1));
         });
         this.put(Integer.valueOf(AlbumMenu.NEXT_PAGE_BUTTON), p -> {
            AlbumMenu.this.clickMenuButton(p, -1);
            AlbumMenu.this.setCurrentSpreadIndex(Math.min((AlbumMenu.this.getPages().size() - 1) / 2, AlbumMenu.this.getCurrentSpreadIndex() + 1));
         });
         this.put(Integer.valueOf(2), p -> AlbumMenu.this.onPhotoButtonPress(p, Side.LEFT));
         this.put(Integer.valueOf(3), p -> AlbumMenu.this.onPhotoButtonPress(p, Side.RIGHT));
         this.put(Integer.valueOf(4), p -> {
            AlbumMenu.this.signing = true;
            AlbumMenu.this.sideBeingAddedTo = null;
         });
         this.put(Integer.valueOf(5), p -> AlbumMenu.this.signAlbum(p));
         this.put(Integer.valueOf(6), p -> AlbumMenu.this.signing = false);
      }
   };

   public AlbumMenu(int containerId, Inventory playerInventory, int albumSlot) {
      this(Exposure.MenuTypes.ALBUM.get(), containerId, playerInventory, albumSlot);
   }

   protected AlbumMenu(MenuType<? extends AbstractContainerMenu> type, int containerId, Inventory playerInventory, int albumSlot) {
      super(type, containerId);
      this.albumSlot = albumSlot;
      this.albumStack = playerInventory.getItem(albumSlot);
      if (this.albumStack.getItem() instanceof AlbumItem item) {
         this.albumItem = item;
         if (this.isAlbumEditable() && item.getContent(this.albumStack).pages().size() < 16) {
            item.setContent(this.albumStack, item.getContent(this.albumStack).toMutable().setPage(15, AlbumPage.EMPTY).toImmutable());
         }

         this.addPhotographSlots();
         this.addPlayerInventorySlots(playerInventory, 70, 115);
         this.addDataSlot(this.currentSpreadIndex);
      } else {
         throw new IllegalStateException("Expected AlbumItem in slot '" + albumSlot + "'. Got: " + this.albumStack);
      }
   }

   protected void addPhotographSlots() {
      ItemStack[] photographs = this.albumItem.getContent(this.albumStack).pages().stream().map(AlbumPage::photograph).toArray(ItemStack[]::new);
      SimpleContainer container = new SimpleContainer(photographs);

      for (int i = 0; i < container.getContainerSize(); i++) {
         int x = i % 2 == 0 ? 71 : 212;
         int y = 67;
         AlbumPhotographSlot slot = new AlbumPhotographSlot(container, i, x, y) {
            public void set(ItemStack stack) {
               super.set(stack);
               AlbumMenu.this.onPhotographSlotChanged(this.getContainerSlot(), stack);
            }
         };
         this.addSlot(slot);
         this.photographSlots.add(slot);
      }
   }

   private void onPhotographSlotChanged(int slotIndex, ItemStack stack) {
      this.albumItem.updatePage(this.albumStack, slotIndex, page -> page.orElse(AlbumPage.EMPTY).setPhotograph(stack));
   }

   private void addPlayerInventorySlots(Inventory playerInventory, int x, int y) {
      for (int row = 0; row < 3; row++) {
         for (int column = 0; column < 9; column++) {
            AlbumPlayerInventorySlot slot = new AlbumPlayerInventorySlot(playerInventory, column + row * 9 + 9, x + column * 18, y + row * 18);
            this.addSlot(slot);
            this.playerInventorySlots.add(slot);
         }
      }

      for (int index = 0; index < 9; index++) {
         final boolean disabled = index == playerInventory.selected && playerInventory.getSelected().getItem() instanceof AlbumItem;
         AlbumPlayerInventorySlot slot = new AlbumPlayerInventorySlot(playerInventory, index, x + index * 18, y + 58) {
            public boolean mayPickup(Player player) {
               return !disabled;
            }

            public boolean mayPlace(ItemStack stack) {
               return !disabled;
            }
         };
         this.addSlot(slot);
         this.playerInventorySlots.add(slot);
      }
   }

   protected void updatePlayerInventorySlots() {
      boolean isInAddingPhotographMode = this.isInAddingPhotographMode();

      for (AlbumPlayerInventorySlot slot : this.playerInventorySlots) {
         slot.setActive(isInAddingPhotographMode);
      }
   }

   public int getAlbumSlot() {
      return this.albumSlot;
   }

   public boolean isAlbumEditable() {
      return true;
   }

   public boolean isInAddingPhotographMode() {
      return this.getSideBeingAddedTo() != null;
   }

   public boolean isInSigningMode() {
      return this.signing;
   }

   public String getTitle() {
      return this.title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public boolean canSignAlbum() {
      for (AlbumPage page : this.getPages()) {
         if (!page.photograph().isEmpty() || !page.note().isEmpty()) {
            return true;
         }
      }

      return false;
   }

   public void signAlbum(Player player) {
      if (player.level.isClientSide) {
         if (!this.canSignAlbum()) {
            throw new IllegalStateException("Cannot sign the album.\n" + Arrays.toString(this.getPages().toArray()));
         } else {
            Packets.sendToServer(new AlbumSignC2SP(this.albumSlot, this.title, player.getScoreboardName()));
         }
      }
   }

   public List<AlbumPlayerInventorySlot> getPlayerInventorySlots() {
      return this.playerInventorySlots;
   }

   public List<AlbumPage> getPages() {
      return this.albumItem.getContent(this.albumStack).pages();
   }

   public Optional<AlbumPage> getPage(int pageIndex) {
      return pageIndex <= this.getPages().size() - 1 ? Optional.ofNullable(this.getPages().get(pageIndex)) : Optional.empty();
   }

   public Optional<AlbumPage> getPage(Side side) {
      return this.getPage(this.getCurrentSpreadIndex() * 2 + side.getIndex());
   }

   public void updatePage(int pageIndex, Function<AlbumPage, AlbumPage> pageTransform) {
      this.albumItem.updatePage(this.albumStack, pageIndex, page -> pageTransform.apply(page.orElse(AlbumPage.EMPTY)));
   }

   public Optional<AlbumPhotographSlot> getPhotographSlot(Side side) {
      return this.getPhotographSlot(this.getCurrentSpreadIndex() * 2 + (side == Side.LEFT ? 0 : 1));
   }

   public Optional<AlbumPhotographSlot> getPhotographSlot(int index) {
      return index >= 0 && index < this.photographSlots.size() ? Optional.ofNullable(this.photographSlots.get(index)) : Optional.empty();
   }

   public ItemStack getPhotograph(Side side) {
      return this.getPhotographSlot(side).<ItemStack>map(Slot::getItem).orElse(ItemStack.EMPTY);
   }

   public int getCurrentSpreadIndex() {
      return this.currentSpreadIndex.get();
   }

   public void setCurrentSpreadIndex(int spreadIndex) {
      this.currentSpreadIndex.set(spreadIndex);
   }

   @Nullable
   public Side getSideBeingAddedTo() {
      return this.sideBeingAddedTo;
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

   private void onPhotoButtonPress(Player player, Side side) {
      Preconditions.checkArgument(this.isAlbumEditable(), "Photo Button should be disabled and hidden when Album is not editable. " + this.albumStack);
      Optional<AlbumPhotographSlot> photographSlot = this.getPhotographSlot(side);
      if (!photographSlot.isEmpty()) {
         AlbumPhotographSlot slot = photographSlot.get();
         if (!slot.hasItem()) {
            this.sideBeingAddedTo = side;
         } else {
            ItemStack stack = slot.getItem();
            if (!player.getInventory().add(stack)) {
               player.drop(stack, false);
            }

            slot.set(ItemStack.EMPTY);
         }

         this.updatePlayerInventorySlots();
      }
   }

   public void clicked(int slotId, int button, ClickType clickType, Player player) {
      if (this.sideBeingAddedTo != null && slotId >= 0 && slotId < this.slots.size()) {
         Slot slot = (Slot)this.slots.get(slotId);
         ItemStack stack = slot.getItem();
         if (button == 0 && slot instanceof AlbumPlayerInventorySlot && stack.getItem() instanceof PhotographItem && this.getCarried().isEmpty()) {
            int pageIndex = this.getCurrentSpreadIndex() * 2 + this.sideBeingAddedTo.getIndex();
            Optional<AlbumPhotographSlot> photographSlot = this.getPhotographSlot(pageIndex);
            if (photographSlot.isEmpty() || !photographSlot.get().getItem().isEmpty()) {
               return;
            }

            photographSlot.get().set(stack);
            slot.set(ItemStack.EMPTY);
            if (player.level.isClientSide) {
               player.playSound(Exposure.SoundEvents.PHOTOGRAPH_PLACE.get(), 0.8F, 1.1F);
            }

            this.sideBeingAddedTo = null;
            this.updatePlayerInventorySlots();
         } else {
            super.clicked(slotId, button, clickType, player);
         }
      } else {
         super.clicked(slotId, button, clickType, player);
      }
   }

   @NotNull
   public ItemStack quickMoveStack(Player player, int index) {
      return ItemStack.EMPTY;
   }

   public boolean stillValid(Player player) {
      return player.getInventory().getItem(this.albumSlot).getItem() instanceof AlbumItem;
   }

   public static AlbumMenu fromBuffer(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
      return new AlbumMenu(containerId, playerInventory, buffer.readVarInt());
   }
}
