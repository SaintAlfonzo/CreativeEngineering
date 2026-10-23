// Scratch-server probe: farming / wood / worldgen features of 2026-10-22 (queue entries SQ-SF, SQ-WC, SQ-MB, SQ-GE
// in feature_writeup/testing/scratch_queue.md). Copy into a SCRATCH server's kubejs/server_scripts/ as
// zz_zfw_probe.js (never the pack's). Prefix ZFW_ (all server scripts share one scope).
//   zfw stem        extra stem fruit (crops.md 2.10): one pad per StemGrownBlock fruit at x 400.., z 400, y 0. Needs
//                   [stem_fruit] secondFruitMinutes = 0, thirdFruitMinutes = 0, fourthFruitMinutes = 1 in the srv toml
//                   before boot. 3000 random ticks -> 3 fruits (4th held by its 1 minute), 1300 ticks later 3000 more
//                   -> 4; then re-point, break-all and enabled = false checks.
//   zfw wild        wild crops (crops.md 2.8, CR-83, CR-86, CR-87, CR-46): biome generation settings after the
//                   biome modifiers, bone-meal flower lists. Registry only, no chunk loads.
//   zfw straw       white strawberries (CR-88): bone-meal strawberry bushes at y 200 and y 150 to ripe.
//   zfw loot        loot rolls: bell peppers (CR-69), leaf apples (CR-57), removed river wood blocks (RW-18).
//   zfw mulberry    Mulberry tree (RW-16, RW-17): the configured feature and the sapling, then the switch off.
//   zfw recipes     removed river wood (RW-15, RW-20): no recipe makes a purged item; one oak-log hedge recipe.
//   zfw geode N     ocean geodes (structure_tuning.md ST-133): up to N (max 36) sites on a 6 x 6 grid, 32 apart,
//                   around the command position: `forceload add X-112 Z-112 X+112 Z+112`, wait, then
//                   `execute positioned X 0 Z run zfw geode 36`. Sites with an old geode are skipped. Places
//                   minecraft:amethyst_geode (and Geode Plus' prismarine geode) per site, reports where it landed.
//   zfw count R ids loaded chunks within R chunks of the command position: count of each block id (space separated).
// stem, straw, loot and mulberry build at x 384-655, z 384-527: `forceload add 384 384 655 527` first (group A).
// Every result is a `[zfw] ...` line in logs/latest.log; checks print PASS / FAIL.
var ZFW_BlockPos = Java.loadClass('net.minecraft.core.BlockPos')
var ZFW_Blocks = Java.loadClass('net.minecraft.world.level.block.Blocks')
var ZFW_Registry = Java.loadClass('net.minecraft.core.Registry')
var ZFW_RL = Java.loadClass('net.minecraft.resources.ResourceLocation')
var ZFW_Dir = Java.loadClass('net.minecraft.core.Direction')
var ZFW_Attached = Java.loadClass('net.minecraft.world.level.block.AttachedStemBlock')
var ZFW_Stem = Java.loadClass('net.minecraft.world.level.block.StemBlock')
var ZFW_Grown = Java.loadClass('net.minecraft.world.level.block.StemGrownBlock')
var ZFW_ForgeReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
var ZFW_Vec3 = Java.loadClass('net.minecraft.world.phys.Vec3')
var ZFW_ItemStack = Java.loadClass('net.minecraft.world.item.ItemStack')
var ZFW_LootBuilder = Java.loadClass('net.minecraft.world.level.storage.loot.LootContext$Builder')
var ZFW_LootParams = Java.loadClass('net.minecraft.world.level.storage.loot.parameters.LootContextParams')
var ZFW_LootSets = Java.loadClass('net.minecraft.world.level.storage.loot.parameters.LootContextParamSets')
var ZFW_Heightmap = Java.loadClass('net.minecraft.world.level.levelgen.Heightmap$Types')
var ZFW_BiomeTags = Java.loadClass('net.minecraft.tags.BiomeTags')
var ZFW_GeodeCfg = Java.loadClass('net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration')
var ZFW_Random = Java.loadClass('net.minecraft.util.RandomSource')
var ZFW_IntArg = Java.loadClass('com.mojang.brigadier.arguments.IntegerArgumentType')
var ZFW_StrArg = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')

var ZFW_HORIZ = [ZFW_Dir.NORTH, ZFW_Dir.EAST, ZFW_Dir.SOUTH, ZFW_Dir.WEST]
var ZFW_pending = []
var ZFW_tally = { pass: 0, fail: 0 }

