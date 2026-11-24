package com.SaintAlfonzo.creativeloot;

import com.SaintAlfonzo.creativeloot.corpse.AncientCorpseModifier;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Creative Loot (feature_writeup/creative_loot.md): the pack's loot engine. The engine itself is KubeJS: the startup
 * half (global.LOOT, the rules queue) in this jar's kubejs_startup/creativeloot/, the server scripts in
 * data/creativeloot/kubejs/ (loot tuning, drops, the LootJS layer). The Java side is {@link GlmMerge} (one merged
 * global loot modifier list, hooked into Forge's LootModifierManager by mixin/LootModifierManagerMixin) and
 * {@link CLResources} (the jar copy of the tuning tables). Registers the ancient corpse feature and its biome modifier
 * serializer ({@code corpse/}, plan_corpses.md 3.2; active only with the Corpse mod). Also the Wandering Trader spawn
 * wait and offer counts ({@code trader/}, two mixins, {@link LootConfig} {@code [wandering_trader]}), moved here from
 * Creative Common in the wave 2 carve (plan_wave2_publish.md W2-2). Requires Creative Common and Creative Compat;
 * Creative Worldgen, Creative Delight and Creative Brewing require this jar.
 */
@Mod(CreativeLoot.MOD_ID)
public class CreativeLoot {

	public static final String MOD_ID = "creativeloot";
	public static final Logger LOGGER = LogUtils.getLogger();

	public CreativeLoot() {
		// [wandering_trader] from creativecommon-common.toml, once (W2-8): carry when this file is new, merge when it
		// exists. Must run before Forge reads the COMMON configs; ConfigRename logs a [swaps] line when it copies.
		if (!com.SaintAlfonzo.creativecommon.swap.ConfigRename.carrySections(LootConfig.COMMON_FILE, LootConfig.FILE, LootConfig.COMMON_SECTIONS))
			com.SaintAlfonzo.creativecommon.swap.ConfigRename.mergeSections(LootConfig.COMMON_FILE, LootConfig.FILE, LootConfig.COMMON_SECTIONS);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, LootConfig.SPEC, LootConfig.FILE);
		AncientCorpseModifier.register(FMLJavaModLoadingContext.get().getModEventBus());
		LOGGER.info("[creativeloot] loaded");
	}
}
