# ui / tooltips: implementation brief

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# ui`, Sub `tooltips` (rows 2, 3 and 5 open; 1 Complete, 4 Testing).
Feature writeup: [tooltips.md](../tooltips.md) (test prefix `TT-`, last id TT-30). Hand-written lines live in
[tooltips.js](../../kubejs/client_scripts/tooltips.js) (helpers L1-86, colour legend `col.*` L95-125, sections from L153).

## Scope and order

| Group | Rows | Work | Why together |
|---|---|---|---|
| A | `tooltips #2` parts a (straw / knives), d (chalk), e (Pretty Pipes cover), f (lunchboxes); `tooltips #3` (Botania, Cloud Storage) | KubeJS: `client_scripts/tooltips.js`, new item_dict keys | All plain text lines in one file, F3+T to reload; one helper extension (`addTooltipTags`) |
| B | `tooltips #2` part b (food type line) | KubeJS: new `client_scripts/food_type_tooltip.js` | Reads `food_types.json`, its own file like the other framework tooltips (tooltips.md 4 "One file per framework") |
| C | `tooltips #2` part c (can AppleSkin "x N"), `tooltips #5` (tall tooltips run off the bottom) | patcherMod: two client mixins, CEConfig keys | Both client render mixins; one patcherMod version bump and one deploy |

