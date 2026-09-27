# CreativeEngineering2 — damage type findings

Scope: every damage source registered by vanilla 1.19.2, the ~330 jars in `mods/`, and the pack's own
code (`patcherMod`, `kubejs`). Generated 2026-09-20.

## 1. Method and caveats

**1.19.2 has no data-driven damage types.** They arrived in 1.19.4 (`data/*/damage_type/*.json`). Here a
"damage type" is a `net.minecraft.world.damagesource.DamageSource` instance: a `msgId` string plus a handful
of boolean flags. The flags are what decide how armor, enchantments, effects, shields and difficulty treat a hit.

How the list was built:

1. Read `death.attack.*` keys from every jar's `en_us.json` (every `msgId` has one; this found 122 keys).
2. Scanned all jars' bytecode for `new DamageSource / EntityDamageSource / IndirectEntityDamageSource`,
   and for classes with `DamageSource` in the name.
3. Traced the flag setters chained onto each construction with `javap -c`. The SRG method ids were
   mapped against the official-mapped Forge jar (`m_19380_` = `bypassArmor`, and so on).
4. Read subclass constructors and overrides (IE, Pneumatic, Allurement, Minecolonies, Supplementaries, Botania).

Limits:

- **Statically derived, not tested in game.** Flags are what the code sets. What a mob or machine actually does with
  them (for example a mod that cancels the event, or a mixin that changes damage) is not covered.
- **Mods that only call vanilla sources** (most mob melee, effects, the Guardian, and so on) create no new
  sources. They fall under the vanilla table.
