# Stepstools

Test prefix **STL-** · Last updated **2026-10-31** · Overall status **BUILT 2026-10-29 (patcherMod 1.1.04 installed 2026-10-31 00:44), scratch server PASSED r1031 (2026-10-31: SQ-STL-01 boot, SQ-STL-02 recipes and tags); STL-01 to STL-16 in game pending** · TODO `cooking/kitchen #17`

## 0. At a glance

| Part | Where | Status |
|---|---|---|
| 16 items `creativeengineering:<colour>_stepstool`, Another Furniture tall stool models | [stepstools.js](../kubejs/startup_scripts/items/stepstools.js), `item_dict.js` | BUILT, needs restart |
| Recipe: Create seat of the colour on top of an oak fence | [stepstool.js](../kubejs/server_scripts/equipment/stepstool.js) | BUILT |
| Tags `#creativeengineering:stepstools`, `#curios:feet` | [curiostags.js](../kubejs/server_scripts/tags/curiostags.js) | BUILT |
| Curios Feet slot (vanilla boots icon) | [feet.json](../kubejs/data/creativeengineering/curios/slots/feet.json), [curios_slots.md](curios_slots.md) | BUILT, needs restart |
| Step up / step off, collision, server checks, sync | patcherMod `stepstool/Stepstool.java` | BUILT |
| Keys, stepping off on input, stool drawn under players | patcherMod `stepstool/client/StepstoolClient.java` | BUILT |
| Stool collision for its owner; no flying kick | `mixin/EntityStepstoolCollisionMixin`, `mixin/ServerGamePacketListenerStepstoolMixin` | BUILT |
| Tooltip (3 lines) | [tooltips.js](../kubejs/client_scripts/tooltips.js) Tools section; [tooltips.md](tooltips.md) | BUILT |

Knobs: `config/creativeengineering-common.toml` `[stepstool]`: `enabled`, `sprintJump` (client), `holdTicks` (client, 5),
`maxDrift` (0.3), `stepOffWhenHurt` (true). Key: Controls > Movement > "Step Up (Stepstool)", unbound by default.

## 1. Overview

A stepstool worn in the Curios Feet slot lets a player stand one block higher. Stand still on the ground and hold
Sprint + Jump (or the Step Up key) for `holdTicks`; the player is lifted onto the stool, drawn under their feet in
its colour. They stay up until they move, crouch, jump or something else knocks them off, then drop the one block.

## 2. Reference

- Step up needs: on the ground, not moving, not crouching, not riding, sleeping, gliding, flying or swimming, a
  stepstool in a Curios slot, and room for the player one block up. Client and server run the same check
  (`Stepstool.canStepUp`); the server decides.
- Server steps off by itself when: drift from the stool centre over `maxDrift`; height off the stool top (below
  -0.05 or above +0.6); any of the step-up conditions breaks (incl. crouch); the worn stool is gone or changed; a
  block's collision fills the space the player stood in; damage (`stepOffWhenHurt`); death, respawn, dimension
  change, logout.
- Client steps off at once (before the server agrees) on movement input, crouch, or a fresh jump press. Holding the
  jump half of the combo after stepping up does not step off; it has to be released and pressed again.
- Examples: under a 2-high ceiling nothing happens (no room). On a carpet the stool stands on the carpet. Next to a
  wall the stool (a whole block centred on the player) sinks into the wall by up to 0.2 of a block.

## 3. What the pack changes

- The stool is a real position change: the player moves up a block (server teleport, rotation kept), so reach,
  hitbox and what others see all follow. The stool itself is a one-block collision box only for its owner, added
  to `Entity#collide`'s entity-collision list on both sides. Other players and mobs pass through it.
- The server's "Flying is not enabled" kick skips a player on a stool (`noBlocksAround` answers false).
- The state is never saved; a crash or restart drops everyone off their stools.
- Drawn in `RenderLevelStageEvent` AFTER_CUTOUT_BLOCKS at the stool's fixed spot, with that block's light, for every
  player on a stool (first person included). Not drawn for a player invisible to the viewer. The block drawn is
  `another_furniture:<colour>_tall_stool` (from the item id), the whole model.
- Sprint + Jump: while a stepstool is worn and the player stands still with room above, holding Sprint blocks the
  jump (so the combo does not jump first). A running jump is unaffected.

## 4. Limits and decisions

- Real move + owner-only collision chosen over a render or eye-height offset: an offset leaves reach, hitbox and
  other players' view at ground height. Collision for every entity rejected: a per-entity lookup on every move.
- Separate stepstool items (owner, 2026-10-29) instead of making the tall stools wearable; models and textures are
  Another Furniture's, nothing copied.
