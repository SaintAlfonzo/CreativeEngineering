// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 item/YuushyaDebugStickItem.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.item;

import com.yuushya.modelling.block.blockstate.YuushyaBlockStates;
import com.yuushya.modelling.blockentity.showblock.ShowBlock;
import com.yuushya.modelling.blockentity.showblock.ShowBlockEntity;
import com.yuushya.modelling.utils.YuushyaUtils;
import java.util.Collection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public class YuushyaDebugStickItem extends AbstractToolItem {
   public YuushyaDebugStickItem(Properties properties, Integer tipLines) {
      super(properties, tipLines);
   }

   public static <T extends Comparable<T>> String getNameHelper(BlockState blockState, Property<T> property) {
      return property.getName(blockState.getValue(property));
   }

   @Override
   public InteractionResult inMainHandRightClickOnBlock(Player player, BlockState blockState, Level level, BlockPos blockPos, ItemStack handItemStack) {
      return !this.handleInteraction(player, level.getBlockState(blockPos), level, blockPos, true, handItemStack)
         ? InteractionResult.FAIL
         : InteractionResult.SUCCESS;
   }

   @Override
   public InteractionResult inMainHandLeftClickOnBlock(Player player, BlockState blockState, Level level, BlockPos blockPos, ItemStack handItemStack) {
      this.handleInteraction(player, blockState, level, blockPos, false, player.getItemInHand(InteractionHand.MAIN_HAND));
      return InteractionResult.PASS;
   }

   private boolean handleInteraction(
      Player player, BlockState stateClicked, LevelAccessor accessor, BlockPos pos, boolean shouldCycleState, ItemStack debugStack
   ) {
      if (!player.canUseGameMasterBlocks()) {
         return false;
      } else {
         boolean isShowBlock = stateClicked.getBlock() instanceof ShowBlock;
         stateClicked = YuushyaUtils.getBlockState(stateClicked, accessor, pos);
         Holder<Block> holder = stateClicked.getBlockHolder();
         StateDefinition<Block, BlockState> stateDefinition = ((Block)holder.value()).getStateDefinition();
         Collection<Property<?>> collection = stateDefinition.getProperties();
         if (collection.isEmpty()) {
            player.displayClientMessage(Component.translatable(this.getDescriptionId() + ".empty", new Object[]{((Block)holder.value()).getDescriptionId()}), true);
            return false;
         } else {
            CompoundTag tag = debugStack.getOrCreateTag();
            Property<?> property = null;
            if (tag.contains("DebugState")) {
               CompoundTag debugState = tag.getCompound("DebugState");
               Block block = NbtUtils.readBlockState(debugState.getCompound("Block")).getBlock();
               if (block == holder.value()) {
                  String propertyName = debugState.getString("Property");
                  property = stateDefinition.getProperty(propertyName);
               }
            }

            if (shouldCycleState) {
               if (property == null) {
                  property = collection.iterator().next();
               }

               BlockState blockStateNew = YuushyaBlockStates.cycleState(stateClicked, property, player.isSecondaryUseActive());
               if (isShowBlock) {
                  ShowBlockEntity showBlockEntity = (ShowBlockEntity)accessor.getBlockEntity(pos);
                  showBlockEntity.setSlotBlockState(0, blockStateNew);
                  showBlockEntity.saveChanged();
               } else {
                  accessor.setBlock(pos, blockStateNew, 18);
               }

               player.displayClientMessage(Component.translatable(this.getDescriptionId() + ".update", new Object[]{property.getName(), getNameHelper(blockStateNew, property)}), true);
            } else {
               property = YuushyaBlockStates.getRelative(collection, property, player.isSecondaryUseActive());
               CompoundTag debugState = new CompoundTag();
               debugState.put("Block", NbtUtils.writeBlockState(stateClicked));
               debugState.putString("Property", property.getName());
               tag.put("DebugState", debugState);
               player.displayClientMessage(Component.translatable(this.getDescriptionId() + ".select", new Object[]{property.getName(), getNameHelper(stateClicked, property)}), true);
            }

            return true;
         }
      }
   }
}
