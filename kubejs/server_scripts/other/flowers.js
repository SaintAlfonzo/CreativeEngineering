ServerEvents.recipes(e => {
    console.log('flowers starting')

    const R = global.NCH
    const i = global.item

    // Bushier Flowers no longer grow from bone meal or generate (patcherMod BushierFlowersDisableMixin
    // + forge:none biome modifiers in kubejs/data/bushierflowers); 4 loose flowers craft one bush instead.
    R.bulk('shapeless', [
        [i.grown_allium,       i.allium],
        [i.grown_azure_bluet,  i.azure_bluet],
        [i.grown_blue_orchid,  i.blue_orchid],
        [i.grown_cornflower,   i.cornflower],
        [i.grown_dandelion,    i.dandelion],
        [i.grown_lily,         i.lily_of_the_valley],
        [i.grown_orange_tulip, i.orange_tulip],
        [i.grown_oxeye_daisy,  i.oxeye_daisy],
        [i.grown_pink_tulip,   i.pink_tulip],
        [i.grown_poppy,        i.poppy],
        [i.grown_red_tulip,    i.red_tulip],
        [i.grown_white_tulip,  i.white_tulip],
        [i.grown_wither_rose,  i.wither_rose]
    ].map(p => [Item.of(p[0], 1), [p[1], p[1], p[1], p[1]], e, p[0].split(':')[1]]))

    console.log('flowers done')
})
