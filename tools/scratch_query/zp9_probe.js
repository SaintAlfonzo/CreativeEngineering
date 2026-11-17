// Scratch-only probe for r1115 phase 9 (scratch_queue.md SQ-FDI-04, SQ-NSM-05, SQ-BREED-01, SQ-RDB-05). Copy into a
// scratch server's kubejs/server_scripts as zz_zp9_probe.js, never into the pack. Command: /zp9 WHAT [ARGS] (permission 2).
// Output: "[zp9] ..." lines in logs/latest.log; every call ends with "[zp9] done" (breed ends with "[zp9] breed done").
//   lbfill NAME BOX FOOD N     put BOX (a lunchbox id) in NAME's main hand with N of FOOD inside (item handler cap)
//   held NAME                  main-hand item id, count, NBT and the item handler's contents
//   heldeat NAME               NAME eats from the main-hand item as a client use would finish it: finishUsingItem of the
//                              held stack, then ForgeEventFactory.onItemUseFinish (LivingEntity.completeUsingItem)
//   dye NAME DYE               2x2 crafting grid (NAME's inventory menu): held item + DYE; matching recipe id, result, NBT
//   skin NAME SKIN             smithing container: held item + SKIN; matching recipe id, result, NBT
//   breed                      SQ-BREED-01 dump: Animal.isFood of every Animal type against every item, FlowerBlock /
//                              TallFlowerBlock blocks with their #minecraft:flowers membership, and the F-B7 candidates
//                              by class; writes kubejs/exported/breeding_dump.json (one entity type per tick)
//   bstate X Y Z               block state string and block entity id at a position (loaded chunks only)
// MIGRATED 2026-11-15 in part (creative_devtools.md): held -> /cdevtest player, heldeat -> /pbot eat, bstate ->
// /cdevtest state. The rest stays here.
(() => {
    const $P9Args = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $P9Reg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $P9RL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $P9Pos = Java.loadClass('net.minecraft.core.BlockPos')
    const $P9Caps = Java.loadClass('net.minecraftforge.common.capabilities.ForgeCapabilities')
    const $P9Stack = Java.loadClass('net.minecraft.world.item.ItemStack')
    const $P9Hand = Java.loadClass('net.minecraft.world.InteractionHand')
    function out(s) { console.info('[zp9] ' + s) }
    function rl(s) { return new $P9RL(s) }
    function stackOf(id, n) { return new $P9Stack($P9Reg.ITEMS.getValue(rl(id)), n) }
    function itemId(st) { return String($P9Reg.ITEMS.getKey(st.getItem())) }
    function player(server, name) {
        let p = server.getPlayerList().getPlayerByName(name)
        if (p == null) throw 'no player ' + name
        return p
    }
    function contents(st) {
        let h = st.getCapability($P9Caps.ITEM_HANDLER).orElse(null)
        if (h == null) return 'nohandler'
        let parts = []
        for (let i = 0; i < h.getSlots(); i++) {
            let s = h.getStackInSlot(i)
            if (!s.isEmpty()) parts.push(i + ':' + itemId(s) + 'x' + s.getCount())
        }
        return 'slots=' + h.getSlots() + ' [' + parts.join(', ') + ']'
    }
    function describe(st) {
        // KubeJS remaps neither hasTag() nor getTag() on a raw ItemStack (r1115 gotchas): read .nbt
        let t = null
        try { t = st.nbt } catch (err) { }
        return itemId(st) + ' x' + st.getCount() + ' nbt=' + (t == null ? '{}' : String(t))
    }
    function breed(server) {
        const level = server.overworld()
        const $Animal = Java.loadClass('net.minecraft.world.entity.animal.Animal')
        const $Flower = Java.loadClass('net.minecraft.world.level.block.FlowerBlock')
        const $Tall = Java.loadClass('net.minecraft.world.level.block.TallFlowerBlock')
        const $BTags = Java.loadClass('net.minecraft.tags.BlockTags')
        const $ITags = Java.loadClass('net.minecraft.tags.ItemTags')
        // BlockState.is / ItemStack.is overloads are ambiguous to Rhino for a TagKey: name the overload
        // (and the explicit-signature form is not remapped): read the tags through the Forge tag managers
        const bis = (st, tag) => $P9Reg.BLOCKS.tags().getTag(tag).contains(st.getBlock())
        const iis = (st, tag) => $P9Reg.ITEMS.tags().getTag(tag).contains(st.getItem())
        const items = [], types = []
        $P9Reg.ITEMS.getValues().forEach(it => { if (String($P9Reg.ITEMS.getKey(it)) != 'minecraft:air') items.push(it) })
        $P9Reg.ENTITY_TYPES.getValues().forEach(t => types.push(t))
        const dump = { animals: {}, notAnimal: 0, failed: {}, flowers: [], candidates: [] }
        // flowers and candidates first (cheap)
        const cand = /shiny|creeper|crush|firefly|floral|four_leaf|clover|dancing_sunflower/
        $P9Reg.BLOCKS.getValues().forEach(b => {
            let id = String($P9Reg.BLOCKS.getKey(b)), isF = b instanceof $Flower, isT = b instanceof $Tall
            let it = b.asItem(), itId = String($P9Reg.ITEMS.getKey(it))
            let st = b.defaultBlockState()
            let row = {
                block: id, cls: String(b.getClass().getName()), sup: String(b.getClass().getSuperclass().getName()),
                flowerBlock: isF, tallFlowerBlock: isT, blockTag: bis(st, $BTags.FLOWERS),
                smallTag: bis(st, $BTags.SMALL_FLOWERS), tallTag: bis(st, $BTags.TALL_FLOWERS),
                item: itId, itemTag: itId == 'minecraft:air' ? null : iis(new $P9Stack(it), $ITags.FLOWERS)
            }
            if (isF || isT) dump.flowers.push(row)
            if (cand.test(id)) dump.candidates.push(row)
        })
        out('breed flowers=' + dump.flowers.length + ' candidates=' + dump.candidates.length + ' items=' + items.length + ' types=' + types.length)
        let idx = 0
        function step() {
            let budget = 4
            while (idx < types.length && budget > 0) {
                let t = types[idx++], tid = String($P9Reg.ENTITY_TYPES.getKey(t)), e = null
                try { e = t.create(level) } catch (err) { dump.failed[tid] = 'create ' + err; continue }
                if (e == null) continue
                if (!(e instanceof $Animal)) { dump.notAnimal++; try { e.discard() } catch (err) { } continue }
                budget--
                let foods = [], errs = 0
                items.forEach(it => {
                    try { if (e.isFood(new $P9Stack(it))) foods.push(String($P9Reg.ITEMS.getKey(it))) } catch (err) { errs++ }
                })
                dump.animals[tid] = { cls: String(e.getClass().getName()), foods: foods, errors: errs }
                out('breed ' + tid + ' foods=' + foods.length + (errs ? ' errors=' + errs : '') + ' first=' + foods.slice(0, 8).join(','))
                try { e.discard() } catch (err) { }
            }
            if (idx < types.length) return
            // java.nio.file is not allowed by KubeJS's class filter: KubeJS JsonIO writes inside the game dir
            let path = 'kubejs/exported/breeding_dump.json'
            JsonIO.write(path, dump)
            out('breed animals=' + Object.keys(dump.animals).length + ' notAnimal=' + dump.notAnimal + ' failed=' + Object.keys(dump.failed).length + ' wrote ' + path)
            dump.candidates.forEach(r => out('breed cand ' + r.block + ' cls=' + r.cls + ' sup=' + r.sup + ' flower=' + r.flowerBlock + ' tall=' + r.tallFlowerBlock + ' blockTag=' + r.blockTag + ' itemTag=' + r.itemTag))
            out('breed done')
            pending = null
        }
        pending = step
    }
    let pending = null
    function run(server, what, argStr) {
        const a = String(argStr).trim() == '' ? [] : String(argStr).trim().split(/\s+/)
        const level = server.overworld()
        try {
            if (what == 'lbfill') {
                let p = player(server, a[0]), box = stackOf(a[1], 1)
                p.setItemInHand($P9Hand.MAIN_HAND, box)
                let held = p.getMainHandItem(), h = held.getCapability($P9Caps.ITEM_HANDLER).orElse(null)
                if (h == null) { out('lbfill ' + a[1] + ' nohandler'); }
                else {
                    let left = stackOf(a[2], parseInt(a[3] || '1'))
                    for (let i = 0; i < h.getSlots() && !left.isEmpty(); i++) left = h.insertItem(i, left, false)
                    out('lbfill ' + a[0] + ' ' + a[1] + ' ' + contents(held) + ' notInserted=' + left.getCount())
                }
            } else if (what == 'held') {
                let p = player(server, a[0]), held = p.getMainHandItem()
                out('held ' + a[0] + ' ' + describe(held) + ' ' + contents(held))
            } else if (what == 'heldeat') {
                let p = player(server, a[0]), held = p.getMainHandItem(), copy = held.copy()
                let $FEF = Java.loadClass('net.minecraftforge.event.ForgeEventFactory')
                let res = held.finishUsingItem(level, p)
                let fin = $FEF.onItemUseFinish(p, copy, 0, res)
                p.setItemInHand($P9Hand.MAIN_HAND, fin)
                out('heldeat ' + a[0] + ' ' + itemId(copy) + ' -> ' + describe(fin) + ' ' + contents(fin) + ' food=' + p.getFoodData().getFoodLevel())
            } else if (what == 'dye' || what == 'skin') {
                let p = player(server, a[0]), held = p.getMainHandItem().copy(), part = stackOf(a[1], 1)
                let $RT = Java.loadClass('net.minecraft.world.item.crafting.RecipeType'), inv, type
                if (what == 'dye') {
                    inv = p.inventoryMenu.getCraftSlots()
                    for (let i = 0; i < inv.getContainerSize(); i++) inv.setItem(i, $P9Stack.EMPTY)
                    inv.setItem(0, held); inv.setItem(1, part); type = $RT.CRAFTING
                } else {
                    let $SC = Java.loadClass('net.minecraft.world.SimpleContainer')
                    // new SimpleContainer(2) resolves to the ItemStack... varargs constructor in Rhino (size 1)
                    inv = new $SC(held, part); type = $RT.SMITHING
                }
                let r = server.getRecipeManager().getRecipeFor(type, inv, level).orElse(null)
                if (r == null) out(what + ' ' + itemId(held) + ' + ' + a[1] + ' no recipe')
                else out(what + ' ' + itemId(held) + ' + ' + a[1] + ' recipe=' + r.getId() + ' -> ' + describe(r.assemble(inv)))
                if (what == 'dye') for (let i = 0; i < inv.getContainerSize(); i++) inv.setItem(i, $P9Stack.EMPTY)
            } else if (what == 'breed') {
                breed(server)
                return
            } else if (what == 'bstate') {
                let pos = new $P9Pos(parseInt(a[0]), parseInt(a[1]), parseInt(a[2])), st = level.getBlockState(pos), be = level.getBlockEntity(pos)
                out('bstate ' + a.join(' ') + ' ' + String(st) + ' be=' + (be == null ? 'none' : String($P9Reg.BLOCK_ENTITY_TYPES.getKey(be.getType()))))
            } else {
                out('unknown subcommand ' + what)
            }
        } catch (err) {
            out('error ' + what + ' ' + argStr + ': ' + err)
        }
        out('done')
    }
    ServerEvents.tick(e => {
        if (pending == null) return
        try { pending() } catch (err) { out('breed error ' + err); pending = null }
    })
    ServerEvents.commandRegistry(e => {
        const { commands: C } = e
        e.register(C.literal('zp9').requires(s => s.hasPermission(2))
            .then(C.argument('what', $P9Args.word())
                .executes(ctx => { run(ctx.source.server, $P9Args.getString(ctx, 'what'), ''); return 1 })
                .then(C.argument('arg', $P9Args.greedyString())
                    .executes(ctx => {
                        run(ctx.source.server, $P9Args.getString(ctx, 'what'), $P9Args.getString(ctx, 'arg'))
                        return 1
                    }))))
    })
})()
