// Scratch-only (resource pack bench, feature_writeup/perf_rpack.md section 4): copied into a throwaway client's
// kubejs/client_scripts/ by rp_bench.py, next to the plan it writes (zz_rpbench_plan.js sets global.rpbenchPlan).
// Never ship in the pack. In single player it walks the plan's configs: selects the config's resource packs, times
// the reload, then for each scene builds a dense grid of that pack's blocks / entities in the End void on the
// integrated server, times a full chunk rebuild (Rubidium) and samples frame times. Results are one JSON object per
// "[rpbench] <kind> {...}" line in logs/kubejs/client.log; rp_report.py reads them with latest.log (atlas sizes).
var RPB_BlockPos = Java.loadClass('net.minecraft.core.BlockPos')
var RPB_Sodium = null
try { RPB_Sodium = Java.loadClass('me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer') } catch (e) { }

var RPB = { started: false, steps: [], wait: 0, cur: null, failed: 0 }

function rpbWall() {
	let d = new Date(), z = n => (n < 10 ? '0' : '') + n
	return `${z(d.getHours())}:${z(d.getMinutes())}:${z(d.getSeconds())}`
}
function rpbLog(kind, obj) { console.info(`[rpbench] ${kind} ${JSON.stringify(obj)}`) }
function rpbServer() { return Client.getSingleplayerServer() }
// runs fn on the server thread; commands and block edits must not run on the render thread
function rpbOnServer(fn) { let s = rpbServer(); s.execute(() => { try { fn(s) } catch (e) { rpbLog('error', { where: 'server', msg: String(e) }) } }) }
// MinecraftServer.getLevel(ResourceKey) is ambiguous in Rhino next to KubeJS's kjs$getLevel(ResourceLocation)
function rpbEnd(s) {
	let end = null
	// Level.dimension() is shadowed by a property in Rhino (KubeJS's getDimension, or the ResourceKey field)
	let seen = []
	s.getAllLevels().forEach(l => {
		let d = String(l.dimension)
		seen.push(d)
		if (d == 'minecraft:the_end' || d.endsWith(' minecraft:the_end]')) end = l
	})
	if (end == null) rpbLog('error', { where: 'rpbEnd', dims: seen })
	return end
}
function rpbCmd(c) { rpbOnServer(s => s.runCommandSilent(c)) }
function rpbTerrainDone() {
	let r = RPB_Sodium ? RPB_Sodium.instanceNullable() : null
	return r == null ? true : r.isTerrainRenderComplete()
}
function rpbSodiumMem() {
	let r = RPB_Sodium ? RPB_Sodium.instanceNullable() : null
	if (r == null) return []
	let out = []
	r.getMemoryDebugStrings().forEach(s => out.push(String(s)))
	return out
}

// Step helpers. Each step is a function called once per client tick until it returns true.
function step(fn) { RPB.steps.push(fn) }
function sleepTicks(n) { let left = n; step(() => --left <= 0) }
function waitUntil(cond, maxTicks, what) {
	let t = 0
	step(() => {
		if (cond()) return true
		if (++t > maxTicks) { rpbLog('timeout', { what: what, ticks: t }); return true }
		return false
	})
}
function once(fn) { step(() => { fn(); return true }) }

function rpbPlayerName() { return String(Client.player.getGameProfile().getName()) }

// ---- scenes ----
function rpbStatesOf(id, perBlock) {
	let b = Block.getBlock(id)
	if (b == null || String(Block.getId(b)) != id) return null
	let all = b.getStateDefinition().getPossibleStates()
	let n = all.size(), out = []
	let k = Math.min(perBlock, n)
	for (let i = 0; i < k; i++) out.push(all.get(k == 1 ? n - 1 : Math.round(i * (n - 1) / (k - 1))))
	return out
}

function rpbClearSite(s, P) {
	let level = rpbEnd(s)
	let air = Block.getBlock('minecraft:air').defaultBlockState()
	for (let y = P.site.y; y <= P.site.y + 2 * P.layers + 2; y++)
		for (let x = 0; x < P.site.size; x++)
			for (let z = 0; z < P.site.size; z++)
				level.setBlock(new RPB_BlockPos(P.site.x + x, y, P.site.z + z), air, 2 | 16)
	s.runCommandSilent(`execute in minecraft:the_end run kill @e[type=!minecraft:player,x=${P.site.x - 4},y=${P.site.y - 4},z=${P.site.z - 4},dx=${P.site.size + 8},dy=${2 * P.layers + 12},dz=${P.site.size + 8}]`)
}

