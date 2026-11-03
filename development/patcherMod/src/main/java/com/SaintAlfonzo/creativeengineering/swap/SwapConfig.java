package com.SaintAlfonzo.creativeengineering.swap;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Triggers for the item merge world half ({@link ItemMerges}), file {@code config/creativeengineering-swaps.toml}
 * (COMMON; kept out of creativeengineering-common.toml so the swap lists' Common API has its own file). The registry
 * remap ({@link SwapRemapper}) needs no trigger and has no switch here.
 */
public final class SwapConfig {
	private SwapConfig() {}

	public static final ForgeConfigSpec SPEC;
	public static final ForgeConfigSpec.BooleanValue ON_WORLD_LOAD;
	public static final ForgeConfigSpec.BooleanValue ON_PLAYER_DATA_LOAD;
	public static final ForgeConfigSpec.BooleanValue ON_CONTAINER_OPEN;
	public static final ForgeConfigSpec.BooleanValue ON_COMMAND;
	public static final ForgeConfigSpec.BooleanValue ONCE_WHEN_UPDATED;

	static {
		ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
		b.comment(
				"Item merges in the world (swap lists 'merges'; feature_writeup/item_merges.md 6): a purged item that is still",
				"registered becomes its survivor in player inventories, containers and dropped items. Count and NBT are kept;",
				"items beyond the survivor's stack size go to free slots, then to the player or to the ground. Nothing is deleted.",
				"Merges are listed in the jars (data/<modid>/swaps/*.json), not here.")
				.push("swappers");
		ON_WORLD_LOAD = b.comment(
				"Rewrite every chunk's containers each time it loads, and every dropped item and container entity as it",
				"enters the world. The thorough setting; costs a container scan per chunk load.")
				.define("onWorldLoad", false);
		ON_PLAYER_DATA_LOAD = b.comment(
				"Rewrite a player's inventory and ender chest each time they join.")
				.define("onPlayerDataLoad", true);
		ON_CONTAINER_OPEN = b.comment(
				"Rewrite a container's slots when a player opens it.")
				.define("onContainerOpen", true);
		ON_COMMAND = b.comment(
				"Allow /ceswap items (operators): rewrites online players and every loaded chunk and entity now.")
				.define("onCommand", true);
		ONCE_WHEN_UPDATED = b.comment(
				"Rewrite each chunk and each player once per merge list version: a chunk or player saved with an older (or no)",
				"list is rewritten when it next loads and then stamped. Dropped items and container entities are rewritten as",
				"they enter the world while this is on.")
				.define("onceWhenUpdated", true);
		b.pop();
		SPEC = b.build();
	}
}
