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
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import net.minecraft.network.FriendlyByteBuf;

public record UniqueSoundStopS2CP(String id, SoundEvent sound) implements Packet {
   public static final ResourceLocation ID = Exposure.resource("unique_sound_stop");
   public static final Type<UniqueSoundStopS2CP> TYPE = new Type(ID);
   public static final StreamCodec<FriendlyByteBuf, UniqueSoundStopS2CP> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8, UniqueSoundStopS2CP::id, ByteBufCodecs.SOUND_EVENT, UniqueSoundStopS2CP::sound, UniqueSoundStopS2CP::new
   );

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   @Override
   public boolean handle(PacketFlow flow, Player player) {
      UniqueSoundManager.stop(this.id, this.sound);
      return true;
   }
}
