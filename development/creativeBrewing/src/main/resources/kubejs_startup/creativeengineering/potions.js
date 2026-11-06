// Moved from the pack's startup_scripts/cooking/potions.js 2026-11-01 (creative_brewing.md 4.5).
// Startup script in the CB jar: runs after the pack folder and CD; CB rebuild + restart.
// ============================================================================
//  ALL custom-potion + potion-brewing logic for the pack lives in this one file.
//
//   1. StartupEvents.registry('potion')   — register the creativebrewing:* potions
//   2. MoreJSEvents.registerPotionBrewing  — brewing-stand mixes as item-agnostic
//        PotionBrewing.Mix entries (so they ALSO drive Create Automatic Brewing,
//        the Create Mechanical Mixer, and the IE Mixer multiblock — none of those
//        need their own recipes)
//   3. StartupEvents.postInit              — drop Create's dragon's-breath
//        Automatic-Brewing recipe (patcherMod does that conversion as a fluid brew)
//
//  Machine coverage (nothing else to write anywhere):
//   • Brewing stand       — the Mix entries below.
//   • Create Auto Brewing — Create builds PotionMixingRecipes from PotionBrewing
//        .POTION_MIXES after common setup (1000 mB, automatic, shows in JEI).
//   • Create Mechanical Mixer — same list.
//   • IE Mixer multiblock — IE's mixer_potion_list is generated from the same
//        PotionBrewing.POTION_MIXES; patcherMod PotionRecipeGeneratorsMixin
//        redirects its fluid in/out to create:potion @ 1000 mB, and
//        PotionHelperMixin un-skips the thick / mundane base potions so brews
//        from those bases are craftable on the multiblock too.
//   • Splash / lingering  — vanilla CONTAINER_MIXES (universal) + patcherMod's
//        dragon-breath fluid brew. Nothing to add.
//
//  Not here (separate machines, separate files / memory):
//   • create:potion  ->  potion / splash / lingering ITEM  — engineering/potion_bottling.js
//   • create:potion  ->  tipped arrow                       — equipment/tipped_arrows.js
// ============================================================================

const S = 20 // ticks per second

// name -> effect, timings, and how the BASE potion is brewed.
//   dur / long  : ticks         strong : [dur, amplifier]
//   base        : vanilla base potion (awkward | thick | mundane)
//   reagent     : brewing-stand ingredient for the base brew
// lengthen step is always redstone, amplify step is always glowstone dust.
const CUSTOM_POTIONS = {
	resistance:     { effect: 'minecraft:resistance',     dur: 180 * S, long: 480 * S, strong: [90 * S, 1],  base: 'awkward', reagent: 'minecraft:iron_block' },
	health_boost:   { effect: 'minecraft:health_boost',   dur: 180 * S, long: 480 * S, strong: [90 * S, 1],  base: 'thick',   reagent: 'minecraft:golden_apple' },
	absorption:     { effect: 'minecraft:absorption',     dur: 120 * S, long: 300 * S, strong: [120 * S, 1], base: 'mundane', reagent: 'minecraft:golden_apple' },
	// no haste here — Rustic Delight already registers rusticdelight:haste (see RD_POTION_MIXES)
	glowing:        { effect: 'minecraft:glowing',        dur: 90 * S,  long: 240 * S,                       base: 'awkward', reagent: 'minecraft:glow_ink_sac' },
	saturation:     { effect: 'minecraft:saturation',     dur: 5,                                             base: 'thick',   reagent: 'minecraft:golden_carrot' },
	wither:         { effect: 'minecraft:wither',         dur: 40 * S,  long: 90 * S,  strong: [20 * S, 1],   base: 'mundane', reagent: 'minecraft:wither_rose' },
	levitation:     { effect: 'minecraft:levitation',     dur: 40 * S,  long: 90 * S,  strong: [20 * S, 1],   base: 'awkward', reagent: ['ends_delight:shulker_meat_slice', 'minecraft:shulker_shell'] },
	mining_fatigue: { effect: 'minecraft:mining_fatigue', dur: 90 * S,  long: 240 * S, strong: [30 * S, 1],   base: 'awkward', reagent: 'minecraft:prismarine_crystals' },
	hunger:         { effect: 'minecraft:hunger',         dur: 30 * S,  long: 60 * S,  strong: [15 * S, 1],   base: 'thick',   reagent: 'minecraft:rotten_flesh' },
	nausea:         { effect: 'minecraft:nausea',         dur: 30 * S,  long: 60 * S,                         base: 'thick',   reagent: 'minecraft:pufferfish' },
	blindness:      { effect: 'minecraft:blindness',      dur: 30 * S,  long: 60 * S,                         base: 'mundane', reagent: 'minecraft:ink_sac' },
	darkness:       { effect: 'minecraft:darkness',       dur: 30 * S,  long: 60 * S,                         base: 'mundane', reagent: 'minecraft:echo_shard' },
}

