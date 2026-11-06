package com.SaintAlfonzo.creativeworldgen.mixin;

import com.SaintAlfonzo.creativeworldgen.worldgen.DeadPlacementFilter;
import net.minecraft.core.Holder;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Stops structure searches from scanning placements that can never generate.
 *
 * THE BUG:
 *   ChunkGenerator#findNearestMapStructure (explorer-map loot, /locate, cartographer trades,
 *   dolphins) walks every candidate chunk of every placement out to the search radius and runs
 *   the structure's full generation-point check on each one. It never looks at the placement's
 *   frequency. structure_overrides.js disables a whole structure_set by writing frequency 0, so
 *   a search for one (e.g. Monobank's vault loot rolling a ruined_portal map) scans all
 *   ~10k candidates, finds nothing, and hangs the server tick past the 60 s watchdog
 *   (debug/crash-2026-09-27_15.32.34-server.txt).
 *
 * THE FIX:
 *   getPlacementsForStructure (m_223138_) is private and only called by findNearestMapStructure,
 *   so filtering its result drops frequency-0 placements from every search and touches nothing
 *   else. If every placement is dead the search returns null, same as vanilla for a structure
 *   that doesn't exist: maps come out blank and /locate reports "not found".
 */
@Mixin(value = ChunkGenerator.class, remap = false)
public class ChunkGeneratorLocateMixin {

	@Inject(method = "m_223138_(Lnet/minecraft/core/Holder;Lnet/minecraft/world/level/levelgen/RandomState;)Ljava/util/List;",
			at = @At("RETURN"), cancellable = true, require = 1, remap = false)
	private void creativeengineering$dropDeadPlacements(Holder<?> structure, RandomState randomState,
			CallbackInfoReturnable<List<StructurePlacement>> cir) {
		List<StructurePlacement> placements = cir.getReturnValue();
		List<StructurePlacement> live = DeadPlacementFilter.filter(placements);
		if (live != placements) cir.setReturnValue(live);
	}
}
