// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 blockentity/AbstractTransformBlockEntity.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractTransformBlockEntity extends BlockEntity {
   protected Integer slot = 0;
   protected Integer showFrame = 0;
   protected Integer showRotAxis = 0;
   protected Integer showPosAxis = 0;
   protected Integer showText = 0;
   protected Axis showAxis = null;

   public AbstractTransformBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
      super(blockEntityType, blockPos, blockState);
   }

   public int getSlot() {
      return this.slot;
   }

   public abstract void setSlot(int var1);

   public abstract void writeBlockState(ItemStack var1, BlockState var2);

   public boolean showFrame() {
      return this.showFrame > 0;
   }

   public void setShowFrame() {
      this.showFrame = 5;
   }

   public void consumeShowFrame() {
      this.showFrame = this.showFrame < 0 ? 0 : this.showFrame - 1;
   }

   public boolean showRotAxis() {
      return this.showRotAxis > 0;
   }

   public void setShowRotAxis() {
      this.showRotAxis = 5;
   }

   public boolean showPosAxis() {
      return this.showPosAxis > 0;
   }

   public void setShowPosAxis() {
      this.showPosAxis = 5;
   }

   public boolean showText() {
      return this.showText > 0;
   }

   public void setShowText() {
      this.showText = 5;
   }

   public void consumeShowAxis() {
      if (this.showRotAxis <= 0 && this.showPosAxis <= 0) {
         this.showAxis = null;
      }
   }

   public void consumeShow() {
      this.showRotAxis = this.showRotAxis < 0 ? 0 : this.showRotAxis - 1;
      this.showPosAxis = this.showPosAxis < 0 ? 0 : this.showPosAxis - 1;
      this.showText = this.showText < 0 ? 0 : this.showText - 1;
   }

   public void saveChanged() {
      this.setChanged();
      if (this.getLevel() != null && !this.getLevel().isClientSide) {
         this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 11);
      }
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this, blockEntity -> {
         CompoundTag compoundTag = this.getUpdateTag();
         this.saveAdditional(compoundTag);
         return compoundTag;
      });
   }

   @NotNull
   public CompoundTag getUpdateTag() {
      CompoundTag compoundTag = super.getUpdateTag();
      compoundTag.putByte("ControlSlot", this.slot.byteValue());
      return compoundTag;
   }

   protected void saveAdditional(@NotNull CompoundTag compoundTag) {
      super.saveAdditional(compoundTag);
      compoundTag.putByte("ControlSlot", this.slot.byteValue());
   }

   public void load(@NotNull CompoundTag compoundTag) {
      super.load(compoundTag);
      this.slot = Integer.valueOf(compoundTag.getByte("ControlSlot"));
      if (this.getLevel() != null && this.getLevel().isClientSide) {
         this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 11);
      }
   }

   public void setShowAxis(Axis showAxis) {
      this.showAxis = showAxis;
   }

   public Axis getShowAxis() {
      return this.showAxis;
   }
}
