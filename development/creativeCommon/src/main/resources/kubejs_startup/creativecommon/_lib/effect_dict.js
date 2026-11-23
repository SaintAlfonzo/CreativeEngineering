// priority: 1000
// Mob effect ids (global.effect), same role as item_dict.js for items: scripts reference
// global.effect.<key> instead of repeating 'namespace:id' strings.
global.effect = {};

// ############################## Assignments ##############################
var ef = global.effect;

// ############################## Vanilla ##############################

// Beneficial
ef.speed = 'minecraft:speed';
ef.haste = 'minecraft:haste';
ef.strength = 'minecraft:strength';
ef.instant_health = 'minecraft:instant_health';
ef.jump_boost = 'minecraft:jump_boost';
ef.regeneration = 'minecraft:regeneration';
ef.resistance = 'minecraft:resistance';
ef.fire_resistance = 'minecraft:fire_resistance';
ef.water_breathing = 'minecraft:water_breathing';
ef.invisibility = 'minecraft:invisibility';
ef.night_vision = 'minecraft:night_vision';
ef.health_boost = 'minecraft:health_boost';
ef.absorption = 'minecraft:absorption';
ef.saturation = 'minecraft:saturation';
ef.luck = 'minecraft:luck';
ef.slow_falling = 'minecraft:slow_falling';
ef.conduit_power = 'minecraft:conduit_power';
ef.dolphins_grace = 'minecraft:dolphins_grace';
ef.hero_of_the_village = 'minecraft:hero_of_the_village';

// Harmful
ef.slowness = 'minecraft:slowness';
ef.mining_fatigue = 'minecraft:mining_fatigue';
ef.instant_damage = 'minecraft:instant_damage';
ef.nausea = 'minecraft:nausea';
ef.blindness = 'minecraft:blindness';
ef.hunger = 'minecraft:hunger';
ef.weakness = 'minecraft:weakness';
ef.poison = 'minecraft:poison';
ef.wither = 'minecraft:wither';
ef.levitation = 'minecraft:levitation';
ef.unluck = 'minecraft:unluck';
ef.bad_omen = 'minecraft:bad_omen';
ef.darkness = 'minecraft:darkness';

// Neutral
ef.glowing = 'minecraft:glowing';

// ############################## Modded ##############################
ef.caffeinated = 'farmersrespite:caffeinated';
ef.agility = 'neapolitan:agility';
ef.climbing = 'vinery:climbing_effect';
ef.knockback_resistance = 'alexsmobs:knockback_resistance';
ef.phantom_bane = 'better_beacons:phantom_bane';

// ############################## Keys added by tools/literal_id_swap.py ##############################
// Ids used two or more times as literals in scripts (plan_rules_fixes.md section 7).
ef.nourishment = 'farmersdelight:nourishment';
ef.comfort = 'farmersdelight:comfort';
ef.magic_resistance = 'creativecommon:magic_resistance';
ef.wither_immunity = 'creativecommon:wither_immunity';
ef.exsanguination = 'alexsmobs:exsanguination';
ef.conductive = 'immersiveengineering:conductive';
ef.pungent = 'hearthandharvest:pungent';
ef.lava_vision = 'alexsmobs:lava_vision';
ef.long_reach = 'better_beacons:long_reach';
ef.alienating = 'fruitsdelight:alienating';
ef.poison_resistance = 'alexsmobs:poison_resistance';
ef.tempting = 'hearthandharvest:tempting';
ef.harmony = 'neapolitan:harmony';
ef.earthquake = 'alexsmobs:earthquake';
ef.fear = 'alexsmobs:fear';
ef.slipping = 'neapolitan:slipping';
ef.flammable = 'immersiveengineering:flammable';
ef.soul_cross = 'botania:soul_cross';
ef.feather_feet = 'botania:feather_feet';
ef.soulsteal = 'alexsmobs:soulsteal';
ef.leaf_piercing = 'fruitsdelight:leaf_piercing';
ef.refreshing = 'fruitsdelight:refreshing';
ef.shrinking = 'fruitsdelight:shrinking';
ef.astringent = 'fruitsdelight:astringent';
ef.suspicious_smell = 'fruitsdelight:suspicious_smell';
ef.lozenge = 'fruitsdelight:lozenge';
ef.sweetening = 'fruitsdelight:sweetening';
ef.digesting = 'fruitsdelight:digesting';

// ############################## Resolver ##############################
// global.effect.resolve(idOrKey): the registered MobEffect for an effect id ('minecraft:speed') or a key of this dict
// ('speed'), or null (warned once per id) when it is not registered. The one effect lookup for every script
// (plan_rules_fixes.md section 12, V-35); found effects are cached, misses are looked up again on the next call, since
// a startup script may ask before a mod's effects are registered. Not enumerable, so it never reads as a dict key.
Object.defineProperty(global.effect, 'resolve', {
	enumerable: false,
	value: (function () {
		var $EffRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
		var $EffRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
		var found = {}
		var warned = {}
		return function (idOrKey) {
			if (idOrKey == null) return null
			var id = String(idOrKey)
			if (id.indexOf(':') < 0) {
				var keyed = global.effect[id]
				if (typeof keyed !== 'string') {
					if (!warned[id]) console.warn('[effect.resolve] "' + id + '" is neither an effect id nor a global.effect key')
					warned[id] = true
					return null
				}
				id = keyed
			}
			if (found[id]) return found[id]
			var me = null
			try { me = $EffRegistries.MOB_EFFECTS.getValue(new $EffRL(id)) } catch (err) {}
			if (me) {
				found[id] = me
			} else if (!warned[id]) {
				console.warn('[effect.resolve] effect "' + id + '" is not registered (bad id, or that mod is not installed)')
				warned[id] = true
			}
			return me
		}
	})(),
})
