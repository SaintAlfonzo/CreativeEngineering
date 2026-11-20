// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.surface;

import com.SaintAlfonzo.creativecompat.CompatConfig;
import com.SaintAlfonzo.creativecompat.CreativeCompat;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Dust particles (plan_owner_notes_1119.md ruling 2), client: Supplementaries ash's rule mirrored to every block in
 * {@code #creativecompat:dust_particles}. A living entity in or on such a block that moved sideways this tick puffs one
 * dust particle at its feet at 1 in 2 ticks (sideways speed up to 1/12, upward 0.05); the local player landing on one
 * bursts {@code min(12, fall * 1.4)} (upward 0.12). The dust takes the block's tint, else its map color. Living
 * entities within {@link #RANGE} blocks of the camera. Switch {@code [surface.passthrough.layers] dustParticles}.
 */
@Mod.EventBusSubscriber(modid = CreativeCompat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class DustParticles {

	static final TagKey<Block> DUST = BlockTags.create(new ResourceLocation(CreativeCompat.NS, "dust_particles"));
	private static final double RANGE = 32.0;
	private static final float SIDE_SPEED = 1.0F / 12.0F;

	/** The local player's fall distance last tick (it is reset on landing, before we see it). */
	private static float lastFall;
	private static boolean lastOnGround = true;

	private DustParticles() {}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		LocalPlayer player = mc.player;
		if (level == null || player == null || mc.isPaused()) return;
		if (!CompatConfig.SPEC.isLoaded() || !CompatConfig.SURFACE.passthrough.dustParticles.get()) return;
		RandomSource random = level.random;

		AABB box = mc.gameRenderer.getMainCamera().getEntity() == null ? player.getBoundingBox().inflate(RANGE)
				: mc.gameRenderer.getMainCamera().getEntity().getBoundingBox().inflate(RANGE);
		for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box)) {
			if (e.isSpectator() || (e.xo == e.getX() && e.zo == e.getZ()) || random.nextInt(2) != 0) continue;
			BlockPos pos = dustPos(level, e);
			if (pos != null) puff(level, e, pos, level.getBlockState(pos), 0.05F, random);
		}

		boolean onGround = player.isOnGround();
		if (onGround && !lastOnGround && lastFall > 0.0F) {
			BlockPos pos = dustPos(level, player);
			if (pos != null) {
				BlockState state = level.getBlockState(pos);
				int n = (int) Math.ceil(Math.min(12.0, lastFall * 1.4));
				for (int i = 0; i < n; i++) puff(level, player, pos, state, 0.12F, random);
			}
		}
		lastOnGround = onGround;
		lastFall = player.fallDistance;
	}

	/** The dust block the entity stands in (a layer) or on (a full block), else null. */
	static BlockPos dustPos(ClientLevel level, LivingEntity e) {
		BlockPos in = e.blockPosition();
		if (level.getBlockState(in).is(DUST)) return in;
		BlockPos on = new BlockPos(e.getX(), e.getY() - 0.2, e.getZ());
		return e.isOnGround() && level.getBlockState(on).is(DUST) ? on : null;
	}

	private static void puff(ClientLevel level, LivingEntity e, BlockPos pos, BlockState state, float up, RandomSource random) {
		VoxelShape shape = state.getShape(level, pos);
		double top = pos.getY() + (shape.isEmpty() ? 0.0 : shape.max(Direction.Axis.Y));
		level.addParticle(new DustParticleOptions(color(level, pos, state), 1.0F), e.getX(), top, e.getZ(),
				Mth.randomBetween(random, -1.0F, 1.0F) * SIDE_SPEED, up, Mth.randomBetween(random, -1.0F, 1.0F) * SIDE_SPEED);
	}

	/** The block's tint at the spot, else its map color. */
	static Vector3f color(ClientLevel level, BlockPos pos, BlockState state) {
		int c = Minecraft.getInstance().getBlockColors().getColor(state, level, pos, 0);
		if (c == -1) c = state.getMapColor(level, pos).col;
		return new Vector3f((c >> 16 & 255) / 255.0F, (c >> 8 & 255) / 255.0F, (c & 255) / 255.0F);
	}
}
