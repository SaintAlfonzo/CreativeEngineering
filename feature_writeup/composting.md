# Composting: the pack's composter table, rules, values and exclusions

Test prefix `CM-` (the backlog proposed `CP-`, which [can_parity.md](can_parity.md) already uses).

## 0. At a glance

| Part | Status |
|---|---|
| `global.compost`: rebuilds the composter table (`ComposterBlock.COMPOSTABLES`) from nothing every time tags load | DEPLOYED (in the 2026-09-27 initial commit; shipped since 1.0.3) |
| Rules: 10 categories by tag, id pattern, block class and edibility, first match wins | DEPLOYED |
| Values: 684 explicit per-item chances (generated once from the mods' own table, hand-edited since) | DEPLOYED |
| Exclusions: never compostable (Canned Goods, golden food, incomplete items, Botania world seed and altered grass, ...) | DEPLOYED |
| `/dumpcompost` (before / after / why) and `tools/gen_compost_values.py` (first run, `--merge` after adding mods) | in use |
| Compost and fertile soil crafting from mulch (in `sawdust.js`) | DEPLOYED; belongs to recipe progression (backlog row 20) |

- **Pending:** nothing built and unshipped. CM-01 to CM-08 have never been run. Findings F1 to F4 (section 4); none
  needs a decision.
- **Files to open:** [compost_rules.js](../kubejs/startup_scripts/compost/compost_rules.js) (categories, exclusions),
  [compost_values.js](../kubejs/startup_scripts/compost/compost_values.js) (per-item chances),
  [func/compost.js](../kubejs/startup_scripts/func/compost.js) (engine).
- **Knobs:** the two data files. After editing: `/kubejs reload startup_scripts`, then `/reload`.

## 1. Overview

Vanilla's composter reads one map, item → chance of adding a layer (7 layers fill it, then bone meal comes out). Vanilla
registers its items in five levels (0.3, 0.5, 0.65, 0.85, 1.0); every mod registers its own items however it likes, or
not at all. In a pack with hundreds of food and plant mods the table was patchy: many plants missing, some odd items
compostable. Hoppers and every modded composter read the same map.

The pack takes the table over: it clears it and refills it from its own rules and values, in the same way on a
dedicated server and its clients.

## 2. Reference

### 2.1 Tiers

| Tier | Chance | Vanilla examples |
|---|---|---|
| `low` | 0.3 | seeds, saplings, leaves, grass |
| `medium` | 0.5 | tall grass, vines, cactus, melon slice |
| `high` | 0.65 | flowers, crops, mushrooms, lily pad |
| `very_high` | 0.85 | bread, baked potato, hay, mushroom blocks |
| `full` | 1.0 | cake, pumpkin pie |

### 2.2 Categories (first match wins)

| Category | Chance | Matches |
|---|---|---|
| seeds | low | seed tags, `*seed(s)` ids, crop/stem blocks; not food; not seed bags, seed pouches, seed oil |
| saplings | low | sapling tag, `*_sapling` / `*propagule`, sapling blocks |
| leaves | low | leaves tag, `*_leaves` / leaf piles / leaf carpets |
| flowers | full | flower and petal tags, Botania floating flowers, petals, Floral Enchantments bushes / vines / baskets, flower blocks |
| mushrooms | high | mushroom tag, mushroom and fungus blocks |
| crops | high | crops, vegetables, fruit, berries, grain, nuts, tea leaves, raw veggie and fruit food types, crop/cocoa/berry/vine/wart blocks |
| baked | very_high | baked and cooked plant food types, `*bread*` / `*cookie*` (not molasses) |
| cakes_pies | full | `*pie*` / `*cake*`, cake blocks (not blaze/echo/slime/powder cakes, hats, molasses, plated food) |
| plant_matter | medium | straw, bark, sawdust, mulch, hay, thatch, packed plant blocks, mushroom blocks, moss (not hats, creosote, hay nests, reinforced) |
| plants | medium | grass, ferns, vines, kelp, seagrass, lichen, roots, moss, any bush/vine/plant block, anything Forge calls plantable |

Meat, fish, dairy, eggs, soups and meals never match a rule.

### 2.3 Values and exclusions

- **Values** (684 items): explicit chances that beat the rules. They started as every item the mods had made
  compostable, at the mods' own chance, grouped by the category the rules would give them; hand-edited since (Botania's
  seeds at full, for example).
- **Exclusions** (beat everything): `botania:world_seed`, Botania's altered grass blocks, muddy mangrove roots and
  petrified blocks, all of `canned_goods:`, golden food (`goldenfood:`, `*golden_*` except golden seeds,
  `#food_type/golden`), `*incomplete_*` items, `endercrop:`.

### 2.4 `/dumpcompost`

