# Scratch run 2026-11-07

## 1. r1107tp: Tempad port (SQ-TP)

Owner request 2026-11-07 10:44: "execute the scratch test queue for tempads". Scope: the two SQ-TP sections of the queue
(SQ-TP-01 to -30 server rows); client rows CQ-TP-01 to -15 and the joined-client rows SQ-TP-17, -29 at the end if time
allows. Targeted run, no perf session (12 GB free on /).

- Jar: `creativecompat-1.0.0.jar` of 11-07 10:31 from build/libs (prep_srv.sh swap; `mods/` holds the 11-06 22:43 one),
  `tempad_upstream/tempad-1.21.1-3.0.4-all.jar` linked by scratch_server.sh.
- Left out of every srv: Immersive Portals 2.3.7, Portal Gun, Portal Gels, loadingscreentips (unlisted in mod_sides.md,
  r1106spf / spfx crash with Serene Seasons; the Tempad does not need them).
- Batches `tools/scratch_query/batches/r1107tp/`, results `perf/runs/r1107tp/<boot>/`, srv in the session scratchpad `tp/`.

### Run notes

- 10:46 claim, preflight: no JVM, 12 GB free. a1 batch (SQ-TP-01a, -02, -10, -18a, -27a); booting a1.
- 10:48 a1 Done (10.8 s). Batch a1: SQ-TP-01a, -02, -10, -18a, -27a all judged PASS (the FAILs of -10, -18a, -27a were
  BATCH-FAULTs: the rdetail output sat in SQ-TP-02's segment; grep -c exit 1).
- 10:50-10:56 new probe `tools/scratch_query/ztp_probe.js` (`/ztp`, drives the Tempad c2s packet handlers on a perfBot
  bot) loaded with /reload; SQ-TP-04 run by hand on a1 (commands recorded in batch a2 for reruns).
- 10:56 a1 CRASHED (tooling): `/ztp respawn` (PlayerList.respawn) on a bot that perfBot had already relogged after
  `/kill`; perfBot ticked the stale object, DistanceManager NPE. Subcommand removed; gotcha added. Killed pid 3073448.
- 10:57 booting a2 (same save, restart).
- 11:03 a2 batch: SQ-TP-03 PASS, -05 PASS (relog only, no Clone), -07 PASS (1 -> 51 chronons in 2 min). Stopped; tempad-common.toml: require_location_upgrade true, cost_per_door 10, time_in_world 100, tempad capacity 2000; booting a3.
- 11:05 a3 batch: SQ-TP-06 PASS, -30 PASS (server half; the closing-time regex was a BATCH-FAULT, door life 100 = age 20 + closing 80). Stopped; toml back to defaults + allow_location_saving false, allow_intradimensional_travel false; booting a4.
- 11:09 a4 batch: SQ-TP-27b PASS, -22a PASS, -22d PASS (leaving), -22e PASS (entering); -22b, -22c BATCH-FAULT (Forge did not reload the toml edited while running: known gotcha). Booting a5 for the reruns.
- 11:11 a5 batch: SQ-TP-22b2 PASS (interdimensional config), -22c2 PASS (intradimensional tag). Stopped; toml defaults, tag removed, spawn-animals=true (pig rows); booting a6 for the block rows (21, 23-26, 28, phase 2-3).
- 11:11 a6 up (toml defaults, spawn-animals=true). SQ-TP-21 vertical case run by hand (offsets 2/1/-3 on a south pad ->
  saved x -2 y 1 z -3, door at pad + turned offset). `/ztp` gained blocks, break, pickup, markerui, hand, openapp,
  `cmdlog.sh` added (hand steps print only their own log lines).
- 11:13 **FINDING (real bug): all five Tempad block loot tables are dropped by the translator** (debug.log "Tempad upstream
  data dropped: data/tempad/loot_table/blocks/<x>.json (names tempad:blocks/<x>, not registered in this phase)"):
  the 1.20 `random_sequence` key holds the table's own id, and `firstUnregistered` checks it as an item/block id
  (`NON_REGISTRY_KEYS` = upgrade, type only). Survival breaks of workstation, marker, chronomark, metronome, projector
  drop nothing (`/loot spawn ... tempad:blocks/<x>` = 0 items). Not fixed (run rule 2). Workaround for the rest of the
  run, scratch srv only: the five upstream JSONs unchanged in `srv/kubejs/data/tempad/loot_tables/blocks/` (what the fix
  would load), /reload.
- 11:14-11:18 SQ-TP-24 by hand on a6 (loot workaround in): marker placed, Marker B named (markerui), card written
  (portal_target id 85c537c6, provider tempad:markers); survival break dropped the marker with anchor_id 85c537c6,
  name, colour, owner; anchors file emptied; re-placed at 25 -60 0 -> anchors = {85c537c6 -> 25 -60 0} (fix 4 works);
  a stack copy with the same anchor_id placed at 25 -60 6 got its own id 42e7d8c4, original kept 85c537c6; Tempad
  markers section lists both, door to Marker B opens at the new place. SQ-TP-09 part: workstation door persisted
  ~300 s while powered, docked Tempad 5000 -> 2250 (~10/s = cost_to_persist). Code read: AbstractMarkerBe saves its
  anchor UUID under NBT key "id", which BlockEntity.saveWithId overwrites with the type id ("tempad:timedoor_marker";
  `/data get block` shows no UUID): suspect the marker loses its id on every chunk reload; SQ-TP-25 checks it.
- 11:20 restart (a7 = same save). SQ-TP-21 restart half PASS (powered workstation door same entity, same place,
  PlacementSettings x -2 y 1 z -3 kept). SQ-TP-25: both markers listed by name after the restart and a door to Marker B
  opens, but **FAIL**: breaking Marker B after the restart left anchor 85c537c6 -> 25 -60 0 in the anchor file and the
  drop had no anchor_id (cards dead after a re-place). Cause confirmed: AbstractMarkerBe's anchor UUID key "id" is
  overwritten by BlockEntity.saveWithId's type id; load() swallows UUID.fromString's exception, id = null, onRemove
  skips points.remove (needs marker.id != null). Every chunk reload does the same, not only restarts.
- 11:20-11:27 by hand on a7 (scratch tags under srv/kubejs/data/tempad, srv only): SQ-TP-21 floor case PASS
  (`tempad:floor` x -2 y 1 z -3; docked portal_offset leftRight 2 upDown 1 forwardBack -3 isUpright 0); SQ-TP-09 part:
  the docked Tempad ran dry (0) and the powered door closed. SQ-TP-23 PASS (east-facing door to a north-saved
  location: item and pig leave moving north, pig rotation -180; Nether: item motion kept unrotated, east).
  SQ-TP-26 PASS (golem back through the exit door, same dimension and Nether, 1 golem after; 64 cobblestone and a
  minecart arrive whole; pig in a scratch teleporting_not_supported tag not moved while a golem beside it is).
  SQ-TP-28 PASS (generator -> cell 6 in 15 s; cell 2000 + battery 4000 -> empty Tempad 6000 at once; creative
  chronometer refills the Tempad after each of 5 doors; blacklisted Tempad stays 0 beside a full cell).
- 11:27-11:37 by hand on a7: SQ-TP-08 server half: placement refused with a block on the terminal side (use FAIL),
  placed with room (pad south-facing, terminal at x+1); survival break of either half removes both and drops the
  workstation + docked Tempad. **FAIL: creative break of the pad also drops both** (reproduced twice, items cleared
  first, playerGameType 1). Likely cause: WorkstationBlock.playerWillDestroy (creative) clears the terminal with
  setBlock flags 35; the neighbour shape update (flags & -34 = 2) runs the pad's own updateShape (terminal side AIR ->
  AIR), and Block.updateOrDestroy destroys the pad with drops (getDrops adds the docked Tempad) before the creative
  removal. Vanilla double blocks avoid it through loot conditions on the other half; check upstream before fixing.
  Pick-block is a client check.
  SQ-TP-09 server half PASS: player_teleport upgrade installed through the pad (Recipe tempad:player_teleport,
  MaxDownloadTime 80, upgrade in installed_upgrades), duplicate refused (item kept; the PASS falls through to the
  empty-hand take-out, upstream PASS_TO_DEFAULT behaviour), docked Tempad and portal settings survived the a7 restart.
  SQ-TP-12 part: card written from the Teleport menu with a stack of 2 on the cursor -> one written card back, one
  blank left (probe `carry`/`card`). SQ-TP-13 server half PASS: projector placed with shift-use (faces the player),
  card in, swap returns the old card, empty-hand use takes it out, shift + empty hand picks it up with chronons and
  card, re-place keeps both, pulse -> glitching door, cost 1000 (2500 -> 1500), second pulse while open: no door, no
  cost; survival break drops it with chronons and card; item use with a card opens a glitching door from the hand
  (1500 -> 500). Drops scatter more than 8 blocks here: `/ztp pickup` radius now 24.
- 11:38-12:05 by hand on a7: SQ-TP-12 server half PASS (written card used with a carried wallet goes in; wallet section
  `tempad:wallet` listed, door to it opens; wallet use opens tempad:wallet menu; WalletInventory.isItemValid needs a
  written card). Note: a wallet whose 18 slots all hold cards still takes an identical card (cards stack to 16 and the
  inventory has no slot limit): upstream's WalletInventory is the same, so "full" = no slot can take it; row wording.
  SQ-TP-11 PASS (2nd bot qb with player_teleport upgrade: qa's broadcaster listed as tempad:player until toggled off,
  enabled 0b; screening device cycles ftbteams -> public -> off, only public lists qa). SQ-TP-20 PASS (party tp_qa of
  qa + qb through TeamManager, `/ztp ftbparty`; device on tempad:ftbteams: qb lists qa, qc does not; the FTB party
  commands do not parse for `execute as` a bot). SQ-TP-14 server half PASS (ceiling marker up=false yoffset -2;
  chronomark yoffset 9 -> 5, angle 45, public; red dye recolours, not consumed; qb lists the public marker and
  chronomark at y -55; qb renames while unlocked, refused once qa locks it private, which also hides it; qb cannot
  place a marker owned by qa; restart half: see SQ-TP-25). SQ-TP-15 PASS with a row correction: a booting metronome
  never reads its slots (the tick returns while bootTime > 0, upstream bytecode identical); it boots from 2000
  chronons pushed into its block capability (a neighbouring chronon block; probe `bchr`), then pool registered after
  100 ticks, slots 1-4 drain into the pool, 5-8 fill, two metronomes sum to 12000, a neighbouring projector receives,
  breaking one drops it with its share and capacity falls back to 6000. SQ-TP-16 server half PASS (history newest
  first with dimension markers; backtrack 3000 -> 2500, Nether -> overworld, later entries dropped; twister stacked on
  the Tempad by inventory right-click keeps 2222 chronons and its name, Timeline app appears; right-click on an empty
  slot detaches it with both).
- 11:52 SQ-TP-19 server half PASS (Curios: bracelet, belt, 3 charm slots; belt Tempad is the tempad:curios context, its macro door cost it 1000; curios tags: Tempad bracelet+belt, wallet belt+charm, cells/battery charm, chronometer bracelet+charm). Stopped; 15 jars (Curios, FTB Teams, WTHIT and their hard dependents: Botania, Applied Botanics, kubejs_botania, charm of undying, corpse curios compat, elytra slot, shulker box slot, simple hats, FTB Chunks, FTB Quests, Quests Additions, FTBQuestsOptimizer) moved out of srv/mods (wthitharvestability is not in the srv); booting a8 (same save) for SQ-TP-18b.
- 11:54 a8 (first try CRASHED: Wardrobe + PortableWardrobe use Curios without declaring it; both moved out, rebooted): SQ-TP-18b PASS (no class-loading errors naming tempad/curios/ftbteams/waila, knowledge recipes load, screening device cycles public -> off with no ftbteams option); SQ-TP-15 restart half PASS (metronome pool count 1, capacity 6000 kept).
- 11:55 a9: srv/tempad_upstream symlink renamed away: SQ-TP-01b PASS (FML stops: 'Tempad (tempad) has failed to load correctly', IllegalStateException naming the 'tempad_upstream' folder, the missing jar path and the Modrinth link). Killed pid 3191073; symlink and compat jars restored in srv. Server rows done; no JVM up.

### Verdicts (r1107tp, server rows)

Driven by perfBot bots and `/ztp` (tools/scratch_query/ztp_probe.js), which calls the Tempad's c2s packet handlers in
place of the screens. Action-bar messages, screens, rendering, sounds and pick-block are client checks (CQ rows).
"Server half" = everything in the row a server can observe.

| ID | Result | Evidence |
|---|---|---|
| SQ-TP-01 | PASS | a1: Done, 0 tempad error lines, tempad_upstream data pack listed; a9: FML stop with the folder, jar path and link |
| SQ-TP-02 | PASS | all phase-1 recipes load, forge: tags / items only, no upgrades/new_location while the option is off |
| SQ-TP-03 | PASS | three locations listed after a restart; Tempad use opens tempad:teleport |
| SQ-TP-04 | PASS | door cost 1000, carries bot / golem / armour stand, same dimension and Nether; macro refused during the 40-tick cooldown; door gone after 200 ticks (age + closing = 200) |
| SQ-TP-05 | PASS (server) | pin and macro after a restart and after a death (perfBot relog: Clone path not covered, needs a client death) |
| SQ-TP-06 | PASS (server) | New Location hidden without the upgrade, shown with it (installed by NBT); upgrade recipe loaded; creative tab is CQ |
| SQ-TP-07 | PASS | carried generator: Tempad 1 -> 51 in 2 min |
| SQ-TP-08 | **FAIL** | placement refused / allowed, survival break of either half drops both; **creative break of the pad drops the workstation and the docked Tempad**; pick-block is CQ |
| SQ-TP-09 | PASS (server) | upgrade install (Recipe, 80-tick download, installed_upgrades), duplicate refused, door at the offsets while powered, ~10 chronons/s, closes when dry, survives a restart; the no-chronon warning is CQ |
| SQ-TP-10 | PASS | all phase-2 recipes; wallet takes forge:leather |
| SQ-TP-11 | PASS | broadcaster / screening device listing for a second bot with the player teleport upgrade |
| SQ-TP-12 | PASS (server) | card written from a stack of 2 (one back), wallet insert, wallet section and door, wallet menu; "full wallet" stacks identical cards (upstream: no slot limit) |
| SQ-TP-13 | PASS (server) | projector placement, card swap / take-out, shift pick-up and survival break keep chronons + card, pulse door (glitching, 1000), no second door while open, hand use |
| SQ-TP-14 | PASS (server) | marker up / down, chronomark clamp 5, angle, access, dye, second-player listing, lock, owner check on placement (restart: see 25) |
| SQ-TP-15 | PASS (row corrected) | boot only from chronons pushed into the block capability (upstream: slots not read while booting); pool, slot flow, summed capacity, neighbour push, break share, restart |
| SQ-TP-16 | PASS (server) | history newest first, backtrack 500 across dimensions dropping later entries, twister stacking on / off keeps chronons and name, Timeline app; low-chronon text is CQ |
| SQ-TP-17 | not run | needs a dedicated server with a joined client |
| SQ-TP-18 | PASS | a1 with Curios / FTB Teams / WTHIT (Curios slot data dropped only); a8 without them and their 15 dependents: no class errors, recipes load, no ftbteams access option |
| SQ-TP-19 | PASS (server) | bracelet, belt, 3 charm slots; belt Tempad context + macro + cost; curios item tags; worn rendering is CQ |
| SQ-TP-20 | PASS | party member lists the carrier, outsider does not |
| SQ-TP-21 | PASS | vertical and floor doors at the facing-turned offsets, per-axis x / y / z saved (-2, 1, -3 on a south pad), same after a restart |
| SQ-TP-22 | PASS | five refusal cases with their keys, chronons unchanged (22b / 22c needed restarts: BATCH-FAULT in a4) |
| SQ-TP-23 | PASS | same dimension: item and pig leave along the exit facing (north); Nether: motion unrotated |
| SQ-TP-24 | PASS | break + re-place adopts the stored anchor id at the new place, one entry; a copied id gets its own id |
| SQ-TP-25 | **FAIL** | after a restart the marker has lost its anchor id: breaking it leaves the anchor entry, the drop has no anchor_id (cards dead after a re-place) |
| SQ-TP-26 | PASS | back through the exit door (both dimensions), 64-stack and minecart whole, tagged pig not moved, no duplicates |
| SQ-TP-27 | PASS | every recipe and item; location saving off hides New Location and its hotkey |
| SQ-TP-28 | PASS | generator -> cell, cell + battery -> Tempad, creative chronometer refills, blacklist tag |
| SQ-TP-29 | not run | needs a dedicated server with two joined clients |
| SQ-TP-30 | PASS (server) | capacity 2000, cost 10, door life 100 from the toml; client sync is CQ |
| CQ-TP-01..15 | not run | client block offered to the owner |

### Findings (r1107tp)

1. **Block loot tables dropped by the translator** (all five: workstation, timedoor_marker, chronomark, metronome,
   timedoor_projector). `UpstreamTranslator.firstUnregistered` treats the 1.20 `random_sequence` value
   (`tempad:blocks/<x>`) as an item / block id; `NON_REGISTRY_KEYS` holds only `upgrade`, `type`. Fix: add
   `random_sequence` (or drop the key in loot conversion). Without it every survival break of those blocks drops
   nothing (projector excepted: its getDrops builds the stack itself). TODO `gameplay/tnt-and-portal`.
2. **Marker anchor id lost on every load** (SQ-TP-25): `AbstractMarkerBe` saves the UUID under `id`, which
   `BlockEntity.saveWithId` overwrites with the type id; `load` swallows the parse error. Fix: another key (e.g.
   `anchor_id`), reading the old `id` only when it parses as a UUID. Also explains part of PORT_NOTES 6.16.15.
3. **Creative break of the workstation pad drops it and the docked Tempad** (SQ-TP-08): shape update from the
   terminal removal destroys the pad with drops (see run notes 11:27). Check upstream 3.0.4 before fixing.

## 2. r1107tp2: Tempad loot tables, marker anchor id, creative workstation break (SQ-TP-31 to -39)

Owner request 2026-11-07 12:05: "execute the latest tempad scratch tests". Scope: the queue section "SQ-TP Tempad port:
loot tables, marker anchor id, creative workstation break" (SQ-TP-31 to -39) plus the reruns it names (break steps of
SQ-TP-08, -13, -14, -15; SQ-TP-24, -25). CQ-TP-16 is a client row: not run. Targeted run, no perf session (12 GB free;
JFR_MAX 200m per boot).

