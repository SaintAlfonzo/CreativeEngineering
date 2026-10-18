package net.satisfy.vinery.legacy.block;

import net.satisfy.foundation.storage.StorageBlock;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.satisfy.vinery.core.item.DrinkBlockItem;
import net.satisfy.vinery.legacy.LegacyStorageTypes;

public class LegacyFourBottleStorageBlock extends StorageBlock {
   public LegacyFourBottleStorageBlock(Properties settings) {
      super(settings);
   }

   public boolean canInsertStack(ItemStack stack) {
      return stack.getItem() instanceof DrinkBlockItem;
   }

   public int size() {
      return 4;
   }

   public ResourceLocation type() {
      return LegacyStorageTypes.FOUR_BOTTLE;
   }

   public Direction[] unAllowedDirections() {
      return new Direction[]{Direction.DOWN, Direction.UP};
   }

   public int getSection(Float x, Float y) {
      if ((double)x.floatValue() > 0.375 && (double)x.floatValue() < 0.625) {
         if ((double)y.floatValue() >= 0.55) {
            return 0;
         }

         if ((double)y.floatValue() <= 0.45) {
            return 3;
         }
      } else if ((double)y.floatValue() > 0.35 && (double)y.floatValue() < 0.65) {
         if ((double)x.floatValue() < 0.4) {
            return 1;
         }

         if ((double)x.floatValue() > 0.65) {
            return 2;
         }
      }

      return Integer.MIN_VALUE;
   }

   public void appendHoverText(ItemStack itemStack, BlockGetter world, List<Component> tooltip, TooltipFlag tooltipContext) {
      tooltip.add(Component.translatable("block.vinery.winebox_small.tooltip.shift_1").withStyle(new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.GRAY}));
   }

   /** Port: 1.3.10 used doapi:storage; the shared storage block entity is vinery:storage (ids rewritten on load, W2). */
   @Override
   public net.minecraft.world.level.block.entity.BlockEntityType<?> blockEntityType() {
      return net.satisfy.vinery.core.registry.EntityTypeRegistry.STORAGE_ENTITY.get();
   }
}
