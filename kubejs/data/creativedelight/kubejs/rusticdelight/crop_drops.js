// requires: creativedelight
// requires: rusticdelight
// Rustic Delight crop drops: per-crop fixes to harvest tables. It runs after every pack loot script (they do not
// touch this table).
//
// Bell peppers (Rustic Delight 1.3.0): the ripe roll was red 6 : green 1 : yellow 1 plus a 15% bonus green and a
// 15% bonus yellow, so red was about 58% of all peppers. The roll is now 1 : 1 : 1 and red gets the same 15% bonus
// pool, so each color averages 0.483 per ripe harvest. Weights and bonus chances are CEConfig
// [crop_drops.bell_peppers] (defaults below if Creative Common is missing); enabled = false leaves the mod's table.
// Wild bell peppers stay red-only (a forage hint; seeds are the real drop). feature_writeup/crops.md section 3,
// TODO farming/crops #9.
//
// Pre-roll, through Creative Loot's drops engine (L.rewrite): the raw table JSON is rewritten before the game parses it. Pools are found by their entries, not by index; existing pools keep their positions
// (Forge names pools by position) and new pools are only appended. modify() resets the table type to
// minecraft:generic, so it is set back to minecraft:block.
//
// Wrapped in an IIFE: all server scripts share one top-level scope.

(function () {

	var TAG = '[crop-drops] '
	var SKIP = '[skip] crop-drops' // a table left as the mod ships it; the engine logs nothing for '[skip]'

	function names(pool) {
		return (pool.entries || []).map(function (e) { return e.name })
	}

	function hasChance(pool) {
		return (pool.conditions || []).some(function (c) { return c.condition === 'minecraft:random_chance' })
	}

	var PEPPER_DEFAULTS = {
		bellPeppers: true,
		redWeight: 1, greenWeight: 1, yellowWeight: 1,
		redBonusChance: 0.15, greenBonusChance: 0.15, yellowBonusChance: 0.15,
	}

	function bellPeppers(pools, i) {
		var cfg = global.NCH.cfg(PEPPER_DEFAULTS, 'CROP_DROPS')
		if (String(cfg.bellPeppers) !== 'true') throw new Error(SKIP)
		var colours = [i.bell_pepper_red, i.bell_pepper_green, i.bell_pepper_yellow]
		var weight = {}, chance = {}
		weight[i.bell_pepper_red] = Number(cfg.redWeight)
		weight[i.bell_pepper_green] = Number(cfg.greenWeight)
		weight[i.bell_pepper_yellow] = Number(cfg.yellowWeight)
		chance[i.bell_pepper_red] = Number(cfg.redBonusChance)
		chance[i.bell_pepper_green] = Number(cfg.greenBonusChance)
		chance[i.bell_pepper_yellow] = Number(cfg.yellowBonusChance)
		var main = pools.filter(function (p) {
			var n = names(p)
			return n.length === colours.length && colours.every(function (c) { return n.indexOf(c) >= 0 })
		})[0]
		var bonus = function (item) {
			return pools.filter(function (p) {
				var n = names(p)
				return n.length === 1 && n[0] === item && hasChance(p)
			})[0]
		}
		if (!main || !bonus(i.bell_pepper_green)) throw new Error('table shape changed, left as is')
		main.entries.forEach(function (e) { e.weight = weight[e.name] })
		if (!bonus(i.bell_pepper_red)) {
			var red = JSON.parse(JSON.stringify(bonus(i.bell_pepper_green)))
			red.entries[0].name = i.bell_pepper_red
			pools.push(red)
		}
		colours.forEach(function (c) {
			bonus(c).conditions.forEach(function (k) {
				if (k.condition === 'minecraft:random_chance') k.chance = chance[c]
			})
		})
		console.info(TAG + 'bell peppers: weights ' + cfg.redWeight + ':' + cfg.greenWeight + ':' + cfg.yellowWeight
			+ ' (red:green:yellow), bonus ' + cfg.redBonusChance + ' / ' + cfg.greenBonusChance + ' / ' + cfg.yellowBonusChance)
	}

	// A table rewrite of Creative Loot's drops engine (loot/09_drops.js, L.rewrite), queued with global.LOOT.rules
	global.LOOT.rules(function (L) {
		if (!global.CD || !global.CD.on('rusticdelight')) return
		var i = global.item
		L.rewrite('rusticdelight:blocks/bell_peppers', function (pools) {
			if (!pools.length) throw new Error(SKIP)
			bellPeppers(pools, i)
			return true
		}, 'crop-drops bell peppers')
	})

})()
