// Scratch queue SQ-MG-10 probe: item merges 2.1 to 2.5 (feature_writeup/item_merges.md, MG-01 to MG-14).
// Scratch-only: copy into a scratch server's kubejs/server_scripts as zz_mgcore_probe.js
// (tools/creative_labels_test/scratch_server.sh), never into the pack. Run /mgcore; results are
// "[mgcore] PASS|FAIL ..." lines in logs/latest.log, then "[mgcore] done N pass, M fail".
// Not covered here (client or a real player): JEI hiding, names, the inventory swap, the milk cauldron, monkeys.
(() => {
    const $MCFakeFactory = Java.loadClass('net.minecraftforge.common.util.FakePlayerFactory')
    const $MCGameProfile = Java.loadClass('com.mojang.authlib.GameProfile')
    const $MCUUID = Java.loadClass('java.util.UUID')
    const $MCCrafting = Java.loadClass('net.minecraft.world.inventory.CraftingContainer')
    const $MCRecipeType = Java.loadClass('net.minecraft.world.item.crafting.RecipeType')
    const $MCForgeReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $MCEvents = Java.loadClass('net.minecraftforge.event.ForgeEventFactory')
    const $MCHand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $MCRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $MCTagKey = Java.loadClass('net.minecraft.tags.TagKey')
    const $MCRegistry = Java.loadClass('net.minecraft.core.Registry')
    const $MCLootBuilder = Java.loadClass('net.minecraft.world.level.storage.loot.LootContext$Builder')
    const $MCLootParams = Java.loadClass('net.minecraft.world.level.storage.loot.parameters.LootContextParams')
    const $MCLootSets = Java.loadClass('net.minecraft.world.level.storage.loot.parameters.LootContextParamSets')
    const $MCVec3 = Java.loadClass('net.minecraft.world.phys.Vec3')

    const PURGED = ['alexsmobs:banana', 'neapolitan:milk_bottle', 'neapolitan:chocolate_cake',
        'moredelight:diced_potatoes', 'decorative_blocks:rocky_dirt']

    let pass = 0, fail = 0
    function check(name, ok, detail) {
        if (ok) pass++; else fail++
        console.info('[mgcore] ' + (ok ? 'PASS' : 'FAIL') + ' ' + name + ': ' + detail)
    }
    function idOf(st) { return String($MCForgeReg.ITEMS.getKey(st.getItem())) }
    function desc(st) { return st == null ? 'nothing' : st.getCount() + 'x ' + idOf(st) }
    // ItemStack.is / BlockState.is have several one-argument overloads Rhino can't pick between (and the runtime name
    // is obfuscated, so the 'is(Type)' signature form fails too); both have getTags()
    function hasTag(obj, tag) { return obj.getTags().anyMatch(t => t.equals(tag)) }
    function itemTag(tag) { return $MCTagKey.create($MCRegistry.ITEM_REGISTRY, new $MCRL(tag)) }
    function blockTag(tag) { return $MCTagKey.create($MCRegistry.BLOCK_REGISTRY, new $MCRL(tag)) }
    function recipe(rm, id) {
        let opt = rm.byKey(new $MCRL(id))
        return opt.isPresent() ? opt.get() : null
    }

    function craft(level, p, stacks) {
        let cc = new $MCCrafting(p.inventoryMenu, 3, 3)
        stacks.forEach((st, n) => { if (st) cc.setItem(n, Item.of(st)) })
        let opt = level.getServer().getRecipeManager().getRecipeFor($MCRecipeType.CRAFTING, cc, level)
        if (!opt.isPresent()) return { id: 'none', out: null }
        let r = opt.get()
        return { id: String(r.getId()), out: r.assemble(cc) }
    }
    function craftCheck(level, p, name, stacks, wantId, wantCount) {
        let c = craft(level, p, stacks)
        check(name, c.out != null && idOf(c.out) === wantId && c.out.getCount() == wantCount, desc(c.out) + ' via ' + c.id)
    }

    function run(level) {
        let server = level.getServer()
        let rm = server.getRecipeManager()
        let p = $MCFakeFactory.get(level, new $MCGameProfile($MCUUID.randomUUID(), 'mgcore'))
        p.getInventory().clearContent()

        // startup list (MG-01 server half): five merges, each purged id flagged and stripped of every item tag
        let merges = global.itemMerges || []
        check('merge list', merges.length == 5, merges.map(m => m[0] + ' -> ' + m[1]).join(', '))
        PURGED.forEach(id => {
            let item = $MCForgeReg.ITEMS.getValue(new $MCRL(id))
            let tags = item.builtInRegistryHolder().tags().count()
            check('purged ' + id, global.removedItems.indexOf(id) >= 0 && tags == 0, 'tags ' + tags)
        })

        // no recipe left with a purged item as result or ingredient
        let hits = []
        rm.getRecipes().forEach(r => {
            let ids = []
            try { ids.push(idOf(r.getResultItem())) } catch (err) {}
            try { r.getIngredients().forEach(ing => { let it = ing.getStacks().iterator(); while (it.hasNext()) ids.push(idOf(it.next())) }) } catch (err) {}
            if (ids.some(x => PURGED.indexOf(x) >= 0)) hits.push(String(r.getId()))
        })
        check('no recipe names a purged item', hits.length == 0, hits.slice(0, 10).join(', ') || 'none')

        // removed duplicates
        ;['create:filling/compat/neapolitan/milk_bottle', 'create:emptying/compat/neapolitan/milk_bottle',
          'delightful:food/kettle/matcha_latte_neapolitan', 'abnormals_delight:neapolitan/cutting/chocolate_cake',
          'moredelight:cutting/diced_potatoes', 'decorative_blocks:rocky_dirt', 'decorative_blocks:dirt_from_rocky_dirt',
          'davebuildingmod:rec_rocky_dirt', 'twigs:rocky_dirt'].forEach(id => check('removed ' + id, recipe(rm, id) == null, ''))

        // 2.1 banana: tags, eating leaves a peel (MG-03), elephant chest loot
        let nb = Item.of('neapolitan:banana')
        check('banana in #forge:crops/banana', hasTag(nb, itemTag('forge:crops/banana')), '')
        check('banana in #alexsmobs:bananas', hasTag(nb, itemTag('alexsmobs:bananas')), '')
        p.setItemInHand($MCHand.MAIN_HAND, Item.of('neapolitan:banana'))
        let held = p.getMainHandItem()
        let copy = held.copy()
        let res = held.finishUsingItem(level, p)
        res = $MCEvents.onItemUseFinish(p, copy, 0, res)
        let peels = p.getInventory().countItem(Item.of('alexsmobs:banana_peel').getItem())
        check('eating a banana leaves a peel', peels == 1, 'peels ' + peels)
        let table = server.getLootTables().get(new $MCRL('alexsmobs:gameplay/trader_elephant_chest'))
        let alexs = 0, neap = 0
        for (let n = 0; n < 300; n++) {
            let ctx = new $MCLootBuilder(level).withParameter($MCLootParams.ORIGIN, new $MCVec3(0, 64, 0))
                .withRandom(level.random).create($MCLootSets.CHEST)
            table.getRandomItems(ctx).forEach(st => {
                let id = idOf(st)
                if (id === 'alexsmobs:banana') alexs += st.getCount()
                else if (id === 'neapolitan:banana') neap += st.getCount()
            })
        }
        check('elephant chest: bananas are Neapolitan\'s', alexs == 0 && neap > 0, '300 rolls: alexsmobs ' + alexs + ', neapolitan ' + neap)

        // 2.2 milk bottle (MG-06)
        check('FD bottle in #forge:bottles/milk', hasTag(Item.of('farmersdelight:milk_bottle'), itemTag('forge:bottles/milk')), '')
        craftCheck(level, p, 'milk bucket + 3 glass bottles',
            ['minecraft:milk_bucket', 'minecraft:glass_bottle', 'minecraft:glass_bottle', 'minecraft:glass_bottle'],
            'farmersdelight:milk_bottle', 3)
        craftCheck(level, p, 'bucket + 3 FD milk bottles',
            ['minecraft:bucket', 'farmersdelight:milk_bottle', 'farmersdelight:milk_bottle', 'farmersdelight:milk_bottle'],
            'minecraft:milk_bucket', 1)

        // 2.3 chocolate cake (MG-10 server half): knife recipe and the placed-cake slice tag
        let cut = recipe(rm, 'creativeengineering:farmersdelightcutting/chocolate_cake')
        let cutOut = 'missing'
        if (cut) {
            cutOut = desc(cut.getResultItem())
            try { let outs = []; cut.getResults().forEach(st => outs.push(desc(st))); cutOut = outs.join(', ') } catch (err) {}
        }
        check('knife: CC&A cake -> 7 slices', cut != null && cutOut.indexOf('7x abnormals_delight:chocolate_cake_slice') >= 0
            && cut.getIngredients().get(0).test(Item.of('createaddition:chocolate_cake')), cutOut)
        let cakeBlock = $MCForgeReg.BLOCKS.getValue(new $MCRL('createaddition:chocolate_cake'))
        check('placed CC&A cake drops slices', hasTag(cakeBlock.defaultBlockState(), blockTag('abnormals_delight:drops_chocolate_cake_slice')), '')
        let slices = []
        for (let n = 0; n < 7; n++) slices.push('abnormals_delight:chocolate_cake_slice')
        craftCheck(level, p, '7 slices -> cake', slices, 'createaddition:chocolate_cake', 1)

        // 2.4 diced potatoes (MG-11, MG-12 server half)
        check('potato slices in #forge:sliced_potato', hasTag(Item.of('rusticdelight:potato_slices'), itemTag('forge:sliced_potato')), '')
        check('Rustic potato cutting kept', recipe(rm, 'rusticdelight:cutting/potato_slices') != null, '')

        // 2.5 rocky dirt (MG-14): pebbles make Pebble Dirt (Dave's), cobblestone makes Rocky Dirt (Twigs'), coarse x2
        craftCheck(level, p, 'Pebble Dirt from dirt', ['minecraft:dirt', 'twigs:pebble', null, 'twigs:pebble', 'minecraft:dirt'],
            'davebuildingmod:rocky_dirt', 4)
        craftCheck(level, p, 'Pebble Dirt from coarse dirt', ['minecraft:coarse_dirt', 'twigs:pebble', null, 'twigs:pebble', 'minecraft:coarse_dirt'],
            'davebuildingmod:rocky_dirt', 8)
        craftCheck(level, p, 'Rocky Dirt from dirt', ['minecraft:dirt', 'minecraft:cobblestone', 'minecraft:cobblestone', 'minecraft:dirt'],
            'twigs:rocky_dirt', 4)
        craftCheck(level, p, 'Rocky Dirt from coarse dirt', ['minecraft:coarse_dirt', 'minecraft:cobblestone', 'minecraft:cobblestone', 'minecraft:coarse_dirt'],
            'twigs:rocky_dirt', 8)
        craftCheck(level, p, 'Pebble Dirt slab', ['davebuildingmod:rocky_dirt', 'davebuildingmod:rocky_dirt', 'davebuildingmod:rocky_dirt'],
            'davebuildingmod:rocky_dirt_slab', 6)
    }

    ServerEvents.commandRegistry(event => {
        const { commands: Commands } = event
        event.register(Commands.literal('mgcore').executes(ctx => {
            pass = 0; fail = 0
            try { run(ctx.source.getLevel()) } catch (e) { check('run', false, 'ERROR ' + e) }
            console.info('[mgcore] done ' + pass + ' pass, ' + fail + ' fail')
            return 1
        }))
    })
})()
