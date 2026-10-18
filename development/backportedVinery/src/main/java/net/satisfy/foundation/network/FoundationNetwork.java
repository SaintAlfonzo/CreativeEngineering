package net.satisfy.foundation.network;

import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.satisfy.foundation.Foundation;
import net.satisfy.foundation.recipe.book.PlaceRecipePacket;

/** Port: Architectury's NetworkManager receivers as one Forge channel. */
public final class FoundationNetwork {
    private static final String VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(Foundation.identifier("main"), () -> VERSION, VERSION::equals, VERSION::equals);

    private FoundationNetwork() {
    }

    public static void init() {
        int id = 0;
        CHANNEL.messageBuilder(PlaceRecipePacket.class, id++)
                .encoder(PlaceRecipePacket::encode)
                .decoder(PlaceRecipePacket::decode)
                .consumerMainThread(PlaceRecipePacket::handle)
                .add();
    }
}
