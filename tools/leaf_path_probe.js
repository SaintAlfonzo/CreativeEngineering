// Scratch-server probe for CEConfig [leaf_decay] maxDistance (feature_writeup/mob_block_tweaks.md section 2.5).
// Copy into a SCRATCH server's kubejs/server_scripts/ (never the pack's), forceload 64 64 143 143, /reload, then
// `ztrees <from> <count>` (310 tree features in 1.0.9; batches of 40 take about 40 s each). For every configured
// feature whose config is a TreeConfiguration it resets a 45 x 45 grass pad at (104, 150, 104), places the tree and
// logs `[ztrees] <i> <id> placed=<bool> {leaves, logs, persist, max, over6, over12, over16, none}`: the longest
// face-connected leaf path to a #minecraft:logs block among the natural leaves, how many are past 6 / 12 / 16, and
// how many have no face path at all (diagonal-only leaves; they decay whatever the range). Paths run through any
// leaves. Templates are covered offline by tools/leaf_path_scan.py.
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

ServerEvents.commandRegistry(event => {
	var { commands: Commands, arguments: Arguments } = event
	event.register(Commands.literal('ztrees')
		.then(Commands.argument('from', Arguments.INTEGER.create(event))
		.then(Commands.argument('count', Arguments.INTEGER.create(event))
		.executes(ctx => {
			try {
				var from = Arguments.INTEGER.getResult(ctx, 'from'), count = Arguments.INTEGER.getResult(ctx, 'count')
				var level = ctx.source.level
				var reg = ctx.source.server.registryAccess().registryOrThrow(ZT_Registry.CONFIGURED_FEATURE_REGISTRY)
				var ids = ztTreeIds(reg)
				var cx = 104, cy = 150, cz = 104
				console.info('[ztrees] total ' + ids.length)
				for (var i = from; i < Math.min(ids.length, from + count); i++) {
					ztClear(level, cx, cy, cz)
					var placed
					try { placed = reg.get(new ZT_RL(ids[i])).place(level, level.getChunkSource().getGenerator(), level.random, new ZT_BlockPos(cx, cy, cz)) } catch (pe) { placed = 'ERR ' + pe }
					console.info('[ztrees] ' + i + ' ' + ids[i] + ' placed=' + placed + ' ' + JSON.stringify(ztMeasure(level, cx, cy - 3, cz)))
				}
				ztClear(level, cx, cy, cz)
				return 1
			} catch (zte) {
				console.info('[ztrees] ERR ' + zte)
				return 0
			}
		}))))
})
