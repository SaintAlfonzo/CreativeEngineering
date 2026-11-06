# r1102b slice 5 rerun: setup (boots RA, RA2, RA3)

Rows: the BATCH-FAULT and OPEN rows of boots A and A2 listed under "Rerun" in perf/runs/r1102b/s5/judged.md, with its
"Batch fixes" applied. 27 queue rows: s5_R_A.qb 23 (+ S5RA-00, S5RA-PSF-bot), s5_R_A2.qb 3 (+ S5RA2-00),
s5_R_A3.qb 1 (+ S5RA3-00). Generated once from s5_A.qb / s5_A2.qb (rows copied, then edited); edit the .qb files
directly from here on. Use a new results dir (RES = perf/runs/r1102b/s5r) so the first run's evidence stays in s5/.
Captures still start with `S5` (S5H is captured again in each setup row).

Probe fixes in tools/scratch_query/s5_probe.js (copy again; the first run's copy is stale):
- `ingStacks(g)`: Ingredient stacks via `getStacks().toList()` (getItems is hidden in this Rhino build); used by
  `potrecipe`, `jarrecipe`, `selfloop`.
- `trim`: `r.nbt` for the tag (getTag is invisible).
- `spout`: the FluidStack is built with `FluidStack.loadFluidStackFromNBT({FluidName, Amount})` (the constructor is
  ambiguous for Architectury fluids).
- `selfloop`: walks `RECIPE_TYPE.keySet()` and `getAllRecipesFor(type)`, skips any `*extruder*` namespace; never
  `r.getType()` per recipe. Prints `selfloop types walked N`.

Jars: check mods/ times against the sources again right before the boot (gotchas.md). One game JVM at a time: boot only
after the running slice-5 B/B2 server is stopped and `game_jvms.sh empty` says so.

## Boot RA: s5_R_A.qb (fresh group A superflat, about 50 min)

    bash "$INST/tools/scratch_query/prep_srv.sh" "$OUT" s5ra A difficulty=normal spawn-monsters=true spawn-animals=true spawn-protection=16
    cp "$INST/tools/fungi_probe/zcount.js"     "$OUT/srv/kubejs/server_scripts/zz_count.js"
    cp "$INST/tools/scratch_query/s5_probe.js" "$OUT/srv/kubejs/server_scripts/zz_s5_probe.js"
    cd "$OUT" || exit 1
    Q="python3 $INST/tools/scratch_query/qrun.py toml"
    # if srv/config has no vintagedelight-common.toml, set these in creativedelight-common.toml under vintage.pickling_jar
    $Q srv/config/vintagedelight-common.toml pickling_jar remainderBuffer true
    $Q srv/config/vintagedelight-common.toml pickling_jar stallWhenFull false
    for k in secondFruitMinutes thirdFruitMinutes fourthFruitMinutes; do $Q srv/config/creativedelight-common.toml stem_fruit $k 0; done
    $Q srv/config/creativedelight-common.toml stem_fruit maxFruits 3
    $Q srv/config/creativedelight-common.toml stem_fruit fertileSoilBoost false
    boot; wait for 'Done ('; qrun.py run OUT batches/r1102b/s5_R_A.qb RES

What changed against the first run:
- Mob spawning: S5RA-00 sets `doMobSpawning false` and kills every non-player entity; every crop row (SQ-PSD-01,
  SQ-PSF-02, -04, -07, -08a, -08) kills crows and slimes again first. SQ-PSU-04 turns spawning on (and `difficulty
  hard`) for itself and off again at its end.
- SQ-PSF-08a starts right after setup; its 600 s wait runs under the probe, jar, click and pump rows (none touches
  randomTickSpeed). Ages are read as one `say S5F8 <stem> age N` line per stem (no shared capture line), every minute
  in SQ-PSF-08 and finally at 600 s or later (`S5F8F` lines decide).
- SQ-VTD8-02: (c) only, slot 9 full at 64 glass bottles (they stack to 64 here).
- SQ-JFP-01/02: creative motor placed with `{ScrollValue:64}`, motor and pump `Speed` read back (expect 64.0f),
  60 s per phase.
- SQ-DRK-06: only the drink_fruits tags (lemon, orange, hamimelon); the rest PASSed in the first run.
- SQ-PSF-07: 120 s at randomTickSpeed 300.
- SQ-PST-03 to -11b: `season set MID_SPRING` / `MID_WINTER`, plus `reject Enum constant must be one of`. SQ-PST-11b
  rebuilds the grass ring and mycelium pad before each counted run.
- SQ-PSU-04: control pass first (no Phantom Bane, 6 min, phantoms expected near both bots), then the bane pass (6 min).
  A control with no phantom near bot 1 leaves the row undecided.
- SQ-FCB-07: bot 3 blocks away, its Inventory dumped after the two breaks (boards = drops + picked up), `zq uses
  minecraft:carrot` for the cutting recipe. Still last (/reload).

## Boot RA2: s5_R_A2.qb (same level, about 12 min)

    scratch_server.sh stop OUT; game_jvms.sh empty
    Q="python3 $INST/tools/scratch_query/qrun.py toml"
    $Q "$OUT/srv/config/creativedelight-common.toml" vintage.pickling_jar remainderBuffer false
    $Q "$OUT/srv/config/creativedelight-common.toml" stem_fruit fertileSoilBoost true
    $Q "$OUT/srv/config/creativedelight-common.toml" qol cookwarePickup false
    boot; qrun.py run OUT batches/r1102b/s5_R_A2.qb RES

SQ-PSF-08b uses new stems at x +52 / +56 (the RA pair stays at +44 / +48), reads every minute for 6 min; the check is
the 6 min pair (mulch 7, farmland below 7), the 4 min reading is for the look. SQ-FCB-08b empties the bot's offhand
(starter-kit shield) before the sneak click. SQ-VTD8-02b reads `inventory.Items[{Slot:7}]` (int slots).

## Boot RA3: s5_R_A3.qb (same level, about 6 min)

    scratch_server.sh stop OUT; game_jvms.sh empty
    python3 "$INST/tools/scratch_query/qrun.py" toml "$OUT/srv/config/creativedelight-common.toml" vintage.pickling_jar overflowEjectDelayTicks 0
    boot; qrun.py run OUT batches/r1102b/s5_R_A3.qb RES

SQ-VTD8-02c with the delay set before the boot (the first run's live edit was not read), A2 config otherwise.

## Not rerun

- SQ-PSU-06: real FAIL, fix not applied yet (`kubejs/data/creativedelight/kubejs/fruitsdelight/recipes.js` lines 50 and
  59 still pass the glass bottle). Rerun its s5_A.qb row after the fix (no group A config needed, plain boot).
- SQ-PSU-09: owner decision first (Nourishment vs the Comfort/Nourishment rule for vinegar_jar).
- SQ-PSU-03, SQ-PSF-09: BATCH-FAULT but proven PASS by the first run's log (judged.md).
- No rerun row needs a client: every one is decided by server commands and logs.
