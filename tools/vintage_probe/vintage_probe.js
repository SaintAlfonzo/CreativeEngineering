// Vintage Delight / Veggies Delight ports scratch-server probe (scratch queue SQ-VP-02 to SQ-VP-12;
// feature_writeup/vintage_delight.md 8 (VTD-), veggies_delight.md 8 (VD-), can_parity.md CP-27 to CP-30).
// Scratch-only: copy into a scratch server's kubejs/server_scripts as zz_vtd_probe.js
// (tools/creative_labels_test/scratch_server.sh setup, then cp), never into the pack.
//
//   /vtdtest <part>   part = recipes | cooking | tags | compost | fluids | fluidsrun | pickling | crops | mobs | loot
//                            | stacks | effects | all
// Results are "[vtdtest] PASS|FAIL <part> <name>: <detail>" lines in logs/latest.log, NOTE lines for record-only
// facts, then "[vtdtest] done <part>: N pass, M fail". Same style as tools/food_drinks_probe (/fdtest).
//
// Probe blocks live at x 200..300, z 20..40, Y -60 (superflat, -61 is grass; crop survival checks sit at the build
// limit, so `crops` also passes in a normal world). Not covered here: trades (real
// villagers after a restart), the same compost table after /reload (run /vtdtest compost again after /reload),
// Quark troughs, hoe area harvest, VD-18 stack policy (after CQ-DUMP; `stacks` only records), exported/loot scan.
(() => {
    const $VFake = Java.loadClass('net.minecraftforge.common.util.FakePlayerFactory')
    const $VProfile = Java.loadClass('com.mojang.authlib.GameProfile')
    const $VUUID = Java.loadClass('java.util.UUID')
    const $VDir = Java.loadClass('net.minecraft.core.Direction')
    const $VBlockPos = Java.loadClass('net.minecraft.core.BlockPos')
    const $VVec3 = Java.loadClass('net.minecraft.world.phys.Vec3')
    const $VAABB = Java.loadClass('net.minecraft.world.phys.AABB')
    const $VHit = Java.loadClass('net.minecraft.world.phys.BlockHitResult')
    const $VHand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $VReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $VRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $VComposter = Java.loadClass('net.minecraft.world.level.block.ComposterBlock')
    const $VSpawn = Java.loadClass('net.minecraft.world.entity.MobSpawnType')
    const $VCaps = Java.loadClass('net.minecraftforge.common.capabilities.ForgeCapabilities')
    const $VFluidStack = Java.loadClass('net.minecraftforge.fluids.FluidStack')
    const $VMobEffect = Java.loadClass('net.minecraft.world.effect.MobEffect')
    const $VPotionUtils = Java.loadClass('net.minecraft.world.item.alchemy.PotionUtils')
    const $VBrewing = Java.loadClass('net.minecraft.world.item.alchemy.PotionBrewing')
    const $VBrewReg = Java.loadClass('net.minecraftforge.common.brewing.BrewingRecipeRegistry')
    const $VItemEntity = Java.loadClass('net.minecraft.world.entity.item.ItemEntity')

    const Y = -60
    const VT = 'vintagedelight:', VG = 'veggiesdelight:', C = 'creativeengineering:'
    // the pack's food recipes are under C or, since the move into Creative Delight (2026-10-25), CDN
    const CDN = 'creativedelight:'
    function ours(id, path) { return id.indexOf(C + path) == 0 || id.indexOf(CDN + path) == 0 }
    let pass = 0, fail = 0, part = ''
    function check(name, ok, detail) {
        if (ok) pass++; else fail++
        console.info('[vtdtest] ' + (ok ? 'PASS' : 'FAIL') + ' ' + part + ' ' + name + ': ' + detail)
    }
    function note(name, detail) { console.info('[vtdtest] NOTE ' + part + ' ' + name + ': ' + detail) }
    function rl(id) { return new $VRL(id) }
    function item(id) { return $VReg.ITEMS.getValue(rl(id)) }
    function itemExists(id) { let it = item(id); return it != null && String($VReg.ITEMS.getKey(it)) == id }
    function itemId(st) { return String($VReg.ITEMS.getKey(st.getItem())) }
    function block(id) { return $VReg.BLOCKS.getValue(rl(id)) }
    function blockId(state) { return String($VReg.BLOCKS.getKey(state.getBlock())) }
    function at(x, y, z) { return new $VBlockPos(x, y, z) }
    function set(level, pos, id) { level.setBlockAndUpdate(pos, block(id).defaultBlockState()) }

    // ---- recipes: id -> recipe, built once per command (never getType() on every recipe)
    let byId = null, aliasId = {}
    function recipes(server) {
        if (byId) return byId
        byId = {}; aliasId = {}
        server.getRecipeManager().getRecipes().forEach(r => { byId[String(r.getId())] = r })
        // Food recipes moved into Creative Delight's tree on 2026-10-25 (creativedelight: ids, same paths): a
        // creativeengineering: id the checks name also finds its creativedelight: twin
        let aliased = 0
        Object.keys(byId).forEach(id => {
            if (id.indexOf('creativedelight:') != 0) return
            let old = 'creativeengineering:' + id.substring('creativedelight:'.length)
            if (byId[old] == null) { aliasId[old] = byId[id]; aliased++ }
        })
        note('recipe ids aliased from creativedelight:', aliased)
        return byId
    }
    function ingredientsOf(r) {
        let out = []
        try { r.getIngredients().forEach(g => out.push(g)) } catch (e) { }
        return out
    }
    function resultOf(r) {
        try { let s = r.getResultItem(); return s == null || s.isEmpty() ? '' : itemId(s) + ' x' + s.getCount() } catch (e) { return '?' }
    }
    function rget(server, id) { let r = recipes(server)[id]; return r != null ? r : aliasId[id] }
    function has(server, id) { return rget(server, id) != null }
    function expectIds(server, ids) {
        ids.forEach(id => check('recipe ' + id, has(server, id), has(server, id) ? resultOf(rget(server, id)) : 'not loaded'))
    }
    function expectGone(server, ids) {
        ids.forEach(id => check('removed ' + id, !has(server, id), has(server, id) ? 'still loaded' : 'gone'))
    }
    function takes(server, id, itemIdStr) {
        let r = rget(server, id)
        if (!r) return false
        return ingredientsOf(r).some(g => g.test(Item.of(itemIdStr)))
    }
    // ids (any namespace) whose id passes `idTest` and whose result is outId
    function idsWhere(server, idTest, outId) {
        return Object.keys(recipes(server)).filter(id => idTest(id) && resultOf(rget(server, id)).indexOf(outId + ' ') == 0)
    }
    function idsMaking(server, prefix, outId) { return idsWhere(server, id => id.indexOf(prefix) == 0, outId) }
    function firstOfTag(tagId) {
        let s = Ingredient.of('#' + tagId).getStacks()
        return s.size() ? itemId(s.toList().get(0)) : null
    }

    // ---- tags (fungi probe helpers)
    function tagHas(reg, tagId, id) {
        let tags = reg.tags(), obj = reg.getValue(rl(id))
        return obj != null && tags.getTag(tags.createTagKey(rl(tagId))).contains(obj)
    }
    function tagList(reg, tagId) {
        let tags = reg.tags(), out = []
        tags.getTag(tags.createTagKey(rl(tagId))).forEach(o => out.push(String(reg.getKey(o))))
        return out.sort()
    }
    function sameSet(a, b) { return a.length == b.length && a.every(x => b.indexOf(x) >= 0) }

    // ---- players and entities
    let fakeN = 0
    function fake(level) {
        fakeN++
        let p = $VFake.get(level, new $VProfile($VUUID.randomUUID(), 'vtdprobe' + fakeN))
        p.getInventory().clearContent()
        return p
    }
    function click(p, level, pos, st) {
        p.setItemInHand($VHand.MAIN_HAND, st)
        let hit = new $VHit(new $VVec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), $VDir.UP, pos, false)
        return String(p.gameMode.useItemOn(p, level, p.getMainHandItem(), $VHand.MAIN_HAND, hit))
    }
    function count(p, id) { return p.getInventory().countItem(item(id)) }
    function invList(p) {
        let out = [], inv = p.getInventory()
        for (let k = 0; k < inv.getContainerSize(); k++) { let s = inv.getItem(k); if (!s.isEmpty()) out.push(itemId(s) + ' x' + s.getCount()) }
        return out
    }
    function spawn(level, id, x, z) {
        let e = $VReg.ENTITY_TYPES.getValue(rl(id)).create(level)
        e.setPos(x + 0.5, Y, z + 0.5)
        try { e.finalizeSpawn(level, level.getCurrentDifficultyAt(e.blockPosition()), $VSpawn.COMMAND, null, null) } catch (err) { }
        level.addFreshEntity(e)
        return e
    }
    // item entities in a box around pos (counted, then discarded)
    function grabDrops(level, pos, r) {
        let box = new $VAABB(pos.getX() - r, pos.getY() - r, pos.getZ() - r, pos.getX() + 1 + r, pos.getY() + 1 + r, pos.getZ() + 1 + r)
        let list = null
        try { list = level.getEntitiesOfClass($VItemEntity, box) } catch (e) { list = level.getEntities(null, box) }
        let out = {}
        list.forEach(en => {
            if (String(en.getEncodeId()) != 'minecraft:item') return
            let s = en.getItem()
            out[itemId(s)] = (out[itemId(s)] || 0) + s.getCount()
            en.discard()
        })
        return out
    }
    function fmt(map) { return Object.keys(map).map(k => k + ' x' + map[k]).join(', ') || 'nothing' }

    // ================================================================ recipes (SQ-VP-02: cans CP-27 to CP-30)
    let canTypes = null
    function cans() {
        if (canTypes) return canTypes
        try { canTypes = JSON.parse(String(JsonIO.readString('kubejs/generation_scripts/food/can_types.json'))).cans } catch (e) { canTypes = {} }
        return canTypes
    }
    // a can recipe: creativeengineering:shapeless/<key><suffix>, or the _manual_only form
    function canRecipeId(server, key, suffix) {
        let base = C + 'shapeless/' + key + suffix
        if (has(server, base)) return base
        if (has(server, base + '_manual_only')) return base + '_manual_only'
        return null
    }
    function expectCan(server, key, suffix, mustTake) {
        let id = canRecipeId(server, key, suffix), out = global.item[key]
        let ok = id != null && resultOf(rget(server, id)).indexOf(out + ' ') == 0
        let detail = id == null ? 'not loaded' : id + ' -> ' + resultOf(rget(server, id))
        if (ok && mustTake) mustTake.forEach(t => { if (!takes(server, id, t)) { ok = false; detail += ', refuses ' + t } })
        check('can ' + key + (suffix || ' (plain)'), ok, detail)
        return id
    }
    function recipeIds(server) {
        let manual = 0, plain = 0
        function tally(id) { if (id == null) return; if (id.endsWith('_manual_only')) manual++; else plain++ }
        // CP-27 raw veggie cans: plain, _salt, _vinegar
        ;[['canned_broccoli', [VG + 'broccoli']], ['canned_cauliflower', [VG + 'cauliflower', VG + 'cauliflower_floret']],
            ['canned_sweet_potato', [VG + 'sweet_potato']], ['canned_turnip', [VG + 'turnip']],
            ['canned_zucchini', [VG + 'zucchini', VG + 'zucchini_slice']]].forEach(c => {
            tally(expectCan(server, c[0], '', c[1]))
            tally(expectCan(server, c[0], '_salt', [c[1][0]]))
            tally(expectCan(server, c[0], '_vinegar', [c[1][0], VT + 'vinegar_bottle']))
        })
        // CP-28 cooked veggies (+ _salt), pastas, breads, salads (+ _vinegar), soups (+ _broth)
        ;['canned_baked_sweet_potato', 'canned_roasted_zucchini', 'canned_roasted_cauliflower'].forEach(k => {
            tally(expectCan(server, k, '')); tally(expectCan(server, k, '_salt'))
        })
        ;['canned_pasta_with_broccoli', 'canned_potato_noodles', 'canned_cauliflower_bread', 'canned_garlic_bread', 'canned_mhadjeb']
            .forEach(k => tally(expectCan(server, k, '')))
        ;['canned_broccoli_salad', 'canned_cesar_salad', 'canned_coleslaw', 'canned_turnip_salad'].forEach(k => {
            tally(expectCan(server, k, '')); tally(expectCan(server, k, '_vinegar', [VT + 'vinegar_bottle']))
        })
        ;['canned_broccoli_soup', 'canned_cacciatore', 'canned_cauliflower_soup', 'canned_garlic_chicken_stew', 'canned_turnip_beef_stew']
            .forEach(k => { tally(expectCan(server, k, '')); tally(expectCan(server, k, '_broth')) })
        // CP-29 sweet potato pie filling: 4 slices, the pot (2 baked + 2 sugar + can), filling + crust by hand and oven
        tally(expectCan(server, 'canned_sweet_potato_pie_filling', '', [VG + 'sweet_potato_pie_slice']))
        let slices = 0, fr = rget(server, canRecipeId(server, 'canned_sweet_potato_pie_filling', '') || '-')
        if (fr) ingredientsOf(fr).forEach(g => { if (g.test(Item.of(VG + 'sweet_potato_pie_slice'))) slices++ })
        check('filling takes 4 slices', slices == 4, slices + ' slots')
        let pot = C + 'farmersdelightcooking/canned_sweet_potato_pie_filling_pot'
        check('filling in the Cooking Pot', has(server, pot) && takes(server, pot, VG + 'baked_sweet_potato') && takes(server, pot, 'minecraft:sugar'),
            has(server, pot) ? resultOf(rget(server, pot)) + ', baked ' + takes(server, pot, VG + 'baked_sweet_potato') + ', sugar ' +
                takes(server, pot, 'minecraft:sugar') : 'not loaded')
        if (has(server, pot)) {
            let ins = ingredientsOf(rget(server, pot)).map(g => { let s = g.getStacks(); return s.size() ? itemId(s.toList().get(0)) : '-' })
            note('pot filling inputs', ins.join(', '))
            try { note('pot filling container', String(rget(server, pot).getOutputContainer())) } catch (e) { }
        }
        expectIds(server, [C + 'shapeless/canned_sweet_potato_pie_filling_pie_manual_only', C + 'baking/sweet_potato_pie_from_filling',
            C + 'baking/sweet_potato_pie'])
        note('can recipe id forms (plain can recipes carry no _manual_only in the 1.1.01 dump)', manual + ' _manual_only, ' + plain + ' plain')
        // CP-30: every can with a _vinegar recipe in can_types.json has it loaded, taking the pack vinegar
        let ct = cans(), vin = Object.keys(ct).filter(k => ct[k].recipes.some(r => r.suffix == '_vinegar'))
        let missing = [], refuses = []
        vin.forEach(k => {
            let id = canRecipeId(server, k, '_vinegar')
            if (id == null) missing.push(k)
            else if (!takes(server, id, VT + 'vinegar_bottle')) refuses.push(k)
        })
        check('can_types.json read', vin.length > 0, vin.length + ' cans with a _vinegar recipe')
        check('every _vinegar can recipe loaded', vin.length > 0 && missing.length == 0, missing.join(', ') || 'all ' + vin.length)
        check('every _vinegar can recipe takes the vinegar bottle', refuses.length == 0, refuses.join(', ') || 'all')
        let loadedVin = Object.keys(recipes(server)).filter(id => id.indexOf(C + 'shapeless/canned_') == 0 && /_vinegar(_manual_only)?$/.test(id))
        note('_vinegar can recipes loaded (queue says 66)', loadedVin.length + ' loaded, ' + vin.length + ' in can_types.json')
        ;['canned_carrot', 'canned_apple', 'canned_raw_beef', 'canned_raw_cod', 'canned_mixed_salad'].forEach(k => {
            let id = canRecipeId(server, k, '_vinegar')
            check('CP-30 ' + k + '_vinegar', id != null, id || 'not loaded (key may differ)')
        })
        // Vintage cans wait for CQ-DUMP (AWAIT_DUMP): no can recipe takes the Vintage sources yet
        let vtdSrc = [VT + 'kimchi', VT + 'pickle', VT + 'salted_cod', VT + 'salted_salmon']
        let early = []
        Object.keys(recipes(server)).filter(id => id.indexOf(C + 'shapeless/canned_') == 0).forEach(id =>
            vtdSrc.forEach(s => { if (takes(server, id, s)) early.push(id + ' takes ' + s) }))
        check('Vintage cans not present yet (AWAIT_DUMP)', early.length == 0, early.join(', ') || 'none')
    }

    // ================================================================ cooking (SQ-VP-03: VTD-48 to VTD-52, VD-20, VD-24)
    function machineRoutes(server, outId) {
        return idsWhere(server, id => id.indexOf('mixing') >= 0 || id.indexOf('createcrafting') >= 0 || id.indexOf('mechanical_crafting') >= 0, outId)
    }
    function assemblies(server, outId) { return idsWhere(server, id => id.indexOf('sequenced_assembly') >= 0, outId) }
    function cooking(server) {
        // VTD-48 burgers
        expectGone(server, [VT + 'cheese_burger', VT + 'deluxe_burger', VG + 'compat/vintagedelight/cheese_burger',
            VG + 'compat/vintagedelight/deluxe_burger'])
        ;['cheese_burger', 'deluxe_burger'].forEach(n => {
            let id = C + 'shaped/' + n + '_manual_only'
            check(n + ' shaped', has(server, id), has(server, id) ? resultOf(rget(server, id)) : 'not loaded')
            check(n + ' takes the cheese slice', takes(server, id, VT + 'cheese_slice'), '')
            check(n + ' takes beef or vegetarian patty', takes(server, id, 'farmersdelight:beef_patty') && takes(server, id, VG + 'cooked_vegetarian_patty'),
                'beef ' + takes(server, id, 'farmersdelight:beef_patty') + ', veg ' + takes(server, id, VG + 'cooked_vegetarian_patty'))
            let asm = assemblies(server, VT + n)
            let asmId = C + 'createsequencedassembly/' + n + '_sequenced_assembly'
            if (!asm.length && has(server, asmId)) asm = [asmId]
            check(n + ' one assembly', asm.length == 1 && (asm[0] == asmId || asm[0] == CDN + asmId.substring(C.length)), asm.join(', ') || 'none (' + asmId + ' loaded: ' + has(server, asmId) + ')')
            let mach = machineRoutes(server, VT + n)
            check(n + ' not in mixer / mechanical crafter', mach.length == 0, mach.join(', ') || 'none')
            let all = Object.keys(recipes(server)).filter(k => resultOf(rget(server, k)).indexOf(VT + n + ' ') == 0)
            note(n + ' recipes', all.join(', '))
        })
        // VTD-49 stuffed burrito
        expectGone(server, [VT + 'stuffed_burrito', VG + 'compat/vintagedelight/stuffed_burrito'])
        let bur = C + 'shapeless/stuffed_burrito_manual_only', tort = firstOfTag('forge:tortilla')
        check('burrito grid', has(server, bur), has(server, bur) ? resultOf(rget(server, bur)) : 'not loaded')
        check('burrito takes a tortilla', tort != null && takes(server, bur, tort), 'first #forge:tortilla ' + tort)
        check('burrito takes ghost pepper or roasted pepper', takes(server, bur, VT + 'ghost_pepper') && takes(server, bur, 'rusticdelight:roasted_bell_pepper_red'),
            'ghost ' + takes(server, bur, VT + 'ghost_pepper') + ', roasted red ' + takes(server, bur, 'rusticdelight:roasted_bell_pepper_red'))
        let burAll = Object.keys(recipes(server)).filter(k => resultOf(rget(server, k)).indexOf(VT + 'stuffed_burrito ') == 0)
        let burBread = burAll.filter(k => takes(server, k, 'minecraft:bread'))
        check('no burrito recipe on bread', burBread.length == 0, burBread.join(', ') || 'none of ' + burAll.length)
        let burAsm = assemblies(server, VT + 'stuffed_burrito')
        check('burrito one assembly', burAsm.length == 1, burAsm.join(', ') || 'none')
        // VTD-50 manual only and heatless
        expectGone(server, [VT + 'pb_j', VT + 'cucumber_salad'])
        expectIds(server, [C + 'shapeless/pb_j_manual_only', C + 'shapeless/cucumber_salad_manual_only'])
        ;['cheese_pasta', 'pad_thai', 'pickle_soup'].forEach(n => {
            expectIds(server, [C + 'shapeless/' + n + '_cooked_manual_only'])
            let mix = idsMaking(server, C + 'createmixing/', VT + n)
            check('no unheated mixer route ' + n, mix.length == 0, mix.join(', ') || 'none')
        })
        ;['pb_j', 'cucumber_salad', 'stuffed_burrito'].forEach(n => {
            let mach = machineRoutes(server, VT + n)
            check('not in mixer / mechanical crafter ' + n, mach.length == 0, mach.join(', ') || 'none')
        })
        // VTD-51 dough and cookies
        expectGone(server, [VT + 'bread_from_smelting', VT + 'bread_from_smoking'])
        let heat = Object.keys(recipes(server)).filter(id => /smelting|smoking|campfire|blasting/.test(id) && takes(server, id, VT + 'oat_dough'))
        check('oat dough has no furnace route', heat.length == 0, heat.join(', ') || 'none')
        check('3 raw oats -> bread kept', has(server, VT + 'oat_bread') && takes(server, VT + 'oat_bread', VT + 'raw_oats') &&
            resultOf(rget(server, VT + 'oat_bread')).indexOf('minecraft:bread ') == 0,
            has(server, VT + 'oat_bread') ? resultOf(rget(server, VT + 'oat_bread')) : 'not loaded')
        expectGone(server, [VT + 'oatmeal_cookie'])
        let cookie = Object.keys(recipes(server)).filter(id => resultOf(rget(server, id)).indexOf(VT + 'oatmeal_cookie ') == 0)
        check('oatmeal cookie only in the oven', cookie.length >= 1 && cookie.every(id => ours(id, 'baking/')) &&
            takes(server, C + 'baking/oatmeal_cookie', VT + 'raw_oats'), cookie.join(', ') || 'none')
        // VTD-52 / VD Veggies compat folded
        expectGone(server, [VG + 'cesar_salad', VG + 'compat/vintagedelight/cesar_salad', VG + 'stuffed_zucchini_boat',
            VG + 'compat/vintagedelight/stuffed_zucchini_boat'])
        let ces = C + 'shapeless/cesar_salad_manual_only'
        check('Cesar salad takes garlic or cheese', takes(server, ces, VG + 'garlic_clove') && takes(server, ces, VT + 'cheese_slice'),
            has(server, ces) ? 'garlic ' + takes(server, ces, VG + 'garlic_clove') + ', cheese ' + takes(server, ces, VT + 'cheese_slice') : 'not loaded')
        let boat = C + 'shapeless/stuffed_zucchini_boat_cooked_manual_only', milk = firstOfTag('forge:milk')
        check('zucchini boat takes milk or cheese', takes(server, boat, milk) && takes(server, boat, VT + 'cheese_slice'),
            has(server, boat) ? 'milk (' + milk + ') ' + takes(server, boat, milk) + ', cheese ' + takes(server, boat, VT + 'cheese_slice') : 'not loaded')
        // VD-20 wraps: a tortilla, no dough route
        expectGone(server, [VG + 'compat/vintagedelight/chicken_fajitas_wrap', VG + 'compat/corndelight/chicken_fajitas_wrap',
            VG + 'compat/corndelight/vegetable_wrap'])
        let doughs = tagList($VReg.ITEMS, 'forge:dough')
        ;['chicken_fajitas_wrap', 'vegetables_wrap'].forEach(n => {
            let all = Object.keys(recipes(server)).filter(k => resultOf(rget(server, k)).indexOf(VG + n + ' ') == 0)
            let onDough = all.filter(k => doughs.some(d => takes(server, k, d)))
            check(n + ' has no dough recipe', all.length > 0 && onDough.length == 0, onDough.join(', ') || all.length + ' recipes, none on dough')
            let grid = C + 'shapeless/' + n + '_manual_only'
            check(n + ' grid takes a tortilla', takes(server, grid, tort), has(server, grid) ? '' : 'not loaded')
            let mach = machineRoutes(server, VG + n)
            check(n + ' not in mixer / mechanical crafter', mach.length == 0, mach.join(', ') || 'none')
        })
        // VD-24: VD grid meals and heatless pot routes not in the mixer / crafter
        let vdBad = []
        ;['broccoli_salad', 'cesar_salad', 'coleslaw', 'turnip_salad', 'steak_and_broccoli', 'garlic_rice_with_cauliflower',
            'garlic_baked_cod', 'dandelion_and_eggs', 'stuffed_zucchini_boat', 'turnip_mutton_skewer', 'zucchini_sandwich',
            'vegetarian_burger', 'broccoli_soup', 'cauliflower_soup', 'garlic_chicken_stew', 'turnip_beef_stew', 'cacciatore',
            'cauliflower_kuku', 'mashed_potatoes', 'pasta_with_broccoli', 'rice_and_vegetables', 'roasted_vegetables', 'shakshouka',
            'stuffed_zucchinis', 'turnip_cake', 'garlic_stuffed_mushrooms'].forEach(n => machineRoutes(server, VG + n).forEach(id => vdBad.push(id)))
        check('VD-24 no VD meal in mixer / mechanical crafter', vdBad.length == 0, vdBad.join(', ') || 'none')
    }

    // ================================================================ tags (SQ-VP-04)
    function tags(server) {
        const I = $VReg.ITEMS, B = $VReg.BLOCKS
        check('#forge:dough holds oat dough', tagHas(I, 'forge:dough', VT + 'oat_dough'), '')
        check('#forge:dough has no cucumber dough', !tagHas(I, 'forge:dough', VT + 'cucumber_dough'), tagList(I, 'forge:dough').join(', '))
        check('#forge:salt holds salt dust', tagHas(I, 'forge:salt', VT + 'salt_dust'), '')
        // salt merge (item_merges.md 2.9): The Salt's salt is folded into salt dust
        check('#forge:salt has no The Salt salt', !tagHas(I, 'forge:salt', 'salt:salt'), tagList(I, 'forge:salt').join(', '))
        let soil = tagList(B, 'vintagedelight:ghost_pepper_soil')
        check('#vintagedelight:ghost_pepper_soil', sameSet(soil, ['minecraft:soul_soil', 'nethersdelight:rich_soul_soil', 'immersive_weathering:nulch_block']),
            soil.join(', '))
        let crops = { oat: VT + 'oat', peanut: VT + 'peanut', cucumber: VT + 'cucumber', ghost_pepper: VT + 'ghost_pepper' }
        let seeds = { oat: VT + 'oat_seeds', cucumber: VT + 'cucumber_seeds', ghost_pepper: VT + 'ghost_pepper_seeds' }
        let store = { oat: VT + 'oat_bale', peanut: VT + 'peanut_crate', cucumber: VT + 'cucumber_crate', ghost_pepper: VT + 'ghost_pepper_crate' }
        Object.keys(crops).forEach(n => {
            check('#forge:crops/' + n, tagHas(I, 'forge:crops/' + n, crops[n]) && tagHas(I, 'forge:crops', crops[n]), tagList(I, 'forge:crops/' + n).join(', '))
            check('#forge:storage_blocks/' + n, tagHas(I, 'forge:storage_blocks/' + n, store[n]), tagList(I, 'forge:storage_blocks/' + n).join(', '))
        })
        Object.keys(seeds).forEach(n => check('#forge:seeds/' + n, tagHas(I, 'forge:seeds/' + n, seeds[n]) && tagHas(I, 'forge:seeds', seeds[n]),
            tagList(I, 'forge:seeds/' + n).join(', ')))
        note('peanut seeds', 'the peanut plants itself (no seed item); #forge:seeds/peanut = ' + (tagList(I, 'forge:seeds/peanut').join(', ') || 'empty'))
        let rp = tagList(I, 'creativeengineering:roasted_bell_pepper')
        // all nine colours since the Rustic Delight 1.7.0 port (creativedelight rusticdelight/tags.js)
        check('#creativeengineering:roasted_bell_pepper', sameSet(rp, ['green', 'red', 'yellow', 'orange', 'white', 'pink', 'blue', 'purple', 'black']
            .map(c => 'rusticdelight:roasted_bell_pepper_' + c)), rp.join(', '))
        check('burrito takes every roasted pepper', rp.length > 0 && rp.every(x => takes(server, C + 'shapeless/stuffed_burrito_manual_only', x)), '')
        // VTD-16: Brewin' pickling only in the jar
        expectGone(server, ['brewinandchewin:fermenting/kimchi', 'brewinandchewin:fermenting/pickled_pickles_from_honey'])
        ;['brewinandchewin:kimchi', 'brewinandchewin:pickled_pickles'].forEach(out => {
            let keg = idsWhere(server, id => id.indexOf('brewinandchewin:fermenting/') == 0 || id.indexOf(C + 'fermenting/') == 0, out)
            check('no keg recipe for ' + out, keg.length == 0, keg.join(', ') || 'none')
        })
        expectIds(server, [C + 'pickling/brewin_kimchi', C + 'pickling/pickled_sea_pickle'])
        // VTD-55: Brewin's pickled pickles merged into Pickled Sea Pickle (2026-10-25); its honey jar recipe is gone
        expectGone(server, [C + 'pickling/brewin_pickled_pickles'])
        check('jar kimchi -> Brewin kimchi x2', has(server, C + 'pickling/brewin_kimchi') &&
            resultOf(rget(server, C + 'pickling/brewin_kimchi')) == 'brewinandchewin:kimchi x2', has(server, C + 'pickling/brewin_kimchi') ?
            resultOf(rget(server, C + 'pickling/brewin_kimchi')) : '-')
        check('jar pickled sea pickle: 5 sea pickles + salt -> 5', has(server, C + 'pickling/pickled_sea_pickle') &&
            takes(server, C + 'pickling/pickled_sea_pickle', 'minecraft:sea_pickle') &&
            resultOf(rget(server, C + 'pickling/pickled_sea_pickle')) == 'creativeengineering:pickled_sea_pickle x5',
            has(server, C + 'pickling/pickled_sea_pickle') ? resultOf(rget(server, C + 'pickling/pickled_sea_pickle')) : '-')
        // VTD-11 since the salt merge (item_merges.md 2.9): jar recipes take salt dust and none takes The Salt's salt
        let jarIds = Object.keys(recipes(server)).filter(id => id.indexOf(VT + 'fermenting/') == 0 || id.indexOf(C + 'pickling/') == 0 ||
            id.indexOf(VG + 'compat/vintagedelight/fermenting/') == 0)
        let salty = jarIds.filter(id => takes(server, id, VT + 'salt_dust'))
        let oldSalt = jarIds.filter(id => takes(server, id, 'salt:salt'))
        check('VTD-11 jar salt recipes take salt dust only', salty.length > 0 && oldSalt.length == 0,
            salty.length + ' of ' + jarIds.length + ' jar recipes take salt dust; take salt:salt: ' + (oldSalt.join(', ') || 'none'))
    }

    // ================================================================ compost (SQ-VP-05, VD-34)
    function compost() {
        const WANT = {}
        ;['wild_oats', 'wild_peanuts', 'wild_cucumbers', 'wild_ghost_peppers'].forEach(n => WANT[VT + n] = 0.65)
        ;['wild_broccoli', 'wild_cauliflowers', 'wild_garlic', 'wild_sweet_potatoes', 'wild_turnips', 'wild_zucchinis'].forEach(n => WANT[VG + n] = 0.65)
        ;['cucumber_crate', 'ghost_pepper_crate', 'peanut_crate', 'oat_bag'].forEach(n => WANT[VT + n] = 1.0)
        ;['broccoli_crate', 'cauliflower_crate', 'garlic_crate', 'sweet_potato_crate', 'turnip_crate', 'zucchini_crate'].forEach(n => WANT[VG + n] = 1.0)
        WANT[VT + 'pickled_egg'] = 0.6
        WANT[VT + 'century_egg'] = 0.9
        Object.keys(WANT).forEach(id => {
            let v = itemExists(id) ? $VComposter.COMPOSTABLES.getOrDefault(item(id), -1) : 'no item'
            check('compost ' + id, Math.abs(v - WANT[id]) < 0.001, v + ' (want ' + WANT[id] + ')')
        })
        // a produce and a seed of each port: any value
        ;[VT + 'oat', VT + 'oat_seeds', VT + 'cucumber', VT + 'cucumber_seeds', VT + 'peanut', VT + 'ghost_pepper', VT + 'ghost_pepper_seeds',
            VT + 'raw_oats', VG + 'broccoli', VG + 'broccoli_seeds', VG + 'cauliflower', VG + 'cauliflower_seeds', VG + 'garlic', VG + 'garlic_clove',
            VG + 'sweet_potato', VG + 'turnip', VG + 'turnip_seeds', VG + 'zucchini', VG + 'zucchini_seeds', VG + 'dandelion_leaf'].forEach(id => {
            let v = itemExists(id) ? $VComposter.COMPOSTABLES.getOrDefault(item(id), -1) : -2
            check('compostable ' + id, v > 0, v == -2 ? 'no item' : String(v))
        })
        // VD-34: every VD food composts (record the misses)
        let miss = []
        $VReg.ITEMS.getKeys().forEach(k => {
            let id = String(k)
            if (id.indexOf(VG) != 0) return
            let it = item(id)
            if (it.isEdible() && $VComposter.COMPOSTABLES.getOrDefault(it, -1) <= 0) miss.push(id)
        })
        note('VD foods not compostable', miss.join(', ') || 'none')
        note('reload', 'run /reload then /vtdtest compost again: the table must be the same')
    }

    // ================================================================ fluids (SQ-VP-06: soft fluids)
    const OLD_SOUPS = ['alexsdelight:acacia_blossom_soup', 'neapolitan:adzuki_stew', 'fruitsdelight:bayberry_soup',
        'rusticdelight:bell_pepper_soup', 'farmersrespite:blazing_chili', 'farmersdelight:bone_broth', 'moredelight:carrot_soup',
        'quarkdelight:cavern_explorer_stew', 'endersdelight:chorus_stew', 'corn_delight:corn_soup',
        'brewinandchewin:creamy_onion_soup', 'goldenfood:enchanted_golden_beetroot_soup',
        'goldenfood:enchanted_golden_mushroom_stew', 'goldenfood:enchanted_golden_rabbit_stew', 'endersdelight:endermite_stew',
        'fruitsdelight:fig_chicken_stew', 'goldenfood:golden_beetroot_soup', 'goldenfood:golden_mushroom_stew',
        'goldenfood:golden_rabbit_stew', 'alexsdelight:kangaroo_stew', 'quarkdelight:miso_with_bamboo_sprouts',
        'farmersdelight:noodle_soup', 'delightful:sinigang', 'nethersdelight:strider_moss_stew']
    const NEW_SOUPS = [VT + 'pickle_soup', VT + 'ghostly_chili', VT + 'oatmeal', VG + 'broccoli_soup', VG + 'cacciatore',
        VG + 'cauliflower_soup', VG + 'garlic_chicken_stew', VG + 'turnip_beef_stew']
    const JAR_CONTENTS = ['apple_sauce', 'glow_berry_jam', 'nut_mash', 'pepper_jam', 'relish', 'sweet_berry_jam', 'vinegar']
    const DRINKS = [VT + 'nut_milk_bottle', VG + 'carrot_juice', VG + 'dandelion_juice', VG + 'turnip_water', VG + 'fermented_garlic_honey']
    let SF = null
    function softFluids() {
        if (SF) return SF
        SF = Java.loadClass('net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry')
        if (SF.getItemsMap().isEmpty()) { SF.onDataLoad(); console.info('[vtdtest] INFO soft fluid map was empty, filled it') }
        return SF
    }
    function sfId(sf) { try { return String(softFluids().getID(sf)) } catch (e) { return '?' } }
    // item in (in the item map) and out (its soft fluid gives the same item back for the container it empties into)
    function inOut(id) {
        let items = softFluids().getItemsMap()
        if (!itemExists(id)) return [false, 'no item']
        let sf = items.get(item(id))
        if (sf == null) return [false, 'not a soft fluid item']
        let empty = sf.getEmptyContainer(item(id))
        if (!empty.isPresent()) return [false, sfId(sf) + ': no empty container']
        let back = sf.getFilledContainer(empty.get())
        let ok = back.isPresent() && back.get() == item(id)
        return [ok, sfId(sf) + ': empty ' + String($VReg.ITEMS.getKey(empty.get())) + ', fills back ' +
            (back.isPresent() ? String($VReg.ITEMS.getKey(back.get())) : 'nothing')]
    }
    let nextX = 200
    function place(level, id, z) {
        let pos = at(nextX, Y, z)
        nextX += 2
        level.setBlockAndUpdate(pos, block('minecraft:air').defaultBlockState())
        level.setBlockAndUpdate(pos, block(id).defaultBlockState())
        return pos
    }
    function fluids(level) {
        softFluids()
        let soups = OLD_SOUPS.concat(NEW_SOUPS)
        let bad = soups.filter(id => !inOut(id)[0])
        check(soups.length + ' soups in and out', bad.length == 0, bad.map(id => id + ' (' + inOut(id)[1] + ')').join('; ') || 'all')
        NEW_SOUPS.forEach(id => { let r = inOut(id); check('soup ' + id, r[0], r[1]) })
        DRINKS.forEach(id => { let r = inOut(id); check('drink ' + id, r[0], r[1]) })
        JAR_CONTENTS.forEach(n => {
            ;[VT + n + '_bottle', VT + n + '_jar'].forEach(id => { let r = inOut(id); check('jar content ' + id, r[0], r[1]) })
        })
        // the 8th jar content: honey (upstream honey_vd.json lists the apple sauce items, not the honey jar)
        let h = inOut(VT + 'honey_jar')
        check('jar content ' + VT + 'honey_jar', h[0], h[1])
        try {
            let hv = softFluids().get(VT + 'honey_vd')
            note('soft fluid vintagedelight:honey_vd', hv == null || hv.isEmpty() ? 'not loaded' :
                'mason jar fills ' + hv.getFilledContainer(item(VT + 'empty_mason_jar')).map(x => String($VReg.ITEMS.getKey(x))).orElse('nothing') +
                ', bottle fills ' + hv.getFilledContainer(item('minecraft:glass_bottle')).map(x => String($VReg.ITEMS.getKey(x))).orElse('nothing'))
        } catch (e) { note('soft fluid vintagedelight:honey_vd', 'read failed: ' + e) }
        // soft fluid ids by namespace
        let ce = [], vt = []
        softFluids().getValues().forEach(sf => { let id = sfId(sf); if (id.indexOf(C) == 0) ce.push(id.replace(C, '')); if (id.indexOf(VT) == 0) vt.push(id.replace(VT, '')) })
        note('creativeengineering soft fluids (' + ce.length + ')', ce.sort().join(', '))
        note('vintagedelight soft fluids (' + vt.length + ')', vt.sort().join(', '))
        // tank fluids as equivalents
        ;['carrot_juice', 'dandelion_juice', 'turnip_water'].forEach(n => {
            let fl = $VReg.FLUIDS.getValue(rl(C + n)), sf = softFluids().getItemsMap().get(item(VG + n))
            let eq = false, from = '?'
            try { eq = sf != null && fl != null && sf.isEquivalent(fl) } catch (e) { from = 'isEquivalent failed ' + e }
            try { from = sfId(softFluids().fromForgeFluid(fl)) } catch (e) { }
            check('tank fluid ' + C + n + ' equivalent', eq, 'item fluid ' + (sf == null ? 'none' : sfId(sf)) + ', fromForgeFluid ' + from)
        })
        // real jar round trip for the drinks and jar contents (fdtest MJ-12 pattern)
        let rt = []
        DRINKS.concat(JAR_CONTENTS.map(n => VT + n + '_bottle'), JAR_CONTENTS.map(n => VT + n + '_jar'), NEW_SOUPS).forEach(id => {
            if (!itemExists(id)) { rt.push(id + ' (no item)'); return }
            let pos = place(level, 'supplementaries:jar', 22), tank = level.getBlockEntity(pos).getSoftFluidTank()
            let p = fake(level)
            click(p, level, pos, Item.of(id))
            let inCount = tank.getCount(), left = invList(p)
            let empty = left.length == 1 ? left[0].split(' ')[0] : null
            if (inCount == 0 || empty == null) { rt.push(id + ' (in ' + inCount + ', hand ' + (left.join(', ') || 'empty') + ')'); return }
            p.getInventory().clearContent()
            click(p, level, pos, Item.of(empty))
            if (count(p, id) != 1 || !tank.isEmpty()) rt.push(id + ' (in ' + inCount + ' via ' + empty + ', back ' + count(p, id) + ', left ' + tank.getCount() + ')')
        })
        check('Supplementaries jar in and out', rt.length == 0, rt.join('; ') || 'all')
        // a jar of carrot juice from the tank fluid: the jar's own fluid handler, if it has one
        try {
            let pos = place(level, 'supplementaries:jar', 22), be = level.getBlockEntity(pos)
            let cap = be.getCapability($VCaps.FLUID_HANDLER, $VDir.UP).resolve()
            if (!cap.isPresent()) note('jar fluid handler', 'none exposed: tank fill not testable here (pipe or spout in game)')
            else {
                let filled = cap.get().fill(new $VFluidStack($VReg.FLUIDS.getValue(rl(C + 'carrot_juice')), 250), 'EXECUTE')
                let p = fake(level)
                click(p, level, pos, Item.of('minecraft:glass_bottle'))
                check('jar fills from a carrot_juice tank', filled > 0 && count(p, VG + 'carrot_juice') == 1, 'filled ' + filled + ' mB, bottle -> ' + invList(p).join(', '))
            }
        } catch (e) { note('jar fluid handler', 'fill attempt failed: ' + e) }
    }

    // ================================================================ fluidsrun (SQ-VP-07, VD-14)
    function fluidsrun(server) {
        const POT_IN = { carrot_juice: 'minecraft:carrot', dandelion_juice: VG + 'dandelion_leaf', turnip_water: VG + 'turnip' }
        Object.keys(POT_IN).forEach(n => {
            let fl = $VReg.FLUIDS.getValue(rl(C + n))
            let key = fl == null ? 'null' : String($VReg.FLUIDS.getKey(fl))
            check('fluid ' + n, key == C + n, key)
            check('tag forge:fluid/' + n, tagHas($VReg.FLUIDS, 'forge:fluid/' + n, C + n), tagList($VReg.FLUIDS, 'forge:fluid/' + n).join(', '))
            expectGone(server, [VG + 'cooking/' + n])
            let pot = C + 'farmersdelightcooking/' + n + '_manual_only'
            check('pot ' + n, has(server, pot) && takes(server, pot, POT_IN[n]), has(server, pot) ? resultOf(rget(server, pot)) +
                ', takes ' + POT_IN[n] + ' ' + takes(server, pot, POT_IN[n]) : 'not loaded')
            // Create mixer (heated): 250 mB out
            let mix = rget(server, C + 'createmixing/' + n)
            if (!mix) check('createmixing ' + n, false, 'not loaded')
            else {
                let d = '?'
                try { let fr = mix.getFluidResults().get(0); d = String($VReg.FLUIDS.getKey(fr.getFluid())) + ' ' + fr.getAmount() } catch (e) { d = 'read failed ' + e }
                let heat = '?'
                try { heat = String(mix.getRequiredHeat()) } catch (e) { }
                check('createmixing ' + n, d == C + n + ' 250', d + ', heat ' + heat)
            }
            ;['createfilling', 'createemptying'].forEach(kind => {
                let r = rget(server, C + kind + '/' + n)
                if (!r) { check(kind + ' ' + n, false, 'not loaded'); return }
                let d = '?'
                try {
                    if (kind == 'createfilling') d = 'needs ' + r.getFluidIngredients().get(0).getRequiredAmount() + ' mB'
                    else { let fr = r.getFluidResults().get(0); d = String($VReg.FLUIDS.getKey(fr.getFluid())) + ' ' + fr.getAmount() }
                } catch (e) { d = 'read failed ' + e }
                check(kind + ' ' + n, d.indexOf('250') >= 0, d)
            })
            // IE Bottling Machine: one per fluid, 250 mB
            let bott = Object.keys(recipes(server)).filter(id => {
                if (!ours(id, 'iebottling/')) return false
                let r = rget(server, id), out = ''
                try { out = itemId(r.output.get().get(0)) } catch (e) { out = resultOf(r) }
                return out.indexOf(VG + n) == 0
            })
            let amt = '?'
            if (bott.length == 1) {
                let r = recipes(server)[bott[0]]
                try { amt = r.fluidInput.getAmount() + ' mB, accepts tank fluid ' + r.fluidInput.test(Fluid.of(C + n, 250)) } catch (e) { amt = 'read failed ' + e }
            }
            check('iebottling ' + n, bott.length == 1 && amt.indexOf('250 mB, accepts tank fluid true') == 0, (bott.join(', ') || 'none') + ': ' + amt)
        })
    }

    // ================================================================ pickling (SQ-VP-08: the Pickling Jar)
    function jarAt(level, x) {
        let pos = at(x, Y, 20)
        level.setBlockAndUpdate(pos, block('minecraft:air').defaultBlockState())
        level.setBlockAndUpdate(pos, block(VT + 'fermenting_jar').defaultBlockState())
        return pos
    }
    function side(be, dir) { let o = be.getCapability($VCaps.ITEM_HANDLER, dir).resolve(); return o.isPresent() ? o.get() : null }
    function slots(h) {
        let out = []
        for (let k = 0; k < h.getSlots(); k++) { let s = h.getStackInSlot(k); out.push(s.isEmpty() ? '-' : itemId(s) + ' x' + s.getCount()) }
        return out.join(' | ')
    }
    function topIn(be, id, n) { return side(be, $VDir.UP).insertItem(0, Item.of(id, n), false) }
    function runJar(level, pos, ticks) {
        let be = level.getBlockEntity(pos)
        for (let k = 0; k < ticks; k++) be.tick(level, pos, level.getBlockState(pos))
    }
    function takeOut(be) {
        let down = side(be, $VDir.DOWN), out = {}
        for (let k = 0; k < down.getSlots(); k++) {
            let s = down.extractItem(k, 64, false)
            if (!s.isEmpty()) out[itemId(s)] = (out[itemId(s)] || 0) + s.getCount()
        }
        return out
    }
    function pickling(server, level) {
        let jb = block(VT + 'fermenting_jar')
        check('Pickling Jar block', jb != null && String($VReg.BLOCKS.getKey(jb)) == VT + 'fermenting_jar', jb == null ? 'null' : String($VReg.BLOCKS.getKey(jb)))
        // side mapping: top 6 ingredient slots, sides the container slot, bottom the 2 outputs and the remainder slot (-04)
        let pos = jarAt(level, 200), be = level.getBlockEntity(pos)
        let up = side(be, $VDir.UP), north = side(be, $VDir.NORTH), down = side(be, $VDir.DOWN)
        check('item handler sides', up != null && north != null && down != null && up.getSlots() == 6 && north.getSlots() == 1 && down.getSlots() == 3,
            'up ' + (up ? up.getSlots() : 'none') + ', side ' + (north ? north.getSlots() : 'none') + ', down ' + (down ? down.getSlots() : 'none'))
        if (up == null || north == null || down == null) return
        let rej = north.insertItem(0, Item.of(VT + 'cucumber'), false)
        check('side refuses a non-container', rej.getCount() == 1, 'left ' + rej.getCount())
        let bot = down.insertItem(0, Item.of(VT + 'cucumber'), false)
        check('bottom refuses inserts', bot.getCount() == 1, 'left ' + bot.getCount())
        let cont = north.insertItem(0, Item.of('minecraft:glass_bottle'), false)
        check('side takes a glass bottle', cont.isEmpty() && itemId(be.getInventory().getStackInSlot(6)) == 'minecraft:glass_bottle', slots(be.getInventory()))
        // 5 cucumbers + salt dust -> 5 pickles (The Salt's salt is merged into the dust, item_merges.md 2.9)
        let pr = rget(server, VT + 'fermenting/pickle_from_fermenting')
        let pt = pr ? pr.getProcessingTime() : 2400
        note('pickle recipe', pr ? resultOf(pr) + ', ' + pt + ' ticks' : 'not loaded')
        ;[[VT + 'salt_dust', 202]].forEach(s => {
            let p2 = jarAt(level, s[1]), b2 = level.getBlockEntity(p2)
            let left = topIn(b2, VT + 'cucumber', 5)
            let left2 = topIn(b2, s[0], 1)
            check('top spreads 5 cucumbers + ' + s[0], left.isEmpty() && left2.isEmpty(), slots(b2.getInventory()))
            runJar(level, p2, pt + 1)
            let out = takeOut(b2)
            check('pickle with ' + s[0], out[VT + 'pickle'] == 5, fmt(out) + ' after ' + (pt + 1) + ' ticks; jar ' + slots(b2.getInventory()))
        })
        // fermented garlic honey (honey bottle input): 1 jar item out; the bottle is not returned (vintageport #4)
        let gid = VG + 'compat/vintagedelight/fermenting/fermented_garlic'
        let gr = rget(server, gid), gt = gr ? gr.getProcessingTime() : 600
        check('garlic honey recipe', gr != null, gr ? resultOf(gr) + ', ' + gt + ' ticks' : 'not loaded')
        let p3 = jarAt(level, 206), b3 = level.getBlockEntity(p3)
        topIn(b3, VG + 'garlic_clove', 4); topIn(b3, 'minecraft:honey_bottle', 1)
        runJar(level, p3, gt + 1)
        let inv3 = slots(b3.getInventory())
        let out3 = takeOut(b3)
        check('garlic honey -> 1 jar item', out3[VG + 'fermented_garlic_honey'] == 1, fmt(out3))
        note('honey bottle remainder (known gap vintageport #4, record only)', 'jar after the craft: ' + inv3 +
            (out3['minecraft:glass_bottle'] ? '; a glass bottle came out' : '; no glass bottle returned'))
        // century egg: any egg + coal + salt; 216000 block-entity ticks (counts jar ticks, not the clock: /time add won't help)
        let cid = VT + 'fermenting/century_egg_from_fermenting', cr = rget(server, cid)
        let ct = cr ? cr.getProcessingTime() : -1
        check('century egg recipe 3 hours', ct == 216000, ct + ' ticks (processingTime counted by the jar tick)')
        let eggs = tagList($VReg.ITEMS, 'forge:eggs')
        let refused = cr ? eggs.filter(e => !takes(server, cid, e)) : eggs
        check('century egg takes any #forge:eggs', cr != null && refused.length == 0, eggs.length + ' eggs, refused: ' + (refused.join(', ') || 'none'))
        let eggUsed = eggs.indexOf('minecraft:egg') >= 0 ? 'minecraft:egg' : eggs[0]
        let otherEgg = eggs.filter(e => e != 'minecraft:egg')[0]
        ;[[eggUsed, 208], [otherEgg, 210]].forEach(eg => {
            if (eg[0] == null) return
            let p4 = jarAt(level, eg[1]), b4 = level.getBlockEntity(p4)
            topIn(b4, eg[0], 1); topIn(b4, 'minecraft:coal', 1); topIn(b4, VT + 'salt_dust', 1)
            runJar(level, p4, 5)
            let tag = b4.saveWithoutMetadata()
            let prog = tag.getInt('fermenting_jar.progress')
            check('century egg starts with ' + eg[0], prog == 5, 'progress ' + prog + ' after 5 ticks; jar ' + slots(b4.getInventory()))
            if (ct > 0) {
                tag.putInt('fermenting_jar.progress', ct - 2)
                b4.load(tag)
                runJar(level, p4, 1)
                let mid = takeOut(b4)
                runJar(level, p4, 1)
                let out4 = takeOut(b4)
                check('century egg at ' + ct + ' ticks from ' + eg[0], !mid[VT + 'century_egg'] && out4[VT + 'century_egg'] == 1,
                    'at ' + (ct - 1) + ': ' + fmt(mid) + '; at ' + ct + ': ' + fmt(out4))
            }
        })
    }

    // ================================================================ crops (SQ-VP-09: VTD-01 to VTD-04, VD-01, VD-25)
    // Survival needs light 8 or open sky (CropBlock): checked just under the build limit, so it also holds in a
    // normal world, where Y is underground
    let cropX = 220
    function survives(level, cropId, soilId) {
        let pos = at(cropX, level.getMaxBuildHeight() - 2, 26)
        cropX += 1
        set(level, pos.below(), soilId)
        set(level, pos, 'minecraft:air')
        let ok = block(cropId).defaultBlockState().canSurvive(level, pos)
        set(level, pos.below(), 'minecraft:air')
        return ok
    }
    function ageOf(state) { let m = String(state).match(/age=(\d+)/); return m ? parseInt(m[1]) : -1 }
    function crops(level) {
        let gp = VT + 'ghost_pepper_crop'
        ;['minecraft:soul_soil', 'nethersdelight:rich_soul_soil', 'immersive_weathering:nulch_block'].forEach(s =>
            check('ghost pepper survives on ' + s, survives(level, gp, s), ''))
        ;['minecraft:crimson_nylium', 'minecraft:farmland', 'minecraft:dirt', 'minecraft:grass_block'].forEach(s =>
            check('ghost pepper refuses ' + s, !survives(level, gp, s), ''))
        const FARM = ['minecraft:farmland', 'farmersdelight:rich_soil_farmland', 'immersive_weathering:mulch_block']
        ;[VT + 'oat_crop', VT + 'peanut_crop', VT + 'cucumber_crop', VG + 'broccoli_crop', VG + 'cauliflower_crop', VG + 'garlic_crop',
            VG + 'sweet_potato_crop', VG + 'turnip_crop', VG + 'zucchini_crop'].forEach(c => {
            if (block(c) == null || String($VReg.BLOCKS.getKey(block(c))) != c) { check('crop block ' + c, false, 'not registered'); return }
            FARM.forEach(s => check(c + ' survives on ' + s, survives(level, c, s), ''))
            note(c + ' on dirt', String(survives(level, c, 'minecraft:dirt')))
        })
        // right-click harvest by a fake player (empty hand)
        let hx = 240
        ;[[VT + 'cucumber_crop', 7, 5, VT + 'cucumber', 'minecraft:farmland'],
            [gp, 7, 5, VT + 'ghost_pepper', 'minecraft:soul_soil'],
            [VG + 'zucchini_crop', 7, 4, VG + 'zucchini', 'minecraft:farmland']].forEach(c => {
            let pos = at(hx, Y, 30); hx += 3
            set(level, pos.below(), c[4])
            level.setBlockAndUpdate(pos, block(c[0]).getStateForAge(c[1]))
            grabDrops(level, pos, 2)
            let before = ageOf(level.getBlockState(pos))
            let p = fake(level)
            let res = click(p, level, pos, Item.of('minecraft:air'))
            let after = level.getBlockState(pos)
            let drops = grabDrops(level, pos, 2)
            let got = (drops[c[3]] || 0) + count(p, c[3])
            check('harvest ' + c[0], blockId(after) == c[0] && ageOf(after) == c[2] && got >= 1 && got <= 2,
                'age ' + before + ' -> ' + (blockId(after) == c[0] ? ageOf(after) : blockId(after)) + ' (want ' + c[2] + '), ' + res + ', drops ' + fmt(drops) +
                (count(p, c[3]) ? ', in hand ' + count(p, c[3]) : ''))
            set(level, pos, 'minecraft:air'); set(level, pos.below(), 'minecraft:grass_block')
        })
        note('hoe area harvest (VTD-04)', 'not probed: client-side hoe use, check in game')
    }

    // ================================================================ mobs (SQ-VP-10: VTD-31, VD-33)
    function staticSet(cls, mojName, srgName) {
        let v = null
        try { v = cls[mojName] } catch (e) { }
        if (v == null) try { v = cls[srgName] } catch (e) { }
        return v
    }
    function mobs(level) {
        let chicken = spawn(level, 'minecraft:chicken', 280, 35)
        let cf = ['minecraft:wheat_seeds', VT + 'oat_seeds', VT + 'cucumber_seeds', VT + 'ghost_pepper_seeds', VG + 'broccoli_seeds',
            VG + 'cauliflower_seeds', VG + 'turnip_seeds', VG + 'zucchini_seeds']
        cf.forEach(id => check('chicken food ' + id, chicken.isFood(Item.of(id)), ''))
        let pig = spawn(level, 'minecraft:pig', 283, 35)
        ;['minecraft:carrot', VT + 'cucumber', VT + 'ghost_pepper', VT + 'raw_oats', VT + 'peanut', VG + 'cauliflower', VG + 'cauliflower_floret',
            VG + 'sweet_potato', VG + 'turnip', VG + 'zucchini', VG + 'zucchini_slice', 'rusticdelight:bell_pepper_red'].forEach(id =>
            check('pig food ' + id, pig.isFood(Item.of(id)), ''))
        // parrots: isFood is always false in vanilla; tame by feeding (1 in 10 a seed), up to 120 seeds
        ;['minecraft:wheat_seeds', VT + 'oat_seeds', VT + 'ghost_pepper_seeds', VT + 'cucumber_seeds', VG + 'broccoli_seeds', VG + 'zucchini_seeds'].forEach((id, k) => {
            let parrot = spawn(level, 'minecraft:parrot', 286 + k, 38)
            let p = fake(level), n = 0
            for (; n < 120 && !parrot.isTame(); n++) {
                p.setItemInHand($VHand.MAIN_HAND, Item.of(id, 64))
                p.interactOn(parrot, $VHand.MAIN_HAND)
            }
            check('parrot tames with ' + id, parrot.isTame(), 'tame ' + parrot.isTame() + ' after ' + n + ' seeds')
            parrot.discard()
        })
        // villagers pick up
        let vil = spawn(level, 'minecraft:villager', 292, 35)
        ;['minecraft:bread', VT + 'ghost_pepper', VT + 'cucumber', VT + 'peanut', VT + 'oat', VG + 'broccoli', VG + 'broccoli_seeds', VG + 'cauliflower',
            VG + 'garlic', VG + 'garlic_clove', VG + 'sweet_potato', VG + 'turnip', VG + 'zucchini', VG + 'cauliflower_bread',
            'rusticdelight:bell_pepper_red'].forEach(id => check('villager wants ' + id, vil.wantsToPickUp(Item.of(id)), ''))
        // static fields, when Rhino can read them (record only)
        try {
            let tame = staticSet(Java.loadClass('net.minecraft.world.entity.animal.Parrot'), 'TAME_FOOD', 'f_29357_')
            note('Parrot.TAME_FOOD', tame == null ? 'not readable' : tame.size() + ' items')
            let want = staticSet(Java.loadClass('net.minecraft.world.entity.npc.Villager'), 'WANTED_ITEMS', 'f_35372_')
            note('Villager.WANTED_ITEMS', want == null ? 'not readable' : want.size() + ' items')
            let pf = staticSet(Java.loadClass('net.minecraft.world.entity.animal.Pig'), 'FOOD_ITEMS', 'f_29458_')
            note('Pig.FOOD_ITEMS', pf == null ? 'not readable' : pf.getStacks().size() + ' stacks')
            let chf = staticSet(Java.loadClass('net.minecraft.world.entity.animal.Chicken'), 'FOOD_ITEMS', 'f_28233_')
            note('Chicken.FOOD_ITEMS', chf == null ? 'not readable' : chf.getStacks().size() + ' stacks')
        } catch (e) { note('static fields', 'read failed: ' + e) }
        note('Quark troughs', 'not probed (trough reads the animal temptation items; check in game)')
        ;[chicken, pig, vil].forEach(e => e.discard())
    }

    // ================================================================ loot (SQ-VP-11: VTD-30, VD-31)
    const VTD_TABLES = ['vd_village_savanna_house', 'vd_bastion_hoglin_stable', 'vd_village_desert_house', 'vd_village_plains_house',
        'vd_village_taiga_house', 'vd_ruined_portal', 'mixed_seeds', 'vd_village_snowy_house'].map(n => VT + 'chests/' + n)
    const VD_LOOT = {
        'minecraft:chests/abandoned_mineshaft': ['broccoli_seeds', 'cauliflower_seeds', 'turnip_seeds', 'zucchini_seeds'],
        'minecraft:chests/pillager_outpost': ['garlic'],
        'minecraft:chests/shipwreck_supply': ['cauliflower_seeds', 'turnip', 'zucchini'],
        'minecraft:chests/simple_dungeon': ['broccoli_seeds'],
        'minecraft:chests/village/village_desert_house': [],
        'minecraft:chests/village/village_plains_house': ['sweet_potato', 'garlic', 'broccoli', 'turnip'],
        'minecraft:chests/village/village_savanna_house': ['zucchini'],
        'minecraft:chests/village/village_snowy_house': ['cauliflower', 'cauliflower_seeds', 'sweet_potato', 'turnip'],
        'minecraft:chests/village/village_taiga_house': ['cauliflower', 'cauliflower_seeds', 'garlic', 'sweet_potato', 'turnip'],
    }
    function loot(server, level) {
        let lt = null, LTC = null
        try { lt = server.getLootTables(); LTC = Java.loadClass('net.minecraft.world.level.storage.loot.LootTables') } catch (e) { }
        if (lt == null) { note('loot tables', 'server.getLootTables() not reachable: scan kubejs/exported/loot offline'); return }
        let ids = []
        lt.getIds().forEach(r => ids.push(String(r)))
        VTD_TABLES.forEach(t => check('table loaded ' + t, ids.indexOf(t) >= 0, ''))
        // serialize every table once; find references
        let json = {}, errs = 0
        ids.forEach(id => { try { json[id] = String(LTC.serialize(lt.get(rl(id))).toString()) } catch (e) { errs++ } })
        note('tables serialized', Object.keys(json).length + ' of ' + ids.length + (errs ? ', ' + errs + ' failed' : ''))
        let refs = []
        Object.keys(json).forEach(id => {
            if (VTD_TABLES.indexOf(id) >= 0) return
            VTD_TABLES.forEach(t => { if (json[id].indexOf('"' + t + '"') >= 0) refs.push(id + ' -> ' + t) })
        })
        check('VTD-30 no table references a Vintage chest table', refs.length == 0, refs.join(', ') || 'none')
        note('global loot modifiers', 'not visible here (the port ships none; a pack GLM would not show in this scan)')
        // /loot can still roll them: LootContext on the chest param set, 5 rolls each
        let $LCB = null, $LCP = null, $LCPS = null
        try {
            $LCB = Java.loadClass('net.minecraft.world.level.storage.loot.LootContext$Builder')
            $LCP = Java.loadClass('net.minecraft.world.level.storage.loot.parameters.LootContextParams')
            $LCPS = Java.loadClass('net.minecraft.world.level.storage.loot.parameters.LootContextParamSets')
        } catch (e) { }
        VTD_TABLES.forEach(t => {
            let n = 0, sample = []
            try {
                for (let k = 0; k < 5; k++) {
                    let ctx = new $LCB(level).withParameter($LCP.ORIGIN, new $VVec3(295, Y, 38)).create($LCPS.CHEST)
                    lt.get(rl(t)).getRandomItems(ctx).forEach(s => { n += s.getCount(); if (sample.length < 4) sample.push(itemId(s)) })
                }
                check('rolls ' + t, n > 0, n + ' items in 5 rolls, e.g. ' + (sample.join(', ') || 'none'))
            } catch (e) { note('rolls ' + t, 'roll failed (' + e + '): use /loot spawn ~ ~ ~ loot ' + t) }
        })
        // VD-31: Veggies items in chest tables only where 07_rules.js adds them; crates as packed splits
        Object.keys(VD_LOOT).forEach(t => {
            if (!json[t]) { check('VD loot table ' + t, false, 'not loaded / not serialized'); return }
            let miss = VD_LOOT[t].filter(n => json[t].indexOf('"' + VG + n + '"') < 0)
            check('VD loot in ' + t, miss.length == 0, miss.length ? 'missing ' + miss.join(', ') : VD_LOOT[t].length + ' items')
        })
        let extra = []
        Object.keys(json).forEach(id => {
            if (id.indexOf('chests/') < 0 || id.indexOf(VG) == 0 || id.indexOf(VT) == 0) return
            let found = (json[id].match(/"veggiesdelight:[a-z_]+"/g) || []).map(s => s.slice(16, -1))
            found.forEach(n => {
                let allowed = (VD_LOOT[id] || []).indexOf(n) >= 0 || /_crate$/.test(n) && (VD_LOOT[id] || []).indexOf(n.replace('_crate', '')) >= 0
                if (!allowed) extra.push(id + ': ' + n)
            })
        })
        check('VD-31 no Veggies item in other chest tables', extra.length == 0, extra.slice(0, 20).join(', ') || 'none')
        let crates = []
        Object.keys(json).forEach(id => { if (id.indexOf('chests/') >= 0 && /"veggiesdelight:[a-z_]+_crate"/.test(json[id])) crates.push(id) })
        note('tables holding a VD crate (L.pack splits)', crates.join(', ') || 'none')
        note('shares 0.015 to 0.04', 'not read here: see /loottuning and the [loot-tuning] line')
        note('trades (VD-32)', 'not probed: roll real villagers after a restart')
    }

    // ================================================================ stacks (SQ-VP-12 VD-18: record only, deferred to CQ-DUMP)
    function stacks() {
        const ROWS = {
            'bowl meals (32)': [VG + 'broccoli_soup', VG + 'cacciatore', VG + 'cauliflower_soup', VG + 'garlic_chicken_stew', VG + 'turnip_beef_stew',
                VG + 'broccoli_salad', VG + 'cesar_salad', VG + 'coleslaw', VG + 'turnip_salad', VG + 'pasta_with_broccoli', VG + 'potato_noodles',
                VT + 'pickle_soup', VT + 'ghostly_chili', VT + 'oatmeal', VT + 'cheese_pasta', VT + 'pad_thai', VT + 'cucumber_salad'],
            'drinks (32)': [VG + 'carrot_juice', VG + 'dandelion_juice', VG + 'turnip_water', VG + 'fermented_garlic_honey', VT + 'nut_milk_bottle', VT + 'vinegar_bottle'],
            'placed cakes and feasts (8)': [VG + 'carrot_cake', VG + 'sweet_potato_pie', VG + 'beetroot_brownie_tray', VG + 'vegan_pizza', VG + 'zucchini_quiche',
                VG + 'lasagna_pan', VG + 'stuffed_bellpeppers_block', VT + 'cheese_pizza', VT + 'meat_pizza'],
        }
        Object.keys(ROWS).forEach(r => note('stack sizes ' + r, ROWS[r].map(id => id + '=' + (itemExists(id) ? item(id).getMaxStackSize() : 'no item')).join(', ')))
        note('VD-18', 'deferred: the policy check runs on the CQ-DUMP stacks dump')
    }

    // ================================================================ effects (SQ-VP-12 VTD-27)
    function potion(id) { return $VPotionUtils.setPotion(Item.of('minecraft:potion'), $VReg.POTIONS.getValue(rl(id))) }
    function effects(server) {
        let eff = [], pot = []
        $VReg.MOB_EFFECTS.getKeys().forEach(k => { if (String(k).toLowerCase().indexOf('dehydrat') >= 0) eff.push(String(k)) })
        $VReg.POTIONS.getKeys().forEach(k => { if (String(k).toLowerCase().indexOf('dehydrat') >= 0) pot.push(String(k)) })
        check('no Dehydrated effect', eff.length == 0, eff.join(', ') || 'none')
        check('no Dehydrated potion', pot.length == 0, pot.join(', ') || 'none')
        let awk = potion('minecraft:awkward'), dust = Item.of(VT + 'salt_dust')
        check('no awkward + salt dust vanilla mix', !$VBrewing.hasMix(awk, dust), '')
        let bo = $VBrewReg.getOutput(awk, dust)
        check('no awkward + salt dust brewing stand route', bo.isEmpty(), bo.isEmpty() ? 'empty' : itemId(bo) + ' ' + String(bo.nbt))
        let extra = []
        $VBrewReg.getRecipes().forEach(r => { let n = String(r.getClass().getName()); if (n.toLowerCase().indexOf('vintage') >= 0 || n.toLowerCase().indexOf('dehydrat') >= 0) extra.push(n) })
        check('no Vintage BrewingRecipeRegistry entry', extra.length == 0, extra.join(', ') || 'none')
        // no Vintage / Veggies food gives a non-vanilla effect named dehydrated
        let bad = []
        $VReg.ITEMS.getKeys().forEach(k => {
            let id = String(k)
            if (id.indexOf(VT) != 0 && id.indexOf(VG) != 0) return
            let fp = item(id).getFoodProperties()
            if (fp == null) return
            fp.getEffects().forEach(pr => { let e = String($VReg.MOB_EFFECTS.getKey(pr.getFirst().getEffect())); if (e.indexOf('dehydrat') >= 0) bad.push(id + ' ' + e) })
        })
        check('no port food gives Dehydrated', bad.length == 0, bad.join(', ') || 'none')
        // the 6 jar brews: vanilla effect ids
        ;['berserks_brew', 'brashing_tonic', 'dragons_belch', 'goblins_piss', 'oily_oaf', 'frost_fermenta'].forEach(n => {
            // berserks_brew is replaced by creativedelight:pickling/berserks_brew (#forge:salt, SQ-VTD4-02)
            let r = rget(server, VT + 'fermenting/' + n) || rget(server, 'creativedelight:pickling/' + n)
            if (!r) { check('brew ' + n, false, 'not loaded'); return }
            let nbt = String(r.getResultItem().nbt), ids = [], m, re = /Id:(-?\d+)/g
            while ((m = re.exec(nbt)) != null) {
                let e = $VMobEffect.byId(parseInt(m[1]))
                ids.push(m[1] + '=' + (e == null ? 'none' : String($VReg.MOB_EFFECTS.getKey(e))))
            }
            check('brew ' + n + ' vanilla effects', ids.length > 0 && ids.every(s => s.indexOf('=minecraft:') > 0), ids.join(', ') || 'no effects in ' + nbt)
        })
    }

    function run(src, which) {
        pass = 0; fail = 0; byId = null; fakeN = 0; nextX = 200; cropX = 220; SF = null; canTypes = null
        let server = src.getServer(), level = src.getLevel()
        let parts = which == 'all' ? ['recipes', 'cooking', 'tags', 'compost', 'fluids', 'fluidsrun', 'pickling', 'crops', 'mobs', 'loot',
            'stacks', 'effects'] : [which]
        parts.forEach(w => {
            part = w
            try {
                if (w == 'recipes') recipeIds(server)
                else if (w == 'cooking') cooking(server)
                else if (w == 'tags') tags(server)
                else if (w == 'compost') compost()
                else if (w == 'fluids') fluids(level)
                else if (w == 'fluidsrun') fluidsrun(server)
                else if (w == 'pickling') pickling(server, level)
                else if (w == 'crops') crops(level)
                else if (w == 'mobs') mobs(level)
                else if (w == 'loot') loot(server, level)
                else if (w == 'stacks') stacks()
                else if (w == 'effects') effects(server)
                else check('part', false, 'unknown part ' + w)
            } catch (e) { fail++; console.info('[vtdtest] FAIL ' + w + ' ERROR ' + e) }
        })
        console.info('[vtdtest] done ' + which + ': ' + pass + ' pass, ' + fail + ' fail')
    }

    ServerEvents.commandRegistry(event => {
        const { commands: Commands, arguments: Arguments } = event
        event.register(Commands.literal('vtdtest')
            .executes(ctx => { run(ctx.source, 'all'); return 1 })
            .then(Commands.argument('part', Arguments.GREEDY_STRING.create(event)).executes(ctx => {
                run(ctx.source, String(Arguments.GREEDY_STRING.getResult(ctx, 'part'))); return 1
            })))
    })
})()
