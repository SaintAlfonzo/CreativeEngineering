package com.SaintAlfonzo.creativeworldgen.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraftforge.common.world.PieceBeardifierModifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A placed feature as a piece of a meta structure (driftwood in a flotilla). The feature is placed once, when
 * the chunk holding its spot is decorated, through the placed feature's own modifiers (heightmap, block
 * filters) but without a biome filter: the "biome" modifier only works for features a biome lists, so a placed
 * feature used here must not have one. The box (spot +- radius) only decides which chunks call postProcess;
 * the feature may write into the neighbouring chunks like any feature. No beard (Forge's PieceBeardifierModifier).
 */
public final class MetaFeaturePiece extends StructurePiece implements PieceBeardifierModifier {

	private static final Logger LOG = LogManager.getLogger("creativeengineering/meta-structures");
	private static final Set<ResourceLocation> MISSING = ConcurrentHashMap.newKeySet();

	private final ResourceLocation feature;
	private BlockPos spot;

	MetaFeaturePiece(ResourceLocation feature, BlockPos spot, int radius) {
		super(MetaStructure.FEATURE_PIECE.get(), 0, new BoundingBox(spot.getX() - radius, spot.getY() - radius, spot.getZ() - radius,
				spot.getX() + radius, spot.getY() + radius, spot.getZ() + radius));
		this.feature = feature;
		this.spot = spot;
	}

	MetaFeaturePiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
		super(MetaStructure.FEATURE_PIECE.get(), tag);
		this.feature = new ResourceLocation(tag.getString("feature"));
		this.spot = NbtUtils.readBlockPos(tag.getCompound("spot"));
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
		tag.putString("feature", feature.toString());
		tag.put("spot", NbtUtils.writeBlockPos(spot));
	}

	@Override
	public void move(int x, int y, int z) {
		super.move(x, y, z);
		spot = spot.offset(x, y, z);
	}

	@Override
	public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
			BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
		if (!chunkBox.isInside(spot)) return;
		PlacedFeature placed = level.registryAccess().registryOrThrow(Registry.PLACED_FEATURE_REGISTRY).get(feature);
		if (placed == null) {
			if (MISSING.add(feature)) LOG.warn("[meta-structures] placed feature {} does not exist; its pieces place nothing", feature);
			return;
		}
		placed.place(level, generator, RandomSource.create(level.getSeed() ^ spot.asLong() * 0x9E3779B97F4A7C15L), spot);
	}

	@Override public BoundingBox getBeardifierBox() { return boundingBox; }
	@Override public TerrainAdjustment getTerrainAdjustment() { return TerrainAdjustment.NONE; }
	@Override public int getGroundLevelDelta() { return 0; }
}
