package com.SaintAlfonzo.creativeworldgen.mixin;

import com.SaintAlfonzo.creativeworldgen.worldgen.GroundMatch;
import com.SaintAlfonzo.creativeworldgen.worldgen.TemplateTrim;
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
 * Two rewrites of a structure template's processed block list, in this order: worldgen/TemplateTrim drops the
 * air and bare pad around the built columns of creativeworldgen:template_trim structures, then
 * worldgen/GroundMatch swaps a grass/dirt base for the surrounding ground where that ground isn't grass/dirt.
 * Same target as StructureTemplateLootSwapMixin: Forge's 6-argument processBlockInfos, which keeps its name in
 * production and which every template placement uses.
 */
@Mixin(value = StructureTemplate.class, remap = false)
public class StructureTemplateGroundMatchMixin {

	@Inject(method = "processBlockInfos(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructurePlaceSettings;Ljava/util/List;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate;)Ljava/util/List;",
			at = @At("RETURN"), cancellable = true)
	private static void creativeengineering$matchGround(LevelAccessor level, BlockPos offset, BlockPos pos, StructurePlaceSettings settings,
			List<StructureTemplate.StructureBlockInfo> infos, StructureTemplate template,
			CallbackInfoReturnable<List<StructureTemplate.StructureBlockInfo>> cir) {
		List<StructureTemplate.StructureBlockInfo> out = cir.getReturnValue();
		List<StructureTemplate.StructureBlockInfo> trimmed = TemplateTrim.apply(level, out);
		if (trimmed != null) out = trimmed;
		List<StructureTemplate.StructureBlockInfo> matched = GroundMatch.apply(level, offset, settings, out);
		if (matched != null) out = matched;
		if (out != cir.getReturnValue()) cir.setReturnValue(out);
	}
}
