// Precision Sawblade profiles.
//
// Each recipe here turns a plain IE sawblade + a "shape template" + steel into a
// creativeengineering:precision_sawblade carrying {Profile:"<shape>"} NBT. Installed
// in the IE Sawmill, that blade makes the machine pick the sawmill recipe whose
// "blade" field matches the profile (see patcherMod SawbladeRouting / the generated
// data/creativeengineering/recipes/sawmill/*.json).
//
// Crafted in the Engineer's Workbench under the "precision_sawblades" blueprint
// category (blueprint item recipe + category name live in blueprints.js / namechanges.js).
//
// Not built through R.ieBlueprint: that helper (NCH._itemJson) can't put NBT on the
// result, so these go through e.custom() with a hand-written immersiveengineering:blueprint
// JSON. IE's result reader honours "nbt" whenever the result is given by "item".

ServerEvents.recipes(e => {

  const BLADE = 'immersiveengineering:sawblade'
  const OUT = 'creativeengineering:precision_sawblade'
  const CATEGORY = 'precision_sawblades'

  // profile -> shape-template ingredient (IngredientWithSize JSON)
  const PROFILES = {
    stairs:         { tag: 'minecraft:wooden_stairs' },
    slab:           { tag: 'minecraft:wooden_slabs' },
    door:           { tag: 'minecraft:wooden_doors' },
    trapdoor:       { tag: 'minecraft:wooden_trapdoors' },
    fence:          { tag: 'minecraft:wooden_fences' },
    fence_gate:     { tag: 'minecraft:fence_gates' },
    button:         { tag: 'minecraft:wooden_buttons' },
    pressure_plate: { tag: 'minecraft:wooden_pressure_plates' },
    sign:           { tag: 'minecraft:signs' },
    vertical_slab:  { tag: 'quark:wooden_vertical_slabs' },
    vertical_planks:{ item: 'quark:vertical_oak_planks' },
    ladder:         { tag: 'quark:ladders' },
    railing:        { item: 'architects_palette:oak_railing' },
  }

  Object.keys(PROFILES).forEach(profile => {
    e.custom({
      type: 'immersiveengineering:blueprint',
      category: CATEGORY,
      inputs: [
        { item: BLADE },
        PROFILES[profile],
        { base_ingredient: { tag: 'forge:plates/steel' }, count: 2 },
      ],
      result: { item: OUT, nbt: { Profile: profile } },
    }).id(`creativeengineering:blueprint/${CATEGORY}/${profile}`)
  })
})
