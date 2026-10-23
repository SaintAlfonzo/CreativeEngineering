// Scratch-only: /zcount x1 y1 z1 x2 y2 z2 logs "[zcount] blocks ..." (id=count) and "[zcount] entities ..." (type=count, items
// by item id) for a loaded box. Used by scratch queue SQ-FGD-07 (pig truffle digging). Copy into a scratch server's
// kubejs/server_scripts as zz_count.js, never into the pack. Load the chunks first (forceload): no chunk loads here.
(() => {
    const $ZPos = Java.loadClass('net.minecraft.core.BlockPos')
    const $ZAABB = Java.loadClass('net.minecraft.world.phys.AABB')
    const $ZReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    function run(src, a) {
        let level = src.getLevel(), blocks = {}, ents = {}
        for (let x = Math.min(a[0], a[3]); x <= Math.max(a[0], a[3]); x++)
            for (let y = Math.min(a[1], a[4]); y <= Math.max(a[1], a[4]); y++)
                for (let z = Math.min(a[2], a[5]); z <= Math.max(a[2], a[5]); z++) {
                    let id = String($ZReg.BLOCKS.getKey(level.getBlockState(new $ZPos(x, y, z)).getBlock()))
                    blocks[id] = (blocks[id] || 0) + 1
                }
        let box = new $ZAABB(a[0], a[1], a[2], a[3] + 1, a[4] + 1, a[5] + 1)
        level.getEntities(null, box).forEach(e => {
            let id = String(e.getEncodeId())
            if (id == 'minecraft:item') id = 'item:' + String($ZReg.ITEMS.getKey(e.getItem().getItem())) + ' x' + e.getItem().getCount()
            ents[id] = (ents[id] || 0) + 1
        })
        console.info('[zcount] blocks ' + Object.keys(blocks).map(k => k + '=' + blocks[k]).join(', '))
        console.info('[zcount] entities ' + (Object.keys(ents).map(k => k + '=' + ents[k]).join(', ') || 'none'))
    }
    ServerEvents.commandRegistry(event => {
        const { commands: Commands, arguments: Arguments } = event
        event.register(Commands.literal('zcount').then(Commands.argument('box', Arguments.GREEDY_STRING.create(event)).executes(ctx => {
            run(ctx.source, String(Arguments.GREEDY_STRING.getResult(ctx, 'box')).trim().split(/\s+/).map(Number)); return 1
        })))
    })
})()
