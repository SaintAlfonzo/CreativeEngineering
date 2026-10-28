// Reserved for Creative Cultivation: the sample syringe exists only to start meat cultures, so it is marked with the rest
// of the cultivated-meat code (cultures, culture blocks, their recipes, tags, lang and textures) for a future own mod,
// Creative Cultivation, the way potion brewing and alcohol are marked for Creative Brewing (creative_delight.md 29.5).
//
// Sample Syringe — right-click (or left-click/attack) a sampleable passive mob to draw
// a sample from it. Split like book/written_book/enchanted_book: one empty, reusable
// syringe item, plus one distinct "filled" item per sampled animal. Filled syringes are
// their own real item ids (not one item + NBT) specifically so recipes can match them by
// plain id - NBT-based ingredient matching (.weakNBT() etc.) kept silently breaking here
// (see startup_scripts/_lib/item_dict.js history), so identity now lives in the item type.
//
// Written out one item per line on purpose (no shared helper/array loop) so each animal
// can be added, removed, or debugged independently.
//
// Behaviour + the sampleable-mob list: startup_scripts/func/sample_syringe.js (global.sampleSyringe)
// Interact/attack handlers:            server_scripts/engineering/sample_syringe.js
//
// All variants share the one empty-syringe texture (assets/creativeengineering/textures/item/
// sample_syringe.png). Filled syringes are told apart from the empty one by the .glow(true)
// below, which forces the enchantment glint unconditionally (Item#isFoil always true) -
// same relationship as book vs. enchanted_book.

const SYRINGE_TEXTURE = 'creativeengineering:item/sample_syringe'

StartupEvents.registry('item', e => {
    e.create('creativeengineering:sample_syringe')
        .unstackable()
        .group('tools')
        .displayName('Sample Syringe')
        .texture(SYRINGE_TEXTURE)

    e.create('creativeengineering:cow_sample_syringe')
        .unstackable()
        .group('tools')
        .displayName('Sample Syringe (Cow)')
        .texture(SYRINGE_TEXTURE)
        .containerItem('creativeengineering:sample_syringe')
        .glow(true)

    e.create('creativeengineering:pig_sample_syringe')
        .unstackable()
        .group('tools')
        .displayName('Sample Syringe (Pig)')
        .texture(SYRINGE_TEXTURE)
        .containerItem('creativeengineering:sample_syringe')
        .glow(true)

    e.create('creativeengineering:chicken_sample_syringe')
        .unstackable()
        .group('tools')
        .displayName('Sample Syringe (Chicken)')
        .texture(SYRINGE_TEXTURE)
        .containerItem('creativeengineering:sample_syringe')
        .glow(true)

    e.create('creativeengineering:sheep_sample_syringe')
        .unstackable()
        .group('tools')
        .displayName('Sample Syringe (Sheep)')
        .texture(SYRINGE_TEXTURE)
        .containerItem('creativeengineering:sample_syringe')
        .glow(true)

    e.create('creativeengineering:rabbit_sample_syringe')
        .unstackable()
        .group('tools')
        .displayName('Sample Syringe (Rabbit)')
        .texture(SYRINGE_TEXTURE)
        .containerItem('creativeengineering:sample_syringe')
        .glow(true)

    e.create('creativeengineering:cod_sample_syringe')
        .unstackable()
        .group('tools')
        .displayName('Sample Syringe (Cod)')
        .texture(SYRINGE_TEXTURE)
        .containerItem('creativeengineering:sample_syringe')
        .glow(true)

    e.create('creativeengineering:salmon_sample_syringe')
        .unstackable()
        .group('tools')
        .displayName('Sample Syringe (Salmon)')
        .texture(SYRINGE_TEXTURE)
        .containerItem('creativeengineering:sample_syringe')
        .glow(true)
})
