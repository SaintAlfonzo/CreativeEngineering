// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 blockentity/transformData/ItemTransformType.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.blockentity.transformData;

import com.yuushya.modelling.block.blockstate.YuushyaBlockStates;
import com.yuushya.modelling.blockentity.BlockShape;
import com.yuushya.modelling.blockentity.itemblock.ItemBlockEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public enum ItemTransformType {
   POS_X(0),
   POS_Y(1),
   POS_Z(2),
   ROT_X(3),
   ROT_Y(4),
   ROT_Z(5),
   SCALE_X(6),
   SCALE_Y(7),
   SCALE_Z(8),
   ITEM_STACK(9),
   SHOWN(10),
   LIT(11),
   REMOVE(12),
   SUCCESS(13),
   FAIL(14),
   SHAPE(15),
   COLOR(16),
   ENABLE_BLOCK(17);

   public final int type;

   private ItemTransformType(int i) {
      this.type = i;
   }

   public double extract(TransformItemData transformData) {
      return switch (this) {
         case POS_X -> transformData.pos.x;
         case POS_Y -> transformData.pos.y;
         case POS_Z -> transformData.pos.z;
         case ROT_X -> (double)transformData.rot.x();
         case ROT_Y -> (double)transformData.rot.y();
         case ROT_Z -> (double)transformData.rot.z();
         case SCALE_X -> (double)transformData.scales.x();
         case SCALE_Y -> (double)transformData.scales.y();
         case SCALE_Z -> (double)transformData.scales.z();
         case COLOR -> (double)transformData.color;
         case ITEM_STACK -> (double)Item.getId(transformData.itemStack.getItem());
         case SHOWN -> transformData.isShown ? 1.0 : 0.0;
         case ENABLE_BLOCK -> transformData.enableBlock ? 1.0 : 0.0;
         case LIT, REMOVE, SUCCESS, SHAPE, FAIL -> 0.0;
      };
   }

   public void modify(TransformItemData transformData, Double number) {
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
         case COLOR:
            transformData.color = number.intValue();
         case ITEM_STACK:
         case LIT:
         case REMOVE:
         case SUCCESS:
         case SHAPE:
         case FAIL:
         default:
            break;
         case SHOWN:
            transformData.isShown = number != 0.0;
            break;
         case ENABLE_BLOCK:
            transformData.enableBlock = number != 0.0;
      }
   }

   public void modifyItem(TransformItemData transformData, int slot, ItemStack stack) {
      transformData.itemStack = stack;
   }

   public double extract(ItemBlockEntity itemBlockEntity, int slot) {
      return this == LIT
         ? (double)((Integer)itemBlockEntity.getBlockState().getValue(YuushyaBlockStates.LIT)).intValue()
         : this.extract(itemBlockEntity.getTransformData(slot));
   }

   public BlockShape extractShape(ItemBlockEntity itemBlockEntity) {
      return (BlockShape)itemBlockEntity.getBlockState().getValue(YuushyaBlockStates.SHAPES);
   }

   public void modify(ItemBlockEntity itemBlockEntity, int slot, double number) {
      if (itemBlockEntity.getLevel() != null) {
         if (this == SUCCESS) {
            itemBlockEntity.saveChanged();
         } else if (this == LIT) {
            Level level = itemBlockEntity.getLevel();
            level.setBlock(itemBlockEntity.getBlockPos(), (BlockState)itemBlockEntity.getBlockState().setValue(YuushyaBlockStates.LIT, (int)Math.round(number)), 18);
         } else if (this == REMOVE) {
            itemBlockEntity.removeTransformData(slot);
         } else if (this == SHAPE) {
            Level level = itemBlockEntity.getLevel();
            level.setBlock(
               itemBlockEntity.getBlockPos(), (BlockState)itemBlockEntity.getBlockState().setValue(YuushyaBlockStates.SHAPES, BlockShape.values()[(int)number]), 18
            );
         } else {
            itemBlockEntity.setSlot(slot);
            this.modify(itemBlockEntity.getTransformData(slot), number);
         }
      }
   }

   public void modify(ItemBlockEntity itemBlockEntity, int slot, ItemStack itemStack) {
      if (itemBlockEntity.getLevel() != null) {
         itemBlockEntity.setSlot(slot);
         this.modifyItem(itemBlockEntity.getTransformData(slot), slot, itemStack);
      }
   }
}
