// Scratch-only Hearth and Harvest gear probe (SQ-HH-15xx, 16xx). Copy into a scratch server's kubejs/server_scripts as
// zz_hh_gear_probe.js, never into the pack. P = player or perfBot bot, TAG = an entity tag set with Tags:[...] at summon.
//   /zhh5 sneak P on|off        shift key state (perfBot uses then act as shift-use)
//   /zhh5 interact P TAG        P right-clicks the tagged entity with the main hand (Player.interactOn, fires Forge's EntityInteract)
//   /zhh5 horse TAG             shoe, speed and jump totals, base, on-ground flag, modifiers, effects, health, temper, age
//   /zhh5 hand P                P's main-hand id, count, damage, NBT
//   /zhh5 offer P ITEM          total of ITEM in P's inventory
//   /zhh5 break P X Y Z         survival block break by P (gameMode.destroyBlock)
//   /zhh5 throw P TICKS         release of the main-hand item after TICKS of charge (Item.releaseUsing)
//   /zhh5 hurt TAG cactus|berry 1 damage from that source; prints shod, the bare LivingAttackEvent result, health before and after
//   /zhh5 xp X Y Z R            total XP in orbs within R, then removes them
//   /zhh5 items X Y Z R         per-item totals of item entities within R, then removes them
//   /zhh5 potion ID             a potion's effects: id, duration, amplifier
//   /zhh5 loot BLOCK N [TOOL]   rolls the block's default state drops N times with TOOL (loot modifiers apply), per-item totals
// Output: "[zhh5] ..." lines in logs/latest.log, then "[zhh5] done".
(() => {
    const $ZStr = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $ZHand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $ZAttr = Java.loadClass('net.minecraft.world.entity.ai.attributes.Attributes')
    const $ZDmg = Java.loadClass('net.minecraft.world.damagesource.DamageSource')
    const $ZCTag = Java.loadClass('net.minecraft.nbt.CompoundTag')
    const $ZPos = Java.loadClass('net.minecraft.core.BlockPos')
    const $ZAABB = Java.loadClass('net.minecraft.world.phys.AABB')
    const $ZVec = Java.loadClass('net.minecraft.world.phys.Vec3')
    const $ZReg = Java.loadClass('net.minecraft.core.Registry')
    const $ZRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $ZStack = Java.loadClass('net.minecraft.world.item.ItemStack')
    const $ZBuilder = Java.loadClass('net.minecraft.world.level.storage.loot.LootContext$Builder')
    const $ZParams = Java.loadClass('net.minecraft.world.level.storage.loot.parameters.LootContextParams')
    const $ZItem = Java.loadClass('net.minecraft.world.entity.item.ItemEntity')
    const $ZOrb = Java.loadClass('net.minecraft.world.entity.ExperienceOrb')
    let $ZHooks = null
    try { $ZHooks = Java.loadClass('net.minecraftforge.common.ForgeHooks') } catch (err) { console.info('[zhh5] ForgeHooks not loadable: ' + err) }
    function out(s) { console.info('[zhh5] ' + s) }
    function player(server, name) { return server.getPlayerList().getPlayerByName(name) }
    function tagged(level, tag) {
        let hit = null
        level.getAllEntities().forEach(e => { if (hit == null && e.getTags().contains(tag)) hit = e })
        return hit
    }
    function box(x, y, z, r) { return new $ZAABB(x - r, y - r, z - r, x + r, y + r, z + r) }
    function run(server, a) {
        let level = server.overworld()
        let sub = a[0]
        if (sub == 'sneak') {
            let p = player(server, a[1])
            p.setShiftKeyDown(a[2] == 'on')
            out('sneak ' + p.isShiftKeyDown())
        } else if (sub == 'interact') {
            let p = player(server, a[1]), e = tagged(level, a[2])
            if (e == null) { out('no entity ' + a[2]); return }
            out('interact ' + p.interactOn(e, $ZHand.MAIN_HAND))
        } else if (sub == 'horse') {
            let h = tagged(level, a[1])
            if (h == null) { out('no entity ' + a[1]); return }
            let sp = h.getAttribute($ZAttr.MOVEMENT_SPEED)
            let mods = []
            sp.getModifiers().forEach(m => mods.push(m.getName() + '=' + m.getAmount()))
            let eff = []
            h.getActiveEffects().forEach(x => eff.push(String(x.getEffect().getDescriptionId()) + ':' + x.getDuration() + ':' + x.getAmplifier()))
            let jump = ''
            try { jump = h.getAttribute($ZAttr.JUMP_STRENGTH).getValue() } catch (err) { jump = 'none' }
            let temper = ''
            try { temper = h.getTemper() } catch (err) { temper = 'none' }
            let fd = h.saveWithoutId(new $ZCTag()).getCompound('ForgeData') // r1111: KubeJS getPersistentData is not Forge's tag
            out('shod=' + fd.contains('hearthandharvest:horseshoe') + ' speedBase=' + sp.getBaseValue() + ' speed=' + sp.getValue()
                + ' jump=' + jump + ' ground=' + h.isOnGround() + ' health=' + h.getHealth() + ' temper=' + temper + ' age=' + h.getAge() + ' mods=[' + mods.join(';') + '] effects=[' + eff.join(';') + ']')
        } else if (sub == 'hand') {
            let s = player(server, a[1]).getMainHandItem()
            out('hand id=' + s.getId() + ' count=' + s.getCount() + ' damage=' + s.getDamageValue() + ' nbt=' + s.nbt)
        } else if (sub == 'offer') {
            let inv = player(server, a[1]).getInventory(), n = 0
            for (let k = 0; k < inv.getContainerSize(); k++) if (String(inv.getItem(k).getId()) == a[2]) n += inv.getItem(k).getCount()
            out('offer ' + a[2] + '=' + n)
        } else if (sub == 'break') {
            out('break ' + player(server, a[1]).gameMode.destroyBlock(new $ZPos(a[2] * 1, a[3] * 1, a[4] * 1)))
        } else if (sub == 'throw') {
            let p = player(server, a[1]), s = p.getMainHandItem()
            s.getItem().releaseUsing(s, level, p, 72000 - a[2] * 1)
            out('throw done, hand count=' + p.getMainHandItem().getCount())
        } else if (sub == 'hurt') {
            let e = tagged(level, a[1])
            let before = e.getHealth()
            let src = a[2] == 'cactus' ? $ZDmg.CACTUS : $ZDmg.SWEET_BERRY_BUSH, hit
            // r1112: shod state and the bare Forge LivingAttackEvent result (false = a listener cancelled it), so a
            // damage that lands can be told apart from a probe path that skips the event
            let shodNow = e.saveWithoutId(new $ZCTag()).getCompound('ForgeData').contains('hearthandharvest:horseshoe'), ev
            try { ev = $ZHooks == null ? 'n/a' : $ZHooks.onLivingAttack(e, src, 1) } catch (err) { ev = 'err ' + err }
            out('hurt ' + a[2] + ' shod=' + shodNow + ' attackEventPasses=' + ev)
            try { hit = e.attack(src, 1) } catch (err) { hit = 'err ' + err } // r1111: Rhino finds neither 'hurt' nor m_6469_; KubeJS attack(DamageSource, float)
            out('hurt ' + a[2] + ' accepted=' + hit + ' before=' + before + ' after=' + e.getHealth())
        } else if (sub == 'xp') {
            let total = 0
            level.getEntitiesOfClass($ZOrb, box(a[1] * 1, a[2] * 1, a[3] * 1, a[4] * 1)).forEach(o => { total += o.getValue(); o.discard() })
            out('xp=' + total)
        } else if (sub == 'items') {
            let m = {}
            level.getEntitiesOfClass($ZItem, box(a[1] * 1, a[2] * 1, a[3] * 1, a[4] * 1)).forEach(i => {
                let k = String(i.getItem().getId())
                m[k] = (m[k] || 0) + i.getItem().getCount()
                i.discard()
            })
            out('items ' + JSON.stringify(m))
        } else if (sub == 'loot') {
            let block = $ZReg.BLOCK.get(new $ZRL(a[1])), n = a[2] * 1, m = {}
            let tool = a.length > 3 ? new $ZStack($ZReg.ITEM.get(new $ZRL(a[3]))) : $ZStack.EMPTY
            for (let k = 0; k < n; k++) {
                let b = new $ZBuilder(level).withRandom(level.random)
                    .withParameter($ZParams.ORIGIN, new $ZVec(0, 64, 0)).withParameter($ZParams.TOOL, tool)
                block.defaultBlockState().getDrops(b).forEach(s => {
                    let id = String(s.getId())
                    m[id] = (m[id] || 0) + s.getCount()
                })
            }
            out('loot ' + a[1] + ' n=' + n + ' ' + JSON.stringify(m))
        } else if (sub == 'potion') {
            let pot = $ZReg.POTION.get(new $ZRL(a[1])), eff = []
            pot.getEffects().forEach(x => eff.push(String(x.getEffect().getDescriptionId()) + ':' + x.getDuration() + ':' + x.getAmplifier()))
            out('potion ' + a[1] + ' effects=[' + eff.join(';') + ']')
        } else out('unknown ' + sub)
    }
    ServerEvents.commandRegistry(e => {
        e.register(e.commands.literal('zhh5').requires(s => s.hasPermission(2))
            .then(e.commands.argument('args', $ZStr.greedyString()).executes(c => {
                try { run(c.source.server, String($ZStr.getString(c, 'args')).split(' ')) }
                catch (err) { out('error ' + err) }
                out('done')
                return 1
            })))
    })
})()
