// Scratch-server probe: Immersive Weathering leaf piles with the pack's leaf decay (feature_writeup/mob_block_tweaks.md
// section 2.5, MT-24; queue entries SQ-LP in feature_writeup/testing/scratch_queue.md). Copy into a SCRATCH server's
// kubejs/server_scripts/ as zz_lp_probe.js (never the pack's). Prefix ZLP_ (all server scripts share one scope).
// Rows sit at x 300-338, z 300; run `forceload add 288 288 351 351` first (scheduled ticks need a loaded chunk).
//   zlp tick   20 lone natural leaves (oak, birch, spruce, two apple leaves) on grass, a scheduled tick queued on each:
//              the pack's decay path (LeafDecayRange.afterTick -> LeafDecayPiles). Result 10 ticks later.
//   zlp rt     the same 20 leaves, randomTick called on each now: vanilla's path, where IW's own LeavesMixin hook sits.
//   zlp tree   minecraft:oak on a clear pad at (330, 0, 330), logs set to air (flags 3), result after 300 ticks.
// Every result is one `[zlp] ...` line in logs/latest.log, with the live config it ran under.
var ZLP_Leaves = Java.loadClass('net.minecraft.world.level.block.LeavesBlock')
var ZLP_BlockPos = Java.loadClass('net.minecraft.core.BlockPos')
var ZLP_Blocks = Java.loadClass('net.minecraft.world.level.block.Blocks')
var ZLP_Registry = Java.loadClass('net.minecraft.core.Registry')
var ZLP_RL = Java.loadClass('net.minecraft.resources.ResourceLocation')
var ZLP_BlockTags = Java.loadClass('net.minecraft.tags.BlockTags')
var ZLP_Pile = Java.loadClass('com.ordana.immersive_weathering.blocks.LeafPileBlock')

var ZLP_TYPES = ['minecraft:oak_leaves', 'minecraft:birch_leaves', 'minecraft:spruce_leaves',
	'fruitsdelight:apple_leaves', 'vinery:apple_leaves']
var ZLP_Y = 0
var ZLP_pending = null

function zlpCfg() {
	var out = {}
	try {
		var ce = Java.loadClass('com.SaintAlfonzo.creativeengineering.CEConfig').LEAF_DECAY
		out.enabled = ce.enabled.get(); out.onlyWhenDisturbed = ce.onlyWhenDisturbed.get(); out.leafPiles = ce.leafPiles.get()
	} catch (e) { out.ceErr = String(e) }
	try {
		out.iwChance = Java.loadClass('com.ordana.immersive_weathering.configs.CommonConfigs').LEAF_PILES_FROM_DECAY_CHANCE.get()
	} catch (e) { out.iwErr = String(e) }
	return JSON.stringify(out)
}

// Stone at y-2, grass at y-1, air from y up; flags 2 | 16: no neighbour or shape updates, nothing queues a tick.
function zlpFloor(level, x0, x1, z0, z1, y, top) {
	var air = ZLP_Blocks.AIR.defaultBlockState()
	for (var ax = x0; ax <= x1; ax++) for (var az = z0; az <= z1; az++) for (var ay = y - 2; ay <= y + top; ay++) {
		var want = ay == y - 2 ? ZLP_Blocks.STONE.defaultBlockState() : (ay == y - 1 ? ZLP_Blocks.GRASS_BLOCK.defaultBlockState() : air)
		level.setBlock(new ZLP_BlockPos(ax, ay, az), want, 2 | 16)
	}
}

// 20 lone leaves 2 blocks apart, default state (distance 7, not persistent): no log anywhere near, so each decays when checked.
function zlpRow(level) {
	zlpFloor(level, 299, 339, 299, 301, ZLP_Y, 3)
	var list = []
	for (var i = 0; i < 20; i++) {
		var id = ZLP_TYPES[i % ZLP_TYPES.length], b = ZLP_Registry.BLOCK.get(new ZLP_RL(id))
		if (!(b instanceof ZLP_Leaves)) { console.info('[zlp] ERROR ' + id + ' is not a LeavesBlock'); continue }
		var p = new ZLP_BlockPos(300 + 2 * i, ZLP_Y, 300)
		level.setBlock(p, b.defaultBlockState(), 2 | 16)
		list.push({ p: p, id: id })
	}
	return list
}

