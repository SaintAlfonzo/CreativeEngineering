// Scratch-only probe for run r1102b slice 5 (tools/scratch_query/batches/r1102b/s5_*.qb). Copy into a scratch server's
// kubejs/server_scripts as zz_s5_probe.js, never into the pack. Output: "[s5] ..." lines in logs/latest.log, then "[s5] done".
//   /s5 sneak <player> on|off          setShiftKeyDown on a bot (perfBot `pbot use` then runs as a sneaking use)
//   /s5 lclick <player> x y z          posts Forge's LeftClickBlock for the bot at the block (top face)
//   /s5 break <player> x y z           the bot breaks the block (ServerPlayerGameMode.destroyBlock, held tool)
//   /s5 eat <player> <item>            the bot eats one item in the main hand: finishUsingItem + onItemUseFinish
//                                      (the order LivingEntity.completeUsingItem uses); logs food level and glass bottles
//   /s5 rest <player> <ticks>          sets the bot's time-since-rest stat (phantom spawning)
//   /s5 trim <item>                    Armor Trims TrimmableItem.applyTrim(item, coast, gold ingot): trimmed or empty
//   /s5 remainder <item>               crafting remainder of one item
//   /s5 spout <fluid> <mb> <item>      Create spout logic: can the item be filled, mB needed, what it becomes
//   /s5 drain <item>                   Create item drain logic: fluid and amount out, item left
//   /s5 containers <text>              farmersdelight:cooking recipes with a result id containing text: serving container
//   /s5 selfloop <item>                recipes that make the item and also take it
//   /s5 fluidtag <tag>                 fluids in a fluid tag
//   /s5 potrecipe                      farmersdelight:cooking recipes served in a bowl with an ingredient that leaves a
//                                      bowl; prints Copper Pot NBT for 1 and for 3 meals (SQ-RB-21)
//   /s5 jarrecipe                      vintagedelight:fermenting recipes with a container whose ingredient leaves that
//                                      container; prints Pickling Jar NBT for 3 batches + 1 container (SQ-RB-22)
(() => {
    const $S5Args = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $S5Reg = Java.loadClass('net.minecraft.core.Registry')
    const $S5RL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $S5Stack = Java.loadClass('net.minecraft.world.item.ItemStack')
    const $S5Pos = Java.loadClass('net.minecraft.core.BlockPos')
    const $S5Dir = Java.loadClass('net.minecraft.core.Direction')
    const $S5Hand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $S5Hooks = Java.loadClass('net.minecraftforge.common.ForgeHooks')
    const $S5Events = Java.loadClass('net.minecraftforge.event.ForgeEventFactory')
    const $S5Stats = Java.loadClass('net.minecraft.stats.Stats')
    const $S5TagKey = Java.loadClass('net.minecraft.tags.TagKey')
    function out(s) { console.info('[s5] ' + s) }
    function item(id) { return $S5Reg.ITEM.get(new $S5RL(id)) }
    function sid(st) { return String($S5Reg.ITEM.getKey(st.getItem())) }
    function bottles(p) {
        let n = 0, inv = p.getInventory()
        for (let k = 0; k < inv.getContainerSize(); k++) if (sid(inv.getItem(k)) == 'minecraft:glass_bottle') n += inv.getItem(k).getCount()
        return n
    }
    // an Ingredient's stacks as a JS array: Ingredient.getItems() is hidden in this Rhino build, getStacks() is
    // KubeJS's ItemStackSet (zq_probe.js zqIng)
    function ingStacks(g) {
        let s = g.getStacks().toList(), o = []
        for (let k = 0; k < s.size(); k++) o.push(s.get(k))
        return o
    }
    // SNBT for one stack in a slot
    function slot(k, st, n) { return '{Slot:' + k + 'b,id:"' + sid(st) + '",Count:' + n + 'b}' }
    function run(server, a) {
        let what = a[0]
        try {
            let p = (what == 'sneak' || what == 'lclick' || what == 'break' || what == 'eat' || what == 'rest')
                ? server.getPlayerList().getPlayerByName(a[1]) : null
            if (p == null && (what == 'sneak' || what == 'lclick' || what == 'break' || what == 'eat' || what == 'rest')) {
                out('no player ' + a[1])
            } else if (what == 'sneak') {
                p.setShiftKeyDown(a[2] == 'on')
                out('sneak ' + a[1] + ' ' + p.isShiftKeyDown())
            } else if (what == 'lclick') {
                let pos = new $S5Pos(+a[2], +a[3], +a[4])
                let ev = $S5Hooks.onLeftClickBlock(p, pos, $S5Dir.UP)
                out('lclick ' + a[1] + ' ' + a[2] + ' ' + a[3] + ' ' + a[4] + ': canceled ' + ev.isCanceled() + ', block now '
                    + String($S5Reg.BLOCK.getKey(p.getLevel().getBlockState(pos).getBlock())) + ' ' + p.getLevel().getBlockState(pos))
            } else if (what == 'break') {
                let pos = new $S5Pos(+a[2], +a[3], +a[4])
                let r = p.gameMode.destroyBlock(pos)
                out('break ' + a[1] + ' with ' + sid(p.getMainHandItem()) + ': ' + r + ', block now ' + p.getLevel().getBlockState(pos))
            } else if (what == 'eat') {
                let st = new $S5Stack(item(a[2]), 1)
                p.setItemInHand($S5Hand.MAIN_HAND, st)
                let food0 = p.getFoodData().getFoodLevel(), b0 = bottles(p)
                p.getFoodData().setFoodLevel(10)
                let used = st.copy()
                let left = st.finishUsingItem(p.getLevel(), p)
                left = $S5Events.onItemUseFinish(p, used, 32, left)
                out('eat ' + a[2] + ': food 10 -> ' + p.getFoodData().getFoodLevel() + ', hand ' + sid(left) + ' x' + left.getCount()
                    + ', glass bottles in inventory ' + b0 + ' -> ' + bottles(p) + ' (food before probe ' + food0 + ')')
                p.setItemInHand($S5Hand.MAIN_HAND, $S5Stack.EMPTY)
            } else if (what == 'rest') {
                p.getStats().setValue(p, $S5Stats.CUSTOM.get($S5Stats.TIME_SINCE_REST), +a[2])
                out('rest ' + a[1] + ' ' + p.getStats().getValue($S5Stats.CUSTOM.get($S5Stats.TIME_SINCE_REST)))
            } else if (what == 'trim') {
                let $S5Trim = Java.loadClass('gg.hipposgrumm.armor_trims.trimming.TrimmableItem')
                let $S5Trims = Java.loadClass('gg.hipposgrumm.armor_trims.trimming.Trims')
                let r = $S5Trim.applyTrim(new $S5Stack(item(a[1]), 1), new $S5Trims(new $S5RL('armor_trims', 'coast')),
                    new $S5Stack(item('minecraft:gold_ingot'), 1))
                out('trim ' + a[1] + ': ' + (r.isEmpty() ? 'empty' : 'trimmed ' + sid(r) + ' ' + r.nbt))
            } else if (what == 'remainder') {
                let r = new $S5Stack(item(a[1]), 1).getCraftingRemainingItem()
                out('remainder ' + a[1] + ': ' + (r.isEmpty() ? 'none' : sid(r) + ' x' + r.getCount()))
            } else if (what == 'spout') {
                // Create's spout logic (recipes create:filling + generic fills): s5 spout <fluid> <mb> <item>
                let $S5Fill = Java.loadClass('com.simibubi.create.content.fluids.spout.FillingBySpout')
                let $S5FS = Java.loadClass('net.minecraftforge.fluids.FluidStack')
                let level = server.overworld(), st = new $S5Stack(item(a[3]), 1)
                // new FluidStack(fluid, mb) is ambiguous for an Architectury fluid (Fluid,int vs FluidStack,int):
                // build it from NBT instead
                let $S5CT = Java.loadClass('net.minecraft.nbt.CompoundTag')
                let ft = new $S5CT()
                ft.putString('FluidName', a[1])
                ft.putInt('Amount', +a[2])
                let fs = $S5FS.loadFluidStackFromNBT(ft)
                if (fs.isEmpty()) { out('spout ' + a[1] + ': no such fluid'); out('done'); return }
                let can = $S5Fill.canItemBeFilled(level, st), need = $S5Fill.getRequiredAmountForItem(level, st, fs.copy())
                let res = need > 0 ? $S5Fill.fillItem(level, need, st.copy(), fs.copy()) : $S5Stack.EMPTY
                out('spout ' + a[1] + ' ' + a[2] + ' mB into ' + a[3] + ': can ' + can + ', needs ' + need + ' mB, gives '
                    + (res.isEmpty() ? 'nothing' : sid(res) + ' x' + res.getCount()))
            } else if (what == 'drain') {
                // Create's item drain logic: s5 drain <item>
                let $S5Empty = Java.loadClass('com.simibubi.create.content.fluids.transfer.GenericItemEmptying')
                let level = server.overworld(), st = new $S5Stack(item(a[1]), 1)
                let can = $S5Empty.canItemBeEmptied(level, st)
                let pr = can ? $S5Empty.emptyItem(level, st.copy(), true) : null
                out('drain ' + a[1] + ': can ' + can + (pr ? ', fluid ' + String($S5Reg.FLUID.getKey(pr.getFirst().getFluid()))
                    + ' ' + pr.getFirst().getAmount() + ' mB, leaves ' + sid(pr.getSecond()) : ''))
            } else if (what == 'containers') {
                // farmersdelight:cooking recipes whose result id contains the text: their serving container
                let n = 0
                // by type, never r.getType() on every recipe: an addon recipe with an abstract getType crashed the
                // server tick (create_mechanical_extruder ExtrudingRecipe, r1102b 19:03)
                server.getRecipeManager().getAllRecipesFor($S5Reg.RECIPE_TYPE.get(new $S5RL('farmersdelight:cooking'))).forEach(r => {
                    let res = sid(r.getResultItem())
                    if (res.indexOf(a[1]) < 0) return
                    n++
                    out('container ' + String(r.getId()) + ' -> ' + res + ' in ' + sid(r.getOutputContainer()))
                })
                out('containers ' + a[1] + ': ' + n)
            } else if (what == 'selfloop') {
                // recipes whose result is the item and that also take the item. Walks the recipe types and asks
                // getAllRecipesFor per type (never r.getType() on every recipe: java.lang.Error escapes try/catch);
                // skips the Mechanical Extruder addon whose recipes have an abstract getType
                let n = 0, types = 0
                $S5Reg.RECIPE_TYPE.keySet().forEach(rl => {
                    if (String(rl.getNamespace()).indexOf('extruder') >= 0) return
                    types++
                    server.getRecipeManager().getAllRecipesFor($S5Reg.RECIPE_TYPE.get(rl)).forEach(r => {
                        let res = ''
                        try { res = sid(r.getResultItem()) } catch (err) { return }
                        if (res != a[1]) return
                        let hit = false
                        r.getIngredients().forEach(g => { ingStacks(g).forEach(st => { if (sid(st) == a[1]) hit = true }) })
                        if (hit) { n++; out('selfloop ' + String(r.getId())) }
                    })
                })
                out('selfloop types walked ' + types)
                out('selfloop ' + a[1] + ': ' + n)
            } else if (what == 'fluidtag') {
                let key = $S5TagKey.create($S5Reg.FLUID_REGISTRY, new $S5RL(a[1])), ids = []
                $S5Reg.FLUID.getTagOrEmpty(key).forEach(h => ids.push(String($S5Reg.FLUID.getKey(h.value()))))
                out('fluidtag ' + a[1] + ' (' + ids.length + '): ' + ids.join(' '))
            } else if (what == 'potrecipe' || what == 'jarrecipe') {
                let pot = what == 'potrecipe', n = 0
                server.getRecipeManager().getAllRecipesFor($S5Reg.RECIPE_TYPE.get(new $S5RL(pot ? 'farmersdelight:cooking' : 'vintagedelight:fermenting'))).forEach(r => {
                    let cont = pot ? r.getOutputContainer() : (r.hasContainer() ? r.getContainerItemStack() : $S5Stack.EMPTY)
                    if (cont.isEmpty() || (pot && sid(cont) != 'minecraft:bowl')) return
                    let ings = r.getIngredients(), stacks = [], hit = false
                    for (let k = 0; k < ings.size(); k++) {
                        let its = ingStacks(ings.get(k))
                        if (its.length == 0) return
                        let st = its[0]
                        for (let j = 0; j < its.length; j++) {
                            let rem = its[j].getCraftingRemainingItem()
                            if (!rem.isEmpty() && sid(rem) == sid(cont)) { st = its[j]; hit = true }
                        }
                        stacks.push(st)
                    }
                    if (!hit || stacks.length > (pot ? 4 : 6)) return
                    n++
                    if (n > 3) return
                    let id = String(r.getId()), parts1 = [], parts3 = []
                    stacks.forEach((st, k) => { parts1.push(slot(k, st, 1)); parts3.push(slot(k, st, 3)) })
                    if (pot) {
                        out('potrecipe ' + id + ' cook ' + r.getCookTime() + ' -> ' + sid(r.getResultItem()))
                        out('potnbt1 ' + id + ' {Inventory:{Size:8,Items:[' + parts1.join(',') + ']}}')
                        out('potnbt3 ' + id + ' {Inventory:{Size:8,Items:[' + parts3.concat([slot(5, cont, 1)]).join(',') + ']}}')
                    } else {
                        out('jarrecipe ' + id + ' time ' + r.getProcessingTime() + ' -> ' + sid(r.getResultItem()) + ', container ' + sid(cont))
                        out('jarnbt3 ' + id + ' {inventory:{Size:10,Items:[' + parts3.concat([slot(6, cont, 1)]).join(',') + ']}}')
                    }
                })
                out(what + ': ' + n + ' found')
            } else out('unknown query ' + what)
        } catch (err) { out('error: ' + err) }
        out('done')
    }
    ServerEvents.commandRegistry(e => {
        const { commands: C } = e
        e.register(C.literal('s5')
            .then(C.argument('args', $S5Args.greedyString())
                .executes(ctx => {
                    run(ctx.source.server, String($S5Args.getString(ctx, 'args')).split(' '))
                    return 1
                })))
    })
})()
