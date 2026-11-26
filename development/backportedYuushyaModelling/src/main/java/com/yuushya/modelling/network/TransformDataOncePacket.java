// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 network/TransformDataOncePacket.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.network;

import com.yuushya.modelling.blockentity.showblock.ShowBlock;
import com.yuushya.modelling.blockentity.showblock.ShowBlockEntity;
import com.yuushya.modelling.blockentity.transformData.TransformType;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkEvent.Context;

public class TransformDataOncePacket {
   private final BlockPos blockPos;
   private final TransformType transformType;
   private final int slot;
   private final double number;

   public TransformDataOncePacket(BlockPos blockPos, TransformType transformType, int slot, double number) {
      this.blockPos = blockPos;
      this.transformType = transformType;
      this.slot = slot;
      this.number = number;
   }

   public TransformDataOncePacket(FriendlyByteBuf buffer) {
      this.blockPos = buffer.readBlockPos();
      this.transformType = (TransformType)buffer.readEnum(TransformType.class);
      this.slot = buffer.readVarInt();
      this.number = buffer.readDouble();
   }

   public void encode(FriendlyByteBuf buffer) {
      buffer.writeBlockPos(this.blockPos);
      buffer.writeEnum(this.transformType);
      buffer.writeVarInt(this.slot);
      buffer.writeDouble(this.number);
   }

   public static void handle(TransformDataOncePacket packet, Supplier<Context> ctx) {
      ctx.get()
         .enqueueWork(
            () -> {
               if ((Object)ctx.get().getSender().getLevel() instanceof ServerLevel serverLevel
                  && serverLevel.hasChunkAt(packet.blockPos)
                  && serverLevel.getBlockState(packet.blockPos).getBlock() instanceof ShowBlock) {
                  if (!(serverLevel.getBlockEntity(packet.blockPos) instanceof ShowBlockEntity showBlockEntity)) {
                     return;
                  }

                  packet.transformType.modify(showBlockEntity, packet.slot, packet.number);
               }
            }
         );
      ctx.get().setPacketHandled(true);
   }

   public static void sendToServerSideSuccess(BlockPos blockPos) {
      sendToServerSide(blockPos, 0, TransformType.SUCCESS, 0.0);
   }

   public static void sendToServerSide(BlockPos blockPos, int slot, TransformType type, double number) {
      YuushyaModellingNetwork.INSTANCE.sendToServer(new TransformDataOncePacket(blockPos, type, slot, number));
   }
}
