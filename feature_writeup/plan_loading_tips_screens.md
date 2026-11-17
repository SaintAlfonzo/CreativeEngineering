# Loading tips: more screens, tip ids, override packs (plan, 2026-11-14)

<!-- generated:status_index begin open_work sha1=f61f45c8ea25 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 5 open TODO rows (Planned 1, In Progress 1, Testing 3), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `ui/loadingtips #2` | Planned | S C | Tip announcements in chat |
| `ui/loadingtips #3` | In Progress | C | Creative Tips: the pack's own tips |
| `ui/loadingtips #1` | Testing | S C | Loading screen tip bar and tip packs |
| `ui/loadingtips #4` | Testing | C | Audit Loading Tips against pack changes |
| `ui/loadingtips #5` | Testing | C | Tip ids, override packs, tips on every loading screen |
<!-- generated:status_index end open_work -->

Owner request 2026-11-14: show the tip bar on every loading screen between world / server selection and join (reading
data, connecting, joining, receiving the level), give every tip in the Loading Tips pack an id, and make Creative Tips an
override pack in the same format that replaces individual tips by id. Lives in Creative Common (`loadingtips/`).
Mechanism: [loading_tips.md](loading_tips.md); TODO row `ui/loadingtips #5`. Built 2026-11-14 (steps 2 to 6; step 1
from sources, its in-game half is LT-07 to LT-09), installed in `mods/creativecommon-1.2.0.jar` 23:22, not seen in game.

## 1. Decisions (owner, 2026-11-14)

- **Loading Tips returns to the unmodified upstream state**: all 532 tips of Loading Tips 1.2.1+26.1 (Naw7k, MIT), text
  verbatim, one id each. Every change the pack made (version filter, audit removals, rewrites, reword, owner edits,
  capital and period normalization) moves into Creative Tips as overrides.
- **Ids are slugs**: `loading_tips:<slug>`, lowercase, words joined by `_`, derived once from the upstream text, cut at
  about 48 characters on a word boundary, a numeric suffix (`_2`) on a collision. Ids are stored in the file; they never
  change when a tip's wording changes. Creative Tips' own new tips use `creative_tips:<slug>`.
- **One tip for the whole load**: the tip on screen stays the same across every loading screen of one join and rolls only
  on the normal `cycleSeconds` timer (today each new screen forces a new tip).
- **Override is a file switch, not a per-tip setting.** A tip file with `"override": true` makes every tip in it replace
  the tip with the same id from an earlier file, with no flag on the individual tip. A file without the switch never
  replaces: a repeated id keeps the first tip and logs the clash.

## 2. Tip file format

```json
{
  "override": true,
  "priority": 10,
  "remove": ["loading_tips:wolves_love_bones_to_tame_and_meat_to_heal"],
  "tips": [
    { "id": "loading_tips:beds_explode_in_the_nether", "text": "Beds explode when used outside the Overworld." },
    { "id": "creative_tips:glass_shards", "text": "A tip with a new id is added." },
    "A plain string is still a tip (generated id), for third-party packs."
  ]
}
```

- `override` (default false): see section 1. `remove` (list of ids) is read only when `override` is true.
- `priority` (int, default 0): files are applied in ascending priority, then by file id, so override order no longer
  depends on namespace spelling. Loading Tips stays 0, Creative Tips 10.
- A tip is `{id, text}` or a plain string. Text is plain text or a lang key, as now. Blank text is skipped.
- A new-id tip in an override file is added at the end. A replaced tip keeps its place in the list.
- `"replace": true` (drop everything read before) stays.
- The loader keeps an ordered id -> text map; deduplication moves from text to id. The tip deck is built from the map.
- The loader logs: file count, tips kept, replaced, removed, id clashes, ids named by `remove` or an override that match
  nothing (a typo or an upstream change shows up in `latest.log`).

## 3. Screens

1. **Inventory (first, before any code).** List what the 1.19.2 client shows between world / server selection and join,
   from the decompiled sources plus Forge's handshake screens: the "Reading data" dirt screen
   (`GenericDirtMessageScreen`), `ConnectScreen` (connecting and negotiating), `ProgressScreen`, `LevelLoadingScreen`,
   `ReceivingLevelScreen`, and anything found when joining a modded server. Confirm in game by logging each screen class
   shown during singleplayer start, world creation, server join and dimension change. Forge's start-up
   `LoadingOverlay` is an overlay before world selection and stays out.
