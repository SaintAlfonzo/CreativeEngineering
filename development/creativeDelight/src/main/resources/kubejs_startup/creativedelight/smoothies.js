// Creative Delight startup script, loaded from the jar after the pack's startup_scripts, so the dicts exist here.
// Restart to apply.
//
// Smoothies (TODO sweets #8; feature_writeup/smoothies.md sections 1 and 2, rulings plan_smoothies.md section 1): 16
// fruit smoothies, 2 x the first fruit + 1 x the second (or 3 x a one-fruit smoothie) + a glass bottle, shapeless.
// Effects: the first fruit at its Fruits Delight base length (jam, jello), the second at half, amplifiers as Fruits
// Delight has them. Every smoothie is always edible, drinks with the honey bottle sound (#creativedelight:honey_drink_sound,
// mixin.minecraft.ItemStackHoneyDrinkMixin), hands the bottle back, stacks to 32 and counts as a cold food for brain freeze
// (#creativecommon:brain_freeze, Creative Common effect/BrainFreeze, sweets #9). Hunger and saturation: the drink values
// (1, 0.2) until the food tuning pass sets the drink/juice family rule.
// Mixed Berry: 2 red berries + 1 blue or purple berry; the craft stores the berries' effects on the item (NBT
// SmoothieEffects, written by the recipe in data creativedelight/kubejs/creativedelight/smoothies.js), the tooltip
// (assets creativedelight/kubejs/creativedelight/smoothie_tooltip.js) and the drink read them.
// Sprites: every smoothie is a copy of Neapolitan's strawberry_banana_smoothie.png under its own id (placeholder, owner
// 2026-11-18; feature_writeup/texture_sources/sources.csv), so the per-flavor swap (sweets #10) replaces files only.
// Recipes, fluids' machine routes: data creativedelight/kubejs/creativedelight/smoothies.js; tags: smoothie_tags.js.

