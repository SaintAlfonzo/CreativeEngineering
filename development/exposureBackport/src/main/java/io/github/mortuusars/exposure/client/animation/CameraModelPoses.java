package io.github.mortuusars.exposure.client.animation;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import java.util.HashMap;
import org.jetbrains.annotations.NotNull;

public class CameraModelPoses {
   public static final HashMap<CameraItem, CameraPoses> POSES = new HashMap<>();
   public static final CameraPoses DEFAULT = new CameraPoses();

   public static void register(CameraItem item, CameraPoses poses) {
      Preconditions.checkArgument(!POSES.containsKey(item), "CameraPoses for item: '" + item + "' already registered.");
      POSES.put(item, poses);
   }

   @NotNull
   public static CameraPoses get(CameraItem item) {
      return POSES.getOrDefault(item, DEFAULT);
   }
}
