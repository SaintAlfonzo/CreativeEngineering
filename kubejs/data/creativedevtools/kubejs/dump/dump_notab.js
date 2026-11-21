// requires: creativedevtools
// SPDX-License-Identifier: GPL-3.0-only
// /dumpnotab   ->  writes kubejs/exported/items/no_tab.json
//
//   Every item with no creative tab (so never in creative mode, its search or JEI), and every item on Creative Common's
//   [creative_menu] orphanTabItems list with the tab it was given (jei/jei-coverage #29, util/OrphanTabs). Fields per
//   item: id, listed (on the allow list), tab (recipe folder name of the tab it has now, null = hidden), edible, block.
//
// tools/no_tab_report.py reads it with the crafting audit and the loot dumps and writes the "Items with no creative
// tab" section of feature_writeup/jei_coverage.md.

ServerEvents.commandRegistry(event => {
	var { commands: Commands } = event
	event.register(
		Commands.literal('dumpnotab')
			.requires(src => src.hasPermission(2))
			.executes(ctx => {
				var g = guardThrowable(() => dumpNoTab())
				if (g.ok) {
					var r = g.value
					ctx.source.sendSuccess(Text.green(`Dumped ${r.hidden} hidden and ${r.listed} listed orphan items to kubejs/exported/items/no_tab.json`), false)
				} else {
					console.error('[dumpnotab] failed: ' + g.error)
					ctx.source.sendFailure(Text.red('dumpnotab failed - see log: ' + g.error))
				}
				return 1
			})
	)
})

function dumpNoTab() {
	var $Reg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
	var $BlockItem = Java.loadClass('net.minecraft.world.item.BlockItem')
	var $OrphanTabs = Java.loadClass('com.SaintAlfonzo.creativecommon.util.OrphanTabs')
	var rows = []
	var hidden = 0
	var listed = 0
	var total = 0
	var it = $Reg.ITEMS.getValues().iterator()
	while (it.hasNext()) {
		var item = it.next()
		total++
		var id = String($Reg.ITEMS.getKey(item))
		if (id === 'minecraft:air') continue
		var isListed = !!$OrphanTabs.isListed(item)
		var tab = null
		guardThrowable(() => {
			var t = item.getItemCategory()
			if (t != null) tab = String(t.getRecipeFolderName())
		})
		if (tab !== null && !isListed) continue
		if (tab === null) hidden++
		if (isListed) listed++
		var edible = false
		guardThrowable(() => { edible = !!item.isEdible() })
		rows.push({ id: id, listed: isListed, tab: tab, edible: edible, block: item instanceof $BlockItem })
	}
	rows.sort((a, b) => a.id < b.id ? -1 : a.id > b.id ? 1 : 0)
	writeExportJson('kubejs/exported/items/no_tab.json', { total: total, hidden: hidden, listed: listed, items: rows })
	return { hidden: hidden, listed: listed }
}
