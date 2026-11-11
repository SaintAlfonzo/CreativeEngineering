# Plan: Rustic Delight baked into its Creative Delight member (2026-11-10)

Status: BUILT 2026-11-10, offline checks pass, live rows SQ-RDB / CQ-RDB queued, CD jar built but not installed (record:
[rustic_delight.md](rustic_delight.md) 5.14; deviations: the coaster pancake parent model is kept, a fix; the CD
`NOTICE-thirdparty.txt` covers the main jar only and says each member carries its own notice, so Rustic's MIT notice is in
the member jar, not added there). Was: PLANNED. Trigger: PhantomWing confirmed MIT and welcomed the port (2026-11-10,
`communications/RusticDelight-PhantomWing.png`, [licence_inquiries.md](todo/licence_inquiries.md),
[rustic_delight.md](rustic_delight.md) 5.13). Owner request: fold Rustic Delight fully into the Creative Delight jar, as
its own member, with all code and content staying in the `rusticdelight` namespace.

## 0. Goal and shape

Today the member `members/rustic` (`:backportedRustic`, mod id `rusticdelight`) holds only our code (25 Java files) and
reads upstream's 1.7.0 art and data at runtime from `rustic_upstream/rusticdelight-forge-1.20.1-1.7.0.jar`, translated in
memory by `RusticTranslator`. After this plan the member jar carries those files itself:

- **Still a separate member**, nested in `creativedelight` (`bundled_in=creativeDelight`), built and installed only through
  `:creativeDelight:`. No change to how Creative Delight nests it.
- **Mod id, registry namespace, resource paths, lang keys, tags, loot, worldgen ids all stay `rusticdelight`.** Nothing moves
  to `creativedelight`: every id is upstream's, so [plan_cd_namespace.md](plan_cd_namespace.md) does not apply to it. No id
  changes, so no swap list and no save conversion.
- **No `rustic_upstream/` folder on clients or servers.** The runtime reader (`RusticTranslator`, `UpstreamJars`, the two
  built-in packs) is deleted.
- Display name stays "Rustic Delight" (owner: keep the name). Current `mod_name` "Rustic Delight (Creative Delight)" is
  kept unless the owner wants the suffix dropped (D2).

## 1. Decisions (D1, D2 answered; D3 default stands)

| # | Question | Default used by the plan |
|---|---|---|
| D1 | Java package | **Decided 2026-11-10: keep `com.SaintAlfonzo.creativedelight.rustic`** (no rename) |
| D2 | Display name suffix | **Decided 2026-11-10: keep "Rustic Delight (Creative Delight)"** |
| D3 | Keep the `rustic_upstream/` jar on the dev machine as the pinned source for the bake script? | Yes, dev only, never deployed (upstream-jar policy) |

## 2. Licence and notices (first, blocks everything)

1. Add `NOTICE-upstream-MIT.txt` and `LICENSE` to the member, like `members/oceans`: MIT text, "Copyright (c) PhantomWing"
   (check the `version/1.21` branch licence file for the exact holder and year line; the 1.7.0 tag has none). Mention the
   2026-11-10 confirmation and the screenshot path.
2. `gradle.properties`: `mod_license=MIT (upstream art and data); GPL-3.0-only (port code)`; rewrite the header comment
   (it still says All Rights Reserved) and `mod_description` (no longer "reads its art from the upstream jar").
3. Add Rustic to the CD `NOTICE-thirdparty.txt` as MIT, `creative_delight.md` licence table (line 54 area) and the notices
   check (TODO `licence/cd #3`).
4. Keep upstream-derived files and our own files distinguishable (see step 3 layout) so the notice stays accurate.

## 3. Bake script (reproducible, committed output)

New `members/rustic/tools/bake_upstream.py` (replaces `tools/check_donor.py` as the donor check):

