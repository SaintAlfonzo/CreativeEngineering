#!/usr/bin/env node
// Offline check of the soil yield script (feature_writeup/crop_yields.md 6, plan_crop_yields_wave2.md 3 / 10).
// Runs kubejs/data/creativedelight/kubejs/creativedelight/soil_yield.js under stubs against the loot dump
// (kubejs/exported/loot_tables, refresh with /dumpall) with the real item and block dicts and every switch on, then:
//   1. every crop row resolves (block, produce, soil ids defined; a base in soil_yield.json),
//   2. each loot base is within 0.5 of the dump's ripe mean (no Fortune, no soil pools) - catches a mod table change,
//   3. no Fortune term is left on any fortune_free table (and melon / hamimelon still have theirs),
//   4. the ripe cucumber and ghost pepper breaks drop seeds,
//   5. prints farmland / Rich / Fertile means per crop (the crop_yields.md 2.1 columns).
// Usage: node tools/soil_yield_check.js   (exit 1 on any FAIL)
'use strict'
const fs = require('fs')
const path = require('path')
const vm = require('vm')

const ROOT = path.resolve(__dirname, '..')
const LIB = path.join(ROOT, 'development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib')
const SCRIPT = path.join(ROOT, 'kubejs/data/creativedelight/kubejs/creativedelight/soil_yield.js')
const TABLE = JSON.parse(fs.readFileSync(path.join(ROOT, 'development/creativeDelight/src/main/resources/creativedelight/soil_yield.json'), 'utf8'))
const DUMP = path.join(ROOT, 'kubejs/exported/loot_tables')

let fails = 0
const fail = (m) => { fails++; console.log('FAIL ' + m) }
const pass = (m) => console.log('PASS ' + m)

// --- dump ---
const dumpCache = {}
function dumpTable(id) {
	const [ns, p] = id.split(':')
	if (!(ns in dumpCache)) {
		const f = path.join(DUMP, ns + '.json')
		dumpCache[ns] = fs.existsSync(f) ? JSON.parse(fs.readFileSync(f, 'utf8')) : null
	}
	const t = dumpCache[ns] && dumpCache[ns][p]
	return t ? JSON.parse(JSON.stringify(t)) : null
}
// The dump was taken after the old script ran: drop its soil pools so the run starts from the mod's tables.
const isSoilPool = (pl) => (pl.conditions || []).some(c => c.condition === 'minecraft:location_check')

// --- sandbox ---
const ctx = { console: { info() {}, warn: (m) => fail('script warning: ' + m), error: (m) => fail('script error: ' + m) } }
ctx.global = ctx
ctx.StartupEvents = new Proxy({}, { get: () => () => {} })
vm.createContext(ctx)
for (const f of ['item_dict.js', 'block_dict.js']) vm.runInContext(fs.readFileSync(path.join(LIB, f), 'utf8'), ctx, { filename: f })
ctx.global.CD = { absentIn: () => false }
ctx.global.NCH = { cfg: (d) => d }
ctx.Platform = { isLoaded: () => true }
ctx.JsonIO = { parseRaw: (s) => ({ __json: s, toString: () => s }) }
const SoilYield = { cropOn: (k) => true }
ctx.Java = { loadClass: (n) => {
	if (n.endsWith('.SoilYieldPicks')) return { lootBase: (k) => (k in TABLE.loot ? TABLE.loot[k] : NaN), fortuneFree: () => TABLE.fortune_free }
	if (n.endsWith('.CDConfig')) return { CROP_DROPS: { SOIL_YIELD: SoilYield } }
	throw new Error('no class ' + n)
} }
let handler = null
ctx.ServerEvents = { genericLootTables: (fn) => { handler = fn } }
vm.runInContext(fs.readFileSync(SCRIPT, 'utf8'), ctx, { filename: 'soil_yield.js' })
if (!handler) { fail('no genericLootTables handler'); process.exit(1) }