2. **Bar.** Render on the inventoried set; the set is one allowlist in `LoadingTipBar` so a screen is a one-line add.
   `current` and `shownAt` survive a screen change when the previous loading screen ended less than a few seconds ago
   (one join = one tip, rolled by the timer). Check the bar against each screen's widgets (the connect screen's Cancel
   button) at small window and GUI scale 4.
3. `[loading_tips] enabled` and `cycleSeconds` apply to every screen. A per-screen toggle only if the inventory finds a
   screen where the bar is wrong.

## 4. Migration (one script, `tools/loading_tips_migrate.py`, run once, kept for the record)

1. Read the upstream list (`DecompiledMods/loading_tips-1.2.1+26.1/assets/loading_tips/text/loading_tips.json`, 532
   tips) and write Loading Tips' `tips.json` as `{id, text}` objects, text unchanged (credits unchanged).
2. Take the current effective list (Loading Tips 321 + Creative Tips 14, owner edits of 2026-11-07 included) as the
   target. Diff it against upstream, by id:
   - **not in the target**: the version-filtered tips (appendix A of loading_tips.md, 192), the audit removals (appendix
     B), "Don't dig straight down!": all become entries in Creative Tips' `remove` list (about 216);
   - **in the target with different text**: about 302 normalization-only differences (trailing period or capitals) plus
     the 5 owner rewrites and the baby-mobs reword: become same-id replacements;
   - **the 14 Creative Tips that rewrote an audited tip**: matched to the upstream tip they replaced and written as
     same-id replacements instead of new tips (the match is listed for owner review before the run; an unmatched one
     stays a new `creative_tips:` tip).
3. Write Creative Tips' `tips.json` (`override: true`, `priority: 10`).
4. **Acceptance check (offline):** apply the two files with a plain script that mirrors the loader's rules and compare
   with the old effective list: same 335 texts, same set. Only then replace the files.
5. Update `tools/loading_tips_md.py` for the object format (list ids, mark replaced and removed tips per pack).

Open point (flagged to the owner): the version filter and the normalization become about 520 override entries in
Creative Tips, because Loading Tips is restored verbatim. If the filter should instead stay as a baseline, say so and
the removals drop out of Creative Tips.

## 5. Dependents

- The planned chat announcer (`ui/loadingtips #2`) reads the same files: it must apply the same override rules, so its
  brief points here. It would read the merged list, never the raw packs.
- Appendix A and B of [loading_tips.md](loading_tips.md) and [plan_creative_tips.md](plan_creative_tips.md) get a note
  that the drops now live as removals and replacements in Creative Tips.
- Creative Common rebuild + install + client restart for the Java and the Loading Tips file; Creative Tips (KubeJS
  assets) needs F3+T only.

## 6. Steps