function rpbBuildScene(s, P, name, sc) {
	rpbClearSite(s, P)
	let level = rpbEnd(s)
	let floor = Block.getBlock(P.floor).defaultBlockState()
	for (let x = 0; x < P.site.size; x++)
		for (let z = 0; z < P.site.size; z++)
			level.setBlock(new RPB_BlockPos(P.site.x + x, P.site.y, P.site.z + z), floor, 2 | 16)
	let states = [], missing = []
	;(sc.blocks || []).forEach(id => {
		let st = rpbStatesOf(id, P.statesPerBlock)
		if (st == null) missing.push(id)
		else st.forEach(x => states.push(x))
	})
	let placed = 0
	if (states.length > 0) {
		// every other column and row so each block shows its sides; the list repeats to fill the grid
		let cells = Math.floor(P.site.size / 2) * Math.floor(P.site.size / 2)
		let layers = Math.min(P.layers, Math.max(1, Math.ceil(states.length / cells)))
		let i = 0
		for (let l = 0; l < layers; l++)
			for (let x = 0; x < P.site.size; x += 2)
				for (let z = 0; z < P.site.size; z += 2) {
					level.setBlock(new RPB_BlockPos(P.site.x + x, P.site.y + 1 + 2 * l, P.site.z + z), states[i % states.length], 2 | 16)
					i++
					placed++
				}
	}
	let summoned = 0, badEnt = []
	let ents = sc.entities || []
	ents.forEach((entry, k) => {
		// "ns:id" or "ns:id {nbt}"; custom NBT replaces the frozen default
		let sp = entry.indexOf(' '), id = sp < 0 ? entry : entry.substring(0, sp)
		let nbt = sp < 0 ? '{NoAI:1b,Silent:1b,NoGravity:1b,Invulnerable:1b,PersistenceRequired:1b}' : entry.substring(sp + 1)
		let per = Math.max(1, Math.floor(P.entityCount / ents.length)), ok = 0
		for (let j = 0; j < per; j++) {
			let c = k * per + j, side = Math.floor(P.site.size / 3)
			let x = P.site.x + 1.5 + 3 * (c % side), z = P.site.z + 1.5 + 3 * Math.floor(c / side) % P.site.size
			ok += s.runCommandSilent(`execute in minecraft:the_end run summon ${id} ${x} ${P.site.y + 1} ${z} ${nbt}`) > 0 ? 1 : 0
		}
		if (ok == 0) badEnt.push(id)
		summoned += ok
	})
	rpbLog('built', { scene: name, states: states.length, placed: placed, missing: missing, summoned: summoned, badEntities: badEnt })
}

// ---- measurements ----
function rpbSampleFrames(ticks, done) {
	let ft = Client.getFrameTimer(), last = ft.getLogEnd(), frames = [], t = 0
	step(() => {
		let log = ft.getLog(), end = ft.getLogEnd()
		while (last != end) { frames.push(Number(log[last]) / 1e6); last = (last + 1) % log.length }
		if (++t < ticks) return false
		frames.sort((a, b) => a - b)
		let n = frames.length, sum = frames.reduce((a, b) => a + b, 0)
		let q = p => n ? frames[Math.min(n - 1, Math.floor(p * n))] : 0
		done({ frames: n, mean_ms: n ? sum / n : 0, p50_ms: q(0.5), p95_ms: q(0.95), p99_ms: q(0.99), max_ms: n ? frames[n - 1] : 0 })
		return true
	})
}

function rpbRebuild(P, out) {
	for (let r = 0; r < P.rebuilds; r++) {
		let t0 = 0
		once(() => { t0 = Date.now(); Client.levelRenderer.allChanged() })
		sleepTicks(3)
		waitUntil(rpbTerrainDone, P.settleTicks, 'rebuild')
		once(() => out.push(Date.now() - t0))
	}
}

function rpbScene(P, cfg, name) {
	let sc = P.scenes[name], res = { config: cfg.name, scene: name, rebuild_ms: [] }
	if (sc.vista) {
		once(() => rpbCmd(`execute in ${sc.vista.dim} run tp ${rpbPlayerName()} ${sc.vista.x} ${sc.vista.y} ${sc.vista.z} ${sc.vista.yaw} ${sc.vista.pitch}`))
	} else {
		let built = false
		once(() => rpbOnServer(s => { rpbBuildScene(s, P, name, sc); built = true }))
		waitUntil(() => built, 600, 'build ' + name)
		let cx = P.site.x + P.site.size / 2, cz = P.site.z - P.camBack
		once(() => rpbCmd(`execute in minecraft:the_end run tp ${rpbPlayerName()} ${cx} ${P.site.y + P.camUp} ${cz} 0 ${P.camPitch}`))
	}
	sleepTicks(40)
	waitUntil(rpbTerrainDone, P.settleTicks, 'settle ' + name)
	rpbRebuild(P, res.rebuild_ms)
	sleepTicks(P.warmTicks)
	rpbSampleFrames(P.sampleTicks, f => {
		Object.keys(f).forEach(k => res[k] = f[k])
		res.sodium = rpbSodiumMem()
		res.visible_sections = RPB_Sodium && RPB_Sodium.instanceNullable() ? RPB_Sodium.instanceNullable().getVisibleChunkCount() : -1
		rpbLog('scene', res)
	})
}

