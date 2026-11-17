// Scratch-only probe (r1115 phase 11, SQ-PTH pass-through rows); never in the pack. Copy into a scratch server's
// kubejs/server_scripts as zz_zpth_probe.js. All entity lookups are by scoreboard tag (summon with Tags:["t"]) in the
// overworld, or a player/bot name where NAME is given. Output lines start with "[zpth] ".
//   /zpth mod TAG             one line per tagged entity: type, pass-through modifier (amount or none), pos, motion,
//                             health, NoGravity, onGround, removed
//   /zpth player NAME         the same for a player or perfBot bot
//   /zpth watch TAG TICKS EVERY   a line every EVERY ticks for TICKS ticks ("w TAG t=..."), "gone" once when the
//                             entity is removed, "watch done TAG" at the end
//   /zpth launch TAG OWNER VX VY VZ   owner (bot/player name or none), NoGravity off, motion set, for every tagged entity
//   /zpth motion TAG VX VY VZ  motion only
//   /zpth input NAME jump|sneak|none   sets a bot's jumping / shift flag (held until changed)
//   /zpth reel NAME           retrieves NAME's fishing hook with the held rod
//   /zpth near TYPE X Y Z R   every entity of TYPE (full id) within R: tags, pos, age-like fields
//   /zpth watchnew TYPE TAG TICKS EVERY X Y Z R   as watch, for entities of TYPE that appear in the box (tagged TAG)
//   /zpth fly NAME true|false  sets a creative player's flying flag (synced to the client)
//   /zpth count TAG           "count TAG n=.." (alive tagged entities)
//   /zpth states ID ...       block state properties of each block id
//   /zpth face NAME YAW PITCH  turns a bot; /zpth use NAME = right-click in air with the main hand item;
//   /zpth release NAME TICKS  releases a held use (bow, trident) as if charged TICKS ticks
//   /zpth attrs NAME|TAG      every movement-speed modifier of a player/bot (or of each tagged entity): uuid, name,
//                             amount, operation, then the attribute value (r1116 SQ-PS15B-11)
// MIGRATED 2026-11-15 in part (creative_devtools.md): input NAME sneak -> /pbot sneak, states -> /cdevtest state.
// Entity watches, jump input and the launch verbs stay here.
(() => {
    const $PStr = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $PAttr = Java.loadClass('net.minecraft.world.entity.ai.attributes.Attributes')
    const $PUUID = Java.loadClass('java.util.UUID')
    const $PVec = Java.loadClass('net.minecraft.world.phys.Vec3')
    const $PAABB = Java.loadClass('net.minecraft.world.phys.AABB')
    const $PLiving = Java.loadClass('net.minecraft.world.entity.LivingEntity')
    const $PEntity = Java.loadClass('net.minecraft.world.entity.Entity')
    const $PProj = Java.loadClass('net.minecraft.world.entity.projectile.Projectile')
    const $PET = Java.loadClass('net.minecraft.world.entity.EntityType')
    const $PForge = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $PRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $PHand = Java.loadClass('net.minecraft.world.InteractionHand')
    const MOD = $PUUID.fromString('a1386f64-4583-4798-9b29-f040c59b8574')
    function out(s) { console.info('[zpth] ' + s) }
    function f(v) { return (v * 1).toFixed(3) }
    function ow(server) { return server.overworld() }
    function tagged(server, tag) {
        var list = []
        ow(server).getAllEntities().forEach(e => { if (e != null && e.getTags().contains(tag)) list.push(e) })
        return list
    }
    function typeId(e) { return String($PET.getKey(e.getType())) }
    function desc(e) {
        var s = typeId(e)
        if (e instanceof $PLiving) {
            var a = e.getAttribute($PAttr.MOVEMENT_SPEED)
            var m = a == null ? null : a.getModifier(MOD)
            s += ' mod=' + (m == null ? 'none' : f(m.getAmount())) + ' hp=' + f(e.getHealth())
            s += ' speed=' + (a == null ? '?' : f(a.getValue()))
            s += ' climb=' + e.onClimbable()
        }
        var v = e.getDeltaMovement()
        s += ' pos=' + f(e.getX()) + ' ' + f(e.getY()) + ' ' + f(e.getZ()) + ' mot=' + f(v.x()) + ' ' + f(v.y()) + ' ' + f(v.z())
        s += ' ng=' + e.isNoGravity() + ' ground=' + e.isOnGround() + ' removed=' + e.isRemoved() + ' fall=' + f(e.fallDistance)
        return s
    }
    function player(server, name) { return server.getPlayerList().getPlayerByName(name) }
    var watches = []
    function run(server, args) {
        var a = args.trim().split(/\s+/)
        var c = a[0]
        if (c == 'mod') {
            var l = tagged(server, a[1])
            if (l.length == 0) out('mod ' + a[1] + ' none found')
            l.forEach(e => out('mod ' + a[1] + ' ' + desc(e)))
        } else if (c == 'player') {
            var p = player(server, a[1])
            out(p == null ? 'player ' + a[1] + ' missing' : 'player ' + a[1] + ' ' + desc(p) + ' shift=' + p.isShiftKeyDown())
        } else if (c == 'watch') {
            var ents = tagged(server, a[1])
            out('watch start ' + a[1] + ' n=' + ents.length)
            watches.push({ tag: a[1], ents: ents.map(e => ({ e: e, gone: false })), t: 0, end: a[2] * 1, every: (a[3] || 1) * 1 })
        } else if (c == 'launch' || c == 'motion') {
            var l2 = tagged(server, a[1])
            var off = c == 'launch' ? 1 : 0
            var owner = c == 'launch' && a[2] != 'none' ? player(server, a[2]) : null
            l2.forEach(e => {
                if (owner != null && e instanceof $PProj) e.setOwner(owner)
                e.setNoGravity(false)
                e.setDeltaMovement(new $PVec(a[2 + off] * 1, a[3 + off] * 1, a[4 + off] * 1))
                e.hurtMarked = true
            })
            out(c + ' ' + a[1] + ' n=' + l2.length + ' owner=' + (owner == null ? 'none' : a[2]))
        } else if (c == 'input') {
            var p2 = player(server, a[1])
            p2.setJumping(a[2] == 'jump')
            p2.setShiftKeyDown(a[2] == 'sneak')
            out('input ' + a[1] + ' ' + a[2])
        } else if (c == 'reel') {
            var p3 = player(server, a[1])
            if (p3.fishing == null) { out('reel ' + a[1] + ' no hook'); return }
            out('reel ' + a[1] + ' hook ' + desc(p3.fishing))
            var r = p3.fishing.retrieve(p3.getMainHandItem())
            out('reel ' + a[1] + ' retrieved=' + r)
        } else if (c == 'near') {
            var x = a[2] * 1, y = a[3] * 1, z = a[4] * 1, rr = a[5] * 1
            var n = 0
            ow(server).getEntitiesOfClass($PEntity, new $PAABB(x - rr, y - rr, z - rr, x + rr, y + rr, z + rr)).forEach(e => {
                if (typeId(e) != a[1]) return
                n++
                out('near ' + a[1] + ' tags=' + e.getTags() + ' ' + desc(e))
            })
            out('near ' + a[1] + ' total=' + n)
        } else if (c == 'states') {
            a.slice(1).forEach(id => {
                var bl = $PForge.BLOCKS.getValue(new $PRL(id))
                var ps = []
                bl.getStateDefinition().getProperties().forEach(p => ps.push(p.getName() + '=' + p.getPossibleValues()))
                out('states ' + id + ' -> ' + String(bl.builtInRegistryHolder().key().location()) + ' ' + ps.join(' '))
            })
        } else if (c == 'face') {
            // Rhino finds no setYRot on perfBot's BotPlayer (r1115): turn with a relative tp instead
            server.runCommandSilent('execute as ' + a[1] + ' at @s run tp @s ~ ~ ~ ' + a[2] + ' ' + a[3])
            out('face ' + a[1] + ' ' + a[2] + ' ' + a[3])
        } else if (c == 'use') {
            var p5 = player(server, a[1])
            var r5 = p5.gameMode.useItem(p5, p5.getLevel(), p5.getMainHandItem(), $PHand.MAIN_HAND)
            out('use ' + a[1] + ' -> ' + r5 + ' using=' + p5.isUsingItem())
        } else if (c == 'release') {
            var p6 = player(server, a[1])
            var st = p6.getMainHandItem()
            st.releaseUsing(p6.getLevel(), p6, st.getUseDuration() - a[2] * 1)
            p6.stopUsingItem()
            out('release ' + a[1] + ' charged ' + a[2])
        } else if (c == 'watchnew') {
            // watchnew TYPE TAG TICKS EVERY X Y Z R: every tick, entities of TYPE inside the box get TAG and join the watch
            watches.push({ tag: a[2], ents: [], t: 0, end: a[3] * 1, every: (a[4] || 1) * 1, type: a[1],
                box: new $PAABB(a[5] - a[8], a[6] - a[8], a[7] - a[8], a[5] * 1 + a[8] * 1, a[6] * 1 + a[8] * 1, a[7] * 1 + a[8] * 1) })
            out('watchnew start ' + a[2] + ' ' + a[1])
        } else if (c == 'fly') {
            var p7 = player(server, a[1])
            p7.getAbilities().flying = a[2] == 'true'
            p7.onUpdateAbilities()
            out('fly ' + a[1] + ' ' + p7.getAbilities().flying)
        } else if (c == 'attrs') {
            var who = player(server, a[1])
            var al = who != null ? [who] : tagged(server, a[1])
            if (al.length == 0) out('attrs ' + a[1] + ' none found')
            al.forEach(e => {
                var at = e.getAttribute($PAttr.MOVEMENT_SPEED)
                if (at == null) { out('attrs ' + a[1] + ' ' + typeId(e) + ' no attribute'); return }
                var n = 0
                at.getModifiers().forEach(m => { n++; out('attrs ' + a[1] + ' ' + typeId(e) + ' mod id=' + m.getId() + ' name="' + m.getName() + '" amount=' + f(m.getAmount()) + ' op=' + m.getOperation()) })
                out('attrs ' + a[1] + ' ' + typeId(e) + ' n=' + n + ' base=' + f(at.getBaseValue()) + ' value=' + f(at.getValue()) + ' pos=' + f(e.getX()) + ' ' + f(e.getY()) + ' ' + f(e.getZ()))
            })
        } else if (c == 'count') {
            out('count ' + a[1] + ' n=' + tagged(server, a[1]).filter(e => !e.isRemoved()).length)
        } else out('error unknown ' + c)
    }
    ServerEvents.tick(event => {
        for (let i = watches.length - 1; i >= 0; i--) {
            var w = watches[i]
            w.t++
            if (w.type) {
                ow(event.server).getEntitiesOfClass($PEntity, w.box).forEach(e => {
                    if (typeId(e) != w.type || e.getTags().contains(w.tag)) return
                    e.addTag(w.tag)
                    w.ents.push({ e: e, gone: false })
                    out('w ' + w.tag + ' t=' + w.t + ' new ' + desc(e))
                })
            }
            w.ents.forEach(o => {
                if (o.gone) return
                if (o.e.isRemoved()) { o.gone = true; out('w ' + w.tag + ' t=' + w.t + ' gone ' + typeId(o.e) + ' at ' + f(o.e.getX()) + ' ' + f(o.e.getY()) + ' ' + f(o.e.getZ())); return }
                if (w.t % w.every == 0) out('w ' + w.tag + ' t=' + w.t + ' ' + desc(o.e))
            })
            if (w.t >= w.end) { out('watch done ' + w.tag); watches.splice(i, 1) }
        }
    })
    ServerEvents.commandRegistry(e => {
        const { commands: C } = e
        e.register(C.literal('zpth').requires(s => s.hasPermission(2))
            .then(C.argument('args', $PStr.greedyString())
                .executes(ctx => {
                    try { run(ctx.source.server, String($PStr.getString(ctx, 'args'))) } catch (err) { out('error ' + err) }
                    return 1
                })))
    })
})()
