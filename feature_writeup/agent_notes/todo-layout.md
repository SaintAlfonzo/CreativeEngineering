# todo-layout

> feature_writeup/todo/TODO.md = # category (Food, Farming, World, General Gameplay, Tech, Large Content, Tooling), ## feature family + table, Id column keeps old family/sub #n (regrouped 2026-10-30); rows by status Bug/Planned/Pinned/In Progress/Testing/Complete, then #; alcohol its own family

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

`feature_writeup/todo/TODO.md`: a `#` heading per family (pack, food, cans, cooking, baking, dishes, farming, worldgen,
structures, mobs, loot, gear, potions, recipes, engineering, gameplay, items, blocks, ui, jei, alcohol, largecontent, ports, creativedelight, tooling)
and a `##` heading per subfamily with its own table (layout changed 2026-10-24: no Sub column; a brief is `briefs/<family>_<sub>.md`).
Columns: # / Change / Status / Work / Details / Decisions / Writeup. A row's id is `family/sub #n` from its two headings; numbers belong
to the subfamily and stay with the row when the table is re-sorted (gaps are fine); a row moving family gets the next free number there.
New subfamilies from 2026-10-24: dishes/jams, dishes/pickles-and-ferments, dishes/ocean, items/jars.
`textures` family (2026-10-25): texture-art rows pulled from other families (subs food, art, tools); each row's Details starts "(was old-id)". Partly-art rows stay put.

Status words (the user's, 2026-10-19): **Planned** (unstarted), **In Progress** (started, not complete), **Testing**
(implementation complete, real performance unknown: incl. scratch-server-only and 1.0.6+ features), **Complete** (tested
enough from real play to be functional; not all test cases need to be run). Special: **Bug** (open bug), **Pinned**.
Table order (user, 2026-10-21): subfamilies A to Z (one `##` each), within one: status Bug, Planned, Pinned, In Progress, Testing, Complete, then row number (was status-first until 10-21). The alcohol/decisions table keeps its own Open/.../Done order. Re-sort by script: parse each `##` table, key (status rank, int #), assert the line set is unchanged. My heuristic for
Complete: live since 1.0.4 or earlier, everyday play, writeup never says "never seen in game".

`alcohol` family = brewinport, vineryport, pack (pack-side alcohol rows) + a separate `alcohol/decisions` table
(statuses Open / Partly decided / Pinned / Decided / Done). The user asked to group all alcohol there, out of largecontent.

`todo/scratchpad.md` is the user's inbox. "Translate the scratchpad": every note becomes a row (new or folded into an
existing one, writeup column "scratchpad <date>"), then remove the notes but keep the file and its two headings.
Writeups cite rows as "[TODO](todo/TODO.md) `family/sub #n`"; dated History lines keep old ids as written.

**Why:** the user wants one place, by area, that shows how far each change is, completed work out of the way.
**How to apply:** when a writeup gains a finding, plan or test backlog, add or update its TODO row in the same session.
Related: memory `feature-writeup-pattern`.

**Archive (2026-10-26):** Complete rows no longer sit in the family tables; they live in a `# archive` table at the bottom of TODO.md (one table, Id column `family/sub #n`, ids never change). A subfamily with nothing open shows "No open rows". At each sweep: re-sort, move new Complete rows there, update the "Status snapshot" counts near the top. The alcohol/decisions table is not archived.

**merges family (2026-10-27):** `# merges` sits between dumps and pack (subs baking, dishes, drinks, fruit, materials, mobs); plans in `item_merges.md` section 5 and `briefs/merges_<sub>.md`. Open merge rows from other families were moved in with `(was old-id)` in Details and old ids rewritten across the docs. The scratchpad keeps only the decorative blocks overlap survey.


**Regroup 2026-10-30 (supersedes the family/sub headings above):** TODO.md is now seven `#` categories (Food, Farming, World, General Gameplay, Tech, Large Content, Tooling), each with `##` feature families named like the changelog (Canning, Jams, Meals, Biomes...). The first column is `Id` = the old `family/sub #n`, unchanged, so every citation still resolves; new rows take the next number of the old family/sub they would have had. Mapping lives in the script logic only, not on disk: find a row's home by grepping its id. alcohol decisions are a `### Decisions` table under `## Alcohol`; the dumps/blocked pointer table sits under `## Blocked on Dumps`. Briefs keep their `<family>_<sub>.md` names.

**Sort key and splits (2026-11-02):** the real table order is (status rank Bug, Planned, Pinned, In Progress, Testing, Complete; then the Id string; then #n), not just status then number; Creative Split keeps (status, #n) as its work order. Tables over about 20 rows are split into feature tables (Meals into Meal Rules and Feasts / Rice, Egg and Sushi / Soups, Salads and Sandwiches / Pizza, Pasta and Pancakes / Skewers / Dish Merges; Baked Goods, Alcohol, Mod Additions, Crops, JEI, Ingredients, Kitchen, CD Members, Performance likewise; new Flower Pots table). Ids are unchanged. Every main table needs its `|---|` separator line (Seasoning was missing it).

**Categories (2026-11-02):** the seven categories were split into 22 narrower ones (Food Rules, Drinks and Drugs, Kitchen and Cooking, Baking and Sweets, Dishes and Ingredients, Farming, Worldgen, Structures, Mobs, Loot and Trade, Gear and Combat, Potions and Effects, Recipes and Materials, Mechanics, Items and Blocks, Interface and JEI, Tech, Alcohol, Mods and Ports, Creative Delight, Creative Mods, Tooling); `##` families and ids unchanged.
