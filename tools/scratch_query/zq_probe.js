// Scratch-only query command for bulk queue runs (feature_writeup/testing/scratch_queue.md). Copy into a scratch
// server's kubejs/server_scripts as zz_zq_probe.js, never into the pack.
//   /zq recipes <text>       recipe ids containing text, with their result
//   /zq rdetail <text>       same ids with serializer, result and inputs (first 3 items of each)
//   /zq itemtag <tag id>     items in an item tag
//   /zq blocktag <tag id>    blocks in a block tag
//   /zq tagsof <item id>     item tags of an item (and block tags of its block)
//   /zq nbt <item id>        how scripts see an ItemStack's NBT (getTag vs .nbt)
//   /zq states all           block-state id count (Block.BLOCK_STATE_REGISTRY) against the 2^20 ceiling
// Output: "[zq] ..." lines in logs/latest.log, then "[zq] done".
(() => {
    const $ZQArgs = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $ZQReg = Java.loadClass('net.minecraft.core.Registry')
    const $ZQRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $ZQTagKey = Java.loadClass('net.minecraft.tags.TagKey')
    const $ZQCollectors = Java.loadClass('java.util.stream.Collectors')
    const $ZQBlock = Java.loadClass('net.minecraft.world.level.block.Block')
    function out(s) { console.info('[zq] ' + s) }
    function run(server, what, arg) {
        try {
            if (what == 'recipes') {
                let n = 0
                server.getRecipeManager().getRecipes().forEach(r => {
                    let id = String(r.getId())
                    if (id.indexOf(arg) < 0) return
                    n++
                    if (n > 80) return   // print at most 80, count them all
                    let res = ''
                    try { res = String(r.getResultItem()) } catch (err) { res = '?' }
                    out(id + ' -> ' + res)
                })
                out('recipes matching ' + arg + ': ' + n + (n > 80 ? ' (first 80 shown)' : ''))
            } else if (what == 'rdetail') {
                // id contains arg: serializer, result, and each ingredient's first 3 items
                let n = 0
                server.getRecipeManager().getRecipes().forEach(r => {
                    let id = String(r.getId())
                    if (id.indexOf(arg) < 0 || n >= 40) return
                    n++
                    let type = '?', res = '?', ins = []
                    try { type = String($ZQReg.RECIPE_SERIALIZER.getKey(r.getSerializer())) } catch (err) { }
                    try { res = String(r.getResultItem()) } catch (err) { }
                    try {
                        r.getIngredients().forEach(g => {
                            let s = g.getStacks(), ids = []
                            for (let k = 0; k < Math.min(3, s.size()); k++) ids.push(String(s.toList().get(k).getId()))
                            ins.push(ids.join('/') + (s.size() > 3 ? '/+' + (s.size() - 3) : ''))
                        })
                    } catch (err) { ins.push('ingredients: ' + err) }
                    out(id + ' {' + type + '} -> ' + res + ' <- ' + ins.join(', '))
                })
                out('rdetail matching ' + arg + ': ' + n)
            } else if (what == 'itemtag' || what == 'blocktag') {
                let reg = what == 'itemtag' ? $ZQReg.ITEM : $ZQReg.BLOCK
                let key = $ZQTagKey.create(reg.key(), new $ZQRL(arg))
                let ids = []
                reg.getTagOrEmpty(key).forEach(h => ids.push(String(reg.getKey(h.value()))))
                out(what + ' ' + arg + ' (' + ids.length + '): ' + ids.join(' '))
            } else if (what == 'tagsof') {
                let item = $ZQReg.ITEM.get(new $ZQRL(arg))
                let tags = []
                item.builtInRegistryHolder().tags().collect($ZQCollectors.toList()).forEach(t => tags.push(String(t.location())))
                out('item tags of ' + arg + ': ' + tags.join(' '))
                let block = $ZQReg.BLOCK.get(new $ZQRL(arg))
                let btags = []
                block.builtInRegistryHolder().tags().collect($ZQCollectors.toList()).forEach(t => btags.push(String(t.location())))
                out('block tags of ' + arg + ': ' + btags.join(' '))
            } else if (what == 'nbt') {
                // how a script sees an ItemStack's NBT (arg = an Item.of id, NBT from the fixed potion below)
                let st = Item.of(arg, '{Potion:"minecraft:water",CustomPotionColor:1}')
                out('typeof getTag ' + typeof st.getTag + ', typeof getItem ' + typeof st.getItem + ', typeof nbt ' + typeof st.nbt + ', nbt ' + st.nbt)
            } else if (what == 'states') {
                let n = $ZQBlock.BLOCK_STATE_REGISTRY.size()
                out('block states ' + n + ' of 1048576 (headroom ' + (1048576 - n) + ')')
            } else out('unknown query ' + what)
        } catch (err) { out('error: ' + err) }
        out('done')
    }
    ServerEvents.commandRegistry(e => {
        const { commands: C } = e
        e.register(C.literal('zq')
            .then(C.argument('what', $ZQArgs.word())
                .then(C.argument('arg', $ZQArgs.greedyString())
                    .executes(ctx => {
                        run(ctx.source.server, $ZQArgs.getString(ctx, 'what'), $ZQArgs.getString(ctx, 'arg'))
                        return 1
                    }))))
    })
})()
