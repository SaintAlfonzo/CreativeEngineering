package io.github.mortuusars.exposure.world.item.component;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import net.minecraft.network.FriendlyByteBuf;
import io.github.mortuusars.exposure.backport.util.Backport;

public record StackedPhotographs(List<ItemStack> photographs) {
   public static final StackedPhotographs EMPTY = new StackedPhotographs(List.of());
   public static final Codec<StackedPhotographs> CODEC = ItemStack.CODEC.listOf().xmap(StackedPhotographs::new, StackedPhotographs::photographs);
   public static final StreamCodec<FriendlyByteBuf, StackedPhotographs> STREAM_CODEC = ByteBufCodecs.ITEM_STACK
      .apply(ByteBufCodecs.list())
      .map(StackedPhotographs::new, StackedPhotographs::photographs);

   public ItemStack getItemUnsafe(int index) {
      return this.photographs.get(index);
   }

   public List<ItemStack> photographsCopy() {
      return Lists.transform(this.photographs, ItemStack::copy);
   }

   public Stream<ItemStack> photographsCopyStream() {
      return this.photographs.stream().map(ItemStack::copy);
   }

   public List<ItemAndStack<PhotographItem>> photographsItemAndStacks() {
      return this.photographs.stream().filter(stack -> stack.getItem() instanceof PhotographItem).map(stack -> new ItemAndStack<PhotographItem>(stack)).toList();
   }

   public int size() {
      return this.photographs.size();
   }

   public boolean isEmpty() {
      return this.photographs.isEmpty();
   }

   public StackedPhotographs add(int index, ItemStack photograph) {
      ArrayList<ItemStack> list = new ArrayList<>(this.photographs);
      list.add(index, photograph);
      return new StackedPhotographs(list);
   }

   public StackedPhotographs remove(int index) {
      ArrayList<ItemStack> list = new ArrayList<>(this.photographs);
      list.remove(index);
      return new StackedPhotographs(list);
   }

   @Override
   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else {
         if (object instanceof StackedPhotographs stackedPhotographs && Backport.listMatches(this.photographs, stackedPhotographs.photographs)) {
            return true;
         }

         return false;
      }
   }

   @Override
   public int hashCode() {
      return Backport.hashStackList(this.photographs);
   }

   @NotNull
   @Override
   public String toString() {
      return "StackedPhotographs" + this.photographs;
   }
}