- Jar: `creativecompat-1.0.0.jar` of 11-07 12:01 from build/libs (prep_srv swap; `mods/` holds the 11-06 22:43 one).
  No scratch loot tables in srv. Left out: Immersive Portals, Portal Gun, Portal Gels, loadingscreentips (as r1107tp).
- srv in the session scratchpad `tp/`, level r1107tp2, group A; bot qa; everything by hand with `cmdlog.sh` + `/ztp`.

### Run notes

- 12:05 claim, preflight: no JVM, 12 GB free. 12:07 boot b1 Done (10.6 s).
- 12:08 SQ-TP-31 PASS: debug.log "Tempad upstream data dropped" lists only advancements, the NeoForge loot modifier
  list and the two curios files, no loot_table; `/loot spawn` of the five tables "Dropped 1" each; no
  "Couldn't parse loot table tempad".
- 12:09-12:11 SQ-TP-32 PASS (bare hand and iron pickaxe): one item per block; workstation + docked Tempad; marker and
  chronomark keep name, colour, owner, anchor_id; metronome `chronon_content` 40 (its pool share); one projector with
  1500 chronons and the card; no workstation_child item. Tooling: `pbot hold` puts a FRESH stack in slot 0 (it does not
  select an inventory stack), so charge with `/ztp setchr qa 0 N` after the hold.
