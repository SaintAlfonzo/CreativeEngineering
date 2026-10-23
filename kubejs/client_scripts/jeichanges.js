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

// ============================================================
// Unwanted enchanted books
// ============================================================
const unwantedEnchants = {
	'nock_enough_arrows:stockpile': 4,
	'nock_enough_arrows:recovery': 3,
	'nock_enough_arrows:cycle': 1,
};

// ============================================================
// Tooltip flag (any level, checked at runtime via NBT)
// ============================================================
ItemEvents.tooltip(e => {
	e.addAdvanced('minecraft:enchanted_book', (item, advanced, text) => {
		let stored = item.nbt ? item.nbt.StoredEnchantments : null;
		if (!stored) return;

		let unwantedIds = Object.keys(unwantedEnchants);
		let hasUnwanted = false;

		for (let i = 0; i < stored.length; i++) {
			if (unwantedIds.includes(stored[i].id)) {
				hasUnwanted = true;
				break;
			}
		}

		if (hasUnwanted) {
			text.add(Text.red('REMOVED ITEM: DO NOT USE').bold());
		}
	});
});

console.log('Hello! The jei event has finished!')