function zlpRowResult(level, label, list) {
	var leaves = 0, piles = 0, layers = 0, byType = {}
	list.forEach(e => {
		var st = level.getBlockState(e.p)
		if (st.getBlock() instanceof ZLP_Leaves) leaves++
		else if (st.getBlock() instanceof ZLP_Pile) {
			piles++; layers += st.getValue(ZLP_Pile.LAYERS)
			byType[e.id] = (byType[e.id] || 0) + 1
		}
	})
	console.info('[zlp] ' + label + ': leaves ' + list.length + ' -> ' + leaves + ', piles ' + piles + ' (layers ' + layers
		+ ') by type ' + JSON.stringify(byType) + ' cfg ' + zlpCfg())
}

function zlpTreeCount(level) {
	var r = { leaves: 0, pilesGround: 0, pilesAir: 0, logs: 0 }
	for (var ax = 310; ax <= 350; ax++) for (var az = 310; az <= 350; az++) for (var ay = ZLP_Y; ay <= ZLP_Y + 30; ay++) {
		var p = new ZLP_BlockPos(ax, ay, az), st = level.getBlockState(p)
		if (st.isAir()) continue
		var b = st.getBlock()
		if (b instanceof ZLP_Leaves) { if (st.getValue(ZLP_Leaves.PERSISTENT) != true) r.leaves++ }
		else if (b instanceof ZLP_Pile) { if (ay == ZLP_Y) r.pilesGround++; else r.pilesAir++ }
		else if (st.getTags().anyMatch(t => t == ZLP_BlockTags.LOGS)) r.logs++
	}
	return r
}

ServerEvents.tick(e => {
	if (!ZLP_pending || e.server.tickCount < ZLP_pending.due) return
	var job = ZLP_pending
	ZLP_pending = null
	try { job.run() } catch (err) { console.info('[zlp] ERROR ' + err) }
})

ServerEvents.commandRegistry(event => {
	var Commands = event.commands
	var root = Commands.literal('zlp')
	root.then(Commands.literal('tick').executes(ctx => { try {
		var level = ctx.source.level, list = zlpRow(level), now = ctx.source.server.tickCount
		list.forEach(e => level.scheduleTick(e.p, level.getBlockState(e.p).getBlock(), 1))
		ZLP_pending = { due: now + 10, run: () => zlpRowResult(level, 'tick', list) }
		return 1 } catch (e) { console.info('[zlp] ERROR ' + e); return 0 } }))
	root.then(Commands.literal('rt').executes(ctx => { try {
		var level = ctx.source.level, list = zlpRow(level)
		list.forEach(e => level.getBlockState(e.p).randomTick(level, e.p, level.random))
		zlpRowResult(level, 'randomTick', list)
		return 1 } catch (e) { console.info('[zlp] ERROR ' + e); return 0 } }))
	root.then(Commands.literal('tree').executes(ctx => { try {
		var level = ctx.source.level, now = ctx.source.server.tickCount
		zlpFloor(level, 310, 350, 310, 350, ZLP_Y, 30)
		var reg = ctx.source.server.registryAccess().registryOrThrow(ZLP_Registry.CONFIGURED_FEATURE_REGISTRY)
		var ok = reg.get(new ZLP_RL('minecraft:oak')).place(level, level.getChunkSource().getGenerator(), level.random, new ZLP_BlockPos(330, ZLP_Y, 330))
		var before = zlpTreeCount(level)
		for (var ax = 310; ax <= 350; ax++) for (var az = 310; az <= 350; az++) for (var ay = ZLP_Y; ay <= ZLP_Y + 30; ay++) {
			var p = new ZLP_BlockPos(ax, ay, az)
			if (level.getBlockState(p).getTags().anyMatch(t => t == ZLP_BlockTags.LOGS)) level.setBlock(p, ZLP_Blocks.AIR.defaultBlockState(), 3)
		}
		console.info('[zlp] tree placed ' + ok + ', before chop ' + JSON.stringify(before))
		ZLP_pending = { due: now + 300, run: () => console.info('[zlp] tree after 300 ticks ' + JSON.stringify(zlpTreeCount(level)) + ' cfg ' + zlpCfg()) }
		return 1 } catch (e) { console.info('[zlp] ERROR ' + e); return 0 } }))
	event.register(root)
})
