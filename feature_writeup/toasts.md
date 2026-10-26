# Toasts: tutorial and advancement toasts filtered pack-wide

The pop-ups in the top-right corner. Tutorial and advancement toasts are dropped for every player by one client
mixin, driven by two CEConfig switches. Every other toast is left alone. Test prefix `TO-`. Tracked in
[TODO](todo/TODO.md) `ui/toasts #1` and `#2`.

## 0. At a glance

| Part | Status |
|---|---|
| `[toasts] tutorial / advancement` + `ToastFilterMixin` | DEPLOYED with patcherMod 1.1.01 (2026-10-22); TO-01 to TO-07 not run |
| Rubidium Extra `tutorial_toast` / `advancement_toast` = `false` | already set in this instance before this feature; player file, never shipped (3.2) |
| Every other toast | on, unchanged (2) |
| In-game tests | TO-01 to TO-07 pending |

- **Files to open:** `development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/ToastFilterMixin.java`,
  `CEConfig.Toasts`.
- **Knobs:** `config/creativeengineering-common.toml` `[toasts] tutorial` and `advancement` (both default `false` =
  hidden). Read for every toast, so a change applies to the next one.
- **How to measure:** the toast doesn't appear, and no toast sound plays.

## 1. Overview

Every toast in the game, vanilla's and the mods', is queued through `ToastComponent.addToast` (`m_94922_`). The mixin
injects at its head and cancels the call when the toast belongs to a family that is switched off. A cancelled toast
never takes one of the five slots and never plays its sound. The code that made it keeps the object and may still
call `hide()` on it, which does nothing for a toast that was never added.

Vanilla classes are matched with `instanceof`. Mod classes are matched by class name, so patcherMod needs no mod jar
to compile and a missing mod is simply no match.

The user asked for this on 2026-10-19 (scratchpad: "disable tutorial toasts", "advancement toasts").

## 2. Reference: every toast in the pack

Found 2026-10-19 by scanning vanilla 1.19.2 and every jar in `mods/` for classes implementing
`net.minecraft.client.gui.components.toasts.Toast`, plus the classes that call vanilla's `SystemToast`.

| Toast | From | When it shows | Pack state |
|---|---|---|---|
| `TutorialToast` | vanilla | New-player steps (move, look, find a tree, punch it, craft planks, open inventory), social interactions on a server, bundles | **hidden** (`tutorial`) |
| `BetterTutorialToast` | Exposure | Camera attachments screen hints (hover, wiki); lightroom "develop film" | **hidden** (`tutorial`); the two attachment hints were also off already in `exposure-client.toml` |
| `ClippyController$Balloon` | Fairy Lights | "Craft hanging lights" once a light item is in the inventory | **hidden** (`tutorial`) |
| `AdvancementToast` | vanilla | Advancement made | **hidden** (`advancement`); none can fire anyway, [advancements.md](advancements.md) |
| `ClientAdvancements$LexiconToast` | Patchouli | Book entry unlocked by an advancement | **hidden** (`advancement`); also dead without advancements |
| `RecipeToast` | vanilla | Recipe unlocked | shown, but effectively never fires: unlocks come from advancements |
| `SystemToast` | vanilla | Narrator toggled (Ctrl+B), world backup, world-gen settings import, pack load / copy failure, world access failure, insecure server, chat preview, periodic notifications | shown |
| `SystemToast` | Advanced Peripherals, MCA, Quests Additions, Quark (automatic recipe unlock), WTHIT (config / theme screens), Yuushya (show block screen), Do a Barrel Roll (config), Item Filters | Short notices from their screens and modules | shown |
| `FinishedJobToast` | AE2 | A long autocrafting job finished (`config/ae2/client.json` `notifyForFinishedCraftingJobs = true`) | shown |
| `ItemToast` | CC: Tweaked | Computer screen notices (e.g. a file uploaded) | shown |
| `RewardToast`, `CustomToast`, `ToastQuestObject` | FTB Quests | Task / quest complete, reward received, quest-defined toasts | shown |
| `SimpleToast` | FTB Library | Generic FTB notices | shown |
| `NotificationToast` | KubeJS | Script notifications | shown |

The user confirmed 2026-10-19 that KubeJS, Do a Barrel Roll, FTB, FTB Quests, WTHIT, CC: Tweaked and AE2 toasts stay
on.

## 3. What the pack changes

### 3.1 The filter

| Switch | Default | Drops |
|---|---|---|
| `tutorial` | `false` | `TutorialToast` and subclasses; Exposure `BetterTutorialToast`; Fairy Lights `ClippyController$Balloon` |
| `advancement` | `false` | `AdvancementToast` and subclasses; Patchouli `ClientAdvancements$LexiconToast` |

