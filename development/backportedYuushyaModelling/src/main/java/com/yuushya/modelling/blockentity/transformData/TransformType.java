// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 blockentity/transformData/TransformType.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.blockentity.transformData;

import com.yuushya.modelling.block.blockstate.YuushyaBlockStates;
import com.yuushya.modelling.blockentity.BlockShape;
import com.yuushya.modelling.blockentity.showblock.ShowBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public enum TransformType {
   POS_X(0),
   POS_Y(1),
   POS_Z(2),
   ROT_X(3),
   ROT_Y(4),
   ROT_Z(5),
   SCALE_X(6),
   SCALE_Y(7),
   SCALE_Z(8),
   BLOCK_STATE(9),
   SHOWN(10),
   LIT(11),
   REMOVE(12),
   SUCCESS(13),
   FAIL(14),
   SHAPE(15);

   public final int type;

   private TransformType(int i) {
      this.type = i;
   }

   public static TransformType from(int i) {
      return switch (i) {
         case 0 -> POS_X;
         case 1 -> POS_Y;
         case 2 -> POS_Z;
         case 3 -> ROT_X;
         case 4 -> ROT_Y;
         case 5 -> ROT_Z;
         case 6 -> SCALE_X;
         case 7 -> SCALE_Y;
         case 8 -> SCALE_Z;
         case 9 -> BLOCK_STATE;
         case 10 -> SHOWN;
         case 11 -> LIT;
         case 12 -> REMOVE;
         case 13 -> SUCCESS;
         default -> FAIL;
         case 15 -> SHAPE;
      };
   }

   public double extract(TransformBlockData transformData) {
      return switch (this) {
         case POS_X -> transformData.pos.x;
         case POS_Y -> transformData.pos.y;
         case POS_Z -> transformData.pos.z;
         case ROT_X -> transformData.rot.x();
         case ROT_Y -> transformData.rot.y();
         case ROT_Z -> transformData.rot.z();
         case SCALE_X -> transformData.scales.x();
         case SCALE_Y -> transformData.scales.y();
         case SCALE_Z -> transformData.scales.z();
         case BLOCK_STATE -> (double)Block.getId(transformData.blockState);
         case SHOWN -> transformData.isShown ? 1.0 : 0.0;
         case LIT, REMOVE, SUCCESS, SHAPE, FAIL -> 0.0;
      };
   }

   public void modify(TransformBlockData transformData, Double number) {
      switch (this) {
         case POS_X:
            transformData.pos.set(number, transformData.pos.y, transformData.pos.z);
            break;
         case POS_Y:
            transformData.pos.set(transformData.pos.x, number, transformData.pos.z);
            break;
         case POS_Z:
            transformData.pos.set(transformData.pos.x, transformData.pos.y, number);
            break;
         case ROT_X:
            transformData.rot.set(number.floatValue(), transformData.rot.y(), transformData.rot.z());
            break;
         case ROT_Y:
            transformData.rot.set(transformData.rot.x(), number.floatValue(), transformData.rot.z());
            break;
         case ROT_Z:
            transformData.rot.set(transformData.rot.x(), transformData.rot.y(), number.floatValue());
            break;
         case SCALE_X:
            transformData.scales.set(number.floatValue(), transformData.scales.y(), transformData.scales.z());
            break;
         case SCALE_Y:
            transformData.scales.set(transformData.scales.x(), number.floatValue(), transformData.scales.z());
            break;
         case SCALE_Z:
            transformData.scales.set(transformData.scales.x(), transformData.scales.y(), number.floatValue());
            break;
         case BLOCK_STATE:
            transformData.blockState = Block.stateById((int)Math.round(number));
            break;
         case SHOWN:
            transformData.isShown = number != 0.0;
         case LIT:
         case REMOVE:
         case SUCCESS:
         case SHAPE:
         case FAIL:
      }
   }

   public double extract(ShowBlockEntity showBlockEntity, int slot) {
      return this == LIT
         ? (double)((Integer)showBlockEntity.getBlockState().getValue(YuushyaBlockStates.LIT)).intValue()
         : this.extract(showBlockEntity.getTransformData(slot));
   }

   public BlockShape extractShape(ShowBlockEntity showBlockEntity) {
      return (BlockShape)showBlockEntity.getBlockState().getValue(YuushyaBlockStates.SHAPES);
   }

   public void modify(ShowBlockEntity showBlockEntity, int slot, double number) {
      if (showBlockEntity.getLevel() != null) {
         if (this == SUCCESS) {
            showBlockEntity.saveChanged();
         } else if (this == LIT) {
            Level level = showBlockEntity.getLevel();
            level.setBlock(showBlockEntity.getBlockPos(), (BlockState)showBlockEntity.getBlockState().setValue(YuushyaBlockStates.LIT, (int)Math.round(number)), 18);
         } else if (this == REMOVE) {
            showBlockEntity.removeTransformData(slot);
         } else if (this == SHAPE) {
            Level level = showBlockEntity.getLevel();
            level.setBlock(
               showBlockEntity.getBlockPos(), (BlockState)showBlockEntity.getBlockState().setValue(YuushyaBlockStates.SHAPES, BlockShape.values()[(int)number]), 18
            );
         } else {
            showBlockEntity.setSlot(slot);
            this.modify(showBlockEntity.getTransformData(slot), number);
         }
      }
   }
}
