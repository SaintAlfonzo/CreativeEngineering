# Plan: Portal Gun fixes and portal colour dyeing

Owner notes of 2026-11-07 (scratchpad "portal changes"). Investigation done 2026-11-07; all four built 2026-11-07 15:58 (section 7), jar not installed. All
decisions answered by the owner 2026-11-07 (section 5).
Parent writeup: [immersive_portals.md](immersive_portals.md) 2.2. TODO rows `gameplay/tnt-and-portal #13..#16`.

## 0. At a glance

| # | Change | Cause found | Fix | Home | Status |
|---|---|---|---|---|---|
| 1 | Portal and mirror have no name | Entities with no lang key | Lang keys "Portal", "Mirror" | Creative Compat names.js | BUILT |
| 2 | Hotbar desaturates the gun and every slot left of it | GeckoLib flushes ImmediatelyFast's hotbar batch early | Client mixin: skip that flush while ImmediatelyFast is batching (not the config switch) | Creative Compat | BUILT |
| 3 | Player is lifted one block after walking out of a wall portal | Immersive Portals' post-teleport lift counts the wall behind the exit portal | Lift only when the exit portal is on the floor | Creative Compat | BUILT |
| 4 | Dye the gun's portal colours by grid column, as with Sophisticated Backpacks | New feature | Own crafting recipe writing the gun's existing portal colour data; no model change | Creative Compat | BUILT |

