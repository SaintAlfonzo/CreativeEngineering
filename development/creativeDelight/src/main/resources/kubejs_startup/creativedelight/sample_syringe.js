// Moved from the pack's startup_scripts/func/sample_syringe.js 2026-11-01 (creative_split.md 7.6, wave 4), unchanged; CD rebuild + restart.
// Reserved for Creative Cultivation: the sample syringe exists only to start meat cultures, so it is marked with the rest
// of the cultivated-meat code (cultures, culture blocks, their recipes, tags, lang and textures) for a future own mod,
// Creative Cultivation, the way potion brewing and alcohol are marked for Creative Brewing (creative_delight.md 29.5).
//
// global.sampleSyringe — shared logic for the Sample Syringe items
// (sample_syringe_items.js (CD startup tree)). Right-click/attack handlers live in
// CD server creativedelight/sample_syringe.js; this just holds the sampleable-mob
// list and builds the filled-syringe stack.
//
// One empty item + one filled item per animal (see sample_syringe_items.js for why) -
// "full" just means "holding one of the filled item ids", no NBT involved.

console.log('sample_syringe func loading')

;(function () {
    const i = global.item
    const EMPTY_ID = i.sample_syringe

    // Sampleable passive mobs. Keyed by entity type id -> { name, filledId }.
    // Written out one entry per line on purpose - add more as they're needed.
    const SAMPLES = {}
    SAMPLES['minecraft:cow'] = { name: 'Cow', filledId: i.cow_sample_syringe }
    if (global.has('more_mooblooms')) SAMPLES['more_mooblooms:moobloom'] = { name: 'Moobloom', filledId: i.cow_sample_syringe }
    SAMPLES['minecraft:pig'] = { name: 'Pig', filledId: i.pig_sample_syringe }
    SAMPLES['minecraft:chicken'] = { name: 'Chicken', filledId: i.chicken_sample_syringe }
    SAMPLES['minecraft:sheep'] = { name: 'Sheep', filledId: i.sheep_sample_syringe }
    SAMPLES['minecraft:rabbit'] = { name: 'Rabbit', filledId: i.rabbit_sample_syringe }
    SAMPLES['minecraft:cod'] = { name: 'Cod', filledId: i.cod_sample_syringe }
    SAMPLES['minecraft:salmon'] = { name: 'Salmon', filledId: i.salmon_sample_syringe }

    global.sampleSyringe = {
        ITEM_ID: EMPTY_ID,
        SAMPLES: SAMPLES,

        // entityTypeId: e.g. 'minecraft:cow' (from Entity#getType()#getId() as a string)
        isSampleable(entityTypeId) {
            return Object.prototype.hasOwnProperty.call(SAMPLES, entityTypeId)
        },

        // A syringe is "full" once it's any of the filled items - i.e. not the empty one.
        isFull(stack) {
            return String(stack.getItem().getId()) !== EMPTY_ID
        },

        // Builds a fresh filled-syringe stack (count 1). The enchantment glint itself comes
        // from .glow(true) on the item definition (sample_syringe_items.js (CD startup tree)),
        // not NBT, so it always shows - JEI, creative tab, etc. included.
        buildFilledStack(entityTypeId) {
            const sample = SAMPLES[entityTypeId]
            if (!sample) return null

            return Item.of(sample.filledId, 1)
        },
    }

    // JEI "Sample Syringe" world-interaction tab (patcherMod compat/jei/world, row format in WorldRow.java):
    // one row per filled syringe, every mob that gives it as the input. Built from SAMPLES so the list lives here only.
    const byFilled = {}
    const order = []
    Object.keys(SAMPLES).forEach(typeId => {
        const filledId = SAMPLES[typeId].filledId
        if (!byFilled[filledId]) {
            byFilled[filledId] = []
            order.push(filledId)
        }
        byFilled[filledId].push(typeId)
    })
    const rows = order.map(filledId => ({
        inputs: [{ entities: byFilled[filledId] }],
        action: { item: EMPTY_ID, label: 'Right-click or hit the mob' },
        outputs: [{ item: filledId }],
        notes: ['Main hand. A hit also deals normal damage.'],
    }))
    try {
        let $WorldRows = Java.loadClass('com.SaintAlfonzo.creativecompat.compat.jei.world.WorldRows')
        $WorldRows.clear('sample_syringe')
        $WorldRows.add('sample_syringe', JSON.stringify(rows))
    } catch (err) {
        console.warn('sample_syringe: JEI world rows not added: ' + err)
    }
})()
