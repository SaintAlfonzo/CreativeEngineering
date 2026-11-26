// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 network/YuushyaModellingNetwork.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry.ChannelBuilder;
import net.minecraftforge.network.simple.SimpleChannel;

public class YuushyaModellingNetwork {
   // Bumped from upstream's "1" (2.2.0 and 2.4.1 both use "1" with other packet sets), so a 2.2.0 client or server is
   // refused at login instead of desyncing on packet ids (plan_yuushya_backport.md A2).
   private static final String PROTOCOL_VERSION = "2.4.1-ce-1";
   public static final SimpleChannel INSTANCE = ChannelBuilder.named(new ResourceLocation("yuushya_modelling", "main"))
      .networkProtocolVersion(() -> PROTOCOL_VERSION)
      .clientAcceptedVersions(PROTOCOL_VERSION::equals)
      .serverAcceptedVersions(PROTOCOL_VERSION::equals)
      .simpleChannel();

   public static void register() {
      int id = 0;
      INSTANCE.messageBuilder(TransformDataOncePacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
         .encoder(TransformDataOncePacket::encode)
         .decoder(TransformDataOncePacket::new)
         .consumerMainThread(TransformDataOncePacket::handle)
         .add();
      INSTANCE.messageBuilder(ItemTransformDataOncePacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
         .encoder(ItemTransformDataOncePacket::encode)
         .decoder(ItemTransformDataOncePacket::new)
         .consumerMainThread(ItemTransformDataOncePacket::handle)
         .add();
      // PHASE-B: TextTransformDataOncePacket and TextLinesPacket take ids 2 and 3 here (upstream order); until then the
      // ids are skipped so the other packets keep their upstream ids.
      id += 2;
      INSTANCE.messageBuilder(TransformDataListPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
         .encoder(TransformDataListPacket::encode)
         .decoder(TransformDataListPacket::new)
         .consumerMainThread(TransformDataListPacket::handle)
         .add();
      INSTANCE.messageBuilder(ItemStackPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
         .encoder(ItemStackPacket::encode)
         .decoder(ItemStackPacket::new)
         .consumerMainThread(ItemStackPacket::handle)
         .add();
      INSTANCE.messageBuilder(ReloadModelPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
         .encoder(ReloadModelPacket::encode)
         .decoder(ReloadModelPacket::new)
         .consumerMainThread(ReloadModelPacket::handle)
         .add();
      INSTANCE.messageBuilder(UpdateAOPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
         .encoder(UpdateAOPacket::encode)
         .decoder(UpdateAOPacket::new)
         .consumerMainThread(UpdateAOPacket::handle)
         .add();
   }
}
