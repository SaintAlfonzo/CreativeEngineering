package com.SaintAlfonzo.creativecommon.trader.client;

import com.SaintAlfonzo.creativecommon.trader.TradeSync;

import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Client half of {@link TradeSync}: puts the client's own trade tables back when it leaves a server. */
public final class TradeSyncClient {

    private TradeSyncClient() {
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        TradeSync.restoreClient();
    }
}
