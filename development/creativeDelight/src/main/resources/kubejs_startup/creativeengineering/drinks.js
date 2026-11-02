// Creative Delight startup script, loaded from the jar after the pack's startup_scripts, so the dicts exist here.
// Restart to apply.
//
// Drink rules (feature_writeup/creative_delight_drinks.md, TODO food/drinks #15): the pack's new teas and juices
// (ids reserved in section 6) and the fluids the rules brew on. Recipes: data/creativedelight/kubejs/creativeengineering/
// drinks.js; names and fluid names of renamed drinks: assets/creativedelight/kubejs/creativeengineering/names.js.
// Art: kubejs/assets/creativeengineering/textures/item/<id>.png, recoloured from Farmer's Respite's green tea (teas) and
// Farmer's Delight's melon juice (juices), both MIT (notice beside them).
//
// Values: the drink family's (Fruits Delight's teas and juices: 1 hunger, 0.2 modifier, always drinkable, 32 per
// stack, the glass bottle comes back). No effects yet: the effects of teas and juices wait for their audit
// (creative_delight_drinks.md 5).

const $DrinkUseAnim = Java.loadClass('net.minecraft.world.item.UseAnim')

StartupEvents.registry('item', e => {
	var i = global.item
	var drink = (id, name) => e.create(id).displayName(name).maxStackSize(32)
		.useAnimation($DrinkUseAnim.DRINK)
		.food(f => {
			f.hunger(1).saturation(0.2).alwaysEdible(true)
			f.eaten(ev => {
				var p = ev.player
				if (p && !p.isCreative()) p.give('minecraft:glass_bottle')
			})
		})
	;[
		[i.kiwi_tea, 'Kiwi Tea'], [i.turnip_tea, 'Turnip Tea'], [i.bayberry_tea, 'Bayberry Tea'],
		[i.hamimelon_tea, 'Cantaloupe Tea'], [i.carrot_tea, 'Carrot Tea'], [i.lemon_tea, 'Lemon Tea'],
		[i.melon_tea, 'Melon Tea'], [i.orange_tea, 'Orange Tea'], [i.pear_tea, 'Pear Tea'],
		[i.lychee_juice, 'Lychee Juice'], [i.hawberry_juice, 'Hawberry Juice'], [i.mango_juice, 'Mango Juice'],
		[i.mangosteen_juice, 'Mangosteen Juice'], [i.peach_juice, 'Peach Juice'],
	].forEach(d => drink(d[0], d[1]))
})

StartupEvents.registry('fluid', e => {
	var f = global.fluid
	var fluid = (id, name, color) => e.create(id).displayName(name).thinTexture(color).bucketColor(color).noBucket().noBlock()
	// New teas (section 2.1) and juices (section 1)
	fluid(f.kiwi_tea, 'Kiwi Tea', 0x8FAE3C)
	fluid(f.turnip_tea, 'Turnip Tea', 0xCBB8C9)
	fluid(f.bayberry_tea, 'Bayberry Tea', 0x8E2A3E)
	fluid(f.hamimelon_tea, 'Cantaloupe Tea', 0xE79E58)
	fluid(f.carrot_tea, 'Carrot Tea', 0xE0832E)
	fluid(f.lemon_tea, 'Lemon Tea', 0xE6D45A)
	fluid(f.melon_tea, 'Melon Tea', 0xD9584A)
	fluid(f.orange_tea, 'Orange Tea', 0xE8952C)
	fluid(f.pear_tea, 'Pear Tea', 0xCCC877)
	fluid(f.lychee_juice, 'Lychee Juice', 0xF1E3DC)
	fluid(f.hawberry_juice, 'Hawberry Juice', 0xC8323C)
	fluid(f.mango_juice, 'Mango Juice', 0xF7B731)
	fluid(f.mangosteen_juice, 'Mangosteen Juice', 0x7A2E5A)
	fluid(f.peach_juice, 'Peach Juice', 0xF6A98A)
	// Chorus Tea (was Ender Nectar, section 2.3) and the two sea juices the Sea Pickle Tea brews on in the Kettle
	fluid(f.chorus_tea, 'Chorus Tea', 0x9F7FB5)
	fluid(f.sea_grape_juice, 'Sea Grape Juice', 0x6FAF5A)
	fluid(f.pickerelweed_juice, 'Pickerelweed Juice', 0x6C7FD0)
})
