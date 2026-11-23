// Creative Common: pack names and Just Enough Effect Descriptions text for effects on the Raspberry Pie diet ladder
// (raspberry_pie.md 12.13, food-tuning #51, #56). Display names only: the effect and potion ids stay. Each key is added
// under the namespace of the jar that ships it, so the KubeJS pack (above every mod jar) wins in that namespace's lang
// stack whatever order the namespaces load in.

ClientEvents.lang('en_us', e => {
	var ef = global.effect
	if (!ef) { console.warn('[effect_names] global.effect missing (Common _lib/effect_dict.js)'); return }
	var ns = id => id.split(':')[0]
	var key = id => 'effect.' + id.replace(':', '.')

	// Astringent -> Steady (food-tuning #51, #56): SteadyRules and the Steady* mixins in Creative Common
	if (Platform.isLoaded('fruitsdelight')) {
		e.add(ns(ef.astringent), key(ef.astringent), 'Steady')
		e.add(ns(ef.astringent), key(ef.astringent) + '.description',
			'You do not slip: ice, soap blocks and slime feel like solid ground, and Slippery and Slipping cannot take hold ' +
			'(Steady clears them). Your step is light too: pressure plates and tripwires do not trigger and big dripleaf ' +
			'does not tip under you.')

		// Leaf Piercing particles (food-tuning #52, LeafPiercingTrail in Creative Common)
		e.add(ns(ef.leaf_piercing), key(ef.leaf_piercing) + '.description',
			'Arrows you shoot pass through leaves, with a puff of leaves where they enter and a trail of leaves behind them.')
	}

	// Poison Resistance -> Poison Immunity (food-tuning #56). Alex's Mobs names the effect with its own key and ships
	// the potion names in its lang file.
	if (Platform.isLoaded('alexsmobs')) {
		var am = ns(ef.poison_resistance)
		e.add(am, 'alexsmobs.potion.poison_resistance', 'Poison Immunity')
		;['', 'long_'].forEach(p => {
			e.add(am, 'item.minecraft.potion.effect.' + p + 'poison_resistance', 'Potion of Poison Immunity')
			e.add(am, 'item.minecraft.splash_potion.effect.' + p + 'poison_resistance', 'Splash Potion of Poison Immunity')
			e.add(am, 'item.minecraft.lingering_potion.effect.' + p + 'poison_resistance', 'Lingering Potion of Poison Immunity')
			e.add(am, 'item.minecraft.tipped_arrow.effect.' + p + 'poison_resistance', 'Arrow of Poison Immunity')
		})
	}

	// Fire Resistance -> Fire Immunity (food-tuning #56); vanilla's long potion shares the short one's name key
	var mc = ns(ef.fire_resistance)
	e.add(mc, key(ef.fire_resistance), 'Fire Immunity')
	e.add(mc, 'item.minecraft.potion.effect.fire_resistance', 'Potion of Fire Immunity')
	e.add(mc, 'item.minecraft.splash_potion.effect.fire_resistance', 'Splash Potion of Fire Immunity')
	e.add(mc, 'item.minecraft.lingering_potion.effect.fire_resistance', 'Lingering Potion of Fire Immunity')
	e.add(mc, 'item.minecraft.tipped_arrow.effect.fire_resistance', 'Arrow of Fire Immunity')
})
