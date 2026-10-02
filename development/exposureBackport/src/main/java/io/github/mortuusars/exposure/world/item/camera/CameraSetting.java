package io.github.mortuusars.exposure.world.item.camera;

import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.serverbound.ActiveCameraSetSettingC2SP;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.sound.Sound;
import io.github.mortuusars.exposure.world.sound.SoundEffect;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.Optional;
import net.minecraft.core.RegistryAccess;
import io.github.mortuusars.exposure.backport.component.DataComponentType;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;

public record CameraSetting<T>(DataComponentType<T> component, T defaultValue, Optional<SoundEffect> sound) {
   public static final Codec<CameraSetting<?>> CODEC = ResourceLocation.CODEC.xmap(CameraSettings::byId, CameraSettings::idOf);
   public static final StreamCodec<ByteBuf, CameraSetting<?>> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.RESOURCE_LOCATION, CameraSettings::idOf, CameraSettings::byId
   );

   public CameraSetting(DataComponentType<T> component, T defaultValue, SoundEffect sound) {
      this(component, defaultValue, Optional.ofNullable(sound));
   }

   public CameraSetting(DataComponentType<T> component, T defaultValue) {
      this(component, defaultValue, Optional.empty());
   }

   @Nullable
   public T get(ItemStack stack) {
      return (T)this.component.get(stack);
   }

   public Optional<T> getOptional(ItemStack stack) {
      return Optional.ofNullable(this.get(stack));
   }

   public T getOrDefault(ItemStack stack) {
      return (T)this.component.getOrDefault(stack, this.defaultValue);
   }

   public T getOrElse(ItemStack stack, T defaultValue) {
      return (T)this.component.getOrDefault(stack, defaultValue);
   }

   @Nullable
   public T get(Camera camera) {
      return (T)this.component.get(camera.getItemStack());
   }

   public Optional<T> getOptional(Camera camera) {
      return Optional.ofNullable(this.get(camera));
   }

   public T getOrDefault(Camera camera) {
      return (T)this.component.getOrDefault(camera.getItemStack(), this.defaultValue);
   }

   public T getOrElse(Camera camera, T defaultValue) {
      return (T)this.component.getOrDefault(camera.getItemStack(), defaultValue);
   }

   public boolean set(ItemStack stack, T value) {
      if (!stack.isEmpty() && !this.getOrDefault(stack).equals(value)) {
         if (value instanceof Boolean bool && !bool) {
            this.component.remove(stack);
            return true;
         }

         this.component.set(stack, value);
         return true;
      } else {
         return false;
      }
   }

   public boolean set(CameraHolder holder, ItemStack stack, T value) {
      if (stack.getItem() instanceof CameraItem cameraItem && this.set(stack, value)) {
         cameraItem.actionPerformed(stack, holder);
         this.sound
            .ifPresent(
               sound -> Sound.playSided(
                     holder.asHolderEntity(), sound.sound().get(), SoundSource.PLAYERS, sound.volume(), sound.pitch(), sound.pitchVariability()
                  )
            );
         return true;
      }

      return false;
   }

   public boolean set(Camera camera, T value) {
      return camera.<Boolean>map((item, stack) -> this.set(camera.getHolder(), stack, value)).orElse(false);
   }

   public boolean setAndSync(Camera camera, T value) {
      return camera.<Boolean>map((item, stack) -> {
         if (this.set(camera.getHolder(), stack, value)) {
            byte[] bytes = this.encodeValue(camera.getHolder().asHolderEntity().level.registryAccess(), value);
            Packets.sendToServer(new ActiveCameraSetSettingC2SP(this, bytes));
            return true;
         } else {
            return false;
         }
      }).orElse(false);
   }

   public boolean decodeAndSet(ItemStack stack, RegistryAccess registryAccess, byte[] bytes) {
      T value = this.decodeValue(registryAccess, bytes);
      return this.set(stack, value);
   }

   public boolean decodeAndSet(CameraHolder holder, ItemStack stack, RegistryAccess registryAccess, byte[] bytes) {
      T value = this.decodeValue(registryAccess, bytes);
      return this.set(holder, stack, value);
   }

   public byte[] encodeValue(RegistryAccess registryAccess, T value) {
      FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

      byte[] var4;
      try {
         this.component.streamCodec().encode(buffer, value);
         var4 = (byte[])buffer.array().clone();
      } finally {
         buffer.release();
      }

      return var4;
   }

   public T decodeValue(RegistryAccess registryAccess, byte[] bytes) {
      FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

      Object var4;
      try {
         buffer.writeBytes(bytes);
         var4 = this.component.streamCodec().decode(buffer);
      } finally {
         buffer.release();
      }

      return (T)var4;
   }
}
