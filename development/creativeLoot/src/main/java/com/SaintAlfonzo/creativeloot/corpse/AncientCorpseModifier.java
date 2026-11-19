package com.SaintAlfonzo.creativeloot.corpse;

import com.SaintAlfonzo.creativeloot.CreativeLoot;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

/**
 * Adds the {@link AncientCorpseFeature} to overworld biomes and the remains biomes (feature_writeup/plan_corpses.md 3.2).
 * Data: {@code data/creativeloot/forge/biome_modifier/ancient_corpses.json} ({@code "type": "creativeloot:ancient_corpses"},
 * always present); the modifier adds nothing unless the Corpse mod is loaded and {@code ancient_corpses.enabled} is
 * true, so the jar runs without Corpse. The feature, its configured and placed features and the serializer are
 * registered always (they never touch Corpse classes).
 */
public record AncientCorpseModifier() implements BiomeModifier {
	public static final Codec<AncientCorpseModifier> CODEC = Codec.unit(AncientCorpseModifier::new);

	static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, CreativeLoot.MOD_ID);
	static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED =
			DeferredRegister.create(Registry.CONFIGURED_FEATURE_REGISTRY, CreativeLoot.MOD_ID);
	static final DeferredRegister<PlacedFeature> PLACED = DeferredRegister.create(Registry.PLACED_FEATURE_REGISTRY, CreativeLoot.MOD_ID);
	static final DeferredRegister<Codec<? extends BiomeModifier>> SERIALIZERS =
			DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, CreativeLoot.MOD_ID);

	static final RegistryObject<AncientCorpseFeature> FEATURE = FEATURES.register("ancient_corpse", AncientCorpseFeature::new);
	static final RegistryObject<ConfiguredFeature<?, ?>> CONFIGURED_CORPSE = CONFIGURED.register("ancient_corpse",
			() -> new ConfiguredFeature<>(FEATURE.get(), NoneFeatureConfiguration.INSTANCE));
	static final RegistryObject<PlacedFeature> PLACED_CORPSE = PLACED.register("ancient_corpse",
			() -> new PlacedFeature(CONFIGURED_CORPSE.getHolder().orElseThrow(), List.of()));
	static final RegistryObject<Codec<AncientCorpseModifier>> SERIALIZER = SERIALIZERS.register("ancient_corpses", () -> CODEC);

	public static void register(IEventBus modBus) {
		FEATURES.register(modBus);
		CONFIGURED.register(modBus);
		PLACED.register(modBus);
		SERIALIZERS.register(modBus);
		MinecraftForge.EVENT_BUS.addListener(AncientCorpseEvents::onReloadListeners);
		MinecraftForge.EVENT_BUS.addListener(AncientCorpseEvents::onServerAboutToStart);
		if (ModList.get().isLoaded("corpse")) AncientCorpseEvents.registerCorpseListeners();
	}

	@Override
	public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
		if (phase != Phase.ADD || !ModList.get().isLoaded("corpse")) return;
		AncientCorpseConfig c = AncientCorpseConfig.get();
		if (!c.enabled) return;
		boolean remains = false;
		if (c.remains.enabled()) for (String id : c.remains.biomes()) if (biome.is(new ResourceLocation(id))) remains = true;
		if (!remains && !biome.is(BiomeTags.IS_OVERWORLD)) return;
		builder.getGenerationSettings().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION,
				PLACED_CORPSE.getHolder().orElseThrow());
	}

	@Override
	public Codec<? extends BiomeModifier> codec() {
		return SERIALIZER.get();
	}
}
