package io.github.mortuusars.exposure.world.camera.frame;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.util.ExtraData;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public record EntityInFrame(ResourceLocation id, String name, BlockPos pos, int distance, ExtraData extraData) {
   public static Codec<EntityInFrame> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
               ResourceLocation.CODEC.fieldOf("id").forGetter(EntityInFrame::id),
               Codec.STRING.optionalFieldOf("name", "").forGetter(EntityInFrame::name),
               BlockPos.CODEC.fieldOf("pos").forGetter(EntityInFrame::pos),
               Codec.INT.optionalFieldOf("distance", Integer.MAX_VALUE).forGetter(EntityInFrame::distance),
               ExtraData.CODEC.optionalFieldOf("extra_data", ExtraData.EMPTY).forGetter(EntityInFrame::extraData)
            )
            .apply(instance, EntityInFrame::new)
   );
   public static StreamCodec<FriendlyByteBuf, EntityInFrame> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.RESOURCE_LOCATION,
      EntityInFrame::id,
      ByteBufCodecs.STRING_UTF8,
      EntityInFrame::name,
      ByteBufCodecs.BLOCK_POS,
      EntityInFrame::pos,
      ByteBufCodecs.VAR_INT,
      EntityInFrame::distance,
      ExtraData.STREAM_CODEC,
      EntityInFrame::extraData,
      EntityInFrame::new
   );

   public static EntityInFrame of(Entity cameraHolder, Entity entity) {
      return of(cameraHolder, entity, ExtraData.EMPTY);
   }

   public static EntityInFrame of(Entity cameraHolder, Entity entity, Consumer<ExtraData> data) {
      ExtraData extraData = new ExtraData();
      data.accept(extraData);
      return of(cameraHolder, entity, extraData);
   }

   public static EntityInFrame of(Entity cameraHolder, Entity entity, ExtraData extraData) {
      ResourceLocation key = EntityType.getKey(entity.getType());
      String name = entity.getName().getString();
      int distance = (int)cameraHolder.distanceTo(entity);
      return new EntityInFrame(key, name, entity.blockPosition(), distance, extraData);
   }
}
