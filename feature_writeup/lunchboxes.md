# Lunchboxes: four tiers, skins, dye, soap, slot tooltip

Spice of Life: Apple Pie Edition (`solapplepie` 2.2.0, LGPL-2.1) ships three food containers. The pack retextures
and renames them, adds a fourth tier, and adds skins, dye and soap. Every recipe that turns one box into another keeps
the food inside. Test prefix `LB-`. Built 2026-10-29; untested in game; not in the 1.1.04 payload. Installed: patcherMod 1.1.04 (2026-10-31 00:44) and Creative Delight 1.0.0 (2026-10-31 00:29) are in `mods/`,
not yet booted. TODO rows: `items/storage-and-item-loss #9` (this feature), `ui/tooltips #2` (slot
count), `mobs/tweaks #7` (seagulls), scratchpad 2026-10-29 (reskinning).

## 0. At a glance

| Part | Status |
|---|---|
| Netherite Lunchbox (18 slots, 2 x 9, fire resistant) | BUILT 2026-10-29, scratch server PASSED r1031 (2026-10-31, SQ-LB-01), in game pending |
| Renames and textures: Lunchbox = Copper, Golden Lunchbox = Iron | BUILT 2026-10-29, untested |
| Tier recipes that keep the contents (shaped copper/iron, smithing netherite) | BUILT 2026-10-29, scratch server PASSED r1031 (2026-10-31, SQ-LB-02: recipes load clean), in game pending |
| Skins (smithing a lower-tier empty box or a berry basket on) | BUILT 2026-10-29, untested |
| Dye (grid, leather-style mixing, base only) | BUILT 2026-10-29, untested |
| Soap (grid: dye off, then skin off) | BUILT 2026-10-29, untested |
| "Slots: N" tooltip | BUILT 2026-10-29, untested |
| Seagulls ignore all lunchboxes (inventory and dropped) | BUILT 2026-10-29, untested (MT-26 in [mob_block_tweaks.md](mob_block_tweaks.md)) |

- **Pending:** the in-game suite (section 7). Scratch rows SQ-LB-01, -02 PASSED r1031 (2026-10-31) with patcherMod
  1.1.04 and Creative Delight 1.0.0.
- **Files to open:** section 5.
- **Knobs:** none in config. The slot count of the new tier is fixed at registration (`LunchboxItems.NETHERITE_SLOTS`).

## 1. Overview

| Tier | Item id | In game | Slots | Look | Recipe |
|---|---|---|---|---|---|
| 1 | `solapplepie:lunchbag` | Lunchbag | 5 | paper (unchanged) | Spice of Life's: 6 paper |
| 2 | `solapplepie:lunchbox` | Copper Lunchbox | 9 | copper | Bundle + 6 Copper Ingots |
| 3 | `solapplepie:golden_lunchbox` | Iron Lunchbox | 14 (7 x 2) | iron | Copper Lunchbox + 6 Iron Ingots |
| 4 | `solapplepie:netherite_lunchbox` (pack) | Netherite Lunchbox | 18 (9 x 2) | netherite | smithing: Iron Lunchbox + Netherite Ingot + Netherite Upgrade template |

The ids keep Spice of Life's names so existing boxes and their contents carry over. `item_dict` has the aliases
`i.copper_lunchbox` and `i.iron_lunchbox`.

## 2. Reference: how Spice of Life is built

- `FoodContainerItem(nslots, name)` builds its own item properties (food tab, stack size 1, no repair). The pack's
  tier subclasses it and overrides `isFireResistant`.
- The food lives in a Forge item-handler **capability** (`FoodContainerCapabilityProvider`, saved under `ForgeCaps`),
  not in the stack tag. `ItemStack.copy()` carries it; a new result stack from a shaped or smithing recipe does not.
  So Spice of Life's own upgrade recipes handed back an empty box.
