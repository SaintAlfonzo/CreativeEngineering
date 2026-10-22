// Baking ingredients of the baking overhaul (feature_writeup/oven.md 2.6): frosting tops every cake, cookie dough is
// the base of every cookie. Plain items, not food. Placeholder textures until they get their own: Rustic Delight's
// cotton boll (frosting), Create Confectionery's gingerdough (cookie dough). Recipes: server_scripts/cooking/baking.js.

StartupEvents.registry('item', e => {
    var i = global.item
    e.create(i.frosting).displayName('Frosting').texture('rusticdelight:item/cotton_boll')
    e.create(i.cookie_dough).displayName('Cookie Dough').texture('create_confectionery:items/gingerdough')
})
