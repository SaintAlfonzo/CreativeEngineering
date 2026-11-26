<!-- SPDX-License-Identifier: GPL-3.0-only -->
# Harmony from the Allay: design options

Row `food-tuning #58` (Raspberry Pie, Creative Common). Ruling (owner, 2026-11-22): Harmony's new source is the vanilla Allay. Needs a loading tooltip and a JEI effect description. Wild allays must not give Harmony (captured allays would stop all mob spawns); a tamed allay, or one with regular access to a material, provides it, with a white sparkle when it applies the effect. Investigation only; nothing built. Vanilla facts below are from memory of the 1.19.2 `Allay` / `AllayAi` classes and are NOT yet verified against the decompiled jar (first step of any build).

## Current state in the pack

| Item | State |
|---|---|
| Harmony `neapolitan:harmony` | Neapolitan effect, key `ef.harmony` in `_lib/effect_dict.js`; sources today are adzuki foods only (food_catalog.md, can_types.js); `effects.md` line: most hostile mobs within 12 blocks run from the holder. Listed in the CommonConfig effect list (level-capped, max 1) |
| Emptiness `botania:emptiness` | Botania effect, no natural mob spawns within 128 blocks of the holder; brewery only, also level-capped 1 |
| Allay hooks | None in `development/` or `kubejs/` code. Only 4 loading tips mention allays (items, jukebox dance, amethyst duplicate, outpost cages). `spawns.json`, `pool_config.json`: spawn and cage data only |
| Audit | `harmony_source_audit.csv` ranks Allay first ("cap by per-allay cooldown or wild-only flag") |
| Effect frameworks | `effect/` in Creative Common: `EffectRules` (remap/removed/forceHudIcon), `LevelCappedEffect`, `CompoundEffects`, `GearEffectMarks`. Periodic apply pattern already exists for armor (`armor_effects.js applyEffects`) and mounts; 6-arg `MobEffectInstance(..., false, false, true)` keeps the HUD icon without particles (effect-refresh note) |

The row text says "applies Emptiness and hostile mob flee". Harmony alone makes hostiles flee; it does not stop spawns. See open question 1.

## Vanilla Allay mechanics (1.19.2)

| Topic | Behavior |
|---|---|
| Item pickup | Allay has a 1-slot inventory plus the main hand. Player right-clicks with an item while the allay hand is empty: allay takes 1 item, remembers its type, and then picks up dropped items of that type within about 32 blocks horizontally and 16 vertically. Right-click with an empty hand while it holds an item: it gives the item back |
| Liked player | Giving an item sets brain memory `LIKED_PLAYER` (UUID, has a codec, so it persists in the entity NBT; no expiry timer in vanilla). Allay follows that player, and delivers collected items to them when within about 4 blocks (it hovers, drops the stack). If the liked player is offline or dead the memory stays but the delivery goal idles |
| Note block | Liked player hits a note block near the allay: memory `LIKED_NOTEBLOCK_POSITION` set, 600-tick cooldown memory. Allay then delivers items to that note block (drops them above it) instead of the player while the position is valid |
| Duplication | Needs the allay to be dancing (jukebox playing within about 3 blocks) and the item to be an amethyst shard, and `DUPLICATION_COOLDOWN_TICKS` (6000 ticks, 5 minutes) at 0. Result is a new allay carrying the same state minus cooldown. Shard is consumed, no cap in vanilla |
| Persistence | Whether a tamed allay is exempt from despawn is unverified (check `removeWhenFarAway` / `isPersistenceRequired` in the jar); a tamed one that despawns would silently lose the effect |
| Flower/petal | The audit's "flower leaves a petal" is not vanilla; it was a proposal for a new item. Vanilla flowers do nothing special except as a pickup item type |

## How the pack can mark "tamed" and "regular access to a material"

