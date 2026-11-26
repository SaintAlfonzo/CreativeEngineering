// SPDX-License-Identifier: GPL-3.0-only
// Port of Create: Oxidized 1.0.0 (Mr.Mulle, MIT; NOTICE-upstream-MIT.txt).
package com.SaintAlfonzo.creativeoxidation;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The block ids that carry the stage and waxed properties (plan_creative_oxidation.md 2.1 and 2.2: 32 Create and Create
 * Deco blocks with upstream art, 13 Design n' Decor blocks), and the B1 fix: upstream added the properties to every
 * block of 48 classes (all iron bars, doors, panes, casings, windows, pillars). Here a mixin on Registrate's
 * BlockBuilder.createEntry names the block being built ({@link #beginBuild}), and StateDefinition.Builder.create adds
 * the properties only when that pending id is a target ({@link #takeForStateDefinition}). Create, Create Deco and
 * Design n' Decor all register their blocks through Registrate.
 *
 * No Minecraft types here, so tools/test_creative_oxidation.sh can check the decision without a game.
 */
public final class OxidationTargets {
	/** Creative tab and JEI order. */
	public static final List<String> ORDERED_BLOCK_IDS = List.of(
		"create:copper_casing",
		"create:fluid_pipe",
		"create:encased_fluid_pipe",
		"create:glass_fluid_pipe",
		"create:mechanical_pump",
		"create:smart_fluid_pipe",
		"create:fluid_valve",
		"create:copper_valve_handle",
		"create:fluid_tank",
		"create:hose_pulley",
		"create:item_drain",
		"create:spout",
		"create:portable_fluid_interface",
		"create:steam_engine",
		"create:copper_ladder",
		"create:copper_bars",
		"create:copper_scaffolding",
		"create:copper_door",
		"createdeco:blue_copper_lamp",
		"createdeco:copper_bars",
		"createdeco:copper_bars_overlay",
		"createdeco:copper_catwalk",
		"createdeco:copper_catwalk_stairs",
		// upstream's createdeco:copper_coinstack (Create Deco 2.x block id); in 1.3.3 copper_coinstack is the item
		"createdeco:copper_coinstack_block",
		"createdeco:copper_hull",
		"createdeco:copper_mesh_fence",
		"createdeco:copper_sheet_metal",
		"createdeco:copper_support",
		"createdeco:green_copper_lamp",
		"createdeco:locked_copper_door",
		"createdeco:red_copper_lamp",
		"createdeco:yellow_copper_lamp",
		"design_decor:copper_boiler",
		"design_decor:copper_boiler_large",
		"design_decor:copper_boiler_structure",
		"design_decor:copper_bolt",
		"design_decor:copper_catwalk",
		"design_decor:copper_floodlight",
		"design_decor:copper_floor",
		"design_decor:copper_gas_tank",
		"design_decor:copper_lamp",
		"design_decor:copper_large_chain",
		"design_decor:copper_light",
		"design_decor:copper_railing",
		"design_decor:copper_screw"
	);
	public static final Set<String> BLOCK_IDS = Set.copyOf(ORDERED_BLOCK_IDS);

	/**
	 * Target blocks whose item is not a BlockItem of the block: item id -> block id. Create Deco 1.3.3's coin stack block
	 * drops and is placed by the plain item createdeco:copper_coinstack (CoinStackItem).
	 */
	public static final Map<String, String> PLACER_ITEMS = Map.of("createdeco:copper_coinstack", "createdeco:copper_coinstack_block");

	/** The id of the block Registrate is constructing on this thread, or null. */
	private static final ThreadLocal<String> PENDING = new ThreadLocal<>();

	private OxidationTargets() {
	}

	public static boolean isTarget(String id) {
		return id != null && BLOCK_IDS.contains(id);
	}

	public static boolean isTarget(String namespace, String path) {
		return isTarget(namespace + ":" + path);
	}

	/**
	 * An item that carries a target's stage (drops, P3): a BlockItem of a target block ({@code blockItemBlockId}, null
	 * for other items), or one of {@link #PLACER_ITEMS}. A glass or encased fluid pipe drops the plain fluid pipe item,
	 * which takes the stage too.
	 */
	public static boolean isTargetItem(String itemId, String blockItemBlockId) {
		return isTarget(blockItemBlockId) || PLACER_ITEMS.containsKey(itemId);
	}

	/** The target block a non-BlockItem places ({@link #PLACER_ITEMS}), or null. */
	public static String placedBlockOf(String itemId) {
		return itemId == null ? null : PLACER_ITEMS.get(itemId);
	}

	/** Registrate is about to construct the block {@code modid:name} on this thread. */
	public static void beginBuild(String id) {
		PENDING.set(id);
	}

	/** Registrate's factory call returned (or threw). */
	public static void endBuild() {
		PENDING.remove();
	}

	/**
	 * Asked by StateDefinition.Builder.create: true when the owner is a block and the block Registrate is building on
	 * this thread is a target. The first block state definition built in that window is the block's own (Block's
	 * constructor builds it), so the pending id is used up there: a second block made inside the same factory call
	 * never gets the properties.
	 */
	public static boolean takeForStateDefinition(boolean ownerIsBlock) {
		if (!ownerIsBlock) return false;
		String id = PENDING.get();
		if (id == null) return false;
		PENDING.remove();
		return isTarget(id);
	}
}
