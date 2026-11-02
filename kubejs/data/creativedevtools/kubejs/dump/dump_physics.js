// requires: creativedevtools
// /dumpphysics  ->  writes kubejs/exported/physics/item_physics.json
//
//   Every item with the facts the item-physics family classifier needs, and what a dropped stack of it
//   does today: water (floats / sinks), fire and lava (survives / destroyed), and why.
//   Per item: mod, block item, the block's Material (name, flammable, solid) and SoundType name, the item's
//   own fire resistance, fuel burn time, edible, gear kind (tool tier / armor material), and the live
//   ItemPhysic + patcherMod answers (ItemPhysicSplit tags and rules, ItemPhysic's three config lists,
//   Item#canBeHurtBy for fire and lava).
//
// tools/item_physics_audit.py reads it (with the tag dump) and writes feature_writeup/item_physics_audit.md.

ServerEvents.commandRegistry(event => {
	var { commands: Commands } = event
	event.register(
		Commands.literal('dumpphysics')
			.requires(src => src.hasPermission(2))
			.executes(ctx => {
				var g = guardThrowable(() => dumpPhysics())
				if (g.ok) {
					var r = g.value
					ctx.source.sendSuccess(Text.green(`Dumped ${r.total} items (${r.lavaSurvivors} survive lava) to kubejs/exported/physics/`), false)
				} else {
					console.error('[dumpphysics] failed: ' + g.error)
					ctx.source.sendFailure(Text.red('dumpphysics failed - see log: ' + g.error))
				}
				return 1
			})
	)
})

