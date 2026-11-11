package com.SaintAlfonzo.creativeworldgen.mixin;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalInt;

/**
 * Flat (and debug / single biome) worlds stay flat. In 1.19.2 a world's dimensions are decoded with
 * RegistryCodecs.dataPackAwareCodec, which lets every data pack's {@code dimension/overworld.json} replace the preset's
 * overworld: BiomeLayoutTuner's (KubeJS virtual pack, biome_overrides.js), Terralith's and Tectonic's. So a
 * {@code level-type=minecraft:flat} server or a Superflat single-player world came out as normal terrain
 * (testing/structures/09_phase_9.md 3.2). Here the dimension registry keeps its own overworld when it isn't a noise
 * generator on a multi-noise biome source; normal worlds (new or loaded) still take the data packs' overworld.
 */
@Mixin(value = RegistryLoader.class, remap = false)
public class RegistryLoaderPresetOverworldMixin {

	@Unique private static final Logger creativeengineering$LOG = LogManager.getLogger("creativeworldgen/worldgen");
	@Unique private static final ThreadLocal<LevelStem> creativeengineering$presetOverworld = new ThreadLocal<>();

	@Inject(method = "m_206762_", at = @At("HEAD"), require = 1, remap = false)
	private void creativeengineering$rememberPresetOverworld(WritableRegistry<?> registry, ResourceKey<?> key, Codec<?> codec,
			DynamicOps<JsonElement> ops, CallbackInfoReturnable<DataResult<?>> cir) {
		creativeengineering$presetOverworld.remove();
		if (!Registry.LEVEL_STEM_REGISTRY.equals(key)) return;
		@SuppressWarnings("unchecked")
		LevelStem stem = ((WritableRegistry<LevelStem>) registry).get(LevelStem.OVERWORLD);
		if (stem != null && !creativeengineering$isMultiNoise(stem.generator())) creativeengineering$presetOverworld.set(stem);
	}

	@Inject(method = "m_206762_", at = @At("RETURN"), require = 1, remap = false)
	private void creativeengineering$restorePresetOverworld(WritableRegistry<?> registry, ResourceKey<?> key, Codec<?> codec,
			DynamicOps<JsonElement> ops, CallbackInfoReturnable<DataResult<?>> cir) {
		LevelStem preset = creativeengineering$presetOverworld.get();
		if (preset == null) return;
		creativeengineering$presetOverworld.remove();
		@SuppressWarnings("unchecked")
		WritableRegistry<LevelStem> stems = (WritableRegistry<LevelStem>) registry;
		if (stems.get(LevelStem.OVERWORLD) == preset) return;
		stems.registerOrOverride(OptionalInt.empty(), LevelStem.OVERWORLD, preset, Lifecycle.stable());
		creativeengineering$LOG.info("[worldgen] kept the world's own overworld generator ({}): data pack dimension/overworld.json overrides skipped",
				preset.generator().getClass().getSimpleName());
	}

	@Unique
	private static boolean creativeengineering$isMultiNoise(ChunkGenerator generator) {
		return generator instanceof NoiseBasedChunkGenerator && generator.getBiomeSource() instanceof MultiNoiseBiomeSource;
	}
}
