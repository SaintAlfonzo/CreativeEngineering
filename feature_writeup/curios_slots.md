# Curios slots

Test prefix **CS-** · Last updated **2026-11-02** · Overall status **BUILT (IMC fix 2026-11-01; slot line 2026-11-02; mouth tag dropped and Face render 2026-11-02), in patcherMod 1.1.07 installed 2026-11-02, Creative Compat / Creative Delight changes not built into mods/, untested in game**

## 0. At a glance

Curios 5.1.6.4 adds no slots itself; every slot comes from a mod by IMC. This version has **no datapack slot loader**: the 1.20
format (`data/<ns>/curios/slots/*.json`, `curios/entities/*.json`) is silently ignored. The pack first shipped the slots that way
(1.1.03, 1.1.05), so none of the three ever appeared; 2026-11-01 they moved to IMC and the JSON files were deleted. Survey of the pack on 2026-10-25: head, back,
body, necklace, ring, belt, charm, bracelet, curio, wallet (Lightman's), calendarslot (Season HUD) and a Create Goggles slot.
Nothing registered **hands**, and no mod offers a mouth or face slot. The pack now adds both.

| Part | Where |
|---|---|
| Registration of all three (IMC `register_type`, only when Curios is loaded) | Creative Equipment `compat/CuriosSlots.java` (from Creative Common 2026-11-04), hooked in `CreativeEquipment` next to `BackpackSkinCurios` |
| Hands slot (1, Curios `HANDS` preset: priority 140, Curios hands icon) | `CuriosSlots` |
| Face slot (1, priority 50 between head and necklace, vanilla empty helmet icon, lang "Face"; own `empty_face_slot.png` dropped 2026-11-04, it rendered as the missing texture) | `CuriosSlots` (`minecraft:item/empty_armor_slot_helmet`), `curios.identifier.face` in `lang/en_us.json` |
| Feet slot (1, priority 190 between belt and charm, vanilla empty boots icon, lang "Feet"), 2026-10-29 | `CuriosSlots`, `curios.identifier.feet` in `lang/en_us.json` |
| Feet items: the 16 stepstools ([stepstool.md](stepstool.md)) | tag `curios:feet` = `#creativeengineering:stepstools`, [curiostags.js](../kubejs/server_scripts/tags/curiostags.js) |
| Player gets all three slots | IMC slots go to every player by default (no entities file in 5.1.x) |
| Hands items: Falconry Glove (the only glove in the pack) | tag `curios:hands`, [curiostags.js](../kubejs/server_scripts/tags/curiostags.js) |
| Face items: Simple Hats chalk stick, smoking pipe (2026-11-04) and six masks (tribal, cucumber eye, ghost, hockey, horse, sleep eye), Botania cosmetic masks (ancient, eerie, tiny potato) | tag `curios:face`, same script (Simple Hats rows in Creative Compat's `tags/curiostags.js`) |
| Cob Pipe in Face | tag `curios:face` in Creative Delight's `corn_delight/tags.js` (the pipe's owner), 2026-11-02 |
| Hats worn in Face drawn on the player | Creative Compat `mixin/SimpleHatsFaceSlotMixin` (client, gated on Simple Hats), 2026-11-02 |

Notes:

- Simple Hats adds every hat to `curios:head` through a mixin, so its face items also stay valid in a head slot. Removing them from
  head would need a patcherMod mixin; not done.
- The Cob Pipe (`creativeengineering:cob_pipe`, [corn_delight.md](corn_delight.md) 2.3, built 2026-10-25) is in `curios:face`
  and `curios:head`. A right click puts it in Face first, then the head slot, then a Curios head slot. Its old
  `curios:mouth` tag was dropped 2026-11-02 (`gameplay/misc #19`): Curios builds the "Slot:" tooltip line from every
  `curios:*` item tag, registered or not, so the pipe listed a Mouth slot that does not exist.
- Rendering (`gameplay/misc #20`, 2026-11-02): Curios draws an item in any slot only through a renderer registered for
  the item. Botania's masks and the Cob Pipe have one and draw from Face. Simple Hats instead draws hats from its own
  layer and only for the slot named `head`, so a mask in Face was invisible; Creative Compat's
  `SimpleHatsFaceSlotMixin` makes that check accept `face` too (cosmetic stack and the slot's render toggle as for head).
- Feet slot not showing (scratchpad 2026-11-02, `gameplay/misc #19`): the last played session (`logs/latest.log`, 10-31)
  predates the IMC registration (2026-11-01, patcherMod 1.1.07 installed 2026-11-02 00:18), so the old datapack files
  were in use and no pack slot existed. No code change: CS-07 on the 1.1.07 jar is the check.
- Wardrobe and Portable Wardrobe tag only `minecraft:air` under hands, so no other item landed there.
- IMC slots need the patcherMod jar on both sides and a full restart; an existing save picks them up on next join (CS-01).
  A world's `serverconfig/curios-server.toml` `curiosSettings` can also define slots; the pack leaves it empty.

## 1. Rule: every Curios item shows its slots in the tooltip

**BUILT 2026-11-02 (patcherMod 1.1.07 installed, not booted; owner request 2026-10-25).** Every item that can go in a Curios
slot names the slot(s) it fits. Curios itself already prints "Slot: Face, Head" (gold, names yellow) from
`getCurioTags(item)`, the `curios:<slot>` item tags plus mixins such as Simple Hats' head rule, so the pack's own slots
get it once registered. The gap: an item with a curio capability passes that line through `ICurio.getSlotsTooltip`,
which its mod can override to hide. Creative Equipment `compat/CuriosSlotTooltip` (client, Forge LOW, only with Curios;
from Creative Common 2026-11-04) adds the same line to any curio item whose tooltip has none;
`creativeequipment-common.toml` `[tooltips] curiosSlotLine` (was Common's `[tooltips]`; the move resets a changed value). Owner 2026-11-02: extend the
current line to all items (wording is Curios'). The stepstools' own "Worn in the Feet slot" line was dropped. Row
`ui/tooltips #8`, tests TT-40 in [tooltips.md](tooltips.md) and CS-08 below.

Owner idea (scratchpad 2026-10-29, TODO `largecontent/mod-additions #32`): Terraria-like accessories for these slots
(defense, abilities, effects). Which accessories and their design are open; nothing built.

## 2. Tests

- **r1102b 2026-11-02** (scratch server; verdicts in `perf/runs/r1102b/` HANDOVER.md and the slice judged.md): rerun pending (batch fault) SQ-CSI-01.
- **r1103 2026-11-03** (scratch server; verdicts in `perf/runs/r1103/` FINAL.md and the slice judged.md): PASS SQ-CSI-01.

- CS-01 The Curios screen shows a Hands and a Face slot (Face has the helmet icon, not the missing texture) on a player in an existing world.
- CS-02 Falconry Glove goes in Hands and nowhere else; nothing else is accepted in Hands.
- CS-03 Chalk stick, smoking pipe, the six Simple Hats masks and the three Botania cosmetic masks go in Face; a Simple Hats mask also still goes in Head.
- CS-04 `/curios list` shows `hands`, `face` and `feet`.
- CS-05 Slot names read "Hands" and "Face" (no raw `curios.identifier` key).
- CS-06 Once the Cob Pipe exists: it goes in Face.
- CS-07 The Curios screen shows a Feet slot (boots icon, "Feet"); only stepstools go in it; `/curios list` shows `feet` from `creativeengineering`.
- CS-08 Hover a stepstool, a Simple Hats mask, the Falconry Glove and the Cob Pipe: one gold "Slot: ..." line each
  (Feet; Face, Head; Hands; the Cob Pipe's registered slots), never two (same check as TT-40).
- CS-09 Hover the Cob Pipe: the slot line reads Face, Head (no Mouth). Right click it with an empty Face slot: it goes in Face.
- CS-10 Wear a Simple Hats mask (hockey mask) in Face: drawn on the head in third person; toggle the slot's render
  button off: hidden. A Botania cosmetic mask and the Cob Pipe in Face are drawn too.