// long_reach / phantom_bane use a modded effect (better_beacons:*) — KubeJS's own
// StartupEvents.registry('potion', ...) runs too early to ever see it (see CE potions
// header comment below and patcherMod's CEPotions), so patcherMod registers
// creativebrewing:long_reach / phantom_bane (+ long_/strong_ variants) as REAL
// DeferredRegister<Potion> entries instead. Brewing mixes for them are added below
// alongside AM_POTION_MIXES. [reagent, fromPotion, toPotion]
const CE_POTION_MIXES = [
	// pack-original effect (CB's CEPotions.java, same timing dance as
	// above since KubeJS's own 'potion' registry runs too early to see even its own
	// 'mob_effect' registrations - see magic_resistance.js). amethyst_shard is a placeholder
	// reagent - retune later.
	['minecraft:amethyst_shard', 'minecraft:thick',                    'creativebrewing:magic_resistance'],
	['minecraft:redstone',       'creativebrewing:magic_resistance',   'creativebrewing:long_magic_resistance'],
	['minecraft:glowstone_dust', 'creativebrewing:magic_resistance',   'creativebrewing:strong_magic_resistance'],
]
// Long Reach / Phantom Bane exist only with Better Beacons (CEPotions skips them otherwise); their base brews use
// Geode's shards.
if (global.has('better_beacons')) {
	CE_POTION_MIXES.push(
		['minecraft:redstone',       'creativebrewing:long_reach',   'creativebrewing:long_long_reach'],
		['minecraft:glowstone_dust', 'creativebrewing:long_reach',   'creativebrewing:strong_long_reach'],
		['minecraft:redstone',       'creativebrewing:phantom_bane', 'creativebrewing:long_phantom_bane'])
	// no glowstone step: Phantom Bane is levelless (CEPotions, like Night Vision)
	if (global.has('geode')) {
		CE_POTION_MIXES.push(
			['geode:wrappist_shard',  'minecraft:thick', 'creativebrewing:long_reach'],
			['geode:celestite_shard', 'minecraft:thick', 'creativebrewing:phantom_bane'])
	}
}

const LENGTHEN_REAGENT = 'minecraft:redstone'
const AMPLIFY_REAGENT = 'minecraft:glowstone_dust'

// Luck / Unluck: the potion types are vanilla minecraft:luck plus buzzier_bees's unluck and long_ variants (no
// CUSTOM_POTIONS registration needed). Buzzier Bees brews them itself (clover base, redstone / glowstone steps,
// fermented spider eye corruption); those mixes are all removed in section 2 and these are the only luck brews.
// Base brew + lengthen + amplify; no fermented spider eye corruption step.
// [reagent, fromPotion, toPotion]

//TODO: give better/magic brewing ingredient
const LUCK_POTION_MIXES = []
// every potion a Buzzier Bees luck mix can output; removeByPotion(null, null, <this>) clears them
const LUCK_POTIONS = []
if (global.has('buzzier_bees')) {
	LUCK_POTION_MIXES.push(
	[global.item.four_leaf_clover,  'minecraft:awkward',   'minecraft:luck'],
	[global.item.redstone,          'minecraft:luck',      'buzzier_bees:long_luck'],
	[global.item.glowstone,         'minecraft:luck',      'buzzier_bees:strong_luck'],

	[global.item.silverfish_bottle, 'minecraft:awkward',   'buzzier_bees:unluck'],
	[global.item.redstone,          'buzzier_bees:unluck', 'buzzier_bees:long_unluck'],
	[global.item.glowstone,         'buzzier_bees:unluck', 'buzzier_bees:strong_unluck'])
	LUCK_POTIONS.push('minecraft:luck', 'buzzier_bees:long_luck', 'buzzier_bees:strong_luck',
		'buzzier_bees:unluck', 'buzzier_bees:long_unluck', 'buzzier_bees:strong_unluck')
}
// potions/effects #9: Resilience (Quark) duplicates Knockback Resistance and goes; Restfulness and Insomnia (Upgrade
// Aquatic) go with the Flare, which only an Insomnia potion on a Phantom can make. Every mix that outputs one is
// removed; the effects themselves are refused or swapped by Creative Brewing ([effects] remap / removed).
const REMOVED_POTIONS = []
if (global.has('quark')) {
	REMOVED_POTIONS.push('quark:resilience', 'quark:long_resilience', 'quark:strong_resilience')
}
if (global.has('upgrade_aquatic')) {
	REMOVED_POTIONS.push('upgrade_aquatic:restfulness', 'upgrade_aquatic:restfulness_strong',
		'upgrade_aquatic:insomnia', 'upgrade_aquatic:insomnia_strong')
}

