// requires: creativecompat
// Creative Compat: display names; moved from kubejs/client_scripts/namechanges.js on 2026-11-01.
ClientEvents.lang('en_us', e => {
	// Flower pots (feature_writeup/flower_pots.md 3.4): Supplementaries' 3-plant box; Beautify's hanging pot has rich
	// soil (recipe in creativecompat/kubejs/other/flowers.js; on the ground WTHIT names it Rich Pot, WthitPotContents)
	e.add('block.supplementaries.flower_box', 'Large Flower Box')
	e.add('block.beautify.hanging_pot', 'Rich Hanging Pot')
	// Redeco's jar is a terrarium (TODO ui/names #15)
	e.add('block.redeco.jar', 'Empty Terrarium')

	// Cozy's Improved Cats guide (feature_writeup/cozy_cats.md 10): no genders, no ancient fish (merged into vanilla
	// fish), vanilla cod and salmon tame
	var g = 'guide.cozys-improved-cats.'
	e.add(g + 'genetic_rules.body', 'Coat and class each come from either parent with equal chances.')
	e.add(g + 'kittens.body', 'Both parents need to be ready for breeding.')
	e.add(g + 'stats_cont_2.body', 'Tamed cats have gold names.')
	e.add(g + 'taming.body', 'Approach a wild cat gently with raw cod or salmon. Offer a fish and try again if they aren\'t convinced!\n\nNewly tamed cats sit down. Use an empty hand to let your new friend follow you.')
	e.add(g + 'ancient_fish.title', 'Taming Fish')
	e.add(g + 'ancient_fish.body', 'Raw cod and raw salmon tame wild cats. Catch them by fishing or by hunting fish, or buy them from fishermen.')
	e.add(g + 'ancient_fish_cont_2.title', 'Taming Fish')
	e.add(g + 'ancient_fish_cont_2.body', 'Each fish offered has a 1 in 2 chance to tame the cat. Server settings may change this.')
	e.add(g + 'training_string.body', 'Craft Training String with String, raw Cod and raw Salmon in any arrangement.\n\nSneak-use it on a tamed cat to check and copy their stats. It wears with use, so keep a spare for a busy cat household!')
	e.add(g + 'foods.body', 'Raw cod, salmon, chicken, beef, porkchop, mutton and rabbit are the usual menu.')
	e.add(g + 'kittens_cont_2.body', 'Kittens are wild by default, so win their trust with raw cod or salmon. Their inherited traits are already there; taming won\'t replace them.')
	e.add(g + 'cooked_fish.title', 'Cooked Fish')
	e.add(g + 'cooked_fish.body', 'Cats eat their fish raw. Cooked cod and salmon are for you.')
	e.add(g + 'cooked_fish_cont_2.title', 'Cooked Fish')
	e.add(g + 'cooked_fish_cont_2.body', 'Cooked fish does not tame cats.')

	// Immersive Portals and the Portal Gun ship no names for these entities (WTHIT, /kill, death messages showed raw
	// keys; plan_portal_gun.md 1). Lang merges per namespace, so each key is filed under its own.
	e.add('portalgun', 'entity.portalgun.custom_portal', 'Portal')
	e.add('imm_ptl_core', 'entity.imm_ptl_core.mirror', 'Mirror')
	e.add('imm_ptl_core', 'entity.imm_ptl_core.breakable_mirror', 'Mirror')

	// Immersive Portals' Nether portal block shipped no name (TODO gameplay/tnt-and-portal #26; WTHIT showed the raw key)
	e.add('imm_ptl_core', 'block.imm_ptl_core.nether_portal_block', 'Nether Portal')

	// Design and Decor's tinted framed glass shared Create Framed's names (plan_portal_changes.md 4, M2)
	e.add('design_decor', 'block.design_decor.tinted_framed_glass', 'Smooth Tinted Glass')
	e.add('design_decor', 'block.design_decor.horizontal_tinted_framed_glass', 'Horizontal Smooth Tinted Glass')
	e.add('design_decor', 'block.design_decor.vertical_tinted_framed_glass', 'Vertical Smooth Tinted Glass')
})
