// requires: creativeworldgen
// End illagers wear Create copper diving gear (TODO mobs/pillagers #2): helmet, backtank and boots, as if they came
// through the void in suits. Their only source is the Mysticrift End tower's spawn box (spawn_boxes.json, box
// end_tower), but the script dresses every illager and witch in the End, whatever spawned it.
// - Once per mob: a persistent-data flag. EntityEvents.spawned also fires for mobs loaded from disk, so mobs that
//   were already in the End get the gear the next time their chunk loads.
// - A head slot that already holds something (a raid captain's banner) is kept.
// - Drop chance 0 for the three pieces: box mobs respawn forever, a free backtank per kill would skip Create's
//   progression.
// - Client side: vanilla illager and witch renderers have no armour layer, so the gear does not show on the model
//   (feature_writeup/mob_spawns.md). It still counts as armour.
// /reload applies.

;(function () {
	const $EndDiverSlot = Java.loadClass('net.minecraft.world.entity.EquipmentSlot')
	const END_DIVER_TYPES = ['minecraft:pillager', 'minecraft:vindicator', 'minecraft:evoker', 'minecraft:illusioner', 'minecraft:witch']
	const END_DIVER_FLAG = 'creativeengineering_end_diver'

	END_DIVER_TYPES.forEach(type => {
		EntityEvents.spawned(type, event => {
			const mob = event.entity
			if (String(event.level.getDimension()) != 'minecraft:the_end') return
			const data = mob.getPersistentData()
			if (data.getBoolean(END_DIVER_FLAG)) return
			data.putBoolean(END_DIVER_FLAG, true)
			const it = global.item
			if (mob.getItemBySlot($EndDiverSlot.HEAD).isEmpty()) {
				mob.setItemSlot($EndDiverSlot.HEAD, Item.of(it.copper_diving_helmet))
				mob.setDropChance($EndDiverSlot.HEAD, 0)
			}
			mob.setItemSlot($EndDiverSlot.CHEST, Item.of(it.copper_backtank))
			mob.setDropChance($EndDiverSlot.CHEST, 0)
			mob.setItemSlot($EndDiverSlot.FEET, Item.of(it.copper_diving_boots))
			mob.setDropChance($EndDiverSlot.FEET, 0)
		})
	})
})()
