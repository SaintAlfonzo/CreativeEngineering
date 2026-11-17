# missing-names-catalog

> Names writeup (feature_writeup/names.md, NM-): Display Delight lang, RD crops, and the 10-20 display-name audit (client /cdevdumpnames -> tools/name_audit.py -> name_fixes.json -> client_scripts/name_fixes.js, 436 fixes); lang merges PER NAMESPACE

Moved from agent memory 2026-11-16 (last edited there 2026-10-02). Repo status docs win over any status line here.

Started 2026-10-02. Details live in `feature_writeup/names.md`.

- Display Delight: `tools/gen_display_delight_names.py` names all 205 blocks from their loot-table food and writes `kubejs/assets/displaydelight/lang/en_us.json`. The rules are Placed / Plated / Small Plated + food name. The json is generated, so edit the tool, not the file.
- Rustic Delight: the user's `bell_peppers` / `cotton` keys were correct. Added `coffee` ("Coffee Plant", because `rusticdelight:coffee` is also the drink item) and 3 potted wild plants in namechanges.js.
- `tools/lang_index.py` indexes every en_us key with its source: vanilla jar at `~/.minecraft/versions/1.19.2/1.19.2.jar`, Forge client jar, mods/, KubeJS, enabled zips. Reuse it for any name work.
- The offline scan over-reports. Mods override description ids (Alex's Mobs effects use `alexsmobs.potion.*`; wall variants borrow their item's name), Everycomp writes its lang at runtime, and Lightman's Currency / Dramatic Doors ship blockstates for compat blocks that only register when the other mod is present. `/dumpnames` (dump_names.js) is the source of truth, and it only works in **single player**: a dedicated server lacks client lang.

**Display-name audit (2026-10-20, section 11, TODO ui/names #4):** user asked for every raw key fixed + Title Case on all displayed items/blocks.
- Dump: patcherMod `devtools/DisplayNameDump` (client command `/cdevdumpnames`, or `[debug.names] dumpAtTitle` at the title screen for a scratch client). Writes `kubejs/exported/display_names.json` (every entry's key, shown text, winning pack + namespace) and a `/dumpnames`-format names.json. Pack ids: `Mod Resources`, `Default`, `KubeJS Resource Pack [assets]`, zips as `Name.zip`.
- **Lang merges per namespace, not per pack** (ClientLanguage walks getNamespaces() then each stack): KubeJS's 2-arg `e.add(key, v)` files under `minecraft`; overrides must use `e.add(ns, key, v)` with the namespace that set the key. name_fixes.js does that.
- "Raw" = shown text is a key/bare id; a missing key alone isn't (IE cartridges, armor trims, Everything Copper build names in code).
- name_fixes.json keeps an `audit` section so a dump taken WITH fixes loaded carries them forward (else a rerun would empty it). Proven stable 10-20.
- Rules/keep decisions live in `kubejs/generation_scripts/names/name_rules.json` (Blocky Siege "a cannon" entities stay lowercase: death messages).
- Mod display names: CEConfig `[mod_names]` default list (116 entries, the one place it lives) + `util/ModDisplayNames` writes FML ModInfo.displayName via sun.misc.Unsafe (service layer: no mixin, no reflection access). names.md section 12.
- User's mod-name style (2026-10-20): spaces between words, possessive apostrophes, "&" for and / 'n, Create addons as "Create: xyz" (incl. GBD, "Create: CC Bridge"), no author handles (NoCube, SuperMartijn642), dummmmmmy = "Target Dummy", the user's own mods (patcherMod, backports, Exposure x5, incubation_additions, more_mooblooms, baby_mobs) end in " (Alfonzo)" as their only bracket ("Brewin' & Chewin' Backport (Alfonzo)"). Pretty Pipes keeps its mod name + creative tab (items stay "Item Pipe ..."). I kept *JS names and -Craft brands whole.

**Old status:** `/dumpnames` ran via /dumpall 2026-10-02 (NM-07/08 PASSED); its catalog is now fixed by the audit.

Related: memory `feature-writeup-pattern`, memory `resource-pack-lang-priority`, [rustic-delight-haste-potions](rustic-delight-haste-potions.md).
