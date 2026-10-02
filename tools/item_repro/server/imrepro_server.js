// Item-loss repro harness, SERVER half. Copied into a scratch server's kubejs/server_scripts by
// tools/item_repro/setup.sh; never part of the pack. See tools/item_repro/README.md.
//
// `/imrepro setup` builds a sealed glass box with a chest, fills the chest and the player's
// inventory with a fixed item set, and records the per-item totals as the baseline. Every tick
// after that the totals are recounted across everything an item can be in inside the box
// (player inventory, 2x2 crafting grid, cursor, chest, shulker and backpack contents, dropped
// item entities). Items can only move between those places during the test, so any change in a
// total is a real loss or duplication; it is logged as a DELTA line with the current phase mark.

(function () {
	var ForgeRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
	var ResourceLocation = Java.loadClass('net.minecraft.resources.ResourceLocation')
	var BlockPos = Java.loadClass('net.minecraft.core.BlockPos')
	var AABB = Java.loadClass('net.minecraft.world.phys.AABB')
	var ItemEntity = Java.loadClass('net.minecraft.world.entity.item.ItemEntity')
	var Blocks = Java.loadClass('net.minecraft.world.level.block.Blocks')
	var JItemStack = Java.loadClass('net.minecraft.world.item.ItemStack')
	var TagParser = Java.loadClass('net.minecraft.nbt.TagParser')
	var StringArg = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
	var IntArg = Java.loadClass('com.mojang.brigadier.arguments.IntegerArgumentType')

	// Kept on global so a /reload of server scripts doesn't drop the baseline mid-run.
	var S = global.imreproS || (global.imreproS = { uuid: null, level: null, x: 0, y: 200, z: 0, chestPos: null, base: null, lastSig: '', mark: '-', lagMs: 0, tick: 0, deltas: 0 })

	function log(s) { console.info('[imrepro] ' + s) }

	function bpCap() {
		try { return Java.loadClass('net.p3pp3rf1y.sophisticatedbackpacks.api.CapabilityBackpackWrapper').getCapabilityInstance() } catch (err) { return null }
	}

	function stackOf(id, count, nbt) {
		var st = new JItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(id)), count)
		if (nbt) st.setNbt(TagParser.parseTag(nbt))
		return st
	}

	// Containers keep their contents in NBT that changes as items move, so key them by id only.
	function keyOf(st) {
		var id = String(ForgeRegistries.ITEMS.getKey(st.getItem()))
		if (id.indexOf('shulker_box') >= 0 || id.indexOf('backpack') >= 0) return id
		var tag = st.getNbt()
		return tag == null ? id : id + String(tag)
	}

	function addStack(map, st, depth) {
		if (st == null || st.isEmpty()) return
		var k = keyOf(st)
		map[k] = (map[k] || 0) + st.getCount()
		if (depth > 2) return
		var tag = st.getNbt()
		if (tag != null && tag.contains('BlockEntityTag')) {
			var list = tag.getCompound('BlockEntityTag').getList('Items', 10)
			for (var li = 0; li < list.size(); li++) addStack(map, JItemStack.of(list.getCompound(li)), depth + 1)
		}
		var cap = bpCap()
		if (cap != null && String(ForgeRegistries.ITEMS.getKey(st.getItem())).indexOf('sophisticatedbackpacks:') == 0) {
			var nbt = st.getNbt()
			if (S.uuids != null) S.uuids.push(nbt != null && nbt.contains('contentsUuid') ? String(nbt.get('contentsUuid')) : 'none')
			var opt = st.getCapability(cap).resolve()
			if (opt.isPresent()) {
				var w = opt.get()
				var inv = w.getInventoryHandler()
				for (var bi = 0; bi < inv.getSlots(); bi++) addStack(map, inv.getStackInSlot(bi), depth + 1)
				var up = w.getUpgradeHandler()
				for (var ui = 0; ui < up.getSlots(); ui++) addStack(map, up.getStackInSlot(ui), depth + 1)
			}
		}
	}

	function guarded(what, fn) {
		try { return fn() } catch (err) { log('ERROR in ' + what + ': ' + err); return 0 }
	}

	function player(server) { return S.uuid == null ? null : server.getPlayerList().getPlayer(S.uuid) }

	function count(p) {
		var map = {}
		var inv = p.getInventory()
		for (var ii = 0; ii < inv.getContainerSize(); ii++) addStack(map, inv.getItem(ii), 0)
		for (var ci = 1; ci <= 4; ci++) addStack(map, p.inventoryMenu.getSlot(ci).getItem(), 0)
		addStack(map, p.containerMenu.getCarried(), 0)
		if (p.containerMenu !== p.inventoryMenu) addStack(map, p.inventoryMenu.getCarried(), 0)
		var chest = S.level.getBlockEntity(S.chestPos)
		if (chest != null) for (var si = 0; si < chest.getContainerSize(); si++) addStack(map, chest.getItem(si), 0)
		var box = new AABB(S.x - 8, S.y - 4, S.z - 8, S.x + 9, S.y + 8, S.z + 9)
		var ents = S.level.getEntitiesOfClass(ItemEntity, box)
		for (var ei = 0; ei < ents.size(); ei++) addStack(map, ents.get(ei).getItem(), 0)
		return map
	}

	function diff(a, b) {
		var out = []
		var keys = {}
		Object.keys(a).forEach(k => keys[k] = 1)
		Object.keys(b).forEach(k => keys[k] = 1)
		Object.keys(keys).sort().forEach(k => {
			var d = (b[k] || 0) - (a[k] || 0)
			if (d != 0) out.push((d > 0 ? '+' : '') + d + ' ' + k)
		})
		return out
	}

	function menuView(p) {
		var m = p.containerMenu
		var parts = []
		for (var mi = 0; mi < m.slots.size(); mi++) {
			var st = m.getSlot(mi).getItem()
			if (!st.isEmpty()) parts.push(mi + '=' + st.getCount() + 'x' + keyOf(st))
		}
		var car = m.getCarried()
		return 'menu=' + m.getClass().getSimpleName() + ' id=' + m.containerId + ' carried=' + (car.isEmpty() ? '-' : car.getCount() + 'x' + keyOf(car)) + ' ' + parts.join(' ')
	}

	function setup(p) {
		var lvl = p.getLevel()
		S.level = lvl
		S.uuid = p.getUuid()
		S.x = Math.floor(p.getX())
		S.z = Math.floor(p.getZ())
		S.y = 200
		var stone = Blocks.STONE.defaultBlockState()
		var glass = Blocks.GLASS.defaultBlockState()
		var air = Blocks.AIR.defaultBlockState()
		for (var dx = -5; dx <= 5; dx++) for (var dz = -5; dz <= 5; dz++) for (var dy = -1; dy <= 4; dy++) {
			var edge = Math.abs(dx) == 5 || Math.abs(dz) == 5
			var state = dy == -1 ? stone : (dy == 4 || edge) ? glass : air
			lvl.setBlock(new BlockPos(S.x + dx, S.y + dy, S.z + dz), state, 3)
		}
		S.chestPos = new BlockPos(S.x + 2, S.y, S.z)
		lvl.setBlock(S.chestPos, Blocks.CHEST.defaultBlockState(), 3)
		p.teleportTo(S.x + 0.5, S.y, S.z + 0.5)

		var chestItems = [
			['minecraft:cobblestone', 64], ['minecraft:cobblestone', 64], ['minecraft:cobblestone', 37],
			['minecraft:egg', 16], ['minecraft:egg', 9], ['minecraft:iron_ingot', 64], ['minecraft:iron_ingot', 12],
			['minecraft:diamond_sword', 1, '{Enchantments:[{id:"minecraft:sharpness",lvl:3s}]}'],
			['minecraft:diamond_sword', 1, '{Damage:100}'],
			['minecraft:potion', 1, '{Potion:"minecraft:swiftness"}'], ['minecraft:potion', 1, '{Potion:"minecraft:healing"}'],
			['minecraft:stone', 20, '{display:{Name:\'{"text":"Marked"}\'}}'],
			['minecraft:ender_pearl', 16], ['minecraft:ender_pearl', 3], ['minecraft:arrow', 64],
			['minecraft:golden_apple', 7], ['minecraft:white_shulker_box', 1], ['minecraft:oak_log', 50],
			['minecraft:torch', 64], ['minecraft:snowball', 11],
		]
		var chest = lvl.getBlockEntity(S.chestPos)
		chest.clearContent()
		for (var ki = 0; ki < chestItems.length; ki++) chest.setItem(ki, stackOf(chestItems[ki][0], chestItems[ki][1], chestItems[ki][2]))

		var inv = p.getInventory()
		inv.clearContent()
		p.containerMenu.setCarried(JItemStack.EMPTY)
		for (var gi = 1; gi <= 4; gi++) p.inventoryMenu.getSlot(gi).set(JItemStack.EMPTY)
		inv.setItem(0, stackOf('minecraft:dirt', 64))
		inv.setItem(1, stackOf('minecraft:dirt', 33))
		inv.setItem(2, stackOf('minecraft:stick', 64))
		inv.setItem(3, stackOf('minecraft:bread', 20))
		inv.setItem(4, stackOf('minecraft:egg', 5))
		inv.setItem(9, stackOf('minecraft:cobblestone', 10))
		inv.setItem(11, stackOf('minecraft:iron_pickaxe', 1))
		inv.setItem(12, stackOf('minecraft:white_shulker_box', 1, '{BlockEntityTag:{Items:[{Slot:0b,id:"minecraft:gravel",Count:32b}]}}'))
		inv.setItem(40, stackOf('minecraft:torch', 10))
		var cap = bpCap()
		if (cap != null) {
			var bp = stackOf('sophisticatedbackpacks:backpack', 1)
			var opt = bp.getCapability(cap).resolve()
			if (opt.isPresent()) {
				var w = opt.get()
				try { w.getUpgradeHandler().setStackInSlot(0, stackOf('sophisticatedbackpacks:stack_upgrade_tier_1', 1)) } catch (err) { log('no upgrade slot on a fresh backpack: ' + err) }
				w.getInventoryHandler().insertItem(0, stackOf('minecraft:cobblestone', 64), false)
				w.getInventoryHandler().insertItem(0, stackOf('minecraft:cobblestone', 36), false)
				w.getInventoryHandler().insertItem(1, stackOf('minecraft:egg', 12), false)
			}
			inv.setItem(10, bp)
		}
		var box = new AABB(S.x - 8, S.y - 4, S.z - 8, S.x + 9, S.y + 8, S.z + 9)
		var ents = lvl.getEntitiesOfClass(ItemEntity, box)
		for (var ei = 0; ents.size() > ei; ei++) ents.get(ei).discard()
		p.containerMenu.broadcastFullState()
		S.base = count(p)
		S.lastSig = ''
		S.deltas = 0
		log('SETUP at ' + S.x + ' ' + S.y + ' ' + S.z + ' baseline ' + JSON.stringify(S.base))
		return 1
	}

	ServerEvents.commandRegistry(event => {
		var C = event.commands
		event.register(C.literal('imrepro').requires(src => src.hasPermission(2))
			.then(C.literal('setup').executes(ctx => guarded('setup', () => setup(ctx.source.getPlayerOrException()))))
			.then(C.literal('setupfirst').executes(ctx => guarded('setupfirst', () => setup(ctx.source.getServer().getPlayerList().getPlayers().get(0)))))
			.then(C.literal('open').executes(ctx => {
				var p = ctx.source.getPlayerOrException()
				var be = p.getLevel().getBlockEntity(S.chestPos)
				if (be != null) p.openMenu(be)
				return 1
			}))
			.then(C.literal('mark').then(C.argument('label', StringArg.word()).executes(ctx => {
				S.mark = String(StringArg.getString(ctx, 'label'))
				log('MARK ' + S.mark + ' tick=' + S.tick)
				return 1
			})))
			.then(C.literal('lag').then(C.argument('ms', IntArg.integer(0, 1000)).executes(ctx => {
				S.lagMs = IntArg.getInteger(ctx, 'ms')
				log('LAG ' + S.lagMs + ' ms/tick')
				return 1
			})))
			.then(C.literal('check').then(C.argument('label', StringArg.word()).executes(ctx => {
				var p = ctx.source.getPlayerOrException()
				var lbl = String(StringArg.getString(ctx, 'label'))
				var now = count(p)
				if (S.base == null) { log('CHECK ' + lbl + ' no baseline'); return 1 }
				var d = diff(S.base, now)
				log('CHECK ' + lbl + ' tick=' + S.tick + ' ' + (d.length ? 'MISMATCH ' + d.join(', ') : 'OK'))
				log('SVIEW ' + lbl + ' ' + menuView(p))
				return 1
			})))
			.then(C.literal('bp').executes(ctx => guarded('bp', () => {
				var p = ctx.source.getServer().getPlayerList().getPlayers().get(0)
				var cap = bpCap()
				function summary(w) {
					var inv = w.getInventoryHandler()
					var parts = []
					for (var si = 0; si < inv.getSlots(); si++) { var st = inv.getStackInSlot(si); if (!st.isEmpty()) parts.push(si + '=' + st.getCount() + 'x' + keyOf(st)) }
					return 'slots=' + inv.getSlots() + ' [' + parts.join(' ') + ']'
				}
				var inv = p.getInventory()
				for (var ii = 0; ii < inv.getContainerSize(); ii++) {
					var st = inv.getItem(ii)
					if (String(ForgeRegistries.ITEMS.getKey(st.getItem())).indexOf('sophisticatedbackpacks:') != 0) continue
					var opt = st.getCapability(cap).resolve()
					log('BP inv slot ' + ii + ' nbt=' + st.getNbt() + ' ' + (opt.isPresent() ? summary(opt.get()) : 'no wrapper'))
				}
				var im = p.inventoryMenu
				var imParts = []
				for (var mi = 0; mi < im.slots.size(); mi++) { var mst = im.getSlot(mi).getItem(); if (!mst.isEmpty()) imParts.push(mi + '=' + mst.getCount() + 'x' + keyOf(mst)) }
				log('BP inventoryMenu ' + imParts.join(' ') + ' | open=' + menuView(p))
				for (var gi = 1; gi <= 4; gi++) {
					var gst = p.inventoryMenu.getSlot(gi).getItem()
					if (String(ForgeRegistries.ITEMS.getKey(gst.getItem())).indexOf('sophisticatedbackpacks:') != 0) continue
					var gopt = gst.getCapability(cap).resolve()
					log('BP craft slot ' + gi + ' nbt=' + gst.getNbt() + ' ' + (gopt.isPresent() ? summary(gopt.get()) : 'no wrapper'))
				}
				var m = p.containerMenu
				if (typeof m.getStorageWrapper == 'function') {
					var w = m.getStorageWrapper()
					var bpst = w.getBackpack()
					log('BP menu ' + m.getClass().getSimpleName() + ' id=' + m.containerId + ' ctx=' + m.getBackpackContext() + ' nbt=' + bpst.getNbt() + ' ' + summary(w))
				} else log('BP menu ' + m.getClass().getSimpleName() + ' (not a backpack menu)')
				return 1
			})))
			.then(C.literal('rebase').executes(ctx => {
				S.base = count(ctx.source.getPlayerOrException())
				S.lastSig = ''
				log('REBASE ' + JSON.stringify(S.base))
				return 1
			})))
	})

	ServerEvents.tick(event => {
		S.tick++
		if (S.lagMs > 0) {
			var end = Date.now() + S.lagMs
			while (Date.now() < end) { }
		}
		if (S.base == null) return
		var p = player(event.server)
		if (p == null) return
		// The test player never walks, so anything it throws would sit out of reach and despawn
		// after 6000 ticks (a false "loss"). Pull dropped items onto the player for normal pickup.
		var box = new AABB(S.x - 8, S.y - 4, S.z - 8, S.x + 9, S.y + 8, S.z + 9)
		var drops = S.level.getEntitiesOfClass(ItemEntity, box)
		for (var di = 0; di < drops.size(); di++) {
			var de = drops.get(di)
			if (Math.abs(de.getX() - p.getX()) > 0.5 || Math.abs(de.getZ() - p.getZ()) > 0.5) de.setPos(p.getX(), p.getY() + 0.2, p.getZ())
		}
		S.uuids = []
		var d = diff(S.base, count(p))
		var uu = S.uuids.sort().join(' ')
		S.uuids = null
		if (uu != S.lastUuids) {
			log('BPUUID tick=' + S.tick + ' mark=' + S.mark + ' ' + (S.lastUuids == null ? '' : S.lastUuids) + ' -> ' + uu + ' | ' + menuView(p))
			S.lastUuids = uu
		}
		var sig = d.join(', ')
		if (sig != S.lastSig) {
			S.lastSig = sig
			if (d.length) S.deltas++
			log('DELTA tick=' + S.tick + ' mark=' + S.mark + ' ' + (d.length ? sig : 'back to baseline') + ' | ' + menuView(p))
		}
	})
})()
