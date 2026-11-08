// requires: creativeengineering
// Creative Engineering; moved from kubejs/server_scripts/wood/bark.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged.
ServerEvents.recipes(e => {
console.log('StartingBarkServerScript')

var i = global.item
var R = global.NCH

// Every "normal" (non-nether, non-compat) species: create:cutting recipes to remove,
// plus the log/wood pair for saw + fd-cutting bulk calls below.
var species = [
	{ log: i.log_oak, wood: i.wood_oak, strippedLog: i.log_oak_stripped, strippedWood: i.wood_oak_stripped, bark: i.bark_oak, hollowLog: i.log_oak_hollow, cutLog: 'create:cutting/oak_log', cutWood: 'create:cutting/oak_wood', fdLog: 'farmersdelight:cutting/oak_log', fdWood: 'farmersdelight:cutting/oak_wood' },
	{ log: i.log_spruce, wood: i.wood_spruce, strippedLog: i.log_spruce_stripped, strippedWood: i.wood_spruce_stripped, bark: i.bark_spruce, hollowLog: i.log_spruce_hollow, cutLog: 'create:cutting/spruce_log', cutWood: 'create:cutting/spruce_wood', fdLog: 'farmersdelight:cutting/spruce_log', fdWood: 'farmersdelight:cutting/spruce_wood' },
	{ log: i.log_birch, wood: i.wood_birch, strippedLog: i.log_birch_stripped, strippedWood: i.wood_birch_stripped, bark: i.bark_birch, hollowLog: i.log_birch_hollow, cutLog: 'create:cutting/birch_log', cutWood: 'create:cutting/birch_wood', fdLog: 'farmersdelight:cutting/birch_log', fdWood: 'farmersdelight:cutting/birch_wood' },
	{ log: i.log_dark_oak, wood: i.wood_dark_oak, strippedLog: i.log_dark_oak_stripped, strippedWood: i.wood_dark_oak_stripped, bark: i.bark_dark_oak, hollowLog: i.log_dark_oak_hollow, cutLog: 'create:cutting/dark_oak_log', cutWood: 'create:cutting/dark_oak_wood', fdLog: 'farmersdelight:cutting/dark_oak_log', fdWood: 'farmersdelight:cutting/dark_oak_wood' },
	{ log: i.log_acacia, wood: i.wood_acacia, strippedLog: i.log_acacia_stripped, strippedWood: i.wood_acacia_stripped, bark: i.bark_acacia, hollowLog: i.log_acacia_hollow, cutLog: 'create:cutting/acacia_log', cutWood: 'create:cutting/acacia_wood', fdLog: 'farmersdelight:cutting/acacia_log', fdWood: 'farmersdelight:cutting/acacia_wood' },
	{ log: i.log_jungle, wood: i.wood_jungle, strippedLog: i.log_jungle_stripped, strippedWood: i.wood_jungle_stripped, bark: i.bark_jungle, hollowLog: i.log_jungle_hollow, cutLog: 'create:cutting/jungle_log', cutWood: 'create:cutting/jungle_wood', fdLog: 'farmersdelight:cutting/jungle_log', fdWood: 'farmersdelight:cutting/jungle_wood' },
	{ log: i.log_mangrove, wood: i.wood_mangrove, strippedLog: i.log_mangrove_stripped, strippedWood: i.wood_mangrove_stripped, bark: i.bark_mangrove, hollowLog: i.log_mangrove_hollow, cutLog: 'create:cutting/mangrove_log', cutWood: 'create:cutting/mangrove_wood', fdLog: 'farmersdelight:cutting/mangrove_log', fdWood: 'farmersdelight:cutting/mangrove_wood', reattach: true },
	{ log: i.log_crimson, wood: i.wood_crimson, strippedLog: i.log_crimson_stripped, strippedWood: i.wood_crimson_stripped, bark: i.bark_crimson, hollowLog: i.log_crimson_hollow, cutLog: 'create:cutting/crimson_stem', cutWood: 'create:cutting/crimson_hyphae', fdLog: 'farmersdelight:cutting/crimson_stem', fdWood: 'farmersdelight:cutting/crimson_hyphae' },
	{ log: i.log_warped, wood: i.wood_warped, strippedLog: i.log_warped_stripped, strippedWood: i.wood_warped_stripped, bark: i.bark_warped, hollowLog: i.log_warped_hollow, cutLog: 'create:cutting/warped_stem', cutWood: 'create:cutting/warped_hyphae', fdLog: 'farmersdelight:cutting/warped_stem', fdWood: 'farmersdelight:cutting/warped_hyphae' },
]

// Compat species (architects_palette / quark / upgrade_aquatic): different create:cutting id shape,
// cutting-board removal via quarkdelight (quark trio) or abnormals_delight (driftwood) instead of farmersdelight.
var compatSpecies = [
	{ log: i.log_twisted, wood: i.wood_twisted, strippedLog: i.log_twisted_stripped, strippedWood: i.wood_twisted_stripped, bark: i.bark_twisted, hollowLog: i.log_twisted_hollow, cutLog: 'create:cutting/compat/architects_palette/twisted_log', cutWood: 'create:cutting/compat/architects_palette/twisted_wood', reattach: true },
	{ log: i.log_blossom, wood: i.wood_blossom, strippedLog: i.log_blossom_stripped, strippedWood: i.wood_blossom_stripped, bark: i.bark_blossom, hollowLog: i.log_blossom_hollow, cutLog: 'create:cutting/compat/quark/blossom_log', cutWood: 'create:cutting/compat/quark/blossom_wood', fdLog: 'quarkdelight:cutting/axe/blossom_log', fdWood: 'quarkdelight:cutting/axe/blossom_wood', reattach: true },
	{ log: i.log_ancient, wood: i.wood_ancient, strippedLog: i.log_ancient_stripped, strippedWood: i.wood_ancient_stripped, bark: i.bark_ancient, hollowLog: i.log_ancient_hollow, cutLog: 'create:cutting/compat/quark/ancient_log', cutWood: 'create:cutting/compat/quark/ancient_wood', fdLog: 'quarkdelight:cutting/axe/ancient_log', fdWood: 'quarkdelight:cutting/axe/ancient_wood', reattach: true },
	{ log: i.log_azalea, wood: i.wood_azalea, strippedLog: i.log_azalea_stripped, strippedWood: i.wood_azalea_stripped, bark: i.bark_azalea, hollowLog: i.log_azalea_hollow, cutLog: 'create:cutting/compat/quark/azalea_log', cutWood: 'create:cutting/compat/quark/azalea_wood', fdLog: 'quarkdelight:cutting/axe/azalea_log', fdWood: 'quarkdelight:cutting/axe/azalea_wood', reattach: true },
	// Upgrade Aquatic driftwood: Abnormals Delight's cutting-board recipes (river wood goes with recipes/wood #3)
	{ log: i.log_driftwood, wood: i.wood_driftwood, strippedLog: i.log_driftwood_stripped, strippedWood: i.wood_driftwood_stripped, bark: i.upgrade_aquatic_driftwood_bark, cutLog: 'create:cutting/compat/upgrade_aquatic/driftwood_log', cutWood: 'create:cutting/compat/upgrade_aquatic/driftwood', fdLog: 'abnormals_delight:upgrade_aquatic/cutting/driftwood_log', fdWood: 'abnormals_delight:upgrade_aquatic/cutting/driftwood' },
]

var all = species.concat(compatSpecies)

// ── Removing existing create log/wood cutting, replace with addition of bark ──
R.bulk('remove', all.map(s => [{ id: s.cutLog }, e]))
R.bulk('remove', all.map(s => [{ id: s.cutWood }, e]))

R.bulk('createSaw', all.map(s => [
	[s.strippedLog, Item.of(s.bark).withChance(0.5)], s.log, e
]))
R.bulk('createSaw', all.map(s => [
	[s.strippedWood, Item.of(s.bark).withChance(0.75)], s.wood, e
]))
// Bamboo: block-only, no separate "wood" form
R.createSaw([i.log_bamboo_stripped, Item.of(i.bark_bamboo).withChance(0.50)], i.log_bamboo, e)

// ── Shapeless recipes to reattach bark to logs (only species that had this originally) ──
var reattachSpecies = all.filter(s => s.reattach)
R.bulk('shapeless', reattachSpecies.map(s => [Item.of(s.log, 1), [s.bark, s.strippedLog], e]))
R.bulk('shapeless', reattachSpecies.map(s => [Item.of(s.wood, 1), [s.bark, s.strippedWood], e]))
R.bulk('shapeless', reattachSpecies.map(s => [Item.of(s.wood, 1), [s.bark, s.log], e, s.log.replace(/^.*:/, '') + '_log_reattatch']))
R.shapeless(Item.of(i.log_bamboo, 1), [i.bark_bamboo, i.log_bamboo_stripped], e)

// ── Remove farmers delight log/wood cutting, replace with addition of specific bark ──
var fdSpecies = species // quark trio use quarkdelight ids instead, handled separately below
R.bulk('remove', fdSpecies.map(s => [{ id: s.fdLog }, e]))
R.bulk('remove', fdSpecies.map(s => [{ id: s.fdWood }, e]))
R.bulk('fdCutting', fdSpecies.map(s => [s.log, R.AXE, [s.strippedLog, Item.of(s.bark).withChance(0.50)], e]))
R.bulk('fdCutting', fdSpecies.map(s => [s.wood, R.AXE, [s.strippedWood, Item.of(s.bark).withChance(0.75)], e]))

// Twisted uses farmersdelight-style fdCutting too, but has no matching removal id in the original
// (kept as-is; note the original also had a bug here, see caveat below)
R.fdCutting(i.log_twisted, R.AXE, [i.log_twisted_stripped, Item.of(i.bark_twisted).withChance(0.50)], e)
R.fdCutting(i.wood_twisted, R.AXE, [i.wood_twisted_stripped, Item.of(i.bark_twisted).withChance(0.75)], e)

// Quark trio + driftwood: quarkdelight / abnormals_delight fdCutting removal/add
var compatFd = compatSpecies.filter(s => s.fdLog)
R.bulk('remove', compatFd.map(s => [{ id: s.fdLog }, e]))
R.bulk('remove', compatFd.map(s => [{ id: s.fdWood }, e]))
R.bulk('fdCutting', compatFd.map(s => [s.log, R.AXE, [s.strippedLog, Item.of(s.bark).withChance(0.50)], e]))
R.bulk('fdCutting', compatFd.map(s => [s.wood, R.AXE, [s.strippedWood, Item.of(s.bark).withChance(0.75)], e]))

// ── Remove quark hollow log cutting, replace with addition of specific bark ──
R.remove({ id: 'quarkdelight:cutting/axe/hollow_logs' }, e)

var hollowSpecies = [
	i.log_oak_hollow, i.log_spruce_hollow, i.log_birch_hollow, i.log_dark_oak_hollow,
	i.log_jungle_hollow, i.log_acacia_hollow, i.log_mangrove_hollow, i.log_crimson_hollow,
	i.log_warped_hollow, i.log_twisted_hollow, i.log_ancient_hollow, i.log_blossom_hollow, i.log_azalea_hollow,
]
var hollowBark = [
	i.bark_oak, i.bark_spruce, i.bark_birch, i.bark_dark_oak,
	i.bark_jungle, i.bark_acacia, i.bark_mangrove, i.bark_crimson,
	i.bark_warped, i.bark_twisted, i.bark_ancient, i.bark_blossom, i.bark_azalea,
]
R.bulk('fdCutting', hollowSpecies.map((log, idx) => [log, R.AXE, hollowBark[idx], e]))

// ── Remove IE sawmill recipes, add own in CreativeEngineering jar file ──
R.bulk('remove', [
	'oak_log', 'spruce_log', 'birch_log', 'dark_oak_log', 'jungle_log',
	'acacia_log', 'mangrove_log', 'crimson_log', 'warped_log',
	'stripped_crimson_log', 'stripped_warped_log',
].map(name => [{ id: 'immersiveengineering:sawmill/' + name }, e]))

// ── FD's tree bark: its paper recipe removed; every cutting-board stripping recipe now gives Immersive
// Weathering's species bark (FD's, Quark Delight's and Abnormals Delight's replaced above) ──
R.remove({ id: 'farmersdelight:paper_from_tree_bark' }, e)

// ── Leather from fermenting bark (TODO recipes/wood #4): any Immersive Weathering bark, 4 + salt in the Pickling Jar ──
R.vdPickling(Item.of(i.leather, 1), [i.any_bark, i.any_bark, i.any_bark, i.any_bark, i.salt], e, 'leather_from_bark')

console.log('BarkServerScriptComplete')
})
