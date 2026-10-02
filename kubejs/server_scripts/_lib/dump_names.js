// /dumpnames  ->  writes kubejs/exported/names.json: every registry entry whose name the game can't
// translate, so JEI / WTHIT / chat show the raw key. Read by tools/missing_names.py, which turns it
// into the catalog in feature_writeup/names.md.
//
// Uses each entry's real getDescriptionId() (mods that build their own key, like Alex's Mobs effects or
// Lightman's Currency blocks, are judged correctly) and asks Language.getInstance().has(key). Run it in
// single player: there Language is the client's, with every mod, resource pack and KubeJS lang loaded.
// On a dedicated server only vanilla + mod jar lang is loaded, so the file is marked dedicated: true.
//
// Output: { dedicated, totals: {kind: n}, missing: {kind: [ {id, key, ...} ]} }
//   block    + hasItem (false = partial block: crop, potted plant, placed food...), props (state properties)
//   item, effect, enchantment, entity, fluid, profession
//   potion   + forms: which of potion / splash / lingering / arrow lack a key

;(function () {
	function dumpNames(server) {
		var $FR = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
		var $Language = Java.loadClass('net.minecraft.locale.Language')
		var lang = $Language.getInstance()
		var totals = {}
		var missing = {}

		function has(key) {
			try { return lang.has(key) } catch (err) { return false }
		}

		// kind: output group; reg: Forge registry; keyOf(value) -> description id; extra(value, row) adds fields
		function walk(kind, reg, keyOf, extra) {
			var rows = []
			var n = 0
			var keys = []
			reg.getKeys().forEach(rl => keys.push([String(rl), rl]))
			keys.sort((a, b) => a[0] < b[0] ? -1 : a[0] > b[0] ? 1 : 0)
			var i, id, value, key, row
			for (i = 0; i < keys.length; i++) {
				id = keys[i][0]
				value = reg.getValue(keys[i][1])
				if (!value) continue
				key = null
				try { key = String(keyOf(value, id)) } catch (err) { key = null }
				if (key === null) continue
				n++
				if (has(key)) continue
				row = { id: id, key: key }
				if (extra) { try { extra(value, row) } catch (err) {} }
				rows.push(row)
			}
			totals[kind] = n
			missing[kind] = rows
		}

		walk('block', $FR.BLOCKS, b => b.getDescriptionId(), (b, row) => {
			row.hasItem = String($FR.ITEMS.getKey(b.asItem())) !== 'minecraft:air'
			var props = []
			b.getStateDefinition().getProperties().forEach(p => props.push(String(p.getName())))
			row.props = props
		})
		walk('item', $FR.ITEMS, it => it.getDescriptionId())
		walk('effect', $FR.MOB_EFFECTS, e => e.getDescriptionId())
		walk('enchantment', $FR.ENCHANTMENTS, e => e.getDescriptionId())
		walk('entity', $FR.ENTITY_TYPES, t => t.getDescriptionId())
		walk('fluid', $FR.FLUIDS, (f, id) => id === 'minecraft:empty' ? null : f.getFluidType().getDescriptionId())
		// Forge's Villager.getTypeName: entity.minecraft.villager.<path>, or <ns>:<path> outside minecraft
		walk('profession', $FR.VILLAGER_PROFESSIONS, (p, id) => {
			if (id === 'minecraft:none') return null
			var ns = id.substring(0, id.indexOf(':'))
			return 'entity.minecraft.villager.' + (ns === 'minecraft' ? id.substring(id.indexOf(':') + 1) : id)
		})

		// Potions: one row per potion that lacks any of its four item names.
		var forms = [
			['potion', 'item.minecraft.potion.effect.'],
			['splash', 'item.minecraft.splash_potion.effect.'],
			['lingering', 'item.minecraft.lingering_potion.effect.'],
			['arrow', 'item.minecraft.tipped_arrow.effect.'],
		]
		var potRows = []
		var potN = 0
		$FR.POTIONS.getKeys().forEach(rl => {
			var id = String(rl)
			var potion = $FR.POTIONS.getValue(rl)
			if (!potion || id === 'minecraft:empty') return
			potN++
			var lacking = []
			var j
			for (j = 0; j < forms.length; j++) {
				if (!has(String(potion.getName(forms[j][1])))) lacking.push(forms[j][0])
			}
			if (lacking.length) potRows.push({ id: id, key: String(potion.getName(forms[0][1])), forms: lacking })
		})
		potRows.sort((a, b) => a.id < b.id ? -1 : a.id > b.id ? 1 : 0)
		totals.potion = potN
		missing.potion = potRows

		var dedicated = false
		try { dedicated = server.isDedicatedServer() } catch (err) {}
		writeExportJson('kubejs/exported/names.json', { dedicated: dedicated, totals: totals, missing: missing })

		var summary = Object.keys(missing).map(k => `${k} ${missing[k].length}/${totals[k]}`).join(', ')
		console.log(`[dumpnames] missing names: ${summary}`)
		return { summary: summary, dedicated: dedicated }
	}

	ServerEvents.commandRegistry(event => {
		const { commands: Commands } = event
		event.register(
			Commands.literal('dumpnames')
				.requires(src => src.hasPermission(2))
				.executes(ctx => {
					const r = dumpNames(ctx.source.getServer())
					ctx.source.sendSuccess(Text.green(`[dumpnames] missing: ${r.summary} -> kubejs/exported/names.json`), false)
					if (r.dedicated) {
						ctx.source.sendFailure(Text.yellow('[dumpnames] dedicated server: resource-pack and KubeJS client lang are not loaded here, so this over-reports. Run it in single player.'))
					}
					return 1
				})
		)
	})
})()
