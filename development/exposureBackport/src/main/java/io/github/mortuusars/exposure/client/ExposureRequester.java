package io.github.mortuusars.exposure.client;

import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.serverbound.ExposureRequestC2SP;
import java.util.HashMap;
import java.util.Map;

public class ExposureRequester {
   public static final int TIMEOUT = 200;
   protected final Map<String, Long> requestedExposures = new HashMap<>();
   protected final int timeout;

   public ExposureRequester(int timeoutTicks) {
      this.timeout = timeoutTicks;
   }

   public ExposureRequester.Status request(String id) {
      long time = this.requestExposure(id);
      this.requestedExposures.put(id, time);
      return ExposureRequester.Status.AWAITING;
   }

   public void requestFulfilled(String id) {
      this.requestedExposures.remove(id);
   }

   public void refresh(String id) {
      this.requestedExposures.remove(id);
   }

   public boolean isTimedOut(String id) {
      Long requestedAt = this.requestedExposures.get(id);
      return requestedAt != null && this.isTimedOut(requestedAt);
   }

   public int getTimeoutSeconds() {
      return this.timeout / 20;
   }

   private boolean isTimedOut(Long time) {
      return this.getGameTime() - time > 200L;
   }

   private long requestExposure(String id) {
      Packets.sendToServer(new ExposureRequestC2SP(id));
      return this.getGameTime();
   }

   private long getGameTime() {
      return Minecrft.level().getGameTime();
   }

   public void clear() {
      this.requestedExposures.clear();
   }

   public static enum Status {
      AWAITING,
      TIMED_OUT;
   }
}
