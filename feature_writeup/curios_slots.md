# Curios slots

Test prefix **CS-** · Last updated **2026-11-02** · Overall status **BUILT (IMC fix 2026-11-01; slot line 2026-11-02), in patcherMod 1.1.07 installed 2026-11-02, not booted, untested in game**

## 0. At a glance

Curios 5.1.6.4 adds no slots itself; every slot comes from a mod by IMC. This version has **no datapack slot loader**: the 1.20
format (`data/<ns>/curios/slots/*.json`, `curios/entities/*.json`) is silently ignored. The pack first shipped the slots that way
(1.1.03, 1.1.05), so none of the three ever appeared; 2026-11-01 they moved to IMC and the JSON files were deleted. Survey of the pack on 2026-10-25: head, back,
body, necklace, ring, belt, charm, bracelet, curio, wallet (Lightman's), calendarslot (Season HUD) and a Create Goggles slot.
Nothing registered **hands**, and no mod offers a mouth or face slot. The pack now adds both.

| Part | Where |
|---|---|
| Registration of all three (IMC `register_type`, only when Curios is loaded) | patcherMod `compat/CuriosSlots.java`, hooked in `CreativeEngineering` next to `BackpackSkinCurios` |
| Hands slot (1, Curios `HANDS` preset: priority 140, Curios hands icon) | `CuriosSlots` |
| Face slot (1, priority 50 between head and necklace, own icon, lang "Face") | `CuriosSlots`, texture `kubejs/assets/creativeengineering/textures/slot/empty_face_slot.png` (Curios stitches every `textures/slot/*.png`), `curios.identifier.face` in `lang/en_us.json` |
| Feet slot (1, priority 190 between belt and charm, vanilla empty boots icon, lang "Feet"), 2026-10-29 | `CuriosSlots`, `curios.identifier.feet` in `lang/en_us.json` |
| Feet items: the 16 stepstools ([stepstool.md](stepstool.md)) | tag `curios:feet` = `#creativeengineering:stepstools`, [curiostags.js](../kubejs/server_scripts/tags/curiostags.js) |
| Player gets all three slots | IMC slots go to every player by default (no entities file in 5.1.x) |
| Hands items: Falconry Glove (the only glove in the pack) | tag `curios:hands`, [curiostags.js](../kubejs/server_scripts/tags/curiostags.js) |
| Face items: Simple Hats chalk stick and six masks (tribal, cucumber eye, ghost, hockey, horse, sleep eye), Botania cosmetic masks (ancient, eerie, tiny potato), Cob Pipe | tag `curios:face`, same script |

Notes:

- Simple Hats adds every hat to `curios:head` through a mixin, so its face items also stay valid in a head slot. Removing them from
  head would need a patcherMod mixin; not done.
- The Cob Pipe (`creativeengineering:cob_pipe`, [corn_delight.md](corn_delight.md) 2.3, built 2026-10-25) is in this face tag and,
  from its own tags, in `curios:head` and `curios:mouth`. Its plan said "mouth slot first": the face slot is that slot now
  (`curios:mouth` stays unused). A right click puts it in Face first, then the head slot, then a Curios head slot.
- Wardrobe and Portable Wardrobe tag only `minecraft:air` under hands, so no other item landed there.
- IMC slots need the patcherMod jar on both sides and a full restart; an existing save picks them up on next join (CS-01).
  A world's `serverconfig/curios-server.toml` `curiosSettings` can also define slots; the pack leaves it empty.

## 1. Rule: every Curios item shows its slots in the tooltip

**BUILT 2026-11-02 (patcherMod 1.1.07 installed, not booted; owner request 2026-10-25).** Every item that can go in a Curios
slot names the slot(s) it fits. Curios itself already prints "Slot: Face, Head" (gold, names yellow) from
`getCurioTags(item)`, the `curios:<slot>` item tags plus mixins such as Simple Hats' head rule, so the pack's own slots
get it once registered. The gap: an item with a curio capability passes that line through `ICurio.getSlotsTooltip`,
which its mod can override to hide. patcherMod `compat/CuriosSlotTooltip` (client, Forge LOW, only with Curios) adds the
same line to any curio item whose tooltip has none; CEConfig `[tooltips] curiosSlotLine`. Owner 2026-11-02: extend the
current line to all items (wording is Curios'). The stepstools' own "Worn in the Feet slot" line was dropped. Row
`ui/tooltips #8`, tests TT-40 in [tooltips.md](tooltips.md) and CS-08 below.

Owner idea (scratchpad 2026-10-29, TODO `largecontent/mod-additions #32`): Terraria-like accessories for these slots
(defense, abilities, effects). Which accessories and their design are open; nothing built.

## 2. Tests

- CS-01 The Curios screen shows a Hands and a Face slot (Face has the mask icon) on a player in an existing world.
- CS-02 Falconry Glove goes in Hands and nowhere else; nothing else is accepted in Hands.
- CS-03 Chalk stick, the six Simple Hats masks and the three Botania cosmetic masks go in Face; a Simple Hats mask also still goes in Head.
- CS-04 `/curios list` shows `hands`, `face` and `feet`.
- CS-05 Slot names read "Hands" and "Face" (no raw `curios.identifier` key).
- CS-06 Once the Cob Pipe exists: it goes in Face.
- CS-07 The Curios screen shows a Feet slot (boots icon, "Feet"); only stepstools go in it; `/curios list` shows `feet` from `creativeengineering`.
- CS-08 Hover a stepstool, a Simple Hats mask, the Falconry Glove and the Cob Pipe: one gold "Slot: ..." line each
  (Feet; Face, Head; Hands; the Cob Pipe's registered slots), never two (same check as TT-40).
