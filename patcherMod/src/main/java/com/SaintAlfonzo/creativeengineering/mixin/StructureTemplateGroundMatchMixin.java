package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.worldgen.GroundMatch;
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
 * Swaps a structure's grass/dirt base for the surrounding ground where that ground isn't grass/dirt
 * (see worldgen/GroundMatch). Same target as StructureTemplateLootSwapMixin: Forge's 6-argument
 * processBlockInfos, which keeps its name in production and which every template placement uses.
 */
@Mixin(value = StructureTemplate.class, remap = false)
public class StructureTemplateGroundMatchMixin {

	@Inject(method = "processBlockInfos(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructurePlaceSettings;Ljava/util/List;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate;)Ljava/util/List;",
			at = @At("RETURN"), cancellable = true)
	private static void creativeengineering$matchGround(LevelAccessor level, BlockPos offset, BlockPos pos, StructurePlaceSettings settings,
			List<StructureTemplate.StructureBlockInfo> infos, StructureTemplate template,
			CallbackInfoReturnable<List<StructureTemplate.StructureBlockInfo>> cir) {
		List<StructureTemplate.StructureBlockInfo> matched = GroundMatch.apply(level, offset, settings, cir.getReturnValue());
		if (matched != null) cir.setReturnValue(matched);
	}
}
