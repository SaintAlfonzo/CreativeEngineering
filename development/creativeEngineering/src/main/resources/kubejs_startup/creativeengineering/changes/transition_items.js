// Creative Engineering; moved from kubejs/startup_scripts/changes/transition_items.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged.
// One incomplete / transition item per sequenced assembly (and per Engineer's-Workbench chain
// that reuses the same "in-progress" sprite).
//
// Add a row here for each sequenced assembly, then reference it in the recipe by passing its key
// wrapped in an array as the transitionalItem argument (the key is independent of the recipe id):
//
//   const inc = R.incomplete('rose_gold_rod')            // id for the sequence steps
//   R.createSequencedAssembly(out, in, [
//       e.recipes.create.deploying(inc, [inc, ...]),
//   ], ['rose_gold_rod'], e, 'any_recipe_id_you_want')
//
// Row: [key, texture?, colorHex?]
//   texture  - OMIT to auto-derive a grey-tinted copy of the output item's sprite
//              (global.item[key] -> '<ns>:item/<path>'). Pass a string when that guess is wrong:
//              block items (-> '<ns>:block/<name>'), mods using 'items/', a stand-in sprite, etc.
//   colorHex - tint applied to layer 0, defaults to '#808080'.
//
// Registered item id is always 'kubejs:incomplete_<key>'.

StartupEvents.registry('item', e => {
	const R = global.NCH
	console.log('transition items starting')

	// TODO
	// Add "const incComponentSteel = R.incomplete('component_steel')" definitions by file to item dict


	// ---- static rows -------------------------------------------------------------------------
	const SEQUENCED_ASSEMBLIES = [
		// key                       texture override (omit = auto from global.item[key])
		['item',                     'vinery:item/mash'],          // generic shared fallback (i.incomplete)
		// food rows (cooking, rolls, large meals, golden food, Rustic, meals): Creative Delight cd_transition_items.js

		// redstone
		['redstone_torch'],
		['daylight_sensor',          'minecraft:block/daylight_detector'],
		['rose_quartz_lamp',         'create:block/rose_quartz_lamp'],
		['transmitter'],
		['redstone_link',            'create:block/redstone_link'],
		['repeater'],
		['fast_repeater'],
		['slow_repeater'],
		['comparator'],
		['redstone_randomizer'],
		['rain_detector'],
		['thunder_detector'],

		// crystal
		['budding_amethyst',         'minecraft:block/budding_amethyst'],
		['echo_shard'],

		// end gear (echo, felsic, dragon) and air brake rows: Creative Equipment's items/end_gear.js since 2026-11-06
		// (TODO `creativebrewing/publish #22`).

		// transport
		['rail'],
		['ll_spring'],

		// metal
		['spool'],
		['chain'],
		['copper_bars',              'minecraft:block/iron_bars'],
		['iron_bars',                'minecraft:block/iron_bars'],
		['cast_iron_bars',           'minecraft:block/iron_bars'],
		['gold_bars',                'minecraft:block/iron_bars'],
		['zinc_bars',                'minecraft:block/iron_bars'],
		['brass_bars',               'minecraft:block/iron_bars'],
		// IE components (server_scripts/engineering/components.js) — IE's item sprite is
		// 'material_component_<m>', not 'component_<m>', so the auto-guess would 404.
		['component_iron',           'immersiveengineering:item/material_component_iron'],
		['component_steel',          'immersiveengineering:item/material_component_steel'],
		['amethyst_lens'],
	]

	// ---- wire coils + coil blocks + pack components ----------------------------------------
	// Keep COIL_METALS in sync with server_scripts/metal/metal_base_materials.js.
	//   coil_<m>       : wire-coil-in-progress  — grey-tint of the wire coil's item sprite, taken
	//                    from i.wire_coil_<m> (the dict has one key per id since 2026-10-27, so
	//                    there is no i.coil_<m> to auto-derive from; the incomplete id stays
	//                    kubejs:incomplete_coil_<m>). creativeengineering:* for the 9 pack coils,
	//                    immersiveengineering:* for copper / steel / electrum.
	//   coil_block_<m> : coil-block-in-progress — the block's own side texture; the coil-block
	//                    item model is a 3D block, so there is no flat item sprite to derive from.
	//                    copper -> IE coil_lv, electrum -> IE coil_mv (placeholder until a
	//                    creativeengineering:coil_electrum block exists); the other 10 (incl.
	//                    steel) -> the pack's own coil block.
	const COIL_METALS = ['copper', 'iron', 'gold', 'zinc', 'aluminum', 'lead', 'nickel', 'silver', 'brass', 'constantan', 'steel', 'electrum']
	const coilSprite = m => String(global.item['wire_coil_' + m]).replace(':', ':item/')
	const coilBlockSide = m =>
		m === 'copper'   ? 'immersiveengineering:block/metal_decoration/coil_lv_side' :
		m === 'electrum' ? 'immersiveengineering:block/metal_decoration/coil_mv_side' :
		                   'creativeengineering:block/coil_block/coil_' + m + '_side'

	COIL_METALS.forEach(m => {
		SEQUENCED_ASSEMBLIES.push(['coil_' + m, coilSprite(m)])
		SEQUENCED_ASSEMBLIES.push(['coil_block_' + m, coilBlockSide(m)])
	})

	// HV wire coil (i.wirecoil_hv = immersiveengineering:wirecoil_steel, the electrical steel
	// coil, distinct from the coil_steel structural rope above) + its paired HV coil block
	// (i.coil_block_hv = immersiveengineering:coil_hv, IE's own block - no flat item sprite).
	SEQUENCED_ASSEMBLIES.push(['wirecoil_hv'])
	SEQUENCED_ASSEMBLIES.push(['coil_block_hv', 'immersiveengineering:block/metal_decoration/coil_hv_side'])

	// The 13 creativeengineering:component_<m> items (startup_scripts/items/components.js).
	// Each has its own creativeengineering:item/component_<m> sprite → auto-derive is correct.
	// (iron + steel are IE items, handled as explicit rows above.)
	const COMPONENT_METALS = ['copper', 'gold', 'diamond', 'zinc', 'netherite', 'aluminum',
		'lead', 'silver', 'nickel', 'uranium', 'brass', 'electrum', 'constantan']
	COMPONENT_METALS.forEach(m => SEQUENCED_ASSEMBLIES.push(['component_' + m]))

	// ---- register --------------------------------------------------------------------------
	SEQUENCED_ASSEMBLIES.forEach(([key, texture, color]) => R.registerIncomplete(e, key, texture, color))

	console.log(`transition items finished (${SEQUENCED_ASSEMBLIES.length})`)
})
