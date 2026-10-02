package io.github.mortuusars.exposure.world.level.storage;

import java.util.function.BiConsumer;
import net.minecraft.server.level.ServerPlayer;

public record ExpectedExposure(String id, long timeoutAt, BiConsumer<ServerPlayer, String> onReceived) {
   public boolean isTimedOut(long currentUnixTimeSeconds) {
      return currentUnixTimeSeconds > this.timeoutAt;
   }
}
