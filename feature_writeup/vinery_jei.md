# Vinery JEI pages

**Code removed 2026-10-27** ([pack_hygiene.md](pack_hygiene.md) finding 6): patcherMod's `compat/vinery/` plugin, its
lang keys and the 1.3.10 jar in `patcherMod/libs/` are gone. The Vinery port's own JEI pages are the only ones. This
file is kept as history; VJ-01 to VJ-10 can no longer run.

## 0. At a glance

**Numbers below are the retired plugin's (6 grapes, 3 stomps, 2 bottles).** The port's Grape Stomping page follows the pot's current values (12 grapes, `grapevinePotStomps` stomps [4], 4 bottles); its Grape Planting page is documented in [jei_world_tabs.md](jei_world_tabs.md) (JW-16).

**Retired by the Vinery port (2026-10-18, [alcohol.md](alcohol.md) Part B):** when `backportedVinery` is loaded, `VineryJEI` registers nothing; the port ships its own five pages (`vinery:wine_fermentation`, `apple_press_mashing`, `apple_press_fermenting`, `grape_stomping`, `wine_aging`). Everything below describes the 1.3.10 setup and stays true only while 1.3.10 is installed.

| Part | Status |
|---|---|
| Aging Barrel fermentation page | BUILT, in `mods/` (patcherMod 1.0.3); **arrow time wrong** (1800 ticks shown, real is about 50, §4) |
| Apple Press page | BUILT, in `mods/` |
| Grape Stomping page | BUILT, in `mods/` |
| Wine Aging page | BUILT, in `mods/` |

- **Files to open:** `development/creativeCommon/.../compat/vinery/` (one class per page, `VineryIds` for every id and number).
- **Knobs:** none of our own. The aging page reads Vinery's `config/vinery/config.json5` (`year_length_in_days`, `years_per_effect_level`).
- **How to measure:** open any Vinery wine, grape, apple or apple mash in JEI and press R / U.

## 1. Overview

Vinery 1.3.10 (the 1.19.2 line) ships an REI plugin only; Vinery's JEI support starts at 1.4.2,
which is 1.20.1-only. With JEI and no REI, none of Vinery's machine recipes were visible. patcherMod
now adds a `@JeiPlugin` (`VineryJEI`) with four pages:

