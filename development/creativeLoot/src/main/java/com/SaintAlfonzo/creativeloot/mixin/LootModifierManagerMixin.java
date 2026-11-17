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
 * Runs {@link GlmMerge} at the end of Forge's {@code LootModifierManager.apply} (SRG {@code m_5787_}, the
 * SimpleJsonResourceReloadListener override), on the same reload, after Forge has built its list. The merged map
 * replaces Forge's (feature_writeup/creative_loot.md, plan_rules_fixes.md section 5).
 */
@Mixin(value = LootModifierManager.class, remap = false)
public abstract class LootModifierManagerMixin {

	@Shadow
	private Map<ResourceLocation, IGlobalLootModifier> registeredLootModifiers;

	@Inject(method = "m_5787_(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
			at = @At("TAIL"), remap = false)
	private void creativeloot$merge(Map<ResourceLocation, JsonElement> definitions, ResourceManager resources, ProfilerFiller profiler,
			CallbackInfo ci) {
		this.registeredLootModifiers = GlmMerge.merge(this.registeredLootModifiers, definitions, resources);
	}
}
