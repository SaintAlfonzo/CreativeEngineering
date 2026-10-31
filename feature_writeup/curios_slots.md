# Curios slots

Test prefix **CS-** · Last updated **2026-10-25** · Overall status **BUILT, untested in game**

## 0. At a glance

Curios 5.1.6.4 adds no slots itself; every slot comes from a mod (IMC or datapack). Survey of the pack on 2026-10-25: head, back,
body, necklace, ring, belt, charm, bracelet, curio, wallet (Lightman's), calendarslot (Season HUD) and a Create Goggles slot.
Nothing registered **hands**, and no mod offers a mouth or face slot. The pack now adds both.

| Part | Where |
|---|---|
| Hands slot (1, vanilla Curios hands icon) | `kubejs/data/creativeengineering/curios/slots/hands.json` |
| Face slot (1, own icon, lang "Face") | `.../curios/slots/face.json`, texture `kubejs/assets/creativeengineering/textures/slot/empty_face_slot.png`, `curios.identifier.face` in `lang/en_us.json` |
| Feet slot (1, vanilla empty boots icon, lang "Feet"), 2026-10-29 | `.../curios/slots/feet.json`, `curios.identifier.feet` in `lang/en_us.json` |
| Feet items: the 16 stepstools ([stepstool.md](stepstool.md)) | tag `curios:feet` = `#creativeengineering:stepstools`, [curiostags.js](../kubejs/server_scripts/tags/curiostags.js) |
| Player gets all three slots | `.../curios/entities/player_hands_face.json` (Feet added 2026-10-29) |
| Hands items: Falconry Glove (the only glove in the pack) | tag `curios:hands`, [curiostags.js](../kubejs/server_scripts/tags/curiostags.js) |
| Face items: Simple Hats chalk stick and six masks (tribal, cucumber eye, ghost, hockey, horse, sleep eye), Botania cosmetic masks (ancient, eerie, tiny potato), Cob Pipe | tag `curios:face`, same script |

Notes:

- Simple Hats adds every hat to `curios:head` through a mixin, so its face items also stay valid in a head slot. Removing them from
  head would need a patcherMod mixin; not done.
- The Cob Pipe (`creativeengineering:cob_pipe`, [corn_delight.md](corn_delight.md) 2.3, built 2026-10-25) is in this face tag and,
  from its own tags, in `curios:head` and `curios:mouth`. Its plan said "mouth slot first": the face slot is that slot now
  (`curios:mouth` stays unused). A right click puts it in Face first, then the head slot, then a Curios head slot.
- Wardrobe and Portable Wardrobe tag only `minecraft:air` under hands, so no other item landed there.
- Slots come from the server datapack, so they need a server restart (not just `/reload`); an existing save should pick them up
  on next join (CS-01 checks it).

## 1. Rule: every Curios item shows its slots in the tooltip

**Open (owner request 2026-10-25, not built).** Every item that can go in a Curios slot should carry a tooltip line naming the
slot(s) it fits, e.g. "Fits: Face, Head". Source of truth is the `curios:<slot>` item tags, so the line can be generated from the
tags rather than hand-written per item (client script reading the tag keys, or a patcherMod tooltip hook). Applies to the pack's
own slots and every mod's. Plan row `ui/tooltips #8` in [todo/TODO.md](todo/TODO.md); line wording goes in [tooltips.md](tooltips.md).

Owner idea (scratchpad 2026-10-29, TODO `largecontent/mod-additions #32`): Terraria-like accessories for these slots
(defense, abilities, effects). Which accessories and their design are open; nothing built.

## 2. Tests

- CS-01 The Curios screen shows a Hands and a Face slot (Face has the mask icon) on a player in an existing world.
- CS-02 Falconry Glove goes in Hands and nowhere else; nothing else is accepted in Hands.
- CS-03 Chalk stick, the six Simple Hats masks and the three Botania cosmetic masks go in Face; a Simple Hats mask also still goes in Head.
- CS-04 `/curios list` shows `hands` and `face` from `creativeengineering`.
- CS-05 Slot names read "Hands" and "Face" (no raw `curios.identifier` key).
- CS-06 Once the Cob Pipe exists: it goes in Face.
- CS-07 The Curios screen shows a Feet slot (boots icon, "Feet"); only stepstools go in it; `/curios list` shows `feet` from `creativeengineering`.
