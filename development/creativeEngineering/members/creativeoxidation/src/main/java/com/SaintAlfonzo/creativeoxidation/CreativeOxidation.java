// SPDX-License-Identifier: GPL-3.0-only
// Port of Create: Oxidized 1.0.0 (Mr.Mulle, MIT; NOTICE-upstream-MIT.txt).
package com.SaintAlfonzo.creativeoxidation;

import com.SaintAlfonzo.creativeoxidation.client.OxidationClient;
import net.minecraft.core.Registry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;

/**
 * Creative Oxidation (feature_writeup/plan_creative_oxidation.md), a Creative Engineering member: Create: Oxidized
 * 1.0.0 (createoxidized, Forge 1.20.1, Mr.Mulle, MIT) ported to Forge 1.19.2 / Create 0.5.1.
 *
 * <p>No new blocks or items. Two block state properties ({@link OxidationProperties}) on the target ids only
 * ({@link OxidationTargets}); aging on random ticks ({@link OxidationManager}); stage and wax on items as NBT
 * ({@link OxidationItemData}); the {@code creativeoxidation:staged} ingredient ({@link StagedIngredient}); a creative
 * tab with every variant; the client wraps the target blocks' baked models and swaps copper sprites for staged copies.
 */
@Mod(CreativeOxidation.MOD_ID)
public final class CreativeOxidation {
	public static final String MOD_ID = "creativeoxidation";

	public static CreativeModeTab TAB;

	public CreativeOxidation() {
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, OxidationConfig.SPEC);
		TAB = new OxidationCreativeTab();
		modBus.addListener(CreativeOxidation::onRegister);
		OxidationEvents.register();
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> OxidationClient.init(modBus));
	}

	private static void onRegister(RegisterEvent event) {
		if (event.getRegistryKey().equals(Registry.RECIPE_SERIALIZER_REGISTRY)) {
			CraftingHelper.register(StagedIngredient.ID, StagedIngredient.Serializer.INSTANCE);
		}
	}
}