1. Verify the pinned jar by sha1 (`bca9adf6...`, from `rustic_upstream/`, dev machine only).
2. Copy `assets/rusticdelight/**` and `data/rusticdelight/**`, plus the other upstream data folders the translator
   passes today (`data/forge/tags`, `data/minecraft/tags`, `data/sereneseasons/tags`, `data/botanypots/recipes`, 
   `assets/rusticdelight/brewinandchewin`), into `src/main/resources/`, applying the rules below once.
3. Never copy: classes, `META-INF`, `pack.mcmeta`, advancements (257 files), biome modifiers, `data/farmersdelight/recipes`,
   the dropped loot modifiers (`DROPPED_*` in `RusticTranslator`).
4. Write `tools/bake_manifest.json` (path, sha1 of upstream file, rule applied) so a later upstream update can be diffed.

Rule translation, from `RusticTranslator` (every rule gets a Python function and a unit check):

| Translator rule | Baked form |
|---|---|
| Removed ids (`fruit_beignet`, `pancake`, `fried_dough`): drop files, lang keys, ingredients | Same, done once; `pancake` ingredient replaced by `supplementaries:pancake` |
| 1.20 loot `any_of` / `all_of`, conditions | Rewritten to 1.19.2 `alternative` / inverted forms once |
| Tag entries from other mods | `required: false` once |
| Cherry blossom tag to Vinery / Quark leaves | Written as the 1.19.2 tag |
| Wild and giant patches keep three base colours | Patch JSON written without the rare colours |
| Renamed model refs (`template_crop_cross` to `crop_cross`, etc.) | Rewritten once |
| **Runtime check**: drop a recipe or file naming an unregistered item / serializer (`missingRecipeId`, `missingItem`) | The one rule that needs the game. Bake as a static decision against the pack's mod list; where the missing item belongs to another optional mod, keep the recipe with a `forge:mod_loaded` condition instead of dropping it |

Our own overlay files stay where they are (`blockstates/glowing_calamari_crate.json`, its loot table).

## 4. Member restructure

1. Delete `upstream/RusticTranslator.java`, `upstream/UpstreamJars.java`; remove the built-in pack registration in
   `RusticDelightMod`; keep every class that registers content (`registry/`, `content/`, `logic/`, `client/`).
2. `gradle.properties`: remove `shared_sources=upstream` (keep `packs` only if still used), `shared_base` if unused,
   `resource_guard_*` (the guard exists to forbid upstream resources in the jar; it must go, or it fails the build),
   `donor_jar` / `donor_sha1` (move to the bake script header).
3. Confirm the jar no longer needs `development/shared` `DonorJars` / `BuiltInPacks.addUpstream` for Rustic (other members
   still use them; do not delete the shared sources).
4. Hard dependency stays Farmer's Delight only.

## 5. Fold in the pack-level Rustic files

- `kubejs/assets/rusticdelight/textures/` (owner-accepted pancake recolours, `rustic_delight.md` 5.11) and
  `kubejs/data/rusticdelight/worldgen/` (the 1.3.0-id overrides) move into the member as ordinary files: the recolours
  replace the baked copies, the worldgen overrides are checked against 1.7.0's placed features and either merged or dropped
  (they only served 1.3.0 ids, `rustic-delight-port` memory).
- Remove the pack copies only after the equivalence check (step 7) passes. Gotcha to respect: KubeJS generated assets
  outrank jar assets, so a stale pack copy would hide the baked one.

## 6. Cross-references that name `rustic_upstream`

