package io.github.mortuusars.exposure.world.item;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.gui.ClientGUI;
import io.github.mortuusars.exposure.world.inventory.tooltip.PhotographTooltip;
import io.github.mortuusars.exposure.world.item.component.StackedPhotographs;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import java.util.Optional;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

public class StackedPhotographsItem extends Item {
   public StackedPhotographsItem(Properties properties) {
      super(properties);
   }

   public int getStackLimit() {
      return (Integer)Config.Server.STACKED_PHOTOGRAPHS_MAX_SIZE.get();
   }

   public StackedPhotographs getPhotographs(ItemStack stack) {
      return (StackedPhotographs)Exposure.DataComponents.STACKED_PHOTOGRAPHS.getOrDefault(stack, StackedPhotographs.EMPTY);
   }

   public void setPhotographs(ItemStack stack, StackedPhotographs photographs) {
      Exposure.DataComponents.STACKED_PHOTOGRAPHS.set(stack, photographs);
   }

   public boolean canAddPhotograph(ItemStack stack) {
      return this.getPhotographs(stack).size() < this.getStackLimit();
   }

   public void addPhotograph(ItemStack stack, ItemStack photographStack, int index) {
      Preconditions.checkElementIndex(index, this.getPhotographs(stack).size() + 1);
      Preconditions.checkState(
         this.canAddPhotograph(stack),
         "Cannot add more photographs than this stack can store. Size: " + this.getPhotographs(stack).size() + ", Max count: " + this.getStackLimit()
      );
      Preconditions.checkArgument(photographStack.getItem() instanceof PhotographItem, "Only PhotographItem can be stacked.");
      this.setPhotographs(stack, this.getPhotographs(stack).add(index, photographStack));
   }

   public void addPhotographOnTop(ItemStack stack, ItemStack photographStack) {
      this.addPhotograph(stack, photographStack, 0);
   }

   public void addPhotographToBottom(ItemStack stack, ItemStack photographStack) {
      this.addPhotograph(stack, photographStack, this.getPhotographs(stack).size());
   }

   public ItemAndStack<PhotographItem> removePhotograph(ItemStack stack, int index) {
      Preconditions.checkElementIndex(index, this.getPhotographs(stack).size());
      StackedPhotographs stackedPhotographs = this.getPhotographs(stack);
      ItemStack removedPhotograph = stackedPhotographs.getItemUnsafe(index);
      this.setPhotographs(stack, stackedPhotographs.remove(index));
      return new ItemAndStack<>(removedPhotograph.copy());
   }

   public ItemAndStack<PhotographItem> removeTopPhotograph(ItemStack stack) {
      return this.removePhotograph(stack, 0);
   }

   public ItemAndStack<PhotographItem> removeBottomPhotograph(ItemStack stack) {
      return this.removePhotograph(stack, this.getPhotographs(stack).size() - 1);
   }

