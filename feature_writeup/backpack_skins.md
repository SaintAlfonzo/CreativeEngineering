# Backpack skins: smith a block onto a Sophisticated Backpack

Smith a block from the skin tag onto any Sophisticated Backpack and it takes that block's look: held, dropped, in the
inventory and placed. Storage and upgrades are unchanged. Test prefix `BP-`. TODO family `storage-and-item-loss`,
sub `#4` ([TODO.md](todo/TODO.md)). Investigated 2026-10-22 against Sophisticated Backpacks 3.20.2 and Curios 5.1.6.4
(jar decompiled in the scratchpad). Built 2026-10-22, untested in game.

## 0. At a glance

| Part | Status | TODO |
|---|---|---|
| Smithing recipe (backpack + skin-tag block -> skinned backpack) | Built, untested | `#4` |
| Remove a skin (crafting grid, skinned backpack alone) | Built, untested | `#4` |
| Held / dropped / inventory look | Built, untested | `#4` |
| Placed look and hitbox | Built, untested | `#4` |
| Equip block with a config switch (off by default) | Built, untested | `#4` |
| Worn look (only reachable with the switch on) | Built, untested | `#4` |
| Skin tag: only the Vinery backport basket | Built, untested | `#4` |

- **Pending:** the in-game suite (section 7). Worn position/scale (constants in `BackpackSkinsClient`) are a guess and
  need tuning by eye with the switch on; check the basket's hitbox for odd edges.
- **Files to open:** section 5.

## 1. Overview

A skinned backpack is still the same item. The skin is one extra value on the stack (the skin block's id). Every
look is drawn from that value, so storage, upgrades, dyeing, tier upgrades and the open key work as before.

## 2. Reference: how Sophisticated Backpacks is built (why this is cheap)

- A placed backpack's block entity keeps the whole backpack stack and sends it to the client, and the loot table copies
  the stack back on break or pickup. A value saved on the stack therefore survives placing, picking up and dropping, and
  the client renderer can read it without any new packet.
- Held / dropped / inventory is drawn by one item renderer (`BackpackItemStackRenderer`).
- Placed is a dynamic baked model (`BackpackDynamicModel`, takes per-block `ModelData`) plus a block entity renderer for
  tanks, battery and the display item.
- Worn: the chest layer and the Curios renderer both call the static `BackpackLayerRenderer.renderBackpack`.
- Backpacks already equip to the chest slot (`BackpackItem.getEquipmentSlot` returns CHEST) and to Curios.
- SB's own model API picks a model per item type, not per stack, so it can't carry a per-stack skin. Mixins are needed.
- Not doable in KubeJS: the look is rendering code.

## 3. What the pack changes

1. **Smithing.** Base: any Sophisticated Backpack. Addition: any block item in the tag `creativeengineering:backpack_skins`.
   Result: the same backpack, all data kept, with the skin value set. Custom recipe class (vanilla smithing drops the
   base's data); SB's netherite upgrade recipe is the pattern. Emitted through a new NCH method; the tag through
   `global.tags`. Smithing again with another skin block replaces the skin. Items that are not blocks are rejected.
2. **Held, dropped, in inventory.** The item renderer draws the skin block's own item (its model, texture, hand poses and
   tint).
3. **Placed.** The block entity gives the skin to the baked model, which shows the skin block's model and render layers.
   If the skin block has a facing, it is set to the backpack's facing. SB's block colour handler uses the skin block's
   colours instead of the backpack's cloth colour.
4. **Worn.** Only when the switch is on: after SB positions the pack on the back, the skin block is drawn scaled down
   instead of the backpack model. Same for the chest layer and Curios.
5. **Equip block.** Config switch in the common config, default `false`. Off: a skinned backpack can't be put in the
   chest slot (`getEquipmentSlot` mixin returns null, which also covers shift-click and dispensers) or in a Curios slot
   (Curios equip event denied). On: both work, rendering as in 4. Config backed up to `configs_edited/` when it is
   hand-edited.

## 4. Limits and decisions

Decided 2026-10-22:

- **Tag contents: only the Vinery backport basket** (`i.basket`, `vinery:basket`) for now. More blocks are added to the
  tag later, one at a time. Blocks drawn by their own renderer (chests and the like) draw as nothing, so each entry
  needs a look in game before it goes in.
- **Hitbox: the skin's.** The placed skinned backpack uses the skin block's shape, so what you see is what you can hit
  and stand against. The basket's shape follows from this; check it for odd edges.
- **Removing a skin: crafting grid, the skinned backpack alone.** The result is the same backpack with the skin cleared,
  all data kept. The skin block is not returned (it was used up when smithed on).
- **No upgrades, tanks, battery or display item are drawn on a skinned backpack**, held, placed or worn. The upgrades
  still work; only their drawing is skipped.

Future, not part of the first build:

- **Custom display item position per skin.** A skin block could define where (and how big) a backpack's display item
  is drawn on it, so the display item can come back on skinned backpacks. Needs a small per-skin data entry (position,
  scale, rotation) keyed by the skin id; the display item is hidden until a skin has one.

Rejected: a vanilla smithing recipe per tier per block (drops data, needs one recipe each); SB's model API (per item
type only).

