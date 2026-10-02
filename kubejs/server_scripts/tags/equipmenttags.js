ServerEvents.tags('item', e => {

console.log('misctagremoval starting')

var i = global.item;

// Chestplates that glide but aren't a vanilla ElytraItem — patcherMod's Easy Elytra
// Takeoff mixins read this (EasyElytraTakeoffHelperMixin = rocket-takeoff is allowed at
// all; EasyElytraTakeoffGroundCheckMixin = allowed even mid-fall / on thin blocks).
e.add('creativeengineering:elytra_chestplates', 'dragonloot:upgraded_dragon_chestplate')

// Dragon-tier gear (every output of end_gear.js's dragonAssembly, dragonloot's own
// items and our custom ones alike) - drives the passive autorepair in
// server_scripts/equipment/dragon_autorepair.js. Includes upgraded_dragon_chestplate
// too - the elytra-capable variant a player wears INSTEAD of the base chestplate (see
// armor_effects.js's dragonEffects set, which registers the same set bonus for both
// for the same reason). dragon_horse_armor is repaired via dragon_autorepair.js's own
// AbstractHorse walk (worn in the horse's inventory slot, not a player slot/hand).
global.tags.add(e, 'creativeengineering:dragon_gear', [
	i.dragon_helmet, i.dragon_chestplate, i.upgraded_dragon_chestplate, i.dragon_leggings, i.dragon_boots,
	i.dragon_sword, i.dragon_pickaxe, i.dragon_axe, i.dragon_shovel, i.dragon_hoe,
	i.dragon_crossbow, i.dragon_bow, i.dragon_trident, i.dragon_anvil, i.dragon_horse_armor,
	i.ce_dragon_shield, i.air_brake
])

// Items that trigger patcherMod's BonemealCharmMixin ("infinite bonemeal" - see
// [[bonemeal-charm-tool-effect]]) when held in the hand opposite bone meal - a chance to
// refund the bone meal just used, at a chance of durability damage to the charm itself.
// Read by com.SaintAlfonzo.creativeengineering.util.BonemealCharms.TAG; tunable via
// CEConfig.BonemealCharm (chance/damageChance/damagePerUse).
global.tags.add(e, 'creativeengineering:bonemeal_charms', [i.dragon_hoe])

// Add fletching tag to feathers and fletchings
e.add('forge:fletching', i.feather)
e.add('forge:fletching', i.arrow_fletching)
e.add('forge:fletching', i.roadrunner_feather)

// Shaft tags
//Light
e.add('forge:arrow_shaft', i.rod_aluminum)
e.add('forge:arrow_shaft/metal', i.rod_aluminum)
e.add('forge:arrow_shaft/light', i.rod_aluminum)

//Heavy
e.add('forge:arrow_shaft', i.rod_lead)
e.add('forge:arrow_shaft/metal', i.rod_lead)
e.add('forge:arrow_shaft/heavy', i.rod_lead)

e.add('forge:arrow_shaft', i.rod_uranium)
e.add('forge:arrow_shaft/metal', i.rod_uranium)
e.add('forge:arrow_shaft/heavy', i.rod_uranium)

//Quick
e.add('forge:arrow_shaft', i.rod_silver)
e.add('forge:arrow_shaft/metal', i.rod_silver)
e.add('forge:arrow_shaft/quick', i.rod_silver)

e.add('forge:arrow_shaft', i.rod_electrum)
e.add('forge:arrow_shaft/metal', i.rod_electrum)
e.add('forge:arrow_shaft/quick', i.rod_electrum)

e.add('forge:arrow_shaft', i.rod_gold)
e.add('forge:arrow_shaft/metal', i.rod_gold)
e.add('forge:arrow_shaft/quick', i.rod_gold)

e.add('forge:arrow_shaft', i.rod_rose_gold)
e.add('forge:arrow_shaft/metal', i.rod_rose_gold)
e.add('forge:arrow_shaft/quick', i.rod_rose_gold)

//Metal
e.add('forge:arrow_shaft', i.rod_copper)
e.add('forge:arrow_shaft/metal', i.rod_copper)
e.add('forge:arrow_shaft', i.rod_iron)
e.add('forge:arrow_shaft/metal', i.rod_iron)
e.add('forge:arrow_shaft', i.rod_zinc)
e.add('forge:arrow_shaft/metal', i.rod_zinc)
e.add('forge:arrow_shaft', i.rod_nickel)
e.add('forge:arrow_shaft/metal', i.rod_nickel)
e.add('forge:arrow_shaft', i.rod_brass)
e.add('forge:arrow_shaft/metal', i.rod_brass)
e.add('forge:arrow_shaft', i.rod_constantan)
e.add('forge:arrow_shaft/metal', i.rod_constantan)
e.add('forge:arrow_shaft', i.rod_steel)
e.add('forge:arrow_shaft/metal', i.rod_steel)

//Blazing
e.add('forge:arrow_shaft', i.blaze_rod)
e.add('forge:arrow_shaft/blazing', i.blaze_rod)

//Wooden (last)
e.add('forge:arrow_shaft', i.stick)
e.add('forge:arrow_shaft/wooden', i.stick)

e.add('forge:arrow_shaft', i.stick_treated)
e.add('forge:arrow_shaft/wooden', i.stick_treated)

})

// Camera equipment - the only items a camera bag accepts (patcherMod camerabag/CameraBag.canHold;
// any slot, normal stack size). Exposure's own tags go in as nested references, so new
// lenses / filters / films added to them later are picked up here too. Exposure counts every
// glass pane and the Interplanar Projector as filters, so those fit as well.
// Instant slides aren't in #exposure:film_rolls (the Polaroid addon has no tag), hence listed.
// Not included: photographs, albums, frames.
ServerEvents.tags('item', e => {
	var i = global.item
	global.tags.add(e, i.t_camera_equipment, [
		i.camera, i.instant_camera, i.camera_stand,
		i.t_exposure_lenses, i.t_exposure_filters,
		i.t_exposure_film_rolls, i.t_exposure_developed_film_rolls,
		i.instant_color_slide, i.instant_black_and_white_slide,
		i.high_sensitivity_instant_color_slide, i.high_sensitivity_instant_black_and_white_slide,
		i.instant_gameboy_slide, i.instant_nes_slide, i.instant_c64_slide, i.instant_cga_slide,
		i.instant_hires_color_slide, i.instant_hires_black_and_white_slide,
	])
})
