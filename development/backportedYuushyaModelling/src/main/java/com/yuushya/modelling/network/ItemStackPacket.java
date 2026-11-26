// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 network/ItemStackPacket.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.network;

import com.yuushya.modelling.blockentity.itemblock.ItemBlockEntity;
import com.yuushya.modelling.blockentity.transformData.ItemTransformType;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent.Context;

public class ItemStackPacket {
   private final BlockPos blockPos;
   private final int slot;
   private final ItemStack itemStack;

   public ItemStackPacket(BlockPos blockPos, int slot, ItemStack itemStack) {
      this.blockPos = blockPos;
      this.slot = slot;
      this.itemStack = itemStack;
   }

   public ItemStackPacket(FriendlyByteBuf buffer) {
      this.blockPos = buffer.readBlockPos();
      this.slot = buffer.readVarInt();
      this.itemStack = buffer.readItem();
   }

   public void encode(FriendlyByteBuf buffer) {
      buffer.writeBlockPos(this.blockPos);
      buffer.writeVarInt(this.slot);
      buffer.writeItem(this.itemStack);
   }

   public static void handle(ItemStackPacket packet, Supplier<Context> ctx) {
      ctx.get()
         .enqueueWork(
            () -> {
               if ((Object)ctx.get().getSender().getLevel() instanceof ServerLevel serverLevel
                  && serverLevel.hasChunkAt(packet.blockPos)
                  && serverLevel.getBlockEntity(packet.blockPos) instanceof ItemBlockEntity itemBlockEntity) {
                  ItemTransformType.ITEM_STACK.modify(itemBlockEntity, packet.slot, packet.itemStack);
               }
            }
         );
      ctx.get().setPacketHandled(true);
   }

   public static void sendToServer(BlockPos blockPos, int slot, ItemStack itemStack) {
      YuushyaModellingNetwork.INSTANCE.sendToServer(new ItemStackPacket(blockPos, slot, itemStack));
   }
}
