# TODO status normalization, 2026-11-16

One-time pass by `tools/status_normalise_todo.py` (plan_process_overhaul.md P1.4 item 3). Mapping: "Built" to
Testing (In Progress when the old text says FAIL or "not built"); "Installed ..." and "Tested" to Testing; "Done" /
"Complete" outside the archive moved to the archive table (Status and Tests kept in Details); "Open" and "Idea" to
Planned; "In progress" to In Progress. Extra text in an old status cell now starts the row's Details cell.
Rows that already sat in the archive were not touched.

85 rows changed.

| Id | Old status | New status |
|---|---|---|
| `recipes/metal #7` | Complete | Complete (moved to the archive) |
| `gameplay/tnt-and-portal #8` | In progress | In Progress |
| `gameplay/tnt-and-portal #9` | Built 2026-11-07; scratch r1107tp: SQ-TP-21..24 PASS (server), SQ-TP-25 FAIL from a separate bug (#11), CQ-TP-12..15 not run (whole-mod gaps SQ-TP-26..28, 30 PASS); also fixed: missing `intradimensional_all` key, array lang lines flattened (PORT_NOTES 6.12, 7) | In Progress |
| `gameplay/tnt-and-portal #10` | Built 2026-11-07 12:01 (owner request), scratch server PASSED r1107tp2 (SQ-TP-31..33, reruns of SQ-TP-08, -13, -14, -15 break steps); jar not installed | Testing |
| `gameplay/tnt-and-portal #11` | Built 2026-11-07 12:01 (owner request; key renamed to `anchor_id`, ctrl-pick copies keep their own id), scratch server PASSED r1107tp2 (SQ-TP-36..39, reruns of SQ-TP-24, -25); CQ-TP-16 (client) left; jar not installed | Testing |
| `gameplay/tnt-and-portal #12` | Built 2026-11-07 12:01 (owner request; terminal cleared with UPDATE_KNOWN_SHAPE), scratch server PASSED r1107tp2 (SQ-TP-34, -35, rerun of SQ-TP-08); CQ-TP-16 (client) left; jar not installed | Testing |
| `gameplay/tnt-and-portal #13` | Built 2026-11-07 15:58, installed with the 16:44 Compat jar; SQ-PG / CQ-PG queued | Testing |
| `gameplay/tnt-and-portal #14` | Built 2026-11-07 15:58, installed with the 16:44 Compat jar; SQ-PG / CQ-PG queued | Testing |
| `gameplay/tnt-and-portal #16` | Built 2026-11-07 15:58, installed with the 16:44 Compat jar; SQ-PG / CQ-PG queued | Testing |
| `gameplay/tnt-and-portal #17` | Built 2026-11-07 16:44, installed; SQ-IP / CQ-IP queued | Testing |
| `gameplay/tnt-and-portal #18` | Built 2026-11-07 16:44, installed; SQ-IP / CQ-IP queued | Testing |
| `gameplay/tnt-and-portal #19` | Built 2026-11-07 16:44, installed; SQ-IP / CQ-IP queued | Testing |
| `gameplay/tnt-and-portal #20` | Built 2026-11-07 16:44, installed; SQ-IP / CQ-IP queued | Testing |
| `gameplay/tnt-and-portal #21` | Built 2026-11-07 16:44, installed; SQ-IP / CQ-IP queued | Testing |
| `gameplay/tnt-and-portal #22` | Built 2026-11-07 16:44, installed; SQ-IP / CQ-IP queued | Testing |
| `gameplay/tnt-and-portal #23` | Built 2026-11-07 16:44, installed; SQ-IP / CQ-IP queued | Testing |
| `gameplay/tnt-and-portal #24` | Built 2026-11-07 16:44, installed; SQ-IP / CQ-IP queued | Testing |
| `gameplay/tnt-and-portal #25` | Idea | Planned |
| `ui/loadingtips #3` | In progress | In Progress |
| `largecontent/mod-additions #65` | Complete | Complete (moved to the archive) |
| `largecontent/mod-additions #66` | Complete | Complete (moved to the archive) |
| `largecontent/mod-additions #67` | Complete | Complete (moved to the archive) |
| `largecontent/mod-additions #69` | Complete | Complete (moved to the archive) |
| `largecontent/mod-additions #70` | Complete | Complete (moved to the archive) |
| `largecontent/mod-additions #72` | Complete | Complete (moved to the archive) |
| `largecontent/mod-additions #75` | Complete | Complete (moved to the archive) |
| `largecontent/mod-additions #76` | Complete | Complete (moved to the archive) |
| `largecontent/mod-additions #84` | Complete | Complete (moved to the archive) |
| `largecontent/mod-additions #87` | Complete | Complete (moved to the archive) |
| `creativedelight/framework #13` | Built | Testing |
| `creativedelight/framework #14` | Built | Testing |
| `creativedelight/framework #15` | Built | Testing |
| `creativedelight/framework #16` | Complete | Complete (moved to the archive) |
| `creativedelight/framework #17` | Complete | Complete (moved to the archive) |
| `creativedelight/members #57` | Complete | Complete (moved to the archive) |
| `creativedelight/members #58` | Complete | Complete (moved to the archive) |
| `creativedelight/members #59` | Complete | Complete (moved to the archive) |
| `creativebrewing/publish #5` | Built | Testing |
| `creativebrewing/publish #6` | Built | Testing |
| `creativebrewing/publish #7` | Open | Planned |
| `creativebrewing/publish #8` | Built | Testing |
| `creativebrewing/publish #9` | Built | Testing |
| `creativebrewing/publish #11` | Built | Testing |
| `creativebrewing/publish #12` | Built | Testing |
| `creativebrewing/publish #13` | Built | Testing |
| `creativebrewing/publish #14` | Built | Testing |
| `creativebrewing/publish #15` | Built | Testing |
| `creativebrewing/publish #16` | Built | Testing |
| `creativebrewing/publish #17` | Built | Testing |
| `creativebrewing/publish #18` | Built | Testing |
| `creativebrewing/publish #19` | Built | Testing |
| `creativebrewing/publish #20` | Built | Testing |
| `creativebrewing/publish #21` | Done | Complete (moved to the archive) |
| `creativebrewing/publish #22` | Built | Testing |
| `creativebrewing/publish #23` | Built | Testing |
| `creativebrewing/publish #24` | Open | Planned |
| `creativebrewing/publish #25` | Open | Planned |
| `creativeengineering/carve #1` | Built | Testing |
| `licence/cb #1` | Done | Complete (moved to the archive) |
| `licence/cb #2` | Done | Complete (moved to the archive) |
| `licence/common #1` | Done | Complete (moved to the archive) |
| `licence/common #2` | Done | Complete (moved to the archive) |
| `licence/common #3` | Done | Complete (moved to the archive) |
| `creativecommon/publish #2` | Built | Testing |
| `creativecommon/publish #3` | Built 2026-11-08 15:08 (Common and Compat), not installed; SQ-CGE-07..16 PASS, client single player PASS (run r1108cge2, ItemPhysic patch and old tag removal included); CQ-CGE-03a..03c left for the owner (client joined to a dedicated server) | Testing |
| `creativecompat/publish #1` | Installed 2026-11-07 23:10, untested | Testing |
| `licence/cd #2` | Done | Complete (moved to the archive) |
| `licence/cd #3` | Done | Complete (moved to the archive) |
| `licence/cd #5` | Done | Complete (moved to the archive) |
| `creativesplit/regression #15` | Tested | Testing |
| `creativesplit/regression #16` | Tested | Testing |
| `creativesplit/regression #19` | Built | Testing |
| `creativesplit/regression #21` | Built | Testing |
| `creativesplit/interface #22` | Built | Testing |
| `pack/server #39` | Complete | Complete (moved to the archive) |
| `tooling/process #1` | Complete | Complete (moved to the archive) |
| `tooling/scratch-1115-follow-ups-2 #8` | Open | Planned |
| `tooling/scratch-1115-follow-ups-2 #9` | Open | Planned |
| `tooling/scratch-1115-follow-ups-2 #10` | Open | Planned |
| `tooling/scratch-1115-follow-ups-2 #11` | Open | Planned |
| `tooling/scratch-1115-follow-ups-2 #12` | Open | Planned |
| `tooling/scratch-1115-follow-ups-2 #13` | Open | Planned |
| `tooling/scratch-1115-follow-ups-2 #14` | Open | Planned |
| `tooling/scratch-1115-follow-ups-2 #15` | Open | Planned |
| `tooling/scratch-1115-follow-ups-2 #16` | Open | Planned |
