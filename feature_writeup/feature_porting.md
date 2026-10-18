
Which mods can be implemented myself

Tier 1: recipe or loot only (an afternoon in KubeJS)

These add little or nothing but recipes and loot, so you can replace them with global.NCH recipes or LootJS:

Nametag Crafting: one shaped recipe.
Just Another Rotten Flesh to Leather (jrftl): one smelting or campfire recipe.
Furnace Recycle: smelting recipes that turn tools and armour back into nuggets.
Create Waystone Recipes, AE2 QoL Recipes, Create Recycle Everything, Create: Copper Splashing, Create: Netherless: these only add recipes (crushing, washing and so on). Run /kubejs export first so you capture the exact recipe list before you remove them.
Book Fishing: one LootJS modifier that adds enchanted books to the fishing loot table.
Starter Kit: a PlayerEvents.loggedIn handler plus a persistent-data flag.
Death Knell: a death event that broadcasts the death coordinates.
Glowing Illagers: apply Glowing to raid mobs when they spawn.
Witch Mobility: one attribute change.
Tier 2: small server-side behaviour (one event handler or mixin each in patcherMod)

Each of these is a single event handler or mixin, with no new content and no client code:

Ice Prevents Crop Growth: a CropGrowEvent.Pre check. It fits straight into your crops/CropGrowth.
Comfortable Campfires: give Regeneration to players within 3 blocks of a lit campfire. Your config is just distance, duration and comfort.
Rain should extinguish campfires and Unlit Campfire: a random-tick or placement check on campfire block states.
Golems Are Friends Not Fodder, Breedable Killer Rabbit: small entity events.
Friendly Griefing: your config only allows villagers to grief, so this is a mob-griefing check on one entity type.
Cherished Items: change ItemEntity lifespan when an item is renamed, enchanted or tagged. Your config has five simple rules.
Tax Free Levels: an enchanting/anvil cost formula. You only use levelBase: 0 and removeAnvilLimit.
Loading Protection: make players invulnerable on join until they move.
Effishiency: your only setting is durability scaling with fish caught.
Don’t void Arrows, Fast Leaf Decay, SwingThroughGrass, Ice Boat Nerf: each is one mixin or event.
Tier 3: mods you’re already patching (absorbing them deletes a dependency and its mixin)
Forgiving Void: ForgivingVoidMixin already redirects its only interesting call. The whole mod is about 20 lines of player-tick logic, so you could own all of it.
Responsive Shields: you run it at Raise Time 0, and you already have a full shield/ system. Removing the 5-tick blocking delay in ShieldBlocking replaces it.
Bushier Flowers: BushierFlowersDisableMixin already turns part of it off, so check what you actually still use.
Easy Elytra Takeoff: you have two mixins into it already, and its core feature is small.
Second Chance: coyote time plus “survive at ½ heart if you were at 7+ hearts”. Two features, both simple.
Tier 4: doable, but they add content (only if you keep the ids)

XP Tome, Bottled Air, Golden Foods, Simple Netherite Horse Armor, Charm of Undying (Curios is already in the pack), Ender Crop, Survivality (about 10 separate small tweaks; easy one by one, but tedious). Some KubeJS item registration plus a little logic would do it, but every one of these has existing world data.