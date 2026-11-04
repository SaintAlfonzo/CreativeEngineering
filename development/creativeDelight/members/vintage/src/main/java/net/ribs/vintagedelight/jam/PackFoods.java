// Pack addition (CreativeEngineering2, TODO dishes/jams #8). Own work, GPLv3.
package net.ribs.vintagedelight.jam;

import net.minecraft.world.food.FoodProperties;

/** A bowl of jello: Fruits Delight's jello values (1 hunger, 0.3, fast, always edible). */
public final class PackFoods {
    private PackFoods() {}

    public static final FoodProperties JELLO = new FoodProperties.Builder().nutrition(1).saturationMod(0.3F).fast().alwaysEat().build();
}
