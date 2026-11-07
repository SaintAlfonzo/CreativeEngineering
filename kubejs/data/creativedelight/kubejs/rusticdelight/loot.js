// requires: creativedelight
// requires: rusticdelight
// Rustic Delight 1.7.0 chest loot (feature_writeup/rustic_delight.md 5.4). The upstream jungle temple, village and
// shipwreck seed swaps are not ported (RusticTranslator); the Pale and Dark seeds come only from these chests and the
// Wandering Trader (economy/wandering_trades.js), so the wild and giant pepper patches stay base colours.
// Very rare (owner 2026-11-02, 95th to 99th percentile): one seed, share 0.03. Marked for the loot overhaul (TODO loot).
//
// The rules sit in a callback, so nothing leaks into the top-level scope all server scripts share.

// Queued with global.CWG.lootRules (Creative Worldgen's loot engine, loot/05_tuning.js): its script pack can load
// after this one, so the rules run when the engine of the current reload exists. No Worldgen jar: no rules.
if (global.CWG) global.CWG.lootRules(function (L) {
    if (!global.CD || !global.CD.on('rusticdelight')) return
    var i = global.item

    // crates and bags of the new colours, like the three base crates (veggiesdelight/loot.js)
    ;['orange', 'white', 'pink', 'blue', 'purple', 'black'].forEach(function (c) {
        L.pack(i['bell_pepper_' + c + '_crate'], i['bell_pepper_' + c])
    })
    L.pack(i.pale_bell_pepper_seeds_bag, i.pale_bell_pepper_seeds)
    L.pack(i.dark_bell_pepper_seeds_bag, i.dark_bell_pepper_seeds)
    L.pack(i.roasted_coffee_beans_bag,   i.roasted_coffee_beans)
    L.pack(i.calamari_crate,             i.calamari)

    // Dark seeds: ancient city chests (the sculk chests and the ice box)
    ;['minecraft:chests/ancient_city', 'minecraft:chests/ancient_city_ice_box'].forEach(function (table) {
        L.add(table, i.dark_bell_pepper_seeds, { share: 0.03, count: 1 })
    })
    // Pale seeds: sky village house chests and the cloud balloon chest
    var SKY = []
    if (global.has('skyvillages')) {
        SKY = ['armorer', 'butcher', 'cartographer', 'fisher', 'fletcher', 'mason', 'normal_house', 'shepherd', 'temple',
            'toolsmith', 'weaponsmith'].map(function (h) { return 'skyvillages:chests/sky_village_' + h })
    }
    if (global.has('cloudstorage')) SKY.push('cloudstorage:chests/loot_balloon')
    SKY.forEach(function (table) {
        L.add(table, i.pale_bell_pepper_seeds, { share: 0.03, count: 1 })
    })
})
