package com.jkvin114.displaydelight.block;

import com.jkvin114.displaydelight.init.BlockAssociations;
import com.jkvin114.displaydelight.init.DisplayConfig;
import com.jkvin114.displaydelight.init.DisplayItems;
import com.jkvin114.displaydelight.init.DisplayProperties;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractItemBlock extends HorizontalDirectionalBlock {
   public static final BooleanProperty SUPPORT = DisplayProperties.SUPPORT;

   public AbstractItemBlock(Properties props) {
      super(props);
      this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(SUPPORT, false));
   }

   // 1.19.2: no Properties.pushReaction; 1.7.0's blocks are destroyed by pistons
   @Override
   public PushReaction getPistonPushReaction(BlockState state) {
      return PushReaction.DESTROY;
   }

   @NotNull
   @Override
   public List<ItemStack> getDrops(BlockState state, LootContext.Builder params) {
      List<ItemStack> droppedStacks = super.getDrops(state, params);
      // Port: a block with its own loot table (1.2.0's way, still used by the pack's patcherMod dishes) drops only
      // that; 1.7.0 ships none for its food blocks, so their drops come from the code below.
      if (!droppedStacks.isEmpty()) {
         return droppedStacks;
      }

      boolean silktouch = false;
      if (DisplayConfig.SILKTOUCH_DROPS_FOOD_BLOCK_ITEM.get()) {
         ItemStack tool = params.getOptionalParameter(LootContextParams.TOOL);
         silktouch = tool != null && tool.getEnchantmentLevel(Enchantments.SILK_TOUCH) > 0;
      }

      Block block = state.getBlock();
      Item foodItem = this.getStackFor().getItem();
      boolean silktouchApplied = false;
      if (block instanceof AbstractStackablePlatedFoodBlock platedFoodBlock) {
         if (foodItem.equals(Items.AIR)
            || silktouch && platedFoodBlock.getStacks(state) >= platedFoodBlock.getMaxStackable() && platedFoodBlock.hasPlate(state)) {
            foodItem = this.asItem();
            silktouchApplied = true;
         }

         droppedStacks.add(new ItemStack(foodItem, !silktouchApplied ? platedFoodBlock.getStacks(state) : 1));
         if (!silktouchApplied && platedFoodBlock.hasPlate(state)) {
            droppedStacks.add(new ItemStack((ItemLike)DisplayItems.PLATE.get()));
         }
      } else if (block instanceof SmallPlatedFoodBlock platedFoodBlock) {
         if (foodItem.equals(Items.AIR) || silktouch && platedFoodBlock.hasPlate(state)) {
            foodItem = this.asItem();
            silktouchApplied = true;
         }

         droppedStacks.add(new ItemStack(foodItem));
         if (!silktouchApplied && platedFoodBlock.hasPlate(state)) {
            droppedStacks.add(new ItemStack((ItemLike)DisplayItems.SMALL_PLATE.get()));
         }
      } else {
         if (foodItem.equals(Items.AIR) || silktouch) {
            foodItem = this.asItem();
         }

         droppedStacks.add(new ItemStack(foodItem));
      }

      return droppedStacks;
   }

   public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
      ItemStack stk = this.getStackFor();
      if (stk.getItem().equals(Items.AIR)) {
         stk = new ItemStack(state.getBlock().asItem());
      }

      return stk;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING, SUPPORT});
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Level level = context.getLevel();
      BlockPos below = context.getClickedPos().below();
      BlockState stateBelow = level.getBlockState(below);
      return (BlockState)((BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()))
         .setValue(SUPPORT, BlockHelper.needSupport(level, below, stateBelow));
   }

   @NotNull
   public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      return direction == Direction.DOWN
         ? (BlockState)state.setValue(SUPPORT, BlockHelper.needSupport(level, neighborPos, neighborState))
         : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
   }

   public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
      return true;
   }

   public ItemStack getStackFor() {
      return new ItemStack(BlockAssociations.getItemFor(this));
   }
}
