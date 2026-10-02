package io.github.mortuusars.exposure.world.item;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.PlatformHelper;
import io.github.mortuusars.exposure.client.gui.ClientGUI;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.inventory.tooltip.PhotographTooltip;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import io.github.mortuusars.exposure.world.level.storage.ExposureIdentifier;
import io.github.mortuusars.exposure.world.photograph.PhotographType;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PhotographItem extends Item {
   public PhotographItem(Properties properties) {
      super(properties);
   }

   public PhotographType getType(ItemStack stack) {
      return PhotographType.REGULAR;
   }

   public Frame getFrame(ItemStack stack) {
      return (Frame)Exposure.DataComponents.PHOTOGRAPH_FRAME.getOrDefault(stack, Frame.EMPTY);
   }

   @NotNull
   public Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack stack) {
      ExposureIdentifier identifier = this.getFrame(stack).identifier();
      return !identifier.isEmpty() ? Optional.of(new PhotographTooltip(List.of(new ItemAndStack<>(stack)))) : Optional.empty();
   }

   public void appendHoverText(ItemStack stack, @Nullable Level context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
      Integer generation = (Integer)Exposure.DataComponents.PHOTOGRAPH_GENERATION.get(stack);
      if (generation != null && generation > 0) {
         tooltipComponents.add(Component.translatable("item.exposure.photograph.generation." + generation).withStyle(ChatFormatting.GRAY));
      }

      Frame frame = (Frame)Exposure.DataComponents.PHOTOGRAPH_FRAME.get(stack);
      if (frame != null) {
         String photographerName = frame.photographer().name();
         if ((Boolean)Config.Client.PHOTOGRAPH_SHOW_PHOTOGRAPHER_IN_TOOLTIP.get() && !photographerName.isEmpty()) {
            tooltipComponents.add(
               Component.translatable(
                     "item.exposure.photograph.photographer_tooltip", new Object[]{Component.literal(photographerName).withStyle(ChatFormatting.WHITE)}
                  )
                  .withStyle(ChatFormatting.GRAY)
            );
         }

         if ((Boolean)Config.Client.RECIPE_TOOLTIPS_WITHOUT_JEI.get()) {
            boolean jeiLoaded = PlatformHelper.isModLoaded("jei");
            if ((generation == null || generation < 2) && !jeiLoaded) {
               ClientGUI.addPhotographCopyingTooltip(stack, context, tooltipComponents, tooltipFlag);
            }
         }

         if (tooltipFlag.isAdvanced()) {
            String identifier = frame.identifier().map(id -> "Id: " + id, texture -> "Texture: " + texture);
            tooltipComponents.add(Component.literal(identifier).withStyle(ChatFormatting.DARK_GRAY));
         }
      }
   }

   @NotNull
   public InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
      ItemStack itemInHand = player.getItemInHand(hand);
      Frame frame = this.getFrame(itemInHand);
      if (frame != Frame.EMPTY && !frame.identifier().isEmpty()) {
         if (level.isClientSide) {
            int slot = hand == InteractionHand.OFF_HAND ? 40 : player.getInventory().selected;
            ClientGUI.openPhotographsScreenFromItem(slot);
            player.playSound(Exposure.SoundEvents.PHOTOGRAPH_RUSTLE.get(), 0.6F, 1.1F);
         }

         return InteractionResultHolder.success(itemInHand);
      } else {
         return InteractionResultHolder.pass(itemInHand);
      }
   }

   public boolean overrideOtherStackedOnMe(
      @NotNull ItemStack stack, @NotNull ItemStack other, @NotNull Slot slot, @NotNull ClickAction action, @NotNull Player player, @NotNull SlotAccess access
   ) {
      if (action != ClickAction.SECONDARY) {
         return false;
      } else if (other.getItem() instanceof PhotographItem) {
         StackedPhotographsItem stackedPhotographsItem = Exposure.Items.STACKED_PHOTOGRAPHS.get();
         ItemStack stackedPhotographsStack = new ItemStack(stackedPhotographsItem);
         stackedPhotographsItem.addPhotographOnTop(stackedPhotographsStack, stack);
         stackedPhotographsItem.addPhotographOnTop(stackedPhotographsStack, other);
         slot.set(stackedPhotographsStack);
         access.set(ItemStack.EMPTY);
         StackedPhotographsItem.playAddSoundClientside(player);
         return true;
      } else {
         return false;
      }
   }
}