// Haste — Rustic Delight registers rusticdelight:haste / long_haste / strong_haste. 1.3.0 brews
// them through its own IBrewingRecipe class (regular potion item only); Creative Delight's 1.7.0
// port registers no brewing at all, so these mixes are the only route there. Create's
// PotionMixingRecipes and IE's PotionHelper only read `instanceof BrewingRecipe` from the
// Forge registry, so RD's haste is invisible to every mixer and has no splash/lingering
// modifier steps. Re-add the same three steps as item-agnostic PotionBrewing.Mix entries
// (same reagents as RD). [reagent, fromPotion, toPotion]
const RD_POTION_MIXES = [
	[global.item.golden_coffee_beans, 'minecraft:awkward',     'rusticdelight:haste'],
	['minecraft:redstone',            'rusticdelight:haste',   'rusticdelight:long_haste'],
	['minecraft:glowstone_dust',      'rusticdelight:haste',   'rusticdelight:strong_haste'],
]

// Alex's Mobs modifier steps. AM locks its own brews to the regular potion item +
// exact NBT, so lengthen / amplify / upgrade stop working once a potion is
// splash / lingering and are invisible to the mixers. Re-add them as item-agnostic
// PotionBrewing.Mix entries. [reagent, fromPotion, toPotion]
const AM_POTION_MIXES = []
if (global.has('alexsmobs')) {
	AM_POTION_MIXES.push(
	['minecraft:redstone',       'alexsmobs:knockback_resistance', 'alexsmobs:long_knockback_resistance'],
	['minecraft:redstone',       'alexsmobs:lava_vision',          'alexsmobs:long_lava_vision'],
	['minecraft:redstone',       'alexsmobs:bug_pheromones',       'alexsmobs:long_bug_pheromones'],
	['minecraft:redstone',       'alexsmobs:soulsteal',            'alexsmobs:long_soulsteal'],
	['minecraft:redstone',       'alexsmobs:clinging',             'alexsmobs:long_clinging'],
	['alexsmobs:komodo_spit',    'alexsmobs:poison_resistance',    'alexsmobs:long_poison_resistance'],
	['minecraft:glowstone_dust', 'alexsmobs:knockback_resistance', 'alexsmobs:strong_knockback_resistance'],
	['minecraft:glowstone_dust', 'alexsmobs:soulsteal',            'alexsmobs:strong_soulsteal'],
	['alexsmobs:gazelle_horn',   'minecraft:strong_swiftness',     'alexsmobs:speed_iii'],

	// AM brews poison_resistance from an alexsmobs:poison_bottle item — the mixers can't see a
	// non-potion base (patcherMod cancels the broken auto-generated {Potion:empty} recipe).
	// Re-base it onto Potion of Poison II so it brews everywhere (stand + both mixers) and
	// picks up splash/lingering parity for free. AM's poison-bottle stand recipe still works.
	['alexsmobs:centipede_leg',  'minecraft:strong_poison',        'alexsmobs:poison_resistance'])
	// crab shell: Quark's Resilience reagent, now a second Knockback Resistance base next to AM's Strength + bear fur
	if (global.has('quark')) {
		AM_POTION_MIXES.push([global.item.crab_shell, 'minecraft:awkward', 'alexsmobs:knockback_resistance'])
	}
}

