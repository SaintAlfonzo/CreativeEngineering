# Baby mobs: the 1.21.2 / 26.1 "Tiny Takeover" babies on 1.19.2 (Baby Mobs, our own jar)

Test prefix `BM-`. Status: **milestone 1 BUILT 2026-10-20 (babyMobs-1.0.0.jar in `mods/`, not deployed): baby squid, glow
squid and dolphin with the 26.1 baby models and textures; BM-01 and BM-02 PASSED on the scratch server, the server halves of BM-04 to BM-07 scratch server PASSED 2026-10-22 (SQ-OM-13, SQ-OM-14), the rest need a client. Milestone 2 (the 26.1
remodel of the ~38 existing babies) is planned, not built.** [TODO](todo/TODO.md) `ports/babymobport #1`.

## 0. At a glance

- **What the user asked (2026-10-20):** "try to port the 1.20+ baby mobs back to 1.19.2 in a new jar. port every baby
  mob behavior, texture, model, drops, and acquire mechanic if any (breeding). spawning will be wired into the mob
  spawning system like mooblooms. don't port the golden dandelion, poison potato already does this."
- **What "the 1.20+ baby mobs" are:** Java 1.21.2 (24w33a) gave squid, glow squid and dolphins a baby form (natural
  spawns only, no breeding), and the 26.1 "Tiny Takeover" drop (2026-03-24) remodelled and retextured every baby mob
  (38 mobs), gave five of them their own sounds, added baby armour models for the undead and the golden dandelion.
  Nothing else in 1.20+ is a "baby mob" that 1.19.2 lacks: camels, sniffers and armadillos are whole mobs (not in the
  pack); baby zombie / villager / animal forms already exist here.