function zfwCheck(name, ok, detail) {
	if (ok) ZFW_tally.pass++; else ZFW_tally.fail++
	console.info('[zfw] ' + (ok ? 'PASS' : 'FAIL') + ' ' + name + ': ' + detail)
}
function zfwPos(x, y, z) { return new ZFW_BlockPos(x, y, z) }
function zfwBlock(id) { return ZFW_Registry.BLOCK.get(new ZFW_RL(id)) }
function zfwId(st) { return String(ZFW_ForgeReg.BLOCKS.getKey(st.getBlock())) }
function zfwItemId(stack) { return String(ZFW_ForgeReg.ITEMS.getKey(stack.getItem())) }
function zfwLater(server, ticks, run) { ZFW_pending.push({ due: server.tickCount + ticks, run: run }) }
function zfwCf(server, id) {
	return server.registryAccess().registryOrThrow(ZFW_Registry.CONFIGURED_FEATURE_REGISTRY).get(new ZFW_RL(id))
}
function zfwStemCfg() { return Java.loadClass('com.SaintAlfonzo.creativeengineering.CEConfig').STEM_FRUIT }
function zfwWgCfg() { return Java.loadClass('com.SaintAlfonzo.creativeengineering.CEConfig').WORLDGEN }

// Stone at y-2, dirt (grass if asked) at y-1, air above, flags 2 | 16 (no updates).
function zfwPad(level, x0, x1, z0, z1, top, grass) {
	for (var ax = x0; ax <= x1; ax++) for (var az = z0; az <= z1; az++) for (var ay = -2; ay <= top; ay++) {
		var st = ay == -2 ? ZFW_Blocks.STONE.defaultBlockState()
			: ay == -1 ? (grass ? ZFW_Blocks.GRASS_BLOCK : ZFW_Blocks.DIRT).defaultBlockState() : ZFW_Blocks.AIR.defaultBlockState()
		level.setBlock(zfwPos(ax, ay, az), st, 2 | 16)
	}
}

// ---------- extra stem fruit ----------
function zfwFruits(level, p, fruit) {
	var n = 0
	ZFW_HORIZ.forEach(d => { if (level.getBlockState(p.relative(d)).getBlock().equals(fruit)) n++ })
	return n
}
function zfwTicks(level, p, n) {
	for (var i = 0; i < n; i++) {
		var st = level.getBlockState(p)
		if (!(st.getBlock() instanceof ZFW_Attached)) return
		st.randomTick(level, p, level.random)
	}
}
// Attached stem at (x, 0, 400) on farmland facing east, its fruit east of it, dirt on the other sides.
function zfwStemPad(level, x, fruit) {
	zfwPad(level, x - 2, x + 2, 398, 402, 2, false)
	var p = zfwPos(x, 0, 400)
	level.setBlock(p.below(), ZFW_Blocks.FARMLAND.defaultBlockState(), 2 | 16)
	level.getBlock(p).set(String(ZFW_ForgeReg.BLOCKS.getKey(fruit.getAttachedStem())), { facing: 'east' })
	level.setBlock(p.east(), fruit.defaultBlockState(), 2 | 16)
	return p
}
function zfwStem(server, level) {
	var cfg = zfwStemCfg()
	var fruits = []
	// A stem's own fruit only: Delightful's slices and cantaloupe extend MelonBlock / PumpkinBlock and report the
	// vanilla stem, which never grows them (StemExtraFruit skips them too)
	ZFW_ForgeReg.BLOCKS.getValues().forEach(b => { if (b instanceof ZFW_Grown && b.getStem().getFruit().equals(b)) fruits.push(b) })
	console.info('[zfw] stem fruits found: ' + fruits.map(f => String(ZFW_ForgeReg.BLOCKS.getKey(f))).join(', ')
		+ ' cfg enabled ' + cfg.enabled.get() + ' max ' + cfg.maxFruits.get() + ' minutes ' + cfg.secondFruitMinutes.get()
		+ '/' + cfg.thirdFruitMinutes.get() + '/' + cfg.fourthFruitMinutes.get())
	zfwCheck('SQ-SF-01 melon, pumpkin, hamimelon present', fruits.length >= 3, fruits.length + ' fruit blocks')
	var pads = []
	fruits.forEach((fruit, n) => {
		var id = String(ZFW_ForgeReg.BLOCKS.getKey(fruit))
		var p = zfwStemPad(level, 400 + 8 * n, fruit)
		zfwCheck('SQ-SF-01 ' + id + ' attached stem random-ticks', String(level.getBlockState(p).isRandomlyTicking()) == 'true',
			zfwId(level.getBlockState(p)))
		zfwTicks(level, p, 3000)
		var c = zfwFruits(level, p, fruit)
		zfwCheck('SQ-SF-02 ' + id + ' 2nd and 3rd fruit, 4th held by its wait', c == 3, c + ' fruits after 3000 random ticks')
		pads.push({ p: p, fruit: fruit, id: id })
	})
	zfwLater(server, 1300, () => {
		pads.forEach(e => {
			zfwTicks(level, e.p, 3000)
			var c = zfwFruits(level, e.p, e.fruit)
			zfwCheck('SQ-SF-03 ' + e.id + ' 4th fruit after the wait, no 5th', c == 4, c + ' fruits')
			// re-point: break the fruit the stem faces
			var st = level.getBlockState(e.p)
			var faced = e.p.relative(st.getValue(ZFW_Attached.FACING))
			level.setBlock(faced, ZFW_Blocks.AIR.defaultBlockState(), 3)
			var st2 = level.getBlockState(e.p)
			var ok = st2.getBlock() instanceof ZFW_Attached
				&& level.getBlockState(e.p.relative(st2.getValue(ZFW_Attached.FACING))).getBlock().equals(e.fruit)
			zfwCheck('SQ-SF-04 ' + e.id + ' re-points at another fruit', ok, String(st2))
			ZFW_HORIZ.forEach(d => {
				if (level.getBlockState(e.p.relative(d)).getBlock().equals(e.fruit)) level.setBlock(e.p.relative(d), ZFW_Blocks.AIR.defaultBlockState(), 3)
			})
			var st3 = level.getBlockState(e.p)
			zfwCheck('SQ-SF-04 ' + e.id + ' all taken -> plain grown stem', st3.getBlock() instanceof ZFW_Stem, String(st3))
		})
		// off switch: a fresh pad grows nothing extra
		cfg.enabled.set(false)
		var f0 = pads[0].fruit, p0 = zfwStemPad(level, 400 + 8 * pads.length, f0)
		zfwTicks(level, p0, 3000)
		var c0 = zfwFruits(level, p0, f0)
		cfg.enabled.set(true)
		zfwCheck('SQ-SF-05 enabled = false: no extra fruit', c0 == 1, c0 + ' fruits')
		console.info('[zfw] stem done ' + ZFW_tally.pass + ' pass, ' + ZFW_tally.fail + ' fail')
	})
}

