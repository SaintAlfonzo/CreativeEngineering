// requires: moredelight
// More Delight toasts (feature_writeup/more_delight.md 2.3, 2.5). Creative Delight startup script, loaded from the jar
// after the pack's startup_scripts, so global.item exists here. Restart to apply.
// Art: kubejs/assets/creativeengineering/textures/item/cheese_toast.png (MIT, More Delight; notice beside it).

// Fast eating (16 ticks, like dried kelp) for every toast, plain and topped. Family rule: feature_writeup/food_families.md
// (bread/slice and sandwich/toast, values). A new toast item has to be added to this list.
global.fastToasts = ['toast', 'toast_with_chocolate', 'toast_with_egg', 'toast_with_glow_berries', 'toast_with_honey',
	'toast_with_sweet_berries', 'caviar_toast', 'cheese_toast']

// Cheese Toast: toast + Flaxen Cheese Wedge (recipe in data/creativedelight/kubejs/moredelight/recipes.js). Hunger 5
// like Toast with Egg, modifier 0.7 (toast 0.4, wedge 1.0).
StartupEvents.registry('item', e => {
	var i = global.item
	e.create(i.cheese_toast).displayName('Cheese Toast').food(f => {
		f.hunger(5).saturation(0.7).fastToEat(true)
	})
})

ItemEvents.modification(e => {
	var i = global.item
	var done = 0
	global.fastToasts.forEach(key => {
		var id = i[key]
		if (typeof id !== 'string') { console.warn('[toast] fast toast: no item for ' + key); return }
		if (key === 'cheese_toast') return // registered fast above
		e.modify(id, it => it.setFoodProperties(fb => fb.fastToEat()))
		done++
	})
	console.log('[toast] ' + done + ' toasts set to fast eating')
})
