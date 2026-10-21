# Storage audit: wine bottles, racks, crates and bags (compressed)

2026-10-19. Read from the port source (`development/backportedVinery`, `-02`; updated for `-03`: alcohol.md 10.10 to
10.12), upstream 1.5.5 data, the 2026-10-18 item and recipe dumps (`kubejs/exported/items_by_mod`, `recipes/all.csv`)
and pack scripts. Nothing run in game.
TODO rows: `alcohol/decisions #6, #7`, `alcohol/vineryport #10, #11, #12`.

## 1. Rules (how the port decides)

Updated 2026-10-19 for `-03` ([alcohol.md](alcohol.md) 10.11, 10.12). The audit below this line used to describe `-02`,
where tags, a SMALL / BIG enum and the per-block count disagreed; that's fixed.

- **One source: the per-block count.** Each wine's `new WineBottleBlock(getWineSettings(), N)` in `ObjectRegistry`.
  3 = small, 2 = medium, 1 = large (`DrinkBlockItem.BottleSize`, derived, never set per item).
- **Rack fit = size**, checked in code: each container names the largest size it takes.
- **Tags follow the size.** `#vinery:small_bottle`, `#vinery:medium_bottle` (new), `#vinery:large_bottle` are rebuilt
  by `UpstreamTranslator.patchData` from the sizes; upstream's lists are replaced. The port doesn't read them.
- **Draw size = the bottle's own model.** A bottle lying in a 4x / 9x rack or the wine box is shrunk to the 15 px slot
  length, measured from its baked model (`BottleRenderer.fitToSlot`). Width isn't fitted.

## 2. Containers

| Block | Ids | Slots | Largest size | Takes |
|---|---|---|---|---|
| Big Wine Rack (9x) | `<wood>_wine_rack_big`, 9 woods | 9 | small | small |
| Small Wine Rack (4x) | `<wood>_wine_rack_small` | 4 | medium | small, medium |
| Wine Box | `wine_box` | 1 | medium | small, medium |
| Wine Rack (single bottle) | `<wood>_wine_rack_mid` | 1 | large | all |

Woods: oak, birch, spruce, dark oak, jungle, acacia, mangrove, bamboo, cherry (the port's dark cherry, ids kept as
`cherry_*` by D1, named "Cherry" since 10.10). The legacy 1.3.10 racks (`wine_rack_1`, `_2`) and cabinet / drawer
(`wine_rack_3`, `_5`) were removed in 10.11; the 1.5.5 cherry racks are the merged ones. The cherry single-bottle rack
uses the 1.3.10 cherry glass door (10.11; upstream's dark cherry door is solid).

## 3. Wines

Size comes from "per block"; the fit columns follow it. "Shrunk" = longer than the 15 px slot, drawn shorter lying
down (model heights from the 1.5.5 / 1.3.10 models). "Wide" = wider than a small bottle's 4 px. "Slim" = drawn as a
4 x 15 px bottle in its own colours in the 4x rack and the wine box ([alcohol.md](alcohol.md) 10.14, 2026-10-19).

| Wine | Id | Per block | Size | 9x | 4x / box | Single | Lying in a rack |
|---|---|---|---|---|---|---|---|
| Cherry | `cherry_wine` | 3 | small | yes | yes | yes | full size |
| Red | `red_wine` | 3 | small | yes | yes | yes | full size |
| Noir | `noir_wine` | 3 | small | yes | yes | yes | full size |
| Bolvar | `bolvar_wine` | 3 | small | yes | yes | yes | full size |
| Solaris | `solaris_wine` | 3 | small | yes | yes | yes | full size |
| Clark | `clark_wine` | 3 | small | yes | yes | yes | full size |
| Kelp Cider | `kelp_cider` | 3 | small | yes | yes | yes | full size |
| Mojang Noir | `bottle_mojang_noir` | 3 | small | yes | yes | yes | full size |
| Villagers Fright | `villagers_fright` | 3 | small | yes | yes | yes | full size |
| Apple Wine | `apple_wine` | 3 | small | yes | yes | yes | full size, wide (6 px) |
| Praetorian | `praetorian_wine` | 2 | medium | no | yes | yes | own model, shrunk 18 -> 15 px, wide (6 px) (your call: no slim) |
| Stal | `stal_wine` | 2 | medium | no | yes | yes | slim (own model 18 px, 8 px wide) |
| Sun-kissed | `glowing_wine` | 2 | medium | no | yes | yes | slim (own model 16 px, 8 px wide) |
| Magnetic | `magnetic_wine` | 2 | medium | no | yes | yes | slim; in the wine box Strad's shape in Magnetic's colours |
| Eiswein | `eiswein` | 2 | medium | no | yes | yes | slim (own model full size, wide (7 px)) |
| Chenet | `chenet_wine` | 2 | medium | no | yes | yes | slim (own model full size, wide (8 px)) |
| Aegis | `aegis_wine` | 2 | medium | no | yes | yes | slim (own model shrunk 16 -> 15 px, wide (8 px)) |
| Strad | `strad_wine` | 2 | medium | no | yes | yes | slim (own model shrunk 16 -> 15 px, wide (8 px)) |
| Mellohi | `mellohi_wine` | 2 | medium | no | yes | yes | slim (own model full size, wide (8 px)) |
| Cider | `apple_cider` | 2 | medium | no | yes | yes | slim (own model shrunk 16 -> 15 px, wide (7 px)) |
| Creepers Crush | `creepers_crush` | 2 | medium | no | yes | yes | slim (own model full size, wide (8 px)) |
| Jellie | `jellie_wine` | 1 | large | no | no | yes | (racks refuse it) |
| Cristel | `cristel_wine` | 1 | large | no | no | yes | (racks refuse it) |
| Lilitu | `lilitu_wine` | 1 | large | no | no | yes | (racks refuse it) |
| Jo's Special Mixture | `jo_special_mixture` | 1 | large | no | no | yes | (racks refuse it) |
| Chorus | `chorus_wine` | 1 | large | no | no | yes | (racks refuse it) |

Model sizes are read from the jars, not seen in game. The shrink is measured at runtime from the baked model, so a
model change (or a resource pack) changes it without code. The slim models and textures are built in memory at load
from the wines' own textures (`SlimBottles`, the palette swap tool's mapping), so a size change needs no art step. A
"own model" wine still draws its model when placed and in the single-bottle rack.

