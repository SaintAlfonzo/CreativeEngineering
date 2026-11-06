// Moved from the pack's server_scripts/engineering/potion_buckets.js 2026-11-01 (creative_brewing.md 4.5), unchanged.
// Potion buckets — right-click a block to burst a large, short-lived, EXPANDING
// stack of lingering-potion rings (a dome, not a flat disc). See:
//   CB startup creativeengineering/potion_buckets.js   (item registration)
//   CB startup creativeengineering/potion_area.js       (global.potionArea — ring stack + per-bucket dials)
//   CB client creativeengineering/potion_buckets.js          (effect tooltip)

// NOTE: read the id list from global.potionArea.BUCKETS rather than declaring our own local
// const array here - KubeJS server_scripts all share ONE top-level scope (not per-file), so a
// same-named top-level const/var/let in two different server_scripts files collides and
// throws "redeclaration of const X" for whichever loads second, silently killing that WHOLE
// file (this once broke bucket placement pack-wide when a since-removed script duplicated
// this same id list under the same name - see memory rhino-fieldandmethods-gotcha).
Object.keys(global.potionArea.BUCKETS).forEach(id => {
    ItemEvents.rightClicked(id, e => {
        const player = e.player
        // NOTE: call getLevel()/x()/y()/z() as METHODS, never the bare `.level`/`.x`/`.y`/`.z`
        // properties. Entity has both a public `level` field and a `getLevel()` method, and
        // Vec3 has both public `x`/`y`/`z` fields and matching `x()`/`y()`/`z()` methods -
        // Rhino's combined field+getter wrapper for those clashes with its constructor-overload
        // coercion (new AreaEffectCloud(...) in potionArea.spawn takes a double x/y/z), throwing
        // a silent NPE that killed this whole handler on every single click.
        const level = player.getLevel()
        if (level.isClientSide()) return
        if (String(e.hand) !== 'MAIN_HAND') return

        // only place against a block, like a real bucket - never in mid-air
        const t = e.target
        if (!t || String(t.type) !== 'BLOCK') return

        const stack = e.item

        // NOTE: not using t.facing.getStepX()/Y()/Z() to offset off the clicked face - this
        // Rhino build can't find those methods on a Direction enum instance at all
        // ("Cannot find function getStepX in object up"), a third distinct Rhino/vanilla-API
        // landmine on top of the FieldAndMethods and ambiguous-overload ones (see memory
        // rhino-fieldandmethods-gotcha). A small fixed vertical nudge is enough to avoid
        // spawning embedded in the clicked block without touching Direction at all.
        const x = t.hit.x()
        const y = t.hit.y() + 0.1
        const z = t.hit.z()

        // a no-effect potion (awkward, thick, mundane, water) spawns no rings: keep the bucket
        const rings = global.potionArea.spawn(level, x, y, z, id, stack, player)
        if (!rings || rings.length === 0) return

        // take the item before shrink(1): the unstackable bucket empties and an empty stack is air
        const bucketItem = stack.getItem()
        if (!player.isCreative()) {
            stack.shrink(1)
            const empty = Item.of('minecraft:bucket')
            if (!player.getInventory().add(empty)) player.drop(empty, false)
        }
        player.getCooldowns().addCooldown(bucketItem, 20)

        return e.success()
    })
})
