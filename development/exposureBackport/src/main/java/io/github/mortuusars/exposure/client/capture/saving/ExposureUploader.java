package io.github.mortuusars.exposure.client.capture.saving;

import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.serverbound.ExposureDataC2SP;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import java.util.function.Consumer;
import net.minecraft.util.StringUtil;
import org.slf4j.Logger;

public class ExposureUploader {
   private static final Logger LOGGER = LogUtils.getLogger();

   public static void upload(String id, ExposureData exposure) {
      Preconditions.checkArgument(!org.apache.commons.lang3.StringUtils.isBlank(id), "Cannot upload exposure with null or empty id.");
      LOGGER.debug("Sending exposure '{}' to server...", id);
      Packets.sendToServer(new ExposureDataC2SP(id, exposure));
   }

   public static Consumer<ExposureData> upload(String id) {
      return exposure -> upload(id, exposure);
   }
}
