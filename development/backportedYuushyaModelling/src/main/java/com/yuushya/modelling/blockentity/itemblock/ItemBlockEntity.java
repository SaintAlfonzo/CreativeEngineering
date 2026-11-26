// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 blockentity/itemblock/ItemBlockEntity.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.blockentity.itemblock;

import com.yuushya.modelling.block.blockstate.YuushyaBlockStates;
import com.yuushya.modelling.blockentity.AbstractTransformBlock;
import com.yuushya.modelling.blockentity.AbstractTransformBlockEntity;
import com.yuushya.modelling.blockentity.BlockShape;
import com.yuushya.modelling.blockentity.transformData.ITransformItemDataInventory;
import com.yuushya.modelling.blockentity.transformData.TransformItemData;
import com.yuushya.modelling.gui.engrave.EngraveItemResult;
import com.yuushya.modelling.item.showblocktool.HistoryItem;
import com.yuushya.modelling.registries.BlockEntityRegistry;
import com.yuushya.modelling.utils.CustomRenderInstance;
import com.yuushya.modelling.utils.ShareUtils;
import com.yuushya.modelling.utils.YuushyaDataTags;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemBlockEntity extends AbstractTransformBlockEntity implements ITransformItemDataInventory {
   private final List<TransformItemData> transformData = new ArrayList<>();

   public ItemBlockEntity(BlockPos blockPos, BlockState blockState) {
      super(BlockEntityRegistry.ITEM_BLOCK_ENTITY.get(), blockPos, blockState);
      this.transformData.add(new TransformItemData());
      this.slot = 0;
   }

   @NotNull
   public TransformItemData getTransFormDataNow() {
      return this.getTransformData(this.slot);
   }

   @Override
   public void setSlot(int slot) {
      if (slot >= this.transformData.size()) {
         for (int i = slot - this.transformData.size() + 1; i > 0; i--) {
            this.transformData.add(new TransformItemData());
         }
      }

      this.slot = slot;
   }

   @Override
   public void load(@NotNull CompoundTag compoundTag) {
      super.load(compoundTag);
      ITransformItemDataInventory.load(compoundTag, this.transformData);
   }

   @Override
   protected void saveAdditional(@NotNull CompoundTag compoundTag) {
      super.saveAdditional(compoundTag);
      ITransformItemDataInventory.saveAdditional(compoundTag, this.transformData);
   }

   @Nullable
   @Override
   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @NotNull
   @Override
   public CompoundTag getUpdateTag() {
      return this.saveWithoutMetadata();
   }

   @Override
   public void writeBlockState(ItemStack itemStack, BlockState blockState) {
      YuushyaDataTags.setLit(itemStack, (Integer)blockState.getValue(YuushyaBlockStates.LIT));
      YuushyaDataTags.setShapes(itemStack, ((BlockShape)blockState.getValue(YuushyaBlockStates.SHAPES)).getSerializedName());
      YuushyaDataTags.setEnableAO(itemStack, (Boolean)blockState.getValue(AbstractTransformBlock.ENABLE_AO));
   }

   public void setRemoved() {
      if (this.level != null && this.level.isClientSide) {
         CustomRenderInstance.getINSTANCE().dirty = true;
         if (!this.isEmpty()) {
            String res = ShareUtils.transferItems(this.getTransformData());
            ShareUtils.ShareItemInformation information = ShareUtils.fromItems(res);
            if (this.level != null) {
               String name = this.level.dimension().location() + "/" + this.getBlockPos().toShortString();
               HistoryItem.HISTORY_ITEMBLOCK_ITEM_MAP.put(name, new EngraveItemResult(name, information));
            }
         }
      }

      super.setRemoved();
   }

   @Override
   public List<TransformItemData> getTransformData() {
      return this.transformData;
   }
}
