// Salt merge block-behaviour probe (scratch queue SQ-SALT-05 to -07, feature_writeup/item_merges.md 2.9). Scratch-only:
// copy into a scratch server's kubejs/server_scripts as zz_salt_probe.js, never into the pack. Run /salttest; results are
// "[salttest] PASS|FAIL|NOTE ..." lines in logs/latest.log, then "[salttest] done N pass, M fail".
// A dedicated server with no player gives forceloaded chunks no random ticks, so the probe calls each block's
// randomTick itself (the same entry the patcherMod salt mixins hook), up to TRIES times, stopping once the block or
// its neighbour changes. Cases sit 10 below the build limit at x 300.., z 60 (open sky in any world type; rain
// needs a biome that rains at that height, the NOTE line names it).
(() => {
    const $STReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $STRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $STPos = Java.loadClass('net.minecraft.core.BlockPos')
    const $STDir = Java.loadClass('net.minecraft.core.Direction')

    const TRIES = 400
    const VT = 'vintagedelight:'

    let pass = 0, fail = 0
    function check(name, ok, detail) {
        if (ok) pass++; else fail++
        console.info('[salttest] ' + (ok ? 'PASS' : 'FAIL') + ' ' + name + ': ' + detail)
    }
    function note(name, detail) { console.info('[salttest] NOTE ' + name + ': ' + detail) }

    function block(id) { return $STReg.BLOCKS.getValue(new $STRL(id)) }
    function idAt(level, pos) { return String($STReg.BLOCKS.getKey(level.getBlockState(pos).getBlock())) }
    function set(level, pos, id) { level.setBlock(pos, block(id).defaultBlockState(), 2) }
    function layersAt(level, pos) {
        let st = level.getBlockState(pos)
        let p = st.getBlock().getStateDefinition().getProperty('layers')
        return p == null ? 0 : parseInt(String(st.getValue(p)))
    }
    // clears the 3x3x3 around pos and lays a stone floor under it (water stays put, layers have support)
    function clear(level, pos) {
        for (let dx = -1; dx <= 1; dx++) for (let dy = -1; dy <= 1; dy++) for (let dz = -1; dz <= 1; dz++)
            set(level, pos.offset(dx, dy, dz), dy == -1 ? 'minecraft:stone' : 'minecraft:air')
    }
    // random-ticks pos until stop() is true; the number of ticks it took, or -1
    function tickUntil(level, pos, stop) {
        let rnd = level.getRandom()
        for (let n = 1; n <= TRIES; n++) {
            let st = level.getBlockState(pos)
            if (st.isAir()) return -1
            st.randomTick(level, pos, rnd)
            if (stop()) return n
        }
        return -1
    }
    function tickTimes(level, pos, times) {
        let rnd = level.getRandom()
        for (let n = 0; n < times; n++) {
            let st = level.getBlockState(pos)
            if (st.isAir()) return
            st.randomTick(level, pos, rnd)
        }
    }

    let caseX = 300
    function nextPos(level) { let p = new $STPos(caseX, level.getMaxBuildHeight() - 10, 60); caseX += 4; return p }

    // SQ-SALT-05: every salt block melts a touching snow layer and ice
    function melting(level) {
        ;[VT + 'salt_bricks', VT + 'salt_brick_stairs', VT + 'salt_lamp_default', VT + 'salt', 'salt:raw_rock_salt_block',
            'salt:salt_block'].forEach(saltId => {
            ;['minecraft:snow', 'minecraft:ice'].forEach(cold => {
                let pos = nextPos(level), side = pos.relative($STDir.EAST)
                clear(level, pos)
                set(level, pos, saltId); set(level, side, cold)
                let n = tickUntil(level, pos, () => idAt(level, side) != cold)
                check(saltId + ' melts ' + cold, n > 0, n > 0 ? 'after ' + n + ' ticks: ' + idAt(level, side) + ', salt ' + idAt(level, pos)
                    : 'still ' + idAt(level, side) + ' after ' + TRIES)
                clear(level, pos)
            })
        })
    }

    // SQ-SALT-06: dissolving, rain, light, the Evaporator
    function dissolving(level) {
        let wasRaining = level.isRaining()
        level.setWeatherParameters(6000, 0, false, false)
        // Salt Layer beside a water source becomes water
        let pos = nextPos(level), side = pos.relative($STDir.EAST)
        clear(level, pos); set(level, pos, VT + 'salt'); set(level, side, 'minecraft:water')
        let n = tickUntil(level, pos, () => idAt(level, pos) != VT + 'salt')
        check('Salt Layer beside water becomes water', n > 0 && idAt(level, pos) == 'minecraft:water', n + ' ticks, now ' + idAt(level, pos))
        clear(level, pos)

        // Salt Layer by a torch, no rain: stays (the snow-layer melt by light is replaced)
        pos = nextPos(level)
        clear(level, pos); set(level, pos, VT + 'salt'); set(level, pos.relative($STDir.EAST), 'minecraft:torch')
        tickTimes(level, pos, TRIES)
        check('Salt Layer by a torch stays', idAt(level, pos) == VT + 'salt', 'now ' + idAt(level, pos))
        clear(level, pos)

        // raw rock salt beside water stays
        pos = nextPos(level)
        clear(level, pos); set(level, pos, 'salt:raw_rock_salt_block'); set(level, pos.relative($STDir.EAST), 'minecraft:water')
        tickTimes(level, pos, TRIES)
        check('raw rock salt beside water stays', idAt(level, pos) == 'salt:raw_rock_salt_block', 'now ' + idAt(level, pos))
        clear(level, pos)

        // Vintage's leftover Salt Block beside water dissolves and grows no layer
        pos = nextPos(level)
        clear(level, pos); set(level, pos, VT + 'salt_block'); set(level, pos.relative($STDir.EAST), 'minecraft:water')
        let grew = false
        n = tickUntil(level, pos, () => { if (idAt(level, pos.above()) == VT + 'salt') grew = true; return idAt(level, pos) != VT + 'salt_block' })
        check('leftover vintagedelight:salt_block by water dissolves, no layer', n > 0 && !grew,
            n + ' ticks, now ' + idAt(level, pos) + (grew ? ', grew a layer' : ''))
        clear(level, pos)

        // Evaporator by water grows a layer on top, up to 8
        pos = nextPos(level)
        clear(level, pos); set(level, pos, VT + 'evaporator'); set(level, pos.relative($STDir.EAST), 'minecraft:water')
        tickTimes(level, pos, TRIES)
        check('Evaporator by water grows layers to 8', idAt(level, pos.above()) == VT + 'salt' && layersAt(level, pos.above()) == 8,
            'above ' + idAt(level, pos.above()) + ' layers ' + layersAt(level, pos.above()) + ', evaporator ' + idAt(level, pos))
        set(level, pos.above(), 'minecraft:air'); clear(level, pos)

        // rain: Salt Layer in open sky goes, raw rock salt stays
        level.setWeatherParameters(0, 6000, true, false)
        pos = nextPos(level)
        let biome = String(level.getBiome(pos).unwrapKey().get().location())
        let rainy = level.isRainingAt(pos.above())
        note('rain cases', 'biome ' + biome + ', isRainingAt ' + rainy)
        clear(level, pos); set(level, pos, VT + 'salt')
        n = tickUntil(level, pos, () => idAt(level, pos) != VT + 'salt')
        check('Salt Layer in rain, open sky, becomes air', rainy && n > 0 && idAt(level, pos) == 'minecraft:air',
            rainy ? n + ' ticks, now ' + idAt(level, pos) : 'no rain at this spot (biome ' + biome + ')')
        clear(level, pos)
        pos = nextPos(level)
        clear(level, pos); set(level, pos, 'salt:raw_rock_salt_block')
        tickTimes(level, pos, TRIES)
        check('raw rock salt in rain stays', idAt(level, pos) == 'salt:raw_rock_salt_block', 'now ' + idAt(level, pos))
        clear(level, pos)
        level.setWeatherParameters(wasRaining ? 0 : 6000, wasRaining ? 6000 : 0, wasRaining, false)
    }

    // SQ-SALT-07: cost of one salt-brick random tick with nothing to melt, against the 0.1 % budget for 10 000 bricks.
    // Calls go through Rhino, so the figure is an upper bound.
    function cost(level) {
        let pos = nextPos(level)
        clear(level, pos)
        let calls = 200000
        let run = id => {
            set(level, pos, id)
            let st = level.getBlockState(pos), rnd = level.getRandom()
            let t0 = Date.now()
            for (let n = 0; n < calls; n++) st.randomTick(level, pos, rnd)
            return (Date.now() - t0) * 1000 / calls
        }
        let stone = run('minecraft:stone')
        let brick = run(VT + 'salt_bricks')
        // 10 000 bricks: 3 picks per section per tick out of 4096 blocks, 20 ticks a second
        let hitsPerSecond = 10000 * 3 * 20 / 4096
        let share = hitsPerSecond * Math.max(brick - stone, 0) / 1e6 * 100
        check('salt brick random tick under 0.1 % of server time at 10 000 bricks', share < 0.1,
            brick.toFixed(3) + ' us per call (stone ' + stone.toFixed(3) + ' us, Rhino overhead), ' + hitsPerSecond.toFixed(0) +
            ' hits/s -> ' + share.toFixed(4) + ' %')
        clear(level, pos)
    }

    ServerEvents.commandRegistry(event => {
        const { commands: Commands } = event
        event.register(Commands.literal('salttest').requires(s => s.hasPermission(2)).executes(ctx => {
            let level = ctx.source.getLevel()
            pass = 0; fail = 0; caseX = 300
            ;[['melting', melting], ['dissolving', dissolving], ['cost', cost]].forEach(p => {
                try { p[1](level) } catch (err) { check(p[0] + ' ran', false, String(err)) }
            })
            console.info('[salttest] done ' + pass + ' pass, ' + fail + ' fail')
            return 1
        }))
    })
})()
