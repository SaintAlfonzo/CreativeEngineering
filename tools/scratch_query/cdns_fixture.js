// Scratch-only: builds the old-id fixture world for the Creative Delight namespace move (plan_cd_namespace.md 5.1).
// Deployed by tools/scratch_query/cdns_fixture.sh into a scratch server that runs the OLD jars (NOSWAP=1), never the pack.
// Commands: /cdnsfix blocks|vanilla|allids|entities|bots|pack|pack2|manifest
// Log lines: "[cdnsfix] OK|MISSING|CMDFAIL ..." in logs/latest.log (console.info goes to logs/kubejs/server.log too).
(() => {
    const $CFReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $CFRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $CFPos = Java.loadClass('net.minecraft.core.BlockPos')
    const $CFCaps = Java.loadClass('net.minecraftforge.common.capabilities.ForgeCapabilities')

    const NS = 'creativeengineering'
    const KINDS = ['canned_corn', 'culture_beef', 'cow_sample_syringe', 'oven', 'sourdough_block', 'cornmeal', 'wooden_water_bucket']
    const BLOCKS = ['oven', 'powdered_sugar_block',
        'beef_culture_block', 'cod_culture_block', 'mutton_culture_block', 'pork_culture_block', 'poultry_culture_block',
        'rabbit_culture_block', 'salmon_culture_block',
        'cookie_dough_block', 'cucumber_dough_block', 'gingerdough_block', 'oat_dough_block', 'sourdough_block', 'sweet_potato_dough_block',
        'placed_cauliflower_kuku', 'placed_corn_popsicle', 'placed_dandelion_and_eggs', 'placed_diced_potatoes_with_egg_and_tomato',
        'placed_inky_cap_scrambled_eggs', 'placed_omelette', 'placed_shakshouka', 'placed_shulker_omelette',
        'placed_suspicious_bacon_and_eggs', 'placed_sweet_potato_pancakes', 'placed_vegetable_omelet']
    const Y = -60
    const ROW = { blocks: 0, vanilla: 4, allids: 8, entities: 12, pack: 16, bots: 20 }
    const BOT_ON = 'cdnsOn'
    const BOT_OFF = 'cdnsOff'

    if (!global.cdnsManifest) global.cdnsManifest = { holders: [], missing: [], fails: 0 }
    const M = global.cdnsManifest

    function id(p) { return NS + ':' + p }
    function say(kind, what) { console.info('[cdnsfix] ' + kind + ' ' + what) }
    function hold(name, x, z, note) { M.holders.push({ name: name, pos: [x, Y, z], note: note || '' }) }
    function missing(name, why) { M.missing.push({ name: name, why: String(why) }); say('MISSING', name + ': ' + why) }
    function rl(s) { return new $CFRL(s) }
    function hasItem(s) { return $CFReg.ITEMS.containsKey(rl(s)) }
    function hasBlock(s) { return $CFReg.BLOCKS.containsKey(rl(s)) }
    function pos(x, z) { return new $CFPos(x, Y, z) }
    function run(server, cmd) {
        let r = 0
        try {
            r = server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), cmd)
        } catch (e) { say('CMDFAIL', cmd.substring(0, 140) + ' -> ' + e); M.fails++; return 0 }
        if (r < 1) { say('CMDFAIL', cmd.substring(0, 180)); M.fails++ }
        return r
    }
    function snbt(p, slot) { return '{' + (slot == null ? '' : 'Slot:' + slot + 'b,') + 'id:"' + id(p) + '",Count:1b}' }
    function items(list) { let o = []; for (let i = 0; i < list.length; i++) o.push(snbt(list[i], i)); return '[' + o.join(',') + ']' }
    function chunks(list, n) { let o = []; for (let i = 0; i < list.length; i += n) o.push(list.slice(i, i + n)); return o }
    function setb(level, x, z, s) { level.setBlockAndUpdate(pos(x, z), $CFReg.BLOCKS.getValue(rl(s)).defaultBlockState()) }
    function stack(p) { return Item.of(id(p)) }
    function cap(o) { return o.getCapability($CFCaps.ITEM_HANDLER).orElse(null) }
    // Fills an item handler with one of each kind; returns how many went in.
    function fill(h, list) {
        let n = 0
        for (let i = 0; i < list.length; i++) {
            let st = stack(list[i])
            for (let s = 0; s < h.getSlots(); s++) {
                if (h.getStackInSlot(s).isEmpty() && h.insertItem(s, st, false).isEmpty()) { n++; break }
            }
        }
        return n
    }
    // One step of the pack row: an absent mod or a changed API is recorded, it does not stop the other holders.
    function attempt(name, fn) {
        try { let d = fn(); say('OK', name + (d == null ? '' : ': ' + d)) } catch (e) { missing(name, e) }
    }

    function blocks(server, level) {
        run(server, 'forceload add 0 0 127 127')
        let x = 0
        BLOCKS.forEach(b => {
            let full = id(b)
            if (!hasBlock(full)) { missing('block ' + b, 'not registered on these jars'); return }
            if (b == 'oven') {
                run(server, 'setblock ' + x + ' ' + Y + ' 0 ' + full + '{Inventory:{Size:10,Items:[' +
                    '{Slot:0,id:"' + id('cornmeal') + '",Count:1b},{Slot:1,id:"' + id('frosting') + '",Count:1b},' +
                    '{Slot:6,id:"' + id('baking_sheet') + '",Count:1b},{Slot:9,id:"' + id('canned_corn') + '",Count:1b}]}}')
            } else {
                setb(level, x, ROW.blocks, full)
            }
            hold('block ' + b, x, ROW.blocks)
            x += 2
        })
    }

    function vanilla(server, level) {
        let z = ROW.vanilla
        let x = 0
        let place = (block, size, name) => chunks(KINDS, size).forEach(part => {
            run(server, 'setblock ' + x + ' ' + Y + ' ' + z + ' ' + block + '{Items:' + items(part) + '}')
            hold(name, x, z, part.join(' '))
            x += 2
        })
        place('minecraft:chest', 27, 'chest')
        place('minecraft:barrel', 27, 'barrel')
        place('minecraft:shulker_box', 27, 'shulker box')
        place('minecraft:hopper', 5, 'hopper')
        place('minecraft:dropper', 9, 'dropper')
        place('minecraft:furnace', 3, 'furnace')
        place('minecraft:brewing_stand', 5, 'brewing stand')
        let inner = '{Slot:0b,id:"minecraft:shulker_box",Count:1b,tag:{BlockEntityTag:{Items:' + items(KINDS) + '}}}'
        run(server, 'setblock ' + x + ' ' + Y + ' ' + z + ' minecraft:chest{Items:[' + inner + ']}')
        hold('shulker box item in chest', x, z)
        x += 2
        setb(level, x, z, 'minecraft:ender_chest')
        hold('ender chest block (contents on the bots)', x, z)
    }

    function allids(server, level) {
        let ids = (global.cdnsIds || []).filter(s => hasItem(s))
        let absent = (global.cdnsIds || []).length - ids.length
        let x = 0
        chunks(ids, 27).forEach(part => {
            setb(level, x, ROW.allids, 'minecraft:chest')
            let be = level.getBlockEntity(pos(x, ROW.allids))
            for (let i = 0; i < part.length; i++) be.setItem(i, Item.of(part[i]))
            hold('all ids chest', x, ROW.allids, part.length + ' ids')
            x += 2
        })
        M.allIds = ids.length
        M.idsWithNoItem = absent
        say('OK', 'allids ' + ids.length + ' items in chests, ' + absent + ' ids have no item form')
    }

    function entities(server) {
        let z = ROW.entities
        let x = 0
        let sum = (type, nbt, name, note) => {
            run(server, 'summon ' + type + ' ' + x + ' ' + Y + ' ' + z + ' ' + nbt)
            hold(name, x, z, note)
            x += 2
        }
        KINDS.forEach(k => sum('minecraft:item_frame', '{Facing:1b,Fixed:1b,Item:' + snbt(k) + '}', 'item frame', k))
        KINDS.forEach(k => sum('minecraft:item', '{Age:-32768,PickupDelay:32767,Item:' + snbt(k) + '}', 'dropped item', k))
        chunks(KINDS, 6).forEach(p => {
            let g = i => (i < p.length ? snbt(p[i]) : '{}')
            sum('minecraft:armor_stand', '{NoGravity:1b,ArmorItems:[' + g(0) + ',' + g(1) + ',' + g(2) + ',' + g(3) + '],HandItems:[' + g(4) + ',' + g(5) + ']}', 'armor stand', p.join(' '))
        })
        sum('minecraft:chest_minecart', '{Items:' + items(KINDS) + '}', 'chest minecart')
        sum('minecraft:villager', '{NoAI:1b,PersistenceRequired:1b}', 'villager inventory')
    }

    // spawn-animals=false discards Animal subclasses silently, so the mob holder is a golem. MCA converts the vanilla
    // villager and drops its inventory: fill it after the conversion, on its own step.
    function mobs(server) {
        let x = 0
        chunks(KINDS, 2).forEach(p => {
            run(server, 'summon minecraft:iron_golem ' + x + ' ' + Y + ' ' + (ROW.entities + 2) +
                ' {NoAI:1b,PersistenceRequired:1b,HandItems:[' + snbt(p[0]) + ',' + (p.length > 1 ? snbt(p[1]) : '{}') + ']}')
            hold('mob holding', x, ROW.entities + 2, p.join(' '))
            x += 3
        })
        run(server, 'data merge entity @e[type=mca:male_villager,limit=1] {Inventory:' + items(KINDS) + '}')
    }

    function fillBot(server, name) {
        for (let i = 0; i < KINDS.length; i++) {
            run(server, 'item replace entity ' + name + ' hotbar.' + i + ' with ' + id(KINDS[i]))
            run(server, 'item replace entity ' + name + ' enderchest.' + i + ' with ' + id(KINDS[i]))
        }
        run(server, 'item replace entity ' + name + ' armor.head with ' + id('canned_corn'))
        run(server, 'item replace entity ' + name + ' weapon.offhand with ' + id('cornmeal'))
    }

    function bots(server) {
        run(server, 'pbot spawn ' + BOT_ON + ' 2 ' + Y + ' ' + ROW.bots + ' minecraft:overworld survival')
        run(server, 'pbot spawn ' + BOT_OFF + ' 6 ' + Y + ' ' + ROW.bots + ' minecraft:overworld survival')
        fillBot(server, BOT_ON)
        fillBot(server, BOT_OFF)
        attempt('curios slots', () => {
            const $Curios = Java.loadClass('top.theillusivec4.curios.api.CuriosApi')
            let done = 0
            ;[BOT_ON, BOT_OFF].forEach(n => {
                let p = server.getPlayerList().getPlayerByName(n)
                let h = $Curios.getCuriosHelper().getCuriosHandler(p).orElse(null)
                if (h == null) throw 'no curios handler on ' + n
                let i = 0
                h.getCurios().values().forEach(sh => {
                    let st = sh.getStacks()
                    for (let s = 0; s < st.getSlots() && i < KINDS.length; s++) { st.setStackInSlot(s, stack(KINDS[i])); i++; done++ }
                })
            })
            if (done == 0) throw 'no curios slots on a player'
            return done + ' slots filled'
        })
        hold('bot online ' + BOT_ON, 2, ROW.bots, 'inventory, head, offhand, ender chest, curios')
        hold('bot logged out ' + BOT_OFF, 6, ROW.bots, 'same; removed in pack2 so its .dat is an offline player')
    }

    function pack(server, level) {
        let z = ROW.pack
        let x = 0
        let at = n => { let v = x; x += n; return v }

        attempt('cfb corner', () => {
            let cx = at(2)
            setb(level, cx, z, 'cookingforblockheads:corner')
            let c = level.getBlockEntity(pos(cx, z)).getContainer()
            for (let i = 0; i < KINDS.length; i++) c.setItem(i, stack(KINDS[i]))
            hold('cfb corner (capability)', cx, z)
        })
        attempt('remainder buffer on a cooking pot', () => {
            let px = at(2)
            setb(level, px, z, 'farmersdelight:cooking_pot')
            let be = level.getBlockEntity(pos(px, z))
            let buf = be['ce$getRemainderBuffer']()
            let inv = be.getInventory()
            let a = buf.store(stack('wooden_bucket'), inv, 7, s => false)
            let b = buf.store(stack('mortar_and_pestle'), inv, 7, s => false)
            hold('remainder buffer (capability)', px, z, 'wooden_bucket, mortar_and_pestle')
            return 'left over: ' + a + ', ' + b + '; tag ' + String(be.saveWithoutMetadata()).indexOf('remainder_buffer')
        })
        attempt('backpack placed and as item', () => {
            let bx = at(2)
            let cx = at(2)
            let mk = () => {
                let st = Item.of('sophisticatedbackpacks:backpack')
                let n = fill(cap(st), KINDS)
                if (n != KINDS.length) throw 'backpack took ' + n
                return st
            }
            setb(level, bx, z, 'sophisticatedbackpacks:backpack')
            level.getBlockEntity(pos(bx, z)).setBackpack(mk())
            setb(level, cx, z, 'minecraft:chest')
            level.getBlockEntity(pos(cx, z)).setItem(0, mk())
            hold('backpack placed', bx, z)
            hold('backpack item in chest', cx, z)
        })
        attempt('storage delight cabinet (the pack has no Sophisticated Storage)', () => {
            let cx = at(2)
            run(server, 'setblock ' + cx + ' ' + Y + ' ' + z + ' storagedelight:acacia_drawer{Items:' + items(KINDS) + '}')
            hold('storage delight drawer', cx, z)
        })
        attempt('create toolbox', () => {
            let cx = at(2)
            setb(level, cx, z, 'create:brown_toolbox')
            let be = level.getBlockEntity(pos(cx, z))
            let n = fill(cap(be), KINDS)
            hold('create toolbox', cx, z, n + ' kinds')
            return n + ' kinds'
        })
        attempt('create depots', () => {
            KINDS.forEach(k => {
                let cx = at(2)
                setb(level, cx, z, 'create:depot')
                let left = cap(level.getBlockEntity(pos(cx, z))).insertItem(0, stack(k), false)
                if (!left.isEmpty()) throw 'depot refused ' + k
                hold('create depot', cx, z, k)
            })
        })
        ae2AndBelt(server, level)
        attempt('lootr chest placed', () => {
            let cx = at(2)
            run(server, 'setblock ' + cx + ' ' + Y + ' ' + z + ' lootr:lootr_chest{LootTable:"cdnsfix:moved",LootTableSeed:7L}')
            M.lootrX = cx
        })
        attempt('lightmans trader', () => {
            let cx = at(2)
            setb(level, cx, z, 'lightmanscurrency:display_case')
            let be = level.getBlockEntity(pos(cx, z))
            let owner = server.getPlayerList().getPlayerByName(BOT_ON)
            be.initialize(owner, Item.of('lightmanscurrency:display_case'))
            let t = be.getTraderData()
            if (t == null) throw 'no trader data after initialize'
            t.getTrade(0).setItem(stack('canned_corn'), 0)
            t.getStorage().forceAddItem(stack('canned_corn'))
            t.markTradesDirty()
            t.markStorageDirty()
            hold('lightmans trader', cx, z, 'canned_corn for sale and in storage')
        })
    }

    // Own row (pack + 2), so a rerun of this part cannot overwrite the other pack holders.
    function ae2AndBelt(server, level) {
        let z = ROW.pack + 2
        attempt('ae2 drive and me chest', () => {
            const $Cells = Java.loadClass('appeng.api.storage.StorageCells')
            const $Key = Java.loadClass('appeng.api.stacks.AEItemKey')
            const $Act = Java.loadClass('appeng.api.config.Actionable')
            const $Src = Java.loadClass('appeng.api.networking.security.IActionSource')
            let cell = () => {
                let st = Item.of('ae2:item_storage_cell_4k')
                let inv = $Cells.getCellInventory(st, null)
                KINDS.forEach(k => {
                    let key = $Key['of(net.minecraft.world.item.ItemStack)'](stack(k))
                    if (inv.insert(key, 1, $Act.MODULATE, $Src.empty()) != 1) throw 'cell refused ' + k
                })
                inv.persist()
                return st
            }
            setb(level, 0, z, 'ae2:drive')
            level.getBlockEntity(pos(0, z)).getInternalInventory().setItemDirect(0, cell())
            hold('ae2 drive cell', 0, z)
            setb(level, 2, z, 'ae2:chest')
            level.getBlockEntity(pos(2, z)).getInternalInventory().setItemDirect(1, cell())
            hold('ae2 me chest cell', 2, z)
        })
        attempt('create belt placed', () => {
            const $Belt = Java.loadClass('com.simibubi.create.content.kinetics.belt.item.BeltConnectorItem')
            $Belt.createBelts(level, pos(6, z), pos(14, z))
            M.beltX = 6
        })
    }

    function beltItem(level) {
        let z = ROW.pack + 2
        attempt('create belt item', () => {
            if (M.beltX == null) throw 'no belt'
            let h = cap(level.getBlockEntity(pos(M.beltX, z)))
            if (h == null) throw 'no handler on the belt'
            let left = h.insertItem(0, stack('cornmeal'), false)
            if (!left.isEmpty()) throw 'belt refused the item'
            hold('create belt item', M.beltX, z, 'cornmeal')
        })
    }

    // Runs a few ticks after pack: the belt needs its controller, and the bot must be near the Lootr chest.
    function pack2(server, level) {
        let z = ROW.pack
        beltItem(level)
        attempt('lootr chest opened by a player', () => {
            if (M.lootrX == null) throw 'no lootr chest'
            run(server, 'tp ' + BOT_ON + ' ' + (M.lootrX + 0.5) + ' ' + Y + ' ' + (z + 2))
            run(server, 'item replace entity ' + BOT_ON + ' hotbar.0 with minecraft:air')
            run(server, 'pbot use ' + BOT_ON + ' ' + M.lootrX + ' ' + Y + ' ' + z + ' north')
            run(server, 'item replace entity ' + BOT_ON + ' hotbar.0 with ' + id(KINDS[0]))
            hold('lootr chest opened by ' + BOT_ON, M.lootrX, z, 'per-player copy in data/lootr')
        })
        run(server, 'pbot remove ' + BOT_OFF)
    }

    function writeManifest() {
        M.y = Y
        M.rows = ROW
        M.kinds = KINDS
        M.bots = { online: BOT_ON, loggedOut: BOT_OFF }
        JsonIO.write('cdns_fixture_manifest.json', M)
        say('OK', 'manifest: ' + M.holders.length + ' holders, ' + M.missing.length + ' missing, ' + M.fails + ' command failures')
    }

    ServerEvents.commandRegistry(event => {
        const { commands: Commands, arguments: Arguments } = event
        const step = (name, fn) => event.register(Commands.literal('cdnsfix').then(Commands.literal(name).executes(ctx => {
            let server = ctx.source.server
            try { fn(server, ctx.source.level); say('OK', 'step ' + name + ' finished') } catch (e) { say('STEPFAIL', name + ': ' + e) }
            return 1
        })))
        step('blocks', blocks)
        step('vanilla', vanilla)
        step('allids', allids)
        step('entities', (s, l) => entities(s))
        step('bots', (s, l) => bots(s))
        step('pack', pack)
        step('pack2', pack2)
        step('ae2belt', (s, l) => { M.missing = []; ae2AndBelt(s, l) })
        step('beltitem', (s, l) => beltItem(l))
        step('mobs', (s, l) => mobs(s))
        step('manifest', (s, l) => writeManifest())
    })
})()
