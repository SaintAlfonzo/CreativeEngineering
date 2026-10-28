ServerEvents.tags('item', e => {

console.log('misctagremoval starting')

var i = global.item;
var t = global.tags

// Chestplates that glide but aren't a vanilla ElytraItem — patcherMod's Easy Elytra
// Takeoff mixins read this (EasyElytraTakeoffHelperMixin = rocket-takeoff is allowed at
// all; EasyElytraTakeoffGroundCheckMixin = allowed even mid-fall / on thin blocks).
global.tags.add(e, 'creativeengineering:elytra_chestplates', [i.upgraded_dragon_chestplate])

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

// Hoe harvest area: side of the square Quark's hoe right-click (harvest + replant) and hoe
// area break cover. Read by patcherMod crops.HoeHarvestArea, which also writes the
// "Harvest area: NxN" tooltip. Any size 1x1 to 9x9 works; the largest tag wins. An even
// area reaches one block further toward where the player looks. A hoe in none of these
// keeps Quark's own 3x3 / 5x5 (config/quark-common.toml [tweaks.hoe_harvesting]).
global.tags.add(e, 'creativeengineering:hoe_harvest/1x1', [i.wooden_hoe, i.stone_hoe])
global.tags.add(e, 'creativeengineering:hoe_harvest/2x2', [i.copper_hoe, i.golden_hoe])
global.tags.add(e, 'creativeengineering:hoe_harvest/3x3', [i.iron_hoe, i.hoe_steel])
global.tags.add(e, 'creativeengineering:hoe_harvest/4x4', [i.diamond_hoe, i.netherite_hoe, i.elementium_hoe, i.manasteel_hoe])
global.tags.add(e, 'creativeengineering:hoe_harvest/5x5', [i.echo_hoe, i.felsic_hoe, i.dragon_hoe])

// Add fletching tag to feathers and fletchings
t.add(e, 'forge:fletching', [i.feather, i.arrow_fletching, i.roadrunner_feather])

// Shaft tags: every shaft is in forge:arrow_shaft plus its kinds' forge:arrow_shaft/<kind>
const shaft = (id, kinds) => t.add(e, ['forge:arrow_shaft'].concat(kinds.map(k => 'forge:arrow_shaft/' + k)), id)
//Light
shaft(i.rod_aluminum, ['metal', 'light'])
//Heavy
shaft(i.rod_lead, ['metal', 'heavy'])
shaft(i.rod_uranium, ['metal', 'heavy'])
//Quick
;[i.rod_silver, i.rod_electrum, i.rod_gold, i.rod_rose_gold].forEach(id => shaft(id, ['metal', 'quick']))
//Metal
;[i.rod_copper, i.rod_iron, i.rod_zinc, i.rod_nickel, i.rod_brass, i.rod_constantan, i.rod_steel].forEach(id => shaft(id, ['metal']))
//Blazing
shaft(i.blaze_rod, ['blazing'])
//Wooden (last)
shaft(i.stick, ['wooden'])
shaft(i.stick_treated, ['wooden'])

})

// Camera equipment (#exposure_extras:camera_equipment) - the only items a camera bag accepts
// (exposureBackport src/extras CameraBag.canHold; any slot, normal stack size).
// The mod ships the defaults in
//   development/exposureBackport/src/extras/resources/data/exposure_extras/tags/items/camera_equipment.json:
//   camera, instant camera, camera stand, #exposure:lenses, #exposure:filters, #exposure:film_rolls,
//   #exposure:developed_film_rolls and the 10 instant slides (Polaroid has no slide tag).
// Exposure's own tags are nested references, so lenses / filters / films added to them later fit too.
// Exposure counts every glass pane and the Interplanar Projector as filters, so those fit as well.
// Not in the defaults: flashes (added below), photographs, albums, frames.
// Adjust here: ADD puts items or '#tags' in, REMOVE takes default members out. REMOVE only works on
// direct members: to drop one item that comes in through a nested tag (e.g. one glass pane via
// #exposure:filters), remove the whole nested tag and add back what you want.
// Bags never accept another bag or anything that refuses to go in containers, whatever the tag says.
ServerEvents.tags('item', e => {
	var i = global.item
	global.tags.add(e, i.t_camera_equipment, [
		i.t_exposure_flashes,
	])
	global.tags.remove(e, i.t_camera_equipment, [
		// e.g. i.camera_stand,
	])
})
