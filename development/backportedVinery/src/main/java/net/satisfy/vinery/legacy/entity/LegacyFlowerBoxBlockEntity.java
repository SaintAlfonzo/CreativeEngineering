package net.satisfy.vinery.legacy.entity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.vinery.core.registry.EntityTypeRegistry;
import net.satisfy.foundation.util.ShapeUtil;
import net.satisfy.foundation.util.LibUtil;

public class LegacyFlowerBoxBlockEntity extends BlockEntity {
   private NonNullList<ItemStack> flowers = NonNullList.withSize(2, ItemStack.EMPTY);

   public LegacyFlowerBoxBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)EntityTypeRegistry.FLOWER_BOX.get(), pos, state);
   }

   public void saveAdditional(CompoundTag nbt) {
      ContainerHelper.saveAllItems(nbt, this.flowers);
      super.saveAdditional(nbt);
   }

   public void load(CompoundTag nbt) {
      super.load(nbt);
      this.flowers = NonNullList.withSize(3, ItemStack.EMPTY);
      ContainerHelper.loadAllItems(nbt, this.flowers);
   }

   public void addFlower(ItemStack stack, int slot) {
      this.flowers.set(slot, stack);
      this.setChanged();
   }

   public ItemStack removeFlower(int slot) {
      ItemStack stack = (ItemStack)this.flowers.set(slot, ItemStack.EMPTY);
      this.setChanged();
      return stack;
   }

   public ItemStack getFlower(int slot) {
      return (ItemStack)this.flowers.get(slot);
   }

   public boolean isSlotEmpty(int slot) {
      return slot < this.flowers.size() && ((ItemStack)this.flowers.get(slot)).isEmpty();
   }

   public Item[] getFlowers() {
      List<Item> items = new ArrayList<>();

      for (ItemStack stack : this.flowers) {
         if (!stack.isEmpty()) {
            items.add(stack.getItem());
         }
      }

      return items.toArray(new Item[0]);
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
