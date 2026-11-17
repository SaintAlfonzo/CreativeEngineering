// requires: creativedelight
// requires: miners_delight
// Squid drops for the squid chain (TODO dishes/ocean #3): a squid drops one Miner's Delight squid item and a glow squid
// one glow squid item, every kill, knife or not; the ink and the calamari come from cutting it
// (miners_delight/recipes.js). The rule is post-roll (Creative Loot's L.onlyDrop, a LootJS modifier), after every mod's
// loot modifiers, so this also takes out what they add: Rustic's calamari, Miner's knife squid item, and the tentacles
// of Miner's, Ocean's and Oceanic Delight (those items are removed, dishes/ocean #4). Exactly one (TODO dishes/ocean
// #9): every other drop goes, whatever added it, so looting, a knife or another mod's modifier can't add a second squid
// or anything else.

var R = global.NCH.forPack('creativedelight')

// Cave carrot crate split out of the loose cave carrot's entry, like the Farmer's Delight crates (pre-roll loot
// framework, Creative Loot loot/05_tuning.js). miners_delight.md I5.
// Queued with global.LOOT.rules (Creative Loot's loot engine, loot/05_tuning.js): its script pack can load
// after this one, so the rules run when the engine of the current reload exists. Creative Delight requires Creative Loot.
//
// Miner's Delight drops (creativedelight members #51, miners_delight.md D8): the donor's loot modifiers are not served
// (MinersTranslator); these rules give the same drops at the same rates, as loot pools in the vanilla tables
// (L.drop / L.dropShare, loot/09_drops.js), so a rate is one number here and /reload applies it. A mob that dies on
// fire (Fire Aspect, lava, sun) drops the cooked item instead of the raw one. "Knife" is #forge:tools/knives in the
// killer's main hand. Squid and glow squid kills are the squid chain's (L.onlyDrop above); only fishing is here.
global.LOOT.rules(function (L) {
	var i = global.item
	var SPIDER = 'minecraft:entities/spider'
	var CAVE_SPIDER = 'minecraft:entities/cave_spider'
	var SILVERFISH = 'minecraft:entities/silverfish'
	var BAT = 'minecraft:entities/bat'
	// the block tag miners_delight:infested_blocks
	var INFESTED = ['stone', 'stone_bricks', 'cracked_stone_bricks', 'mossy_stone_bricks', 'chiseled_stone_bricks', 'deepslate']
		.map(function (b) { return 'minecraft:blocks/infested_' + b })

	L.pack(i.cave_carrot_crate, i.cave_carrot)

	// The squid chain (top of this file): one squid item per kill, nothing else
	L.onlyDrop('minecraft:squid', i.squid, { logName: R.logName('squid_chain/squid') })
	L.onlyDrop('minecraft:glow_squid', i.glow_squid, { logName: R.logName('squid_chain/glow_squid') })

	// Abandoned mineshaft chests also roll Miner's own table (cave carrots, copper carrots, ...)
	L.addPool('minecraft:chests/abandoned_mineshaft',
		{ rolls: 1, entries: [{ type: 'minecraft:loot_table', name: 'miners_delight:chests/md_abandoned_mineshaft' }] },
		{ name: 'miners_delight' })

	// L.drop / L.dropShare come with a Creative Loot jar that has loot/09_drops.js: an older one gives no Miner's drops
	if (!L.drop || !L.dropShare) {
		console.error('[miners_delight] the loot engine has no drop rules (Creative Loot older than loot/09_drops.js): ' +
			'no bat wing, arthropod, spider leg, silverfish egg or fished squid drops')
		return
	}

	// Bats: 2 wings with a knife, else 50% of 1
	L.drop(BAT, i.bat_wing, { knife: true, count: 2, cooked: i.smoked_bat_wing })
	L.drop(BAT, i.bat_wing, { knife: false, chance: 0.5, cooked: i.smoked_bat_wing })

	// Spiders and cave spiders, knife: 1-3 arthropods; 20% cobweb (not burning)
	L.drop([SPIDER, CAVE_SPIDER], i.arthropod, { knife: true, count: [1, 3], cooked: i.cooked_arthropod })
	L.drop([SPIDER, CAVE_SPIDER], i.cobweb, { knife: true, chance: 0.2, burning: false })
	// Spider legs, knife: 40% + 10% per Looting; 0-4 raw from spiders, 1 baked from spiders and cave spiders on fire
	L.drop(SPIDER, i.spider_leg, { knife: true, chance: 0.4, looting: 0.1, count: [0, 4], cooked: { item: i.baked_spider_leg, count: 1 } })
	L.drop(CAVE_SPIDER, i.baked_spider_leg, { knife: true, chance: 0.4, looting: 0.1, burning: true })

	// Silverfish: 1 arthropod with a knife; eggs 50% with a knife, 5% without
	L.drop(SILVERFISH, i.arthropod, { knife: true, cooked: i.cooked_arthropod })
	L.drop(SILVERFISH, i.silverfish_eggs, { knife: true, chance: 0.5 })
	L.drop(SILVERFISH, i.silverfish_eggs, { knife: false, chance: 0.05 })

	// Infested blocks broken with a pickaxe: 75% of 1-3 silverfish eggs
	L.drop(INFESTED, i.silverfish_eggs, { tool: 'pickaxe_dig', chance: 0.75, count: [1, 3] })

	// Fishing: 5% of catches are a squid, 0.475% a glow squid (the donor's 0.5% replace, checked before the squid's)
	L.dropShare('minecraft:gameplay/fishing', i.squid, 0.05)
	L.dropShare('minecraft:gameplay/fishing', i.glow_squid, 0.00475)
})