// ---------- wild crops (biome registry) ----------
function zfwPlaced(reg, biomeId) {
	var b = reg.get(new ZFW_RL(biomeId)), out = {}
	if (b == null) return null
	b.getGenerationSettings().features().forEach(set => set.forEach(h => {
		var k = h.unwrapKey()
		if (k.isPresent()) out[String(k.get().location())] = true
	}))
	return out
}
function zfwHas(reg, biome, feats, want) {
	var placed = zfwPlaced(reg, biome)
	if (placed == null) { zfwCheck(biome, false, 'no such biome'); return }
	var have = feats.filter(f => placed[f])
	return want ? have.length == feats.length : have.length == 0
}
function zfwWild(server) {
	var reg = server.registryAccess().registryOrThrow(ZFW_Registry.BIOME_REGISTRY)
	var TEA = ['farmersrespite:patch_wild_tea_bush']
	;['minecraft:swamp', 'minecraft:mangrove_swamp', 'minecraft:jungle', 'minecraft:dark_forest', 'terralith:lush_valley',
		'terralith:sakura_grove', 'terralith:orchid_swamp', 'terralith:tropical_jungle', 'terralith:cloud_forest']
		.forEach(b => zfwCheck('SQ-WC-01 tea in ' + b, zfwHas(reg, b, TEA, true), ''))
	zfwCheck('SQ-WC-01 no tea in minecraft:plains', zfwHas(reg, 'minecraft:plains', TEA, false), '')
	var G = 'vinery:', GR = [G + 'red_grape_bush_chance', G + 'white_grape_bush_chance'],
		GS = [G + 'savanna_red_grape_bush_chance', G + 'savanna_white_grape_bush_chance'],
		GT = [G + 'taiga_red_grape_bush_chance', G + 'taiga_white_grape_bush_chance'],
		GJ = [G + 'jungle_red_grape_bush_chance', G + 'jungle_white_grape_bush_chance']
	zfwCheck('SQ-WC-02 grape bushes in terralith:lavender_forest', zfwHas(reg, 'terralith:lavender_forest', GR, true), '')
	zfwCheck('SQ-WC-02 savanna grapes in terralith:shrubland', zfwHas(reg, 'terralith:shrubland', GS, true), '')
	zfwCheck('SQ-WC-02 taiga grapes in terralith:shield', zfwHas(reg, 'terralith:shield', GT, true), '')
	;['terralith:tropical_jungle', 'minecraft:mangrove_swamp'].forEach(b =>
		zfwCheck('SQ-WC-02 grape vines in ' + b, zfwHas(reg, b, GJ, true), ''))
	;['minecraft:snowy_taiga', 'minecraft:dark_forest'].forEach(b =>
		zfwCheck('SQ-WC-02 no grapes in ' + b, zfwHas(reg, b, GR.concat(GS, GT, GJ), false), ''))
	// wild flax: no overworld biome places a flax feature
	var flax = []
	reg.keySet().forEach(rl => {
		var placed = zfwPlaced(reg, String(rl))
		Object.keys(placed || {}).forEach(f => { if (f.indexOf('flax') >= 0) flax.push(String(rl) + ' ' + f) })
	})
	zfwCheck('SQ-WC-03 no wild flax anywhere', flax.length == 0, flax.slice(0, 5).join(', '))
	// Rustic Delight patches: still generated, not in the bone-meal flower lists
	zfwCheck('SQ-WC-04 savanna generates wild cotton', zfwHas(reg, 'minecraft:savanna', ['rusticdelight:wild_cotton_placed'], true), '')
	zfwCheck('SQ-WC-04 jungle generates wild bell peppers and coffee',
		zfwHas(reg, 'minecraft:jungle', ['rusticdelight:wild_bell_peppers_placed', 'rusticdelight:wild_coffee_placed'], true), '')
	var cfReg = server.registryAccess().registryOrThrow(ZFW_Registry.CONFIGURED_FEATURE_REGISTRY)
	;['minecraft:savanna', 'minecraft:jungle', 'minecraft:plains'].forEach(b => {
		var names = []
		reg.get(new ZFW_RL(b)).getGenerationSettings().getFlowerFeatures().forEach(cf => names.push(String(cfReg.getKey(cf))))
		zfwCheck('SQ-WC-04 bone meal in ' + b + ' grows no Rustic Delight patch',
			names.filter(n => n.indexOf('rusticdelight:') == 0).length == 0, names.join(', '))
	})
	console.info('[zfw] wild done ' + ZFW_tally.pass + ' pass, ' + ZFW_tally.fail + ' fail')
}