- 12:12-12:16 SQ-TP-33: terminal-half survival break PASS (both gone, one workstation + the Tempad). TNT: marker 6/6
  dropped one item and its anchor entry went; workstation and metronome are blast-proof (resistance 1200, as upstream
  strength(3, 1200)), so the row's TNT step cannot apply to them: row wording, not a bug.
- 12:17-12:25 SQ-TP-34 PASS: creative break of pad, then of terminal (docked Tempad): both halves gone, 0 item
  entities; powered (door open): pad break leaves 0 items, the door runs out its closing time (<= 6 s here, persist
  stops); no exception in latest.log.
- 12:25-12:31 SQ-TP-35 PASS: survival powered break of pad and of terminal: one workstation + the docked Tempad each
  time; placement refused (use FAIL) with stone on the terminal spot. Note: in the powered terminal case the dropped
  Tempad fell into the open timedoor on the pad and came out at the destination (found intact at 20 -60 21): door
  behaviour, the drop itself is right.
- 12:13-12:16 SQ-TP-36 PASS: marker "Marker R" (40 -60 0) and chronomark "Chrono R" (44 -60 0) named, a card written
  from each (card ids 0b6ae395 / 6e77e623 = the blocks' `anchor_id`; `id` = the type id); save-all, stop, boot b2
  (Done 4.3 s): both blocks still hold the same `anchor_id`, anchors file 2 entries, `/ztp locs` lists both under
  tempad:markers.
- 12:17-12:19 SQ-TP-37 PASS (= SQ-TP-25 rerun PASS): survival break of Marker R after the restart removed its entry;
  drop has anchor_id 0b6ae395, name, colour, owner; re-placed at 60 -60 0 (main hand set to the drop's exact NBT:
  1.19.2 `item replace ... from entity` has no `contents` slot for item entities) -> entry 0b6ae395 -> 60 -60 0; Tempad
  door opens there; a projector with the card (pulse) opens a glitching door to 60 -59.5 -0.5, 2000 -> 1000.
- 12:20 SQ-TP-38 PASS: marker at 300 -60 300 (forceloaded, named, card 118b2c75), forceload removed, bot 500 blocks
  away 30 s, back: anchor_id kept; break removed the entry, drop keeps anchor_id. (The unload is inferred: 1.19.2 has
  no `execute if loaded`.)
- 12:21 SQ-TP-39 PASS (op qa): item with the standing marker's `anchor_id` -> new id cae9a01c; item with
  `BlockEntityTag:{anchor_id:...}` -> new id decbf73c; the standing marker keeps 0b6ae395 and its door still opens to it.
- 12:22 SQ-TP-24 rerun, chronomark half PASS: Chrono R broken (entry gone, drop keeps anchor_id 6e77e623 and name),
  re-placed at 24 -60 0 -> entry moves there, door opens there. Marker half = SQ-TP-37 and -39. Reruns of SQ-TP-08,
  -13, -14, -15 break steps covered by SQ-TP-32..35 and -37 (workstation, projector, marker, metronome). Side notes,
  not row checks: a marker re-placed from its item reads `Locked: 1b`, and its y offset / angle return to defaults
  (the item carries name, colour, owner, id only; as r1107tp's SQ-TP-14 noted).
- 12:24 stopped; no JVM up. latest.log: no tempad error or exception lines across both boots.

### Verdicts (r1107tp2, server rows)

| ID | Result | Evidence |
|---|---|---|
| SQ-TP-31 | PASS | debug.log drop list without loot tables; five `/loot spawn` "Dropped 1" |
| SQ-TP-32 | PASS | one item per block, bare hand and iron pickaxe, NBT as the row |
| SQ-TP-33 | PASS (row wording) | terminal break: one workstation + Tempad; TNT: marker one item 6/6; workstation and metronome blast-proof (1200), cannot apply |
| SQ-TP-34 | PASS | creative pad / terminal / powered: 0 items, no terminal left, door ran out, no exception |
| SQ-TP-35 | PASS | survival powered pad and terminal: workstation + Tempad each; placement refused with stone on the terminal spot |
| SQ-TP-36 | PASS | anchor_id = card id before and after restart; 2 anchors; both listed |
| SQ-TP-37 | PASS | entry removed, drop keeps id, re-place moves entry, Tempad and projector doors open there |
| SQ-TP-38 | PASS | same after chunk unload (inferred) without a restart |
| SQ-TP-39 | PASS | both copies new ids, original kept |
| SQ-TP-24 | PASS (rerun) | marker via 37 / 39, chronomark break and re-place |
| SQ-TP-25 | PASS (rerun) | = SQ-TP-37 |
| SQ-TP-08, -13, -14, -15 | PASS (break steps rerun) | via SQ-TP-32..35, -37 |
| CQ-TP-16 | not run | client row |

Row wording to fix in the queue: SQ-TP-33's TNT step applies to the marker / chronomark / projector only (workstation
and metronome resist 1200).

## 3. r1107cc: Creative Common 1.1.0 full check (SQ-CC, with the SQ-CPP rows it reruns)

Owner request 2026-11-07 18:10: "execute the creative common scratch tests, debug as you go along for anything that
doesn't require a decision, and update the dev and port test instance afterwards". Scope: queue sections "SQ-CC Creative
Common 1.1.0 full check" (server rows) and "SQ-CPP Creative Common publish cleanup" (its server rows are reruns inside
SQ-CC). Client rows (CQ-CC-01..06, CQ-CPP-01) and SQ-CC-12 (needs the owner client): not run. Targeted run, no perf
session (JFR on per boot; SQ-CC-13 takes its own recording). "Dev instance" = this instance's `mods/`, "port test
instance" = `CEPortTesting/mods/`.

