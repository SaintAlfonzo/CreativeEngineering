package com.SaintAlfonzo.creativedelight.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Banana merge (feature_writeup/item_merges.md 2.1) for the drops Alex's Mobs builds in code from its own banana: the
 * {@code mixin.alexsmobs} compartment hands its {@code AMItemRegistry.BANANA} reads to {@link #banana}. No Alex's Mobs
 * type here, so this class loads the same without it.
 */
public final class BananaMergeHooks {
	private BananaMergeHooks() {}

	private static final RegistryObject<Item> NEAPOLITAN_BANANA =
			RegistryObject.create(new ResourceLocation("neapolitan", "banana"), ForgeRegistries.ITEMS);

	/** Neapolitan's banana when it is registered, else the purged one unchanged (Neapolitan absent). */
	public static RegistryObject<Item> banana(RegistryObject<Item> alexs) {
		return NEAPOLITAN_BANANA.isPresent() ? NEAPOLITAN_BANANA : alexs;
	}
}
