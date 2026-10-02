// Item-loss repro harness, CLIENT half. Copied into a throwaway client's kubejs/client_scripts by
// tools/item_repro/setup.sh; never part of the pack. See tools/item_repro/README.md.
//
// Once the player is op on the scratch server, it runs `/imrepro setup` and then rounds of
// seeded random inventory actions (the same clicks a screen, Mouse Tweaks or a sort button would
// send), with the server half counting items every tick. Settings are re-read from
// kubejs/imrepro.json at the start of every round, so latency phases can be switched live.

(function () {
	var ClickType = Java.loadClass('net.minecraft.world.inventory.ClickType')
	var AbstractContainerMenu = Java.loadClass('net.minecraft.world.inventory.AbstractContainerMenu')
	var ForgeRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')

	var C = { ticks: 0, state: 'wait', wait: 0, round: 0, burst: 0, cfg: null, seed: 1, setupDone: false, actions: 0 }

	function log(s) { console.info('[imrepro] ' + s) }

	function rnd() {
		C.seed = (C.seed * 1103515245 + 12345) % 2147483648
		return C.seed / 2147483648
	}
	function pick(n) { return Math.floor(rnd() * n) }

	function cmd(s) {
		var p = Client.player
		if (!p.commandUnsigned(s)) log('command not sent: ' + s)
	}

	function readCfg() {
		var def = { reload: 0, seed: 1, rounds: 1000000, burstTicks: 40, settleTicks: 60, minActions: 1, maxActions: 6, phase: 'p0', stop: false, mods: true, logActions: true }
		try {
			var j = JsonIO.read('kubejs/imrepro.json')
			if (j != null) Object.keys(def).forEach(k => { if (j[k] !== undefined && j[k] !== null) def[k] = j[k] })
		} catch (err) { log('cfg read failed: ' + err) }
		def.burstTicks = Number(def.burstTicks); def.settleTicks = Number(def.settleTicks)
		def.minActions = Number(def.minActions); def.maxActions = Number(def.maxActions); def.rounds = Number(def.rounds)
		def.reload = Number(def.reload); def.phase = String(def.phase); def.stop = def.stop == true; def.mods = def.mods == true; def.logActions = def.logActions == true
		return def
	}

	function keyOf(st) {
		var id = String(ForgeRegistries.ITEMS.getKey(st.getItem()))
		if (id.indexOf('shulker_box') >= 0 || id.indexOf('backpack') >= 0) return id
		var tag = st.getNbt()
		return tag == null ? id : id + String(tag)
	}

	function view() {
		var m = Client.player.containerMenu
		var parts = []
		for (var mi = 0; mi < m.slots.size(); mi++) {
			var st = m.getSlot(mi).getItem()
			if (!st.isEmpty()) parts.push(mi + '=' + st.getCount() + 'x' + keyOf(st))
		}
		var car = m.getCarried()
		return 'menu=' + m.getClass().getSimpleName() + ' id=' + m.containerId + ' carried=' + (car.isEmpty() ? '-' : car.getCount() + 'x' + keyOf(car)) + ' ' + parts.join(' ')
	}

	function act(desc) {
		if (C.cfg && C.cfg.logActions) log('ACT ' + Date.now() + ' r' + C.round + ' menu=' + Client.player.containerMenu.containerId + ' ' + desc)
	}

	function click(slot, button, type) {
		var p = Client.player
		var m = p.containerMenu
		var st = slot >= 0 && slot < m.slots.size() ? m.getSlot(slot).getItem() : null
		act(type + ' slot=' + slot + ' btn=' + button + ' item=' + (st == null || st.isEmpty() ? '-' : st.getCount() + 'x' + String(ForgeRegistries.ITEMS.getKey(st.getItem()))) + ' carried=' + (m.getCarried().isEmpty() ? '-' : m.getCarried().getCount() + 'x' + String(ForgeRegistries.ITEMS.getKey(m.getCarried().getItem()))))
		Client.gameMode.handleInventoryMouseClick(m.containerId, slot, button, type, p)
		C.actions++
	}

	function randomSlot(m) {
		// Slot 0 of the player inventory menu is the crafting result: crafting would turn items
		// into other items and break the item totals, so it is never clicked.
		var n = m.slots.size()
		if (m.containerId == 0) return 1 + pick(n - 1)
		return pick(n)
	}

	function sendMod(kind) {
		act('MOD ' + kind)
		try {
			if (kind == 'quarkSort') {
				var QN = Java.loadClass('vazkii.quark.base.network.QuarkNetwork')
				var SortMsg = Java.loadClass('vazkii.quark.base.network.message.SortInventoryMessage')
				QN.sendToServer(new SortMsg(rnd() < 0.5))
			} else if (kind == 'quarkTransfer') {
				var QN2 = Java.loadClass('vazkii.quark.base.network.QuarkNetwork')
				var TrMsg = Java.loadClass('vazkii.quark.base.network.message.InventoryTransferMessage')
				QN2.sendToServer(new TrMsg(rnd() < 0.5, rnd() < 0.5))
			} else if (kind == 'backpackOpen') {
				var SBP = Java.loadClass('net.p3pp3rf1y.sophisticatedbackpacks.network.SBPPacketHandler')
				var OpenMsg = Java.loadClass('net.p3pp3rf1y.sophisticatedbackpacks.network.BackpackOpenMessage')
				SBP.INSTANCE.sendToServer(new OpenMsg())
			} else if (kind == 'backpackSort') {
				var SBP2 = Java.loadClass('net.p3pp3rf1y.sophisticatedcore.network.PacketHandler')
				var DataMsg = Java.loadClass('net.p3pp3rf1y.sophisticatedcore.network.SyncContainerClientDataMessage')
				var TagParser = Java.loadClass('net.minecraft.nbt.TagParser')
				SBP2.INSTANCE.sendToServer(new DataMsg(TagParser.parseTag('{action:"sort"}')))
			}
			C.actions++
		} catch (err) { log('mod action ' + kind + ' failed: ' + err) }
	}

	// LocalPlayer.closeContainer is hidden from JS (both names), so close the way the screen's own
	// Escape handler does; fall back to the player methods in case a KubeJS version exposes them.
	function closeScreen() {
		var p = Client.player
		var how = null
		if (typeof p.closeContainer == 'function') { p.closeContainer(); how = 'player.closeContainer' }
		else if (typeof p.m_6915_ == 'function') { p.m_6915_(); how = 'player.m_6915_' }
		else if (Client.screen != null && typeof Client.screen.onClose == 'function') { Client.screen.onClose(); how = 'screen.onClose' }
		else if (Client.screen != null && typeof Client.screen.m_7379_ == 'function') { Client.screen.m_7379_(); how = 'screen.m_7379_' }
		act('CLOSE via ' + how)
		C.actions++
	}

	function randomAction() {
		var m = Client.player.containerMenu
		var r = rnd()
		if (r < 0.30) click(randomSlot(m), 0, ClickType.PICKUP)
		else if (r < 0.45) click(randomSlot(m), 1, ClickType.PICKUP)
		else if (r < 0.60) click(randomSlot(m), 0, ClickType.QUICK_MOVE)
		else if (r < 0.70) click(randomSlot(m), rnd() < 0.2 ? 40 : pick(9), ClickType.SWAP)
		else if (r < 0.74) click(randomSlot(m), 0, ClickType.PICKUP_ALL)
		else if (r < 0.80) {
			// drag-distribute: start, 2-5 slots, end (left = split evenly, right = one each)
			var type = pick(2)
			click(-999, AbstractContainerMenu.getQuickcraftMask(0, type), ClickType.QUICK_CRAFT)
			var k = 2 + pick(4)
			for (var qi = 0; qi < k; qi++) click(randomSlot(m), AbstractContainerMenu.getQuickcraftMask(1, type), ClickType.QUICK_CRAFT)
			click(-999, AbstractContainerMenu.getQuickcraftMask(2, type), ClickType.QUICK_CRAFT)
		}
		else if (r < 0.82) click(randomSlot(m), pick(2), ClickType.THROW)
		else if (r < 0.83) click(-999, 0, ClickType.PICKUP)
		else if (r < 0.86) closeScreen()
		else if (r < 0.89) { act('REOPEN'); cmd('imrepro open'); C.actions++ }
		else if (!C.cfg.mods) click(randomSlot(m), 0, ClickType.PICKUP)
		else if (r < 0.93) sendMod('quarkSort')
		else if (r < 0.95) sendMod('quarkTransfer')
		else if (r < 0.97) sendMod('backpackOpen')
		else sendMod('backpackSort')
	}

	ClientEvents.tick(event => {
		try {
			C.raw = (C.raw || 0) + 1
			if (C.raw % 100 == 0) {
				var rc = readCfg().reload
				if (C.reloadSeen == null) C.reloadSeen = rc
				else if (rc > C.reloadSeen) { C.reloadSeen = rc; log('RELOAD requested'); Client.reloadResourcePacks(); return }
			}
			if (C.raw % 600 == 1) log('HEARTBEAT raw=' + C.raw + ' ticks=' + C.ticks + ' state=' + C.state + ' player=' + (Client.player != null) + ' level=' + (Client.level != null) + ' perm=' + (Client.player != null && Client.player.hasPermissions(2)))
			var p = Client.player
			if (p == null) { C.ticks = 0; return }
			C.ticks++
			if (C.wait > 0) { C.wait--; return }

			if (C.state == 'wait') {
				if (C.ticks < 100 || !p.hasPermissions(2)) return
				C.cfg = readCfg()
				C.seed = C.cfg.seed
				log('START seed=' + C.seed)
				cmd('imrepro setup')
				C.state = 'open'
				C.wait = 40
			} else if (C.state == 'open') {
				C.cfg = readCfg()
				if (C.cfg.stop || C.round >= C.cfg.rounds) {
					log('STOPPED after ' + C.round + ' rounds, ' + C.actions + ' actions')
					C.state = 'done'
					return
				}
				C.round++
				cmd('imrepro mark ' + C.cfg.phase + '-r' + C.round)
				if (p.containerMenu.containerId == 0) cmd('imrepro open')
				C.burst = C.cfg.burstTicks
				C.state = 'burst'
				C.wait = 10
			} else if (C.state == 'burst') {
				var n = C.cfg.minActions + pick(C.cfg.maxActions - C.cfg.minActions + 1)
				for (var ai = 0; ai < n; ai++) randomAction()
				if (--C.burst <= 0) {
					// Put the cursor stack back down and let everything settle before comparing views.
					C.state = 'settle'
					C.wait = C.cfg.settleTicks
				}
			} else if (C.state == 'settle') {
				var lbl = C.cfg.phase + '-r' + C.round
				log('CVIEW ' + lbl + ' ' + view())
				cmd('imrepro check ' + lbl)
				C.state = 'open'
				C.wait = 20
			}
		} catch (err) {
			log('tick error: ' + err)
			C.wait = 20
		}
	})
})()
