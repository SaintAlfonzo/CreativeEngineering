package io.github.mortuusars.exposure.client;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.level.storage.RequestedExposureStatus;
import io.github.mortuusars.exposure.world.level.storage.RequestedPalettedExposure;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.NotNull;

public class ExposureStore {
   private final ExposureRequester requester = new ExposureRequester(200);
   private final Map<String, RequestedPalettedExposure> exposures = new ConcurrentHashMap<>();

   public RequestedPalettedExposure getOrRequest(@NotNull String id) {
      if (org.apache.commons.lang3.StringUtils.isBlank(id)) {
         return RequestedPalettedExposure.INVALID_ID;
      } else {
         RequestedPalettedExposure exposure = this.exposures.getOrDefault(id, RequestedPalettedExposure.NOT_REQUESTED);
         if (exposure.is(RequestedExposureStatus.SUCCESS)) {
            return exposure;
         } else if (exposure.is(RequestedExposureStatus.NOT_REQUESTED)) {
            return this.request(id);
         } else if (exposure.is(RequestedExposureStatus.AWAITED) && this.requester.isTimedOut(id)) {
            Exposure.LOGGER.info("Exposure '{}' was not received in {} seconds. Requesting again.", id, this.requester.getTimeoutSeconds());
            return this.request(id);
         } else {
            return exposure;
         }
      }
   }

   public void receive(@NotNull String id, RequestedPalettedExposure result) {
      if (!org.apache.commons.lang3.StringUtils.isBlank(id)) {
         this.exposures.put(id, result);
         this.requester.requestFulfilled(id);
         RequestedExposureStatus status = result.getStatus();
         if (status != RequestedExposureStatus.SUCCESS && status != RequestedExposureStatus.NEEDS_REFRESH) {
            Exposure.LOGGER.error("Received unsuccessful exposure '{}'. Status: {}", id, status);
         }
      }
   }

   public void refresh(String id) {
      this.exposures
         .compute(
            id,
            (identifier, exposure) -> exposure != null && exposure.is(RequestedExposureStatus.SUCCESS)
                  ? RequestedPalettedExposure.needsRefresh(exposure)
                  : null
         );
      this.requester.refresh(id);
   }

   public void clear() {
      this.exposures.clear();
      this.requester.clear();
   }

   private RequestedPalettedExposure request(String id) {
      ExposureRequester.Status requestStatus = this.requester.request(id);
      RequestedPalettedExposure requestResult = RequestedPalettedExposure.fromRequestStatus(requestStatus);
      this.exposures.put(id, requestResult);
      return requestResult;
   }
}
