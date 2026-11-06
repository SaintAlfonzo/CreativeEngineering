package com.jkvin114.displaydelight.events;

import com.jkvin114.displaydelight.block.AbstractStackablePlatedFoodBlock;
import com.jkvin114.displaydelight.block.SmallPlatedFoodBlock;
import com.jkvin114.displaydelight.init.BlockAssociations;
import com.jkvin114.displaydelight.init.DisplayBlocks;
import com.jkvin114.displaydelight.init.DisplayConfig;
import com.jkvin114.displaydelight.init.DisplayItems;
import com.jkvin114.displaydelight.init.DisplayTags;
import java.util.HashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class InterationManager {
   static boolean isCompatLoaded(AbstractStackablePlatedFoodBlock target) {
      return BlockAssociations.getPlatedItemFor(target) != Items.AIR;
   }

   static boolean isCompatLoaded(SmallPlatedFoodBlock target) {
      return BlockAssociations.getSmallPlatedItemFor(target) != Items.AIR;
   }

   public static boolean tryTakePlateWithAxe(Player player, ServerLevel world, BlockHitResult rez) {
      BlockPos pos = rez.getBlockPos();
      BlockState state = world.getBlockState(pos);
      if (state.getBlock() instanceof AbstractStackablePlatedFoodBlock target && target.hasPlate(state)) {
         BlockState newState = (BlockState)state.setValue(AbstractStackablePlatedFoodBlock.PLATE_HIDDEN, true);
         world.setBlock(pos, newState, 2);
         if (isCompatLoaded(target)) {
            Block.popResource(world, pos, new ItemStack((ItemLike)DisplayItems.PLATE.get(), 1));
         }

         world.playSound(null, pos, SoundEvents.AXE_STRIP, SoundSource.PLAYERS, 1.0F, (float)(0.8F + Math.random() * 0.2));
         return true;
      }

      if (state.getBlock() instanceof SmallPlatedFoodBlock target && target.hasPlate(state)) {
         BlockState newState = (BlockState)state.setValue(SmallPlatedFoodBlock.PLATE_HIDDEN, true);
         world.setBlock(pos, newState, 2);
         if (isCompatLoaded(target)) {
            Block.popResource(world, pos, new ItemStack((ItemLike)DisplayItems.SMALL_PLATE.get(), 1));
         }

         world.playSound(null, pos, SoundEvents.AXE_STRIP, SoundSource.PLAYERS, 1.0F, (float)(0.8F + Math.random() * 0.2));
         return true;
      }

      return false;
   }

   public static boolean testInsertPlate(Player player, Level world, BlockHitResult rez, InteractionHand handy) {
      BlockPos pos = rez.getBlockPos();
      BlockState state = world.getBlockState(pos);
      ItemStack stack = player.getItemInHand(handy);
      return stack.getItem() == DisplayItems.PLATE.get() && state.getBlock() instanceof AbstractStackablePlatedFoodBlock targetx && !targetx.hasPlate(state)
         ? true
         : stack.getItem() == DisplayItems.SMALL_PLATE.get() && state.getBlock() instanceof SmallPlatedFoodBlock target && !target.hasPlate(state);
   }

   public static boolean tryInsertPlate(Player player, ServerLevel world, BlockHitResult rez, InteractionHand handy) {
      BlockPos pos = rez.getBlockPos();
      BlockState state = world.getBlockState(pos);
      ItemStack stack = player.getItemInHand(handy);
      if (state.getBlock() instanceof AbstractStackablePlatedFoodBlock target && !target.hasPlate(state)) {
         BlockState newState = (BlockState)state.setValue(AbstractStackablePlatedFoodBlock.PLATE_HIDDEN, false);
         world.setBlock(pos, newState, 2);
         world.playSound(null, pos, state.getSoundType(world, pos, player).getPlaceSound(), SoundSource.PLAYERS, 1.0F, (float)(0.8F + Math.random() * 0.2));
         if (!player.isCreative() && isCompatLoaded(target)) {
            stack.shrink(1);
         }

         return true;
      }

      return false;
   }

   public static boolean tryInsertSmallPlate(Player player, ServerLevel world, BlockHitResult rez, InteractionHand handy) {
      BlockPos pos = rez.getBlockPos();
      BlockState state = world.getBlockState(pos);
      ItemStack stack = player.getItemInHand(handy);
      if (state.getBlock() instanceof SmallPlatedFoodBlock target && !target.hasPlate(state)) {
         BlockState newState = (BlockState)state.setValue(SmallPlatedFoodBlock.PLATE_HIDDEN, false);
         world.setBlock(pos, newState, 2);
         world.playSound(null, pos, state.getSoundType(world, pos, player).getPlaceSound(), SoundSource.PLAYERS, 1.0F, (float)(0.8F + Math.random() * 0.2));
         if (!player.isCreative() && isCompatLoaded(target)) {
            stack.shrink(1);
         }

         return true;
      }

      return false;
   }

   public static boolean tryTakeItemWithBareHand(Player player, ServerLevel world, BlockHitResult rez) {
      InteractionHand handy = InteractionHand.MAIN_HAND;
      BlockPos pos = rez.getBlockPos();
      BlockState state = world.getBlockState(pos);
      if (state.getBlock() instanceof AbstractStackablePlatedFoodBlock target) {
         Item plateItem = BlockAssociations.getPlatedItemFor(target);
         if (plateItem == Items.AIR && !player.isCreative()) {
            return false;
         } else {
            int count = 1;
            if (player.isShiftKeyDown() && !(Boolean)DisplayConfig.DISABLE_TAKE_ALL_INTERACTION.get()) {
               count = target.getStacks(state);
               world.setBlock(pos, target.hasPlate(state) ? ((Block)DisplayBlocks.PLATE.get()).defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);
            } else if (target.getStacks(state) > 1) {
               BlockState newState = target.getDecrementedState(state);
               world.setBlock(pos, newState, 2);
            } else {
               world.setBlock(pos, target.hasPlate(state) ? ((Block)DisplayBlocks.PLATE.get()).defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);
            }

            world.playSound(null, pos, SoundEvents.CHICKEN_EGG, SoundSource.PLAYERS, 1.0F, (float)(0.8F + Math.random() * 0.2));
            HashSet<Item> set = new HashSet<>();
            set.add(plateItem);
            if (player.getInventory().hasAnyOf(set)) {
               player.getInventory().add(new ItemStack(plateItem, count));
            } else {
               player.setItemInHand(handy, new ItemStack(plateItem, count));
            }

            player.swing(handy, true);
            return true;
         }
      } else if (state.getBlock() instanceof SmallPlatedFoodBlock targetx) {
         Item plateItem = BlockAssociations.getSmallPlatedItemFor(targetx);
         if (plateItem == Items.AIR && !player.isCreative()) {
            return false;
         } else {
            world.setBlock(pos, targetx.hasPlate(state) ? ((Block)DisplayBlocks.SMALL_PLATE.get()).defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);
            world.playSound(null, pos, SoundEvents.CHICKEN_EGG, SoundSource.PLAYERS, 1.0F, (float)(0.8F + Math.random() * 0.2));
            HashSet<Item> set = new HashSet<>();
            set.add(plateItem);
            if (player.getInventory().hasAnyOf(set)) {
               player.getInventory().add(new ItemStack(plateItem, 1));
            } else {
               player.setItemInHand(handy, new ItemStack(plateItem, 1));
            }

            player.swing(handy, true);
            return true;
         }
      } else {
         return false;
      }
   }

   public static boolean tryPlaceItemOnSmallPlate(Player player, ServerLevel world, BlockHitResult rez, boolean isMainHand) {
      InteractionHand handy = isMainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
      ItemStack stack = player.getItemInHand(handy);
      if (!stack.is(DisplayTags.SMALL_PLATE_DISPLAYABLE)) {
         return false;
      } else {
         BlockPos pos = rez.getBlockPos();
         BlockState state = world.getBlockState(pos);
         if (!state.is((Block)DisplayBlocks.SMALL_PLATE.get())) {
            return false;
         } else if (BlockAssociations.getSmallPlateBlockFor(stack.getItem()) instanceof SmallPlatedFoodBlock food) {
            UseOnContext ctx = new UseOnContext(player, handy, rez);
            BlockState newstate = food.getStateFrom(world, state, pos, ctx.getHorizontalDirection());
            world.setBlock(pos, newstate, 2);
            world.playSound(null, pos, state.getSoundType(world, pos, player).getPlaceSound(), SoundSource.BLOCKS, 1.0F, (float)(0.8F + Math.random() * 0.2));
            if (!player.isCreative()) {
               stack.shrink(1);
            }

            player.swing(handy, true);
            return true;
         } else {
            return false;
         }
      }
   }

   public static boolean tryPlaceItemOnPlate(Player player, ServerLevel world, BlockHitResult rez, boolean isMainHand) {
      InteractionHand handy = isMainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
      ItemStack handStack = player.getItemInHand(handy);
      if (!handStack.is(DisplayTags.PLATE_DISPLAYABLE)) {
         return false;
      } else {
         BlockPos pos = rez.getBlockPos();
         BlockState state = world.getBlockState(pos);
         if (state.getBlock() instanceof AbstractStackablePlatedFoodBlock target) {
            if (handStack.is(target.getStackFor().getItem()) && target.getStacks(state) < target.getMaxStackable()) {
               int count = 1;
               if (player.isShiftKeyDown() && !(Boolean)DisplayConfig.DISABLE_PLACE_ALL_INTERACTION.get()) {
                  int stacksLeft = target.getMaxStackable() - target.getStacks(state);
                  count = player.isCreative() ? stacksLeft : Math.min(stacksLeft, handStack.getCount());
               }

               world.setBlock(pos, target.getIncrementedState(state, count), 2);
               world.playSound(null, pos, state.getSoundType(world, pos, player).getPlaceSound(), SoundSource.BLOCKS, 1.0F, (float)(0.8F + Math.random() * 0.2));
               if (!player.isCreative()) {
                  handStack.shrink(count);
               }

               player.swing(handy, true);
               return true;
            }
         } else if (state.is((Block)DisplayBlocks.PLATE.get())) {
            if (BlockAssociations.getPlateBlockFor(handStack.getItem()) instanceof AbstractStackablePlatedFoodBlock targetx) {
               int countx = 1;
               if (player.isShiftKeyDown() && !(Boolean)DisplayConfig.DISABLE_PLACE_ALL_INTERACTION.get()) {
                  int stacksLeft = targetx.getMaxStackable();
                  countx = player.isCreative() ? stacksLeft : Math.min(stacksLeft, handStack.getCount());
               }

               UseOnContext ctx = new UseOnContext(player, handy, rez);
               BlockState newstate = targetx.getStateFrom(world, state, pos, ctx.getHorizontalDirection(), countx);
               world.setBlock(pos, newstate, 2);
               world.playSound(null, pos, state.getSoundType(world, pos, player).getPlaceSound(), SoundSource.BLOCKS, 1.0F, (float)(0.8F + Math.random() * 0.2));
               if (!player.isCreative()) {
                  handStack.shrink(countx);
               }

               player.swing(handy, true);
               return true;
            }

            return false;
         }

         return false;
      }
   }

   public static boolean tryPlaceItem(Player player, ServerLevel world, BlockHitResult rez, boolean isMainHand) {
      InteractionHand handy = isMainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
      ItemStack stack = player.getItemInHand(handy);
      if (!stack.is(DisplayTags.DISPLAYABLE)) {
         return false;
      } else {
         BlockPos pos = rez.getBlockPos();
         if (player.isShiftKeyDown()) {
            Block target = BlockAssociations.getBlockFor(stack.getItem());
            if (target.asItem().useOn(new UseOnContext(player, handy, rez)) == InteractionResult.CONSUME) {
               world.playSound(
                  null,
                  pos.above(),
                  target.defaultBlockState().getSoundType(world, pos.above(), player).getPlaceSound(),
                  SoundSource.BLOCKS,
                  1.0F,
                  (float)(0.8F + Math.random() * 0.2)
               );
               player.swing(handy, true);
               return true;
            }
         } else if (world.getBlockState(pos).isFaceSturdy(world, pos, Direction.UP) && rez.getDirection() == Direction.UP && (Boolean)DisplayConfig.MESSAGE.get()) {
            player.displayClientMessage(Component.translatable("item.displaydelight.tooltip.displayable"), true);
         }

         return false;
      }
   }
}
