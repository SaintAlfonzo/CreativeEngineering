package umpaz.brewinandchewin.forge.network;

import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import umpaz.brewinandchewin.common.network.serverbound.JEITransferKegRecipeServerboundPacket;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.network.BnCPacket;
import umpaz.brewinandchewin.common.network.clientbound.ClearKegFluidContainerComponentsClientboundPacket;
import umpaz.brewinandchewin.common.network.clientbound.MakeNextPlayerChatTipsyClientboundPacket;
import umpaz.brewinandchewin.common.network.clientbound.SyncNumbedHeartsClientboundPacket;
import umpaz.brewinandchewin.common.network.clientbound.SyncRagingStacksClientboundPacket;

/** Port: upstream's payload registrar ("3") -> one SimpleChannel. Clients and servers must run the same protocol. */
public class BnCNetwork {
   private static final String PROTOCOL = "4.5.0-2"; // -2: JEI keg transfer packet
   public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(BrewinAndChewin.asResource("main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);
   private static int index = 0;

   public static void register() {
      clientbound(ClearKegFluidContainerComponentsClientboundPacket.class, ClearKegFluidContainerComponentsClientboundPacket::new);
      clientbound(MakeNextPlayerChatTipsyClientboundPacket.class, MakeNextPlayerChatTipsyClientboundPacket::new);
      clientbound(SyncNumbedHeartsClientboundPacket.class, SyncNumbedHeartsClientboundPacket::new);
      clientbound(SyncRagingStacksClientboundPacket.class, SyncRagingStacksClientboundPacket::new);
      serverbound(JEITransferKegRecipeServerboundPacket.class, JEITransferKegRecipeServerboundPacket::new);
   }

   private static <T extends BnCPacket> void clientbound(Class<T> type, Function<FriendlyByteBuf, T> decoder) {
      CHANNEL.messageBuilder(type, index++, NetworkDirection.PLAY_TO_CLIENT)
         .encoder(BnCPacket::encode)
         .decoder(decoder)
         .consumerMainThread(BnCNetwork::handle)
         .add();
   }

   private static <T extends BnCPacket> void serverbound(Class<T> type, Function<FriendlyByteBuf, T> decoder) {
      CHANNEL.messageBuilder(type, index++, NetworkDirection.PLAY_TO_SERVER)
         .encoder(BnCPacket::encode)
         .decoder(decoder)
         .consumerMainThread(BnCNetwork::handle)
         .add();
   }

   private static <T extends BnCPacket> void handle(T packet, Supplier<NetworkEvent.Context> context) {
      packet.handle(context.get().getSender());
   }
}
