// Applies global.armorEffects (startup_scripts/func/armor_effects.js) piece()/set()
// effects every tick to whichever players have the matching armor equipped, and its
// mount() effects to whichever horses have the matching horse armor equipped.
// Tooltip text for the same data lives in client_scripts/armor_effects_tooltip.js.
//
// Each matching effect is (re)applied every tick as a short, continuously-refreshed
// MobEffectInstance — same "while worn, effectively infinite" idiom Create: Deep
// Dark's own echo-armor set bonus uses (EchoArmorEffectProcedure, reapplied every
// tick at a fixed 60-tick duration — see patcherMod's EchoArmorEffectProcedureMixin).
// ambient=false, visible=false by default (spec.visible: true opts back in) so the
// bonus doesn't spam a HUD potion icon — the tooltip is the intended indicator,
// same as CDD's own hidden set-bonus effects.

const $MobEffectInstance = Java.loadClass('net.minecraft.world.effect.MobEffectInstance')
const $EquipmentSlot = Java.loadClass('net.minecraft.world.entity.EquipmentSlot')
const $AbstractHorse = Java.loadClass('net.minecraft.world.entity.animal.horse.AbstractHorse')

const ARMOR_SLOTS = [$EquipmentSlot.HEAD, $EquipmentSlot.CHEST, $EquipmentSlot.LEGS, $EquipmentSlot.FEET]
const REFRESH_TICKS = 40 // 2 sec — reapplied every tick, just a buffer against a brief server hiccup

// AbstractHorse.EQUIPMENT_SLOT_OFFSET (400) + AbstractHorse.INV_SLOT_ARMOR (1) — the
// Container index Entity#getSlot(int) maps to the horse's own armor item. There's no
// public getArmor()-style getter on AbstractHorse (the backing `inventory` field is
// protected), but getSlot(int)/SlotAccess is the same public API "/item replace entity"
// uses, so it's stable across mods without needing a mixin.
const HORSE_ARMOR_SLOT = 401

// patcherMod's CEConfig, for perf.mountEffectIntervalTicks (how often the horse pass below runs).
// Null if the mod isn't on the classpath; the pass then uses MOUNT_INTERVAL_DEFAULT.
const $ArmorFxCEConfig = (function () {
	try {
		return Java.loadClass('com.SaintAlfonzo.creativeengineering.CEConfig')
	} catch (err) {
		return null
	}
})()
const MOUNT_INTERVAL_DEFAULT = 100

function applyEffects(target, effects, durationTicks) {
	effects.forEach(spec => {
		var me = global.armorEffects._resolveEffect(spec.effect)
		if (!me) return
		target.addEffect(new $MobEffectInstance(me, durationTicks || REFRESH_TICKS, spec.amplifier || 0, false, spec.visible === true))
	})
}

PlayerEvents.tick(event => {
	var player = event.getPlayer()
	if (!player || player.getLevel().isClientSide()) return

	var AE = global.armorEffects
	if (!AE) return

	var worn = []
	ARMOR_SLOTS.forEach(slot => {
		try {
			var stack = player.getItemBySlot(slot)
			if (stack && !stack.isEmpty()) worn.push(String(stack.getItem().getId()))
		} catch (err) {}
	})
	if (!worn.length) return

	Object.keys(AE.PIECES).forEach(itemId => {
		if (worn.indexOf(itemId) !== -1) applyEffects(player, AE.PIECES[itemId])
	})

	AE.SETS.forEach(group => {
		if (group.ids.every(id => worn.indexOf(id) !== -1)) applyEffects(player, group.effects)
	})
})

// global.armorEffects.mount() entries (e.g. dragon_horse_armor) — worn by an
// AbstractHorse in ITS OWN inventory, not a player armor slot, so PlayerEvents.tick
// above can never see it. Walks every horse in the level instead.
//
// Not every tick: getAllEntities() over a whole level every tick was a measured server
// cost (feature_writeup/perf.md section 7), so the walk runs once per
// CEConfig perf.mountEffectIntervalTicks (default 100) and each application lasts
// interval + REFRESH_TICKS, so the effect never lapses between passes. Newly equipped
// armor starts working within one interval.
//
// One ServerEvents.tick, not LevelEvents.tick: the per-level handler ran once per
// dimension every tick (16 calls, perf.md 10.2 #6). The interval check comes first, and
// dimensions with no player in them are skipped.
ServerEvents.tick(event => {
	var AE = global.armorEffects
	if (!AE || !Object.keys(AE.MOUNTS).length) return

	var server = event.getServer()
	var interval = $ArmorFxCEConfig ? $ArmorFxCEConfig.PERF.mountEffectIntervalTicks.get() : MOUNT_INTERVAL_DEFAULT
	if (server.getTickCount() % interval !== 0) return

	server.getAllLevels().forEach(level => {
		if (level.getPlayers().isEmpty()) return
		level.getAllEntities().forEach(entity => {
			if (!(entity instanceof $AbstractHorse)) return
			var armorStack = entity.getSlot(HORSE_ARMOR_SLOT).get()
			if (!armorStack || armorStack.isEmpty()) return
			var effects = AE.MOUNTS[String(armorStack.getItem().getId())]
			if (effects && effects.length) applyEffects(entity, effects, interval + REFRESH_TICKS)
		})
	})
})
