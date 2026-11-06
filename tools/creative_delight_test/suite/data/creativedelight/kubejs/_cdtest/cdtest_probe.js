// priority: -1000
// Creative Delight scratch test suite: the probe (tools/creative_delight_test/README.md; scratch queue SQ-CDT,
// tests CDT- in feature_writeup/creative_delight.md). Never in the pack or a release jar.
//
//   /cdtest <part>   part = startup | ns | mirror | requires | tags | config | all
// Results are "[cdtest] PASS|FAIL <part> <name>: <detail>" lines in logs/latest.log, NOTE lines for record-only
// facts, then "[cdtest] done <part>: N pass, M fail". Same style as /vtdtest.
(() => {
	const $CDConfig = Java.loadClass('com.SaintAlfonzo.creativedelight.CDConfig')
	const MEMBERS = ['oceansdelight', 'oceanic_delight', 'ends_delight']

	let pass = 0, fail = 0, part = ''
	function check(name, ok, detail) {
		if (ok) pass++; else fail++
		console.info('[cdtest] ' + (ok ? 'PASS' : 'FAIL') + ' ' + part + ' ' + name + ': ' + detail)
	}
	function note(name, detail) { console.info('[cdtest] NOTE ' + part + ' ' + name + ': ' + detail) }

	// ---- recipes: id -> recipe, built once per command (never getType() on every recipe)
	let byId = null
	function recipes(server) {
		if (byId) return byId
		byId = {}
		server.getRecipeManager().getRecipes().forEach(r => { byId[String(r.getId())] = r })
		return byId
	}
	function has(server, id) { return recipes(server)[id] != null }
	function resultOf(r) {
		try { let s = r.getResultItem(); return s == null || s.isEmpty() ? '' : String(s.getId()) + ' x' + s.getCount() } catch (e) { return '?' }
	}
	function ingredientsOf(r) {
		let out = []
		try {
			r.getIngredients().forEach(g => {
				let ids = []
				g.getStacks().forEach(s => ids.push(String(s.getId())))
				out.push(ids.sort().join('|'))
			})
		} catch (e) { out.push('?') }
		return out.join(', ')
	}
	function typeOf(r) { try { return String(r.getType()) } catch (e) { return '?' } }

	const parts = {
		startup(server) {
			let s = global.cdtestStartup
			if (!s) { check('suite ran', false, 'global.cdtestStartup missing: the CD jar was not built with -Pcd_test_suite, or the startup loader did not run'); return }
			check('helpers before the CD pack', s.nch === 'object' && s.tags === 'object' && s.compost === 'object',
				'NCH ' + s.nch + ', tags ' + s.tags + ', compost ' + s.compost)
			check('namespace at load', s.nsAtLoad === 'creativeengineering', String(s.nsAtLoad))
			check('dicts before the CD pack', s.itemDict === 'minecraft:wheat', 'global.item.wheat = ' + s.itemDict)
			check('cd_core.js first in its pack (priority)', s.cd === 'object', 'global.CD ' + s.cd)
			check('compost helper callable at startup', s.compostTiers > 0, s.compostTiers + ' tiers')
			check('requires: skips a jar startup file', global.cdtestStartupAbsentRan !== true, String(global.cdtestStartupAbsentRan))
		},
		ns(server) {
			let st = global.cdtestState || {}
			check('ns() inside CD.recipes', st.nsInside === 'creativedelight', String(st.nsInside))
			check('ns() after the block', st.nsAfter === 'creativeengineering', String(st.nsAfter))
			check('ns() restored after an error', st.nsAfterThrow === 'creativeengineering', String(st.nsAfterThrow))
			;[['creativedelight:shapeless/cdtest/own_shapeless', true], ['cdtest_inner:shapeless/cdtest/nested', true],
				['creativedelight:shapeless/cdtest/after_nested', true], ['creativeengineering:shapeless/cdtest_ns_restored', true],
				['creativedelight:shapeless/cdtest_ns_restored', false], ['creativeengineering:shapeless/cdtest/own_shapeless', false],
			].forEach(([id, want]) => check((want ? 'recipe ' : 'no recipe ') + id, has(server, id) === want, has(server, id) ? resultOf(recipes(server)[id]) : 'absent'))
			check('member row follows CD.on()', has(server, 'creativedelight:shapeless/cdtest/oceanic_member_row') === !!st.memberOn,
				'CD.on(oceanic_delight) ' + st.memberOn + ', recipe ' + has(server, 'creativedelight:shapeless/cdtest/oceanic_member_row'))
			check('pack server globals before CD scripts', st.lootAtLoad === 'object' && st.cdAtLoad === 'object',
				'LOOT ' + st.lootAtLoad + ', CD ' + st.cdAtLoad)
		},
		mirror(server) {
			let rows = global.cdtestMirror || []
			check('mirror file loaded', rows.length > 0, rows.length + ' calls')
			let all = recipes(server)
			let cd = Object.keys(all).filter(id => id.startsWith('creativedelight:') && id.indexOf('cdtest') < 0)
			check('twins made', cd.length >= rows.length, cd.length + ' creativedelight recipes for ' + rows.length + ' calls')
			let helpers = {}
			rows.forEach(r => { helpers[r.helper] = true })
			note('helpers covered', Object.keys(helpers).sort().join(', '))
			cd.sort().forEach(id => {
				let twinId = 'creativeengineering:' + id.substring('creativedelight:'.length)
				let a = all[id], b = all[twinId]
				if (!b) { check('twin of ' + id, false, 'no ' + twinId); return }
				let diffs = []
				if (typeOf(a) !== typeOf(b)) diffs.push('type ' + typeOf(a) + ' vs ' + typeOf(b))
				if (resultOf(a) !== resultOf(b)) diffs.push('result ' + resultOf(a) + ' vs ' + resultOf(b))
				if (ingredientsOf(a) !== ingredientsOf(b)) diffs.push('ingredients ' + ingredientsOf(a) + ' vs ' + ingredientsOf(b))
				check('twin of ' + id, diffs.length === 0, diffs.length ? diffs.join('; ') : typeOf(a) + ', ' + (resultOf(a) || 'no item result'))
			})
		},
		requires(server) {
			check('requires: skips an absent mod file', global.cdtestAbsentRan !== true && !has(server, 'creativedelight:shapeless/cdtest/requires_absent'),
				'ran ' + global.cdtestAbsentRan + ', recipe ' + has(server, 'creativedelight:shapeless/cdtest/requires_absent'))
			check('requires: runs a loaded member file', has(server, 'creativedelight:shapeless/cdtest/requires_present'),
				has(server, 'creativedelight:shapeless/cdtest/requires_present') ? 'recipe present' : 'recipe absent')
		},
		tags(server) {
			let stacks = Ingredient.of('#creativedelight:cdtest/members').getStacks()
			let ids = []
			stacks.forEach(s => ids.push(String(s.getId())))
			check('tag helper from a CD script', ids.indexOf('oceanic_delight:fishing_net') >= 0 && ids.indexOf('minecraft:wheat') >= 0, ids.join(', ') || 'empty')
		},
		config(server) {
			let members = global.CD ? global.CD.members() : []
			check('CD.members() = the bundle', members.join(',') === MEMBERS.join(','), members.join(', '))
			MEMBERS.forEach(m => {
				check('member ' + m + ' loaded', Platform.isLoaded(m), String(Platform.isLoaded(m)))
				note('switch ' + m, 'CDConfig ' + $CDConfig.member(m) + ', CD.on ' + (global.CD ? global.CD.on(m) : 'n/a'))
			})
			check('non-member is off', global.CD && global.CD.on('farmersdelight') === false, 'CD.on(farmersdelight) ' + (global.CD ? global.CD.on('farmersdelight') : 'n/a'))
		},
	}

	function run(source, which) {
		pass = 0; fail = 0; byId = null
		let server = source.getServer()
		let names = which === 'all' ? Object.keys(parts) : [which]
		names.forEach(n => {
			part = n
			if (!parts[n]) { check('part', false, 'unknown part ' + n + ' (startup | ns | mirror | requires | tags | config | all)'); return }
			try { parts[n](server) } catch (e) { check('crashed', false, String(e)) }
		})
		console.info('[cdtest] done ' + which + ': ' + pass + ' pass, ' + fail + ' fail')
	}

	ServerEvents.commandRegistry(event => {
		const { commands: Commands, arguments: Arguments } = event
		event.register(Commands.literal('cdtest')
			.executes(ctx => { run(ctx.source, 'all'); return 1 })
			.then(Commands.argument('part', Arguments.GREEDY_STRING.create(event)).executes(ctx => {
				run(ctx.source, String(Arguments.GREEDY_STRING.getResult(ctx, 'part'))); return 1
			})))
	})
})()
