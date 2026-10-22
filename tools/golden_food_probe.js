// Gold foods accept rose gold probe (scratch queue SQ-GF-01, feature_writeup/recipes_cooking.md F5, RC-70).
// Scratch-only: copy into a scratch server's kubejs/server_scripts (tools/creative_labels_test/scratch_server.sh),
// never into the pack. Run /gftest; results are "[gftest] ..." lines in logs/latest.log, ending in
// "[gftest] done: N PASS, M FAIL".
//
//  tags     the three forge gold tags hold the pack's rose gold nugget, MCA ingot and MCA block
//  recipes  every loaded recipe making a golden carrot, golden apple or enchanted golden apple: each ingredient
//           (shaped / mixing ingredients, IE arc furnace additives, Create sequenced assembly steps) that accepts
//           the vanilla gold item also accepts its rose gold twin, and the expected recipes are present
(() => {
    const $GFForgeReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')

    const i = global.item
    let pass = 0, fail = 0
    function log(s) { console.info('[gftest] ' + s) }
    function ok(cond, what) { if (cond) { pass++; log('PASS ' + what) } else { fail++; log('FAIL ' + what) } }
    function idOf(st) { return String($GFForgeReg.ITEMS.getKey(st.getItem())) }

    const PAIRS = [
        ['minecraft:gold_nugget', i.nugget_rose_gold],
        ['minecraft:gold_ingot', i.ingot_rose_gold],
        ['minecraft:gold_block', i.block_rose_gold]
    ]
    const FOODS = ['minecraft:golden_carrot', 'minecraft:golden_apple', 'minecraft:enchanted_golden_apple']
    const EXPECTED = ['golden_carrot_gilding', 'golden_carrot_arc_gilding', 'golden_apple_gilding',
        'golden_apple_arc_gilding', 'enchanted_golden_sequenced_assembly']

    function tags() {
        ok(Ingredient.of(i.any_nugget_gold).test(Item.of(i.nugget_rose_gold)), 'tag ' + i.any_nugget_gold + ' holds ' + i.nugget_rose_gold)
        ok(Ingredient.of(i.any_ingot_gold).test(Item.of(i.ingot_rose_gold)), 'tag ' + i.any_ingot_gold + ' holds ' + i.ingot_rose_gold)
        ok(Ingredient.of(i.any_block_gold).test(Item.of(i.block_rose_gold)), 'tag ' + i.any_block_gold + ' holds ' + i.block_rose_gold)
    }

    // Every predicate-like thing a recipe checks its inputs with.
    function inputsOf(r) {
        let out = []
        try { r.getIngredients().forEach(g => out.push(g)) } catch (e) { }
        try { r.additives.forEach(g => out.push(g)) } catch (e) { }
        try {
            r.getSequence().forEach(s => {
                try { s.getRecipe().getIngredients().forEach(g => out.push(g)) } catch (e) { }
            })
        } catch (e) { }
        return out
    }

    function recipes(server) {
        let seen = []
        server.getRecipeManager().getRecipes().forEach(r => {
            let res
            try { res = r.getResultItem() } catch (e) { return }
            if (res == null || res.isEmpty() || FOODS.indexOf(idOf(res)) < 0) return
            let id = String(r.getId())
            seen.push(id)
            let golds = 0, bad = []
            inputsOf(r).forEach(g => PAIRS.forEach(p => {
                if (!g.test(Item.of(p[0]))) return
                golds++
                if (!g.test(Item.of(p[1]))) bad.push(p[0])
            }))
            log('recipe ' + id + ' (' + r.getType() + ') -> ' + idOf(res) + ': ' + golds + ' gold inputs')
            if (golds > 0) ok(bad.length == 0, 'recipe ' + id + ' takes rose gold wherever it takes gold' +
                (bad.length ? ' (refuses rose gold for ' + bad.join(', ') + ')' : ''))
        })
        EXPECTED.forEach(k => ok(seen.some(s => s.endsWith('/' + k) || s.endsWith(':' + k)), 'recipe *' + k + ' loaded'))
        ok(seen.indexOf('minecraft:golden_carrot') < 0, 'vanilla minecraft:golden_carrot removed')
    }

    function run(server) {
        pass = 0; fail = 0
        try { tags() } catch (e) { fail++; log('ERROR tags ' + e) }
        try { recipes(server) } catch (e) { fail++; log('ERROR recipes ' + e) }
        log('done: ' + pass + ' PASS, ' + fail + ' FAIL')
    }

    ServerEvents.commandRegistry(event => {
        const { commands: Commands } = event
        event.register(Commands.literal('gftest').executes(ctx => {
            run(ctx.source.getServer())
            return 1
        }))
    })
})()
