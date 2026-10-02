package io.github.mortuusars.exposure.world.item;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.capture.DitherMode;
import io.github.mortuusars.exposure.world.camera.capture.Projection;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.screens.Screen;
import io.github.mortuusars.exposure.backport.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class InterplanarProjectorItem extends Item {
   public InterplanarProjectorItem(Properties properties) {
      super(properties);
   }

   public DitherMode getMode(ItemStack stack) {
      return (DitherMode)Exposure.DataComponents.INTERPLANAR_PROJECTOR_MODE.getOrDefault(stack, DitherMode.DITHERED);
   }

   public void setMode(ItemStack stack, DitherMode mode) {
      Exposure.DataComponents.INTERPLANAR_PROJECTOR_MODE.set(stack, mode);
   }

   public boolean isConsumable(ItemStack stack) {
      return this.isAllowed();
   }

   protected boolean isAllowed() {
      return (Boolean)Config.Server.CAN_PROJECT.get();
   }

   public void appendHoverText(ItemStack stack, @Nullable Level context, List<Component> components, TooltipFlag tooltipFlag) {
      if (!this.isAllowed()) {
         components.add(Component.translatable("item.exposure.interplanar_projector.tooltip.disabled"));
      }

      if (this.getProjection(stack).isPresent()) {
         components.add(this.getMode(stack).translate());
      }

      if (Screen.hasShiftDown()) {
         if (this.isConsumable(stack)) {
            components.add(Component.translatable("item.exposure.interplanar_projector.tooltip.consumed_info"));
         }

         components.add(Component.translatable("item.exposure.interplanar_projector.tooltip.info"));
         if (this.getProjection(stack).isPresent()) {
            components.add(Component.translatable("item.exposure.interplanar_projector.tooltip.change_mode_info"));
         }
      } else {
         components.add(Component.translatable("tooltip.exposure.hold_for_details"));
      }
   }

   public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
      if (other.isEmpty() && action == ClickAction.SECONDARY && this.getProjection(stack).isPresent()) {
         this.setMode(stack, this.getMode(stack).cycle());
         slot.setChanged();
         if (player.level.isClientSide) {
            player.playSound(Exposure.SoundEvents.CAMERA_GENERIC_CLICK.get(), 0.6F, 1.0F);
         }

         return true;
      } else {
         return super.overrideOtherStackedOnMe(stack, other, slot, action, player, access);
      }
   }

   public Optional<String> getPath(ItemStack stack) {
      Component customName = (Component)DataComponents.CUSTOM_NAME.get(stack);
      return customName != null ? Optional.of(customName.getString()) : Optional.empty();
   }

   public Optional<Projection> getProjection(ItemStack stack) {
      return this.isAllowed() ? this.getPath(stack).map(filepath -> new Projection(filepath, this.getMode(stack))) : Optional.empty();
   }
}
