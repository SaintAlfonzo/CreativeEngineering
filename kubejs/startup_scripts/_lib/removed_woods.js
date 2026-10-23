// priority: -10002
// ============================================================
// Removed woods: a wood set merged into oak
// ============================================================
// Each set lists purged id -> oak twin, for every block, item and other mod's variant of that wood. Runs after
// item_merges.js and joins its list, so the merge does the rest (recipe inputs, loot, inventories; hidden in JEI,
// tags stripped). server_scripts/wood/removed_woods.js removes every recipe that makes a purged item first, so the
// merge can't turn them into duplicate oak recipes. Design and tests: feature_writeup/recipes_wood.md 2.6.

var i = global.item;

global.removedWoods = {
	// Upgrade Aquatic's river wood (recipes/wood #3). Its tree is the Mulberry tree now: oak trunk, mulberry leaves
	// and vines (kubejs/data/upgrade_aquatic/worldgen/configured_feature/river_tree.json, patcherMod
	// MulberrySapling). Leaves, sapling, leaf pile, carpet and hedge stay.
	river: [
		[i.large_river_boat,                  i.large_oak_boat],
		[i.river_beehive,                     i.beehive],
		[i.river_boards,                      i.oak_boards],
		[i.river_boat,                        i.oak_boat],
		[i.river_bookshelf,                   i.bookshelf],
		[i.river_button,                      i.button_oak],
		[i.river_chest,                       i.chest],
		[i.river_chest_boat,                  i.oak_chest_boat],
		[i.river_door,                        i.door_oak],
		[i.river_fence,                       i.fence_oak],
		[i.river_fence_gate,                  i.fencegate_oak],
		[i.river_furnace_boat,                i.oak_furnace_boat],
		[i.river_ladder,                      i.ladder_oak],
		[i.river_log,                         i.log_oak],
		[i.river_planks,                      i.planks_oak],
		[i.river_post,                        i.oak_post],
		[i.river_pressure_plate,              i.pressureplate_oak],
		[i.river_sign,                        i.sign_oak],
		[i.river_slab,                        i.slab_oak],
		[i.river_stairs,                      i.stairs_oak],
		[i.river_trapdoor,                    i.trapdoor_oak],
		[i.river_trapped_chest,               i.trapped_chest],
		[i.river_vertical_slab,               i.vslab_oak],
		[i.river_wood,                        i.wood_oak],
		[i.stripped_river_log,                i.log_oak_stripped],
		[i.stripped_river_post,               i.stripped_oak_post],
		[i.stripped_river_wood,               i.wood_oak_stripped],
		[i.vertical_river_planks,             i.vplanks_oak],
		[i.river_cabinet,                     i.oak_cabinet],
		[i.short_river_door,                  i.short_oak_door],
		[i.tall_river_door,                   i.tall_oak_door],
		[i.river_bench,                       i.oak_bench],
		[i.river_chair,                       i.oak_chair],
		[i.river_drawer,                      i.oak_drawer],
		[i.river_planter_box,                 i.oak_planter_box],
		[i.river_shelf,                       i.oak_shelf],
		[i.river_shutter,                     i.oak_shutter],
		[i.river_table,                       i.another_furniture_oak_table],
		[i.river_board_slab,                  i.oak_board_slab],
		[i.river_board_stairs,                i.oak_board_stairs],
		[i.river_board_wall,                  i.oak_board_wall],
		[i.everycomp_river_boards,            i.oak_boards],
		[i.river_railing,                     i.railing_oak],
		[i.river_blinds,                      i.oak_blinds],
		[i.river_picture_frame,               i.oak_picture_frame],
		[i.river_trellis,                     i.oak_trellis],
		[i.river_window,                      i.oak_window],
		[i.river_window_pane,                 i.oak_window_pane],
		[i.river_beam,                        i.oak_beam],
		[i.river_palisade,                    i.oak_palisade],
		[i.river_seat,                        i.oak_seat],
		[i.river_support,                     i.oak_support],
		[i.everycomp_river_bench,             i.handcrafted_oak_bench],
		[i.river_board,                       i.oak_board],
		[i.everycomp_river_chair,             i.handcrafted_oak_chair],
		[i.river_corner_trim,                 i.oak_corner_trim],
		[i.river_couch,                       i.oak_couch],
		[i.river_counter,                     i.oak_counter],
		[i.river_cupboard,                    i.oak_cupboard],
		[i.river_desk,                        i.oak_desk],
		[i.river_dining_bench,                i.oak_dining_bench],
		[i.everycomp_river_drawer,            i.handcrafted_oak_drawer],
		[i.river_fancy_bed,                   i.oak_fancy_bed],
		[i.river_nightstand,                  i.oak_nightstand],
		[i.river_pillar_trim,                 i.oak_pillar_trim],
		[i.everycomp_river_shelf,             i.handcrafted_oak_shelf],
		[i.river_side_table,                  i.oak_side_table],
		[i.everycomp_river_table,             i.handcrafted_oak_table],
		[i.auction_stand_river,               i.auction_stand_oak],
		[i.bookshelf_trader_river,            i.bookshelf_trader_oak],
		[i.shelf_2x2_river,                   i.shelf_2x2_oak],
		[i.shelf_river,                       i.shelf_oak],
		[i.hollow_river_log,                  i.log_oak_hollow],
		[i.everycomp_river_bookshelf,         i.bookshelf],
		[i.everycomp_river_chest,             i.oak_chest],
		[i.everycomp_river_ladder,            i.ladder_oak],
		[i.everycomp_river_post,              i.oak_post],
		[i.everycomp_river_trapped_chest,     i.oak_trapped_chest],
		[i.everycomp_stripped_river_post,     i.stripped_oak_post],
		[i.everycomp_vertical_river_planks,   i.vplanks_oak],
		[i.river_full_drawers_1,              i.oak_full_drawers_1],
		[i.river_full_drawers_2,              i.oak_full_drawers_2],
		[i.river_full_drawers_4,              i.oak_full_drawers_4],
		[i.river_half_drawers_1,              i.oak_half_drawers_1],
		[i.river_half_drawers_2,              i.oak_half_drawers_2],
		[i.river_half_drawers_4,              i.oak_half_drawers_4],
		[i.river_trim,                        i.oak_trim],
		[i.tw_upgrade_aquatic_river_table,    i.oak_table],
		[i.upgrade_aquatic_river_bark,        i.bark_oak],
		[i.hanging_sign_river,                i.hanging_sign_oak],
		[i.sign_post_river,                   i.sign_post_oak],
		[i.item_shelf_river,                  i.item_shelf],
	],
};

Object.keys(global.removedWoods).forEach(wood => {
	global.removedWoods[wood].forEach(m => {
		global.itemMerges.push(m);
		global.removedItems.push(m[0]);
	});
});

console.log(`[removedWoods] ${Object.keys(global.removedWoods).length} woods merged into oak`)