Build order: A (needs the user's OK on the text, `tooltips #3` decision "the text"), then B, then C with the next patcherMod
build. A and B are client-only KubeJS (no restart, F3+T); C needs a client restart.

Cross-family links:
- `dishes/ingredients #10` (fats and oils, [brief](dishes_ingredients.md)) also wants a knife tooltip ("knives give more meat and
  fat"). Write both knife lines in one place (part a below), so the knife carries one block of lines.
- `gameplay/misc #8` (every chalk symbol unlocked) changes what the chalk line should say about symbols (part d).
- `mobs/tweaks #7` (seagulls can't take lunchboxes, [brief](mobs_tweaks.md)) is about the same three items as part f.
- `food/types-and-tuning #1` part C (SoL complexity per type, [brief](food_types-and-tuning.md)) can later add the type's
  diversity value to the part b line.
- tooltips.md 8 also lists an AE2 cable line (`jei/jei-coverage #3`) and flower pot labels (`blocks/framed-and-decor #6`):
  those rows own them.

---

## tooltips #2: Tooltips to add (items)
Status **Planned** · Work: KubeJS / patcherMod · id `ui/tooltips #2`

### Goal
Six item families get lines that say what is not obvious in game: (a) straw and knives, (b) a food type line on every
typed food, (c) cans show AppleSkin's single-serving bars followed by "x N", (d) chalk use, (e) Pretty Pipes covers with
the wrench, (f) lunchbox slot count.

### Now
- **(a) Straw (verified, Farmer's Delight 1.2.4 jar).** Five FD global loot modifiers `farmersdelight:add_item` give
  `farmersdelight:straw`, each gated on the tool tag `farmersdelight:straw_harvesters` (= `#farmersdelight:tools/knives`):
  `straw_from_grass` and `straw_from_tall_grass` 20 %, `straw_from_sandy_shrub` 30 %, `straw_from_mature_wheat` (age 7)
  and `straw_from_mature_rice` (`rice_panicles` age 3) always. The only recipe is a straw bale -> 9 straw. The pack
  overrides none of it (no `kubejs/data/farmersdelight/`, no LootJS rule on grass). Loot modifiers don't show in JEI,
  which is why the line is wanted. Only the lead knife has a line today (tooltips.js 487, "Extra nutrients in your
  food..."). Dict: `i.straw` (item_dict 599), `i.any_knife = '#forge:tools/knives'` (197); no key for
  `#farmersdelight:straw_harvesters`.
- **(b) Food types.** 31 types in [food_types.json](../../kubejs/generation_scripts/food/food_types.json) (`types`:
  type -> item_dict keys, one type per food, `excluded` list, `canned` map), turned into
  `#creativeengineering:food_type/<type>` (nested types also land in their parents) by
  [foodtypetags.js](../../kubejs/data/creativedelight/kubejs/creativeengineering/food_type_tags.js). Type list and meaning: [food.md](../food.md) 2.1.
  Spice of Life: Apple Pie Edition 2.2.0 already adds its own diversity line (`tooltip.solapplepie.*`).
- **(c) Cans.** Each serving is the full food value, so AppleSkin already draws one serving's bars. patcherMod
  `food/CanServingsTooltip` inserts "Servings: x / y" at line 1; `food/CanServings.servingsLeft(stack)` /
  `maxServings(stack)` give the counts (max from `#creativeengineering:cans/servings/<n>`, cached, cleared on
  `TagsUpdatedEvent`). AppleSkin 2.4.2 (verified, decompiled): `squeek.appleskin.client.TooltipOverlayHandler$FoodTooltip`
  (package-private, private field `ItemStack itemStack`) and `$FoodTooltipRenderer` (private field `foodTooltip`,
  `getWidth` = SRG `m_142069_(Font)` returns `max(hungerBars*9 (+text), saturationBars*7 (+text)) + 2`, `renderImage` =
  SRG `m_183452_(Font, int, int, PoseStack, ItemRenderer, int)`, hunger row at `y`, saturation row at `y + 10`, height
  20). AppleSkin itself writes "xN" only when a row would exceed 10 icons. It also posts its API event
  `squeek.appleskin.api.event.TooltipOverlayEvent.Render` (item, x, y, pose) before drawing.
- **(d) Chalk (verified, Chalk 1.6.4 jar).** `ChalkItem.useOn`: right-click a block face draws a mark (a regular mark:
  a dot or an arrow, depending on where on the face you click; the dot/arrow split is the mod's documented behaviour,
  not read from code); **sneak + right-click opens the symbol picker** (`openSymbolSelectionScreen`). The TODO / tooltips.md
  wording "hold right click to change mark" was wrong (tooltips.md 8 corrected). Off hand is ignored when the main hand
  holds chalk. Uses: `ChalkUses = 64` ([chalk-common.toml](../../config/chalk-common.toml) line 3). Symbols unlock by
  advancement (Skull, Heart, Pickaxe, House have one; Cross and Check are always on), and the pack loads no
  advancements, which was `gameplay/misc #8`: done 2026-10-22, every `*UnlockAdvancement = ""` in chalk-common.toml, so all
  six symbols are on from the start (fixes_compat.md FX-27). The Chalk Box has its own lines ("Drawing with:", "<Right Click to open>").
  Dict: `i.chalk_<colour>` (item_dict 489-504), `i.t_chalks = '#chalk:chalks'` (129), `i.chalk_box` (12016).
  Supplementaries' blackboard line already mentions chalk (tooltips.js 721).
- **(e) Pretty Pipes 1.13.6 (verified).** `WrenchItem.useOn`: right-click a pipe that has no cover while holding a block
  (not a block entity) in the off hand -> that block becomes the cover, one is used; sneak + right-click removes the cover,
  or the pipe if it has none. PP already says this, but only under Shift (`Utility.addTooltip`, key
  `info.prettypipes.wrench`: "Allows modifying pipe connections / Places blocks in offhand as covers / Sneak to remove
  cover or pipe"); the pipe itself has no line. Pack lang overlay
  [kubejs/assets/prettypipes/lang/en_us.json](../../kubejs/assets/prettypipes/lang/en_us.json) names them "Item Pipe" /
  "Item Pipe Wrench" (`names #5`). No `prettypipes:*` ids in item_dict.
- **(f) Lunchboxes (verified, solapplepie 2.2.0).** `SOLSweetPotatoItems`: `solapplepie:lunchbag` 5 slots,
  `solapplepie:lunchbox` 9, `solapplepie:golden_lunchbox` 14 (`new FoodContainerItem(n, ...)`). Right-click eats the stored
  food that adds the most diversity (`getBestFoodSlot`), sneak + right-click opens it. No slot count shown today.
  Dict: `i.lunchbag`, `i.lunchbox`, `i.golden_lunchbox` (12489-12491).

### Build
1. **Helper (tooltips.js, next to `addTooltipPattern`):** tag lines checked at hover time, since item tags only reach the
   client on join (the reason placeable_tooltip.js uses `addAdvancedToAll` + `item.hasTag`):
   ```js
   // addTooltipTags(e, [tag, [text, col], ...], ...): lines on every item in a tag, tested per tooltip
   function addTooltipTags(e) {
       var rules = Array.prototype.slice.call(arguments, 1).map(r => ({
           tag: String(r[0]).replace(/^#/, ''), lines: r.slice(1).map(buildTooltipLine) }))
       e.addAdvancedToAll((item, advanced, text) => {
           rules.forEach(r => { if (item.hasTag(r.tag)) r.lines.forEach(l => text.add(Text.of(l))) })
       })
   }
   ```
   One `addAdvancedToAll` per call: call it once with every rule. `Text.of` on the `{text, color}` object is
   **unverified** in this KubeJS build; fall back to `Text.literal(l.text).color(l.color)` if it prints JSON.
2. **(a)** item_dict: `i.t_straw_harvesters = '#farmersdelight:straw_harvesters';` near `i.any_knife`. tooltips.js
   "Food / Farmers Delight" (L484):
   - `addTooltips(e, [i.straw, ['Cut grass or tall grass with a knife (1 in 5), or mature wheat or rice (always)', col.norm]])`
   - `addTooltipTags(e, [i.t_straw_harvesters, ['Cutting grass, tall grass, mature wheat or rice can give straw', col.norm]])`.
     Key it on `straw_harvesters`, the tag the loot modifiers read, not `#forge:tools/knives` (a mod knife in only the
     forge tag would get a line but no straw).
   - When `dishes/ingredients #10` lands, its "more meat and fat" line goes in the same `addTooltipTags` rule (that
     drop reads `#forge:tools/scavenging`, Delightful's tag: give it its own rule if the tags differ).
3. **(d)** "Misc Mods" or a new "Chalk" block: `addTooltipTags(e, [i.t_chalks, ['Right-click a block to draw a mark: a dot in the middle, an arrow off-centre', col.norm], ['Sneak + right-click to pick a symbol', col.norm]])`;
   add "64 marks per chalk" only if wanted (it is already the durability bar). `gameplay/misc #8` landed 2026-10-22 (every
   symbol unlocked), so no line about learning symbols.
4. **(e)** item_dict: `i.pp_pipe = 'prettypipes:pipe';`, `i.pp_wrench = 'prettypipes:wrench';` (short mod prefix, like
   `i.sup_wrench`, `i.fd_basket`; `i.pipe` is Quark's). tooltips.js "Transport" (L478):
   `[i.pp_pipe, ['Cover it: hold a block in your off hand and right-click it with the Item Pipe Wrench', col.norm]]`,
   `[i.pp_wrench, ['Holding a block in your off hand: right-click a pipe to cover it; sneak to take it off', col.uncm]]`.
   Leave PP's Shift text as is, or reword it in the prettypipes lang overlay (`info.prettypipes.wrench`).
5. **(f)** tooltips.js "Food / Farmers Delight": `[i.lunchbag, ['Holds 5 foods', col.uncm], ['Eats the food that adds the most diversity; sneak + right-click to open', col.norm]]`,
   lunchbox 9, golden lunchbox 14.
6. **(b) Food type line**, new `kubejs/client_scripts/food_type_tooltip.js`:
   - At load, read `kubejs/generation_scripts/food/food_types.json` with `JsonIO.readString` (works on the client:
     name_fixes.js does the same) and build `id -> type` from `types` through `global.item[key]` (skip `#` tags and
     missing keys). No tag lookups, so it works before tags sync.
   - `LABELS` map type -> text in the script, e.g. `meal/feast` "Feast", `sweet` "Sweets", `baked` "Baked",
     `raw/fruit` "Fruit", `raw/meat/sliced` "Raw Meat Cut", `drink/alcohol` "Alcohol", `golden/enchanted` "Enchanted
     Golden", `canned` "Canned"; a type with no label prints its path in Title Case and logs a warning.
   - `ItemEvents.tooltip(e => e.addAdvancedToAll((item, adv, text) => { var t = TYPE[item.id]; if (t) text.add(...) }))`,
     one handler, one map lookup per tooltip. For cans, optionally add the source's type from `canned`
     ("Canned: Soup"), decision D2.
   - Log `[food type tooltip] N foods, M types`.
7. **(c) Can "x N" (patcherMod):**
   - `mixin/AppleSkinFoodTooltipAccessor` (client, `@Mixin(targets = "squeek.appleskin.client.TooltipOverlayHandler$FoodTooltip", remap = false)`,
     `@Accessor("itemStack") ItemStack creativeengineering$getItemStack();`).
   - `mixin/AppleSkinCanServingsMixin` on `...TooltipOverlayHandler$FoodTooltipRenderer` (`remap = false`, SRG names as
     the other mixins use them, e.g. FtbQuestDescriptionTooltipMixin):
     ```java
     @Unique private ItemStack creativeengineering$stack = ItemStack.EMPTY;
     @Inject(method = "<init>", at = @At("RETURN"))
     private void creativeengineering$init(@Coerce TooltipComponent tip, CallbackInfo ci) {
         creativeengineering$stack = ((AppleSkinFoodTooltipAccessor) tip).creativeengineering$getItemStack(); }
     @Inject(method = "m_142069_", at = @At("RETURN"), cancellable = true)          // getWidth
     private void creativeengineering$width(Font font, CallbackInfoReturnable<Integer> cir) {
         String s = CanServingsAppleSkin.label(creativeengineering$stack);           // "x3", or null when not a can
         if (s != null) cir.setReturnValue(cir.getReturnValue() + 3 + font.width(s)); }
     @Inject(method = "m_183452_", at = @At("TAIL"))                                // renderImage
     private void creativeengineering$draw(Font font, int x, int y, PoseStack pose, ItemRenderer ir, int z, CallbackInfo ci) { ... }
     ```
     Draw the label once, vertically between the two rows (`y + 6`), right of the wider row, grey `0xAAAAAA` like
     AppleSkin's own "xN", with `pose.translate(0, 0, z)`. `CanServingsAppleSkin` (new, in `food/`) returns
     `"x" + CanServings.servingsLeft(stack)` when `maxServings > 1` and the `[canning] appleSkinServings` switch is on.
   - CEConfig `Canning`: `appleSkinServings` (boolean, default true, "client side: AppleSkin's bars on a can show one
     serving followed by xN, the servings left").
   - Register both mixins under `client` in creativeengineering.mixins.json.
8. Docs: tooltips.md 3 "Added" (a, d, e, f, b) and "Fixed" (c), section 5 file map, section 8 ticks; cans.md 2.1 one line
   on (c).

### Ids and data
- Verified in jars: FD loot modifier ids and chances above, `farmersdelight:straw_harvesters`; Chalk `ChalkUses`, sneak to
  pick; PP `prettypipes:pipe`, `prettypipes:wrench`, `info.prettypipes.wrench`; solapplepie slot counts; AppleSkin class,
  field and SRG method names.
- New dict keys: `i.t_straw_harvesters`, `i.pp_pipe`, `i.pp_wrench`.
- Unverified: Chalk's dot vs arrow rule (from the mod page), `Text.of` on the line object, `@Coerce` on a constructor
  injector parameter (if Mixin refuses it, make the accessor's target the renderer instead and read `foodTooltip` as
  `Object` through reflection once, cached).

### Decisions
- **D1 Line text (a, d, e, f).** Proposed wording above; recommend the user reads all six in one pass (same as `#3`).
- **D2 Food type line format.** Recommend just the label ("Feast"), grey, at the end of the tooltip; cans read
  "Canned: <source type>" from the `canned` map. Alternative: "Food type: Feast" (clearer, longer).
- **D3 Can label: servings left or full count.** Recommend servings left ("x3" on a can with 3 of 4 left), matching the
  "Servings: 3 / 4" line; the bars are always one serving.

### Gotchas
- `addTooltipPattern` resolves at load; `addTooltipTags` resolves per hover, so a tag added by a server script works
  only after joining a world (JEI before joining shows no line). That is fine for these tags.
- KubeJS's static `e.add('#tag', ...)` resolves the tag when the tooltip event fires, which can be before tags sync:
  don't use it for tags.
- A second `addAdvancedToAll` per tag rule costs one check per hovered item per rule; keep the rules in one call.
- (c) AppleSkin hides its bars unless Shift is held when its client config says so; the label follows the bars (it is
  drawn in AppleSkin's own component, so it only appears when they do).

### Tests (TT-)
- TT-31 straw: hover Straw and an Iron Knife -> the lines above; break 20 grass with the knife -> a few straw.
- TT-32 chalk: hover White Chalk -> both lines; sneak + right-click a block -> the symbol picker opens.
- TT-33 pipes: hover an Item Pipe and the Item Pipe Wrench -> the cover lines; with stone in the off hand, right-click
  a pipe with the wrench -> stone cover; sneak + right-click -> cover back.
- TT-34 lunchboxes: Lunchbag "Holds 5 foods", Lunchbox 9, Golden Lunchbox 14.
- TT-35 food type: Roast Chicken (feast serving) "Feast", Cookie "Sweets", Bread "Baked", a Canned Beef Stew
  "Canned: Soup", a lunchbox nothing; latest.log `[food type tooltip]` count equals food_types.json's typed keys.
- TT-36 can bars: a full Canned Beef Stew -> AppleSkin bars of one serving and "x4" (or its count) right of them, inside
  the tooltip box; eat one -> "x3"; `[canning] appleSkinServings = false` -> no label.

---

## tooltips #3: Tooltips to add (mods)
Status **Planned** · Work: KubeJS · id `ui/tooltips #3`

### Goal
Botania and Cloud Storage get the practical lines a player needs without opening the mod's book.

### Now
- **Botania 440.2.** Pack lines: only the vine ball (tooltips.js 791-795). Botania's own tooltips: special flowers show
  "Generating / Functional Flower" plus a joke `*.reference` line (52 of them, e.g. Pure Daisy "What color do you want
  to be?"), so nothing practical. Pack changes to Botania that a line could explain: none in recipes that needs one
  (resource_blocks.js removes two mana infusions, storage.js the Third Eye recipe, fabric.js swaps cloak / flower bag
  inputs). Open rows that will need a line later: `recipes/magic #3` (trinket case takes any item),
  `dishes/ingredients #11` (a use for the bowl of water).
- **Cloud Storage 1.3.4 (verified, the mod's guide book `storage_clouds.txt`).** No pack lines. Mechanics: a Balloon
  (dyeable, colour = cloud account) tied to a chest and released uploads that chest's space to the colour's cloud; a
  Cloud Chest with a balloon of that colour opens it, needs sky access, not in the Nether, private to you (hoppers
  can't reach it); a Static Cloud Chest (from Static Cloud blocks) is shared, filled with static balloons (a balloon used
  on a Static Cloud block); Balloon Arrows (balloon + 8 arrows) fly straight and lift the mob hit; held balloons slow
  falls; the Cloud Blower runs on cloud bottles and changes mode on swing. Dict keys exist: `i.balloon` (1753),
  `i.cloud_chest`, `i.static_cloud_chest` (646-647), `i.balloon_arrow` (773), `i.static_cloud`, `i.cloud_blower`,
  `i.balloon_stand`, `i.happy_cloud_in_a_bottle`, `i.angry_cloud_in_a_bottle`, `i.propeller_hat` (11926-11942).

### Build
1. tooltips.js: new "Cloud Storage" section after "Botania", `addTooltips` with `col.norm` / `col.uncm`. PROPOSED text:
   - Balloon: "Tie it to a chest and let it go: the chest's space joins your cloud of that colour" / "Dye it to pick
     the cloud"
   - Cloud Chest: "Put a balloon on it to open that colour's cloud; needs open sky, not in the Nether" / "Only you can
     open it; hoppers can't reach it"
   - Static Cloud Chest: "A cloud everyone shares; fill it with static balloons (a balloon used on a Static Cloud block)"
   - Balloon Arrow: "Flies straight; the mob it hits floats up"
   - Balloon Stand: "Shows how many cloud slots each colour has" (from `cloudstorage.container.balloon_stand.*`)
2. Botania: recommend a short onboarding set, not one line per flower: Lexica Botania ("Botania's guide: start here"),
   mystical flowers and petals ("Petals go in a Petal Apothecary filled with water"), Petal Apothecary, Pure Daisy
   ("Turns the 8 blocks around it into living rock / wood"), Mana Spreader and Mana Pool ("Generating flowers fill
   spreaders; spreaders fill pools"), Wand of the Forest ("Links spreaders and flowers; sneak to switch mode"), using the
   dict keys `i.lexicon`, `i.white_mystical_flower` (pattern `botania:*_mystical_flower`), `i.white_petal` (pattern
   `botania:*_petal`), `i.apothecary_default`, `i.pure_daisy`, `i.mana_spreader`, `i.mana_pool`, `i.twig_wand`.
   Use `addTooltipPattern` for the 16 colours (check the log line for the match count).
3. tooltips.md 8 "Mods to Cover": tick both.

### Ids and data
Cloud Storage facts from its jar's guide book (verified text, behaviour not tested in game). Botania facts from its
lang and Lexicon (general knowledge, unverified against 440.2 code for the Pure Daisy count and wand modes).

### Decisions
- **D1 The text** (the row's open decision): the PROPOSED lines above.
- **D2 Botania scope:** recommend the onboarding set (about 8 entries); a line per special flower (60+) is a big text job
  and duplicates the Lexicon.

### Gotchas
- `addTooltipPattern('botania:*_petal', ...)` also matches `botania:*_petal_block`; exclude them or use
  `'botania:*_petal'` with `{exclude: [...]}`, and check `[addTooltipPattern] ... -> N matches` (16 expected).

### Tests (TT-)
- TT-37: hover a Balloon, Cloud Chest, Static Cloud Chest, Balloon Arrow -> the lines; a Cloud Chest placed underground
  with a balloon still says "Cloud Chest must be under the sky" (the line matches the mod).
- TT-38: hover the Lexica, a white mystical flower, a white petal (not the petal block), Pure Daisy -> the lines;
  latest.log pattern lines report 16 matches.

---

## tooltips #5: Large tooltips run off the bottom
Status **Testing** (BUILT 2026-11-02 as below with `index = 9`, the javap check: int locals x 3, y 4, width 6, height 7, tooltipX 8, tooltipY 9; [tooltips.md](../tooltips.md) 3 "Fixed") · Work: patcherMod · id `ui/tooltips #5`

### Goal
A tooltip taller than the screen keeps its top (name and first lines) on screen and runs off the bottom, instead of the
other way round.

### Now
- Forge 1.19.2 (43.3.9) `Screen.renderTooltipInternal` = SRG `m_169383_(PoseStack, List, int, int)` (verified from
  Legendary Tooltips' refmap) places the box: `x = preEvent.getX() + 12`, `y = preEvent.getY() - 12`, flip left if it
  overflows the right edge, and `if (y + height + 6 > this.height) y = this.height - height - 6`. A box taller than the
  screen therefore gets a negative `y`: the top is cut off.
- Legendary Tooltips 1.4.0 (`mixin/ScreenMixin`) and Iceberg 1.1.4 (`mixin/ScreenMixin`) both hook this same method
  (LT: `@ModifyVariable` on the width, int ordinal 2, and a title-centering inject at the first `List.size()`; Iceberg:
  HEAD / TAIL), and draw their frame from the position vanilla computes, so fixing the position there carries LT's frame
  along. Iceberg's own `Tooltips.calculateRect` (used for its item-tooltip path) has the same clamp.
- `RenderTooltipEvent.Pre.setY` can't fix it: the clamp runs after the event.
- Tooltips drawn by FTB Library's own text renderer (text-only quest hovers) don't use this method.

### Build
1. `mixin/ScreenTooltipTopMixin` (client) on `net.minecraft.client.gui.screens.Screen`:
   `@ModifyVariable(method = "m_169383_", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;m_85836_()V", ordinal = 0), ordinal = N, remap = false)`
   returning `Math.max(y, 4)` when `[tooltips] keepTopOnScreen` is on. `N` is the int-local ordinal of the clamped `y`
   (with the two int parameters at 0 and 1 and the width at 2, as LT's ordinal shows, `y` is probably 5): **verify it
   first** with `javap -c -l -p` on `net/minecraft/client/gui/screens/Screen.class` from the Forge client jar
   (`~/.local/share/atlauncher/libraries/net/minecraftforge/forge/1.19.2-43.3.9/forge-1.19.2-43.3.9-client.jar`, the path
   tools/lang_index.py uses) or from patcherMod's ForgeGradle cache. Use `require = 1` so a wrong target fails loudly.
2. Iceberg's `Tooltips.calculateRect` (`remap = false`, `@ModifyVariable` on its `tooltipY` before the `new Rect2i`) with
   the same rule, only if a test shows a tall tooltip going through that path (equipment comparison).
3. CEConfig: new `Tooltips` section `[tooltips]` (client side, like `[toasts]`), `keepTopOnScreen = true`, comment
   "A tooltip taller than the screen starts at the top and runs off the bottom (vanilla keeps the bottom on screen and
   cuts off the name)". Read live.
4. tooltips.md 3 "Fixed" and the at-a-glance table.

### Ids and data
Verified: the SRG name `m_169383_`, LT and Iceberg hooks. Unverified until the javap check: the local ordinal and the
exact Forge 43.3.9 statement order (read from memory of Forge 43 sources).

### Decisions
- **D1 Top margin:** recommend 4 px (the box border starts at `y - 4`, so 4 keeps the frame's top edge on screen).
- **D2 Also shorten long tooltips (wrap or scroll)?** Not asked; a scrolling tooltip mod would be a new jar. Recommend
  no.

### Gotchas
- The fix runs for every screen tooltip; it only changes anything when the tooltip is taller than the screen minus 10.
- LT's `ModifyVariable` on the width and our `ModifyVariable` on `y` target different locals; check the mixin audit log
  for a conflict.

### Tests (TT-)
- TT-39: at GUI scale 4 (or a small window), hover an item with a very long tooltip (a shulker box full of named items,
  or a Botania Lexicon with Shift) -> the name is at the top of the screen, the end is cut off at the bottom; set
  `keepTopOnScreen = false` -> back to vanilla. A short tooltip near the bottom of the screen still moves up to fit.

---

## Built, needs play

| Row | Change | Status | Notes |
|---|---|---|---|
| tooltips #1 | Tooltip framework and lines | Complete | TT-01 to TT-18 still unticked. tooltips.md had two stale "not in `mods/`" notes (Brewin' lines, shield Shift lines), fixed below |
| tooltips #4 | AppleSkin bars in more screens | Testing | Matches tooltips.md 3 "Fixed" (patcherMod 1.1.01, Brewin' `-03`, Vinery `-04`); TT-19 to TT-30 open. Not deployed yet (1.0.9 was deployed 2026-10-19, before the 10-20 build) |

## Writeup corrections made

- [tooltips.md](../tooltips.md) 0, Brewin' row: "BUILT 2026-10-18, not in `mods/` yet" -> "DEPLOYED with 1.0.8 (Brewin'
  `-02`)" (plan_1.0.8.md AL says DEPLOYED).
- [tooltips.md](../tooltips.md) 3, frameworks list: the shield Shift lines "(built, not in `mods/`)" -> "(DEPLOYED with
  1.0.6)", matching the at-a-glance row.
- [tooltips.md](../tooltips.md) 8, Chalk item: noted that sneak + right-click (not holding right-click) opens the symbol
  picker in Chalk 1.6.4, with a link to this brief.