global.smoothies = (function () {
	var i = global.item
	var ef = global.effect
	// [effect key, seconds, amplifier]
	var e = (key, s, amp) => [key, s, amp || 0]
	var STR3 = 2 // Strength III (Fruits Delight's mango and lychee)
	return {
		// key: global.item key (also the fluid key); a, b: recipe fruits (a x2, b x1; b null = a x3); mods: what the
		// recipe needs; effects: first fruit, then second; color: the fluid tint; special: 'teleport', 'berries'
		// G5 begin: the recipe script skips a row unless every mod in its mods list is loaded (Platform.isLoaded), and effects pass effectOn
		list: [
			{ key: 'mixed_berry_smoothie', name: 'Mixed Berry Smoothie', a: i.t_berries_red, b: i.t_berries_blue_or_purple,
				mods: [], effects: [], special: 'berries' },
			{ key: 'cran_apple_smoothie', name: 'Cran Apple Smoothie', a: i.t_fruits_cranberry, b: i.apple, mods: ['fruitsdelight'],
				effects: [e(ef.shrinking, 80), e(ef.absorption, 10)], color: 0xC8434A },
			{ key: 'orange_mango_smoothie', name: 'Orange Mango Smoothie', a: i.orange_slice, b: i.t_fruits_mango, mods: ['fruitsdelight'],
				effects: [e(ef.regeneration, 20), e(ef.recovering, 20), e(ef.strength, 10, STR3), e(ef.rage_aura, 10)], color: 0xF5A53A },
			{ key: 'kiwi_strawberry_smoothie', name: 'Kiwi Strawberry Smoothie', a: i.t_fruits_kiwi, b: i.t_fruits_strawberry,
				mods: ['fruitsdelight', 'neapolitan'], effects: [e(ef.cycling, 4)], color: 0xD9706A },
			{ key: 'glowberry_smoothie', name: 'Glowberry Smoothie', a: i.glow_berries, b: null, mods: [],
				effects: [e(ef.glowing, 80)], color: 0xF2A93B },
			{ key: 'chorus_smoothie', name: 'Chorus Smoothie', a: i.chorus_fruit, b: null, mods: [],
				effects: [], special: 'teleport', color: 0x9F7FB5 },
			{ key: 'peach_mango_smoothie', name: 'Peach Mango Smoothie', a: i.t_fruits_peach, b: i.t_fruits_mango, mods: ['fruitsdelight'],
				effects: [e(ef.heal_aura, 10), e(ef.strength, 10, STR3), e(ef.rage_aura, 10)], color: 0xF7B060 },
			{ key: 'mango_pineapple_smoothie', name: 'Mango Pineapple Smoothie', a: i.t_fruits_mango, b: i.pineapple_slice,
				mods: ['fruitsdelight'], effects: [e(ef.strength, 20, STR3), e(ef.rage_aura, 20), e(ef.speed, 10), e(ef.sweetening, 10)],
				color: 0xF8C640 },
			{ key: 'melon_smoothie', name: 'Melon Smoothie', a: i.melon, b: i.cantaloupe_slice, mods: ['delightful'],
				effects: [e(ef.regeneration, 40), e(ef.health_boost, 10)], color: 0xF08A6A },
			{ key: 'cherry_lemon_smoothie', name: 'Cherry Lemon Smoothie', a: i.t_fruits_cherry, b: i.lemon_slice,
				mods: ['vinery', 'fruitsdelight'], effects: [e(ef.haste, 10), e(ef.refreshing, 10)], color: 0xE06070 },
			{ key: 'blueberry_lemon_smoothie', name: 'Blueberry Lemon Smoothie', a: i.t_fruits_blueberry, b: i.lemon_slice,
				mods: ['fruitsdelight'], effects: [e(ef.night_vision, 20), e(ef.brightening, 20), e(ef.haste, 10), e(ef.refreshing, 10)],
				color: 0x7A6FC0 },
			// Pear (owner, 2026-11-17): the "eat at any hunger" drink
			{ key: 'pear_smoothie', name: 'Pear Smoothie', a: i.t_fruits_pear, b: i.t_fruits_hawberry, mods: ['fruitsdelight'],
				effects: [e(ef.appetizing, 300), e(ef.lozenge, 30)], color: 0xD6CF8A },
			{ key: 'cherry_smoothie', name: 'Cherry Smoothie', a: i.t_fruits_cherry, b: i.t_fruits_lychee, mods: ['vinery', 'fruitsdelight'],
				effects: [e(ef.strength, 5, STR3)], color: 0xB8283C },
			{ key: 'raspberry_peach_smoothie', name: 'Raspberry Peach Smoothie', a: i.raspberry, b: i.t_fruits_peach,
				mods: ['hearthandharvest', 'fruitsdelight'], effects: [e(ef.heal_aura, 5)], color: 0xE86F7A },
			{ key: 'orange_peach_smoothie', name: 'Orange Peach Smoothie', a: i.orange_slice, b: i.t_fruits_peach, mods: ['fruitsdelight'],
				effects: [e(ef.regeneration, 20), e(ef.recovering, 20), e(ef.heal_aura, 5)], color: 0xF39A5A },
			{ key: 'hawberry_smoothie', name: 'Hawberry Smoothie', a: i.t_fruits_hawberry, b: i.white_strawberries,
				mods: ['fruitsdelight', 'neapolitan'], effects: [e(ef.appetizing, 20)], color: 0xD04A55 },
		],
		// G5 end
		// Mixed Berry (smoothies.md 2.1): what each berry adds, at half its base length; the same berry twice adds up to
		// the full base. Berries not listed (raspberry, strawberries, sweet berries, cherry, mulberry) add nothing.
		berryEffects: (function () {
			var m = {}
			// G5 begin: a lookup by the crafted berry (an absent mod's berry is never in a recipe); effects pass effectOn
			m[i.cranberry] = [e(ef.shrinking, 40)]
			m[i.hawberry] = [e(ef.appetizing, 10)]
			m[i.blueberry] = [e(ef.night_vision, 10), e(ef.brightening, 10)]
			m[i.bayberry] = [e(ef.leaf_piercing, 10)]
			// G5 end
			return m
		})(),
		// the NBT key on Mixed Berry: "effect id|ticks|amplifier" entries joined with ';' ('' = no effect)
		nbtKey: 'SmoothieEffects',
		// an effect's mod is loaded (Fruits Delight's effects are left out without it)
		effectOn: id => Platform.isLoaded(String(id).split(':')[0]),
	}
})()

