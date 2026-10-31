// Selftest for rp_bench.js without the game (feature_writeup/perf_rpack.md 6): node + mocked Client, server, Block and
// Sodium. Checks the step machine runs every config and scene of a plan and logs well-formed result lines.
//   node tools/perf/rpack/selftest/js_harness.js PLAN.json RESULT.log
// Rhino differences are not covered (the live run is the test for those).
const fs = require('fs')
const [planPath, outPath] = process.argv.slice(2)
const out = []
let tick = 0, blocksSet = 0, cmds = []
const now = { t: 0 }
const RealDate = Date
global.Date = class extends RealDate { constructor(...a) { super(...(a.length ? a : [1e12 + now.t])) } static now() { return 1e12 + now.t } }
const frames = { log: new Array(240).fill(0), end: 0 }
const sodium = { complete: true, instanceNullable() { return this }, isTerrainRenderComplete() { return this.complete },
	getMemoryDebugStrings() { return { forEach: f => ['Device buffer: 12/64 MiB'].forEach(f) } }, getVisibleChunkCount() { return 42 } }
const level = { dimension: 'ResourceKey[minecraft:dimension / minecraft:the_end]', setBlock() { blocksSet++ }, getBlockState() { return { isAir: () => true } } }
const server = { execute(fn) { fn() }, getLevel() { return level }, getAllLevels() { return { forEach(f) { f(level) } } }, runCommandSilent(c) { cmds.push(c); return c.includes('summon minecraft:nope') ? 0 : 1 } }
let selected = [], reloadDone = 0
const repo = { isAvailable: id => !id.includes('missing'), setSelected(l) { selected = Array.from(l) },
	getSelectedIds() { return { forEach: f => selected.forEach(f) } }, getAvailableIds() { return { forEach: f => ['vanilla'].forEach(f) } } }
global.Java = { loadClass(n) {
	if (n.endsWith('BlockPos')) return class { constructor(x, y, z) { this.p = [x, y, z] } }
	if (n.endsWith('.Level')) return { END: 'end' }
	if (n.endsWith('SodiumWorldRenderer')) return sodium
	throw new Error('class ' + n)
} }
global.Block = { getBlock: id => id.includes('unknown') ? { id: 'minecraft:air' } : { id,
		defaultBlockState: () => ({}), getStateDefinition: () => ({ getPossibleStates: () => ({ size: () => 8, get: i => ({ i }) }) }) },
	getId: b => b.id }
global.console = { info: s => out.push(s) }
global.Client = {
	player: { getGameProfile: () => ({ getName: () => 'PerfEval' }) }, level: {}, options: { hideGui: false },
	levelRenderer: { allChanged() { sodium.complete = false; setTimeout_(() => sodium.complete = true, 30) } },
	getSingleplayerServer: () => server, getOverlay: () => null, getResourcePackRepository: () => repo,
	reloadResourcePacks() { let d = tick + 40; return { isDone: () => tick >= d } },
	getFrameTimer: () => ({ getLogEnd: () => frames.end, getLog: () => frames.log })
}
const timers = []
function setTimeout_(f, ticks) { timers.push([tick + ticks, f]) }
let handler = null
global.ClientEvents = { tick: f => handler = f }
eval(fs.readFileSync(__dirname + '/../rp_bench.js', 'utf8').replace(/^var /gm, 'global.'))
global.global.rpbenchPlan = JSON.parse(fs.readFileSync(planPath, 'utf8'))
for (tick = 0; tick < 2e6 && !out.some(l => l.includes('[rpbench] done')); tick++) {
	now.t = tick * 50
	for (let k = 0; k < 6; k++) { frames.log[frames.end] = (4 + (tick % 3)) * 1e6; frames.end = (frames.end + 1) % 240 }
	timers.filter(t => t[0] <= tick).forEach(t => t[1]())
	handler({})
}
fs.writeFileSync(outPath, out.join('\n') + '\n')
process.stderr.write(`ticks ${tick}, lines ${out.length}, setBlock ${blocksSet}, commands ${cmds.length}\n`)
