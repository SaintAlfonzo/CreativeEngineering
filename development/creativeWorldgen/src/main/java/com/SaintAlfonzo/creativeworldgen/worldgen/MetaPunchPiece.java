package com.SaintAlfonzo.creativeworldgen.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraftforge.common.world.PieceBeardifierModifier;

/**
 * The hole a 'punch' part leaves (plan_worldgen_edit B2): a round column of air from just above the meteorite's
 * sphere up to the top of the structures it struck, so the meteorite sits in a shaft through the pyramid instead of
 * AE2's crater carving away everything above the ground. Placed after the meteorite piece in the meta's list, so the
 * sky stone is already there and the column starts above it. No beard.
 */
public final class MetaPunchPiece extends StructurePiece implements PieceBeardifierModifier {

	private BlockPos base;
	private final int radius;

	MetaPunchPiece(BlockPos base, int radius, int top) {
		super(MetaStructure.PUNCH_PIECE.get(), 0, new BoundingBox(base.getX() - radius, base.getY(), base.getZ() - radius,
				base.getX() + radius, Math.max(base.getY(), top), base.getZ() + radius));
		this.base = base;
		this.radius = radius;
	}

	MetaPunchPiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
		super(MetaStructure.PUNCH_PIECE.get(), tag);
		this.base = NbtUtils.readBlockPos(tag.getCompound("base"));
		this.radius = tag.getInt("radius");
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
		tag.put("base", NbtUtils.writeBlockPos(base));
		tag.putInt("radius", radius);
	}

	@Override
	public void move(int x, int y, int z) {
		super.move(x, y, z);
		base = base.offset(x, y, z);
	}

	@Override
	public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
			BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		double r2 = (radius + 0.5) * (radius + 0.5);
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				if (dx * dx + dz * dz > r2) continue;
				for (int y = boundingBox.minY(); y <= boundingBox.maxY(); y++) {
					pos.set(base.getX() + dx, y, base.getZ() + dz);
					if (!chunkBox.isInside(pos) || level.getBlockState(pos).isAir()) continue;
					level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
				}
			}
		}
	}

	@Override public BoundingBox getBeardifierBox() { return boundingBox; }
	@Override public TerrainAdjustment getTerrainAdjustment() { return TerrainAdjustment.NONE; }
	@Override public int getGroundLevelDelta() { return 0; }
}
