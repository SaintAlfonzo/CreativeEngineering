package com.SaintAlfonzo.creativeengineering.worldgen;

import com.SaintAlfonzo.creativeengineering.CreativeEngineering;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraftforge.event.level.SaplingGrowTreeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Upgrade Aquatic's river sapling (Mulberry Sapling in game) grows the datapack copy of {@code upgrade_aquatic:river_tree}
 * (kubejs/data/upgrade_aquatic/worldgen/configured_feature/river_tree.json: oak trunk, mulberry leaves and vines).
 * Worldgen already reads that copy; UA's RiverTreeGrower returns the built-in holder, which a datapack override doesn't
 * reach in 1.19.2, so saplings would keep growing river logs. Switch: [worldgen.systems] mulberrySapling.
 * Feature writeup: recipes_wood.md 2.6.
 */
@Mod.EventBusSubscriber(modid = CreativeEngineering.MODID)
public final class MulberrySapling {

	private static final ResourceKey<ConfiguredFeature<?, ?>> RIVER_TREE = ResourceKey.create(
			Registry.CONFIGURED_FEATURE_REGISTRY, new ResourceLocation("upgrade_aquatic", "river_tree"));

	private MulberrySapling() {
	}

	@SubscribeEvent
	public static void onGrow(SaplingGrowTreeEvent event) {
		Holder<? extends ConfiguredFeature<?, ?>> feature = event.getFeature();
		if (feature == null || !feature.is(RIVER_TREE.location()) || !WorldgenConfig.mulberrySaplingEnabled()) return;
		event.getLevel().registryAccess().registry(Registry.CONFIGURED_FEATURE_REGISTRY)
				.flatMap(r -> r.getHolder(RIVER_TREE))
				.ifPresent(event::setFeature);
	}
}