- Jars: build/libs as of 18:16 (Common 18:02, Compat 18:03, Calendar 17:40, Engineering 17:01, Delight / Brewing /
  Equipment 17:03, Magic 16:59), none installed; no source newer than its jar. prep_srv swaps them in.
- b1: srv `cc/` in the session scratchpad, level r1107cc, group A. Left out: loadingscreentips (as r1107tp). Kept:
  Immersive Portals 2.3.7 and Portal Gun (SQ-CC-08 needs IP with Serene Seasons). Probes: `cc_dict_probe.js`,
  `cc_seqasm_probe.js` (new: `/zsa <seq assembly id>`, `/zsa bottle|refine <potion>`, `/zsa selftest`), zq, s5.

### Run notes

- 18:16 claim, preflight: no JVM, 61 GB free. SQ-CC-01 (no boot) PASS.
- 18:17 boot b1, Done 11.0 s. Mixin "Error loading class" WARNs are absent optional mods; one third-party D&D client
  class error; tag failures only creategoggles' Mekanism reference; Railways loot tables (third-party).
- 18:19 probe fix (tooling): `cc_dict_probe.js` read `global.block`; the block dict is `global.blocks`.
- 18:20-18:24 SQ-CC-06, -07, -05, -10 (Magic Resistance half), -08, -14, -15, -16, -13: see verdicts. Bots are Forge
  FakePlayers (invulnerable): damage checks use NoAI villagers (pigs are discarded with spawn-animals=false).
