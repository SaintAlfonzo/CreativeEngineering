# r1102b worldgen slice: rows not batched

| ID | Reason | Evidence |
|---|---|---|
| CQ-WGE-07a | client look | Cool Rain mixin apply is checked on the client log after the title screen (scratch_queue.md SQ-WGE, "one client session for 07") |
| CQ-WGE-07b | client look | rain splash sounds by biome and season; no server-side signal |
| CQ-WGE-07c | client look | Pretty Rain / Cool Rain removed boots are client boots |
| SQ-WGE-00 | offline, run by the batch writer | result in w_offline.md (not a batch row) |

Halves left out of batched rows (the rest of each row is in a batch; the row ends LOOK for these):

| ID | Half not batched | Why |
|---|---|---|
| SQ-WGE-06b | sample 300 generations of the bridge pool and compare 10 : 15 : 20 | no pool-sampling probe exists; the batch checks `current == original` for every duplicate and the pillar order |
| SQ-WGE-05b | each waystone registered (right-click shows a name) | needs a player |
| SQ-IP-15 | drop by a player (Q) | summoned item entities stand in for the drop; a client drop is the full check |
| SQ-EFB-03, -04, -07, -10, -13, -14, -19, -22 | the side views (mounds, camps, stilts, beard edge, street ends, castle walls, river banks, oasis villages) | client tour at the positions the probe and log lines give |
| SQ-PSW-02 | biome under each Nether ruin, lava flood | `execute if biome` is 1.19.3+; positions captured (NC, ND, NR, NS) for a client tp |
| SQ-PSW-04 | gothic church shows yellow corundum (NW-11) | client look |
| SQ-PSW-05 | spike bases on end stone from the side | client look at the listed chunks |
| SQ-WGE-03b | tp to a kept castle on a slope (ST-240) | client look |