- **Mods that build sources through a shared helper** (Collective `createDamageSource`, PuzzlesLib
  `CommonAbstractions.damageSource`, Alex's Mobs `MessageHurtMultipart`) can't be attributed to an id from
  bytecode alone. They probably build vanilla-shaped sources.
- The `.player`, `.item` and `.turret` suffixes and `farseer_0..3` are **death-message variants of one source**, not
  separate types. Copper poisoning stages 0 to 3 and the eight IE revolver rounds are separate sources.

## 2. What the flags do (1.19.2)

| Flag | Set by | Effect in game |
|---|---|---|
| **bypassArmor** | `bypassArmor()` | Skips armor points and toughness, and **skips shield blocking** (`isDamageSourceBlocked` returns false for it). Armor durability is not damaged. Resistance and enchantments still apply unless bypassMagic is also set. |
| **bypassMagic** | `bypassMagic()` | Skips the Resistance effect **and** protection enchantments. Only vanilla `starve` has it. |
| **bypassEnchantments** | `bypassEnchantments()` | Skips protection enchantments only. Vanilla `sonic_boom` and Alex's Mobs `farseer` have it. |
| **bypassInvul** | `bypassInvul()` | Hurts creative and invulnerable entities and ignores the invulnerability check. This is the void/kill tier. |
| **isFire** | `setIsFire()` | Fire Protection reduces it, Fire Resistance negates it, and fire-immune mobs ignore it. |
| **isMagic** | `setMagic()` | What your `creativeengineering:magic_resistance` effect keys on. Also the flag for witch resistance. |
| **isExplosion** | `setExplosion()` | Blast Protection reduces it. |
| **isProjectile** | `setProjectile()` | Projectile Protection reduces it. |
| **isFall** | `setIsFall()` | Feather Falling reduces it (x3 per level). |
| **scalesWithDifficulty** | `setScalesWithDifficulty()` | Multiplied by difficulty. **Zero on Peaceful for a player** (`Player.hurt` drops it), which is the reason your `BurnBlastDamage` skips it. |
| **damageHelmet** | `damageHelmet()` | Falling-block damage: a helmet cuts it by 75% and loses durability. |
| **noAggro** | `setNoAggro()` | Doesn't make the victim aggro on the attacker. No mod source sets it. |

Everything not flagged **bypassArmor** is reduced by armor, toughness and Resistance, and can be blocked by shields.

## 3. Vanilla 1.19.2 (traced from the mapped jar)

Legend: **A** bypassArmor, **Fi** fire, **Mg** magic, **Ex** explosion, **Pr** projectile, **Fa** fall,
**Df** scales with difficulty, **He** damageHelmet, **Iv** bypassInvul, **BM** bypassMagic, **BE** bypassEnchantments, `-` none.

### Constants
| msgId | Flags | Comes from |
|---|---|---|
| `inFire` | A Fi | Standing in fire or soul fire |
| `onFire` | A Fi | Burning after ignition |
| `lava` | Fi | Lava |
| `hotFloor` | Fi | Magma blocks |
| `lightningBolt` | - | Lightning |
| `inWall` | A | Suffocation |
| `cramming` | A | Entity cramming |
| `drown` | A | Drowning |
| `starve` | A BM | Hunger |
| `cactus` | - | Cactus |
| `sweetBerryBush` | - | Sweet berry bush |
| `fall` | A Fa | Fall damage |
| `stalagmite` | A Fa | Landing on a dripstone stalagmite |
| `fallingStalactite` | He | Falling dripstone |
| `anvil` | He | Falling anvil |
| `fallingBlock` | He | Other falling blocks |
| `flyIntoWall` | A | Elytra kinetic energy |
| `outOfWorld` | A Iv | Void |
| `generic` | A | Generic (misc commands and mods) |
| `magic` | A Mg | Poison, Instant Damage, Guardian beam |
| `wither` | A | Wither effect |
| `dragonBreath` | A | Dragon breath cloud. Your Create fan also uses it, at 4.0 damage. |
| `freeze` | A | Powder snow |
| `dryout` | - | Fish and axolotls out of water |

### Factories
| msgId | Flags | Comes from |
|---|---|---|
| `mob` | - | Mob melee |
| `player` | - | Player melee |
| `sting` | - | Bee |
| `arrow` | Pr | Arrows |
| `trident` | Pr | Trident |
| `thrown` | Pr | Snowballs, eggs, pearls. Your **Shrapnel** entity also uses it. |
| `witherSkull` | Pr | Wither skull |
| `fireball` and `onFire` | Fi Pr | Ghast and blaze fireballs |
| `fireworks` | Ex | Firework rocket |
| `indirectMagic` | A Mg | Splash and lingering potions, evoker fangs |
| `thorns` | Mg | Thorns enchantment and guardians |
| `explosion` and `explosion.player` | Ex Df | TNT, creepers, crystals |
| `badRespawnPoint` | Ex Df | Bed or anchor in the wrong dimension |
| `sonic_boom` | A BE Mg | Warden |

## 4. Modded sources

### 4.1 Create and Create addons
| msgId | Flags | Acts as | Source |
|---|---|---|---|
| `create.crush` | A Df | Armor-piercing crush. **Zero on Peaceful.** | Crushing wheels (also Design Decor's copy) |
| `create.mechanical_saw` | A | Armor-piercing cut | Mechanical saw |
| `create.mechanical_drill` | A | Armor-piercing drill | Drill, and DnDesire modular drill |
| `create.mechanical_roller` | - | Plain blunt | Roller |
| `create.run_over` | - | Plain blunt (`EntityDamageSource`) | Contraption or train hits |
| `create.potato_cannon` | Pr | Plain projectile | Potato cannon |
| `create.cuckoo_clock_explosion` | Ex | Explosion, no difficulty scaling | Cuckoo clock |
| `create.fan_fire` | Fi Df | Fire. **Zero on Peaceful.** | Smoking fan |
| `create.fan_lava` | Fi Df | Fire. **Zero on Peaceful.** | Blasting fan |
| `tesla_coil` | - | Plain, so armor works | Create Addition Tesla Coil |
| `barbed_wire` | - | Plain, so armor works | Create Addition barbed wire |
| `out_of_oxygen` | A | Armor-piercing suffocation | Create Gravity |
| `mob` (Guardian turret) | - | Plain mob melee | Create Guardian Beam Defense |

### 4.2 Immersive Engineering and Immersive Petroleum
| msgId | Flags | Notes |
|---|---|---|
| `ieRevolver_casull`, `_buckshot`, `_homing`, `_wolfpack`, `_silver`, `_potion` | - | **Not flagged Projectile**, so Projectile Protection does nothing and armor does work. |
| `ieRevolver_armorPiercing` | A | Bypasses armor and shields |
| `ieRevolver_dragonsbreath` | Fi | Fire |
| `ieRailgun` | A | Bypasses armor. Also not Projectile. |
| `ieSawmill` (blade), `ieCrushed`, `ieAcid`, `ieRazorWire` | - | Plain |
| `ieRazorShock`, `ieTesla`, `ieWireShock` | A | Electric, armor-piercing (`ElectricDamageSource`) |
| `ieTeslaPrimary` | A | Armor-piercing |
| Chemthrower "extinguish" drown | A | `getPlayerDrownDamage` |
| `ipFlarestack` | A Fi | Fire, armor-piercing |

### 4.3 Pneumatic Craft
| msgId | Flags | Notes |
|---|---|---|
| `pressure` | A | Over-pressure |
| `securityStation` | A | Security station |
| `droneOverload` | A Iv | The only mod source that also bypasses invulnerability |
| `armor_piercing` | A | Minigun AP ammo |
| `acid` | - | Etching acid, plain |
| `plastic_block` | - | Plain |

### 4.4 Others
| msgId | Flags | Acts as | Source |
|---|---|---|---|
| `securitycraft.fakeWater`, `.electricity` | A | Armor-piercing | SecurityCraft |
| `securitycraft.laser`, `.taser`, `.incorrectPasscode` | - | Plain | SecurityCraft |
| `farseer` (with `_0` to `_3`) | A BE Mg Df | Armor-piercing psychic magic. Resistance still works, Magic Resistance applies, **zero on Peaceful**. | Alex's Mobs farseer |
| `freddy` | - | Plain | Alex's Mobs |
| `allurement.shockwave` | - (conditional Df) | Plain | Allurement |
| `botania-relic` | - | Plain | Botania relic curse |
| `health_siphon` | - | Plain | Domestication Innovation |
| `wasWatered` | A | Armor-piercing water damage | Ender's Delight |
| `sneak_balloon_attack` | - | Plain | Cloud Storage |
| `everythingcopper.copper_poisoning` (`0` to `3`) | - | Plain, four stages, armor works | Everything Copper |
| `supplementaries.bamboo_spikes` | - | Plain | Supplementaries |
| `supplementaries.xp_extracting` | - | Plain | Supplementaries |
| `supplementaries.bomb_explosion` | Ex Df | Explosion. **Zero on Peaceful.** | Supplementaries bombs |
| `barb` | A | Armor-piercing | Dustrial Decor barbed pane |
| `icicle` | A Fa | Armor-piercing, **Feather Falling helps** | Immersive Weathering |
| `fallingIcicle` | He | Helmet-cushioned | Immersive Weathering |
| `farmersdelight.stove`, `endersdelight.endstone_stove` | Fi | Fire, armor applies | Stoves |
| `arrow` (reinforced) | A Pr | Arrow that bypasses armor | Nock Enough Arrows mixin |
| `player` (pickarang) | Pr | Projectile | Quark pickarang |
| `spear` | - | Plain | Minecolonies spear |
| `slap` | - | Plain | Minecolonies mercenary |
| `entity.minecolonies.*` | - | Plain mob-named melee, never scaled | Minecolonies raiders (`NamedDamageSource`) |
| `despawn`, `Console`, `nether`, `Stuck-damage`, `Default` | - | Internal colony kills and pathing | Minecolonies |

### 4.5 Pack's own code (patcherMod and kubejs)
| Source | Flags | Notes |
|---|---|---|
| `explosion` from `BurnBlastDamage` | Ex (**no Df**) | Deliberately unscaled so Peaceful players still take ItemPhysic burn-blast damage |
| `DamageSource.DRAGON_BREATH` from the fan | A | The Create dragon-breathing fan, 4.0 damage |
| `DamageSource.thrown` from `ShrapnelEntity` | Pr | Projectile, so armor and Projectile Protection apply |
| `creativeengineering:magic_resistance` effect | n/a | Cuts damage 20% per amplifier on sources with `isMagic()` |

## 5. Findings worth acting on

1. **Armor-piercing is common.** About 23 of the non-vanilla sources have bypassArmor, and all of them also bypass
   shields. That includes Create's crush, saw and drill, IE electricity and the railgun, Pneumatic
   pressure, and Farseer. Any shield-tier or armor-tier work has to account for this
   (see `shield-mechanics-findings` in memory).
2. **Peaceful-mode zeroing.** Create fans, Create crushing wheels, Supplementaries bombs, Farseer and
   vanilla explosions all scale with difficulty, so they do **nothing to a player on Peaceful**. Only your
   `BurnBlastDamage` was patched.
3. **IE bullets are not flagged as projectiles.** Projectile Protection doesn't reduce revolver or railgun hits.
   Armor does, except for the armor-piercing and railgun rounds.
4. **Magic Resistance is narrow.** `isMagic()` covers only vanilla `magic`, `indirectMagic`, `thorns`, `sonic_boom`
   and Farseer. Poison, potions and the Guardian are covered, while acid, copper poisoning, Botania relic and
   health siphon are not.
5. **Feather Falling applies to icicles** (flagged as fall), even though the damage isn't a fall.
6. **Only `droneOverload` and vanilla void** bypass invulnerability.
7. **Vanilla `starve` is the only source that bypasses Resistance and protection enchantments together.**

## 6. Not covered

- Which vanilla source each mob or effect in every mod actually uses. Mods that only use vanilla constants
  don't appear here.
- Runtime changes by mixins or event handlers (`LivingHurtEvent`, `EntityEvents.hurt`), such as your own Magic
  Resistance listener and Quark or Botania hooks.
- The three helper-built sources named in section 1 (Collective, PuzzlesLib, Alex's Mobs multipart).

## 7. Damage tags (implemented 2026-09-20, comprehensive pass 2026-09-24)

Built in patcherMod, package `damage/`. Built and smoke-tested headlessly (93 checks); **not yet deployed to
`mods/` or tried in game** (the game was running when this pass finished).

**What it is.** 1.19.2 has no damage-type registry, so this adds one layer of tags on top of `DamageSource`.
A hit's tags are the union of:

- **Built-in flag tags**, computed live from the source's flags (`DamageFlag`): `armor_piercing`, `fire`, `magic`,
  `explosion`, `projectile`, `fall`, `difficulty_scaled`, `melee` (direct damager is living), `helmet_damage`,
  `bypasses_resistance`, `bypasses_enchantments`, `bypasses_invulnerability`. All in the `creativeengineering:` namespace.
- **Message-id tags** from `data/<ns>/damage_tags/<name>.json` (tag id `<ns>:<name>`). Vanilla tag syntax:
  `{"replace": false, "values": ["ieTesla", "ieRevolver_*", "#creativeengineering:cutting"]}`. `*` is a wildcard
  inside an id, `#` includes another tag (built-in or data-defined; cycles are fine). Files merge across datapacks,
  lowest priority first, and reload with `/reload`. A `"description"` key is ignored.
- A data file named like a built-in flag (`fire.json`) **adds** ids on top of the flag. It never removes a flagged source.

**Every flag now has a matching visible file (2026-09-24).** The first pass left `magic`/`fire`/`explosion`/
`projectile`/`fall`/`armor_piercing`/`melee` and the four rarer bypass/helmet flags as pure computed flags with no
file in the data folder, so a modded source's classification for e.g. "magic" was invisible/uneditable. All 12 now
have a comprehensive, hand-verified `damage_tags/<flag>.json` mirroring the findings-doc tables in section 3-4,
so every one of the ~127 known message ids is explicitly covered by at least one tag - not just reachable through
a live flag. Two message ids (`mob`, `player`) are deliberately left out of any explicit list because they're
reused by unrelated mechanics under the same id (vanilla melee vs. Quark's pickarang, vs. Create's Guardian
turret) - only the live per-instance flag can tell those apart correctly, so they rely on that alone.

**Meta tags.** Two pure aggregator tags exist purely to `#include` other tags, with no message ids of their own:
- `elemental` = `fire` ∪ `cold` ∪ `electric` ∪ `chemical`
- `physical` = `melee` ∪ `machinery` ∪ `piercing` ∪ `firearm` ∪ `explosion` ∪ `projectile`
- `magic` is a hybrid: the vanilla-flagged ids directly, plus `#include`s of `psychic` and `life_drain`.

Moving a whole kind of damage between shield columns (e.g. deciding acid should count as elemental) is now a
one-line edit to `elemental.json`'s `#include` list - no Java, confirmed live in the smoke test.

**All 30 shipped tags:** `armor_piercing`, `blunt`, `bypasses_enchantments`, `bypasses_invulnerability`,
`bypasses_resistance`, `chemical`, `cold`, `cutting`, `difficulty_scaled`, `electric`, `elemental`,
`environmental`, `explosion`, `fall`, `fire`, `firearm`, `helmet_damage`, `life_drain`, `machinery` (now a pure
meta of `blunt`+`cutting` - fixes a bug from the first pass where it referenced an undefined `cutting` tag),
`magic`, `melee`, `misc` (administrative/odd ids: Minecolonies internals, Alex's Mobs' `freddy`), `physical`,
`piercing`, `pneumatic`, `projectile`, `psychic`, `security`, `suffocation`, `void`.

**Shields consume tags only - confirmed by inspection (2026-09-24).** `ShieldBlocking` no longer calls
`source.isBypassArmor()`/`isBypassInvul()` directly; both are now `DamageTags.is(source, DamageFlag.ARMOR_PIERCING/
BYPASSES_INVULNERABILITY.tag())`. The one deliberate exception is the piercing-arrow check (`AbstractArrow`
pierce level), which is a structural vanilla mechanic, not a damage-type classification. `ShieldGroup`'s `FIRE`
constant was renamed `ELEMENTAL` (config key `fire` -> `elemental`) and now reads the `elemental` meta tag instead
of the narrow `fire` flag; `ShieldCategory` reads `DamageTags.id(...)` directly rather than `DamageFlag`, so its
five tagged categories (`EXPLOSION`/`PROJECTILE`/`MAGIC`/`ELEMENTAL`/`MELEE`, plus catch-all `OTHER`) work
identically whether a tag is flag-derived or purely data-defined.

**Real behaviour changes from this pass** (all intended, all covered by the smoke test): sources with **no**
vanilla flags at all - lightning, IE acid, IE tesla/wire/razor shock, cold/icicle damage, copper poisoning -
now shield as **ELEMENTAL** instead of falling to `OTHER`/physical, because they carry `electric`/`cold`/`chemical`
which the `elemental` meta tag pulls in. Botania's relic curse now shields as **MAGIC** (via `magic`'s include of
`psychic`) instead of `OTHER`. Vanilla shield-tuning defaults (100/0/0 -> 100/100/100 up the tier ladder) carried
over unchanged, just relabelled physical/elemental/magic.

**Discovery.** `[damage_tags] logSeenSources = true` in `creativeengineering-common.toml` logs each distinct
(message id, flags) once per session as `[damage-tags] seen msgId='...' flags=... tags=... attacker=... direct=... victim=...`.
`tags=NONE` marks a source no tag file claims yet (should be rare now - almost everything is covered).

**Not done yet.** The armor/resistance consumer (per-tag resistance attributes) and a KubeJS `global.damageTags`
helper that writes these files instead of hand-edited JSON.
