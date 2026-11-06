// Creative Engineering; moved from kubejs/server_scripts/wood/wood_types.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged.
ServerEvents.recipes(e => {

console.log('Hello! The wood type event has started!')

var i = global.item
var R = global.NCH

var species = [
	{ name: 'oak', planks: i.planks_oak, slab: i.slab_oak, vslab: i.vslab_oak, vplanks: i.vplanks_oak, stairs: i.stairs_oak, fence: i.fence_oak, fencegate: i.fencegate_oak, door: i.door_oak, trapdoor: i.trapdoor_oak, ladder: i.ladder_oak, button: i.button_oak, pressureplate: i.pressureplate_oak, sign: i.sign_oak, railing: i.railing_oak, sawmill: true },
	{ name: 'spruce', planks: i.planks_spruce, slab: i.slab_spruce, vslab: i.vslab_spruce, vplanks: i.vplanks_spruce, stairs: i.stairs_spruce, fence: i.fence_spruce, fencegate: i.fencegate_spruce, door: i.door_spruce, trapdoor: i.trapdoor_spruce, ladder: i.ladder_spruce, button: i.button_spruce, pressureplate: i.pressureplate_spruce, sign: i.sign_spruce, railing: i.railing_spruce, sawmill: true },
	{ name: 'birch', planks: i.planks_birch, slab: i.slab_birch, vslab: i.vslab_birch, vplanks: i.vplanks_birch, stairs: i.stairs_birch, fence: i.fence_birch, fencegate: i.fencegate_birch, door: i.door_birch, trapdoor: i.trapdoor_birch, ladder: i.ladder_birch, button: i.button_birch, pressureplate: i.pressureplate_birch, sign: i.sign_birch, railing: i.railing_birch, sawmill: true },
	{ name: 'dark_oak', planks: i.planks_dark_oak, slab: i.slab_dark_oak, vslab: i.vslab_dark_oak, vplanks: i.vplanks_dark_oak, stairs: i.stairs_dark_oak, fence: i.fence_dark_oak, fencegate: i.fencegate_dark_oak, door: i.door_dark_oak, trapdoor: i.trapdoor_dark_oak, ladder: i.ladder_dark_oak, button: i.button_dark_oak, pressureplate: i.pressureplate_dark_oak, sign: i.sign_dark_oak, railing: i.railing_dark_oak, sawmill: true },
	{ name: 'jungle', planks: i.planks_jungle, slab: i.slab_jungle, vslab: i.vslab_jungle, vplanks: i.vplanks_jungle, stairs: i.stairs_jungle, fence: i.fence_jungle, fencegate: i.fencegate_jungle, door: i.door_jungle, trapdoor: i.trapdoor_jungle, ladder: i.ladder_jungle, button: i.button_jungle, pressureplate: i.pressureplate_jungle, sign: i.sign_jungle, railing: i.railing_jungle, sawmill: true },
	{ name: 'acacia', planks: i.planks_acacia, slab: i.slab_acacia, vslab: i.vslab_acacia, vplanks: i.vplanks_acacia, stairs: i.stairs_acacia, fence: i.fence_acacia, fencegate: i.fencegate_acacia, door: i.door_acacia, trapdoor: i.trapdoor_acacia, ladder: i.ladder_acacia, button: i.button_acacia, pressureplate: i.pressureplate_acacia, sign: i.sign_acacia, railing: i.railing_acacia, sawmill: true },
	{ name: 'mangrove', planks: i.planks_mangrove, slab: i.slab_mangrove, vslab: i.vslab_mangrove, vplanks: i.vplanks_mangrove, stairs: i.stairs_mangrove, fence: i.fence_mangrove, fencegate: i.fencegate_mangrove, door: i.door_mangrove, trapdoor: i.trapdoor_mangrove, ladder: i.ladder_mangrove, button: i.button_mangrove, pressureplate: i.pressureplate_mangrove, sign: i.sign_mangrove, railing: i.railing_mangrove, sawmill: true },
	{ name: 'crimson', planks: i.planks_crimson, slab: i.slab_crimson, vslab: i.vslab_crimson, vplanks: i.vplanks_crimson, stairs: i.stairs_crimson, fence: i.fence_crimson, fencegate: i.fencegate_crimson, door: i.door_crimson, trapdoor: i.trapdoor_crimson, ladder: i.ladder_crimson, button: i.button_crimson, pressureplate: i.pressureplate_crimson, sign: i.sign_crimson, railing: i.railing_crimson, sawmill: true },
	{ name: 'warped', planks: i.planks_warped, slab: i.slab_warped, vslab: i.vslab_warped, vplanks: i.vplanks_warped, stairs: i.stairs_warped, fence: i.fence_warped, fencegate: i.fencegate_warped, door: i.door_warped, trapdoor: i.trapdoor_warped, ladder: i.ladder_warped, button: i.button_warped, pressureplate: i.pressureplate_warped, sign: i.sign_warped, railing: i.railing_warped, sawmill: true },
	{ name: 'twisted', planks: i.planks_twisted, slab: i.slab_twisted, vslab: i.vslab_twisted, vplanks: i.vplanks_twisted, stairs: i.stairs_twisted, fence: i.fence_twisted, fencegate: i.fencegate_twisted, door: i.door_twisted, trapdoor: i.trapdoor_twisted, ladder: i.ladder_twisted, button: i.button_twisted, pressureplate: i.pressureplate_twisted, sign: i.sign_twisted, railing: i.railing_twisted, sawmill: false },
	{ name: 'ancient', planks: i.planks_ancient, slab: i.slab_ancient, vslab: i.vslab_ancient, vplanks: i.vplanks_ancient, stairs: i.stairs_ancient, fence: i.fence_ancient, fencegate: i.fencegate_ancient, door: i.door_ancient, trapdoor: i.trapdoor_ancient, ladder: i.ladder_ancient, button: i.button_ancient, pressureplate: i.pressureplate_ancient, sign: i.sign_ancient, railing: i.railing_ancient, quarkSawmill: true },
	{ name: 'blossom', planks: i.planks_blossom, slab: i.slab_blossom, vslab: i.vslab_blossom, vplanks: i.vplanks_blossom, stairs: i.stairs_blossom, fence: i.fence_blossom, fencegate: i.fencegate_blossom, door: i.door_blossom, trapdoor: i.trapdoor_blossom, ladder: i.ladder_blossom, button: i.button_blossom, pressureplate: i.pressureplate_blossom, sign: i.sign_blossom, railing: i.railing_blossom, quarkSawmill: true },
	// azalea: removed wood, merged into oak (removed_woods.js, farming/trees #5, 2026-11-02)
]

// forms that output a single item (createSaw(output, input, e))
var singleForms = ['vplanks', 'stairs', 'fence', 'fencegate', 'door', 'trapdoor', 'ladder', 'button', 'pressureplate', 'sign', 'railing']
// forms that output a stack of 2
var doubleForms = ['slab', 'vslab']

singleForms.forEach(form => {
	R.bulk('createSaw', species
		.filter(s => s[form] !== i.na)
		.map(s => [s[form], s.planks, e]))
})
doubleForms.forEach(form => {
	R.bulk('createSaw', species
		.filter(s => s[form] !== i.na)
		.map(s => [Item.of(s[form], 2), s.planks, e]))
})

// ── Remove IE sawmill door/stairs recipes for the vanilla-shaped species ──
R.bulk('remove', species.filter(s => s.sawmill).reduce((acc, s) => acc.concat([
	[{ id: 'immersiveengineering:sawmill/' + s.name + '_door' }, e],
	[{ id: 'immersiveengineering:sawmill/' + s.name + '_stairs' }, e],
]), []))

// ── Remove quarkengineering sawmill recipes for the quark trio ──
// the pack's sawmill JSONs (patcherMod) replace Quark Engineering's slab / log / door / stairs recipes;
// its bookshelf recipes stay
R.bulk('remove', species.filter(s => s.quarkSawmill).reduce((acc, s) => acc.concat([
	[{ id: 'quarkengineering:sawmill/' + s.name + '/' + s.name + '_slab' }, e],
	[{ id: 'quarkengineering:sawmill/' + s.name + '/' + s.name + '_log' }, e],
	[{ id: 'quarkengineering:sawmill/' + s.name + '/' + s.name + '_door' }, e],
	[{ id: 'quarkengineering:sawmill/' + s.name + '/' + s.name + '_stairs' }, e],
]), []))

console.log('The wood type event has finished!')
})
