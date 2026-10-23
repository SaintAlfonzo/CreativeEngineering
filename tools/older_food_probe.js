// Older food builds probe (scratch queue SQ-OF: food builds of 2026-10-19 to 2026-10-21 never checked on a server).
// Scratch-only: copy into a scratch server's kubejs/server_scripts as zz_of_probe.js
// (tools/creative_labels_test/scratch_server.sh), never into the pack. Run /ofprobe <part> or /ofprobe all; results
// are "[ofprobe] PASS|FAIL|INFO ..." lines in logs/latest.log, ending "[ofprobe] done: N pass, M fail".
//
//  cans       every can in generation_scripts/food/can_types.json: registered, food values / flags / effects as the json,
//             servings (CanServings) as the json, every recipe id there with its result and ingredient count
//             (TODO cans/canning #3, #4, #14, types-and-tuning #2)
//  canextra   hand-written expectations from the TODO rows: raw-can effects, Coffee Berries wither, pumpkin as fruit,
//             cactus flesh 4 / 3 + salt, Canned Cake (canning #4, #5, #14, #15), cantaloupe slice = pumpkin slice
//  canremoved no recipe makes a partly full can, Canned Food or the Hot Hot Hotpot; no butcher trade sells Canned Food
//  cornmeal   knife 4 kernels, IE crusher / PNC routes, tortilla and cornbread batter take cornmeal not corn, old Corn
//             Delight ids gone, #forge:flour/corn (ingredients #1)
//  muffin     oven muffins 4, pot muffins 2 (oven #5)
//  meals      heatless grid meals, per-meal changes, burgers, quiche (meals #1, #2)
//  assembly   meal assemblies and the shaped-only meals (meals #3)
//  expensive  expensive grid meals and squid ink pasta mixing (meals #4)
//  frying     fish frying with cooking oil, PneumaticCraft frying gone (meals #7)
//  dogfood    dog food grid recipe (meals #8)
//  melon      Glistering Melon Slice edible at Golden Melon Slice values, no effects, golden food type (meals #6)
//
// Rhino rules (memory rhino-fieldandmethods-gotcha): no getType() on every recipe (only on ones already filtered by
// result), Ingredient.getStacks() not getItems(), every Java call that may throw inside try.
(() => {
    const $OFForgeReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $OFRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $OFServings = Java.loadClass('com.SaintAlfonzo.creativeengineering.food.CanServings')
    const $OFAllTypes = Java.loadClass('com.simibubi.create.AllRecipeTypes')
    const $OFProf = Java.loadClass('net.minecraft.world.entity.npc.VillagerProfession')
    const $OFEntityType = Java.loadClass('net.minecraft.world.entity.EntityType')

    const i = global.item
    const CE = 'creativeengineering:'
    let pass = 0, fail = 0
    function log(s) { console.info('[ofprobe] ' + s) }
    function ok(cond, what) { if (cond) { pass++; log('PASS ' + what) } else { fail++; log('FAIL ' + what) } }
    function info(s) { log('INFO ' + s) }
    function rl(id) { return new $OFRL(id) }
    function idOf(st) { return String($OFForgeReg.ITEMS.getKey(st.getItem())) }
    function registered(id) { return typeof id === 'string' && id.charAt(0) != '#' && $OFForgeReg.ITEMS.containsKey(rl(id)) }
    function stacksOf(ing) {
        let out = [], it = ing.getStacks().iterator()
        while (it.hasNext()) out.push(it.next())
        return out
    }
    // the item ids an item_dict value (item id or #tag) stands for
    function idsOfKey(key) {
        let v = i[key]
        if (typeof v !== 'string') return []
        if (v.charAt(0) != '#') {
            // a purged id (item merges) is swapped for its survivor in every recipe
            let m = (global.itemMerges || []).filter(pair => String(pair[0]) == v)
            return m.length ? [String(m[0][1])] : [v]
        }
        try { return stacksOf(Ingredient.of(v)).map(idOf) } catch (e) { return [] }
    }
    function near(a, b) { return Math.abs(Number(a) - Number(b)) < 0.001 }

    let RM = null
    function recipe(id) {
        try { return RM.byKey(rl(id)).orElse(null) } catch (e) { return null }
    }
    function ingIds(r) {
        let out = []
        try { r.getIngredients().forEach(g => out.push(stacksOf(g).map(idOf))) } catch (e) { }
        return out
    }
    function fluidIds(r) {
        let out = []
        try {
            r.getFluidIngredients().forEach(fi => {
                fi.getMatchingFluidStacks().forEach(fs => out.push(String($OFForgeReg.FLUIDS.getKey(fs.getFluid()))))
            })
        } catch (e) { }
        return out
    }
    function result(r) {
        try { let s = r.getResultItem(); return { id: idOf(s), n: s.getCount() } } catch (e) { return { id: '?', n: 0 } }
    }
    function outputs(r) {
        let out = []
        try {
            r.getRollableResults().forEach(o => out.push(o.getStack().getCount() + 'x ' + idOf(o.getStack()) +
                (o.getChance() < 1 ? ' @' + o.getChance() : '')))
        } catch (e) { }
        if (!out.length) { let s = result(r); out.push(s.n + 'x ' + s.id) }
        return out
    }
    // the recipe takes something from item_dict key `key` in at least one slot
    function takes(r, key) {
        let want = idsOfKey(key)
        return ingIds(r).some(slot => slot.some(id => want.indexOf(id) >= 0))
    }
    function takesId(r, id) { return ingIds(r).some(slot => slot.indexOf(id) >= 0) }
    // a recipe id is there, makes key x n, and is skipped by Create's automation when manual
    function present(id, key, n) {
        let r = recipe(id)
        if (!r) { ok(false, id + ' present'); return null }
        let res = result(r)
        let wantId = i[key]
        ok(res.id == wantId && (n == null || res.n == n), id + ' makes ' + (n == null ? '' : n + 'x ') + wantId +
            ' (' + res.n + 'x ' + res.id + ')')
        if (/_manual_only$/.test(id)) {
            let ignored = false
            try { ignored = $OFAllTypes.shouldIgnoreInAutomation(r) } catch (e) { }
            ok(ignored, id + ' skipped by Create automation')
        }
        return r
    }
    function absent(id) { ok(recipe(id) == null, id + ' removed') }
    // every recipe whose result is one of `ids` (getType only on these)
    function makersOf(ids) {
        let out = []
        RM.getRecipes().forEach(r => {
            let res
            try { res = idOf(r.getResultItem()) } catch (e) { return }
            if (ids.indexOf(res) >= 0) out.push(r)
        })
        return out
    }
    function typeOf(r) { try { return String(r.getType()) } catch (e) { return '?' } }
    // getType() here gives the serializer id: minecraft:crafting_shaped / minecraft:crafting_shapeless
    function isGrid(r) { return /^minecraft:crafting_(shaped|shapeless)$/.test(typeOf(r)) }
    function food(id) {
        try { return $OFForgeReg.ITEMS.getValue(rl(id)).getFoodProperties() } catch (e) { return null }
    }
    function effectsOf(props) {
        let out = []
        try {
            props.getEffects().forEach(pair => {
                let inst = pair.getFirst()
                out.push(String($OFForgeReg.MOB_EFFECTS.getKey(inst.getEffect())) + '|' + inst.getDuration() + '|' +
                    inst.getAmplifier() + '|' + Math.round(Number(pair.getSecond()) * 100) / 100)
            })
        } catch (e) { out.push('ERROR ' + e) }
        return out.sort()
    }
    function foodDesc(props) {
        if (!props) return 'not food'
        return props.getNutrition() + ' / ' + props.getSaturationModifier() + (props.isMeat() ? ' meat' : '') +
            (props.isFastFood() ? ' fast' : '') + (props.canAlwaysEat() ? ' always' : '') + ' [' + effectsOf(props).join(', ') + ']'
    }

    function canTypes() {
        return JSON.parse(String(JsonIO.readString('kubejs/generation_scripts/food/can_types.json'))).cans
    }

    //######################### cans: every can against can_types.json #########################
    function cans() {
        let c = canTypes()
        let keys = Object.keys(c)
        let generated = 0, foodOk = 0, servOk = 0, recOk = 0, recAll = 0
        info(keys.length + ' cans in can_types.json')
        keys.forEach(key => {
            let can = c[key]
            let id = i[key]
            if (!registered(id)) { ok(false, key + ' registered (' + id + ')'); return }
            if (!can.existing) generated++
            let st = Item.of(id)
            // food values (one serving)
            let props = food(id)
            if (can.food === false) {
                let inedible = props == null && !st.getItem().isEdible()
                if (inedible) foodOk++
                ok(inedible, key + ' not edible (' + foodDesc(props) + ')')
            } else {
                let want = (can.effects || []).map(fx => fx.effect + '|' + fx.ticks + '|' + fx.amp + '|' +
                    Math.round(Number(fx.chance) * 100) / 100).sort()
                let good = props != null && props.getNutrition() == can.hunger && near(props.getSaturationModifier(), can.modifier) &&
                    props.isMeat() == !!can.meat && props.isFastFood() == !!can.fast && props.canAlwaysEat() == !!can.always &&
                    effectsOf(props).join(',') == want.join(',')
                if (good) foodOk++
                else ok(false, key + ' food ' + foodDesc(props) + ', json ' + can.hunger + ' / ' + can.modifier +
                    (can.meat ? ' meat' : '') + (can.fast ? ' fast' : '') + (can.always ? ' always' : '') + ' [' + want.join(', ') + ']')
            }
            // servings
            let serv = -1
            try { serv = $OFServings.maxServings(st) } catch (e) { }
            if (serv == can.servings) servOk++
            else ok(false, key + ' servings ' + serv + ', json ' + can.servings)
            // recipes
            ;(can.recipes || []).forEach(rec => {
                recAll++
                let rid = CE + (can.pot ? 'farmersdelightcooking/' : 'shapeless/') + key + rec.suffix
                let r = recipe(rid)
                if (!r) { ok(false, rid + ' present'); return }
                let res = result(r)
                let slots = 0
                rec.inputs.forEach(inp => slots += inp.n)
                if (!can.pot) slots += 1   // the empty can
                let ings = ingIds(r)
                let firstOk = rec.inputs.every(inp => {
                    let want = []
                    inp.any.forEach(k => want = want.concat(idsOfKey(k)))
                    return ings.some(slot => slot.some(x => want.indexOf(x) >= 0))
                })
                let canOk = can.pot ? true : takesId(r, i.empty_can)
                if (can.pot) {
                    try { canOk = idOf(r.getOutputContainer()) == i.empty_can } catch (e) { canOk = false }
                }
                if (res.id == id && res.n == 1 && ings.length == slots && firstOk && canOk) recOk++
                else ok(false, rid + ': ' + res.n + 'x ' + res.id + ', ' + ings.length + ' slots (want ' + slots + '), inputs ' +
                    (firstOk ? 'ok' : 'missing') + ', empty can ' + (canOk ? 'ok' : 'missing'))
            })
            if (can.pie) {
                recAll++
                let pid = CE + 'shapeless/' + key + '_pie_manual_only'
                let r = recipe(pid)
                if (r && result(r).id == i[can.pie]) recOk++
                else ok(false, pid + ' makes ' + i[can.pie] + (r ? ' (' + result(r).id + ')' : ' (missing)'))
            }
        })
        ok(foodOk == keys.length, 'food values as the json: ' + foodOk + ' of ' + keys.length)
        ok(servOk == keys.length, 'servings as the json: ' + servOk + ' of ' + keys.length)
        ok(recOk == recAll, 'can recipes as the json: ' + recOk + ' of ' + recAll)
        info(generated + ' generated cans registered; startup.log "[cans] registered N generated cans" must say ' + generated)
    }

    //######################### canextra: values named in the TODO rows #########################
    function canextra() {
        function fx(key, effect, ticks, chance) {
            let props = food(i[key])
            let want = effect + '|' + ticks + '|0|' + chance
            ok(props != null && effectsOf(props).indexOf(want) >= 0, key + ' has ' + want + ' (' + foodDesc(props) + ')')
        }
        // canning #4: migrated raw cans carry their food's effects
        fx('canned_banana', 'neapolitan:agility', 300, 1)
        fx('canned_glow_berry', 'minecraft:glowing', 300, 1)
        fx('canned_salmonberry', 'minecraft:water_breathing', 100, 1)
        fx('canned_raw_chicken', 'minecraft:hunger', 600, 0.3)
        // canning #14: Coffee Berries keep Wither 2 s (80 %) and Caffeinated 10 s
        fx('canned_coffee_berries', 'minecraft:wither', 40, 0.8)
        fx('canned_coffee_berries', 'farmersrespite:caffeinated', 200, 1)
        // CP-20: cactus flesh 4, or 3 + salt
        let cf = recipe(CE + 'shapeless/canned_cactus_flesh'), cfs = recipe(CE + 'shapeless/canned_cactus_flesh_salt')
        ok(cf != null && ingIds(cf).length == 5 && cfs != null && ingIds(cfs).length == 5 && takes(cfs, 'salt'),
            'Canned Cactus Flesh: 4 + can, 3 + salt + can')
        // canning #5: pumpkin is a fruit can, 3 servings, Fruit Meal values, 4 slices or 3 + sugar
        let pk = food(i.canned_pumpkin)
        ok(pk != null && pk.getNutrition() == 5 && near(pk.getSaturationModifier(), 0.3), 'Canned Pumpkin 5 / 0.3 (' + foodDesc(pk) + ')')
        ok($OFServings.maxServings(Item.of(i.canned_pumpkin)) == 3, 'Canned Pumpkin 3 servings')
        let pkr = recipe(CE + 'shapeless/canned_pumpkin_sugar')
        ok(pkr != null && takes(pkr, 'pumpkin_slice') && takes(pkr, 'sugar') && ingIds(pkr).length == 5,
            'Canned Pumpkin: 3 slices + sugar + can')
        // CP-09: cantaloupe slice eats as a pumpkin slice (no Resistance)
        let cs = food(i.cantaloupe_slice), ps = food(i.pumpkin_slice)
        ok(cs != null && ps != null && foodDesc(cs) == foodDesc(ps), 'cantaloupe slice ' + foodDesc(cs) + ' = pumpkin slice ' + foodDesc(ps))
        // canning #15: Canned Cake
        let cake = food(i.canned_cake)
        ok(cake != null && cake.getNutrition() == 4 && cake.isFastFood() && cake.getSaturationModifier() < 1,
            'Canned Cake 4 hunger, fast, modifier below 1 (' + foodDesc(cake) + ')')
        fx('canned_cake', 'minecraft:speed', 400, 1)
        ok($OFServings.maxServings(Item.of(i.canned_cake)) == 4, 'Canned Cake 4 servings')
        let c1 = recipe(CE + 'shapeless/canned_cake'), c4 = recipe(CE + 'shapeless/canned_cake_slices')
        ok(c1 != null && takes(c1, 'cake') && ingIds(c1).length == 2, 'Canned Cake from 1 cake + can')
        ok(c4 != null && takes(c4, 'cake_slice') && takes(c4, 'sugar') && takes(c4, 'honey_bottle') && ingIds(c4).length == 6,
            'Canned Cake from 4 slices + sugar or honey + can (' + (c4 ? JSON.stringify(ingIds(c4)) : 'missing') + ')')
        // types-and-tuning #2: no Canned Goods can keeps a saturation modifier above its family's (were up to 42)
        let high = []
        Object.keys(canTypes()).forEach(k => {
            let p = food(i[k])
            if (p != null && p.getSaturationModifier() > 10) high.push(k + ' ' + p.getSaturationModifier())
        })
        ok(high.length == 0, 'no can with a saturation modifier above 10 (' + high.join(', ') + ')')
        // Canned Goods' cookie cans no longer meat
        let meatCookies = Object.keys(canTypes()).filter(k => /cookie/.test(k) && food(i[k]) != null && food(i[k]).isMeat())
        ok(meatCookies.length == 0, 'no cookie can is meat (' + meatCookies.join(', ') + ')')
    }

    //######################### canremoved #########################
    function canremoved() {
        let c = canTypes()
        let canIds = Object.keys(c).map(k => i[k]).filter(x => typeof x === 'string')
        let partial = [], removed = []
        RM.getRecipes().forEach(r => {
            let s
            try { s = r.getResultItem() } catch (e) { return }
            let id = idOf(s)
            if (id == i.canned_food || id == i.hot_hot_hotpot) removed.push(String(r.getId()))
            if (canIds.indexOf(id) < 0) return
            let tagged = false
            try { tagged = s.hasTag() } catch (e) { try { tagged = s.getNbt() != null } catch (e2) { } }
            if (tagged) partial.push(String(r.getId()))
        })
        ok(partial.length == 0, 'no recipe makes a can with NBT (partly full) (' + partial.join(', ') + ')')
        ok(removed.length == 0, 'no recipe makes Canned Food or the Hot Hot Hotpot (' + removed.join(', ') + ')')
        // butcher: Canned Goods' Canned Food trade (level 2) removed, other trades kept. Real villagers roll their
        // offers: Rhino can't wrap VillagerTrades on a dedicated server (an uncaught Java error), so no TRADES read.
        try {
            let level = Utils.getServer().overworld()
            let seen = {}, canned = 0, offersN = 0
            for (let k = 0; k < 60; k++) {
                let v = $OFEntityType.VILLAGER.create(level)
                v.setVillagerData(v.getVillagerData().setProfession($OFProf.BUTCHER).setLevel(2))
                let offers = v.getOffers()
                for (let j = 0; j < offers.size(); j++) {
                    let id = idOf(offers.get(j).getResult())
                    offersN++
                    seen[id] = (seen[id] || 0) + 1
                    if (id == i.canned_food) canned++
                }
            }
            info('butcher level 2 offers over 60 villagers: ' + JSON.stringify(seen))
            ok(canned == 0, 'butcher sells no Canned Food (' + canned + ' of ' + offersN + ' level 2 offers)')
            ok(Object.keys(seen).length > 1, 'butcher keeps its other trades (' + Object.keys(seen).length + ' kinds)')
        } catch (e) { fail++; log('FAIL butcher trades: ERROR ' + e) }
    }

    //######################### cornmeal (ingredients #1) #########################
    function cornmeal() {
        let knife = present(CE + 'farmersdelightcutting/corn_kernels', 'corn_seeds', null)
        if (knife) ok(outputs(knife).join(',').indexOf('4x ' + i.corn_seeds) >= 0, 'knife: 1 maize -> 4 kernels (' + outputs(knife).join(', ') + ')')
        present(CE + 'iecrushing/corn_cornmeal', 'cornmeal', 2)
        present(CE + 'iecrushing/corn_crate_cornmeal', 'cornmeal', 18)
        let pnc = recipe(CE + 'pcpressurechamber/corn_kernels_cornmeal')
        ok(pnc != null, CE + 'pcpressurechamber/corn_kernels_cornmeal present')
        if (pnc) info('PNC kernels: ' + outputs(pnc).join(', ') + ' (want 3x ' + i.cornmeal + ' from 4 kernels)')
        let batter = present(CE + 'shapeless/cornbread_batter', 'cornbread_batter', 3)
        if (batter) ok(takes(batter, 'cornmeal') && !takes(batter, 't_crops_corn'), 'cornbread batter takes cornmeal, not corn')
        ;['corn_delight:cutting/corn', 'corn_delight:cutting/wild_corn', 'corn_delight:tortilla_raw',
            'corn_delight:integration/create/splashing/tortilla_raw', 'corn_delight:cornbread_batter',
            'corn_delight:integration/create/mixing/cornbread_batter_from_mixing',
            'corn_delight:integration/immersiveengineering/crusher/corn',
            'corn_delight:integration/immersiveengineering/crusher/wild_corn',
            'corn_delight:integration/create/milling/corn', 'corn_delight:integration/create/milling/wild_corn'].forEach(absent)
        let cornIds = idsOfKey('t_crops_corn')
        let fromCorn = makersOf([i.tortilla_raw, i.cornbread_batter]).filter(r => ingIds(r).some(s => s.some(x => cornIds.indexOf(x) >= 0)))
        ok(fromCorn.length == 0, 'no raw tortilla or cornbread batter recipe takes corn (' + fromCorn.map(r => String(r.getId())).join(', ') + ')')
        let tagOk = false
        try { tagOk = Ingredient.of(i.t_flour_corn).test(Item.of(i.cornmeal)) } catch (e) { }
        ok(tagOk, 'cornmeal in ' + i.t_flour_corn)
        // pancakes accept cornmeal (RC-17): listed for the runner
        makersOf(idsOfKey('pancakes')).forEach(r => info('pancakes: ' + r.getId() + ' takes cornmeal: ' + takes(r, 'cornmeal')))
    }

    //######################### muffin (oven #5) #########################
    function muffin() {
        present(CE + 'baking/blueberry_muffin', 'blueberry_muffin', 4)
        present(CE + 'baking/cranberry_muffin', 'cranberry_muffin', 4)
        present(CE + 'farmersdelightcooking/blueberry_muffin', 'blueberry_muffin', 2)
        present(CE + 'farmersdelightcooking/cranberry_muffin', 'cranberry_muffin', 2)
    }

    //######################### meals (meals #1, #2) #########################
    function meals() {
        // #1 heatless grid routes
        ;['vegetable_soup', 'vegetable_noodles', 'ratatouille', 'corn_soup'].forEach(k => present(CE + 'shapeless/' + k + '_cooked_manual_only', k, 1))
        let veg = idsOfKey('t_soup_veggie_cooked')
        ok(veg.length > 0, i.t_soup_veggie_cooked + ' holds ' + veg.length + ' items')
        let loose = []
        RM.getRecipes().forEach(r => {
            let id = String(r.getId())
            // can recipes (shapeless/canned_cooked_*) are not meals: cans may be automated
            if (id.indexOf(CE + 'shapeless/') == 0 && id.indexOf(CE + 'shapeless/canned_') != 0 && /_cooked/.test(id) && !/_manual_only$/.test(id)) loose.push(id)
        })
        ok(loose.length == 0, 'every cooked shapeless meal ends _manual_only (' + loose.join(', ') + ')')
        // #2 cooked inputs
        let fried = present(CE + 'shapeless/fried_rice_cooked_manual_only', 'fried_rice', 1)
        if (fried) ok(takes(fried, 'cooked_eggs') && !takesId(fried, 'minecraft:egg'), 'fried rice takes cooked eggs, not raw')
        let mush = present(CE + 'shapeless/mushroom_rice_cooked_manual_only', 'mushroom_rice', 1)
        if (mush) ok(takes(mush, 'cooked_rice') && takes(mush, 'baked_potato'), 'mushroom rice takes cooked rice and a baked potato')
        present(CE + 'shapeless/baked_cod_stew_cooked_manual_only', 'baked_cod_stew', 1)
        let soup = present(CE + 'shapeless/chicken_soup_cooked_manual_only', 'chicken_soup', 1)
        if (soup) ok(takes(soup, 'cooked_chicken') && !takesId(soup, 'minecraft:chicken'), 'chicken soup takes cooked chicken')
        // mutton wrap on a tortilla
        absent('farmersdelight:mutton_wrap')
        absent('create_central_kitchen:sequenced_assembly/mutton_wrap')
        let wrap = present(CE + 'shapeless/mutton_wrap_manual_only', 'mutton_wrap', 1)
        if (wrap) ok(takes(wrap, 't_tortilla') && !takesId(wrap, 'minecraft:bread'), 'mutton wrap takes a tortilla, not bread')
        let wrapA = present(CE + 'createsequencedassembly/mutton_wrap_sequenced_assembly', 'mutton_wrap', 1)
        if (wrapA) {
            let base = []
            try { base = stacksOf(wrapA.getIngredient()).map(idOf) } catch (e) { }
            let tort = idsOfKey('t_tortilla')
            ok(base.some(x => tort.indexOf(x) >= 0), 'mutton wrap assembly starts on a tortilla (' + base.join(', ') + ')')
        }
        // scrambled eggs, omelet
        absent('incubation:scrambled_eggs')
        let eggs = present(CE + 'shapeless/scrambled_eggs_manual_only', 'scrambled_eggs', 1)
        if (eggs) {
            let eggIds = idsOfKey('eggs')
            ok(ingIds(eggs).filter(s => s.some(x => eggIds.indexOf(x) >= 0)).length == 3, 'scrambled eggs take 3 eggs')
        }
        let omelet = recipe('brewinandchewin:cooking/vegetable_omelet')
        ok(omelet != null && takes(omelet, 'cooked_eggs') && !takesId(omelet, 'minecraft:egg'), 'vegetable omelet pot takes fried eggs')
        present(CE + 'shapeless/vegetable_omelet_from_omelette_manual_only', 'vegetable_omelet', 1)
        // fruit bowls
        absent('quarkdelight:bowl_of_exotic_fruits')
        absent('farmersdelight:fruit_salad')
        present(CE + 'shapeless/bowl_of_exotic_fruits_manual_only', 'bowl_of_exotic_fruits', 1)
        present(CE + 'shapeless/fruit_salad_manual_only', 'fruit_salad', 1)
        present(CE + 'createsequencedassembly/fruit_salad_sequenced_assembly', 'fruit_salad', 2)
        ok(idsOfKey('t_exotic_fruit').length > 0 && idsOfKey('t_fruit_salad_slice').length > 0,
            'exotic fruit ' + idsOfKey('t_exotic_fruit').length + ', fruit salad slice ' + idsOfKey('t_fruit_salad_slice').length + ' items')
        // burgers: one shaped grid recipe each, plus one assembly
        let BURGERS = ['hamburger', 'cheeseburger', 'deluxe_cheeseburger', 'simple_hamburger', 'hamburger_with_egg',
            'loaded_hamburger', 'bison_burger', 'kangaroo_burger']
        ;['farmersdelight:hamburger', 'create_central_kitchen:sequenced_assembly/hamburger', 'delightful:food/cheeseburger',
            'delightful:food/cheeseburger_from_milk', 'delightful:food/deluxe_cheeseburger',
            'delightful:food/deluxe_cheeseburger_from_milk', 'moredelight:simple_hamburger', 'moredelight:hamburger_with_egg',
            'moredelight:loaded_hamburger', 'alexsdelight:bison_burger', 'alexsmobs:kangaroo_burger'].forEach(absent)
        info('hamburger makers: ' + makersOf([i.hamburger]).map(r => r.getId() + ' = ' + typeOf(r)).join(', '))
        BURGERS.forEach(k => {
            present(CE + 'shaped/' + k + '_manual_only', k, 1)
            present(CE + 'createsequencedassembly/' + k + '_sequenced_assembly', k, 1)
            let grid = makersOf([i[k]]).filter(isGrid)
            ok(grid.length == 1, k + ': one grid recipe (' + grid.map(r => String(r.getId())).join(', ') + ')')
            let milk = grid.filter(r => takes(r, 'milk'))
            ok(milk.length == 0, k + ': no milk')
        })
        // quiche: one shaped grid recipe
        absent('brewinandchewin:quiche_from_bacon')
        absent('brewinandchewin:quiche_from_mushroom')
        present(CE + 'shaped/quiche_manual_only', 'quiche', 1)
        present(CE + 'createsequencedassembly/quiche_sequenced_assembly', 'quiche', 1)
        // brewinandchewin:quiche_from_slices puts 4 slices back together: not a way to make one
        let qgrid = makersOf([i.quiche]).filter(isGrid).filter(r => !/_from_slices$/.test(String(r.getId())))
        ok(qgrid.length == 1, 'quiche: one grid recipe (' + qgrid.map(r => String(r.getId())).join(', ') + ')')
    }

    //######################### assembly (meals #3) #########################
    function assembly() {
        absent('farmersdelight:shepherds_pie_block')
        let sp = present(CE + 'shapeless/shepherds_pie_manual_only', 'shepherds_pie_block', 1)
        if (sp) ok(takes(sp, 't_cheese_wedge') && !takes(sp, 'milk'), "shepherd's pie takes a cheese wedge, not milk")
        present(CE + 'createsequencedassembly/shepherds_pie_sequenced_assembly', 'shepherds_pie_block', 1)
        present(CE + 'createsequencedassembly/horror_lasagna_sequenced_assembly', 'horror_lasagna', 2)
        present(CE + 'createsequencedassembly/stuffed_potato_sequenced_assembly', 'stuffed_potato', 1)
        ;['crispy_skewer', 'crawling_sandwich', 'steak_and_potatoes'].forEach(k => {
            present(CE + 'shaped/' + k + '_manual_only', k, 1)
            present(CE + 'createsequencedassembly/' + k + '_sequenced_assembly', k, 1)
            let grid = makersOf([i[k]]).filter(isGrid)
            ok(grid.length == 1, k + ': only the shaped grid recipe (' + grid.map(r => String(r.getId())).join(', ') + ')')
        })
        absent('endersdelight:crispy_skewer')
        absent('endersdelight:crawling_sandwich')
        absent('farmersdelight:steak_and_potatoes')
        // horror lasagna: pasta first, so a bowl under a tomato sauce spout stays tomato sauce
        let hl = recipe(CE + 'createsequencedassembly/horror_lasagna_sequenced_assembly')
        if (hl) {
            let first = ''
            try { first = String(hl.getSequence().get(0).getRecipe().getId()) + ' ' + ingIds(hl.getSequence().get(0).getRecipe()).map(s => s[0]).join(',') } catch (e) { first = 'ERROR ' + e }
            info('horror lasagna first step: ' + first + ' (want pasta)')
        }
    }

    //######################### expensive (meals #4) #########################
    function expensive() {
        ;['chorus_stew', 'endermite_stew', 'crab_pasta', 'pumpkin_soup', 'pasta_with_meatballs', 'dumplings',
            'cavern_explorer_stew', 'squid_ink_pasta'].forEach(k => present(CE + 'shapeless/' + k + '_cooked_manual_only', k, 1))
        let mix = present(CE + 'createmixing/squid_ink_pasta_mixing', 'squid_ink_pasta', 2)
        if (mix) {
            let fl = fluidIds(mix)
            let heat = '?'
            try { heat = String(mix.getRequiredHeat()) } catch (e) { }
            ok(fl.length >= 2 && fl.some(x => /ink/.test(x)) && fl.some(x => /tomato/.test(x)) && /HEATED/i.test(heat),
                'squid ink pasta mixing: ink + tomato sauce, heated (' + fl.join(', ') + ', ' + heat + ')')
        }
    }

    //######################### frying (meals #7) #########################
    function frying() {
        ;['pneumaticcraft:thermo_plant/chips', 'pneumaticcraft:thermo_plant/salmon_tempura',
            'pneumaticcraft:raw_salmon_tempura', 'pneumaticcraft:cod_n_chips'].forEach(absent)
        let raw = present(CE + 'shapeless/raw_fish_tempura', 'raw_salmon_tempura', 1)
        if (raw) ok(takesId(raw, 'minecraft:cod') && takesId(raw, 'minecraft:salmon') && takes(raw, 'batter'),
            'raw fish tempura: any whole fish (cod, salmon) + batter')
        let tem = present(CE + 'farmersdelightcooking/fish_tempura', 'salmon_tempura', 1)
        if (tem) ok(takes(tem, 't_cooking_oil'), 'fish tempura takes cooking oil')
        let fries = present(CE + 'farmersdelightcooking/fries', 'chips', 2)
        if (fries) ok(takes(fries, 't_cooking_oil') && takes(fries, 't_sliced_potatoes'), 'fries: cooking oil + sliced potatoes')
        let fc = present(CE + 'farmersdelightcooking/fish_and_chips', 'cod_n_chips', 1)
        if (fc) {
            let cont = '?'
            try { cont = idOf(fc.getOutputContainer()) } catch (e) { }
            ok(cont == i.paper, 'fish and chips plated on paper (' + cont + ')')
        }
        present(CE + 'shapeless/fish_and_chips_cooked_manual_only', 'cod_n_chips', 1)
        let sliced = idsOfKey('t_sliced_potatoes')
        ok(sliced.indexOf(i.potato_slices) >= 0, i.t_sliced_potatoes + ' holds ' + i.potato_slices)
        ok(idsOfKey('t_cooking_oil').length > 0, i.t_cooking_oil + ' holds ' + idsOfKey('t_cooking_oil').join(', '))
        let oily = makersOf([i.salmon_tempura, i.chips, i.cod_n_chips]).filter(r => fluidIds(r).some(x => /plantoil|plant_oil/.test(x)))
        ok(oily.length == 0, 'no plant-oil frying left (' + oily.map(r => String(r.getId())).join(', ') + ')')
        makersOf([i.salmon_tempura, i.chips, i.cod_n_chips]).forEach(r => info('fry route: ' + r.getId() + ' (' + typeOf(r) + ')'))
    }

    //######################### dogfood (meals #8) #########################
    function dogfood() {
        let r = present(CE + 'shapeless/dog_food_cooked_manual_only', 'dog_food', 1)
        if (r) ok(ingIds(r).length == 5 && takes(r, 'rotten_flesh') && takes(r, 'bonemeal') && takes(r, 'cooked_meat') &&
            takes(r, 'cooked_rice'), 'dog food: rotten flesh + bone meal + 2 cooked meat + cooked rice')
    }

    //######################### melon (meals #6) #########################
    function melon() {
        let g = food(i.glistering_melon_slice), src = food(i.golden_melon_slice)
        ok(g != null, 'Glistering Melon Slice is food (' + foodDesc(g) + ')')
        if (g && src) {
            ok(g.getNutrition() == src.getNutrition() && near(g.getSaturationModifier(), src.getSaturationModifier()),
                'glistering ' + foodDesc(g) + ' = golden melon slice values ' + foodDesc(src))
            ok(g.canAlwaysEat(), 'Glistering Melon Slice always edible')
            ok(effectsOf(g).length == 0, 'Glistering Melon Slice has no effects')
        }
        let tagged = false
        try { tagged = Ingredient.of(i.t_food_golden).test(Item.of(i.glistering_melon_slice)) } catch (e) { }
        ok(tagged, 'Glistering Melon Slice in ' + i.t_food_golden)
    }

    const PARTS = { cans: cans, canextra: canextra, canremoved: canremoved, cornmeal: cornmeal, muffin: muffin,
        meals: meals, assembly: assembly, expensive: expensive, frying: frying, dogfood: dogfood, melon: melon }

    function run(server, part) {
        pass = 0; fail = 0
        RM = server.getRecipeManager()
        let names = part == 'all' ? Object.keys(PARTS) : [part]
        names.forEach(n => {
            if (!PARTS[n]) { fail++; log('FAIL unknown part ' + n + ' (' + Object.keys(PARTS).join('|') + '|all)'); return }
            log('== ' + n)
            try { PARTS[n]() } catch (e) { fail++; log('FAIL ' + n + ' ERROR ' + e) }
        })
        log('done: ' + pass + ' pass, ' + fail + ' fail')
    }

    ServerEvents.commandRegistry(event => {
        const { commands: Commands, arguments: Arguments } = event
        event.register(Commands.literal('ofprobe')
            .then(Commands.argument('part', Arguments.GREEDY_STRING.create(event)).executes(ctx => {
                run(ctx.source.getServer(), String(Arguments.GREEDY_STRING.getResult(ctx, 'part')).trim())
                return 1
            }))
            .executes(ctx => { run(ctx.source.getServer(), 'all'); return 1 }))
    })
})()