const tables = {}
const event = { modify(id, fn) {
	if (!(id in tables)) {
		const t = dumpTable(id)
		if (!t) { if (!TABLE.fortune_free.includes(id)) fail('table not in dump: ' + id); return }
		t.pools = (t.pools || []).filter(pl => !isSoilPool(pl))
		tables[id] = t
	}
	const t = tables[id]
	const b = { type: t.type, pools: { toString: () => JSON.stringify(t.pools) }, functions: { toString: () => JSON.stringify(t.functions || []) } }
	fn(b)
	if (b.pools.__json) t.pools = JSON.parse(b.pools.__json)
	if (b.functions && b.functions.__json) t.functions = JSON.parse(b.functions.__json)
} }
handler(event)

// --- evaluator: mean count of items in `produce` for a block state on a given soil, no tool, Fortune 0 ---
function condOk(c, st) {
	switch (c.condition) {
		case 'minecraft:block_state_property':
			return Object.entries(c.properties || {}).every(([k, v]) => String(st.props[k]) === String(v))
		case 'minecraft:location_check': return (c.predicate.block.blocks || []).includes(st.soilAt[-(c.offsetY || 0)])
		case 'minecraft:match_tool': return false
		case 'minecraft:inverted': return !condOk(c.term, st)
		case 'minecraft:alternative': return (c.terms || []).some(t => condOk(t, st))
		case 'minecraft:survives_explosion': return true
		case 'minecraft:random_chance': return true // weighted below
		default: return true
	}
}
const chance = (conds) => (conds || []).reduce((p, c) => c.condition === 'minecraft:random_chance' ? p * c.chance : p, 1)
const allOk = (conds, st) => (conds || []).every(c => condOk(c, st))
function numMean(v) {
	if (typeof v === 'number') return v
	if (v.type === 'minecraft:uniform') return (numMean(v.min) + numMean(v.max)) / 2
	if (v.type === 'minecraft:binomial') return numMean(v.n) * numMean(v.p)
	if (v.type === 'minecraft:constant' || v.value !== undefined) return numMean(v.value)
	return NaN
}
function countMean(fns, st) {
	let c = 1
	for (const f of fns || []) {
		if (!allOk(f.conditions, st)) continue
		if (f.function === 'minecraft:set_count') c = (f.add ? c : 0) + numMean(f.count)
		else if (f.function === 'minecraft:apply_bonus') { /* Fortune 0 */ if (f.formula === 'minecraft:binomial_with_bonus_count') c += f.parameters.extra * f.parameters.probability }
		else if (f.function === 'minecraft:limit_count') { /* ignored */ }
	}
	return c
}
function entryMean(e, st, produce, poolFns) {
	if (!allOk(e.conditions, st)) return null
	if (e.type === 'minecraft:alternatives') {
		for (const ch of e.children) { const m = entryMean(ch, st, produce, poolFns); if (m !== null) return m }
		return null
	}
	if (e.type !== 'minecraft:item') return 0
	if (!produce.includes(e.name)) return 0
	return countMean([...(e.functions || []), ...(poolFns || [])], st) * chance(e.conditions)
}
function tableMean(t, st, produce) {
	let total = 0
	for (const pl of t.pools || []) {
		if (!allOk(pl.conditions, st)) continue
		const rolls = numMean(pl.rolls) * chance(pl.conditions)
		const ok = pl.entries.map(e => ({ e, w: e.weight || 1 })).filter(x => allOk(x.e.conditions, st) || x.e.type === 'minecraft:alternatives')
		const wsum = ok.reduce((s, x) => s + x.w, 0)
		for (const x of ok) {
			const m = entryMean(x.e, st, produce, pl.functions)
			if (m) total += rolls * (x.w / wsum) * m
		}
	}
	return total
}
const hasFortune = (t) => JSON.stringify(t).includes('minecraft:fortune')

