package com.SaintAlfonzo.creativedelight.farmers.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/** The Gleaming Salad's sparkle (1.3.4 SparkleParticle): a full-bright 4-frame flash. */
public class SparkleParticle extends TextureSheetParticle {

	private final SpriteSet sprites;

	protected SparkleParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
		super(level, x, y, z, 0.0, 0.0, 0.0);
		this.lifetime = 4;
		this.quadSize *= 0.75F;
		this.sprites = sprites;
		this.setSpriteFromAge(sprites);
	}

	@Override
	public int getLightColor(float partialTick) {
		return 15728880;
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if (this.age++ >= this.lifetime) {
			this.remove();
		} else {
			this.setSpriteFromAge(this.sprites);
		}
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}

	public static class Factory implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public Factory(SpriteSet sprites) {
			this.sprites = sprites;
		}

		@Override
		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			return new SparkleParticle(level, x, y, z, this.sprites);
		}
	}
}