// ---------- white strawberries ----------
function zfwStraw(level) {
	var cfgH = 'n/a'
	try { cfgH = Java.loadClass('com.teamabnormals.neapolitan.core.NeapolitanConfig').COMMON.whiteStrawberryMinHeight.get() } catch (e) { cfgH = String(e) }
	;[[200, 'white'], [191, 'red'], [150, 'red']].forEach(row => {
		var y = row[0], p = zfwPos(600, y, 400)
		level.setBlock(p.below(), ZFW_Blocks.GRASS_BLOCK.defaultBlockState(), 2 | 16)
		level.getBlock(p).set('neapolitan:strawberry_bush', {})
		for (var i = 0; i < 60; i++) {
			var st = level.getBlockState(p)
			if (String(st).indexOf('age=6') >= 0) break
			st.getBlock().performBonemeal(level, level.random, p, st)
		}
		var s = String(level.getBlockState(p))
		zfwCheck('SQ-WC-05 strawberry bush at y ' + y + ' ripens ' + row[1], s.indexOf('age=6') >= 0 && s.indexOf('type=' + row[1]) >= 0,
			s + ' (config min height ' + cfgH + ')')
	})
}

// ---------- loot ----------
function zfwRoll(level, state, n) {
	var table = level.getServer().getLootTables().get(state.getBlock().getLootTable())
	var out = {}
	for (var i = 0; i < n; i++) {
		var ctx = new ZFW_LootBuilder(level).withParameter(ZFW_LootParams.ORIGIN, new ZFW_Vec3(0, 64, 0))
			.withParameter(ZFW_LootParams.BLOCK_STATE, state).withParameter(ZFW_LootParams.TOOL, ZFW_ItemStack.EMPTY)
			.withRandom(level.random).create(ZFW_LootSets.BLOCK)
		table.getRandomItems(ctx).forEach(st => { var id = zfwItemId(st); out[id] = (out[id] || 0) + st.getCount() })
	}
	return out
}
function zfwStateAt(level, id, props) {
	var p = zfwPos(640, 0, 400)
	level.setBlock(p.below(), ZFW_Blocks.FARMLAND.defaultBlockState(), 2 | 16)
	level.getBlock(p).set(id, props)
	return level.getBlockState(p)
}
function zfwLoot(level) {
	var ripe = zfwStateAt(level, 'rusticdelight:bell_peppers', { age: '7' }), N = 600
	var r = zfwRoll(level, ripe, N), col = { red: 0, green: 0, yellow: 0 }
	Object.keys(r).forEach(id => Object.keys(col).forEach(c => { if (id.indexOf('pepper') >= 0 && id.indexOf(c) >= 0 && id.indexOf('seed') < 0) col[c] += r[id] }))
	var per = Object.keys(col).map(c => c + ' ' + (col[c] / N).toFixed(3))
	zfwCheck('SQ-CD-01 bell peppers about 0.48 of each colour per ripe harvest',
		Object.keys(col).every(c => col[c] / N > 0.40 && col[c] / N < 0.56), per.join(', ') + ' ' + JSON.stringify(r))
	var unripe = zfwRoll(level, zfwStateAt(level, 'rusticdelight:bell_peppers', { age: '3' }), 100)
	zfwCheck('SQ-CD-01 unripe bell peppers drop no pepper',
		Object.keys(unripe).filter(id => id.indexOf('pepper') >= 0 && id.indexOf('seed') < 0).length == 0, JSON.stringify(unripe))
	;['minecraft:oak_leaves', 'minecraft:dark_oak_leaves', 'myrtrees:rubberwood_leaves'].forEach(id => {
		var b = zfwBlock(id)
		var d = zfwRoll(level, b.defaultBlockState(), 2000)
		zfwCheck('SQ-CD-02 no apples from ' + id + ' (2000 rolls)', !d['minecraft:apple'], JSON.stringify(d))
	})
	;[['upgrade_aquatic:river_log', 'minecraft:oak_log'], ['upgrade_aquatic:river_planks', 'minecraft:oak_planks']].forEach(row => {
		var d = zfwRoll(level, zfwBlock(row[0]).defaultBlockState(), 5)
		zfwCheck('SQ-MB-03 ' + row[0] + ' drops ' + row[1], d[row[1]] == 5 && Object.keys(d).length == 1, JSON.stringify(d))
	})
	console.info('[zfw] loot done ' + ZFW_tally.pass + ' pass, ' + ZFW_tally.fail + ' fail')
}

