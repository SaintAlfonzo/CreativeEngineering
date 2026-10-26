package com.SaintAlfonzo.creativedelight.delightful;

import com.teamabnormals.blueprint.core.api.BlueprintCauldronInteraction;
import com.teamabnormals.neapolitan.core.other.NeapolitanCauldronInteractions;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

/**
 * Neapolitan compartment: the matcha and salmonberry milkshake cauldrons (3.8.1 DelightfulCauldronInteractions).
 * A milk cauldron + the ice cream makes a milkshake cauldron; bottles fill and empty it, as Neapolitan's own
 * flavours. The only class that names Neapolitan or Blueprint, loaded only when Neapolitan is (the registry and
 * {@link DelightfulBackports} check first).
 */
public final class DelightfulMilkshakes {

	private static final BlueprintCauldronInteraction MATCHA = register("matcha");
	private static final BlueprintCauldronInteraction SALMONBERRY = register("salmonberry");

	private DelightfulMilkshakes() {}

	private static BlueprintCauldronInteraction register(String flavour) {
		return BlueprintCauldronInteraction.register(new ResourceLocation("delightful", flavour + "_milkshake"),
				CauldronInteraction.newInteractionMap());
	}

	/** The cauldron block's interaction map (filled in by {@link #registerCauldronInteractions}). */
	static Map<Item, CauldronInteraction> map(String flavour) {
		return ("matcha".equals(flavour) ? MATCHA : SALMONBERRY).map();
	}

	static void registerCauldronInteractions() {
		add(DelightfulBackportRegistry.MATCHA_MILKSHAKE.get(), DelightfulBackportRegistry.MATCHA_MILKSHAKE_CAULDRON.get(), "matcha");
		add(DelightfulBackportRegistry.SALMONBERRY_MILKSHAKE.get(), DelightfulBackportRegistry.SALMONBERRY_MILKSHAKE_CAULDRON.get(), "salmonberry");
	}

	private static void add(Item milkshake, Block cauldron, String flavour) {
		Item iceCream = ForgeRegistries.ITEMS.getValue(new ResourceLocation("delightful", flavour + "_ice_cream"));
		NeapolitanCauldronInteractions.addMilkshakeInteractions(milkshake, cauldron, iceCream, map(flavour));
	}
}
