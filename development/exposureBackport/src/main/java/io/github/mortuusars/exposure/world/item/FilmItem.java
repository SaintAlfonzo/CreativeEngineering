package io.github.mortuusars.exposure.world.item;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public interface FilmItem {
   ExposureType getType();

   default int getDefaultMaxFrameCount(ItemStack stack) {
      return 16;
   }

   default int getDefaultFrameSize(ItemStack stack) {
      return (Integer)Config.Server.DEFAULT_FRAME_SIZE.get();
   }

   default int getMaxFrameCount(ItemStack stack) {
      return (Integer)Exposure.DataComponents.FILM_FRAME_COUNT.getOrDefault(stack, this.getDefaultMaxFrameCount(stack));
   }

   default int getFrameSize(ItemStack stack) {
      return (Integer)Exposure.DataComponents.FILM_FRAME_SIZE.getOrDefault(stack, this.getDefaultFrameSize(stack));
   }

   default List<Frame> getStoredFrames(ItemStack stack) {
      return (List<Frame>)Exposure.DataComponents.FILM_FRAMES.getOrDefault(stack, Collections.emptyList());
   }

   default int getStoredFramesCount(ItemStack stack) {
      return this.getStoredFrames(stack).size();
   }

   default boolean hasFrames(ItemStack stack) {
      return !this.getStoredFrames(stack).isEmpty();
   }

   default boolean hasFrameAt(ItemStack stack, int index) {
      return this.getStoredFrames(stack).size() > index;
   }

   default float getFullness(ItemStack stack) {
      return (float)this.getStoredFramesCount(stack) / (float)this.getMaxFrameCount(stack);
   }

   default boolean isFull(ItemStack stack) {
      return this.getStoredFramesCount(stack) == this.getMaxFrameCount(stack);
   }
}
