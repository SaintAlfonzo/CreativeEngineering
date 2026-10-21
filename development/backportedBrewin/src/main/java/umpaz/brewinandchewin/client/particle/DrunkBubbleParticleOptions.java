package umpaz.brewinandchewin.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.math.Vector3f;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraftforge.registries.ForgeRegistries;
import umpaz.brewinandchewin.common.registry.BnCParticleTypes;

// Port: 1.19.2 particle options serialize through a Deserializer + writeToNetwork, not a StreamCodec. Common class, as
// upstream (it lives in the client package there too).
public class DrunkBubbleParticleOptions implements ParticleOptions {
   public static final Codec<DrunkBubbleParticleOptions> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            Vector3f.CODEC.fieldOf("color").forGetter(DrunkBubbleParticleOptions::getColor),
            Codec.FLOAT.fieldOf("scale").forGetter(DrunkBubbleParticleOptions::getScale)
         )
         .apply(inst, DrunkBubbleParticleOptions::new)
   );
   @SuppressWarnings("deprecation")
   public static final ParticleOptions.Deserializer<DrunkBubbleParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<>() {
      @Override
      public DrunkBubbleParticleOptions fromCommand(ParticleType<DrunkBubbleParticleOptions> type, StringReader reader) throws CommandSyntaxException {
         reader.expect(' ');
         float r = reader.readFloat();
         reader.expect(' ');
         float g = reader.readFloat();
         reader.expect(' ');
         float b = reader.readFloat();
         reader.expect(' ');
         float scale = reader.readFloat();
         return new DrunkBubbleParticleOptions(new Vector3f(r, g, b), scale);
      }

      @Override
      public DrunkBubbleParticleOptions fromNetwork(ParticleType<DrunkBubbleParticleOptions> type, FriendlyByteBuf buf) {
         return new DrunkBubbleParticleOptions(new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat()), buf.readFloat());
      }
   };
   private final Vector3f color;
   private final float scale;

   public DrunkBubbleParticleOptions(Vector3f color, float scale) {
      this.color = color;
      this.scale = Mth.clamp(scale, 0.01F, 4.0F);
   }

   public Vector3f getColor() {
      return this.color;
   }

   public float getScale() {
      return this.scale;
   }

   @Override
   public ParticleType<DrunkBubbleParticleOptions> getType() {
      return BnCParticleTypes.DRUNK_BUBBLE;
   }

   @Override
   public void writeToNetwork(FriendlyByteBuf buf) {
      buf.writeFloat(this.color.x());
      buf.writeFloat(this.color.y());
      buf.writeFloat(this.color.z());
      buf.writeFloat(this.scale);
   }

   @Override
   public String writeToString() {
      return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f", ForgeRegistries.PARTICLE_TYPES.getKey(this.getType()), this.color.x(), this.color.y(),
         this.color.z(), this.scale);
   }
}