- The screen (`FoodContainerScreen`, `FoodContainer`) lays out up to 9 slots in one row, more in two rows of
  slots / 2. 18 gives two rows of 9 in the stock 176 px background.
- Eating from a box (checked 2026-10-29, bytecode): `FoodContainerItem.finishUsingItem` picks the slot with the best
  diversity gain (`getBestFoodSlot`, from the player's food list), calls that food's own `finishUsingItem` (hunger,
  saturation, effects, KubeJS `foodEaten`, the bowl back to the inventory), then fires `LivingEntityUseItemEvent.Finish`
  itself with a copy of the food. `FoodTracker.onFoodEaten` returns early for any `FoodContainerItem`, so vanilla's
  second Finish event (carrying the box) is ignored: Spice of Life records the food, never the box. The Netherite
  Lunchbox subclasses `FoodContainerItem`, so the same holds. Pack listeners agree: `Seasonings.onEaten` reads the
  food copy's NBT (seasonings apply from a box), the box has none.
- The screen title is `item.solapplepie.container.<name>`; the name is the constructor's second argument.
- Alex's Mobs seagulls: `SeagullAIStealFromPlayers.hasFoods` / `getFoodItemFrom` scan the player's whole inventory
  for `isEdible()` items not in `seagullStealingBlacklist`. Lunchboxes report `isEdible() = true`. Dropped items
  go through `EntitySeagull.canTargetItem`.
- Armor Trims (1.3.3) replaces the smithing table: slot 0 base, slot 1 template, slot 2 material (slot 2 only takes
  items while slot 1 holds a template). With no template it runs vanilla-style recipes with `[slot 0, slot 1]`;
  with an upgrade template, `[slot 0, slot 2]`, and a netherite ingot needs the Netherite Upgrade template.

## 3. What the pack changes

1. **Netherite tier.** patcherMod registers `solapplepie:netherite_lunchbox` (`LunchboxItems`), 18 slots, fire
   resistant, only when Spice of Life is loaded.
2. **Looks.** New item models in `kubejs/assets/solapplepie/models/item/` point Lunchbox at the copper texture and
   Golden Lunchbox at the iron texture. Spice of Life's own `lunchbox.png` and `golden_lunchbox.png` are untouched:
   the camera bags still use them ([camera_bags.md](camera_bags.md) F2).
3. **Recipes keep the contents.** Shaped tier upgrades are type `creativeengineering:lunchbox_shaped` (vanilla shaped
   JSON). The result takes the grid box's name, dye, skin and contents. A bundle that still holds items is refused.
   The netherite step is `creativeengineering:lunchbox_upgrade` (vanilla smithing JSON, same copy).
4. **Skins.** Smithing (`creativeengineering:lunchbox_skin`): any lunchbox in slot 0, a skin item in slot 1, no
   template. The skin item is used up. Allowed: a lunchbox of a lower tier that is empty, or a Fantasy Furniture berry
   basket (empty, blueberry, strawberry, sweetberry). Smithing another skin replaces the old one. The skin is the item
   id in the stack tag `CELunchboxSkin`; contents, name and dye stay.
5. **Dye.** Grid (`creativeengineering:lunchbox_dye`): one lunchbox + any number of dyes, mixed like leather armor
   (vanilla's formula, repeated because vanilla's only takes leather items). Stored in `display.color`, so the vanilla
   "Dyed" tooltip line shows. Only the base is recolored; outline and latches keep their colors (layer 1 of the dyed
   model is never tinted). Refused while the box wears a basket skin.
6. **Soap.** Grid (`creativeengineering:lunchbox_soap`): one lunchbox + one Supplementaries Soap. Dyed: the dye comes
   off. Not dyed but skinned: the skin comes off. Neither: no recipe. Supplementaries' own soap recipe never matches
   a lunchbox.
7. **Tooltip.** `Slots: N` under the name (`LunchboxTooltip`, the capability's slot count).
8. **Seagulls.** `AlexsMobsSeagullStealMixin` makes every `FoodContainerItem` count as blacklisted (inventory steals),
   `AlexsMobsSeagullItemMixin` makes `canTargetItem` false for them (dropped boxes). The config blacklist from
   2026-10-22 stays; it is no longer needed for lunchboxes.

Rendering: two item properties, `creativeengineering:lunchbox_skin` (position in `Lunchboxes.SKINS` + 1) and
`creativeengineering:lunchbox_dyed`, drive the model overrides. Each look has a plain model (the drawn texture) and a
dyed model: layer 0 is a greyscale copy of the base (tinted by the item colour handler), layer 1 the outline and
latches. `tools/gen_lunchbox_looks.py` writes those textures and every model from the source art.

## 4. Limits and decisions

Decided 2026-10-29 (owner):

- **Names and recipes follow the textures.** Copper starts from a Bundle, not the Lunchbag. Netherite by smithing with
  the template, like every netherite upgrade (camera bags F1 decision).
- **Reskinning is smithing and uses the skin item up.** A skin box must be empty, so no food is lost with it.
- **Dye mixes like leather armor**, can be layered.
- **Seagulls ignore every lunchbox** in code, not through the config list (the list alone had not stopped them).
- **Soap order:** dye first, then skin (scratchpad).

Limits:

- A box can only wear lower tiers' looks; to get its own look back, soap the skin off.
- Basket skins draw Fantasy Furniture's basket model (3D, as in the inventory); dye is not shown on them and dyeing
  is refused while one is on.
- Skin numbers are fixed by list order in `Lunchboxes.SKINS` and the generator's `SKINS`: only append, never reorder,
  or skinned boxes in saves change look.
- Removing Fantasy Furniture leaves basket-skinned boxes pointing at a missing model (they draw as the missing-model
  cube until soaped).

Rejected: overwriting Spice of Life's `lunchbox.png` / `golden_lunchbox.png` (would change the camera bags too);
making lunchboxes `DyeableLeatherItem` by mixin (Supplementaries' soap and the cauldron would then wash only the dye,
with no step for the skin, and Supplementaries' recipe would compete with the pack's).

## 5. File map

| File | What |
|---|---|
| [lunchbox/Lunchboxes.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/lunchbox/Lunchboxes.java) | tiers, skins table, tag/dye/contents helpers |
| [lunchbox/LunchboxItems.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/lunchbox/LunchboxItems.java) | Netherite Lunchbox |
| [lunchbox/LunchboxRecipes.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/lunchbox/LunchboxRecipes.java) | the 5 serializers; recipe classes beside it |
| [lunchbox/LunchboxTooltip.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/lunchbox/LunchboxTooltip.java) | slot line |
| [lunchbox/client/LunchboxClient.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/lunchbox/client/LunchboxClient.java) | item properties, dye tint |
| [mixin/AlexsMobsSeagullStealMixin.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/AlexsMobsSeagullStealMixin.java), [AlexsMobsSeagullItemMixin.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/AlexsMobsSeagullItemMixin.java) | seagulls |
| [neoCraftHelper.js](../development/patcherMod/src/main/resources/kubejs_startup/creativeengineering/func/neoCraftHelper.js) | `lunchboxShaped`, `lunchboxSmithing`, `lunchboxSkin`, `lunchboxDye`, `lunchboxSoap` |
| [solapplepie/lunchboxes.js](../kubejs/data/creative_delight/kubejs/solapplepie/lunchboxes.js) | the recipes (Creative Delight tree; in the CD jar) |
| [namechanges.js](../kubejs/client_scripts/namechanges.js) | names and screen titles |
| `kubejs/assets/solapplepie/textures/items/` | source art (copper, iron, netherite + outline masks); `lunchbox_dye/` generated |
| `kubejs/assets/solapplepie/models/item/` | generated item and look models |
| [tools/gen_lunchbox_looks.py](../tools/gen_lunchbox_looks.py) | writes the dye textures and models |

## 6. Offline checks

- `python3 tools/gen_lunchbox_looks.py --check` (exit 0 = textures and models match the source art).
- `./gradlew :patcherMod:build --offline` (2026-10-29: clean, no mixin target warnings for the seagull mixins).
- `node --check` on `lunchboxes.js`, `neoCraftHelper.js`, `namechanges.js`.

## 7. In-game test suite

- [ ] **LB-01 Names and looks:** JEI search "lunchbox" → Lunchbag (paper), Copper Lunchbox (copper), Iron Lunchbox
  (iron), Netherite Lunchbox (netherite); open each: screen titles match, 5 / 9 / 14 / 18 slots, 18 = two rows of 9.
- [ ] **LB-02 Tooltip:** hover each → `Slots: 5` / `9` / `14` / `18` under the name.
- [ ] **LB-03 Copper recipe:** 6 copper ingots + an empty Bundle → Copper Lunchbox. A Bundle holding items → no result.
- [ ] **LB-04 Iron upgrade keeps food:** Copper Lunchbox with 3 foods + 6 iron ingots → Iron Lunchbox with the same 3
  foods in the same slots; rename the copper box first, the name stays.
- [ ] **LB-05 Netherite upgrade:** smithing table, Iron Lunchbox (with food) in slot 0. Netherite Ingot without a
  template → nothing; Netherite Upgrade template + ingot → Netherite Lunchbox, food kept, template used up.
- [ ] **LB-06 Fire:** drop a Netherite Lunchbox in lava → it floats, not burnt. Iron Lunchbox → burns.
- [ ] **LB-07 Skin, lower tier:** Iron Lunchbox in slot 0, empty Copper Lunchbox in slot 1 (no template) → Iron Lunchbox
  with the copper look, still 14 slots, contents kept; the copper box is used up. Copper box with food in it → no
  result. A Netherite Lunchbox as the skin on an Iron Lunchbox → no result.
- [ ] **LB-08 Skin, basket:** Lunchbag + each berry basket → basket look in hand, inventory, dropped.
- [ ] **LB-09 Dye:** Copper Lunchbox + red dye → red base, dark outline and gold latches unchanged; + blue dye again →
  mixed color like leather armor; tooltip "Dyed". Basket-skinned box + dye → no result.
- [ ] **LB-10 Dyed skin:** dye an Iron Lunchbox wearing the copper skin → copper-shaped box with dyed base.
- [ ] **LB-11 Soap:** dyed and skinned box + Soap → dye gone, skin stays; + Soap again → own look; undyed unskinned box +
  Soap → no result. Contents kept each time.
- [ ] **LB-12 Seagulls:** stand on a beach with only lunchboxes (with food inside) and one Bread in the inventory →
  gulls take the Bread, never a box. Drop a lunchbox near gulls → they ignore it (MT-26).
- [ ] **LB-14 Eaten food counts, not the box:** fill a Netherite Lunchbox with 3 foods never eaten, eat from it
  3 times → the Spice of Life food book lists those 3 foods and never a lunchbox; diversity rises each time; a
  seasoned food eaten from the box gives its seasoning bonus; a bowl food returns the bowl to the inventory.
- [ ] **LB-13 Camera bags unchanged:** iron and netherite camera bags still show the old Spice of Life textures.

## 8. History

- 2026-10-29: built. Owner decisions in section 4. Textures from the owner's art (`copper_lunchbox.png`,
  `iron_lunchbox.png`, `netherite_lunchbox.png`, two outline masks; moved from `kubejs/assets/` into
  `kubejs/assets/solapplepie/textures/items/`, the `ntherite` typo fixed). patcherMod and Creative Delight built and
  installed. Changelog bullets in `unreleased.md` (Food & Cooking, Mobs & Animals).
- 2026-10-29: checked that eating from a lunchbox records the food in Spice of Life, not the box (section 2): already
  so in Spice of Life 2.2.0, covers the Netherite tier; no code change. LB-14 added.
