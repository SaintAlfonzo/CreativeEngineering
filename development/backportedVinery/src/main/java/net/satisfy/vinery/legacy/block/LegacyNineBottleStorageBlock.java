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

public class LegacyNineBottleStorageBlock extends StorageBlock {
   public LegacyNineBottleStorageBlock(Properties settings) {
      super(settings);
   }

   public int size() {
      return 9;
   }

   public boolean canInsertStack(ItemStack stack) {
      return stack.getItem() instanceof DrinkBlockItem;
   }

   public ResourceLocation type() {
      return LegacyStorageTypes.NINE_BOTTLE;
   }

   public Direction[] unAllowedDirections() {
      return new Direction[]{Direction.DOWN, Direction.UP};
   }

   public int getSection(Float x, Float y) {
      float l = 0.33333334F;
      int nSection;
      if (x < 0.375F) {
         nSection = 0;
      } else {
         nSection = x < 0.6875F ? 1 : 2;
      }

      int i = y >= l * 2.0F ? 0 : (y >= l ? 1 : 2);
      return nSection + i * 3;
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
