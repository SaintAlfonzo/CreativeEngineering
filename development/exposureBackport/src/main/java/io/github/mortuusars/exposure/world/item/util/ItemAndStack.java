package io.github.mortuusars.exposure.world.item.util;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import io.github.mortuusars.exposure.backport.util.Backport;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemAndStack<T extends Item> {
   private final T item;
   private final ItemStack stack;

   public ItemAndStack(ItemStack stack) {
      this.stack = stack;
      this.item = (T)stack.getItem();
   }

   public T getItem() {
      return this.item;
   }

   public ItemStack getItemStack() {
      return this.stack;
   }

   public ItemAndStack<T> apply(BiConsumer<T, ItemStack> function) {
      function.accept(this.item, this.stack);
      return this;
   }

   public <R> R map(BiFunction<T, ItemStack, R> mappingFunction) {
      return mappingFunction.apply(this.item, this.stack);
   }

   @Override
   public String toString() {
      return "ItemAndStack{" + this.stack.toString() + "}";
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ItemAndStack<?> that = (ItemAndStack<?>)o;
         // 1.19.2: data components are NBT-backed here, so re-reading one (e.g. StackedPhotographs) builds new ItemStack
         // instances each time. Upstream's identity compare would never match; compare contents instead.
         return Objects.equals(this.item, that.item) && ItemStack.matches(this.stack, that.stack);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.item, Backport.hashItemAndComponents(this.stack), this.stack.getCount());
   }
}
