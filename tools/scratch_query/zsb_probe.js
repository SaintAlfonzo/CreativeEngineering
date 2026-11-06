// Scratch-only probe for run r1103 slice B (tools/scratch_query/batches/r1103/sB_*.qb). Copy into a scratch server's
// kubejs/server_scripts as zz_zsb_probe.js, never into the pack. Output: "[zsb] ..." lines in logs/latest.log, then
// "[zsb] done".
//   /zsb cans                    every recipe with an ingredient that accepts a pack can (#creativeengineering:cans or a
//                                creativeengineering:canned_* item): "can <serializer> <id> -> <result>"; "VIOLATION ..."
//                                for a crafting recipe whose id does not end _manual_only, or a create:mixing /
//                                create:mechanical_crafting / immersiveengineering:* / ae2:* recipe taking a can whose
//                                result is not a can (a canning route makes a can); then the totals (SQ-CNP-01)
//   /zsb ns <namespace>          registry items in the namespace (count) and the ids among them containing "cup"
//   /zsb potnbt <result item>    the first farmersdelight:cooking recipe making the item: cook time, serving container,
//                                and Copper Pot NBT "potnbt <item> {Inventory:{...}}" with one of each ingredient in
//                                slots 0.. and one serving container in slot 5 (SQ-CUP-03)
//   /zsb rem <recipe id>         per ingredient: the first stack and its crafting remainder
//   /zsb remitem <item>          crafting remainder of one item
//   /zsb items x1 y1 z1 x2 y2 z2 item entities in a loaded box summed by item id: "items <id>=N, ..."
// Rules followed (gotchas.md): no recipe.getType() over every recipe (getSerializer only), Ingredient.getStacks(),
// no chunk loads (forceload the boxes first).
(() => {
    const $ZSBArgs = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $ZSBReg = Java.loadClass('net.minecraft.core.Registry')
    const $ZSBRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $ZSBTagKey = Java.loadClass('net.minecraft.tags.TagKey')
    const $ZSBAABB = Java.loadClass('net.minecraft.world.phys.AABB')
    const $ZSBItemEntity = Java.loadClass('net.minecraft.world.entity.item.ItemEntity')
    function out(s) { console.info('[zsb] ' + s) }
    function sid(st) { return String($ZSBReg.ITEM.getKey(st.getItem())) }
    function stacksOf(g) {
        let s = g.getStacks().toList(), o = []
        for (let k = 0; k < s.size(); k++) o.push(s.get(k))
        return o
    }
    function serOf(r) {
        try { return String($ZSBReg.RECIPE_SERIALIZER.getKey(r.getSerializer())) } catch (err) { return '?' }
    }
    function resOf(r) {
        try { let st = r.getResultItem(); return st.isEmpty() ? '' : sid(st) } catch (err) { return '?' }
    }
    function packCans() {
        let set = {}
        let key = $ZSBTagKey.create($ZSBReg.ITEM_REGISTRY, new $ZSBRL('creativeengineering:cans'))
        $ZSBReg.ITEM.getTagOrEmpty(key).forEach(h => { set[String($ZSBReg.ITEM.getKey(h.value()))] = true })
        $ZSBReg.ITEM.keySet().forEach(rl => {
            let id = String(rl)
            if (id.indexOf('creativeengineering:canned_') == 0) set[id] = true
        })
        return set
    }
    function run(server, a) {
        let what = a[0]
        try {
            if (what == 'cans') {
                let cans = packCans(), n = 0, bad = 0, bySer = {}
                out('cans: ' + Object.keys(cans).length + ' pack can ids')
                server.getRecipeManager().getRecipes().forEach(r => {
                    let takes = null
                    try {
                        r.getIngredients().forEach(g => { stacksOf(g).forEach(st => { if (takes == null && cans[sid(st)]) takes = sid(st) }) })
                    } catch (err) { return }
                    if (takes == null) return
                    n++
                    let ser = serOf(r), id = String(r.getId()), res = resOf(r)
                    bySer[ser] = (bySer[ser] || 0) + 1
                    let crafting = /crafting|shaped|shapeless/.test(ser) && !/mechanical_crafting/.test(ser)
                    let auto = /^create:(mixing|mechanical_crafting)$/.test(ser) || /^(immersiveengineering|ae2):/.test(ser)
                    let why = null
                    if (crafting && !/_manual_only$/.test(id)) why = 'crafting id without _manual_only'
                    if (auto && !cans[res]) why = 'automation recipe takes a can'
                    if (why) { bad++; out('VIOLATION ' + why + ': ' + ser + ' ' + id + ' -> ' + res + ' (takes ' + takes + ')') }
                    else if (n <= 60 || auto) out('can ' + ser + ' ' + id + ' -> ' + res + ' (takes ' + takes + ')')
                })
                out('cans by serializer: ' + Object.keys(bySer).map(k => k + '=' + bySer[k]).join(', '))
                out('cans: ' + n + ' recipes take a pack can, ' + bad + ' violations')
            } else if (what == 'ns') {
                let n = 0, cups = []
                $ZSBReg.ITEM.keySet().forEach(rl => {
                    if (String(rl.getNamespace()) != a[1]) return
                    n++
                    if (String(rl.getPath()).indexOf('cup') >= 0) cups.push(String(rl))
                })
                out('ns ' + a[1] + ': ' + n + ' items, cup ids (' + cups.length + '): ' + cups.join(' '))
            } else if (what == 'potnbt') {
                let found = false
                server.getRecipeManager().getAllRecipesFor($ZSBReg.RECIPE_TYPE.get(new $ZSBRL('farmersdelight:cooking'))).forEach(r => {
                    if (found || resOf(r) != a[1]) return
                    found = true
                    let parts = [], k = 0
                    r.getIngredients().forEach(g => {
                        let its = stacksOf(g)
                        if (its.length > 0) parts.push('{Slot:' + (k++) + 'b,id:"' + sid(its[0]) + '",Count:1b}')
                    })
                    let cont = r.getOutputContainer()
                    if (!cont.isEmpty()) parts.push('{Slot:5b,id:"' + sid(cont) + '",Count:1b}')
                    out('potrecipe ' + String(r.getId()) + ' cook ' + r.getCookTime() + ' -> ' + r.getResultItem().getCount() + ' ' + a[1] + ', container ' + (cont.isEmpty() ? 'none' : sid(cont)) + ', ingredients ' + k)
                    out('potnbt ' + a[1] + ' {Inventory:{Size:8,Items:[' + parts.join(',') + ']}}')
                })
                if (!found) out('potnbt ' + a[1] + ': no farmersdelight:cooking recipe')
            } else if (what == 'rem') {
                let r = server.getRecipeManager().byKey(new $ZSBRL(a[1]))
                if (!r.isPresent()) { out('rem ' + a[1] + ': no such recipe') } else {
                    r = r.get()
                    out('rem ' + a[1] + ' {' + serOf(r) + '} -> ' + resOf(r))
                    let k = 0
                    r.getIngredients().forEach(g => {
                        let its = stacksOf(g)
                        let line = its.slice(0, 4).map(st => { let rm = st.getCraftingRemainingItem(); return sid(st) + ' => ' + (rm.isEmpty() ? 'nothing' : sid(rm)) })
                        out('rem slot ' + (k++) + ': ' + line.join(' | ') + (its.length > 4 ? ' | +' + (its.length - 4) : ''))
                    })
                }
            } else if (what == 'remitem') {
                let st = Item.of(a[1])
                let rm = st.getCraftingRemainingItem()
                out('remitem ' + a[1] + ' => ' + (rm.isEmpty() ? 'nothing' : sid(rm)))
            } else if (what == 'items') {
                let c = a.slice(1, 7).map(Number), sums = {}
                // server.overworld(): KubeJS makes level.dimension a property (a ResourceLocation), so the old
                // l.dimension.location() threw "Cannot find function location" (r1103 sB, SQ-CN-04b)
                let level = server.overworld()
                let box = new $ZSBAABB(Math.min(c[0], c[3]), Math.min(c[1], c[4]), Math.min(c[2], c[5]),
                    Math.max(c[0], c[3]) + 1, Math.max(c[1], c[4]) + 1, Math.max(c[2], c[5]) + 1)
                let list = level.getEntitiesOfClass($ZSBItemEntity, box)
                for (let i = 0; i < list.size(); i++) {
                    let st = list.get(i).getItem()
                    sums[sid(st)] = (sums[sid(st)] || 0) + st.getCount()
                }
                out('items ' + (Object.keys(sums).map(k => k + '=' + sums[k]).join(', ') || 'none'))
            } else out('unknown query ' + what)
        } catch (err) { out('error: ' + err + (err.javaException ? ' / ' + err.javaException : '')) }
        out('done')
    }
    ServerEvents.commandRegistry(e => {
        const { commands: C } = e
        e.register(C.literal('zsb').requires(s => s.hasPermission(2))
            .then(C.argument('args', $ZSBArgs.greedyString())
                .executes(ctx => {
                    run(ctx.source.server, String($ZSBArgs.getString(ctx, 'args')).trim().split(/\s+/))
                    return 1
                })))
    })
})()
