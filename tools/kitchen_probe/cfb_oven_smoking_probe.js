// TODO food/oven #4 probe: the Cooking for Blockheads oven cooks smoker recipes too (patcherMod CfbOvenSmokingMixin).
// Scratch-only: copy into a scratch server's kubejs/server_scripts (tools/creative_labels_test/scratch_server.sh),
// never into the pack. Run /kotest; results are "[kotest] PASS|FAIL|INFO ..." lines in logs/latest.log, and the
// live cook check reports 30 s later.
(() => {
    const $KOBlockPos = Java.loadClass('net.minecraft.core.BlockPos')
    const $KOForgeReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $KORL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $KOBlocks = Java.loadClass('net.minecraft.world.level.block.Blocks')

    const Y = -58
    function log(kind, name, detail) { console.info('[kotest] ' + kind + ' ' + name + ': ' + detail) }
    function check(name, ok, detail) { log(ok ? 'PASS' : 'FAIL', name, detail) }
    function idOf(st) { return String($KOForgeReg.ITEMS.getKey(st.getItem())) }
    function pos(x, y, z) { return new $KOBlockPos(x, y, z) }
    function set(level, p, id) { level.setBlockAndUpdate(p, $KOForgeReg.BLOCKS.getValue(new $KORL(id)).defaultBlockState()) }
    function result(be, id) { return idOf(be.getSmeltingResult(Item.of(id))) }

    function oven(level, server) {
        let h = pos(0, Y, 0)
        set(level, h, 'cookingforblockheads:oven')
        let be = level.getBlockEntity(h)
        let ham = result(be, 'farmersdelight:ham')
        check('OV-24a ham smokes (smoking only)', ham == 'farmersdelight:smoked_ham', 'getSmeltingResult(ham) = ' + ham)
        let beef = result(be, 'minecraft:beef')
        check('OV-24b beef still smelts', beef == 'minecraft:cooked_beef', 'getSmeltingResult(beef) = ' + beef)
        let cobble = result(be, 'minecraft:cobblestone')
        check('OV-24c non-food still refused', cobble == 'minecraft:air', 'getSmeltingResult(cobblestone) = ' + cobble)
        let c = be.getContainer()
        // CfB 13 layout: 0-2 input, 3 fuel, 4-6 output, 7-15 processing, 16-19 tools
        c.setItem(3, Item.of('minecraft:coal', 4))
        c.setItem(0, Item.of('farmersdelight:ham', 1))
        server.scheduleInTicks(600, () => {
            let out = []
            let smoked = 0
            for (let s = 0; s < c.getContainerSize(); s++) {
                let st = c.getItem(s)
                if (st.isEmpty()) continue
                out.push(s + ':' + st.getCount() + 'x ' + idOf(st))
                if (s >= 4 && s <= 6 && idOf(st) == 'farmersdelight:smoked_ham') smoked += st.getCount()
            }
            check('OV-24d ham cooks in the oven', smoked == 1, out.join(', '))
        })
    }

    ServerEvents.commandRegistry(event => {
        const { commands: Commands } = event
        event.register(Commands.literal('kotest').executes(ctx => {
            let level = ctx.source.getLevel()
            let server = ctx.source.getServer()
            for (let x = -2; x <= 2; x++) for (let z = -2; z <= 2; z++) {
                set(level, pos(x, Y - 1, z), 'minecraft:stone')
                for (let y = Y; y <= Y + 2; y++) level.setBlockAndUpdate(pos(x, y, z), $KOBlocks.AIR.defaultBlockState())
            }
            try { oven(level, server) } catch (e) { log('FAIL', 'oven', 'ERROR ' + e) }
            log('INFO', 'done', 'cook result follows at 30 s')
            return 1
        }))
    })
})()
