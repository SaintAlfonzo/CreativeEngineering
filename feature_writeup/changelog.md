# Player changelog (title-screen "What's New")

Test prefix `CL-`. Status: BUILT 2026-10-21 (patcherMod 1.1.01 installed in `mods/`), offline checks pass, no
in-game test yet. Open decisions in 4.1.

## 0. At a glance

| Part | Status | Where it will live |
|---|---|---|
| Changelog source files (one Markdown file per version, plus the working list) | BUILT | `kubejs/assets/creativeengineering/changelog/` (only `unreleased.md` so far) |
| Book button on the title screen and the pause menu, with an update popup | BUILT | patcherMod `changelog/ChangelogButtons.java` (`ScreenEvent.Init.Post`, `ScreenEvent.Render.Post`) |
| Changelog screen (version list left, scrolling text right, Forge Mods-screen layout) | BUILT | patcherMod `changelog/ChangelogScreen.java`, `ChangelogPanel.java` |
| Markdown -> vanilla text (commonmark-java 0.21.0, relocated into the jar) | BUILT | patcherMod `changelog/ChangelogMarkdown.java` |
| Version list + read marker (newest version the player has opened) | BUILT | `changelog/ChangelogStore.java`; `config/creativeengineering/changelog_seen.txt` (player config, git-ignored) |
| Deploy integration (ship `unreleased.md` as `<version>.md`, rename on snapshot, deployed files frozen, lint) | BUILT | `tools/build_deploy.py` (`check_changelog`, `freeze_changelog`), `tools/changelog_lint.py` |
| Writing rules for entries | DESIGN | section 3.6 of this file |
| 1.1.01 changelog (every change since 1.0.9) | DEPLOYED 2026-10-22, frozen as `changelog/1.1.01.md`. 1.1.02 deployed 2026-10-23, frozen as `changelog/1.1.02.md`; `unreleased.md` now holds the 1.1.03 working list. Written 2026-10-21, audited 2026-10-22 (section 8) | `unreleased.md`: one bullet per player-visible change in 9 families, kept true as work lands (re-synced 2026-10-22 against the working tree); lint 0 errors, length warnings only (the lint counts sub-bullets into the 300, every single bullet and sub-bullet is under it). Same-behaviour ports (tier 1, Glowing Illagers, Comfortable Campfires, Bottled Air, Fast Leaf Decay's cascade) have no bullet |
| Historic changelogs 1.0.0 to 1.0.9 (brief) | Planned | TODO `ui/changelog #2` |

Knobs (CEConfig `[changelog]`, client side): `enabled` (true), `titleButton` (true), `pauseButton` (true),
`updatePopup` (true).

How to check: open the title screen, press the book button, read the newest version; F3+T reloads the text
without a restart.

## 1. Overview

Players get a book button on the title screen and on the pause menu. It opens a screen that lists every pack
version on the left (newest first) and shows the selected version's notes on the right: family headings,
bulleted changes with **New / Changed / Fixed / Removed** labels, bold, italics, commands and links. After an
update, a speech bubble like Quark's "Configure Quark Here!" bobs next to the button ("New in 1.1.01!") until
the player opens the changelog once.

On the dev side, `unreleased.md` is the working list for the version being built. A change gets its bullet as soon
as it can affect gameplay in the dev instance (TODO status Testing or later), written for players and ready to
ship as-is, because there may be no editing pass before a deploy. The whole working list is kept true as later
work changes earlier bullets. The deploy build ships that file under the release's version number;
`--snapshot` renames it in the tree and starts a fresh working list. Once a version is deployed, its file never
changes again.

1.1.01 is the first major version jump. Its changelog documents every change since 1.0.9 in full. Versions
1.0.0 to 1.0.9 get brief historic changelogs (TODO `ui/changelog #2`), so players can see how the pack got here.

## 2. Reference: what already exists

### 2.1 Pieces reused (no new mods)

| Need | Existing piece | Why it fits |
|---|---|---|
| Add a button to the title screen and pause menu | Forge `ScreenEvent.Init.Post` + `event.addListener(...)` | patcherMod already listens to client screen events (`devtools/DisplayNameDump.java`); no FancyMenu needed |
| Draw the popup over the menu | Forge `ScreenEvent.Render.Post` | draws after the screen, like Quark's bubble |
| Screen layout | Forge `net.minecraftforge.client.gui.ModListScreen` | a list on the left and an `InfoPanel` on the right that wraps text and opens clicked links; we copy its shape |
| Scrolling text area | Forge `net.minecraftforge.client.gui.widget.ScrollPanel` | scrollbar, mouse wheel and clipping done; `ModListScreen.InfoPanel` extends it |
| Version list | vanilla `ObjectSelectionList` | same as the Forge mod list |
| Styling | vanilla `Component` + `Style` (bold, italic, colour, `ClickEvent.OPEN_URL`), `Font.split` for wrapping | links get vanilla's "open this link?" confirm screen through `Screen.handleComponentClicked` |
| Book icon | vanilla's written book item (`ItemRenderer.renderGuiItem`, 1.19.2 has no `GuiGraphics`) | no new texture; the same call JEI and WTHIT use |
| Markdown parsing | **commonmark-java** (BSD-2-Clause, about 200 KB, no dependencies), shaded into the patcherMod jar | the reference CommonMark parser; handles nesting, escapes and emphasis edge cases a hand parser gets wrong |
| Text files players get without a restart | KubeJS's `kubejs/assets/` resource pack | loaded before the title screen, reloaded by F3+T, already shipped by build_deploy (`KUBEJS_CLASSES`) |

### 2.2 Checked in the jars (2026-10-21)

- No mod in `mods/` bundles a Markdown library (searched every jar for commonmark, flexmark and intellij-markdown).
- FancyMenu is not installed (only its library Konkrete). Adding FancyMenu for one button would bring a whole
  menu editor and its own layout files.
- Quark's Q button (`QButtonHandler`) finds the title-screen button labelled "Mods", "Realms", "Options" or
  "Mod Options" and sits beside it. The book button must not take that spot; see 3.3.
- Quark's popup (`QButton`, `GeneralConfig.enableOnboarding`): a bubble with "Configure Quark Here!"
  (`quark.misc.configure_quark_here`) bobs beside the Q button on a sine of the client tick counter, until the
  Q menu has been opened once; the marker is the file `.qmenu_opened.marker` in the game folder. Ours copies the
  look and the bob, and the marker holds a version instead of being a plain file (3.3). Checked in the bytecode
  while building: the "bob" is a one-pixel step every half second (`ClientTicker.total % 20 > 10`), not a sine,
  and the bubble is 5 + 6 px end pieces from Quark's `general_icons.png` around a 1 px repeated middle. Ours
  redraws those pixels with fills (no Quark texture, no Quark dependency).
- Forge 43.3.9 has `ScrollPanel` at `net/minecraftforge/client/gui/widget/ScrollPanel` (official mappings jar in
  the Gradle cache).

### 2.3 Example of the kind of text the screen shows

For style only; the claims are not checked and the date is made up.

```markdown
# 1.1.01 - The Brewing Update

Released 2026-10-25

Drink food values, wine aging, hunger bars in food screens.

## Brewing & Drinks

- **Changed:** Alcoholic drinks fill half a hunger shank, give no saturation and give 5 seconds of Nausea. Tooltip
  shows *Intoxicating Drink*.
- **New:** Wine ages x2 in the dark, x2 in the cold, x4 in a cold, dark spot.
  - Warm spots: x0.75.

## Interface & Tooltips

- **Fixed:** Hunger and saturation bars missing in the Cooking Pot, the Keg, Vinery's recipe book and other food
  screens.
- **Fixed:** Hundreds of items showed a raw name like `item.foo.bar`.
```

## 3. What the pack will change

### 3.1 Source files

```
kubejs/assets/creativeengineering/changelog/
  unreleased.md     the working list (dev only, never shipped under this name)
  1.1.01.md         one file per released version, frozen once deployed
  1.0.9.md          historic (brief), added with 1.1.01
  ...
  1.0.0.md
```

- The newest version file is the pack's current version as far as the client is concerned; nothing else needs to
  tell the client which version it runs.
- Version order compares dotted numbers segment by segment as integers (`1.1.01` > `1.0.10` > `1.0.9`). The file
  name is what the screen shows, so `1.1.01` keeps its leading zero. Two files with the same numeric version
  (`1.1.01` and `1.1.1`) are a lint error.
- Deployed files are permanent (3.5): no corrections, no notes pointing at newer versions.

### 3.2 File format (the Markdown subset)

| Element | Write | Shows as |
|---|---|---|
| Version title | `# 1.1.01` or `# 1.1.01 - The Brewing Update` (first line) | list entry + big title |
| Release date | `Released 2026-10-25` (second line; build_deploy writes it) | grey line under the title |
| Intro | a plain paragraph before the first `##` (optional, 1 to 3 sentences) | normal text |
| Family | `## Food & Cooking`, optionally `## Food & Cooking <!-- icon: farmersdelight:cooking_pot -->` | heading, with the item's icon when given |
| Change | `- **New:** text` (label one of New, Changed, Fixed, Removed) | bullet; label coloured green / yellow / aqua / red |
| Detail | two-space indent: `  - text` (one level only) | indented smaller bullet |
| Emphasis | `**bold**`, `*italic*` | bold / italic |
| In-game name, command, key | `` `text` `` | gold text (no monospace font in vanilla) |
| Link | `[wiki page](https://...)` | blue underlined, confirm screen on click |
| Divider | `---` | thin line |

Anything else (tables, images, HTML other than the icon comment, deeper nesting) is not supported; the lint (3.5)
rejects it so the in-game view never shows raw syntax. The icon comment is invisible in any other Markdown viewer
too (GitHub, the IDE preview). A missing icon item draws no icon and logs one warning.

**Families are per changelog.** Each version file has its own `##` headings. A heading is created when the first
feature in that family goes into the working list, placed where it reads best (the biggest or most-played change
first). There is no fixed list. These names are suggestions, to keep versions looking alike where they can:

| TODO family | Suggested heading |
|---|---|
| worldgen | World & Exploration |
| farming | Farming & Nature |
| food | Food & Cooking |
| alcohol | Brewing & Drinks |
| mobs | Mobs & Animals |
| gear | Gear & Combat |
| loot | Loot & Trading |
| recipes | Crafting & Recipes |
| engineering | Engineering & Automation |
| blocks | Blocks & Building |
| gameplay | Gameplay |
| ui | Interface & Tooltips |
| mod jars added or removed | Mods Added & Removed |
| crash, lag and desync fixes | Performance & Stability |
| largecontent | whichever family the player would look in (a port of a mod they know usually gets nothing, see 3.6) |
| tooling, pack/docs | never |

A version with one big theme can use its own headings instead (for example `## The Brewin' Port` and
`## Everything Else`).

### 3.3 Book button and update popup

- `ScreenEvent.Init.Post` adds a 20x20 button showing the written book item, tooltip "What's New".
- **Title screen:** right of the Singleplayer button (`width / 2 + 104`, `height / 4 + 48`). Vanilla leaves that
  spot empty and Quark's Q button never uses it (it goes beside Mods / Realms / Options). If any widget already
  overlaps the spot (another mod), try right of Multiplayer, then left of Singleplayer; log which one was used.
- **Pause menu** (`pauseButton`, on by default): right of "Back to Game". Done on the changelog screen returns to
  the pause menu; the game stays paused in single player as for any pause-menu screen.
- **Update popup** (`updatePopup`, on by default): while the newest version is newer than the read marker, a
  Quark-style bubble beside the button says "New in 1.1.01!" (lang key
  `creativeengineering.changelog.popup`) and bobs one pixel up and down every half second (Quark's motion).
  It shows on both the title screen and the pause menu, so a player who joins a server straight from a launcher
  still sees it. The bubble sits on the side that has room and is drawn after the screen (`ScreenEvent.Render.Post`),
  so it never covers the button itself.
- **Acknowledged on first read:** the screen opens on the newest version, and opening it writes that version to
  the read marker. The popup is gone from then on, until the next update ships a newer file. Skipped updates (a
  player jumps from 1.0.7 to 1.1.01) are all acknowledged at once; their versions carry a `*` in the list for that
  visit.
- Fresh installs have no marker, so the popup shows once. That is the intended introduction to the button.

### 3.4 Changelog screen

```
+--------------------------------------------------------------+
|                         What's New                           |
|  +-----------+  +-----------------------------------------+  |
|  | 1.1.01 *  |  | 1.1.01 - The Brewing Update             |  |
|  | 1.0.9     |  | Released 2026-10-25                     |  |
|  | 1.0.8     |  |                                         |  |
|  | 1.0.7     |  | [icon] Brewing & Drinks                 |  |
|  |  ...      |  |  - Changed: Every alcoholic drink ...   |  |
|  |           |  |  - New: Wine ages faster in a cellar    |  |
|  |           |  |      - Warm rooms slow it down ...     |#|
|  +-----------+  +-----------------------------------------+  |
|                          [ Done ]                            |
+--------------------------------------------------------------+
```

- Left: `ObjectSelectionList` of versions, newest first, a `*` on the ones that were unread when the screen opened.
- Right: a `ScrollPanel` subclass. On open (and on window resize) each block becomes wrapped
  `FormattedCharSequence` lines through `Font.split` at the panel width; headings with an icon reserve 18 px for
  it. Clicks find the `Style` under the cursor (`font.getSplitter().componentStyleAtWidth`, as in
  `ModListScreen.InfoPanel`) and pass it to `handleComponentClicked`.
- Dev instance only: when `unreleased.md` has any bullets, the list shows "In development" at the top with that
  file's text. It never counts for the popup. Shipped payloads never contain `unreleased.md`, so players never
  see it.
- Text is loaded from the resource manager on screen open (`listResources("changelog", ...)`), so F3+T picks up
  edits.

### 3.5 Deploy integration, permanence and lint

- `KUBEJS_CLASSES`: `assets/creativeengineering/changelog/unreleased.md` -> dev; `assets/creativeengineering/
  changelog/*.md` -> client (assets are client-side; both classes ship).
- `--version X` staging: if `unreleased.md` has bullets, the payload gets `kubejs/assets/creativeengineering/
  changelog/X.md` = the working list with `# X` and `Released <today>` stamped on top (the tree is not touched,
  so restaging just copies the newer text). No bullets = warning ("this version has no player changelog").
  An existing `X.md` in the tree for a version being staged is an error (one source per version).
- `--snapshot X` (the payload really went out): the tree's `unreleased.md` becomes `X.md` with the same stamp,
  and a fresh `unreleased.md` (title line only) is written. Shipped text and tree text are then identical.
- **Deployed files are frozen.** Preflight compares every changelog file of a deployed version with the sha1 that
  version's `deploy/history/<ver>.json` recorded when it shipped. Any difference is an error, and a deployed
  version's file going missing is an error too. Historic files (1.0.0 to 1.0.9) were never in a payload, so they
  can be written and edited freely until they ship with 1.1.01; after that they are frozen the same way.
- `CONFIG_CLASSES`: `creativeengineering/changelog_seen.txt` -> player, so updates never reset what a player read.
  It is also git-ignored, so the dev instance's own marker never ships (not even with `--config full`).
- `--snapshot` when the working list changed after staging: the shipped text still goes into the tree (from
  `deploy/latest/`, or rebuilt from the working list and the history date), the working list is left alone and
  the script says to remove the shipped bullets by hand.
- `tools/changelog_lint.py` (run by build_deploy preflight, also on its own): title and date lines in place; every
  `##` has at least one bullet and is unique in its file; every top-level bullet has a label; one nesting level;
  icon comments name a well-formed id; no emoji; no unsupported Markdown; no dev words (regex list: `patcherMod`,
  `KubeJS`, `mixin`, `CEConfig`, `\.js\b`, `\.java\b`, `\.json\b`, `TODO`, test ids `\b[A-Z]{2}-\d{2,3}\b`, TODO
  ids `\w+/\w+ #\d+`, `namespace:id` outside icon comments); bullets over 300 characters warn. Errors stop the
  build; warnings print.

### 3.6 Writing rules (the part that matters most)

**When to write.** The same session that gets a change to the point where it can affect play in the dev instance
(its TODO row reaches Testing). Not when it is planned, and not "later". If the change belongs to a family the
working list has no heading for yet, add the heading then.

**Keep the working list true.** Every later change during the same development cycle updates whatever it touches
in `unreleased.md`: rewrite a bullet when the feature changes again, merge bullets that became one change, delete
a bullet if its change was pulled. The list should read at every moment as if it were final.

**Deployed versions are permanent.** Never edit a version file after it ships, not even to point at a newer
version. If something from an older version changes, it gets its own bullet in the working list, written so it
stands on its own ("**Changed:** Wine now ages in any cold room, not only in a cellar.").

**Who it is for.** A player who knows the game and this pack's mods but not its code, configs or history. They
read it to learn what changed. It is a change list, not a guide or a pitch.

**How a bullet reads.** (User rule 2026-10-21: efficient and clear, no flavor words, say what changed, not how to
enjoy it.)
- State the change and nothing else: what it is, where, the numbers. Old value in brackets when one changed
  ("4 Corn Kernels (was 2)").
- No benefit or selling clauses ("so you can...", "makes your catch go twice as far", "drinking is for fun"), no
  tips, no reassurance, no adjectives that carry no fact ("big", "a pile of", "proper", "nice").
- Controls and recipes are facts: give them when a new feature needs them, tersely ("press `B` to pick it up").
- One change per bullet, as short as stays clear; fragments and colons are fine ("Small: all racks. Large: Single
  Wine Rack."). Detail lists go in sub-bullets.
- Use in-game names exactly as the game shows them. Never registry ids, file names, config keys, mod internals,
  test ids or TODO ids.
- Fixes state the bug in past tense ("The Oven accepted no fuel."), adding the new behaviour only if it is not
  obvious from the bug.
- Intro: one plain line of scope ("All changes since 1.0.9."), no summary of highlights.
- Commands only when players can run them, in backticks. Settings only if players can change them (client
  settings, keybinds).
- No emoji.

**What gets no bullet.**
- Tooling, generators, writeups, deploy changes, refactors, dev-only commands.
- Ports of a mod into the pack that behave the same as before (Glowing Illagers, Comfortable Campfires and Bottled
  Air in 1.1.01 changed nothing players can notice). A port that changes behaviour gets a bullet about the
  behaviour change only.
- Performance work players won't notice. Noticeable gains get one bullet under a performance heading ("Large
  villages generate with fewer lag spikes").

**Labels.** New = something that wasn't there. Changed = something works differently (including balance). Fixed = a
bug players could hit. Removed = something is gone (say what replaces it, if anything).

**Historic changelogs (1.0.0 to 1.0.9).** Same format and rules, but brief: an intro line and a handful of bullets
for what a player would have noticed (new mods, big systems, notable fixes), not every change. Source: the plan
docs (`plan_1.0.3.md` to `plan_1.0.9.md`), `deploy/DEPLOY_LOG.md` and `deploy/history/*.json`. Date line = the
deployed date from the log. 1.0.5 was never deployed (folded into 1.0.6), so it gets no file.

**Bad and good, side by side.**

| Dev-style or flavored (don't ship) | Player-style |
|---|---|
| Added `IntoxicatingDrinkTooltip`; Quark saturation word replaced via `[alcohol] quarkLine` | **Changed:** Alcoholic drinks show *Intoxicating Drink* in their tooltip (was "Disgusting Snack"). |
| Copper Drops loot table override (1 raw copper) | **Changed:** Copper ore drops 1 Raw Copper instead of 2 to 5. |
| Fixed SlotItemHandler.mayPickup on empty oven slots | **Fixed:** The oven's [+] button in JEI did nothing. |
| Display-name audit: 436 fixes, 116 mod names | **Fixed:** Hundreds of items, blocks and effects showed raw names (like `item.foo.bar`) or wrong capitalisation. |
| Sliced meat now fills a can at the same count, so cutting first makes your catch go twice as far! | **Changed:** Sliced meat fills a can at the same count as the whole item. |

(Check each claim against the feature's writeup before writing it; the examples above are for style, not facts.)

### 3.7 Code layout (planned)

`development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/changelog/`:

| Class | Job |
|---|---|
| `ChangelogButtons` | `@Mod.EventBusSubscriber(value = Dist.CLIENT)`: `ScreenEvent.Init.Post` for TitleScreen / PauseScreen (button, spot picking), `ScreenEvent.Render.Post` (popup bubble) |
| `ChangelogStore` | loads version files from the resource manager, sorts versions, read marker |
| `ChangelogMarkdown` | commonmark AST -> list of blocks (title, date, paragraph, heading + optional icon, bullet + depth, rule) holding `Component`s |
| `ChangelogScreen` | version list + panel + Done, like `ModListScreen`; returns to the screen it was opened from |
| `ChangelogPanel` | `ScrollPanel` subclass: wrap, draw, icons, clicks |

Build (as built): the Shadow plugin is applied with `apply false`, only for its task type. `relocateCommonmark`
(a `ShadowJar` task over a `shade` configuration) writes `build/shade/shade-relocated.jar` with `org.commonmark`
moved to `com.SaintAlfonzo.creativeengineering.shadow.commonmark`. The source compiles against that jar
(`compileOnly`) and imports the relocated package; the `jar` task copies its classes in. So `jar -> reobfJar ->
archiveJar / installToMods` is unchanged (reobf leaves the library classes alone), and nothing in build-logic
changed. The jar gains 170 classes (about 370 KB) and `creativeengineering-NOTICE-commonmark.txt` (BSD-2-Clause).
Rebuilds stay byte-identical. Dev runs (`runClient`) would not see the library (not on the run classpath); patcherMod
is not run in dev.

## 4. Limits and decisions

- **Text lives in `kubejs/assets`, not in the patcherMod jar.** Rejected: text in the jar (a rebuild for every
  bullet, and the jar version would gate the text). Rejected: a new top-level `changelog/` folder in the instance
  (a new build_deploy category, and no F3+T reload).
- **Own screen, not Patchouli.** Patchouli is installed and has formatted books, but entries are JSON page by
  page in its own `$(l)` syntax, the page layout is small, and its book screen expects a world (locks and
  bookmarks look at the player). Rejected for authoring cost and the title-screen risk.
- **commonmark-java, not a hand parser.** Uses an existing library as asked; the shading cost is a one-time build
  change. A hand parser stays the fallback if shading fails (the subset in 3.2 is line-based, about 150 lines).
- **Not FancyMenu.** A full menu editor for one button, plus layout files to keep in step with other mods.
- **Popup bubble, not an automatic screen.** The bubble asks for attention the way Quark's does; popping the
  whole changelog open on start would get in the way of players who just want to play.
- **Families per changelog, not a fixed list** (your call, 2026-10-21). Suggested names keep versions alike; the
  lint checks shape, not names.
- **Deployed changelogs are permanent** (your call, 2026-10-21). Enforced by the history sha1 check, so a slip is
  a build error rather than a silent edit.
- **The working list is shipped as written.** The lint is the only gate, so the writing rules in 3.6 apply at the
  time of writing, not at release.

### 4.1 Open decisions

- Decided 2026-10-21: next version 1.1.01 (documents every change since 1.0.9); historic changelogs, brief (TODO
  `ui/changelog #2`); families per changelog; book icon; Quark-style update popup, acknowledged on first read;
  pause-menu button on.
- D6: bubble text. Built with the proposed "New in 1.1.01!" (lang key `creativeengineering.changelog.popup`, `%s` =
  version); alternatives "The pack was updated!" or "See what's new!" are a one-line lang change.

## 5. File map

- [../kubejs/assets/creativeengineering/changelog/](../kubejs/assets/creativeengineering/changelog/)
- [../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/changelog/](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/changelog/)
- [../development/patcherMod/build.gradle](../development/patcherMod/build.gradle) (`shade`, `relocateCommonmark`), [../development/patcherMod/NOTICE-commonmark.txt](../development/patcherMod/NOTICE-commonmark.txt)
- [../tools/build_deploy.py](../tools/build_deploy.py) (`KUBEJS_CLASSES`, `CONFIG_CLASSES`, `check_changelog`, `freeze_changelog`)
- [../tools/changelog_lint.py](../tools/changelog_lint.py)
- [../tools/test_changelog.sh](../tools/test_changelog.sh), [../tools/changelog_test/](../tools/changelog_test/)
- [deploy.md](deploy.md) ("Player changelog step")
- [todo/TODO.md](todo/TODO.md) `ui/changelog #1`, `#2`

## 6. Offline checks (2026-10-21, all pass)

`bash tools/test_changelog.sh` (build the patcherMod classes first) runs:
- `ChangelogTest.java` (headless, real patcherMod classes + the relocated commonmark): version ordering (`1.0.10`
  after `1.0.9`, `1.1.01` after `1.0.10`, `1.1.01` equal to `1.1.1`), bullet detection, and Markdown -> blocks on
  the 2.3 example: kinds, title / date, label colours, bold label, italic, gold backticks, link click action
  (http/https only), sub-bullet depth, icon id, hidden HTML comment. Then every file in the changelog folder
  parses with no raw Markdown left.
- `changelog_lint.py --self-test` (24 cases: every error rule fails for its own reason, the long-bullet warning,
  the version clash, the stamp) and the lint on the real folder: 0 errors.
- `changelog_test/deploy_test.py` (scratch folder, real build_deploy functions): staging stamps `X.md` without
  touching the tree; `--snapshot` writes the shipped text and starts a fresh working list, twice is a no-op;
  editing or deleting a deployed file is an error; a tree `X.md` for the version being built is an error;
  snapshot with the payload gone rebuilds the same bytes; working list edited after staging is kept.

Also: `build_deploy.py --version 1.1.01 --dry-run` prints `changelog: .../1.1.01.md from unreleased.md` and the
payload lists `changelog/1.1.01.md`, not `unreleased.md`. The jar has the relocated classes, one manifest, the
NOTICE, and the MC overrides reobfuscated (`m_6303_`, `m_5646_`, ...).

## 7. In-game test suite (planned)

- [ ] **CL-01 button:** open the title screen → a book button right of Singleplayer, no overlap with Quark's Q
  button or any other button; tooltip "What's New".
- [ ] **CL-02 screen:** press it → version list newest first, the newest selected, text wraps inside the panel,
  scrollbar works with the wheel and by dragging.
- [ ] **CL-03 formatting:** family headings show their icons where given; New / Changed / Fixed / Removed labels
  coloured; bold, italic and backtick text styled; sub-bullets indented; no raw Markdown visible.
- [ ] **CL-04 links:** click a link → vanilla confirm screen; Yes opens the browser, No returns to the changelog.
- [ ] **CL-05 popup:** with a read marker older than the newest version → the bubble bobs beside the book button
  on the title screen; open the changelog, go back → bubble gone; restart → still gone.
- [ ] **CL-06 update brings it back:** apply an update payload → the bubble shows again with the new version's
  number; versions skipped since the marker carry a `*` in the list on that first visit.
- [ ] **CL-07 reload:** edit a bullet in the dev instance, F3+T, reopen → new text.
- [ ] **CL-08 resize:** change the window size and GUI scale with the screen open → text rewraps, nothing cut off;
  the bubble stays on screen.
- [ ] **CL-09 in development:** dev instance with bullets in `unreleased.md` → "In development" at the top, no
  popup for it; a payload staged with `--version X` has `X.md` with the date and no `unreleased.md`.
- [ ] **CL-10 pause menu:** in a world, Esc → the book button right of "Back to Game", the bubble too if unread;
  Done returns to the pause menu.
- [ ] **CL-11 server:** dedicated server boots with the jar (no client classes loaded server-side). Scratch server
  2026-10-21: Done in 10.8 s, `[changelog]` written to the toml, no log line from the changelog classes. Two more
  `PoseStack ... invalid dist` lines than a boot with the previous 1.1.01 dev jar (19 vs 17), both in the KubeJS load
  phase like the rest, not traced (that jar also lacks other uncommitted changes); worth a look on the real server.
- [ ] **CL-12 fresh install:** delete `config/creativeengineering/changelog_seen.txt` → the bubble shows once.

## 8. History

- 2026-10-21: designed (first pass: fixed families list, notes on old entries).
- 2026-10-21: revised with your answers: deployed changelogs permanent (history sha1 check), next version 1.1.01
  documents everything since 1.0.9, brief historic changelogs planned, families per changelog with an optional
  icon comment, book icon, Quark-style update popup acknowledged on first read, pause-menu button on by default.
  Nothing built.
- 2026-10-21: built. patcherMod `changelog/` (5 classes), CEConfig `[changelog]`, 5 lang keys, commonmark 0.21.0
  relocated into the jar, `changelog_lint.py`, build_deploy staging / snapshot / frozen check, `test_changelog.sh`.
  Working list started with this feature's own bullet. Changes from the design: the bob is Quark's one-pixel
  step (the design said sine), changelog files are KUBEJS class `client` (said runtime), the library is
  relocated before compiling instead of shadowing the whole jar, the read marker is git-ignored. Jar installed in
  `mods/` (md5 0dfc9245).
- 2026-10-22: audited `unreleased.md` against everything since 1.0.9 (dry-run change list, git diff from bb67d68 of
  kubejs, development/ and config, Testing rows in TODO.md). Fixed the Mortar corn yield (2 Maize = 3 Cornmeal).
  Added: baking overhaul (bases, frosting, cookie dough, oven-only pies and cookies), pie fillings in the pot,
  hand-only can servings, Canned Dough, Shulker and White Strawberry can hunger, salt yields, JEI Seasoning tab,
  fish frying, gold foods, Dog Food, tortilla, wild corn, the other hand-craft meals, Squid Ink Pasta mixing,
  Sinigang, more hand-only recipes, drink stack sizes, wine drink sound, Keg leftover overflow, more sticky note
  and label rules, baby mob XP and spawn eggs, WTHIT honey, anchor and sea pickle lines. Lint 0 errors.
  The four new server configs got bullets after the user kept them: Spice of Life (10 foods to activate, reset on
  death, 256 remembered; tier bonuses still WIP, no bullet), Create schematics 256, Netherite Backpack 8 upgrade
  slots and Looting Upgrade +5% per level, fishing experience x2 / x3 / x5.
- 2026-10-22 (doc sweep against the code): every 2026-10-22 feature has a bullet. Fixed: hand-only meals line
  (no Create recipe makes Pie Crust since the baking overhaul; Scrambled Eggs, Vegetable Omelet, Quiche and Frosting
  have Create routes through the new fluids), Dragon Bow / Crossbow lines (hits not only arrows, old value wording,
  "bleeding" is Exsanguination II), sacks "place 1" sub-bullet removed (Supplementaries' own behaviour), undo refusal
  covers every block with stored data, camera bag flashes named. Added: the `/undo_excavation` logout crash fix.
  Conditional: if extra stem fruit keeps ignoring seasons (TODO `farming/crops #12`), its bullet needs "Extra fruit
  grows in any season."
