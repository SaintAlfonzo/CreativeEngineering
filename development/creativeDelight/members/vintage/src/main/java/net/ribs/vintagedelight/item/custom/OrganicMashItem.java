package net.ribs.vintagedelight.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class OrganicMashItem extends BoneMealItem {
   public OrganicMashItem(Properties pProperties) {
      super(pProperties);
   }

   public InteractionResult useOn(UseOnContext context) {
      Level level = context.getLevel();
      BlockPos blockpos = context.getClickedPos();
      Player player = context.getPlayer();
      ItemStack itemStack = context.getItemInHand();
      if (!level.isClientSide) {
         boolean success = false;
         int numberOfApplications = 3;

         // Each successful applyBonemeal shrinks the stack it gets; apply on a copy so one use costs exactly one mash
         // (plan_scratch_findings_1121 F4, R5).
         ItemStack applied = itemStack.copy();
         applied.setCount(numberOfApplications);
         for (int i = 0; i < numberOfApplications; i++) {
            success |= BoneMealItem.applyBonemeal(applied, level, blockpos, player);
         }

         if (success) {
            level.levelEvent(2005, blockpos, 0);
            itemStack.shrink(1);
            return InteractionResult.sidedSuccess(false);
         }
      }

      return InteractionResult.PASS;
   }

   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      player.startUsingItem(hand);
      return new InteractionResultHolder(InteractionResult.SUCCESS, player.getItemInHand(hand));
   }
}