   @NotNull
   public Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack stack) {
      StackedPhotographs photographs = this.getPhotographs(stack);
      return photographs.isEmpty() ? Optional.empty() : Optional.of(new PhotographTooltip(photographs.photographsItemAndStacks()));
   }

   public boolean overrideStackedOnOther(@NotNull ItemStack stack, @NotNull Slot slot, @NotNull ClickAction action, @NotNull Player player) {
      if (action == ClickAction.SECONDARY && !this.getPhotographs(stack).isEmpty() && slot.mayPlace(new ItemStack((ItemLike)Exposure.Items.PHOTOGRAPH.get()))) {
         ItemStack slotItem = slot.getItem();
         if (slotItem.isEmpty()) {
            ItemAndStack<PhotographItem> photograph = this.removeBottomPhotograph(stack);
            slot.set(photograph.getItemStack());
            if (this.getPhotographs(stack).size() == 1) {
               player.containerMenu.setCarried(this.removeTopPhotograph(stack).getItemStack());
            }

            playRemoveSoundClientside(player);
            return true;
         } else if (slotItem.getItem() instanceof PhotographItem && this.canAddPhotograph(stack)) {
            this.addPhotographToBottom(stack, slotItem);
            slot.set(ItemStack.EMPTY);
            playAddSoundClientside(player);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean overrideOtherStackedOnMe(
      @NotNull ItemStack stack, @NotNull ItemStack other, @NotNull Slot slot, @NotNull ClickAction action, @NotNull Player player, @NotNull SlotAccess access
   ) {
      if (action != ClickAction.SECONDARY || !slot.mayPlace(new ItemStack((ItemLike)Exposure.Items.PHOTOGRAPH.get()))) {
         return false;
      } else if (!this.getPhotographs(stack).isEmpty() && other.isEmpty()) {
         ItemAndStack<PhotographItem> photograph = this.removeTopPhotograph(stack);
         access.set(photograph.getItemStack());
         if (this.getPhotographs(stack).size() == 1) {
            ItemAndStack<PhotographItem> lastPhotograph = this.removeTopPhotograph(stack);
            slot.set(lastPhotograph.getItemStack());
         }

         playRemoveSoundClientside(player);
         return true;
      } else if (!(other.getItem() instanceof PhotographItem)) {
         if (other.getItem() instanceof StackedPhotographsItem otherStackedItem) {
            int otherCount = otherStackedItem.getPhotographs(other).size();
            int addedCount = 0;

            for (int i = 0; i < otherCount; i++) {
               if (this.canAddPhotograph(stack)) {
                  ItemAndStack<PhotographItem> photograph = otherStackedItem.removeBottomPhotograph(other);
                  this.addPhotographOnTop(stack, photograph.getItemStack());
                  addedCount++;
               }
            }

            if (otherStackedItem.getPhotographs(other).isEmpty()) {
               access.set(ItemStack.EMPTY);
            } else if (otherStackedItem.getPhotographs(other).size() == 1) {
               access.set(otherStackedItem.removeTopPhotograph(other).getItemStack());
            }

            if (addedCount > 0) {
               playAddSoundClientside(player);
            }

            return true;
         } else {
            return false;
         }
      } else if (this.canAddPhotograph(stack)) {
         this.addPhotographOnTop(stack, other);
         access.set(ItemStack.EMPTY);
         playAddSoundClientside(player);
         return true;
      } else {
         return false;
      }
   }

   @NotNull
   public InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
      ItemStack itemInHand = player.getItemInHand(hand);
      if (player.isSecondaryUseActive() && this.cyclePhotographs(itemInHand)) {
         player.level
            .playSound(
               player, player, Exposure.SoundEvents.PHOTOGRAPH_RUSTLE.get(), SoundSource.PLAYERS, 0.6F, player.level.getRandom().nextFloat() * 0.2F + 1.2F
            );
         player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
         return InteractionResultHolder.success(itemInHand);
      } else {
         StackedPhotographs photographs = this.getPhotographs(itemInHand);
         if (!photographs.isEmpty()) {
            if (level.isClientSide) {
               int slot = hand == InteractionHand.OFF_HAND ? 40 : player.getInventory().selected;
               ClientGUI.openPhotographsScreenFromItem(slot);
               player.playSound(Exposure.SoundEvents.PHOTOGRAPH_RUSTLE.get(), 0.6F, 1.1F);
            }

            return InteractionResultHolder.success(itemInHand);
         } else {
            return InteractionResultHolder.fail(itemInHand);
         }
      }
   }

   public boolean cyclePhotographs(ItemStack stack) {
      if (this.getPhotographs(stack).size() < 2) {
         return false;
      } else {
         ItemAndStack<PhotographItem> topPhotograph = this.removeTopPhotograph(stack);
         this.addPhotographToBottom(stack, topPhotograph.getItemStack());
         return true;
      }
   }

   public static void playAddSoundClientside(Player player) {
      if (player.level.isClientSide) {
         player.playSound(Exposure.SoundEvents.PHOTOGRAPH_RUSTLE.get(), 0.6F, player.level.getRandom().nextFloat() * 0.2F + 1.2F);
      }
   }

   public static void playRemoveSoundClientside(Player player) {
      if (player.level.isClientSide) {
         player.playSound(Exposure.SoundEvents.PHOTOGRAPH_RUSTLE.get(), 0.75F, player.level.getRandom().nextFloat() * 0.2F + 0.75F);
      }
   }
}