function rpbConfig(P, cfg, idx) {
	let reload = { config: cfg.name, index: idx }
	once(() => {
		let repo = Client.getResourcePackRepository()
		let want = cfg.packs.filter(id => repo.isAvailable(id))
		reload.missing = cfg.packs.filter(id => !repo.isAvailable(id))
		repo.setSelected(want)
		reload.selected = []
		repo.getSelectedIds().forEach(id => reload.selected.push(String(id)))
		reload.t0 = Date.now()
		reload.wall0 = rpbWall()
		RPB.cur = Client.reloadResourcePacks()
	})
	waitUntil(() => RPB.cur.isDone(), 20 * 600, 'reload ' + cfg.name)
	once(() => {
		reload.ms = Date.now() - reload.t0
		reload.wall1 = rpbWall()
	})
	waitUntil(() => Client.getOverlay() == null, 20 * 60, 'overlay')
	once(() => rpbLog('reload', reload))
	sleepTicks(P.postReloadTicks)
	cfg.scenes.forEach(n => rpbScene(P, cfg, n))
	once(() => rpbLog('config_end', { config: cfg.name, index: idx }))
}

function rpbStart() {
	let P = global.rpbenchPlan
	if (!P) { rpbLog('error', { msg: 'no global.rpbenchPlan' }); return }
	let name = rpbPlayerName()
	let o = Client.options
	o.hideGui = true // render distance, FOV, vsync and the frame cap come from the copied options.txt
	let avail = []
	Client.getResourcePackRepository().getAvailableIds().forEach(id => avail.push(String(id)))
	rpbLog('start', { configs: P.configs.length, sodium: RPB_Sodium != null, available: avail })
	;[`gamemode spectator ${name}`, 'gamerule randomTickSpeed 0', 'gamerule doMobSpawning false',
		'gamerule doDaylightCycle false', 'gamerule doWeatherCycle false', 'gamerule doFireTick false',
		'gamerule doTileDrops false', 'gamerule mobGriefing false', 'time set 6000', 'weather clear',
		`execute in minecraft:the_end run forceload add ${P.site.x - 16} ${P.site.z - 48} ${P.site.x + P.site.size + 16} ${P.site.z + P.site.size + 16}`
	].forEach(c => rpbCmd(c))
	sleepTicks(100)
	// the site must be void: anything here (End islands, a structure) would be in every scene's frame
	once(() => rpbOnServer(s => {
		let level = rpbEnd(s), n = 0
		for (let x = -32; x < P.site.size + 32; x += 4)
			for (let z = -64; z < P.site.size + 32; z += 4)
				for (let y = 0; y < 256; y += 4)
					if (!level.getBlockState(new RPB_BlockPos(P.site.x + x, y, P.site.z + z)).isAir()) n++
		rpbLog('site', { nonAirSamples: n })
	}))
	P.configs.forEach((cfg, i) => rpbConfig(P, cfg, i))
	once(() => {
		Client.getResourcePackRepository().setSelected(P.configs[0].packs)
		o.hideGui = false
		rpbLog('done', { failed: RPB.failed })
	})
}

ClientEvents.tick(event => {
	if (!RPB.started) {
		if (Client.player == null || Client.level == null || rpbServer() == null || Client.getOverlay() != null) return
		// let the world join finish (JEI, chunk load) before the first step
		if (++RPB.wait < (global.rpbenchPlan ? global.rpbenchPlan.joinTicks : 600)) return
		RPB.started = true
		try { rpbStart() } catch (e) { rpbLog('error', { where: 'start', msg: String(e) }) }
		return
	}
	let budget = 50 // run steps that finish at once in the same tick, but never spin forever
	while (RPB.steps.length > 0 && budget-- > 0) {
		let done
		try { done = RPB.steps[0]() } catch (e) { RPB.failed++; rpbLog('error', { where: 'step', msg: String(e) }); done = true }
		if (!done) break
		RPB.steps.shift()
	}
})
