// requires: creativedevtools
// /dumppotions  ->  writes two files under kubejs/exported/, both grouped by mod namespace:
//
//   mob_effects.json   every MobEffect:  { ns: { "ns:id": {name, category, instant, color} } }
//   potions.json       every brewable Potion (Registry.POTION), with the effect(s) it applies:
//                      { ns: { "ns:id": {name, effects:[{effect,duration,amplifier}]} } }
//
// Also callable from other scripts:  dumpPotions()

ServerEvents.commandRegistry(event => {
	const { commands: Commands } = event

	event.register(
		Commands.literal('dumppotions')
			.requires(src => src.hasPermission(2))
			.executes(ctx => {
				const r = dumpPotions()
				ctx.source.sendSuccess(
					Text.green(`Dumped ${r.effects} mob effects + ${r.potions} potions to kubejs/exported/`), false)
				return 1
			})
	)
})

function dumpPotions() {
	const ForgeRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')

	// ── MobEffects ───────────────────────────────────────────────────────────
	const effReg = ForgeRegistries.MOB_EFFECTS
	const effByMod = {}
	let effCount = 0

	effReg.getKeys().forEach(rl => {
		const id = rl.toString()
		const ns = id.substring(0, id.indexOf(':'))
		const eff = effReg.getValue(rl)
		if (!eff) return

		let name = id
		try { name = eff.getDisplayName().getString() } catch (err) {}

		let category = 'NEUTRAL'
		try { category = eff.getCategory().toString() } catch (err) {}

		let color = 0
		try { color = eff.getColor() } catch (err) {}

		let instant = false
		try { instant = eff.isInstantenous() } catch (err) {}

		;(effByMod[ns] = effByMod[ns] || {})[id] = {
			name: name,
			category: category,
			instant: instant,
			color: '#' + ('000000' + (color >>> 0).toString(16)).slice(-6)
		}
		effCount++
	})

	// ── Potions (brewable) ───────────────────────────────────────────────────
	const potReg = ForgeRegistries.POTIONS
	const potByMod = {}
	let potCount = 0

	potReg.getKeys().forEach(rl => {
		const id = rl.toString()
		const ns = id.substring(0, id.indexOf(':'))
		const potion = potReg.getValue(rl)
		if (!potion) return

		const effects = []
		// Rhino quirk (same as potions.js's postInit loop): const/let inside a nested
		// block re-entered on every forEach call throws "redeclaration of var" - hoist
		// to function-scoped var, assign inside.
		var it, mei, eid
		try {
			it = potion.getEffects().iterator()
			while (it.hasNext()) {
				mei = it.next()
				eid = 'unknown'
				try { eid = effReg.getKey(mei.getEffect()).toString() } catch (err) {}
				effects.push({
					effect: eid,
					duration: mei.getDuration(),
					amplifier: mei.getAmplifier()
				})
			}
		} catch (err) { console.warn(`[dumppotions] getEffects() failed for ${id}: ${err}`) }

		let name = id
		try { name = String(potion.getName('')) } catch (err) {}

		;(potByMod[ns] = potByMod[ns] || {})[id] = { name: name, effects: effects }
		potCount++
	})

	// ── sort + write ─────────────────────────────────────────────────────────
	const sortByMod = obj => {
		const out = {}
		Object.keys(obj).sort().forEach(ns => {
			const inner = {}
			Object.keys(obj[ns]).sort().forEach(k => { inner[k] = obj[ns][k] })
			out[ns] = inner
		})
		return out
	}

	writeExportJson('kubejs/exported/mob_effects.json', sortByMod(effByMod))
	writeExportJson('kubejs/exported/potions.json', sortByMod(potByMod))

	console.log(`[dumppotions] wrote ${effCount} mob effects (${Object.keys(effByMod).length} mods) ` +
	            `+ ${potCount} potions (${Object.keys(potByMod).length} mods)`)

	return { effects: effCount, potions: potCount }
}
