// Creative Compat; moved from kubejs/server_scripts/other/starter_kit.js on 2026-11-01.
// Starter Kit port (feature_writeup/feature_porting.md 1, Starter Kit 7.0 + Collective gear functions, jar retired
// 2026-10-20). A player joining a world for the first time gets one of the kits below at random, 10 ticks after the
// join (as the mod: books other mods hand out on join exist by then and are given back after the kit is set).
//
// Who already has one (the mod's own records, so nobody gets a second kit):
//   - scoreboard tag collective.firstJoin.starterkit (the mod's marker; the port adds it too)
//   - <world>/data/starterkit/tracking.json: {"singleplayer": {<world folder>: true}, "multiplayer": {<uuid>: true}}.
//     The mod wrote it on every start, marking every player with a playerdata file, so on the live server every
//     existing player is in it. The port keeps writing the same file.
// Kit = the mod's two active kits (config/starterkit/kits/Default.txt, Engineer.txt), slots as the mod stored them:
// the inventory is emptied, kit slots set, then whatever the player held is given back (addExistingItemsAfterKitSet).
// Not ported: the kit-choice GUI (randomizeMultipleKitsToggle was on, so it never showed), effects (both kits had
// none), the "select hotbar slot 1" packet, /sk commands. Contents changes are TODO items/starter-kit #1.

var STARTER_KITS = {
	marker: 'collective.firstJoin.starterkit',
	delayTicks: 10,
	kits: [
		{ name: 'Default', feet: ['leather_boots'], offhand: ['shield'], slots: { 0: ['wooden_sword'], 1: ['minecraft_bread', 9] } },
		{ name: 'Engineer', feet: ['leather_boots'], offhand: ['ce_copper_shield'], slots: { 0: ['copper_pickaxe'], 1: ['copper_axe'], 2: ['minecraft_bread', 8] } },
	],
}

PlayerEvents.loggedIn(e => {
	var player = e.player
	var server = e.server
	if (!player || !server || player.level.isClientSide()) return
	if (player.getTags().contains(STARTER_KITS.marker)) return

	var $LevelResource = Java.loadClass('net.minecraft.world.level.storage.LevelResource')
	var worldRoot = server.getWorldPath($LevelResource.ROOT).toAbsolutePath().normalize()
	// java.io/nio are class-filtered: no loadClass, JsonIO + instance methods only. Path.resolve has a Path and a String
	// overload that Rhino can't choose between, so the String one is named; KubeJS 1902's JsonIO takes a Path.
	var trackingPath = worldRoot['resolve(java.lang.String)']('data/starterkit/tracking.json')
	var trackingFile = trackingPath.toFile()
	var dedicated = server.isDedicated() // KubeJS's name: Rhino can't see MinecraftServer.isDedicatedServer on DedicatedServer
	// Entity UUID getters are hidden by KubeJS; the profile id is the same uuid
	var key = dedicated ? String(player.getGameProfile().getId()) : String(worldRoot.getFileName())
	var env = dedicated ? 'multiplayer' : 'singleplayer'

	var tracking = { singleplayer: {}, multiplayer: {} }
	if (trackingFile.exists()) {
		try {
			var parsed = JSON.parse(String(JsonIO.readString(trackingPath)))
			if (parsed && typeof parsed === 'object') {
				tracking.singleplayer = parsed.singleplayer || {}
				tracking.multiplayer = parsed.multiplayer || {}
			}
		} catch (err) {
			console.warn('[starter-kit] could not read ' + trackingFile + ': ' + err)
		}
	}
	if (tracking[env][key] === true) {
		player.addTag(STARTER_KITS.marker) // the mod's record, now also on the player
		return
	}

	server.scheduleInTicks(STARTER_KITS.delayTicks, () => {
		if (player.isRemoved() || player.getTags().contains(STARTER_KITS.marker)) return
		var i = global.item
		var $EquipmentSlot = Java.loadClass('net.minecraft.world.entity.EquipmentSlot')
		var kit = STARTER_KITS.kits[Math.floor(Math.random() * STARTER_KITS.kits.length)]
		var stack = spec => {
			var id = i[spec[0]]
			if (typeof id !== 'string') { console.error('[starter-kit] no item_dict key ' + spec[0]); return null }
			return Item.of(id, spec.length > 1 ? spec[1] : 1)
		}
		var inv = player.getInventory()
		var keep = []
		for (var n = 0; n < 36; n++) {
			var held = inv.getItem(n)
			if (!held.isEmpty()) keep.push(held.copy())
		}
		player.removeAllEffects()
		inv.clearContent()
		Object.keys(kit.slots).forEach(slot => { var s = stack(kit.slots[slot]); if (s) inv.setItem(Number(slot), s) })
		;[['head', 'HEAD'], ['chest', 'CHEST'], ['legs', 'LEGS'], ['feet', 'FEET'], ['offhand', 'OFFHAND']].forEach(pair => {
			if (kit[pair[0]]) { var s = stack(kit[pair[0]]); if (s) player.setItemSlot($EquipmentSlot[pair[1]], s) }
		})
		keep.forEach(s => player.give(s))
		player.addTag(STARTER_KITS.marker)

		tracking[env][key] = true
		try {
			trackingFile.getParentFile().mkdirs()
			JsonIO.write(trackingPath, tracking)
		} catch (err) {
			console.warn('[starter-kit] could not write ' + trackingFile + ': ' + err)
		}
		console.log('[starter-kit] ' + kit.name + ' kit given to ' + player.getName().getString() + ' (' + keep.length + ' stacks given back)')
	})
})
