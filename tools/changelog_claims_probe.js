// Changelog claims probe (scratch queue SQ-CL-01: bullets of kubejs/assets/creativeengineering/changelog/unreleased.md
// fixed in the 2026-10-22 audit). Scratch-only: copy into a scratch server's kubejs/server_scripts
// (tools/creative_labels_test/scratch_server.sh), never into the pack. Run /cltest; results are "[cltest] ..." lines
// in logs/latest.log, ending in "[cltest] done: N PASS, M FAIL".
//
//  food     Canned Dough not edible, 8 servings; Canned Shulker Flesh 2 hunger; Canned White Strawberries 7 hunger
//  recipes  every recipe making cornmeal, raw tortilla or salt from raw rock salt / water, listed as
//           "id (type) inputs -> outputs" with counts and chances, for comparing against the bullets by eye
//  hand     every recipe that uses up a pie filling can, except the oven's, is skipped by Create's automation
(() => {
    const $CLForgeReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $CLServings = Java.loadClass('com.SaintAlfonzo.creativedelight.food.CanServings')

    const i = global.item
    let pass = 0, fail = 0
    function log(s) { console.info('[cltest] ' + s) }
    function ok(cond, what) { if (cond) { pass++; log('PASS ' + what) } else { fail++; log('FAIL ' + what) } }
    function idOf(st) { return String($CLForgeReg.ITEMS.getKey(st.getItem())) }
    function stacksOf(ing) {
        // Ingredient.getItems() is hidden behind KubeJS's IngredientKJS; getStacks() is its view
        let out = [], it = ing.getStacks().iterator()
        while (it.hasNext()) out.push(it.next())
        return out
    }
    function ingDesc(ing) {
        let st = stacksOf(ing)
        return st.length ? idOf(st[0]) + (st.length > 1 ? '(+' + (st.length - 1) + ')' : '') : '-'
    }
    function inputs(r) {
        let out = []
        try { r.getIngredients().forEach(g => out.push(ingDesc(g))) } catch (e) { }
        try { r.getFluidIngredients().forEach(f => out.push('fluid ' + f)) } catch (e) { }
        return out
    }
    function outputs(r) {
        let out = []
        try {
            r.getRollableResults().forEach(o => out.push(o.getStack().getCount() + 'x ' + idOf(o.getStack()) +
                (o.getChance() < 1 ? ' @' + o.getChance() : '')))
        } catch (e) { }
        if (!out.length) try { let s = r.getResultItem(); out.push(s.getCount() + 'x ' + idOf(s)) } catch (e) { }
        try { r.getFluidResults().forEach(f => out.push('fluid ' + f)) } catch (e) { }
        return out
    }

    function food() {
        let dough = Item.of(i.canned_dough)
        ok(!dough.getItem().isEdible(), 'Canned Dough not edible')
        ok($CLServings.maxServings(dough) == 8, 'Canned Dough 8 servings (' + $CLServings.maxServings(dough) + ')')
        ;[[i.canned_shulker, 2], [i.canned_white_strawberry, 7]].forEach(row => {
            let props = Item.of(row[0]).getItem().getFoodProperties()
            let n = props == null ? -1 : props.getNutrition()
            ok(n == row[1], row[0] + ' ' + row[1] + ' hunger per serving (' + n + ')')
        })
    }

    function recipes(server) {
        const WATCH = [i.cornmeal, i.tortilla_raw]
        server.getRecipeManager().getRecipes().forEach(r => {
            let res
            try { res = r.getResultItem() } catch (e) { return }
            let outs = outputs(r), ins = inputs(r)
            let hit = WATCH.some(w => outs.some(o => o.indexOf(' ' + w) >= 0))
                || (outs.some(o => /salt/.test(o)) && ins.some(x => /raw_rock_salt|water/.test(x)))
            if (!hit) return
            log('recipe ' + r.getId() + ' (' + r.getType() + ') [' + ins.join(', ') + '] -> ' + outs.join(', '))
        })
    }

    function hand(server) {
        let $AllTypes = Java.loadClass('com.simibubi.create.AllRecipeTypes')
        server.getRecipeManager().getRecipes().forEach(r => {
            let res
            try { res = r.getResultItem() } catch (e) { return }
            // a recipe that uses up a filling can; making one from slices + an empty can stays automatable
            if (!inputs(r).some(x => /canned_[a-z_]*pie_filling/.test(x))) return
            let type = String(r.getType())
            if (type == 'creativeengineering:baking') { log('oven: ' + r.getId()); return }
            ok($AllTypes.shouldIgnoreInAutomation(r), 'hand only: ' + r.getId() + ' (' + type + ')')
        })
    }

    function run(server) {
        pass = 0; fail = 0
        try { food() } catch (e) { fail++; log('ERROR food ' + e) }
        try { recipes(server) } catch (e) { fail++; log('ERROR recipes ' + e) }
        try { hand(server) } catch (e) { fail++; log('ERROR hand ' + e) }
        log('done: ' + pass + ' PASS, ' + fail + ' FAIL')
    }

    ServerEvents.commandRegistry(event => {
        const { commands: Commands } = event
        event.register(Commands.literal('cltest').executes(ctx => {
            run(ctx.source.getServer())
            return 1
        }))
    })
})()
