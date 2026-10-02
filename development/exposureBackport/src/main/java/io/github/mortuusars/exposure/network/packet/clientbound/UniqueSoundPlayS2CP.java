package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.sound.UniqueSoundManager;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import net.minecraft.network.FriendlyByteBuf;

public record UniqueSoundPlayS2CP(String id, int entityId, SoundEvent sound, SoundSource source, float volume, float pitch) implements Packet {
   public static final ResourceLocation ID = Exposure.resource("unique_sound_play");
   public static final Type<UniqueSoundPlayS2CP> TYPE = new Type(ID);
   public static final StreamCodec<FriendlyByteBuf, UniqueSoundPlayS2CP> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8,
      UniqueSoundPlayS2CP::id,
      ByteBufCodecs.VAR_INT,
      UniqueSoundPlayS2CP::entityId,
      ByteBufCodecs.SOUND_EVENT,
      UniqueSoundPlayS2CP::sound,
      ByteBufCodecs.idMapper(i -> SoundSource.values()[i], Enum::ordinal),
      UniqueSoundPlayS2CP::source,
      ByteBufCodecs.FLOAT,
      UniqueSoundPlayS2CP::volume,
      ByteBufCodecs.FLOAT,
      UniqueSoundPlayS2CP::pitch,
      UniqueSoundPlayS2CP::new
   );

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   @Override
   public boolean handle(PacketFlow flow, Player player) {
      Entity entity = player.level.getEntity(this.entityId);
      if (entity != null) {
         UniqueSoundManager.play(this.id, entity, this.sound, this.source, this.volume, this.pitch);
      }

      return true;
   }
}
