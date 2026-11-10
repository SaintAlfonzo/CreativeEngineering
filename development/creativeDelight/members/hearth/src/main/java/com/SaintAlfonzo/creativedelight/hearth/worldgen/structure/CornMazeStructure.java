package com.SaintAlfonzo.creativedelight.hearth.worldgen.structure;

import com.SaintAlfonzo.creativedelight.hearth.HHConfig;
import com.SaintAlfonzo.creativedelight.hearth.worldgen.HHWorldgen;
import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/** The Corn Maze (hearth_and_harvest.md 3.2, N14). Frequency, biomes and spacing come from the structure framework. */
public class CornMazeStructure extends Structure {
    public static final Codec<CornMazeStructure> CODEC = simpleCodec(CornMazeStructure::new);
    private static final int MAX_HEIGHT_VARIANCE = 5;

    public CornMazeStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public StructureType<?> type() {
        return HHWorldgen.CORN_MAZE.get();
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        if (!HHConfig.CORN_MAZE_ENABLED.get() || CornMazePiece.corn() == null) {
            return Optional.empty();
        }
        RandomSource random = context.random();
        int w = 23 + random.nextInt(7) * 2;
        int h = 23 + random.nextInt(7) * 2;
        long mazeSeed = random.nextLong();
        int baseX = context.chunkPos().getMinBlockX();
        int baseZ = context.chunkPos().getMinBlockZ();
        if (!isValidPlacement(context, baseX, baseZ, w, h)) {
            return Optional.empty();
        }
        int surfaceY = context.chunkGenerator().getBaseHeight(baseX + w / 2, baseZ + h / 2, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        BlockPos origin = new BlockPos(baseX, surfaceY, baseZ);
        return Optional.of(new GenerationStub(origin, builder -> builder.addPiece(new CornMazePiece(origin, w, h, mazeSeed))));
    }

    private static boolean isValidPlacement(GenerationContext context, int baseX, int baseZ, int w, int h) {
        ChunkGenerator gen = context.chunkGenerator();
        LevelHeightAccessor heightAccessor = context.heightAccessor();
        RandomState randomState = context.randomState();
        int seaLevel = gen.getSeaLevel();
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (int sx = 0; sx <= w; sx += 4) {
            for (int sz = 0; sz <= h; sz += 4) {
                int x = baseX + sx;
                int z = baseZ + sz;
                int surface = gen.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, heightAccessor, randomState);
                int floor = gen.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, heightAccessor, randomState);
                if (surface != floor || surface <= seaLevel + 2) {
                    return false;
                }
                minY = Math.min(minY, surface);
                maxY = Math.max(maxY, surface);
            }
        }
        return maxY - minY <= MAX_HEIGHT_VARIANCE;
    }
}