`true` lets that family through again. The section is in the COMMON config, so it ships with the pack (functional
file) and the defaults live in code; nothing is needed in `configs_edited/`. A server ignores it.

### 3.2 Why not Rubidium Extra's toggles

Rubidium Extra (Sodium Extra) already has `toasts`, `advancement_toast`, `recipe_toast`, `system_toast` and
`tutorial_toast` in `config/sodium-extra-options.json`, and this instance had the tutorial and advancement ones off.
Two gaps:

- `tools/build_deploy.py` classes that file as `player` (video settings), so a functional deploy never sends it to
  other players.
- Its mixins hook only the four vanilla toast classes (`draw` returns `HIDE`); the mod tutorial toasts are untouched.

Those toggles still work on top of the filter, per player.

## 4. Limits and decisions

- **Two families only.** The rest were reviewed and kept (2). Another family is one set and one switch in the mixin
  and `CEConfig.Toasts`.
- **Class names are hard-coded.** A mod that renames its toast class falls out of the filter silently; TO-03 and
  TO-04 catch it after an Exposure or Fairy Lights update.
- **Tutorial progress isn't advanced.** Vanilla's tutorial still runs its steps (`options.txt` `tutorialStep`), only
  without the toast. Fairy Lights keeps `progress = "none"` in `fairylights-client.toml`. Harmless.
- **Rejected: shipping `sodium-extra-options.json`.** It would overwrite every player's video settings and still
  miss the mod toasts.
- **Rejected: a "Toast Control"-style mod.** One more jar for two switches the mixin covers.

## 5. File map

- [ToastFilterMixin.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/ToastFilterMixin.java)
- [CEConfig.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/CEConfig.java) `Toasts`
- [creativeengineering.mixins.json](../development/patcherMod/src/main/resources/creativeengineering.mixins.json) `client` list
- Related: [advancements.md](advancements.md), Exposure hints in `config/exposure-client.toml`, Rubidium Extra in
  `config/sodium-extra-options.json`

## 6. Offline checks

- `unzip -l mods/creativeengineering-1.0.9.jar | grep ToastFilterMixin`: the class is in the installed jar.
- `md5sum development/patcherMod/build/libs/creativeengineering-1.0.9.jar mods/creativeengineering-1.0.9.jar`: equal
  once installed (build 2026-10-19: `68b6f829...`).
- `grep -A8 "^\[toasts\]" config/creativeengineering-common.toml` after a launch: the section with both switches
  `false`.

## 7. In-game test suite

Before testing, set Rubidium Extra's `tutorial_toast` and `advancement_toast` back to `true` (Video Settings > Extras)
so only the filter is being tested; put them back afterwards if wanted.

- [ ] **TO-01 Config section:** launch → `[toasts]` in `creativeengineering-common.toml` with `tutorial = false`,
  `advancement = false`; no mixin error in `latest.log`.
- [ ] **TO-02 Vanilla tutorial:** set `tutorialStep:movement` in `options.txt`, start a world → no "Move" / "Look
  around" toast; walk, look, punch a tree → no toasts.
- [ ] **TO-03 Exposure hints:** set `attachments_show_info_toast = true` in `exposure-client.toml`, open a camera's
  attachments screen → no hint toast. Develop film in the lightroom → no "develop film" toast.
- [ ] **TO-04 Fairy Lights:** set `progress = "none"` in `fairylights-client.toml`, pick up a light item → no
  "craft hanging lights" balloon.
- [ ] **TO-05 Switch back on:** set `[toasts] tutorial = true`, repeat TO-02 → the movement toast shows; set it back to
  `false`.
- [ ] **TO-06 Other toasts still show:** press Ctrl+B twice (narrator toast), finish a long AE2 autocraft, complete an
  FTB quest task → each toast shows.
- [ ] **TO-07 Dedicated server:** join the updated server → no error; the server log has no `ToastFilterMixin` line
  (client-only mixin).

## 8. History

- 2026-10-19: built at the user's request (TODO `ui/toasts #1`, `#2`): `ToastFilterMixin` + `CEConfig.Toasts`, into
  the patcherMod 1.0.9 jar in `build/libs/` without a version bump. Every toast class in the pack catalogued (2). The
  user kept KubeJS, Do a Barrel Roll, FTB, FTB Quests, WTHIT, CC: Tweaked and AE2 toasts on. TO-01 to TO-07 added,
  none run.
