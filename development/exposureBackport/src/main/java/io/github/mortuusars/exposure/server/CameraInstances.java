package io.github.mortuusars.exposure.server;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.CameraId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CameraInstances {
   private static final Map<CameraId, CameraInstance> INSTANCES = new HashMap<>();

   @Nullable
   public static CameraInstance get(CameraId id) {
      return id.uuid().equals(Util.NIL_UUID) ? null : INSTANCES.get(id);
   }

   public static Optional<CameraInstance> getOptional(CameraId id) {
      return id.uuid().equals(Util.NIL_UUID) ? Optional.empty() : Optional.ofNullable(INSTANCES.get(id));
   }

   @Nullable
   public static CameraInstance get(ItemStack stack) {
      return get(CameraId.ofStack(stack));
   }

   public static Optional<CameraInstance> getOptional(ItemStack stack) {
      return getOptional(CameraId.ofStack(stack));
   }

   public static CameraInstance getOrThrow(CameraId id) {
      CameraInstance instance = get(id);
      Preconditions.checkState(instance != null, "No Camera Instance with id '%s' found.", id);
      return instance;
   }

   public static void ifPresent(CameraId id, Consumer<CameraInstance> instanceConsumer) {
      CameraInstance instance = get(id);
      if (instance != null) {
         instanceConsumer.accept(instance);
      }
   }

   public static void ifPresent(ItemStack stack, Consumer<CameraInstance> instanceConsumer) {
      CameraId id = (CameraId)Exposure.DataComponents.CAMERA_ID.get(stack);
      if (id != null) {
         ifPresent(id, instanceConsumer);
      }
   }

   public static void add(CameraId id, CameraInstance instance) {
      INSTANCES.put(id, instance);
   }

   public static void createOrUpdate(CameraId id, Consumer<CameraInstance> instanceConsumer) {
      CameraInstance instance = INSTANCES.computeIfAbsent(id, uuid -> new CameraInstance(id));
      instanceConsumer.accept(instance);
   }

   public static boolean canReleaseShutter(CameraId id) {
      return getOptional(id).map(cameraInstance -> !cameraInstance.isWaitingForProjection()).orElse(true);
   }
}
