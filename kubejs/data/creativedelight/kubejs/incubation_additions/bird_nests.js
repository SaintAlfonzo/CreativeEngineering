// requires: incubation_additions
// Wild duck, goose and emu nests (nests.md 2.4, TODO mobs/nests #3, 2026-11-03): the pack's habitats on top of
// Incubation Additions' defaults (the jar's has_feature tags: duck mod river tags, #minecraft:is_savanna for emus).
// Ducks and geese also nest in the swamps and on the beaches of the wetlands spawn rules, emus in every savanna
// biome of the emu spawn rule (Terralith's included). The rule tags are Creative Worldgen's mob_spawns.js
// (#creativeengineering:mob_spawns/<rule id>); the *_ponds rules are left out (they match every grassy land biome).
// Biome modifiers are read at world load: new chunks only.
ServerEvents.tags('worldgen/biome', e => {
	if (!global.CD || !Platform.isLoaded('creativeworldgen')) return
	var t = global.tags
	if (Platform.isLoaded('untitledduckmod')) {
		t.add(e, 'incubation_additions:has_feature/duck_nest', '#creativeengineering:mob_spawns/duck_wetlands')
		t.add(e, 'incubation_additions:has_feature/goose_nest', '#creativeengineering:mob_spawns/goose_wetlands')
	}
	if (Platform.isLoaded('alexsmobs'))
		t.add(e, 'incubation_additions:has_feature/emu_nest', '#creativeengineering:mob_spawns/am_emu_savanna')
})