// ---------- Mulberry tree ----------
function zfwTreeCount(level, x0, z0) {
	var r = { oak_log: 0, river_log: 0, mulberry_leaves: 0, vines: 0 }
	for (var ax = x0 - 6; ax <= x0 + 6; ax++) for (var az = z0 - 6; az <= z0 + 6; az++) for (var ay = 0; ay <= 14; ay++) {
		var st = level.getBlockState(zfwPos(ax, ay, az))
		if (st.isAir()) continue
		var id = zfwId(st)
		if (id == 'minecraft:oak_log') r.oak_log++
		else if (id == 'upgrade_aquatic:river_log') r.river_log++
		else if (id == 'upgrade_aquatic:river_leaves') r.mulberry_leaves++
		else if (id.indexOf('mulberry') >= 0) r.vines++
	}
	return r
}
function zfwSapling(level, x) {
	var p = zfwPos(x, 0, 500)
	level.getBlock(p).set('upgrade_aquatic:river_sapling', {})
	for (var i = 0; i < 40; i++) {
		var st = level.getBlockState(p)
		if (zfwId(st) != 'upgrade_aquatic:river_sapling') break
		st.getBlock().performBonemeal(level, level.random, p, st)
	}
	return zfwTreeCount(level, x, 500)
}
function zfwMulberry(server, level) {
	zfwPad(level, 482, 573, 492, 508, 14, true)
	var ok = zfwCf(server, 'upgrade_aquatic:river_tree').place(level, level.getChunkSource().getGenerator(), level.random, zfwPos(490, 0, 500))
	var f = zfwTreeCount(level, 490, 500)
	zfwCheck('SQ-MB-01 river_tree feature: oak trunk, mulberry leaves', ok && f.oak_log > 0 && f.river_log == 0 && f.mulberry_leaves > 0,
		'placed ' + ok + ' ' + JSON.stringify(f))
	var s = zfwSapling(level, 510)
	zfwCheck('SQ-MB-02 bone-meal Mulberry Sapling: oak trunk', s.oak_log > 0 && s.river_log == 0 && s.mulberry_leaves > 0, JSON.stringify(s))
	var sys = zfwWgCfg().systems
	sys.mulberrySapling.set(false)
	var off = zfwSapling(level, 530)
	sys.mulberrySapling.set(true)
	zfwCheck('SQ-MB-02 mulberrySapling = false: UA tree with river logs', off.river_log > 0 && off.oak_log == 0, JSON.stringify(off))
	console.info('[zfw] mulberry done ' + ZFW_tally.pass + ' pass, ' + ZFW_tally.fail + ' fail')
}

// Recipes: nothing makes a purged river item; the Mulberry Hedge recipe takes an oak log.
function zfwRecipes(server) {
	var purged = {}
	Object.keys(global.removedWoods || {}).forEach(w => global.removedWoods[w].forEach(m => { purged[String(m[0])] = true }))
	var makers = [], hedge = []
	server.getRecipeManager().getRecipes().forEach(r => {
		var res
		try { res = r.getResultItem() } catch (e) { return }
		if (res == null || res.isEmpty()) return
		var id = zfwItemId(res)
		if (purged[id]) makers.push(String(r.getId()) + ' -> ' + id)
		if (id == 'upgrade_aquatic:river_hedge') {
			var oak = false
			r.getIngredients().forEach(ing => { if (ing.test(Item.of('minecraft:oak_log'))) oak = true })
			hedge.push(String(r.getId()) + ' x' + res.getCount() + (oak ? ' oak log' : ' NO oak log'))
		}
	})
	zfwCheck('SQ-MB-00 ' + Object.keys(purged).length + ' purged ids, no recipe makes one', Object.keys(purged).length > 0 && makers.length == 0,
		makers.slice(0, 8).join(', '))
	zfwCheck('SQ-MB-00 one Mulberry Hedge recipe, oak log, x2', hedge.length == 1 && hedge[0].indexOf(' x2 oak log') > 0, hedge.join(', '))
}

