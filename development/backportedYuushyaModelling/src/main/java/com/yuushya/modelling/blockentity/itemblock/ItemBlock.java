// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 blockentity/itemblock/ItemBlock.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.blockentity.itemblock;

import com.yuushya.modelling.block.blockstate.YuushyaBlockStates;
import com.yuushya.modelling.blockentity.AbstractTransformBlock;
import com.yuushya.modelling.blockentity.BlockShape;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemBlock extends AbstractTransformBlock {
   public ItemBlock(Properties properties, Integer tipLines) {
      super(properties, tipLines);
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      if (level.getBlockState(pos).is(state.getBlock()) && level.getBlockEntity(pos) instanceof ItemBlockEntity itemBlockEntity) {
         if (context.isHoldingItem((Item)Registry.ITEM.get(new ResourceLocation("yuushya", "gui_item")))) {
            itemBlockEntity.setShowFrame();
         } else if (context.isHoldingItem((Item)Registry.ITEM.get(new ResourceLocation("yuushya", "rot_trans_item")))) {
            itemBlockEntity.setShowRotAxis();
            itemBlockEntity.setShowText();
         } else if (context.isHoldingItem((Item)Registry.ITEM.get(new ResourceLocation("yuushya", "pos_trans_item")))
            || context.isHoldingItem((Item)Registry.ITEM.get(new ResourceLocation("yuushya", "micro_pos_trans_item")))) {
            itemBlockEntity.setShowPosAxis();
            itemBlockEntity.setShowText();
         } else if (context.isHoldingItem((Item)Registry.ITEM.get(new ResourceLocation("yuushya", "slot_trans_item")))
            || context.isHoldingItem((Item)Registry.ITEM.get(new ResourceLocation("yuushya", "get_showblock_item")))
            || context.isHoldingItem((Item)Registry.ITEM.get(new ResourceLocation("yuushya", "move_transformdata_item")))
            || context.isHoldingItem((Item)Registry.ITEM.get(new ResourceLocation("yuushya", "get_blockstate_item")))
            || context.isHoldingItem((Item)Registry.ITEM.get(new ResourceLocation("yuushya", "scale_trans_item")))
            || context.isHoldingItem((Item)Registry.ITEM.get(new ResourceLocation("yuushya", "debug_stick_item")))
            || context.isHoldingItem((Item)Registry.ITEM.get(new ResourceLocation("yuushya", "destroy_item")))) {
            itemBlockEntity.setShowText();
         }
      }

      return super.getShape(state, level, pos, context);
   }

   @NotNull
   public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
      return ((BlockShape)blockState.getValue(YuushyaBlockStates.SHAPES)).voxelShape;
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
      return new ItemBlockEntity(blockPos, blockState);
   }

   public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
      this.spawnDestroyParticles(level, player, pos, state);
   }
}
