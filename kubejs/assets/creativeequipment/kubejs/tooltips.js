// Creative Equipment: gear tooltips (stepstools, felsic, elytra, air brake, dragon gear); moved from
// kubejs/client_scripts/tooltips.js on 2026-11-04, rows unchanged. The helpers and the colour palette stay in the pack's
// tooltips.js and are read here through global.tooltips at event time (each script pack has its own scope).
ItemEvents.tooltip(e => {
	var T = global.tooltips
	if (!T) { console.error('[creativeequipment] global.tooltips missing: the pack\'s client_scripts/tooltips.js did not run'); return }
	var addTooltips = T.addTooltips
	var col = T.col
	var i = global.item

	//Stepstools (stepstool/; feature_writeup/stepstool.md). The slot comes from Curios' "Slot: Feet" line.
	;['white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray',
		'light_gray', 'cyan', 'purple', 'blue', 'brown', 'green', 'red', 'black'].forEach(c =>
		addTooltips(e, [i[c + '_stepstool'],
			['Stand still and hold Sprint + Jump to step up', col.norm], ['Move, crouch or jump to step off', col.norm]]))

// ############################## End Gear ##############################

	//removeTooltips(e, i.echo_helmet, i.echo_chestplate, i.echo_leggings, i.echo_boots);

	addTooltips(e,
		[i.felsic_helmet, ['Capable of binding to dragon scale', col.epic]],
		[i.felsic_chestplate, ['Capable of binding to dragon scale', col.epic]],
		[i.felsic_leggings, ['Capable of binding to dragon scale', col.epic]],
		[i.felsic_boots, ['Capable of binding to dragon scale', col.epic]],
		[i.felsic_sword, ['Capable of binding to dragon scale', col.epic]],
		[i.felsic_pickaxe, ['Capable of binding to dragon scale', col.epic]],
		[i.felsic_axe, ['Capable of binding to dragon scale', col.epic]],
		[i.felsic_shovel, ['Capable of binding to dragon scale', col.epic]],
		[i.felsic_hoe, ['Capable of binding to dragon scale', col.epic]],
		[i.elytra, ['Craft with dyes or a banner to customize', col.epic], ['Wings can be split, customized individually, and recombined', col.epic],],
		[i.custom_elytra, ['Craft with dyes or a banner to customize', col.epic], ['Wings can be split, customized individually, and recombined', col.epic],],
		[i.elytra_wing, ['Craft with dyes or a banner to customize', col.epic], ['Craft with another wing to recombine', col.epic],],
		[i.air_brake, ['Slows horizontal and upward elytra flight, causes stalling at low speeds', col.epic]],
		[i.dragon_anvil, ['Unbreakable!', col.epic]],
		[i.dragon_sword, ['Weaken and bleed your target', col.epic], ['Steals life-force from kills', col.epic]],
		[i.dragon_axe, ['Levitate and bleed your target', col.epic], ['Jump slightly higher while held', col.epic]],
		[i.dragon_shovel, ['Discombobulate your opponent', col.epic], ['Reduces knockback when held', col.epic]],
		[i.dragon_trident, ['Inflict mining fatigue on hit', col.epic], ['Fall slower when held', col.epic]],
		[i.dragon_hoe, ['Chance to refund bone meal held in the other hand', col.epic]],
	);
})
