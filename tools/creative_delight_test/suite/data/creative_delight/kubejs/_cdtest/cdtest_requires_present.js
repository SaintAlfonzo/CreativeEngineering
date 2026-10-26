// requires: oceanic_delight
// Scratch test suite (SQ-CDT): a compartment file for a member that is loaded (nested in the CD jar). Runs.
ServerEvents.recipes(e => {
	global.CD.recipes(() => global.NCH.shapeless(Item.of(global.item.wheat, 6), [global.item.fishing_net], e, 'cdtest/requires_present'))
})
