// Pack drink and cooking fluids. Like Central Kitchen's drink fluids they are "virtual": no placed block and no bucket, they live
// in tanks, basins and pipes only. Ids in _lib/fluid_dict.js ("Pack fluids"), forge:fluid/<name> tags in
// server_scripts/tags/fluidtags.js, recipes in server_scripts/cooking/cooking.js ("Fluids"). JEI lists every
// registered fluid (client_scripts/fluid_jei.js) and shows the bottle's effects on the fluid through its Create
// emptying recipe (patcherMod DrinkFluidEffects).

StartupEvents.registry('fluid', e => {
    var f = global.fluid
    // Ender's Delight's chorus juice (TODO food/drinks #4)
    e.create(f.chorus_juice).displayName('Chorus Juice').thinTexture(0xB283C9).bucketColor(0xB283C9).noBucket().noBlock()
    // Veggies Delight's juices (feature_writeup/veggies_delight.md 4.9)
    e.create(f.carrot_juice).displayName('Carrot Juice').thinTexture(0xF08A24).bucketColor(0xF08A24).noBucket().noBlock()
    e.create(f.dandelion_juice).displayName('Dandelion Juice').thinTexture(0xEFCF3A).bucketColor(0xEFCF3A).noBucket().noBlock()
    e.create(f.turnip_water).displayName('Turnip Water').thinTexture(0xE3D5E6).bucketColor(0xE3D5E6).noBucket().noBlock()
    // Cooking fluids (TODO dishes/ingredients #2): egg solution stands in for egg + milk, frosting for the frosting item
    e.create(f.egg_solution).displayName('Egg Solution').thinTexture(0xF3D97A).bucketColor(0xF3D97A).noBucket().noBlock()
    e.create(f.frosting).displayName('Frosting').thinTexture(0xFFF4F0).bucketColor(0xFFF4F0).noBucket().noBlock()
})