### Findings (status 2026-10-19)

- **B1 Sun-kissed clipped in the cherry 4x / 9x.** FIXED: those were the legacy racks (removed, 10.11); sun-kissed is
  small now and is shrunk to the slot length.
- **B2 Cherry 4x held stal, dark oak didn't.** FIXED: one set of racks, every wood checks size the same way.
- **B3 Stal was inconsistent** (large tag, SMALL enum, 3 per block). FIXED: small, from its count; medium (2) since
  10.14, your call.
- **B4 Sun-kissed was inconsistent** (large / BIG, 3 per block). FIXED: small, from its count; medium (2) since 10.14.
- **B5 Legacy racks bypassed everything.** GONE with the legacy racks; juices are refused everywhere (not wines).
- **B6 Three sizes from the per-block count.** BUILT (10.12).
- **B7 Width isn't fitted.** BUILT (10.14, untested): every medium but Praetorian lies slim in the 4x rack and the
  wine box; Stal and Sun-kissed left the 9x rack. Left wide: Apple Wine (6 px, small) in the 4x / 9x racks, and
  Praetorian (6 px) in the 4x rack.

### Remaining (decision `alcohol/decisions #6`)

- **Model tweaks:** DONE for the mediums (10.14). Apple Wine is the only wide small left; add it to the slim set if it
  clips in game. The single-bottle rack stands bottles upright and needs nothing.
- **To change a wine's size:** edit its per-block number in `ObjectRegistry` and rebuild (VB-75).

## 4. Crates and bags

Food 9-blocks only (egg crates, sleeping bags, dyed sacks, IE crates and planter boxes left out). "Pack" = 9 -> 1,
"unpack" = 1 -> 9; "-" = no recipe of that id in the dump. Material: crate = wood, bag / sack = wool.

