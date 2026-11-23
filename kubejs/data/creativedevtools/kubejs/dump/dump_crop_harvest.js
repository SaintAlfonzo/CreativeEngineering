// requires: creativedevtools
// SPDX-License-Identifier: GPL-3.0-only
// /dumpcropharvest   ->  writes kubejs/exported/crops/crop_harvest_audit.json
//
//   How every growing block is harvested (farming/crops #3, briefs/farming_crops.md crops #3). Same block walk as
//   /dumpcropgrowth (cropGrowthWalk in dump_crop_growth.js), plus any block Quark lists that the walk misses
//   (source "quark"). Per block:
//     quark            replant (a Simple Harvest ripe state is this block), click (in rightClickCrops; Quark and the
//                      hoe area right-click it instead of replanting), replantBlock (in Quark's cropBlocks: the
//                      block of some entry's replant state), blacklisted (#quark:simple_harvest_blacklisted)
//     ripeStates       Quark's "ripe -> replant" pairs for this block
//     parity / ownPick / fromBottom   #creativedelight:crops/farmland_crops, crops/own_pick, crops/harvest_from_bottom
//     season           the creativecalendar:crop_seasons/<type> tags it is in
//     ownSoilCheck     non-vanilla classes in its chain that declare canSurvive / mayPlaceOn (those may skip Forge's
//                      canSustainPlant, so Rich Farmland or Fertile Soil can refuse them; a flag, not a placement test)
//     wthitNative, classes, props, item   from the growth walk
//   Checked by tools/crop_harvest_coverage.py against tools/crop_harvest_rules.json.
//
// Quark fills its lists in configChanged(), so this runs after the world loads (a command always does).

ServerEvents.commandRegistry(event => {
	var { commands: Commands } = event
	event.register(
		Commands.literal('dumpcropharvest')
			.requires(src => src.hasPermission(2))
			.executes(ctx => {
				var g = guardThrowable(() => dumpCropHarvest())
				if (g.ok) {
					var r = g.value
					ctx.source.sendSuccess(Text.green(`Dumped ${r.listed} crop blocks (${r.replant} Quark replant, ${r.click} Quark click${r.quark ? '' : ', Quark not read: ' + r.quarkError}) to kubejs/exported/crops/`), false)
				} else {
					console.error('[dumpcropharvest] failed: ' + g.error)
					ctx.source.sendFailure(Text.red('dumpcropharvest failed - see log: ' + g.error))
				}
				return 1
			})
	)
})

function dumpCropHarvest() {
	var $Probe = Java.loadClass('com.SaintAlfonzo.creativedevtools.dump.CropHarvestProbe')
	var probe = $Probe.snapshot()
	var toArr = list => { var out = []; for (var i = 0; i < list.size(); i++) out.push(String(list.get(i))); return out }

	var describe = (block, base) => {
		var tags = toArr($Probe.tags(block))
		var ripe = toArr(probe.quarkRipeStates(block))
		var quark = []
		if (ripe.length) quark.push('replant')
		if (probe.quarkClick(block)) quark.push('click')
		if (probe.quarkReplantBlock(block)) quark.push('replantBlock')
		if (tags.indexOf('quark:simple_harvest_blacklisted') >= 0) quark.push('blacklisted')
		base.quark = quark
		base.ripeStates = ripe
		base.parity = tags.indexOf('creativedelight:crops/farmland_crops') >= 0
		base.ownPick = tags.indexOf('creativedelight:crops/own_pick') >= 0
		base.fromBottom = tags.indexOf('creativedelight:crops/harvest_from_bottom') >= 0
		base.season = tags.filter(t => t.indexOf('creativecalendar:crop_seasons/') === 0)
			.map(t => t.substring('creativecalendar:crop_seasons/'.length))
		base.ownSoilCheck = toArr($Probe.ownSoilCheck(block))
		return base
	}

	var w = cropGrowthWalk()
	var rows = [], seen = [], replant = 0, click = 0
	w.entries.forEach(e => {
		var g = guardThrowable(() => {
			var r = e.row
			rows.push(describe(e.block, {
				id: r.id, mod: r.mod, source: 'walk', item: r.item, wthitNative: r.wthitNative,
				props: r.props, classes: r.classes,
			}))
			seen.push(r.id)
		})
		if (!g.ok) console.warn(`[dumpcropharvest] skipped ${e.row.id}: ${g.error}`)
	})
	toArr(probe.quarkOnly(seen.join('\n'))).forEach(id => {
		var g = guardThrowable(() => {
			var block = $Probe.block(id)
			var classes = []
			for (var c = block.getClass(); c && String(c.getName()) !== 'java.lang.Object' && classes.length < 14; c = c.getSuperclass())
				classes.push(String(c.getName()))
			rows.push(describe(block, {
				id: id, mod: id.substring(0, id.indexOf(':')), source: 'quark', item: null, wthitNative: null,
				props: [], classes: classes,
			}))
		})
		if (!g.ok) console.warn(`[dumpcropharvest] skipped Quark-only ${id}: ${g.error}`)
	})
	rows.sort((a, b) => a.id < b.id ? -1 : a.id > b.id ? 1 : 0)
	rows.forEach(r => {
		if (r.quark.indexOf('replant') >= 0) replant++
		if (r.quark.indexOf('click') >= 0) click++
	})

	var quarkOk = probe.quarkLoaded()
	writeExportJson('kubejs/exported/crops/crop_harvest_audit.json', {
		listed: rows.length, quark: quarkOk, quarkError: String(probe.quarkError()),
		replant: replant, click: click, blocks: rows,
	})
	console.log(`[dumpcropharvest] ${rows.length} crop blocks; Quark ${quarkOk ? 'read' : 'NOT read (' + probe.quarkError() + ')'}: ${replant} replant, ${click} click`)
	return { listed: rows.length, replant: replant, click: click, quark: quarkOk, quarkError: String(probe.quarkError()) }
}