| File | Change |
|---|---|
| `tools/build_deploy.py` lines 215, 593, 1584 to 1590 | Stop shipping and mentioning `rustic_upstream/` (client and server lists, the "goes next to `mods/`" note); keep the entry only if Common's jar-mover still needs it for old installs |
| `tools/gen_crop_worldgen_table.py` | Read from the member's committed resources, not the donor jar |
| `development/creativeDelight/members/rustic/tools/check_donor.py` | Replaced by `bake_upstream.py --check` |
| Creative Common upstream-jar detection | Check for a Rustic entry; remove it so Common stops moving or warning about the jar |
| `mod_sides.md`, `pack/MODLIST.md` (via `gen_pack_manifest.py`) | Rustic row: bundled, MIT, no donor |
| [rustic_delight.md](rustic_delight.md) 5.1, 5.7, 5.13; [creative_delight.md](creative_delight.md); [pack_tooling.md](pack_tooling.md); README row 87; memory `rustic-delight-port` | Rewrite "donor jar / read at runtime" to "baked, MIT" |

## 7. Verification

**Offline equivalence test (main regression guard).** Before step 4, dump the current runtime view: run
`RusticTranslator.assets()` and `data()` (headless harness, [patchermod-headless-smoke-test]) into a directory.
After baking, diff that dump against the member's resources. Expected differences are only: the runtime drop of
unregistered recipes (now static or conditioned), our overlay files, and the recolours. Anything else is a bake bug.

Further offline checks: `./gradlew :backportedRustic:build --offline` passes with the guard removed; every `rusticdelight:`
model and texture reference resolves; every recipe, loot table and tag parses as 1.19.2 JSON; no `1.20` format leftovers
(`all_of`, `any_of`); the jar has no class or file from upstream's `META-INF`.

**Scratch queue** (rows added to [scratch_queue.md](testing/scratch_queue.md) in the same session, run only when the owner
says to; skill `scratch-test`), prefix `SQ-RDB`:

| Row | Check |
|---|---|
| SQ-RDB-01 | Server boots with no `rustic_upstream/` folder: no Rustic error, 162 items and all blocks register |
| SQ-RDB-02 | Recipe, loot table and tag counts for `rusticdelight` equal the pre-change run (`/dumpall` numbers) |
| SQ-RDB-03 | Existing checks that must still pass: SQ-RD-08, -10, -11, -22 (open FAIL), -24, -26, SQ-RDX-01 to -14 |
| SQ-RDB-04 | Wild pepper, cotton, coffee patches generate; seed trades and chest seeds unchanged |
| SQ-RDB-05 | An old save with 1.3.0 plates and crops loads, nothing lost |
| SQ-RDB-06 | Same world with an old `rustic_upstream/` folder left in place: no duplicate load, no error |
| CQ-RDB-01 | Client: pepper, pancake, coffee, syrup models and textures, pancake recolours, lang, JEI, tooltips |

## 8. Order of work

1. Licence files and gradle.properties text (section 2).
2. Pre-change runtime dump for the equivalence test (7).
3. Write `bake_upstream.py` rule by rule, bake, run the equivalence diff (3, 7).
4. Restructure the member, delete the reader, drop the guard, build (4).
5. Fold pack overrides, re-diff (5).
6. Update tools, docs, notices, TODO, scratch queue (6, 7).
7. Build `:creativeCommon:` only if Common's detection changed, then `:creativeDelight:build` and install per the
   CLAUDE.md rules (never replace a jar while the game runs; ask if unsure).

## 9. Risks and rollback

- **Runtime-dependent drops** (section 3, last rule) are the main risk: a recipe that exists in the old view but not the new
  one, or the reverse. The equivalence diff catches it.
- **Missed notice**: covered by section 2 and the notices check.
- **Stale pack overrides** hiding baked files: section 5 ordering.
- **Upstream 1.7.1 later**: rerun the bake script, diff `bake_manifest.json`, review.
- **Rollback**: the donor-route member (this git state, jar in `jar_archive/`) still works with `rustic_upstream/`.

## 10. Done means

Writeup and test list updated; TODO row `creativedelight/members #55`; deploy note in [deploy.md](deploy.md) that
`rustic_upstream/` is no longer shipped. **Player changelog: no bullet** (same behaviour); mention only if a player-visible
difference turns up in the equivalence diff. Hand over with the SQ-RDB / CQ-RDB ids and the CD jar waiting to be installed.
