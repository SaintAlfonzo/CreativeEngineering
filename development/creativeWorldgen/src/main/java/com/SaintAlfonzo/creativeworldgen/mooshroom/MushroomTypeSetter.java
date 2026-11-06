package com.SaintAlfonzo.creativeworldgen.mooshroom;

import net.minecraft.world.entity.animal.MushroomCow;

/**
 * Every MushroomCow implements this through {@code MushroomCowBreedingMixin}: the private
 * {@code MushroomCow.setMushroomType} (m_28928_), for brown natural spawns and a bred calf's mushroom.
 */
public interface MushroomTypeSetter {
	void creativeengineering$setMushroomType(MushroomCow.MushroomType type);
}
