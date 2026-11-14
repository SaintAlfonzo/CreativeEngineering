// Scratch-only probe for the Create: Power Grid port, phase P10 rows (SQ-PWG-90 onward, plan_power_grid.md P10: copper
// plating, ceiling tiles, string lights). Copy into a scratch server's kubejs/server_scripts as zz_pwg_p10_probe.js next
// to the P2 to P9d probes (zpwg3: place, w, src, res; zpwg6: bulb, light, solar, solarjoin); never into the pack. Uses
// its own Forge fake player ("[zpwg10]", survival) for block use and the wrench. No chunk loads here: `forceload add`
// the area first (Rhino deadlock).
//   check                    P10 blocks, items, entity type, block entity types, the crafting_special_string_light_cord
//                            serializer; the P10 recipes and the two that waited for copper_plating (class, type,
//                            result, ingredients that resolve to no item); powergrid recipe count; mineable tags,
//                            tool checks and loot drops of the plating blocks (B33); the dye pattern recipe on test
//                            grids (B34)
//   attach X Y Z ITEM        [zpwg10] holds 2 ITEM and uses the block at X Y Z (CeilingTileBlock.use: factory light,
//                            wire connector, cord junction, solar panel make the four attachments)
//   helper X Y Z DIR         [zpwg10] holds 4 ceiling tiles and uses the ceiling block at X Y Z, hit near its DIR edge
//                            (east, west, north, south): the placement helper puts a tile next to it
//   wrench X Y Z [FACE]      [zpwg10] (inventory emptied) uses a Create wrench on the block at X Y Z, clicked FACE
//                            (default north): onWrenched; prints the block after and what the player got (B32)
//   drops X Y Z              loot of the block at X Y Z with its block entity and an iron pickaxe (B32: the lamp's bulb)
//   node X Y Z               terminal nodes of the electric block at X Y Z: voltage, saved value, index, network (SQ-PWG-94)
//   lamp X Y Z               ceiling tile lamp: state, bulb, temperature, emission, block light, terminals
//   solar X Y Z              ceiling tile solar: terminals, goggle lines, multiblock (controller, connected), sky light
//   solarjoin X Y Z X2 Y2 Z2 merge the ceiling solar multiblock at 2 into the one at X Y Z (what the wrench does)
//   cord X1 Y1 Z1 X2 Y2 Z2 N [DYE,DYE,...]   string light cord of N items (pattern from the dye names) between the
//                            blocks' terminals 0 and 1 (AutoCordEndpoint; a block that accepts cords gives its own end
//                            point, as a player's click at its centre)
//   lights X1 Y1 Z1 X2 Y2 Z2 string light cords between the two blocks: items, filament temperature, glow (x^2),
//                            pattern, overheated, the end voltages and the expected power per item, the light blocks
//                            on the line and in the box
//   rewire X1 Y1 Z1 X2 Y2 Z2 B30: makeWire() twice on each string light cord between the two blocks (a rebuild's rewire);
//                            nothing may change, and after a `cut` the far end must read 0 V (upstream left the old
//                            wires in the network, so it kept the source voltage)
//   cut X1 Y1 Z1 X2 Y2 Z2    discard the string light cords between the two blocks (as a wire cutter would remove
//                            them); then `lights` again
// Output: "[zpwg10] ..." lines in logs/latest.log, then "[zpwg10] done".
(() => {
    const $Args = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $FR = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $RL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $Pos = Java.loadClass('net.minecraft.core.BlockPos')
    const $Dir = Java.loadClass('net.minecraft.core.Direction')
    const $Hand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $Fake = Java.loadClass('net.minecraftforge.common.util.FakePlayerFactory')
    const $Profile = Java.loadClass('com.mojang.authlib.GameProfile')
    const $UUID = Java.loadClass('java.util.UUID')
    const $Compound = Java.loadClass('net.minecraft.nbt.CompoundTag')
    const $ItemStack = Java.loadClass('net.minecraft.world.item.ItemStack')
    const $GameType = Java.loadClass('net.minecraft.world.level.GameType')
    const $BHR = Java.loadClass('net.minecraft.world.phys.BlockHitResult')
    const $Vec3 = Java.loadClass('net.minecraft.world.phys.Vec3')
    const $AABB = Java.loadClass('net.minecraft.world.phys.AABB')
    const $UseOn = Java.loadClass('net.minecraft.world.item.context.UseOnContext')
    const $Block = Java.loadClass('net.minecraft.world.level.block.Block')
    const $BlockTags = Java.loadClass('net.minecraft.tags.BlockTags')
    const $LightLayer = Java.loadClass('net.minecraft.world.level.LightLayer')
    const $CraftingMenu = Java.loadClass('net.minecraft.world.inventory.CraftingMenu')
    const $CraftingContainer = Java.loadClass('net.minecraft.world.inventory.CraftingContainer')
    const $BEB = Java.loadClass('com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour')
    const $Goggles = Java.loadClass('com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation')
    const $List = Java.loadClass('java.util.ArrayList')
    const $Byte = Java.loadClass('java.lang.Byte')
    const PROFILE = new $Profile($UUID.fromString('5a7c0e57-0b0e-4a11-9d51-70e9a1c0f010'), '[zpwg10]')
    const P10_BLOCKS = ['copper_plating', 'copper_plating_slab', 'copper_plating_stairs', 'ceiling_tile', 'ceiling_tile_connector',
        'ceiling_tile_junction', 'ceiling_tile_lamp', 'ceiling_tile_solar', 'string_light_block']
    const P10_BES = ['ceiling_tile_lamp', 'ceiling_tile_connector', 'ceiling_tile_junction', 'ceiling_tile_solar']
    const P10_RECIPES = ['copper_plating_slab_from_copper_plating_stonecutting', 'copper_plating_stairs_from_copper_plating_stonecutting',
        'crafting/copper_plating_slab', 'crafting/copper_plating_stairs', 'crafting/string_light_cord_pattern',
        'deploying/ceiling_tile', 'deploying/copper_plating', 'deploying/string_light_cord',
        'crafting/circuit_design_table', 'crafting/solar_panel_bearing']
    let PG = null
    function pg() {
        if (PG === null) PG = {
            Electric: Java.loadClass('org.patryk3211.powergrid.electricity.base.ElectricBehaviour'),
            Fixture: Java.loadClass('org.patryk3211.powergrid.electricity.light.fixture.AbstractLightFixtureBlockEntity'),
            Lamp: Java.loadClass('org.patryk3211.powergrid.general.ceilingtile.lamp.CeilingTileLampBlockEntity'),
            Solar: Java.loadClass('org.patryk3211.powergrid.general.ceilingtile.solar.CeilingTileSolarBlockEntity'),
            Ceiling: Java.loadClass('org.patryk3211.powergrid.general.ceilingtile.CeilingBlock'),
            CordItem: Java.loadClass('org.patryk3211.powergrid.electricity.light.string.StringLightCordItem'),
            Cord: Java.loadClass('org.patryk3211.powergrid.electricity.light.string.StringLightCordEntity'),
            Recipe: Java.loadClass('org.patryk3211.powergrid.electricity.light.string.StringLightCordRecipe'),
            Auto: Java.loadClass('org.patryk3211.powergrid.electricity.wire.powercord.AutoCordEndpoint'),
            AcceptCord: Java.loadClass('org.patryk3211.powergrid.electricity.wire.powercord.IAcceptCord'),
            Casing: Java.loadClass('com.simibubi.create.content.decoration.encasing.CasingBlock'),
        }
        return PG
    }
    function out(s) { console.info('[zpwg10] ' + s) }
    function pos(x, y, z) { return new $Pos(x, y, z) }
    function fmt(x) { return (Math.round(x * 1000) / 1000) }
    function rl(s) { return s.indexOf(':') >= 0 ? new $RL(s) : new $RL('powergrid', s) }
    function item(id) { return $FR.ITEMS.getValue(rl(id)) }
    function idOf(stack) { return String($FR.ITEMS.getKey(stack.getItem())) }
    function blockId(state) { return String($FR.BLOCKS.getKey(state.getBlock())) }
    function stackLine(s) {
        if (s.isEmpty()) return 'empty'
        let t = s.save(new $Compound())
        let tag = t.contains('tag') ? ' tag=' + t.getCompound('tag') : ''
        return idOf(s) + ' x' + s.getCount() + tag
    }
    function prop(state, name) {
        let v = null
        // r1113 pwg17: an enum value prints its constant name (LAMP_ON); lower case as the blockstate shows it
        state.getProperties().forEach(p => { if (p.getName() == name) v = String(state.getValue(p)).toLowerCase() })
        return v
    }
    function fake(level) {
        let p = $Fake.get(level, PROFILE)
        p.setGameMode($GameType.SURVIVAL)
        return p
    }
    function inventoryLine(p) {
        let parts = []
        let inv = p.getInventory()
        for (let i = 0; i < inv.getContainerSize(); i++) { let s = inv.getItem(i); if (!s.isEmpty()) parts.push(stackLine(s)) }
        return parts.length ? parts.join(', ') : 'empty'
    }
    function terminals(be) {
        let eb = $BEB.get(be, pg().Electric.TYPE)
        if (eb == null) { out('terminals none'); return }
        let vs = []
        for (let i = 0; i < 4; i++) if (eb.hasTerminal(i)) vs.push('T' + i + '=' + fmt(eb.getTerminal(i).getVoltage()))
        out('terminals ' + vs.join(' '))
    }
    function termV(level, p, t) {
        let be = level.getBlockEntity(p)
        if (be == null) return null
        let eb = $BEB.get(be, pg().Electric.TYPE)
        if (eb == null || !eb.hasTerminal(t)) return null
        return eb.getTerminal(t).getVoltage()
    }
    function recipe(level, id) {
        let o = level.getRecipeManager().byKey(rl(id))
        return o.isPresent() ? o.get() : null
    }
    // getClass() is filtered by KubeJS: a short class name from toString
    function simpleName(o) { let s = String(o); let m = s.match(/([A-Za-z0-9_$.]+)@[0-9a-f]+$/); return m ? m[1].replace(/^.*[.$]/, '') : s }
    // BlockState.is / ItemStack.is(TagKey) are ambiguous in Rhino (r1113 pwg16): Forge's tag manager instead
    function blockInTag(st, key) {
        try { return $FR.BLOCKS.tags().getTag(key).contains(st.getBlock()) } catch (err) { return 'error ' + err }
    }
    // no BlockPos.relative / Direction.getStepX from Rhino (overloads, unreachable getters): offsets by name
    const STEP = { north: [0, -1], south: [0, 1], east: [1, 0], west: [-1, 0] }
    function beside(P, name) { let d = STEP[name]; return pos(P.getX() + d[0], P.getY(), P.getZ() + d[1]) }
    function check(level) {
        P10_BLOCKS.forEach(id => {
            let b = $FR.BLOCKS.getValue(rl(id))
            let reg = b != null && String($FR.BLOCKS.getKey(b)) == 'powergrid:' + id
            let it = item(id)
            let hasItem = it != null && String($FR.ITEMS.getKey(it)) == 'powergrid:' + id
            out('block ' + id + ' registered=' + reg + ' item=' + hasItem + (reg ? ' class=' + simpleName(b) : ''))
        })
        let ci = item('string_light_cord')
        out('item string_light_cord ' + (ci == null ? 'MISSING' : 'cordItem=' + (ci instanceof pg().CordItem)))
        let et = $FR.ENTITY_TYPES.getValue(rl('string_light_cord'))
        out('entity string_light_cord registered=' + (et != null && String($FR.ENTITY_TYPES.getKey(et)) == 'powergrid:string_light_cord'))
        P10_BES.forEach(id => {
            let t = $FR.BLOCK_ENTITY_TYPES.getValue(rl(id))
            out('block entity ' + id + ' registered=' + (t != null && String($FR.BLOCK_ENTITY_TYPES.getKey(t)) == 'powergrid:' + id))
        })
        let ser = $FR.RECIPE_SERIALIZERS.getValue(rl('crafting_special_string_light_cord'))
        out('serializer powergrid:crafting_special_string_light_cord ' + (ser == null ? 'MISSING' : 'ok ' + (ser === pg().Recipe.SERIALIZER)))
        P10_RECIPES.forEach(id => {
            let r = recipe(level, id)
            if (r == null) { out('recipe ' + id + ' MISSING'); return }
            let empty = [], ings = r.getIngredients()
            // r1113 pwg17: Ingredient.getItems() is hidden in this Rhino build (getStacks), and no getType() on recipes
            // (AbstractMethodError on a foreign recipe escapes the catch; KubeJS remaps it to the serializer id anyway)
            for (let i = 0; i < ings.size(); i++) { let ing = ings.get(i); if (ing.getStacks().toList().size() == 0) empty.push(i) }
            let ser = null
            try { ser = $FR.RECIPE_SERIALIZERS.getKey(r.getSerializer()) } catch (err) { ser = '?' }
            out('recipe ' + id + ' class=' + simpleName(r) + ' serializer=' + ser + ' result=' + stackLine(r.getResultItem())
                + ' ingredients=' + r.getIngredients().size() + ' unresolved=[' + empty.join(',') + ']')
        })
        let n = 0
        level.getRecipeManager().getRecipes().forEach(r => { if (String(r.getId().getNamespace()) == 'powergrid') n++ })
        out('recipes powergrid ' + n)
        // B33: tool tags, tool checks and loot of the plating blocks
        let pick = new $ItemStack(item('minecraft:iron_pickaxe'), 1)
        let p = fake(level)
        for (let id of ['copper_plating', 'copper_plating_slab', 'copper_plating_stairs', 'ceiling_tile']) {
            let st = $FR.BLOCKS.getValue(rl(id)).defaultBlockState()
            out('tool ' + id + ' pickaxeTag=' + blockInTag(st, $BlockTags.MINEABLE_WITH_PICKAXE) + ' axeTag=' + blockInTag(st, $BlockTags.MINEABLE_WITH_AXE)
                + ' needsTool=' + st.requiresCorrectToolForDrops() + ' ironPickaxeCorrect=' + pick.isCorrectToolForDrops(st))
        }
        let slab = $FR.BLOCKS.getValue(rl('copper_plating_slab')).defaultBlockState()
        for (let type of ['bottom', 'top', 'double']) {
            let st = slab
            st.getProperties().forEach(pr => { if (pr.getName() == 'type') st = st.setValue(pr, pr.getValue(type).get()) })
            out('loot copper_plating_slab type=' + type + ' -> ' + dropsLine(level, st, pos(0, 0, 0), null, p, pick))
        }
        // B34 and the pattern recipe
        grid(level, p, ['string_light_cord', 'minecraft:red_dye', 'minecraft:green_dye', 'minecraft:blue_dye'])
        grid(level, p, ['string_light_cord'])
        grid(level, p, ['string_light_cord', 'string_light_cord'])
        grid(level, p, ['string_light_cord', 'minecraft:stick'])
        grid(level, p, ['minecraft:red_dye'])
        grid(level, p, ['string_light_cord', 'minecraft:yellow_dye', 'minecraft:yellow_dye', 'minecraft:purple_dye'])
    }
    function grid(level, p, ids) {
        let r = recipe(level, 'crafting/string_light_cord_pattern')
        if (r == null) { out('grid: recipe missing'); return }
        let menu = new $CraftingMenu(0, p.getInventory())
        let c = new $CraftingContainer(menu, 3, 3)
        ids.forEach((id, i) => c.setItem(i, new $ItemStack(item(id), 1)))
        let m = r['matches(net.minecraft.world.inventory.CraftingContainer,net.minecraft.world.level.Level)'](c, level)
        let res = m ? stackLine(r['assemble(net.minecraft.world.inventory.CraftingContainer)'](c)) : '-'
        out('grid [' + ids.join(' ') + '] matches=' + m + ' result=' + res)
    }
    function dropsLine(level, st, P, be, p, tool) {
        let parts = []
        try {
            $Block.getDrops(st, level, P, be, p, tool).forEach(s => parts.push(stackLine(s)))
        } catch (err) { return 'error ' + err }
        return parts.length ? parts.join(', ') : 'nothing'
    }
    function hitAt(P, face, dx, dz) {
        return new $BHR(new $Vec3(P.getX() + 0.5 + dx, P.getY() + 0.0625, P.getZ() + 0.5 + dz), face, P, false)
    }
    function attach(level, P, id) {
        let p = fake(level)
        p.getInventory().clearContent()
        p.setItemInHand($Hand.MAIN_HAND, new $ItemStack(item(id), 2))
        let before = blockId(level.getBlockState(P))
        let res
        try { res = level.getBlockState(P).use(level, p, $Hand.MAIN_HAND, hitAt(P, $Dir.DOWN, 0, 0)) } catch (err) { res = 'error ' + err }
        out('attach ' + id + ' result=' + res + ' block ' + before + ' -> ' + blockId(level.getBlockState(P))
            + ' state=' + level.getBlockState(P) + ' hand=' + stackLine(p.getMainHandItem()))
    }
    function helper(level, P, dir) {
        let p = fake(level)
        p.getInventory().clearContent()
        p.setItemInHand($Hand.MAIN_HAND, new $ItemStack(item('ceiling_tile'), 4))
        let d = { east: [0.45, 0], west: [-0.45, 0], south: [0, 0.45], north: [0, -0.45] }[dir] || [0.45, 0]
        let res
        try { res = level.getBlockState(P).use(level, p, $Hand.MAIN_HAND, hitAt(P, $Dir.DOWN, d[0], d[1])) } catch (err) { res = 'error ' + err }
        let around = []
        for (let n of ['north', 'south', 'east', 'west']) around.push(n + '=' + blockId(level.getBlockState(beside(P, n))))
        out('helper ' + dir + ' result=' + res + ' ' + around.join(' ') + ' hand=' + stackLine(p.getMainHandItem()))
    }
    function wrench(level, P, face) {
        let p = fake(level)
        p.getInventory().clearContent()
        p.setItemInHand($Hand.MAIN_HAND, new $ItemStack(item('create:wrench'), 1))
        let st = level.getBlockState(P)
        let fname = STEP[face] ? face : 'north'
        let f = $Dir.byName(fname)
        let hit = new $BHR(new $Vec3(P.getX() + 0.5 + STEP[fname][0] * 0.5, P.getY() + 0.0625, P.getZ() + 0.5 + STEP[fname][1] * 0.5), f, P, false)
        let res
        try { res = st.getBlock().onWrenched(st, new $UseOn(p, $Hand.MAIN_HAND, hit)) } catch (err) { res = 'error ' + err }
        // r1113 pwg17: the wrench itself is not part of "got" (inventoryLine listed create:wrench x1 first)
        p.setItemInHand($Hand.MAIN_HAND, $ItemStack.EMPTY)
        out('wrench ' + blockId(st) + ' face=' + f + ' result=' + res + ' -> ' + blockId(level.getBlockState(P)) + ' got: ' + inventoryLine(p))
    }
    function lamp(level, P) {
        let be = level.getBlockEntity(P)
        let st = level.getBlockState(P)
        if (be == null || !(be instanceof pg().Lamp)) { out('lamp none at ' + P + ' (' + blockId(st) + ')'); return }
        let tag = be.saveWithoutMetadata()
        out('lamp state=' + prop(st, 'state') + ' bulb=' + (tag.contains('Bulb') ? tag.getString('Bulb') : 'none')
            + ' T=' + fmt(tag.getFloat('Temperature')) + ' burned=' + tag.getBoolean('Burned') + ' emission=' + st.getLightEmission()
            + ' blockLight=' + level.getBrightness($LightLayer.BLOCK, P) + ' below=' + level.getBrightness($LightLayer.BLOCK, P.below()))
        terminals(be)
    }
    // SQ-PWG-94 (r1113 pwg17: the ceiling connector read T0=0 with 1.2 A through it): each terminal node's voltage, saved
    // value, matrix index and network
    function node(level, P) {
        let be = level.getBlockEntity(P)
        let eb = be == null ? null : $BEB.get(be, pg().Electric.TYPE)
        if (eb == null) { out('node none at ' + P + ' (' + blockId(level.getBlockState(P)) + ')'); return }
        for (let i = 0; i < 4; i++) {
            if (!eb.hasTerminal(i)) continue
            let n = eb.getTerminal(i)
            let net = null, idx = '?', saved = '?'
            try { net = n.getNetwork() } catch (err) { net = null }
            try { idx = n.getIndex() } catch (err) { idx = 'error' }
            try { saved = fmt(n.getSavedValue()) } catch (err) { saved = 'error' }
            out('node ' + blockId(level.getBlockState(P)) + ' T' + i + ' V=' + fmt(n.getVoltage()) + ' saved=' + saved + ' index=' + idx
                + ' network=' + (net == null ? 'none' : simpleName(net) + ' nodes=' + net.getNodes().size()) + ' paused=' + eb.isPaused())
        }
    }
    function solar(level, P) {
        let be = level.getBlockEntity(P)
        if (be == null || !(be instanceof pg().Solar)) { out('solar none at ' + P + ' (' + blockId(level.getBlockState(P)) + ')'); return }
        let tag = be.saveWithoutMetadata()
        let ctl = be.getController()
        out('solar ceiling controller=' + (ctl.isPresent() ? ctl.get().getBlockPos() : 'none')
            + ' connected=' + (tag.contains('Connected') ? tag.getList('Connected', 10).size() : 0)
            + ' sky=' + level.getBrightness($LightLayer.SKY, P.above()) + ' keys=' + tag.getAllKeys())
        terminals(be)
        if (be instanceof $Goggles) {
            let lines = new $List()
            try { be.addToGoggleTooltip(lines, true); lines.forEach(c => out('goggles ' + String(c.getString()).trim())) }
            catch (err) { out('goggles skipped ' + err) }
        }
    }
    function endpoint(level, P) {
        let st = level.getBlockState(P)
        let b = st.getBlock()
        if (b instanceof pg().AcceptCord) {
            try {
                let p = fake(level)
                let e = b.getEndpoint(new $UseOn(p, $Hand.MAIN_HAND, new $BHR(new $Vec3(P.getX() + 0.5, P.getY() + 0.5, P.getZ() + 0.5), $Dir.DOWN, P, false)))
                if (e != null) return e
            } catch (err) { out('endpoint ' + blockId(st) + ' getEndpoint error ' + err) }
        }
        return new (pg().Auto)(P, 0, 1, new $Vec3(P.getX() + 0.5, P.getY() + 0.5, P.getZ() + 0.5), null)
    }
    function cord(level, A, B, n, dyes) {
        let stack = new $ItemStack(item('string_light_cord'), n)
        if (dyes) {
            let names = dyes.split(','), bytes = []
            let all = ['white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray', 'light_gray', 'cyan', 'purple', 'blue', 'brown', 'green', 'red', 'black']
            names.forEach(d => bytes.push(all.indexOf(d)))
            let t = new $Compound()
            let arr = new $List()
            bytes.forEach(b => arr.add($Byte.valueOf(String(b))))
            t.putByteArray('Pattern', arr)
            // r1113 pwg17: ItemStack.setTag is not visible from Rhino; getOrCreateTag works
            stack.getOrCreateTag().put('Pattern', t.get('Pattern'))
        }
        let e
        try { e = pg().Cord.create(level, endpoint(level, A), endpoint(level, B), stack, null) } catch (err) { out('cord create error ' + err); return }
        let ok = level.addFreshEntity(e)
        // r1113 pwg17: no getUUID() from Rhino on the entity (KubeJS wrapper): left out
        out('cord added=' + ok + ' items=' + n + ' length=' + fmt(Math.sqrt(A.distSqr(B))) + ' pattern=' + (dyes || 'none'))
    }
    function cordsBetween(level, A, B) {
        let box = new $AABB(Math.min(A.getX(), B.getX()) - 1, Math.min(A.getY(), B.getY()) - 2, Math.min(A.getZ(), B.getZ()) - 1,
            Math.max(A.getX(), B.getX()) + 2, Math.max(A.getY(), B.getY()) + 2, Math.max(A.getZ(), B.getZ()) + 2)
        return [level.getEntitiesOfClass(pg().Cord, box), box]
    }
    function lights(level, A, B) {
        let cb = cordsBetween(level, A, B)
        let cords = cb[0], box = cb[1]
        out('cords ' + cords.size())
        cords.forEach(c => {
            let t = c.saveWithoutId(new $Compound())
            let T = t.getFloat('Filament')
            let x = Math.min(Math.max((T - 600) / 800, 0), 1)
            let pat = 'none'
            if (t.contains('Pattern')) { let arr = t.getIntArray('Pattern'), hs = []; for (let i = 0; i < arr.length; i++) hs.push((arr[i] & 0xffffff).toString(16)); pat = '[' + hs.join(' ') + ']' }
            out('cord filament=' + fmt(T) + ' glow=' + fmt(x * x) + ' lit=' + (x * x > 0.75) + ' overheated=' + c.isOverheated()
                + ' removed=' + c.isRemoved() + ' pattern=' + pat + ' keys=' + t.getAllKeys())
        })
        let n = 0
        for (let x = box.minX; x < box.maxX; x++) for (let y = box.minY; y < box.maxY; y++) for (let z = box.minZ; z < box.maxZ; z++)
            if (blockId(level.getBlockState(pos(x, y, z))) == 'powergrid:string_light_block') n++
        out('light blocks in box=' + n)
        for (let P of [A, B]) {
            let v0 = termV(level, P, 0), v1 = termV(level, P, 1)
            if (v0 != null && v1 != null) out('end ' + P.getX() + ' ' + P.getY() + ' ' + P.getZ() + ' V=' + fmt(v0 - v1))
        }
    }
    function cut(level, A, B) {
        let cords = cordsBetween(level, A, B)[0]
        let n = 0
        cords.forEach(c => { c.discard(); n++ })
        out('cut ' + n)
    }
    function run(level, a) {
        let cmd = a[0]
        if (cmd == 'check') { check(level); return }
        let P = pos(parseInt(a[1]), parseInt(a[2]), parseInt(a[3]))
        if (cmd == 'attach') attach(level, P, a[4])
        else if (cmd == 'helper') helper(level, P, a[4])
        else if (cmd == 'wrench') wrench(level, P, a[4])
        else if (cmd == 'drops') {
            let st = level.getBlockState(P)
            out('drops ' + blockId(st) + ' -> ' + dropsLine(level, st, P, level.getBlockEntity(P), fake(level), new $ItemStack(item('minecraft:iron_pickaxe'), 1)))
        } else if (cmd == 'node') node(level, P)
        else if (cmd == 'lamp') lamp(level, P)
        else if (cmd == 'solar') solar(level, P)
        else if (cmd == 'solarjoin') {
            let a1 = level.getBlockEntity(P), a2 = level.getBlockEntity(pos(parseInt(a[4]), parseInt(a[5]), parseInt(a[6])))
            if (!(a1 instanceof pg().Solar) || !(a2 instanceof pg().Solar)) out('solarjoin: both must be ceiling tile solars')
            else {
                let c1 = a1.getController(), c2 = a2.getController()
                if (!c1.isPresent() || !c2.isPresent()) out('solarjoin: no controller')
                else { pg().Solar.mergeMultiblock(c1.get(), c2.get()); out('solarjoin ok') }
            }
        } else {
            let B = pos(parseInt(a[4]), parseInt(a[5]), parseInt(a[6]))
            if (cmd == 'cord') cord(level, P, B, parseInt(a[7]), a[8])
            else if (cmd == 'lights') lights(level, P, B)
            else if (cmd == 'cut') cut(level, P, B)
            else if (cmd == 'rewire') {
                // B30: makeWire again, as ElectricBehaviour does when an endpoint's connections are rebuilt
                let cords = cordsBetween(level, P, B)[0]
                let n = 0
                cords.forEach(c => { c.makeWire(); c.makeWire(); n++ })
                out('rewire ' + n + ' (makeWire twice each)')
            }
            else out('unknown subcommand ' + cmd)
        }
    }
    ServerEvents.commandRegistry(e => {
        const C = e.commands
        e.register(C.literal('zpwg10').requires(s => s.hasPermission(2))
            .then(C.argument('args', $Args.greedyString()).executes(ctx => {
                try {
                    run(ctx.source.getLevel(), String($Args.getString(ctx, 'args')).split(' '))
                } catch (err) { out('error ' + err) }
                out('done')
                return 1
            })))
    })
})()