// ---------- ocean geodes ----------
function zfwLoaded(level, x0, x1, z0, z1) {
	for (var cx = x0 >> 4; cx <= x1 >> 4; cx++) for (var cz = z0 >> 4; cz <= z1 >> 4; cz++) if (!level.hasChunk(cx, cz)) return false
	return true
}
function zfwShellBlocks() { return [ZFW_Blocks.CALCITE, ZFW_Blocks.AMETHYST_BLOCK, ZFW_Blocks.BUDDING_AMETHYST, ZFW_Blocks.SMOOTH_BASALT] }
function zfwIsAny(st, blocks) { var b = st.getBlock(); for (var i = 0; i < blocks.length; i++) if (b.equals(blocks[i])) return true; return false }
// Every block of the given kinds in loaded chunks of the box: list of {x, y, z, water}, sections skipped by palette.
function zfwScan(level, x0, x1, z0, z1, blocks) {
	var out = []
	for (var cx = x0 >> 4; cx <= x1 >> 4; cx++) for (var cz = z0 >> 4; cz <= z1 >> 4; cz++) {
		if (!level.hasChunk(cx, cz)) continue
		var chunk = level.getChunk(cx, cz), secs = chunk.getSections()
		for (var i = 0; i < secs.length; i++) {
			var sec = secs[i]
			if (sec.hasOnlyAir() || !sec.getStates().maybeHas(s => zfwIsAny(s, blocks))) continue
			var by = chunk.getSectionYFromSectionIndex(i) * 16
			for (var lx = 0; lx < 16; lx++) for (var lz = 0; lz < 16; lz++) for (var ly = 0; ly < 16; ly++) {
				var wx = cx * 16 + lx, wz = cz * 16 + lz
				if (wx < x0 || wx > x1 || wz < z0 || wz > z1) continue
				if (zfwIsAny(sec.getBlockState(lx, ly, lz), blocks)) out.push({ x: wx, y: by + ly, z: wz })
			}
		}
	}
	return out
}
function zfwGeodeFeatures(server) {
	var reg = server.registryAccess().registryOrThrow(ZFW_Registry.CONFIGURED_FEATURE_REGISTRY), out = []
	reg.keySet().forEach(rl => {
		var cf = reg.get(rl), c = cf.config()
		if (!(c instanceof ZFW_GeodeCfg)) return
		var inner = c.geodeBlockSettings.innerLayerProvider.getState(ZFW_Random.create(0), ZFW_BlockPos.ZERO)
		var alt = c.geodeBlockSettings.alternateInnerLayerProvider.getState(ZFW_Random.create(0), ZFW_BlockPos.ZERO)
		out.push({ id: String(rl), inner: zfwId(inner), alt: zfwId(alt), cf: cf })
	})
	return out
}
function zfwGeode(server, level, src, n) {
	var geodes = zfwGeodeFeatures(server)
	console.info('[zfw] geode features: ' + geodes.map(g => g.id + ' (' + g.inner + ')').join(', '))
	var wg = zfwWgCfg().oceanGeodes
	var top = wg.lowestFloorY.get() - wg.clearance.get()
	console.info('[zfw] geode cfg on ' + zfwWgCfg().systems.oceanGeodes.get() + ' lowestFloorY ' + wg.lowestFloorY.get()
		+ ' clearance ' + wg.clearance.get() + ' spread ' + wg.spread.get() + ' stayShare ' + wg.stayShare.get() + ' -> moved tops <= ' + top)
	var amethyst = geodes.filter(g => g.id == 'minecraft:amethyst_geode')[0]
	// Geode Plus' prismarine geode: plain prismarine inside, budding prismarine in the alternate layer (OceanGeodes checks both)
	var prism = geodes.filter(g => g.inner == 'geode:budding_prismarine' || g.alt == 'geode:budding_prismarine')[0]
	var gen = level.getChunkSource().getGenerator(), shell = zfwShellBlocks()
	var res = { ocean: [], land: [], prism: [] }, tried = 0
	for (var k = 0; tried < n && k < 36; k++) {
		// 6 x 6 grid of sites 32 blocks apart around the command position (fits one 15 x 15 chunk forceload)
		var sx = src.getX() + 32 * ((k % 6) - 3) + 16, sz = src.getZ() + 32 * (Math.floor(k / 6) - 3) + 16
		if (!zfwLoaded(level, sx - 20, sx + 20, sz - 20, sz + 20)) continue
		tried++
		var floor = level.getHeight(ZFW_Heightmap.OCEAN_FLOOR, sx, sz)
		var ocean = level.getBiome(zfwPos(sx, floor, sz)).tags().anyMatch(t => t.equals(ZFW_BiomeTags.IS_OCEAN))
		var kind = ocean ? (prism && tried % 4 == 0 ? 'prism' : 'ocean') : 'land'
		var origin = zfwPos(sx, floor - (ocean ? 4 : 20), sz)
		var g = kind == 'prism' ? prism : amethyst
		var blocks = kind == 'prism' ? [zfwBlock('geode:budding_prismarine'), ZFW_Blocks.CALCITE, ZFW_Blocks.SMOOTH_BASALT] : shell
		// natural geodes are common in a full-height 36 x 36 column: keep only the blocks this placement added
		var before = {}
		zfwScan(level, sx - 18, sx + 18, sz - 18, sz + 18, blocks).forEach(b => { before[b.x + ',' + b.y + ',' + b.z] = true })
		var ok = g.cf.place(level, gen, level.random, origin)
		var got = zfwScan(level, sx - 18, sx + 18, sz - 18, sz + 18, blocks).filter(b => !before[b.x + ',' + b.y + ',' + b.z])
		var maxY = -999, water = 0
		got.forEach(b => {
			if (b.y > maxY) maxY = b.y
			var bp = zfwPos(b.x, b.y, b.z)
			if (!level.getFluidState(bp.above()).isEmpty()) water++
		})
		var row = { x: sx, z: sz, floor: floor, origin: origin.getY(), placed: String(ok), top: got.length ? maxY : null, exposed: water > 0 }
		res[kind].push(row)
		console.info('[zfw] geode site ' + kind + ' ' + JSON.stringify(row))
	}
	var oc = res.ocean.filter(r => r.top != null), moved = oc.filter(r => r.top <= top)
	zfwCheck('SQ-GE-01 ocean amethyst geodes moved under y ' + top, oc.length > 0 && moved.length >= Math.ceil(oc.length * 0.75),
		moved.length + ' of ' + oc.length + ' placed (' + res.ocean.length + ' sites; the rest kept their height: stayShare, or did not place in water)')
	// water above a shell block is often an aquifer or cave under the ocean, not the sea: the check is the top
	var reach = moved.filter(r => r.top > r.floor - wg.clearance.get())
	zfwCheck('SQ-GE-01 moved ocean geodes never reach the sea floor', reach.length == 0,
		reach.length + ' reach it; ' + moved.filter(r => r.exposed).length + ' touch water (aquifers, caves)')
	var ld = res.land.filter(r => r.top != null)
	zfwCheck('SQ-GE-02 land geodes keep their height', ld.every(r => r.top >= r.origin - 8), ld.length + ' land geodes: '
		+ ld.map(r => r.origin + '->' + r.top).join(' '))
	var pr = res.prism.filter(r => r.top != null)
	zfwCheck('SQ-GE-03 prismarine geodes keep their height', prism != null && pr.every(r => r.top >= r.origin - 8),
		(prism ? prism.id : 'no prismarine geode feature') + ': ' + pr.map(r => r.origin + '->' + r.top).join(' '))
	console.info('[zfw] geode done ' + ZFW_tally.pass + ' pass, ' + ZFW_tally.fail + ' fail')
}