The mod: `portalgun-1.19.2-5.0.0.jar`, mod id `portalgun` (MeowMC, qouteall, Thelnfamous1), **MIT**, built on Immersive
Portals 2.3.7 (`imm_ptl_core`) and GeckoLib 3.1.40. Items: `portalgun:portal_gun`, `portalgun_body`, `portalgun_claw`.
The portal is the entity `portalgun:custom_portal` (a subclass of Immersive Portals' `Portal`), not a block.

Side finding: `item_dict.js` lists `i.portal_gun`, `i.golden_portal_gun`, `i.prime_portal_gun` as `ricksportalgun:*`, a
different mod that is not in the pack (dead keys, log_warnings S17). Point `i.portal_gun` at `portalgun:portal_gun`, add
`i.portal_gun_body` / `i.portal_gun_claw`, drop the golden and prime keys. This needs a Creative Common rebuild, so do
it in the next Common build, not on its own.

All four changes are gated mixins and scripts on third-party mods with no wider system, so they belong in Creative
Compat (`development/creativeCompat`). Gate each mixin in `CompatMixinPlugin` on its mods (`portalgun`, `imm_ptl_core`,
`geckolib3`, `immediatelyfast`). Put the client ones under `client`.

## 1. Names: Portal and Mirror

**Found.** Immersive Portals' `en_us.json` names only `imm_ptl_core:portal` and `nether_portal_new`. The Portal Gun
ships no entity key. So WTHIT, `/kill` and death messages show raw keys for:

| Entity | Key | Name |
|---|---|---|
| `portalgun:custom_portal` (the gun's portal) | `entity.portalgun.custom_portal` | Portal |
| `imm_ptl_core:mirror` | `entity.imm_ptl_core.mirror` | Mirror |
| `imm_ptl_core:breakable_mirror` (the one players make: flint and steel on glass in a frame) | `entity.imm_ptl_core.breakable_mirror` | Mirror |

Only these three. The other Immersive Portals entities with no key in its lang (`end_portal`, `end_floor_portal`,
`global_tracked_portal`, `border_portal`, `general_breakable_portal`, `loading_indicator`) are left alone: the owner
sees a valid name on the End Portal in game (D1), and the rest are excluded by config or never looked at.

**Work.** Add the keys to `kubejs/assets/creativecompat/kubejs/names.js` with the 3-argument form
`e.add('<namespace>', key, name)` (lang merges per namespace, see names.md). That tree is packed into the Compat jar
(`scriptTrees`), so it needs a Compat rebuild and install. Testing it from `kubejs/` alone is not enough.

## 2. Hotbar desaturation

**Found.** The gun's item renderer is GeckoLib's `GeoItemRenderer` (model `builtin/entity`). In the GUI branch of
`renderByItem` it does:

```
BufferSource src = bufferSource instanceof BufferSource b ? b : Minecraft...bufferSource();
Lighting.setupForFlatItems();
render(...);
src.endBatch();                 // flushes everything buffered so far
RenderSystem.enableDepthTest();
Lighting.setupFor3DItems();
```

ImmediatelyFast 1.4.0 has `hud_batching: true` (config/immediatelyfast.json). It batches the hotbar's item models into
`BatchingBuffers.ITEM_MODEL_BATCH`, which is itself a `BufferSource`. So GeckoLib's `endBatch()` flushes that batch
part-way through the hotbar. Slots 1 to N (left of the gun) plus the gun are drawn early, outside the render state
ImmediatelyFast sets up for its own flush. Slots right of the gun are buffered afterwards and drawn normally. That is
exactly the symptom. Any other GeckoLib 3 item in the hotbar would do the same.

**Fix (decided, D5): client mixin on `software.bernie.geckolib3.renderers.geo.GeoItemRenderer#renderByItem`
(`m_108829_`).** Use `@Redirect` on the `BufferSource.endBatch()` call in the GUI branch: skip it when the source is
ImmediatelyFast's active item batch (`BatchingBuffers.ITEM_MODEL_CONSUMER == src`, read reflectively or by class name
so Compat has no hard dependency). The gun's vertices stay in the batch. ImmediatelyFast keeps the flat lighting that
was current when they were buffered (its `LightingState` wrapper), and the whole hotbar flushes once at the end.
Outside HUD batching (inventories, JEI, which `experimental_screen_batching: false` leaves unbatched) nothing changes.

Not chosen: `hud_batching: false` in `config/immediatelyfast.json`. It costs some HUD frame time every frame for every
player. It is still the quickest proof of the cause: flip it once in a client, see the desaturation go, put it back.

## 3. Lifted one block after leaving a portal

**Found.** After a client teleport, Immersive Portals runs `ClientTeleportationManager.adjustPlayerPosition` when the
entered portal's `PortalExtension.adjustPositionAfterTeleport` is true (the default). It takes the union of every block
collision in the bottom half of the player's box and, if that union's top is above the feet, lifts the player to it
(+0.01) over 5 ticks. Walking out of a wall portal, the box still straddles the exit portal's plane, so it overlaps the
wall block the portal is on. The union top is that block's top, so the player is lifted about one block. The gun's
`CollisionHelperMixin` excludes that wall from movement collision, but this lift check calls `getBlockCollisions`
directly and never sees the exclusion.

**Evidence:** the owner's `logs/latest.log` (2026-11-07) has 67 `Client Teleported CustomPortal{...}` lines and 60
`Adjusting Client Player Position` lines. Most are through `south` and `west` (wall) portals.

**Fix (decided, D3): the lift runs only when the exit portal is on the floor** (it faces up). There it is what lifts
the player clear of the floor block the portal sits on. Wall and ceiling exits get no lift.

- Common (both sides) mixin on `tk.meowmc.portalgun.entities.CustomPortal`:
  - in `updateState`, before the `reloadAndSyncToClient()` call in the branch that sets the destination, set
    `PortalExtension.get(this).adjustPositionAfterTeleport = exitOut.y > 0.5`, where
    `exitOut = transformLocalVecNonScale(getContentDirection())` (the direction the player leaves the exit portal);
  - at the tail of `readAdditionalSaveData` (`m_7378_`), recompute the same value, so portals placed before the fix and
    the client copy (which gets the entity through that path) agree.
- The flag is saved and synced with the portal's NBT (`PortalExtension` writes `adjustPositionAfterTeleport`), so the
  client check reads the server's value.

Not chosen: (a) the lift off for every gun portal (players may sink into the floor on floor exits); (c) keep the lift
and filter out the exit wall: a `@Redirect` of `getBlockCollisions` in
`adjustPlayerPosition` through the exit portal's `CollisionExcluder`. It is the most exact, but `adjustPlayerPosition`
gets only the player, so the exit portal has to be found, which is more code and more risk.

## 4. Dyeing the portal colours

**Sophisticated Backpacks' rule** (`StorageDyeRecipeBase`, `BackpackDyeRecipe`): any crafting grid, one backpack plus
dyes (`forge:dyes`) anywhere. A dye in a column **left of or in** the backpack's column colours the main part. A dye
**right of or in** its column colours the trim, so a dye in the same column colours both. Several dyes average like
leather armour (`ColorHelper.calculateColor`), and the current colour joins the average unless it is still the
default. There is no washing.

**The gun's existing colour data** (`me.Thelnfamous1.portalgun.ColoredPortalGun`, which `PortalGunItem` implements):
stack NBT `CustomPortalColors {blue:int, orange:int}`. Only the `/customportalcolors` command sets it today. With no
entry a side uses its default (blue `#005DDF`, orange `#EE7F1B`). When a portal is shot, `colorPortal` copies the side's
colour onto the `CustomPortal` entity, and `CustomPortalEntityRenderer` draws the portal in it. The tooltip already
lists each custom colour ("Blue Portal Color: #RRGGBB"). The gun's item model ignores these colours, and that stays as
it is (owner, 2026-11-07).

**Design (decided, D2).** The two grid parts are the two portal colours, and the dyes write `CustomPortalColors`, the
same data the command writes:

| Grid position (Sophisticated's column rule) | Portal side | Fired by |
|---|---|---|
| Column left of the gun, or the gun's column | Blue | Left click (`onAttack`) |
| Column right of the gun, or the gun's column | Orange | Right click (`use`) |

Left goes with the left mouse button. Several dyes on one side average. The side's current colour joins the average
when it has a custom entry (as with a dyed backpack); a side still on its default colour is replaced, not mixed with
its default. The command keeps working and edits the same data.

**Work.**

1. **Recipe:** own `CustomRecipe` `creativecompat:portal_gun_dye` with the same column rule and averaging. Write it
   ourselves rather than depend on Sophisticated Core's classes (it is GPLv3 like our code, but the dependency is not
   worth it). It matches `portalgun:portal_gun` plus at least one dye and nothing else. Output: a copy of the gun with
   `setCustomPortalColorForSide` called for each side that got dyes, so all its other NBT is kept. Register the
   serializer in Compat. Add one `data/creativecompat/recipes/portal_gun_dye.json` (`{"type": ...}`), shaped like
   Sophisticated's `backpack_dye.json`. A special recipe has no ingredients for `global.NCH` to build, so it is a jar
   data file. Check whether NCH has a special-recipe entry first and use it if so.
2. **Portals already placed** keep their old colour until they are shot again (the colour is copied at shot time).
   That is upstream behaviour; the changelog bullet says it.
3. **JEI:** example recipes as Sophisticated does: one per dye for each side, plus one two-sided example, from
   Compat's JEI plugin. The output's tooltip shows the colour lines.
4. **No washing** (D4). To go back to the defaults, use `/customportalcolors` clear.
5. **Later, not now:** tinting the gun's lights on the item model to match.

## 5. Decisions (owner, 2026-11-07)

| Id | Question | Decision |
|---|---|---|
| D1 | Name more Immersive Portals entities? | No: only the gun portal and the two mirrors; the End Portal already shows a valid name |
| D2 | What do the two dye parts colour? | The portal colours: left of the gun blue, right of the gun orange, written to `CustomPortalColors`; the item model is not changed |
| D3 | Exit lift | Only when the exit portal is on the floor |
| D4 | Washing in a cauldron | No |
| D5 | Desaturation fix | Compat mixin (`hud_batching` stays on) |

## 6. Order and tests

1. Names (1), hop (3), desaturation (2), dyeing (4): all decided and independent.
2. Build `:creativeCompat:build --offline`, then `:creativeCompat:installToMods` with the game closed.
3. Scratch queue rows (prefix `PG-`), added when the work is built:
   - SQ-PG-01: `/summon` or place gun portals; NBT of each `portalgun:custom_portal` shows
     `adjustPositionAfterTeleport` false on wall-exit portals and true on floor-exit ones, including portals saved
     before the fix.
   - SQ-PG-02: the dye recipe: gun + dye in the column left sets `CustomPortalColors.blue`, right sets `orange`, the
     same column sets both; two dyes on one side average; a side with a custom colour mixes, a default side is
     replaced; other gun NBT kept; two guns or a non-dye item gives no output.
   - CQ-PG-01: look at a gun portal and a breakable mirror with WTHIT: "Portal", "Mirror".
   - CQ-PG-02: gun in hotbar slot 5 with block items and flat items in 1-4 and 6-9: no slot desaturated; the gun looks
     the same as in the inventory screen. Repeat with another GeckoLib item if the pack has one.
   - CQ-PG-03: walk (not jump) through wall-to-wall, wall-to-floor and floor-to-wall portal pairs: no lift on wall
     exits; a floor exit still clears the floor; no `Adjusting Client Player Position` line for wall exits.
   - CQ-PG-04: shoot both portals with a dyed gun: each portal is drawn in its dyed colour; the tooltip lists both
     colours; the command still overrides; JEI shows the examples.
   - CQ-PG-05: mirror creation (flint and steel on glass) still works beside Creative Magic's flint-and-steel portal
     rule.
4. Changelog bullets when built, under a `## Portal Gun` family: names, the two fixes (past tense), dyeing (left of the
   gun sets the blue portal, right the orange; placed portals change when shot again).

Out of scope here (still in the scratchpad): the extra mirror glass types (tinted, Quark framed, dirty, frosted), the
Portal Gun as a computing endgame recipe.

## 7. Built (2026-11-07)

`creativecompat-1.0.0.jar` 15:58 (build/libs; not in `mods/`). The Portal Gun and Immersive Portals jars were copied
into `development/creativeCommon/libs/` (git-ignored) to compile against. ImmediatelyFast is read by class name only.

| # | Files | Notes |
|---|---|---|
| 1 | `kubejs/assets/creativecompat/kubejs/names.js` | 3-argument `e.add` under `portalgun` and `imm_ptl_core` |
| 2 | `mixin/GeckoItemHudBatchMixin` (client), `compat/GeckoHudBatch` | `@Redirect` of `m_109911_` in `GeoItemRenderer#m_108829_`; skips the flush when the source is `BatchingBuffers.ITEM_MODEL_CONSUMER` (reflective; warns once if missing). Gated on `geckolib3` + `immediatelyfast` |
| 3 | `mixin/PortalGunExitLiftMixin`, `portalgun/PortalGunExitLift` | Injects before every `reloadAndSyncToClient()` in `updateState` (both branches; the hidden-portal one is harmless) and at the tail of `m_7378_`. Gated on `portalgun` |
| 4 | `portalgun/PortalGunDyeRecipe`, `PortalGunRecipes`, `PortalGunDyeJEI`, `data/creativecompat/recipes/portal_gun_dye.json` | Reads and writes `CustomPortalColors` NBT directly and names the gun only by id, so nothing loads Portal Gun classes and the serializer registers always; the JSON has a `forge:mod_loaded` condition. NCH has no generic special-recipe entry (only `creativeLabelsSpecial`), so the JSON is a jar data file. Dye averaging is the leather-armour formula on `DyeColor` diffuse colours. JEI: 16 blue + 16 orange one-dye pages, a both-sides page (light blue left, red right) and a same-column page (lime under the gun) |

Still open from this plan: the `item_dict.js` keys (side finding above) wait for the next Creative Common build.
Tests: SQ-PG-01, -02, CQ-PG-01..05 in [scratch_queue.md](testing/scratch_queue.md).
