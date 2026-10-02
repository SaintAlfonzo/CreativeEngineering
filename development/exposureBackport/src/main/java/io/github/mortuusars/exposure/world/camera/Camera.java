package io.github.mortuusars.exposure.world.camera;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.item.camera.Attachment;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

public abstract class Camera {
   private static final Logger LOGGER = LogUtils.getLogger();
   protected final CameraHolder holder;
   protected final CameraId id;

   public Camera(CameraHolder holder, CameraId id) {
      this.holder = holder;
      this.id = id;
   }

   public abstract ItemStack getItemStack();

   public abstract Packet createSyncPacket();

   public CameraHolder getHolder() {
      return this.holder;
   }

   public CameraId getId() {
      return this.id;
   }

   public void update() {
   }

   public boolean isEmpty() {
      return !(this.getItemStack().getItem() instanceof CameraItem);
   }

   public boolean isActive() {
      return this.map(CameraItem::isActive, false);
   }

   public boolean inSelfieMode() {
      return this.map(CameraItem::isInSelfieMode).orElse(false);
   }

   public boolean idMatches(CameraId id) {
      return this.id.equals(id);
   }

   public boolean isShutterOpen() {
      return this.map((item, stack) -> item.getShutter().isOpen(stack), false);
   }

   public boolean deactivate() {
      ItemStack stack = this.getItemStack();
      if (this.getId().matches(stack) && stack.getItem() instanceof CameraItem cameraItem && cameraItem.isActive(stack)) {
         cameraItem.deactivate(this.getHolder().asHolderEntity(), stack);
         return true;
      } else {
         return false;
      }
   }

   public void release() {
      this.ifPresent(
         (item, stack) -> item.release(this.getHolder(), this.getItemStack()),
         () -> LOGGER.error("Cannot take a shot: camera is not active. Camera Holder: {}", this.holder)
      );
   }

   public Camera ifPresent(BiConsumer<CameraItem, ItemStack> ifPresent) {
      ItemStack stack = this.getItemStack();
      if (stack.getItem() instanceof CameraItem item) {
         ifPresent.accept(item, stack);
      }

      return this;
   }

   public Camera ifPresent(BiConsumer<CameraItem, ItemStack> ifPresent, Runnable orElse) {
      ItemStack stack = this.getItemStack();
      if (stack.getItem() instanceof CameraItem item) {
         ifPresent.accept(item, stack);
      } else {
         orElse.run();
      }

      return this;
   }

   public <T> Optional<T> map(Function<ItemStack, T> map) {
      ItemStack stack = this.getItemStack();
      return stack.getItem() instanceof CameraItem ? Optional.ofNullable(map.apply(stack)) : Optional.empty();
   }

   public <T> T map(Function<ItemStack, T> map, T orElse) {
      ItemStack stack = this.getItemStack();
      return stack.getItem() instanceof CameraItem ? map.apply(stack) : orElse;
   }

   public <T> Optional<T> map(BiFunction<CameraItem, ItemStack, T> map) {
      ItemStack stack = this.getItemStack();
      return stack.getItem() instanceof CameraItem item ? Optional.ofNullable(map.apply(item, stack)) : Optional.empty();
   }

   public <T> T map(BiFunction<CameraItem, ItemStack, T> map, T orElse) {
      ItemStack stack = this.getItemStack();
      return stack.getItem() instanceof CameraItem item ? map.apply(item, stack) : orElse;
   }

   public <T extends Item, R> Optional<R> mapAttachment(Attachment<T> attachment, BiFunction<T, ItemStack, R> func) {
      return this.getItemStack().getItem() instanceof CameraItem ? attachment.map(this.getItemStack(), func) : Optional.empty();
   }
}