| Page | Source of the recipes | Catalyst |
|---|---|---|
| Aging Barrel (REI's title) | `vinery:wine_fermentation` recipes from the recipe manager | Aging Barrel |
| Apple Press | hardcoded in `WinePressBlockEntity`: apple -> apple mash, 72 ticks | Apple Press |
| Grape Stomping | hardcoded in `GrapevinePotBlock`: 8 grape -> juice pairs | Grapevine Pot |
| Wine Aging | one page per distinct fermentation output | Aging Barrel |

## 2. Reference

- **Fermentation:** REI layout. 2x2 ingredient grid (unused cells still drawn), the wine bottle the
  barrel always consumes from its slot 0, an arrow that fills over 1800 ticks (hover: 90 seconds),
  large result slot. Datapack or KubeJS `vinery:wine_fermentation` recipes appear automatically.
- **Apple Press:** REI layout. Apple, arrow filling over 72 ticks (hover: 3.6 seconds), apple mash.
- **Grape Stomping:** 6 grapes + 2 wine bottles -> 2 juice, "Jump on the pot 3 times". A full pot is
  6 grapes; 3 landings finish the stomp; each bottle takes 3 grapes' worth. The juice type follows the
  first grape put in (grape slot tooltip).
- **Wine Aging:** Aging Barrel -> wine with NBT `Year` = this year -> the same wine at full age.
  The barrel writes `Year` once when the drink finishes; nothing changes it afterwards. On drinking,
  effect level = `min(5, (currentYear - Year) / years_per_effect_level)`, and a year is
  `year_length_in_days` days (pack: 16 and 4, so +1 level every 64 days, max VI after 320 days).
  The page's stacks carry real `Year` tags computed when the page opens, so Vinery's own
  "Year of Production" and potency lines match.

## 3. What the pack changes

Display only. No Vinery behaviour, recipe or config is changed.

## 4. Limits and decisions

- **No compile dependency on Vinery.** Items and the recipe type are looked up by id, the aging config
  is read reflectively (falls back to 16 / 4). A renamed id drops that page or slot instead of crashing.
  Hardcoded numbers are copied from the 1.3.10 jar and listed in `VineryIds`; re-check on a Vinery update.
- **Aged stack can show a negative year** in a young world (year now minus 20). That is the honest
  `Year` value for "made 20 years ago"; Vinery's tooltip then shows the max potency.
- **Fermentation time is wrong (open, found 2026-10-01).** `VineryIds.FERMENTATION_TICKS = 1800` copies
  `COOKING_TIME_IN_TICKS`, which Vinery never reads. The barrel really runs about 50 ticks per wine
  ([vinery.md §2.3](vinery.md)). Fix the constant and VJ-04's expected text together. Moot since 2026-10-19: with the port in `mods/` these pages stand down, and the port's own page shows no tick count.
- **Stomping arrow** has no real timer; it animates at the Apple Press pace.
- **Rejected:** installing REI (swaps the viewer for the whole pack and puts our own JEI categories
  behind REI's compat layer); KubeJS info pages only (KubeJS 1902 cannot add JEI categories).
- World interactions (leaf/bush harvesting, stem pruning, kitchen sink) are not covered; KubeJS
  `JEIEvents.information` pages would suit them.

### Planned from the scratchpad (2026-10-24)

Brief: [briefs/alcohol_vineryport.md](briefs/alcohol_vineryport.md). Nothing here is built.

- [TODO](todo/TODO.md) `alcohol/vineryport #26`: shorten the JEI description texts of the Backported Vinery items (same trim as the planting text,
  `jei/jei-coverage #12`). The pages in this writeup stand down when the port is in `mods/`; the texts to cut are the port's own lang
  (`development/backportedVinery`). Not looked at yet.

## 5. File map

- [VineryJEI.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/compat/vinery/VineryJEI.java): plugin, registration, catalysts
- [VineryIds.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/compat/vinery/VineryIds.java): ids, Vinery's hardcoded numbers, config read
- [VineryCategory.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/compat/vinery/VineryCategory.java): shared REI-style drawing
- [FermentationCategory.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/compat/vinery/FermentationCategory.java), [WinePressCategory.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/compat/vinery/WinePressCategory.java), [GrapeStompingCategory.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/compat/vinery/GrapeStompingCategory.java), [WineAgingCategory.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/compat/vinery/WineAgingCategory.java)
- Lang: `creativeengineering.jei.vinery.*` in [en_us.json](../development/creativeCommon/src/main/resources/assets/creativecommon/lang/en_us.json)

## 6. Offline checks

- `./gradlew :creativeCommon:build --offline` compiles; `javap -c` on the built classes shows every vanilla
  call remapped to SRG (`Level.m_46468_` getDayTime, `Font.m_92889_` draw, ...). Done 2026-10-01.

## 7. In-game test suite

- [ ] **VJ-01 plugin loads:** start the client, open JEI → four new tabs appear on Vinery items; `logs/latest.log` has no `[CE] Vinery JEI` warnings.
- [ ] **VJ-02 fermentation uses:** R on `vinery:red_wine` → Aging Barrel page with its ingredients in the 2x2 grid, a wine bottle in the lower slot, the wine in the large slot.
- [ ] **VJ-03 fermentation count:** cycle the Aging Barrel tab (click the barrel catalyst) → 29 pages (Vinery's stock count, more if the pack adds recipes).
- [ ] **VJ-04 fermentation arrow:** hover the arrow → "90 seconds"; the arrow fills slowly. *(Expected value is wrong until the 1800 -> 50 fix; see §4.)*
- [ ] **VJ-05 apple press:** R on Apple Mash → apple -> apple mash page; arrow hover "3.6 seconds"; U on Apple also lists it.
- [ ] **VJ-06 grape stomping:** R on any grape juice → 6 matching grapes + 2 wine bottles -> 2 juice, "Jump on the pot 3 times"; grape tooltip mentions the first grape; 8 pages total.
- [ ] **VJ-07 stomping in world:** do it for real: 6 grapes, jump 3 times, 2 bottles → 2 juice, matching the page.
- [ ] **VJ-08 wine aging page:** R or U on a wine → Wine Aging page: barrel, wine with "Year of Production" = current year, aged wine with potency VI; text lines show 4 years / VI / 16 days.
- [ ] **VJ-09 aging arrows:** hover the first arrow → "Aging Barrel writes NBT Year: N" (current year); hover the second → "20 years later (320 days)".
- [ ] **VJ-10 aging matches real wine:** take a wine fresh from a barrel → its tooltip year equals the page's fresh-wine year.

## 8. History

- 2026-10-01: built (patcherMod 1.0.3). Not copied to `mods/` because a game instance was running.
- 2026-10-01: the real barrel time is about 50 ticks, not 1800 (from the decompiled jar); fix open. Full Vinery behaviour reference: [vinery.md](vinery.md).
- 2026-10-02: in `mods/` (patcherMod 1.0.3 copied in, confirmed against the jar contents); not deployed.
- 2026-10-24: planned from the scratchpad: shorter Vinery port JEI descriptions (`alcohol/vineryport #26`). No code changed.
