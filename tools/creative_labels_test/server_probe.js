// Creative Labels scratch-server probe (feature_writeup/sticky_notes.md 6.1). Scratch-only: scratch_server.sh copies
// it into srv/kubejs/server_scripts as zz_cl_probe.js; it is never in the pack.
//
//   /cltest <group>   groups: place, actions, host, items, labels, donors (or all, which runs them a few ticks apart)
//
// Clicks go through ServerPlayerGameMode.useItemOn on Forge fake players, the same path a client's right-click packet
// takes on the server (RightClickBlock event, the paper's use, then the item's useOn). Results are logged as
// "[cltest] PASS|FAIL|INFO <id> <name>: <detail>" in logs/latest.log.
(() => {
    const $CLFakeFactory = Java.loadClass('net.minecraftforge.common.util.FakePlayerFactory')
    const $CLGameProfile = Java.loadClass('com.mojang.authlib.GameProfile')
    const $CLUUID = Java.loadClass('java.util.UUID')
    const $CLDir = Java.loadClass('net.minecraft.core.Direction')
    const $CLBlockPos = Java.loadClass('net.minecraft.core.BlockPos')
    const $CLVec3 = Java.loadClass('net.minecraft.world.phys.Vec3')
    const $CLAABB = Java.loadClass('net.minecraft.world.phys.AABB')
    const $CLHit = Java.loadClass('net.minecraft.world.phys.BlockHitResult')
    const $CLHand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $CLItemStack = Java.loadClass('net.minecraft.world.item.ItemStack')
    const $CLItemEntity = Java.loadClass('net.minecraft.world.entity.item.ItemEntity')
    const $CLExplosion = Java.loadClass('net.minecraft.world.level.Explosion')
    const $CLClickAction = Java.loadClass('net.minecraft.world.inventory.ClickAction')
    const $CLAnvilMenu = Java.loadClass('net.minecraft.world.inventory.AnvilMenu')
    const $CLAccess = Java.loadClass('net.minecraft.world.inventory.ContainerLevelAccess')
    const $CLCraftingContainer = Java.loadClass('net.minecraft.world.inventory.CraftingContainer')
    const $CLRecipeType = Java.loadClass('net.minecraft.world.item.crafting.RecipeType')
    const $CLDyeColor = Java.loadClass('net.minecraft.world.item.DyeColor')
    const $CLDyeItem = Java.loadClass('net.minecraft.world.item.DyeItem')
    const $CLItems = Java.loadClass('net.minecraft.world.item.Items')
    const $CLBlocks = Java.loadClass('net.minecraft.world.level.block.Blocks')
    const $CLForgeReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $CLRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $CLCompound = Java.loadClass('net.minecraft.nbt.CompoundTag')
    const $CLCreativeTab = Java.loadClass('net.minecraft.world.item.CreativeModeTab')
    const $CLNonNull = Java.loadClass('net.minecraft.core.NonNullList')
    const $CLAnchor = Java.loadClass('net.minecraft.commands.arguments.EntityAnchorArgument$Anchor')
    const $CLNoteData = Java.loadClass('com.SaintAlfonzo.creative_labels.block.NoteData')
    const $CLItemLabels = Java.loadClass('com.SaintAlfonzo.creative_labels.item.ItemLabels')
    const $CLColours = Java.loadClass('com.SaintAlfonzo.creative_labels.item.NoteColours')

    const Y = -58          // hosts float above the superflat ground (grass at -61)
    let results = { pass: 0, fail: 0 }

    function log(kind, id, name, detail) {
        if (kind == 'PASS') results.pass++
        if (kind == 'FAIL') results.fail++
        console.info('[cltest] ' + kind + ' ' + id + ' ' + name + ': ' + detail)
    }
    function check(id, name, ok, detail) { log(ok ? 'PASS' : 'FAIL', id, name, detail) }

    function item(id) { return $CLForgeReg.ITEMS.getValue(new $CLRL(id)) }
    // KubeJS Item.of: new ItemStack(item, n) is ambiguous in Rhino (ItemLike / Holder overloads)
    function stack(id, count) { return Item.of(id, count || 1) }
    function block(id) { return $CLForgeReg.BLOCKS.getValue(new $CLRL(id)) }
    function pos(x, y, z) { return new $CLBlockPos(x, y, z) }
    function dir(name) { return $CLDir.byName(name) }
    // Entity.moveTo's overloads don't resolve in Rhino: position and rotation one by one
    // setYRot / getDirection are hidden from scripts; KubeJS's own EntityKJS wrappers do the same
    function place(p, x, y, z, yaw, pitch) { p.setPositionAndRotation(x, y, z, yaw, pitch) }
    // ItemStack.hasTag/getTag are invisible to Rhino; KubeJS's getNbt() is the same tag or null
    function tagOf(st) { let t = st.getNbt(); return t == null || t.isEmpty() ? null : t }

    let fakeN = 0
    function fake(level, name) {
        fakeN++
        let p = $CLFakeFactory.get(level, new $CLGameProfile($CLUUID.randomUUID(), (name || 'cl') + fakeN))
        p.setShiftKeyDown(false)
        p.getInventory().clearContent()
        return p
    }

    // the clicked point on side D of the block at h, at (u, v) across that side
    const STEP = { north: [0, 0, -1], south: [0, 0, 1], east: [1, 0, 0], west: [-1, 0, 0], up: [0, 1, 0], down: [0, -1, 0] }
    const OPP = { north: 'south', south: 'north', east: 'west', west: 'east', up: 'down', down: 'up' }
    function facePoint(h, D, u, v) {
        let x = h.getX(), y = h.getY(), z = h.getZ()
        if (D == 'north') return new $CLVec3(x + u, y + v, z)
        if (D == 'south') return new $CLVec3(x + u, y + v, z + 1)
        if (D == 'west') return new $CLVec3(x, y + v, z + u)
        if (D == 'east') return new $CLVec3(x + 1, y + v, z + u)
        if (D == 'down') return new $CLVec3(x + u, y, z + v)
        return new $CLVec3(x + u, y + 1, z + v)
    }
    function cellOf(h, D) { let s = STEP[D]; return pos(h.getX() + s[0], h.getY() + s[1], h.getZ() + s[2]) }

    // right-click side D of the host h (or of whatever block is at h) holding st in the main hand
    function click(p, level, h, D, u, v, st, sneak) {
        p.setItemInHand($CLHand.MAIN_HAND, st)
        p.setShiftKeyDown(!!sneak)
        let hit = new $CLHit(facePoint(h, D, u, v), dir(D), h, false)
        let r = p.gameMode.useItemOn(p, level, p.getMainHandItem(), $CLHand.MAIN_HAND, hit)
        p.setShiftKeyDown(false)
        return String(r)
    }
    // right-click a point inside the paper cell (on its face toward the host)
    function clickPaper(p, level, cell, paperFace, u, v, st, sneak) {
        // the paper face toward the host = the host's side OPP[paperFace]; same plane
        let h = cellOf(cell, paperFace)
        p.setItemInHand($CLHand.MAIN_HAND, st)
        p.setShiftKeyDown(!!sneak)
        let at = facePoint(h, OPP[paperFace], u, v)
        // nudge 0.02 into the cell so the hit lands on the note
        let s = STEP[paperFace]
        at = new $CLVec3(at.x() - s[0] * 0.02, at.y() - s[1] * 0.02, at.z() - s[2] * 0.02)
        let hit = new $CLHit(at, dir(OPP[paperFace]), cell, false)
        let r = p.gameMode.useItemOn(p, level, p.getMainHandItem(), $CLHand.MAIN_HAND, hit)
        p.setShiftKeyDown(false)
        return String(r)
    }

    function be(level, cell) {
        let b = level.getBlockEntity(cell)
        if (b == null || String(b.getClass().getSimpleName()) != 'FacePaperBlockEntity') return null
        return b
    }
    // notes on the paper face pointing from the cell to the host (the paper face = OPP of the host side clicked)
    function notesOn(level, h, D) {
        let b = be(level, cellOf(h, D))
        return b == null ? 0 : b.face(dir(OPP[D])).noteCount()
    }
    function faceOf(level, h, D) {
        let b = be(level, cellOf(h, D))
        return b == null ? null : b.face(dir(OPP[D]))
    }
    function set(level, p, id) { level.setBlockAndUpdate(p, block(id).defaultBlockState()) }
    function setState(level, p, state) { level.setBlockAndUpdate(p, state) }
    // 'minecraft:oak_stairs[facing=north,half=bottom]' through KubeJS's block container
    function setSpec(level, p, spec) {
        let i = spec.indexOf('[')
        let props = {}
        if (i >= 0) {
            let parts = spec.substring(i + 1, spec.length - 1).split(',')
            for (let k = 0; k < parts.length; k++) { var kv = parts[k].split('='); props[kv[0]] = kv[1] }
        }
        level.getBlock(p).set(i >= 0 ? spec.substring(0, i) : spec, props)
    }
    function clearArea(level, x0, z0) {
        for (let x = x0 - 2; x <= x0 + 8; x++) for (let y = Y - 3; y <= Y + 4; y++) for (let z = -3; z <= 12; z++) {
            level.setBlockAndUpdate(pos(x, y, z), $CLBlocks.AIR.defaultBlockState())
        }
        killItems(level, x0)
    }
    function itemsNear(level, x0) {
        let box = new $CLAABB(x0 - 3, Y - 6, -4, x0 + 9, Y + 6, 13)
        return level.getEntitiesOfClass($CLItemEntity, box)
    }
    function killItems(level, x0) {
        let list = itemsNear(level, x0)
        for (let i = 0; i < list.size(); i++) list.get(i).discard()
    }
    function dropSummary(level, x0) {
        let list = itemsNear(level, x0)
        let out = []
        for (let i = 0; i < list.size(); i++) {
            let st = list.get(i).getItem()
            out.push(String(st.getCount()) + 'x ' + String($CLForgeReg.ITEMS.getKey(st.getItem())) + (tagOf(st) != null ? String(tagOf(st)) : ''))
        }
        return out
    }
    function note(opts) {
        let st = stack('creative_labels:sticky_note', (opts && opts.count) || 64)
        if (opts && opts.text) {
            let tag = st.getOrCreateTag()
            let cl = new $CLCompound()
            cl.putString('Text', opts.text)
            tag.put('CreativeLabels', cl)
        }
        if (opts && opts.color != null) $CLNoteData.writeColor(st, opts.color)
        if (opts && opts.special) $CLNoteData.writeSpecial(st, opts.special)
        return st
    }
    function hex(c) { return c < 0 ? 'none' : '#' + ('000000' + c.toString(16)).slice(-6) }
    function menuName(p) { return String(p.containerMenu.getClass().getSimpleName()) }
    function closeMenu(p) { try { p.doCloseContainer() } catch (e) { p.containerMenu = p.inventoryMenu } }

    // vanilla leather colour: helmet dyed with the dyes in order, one dyeing per dye
    function leatherMix(dyes) {
        let $Dyeable = Java.loadClass('net.minecraft.world.item.DyeableLeatherItem')
        let st = stack('minecraft:leather_helmet', 1)
        for (let i = 0; i < dyes.length; i++) {
            let list = Java.loadClass('java.util.List').of(item('minecraft:' + dyes[i] + '_dye'))
            st = $Dyeable.dyeArmor(st, list)
        }
        return st.getItem().getColor(st)
    }

    // ------------------------------------------------------------------------------------------- groups

    function groupPlace(level) {
        let X = 0
        clearArea(level, X)
        let p = fake(level, 'place')

        // SN-01 five spots + a sixth refused
        let h = pos(X, Y, 2)
        set(level, h, 'minecraft:stone')
        let pts = [[0.2, 0.2], [0.8, 0.2], [0.5, 0.5], [0.2, 0.8], [0.8, 0.8]]
        for (let i = 0; i < pts.length; i++) click(p, level, h, 'north', pts[i][0], pts[i][1], note({ count: 1 }), false)
        let five = notesOn(level, h, 'north')
        let mask = faceOf(level, h, 'north') == null ? 0 : faceOf(level, h, 'north').noteMask()
        let sixth = click(p, level, h, 'north', 0.5, 0.5, note({ count: 1 }), false)
        check('SN-01', 'five spots', five == 5 && mask == 31 && notesOn(level, h, 'north') == 5,
            five + ' notes, mask ' + mask + ', sixth click ' + sixth + ' -> ' + notesOn(level, h, 'north'))

        // SN-02 nearest free spot: two clicks on the same point
        let h2 = pos(X + 2, Y, 2)
        set(level, h2, 'minecraft:stone')
        click(p, level, h2, 'north', 0.5, 0.5, note({ count: 1 }), false)
        click(p, level, h2, 'north', 0.5, 0.5, note({ count: 1 }), false)
        check('SN-02', 'nearest free spot', notesOn(level, h2, 'north') == 2, notesOn(level, h2, 'north') + ' notes after two clicks on the centre')

        // SN-03 chest: plain click opens, sneak places, five fit
        let hc = pos(X + 4, Y, 2)
        set(level, hc, 'minecraft:chest')
        let r = click(p, level, hc, 'north', 0.5, 0.5, note({ count: 1 }), false)
        let opened = menuName(p)
        closeMenu(p)
        for (let i = 0; i < 6; i++) click(p, level, hc, 'north', 0.5, 0.5, note({ count: 1 }), true)
        check('SN-03', 'chests', opened != 'InventoryMenu' && notesOn(level, hc, 'north') == 5,
            'plain click ' + r + ' opened ' + opened + '; sneak x6 -> ' + notesOn(level, hc, 'north') + ' notes')

        // SN-04 / SN-05 / SN-06 / SN-07 the host table: fill every side with sneak clicks, count
        let table = [
            ['SN-04', 'stairs facing north, bottom', 'minecraft:oak_stairs[facing=north,half=bottom]', { down: 5, north: 5, south: 2, up: 2, east: 3, west: 3 }],
            ['SN-04', 'stairs facing east, bottom', 'minecraft:oak_stairs[facing=east,half=bottom]', { down: 5, east: 5, west: 2, up: 2, north: 3, south: 3 }],
            ['SN-04', 'stairs facing south, top', 'minecraft:oak_stairs[facing=south,half=top]', { up: 5, south: 5, north: 2, down: 2, east: 3, west: 3 }],
            ['SN-05', 'bottom slab', 'minecraft:stone_slab[type=bottom]', { up: 5, down: 5, north: 2, south: 2, east: 2, west: 2 }],
            ['SN-05', 'top slab', 'minecraft:stone_slab[type=top]', { up: 5, down: 5, north: 2, south: 2, east: 2, west: 2 }],
            ['SN-05', 'double slab', 'minecraft:stone_slab[type=double]', { up: 5, down: 5, north: 5, south: 5, east: 5, west: 5 }],
            ['SN-06', 'oak fence', 'minecraft:oak_fence', { up: 1, down: 1, north: 0, south: 0, east: 0, west: 0 }],
            ['SN-06', 'cobblestone wall', 'minecraft:cobblestone_wall', { up: 1, down: 1, north: 0, east: 0 }],
            ['SN-06', 'iron bars', 'minecraft:iron_bars', { up: 1, down: 1, north: 0, east: 0 }],
            ['SN-06', 'glass pane', 'minecraft:glass_pane', { up: 1, down: 1, north: 0, east: 0 }],
            ['SN-06', 'end rod', 'minecraft:end_rod[facing=up]', { up: 1, down: 1, north: 0, east: 0 }],
            ['SN-06', 'lightning rod', 'minecraft:lightning_rod[facing=up]', { up: 1, down: 1, north: 0, east: 0 }],
            ['SN-06', 'chain', 'minecraft:chain[axis=y]', { up: 1, down: 1, north: 0, east: 0 }],
            ['SN-07', 'carpet', 'minecraft:white_carpet', { up: 0, north: 0 }],
            ['SN-07', 'button', 'minecraft:stone_button[face=floor]', { up: 0, north: 0 }],
            ['SN-07', 'ladder', 'minecraft:ladder[facing=north]', { north: 0, south: 0 }],
            ['SN-07', 'door', 'minecraft:oak_door[half=lower]', { north: 0, up: 0 }],
            ['SN-07', 'trapdoor', 'minecraft:oak_trapdoor[half=bottom]', { up: 0, north: 0 }],
            ['SN-07', 'flower', 'minecraft:poppy', { north: 0, up: 0 }],
            ['SN-07', 'head', 'minecraft:skeleton_skull', { north: 0, up: 0 }],
        ]
        let row = 0
        for (let t = 0; t < table.length; t++) {
            let spec = table[t]
            let sides = Object.keys(spec[3])
            let got = {}
            let bad = []
            for (let s = 0; s < sides.length; s++) {
                let D = sides[s]
                // a fresh host per side so the paper of one side never touches another
                var hp = pos(X + 10 + (row % 8) * 3, Y + 1, -2 + Math.floor(row / 8) * 3)
                row++
                for (let x = -1; x <= 1; x++) for (let y = -1; y <= 1; y++) for (let z = -1; z <= 1; z++) {
                    level.setBlockAndUpdate(pos(hp.getX() + x, hp.getY() + y, hp.getZ() + z), $CLBlocks.AIR.defaultBlockState())
                }
                setSpec(level, hp, spec[2])
                if (spec[2].indexOf('poppy') >= 0) setState(level, pos(hp.getX(), hp.getY() - 1, hp.getZ()), $CLBlocks.GRASS_BLOCK.defaultBlockState())
                if (spec[2].indexOf('oak_door') >= 0) setState(level, pos(hp.getX(), hp.getY() - 1, hp.getZ()), $CLBlocks.STONE.defaultBlockState())
                for (let k = 0; k < 9; k++) {
                    var uv = [[0.2, 0.2], [0.8, 0.2], [0.5, 0.5], [0.2, 0.8], [0.8, 0.8], [0.5, 0.2], [0.5, 0.8], [0.2, 0.5], [0.8, 0.5]][k]
                    click(p, level, hp, D, uv[0], uv[1], note({ count: 1 }), true)
                }
                got[D] = notesOn(level, hp, D)
                if (got[D] != spec[3][D]) bad.push(D + ' ' + got[D] + ' (want ' + spec[3][D] + ')')
            }
            check(spec[0], spec[1], bad.length == 0, bad.length == 0 ? JSON.stringify(got) : bad.join(', '))
        }

        // SN-08 floor direction: first placer's facing wins
        let hf = pos(X, Y, 6)
        set(level, hf, 'minecraft:stone')
        place(p, X + 0.5, Y + 1, 9.5, 180, 30)   // yaw 180 = facing north
        click(p, level, hf, 'up', 0.2, 0.2, note({ count: 1 }), false)
        let r1 = String(faceOf(level, hf, 'up').rotation)
        place(p, X - 2.5, Y + 1, 6.5, -90, 30)  // yaw -90 = facing east
        click(p, level, hf, 'up', 0.8, 0.8, note({ count: 1 }), false)
        let r2 = String(faceOf(level, hf, 'up').rotation)
        let hf2 = pos(X + 2, Y, 6)
        set(level, hf2, 'minecraft:stone')
        click(p, level, hf2, 'up', 0.5, 0.5, note({ count: 1 }), false)
        let r3 = String(faceOf(level, hf2, 'up').rotation)
        check('SN-08', 'floor direction', r1 == 'north' && r2 == 'north' && r3 == 'east', 'first north -> ' + r1 + ', then east -> ' + r2 + '; fresh face east -> ' + r3)

        // SN-09 notes or label per face; label + notes on different faces of one cell
        let hl = pos(X + 4, Y, 6)
        set(level, hl, 'minecraft:stone')
        click(p, level, hl, 'north', 0.5, 0.5, note({ count: 1 }), false)
        let lr = click(p, level, hl, 'north', 0.5, 0.5, stack('creative_labels:label'), false)
        let hasLabel = faceOf(level, hl, 'north').hasLabel()
        // the cell north of hl: put a stone further north-west so the same cell has a second host on its west
        let cell = cellOf(hl, 'north')
        let west = pos(cell.getX() - 1, cell.getY(), cell.getZ())
        set(level, west, 'minecraft:stone')
        let lr2 = click(p, level, west, 'east', 0.5, 0.5, stack('creative_labels:label'), false)
        let b = be(level, cell)
        let both = b != null && b.face(dir('south')).noteCount() == 1 && b.face(dir('west')).hasLabel()
        let nr = click(p, level, west, 'east', 0.5, 0.5, note({ count: 1 }), false)
        check('SN-09', 'notes or label', !hasLabel && both && b.face(dir('west')).noteCount() == 0,
            'label on a noted face ' + lr + ' (label ' + hasLabel + '); label on the other face ' + lr2 + ', both kept ' + both + '; note on the label face ' + nr)

        // SN-22 water: paper in a water source keeps the water, and the water comes back when it goes
        let hw = pos(X + 6, Y, 6)
        set(level, hw, 'minecraft:stone')
        let wc = cellOf(hw, 'north')
        set(level, wc, 'minecraft:water')
        click(p, level, hw, 'north', 0.5, 0.5, note({ count: 1 }), false)
        let ws = level.getBlockState(wc)
        let logged = String(ws).indexOf('waterlogged=true') >= 0
        clickPaper(p, level, wc, 'south', 0.5, 0.5, $CLItemStack.EMPTY, true)
        let after = String(level.getBlockState(wc).getBlock())
        check('SN-22', 'water', logged && after.indexOf('water') >= 0, 'placed: ' + ws + '; after pick-up: ' + after)
        p.discard()
    }

    function groupActions(level) {
        let X = 40
        clearArea(level, X)
        let p = fake(level, 'act')

        // SN-11 dye a placed note: only it, one dye used, mixes like leather, special refuses
        let h = pos(X, Y, 2)
        set(level, h, 'minecraft:stone')
        click(p, level, h, 'north', 0.2, 0.2, note({ count: 1 }), false)
        click(p, level, h, 'north', 0.8, 0.8, note({ count: 1 }), false)
        let red = stack('minecraft:red_dye', 4)
        clickPaper(p, level, cellOf(h, 'north'), 'south', 0.2, 0.2, red, false)
        let usedRed = 4 - p.getMainHandItem().getCount()
        // the dyed note is the one under the click; find it by colour
        let c = faceOf(level, h, 'north')
        let spotA = -1, spotB = -1
        for (let i = 0; i < 5; i++) { if (c.hasNote(i)) { if (c.note(i).color >= 0) spotA = i; else spotB = i } }
        let cA = spotA < 0 ? -1 : c.note(spotA).color, cB = spotB < 0 ? 99 : c.note(spotB).color
        clickPaper(p, level, cellOf(h, 'north'), 'south', 0.2, 0.2, stack('minecraft:blue_dye', 1), false)
        let mixed = spotA < 0 ? -1 : faceOf(level, h, 'north').note(spotA).color
        let cB2 = spotB < 0 ? 99 : faceOf(level, h, 'north').note(spotB).color
        let want = leatherMix(['red', 'blue'])
        let wantRed = leatherMix(['red'])
        let hs = pos(X + 2, Y, 2)
        set(level, hs, 'minecraft:stone')
        click(p, level, hs, 'north', 0.5, 0.5, note({ count: 1, special: 'creeper' }), false)
        clickPaper(p, level, cellOf(hs, 'north'), 'south', 0.5, 0.5, stack('minecraft:red_dye', 1), false)
        let sp = faceOf(level, hs, 'north')
        let spNote = null
        for (let i = 0; i < 5; i++) if (sp.hasNote(i)) spNote = sp.note(i)
        check('SN-11', 'dye placed', usedRed == 1 && cA == wantRed && cB < 0 && cB2 < 0 && mixed == want && spNote.color < 0 && String(spNote.special) == 'creeper',
            'red used ' + usedRed + ', dyed ' + hex(cA) + ' (leather ' + hex(wantRed) + '), other ' + hex(cB) + ', +blue ' + hex(mixed) +
            ' (leather ' + hex(want) + '), special after dye ' + hex(spNote.color) + ' ' + spNote.special)

        // SN-13 sneak + empty hand takes the note back with its data
        p.getInventory().clearContent()
        clickPaper(p, level, cellOf(h, 'north'), 'south', 0.2, 0.2, $CLItemStack.EMPTY, true)
        let got = null
        for (let i = 0; i < p.getInventory().getContainerSize(); i++) {
            var s = p.getInventory().getItem(i)
            if (!s.isEmpty() && String($CLForgeReg.ITEMS.getKey(s.getItem())) == 'creative_labels:sticky_note') got = s
        }
        check('SN-13', 'remove', got != null && $CLNoteData.readColor(got) == want && notesOn(level, h, 'north') == 1,
            got == null ? 'no note in inventory' : 'got ' + got.getOrCreateTag() + ', ' + notesOn(level, h, 'north') + ' left')

        // SN-14 eraser on a placed written red note: text first, then colour
        let he = pos(X + 4, Y, 2)
        set(level, he, 'minecraft:stone')
        click(p, level, he, 'north', 0.5, 0.5, note({ count: 1, text: 'Iron', color: wantRed }), false)
        let ce = faceOf(level, he, 'north')
        let spot = -1
        for (let i = 0; i < 5; i++) if (ce.hasNote(i)) spot = i
        let n0 = ce.note(spot)
        let t0 = String(n0.text), col0 = n0.color
        let gold0 = String(n0.special)
        clickPaper(p, level, cellOf(he, 'north'), 'south', 0.5, 0.5, stack('creative_labels:eraser'), false)
        let n1 = faceOf(level, he, 'north').note(spot)
        let t1 = String(n1.text), col1 = n1.color
        clickPaper(p, level, cellOf(he, 'north'), 'south', 0.5, 0.5, stack('creative_labels:eraser'), false)
        let n2 = faceOf(level, he, 'north').note(spot)
        check('SN-14', 'eraser on a placed note', t0 == 'Iron' && t1 == '' && col1 == wantRed && n2.color < 0 && n2.isPlain(),
            'before "' + t0 + '" ' + hex(col0) + ' (special ' + gold0 + '); 1st "' + t1 + '" ' + hex(col1) + '; 2nd ' + hex(n2.color) + ' plain ' + n2.isPlain())

        // SN-15 covered face: five notes on a crafting table block it; four don't
        let ht = pos(X, Y, 6)
        set(level, ht, 'minecraft:crafting_table')
        for (let i = 0; i < 5; i++) click(p, level, ht, 'north', 0.5, 0.5, note({ count: 1 }), true)
        let tcell = cellOf(ht, 'north')
        closeMenu(p)
        let rc = clickPaper(p, level, tcell, 'south', 0.5, 0.5, stack('minecraft:stick'), false)
        let m1 = menuName(p)
        closeMenu(p)
        clickPaper(p, level, tcell, 'south', 0.5, 0.5, $CLItemStack.EMPTY, true)
        // the freed centre has no paper under it, so the ray reaches the table itself
        let rc2 = click(p, level, ht, 'north', 0.5, 0.5, stack('minecraft:stick'), false)
        let m2 = menuName(p)
        closeMenu(p)
        check('SN-15', 'covered face', m1 == 'InventoryMenu' && m2 != 'InventoryMenu',
            '5 notes: stick click ' + rc + ' -> ' + m1 + '; 4 notes, click on the freed spot ' + rc2 + ' -> ' + m2)

        // SN-16 pass-through: a note on a furnace, click it with coal
        let hfu = pos(X + 2, Y, 6)
        set(level, hfu, 'minecraft:furnace')
        click(p, level, hfu, 'north', 0.2, 0.2, note({ count: 1 }), true)
        click(p, level, hfu, 'north', 0.8, 0.8, note({ count: 1 }), true)
        let rf = clickPaper(p, level, cellOf(hfu, 'north'), 'south', 0.2, 0.2, stack('minecraft:coal'), false)
        let mf = menuName(p)
        closeMenu(p)
        check('SN-16', 'pass-through', mf != 'InventoryMenu', 'coal on a note ' + rf + ' -> ' + mf)

        // SN-17 break one note (survival fake player looking at it): drops with data, the others stay
        let hb = pos(X + 4, Y, 6)
        set(level, hb, 'minecraft:stone')
        click(p, level, hb, 'north', 0.2, 0.2, note({ count: 1, text: 'Keep', color: wantRed }), false)
        click(p, level, hb, 'north', 0.8, 0.8, note({ count: 1 }), false)
        click(p, level, hb, 'north', 0.5, 0.5, note({ count: 1 }), false)
        killItems(level, X)
        let bcell = cellOf(hb, 'north')
        // aim from 1.5 blocks north of the face at the 0.2/0.2 note
        let target = facePoint(hb, 'north', 0.2, 0.2)
        place(p, target.x(), target.y() - 1.62, target.z() - 1.5, 0, 0)
        p.lookAt($CLAnchor.EYES, target)
        p.setItemInHand($CLHand.MAIN_HAND, $CLItemStack.EMPTY)
        let ok = p.gameMode.destroyBlock(bcell)
        let left = notesOn(level, hb, 'north')
        let drops = dropSummary(level, X)
        let dropText = drops.join(' | ')
        check('SN-17', 'break one note', ok && left == 2 && drops.length == 1 && dropText.indexOf('Keep') >= 0,
            'destroyBlock ' + ok + ', ' + left + ' left, drops: ' + dropText)
        p.discard()
    }

    function groupHost(level) {
        let X = 80
        clearArea(level, X)
        let p = fake(level, 'host')

        // SN-18 host mined: five notes with data on one face, a pictured label on another face
        let h = pos(X, Y, 2)
        set(level, h, 'minecraft:stone')
        for (let i = 0; i < 5; i++) click(p, level, h, 'north', 0.5, 0.5, note({ count: 1, text: 'N' + i }), false)
        click(p, level, h, 'east', 0.5, 0.5, stack('creative_labels:label'), false)
        clickPaper(p, level, cellOf(h, 'east'), 'west', 0.5, 0.5, stack('minecraft:diamond'), true)
        let pic = faceOf(level, h, 'east') != null && faceOf(level, h, 'east').hasLabel() ? String(faceOf(level, h, 'east').label.picture) : 'none'
        killItems(level, X)
        level.destroyBlock(h, true)
        let drops = dropSummary(level, X)
        let joined = drops.join(' | ')
        let noteDrops = 0, written = 0, labelBlank = false
        for (let i = 0; i < drops.length; i++) {
            if (drops[i].indexOf('creative_labels:sticky_note') >= 0) { noteDrops++; if (drops[i].indexOf('Text') >= 0) written++ }
            if (drops[i].indexOf('creative_labels:label') >= 0 && drops[i].indexOf('{') < 0) labelBlank = true
        }
        let gone = String(level.getBlockState(cellOf(h, 'north')).getBlock()) + ' / ' + String(level.getBlockState(cellOf(h, 'east')).getBlock())
        check('SN-18', 'host mined', noteDrops == 5 && written == 5 && labelBlank,
            'picture before ' + pic + '; drops: ' + joined + '; cells now ' + gone)

        // SN-19 explosions. a: obsidian survives -> paper stays. b: stone wall destroyed -> paper drops, none lost
        let ho = pos(X, Y, 6)
        set(level, ho, 'minecraft:obsidian')
        for (let i = 0; i < 5; i++) click(p, level, ho, 'north', 0.5, 0.5, note({ count: 1, text: 'O' + i }), false)
        click(p, level, ho, 'up', 0.5, 0.5, stack('creative_labels:label'), false)
        killItems(level, X)
        level.explode(null, X + 0.5, Y + 0.5, 6 - 1.8, 4.0, $CLExplosion.BlockInteraction.BREAK)
        let kept = notesOn(level, ho, 'north')
        let keptLabel = faceOf(level, ho, 'up') != null && faceOf(level, ho, 'up').hasLabel()
        let dropsA = dropSummary(level, X)
        check('SN-19', 'explosion, host survives', kept == 5 && keptLabel, kept + ' notes, label ' + keptLabel + ', loose drops: ' + dropsA.join(' | '))

        let hs = pos(X + 4, Y, 6)
        set(level, hs, 'minecraft:stone')
        for (let i = 0; i < 5; i++) click(p, level, hs, 'north', 0.5, 0.5, note({ count: 1, text: 'S' + i }), false)
        click(p, level, hs, 'up', 0.5, 0.5, stack('creative_labels:label'), false)
        killItems(level, X)
        level.explode(null, X + 4.5, Y + 0.5, 6 + 1.5, 4.0, $CLExplosion.BlockInteraction.BREAK)
        let hostAfter = String(level.getBlockState(hs).getBlock())
        // drops spawn on the explosion's tick; read them a tick later too
        let dropsB = dropSummary(level, X + 4)
        let nB = 0, lB = 0
        for (let i = 0; i < dropsB.length; i++) {
            if (dropsB[i].indexOf('creative_labels:sticky_note') >= 0 && dropsB[i].indexOf('Text') >= 0) nB += parseInt(dropsB[i])
            if (dropsB[i].indexOf('creative_labels:label') >= 0) lB += parseInt(dropsB[i])
        }
        check('SN-19', 'explosion, host destroyed', hostAfter.indexOf('stone') < 0 && nB == 5 && lB == 1,
            'host now ' + hostAfter + '; written notes dropped ' + nB + ', labels ' + lB + '; drops: ' + dropsB.join(' | '))

        // SN-21 pistons: push a papered block; its paper drops with data
        let base = pos(X, Y, 10)
        setSpec(level, base, 'minecraft:piston[facing=east]')
        let hostP = pos(X + 1, Y, 10)
        set(level, hostP, 'minecraft:stone')
        for (let i = 0; i < 3; i++) click(p, level, hostP, 'up', 0.5, 0.5, note({ count: 1, text: 'P' + i }), false)
        killItems(level, X)
        set(level, pos(X - 1, Y, 10), 'minecraft:redstone_block')
        console.info('[cltest] INFO SN-21 piston fired; read with /cltest pistoncheck after a few ticks')
        p.discard()
    }

    function pistonCheck(level) {
        let X = 80
        // a piston with no player nearby can leave the block as moving_piston for a while; the paper is what counts
        let moved = String(level.getBlockState(pos(X + 2, Y, 10)).getBlock())
        let paperLeft = be(level, pos(X + 1, Y + 1, 10)) != null || be(level, pos(X + 2, Y + 1, 10)) != null
        let drops = dropSummary(level, X)
        let n = 0
        for (let i = 0; i < drops.length; i++) if (drops[i].indexOf('sticky_note') >= 0 && drops[i].indexOf('Text') >= 0) n += parseInt(drops[i])
        check('SN-21', 'pistons', (moved.indexOf('stone') >= 0 || moved.indexOf('moving_piston') >= 0) && !paperLeft && n == 3, 'host moved to ' + moved + ', paper left ' + paperLeft + ', drops: ' + drops.join(' | '))
    }

    // crafting through the real recipe manager on a 3x3 container
    function craft(level, p, grid) {
        let cc = new $CLCraftingContainer(p.inventoryMenu, 3, 3)
        for (let i = 0; i < grid.length; i++) if (grid[i] != null) cc.setItem(i, grid[i])
        let opt = level.getServer().getRecipeManager().getRecipeFor($CLRecipeType.CRAFTING, cc, level)
        if (!opt.isPresent()) return { out: null, id: 'none', rem: null }
        let r = opt.get()
        return { out: r.assemble(cc), id: String(r.getId()), rem: r.getRemainingItems(cc) }
    }

    function groupItems(level) {
        let p = fake(level, 'items')
        let red = leatherMix(['red'])
        let redBlue = leatherMix(['red', 'blue'])

        // SN-25 stacking
        let a = note({ count: 1 }), b = note({ count: 1 })
        let ra = note({ count: 1, color: red }), rb = note({ count: 1, color: red })
        let w = note({ count: 1, text: 'Iron' })
        check('SN-25', 'stacking', $CLItemStack.isSameItemSameTags(a, b) && $CLItemStack.isSameItemSameTags(ra, rb) && !$CLItemStack.isSameItemSameTags(a, w) && a.getMaxStackSize() == 64,
            'plain/plain, red/red, plain/written, max ' + a.getMaxStackSize())

        // SN-26 colour in the grid
        let g = []
        for (let i = 0; i < 8; i++) g.push(note({ count: 1 }))
        g.push(stack('minecraft:red_dye'))
        let r1 = craft(level, p, g)
        let g2 = []
        for (let i = 0; i < 8; i++) g2.push(note({ count: 1, color: red }))
        g2.push(stack('minecraft:blue_dye'))
        let r2 = craft(level, p, g2)
        let r3 = craft(level, p, [note({ count: 1, text: 'Iron' }), stack('minecraft:red_dye')])
        let g4 = []
        for (let i = 0; i < 8; i++) g4.push(note({ count: 1 }))
        g4.push(stack('minecraft:gunpowder'))
        let r4 = craft(level, p, g4)
        let ok26 = r1.out != null && r1.out.getCount() == 8 && $CLNoteData.readColor(r1.out) == red &&
            r2.out != null && r2.out.getCount() == 8 && $CLNoteData.readColor(r2.out) == redBlue &&
            r3.out != null && String($CLNoteData.readText(r3.out)) == 'Iron' &&
            r4.out != null && r4.out.getCount() == 8 && String($CLNoteData.readSpecial(r4.out)) == 'creeper'
        check('SN-26', 'colour in the grid', ok26,
            '8+red ' + r1.id + ' -> ' + (r1.out ? r1.out.getCount() + ' ' + hex($CLNoteData.readColor(r1.out)) : 'none') + ' (leather ' + hex(red) + '); ' +
            '8 red+blue -> ' + (r2.out ? r2.out.getCount() + ' ' + hex($CLNoteData.readColor(r2.out)) : 'none') + ' (leather ' + hex(redBlue) + '); ' +
            'written+red -> ' + (r3.out ? '"' + $CLNoteData.readText(r3.out) + '"' : 'none') + '; 8+gunpowder ' + r4.id + ' -> ' + (r4.out ? r4.out.getCount() + ' ' + $CLNoteData.readSpecial(r4.out) : 'none'))

        // SN-26b specials refuse dye, also through vanilla's armour-dye recipe (ArmorDyeRecipeMixin)
        let r5 = craft(level, p, [note({ count: 1, special: 'creeper' }), stack('minecraft:red_dye')])
        check('SN-26', 'special + dye gives nothing', r5.out == null || r5.out.isEmpty(), 'creeper + red -> ' + r5.id)

        // SN-27 eraser in the grid
        let e1 = craft(level, p, [note({ count: 1, text: 'Iron', color: red }), stack('creative_labels:eraser')])
        let e2 = e1.out == null ? null : craft(level, p, [e1.out, stack('creative_labels:eraser')])
        let rem = e1.rem == null ? '' : String(e1.rem)
        let eraser = stack('creative_labels:eraser')
        let ok27 = e1.out != null && String($CLNoteData.readText(e1.out)) == '' && $CLNoteData.readColor(e1.out) == red &&
            e2 != null && e2.out != null && $CLNoteData.readColor(e2.out) < 0 && rem.indexOf('eraser') >= 0 && !eraser.isDamageableItem()
        check('SN-27', 'eraser in the grid', ok27,
            e1.id + ' -> ' + (e1.out ? '"' + $CLNoteData.readText(e1.out) + '" ' + hex($CLNoteData.readColor(e1.out)) : 'none') + ', remainder ' + rem +
            '; again -> ' + (e2 && e2.out ? hex($CLNoteData.readColor(e2.out)) : 'none') + '; damageable ' + eraser.isDamageableItem())

        // SN-53 copy: 1 written red + 8 blank mixed colours -> 9 red written
        let gc = [note({ count: 1, text: 'Iron', color: red })]
        for (let i = 0; i < 8; i++) gc.push(note({ count: 1, color: i % 2 == 0 ? -1 : redBlue }))
        let rc = craft(level, p, gc)
        check('SN-53', 'copy', rc.out != null && rc.out.getCount() == 9 && String($CLNoteData.readText(rc.out)) == 'Iron' && $CLNoteData.readColor(rc.out) == red,
            rc.id + ' -> ' + (rc.out ? rc.out.getCount() + ' "' + $CLNoteData.readText(rc.out) + '" ' + hex($CLNoteData.readColor(rc.out)) : 'none'))

        // SN-30 inventory labelling: written note on the cursor onto a slot of 32 cobblestone
        let p30 = fake(level, 'inv')
        p30.getInventory().setItem(9, stack('minecraft:cobblestone', 32))
        let slot = p30.inventoryMenu.getSlot(9)
        let wn = note({ count: 3, text: 'Iron' })
        let handled = wn.getItem().overrideStackedOnOther(wn, slot, $CLClickAction.SECONDARY, p30)
        let lines = $CLItemLabels.read(slot.getItem())
        check('SN-30', 'inventory', handled && String(lines) == '[Iron]' && slot.getItem().getCount() == 32 && wn.getCount() == 2,
            'handled ' + handled + ', lines ' + lines + ' on ' + slot.getItem().getCount() + ', notes left ' + wn.getCount())

        // SN-54 drag: a second labelling in the same tick is swallowed (5-tick gate)
        p30.getInventory().setItem(10, stack('minecraft:cobblestone', 5))
        let slot2 = p30.inventoryMenu.getSlot(10)
        let handled2 = wn.getItem().overrideStackedOnOther(wn, slot2, $CLClickAction.SECONDARY, p30)
        check('SN-54', 'drag gate', handled2 && !$CLItemLabels.has(slot2.getItem()) && wn.getCount() == 2,
            'second slot same tick: handled ' + handled2 + ', labelled ' + $CLItemLabels.has(slot2.getItem()) + ', notes left ' + wn.getCount())

        // SN-31 anvil: cobblestone + written note -> line, cost 1
        let p31 = fake(level, 'anvil')
        let anvil = new $CLAnvilMenu(77, p31.getInventory(), $CLAccess.create(level, pos(0, Y, 0)))
        anvil.getSlot(0).set(stack('minecraft:cobblestone', 16))
        anvil.getSlot(1).set(note({ count: 1, text: 'Anvil' }))
        anvil.createResult()
        let outA = anvil.getSlot(2).getItem()
        check('SN-31', 'anvil', !outA.isEmpty() && String($CLItemLabels.read(outA)) == '[Anvil]' && anvil.getCost() == 1 && outA.getCount() == 16,
            'output ' + outA + ' ' + $CLItemLabels.read(outA) + ', cost ' + anvil.getCost())

        // SN-32 off hand: sneak + use in the air labels the off-hand sword
        let p32 = fake(level, 'off')
        p32.setItemInHand($CLHand.OFF_HAND, stack('minecraft:iron_sword'))
        let n32 = note({ count: 2, text: 'Mine' })
        p32.setItemInHand($CLHand.MAIN_HAND, n32)
        p32.setShiftKeyDown(true)
        let u32 = p32.gameMode.useItem(p32, level, p32.getMainHandItem(), $CLHand.MAIN_HAND)
        p32.setShiftKeyDown(false)
        let sword = p32.getOffhandItem()
        check('SN-32', 'off hand', String($CLItemLabels.read(sword)) == '[Mine]' && p32.getMainHandItem().getCount() == 1,
            'use ' + u32 + ', sword lines ' + $CLItemLabels.read(sword) + ', notes left ' + p32.getMainHandItem().getCount())

        // SN-33 three lines, a fourth refused (fresh players to pass the gate)
        let target = stack('minecraft:cobblestone', 4)
        let used = 0
        for (let i = 0; i < 4; i++) {
            var pl = fake(level, 'three')
            pl.getInventory().setItem(9, target)
            var sl = pl.inventoryMenu.getSlot(9)
            var nn = note({ count: 1, text: 'L' + i })
            nn.getItem().overrideStackedOnOther(nn, sl, $CLClickAction.SECONDARY, pl)
            if (nn.isEmpty()) used++
            target = sl.getItem()
            pl.discard()
        }
        check('SN-33', 'three lines', String($CLItemLabels.read(target)) == '[L0, L1, L2]' && used == 3,
            'lines ' + $CLItemLabels.read(target) + ', notes used ' + used + ' of 4')

        // SN-34 eraser on items: lines off one by one, then the stack merges with clean cobblestone
        for (let i = 0; i < 3; i++) {
            var pe = fake(level, 'erase')
            pe.getInventory().setItem(9, target)
            var se = pe.inventoryMenu.getSlot(9)
            var er = stack('creative_labels:eraser')
            er.getItem().overrideStackedOnOther(er, se, $CLClickAction.SECONDARY, pe)
            target = se.getItem()
            pe.discard()
        }
        check('SN-34', 'eraser on items', !$CLItemLabels.has(target) && $CLItemStack.isSameItemSameTags(target, stack('minecraft:cobblestone')),
            'after 3 erasings: ' + target + ' tag ' + (tagOf(target) != null ? tagOf(target) : 'none'))

        // SN-55 labelled items give no crafting result (blockLabelledCrafting on). FastWorkbench queues the result
        // update to the end of the tick, so the result is read a few ticks later.
        let p55 = fake(level, 'craft')
        let plank = stack('minecraft:oak_planks', 1)
        $CLItemLabels.add(plank, 'Wood')
        let craftSlots = p55.inventoryMenu.getCraftSlots()
        craftSlots.setItem(0, plank)
        craftSlots.setItem(2, stack('minecraft:oak_planks', 1))
        p55.inventoryMenu.slotsChanged(craftSlots)
        let server = level.getServer()
        server.scheduleInTicks(3, () => {
            let res = String(p55.inventoryMenu.getSlot(0).getItem())
            craftSlots.setItem(0, stack('minecraft:oak_planks', 1))
            p55.inventoryMenu.slotsChanged(craftSlots)
            server.scheduleInTicks(3, () => {
                let res2 = p55.inventoryMenu.getSlot(0).getItem()
                check('SN-55', 'crafting blocked', res.indexOf('air') >= 0 && !res2.isEmpty(), 'labelled plank + plank -> ' + res + '; clean planks -> ' + res2)
                p55.discard()
            })
        })
        p.discard()
    }

    function groupLabels(level) {
        let X = 120
        clearArea(level, X)
        let p = fake(level, 'lab')
        let h = pos(X, Y, 2)
        set(level, h, 'minecraft:chest')
        let cell = cellOf(h, 'north')

        // SN-36 place (sneak on a chest), flush: inset 0
        let r = click(p, level, h, 'north', 0.5, 0.5, stack('creative_labels:label', 4), true)
        let c = faceOf(level, h, 'north')
        check('SN-36', 'place', c != null && c.hasLabel(), 'click ' + r + ', label ' + (c != null && c.hasLabel()) + ', inset ' + (c == null ? '-' : c.inset) + ', labels left ' + p.getMainHandItem().getCount())

        // SN-37 picture: sneak + diamond, not used up
        let dia = stack('minecraft:diamond', 3)
        clickPaper(p, level, cell, 'south', 0.5, 0.5, dia, true)
        let l = faceOf(level, h, 'north').label
        check('SN-37', 'picture', String(l.picture).indexOf('diamond') >= 0 && p.getMainHandItem().getCount() == 3,
            'picture ' + l.picture + ', diamonds left ' + p.getMainHandItem().getCount())

        // SN-38 stack tints kept in the picture (C2 strips the rest): healing potion, dyed leather
        let h2 = pos(X + 2, Y, 2)
        set(level, h2, 'minecraft:stone')
        click(p, level, h2, 'north', 0.5, 0.5, stack('creative_labels:label'), false)
        let potion = stack('minecraft:potion')
        potion.getOrCreateTag().putString('Potion', 'minecraft:healing')
        let junk = ''
        for (let i = 0; i < 200; i++) junk += 'x'
        potion.getOrCreateTag().putString('Junk', junk)
        clickPaper(p, level, cellOf(h2, 'north'), 'south', 0.5, 0.5, potion, true)
        let pic = String(faceOf(level, h2, 'north').label.picture.getOrCreateTag())
        check('SN-38', 'picture NBT kept / stripped', pic.indexOf('healing') >= 0 && pic.indexOf('Junk') < 0, 'stored tag ' + pic)

        // SN-40 dye the icon; SN-42 glow, ink, feather
        clickPaper(p, level, cell, 'south', 0.5, 0.5, stack('minecraft:red_dye', 2), false)
        let dyeUsed = 2 - p.getMainHandItem().getCount()
        clickPaper(p, level, cell, 'south', 0.5, 0.5, stack('minecraft:glow_ink_sac', 1), false)
        let glow = faceOf(level, h, 'north').label.glow
        clickPaper(p, level, cell, 'south', 0.5, 0.5, stack('minecraft:ink_sac', 1), false)
        let glow2 = faceOf(level, h, 'north').label.glow
        clickPaper(p, level, cell, 'south', 0.5, 0.5, stack('minecraft:feather', 1), false)
        let name = faceOf(level, h, 'north').label.showName
        l = faceOf(level, h, 'north').label
        check('SN-40', 'dyed icon (server half)', String(l.dye) == 'red' && dyeUsed == 1 && String(l.picture).indexOf('diamond') >= 0, 'dye ' + l.dye + ', used ' + dyeUsed)
        check('SN-42', 'glow, ink, feather (server half)', glow && !glow2 && name, 'glow ink -> ' + glow + ', ink -> ' + glow2 + ', feather -> showName ' + name)

        // SN-43 open through: empty hand opens the chest
        closeMenu(p)
        let ro = clickPaper(p, level, cell, 'south', 0.5, 0.5, $CLItemStack.EMPTY, false)
        let m = menuName(p)
        closeMenu(p)
        check('SN-43', 'open through', m != 'InventoryMenu', 'empty hand ' + ro + ' -> ' + m)

        // SN-41 clear: sneak + empty hand
        clickPaper(p, level, cell, 'south', 0.5, 0.5, $CLItemStack.EMPTY, true)
        let cleared = faceOf(level, h, 'north').label.picture
        check('SN-41', 'clear', cleared == null || cleared.isEmpty(), 'picture after ' + cleared)

        // SN-44 break the label: drops a blank label (picture set again first)
        clickPaper(p, level, cell, 'south', 0.5, 0.5, stack('minecraft:diamond'), true)
        killItems(level, X)
        let target = facePoint(h, 'north', 0.5, 0.5)
        place(p, target.x(), target.y() - 1.62, target.z() - 1.5, 0, 0)
        p.lookAt($CLAnchor.EYES, target)
        p.setItemInHand($CLHand.MAIN_HAND, $CLItemStack.EMPTY)
        let ok = p.gameMode.destroyBlock(cell)
        let drops = dropSummary(level, X)
        check('SN-44', 'break', ok && drops.length == 1 && drops[0] == '1x creative_labels:label' && String(level.getBlockState(cell).getBlock()).indexOf('air') >= 0,
            'destroyBlock ' + ok + ', drops ' + drops.join(' | ') + ', cell ' + level.getBlockState(cell).getBlock())

        // SN-45 floor label turns to the placer's facing
        let hf = pos(X + 4, Y, 2)
        set(level, hf, 'minecraft:stone')
        place(p, X + 2.5, Y + 1, 2.5, -90, 30)   // facing east
        click(p, level, hf, 'up', 0.5, 0.5, stack('creative_labels:label'), false)
        check('SN-45', 'floor label', String(faceOf(level, hf, 'up').rotation) == 'east', 'rotation ' + faceOf(level, hf, 'up').rotation)
        p.discard()
    }

    function groupDonors(level) {
        // SN-49 donors off: no recipe makes or (for crafting) is a sticky_it:/labels: item; their items in no creative tab
        let rm = level.getServer().getRecipeManager()
        let all = rm.getRecipes()
        let makers = []
        let ours = { note: 0, label: 0, eraser: 0 }
        let it = all.iterator()
        while (it.hasNext()) {
            var rec = it.next()
            var out = rec.getResultItem()
            var id = out == null || out.isEmpty() ? '' : String($CLForgeReg.ITEMS.getKey(out.getItem()))
            if (id.indexOf('sticky_it:') == 0 || id.indexOf('labels:') == 0) makers.push(String(rec.getId()) + ' -> ' + id)
            if (id == 'creative_labels:sticky_note') ours.note++
            if (id == 'creative_labels:label') ours.label++
            if (id == 'creative_labels:eraser') ours.eraser++
        }
        check('SN-49', 'donor recipes gone', makers.length == 0, makers.length == 0 ? 'none; recipes making ours ' + JSON.stringify(ours) : makers.join(', '))
        let tabs = $CLCreativeTab.TABS
        let shown = []
        let donors = ['sticky_it:sticky_note', 'sticky_it:eraser', 'labels:label']
        for (let d = 0; d < donors.length; d++) {
            var di = item(donors[d])
            if (di == null || String($CLForgeReg.ITEMS.getKey(di)) == 'minecraft:air') { shown.push(donors[d] + ' missing'); continue }
            // allowedIn is protected; fillItemCategory is public and asks it first
            for (let t = 0; t < tabs.length; t++) {
                var lst = $CLNonNull.create()
                di.fillItemCategory(tabs[t], lst)
                if (lst.size() > 0) shown.push(donors[d] + ' in ' + tabs[t].getRecipeFolderName())
            }
        }
        let ourList = $CLNonNull.create()
        item('creative_labels:sticky_note').fillItemCategory($CLCreativeTab.TAB_DECORATIONS, ourList)
        let ourTab = ourList.size() == 5
        check('SN-49', 'donor items out of the tabs', shown.length == 0 && ourTab, (shown.length == 0 ? 'none shown' : shown.join(', ')) + '; our note lists ' + ourList.size() + ' entries in decorations (plain + 4 specials)')

        // SN-24 gold chance: 400 plain notes placed one by one -> about 4 gold (goldChance 0.01)
        let X = 160
        clearArea(level, X)
        let p = fake(level, 'gold')
        let gold = 0, placed = 0
        for (let i = 0; i < 400; i++) {
            var hp = pos(X + (i % 20), Y, Math.floor(i / 20) % 10)
            if (i % 200 == 0) {
                for (let x = 0; x < 20; x++) for (let z = 0; z < 10; z++) {
                    level.setBlockAndUpdate(pos(X + x, Y + 1, z), $CLBlocks.AIR.defaultBlockState())
                    level.setBlockAndUpdate(pos(X + x, Y, z), $CLBlocks.STONE.defaultBlockState())
                }
            }
            click(p, level, hp, 'up', 0.5, 0.5, note({ count: 1 }), false)
            var fc = faceOf(level, hp, 'up')
            if (fc != null) {
                for (let s = 0; s < 5; s++) if (fc.hasNote(s)) { placed++; if (String(fc.note(s).special) == 'gold') gold++ }
                level.setBlockAndUpdate(cellOf(hp, 'up'), $CLBlocks.AIR.defaultBlockState())
            }
        }
        check('SN-24', 'gold chance (server half)', placed == 400 && gold >= 0 && gold <= 14, gold + ' gold of ' + placed + ' placed (expect about 4)')
        p.discard()
    }

    function runGroup(name, level) {
        results = { pass: 0, fail: 0 }
        try {
            if (name == 'place') groupPlace(level)
            else if (name == 'actions') groupActions(level)
            else if (name == 'host') groupHost(level)
            else if (name == 'pistoncheck') pistonCheck(level)
            else if (name == 'items') groupItems(level)
            else if (name == 'labels') groupLabels(level)
            else if (name == 'donors') groupDonors(level)
            else console.info('[cltest] INFO unknown group ' + name)
        } catch (e) {
            log('FAIL', 'ERR', name, 'exception: ' + e + (e.javaException ? ' / ' + e.javaException : ''))
        }
        console.info('[cltest] DONE ' + name + ': ' + results.pass + ' pass, ' + results.fail + ' fail')
        return 1
    }

    ServerEvents.commandRegistry(event => {
        const Commands = Java.loadClass('net.minecraft.commands.Commands')
        const StringArg = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
        event.register(
            Commands.literal('cltest')
                .requires(src => src.hasPermission(2))
                .then(Commands.argument('group', StringArg.word())
                    .executes(ctx => runGroup(String(StringArg.getString(ctx, 'group')), ctx.source.getLevel())))
        )
    })
})()
