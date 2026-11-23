# sawblade-recipe-routing

> IE Sawmill recipe selection driven by the loaded sawblade's {Profile} NBT — how it works, the 3 mixins, the data

Moved from agent memory 2026-11-16 (last edited there 2026-09-07). Repo status docs win over any status line here.

The IE Sawmill can run **different recipes for the same input** depending on which sawblade
is present. `minecraft:oak_planks` -> stairs / door / fence / … is chosen by the blade.

**Why it was needed:** the pack's sawmill recipes (now written by `wood_types.js`) are many `immersiveengineering:sawmill`
recipes that share one input; IE's `SawmillRecipe.findRecipe` returns only the first input
match (non-deterministic map order), so all but one were dead. Routing fixes that and makes
selection deterministic.

## Mechanism

- Recipe JSON gains an optional `"blade": "<profile>"` field. Absent/`""` = the profile-less
  default for that input (what a plain `immersiveengineering:sawblade` produces).
- Precision sawblade (`creativeengineering:precision_sawblade`) carries NBT `{Profile:"<shape>"}`.
- Creative Common [creative-common-coremod](creative-common-coremod.md) routes: among input-matching recipes, prefer the one
  whose `blade` == the loaded blade's `Profile`; else the `blade==""` default; else null
  (IE passes the item through unchanged — safe, `getCurrentStack` null-guards).

## Creative Common pieces (`com.SaintAlfonzo.creativeengineering.sawmill.*` + `.mixin.*`)

- `SawbladeRouting` — `profileOf(ItemStack)`, `route(Level, input, profile)`. Pure logic + the
  `((Recipe<?>)r).getId()` cast (IE's SRG jar hides the Mojmap `Recipe` methods; cast to the
  Mojmap `Recipe` interface so reobf maps `getId`->`m_6423_`).
- `SawmillRecipeMixin` — `@Unique String` blade field + `SawmillRecipeExt` duck iface.
- `SawmillRecipeSerializerMixin` — `@Inject` RETURN of `readFromJson`, `GsonHelper.getAsString(json,"blade","")`.
- `SawmillProcessMixin` (targets `SawmillBlockEntity$SawmillProcess`) — captures `be.sawblade`
  profile at `processStep` HEAD (server), `@Redirect`s the `SawmillRecipe.findRecipe` call in
  `getRecipeDependentData`, and persists profile + resolved-recipe-id through
  `writeToNBT`/`readFromNBT` (the client-sync path — client never runs `processStep`, so it
  re-loads the exact recipe by id via `SawmillRecipe.RECIPES.getById`).
- Not synced over the IE network — the persisted recipe id on the process handles client parity.

## Data

- `kubejs/data/creativeengineering/kubejs/wood/wood_types.js` (since 2026-11-21) — `ServerEvents.highPriorityData` walks the
  species table and calls NCH `sawmillRaw` (raw datapack JSON, ids `creativeengineering:sawmill/<input>-<output>`); the
  blade comes from the form (`sawBlades`). Replaced the node script `generation_scripts/sawmill.js` and its 211 jar JSONs
  (Rhino harness parity 211/211). Gotcha: KubeJS's Rhino `JSON.stringify` writes numbers as doubles (800.0), so
  `sawmillRaw` uses NCH `_jsonText`.
- Default per planks input: IE's own `immersiveengineering:sawmill/<species>_slab` (9 vanilla/nether,
  still live) or the generated `*_slab` (4 modded woods). `wood_types.js` unchanged.
- Blueprint recipes: `kubejs/data/creativeengineering/recipes/blueprint/precision_sawblade_*.json`
  (12, raw datapack JSON — kjs-ie's `BlueprintRecipeSchema` mangles `e.custom` blueprint recipes,
  same class of bug as memory `create-automatic-brewing`-adjacent kjs-ie schema issues). Recipe:
  `immersiveengineering:sawblade + #<shape tag> + 2 steel plates → precision_sawblade{Profile}`.
  Crafted fresh from a consumed plain sawblade — **no recipe consumes a precision_sawblade**, so
  there is no re-program / durability-reset path.
  - Shape tag is a broad vanilla tag (`minecraft:wooden_stairs` etc.) where one exists. No
    upstream tag exists for **vertical planks** or **railings**, so those two use pack tags
    `creativeengineering:vertical_planks` / `creativeengineering:railings` defined in
    `server_scripts/tags/engineeringtags.js` (13 quark vertical-plank + 13 AP/everycomp railing
    items — keep in sync with the `wood_types.js` species table). Fixed 2026-09-07: both
    recipes previously hard-coded the *oak* item only (`quark:vertical_oak_planks` /
    `architects_palette:oak_railing`), so no other wood could craft that blade.
- Blueprint item + category name: `blueprints.js` (`{blueprint:"precision_sawblades"}` + added to
  `blueprintTypes`) and `namechanges.js`. Blade profile tooltip in `tooltips.js` (`addAdvanced`).

## Crash on first sawmill use (fixed 2026-09-06)

Inserting any precision sawblade crashed: `ClassMetadataNotFoundException:
com.SaintAlfonzo.creativeengineering.sawmill.SawbladeRouting` (also `duck.SawmillProcessExt`)
while `SawmillProcessMixin` applied. `SawmillProcess` is transformed lazily on the first
sawmill tick, minutes after mod load; ModLauncher's Mixin bytecode provider can't locate a
class from Creative Common's own jar that has never been loaded during a *late* transform ("Error
loading class ... ClassNotFoundException"). Other Creative Common helper classes (`DragonBreathBrewing`,
`fan.*`) dodge this only because `commonSetup` already referenced them.
Fix: `CreativeEngineering.commonSetup` now `Class.forName`s `SawbladeRouting` +
both `duck` interfaces up front (`forceLoad(...)`), so they're defined before the lazy apply.
Any future mixin that's applied lazily and references a Creative Common class needs the same treatment.

Rows: see feature_writeup/ (sawmill/sawblade rows in todo/TODO.md).

## Done 2026-11-21: the sawmill recipes are dynamic

The node pipeline (sawmill.js -> 211 hand-copied jar JSONs) is gone; see Data above. Still to verify in game that `blade` and the secondaries survive kubejs-immersive-engineering's interception of `immersiveengineering:*` recipes (queue row, EN-).
