# incubation-additions-mod

> development/incubationAdditions in-tree mod (incubation_additions) - emu/duck/goose nests moved out of patcherMod with old-id remap; install both jars together; status in feature_writeup/nests.md

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

Created 2026-10-05 at the user's request ("just like the camera backport"). Third project in root settings.gradle; IN_TREE_MODS row added in tools/build_deploy.py; mod_sides.md row added (marked NOT in mods/).

- Holds everything nest-related that was in patcherMod: nest/EmuNests, nest/WaterfowlNests, the 4 mixins (Emu/Duck/Goose EggLayer + LayEggInNestGoal), blockstates/models/textures/lang, 6 loot tables. All assets are in the jar (the duck/goose files that were briefly in kubejs/assets/creativeengineering moved in too); hotswap = same-path PNG under kubejs/assets/incubation_additions/ + F3+T.
- Incubation is the only mandatory dep. IncubationAdditionsMixinPlugin skips a bird's EggLayer mixin, and IncubationAdditions skips its registration, when alexsmobs / untitledduckmod is absent.
- `remapOldIds` (MissingMappingsEvent, same pattern as ExposureExtras for the camera bags) maps creativeengineering:{emu,duck,goose}_nest + *_egg_nest blocks and the 3 items to incubation_additions:.
- No refmap, SRG names + remap=false, -AdisableTargetValidator (same as patcherMod).
- KubeJS item_dict/block_dict now point at incubation_additions: ids.

**Verified on a scratch server 2026-10-05:** a world saved with the old jar keeps its nests after the swap (egg counts and chest items intact). Duck 3->4 and goose 5->6 laid into their nests. An emu alone laid into a fresh emu nest. Geese pick up loose eggs, so test emus without a goose present.

**Install rule:** install BOTH jars together (`./gradlew :patcherMod:installToMods :incubationAdditions:installToMods`); one without the other loses or duplicates nests. NE-12 is the old-world check. Current state: feature_writeup/nests.md.

Writeup: feature_writeup/nests.md. Related: [incubation-nest-internals](incubation-nest-internals.md), [root-gradle-build](root-gradle-build.md), [exposure-backport](exposure-backport.md), memory `scratch-server-verify`.

**Per-mod gating + crates (2026-10-05, later):**
- User rule: emu content only with Alex's Mobs, duck/goose content only with Untitled Duck Mod, and with neither the mod does NOTHING.
- Implementation: `ContentSet` list in IncubationAdditions; the mixin plugin gates all 4 mixins. Resources live in `packs/<mod id>/` (no root assets/data), added via AddPackFindersEvent as a required, hidden built-in pack at Position.BOTTOM.
- Duck/goose egg crates are in crate/WaterfowlEggCrates (Properties.copy of the chicken crate, flammable 5/20). 9 eggs <-> crate.
- The duck mod's egg item sprites are swapped via client/DuckModEggModels (RegisterAdditional + BakingCompleted model swap), NOT shipped as assets/untitledduckmod files.
  - Why: inside mod_resources the first jar in mod order wins a path.
  - Why not Position.TOP: KubeJS's GeneratedClientResourcePack.inject inserts kubejs/assets just below the first FilePackResources (zip), so a TOP pack would sit above kubejs/assets and break the hotswap.
- Emu nest bark drop goes through an optional item tag (`emu_nest_drops`), so the table loads without IW.
- Nest recipes and the bird_nests tag moved from KubeJS into the packs.

**Verified (2026-10-05):** minimal scratch servers (tools: scratchpad minsrv.sh pattern: Forge + Incubation + Blueprint + jar, plus one bird mod) and the full pack (both packs on, 7 recipes, bark drop).

**NOT verified:** the client half: resource pack loading, the model swap, the crate textures (NE-13/14).
