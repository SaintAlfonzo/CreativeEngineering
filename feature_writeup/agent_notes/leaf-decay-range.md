# leaf-decay-range

> CEConfig [leaf_decay] - range 12 (vanilla 6), decay only from scheduled ticks (onlyWhenDisturbed), Fast Leaf Decay ported in (jar retired); LeavesDecayRangeMixin randomTick HEAD + tick TAIL

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

Built 2026-10-19 into Creative Common 1.0.9 (rebuilt in mods/). Writeup: feature_writeup/mob_block_tweaks.md
2.5 (MT-13..18; scratch server passed 13-16); TODO `farming/trees #1`.

- Hook: HEAD of `LeavesBlock#randomTick` (m_213898_); Fast Leaf Decay and the pack's fruit leaves all end there.
  `block/LeafDecayRange.keep` BFS through distance-7 leaves only; neighbor with d<7 at k steps = log at k+d.
- Separated Leaves rules read by reflection (`Reload.LEAVES_RULES`, record `leaves()`).
- maxDistance semantics: longest surviving leaf path, vanilla 6 (an early draft had an off-by-one).
- Default 12 chosen from measurement: all 310 tree features placed on a scratch server (tools/leaf_path_probe.js):
  longest path 10 (Terralith swamp_small / cave jungle / max_spruce_big, Vinery apple 7). Templates
  (tools/leaf_path_scan.py): none past 6.
- Timing (user liked this approach): randomTick decay canceled; `tick` TAIL (`afterTick`) decides: before<7 -> recheck
  in decayMin..MaxTicks, before==7 -> keep() or drop. Fast Leaf Decay ported as `block/FastLeafDecayPort`
  (NeighborNotifyEvent on air -> scheduleTick); FastLeafDecay-30.jar in disabledmods/, mod_sides row gone,
  pack/ + server_mods regenerated.
- Gotcha: TreeFeature ends with StructureTemplate.updateShapeAtEdge, so edge leaves of a just-placed tree DO get one
  tick: diagonal-only floaters and leaves past range drop at placement/first load (F6). Within 12 survives.
- Scratch numbers: 1351-leaf spruce felled -> gone in ~1 s, worst tick 146 ms (incl. /fill); flags 19 removal decays
  via the event, flags 18 doesn't (same as FLD). tools/leaf_decay_probe.js.

Related: [creative-common-coremod](creative-common-coremod.md), memory `scratch-server-verify`.

**2026-10-20 (Creative Common 1.1.01, in mods/, MT-24/25):** IW leaf piles restored. IW's LeavesMixin calls
`LeavesGrowth.decayLeavesPile` after removeBlock in randomTick (FLD ran randomTick, so it got piles); our tick-path
decay now calls it via `block/LeafDecayPiles` (`[leaf_decay] leafPiles`). `[leaf_piles] slowFromLayers` (default 4 =
IW's `layers > 3`) via `mixin/LeafPileSlowMixin` (@ModifyConstant on the only iconst_3 in LeafPileBlock m_7892_);
WTHIT `surface/StuckInBlock.leafPile` reads the same value.
