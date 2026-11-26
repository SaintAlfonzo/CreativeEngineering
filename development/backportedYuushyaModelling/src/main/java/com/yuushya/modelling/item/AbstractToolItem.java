// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 item/AbstractToolItem.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class AbstractToolItem extends AbstractYuushyaItem {
   public AbstractToolItem(Properties properties, Integer tipLines) {
      super(properties, tipLines);
   }

   public float getDestroySpeed(ItemStack stack, BlockState state) {
      return 119.5F;
   }

   public boolean canAttackBlock(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull Player player) {
      InteractionResult result = this.inMainHandLeftClickOnBlock(player, blockState, level, blockPos, player.getItemInHand(InteractionHand.MAIN_HAND));
      if (!level.isClientSide && result.consumesAction()) {
         level.playSound(null, blockPos, SoundEvents.UI_BUTTON_CLICK, SoundSource.BLOCKS, 1.0F, 0.2F);
      }

      return false;
   }

   public InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
      BlockPos blockPos = player.blockPosition();
      ItemStack handItemStack = player.getItemInHand(hand);
      InteractionResultHolder<ItemStack> resultHolder;
      if (hand == InteractionHand.OFF_HAND) {
         resultHolder = new InteractionResultHolder(
            this.inOffHandRightClickInAir(player, level.getBlockState(blockPos), level, blockPos, handItemStack), handItemStack
         );
      } else {
         resultHolder = new InteractionResultHolder(
            this.inMainHandRightClickInAir(player, level.getBlockState(blockPos), level, blockPos, handItemStack), handItemStack
         );
      }

      if (!level.isClientSide && resultHolder.getResult().consumesAction()) {
         level.playSound(null, blockPos, SoundEvents.UI_BUTTON_CLICK, SoundSource.BLOCKS, 1.0F, 0.2F);
      }

      return resultHolder;
   }

   public InteractionResult useOn(UseOnContext useOnContext) {
      Player player = useOnContext.getPlayer();
      Level level = useOnContext.getLevel();
      InteractionHand hand = useOnContext.getHand();
      BlockPos blockPos = useOnContext.getClickedPos();
      InteractionResult result;
      if (hand == InteractionHand.OFF_HAND) {
         result = this.inOffHandRightClickOnBlock(player, level.getBlockState(blockPos), level, blockPos, useOnContext.getItemInHand());
      } else {
         result = this.inMainHandRightClickOnBlock(player, level.getBlockState(blockPos), level, blockPos, useOnContext.getItemInHand());
      }

      if (!level.isClientSide && result.consumesAction()) {
         level.playSound(null, blockPos, SoundEvents.UI_BUTTON_CLICK, SoundSource.BLOCKS, 1.0F, 0.2F);
      }

      return result;
   }

   public InteractionResult inMainHandRightClickOnBlock(Player player, BlockState blockState, Level level, BlockPos blockPos, ItemStack handItemStack) {
      return InteractionResult.PASS;
   }

   public InteractionResult inMainHandLeftClickOnBlock(Player player, BlockState blockState, Level level, BlockPos blockPos, ItemStack handItemStack) {
      return InteractionResult.PASS;
   }

   public InteractionResult inOffHandRightClickOnBlock(Player player, BlockState blockState, Level level, BlockPos blockPos, ItemStack handItemStack) {
      return InteractionResult.PASS;
   }

   public InteractionResult inMainHandRightClickInAir(Player player, BlockState blockState, Level level, BlockPos blockPos, ItemStack handItemStack) {
      return InteractionResult.PASS;
   }

   public InteractionResult inOffHandRightClickInAir(Player player, BlockState blockState, Level level, BlockPos blockPos, ItemStack handItemStack) {
      return InteractionResult.PASS;
   }
}
