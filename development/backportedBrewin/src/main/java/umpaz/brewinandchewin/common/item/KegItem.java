package umpaz.brewinandchewin.common.item;

import java.util.Optional;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;

public class KegItem extends BlockItem {
   private static final int BAR_COLOR = Mth.color(0.4F, 0.4F, 1.0F);

   public KegItem(Block block, Properties properties) {
      super(block, properties);
   }

   @Override
   public boolean isBarVisible(ItemStack stack) {
      return getServingCount(stack) > 0L;
   }

   @Override
   public int getBarWidth(ItemStack stack) {
      return (int)Math.min(1L + getServingCount(stack) / 77L, 13L);
   }

   @Override
   public int getBarColor(ItemStack stack) {
      return BAR_COLOR;
   }

   // Port: KegTooltipComponent lives in common code (upstream nests it in the client-only KegTooltip).
   @Override
   public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
      AbstractedFluidStack mealStack = KegBlockEntity.getMealFromItem(stack);
      return Optional.of(new KegTooltipComponent(mealStack));
   }

   private static long getServingCount(ItemStack stack) {
      if (BlockItem.getBlockEntityData(stack) == null) {
         return 0L;
      } else {
         AbstractedFluidStack mealStack = KegBlockEntity.getMealFromItem(stack);
         return mealStack.amount();
      }
   }
}
