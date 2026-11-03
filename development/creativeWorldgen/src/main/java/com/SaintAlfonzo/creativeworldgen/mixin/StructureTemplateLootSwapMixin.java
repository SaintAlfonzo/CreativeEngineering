package com.SaintAlfonzo.creativeworldgen.mixin;

import com.SaintAlfonzo.creativeworldgen.worldgen.LootContainerSwaps;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Swaps modded loot containers for vanilla ones as a structure template places them, so Lootr
 * converts them (see worldgen/LootContainerSwaps). Targets Forge's 6-argument processBlockInfos:
 * it's Forge-added, so it keeps this name in production (no SRG name), and both placeInWorld and
 * vanilla's 5-argument overload route through it.
 */
@Mixin(value = StructureTemplate.class, remap = false)
public class StructureTemplateLootSwapMixin {

	@Inject(method = "processBlockInfos(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructurePlaceSettings;Ljava/util/List;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate;)Ljava/util/List;",
			at = @At("RETURN"), cancellable = true)
	private static void creativeengineering$swapLootContainers(LevelAccessor level, BlockPos offset, BlockPos pos, StructurePlaceSettings settings,
			List<StructureTemplate.StructureBlockInfo> infos, StructureTemplate template,
			CallbackInfoReturnable<List<StructureTemplate.StructureBlockInfo>> cir) {
		List<StructureTemplate.StructureBlockInfo> swapped = LootContainerSwaps.apply(cir.getReturnValue());
		if (swapped != null) cir.setReturnValue(swapped);
	}
}
