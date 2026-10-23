// Scratch-only goose golden egg helper (scratch queue SQ-VP-13, NE-15, NE-16). Copy into a scratch server's
// kubejs/server_scripts as zz_goose.js, never into the pack.
//   /zgoose spawn x y z timer   adult untitledduckmod:goose at x y z with its egg timer set (Incubation Additions EggLayer)
//   /zgoose report x1 z1 x2 z2  "[zgoose] ..." lines: every goose, egg item entity (position) and nest block entity in the box
(() => {
    const $GsReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $GsRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $GsAABB = Java.loadClass('net.minecraft.world.phys.AABB')
    const $GsPos = Java.loadClass('net.minecraft.core.BlockPos')
    function spawn(level, a) {
        let g = $GsReg.ENTITY_TYPES.getValue(new $GsRL('untitledduckmod:goose')).create(level)
        g.setPos(a[0] + 0.5, a[1], a[2] + 0.5)
        level.addFreshEntity(g)
        g.setEggTimer(a[3])
        console.info('[zgoose] spawned at ' + a.slice(0, 3).join(' ') + ' timer ' + g.getEggTimer())
    }
    function report(level, a) {
        let box = new $GsAABB(a[0], -64, a[1], a[2] + 1, 0, a[3] + 1)
        level.getEntities(null, box).forEach(e => {
            let id = String(e.getEncodeId()), at = Math.floor(e.getX()) + ' ' + Math.floor(e.getY()) + ' ' + Math.floor(e.getZ())
            if (id == 'minecraft:item') console.info('[zgoose] item ' + String($GsReg.ITEMS.getKey(e.getItem().getItem())) + ' at ' + at)
            else if (id == 'untitledduckmod:goose') console.info('[zgoose] goose at ' + at + ' timer ' + e.getEggTimer())
        })
        for (let x = a[0]; x <= a[2]; x++) for (let z = a[1]; z <= a[3]; z++) for (let y = -61; y <= -58; y++) {
            let p = new $GsPos(x, y, z), id = String($GsReg.BLOCKS.getKey(level.getBlockState(p).getBlock()))
            if (id.indexOf('nest') >= 0) console.info('[zgoose] nest ' + id + ' at ' + x + ' ' + y + ' ' + z + ' state ' + level.getBlockState(p))
        }
    }
    ServerEvents.commandRegistry(event => {
        const { commands: Commands, arguments: Arguments } = event
        event.register(Commands.literal('zgoose').then(Commands.argument('args', Arguments.GREEDY_STRING.create(event)).executes(ctx => {
            let a = String(Arguments.GREEDY_STRING.getResult(ctx, 'args')).trim().split(/\s+/)
            let level = ctx.source.getLevel(), n = a.slice(1).map(Number)
            if (a[0] == 'spawn') spawn(level, n); else report(level, n)
            return 1
        })))
    })
})()
