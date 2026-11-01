// Fallback only: patcherMod's NEA mixins make the three enchantments non-tradeable (isTradeable), which is what keeps
// them out of Dynamic Villager Trades librarians and Lightman's Currency cashiers; this filter only sees vanilla's listing.
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
