// requires: creativeengineering
// Creative Engineering; moved from kubejs/server_scripts/tags/wood_tags.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged.
ServerEvents.tags('item', e => {
console.log('wood tag started')

var i = global.item;
var t = global.tags

// Mulch and millable-seed tags moved to Creative Delight 2026-11-06 (creativeengineering/compost_tags.js there);
// the Vinery barrel tag to Creative Brewing (vinery/tags.js).

// ============================================================
// Treated wood filling: planks that are not smoked
// ============================================================
// the smoked planks have their own cheaper creosote recipe (wood/smoked_wood.js); expanded from #minecraft:planks
// as it stands when this runs
t.addMatching(e, 'creativeengineering:unsmoked_planks', '#minecraft:planks', { except: i.smoked_planks })

// Smokable wood tags moved to Creative Common 2026-11-10 (data/creativecommon/kubejs/tags/smokable_tags.js).

console.log('wood tag finished')
})
