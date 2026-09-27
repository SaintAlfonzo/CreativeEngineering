// Resolves the texture resource-location string for an item's currently baked model,
// in the same format .texture(...) expects (e.g. 'quarkdelight:items/crab_bars').
// Must be called after resources are loaded (e.g. from the client console once
// in-game, not from ClientEvents.init) since it needs a real baked model.
function getItemTextureLocation(itemId) {
	try {
		var mc = Java.loadClass('net.minecraft.client.Minecraft').getInstance();
		var stack = Item.of(itemId);
		var model = mc.getItemRenderer().getModel(stack, mc.level, null, 0);
		return model.getParticleIcon().getName().toString();
	} catch (err) {
		console.error('[textureTools] failed to resolve texture for ' + itemId + ': ' + err);
		return null;
	}
}

// Prints the location to chat + console in a copy-paste-ready form.
function printItemTexture(itemId) {
	var loc = getItemTextureLocation(itemId);
	if (!loc) return;
	console.log("[textureTools] " + itemId + " -> .texture('" + loc + "')");
	if (Client.player) Client.player.tell("§b" + itemId + " §7-> §a.texture('" + loc + "')");
}

global.getItemTextureLocation = getItemTextureLocation;
global.printItemTexture = printItemTexture;

// The 29 sequenced-assembly outputs currently falling back to i.incomplete
// (see engineering/redstone.js, transport.js, crystal.js, metal/metal_building.js,
// metal/misc_metal_crafts.js, cooking/cooking.js). Deduped — daylight_sensor has two
// recipes but only one output item, so only one texture lookup is needed for it.
var pendingIncompleteItems = [
	'redstone_torch', 'daylight_sensor', 'rose_quartz_lamp', 'transmitter',
	'redstone_link', 'repeater', 'fast_repeater', 'slow_repeater', 'comparator',
	'redstone_randomizer', 'rain_detector', 'thunder_detector',
	'rail', 'll_spring',
	'budding_amethyst', 'echo_shard',
	'copper_bars', 'iron_bars', 'cast_iron_bars', 'gold_bars', 'zinc_bars', 'brass_bars',
	'spool', 'chain',
	'bbq_stick', 'grilled_salmon', 'bacon_and_eggs', 'roasted_mutton_chops'
];

// Resolves each key's own item texture and prints a ready-to-paste
// R.createIncompleteItem(...) line for transition_items.js.
function dumpIncompleteItemTextures(keys) {
	var i = global.item;
	(keys || pendingIncompleteItems).forEach(key => {
		var id = i[key];
		if (!id) {
			console.error('[textureTools] global.item.' + key + ' is not defined — skipping');
			return;
		}
		var loc = getItemTextureLocation(id);
		if (!loc) return;
		var line = "R.createIncompleteItem(event, 'incomplete_" + key + "', '" + loc + "')";
		console.log('[textureTools] ' + line);
		if (Client.player) Client.player.tell("§a" + line);
	});
}

global.dumpIncompleteItemTextures = dumpIncompleteItemTextures;
