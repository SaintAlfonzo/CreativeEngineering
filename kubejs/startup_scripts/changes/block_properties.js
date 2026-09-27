// Block property overrides for other mods' blocks (BlockEvents.modification runs at startup).
// Walk speed belongs to BlockRunner's tiers (feature_writeup/blockrunner.md), so native movement
// tweaks that would stack on top of a tier are reset to vanilla here.

BlockEvents.modification(e => {
	// Create D&D asphalt ships friction 0.35 (vanilla 0.6): about 3.4x top walking speed on its own,
	// stacked on BlockRunner's asphalt tier. Its speedFactor 0.001 is hidden while BlockRunner tiers
	// the block, but would freeze anyone walking on it if that ever stopped. jumpFactor 1.25 is kept.
	var colors = ['white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray',
		'light_gray', 'cyan', 'purple', 'blue', 'brown', 'green', 'red', 'black']
	colors.forEach(color => {
		e.modify(global.item[color + '_asphalt_block'], block => {
			block.setFriction(0.6)
			block.setSpeedFactor(1.0)
		})
	})
})
