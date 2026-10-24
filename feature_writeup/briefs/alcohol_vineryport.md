# Brief: alcohol / vineryport

Written 2026-10-24 from [todo/TODO.md](../todo/TODO.md) `# alcohol`, Sub `vineryport` (row 26 only; other open rows 15, 22, 25
are older scratchpad rows not briefed here). Layout per [README.md](README.md). Writeups:
[vinery_jei.md](../vinery_jei.md), [alcohol.md](../alcohol.md) (B7.1 JEI), [jei_coverage.md](../jei_coverage.md).

---

## vineryport #26: Shorten Backported Vinery JEI descriptions
Status **Planned** / Work: new jar / lang / id `alcohol/vineryport #26`

### Goal
Shorten the JEI description texts of the Vinery backport items (see `jei/jei-coverage #12`, planting text).

### Now
- The texts are lang keys in `development/backportedVinery/src/main/resources/assets/vinery_backport/lang/en_us.json`
  (lines 19 to about 45): `jei.vinery_backport.grape_stomping.*`, `wine_aging.*` (cellar, cold, warm), and
  `grape_planting.*` (where.bush / vine / stem / lattice, slow, fast, taiga, vine_shears, leaves, support.*, grapes.*),
  drawn by `.../core/compat/jei/port/GrapePlantingCategory.java` (and the Stomping / Aging categories next to it).
- The planting page already uses "slowly" / "quickly" (lines 31-36), the wording `jei/jei-coverage #12` asked for, and
  the row shows only 6 visible lines (it still cuts off at "stop it growing and" per that row; unverified in game).
- `jei/jei-coverage #12` and `alcohol/vineryport #25` (grapes only on soil blocks) touch the same strings.

### Build
1. Rewrite each long line to one line that fits the page width; cut repetition ("grows %s at light level %s or more"
   -> "Grows %s, light %s+"); merge `support.*` into `where.*`.
2. Check the page's text-area height in `GrapePlantingCategory` (it draws a fixed number of lines): if it wraps, make
   it measure the wrapped height instead of a fixed 6.
3. Rebuild `:backportedVinery` and `installToMods`; keep `vinery_jei.md` section 3 in sync.

### Decisions
- Order with `#25`: change the soil text once, after the soil tag is decided (recommend `#25` first, then this pass).

### Tests
- VJ-new (the prefix of vinery_jei.md section 7): the planting, stomping and aging pages show all
  their text with no cut-off at the default GUI scale; the longest line is under the page width.
