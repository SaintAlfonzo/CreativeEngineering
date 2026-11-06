// Older misc builds probe (scratch queue SQ-OM: alcohol ports, feature ports, baby mobs, BlockRunner leaf piles,
// campfire rain, JEED providers and the world serverconfig payload, built 2026-10-19 to 2026-10-21 and never checked
// on a server). Scratch-only: copy into a scratch server's kubejs/server_scripts as zz_om_probe.js
// (tools/creative_labels_test/scratch_server.sh), never into the pack. Run /omprobe <part> or /omprobe all; results are
// "[omprobe] PASS|FAIL|INFO ..." lines in logs/latest.log, ending "[omprobe] done: N pass, M fail".
// Blocks go in a strip at x 0-215, y -60, z 92-100 (superflat ground is grass at -61): `forceload add 0 80 223 111`
// first.
//
//  keg        Brewin' keg slot rules from every face, take-only output / leftover slots, an 8-slot (-02) keg's NBT
//             loads into 9 slots (alcohol.md 10.16, AL-81 to AL-84 logic; TODO alcohol/brewinport #6, #7)
//  coaster    CoasterBlockEntity.accepts and a fake-player click: tagged items only (alcohol.md 12, AL-91 logic;
//             brewinport #8)
//  compost    the port's 4 compost chances and BlockRunner wood tier for heating cask / ice crate (alcohol.md 10.16;
//             brewinport #4)
//  drinks     food values of the 44 alcoholic drinks (global.alcohol) and a fake player drinking Red Wine (alcohol.md 11,
//             AL-85 to AL-87 logic; alcohol/pack #1)
//  setup      builds the wine storage spots and the campfires, starts rain; then wait 30 s and run `wine`, `campfire`
//  wine       WineYears.storageKind and the rate for sunny / cold / warm x open / cellar (alcohol.md 10.13, VB-76
//             logic; alcohol/pack #2)
//  campfire   after `setup`: plain campfire on stone out in rain, on coal / charcoal blocks still lit, soul campfire
//             lit; Unlit Campfire burn time from the world serverconfig (gameplay/misc #2)
//  cask       Vinery cask hopper rules (alcohol.md 10.19, VB-93 logic; vineryport #7)
//  press      Vinery apple press hopper rules (alcohol.md 10.19, VB-94 logic; vineryport #7)
//  houses X Z after `place structure minecraft:village_<biome> X -60 Z`: Vinery cask / press block entities within
//             6 chunks (alcohol.md 13, VB-97 server half; vineryport #14). Not in `all`
//  leafslow   IW leaf piles slow from [leaf_piles] slowFromLayers up (mob_block_tweaks.md MT-25 logic;
//             gameplay/blockrunner #3)
//  raid       a raider added to a raid wave gets hidden Glowing (feature_porting.md 2.4, FP-11 logic)
//  air        Bottled Air: glass bottle under water refills air, lava bucket in the other hand evaporates a water
//             bottle, no drinking a water bottle under water (FP-13 logic)
//  tier1      tier 1 port recipes: counts, outputs, generators gone, obsidian kept, copper ore drops 1 raw copper
//             (feature_porting.md 1.1, FP-09 recipe half, FP-15; ports/featureporting #2)
//  baby       baby squid / glow squid / dolphin: no drops, dolphin fish feeding, natural spawn share
//             (baby_mobs.md, BM-04 to BM-07 server half; ports/babymobport #1); then `babyxp` 2 s later:
//             no experience orbs from the babies (the adult control leaves some). Not in `all`
//  jeed       jeed:effect_provider recipes: no empty provider, Vinery and Xaero providers, cherry cream soda
//             (effects.md 3, EF-15 server half; ui/effects #4)
//  serverconfig  Unlit Campfire values read from the world serverconfig (deploy.md DP-08 second half;
//             pack/release #6). Not in `all`
//
// Rhino rules (memory rhino-fieldandmethods-gotcha): no getType() on every recipe, Ingredient.getStacks() not
// getItems(), config Booleans compared as text, no same-arity overloads with unrelated types (WineYears.storageRate is
// one: the rate is computed here from VintageClock), let not const inside loops.
(() => {
    const $OMReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $OMRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $OMStack = Java.loadClass('net.minecraft.world.item.ItemStack')
    const $OMPos = Java.loadClass('net.minecraft.core.BlockPos')
    const $OMDir = Java.loadClass('net.minecraft.core.Direction')
    const $OMCaps = Java.loadClass('net.minecraftforge.common.capabilities.ForgeCapabilities')
    const $OMTagParser = Java.loadClass('net.minecraft.nbt.TagParser')
    const $OMFake = Java.loadClass('net.minecraftforge.common.util.FakePlayerFactory')
    const $OMProfile = Java.loadClass('com.mojang.authlib.GameProfile')
    const $OMUUID = Java.loadClass('java.util.UUID')
    const $OMHand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $OMVec3 = Java.loadClass('net.minecraft.world.phys.Vec3')
    const $OMHit = Java.loadClass('net.minecraft.world.phys.BlockHitResult')
    const $OMAABB = Java.loadClass('net.minecraft.world.phys.AABB')
    const $OMItemEntity = Java.loadClass('net.minecraft.world.entity.item.ItemEntity')
    const $OMOrb = Java.loadClass('net.minecraft.world.entity.ExperienceOrb')
    const $OMEntityType = Java.loadClass('net.minecraft.world.entity.EntityType')
    const $OMSpawnType = Java.loadClass('net.minecraft.world.entity.MobSpawnType')
    const $OMDamage = Java.loadClass('net.minecraft.world.damagesource.DamageSource')
    const $OMBus = Java.loadClass('net.minecraftforge.common.MinecraftForge')
    const $OMEvents = Java.loadClass('net.minecraftforge.event.ForgeEventFactory')
    const $OMCE = Java.loadClass('com.SaintAlfonzo.creativecommon.CommonConfig')

    const i = global.item
    const Y = -60
    const Z = 96

    let pass = 0, fail = 0
    let SERVER = null, RM = null
    function log(s) { console.info('[omprobe] ' + s) }
    function info(s) { log('INFO ' + s) }
    function check(ok, what) { if (ok) pass++; else fail++; log((ok ? 'PASS ' : 'FAIL ') + what) }
    function rl(id) { return new $OMRL(id) }
    function item(id) { return $OMReg.ITEMS.getValue(rl(id)) }
    function exists(id) { return $OMReg.ITEMS.containsKey(rl(id)) }
    function stack(id, n) { return new $OMStack(item(id), n || 1) }
    function idOf(st) { return String($OMReg.ITEMS.getKey(st.getItem())) }
    function effect(id) { return $OMReg.MOB_EFFECTS.getValue(rl(id)) }
    function pos(x, y, z) { return new $OMPos(x, y, z) }
    function cmd(s) { SERVER.runCommandSilent(s) }
    // 'minecraft:campfire[lit=true]' through KubeJS's block container (setValue with JS values finds no overload)
    function setSpec(level, p, spec) {
        let k = spec.indexOf('['), props = {}
        if (k >= 0) spec.substring(k + 1, spec.length - 1).split(',').forEach(kv => { let a = kv.split('='); props[a[0]] = a[1] })
        level.getBlock(p).set(k >= 0 ? spec.substring(0, k) : spec, props)
    }
    function blockTagged(state, tag) { return state.getTags().anyMatch(t => String(t.location()) == tag) }
    function stacksOf(ing) { let out = [], it = ing.getStacks().iterator(); while (it.hasNext()) out.push(it.next()); return out }
    function fake(level, name) {
        let p = $OMFake.get(level, new $OMProfile($OMUUID.randomUUID(), name))
        p.getInventory().clearContent()
        p.setShiftKeyDown(false)
        return p
    }
    function recipesOf(typeId) {
        let t = $OMReg.RECIPE_TYPES.getValue(rl(typeId))
        let out = []
        if (t == null) return out
        RM.getAllRecipesFor(t).forEach(r => out.push(r))
        return out
    }
    function itemsIn(level, x0, y0, z0, x1, y1, z1) { return level.getEntitiesOfClass($OMItemEntity, new $OMAABB(x0, y0, z0, x1, y1, z1)) }
    function clearItems(level, x0, z0) { itemsIn(level, x0 - 4, Y - 4, z0 - 4, x0 + 5, Y + 6, z0 + 5).forEach(e => e.discard()) }

    // ---------------------------------------------------------------- keg (brewinport #6, #7)
    function keg(level) {
        const $Keg = Java.loadClass('umpaz.brewinandchewin.common.block.entity.KegBlockEntity')
        const $BnCConfig = Java.loadClass('umpaz.brewinandchewin.common.BnCConfiguration')
        let kc = $BnCConfig.COMMON_CONFIG.get().keg()
        info('keg config remainderBuffer=' + kc.remainderBuffer() + ' stallWhenFull=' + kc.stallWhenFull())
        let p = pos(0, Y, Z)
        setSpec(level, p, 'brewinandchewin:keg')
        let be = level.getBlockEntity(p)
        check(be != null && String(be.getClass().getName()).indexOf('KegBlockEntity') >= 0, 'keg block entity placed')
        let side = d => be.getCapability($OMCaps.ITEM_HANDLER, d).orElse(null)
        let N = side($OMDir.NORTH), U = side($OMDir.UP), D = side($OMDir.DOWN)
        check(N.getSlots() == 9, 'keg has 9 slots (' + N.getSlots() + ')')
        let takes = (h, slot, id) => h.insertItem(slot, stack(id, 1), true).isEmpty()
        check(be.isFermentingIngredient(stack('minecraft:wheat')), 'wheat is a fermenting ingredient')
        check(takes(N, 0, 'minecraft:wheat') && takes(D, 1, 'minecraft:wheat') && takes(U, 2, 'minecraft:wheat'),
            'wheat into the grid from side, bottom and top')
        check(!takes(N, 0, 'minecraft:dirt') && !takes(U, 0, 'minecraft:dirt'), 'dirt refused by the grid')
        check(takes(N, $Keg.CONTAINER_SLOT, 'brewinandchewin:tankard'), 'tankard into the container slot from a side')
        check(!takes(N, $Keg.CONTAINER_SLOT, 'minecraft:dirt'), 'dirt refused by the container slot')
        check(be.isFluidSource(stack('minecraft:honey_bottle')), 'honey bottle is a fluid source')
        check(!takes(N, 0, 'minecraft:honey_bottle') && !takes(D, 0, 'minecraft:honey_bottle'),
            'honey bottle skips the grid from side and bottom')
        check(takes(N, $Keg.FLUID_IN_SLOT, 'minecraft:honey_bottle') && takes(D, $Keg.FLUID_IN_SLOT, 'minecraft:honey_bottle'),
            'honey bottle into the fluid slot from side and bottom')
        check(!takes(N, $Keg.CONTAINER_SLOT, 'minecraft:honey_bottle'), 'honey bottle refused by the container slot')
        if (be.isFermentingIngredient(stack('minecraft:honey_bottle'))) check(takes(U, 0, 'minecraft:honey_bottle'), 'honey bottle into the grid from the top')
        else info('honey bottle is no fermenting ingredient here; top-grid case skipped')
        let noIn = [$Keg.OUTPUT_SLOT, $Keg.FLUID_OUT_SLOT, $Keg.REMAINDER_SLOT].every(s => !takes(N, s, 'minecraft:glass_bottle') && !takes(U, s, 'minecraft:glass_bottle') && !takes(D, s, 'minecraft:glass_bottle'))
        check(noIn, 'output, empty-container and leftover slots take nothing from any face')

        // an 8-slot keg saved by -02 (before the leftover slot): every item stays (AL-81)
        be.load($OMTagParser.parseTag('{Inventory:{Size:8,Items:[{Slot:0b,id:"minecraft:wheat",Count:3b},' +
            '{Slot:4b,id:"brewinandchewin:tankard",Count:2b},{Slot:5b,id:"brewinandchewin:beer",Count:1b},' +
            '{Slot:7b,id:"minecraft:glass_bottle",Count:4b}]}}'))
        let inv = be.getInventory()
        let got = [0, 4, 5, 7].map(s => idOf(inv.getStackInSlot(s)) + 'x' + inv.getStackInSlot(s).getCount())
        check(inv.getSlotCount() == 9 && got.join(' ') == 'minecraft:wheatx3 brewinandchewin:tankardx2 brewinandchewin:beerx1 minecraft:glass_bottlex4',
            '8-slot keg NBT loads into 9 slots: ' + got.join(' '))
        be.load($OMTagParser.parseTag('{BnCInventoryVersion:2,Inventory:{Size:9,Items:[{Slot:0b,id:"minecraft:wheat",Count:1b},' +
            '{Slot:5b,id:"brewinandchewin:beer",Count:1b},{Slot:7b,id:"minecraft:glass_bottle",Count:1b},{Slot:8b,id:"minecraft:bowl",Count:2b}]}}'))
        N = side($OMDir.NORTH); U = side($OMDir.UP); D = side($OMDir.DOWN)
        let pulls = (h, s) => !h.extractItem(s, 1, true).isEmpty()
        check(pulls(D, $Keg.OUTPUT_SLOT) && pulls(D, $Keg.FLUID_OUT_SLOT) && pulls(D, $Keg.REMAINDER_SLOT) && pulls(N, $Keg.REMAINDER_SLOT),
            'bottom and sides pull output, empties and leftovers (AL-84)')
        check(!pulls(D, 0) && pulls(U, 0) && !pulls(U, $Keg.OUTPUT_SLOT), 'top pulls only ingredients; bottom no ingredients')
        level.setBlockAndUpdate(p, Java.loadClass('net.minecraft.world.level.block.Blocks').AIR.defaultBlockState())
    }

    // ---------------------------------------------------------------- coaster (brewinport #8)
    function coaster(level) {
        const $Coaster = Java.loadClass('umpaz.brewinandchewin.common.block.entity.CoasterBlockEntity')
        const $BnCConfig = Java.loadClass('umpaz.brewinandchewin.common.BnCConfiguration')
        info('coaster taggedItemsOnly=' + $BnCConfig.COMMON_CONFIG.get().coaster().taggedItemsOnly())
        let tag = Ingredient.of('#brewinandchewin:placeable_on_coaster')
        let n = stacksOf(tag).length
        check(n >= 100, '#brewinandchewin:placeable_on_coaster holds ' + n + ' items (writeup: about 152 with the pack tag)')
        ;[['brewinandchewin:beer', true], ['minecraft:apple', true], ['farmersdelight:hot_cocoa', true],
          ['vinery:red_wine', false], ['farmersrespite:green_tea', false], ['minecraft:stone', false]].forEach(r => {
            if (!exists(r[0])) { info(r[0] + ' not registered, skipped'); return }
            check($Coaster.accepts(stack(r[0])) == r[1], r[0] + (r[1] ? ' goes on a coaster' : ' refused by a coaster'))
        })
        // fake-player clicks on a placed coaster: stone stays in hand, beer goes on
        let p = pos(8, Y, Z)
        setSpec(level, p, 'brewinandchewin:coaster')
        let pl = fake(level, 'omprobe_coaster')
        pl.setPos(8.5, Y, Z - 1.5)
        let click = id => {
            pl.setItemInHand($OMHand.MAIN_HAND, stack(id, 4))
            let hit = new $OMHit(new $OMVec3(8.5, Y + 0.05, Z + 0.5), $OMDir.UP, p, false)
            pl.gameMode.useItemOn(pl, level, pl.getMainHandItem(), $OMHand.MAIN_HAND, hit)
        }
        let held = () => { let be = level.getBlockEntity(p); let c = 0; if (be != null) be.getItems().forEach(s => { if (!s.isEmpty()) c++ }); return c }
        click('minecraft:stone')
        check(held() == 0, 'click with stone: coaster still empty (' + held() + ')')
        click('brewinandchewin:beer')
        check(held() == 1, 'click with beer: 1 item on the coaster (' + held() + ')')
        level.setBlockAndUpdate(p, Java.loadClass('net.minecraft.world.level.block.Blocks').AIR.defaultBlockState())
        clearItems(level, 8, Z)
    }

    // ---------------------------------------------------------------- compost + BlockRunner (brewinport #4)
    function compost(level) {
        const $Composter = Java.loadClass('net.minecraft.world.level.block.ComposterBlock')
        ;[['kimchi', 0.5], ['pickled_pickles', 0.5], ['quiche', 0.85], ['quiche_slice', 0.85]].forEach(r => {
            let id = i[r[0]]
            let c = $Composter.COMPOSTABLES.getFloat(item(id))
            check(Math.abs(c - r[1]) < 0.001, id + ' compost chance ' + c + ' (want ' + r[1] + ')')
        })
        ;[i.heating_cask, i.ice_crate].forEach(id => {
            let st = $OMReg.BLOCKS.getValue(rl(id)).defaultBlockState()
            check(blockTagged(st, 'blockrunner:surface/wood'), id + ' in blockrunner:surface/wood')
        })
        let co = $OMReg.BLOCKS.getValue(rl(i.coaster)).defaultBlockState()
        check(!co.getTags().anyMatch(t => String(t.location()).indexOf('blockrunner:surface/') == 0), i.coaster + ' in no BlockRunner surface tag')
    }

    // ---------------------------------------------------------------- drinks (alcohol/pack #1)
    function drinks(level) {
        const A = global.alcohol
        if (!A) { check(false, 'global.alcohol missing (startup alcohol_drinks.js not run)'); return }
        let INTOX = effect('brewinandchewin:intoxication'), TIPSY = effect('brewinandchewin:tipsy'), NAUSEA = effect('minecraft:nausea')
        let ok = 0, bad = []
        A.drinks.forEach(key => {
            let id = i[key]
            if (typeof id !== 'string' || !exists(id)) { bad.push(key + ' (no item)'); return }
            let f = item(id).getFoodProperties()
            if (f == null) { bad.push(id + ' (not food)'); return }
            let intox = null, nausea = null, tipsy = false
            f.getEffects().forEach(pair => {
                let inst = pair.getFirst()
                if (inst.getEffect().equals(INTOX)) intox = inst
                if (inst.getEffect().equals(NAUSEA)) nausea = inst
                if (inst.getEffect().equals(TIPSY)) tipsy = true
            })
            let drunk = tipsy || A.ownDrunkEffect.indexOf(key) >= 0
            let wantNausea = drunk ? -1 : (A.mixed.indexOf(key) >= 0 ? A.mixedNauseaTicks : A.nauseaTicks)
            let why = []
            if (f.getNutrition() != A.hunger) why.push('hunger ' + f.getNutrition())
            if (f.getSaturationModifier() != 0) why.push('saturation ' + f.getSaturationModifier())
            if (!f.canAlwaysEat()) why.push('not alwaysEat')
            if (intox == null) why.push('no Intoxication')
            else if (intox.isVisible()) why.push('Intoxication visible')
            if ((nausea == null ? -1 : nausea.getDuration()) != wantNausea) why.push('nausea ' + (nausea == null ? 'none' : nausea.getDuration()) + ' want ' + wantNausea)
            if (why.length) bad.push(id + ' (' + why.join(', ') + ')'); else ok++
        })
        check(ok == A.drinks.length && A.drinks.length == 44, 'drink food values: ' + ok + ' of ' + A.drinks.length + ' right' + (bad.length ? '; wrong: ' + bad.join('; ') : ''))

        // a fake player in survival drinks Red Wine (Vinery DrinkBlockItem eats it)
        let pl = fake(level, 'omprobe_wine')
        pl.removeAllEffects()
        pl.getFoodData().setFoodLevel(10)
        pl.getFoodData().setSaturation(0)
        let wine = stack(i.red_wine || 'vinery:red_wine', 1)
        pl.setItemInHand($OMHand.MAIN_HAND, wine)
        let copy = wine.copy()
        let res = wine.finishUsingItem(level, pl)
        res = $OMEvents.onItemUseFinish(pl, copy, 0, res)
        let nz = pl.getEffect(NAUSEA), ix = pl.getEffect(INTOX)
        check(pl.getFoodData().getFoodLevel() == 11 && pl.getFoodData().getSaturationLevel() == 0,
            'Red Wine: hunger 10 -> ' + pl.getFoodData().getFoodLevel() + ', saturation ' + pl.getFoodData().getSaturationLevel())
        check(ix != null && !ix.isVisible() && nz != null && nz.getDuration() == A.nauseaTicks,
            'Red Wine gives hidden Intoxication (' + (ix == null ? 'none' : ix.getDuration()) + ') and Nausea ' + (nz == null ? 'none' : nz.getDuration()))
        info('Red Wine returns ' + idOf(res) + ' x' + res.getCount() + '; effects ' + pl.getActiveEffects().size())
    }

    // ---------------------------------------------------------------- setup for wine + campfire (two-step parts)
    const WINE_SPOTS = [
        // x, cellar, climate block placed east of the spot, expected kind (CELLAR 1 | COLD 2 | WARM 4)
        [40, false, null, 0], [48, false, 'cold', 2], [56, false, 'warm', 4],
        [72, true, null, 1], [84, true, 'cold', 3], [96, true, 'warm', 5]]
    const CAMPFIRES = [
        // x, campfire spec, block below, expect lit after rain
        [150, 'minecraft:campfire[lit=true]', 'minecraft:stone', false],
        [154, 'minecraft:campfire[lit=true]', 'minecraft:coal_block', true],
        [158, 'minecraft:campfire[lit=true]', 'quark:charcoal_block', true],
        [162, 'minecraft:soul_campfire[lit=true]', 'minecraft:stone', true]]
    function heatBlock(level) {
        let cands = ['minecraft:magma_block', 'minecraft:campfire']
        for (let k = 0; k < cands.length; k++) {
            if (blockTagged($OMReg.BLOCKS.getValue(rl(cands[k])).defaultBlockState(), 'brewinandchewin:heat_sources')) return cands[k]
        }
        return null
    }
    function setup(level) {
        let heat = heatBlock(level)
        info('warm spots use ' + heat)
        WINE_SPOTS.forEach(s => {
            let x = s[0]
            if (s[1]) {
                cmd('fill ' + (x - 2) + ' ' + (Y - 1) + ' ' + (Z - 2) + ' ' + (x + 2) + ' ' + (Y + 3) + ' ' + (Z + 2) + ' minecraft:stone hollow')
            }
            if (s[2] == 'cold') setSpec(level, pos(x + 1, Y, Z), 'minecraft:ice')
            if (s[2] == 'warm' && heat) setSpec(level, pos(x + 1, Y, Z), heat == 'minecraft:campfire' ? 'minecraft:campfire[lit=true]' : heat)
        })
        CAMPFIRES.forEach(c => {
            if (!$OMReg.BLOCKS.containsKey(rl(c[2]))) { info(c[2] + ' not registered, campfire at x ' + c[0] + ' skipped'); return }
            setSpec(level, pos(c[0], Y - 1, Z), c[2])
            setSpec(level, pos(c[0], Y, Z), c[1])
        })
        cmd('weather rain 6000')
        info('setup done: wait 30 s (light and rain), then /omprobe wine and /omprobe campfire')
        check(true, 'setup placed ' + WINE_SPOTS.length + ' wine spots and ' + CAMPFIRES.length + ' campfires')
    }

    // ---------------------------------------------------------------- wine storage (alcohol/pack #2)
    function wine(level) {
        const $Years = Java.loadClass('net.satisfy.vinery.core.wine.WineYears')
        const $Clock = Java.loadClass('net.satisfy.vinery.core.wine.VintageClock')
        let c = $Clock.of(level)
        info('VintageClock cellar ' + c.cellarPercent() + ' %, cold ' + c.coldPercent() + ' %, warm ' + c.warmPercent() + ' %')
        let rate = kind => {
            let r = 100
            if (kind & 1) r *= c.cellarPercent() / 100
            if (kind & 2) r *= c.coldPercent() / 100
            if (kind & 4) r *= c.warmPercent() / 100
            return Math.round(r)
        }
        let WANT = { 0: 100, 2: 200, 4: 75, 1: 200, 3: 400, 5: 150 } // VB-76
        try {
            let $Heat = Java.loadClass('umpaz.brewinandchewin.common.utility.BnCHeatSources')   // not const: Rhino calls a re-run const a redeclaration
            info('Brewin climate at the spots: ' + WINE_SPOTS.map(s => $Heat.climate(level, pos(s[0], Y, Z))).join(' '))
        } catch (e) { info('BnCHeatSources.climate missing (Brewin -03 not installed?): ' + e) }
        WINE_SPOTS.forEach(s => {
            let k = $Years.storageKind(level, pos(s[0], Y, Z))
            check(k == s[3], 'x ' + s[0] + ' (' + (s[1] ? 'cellar' : 'open') + (s[2] ? ', ' + s[2] : '') + '): kind ' + k + ' (want ' + s[3] + ')')
            check(rate(k) == WANT[s[3]], 'x ' + s[0] + ' rate ' + rate(k) + ' % (VB-76 wants ' + WANT[s[3]] + ' %)')
        })
    }

    // ---------------------------------------------------------------- campfire rain (gameplay/misc #2)
    function campfire(level) {
        info('[campfire] rainDouses=' + $OMCE.CAMPFIRE.rainDouses.get() + ' rainOutTicks=' + $OMCE.CAMPFIRE.rainOutTicks.get() + '; raining ' + level.isRaining())
        CAMPFIRES.forEach(c => {
            if (!$OMReg.BLOCKS.containsKey(rl(c[2]))) return
            let st = level.getBlockState(pos(c[0], Y, Z))
            let lit = String(st).indexOf('lit=true') >= 0
            check(lit == c[3], c[1].split('[')[0] + ' on ' + c[2] + ': lit ' + lit + ' (want ' + c[3] + ')')
        })
        serverValue('unlitcampfire-server.toml', 'unlitcampfire', 'campfireLitTime', '0', 'Unlit Campfire burns forever')
    }

    // world serverconfig values: Unlit Campfire's own spec values, loaded from <world>/serverconfig/ (KubeJS's class
    // filter blocks ConfigTracker, so the source file is not printed)
    const UC_FIELDS = { campfireLitTime: 'CAMPFIRE_LIT_TIME', campfireRainUnlitTime: 'CAMPFIRE_RAIN_UNLIT_TIME',
        soulCampfireLitTime: 'SOUL_CAMPFIRE_LIT_TIME' }
    function serverValue(file, section, key, want, what) {
        let v = null
        try { v = Java.loadClass('cech12.unlitcampfire.config.ServerConfig')[UC_FIELDS[key]].get() } catch (e) { check(false, file + ' ' + key + ' unreadable: ' + e); return }
        check(String(v) == want, what + ': ' + file + ' ' + section + '.' + key + ' = ' + v + ' (want ' + want + ')')
    }
    function serverconfig(level) {
        serverValue('unlitcampfire-server.toml', 'unlitcampfire', 'campfireLitTime', '0', 'campfire lit time')
        serverValue('unlitcampfire-server.toml', 'unlitcampfire', 'campfireRainUnlitTime', '-1', 'campfire rain unlit time')
        serverValue('unlitcampfire-server.toml', 'unlitcampfire', 'soulCampfireLitTime', '0', 'soul campfire lit time')
    }

    // ---------------------------------------------------------------- cask (vineryport #7)
    function assign(held, ings) {
        let used = ings.map(() => false)
        let go = n => {
            if (n == held.length) return true
            for (let k = 0; k < ings.length; k++) {
                if (!used[k] && ings[k].test(held[n])) { used[k] = true; if (go(n + 1)) return true; used[k] = false }
            }
            return false
        }
        return go(0)
    }
    function cask(level) {
        const $Juice = Java.loadClass('net.satisfy.vinery.core.wine.JuiceUtil')
        let recipes = recipesOf('vinery:wine_fermentation')
        info(recipes.length + ' cask recipes')
        let ingsOf = r => { let out = []; r.getIngredients().forEach(g => { if (!g.isEmpty()) out.push(g) }); return out }
        let r1 = null, a = null, b = null
        for (let k = 0; k < recipes.length && r1 == null; k++) {
            let g = ingsOf(recipes[k])
            if (g.length < 2) continue
            let sa = stacksOf(g[0]), sb = stacksOf(g[1])
            if (!sa.length || !sb.length || sa[0].getItem() == sb[0].getItem()) continue
            r1 = recipes[k]; a = sa[0].copy(); b = sb[0].copy(); a.setCount(1); b.setCount(1)
        }
        if (r1 == null) { check(false, 'no cask recipe with 2 different ingredients'); return }
        let x = null
        for (let k = 0; k < recipes.length && x == null; k++) {
            ingsOf(recipes[k]).forEach(g => {
                let s = stacksOf(g)[0]
                if (x == null && s && s.getItem() != a.getItem() && !recipes.some(r => assign([a, s], ingsOf(r)))) { x = s.copy(); x.setCount(1) }
            })
        }
        info('recipe ' + r1.getId() + ': a ' + idOf(a) + ', b ' + idOf(b) + ', other-recipe item ' + (x ? idOf(x) : 'none'))
        let p = pos(16, Y, Z)
        setSpec(level, p, 'vinery:fermentation_barrel')
        let be = level.getBlockEntity(p)
        let NO = $OMDir.NORTH, UP = $OMDir.UP, DN = $OMDir.DOWN
        be.setItem(1, a.copy())
        check(be.canPlaceItemThroughFace(1, a, NO), 'same ingredient tops up its own slot')
        check(!be.canPlaceItemThroughFace(2, a, NO), 'same ingredient refused in the next slot (spill jam)')
        check(be.canPlaceItemThroughFace(2, b, NO), 'second ingredient of the recipe accepted')
        if (x) check(!be.canPlaceItemThroughFace(2, x, NO), idOf(x) + ' (no recipe with ' + idOf(a) + ') refused')
        check(!be.canPlaceItemThroughFace(2, stack('minecraft:dirt'), NO), 'dirt refused')
        check(!be.canTakeItemThroughFace(1, a, DN) && be.canTakeItemThroughFace(5, a, DN), 'only the output can be pulled')
        check(be.canPlaceItemThroughFace(4, stack('vinery:wine_bottle'), UP) && be.canPlaceItemThroughFace(4, stack('vinery:wine_bottle'), NO),
            'wine bottles from top and side')
        let jd = r1.getJuiceData()
        if (jd.amount() > 0) {
            let t1 = String(jd.type())
            let j1 = $Juice.juiceItem(t1)
            let t2 = null
            $Juice.types().forEach(t => {
                t = String(t)
                if (t2 == null && t != t1 && $Juice.juiceItem(t) != null &&
                    !recipes.some(r => String(r.getJuiceData().type()) == t && assign([a], ingsOf(r)))) t2 = t
            })
            info('juice ' + t1 + ' -> ' + (j1 ? String($OMReg.ITEMS.getKey(j1)) : 'none') + ', other type ' + t2)
            if (j1) check(be.canPlaceItemThroughFace(0, new $OMStack(j1, 1), UP), t1 + ' juice accepted from the top (empty cask, recipe fits)')
            if (t2) check(!be.canPlaceItemThroughFace(0, new $OMStack($Juice.juiceItem(t2), 1), UP), t2 + ' juice refused (no recipe with ' + idOf(a) + ')')
            be.setItem(1, $OMStack.EMPTY)
            be.setJuiceType(t1)
            be.setFluidLevel(1)
            if (t2) check(!be.canPlaceItemThroughFace(0, new $OMStack($Juice.juiceItem(t2), 1), UP), t2 + ' juice refused while ' + t1 + ' is in')
            if (j1) check(be.canPlaceItemThroughFace(0, new $OMStack(j1, 1), UP), t1 + ' juice accepted while ' + t1 + ' is in')
        } else info('recipe ' + r1.getId() + ' needs no juice; juice checks skipped')
        level.setBlockAndUpdate(p, Java.loadClass('net.minecraft.world.level.block.Blocks').AIR.defaultBlockState())
        clearItems(level, 16, Z)
    }

    // ---------------------------------------------------------------- press (vineryport #7)
    function press(level) {
        let mash = recipesOf('vinery:apple_mashing'), ferm = recipesOf('vinery:apple_fermenting')
        info(mash.length + ' mashing, ' + ferm.length + ' fermenting recipes')
        let mashOf = st => { let r = mash.filter(m => m.getInput().test(st)); return r.length ? r[0].getResultItem() : null }
        let fermOf = st => { let r = ferm.filter(m => m.getInput().test(st)); return r.length ? r[0] : null }
        let apple = stack('minecraft:apple'), comb = stack('minecraft:honeycomb')
        let p = pos(24, Y, Z)
        setSpec(level, p, 'vinery:wine_press')
        let be = level.getBlockEntity(p)
        let NO = $OMDir.NORTH, UP = $OMDir.UP, DN = $OMDir.DOWN
        check(be.canPlaceItemThroughFace(0, apple, NO), 'apple into an empty press from a side')
        if (mashOf(comb)) check(be.canPlaceItemThroughFace(0, comb, NO), 'honeycomb into an empty press from a side')
        else info('honeycomb is no mashing input here')
        check(!be.canPlaceItemThroughFace(0, apple, UP), 'nothing from the top')
        be.setItem(0, apple.copy())
        check(be.canPlaceItemThroughFace(0, apple, NO), 'more apples while apples are in')
        check(!be.canPlaceItemThroughFace(0, comb, NO), 'honeycomb waits while apples are in')
        let am = mashOf(apple), ar = am ? fermOf(am) : null, hm = mashOf(comb), hr = hm ? fermOf(hm) : null
        if (ar && ar.requiresBottle()) {
            let want = stacksOf(ar.getBottle())[0]
            check(be.canPlaceItemThroughFace(2, want, NO), idOf(want) + ' (apple recipe bottle) accepted')
            if (hr && hr.requiresBottle()) {
                let other = stacksOf(hr.getBottle())[0]
                if (other.getItem() != want.getItem()) check(!be.canPlaceItemThroughFace(2, other, NO), idOf(other) + ' (honey recipe bottle) waits')
            }
        } else info('apple recipe needs no bottle; bottle checks skipped')
        check(be.canTakeItemThroughFace(3, apple, DN) && !be.canTakeItemThroughFace(0, apple, DN), 'only the output can be pulled')
        level.setBlockAndUpdate(p, Java.loadClass('net.minecraft.world.level.block.Blocks').AIR.defaultBlockState())
        clearItems(level, 24, Z)
    }

    // ---------------------------------------------------------------- houses X Z (vineryport #14)
    function houses(level, args) {
        let x = parseInt(args[0]), z = parseInt(args[1])
        if (isNaN(x) || isNaN(z)) { check(false, 'usage: /omprobe houses X Z'); return }
        let cx = x >> 4, cz = z >> 4, found = {}, total = 0
        for (let dx = -6; dx <= 6; dx++) for (let dz = -6; dz <= 6; dz++) {
            let ch = level.getChunk(cx + dx, cz + dz)
            ch.getBlockEntities().values().forEach(be => {
                let id = String($OMReg.BLOCK_ENTITY_TYPES.getKey(be.getType()))
                if (id.indexOf('vinery:') == 0) { found[id] = (found[id] || 0) + 1; total++ }
            })
        }
        let winery = (found['vinery:wine_press'] || 0) + (found['vinery:fermentation_barrel'] || 0)
        info('village at ' + x + ' ' + z + ': Vinery block entities ' + JSON.stringify(found))
        log('VILLAGE ' + x + ' ' + z + ' winery=' + (winery > 0))
        check(true, 'houses scan done')
    }

    // ---------------------------------------------------------------- leafslow (gameplay/blockrunner #3)
    function leafslow(level) {
        let n = Number($OMCE.LEAF_PILES.slowFromLayers.get())
        info('[leaf_piles] slowFromLayers = ' + n)
        let zombie = $OMEntityType.ZOMBIE.create(level)
        let layers = [n - 1, n, n + 1].filter(l => l >= 1 && l <= 8)
        layers.forEach((l, k) => {
            let p = pos(120 + k * 2, Y, Z)
            setSpec(level, p, 'immersive_weathering:oak_leaf_pile[layers=' + l + ']')
            let st = level.getBlockState(p)
            zombie.setPos(120 + k * 2 + 0.5, Y, Z + 0.5)
            zombie.fallDistance = 5
            st.entityInside(level, p, zombie)
            let slowed = zombie.fallDistance == 0
            check(slowed == (l >= n), 'pile with ' + l + ' layers (' + st + '): slows ' + slowed + ' (want ' + (l >= n) + ')')
        })
    }

    // ---------------------------------------------------------------- raid (featureporting #3, FP-11)
    function raid(level) {
        const $Raid = Java.loadClass('net.minecraft.world.entity.raid.Raid')
        info('[raids] glowingRaiders=' + $OMCE.RAIDS.glowingRaiders.get() + ' glowingAmplifier=' + $OMCE.RAIDS.glowingAmplifier.get())
        let r = new $Raid(990001, level, pos(0, Y, Z))
        let pill = $OMEntityType.PILLAGER.create(level)
        r.addWaveMob(1, pill, false)
        let g = pill.getEffect(effect('minecraft:glowing'))
        let on = String($OMCE.RAIDS.glowingRaiders.get()) == 'true'
        if (on) check(g != null && g.getAmplifier() == Number($OMCE.RAIDS.glowingAmplifier.get()) && !g.isVisible() && !g.showIcon(),
            'raider in a wave glows: ' + (g == null ? 'none' : 'amp ' + g.getAmplifier() + ', visible ' + g.isVisible() + ', icon ' + g.showIcon()))
        else check(g == null, 'glowingRaiders off: no Glowing')
    }

    // ---------------------------------------------------------------- air (featureporting #3, FP-13)
    function air(level) {
        const $RightClick = Java.loadClass('net.minecraftforge.event.entity.player.PlayerInteractEvent$RightClickItem')
        let cfg = Java.loadClass('com.SaintAlfonzo.creativeequipment.CEQConfig').BOTTLED_AIR  // [bottled_air]: Creative Equipment since 2026-11-04
        info('[bottled_air] enabled=' + cfg.enabled.get() + ' airPerBottle=' + cfg.airPerBottle.get() + ' fireEvaporates=' + cfg.fireEvaporates.get() + ' noDrinkingUnderwater=' + cfg.noDrinkingUnderwater.get())
        let x = 180
        cmd('fill ' + (x - 1) + ' ' + Y + ' ' + (Z - 1) + ' ' + (x + 1) + ' ' + (Y + 3) + ' ' + (Z + 1) + ' minecraft:glass')
        cmd('fill ' + x + ' ' + Y + ' ' + Z + ' ' + x + ' ' + (Y + 3) + ' ' + Z + ' minecraft:water')
        let pl = fake(level, 'omprobe_air')
        pl.setPos(x + 0.5, Y, Z + 0.5)
        pl.baseTick()
        info('fake player isInWater ' + pl.isInWater() + ', isUnderWater ' + pl.isUnderWater())
        if (!pl.isInWater()) { check(false, 'fake player not in water after baseTick (probe setup)'); return }
        let post = (main, off) => {
            pl.setItemInHand($OMHand.MAIN_HAND, main)
            pl.setItemInHand($OMHand.OFF_HAND, off || $OMStack.EMPTY)
            let ev = new $RightClick(pl, $OMHand.MAIN_HAND)
            $OMBus.EVENT_BUS.post(ev)
            return ev
        }
        pl.setAirSupply(0)
        post(stack('minecraft:glass_bottle'))
        check(pl.getAirSupply() == Math.min(pl.getMaxAirSupply(), Number(cfg.airPerBottle.get())), 'glass bottle under water: air 0 -> ' + pl.getAirSupply())
        let water = Java.loadClass('net.minecraft.world.item.alchemy.PotionUtils').setPotion(stack('minecraft:potion'),
            Java.loadClass('net.minecraft.world.item.alchemy.Potions').WATER)
        pl.getInventory().setItem(10, water.copy())
        pl.setAirSupply(0)
        post(stack('minecraft:glass_bottle'), stack('minecraft:lava_bucket'))
        let left = pl.getInventory().getItem(10)
        check(left.isEmpty() || idOf(left) != 'minecraft:potion', 'lava bucket in the off hand: the water bottle in slot 10 is gone (' + idOf(left) + ')')
        let ev = post(water.copy())
        check(!pl.isUnderWater() || ev.isCanceled(), 'water bottle under water: drinking cancelled (' + ev.isCanceled() + ')')
        cmd('fill ' + (x - 1) + ' ' + Y + ' ' + (Z - 1) + ' ' + (x + 1) + ' ' + (Y + 3) + ' ' + (Z + 1) + ' minecraft:air')
    }

    // ---------------------------------------------------------------- tier1 (featureporting #2)
    function tier1(level) {
        let ids = {}, list = []
        RM.getRecipes().forEach(r => { let id = String(r.getId()); ids[id] = r; list.push(id) })
        let ce = list.filter(id => id.indexOf('creativeengineering:') == 0)
        let ae2 = ce.filter(id => id.indexOf('/ae2_') >= 0).length
        let rec = ce.filter(id => id.indexOf('/recycle_') >= 0).length
        let spl = ce.filter(id => /createsplashing\/.*_(exposed|weathered|oxidized)$/.test(id)).length
        check(ae2 == 311, 'AE2 cable recipes ' + ae2 + ' (want 311)')
        check(rec == 26, 'recycle smelting recipes ' + rec + ' (want 26)')
        check(spl == 33, 'copper splashing recipes ' + spl + ' (want 33)')
        let out = id => { let r = ids[id]; return r == null ? 'missing' : idOf(r.getResultItem()) + ' x' + r.getResultItem().getCount() }
        ;[['creativeengineering:shaped/name_tag', 'minecraft:name_tag x1'],
          ['creativeengineering:smelting/recycle_hopper', 'minecraft:iron_ingot x1'],
          ['creativeengineering:smelting/recycle_rail', 'minecraft:iron_nugget x1'],
          ['creativeengineering:smelting/recycle_anvil', 'minecraft:iron_block x1'],
          ['creativeengineering:createsplashing/copper_block_exposed', 'minecraft:exposed_copper x1']].forEach(r =>
            check(out(r[0]) == r[1], r[0] + ' -> ' + out(r[0])))
        let nt = ids['creativeengineering:shaped/name_tag']
        if (nt) {
            let ins = []
            nt.getIngredients().forEach(g => { let s = stacksOf(g)[0]; if (s) ins.push(idOf(s)) })
            check(ins.sort().join(',') == 'minecraft:leather,minecraft:paper,minecraft:string,minecraft:string', 'name tag from ' + ins.join(', '))
        }
        let gen = list.filter(id => id.indexOf('createmixing/generator_') >= 0 || id.indexOf('create_generators:') == 0)
        check(gen.length == 0, 'no generator recipes left (' + gen.slice(0, 5).join(', ') + ')')
        let obs = list.filter(id => /obsidian_generation_heated$/.test(id))
        check(obs.length == 1, 'obsidian_generation_heated kept: ' + obs.join(', '))

        // copper ore drops 1 raw copper (Copper Drops port), silk touch the ore
        let pl = fake(level, 'omprobe_copper')
        const $Ench = Java.loadClass('net.minecraft.world.item.enchantment.Enchantments')
        let mine = (ore, silk) => {
            let p = pos(200, Y, Z)
            clearItems(level, 200, Z)
            setSpec(level, p, ore)
            let pick = stack('minecraft:iron_pickaxe')
            if (silk) pick = pick.enchant($Ench.SILK_TOUCH, 1)   // KubeJS's enchant() returns an enchanted copy
            pl.setItemInHand($OMHand.MAIN_HAND, pick)
            pl.gameMode.destroyBlock(p)
            let got = {}
            itemsIn(level, 196, Y - 2, Z - 4, 205, Y + 3, Z + 5).forEach(e => { let s = e.getItem(); got[idOf(s)] = (got[idOf(s)] || 0) + s.getCount() })
            return got
        }
        let counts = []
        for (let k = 0; k < 10; k++) { let g = mine('minecraft:copper_ore', false); counts.push(g['minecraft:raw_copper'] || 0) }
        check(counts.every(c => c == 1), 'copper ore x10 -> raw copper ' + counts.join(' '))
        let dg = mine('minecraft:deepslate_copper_ore', false)
        check((dg['minecraft:raw_copper'] || 0) == 1, 'deepslate copper ore -> ' + JSON.stringify(dg))
        let sg = mine('minecraft:copper_ore', true)
        check((sg['minecraft:copper_ore'] || 0) == 1, 'silk touch -> ' + JSON.stringify(sg))
        clearItems(level, 200, Z)
    }

    // ---------------------------------------------------------------- baby (babymobport #1)
    const BABY_KILLS = [['squid', 'SQUID', 214], ['glow squid', 'GLOW_SQUID', 218], ['dolphin', 'DOLPHIN', 222]]
    function babyxp(level) {
        let orbs = kx => level.getEntitiesOfClass($OMOrb, new $OMAABB(kx - 1.9, Y - 3, Z - 2, kx + 2.9, Y + 4, Z + 3)).size()
        check(orbs(210) > 0, 'adult squid (control) left ' + orbs(210) + ' experience orbs')
        BABY_KILLS.forEach(t => check(orbs(t[2]) == 0, 'baby ' + t[0] + ' left ' + orbs(t[2]) + ' experience orbs'))
    }
    function baby(level) {
        const $Ages = Java.loadClass('com.SaintAlfonzo.baby_mobs.age.BabyAges')
        const $BM = Java.loadClass('com.SaintAlfonzo.baby_mobs.BMConfig')
        let x = 210
        // drops: adult squid as the control, babies nothing. Items drop at death; experience only when the death
        // animation ends (20 ticks), so `babyxp` counts the orbs a second later
        let kill = (type, isBaby, kx) => {
            itemsIn(level, kx - 1.9, Y - 3, Z - 2, kx + 2.9, Y + 4, Z + 3).forEach(e => e.discard())
            level.getEntitiesOfClass($OMOrb, new $OMAABB(kx - 1.9, Y - 3, Z - 2, kx + 2.9, Y + 4, Z + 3)).forEach(e => e.discard())
            let m = type.create(level)
            m.setPos(kx + 0.5, Y, Z + 0.5)
            if (isBaby) $Ages.setAge(m, -24000)
            level.addFreshEntity(m)
            let pl = fake(level, 'omprobe_baby')
            m.attack($OMDamage.playerAttack(pl), 1000)   // KubeJS hides hurt(); attack() calls it
            return [itemsIn(level, kx - 1.9, Y - 3, Z - 2, kx + 2.9, Y + 4, Z + 3).size(), isBaby ? $Ages.isBaby(m) : false]
        }
        let ad = kill($OMEntityType.SQUID, false, x)
        check(ad[0] > 0, 'adult squid (control) drops ' + ad[0] + ' item stacks')
        BABY_KILLS.forEach(t => {
            let r = kill($OMEntityType[t[1]], true, t[2])
            check(r[1] && r[0] == 0, 'baby ' + t[0] + ' (isBaby ' + r[1] + ') drops ' + r[0] + ' item stacks')
        })
        info('run /omprobe babyxp in 2 s or more for the experience orbs')

        // dolphin feeding: a cod ages a baby by 10 % of what's left
        let d = $OMEntityType.DOLPHIN.create(level)
        d.setPos(x + 16.5, Y, Z + 0.5)
        $Ages.setAge(d, -24000)
        level.addFreshEntity(d)
        let pl = fake(level, 'omprobe_feed')
        pl.setItemInHand($OMHand.MAIN_HAND, stack('minecraft:cod', 3))
        d.interact(pl, $OMHand.MAIN_HAND)
        let age = $Ages.getAge(d)
        info('[growth] dolphinFishFeeding=' + $BM.DOLPHIN_FISH_FEEDING.get())
        check(age == -21600 && pl.getMainHandItem().getCount() == 2, 'baby dolphin fed a cod: age -24000 -> ' + age + ' (want -21600), cod left ' + pl.getMainHandItem().getCount())
        d.discard()

        // natural spawn share: packs of 4 through finalizeSpawn (NATURAL), the first of a pack never a baby
        let diff = level.getCurrentDifficultyAt(pos(x, Y, Z))
        ;[['squid', $OMEntityType.SQUID, Number($BM.SQUID_BABY_CHANCE.get())], ['glow squid', $OMEntityType.GLOW_SQUID, Number($BM.GLOW_SQUID_BABY_CHANCE.get())],
          ['dolphin', $OMEntityType.DOLPHIN, Number($BM.DOLPHIN_BABY_CHANCE.get())]].forEach(t => {
            let first = 0, rest = 0, babies = 0
            for (let pack = 0; pack < 400; pack++) {
                let data = null
                for (let k = 0; k < 4; k++) {
                    let m = t[1].create(level)
                    m.setPos(x + 0.5, Y, Z + 0.5)
                    data = m.finalizeSpawn(level, diff, $OMSpawnType.NATURAL, data, null)
                    if (k == 0) { if ($Ages.isBaby(m)) first++ } else { rest++; if ($Ages.isBaby(m)) babies++ }
                }
            }
            let share = babies / rest
            check(first == 0, t[0] + ': first of a pack never a baby (' + first + ' of 400)')
            check(t[2] == 0 ? babies == 0 : Math.abs(share - t[2]) <= Math.max(0.025, t[2] * 0.4),
                t[0] + ': ' + babies + ' of ' + rest + ' pack members are babies (' + (share * 100).toFixed(1) + ' %, config ' + (t[2] * 100) + ' %)')
        })
    }

    // ---------------------------------------------------------------- jeed (ui/effects #4)
    function jeed(level) {
        let byEffect = {}, ours = 0, empty = []
        RM.getRecipes().forEach(r => {
            if (String(r.getClass().getName()) != 'net.mehvahdjukaar.jeed.recipes.EffectProviderRecipe') return
            let id = String(r.getId())
            let mine = id.indexOf('jeed:') != 0 && id.indexOf('meed:') != 0
            if (mine) ours++
            let items = []
            r.getIngredients().forEach(g => {
                let st = stacksOf(g)
                if (!st.length || st.every(s => s.isEmpty())) { if (mine) empty.push(id) } else st.forEach(s => items.push(idOf(s)))
            })
            r.getEffects().forEach(e => {
                let eid = String($OMReg.MOB_EFFECTS.getKey(e))
                byEffect[eid] = (byEffect[eid] || []).concat(items)
            })
        })
        info(ours + ' pack provider recipes (the boot log line says how many jeed_providers.js wrote), ' + Object.keys(byEffect).length + ' effects with any provider recipe')
        check(empty.length == 0, 'no pack provider recipe has an empty ingredient' + (empty.length ? ': ' + empty.slice(0, 8).join(', ') : ''))
        let has = (eff, id) => (byEffect[eff] || []).indexOf(id) >= 0
        check(has('minecraft:health_boost', i.vinery_standard) && has('minecraft:health_boost', 'vinery:solaris_wine') && has('minecraft:health_boost', 'vinery:stal_wine'),
            'Health Boost lists the Vinery standard, Solaris and Stal Wine')
        // Xaero's is client-only: a dedicated server has no such effect, and jeed_providers.js skips it there
        if (Platform.isLoaded('xaerominimap')) check(has('xaerominimap:no_minimap', 'minecraft:command_block'), 'No Minimap lists the command block')
        else log('INFO No Minimap: xaerominimap not loaded (dedicated server), no provider expected')
        check(!byEffect['sunny'] && Object.keys(byEffect).every(k => k.indexOf(':sunny') < 0), 'Sunny has no provider')
        let soda = i.cherry_cream_soda
        let craftable = false
        RM.getRecipes().forEach(r => { try { if (!craftable && idOf(r.getResultItem()) == soda) craftable = true } catch (e) {} })
        info('cherry cream soda (' + soda + ') registered ' + exists(soda) + ', listed for slow_falling ' + has('minecraft:slow_falling', soda) + ', some recipe makes it ' + craftable + ' (TODO ui/effects #4 bug)')
        let none = []
        $OMReg.MOB_EFFECTS.getKeys().forEach(k => { if (!byEffect[String(k)]) none.push(String(k)) })
        info(none.length + ' effects without a provider recipe (foods and potions JEED finds itself): ' + none.slice(0, 40).join(' '))
    }

    const PARTS = { keg: keg, coaster: coaster, compost: compost, drinks: drinks, cask: cask, press: press, leafslow: leafslow,
        raid: raid, air: air, tier1: tier1, baby: baby, jeed: jeed }
    const EXTRA = { babyxp: babyxp, setup: setup, wine: wine, campfire: campfire, houses: houses, serverconfig: serverconfig }

    function run(source, line) {
        pass = 0; fail = 0
        SERVER = source.getServer()
        RM = SERVER.getRecipeManager()
        let level = source.getLevel()
        let words = line.split(/\s+/)
        let part = words[0]
        let names = part == 'all' ? Object.keys(PARTS) : [part]
        names.forEach(n => {
            let fn = PARTS[n] || EXTRA[n]
            if (!fn) { fail++; log('FAIL unknown part ' + n + ' (' + Object.keys(PARTS).concat(Object.keys(EXTRA)).join('|') + '|all)'); return }
            log('== ' + n)
            try { fn(level, words.slice(1)) } catch (e) { fail++; log('FAIL ' + n + ' ERROR ' + e) }
        })
        log('done: ' + pass + ' pass, ' + fail + ' fail')
    }

    ServerEvents.commandRegistry(event => {
        const { commands: Commands, arguments: Arguments } = event
        event.register(Commands.literal('omprobe')
            .then(Commands.argument('part', Arguments.GREEDY_STRING.create(event)).executes(ctx => {
                run(ctx.source, String(Arguments.GREEDY_STRING.getResult(ctx, 'part')).trim())
                return 1
            }))
            .executes(ctx => { run(ctx.source, 'all'); return 1 }))
    })
})()