function zfwCount(level, src, r, ids) {
	var blocks = ids.map(id => zfwBlock(id)), cx = src.getX() >> 4, cz = src.getZ() >> 4, loaded = 0
	for (var a = cx - r; a <= cx + r; a++) for (var b = cz - r; b <= cz + r; b++) if (level.hasChunk(a, b)) loaded++
	var counts = {}
	ids.forEach((id, i) => {
		counts[id] = zfwScan(level, (cx - r) * 16, (cx + r) * 16 + 15, (cz - r) * 16, (cz + r) * 16 + 15, [blocks[i]]).length
	})
	console.info('[zfw] count in ' + loaded + ' loaded chunks around ' + src.getX() + ' ' + src.getZ() + ': ' + JSON.stringify(counts))
}

// SQ-MB-04 without luck: UA's placed feature trees_river (the worldgen path: placed -> configured holder, with its own
// biome filter and dry-land placement) placed at every loaded chunk around the position, then only the added blocks counted.
// River biome chunks only place anything. Run after forceloading a river area.
function zfwRiverTrees(server, level, src, r) {
	var reg = server.registryAccess().registryOrThrow(ZFW_Registry.PLACED_FEATURE_REGISTRY)
	var pf = reg.get(new ZFW_RL('upgrade_aquatic:trees_river'))
	if (pf == null) { zfwCheck('SQ-MB-04 placed feature upgrade_aquatic:trees_river exists', false, 'missing'); return }
	var ids = ['upgrade_aquatic:river_log', 'upgrade_aquatic:river_leaves', 'minecraft:oak_log']
	var blocks = ids.map(id => zfwBlock(id)), cx = src.getX() >> 4, cz = src.getZ() >> 4
	var sy = level.getHeight(ZFW_Heightmap.WORLD_SURFACE, src.getX(), src.getZ())
	var biome = level.getBiome(zfwPos(src.getX(), sy, src.getZ()))
	var dry = 0
	for (var dx = -48; dx <= 48; dx += 4) for (var dz = -48; dz <= 48; dz += 4) {
		var hx = src.getX() + dx, hz = src.getZ() + dz, hy = level.getHeight(ZFW_Heightmap.OCEAN_FLOOR, hx, hz)
		if (level.getFluidState(zfwPos(hx, hy, hz)).isEmpty() && String(level.getBiome(zfwPos(hx, hy, hz)).unwrapKey().get().location()) == String(biome.unwrapKey().get().location())) dry++
	}
	console.info('[zfw] rivertrees biome ' + biome.unwrapKey().get().location() + ' has trees_river ' + biome.value().getGenerationSettings().hasFeature(pf)
		+ '; dry columns of that biome in a 25 x 25 sample: ' + dry)
	var x0 = (cx - r) * 16, x1 = (cx + r) * 16 + 15, z0 = (cz - r) * 16, z1 = (cz + r) * 16 + 15
	var before = {}
	zfwScan(level, x0, x1, z0, z1, blocks).forEach(b => { before[b.x + ',' + b.y + ',' + b.z] = true })
	var gen = level.getChunkSource().getGenerator(), placed = 0, tries = 0
	for (var a = cx - r; a <= cx + r; a++) for (var b = cz - r; b <= cz + r; b++) {
		if (!level.hasChunk(a, b)) continue
		for (var k = 0; k < 4; k++) { tries++; if (pf.placeWithBiomeCheck(level, gen, level.random, zfwPos(a * 16, 0, b * 16))) placed++ }
	}
	var added = {}
	ids.forEach(id => added[id] = 0)
	zfwScan(level, x0, x1, z0, z1, blocks).forEach(bl => {
		if (before[bl.x + ',' + bl.y + ',' + bl.z]) return
		var id = zfwId(level.getBlockState(zfwPos(bl.x, bl.y, bl.z)))
		if (added[id] != null) added[id]++
	})
	console.info('[zfw] rivertrees ' + placed + ' of ' + tries + ' placements, added ' + JSON.stringify(added))
	if (placed > 0) {
		zfwCheck('SQ-MB-04 worldgen river trees: leaves, no river logs', added['upgrade_aquatic:river_leaves'] > 0
			&& added['upgrade_aquatic:river_log'] == 0 && added['minecraft:oak_log'] > 0, JSON.stringify(added))
		return
	}
	// No dry land in the river biome here (trees_river needs dry columns): place the configured feature the placed
	// feature's holder points at (the datapack override, if the worldgen path reads it) on dry ground nearby
	var cf = pf.feature().value(), grown = 0, add2 = {}
	ids.forEach(id => add2[id] = 0)
	var before2 = {}
	zfwScan(level, x0, x1, z0, z1, blocks).forEach(b => { before2[b.x + ',' + b.y + ',' + b.z] = true })
	for (var dx = -40; dx <= 40 && grown < 6; dx += 8) for (var dz = -40; dz <= 40 && grown < 6; dz += 8) {
		var hx = src.getX() + dx, hz = src.getZ() + dz, hy = level.getHeight(ZFW_Heightmap.OCEAN_FLOOR, hx, hz)
		if (!level.getFluidState(zfwPos(hx, hy, hz)).isEmpty()) continue
		if (cf.place(level, gen, level.random, zfwPos(hx, hy, hz))) grown++
	}
	zfwScan(level, x0, x1, z0, z1, blocks).forEach(bl => {
		if (before2[bl.x + ',' + bl.y + ',' + bl.z]) return
		var id = zfwId(level.getBlockState(zfwPos(bl.x, bl.y, bl.z)))
		if (add2[id] != null) add2[id]++
	})
	console.info('[zfw] rivertrees: no dry river land, placed the holder\'s configured feature ' + grown + ' times on dry ground')
	zfwCheck('SQ-MB-04 trees_river holder grows the override: leaves, oak trunk, no river logs', grown > 0
		&& add2['upgrade_aquatic:river_leaves'] > 0 && add2['upgrade_aquatic:river_log'] == 0 && add2['minecraft:oak_log'] > 0, JSON.stringify(add2))
}