- Stays up until moving/crouching/jumping (owner, 2026-10-29), with a short hold before stepping up. Whole block
  collision and render (owner, 2026-10-29).
- The key: vanilla key mappings take one key plus Ctrl/Shift/Alt, so Sprint + Jump is read from the two vanilla
  keys (`sprintJump`), with a separate rebindable key beside it.
- Toggle Sprint on: Sprint stays "held", so Jump alone while standing still steps up (and does not jump).
- Feet slot icon is vanilla's empty boots slot (`minecraft:item/empty_armor_slot_boots`), no new art.

## 5. File map

- [kubejs/startup_scripts/items/stepstools.js](../kubejs/startup_scripts/items/stepstools.js), `kubejs/startup_scripts/_lib/item_dict.js` (`i.<colour>_stepstool`, `i.t_stepstools`, `i.t_curios_feet`)
- [kubejs/server_scripts/equipment/stepstool.js](../kubejs/server_scripts/equipment/stepstool.js), [kubejs/server_scripts/tags/curiostags.js](../kubejs/server_scripts/tags/curiostags.js)
- [kubejs/data/creativeengineering/curios/slots/feet.json](../kubejs/data/creativeengineering/curios/slots/feet.json), [entities/player_hands_face.json](../kubejs/data/creativeengineering/curios/entities/player_hands_face.json); lang `curios.identifier.feet` in `kubejs/assets/creativeengineering/lang/en_us.json`
- [development/patcherMod/.../stepstool/](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/stepstool/) `Stepstool`, `StepstoolCurios`, `client/StepstoolClient`
- patcherMod `mixin/EntityStepstoolCollisionMixin` (`m_20272_`), `mixin/ServerGamePacketListenerStepstoolMixin` (`m_9793_`); `CEConfig.Stepstool`; key lang in patcherMod `assets/creativeengineering/lang/en_us.json`

## 6. Offline checks

- 2026-10-29: `./gradlew :patcherMod:build --offline` passes. `changelog_lint.py` 0 errors.

## 7. In-game test suite

- [ ] **STL-01 boot:** start the game → no `Mixin apply failed` / `InvalidInjectionException` naming `Stepstool`; `latest.log` no KubeJS errors from `stepstools.js`, `stepstool.js`, `curiostags.js`.
- [ ] **STL-02 items:** JEI shows 16 stepstools, each looking like that colour's tall stool, names "<Colour> Stepstool", tooltip 3 lines.
- [ ] **STL-03 recipe:** red seat above an oak fence gives a Red Stepstool; same for one other colour.
- [ ] **STL-04 slot:** the Curios screen has a Feet slot (boots icon, "Feet"); a stepstool goes in, other items do not.
- [ ] **STL-05 step up:** wear one, stand still, hold Sprint + Jump → after about a quarter second you are a block higher, the stool is drawn under you (first and third person), no jump happens, wood sound.
- [ ] **STL-06 quick press:** tap Sprint + Jump → nothing.
- [ ] **STL-07 stay:** release the keys → you stay up; stand for 10 seconds → no "Flying is not enabled" kick (test on a dedicated server).
- [ ] **STL-08 step off moving:** press W → you drop off and walk; same for A/S/D.
- [ ] **STL-09 step off crouch / jump:** crouch → drop off. Step up again, release, press Jump → drop off, no jump.
- [ ] **STL-10 held jump:** step up and keep holding Jump (let go of Sprint) → you stay up.
- [ ] **STL-11 refusals:** while moving, in the air, crouching, under a 2-high ceiling, without a stepstool worn → nothing happens.
- [ ] **STL-12 server step off:** on the stool: get hit by a mob, unequip the stool, place a block in the space under you → each steps you off; no fall damage.
- [ ] **STL-13 other player:** a second player sees you on the stool in its colour; joins/walks into range while you are up → sees it too.
- [ ] **STL-14 rebind:** bind Step Up to a key, set `sprintJump=false` → only the key steps up; Sprint + Jump jumps normally.
- [ ] **STL-15 relog / dimension:** log out while up, log in → on the ground, no floating stool. Same through a portal.
- [ ] **STL-16 reach:** on the stool you can reach and place one block higher than from the ground.

## 8. History

- 2026-10-29: Investigated (Another Furniture tall stools are one block tall; no Feet slot existed) and built: items,
  recipe, Feet slot, patcherMod stepstool package and two mixins. patcherMod 1.1.04 built, not installed (game running).
- 2026-10-31: patcherMod 1.1.04 installed in `mods/` at 00:44 (stepstool classes in the jar); not yet booted.
