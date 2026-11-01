package net.satisfy.vinery.forge.network;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.satisfy.vinery.core.Vinery;


/** The port's channel: the vintage clock for wine tooltips (B4.5). */
public final class VineryNetwork {
    private static final String VERSION = "2"; // 2: VintageClockPacket carries coldPercent and warmPercent
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(Vinery.identifier("main"), () -> VERSION, VERSION::equals, VERSION::equals);

    private VineryNetwork() {
    }

    public static void init() {
        int id = 0;
        CHANNEL.messageBuilder(VintageClockPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(VintageClockPacket::encode)
                .decoder(VintageClockPacket::decode)
                .consumerMainThread(VintageClockPacket::handle)
                .add();
    }
}
