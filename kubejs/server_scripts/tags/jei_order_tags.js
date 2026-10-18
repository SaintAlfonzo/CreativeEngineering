// creativeengineering:jei/<path> tags for the pack's JEI order: one tag per node of the definition
// (startup_scripts/jei/order.js) that has members (`list` + `from`). patcherMod's PackIngredientOrder
// reads them on the client to place each ingredient. Helper: startup_scripts/func/jei_order.js.
// Multiblocks have no registry, so they have no tags (their nodes place by `list` on the client).
ServerEvents.tags('item', e => global.jeiOrder.tag(e, 'item'))
ServerEvents.tags('fluid', e => global.jeiOrder.tag(e, 'fluid'))
ServerEvents.tags('mob_effect', e => global.jeiOrder.tag(e, 'effect'))
