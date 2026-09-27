MoreJSEvents.filterEnchantedBookTrade(e => {
	e.remove('nock_enough_arrows:stockpile');
	e.remove('nock_enough_arrows:recovery');
	e.remove('nock_enough_arrows:cycle');
});

MoreJSEvents.filterAvailableEnchantments(e => {
	e.remove('nock_enough_arrows:stockpile');
	e.remove('nock_enough_arrows:recovery');
	e.remove('nock_enough_arrows:cycle');
});
