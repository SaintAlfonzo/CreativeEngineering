package io.github.mortuusars.exposure.world.camera.frame;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import io.github.mortuusars.exposure.backport.codec.BackportCodecs;

public final class Photographer {
   public static final Photographer EMPTY = new Photographer("", Util.NIL_UUID);
   public static final Codec<Photographer> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
               Codec.STRING.optionalFieldOf("name", "").forGetter(Photographer::name),
               BackportCodecs.UUID_LENIENT.optionalFieldOf("uuid", Util.NIL_UUID).forGetter(Photographer::uuid)
            )
            .apply(instance, Photographer::new)
   );
   public static final StreamCodec<ByteBuf, Photographer> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8, Photographer::name, ByteBufCodecs.UUID, Photographer::uuid, Photographer::new
   );
   private final String name;
   private final UUID uuid;

   private Photographer(String name, UUID uuid) {
      this.name = name;
      this.uuid = uuid;
   }

   public Photographer(CameraHolder cameraHolder) {
      Entity owner = cameraHolder.getExposureAuthorEntity();
      this.name = owner instanceof Player ? owner.getScoreboardName() : EntityType.getKey(owner.getType()).toString();
      this.uuid = owner instanceof Player ? owner.getUUID() : Util.NIL_UUID;
   }

   public boolean matches(Entity entity) {
      return this.uuid.equals(entity.getUUID());
   }

   public boolean isPlayer() {
      return !org.apache.commons.lang3.StringUtils.isBlank(this.name) && !this.uuid.equals(Util.NIL_UUID);
   }

   public boolean isNPC() {
      return !org.apache.commons.lang3.StringUtils.isBlank(this.name) && this.uuid.equals(Util.NIL_UUID);
   }

   public boolean isEmpty() {
      return this.equals(EMPTY);
   }

   public String name() {
      return this.name;
   }

   public UUID uuid() {
      return this.uuid;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj != null && obj.getClass() == this.getClass()) {
         Photographer that = (Photographer)obj;
         return Objects.equals(this.name, that.name) && Objects.equals(this.uuid, that.uuid);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.name, this.uuid);
   }

   @Override
   public String toString() {
      return "Photographer[name=" + this.name + ", uuid=" + this.uuid + "]";
   }
}
