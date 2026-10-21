package net.satisfy.vinery.legacy.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.vinery.core.registry.EntityTypeRegistry;
import net.satisfy.foundation.util.ShapeUtil;
import net.satisfy.foundation.util.LibUtil;

public class LegacyFlowerPotBlockEntity extends BlockEntity {
   private Item flower;
   public static final String FLOWER_KEY = "flower";

   public LegacyFlowerPotBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)EntityTypeRegistry.FLOWER_POT.get(), pos, state);
   }

   public Item getFlower() {
      return this.flower;
   }

   public void setFlower(Item flower) {
      this.flower = flower;
      this.setChanged();
   }

   public void saveAdditional(CompoundTag nbt) {
      super.saveAdditional(nbt);
      this.writeFlower(nbt, this.flower);
   }

   public void load(CompoundTag nbt) {
      super.load(nbt);
      this.flower = this.readFlower(nbt);
   }

   public void writeFlower(CompoundTag nbt, Item flower) {
      CompoundTag nbtCompound = new CompoundTag();
      if (flower != null) {
         flower.getDefaultInstance().save(nbtCompound);
      }

      nbt.put("flower", nbtCompound);
   }

   public Item readFlower(CompoundTag nbt) {
      super.load(nbt);
      if (nbt.contains("flower")) {
         CompoundTag nbtCompound = nbt.getCompound("flower");
         if (!nbtCompound.isEmpty()) {
            return ItemStack.of(nbtCompound).getItem();
         }
      }

      return null;
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public CompoundTag getUpdateTag() {
      return this.saveWithoutMetadata();
   }

   public void setChanged() {
      if (this.level != null && !this.level.isClientSide()) {
         Packet<ClientGamePacketListener> updatePacket = this.getUpdatePacket();

         for (ServerPlayer player : LibUtil.tracking((ServerLevel)this.level, this.getBlockPos())) {
            player.connection.send(updatePacket);
         }
      }

      super.setChanged();
   }
}
