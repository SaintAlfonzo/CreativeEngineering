// Baking overhaul probe (feature_writeup/oven.md 2.6, tests OV-25 to OV-31, queue: feature_writeup/testing/scratch_queue.md).
// Scratch-only: copy into a scratch server's kubejs/server_scripts (tools/creative_labels_test/scratch_server.sh),
// never into the pack. Run /bktest; results are "[bktest] ..." lines in logs/latest.log, ending in
// "[bktest] done: N PASS, M FAIL".
//
//  items      frosting and cookie dough are registered
//  audit      every loaded recipe whose result is a cake, pie, cookie, cake base, pie crust, frosting or cookie dough
//             is the pack's own (creativeengineering:) or a *_from_slices recipe; every other one is a FAIL
//  ids        the recipes the overhaul expects are loaded (crafting, oven, pot)
//  automation no recipe for a base (cake base, crust, frosting, cookie dough) is a Create or IE type, and every
//             crafting one is skipped by Create's automation (AllRecipeTypes.shouldIgnoreInAutomation)
//  oven       BakingRecipe.matches on a plain ItemStackHandler: apple pie from 2 apples + sugar + honey bottle,
//             crust + full filling can (true), crust + an opened can (false), no pie tin (false)
(() => {
    const $BKHandler = Java.loadClass('net.minecraftforge.items.ItemStackHandler')
    const $BKWrapper = Java.loadClass('net.minecraftforge.items.wrapper.RecipeWrapper')
    const $BKRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $BKForgeReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $BKServings = Java.loadClass('com.SaintAlfonzo.creativeengineering.food.CanServings')
    const $BKOven = Java.loadClass('com.SaintAlfonzo.creativeengineering.oven.OvenBlockEntity')
    const $BKConfig = Java.loadClass('com.SaintAlfonzo.creativeengineering.CEConfig')

    const i = global.item
    let pass = 0, fail = 0
    function log(s) { console.info('[bktest] ' + s) }
    function ok(cond, what) { if (cond) { pass++; log('PASS ' + what) } else { fail++; log('FAIL ' + what) } }
    function idOf(st) { return String($BKForgeReg.ITEMS.getKey(st.getItem())) }

    const CAKES = ['cake', 'adzuki_cake', 'banana_cake', 'chocolate_cake', 'neapolitan_chocolate_cake', 'honey_cake',
        'mint_cake', 'strawberry_cake', 'vanilla_cake', 'coffee_cake', 'mangosteen_cake']
    const PIES = ['apple_pie', 'chocolate_pie', 'chorus_pie', 'mulberry_pie', 'pumpkin_pie', 'rose_hip_pie',
        'salmonberry_pie', 'durian_pie', 'pineapple_pie', 'sweet_berry_cheesecake']
    const COOKIES = ['cookie', 'honey_cookie', 'sweet_berry_cookie', 'green_tea_cookie', 'bayberry_cookie',
        'cranberry_cookie', 'persimmon_cookie', 'lemon_cookie', 'mulberry_cookie', 'uncanny_cookies', 'cherry_cookie']
    const BASES = ['cake_base', 'cake_base_baked', 'pie_crust', 'frosting', 'cookie_dough']
    const FILLINGS = ['apple', 'chocolate', 'chorus', 'mulberry', 'pumpkin', 'rose_hip', 'salmonberry', 'durian', 'pineapple']

    function items() {
        ok(Item.of(i.frosting).getId() == i.frosting, 'item ' + i.frosting + ' registered')
        ok(Item.of(i.cookie_dough).getId() == i.cookie_dough, 'item ' + i.cookie_dough + ' registered')
    }

    function byOutput(server) {
        let watched = {}
        CAKES.concat(PIES, COOKIES, BASES).forEach(k => { watched[i[k]] = k })
        let out = {}
        server.getRecipeManager().getRecipes().forEach(r => {
            let res
            try { res = r.getResultItem() } catch (e) { return }
            if (res == null || res.isEmpty()) return
            let k = watched[idOf(res)]
            if (k) (out[k] = out[k] || []).push(r)
        })
        return out
    }

    function audit(found) {
        let foreign = 0
        Object.keys(found).forEach(k => found[k].forEach(r => {
            let id = String(r.getId())
            if (id.startsWith('creativeengineering:') || id.endsWith('_from_slices')) return
            foreign++
            log('FAIL leftover recipe ' + id + ' (' + r.getType() + ') -> ' + k)
        }))
        ok(foreign == 0, 'audit: no other mod recipe makes a cake / pie / cookie / base (' + foreign + ' left)')
        Object.keys(found).sort().forEach(k => log('recipes for ' + k + ': ' + found[k].map(r => String(r.getId())).join(', ')))
    }

    function ids(server) {
        let rm = server.getRecipeManager()
        let want = ['shapeless/cake_base_flour_manual_only', 'shapeless/cake_base_dough_manual_only',
            'shapeless/pie_crust_flour_manual_only', 'shapeless/pie_crust_dough_manual_only',
            'shapeless/frosting_manual_only', 'shapeless/cookie_dough_manual_only',
            'shapeless/cake_manual_only', 'shapeless/cake_berry_manual_only', 'shapeless/strawberry_cake_manual_only',
            'baking/cake_base_baked_flour', 'baking/cake_base_baked_dough', 'baking/cake_base_baked',
            'baking/cake', 'baking/cake_berry', 'baking/strawberry_cake', 'baking/sweet_berry_cheesecake',
            'baking/cookie', 'baking/cherry_cookie', 'baking/durian_pie']
        FILLINGS.forEach(f => {
            want.push('baking/' + f + '_pie', 'baking/' + f + '_pie_from_filling',
                'farmersdelightcooking/canned_' + f + '_pie_filling_pot')
        })
        ;['apple', 'chocolate', 'chorus', 'mulberry', 'pumpkin', 'rose_hip', 'salmonberry'].forEach(f =>
            want.push('shapeless/canned_' + f + '_pie_filling_pie_manual_only'))
        let missing = want.filter(w => !rm.byKey(new $BKRL('creativeengineering', w)).isPresent())
        missing.forEach(w => log('FAIL missing recipe creativeengineering:' + w))
        ok(missing.length == 0, 'ids: ' + (want.length - missing.length) + ' of ' + want.length + ' expected recipes loaded')
        ok(!rm.byKey(new $BKRL('creativeengineering', 'createcompacting/pie_crust_flour')).isPresent(),
            'ids: crust compacting recipe gone')
        let spout = rm.byKey(new $BKRL('creativeengineering', 'createfilling/chocolate_cake_spout'))
        ok(spout.isPresent() && String(spout.get().getType()) == 'create:filling'
            && idOf(spout.get().getResultItem()) == i.chocolate_cake,
            'spout: chocolate cake filling recipe loaded (create:filling -> ' + i.chocolate_cake + ')')
    }

    function automation(found) {
        let $AllTypes = null
        try { $AllTypes = Java.loadClass('com.simibubi.create.AllRecipeTypes') } catch (e) { log('Create AllRecipeTypes not loadable: ' + e) }
        BASES.forEach(k => (found[k] || []).forEach(r => {
            let type = String(r.getType())
            let id = String(r.getId())
            ok(!type.startsWith('create:') && !type.startsWith('immersiveengineering:'), 'automation: ' + id + ' is ' + type)
            if (type == 'minecraft:crafting' && $AllTypes != null)
                ok($AllTypes.shouldIgnoreInAutomation(r), 'automation: Create skips ' + id)
        }))
    }

    function oven(server, level) {
        let rm = server.getRecipeManager()
        function recipe(path) { return rm.byKey(new $BKRL('creativeengineering', path)).orElse(null) }
        function inv(stacks, tool) {
            let h = new $BKHandler($BKOven.INVENTORY_SIZE)
            stacks.forEach((s, n) => h.setStackInSlot(n, s))
            if (tool) h.setStackInSlot($BKOven.TOOL_SLOT, Item.of(i.pie_tin))
            return new $BKWrapper(h)
        }
        let pie = recipe('baking/apple_pie')
        let fromCan = recipe('baking/apple_pie_from_filling')
        if (pie == null || fromCan == null) { ok(false, 'oven: apple pie recipes loaded'); return }
        // [cooking] ovenIngredientCounts off (the default): n(apple, 2) is 2 slots of 1; on: 2 in one slot
        let counts = String($BKConfig.COOKING.ovenIngredientCounts.get()) === 'true'  // boxed Boolean: compare as text
        let apples = counts ? [Item.of(i.apple, 2)] : [Item.of(i.apple), Item.of(i.apple)]
        ok(pie.matches(inv([Item.of(i.pie_crust)].concat(apples, [Item.of(i.sugar), Item.of(i.honey_bottle)]), true), level),
            'oven: crust + 2 apples + sugar + honey bottle matches apple_pie (ovenIngredientCounts ' + counts + ')')
        let full = Item.of(i.canned_apple_pie_filling)
        ok(fromCan.matches(inv([Item.of(i.pie_crust), full], true), level), 'oven: crust + full filling can matches')
        let max = $BKServings.maxServings(full)
        let opened = $BKServings.withServings(Item.of(i.canned_apple_pie_filling), Math.max(1, max - 1))
        log('filling can servings: max ' + max + ', opened partial = ' + $BKServings.isPartial(opened))
        ok($BKServings.isPartial(opened) && !fromCan.matches(inv([Item.of(i.pie_crust), opened], true), level),
            'oven: crust + opened filling can does not match')
        ok(!fromCan.matches(inv([Item.of(i.pie_crust), Item.of(i.canned_apple_pie_filling)], false), level),
            'oven: no pie tin, no match')
    }

    ServerEvents.commandRegistry(event => {
        const { commands: Commands } = event
        event.register(Commands.literal('bktest').executes(ctx => {
            let server = ctx.source.getServer()
            let level = ctx.source.getLevel()
            pass = 0; fail = 0
            let found = {}
            try { items() } catch (e) { ok(false, 'items ERROR ' + e) }
            try { found = byOutput(server) } catch (e) { ok(false, 'byOutput ERROR ' + e) }
            try { audit(found) } catch (e) { ok(false, 'audit ERROR ' + e) }
            try { ids(server) } catch (e) { ok(false, 'ids ERROR ' + e) }
            try { automation(found) } catch (e) { ok(false, 'automation ERROR ' + e) }
            try { oven(server, level) } catch (e) { ok(false, 'oven ERROR ' + e) }
            log('done: ' + pass + ' PASS, ' + fail + ' FAIL')
            return 1
        }))
    })
})()
