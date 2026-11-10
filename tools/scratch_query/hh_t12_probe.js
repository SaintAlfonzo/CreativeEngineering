// Scratch-only probe for the Hearth and Harvest port, tranches 1 and 2 (scratch_queue.md, SQ-HH-1101 up; hearth_and_harvest.md
// 4.3, 4.4). Copy into a scratch server's kubejs/server_scripts as zz_hh_t12_probe.js, never into the pack. Needs no player
// except where a bot name is given (a perfBot bot; `pbot spawn`). A SEL is a player name, or a tag set with `Tags:["x"]`
// on a summoned entity (the first entity carrying it). Coordinates are integers in the overworld; load the chunk first.
//   /zhh1 ids                     kept item ids, dropped upstream ids, fluids, effects, entity types, sound (registries)
//   /zhh1 interact NAME TAG [sneak]   player interacts with the tagged entity (main hand), sneaking if asked; prints the result
//   /zhh1 fx SEL                  active effects of SEL ("id amp ticks")
//   /zhh1 dist SEL1 SEL2          distance between two entities
//   /zhh1 goals TAG               how many EffectAuras goals the tagged mob holds
//   /zhh1 drops X Y Z R           item entities within R of X Y Z, summed by id, then discarded
//   /zhh1 skip N                  advance the overworld game time by N ticks (scheduled ticks and game-time timers come due)
//   /zhh1 throw NAME TICKS        releases the bot's main-hand throwable after TICKS of charge
//   /zhh1 ate TAG                 Sheep.ate() on the tagged sheep (the grazing hook)
//   /zhh1 jump SEL                upward motion 0.42, then Forge's jump event; prints the vertical motion
//   /zhh1 vuln NAME on|off        bot damageable or invulnerable (perfBot bots start invulnerable)
//   /zhh1 melee NAME TAG          the bot attacks the tagged entity with its main-hand item
//   /zhh1 finish NAME             finishUsingItem on the main-hand item; prints the result stack
//   /zhh1 compost ID              ComposterBlock chance of the item
//   /zhh1 burn ID                 Forge burn time of the item
//   /zhh1 loot X Y Z TOOL ROLLS   the block at X Y Z rolled through Block.getDrops ROLLS times with TOOL (id or none); totals
//   /zhh1 dspeed X Y Z ID         destroy speed of item ID on the block at X Y Z
//   /zhh1 etag TAG / fluidtag TAG  entity types / fluids in a tag
//   /zhh1 mods ID                 main-hand attribute modifiers of the item
//   /zhh1 ench ID ENCH            canApplyAtEnchantingTable
//   /zhh1 remain ID DAMAGE        crafting remainder of the item at that damage
//   /zhh1 container RECIPE_ID     a Cooking Pot recipe's output container
//   /zhh1 pluck NAME X Y Z N      N fresh chickens, each plucked once by the sneaking bot; feathers and damaged chickens
//   /zhh1 litter X Y Z TYPE N [truffle]  N parent pairs bred through spawnChildFromBreeding; babies counted, all discarded
// Output: "[zhh1] ..." lines in logs/latest.log, then "[zhh1] done". Written 2026-11-08, not run yet: one live call each
// before a batch relies on it (scratch-test gotchas). No Rhino-only tricks: Java collections are copied first.
(() => {
    const KEPT = [
        'acacia_half_cabinet', 'baked_apple', 'birch_half_cabinet', 'biscuits_and_gravy', 'blue_mum',
        'blueberry_juice', 'blueberry_wine', 'brine_bottle', 'brine_bucket', 'butter', 'candy_corn',
        'charred_marshmallow_stick', 'cherry_juice', 'chocolate_milk_bottle', 'cider_donut', 'cooked_sausage',
        'corn_husk', 'corn_husk_bundle', 'crimson_fungus_crate', 'crimson_half_cabinet', 'croissant',
        'crow_feather', 'dark_oak_half_cabinet', 'diamond_cleaver', 'flint_cleaver', 'glazed_carrots',
        'glistering_melon_crate', 'glow_berry_juice', 'glow_berry_wine', 'goat_cheese_slice', 'goat_cheese_wheel',
        'goat_milk_bottle', 'goat_milk_bucket', 'golden_cleaver', 'grape_pie', 'grape_pie_slice', 'hay_rug',
        'hops', 'hops_bag', 'hops_salad', 'hot_dog', 'iron_cleaver', 'jungle_half_cabinet', 'lemon_wine',
        'light_blue_mum', 'macaroni_and_cheese', 'mangrove_half_cabinet', 'manure', 'manure_bag', 'manure_block',
        'manure_brick', 'manure_brick_slab', 'manure_brick_stairs', 'manure_brick_wall', 'manure_bricks',
        'melon_wine', 'moonshine', 'netherite_cleaver', 'oak_half_cabinet', 'orange_mum', 'peanut_butter_cookie',
        'peanut_butter_pie', 'peanut_butter_pie_slice', 'pickled_carrots', 'pickled_potatoes', 'pink_mum',
        'pitchfork', 'poisonous_potato_crate', 'polished_manure', 'polished_manure_slab', 'polished_manure_stairs',
        'polished_manure_wall', 'purple_mum', 'raisin_cookie', 'raisins', 'raspberry', 'raspberry_juice',
        'raspberry_pie', 'raspberry_pie_slice', 'raspberry_pips', 'raspberry_scone', 'raspberry_wine',
        'raw_sausage', 'red_mum', 'root_beer', 'rope_coil', 'rotten_tomato_crate', 'sap_bucket', 'spool',
        'spruce_half_cabinet', 'straw_rug', 'street_corn', 'sunflower_seeds', 'sweet_berry_juice',
        'sweet_berry_wine', 'tamale', 'trail_mix', 'tree_tapper', 'unripe_goat_cheese_wheel', 'waffle',
        'warped_fungus_crate', 'warped_half_cabinet', 'wet_manure_brick', 'white_mum', 'wild_hops', 'yellow_mum',
    ]
    const DROPPED = [
        'acacia_bottle_rack', 'apple_crate', 'apple_jam', 'bamboo_bottle_rack', 'bamboo_half_cabinet', 'batter',
        'beer', 'beer_aged', 'beer_fine', 'beer_reserve', 'birch_bottle_rack', 'blueberries', 'blueberry_crate',
        'blueberry_jam', 'blueberry_muffin', 'blueberry_pie', 'blueberry_pie_slice', 'blueberry_pips',
        'blueberry_wine_aged', 'blueberry_wine_fine', 'blueberry_wine_reserve', 'brown_mushroom_crate', 'caramel',
        'caramel_apple', 'carrot_cake', 'carrot_cake_slice', 'cask', 'charcoal_block', 'cheese_pizza',
        'cheese_pizza_slice', 'cheese_slice', 'cheese_wheel', 'cherry', 'cherry_bottle_rack', 'cherry_crate',
        'cherry_half_cabinet', 'cherry_jam', 'cherry_pie', 'cherry_pie_slice', 'cherry_wine', 'cherry_wine_aged',
        'cherry_wine_fine', 'cherry_wine_reserve', 'chicken_pot_pie', 'chicken_pot_pie_slice', 'chocolate_bar',
        'chocolate_cake', 'chocolate_cake_slice', 'cocoa_bean_bag', 'cooked_corn_on_the_cob', 'cooking_oil',
        'corn', 'corn_bread', 'corn_crate', 'corn_kernel_bag', 'corn_kernels', 'corn_meal', 'corn_stew', 'cotton',
        'cotton_bale', 'cotton_candy', 'cotton_seeds', 'counter', 'crate', 'crimson_bottle_rack', 'crow_spawn_egg',
        'dark_oak_bottle_rack', 'drawer', 'farmers_hat', 'feather_bag', 'fertilizer_bag', 'flour', 'flour_bag',
        'glow_berry_crate', 'glow_berry_jam', 'glow_berry_wine_aged', 'glow_berry_wine_fine',
        'glow_berry_wine_reserve', 'golden_apple_crate', 'golden_carrot_crate', 'grape_jam', 'green_grape_crate',
        'green_grape_juice', 'green_grapes', 'green_grape_wine', 'green_grape_wine_aged', 'green_grape_wine_fine',
        'green_grape_wine_reserve', 'gunpowder_bag', 'hard_cider', 'hard_cider_aged', 'hard_cider_fine',
        'hard_cider_reserve', 'horseshoe', 'jerky', 'jug', 'jungle_bottle_rack', 'keg', 'mangrove_bottle_rack',
        'maple_cookie', 'marshmallow_stick', 'mashed_potatoes', 'mead', 'mead_aged', 'mead_fine', 'mead_reserve',
        'meat_pizza', 'meat_pizza_slice', 'melon_jam', 'melon_wine_aged', 'melon_wine_fine', 'melon_wine_reserve',
        'mulch', 'nest', 'oak_bottle_rack', 'palm_bottle_rack', 'palm_half_cabinet', 'pancake', 'peanut',
        'peanut_butter', 'peanut_butter_and_jelly_sandwich', 'peanut_crate', 'pickled_beetroots',
        'pickled_beetroots_aged', 'pickled_beetroots_fine', 'pickled_beetroots_reserve', 'pickled_cabbage',
        'pickled_cabbage_aged', 'pickled_cabbage_fine', 'pickled_cabbage_reserve', 'pickled_carrots_aged',
        'pickled_carrots_fine', 'pickled_carrots_reserve', 'pickled_onions', 'pickled_onions_aged',
        'pickled_onions_fine', 'pickled_onions_reserve', 'pickled_potatoes_aged', 'pickled_potatoes_fine',
        'pickled_potatoes_reserve', 'pizza', 'pizza_slice', 'polished_salt_block', 'polished_salt_slab',
        'polished_salt_stairs', 'polished_salt_wall', 'popcorn', 'raspberry_crate', 'raspberry_jam',
        'raspberry_wine_aged', 'raspberry_wine_fine', 'raspberry_wine_reserve', 'raw_skewered_sausage',
        'red_grape_crate', 'red_grape_juice', 'red_grapes', 'red_grape_wine', 'red_grape_wine_aged',
        'red_grape_wine_fine', 'red_grape_wine_reserve', 'red_mushroom_crate', 'roasted_marshmallow_stick',
        'roasted_peanuts', 'root_beer_aged', 'root_beer_fine', 'root_beer_reserve', 'salt', 'salt_bag',
        'salt_block', 'salt_drip', 'salt_lamp', 'salt_slab', 'salt_stairs', 'salt_wall', 'scarecrow', 'seed_pouch',
        'skewered_sausage', 'smore', 'sprinkler', 'spruce_bottle_rack', 'stick_brush', 'stomping_basin',
        'sugar_bag', 'sugar_cane_bundle', 'sugar_cubes', 'sweet_berry_crate', 'sweet_berry_jam',
        'sweet_berry_wine_aged', 'sweet_berry_wine_fine', 'sweet_berry_wine_reserve', 'syrup_bottle', 'taco',
        'tortilla', 'trough', 'uncooked_corn_on_the_cob', 'unripe_cheese_wheel', 'veggie_pizza',
        'veggie_pizza_slice', 'warped_bottle_rack', 'wild_cotton', 'wild_green_grapes', 'wild_peanuts',
        'wild_red_grapes',
    ]
    const BLOCK_IDS = [
        'sap_cauldron', 'hops', 'raspberry_bush', 'potted_white_mum', 'potted_red_mum', 'potted_orange_mum',
        'potted_yellow_mum', 'potted_light_blue_mum', 'potted_blue_mum', 'potted_purple_mum', 'potted_pink_mum',
    ]
    const FLUIDS = [
        'goat_milk', 'brine', 'sap', 'blueberry_juice', 'raspberry_juice', 'cherry_juice', 'sweet_berry_juice',
        'glow_berry_juice', 'blueberry_wine', 'raspberry_wine', 'sweet_berry_wine', 'glow_berry_wine',
        'melon_wine', 'lemon_wine', 'root_beer', 'moonshine',
    ]

    const $HArgs = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $HReg = Java.loadClass('net.minecraft.core.Registry')
    const $HRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $HTagKey = Java.loadClass('net.minecraft.tags.TagKey')
    const $HArrayList = Java.loadClass('java.util.ArrayList')
    const $HHand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $HPos = Java.loadClass('net.minecraft.core.BlockPos')
    const $HVec = Java.loadClass('net.minecraft.world.phys.Vec3')
    const $HAABB = Java.loadClass('net.minecraft.world.phys.AABB')
    const $HItemEnt = Java.loadClass('net.minecraft.world.entity.item.ItemEntity')
    const $HForge = Java.loadClass('net.minecraftforge.common.ForgeHooks')
    const $HEnch = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $HComposter = Java.loadClass('net.minecraft.world.level.block.ComposterBlock')
    const $HLootB = Java.loadClass('net.minecraft.world.level.storage.loot.LootContext$Builder')
    const $HLootP = Java.loadClass('net.minecraft.world.level.storage.loot.parameters.LootContextParams')
    const $HSlot = Java.loadClass('net.minecraft.world.entity.EquipmentSlot')
    const $HAnimal = Java.loadClass('net.minecraft.world.entity.animal.Animal')
    function out(s) { console.info('[zhh1] ' + s) }
    function level(server) { return server.overworld() }
    function player(server, name) {
        let p = server.getPlayerList().getPlayerByName(name)
        if (p == null) out('no player ' + name)
        return p
    }
    function byTag(server, tag) {
        let found = null
        level(server).getAllEntities().forEach(e => { if (found == null && e.getTags().contains(tag)) found = e })
        if (found == null) out('no entity tagged ' + tag)
        return found
    }
    function sel(server, s) {
        let p = server.getPlayerList().getPlayerByName(s)
        return p != null ? p : byTag(server, s)
    }
    function idOf(st) { return String($HReg.ITEM.getKey(st.getItem())) }
    function run(server, line) {
        try {
            let a = String(line).trim().split(/\s+/), what = a[0], lv = level(server)
            if (what == 'ids') {
                let miss = [], here = []
                KEPT.forEach(k => { if (!$HReg.ITEM.containsKey(new $HRL('hearthandharvest', k))) miss.push(k) })
                DROPPED.forEach(k => { if ($HReg.ITEM.containsKey(new $HRL('hearthandharvest', k))) here.push(k) })
                out('ids kept ' + (KEPT.length - miss.length) + ' of ' + KEPT.length + ' missing: ' + (miss.length ? miss.join(' ') : 'none'))
                out('ids dropped checked ' + DROPPED.length + ' present: ' + (here.length ? here.join(' ') : 'none'))
                let b = [], f = [], fx = [], e = []
                BLOCK_IDS.forEach(k => { if (!$HReg.BLOCK.containsKey(new $HRL('hearthandharvest', k))) b.push(k) })
                FLUIDS.forEach(k => {
                    if (!$HReg.FLUID.containsKey(new $HRL('hearthandharvest', k))) f.push(k)
                    if (!$HReg.FLUID.containsKey(new $HRL('hearthandharvest', 'flowing_' + k))) f.push('flowing_' + k)
                })
                ;['pungent', 'tempting', 'prickly', 'clarity', 'pinned'].forEach(k => { if (!$HReg.MOB_EFFECT.containsKey(new $HRL('hearthandharvest', k))) fx.push(k) })
                ;['thrown_cleaver', 'thrown_pitchfork', 'manure_projectile'].forEach(k => { if (!$HReg.ENTITY_TYPE.containsKey(new $HRL('hearthandharvest', k))) e.push(k) })
                out('ids blocks ' + BLOCK_IDS.length + ' missing: ' + (b.length ? b.join(' ') : 'none'))
                out('ids fluids ' + FLUIDS.length + ' (and flowing) missing: ' + (f.length ? f.join(' ') : 'none'))
                out('ids effects missing: ' + (fx.length ? fx.join(' ') : 'none') + '; entities missing: ' + (e.length ? e.join(' ') : 'none'))
                out('ids sound hearthandharvest:entities.fart ' + $HReg.SOUND_EVENT.containsKey(new $HRL('hearthandharvest', 'entities.fart')))
            } else if (what == 'interact') {
                let p = player(server, a[1]), e = byTag(server, a[2])
                if (p != null && e != null) {
                    if (a[3] == 'sneak') p.setShiftKeyDown(true)
                    let r = p.interactOn(e, $HHand.MAIN_HAND)
                    if (a[3] == 'sneak') p.setShiftKeyDown(false)
                    out('interact ' + a[1] + ' ' + a[2] + ': ' + r + ', hand ' + idOf(p.getMainHandItem()) + ' x' + p.getMainHandItem().getCount())
                }
            } else if (what == 'fx') {
                let e = sel(server, a[1]), o = []
                if (e != null) {
                    new $HArrayList(e.getActiveEffects()).forEach(i => o.push(String($HReg.MOB_EFFECT.getKey(i.getEffect())) + ' ' + i.getAmplifier() + ' ' + i.getDuration()))
                    out('fx ' + a[1] + ': ' + (o.length ? o.join(', ') : 'none'))
                }
            } else if (what == 'dist') {
                let p = sel(server, a[1]), q = sel(server, a[2])
                if (p != null && q != null) out('dist ' + a[1] + ' ' + a[2] + ': ' + Math.sqrt(Math.pow(p.getX()-q.getX(),2)+Math.pow(p.getY()-q.getY(),2)+Math.pow(p.getZ()-q.getZ(),2)).toFixed(2))
            } else if (what == 'goals') {
                let e = byTag(server, a[1]), n = 0
                if (e != null) {
                    new $HArrayList(e.goalSelector.getAvailableGoals()).forEach(w => { if (String(w.getGoal()).indexOf('AuraGoal') >= 0) n++ })
                    out('goals ' + a[1] + ': ' + n)
                }
            } else if (what == 'drops') {
                let x = parseInt(a[1]), y = parseInt(a[2]), z = parseInt(a[3]), r = parseInt(a[4])
                let box = new $HAABB(x - r, y - r, z - r, x + r + 1, y + r + 1, z + r + 1), sum = {}
                lv.getEntitiesOfClass($HItemEnt, box).forEach(e => {
                    let st = e.getItem(), id = idOf(st)
                    sum[id] = (sum[id] || 0) + st.getCount()
                    e.discard()
                })
                let keys = Object.keys(sum).sort()
                out('drops ' + x + ' ' + y + ' ' + z + ': ' + (keys.length ? keys.map(k => k + ' x' + sum[k]).join(', ') : 'none'))
            } else if (what == 'skip') {
                let n = parseInt(a[1]), t0 = lv.getLevelData().getGameTime()
                lv.getLevelData().setGameTime(t0 + n)
                out('skip ' + n + ': game time ' + t0 + ' -> ' + lv.getLevelData().getGameTime())
            } else if (what == 'throw') {
                let p = player(server, a[1])
                if (p != null) {
                    let st = p.getMainHandItem(), id = idOf(st), n = parseInt(a[2])
                    st.getItem().releaseUsing(st, lv, p, 72000 - n)
                    out('throw ' + a[1] + ' charge ' + n + ': ' + id)
                }
            } else if (what == 'ate') {
                let e = byTag(server, a[1])
                if (e != null) { e.ate(); out('ate ' + a[1]) }
            } else if (what == 'jump') {
                let e = sel(server, a[1])
                if (e != null) {
                    let m = e.getDeltaMovement()
                    e.setDeltaMovement(new (Java.loadClass('net.minecraft.world.phys.Vec3'))(m.x(), 0.42, m.z()))
                    $HForge.onLivingJump(e)
                    out('jump ' + a[1] + ': vy ' + e.getDeltaMovement().y().toFixed(3))
                }
            } else if (what == 'vuln') {
                let p = player(server, a[1])
                if (p != null) { p.setInvulnerable(a[2] != 'on'); out('vuln ' + a[1] + ': damageable ' + (a[2] == 'on')) }
            } else if (what == 'melee') {
                let p = player(server, a[1]), e = byTag(server, a[2])
                if (p != null && e != null) {
                    p.resetAttackStrengthTicker()
                    p.attack(e)
                    out('melee ' + a[1] + ' ' + a[2] + ': target health ' + e.getHealth())
                }
            } else if (what == 'finish') {
                let p = player(server, a[1])
                if (p != null) {
                    let st = p.getMainHandItem(), id = idOf(st), r = st.finishUsingItem(lv, p)
                    p.setItemInHand($HHand.MAIN_HAND, r)
                    out('finish ' + a[1] + ' ' + id + ' -> ' + idOf(r) + ' x' + r.getCount())
                }
            } else if (what == 'compost') {
                out('compost ' + a[1] + ': ' + $HComposter.COMPOSTABLES.getFloat(Item.of(a[1]).getItem()))
            } else if (what == 'burn') {
                out('burn ' + a[1] + ': ' + $HForge.getBurnTime(Item.of(a[1]), null))
            } else if (what == 'loot') {
                let x = parseInt(a[1]), y = parseInt(a[2]), z = parseInt(a[3]), rolls = parseInt(a[5]), sum = {}
                let pos = new $HPos(x, y, z), st = lv.getBlockState(pos)
                let tool = a[4] == 'none' ? Item.of('minecraft:air') : Item.of(a[4])
                for (let k = 0; k < rolls; k++) {
                    let b = new $HLootB(lv).withRandom(lv.random).withParameter($HLootP.ORIGIN, new $HVec(x + 0.5, y + 0.5, z + 0.5)).withParameter($HLootP.TOOL, tool)
                    st.getDrops(b).forEach(s => { let id = idOf(s); sum[id] = (sum[id] || 0) + s.getCount() })
                }
                let keys = Object.keys(sum).sort()
                out('loot ' + String(st) + ' tool ' + a[4] + ' rolls ' + rolls + ': ' + (keys.length ? keys.map(k => k + ' x' + sum[k]).join(', ') : 'none'))
            } else if (what == 'dspeed') {
                let pos = new $HPos(parseInt(a[1]), parseInt(a[2]), parseInt(a[3]))
                out('dspeed ' + a[4] + ' on ' + String(lv.getBlockState(pos)) + ': ' + Item.of(a[4]).getDestroySpeed(lv.getBlockState(pos)))
            } else if (what == 'etag' || what == 'fluidtag') {
                let reg = what == 'etag' ? $HReg.ENTITY_TYPE : $HReg.FLUID
                let key = $HTagKey.create(reg.key(), new $HRL(a[1])), ids = []
                reg.getTagOrEmpty(key).forEach(h => ids.push(String(reg.getKey(h.value()))))
                out(what + ' ' + a[1] + ' (' + ids.length + '): ' + ids.join(' '))
            } else if (what == 'mods') {
                let o = []
                new $HArrayList(Item.of(a[1]).getAttributeModifiers($HSlot.MAINHAND).entries()).forEach(en => {
                    let m = en.getValue()
                    o.push(String($HReg.ATTRIBUTE.getKey(en.getKey())) + ' ' + m.getOperation() + ' ' + m.getAmount())
                })
                out('mods ' + a[1] + ': ' + o.join('; '))
            } else if (what == 'ench') {
                let en = $HEnch.ENCHANTMENTS.getValue(new $HRL(a[2]))
                out('ench ' + a[1] + ' ' + a[2] + ': ' + Item.of(a[1]).canApplyAtEnchantingTable(en))
            } else if (what == 'remain') {
                let s = Item.of(a[1]), d = parseInt(a[2])
                s.setDamageValue(d)
                let r = s.getCraftingRemainingItem()
                out('remain ' + a[1] + ' damage ' + d + ': ' + (r.isEmpty() ? 'empty' : idOf(r) + ' damage ' + r.getDamageValue()))
            } else if (what == 'container') {
                let want = String(a[1]), res = 'recipe not found'
                server.getRecipeManager().getRecipes().forEach(r => {
                    if (String(r.getId()) != want) return
                    try { res = idOf(r.getOutputContainer()) } catch (err) { res = 'no container method' }
                })
                out('container ' + want + ': ' + res)
            } else if (what == 'pluck') {
                let p = player(server, a[1]), x = parseInt(a[2]), y = parseInt(a[3]), z = parseInt(a[4]), n = parseInt(a[5])
                if (p != null) {
                    let type = $HEnch.ENTITY_TYPES.getValue(new $HRL('minecraft:chicken')), feathers = 0, damaged = 0
                    let box = new $HAABB(x - 3, y - 2, z - 3, x + 4, y + 3, z + 4)
                    p.setShiftKeyDown(true)
                    for (let k = 0; k < n; k++) {
                        let c = type.create(lv)
                        c.setPos(x + 0.5, y, z + 0.5)
                        lv.addFreshEntity(c)
                        p.interactOn(c, $HHand.MAIN_HAND)
                        if (c.getHealth() < 4) damaged++
                        c.discard()
                        lv.getEntitiesOfClass($HItemEnt, box).forEach(e => { if (idOf(e.getItem()) == 'minecraft:feather') feathers += e.getItem().getCount(); e.discard() })
                    }
                    p.setShiftKeyDown(false)
                    out('pluck ' + n + ': feathers ' + feathers + ', damaged ' + damaged)
                }
            } else if (what == 'litter') {
                let x = parseInt(a[1]), y = parseInt(a[2]), z = parseInt(a[3]), id = a[4], n = parseInt(a[5]), truffle = a[6] == 'truffle'
                let type = $HEnch.ENTITY_TYPES.getValue(new $HRL(id)), babies = 0
                let box = new $HAABB(x - 4, y - 2, z - 4, x + 5, y + 4, z + 5)
                for (let k = 0; k < n; k++) {
                    let pa = type.create(lv), pb = type.create(lv)
                    pa.setPos(x + 0.5, y, z + 0.5); pb.setPos(x + 0.5, y, z + 0.5)
                    lv.addFreshEntity(pa); lv.addFreshEntity(pb)
                    if (truffle) pa.getPersistentData().putBoolean('FD_TruffleBred', true)
                    pa.spawnChildFromBreeding(lv, pb)
                    lv.getEntitiesOfClass($HAnimal, box).forEach(e => { if (e.isBaby()) babies++; e.discard() })
                }
                out('litter ' + id + ' x' + n + (truffle ? ' truffle' : '') + ': babies ' + babies + ', extra ' + (babies - n))
            } else out('unknown ' + what)
        } catch (err) { out('error: ' + err) }
        out('done')
    }
    ServerEvents.commandRegistry(e => {
        const { commands: C } = e
        e.register(C.literal('zhh1').then(C.argument('line', $HArgs.greedyString()).executes(ctx => {
            run(ctx.source.server, $HArgs.getString(ctx, 'line'))
            return 1
        })))
    })
})()