// ── 1. register the creativebrewing:* potions ────────────────────────────────────────
// CUSTOM_POTIONS entries must use a VANILLA effect only. KubeJS evaluates this whole
// callback in one early pass (to gather everything before real Forge registration
// begins) — well before any OTHER mod's DeferredRegister<MobEffect> has fired, so a
// modded effect id always resolves to null here regardless of which registry API is
// used (Registry.MOB_EFFECT / ForgeRegistries.MOB_EFFECTS — same result, same timing).
// For a modded effect (e.g. better_beacons:long_reach / phantom_bane), register the
// potion in patcherMod instead, as a real DeferredRegister<Potion> — see
// CEPotions.java + CE_POTION_MIXES below.
StartupEvents.registry('potion', event => {
	const EFFECTS = Java.loadClass('net.minecraft.core.Registry').MOB_EFFECT
	const ResourceLocation = Java.loadClass('net.minecraft.resources.ResourceLocation')

	Object.keys(CUSTOM_POTIONS).forEach(name => {
		const p = CUSTOM_POTIONS[name]
		const eff = EFFECTS.get(new ResourceLocation(p.effect))
		if (!eff) {
			console.warn(`[CE potions] effect '${p.effect}' not registered — skipped ${name}`)
			return
		}
		const amp = p.amp || 0
		event.create('creativebrewing:' + name).effect(eff, p.dur, amp)
		if (p.long) event.create('creativebrewing:long_' + name).effect(eff, p.long, amp)
		if (p.strong) event.create('creativebrewing:strong_' + name).effect(eff, p.strong[0], p.strong[1])
	})
})

// ── 2. brewing mixes (brewing stand + every mixer) ──────────────────────────
MoreJSEvents.registerPotionBrewing(event => {
	const Registry = Java.loadClass('net.minecraft.core.Registry')
	const ResourceLocation = Java.loadClass('net.minecraft.resources.ResourceLocation')
	const POTIONS = Registry.POTION

	const potion = id => {
		const rl = new ResourceLocation(id)
		return POTIONS.containsKey(rl) ? POTIONS.get(rl) : null
	}

	// A reagent is an id, a #tag, or a list of item ids: the first registered one is used (the levitation reagent is
	// End's Delight's shulker meat slice, the merge target of Ender's filet; shulker shell if the slice is missing).
	// An unknown item or an empty ingredient skips that one mix with a warning instead of failing the whole event.
	const ITEMS = Registry.ITEM
	const known = id => String(id).startsWith('#') || ITEMS.containsKey(new ResourceLocation(id))
	let added = 0
	const mix = (reagent, fromId, toId) => {
		const from = potion(fromId)
		const to = potion(toId)
		if (from === null || to === null) {
			console.warn(`[CE potions] mix skipped: ${fromId} + ${reagent} -> ${toId} (potion not registered)`)
			return
		}
		const pick = Array.isArray(reagent) ? reagent.filter(known)[0] : (known(reagent) ? reagent : undefined)
		let ing = null
		try { ing = pick === undefined ? null : Ingredient.of(pick) } catch (e) { ing = null }
		if (ing === null || ing.isEmpty()) {
			console.warn(`[CE potions] mix skipped: ${fromId} + ${reagent} -> ${toId} (reagent not registered or empty)`)
			return
		}
		// addPotionBrewing(reagentIngredient, fromPotion, toPotion) -> real PotionBrewing.Mix
		event.addPotionBrewing(ing, from, to)
		added++
	}

	// custom potions: base brew, then lengthen / amplify
	Object.keys(CUSTOM_POTIONS).forEach(name => {
		const p = CUSTOM_POTIONS[name]
		mix(p.reagent, 'minecraft:' + p.base, 'creativebrewing:' + name)
		if (p.long) mix(LENGTHEN_REAGENT, 'creativebrewing:' + name, 'creativebrewing:long_' + name)
		if (p.strong) mix(AMPLIFY_REAGENT, 'creativebrewing:' + name, 'creativebrewing:strong_' + name)
	})

	// Alex's Mobs modifier steps
	AM_POTION_MIXES.forEach(row => mix(row[0], row[1], row[2]))

	// patcherMod-registered potions (modded effect, can't go through CUSTOM_POTIONS — see above)
	CE_POTION_MIXES.forEach(row => mix(row[0], row[1], row[2]))

	// Luck / Unluck — Buzzier Bees' own mixes out (MoreJS fires in client / dedicated-server setup, after
	// its commonSetup enqueueWork registered them), then the pack's base + lengthen rows
	LUCK_POTIONS.forEach(id => {
		const p = potion(id)
		if (p !== null) event.removeByPotion(null, null, p)
	})
	LUCK_POTION_MIXES.forEach(row => mix(row[0], row[1], row[2]))

	// Resilience, Restfulness, Insomnia: out (REMOVED_POTIONS)
	REMOVED_POTIONS.forEach(id => {
		const p = potion(id)
		if (p !== null) event.removeByPotion(null, null, p)
	})

	// Haste — Rustic Delight's own potions, made item-agnostic
	RD_POTION_MIXES.forEach(row => mix(row[0], row[1], row[2]))

	console.log(`[CE potions] added ${added} item-agnostic potion mixes`)
})

