// requires: creativeengineering
// Creative Engineering.
ServerEvents.tags('item', e => {
console.log('wood tag started')

var i = global.item;
var t = global.tags

// The Vinery barrel tag to Creative Brewing (vinery/tags.js).

// ============================================================
// Treated wood filling: planks that are not smoked
// ============================================================
// the smoked planks have their own cheaper creosote recipe (wood/smoked_wood.js); expanded from #minecraft:planks
// as it stands when this runs
t.addMatching(e, 'creativeengineering:unsmoked_planks', i.planks, { except: i.smoked_planks })


console.log('wood tag finished')
})
