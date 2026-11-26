# CurseForge project page format

The default layout for every project page in this folder (owner, 2026-11-25). It replaces the W2-6 layout
(plan_wave2_publish.md: one spoiler per feature) and the one-spoiler-per-family layout that followed it. Pages are
Markdown with inline HTML, pasted into CurseForge's Markdown description editor.

## Page skeleton, in order

1. `# <Mod name>` (the title line; CurseForge shows the project name itself, so it is optional when pasting).
2. A bold one-sentence pitch, then a short paragraph: what the mod is, what it changes, what it needs.
3. Optional: `## Mods that require <name>` (library jars only), a plain bullet list.
4. Optional: one or two plain paragraphs on how the features are switched (config file, restart rule, the version
   guard for changes that touch another mod's code, how a mod-specific line is marked).
5. The **feature families** (below), separated by `***`.
6. Reference sections, in this order and in the same family header style: settings (one spoiler with a table),
   Requirements (table), Source, issues and license.

## Families

- A family is a high-level theme a player would look for (Walking and movement, Effects and potions, Bug fixes).
- Header: a large header, bold and underlined: `## <u><strong>Walking and movement</strong></u>`.
- A `***` rule before each family, so families are split apart visually.
- One plain line under the header saying what the family covers.
- A family holds **3 to 8 subfamilies**. A family with a truly large feature set may have more; a theme too small
  for 3 subfamilies is merged into a neighbor.
- Families are themes, not "works alone" versus "needs another mod". A change that needs another mod goes into the
  family of its theme, beside the changes that need no mod.

## Subfamilies

- Header: `### Name` (sentence case), directly followed by one spoiler: `<div class="spoiler"><ul>...</ul></div>`.
  CurseForge spoilers have no title bar, so the h3 is the spoiler's title.
- A subfamily groups similar content and usually holds several feature lines (about 3 to 7). A one-line subfamily
  is merged into its closest neighbor.
- Order inside a family: the subfamily most players care about first; fixes and edge cases last.

## Feature lines

- `<li><strong>Name (Mod version):</strong> description</li>`. The bracket names the other mod and the version
  range tested (`Quark 3.4`, `Create: Gravity 1.2.x`); a line that needs no other mod has no bracket.
- The description says what changes, its default numbers, and the config section or switch last
  (`Section: <code>[corpses]</code>.`).
- Terse, facts only, as in the player changelog (changelog.md 3.6): no flavor, no ids except config keys, tags and
  commands a player or pack author types.
- Command arguments in square brackets: `/solrp reset [players]` (W2-7), no `&lt;` escapes.
- Images go inside the line they show (`<br><img ...>`). A missing screenshot is an HTML comment
  `<!-- screenshot: ... -->` at the spot it belongs.

## Settings, Requirements, Source

- Settings: one spoiler holding a lead paragraph and a `<table>` (Setting, What it does; add Default when the page
  lists defaults).
- Requirements: a Markdown table (Minecraft, Forge, required jars), then one line on optional mods ("see the
  families above"), then the client/server side.
- Source, issues and license: bullet list; the license line names every third-party license included.

## Rules carried from the pack

ISO dates, American spelling, no emoji, no personal data or machine paths (privacy.md), full mod names instead of
the CLAUDE.md shorthand.
