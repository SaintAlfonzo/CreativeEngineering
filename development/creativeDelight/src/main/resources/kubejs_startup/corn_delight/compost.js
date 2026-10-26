// requires: corn_delight
// Corn Delight hand-written compost rows (the generated ones are compost_values.js; a row here wins and the generator
// skips it). Startup script in the CD jar: an edit needs a CD rebuild and restart.
;(function () {
	var i = global.item
	var C = global.compost

	C.values('other', [
		[i.corncob, 0.3], // the Straw chance (farmersdelight/compost.js), corn_delight.md 2.1
	])
})()
