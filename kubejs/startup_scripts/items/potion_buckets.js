// Potion buckets — three tiers of a throwable "large, short-lived, expanding
// lingering-potion cloud" item.
//
//   normal    -> creativeengineering:potion_bucket
//   splash    -> creativeengineering:splash_potion_bucket       (bigger area, longer effects)
//   lingering -> creativeengineering:lingering_potion_bucket    (bigger still)
//
// Right-click behaviour + cloud spawning:
//   server_scripts/engineering/potion_buckets.js
//   startup_scripts/func/potion_area.js   (global.potionArea — all the tuning)
//
// Textures: kubejs/assets/creativeengineering/textures/item/<id>.png (body, untinted) +
//   <id>_liquid.png (liquid swirl, tinted) — two-layer item/generated model in
//   assets/creativeengineering/models/item/<id>.json. See tools/potion_bucket_texture.py.
//
// The buckets carry a potion the same way minecraft:potion does (NBT `Potion` /
// `CustomPotionEffects` / `CustomPotionColor`). A blank bucket falls back to
// global.potionArea.DEFAULT_EFFECTS. Survival crafting: server_scripts/engineering/
// potion_bottling.js's potion_bucket_fill/potion_bucket_drain shapeless recipes convert
// bucket <-> 4 matching potion bottles. Still also /give-able directly, e.g.
//   /give @s creativeengineering:lingering_potion_bucket{Potion:"minecraft:strong_regeneration"}

StartupEvents.registry('item', e => {
    const SimpleColor = Java.loadClass('dev.latvian.mods.rhino.mod.util.color.SimpleColor')
    const $PotionUtils = Java.loadClass('net.minecraft.world.item.alchemy.PotionUtils')

    // Same blend as global.potionArea's blendColor() (startup_scripts/func/potion_area.js) -
    // deliberately NOT PotionUtils.getColor(stack): this Rhino build (rhino-forge
    // 1902.2.3-build.284) throws "ambiguous method" on it (3 same-arity overloads:
    // Potion/ItemStack/Collection). getMobEffects() is a single unambiguous overload, and
    // MobEffect.getColor() is a single no-arg method, so blend from those instead. See
    // memory note rhino-fieldandmethods-gotcha.
    function potionTint(stack) {
        try {
            const tag = stack && stack.getOrCreateTag() // getTag() is invisible to Rhino here
            if (tag && tag.contains('CustomPotionColor')) return new SimpleColor(tag.getInt('CustomPotionColor'))
        } catch (err) {}

        const effects = stack ? $PotionUtils.getMobEffects(stack) : null
        if (!effects || effects.isEmpty()) return SimpleColor.WHITE
        let r = 0, g = 0, b = 0, n = 0
        effects.forEach(inst => {
            const c = inst.getEffect().getColor()
            r += (c >> 16) & 255
            g += (c >> 8) & 255
            b += c & 255
            n++
        })
        return new SimpleColor((Math.round(r / n) << 16) | (Math.round(g / n) << 8) | Math.round(b / n))
    }

    const mk = (id, name) => e.create('creativeengineering:' + id)
        .unstackable() // like vanilla buckets - each one carries its own potion contents
        .group('brewing')
        .displayName(name)
        // layer0 = bucket body (untinted, white = no-op), layer1 = liquid (potion colour)
        .color((stack, layer) => layer === 0 ? SimpleColor.WHITE : potionTint(stack))
        // crafting remainder for the potion_bucket_drain shapeless recipes
        // (server_scripts/engineering/potion_bottling.js) - same idiom as
        // startup_scripts/items/sample_syringe.js's own containerItem calls.
        .containerItem('minecraft:bucket')

    mk('potion_bucket',           'Potion Bucket')
    mk('splash_potion_bucket',    'Splash Potion Bucket')
    mk('lingering_potion_bucket', 'Lingering Potion Bucket')
})

// Crafting remainder for the potion_bucket_fill shapeless recipes (potion_bottling.js): each
// bottle consumed leaves a glass bottle behind, same mechanic as a vanilla water bucket leaving
// an empty bucket in the grid. Unlike the bucket's own .containerItem() above (an ItemBuilder
// method, registration-time only, OUR OWN item), these are VANILLA items, so this has to go
// through ItemEvents.modification instead of e.create(...) — and the modification event's
// runtime Item has NO .containerItem(): decompiling this build's jar shows ItemKJS/
// ModifiableItemKJS expose `kjs$setCraftingRemainder(Item)` instead (unprefixed per
// @RemapPrefixForJS('kjs$') — see [[tune-tools-func]] — so `.setCraftingRemainder(...)`, a
// DIFFERENT name AND it takes a real Item object, not a string id).
ItemEvents.modification(event => {
    var glassBottle = Item.of('minecraft:glass_bottle').getItem()
    ;['minecraft:potion', 'minecraft:splash_potion', 'minecraft:lingering_potion'].forEach(id => {
        try {
            event.modify(id, item => item.setCraftingRemainder(glassBottle))
        } catch (err) {
            console.warn('[potion_buckets] could not set glass_bottle crafting remainder on ' + id + ': ' + err)
        }
    })
})
