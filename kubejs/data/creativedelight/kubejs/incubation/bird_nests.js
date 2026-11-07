// requires: creativedelight
// requires: incubation
// Wild duck, goose and emu nests (nests.md 2.4, TODO mobs/nests #3, 2026-11-03): the pack's habitats on top of the
// defaults of Creative Delight's Incubation compartment (its has_feature tags: duck mod river tags, #minecraft:is_savanna
// for emus; creativedelight:has_feature/* since M8, were incubation_additions:has_feature/*).
// Ducks and geese also nest in the swamps and on the beaches of the wetlands spawn rules, emus in every savanna
// biome of the emu spawn rule (Terralith's included). The rule tags are Creative Worldgen's mob_spawns.js
// (#creativeengineering:mob_spawns/<rule id>); the *_ponds rules are left out (they match every grassy land biome).
// Biome modifiers are read at world load: new chunks only.
ServerEvents.tags('worldgen/biome', e => {
	if (!global.CD || !global.CD.on('incubation') || !global.has('creativeworldgen')) return
	var t = global.tags
	if (global.has('untitledduckmod')) {
		t.add(e, 'creativedelight:has_feature/duck_nest', '#creativeengineering:mob_spawns/duck_wetlands')
		t.add(e, 'creativedelight:has_feature/goose_nest', '#creativeengineering:mob_spawns/goose_wetlands')
	}
	if (global.has('alexsmobs'))
		t.add(e, 'creativedelight:has_feature/emu_nest', '#creativeengineering:mob_spawns/am_emu_savanna')
})
