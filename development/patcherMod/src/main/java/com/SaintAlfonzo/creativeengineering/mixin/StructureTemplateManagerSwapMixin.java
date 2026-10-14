package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.worldgen.TemplateBlockSwaps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * StructureTemplateManager#get (m_230407_), which every jigsaw piece and single-template structure loads its template
 * through (getOrCreate goes through it too): apply the per-position block swaps (worldgen/TemplateBlockSwaps) to a
 * template the first time it is handed out.
 */
@Mixin(value = StructureTemplateManager.class, remap = false)
public class StructureTemplateManagerSwapMixin {

	@Inject(method = "m_230407_(Lnet/minecraft/resources/ResourceLocation;)Ljava/util/Optional;", at = @At("RETURN"), require = 1, remap = false)
	private void creativeengineering$swapBlocks(ResourceLocation id, CallbackInfoReturnable<Optional<StructureTemplate>> cir) {
		cir.getReturnValue().ifPresent(t -> TemplateBlockSwaps.apply(id, t));
	}
}
