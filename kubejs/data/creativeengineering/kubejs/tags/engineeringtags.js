// requires: creativeengineering
// Creative Engineering; moved from kubejs/server_scripts/tags/engineeringtags.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged.
console.log('engineering tags starting')

ServerEvents.tags('item', e => {
	var t = global.tags
	var i = global.item
    var b = global.blocks

	// Bulb glass / carbon filament
	t.add(e, 'creativeengineering:bulb_glass', [
		i.glass, i.glass_bottle, i.quark_framed_glass,
		i.framed_glass, i.framed_glass_vertical, i.framed_glass_horizontal, i.framed_glass_tiled,
	])
	t.add(e, 'creativeengineering:carbon_filament', [i.paper, i.bamboo])

	// The culture base tag moved to Creative Delight 2026-10-27 (creativeengineering/cultivated_meat_tags.js; reserved for
	// Creative Cultivation, creative_delight.md 29.5)

	// Precision sawblade blueprint templates — the "shape reference" ingredient for the
	// vertical_planks / railing sawblades. No upstream tag exists for either shape, so the
	// blueprint recipes (server_scripts/engineering/blueprints.js, precision sawblades)
	// point at these. Keep in sync with the sawmill outputs generated in generation_scripts/sawmill.js.
	t.add(e, 'creativeengineering:vertical_planks', [
		i.vplanks_oak, i.vplanks_spruce, i.vplanks_birch,
		i.vplanks_jungle, i.vplanks_acacia, i.vplanks_dark_oak,
		i.vplanks_mangrove, i.vplanks_crimson, i.vplanks_warped,
		i.vplanks_ancient, i.vplanks_azalea, i.vplanks_blossom,
		i.vplanks_twisted,
	])
	t.add(e, 'creativeengineering:railings', [
		i.railing_oak, i.railing_spruce, i.railing_birch,
		i.railing_jungle, i.railing_acacia, i.railing_dark_oak,
		i.railing_mangrove, i.railing_crimson, i.railing_warped,
		i.railing_twisted,
		i.railing_ancient, i.railing_azalea, i.railing_blossom,
	])
})

console.log('engineering tags finished')