// --- 1 and 2, 5: crop rows ---
const i = ctx.global.item, b = ctx.global.blocks
const soils = { farmland: i.farmland, rich: i.rich_farmland, fertile: i.fertile_soil, richSoul: b.rich_soul_soil, fertileSoul: i.nulch }
// Pull the rows back out of the script source (crops(i, b) is private to the IIFE): re-run with a capture.
const rowsSrc = fs.readFileSync(SCRIPT, 'utf8').match(/function crops\(i, b\) \{[\s\S]*?\n    \}\n/)[0]
const rows = vm.runInContext('(' + rowsSrc.replace('function crops', 'function') + ')', ctx)(i, b)
console.log('\ncrop | key | base | dump mean | farmland / rich / fertile (nether: base / rich soul / fertile soul)')
for (const r of rows) {
	const label = r.key + (r.ripe.half ? '/' + r.ripe.half : '') + (r.ripe.type ? '/' + r.ripe.type : '')
	if (!r.block || r.produce.some(p => !p)) { fail(label + ': undefined block or produce id'); continue }
	const base = TABLE.loot[r.key]
	if (!(base > 0)) { fail(label + ': no loot base'); continue }
	const [ns, id] = r.block.split(':')
	const tid = ns + ':blocks/' + id
	const orig = dumpTable(tid)
	if (!orig) { fail(label + ': ' + tid + ' not in dump'); continue }
	orig.pools = (orig.pools || []).filter(pl => !isSoilPool(pl))
	const below = r.below || 1
	const st = (soil) => ({ props: r.ripe, soilAt: { [below]: soil } })
	const dumpMean = tableMean(orig, st('none'), r.produce)
	if (Math.abs(dumpMean - base) > 0.5) fail(label + ': base ' + base + ' vs dump mean ' + dumpMean.toFixed(2))
	const t = tables[tid]
	const cols = (r.nether ? [null, soils.richSoul, soils.fertileSoul] : [soils.farmland, soils.rich, soils.fertile])
		.map(s => tableMean(t, st(s || 'none'), r.produce).toFixed(2))
	console.log(`${tid} | ${label} | ${base} | ${dumpMean.toFixed(2)} | ${cols.join(' / ')}`)
}

// --- 3: Fortune ---
let left = TABLE.fortune_free.filter(id => tables[id] && hasFortune(tables[id]))
let missing = TABLE.fortune_free.filter(id => !tables[id])
if (left.length) fail('Fortune left on: ' + left.join(', '))
else pass('Fortune stripped on ' + (TABLE.fortune_free.length - missing.length) + ' tables')
if (missing.length) console.log('note: not in dump (newer than it): ' + missing.join(', '))
for (const id of ['minecraft:blocks/melon', 'fruitsdelight:blocks/hamimelon']) {
	if (TABLE.fortune_free.includes(id) || tables[id]) fail(id + ' touched (melons stay as shipped)')
}
// same mean: the stripped table at Fortune 0 equals the original
for (const id of TABLE.fortune_free) {
	const orig = dumpTable(id), t = tables[id]
	if (!orig || !t) continue
	orig.pools = (orig.pools || []).filter(pl => !isSoilPool(pl))
	const pools = t.pools.filter(pl => !isSoilPool(pl) && !pl.__seed)
	const items = new Set(JSON.stringify(orig).match(/"name":"[a-z0-9_]+:[a-z0-9_/]+"/g).map(s => s.slice(8, -1)))
	for (const age of ['0', '1', '2', '3', '4', '5', '6', '7']) {
		const st = { props: { age, half: 'lower', blockstate: age, growth: 'top0', pepper: 'true' }, soilAt: {} }
		for (const it of items) {
			const a = tableMean(orig, st, [it]), c = tableMean({ pools: pools.slice(0, orig.pools.length) }, st, [it])
			if (Math.abs(a - c) > 1e-6) { fail(`${id} ${it} age ${age}: mean ${a} -> ${c} after the strip`); break }
		}
	}
}

// --- 4: ripe seeds ---
for (const [blk, seeds] of [[b.cucumber_crop, i.cucumber_seeds], [b.ghost_pepper_crop, i.ghost_pepper_seeds]]) {
	const [ns, id] = blk.split(':'), t = tables[ns + ':blocks/' + id]
	const m7 = t ? tableMean(t, { props: { age: '7' }, soilAt: {} }, [seeds]) : 0
	const m3 = t ? tableMean(t, { props: { age: '3' }, soilAt: {} }, [seeds]) : 0
	if (Math.abs(m7 - 2.714) > 0.01) fail(`${blk} ripe seeds ${m7.toFixed(3)} (want 2.714)`)
	else pass(`${blk} seeds ripe ${m7.toFixed(2)}, age 3 ${m3.toFixed(2)}`)
}

console.log(fails ? `\n${fails} FAIL` : '\nall PASS')
process.exit(fails ? 1 : 0)
