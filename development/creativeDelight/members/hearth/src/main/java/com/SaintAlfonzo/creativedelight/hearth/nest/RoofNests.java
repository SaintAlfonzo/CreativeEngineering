package com.SaintAlfonzo.creativedelight.hearth.nest;

import com.SaintAlfonzo.creativedelight.hearth.crow.CrowConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;

/**
 * Village roof nests (plan_hearth_crow_swap.md 5.2), run from ChunkGeneratorCrowNestMixin at the end of a chunk's decoration.
 * Each village house piece (template path containing "/houses/") gets one roll, seeded from its box, so every chunk the house
 * covers computes the same column; only the chunk holding that column places, so a house gets at most one nest. Every read and
 * write is inside the chunk being decorated.
 */
public final class RoofNests {
    private static final int ROOF_DEPTH = 3;

    private RoofNests() {
    }

    public static void decorate(WorldGenLevel level, ChunkAccess chunk, StructureManager structures) {
        if (!CrowConfig.VILLAGE_ROOF_NESTS.get()) {
            return;
        }
        ChunkPos chunkPos = chunk.getPos();
        Registry<Structure> registry = level.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY);
        double chance = CrowConfig.VILLAGE_ROOF_NEST_CHANCE.get();
        for (StructureStart start : structures.startsForStructure(chunkPos,
                structure -> registry.getResourceKey(structure).flatMap(registry::getHolder).map(holder -> holder.is(StructureTags.VILLAGE)).orElse(false))) {
            for (StructurePiece piece : start.getPieces()) {
                if (piece instanceof PoolElementStructurePiece pool && isHouse(pool)) {
                    tryHouse(level, chunkPos, pool.getBoundingBox(), chance);
                }
            }
        }
    }

    /** SinglePoolElement.toString() is "Single[Left[<template id>]]"; the template field itself is SRG-named. */
    private static boolean isHouse(PoolElementStructurePiece piece) {
        return piece.getElement() instanceof SinglePoolElement element && element.toString().contains("/houses/");
    }

    private static void tryHouse(WorldGenLevel level, ChunkPos chunkPos, BoundingBox box, double chance) {
        RandomSource random = RandomSource.create(Mth.getSeed(box.minX(), box.minY(), box.minZ()) ^ level.getSeed());
        if (random.nextDouble() >= chance) {
            return;
        }
        // middle third of the box: the ridge of most roofs, so the column more often meets a roof than an eave
        int x = middle(random, box.minX(), box.maxX());
        int z = middle(random, box.minZ(), box.maxZ());
        if (x >> 4 != chunkPos.x || z >> 4 != chunkPos.z) {
            return;
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, box.maxY(), z);
        for (int y = box.maxY(); y >= box.maxY() - ROOF_DEPTH && y >= box.minY(); y--) {
            cursor.setY(y);
            BlockState state = level.getBlockState(cursor);
            if (state.isAir()) {
                continue;
            }
            BlockPos above = cursor.above();
            if (state.isCollisionShapeFullBlock(level, cursor) && level.isEmptyBlock(above)
                    && level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) <= above.getY()) {
                NestCrows.placeGenerated(level, above, random, NestCrows.VILLAGE_LOOT);
            }
            return;
        }
    }

    private static int middle(RandomSource random, int min, int max) {
        int third = (max - min + 1) / 3;
        return min + third + random.nextInt(Math.max(1, max - min + 1 - 2 * third));
    }
}
