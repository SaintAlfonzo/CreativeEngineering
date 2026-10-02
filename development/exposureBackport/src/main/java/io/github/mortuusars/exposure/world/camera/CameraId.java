package io.github.mortuusars.exposure.world.camera;

import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.Exposure;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;

public record CameraId(UUID uuid) {
   public static final Codec<CameraId> CODEC = UUIDUtil.CODEC.xmap(CameraId::new, CameraId::uuid);
   public static final StreamCodec<ByteBuf, CameraId> STREAM_CODEC = ByteBufCodecs.UUID.map(CameraId::new, CameraId::uuid);

   public static CameraId create() {
      return new CameraId(UUID.randomUUID());
   }

   public static CameraId ofStack(ItemStack stack) {
      return (CameraId)Exposure.DataComponents.CAMERA_ID.getOrDefault(stack, new CameraId(Util.NIL_UUID));
   }

   public boolean matches(ItemStack stack) {
      return this.equals(Exposure.DataComponents.CAMERA_ID.get(stack));
   }

   @Override
   public String toString() {
      return this.uuid().toString();
   }
}
