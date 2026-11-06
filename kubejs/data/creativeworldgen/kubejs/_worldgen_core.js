// priority: 1000
// requires: creativeworldgen
// Creative Worldgen server scripts (feature_writeup/creative_split.md 3.9, 7.6, wave 5). Authored here in the pack
// folder (loads on /reload); the Worldgen build copies this tree into the jar at the same path
// (data/creativeworldgen/kubejs/). Folders keep the pack's layout: worldgen/ (structures, biome layout, earth and water
// fill, terrain adaptation, bee nests, wild flowers, mushroom and mob spawns), loot/ (the chest loot framework and its
// loot_config.json / loot_index.json), mobs/ (End divers), other/ (removed mobs), tags/ (worldgen block tags, loot
// groups, entity tags).
// Moved from kubejs/server_scripts/ on 2026-11-02; priorities unchanged, and they order only within this tree.
// The startup half (global.CWG, the config registration) ships in the jar's kubejs_startup/creativeworldgen/.
// Each script pack has its own scope: these scripts read only global.* (tags, item, blocks, removedItems,
// guardThrowable, writeExportJson) and publish global.biomeRules, global.lootTuning and global.LOOT. Readers in
// other packs read them at event time (Creative Delight's wild crops), or queue loot rules with global.CWG.lootRules.
ServerEvents.loaded(e => {
	if (!global.CWG) console.error('[creativeworldgen] global.CWG missing: the Worldgen jar\'s startup scripts did not ' +
		'run (patcherMod without the startup loader, or no creativeworldgen jar in mods/)')
})
