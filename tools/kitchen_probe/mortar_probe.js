// TODO cooking/kitchen #10 probe: mortar and pestle crafts (NCH.mortar via foodGrinding and cooking/mortar.js) and
// the bamboo bowl. Scratch-only: copy into a scratch server's kubejs/server_scripts
// (tools/creative_labels_test/scratch_server.sh). Run /kmtest; results are "[kmtest] PASS|FAIL ..." lines.
(() => {
    const $KMFakeFactory = Java.loadClass('net.minecraftforge.common.util.FakePlayerFactory')
    const $KMGameProfile = Java.loadClass('com.mojang.authlib.GameProfile')
    const $KMUUID = Java.loadClass('java.util.UUID')
    const $KMCrafting = Java.loadClass('net.minecraft.world.inventory.CraftingContainer')
    const $KMRecipeType = Java.loadClass('net.minecraft.world.item.crafting.RecipeType')
    const $KMRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $KMForgeReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')

    function check(name, ok, detail) { console.info('[kmtest] ' + (ok ? 'PASS' : 'FAIL') + ' ' + name + ': ' + detail) }
    function idOf(st) { return String($KMForgeReg.ITEMS.getKey(st.getItem())) }

    // put the stacks in a 3x3 grid, ask the recipe manager, return result + leftovers
    function craft(level, p, stacks) {
        let cc = new $KMCrafting(p.inventoryMenu, 3, 3)
        stacks.forEach((st, n) => cc.setItem(n, st))
        let opt = level.getServer().getRecipeManager().getRecipeFor($KMRecipeType.CRAFTING, cc, level)
        if (!opt.isPresent()) return null
        let r = opt.get()
        let rem = []
        r.getRemainingItems(cc).forEach(st => { if (!st.isEmpty()) rem.push(idOf(st) + ' dmg ' + st.getDamageValue()) })
        let out = r.assemble(cc)
        return { id: String(r.getId()), out: out.getCount() + 'x ' + idOf(out), rem: rem.join(', ') }
    }

    // Runs once the server has loaded (no console needed), and again on /kmtest.
    function run(level) {
            try {
                let rm = level.getServer().getRecipeManager()
                let p = $KMFakeFactory.get(level, new $KMGameProfile($KMUUID.randomUUID(), 'km'))
                let ids = ['creativeengineering:mortar/corn_cornmeal', 'creativeengineering:mortar/wild_corn_cornmeal',
                    'creativeengineering:mortar/corn_kernels_cornmeal', 'creativeengineering:createcrushing/wild_corn_cornmeal',
                    'creativeengineering:createmilling/wild_corn_cornmeal', 'creativeengineering:iebottling/cornmeal_tortilla_raw',
                    'creativeengineering:mortar/hay_block_flour', 'creativeengineering:createmilling/hay_block_flour',
                    'creativeengineering:createcrushing/hay_block_flour', 'creativeengineering:createmilling/corn_cornmeal',
                    'creativeengineering:shaped/bowl_from_bamboo', 'creativeengineering:shaped/mortar_and_pestle',
                    'creativeengineering:createsplashing/cornmeal_tortilla_raw']
                let catalog = JSON.parse(String(JsonIO.readString('kubejs/generation_scripts/food/mortar_catalog.json')))
                catalog.recipes.forEach(r => ids.push('creativeengineering:mortar/' + r.id))
                ids.forEach(id => check('KM-01 loaded ' + id, rm.byKey(new $KMRL(id)).isPresent(), ''))

                let mortar = () => Item.of('creativeengineering:mortar_and_pestle')
                let r1 = craft(level, p, [Item.of('minecraft:wheat'), mortar()])
                check('KM-02 wheat + mortar', r1 != null && r1.out.indexOf('wheat_flour') >= 0 && r1.rem.indexOf('mortar_and_pestle dmg 1') >= 0,
                    r1 == null ? 'no recipe' : r1.id + ' -> ' + r1.out + ', left ' + r1.rem)
                let r2 = craft(level, p, [Item.of('corn_delight:corn_seeds'), Item.of('corn_delight:corn_seeds'), Item.of('corn_delight:corn_seeds'), mortar()])
                check('KM-03 3 kernels + mortar', r2 != null && r2.out == '1x creativeengineering:cornmeal',
                    r2 == null ? 'no recipe' : r2.id + ' -> ' + r2.out + ', left ' + r2.rem)
                let r3 = craft(level, p, [Item.of('minecraft:sugar_cane'), mortar()])
                check('KM-04 sugar cane + mortar', r3 != null && r3.out == '2x minecraft:sugar', r3 == null ? 'no recipe' : r3.id + ' -> ' + r3.out)
                let r4 = craft(level, p, [Item.of('minecraft:bamboo'), Item.of('minecraft:air'), Item.of('minecraft:bamboo'),
                    Item.of('minecraft:air'), Item.of('minecraft:bamboo')])
                check('KM-05 bamboo bowl', r4 != null && r4.out == '1x minecraft:bowl', r4 == null ? 'no recipe' : r4.id + ' -> ' + r4.out)
                let r5 = craft(level, p, [Item.of('minecraft:air'), Item.of('minecraft:lever'), Item.of('minecraft:air'),
                    Item.of('minecraft:polished_granite'), Item.of('minecraft:air'), Item.of('minecraft:polished_granite'),
                    Item.of('minecraft:air'), Item.of('minecraft:polished_granite')])
                check('KM-06 mortar craft', r5 != null && r5.out == '1x creativeengineering:mortar_and_pestle', r5 == null ? 'no recipe' : r5.id + ' -> ' + r5.out)
                let r6 = craft(level, p, [Item.of('minecraft:wheat'), Item.of('oldguns:mortar_and_pestle')])
                check('KM-07 wheat + Powder Pestle gives nothing', r6 == null, r6 == null ? 'no recipe' : r6.id + ' -> ' + r6.out)
                // corn: the machines average 1.5 cornmeal, so the mortar takes 2 corn for 3 (dishes/ingredients #14)
                let r7 = craft(level, p, [Item.of('corn_delight:corn'), Item.of('corn_delight:corn'), mortar()])
                check('KM-08 2 corn + mortar', r7 != null && r7.out == '3x creativeengineering:cornmeal' && r7.rem.indexOf('mortar_and_pestle dmg 1') >= 0,
                    r7 == null ? 'no recipe' : r7.id + ' -> ' + r7.out + ' / ' + r7.rem)
                let r8 = craft(level, p, [Item.of('corn_delight:corn'), mortar()])
                // crates and bags are machine-only (dishes/ingredients #14)
                ;['corn_crate_cornmeal', 'corn_kernel_bag_cornmeal'].forEach(id => check('KM-12 no mortar/' + id,
                    !rm.byKey(new $KMRL('creativeengineering:mortar/' + id)).isPresent(), ''))
                let r11 = craft(level, p, [Item.of('corn_delight:wild_corn'), Item.of('corn_delight:wild_corn'), mortar()])
                check('KM-13 2 wild corn + mortar', r11 != null && r11.out == '3x creativeengineering:cornmeal',
                    r11 == null ? 'no recipe' : r11.id + ' -> ' + r11.out)
                check('KM-09 1 corn + mortar gives nothing', r8 == null, r8 == null ? 'no recipe' : r8.id + ' -> ' + r8.out)
                let cm = () => Item.of('creativeengineering:cornmeal'), air = () => Item.of('minecraft:air')
                let r9 = craft(level, p, [cm(), cm(), cm(), air(), Item.of('minecraft:potion', '{Potion:"minecraft:water"}')])
                check('KM-10 tortilla with a water bottle', r9 != null && r9.out == '3x corn_delight:tortilla_raw' && r9.rem.indexOf('glass_bottle') >= 0,
                    r9 == null ? 'no recipe' : r9.id + ' -> ' + r9.out + ' / ' + r9.rem)
                let r10 = craft(level, p, [cm(), cm(), cm(), air(), Item.of('minecraft:water_bucket')])
                check('KM-11 tortilla with a water bucket', r10 != null && r10.out == '3x corn_delight:tortilla_raw' && r10.rem.indexOf('minecraft:bucket') >= 0,
                    r10 == null ? 'no recipe' : r10.id + ' -> ' + r10.out + ' / ' + r10.rem)
            } catch (e) { console.info('[kmtest] FAIL run: ERROR ' + e) }
            console.info('[kmtest] INFO done')
    }

    ServerEvents.loaded(event => run(event.server.overworld()))
    ServerEvents.commandRegistry(event => {
        const { commands: Commands } = event
        event.register(Commands.literal('kmtest').executes(ctx => { run(ctx.source.getLevel()); return 1 }))
    })
})()
