// requires: creativecalendar
// priority: 1000
// Creative Calendar server scripts (feature_writeup/creative_calendar.md 4.4). Authored here in the pack folder (loads
// on /reload); the Calendar build copies this tree into the jar at the same path (data/creativecalendar/kubejs/). Ships
// in the jar only. One folder per mod below this one (sereneseasons/: the season type and crop season tags), files
// headed `// requires: <modid>`.
//
// This file only checks that the startup half is there: global.CAL comes from the Calendar jar's startup script.
ServerEvents.loaded(e => {
	if (!global.CAL) {
		console.error('[creativecalendar] global.CAL missing: the Calendar jar\'s startup scripts did not run ' +
			'(patcherMod without the startup loader, or no creativecalendar jar in mods/)')
		return
	}
	console.info('[creativecalendar] server scripts ready, features on: ' +
		['seasons', 'crop_seasons', 'time'].filter(f => global.CAL.feature(f)).join(', '))
})
