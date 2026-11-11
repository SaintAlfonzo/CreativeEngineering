package com.SaintAlfonzo.creativeworldgen.mixin;

import com.SaintAlfonzo.creativeworldgen.worldgen.LegacyIds;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * D-DIM read alias (worldgen/LegacyIds#keyAlias): ResourceKey#create(ResourceKey, ResourceLocation) (m_135785_) returns
 * the creativeworldgen: key for the old creativeengineering: dimension, level stem, dimension type and noise settings
 * ids. Every saved dimension string is read through it (level.dat's dimension list through the registry codecs, player
 * Dimension and SpawnDimension through Level.RESOURCE_KEY_CODEC, other mods' saved data, typed commands), so old saves
 * resolve to the moved dimensions. Ids of any other namespace return at the first comparison.
 */
@Mixin(value = ResourceKey.class, remap = false)
public class ResourceKeyLegacyDimensionMixin {

	@Inject(method = "m_135785_(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/resources/ResourceKey;",
			at = @At("HEAD"), cancellable = true, require = 1, remap = false)
	private static <T> void creativeworldgen$legacyDimension(ResourceKey<? extends Registry<T>> registry, ResourceLocation location,
			CallbackInfoReturnable<ResourceKey<T>> cir) {
		if (!LegacyIds.LEGACY_NS.equals(location.getNamespace())) return;
		ResourceLocation moved = LegacyIds.keyAlias(registry.location(), location);
		if (moved != null) cir.setReturnValue(ResourceKey.create(registry, moved));
	}
}
