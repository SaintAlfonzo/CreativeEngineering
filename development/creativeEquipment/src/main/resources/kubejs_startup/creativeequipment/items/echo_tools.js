// Creative Equipment; moved from kubejs/startup_scripts/ on 2026-11-04 (creative_split.md 7.6, wave 7), rows unchanged.
// Echo-tier tools — pickaxe / axe / shovel / hoe at the same tier as Create: Deep
// Dark's echo sword + armour (that mod only ships a sword).
//
// Tier numbers lifted straight from create_deep_dark EchoSwordItem's anonymous Tier:
//   uses 2124 | speed 9.0 | attackDamageBonus 6.0 | enchantmentValue 16
// ...EXCEPT mining level: the echo sword's tier is level 1 (stone-tier), which is fine
// for a sword but would make an echo pickaxe unable to mine diamond/obsidian. Bumped
// to 4 (netherite-equivalent) so the digging tools actually work. Drop it back to 1
// in the `setLevel` call below if you want a literal match.
//
// Textures: kubejs/assets/creativeengineering/textures/item/echo_<tool>.png (16x192,
// 12-frame animated) + .png.mcmeta — see tools/echo_texture.py. Model is auto-generated
// (item/handheld) from the item id, no .texture() call needed.

ItemEvents.toolTierRegistry(event => {
	event.add('echo', tier => {
		tier.setUses(2124)
		tier.setSpeed(9.0)
		tier.setAttackDamageBonus(6.0)
		tier.setLevel(4)                                   // 1 = literal echo-sword match
		tier.setEnchantmentValue(18)
		tier.setRepairIngredient(Ingredient.of('create_deep_dark:echo_ingot'))
	})
})

StartupEvents.registry('item', event => {
	const ECHO_TOOLS = {
		echo_pickaxe: 'pickaxe',
		echo_axe:     'axe',
		echo_shovel:  'shovel',
		echo_hoe:     'hoe',
	}

	Object.keys(ECHO_TOOLS).forEach(name => {
		const pretty = 'Echo ' + name.split('_')[1].replace(/^./, c => c.toUpperCase())
		event.create('creativeengineering:' + name, ECHO_TOOLS[name])
			.tier('echo')
			.fireResistant()                              // echo gear survives fire (Item.Properties.fireResistant)
			.group('tools')
			.displayName(pretty)
			// On-hit Weakness II + Darkness II (tooltip + effect) registered via
			// global.toolEffects.onHit() in CB startup creativeengineering/tool_effects.js — matches
			// create_deep_dark:echo_sword's own tooltip/effect for consistency.
	})
})

// Amethyst Dust and Amethyst Lens (components): the pack's startup_scripts/items/components_amethyst.js.
