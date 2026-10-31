// Stepstools — one per Another Furniture tall stool colour. Worn in the Curios feet slot: stand still and hold
// Sprint + Jump (or the "Step Up" key) to stand on the stool, a block higher; moving, crouching or jumping steps off.
//
// The items are pure data and borrow Another Furniture's tall stool models, so each one looks like its stool.
// Everything else lives in patcherMod's stepstool/ package (key handling, collision, the stool drawn under the
// player); knobs in config/creativeengineering-common.toml under [stepstool]. Recipe: server_scripts/equipment/stepstool.js.

const STEPSTOOL_COLOURS = ['white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray',
	'light_gray', 'cyan', 'purple', 'blue', 'brown', 'green', 'red', 'black']

function stepstoolName(colour) {
	return colour.split('_').map(w => w.charAt(0).toUpperCase() + w.slice(1)).join(' ') + ' Stepstool'
}

StartupEvents.registry('item', event => {
	STEPSTOOL_COLOURS.forEach(colour => {
		event.create(global.item[colour + '_stepstool'])
			.maxStackSize(16)
			.group('tools')
			.displayName(stepstoolName(colour))
			.parentModel('another_furniture:block/tall_stool/' + colour)
	})
})
