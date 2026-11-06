// SQ-SNF-03 craft probe (feature_writeup/testing/scratch_queue.md). Scratch-only: copy into a scratch server's
// kubejs/server_scripts, never into the pack. Run /sntest; results are "[sntest] PASS|FAIL ..." lines in logs/latest.log.
// Builds a 3x3 crafting grid on a fake player's inventory menu and asks the RecipeManager which seasoning recipe matches.
(() => {
    const $SNFakeFactory = Java.loadClass('net.minecraftforge.common.util.FakePlayerFactory')
    const $SNGameProfile = Java.loadClass('com.mojang.authlib.GameProfile')
    const $SNUUID = Java.loadClass('java.util.UUID')
    const $SNCraftingContainer = Java.loadClass('net.minecraft.world.inventory.CraftingContainer')
    const $SNRecipeType = Java.loadClass('net.minecraft.world.item.crafting.RecipeType')
    const $SNSeasonings = Java.loadClass('com.SaintAlfonzo.creativedelight.food.Seasonings')
    const $SNRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $SNList = Java.loadClass('java.util.List')
    const i = global.item

    function stack(id) {
        // tag keys ('#ns:path') resolve to the tag's first item
        return Item.of(id).copy()
    }

    function seasonMatch(level, fake, stacks) {
        let grid = new $SNCraftingContainer(fake.inventoryMenu, 3, 3)
        stacks.forEach((s, n) => grid.setItem(n, s))
        let found = level.getServer().getRecipeManager().getRecipeFor($SNRecipeType.CRAFTING, grid, level)
        if (!found.isPresent()) return null
        return String(found.get().getId())
    }

    ServerEvents.commandRegistry(event => {
        const { commands: Commands } = event
        event.register(Commands.literal('sntest').requires(s => s.hasPermission(2)).executes(ctx => {
            let level = ctx.source.getLevel()
            let fake = $SNFakeFactory.get(level, new $SNGameProfile($SNUUID.fromString('5e450000-0000-0000-0000-00000000005e'), '[sntest]'))
            let garlicPopcorn = $SNSeasonings.with(stack(i.popcorn), $SNList.of(new $SNRL('creativeengineering:garlic')))
            let cases = [
                ['popcorn + flaxen cheese wedge', [stack(i.popcorn), stack(i.flaxen_cheese_wedge)], true],
                ['tortilla chip + cheese wedge', [stack(i.tortilla_chip), stack(i.flaxen_cheese_wedge)], false],
                ['waffle + onion', [stack(i.waffle), stack(i.onion)], false],
                ['garlic popcorn + onion', [garlicPopcorn, stack(i.onion)], true],
                ['garlic popcorn + garlic', [garlicPopcorn.copy(), stack(i.garlic)], false],
            ]
            let pass = 0
            cases.forEach(c => {
                let id = null
                try { id = seasonMatch(level, fake, c[1]) } catch (err) { id = 'ERROR ' + err }
                let seasoned = id != null && id.indexOf('season') >= 0
                let ok = c[2] ? seasoned : !seasoned
                if (ok) pass++
                console.info(`[sntest] ${ok ? 'PASS' : 'FAIL'} ${c[0]}: expected ${c[2] ? 'a season recipe' : 'no season recipe'}, got ${id}`)
            })
            console.info(`[sntest] done ${pass}/${cases.length}`)
            return 1
        }))
    })
})()
