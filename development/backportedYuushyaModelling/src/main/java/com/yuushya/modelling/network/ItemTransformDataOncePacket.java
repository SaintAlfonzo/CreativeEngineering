// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 network/ItemTransformDataOncePacket.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.network;

import com.yuushya.modelling.blockentity.itemblock.ItemBlock;
import com.yuushya.modelling.blockentity.itemblock.ItemBlockEntity;
import com.yuushya.modelling.blockentity.transformData.ItemTransformType;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkEvent.Context;

public class ItemTransformDataOncePacket {
   private final BlockPos blockPos;
   private final ItemTransformType transformType;
   private final int slot;
   private final double number;

   public ItemTransformDataOncePacket(BlockPos blockPos, ItemTransformType transformType, int slot, double number) {
      this.blockPos = blockPos;
      this.transformType = transformType;
      this.slot = slot;
      this.number = number;
   }

   public ItemTransformDataOncePacket(FriendlyByteBuf buffer) {
      this.blockPos = buffer.readBlockPos();
      this.transformType = (ItemTransformType)buffer.readEnum(ItemTransformType.class);
      this.slot = buffer.readVarInt();
      this.number = buffer.readDouble();
   }

   public void encode(FriendlyByteBuf buffer) {
      buffer.writeBlockPos(this.blockPos);
      buffer.writeEnum(this.transformType);
      buffer.writeVarInt(this.slot);
      buffer.writeDouble(this.number);
   }

   public static void sendToServerSideSuccess(BlockPos blockPos) {
      sendToServerSide(blockPos, 0, ItemTransformType.SUCCESS, 0.0);
   }

   public static void sendToServerSide(BlockPos blockPos, int slot, ItemTransformType type, double number) {
      YuushyaModellingNetwork.INSTANCE.sendToServer(new ItemTransformDataOncePacket(blockPos, type, slot, number));
   }

   public static void handle(ItemTransformDataOncePacket packet, Supplier<Context> ctx) {
      ctx.get()
         .enqueueWork(
            () -> {
               if ((Object)ctx.get().getSender().getLevel() instanceof ServerLevel serverLevel
                  && serverLevel.hasChunkAt(packet.blockPos)
                  && serverLevel.getBlockState(packet.blockPos).getBlock() instanceof ItemBlock) {
                  if (!(serverLevel.getBlockEntity(packet.blockPos) instanceof ItemBlockEntity itemBlockEntity)) {
                     return;
                  }

                  packet.transformType.modify(itemBlockEntity, packet.slot, packet.number);
               }
            }
         );
      ctx.get().setPacketHandled(true);
   }
}
