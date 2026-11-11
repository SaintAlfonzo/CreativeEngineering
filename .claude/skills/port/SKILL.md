---
name: port
description: Port or backport a mod (or a feature of one) to the pack's Forge 1.19.2 - licence route, in-tree mod or CD member, format changes, id rules. Use for "backport X", "port feature Y", newer-upstream carry-overs.
---

# Port or backport

Models: [veggies_delight.md](../../../feature_writeup/veggies_delight.md) 4.1 (fork of a permissive mod),
[miners_delight.md](../../../feature_writeup/miners_delight.md) (ARR donor-jar backport),
[feature_porting.md](../../../feature_writeup/feature_porting.md) (small mods absorbed into KubeJS / patcherMod),
[devmods_evaluation.md](../../../feature_writeup/devmods_evaluation.md) section 1 (newer upstream against ours).

1. **Survey first.** Run the `integration-report` skill (or extend the mod's writeup) so overlaps and decisions
   exist before code. Owner decides drops after seeing overlaps.
2. **Licence route** (record it in the writeup):
   - GPL-compatible / MIT code and assets: fork into `development/backported<Name>`, our code GPLv3, keep the
     upstream notice.
   - ARR (code or assets): upstream jar unmodified in `<name>_upstream/` as a donor; our jar ships only our own
     code written from behaviour, never translated from the decompile (memories `miners-delight-backport-plan`,
     `backported-vinery`, `farmers-structures-donor`).
     Donor jars go unmodified to local dev instances only; at publish the pack links official sources and Creative
     Common files the detected jars into the upstream folder (memory `upstream-jar-distribution-policy`). Do not
     raise rehosting as a blocker.
   - Small feature: tier it as in feature_porting.md (KubeJS / data, or one patcherMod handler or mixin).
3. **Home.** Food content: Creative Delight member or CD tree (memory `creative-delight-routing`); else its own
   in-tree mod. New project: root `settings.gradle`, `installToMods`, `IN_TREE_MODS` in `tools/build_deploy.py`,
   `mod_sides.md` row.
4. **Rules.** Keep upstream ids (data and textures carry over). No old-version or cross-mod conversions. No
   advancements (dropped at load). Worldgen wraps into 1.19.2 vanilla types, never backports new worldgen types.
   Ids stay in `item_dict.js`; recipes via `global.NCH`, tags via `global.tags`.
5. **1.20 -> 1.19.2 format**: `Material` on block properties; `CreativeModeTab` subclass; recipe JSON without
   `category`; loot `any_of` -> `alternative`; drop dangling GLM entries; `SimpleJsonResourceReloadListener`
   path; FD effect suppliers resolved too early. 1.21 NeoForge: also result/ingredient format, data components.
6. **Build**: `./gradlew :<mod>:build --offline`, logic tests in a javac harness, then `:<mod>:installToMods`
   only with the game closed. Live checks go to [scratch_queue.md](../../../feature_writeup/testing/scratch_queue.md).
7. **Integrate** the fold-in steps of the report; then the `add-mod` docs steps (sides, dumps, changelog).
   Same-behaviour ports get no changelog bullet; new content does.
