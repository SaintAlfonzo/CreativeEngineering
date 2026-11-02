// requires: cdtest_not_a_mod
// Scratch test suite (SQ-CDT): a compartment file for a mod that is not loaded. KubeJS must skip the whole file.
global.cdtestAbsentRan = true
ServerEvents.recipes(e => {
	global.CD.recipes(() => global.NCH.shapeless(global.item.wheat, [global.item.bread], e, 'cdtest/requires_absent'))
})
