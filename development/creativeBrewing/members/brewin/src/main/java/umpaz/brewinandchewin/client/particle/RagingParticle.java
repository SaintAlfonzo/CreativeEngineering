package umpaz.brewinandchewin.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

public class RagingParticle extends TextureSheetParticle {
   private static final RandomSource RANDOM = RandomSource.create();
   private final SpriteSet sprites;

   protected RagingParticle(ClientLevel level, double x, double y, double z, double motionX, double motionY, double motionZ, float size, SpriteSet sprites) {
      super(level, x, y, z, 0.5 - RANDOM.nextDouble(), motionY, 0.5 - RANDOM.nextDouble());
      this.friction = 0.96F;
      this.gravity = -0.1F;
      this.speedUpWhenYMotionIsBlocked = true;
      this.sprites = sprites;
      this.yd *= 0.2F;
      if (motionX == 0.0 && motionZ == 0.0) {
         this.xd *= 0.1F;
         this.zd *= 0.1F;
      }

      this.quadSize *= size;
      this.oRoll = 0.4F;
      this.roll = 0.4F;
      this.lifetime = (int)(8.0 / (Math.random() * 0.8 + 0.2));
      this.hasPhysics = false;
      this.setSpriteFromAge(sprites);
      if (this.isCloseToScopingPlayer()) {
         this.setAlpha(0.0F);
      }
   }

   @NotNull
   @Override
   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
   }

   @Override
   public void tick() {
      super.tick();
      if (this.random.nextFloat() < 0.2F) {
         this.setSprite(this.sprites.get(this.random));
      }

      this.oRoll = 0.4F;
      this.roll = 0.4F;
      if (this.isCloseToScopingPlayer()) {
         this.setAlpha(0.0F);
      } else {
         this.setAlpha(Mth.lerp(0.025F, this.alpha, 1.0F));
      }
   }

   private boolean isCloseToScopingPlayer() {
      Minecraft minecraft = Minecraft.getInstance();
      LocalPlayer localplayer = minecraft.player;
      return localplayer != null
         && localplayer.getEyePosition().distanceToSqr(this.x, this.y, this.z) <= 9.0
         && minecraft.options.getCameraType().isFirstPerson()
         && localplayer.isScoping();
   }

   public static class Factory<T extends RagingParticleOptions> implements ParticleProvider<T> {
      private final SpriteSet spriteSet;

      public Factory(SpriteSet sprite) {
         this.spriteSet = sprite;
      }

      @Override
      public Particle createParticle(T typeIn, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
         return new RagingParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, typeIn.getScale(), this.spriteSet);
      }
   }
}
