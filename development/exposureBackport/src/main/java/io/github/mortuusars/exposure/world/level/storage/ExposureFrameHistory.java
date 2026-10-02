package io.github.mortuusars.exposure.world.level.storage;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import io.github.mortuusars.exposure.backport.codec.BackportCodecs;

public class ExposureFrameHistory extends SavedData {
   public static final Codec<ExposureFrameHistory> CODEC = Codec.unboundedMap(BackportCodecs.UUID_STRING, Codec.list(Frame.CODEC)).stable().xmap(ExposureFrameHistory::new, ExposureFrameHistory::getFrames);
   public static final int LIMIT = 32;
   private final Map<UUID, List<Frame>> frames;

   public ExposureFrameHistory(Map<UUID, List<Frame>> frames) {
      this.frames = new HashMap(frames);
   }

   public Map<UUID, List<Frame>> getFrames() {
      return this.frames;
   }

   public List<Frame> getFramesOf(Entity entity) {
      return this.getFramesOf(entity.getUUID());
   }

   public List<Frame> getFramesOf(UUID uuid) {
      return (List)this.frames.getOrDefault(uuid, Collections.emptyList());
   }

   public void add(Entity entity, Frame frame) {
      this.add(entity.getUUID(), frame);
   }

   public void add(UUID uuid, Frame frame) {
      List<Frame> list = (List)this.frames.compute(uuid, (id, framesList) -> framesList == null ? new ArrayList() : new ArrayList(framesList));

      while(list.size() >= 32) {
         list.remove(0);
      }

      list.add(frame);
      this.setDirty();
   }

   public void clear() {
      this.frames.clear();
   }

   public void clearOf(Entity entity) {
      this.frames.remove(entity.getUUID());
   }

   public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
      DataResult<Tag> encodingResult = CODEC.encode(this, NbtOps.INSTANCE, tag);
      if (BackportCodecs.isSuccess(encodingResult)) {
         Tag encodedTag = (Tag)BackportCodecs.getOrThrow(encodingResult);
         if (encodedTag instanceof CompoundTag) {
            return (CompoundTag)encodedTag;
         }

         Exposure.LOGGER.error("Cannot save FramesHistory: '{}'. Encoded tag is not CompoundTag but a {}", this, encodedTag.getType());
      }

      encodingResult.error().ifPresent((error) -> Exposure.LOGGER.error("Cannot save FramesHistory: {}", error.message()));
      return tag;
   }

   public static ExposureFrameHistory load(CompoundTag tag) {
      return (ExposureFrameHistory)BackportCodecs.getOrThrow(CODEC.parse(NbtOps.INSTANCE, tag));
   }

   public static @NotNull ExposureFrameHistory loadOrCreate(MinecraftServer server) {
      return (ExposureFrameHistory)server.overworld().getDataStorage().computeIfAbsent(ExposureFrameHistory::load, () -> new ExposureFrameHistory(new HashMap<>()), "exposure_frame_history");
   }
}
