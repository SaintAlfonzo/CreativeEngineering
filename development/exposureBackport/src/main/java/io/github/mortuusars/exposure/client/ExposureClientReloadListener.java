package io.github.mortuusars.exposure.client;

import io.github.mortuusars.exposure.event.ClientEvents;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

public class ExposureClientReloadListener extends SimplePreparableReloadListener<Boolean> {
   @NotNull
   protected Boolean prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
      return true;
   }

   protected void apply(Boolean object, ResourceManager resourceManager, ProfilerFiller profiler) {
      ClientEvents.resourcesReloaded();
   }
}
