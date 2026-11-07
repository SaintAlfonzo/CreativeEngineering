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
