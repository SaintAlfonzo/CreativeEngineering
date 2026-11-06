// requires: creativeworldgen
// Mobs taken out of the pack that have no spawn rule to switch off. Each one is refused when it joins a level (spawn
// egg, summon, a mod's own code, or one already saved in a chunk, which is dropped when the chunk loads).
//   upgrade_aquatic:flare - only an Insomnia potion on a Phantom made one; Insomnia and Restfulness are removed
//   (potions/effects #9, CB startup creativeengineering/potions.js). Its spawn egg is in global.removedItems.
// /reload applies.

const REMOVED_MOBS = ['upgrade_aquatic:flare']

REMOVED_MOBS.forEach(id => {
	EntityEvents.spawned(id, event => {
		event.cancel()
	})
})
