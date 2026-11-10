// Scratch-only probe for the Hearth and Harvest crow, salt lick, scarecrow and nest rows (queue SQ-HH-401 upward).
// Copy into a scratch server's kubejs/server_scripts as zz_hh_crow_probe.js, never into the pack. Command: /zhh3 (permission 2).
// Output: "[zhh3] ..." lines in logs/latest.log; every call ends with "[zhh3] done" (watch also prints "watch TAG done" later).
//   cfgdump                     every [salt] and [crow] value as the game reads it
//   cfg KEY [true|false|D]      read a key, or live-set a boolean / chance key of [crow] or [salt] (ints: toml before boot)
//   spawn TYPE N X Y Z [SPREAD] [noai]   summon N of TYPE around X Y Z (random within SPREAD horizontally)
//   log TYPE X Y Z R [REF]      one line per entity within R of X Y Z, with wariness state and goals (crows: wariness; others: restriction)
//   watch TYPE X Y Z R N EVERY TAG [REF]   sample like log N times, EVERY ticks apart; REF = @BOTNAME or x,y,z for the distance column
//   assert TAG STAT OP V        compare a watch statistic: "[zhh3] assert TAG STAT=v OP V PASS|FAIL"
//   seen TAG GOAL               did goal class GOAL run in any sample of TAG: "[zhh3] seen TAG GOAL yes|no"
//   goals TYPE X Y Z R          the goal list of each entity with priorities (available goals, running ones marked)
//   tally X Y Z R               "tally crows=N tamed=T carrying=C" for the crows in the box
//   target X Y Z R ITEM        AM crow canTargetItem(ITEM) for each crow (mixin proof): "[zhh3] canTarget ITEM true|false"
//   block X Y Z                 block id and properties
//   poi X Y Z                   POI type at the position, or none
//   items X Y Z R [clear]       item entities (id, count, Age, Invulnerable, Thrower present, PickupDelay), then "items total=N id xN ..."
//   inv NAME [ITEM]             hotbar, main, offhand of a player; with ITEM also "invcount NAME ITEM n"
//   throw NAME X Y Z ITEM N [SPREAD]   N item entities (1 item each) with player NAME as thrower, no pickup delay
//   hit NAME X Y Z R [DMG]      player NAME hits the nearest crow to X Y Z (DamageSource.playerAttack, default 1)
//   feed NAME X Y Z R           player NAME right-clicks every wild crow in the box once (Player.interactOn, main hand)
//   interact NAME X Y Z R       player NAME right-clicks every crow in the box (wild or tame), main hand
//   tame NAME X Y Z R           tames every crow in the box to player NAME (TamableAnimal.tame)
//   sneak NAME on|off / sprint NAME on|off   set the flag directly; bots have no input
//   loot X Y Z R ITEM [TICKS]   CrowLogic.holdLoot on every crow in the box (TICKS overrides the carry timer)
//   leash NAME X Y Z R TYPE     leash every TYPE in the box to player NAME
//   enttag TAG                  entity types in an entity type tag
//   nests X Z R                 ground nests on the surface in a loaded square: generated / placed counts, first 5 with biome
//   kill X Y Z TYPE N LOOT      a fake player with an iron sword of Looting LOOT kills N fresh TYPE at X Y Z; sums the drops
// TYPE defaults to alexsmobs:crow when written "crow". No chunk loads here: forceload first (Rhino deadlock, scratch-test gotchas).
(() => {
    const $HH3Args = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $HH3Reg = Java.loadClass('net.minecraft.core.Registry')
    const $HH3FR = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $HH3RL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $HH3Pos = Java.loadClass('net.minecraft.core.BlockPos')
    const $HH3AABB = Java.loadClass('net.minecraft.world.phys.AABB')
    const $HH3Entity = Java.loadClass('net.minecraft.world.entity.Entity')
    const $HH3Item = Java.loadClass('net.minecraft.world.entity.item.ItemEntity')
    const $HH3CT = Java.loadClass('net.minecraft.nbt.CompoundTag')
    const $HH3Hand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $HH3DS = Java.loadClass('net.minecraft.world.damagesource.DamageSource')
    const $HH3TagKey = Java.loadClass('net.minecraft.tags.TagKey')
    const $HH3HT = Java.loadClass('net.minecraft.world.level.levelgen.Heightmap$Types')
    const $HH3Fake = Java.loadClass('net.minecraftforge.common.util.FakePlayerFactory')
    const $HH3Profile = Java.loadClass('com.mojang.authlib.GameProfile')
    const $HH3UUID = Java.loadClass('java.util.UUID')
    const $HH3List = Java.loadClass('java.util.ArrayList')
    const $HH3Collectors = Java.loadClass('java.util.stream.Collectors')
    // Member classes load on first use, so a boot without the member still registers /zhh3
    const HH3PKG = 'com.SaintAlfonzo.creativedelight.hearth.'
    let $HH3Wary = null, $HH3Logic = null, $HH3Access = null, $HH3Crow = null, $HH3Salt = null
    function crowCls() {
        if ($HH3Wary === null) {
            $HH3Wary = Java.loadClass(HH3PKG + 'crow.CrowWariness')
            $HH3Logic = Java.loadClass(HH3PKG + 'crow.CrowLogic')
            $HH3Access = Java.loadClass(HH3PKG + 'crow.CrowAccess')
            $HH3Crow = Java.loadClass(HH3PKG + 'crow.CrowConfig')
            $HH3Salt = Java.loadClass(HH3PKG + 'salt.SaltConfig')
        }
    }
    function out(s) { console.info('[zhh3] ' + s) }
    function typeId(t) { return t == 'crow' ? 'alexsmobs:crow' : t }
    function lvl(server) { return server.overworld() }
    function box(x, y, z, r) { return new $HH3AABB(x - r, y - r, z - r, x + r, y + r, z + r) }
    // getEncodeId, not getType(): KubeJS shadows Entity.getType with a String; players encode to null
    function entId(e) { let t = e.getEncodeId(); return t == null ? 'player' : String(t) }
    function ofType(level, type, x, y, z, r) {
        let want = typeId(type), o = []
        level.getEntitiesOfClass($HH3Entity, box(x, y, z, r)).forEach(e => { if (entId(e) == want) o.push(e) })
        return o
    }
    function player(server, name) { return server.getPlayerList().getPlayerByName(name) }
    function itemId(st) { return String($HH3Reg.ITEM.getKey(st.getItem())) }
    function stackStr(st) { return st.isEmpty() ? 'empty' : itemId(st) + ' x' + st.getCount() }
    function dist(a, x, y, z) { return Math.sqrt(a.distanceToSqr(x, y, z)) }
    // REF: @NAME (a player) or x,y,z. Returns [x, y, z] or null.
    function refPoint(server, ref) {
        if (!ref) return null
        if (ref.charAt(0) == '@') {
            let p = player(server, ref.substring(1))
            return p == null ? null : [p.getX(), p.getY(), p.getZ()]
        }
        let c = ref.split(',')
        return [parseFloat(c[0]), parseFloat(c[1]), parseFloat(c[2])]
    }
    function goalsOf(m) {
        let o = []
        m.goalSelector.getRunningGoals().collect($HH3Collectors.toList()).forEach(w => o.push(String(w.getGoal())))
        return o
    }
    // One entity's state: a JS object, plus a printable line
    function stateOf(server, e, ref) {
        let id = entId(e), s = { id: id, line: id, goals: goalsOf(e), trust: 0, alarm: 0, carry: false, carryT: 0, main: 'empty', d: -1 }
        if (ref) s.d = dist(e, ref[0], ref[1], ref[2])
        s.main = stackStr(e.getMainHandItem())
        let pos = e.getX().toFixed(1) + ',' + e.getY().toFixed(1) + ',' + e.getZ().toFixed(1)
        s.line = id + ' pos=' + pos + ' d=' + s.d.toFixed(2) + ' hp=' + e.getHealth().toFixed(1)
        if (id == 'alexsmobs:crow') {
            crowCls()
            let w = $HH3Wary.of(e)
            let tag = w == null ? new $HH3CT() : w.serializeNBT()
            s.trust = tag.getInt('Trust')
            s.alarm = tag.getInt('AlarmTicks')
            s.carry = tag.getBoolean('Carrying')
            s.carryT = tag.getInt('CarryTicks')
            s.tame = e.isTame()
            s.snatch = w != null && w.isSnatching()
            s.fly = $HH3Access.isFlying(e)
            let tgt = e.getTarget()
            s.tgt = tgt == null ? 'none' : 'set'
            s.line += ' tame=' + s.tame + ' fly=' + s.fly + ' sit=' + $HH3Access.isSitting(e) + ' trust=' + s.trust + ' alarm=' + s.alarm
                + ' carry=' + s.carry + '/' + s.carryT + ' snatch=' + s.snatch + ' main=' + s.main + ' target=' + s.tgt + ' cap=' + (w != null)
        } else {
            s.restr = e.hasRestriction()
            s.line += ' restricted=' + s.restr + ' leashed=' + e.isLeashed()
        }
        s.line += ' goals=[' + s.goals.join(',') + ']'
        return s
    }
    // Registered watches: TAG -> statistics; running samplers in hh3Active
    let hh3Stats = {}, hh3Active = []
    function newStat() { return { samples: 0, count: 0, minD: 1e9, maxD: -1, lastD: -1, minTrust: 1e9, maxTrust: -1, lastTrust: -1, minAlarm: 1e9, maxAlarm: -1, lastAlarm: -1, everCarry: 0, lastCarry: -1, flew: 0, tame: 0, goals: {}, mains: {} } }
    function sample(server, w) {
        let level = lvl(server), list = ofType(level, w.type, w.x, w.y, w.z, w.r), ref = refPoint(server, w.ref)
        let st = w.stat, near = null
        st.samples++
        st.count = Math.max(st.count, list.length)
        list.forEach(e => {
            let s = stateOf(server, e, ref)
            if (ref) { st.minD = Math.min(st.minD, s.d); st.maxD = Math.max(st.maxD, s.d) }
            st.minTrust = Math.min(st.minTrust, s.trust); st.maxTrust = Math.max(st.maxTrust, s.trust)
            st.minAlarm = Math.min(st.minAlarm, s.alarm); st.maxAlarm = Math.max(st.maxAlarm, s.alarm)
            if (s.carry) st.everCarry = 1
            if (s.fly) st.flew = 1
            if (s.tame) st.tame = 1
            s.goals.forEach(g => { st.goals[g] = 1 })
            st.mains[s.main] = 1
            if (near === null || s.d < near.d) near = s
            if (w.logEvery) out('watch ' + w.tag + ' t=' + st.samples + ' ' + s.line)
        })
        if (near !== null) { st.lastD = near.d; st.lastTrust = near.trust; st.lastAlarm = near.alarm; st.lastCarry = near.carry ? 1 : 0 }
    }
    function finish(w) {
        let st = w.stat
        out('watch ' + w.tag + ' done samples=' + st.samples + ' count=' + st.count + ' minD=' + st.minD.toFixed(2) + ' maxD=' + st.maxD.toFixed(2) + ' lastD=' + st.lastD.toFixed(2)
            + ' minTrust=' + st.minTrust + ' maxTrust=' + st.maxTrust + ' lastTrust=' + st.lastTrust + ' minAlarm=' + st.minAlarm + ' maxAlarm=' + st.maxAlarm + ' lastAlarm=' + st.lastAlarm
            + ' everCarry=' + st.everCarry + ' lastCarry=' + st.lastCarry + ' goals=' + Object.keys(st.goals).join(',') + ' mains=' + Object.keys(st.mains).join(','))
    }
    function compare(v, op, n) {
        if (op == '>=') return v >= n
        if (op == '<=') return v <= n
        if (op == '>') return v > n
        if (op == '<') return v < n
        if (op == '==') return v == n
        if (op == '!=') return v != n
        return false
    }
    const HH3Keys = {
        fleeing: 'c:FLEEING', flockAlarm: 'c:FLOCK_ALARM', tempting: 'c:TEMPTING', handFeeding: 'c:HAND_FEEDING', eatDroppedFood: 'c:EAT_DROPPED_FOOD',
        cornBreeding: 'c:CORN_BREEDING', eatCrops: 'c:EAT_CROPS', stealShinyItems: 'c:STEAL_SHINY_ITEMS', stealChance: 'c:STEAL_CHANCE', stashInNests: 'c:STASH_IN_NESTS',
        dropOnHurt: 'c:DROP_ON_HURT', carryTicks: 'c:CARRY_TICKS', scareRadius: 'c:SCARE_RADIUS', generateNests: 'c:GENERATE_NESTS', spawnNearNests: 'c:SPAWN_NEAR_NESTS',
        spawnRadius: 'c:SPAWN_RADIUS', cropRequirement: 'c:CROP_REQUIREMENT', denySpawnsElsewhere: 'c:DENY_SPAWNS_ELSEWHERE',
        playerLicking: 's:PLAYER_LICKING', animalLicking: 's:ANIMAL_LICKING', saltPlayerLickChance: 's:PLAYER_LICK_CHANCE', saltAnimalStageChance: 's:ANIMAL_STAGE_CHANCE',
        saltAnimalRadius: 's:ANIMAL_RADIUS', saltSearchInterval: 's:SEARCH_INTERVAL', saltLickInterval: 's:LICK_INTERVAL'
    }
    const HH3Int = { carryTicks: 1, scareRadius: 1, spawnRadius: 1, cropRequirement: 1, saltAnimalRadius: 1, saltSearchInterval: 1, saltLickInterval: 1 }
    function cfgVal(key) {
        crowCls()
        let m = HH3Keys[key]
        if (!m) throw 'unknown key ' + key
        let cls = m.charAt(0) == 'c' ? $HH3Crow : $HH3Salt
        return cls[m.substring(2)]
    }
    function run(server, what, argStr) {
        let a = argStr ? String(argStr).trim().split(/\s+/) : []
        try {
            let level = lvl(server)
            if (what == 'cfgdump') {
                Object.keys(HH3Keys).forEach(k => out('cfg ' + (HH3Keys[k].charAt(0) == 'c' ? 'crow.' : 'salt.') + k + ' = ' + cfgVal(k).get()))
            } else if (what == 'cfg') {
                let cv = cfgVal(a[0])
                if (a[1] !== undefined) {
                    if (HH3Int[a[0]]) throw 'int key: set it in the toml before the boot'
                    if (a[1] == 'true' || a[1] == 'false') cv.set(a[1] == 'true'); else cv.set(parseFloat(a[1]))
                }
                out('cfg ' + a[0] + ' = ' + cv.get())
            } else if (what == 'spawn') {
                let n = parseInt(a[1]), x = parseFloat(a[2]), y = parseFloat(a[3]), z = parseFloat(a[4]), spread = a[5] ? parseFloat(a[5]) : 0, noai = a[6] == 'noai' || a[5] == 'noai'
                if (a[5] == 'noai') spread = 0
                let et = $HH3FR.ENTITY_TYPES.getValue(new $HH3RL(typeId(a[0])))
                for (let i = 0; i < n; i++) {
                    let m = et.create(level)
                    m.setPos(x + (Math.random() - 0.5) * 2 * spread, y, z + (Math.random() - 0.5) * 2 * spread)
                    if (noai) m.setNoAi(true)
                    m.setPersistenceRequired()
                    level.addFreshEntity(m)
                }
                out('spawned ' + n + ' ' + a[0])
            } else if (what == 'log') {
                let list = ofType(level, a[0], parseFloat(a[1]), parseFloat(a[2]), parseFloat(a[3]), parseFloat(a[4])), ref = refPoint(server, a[5])
                list.forEach(e => out('log ' + stateOf(server, e, ref).line))
                out('log count=' + list.length)
            } else if (what == 'watch') {
                let w = { type: a[0], x: parseFloat(a[1]), y: parseFloat(a[2]), z: parseFloat(a[3]), r: parseFloat(a[4]), n: parseInt(a[5]), every: parseInt(a[6]), tag: a[7], ref: a[8], left: parseInt(a[5]), wait: 0, stat: newStat() }
                hh3Stats[w.tag] = w.stat
                w.logEvery = true
                hh3Active.push(w)
                out('watch ' + w.tag + ' started, ' + w.n + ' samples every ' + w.every + ' ticks')
            } else if (what == 'tally') {
                let list = ofType(level, 'crow', parseFloat(a[0]), parseFloat(a[1]), parseFloat(a[2]), parseFloat(a[3])), tamed = 0, carrying = 0
                crowCls()
                list.forEach(e => { if (e.isTame()) tamed++; let w = $HH3Wary.of(e); if (w != null && w.isCarrying()) carrying++ })
                out('tally crows=' + list.length + ' tamed=' + tamed + ' carrying=' + carrying)
            } else if (what == 'assert') {
                let st = hh3Stats[a[0]]
                if (!st) { out('assert ' + a[0] + ' no such watch FAIL'); }
                else {
                    let v = st[a[1]], ok = compare(v, a[2], parseFloat(a[3]))
                    out('assert ' + a[0] + ' ' + a[1] + '=' + v + ' ' + a[2] + ' ' + a[3] + ' ' + (ok ? 'PASS' : 'FAIL'))
                }
            } else if (what == 'seen') {
                let st = hh3Stats[a[0]]
                out('seen ' + a[0] + ' ' + a[1] + ' ' + (st && st.goals[a[1]] ? 'yes' : 'no'))
            } else if (what == 'goals') {
                ofType(level, a[0], parseFloat(a[1]), parseFloat(a[2]), parseFloat(a[3]), parseFloat(a[4])).forEach(e => {
                    let o = []
                    new $HH3List(e.goalSelector.getAvailableGoals()).forEach(w => o.push(w.getPriority() + ':' + String(w.getGoal()) + (w.isRunning() ? '*' : '')))
                    out('goals ' + entId(e) + ' ' + o.join(' '))
                })
            } else if (what == 'target') {
                let it = Item.of(a[4]);
                ofType(level, 'crow', parseFloat(a[0]), parseFloat(a[1]), parseFloat(a[2]), parseFloat(a[3])).forEach(e => out('canTarget ' + a[4] + ' ' + e.canTargetItem(it) + ' wild=' + !e.isTame()))
            } else if (what == 'block') {
                let p = new $HH3Pos(parseInt(a[0]), parseInt(a[1]), parseInt(a[2])), st = level.getBlockState(p), props = []
                st.getProperties().forEach(pr => props.push(String(pr.getName()) + '=' + String(st.getValue(pr))))
                out('block ' + a[0] + ' ' + a[1] + ' ' + a[2] + ' ' + String($HH3Reg.BLOCK.getKey(st.getBlock())) + (props.length ? ' [' + props.join(',') + ']' : ''))
            } else if (what == 'poi') {
                let p = new $HH3Pos(parseInt(a[0]), parseInt(a[1]), parseInt(a[2])), opt = level.getPoiManager().getType(p)
                out('poi ' + a[0] + ' ' + a[1] + ' ' + a[2] + ' ' + (opt.isPresent() ? String(opt.get().unwrapKey().get().location()) : 'none'))
            } else if (what == 'items') {
                let x = parseFloat(a[0]), y = parseFloat(a[1]), z = parseFloat(a[2]), r = parseFloat(a[3]), counts = {}, total = 0
                level.getEntitiesOfClass($HH3Item, box(x, y, z, r)).forEach(e => {
                    let st = e.getItem(), id = itemId(st), t = e.saveWithoutId(new $HH3CT())
                    out('item ' + id + ' x' + st.getCount() + ' Age=' + t.getShort('Age') + ' Invulnerable=' + t.getBoolean('Invulnerable') + ' Thrower=' + t.hasUUID('Thrower') + ' PickupDelay=' + t.getShort('PickupDelay'))
                    counts[id] = (counts[id] || 0) + st.getCount(); total += st.getCount()
                    if (a[4] == 'clear') e.discard()
                })
                out('items total=' + total + ' ' + Object.keys(counts).sort().map(k => k + ' x' + counts[k]).join(', '))
            } else if (what == 'inv') {
                let p = player(server, a[0])
                if (p == null) { out('no player ' + a[0]) } else {
                    let inv = p.getInventory(), hot = [], main = [], want = a[1], cnt = 0
                    for (let i = 0; i < 36; i++) {
                        let st = inv.getItem(i)
                        if (st.isEmpty()) continue
                        ;(i < 9 ? hot : main).push(i + ':' + stackStr(st))
                        if (want && itemId(st) == want) cnt += st.getCount()
                    }
                    let off = p.getOffhandItem()
                    if (want && !off.isEmpty() && itemId(off) == want) cnt += off.getCount()
                    out('inv ' + a[0] + ' hotbar=[' + hot.join('; ') + '] main=[' + main.join('; ') + '] off=' + stackStr(off))
                    if (want) out('invcount ' + a[0] + ' ' + want + ' ' + cnt)
                }
            } else if (what == 'throw') {
                let p = player(server, a[0]), x = parseFloat(a[1]), y = parseFloat(a[2]), z = parseFloat(a[3]), n = parseInt(a[5]), spread = a[6] ? parseFloat(a[6]) : 0
                let uuid = p.getGameProfile().getId()
                for (let i = 0; i < n; i++) {
                    let e = new $HH3Item(level, x + (Math.random() - 0.5) * 2 * spread, y, z + (Math.random() - 0.5) * 2 * spread, Item.of(a[4], 1))
                    e.setThrower(uuid)
                    e.setNoPickUpDelay()
                    level.addFreshEntity(e)
                }
                out('threw ' + n + ' ' + a[4])
            } else if (what == 'hit') {
                let p = player(server, a[0]), x = parseFloat(a[1]), y = parseFloat(a[2]), z = parseFloat(a[3]), best = null, bd = 1e9
                ofType(level, 'crow', x, y, z, parseFloat(a[4])).forEach(e => { let d = dist(e, x, y, z); if (d < bd) { bd = d; best = e } })
                if (best === null) { out('hit: no crow') } else { out('hit result ' + best.attack($HH3DS.playerAttack(p), a[5] ? parseFloat(a[5]) : 1)) }
            } else if (what == 'feed') {
                let p = player(server, a[0]), x = parseFloat(a[1]), y = parseFloat(a[2]), z = parseFloat(a[3]), tries = 0, tamed = 0
                ofType(level, 'crow', x, y, z, parseFloat(a[4])).forEach(e => {
                    if (e.isTame()) return
                    tries++
                    let r = p.interactOn(e, $HH3Hand.MAIN_HAND)
                    if (e.isTame()) tamed++
                })
                out('feed attempts=' + tries + ' tamed=' + tamed + ' held=' + stackStr(p.getMainHandItem()))
            } else if (what == 'interact') {
                let p = player(server, a[0]), n = 0
                ofType(level, 'crow', parseFloat(a[1]), parseFloat(a[2]), parseFloat(a[3]), parseFloat(a[4])).forEach(e => { p.interactOn(e, $HH3Hand.MAIN_HAND); n++ })
                out('interact ' + n + ' held=' + stackStr(p.getMainHandItem()))
            } else if (what == 'tame') {
                let p = player(server, a[0]), n = 0
                ofType(level, 'crow', parseFloat(a[1]), parseFloat(a[2]), parseFloat(a[3]), parseFloat(a[4])).forEach(e => { e.tame(p); n++ })
                out('tamed ' + n)
            } else if (what == 'sneak' || what == 'sprint') {
                let p = player(server, a[0]), on = a[1] == 'on'
                if (what == 'sneak') p.setShiftKeyDown(on); else p.setSprinting(on)
                out(what + ' ' + a[0] + ' ' + on)
            } else if (what == 'loot') {
                crowCls()
                let n = 0
                ofType(level, 'crow', parseFloat(a[0]), parseFloat(a[1]), parseFloat(a[2]), parseFloat(a[3])).forEach(e => {
                    let w = $HH3Wary.of(e)
                    $HH3Logic.holdLoot(e, w, Item.of(a[4], 1))
                    if (a[5]) w.startCarrying(parseInt(a[5]))
                    n++
                })
                out('loot on ' + n + ' crows')
            } else if (what == 'leash') {
                let p = player(server, a[0]), n = 0
                ofType(level, a[5], parseFloat(a[1]), parseFloat(a[2]), parseFloat(a[3]), parseFloat(a[4])).forEach(e => { e.setLeashedTo(p, true); n++ })
                out('leashed ' + n)
            } else if (what == 'enttag') {
                let key = $HH3TagKey.create($HH3Reg.ENTITY_TYPE_REGISTRY, new $HH3RL(a[0])), ids = []
                $HH3Reg.ENTITY_TYPE.getTagOrEmpty(key).forEach(h => ids.push(String($HH3Reg.ENTITY_TYPE.getKey(h.value()))))
                out('enttag ' + a[0] + ' (' + ids.length + '): ' + ids.join(' '))
            } else if (what == 'nests') {
                let x0 = parseInt(a[0]), z0 = parseInt(a[1]), r = parseInt(a[2]), gen = 0, placed = 0, shown = 0
                for (let x = x0 - r; x <= x0 + r; x++) for (let z = z0 - r; z <= z0 + r; z++) {
                    if (!level.hasChunk(x >> 4, z >> 4)) continue
                    let h = level.getHeight($HH3HT.MOTION_BLOCKING, x, z)
                    for (let y = h - 2; y <= h + 1; y++) {
                        let st = level.getBlockState(new $HH3Pos(x, y, z))
                        if (String($HH3Reg.BLOCK.getKey(st.getBlock())) != 'hearthandharvest:nest') continue
                        let isGen = false
                        st.getProperties().forEach(pr => { if (String(pr.getName()) == 'generated') isGen = String(st.getValue(pr)) == 'true' })
                        if (isGen) gen++; else placed++
                        if (shown++ < 5) out('nest at ' + x + ' ' + y + ' ' + z + ' generated=' + isGen + ' biome=' + String(level.getBiome(new $HH3Pos(x, y, z)).unwrapKey().get().location()))
                    }
                }
                out('nests generated=' + gen + ' placed=' + placed)
            } else if (what == 'kill') {
                let x = parseFloat(a[0]), y = parseFloat(a[1]), z = parseFloat(a[2]), n = parseInt(a[4]), loot = parseInt(a[5])
                let fake = $HH3Fake.get(level, new $HH3Profile($HH3UUID.randomUUID(), 'zhh3kill'))
                fake.setItemInHand($HH3Hand.MAIN_HAND, loot > 0 ? Item.of('minecraft:iron_sword', '{Enchantments:[{id:"minecraft:looting",lvl:' + loot + 's}]}') : Item.of('minecraft:iron_sword'))
                let et = $HH3FR.ENTITY_TYPES.getValue(new $HH3RL(typeId(a[3]))), b = box(x, y, z, 4), counts = {}
                level.getEntitiesOfClass($HH3Item, b).forEach(e => e.discard())
                for (let i = 0; i < n; i++) {
                    let m = et.create(level)
                    m.setPos(x + 0.5, y, z + 0.5)
                    level.addFreshEntity(m)
                    m.attack($HH3DS.playerAttack(fake), 1000)
                }
                level.getEntitiesOfClass($HH3Item, b).forEach(e => { let id = itemId(e.getItem()); counts[id] = (counts[id] || 0) + e.getItem().getCount() })
                out('kill ' + n + ' ' + a[3] + ' looting ' + loot + ': ' + (Object.keys(counts).sort().map(k => k + ' x' + counts[k]).join(', ') || 'none'))
            } else out('unknown query ' + what)
        } catch (err) { out('error: ' + err) }
        out('done')
    }
    // One sample per watch and tick range
    ServerEvents.tick(e => {
        try {
            if (hh3Active.length == 0) return
            let server = e.server
            hh3Active = hh3Active.filter(w => {
                if (w.wait-- > 0) return true
                sample(server, w)
                w.left--
                w.wait = w.every - 1
                if (w.left <= 0) { finish(w); return false }
                return true
            })
        } catch (err) { console.info('[zhh3] tick error: ' + err); hh3Active = [] }
    })
    ServerEvents.commandRegistry(e => {
        const { commands: C } = e
        e.register(C.literal('zhh3').requires(s => s.hasPermission(2))
            .then(C.argument('what', $HH3Args.word())
                .executes(ctx => { run(ctx.source.server, $HH3Args.getString(ctx, 'what'), ''); return 1 })
                .then(C.argument('arg', $HH3Args.greedyString())
                    .executes(ctx => {
                        run(ctx.source.server, $HH3Args.getString(ctx, 'what'), $HH3Args.getString(ctx, 'arg'))
                        return 1
                    }))))
    })
})()
