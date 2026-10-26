// Creative Delight food batch and End's / Ender's probe (scratch_queue.md SQ-CDB-04, -06, -07; SQ-EN-02, -03, -04,
// -06; ends_delight.md EN-01, EN-03, EN-04, EN-07, EN-09). Scratch-only: copy into a scratch server's
// kubejs/server_scripts as zz_cdb_en_probe.js, never into the pack.
//
//   /cbetest <part>   part = blocks | soils | growth | dragon | anvil | squid | jars | all
//
// Needs the chunk at X, Z loaded: `forceload add 400 400` from the console about 30 s before (the cdj_probe spot).
// Results are "[cbetest] PASS|FAIL|INFO <part> <name>: <detail>" lines in logs/latest.log, then "[cbetest] done".
// Kills and clicks go through a Forge fake player (cdj_probe / food_drinks_probe patterns). Not covered (client):
// JEI, tooltips, art, mining speed.
(() => {
    const $CEFake = Java.loadClass('net.minecraftforge.common.util.FakePlayerFactory')
    const $CEProfile = Java.loadClass('com.mojang.authlib.GameProfile')
    const $CEUUID = Java.loadClass('java.util.UUID')
    const $CEDir = Java.loadClass('net.minecraft.core.Direction')
    const $CEBlockPos = Java.loadClass('net.minecraft.core.BlockPos')
    const $CEVec3 = Java.loadClass('net.minecraft.world.phys.Vec3')
    const $CEHit = Java.loadClass('net.minecraft.world.phys.BlockHitResult')
    const $CEHand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $CEReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $CERL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $CEBlock = Java.loadClass('net.minecraft.world.level.block.Block')
    const $CEDamage = Java.loadClass('net.minecraft.world.damagesource.DamageSource')
    const $CEItemEntity = Java.loadClass('net.minecraft.world.entity.item.ItemEntity')
    const $CEAABB = Java.loadClass('net.minecraft.world.phys.AABB')

    const X = 400, Y = -60, Z = 400
    let pass = 0, fail = 0, part = '', fakeN = 0, nextX = X - 20
    function check(name, ok, detail) {
        if (ok) pass++; else fail++
        console.info('[cbetest] ' + (ok ? 'PASS' : 'FAIL') + ' ' + part + ' ' + name + ': ' + detail)
    }
    function info(name, detail) { console.info('[cbetest] INFO ' + part + ' ' + name + ': ' + detail) }
    function rl(id) { return new $CERL(id) }
    function item(id) { return $CEReg.ITEMS.getValue(rl(id)) }
    function block(id) { return $CEReg.BLOCKS.getValue(rl(id)) }
    function itemId(st) { return String($CEReg.ITEMS.getKey(st.getItem())) }
    function fake(level, held) {
        fakeN++
        let p = $CEFake.get(level, new $CEProfile($CEUUID.randomUUID(), 'cbeprobe' + fakeN))
        p.getInventory().clearContent()
        if (held) p.setItemInHand($CEHand.MAIN_HAND, Item.of(held, 1))
        return p
    }
    function click(p, level, at, st) {
        p.setItemInHand($CEHand.MAIN_HAND, st)
        let hit = new $CEHit(new $CEVec3(at.getX() + 0.5, at.getY() + 1, at.getZ() + 0.5), $CEDir.UP, at, false)
        return String(p.gameMode.useItemOn(p, level, p.getMainHandItem(), $CEHand.MAIN_HAND, hit))
    }
    function count(p, id) { return p.getInventory().countItem(item(id)) }
    // a fresh spot: air over the column first, so nothing from an earlier run is left
    function spot(level) {
        let at = new $CEBlockPos(nextX, Y, Z + 6)
        nextX += 2
        for (let dy = -1; dy <= 2; dy++) level.setBlockAndUpdate(at.above(dy), block('minecraft:air').defaultBlockState())
        return at
    }
    // item entities within r blocks of the probe spot: id -> count
    function drops(level, r) {
        let out = {}
        level.getEntitiesOfClass($CEItemEntity, new $CEAABB(X - r, Y - 4, Z - r, X + r + 1, Y + r, Z + r + 1)).forEach(e => {
            let st = e.getItem()
            out[itemId(st)] = (out[itemId(st)] || 0) + st.getCount()
        })
        return out
    }
    function clearDrops(level, r) {
        level.getEntitiesOfClass($CEItemEntity, new $CEAABB(X - r, Y - 4, Z - r, X + r + 1, Y + r, Z + r + 1)).forEach(e => e.discard())
    }
    function fluidName(tank) {
        let f = tank.getFluid()
        try { return String(f.getRegistryName()) } catch (e) { }
        try { return String(f.getTranslationKey()) } catch (e) { }
        return String(f)
    }
    function show(o) { return Object.keys(o).sort().map(k => k + ' x' + o[k]).join(', ') || 'none' }

    // EN-01: crate and stove drop themselves (with the right tool and by hand)
    function blocks(level) {
        ;[['endersdelight:chorus_crate', 'minecraft:diamond_axe'], ['endersdelight:endstone_stove', 'minecraft:diamond_pickaxe']].forEach(x => {
            ;[x[1], ''].forEach(tool => {
                let at = spot(level)
                let st = block(x[0]).defaultBlockState()
                level.setBlockAndUpdate(at, st)
                let got = []
                $CEBlock.getDrops(level.getBlockState(at), level, at, level.getBlockEntity(at), fake(level, tool),
                    tool ? Item.of(tool, 1) : Item.empty).forEach(s => got.push(itemId(s) + ' x' + s.getCount()))
                check(x[0] + ' drops itself (' + (tool || 'hand') + ')', got.length == 1 && got[0] == x[0] + ' x1', got.join(', ') || 'none')
            })
        })
    }

    const PLANTS = ['endersdelight:amberveil_mushroom', 'endersdelight:chorus_flame_block', 'endersdelight:ethereal_saffron_bush',
        'endersdelight:voidpepper_block']
    // EN-03: each plant survives only on end stone and tilled end stone
    function soils(level) {
        PLANTS.forEach(id => {
            let res = {}
            ;['minecraft:end_stone', 'endercrop:tilled_end_stone', 'minecraft:dirt', 'minecraft:grass_block', 'minecraft:farmland'].forEach(soil => {
                let at = spot(level)
                level.setBlockAndUpdate(at.below(), block(soil).defaultBlockState())
                res[soil] = block(id).defaultBlockState().canSurvive(level, at)
            })
            let ok = res['minecraft:end_stone'] && res['endercrop:tilled_end_stone'] && !res['minecraft:dirt'] &&
                !res['minecraft:grass_block'] && !res['minecraft:farmland']
            check(id + ' soils', ok, Object.keys(res).map(k => k.split(':')[1] + '=' + res[k]).join(', '))
        })
    }

    // EN-04: chorusflame and void pepper grow faster on tilled end stone than on end stone. One random tick per trial
    // (more saturates: chorusflame reaches max age either way): chorusflame counts trials that grew a stage (config 0.1
    // slow / 0.2 fast), void pepper trials that changed the sprout (spread or fruit; 0.5 / 1.0). A sprout needs
    // SPREADING and its DOWN face, which getStateForPlacement sets; defaultBlockState has neither.
    function growth(level) {
        const N = 100
        ;['endersdelight:chorus_flame_block', 'endersdelight:voidpepper_block'].forEach(id => {
            let hits = {}
            ;['minecraft:end_stone', 'endercrop:tilled_end_stone'].forEach(soil => {
                let n = 0
                for (let trial = 0; trial < N; trial++) {
                    let at = spot(level)
                    level.setBlockAndUpdate(at.below(), block(soil).defaultBlockState())
                    let st = block(id).defaultBlockState()
                    st.getProperties().forEach(pr => {
                        let nm = String(pr.getName())
                        if ((nm == 'spreading' || nm == 'down') && String(st.getValue(pr)) == 'false') st = st.cycle(pr)   // setValue(prop, boolean) is ambiguous in Rhino
                    })
                    level.setBlock(at, st, 2)
                    let before = level.getBlockState(at)
                    before.randomTick(level, at, level.random)
                    let after = level.getBlockState(at)
                    if (String(after) != String(before)) n++
                    else if (id.indexOf('voidpepper') > 0) {
                        // a spread leaves the origin unchanged: look for a new sprout nearby
                        let found = false
                        for (let dx = -7; dx <= 7 && !found; dx++) for (let dz = -7; dz <= 7 && !found; dz++) for (let dy = -2; dy <= 2 && !found; dy++) {
                            if (dx == 0 && dz == 0 && dy == 0) continue
                            let q = at.offset(dx, dy, dz)
                            if (level.getBlockState(q).getBlock() == block(id)) { found = true; level.setBlock(q, block('minecraft:air').defaultBlockState(), 2) }
                        }
                        if (found) n++
                    }
                    level.setBlock(at, block('minecraft:air').defaultBlockState(), 2)
                }
                hits[soil.split(':')[1]] = n
            })
            check(id + ' faster on tilled (' + N + ' single ticks)', hits.tilled_end_stone > hits.end_stone,
                'end_stone ' + hits.end_stone + ', tilled ' + hits.tilled_end_stone)
        })
    }

    // EN-07: dragon drops, bare hand vs knife vs machete
    function dragon(level) {
        let R = 60
        ;[['', 'bare'], ['farmersdelight:iron_knife', 'knife'], ['nethersdelight:iron_machete', 'machete']].forEach(x => {
            let sums = {}, per = []
            for (let k = 0; k < 4; k++) {
                clearDrops(level, R)
                let m = $CEReg.ENTITY_TYPES.getValue(rl('minecraft:ender_dragon')).create(level)
                m.setPos(X + 0.5, Y + 3, Z + 0.5)
                level.addFreshEntity(m)
                let pl = fake(level, x[0])
                let head = m.getSubEntities()[0]
                head.attack($CEDamage.playerAttack(pl), 1000)
                let got = drops(level, R)
                per.push('alive=' + m.isAlive() + ' ' + show(got))
                Object.keys(got).forEach(k => { sums[k] = (sums[k] || 0) + got[k] })
                m.discard()
            }
            info('dragon ' + x[1] + ' (4 kills)', per.join(' | '))
            let meat = sums['ends_delight:raw_dragon_meat'] || 0, legs = sums['ends_delight:dragon_leg'] || 0,
                teeth = sums['ends_delight:dragon_tooth'] || 0
            if (x[1] == 'bare') check('dragon bare: 5-7 meat, 2 legs, 1-2 teeth per kill', meat >= 20 && meat <= 28 && legs == 8 && teeth >= 4 && teeth <= 8,
                'meat ' + meat + ', legs ' + legs + ', teeth ' + teeth + ' in 4')
            else check('dragon ' + x[1] + ': +2 meat, +1 tooth per kill', meat >= 28 && meat <= 36 && legs == 8 && teeth >= 8 && teeth <= 12,
                'meat ' + meat + ', legs ' + legs + ', teeth ' + teeth + ' in 4')
        })
        clearDrops(level, R)
    }

    // EN-09: Dragon Tooth Knife repairs with a Dragon Tooth
    function anvil() {
        let k = Item.of('ends_delight:dragon_tooth_knife', 1)
        check('dragon tooth knife repairs with a dragon tooth', k.getItem().isValidRepairItem(k, Item.of('ends_delight:dragon_tooth', 1)), '')
    }

    // CDB-04: squid and glow squid drop their own item, no ink sac / calamari / tentacles
    function squid(level) {
        ;[['minecraft:squid', 'miners_delight:squid'], ['minecraft:glow_squid', 'miners_delight:glow_squid']].forEach(x => {
            ;['minecraft:iron_sword', 'farmersdelight:iron_knife'].forEach(held => {
                let sums = {}
                for (let k = 0; k < 5; k++) {
                    clearDrops(level, 4)
                    let m = $CEReg.ENTITY_TYPES.getValue(rl(x[0])).create(level)
                    m.setPos(X + 0.5, Y, Z + 0.5)
                    level.addFreshEntity(m)
                    m.attack($CEDamage.playerAttack(fake(level, held)), 1000)
                    let got = drops(level, 4)
                    Object.keys(got).forEach(i => { sums[i] = (sums[i] || 0) + got[i] })
                }
                let others = Object.keys(sums).filter(i => i != x[1] && i != 'delightful:animal_fat')   // Delightful's knife fat: upstream
                check(x[0] + ' with ' + held + ': 1 ' + x[1] + ' per kill, no ink / calamari / tentacles', (sums[x[1]] || 0) == 5 && others.length == 0, show(sums) + ' in 5')
            })
        })
        clearDrops(level, 4)
    }

    // CDB-06 / -07: lava bottle, milkshake and Splash Milk's bottle go into a jar (and lava back out)
    function jars(level) {
        function jar() { let at = spot(level); level.setBlockAndUpdate(at, block('supplementaries:jar').defaultBlockState()); return [at, level.getBlockEntity(at).getSoftFluidTank()] }
        let [a, ta] = jar()
        let p = fake(level)
        click(p, level, a, Item.of('alexsmobs:lava_bottle'))
        let inLava = ta.getCount()
        check('lava bottle goes in', inLava > 0, 'count ' + inLava + ', fluid ' + fluidName(ta))
        p.getInventory().clearContent()
        click(p, level, a, Item.of('minecraft:glass_bottle'))
        check('lava comes back out into a bottle', count(p, 'alexsmobs:lava_bottle') == 1 && ta.getCount() < inLava,
            'lava bottles ' + count(p, 'alexsmobs:lava_bottle') + ', left ' + ta.getCount())
        ;['neapolitan:chocolate_milkshake', 'neapolitan:vanilla_milkshake', 'splash_milk:milk_bottle'].forEach(id => {
            let [b, tb] = jar()
            click(fake(level), level, b, Item.of(id))
            check(id + ' goes in', tb.getCount() > 0, 'count ' + tb.getCount() + ', fluid ' + fluidName(tb))
        })
    }

    function run(src, which) {
        pass = 0; fail = 0; fakeN = 0; nextX = X - 20
        let level = src.getLevel()
        let parts = which == 'all' ? ['blocks', 'soils', 'growth', 'anvil', 'squid', 'jars', 'dragon'] : [which]
        parts.forEach(w => {
            part = w
            try {
                if (w == 'blocks') blocks(level)
                else if (w == 'soils') soils(level)
                else if (w == 'growth') growth(level)
                else if (w == 'dragon') dragon(level)
                else if (w == 'anvil') anvil()
                else if (w == 'squid') squid(level)
                else if (w == 'jars') jars(level)
                else check('part', false, 'unknown part ' + w)
            } catch (e) { fail++; console.info('[cbetest] FAIL ' + w + ' ERROR ' + e) }
        })
        console.info('[cbetest] done ' + which + ': ' + pass + ' pass, ' + fail + ' fail')
    }

    ServerEvents.commandRegistry(event => {
        const { commands: Commands, arguments: Arguments } = event
        event.register(Commands.literal('cbetest')
            .requires(src => src.hasPermission(2))
            .executes(ctx => { run(ctx.source, 'all'); return 1 })
            .then(Commands.argument('part', Arguments.GREEDY_STRING.create(event)).executes(ctx => {
                run(ctx.source, String(Arguments.GREEDY_STRING.getResult(ctx, 'part'))); return 1
            })))
    })
})()
