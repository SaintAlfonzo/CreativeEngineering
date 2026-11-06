// priority: 1000
// Creative Calendar core: global.CAL (feature_writeup/creative_calendar.md 4.4). Startup script in the Calendar jar,
// loaded by patcherMod's startup loader after the pack's startup_scripts folder and before Creative Delight's and
// Creative Brewing's packs (both order AFTER creativecalendar), so global.NCH, global.tags and the dicts exist here, and
// CD and CB scripts can read global.CAL. Calendar has no nested members.
//
//   global.CAL.on('sereneseasons')    mod loaded (Platform.isLoaded); the seasons code steers Serene Seasons,
//                                     SeasonHUD, Better Days, Xaero's Minimap and Pretty Rain
//   global.CAL.feature('<id>')        a feature switch in creativecalendar-common.toml: 'seasons', 'crop_seasons',
//                                     'time' (each section's enabled key); read it inside an event callback (the config
//                                     loads after startup scripts)
//   global.CAL.recipes(() => { ... }) every recipe id the NCH helpers write inside is creativecalendar:<type>/<suffix>
//                                     (Calendar has no recipes yet)
console.info('[creativecalendar] core starting')

var $CalendarConfig = Java.loadClass('com.SaintAlfonzo.creativecalendar.CalendarConfig')

global.CAL = {
	modId: 'creativecalendar',

	on(modId) {
		return Platform.isLoaded(modId)
	},

	feature(id) {
		return !!$CalendarConfig.feature(id)
	},

	recipes(fn) {
		return global.NCH.withNamespace(global.CAL.modId, fn)
	},
}

console.info('[creativecalendar] core finished')
