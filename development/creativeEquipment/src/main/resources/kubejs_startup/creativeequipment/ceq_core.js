// priority: 1000
// Creative Equipment core: global.CEQ (feature_writeup/creative_equipment.md, creative_split.md 3.9, wave 7). Startup
// scripts in the Equipment jar, loaded by patcherMod's startup loader after the pack's startup_scripts folder, so
// global.NCH, global.tags and the dicts already exist here. This pack also holds the gear items made in KubeJS
// (items/: air brake, echo tools, stepstools, felsic scrap) and the tool and armor tuning (func/: global.tuneTools,
// global.tuneArmor). The server scripts are in kubejs/data/creativeequipment/kubejs/ (_equipment_core.js), the client
// scripts in kubejs/assets/creativeequipment/kubejs/.
//
//   global.CEQ.on('sophisticatedbackpacks')  mod loaded; read it inside an event callback
//   global.CEQ.recipes(() => { ... })        every recipe id the NCH helpers write inside is creativeequipment:<type>/<suffix>
console.info('[creativeequipment] core starting')

global.CEQ = {
	modId: 'creativeequipment',

	on(member) {
		return Platform.isLoaded(member)
	},

	recipes(fn) {
		return global.NCH.withNamespace(global.CEQ.modId, fn)
	},
}

// The config sections that moved from CEConfig at the Equipment carve are CEQConfig's: NCH.cfg finds them by section
// name (end_gear.js reads CRAFTING.FELSIC_<PIECE>).
global.NCH.registerConfig(global.CEQ.modId, 'com.SaintAlfonzo.creativeequipment.CEQConfig')

console.info('[creativeequipment] core finished')
