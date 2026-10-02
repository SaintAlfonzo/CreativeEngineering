package io.github.mortuusars.exposure.world.inventory.slot;

import com.google.common.base.Preconditions;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class FilteredSlot extends Slot {
   protected final Consumer<FilteredSlot.SlotChangedArgs> onItemChanged;
   protected final int maxStackSize;
   protected final int slot;
   protected final Predicate<ItemStack> mayPlacePredicate;

   public FilteredSlot(
      Container container,
      int slot,
      int x,
      int y,
      int maxStackSize,
      Consumer<FilteredSlot.SlotChangedArgs> onItemChanged,
      Predicate<ItemStack> mayPlacePredicate
   ) {
      super(container, slot, x, y);
      Preconditions.checkArgument(maxStackSize > 0 && maxStackSize <= 64, maxStackSize + " is not valid. (1-64)");
      this.slot = slot;
      this.maxStackSize = maxStackSize;
      this.onItemChanged = onItemChanged;
      this.mayPlacePredicate = mayPlacePredicate;
   }

   public int getSlotId() {
      return this.slot;
   }

   public int getMaxStackSize() {
      return Math.min(this.container.getMaxStackSize(), this.maxStackSize);
   }

   public boolean mayPlace(ItemStack stack) {
      return this.mayPlacePredicate.test(stack);
   }

   public void set(ItemStack stack) {
      ItemStack oldStack = this.getItem().copy();
      super.set(stack);
      this.onItemChanged.accept(new FilteredSlot.SlotChangedArgs(this, oldStack, this.getItem()));
   }

   @NotNull
   public ItemStack remove(int amount) {
      ItemStack oldStack = this.getItem().copy();
      ItemStack removed = super.remove(amount);
      ItemStack newStack = this.getItem();
      this.onItemChanged.accept(new FilteredSlot.SlotChangedArgs(this, oldStack, newStack));
      return removed;
   }

   public static record SlotChangedArgs(FilteredSlot slot, ItemStack oldStack, ItemStack newStack) {
   }
}
