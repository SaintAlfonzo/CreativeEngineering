// Scratch queue SQ-01 probe: Sweet Berry Pie and Canned Sweet Berry Pie Filling (oven.md OV-32, can_parity.md CP-26).
// Scratch-only: copy into a scratch server's kubejs/server_scripts (tools/creative_labels_test/scratch_server.sh),
// never into the pack. Run /sbptest; results are "[sbptest] PASS|FAIL ..." lines in logs/latest.log, then a
// "[sbptest] done N pass, M fail" line. Names are client lang and are not checked here (see the queue entry).
(() => {
    const $SBFakeFactory = Java.loadClass('net.minecraftforge.common.util.FakePlayerFactory')
    const $SBGameProfile = Java.loadClass('com.mojang.authlib.GameProfile')
    const $SBUUID = Java.loadClass('java.util.UUID')
    const $SBCrafting = Java.loadClass('net.minecraft.world.inventory.CraftingContainer')
    const $SBRecipeType = Java.loadClass('net.minecraft.world.item.crafting.RecipeType')
    const $SBForgeReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $SBServings = Java.loadClass('com.SaintAlfonzo.creativedelight.food.CanServings')

    const CAN = 'creativedelight:canned_sweet_berry_pie_filling'
    const PIE = 'farmersdelight:sweet_berry_cheesecake'
    const SLICE = 'farmersdelight:sweet_berry_cheesecake_slice'
    const CRUST = 'farmersdelight:pie_crust'
    const EMPTY = 'canned_goods:empty_can'

    let pass = 0, fail = 0
    function check(name, ok, detail) {
        if (ok) pass++; else fail++
        console.info('[sbptest] ' + (ok ? 'PASS' : 'FAIL') + ' ' + name + ': ' + detail)
    }
    function idOf(st) { return String($SBForgeReg.ITEMS.getKey(st.getItem())) }
    function ingIds(r) {
        let out = []
        r.getIngredients().forEach(ing => {
            // Ingredient.getItems() is hidden behind KubeJS's IngredientKJS; getStacks() is its view
            let items = [], it = ing.getStacks().iterator()
            while (it.hasNext()) items.push(it.next())
            out.push(items.length ? idOf(items[0]) + (items.length > 1 ? '+' + (items.length - 1) : '') : '-')
        })
        return out
    }

    // stacks into a 3x3 grid of a fake player's menu (the NCH can guard wants a player grid), then assemble
    function craft(level, p, stacks) {
        let cc = new $SBCrafting(p.inventoryMenu, 3, 3)
        stacks.forEach((st, n) => cc.setItem(n, st))
        let opt = level.getServer().getRecipeManager().getRecipeFor($SBRecipeType.CRAFTING, cc, level)
        if (!opt.isPresent()) return { id: 'none', out: null }
        let r = opt.get()
        return { id: String(r.getId()), out: r.assemble(cc) }
    }

    function run(level) {
        let p = $SBFakeFactory.get(level, new $SBGameProfile($SBUUID.randomUUID(), 'sbp'))
        let can = Item.of(CAN)
        check('can registered', !can.isEmpty(), CAN)
        if (can.isEmpty()) return

        let food = can.getItem().getFoodProperties()
        let fx = []
        if (food != null) food.getEffects().forEach(pr => {
            let e = pr.getFirst()
            fx.push(String($SBForgeReg.MOB_EFFECTS.getKey(e.getEffect())) + ' ' + e.getDuration())
        })
        check('can food', food != null && food.getNutrition() == 3 && Math.abs(food.getSaturationModifier() - 0.3) < 0.001,
            food == null ? 'no food' : food.getNutrition() + ' hunger, ' + food.getSaturationModifier() + ' modifier')
        check('can effect', fx.length == 1 && fx[0] == 'minecraft:speed 600', fx.join(', ') || 'none')
        check('can servings', $SBServings.maxServings(can) == 4, 'max ' + $SBServings.maxServings(can))

        let r1 = craft(level, p, [Item.of(SLICE), Item.of(SLICE), Item.of(SLICE), Item.of(SLICE), Item.of(EMPTY)])
        check('4 slices + empty can', r1.out != null && idOf(r1.out) == CAN && r1.out.getCount() == 1,
            r1.id + ' -> ' + (r1.out == null ? 'nothing' : r1.out.getCount() + 'x ' + idOf(r1.out)))

        let r2 = craft(level, p, [Item.of(CAN), Item.of(CRUST)])
        check('full can + crust by hand', r2.out != null && idOf(r2.out) == PIE,
            r2.id + ' -> ' + (r2.out == null ? 'nothing' : r2.out.getCount() + 'x ' + idOf(r2.out)))

        let opened = $SBServings.withServings(Item.of(CAN), 2)
        let r3 = craft(level, p, [opened, Item.of(CRUST)])
        check('opened can + crust by hand gives nothing', r3.out == null || r3.out.isEmpty(),
            r3.id + ' -> ' + (r3.out == null || r3.out.isEmpty() ? 'nothing' : idOf(r3.out)))

        // pot filling and the two oven pies
        let pot = [], oven = []
        level.getServer().getRecipeManager().getRecipes().forEach(r => {
            let res
            try { res = r.getResultItem() } catch (e) { return }
            if (res == null || res.isEmpty()) return
            if (idOf(res) != CAN && idOf(res) != PIE) return  // r.getType() can throw an uncatchable AbstractMethodError (Create Mechanical Extruder)
            let type = String(r.getType())
            let line = r.getId() + ' [' + ingIds(r).join(', ') + ']'
            if (idOf(res) == CAN && type == 'farmersdelight:cooking') pot.push(line)
            if (idOf(res) == PIE && type == 'creativedelight:baking') oven.push(line)
        })
        check('pot filling', pot.length == 1 && pot[0].indexOf('minecraft:sweet_berries') >= 0, pot.join(' | ') || 'none')
        check('oven pies', oven.length == 2 && oven.some(l => l.indexOf(CAN) >= 0), oven.join(' | ') || 'none')

        // the pack's only sweet berry pie: no other registered item id names one
        let others = []
        $SBForgeReg.ITEMS.getKeys().forEach(k => {
            let s = String(k)
            if (/sweet_?berr(y|ies)_(pie|tart|cheesecake)/.test(s) && s != PIE && s != SLICE && s != CAN &&
                s.indexOf('displaydelight:') != 0 && s != 'create_central_kitchen:incomplete_sweet_berry_cheesecake') others.push(s)
        })
        check('no other sweet berry pie', others.length == 0, others.join(', ') || 'none')
    }

    ServerEvents.commandRegistry(event => {
        const { commands: Commands } = event
        event.register(Commands.literal('sbptest').executes(ctx => {
            pass = 0; fail = 0
            try { run(ctx.source.getLevel()) } catch (e) { check('run', false, 'ERROR ' + e) }
            console.info('[sbptest] done ' + pass + ' pass, ' + fail + ' fail')
            return 1
        }))
    })
})()
