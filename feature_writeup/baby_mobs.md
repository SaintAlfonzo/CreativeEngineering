# Baby mobs: the 1.21.2 / 26.1 "Tiny Takeover" babies on 1.19.2 (Baby Mobs, our own jar)

Test prefix `BM-`. Status: **milestone 1 BUILT 2026-10-20 (babyMobs-1.0.0.jar, deployed since 1.1.01, 2026-10-22): baby squid, glow
squid and dolphin with the 26.1 baby models and textures; BM-01 and BM-02 PASSED on the scratch server, the server halves of BM-04 to BM-07 scratch server PASSED 2026-10-22 (SQ-OM-13, SQ-OM-14), the rest need a client. Milestone 2 (the 26.1
remodel of every other baby) BUILT 2026-11-01 as babyMobs 1.1.0 (nested in Creative Delight), not yet installed or seen in game; spawn egg on an adult fixed the same day.** [TODO](todo/TODO.md) `ports/babymobport #1`.

## 0. At a glance

- **What the user asked (2026-10-20):** "try to port the 1.20+ baby mobs back to 1.19.2 in a new jar. port every baby
  mob behavior, texture, model, drops, and acquire mechanic if any (breeding). spawning will be wired into the mob
  spawning system like mooblooms. don't port the golden dandelion, poison potato already does this."
- **What "the 1.20+ baby mobs" are:** Java 1.21.2 (24w33a) gave squid, glow squid and dolphins a baby form (natural
  spawns only, no breeding), and the 26.1 "Tiny Takeover" drop (2026-03-24) remodelled and retextured every baby mob
  (38 mobs), gave five of them their own sounds, added baby armour models for the undead and the golden dandelion.
  Nothing else in 1.20+ is a "baby mob" that 1.19.2 lacks: camels, sniffers and armadillos are whole mobs (not in the
  pack); baby zombie / villager / animal forms already exist here.
- **Where:** `development/creativeDelight/members/babymobs` (mod id `baby_mobs`, jar `babyMobs-<version>.jar` nested in
  Creative Delight since the 2026-11-01 split; build with `:creativeDelight:build`). GPLv3 like our other mods; the baby
  textures are Mojang's 26.1 files (D6).
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
| How a baby appears | 5 % of natural spawns (vanilla `AgeableMobGroupData`: the first mob of a spawn group is always an adult, every further member rolls the chance); spawn eggs give adults, one used on an adult gives a baby; no breeding | 10 % of natural spawns, same rule; no breeding |
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
- **Spawn egg on an adult (fixed 2026-11-01):** vanilla's `spawnOffspringFromSpawnEgg` creates the mob, calls
  `Mob.setBaby(true)` and drops it unless `isBaby()` is then true. `Mob.setBaby` is empty in 1.19.2 and squid /
  dolphins don't override it, so the egg did nothing on an adult. `MobBabyMixin` now hooks `m_6863_` setBaby:
  `BabyAges.setBaby` -> age `-growTicks` (or 0 for false).

## 3a. Milestone 2: the 26.1 remodels (built 2026-11-01, babyMobs 1.1.0)

User (2026-11-01): "id also like all of the other new baby mob textures ported". The 26.1 baby textures only fit the
26.1 baby meshes, so every family got its model ported too. Reference: the 26.1 client jar from piston-meta,
decompiled with Vineflower (models, renderers, `LayerDefinitions`).

- **Framework (`client/kit/`):** `BabyKit` = one baby per entity type: the 26.1 mesh, a factory that builds an
  instance of the 1.19.2 adult model class, the texture choice, layer swaps keyed by the adult layer texture
  (`layerTexture`, `layerTexturePrefix` fallback, `layerModel` for layers with their own model), an optional pose
  transform. `BabyRender` = registry + render stack. `mixin/client/LivingEntityRendererBabyMixin`: at render HEAD the
  renderer's `model` becomes the baked baby model (only if the renderer's model is an instance of the kit's adult
  class, so Quark's variant renderers work and foreign models are left alone), `young` is forced false (no 1.19.2
  half-size shrink), the kit's texture replaces the body texture in `getRenderType`, the pose hook runs after
  `scale()`, the adult model is restored at RETURN. `RenderLayerBabyMixin`: swaps layer textures / models in
  `coloredCutoutModelCopyLayerRender` and `renderColoredCutoutModel`. A kit with no texture for an entity is skipped.
  Squid and dolphins keep their milestone 1 renderers. `babyModels = false` turns all of it off.
