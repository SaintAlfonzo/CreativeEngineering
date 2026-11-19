package com.SaintAlfonzo.creativeloot;

import com.SaintAlfonzo.creativeloot.corpse.AncientCorpseModifier;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Creative Loot (feature_writeup/creative_loot.md): the pack's loot engine. The engine itself is KubeJS: the startup
 * half (global.LOOT, the rules queue) in this jar's kubejs_startup/creativeloot/, the server scripts in
 * data/creativeloot/kubejs/ (loot tuning, drops, the LootJS layer). The Java side is {@link GlmMerge} (one merged
 * global loot modifier list, hooked into Forge's LootModifierManager by mixin/LootModifierManagerMixin) and
 * {@link CLResources} (the jar copy of the tuning tables). Registers the ancient corpse feature and its biome modifier
 * serializer ({@code corpse/}, plan_corpses.md 3.2; active only with the Corpse mod); no config file. Requires Creative
 * Common and Creative Compat; Creative Worldgen, Creative Delight and Creative Brewing require this jar.
 */
@Mod(CreativeLoot.MOD_ID)
public class CreativeLoot {

	public static final String MOD_ID = "creativeloot";
	public static final Logger LOGGER = LogUtils.getLogger();

	public CreativeLoot() {
		AncientCorpseModifier.register(FMLJavaModLoadingContext.get().getModEventBus());
		LOGGER.info("[creativeloot] loaded");
	}
}