## 5. File map

patcherMod `com.SaintAlfonzo.creativeengineering.backpackskin`: `BackpackSkins` (stack tag `CESkin`, skin state),
`BackpackSkinRecipe` + `BackpackSkinClearRecipe` + `BackpackSkinSerializers`, `BackpackSkinCurios` (denies
`CurioEquipEvent`, registered only with Curios), `BackpackSkinsClient` (model data, quads, worn draw),
`BackpackSkinColors` (replaces SB's block colour handler, lowest priority). Mixins in `mixin/`:
`BackpackSkinItemRendererMixin` (held/dropped/GUI), `BackpackSkinBakedModelMixin` (placed quads, render types,
particle), `BackpackSkinBlockEntityMixin` (model data), `BackpackSkinBlockShapeMixin` (hitbox),
`BackpackSkinWornMixin` (worn), `BackpackSkinEquipMixin` (chest slot). Config `[backpackSkins] allowEquip`
(`CEConfig.BACKPACK_SKINS`). KubeJS: `NCH.backpackSkin` / `backpackSkinClear`,
`server_scripts/other/backpack_skins.js` (also holds the tag). Build needs SB and Curios jars in `libs/`.
Curios note: 5.1.6.4 posts `CurioEquipEvent` with a result; DENY blocks the slot.

## 6. Offline checks

Compiles; mixin descriptors checked with javap against SB 3.20.2. Recipes not yet run (scratch server: give the result, read the NBT).

## 7. In-game test suite

- [ ] **BP-01 Smithing:** smith a Sophisticated Backpack with a Basket: a skinned backpack with contents and upgrades intact; a non-block or untagged item is refused.
- [ ] **BP-02 Clear:** craft the skinned backpack alone: the skin is gone, contents kept.
- [ ] **BP-03 Held and dropped:** held, in the inventory, dropped and in an item frame the backpack shows the basket.
- [ ] **BP-04 Placed:** placed it shows the basket, the hitbox is the basket's, it faces the backpack's facing; pick it up and the skin is kept; no upgrade, tank or display drawing.
- [ ] **BP-05 Equip switch:** switch off (default): chest slot, shift-click and Curios slot refuse the skinned backpack; switch on: both accept it and the basket is drawn on the back.

## 8. History

- 2026-10-22: built (recipes, six mixins, colour handler, config switch, Curios deny, NCH methods, tag with the basket). Not run in game.
- 2026-10-22: investigated, decisions recorded (basket only, skin hitbox, clear by crafting, no upgrade or display
  drawing, per-skin display position as future work).
- 2026-11-04: moved to Creative Equipment ([creative_equipment.md](creative_equipment.md)): `backpackskin/`, the six mixins, `[backpackSkins]` (`config/creativeequipment-common.toml`), `kubejs/data/creativeequipment/kubejs/other/backpack_skins.js`. Same behaviour.