- **Families (`client/family/`):** `FarmBabies` cow, mooshroom, pig, sheep (+ wool layer), chicken; `PetBabies` cat,
  ocelot, wolf, fox, rabbit (+ `PetAnimations` keyframes); `EquineBabies` horse, donkey, mule, skeleton / zombie
  horse, llama, trader llama; `MiscBabies` bee, axolotl, goat, panda, polar bear, turtle, strider, hoglin, zoglin
  (+ `MiscAxolotlAnimation`); `UndeadBabies` zombie, husk, drowned (+ outer layer), zombie villager, villager,
  piglin, zombified piglin, with baby armour (26.1 `humanoid_baby` textures, per-slot baby armour meshes). Family
  mixins: `EquineHorseMarkingMixin`, `EquineHorseArmorMixin`, `EquineLlamaDecorMixin`, `PetFoxHeldItemMixin`,
  `UndeadArmorLayerMixin`, `UndeadItemInHandMixin`, `UndeadCustomHeadMixin`.
- **How the ports fit 1.19.2:** each baby model is a subclass (or plain instance) of the 1.19.2 adult class, with
  empty placeholder parts where 1.19.2's constructor needs parts the baby lacks; 1.19.2 code that writes absolute
  adult positions is replaced by the 26.1 baby animation (or, humanoids, offset by baby minus adult defaults; piglin
  default poses set by reflection, `f_103333_/f_103334_/f_103337_/f_103338_`). Renderer-wide scales 26.1 moved into the
  adult meshes are undone for babies by pose hooks (horse 1.1, donkey 0.87, mule 0.92, cat 0.8, polar bear 1.2,
  strider 0.5, husk 1.0625, villager 0.46875).
