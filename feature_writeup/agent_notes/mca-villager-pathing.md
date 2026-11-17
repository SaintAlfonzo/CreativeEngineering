# mca-villager-pathing

> MCA villagers path with their own node maker, so WalkNodeEvaluator mixins (vanilla or other mods') never reach them

Moved from agent memory 2026-11-16 (last edited there 2026-10-31). Repo status docs win over any status line here.

MCA overwrites vanilla villagers (`overwriteOriginalVillagers: true`) and VillagerEntityMCA always uses
`VillagerNavigation` -> `forge.net.mca.entity.ai.pathfinder.VillagerLandPathNodeMaker` (extends NodeEvaluator, not
WalkNodeEvaluator). Block classification is its static `getCommonNodeType(BlockGetter, BlockPos)`; `findNearbyNodeTypes`
adds overrides (it made every fence gate WALKABLE_DOOR). The door task depends on `useSmarterDoorAI` (pack: false ->
vanilla InteractWithDoor, which Dramatic Doors extends; true -> MCA SmarterOpenDoorsTask, vanilla doors and gates only).

Ladder gotchas (2026-10-30, mob_block_tweaks.md 2.8): vanilla MoveControl jumps every tick in a ladder (its
collision shape is at the feet), so mobs can only climb up; and GroundPathNavigation.canUpdatePath is false off the
ground, so a descending mob skips every rung node at once.

**Why:** Any mod that teaches villagers a block via WalkNodeEvaluator (Dramatic Doors did) silently misses MCA villagers.
**How to apply:** for villager pathing changes, patch `VillagerLandPathNodeMaker` (see patcherMod
`McaVillagerPathGatesMixin`, mob_block_tweaks.md 2.7) and keep a WalkNodeEvaluator twin only for unconverted villagers.
