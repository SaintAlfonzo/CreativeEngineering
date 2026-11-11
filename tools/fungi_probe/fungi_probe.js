// Fungi Delight port scratch-server probe (scratch queue SQ-FGD-01 to SQ-FGD-10; feature_writeup/fungi_delight.md
// FGD-, potions.md PO-27). Scratch-only: copy into a scratch server's kubejs/server_scripts as zz_fgd_probe.js
// (tools/creative_labels_test/scratch_server.sh setup, then cp), never into the pack.
//
//   /fgdtest <part>   part = recipes | tags | compost | mobs | brewing | blocks | cows | breed | stomach | all
// Results are "[fgdtest] PASS|FAIL <part> <name>: <detail>" lines in logs/latest.log, then
// "[fgdtest] done <part>: N pass, M fail". Same style as tools/food_drinks_probe (/fdtest).
//
// Not covered here: pig truffle digging (SQ-FGD-07, real time, commands), trades (SQ-FGD-11, real villagers),
// worldgen counts (SQ-FGD-12 to 15, offline from region files).
(() => {
    const $GFake = Java.loadClass('net.minecraftforge.common.util.FakePlayerFactory')
    const $GProfile = Java.loadClass('com.mojang.authlib.GameProfile')
    const $GUUID = Java.loadClass('java.util.UUID')
    const $GBlockPos = Java.loadClass('net.minecraft.core.BlockPos')
    const $GHand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $GReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $GRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $GComposter = Java.loadClass('net.minecraft.world.level.block.ComposterBlock')
    const $GPotionUtils = Java.loadClass('net.minecraft.world.item.alchemy.PotionUtils')
    const $GBrewing = Java.loadClass('net.minecraft.world.item.alchemy.PotionBrewing')
    const $GBrewReg = Java.loadClass('net.minecraftforge.common.brewing.BrewingRecipeRegistry')
    const $GBoneMeal = Java.loadClass('net.minecraft.world.item.BoneMealItem')
    const $GEvents = Java.loadClass('net.minecraftforge.event.ForgeEventFactory')
    const $GSpawn = Java.loadClass('net.minecraft.world.entity.MobSpawnType')

    const Y = -60
    let pass = 0, fail = 0, part = ''
    function check(name, ok, detail) {
        if (ok) pass++; else fail++
        console.info('[fgdtest] ' + (ok ? 'PASS' : 'FAIL') + ' ' + part + ' ' + name + ': ' + detail)
    }
    function note(name, detail) { console.info('[fgdtest] NOTE ' + part + ' ' + name + ': ' + detail) }
    function rl(id) { return new $GRL(id) }
    function item(id) { return $GReg.ITEMS.getValue(rl(id)) }
    function itemId(st) { return String($GReg.ITEMS.getKey(st.getItem())) }
    function block(id) { return $GReg.BLOCKS.getValue(rl(id)) }
    function blockId(state) { return String($GReg.BLOCKS.getKey(state.getBlock())) }
    // Grid and pot meals moved into Creative Delight's tree (creativedelight: ids) on 2026-10-25; the oven pie is still C
    const FG = 'fungidelight:', C = 'creativeengineering:', CDN = 'creativedelight:'

    let byId = null
    function recipes(server) {
        if (byId) return byId
        byId = {}
        server.getRecipeManager().getRecipes().forEach(r => { byId[String(r.getId())] = r })
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
    function takes(server, id, itemIdStr) {
        let r = recipes(server)[id]
        if (!r) return false
        return ingredientsOf(r).some(g => g.test(Item.of(itemIdStr)))
    }
    function idsMaking(server, prefix, outId) {
        return Object.keys(recipes(server)).filter(id => id.indexOf(prefix) == 0 && resultOf(recipes(server)[id]).indexOf(outId + ' ') == 0)
    }

    let fakeN = 0
    function fake(level) {
        fakeN++
        let p = $GFake.get(level, new $GProfile($GUUID.randomUUID(), 'fgdprobe' + fakeN))
        p.getInventory().clearContent()
        p.removeAllEffects()
        return p
    }
    function at(x, y, z) { return new $GBlockPos(x, y, z) }
    function set(level, pos, id) { level.setBlockAndUpdate(pos, block(id).defaultBlockState()) }
    function spawn(level, id, x, z) {
        let e = $GReg.ENTITY_TYPES.getValue(rl(id)).create(level)
        e.setPos(x + 0.5, Y, z + 0.5)
        try { e.finalizeSpawn(level, level.getCurrentDifficultyAt(e.blockPosition()), $GSpawn.COMMAND, null, null) } catch (err) { }
        level.addFreshEntity(e)
        return e
    }

    // ---- recipes (FGD-20, FGD-22, FGD-25, FGD-34, FGD-38 recipe level)
    const GRID = ['inky_caps_salad', 'inky_cap_scrambled_eggs', 'truffle_ice_cream']
    const POT = ['creamy_morel_soup', 'mutton_chops_with_truffle', 'pork_marsala_with_mushrooms', 'rabbit_with_morels',
        'steak_with_mushrooms', 'stuffed_morels', 'truffle_pasta']
    function recipeIds(server) {
        expectGone(server, [FG + 'inky_cap_salad', FG + 'inky_cap_scrambled_eggs', FG + 'truffle_ice_cream'])
        expectIds(server, GRID.map(n => CDN + 'shapeless/' + n + '_manual_only'))
        expectIds(server, POT.map(n => CDN + 'shapeless/' + n + '_cooked_manual_only'))
        expectIds(server, POT.map(n => FG + 'cooking/' + n))
        GRID.concat(POT).forEach(n => {
            let bad = idsMaking(server, C + 'createmixing/', FG + n).concat(idsMaking(server, C + 'createcrafting/', FG + n))
            check('no unheated mixer/crafter route ' + n, bad.length == 0, bad.join(', ') || 'none')
        })
        check('salad takes cleaned cap', takes(server, CDN + 'shapeless/inky_caps_salad_manual_only', FG + 'cleaned_cap'), 'cleaned cap')
        check('scrambled eggs take cooked cap', takes(server, CDN + 'shapeless/inky_cap_scrambled_eggs_manual_only', FG + 'cooked_cleaned_cap'), 'cooked cap')
        // effect stews: Luck / Comfort / Haste, 160 ticks
        const STEW = { inky_cap_mushroom: 'minecraft:luck', morel_mushroom: 'farmersdelight:comfort', truffle: 'minecraft:haste' }
        Object.keys(STEW).forEach(n => {
            let id = FG + 'suspicious_stew_from_' + n, r = recipes(server)[id]
            if (!r) { check('effect stew ' + n, false, 'not loaded'); return }
            let out = null
            try { out = r.assemble(null) } catch (e) { out = r.getResultItem() }
            let nbt = String(out.nbt)
            let effId = $GReg.MOB_EFFECTS.getValue(rl(STEW[n]))
            let numId = Java.loadClass('net.minecraft.world.effect.MobEffect').getId(effId)
            check('effect stew ' + n, nbt.indexOf('EffectId:' + numId + ',') >= 0 && nbt.indexOf('EffectDuration:160') >= 0,
                STEW[n] + ' (' + numId + ') -> ' + nbt)
        })
        let pie = [CDN + 'baking/truffle_pie', C + 'baking/truffle_pie'].filter(id => has(server, id))
        note('ids with truffle_pie', Object.keys(recipes(server)).filter(id => id.indexOf('truffle_pie') >= 0).join(', ') || 'none')
        // TODO fungiport #3: the pack registers CCK's truffle_pie item (2026-10-31), so the oven recipe must load
        check('oven truffle pie', pie.length >= 1 && takes(server, pie[0], FG + 'truffle'), pie.join(', ') || 'none')
        if (pie.length) {
            let ins = ingredientsOf(recipes(server)[pie[0]]).map(g => { let s = g.getStacks(); if (s.toList) s = s.toList(); return s.size() ? itemId(s.get(0)) + 'x' + s.get(0).getCount() : '-' })
            note('truffle pie inputs', ins.join(', '))
        }
        expectIds(server, ['cleaned_cap', 'inky_cap_mushroom_colony', 'morel_mushroom_colony', 'truffle_slice'].map(n => FG + 'cutting/' + n))
        expectIds(server, ['brown_mushroom', 'red_mushroom', 'inky_cap_mushroom', 'morel_mushroom', 'truffle'].map(n => FG + n + '_crate'))
        expectIds(server, [FG + 'cooked_cleaned_cap_from_smelting', FG + 'cooked_cleaned_cap_from_smoking', FG + 'cooked_cleaned_cap_from_campfire_cooking'])
        let mill = Object.keys(recipes(server)).filter(id => id.indexOf(FG) == 0 && id.indexOf('miling') >= 0)
        check('Create milling', mill.length == 2, mill.join(', ') || 'none')
    }

    // ---- tags
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
    function tags() {
        const I = $GReg.ITEMS, B = $GReg.BLOCKS
        let want = {
            'forge:mushrooms/inky_cap': ['inky_cap_mushroom', 'cleaned_cap', 'cooked_cleaned_cap', 'inky_cap_mushroom_colony'],
            'forge:mushrooms/morel': ['morel_mushroom', 'morel_mushroom_colony'],
            'forge:mushrooms/truffle': ['truffle', 'truffle_slice'],
        }
        Object.keys(want).forEach(t => {
            let got = tagList(I, t), exp = want[t].map(n => FG + n).sort()
            check('#' + t, sameSet(got, exp), got.join(', '))
            exp.forEach(x => check('#forge:mushrooms holds ' + x, tagHas(I, 'forge:mushrooms', x), ''))
        })
        let mush = tagList(I, 'forge:mushrooms').filter(x => x.indexOf('crate') >= 0 || x.indexOf(FG) == 0 && POT.concat(GRID).some(n => x == FG + n))
        check('#forge:mushrooms has no dish or crate', mush.length == 0, mush.join(', ') || 'none')
        ;['brown_mushroom', 'red_mushroom', 'inky_cap', 'morel', 'truffle'].forEach(n => {
            let got = tagList(I, 'forge:storage_blocks/' + n)
            check('#forge:storage_blocks/' + n, got.some(x => x.indexOf(FG) == 0), got.join(', '))
        })
        let alc = tagList(I, 'forge:alcohol').filter(x => x.indexOf(FG) == 0)
        check('#forge:alcohol has no Fungi ids', alc.length == 0, alc.join(', ') || 'none')
        check('truffle in #minecraft:piglin_food', tagHas(I, 'minecraft:piglin_food', FG + 'truffle'), '')
        check('truffle dirt in #minecraft:dirt', tagHas(B, 'minecraft:dirt', FG + 'truffle_dirt'), '')
        check('truffle dirt shovel mineable', tagHas(B, 'minecraft:mineable/shovel', FG + 'truffle_dirt'), '')
        // colonies: FD's own colonies are in neither Serene Seasons tag, so the port leaves the Fungi ones out too (2026-10-25 run)
        ;['inky_cap_mushroom', 'morel_mushroom'].forEach(n => {
            check('year-round block ' + n, tagHas(B, 'sereneseasons:year_round_crops', FG + n), '')
            check('unbreakable infertile ' + n, tagHas(B, 'sereneseasons:unbreakable_infertile_crops', FG + n), '')
        })
    }

    // ---- compost (FGD-24)
    function compost() {
        const WANT = {
            inky_cap_mushroom: 0.65, morel_mushroom: 0.65, truffle: 1.0, inky_cap_mushroom_colony: 1.0, morel_mushroom_colony: 1.0,
            brown_mushroom_crate: 1.0, red_mushroom_crate: 1.0, inky_cap_mushroom_crate: 1.0, morel_mushroom_crate: 1.0, truffle_crate: 1.0,
            inky_cap_mushroom_block: 0.85, inky_cap_mushroom_edge: 0.85, morel_mushroom_block: 0.85,
            inky_cap_mushroom_stem: 0.65, morel_mushroom_stem: 0.65, inky_goo_veil: 0.65,
            cooked_cleaned_cap: 0.65, cleaned_cap: 0.5, truffle_slice: 0.5,
        }
        Object.keys(WANT).forEach(n => {
            let v = $GComposter.COMPOSTABLES.getOrDefault(item(FG + n), -1)
            check('compost ' + n, Math.abs(v - WANT[n]) < 0.001, v + ' (want ' + WANT[n] + ')')
        })
    }

    // ---- mobs (FGD-11, FGD-12 logic)
    function mobs(level) {
        let pig = spawn(level, 'minecraft:pig', 60, 0)
        ;['minecraft:brown_mushroom', 'minecraft:red_mushroom', FG + 'inky_cap_mushroom', FG + 'morel_mushroom', FG + 'truffle',
            'minecraft:carrot'].forEach(id => check('pig food ' + id, pig.isFood(Item.of(id)), ''))
        let pigVeg = []
        ;['vintagedelight:oat', 'vintagedelight:peanut', 'vintagedelight:cucumber', 'veggiesdelight:zucchini', 'veggiesdelight:sweet_potato', 'veggiesdelight:turnip']
            .forEach(id => { if (item(id) != null && item(id) != item('minecraft:air')) pigVeg.push(id + '=' + pig.isFood(Item.of(id))) })
        note('pig food Veggies / Vintage', pigVeg.join(', ') || 'no ids found')
        let pigGoals = []
        pig.goalSelector.getAvailableGoals().forEach(w => pigGoals.push(String(w.getGoal().getClass().getSimpleName())))
        check('pig has TruffleDiggingGoal', pigGoals.indexOf('TruffleDiggingGoal') >= 0, pigGoals.join(', '))
        let hog = spawn(level, 'minecraft:hoglin', 63, 0)
        check('hoglin isFood truffle', hog.isFood(Item.of(FG + 'truffle')), '')
        let wolf = spawn(level, 'minecraft:wolf', 66, 0)
        wolf.tame(fake(level))
        let goals = []
        wolf.goalSelector.getAvailableGoals().forEach(w => goals.push(String(w.getGoal().getClass().getSimpleName())))
        check('tamed wolf has DogTruffleHuntGoal', goals.indexOf('DogTruffleHuntGoal') >= 0, goals.join(', '))
        ;[pig, hog, wolf].forEach(e => e.discard())
    }

    // ---- brewing (FGD-18, PO-27 recipe level)
    function potion(id) {
        return $GPotionUtils.setPotion(Item.of('minecraft:potion'), $GReg.POTIONS.getValue(rl(id)))
    }
    function potionId(st) { return st.isEmpty() ? 'empty' : itemId(st) + '/' + String($GReg.POTIONS.getKey($GPotionUtils.getPotion(st))) }
    function brewing() {
        const steps = [['minecraft:awkward', FG + 'truffle', 'minecraft:potion/' + FG + 'burrowing'],
            [FG + 'burrowing', 'minecraft:redstone', 'minecraft:potion/' + FG + 'long_burrowing'],
            [FG + 'burrowing', 'minecraft:glowstone_dust', 'minecraft:potion/' + FG + 'strong_burrowing']]
        steps.forEach(s => {
            let p = potion(s[0]), ing = Item.of(s[1])
            check('vanilla mix ' + s[0] + ' + ' + s[1], $GBrewing.hasMix(p, ing) && potionId($GBrewing.mix(ing, p)) == s[2],
                potionId($GBrewing.mix(ing, p)))
            let out = $GBrewReg.getOutput(p, ing)
            check('brewing stand route ' + s[0] + ' + ' + s[1], potionId(out) == s[2], potionId(out))
        })
        let extra = []
        $GBrewReg.getRecipes().forEach(r => { let n = String(r.getClass().getName()); if (n.toLowerCase().indexOf('fungi') >= 0) extra.push(n) })
        check('no Fungi BrewingRecipeRegistry entry', extra.length == 0, extra.join(', ') || 'none')
        let found = 'class not found'
        ;['com.simibubi.create.content.contraptions.fluids.potion.PotionMixingRecipes',
            'com.simibubi.create.content.fluids.potion.PotionMixingRecipes'].forEach(cn => {
            if (found != 'class not found') return
            try {
                let cls = Java.loadClass(cn), n = 0
                cls.ALL.forEach(r => { if (String(r.getId()).indexOf('burrowing') >= 0 || resultOf(r).indexOf('burrowing') >= 0) n++; try { r.getFluidResults().forEach(f => { if (String(f.getOrCreateTag()).indexOf('burrowing') >= 0) n++ }) } catch (e) { } })
                found = cn.split('.').pop() + ' burrowing entries ' + n
            } catch (e) { }
        })
        check('Create potion mixing lists burrowing', found.indexOf('entries ') >= 0 && !found.endsWith(' 0'), found)
    }

    // ---- blocks (FGD-04, FGD-05, FGD-35, FGD-31 block half)
    // a closed dark stone room at x 100.., one cell per check
    function darkCell(level, x0, floor) {
        for (let x = x0 - 5; x <= x0 + 5; x++) for (let z = -5; z <= 5; z++) for (let y = Y - 1; y <= Y + 15; y++) {
            let edge = x == x0 - 5 || x == x0 + 5 || z == -5 || z == 5 || y == Y - 1 || y == Y + 15
            set(level, at(x, y, z), edge ? 'minecraft:stone' : 'minecraft:air')
        }
        set(level, at(x0, Y - 1, 0), floor)
        return at(x0, Y, 0)
    }
    function boneMeal(level, pos, n) {
        let p = fake(level)
        for (let k = 0; k < n; k++) {
            let st = Item.of('minecraft:bone_meal', 64)
            $GBoneMeal.applyBonemeal(st, level, pos, p)
        }
    }
    function blocks(level) {
        let x = 100
        ;[['inky_cap_mushroom', 'minecraft:dirt'], ['morel_mushroom', 'minecraft:dirt']].forEach(c => {
            let pos = darkCell(level, x, c[1]); x += 12
            set(level, pos, FG + c[0])
            check(c[0] + ' survives in dark room', blockId(level.getBlockState(pos)) == FG + c[0], 'light ' + level.getMaxLocalRawBrightness(pos))
            boneMeal(level, pos, 30)
            let up = level.getBlockState(pos.above()), here = level.getBlockState(pos)
            check('bone meal grows huge ' + c[0], blockId(here) != FG + c[0] && blockId(here) != 'minecraft:air' || blockId(up) != 'minecraft:air',
                'base ' + blockId(here) + ', above ' + blockId(up))
        })
        ;[['inky_cap_mushroom', 'farmersdelight:rich_soil'], ['morel_mushroom', 'farmersdelight:rich_soil']].forEach(c => {
            let pos = darkCell(level, x, c[1]); x += 12
            set(level, pos, FG + c[0])
            boneMeal(level, pos, 30)
            let here = blockId(level.getBlockState(pos)), up = blockId(level.getBlockState(pos.above()))
            check('bone meal refused on rich soil ' + c[0], (here == FG + c[0] || here == FG + c[0] + '_colony') && up == 'minecraft:air',
                'base ' + here + ', above ' + up)
            set(level, pos, FG + c[0])
            let st = level.getBlockState(pos), k = 0
            for (; k < 2000 && blockId(level.getBlockState(pos)) == FG + c[0]; k++) st.randomTick(level, pos, level.random)
            check('random ticks make a colony on rich soil ' + c[0], blockId(level.getBlockState(pos)) == FG + c[0] + '_colony',
                blockId(level.getBlockState(pos)) + ' after ' + k + ' ticks')
        })
        // veil under a 20-block overhang at x 140
        let vx = 160, top = Y + 21
        for (let y = Y; y <= top; y++) set(level, at(vx, y, 0), 'minecraft:air')
        set(level, at(vx, top, 0), 'minecraft:stone')
        set(level, at(vx, top - 1, 0), FG + 'inky_goo_veil')
        function chain() { let n = 0; for (let y = top - 1; y >= Y; y--) { if (blockId(level.getBlockState(at(vx, y, 0))) == FG + 'inky_goo_veil') n++; else break } return n }
        let p = fake(level)
        for (let k = 0; k < 30; k++) $GBoneMeal.applyBonemeal(Item.of('minecraft:bone_meal', 64), level, at(vx, top - 1, 0), p)
        check('veil bone meal stops at 16', chain() == 16, 'chain ' + chain())
        let st = Item.of('minecraft:bone_meal', 64)
        let ok = $GBoneMeal.applyBonemeal(st, level, at(vx, top - 1, 0), p)
        check('veil refuses bone meal at 16', !ok && st.getCount() == 64, 'applied ' + ok + ', count ' + st.getCount())
        for (let y = Y; y < top - 1; y++) set(level, at(vx, y, 0), 'minecraft:air')
        let vs = level.getBlockState(at(vx, top - 1, 0))
        for (let k = 0; k < 2000; k++) { let b = top - chain(); level.getBlockState(at(vx, b, 0)).randomTick(level, at(vx, b, 0), level.random) }
        check('veil random ticks stop at 16', chain() == 16, 'chain ' + chain() + ' after 2000 ticks')
    }

    // ---- cows (FGD-27): lightning
    function cows(level) {
        const $Bolt = $GReg.ENTITY_TYPES.getValue(rl('minecraft:lightning_bolt'))
        let x = 70
        function strike(e) { let b = $Bolt.create(level); b.setPos(e.getX(), e.getY(), e.getZ()); e.thunderHit(level, b); return b }
        ;['inky_cap_cow', 'morel_cow'].forEach(n => {
            let c = spawn(level, FG + n, x, 4); x += 3
            let before = String(c.getType().toString()), nb = String(c.saveWithoutId(new (Java.loadClass('net.minecraft.nbt.CompoundTag'))()).get('Type'))
            strike(c)
            let after = c.isRemoved() ? 'removed' : String(c.getType().toString()), na = String(c.saveWithoutId(new (Java.loadClass('net.minecraft.nbt.CompoundTag'))()).get('Type'))
            check('lightning leaves ' + n, !c.isRemoved() && before == after && nb == na, before + ' ' + nb + ' -> ' + after + ' ' + na)
            c.discard()
        })
        ;['red', 'brown'].forEach(t => {
            let c = spawn(level, 'minecraft:mooshroom', x, 4); x += 3
            c.readAdditionalSaveData(NBT.toTagCompound({ Type: t }))
            strike(c)
            let got = String(c.saveWithoutId(new (Java.loadClass('net.minecraft.nbt.CompoundTag'))()).getString('Type'))
            check('lightning flips ' + t + ' mooshroom', got == (t == 'red' ? 'brown' : 'red'), t + ' -> ' + got)
            c.discard()
        })
    }

    // ---- breed (FGD-30 logic): getBreedOffspring through the vanilla path, 40 per pairing
    function kindOf(e) {
        // KubeJS turns Entity.getType() into a string id, so a registry getKey on it answers the default (minecraft:pig)
        let id = String(e.getEncodeId())
        if (id != 'minecraft:mooshroom') return id.replace(FG, '')
        return String(e.saveWithoutId(new (Java.loadClass('net.minecraft.nbt.CompoundTag'))()).getString('Type'))
    }
    function breed(level) {
        function make(k, x) {
            if (k == 'red' || k == 'brown') { let c = spawn(level, 'minecraft:mooshroom', x, 8); c.readAdditionalSaveData(NBT.toTagCompound({ Type: k })); return c }
            return spawn(level, FG + k, x, 8)
        }
        ;[['red', 'brown'], ['red', 'inky_cap_cow'], ['inky_cap_cow', 'morel_cow'], ['red', 'red'], ['inky_cap_cow', 'inky_cap_cow']].forEach(pr => {
            let a = make(pr[0], 80), b = make(pr[1], 82), tally = {}
            for (let k = 0; k < 40; k++) {
                let calf = (k % 2 == 0 ? a : b).getBreedOffspring(level, k % 2 == 0 ? b : a)
                let kind = calf == null ? 'none' : kindOf(calf)
                tally[kind] = (tally[kind] || 0) + 1
                if (calf != null) calf.discard()
            }
            let txt = Object.keys(tally).map(k => k + '=' + tally[k]).join(', ')
            let ok = pr[0] == pr[1] ? (tally[pr[0]] || 0) >= 36 : (tally[pr[0]] || 0) >= 8 && (tally[pr[1]] || 0) >= 8 && !tally.none
            check('calves ' + pr[0] + ' x ' + pr[1], ok, txt)
            a.discard(); b.discard()
        })
        let moo = make('red', 90), mb = null
        try { mb = spawn(level, 'more_mooblooms:moobloom', 92, 8) } catch (e) { }
        if (mb == null) { check('moobloom x mooshroom', false, 'moobloom entity id not found'); return }
        moo.setInLove(null); mb.setInLove(null)
        check('moobloom x mooshroom cannot mate', !moo.canMate(mb) && !mb.canMate(moo), 'moo->mb ' + moo.canMate(mb) + ', mb->moo ' + mb.canMate(moo))
        moo.discard(); mb.discard()
    }

    // ---- stomach (FGD-16, FGD-17, FGD-36): finishUsingItem + onItemUseFinish, as tools/kitchen_probe/serve_items_probe.js
    function eat(level, p, id) {
        let st = Item.of(id)
        p.setItemInHand($GHand.MAIN_HAND, st)
        let copy = st.copy()
        let res = st.finishUsingItem(level, p)
        $GEvents.onItemUseFinish(p, copy, 0, res)
    }
    function effects(p) {
        let out = []
        p.getActiveEffects().forEach(e => out.push(String($GReg.MOB_EFFECTS.getKey(e.getEffect())) + '/' + e.getAmplifier() + '/' + e.getDuration()))
        return out
    }
    function hasEff(p, id, amp) { return effects(p).some(s => s.indexOf(id + '/' + (amp == null ? '' : amp)) == 0) }
    function stomach(level, server) {
        let alcTag = 'creativedelight:food_type/drink/alcohol'
        let alc = tagList($GReg.ITEMS, alcTag)
        note('alcohol tag size', alc.length + ' first ' + alc.slice(0, 3).join(', '))
        let p = fake(level)
        eat(level, p, FG + 'cleaned_cap')
        check('cleaned cap gives Weak Stomach', hasEff(p, FG + 'weak_stomach'), effects(p).join(', ') || 'none')
        if (alc.length) {
            eat(level, p, alc[0])
            let e = effects(p)
            check('alcohol with Weak Stomach', hasEff(p, 'minecraft:nausea', 1) && hasEff(p, 'minecraft:poison', 1) && hasEff(p, 'minecraft:weakness', 1),
                alc[0] + ': ' + e.join(', '))
        }
        p = fake(level)
        eat(level, p, FG + 'cleaned_cap')
        p.setItemInHand($GHand.MAIN_HAND, potion('minecraft:water'))
        eat2(level, p, potion('minecraft:water'))
        check('water bottle with Weak Stomach: no sickness', !hasEff(p, 'minecraft:nausea') && !hasEff(p, 'minecraft:poison'), effects(p).join(', '))
        eat2(level, p, potion('minecraft:swiftness'))
        check('Swiftness with Weak Stomach', hasEff(p, 'minecraft:nausea', 1) && hasEff(p, 'minecraft:poison', 1) && hasEff(p, 'minecraft:weakness', 1),
            effects(p).join(', '))
        p = fake(level)
        let durian = tagList($GReg.ITEMS, 'fungidelight:alcohol_reaction').filter(id => id.indexOf('durian') >= 0 && item(id).isEdible())
        note('edible durian in alcohol_reaction', durian.join(', ') || 'none')
        if (durian.length) {
            eat(level, p, durian[0])
            check('durian gives Weak Stomach', hasEff(p, FG + 'weak_stomach'), durian[0] + ': ' + (effects(p).join(', ') || 'none'))
        } else check('durian gives Weak Stomach', false, 'no durian item id found')
    }
    function eat2(level, p, st) {
        p.setItemInHand($GHand.MAIN_HAND, st)
        let copy = st.copy()
        let res = st.finishUsingItem(level, p)
        $GEvents.onItemUseFinish(p, copy, 0, res)
    }

    function run(src, which) {
        pass = 0; fail = 0; byId = null; fakeN = 0
        let server = src.getServer(), level = src.getLevel()
        let parts = which == 'all' ? ['recipes', 'tags', 'compost', 'mobs', 'brewing', 'blocks', 'cows', 'breed', 'stomach'] : [which]
        parts.forEach(w => {
            part = w
            try {
                if (w == 'recipes') recipeIds(server)
                else if (w == 'tags') tags()
                else if (w == 'compost') compost()
                else if (w == 'mobs') mobs(level)
                else if (w == 'brewing') brewing()
                else if (w == 'blocks') blocks(level)
                else if (w == 'cows') cows(level)
                else if (w == 'breed') breed(level)
                else if (w == 'stomach') stomach(level, server)
                else check('part', false, 'unknown part ' + w)
            } catch (e) { fail++; console.info('[fgdtest] FAIL ' + w + ' ERROR ' + e) }
        })
        console.info('[fgdtest] done ' + which + ': ' + pass + ' pass, ' + fail + ' fail')
    }

    ServerEvents.commandRegistry(event => {
        const { commands: Commands, arguments: Arguments } = event
        event.register(Commands.literal('fgdtest')
            .executes(ctx => { run(ctx.source, 'all'); return 1 })
            .then(Commands.argument('part', Arguments.GREEDY_STRING.create(event)).executes(ctx => {
                run(ctx.source, String(Arguments.GREEDY_STRING.getResult(ctx, 'part'))); return 1
            })))
    })
})()
