// Scratch-only probe (never in the pack): copy into a scratch server's kubejs/server_scripts as zz_cge_probe.js.
// SQ-CGE rows (Creative Common CreateGravityEntitiesMixin): tick-exact fall distances of non-living entities.
//   /zgv drop <dim> <x> <y> <z>   summons a falling sand block, a stone item and lit TNT (Fuse 200) 3 blocks apart at
//                                 that spot, tagged zgv, and prints each one's drop after 20 and 40 server ticks:
//                                 "[zgv] t=40 dim=... type=... y0=... y=... drop=... onGround=..."; then discards them.
//   /zgv dropall <dim> <x> <y> <z>   as drop, plus an XP orb, an arrow, a snowball, a boat, a minecart and llama spit
//                                 (3 blocks apart along z): every type Common's LowGravity* mixins scale.
//   /zgv watch <dim> <x> <y> <z> <ticks>   the same three, but prints every 20 ticks until <ticks> and keeps them
//                                 (SQ-CGE-04 resting entities); "[zgv] watch done" at the end.
//   /zgv cfg <dim> <x> <y> <z>    Create Gravity's own lookup for the biome there: "[zgv] cfg dim=... biome=... config=null"
//                                 or "factor=..."; null means the mod's biome list is not built (or the biome is unlisted).
//   /zgv reload                   calls Create Gravity's ServerConfig.reloadConfig() (what a TagsUpdatedEvent does).
// <dim> is a vanilla path without namespace (overworld, the_end, the_nether) or a quoted full id.
// Output lines start with "[zgv] ", errors "[zgv] error ...".
(() => {
    const $ZGVInt = Java.loadClass('com.mojang.brigadier.arguments.IntegerArgumentType')
    const $ZGVStr = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $ZGVPos = Java.loadClass('net.minecraft.core.BlockPos')
    let $ZGVCfg = null
    try { $ZGVCfg = Java.loadClass('de.cadentem.create_gravity.config.ServerConfig') } catch (err) { }
    function out(s) { console.info('[zgv] ' + s) }
    var runs = []
    function levelOf(server, dim) {
        if (dim.indexOf(':') < 0) dim = 'minecraft:' + dim
        var found = null
        server.getAllLevels().forEach(l => { if (String(l.getDimension()) == dim) found = l })
        return found
    }
    function tagged(level, tag) {
        var list = []
        level.getAllEntities().forEach(e => { if (e != null && e.getTags().contains(tag)) list.push(e) })
        return list
    }
    function summon(server, dim, x, y, z, tag) {
        var nbt = ',Tags:["zgv","' + tag + '"]}'
        server.runCommandSilent('execute in ' + dim + ' run forceload add ' + x + ' ' + z)
        server.runCommandSilent('execute in ' + dim + ' run summon minecraft:falling_block ' + x + ' ' + y + ' ' + z + ' {BlockState:{Name:"minecraft:sand"},Time:1' + nbt)
        server.runCommandSilent('execute in ' + dim + ' run summon minecraft:item ' + (x + 3) + ' ' + y + ' ' + z + ' {Item:{id:"minecraft:stone",Count:1b},Age:0,PickupDelay:32767' + nbt)
        server.runCommandSilent('execute in ' + dim + ' run summon minecraft:tnt ' + (x + 6) + ' ' + y + ' ' + z + ' {Fuse:200' + nbt)
        if (!allTypes) return
        // r1108 part 2: every type with a fixed pull (Common LowGravity* mixins), 3 blocks apart along z
        var more = [
            'minecraft:experience_orb {Value:1,Age:0', 'minecraft:arrow {pickup:0b,life:0s', 'minecraft:snowball {',
            'minecraft:boat {', 'minecraft:minecart {', 'minecraft:llama_spit {',
        ]
        more.forEach((m, k) => {
            var sep = m.indexOf(' ')
            var body = m.substring(sep + 1)
            server.runCommandSilent('execute in ' + dim + ' run summon ' + m.substring(0, sep) + ' ' + x + ' ' + y + ' ' + (z + 3 * (k + 1)) + ' ' +
                body + (body.length > 1 ? ',' : '') + 'Tags:["zgv","' + tag + '"]}')
        })
    }
    var allTypes = false
    function start(ctx, watchTicks) {
        try {
            var server = ctx.source.server
            var dim = $ZGVStr.getString(ctx, 'dim')
            if (dim.indexOf(':') < 0) dim = 'minecraft:' + dim
            var x = $ZGVInt.getInteger(ctx, 'x'), y = $ZGVInt.getInteger(ctx, 'y'), z = $ZGVInt.getInteger(ctx, 'z')
            var level = levelOf(server, dim)
            if (level == null) { out('error no level ' + dim); return 0 }
            var tag = 'zgv' + runs.length + '_' + Date.now() % 100000
            summon(server, dim, x, y, z, tag)
            var ents = tagged(level, tag).map(e => ({ e: e, type: String(e.getType()), y0: e.getY() }))
            out('start dim=' + dim + ' entities=' + ents.length + ' y=' + y)
            runs.push({ level: level, dim: dim, ents: ents, t: 0, end: watchTicks > 0 ? watchTicks : 40, every: watchTicks > 0 ? 20 : 20, keep: watchTicks > 0 })
        } catch (err) { out('error ' + err) }
        return 1
    }
    ServerEvents.tick(event => {
        for (let i = runs.length - 1; i >= 0; i--) {
            var r = runs[i]
            r.t++
            if (r.t % r.every == 0 || r.t == r.end) {
                r.ents.forEach(o => {
                    var e = o.e
                    out('t=' + r.t + ' dim=' + r.dim + ' type=' + o.type + ' y0=' + o.y0.toFixed(3) + ' y=' + e.getY().toFixed(3) +
                        ' drop=' + (o.y0 - e.getY()).toFixed(3) + ' onGround=' + e.isOnGround() + ' removed=' + e.isRemoved() +
                        ' vy=' + e.getDeltaMovement().y().toFixed(4))
                })
            }
            if (r.t >= r.end) {
                if (!r.keep) r.ents.forEach(o => { if (!o.e.isRemoved()) o.e.discard() })
                out((r.keep ? 'watch' : 'drop') + ' done dim=' + r.dim)
                runs.splice(i, 1)
            }
        }
    })
    function cfg(ctx) {
        try {
            var server = ctx.source.server
            var dim = $ZGVStr.getString(ctx, 'dim')
            if (dim.indexOf(':') < 0) dim = 'minecraft:' + dim
            var level = levelOf(server, dim)
            if (level == null) { out('error no level ' + dim); return 0 }
            var pos = new $ZGVPos($ZGVInt.getInteger(ctx, 'x'), $ZGVInt.getInteger(ctx, 'y'), $ZGVInt.getInteger(ctx, 'z'))
            var holder = level.getBiome(pos)
            var biome = holder.unwrapKey().map(k => String(k.location())).orElse('?')
            if ($ZGVCfg == null) { out('cfg dim=' + dim + ' biome=' + biome + ' create_gravity absent'); return 1 }
            var c = $ZGVCfg.getBiomeConfig(holder)
            out('cfg dim=' + dim + ' biome=' + biome + (c == null ? ' config=null' : ' factor=' + c.gravityFactor() + ' oxygen=' + c.oxygenFactor()))
        } catch (err) { out('error ' + err) }
        return 1
    }
    ServerEvents.commandRegistry(e => {
        var { commands: C } = e
        var posArgs = (lit, fn) => C.literal(lit).then(C.argument('dim', $ZGVStr.string())
            .then(C.argument('x', $ZGVInt.integer()).then(C.argument('y', $ZGVInt.integer()).then(C.argument('z', $ZGVInt.integer())
                .executes(fn)))))
        e.register(C.literal('zgv')
            .then(posArgs('drop', ctx => { allTypes = false; return start(ctx, 0) }))
            .then(posArgs('dropall', ctx => { allTypes = true; return start(ctx, 0) }))
            .then(posArgs('cfg', ctx => cfg(ctx)))
            .then(C.literal('watch').then(C.argument('dim', $ZGVStr.string())
                .then(C.argument('x', $ZGVInt.integer()).then(C.argument('y', $ZGVInt.integer()).then(C.argument('z', $ZGVInt.integer())
                    .then(C.argument('ticks', $ZGVInt.integer(20, 2000)).executes(ctx => start(ctx, $ZGVInt.getInteger(ctx, 'ticks')))))))))
            .then(C.literal('reload').executes(ctx => {
                try { if ($ZGVCfg == null) out('reload: create_gravity absent'); else { $ZGVCfg.reloadConfig(); out('reload done') } } catch (err) { out('error ' + err) }
                return 1
            })))
    })
})()
