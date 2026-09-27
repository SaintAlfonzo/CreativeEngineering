// Dump every data-driven table the server has loaded.
//
//   /dumploot            -> loot tables only
//   /dumploot all        -> loot tables + villager / wandering-trader trades
//   /dumploot <samples>   (implies "all")  -> trades sampled N times per listing (default 24)
//
// Output (under the instance's kubejs/ folder):
//   kubejs/exported/loot_tables/<namespace>/<path>.json   - one file per loot table
//       (mob drops = entities/*, chests/*, gameplay/fishing*, archaeology/*, blocks/*, ...)
//   kubejs/exported/villager_trades.json                  - trades, grouped profession -> level
//
// Trades are NOT loot tables - they're ItemListing factories that build a MerchantOffer from
// an entity + RNG - so the trade half spawns a throwaway villager per profession and samples
// each listing, de-duping the results. Random listings (enchanted books, treasure maps, and
// "gatherer" trades that vary by villager biome type) only show variants that rolled.
//
// Wrapped in an IIFE so its helpers don't collide with other _lib scripts.

(() => {

    const $LootTables = Java.loadClass('net.minecraft.world.level.storage.loot.LootTables')
    const $VillagerTrades = Java.loadClass('net.minecraft.world.entity.npc.VillagerTrades')
    const $VillagerProfession = Java.loadClass('net.minecraft.world.entity.npc.VillagerProfession')
    const $EntityType = Java.loadClass('net.minecraft.world.entity.EntityType')
    const $RandomSource = Java.loadClass('net.minecraft.util.RandomSource')
    const $ForgeRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $HashMap = Java.loadClass('java.util.HashMap')
    const $ArrayList = Java.loadClass('java.util.ArrayList')
    const $JsonObject = Java.loadClass('com.google.gson.JsonObject')

    // ------------------------------------------------------------ loot tables

    // JsonIO.write does not create parent directories, and one-file-per-table would need
    // thousands of nested folders. Instead group every table into one JSON object per
    // namespace and write flat files: kubejs/exported/loot_tables.<namespace>.json,
    // keyed by the table's path (e.g. "entities/zombie", "chests/simple_dungeon").
    function dumpLootTables(source) {
        const lootTables = source.getServer().getLootTables()
        const ids = new $ArrayList(lootTables.getIds())
        ids.sort(null)

        const byNamespace = {}
        let ok = 0, failed = 0
        ids.forEach(id => {
            try {
                var ns = id.getNamespace()
                var obj = byNamespace[ns] || (byNamespace[ns] = new $JsonObject())
                obj.add(id.getPath(), $LootTables.serialize(lootTables.get(id)))
                ok++
            } catch (err) {
                failed++
                console.warn(`[dumploot] ${id}: ${err.message || err}`)
            }
        })

        let files = 0
        for (var ns in byNamespace) {
            writeExportJson(`kubejs/exported/loot_tables/${ns}.json`, byNamespace[ns])
            files++
        }

        const msg = `Dumped ${ok} loot tables into ${files} files (kubejs/exported/loot_tables/<namespace>.json)${failed ? `, ${failed} failed - see log` : ''}`
        source.sendSuccess(Text.green(msg), false)
        console.info(msg)
    }

    // ------------------------------------------------------------ trades

    function stackJson(stack) {
        if (!stack || stack.isEmpty()) return null
        const out = { item: String($ForgeRegistries.ITEMS.getKey(stack.getItem())), count: stack.getCount() }
        const nbt = stack.getNbt()
        if (nbt) out.nbt = String(nbt)
        return out
    }

    function offerJson(offer) {
        return {
            costA: stackJson(offer.getCostA()),
            costB: stackJson(offer.getCostB()),
            result: stackJson(offer.getResult()),
            maxUses: offer.getMaxUses(),
            villagerXp: offer.getXp(),
            priceMultiplier: offer.getPriceMultiplier(),
            demand: offer.getDemand()
        }
    }

    // fastutil Int2ObjectMap: Rhino's wrapper calls containsKey(String) on member access and
    // throws ClassCastException. Copy into a plain HashMap<Integer,V> first.
    function intKeyedEntries(int2ObjectMap) {
        const copy = new $HashMap(int2ObjectMap)
        const entries = []
        copy.forEach((k, v) => entries.push([k, v]))
        return entries
    }

    function sampleListings(listings, villager, rand, count) {
        const rows = []
        const size = listings.length !== undefined ? listings.length : listings.size()
        const at = j => listings.length !== undefined ? listings[j] : listings.get(j)
        for (let x = 0; x < size; x++) {
            var listing = at(x)
            var seen = {}
            var offers = []
            for (let s = 0; s < count; s++) {
                let offer
                try {
                    offer = listing.getOffer(villager, rand)
                } catch (err) {
                    offers.push({ error: String(err.message || err) })
                    break
                }
                if (offer == null) continue
                var j = offerJson(offer)
                var key = JSON.stringify(j)
                if (seen[key]) continue
                seen[key] = true
                offers.push(j)
            }
            rows.push({ listing: String(listing.getClass().getName()).replace(/.*[.$]/, ''), offers: offers })
        }
        return rows
    }

    function makeVillager(level, profession) {
        const villager = $EntityType.VILLAGER.create(level)
        villager.setPos(0.5, 64, 0.5)
        villager.setVillagerData(villager.getVillagerData().setProfession(profession).setLevel(5))
        return villager
    }

    function dumpTrades(source, samples) {
        const level = source.getLevel()
        const rand = $RandomSource.create()
        const result = { samplesPerListing: samples, professions: {}, wandering_trader: {} }
        let professionCount = 0

        $VillagerTrades.TRADES.forEach((profession, byLevel) => {
            var key = String($ForgeRegistries.VILLAGER_PROFESSIONS.getKey(profession))
            var villager = makeVillager(level, profession)
            var levels = {}
            intKeyedEntries(byLevel).forEach(entry => {
                levels['level_' + entry[0]] = sampleListings(entry[1], villager, rand, samples)
            })
            villager.discard()
            result.professions[key] = levels
            professionCount++
        })

        const wt = makeVillager(level, $VillagerProfession.NONE)
        intKeyedEntries($VillagerTrades.WANDERING_TRADER_TRADES).forEach(entry => {
            result.wandering_trader['pool_' + entry[0]] = sampleListings(entry[1], wt, rand, samples)
        })
        wt.discard()

        writeExportJson('kubejs/exported/villager_trades.json', result)

        const msg = `Dumped ${professionCount} trade professions (x${samples} samples) -> kubejs/exported/villager_trades.json`
        source.sendSuccess(Text.green(msg), false)
        console.info(msg)
    }

    // ------------------------------------------------------------ command

    function runDump(source, withTrades, samples) {
        dumpLootTables(source)
        if (withTrades) dumpTrades(source, samples)
        return 1
    }

    ServerEvents.commandRegistry(event => {
        const Commands = Java.loadClass('net.minecraft.commands.Commands')
        const StringArgumentType = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')

        event.register(
            Commands.literal('dumploot')
                .requires(src => src.hasPermission(2))
                .executes(ctx => runDump(ctx.source, false, 24))
                .then(Commands.argument('mode', StringArgumentType.word())
                    .executes(ctx => {
                        var arg = StringArgumentType.getString(ctx, 'mode')
                        var n = parseInt(arg)
                        if (!isNaN(n)) return runDump(ctx.source, true, Math.max(1, Math.min(500, n)))
                        return runDump(ctx.source, arg.toLowerCase() === 'all', 24)
                    }))
        )
    })

})()
