# r1103 slice A: rows not in a server batch

| ID | Reason | Evidence |
|---|---|---|
| SQ-GLD2-05 | needs owner generator run (client dumps): `gen_can_types.py` reads `/dumpfood`, which needs the client; Boot 0 did not run the generators (runner, 00:59) | scratch_queue.md SQ-CDJ7; gotchas.md "Dedicated-server dumps" |
| CQ-CWG-11 | client, pinned until wave 8+: moobloom, baby squid and dolphin models, JEI Moobloom Conversion | SQ-CWG table |
| CQ-SLT-01 | client: Mods screen names, JEI mod line, WTHIT | Salt table |
| CQ-SLT-02 | client: a real player beside a campfire cauldron for 60 s (evaporation, random ticks near a player) | Salt table |
| CQ-CNG-01 | client: real player dyeing a golem, textures and sound | SQ-CNG table |
| CQ-CNG-02 | client: Mods screen entry | SQ-CNG table |
| CQ-GLD-01 | client: creative tab, textures, glint | SQ-GLD table |
| CQ-GLD-02 | client: teleport sounds, second player, rubber-banding | SQ-GLD table |
| CQ-GLD-03 | client: JEI sequenced assembly sprites | SQ-GLD table |
| CQ-PCK-01 | client: placed model, WTHIT, hand pick-up | Pancake table |
| CQ-PCK-02 | client: tooltip after placeable_catalog.py (also needs the owner's generator run) | Pancake table |
| CQ-VTDA-01 | client: salt layer feel, JEI pages, names | SQ-CDJ7 table |
| CQ-CDM2-01 | client: names, tooltips, JEED page | SQ-CDJ7 table |
| CQ-GLD2-04 | client: names, JEI assembly | SQ-CDJ7 table |
| CQ-MRGT-01 | client: merged item art | SQ-CDJ7 table |
| CQ-BBQF-01 | client: seasoning tooltips, skewer names, Grilled line, butcher trade screen (barbeques_delight.md BBQ-17, -22 to -29) | SQ-CDJ7 table |

Server halves left out of batched rows (the row is in a batch; this part is not):

- SQ-GLD-11: the physical Create line (deployer, spout, press, belt). The batch checks the recipes (`/zq rdetail+`:
  steps, transitional `kubejs:incomplete_enchanted_golden_*`, outputs). A belt line built by commands is a long
  setup with its own failure modes; put it in the client block if the owner wants it seen.
- SQ-VTDA-13: the oven bake with a pie tin. The batch checks the baking recipes and that no grid recipe exists.
- SQ-GLD-02 glint, SQ-GLD-06 book hover: client.
- SQ-CWG-08: "spawn a cow next to a flower biome" (superflat has one biome; More Mooblooms conversion is CQ-CWG-11's).
- SQ-CWG-09: "upgrade_aquatic flares refused" has no server command that decides it (LOOK on the log only).
- SQ-SLT-04 reads counts from `/zq` and Boot 0's dump instead of a fresh `/dumpall` in boot 2 (same jars).

Rows of the SQ-CDJ7 section that are not in sliceA_sections.txt: the reruns SQ-RD-22, SQ-CDF-08 and SQ-VP-06 (the
queue lists them in that table). Not batched here; SQ-CDF-08 needs `/dumpcompost` from a client (compost is a client
dump) and `gen_compost_values.py --check`. Whoever owns the reruns should pick them up.
