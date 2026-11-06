# r1102b slice 5: rows not in a server batch

| ID | Reason | Evidence |
|---|---|---|
| CQ-PST-04 | client: SeasonHUD text and world re-tint | scratch_queue.md SQ-PST table, Group "client" |
| CQ-PST-05 | client: item tooltip and WTHIT line | same table, Group "client" |
| CQ-PSD-08 | client: JEI search and WTHIT | SQ-PSD table, Group "client" |
| CQ-PSD-09 | client: owner visual sign-off on oat crop models | SQ-PSD table, Group "client" (LOOK, owner) |
| CQ-PSF-01 | client: stem models toward grown fruit | SQ-PSF table, Group "client" |
| CQ-PSU-01 | client: Curios slot tooltip, F5 render | SQ-PSU table, Group "client" |
| CQ-PSU-02 | client: item names (lang order) | SQ-PSU table, Group "client" |
| CQ-PSU-03 | client: tooltips and WTHIT walk-speed line | SQ-PSU table, Group "client" |
| CQ-PSU-04 | client: armor trim rendering, sprites | SQ-PSU table, Group "client" |
| CQ-PSU-05 | client: eating and grid crafts by hand | SQ-PSU table, Group "client" |

Server halves left to the client block (rows are in a batch, these parts are not): the real left-click of SQ-FCB-08
(the batch posts Forge's LeftClickBlock through /s5 lclick; CQ-FCB-04 covers the client), a physical spout and item
drain for SQ-DRK-04 (the batch calls Create's FillingBySpout / GenericItemEmptying, the logic both blocks run).
