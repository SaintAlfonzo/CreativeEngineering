// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 network/UpdateAOPacket.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.network;

import com.yuushya.modelling.blockentity.AbstractTransformBlock;
import com.yuushya.modelling.blockentity.itemblock.ItemBlockEntity;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent.Context;

public class UpdateAOPacket {
   private final boolean enableAO;
   private final boolean fullBlock;
   private final BlockPos blockPos;

   public UpdateAOPacket(boolean enableAO, boolean fullBlock, BlockPos blockPos) {
      this.enableAO = enableAO;
      this.fullBlock = fullBlock;
      this.blockPos = blockPos;
   }

   public UpdateAOPacket(FriendlyByteBuf buffer) {
      this.enableAO = buffer.readBoolean();
      this.fullBlock = buffer.readBoolean();
      this.blockPos = buffer.readBlockPos();
   }

   public void encode(FriendlyByteBuf buffer) {
      buffer.writeBoolean(this.enableAO);
      buffer.writeBoolean(this.fullBlock);
      buffer.writeBlockPos(this.blockPos);
   }

   public static void handle(UpdateAOPacket packet, Supplier<Context> ctx) {
      ctx.get()
         .enqueueWork(
            () -> {
               Level level = ctx.get().getSender().getLevel();
               if (level instanceof ServerLevel serverLevel) {
                  BlockEntity blockEntity = serverLevel.getBlockEntity(packet.blockPos);
                  if (blockEntity instanceof ItemBlockEntity) {
                     level.setBlockAndUpdate(
                        blockEntity.getBlockPos(),
                        (BlockState)((BlockState)blockEntity.getBlockState().setValue(AbstractTransformBlock.ENABLE_AO, packet.enableAO))
                           .setValue(AbstractTransformBlock.FULL_BLOCK, packet.fullBlock)
                     );
                  }
               }
            }
         );
      ctx.get().setPacketHandled(true);
   }

   public static void sendToServer(boolean enableAO, boolean fullBlock, BlockPos blockPos) {
      YuushyaModellingNetwork.INSTANCE.sendToServer(new UpdateAOPacket(enableAO, fullBlock, blockPos));
   }
}