| Contents | Blocks | Pack / unpack | Notes |
|---|---|---|---|
| Apple | `vinery:apple_bag`, `fruitsdelight:apple_crate`, `quark:apple_crate` | Vinery: pack only; FrD: both, **same 9-apple pattern as the bag**; Quark: removed by pack (`cooking.js` 369) | 2 live recipes on one pattern; the Vinery bag can't be unpacked. `vinery:apple_crate` removed (10.10) |
| Cherry | `vinery:cherry_bag` | pack only | `vinery:cherry_crate` removed (10.10) |
| Red / white grape | `vinery:{red,white}_grape_bag` | pack only | `vinery:{red,white}_grape_crate` removed (10.10) |
| Biome grapes (6) | `vinery:{red,white}_{savanna,taiga,jungle}_grape_bag` | pack only | no duplicates; no unpack |
| Carrot / potato / beetroot | `farmersdelight:*_crate`, `quark:*_crate` | FD both; Quark removed by pack | Quark blocks still registered (loot / creative tab) |
| Cabbage / onion / tomato | `farmersdelight:*_crate` | both | no duplicates |
| Sweet berries | `quark:berry_sack`, `berry_good:sweet_berry_basket`, `fantasyfurniture:decorations/berry_basket_sweetberry` | Quark both; Berry Good -; FF decor | 2 storage + 1 decor |
| Glow berries | `quark:glowberry_sack`, `berry_good:glow_berry_basket` | Quark both; Berry Good - | duplicate |
| Blueberry | `fruitsdelight:blueberry_crate`, `delightful:blueberry_sack`, FF `berry_basket_blueberry` | FrD both; Delightful - | duplicate + decor |
| Strawberry | `neapolitan:strawberry_basket`, `white_strawberry_basket`, FF `berry_basket_strawberry` | Neapolitan pack only | decor duplicate |
| Coffee beans | `farmersrespite:coffee_beans_sack`, `rusticdelight:coffee_beans_bag` | FR pack only; RD both | duplicate (merge with the coffee merge, `food/drinks #3`) |
| Tea leaves (3) | `farmersrespite:*_tea_leaves_sack` | pack only | no unpack |
| Mint | `neapolitan:mint_basket` | pack only | no unpack |
| Corn / kernels | `corn_delight:corn_crate`, `corn_kernel_bag` | both | none |
| Rice | `farmersdelight:rice_bag` | both | none |
| Others, single | FrD 15 fruit crates, Neapolitan adzuki / roasted adzuki / banana crates, Rustic Delight 3 pepper crates + seed bags + cotton, Quark cocoa / nether wart / gunpowder sacks + golden apple / carrot crates, Delightful acorn / crimson berry / nightshade / salmonberry sacks + baobab / green apple / joshua crates, Ender's Delight chorus, Quark Delight ancient fruit, B&C `ice_crate` | mostly both; Delightful sacks / crates - | no duplicates |

### Findings

- **C1 One recipe pattern makes 2-3 blocks.** Was apple 3, cherry / red grape / white grape 2 each. Since 10.10 (the
  Vinery crates removed, the 1.5.5 bags kept) only apple is left: the Vinery bag and the FrD crate.
- **C2 Vinery storage is one-way.** No bag unpacks, in 1.5.5 or 1.3.10. That makes them a trap: the loot rules pack
  apples, cherries and red / white grapes into `vinery:*_bag` (`loot/07_rules.js` 69-72, retargeted from the crates in
  10.10), and the player can never get the fruit back. Still open.
- **C3 No unpack recipe in the dump** for Farmer's Respite sacks, Neapolitan baskets, Berry Good baskets, Delightful
  sacks and crates. Some may not be storage at all (decor or pick-up-only); check each jar before merging.
- **C4 Quark's apple / carrot / potato / beetroot crates** have no recipes but still exist (creative tab, JEI, any loot).
  Hide them, or remap them to the survivor.
- **C5 Duplicates to merge:** apple (3 since 10.10), sweet berries (2 + decor),
  glow berries (2), blueberry (2 + decor), coffee beans (2), carrots / potatoes / beetroot (Quark leftovers).

### Proposed merge (decision `alcohol/decisions #7`; Vinery part decided 2026-10-19: bags kept, crates removed, 10.10)

- **One storage block per contents, both ways, through NCH** (`compress` / `decompress` style helpers, ids from
  `item_dict.js`).
- **Survivor rule:**
  - Fruit and vegetables keep a wooden **crate** (FD / FrD style). Berries, beans, leaves and seeds keep a cloth **sack** or **bag**.
  - Decor baskets (Fantasy Furniture) stay as decor with no recipe change.
- **Suggested survivors:**
  - apple `fruitsdelight:apple_crate` or `vinery:apple_bag` (open)
  - cherry `vinery:cherry_bag` (decided, 10.10)
  - grapes: `vinery:*_grape_bag` (decided, 10.10)
  - sweet / glow berries `quark:*_sack`
  - blueberry `fruitsdelight:blueberry_crate`
  - coffee `rusticdelight:coffee_beans_bag` (or follow the coffee merge)
- **Losers:**
  - Recipes removed and items hidden from JEI.
  - The loot `pack` lines retarget to the survivor.
  - Placed blocks stay (removing a registered block would need a port remap: allowed only inside the port's own ids,
    per memory `ports-no-conversions`).
- **Vinery:** DONE 10.10: the 4 crates were unregistered with no remap (your rule); the bags stay.

## 5. Next steps

1. Bottles: built, slim models included (10.14). Test with VB-59, VB-67, VB-71 to VB-75, VB-81 to VB-83.
2. Crates: decide the apple survivor (FrD crate or Vinery bag); NCH pack / unpack for every survivor (the Vinery bags
   first, C2), hide the losers, check C3 in the jars.
3. Mushroom crates (scratchpad 2026-10-21): a crate for mushrooms, merged by the same survivor rule (TODO `blocks/new #7`).
4. Sacks as bundles (scratchpad 2026-10-21): sacks behave like bundles in the inventory (scroll to cycle items, take
   out a stack, place 1), and the GUI stays open during world interaction (TODO `gameplay/storage-and-item-loss #7`).
