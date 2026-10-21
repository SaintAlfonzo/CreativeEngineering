// JEED effect providers: one jeed:effect_provider recipe per effect, listing every item that gives it, so each
// effect's JEI page shows where it comes from (feature_writeup/effects.md 3, TODO ui/effects #4).
//
// What JEED finds by itself (EffectInfo.buildStaticCache in jeed-1.19.2-2.1.4), so it isn't repeated here:
//   - every food's FoodProperties effects (incl. the pack's KubeJS food edits: alcohol, cans);
//   - suspicious stew per flower; the beacon for vanilla beacon effects;
//   - every potion, through jeed:potion_provider (bottle, splash, lingering, tipped arrow, Supplementaries spikes);
//   - its own 20 recipes (data/jeed/recipes): vanilla spawn eggs, conduit, turtle helmet, totem, wither rose...
// This file adds the same kinds of source for everything else:
//   - mobs that inflict an effect, as their spawn egg (JEED's husk -> Hunger);
//   - blocks and gear that give one (JEED's conduit, turtle helmet), incl. IE fluids and the chemthrower;
//   - food and drink that applies its effect in code (no FoodProperties entry: chocolate, teas, coins);
//   - pack gear, read from global.armorEffects / global.toolEffects, and the echo armor set (CDD mixin);
//   - Vinery wines (the port's [wine] effects config) and the Vinery standard banner;
//   - Botania brews (vial, flask, incense stick, blood pendant, by their brewKey NBT);
//   - Xaero's map switches: the command block, since only /effect gives them;
// plus the potion carriers JEED doesn't know (potion buckets, potion bundles, IE potion bullet, PneumaticCraft ammo).
// Sources were read from the decompiled jars (effects.md 2.1 Detailed column, 10-20 provider pass).
// Sunny is hidden from JEI (config/jeed-client.toml hidden_effects), so it gets none.
ServerEvents.recipes(e => {
	var i = global.item
	var R = global.NCH

	// effect id -> provider list; built up by add(), written once per effect at the end
	var providers = {}
	var add = (effect, items) => {
		var list = providers[effect] || (providers[effect] = [])
		var incoming = Array.isArray(items) ? items : [items]
		incoming.forEach(item => {
			var key = typeof item === 'string' ? item : JSON.stringify(item)
			if (!list.some(x => (typeof x === 'string' ? x : JSON.stringify(x)) === key)) list.push(item)
		})
	}

	//######################### Mobs (spawn eggs) #########################
	add('minecraft:slowness', [i.stray_spawn_egg, i.witch_spawn_egg, i.spawn_egg_mudskipper, i.wraith_spawn_egg])
	add('minecraft:instant_damage', i.witch_spawn_egg)
	add('minecraft:weakness', i.witch_spawn_egg)
	add('minecraft:poison', [i.bee_spawn_egg, i.pufferfish_spawn_egg, i.lionfish_spawn_egg, i.jellyfish_spawn_egg,
		i.spawn_egg_centipede, i.spawn_egg_komodo_dragon, i.spawn_egg_mimic_octopus, i.spawn_egg_rattlesnake,
		i.spawn_egg_platypus])
	add('minecraft:nausea', [i.pufferfish_spawn_egg, i.spawn_egg_skunk])
	add('minecraft:hunger', i.pufferfish_spawn_egg)
	add('minecraft:blindness', [i.spawn_egg_guster, i.squid_spawn_egg])     // Guster sand; UA: hurt squid ink
	add('minecraft:night_vision', i.glow_squid_spawn_egg)                    // UA: hurt glow squid ink
	add('minecraft:glowing', i.spawn_egg_blue_jay)
	add('minecraft:slow_falling', i.spawn_egg_sugar_glider)                  // riding on your shoulder
	add('alexsmobs:debilitating_sting', i.spawn_egg_tarantula_hawk)
	add('alexsmobs:earthquake', i.spawn_egg_rocky_roller)
	add('alexsmobs:ender_flu', [i.spawn_egg_endergrade, i.spawn_egg_enderiophage])
	add('alexsmobs:exsanguination', i.spawn_egg_frilled_shark)
	add('alexsmobs:fear', i.spawn_egg_tiger)
	add('alexsmobs:tigers_blessing', i.spawn_egg_tiger)
	add('alexsmobs:fleet_footed', i.spawn_egg_jerboa)
	add('alexsmobs:orcas_might', i.spawn_egg_orca)
	add('alexsmobs:power_down', i.spawn_egg_grizzly_bear)                  // April Fools only
	add('alexsmobs:sunbird_blessing', i.spawn_egg_sunbird)
	add('alexsmobs:sunbird_curse', i.spawn_egg_sunbird)
	add('neapolitan:slipping', i.plantain_spider_spawn_egg)

	//######################### Blocks, fluids and gear #########################
	add('minecraft:mining_fatigue', [i.guardian_spine, i.elder_guardian_spine])
	add('minecraft:slowness', i.concrete_bucket)
	add('minecraft:nausea', [i.ethanol_bucket, i.acetaldehyde_bucket])
	add('minecraft:fire_resistance', i.straddleboard)
	add('minecraft:poison', [i.thorn_chakram, i.spiky_bomb])
	add('minecraft:weakness', i.blue_bomb)
	add('minecraft:speed', i.oil_can)                                       // lubricated iron golems
	add('minecraft:strength', i.oil_can)
	add('minecraft:absorption', i.electrum_amulet)
	add('minecraft:luck', i.electrum_amulet)
	add('minecraft:bad_omen', i.ravager_horn)
	add('alexsmobs:exsanguination', i.shield_of_the_deep)
	add('better_beacons:long_reach', i.beacon)
	add('better_beacons:patrol_nullifier', i.beacon)
	add('better_beacons:phantom_bane', i.beacon)
	add('createaddition:shocking', i.createaddition_tesla_coil)
	add('fruitsdelight:alienating', i.durian_helmet)
	// Alienating gives mobs nearby Disgusted: the helmet and the foods with Alienating
	add('fruitsdelight:disgusted', [i.durian_helmet, i.baked_durian, i.durian_jello, i.durian_jelly, i.durian_pie])
	add('immersiveengineering:concrete_feet', [i.concrete_bucket, i.chemthrower])
	add('immersiveengineering:conductive', [i.redstone_acid_bucket, i.chemthrower])
	add('immersiveengineering:flammable', [i.creosote_bucket, i.biodiesel_bucket, i.chemthrower, i.crudeoil_bucket,
		i.gasoline_bucket, i.naphtha_bucket, i.benzene_bucket, i.napalm_bucket])
	add('immersiveengineering:flashed', [i.immersiveengineering_shield, i.toolupgrade_shield_flash])
	add('immersiveengineering:slippery', [i.lubricant_bucket, i.chemthrower])
	add('immersiveengineering:sticky', [i.phenolic_resin_bucket, i.chemthrower])
	add('immersiveengineering:stunned', [i.railgun, i.tesla_coil])            // endermen
	add('immersivepetroleum:anti_fire', [i.speedboat, i.upgrade_reinforced_hull])
	add('monobank:thief', i.monobank)
	add('neapolitan:slipping', i.bananarrow)
	add('via_romana:travellers_fatigue', i.any_sign)                         // fast travel between linked signs

	//######################### Food and drink with code-applied effects #########################
	add('minecraft:speed', [i.bar_of_caramel, i.black_chocolate_candy, i.ruby_chocolate_candy, i.pancake,
		i.coin_chocolate_gold])
	add('minecraft:haste', [i.builders_tea, i.candy_cane, i.coin_chocolate_iron])
	add('minecraft:regeneration', [i.hot_chocolate_bottle, i.soothing_hot_chocolate])
	add('minecraft:resistance', [i.gingerbread, i.coin_chocolate_diamond])
	add('minecraft:fire_resistance', i.coin_chocolate_netherite)
	add('minecraft:absorption', i.coin_chocolate_netherite)
	add('minecraft:luck', i.coin_chocolate_emerald)
	add('minecraft:saturation', [i.bar_of_ruby_chocolate, i.full_ruby_chocolate_bar, i.ruby_chocolate_candy,
		i.ruby_chocolate_candy_2, i.ruby_chocolate_candy_3])
	add('minecraft:water_breathing', i.pickerelweed_juice)
	add('minecraft:slow_falling', i.cherry_cream_soda)
	add('minecraft:nausea', i.candy)
	var gamblersTea = [i.gamblers_tea, i.long_gamblers_tea, i.strong_gamblers_tea]
	add('minecraft:glowing', gamblersTea)
	add('minecraft:night_vision', gamblersTea)
	add('minecraft:wither', gamblersTea)
	add('alexsmobs:oiled', i.fish_oil)
	add('create_confectionery:rest', [i.bar_of_white_chocolate, i.full_white_chocolate_bar, i.black_chocolate_candy_2,
		i.ruby_chocolate_candy_2])
	add('create_confectionery:stimulation', [i.bar_of_black_chocolate, i.full_black_chocolate_bar,
		i.black_chocolate_candy, i.black_chocolate_candy_2, i.ruby_chocolate_candy_3])

	//######################### Pack gear (armor and tool effect frameworks) #########################
	var addEffects = (effects, items) => (effects || []).forEach(spec => add(String(spec.effect), items))
	var A = global.armorEffects, T = global.toolEffects
	if (A) {
		Object.keys(A.PIECES).forEach(id => addEffects(A.PIECES[id], id))
		A.SETS.forEach(set => addEffects(set.effects, set.ids))
		Object.keys(A.MOUNTS).forEach(id => addEffects(A.MOUNTS[id], id))
	}
	if (T) {
		Object.keys(T.ON_HIT).forEach(id => addEffects(T.ON_HIT[id], id))
		Object.keys(T.HELD).forEach(id => addEffects(T.HELD[id], id))
	}
	// Echo armor: CDD's own set bonus, redirected by patcherMod's EchoArmorEffectMixin (memory echo-tier-gear)
	var echoArmor = [i.echo_helmet, i.echo_chestplate, i.echo_leggings, i.echo_boots]
	add('minecraft:night_vision', echoArmor)
	add('better_beacons:phantom_bane', echoArmor)

	//######################### Botania brews #########################
	// BotaniaBrews.registerBrews (Botania 440.2): brew key -> effects, and whether it infuses incense / the pendant
	var brews = [
		['speed', ['minecraft:speed'], true, true],
		['strength', ['minecraft:strength'], true, true],
		['haste', ['minecraft:haste'], true, true],
		['healing', ['minecraft:instant_health'], true, true],
		['jump_boost', ['minecraft:jump_boost'], true, true],
		['regen', ['minecraft:regeneration'], true, true],
		['regen_weak', ['minecraft:regeneration'], true, true],
		['resistance', ['minecraft:resistance'], true, true],
		['fire_resistance', ['minecraft:fire_resistance'], true, true],
		['water_breathing', ['minecraft:water_breathing'], true, true],
		['invisibility', ['minecraft:invisibility'], true, false],
		['night_vision', ['minecraft:night_vision'], true, true],
		['absorption', ['minecraft:absorption'], false, false],
		['overload', ['minecraft:strength', 'minecraft:speed', 'minecraft:weakness', 'minecraft:hunger'], true, true],
		['soul_cross', ['botania:soul_cross'], true, true],
		['feather_feet', ['botania:feather_feet'], true, true],
		['emptiness', ['botania:emptiness'], true, true],
		['bloodthirst', ['botania:bloodthirst'], true, true],
		['allure', ['botania:allure'], true, true],
		['clear', ['botania:clear'], true, true],
	]
	brews.forEach(b => {
		var nbt = '{brewKey:"botania:' + b[0] + '"}'
		var items = [{ item: i.brew_vial, nbt: nbt }, { item: i.brew_flask, nbt: nbt }]
		if (b[2]) items.push({ item: i.incense_stick, nbt: nbt })
		if (b[3]) items.push({ item: i.blood_pendant, nbt: nbt })
		b[1].forEach(effect => add(effect, items))
	})

	//######################### Vinery: wines and the standard banner #########################
	// The wine -> effect map is the port's [wine] effects config, so editing it and running /reload moves the
	// providers (alcohol.md B7.5).
	if (Platform.isLoaded('vinery')) {
		try {
			var winesByEffect = Java.loadClass('net.satisfy.vinery.core.wine.WineEffects').winesByEffect()
			var effectIds = winesByEffect.keySet().iterator()
			while (effectIds.hasNext()) {
				var effectId = String(effectIds.next())
				var wineList = winesByEffect.get(effectId)
				for (var n = 0; n < wineList.size(); n++) add(effectId, String(wineList.get(n)))
			}
		} catch (err) {
			console.warn('jeed_providers.js: no Vinery wine providers (' + err + ')')
		}
		try {
			var $VineryPlatform = Java.loadClass('net.satisfy.vinery.platform.PlatformHelper')
			var bannerEffect = $VineryPlatform.getBannerEffect()
			if ($VineryPlatform.shouldGiveEffect() && bannerEffect) {
				add(String(Java.loadClass('net.minecraftforge.registries.ForgeRegistries').MOB_EFFECTS.getKey(bannerEffect)),
					i.vinery_standard)
			}
		} catch (err) {
			console.warn('jeed_providers.js: no Vinery banner provider (' + err + ')')
		}
	}

	//######################### Xaero's map switches #########################
	// Server-side feature switches with no item or mob source: only /effect gives them.
	var xaeroSwitches = ['xaerominimap:no_minimap', 'xaerominimap:no_minimap_harmful', 'xaerominimap:no_entity_radar',
		'xaerominimap:no_entity_radar_harmful', 'xaerominimap:no_waypoints', 'xaerominimap:no_waypoints_harmful',
		'xaerominimap:no_cave_maps', 'xaerominimap:no_cave_maps_harmful', 'xaeroworldmap:no_world_map',
		'xaeroworldmap:no_world_map_harmful', 'xaeroworldmap:no_cave_maps', 'xaeroworldmap:no_cave_maps_harmful']
	xaeroSwitches.forEach(id => add(id, i.command_block))

	//######################### Write #########################
	var written = 0
	Object.keys(providers).sort().forEach(effect => {
		R.jeedProvider(effect, providers[effect], e)
		written++
	})

	// Items holding a `Potion` NBT tag like minecraft:potion; JEED fills each with every potion of the effect
	R.jeedPotionProvider([i.ce_potion_bucket, i.ce_splash_potion_bucket, i.ce_lingering_potion_bucket,
		i.potion_bundle, i.splash_potion_bundle, i.lingering_potion_bundle,
		i.immersiveengineering_potion, i.gun_ammo], e, 'potion_carriers')

	console.log('jeed_providers.js: ' + written + ' effect provider recipes')
})