ServerEvents.tick(e => {
	if (!ZFW_pending.length) return
	var now = e.server.tickCount
	var due = ZFW_pending.filter(j => now >= j.due)
	if (!due.length) return
	ZFW_pending = ZFW_pending.filter(j => now < j.due)
	due.forEach(j => { try { j.run() } catch (err) { console.info('[zfw] ERROR ' + err) } })
})

ServerEvents.commandRegistry(event => {
	var C = event.commands
	var root = C.literal('zfw')
	function sub(name, fn) {
		root.then(C.literal(name).executes(ctx => { try { fn(ctx); return 1 } catch (e) { console.info('[zfw] ERROR ' + name + ': ' + e); return 0 } }))
	}
	function srcPos(ctx) { var v = ctx.source.getPosition(); return zfwPos(Math.floor(v.x()), 0, Math.floor(v.z())) }
	sub('stem', ctx => zfwStem(ctx.source.server, ctx.source.level))
	sub('wild', ctx => zfwWild(ctx.source.server))
	sub('straw', ctx => zfwStraw(ctx.source.level))
	sub('loot', ctx => zfwLoot(ctx.source.level))
	sub('mulberry', ctx => zfwMulberry(ctx.source.server, ctx.source.level))
	sub('recipes', ctx => zfwRecipes(ctx.source.server))
	root.then(C.literal('geode').then(C.argument('n', ZFW_IntArg.integer(1, 36)).executes(ctx => { try {
		zfwGeode(ctx.source.server, ctx.source.level, srcPos(ctx), ZFW_IntArg.getInteger(ctx, 'n')); return 1
	} catch (e) { console.info('[zfw] ERROR geode: ' + e); return 0 } })))
	root.then(C.literal('rivertrees').then(C.argument('r', ZFW_IntArg.integer(0, 8)).executes(ctx => { try {
		zfwRiverTrees(ctx.source.server, ctx.source.level, srcPos(ctx), ZFW_IntArg.getInteger(ctx, 'r')); return 1
	} catch (e) { console.info('[zfw] ERROR rivertrees: ' + e); return 0 } })))
	root.then(C.literal('count').then(C.argument('r', ZFW_IntArg.integer(0, 16)).then(C.argument('ids', ZFW_StrArg.greedyString()).executes(ctx => { try {
		zfwCount(ctx.source.level, srcPos(ctx), ZFW_IntArg.getInteger(ctx, 'r'), String(ZFW_StrArg.getString(ctx, 'ids')).trim().split(/\s+/)); return 1
	} catch (e) { console.info('[zfw] ERROR count: ' + e); return 0 } }))))
	event.register(root)
})
