// requires: corn_delight
// Corn Delight 1.2.11's three new items on the installed 1.0.3 (feature_writeup/corn_delight.md 2). Creative Delight
// startup script, loaded from the jar after the pack's startup_scripts, so global.item exists here. Restart to apply.
// Art: kubejs/assets/creativeengineering/textures/item (MIT, from the 1.2.11 jar; notice beside it). Recipes, tags,
// loot and trades: data/creativedelight/kubejs/corn_delight/.

StartupEvents.registry('item', e => {
    var i = global.item

    // Corncob (2.1): the most kernels per item on the cutting board, a 10% side output of corn milling and crushing,
    // 25% from mature corn broken with a knife
    e.create(i.corncob).displayName('Corncob')

    // Corn Popsicle (2.2), frozen/popsicle family: upstream's 4 hunger, 0.2 modifier and quick eating (12 ticks);
    // the stick comes back, as Fruits Delight's popsicles
    e.create(i.corn_popsicle).displayName('Corn Popsicle').food(f => {
        f.hunger(4).saturation(0.2).fastToEat(true)
        f.eaten(ev => {
            var p = ev.player
            if (p && !p.isCreative()) p.give('minecraft:stick')
        })
    })

    // Cob Pipe (2.3): cosmetic, no stats. Head slot and Curios: patcherMod compat/CobPipe; 3D model through Forge's
    // separate_transforms (models/item/cob_pipe.json), the flat sprite in the GUI, on the ground and in frames
    e.create(i.cob_pipe).displayName('Cob Pipe').maxStackSize(1)
})
