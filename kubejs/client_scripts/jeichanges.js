// ============================================================
// JEI hide
// ============================================================
function hideRemovedItems(e) {
	global.removedItems.forEach(id => e.hide(id));
}

JEIEvents.hideItems(e => {
	hideRemovedItems(e);
});

// ============================================================
// Tooltip flag
// ============================================================
function tagRemovedItems(e) {
	global.removedItems.forEach(id => {
		e.add(id, Text.red('REMOVED ITEM: DO NOT USE').bold());
	});
}

ItemEvents.tooltip(e => {
	tagRemovedItems(e);
});

// Unwanted enchanted books (the three Nock Enough Arrows enchantments): Creative Magic (assets/creativemagic/kubejs/jei.js).

console.log('Hello! The jei event has finished!')
