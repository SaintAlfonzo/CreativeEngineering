package com.SaintAlfonzo.creativedelight.compat;

import dev.xkmc.fruitsdelight.events.FoodDataAccessor;
import net.minecraft.world.entity.player.Player;

/**
 * Replacement for the {@code fruitsdelight$setPlayer} call in Fruits Delight's getFoodData handler.
 * {@link CDMixinPlugin} rewrites the handler to call this instead. Only reached when Fruits Delight is loaded (the
 * call lives in its handler), so the Fruits Delight type here is never resolved without it.
 */
public final class FruitsDelightHooks {
	private FruitsDelightHooks() {}

	/** {@code accessor.fruitsdelight$setPlayer(player)}, skipped for a null FoodData (Fruits Delight 1.1.3's guard). */
	public static void setPlayer(Object accessor, Player player) {
		if (accessor != null) ((FoodDataAccessor) accessor).fruitsdelight$setPlayer(player);
	}
}
