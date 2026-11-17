// CD rebuild + restart. Reserved for Creative Cultivation: the sample syringe exists only to start meat cultures, so it
// is marked with the rest of the cultivated-meat code (cultures, culture blocks, their recipes, tags, lang and
// textures) for a future own mod, Creative Cultivation, the way potion brewing and alcohol are marked for Creative
// Brewing (creative_delight.md 29.5).
//
// Sample Syringe — right-click (or left-click/attack) a sampleable passive mob to draw
// a sample from it. Split like book/written_book/enchanted_book: one empty, reusable
// syringe item, plus one distinct "filled" item per sampled animal. Filled syringes are
// their own real item ids (not one item + NBT) specifically so recipes can match them by
// plain id - NBT-based ingredient matching (.weakNBT() etc.) kept silently breaking here
// (see creativecommon/_lib/item_dict.js history), so identity now lives in the item type.
//
// Written out one item per line on purpose (no shared helper/array loop) so each animal
// can be added, removed, or debugged independently.
//
// Behavior + the sampleable-mob list: sample_syringe.js (CD startup tree) (global.sampleSyringe)
// Interact/attack handlers:            CD server creativedelight/sample_syringe.js
//
// All variants share the one empty-syringe texture (assets/creativeengineering/textures/item/
// sample_syringe.png). Filled syringes are told apart from the empty one by the .glow(true)
// below, which forces the enchantment glint unconditionally (Item#isFoil always true) -
// same relationship as book vs. enchanted_book.

const SYRINGE_TEXTURE = 'creativedelight:item/sample_syringe'

var reg = global.reg.forPack('creativedelight')
StartupEvents.registry('item', e => {
	reg.item(e, 'sample_syringe')
		.unstackable()
		.group('tools')
		.displayName('Sample Syringe')
		.texture(SYRINGE_TEXTURE)

	reg.item(e, 'cow_sample_syringe')
		.unstackable()
		.group('tools')
		.displayName('Sample Syringe (Cow)')
		.texture(SYRINGE_TEXTURE)
		.containerItem(global.item.sample_syringe)
		.glow(true)

	reg.item(e, 'pig_sample_syringe')
		.unstackable()
		.group('tools')
		.displayName('Sample Syringe (Pig)')
		.texture(SYRINGE_TEXTURE)
		.containerItem(global.item.sample_syringe)
		.glow(true)

	reg.item(e, 'chicken_sample_syringe')
		.unstackable()
		.group('tools')
		.displayName('Sample Syringe (Chicken)')
		.texture(SYRINGE_TEXTURE)
		.containerItem(global.item.sample_syringe)
		.glow(true)

	reg.item(e, 'sheep_sample_syringe')
		.unstackable()
		.group('tools')
		.displayName('Sample Syringe (Sheep)')
		.texture(SYRINGE_TEXTURE)
		.containerItem(global.item.sample_syringe)
		.glow(true)

	reg.item(e, 'rabbit_sample_syringe')
		.unstackable()
		.group('tools')
		.displayName('Sample Syringe (Rabbit)')
		.texture(SYRINGE_TEXTURE)
		.containerItem(global.item.sample_syringe)
		.glow(true)

	reg.item(e, 'cod_sample_syringe')
		.unstackable()
		.group('tools')
		.displayName('Sample Syringe (Cod)')
		.texture(SYRINGE_TEXTURE)
		.containerItem(global.item.sample_syringe)
		.glow(true)

	reg.item(e, 'salmon_sample_syringe')
		.unstackable()
		.group('tools')
		.displayName('Sample Syringe (Salmon)')
		.texture(SYRINGE_TEXTURE)
		.containerItem(global.item.sample_syringe)
		.glow(true)
})
