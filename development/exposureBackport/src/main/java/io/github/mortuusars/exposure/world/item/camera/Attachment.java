package io.github.mortuusars.exposure.world.item.camera;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.item.FilmRollItem;
import io.github.mortuusars.exposure.world.item.component.StoredItemStack;
import io.github.mortuusars.exposure.world.sound.Sound;
import io.github.mortuusars.exposure.world.sound.SoundEffect;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import io.github.mortuusars.exposure.backport.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record Attachment<T extends Item>(
   ResourceLocation id,
   DataComponentType<StoredItemStack> component,
   Predicate<ItemStack> itemPredicate,
   Class<T> itemType,
   Supplier<Integer> maxCount,
   Optional<SoundEffect> insertedSound,
   Optional<SoundEffect> removedSound
) {
   public static final Attachment<FilmRollItem> FILM = new Attachment<>(
      Exposure.resource("film"),
      Exposure.DataComponents.FILM,
      stack -> stack.getItem() instanceof FilmRollItem,
      FilmRollItem.class,
      () -> 1,
      new SoundEffect(Exposure.SoundEvents.FILM_ADVANCE, 0.9F, 1.0F),
      new SoundEffect(Exposure.SoundEvents.FILM_REMOVED, 0.7F, 1.0F)
   );
   public static final Attachment<Item> FLASH = new Attachment<>(
      Exposure.resource("flash"),
      Exposure.DataComponents.FLASH,
      stack -> stack.is(Exposure.Tags.Items.FLASHES),
      Item.class,
      () -> 1,
      new SoundEffect(Exposure.SoundEvents.CAMERA_GENERIC_CLICK, 0.6F, 1.15F),
      new SoundEffect(Exposure.SoundEvents.CAMERA_GENERIC_CLICK, 0.35F, 0.95F)
   );
   public static final Attachment<Item> LENS = new Attachment<>(
      Exposure.resource("lens"),
      Exposure.DataComponents.LENS,
      stack -> stack.is(Exposure.Tags.Items.LENSES),
      Item.class,
      () -> 1,
      new SoundEffect(Exposure.SoundEvents.LENS_INSERT),
      new SoundEffect(Exposure.SoundEvents.LENS_REMOVE)
   );
   public static final Attachment<Item> FILTER = new Attachment<>(
      Exposure.resource("filter"),
      Exposure.DataComponents.FILTER,
      stack -> stack.is(Exposure.Tags.Items.FILTERS),
      Item.class,
      () -> 1,
      new SoundEffect(Exposure.SoundEvents.FILTER_INSERT),
      new SoundEffect(Exposure.SoundEvents.FILTER_REMOVE, 0.5F)
   );

   public Attachment(
      ResourceLocation id,
      DataComponentType<StoredItemStack> component,
      Predicate<ItemStack> itemPredicate,
      Class<T> itemType,
      Supplier<Integer> maxCount,
      SoundEffect insertedSound,
      SoundEffect removedSound
   ) {
      this(id, component, itemPredicate, itemType, maxCount, Optional.of(insertedSound), Optional.of(removedSound));
   }

   public Attachment(
      ResourceLocation id, DataComponentType<StoredItemStack> component, Predicate<ItemStack> itemPredicate, Class<T> itemType, Supplier<Integer> maxCount
   ) {
      this(id, component, itemPredicate, itemType, maxCount, Optional.empty(), Optional.empty());
   }

   public boolean matches(ItemStack stack) {
      return this.itemPredicate.test(stack);
   }

   public boolean isEmpty(ItemStack stack) {
      StoredItemStack storedItemStack = this.get(stack);
      return storedItemStack.isEmpty() || !this.itemType.isInstance(storedItemStack.getItem());
   }

   public boolean isPresent(ItemStack stack) {
      return !this.isEmpty(stack);
   }

   public StoredItemStack get(ItemStack stack) {
      return (StoredItemStack)this.component.getOrDefault(stack, StoredItemStack.EMPTY);
   }

   public Attachment<T> ifPresent(ItemStack stack, BiConsumer<T, ItemStack> ifPresent) {
      StoredItemStack storedItemStack = this.get(stack);
      if (this.itemType.isInstance(storedItemStack.getItem())) {
         ifPresent.accept(this.itemType.cast(storedItemStack.getItem()), storedItemStack.getForReading());
      }

      return this;
   }

   public Attachment<T> ifPresent(ItemStack stack, Consumer<ItemStack> ifPresent) {
      StoredItemStack storedItemStack = this.get(stack);
      if (this.itemType.isInstance(storedItemStack.getItem())) {
         ifPresent.accept(storedItemStack.getForReading());
      }

      return this;
   }

   public Attachment<T> orElse(ItemStack stack, Runnable orElse) {
      StoredItemStack storedItemStack = this.get(stack);
      if (!this.itemType.isInstance(storedItemStack.getItem())) {
         orElse.run();
      }

      return this;
   }

   public Attachment<T> ifPresentOrElse(ItemStack stack, BiConsumer<T, ItemStack> ifPresent, Runnable orElse) {
      return this.ifPresent(stack, ifPresent).orElse(stack, orElse);
   }

   public <R> Optional<R> map(ItemStack stack, Function<ItemStack, R> mappingFunc) {
      return this.mapOrElse(stack, st -> Optional.of(mappingFunc.apply(st)), Optional::empty);
   }

   public <R> Optional<R> map(ItemStack stack, BiFunction<T, ItemStack, R> mappingFunc) {
      return this.mapOrElse(stack, (it, st) -> Optional.of(mappingFunc.apply(it, st)), Optional::empty);
   }

   public <R> R mapOrElse(ItemStack stack, Function<ItemStack, R> ifPresentMappingFunc, Supplier<R> orElseSupplier) {
      StoredItemStack storedItemStack = this.get(stack);
      return this.itemType.isInstance(storedItemStack.getItem()) ? ifPresentMappingFunc.apply(storedItemStack.getForReading()) : orElseSupplier.get();
   }

   public <R> R mapOrElse(ItemStack stack, BiFunction<T, ItemStack, R> ifPresentMappingFunc, Supplier<R> orElseSupplier) {
      StoredItemStack storedItemStack = this.get(stack);
      return this.itemType.isInstance(storedItemStack.getItem())
         ? ifPresentMappingFunc.apply(this.itemType.cast(storedItemStack.getItem()), storedItemStack.getForReading())
         : orElseSupplier.get();
   }

   public Attachment<T> set(ItemStack stack, ItemStack attachment) {
      if (attachment.isEmpty()) {
         this.component.remove(stack);
      } else {
         this.component.set(stack, new StoredItemStack(attachment));
      }

      return this;
   }

   public void playInsertSoundSided(Entity entity) {
      this.insertedSound().ifPresent(sound -> Sound.playUniqueSided(Integer.toString(entity.getId()), entity, sound, SoundSource.PLAYERS));
   }

   public void playRemoveSoundSided(Entity entity) {
      this.removedSound().ifPresent(sound -> Sound.playUniqueSided(Integer.toString(entity.getId()), entity, sound, SoundSource.PLAYERS));
   }

   @Override
   public String toString() {
      return "Attachment{id='" + this.id + "'}";
   }
}
