// Scratch-only probe for the r1113 chunk-4 rows (scratch_queue.md SQ-GAP, SQ-FLR, SQ-FDI, SQ-BREED). Copy into a scratch
// server's kubejs/server_scripts as zz_zc4_probe.js, never into the pack. Command: /zc4 WHAT [ARGS] (permission 2).
// Output: "[zc4] ..." lines in logs/latest.log; every call ends with "[zc4] done". No chunk loads: forceload first.
//   cmd a,b,c                 root command registered or missing, one line each ("[zc4] cmd NAME registered|missing")
//   put X Y Z ITEM N          insert N of ITEM into the block's item handler (no side): "[zc4] put ... inserted=K"
//   fill X Y Z FLUID MB       fill the block's fluid handler: "[zc4] fill ... filled=K"
//   inv X Y Z                 item handler slots and fluid tanks: "[zc4] inv X Y Z total=N id xN, ..." and "[zc4] tanks ..."
//   pools X Y Z R             item entities within R summed into the Visceral Heap pools (plan_flesh_recipes.md 3):
//                             "[zc4] pools meat=N bone=N eye=N misc=N slime=N string=N other=N total=N"; R < 0 = clear
//                             the counted entities after printing (radius |R|)
//   poolsinv X Y Z            the same sums over the block's item handler
//   fuel X Y Z ITEM           Create BlazeBurnerBlock.tryInsert with ITEM (no overflow), then the block's blaze property
//   cooktime TEXT             Cooking Pot recipes whose id or result contains TEXT: "[zc4] cook ID -> N RESULT time=T"
//   eat NAME ITEM             player NAME eats ITEM as a client use would finish it (finishUsingItem plus Forge's
//                             LivingEntityUseItemEvent.Finish, which both Spice of Life books listen to)
//   hunger NAME N             set food level N (saturation 0), so placed cakes and pies accept a bite
//   sol NAME [ITEM]           "[zc4] sol NAME carrot=N applepie=N maxhealth=H" (+ "carrotHas=b applepieHas=b" for ITEM)
//   isfood TYPE ITEM          Animal.isFood of a fresh (not added) entity of TYPE: "[zc4] isfood TYPE ITEM true|false"
//   bees X Y Z R              per bee: FlowerPos, HivePos, HasNectar ("[zc4] bee ... flower=x,y,z|none ...")
(() => {
    const $C4Args = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $C4Reg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $C4RL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $C4Pos = Java.loadClass('net.minecraft.core.BlockPos')
    const $C4Caps = Java.loadClass('net.minecraftforge.common.capabilities.ForgeCapabilities')
    const $C4Item = Java.loadClass('net.minecraft.world.entity.item.ItemEntity')
    const $C4AABB = Java.loadClass('net.minecraft.world.phys.AABB')
    const $C4CT = Java.loadClass('net.minecraft.nbt.CompoundTag')
    const $C4Stack = Java.loadClass('net.minecraft.world.item.ItemStack')
    function out(s) { console.info('[zc4] ' + s) }
    function rl(s) { return new $C4RL(s) }
    function lvl(server) { return server.overworld() }
    function pos(a, i) { return new $C4Pos(parseInt(a[i]), parseInt(a[i + 1]), parseInt(a[i + 2])) }
    function stackOf(id, n) { return new $C4Stack($C4Reg.ITEMS.getValue(rl(id)), n) }
    function itemId(st) { return String($C4Reg.ITEMS.getKey(st.getItem())) }
    function cap(be, c) { return be == null ? null : be.getCapability(c).orElse(null) }
    function player(server, name) {
        let p = server.getPlayerList().getPlayerByName(name)
        if (p == null) throw 'no player ' + name
        return p
    }
    const POOLS = {
        meat: ['minecraft:rotten_flesh', 'hostiledelight:rottenham', 'minecraft:leather', 'miners_delight:spider_leg',
            'nethersdelight:hoglin_ear', 'nethersdelight:hoglin_loin'],
        bone: ['minecraft:bone', 'minecraft:bone_meal', 'philipsruins:broken_bone', 'philipsruins:bone_chunk',
            'alexsmobs:bone_serpent_tooth'],
        eye: ['minecraft:spider_eye', 'biomesoplenty:eyebulb', 'minecraft:fermented_spider_eye'],
        misc: ['biomesoplenty:pus_bubble', 'biomesoplenty:flesh_tendons', 'biomesoplenty:hair'],
        slime: ['minecraft:slime_ball'],
        string: ['minecraft:string']
    }
    function poolLine(counts) {
        let sums = { meat: 0, bone: 0, eye: 0, misc: 0, slime: 0, string: 0, other: 0 }, total = 0, others = []
        Object.keys(counts).forEach(id => {
            let n = counts[id], g = 'other'
            Object.keys(POOLS).forEach(k => { if (POOLS[k].indexOf(id) >= 0) g = k })
            sums[g] += n; total += n
            if (g == 'other') others.push(id + ' x' + n)
        })
        out('pools meat=' + sums.meat + ' bone=' + sums.bone + ' eye=' + sums.eye + ' misc=' + sums.misc + ' slime=' + sums.slime +
            ' string=' + sums.string + ' other=' + sums.other + ' total=' + total + (others.length ? ' others: ' + others.join(', ') : ''))
        out('pools by id ' + Object.keys(counts).sort().map(k => k + ' x' + counts[k]).join(', '))
    }
    function invCounts(h) {
        let counts = {}
        for (let s = 0; s < h.getSlots(); s++) {
            let st = h.getStackInSlot(s)
            if (st.isEmpty()) continue
            let id = itemId(st)
            counts[id] = (counts[id] || 0) + st.getCount()
        }
        return counts
    }
    function run(server, what, argStr) {
        // String(): argStr is a Java String, and Java's split("/\\s+/") never splits (r1113 c4_a: every multi-arg call failed)
        let argS = String(argStr).trim(), a = argS.length ? argS.split(/\s+/) : [], level = lvl(server)
        try {
            if (what == 'cmd') {
                let root = server.getCommands().getDispatcher().getRoot()
                a[0].split(',').forEach(n => out('cmd ' + n + ' ' + (root.getChild(n) != null ? 'registered' : 'missing')))
            } else if (what == 'put') {
                let h = cap(level.getBlockEntity(pos(a, 0)), $C4Caps.ITEM_HANDLER)
                if (h == null) throw 'no item handler at ' + a.slice(0, 3).join(' ')
                let left = stackOf(a[3], parseInt(a[4]))
                for (let s = 0; s < h.getSlots() && !left.isEmpty(); s++) left = h.insertItem(s, left, false)
                out('put ' + a.slice(0, 3).join(' ') + ' ' + a[3] + ' inserted=' + (parseInt(a[4]) - left.getCount()))
            } else if (what == 'fill') {
                let h = cap(level.getBlockEntity(pos(a, 0)), $C4Caps.FLUID_HANDLER)
                if (h == null) throw 'no fluid handler at ' + a.slice(0, 3).join(' ')
                let $FA = Java.loadClass('net.minecraftforge.fluids.capability.IFluidHandler$FluidAction')
                // KubeJS FluidStackJS -> Forge FluidStack: a FluidStack built with new in Rhino can reach fill() as a
                // non-matching stack (accepted 0), scratch-test gotchas
                let n = h.fill(Fluid.of(a[3], parseInt(a[4])).getFluidStack(), $FA.EXECUTE)
                out('fill ' + a.slice(0, 3).join(' ') + ' ' + a[3] + ' filled=' + n)
            } else if (what == 'inv' || what == 'poolsinv') {
                let be = level.getBlockEntity(pos(a, 0))
                let h = cap(be, $C4Caps.ITEM_HANDLER), counts = h == null ? {} : invCounts(h), total = 0
                Object.keys(counts).forEach(k => total += counts[k])
                out('inv ' + a.slice(0, 3).join(' ') + ' total=' + total + ' ' + Object.keys(counts).sort().map(k => k + ' x' + counts[k]).join(', '))
                let fh = cap(be, $C4Caps.FLUID_HANDLER), tanks = []
                if (fh != null) for (let t = 0; t < fh.getTanks(); t++) {
                    let fs = fh.getFluidInTank(t)
                    if (!fs.isEmpty()) tanks.push(String($C4Reg.FLUIDS.getKey(fs.getFluid())) + ' ' + fs.getAmount())
                }
                out('tanks ' + a.slice(0, 3).join(' ') + ' ' + (tanks.length ? tanks.join(', ') : 'empty'))
                if (what == 'poolsinv') poolLine(counts)
            } else if (what == 'pools') {
                let x = parseFloat(a[0]), y = parseFloat(a[1]), z = parseFloat(a[2]), r = parseFloat(a[3]), clear = r < 0, counts = {}
                r = Math.abs(r)
                level.getEntitiesOfClass($C4Item, new $C4AABB(x - r, y - r, z - r, x + r + 1, y + r + 1, z + r + 1)).forEach(e => {
                    let st = e.getItem(), id = itemId(st)
                    counts[id] = (counts[id] || 0) + st.getCount()
                    if (clear) e.discard()
                })
                poolLine(counts)
            } else if (what == 'fuel') {
                // BlazeBurnerBlockEntity.tryUpdateFuel is protected (Rhino cannot call it); the block's public static
                // tryInsert(state, level, pos, stack, doNotConsume, forceOverflow, simulate) feeds the same path
                let p = pos(a, 0), ok = 'n/a'
                try {
                    let $BBB = Java.loadClass('com.simibubi.create.content.processing.burner.BlazeBurnerBlock')
                    ok = String($BBB.tryInsert(level.getBlockState(p), level, p, stackOf(a[3], 1), false, false, false).getResult())
                } catch (err) { ok = 'error ' + err }
                let st = level.getBlockState(p), props = []
                st.getValues().forEach((k, v) => props.push(k.getName() + '=' + k.getName(v)))   // Property.getName(value): lower case, as in /setblock
                out('fuel ' + a.slice(0, 3).join(' ') + ' ' + a[3] + ' result=' + ok + ' state ' + props.join(','))
            } else if (what == 'cooktime') {
                let n = 0
                server.getRecipeManager().getRecipes().forEach(r => {
                    // serializer, not getType(): Mechanical Extruder's ExtrudingRecipe.getType() throws AbstractMethodError (a Java
                    // Error Rhino cannot catch; it crashed the r1113 c4_a server in SQ-GAP-01)
                    let id = String(r.getId()), t = '?'
                    try { t = String($C4Reg.RECIPE_SERIALIZERS.getKey(r.getSerializer())) } catch (err) { }
                    if (t != 'farmersdelight:cooking') return
                    let res = r.getResultItem(), rid = itemId(res)
                    if (id.indexOf(a[0]) < 0 && rid.indexOf(a[0]) < 0) return
                    n++
                    let time = '?'
                    try { time = r.getCookTime() } catch (err) { }
                    out('cook ' + id + ' -> ' + res.getCount() + ' ' + rid + ' time=' + time)
                })
                out('cooktime ' + a[0] + ' matched ' + n)
            } else if (what == 'eat') {
                let p = player(server, a[0]), st = stackOf(a[1], 1), copy = st.copy()
                let $FEF = Java.loadClass('net.minecraftforge.event.ForgeEventFactory')
                let res = st.finishUsingItem(level, p)
                $FEF.onItemUseFinish(p, copy, 0, res)
                out('eat ' + a[0] + ' ' + a[1] + ' food=' + p.getFoodData().getFoodLevel())
            } else if (what == 'hunger') {
                let p = player(server, a[0])
                p.getFoodData().setFoodLevel(parseInt(a[1]))
                p.getFoodData().setSaturation(0)
                out('hunger ' + a[0] + ' ' + p.getFoodData().getFoodLevel())
            } else if (what == 'sol') {
                let p = player(server, a[0]), it = a[1] ? $C4Reg.ITEMS.getValue(rl(a[1])) : null, line = 'sol ' + a[0]
                try {
                    let cl = Java.loadClass('com.cazsius.solcarrot.tracking.FoodList').get(p)
                    line += ' carrot=' + cl.getEatenFoodCount() + (it ? ' carrotHas=' + cl.hasEaten(it) : '')
                } catch (err) { line += ' carrot=error(' + err + ')' }
                try {
                    let al = Java.loadClass('com.tarinoita.solsweetpotato.tracking.FoodList').get(p)
                    line += ' applepie=' + al.getEatenFoods().size() + ' applepieEntries=' + al.getFoodsEaten() + (it ? ' applepieHas=' + al.hasEaten(it) : '')
                } catch (err) { line += ' applepie=error(' + err + ')' }
                line += ' maxhealth=' + p.getMaxHealth()
                out(line)
            } else if (what == 'isfood') {
                let type = $C4Reg.ENTITY_TYPES.getValue(rl(a[0])), e = type.create(level)
                out('isfood ' + a[0] + ' ' + a[1] + ' ' + e.isFood(stackOf(a[1], 1)))
                e.discard()
            } else if (what == 'bees') {
                let x = parseFloat(a[0]), y = parseFloat(a[1]), z = parseFloat(a[2]), r = parseFloat(a[3]), n = 0
                let $Bee = Java.loadClass('net.minecraft.world.entity.animal.Bee')
                level.getEntitiesOfClass($Bee, new $C4AABB(x - r, y - r, z - r, x + r + 1, y + r + 1, z + r + 1)).forEach(b => {
                    let t = b.saveWithoutId(new $C4CT()), f = 'none', hv = 'none'
                    if (t.contains('FlowerPos')) { let c = t.getCompound('FlowerPos'); f = c.getInt('X') + ',' + c.getInt('Y') + ',' + c.getInt('Z') }
                    if (t.contains('HivePos')) { let c = t.getCompound('HivePos'); hv = c.getInt('X') + ',' + c.getInt('Y') + ',' + c.getInt('Z') }
                    n++
                    out('bee ' + Math.floor(b.getX()) + ' ' + Math.floor(b.getY()) + ' ' + Math.floor(b.getZ()) + ' flower=' + f + ' hive=' + hv + ' nectar=' + t.getBoolean('HasNectar'))
                })
                out('bees ' + n)
            } else {
                out('unknown subcommand ' + what)
            }
        } catch (err) {
            out('error ' + what + ' ' + argStr + ': ' + err)
        }
        out('done')
    }
    ServerEvents.commandRegistry(e => {
        const { commands: C } = e
        e.register(C.literal('zc4').requires(s => s.hasPermission(2))
            .then(C.argument('what', $C4Args.word())
                .executes(ctx => { run(ctx.source.server, $C4Args.getString(ctx, 'what'), ''); return 1 })
                .then(C.argument('arg', $C4Args.greedyString())
                    .executes(ctx => {
                        run(ctx.source.server, $C4Args.getString(ctx, 'what'), $C4Args.getString(ctx, 'arg'))
                        return 1
                    }))))
    })
})()
