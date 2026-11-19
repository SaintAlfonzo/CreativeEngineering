package com.SaintAlfonzo.creativeloot.mixin;

import com.SaintAlfonzo.creativeloot.GlmMerge;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifierManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Runs {@link GlmMerge} at the end of Forge's {@code LootModifierManager.apply(Map, ResourceManager, ProfilerFiller)},
 * on the same reload, after Forge has built its list. The merged map replaces Forge's (feature_writeup/creative_loot.md,
 * plan_rules_fixes.md section 5).
 *
 * <p>Target: Forge's own method keeps the name {@code apply} in the production jar too; only the erased bridge
 * {@code m_5787_(Object, ...)} carries the SRG name ({@code javap -p} on forge-1.19.2-43.3.13-universal.jar). The old
 * target {@code m_5787_(Map, ...)} matched neither, so every jar GLM was dropped silently
 * (plan_bugfix_r1118.md B1); {@code injectors.defaultRequire} in creativeloot.mixins.json now fails a miss at load.
 */
@Mixin(value = LootModifierManager.class, remap = false)
public abstract class LootModifierManagerMixin {

	@Shadow
	private Map<ResourceLocation, IGlobalLootModifier> registeredLootModifiers;

	@Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
			at = @At("TAIL"), remap = false)
	private void creativeloot$merge(Map<ResourceLocation, JsonElement> definitions, ResourceManager resources, ProfilerFiller profiler,
			CallbackInfo ci) {
		this.registeredLootModifiers = GlmMerge.merge(this.registeredLootModifiers, definitions, resources);
	}
}
