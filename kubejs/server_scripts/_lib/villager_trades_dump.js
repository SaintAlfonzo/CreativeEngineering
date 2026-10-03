// Dump every registered villager trade, grouped by profession -> level.
//
//   /dumptrades            -> kubejs/exported/villager_trades.json
//   /dumptrades <samples>  -> same, with N getOffer() samples per listing (default 24)
//
// Trades are ItemListing factories, not static data: each builds a MerchantOffer from an
// entity + RNG. So this makes a throwaway villager per profession and samples each listing
// a number of times, de-duping the offers. Random listings (enchanted books, dyed armour,
// treasure maps, and "gatherer" trades that vary by villager biome type) only show the
// variants that happened to roll - raise <samples> to catch more.
// Also dumps Vinery's Wandering Winemaker pool (its own static map, not part of the wanderer pools).
// Not seen here: Quark's Pathfinder's Quill, which Quark puts on every Wandering Trader outside the pools.

ServerEvents.commandRegistry(event => {
    const Commands = Java.loadClass('net.minecraft.commands.Commands')
    const IntegerArgumentType = Java.loadClass('com.mojang.brigadier.arguments.IntegerArgumentType')

    event.register(
        Commands.literal('dumptrades')
            .requires(src => src.hasPermission(2))
            .executes(ctx => dumpVillagerTrades(ctx.source, 24))
            .then(Commands.argument('samples', IntegerArgumentType.integer(1, 500))
                .executes(ctx => dumpVillagerTrades(ctx.source, IntegerArgumentType.getInteger(ctx, 'samples'))))
    )
})

const $VillagerTrades = Java.loadClass('net.minecraft.world.entity.npc.VillagerTrades')
const $VillagerProfession = Java.loadClass('net.minecraft.world.entity.npc.VillagerProfession')
const $EntityType = Java.loadClass('net.minecraft.world.entity.EntityType')
const $RandomSource = Java.loadClass('net.minecraft.util.RandomSource')
const $ForgeRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
const $HashMap = Java.loadClass('java.util.HashMap')

// VillagerTrades' per-level maps are fastutil Int2ObjectMap; Rhino's map wrapper calls
// containsKey(String) on them while resolving any member access, which throws
// ClassCastException. Copy into a plain HashMap<Integer,V> first.
function intKeyedEntries(int2ObjectMap) {
    const copy = new $HashMap(int2ObjectMap)
    const entries = []
    copy.forEach((k, v) => entries.push([k, v]))
    return entries
}

function stackJson(stack) {
    if (!stack || stack.isEmpty()) return null
    const out = {
        item: String($ForgeRegistries.ITEMS.getKey(stack.getItem())),
        count: stack.getCount()
    }
    const nbt = stack.getNbt() // KubeJS: null when the stack has no tag
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

function sampleListings(listingArray, villager, rand, count) {
    const rows = []
    for (let x = 0; x < listingArray.length; x++) {
        var listing = listingArray[x]
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
        rows.push({
            listing: String(listing.getClass().getName()).replace(/.*[.$]/, ''),
            offers: offers
        })
    }
    return rows
}

function makeVillager(level, profession) {
    const villager = $EntityType.VILLAGER.create(level)
    villager.setPos(0.5, 64, 0.5)
    villager.setVillagerData(villager.getVillagerData().setProfession(profession).setLevel(5))
    return villager
}

// Rhino rejects a const declared inside try {} here ("redeclaration of var"), hence a separate function with var.
function dumpWinemakerPool(level, rand, samples, result) {
    try {
        var wmClass = Java.loadClass('satisfyu.vinery.entity.WanderingWinemakerEntity')
        var wmType = $EntityType.byString('vinery:wandering_winemaker').orElse(null)
        var wm = wmType ? wmType.create(level) : null
        if (!wm) return
        wm.setPos(0.5, 64, 0.5)
        intKeyedEntries(wmClass.TRADES).forEach(entry => {
            result.wandering_winemaker['pool_' + entry[0]] = sampleListings(entry[1], wm, rand, samples)
        })
        wm.discard()
    } catch (err) {
        console.warn('[dumptrades] Wandering Winemaker pool not dumped: ' + err)
    }
}

function dumpVillagerTrades(source, samples) {
    const level = source.getLevel()
    const rand = $RandomSource.create()
    const result = { samplesPerListing: samples, professions: {}, wandering_trader: {}, wandering_winemaker: {} }
    let professionCount = 0
    let offerCount = 0

    // --- professions (includes any added by other mods) ---
    $VillagerTrades.TRADES.forEach((profession, byLevel) => {
        var key = String($ForgeRegistries.VILLAGER_PROFESSIONS.getKey(profession))
        var villager = makeVillager(level, profession)
        var levels = {}
        intKeyedEntries(byLevel).forEach(entry => {
            var rows = sampleListings(entry[1], villager, rand, samples)
            levels['level_' + entry[0]] = rows
            rows.forEach(r => { offerCount += r.offers.length })
        })
        villager.discard()
        result.professions[key] = levels
        professionCount++
    })

    // --- wandering trader: Int2ObjectMap<ItemListing[]> keyed by pool (1 = always, 2 = pick-N) ---
    const wt = makeVillager(level, $VillagerProfession.NONE)
    intKeyedEntries($VillagerTrades.WANDERING_TRADER_TRADES).forEach(entry => {
        result.wandering_trader['pool_' + entry[0]] = sampleListings(entry[1], wt, rand, samples)
    })
    wt.discard()

    // --- Vinery's Wandering Winemaker: WanderingWinemakerEntity.TRADES (key 1; it draws 8) ---
    dumpWinemakerPool(level, rand, samples, result)

    writeExportJson('kubejs/exported/villager_trades.json', result)

    const msg = `Dumped ${professionCount} professions (~${offerCount} offer variants) -> kubejs/exported/villager_trades.json`
    source.sendSuccess(Text.green(msg), false)
    console.info(msg)
    return 1
}