- **Where:** `development/babyMobs` (mod id `baby_mobs`, jar `babyMobs-<version>.jar`, GPLv3 like our other mods; the
  three baby textures are Mojang's 26.1 files). Root `settings.gradle` includes it; `tools/build_deploy.py`
  `IN_TREE_MODS` lists it; `mod_sides.md` row `both`.
- **Knobs (`config/baby_mobs-common.toml`):** `[spawns] squidBabyChance 0.05, glowSquidBabyChance 0.05,
  dolphinBabyChance 0.10`; `[growth] growTicks 24000, dolphinFishFeeding true`; `[client] babyModels true`.
- **How to see it:** `/summon minecraft:squid ~ ~ ~ {Age:-24000}` (also `glow_squid`, `dolphin`): a small squid
  with the 26.1 baby shape and texture; `/data get entity <it> Age` counts up to 0, then it is an adult.

## 1. Overview

Squid and dolphins are not `AgeableMob`s in 1.19.2, so the jar gives the three vanilla classes an age of their own
through mixins (section 3) and replaces their renderers on the client. No entity type, item or block is registered: a
world that loses the jar keeps every squid and dolphin, grown up (their `Age` tag is simply ignored by vanilla).

## 2. Reference: what 1.21.2 / 26.1 do

| | Squid, glow squid | Dolphin |
|---|---|---|
| How a baby appears | 5 % of natural spawns (vanilla `AgeableMobGroupData`: the first mob of a spawn group is always an adult, every further member rolls the chance); spawn eggs give adults; no breeding | 10 % of natural spawns, same rule; no breeding |
| Growing up | 24000 ticks (20 min), can't be sped up | 24000 ticks; each fish fed (`#minecraft:fishes`) takes 10 % off the time left (`AgeableMob.getSpeedUpSecondsWhenFeeding`), eat sound, fish used up |
| Size | hitbox 0.5 x 0.63 (26.2: 0.5 x 0.5) against 0.8 x 0.8; eye height 0.37 | 0.65 of the adult (0.585 x 0.39) |
| Looks (26.1) | own 32x32 texture (`squid_baby.png`, `glow_squid_baby.png`) and model: 8x10x8 body at y 13, 8 tentacles 2x6x2 on a 3-block ring at y 18.5; renderer offsets 0.25 / -0.6 instead of 0.5 / -1.2 | own 64x64 texture and model: 6x5x8 body at y 21.5, head 6x5x4 with a 2x2x2 nose, fins 1x3x6 at 0.8727 / 1.7017 rad, tail 4x3x7 + fin 8x1x4, back fin 1x3x4 |
| Sounds | pitch 1.3 to 1.7 (vanilla `getVoicePitch` for any baby) | same; eat / splash unchanged |
| Drops | none, no experience (vanilla `shouldDropLoot` / `shouldDropExperience` for any baby) | none, no experience |
| Behaviour | ink cloud a little tighter (0.1 instead of 0.3 offset) | passive: `canAttack` is false while young (adults are neutral and hit back) |
| Golden dandelion | pauses growth | pauses growth. **Not ported** (user: Quark's poisonous potato already stops baby growth) |

## 3. What the jar does (milestone 1)

- **Age state (`age/BabyAges`, `mixin/MobBabyMixin`):** `Mob` gets an `age` field (duck `BabyAgeHolder`) saved as
  `Age`, and one synced boolean per class (`SynchedEntityData.defineId` on `Squid` and `Dolphin`, defined at mod
  construction so it exists before any entity). `setAge` flips the flag and refreshes dimensions when 0 is crossed,
  `ageUp(seconds)` is vanilla's. `finalizeSpawn` (Mob, `m_6518_`): a null group becomes `AgeableMobGroupData(chance)`
  (`@ModifyVariable` at HEAD, as 1.21.2's Squid / Dolphin do) and at RETURN vanilla's roll runs: first member adult,
  then `chance` per member, baby = `-growTicks`.
- **Baby behaviour (`mixin/LivingEntityBabyMixin`):** `isBaby` -> age < 0 (client: the flag); `getScale` -> 0.625 for
  squid (0.5 / 0.8), 0.65 for dolphins, so vanilla's `getDimensions` shrinks the hitbox; `aiStep` TAIL ticks the age
  up to 0; `canAttack` false for a baby dolphin; `onSyncedDataUpdated` refreshes the client hitbox. Pitch, loot and
  experience need nothing: vanilla already keys them off `isBaby()`.
- **Dolphin feeding (`mixin/DolphinFeedMixin`):** HEAD of `Dolphin.mobInteract`: baby + fish -> eat sound, fish taken
  (not in creative), `ageUp(getSpeedUpSecondsWhenFeeding(-age))`, success; adults keep vanilla's treasure behaviour.
- **Rendering (`client/`):** `EntityRenderersEvent.RegisterRenderers` re-registers squid, glow squid and dolphin with
  `BabySquidRenderer` / `BabyGlowSquidRenderer` / `BabyDolphinRenderer` (subclasses of vanilla's; a later
  registration replaces the renderer). While the mob is young they swap `model` for a vanilla `SquidModel` /
  `DolphinModel` baked from the ported 26.1 geometry (`BabyModels`, part names as 1.19.2 expects), use the baby
  texture and skip the young-flag half-scale (the mesh is already baby-sized); the squid renderer uses 26.1's baby
  rotation offsets. `babyModels = false` renders the adult model at half size like any vanilla baby.
- **Spawning in the pack:** nothing to wire beyond the chances: babies are part of the adult spawn groups, and the
  adult lists stay with [mob_spawns.md](mob_spawns.md) (`mob_spawns.json`). Spawn eggs and `/summon` give adults
  unless `Age:-24000` is set, as vanilla.

## 4. Limits and decisions

- D1 **Golden dandelion:** not ported (user). Growth pause stays Quark's poisonous potato (works on `AgeableMob`
  babies through Quark's own code; squid / dolphin babies are not `AgeableMob`s, so the potato does nothing on them;
  a hook could be added if wanted).
- D2 **Breeding:** none in vanilla for these three, none here.
- D3 **Ink offset:** the 0.1 / 0.3 ink particle offset for babies is not ported (private `spawnInk`; cosmetic).
- D4 **Hitbox height:** 26.1's 0.63 became 0.5 in 26.2; the port uses a uniform 0.625 scale (0.5 x 0.5), the later
  value.
- D5 **Milestone 2, the 26.1 remodels (NOT BUILT):** 26.1 gives these babies new models (classes
  `net.minecraft.client.model.*.Baby*Model`, read from the unobfuscated 26.1 client jar) and textures (`*_baby.png`):
  armadillo (n/a), axolotl, bee, camel (n/a), cat / ocelot, chicken, cow, mooshroom, donkey, horse, mule, skeleton and
  zombie horse, fox, goat, llama, trader llama, panda, pig, polar bear, rabbit (whole mob), sheep, strider, turtle,
  wolf, hoglin, zoglin, piglin, zombified piglin, zombie, husk, drowned, zombie villager, villager, plus baby sounds
  for cat, chicken, horse, pig, wolf and baby armour models for the undead. Each needs the geometry ported into a
  1.19.2 model class with the same part names as the vanilla model it replaces (or a new model + renderer), its
  texture, and a renderer swap like section 3. Our own mooblooms (More Mooblooms) and the pack's many mob mods are
  untouched either way. Build it one family at a time (cow / mooshroom / moobloom first: shared model).
- D6 **Mojang textures in the jar:** the three `*_baby.png` files are copied from the 26.1 client. The user asked for
  the textures; keep the jar in the pack (not published) or replace them with own art before publishing.

## 5. File map

- [development/babyMobs/](../development/babyMobs/): `BabyMobs` (entry), `BMConfig`, `age/BabyAges`,
  `age/BabyAgeHolder`, `mixin/MobBabyMixin`, `mixin/LivingEntityBabyMixin`, `mixin/DolphinFeedMixin`,
  `client/ClientSetup`, `client/BabyModels`, `client/BabySquidRenderer`, `client/BabyGlowSquidRenderer`,
  `client/BabyDolphinRenderer`; `baby_mobs.mixins.json`; `assets/baby_mobs/textures/entity/squid/{squid,glow_squid}_baby.png`,
  `dolphin/dolphin_baby.png`.
- Reference source: the 26.1 client jar (unobfuscated since 26.1; `piston-meta` version json has no mappings) decompiled
  with `tools/decompile_mod.py` into the session scratchpad: `BabySquidModel`, `BabyDolphinModel`, `SquidRenderer`,
  `DolphinRenderer`, `Squid`, `Dolphin`, `AgeableWaterCreature`.

## 6. Offline checks

- `./gradlew :babyMobs:build --offline` (root), jar in `development/babyMobs/build/libs/`.
- SRG names in the mixins were taken from the ForgeGradle `srg_to_official_1.19.2.tsrg` (m_6518_ finalizeSpawn,
  m_8097_ defineSynchedData, m_7380_ / m_7378_ save / load, m_6162_ isBaby, m_6134_ getScale, m_8107_ aiStep,
  m_6779_ canAttack, m_7350_ onSyncedDataUpdated, m_6071_ mobInteract).

## 7. Tests

- [x] **BM-01 Summon babies [scratch]:** `/summon minecraft:squid ~ ~ ~ {Age:-24000}` (and glow_squid, dolphin) in
  water -> `/data get entity` shows `Age: -24000` rising by 20 every second; a plain `/summon` gives `Age: 0`. *PASSED
  scratch 2026-10-20 (nw4): all three read -24000, the plain squid 0; 66 s later squid and dolphin read -22687 (1313
  ticks). Right after `/forceload add` the chunk's entities took a few seconds to start ticking (known).*
- [x] **BM-02 Grows up [scratch]:** `Age:-100` -> after 5 s the mob is an adult (`Age: 0`, full-size hitbox). *PASSED
  scratch 2026-10-20: `Age: 0` after 9 s (hitbox not measurable from the console).*
- [ ] **BM-03 Looks [game]:** a baby squid / glow squid / dolphin shows the 26.1 baby shape and texture (stubby body,
  short tentacles; short dolphin with a round head), floating at the right height; the hitbox (F3+B) is small; a glow
  squid baby still glows and dims when hit
- [ ] **BM-04 Sounds, drops [game]:** *Server half: scratch server PASSED 2026-10-22 (SQ-OM-13, no items or orbs from babies).* baby sounds are high-pitched; killing a baby drops nothing and no experience
- [ ] **BM-05 Dolphin feeding [game]:** *Server half: scratch server PASSED 2026-10-22 (SQ-OM-13, age -24000 -> -21600 per cod).* a baby dolphin fed cod ages up 10 % per fish (eat sound, fish gone); it never
  attacks when hit; an adult fed cod still leads to treasure
- [ ] **BM-06 Natural babies [game]:** *Server half: scratch server PASSED 2026-10-22 (SQ-OM-13, first of a pack never a baby).* in a fresh ocean, among many squid / dolphins about 1 in 20 squid and 1 in 10
  dolphins are babies (never the first of a group); `glowSquidBabyChance` likewise in caves
- [ ] **BM-07 Config [game]:** *Server half: scratch server PASSED 2026-10-22 (SQ-OM-14, 1200 of 1200 pack members after the first are babies).* `babyModels = false` -> babies render as half-size adults; `squidBabyChance = 1` -> every
  squid after the first of a group is a baby
- [ ] **BM-08 Without the jar [game]:** remove the jar -> existing babies are adults, no errors

## 8. History

- 2026-10-20: milestone 1 built (babyMobs-1.0.0) after the tier-1 / tier-2 feature ports; milestone 2 planned.
- 2026-10-22 (second bulk run): SQ-OM-13 and SQ-OM-14 PASS on the scratch server (drops, dolphin feeding, pack shares,
  `squidBabyChance = 1.0`: 1200 of 1200). Looks, sounds and the `babyModels` switch of BM-04 to BM-07 stay in game.
