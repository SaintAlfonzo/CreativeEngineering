// Scratch-only probe for run r1113 chunk 3 (tools/scratch_query/batches/r1113/c3_*.qb: SQ-PG, SQ-IP, SQ-BOT, SQ-FCK and
// neighbours). Copy into a scratch server's kubejs/server_scripts as zz_c3_probe.js, never into the pack. NAME = a perfBot
// bot (`pbot spawn`). Coordinates are integers in the overworld unless a command says otherwise; load the chunk first
// (forceload): nothing here loads chunks.
//   /zc3 aim NAME YAW PITCH           sets the bot's body, head and view rotation (server side; no teleport packet)
//   /zc3 shoot NAME blue|orange       the bot's main-hand Portal Gun fires that side (PortalGunItem.interact, what the
//                                     client's left / right click packet runs on the server); prints the result
//   /zc3 portals                      every portalgun:custom_portal in every level: dim, pos, normal, NBT keys with
//                                     "adjust" in their name (adjustPositionAfterTeleport), owner descriptor, colour
//   /zc3 craft NAME C1|C2|...|C9      a 3x3 crafting grid (a CraftingMenu on the bot's inventory) filled row by row with
//                                     the cells ("-" = empty, "id" or "id{snbt}"); prints the matching recipe id and the
//                                     assembled result with its NBT, or "no recipe"
//   /zc3 pot RECIPE_ID                a Botany Pots crop recipe: growthTicks and every display state (render state at 0)
//   /zc3 bstate X Y Z [X Y Z ...]     the block state at each position
//   /zc3 items X Y Z R [keep]         item entities within R of X Y Z summed by id, then discarded (keep: left in place)
//   /zc3 loot X Y Z TOOL ROLLS        the block at X Y Z rolled through Block.getDrops ROLLS times with TOOL (id or none)
//   /zc3 rest NAME                    the bot's minecraft:time_since_rest stat
// Output: "[c3] ..." lines in logs/latest.log, then "[c3] done". Written 2026-11-13, not run yet: one live call each
// before a batch relies on it (scratch-test gotchas).
(() => {
    const $CArgs = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $CReg = Java.loadClass('net.minecraft.core.Registry')
    const $CRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $CPos = Java.loadClass('net.minecraft.core.BlockPos')
    const $CVec = Java.loadClass('net.minecraft.world.phys.Vec3')
    const $CAABB = Java.loadClass('net.minecraft.world.phys.AABB')
    const $CItemEnt = Java.loadClass('net.minecraft.world.entity.item.ItemEntity')
    const $CHand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $CLootB = Java.loadClass('net.minecraft.world.level.storage.loot.LootContext$Builder')
    const $CLootP = Java.loadClass('net.minecraft.world.level.storage.loot.parameters.LootContextParams')
    const $CCompound = Java.loadClass('net.minecraft.nbt.CompoundTag')
    const $CRecipeType = Java.loadClass('net.minecraft.world.item.crafting.RecipeType')
    const $CCraftMenu = Java.loadClass('net.minecraft.world.inventory.CraftingMenu')
    const $CStats = Java.loadClass('net.minecraft.stats.Stats')
    function out(s) { console.info('[c3] ' + s) }
    function idOf(st) { return String($CReg.ITEM.getKey(st.getItem())) }
    function player(server, name) {
        let p = server.getPlayerList().getPlayerByName(name)
        if (p == null) out('no player ' + name)
        return p
    }
    function stackOf(cell) {
        // "id" or "id{snbt}"
        let i = cell.indexOf('{')
        if (i < 0) return Item.of(cell, 1)
        return Item.of(cell.substring(0, i), 1, cell.substring(i))
    }
    function run(server, line) {
        try {
            let a = String(line).trim().split(/\s+/), what = a[0], lv = server.overworld()
            if (what == 'aim') {
                let p = player(server, a[1]); if (p == null) return
                let yaw = parseFloat(a[2]), pitch = parseFloat(a[3])
                p.setYRot(yaw); p.setXRot(pitch); p.setYHeadRot(yaw); p.setYBodyRot(yaw)
                let v = p.getViewVector(1.0)
                out('aim ' + a[1] + ' yaw ' + yaw + ' pitch ' + pitch + ' view ' + v.x().toFixed(2) + ' ' + v.y().toFixed(2) + ' ' + v.z().toFixed(2)
                    + ' eye ' + p.getEyePosition().toString())
            } else if (what == 'shoot') {
                let p = player(server, a[1]); if (p == null) return
                let $Side = Java.loadClass('tk.meowmc.portalgun.PortalGunRecord$PortalGunSide')
                let side = a[2] == 'orange' ? $Side.orange : $Side.blue
                let st = p.getMainHandItem()
                let r = st.getItem().interact(p, $CHand.MAIN_HAND, side)
                out('shoot ' + a[1] + ' ' + a[2] + ' with ' + idOf(st) + ': ' + r)
            } else if (what == 'portals') {
                let n = 0
                server.getAllLevels().forEach(level => {
                    level.getAllEntities().forEach(e => {
                        let type = String($CReg.ENTITY_TYPE.getKey(e.getType()))
                        if (type != 'portalgun:custom_portal') return
                        n++
                        let t = new $CCompound(), keys = []
                        e.saveWithoutId(t)
                        t.getAllKeys().forEach(k => { if (String(k).toLowerCase().indexOf('adjust') >= 0) keys.push(k + '=' + t.get(k)) })
                        let normal = '?'
                        try { normal = String(e.getNormal()) } catch (err) { }
                        let desc = t.contains('descriptor') ? String(t.get('descriptor')) : '-'
                        out('portal ' + String(level.dimension().location()) + ' ' + Math.floor(e.getX()) + ' ' + Math.floor(e.getY()) + ' '
                            + Math.floor(e.getZ()) + ' normal ' + normal + ' ' + (keys.length ? keys.join(' ') : 'no adjust key') + ' descriptor ' + desc)
                    })
                })
                out('portals: ' + n)
            } else if (what == 'craft') {
                let p = player(server, a[1]); if (p == null) return
                let cells = a.slice(2).join(' ').split('|').map(s => s.trim())
                let menu = new $CCraftMenu(0, p.getInventory())
                let grid = menu.getSlot(1).container
                for (let k = 0; k < 9; k++) {
                    let c = cells[k] || '-'
                    grid.setItem(k, c == '-' ? Item.of('minecraft:air') : stackOf(c))
                }
                let opt = server.getRecipeManager().getRecipeFor($CRecipeType.CRAFTING, grid, lv)
                if (!opt.isPresent()) { out('craft: no recipe'); return }
                let r = opt.get(), res = r.assemble(grid)
                out('craft: recipe ' + String(r.getId()) + ' -> ' + res.getCount() + ' ' + idOf(res) + ' nbt ' + (res.nbt == null ? '{}' : String(res.nbt)))
            } else if (what == 'pot') {
                let opt = server.getRecipeManager().byKey(new $CRL(a[1]))
                if (!opt.isPresent()) { out('pot ' + a[1] + ': no recipe'); return }
                let r = opt.get(), gt = '?', ds = []
                try { gt = r.getGrowthTicks() } catch (err) { gt = 'error ' + err }
                try {
                    r.getDisplayState(null, null, null).forEach(d => {
                        let s = String(d)
                        try { s = String(d.getRenderState(0)) } catch (err) { }
                        ds.push(s)
                    })
                } catch (err) { ds.push('error ' + err) }
                out('pot ' + a[1] + ': growthTicks ' + gt + ' display [' + ds.join('; ') + ']')
            } else if (what == 'bstate') {
                for (let k = 1; k + 2 < a.length; k += 3) {
                    let pos = new $CPos(parseInt(a[k]), parseInt(a[k + 1]), parseInt(a[k + 2]))
                    out('bstate ' + a[k] + ' ' + a[k + 1] + ' ' + a[k + 2] + ' ' + String(lv.getBlockState(pos)))
                }
            } else if (what == 'items') {
                let x = parseInt(a[1]), y = parseInt(a[2]), z = parseInt(a[3]), r = parseInt(a[4]), keep = a[5] == 'keep'
                let box = new $CAABB(x - r, y - r, z - r, x + r + 1, y + r + 1, z + r + 1), sum = {}
                lv.getEntitiesOfClass($CItemEnt, box).forEach(e => {
                    let st = e.getItem(), id = idOf(st)
                    sum[id] = (sum[id] || 0) + st.getCount()
                    if (!keep) e.discard()
                })
                let keys = Object.keys(sum).sort()
                out('items ' + x + ' ' + y + ' ' + z + ': ' + (keys.length ? keys.map(k => k + ' x' + sum[k]).join(', ') : 'none'))
            } else if (what == 'loot') {
                let x = parseInt(a[1]), y = parseInt(a[2]), z = parseInt(a[3]), rolls = parseInt(a[5]), sum = {}
                let pos = new $CPos(x, y, z), st = lv.getBlockState(pos)
                let tool = a[4] == 'none' ? Item.of('minecraft:air') : Item.of(a[4])
                for (let k = 0; k < rolls; k++) {
                    let b = new $CLootB(lv).withRandom(lv.random).withParameter($CLootP.ORIGIN, new $CVec(x + 0.5, y + 0.5, z + 0.5)).withParameter($CLootP.TOOL, tool)
                    st.getDrops(b).forEach(s => { let id = idOf(s); sum[id] = (sum[id] || 0) + s.getCount() })
                }
                let keys = Object.keys(sum).sort()
                out('loot ' + String(st) + ' tool ' + a[4] + ' rolls ' + rolls + ': ' + (keys.length ? keys.map(k => k + ' x' + sum[k]).join(', ') : 'none'))
            } else if (what == 'rest') {
                let p = player(server, a[1]); if (p == null) return
                out('rest ' + a[1] + ': ' + p.getStats().getValue($CStats.CUSTOM.get($CStats.TIME_SINCE_REST)))
            } else out('unknown ' + what)
        } catch (err) { out('error: ' + err) }
        out('done')
    }
    ServerEvents.commandRegistry(e => {
        const { commands: C } = e
        e.register(C.literal('zc3').then(C.argument('line', $CArgs.greedyString()).executes(ctx => {
            run(ctx.source.server, $CArgs.getString(ctx, 'line'))
            return 1
        })))
    })
})()
