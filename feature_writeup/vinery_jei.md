# Vinery JEI pages

## 0. At a glance

| Part | Status |
|---|---|
| Aging Barrel fermentation page | BUILT (patcherMod 1.0.3), not in `mods/`; **arrow time wrong** (1800 ticks shown, real is about 50, §4) |
| Apple Press page | BUILT, not in `mods/` |
| Grape Stomping page | BUILT, not in `mods/` |
| Wine Aging page | BUILT, not in `mods/` |

- **Files to open:** `development/patcherMod/.../compat/vinery/` (one class per page, `VineryIds` for every id and number).
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
  ([vinery.md §2.3](vinery.md)). Fix the constant and VJ-04's expected text together.
- **Stomping arrow** has no real timer; it animates at the Apple Press pace.
- **Rejected:** installing REI (swaps the viewer for the whole pack and puts our own JEI categories
  behind REI's compat layer); KubeJS info pages only (KubeJS 1902 cannot add JEI categories).
- World interactions (leaf/bush harvesting, stem pruning, kitchen sink) are not covered; KubeJS
  `JEIEvents.information` pages would suit them.

## 5. File map

- [VineryJEI.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/compat/vinery/VineryJEI.java): plugin, registration, catalysts
- [VineryIds.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/compat/vinery/VineryIds.java): ids, Vinery's hardcoded numbers, config read
- [VineryCategory.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/compat/vinery/VineryCategory.java): shared REI-style drawing
- [FermentationCategory.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/compat/vinery/FermentationCategory.java), [WinePressCategory.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/compat/vinery/WinePressCategory.java), [GrapeStompingCategory.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/compat/vinery/GrapeStompingCategory.java), [WineAgingCategory.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/compat/vinery/WineAgingCategory.java)
- Lang: `creativeengineering.jei.vinery.*` in [en_us.json](../development/patcherMod/src/main/resources/assets/creativeengineering/lang/en_us.json)

## 6. Offline checks

- `./gradlew :patcherMod:build --offline` compiles; `javap -c` on the built classes shows every vanilla
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
