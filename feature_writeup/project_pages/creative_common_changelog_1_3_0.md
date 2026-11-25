# Creative Common 1.3.0 (CurseForge file changelog)

Changes since 1.2.0. Creative jars that need Creative Common now require 1.3.0 or later.

## Moved to other mods

- Low gravity for items, blocks and projectiles (Create: Gravity) moved to Creative Compat. The per-dimension limit on Create: Gravity's entity work stays in Creative Common.
- The honeycomb multiplier moved to Creative Delight. Creative Common no longer changes hive drops (default was x2).
- Wandering Trader settings moved to Creative Loot.
- Moved settings carry your edited values into the new mod's config file.
- The Creative Engineering pack's removed-item, merge and removed-wood lists are no longer in the jar. The helpers remain, with empty lists.
- The Diet Log, Liquid Diet Log and Food Dictionary dictionary keys moved to Spice of Life: Raspberry Pie.

## New

### Effects

- Wither Immunity: Wither cannot be applied, and a Wither already present deals no damage.
- Brain Freeze: cold foods eaten close together can freeze the player. A mod or pack has to fill the item tag for it to trigger.
- Level caps: effects that do the same at every level are held at level I from every source, tooltips included. 87 effects by default.
- Steady (Fruits Delight) also blocks slipping effects, and pressure plates, tripwires and big dripleaf ignore the holder.
- Leaf Piercing (Fruits Delight) arrows leave a leaf particle trail.
- The inventory shows permanent effects in a second column.

### Chat and privacy

- Chat reporting is off by default on the client: chat is not signed, and no reports or telemetry are sent. Turn it off to join servers that enforce secure chat.
- Dedicated servers accept players without a chat signing key by default.

### Settings and performance

- Pack switch to block all recipe unlocks (off by default).
- Items without a creative tab can be listed in a tab by id.
- Changes saved to the pack's own config files apply while the game runs.
- Blocks that can no longer change on a random tick stop asking for random ticks.

### Scripting

- Recipe helpers bound to another namespace (`NCH.forPack`, `reg.forPack`), sandpaper polishing, config-driven recipe values and a villager trade helper.
- Tags for the WTHIT overlay.

## Changed

- No effect is swapped or given a different category by default. Existing config files keep their values.
- 136 loading screen tips that are wrong or describe content newer than Minecraft 1.19.2 are filtered out; 396 show.
- Item, block and fluid dictionaries follow the mod that owns each id.
- Own text uses American spelling.

## Fixed

- Client crash at window creation with about 420 or more mods.
- Several recipe helpers did not mark an edited recipe as changed.
- Mods whose data namespace differs from their mod id were treated as not installed by the dictionaries and tag scripts.

## Requirements

Minecraft 1.19.2, Forge 43 or newer, KubeJS 1902.6.2-build.73 or newer (1902.6.x). Both client and server need it.
