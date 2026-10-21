// Scratch-server probe for CEConfig [leaf_decay] timing and the Fast Leaf Decay port (feature_writeup/mob_block_tweaks.md
// section 2.5, MT-19 to MT-23). Copy into a SCRATCH server's kubejs/server_scripts/ (never the pack's), on its own:
// it repeats tools/leaf_path_probe.js's helpers, so don't load both. forceload 64 64 143 143, /reload.
//   zplace <feature id>  reset the pad at (104, 150, 104) and place that tree, log its leaf paths
//   zcount               natural leaves on the pad (a JS scan of ~140k blocks: costs ~250 ms itself)
//   zrt                  call randomTick on every natural leaf on the pad (forceloaded chunks get no random ticks)
//   zchop <flags>        set every #minecraft:logs block on the pad to air with these setBlock flags
//                        (3 = normal, 19 = notify but no shape updates, 18 = no notify, no shape updates)
//   zleaftag             blocks in #minecraft:leaves that are not LeavesBlock (the port only handles LeavesBlock)
// Also logs `[ztick] max gap N ms` for any 2 s window whose longest server tick gap is over 80 ms. For clean timing
// chop with vanilla `/fill ... minecraft:air replace #minecraft:logs` (14-high slabs) and don't run zcount meanwhile.
var ZT_Leaves = Java.loadClass('net.minecraft.world.level.block.LeavesBlock')
var ZT_BlockTags = Java.loadClass('net.minecraft.tags.BlockTags')
var ZT_BlockPos = Java.loadClass('net.minecraft.core.BlockPos')
var ZT_Registry = Java.loadClass('net.minecraft.core.Registry')
var ZT_TreeCfg = Java.loadClass('net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration')
var ZT_Blocks = Java.loadClass('net.minecraft.world.level.block.Blocks')
var ZT_RL = Java.loadClass('net.minecraft.resources.ResourceLocation')

function ztTreeIds(reg) {
	var out = []
	reg.keySet().forEach(rl => { var cf = reg.get(rl); if (cf && cf.config() instanceof ZT_TreeCfg) out.push(String(rl)) })
	out.sort()
	return out
}

function ztMeasure(level, cx, cy, cz) {
	var info = {}, dist = {}, queue = [], key = (a, b, c) => a + ',' + b + ',' + c
	for (var ax = cx - 22; ax <= cx + 22; ax++) for (var az = cz - 22; az <= cz + 22; az++) for (var ay = cy; ay <= cy + 70; ay++) {
		var st = level.getBlockState(new ZT_BlockPos(ax, ay, az))
		if (st.isAir()) continue
		if (st.getBlock() instanceof ZT_Leaves) info[key(ax, ay, az)] = { p: st.getValue(ZT_Leaves.PERSISTENT) == true, x: ax, y: ay, z: az }
		else if (st.getTags().anyMatch(t => t == ZT_BlockTags.LOGS)) info[key(ax, ay, az)] = { log: true, x: ax, y: ay, z: az }
	}
	Object.keys(info).forEach(k => { if (info[k].log) { dist[k] = 0; queue.push(k) } })
	var dirs = [[1, 0, 0], [-1, 0, 0], [0, 1, 0], [0, -1, 0], [0, 0, 1], [0, 0, -1]]
	for (var qi = 0; qi < queue.length; qi++) {
		var cur = info[queue[qi]], cd = dist[queue[qi]]
		dirs.forEach(dd => {
			var nk = key(cur.x + dd[0], cur.y + dd[1], cur.z + dd[2])
			var n = info[nk]
			if (n && !n.log && dist[nk] === undefined) { dist[nk] = cd + 1; queue.push(nk) }
		})
	}
	var r = { leaves: 0, logs: 0, persist: 0, max: 0, over6: 0, over12: 0, over16: 0, none: 0 }
	Object.keys(info).forEach(k => {
		var e = info[k]
		if (e.log) { r.logs++; return }
		if (e.p) { r.persist++; return }
		r.leaves++
		var dv = dist[k]
		if (dv === undefined) { r.none++; return }
		if (dv > r.max) r.max = dv
		if (dv > 6) r.over6++
		if (dv > 12) r.over12++
		if (dv > 16) r.over16++
	})
	return r
}

