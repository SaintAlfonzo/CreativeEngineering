// Scratch-server probe for the 2026-10-22 gear / mobs / storage / misc work (queue entries SQ-GM-* in
// feature_writeup/testing/scratch_queue.md). Scratch-only: copy into a scratch server's kubejs/server_scripts/ as
// zz_gm_probe.js (tools/creative_labels_test/scratch_server.sh), never into the pack. Run `/gmprobe` for every part or
// `/gmprobe <part>`; results are "[gmprobe] PASS|FAIL|INFO ..." lines in logs/latest.log, ending
// "[gmprobe] done: N pass, M fail".
//
//  shield   shields.md 2.4, SH-44: a zombie holding each tier shield gets a ShieldBlockEvent; on-block effects
//  effects  armor_effects.md F4, AE-19 / AE-20 on-hit half: global.toolEffects tables, and a fake player hitting a
//           zombie with the dragon bow / crossbow in the main hand
//  bag      camera_bags.md F4, CB-11: CameraBag.canHold for the flashes
//  archery  archery.md F1, F5, AR-07 / AR-13: dragon scale cartridges in blueprint category bullet (matches the
//           inputs), explosive arrow blueprint and fletching recipes take the blazing shaft tag
//  gull     mob_block_tweaks.md MT-26: Alex's Mobs seagullStealingBlacklist holds the three lunchboxes
//  chalk    fixes_compat.md FX-27: Chalk unlocks every special symbol for a player with no advancements
//  vein     fixes_compat.md 2.26, FX-31 / FX-32 / FX-34 logic: VeinUndo.refusal on a hand-built Ore Excavation
//           history, the cooldown, Auto Pickup, the offline-player guard in tickRestore
//  sack     storage_audit.md 6, SB-01 / SB-03 / SB-04 logic: SackBundle.rotate and the take-out mixin
//           (SackItem.overrideOtherStackedOnMe with an empty cursor)
//
// Rhino notes (memory rhino-fieldandmethods-gotcha): no BlockState.is / ItemStack.is from JS (ambiguous overloads),
// config Booleans compared as text, every top-level name is inside this IIFE.
(() => {
    const $GMFake = Java.loadClass('net.minecraftforge.common.util.FakePlayerFactory')
    const $GMProfile = Java.loadClass('com.mojang.authlib.GameProfile')
    const $GMUUID = Java.loadClass('java.util.UUID')
    const $GMReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $GMRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $GMStack = Java.loadClass('net.minecraft.world.item.ItemStack')
    const $GMTagParser = Java.loadClass('net.minecraft.nbt.TagParser')
    const $GMEntityType = Java.loadClass('net.minecraft.world.entity.EntityType')
    const $GMHand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $GMDamage = Java.loadClass('net.minecraft.world.damagesource.DamageSource')
    const $GMBus = Java.loadClass('net.minecraftforge.common.MinecraftForge')
    const $GMShieldEvent = Java.loadClass('net.minecraftforge.event.entity.living.ShieldBlockEvent')

    const SHIELDS = [
        // id, expected [effect, amplifier] on the holder, attacker effect or null
        ['creativeequipment:netherite_shield', [], null],
        ['creativeequipment:echo_shield', [['minecraft:resistance', 0]], null],
        ['creativeequipment:felsic_shield', [['minecraft:resistance', 1]], null],
        ['creativeequipment:dragon_shield', [['minecraft:resistance', 1], ['minecraft:regeneration', 0]], 'minecraft:poison']
    ]
    const BOW = 'dragonloot:dragon_bow', CROSSBOW = 'dragonloot:dragon_crossbow'

    let pass = 0, fail = 0
    function log(s) { console.info('[gmprobe] ' + s) }
    function check(ok, what) { if (ok) pass++; else fail++; log((ok ? 'PASS ' : 'FAIL ') + what) }
    function rl(id) { return new $GMRL(id) }
    function item(id) { return $GMReg.ITEMS.getValue(rl(id)) }
    function stack(id, n) { return new $GMStack(item(id), n) }
    function idOf(st) { return String($GMReg.ITEMS.getKey(st.getItem())) }
    function effect(id) { return $GMReg.MOB_EFFECTS.getValue(rl(id)) }
    function amp(entity, id) { let e = entity.getEffect(effect(id)); return e == null ? -1 : e.getAmplifier() }
    // level.dimension is KubeJS's ResourceLocation property, which hides Level.dimension()
    function dimKey(level) {
        return Java.loadClass('net.minecraft.resources.ResourceKey').create(Java.loadClass('net.minecraft.core.Registry').DIMENSION_REGISTRY, rl(String(level.dimension)))
    }
    function fake(level, name) {
        let p = $GMFake.get(level, new $GMProfile($GMUUID.randomUUID(), name))
        p.getInventory().clearContent()
        return p
    }

    function shield(level) {
        const $ShieldConfig = Java.loadClass('com.SaintAlfonzo.creativeequipment.shield.ShieldConfig')
        const $ShieldTier = Java.loadClass('com.SaintAlfonzo.creativeequipment.shield.ShieldTier')
        ;['ECHO', 'FELSIC', 'DRAGON'].forEach(t => log('INFO shielderEffects ' + t + ' = "' + $ShieldConfig.shielderEffects($ShieldTier[t]) + '"'))
        SHIELDS.forEach(row => {
            // Not zombies: undead mobs can't get Regeneration or Poison (dragon shield)
            let holder = $GMEntityType.PILLAGER.create(level)
            let attacker = $GMEntityType.PILLAGER.create(level)
            holder.setItemInHand($GMHand.MAIN_HAND, stack(row[0], 1))
            holder.startUsingItem($GMHand.MAIN_HAND)
            check(idOf(holder.getUseItem()) == row[0], row[0] + ' raised')
            $GMBus.EVENT_BUS.post(new $GMShieldEvent(holder, $GMDamage.mobAttack(attacker), 4.0))
            let res = amp(holder, 'minecraft:resistance')
            let want = row[1].filter(e => e[0] == 'minecraft:resistance')
            check(res == (want.length ? want[0][1] : -1), row[0] + ' Resistance amplifier ' + res + ' (want ' + (want.length ? want[0][1] : 'none') + ')')
            row[1].filter(e => e[0] != 'minecraft:resistance').forEach(e =>
                check(amp(holder, e[0]) == e[1], row[0] + ' ' + e[0] + ' amplifier ' + amp(holder, e[0])))
            if (row[2]) check(amp(attacker, row[2]) >= 0, row[0] + ' attacker gets ' + row[2])
            let r = holder.getEffect(effect('minecraft:resistance'))
            if (r != null) check(r.getDuration() == 40, row[0] + ' Resistance lasts 40 ticks (' + r.getDuration() + ')')
        })
    }

    function effects(level) {
        const TE = global.toolEffects
        let names = (list) => (list || []).map(s => s.effect + '/' + (s.amplifier || 0)).join(', ')
        log('INFO bow onHit [' + names(TE.ON_HIT[BOW]) + '] held [' + names(TE.HELD[BOW]) + ']')
        log('INFO crossbow onHit [' + names(TE.ON_HIT[CROSSBOW]) + '] held [' + names(TE.HELD[CROSSBOW]) + ']')
        check(names(TE.HELD[BOW]) == 'minecraft:invisibility/0', 'bow held = Invisibility I')
        check(names(TE.HELD[CROSSBOW]) == 'minecraft:resistance/1', 'crossbow held = Resistance II')
        ;[[BOW, [['minecraft:slowness', 3], ['minecraft:glowing', 0]]],
          [CROSSBOW, [['alexsmobs:exsanguination', 1], ['minecraft:weakness', 2]]]].forEach(row => {
            let p = fake(level, 'gmprobe_fx')
            p.setItemInHand($GMHand.MAIN_HAND, stack(row[0], 1))
            let target = $GMEntityType.ZOMBIE.create(level)
            let hit = target.attack($GMDamage.playerAttack(p), 1.0)   // KubeJS hides hurt(); attack() calls it
            log('INFO ' + row[0] + ' hit returned ' + hit + ', target health ' + target.getHealth() + ', attacker holds ' + idOf(p.getMainHandItem()))
            row[1].forEach(e => check(amp(target, e[0]) == e[1], row[0] + ' hit gives ' + e[0] + ' amplifier ' + amp(target, e[0]) + ' (want ' + e[1] + ')'))
            ;['minecraft:invisibility', 'minecraft:resistance'].forEach(id =>
                check(amp(target, id) < 0, row[0] + ' hit gives no ' + id))
        })
    }

    function bag() {
        const $Bag = Java.loadClass('com.SaintAlfonzo.exposureextras.camerabag.CameraBag')
        check($Bag.canHold(stack('minecraft:redstone_lamp', 1)), 'bag holds a redstone lamp')
        check($Bag.canHold(stack('immersiveengineering:light_bulb', 1)), 'bag holds an IE light bulb')
        check(!$Bag.canHold(stack('minecraft:stick', 1)), 'bag refuses a stick')
    }

    function archery(level) {
        const $BP = Java.loadClass('blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe')
        const $NNL = Java.loadClass('net.minecraft.core.NonNullList')
        let find = (cat, id) => {
            let arr = $BP.findRecipes(level, cat), hit = null
            for (let n = 0; n < arr.length; n++) if (String(arr[n].getId()) == id) hit = arr[n]
            return hit
        }
        let rod = stack('minecraft:blaze_rod', 1)
        let ap = find('bullet', 'creativeengineering:blueprint/bullet/armor_piercing_dragon_scale')
        check(ap != null, 'dragon scale cartridges in blueprint category bullet')
        check(find('projectiles', 'creativeengineering:blueprint/projectiles/armor_piercing_dragon_scale') == null, 'no projectiles copy left')
        if (ap != null) {
            let inv = $NNL.create()
            inv.add(stack('immersiveengineering:empty_casing', 64))
            inv.add(stack('minecraft:gunpowder', 32))
            inv.add(stack('dragonloot:dragon_scale', 1))
            check(ap.matchesRecipe(inv), '64 casings + 32 gunpowder + dragon scale match (' + ap.getMaxCrafted(inv) + ' crafts)')
            let out = ap.output.get()
            check(out.getCount() == 64 && /armor_piercing/.test(idOf(out)), 'output ' + out.getCount() + 'x ' + idOf(out))
        }
        let ex = find('archery', 'creativeengineering:blueprint/archery/explosive_arrow')
        let exRod = false
        if (ex != null) for (let n = 0; n < ex.inputs.length; n++) if (ex.inputs[n].testIgnoringSize(rod)) exRod = true
        check(exRod, 'explosive arrow blueprint takes a blaze rod through the shaft tag')
        let fl = level.getServer().getRecipeManager().byKey(rl('creativeengineering:fletching/tnt_to_explosive_arrow_by_fletching'))
        let flRod = false
        if (fl.isPresent()) fl.get().getIngredients().forEach(x => { if (x.test(rod)) flRod = true })
        check(flRod, 'explosive arrow fletching recipe takes a blaze rod')
    }

    function gull() {
        const $AM = Java.loadClass('com.github.alexthe666.alexsmobs.config.AMConfig')
        let list = String($AM.seagullStealingBlacklist)
        log('INFO seagullStealingBlacklist = ' + list)
        ;['solapplepie:lunchbox', 'solapplepie:lunchbag', 'solapplepie:golden_lunchbox'].forEach(id =>
            check(list.indexOf(id) >= 0 && item(id) != null && idOf(stack(id, 1)) == id, 'gull blacklist has ' + id))
    }

    function chalk(level) {
        const $Unlock = Java.loadClass('io.github.mortuusars.chalk.core.SymbolUnlocking')
        const $Mark = Java.loadClass('io.github.mortuusars.chalk.core.MarkSymbol')
        let got = String($Unlock.getUnlockedSymbols(fake(level, 'gmprobe_chalk')))
        log('INFO unlocked for a new player: ' + got)
        $Mark.getSpecialSymbols().forEach(m => check(got.indexOf(String(m)) >= 0, 'chalk symbol ' + m + ' unlocked'))
    }

    function vein(level) {
        const $VU = Java.loadClass('com.SaintAlfonzo.creativecompat.excavation.VeinUndo')
        const $VUA = Java.loadClass('com.SaintAlfonzo.creativecompat.excavation.VeinUndoAccess')
        const $CE = Java.loadClass('com.SaintAlfonzo.creativecommon.CommonConfig')
        const $History = Java.loadClass('oreexcavation.undo.ExcavateHistory')
        const $BlockHistory = Java.loadClass('oreexcavation.undo.BlockHistory')
        const $Deque = Java.loadClass('java.util.ArrayDeque')
        const $Pos = Java.loadClass('net.minecraft.core.BlockPos')
        check($VUA.autoPickup() == true, 'Ore Excavation Auto Pickup on')
        log('INFO [vein_undo] cooldownSeconds ' + $CE.VEIN_UNDO.cooldownSeconds.get() + ', denyBlockEntities ' + String($CE.VEIN_UNDO.denyBlockEntities.get()))
        let map = $VUA.scheduler()['creativeengineering$undoHistory']()
        let refusalFor = (blockId) => {
            let p = fake(level, 'gmprobe_vein')
            let h = new $History(dimKey(level))
            h.addRecordedBlock(new $BlockHistory(new $Pos(0, 64, 0), $GMReg.BLOCKS.getValue(rl(blockId)).defaultBlockState(), null))
            let d = new $Deque(); d.push(h)
            map.put(p.getGameProfile().getId(), d)
            try { let c = $VU.refusal(p); return c == null ? null : String(c.getString()) } finally { map.remove(p.getGameProfile().getId()) }
        }
        check(refusalFor('minecraft:stone') == null, 'stone vein mine can be undone')
        ;['minecraft:iron_ore', 'minecraft:deepslate_coal_ore', 'minecraft:diamond_block', 'minecraft:budding_amethyst',
          'minecraft:spawner', 'minecraft:ancient_debris', 'minecraft:chest'].forEach(id => {
            let r = refusalFor(id)
            check(r != null && r.indexOf("can't be undone") >= 0, id + ' refused: ' + r)
        })
        let p = fake(level, 'gmprobe_vein_cd')
        check($VU.refusal(p) == null, 'no history, no cooldown: no refusal of ours')
        $VU.startCooldown(p)
        let cd = $VU.refusal(p)
        check(cd != null && String(cd.getString()).indexOf('undo another vein mine') >= 0, 'cooldown after an undo: ' + (cd == null ? null : cd.getString()))
        let h = new $History(dimKey(level))
        check(h['creativeengineering$history']() != null, 'ExcavateHistory mixin applied')
        let ok = false
        try { ok = h.tickRestore(level.getServer(), null) == false } catch (e) { log('INFO tickRestore threw ' + e) }
        check(ok, 'tickRestore with an offline player waits (false, no crash)')
    }

    function sack(level) {
        const $SB = Java.loadClass('com.SaintAlfonzo.creativecommon.compat.SackBundle')
        const $Slot = Java.loadClass('net.minecraft.world.inventory.Slot')
        const $SlotAccess = Java.loadClass('net.minecraft.world.entity.SlotAccess')
        const $Click = Java.loadClass('net.minecraft.world.inventory.ClickAction')
        const $Simple = Java.loadClass('net.minecraft.world.SimpleContainer')
        let filled = (ids) => {
            let items = ids.map((id, n) => '{Slot:' + n + 'b,id:"' + id + '",Count:' + (n + 2) + 'b}').join(',')
            return Item.of('supplementaries:sack', 1, '{BlockEntityTag:{Items:[' + items + ']}}')   // KubeJS hides setTag()
        }
        let first = (s) => { let t = s.getTagElement('BlockEntityTag'); return t == null ? 'none' : String(t.getList('Items', 10).getCompound(0).getString('id')) }
        let count = (s) => { let t = s.getTagElement('BlockEntityTag'); return t == null ? 0 : t.getList('Items', 10).size() }

        let s = filled(['minecraft:cobblestone', 'minecraft:dirt', 'minecraft:apple'])
        check($SB.canRotate(s), 'sack with 3 stacks can rotate')
        $SB.rotate(s, true)
        check(first(s) == 'minecraft:dirt', 'scroll down: dirt selected (' + first(s) + ')')
        $SB.rotate(s, false)
        check(first(s) == 'minecraft:cobblestone', 'scroll up: cobblestone back (' + first(s) + ')')
        check(!$SB.canRotate(filled(['minecraft:dirt'])), 'one stack: no rotate')

        let p = fake(level, 'gmprobe_sack')
        p.getInventory().setItem(9, s)
        let slot = new $Slot(p.getInventory(), 9, 0, 0)
        let cursor = new $Simple(1)
        let took = s.getItem().overrideOtherStackedOnMe(s, $GMStack.EMPTY, slot, $Click.SECONDARY, p, $SlotAccess.forContainer(cursor, 0))
        let got = cursor.getItem(0)
        check(took == true && idOf(got) == 'minecraft:cobblestone' && got.getCount() == 2, 'right-click, empty cursor: ' + got.getCount() + 'x ' + idOf(got))
        check(count(s) == 2 && first(s) == 'minecraft:dirt', 'sack keeps the other 2, dirt next (' + count(s) + ', ' + first(s) + ')')

        let one = filled(['minecraft:apple'])
        let cur2 = new $Simple(1)
        one.getItem().overrideOtherStackedOnMe(one, $GMStack.EMPTY, slot, $Click.SECONDARY, p, $SlotAccess.forContainer(cur2, 0))
        check(idOf(cur2.getItem(0)) == 'minecraft:apple' && one.nbt == null, 'emptied sack has no tag (nbt ' + one.nbt + ')')   // KubeJS hides hasTag()

        let left = new $Simple(1)
        let primary = s.getItem().overrideOtherStackedOnMe(s, $GMStack.EMPTY, slot, $Click.PRIMARY, p, $SlotAccess.forContainer(left, 0))
        check(left.getItem(0).isEmpty() && count(s) == 2, 'left click takes nothing out (returned ' + primary + ')')
    }

    const PARTS = { shield: shield, effects: effects, bag: bag, archery: archery, gull: gull, chalk: chalk, vein: vein, sack: sack }

    function run(level, which) {
        pass = 0; fail = 0
        Object.keys(PARTS).forEach(k => {
            if (which != 'all' && which != k) return
            try { PARTS[k](level) } catch (e) { fail++; log('FAIL ' + k + ' ERROR ' + e) }
        })
        log('done: ' + pass + ' pass, ' + fail + ' fail')
    }

    ServerEvents.commandRegistry(event => {
        const { commands: Commands, arguments: Arguments } = event
        event.register(Commands.literal('gmprobe')
            .executes(ctx => { run(ctx.source.getLevel(), 'all'); return 1 })
            .then(Commands.argument('part', Arguments.GREEDY_STRING.create(event))
                .executes(ctx => { run(ctx.source.getLevel(), String(Arguments.GREEDY_STRING.getResult(ctx, 'part'))); return 1 })))
    })
})()
