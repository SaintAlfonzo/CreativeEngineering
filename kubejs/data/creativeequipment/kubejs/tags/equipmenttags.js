// Creative Equipment; moved from kubejs/server_scripts/tags/equipmenttags.js on 2026-11-04 (creative_split.md 7.6, wave 7), rows unchanged.
ServerEvents.tags('item', e => {

console.log('misctagremoval starting')

var i = global.item;
var t = global.tags

// Chestplates that glide but aren't a vanilla ElytraItem — patcherMod's Easy Elytra
// Takeoff mixins read this (EasyElytraTakeoffHelperMixin = rocket-takeoff is allowed at
// all; EasyElytraTakeoffGroundCheckMixin = allowed even mid-fall / on thin blocks).
global.tags.add(e, 'creativeengineering:elytra_chestplates', [i.upgraded_dragon_chestplate])

// Armor Trims allowlist (gear/tuning #14, gear_tuning.md Armor Trims): only standard armor drawn on the vanilla armor
// model can be trimmed. patcherMod compat/ArmorTrimsItemCache keeps only these in Armor Trims' trimmable list (smithing
// table, JEI trim pages, the trimmed-item decorator, untrimming); an empty tag means no filter. Out: hats, mob heads,
// diving gear, backtanks, goggles, Immersive Armors, Wardrobe, Botania and other custom models (audit: gear/tuning #16).
var trimPieces = ['helmet', 'chestplate', 'leggings', 'boots']
var trimSets = ['leather', 'chainmail', 'iron', 'golden', 'diamond', 'netherite',  // vanilla
	'copper', 'echo', 'felsic', 'dragon']                                             // Everything Copper, D&D echo, Felsic, Dragon Loot
var trimmable = []
trimSets.forEach(m => trimPieces.forEach(p => trimmable.push(i[m + '_' + p])))
trimmable.push(i.turtle_helmet, i.upgraded_dragon_chestplate,
	i.armor_steel_head, i.armor_steel_chest, i.armor_steel_legs, i.armor_steel_feet,  // Immersive Engineering steel
	i.compressed_iron_helmet, i.compressed_iron_chestplate, i.compressed_iron_leggings, i.compressed_iron_boots)  // PneumaticCraft
t.add(e, i.t_trimmable_armor, trimmable)

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
// Read by com.SaintAlfonzo.creativeequipment.util.BonemealCharms.TAG; tunable via
// CEQConfig.BonemealCharm (chance/damageChance/damagePerUse).
global.tags.add(e, 'creativeengineering:bonemeal_charms', [i.dragon_hoe])

// Hoe harvest area tags: Creative Delight (creativeengineering/crop_tags.js).

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