;(function () {
	var reg = global.reg.forPack('creativedelight')
	var $SmUseAnim = Java.loadClass('net.minecraft.world.item.UseAnim')
	var $SmEffectInstance = Java.loadClass('net.minecraft.world.effect.MobEffectInstance')
	var $SmSounds = Java.loadClass('net.minecraft.sounds.SoundEvents')
	var $SmSource = Java.loadClass('net.minecraft.sounds.SoundSource')
	var S = global.smoothies

	// Mixed Berry: the effects the craft stored on the drunk stack
	var storedEffects = (p, stack) => {
		var tag = stack.getTag()
		if (!tag || !tag.contains(S.nbtKey)) return
		String(tag.getString(S.nbtKey)).split(';').forEach(part => {
			var v = part.split('|')
			if (v.length !== 3 || !S.effectOn(v[0])) return
			var effect = global.effect.resolve(v[0])
			if (effect) p.addEffect(new $SmEffectInstance(effect, parseInt(v[1]), parseInt(v[2])))
		})
	}

	// Chorus: a random teleport as eating chorus fruit does (16 tries within 8 blocks, the cooldown on the item)
	var teleport = (p, stack) => {
		var lvl = p.level
		var x0 = p.getX(), y0 = p.getY(), z0 = p.getZ()
		var minY = lvl.getMinBuildHeight(), maxY = lvl.getMaxBuildHeight() - 1
		for (var n = 0; n < 16; n++) {
			var x = x0 + (Math.random() - 0.5) * 16
			var y = Math.max(minY, Math.min(maxY, y0 + Math.floor(Math.random() * 16) - 8))
			var z = z0 + (Math.random() - 0.5) * 16
			if (p.isPassenger()) p.stopRiding()
			if (p.randomTeleport(x, y, z, true)) {
				lvl.playSound(null, x0, y0, z0, $SmSounds.CHORUS_FRUIT_TELEPORT, $SmSource.PLAYERS, 1.0, 1.0)
				lvl.playSound(null, p.getX(), p.getY(), p.getZ(), $SmSounds.CHORUS_FRUIT_TELEPORT, $SmSource.PLAYERS, 1.0, 1.0)
				break
			}
		}
		p.getCooldowns().addCooldown(stack.getItem(), 20)
	}

	StartupEvents.registry('item', e => {
		var i = global.item
		S.list.forEach(s => {
			var id = i[s.key]
			reg.item(e, id).displayName(s.name).maxStackSize(32)
				.useAnimation($SmUseAnim.DRINK)
				.texture('creativedelight:item/' + id.split(':')[1])
				.food(f => {
					f.hunger(1).saturation(0.2).alwaysEdible(true)
					s.effects.forEach(x => {
						if (S.effectOn(x[0])) f.effect(x[0], x[1] * 20, x[2], 1.0)
					})
					f.eaten(ev => {
						var p = ev.player
						if (!p || p.level.isClientSide()) return
						if (s.special === 'berries') storedEffects(p, ev.item)
						if (s.special === 'teleport') teleport(p, ev.item)
						if (!p.isCreative()) p.give(i.glass_bottle)
					})
				})
		})
	})

	StartupEvents.registry('fluid', e => {
		var f = global.fluid
		S.list.forEach(s => {
			if (!s.color) return // Mixed Berry: no fluid (its effects are on the item)
			reg.fluid(e, f[s.key]).displayName(s.name).thinTexture(s.color).bucketColor(s.color).noBucket().noBlock()
		})
	})
})()
