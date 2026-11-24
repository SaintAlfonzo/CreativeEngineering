// requires: brewinandchewin
// SPDX-License-Identifier: GPL-3.0-only
// Startup script in the CB jar; CB rebuild + restart. Brewin' and Chewin' items removed from the pack while the port
// still registers them: hidden in JEI and stripped of their tags through Creative Common's removed items. Their ids are
// in data/creativebrewing/swaps/removed.json.
// Kippers (creative-brewing #1): the fermenting recipe and the origins meat tag entry are deleted in the Brewin' member.

global.removedItems.push(global.item.kippers)
