// Scratch-only probe for the Create: Power Grid port, phase P7 rows (SQ-PWG-55 onward, plan_power_grid.md P7:
// equipment). Copy into a scratch server's kubejs/server_scripts as zz_pwg_p7_probe.js next to the P2 to P6 probes
// (zpwg3: place, w, src, be, drop, count); never into the pack. Uses one Forge fake player ("[zpwg7]"), which never
// ticks (FakePlayer.tick is empty): the worn-battery tick and the drill's speed decay are called or read directly here,
// and their live hooks are client rows. No chunk loads here: `forceload add` the area first (Rhino deadlock).
//   check                     registries and glue: P7 blocks, items, entity, block entity; the lightning rod's Create
//                             movement behaviour; the zap damage source; Capacity on the battery; the fake player is a
//                             PlayerDrillExtensions (mixin applied); equipment config values
//   fp X Y Z [YAW PITCH]      put the fake player at X Y Z (survival, on ground, empty hands and chest)
//   hold ITEM                 main hand = 1 ITEM (fresh stack)
//   holdbat CHARGE [LEVEL]    main hand = portable battery with Charge CHARGE and Capacity enchant LEVEL (to place it)
//   wear CHARGE [LEVEL]       chest slot = portable battery with Charge CHARGE and Capacity enchant LEVEL (default 0)
//   bat                       worn battery: charge, max, output fraction (BatteryUtils), chest item
//   dig X Y Z                 break the block at X Y Z with the held item: dig speed (Player.getDigSpeed, the
//                             PlayerDrillMixin result), the item's destroy speed, correct tool, drill speed multiplier;
//                             then gameMode.destroyBlock: result, block after, charge and durability before / after
//   action X Y Z start|stop|abort   gameMode.handleBlockBreakAction (DrillBreakActionMixin), then the mining flag
//   baton R                   held Electro-Baton: hurtEnemy on the nearest living entity within R of the fake player;
//                             slowness amplifier / duration, charge and durability before / after, attribute modifiers
//   zap R                     held Electro-Zapper: aim at the nearest living entity within R and use the item; the new
//                             zap projectile (power, motion), charge and durability before / after, cooldown
//   hp X Y Z R                living entities within R: type, health / max, slowness
//   shear R                   held item on the nearest sheep within R (Sheep.mobInteract, SheepMixin): result, sheared,
//                             health before / after, string item entities within 3 blocks
//   place X Y Z               held portable battery (worn item) used on the top of the block below X Y Z (as a player
//                             places it); then the block entity
//   pbat X Y Z                portable battery block: charge, capacity level, max, comparator, terminals, clone stack
//   pick X Y Z                PortableBatteryBlock.use with the fake player (expected PASS: fake players do not pick up)
//   worn N                    call the worn tick (PortableBatteryItem.onWornTick) N times: held item FE and battery
//                             charge before / after
//   feitems                   up to 12 items whose stack has a Forge Energy capability (stored / capacity)
//   drops X Y Z R             item entities within R: id, count, battery Charge and Capacity level
//   sneak on|off              fake player sneaking (the saw fells the whole tree when sneaking)
// Output: "[zpwg7] ..." lines in logs/latest.log, then "[zpwg7] done".
(() => {
    const $Args = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $FR = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $RL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $Pos = Java.loadClass('net.minecraft.core.BlockPos')
    const $Dir = Java.loadClass('net.minecraft.core.Direction')
    const $AABB = Java.loadClass('net.minecraft.world.phys.AABB')
    const $Vec3 = Java.loadClass('net.minecraft.world.phys.Vec3')
    const $BHR = Java.loadClass('net.minecraft.world.phys.BlockHitResult')
    const $UseOn = Java.loadClass('net.minecraft.world.item.context.UseOnContext')
    const $Hand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $Slot = Java.loadClass('net.minecraft.world.entity.EquipmentSlot')
    const $ItemStack = Java.loadClass('net.minecraft.world.item.ItemStack')
    const $ItemEntity = Java.loadClass('net.minecraft.world.entity.item.ItemEntity')
    const $Living = Java.loadClass('net.minecraft.world.entity.LivingEntity')
    const $Sheep = Java.loadClass('net.minecraft.world.entity.animal.Sheep')
    const $Effects = Java.loadClass('net.minecraft.world.effect.MobEffects')
    const $Attributes = Java.loadClass('net.minecraft.world.entity.ai.attributes.Attributes')
    const $Anchor = Java.loadClass('net.minecraft.commands.arguments.EntityAnchorArgument$Anchor')
    const $Action = Java.loadClass('net.minecraft.network.protocol.game.ServerboundPlayerActionPacket$Action')
    const $GameType = Java.loadClass('net.minecraft.world.level.GameType')
    const $EnchHelper = Java.loadClass('net.minecraft.world.item.enchantment.EnchantmentHelper')
    const $Caps = Java.loadClass('net.minecraftforge.common.capabilities.ForgeCapabilities')
    const $Fake = Java.loadClass('net.minecraftforge.common.util.FakePlayerFactory')
    const $Profile = Java.loadClass('com.mojang.authlib.GameProfile')
    const $UUID = Java.loadClass('java.util.UUID')
    const $Blocks = Java.loadClass('net.minecraft.world.level.block.Blocks')
    const $AllEnch = Java.loadClass('com.simibubi.create.AllEnchantments')
    const $AMB = Java.loadClass('com.simibubi.create.AllMovementBehaviours')
    const $BEB = Java.loadClass('com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour')
    const $List = Java.loadClass('java.util.ArrayList')
    const $TagParser = Java.loadClass('net.minecraft.nbt.TagParser')
    const PROFILE = new $Profile($UUID.fromString('5a7c0e57-0b0e-4a11-9d51-70e9a1c0ffee'), '[zpwg7]')
    let PG = null
    function pg() {
        if (PG === null) PG = {
            Configs: Java.loadClass('org.patryk3211.powergrid.collections.ModdedConfigs'),
            Damage: Java.loadClass('org.patryk3211.powergrid.collections.ModdedDamageTypes'),
            BatteryUtils: Java.loadClass('org.patryk3211.powergrid.equipment.portablebattery.BatteryUtils'),
            BatteryItem: Java.loadClass('org.patryk3211.powergrid.equipment.portablebattery.PortableBatteryItem'),
            BatteryBE: Java.loadClass('org.patryk3211.powergrid.equipment.portablebattery.PortableBatteryBlockEntity'),
            DrillExt: Java.loadClass('org.patryk3211.powergrid.equipment.drill.PlayerDrillExtensions'),
            Zap: Java.loadClass('org.patryk3211.powergrid.equipment.zapper.ZapProjectileEntity'),
            Electric: Java.loadClass('org.patryk3211.powergrid.electricity.base.ElectricBehaviour'),
        }
        return PG
    }
    function out(s) { console.info('[zpwg7] ' + s) }
    function gtime(level) {
        try { return level.getLevelData().getGameTime() } catch (e1) {}
        return 'ms' + Date.now()
    }
    function pos(x, y, z) { return new $Pos(x, y, z) }
    function fmt(x) { return (Math.round(x * 1000) / 1000) }
    function idOf(stack) { return String($FR.ITEMS.getKey(stack.getItem())) }
    // entity id: KubeJS remaps Entity.getType() to the id String in Rhino, so ENTITY_TYPES.getKey(e.getType()) fell back
    // to the registry default minecraft:pig for every mob (r1113 pwg10b); the save id is exact (null only for players)
    function eid(e) { let id = e.getEncodeId(); return String(id != null ? id : e.getType()) }
    function blockId(state) { return String($FR.BLOCKS.getKey(state.getBlock())) }
    function item(id) { return $FR.ITEMS.getValue(new $RL(id)) }
    function fake(level) { return $Fake.get(level, PROFILE) }
    function dur(stack) { return stack.isDamageableItem() ? (stack.getMaxDamage() - stack.getDamageValue()) + '/' + stack.getMaxDamage() : 'n/a' }
    // getMaxCharge is overloaded (int, ItemStack): same arity, unrelated types; pick by signature (r1113 Rhino gotcha)
    function maxAt(lvl) { return pg().BatteryUtils['getMaxCharge(int)'](lvl) }
    function maxOf(s) { return pg().BatteryUtils['getMaxCharge(net.minecraft.world.item.ItemStack)'](s) }
    function chargeOf(p) {
        let s = p.getItemBySlot($Slot.CHEST)
        if (!(s.getItem() instanceof pg().BatteryItem)) return 'none'
        return pg().BatteryUtils.getCurrentCharge(s) + '/' + maxOf(s)
    }
    function mining(p) {
        if (!(p instanceof pg().DrillExt)) return 'no-mixin'
        return 'mining=' + p.powerGrid$isMining() + ' speedMult=' + fmt(p.powerGrid$drillSpeedMultiplier())
    }
    function nearest(level, p, r, cls) {
        let list = level.getEntitiesOfClass(cls, new $AABB(p.getX() - r, p.getY() - r, p.getZ() - r, p.getX() + r, p.getY() + r, p.getZ() + r))
        let best = null, bd = 1e9
        list.forEach(e => {
            if (e == p || !e.isAlive()) return
            let dx = e.getX() - p.getX(), dy = e.getY() - p.getY(), dz = e.getZ() - p.getZ()
            let d = dx * dx + dy * dy + dz * dz
            if (d < bd) { bd = d; best = e }
        })
        return best
    }
    function slow(e) {
        let inst = e.getEffect($Effects.MOVEMENT_SLOWDOWN)
        return inst == null ? 'none' : ('amp=' + inst.getAmplifier() + ' dur=' + inst.getDuration())
    }
    function check(level) {
        for (let id of ['portable_battery', 'portable_battery_placeable', 'portable_drill', 'portable_saw', 'electrobaton', 'electrozapper'])
            out('item powergrid:' + id + ' registered=' + $FR.ITEMS.containsKey(new $RL('powergrid', id)))
        out('block powergrid:portable_battery registered=' + $FR.BLOCKS.containsKey(new $RL('powergrid', 'portable_battery'))
            + ' blockEntity=' + $FR.BLOCK_ENTITY_TYPES.containsKey(new $RL('powergrid', 'portable_battery'))
            + ' entity zap_projectile=' + $FR.ENTITY_TYPES.containsKey(new $RL('powergrid', 'zap_projectile')))
        let mb = $AMB.getBehaviour($Blocks.LIGHTNING_ROD.defaultBlockState())
        out('lightning_rod movement behaviour=' + (mb == null ? 'none' : mb.getClass().getName()))
        out('damage source zap msgId=' + pg().Damage.ZAP.messageId)
        let bat = item('powergrid:portable_battery').getDefaultInstance()
        out('capacity enchant canEnchant(portable_battery)=' + $AllEnch.CAPACITY.get().canEnchant(bat)
            + ' max level=' + $AllEnch.CAPACITY.get().getMaxLevel())
        out('battery maxCharge level0=' + maxAt(0) + ' level1=' + maxAt(1)
            + ' level3=' + maxAt(3))
        let p = fake(level)
        out('fake player PlayerDrillExtensions=' + (p instanceof pg().DrillExt))
        let e = pg().Configs.server().equipment
        out('config drillEnergyPerUse=' + e.drillEnergyPerUse.get() + ' drillMineSpeedBase=' + fmt(e.drillMineSpeedBase.get())
            + ' drillMineSpeedBulk=' + fmt(e.drillMineSpeedBulk.get()) + ' sawEnergyPerUse=' + e.sawEnergyPerUse.get()
            + ' sawMineSpeed=' + fmt(e.sawMineSpeed.get()) + ' batonEnergy=' + e.electroBatonEnergyPerUse.get()
            + ' zapperEnergy=' + e.electroZapperEnergyPerShot.get() + ' batteryBase=' + e.portableBatteryBaseCapacity.get()
            + ' batteryEnchant=' + e.portableBatteryEnchantCapacity.get())
        out('config forgeEnergyPerVolt=' + fmt(pg().Configs.server().electricity.forgeEnergyPerVolt.get()))
    }
    function fp(level, a) {
        let p = fake(level)
        p.setGameMode($GameType.SURVIVAL)
        // moveTo(5 numbers) resolves to the 3-argument teleportTo (m_6027_) in Rhino and throws (r1113): absMoveTo, then
        // teleportTo plus the rotation setters as a fallback
        let mx = parseFloat(a[1]) + 0.5, my = parseFloat(a[2]), mz = parseFloat(a[3]) + 0.5
        let myaw = a.length > 4 ? parseFloat(a[4]) : 0, mpitch = a.length > 5 ? parseFloat(a[5]) : 0
        try { p.absMoveTo(mx, my, mz, myaw, mpitch) } catch (e1) {
            p.teleportTo(mx, my, mz)
            try { p.setYRot(myaw); p.setXRot(mpitch); p.setYHeadRot(myaw) } catch (e2) {}
        }
        p.setOnGround(true)
        p.setShiftKeyDown(false)
        p.getInventory().clearContent()
        p.setItemSlot($Slot.CHEST, $ItemStack.EMPTY)
        out('fp at ' + fmt(p.getX()) + ' ' + fmt(p.getY()) + ' ' + fmt(p.getZ()) + ' mode=' + p.gameMode.getGameModeForPlayer() + ' ' + mining(p))
    }
    // built from SNBT: Rhino does not see ItemStack.getTag here, and KubeJS's enchant returns a copy (gotchas)
    function batteryStack(charge, lvl) {
        let snbt = '{id:"powergrid:portable_battery",Count:1b,tag:{Charge:' + charge + (lvl > 0 ? ',Enchantments:[{id:"create:capacity",lvl:' + lvl + 's}]' : '') + '}}'
        return $ItemStack.of($TagParser.parseTag(snbt))
    }
    function wear(level, charge, lvl) {
        let p = fake(level)
        p.setItemSlot($Slot.CHEST, batteryStack(charge, lvl))
        out('wear charge=' + chargeOf(p) + ' capacityLevel=' + $EnchHelper.getItemEnchantmentLevel($AllEnch.CAPACITY.get(), p.getItemBySlot($Slot.CHEST)))
    }
    function bat(level) {
        let p = fake(level)
        let s = p.getItemBySlot($Slot.CHEST)
        if (!(s.getItem() instanceof pg().BatteryItem)) { out('bat none (chest ' + idOf(s) + ')'); return }
        out('bat charge=' + chargeOf(p) + ' output=' + fmt(pg().BatteryUtils.tryDrawEnergy(s, 1)) + ' t=' + gtime(level))
    }
    function dig(level, P) {
        let p = fake(level)
        let st = level.getBlockState(P)
        let held = p.getMainHandItem()
        out('dig ' + blockId(st) + ' with ' + idOf(held) + ' digSpeed=' + fmt(p.getDigSpeed(st, P)) + ' itemSpeed=' + fmt(held.getDestroySpeed(st))
            + ' correctTool=' + held.isCorrectToolForDrops(st) + ' hasCorrectTool=' + p.hasCorrectToolForDrops(st)
            + ' charge=' + chargeOf(p) + ' dur=' + dur(held) + ' ' + mining(p))
        let ok = p.gameMode.destroyBlock(P)
        out('dig result=' + ok + ' after=' + blockId(level.getBlockState(P)) + ' charge=' + chargeOf(p) + ' dur=' + dur(p.getMainHandItem())
            + ' ' + mining(p) + ' digSpeedNow=' + fmt(p.getDigSpeed(st, P)) + ' t=' + gtime(level))
    }
    function action(level, P, name) {
        let p = fake(level)
        let act = name == 'start' ? $Action.START_DESTROY_BLOCK : (name == 'stop' ? $Action.STOP_DESTROY_BLOCK : $Action.ABORT_DESTROY_BLOCK)
        p.gameMode.handleBlockBreakAction(P, act, $Dir.UP, level.getMaxBuildHeight(), 0)
        out('action ' + act + ' held=' + idOf(p.getMainHandItem()) + ' ' + mining(p))
    }
    function baton(level, r) {
        let p = fake(level)
        let held = p.getMainHandItem()
        let t = nearest(level, p, r, $Living)
        if (t == null) { out('baton: no target within ' + r); return }
        let mods = []
        held.getAttributeModifiers($Slot.MAINHAND).entries().forEach(en => mods.push(String(en.getKey().getDescriptionId()).replace('attribute.name.', '') + '=' + fmt(en.getValue().getAmount())))
        out('baton ' + idOf(held) + ' modifiers ' + mods.join(' '))
        let c0 = chargeOf(p), d0 = dur(held)
        let res = held.getItem().hurtEnemy(held, t, p)
        out('baton target=' + eid(t) + ' maxHealth=' + fmt(t.getMaxHealth()) + ' result=' + res
            + ' slowness ' + slow(t) + ' charge ' + c0 + ' -> ' + chargeOf(p) + ' dur ' + d0 + ' -> ' + dur(p.getMainHandItem()))
    }
    function zap(level, r) {
        let p = fake(level)
        let held = p.getMainHandItem()
        let t = nearest(level, p, r, $Living)
        if (t == null) { out('zap: no target within ' + r); return }
        p.lookAt($Anchor.EYES, new $Vec3(t.getX(), t.getY() + t.getBbHeight() / 2, t.getZ()))
        let before = level.getEntitiesOfClass(pg().Zap, p.getBoundingBox().inflate(8)).size()
        let c0 = chargeOf(p), d0 = dur(held)
        let res
        try { res = String(held.use(level, p, $Hand.MAIN_HAND).getResult()) } catch (err) { res = 'error ' + err }
        let zaps = level.getEntitiesOfClass(pg().Zap, p.getBoundingBox().inflate(8))
        out('zap target=' + eid(t) + ' health=' + fmt(t.getHealth()) + '/' + fmt(t.getMaxHealth())
            + ' use=' + res + ' projectiles ' + before + ' -> ' + zaps.size() + ' charge ' + c0 + ' -> ' + chargeOf(p)
            + ' dur ' + d0 + ' -> ' + dur(p.getMainHandItem()) + ' cooldown=' + p.getCooldowns().isOnCooldown(held.getItem()) + ' t=' + gtime(level))
        zaps.forEach(z => {
            let tag = new (Java.loadClass('net.minecraft.nbt.CompoundTag'))()
            z.saveWithoutId(tag)
            let m = z.getDeltaMovement()
            out('zap projectile power=' + fmt(tag.getFloat('Power')) + ' motion=' + fmt(m.x()) + ',' + fmt(m.y()) + ',' + fmt(m.z()))
        })
    }
    function hp(level, P, r) {
        let list = level.getEntitiesOfClass($Living, new $AABB(P.getX() - r, P.getY() - r, P.getZ() - r, P.getX() + r + 1, P.getY() + r + 1, P.getZ() + r + 1))
        out('hp ' + list.size() + ' t=' + gtime(level))
        list.forEach(e => out('hp ' + eid(e) + ' ' + fmt(e.getHealth()) + '/' + fmt(e.getMaxHealth())
            + ' slowness ' + slow(e) + ' at ' + fmt(e.getX()) + ' ' + fmt(e.getY()) + ' ' + fmt(e.getZ())))
    }
    function strings(level, e) {
        let n = 0
        level.getEntitiesOfClass($ItemEntity, e.getBoundingBox().inflate(3)).forEach(i => { if (idOf(i.getItem()) == 'minecraft:string') n += i.getItem().getCount() })
        return n
    }
    function shear(level, r) {
        let p = fake(level)
        let s = nearest(level, p, r, $Sheep)
        if (s == null) { out('shear: no sheep within ' + r); return }
        let h0 = s.getHealth(), sh0 = s.isSheared(), str0 = strings(level, s)
        let res = String(s.mobInteract(p, $Hand.MAIN_HAND))
        out('shear with ' + idOf(p.getMainHandItem()) + ' result=' + res + ' sheared ' + sh0 + ' -> ' + s.isSheared()
            + ' health ' + fmt(h0) + ' -> ' + fmt(s.getHealth()) + ' strings ' + str0 + ' -> ' + strings(level, s) + ' t=' + gtime(level))
    }
    function place(level, P) {
        let p = fake(level)
        let held = p.getMainHandItem()
        // read the id before useOn: the placed stack shrinks to empty and reads as minecraft:air (r1113 pwg10b)
        let heldId = idOf(held)
        let below = P.below()
        let hit = new $BHR(new $Vec3(below.getX() + 0.5, below.getY() + 1, below.getZ() + 0.5), $Dir.UP, below, false)
        let res = held.useOn(new $UseOn(p, $Hand.MAIN_HAND, hit))
        out('place ' + heldId + ' result=' + res + ' block=' + blockId(level.getBlockState(P)) + ' heldNow=' + idOf(p.getMainHandItem()) + 'x' + p.getMainHandItem().getCount())
        pbat(level, P)
    }
    function pbat(level, P) {
        let be = level.getBlockEntity(P)
        if (be == null || !(be instanceof pg().BatteryBE)) { out('pbat none at ' + P + ' (' + blockId(level.getBlockState(P)) + ')'); return }
        let tag = be.saveWithoutMetadata()
        let clone = level.getBlockState(P).getBlock().getCloneItemStack(level, P, level.getBlockState(P))
        out('pbat charge=' + tag.getInt('Charge') + ' capacityLevel=' + tag.getInt('CapacityLevel') + ' comparator=' + be.getComparatorOutput()
            + ' clone=' + idOf(clone) + ' cloneCharge=' + pg().BatteryUtils.getCurrentCharge(clone) + ' t=' + gtime(level))
        let eb = $BEB.get(be, pg().Electric.TYPE)
        if (eb != null) {
            let vs = []
            for (let i = 0; i < 2; i++) if (eb.hasTerminal(i)) vs.push('T' + i + '=' + fmt(eb.getTerminal(i).getVoltage()))
            out('pbat terminals ' + vs.join(' '))
        }
    }
    function pick(level, P) {
        let p = fake(level)
        let st = level.getBlockState(P)
        let hit = new $BHR(new $Vec3(P.getX() + 0.5, P.getY() + 0.5, P.getZ() + 0.5), $Dir.UP, P, false)
        let res = st.use(level, p, $Hand.MAIN_HAND, hit)
        out('pick result=' + res + ' block=' + blockId(level.getBlockState(P)) + ' chest=' + idOf(p.getItemBySlot($Slot.CHEST)))
    }
    function fe(stack) {
        let o = stack.getCapability($Caps.ENERGY).resolve()
        if (!o.isPresent()) return 'no-fe'
        let h = o.get()
        return h.getEnergyStored() + '/' + h.getMaxEnergyStored()
    }
    function worn(level, n) {
        let p = fake(level)
        let chest = p.getItemBySlot($Slot.CHEST)
        if (!(chest.getItem() instanceof pg().BatteryItem)) { out('worn: no battery worn'); return }
        let held = p.getMainHandItem()
        let f0 = fe(held), c0 = chargeOf(p)
        for (let i = 0; i < n; i++) chest.getItem().onWornTick(chest, p)
        out('worn x' + n + ' held=' + idOf(held) + ' fe ' + f0 + ' -> ' + fe(p.getMainHandItem()) + ' charge ' + c0 + ' -> ' + chargeOf(p))
    }
    function feitems() {
        let found = []
        $FR.ITEMS.getValues().forEach(it => {
            if (found.length >= 12) return
            try {
                let r = fe(it.getDefaultInstance())
                if (r != 'no-fe') found.push(String($FR.ITEMS.getKey(it)) + '(' + r + ')')
            } catch (err) {}
        })
        out('feitems ' + found.length + ': ' + found.join(' '))
    }
    function drops(level, P, r) {
        let list = level.getEntitiesOfClass($ItemEntity, new $AABB(P.getX() - r, P.getY() - r, P.getZ() - r, P.getX() + r + 1, P.getY() + r + 1, P.getZ() + r + 1))
        out('drops ' + list.size())
        list.forEach(e => {
            let s = e.getItem()
            out('drop ' + idOf(s) + ' x' + s.getCount() + (s.getItem() instanceof pg().BatteryItem ? ' Charge=' + pg().BatteryUtils.getCurrentCharge(s) + ' capacityLevel=' + $EnchHelper.getItemEnchantmentLevel($AllEnch.CAPACITY.get(), s) : ''))
        })
    }
    function run(level, a) {
        let cmd = a[0]
        if (cmd == 'check') { check(level); return }
        if (cmd == 'fp') { fp(level, a); return }
        if (cmd == 'hold') { fake(level).setItemInHand($Hand.MAIN_HAND, item(a[1]).getDefaultInstance()); out('hold ' + idOf(fake(level).getMainHandItem())); return }
        if (cmd == 'holdbat') { let hs = batteryStack(parseInt(a[1]), a.length > 2 ? parseInt(a[2]) : 0); fake(level).setItemInHand($Hand.MAIN_HAND, hs); out('holdbat charge=' + pg().BatteryUtils.getCurrentCharge(fake(level).getMainHandItem())); return }
        if (cmd == 'wear') { wear(level, parseInt(a[1]), a.length > 2 ? parseInt(a[2]) : 0); return }
        if (cmd == 'bat') { bat(level); return }
        if (cmd == 'baton') { baton(level, parseFloat(a[1])); return }
        if (cmd == 'zap') { zap(level, parseFloat(a[1])); return }
        if (cmd == 'shear') { shear(level, parseFloat(a[1])); return }
        if (cmd == 'worn') { worn(level, parseInt(a[1])); return }
        if (cmd == 'feitems') { feitems(); return }
        if (cmd == 'sneak') { fake(level).setShiftKeyDown(a[1] == 'on'); out('sneak ' + fake(level).isShiftKeyDown()); return }
        let P = pos(parseInt(a[1]), parseInt(a[2]), parseInt(a[3]))
        if (cmd == 'dig') dig(level, P)
        else if (cmd == 'action') action(level, P, a[4])
        else if (cmd == 'hp') hp(level, P, parseInt(a[4]))
        else if (cmd == 'place') place(level, P)
        else if (cmd == 'pbat') pbat(level, P)
        else if (cmd == 'pick') pick(level, P)
        else if (cmd == 'drops') drops(level, P, parseInt(a[4]))
        else out('unknown subcommand ' + cmd)
    }
    ServerEvents.commandRegistry(e => {
        const C = e.commands
        e.register(C.literal('zpwg7').requires(s => s.hasPermission(2))
            .then(C.argument('args', $Args.greedyString()).executes(ctx => {
                try {
                    run(ctx.source.getLevel(), String($Args.getString(ctx, 'args')).split(' '))
                } catch (err) { out('error ' + err) }
                out('done')
                return 1
            })))
    })
})()
