# baby-mobs-backport

> Baby Mobs (baby_mobs, now CD member members/babymobs) = 1.21.2 baby squid/dolphin + 26.1 Tiny Takeover remodels of 33 more babies via a generic LivingEntityRenderer kit framework; no golden dandelion

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

2026-10-20 (feature_writeup/baby_mobs.md, BM-01..08; TODO largecontent/babymobport #1 In Progress). User's definition
of "the 1.20+ baby mobs": port every baby behaviour, texture, model, drops and acquire mechanic; spawning via the
pack's spawn system like mooblooms; NOT the golden dandelion (Quark poisonous potato already pauses growth).
- Facts: 1.21.2 (24w33a) added baby squid/glow squid (5 % of natural spawns) and dolphins (10 %), no breeding, 20 min
  growth, dolphin fish feeding 10 %/fish, babies drop nothing. 26.1 "Tiny Takeover" (2026-03-24) remodelled every
  baby (classes `net.minecraft.client.model.*.Baby*Model`, textures `*_baby.png`) + golden dandelion + name tag recipe.
- The 26.1 client jar is UNOBFUSCATED (version json has no client_mappings): `tools/decompile_mod.py` on an extracted
  sub-jar reads it directly. GlowSquid is `net.minecraft.world.entity.GlowSquid` in 1.19.2 (not .animal).
- Design: `age/BabyAges` + `MobBabyMixin` (duck age field, synced flag per class via defineId on Squid/Dolphin at mod
  construct, Age NBT, finalizeSpawn @ModifyVariable group + RETURN roll), `LivingEntityBabyMixin` (isBaby/getScale/
  aiStep/canAttack/onSyncedDataUpdated with instanceof filters; vanilla keys pitch, loot, xp, dimensions off isBaby),
  `DolphinFeedMixin`; client re-registers SquidRenderer/GlowSquidRenderer/DolphinRenderer subclasses that swap
  `model` (protected, non-final in LivingEntityRenderer) to a vanilla SquidModel/DolphinModel baked from the ported
  26.1 geometry (part names kept), skip the young half-scale, 26.1 squid offsets 0.25/-0.6.
- Mixins in a mixin class: `this instanceof Mob` needs `(Object) this` (compile error otherwise).
- In mods/ + mod_sides row + build_deploy IN_TREE_MODS + settings.gradle; scratch: summon with {Age:-24000}.
- 2026-11-01: moved to development/creativeDelight/members/babymobs (build `:creativeDelight:build`, nested
  babyMobs-1.1.0). Spawn egg on adult needed a `Mob.setBaby` (m_6863_) hook: 1.19.2's Mob.setBaby is empty and
  SpawnEggItem drops the offspring unless isBaby() is true afterwards.
- Milestone 2 framework: `client/kit/BabyKit` per entity type, applied by a LivingEntityRenderer mixin that swaps
  `model` only if it is instanceof the kit's 1.19.2 adult class, forces `young` false, swaps texture in getRenderType
  and layer textures/models by adult layer texture key. Anything 1.19.2 keys off `young` (armour, held item, head,
  renderer-wide scales) needs per-family fixes. 26.1 client jar: piston-meta version json, Vineflower.
- Offline mixin check: javap against forge-1.19.2-43.3.9-srg.jar (gradle cache) for every remap=false target.
Related: [feature-ports-2026-10-20](feature-ports-2026-10-20.md), [moobloom-variants](moobloom-variants.md), [root-gradle-build](root-gradle-build.md).
