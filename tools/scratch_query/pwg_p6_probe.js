// Scratch-only probe for the Create: Power Grid port, phase P6 rows (SQ-PWG-46 onward, plan_power_grid.md P6: heat,
// light, fluid, solar). Copy into a scratch server's kubejs/server_scripts as zz_pwg_p6_probe.js next to the P2 to P5
// probes (zpwg3: place, w, src, res, scroll, be; zpwg4: kin); never into the pack. Reads server-side values only.
// No chunk loads here: `forceload add` the area first (Rhino deadlock, scratch-test gotchas).
//   check                     registries and glue: fan processing types in priority order (id, priority; the two
//                             heater types and the pack's creativeengineering:dragon_breathing), the solar panel
//                             contraption type, solar biome overrides loaded, P6 config values, then loads the six mixin
//                             target classes ("mixin target <class> loaded")
//   fan X Y Z                 air current source at X Y Z (electric fan or Create encased fan): speed, flow direction,
//                             reach, the processing type at each block of the stream
//   fantype X Y Z             FanProcessingType.getAt for the block at X Y Z (the catalyst a stream would see there)
//   heater X Y Z              heating coil (state, temperature) or basin heater (heat level, temperature, boiler heat,
//                             the basin's view of the heat level)
//   items X Y Z R             item entities within R blocks: id x count, position, and Create's fan processing data
//                             (type and remaining time) when the item is being processed (read from the saved entity:
//                             ForgeData/CreateData; KubeJS's getPersistentData is its own tag, r1113 pwg10)
//   chute X Y Z               Create chute item motion (pull / push from a fan below / above)
//   light X Y Z               light fixture or factory light: power level, bulb (item, temperature, burned, colour),
//                             the fixture's own light emission (block state), block light at the fixture and, for a
//                             factory light, the light blocks below it
//   bulb X Y Z ITEM           put bulb ITEM into the fixture at X Y Z (through the block entity's saved data, as a
//                             loaded bulb), or `bulb X Y Z none` to empty it
//   solar X Y Z               solar panel or solar panel bearing: goggle lines, terminals, the biome override entry
//                             that applies there, sky light, day time, rain
//   solarjoin X Y Z X2 Y2 Z2  merge the panel multiblock at 2 into the one at X Y Z (what the wrench / placement helper
//                             does; panels placed by command stay single)
//   assemble X Y Z            solar panel bearing: assemble its contraption now (as on a redstone signal), then print
//                             its last AssemblyException (none when it assembled), running, the block above
//   pump X Y Z                electric pump: speed, terminals, goggle lines
//   tank X Y Z                fluid in the fluid handler at X Y Z (Create tank, basin)
//   fill X Y Z FLUID MB       fill the fluid handler at X Y Z (unsided) with MB of FLUID; prints the stack amount, the
//                             handler class and isFluidValid, and when the capability takes 0 fills a Create tank's
//                             own inventory (its controller's) instead
//   insert X Y Z ITEM N       insert N of ITEM into the item handler at X Y Z (unsided; basin, depot, chute)
//   inv X Y Z                 contents of the item handler at X Y Z
//   fd X Y Z                  Farmer's Delight cooking pot or skillet: isHeated(), isCooking() (skillet), stored item
//   skillet X Y Z ITEM        put ITEM on the skillet through addItemToCook (as a player would, without the player)
//   age X Y Z R               crop ages within R (horizontal) on the layer Y: count per age, mean age
//   contraption X Y Z R       contraption entities within R: entity type, the saved contraption Type, block count, and
//                             a count per entity type of every entity in the box
//   contraptions all [X Y Z]  level-wide scan of create:controlled_contraption and stationary_contraption entities (id, uuid, position, isRemoved,
//                             alive, contraption Type, blocks, saved Controller pos); with X Y Z a solar panel bearing:
//                             its movedContraption (null or set: field read, else isAttachedTo(null) and against
//                             each entity found; the entity id when known), running
// Output: "[zpwg6] ..." lines in logs/latest.log, then "[zpwg6] done".
(() => {
    const $Args = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $FR = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $RL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $Pos = Java.loadClass('net.minecraft.core.BlockPos')
    const $AABB = Java.loadClass('net.minecraft.world.phys.AABB')
    const $ItemEntity = Java.loadClass('net.minecraft.world.entity.item.ItemEntity')
    const $Entity = Java.loadClass('net.minecraft.world.entity.Entity')
    const $ItemStack = Java.loadClass('net.minecraft.world.item.ItemStack')
    const $Compound = Java.loadClass('net.minecraft.nbt.CompoundTag')
    const $LightLayer = Java.loadClass('net.minecraft.world.level.LightLayer')
    const $Caps = Java.loadClass('net.minecraftforge.common.capabilities.ForgeCapabilities')
    const $FluidStack = Java.loadClass('net.minecraftforge.fluids.FluidStack')
    const $FluidAction = Java.loadClass('net.minecraftforge.fluids.capability.IFluidHandler$FluidAction')
    const $BEB = Java.loadClass('com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour')
    const $Goggles = Java.loadClass('com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation')
    const $FanType = Java.loadClass('com.simibubi.create.content.kinetics.fan.processing.FanProcessingType')
    const $FanReg = Java.loadClass('com.simibubi.create.content.kinetics.fan.processing.FanProcessingTypeRegistry')
    const $AirSource = Java.loadClass('com.simibubi.create.content.kinetics.fan.IAirCurrentSource')
    const $Chute = Java.loadClass('com.simibubi.create.content.logistics.chute.ChuteBlockEntity')
    const $Boiler = Java.loadClass('com.simibubi.create.content.fluids.tank.BoilerHeaters')
    const $Basin = Java.loadClass('com.simibubi.create.content.processing.basin.BasinBlockEntity')
    const $CType = Java.loadClass('com.simibubi.create.content.contraptions.ContraptionType')
    const $List = Java.loadClass('java.util.ArrayList')
    let PG = null
    function pg() {
        if (PG === null) PG = {
            Electric: Java.loadClass('org.patryk3211.powergrid.electricity.base.ElectricBehaviour'),
            Thermal: Java.loadClass('org.patryk3211.powergrid.electricity.base.ThermalBehaviour'),
            Heater: Java.loadClass('org.patryk3211.powergrid.electricity.heater.HeaterBlockEntity'),
            BasinHeater: Java.loadClass('org.patryk3211.powergrid.electricity.basinheater.BasinHeaterBlockEntity'),
            Fan: Java.loadClass('org.patryk3211.powergrid.electricity.fan.ElectricFanBlockEntity'),
            Pump: Java.loadClass('org.patryk3211.powergrid.electricity.pump.ElectricPumpBlockEntity'),
            Fixture: Java.loadClass('org.patryk3211.powergrid.electricity.light.fixture.AbstractLightFixtureBlockEntity'),
            Factory: Java.loadClass('org.patryk3211.powergrid.electricity.light.factorylight.FactoryLightBlockEntity'),
            Solar: Java.loadClass('org.patryk3211.powergrid.electricity.solarpanel.SolarPanelBlockEntity'),
            Bearing: Java.loadClass('org.patryk3211.powergrid.electricity.solarpanel.SolarPanelBearingBlockEntity'),
            SolarReg: Java.loadClass('org.patryk3211.powergrid.electricity.solarpanel.registry.SolarBiomeRegistry'),
            Configs: Java.loadClass('org.patryk3211.powergrid.collections.ModdedConfigs'),
        }
        return PG
    }
    let FD = null
    function fd() {
        if (FD === null) FD = {
            Pot: Java.loadClass('vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity'),
            Skillet: Java.loadClass('vectorwing.farmersdelight.common.block.entity.SkilletBlockEntity'),
        }
        return FD
    }
    // ENTITY_TYPES.getKey(e.getType()) returned minecraft:pig for every entity in Rhino (r1113 pwg10b): use getEncodeId
    function eidOf(e) {
        try { let k = e.getEncodeId(); if (k != null) return String(k) } catch (e1) {}
        try { return String(e.getType().toShortString()) } catch (e2) {}
        return String($FR.ENTITY_TYPES.getKey(e.getType()))
    }
    function out(s) { console.info('[zpwg6] ' + s) }
    // game time: ServerLevel.getGameTime is not reachable from Rhino here (r1113, as in pwg_p3_probe.js)
    function gtime(level) {
        try { return level.getLevelData().getGameTime() } catch (e1) {}
        try { return level.getServer().getTickCount() } catch (e2) {}
        return 'ms' + Date.now()
    }
    function dtime(level) {
        try { return level.getLevelData().getDayTime() % 24000 } catch (e1) {}
        try { return level.getDayTime() % 24000 } catch (e2) {}
        return '?'
    }
    function pos(x, y, z) { return new $Pos(x, y, z) }
    function fmt(x) { return (Math.round(x * 1000) / 1000) }
    function simpleName(o) { let s = String(o); let m = s.match(/([A-Za-z0-9_$.]+)@[0-9a-f]+$/); return m ? m[1].replace(/^.*[.$]/, '') : s }
    function idOf(stack) { return String($FR.ITEMS.getKey(stack.getItem())) }
    function blockId(state) { return String($FR.BLOCKS.getKey(state.getBlock())) }
    function prop(state, name) {
        let v = null
        state.getProperties().forEach(p => { if (p.getName() == name) v = String(state.getValue(p)) })
        return v
    }
    function typeId(t) {
        if (t == null) return 'none'
        try { return String($FanReg.getId(t)) } catch (err) { return 'unregistered:' + t }
    }
    function terminals(be) {
        let eb = $BEB.get(be, pg().Electric.TYPE)
        if (eb == null) return
        let vs = []
        for (let i = 0; i < 8; i++) if (eb.hasTerminal(i)) vs.push('T' + i + '=' + fmt(eb.getTerminal(i).getVoltage()))
        out('terminals ' + vs.join(' '))
    }
    function thermal(be) {
        let th = $BEB.get(be, pg().Thermal.TYPE)
        if (th != null) out('thermal T=' + fmt(th.getTemperature()) + ' overheated=' + th.isOverheated())
    }
    function goggles(be) {
        if (!(be instanceof $Goggles)) return
        let lines = new $List()
        try {
            be.addToGoggleTooltip(lines, true)
            lines.forEach(c => out('goggles ' + String(c.getString()).trim()))
        } catch (err) { out('goggles skipped (client-only tooltip)') }
    }
    function check() {
        let sorted = $FanReg.getSortedTypesView()
        let parts = []
        sorted.forEach(t => parts.push(typeId(t) + '(' + t.getPriority() + ')'))
        out('fan types ' + sorted.size() + ': ' + parts.join(' '))
        for (let id of ['powergrid:heater_blasting', 'powergrid:heater_smoking', 'creativeengineering:dragon_breathing'])
            out('fan type ' + id + ' registered=' + ($FanReg.getType(new $RL(id)) != null))
        try { out('contraption type powergrid:solar_panel registered=' + $CType.ENTRIES.containsKey('powergrid:solar_panel')) }
        catch (err) { out('contraption type error ' + err) }
        try { out('solar biome overrides ' + pg().SolarReg.size()) } catch (err) { out('solar overrides error ' + err) }
        try {
            let e = pg().Configs.server().electricity
            out('config heaterFanProcessingSpeedMultiplier=' + fmt(e.heaterFanProcessingSpeedMultiplier.get())
                + ' growthLampRadius=' + e.growthLampRadius.get() + ' growthLampChance=' + e.growthLampChance.get()
                + ' electricPumpRange=' + e.electricPumpRange.get() + ' solarPanelMaxSize=' + e.solarPanelMaxSize.get())
            out('config clientMirror=' + pg().Configs.common().clientMirror.get())
        } catch (err) { out('config error ' + err) }
        // the six P6 mixins apply when their target class loads: load each now so a failed injection shows here
        for (let cn of ['com.simibubi.create.content.logistics.chute.ChuteBlockEntity',
            'com.simibubi.create.content.kinetics.fan.processing.FanProcessing',
            'com.simibubi.create.content.fluids.FluidPropagator',
            'com.simibubi.create.content.processing.basin.BasinRecipe',
            'vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity',
            'vectorwing.farmersdelight.common.block.entity.SkilletBlockEntity']) {
            try { Java.loadClass(cn); out('mixin target ' + cn + ' loaded') } catch (err) { out('mixin target ' + cn + ' error ' + err) }
        }
    }
    // FanProcessingType.getAt is a static interface method; if Rhino cannot reach it, the same priority walk by hand
    function fanTypeAt(level, p) {
        try { return $FanType.getAt(level, p) } catch (err) {}
        let r = null
        $FanReg.getSortedTypesView().forEach(t => { if (r == null && t.isValidAt(level, p)) r = t })
        return r
    }
    function fan(level, p) {
        let be = level.getBlockEntity(p)
        if (be == null || !(be instanceof $AirSource)) { out('fan none at ' + p + ' (' + blockId(level.getBlockState(p)) + ')'); return }
        let ac = be.getAirCurrent()
        let dir = be.getAirFlowDirection()
        out('fan ' + blockId(level.getBlockState(p)) + ' speed=' + fmt(be.getSpeed()) + ' flow=' + (dir == null ? 'none' : dir)
            + ' reach=' + fmt(ac.maxDistance) + ' segments=' + ac.segments.size() + ' t=' + gtime(level))
        let types = []
        for (let i = 0; i < Math.ceil(ac.maxDistance); i++) types.push(i + ':' + typeId(ac.getTypeAt(i + 0.5)))
        out('fan stream ' + types.join(' '))
        if (be instanceof pg().Fan) { terminals(be) }
    }
    function heater(level, p) {
        let be = level.getBlockEntity(p)
        let st = level.getBlockState(p)
        if (be == null) { out('heater none at ' + p); return }
        if (be instanceof pg().Heater) {
            out('heater coil state=' + be.getState() + ' t=' + gtime(level))
        } else if (be instanceof pg().BasinHeater) {
            let above = level.getBlockState(p.above())
            let boiler
            try { boiler = fmt($Boiler.getActiveHeat(level, p, st)) } catch (err) { boiler = 'error ' + err }
            let basin
            try { basin = String($Basin.getHeatLevelOf(st)) } catch (err) { basin = 'error ' + err }
            out('heater basin heat_level=' + prop(st, 'blaze') + ' boilerHeat=' + boiler + ' basinSees=' + basin
                + ' above=' + blockId(above) + ' light=' + level.getBrightness($LightLayer.BLOCK, p) + ' t=' + gtime(level))
        } else out('heater: not a heater (' + blockId(st) + ')')
        thermal(be)
        terminals(be)
    }
    function items(level, p, r) {
        let list = level.getEntitiesOfClass($ItemEntity, new $AABB(p.getX() - r, p.getY() - r, p.getZ() - r, p.getX() + r + 1, p.getY() + r + 1, p.getZ() + r + 1))
        out('items ' + list.size() + ' t=' + gtime(level))
        list.forEach(e => {
            let s = e.getItem()
            let proc = ''
            try {
                // Forge's persistent data is saved under ForgeData; e.getPersistentData() is KubeJS's own tag here
                let cd = e.saveWithoutId(new $Compound()).getCompound('ForgeData').getCompound('CreateData')
                if (cd.contains('Processing')) { let pr = cd.getCompound('Processing'); proc = ' processing=' + pr.getString('Type') + ' time=' + pr.getInt('Time') }
            } catch (err) { proc = ' processing=?' }
            out('item ' + idOf(s) + ' x' + s.getCount() + ' at ' + fmt(e.getX()) + ' ' + fmt(e.getY()) + ' ' + fmt(e.getZ()) + proc)
        })
    }
    // the fixture's own light emission from its block state (blockLight also counts nearby fixtures)
    function emission(level, p, st) {
        try { return st.getLightEmission(level, p) } catch (e1) {}
        try { return st.getLightEmission() } catch (e2) {}
        try { return level.getLightEmission(p) } catch (e3) {}
        return 'unreadable'
    }
    function assemblyError(be) {
        let ex
        try { ex = be.getLastAssemblyException() } catch (e1) {
            try { ex = be.lastException } catch (e2) { return 'unreadable' }
        }
        if (ex == null) return 'none'
        let msg
        try { msg = ex.component.getString() } catch (e3) { msg = null }
        if (msg == null) { try { msg = ex.getMessage() } catch (e4) { msg = null } }
        return String(msg == null ? ex : msg)
    }
    function light(level, p) {
        let be = level.getBlockEntity(p)
        let st = level.getBlockState(p)
        if (be == null || !(be instanceof pg().Fixture)) { out('light none at ' + p + ' (' + blockId(st) + ')'); return }
        let tag = be.saveWithoutMetadata()
        out('light ' + blockId(st) + ' power=' + prop(st, 'power') + ' bulb=' + (tag.contains('Bulb') ? tag.getString('Bulb') : 'none')
            + ' T=' + fmt(tag.getFloat('Temperature')) + ' burned=' + tag.getBoolean('Burned')
            + ' color=' + (tag.contains('Color') ? tag.getInt('Color') : 'none')
            + ' emission=' + emission(level, p, st)
            + ' blockLight=' + level.getBrightness($LightLayer.BLOCK, p) + ' t=' + gtime(level))
        if (be instanceof pg().Factory) {
            let n = 0, last = 0, lights = []
            for (let i = 1; i <= 20; i++) {
                let q = pos(p.getX(), p.getY() - i, p.getZ())
                let s = level.getBlockState(q)
                if (blockId(s) == 'powergrid:factory_light_light') { n++; last = i; lights.push(i + ':' + prop(s, 'power')) }
            }
            out('factory light blocks below=' + n + ' deepest=' + last + ' [' + lights.join(' ') + ']'
                + ' blockLight16=' + level.getBrightness($LightLayer.BLOCK, pos(p.getX(), p.getY() - 16, p.getZ())))
        }
        terminals(be)
    }
    function bulb(level, p, item) {
        let be = level.getBlockEntity(p)
        if (be == null || !(be instanceof pg().Fixture)) { out('bulb: no fixture at ' + p); return }
        let tag = be.saveWithoutMetadata()
        tag.remove('Bulb'); tag.remove('Temperature'); tag.remove('Burned'); tag.remove('Color')
        if (item != 'none') { tag.putString('Bulb', item); tag.putFloat('Temperature', 20) }
        be.load(tag)
        be.notifyUpdate()
        let t2 = be.saveWithoutMetadata()
        out('bulb ' + (t2.contains('Bulb') ? t2.getString('Bulb') : 'none'))
    }
    function solar(level, p) {
        let be = level.getBlockEntity(p)
        if (be == null || !(be instanceof pg().Solar || be instanceof pg().Bearing)) { out('solar none at ' + p + ' (' + blockId(level.getBlockState(p)) + ')'); return }
        let entry = null
        try { entry = pg().SolarReg.forBiome(level, p) } catch (err) { out('solar biome error ' + err) }
        let biome
        try { biome = String(level.getBiome(p).unwrapKey().get().location()) } catch (err) { biome = '?' }
        out('solar ' + blockId(level.getBlockState(p)) + ' biome=' + biome + ' override=' + (entry == null ? 'none'
            : ('constant=' + entry.solarConstant() + '/' + entry.overrideSolarConstant() + ' temp=' + entry.biomeTemp() + '/' + entry.overrideTemp()))
            + ' sky=' + level.getBrightness($LightLayer.SKY, p.above()) + ' dayTime=' + dtime(level)
            + ' rain=' + fmt(level.getRainLevel(1)) + ' t=' + gtime(level))
        terminals(be)
        goggles(be)
    }
    function pump(level, p) {
        let be = level.getBlockEntity(p)
        if (be == null || !(be instanceof pg().Pump)) { out('pump none at ' + p); return }
        out('pump speed=' + fmt(be.getSpeed()) + ' t=' + gtime(level))
        terminals(be)
        thermal(be)
        goggles(be)
    }
    function fluidHandler(level, p) {
        let be = level.getBlockEntity(p)
        if (be == null) return null
        let cap = be.getCapability($Caps.FLUID_HANDLER, null)
        if (cap == null || !cap.isPresent()) return null
        return cap.resolve().get()
    }
    function tank(level, p) {
        let h = fluidHandler(level, p)
        if (h == null) { out('tank none at ' + p); return }
        let parts = []
        for (let i = 0; i < h.getTanks(); i++) {
            let f = h.getFluidInTank(i)
            parts.push(i + ':' + (f.isEmpty() ? 'empty' : String($FR.FLUIDS.getKey(f.getFluid())) + ' ' + f.getAmount()) + '/' + h.getTankCapacity(i))
        }
        out('tank ' + parts.join(' ') + ' at ' + p.getX() + ' ' + p.getY() + ' ' + p.getZ() + ' t=' + gtime(level))
    }
    function fill(level, p, fluid, mb) {
        let h = fluidHandler(level, p)
        if (h == null) { out('fill: no fluid handler at ' + p); return }
        // r1113 pwg8: `new FluidStack(fluid, mb)` is ambiguous in Rhino (Fluid / Holder ctors); build it from NBT
        let ftag = new $Compound()
        ftag.putString('FluidName', fluid)
        ftag.putInt('Amount', mb)
        let fs = $FluidStack.loadFluidStackFromNBT(ftag)
        if (fs.isEmpty()) { out('fill: unknown fluid ' + fluid); return }
        let valid
        try { valid = h.isFluidValid(0, fs) } catch (e1) { valid = 'unreadable' }
        out('fill stack ' + String($FR.FLUIDS.getKey(fs.getFluid())) + ' amount=' + fs.getAmount() + ' handler=' + simpleName(h)
            + ' tanks=' + h.getTanks() + ' isFluidValid=' + valid)
        let n = h.fill(fs.copy(), $FluidAction.EXECUTE)
        let via = 'capability'
        if (n == 0) {
            // r1113 pwg10: the tank's capability accepted 0 mB; fill the tank inventory (the controller's) directly
            let be = level.getBlockEntity(p), inv = null, tb = be
            try { let c = be.getControllerBE(); tb = (c != null ? c : be); inv = tb.getTankInventory(); via = c != null ? 'controller inventory' : 'own inventory' } catch (e2) { via = 'no tank inventory (' + e2 + ')' }
            if (inv != null) {
                try { n = inv.fill(fs.copy(), $FluidAction.EXECUTE); via += ' ' + simpleName(inv) } catch (e3) { via += ' fill error ' + e3 }
            }
            // r1113 pwg12: both routes take 0 mB because KubeJS's FluidStack type wrapper (FluidStackJS.of) turns a Forge
            // FluidStack argument into EMPTY on the way into Java; write the tank's NBT instead (no FluidStack crosses Rhino)
            if (n == 0 && tb != null) {
                try {
                    let tag = tb.saveWithoutMetadata()
                    tag.put('TankContent', ftag)
                    tb.load(tag)
                    tb.setChanged()
                    try { tb.sendData() } catch (e5) { }
                    n = tb.getTankInventory().getFluidAmount()
                    via += '; nbt TankContent on ' + simpleName(tb)
                } catch (e4) { via += '; nbt error ' + e4 }
            }
        }
        out('fill ' + fluid + ' ' + n + ' via ' + via)
    }
    function itemHandler(level, p) {
        let be = level.getBlockEntity(p)
        if (be == null) return null
        let cap = be.getCapability($Caps.ITEM_HANDLER, null)
        if (cap == null || !cap.isPresent()) return null
        return cap.resolve().get()
    }
    function insert(level, p, item, n) {
        let h = itemHandler(level, p)
        if (h == null) { out('insert: no item handler at ' + p); return }
        let rest = new $ItemStack($FR.ITEMS.getValue(new $RL(item)), n)
        for (let i = 0; i < h.getSlots() && !rest.isEmpty(); i++) rest = h.insertItem(i, rest, false)
        out('insert ' + item + ' x' + n + ' rest=' + rest.getCount())
    }
    function inv(level, p) {
        let h = itemHandler(level, p)
        if (h == null) { out('inv: no item handler at ' + p); return }
        let parts = []
        for (let i = 0; i < h.getSlots(); i++) {
            let st = h.getStackInSlot(i)
            if (!st.isEmpty()) parts.push(i + ':' + idOf(st) + ' x' + st.getCount())
        }
        out('inv [' + parts.join(' ') + '] t=' + gtime(level))
    }
    function fdCheck(level, p) {
        let be = level.getBlockEntity(p)
        if (be == null) { out('fd none at ' + p); return }
        let below = blockId(level.getBlockState(p.below()))
        if (be instanceof fd().Pot) {
            // isHeated() carries the HEAD inject of the port's CookingPotBlockEntityMixin; the FD heat check alone
            // (isHeated(level, pos), the interface default) does not know the basin heater
            let fdOnly
            try { fdOnly = be['isHeated(net.minecraft.world.level.Level,net.minecraft.core.BlockPos)'](level, p) } catch (err) { fdOnly = 'unreadable' }
            out('fd pot isHeated=' + be.isHeated() + ' fdHeatSourceOnly=' + fdOnly
                + ' below=' + below + ' t=' + gtime(level))
        } else if (be instanceof fd().Skillet) {
            // isCooking() reads isHeated() through the port's redirect; isHeated() itself is not redirected
            out('fd skillet isCooking=' + be.isCooking() + ' isHeated=' + be.isHeated() + ' stored=' + idOf(be.getStoredStack())
                + ' x' + be.getStoredStack().getCount() + ' below=' + below + ' t=' + gtime(level))
        } else out('fd: not a cooking pot or skillet (' + blockId(level.getBlockState(p)) + ')')
    }
    function skillet(level, p, item) {
        let be = level.getBlockEntity(p)
        if (be == null || !(be instanceof fd().Skillet)) { out('skillet: none at ' + p); return }
        let rest = be.addItemToCook(new $ItemStack($FR.ITEMS.getValue(new $RL(item))), null)
        out('skillet added ' + item + ' rest=' + rest.getCount() + ' stored=' + idOf(be.getStoredStack()))
    }
    function age(level, p, r) {
        let counts = {}, n = 0, sum = 0
        for (let dx = -r; dx <= r; dx++) for (let dz = -r; dz <= r; dz++) {
            let s = level.getBlockState(pos(p.getX() + dx, p.getY(), p.getZ() + dz))
            let a = prop(s, 'age')
            if (a == null) continue
            counts[a] = (counts[a] || 0) + 1
            n++; sum += parseInt(a)
        }
        out('age ' + JSON.stringify(counts) + ' crops=' + n + ' mean=' + (n == 0 ? 0 : fmt(sum / n)) + ' t=' + gtime(level))
    }
    function contraption(level, p, r) {
        let list = level.getEntitiesOfClass($Entity, new $AABB(p.getX() - r, p.getY() - r, p.getZ() - r, p.getX() + r + 1, p.getY() + r + 1, p.getZ() + r + 1))
        let n = 0, types = {}
        list.forEach(e => {
            let id = eidOf(e)
            types[id] = (types[id] || 0) + 1
            if (id.indexOf('contraption') < 0) return
            n++
            let tag = new $Compound()
            e.saveWithoutId(tag)
            let c = tag.getCompound('Contraption')
            out('contraption ' + id + ' type=' + c.getString('Type') + ' blocks=' + c.getCompound('Blocks').getList('BlockList', 10).size()
                + ' at ' + fmt(e.getX()) + ' ' + fmt(e.getY()) + ' ' + fmt(e.getZ()))
        })
        out('contraption box types ' + Object.keys(types).sort().map(k => k + 'x' + types[k]).join(' '))
        out('contraptions ' + n)
    }
    function entityLine(e) {
        let tag = e.saveWithoutId(new $Compound())
        let c = tag.getCompound('Contraption')
        let ctl = tag.contains('Controller') ? tag.getCompound('Controller') : null
        return 'id=' + e.getId() + ' uuid=' + String(e.getUUID()) + ' at ' + fmt(e.getX()) + ' ' + fmt(e.getY()) + ' ' + fmt(e.getZ())
            + ' removed=' + e.isRemoved() + ' alive=' + e.isAlive() + ' type=' + c.getString('Type')
            + ' blocks=' + c.getCompound('Blocks').getList('BlockList', 10).size()
            + ' controller=' + (ctl == null ? 'none' : ctl.getInt('X') + ',' + ctl.getInt('Y') + ',' + ctl.getInt('Z'))
    }
    // r1113 pwg10 SQ-PWG-54: assemble ran (running=true) but no contraption entity was near the bearing 100 ticks later
    function contraptionsAll(level, bp) {
        let all = []
        let it
        try { it = level.getAllEntities() } catch (e1) { it = level.getEntities().getAll() }
        let total = 0
        it.forEach(e => {
            total++
            // r1113 pwg12r: the solar panel bearing assembles a create:stationary_contraption, not a controlled one
            let id = eidOf(e)
            if (id == 'create:controlled_contraption' || id == 'create:stationary_contraption') all.push(e)
        })
        out('contraptions all t=' + gtime(level) + ' entities=' + total + ' controlled=' + all.length)
        let be = bp == null ? null : level.getBlockEntity(bp)
        let bearing = be != null && be instanceof pg().Bearing
        all.forEach(e => {
            let att = ''
            if (bearing) { try { att = ' attachedToBearing=' + be.isAttachedTo(e) } catch (e2) { att = ' attachedToBearing=?' } }
            out('controlled ' + entityLine(e) + att)
        })
        if (bp == null) return
        if (!bearing) { out('bearing none at ' + bp); return }
        let mc, how = 'field'
        try { mc = be.movedContraption; if (mc === undefined) throw 'undefined' } catch (e3) { mc = undefined }
        let line
        if (mc !== undefined) line = mc == null ? 'null' : 'set ' + entityLine(mc)
        else {
            how = 'isAttachedTo'
            let isNull
            try { isNull = be.isAttachedTo(null) } catch (e4) { isNull = '?' }
            let match = all.filter(e => { try { return be.isAttachedTo(e) } catch (e5) { return false } })
            line = isNull === true ? 'null' : (match.length ? 'set id=' + match[0].getId() + ' (in the level)' : (isNull === false ? 'set (entity not in the level)' : 'unreadable'))
        }
        let bt = be.saveWithoutMetadata()
        out('bearing at ' + bp.getX() + ' ' + bp.getY() + ' ' + bp.getZ() + ' movedContraption=' + line + ' (' + how + ') running=' + bt.getBoolean('Running')
            + ' panels=' + bt.getInt('PanelCount') + ' removed=' + be.isRemoved())
    }
    function run(level, a) {
        let cmd = a[0]
        if (cmd == 'check') { check(); return }
        if (cmd == 'contraptions') {
            if (a[1] != 'all') { out('contraptions: use contraptions all [X Y Z]'); return }
            contraptionsAll(level, a.length > 4 ? pos(parseInt(a[2]), parseInt(a[3]), parseInt(a[4])) : null)
            return
        }
        let P = pos(parseInt(a[1]), parseInt(a[2]), parseInt(a[3]))
        if (cmd == 'fan') fan(level, P)
        else if (cmd == 'fantype') out('fantype ' + typeId(fanTypeAt(level, P)) + ' at ' + P + ' (' + blockId(level.getBlockState(P)) + ')')
        else if (cmd == 'heater') heater(level, P)
        else if (cmd == 'items') items(level, P, parseInt(a[4]))
        else if (cmd == 'chute') {
            let be = level.getBlockEntity(P)
            if (be == null || !(be instanceof $Chute)) out('chute none at ' + P)
            else out('chute motion=' + fmt(be.getItemMotion()) + ' t=' + gtime(level))
        } else if (cmd == 'light') light(level, P)
        else if (cmd == 'bulb') bulb(level, P, a[4])
        else if (cmd == 'solar') solar(level, P)
        else if (cmd == 'solarjoin') {
            let a1 = level.getBlockEntity(P), a2 = level.getBlockEntity(pos(parseInt(a[4]), parseInt(a[5]), parseInt(a[6])))
            if (!(a1 instanceof pg().Solar) || !(a2 instanceof pg().Solar)) out('solarjoin: both must be solar panels')
            else {
                let c1 = a1.getController(), c2 = a2.getController()
                if (!c1.isPresent() || !c2.isPresent()) out('solarjoin: no controller')
                else { pg().Solar.mergeMultiblock(c1.get(), c2.get()); out('solarjoin ok') }
            }
        } else if (cmd == 'assemble') {
            let be = level.getBlockEntity(P)
            if (!(be instanceof pg().Bearing)) out('assemble: no solar panel bearing at ' + P)
            else {
                be.assemble()
                out('assemble called')
                // lastException none with Running false: contraption.assemble returned false (nothing to move)
                let bt = be.saveWithoutMetadata()
                out('assemble lastException=' + assemblyError(be) + ' running=' + bt.getBoolean('Running')
                    + ' panels=' + bt.getInt('PanelCount') + ' parallel=' + bt.getInt('StringsInParallel')
                    + ' facing=' + prop(level.getBlockState(P), 'facing') + ' above=' + blockId(level.getBlockState(P.above())))
            }
        } else if (cmd == 'pump') pump(level, P)
        else if (cmd == 'tank') tank(level, P)
        else if (cmd == 'fill') fill(level, P, a[4], parseInt(a[5]))
        else if (cmd == 'insert') insert(level, P, a[4], parseInt(a[5]))
        else if (cmd == 'inv') inv(level, P)
        else if (cmd == 'fd') fdCheck(level, P)
        else if (cmd == 'skillet') skillet(level, P, a[4])
        else if (cmd == 'age') age(level, P, parseInt(a[4]))
        else if (cmd == 'contraption') contraption(level, P, parseInt(a[4]))
        else out('unknown subcommand ' + cmd)
    }
    ServerEvents.commandRegistry(e => {
        const C = e.commands
        e.register(C.literal('zpwg6').requires(s => s.hasPermission(2))
            .then(C.argument('args', $Args.greedyString()).executes(ctx => {
                try {
                    run(ctx.source.getLevel(), String($Args.getString(ctx, 'args')).split(' '))
                } catch (err) { out('error ' + err) }
                out('done')
                return 1
            })))
    })
})()
