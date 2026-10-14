package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.worldgen.PlacementPriority;
import net.minecraft.core.Registry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Placement priority shaping (worldgen/PlacementPriority#shape): JigsawPlacement's assembler (m_227210_, private
 * static, called once per jigsaw structure with the free space its pieces must fit in) gets the higher-ranked
 * structures predicted nearby cut out of that space, so a settlement grows around them.
 */
@Mixin(value = JigsawPlacement.class, remap = false)
public class JigsawPlacementPriorityMixin {

	@ModifyVariable(method = "m_227210_(Lnet/minecraft/world/level/levelgen/RandomState;IZLnet/minecraft/world/level/chunk/ChunkGenerator;"
			+ "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplateManager;Lnet/minecraft/world/level/LevelHeightAccessor;"
			+ "Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/Registry;Lnet/minecraft/world/level/levelgen/structure/PoolElementStructurePiece;"
			+ "Ljava/util/List;Lnet/minecraft/world/phys/shapes/VoxelShape;)V",
			at = @At("HEAD"), argsOnly = true, require = 1, remap = false)
	private static VoxelShape creativeengineering$shapeAroundHigher(VoxelShape free, RandomState random, int depth, boolean expansionHack,
			ChunkGenerator generator, StructureTemplateManager templates, LevelHeightAccessor heights, RandomSource rand, Registry<?> pools,
			PoolElementStructurePiece start) {
		return PlacementPriority.shape(free, start);
	}
}
