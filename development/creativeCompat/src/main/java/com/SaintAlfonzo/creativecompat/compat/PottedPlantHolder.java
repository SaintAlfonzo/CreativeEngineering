package com.SaintAlfonzo.creativecompat.compat;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

import javax.annotation.Nullable;

/**
 * A block entity that holds potted plants, read by {@link WthitPotContents}: BotanistPotBlockEntity (the Beautify
 * botanist workbench as a flower pot, one plant) and DoublePotBlockEntity (two flower pots in one block).
 */
public interface PottedPlantHolder {

	/** Id of the plant block in the pot, or null when empty. */
	@Nullable
	ResourceLocation plant();

	/** Every plant held, in slot order; empty when none. */
	default List<ResourceLocation> plants() {
		ResourceLocation plant = plant();
		return plant == null ? List.of() : List.of(plant);
	}
}