- 18:27 SQ-CPP-02 sequenced assembly: `/zsa` walks the recipe through Create's public
  `SequencedAssemblyRecipe.getRecipe(level, stack, type, class)` (advance() is private): echo shovel and felsic
  leggings keep Unbreaking III and the name. Rhino notes: KubeJS `enchant` returns a copy; no Mojmap `getItems` /
  `getTag` here (use `getStacks` / `getNbt`).
- 18:30-18:45 FINDING (IE fluid inputs): `/zsa selftest`: every IE recipe holding a FluidTagInput rejects the first of
  its own `getMatchingFluidStacks()` (bottling 1906, mixer 14, refinery 20, Immersive Petroleum 12; plain and NBT
  alike); `BottlingMachineRecipe.findRecipe(glass bottle, REGULAR swiftness)` = none. The fluid is in the tag
  (`Fluid.is` true), the tags are equal (`equals`, `NbtUtils.compareNbt` both ways true). Engineering's
  FluidTagInputMixin is the only mixin into the class. Next boot exports the transformed class.
- 18:33 FINDING (fixed in source): 22 Fruits Delight jelly Cooking Pot recipes fail to parse ("Expected cookingtime to
  be a Int, was "400.0""), also 88 times in the owner's own 17:56 log: `NCH.setCookTime` passed a JS number to
  `JsonObject.addProperty`; Rhino picks the String overload (checked: plain number and `Integer.valueOf` both give
  "400.0"; `JsonParser.parseString` gives 400). Fixed in `neoCraftHelper.js` (Common rebuild). Not shipped (the
  deployed Creative Delight jar has no setCookTime call), so no new changelog bullet: the unreleased "Jams" bullet
  (cook for 20 seconds) becomes true with the fix.
- 18:35 SQ-CC-10b (Phantom Bane, r1102b SQ-PSU-04 batch with fixed coordinates,
  `tools/scratch_query/batches/r1107cc/b1_phantom.qb`) started, about 13 min.
- 18:50 IE "finding" WITHDRAWN (probe fault): KubeJS converts every FluidStack a script passes into a Java method,
  and a raw FluidStack does not survive it (`fl.isFluidStackIdentical(fl)` = false, `isFluidEqual` ambiguous with the
  ItemStack overload). Passed as `Fluid.of(id, amount, nbt)`: `/zsa selftest` 1952 pass, 0 fail; bottling REGULAR
  swiftness -> `creativebrewing:iebottling/create_potion/minecraft_swiftness_regular` (Swiftness potion); refinery
  SPLASH swiftness + `create_central_kitchen:dragon_breath` -> `creativebrewing:lingerification/minecraft_swiftness_from_splash`
  (LINGERING). Probe fixed; gotcha added.
- 18:47-18:55 SQ-CC-10b on b1: UNDECIDED (no phantom near either bot in the control pass). Cause: group A's
  `spawn-monsters=false` (the phantom spawner needs enemy spawning; r1102b's world had it on). Batch also counted
  the `rest` lines too strictly (4 in the row, wanted == 2; now >= 2).
- 18:55 b1 stopped. Common rebuilt 18:51 with the setCookTime fix; copied (not linked) into srv/mods.
- 18:56 b2 (same srv and world; `spawn-monsters=true`; `[effects] removed = ["minecraft:slowness"]` for SQ-CC-11),
  Done 4.2 s. 0 "Expected cookingtime" (b1: 22 recipes x every reload); `farmersdelight:cooking/apple_jelly` loads.
  SQ-CC-11 PASS. SQ-CC-09 (emulated: chest NBT `Potion:"creativecommon:long_reach"` written with setblock, the way an
  11-06 save holds it): `/ccswap items` 0 rewritten (it rewrites item merges, not potion ids); `/zsa potionat`:
  the stack reads `creativebrewing:long_reach` with Better Beacons Long Reach 3600 t; NBT keeps the old string,
  which is the documented design of `PotionByNameRemapMixin`. PASS (row wording).
- 19:00 configs_edited refreshed from the live config for Common, Equipment, Brewing, Delight (keys moved in the carve;
  no value differed in any Creative config).
- 19:01 SQ-CC-10b rerun on b2 (`res_b2`).
- 19:15 SQ-CC-10b on b2 again no phantom (not a FAIL): `/zph` (new `cc_phantom_probe.js`: fresh PhantomSpawner passes)
  showed seaLevel=63 in this superflat; the spawner skips players below sea level and the bots stood at y -60.
  Bots on stone platforms at y 70: control 10 passes, 27 phantoms, phantoms near both bots; Phantom Bane on the first
  bot: 10 passes, 15 phantoms, all near the second bot, none near the first. PASS (also settles SQ-PSU-04, OPEN since
  r1103). Batch fixed (platforms at y 70), gotcha added.
- 19:20 b3 (`cc3/`: Rhino, Architectury, KubeJS, Serene Seasons, Common, Compat, Calendar; empty kubejs/, config/,
  defaultconfigs/). First try crashed: my setup removed the `*_upstream` links and Compat's nested Tempad needs
  `tempad_upstream` (by design). The crashed JVM hung and a second boot started beside it: both mine, both stopped
  (stop_wait killed the hung one), world wiped, clean reboot Done 5.6 s. Lesson: `stop_wait.sh` after every CRASH.
- 19:23 SQ-CC-03: no class or mixin error with Create, IE, Fruits Delight, Immersive Portals, Heartstone absent; one
  KubeJS ERROR per load: Compat `misc_mods.js` `R.replaceInput({ id: 'supplementaries:soap' }, ...)` with Supplementaries
  absent (dict values undefined). FIXED (Common): `NCH._absentInFilter` now counts a top-level `{ id }` / `{ mod }`
  filter of an absent mod in the absent-mods summary (not inside OR lists or nested filters). Audit: all 66
  namespaces used in such filters across the scripts are loaded mod ids in the full pack. SQ-CC-04 PASS.
- 19:25 Common rebuilt, b3 rebooted (Done 3.3 s): 0 KubeJS errors, 0 class errors. SQ-CC-03 PASS, SQ-CPP-05 PASS.
- 19:27 b4 (`cc4/`: chain without Engineering: Rhino, Architectury, KubeJS, KubeJS Create / Delight, LootJS, MoreJS,
  Create, Serene Seasons, IE, Fruits Delight, Common, Compat, Calendar, Delight, Brewing; CEPortTesting's kubejs/;
  group B seed 8675309), Done 25.0 s. SQ-CPP-04 PASS: 0 recipe parse errors, 267
  `creativebrewing:iebottling/create_potion/` recipes, REGULAR swiftness bottles to a Swiftness potion with IE's exact
  NBT match (no Engineering); the refinery half cannot apply (no dragon's breath fluid without Central Kitchen).
  KubeJS: 24 NCH ERRORs. One was Common's: `ieMetalPress` uses KubeJS IE's schema, absent here ("Constructor for
  immersiveengineering:metal_press with 3 arguments not found"). FIXED: skipped (counted) without
  `kubejs_immersive_engineering`. The other 23 are FINDINGS for Delight / Brewing in this reduced chain (TODO
  `creativebrewing/publish #24`, `#25`): tags `#forge:tortilla`, `#forge:fruits/banana`, `#forge:animal_fat`,
  `#creativecommon:bark` empty; undefined outputs (cut copper vertical slab splashing, mixing, ceBaking, vdPickling);
  `replaceItems` aborted by a Delight IE Bottling recipe whose result is the tag `#forge:dough/wheat`
  (`kubejs/data/creativedelight/kubejs/create/recipes.js` 48, `R.ieBottling(i.dough, ...)`).
- 19:31 Common rebuilt (final), b4 rebooted: metal press error gone, the 23 remain.
- 19:32 b5 (`cc5/`, fresh prep_srv group A, final Common 19:31), Done 11.1 s: 0 mixin errors, all 10 jars' startup files
  loaded, 0 KubeJS errors, 0 "Expected cookingtime", 14 parse errors (all third-party), dict probe 8 lines. Absent
  summary as b2 plus one line (`createsifter`, the `{ mod: 'createsifter' }` replaceOutput in Engineering's
  metal_base_materials.js: was a silent no-op, now counted). 19:34 stopped; no JVM up.
- 19:35-19:38 installed to the dev instance `mods/` (installToMods): Common 19:35 (same neoCraftHelper.js as the tested
  19:31 jar), Compat, Calendar, Delight, Brewing, Equipment, Engineering, Magic, Devtools, Worldgen. Brewing's install
  first failed in the nested Vinery member ("vinery.refmap.json is not in backportedVinery-...jar", build-logic's
  refmap backstop): `:backportedVinery:clean`, then it built. `repackedStorage` -02 (11-07 14:39) in build/libs is newer
  than the -01 in `mods/`: not part of this run, left. CEPortTesting/mods: Common replaced (old one in its
  `jar_archive/displaced/creativecommon-1.1.0.1938.jar`); its 4 jars now match the dev instance's.

### Verdicts (r1107cc, server rows)

| ID | Result | Evidence |
|---|---|---|
| SQ-CC-01 | PASS (row wording) | no moved class, no `resourcepacks/creative_tips`, no `data/create/tags`; 40 mixins all with classes; no create / IE / fruitsdelight row; immersive_portals and heartstone listed (optional, `ordering="NONE"`: the order fix is in the mixin plugin, as the mods.toml comment says) |
| SQ-CC-02 | PASS | b1 and b5: no mixin apply error, no out-of-range WARN, all startup files loaded (Common 23 of 23), 0 startup script errors; mixin "Error loading class" WARNs are absent optional mods |
| SQ-CC-03 | PASS (after fix) | Common + Compat + Calendar: no class error; the one script error fixed in NCH (absent-mod id filter) |
| SQ-CC-04 | PASS | generated config: overrides [], removed [], advancements on, creative menu not disabled, gravity limit on |
| SQ-CC-05 | PASS | pack config keeps the 3 removed effects and the name list; restfulness / power_down refused; Speed applies |
| SQ-CC-06 | PASS | `any_bark` = `#creativecommon:bark`, `t_metal_dusts` = `#creativecommon:metal_dusts`; Create Framed / SecurityCraft keys print ids; mirror glass tags `#creativecompat:mirror_glass(_panes)`; bark 19, metal_dusts 15 |
| SQ-CC-07 | PASS | scrap_wood_small 124, scrap_copper_blocks 8 with Create Recycle; no error naming a scrap tag |
| SQ-CC-08 | PASS | "[compat] Immersive Portals overwrites conform to target visibility (Serene Seasons widens ChunkMap)"; bot to the Nether and back; no mixin apply error |
| SQ-CC-09 | PASS (row wording, emulated save) | `Potion:"creativecommon:long_reach"` reads `creativebrewing:long_reach` (Long Reach 3600 t); NBT keeps the old string by design; `/ccswap items` rewrites item merges only (0) |
| SQ-CC-10 | PASS | Magic Resistance I: Poison I and Wither I no damage (control lost 6), Poison II 20 % cut; Phantom Bane on the first bot: 15 phantoms, all at the second bot (control: 27, both) |
| SQ-CC-11 | PASS | `removed = ["minecraft:slowness"]`: Slowness refused, Speed applies, no config error |
| SQ-CC-12 | not run | needs the owner client |
| SQ-CC-13 | PASS | JFR profile 60 s: creativecommon.LevelTick, ServerTick, KubeJSHandler(Stats), EntityTypeTicks, ChunkWaitStats |
| SQ-CC-14 | PASS | advancement grant: "Unknown advancement"; trader 31 offers (quill + 25 + 5) |
| SQ-CC-15 | PASS | mixin applied; End villager gravity 0.016, overworld 0.08, the modifier removed after leaving the End |
| SQ-CC-16 | PASS (whistle not shot) | Done 11.0 s; WorldEdit / Quark / Nock Enough Arrows mixins applied, no range WARN; tp to 50000 under a second, no "Can't keep up" |
| SQ-CPP-01 | PASS (row wording) | = SQ-CC-02 / -06 / -05; bark is Immersive Weathering only, as the old `creativeengineering:bark` was (no FD tree bark) |
| SQ-CPP-02 | PASS | `/zsa`: echo shovel and felsic leggings keep Unbreaking III and the name; Brewing's iebottling and lingerification recipes load |
| SQ-CPP-03 | PASS (lookup, no multiblock) | bottling REGULAR swiftness -> Swiftness potion; refinery SPLASH swiftness + dragon's breath -> LINGERING; selftest 1952 of 1952 |
| SQ-CPP-04 | PASS | chain without Engineering boots; 267 IE potion bottling recipes; bottling works with IE's exact match (23 Delight / Brewing script errors: findings) |
| SQ-CPP-05 | PASS | Common + Compat without Create Recycle: physics tags load, no `create:scrap_*` error |
| SQ-PSU-04 | PASS (via SQ-CC-10b) | open since r1103 |
| CQ-CC-01..06, CQ-CPP-01 | not run | client rows |

### Findings and fixes (r1107cc)

- FIXED, Common: `NCH.setCookTime` wrote `"cookingtime":"400.0"`; the 22 Fruits Delight jelly Cooking Pot recipes failed
  to parse (also in the owner's 17:56 log). Never shipped; the unreleased "Jams" changelog bullet now holds.
- FIXED, Common: an `{ id }` / `{ mod }` filter of an absent mod logged an ERROR (Compat's Supplementaries soap / fodder
  rows) instead of joining the absent-mods summary.
- FIXED, Common: `ieMetalPress` without KubeJS Immersive Engineering logged an ERROR; now skipped and counted.
- OPEN (TODO `creativebrewing/publish #24`): 22 Delight / Brewing script errors in the Brewing chain without
  Engineering (empty tags and undefined dict outputs of absent mods).
- OPEN (TODO `creativebrewing/publish #25`): Delight's `R.ieBottling(i.dough, ...)` writes a tag as a result; the item
  merge recipe swap aborts on it in the reduced chain.
- WITHDRAWN: "IE fluid inputs reject every fluid" was the probe (KubeJS converts a raw FluidStack); gotcha added.
- Done: `configs_edited/` copies of Common, Equipment, Brewing, Delight refreshed from `config/` (no value differed).
- Tooling: `cc_dict_probe.js` (global.blocks), new `cc_seqasm_probe.js` (`/zsa`), `cc_phantom_probe.js` (`/zph`),
  `batches/r1107cc/b1_phantom.qb` (platforms at y 70).

Row wording to fix in the queue (done in the rows): SQ-CC-06 / -07 / SQ-CPP-01 use `/zq itemtag` (KubeJS 1902 has no
`/kubejs dump_tag`); SQ-CPP-01 bark has no FD tree bark; SQ-CC-09 pass = reads the new id (NBT unchanged);
SQ-CC-01 ordering is in the mixin plugin; SQ-CC-10 damage on villagers, phantom bots at y 70.

## 4. r1107cco: Creative Common with its optional mods only (SQ-CCO)

Owner request 2026-11-07 20:35: "execute the latest creative common scratch tests with all optional mods loaded". Scope:
queue section "SQ-CCO Creative Common with its optional mods only", server rows SQ-CCO-01 to -16. Not run: CQ-CCO-01 to
-03 (client rows; need a window). Targeted run, no perf session (JFR on per boot, recordings in each srv's perf/).

- Jars: Common `creativecommon-1.1.0` 11-07 20:28 (build/libs = `mods/`), the optional mods from `mods/`, perfBot
  `cdevperfbot-0.1.0` 19:37. Servers from `tools/scratch_query/cc_only_srv.sh` (group B, seed 8675309, difficulty normal).
- Boots: b1/b2 `full` (srv `cco1`, Done 28.9 s), b3 `full` toggled (IW fast path off, gravityLimitDimensions false,
  effects removed Slowness), seven set boots (`tools/scratch_query/cco_sets.sh`: six `without=` + `alone`, Done 27.2 to
  30.9 s), b5 `full` defaults again. Batches `tools/scratch_query/batches/r1107cco/` b1_full, b2_console, b3_toggled,
  b4_without, b5_gravity; results `perf/runs/r1107cco/`.

### Run notes

- 20:37 claim, preflight: no JVM, 60 GB free; Common in `mods/` = newest build.
- 20:37 tooling: `cc_only_srv.sh` pruned the perfBot jar (bots for every effect row): added to the always-kept list.
  b1_full.qb spawned its bot in SQ-CCO-03 after SQ-CCO-02 used it: moved up.
- 20:38 first boot CRASHED at world creation: unbound `terralith:overworld_large_biomes/...` density function.
  `cc_only_srv.sh` kept the pack's `kubejs/data/` (Terralith noise overrides, every Creative jar's data); fixed to
  keep only the zq scratch data. The hung JVM (mine) killed by stop_wait.sh.
- 20:51 b1 Done 28.9 s. Batch faults fixed and rerun: the class-error reject caught Quark's own mixin probing absent
  Lootr ("Error loading class", third-party); the Heartstone priority line is client-only (`orderHeartstoneBeforePortals`
  returns on a dedicated server: CQ-CCO-01 checks it); "Applied effect Phantom Bane" is the console text, not the id.
- 20:55 b2_console: SQ-CCO-11's "Static binding violation ... MixinChunkMap_C ... visibility will be upgraded" is the
  designed result of the conform (one line; Mixin's own text says "visibiliy", so a "cannot reduce visibility" regex
  must exclude it). SQ-CCO-10 first try: platforms in unloaded chunks were not placed, bot 2 fell into a cave
  (sky=false): forceload added; bot 1 kept Phantom Bane across respawns (player data persists): `effect clear` added.
- 21:00-21:12 SQ-CCO-08 (Create Gravity): no low-gravity modifier on a falling zombie, a survival bot or a standing
  villager in the End, with Common's gate on (b2, b5) AND off (b3): Gravity applies nothing on this set
  (default `biome_configs = ["#minecraft:is_end;;"]`, written fresh), while r1107cc saw End villager 0.016 in the full
  pack. Common's gate is not the cause; not chased further (third-party). PARTIAL, TODO `creativecommon/publish #3`.
  Learned: NoAI mobs do not move at all in 1.19; `@e` without a position searches every dimension.
- 21:10-21:22 seven set boots, all clean (table below). 21:24 b5: SQ-CCO-14 (emulated) and the Gravity recheck.
- 21:27 all JVMs stopped.

### Verdicts (r1107cco)

| ID | Result | Evidence |
|---|---|---|
| SQ-CCO-01 | PASS | Done 28.9 s; no mixin / class error from our jars (only Quark -> Lootr, third-party); IW callTick and IP conform lines; no other Creative jar; 22 of 22 Common startup files, 0 KubeJS errors. Heartstone line: client-only, CQ-CCO-01 |
| SQ-CCO-02 | PASS | Magic Resistance and Phantom Bane apply on a bot; no `[CE potions] not present` line |
| SQ-CCO-03 | PASS (fill; `//set` needs a player) | fill works, no WorldEdit mixin error; `full` Done 28.9 s vs `without=we` 30.0 s (no stall) |
| SQ-CCO-04 | PASS | fresh config: overrides = [], removed = [], gravityLimitDimensions = true, [advancements] enabled = true, [creative_menu] disabled = false |
| SQ-CCO-05 | PASS | no ASM warning; mossy cobblestone 1 -> 83 in 60 s (fast path); fast path off: off line logged, 1 -> 84 |
| SQ-CCO-06 | PASS | two `/reload`s, no Armor Trims / cache error |
| SQ-CCO-07 | PASS | `/tp` to 50000 100 50000 inside the 4 s of the two pace steps; no "Can't keep up", no Quark mixin error |
| SQ-CCO-08 | PARTIAL | no mixin error; overworld fall vanilla; End modifier never applied on this set even with the gate off (see notes) |
| SQ-CCO-09 | PASS (server part) | pig, cow and a whistling arrow ticking, no NEA error; tick cost is CQ-CCO-03 |
| SQ-CCO-10 | PASS | `/zph` with bots on y 150 platforms: control (Bane off) phantoms reach bot 1; Bane on bot 1: 10 and 9 phantoms, all near bot 2, none near bot 1 |
| SQ-CCO-11 | PASS | conform line 1, one designed Static binding violation, no "cannot reduce visibility"; Nether trip and back clean |
| SQ-CCO-12 | PASS | six sets: each boots, reloads, stops; 0 KubeJS startup errors; Common mixins listed only for mods in the set; Phantom Bane unknown only without Better Beacons (no skip line: Common's only potion row, Magic Resistance, has no needsMod) |
| SQ-CCO-13 | PASS | `alone`: Done 30.9 s, 0 KubeJS errors, no optional-mod mixin applied, Magic Resistance applies |
| SQ-CCO-14 | PASS (emulated) | chest potion `creativecommon:long_reach` kept as is after save-all with Brewing absent, no error |
| SQ-CCO-15 | PASS | 22 of 22 files; `creativecommon:bark` 12 items (IW), `metal_dusts` empty (no metal-dust mod in the set); `[ccdict]` values print, 0 exceptions |
| SQ-CCO-16 | PASS | trader offers vanilla + Quark's own (no pack trades); advancement grant works; with Slowness removed it is refused and Speed applies |
| CQ-CCO-01..03 | see section 5 | owner client 2026-11-07: -01 PASS, -02 / -03 PARTIAL |

Tooling: `cc_only_srv.sh` (perfBot kept, pack `kubejs/data` dropped), new `cco_sets.sh`, batches r1107cco b1 (fixed),
b2 to b5. No gameplay change, no changelog bullet (test-only session).

## 5. Owner client rows, CEPortTesting (CQ-CCO, CQ-CMV-01, CQ-SPFX-02, CQ-CCB-01, -03)

Owner client, 2026-11-07 21:40 to 22:54, CEPortTesting with Common 21:38 (`build/libs`; the dev `mods/` still holds
20:28). Launch plan: 1 = the `cc_only_srv.sh full` set, 2 = minus Legendary Tooltips and Immersive Portals, 3 = Common
alone, 4 = Common + Compat without JEI. Logs read per launch from `CEPortTesting/logs/` (the owner's greps ran on the dev
instance's log by mistake; re-run here): launch 1 = `2026-11-07-3/-4/-5.log.gz`, launch 2 = `-6`, launch 3 = `-7`,
launch 4 = `latest.log` + `crash-reports/crash-2026-11-07_22.54.33-fml.txt`.

### Verdicts

| ID | Result | Evidence |
|---|---|---|
| CQ-CCO-01 | PASS (Mods screen description not reported) | title screen reached every launch; Heartstone priority line once per launch; `Loading tips: 321 tips from 1 files` (Common alone has no pack tips: expected); no `Mixin apply failed`, no `NoClassDefFound` |
| CQ-CCO-02 | PARTIAL | Armor Trims works (owner). Nether trip made (Immersive Portals `too many portal nearby` ERRORs in the Nether, third-party). Not reported: creative inventory speed, Heartstone render with a portal in view. Log: Armor Trims' own JEI `There is no recipe category registered for armor_trims:armor_trimming` (third-party, every launch with JEI) |
| CQ-CCO-03 | PARTIAL | whistle arrows work, no error in the log (only Nock Enough Arrows' own `light_air` missing-model WARN); Quark biome locate and teleport work; Create Gravity: a survival player falls slowly in the End, falling blocks fall at normal speed there (owner). WorldEdit `//wand` / `//set` not reported |
| CQ-CMV-01 | PASS | launch 2 (`-6`): Iceberg without Legendary Tooltips, tooltips look fine, no crash. Owner: the underline on 3D item tooltips is fixed |
| CQ-SPFX-02 | PARTIAL | first half PASS (one `priority 1000 -> 999` line, no apply failure). Second half not done: launch 2 still had Immersive Portals (priority line present in `-6`). Outline check moved to the server join |
| CQ-CCB-01 | PASS | launch 3 (`-7`, Common alone): Magic Resistance icon right (owner); grep for `magic_resistance.png`, `JEIEvents`, `jei_order` and the `#creativedelight:stoves` never-match WARN: no line. Other lines: `[absent mods] skipped 2 tag entry rows for absent nock_enough_arrows`, KubeJS `Failed to get filter section from pack kubejs` (KubeJS's own) |
| CQ-CCB-03 | FAIL (crash, not debugged: owner) | launch 4 (Common + Compat + Tempad port, no JEI): `Mod loading error`, `IllegalStateException: Tempad reads its textures, models and recipes from the original Tempad 3.0.4 jar, which must be in the folder 'tempad_upstream' of the game directory`. CEPortTesting has no `tempad_upstream/`. TODO `creativecompat/publish #1` |

Aside from the owner's dev-instance grep (full pack, 16:52, Common 20:28 or older): `java.lang.NoClassDefFoundError:
com/sammy/minersdelight/jei/CopperPotCookingRecipeCategory`. Not chased; recheck in CQ-CC-01 on the next full-pack launch.

Still to run: CQ-CCO-02 / -03 leftovers (inventory speed, Heartstone with a portal, WorldEdit), CQ-SPFX-02 second launch
(Immersive Portals and Portal Gun out), CQ-CCB-03 after the Tempad fix, the full-pack rows (CQ-CC-01..06, CQ-CMV-02, -03,
CQ-CPP-01, CQ-CCB-02, CQ-CIF-01) after Common 21:38 goes into `mods/`, and SQ-CC-12 with the server rows on the owner's
signal. No gameplay change, no changelog bullet (test-only).

### 5.1 Owner follow-up (2026-11-07 late)

- CQ-CCO-02: PASS (creative inventory speed good, no Heartstone issue). CQ-CCO-03: WorldEdit worked; whistle arrow, Quark, player gravity PASS; **Create Gravity in the End: falling blocks do not get low gravity, owner expects every entity in the End to** (TODO `creativecommon/publish #3`, now also a possible real gap, not only a test-set difference).
- CQ-SPFX-02 stays PARTIAL; open part = (a) launch with Immersive Portals (and Portal Gun) removed: no `Heartstone LevelRendererMixin` line, title screen reached; (b) two bonded players (server join): the partner keeps the Heartstone outline.