| Signal | How to read it | Notes |
|---|---|---|
| Tamed | `LIKED_PLAYER` memory present (`allay.getBrain().getMemory(MemoryModuleType.LIKED_PLAYER)`, possibly via Mojmap `Allay#getLikedPlayer`) | Free, vanilla-persistent, set by the vanilla give-an-item action, so no mixin or extra save data. Wild and cage allays never have it. Can be also copied to a persistent tag (`getPersistentData`, Forge) the first time it is seen so a later vanilla change cannot lose it |
| Regular access to a material | Allay main hand is non-empty (it was given a material and is gathering it) | A stricter reading: also require `LIKED_PLAYER` or note block position. A held amethyst shard or any item counts as "material"; optional whitelist tag |
| Owner | Liked player UUID | Allay can only buff the liked player, or anyone within range |

## Applying the effect: robust timer design

Use a Forge `LivingEvent.LivingTickEvent` handler (server side, `entity instanceof Allay`), gated by `tickCount % 40 == 0` (2 seconds). Per pass: check the signal above, then `level.getEntitiesOfClass(Player, allay.getBoundingBox().inflate(R))` (R 12 matches Harmony's 12 block radius, owner to confirm), add `Harmony` for 100 ticks with the 6-arg constructor (HUD icon on, no vanilla particles) so it lapses about 5 seconds after leaving, and send `ServerLevel#sendParticles` with `ParticleTypes.END_ROD` (white) at the allay and at the player. Do not touch any amplifier above 1 (the effect is level-capped). A tick event keeps all logic in one place; no mixin into the brain is needed. Cost: one cheap check per allay every 2 seconds; players list only when the signal holds. Refreshing a longer duration on re-add is fine for Harmony (no attribute modifiers), unlike Health Boost.

## Options

| Option | Rule | Pros | Cons |
|---|---|---|---|
| A. Liked-player allay | Any allay with `LIKED_PLAYER` buffs players within R | Smallest code, uses vanilla persistence, matches "tamed" literally | Duplicating tamed allays (amethyst, dance) gives overlap but no stacking (Harmony is level-capped 1); 5-minute duplicate cooldown already limits growth |
| B. Liked player AND holding a material | A plus non-empty hand (or a pack tag of accepted materials) | Matches "tamed, or regular access to a material" and can distinguish an idle tamed pet; hand is empty after the give-back action so the player can switch it off | Slightly more state to explain in the tooltip |
| C. Liked player only buffs that player; note-block allay buffs everyone nearby | Owner-only vs area modes | Fits "regular access" (note-block delivery means the allay is reliably stationed) | More rules, more tooltip text, more to test; note-block position validity must be checked each pass |

## Recommendation

Option B, owner-only is not required: buff all players within 12 blocks when the allay has a liked player and a non-empty hand. Wild and cage allays never qualify (no liked-player memory), so the mob-spawn worry is gone unless the owner also wants Emptiness (see below). Put the code in Creative Common (`allay/AllayHarmony`, new package, CommonConfig `[allay]` keys: enabled, radius, intervalTicks, effectTicks, requireHeldItem, particle). Loading tip text and a JEED provider entry in `kubejs/server_scripts/other/jeed_providers.js` (custom text via lang key, since JEED has no entity-source slot other than spawn eggs; use the allay spawn egg as the provider input) cover the two documentation requirements.

## Open questions for the owner

1. Does the allay also apply Emptiness (no natural spawns within 128 blocks), as the row text says, or only Harmony? Emptiness from a mobile, duplicable ally is the spawn-suppression risk the owner named, so recommended: Harmony only.
2. Who is buffed: all players within the radius, or only the liked player? And radius (12 to match Harmony, or larger)?
3. Is "tamed" enough on its own (option A), or must the allay also hold a material (option B)?
4. Should the amethyst duplication stay uncapped, or capped per player (for example at 4 buffing allays)? Because Harmony does not stack, a cap is only needed if Emptiness is included.
5. Which white particle: end rod (white glint, default here), or cloud/wax-off?
6. JEI text: does the owner want the Harmony description changed globally to mention the allay, or an allay-specific entry?

## Next steps if approved

Verify the vanilla facts above against the decompiled 1.19.2 jar, then build, add queue rows (prefix per `test_prefix_index.md`), a changelog bullet under the Harmony or Raspberry Pie family, and update `effects.md` (Harmony source column) and the TODO row via `tools/todo.py`.
