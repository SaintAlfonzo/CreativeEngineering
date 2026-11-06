package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.compat.BotanistPot;
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
 * Botanist workbenches placed by a structure template start with a random vanilla flower (compat/BotanistPot.seed).
 * Same target as StructureTemplateLootSwapMixin: Forge's 6-argument processBlockInfos.
 */
@Mixin(value = StructureTemplate.class, remap = false)
public class StructureTemplateBotanistPotMixin {

	@Inject(method = "processBlockInfos(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructurePlaceSettings;Ljava/util/List;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate;)Ljava/util/List;",
			at = @At("RETURN"), cancellable = true)
	private static void creativeengineering$seedBotanistPots(LevelAccessor level, BlockPos offset, BlockPos pos, StructurePlaceSettings settings,
			List<StructureTemplate.StructureBlockInfo> infos, StructureTemplate template,
			CallbackInfoReturnable<List<StructureTemplate.StructureBlockInfo>> cir) {
		List<StructureTemplate.StructureBlockInfo> seeded = BotanistPot.seed(cir.getReturnValue(), settings);
		if (seeded != null) cir.setReturnValue(seeded);
	}
}