// Grass at cy - 1, dirt below, air above; flags 2 | 16 so nothing (Fast Leaf Decay included) reacts.
function ztClear(level, cx, cy, cz) {
	var air = ZT_Blocks.AIR.defaultBlockState(), grass = ZT_Blocks.GRASS_BLOCK.defaultBlockState(), dirt = ZT_Blocks.DIRT.defaultBlockState()
	for (var ax = cx - 22; ax <= cx + 22; ax++) for (var az = cz - 22; az <= cz + 22; az++) for (var ay = cy - 3; ay <= cy + 70; ay++) {
		var p = new ZT_BlockPos(ax, ay, az)
		var want = ay == cy - 1 ? grass : (ay < cy - 1 ? dirt : air)
		if (level.getBlockState(p) != want) level.setBlock(p, want, 2 | 16)
	}
}

var ZD_lastTick = 0, ZD_maxGap = 0, ZD_windowStart = 0
ServerEvents.tick(e => {
	var now = Date.now()
	if (ZD_lastTick > 0 && now - ZD_lastTick > ZD_maxGap) ZD_maxGap = now - ZD_lastTick
	ZD_lastTick = now
	if (now - ZD_windowStart > 2000) { if (ZD_maxGap > 80) console.info('[ztick] max gap ' + ZD_maxGap + ' ms'); ZD_maxGap = 0; ZD_windowStart = now }
})
function zdLeaves(level, cb) {
	var n = 0
	for (var ax = 82; ax <= 126; ax++) for (var az = 82; az <= 126; az++) for (var ay = 147; ay <= 220; ay++) {
		var p = new ZT_BlockPos(ax, ay, az), st = level.getBlockState(p)
		if (st.getBlock() instanceof ZT_Leaves && st.getValue(ZT_Leaves.PERSISTENT) != true) { n++; if (cb) cb(st, p) }
	}
	return n
}
ServerEvents.commandRegistry(event => {
	var { commands: Commands, arguments: Arguments } = event
	event.register(Commands.literal('zplace').then(Commands.argument('id', Arguments.GREEDY_STRING.create(event)).executes(ctx => { try {
		var level = ctx.source.level, id = Arguments.GREEDY_STRING.getResult(ctx, 'id')
		var reg = ctx.source.server.registryAccess().registryOrThrow(ZT_Registry.CONFIGURED_FEATURE_REGISTRY)
		ztClear(level, 104, 150, 104)
		var ok = reg.get(new ZT_RL(id)).place(level, level.getChunkSource().getGenerator(), level.random, new ZT_BlockPos(104, 150, 104))
		console.info('[zd] placed ' + id + ' ' + ok + ' ' + JSON.stringify(ztMeasure(level, 104, 147, 104)))
		return 1 } catch (e) { console.info('[zd] ERR ' + e); return 0 } })))
	event.register(Commands.literal('zcount').executes(ctx => { console.info('[zd] count ' + zdLeaves(ctx.source.level)); return 1 }))
	event.register(Commands.literal('zrt').executes(ctx => { try {
		var level = ctx.source.level, before = zdLeaves(level), list = []
		zdLeaves(level, (st, p) => list.push(p))
		list.forEach(p => { var st = level.getBlockState(p); if (st.getBlock() instanceof ZT_Leaves) st.randomTick(level, p, level.random) })
		console.info('[zd] randomTick ' + list.length + ' leaves: ' + before + ' -> ' + zdLeaves(level))
		return 1 } catch (e) { console.info('[zd] ERR ' + e); return 0 } }))
	event.register(Commands.literal('zchop').then(Commands.argument('flags', Arguments.INTEGER.create(event)).executes(ctx => { try {
		var level = ctx.source.level, flags = Arguments.INTEGER.getResult(ctx, 'flags'), n = 0, t0 = Date.now()
		for (var ax = 82; ax <= 126; ax++) for (var az = 82; az <= 126; az++) for (var ay = 150; ay <= 220; ay++) {
			var p = new ZT_BlockPos(ax, ay, az), st = level.getBlockState(p)
			if (st.getTags().anyMatch(t => t == ZT_BlockTags.LOGS)) { level.setBlock(p, ZT_Blocks.AIR.defaultBlockState(), flags); n++ }
		}
		console.info('[zd] chopped ' + n + ' logs with flags ' + flags + ' in ' + (Date.now() - t0) + ' ms')
		return 1 } catch (e) { console.info('[zd] ERR ' + e); return 0 } })))
	event.register(Commands.literal('zleaftag').executes(ctx => {
		var bad = []
		ZT_Registry.BLOCK.forEach(b => { if (b.defaultBlockState().getTags().anyMatch(t => t == ZT_BlockTags.LEAVES) && !(b instanceof ZT_Leaves)) bad.push(String(ZT_Registry.BLOCK.getKey(b))) })
		console.info('[zd] #leaves not LeavesBlock: ' + JSON.stringify(bad)); return 1 }))
})