// ── 3. Create Automatic Brewing: rebuild the recipe list, then blacklist ────
// PotionMixingRecipes.ALL / BY_ITEM are filled in a static {} block that runs the
// FIRST time the class is touched — which Create does DURING mod setup, i.e. before
// MoreJS (FMLCommonSetupEvent) has added our PotionBrewing.Mix entries. So the custom
// creativebrewing:* potions never make it into Automatic Brewing.
//
// postInit (FMLLoadComplete) is always after common setup, so here we reflectively
// re-run PotionMixingRecipes.createRecipes() / sortRecipesByItem() against the now-
// complete PotionBrewing state and swap the results in. Then apply the dragon's-breath
// blacklist (lingering conversion — patcherMod does that as a fluid brew instead).
StartupEvents.postInit(() => {
	const PMR = Java.loadClass('com.simibubi.create.content.fluids.potion.PotionMixingRecipes')
	// KubeJS' Rhino has no ".class" (that's Nashorn syntax) — Java.loadClass() hands back a
	// NativeJavaClass wrapper, and asking it for ".class" makes it hunt for a nested class
	// named "$class" and throw. "__javaObject__" is Rhino's accessor for the real
	// java.lang.Class instance behind the wrapper, which is what reflection needs.
	const PMR_CLASS = PMR.__javaObject__
	const LIST_CLASS = Java.loadClass('java.util.List').__javaObject__

	// Same Rhino quirk as the while loop below: a const/let inside a nested block
	// (this try) throws "redeclaration of var". Declare once with function-scoped
	// var, assign inside the try.
	var createRecipes, sortRecipesByItem, fresh, freshByItem, DBB
	try {
		createRecipes = PMR_CLASS.getDeclaredMethod('createRecipes')
		sortRecipesByItem = PMR_CLASS.getDeclaredMethod('sortRecipesByItem', LIST_CLASS)
		createRecipes.setAccessible(true)
		sortRecipesByItem.setAccessible(true)

		fresh = createRecipes.invoke(null)
		freshByItem = sortRecipesByItem.invoke(null, fresh)

		PMR.ALL.clear()
		PMR.ALL.addAll(fresh)
		PMR.BY_ITEM.clear()
		PMR.BY_ITEM.putAll(freshByItem)
		console.log(`[CE potions] Automatic Brewing: rebuilt ${PMR.ALL.size()} recipes from PotionBrewing`)

		// patcherMod's DragonBreathBrewing appended its splash-fluid + dragon's-breath-fluid
		// -> lingering-fluid recipes to PMR.ALL during commonSetup — the clear() above (postInit,
		// runs later) just wiped them, so the dragon-breath lingering brews vanished from the
		// JEI "Automatic Brewing" category. FLUID_BREWING still holds them; re-append so JEI
		// shows them again. They carry no item ingredient, so the dragon_breath blacklist
		// loop below (item-ingredient test) leaves them alone.
		try {
			DBB = Java.loadClass('com.SaintAlfonzo.creativebrewing.fluidbrewing.DragonBreathBrewing')
			PMR.ALL.addAll(DBB.FLUID_BREWING)
			console.log(`[CE potions] Automatic Brewing: re-added ${DBB.FLUID_BREWING.size()} dragon-breath fluid brews`)
		} catch (dbErr) {
			console.warn(`[CE potions] could not re-add DragonBreathBrewing fluid brews: ${dbErr}`)
		}
	} catch (err) {
		console.error(`[CE potions] Automatic Brewing rebuild FAILED (custom potions may be missing): ${err}`)
	}

	// reagent ids whose Automatic-Brewing recipes should be removed
	const BLACKLIST = ['minecraft:dragon_breath'] // lingering — replaced by patcherMod's fluid brew

	const stacks = BLACKLIST.map(id => Item.of(id))

	// declare loop-body vars ONCE with var — Rhino throws "redeclaration of var"
	// on a const/let inside a while loop.
	let removed = 0
	var ings, hit, k, s
	const it = PMR.ALL.iterator()
	while (it.hasNext()) {
		ings = it.next().getIngredients()
		hit = false
		for (k = 0; k < ings.size() && !hit; k++)
			for (s = 0; s < stacks.length && !hit; s++)
				if (ings.get(k).test(stacks[s])) hit = true
		if (hit) {
			it.remove()
			removed++
		}
	}
	stacks.forEach(st => PMR.BY_ITEM.remove(st.getItem()))

	console.log(`[CE potions] Automatic Brewing: removed ${removed} recipe(s) for [${BLACKLIST.join(', ')}]`)
})
