// Food / drinks scratch-server probe (scratch queue SQ-FL, SQ-DR, SQ-IN, SQ-MJ, SQ-SOL, SQ-CO;
// feature_writeup/recipes_cooking.md RC-71 to RC-78, mixed_jars.md MJ-11 to MJ-14, food.md FO-44,
// item_merges.md 2.8). Scratch-only: copy into a scratch server's kubejs/server_scripts as zz_fd_probe.js
// (tools/creative_labels_test/scratch_server.sh setup, then cp), never into the pack.
//
//   /fdtest <part>   part = fluids | recipes | ice | milkjar | soupjar | sol | coffee | all
// Results are "[fdtest] PASS|FAIL <part> <name>: <detail>" lines in logs/latest.log, then
// "[fdtest] done <part>: N pass, M fail".
//
// Player clicks go through ServerPlayerGameMode.useItemOn on a Forge fake player (tools/jar_mixing_test/probe.js
// pattern). Not covered (client or a real player): JEI, names, colours, sounds, the Spice of Life bite itself
// (PlacedFoodSol skips fake players on purpose), the kettle and oven screens.
(() => {
    const $FDFake = Java.loadClass('net.minecraftforge.common.util.FakePlayerFactory')
    const $FDProfile = Java.loadClass('com.mojang.authlib.GameProfile')
    const $FDUUID = Java.loadClass('java.util.UUID')
    const $FDDir = Java.loadClass('net.minecraft.core.Direction')
    const $FDBlockPos = Java.loadClass('net.minecraft.core.BlockPos')
    const $FDVec3 = Java.loadClass('net.minecraft.world.phys.Vec3')
    const $FDHit = Java.loadClass('net.minecraft.world.phys.BlockHitResult')
    const $FDHand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $FDReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $FDRL = Java.loadClass('net.minecraft.resources.ResourceLocation')

    const Y = -58
    let pass = 0, fail = 0, part = ''
    function check(name, ok, detail) {
        if (ok) pass++; else fail++
        console.info('[fdtest] ' + (ok ? 'PASS' : 'FAIL') + ' ' + part + ' ' + name + ': ' + detail)
    }
    function rl(id) { return new $FDRL(id) }
    function item(id) { return $FDReg.ITEMS.getValue(rl(id)) }
    function itemId(st) { return String($FDReg.ITEMS.getKey(st.getItem())) }
    function block(id) { return $FDReg.BLOCKS.getValue(rl(id)) }

    // id -> recipe, built once per command (never getType() on every recipe: memory rhino-fieldandmethods-gotcha)
    let byId = null, aliased = {}
    // recipe ids for enumeration: a moved recipe is listed once, under its creativeengineering: alias (the checks'
    // prefixes), not also under its creativedelight: id
    function recipeKeys(server) {
        return Object.keys(recipes(server)).filter(id => !aliased[id])
    }
    function recipes(server) {
        if (byId) return byId
        byId = {}
        server.getRecipeManager().getRecipes().forEach(r => { byId[String(r.getId())] = r })
        // Food recipes moved into Creative Delight's tree on 2026-10-25 (creativedelight: ids, same paths): a
        // creativeengineering: id the checks name also finds its creativedelight: twin
        Object.keys(byId).forEach(id => {
            if (id.indexOf('creativedelight:') != 0) return
            let old = 'creativeengineering:' + id.substring('creativedelight:'.length)
            if (byId[old] == null) { byId[old] = byId[id]; aliased[id] = true }
        })
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
    function has(server, id) { return recipes(server)[id] != null }
    function expectIds(server, ids) {
        ids.forEach(id => check('recipe ' + id, has(server, id), has(server, id) ? resultOf(recipes(server)[id]) : 'not loaded'))
    }
    function expectGone(server, ids) {
        ids.forEach(id => check('removed ' + id, !has(server, id), has(server, id) ? 'still loaded' : 'gone'))
    }
    // does some ingredient of recipe `id` accept `itemIdStr`
    function takes(server, id, itemIdStr) {
        let r = recipes(server)[id]
        if (!r) return false
        return ingredientsOf(r).some(g => g.test(Item.of(itemIdStr)))
    }

    let fakeN = 0
    function fake(level) {
        fakeN++
        let p = $FDFake.get(level, new $FDProfile($FDUUID.randomUUID(), 'fdprobe' + fakeN))
        p.getInventory().clearContent()
        return p
    }
    function click(p, level, at, st) {
        p.setItemInHand($FDHand.MAIN_HAND, st)
        let hit = new $FDHit(new $FDVec3(at.getX() + 0.5, at.getY() + 1, at.getZ() + 0.5), $FDDir.UP, at, false)
        return String(p.gameMode.useItemOn(p, level, p.getMainHandItem(), $FDHand.MAIN_HAND, hit))
    }
    function count(p, id) { return p.getInventory().countItem(item(id)) }
    let nextX = 40
    function place(level, id) {
        let at = new $FDBlockPos(nextX, Y, 7)
        nextX += 2
        // air first: a jar left by an earlier run keeps its block entity (and contents) otherwise
        level.setBlockAndUpdate(at, block('minecraft:air').defaultBlockState())
        level.setBlockAndUpdate(at, block(id).defaultBlockState())
        return at
    }

    // ---- fluids: the three pack fluids are registered and tagged (RC-71, RC-77, RC-78 load part)
    function fluids() {
        let tags = $FDReg.FLUIDS.tags()
        ;['chorus_juice', 'egg_solution', 'frosting'].forEach(n => {
            let f = $FDReg.FLUIDS.getValue(rl('creativeengineering:' + n))
            let key = f == null ? 'null' : String($FDReg.FLUIDS.getKey(f))
            check('fluid ' + n, key == 'creativeengineering:' + n, key)
            let tag = tags.getTag(tags.createTagKey(rl('forge:fluid/' + n)))
            check('tag forge:fluid/' + n, f != null && tag.contains(f), 'size ' + tag.size())
        })
    }

    // ---- recipes: every new id loads, the replaced ones are gone (RC-71 to RC-75, RC-77, RC-78)
    function recipeIds(server) {
        let C = 'creativeengineering:'
        expectIds(server, [C + 'farmersdelightcooking/chorus_juice_manual_only', C + 'createmixing/chorus_juice',
            C + 'createfilling/chorus_juice', C + 'createemptying/chorus_juice'])
        let bottling = recipeKeys(server).filter(id => id.indexOf(C + 'iebottling/') == 0 &&
            resultOf(recipes(server)[id]).indexOf('endersdelight:chorus_juice') == 0)
        check('IE bottling chorus juice', bottling.length == 1, bottling.join(', ') || 'none')
        expectIds(server, ['egg_solution', 'dough_egg_solution', 'cornbread_batter_egg_solution', 'pancakes_egg_solution',
            'batter_egg_solution', 'scrambled_eggs_egg_solution',
            'potato_salad_egg_solution', 'glow_berry_custard_egg_solution', 'blueberry_custard_egg_solution',
            'vanilla_pudding_egg_solution'].map(s => C + 'createmixing/' + s))
        // Brewin' dishes: Creative Delight brewinandchewin/recipes.js since 2026-10-31
        expectIds(server, ['creativedelight:createmixing/vegetable_omelet_egg_solution',
            'creativedelight:createsequencedassembly/quiche_egg_solution_sequenced_assembly'])
        expectIds(server, [C + 'createmixing/frosting', C + 'createcompacting/frosting', C + 'createfilling/cake_frosting_spout',
            C + 'shapeless/frosting_manual_only'])
        // berry matcha latte (RC-73)
        expectGone(server, ['delightful:food/berry_matcha_latte_from_matcha_latte_neapolitan'])
        expectIds(server, [C + 'farmersrespitebrewing/berry_matcha_latte', C + 'shapeless/berry_matcha_latte'])
        check('latte grid takes ice', takes(server, C + 'shapeless/berry_matcha_latte', 'minecraft:ice') &&
            takes(server, C + 'shapeless/berry_matcha_latte', 'neapolitan:ice_cubes'), 'ice and ice cubes')
        let rem = Item.of('delightful:matcha_latte').getCraftingRemainingItem()
        check('latte gives its bottle back in the grid', !rem.isEmpty() && itemId(rem) == 'minecraft:glass_bottle', String(rem))
        let others = recipeKeys(server).filter(id => resultOf(recipes(server)[id]).indexOf('delightful:berry_matcha_latte') == 0)
        check('berry latte recipes', others.length == 2, others.join(', '))
        // bone broth soups and pastas (RC-75): hand-only ids (Create skips *_manual_only)
        expectIds(server, ['acacia_blossom_soup', 'carrot_soup', 'creamy_onion_soup', 'fig_chicken_stew', 'kangaroo_stew',
            'miso_with_bamboo_sprouts', 'bell_pepper_pasta', 'cheesy_pasta', 'creamy_pasta_with_chicken_cuts',
            'creamy_pasta_with_ham', 'kangaroo_pasta', 'lobster_pasta', 'duck_noodles']
            .map(s => C + 'shapeless/' + s + '_cooked_manual_only'))
        check('kangaroo stew takes bone broth', takes(server, C + 'shapeless/kangaroo_stew_cooked_manual_only', 'farmersdelight:bone_broth'), '')
    }

    // ---- ice cubes (RC-76)
    function ice(server) {
        let C = 'creativeengineering:'
        expectGone(server, ['neapolitan:ice_cutting'])
        ;[[C + 'farmersdelightcutting/ice_cubes_hammering', 4], [C + 'shapeless/ice_cubes_hammering', 4], [C + 'iecrushing/ice_cubes', 3]].forEach(x => {
            let r = recipes(server)[x[0]]
            let res = r ? resultOf(r) : 'not loaded'
            // FD cutting and IE crushing keep their outputs outside getResultItem: accept the loaded id there
            check('ice ' + x[0], r != null && (res == '?' || res == '' || res == 'neapolitan:ice_cubes x' + x[1]), res)
        })
        check('#forge:ice_cubes holds ice', Ingredient.of('#forge:ice_cubes').test(Item.of('minecraft:ice')), '')
        ;['abnormals_delight:neapolitan/melon_popsicle', 'moredelight:chocolate_popsicle', 'fruitsdelight:kiwi_popsicle',
            'fruitsdelight:hamimelon_popsicle', 'fruitsdelight:hamimelon_shaved_ice', 'farmersdelight:cooking/bellini_cocktail'
        ].forEach(id => check('ice food ' + id, takes(server, id, 'minecraft:ice') && takes(server, id, 'neapolitan:ice_cubes'),
            has(server, id) ? 'ice ' + takes(server, id, 'minecraft:ice') + ', cubes ' + takes(server, id, 'neapolitan:ice_cubes') : 'recipe not loaded'))
        let creams = recipeKeys(server).filter(id => id.indexOf('neapolitan:') == 0 && id.indexOf('ice_cream') > 0)
        let creamsBad = creams.filter(id => takes(server, id, 'neapolitan:ice_cubes') && !takes(server, id, 'minecraft:ice'))
        check('Neapolitan ice creams take ice', creams.length > 0 && creamsBad.length == 0, creams.length + ' recipes, refuse ice: ' + creamsBad.join(', '))
        check('9 cubes -> ice keeps cubes only', has(server, 'neapolitan:ice') && !takes(server, 'neapolitan:ice', 'minecraft:ice'),
            has(server, 'neapolitan:ice') ? 'takes ice ' + takes(server, 'neapolitan:ice', 'minecraft:ice') : 'not loaded')
    }

    // ---- milk jars (RC-74, milk_jar.js)
    function milkjar(level) {
        let milk = Java.loadClass('net.blay09.mods.cookingforblockheads.compat.Compat').getMilkFluid()
        ;['cookingforblockheads:milk_jar', 'cookingforblockheads:cow_jar'].forEach(id => {
            let at = place(level, id)
            let tank = level.getBlockEntity(at).getFluidTank()
            let p = fake(level)
            click(p, level, at, Item.of('minecraft:glass_bottle'))
            check(id + ' empty jar refuses a bottle', count(p, 'farmersdelight:milk_bottle') == 0, 'milk bottles ' + count(p, 'farmersdelight:milk_bottle'))
            tank.fill(milk, 1000, false)
            click(p, level, at, Item.of('minecraft:glass_bottle', 2))
            check(id + ' bottle takes 250 mB', tank.getAmount() == 750 && count(p, 'farmersdelight:milk_bottle') == 1 &&
                count(p, 'minecraft:glass_bottle') == 1, 'tank ' + tank.getAmount() + ', milk bottles ' + count(p, 'farmersdelight:milk_bottle') +
                ', glass ' + count(p, 'minecraft:glass_bottle'))
            let q = fake(level)
            click(q, level, at, Item.of('farmersdelight:milk_bottle'))
            check(id + ' milk bottle puts 250 mB back', tank.getAmount() == 1000 && count(q, 'minecraft:glass_bottle') == 1,
                'tank ' + tank.getAmount() + ', glass ' + count(q, 'minecraft:glass_bottle'))
        })
    }

    // ---- soups in Supplementaries jars (MJ-11 to MJ-14, data/creativeengineering/moonlight/soft_fluids)
    const SOUPS = ['alexsdelight:acacia_blossom_soup', 'neapolitan:adzuki_stew', 'fruitsdelight:bayberry_soup',
        'rusticdelight:bell_pepper_soup', 'farmersrespite:blazing_chili', 'farmersdelight:bone_broth', 'moredelight:carrot_soup',
        'quarkdelight:cavern_explorer_stew', 'endersdelight:chorus_stew', 'corn_delight:corn_soup',
        'brewinandchewin:creamy_onion_soup', 'goldenfood:enchanted_golden_beetroot_soup',
        'goldenfood:enchanted_golden_mushroom_stew', 'goldenfood:enchanted_golden_rabbit_stew', 'endersdelight:endermite_stew',
        'fruitsdelight:fig_chicken_stew', 'goldenfood:golden_beetroot_soup', 'goldenfood:golden_mushroom_stew',
        'goldenfood:golden_rabbit_stew', 'alexsdelight:kangaroo_stew', 'quarkdelight:miso_with_bamboo_sprouts',
        'farmersdelight:noodle_soup', 'delightful:sinigang', 'nethersdelight:strider_moss_stew']
    function soupjar(level) {
        // Moonlight fills its item map on OnDatapackSyncEvent (a player joining, or /reload): empty on a fresh
        // dedicated server with no player, so fill it the same way first.
        let $SFReg = Java.loadClass('net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry')
        if ($SFReg.getItemsMap().isEmpty()) { $SFReg.onDataLoad(); console.info('[fdtest] INFO soupjar soft fluid map was empty, filled it') }
        let items = $SFReg.getItemsMap()
        let missing = SOUPS.filter(id => items.get(item(id)) == null)
        check('24 soups are soft fluid items', missing.length == 0, missing.length + ' missing: ' + missing.join(', '))
        let tagIds = []
        let it = Ingredient.of('#creativedelight:food_type/soup').getStacks().iterator()
        while (it.hasNext()) tagIds.push(itemId(it.next()))
        let tagMissing = tagIds.filter(id => items.get(item(id)) == null)
        check('every #food_type/soup item is a soft fluid item', tagMissing.length == 0, tagMissing.join(', ') || 'all')

        function jar() { let at = place(level, 'supplementaries:jar'); return [at, level.getBlockEntity(at).getSoftFluidTank()] }
        // MJ-11: a bowl of corn soup pours 2, bowl back; 6 fill; a 7th stays; an empty bowl takes one out
        let [a, ta] = jar()
        let p = fake(level)
        click(p, level, a, Item.of('corn_delight:corn_soup'))
        check('MJ-11 corn soup in', ta.getCount() == 2 && count(p, 'minecraft:bowl') == 1, 'count ' + ta.getCount() + ', bowls ' + count(p, 'minecraft:bowl'))
        for (let n = 0; n < 6; n++) click(p, level, a, Item.of('corn_delight:corn_soup'))
        check('MJ-11 six bowls fill, the 7th stays', ta.getCount() == 12 && count(p, 'corn_delight:corn_soup') == 1,
            'count ' + ta.getCount() + ', soup left ' + count(p, 'corn_delight:corn_soup'))
        let q = fake(level)
        click(q, level, a, Item.of('minecraft:bowl'))
        check('MJ-11 bowl takes soup out', ta.getCount() == 10 && count(q, 'corn_delight:corn_soup') == 1, 'count ' + ta.getCount())
        // MJ-12: every soup goes in and comes back as itself
        let bad = []
        SOUPS.forEach(id => {
            let [b, tb] = jar()
            let r = fake(level)
            click(r, level, b, Item.of(id))
            let cap = id == 'fruitsdelight:bayberry_soup' ? 1 : 2
            let empty = id.indexOf('endersdelight:') == 0 ? 'endersdelight:shulker_bowl' : (cap == 1 ? 'minecraft:glass_bottle' : 'minecraft:bowl')
            r.getInventory().clearContent()
            click(r, level, b, Item.of(empty))
            if (count(r, id) != 1 || !tb.isEmpty()) bad.push(id + ' (in ' + cap + ', back ' + count(r, id) + ', left ' + tb.getCount() + ')')
        })
        check('MJ-12 every soup in and out as itself', bad.length == 0, bad.join('; ') || 'all 24')
        // MJ-13: chorus stew gives the shulker bowl back; a wooden bowl can't take it out
        let [c, tc] = jar()
        let s = fake(level)
        click(s, level, c, Item.of('endersdelight:chorus_stew'))
        check('MJ-13 shulker bowl back', count(s, 'endersdelight:shulker_bowl') == 1 && tc.getCount() == 2, 'shulker bowls ' + count(s, 'endersdelight:shulker_bowl'))
        s.getInventory().clearContent()
        click(s, level, c, Item.of('minecraft:bowl'))
        check('MJ-13 wooden bowl refused', tc.getCount() == 2 && count(s, 'endersdelight:chorus_stew') == 0, 'count ' + tc.getCount())
        // MJ-14: bayberry soup bottle pours 1
        let [d, td] = jar()
        let t = fake(level)
        click(t, level, d, Item.of('fruitsdelight:bayberry_soup'))
        check('MJ-14 bottle soup pours 1', td.getCount() == 1 && count(t, 'minecraft:glass_bottle') == 1, 'count ' + td.getCount())
    }

    // ---- placed food for Spice of Life (FO-44): class, map file, every entry valid
    function sol() {
        let cls = null
        try { cls = Java.loadClass('com.SaintAlfonzo.creativedelight.food.PlacedFoodSol') } catch (e) { }
        check('PlacedFoodSol in the jar', cls != null, cls == null ? 'class not found: wrong jar' : 'loaded')
        let blocks = null
        try { blocks = JSON.parse(String(JsonIO.readString('kubejs/generation_scripts/food/placed_food_slices.json'))).blocks } catch (e) { }
        check('map file read', blocks != null, blocks == null ? 'missing' : Object.keys(blocks).length + ' entries')
        if (blocks == null) return
        let bad = []
        Object.keys(blocks).forEach(k => {
            let b = block(k), it = item(blocks[k])
            if (b == null || String($FDReg.BLOCKS.getKey(b)) != k) bad.push(k + ' (no block)')
            else if (it == null || !it.isEdible()) bad.push(k + ' -> ' + blocks[k] + ' (not food)')
        })
        check('every entry a block -> food item', bad.length == 0, bad.join(', ') || 'all')
        check('chocolate cake bite = the knife slice', blocks['createaddition:chocolate_cake'] == 'abnormals_delight:chocolate_cake_slice',
            String(blocks['createaddition:chocolate_cake']))
    }

    // ---- coffee merge (item_merges.md 2.8, MG-23, MG-26 recipe parts, MG-24 effect part)
    function coffee(server) {
        let PURGED = ['farmersrespite:coffee_berries', 'farmersrespite:coffee_beans', 'farmersrespite:coffee_beans_sack',
            'farmersrespite:wild_coffee_bush', 'farmersrespite:coffee', 'farmersrespite:long_coffee', 'farmersrespite:strong_coffee']
        let removed = global.removedItems || []
        PURGED.forEach(id => check('purged ' + id, removed.indexOf(id) >= 0, removed.indexOf(id) >= 0 ? 'in global.removedItems' : 'not listed'))
        let hits = []
        recipeKeys(server).forEach(id => {
            let r = recipes(server)[id]
            let out = resultOf(r)
            if (PURGED.some(p => out.indexOf(p + ' ') == 0)) hits.push(id + ' makes ' + out)
            ingredientsOf(r).forEach(g => PURGED.forEach(p => { if (g.test(Item.of(p))) hits.push(id + ' takes ' + p) }))
        })
        check('no recipe names a purged coffee item', hits.length == 0, hits.slice(0, 12).join('; ') || 'none')
        expectGone(server, ['farmersrespite:brewing/coffee', 'farmersrespite:brewing/long_coffee', 'farmersrespite:brewing/strong_coffee',
            'create_central_kitchen:mixing/coffee', 'create_central_kitchen:milling/coffee_berries', 'farmersrespite:cutting/coffee_berries',
            'farmersrespite:coffee_beans', 'farmersrespite:coffee_beans_sack', 'minecraft:red_dye'])
        let cake = recipeKeys(server).filter(id => resultOf(recipes(server)[id]).indexOf('farmersrespite:coffee_cake ') == 0 &&
            takes(server, id, 'rusticdelight:roasted_coffee_beans'))
        check('coffee cake takes roasted beans', cake.length > 0, cake.join(', ') || 'none')
        let eff = []
        let fp = item('rusticdelight:coffee').getFoodProperties()
        if (fp != null) fp.getEffects().forEach(pr => eff.push(String($FDReg.MOB_EFFECTS.getKey(pr.getFirst().getEffect()))))
        check('Rustic coffee gives Caffeinated, no Speed / Haste', eff.indexOf('farmersrespite:caffeinated') >= 0 &&
            eff.indexOf('minecraft:speed') < 0 && eff.indexOf('minecraft:haste') < 0, eff.join(', ') || 'no effects')
    }

    function run(src, which) {
        pass = 0; fail = 0; byId = null; aliased = {}; fakeN = 0; nextX = 40
        let server = src.getServer(), level = src.getLevel()
        let parts = which == 'all' ? ['fluids', 'recipes', 'ice', 'milkjar', 'soupjar', 'sol', 'coffee'] : [which]
        parts.forEach(w => {
            part = w
            try {
                if (w == 'fluids') fluids()
                else if (w == 'recipes') recipeIds(server)
                else if (w == 'ice') ice(server)
                else if (w == 'milkjar') milkjar(level)
                else if (w == 'soupjar') soupjar(level)
                else if (w == 'sol') sol()
                else if (w == 'coffee') coffee(server)
                else check('part', false, 'unknown part ' + w)
            } catch (e) { fail++; console.info('[fdtest] FAIL ' + w + ' ERROR ' + e) }
        })
        console.info('[fdtest] done ' + which + ': ' + pass + ' pass, ' + fail + ' fail')
    }

    ServerEvents.commandRegistry(event => {
        const { commands: Commands, arguments: Arguments } = event
        event.register(Commands.literal('fdtest')
            .executes(ctx => { run(ctx.source, 'all'); return 1 })
            .then(Commands.argument('part', Arguments.GREEDY_STRING.create(event)).executes(ctx => {
                run(ctx.source, String(Arguments.GREEDY_STRING.getResult(ctx, 'part'))); return 1
            })))
    })
})()
