# config-driven-recipe-values

> global.NCH.cfg(defaults, path) framework letting a CEConfig toml value shape a whole KubeJS recipe's tuning object, not just on/off toggles

Moved from agent memory 2026-11-16 (last edited there 2026-09-15). Repo status docs win over any status line here.

Framework for driving a config-driven craft's tuning values (potion, mB amount, loop count,
hammer blows, etc.) from CEConfig instead of hardcoding them — built 2026-09-13, test case
was the `felsic_sword` sequenced assembly in [echo-tier-gear](echo-tier-gear.md)/end_gear.js's Felsic section;
extended 2026-09-15 to cover all 9 felsic_gear pieces (helmet/chestplate/leggings/boots/sword/
pickaxe/axe/shovel/hoe), not just the sword.
Went through several iterations before landing here — see the shape below, not earlier
per-field or separate-file versions.

- **Java side**: `CEConfig.Crafting.FelsicPiece` (creativeCommon/.../CEConfig.java) — **one reusable
  nested class**, not one class per piece: constructor takes `(Builder b, String tomlSection,
  String defaultPotion, int defaultMb, int defaultLoops, int defaultBlows)`, instantiated 9x in
  `Crafting`'s constructor (`FELSIC_HELMET`, `FELSIC_CHESTPLATE`, ..., `FELSIC_HOE`), each under
  its own toml section (`CEConfig.CRAFTING.<SECTION>`). Was originally a one-off `FelsicSword`
  class before the other 8 pieces were added - renamed/parameterized rather than copy-pasting
  8 near-identical classes. **Java field names are kept short/internal** (`mb`, `blows`) while
  the **toml key strings** passed to `.define("potionMb", ...)` / `.defineInRange("hammerBlows",
  ...)` stay the descriptive, human-facing labels shown in the toml file and Create's Configs
  GUI — these are deliberately different from each other by request. Requires a Creative Common
  rebuild + jar redeploy to take effect (never overwrite `mods/` while the game is running —
  see [patchermod-coremod](patchermod-coremod.md)).
- **JS side**: `global.NCH.cfg(defaults, path)` (neoCraftHelper.js, next to `bulk()`) — takes
  the **whole defaults object** for a craft (keys must match the CEConfig section's *Java
  field* names exactly, e.g. `potion`/`mb`/`loops`/`blows` - not the toml key strings) plus a
  dotted path to that section (e.g. `'CRAFTING.FELSIC_SWORD'`), walks the path against the
  lazily-loaded `CEConfig` class via property access (works because Rhino exposes public
  static fields as JS properties, same trick as `CFG.DRAGON_GEAR.autorepairEnabled.get()`
  elsewhere), and returns a **new object** with each field replaced by its resolved CEConfig
  value where available — or the given default field-by-field if CEConfig isn't on the
  classpath or a field/section doesn't resolve. **Logs whenever a resolved value differs from
  its default**, so a toml override is visible in the log without diffing the file by hand.
  One call builds the whole tuning object, right where the recipe is defined:
  ```js
  const felsicSword = R.cfg({potion: 'creativeengineering:magic_resistance', mb: 250, loops: 1, blows: 3}, 'CRAFTING.FELSIC_SWORD')
  // then: felsicSword.potion, felsicSword.mb, felsicSword.loops, felsicSword.blows
  ```
- Deliberately **not** a separate file per craft — user rejected that (`kubejs/server_scripts/crafting/felsic_config.js`
  was tried and removed) in favor of keeping the config resolution inline in the same file
  (end_gear.js) where the rest of that item's siblings are defined, right next to the
  `R.bulk('felsicAssembly', ...)` call it feeds into.
- Recipes are baked at datapack-reload time (`ServerEvents.recipes`), so a toml edit needs
  `/reload` to reach an already-generated recipe — CEConfig's own live-reload only refreshes
  the value on disk, not recipes already built from it.
- To add another config-driven craft: add a new nested class under `CEConfig.Crafting`
  (Java, short field names + descriptive toml key strings), rebuild+redeploy the jar, then
  call `R.cfg({...defaults...}, 'CRAFTING.<NEW_SECTION>')` in the relevant server script —
  no other plumbing needed.
- **Create's Configs GUI gap, now fixed**: Create's own `SubMenuConfigScreen` only renders
  `Boolean`/`Enum`/`Number` `ForgeConfigSpec` values natively - a plain `ConfigValue<String>`
  (every `FelsicPiece.potion` field) fell through to an "Impl missing" placeholder row. First
  found 2026-09-15, initially left as toml-and-`/reload`-only by user's call; later that same
  session a real fix was built - see [create-config-string-widget](create-config-string-widget.md) (Creative Common mixin +
  custom widget class) - so `potion` fields now get a proper in-game text field too.
