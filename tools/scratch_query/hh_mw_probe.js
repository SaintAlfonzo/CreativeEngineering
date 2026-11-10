// Scratch-only probe for the Hearth and Harvest machines and worldgen rows (SQ-HH-201 up, SQ-HH-301 up in
// feature_writeup/testing/scratch_queue.md). Copy into a scratch server's kubejs/server_scripts as zz_hh2.js, never into
// the pack. Output: "[zhh2] ..." lines in logs/latest.log, then "[zhh2] done".
//   /zhh2 tank x y z                      fluid and amount of a machine's tank (jug, trough, sprinkler, basin)
//   /zhh2 fill x y z fluid amount [tagged]   FLUID_HANDLER fill (what a pipe does); "tagged" adds NBT {hh:1b}; prints accepted mB
//   /zhh2 drain x y z amount              FLUID_HANDLER drain; prints what came out
//   /zhh2 insert x y z item n [side]      ITEM_HANDLER insert (what a hopper does); prints the remainder
//   /zhh2 extract x y z slot n [side]     ITEM_HANDLER extract; prints what came out
//   /zhh2 cmp x y z                       comparator output of the block
//   /zhh2 basin x y z                     basin role, controller, tank and slots
//   /zhh2 stomping                        every hearthandharvest:stomping recipe: id, fluid, amount, ingredient count, inputs
//   /zhh2 stomp x y z                     basin.tryProcess with the nearest living entity within 3 blocks (cooldown 5 ticks per entity)
//   /zhh2 sneak name true|false           setShiftKeyDown on a bot or player
//   /zhh2 useat name x y z face hx hy hz  block use with the hit at x+hx y+hy z+hz (pbot use always hits the face centre)
//   /zhh2 countset x1 y1 z1 x2 y2 z2 a,b,c    how many of each block id in the box, one scan (loaded chunks only)
//   /zhh2 chests x1 y1 z1 x2 y2 z2        every chest in the box: position and LootTable
//   /zhh2 unpack x y z                    rolls a chest's loot table now and lists its items
//   /zhh2 lootroll table n                rolls a chest loot table n times: item histogram
(() => {
    const $HArgs = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $HReg = Java.loadClass('net.minecraft.core.Registry')
    const $HRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $HPos = Java.loadClass('net.minecraft.core.BlockPos')
    const $HDir = Java.loadClass('net.minecraft.core.Direction')
    const $HVec = Java.loadClass('net.minecraft.world.phys.Vec3')
    const $HHit = Java.loadClass('net.minecraft.world.phys.BlockHitResult')
    const $HAABB = Java.loadClass('net.minecraft.world.phys.AABB')
    const $HHand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $HLiving = Java.loadClass('net.minecraft.world.entity.LivingEntity')
    const $HChest = Java.loadClass('net.minecraft.world.level.block.entity.ChestBlockEntity')
    const $HCaps = Java.loadClass('net.minecraftforge.common.capabilities.ForgeCapabilities')
    const $HFluids = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $HFStack = Java.loadClass('net.minecraftforge.fluids.FluidStack')
    const $HAction = Java.loadClass('net.minecraftforge.fluids.capability.IFluidHandler$FluidAction')
    const $HHelper = Java.loadClass('net.minecraftforge.items.ItemHandlerHelper')
    const $HNbt = Java.loadClass('net.minecraft.nbt.CompoundTag')
    const $HLootBuilder = Java.loadClass('net.minecraft.world.level.storage.loot.LootContext$Builder')
    const $HLootParams = Java.loadClass('net.minecraft.world.level.storage.loot.parameters.LootContextParams')
    const $HLootSets = Java.loadClass('net.minecraft.world.level.storage.loot.parameters.LootContextParamSets')
    function out(s) { console.info('[zhh2] ' + s) }
    function overworld(server) { return server.overworld() }
    function pos(a, i) { return new $HPos(parseInt(a[i]), parseInt(a[i + 1]), parseInt(a[i + 2])) }
    function dir(name) { return name ? $HDir.byName(name) : null }
    function fluidStr(s) { return s.isEmpty() ? 'empty 0' : String($HFluids.FLUIDS.getKey(s.getFluid())) + ' ' + s.getAmount() }
    function itemStr(s) { return s.isEmpty() ? 'empty' : s.getCount() + ' ' + String($HReg.ITEM.getKey(s.getItem())) }
    function run(server, what, args) {
        let lv = overworld(server)
        let a = args ? String(args).trim().split(/\s+/) : []
        try {
            if (what == 'tank') {
                let be = lv.getBlockEntity(pos(a, 0))
                if (be == null) { out('tank ' + args + ': no block entity') } else {
                    let t = be.tank()
                    out('tank ' + a[0] + ' ' + a[1] + ' ' + a[2] + ': ' + fluidStr(t.getFluid()) + ' / ' + t.getCapacity())
                }
            } else if (what == 'fill' || what == 'drain') {
                let be = lv.getBlockEntity(pos(a, 0))
                let h = be.getCapability($HCaps.FLUID_HANDLER, null).orElse(null)
                if (what == 'fill') {
                    // KubeJS's Fluid.of: a Forge FluidStack made in Rhino (constructor or loadFluidStackFromNBT) reaches fill() as an unequal or empty stack (r1108hh)
                    let fl = Fluid.of(a[3], parseInt(a[4])).getFluidStack()
                    if (a[5] == 'tagged') { let t = new $HNbt(); t.putBoolean('hh', true); fl.setTag(t) }
                    out('fill ' + a[3] + ' ' + a[4] + (a[5] ? ' ' + a[5] : '') + ': accepted ' + h.fill(fl, $HAction.EXECUTE))
                } else {
                    out('drain ' + a[3] + ': ' + fluidStr(h.drain(parseInt(a[3]), $HAction.EXECUTE)))
                }
            } else if (what == 'insert') {
                let be = lv.getBlockEntity(pos(a, 0))
                let h = be.getCapability($HCaps.ITEM_HANDLER, dir(a[5])).orElse(null)
                let rest = $HHelper.insertItem(h, Item.of(a[3], parseInt(a[4])), false)
                out('insert ' + a[3] + ' x' + a[4] + ': remainder ' + itemStr(rest))
            } else if (what == 'extract') {
                let be = lv.getBlockEntity(pos(a, 0))
                let h = be.getCapability($HCaps.ITEM_HANDLER, dir(a[5])).orElse(null)
                out('extract slot ' + a[3] + ' x' + a[4] + ': ' + itemStr(h.extractItem(parseInt(a[3]), parseInt(a[4]), false)))
            } else if (what == 'cmp') {
                let p = pos(a, 0)
                out('cmp ' + a[0] + ' ' + a[1] + ' ' + a[2] + ': ' + lv.getBlockState(p).getAnalogOutputSignal(lv, p))
            } else if (what == 'basin') {
                let be = lv.getBlockEntity(pos(a, 0))
                let its = be.items(), parts = []
                for (let k = 0; k < its.getSlots(); k++) parts.push(itemStr(its.getStackInSlot(k)))
                let c = be.controllerPos()
                out('basin ' + a[0] + ' ' + a[1] + ' ' + a[2] + ': role ' + be.role().getSerializedName() + ', controller '
                    + (c == null ? 'none' : c.getX() + ' ' + c.getY() + ' ' + c.getZ()) + ', tank ' + fluidStr(be.tank().getFluid()) + ' / ' + be.tank().getCapacity() + ', slots ' + parts.join(', '))
            } else if (what == 'stomping') {
                let n = 0
                server.getRecipeManager().getRecipes().forEach(r => {
                    let id = String(r.getId())
                    if (id.indexOf('stomping/') < 0) return
                    if (String($HReg.RECIPE_SERIALIZER.getKey(r.getSerializer())) != 'hearthandharvest:stomping') return
                    n++
                    let fl = r.resultFluid()
                    let ins = []
                    r.getIngredients().forEach(g => {
                        let first = g.getStacks().toList().get(0)
                        ins.push(String(first.getId()))
                    })
                    out('stomping ' + id + ': ' + fluidStr(fl) + ' x' + ins.length + ' <- ' + ins[0])
                })
                out('stomping recipes: ' + n)
            } else if (what == 'stomp') {
                let p = pos(a, 0)
                let be = lv.getBlockEntity(p)
                let list = lv.getEntitiesOfClass($HLiving, new $HAABB(p.getX() - 3, p.getY() - 3, p.getZ() - 3, p.getX() + 4, p.getY() + 4, p.getZ() + 4))
                if (list.isEmpty()) { out('stomp: no living entity in reach') } else {
                    be.tryProcess(list.get(0))
                    out('stomp done, tank ' + fluidStr(be.tank().getFluid()))
                }
            } else if (what == 'sneak') {
                let p = server.getPlayerList().getPlayerByName(a[0])
                if (p == null) { out('sneak: no player ' + a[0]) } else { p.setShiftKeyDown(a[1] == 'true'); out('sneak ' + a[0] + ' ' + a[1]) }
            } else if (what == 'useat') {
                let p = server.getPlayerList().getPlayerByName(a[0])
                if (p == null) { out('useat: no player ' + a[0]) } else {
                    let bp = pos(a, 1)
                    let d = dir(a[4])
                    let hit = new $HHit(new $HVec(bp.getX() + parseFloat(a[5]), bp.getY() + parseFloat(a[6]), bp.getZ() + parseFloat(a[7])), d, bp, false)
                    let r = p.gameMode.useItemOn(p, p.getLevel(), p.getMainHandItem(), $HHand.MAIN_HAND, hit)
                    out('useat ' + a[1] + ' ' + a[2] + ' ' + a[3] + ': ' + r)
                }
            } else if (what == 'countset') {
                let want = {}
                a[6].split(',').forEach(id => { want[id] = 0 })
                let x1 = Math.min(+a[0], +a[3]), x2 = Math.max(+a[0], +a[3])
                let y1 = Math.min(+a[1], +a[4]), y2 = Math.max(+a[1], +a[4])
                let z1 = Math.min(+a[2], +a[5]), z2 = Math.max(+a[2], +a[5])
                let skipped = 0
                for (let x = x1; x <= x2; x++) for (let z = z1; z <= z2; z++) {
                    if (!lv.hasChunk(x >> 4, z >> 4)) { skipped++; continue }
                    for (let y = y1; y <= y2; y++) {
                        let id = String($HReg.BLOCK.getKey(lv.getBlockState(new $HPos(x, y, z)).getBlock()))
                        if (want[id] !== undefined) want[id]++
                    }
                }
                Object.keys(want).forEach(id => out('countset ' + id + ': ' + want[id]))
                out('countset columns skipped: ' + skipped)
            } else if (what == 'chests') {
                let x1 = Math.min(+a[0], +a[3]), x2 = Math.max(+a[0], +a[3])
                let y1 = Math.min(+a[1], +a[4]), y2 = Math.max(+a[1], +a[4])
                let z1 = Math.min(+a[2], +a[5]), z2 = Math.max(+a[2], +a[5])
                let n = 0
                for (let x = x1; x <= x2; x++) for (let z = z1; z <= z2; z++) {
                    if (!lv.hasChunk(x >> 4, z >> 4)) continue
                    for (let y = y1; y <= y2; y++) {
                        let be = lv.getBlockEntity(new $HPos(x, y, z))
                        if (be == null || !(be instanceof $HChest)) continue
                        n++
                        let tag = be.saveWithFullMetadata()
                        out('chest ' + x + ' ' + y + ' ' + z + ' table ' + (tag.contains('LootTable') ? tag.getString('LootTable') : 'none'))
                    }
                }
                out('chests: ' + n)
            } else if (what == 'unpack') {
                let p = pos(a, 0)
                let be = lv.getBlockEntity(p)
                be.unpackLootTable(null)
                let n = 0
                for (let k = 0; k < be.getContainerSize(); k++) {
                    let s = be.getItem(k)
                    if (!s.isEmpty()) { n++; out('unpack item ' + itemStr(s)) }
                }
                out('unpack items: ' + n)
            } else if (what == 'lootroll') {
                let table = server.getLootTables().get(new $HRL(a[0]))
                let n = parseInt(a[1]), hist = {}
                for (let k = 0; k < n; k++) {
                    let ctx = new $HLootBuilder(lv).withParameter($HLootParams.ORIGIN, new $HVec(0.5, 64.5, 0.5)).create($HLootSets.CHEST)
                    table.getRandomItems(ctx).forEach(s => {
                        let id = String($HReg.ITEM.getKey(s.getItem()))
                        hist[id] = (hist[id] || 0) + s.getCount()
                    })
                }
                Object.keys(hist).sort().forEach(id => out('lootroll ' + id + ' ' + hist[id]))
                out('lootroll ' + a[0] + ' rolls ' + n + ', ids ' + Object.keys(hist).length)
            } else out('unknown query ' + what)
        } catch (err) { out('error: ' + err) }
        out('done')
    }
    ServerEvents.commandRegistry(e => {
        const { commands: C } = e
        e.register(C.literal('zhh2')
            .then(C.argument('what', $HArgs.word())
                .executes(ctx => { run(ctx.source.server, $HArgs.getString(ctx, 'what'), ''); return 1 })
                .then(C.argument('arg', $HArgs.greedyString())
                    .executes(ctx => {
                        run(ctx.source.server, $HArgs.getString(ctx, 'what'), $HArgs.getString(ctx, 'arg'))
                        return 1
                    }))))
    })
})()