- **Variant mobs (owner, 2026-11-01): config `[client] variantBabies`.** A variant = the renderer's own adult
  texture (with other mods' hooks applied; `BabyRender.begin` asks the renderer) is outside the `minecraft`
  namespace: More Mob Variants coats, Quark pride bees / shiny animals / cow-pig-chicken coats, Cozy's end and
  nether cats. Kits can name pool namespaces that count as regular coats (cat: `moremobvariants`).
  - `OLD_MODELS` (default): the species in `keepVanillaBabies` (wolf, cow, pig, chicken, sheep, zombie) keep the
    1.19.2 baby; on every other kit a variant baby keeps the 1.19.2 baby with its coat, mixed in with remodelled
    babies of the same mob (pride bees among new-model bees, Cozy's end / nether kittens among new-model kittens,
    Quark shiny rabbits and llamas). More Mob Variants cat coats use the baby of the cat's own vanilla coat.
  - `NEW_MODELS`: every kit applies to every baby. A coat with a 26.1 baby gets it (More Mob Variants ashen, black,
    chestnut, rusty, snowy, spotted, striped and woods wolves, also tame / angry); any other coat uses the mob's
    regular baby (cow, pig, chicken temperate; pale wolf; plain bee; the cat's vanilla coat). More Mob Variants' wool
    and wolf collar textures go to the baby wool / collar (`layerPrefixAs`); its pig mud, sheep horns and
    sheared-wool layers are skipped on remodelled babies (`PetMoreMobVariantsLayersMixin`, @Pseudo).
  - MCA villagers are their own types (`mca:male_villager`, `mca:female_villager` and the zombie ones): no kit, their
    baby skins stay. Modded villager biomes on vanilla villagers use the plains baby clothes in both modes.
  - Squid and dolphins keep their milestone 1 renderers (a Quark shiny dolphin baby shows the plain baby).
  - Open: baby art for every variant, TODO `ports/babymobport #2`.
- **Cozy's Improved Cats:** its `cozys-improved-cats:improved_cat` (extends Cat, renderer extends CatRenderer,
  vanilla CatModel) shares the cat kit, registered by id (`BabyRender.register(ResourceLocation, kit)`, resolved at
  render time, since the type doesn't exist when the families register). `PetCozyYarnMixin` (@Pseudo, require 0):
  the yarn ball in a baby's mouth skips Cozy's old half-size baby offset and scales 0.75 at the head.
- **Decisions / deviations (from the family reports):** babies never show carpets, horse armour, saddles or chests
  (as 26.1); trader llama babies get the 26.1 baby trader decor; held items and head items are full size (26.1);
  modded armour on a baby falls back to 1.19.2's half-size adult armour; no armour trims on babies (26.1);
  ElytraLayer and the villager crossed-arms item layer are not ported; catloaf's loaf pose doesn't apply to baby
  cats; cozy's improved cats (own entity type) keep the 1.19.2 baby; Quark variant / pride bee, More Mob Variants
  zombie and modded villager-type babies use the single baby texture (modded villager types: plains clothes);
  the 26.1 axolotl quirk (no underwater-walk animation) is kept; the rabbit head-tilt timer only runs while rendered;
  baby turtle legs draw without back-face culling; humanoid attack swing keeps the baby shoulder offset.
- **Mixin check:** every injection target was checked against the SRG Forge jar (`forge-1.19.2-43.3.9-srg.jar`,
  javap) on 2026-11-01; all resolve.

## 4. Limits and decisions

- D1 **Golden dandelion:** not ported (user). Growth pause stays Quark's poisonous potato (works on `AgeableMob`
  babies through Quark's own code; squid / dolphin babies are not `AgeableMob`s, so the potato does nothing on them;
  a hook could be added if wanted).
- D2 **Breeding:** none in vanilla for these three, none here.
- D3 **Ink offset:** the 0.1 / 0.3 ink particle offset for babies is not ported (private `spawnInk`; cosmetic).
- D4 **Hitbox height:** 26.1's 0.63 became 0.5 in 26.2; the port uses a uniform 0.625 scale (0.5 x 0.5), the later
  value.
- D5 **Milestone 2, the 26.1 remodels: BUILT 2026-11-01, see section 3a.** Original plan: 26.1 gives these babies new models (classes
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

- [development/creativeDelight/members/babymobs/](../development/creativeDelight/members/babymobs/) (milestone 2 files: section 3a): `BabyMobs` (entry), `BMConfig`, `age/BabyAges`,
  `age/BabyAgeHolder`, `mixin/MobBabyMixin`, `mixin/LivingEntityBabyMixin`, `mixin/DolphinFeedMixin`,
  `client/ClientSetup`, `client/BabyModels`, `client/BabySquidRenderer`, `client/BabyGlowSquidRenderer`,
  `client/BabyDolphinRenderer`; `baby_mobs.mixins.json`; `assets/baby_mobs/textures/entity/squid/{squid,glow_squid}_baby.png`,
  `dolphin/dolphin_baby.png`.
- Reference source: the 26.1 client jar (unobfuscated since 26.1; `piston-meta` version json has no mappings) decompiled
  with `tools/decompile_mod.py` into the session scratchpad: `BabySquidModel`, `BabyDolphinModel`, `SquidRenderer`,
  `DolphinRenderer`, `Squid`, `Dolphin`, `AgeableWaterCreature`.

## 6. Offline checks

- `./gradlew :creativeDelight:build --offline` (root); Baby Mobs is nested in the Creative Delight jar (`META-INF/jarjar/babyMobs-<version>.jar`). `:babyMobs:compileJava` for a quick compile.
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
- [ ] **BM-09 Spawn egg on an adult [game]:** a squid / glow squid / dolphin spawn egg used on an adult of its kind
  spawns a baby (egg used up outside creative); used on the ground it still gives an adult
- [ ] **BM-10 Farm babies [game]:** baby mooshroom (red, brown): 26.1 shape and texture, legs walk; baby cow, pig,
  chicken, sheep, wolf and zombie keep the 1.19.2 half-size baby with their More Mob Variants / Quark coat
  (`keepVanillaBabies`); with `variantBabies = NEW_MODELS` all six are remodelled: a More Mob Variants ashen wolf
  baby shows the ashen baby (tame, angry, collar), an albino cow baby the temperate baby, a fuzzy sheep baby the baby
  wool with no horns, a muddy pig baby no mud
- [ ] **BM-11 Pet babies [game]:** baby cat (variants, collar on a tamed one, sit, lie down, sprint; a More Mob
  Variants coat shows its vanilla coat's baby), Cozy's improved cat (ordinary: new model; end and nether: 1.19.2 baby
  with its coat, and with `NEW_MODELS` the baby of its vanilla coat; yarn ball in a new-model kitten's mouth sits at
  the snout), ocelot, fox (red, snow, sleeping, item in mouth),
  rabbit (types, Toast, killer rabbit, hop, idle head tilt)
- [ ] **BM-12 Horse babies [game]:** baby horse in all 7 coats with each marking (size not 1.1x, legs visible,
  rearing, grazing, tail), skeleton / zombie horse, donkey, mule, llama (4 colours, no carpet), trader llama (baby
  decor fits); summoned with armour: none drawn
- [ ] **BM-13 Other babies [game]:** bee (hover height, angry, nectar; pride bees: 1.19.2 baby with the flag, mixed
  in with new-model babies; `NEW_MODELS` -> plain baby bee), axolotl (5 colours, land / water / play dead),
  goat (head tipped), panda (7 genes, sit, sneeze, eat), polar bear (not 1.2x), turtle, strider (full size, cold
  texture), hoglin / zoglin (headbutt)
- [ ] **BM-14 Humanoid babies [game]:** baby husk, drowned (outer layer), zombie villager and villager (7
  biomes; MCA replaces most villagers, use `/summon minecraft:villager ~ ~ ~ {Age:-24000}`), piglin (admire, dance),
  zombified piglin: shape, texture, walk, zombie arms; armour on each slot (all vanilla materials, dyed leather,
  glint, turtle helmet) fits; a sword / gold / trident in hand and a pumpkin / skull on the head sit right; one
  modded armour piece falls back to half-size armour; chicken jockey
- [ ] **BM-16 MCA [game]:** MCA baby villagers and zombie villagers keep their own skins and model
- [ ] **BM-15 Adults and log [game]:** adults of every mob above unchanged; inventory / spawn egg previews fine;
  `logs/latest.log` has no "Baby Mobs:" errors (an info line "renders with ..., not ..." names a mod renderer the kit
  skipped) and no "piglin default poses not reachable"; `babyModels = false` -> all babies are the 1.19.2 half-size
  adults

## 8. History

- 2026-11-01: spawn egg on an adult squid / dolphin fixed (`setBaby` hook). Milestone 2 built (babyMobs 1.1.0): the
  generic renderer framework plus five families (33 mobs), ported from the decompiled 26.1 client by family, in
  parallel; compiles, mixin targets checked against the SRG jar, not run in game. Same day (owner): wolf, cow, pig,
  chicken, sheep, zombie back to the 1.19.2 baby (`keepVanillaBabies`); Cozy's improved cats get the cat kit; cat
  coats without a baby use their vanilla coat's baby. Then (owner): `variantBabies` OLD_MODELS / NEW_MODELS; Cozy end
  / nether kittens and Quark pride bee babies keep the 1.19.2 baby in OLD_MODELS; More Mob Variants wolf coats map to
  the 26.1 wolf variant babies in NEW_MODELS; TODO `ports/babymobport #2` for baby art of every variant.

- 2026-10-20: milestone 1 built (babyMobs-1.0.0) after the tier-1 / tier-2 feature ports; milestone 2 planned.
- 2026-10-22 (second bulk run): SQ-OM-13 and SQ-OM-14 PASS on the scratch server (drops, dolphin feeding, pack shares,
  `squidBabyChance = 1.0`: 1200 of 1200). Looks, sounds and the `babyModels` switch of BM-04 to BM-07 stay in game.
