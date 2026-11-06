# r1102b rerun: rows not in r_A.qb / r_B.qb / r_B2.qb

## No server command decides them

| ID | Reason |
|---|---|
| SQ-RD-03, SQ-RD-03a, SQ-DF-07, SQ-RSP-14 | NOT RUN in s2: Rustic 1.3.0 and Delightful 3.4.1 do not boot beside the current Creative Delight (unbound placed feature), so no old-world save exists to load. Needs an old save made by an older pack build (client or a pre-nest server), not a command |
| SQ-CDF-06 (live half) | Baking in the oven, grinding in the mortar and filling the milk jar are client uses (GUI, held use); r_A.qb reruns the recipe half only |
| SQ-BBQ-06 (flip half) | Flipping needs a sneak use on the grill entry: already in s3_skip.md as a client half |

## Need a data or code fix first (FAIL, not a batch fault)

| ID | Reason |
|---|---|
| SQ-RD-22 | comfort_nourishment.json predates Rustic 1.7.0: regenerate with `/cegen run comfort_nourishment`, then rerun |
| SQ-CDF-08 | gen_compost_values.py --check: 21 compostables with no row; run `--add-new` and write the 2 stale files |
| SQ-FSD-02 | the world does not load without the Farmer's Structures 1.0.6 donor (6 unbound structure ids): code fix and a fresh world first |

## Already decided from the first-run logs (no rerun)

Slice 2, real PASS from the logs (regex fixed in place): SQ-FR-02, -FR-04, -RD-07, -RD-13, -RD-17, -RD-19, -RSP-02,
-RSP-13, -CN-02, -CN-05, -CDF-01, -CDF-05, -CDQ-03. Slice 3, real PASS (s3/judged.md "BATCH-FAULT, real PASS"): BM-01,
ROLL-01, LGM-01, CDFW-01, MG-21, FFH-02, ABD-04, VDF-05, RDX-03, -06, -07, -12, ATC-02, -04, CDB-09, LGM-03, -04b, -08,
NDM-04.

## Changed scope in the rerun (same id, narrower check)

- SQ-RD-08: only the squid half (ancient city, sky village and trader halves passed in s2).
- SQ-EN-03: only void pepper spread / fruit / drops (soils and growth passed in s3).
- SQ-NDM-05: only the drop of the broken open-sky colony (the other three cases passed).
- SQ-POT-04v (new id): the hand check on the vanilla pot with Gossypium and IE hemp seed; SQ-POT-04 itself stays FAIL
  (beautify hanging_pot loses the plant).
- SQ-RD-10b (new id): the plains half of SQ-RD-10, split so its wheat count does not mix with the savanna ones.
