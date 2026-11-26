// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 network/TransformDataListPacket.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.network;

import com.yuushya.modelling.gui.AbstractEngraveMenu;
import com.yuushya.modelling.gui.engrave.IEngraveResult;
import com.yuushya.modelling.registries.BlockEntityRegistry;
import com.yuushya.modelling.utils.YuushyaDataTags;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent.Context;

public class TransformDataListPacket {
   private final CompoundTag tag;
   public static final Set<String> SendingCache = new HashSet<>();
   private static final Map<String, ItemStack> HandlingCache = new HashMap<>();

   public TransformDataListPacket(CompoundTag tag) {
      this.tag = tag;
   }

   public TransformDataListPacket(FriendlyByteBuf buffer) {
      this.tag = buffer.readNbt();
   }

   public void encode(FriendlyByteBuf buffer) {
      buffer.writeNbt(this.tag);
   }

   public static void updateSendingCache(String name) {
      SendingCache.remove(name);
   }

   public static void sendToServerSide(IEngraveResult itemResult) {
      String name = itemResult.getName();
      CompoundTag tag;
      if (SendingCache.contains(name)) {
         tag = new CompoundTag();
      } else {
         ItemStack itemStack = itemResult.getResultItem();
         CompoundTag transformDataTag = YuushyaDataTags.getTransformData(itemStack);
         CompoundTag textDataTag = YuushyaDataTags.getTextData(itemStack);
         tag = !transformDataTag.isEmpty() ? transformDataTag : (!textDataTag.isEmpty() ? textDataTag : new CompoundTag());
      }

      tag.putString("ItemName", name);
      YuushyaModellingNetwork.INSTANCE.sendToServer(new TransformDataListPacket(tag));
   }

   public static void handle(TransformDataListPacket packet, Supplier<Context> ctx) {
      ctx.get().enqueueWork(() -> {
         Player player = ctx.get().getSender();
         if (player.containerMenu instanceof AbstractEngraveMenu menu) {
            if (!menu.stillValid(player)) {
               return;
            }

            String name = packet.tag.getString("ItemName");
            String hash = player.getStringUUID() + name;
            if (!packet.tag.contains("Blocks") && HandlingCache.containsKey(hash)) {
               menu.setupResultSlotServer(HandlingCache.get(hash));
            } else {
               String itemType = switch (menu.getBlockType()) {
                  case BLOCK -> "itemblock";
                  case ITEM -> "showblock";
                  case TEXT -> "textblock";
               };
               ItemStack itemStack = ((Item)Registry.ITEM.get(new ResourceLocation("yuushya", itemType))).getDefaultInstance();
               itemStack.setHoverName(Component.literal(name));
               CompoundTag blockEntityTag = new CompoundTag();
               CompoundTag dataTag = packet.tag.copy();
               dataTag.remove("ItemName");
               blockEntityTag.put("Blocks", dataTag.getList("Blocks", 10));
               switch (menu.getBlockType()) {
                  case BLOCK:
                     blockEntityTag.putString("id", BlockEntityRegistry.ITEM_BLOCK_ENTITY.get().toString());
                     if (packet.tag.contains("ControlSlot")) {
                        blockEntityTag.putByte("ControlSlot", packet.tag.getByte("ControlSlot"));
                     }

                     BlockItem.setBlockEntityData(itemStack, BlockEntityRegistry.ITEM_BLOCK_ENTITY.get(), blockEntityTag);
                     break;
                  case ITEM:
                     blockEntityTag.putString("id", BlockEntityRegistry.SHOW_BLOCK_ENTITY.get().toString());
                     if (packet.tag.contains("ControlSlot")) {
                        blockEntityTag.putByte("ControlSlot", packet.tag.getByte("ControlSlot"));
                     }

                     BlockItem.setBlockEntityData(itemStack, BlockEntityRegistry.SHOW_BLOCK_ENTITY.get(), blockEntityTag);
                     break;
                  case TEXT:
                     // PHASE-B: Text Block result (TEXT_BLOCK_ENTITY tag, as the two cases above).
                     break;
               }

               HandlingCache.put(hash, itemStack);
               menu.setupResultSlotServer(itemStack);
            }
         }
      });
      ctx.get().setPacketHandled(true);
   }
}
