package io.github.mortuusars.exposure.forge.integration.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.ScriptType;
import io.github.mortuusars.exposure.forge.api.event.FrameAddedEvent;
import io.github.mortuusars.exposure.forge.api.event.ModifyEntityInFrameDataEvent;
import io.github.mortuusars.exposure.forge.api.event.ModifyFrameExtraDataEvent;
import io.github.mortuusars.exposure.forge.integration.kubejs.event.ExposureJSEvents;
import io.github.mortuusars.exposure.forge.integration.kubejs.event.FrameAddedEventJS;
import io.github.mortuusars.exposure.forge.integration.kubejs.event.ModifyEntityInFrameExtraDataEventJS;
import io.github.mortuusars.exposure.forge.integration.kubejs.event.ModifyFrameExtraDataEventJS;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;

/**
 * KubeJS 1902 (6.x) plugin, listed in {@code kubejs.plugins.txt}: the {@code ExposureEvents} group re-posts
 * Exposure's Forge events to scripts. Upstream targets KubeJS 7, whose plugin is an interface; 1902's is a class.
 */
public class ExposureKubeJSPlugin extends KubeJSPlugin {
   // KubeJS 1902 reads kubejs.plugins.txt once per mod in a jar; this jar holds four mods, so it creates this
   // plugin four times. Only the first instance registers anything (otherwise every JS event fires four times).
   private static final java.util.concurrent.atomic.AtomicBoolean EVENTS_REGISTERED = new java.util.concurrent.atomic.AtomicBoolean();
   private static final java.util.concurrent.atomic.AtomicBoolean INITIALIZED = new java.util.concurrent.atomic.AtomicBoolean();

   @Override
   public void registerEvents() {
      if (EVENTS_REGISTERED.compareAndSet(false, true)) {
         ExposureJSEvents.GROUP.register();
      }
   }

   @Override
   public void init() {
      if (INITIALIZED.compareAndSet(false, true)) {
         this.subscribeToForgeEvents();
      }
   }

   private void subscribeToForgeEvents() {
      MinecraftForge.EVENT_BUS.addListener(EventPriority.LOW, this::postAddEntityInFrameDataEvent);
      MinecraftForge.EVENT_BUS.addListener(EventPriority.LOW, this::postModifyFrameDataEvent);
      MinecraftForge.EVENT_BUS.addListener(EventPriority.LOW, this::postFrameAddedEvent);
   }

   private void postAddEntityInFrameDataEvent(ModifyEntityInFrameDataEvent event) {
      ExposureJSEvents.ADD_ENTITY_IN_FRAME_DATA.post(ScriptType.SERVER, new ModifyEntityInFrameExtraDataEventJS(event));
   }

   private void postModifyFrameDataEvent(ModifyFrameExtraDataEvent event) {
      ExposureJSEvents.MODIFY_FRAME_DATA.post(ScriptType.SERVER, new ModifyFrameExtraDataEventJS(event));
   }

   private void postFrameAddedEvent(FrameAddedEvent event) {
      ExposureJSEvents.FRAME_ADDED.post(ScriptType.SERVER, new FrameAddedEventJS(event));
   }
}
