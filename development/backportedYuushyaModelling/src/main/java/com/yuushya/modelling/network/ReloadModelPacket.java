// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 network/ReloadModelPacket.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.network;

import com.yuushya.modelling.command.ReloadModelCommand;
import com.yuushya.modelling.event.ClientEvent;
import com.yuushya.modelling.gui.engrave.EngraveBlockResultLoader;
import com.yuushya.modelling.gui.engrave.EngraveItemResultLoader;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class ReloadModelPacket {
   private final ReloadModelCommand.ReloadType reloadType;

   public ReloadModelPacket(ReloadModelCommand.ReloadType reloadType) {
      this.reloadType = reloadType;
   }

   public ReloadModelPacket(FriendlyByteBuf buffer) {
      this.reloadType = (ReloadModelCommand.ReloadType)buffer.readEnum(ReloadModelCommand.ReloadType.class);
   }

   public void encode(FriendlyByteBuf buffer) {
      buffer.writeEnum(this.reloadType);
   }

   public static void handle(ReloadModelPacket packet, Supplier<Context> ctx) {
      ctx.get().enqueueWork(() -> {
         Minecraft minecraft = Minecraft.getInstance();
         if (minecraft.level != null) {
            switch (packet.reloadType) {
               case BLOCKS:
                  EngraveBlockResultLoader.SHOWBLOCK_ITEM_MAP.clear();
                  EngraveBlockResultLoader.load();
                  break;
               case ITEMS:
                  EngraveItemResultLoader.ITEMBLOCK_ITEM_MAP.clear();
                  EngraveItemResultLoader.load();
                  break;
               case TEXTS:
                  // PHASE-B: EngraveTextResultLoader.TEXTBLOCK_ITEM_MAP.clear(); EngraveTextResultLoader.load();
                  break;
               case ALL:
                  ClientEvent.load();
            }
         }
      });
      ctx.get().setPacketHandled(true);
   }
}
