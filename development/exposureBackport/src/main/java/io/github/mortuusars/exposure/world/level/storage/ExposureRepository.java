package io.github.mortuusars.exposure.world.level.storage;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.clientbound.ExposureDataChangedS2CP;
import io.github.mortuusars.exposure.network.packet.clientbound.ExposureDataResponseS2CP;
import io.github.mortuusars.exposure.util.UnixTimestamp;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class ExposureRepository {
   public static final int EXPECTED_TIMEOUT_SECONDS = 60;
   public static final String EXPOSURES_DIRECTORY_NAME = "exposures";
   private static final Logger LOGGER = LogUtils.getLogger();
   protected final MinecraftServer server;
   protected final DimensionDataStorage dataStorage;
   protected final Path worldFolderPath;
   protected final Path exposuresFolderPath;
   protected final Map<ServerPlayer, Set<ExpectedExposure>> expectedExposures = new HashMap<>();

   public ExposureRepository(MinecraftServer server) {
      this.server = server;
      this.dataStorage = server.overworld().getDataStorage();
      this.worldFolderPath = server.getWorldPath(LevelResource.ROOT);
      this.exposuresFolderPath = this.worldFolderPath.resolve("data/exposures");
   }

   public List<String> getAllIds() {
      this.dataStorage.save();
      File folder = this.exposuresFolderPath.toFile();
      File[] filesList = folder.listFiles();
      return filesList == null
         ? Collections.emptyList()
         : Arrays.stream(filesList).filter(file -> file != null && file.isFile()).map(file -> Files.getNameWithoutExtension(file.getName())).toList();
   }

   public RequestedPalettedExposure load(@NotNull String id) {
      Preconditions.checkNotNull(id, "id");
      Preconditions.checkArgument(!org.apache.commons.lang3.StringUtils.isBlank(id), "Cannot load exposure: id is empty.");
      String name = "exposures/" + id;
      ExposureData exposureData = (ExposureData)this.dataStorage.get(ExposureData::load, name);
      if (exposureData == null) {
         File filepath = this.exposuresFolderPath.resolve(id + ".dat").toFile();
         if (!filepath.exists()) {
            LOGGER.error("Exposure '{}' was not loaded. File '{}' does not exist.", id, filepath);
            return RequestedPalettedExposure.NOT_FOUND;
         } else {
            LOGGER.error("Exposure '{}' was not loaded. Check above messages for errors.", id);
            return RequestedPalettedExposure.CANNOT_LOAD;
         }
      } else {
         return RequestedPalettedExposure.success(exposureData);
      }
   }

   public void save(@NotNull String id, ExposureData data) {
      Preconditions.checkArgument(!org.apache.commons.lang3.StringUtils.isBlank(id), "Cannot save exposure: id is null or empty.");
      if (this.ensureExposuresDirectoryExists()) {
         String saveDataName = "exposures/" + id;
         this.dataStorage.set(saveDataName, data);
         data.setDirty();
         Packets.sendToAllClients(new ExposureDataChangedS2CP(id));
      }
   }

   public void update(@NotNull String id, Function<ExposureData, ExposureData> updateFunction) {
      Preconditions.checkArgument(!org.apache.commons.lang3.StringUtils.isBlank(id), "Cannot update exposure: id is null or empty.");
      this.load(id).getData().ifPresent(exposure -> {
         ExposureData updatedExposure = updateFunction.apply(exposure);
         if (!updatedExposure.equals(exposure)) {
            this.save(id, updatedExposure);
         }
      });
   }

   public boolean delete(String id) throws IOException {
      return java.nio.file.Files.deleteIfExists(this.exposuresFolderPath.resolve(id + ".dat"));
   }

   public void expect(ServerPlayer player, String id, BiConsumer<ServerPlayer, String> onReceived) {
      Preconditions.checkArgument(!org.apache.commons.lang3.StringUtils.isBlank(id), "id cannot be null or empty.");
      Set<ExpectedExposure> exposures = this.expectedExposures.computeIfAbsent(player, pl -> new HashSet<>());
      exposures.add(new ExpectedExposure(id, UnixTimestamp.Seconds.fromNow(60), onReceived));
   }

   public void expect(ServerPlayer player, String id) {
      this.expect(player, id, (pl, i) -> {
      });
   }

   public void handleClientRequest(ServerPlayer player, String id) {
      RequestedPalettedExposure result;
      if (org.apache.commons.lang3.StringUtils.isBlank(id)) {
         LOGGER.error("Null or empty id cannot be used to get an exposure data. Player: '{}'", player.getScoreboardName());
         result = RequestedPalettedExposure.INVALID_ID;
      } else {
         result = this.load(id);
      }

      Packets.sendToClient(new ExposureDataResponseS2CP(id, result), player);
   }

   public void receiveClientUpload(ServerPlayer player, String id, ExposureData exposure) {
      if (this.validateUpload(player, id)) {
         this.save(id, exposure);
         this.onExposureReceived(player, id);
         LOGGER.debug("Saved exposure '{}' uploaded by '{}'.", id, player.getScoreboardName());
      }
   }

   public void clearExpectedExposuresTimedOutLongAgo() {
      for (Entry<ServerPlayer, Set<ExpectedExposure>> exposures : this.expectedExposures.entrySet()) {
         AtomicInteger cleared = new AtomicInteger();
         exposures.getValue().removeIf(expected -> {
            if (expected.isTimedOut(UnixTimestamp.Seconds.now() - 60L)) {
               cleared.getAndIncrement();
               return true;
            } else {
               return false;
            }
         });
         if (cleared.get() > 0) {
            LOGGER.info("Cleared {} timed out expected exposures of player: '{}'", cleared.get(), exposures.getKey().getScoreboardName());
         }
      }
   }

   protected boolean validateUpload(ServerPlayer player, String id) {
      if (org.apache.commons.lang3.StringUtils.isBlank(id)) {
         LOGGER.error("Null or empty id cannot be used to save captured exposure. Player: '{}'", player.getScoreboardName());
         return false;
      } else {
         ExpectedExposure expectedExposure = this.expectedExposures
            .getOrDefault(player, Collections.emptySet())
            .stream()
            .filter(ee -> ee.id().equals(id))
            .findFirst()
            .orElse(null);
         if (expectedExposure == null) {
            LOGGER.error("Received unexpected upload from player '{}' with ID '{}'. Discarding.", player.getScoreboardName(), id);
            return false;
         } else if (expectedExposure.isTimedOut(UnixTimestamp.Seconds.now())) {
            LOGGER.error(
               "Received expected upload from player '{}' with ID '{}' - {}seconds later than expected. Discarding.",
               new Object[]{player.getScoreboardName(), id, UnixTimestamp.Seconds.now() - expectedExposure.timeoutAt()}
            );
            return false;
         } else {
            return true;
         }
      }
   }

   protected void onExposureReceived(ServerPlayer player, String id) {
      this.expectedExposures.getOrDefault(player, Collections.emptySet()).removeIf(expectedExposure -> {
         if (expectedExposure.id().equals(id)) {
            expectedExposure.onReceived().accept(player, id);
            return true;
         } else {
            return false;
         }
      });
   }

   protected boolean ensureExposuresDirectoryExists() {
      try {
         return java.nio.file.Files.exists(this.exposuresFolderPath) || this.exposuresFolderPath.toFile().mkdirs();
      } catch (Exception var2) {
         LOGGER.error("Failed to create exposure storage directory: {}", var2.toString());
         return false;
      }
   }
}
