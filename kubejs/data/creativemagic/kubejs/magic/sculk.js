// requires: creativemagic
// Creative Magic; moved from kubejs/server_scripts/magic/sculk.js on 2026-11-04 (creative_split.md 7.6, wave 6), rows unchanged.
// Recipe ids are creativemagic:<type>/<suffix> (global.CMG.recipes).
ServerEvents.recipes(e => {
global.CMG.recipes(() => {
console.log('Sculk starting')

const R = global.NCH
var f = global.fluid

var i = global.item;

// ============================================================
// XP Mixing
// ============================================================

R.createMixing(Item.of(i.sculk, 4), [Fluid.of(f.xp, 50), Item.of(i.sculk, 2)], e, undefined, 'n/a')
R.createMixing(Item.of(i.sculk, 8), [Fluid.of(f.hyper_xp, 5), Item.of(i.sculk, 4)], e, undefined, 'n/a')

console.log('Sculk done')
})
})
