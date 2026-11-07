// requires: creativemagic
// Creative Magic: the client half of magic/enchants.js and the three Nock Enough Arrows enchantment mixins; moved from
// kubejs/client_scripts/jeichanges.js on 2026-11-04, rows unchanged. An enchanted book holding Stockpile, Recovery or
// Cycle (at any level, read from its NBT) gets the red removed-item line.
ItemEvents.tooltip(e => {
	var unwantedIds = ['nock_enough_arrows:stockpile', 'nock_enough_arrows:recovery', 'nock_enough_arrows:cycle']

	e.addAdvanced('minecraft:enchanted_book', (item, advanced, text) => {
		let stored = item.nbt ? item.nbt.StoredEnchantments : null;
		if (!stored) return;

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