function dumpPhysics() {
	var $Reg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
	var $BlockItem = Java.loadClass('net.minecraft.world.item.BlockItem')
	var $Material = Java.loadClass('net.minecraft.world.level.material.Material')
	var $SoundType = Java.loadClass('net.minecraft.world.level.block.SoundType')
	var $DamageSource = Java.loadClass('net.minecraft.world.damagesource.DamageSource')
	var $ForgeHooks = Java.loadClass('net.minecraftforge.common.ForgeHooks')
	var $TieredItem = Java.loadClass('net.minecraft.world.item.TieredItem')
	var $ArmorItem = Java.loadClass('net.minecraft.world.item.ArmorItem')
	var SPLIT = null, IP = null
	try { SPLIT = Java.loadClass('com.SaintAlfonzo.creativecompat.itemphysic.ItemPhysicSplit') } catch (e) { console.warn('[dumpphysics] no ItemPhysicSplit: ' + e) }
	try { IP = Java.loadClass('team.creative.itemphysic.ItemPhysic') } catch (e) { console.warn('[dumpphysics] no ItemPhysic: ' + e) }

	// Material and SoundType have no names of their own: match by identity against the vanilla constants.
	var MATERIALS = ['AIR', 'STRUCTURAL_AIR', 'PORTAL', 'CLOTH_DECORATION', 'PLANT', 'WATER_PLANT', 'REPLACEABLE_PLANT',
		'REPLACEABLE_FIREPROOF_PLANT', 'REPLACEABLE_WATER_PLANT', 'WATER', 'BUBBLE_COLUMN', 'LAVA', 'TOP_SNOW', 'FIRE',
		'DECORATION', 'WEB', 'SCULK', 'BUILDABLE_GLASS', 'CLAY', 'DIRT', 'GRASS', 'ICE_SOLID', 'SAND', 'SPONGE',
		'SHULKER_SHELL', 'WOOD', 'NETHER_WOOD', 'BAMBOO_SAPLING', 'BAMBOO', 'WOOL', 'EXPLOSIVE', 'LEAVES', 'GLASS', 'ICE',
		'CACTUS', 'STONE', 'METAL', 'SNOW', 'HEAVY_METAL', 'BARRIER', 'PISTON', 'MOSS', 'VEGETABLE', 'EGG', 'CAKE',
		'AMETHYST', 'POWDER_SNOW', 'FROGSPAWN', 'FROGLIGHT']
	var SOUNDS = ['WOOD', 'GRAVEL', 'GRASS', 'LILY_PAD', 'STONE', 'METAL', 'GLASS', 'WOOL', 'SAND', 'SNOW', 'POWDER_SNOW',
		'LADDER', 'ANVIL', 'SLIME_BLOCK', 'HONEY_BLOCK', 'WET_GRASS', 'CORAL_BLOCK', 'BAMBOO', 'BAMBOO_SAPLING',
		'SCAFFOLDING', 'SWEET_BERRY_BUSH', 'CROP', 'HARD_CROP', 'VINE', 'NETHER_WART', 'LANTERN', 'STEM', 'NYLIUM',
		'FUNGUS', 'ROOTS', 'SHROOMLIGHT', 'WEEPING_VINES', 'TWISTING_VINES', 'SOUL_SAND', 'SOUL_SOIL', 'BASALT',
		'WART_BLOCK', 'NETHERRACK', 'NETHER_BRICKS', 'NETHER_SPROUTS', 'NETHER_ORE', 'BONE_BLOCK', 'NETHERITE_BLOCK',
		'ANCIENT_DEBRIS', 'LODESTONE', 'CHAIN', 'NETHER_GOLD_ORE', 'GILDED_BLACKSTONE', 'CANDLE', 'AMETHYST',
		'AMETHYST_CLUSTER', 'SMALL_AMETHYST_BUD', 'MEDIUM_AMETHYST_BUD', 'LARGE_AMETHYST_BUD', 'TUFF', 'CALCITE',
		'DRIPSTONE_BLOCK', 'POINTED_DRIPSTONE', 'COPPER', 'CAVE_VINES', 'SPORE_BLOSSOM', 'AZALEA', 'FLOWERING_AZALEA',
		'MOSS_CARPET', 'MOSS', 'BIG_DRIPLEAF', 'SMALL_DRIPLEAF', 'ROOTED_DIRT', 'HANGING_ROOTS', 'AZALEA_LEAVES',
		'SCULK_SENSOR', 'SCULK_CATALYST', 'SCULK', 'SCULK_VEIN', 'SCULK_SHRIEKER', 'GLOW_LICHEN', 'DEEPSLATE',
		'DEEPSLATE_BRICKS', 'DEEPSLATE_TILES', 'POLISHED_DEEPSLATE', 'FROGLIGHT', 'FROGSPAWN', 'MANGROVE_ROOTS',
		'MUDDY_MANGROVE_ROOTS', 'MUD', 'MUD_BRICKS', 'PACKED_MUD']
	var constants = (cls, names) => {
		var out = []
		names.forEach(n => { try { var v = cls[n]; if (v) out.push([n, v]) } catch (e) {} })
		return out
	}
	var materials = constants($Material, MATERIALS)
	var sounds = constants($SoundType, SOUNDS)
	var nameOf = (pairs, obj) => { for (var k = 0; k < pairs.length; k++) if (pairs[k][1] === obj) return pairs[k][0]; return obj ? 'modded' : null }

	var keyOf = (reg, obj) => { try { var k = reg.getKey(obj); return k ? String(k) : null } catch (e) { return null } }
	var cfg = IP ? IP.CONFIG.general : null
	var canPass = (list, stack) => { try { return !!list.canPass(stack) } catch (e) { return null } }
	var bool = v => (v == null) ? null : !!v

	var keys = []
	$Reg.ITEMS.getKeys().forEach(rl => keys.push(rl))
	keys.sort((a, b) => String(a) < String(b) ? -1 : String(a) > String(b) ? 1 : 0)

	var rows = [], total = 0, skipped = 0, lavaSurvivors = 0
	keys.forEach(rl => {
		var id = String(rl)
		if (id === 'minecraft:air') return
		total++
		var g = guardThrowable(() => {
			var item = $Reg.ITEMS.getValue(rl)
			var stack = Item.of(id)
			var row = { id: id, mod: id.substring(0, id.indexOf(':')) }
			row.blockItem = item instanceof $BlockItem
			if (row.blockItem) {
				var gb = guardThrowable(() => {
					var state = item.getBlock().defaultBlockState()
					var m = state.getMaterial()
					row.material = nameOf(materials, m)
					row.materialFlammable = !!m.isFlammable()
					row.materialSolid = !!m.isSolid()
					row.sound = nameOf(sounds, state.getSoundType())
				})
				if (!gb.ok) row.blockError = String(gb.error)
			}
			try { row.fireResistant = !!item.isFireResistant() } catch (e) { row.fireResistant = null }
			try { row.burnTime = $ForgeHooks.getBurnTime(stack, null) } catch (e) { row.burnTime = null }
			try { row.edible = !!item.isEdible() } catch (e) { row.edible = false }
			// guardThrowable, not try/catch: some mods' ArmorMaterial lacks getName (AbstractMethodError, see dump_gear.js)
			if (item instanceof $TieredItem) { var gt = guardThrowable(() => String(item.getTier())); if (gt.ok) row.tier = gt.value }
			if (item instanceof $ArmorItem) { var ga = guardThrowable(() => String(item.getMaterial().getName())); if (ga.ok) row.armorMaterial = ga.value }

			// what ItemPhysic + patcherMod decide for a dropped stack (mirrors ItemPhysicServerMixin.route)
			var now = {}
			try { now.hurtByFire = !!item.canBeHurtBy($DamageSource.IN_FIRE) } catch (e) { now.hurtByFire = null }
			try { now.hurtByLava = !!item.canBeHurtBy($DamageSource.LAVA) } catch (e) { now.hurtByLava = null }
			if (cfg) {
				now.undestroyable = bool(canPass(cfg.undestroyableItems, stack))
				now.ipSwims = bool(canPass(cfg.swimmingItems, stack))
				now.ipBurns = bool(canPass(cfg.burningItems, stack))
			}
			if (SPLIT) {
				var dec = (lava) => { try { var d = SPLIT.burnDecision(stack, lava); return d == null ? null : !!d } catch (e) { return null } }
				now.splitFire = dec(false)
				now.splitLava = dec(true)
				try { now.forcedSink = !!SPLIT.forcedSink(stack) } catch (e) {}
				try { now.splitFloat = !!SPLIT.isFloat(stack) } catch (e) {}
			}
			var burnsIn = (split) => (split != null ? split : (now.ipBurns != null ? now.ipBurns : true))
			var survives = (split, hurt) => !!now.undestroyable || hurt === false || !burnsIn(split)
			row.floats = !now.forcedSink && (!!now.splitFloat || !!now.ipSwims)
			row.survivesFire = survives(now.splitFire, now.hurtByFire)
			row.survivesLava = survives(now.splitLava, now.hurtByLava)
			row.why = []
			if (now.undestroyable) row.why.push('itemphysic_undestroyable')
			if (now.hurtByLava === false) row.why.push('fire_resistant_item')
			if (now.splitLava === false) row.why.push('split_lavaproof')
			if (row.survivesLava) lavaSurvivors++
			row.now = now
			rows.push(row)
		})
		if (!g.ok) { skipped++; console.warn(`[dumpphysics] skipped ${id}: ${g.error}`) }
	})

	writeExportJson('kubejs/exported/physics/item_physics.json', {
		note: 'Written by /dumpphysics (kubejs/server_scripts/_lib/dump_physics.js); read by tools/item_physics_audit.py',
		total: total, skipped: skipped, lavaSurvivors: lavaSurvivors, items: rows,
	})
	console.log(`[dumpphysics] ${rows.length} of ${total} items, ${lavaSurvivors} survive lava (${skipped} skipped)`)
	return { total: total, lavaSurvivors: lavaSurvivors }
}
