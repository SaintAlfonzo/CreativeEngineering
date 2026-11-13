// Scratch-only: builds the NSM old-save fixture (scratch_queue.md "SQ-NSM", namespace_move.md 5; SQ-HCS-09 crows).
// Deployed by tools/scratch_query/nsm_fixture.sh into a scratch server on the 1.2.02 jars (old namespaces), never the pack.
// Commands, in order: /nsmfix chests | placed | ground | bags | bots | gear | labels | crows | dims | manifest
// Log lines: "[nsmfix] OK|MISSING|CMDFAIL|STEPFAIL ..." in logs/latest.log. Manifest: srv/nsm_fixture_manifest.json.
(() => {
    const $NReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $NRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $NPos = Java.loadClass('net.minecraft.core.BlockPos')
    const $NCaps = Java.loadClass('net.minecraftforge.common.capabilities.ForgeCapabilities')

    const CE = 'creativeengineering'
    const TIERS = ['copper', 'gold', 'diamond', 'netherite', 'dragon', 'echo', 'felsic']
    const DYES = ['white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray', 'light_gray', 'cyan',
        'purple', 'blue', 'brown', 'green', 'red', 'black']
    const MOVED = [].concat(
        TIERS.map(t => CE + ':' + t + '_shield'),
        DYES.map(d => CE + ':' + d + '_stepstool'),
        ['axe', 'hoe', 'pickaxe', 'shovel'].map(t => CE + ':echo_' + t),
        [CE + ':air_brake', CE + ':felsic_scrap', CE + ':arrow_fletching', CE + ':backpack_skin', CE + ':backpack_skin_clear',
            CE + ':flint_and_terrasteel', CE + ':weathered_cardboard_box'])
    const LUNCH = 'solapplepie:netherite_lunchbox'
    const PATTERNS = '{BlockEntityTag:{Base:11,Patterns:[{Pattern:"cr",Color:14},{Pattern:"bo",Color:4}]}}'
    const Y = -60
    const ROW = { chests: 0, placed: 4, ground: 8, bags: 12, labels: 16, crows: 22, bots: 28 }
    const BOT = 'nsmA'          // stays in the Overworld, gear worn
    const BOT_Y = 'nsmY'        // logs out in yellowstone with a spawn there
    const BOT_H = 'nsmH'        // logs out in hub

    if (!global.nsmManifest) global.nsmManifest = { holders: [], missing: [], fails: 0 }
    const M = global.nsmManifest

    function say(kind, what) { console.info('[nsmfix] ' + kind + ' ' + what) }
    function hold(name, x, y, z, note, dim) { M.holders.push({ name: name, pos: [x, y, z], dim: dim || 'minecraft:overworld', note: note || '' }) }
    function missing(name, why) { M.missing.push({ name: name, why: String(why) }); say('MISSING', name + ': ' + why) }
    function rl(s) { return new $NRL(s) }
    function hasItem(s) { return $NReg.ITEMS.containsKey(rl(s)) }
    function hasBlock(s) { return $NReg.BLOCKS.containsKey(rl(s)) }
    function pos(x, y, z) { return new $NPos(x, y, z) }
    function run(server, cmd) {
        let r = 0
        try {
            r = server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), cmd)
        } catch (e) { say('CMDFAIL', cmd.substring(0, 160) + ' -> ' + e); M.fails++; return 0 }
        if (r < 1) { say('CMDFAIL', cmd.substring(0, 200)); M.fails++ }
        return r
    }
    function attempt(name, fn) {
        try { let d = fn(); say('OK', name + (d == null ? '' : ': ' + d)) } catch (e) { missing(name, e) }
    }
    function snbt(idv, slot, tag) {
        return '{' + (slot == null ? '' : 'Slot:' + slot + 'b,') + 'id:"' + idv + '",Count:1b' + (tag ? ',tag:' + tag : '') + '}'
    }
    function cap(o, c) { return o.getCapability(c || $NCaps.ITEM_HANDLER).orElse(null) }
    function present(list) {
        return list.filter(s => { if (hasItem(s)) return true; missing('item ' + s, 'not registered on these jars'); return false })
    }
    function lunchbox() {
        let st = Item.of(LUNCH)
        let h = cap(st)
        if (h == null) throw 'lunchbox has no item handler'
        let food = ['minecraft:bread', 'minecraft:cooked_beef', 'minecraft:apple']
        let n = 0
        food.forEach((f, i) => { if (h.insertItem(i, Item.of(f, 4), false).isEmpty()) n++ })
        if (n == 0) throw 'lunchbox took no food'
        return st
    }

    // A chest with one of each moved Equipment / Magic / Compat item, a patterned shield, the filled lunchbox.
    function chests(server, level) {
        run(server, 'forceload add -16 -16 63 63')
        let ids = present(MOVED)
        let x = 0
        for (let i = 0; i < ids.length; i += 27) {
            let part = ids.slice(i, i + 27)
            run(server, 'setblock ' + x + ' ' + Y + ' ' + ROW.chests + ' minecraft:chest{Items:[' +
                part.map((s, k) => snbt(s, k)).join(',') + ']}')
            hold('moved ids chest', x, Y, ROW.chests, part.length + ' ids')
            x += 2
        }
        attempt('patterned shield and lunchbox chest', () => {
            run(server, 'setblock ' + x + ' ' + Y + ' ' + ROW.chests + ' minecraft:chest{Items:[' +
                snbt(CE + ':diamond_shield', 0, PATTERNS) + ',' + snbt(CE + ':echo_shield', 1, PATTERNS) + ']}')
            let be = level.getBlockEntity(pos(x, Y, ROW.chests))
            be.setItem(2, lunchbox())
            hold('patterned shields + lunchbox with food', x, Y, ROW.chests, 'slots 0, 1 shields; slot 2 lunchbox')
        })
    }

    // Dark Fire, Weathered Cardboard Box, Botanist Pot with a plant, jar with fluid, nightstand with items.
    function placed(server, level) {
        let z = ROW.placed
        attempt('dark fire', () => {
            run(server, 'setblock 0 ' + (Y - 1) + ' ' + z + ' minecraft:netherrack')
            run(server, 'setblock 0 ' + Y + ' ' + z + ' ' + CE + ':dark_fire')
            hold('dark fire (netherrack under it)', 0, Y, z)
            return String(level.getBlockState(pos(0, Y, z)).getBlock().getId())
        })
        attempt('weathered cardboard box', () => {
            run(server, 'setblock 2 ' + Y + ' ' + z + ' ' + CE + ':weathered_cardboard_box')
            hold('weathered cardboard box', 2, Y, z)
            return String(level.getBlockState(pos(2, Y, z)).getBlock().getId())
        })
        potAndJar(server, level)
        attempt('handcrafted nightstand with items', () => {
            run(server, 'setblock 8 ' + Y + ' ' + z + ' handcrafted:oak_nightstand')
            let be = level.getBlockEntity(pos(8, Y, z))
            let h = be == null ? null : cap(be)
            if (h == null) throw 'no item handler on the nightstand'
            let a = h.insertItem(0, Item.of('minecraft:book', 3), false)
            let b = h.insertItem(1, Item.of(CE + ':felsic_scrap', 2), false)
            be.setChanged()
            hold('handcrafted nightstand', 8, Y, z, 'book x3, felsic scrap x2')
            return 'left ' + a + ', ' + b + '; nbt ' + String(be.saveWithoutMetadata()).substring(0, 200)
        })
    }

    // Rerunnable on its own (/nsmfix potjar): Beautify's botanist workbench block (Compat's botanist_pot block entity), jar.
    function potAndJar(server, level) {
        let z = ROW.placed
        attempt('botanist pot with a plant', () => {
            let ids = ['beautify:botanist_workbench'].filter(hasBlock)
            if (ids.length == 0) throw 'no botanist pot block'
            run(server, 'setblock 4 ' + Y + ' ' + z + ' ' + ids[0])
            let be = level.getBlockEntity(pos(4, Y, z))
            let h = be == null ? null : cap(be)
            let how = 'no handler'
            if (h != null && h.insertItem(0, Item.of('minecraft:oak_sapling'), false).isEmpty()) how = 'item handler'
            else if (run(server, 'data merge block 4 ' + Y + ' ' + z + ' {Plant:"minecraft:poppy"}') > 0) how = 'Plant tag'
            hold('botanist pot ' + ids[0], 4, Y, z, how)
            return ids[0] + ', ' + how + ', nbt ' + (be == null ? 'none' : String(be.saveWithoutMetadata()).substring(0, 200))
        })
        attempt('supplementaries jar with fluid', () => {
            run(server, 'setblock 6 ' + Y + ' ' + z + ' supplementaries:jar')
            let be = level.getBlockEntity(pos(6, Y, z))
            let fh = cap(be, $NCaps.FLUID_HANDLER)
            if (fh == null) throw 'no fluid handler on the jar'
            const $FS = Java.loadClass('net.minecraftforge.fluids.FluidStack')
            const $FA = Java.loadClass('net.minecraftforge.fluids.capability.IFluidHandler$FluidAction')
            let n = fh.fill(new $FS['(net.minecraft.world.level.material.Fluid,int)']($NReg.FLUIDS.getValue(rl('minecraft:water')), 1000), $FA.EXECUTE)
            // the jar's handler refuses Forge water (0 mB on 1.2.02): write its soft fluid holder instead
            if (n == 0) run(server, 'data merge block 6 ' + Y + ' ' + z + ' {FluidHolder:{Count:4,Fluid:"minecraft:water"}}')
            be.setChanged()
            hold('supplementaries jar', 6, Y, z, n + ' mB water')
            return n + ' mB; nbt ' + String(be.saveWithoutMetadata()).substring(0, 200)
        })
    }

    // The same moved items dropped on the ground (no despawn, no pickup).
    function ground(server) {
        let x = 0
        let list = present([CE + ':gold_shield', CE + ':red_stepstool', CE + ':echo_pickaxe', CE + ':air_brake',
            CE + ':felsic_scrap', CE + ':arrow_fletching', CE + ':flint_and_terrasteel', CE + ':weathered_cardboard_box', LUNCH])
        list.forEach(s => {
            run(server, 'summon minecraft:item ' + x + ' ' + Y + ' ' + ROW.ground + ' {Age:-32768,PickupDelay:32767,Item:' + snbt(s) + '}')
            hold('dropped item', x, Y, ROW.ground, s)
            x += 2
        })
    }

    // Camera bags: leather with camera and film, dyed iron, netherite, in a chest (SQ-NSM-10).
    function bags(server) {
        let z = ROW.bags
        let content = '{CameraBag:{Items:[' + snbt('exposure:camera', 0) + ',' + snbt('exposure:black_and_white_film', 1) + ']}}'
        let dyed = '{display:{color:3949738},CameraBag:{Items:[' + snbt('exposure:color_film', 0) + ']}}'
        let ids = present(['exposure_extras:leather_camera_bag', 'exposure_extras:iron_camera_bag', 'exposure_extras:netherite_camera_bag',
            CE + ':iron_camera_bag'])
        let items = []
        if (ids.indexOf('exposure_extras:leather_camera_bag') >= 0) items.push(snbt('exposure_extras:leather_camera_bag', 0, content))
        if (ids.indexOf('exposure_extras:iron_camera_bag') >= 0) items.push(snbt('exposure_extras:iron_camera_bag', 1, dyed))
        if (ids.indexOf('exposure_extras:netherite_camera_bag') >= 0) items.push(snbt('exposure_extras:netherite_camera_bag', 2))
        if (ids.indexOf(CE + ':iron_camera_bag') >= 0) items.push(snbt(CE + ':iron_camera_bag', 3, content))
        run(server, 'setblock 0 ' + Y + ' ' + z + ' minecraft:chest{Items:[' + items.join(',') + ']}')
        hold('camera bags chest', 0, Y, z, ids.join(' '))
    }

    function curios(server, name, slotType, stack) {
        const $Curios = Java.loadClass('top.theillusivec4.curios.api.CuriosApi')
        let p = server.getPlayerList().getPlayerByName(name)
        let h = $Curios.getCuriosHelper().getCuriosHandler(p).orElse(null)
        if (h == null) throw 'no curios handler on ' + name
        let sh = h.getCurios().get(slotType)
        if (sh == null) throw 'no curios slot type ' + slotType + ' (have ' + h.getCurios().keySet() + ')'
        sh.getStacks().setStackInSlot(0, stack)
        return slotType + ' slot 0'
    }

    // Three bots: nsmA in the Overworld (gear), nsmY and nsmH sent to the moved dimensions in step dims.
    function bots(server) {
        run(server, 'pbot spawn ' + BOT + ' 2 ' + Y + ' ' + ROW.bots + ' minecraft:overworld survival')
        run(server, 'pbot spawn ' + BOT_Y + ' 6 ' + Y + ' ' + ROW.bots + ' minecraft:overworld survival')
        run(server, 'pbot spawn ' + BOT_H + ' 10 ' + Y + ' ' + ROW.bots + ' minecraft:overworld survival')
        hold('bot ' + BOT, 2, Y, ROW.bots, 'offhand shield, curios stepstool, notes, eraser, bag')
    }

    function gear(server) {
        run(server, 'item replace entity ' + BOT + ' weapon.offhand with ' + CE + ':netherite_shield' + PATTERNS)
        attempt('curios stepstool', () => {
            let st = Item.of(CE + ':blue_stepstool')
            let r = null
            ;['feet', 'stepstool', 'charm', 'belt'].some(t => { try { r = curios(server, BOT, t, st); return true } catch (e) { r = String(e); return false } })
            if (r.indexOf('slot 0') < 0) throw r
            return r
        })
        run(server, 'item replace entity ' + BOT + ' hotbar.1 with ' + CE + ':green_stepstool')
        run(server, 'item replace entity ' + BOT + ' hotbar.2 with ' + LUNCH)
        run(server, 'item replace entity ' + BOT + ' hotbar.3 with creative_labels:sticky_note{display:{color:16711680},CreativeLabels:{Text:"bot note"}} 3')
        run(server, 'item replace entity ' + BOT + ' hotbar.4 with creative_labels:eraser')
        run(server, 'item replace entity ' + BOT + ' hotbar.5 with exposure_extras:leather_camera_bag{CameraBag:{Items:[{Slot:0b,id:"exposure:camera",Count:1b}]}}')
    }

    // Labels: a stone wall with 3 written dyed notes (one special, one erased later by hand), a label with a diamond on a
    // chest face, notes and an eraser in a chest. Placement goes through the item's useOn, by the bot.
    function labels(server, level) {
        let z = ROW.labels
        run(server, 'fill 0 ' + Y + ' ' + z + ' 4 ' + (Y + 2) + ' ' + z + ' minecraft:stone')
        run(server, 'setblock 6 ' + Y + ' ' + z + ' minecraft:chest[facing=south]{Items:[' +
            snbt('creative_labels:sticky_note', 0, '{display:{color:65280},CreativeLabels:{Text:"chest note"}}') + ',' +
            snbt('creative_labels:eraser', 1) + ',' + snbt('creative_labels:label', 2) + ']}')
        hold('labels chest', 6, Y, z, 'note with text, eraser, label')
        run(server, 'tp ' + BOT + ' 2.5 ' + Y + ' ' + (z + 2.5) + ' 180 0')
        let notes = [
            '{display:{color:16711680},CreativeLabels:{Text:"red note one"}}',
            '{display:{color:255},CreativeLabels:{Text:"blue special",Special:"star"}}',
            '{display:{color:16776960},CreativeLabels:{Text:"to be erased"}}']
        notes.forEach((t, i) => {
            run(server, 'item replace entity ' + BOT + ' weapon.mainhand with creative_labels:sticky_note' + t)
            run(server, 'pbot use ' + BOT + ' ' + (1 + i) + ' ' + (Y + 1) + ' ' + z + ' south')
        })
        label(server, level)
        attempt('labels placed', () => {
            let out = []
            for (let x = 0; x <= 6; x++) for (let y = Y; y <= Y + 2; y++) for (let dz = 0; dz <= 1; dz++) {
                let b = String(level.getBlockState(pos(x, y, z + dz)).getBlock().getId())
                if (b.indexOf('creative_labels') == 0) out.push(x + ',' + y + ',' + (z + dz) + ' ' + b)
            }
            hold('labels wall', 0, Y, z, out.join('; '))
            return out.length + ' face_paper blocks: ' + out.join('; ')
        })
    }

    // The label goes on the chest's south face. The bot cannot sneak, so a use would open the chest: call the item's
    // placement directly, then set the picture (a diamond) in the face_paper block entity.
    function label(server, level) {
        let z = ROW.labels
        attempt('label with a diamond on the chest face', () => {
            const $Dir = Java.loadClass('net.minecraft.core.Direction')
            const $V3 = Java.loadClass('net.minecraft.world.phys.Vec3')
            let cls = null
            ;['com.SaintAlfonzo.creativelabels.block.FacePaperInteractions', 'com.SaintAlfonzo.creative_labels.block.FacePaperInteractions']
                .some(n => { try { cls = Java.loadClass(n); return true } catch (e) { return false } })
            if (cls == null) throw 'no FacePaperInteractions class'
            let p = server.getPlayerList().getPlayerByName(BOT)
            let cell = pos(6, Y, z + 1)
            let r = cls.placeLabel(level, p, Item.of('creative_labels:label'), cell, $Dir.NORTH, new $V3(6.5, Y + 0.5, z + 1.0))
            let be = level.getBlockEntity(cell)
            if (be == null) throw 'placeLabel returned ' + r + ', no block entity'
            run(server, 'data merge block 6 ' + Y + ' ' + (z + 1) + ' {Faces:{north:{Label:{Picture:{id:"minecraft:diamond",Count:1b}}}}}')
            hold('label on chest face', 6, Y, z + 1, 'diamond')
            return r + '; ' + String(be.saveWithoutMetadata()).substring(0, 300)
        })
    }

    // SQ-HCS-09: a tamed AM crow, an AM crow carrying an item, a hearthandharvest:nest on the ground.
    function crows(server, level) {
        let z = ROW.crows
        let p = server.getPlayerList().getPlayerByName(BOT)
        let uuid = p == null ? null : String(p.getGameProfile().getId())
        run(server, 'fill -2 ' + (Y + 3) + ' ' + (z - 2) + ' 12 ' + (Y + 3) + ' ' + (z + 2) + ' minecraft:glass')
        if (uuid) run(server, 'summon alexsmobs:crow 2 ' + Y + ' ' + z + ' {PersistenceRequired:1b,Owner:"' + uuid + '",CustomName:\'"tamed crow"\'}')
        run(server, 'summon alexsmobs:crow 6 ' + Y + ' ' + z + ' {PersistenceRequired:1b,HandItems:[{id:"minecraft:gold_nugget",Count:1b},{}],CustomName:\'"carrier crow"\'}')
        let r = run(server, 'place feature hearthandharvest:nest 10 ' + Y + ' ' + z)
        if (r < 1) run(server, 'setblock 10 ' + Y + ' ' + z + ' hearthandharvest:nest')
        hold('AM crows (tamed by ' + BOT + ', carrier) and a ground nest', 2, Y, z, 'nest at 10 ' + Y + ' ' + z + ' or near it')
        attempt('crow check', () => String(level.getEntities().getAll().filter(e => String(e.getType()).indexOf('crow') >= 0).length) + ' crows')
    }

    // Old dimensions: nsmY logs out in creativeengineering:yellowstone with a spawn point there, nsmH in hub.
    function dims(server) {
        run(server, 'execute in ' + CE + ':yellowstone run forceload add 0 0')
        run(server, 'execute in ' + CE + ':hub run forceload add 0 0')
        run(server, 'execute in ' + CE + ':yellowstone run tp ' + BOT_Y + ' 0 200 0')
        run(server, 'execute in ' + CE + ':hub run tp ' + BOT_H + ' 0 200 0')
        run(server, 'execute in ' + CE + ':yellowstone run spreadplayers 0 0 0 16 false ' + BOT_Y)
        run(server, 'execute in ' + CE + ':hub run spreadplayers 0 0 0 16 false ' + BOT_H)
        run(server, 'execute as ' + BOT_Y + ' at @s run setblock ~ ~ ~1 minecraft:red_bed[facing=south,part=foot]')
        run(server, 'execute as ' + BOT_Y + ' at @s run setblock ~ ~ ~2 minecraft:red_bed[facing=south,part=head]')
        run(server, 'execute as ' + BOT_Y + ' at @s run spawnpoint ' + BOT_Y + ' ~ ~ ~1')
        hold('bot ' + BOT_Y + ' logged out in yellowstone, spawn at the bed', 0, 0, 0, 'see the bot line in the log', CE + ':yellowstone')
        hold('bot ' + BOT_H + ' logged out in hub', 0, 0, 0, '', CE + ':hub')
    }

    function logout(server) {
        run(server, 'pbot remove ' + BOT_Y)
        run(server, 'pbot remove ' + BOT_H)
    }

    function writeManifest() {
        M.y = Y
        M.rows = ROW
        M.bots = { overworld: BOT, yellowstone: BOT_Y, hub: BOT_H }
        JsonIO.write('nsm_fixture_manifest.json', M)
        say('OK', 'manifest: ' + M.holders.length + ' holders, ' + M.missing.length + ' missing, ' + M.fails + ' command failures')
    }

    ServerEvents.commandRegistry(event => {
        const { commands: Commands } = event
        const step = (name, fn) => event.register(Commands.literal('nsmfix').then(Commands.literal(name).executes(ctx => {
            let server = ctx.source.server
            try { fn(server, ctx.source.level); say('OK', 'step ' + name + ' finished') } catch (e) { say('STEPFAIL', name + ': ' + e) }
            return 1
        })))
        step('chests', chests)
        step('placed', placed)
        step('ground', (s, l) => ground(s))
        step('bags', (s, l) => bags(s))
        step('bots', (s, l) => bots(s))
        step('gear', (s, l) => gear(s))
        step('labels', labels)
        step('label', label)
        step('potjar', potAndJar)
        step('crows', crows)
        step('dims', (s, l) => dims(s))
        step('logout', (s, l) => logout(s))
        step('manifest', (s, l) => writeManifest())
    })
})()