| # | Step | Status |
|---|---|---|
| 1 | Screen inventory (section 3.1), list and log in game | Sources DONE 2026-11-14 (section 8); the bar logs each screen it draws on, in-game check LT-07 to LT-09 |
| 2 | Loader: ids, file-level `override`, `remove`, `priority`, log lines | DONE 2026-11-14 |
| 3 | Migration script, owner review of the 14 matches, acceptance check, replace the two files | DONE 2026-11-14: all 14 matched (section 8), acceptance PASS (335 = 335); owner review after the fact |
| 4 | Bar: allowlist, tip carried across screens | DONE 2026-11-14 |
| 5 | `loading_tips_md.py`, loading_tips.md sections 2, 5, 7 and appendix notes, TODO and README rows | DONE 2026-11-14 |
| 6 | Build `:creativeCommon:`, install, scratch queue rows, changelog bullet (Loading screen tips) | DONE 2026-11-14: installed 23:22, CQ-LT-07 to CQ-LT-14, two bullets under Interface (the bar's only family so far) |

## 7. Tests (rows go into loading_tips.md section 7 when built)

| ID | Check | Expected |
|---|---|---|
| LT-07 | client, start a singleplayer world | a tip shows on "Reading data" and on the chunk screen; the same tip, rolled only by the timer |
| LT-08 | client, join a server | tip on the connecting and receiving screens, same tip until the timer rolls it |
| LT-09 | client, change dimension | tip on the transition screen |
| LT-10 | client | `latest.log`: tips kept equals the effective count, replaced about 300+, removed about 216, no id clash, no unmatched id |
| LT-11 | client, a test pack without `override` that repeats an id | the first tip stays, a clash line in the log |
| LT-12 | client, a test pack with `override: true` and one tip | that tip replaces the same-id tip, no per-tip flag needed |
| LT-13 | client | a plain-string tip from a third-party pack still shows |
| LT-14 | client, small window, GUI scale 4 | the bar does not cover a screen's buttons |

## 8. Results (2026-11-14)

**Screen inventory, from the Forge 1.19.2-43.3.9 sources.** Bar always (starts or continues a join):
`ConnectScreen` (connecting, Forge's negotiation runs under it), `LevelLoadingScreen` (singleplayer chunks),
`ReceivingLevelScreen` (joining, dimension change), `GenericDirtMessageScreen` titled `selectWorld.data_read`
("Reading world data"), `createWorld.preparing` or `dataPack.validation.working`. Bar only inside a join (a loading
screen shown less than 5 s ago), because these also show when leaving or deleting a world: `ProgressScreen`
("Joining world" in `Minecraft.setLevel`, server resource pack download) and any other dirt screen (`multiplayer.applyingPack`).
Never: the dirt screen `menu.savingLevel`. `DatapackLoadFailureScreen` is a prompt, not a loading screen. The bar logs
`Loading tips: bar on <class> (<title key>)` once per screen change, so the in-game half of step 1 is a grep
(LT-07 to LT-09); a modded screen between them shows up as a gap in that list. The bar moves below the lowest
visible widget when it would cover one and skips the frame when that runs off screen (connect screen at GUI scale 4).

**Migration** (`tools/loading_tips_migrate.py`, dry run without `--write`; refuses to run again on migrated files):
Loading Tips 532 tips with ids; Creative Tips `override: true`, priority 10, `remove` 197 (192 version filter, 4 audit
drops, "Don't dig straight down!"), 321 same-id replacements (302 normalization only, 5 owner rewrites and the
baby-mobs reword, 14 audit rewrites), no new `creative_tips:` tips. Acceptance: 335 effective texts, the same set as
before. The 14 Creative Tips and the 5 Loading Tips rewrites are pinned by exact upstream text in the script (fuzzy
matching picked wrong tips for gold tools and villager trading). The old files are in git at ce01f9cd.

| Creative Tip | Upstream tip it replaces |
|---|---|
| Glass breaks into glass shards unless mined with Silk Touch; ... | Use a silk touch pickaxe for glass. |
| Composters turn plants, food, straw, bark and sawdust into bone meal | Composters turn food into bone meal. |
| Kill a mob an Axolotl is fighting to gain Regeneration | Axolotls help fight guardians and grant Regeneration when they kill one! |
| Gilded apples give massive buffs | Enchanted apples give massive buffs. |
| Some Pillager outposts hold caged Allays | Pillager Outposts hold trapped Allays. |
| Leather boots let you walk on powder snow | Leather boots let you walk on snow. |
| Compasses point to spawn in the Overworld, ... | Compasses point to world spawn. |
| Lava flowing over soul soil next to blue ice turns into basalt | Basalt forms from blue ice and lava. |
| Water and lava make cobblestone, or other stones ... | Cobblestone forms from water and lava. |
| Every player can take an elytra from each End ship | End ships hold the Elytra. |
| Facing a Creeper blast with a shield cuts the damage; ... | Shields can block Creeper explosions if you are facing them. |
| Sweet berry bushes slow you down and hurt you unless you wear leggings | Sweet Berry bushes slow you down and deal damage. |
| A totem of undying saves you from your hand or your charm slot | Totems of Undying must be held to save you from death. (the other totem tip was a dropped duplicate) |
| Lava buckets are a great furnace fuel | Lava buckets are the best furnace fuel. |

Open point of section 4 (filter as removals or as baseline): built as removals, per section 1.

The counts above are the migration's. Tip audit 2 (same day, [loading_tips_audit.md](loading_tips_audit.md)) then
edited Creative Tips by id: 319 removes, 210 replacements, 213 tips. Do not rerun the migration script.
