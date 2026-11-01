# items / starter-kit: implementation brief

Written 2026-10-31 from [TODO](../todo/TODO.md) `# items`, Sub `starter-kit` (row 1 Planned, row 2 Testing). Layout per
[README.md](README.md). Feature writeup: [feature_porting.md](../feature_porting.md) 1 and 1.1 (Starter Kit port, test
prefix `FP-`; kit tests FP-03, FP-10; scratch rows SQ-SK-01, -02 in [scratch_queue.md](../testing/scratch_queue.md)).
New tests here are placeholders `FP-newN`, numbered when queued.

## Scope and order

| Step | Row | Work | Notes |
|---|---|---|---|
| 1 | `starter-kit #1` kit contents | KubeJS (+ patcherMod or KubeJS for consumable MCA tools) | Needs the "which books" answer first |
| - | `starter-kit #2` kit never given | Testing | Built, needs play (below) |

---

## starter-kit #1: Starter kit contents
Status **Planned** / Work KubeJS / TODO `items/starter-kit #1`

### Goal
A new player's kit: no starting books from other mods, but an animal dictionary and a food dictionary in the kit; MCA's
comb and needle and thread in the kit too, and made single-use.

### Now
- [starter_kit.js](../../kubejs/server_scripts/other/starter_kit.js) (92 lines): `STARTER_KITS.kits` (18-21), two kits
  picked at random: Default (leather boots, shield offhand, wooden sword, 9 bread) and Engineer (leather boots, copper
  shield offhand, copper pickaxe and axe, 8 bread). Specs are item_dict keys (`stack`, 65-69 looks up `global.item`).
- 10 ticks after the first join the inventory is copied to `keep` (71-75), cleared, the kit set, then `keep` is given
  back (81): this is how other mods' join books survive. The latest real join (singleplayer, `logs/latest.log`
  2026-10-31 17:19) logged "Default kit given ... (1 stacks given back)".
- Books handed out on first join (configs checked 2026-10-31): Alex's Mobs `giveBookOnStartup = true`
  (`config/alexsmobs.toml` 4, the Animal Dictionary). Botania's `spawnWithLexicon` is Garden of Glass only; Advanced
  Peripherals `givePlayerBookOnJoin = false`. Patchouli books with `give_on_first_join` inside mod jars are not scanned:
  the given-back count above (1) suggests the AM book is the only one, unverified.
- Ids (item_dict, verified 2026-10-31): `i.animal_dictionary` = `alexsmobs:animal_dictionary` (10143),
  `i.comb` = `mca:comb` (11797), `i.needle_and_thread` = `mca:needle_and_thread` (11815). The food dictionary is
  Spice of Life's Food Book, `solapplepie:food_book` (model in `solapplepie-1.19.2-2.2.0.jar`, shapeless recipe in
  `kubejs/exported/recipes/all.csv`): **no item_dict key yet**.
- MCA 7.5.20 (`forge/net/mca/item/NeedleAndThreadItem`, `CombItem`, javap 2026-10-31): right-click sends an
  `OpenGuiRequest` (self editor, or the villager editor on a villager); neither item has durability or shrinks.
  Recipes: needle = 3 iron + 2 string shaped (`data/mca/recipes/needle_and_thread.json`), comb shaped.

### Build
1. item_dict: add `i.food_book = 'solapplepie:food_book';` in the solapplepie block (12753-12757).
2. Kit contents in `STARTER_KITS.kits`: add `animal_dictionary`, `food_book`, `comb`, `needle_and_thread` to both kits
   (free slots 2-8 / 3-8), by key.
3. Remove other mods' join books: set `giveBookOnStartup = false` in `config/alexsmobs.toml` (the kit gives it now) and
   copy the file to `configs_edited/root_config/alexsmobs.toml`. Then, as a safety net for unknown Patchouli books, drop
   from `keep` any stack that is `#forge:books`-like or a `patchouli:guide_book` (`i.guide_book`); see Decisions.
4. Consumable comb and needle: a KubeJS `ItemEvents.rightClicked` on `i.comb` / `i.needle_and_thread` (and
   `ItemEvents.entityInteracted` for the villager path) that shrinks the held stack by 1 on the server after MCA opens
   its screen (`e.item.count--`, not cancelled). If KubeJS sees the event before MCA's `use` and the order matters,
   move it to a patcherMod mixin on `NeedleAndThreadItem#use` / `CombItem#use` RETURN (and `interactLivingEntity`).
5. Tooltips: one lang line each ("Single use") through the pack's tooltip route (`ui/tooltips`), so players know.
6. Writeup: feature_porting.md 1.1 (kit contents), the starter_kit.js header comment line 13.

### Ids and data
- Verified: `alexsmobs:animal_dictionary`, `mca:comb`, `mca:needle_and_thread`, `solapplepie:food_book`,
  `patchouli:guide_book`. Config key `giveBookOnStartup` (AM).

### Decisions
- **Which books are "starting books"** (TODO decision "which books"): recommend all books given on join (today only
  AM's dictionary, which moves into the kit). Ask the user whether any Patchouli guide (e.g. the pack's own) should stay.
- **Strip unknown books from `keep`:** recommend no filter (only the AM config change); a filter would also eat a book a
  returning player picked up before the 10-tick delay, and the given-back count shows only one book today.
- **Consumable when the editor is cancelled:** recommend consuming on open (simple, server-side); consuming only on a
  saved edit needs MCA's editor packet and a mixin.
- **Both kits or one:** recommend both (the books are informational, the MCA tools are social).

### Gotchas
- The kit is set into fixed slots: keep each kit within slots 0-8 so the hotbar order stays readable, and do not reuse a
  slot index (the second `setItem` overwrites the first).
- Players already marked (`collective.firstJoin.starterkit` or `tracking.json`) never see the new kit; that is intended.
- MCA's interaction screen is client-side: shrinking the stack on the client too would double-count; do it server-side
  only and let the inventory sync.

### Tests
- **FP-new1 New kit:** fresh player: kit has the Animal Dictionary, Food Book, comb and needle and thread; no second AM
  dictionary arrives; `[starter-kit] ... (0 stacks given back)` in the log.
- **FP-new2 Single use:** use the comb on yourself and the needle on a villager: each opens its editor and is gone after.

---

## Built, needs play

| Row | Change | Status | Note |
|---|---|---|---|
| #2 | Starter kit never given (bug since the 1.1.01 port) | Testing | Fixed 2026-10-28 in `starter_kit.js`; FP-10 PASSED on bots ([run log](../testing/scratch_queue_run_2026-10-28.md)). Shipped to the client in 1.1.04; the server runs 1.1.05 since 2026-10-31 (`deploy/DEPLOYED.json`), so server joins get the kit now. A real singleplayer join got the Default kit on 2026-10-31 17:19 (`logs/latest.log`). Open: a real client join on the server (FP-03, SQ-SK-01, -02) |
