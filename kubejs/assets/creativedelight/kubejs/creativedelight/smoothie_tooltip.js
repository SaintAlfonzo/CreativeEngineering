// requires: creativedelight
// Mixed Berry Smoothie (TODO sweets #8, feature_writeup/smoothies.md 2.1): its effects are the berries the craft used,
// stored on the item (SmoothieEffects, "effect id|ticks|amplifier" entries joined with ';'; recipe in data
// creativedelight/kubejs/creativedelight/smoothies.js). This lists them under the name, blue, as potion lines do:
// "Shrinking (1:20)", "Strength III (0:10)". A craft whose berries add nothing reads "No effects".

ItemEvents.tooltip(e => {
	if (!global.smoothies) return
	var KEY = global.smoothies.nbtKey
	var roman = ['', ' II', ' III', ' IV', ' V']
	var time = ticks => {
		var s = Math.floor(ticks / 20)
		var sec = s % 60
		return Math.floor(s / 60) + ':' + (sec < 10 ? '0' : '') + sec
	}
	e.addAdvanced(global.item.mixed_berry_smoothie, (item, advanced, text) => {
		var tag = item.getTag()
		if (!tag || !tag.contains(KEY)) return
		var raw = String(tag.getString(KEY))
		var lines = []
		raw.split(';').forEach(part => {
			var v = part.split('|')
			if (v.length !== 3) return
			var id = v[0].split(':')
			var amp = parseInt(v[2])
			lines.push(Text.translate('effect.' + id[0] + '.' + id[1])
				.append((roman[amp] || (' ' + (amp + 1))) + ' (' + time(parseInt(v[1])) + ')').blue())
		})
		if (!lines.length) lines.push(Text.of('No effects').gray())
		for (var n = 0; n < lines.length; n++) text.add(1 + n, lines[n])
	})
})
