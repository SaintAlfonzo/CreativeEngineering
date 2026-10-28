# Brief: ports / veggiesport

Written 2026-10-24 from [todo/TODO.md](../todo/TODO.md) `# ports`, Sub `veggiesport`. Only row 2 gained scratchpad
2026-10-24 scope here (folded text); the other rows have no brief yet. Layout per [README.md](README.md). Feature
writeup: [veggies_delight.md](../veggies_delight.md) (5.1 marked merges). The port is the in-tree mod
`development/backportedVeggies` (mod id `veggiesdelight`); merges go through `global.itemMerges`
([item_merges.js](../../kubejs/startup_scripts/_lib/item_merges.js), design in [item_merges.md](../item_merges.md)).

## veggiesport #2: Marked merges: Fish and Chips, Mashed Potatoes, Stuffed Bell Peppers
Status: Pinned / Work: KubeJS / TODO `merges/dishes #1 to #3` ([veggies_delight.md](../veggies_delight.md) 3.3, 5.1; no earlier section here)

Base work is in the writeup (both sides stay until the port has been played, then merge). Folded scope (scratchpad
2026-10-24): merge fish and cod and chips, i.e. the first marked merge (M1) is now asked for:
- Items: `veggiesdelight:fish_and_chips` (pot: eggs, raw fish, `#forge:vegetables/potato`, bowl; compat variant with
  More Delight `diced_potatoes`) and `pneumaticcraft:cod_n_chips` (`i.cod_n_chips`, renamed "Fish and Chips" in
  [namechanges.js](../../kubejs/client_scripts/namechanges.js) line 78; the pack's pot recipe is cooking.js lines
  369-372: oil, any raw fish, 2 `#forge:sliced_potato`, paper container). The "cod" wording in the note is the second
  item's id, not a third dish.
- Likely survivor: recommend the Pneumaticcraft id (`cod_n_chips`, already named Fish and Chips and already
  tuned by cooking.js) with VD's `fish_and_chips` purged to it via a `global.itemMerges` entry, or the
  reverse if the VD texture is preferred. Move the VD recipes' inputs over (`#forge:sliced_potato` covers
  `ingredients #20` sweet potato slices). Mashed Potatoes (M2) and Stuffed Bell Peppers (M3) stay pinned.
- The row is Pinned "until the port is in game": the jar is installed (`backportedVeggies-1.9.2-ce-19.2-02`); the merge
  waits on a play session.

### Tests
VD-41 After the merge: one Fish and Chips in JEI; the purged item in an old chest becomes the survivor.
