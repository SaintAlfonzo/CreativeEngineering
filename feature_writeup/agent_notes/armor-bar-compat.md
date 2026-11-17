# armor-bar-compat

> DetailArmorBar recolor()/texture() via public API; extra armor rows past 20 points

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

Merged 2026-10-19 from 2 memories (index compaction).

## detailarmorbar-compat

DetailArmorBar (modid `detailab`, jar in `mods/`, client-side only) ships a real public
extension point, `com.redlimerl.detailab.api.DetailArmorBarAPI` — decompiled with CFR (jar
copied to `creativeCommon/libs/DetailArmorBar-2.6.4+1.19.2-forge.jar`, compileOnly) to confirm this
before building against it. Its `ArmorBarRenderManager` color arg is a straight RGBA tint
(`RenderSystem#setShaderColor` under the draw call) — any `java.awt.Color` works, it's not a
fixed palette. Proof: DetailArmorBar's own leather-armor entry reads the dye color off the
ItemStack and tints its neutral gray icon with it. One quirk: alpha divides by 100, not 255 —
values above ~100 just clamp to fully opaque.

`DetailArmorBarCompat.recolor()` tints the plain IRON icon (sheet coords x=63 full / x=54 half
in the 128x128 `armor_bar.png` — the same offset `CustomArmorBar.DEFAULT` itself falls back to
for unregistered armor), not leather's own slot (x=117/108) — switched 2026-09-17 on request,
since leather's icon carries an "unranked" connotation iron's plain/default one doesn't. Full
tile-by-tile mapping of the sheet (visually confirmed via ImageMagick crops at y=9, THEN verified
authoritative by resolving each SRG field against Creative Common's own `build/createMcpToSrg/output.tsrg`
— e.g. `f_42480_` = `Items.NETHERITE_HELMET` — rather than trusting the visual guess alone): x=9
netherite (dark/black), x=27 diamond (cyan), x=45 turtle (green, single-item group), x=63 iron
(white/silver, = DEFAULT), x=81 chainmail (gray checkered), x=99 gold (yellow), x=117 leather
(plain white/grey, dynamically tinted from dye color). `NETHERITE_ICON_X/HALF_X` constants reuse
x=9/x=0 untinted for "matches real netherite" cases (Create's netherite diving gear, below).

Built `creativeCommon/.../compat/DetailArmorBarCompat.java` (parallels `IECompat`'s style/package)
with two entry points, called from `CreativeEngineering.ClientModEvents#onClientSetup`
(guarded by `ModList.get().isLoaded("detailab")`, same optional-dependency pattern as
`create_deep_dark`/`easyelytratakeoff` in mods.toml — `detailab` entry added there too,
`side="CLIENT"`):
- `recolor(Color, String... itemIds)` — neutral icon + custom tint, zero new art.
- `texture(ResourceLocation|null, TextureOffset full, TextureOffset half, Color, String...)` —
  custom icon coords; `null` texture reuses DetailArmorBar's own sheet (pick one of its
  existing baked-in material icons at a different offset before bespoke art exists).
- `texture(...)` full-control overload adds textureWidth/height + explicit outline/outlineHalf
  offsets, needed once a genuinely custom sheet doesn't share DAB's own outline-icon
  coordinates (the low-durability/mending flash overlay draws from the *same* texture as the
  main icon, at whatever outline offset was registered).

`register()` now has 3 real `recolor()` calls, all texture-reuse (default sheet's neutral icon,
no new art), colors sampled from each mod's own item texture with ImageMagick histograms:
- `felsic_gear:felsic_{helmet,chestplate,leggings,boots}` -> `0xB89B6A` khaki/tan (felsic_chestplate.png averages ~#585639)
- `create_deep_dark:echo_armor_{helmet,chestplate,leggings,boots}` -> `0x17A8B8` teal (echo_shard_chestplate.png averages ~#0D626C)
- `dragonloot:dragon_{chestplate,leggings,boots}` + `dragonloot:upgraded_dragon_chestplate` -> `0x9B30FF` Ender-purple (its own texture is near-black/grey and unreadable against the dark HUD, so this leans into End/Ender Dragon branding instead of sampling the item texture directly)
- `dragonloot:dragon_helmet` deliberately pulled OUT of that recolor (2026-09-17, on request) as a
  worked `texture()` example: same plain-iron coordinates `recolor()` itself uses (x=63/54) but
  untinted `Color.WHITE`, i.e. renders exactly like `CustomArmorBar.DEFAULT`'s own fallback —
  a template to swap in a real custom `ResourceLocation` from later, not a recolor variant
- Everything Copper armor, 4 oxidation stages × helmet/chestplate/leggings/boots = 16 real items
  (`everythingcopper:copper_*`, `exposed_copper_*`, `weathered_copper_*`, `oxidized_copper_*`),
  each tinted from that stage's own chestplate texture: `0xC26B4C`/`0xA87762`/`0x66A977`/`0x53A178`.
  **"Waxed" copper armor pieces are NOT real registered items** in 2.3.4 despite lang keys existing
  for them (`item.everythingcopper.waxed_unaffected_copper_boots` etc.) — confirmed by checking
  for their model jsons (72 other "waxed" item models exist, zero for armor) and recipes (zero) —
  likely unused/leftover translation strings. Don't add them; `resolveArmor` would just warn+skip
  anyway but they're not worth listing.
- Create diving equipment (`content.equipment.armor.BaseArmorItem extends ArmorItem`, confirmed
  via javap): `create:copper_backtank`/`copper_diving_helmet`/`copper_diving_boots` reuse the
  exact same `0xC26B4C` as Everything Copper's base tier (shared `Color copperBase` local var in
  `register()`, so they can't silently drift apart); `create:netherite_backtank`/
  `netherite_diving_helmet`/`netherite_diving_boots` use the real netherite icon offset (untinted
  `texture()` call) instead of a recolor, so they match vanilla netherite armor exactly.
  `create:goggles` extends plain `Item` (confirmed via javap), not `ArmorItem` — Create's Goggles
  aren't worn in an armor slot in 0.5.1.i, so there's no armor-bar entry possible for it; excluded
  on purpose, not an oversight. No diving leggings exist (helmet/boots + backtank in chest slot).

Still open for later: a genuinely custom-texture entry (the 3-overload `texture()` full-control
path) for any set that wants its own bespoke icon shape instead of the shared neutral one -
memory `centralize-recipes-tags-items` style, add calls to `register()` one line at a time.

2026-09-17: switched felsic/echo/dragon (the "endgame gear" tiers) off `recolor()`/the old
dragon-only bespoke sheet, and added Botania (manasteel/elementium/terrasteel) + Immersive Armors
(bone/slime/wither) - 9 sets total, each its own hand-painted `<name>_tint.png` (user staged them
loose at `kubejs/` root; real destination is
`creativeCommon/src/main/resources/assets/creativeengineering/textures/gui/`, matching the pre-existing
`dragon_armor_bar.png` convention - bespoke GUI sheets are Creative Common resources, not KubeJS-managed
item/block textures). Added a shared `tintedArmor(ResourceLocation, String...)` helper + `TINT_*`
constants instead of one-off per-set offsets.
- **The user's art was 18x9 (half+full icon, 9x9 each, tightly packed) with no blank tile for the
  outline overlay.** Traced `CustomArmorBar#drawOutLine` via `javap` on the decompiled 2.6.4 jar
  (no CFR available this session, only old `.class` files under `DecompiledMods/`) to confirm it's
  called **unconditionally every frame** per equipped piece (not just on a rare mending flash), and
  reads a fixed 9x9 tile from `getTextureOffsetOutline()/OutlineHalf()` on the *same* sheet/dims as
  the icon - so pointing it at the half-icon's own (0,0) (which worked for the padded dragon sheet)
  would have sampled real glyph pixels here and ghosted during the flash. Fix: padded all 9 canvases
  from 18x9 to 27x9 (transparent `convert -size 27x9 xc:none original.png -geometry +0+0 -composite`)
  and pointed outline/outlineHalf at the new blank tile (18,0) - generalizes the exact
  "guaranteed-transparent corner" trick the dragon sheet used, into one shared `TINT_NO_OUTLINE`.
- Item ids confirmed against each mod's own lang file (jar unzipped to scratch dir) rather than
  guessed: `botania:manasteel_helmet/chestplate/leggings/boots` (+ elementium/terrasteel, same
  pattern), `immersive_armors:bone_helmet/chestplate/leggings/boots` (+ slime/wither, same pattern).
- Old `dragon_armor_bar.png` (20x16, the original one-off bespoke sheet) deleted along with its
  now-unused `DRAGON_SHEET_WIDTH/HEIGHT`/`DRAGON_ICON_FULL/HALF`/`DRAGON_NO_OUTLINE` constants -
  confirmed zero other references first (`grep -rn` across `creativeCommon/src` and `kubejs`).
- This session's crash-debugging also reconfirmed [patchermod-coremod](patchermod-coremod.md)'s "never overwrite `mods/`
  while the game is running" rule end-to-end: checked `pgrep` for a live Minecraft process (none)
  before `cp build/libs/... ../mods/...`, then verified by `md5sum` - same discipline that would
  have prevented that crash in the first place.

2026-09-17 (same day, follow-up): added `recolor()` for immersive_armors' 7 remaining sets
(bone/slime/wither already had bespoke `tintedArmor()` art above). **2 of the 7 turned out to be
genuinely dyeable, not fixed-color** - `divine`/`robe` are built via the mod's own
`Items.registerDyeableSet(...)` (confirmed by `javap`-ing `immersive_armors/Items.class`:
`DyeableExtendedArmorItem` instantiations vs. plain `ExtendedArmorItem` for the other 5), and
`DyeableExtendedArmorItem implements DyeableLeatherItem` directly - so they went through
`dyedTexture()` (reusing the vanilla leather icon offset, dynamic per-stack color) instead of a
fixed `recolor()`, exactly like `creategoggles:leather_backtank`. A flat recolor would have been
visibly wrong the moment someone actually dyed a piece. The other 5 (heavy/prismarine/steampunk/
warrior/wooden) are plain `ExtendedArmorItem` - real `recolor()`, colors sampled via a weighted
average of only the fully-opaque pixels in each set's own `*_chestplate.png` histogram (plain
`-resize 1x1` skews toward transparent-padding artifacts; filtering to `alpha==255` rows first and
weighting by count gives a much more representative "material color" than either an unfiltered
average or eyeballing the top single histogram entry, which tends to just be the outline black).

2026-09-17 (same day, second follow-up): one more bespoke set the user had missed -
`botania:manaweave_helmet/chestplate/leggings/boots` (Botania's 4th armor set, the mana-cost-reduction
robes - confirmed real via lang keys + `models/item/manaweave_*.json`, distinct from
manasteel/elementium/terrasteel above). Staged this time at `kubejs/assets/manaweave_tint.png`
(not loose at `kubejs/` root like the original 9) - same destination and pad-to-27x9 treatment
regardless of where the user drops the original; the real home is always
`creativeCommon/src/main/resources/assets/creativeengineering/textures/gui/`. `MANAWEAVE_ARMOR_BAR` +
one `tintedArmor()` call, same pattern as every other bespoke set in this file.

2026-09-17 (third follow-up): added `creategoggles`'s "Goggle Helmet" family - confirmed via
`javap -p -c` on `com/robocraft999/creategoggles/registry/CGItems.class` that each one is built
directly on a REAL vanilla `ArmorMaterials` value (not its own material), same 1:1-reuse pattern
as the backtanks already in this file: `goggle_chainmail_helmet`->CHAIN, `goggle_diamond_helmet`->
DIAMOND, `goggle_golden_helmet`->GOLD, `goggle_turtle_helmet`->TURTLE (new `TURTLE_ICON_X=45`/
`TURTLE_ICON_HALF_X=36` constants, straight off this file's own documented tile map - turtle was
listed there but never had constants until now), `goggle_netherite_helmet`->NETHERITE,
`goggle_leather_helmet`->`DyableGoggleHelmet implements DyeableLeatherItem` (dynamic dye path, not
fixed). `goggle_iron_helmet` needs no entry (IRON, matches the DEFAULT fallback already).
`goggle_diving_helmet`/`goggle_netherite_diving_helmet` are `DivingGoggleHelmet` on Create's own
`AllArmorMaterials.COPPER` / vanilla NETHERITE respectively (mirrors `create:copper_diving_helmet`/
`create:netherite_diving_helmet` already registered) - reused `copperBase` and the netherite
texture() call rather than inventing new colors.

Also asked, same message: "any similar vanilla-armor-type modded items" pack-wide (~300+ mods) -
dispatched an Explore agent (read-only, background) to scan for OTHER mods whose armor is built
directly on a real vanilla `ArmorMaterials` value the same way. **Result: none found - coverage of
this category is now confirmed complete.** Method was exhaustive, not a sample: grepped every one
of the ~330 mod jars' raw bytes for the constant-pool string `ArmorMaterials` (any bytecode
reference to the vanilla enum must contain it) - only 11 jars matched at all (CodeChickenLib,
create, creategoggles, DetailArmorBar, geckolib-forge, jei, kubejs-forge, letsdo-vinery,
minecolonies, Quark, worldedit-mod), then `javap`-traced each candidate's `ArmorItem`-family
constructors back to their actual material arg. Everything else either builds its own distinct
custom `ArmorMaterial` (dustrial_decor cardboard, vinery vinemaker, creategbd beam reactor,
alexsmobs `AMArmorMaterial`, wardrobe/warbrode/essence + create_things_and_misc's MCreator-generated
anonymous-class armor, create_dd `MinerArmorMaterial`, minecolonies `MineColoniesArmorMaterial`,
pneumaticcraft `CompressedIronArmorMaterial`/`PNEUMATIC_ARMOR_MATERIAL` despite the "iron" name,
immersiveengineering `SteelArmorMaterial`/`FaradayArmorMaterial`) - all correctly already handled
by `recolor()`-with-sampled-color, not this category - or isn't a player `ArmorItem` at all
(netherite_horse_armor, portablewardrobearmorswap's Curios-based swap items, armortrims,
cosmeticarmorreworked). One genuine real-vanilla-material hit exists (`geckolib-forge`'s example
`GeckoArmorItem` on `ArmorMaterials.DIAMOND`) but its registration is gated behind
`!FMLEnvironment.production` - it never actually loads in a normally-launched instance, so
nothing to wire. Don't re-run this search unless a mod gets added/updated; treat the 11-jar
shortlist above as the only ones worth re-checking if that happens.

## armor-overflow-renderer

DetailArmorBar (detailab)'s own `ArmorBarRenderer#render` only ever draws one row of 10
armor-point icons and collapses anything past 20 armor points into tiny 3px tick marks to the
left of the bar (`if (armorPoints.size() > 20) { ... CustomArmorBar.DEFAULT.draw(...) }`). No
detailab-compatible "armor overflow" mod exists (checked), so
`creativeCommon/.../compat/ArmorOverflowRenderer.java` adds one: real extra rows of the same
textured/recolored icons ([armor-bar-compat](armor-bar-compat.md)) stacked above row 0.

Mechanism (zero touches to detailab's own render method):
- Hooks the exact same event detailab's `PlayerEvents#onArmorRender` does —
  `RenderGuiOverlayEvent.Pre` for `VanillaGuiOverlay.ARMOR_LEVEL` — at `EventPriority.LOWEST`
  with `receiveCanceled = true` (detailab always cancels that event to suppress vanilla's own
  armor icon; without `receiveCanceled` a lower-priority listener never runs once canceled).
- `mixin/ArmorBarRendererAccessor.java` — a Mixin `@Invoker` onto detailab's own private
  `getArmorPoints(Player)` (confirmed via `javap` against the real class, not just the
  decompiled source: `private static List getArmorPoints(Player)`) — reuses detailab's own
  exact (ItemStack, CustomArmorBar) list/order instead of re-deriving the armor-point algorithm
  (attribute-modifier bonus points included) independently. `remap = false`, same as every other
  mixin here targeting an optional third-party mod class directly — Mixin just skips applying if
  detailab is absent.
- Row 0 always shows the LAST (possibly partial) 20-point chunk; overflow draws earlier full
  20-point chunks as rows above it, topmost row drawn FIRST so a lower row's later draw call
  paints over the row above it in compact mode (the "tucked behind" look).
- detailab's own row-0 Y anchor (health-row-dependent, `ArmorBarRenderer#render`'s `yPos`) is
  private with no accessor — duplicated from the decompiled source in `row0Y()` rather than
  mixed into; a future detailab update could drift this a few px, not crash.
- `CEConfig.ArmorOverflow`: `enabled` (master switch), `compact` (tuck rows tightly instead of
  full 9px icon-height spacing), `compactRowSpacing` (1-9, default 4).

Known limitation, left alone deliberately: detailab's own tiny tick-mark indicator (to the left
of row 0) still draws too — suppressing it would need a much more fragile Mixin (its draw call
site can't be cleanly isolated without breaking row 0's own `stackCount`/`stackRow` math, which
reuses the same literal `20`). Redundant but non-overlapping with the new rows; flagged to the
user rather than silently deciding either way.

Tooling note: this session's decompile scratchpad (CFR jar + `dab_src/`) gets wiped between
sessions — CFR itself isn't vendored anywhere on disk, so it was re-downloaded from
`https://github.com/leibnitz27/cfr/releases/download/0.152/cfr-0.152.jar` (network access
confirmed available). Re-decompile directly from `mods/DetailArmorBar-2.6.4+1.19.2-forge.jar`
(not `DecompiledMods/`, which only had raw `.class` files) if this comes up again.
