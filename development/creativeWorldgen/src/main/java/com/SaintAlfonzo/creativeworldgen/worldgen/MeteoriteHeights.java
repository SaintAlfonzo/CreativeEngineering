package com.SaintAlfonzo.creativeworldgen.worldgen;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Height of a free-standing AE2 meteorite (plan_worldgen_edit C6, owner 2026-11-02: free-standing meteorites only;
 * the meteorite metas keep surface or anchor placement). AE2 sets the sphere just under the surface and carves a
 * crater. Here a start keeps that with {@link #SURFACE}; otherwise it is moved underground (anywhere between the
 * world floor and {@link #CLEAR} blocks under the ground) or into the air ({@link #AIR_MIN} to {@link #AIR_MAX} blocks
 * over the ground). A moved meteorite gets crater type NONE (no crater, fallout or crater lake: the sphere replaces
 * what it overlaps), since AE2's crater clears everything above its bowl up to y 255.
 *
 * The piece is rebuilt through its own save tag (AE2 12.9 keys: c centre, r radius, t crater type ordinal, p pure
 * crater, l lake), so no AE2 class is linked here. The roll is seeded by the world seed and the start's chunk.
 */
public final class MeteoriteHeights {

	private static final Logger LOG = LogManager.getLogger("creativeengineering/meteorites");
	private static final String PIECE = "appeng.worldgen.meteorite.MeteoriteStructurePiece";
	private static final int SALT = 0x6d657465;   // "mete"

	/** Share of free-standing meteorites left at AE2's surface spot; the rest split evenly underground / air. */
	static final float SURFACE = 0.4f;
	static final int CLEAR = 8, AIR_MIN = 16, AIR_MAX = 96;

	/** Set while MetaStructure builds a part: such a meteorite keeps AE2's height. */
	private static final ThreadLocal<Integer> IN_META = ThreadLocal.withInitial(() -> 0);

	private MeteoriteHeights() {}

	static void enterMeta() { IN_META.set(IN_META.get() + 1); }
	static void exitMeta() { IN_META.set(Math.max(0, IN_META.get() - 1)); }

	public static void apply(StructurePiecesBuilder builder, Structure.GenerationContext ctx) {
		if (IN_META.get() > 0 || builder.isEmpty()) return;
		List<StructurePiece> pieces = builder.build().pieces();
		if (pieces.stream().noneMatch(p -> PIECE.equals(p.getClass().getName()))) return;
		WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
		random.setLargeFeatureWithSalt(ctx.seed(), ctx.chunkPos().x, ctx.chunkPos().z, SALT);
		float roll = random.nextFloat();
		if (roll < SURFACE) return;
		boolean air = roll >= SURFACE + (1 - SURFACE) / 2;
		try {
			StructurePieceSerializationContext sctx = new StructurePieceSerializationContext(null, ctx.registryAccess(), ctx.structureTemplateManager());
			builder.clear();
			for (StructurePiece piece : pieces) {
				if (!PIECE.equals(piece.getClass().getName())) { builder.addPiece(piece); continue; }
				CompoundTag tag = piece.createTag(sctx);
				net.minecraft.core.BlockPos c = net.minecraft.core.BlockPos.of(tag.getLong("c"));
				int r = (int) Math.ceil(tag.getFloat("r"));
				int y = newY(c.getY() + r + 1, r, air, ctx.heightAccessor(), random);   // AE2: centre = ground - (r + 1)
				if (y == Integer.MIN_VALUE) { builder.addPiece(piece); continue; }
				tag.putLong("c", new net.minecraft.core.BlockPos(c.getX(), y, c.getZ()).asLong());
				tag.putByte("t", (byte) 0);   // CraterType.NONE
				tag.putBoolean("p", false);
				tag.putBoolean("l", false);
				StructurePiece moved = piece.getType().load(sctx, tag);
				moved.move(0, y - c.getY(), 0);   // the box sits at the centre's height
				builder.addPiece(moved);
			}
		} catch (RuntimeException e) {
			LOG.error("[meteorites] could not move a meteorite piece; it stays on the surface", e);
			builder.clear();
			pieces.forEach(builder::addPiece);
		}
	}

	/** Underground: floor + r + 4 .. ground - CLEAR - r; air: ground + AIR_MIN + r .. ground + AIR_MAX, under the build limit. */
	static int newY(int ground, int r, boolean air, LevelHeightAccessor h, WorldgenRandom random) {
		int lo, hi;
		if (air) {
			lo = ground + AIR_MIN + r;
			hi = Math.min(ground + AIR_MAX, h.getMaxBuildHeight() - r - 2);
		} else {
			lo = h.getMinBuildHeight() + r + 4;
			hi = ground - CLEAR - r;
		}
		return hi < lo ? Integer.MIN_VALUE : lo + random.nextInt(hi - lo + 1);
	}
}
