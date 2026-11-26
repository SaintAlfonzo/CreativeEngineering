// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 blockentity/showblock/ShowBlockEntity.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.blockentity.showblock;

import com.yuushya.modelling.block.blockstate.YuushyaBlockStates;
import com.yuushya.modelling.blockentity.AbstractTransformBlock;
import com.yuushya.modelling.blockentity.AbstractTransformBlockEntity;
import com.yuushya.modelling.blockentity.BlockShape;
import com.yuushya.modelling.blockentity.transformData.ITransformDataInventory;
import com.yuushya.modelling.blockentity.transformData.TransformBlockData;
import com.yuushya.modelling.gui.engrave.EngraveBlockResult;
import com.yuushya.modelling.item.showblocktool.HistoryItem;
import com.yuushya.modelling.registries.BlockEntityRegistry;
import com.yuushya.modelling.utils.ShareUtils;
import com.yuushya.modelling.utils.YuushyaDataTags;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class ShowBlockEntity extends AbstractTransformBlockEntity implements ITransformDataInventory {
   private final List<TransformBlockData> transformData;
   private Integer showFrame = 0;
   private Integer showRotAxis = 0;
   private Integer showPosAxis = 0;
   private Integer showText = 0;
   private Axis showAxis = null;

   public ShowBlockEntity(BlockPos blockPos, BlockState blockState) {
      super(BlockEntityRegistry.SHOW_BLOCK_ENTITY.get(), blockPos, blockState);
      this.transformData = new ArrayList<>();
      this.transformData.add(new TransformBlockData());
      this.slot = 0;
   }

   @NotNull
   public TransformBlockData getTransFormDataNow() {
      return this.getTransformData(this.slot);
   }

   public void removeTransFormDataNow() {
      this.removeTransformData(this.slot);
   }

   public void setTransformDataNow(TransformBlockData transformData) {
      this.setTransformData(this.slot, transformData);
   }

   public void setSlotBlockStateNow(BlockState blockState) {
      this.setSlotBlockState(this.slot, blockState);
   }

   @Override
   public void setSlot(int slot) {
      if (slot >= this.transformData.size()) {
         for (int i = slot - this.transformData.size() + 1; i > 0; i--) {
            this.transformData.add(new TransformBlockData());
         }
      }

      this.slot = slot;
   }

   @Override
   public boolean showFrame() {
      return this.showFrame > 0;
   }

   @Override
   public void setShowFrame() {
      this.showFrame = 5;
   }

   @Override
   public void consumeShowFrame() {
      this.showFrame = this.showFrame <= 0 ? 0 : this.showFrame - 1;
   }

   @Override
   public boolean showRotAxis() {
      return this.showRotAxis > 0;
   }

   @Override
   public void setShowRotAxis() {
      this.showRotAxis = 5;
   }

   @Override
   public boolean showPosAxis() {
      return this.showPosAxis > 0;
   }

   @Override
   public void setShowPosAxis() {
      this.showPosAxis = 5;
   }

   @Override
   public boolean showText() {
      return this.showText > 0;
   }

   @Override
   public void setShowText() {
      this.showText = 5;
   }

   @Override
   public Axis getShowAxis() {
      return this.showAxis;
   }

   @Override
   public void setShowAxis(Axis axis) {
      this.showAxis = axis;
   }

   @Override
   public void consumeShowAxis() {
      if (this.showRotAxis <= 0 && this.showPosAxis <= 0) {
         this.showAxis = null;
      }
   }

   @Override
   public void consumeShow() {
      this.showRotAxis = this.showRotAxis <= 0 ? 0 : this.showRotAxis - 1;
      this.showPosAxis = this.showPosAxis <= 0 ? 0 : this.showPosAxis - 1;
      this.showText = this.showText <= 0 ? 0 : this.showText - 1;
   }

   @Override
   public void load(@NotNull CompoundTag compoundTag) {
      super.load(compoundTag);
      ITransformDataInventory.load(compoundTag, this.transformData);
      this.slot = Integer.valueOf(compoundTag.getByte("ControlSlot"));
      if (this.getLevel() != null && this.getLevel().isClientSide) {
         this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 11);
      }
   }

   @Override
   protected void saveAdditional(@NotNull CompoundTag compoundTag) {
      super.saveAdditional(compoundTag);
      ITransformDataInventory.saveAdditional(compoundTag, this.transformData);
      compoundTag.putByte("ControlSlot", this.slot.byteValue());
   }

   @NotNull
   @Override
   public CompoundTag getUpdateTag() {
      CompoundTag compoundTag = super.getUpdateTag();
      ITransformDataInventory.saveAdditional(compoundTag, this.transformData);
      return compoundTag;
   }

   @Override
   public void saveChanged() {
      this.setChanged();
      if (this.getLevel() != null && !this.getLevel().isClientSide) {
         this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 11);
      }
   }

   @Override
   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @Override
   public void writeBlockState(ItemStack itemStack, BlockState blockState) {
      YuushyaDataTags.setLit(itemStack, (Integer)blockState.getValue(YuushyaBlockStates.LIT));
      YuushyaDataTags.setShapes(itemStack, ((BlockShape)blockState.getValue(YuushyaBlockStates.SHAPES)).getSerializedName());
      YuushyaDataTags.setEnableAO(itemStack, (Boolean)blockState.getValue(AbstractTransformBlock.ENABLE_AO));
   }

   public void setRemoved() {
      if (this.level != null && !this.isEmpty() && this.level.isClientSide) {
         String res = ShareUtils.transfer(this.getTransformData());
         ShareUtils.ShareBlockInformation information = ShareUtils.from(res);
         if (this.level != null) {
            String name = this.level.dimension().location() + "/" + this.getBlockPos().toShortString();
            HistoryItem.HISTORY_SHOWBLOCK_MAP.put(name, new EngraveBlockResult(name, information));
         }
      }

      super.setRemoved();
   }

   @Override
   public List<TransformBlockData> getTransformData() {
      return this.transformData;
   }
}
