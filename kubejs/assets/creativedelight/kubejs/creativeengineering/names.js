// Delight dish, crop and fluid names and the lunchbox tier names. Moved from the pack's client_scripts/namechanges.js
// 2026-11-01 (creative_split.md 7.6, wave 4), rows unchanged; no pack or CD script names these keys, so running after
// the pack's lang pass gives the same names.
ClientEvents.lang('en_us', e => {
  if (!global.CD) return
  // Oceanic Delight's salmon eggs are Caviar, and its fish egg dishes are named after it (dishes/ocean #5, ocean_delight.md). Ancient
  // Fish Eggs keep their name. The JEI pages follow the fishing net's biome tables (ocean_delight.md 5.1, 5.2).
  e.renameItem(global.item.caviar, 'Caviar')
  e.renameItem(global.item.fish_egg_roll, 'Caviar Roll')
  e.renameItem(global.item.fish_egg_roll_slice, 'Caviar Roll Slice')
  e.renameItem(global.item.fish_egg_noodle_soup, 'Caviar Noodle Soup')
  e.add('jei.oceanic_delight.salmon_eggs_information',
    'Salmon roe.\n\nDrops from Salmon killed with a Knife, and from a Fishing Net used on kelp or seagrass in rivers.')
  e.add('jei.oceanic_delight.shrimp_information',
    'Found in kelp and seagrass in oceans that are not cold.\n\nUse a Fishing Net on them to catch some.')
  // Fruits Delight's bayberry soup is a drink (food/drinks #9): item, its Forge fluid and the can. Ids unchanged.
  e.renameItem(global.item.bayberry_soup, 'Bayberry Juice')
  e.add('fluid.fruitsdelight.bayberry_soup', 'Bayberry Juice')
  e.renameItem(global.item.canned_bayberry_soup, 'Canned Bayberry Juice')
  // Veggies Delight's potato noodle is Gnocchi and potato noodles are Creamy Gnocchi (dishes/meals #33). The two
  // sprites are still the noodle art (texture swap open in the TODO row).
  e.renameItem(global.item.potato_noodle, 'Gnocchi')
  e.renameItem(global.item.potato_noodles, 'Creamy Gnocchi')
  e.renameItem(global.item.canned_potato_noodles, 'Canned Creamy Gnocchi')
  // Vintage Delight's cucumber noodles take more vegetables (dishes/meals #43); sprite recoloured to the zucchini's
  // palette in kubejs/assets/vintagedelight/textures/item/cucumber_noodles.png
  e.renameItem(global.item.cucumber_noodles, 'Veggie Noodles')
  // Brewin's Pickled Pickles merged into the Pickled Sea Pickle (dishes/pickles-and-ferments #1): the can holds pickles
  e.renameItem(global.item.canned_pickled_pickles, 'Canned Pickles')

  // Ender's Delight additions: the member jar's lang file shares a path with the ARR jar's and does not render
  // (creativedelight/members #28); the KubeJS pack sits above both. Same names as the member's lang file.
  var ed = {
    'block.endersdelight.amberveil_mushroom': 'Amberveils',
    'block.endersdelight.ethereal_saffron_bush': 'Ethereal Saffron',
    'block.endersdelight.chorus_flame_block': 'Chorusflame',
    'block.endersdelight.voidpepper_block': 'Void Pepper Sprout',
    'item.endersdelight.crushed_voidpepper': 'Crushed Void Pepper',
    'item.endersdelight.amberveil_stew': 'Amberveil Stew',
    'item.endersdelight.amberveiled_curry': 'Amberveiled Curry',
    'item.endersdelight.chicken_curry': 'End Chicken Curry',
    'item.endersdelight.steak_fries': 'Steak with French Fries',
    'item.endersdelight.veil_of_flames_risotto': 'Veil of Flames Risotto',
    'item.endersdelight.voidpepper': 'Void Pepper'
  }
  Object.keys(ed).forEach(k => e.add('endersdelight', k, ed[k]))
  // Veggies Delight crop blocks have no lang key in the backport (ui/names #10)
  ;['broccoli', 'cauliflower', 'garlic', 'sweet_potato', 'turnip', 'zucchini'].forEach(c =>
    e.add('veggiesdelight', 'block.veggiesdelight.' + c + '_crop',
      c.split('_').map(w => w.charAt(0).toUpperCase() + w.slice(1)).join(' ') + ' Plant'))

  // Dish renames (scratchpad 2026-10-30). Ids unchanged.
  e.renameItem(global.item.cheesy_pasta, 'Cheesy Fish Pasta')  // Brewin's; merges/dishes #5
  e.renameItem(global.item.canned_cheesy_pasta, 'Canned Cheesy Fish Pasta')
  e.add('block.displaydelight.bnc_cheesy_pasta', 'Placed Cheesy Fish Pasta')
  e.renameItem(global.item.deluxe_burger, 'Loaded Burger')  // Vintage's "The Burger"; merges/dishes #10
  e.add('block.supplementaries.pancake', 'Pancake')  // owner 2026-11-01 (creativedelight/members #15): "Pancake", the tooltip says stackable (supplementaries/client.js); was Stackable Pancake (merges/baking #1)
  e.renameItem(global.item.jelly_bread, 'Bread with Jam')  // dishes/jams #3
  e.renameItem(global.item.coffee_cheesecake, 'Coffee Pie')  // creativedelight/members #25
  e.add('item.rusticdelight.coffee_cheesecake_slice', 'Slice of Coffee Pie')
  // The Dirty Stuff: the leaf package and the tobacco package both read "Tobacco Package" (food/drugs #2)
  e.add('item.the_dirty_stuff.tobacco_leafs_package', 'Bundle of Tobacco Leaves')
  e.add('item.the_dirty_stuff.dried_tobacco_leafs_package', 'Bundle of Dried Tobacco Leaves')
  e.add('item.the_dirty_stuff.dried_tobacco_leaf_package', 'Bundle of Dried Tobacco Leaves')
  e.add('item.the_dirty_stuff.tobacco_package', 'Tobacco Bundle')
  // Cantaloupe merge (item_merges.md 2.10): Fruits Delight's hamimelon line is the Cantaloupe, Delightful's cantaloupe
  // (sand crop) the Mini Cantaloupe. Ids unchanged.
  e.renameItem(global.item.hamimelon, 'Cantaloupe')
  e.renameItem(global.item.hamimelon_seeds, 'Cantaloupe Seeds')
  e.renameItem(global.item.hamimelon_jello, 'Cantaloupe Jello')
  e.renameItem(global.item.hamimelon_jelly, 'Cantaloupe Jam')
  e.renameItem(global.item.hamimelon_juice, 'Cantaloupe Juice')
  e.renameItem(global.item.hamimelon_shaved_ice, 'Cantaloupe Shaved Ice')
  e.renameItem(global.item.hamimelon_jello_block, 'Cantaloupe Jello Block')
  e.renameItem(global.item.hamimelon_jelly_block, 'Cantaloupe Jam Block')
  e.add('block.fruitsdelight.hamimelon_stem', 'Cantaloupe Stem')
  e.add('block.fruitsdelight.attached_hamimelon_stem', 'Attached Cantaloupe Stem')
  e.add('block.fruitsdelight.hamimelon_cauldron', 'Cantaloupe Cauldron')
  e.add('block.fruitsdelight.hamimelon_jello_cauldron', 'Cantaloupe Jello Cauldron')
  e.add('block.fruitsdelight.hamimelon_jelly_cauldron', 'Cantaloupe Jam Cauldron')
  e.add('fluid.fruitsdelight.hamimelon_jam', 'Cantaloupe Jam')
  e.add('fluid.fruitsdelight.hamimelon_jello', 'Melted Cantaloupe Jello')
  e.add('fluid.fruitsdelight.hamimelon_juice', 'Cantaloupe Juice')
  e.renameItem(global.item.cantaloupe, 'Mini Cantaloupe')
  e.renameItem(global.item.cantaloupe_seeds, 'Mini Cantaloupe Seeds')
  e.add('block.delightful.sliced_cantaloupe', 'Sliced Mini Cantaloupe')
  e.add('block.delightful.cantaloupe_plant', 'Mini Cantaloupe Plant')
  e.add('delightful.cantaloupe.desc', 'Found in deserts and badlands.')
  e.add('jei.delightful.info.cantaloupe_seeds', 'Mini Cantaloupe Seeds will sprout when planted in sand next to water.')

  // Lunchbox tiers follow their textures (feature_writeup/lunchboxes.md). The container.* keys are the screen titles.
  e.renameItem(global.item.copper_lunchbox, 'Copper Lunchbox')
  e.add('item.solapplepie.container.lunchbox', 'Copper Lunchbox')
  e.renameItem(global.item.iron_lunchbox, 'Iron Lunchbox')
  e.add('item.solapplepie.container.golden_lunchbox', 'Iron Lunchbox')
  e.add('item.solapplepie.netherite_lunchbox', 'Netherite Lunchbox')
  e.add('item.solapplepie.container.netherite_lunchbox', 'Netherite Lunchbox')
})
