// Scratch-only query command for bulk queue runs (feature_writeup/testing/scratch_queue.md). Copy into a scratch
// server's kubejs/server_scripts as zz_zq_probe.js, never into the pack.
//   /zq recipes <text>       recipe ids containing text, with their result
//   /zq rdetail <text>       same ids with serializer, result and inputs (first 3 items of each); "rdetail+" lines add
//                            rolled results with counts and chances, a sequenced assembly's start ingredient and steps
//   /zq bytype <serializer>  every recipe of that serializer id (e.g. barbequesdelight:combine), then the count
//   /zq uses <item id>       every recipe with an ingredient accepting the item ("alone" = nothing else accepted)
//   /zq makes <item id>      every recipe whose result is the item (no ':' = result id contains the text)
//   /zq food <item id>       nutrition, saturation modifier, effects (amplifier, ticks, chance)
//   /zq item <item id>       item class, max damage, max stack size
//   /zq potion <id> [a,b]    potion registered?; with ingredients: what each brews the potion into (Forge registry)
//   /zq season <b1,b2,..>    SeasonTypes.tagged (cached season type) of each biome
//   /zq count x1 y1 z1 x2 y2 z2 <block>   blocks of that id in a box and what they stand on (loaded chunks only)
//   /zq ages x1 y1 z1 x2 y2 z2 <block>    histogram and mean of the block's age property in a box (crop growth comparisons)
//   /zq floating <x> <z> <r> sand / red sand / gravel with air below, y 0 to 140 (loaded chunks only, first 10)
//   /zq under <x> <z> <rChunks> [id text|-] [dim]   structure starts judged on the placed blocks: air / water under the
//                            built bottom, water and earth_fill/earth ids in the base, native ring, foreign earth (SQ-EFB)
//   /zq chunk <x> <z> [dim]  ticket level, status and tickets of the chunk at block x z
//   /zq biometag <tag id>    biomes in a biome tag
//   /zq itemtag <tag id>     items in an item tag
//   /zq blocktag <tag id>    blocks in a block tag
//   /zq tagsof <item id>     item tags of an item (and block tags of its block)
//   /zq nbt <item id>        how scripts see an ItemStack's NBT (getTag vs .nbt)
//   /zq useitem <player>     air use of the main-hand item (the client's follow-up when a block use PASSes), then of
//                            the off-hand item if the main hand did not consume the action
//   /zq states all           block-state id count (Block.BLOCK_STATE_REGISTRY) against the 2^20 ceiling
// Output: "[zq] ..." lines in logs/latest.log, then "[zq] done".
(() => {
    const $ZQArgs = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $ZQReg = Java.loadClass('net.minecraft.core.Registry')
    const $ZQRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $ZQTagKey = Java.loadClass('net.minecraft.tags.TagKey')
    const $ZQCollectors = Java.loadClass('java.util.stream.Collectors')
    const $ZQBlock = Java.loadClass('net.minecraft.world.level.block.Block')
    const $ZQPU = Java.loadClass('net.minecraft.world.item.alchemy.PotionUtils')
    const $ZQBR = Java.loadClass('net.minecraftforge.common.brewing.BrewingRecipeRegistry')
    const $ZQRK = Java.loadClass('net.minecraft.resources.ResourceKey')
    const $ZQHand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $ZQSeason = Java.loadClass('com.SaintAlfonzo.creativecalendar.seasons.SeasonTypes')
    function out(s) { console.info('[zq] ' + s) }
    function zqIng(g) {
        let s = g.getStacks(), ids = []
        for (let k = 0; k < Math.min(3, s.size()); k++) ids.push(String(s.toList().get(k).getId()))
        return ids.join('/') + (s.size() > 3 ? '/+' + (s.size() - 3) : '')
    }
    function zqRolls(r) {
        // "N id @chance" per rolled result; null when the recipe has no getRollableResults (Rhino throws on a missing method)
        try {
            let o = []
            r.getRollableResults().forEach(c => {
                let st = c.getStack(), ch = 1
                try { ch = c.getChance() } catch (err) { }
                o.push(st.getCount() + ' ' + String(st.getId()) + ' @' + ch)
            })
            return o.join(', ')
        } catch (err) { return null }
    }
    function run(server, what, arg) {
        try {
            if (what == 'recipes') {
                let n = 0
                server.getRecipeManager().getRecipes().forEach(r => {
                    let id = String(r.getId())
                    if (id.indexOf(arg) < 0) return
                    n++
                    if (n > 80) return   // print at most 80, count them all
                    let res = ''
                    try { res = String(r.getResultItem()) } catch (err) { res = '?' }
                    out(id + ' -> ' + res)
                })
                out('recipes matching ' + arg + ': ' + n + (n > 80 ? ' (first 80 shown)' : ''))
            } else if (what == 'uses') {
                // recipes with an ingredient that accepts item arg; "alone" = that ingredient accepts nothing else
                let alone = [], shared = []
                server.getRecipeManager().getRecipes().forEach(r => {
                    let a = false, s = false
                    try {
                        r.getIngredients().forEach(g => {
                            let st = g.getStacks(), hit = false
                            for (let k = 0; k < st.size(); k++) if (String(st.toList().get(k).getId()) == arg) hit = true
                            if (hit && st.size() == 1) a = true
                            else if (hit) s = true
                        })
                    } catch (err) { }
                    if (a) alone.push(String(r.getId()))
                    else if (s) shared.push(String(r.getId()))
                })
                alone.forEach(id => out('alone ' + id))
                shared.forEach(id => out('shared ' + id))
                out('uses ' + arg + ': ' + alone.length + ' alone, ' + shared.length + ' with other items')
            } else if (what == 'makes') {
                // recipes whose result is item arg (full id), or whose result id contains arg when it has no ':'
                let n = 0
                server.getRecipeManager().getRecipes().forEach(r => {
                    let rid = ''
                    try { rid = String(r.getResultItem().getId()) } catch (err) { return }
                    if (arg.indexOf(':') >= 0 ? rid != arg : rid.indexOf(arg) < 0) return
                    n++
                    out(String(r.getId()) + ' -> ' + r.getResultItem().getCount() + ' ' + rid)
                })
                out('makes ' + arg + ': ' + n)
            } else if (what == 'food') {
                // nutrition, saturation modifier and effects (id amplifier ticks chance) of a food item
                let fp = Item.of(arg).getItem().getFoodProperties()
                if (fp == null) { out('food ' + arg + ': not a food'); return }
                let ef = []
                fp.getEffects().forEach(pr => {
                    let i = pr.getFirst()
                    ef.push(String($ZQReg.MOB_EFFECT.getKey(i.getEffect())) + ' amp ' + i.getAmplifier() + ' ' + i.getDuration() + 't x' + pr.getSecond())
                })
                out('food ' + arg + ': nutrition ' + fp.getNutrition() + ', modifier ' + fp.getSaturationModifier() + ', effects [' + ef.join('; ') + ']')
            } else if (what == 'item') {
                let it = Item.of(arg).getItem()
                out('item ' + arg + ': class ' + it.getClass().getSimpleName() + ', max damage ' + it.getMaxDamage() + ', max stack ' + it.getMaxStackSize())
            } else if (what == 'potion') {
                // registry presence, then what each ingredient (comma list after the potion id) brews it into
                let parts = arg.split(' ')
                let has = $ZQReg.POTION.containsKey(new $ZQRL(parts[0]))
                out('potion ' + parts[0] + ' registered: ' + has)
                if (!has || parts.length < 2) return
                let base = $ZQPU.setPotion(Item.of('minecraft:potion'), $ZQReg.POTION.get(new $ZQRL(parts[0])))
                parts[1].split(',').forEach(ing => {
                    let o = $ZQBR.getOutput(base, Item.of(ing))
                    out('  ' + parts[0] + ' + ' + ing + ' -> ' + (o.isEmpty() ? 'nothing' : String($ZQReg.ITEM.getKey(o.getItem())) + ' ' + String($ZQPU.getPotion(o).getName('')) ))
                })
            } else if (what == 'season') {
                // SeasonTypes.tagged (the cached season type) of each biome in a comma list
                let reg = server.registryAccess().registryOrThrow($ZQReg.BIOME_REGISTRY)
                arg.split(',').forEach(b => {
                    let h = reg.getHolderOrThrow($ZQRK.create($ZQReg.BIOME_REGISTRY, new $ZQRL(b)))
                    out('season ' + b + ': ' + $ZQSeason.tagged(h))
                })
            } else if (what == 'ages') {
                // ages x1 y1 z1 x2 y2 z2 <block>: histogram of the block's "age" property in the box (loaded chunks)
                let lv = null
                server.getAllLevels().forEach(l => { if (String(l.dimension) == 'minecraft:overworld') lv = l })
                let $ZQPos = Java.loadClass('net.minecraft.core.BlockPos')
                let a = arg.split(' ')
                let want = a[6], hist = {}, n = 0, sum = 0
                for (let x = Math.min(+a[0], +a[3]); x <= Math.max(+a[0], +a[3]); x++)
                    for (let y = Math.min(+a[1], +a[4]); y <= Math.max(+a[1], +a[4]); y++)
                        for (let z = Math.min(+a[2], +a[5]); z <= Math.max(+a[2], +a[5]); z++) {
                            let st = lv.getBlockState(new $ZQPos(x, y, z))
                            if (String($ZQReg.BLOCK.getKey(st.getBlock())) != want) continue
                            let age = -1
                            st.getProperties().forEach(p => { if (String(p.getName()) == 'age') age = +String(st.getValue(p)) })
                            hist[age] = (hist[age] || 0) + 1; n++; sum += age
                        }
                out('ages ' + want + ': ' + n + ' blocks, mean ' + (n ? (sum / n).toFixed(2) : '-') + ', ' + JSON.stringify(hist))
            } else if (what == 'count' || what == 'floating') {
                // count x1 y1 z1 x2 y2 z2 <block>: blocks of that id in the box (loaded chunks only)
                // floating x z r: sand / red sand / gravel with air below, y 0 to 140, loaded chunks only (first 10)
                let lv = null
                server.getAllLevels().forEach(l => { if (String(l.dimension) == 'minecraft:overworld') lv = l })
                let $ZQPos = Java.loadClass('net.minecraft.core.BlockPos')
                let a = arg.split(' ').map(v => v)
                let n = 0, found = [], skipped = 0, under = {}
                let x1, y1, z1, x2, y2, z2, want = null
                if (what == 'count') {
                    x1 = Math.min(+a[0], +a[3]); x2 = Math.max(+a[0], +a[3]); y1 = Math.min(+a[1], +a[4]); y2 = Math.max(+a[1], +a[4])
                    z1 = Math.min(+a[2], +a[5]); z2 = Math.max(+a[2], +a[5]); want = a[6]
                } else {
                    x1 = +a[0] - +a[2]; x2 = +a[0] + +a[2]; z1 = +a[1] - +a[2]; z2 = +a[1] + +a[2]; y1 = 0; y2 = 140
                }
                let fall = { 'minecraft:sand': 1, 'minecraft:red_sand': 1, 'minecraft:gravel': 1 }
                for (let x = x1; x <= x2; x++) for (let z = z1; z <= z2; z++) {
                    if (!lv.hasChunk(x >> 4, z >> 4)) { skipped++; continue }
                    let below = null
                    for (let y = y1; y <= y2; y++) {
                        let id = String($ZQReg.BLOCK.getKey(lv.getBlockState(new $ZQPos(x, y, z)).getBlock()))
                        if (want) { if (id == want) { n++; under[below] = (under[below] || 0) + 1 } }
                        else if (fall[id] && (below == 'minecraft:air' || below == 'minecraft:cave_air') && found.length < 10) found.push(id + ' ' + x + ' ' + y + ' ' + z)
                        below = id
                    }
                }
                if (want) out('count ' + want + ': ' + n + (skipped ? ' (' + skipped + ' columns in unloaded chunks skipped)' : '') + '; on: ' + Object.keys(under).map(k => k + ' ' + under[k]).join(', '))
                else { found.forEach(f => out('floating ' + f)); out('floating: ' + found.length + ' found' + (skipped ? ', ' + skipped + ' columns unloaded' : '')) }
            } else if (what == 'under') {
                // under <x> <z> <rChunks> [id text] [dim]: every structure start in the loaded chunks of the square, judged on
                // the blocks really placed (earth fill only cancels or prunes; the beard fills and ground match swaps).
                // Per start, over the columns of its pieces (each column once, at the lowest built block of the boxes):
                //   air-below    columns whose first block under the built bottom is air; max gap; gaps over 6 (beardReach)
                //   water-below  same for water (waterlogged counts); max depth
                //   base water   water inside the bottom 3 layers of the structure (a flooded floor)
                //   base earth   ids of earth_fill/earth blocks left in the bottom 3 layers (after ground match)
                //   ring         top blocks (OCEAN_FLOOR) every 2 blocks on a ring 3 out from the start's box: the native ground
                //   foreign      base earth ids that never occur in the ring (ground match should have swapped them)
                // Untested before its first scratch run (written 2026-11-02 for SQ-EFB); first 30 starts only.
                let a = arg.split(' '), cx0 = (+a[0]) >> 4, cz0 = (+a[1]) >> 4, r = +a[2], filt = a[3] && a[3] != '-' ? a[3] : null
                let dim = a[4] || 'minecraft:overworld', lv = null
                server.getAllLevels().forEach(l => { if (String(l.dimension) == dim) lv = l })
                if (!lv) { out('under: no level ' + dim); out('done'); return }
                let $P = Java.loadClass('net.minecraft.core.BlockPos')
                let $HT = Java.loadClass('net.minecraft.world.level.levelgen.Heightmap$Types')
                let sreg = lv.registryAccess().registryOrThrow($ZQReg.STRUCTURE_REGISTRY)
                let earth = {}
                $ZQReg.BLOCK.getTagOrEmpty($ZQTagKey.create($ZQReg.BLOCK_REGISTRY, new $ZQRL('creativeengineering:earth_fill/earth')))
                    .forEach(h => { earth[String($ZQReg.BLOCK.getKey(h.value()))] = 1 })
                function idAt(x, y, z) { return String($ZQReg.BLOCK.getKey(lv.getBlockState(new $P(x, y, z)).getBlock())) }
                function kind(x, y, z) {
                    let st = lv.getBlockState(new $P(x, y, z))
                    let fl = String($ZQReg.FLUID.getKey(st.getFluidState().getType()))
                    if (fl.indexOf('water') >= 0) return 'water'
                    if (st.isAir() || !st.getMaterial().isSolid()) return 'air'
                    return 'solid'
                }
                function top(m) { return Object.keys(m).sort((p, q) => m[q] - m[p]).slice(0, 6).map(k => k + ' ' + m[k]).join(', ') }
                let shown = 0, skipped = 0
                for (let cx = cx0 - r; cx <= cx0 + r && shown < 30; cx++) for (let cz = cz0 - r; cz <= cz0 + r && shown < 30; cz++) {
                    if (!lv.hasChunk(cx, cz)) { skipped++; continue }
                    lv.getChunk(cx, cz).getAllStarts().entrySet().forEach(en => {
                        if (shown >= 30) return
                        let st = en.getValue()
                        if (!st.isValid()) return
                        let id = String(sreg.getKey(en.getKey()))
                        if (filt && id.indexOf(filt) < 0) return
                        let cols = {}
                        st.getPieces().forEach(pc => {
                            let b = pc.getBoundingBox()
                            for (let x = b.minX(); x <= b.maxX(); x++) for (let z = b.minZ(); z <= b.maxZ(); z++) {
                                let k = x + ',' + z, c = cols[k]
                                if (!c) cols[k] = [b.minY(), b.maxY()]
                                else { c[0] = Math.min(c[0], b.minY()); c[1] = Math.max(c[1], b.maxY()) }
                            }
                        })
                        let n = 0, airB = 0, gapMax = 0, deep = 0, watB = 0, watMax = 0, baseWat = 0, baseEarth = {}, unloaded = 0
                        Object.keys(cols).forEach(k => {
                            let xz = k.split(','), x = +xz[0], z = +xz[1], c = cols[k]
                            if (!lv.hasChunk(x >> 4, z >> 4)) { unloaded++; return }
                            let y0 = null
                            for (let y = c[0]; y <= c[1]; y++) if (kind(x, y, z) == 'solid') { y0 = y; break }
                            if (y0 == null) return
                            n++
                            for (let y = y0; y <= y0 + 2; y++) {
                                if (kind(x, y, z) == 'water') baseWat++
                                let bid = idAt(x, y, z)
                                if (earth[bid]) baseEarth[bid] = (baseEarth[bid] || 0) + 1
                            }
                            let first = kind(x, y0 - 1, z)
                            if (first == 'solid') return
                            let gap = 0, wat = 0
                            for (let y = y0 - 1; y > y0 - 65; y--) { let kd = kind(x, y, z); if (kd == 'solid') break; gap++; if (kd == 'water') wat++ }
                            if (first == 'air') { airB++; gapMax = Math.max(gapMax, gap) } else { watB++; watMax = Math.max(watMax, wat) }
                            if (gap > 6) deep++
                        })
                        let bb = st.getBoundingBox(), ring = {}
                        for (let x = bb.minX() - 3; x <= bb.maxX() + 3; x += 2) for (let z = bb.minZ() - 3; z <= bb.maxZ() + 3; z += 2) {
                            if (x > bb.minX() - 3 && x < bb.maxX() + 2 && z > bb.minZ() - 3 && z < bb.maxZ() + 2) continue
                            if (!lv.hasChunk(x >> 4, z >> 4)) continue
                            let t = idAt(x, lv.getHeight($HT.OCEAN_FLOOR, x, z) - 1, z)
                            ring[t] = (ring[t] || 0) + 1
                        }
                        let foreign = Object.keys(baseEarth).filter(k => !ring[k])
                        let o = st.getChunkPos()
                        out('under ' + id + ' chunk ' + o.x + ' ' + o.z + ' box ' + bb.minX() + ' ' + bb.minY() + ' ' + bb.minZ() + ' to ' + bb.maxX() + ' ' + bb.maxY() + ' ' + bb.maxZ()
                            + ' | pieces ' + st.getPieces().size() + ' cols ' + n + (unloaded ? ' (' + unloaded + ' unloaded)' : '')
                            + ' | air-below ' + airB + ' max ' + gapMax + ' over6 ' + deep + ' | water-below ' + watB + ' max ' + watMax
                            + ' | base water ' + baseWat + ' | base earth: ' + (top(baseEarth) || 'none') + ' | ring: ' + top(ring)
                            + ' | foreign: ' + (foreign.length ? foreign.join(', ') : 'none'))
                        shown++
                    })
                }
                out('under: ' + shown + ' starts' + (skipped ? ', ' + skipped + ' chunks unloaded' : ''))
            } else if (what == 'chunk') {
                // chunk <x> <z> (block coords) [dim]: ticket level, status and tickets (ServerChunkCache.getChunkDebugData)
                let a = arg.split(' '), dim = a[2] || 'minecraft:overworld', lv = null
                server.getAllLevels().forEach(l => { if (String(l.dimension) == dim) lv = l })
                let $ZQCP = Java.loadClass('net.minecraft.world.level.ChunkPos')
                out('chunk ' + a[0] + ' ' + a[1] + ': ' + lv.getChunkSource().getChunkDebugData(new $ZQCP(+a[0] >> 4, +a[1] >> 4)))
            } else if (what == 'biometag') {
                let reg = server.registryAccess().registryOrThrow($ZQReg.BIOME_REGISTRY)
                let tk = $ZQTagKey.create($ZQReg.BIOME_REGISTRY, new $ZQRL(arg)), ids = []
                reg.getTagOrEmpty(tk).forEach(h => ids.push(String(h.unwrapKey().get().location())))
                out('biometag ' + arg + ' (' + ids.length + '): ' + ids.join(' '))
            } else if (what == 'bytype') {
                // every recipe whose serializer id is arg: "bytype <serializer> <id> -> <result>", then the count
                let n = 0
                server.getRecipeManager().getRecipes().forEach(r => {
                    let type = '?'
                    try { type = String($ZQReg.RECIPE_SERIALIZER.getKey(r.getSerializer())) } catch (err) { }
                    if (type != arg) return
                    n++
                    let res = '?'
                    try { res = String(r.getResultItem()) } catch (err) { }
                    out('bytype ' + arg + ' ' + String(r.getId()) + ' -> ' + res)
                })
                out('bytype ' + arg + ': ' + n)
            } else if (what == 'rdetail') {
                // id contains arg: serializer, result, and each ingredient's first 3 items
                let n = 0
                server.getRecipeManager().getRecipes().forEach(r => {
                    let id = String(r.getId())
                    if (id.indexOf(arg) < 0 || n >= 40) return
                    n++
                    let type = '?', res = '?', ins = []
                    try { type = String($ZQReg.RECIPE_SERIALIZER.getKey(r.getSerializer())) } catch (err) { }
                    try { res = String(r.getResultItem()) } catch (err) { }
                    try {
                        r.getIngredients().forEach(g => {
                            let s = g.getStacks(), ids = []
                            for (let k = 0; k < Math.min(3, s.size()); k++) ids.push(String(s.toList().get(k).getId()))
                            ins.push(ids.join('/') + (s.size() > 3 ? '/+' + (s.size() - 3) : ''))
                        })
                    } catch (err) { ins.push('ingredients: ' + err) }
                    out(id + ' {' + type + '} -> ' + res + ' <- ' + ins.join(', '))
                    // extra lines (never "<id> {", so counts on the main line stay valid): every rolled result with
                    // count and chance (FD cutting ChanceResult, Create ProcessingOutput: both getRollableResults),
                    // and a sequenced assembly's starting ingredient and steps
                    let rolls = zqRolls(r)
                    if (rolls) out('rdetail+ ' + id + ' results: ' + rolls)
                    let start = null
                    try { start = zqIng(r.getIngredient()) } catch (err) { }
                    if (start != null) out('rdetail+ ' + id + ' start: ' + start)
                    // public Ingredient fields of recipes whose getIngredients is empty (CD Barbeque's skewering)
                    let fields = []
                    ;['tool', 'ingredient', 'side'].forEach(f => {
                        try {
                            let g = r[f]
                            if (g != null && g.getStacks) {
                                let c = r[f + 'Count']
                                fields.push(f + ' ' + (c != null ? c + 'x ' : '') + zqIng(g))
                            }
                        } catch (err) { }
                    })
                    if (fields.length) out('rdetail+ ' + id + ' fields: ' + fields.join(', '))
                    try {
                        let k = 0
                        r.getSequence().forEach(sr => {
                            let pr = sr.getRecipe(), st = '?', gs = []
                            try { st = String($ZQReg.RECIPE_SERIALIZER.getKey(pr.getSerializer())) } catch (err) { }
                            try { pr.getIngredients().forEach(g => gs.push(zqIng(g))) } catch (err) { }
                            out('rdetail+ ' + id + ' step ' + (k++) + ' {' + st + '} <- ' + gs.join(', '))
                        })
                    } catch (err) { }
                })
                out('rdetail matching ' + arg + ': ' + n)
            } else if (what == 'itemtag' || what == 'blocktag') {
                let reg = what == 'itemtag' ? $ZQReg.ITEM : $ZQReg.BLOCK
                let key = $ZQTagKey.create(reg.key(), new $ZQRL(arg))
                let ids = []
                reg.getTagOrEmpty(key).forEach(h => ids.push(String(reg.getKey(h.value()))))
                out(what + ' ' + arg + ' (' + ids.length + '): ' + ids.join(' '))
            } else if (what == 'tagsof') {
                let item = $ZQReg.ITEM.get(new $ZQRL(arg))
                let tags = []
                item.builtInRegistryHolder().tags().collect($ZQCollectors.toList()).forEach(t => tags.push(String(t.location())))
                out('item tags of ' + arg + ': ' + tags.join(' '))
                let block = $ZQReg.BLOCK.get(new $ZQRL(arg))
                let btags = []
                block.builtInRegistryHolder().tags().collect($ZQCollectors.toList()).forEach(t => btags.push(String(t.location())))
                out('block tags of ' + arg + ': ' + btags.join(' '))
            } else if (what == 'nbt') {
                // how a script sees an ItemStack's NBT (arg = an Item.of id, NBT from the fixed potion below)
                let st = Item.of(arg, '{Potion:"minecraft:water",CustomPotionColor:1}')
                out('typeof getTag ' + typeof st.getTag + ', typeof getItem ' + typeof st.getItem + ', typeof nbt ' + typeof st.nbt + ', nbt ' + st.nbt)
            } else if (what == 'states') {
                let n = $ZQBlock.BLOCK_STATE_REGISTRY.size()
                out('block states ' + n + ' of 1048576 (headroom ' + (1048576 - n) + ')')
            } else if (what == 'useitem') {
                // air use of the main-hand item by a player or bot (what the client sends after a block use PASSes;
                // perfBot's `pbot use` only does the block use)
                let p = server.getPlayerList().getPlayerByName(arg)
                if (p == null) { out('no player ' + arg) } else {
                    // main hand first, then the off hand when the main hand does not consume the action (the
                    // client's order: Minecraft.startUseItem tries each hand until one consumesAction)
                    let r = p.gameMode.useItem(p, p.getLevel(), p.getMainHandItem(), $ZQHand.MAIN_HAND)
                    out('useitem ' + arg + ' ' + r)
                    if (!r.consumesAction() && !p.getOffhandItem().isEmpty()) {
                        let r2 = p.gameMode.useItem(p, p.getLevel(), p.getOffhandItem(), $ZQHand.OFF_HAND)
                        out('useitem ' + arg + ' offhand ' + r2)
                    }
                }
            } else out('unknown query ' + what)
        } catch (err) { out('error: ' + err) }
        out('done')
    }
    ServerEvents.commandRegistry(e => {
        const { commands: C } = e
        e.register(C.literal('zq')
            .then(C.argument('what', $ZQArgs.word())
                .then(C.argument('arg', $ZQArgs.greedyString())
                    .executes(ctx => {
                        run(ctx.source.server, $ZQArgs.getString(ctx, 'what'), $ZQArgs.getString(ctx, 'arg'))
                        return 1
                    }))))
    })
})()
