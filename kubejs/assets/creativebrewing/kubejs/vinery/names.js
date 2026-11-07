// requires: creativebrewing
// requires: vinery
// Vinery names. Moved from the pack's client_scripts/namechanges.js 2026-11-01 (creative_brewing.md 4.5), rows
// unchanged; no other script names these keys, so running after the pack's lang pass gives the same names.
ClientEvents.lang('en_us', e => {
  if (!global.CB || !global.CB.on('vinery')) return
  e.add('entity.minecraft.villager.winemaker', 'Winemaker')
  // Vinery's wood is "Cherry", not "Dark Cherry" (alcohol.md 10.10; the port renames its own names). The Dramatic
  // Doors cherry door names went 2026-11-06: the port has no cherry wood set, so those doors are not registered.
  // Vinery's cider is alcoholic; Farmer's Delight's and Farmer's Respite's keep "Apple Cider"
  e.renameItem(global.item.apple_cider, 'Hard Cider')
  e.add('item.vinery.juice.tooltip.item.vinery.apple_juice', 'Used to craft Apple Wine and Hard Cider')
})