Writes `kubejs/exported/compost/compost.json`: `original` (the table as vanilla and mods registered it, captured on
the first rebuild), `now` (each item's chance, whether a value or a rule put it there, and which category), and
`excluded` (items a value, rule or the original table would compost but an exclusion blocked). Part of `/dumpall`.

## 3. What the pack changes

- **Engine** (`global.compost`, `func/compost.js`, priority 900, after the dictionaries and before the two data files):
  `tiers()`, `category(name, chance, { tags, ids, blocks, items, edible, except })`, `values(category, rows)`,
  `exclude(entries)`, `apply(cause)`, `report()`.
- **When it runs:** a Forge `TagsUpdatedEvent` listener (registered from the startup script) rebuilds the table: at server
  start, on `/reload`, and on each client when it receives the server's tags. So a dedicated server and its clients
  build the same table from the same scripts, and categories that read tags see the live tags.
- **Rebuild order:** clear → rules → values (beat rules) → exclusions (beat both). The log has `[compost] <cause>: N
  compostable (...)` and warnings for values naming items that don't exist or an unknown tier.
- **Generator:** `tools/gen_compost_values.py` wrote `compost_values.js` from a `/dumpcompost` once (it refuses to
  overwrite without `--force`). After adding mods, `--merge` appends only the new mods' own compostables at their
  original chance and leaves every existing row alone. Ids the item dict lacks get keys in a generated block of
  `item_dict.js`.

## 4. Limits and decisions

### Findings (2026-10-18)

- **F1. Rules can catch odd items.** Id patterns and block classes are broad (anything plantable, anything named
  `*_grass`, `*bark*`, `*cake*`). The exclusions and `except` lists cover the cases found so far; after adding mods,
  check `/dumpcompost`'s `now` for surprises (CM-06).
- **F2. A two-step reload.** Edits need `/kubejs reload startup_scripts` and then `/reload`; `/reload` alone keeps the
  old rules.
- **F3. `gen_compost_values.py` isn't in `/cegen`.** It's a manual `--merge` after adding mods (the file is hand-edited,
  so a blind regeneration would be wrong). Fine as is; noted for the tooling list ([pack_tooling.md](pack_tooling.md) F5).
- **F4. The nulch recipe** sits in `sawdust.js` (line 83) with a note that it belongs with compost ([TODO.md](todo/TODO.md) 6).

### Known limits

- **Composter only:** the rules decide the composter chance, not any other mod's composting machine with its own recipe
  list.
- **Excluded means never:** an excluded item can't be made compostable by a value row.

### Rejected options

- **Adding to the mods' table:** leaves every odd mod registration in place; a full rebuild is the only way to remove
  them.

## 5. File map

- [startup_scripts/func/compost.js](../kubejs/startup_scripts/func/compost.js), [startup_scripts/compost/compost_rules.js](../kubejs/startup_scripts/compost/compost_rules.js), [startup_scripts/compost/compost_values.js](../kubejs/startup_scripts/compost/compost_values.js).
- [server_scripts/_lib/dump_compost.js](../kubejs/server_scripts/_lib/dump_compost.js) (`/dumpcompost`).
- [tools/gen_compost_values.py](../tools/gen_compost_values.py).
- Related recipes: [wood/sawdust.js](../kubejs/server_scripts/wood/sawdust.js) (compost, fertile soil, mulch).

## 6. Offline checks

`python3 tools/gen_compost_values.py --merge --dry-run` against a fresh `/dumpcompost` lists any mod compostables that
have no row yet.

## 7. In-game test suite

Use a composter and watch its level; or read `/dumpcompost`.

- [ ] **CM-01 Rebuild log:** `/reload` → `[compost] ...: N compostable` in the log; no "do not exist" warnings.
- [ ] **CM-02 Seeds and flowers:** wheat seeds raise the level about 3 times in 10; any flower (vanilla or Botania) every
  time.
- [ ] **CM-03 Modded crop:** a Farmer's Delight tomato → about 65 %.
- [ ] **CM-04 Baked:** bread → about 85 %; a pie → always.
- [ ] **CM-05 Excluded:** a Canned Goods can, a golden carrot and a Botania world seed → refused.
- [ ] **CM-06 Dump:** `/dumpcompost` → `kubejs/exported/compost/compost.json` with `original`, `now`, `excluded`; skim
  `now` for odd items (F1).
- [ ] **CM-07 Hopper:** a hopper feeding seeds into a composter → accepted at the same chance.
- [ ] **CM-08 Client agrees:** on the dedicated server, right-click a composter with a modded crop → the client shows
  the fill (no desync), same as the server.

## 8. History

- By 2026-09-27 (initial git commit): engine, rules, exclusions, `compost_values.js` generated from the first
  `/dumpcompost`, the dump and the generator; shipped with 1.0.3.
- 2026-10-02: values merged for the 1.0.4 mods ([plan_1.0.4.md](plan_1.0.4.md) DR).
- 2026-10-18: this writeup; findings F1 to F4; tests CM-01 to CM-08. No code changed.
