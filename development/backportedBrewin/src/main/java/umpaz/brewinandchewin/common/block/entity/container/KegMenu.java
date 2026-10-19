package umpaz.brewinandchewin.common.block.entity.container;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.BnCRecipeBookTypes;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.container.AbstractedFluidTank;
import umpaz.brewinandchewin.common.container.AbstractedItemHandler;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.registry.BnCBlocks;
import umpaz.brewinandchewin.common.registry.BnCMenuTypes;
import umpaz.brewinandchewin.common.utility.KegRecipeWrapper;

public class KegMenu extends RecipeBookMenu<KegRecipeWrapper> {
   public static final ResourceLocation EMPTY_CONTAINER_SLOT_TANKARD = BrewinAndChewin.asResource("item/empty_container_slot_tankard");
   public final KegBlockEntity blockEntity;
   public final AbstractedItemHandler inventory;
   public final AbstractedFluidTank kegTank;
   private final ContainerData kegData;
   private final ContainerLevelAccess canInteractWithCallable;
   protected final Level level;
   private final KegRecipeWrapper recipeWrapper;

   public KegMenu(int windowId, Inventory playerInventory, BlockPos data) {
      this(windowId, playerInventory, getTileEntity(playerInventory, data), new SimpleContainerData(4));
   }

   public KegMenu(int windowId, Inventory playerInventory, KegBlockEntity blockEntity, ContainerData kegDataIn) {
      super(BnCMenuTypes.KEG, windowId);
      this.blockEntity = blockEntity;
      this.inventory = blockEntity.getInventory();
      this.kegTank = blockEntity.getFluidTank();
      this.kegData = kegDataIn;
      this.level = playerInventory.player.level;
      this.canInteractWithCallable = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
      this.recipeWrapper = BrewinAndChewin.getHelper().createRecipeWrapper(this.inventory, this.kegTank);
      int startX = 8;
      int startY = 18;
      int inputStartX = 39;
      int inputStartY = 17;
      int borderSlotSize = 18;

      for (int row = 0; row < 2; row++) {
         for (int column = 0; column < 2; column++) {
            this.addSlot(
               BrewinAndChewin.getHelper()
                  .createKegSlot(this.inventory, row * 2 + column, inputStartX + column * borderSlotSize, inputStartY + row * borderSlotSize)
            );
         }
      }

      this.addSlot(BrewinAndChewin.getHelper().createKegContainerSlot(this.inventory, 4, 91, 55));
      this.addSlot(BrewinAndChewin.getHelper().createKegResultSlot(this.inventory, 5, 124, 55));
      // Port (alcohol.md 8.4): fluid in and empty container out, in the column left of the grid.
      this.addSlot(BrewinAndChewin.getHelper().createKegSlot(this.inventory, KegBlockEntity.FLUID_IN_SLOT, 8, 17));
      this.addSlot(BrewinAndChewin.getHelper().createKegResultSlot(this.inventory, KegBlockEntity.FLUID_OUT_SLOT, 8, 53));
      int startPlayerInvY = startY * 4 + 12;

      for (int row = 0; row < 3; row++) {
         for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(playerInventory, 9 + row * 9 + column, startX + column * borderSlotSize, startPlayerInvY + row * borderSlotSize));
         }
      }

      for (int column = 0; column < 9; column++) {
         this.addSlot(new Slot(playerInventory, column, startX + column * borderSlotSize, 142));
      }

      this.addDataSlots(kegDataIn);
   }

   private static KegBlockEntity getTileEntity(Inventory playerInventory, BlockPos data) {
      Objects.requireNonNull(playerInventory, "playerInventory cannot be null");
      Objects.requireNonNull(data, "data cannot be null");
      BlockEntity tileAtPos = playerInventory.player.level.getBlockEntity(data);
      if (tileAtPos instanceof KegBlockEntity keg) {
         return keg;
      } else {
         throw new IllegalStateException("Tile entity is not correct! " + tileAtPos);
      }
   }

   @Override
   public boolean stillValid(Player playerIn) {
      return stillValid(this.canInteractWithCallable, playerIn, BnCBlocks.KEG);
   }

   @Override
   public ItemStack quickMoveStack(Player playerIn, int index) {
      int indexContainerInput = KegBlockEntity.CONTAINER_SLOT;
      int indexOutput = KegBlockEntity.OUTPUT_SLOT;
      int indexFluidInput = KegBlockEntity.FLUID_IN_SLOT;
      int startPlayerInv = KegBlockEntity.INVENTORY_SIZE;
      int endPlayerInv = startPlayerInv + 36;
      Slot slot = this.slots.get(index);
      ItemStack slotStackCopy = ItemStack.EMPTY;
      if (slot.hasItem()) {
         ItemStack slotStack = slot.getItem();
         slotStackCopy = slotStack.copy();
         if (index == indexOutput || index == KegBlockEntity.FLUID_OUT_SLOT) {
            if (!this.moveItemStackTo(slotStack, startPlayerInv, endPlayerInv, true)) {
               return ItemStack.EMPTY;
            }
         } else if (index < startPlayerInv) {
            if (!this.moveItemStackTo(slotStack, startPlayerInv, endPlayerInv, false)) {
               return ItemStack.EMPTY;
            }
         } else if (this.blockEntity.isFluidSource(slotStack)) {
            // Port (alcohol.md 8.4): filled containers go to the fluid slot, then the ingredient grid.
            if (!this.moveItemStackTo(slotStack, indexFluidInput, indexFluidInput + 1, false)
               && !this.moveItemStackTo(slotStack, 0, indexContainerInput, false)) {
               return ItemStack.EMPTY;
            }
         } else {
            boolean isValidContainer = slotStack.is(this.blockEntity.getInventory().getStackInSlot(4).getItem())
               || this.blockEntity.getPouringRecipe(slotStack).isPresent();
            if (isValidContainer && !this.moveItemStackTo(slotStack, indexContainerInput, indexContainerInput + 1, false)) {
               return ItemStack.EMPTY;
            }

            if (!this.moveItemStackTo(slotStack, 0, indexContainerInput, false)) {
               return ItemStack.EMPTY;
            }
         }

         if (slotStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         if (slotStack.getCount() == slotStackCopy.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTake(playerIn, slotStack);
      }

      return slotStackCopy;
   }

   public int getFermentProgressionScaled() {
      int i = this.kegData.get(0);
      int j = this.kegData.get(1);
      return j != 0 && i != 0 ? i * 22 / j : 0;
   }

   public float getProgression() {
      return (float)this.kegData.get(0);
   }

   public int getKegTemperature() {
      return this.blockEntity.getTemperature();
   }

   public boolean isFermenting() {
      Optional<KegFermentingRecipe> recipe = this.blockEntity.getRecipeWithoutTemperature();
      return recipe.isPresent() && KegBlockEntity.isValidTemp(this.getKegTemperature(), recipe.get().getTemperature());
   }

   // Port: the vanilla placer (item ingredients only) until KegPlaceRecipe / KegStackedContents are ported.
   @Override
   @SuppressWarnings("unchecked")
   public void handlePlacement(boolean placeAll, Recipe<?> recipe, ServerPlayer player) {
      if (recipe instanceof KegFermentingRecipe) {
         new ServerPlaceRecipe<>(this).recipeClicked(player, (Recipe<KegRecipeWrapper>)recipe, placeAll);
      }
   }

   @Override
   public void fillCraftSlotsStackedContents(StackedContents helper) {
      for (int i = 0; i < this.inventory.getSlotCount(); i++) {
         helper.accountSimpleStack(this.inventory.getStackInSlot(i));
      }
   }

   @Override
   public void clearCraftingContent() {
      for (int i = 0; i < 4; i++) {
         this.inventory.setStackInSlot(i, ItemStack.EMPTY);
      }
   }

   @Override
   public boolean recipeMatches(Recipe<? super KegRecipeWrapper> recipe) {
      return recipe.matches(this.recipeWrapper, this.level);
   }

   @Override
   public int getResultSlotIndex() {
      return 5;
   }

   @Override
   public int getGridWidth() {
      return 2;
   }

   @Override
   public int getGridHeight() {
      return 2;
   }

   @Override
   public int getSize() {
      return 6;
   }

   @Override
   public RecipeBookType getRecipeBookType() {
      return BnCRecipeBookTypes.FERMENTING;
   }

   @Override
   public boolean shouldMoveToInventory(int slot) {
      return slot < this.getGridWidth() * this.getGridHeight();
   }
}
