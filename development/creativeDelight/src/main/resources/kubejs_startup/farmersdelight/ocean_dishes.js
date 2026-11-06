// requires: farmersdelight
// Kelp Encrusted Cod (TODO dishes/ocean #7, ocean_delight.md 3.1): Ocean's Delight 1.0.4's dish (newer than the pack's
// Ocean's Delight), added in the pack namespace. Creative Delight startup script, loaded from the jar after the pack's
// startup_scripts, so global.item exists here. Restart to apply. Recipe: data/creativedelight/kubejs/farmersdelight/recipes.js.
// Art: kubejs/assets/creativeengineering/textures/item/kelp_encrusted_cod.png, Ocean's Delight 1.0.4's sprite (MIT,
// notice beside it). Values: 1.0.4's "kelp crusted cod" food (5 hunger, 0.6), not the stuffed cod values it registers
// the item with; food tuning sets the final values.
StartupEvents.registry('item', e => {
	e.create(global.item.kelp_encrusted_cod).displayName('Kelp Encrusted Cod').food(f => f.hunger(5).saturation(0.6))
})
