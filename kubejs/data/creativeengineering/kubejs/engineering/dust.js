// Creative Engineering; moved from kubejs/server_scripts/engineering/dust.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged.
ServerEvents.recipes(e => {
    console.log('Hello! The dust event has started!')

    var i = global.item
    var f = global.fluid
    var R = global.NCH

    // ############################## Amethyst ##############################
    // One shard recipe per machine: millstone and crushing wheels 0.75 dust, IE crusher 1 + 25 % (1.25).

    R.createMilling(Item.of(i.amethyst_dust, 1).withChance(0.75), i.amethyst_shard, e)
    R.createCrushing(Item.of(i.amethyst_dust, 1).withChance(0.75), i.amethyst_shard, e)
    R.ieCrushing(Item.of(i.amethyst_dust, 1), i.amethyst_shard, e, undefined, [{ chance: 0.25, output: Item.of(i.amethyst_dust, 1) }])

    // ############################## Diamond ##############################

    R.remove({id:'createaddition:crushing/diamond'}, e)
    R.createCrushing(Item.of(i.dust_diamond, 1).withChance(0.75), i.diamond, e)
    R.ieCrushing(Item.of(i.dust_diamond, 1), i.diamond, e, undefined, [{ chance: 0.25, output: Item.of(i.dust_diamond, 1) }])

    // ############################## Cinder Flour Dusts ##############################

    // Redstone <- Strength
    R.shapeless(Item.of(i.redstone, 8), [Item.of(i.cinder_flour, 8), { type: 'forge:partial_nbt', item: i.potion, nbt: '{Potion:"minecraft:strength"}' }], e, 'cinder_redstone_shapeless', null, [i.potion, Item.of(i.glass_bottle)])
    R.ieBottling(i.redstone,{ tag: f.t_potion, amount: 20, nbt: '{Bottle:"REGULAR",Potion:"minecraft:strength"}' },i.cinder_flour,e, 'redstone_from_cinder_flour_potion')
    R.createFilling(Item.of(i.redstone, 9), [Fluid.of(f.create_potion, 225, '{Bottle:"REGULAR",Potion:"minecraft:strength"}'), i.cinder_flour_pile], e, 'cinder_pile_redstone_filling')
    R.ieBottling(Item.of(i.redstone, 9),{ tag: f.t_potion, amount: 180, nbt: '{Bottle:"REGULAR",Potion:"minecraft:strength"}' },i.cinder_flour_pile,e, 'redstone_from_cinder_flour_pile_potion')

    // Glowstone <- Night Vision
    R.shapeless(Item.of(i.glowstone, 8), [Item.of(i.cinder_flour, 8), { type: 'forge:partial_nbt', item: i.potion, nbt: '{Potion:"minecraft:night_vision"}' }], e, 'cinder_glowstone_shapeless', null, [i.potion, Item.of(i.glass_bottle)])
    R.ieBottling(i.glowstone,{ tag: f.t_potion, amount: 20, nbt: '{Bottle:"REGULAR",Potion:"minecraft:night_vision"}' },i.cinder_flour,e, 'glowstone_from_cinder_flour_potion')
    R.createFilling(Item.of(i.glowstone, 9), [Fluid.of(f.create_potion, 225, '{Bottle:"REGULAR",Potion:"minecraft:night_vision"}'), i.cinder_flour_pile], e, 'cinder_pile_glowstone_filling')
    R.ieBottling(Item.of(i.glowstone, 9),{ tag: f.t_potion, amount: 180, nbt: '{Bottle:"REGULAR",Potion:"minecraft:night_vision"}' },i.cinder_flour_pile,e, 'glowstone_from_cinder_flour_pile_potion')

    // Gunpowder <- Harming
    R.shapeless(Item.of(i.gunpowder, 8), [Item.of(i.cinder_flour, 8), { type: 'forge:partial_nbt', item: i.potion, nbt: '{Potion:"minecraft:harming"}' }], e, 'cinder_gunpowder_shapeless', null, [i.potion, Item.of(i.glass_bottle)])
    R.ieBottling(i.gunpowder,{ tag: f.t_potion, amount: 20, nbt: '{Bottle:"REGULAR",Potion:"minecraft:harming"}' },i.cinder_flour,e, 'gunpowder_from_cinder_flour_potion')
    R.createFilling(Item.of(i.gunpowder, 9), [Fluid.of(f.create_potion, 225, '{Bottle:"REGULAR",Potion:"minecraft:harming"}'), i.cinder_flour_pile], e, 'cinder_pile_gunpowder_filling')
    R.ieBottling(Item.of(i.gunpowder, 9),{ tag: f.t_potion, amount: 180, nbt: '{Bottle:"REGULAR",Potion:"minecraft:harming"}' },i.cinder_flour_pile,e, 'gunpowder_from_cinder_flour_pile_potion')

    // Sulfur <- Long Fire Resistance
    R.shapeless(Item.of(i.dust_sulfur, 8), [Item.of(i.cinder_flour, 8), { type: 'forge:partial_nbt', item: i.potion, nbt: '{Potion:"minecraft:long_fire_resistance"}' }], e, 'cinder_sulfur_shapeless', null, [i.potion, Item.of(i.glass_bottle)])
    R.ieBottling(i.dust_sulfur,{ tag: f.t_potion, amount: 20, nbt: '{Bottle:"REGULAR",Potion:"minecraft:long_fire_resistance"}' },i.cinder_flour,e, 'sulfur_from_cinder_flour_potion')
    R.createFilling(i.dust_sulfur, [Fluid.of(f.create_potion, 25, '{Bottle:"REGULAR",Potion:"minecraft:long_fire_resistance"}'), i.cinder_flour], e, 'cinder_sulfur_filling')
    R.ieBottling(Item.of(i.dust_sulfur, 9),{ tag: f.t_potion, amount: 180, nbt: '{Bottle:"REGULAR",Potion:"minecraft:long_fire_resistance"}' },i.cinder_flour_pile,e, 'sulfur_from_cinder_flour_pile_potion')
    R.createFilling(Item.of(i.dust_sulfur, 9), [Fluid.of(f.create_potion, 225, '{Bottle:"REGULAR",Potion:"minecraft:long_fire_resistance"}'), i.cinder_flour_pile], e, 'cinder_pile_sulfur_filling')

    // Nitrite <- Long Invisibility
    R.shapeless(Item.of(i.dust_saltpeter, 8), [Item.of(i.cinder_flour, 8), { type: 'forge:partial_nbt', item: i.potion, nbt: '{Potion:"minecraft:long_invisibility"}' }], e, 'cinder_saltpeter_shapeless', null, [i.potion, Item.of(i.glass_bottle)])
    R.ieBottling(i.dust_saltpeter,{ tag: f.t_potion, amount: 20, nbt: '{Bottle:"REGULAR",Potion:"minecraft:long_invisibility"}' },i.cinder_flour,e, 'saltpeter_from_cinder_flour_potion')
    R.createFilling(i.dust_saltpeter, [Fluid.of(f.create_potion, 25, '{Bottle:"REGULAR",Potion:"minecraft:long_invisibility"}'), i.cinder_flour], e, 'cinder_saltpeter_filling')
    R.ieBottling(Item.of(i.dust_saltpeter, 9),{ tag: f.t_potion, amount: 180, nbt: '{Bottle:"REGULAR",Potion:"minecraft:long_invisibility"}' },i.cinder_flour_pile,e, 'saltpeter_from_cinder_flour_pile_potion')
    R.createFilling(Item.of(i.dust_saltpeter, 9), [Fluid.of(f.create_potion, 225, '{Bottle:"REGULAR",Potion:"minecraft:long_invisibility"}'), i.cinder_flour_pile], e, 'cinder_pile_saltpeter_filling')

    // Coke dust <- Strong Slowness
    R.shapeless(Item.of(i.dust_coke, 8), [Item.of(i.cinder_flour, 8), { type: 'forge:partial_nbt', item: i.potion, nbt: '{Potion:"minecraft:strong_slowness"}' }], e, 'cinder_coke_dust_shapeless', null, [i.potion, Item.of(i.glass_bottle)])
    R.ieBottling(i.dust_coke,{ tag: f.t_potion, amount: 20, nbt: '{Bottle:"REGULAR",Potion:"minecraft:strong_slowness"}' },i.cinder_flour,e, 'coke_dust_from_cinder_flour_potion')
    R.createFilling(i.dust_coke, [Fluid.of(f.create_potion, 25, '{Bottle:"REGULAR",Potion:"minecraft:strong_slowness"}'), i.cinder_flour], e, 'cinder_coke_dust_filling')
    R.ieBottling(Item.of(i.dust_coke, 9),{ tag: f.t_potion, amount: 180, nbt: '{Bottle:"REGULAR",Potion:"minecraft:strong_slowness"}' },i.cinder_flour_pile,e, 'coke_dust_from_cinder_flour_pile_potion')
    R.createFilling(Item.of(i.dust_coke, 9), [Fluid.of(f.create_potion, 225, '{Bottle:"REGULAR",Potion:"minecraft:strong_slowness"}'), i.cinder_flour_pile], e, 'cinder_pile_coke_dust_filling')

    // Cocoa powder <- Weakness
    R.shapeless(Item.of(i.cocoa_powder, 8), [Item.of(i.cinder_flour, 8), { type: 'forge:partial_nbt', item: i.potion, nbt: '{Potion:"minecraft:weakness"}' }], e, 'cinder_cocoa_powder_shapeless', null, [i.potion, Item.of(i.glass_bottle)])
    R.ieBottling(i.cocoa_powder,{ tag: f.t_potion, amount: 20, nbt: '{Bottle:"REGULAR",Potion:"minecraft:weakness"}' },i.cinder_flour,e, 'cocoa_powder_from_cinder_flour_potion')
    R.createFilling(i.cocoa_powder, [Fluid.of(f.create_potion, 25, '{Bottle:"REGULAR",Potion:"minecraft:weakness"}'), i.cinder_flour], e, 'cinder_cocoa_powder_filling')
    R.ieBottling(Item.of(i.cocoa_powder, 9),{ tag: f.t_potion, amount: 180, nbt: '{Bottle:"REGULAR",Potion:"minecraft:weakness"}' },i.cinder_flour_pile,e, 'cocoa_powder_from_cinder_flour_pile_potion')
    R.createFilling(Item.of(i.cocoa_powder, 9), [Fluid.of(f.create_potion, 225, '{Bottle:"REGULAR",Potion:"minecraft:weakness"}'), i.cinder_flour_pile], e, 'cinder_pile_cocoa_powder_filling')

    // Matcha <- Slow Falling
    R.shapeless(Item.of(i.matcha, 8), [Item.of(i.cinder_flour, 8), { type: 'forge:partial_nbt', item: i.potion, nbt: '{Potion:"minecraft:slow_falling"}' }], e, 'cinder_matcha_shapeless', null, [i.potion, Item.of(i.glass_bottle)])
    R.ieBottling(i.matcha,{ tag: f.t_potion, amount: 20, nbt: '{Bottle:"REGULAR",Potion:"minecraft:slow_falling"}' },i.cinder_flour,e, 'matcha_from_cinder_flour_potion')
    R.createFilling(i.matcha, [Fluid.of(f.create_potion, 25, '{Bottle:"REGULAR",Potion:"minecraft:slow_falling"}'), i.cinder_flour], e, 'cinder_matcha_filling')
    R.ieBottling(Item.of(i.matcha, 9),{ tag: f.t_potion, amount: 180, nbt: '{Bottle:"REGULAR",Potion:"minecraft:slow_falling"}' },i.cinder_flour_pile,e, 'matcha_from_cinder_flour_pile_potion')
    R.createFilling(Item.of(i.matcha, 9), [Fluid.of(f.create_potion, 225, '{Bottle:"REGULAR",Potion:"minecraft:slow_falling"}'), i.cinder_flour_pile], e, 'cinder_pile_matcha_filling')

    // Ash <- Healing
    R.shapeless(Item.of(i.ash, 32), [Item.of(i.cinder_flour, 8), { type: 'forge:partial_nbt', item: i.potion, nbt: '{Potion:"minecraft:healing"}' }], e, 'cinder_ash_shapeless', null, [i.potion, Item.of(i.glass_bottle)])
    R.ieBottling(Item.of(i.ash, 4),{ tag: f.t_potion, amount: 20, nbt: '{Bottle:"REGULAR",Potion:"minecraft:healing"}' },i.cinder_flour,e, 'ash_from_cinder_flour_potion')
    R.createFilling(Item.of(i.ash, 4), [Fluid.of(f.create_potion, 25, '{Bottle:"REGULAR",Potion:"minecraft:healing"}'), i.cinder_flour], e, 'cinder_ash_filling')
    R.ieBottling(Item.of(i.ash, 36),{ tag: f.t_potion, amount: 180, nbt: '{Bottle:"REGULAR",Potion:"minecraft:healing"}' },i.cinder_flour_pile,e, 'ash_from_cinder_flour_pile_potion')
    R.createFilling(Item.of(i.ash, 36), [Fluid.of(f.create_potion, 225, '{Bottle:"REGULAR",Potion:"minecraft:healing"}'), i.cinder_flour_pile], e, 'cinder_pile_ash_filling')

    // Powdered obsidian <- Resistance
    R.shapeless(Item.of(i.powdered_obsidian, 8), [Item.of(i.cinder_flour, 8), { type: 'forge:partial_nbt', item: i.potion, nbt: '{Potion:"creativebrewing:resistance"}' }], e, 'cinder_powdered_obsidian_shapeless', null, [i.potion, Item.of(i.glass_bottle)])
    R.ieBottling(i.powdered_obsidian,{ tag: f.t_potion, amount: 20, nbt: '{Bottle:"REGULAR",Potion:"creativebrewing:resistance"}' },i.cinder_flour,e, 'powdered_obsidian_from_cinder_flour_potion')
    R.createFilling(i.powdered_obsidian, [Fluid.of(f.create_potion, 25, '{Bottle:"REGULAR",Potion:"creativebrewing:resistance"}'), i.cinder_flour], e, 'cinder_powdered_obsidian_filling')
    R.ieBottling(Item.of(i.powdered_obsidian, 9),{ tag: f.t_potion, amount: 180, nbt: '{Bottle:"REGULAR",Potion:"creativebrewing:resistance"}' },i.cinder_flour_pile,e, 'powdered_obsidian_from_cinder_flour_pile_potion')
    R.createFilling(Item.of(i.powdered_obsidian, 9), [Fluid.of(f.create_potion, 225, '{Bottle:"REGULAR",Potion:"creativebrewing:resistance"}'), i.cinder_flour_pile], e, 'cinder_pile_powdered_obsidian_filling')

    // ############################## Sulfur ##############################

    // Remove sulfur drop from scoria
    R.remove({id: 'vintageimprovements:crushing/scoria'}, e)
    R.remove({id: 'vintageimprovements:crushing/scoria_recycling'}, e)
    // Crush blaze powder to sulfur
    R.createCrushing(Item.of(i.dust_sulfur, 1).withChance(0.75), i.blaze_powder, e)
    //Make TNT require sulfur, nether
    R.remove({id: 'minecraft:tnt'}, e)
    R.shaped(Item.of(i.tnt, 4), ['ABA','BAB', 'ABA'], {A: i.gunpowder, B: i.dust_sulfur}, e)

    // Glowstone from amethyst shards was removed (recipes/engineering #2); glowstone comes from cinder flour above.

    // ############################## Obsidian ##############################

    R.createMixing(Item.of(i.layered_obsidian, 1), [i.powdered_obsidian_block, Fluid.of(f.lava, 2000)], e, "layered_obsidian_mixing", "superheated")
    R.createSaw(Item.of(i.sturdy_plate, 12), i.layered_obsidian, e)

    console.log('The dust event has finished!')
})
