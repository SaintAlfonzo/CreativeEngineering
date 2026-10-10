// global.sampleSyringe — shared logic for the Sample Syringe items
// (startup_scripts/items/sample_syringe.js). Right-click/attack handlers live in
// server_scripts/engineering/sample_syringe.js; this just holds the sampleable-mob
// list and builds the filled-syringe stack.
//
// One empty item + one filled item per animal (see items/sample_syringe.js for why) -
// "full" just means "holding one of the filled item ids", no NBT involved.

console.log('sample_syringe func loading')

;(function () {
    const EMPTY_ID = 'creativeengineering:sample_syringe'

    // Sampleable passive mobs. Keyed by entity type id -> { name, filledId }.
    // Written out one entry per line on purpose - add more as they're needed.
    const SAMPLES = {}
    SAMPLES['minecraft:cow'] = { name: 'Cow', filledId: 'creativeengineering:cow_sample_syringe' }
    SAMPLES['more_mooblooms:moobloom'] = { name: 'Moobloom', filledId: 'creativeengineering:cow_sample_syringe' }
    SAMPLES['minecraft:pig'] = { name: 'Pig', filledId: 'creativeengineering:pig_sample_syringe' }
    SAMPLES['minecraft:chicken'] = { name: 'Chicken', filledId: 'creativeengineering:chicken_sample_syringe' }
    SAMPLES['minecraft:sheep'] = { name: 'Sheep', filledId: 'creativeengineering:sheep_sample_syringe' }
    SAMPLES['minecraft:rabbit'] = { name: 'Rabbit', filledId: 'creativeengineering:rabbit_sample_syringe' }
    SAMPLES['minecraft:cod'] = { name: 'Cod', filledId: 'creativeengineering:cod_sample_syringe' }
    SAMPLES['minecraft:salmon'] = { name: 'Salmon', filledId: 'creativeengineering:salmon_sample_syringe' }

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
        // from .glow(true) on the item definition (startup_scripts/items/sample_syringe.js),
        // not NBT, so it always shows - JEI, creative tab, etc. included.
        buildFilledStack(entityTypeId) {
            const sample = SAMPLES[entityTypeId]
            if (!sample) return null

            return Item.of(sample.filledId, 1)
        },
    }
})()
