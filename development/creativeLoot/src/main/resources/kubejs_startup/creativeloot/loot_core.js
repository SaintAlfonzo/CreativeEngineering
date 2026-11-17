// priority: 1000
// Creative Loot core: global.LOOT (feature_writeup/creative_loot.md). Startup script in the Creative Loot jar, loaded by
// Creative Common's startup loader after the pack's startup_scripts folder. The server scripts are in
// kubejs/data/creativeloot/kubejs/ (loot/00_lib.js adds the LootJS helpers to this object on every reload).
//
//   global.LOOT.rules(L => { ... })  queue loot rules for the loot engine (loot/05_tuning.js, global.lootTuning).
//                                    For scripts in other packs (Creative Delight's loot.js files): script packs load
//                                    in namespace hash order, so such a pack can load before 05_tuning.js has made the
//                                    current reload's engine. 05_tuning.js runs the queue in highPriorityData, before
//                                    the tag event that captures '#tag' members, and empties it.
console.info('[creativeloot] core starting')

global.LOOT = {
	modId: 'creativeloot',

	_queue: [],

	rules(fn) {
		global.LOOT._queue.push(fn)
	},
}

console.info('[creativeloot] core finished')
