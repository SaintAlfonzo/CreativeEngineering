package io.github.mortuusars.exposure.world.item.component;

import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import java.util.Optional;
import java.util.function.BiFunction;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.util.Backport;

public class StoredItemStack {
   public static final StoredItemStack EMPTY = new StoredItemStack(ItemStack.EMPTY);
   public static final Codec<StoredItemStack> CODEC = ItemStack.CODEC.xmap(StoredItemStack::new, StoredItemStack::getForReading);
   public static final StreamCodec<FriendlyByteBuf, StoredItemStack> STREAM_CODEC = ByteBufCodecs.ITEM_STACK
      .map(StoredItemStack::new, StoredItemStack::getForReading);
   private final ItemStack stack;

   public StoredItemStack(ItemStack stack) {
      this.stack = stack;
   }

   public ItemStack getForReading() {
      return this.stack;
   }

   public ItemStack getCopy() {
      return this.stack.copy();
   }

   public <T extends Item> ItemAndStack<T> getItemAndStackCopy() {
      return new ItemAndStack<>(this.getCopy());
   }

   public Item getItem() {
      return this.getForReading().getItem();
   }

   public boolean isEmpty() {
      return this.getForReading().isEmpty();
   }

   public <T, R> Optional<R> mapIf(Class<T> clazz, BiFunction<T, ItemStack, R> func) {
      return this.getForReading().getItem().getClass().equals(clazz)
         ? Optional.ofNullable(func.apply(clazz.cast(this.getForReading().getItem()), this.getForReading()))
         : Optional.empty();
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof StoredItemStack other ? Backport.isSameItemSameComponents(this.getForReading(), other.getForReading()) : false;
   }

   @Override
   public int hashCode() {
      return Backport.hashItemAndComponents(this.getForReading());
   }

   @Override
   public String toString() {
      return "ItemStackBox{stack=" + this.getForReading() + "}";
   }
}
