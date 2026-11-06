// Creative Delight scratch test suite, startup half (tools/creative_delight_test/README.md; SQ-CDT). Never in the
// pack or a release jar. In the CD jar (built with -Pcd_test_suite) it runs from patcherMod's startup loader, in
// the creativedelight pack: after patcherMod's helper pack and the pack folder, after cd_core.js (priority 1000).
// It records what existed when it ran; `/cdtest startup` checks the record.
global.cdtestStartup = {
	nch: typeof global.NCH,
	nsAtLoad: global.NCH && global.NCH.ns ? global.NCH.ns() : null,
	tags: typeof global.tags,
	compost: typeof global.compost,
	itemDict: global.item ? String(global.item.wheat) : null,
	cd: typeof global.CD,
	absentRan: false,
}
// A helper call at startup top level: the compost helper exists before this pack (it is patcherMod's).
// tiers(map) is a setter: call it empty, then count what the pack folder's compost_values.js registered.
if (global.compost && global.compost.tiers) global.compost.tiers({})
global.cdtestStartup.compostTiers = global.compost && global.compost.TIERS ? Object.keys(global.compost.TIERS).length : -1
console.info('[cdtest] startup suite ran: ' + JSON.stringify(global.cdtestStartup))
