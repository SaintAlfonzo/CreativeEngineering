// Scratch-only Tempad probe (r1107tp, SQ-TP rows). Copy into a scratch server's kubejs/server_scripts as zz_ztp_probe.js,
// never into the pack. Drives the Tempad port's c2s packet handlers on a named player (perfBot bot), so rows that
// need the Tempad screens run without a client. P = player name. Output: "[ztp] ..." lines in logs/latest.log, then
// "[ztp] done".
//   /ztp ctx P                     the Tempad context the macro key would use (type, slot)
//   /ztp save P NAME               New Location app Done (CreateLocationPacket) at P's position, default colour
//   /ztp locs P                    every location P's Tempad lists: provider, uuid, name, dimension, x y z, angle
//   /ztp open P PROVIDER NAME      Teleport app: open a timedoor to the location named NAME (openTimedoor, message logged)
//   /ztp pin P PROVIDER NAME       Teleport app pin (SetFavoritePacket); "/ztp pin P none" clears it
//   /ztp settings P APP MACRO      Settings app Done (SaveSettingsPacket), ids like tempad:teleport tempad:pinned_location
//   /ztp openapp P APP             app hotkey (OpenAppPacket), e.g. tempad:new_location; check with /ztp menu P
//   /ztp macro P                   macro key (UseMacroPacket)
//   /ztp apps P                    apps the Tempad screen shows (AppRegistry.getAll, not stationary)
//   /ztp chr P                     chronons of every chronon item in P's inventory (slot, id, power/max)
//   /ztp setchr P SLOT N           set the chronons of the item in SLOT to N (extract all, insert N)
//   /ztp cd P                      Tempad cooldown left (ticks, percent)
//   /ztp menu P                    P's open container menu type
//   /ztp doors                     every timedoor: dim, pos, age, closing time, target, glitching, PlacementSettings NBT
//   /ztp blocks X1 Y1 Z1 X2 Y2 Z2  every tempad: block state in an overworld box (loaded chunks only)
//   /ztp break P X Y Z             survival block break by P (gameMode.destroyBlock, drops as a player break)
//   /ztp pickup P [R=24]           move every item entity within R blocks into P's inventory (bots pick nothing up)
//   /ztp markerui P X Y Z ACCESS LOCKED NAME   Timedoor Marker screen close (UpdateAnchorPacket), default colour
//   /ztp chronoui P X Y Z ACCESS LOCKED YOFF ANGLE NAME   Chronomark screen close (UpdateChronomarkPacket)
//   /ztp hand P                    P's main-hand item and its NBT
//   /ztp bchr X Y Z [N]            chronons of an overworld block entity (projector, metronome, workstation); N sets them
//   /ztp carry P SLOT / uncarry P   inventory stack onto the open menu's cursor and back (card writing, wallet)
//   /ztp sneak P on|off           shift key state (perfBot uses then act as shift-use)
//   /ztp ftbparty A B             FTB Teams party of A and B through TeamManager (party commands need a player source)
//   /ztp mput X Y Z SLOT N / metro X Y Z   metronome: put a cell with N chronons in SLOT; boot, slots, owner's pool
//   /ztp history P / backtrack P N   travel history newest first; backtrack to entry N with the carried twister
//   /ztp click P MENUSLOT BUTTON [inv]   PICKUP click on the open menu, or the inventory menu with inv (hotbar 36..44; button 1 = right)
//   /ztp curios P / curio P TYPE INDEX INVSLOT   Curios slots of P; move an inventory stack into a Curios slot
//   /ztp anchors                   tempad_anchor_points entries (uuid -> GlobalPos)
// Death: /kill a perfBot bot and perfBot relogs it (remove + spawn, no PlayerEvent.Clone); never PlayerList.respawn a
// bot (perfBot keeps ticking the old object: DistanceManager NPE crash, r1107tp).
//   /ztp pdata P                   P's tempad:player_data capability NBT (pinned location, travel history)
//   /ztp portal P X Y Z ANGLE UPRIGHT [PROVIDER NAME]   Portal Setup close (SyncPortalSettingsPacket) on the workstation
//                                  context P's open menu holds (open it first with a use on the terminal)
//   /ztp card P PROVIDER NAME      Teleport app: write the selected location onto the carried blank card (WriteToCardPacket)
(() => {
    const $TPArgs = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $TPCmds = Java.loadClass('net.minecraft.commands.Commands')
    const $TPRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $TPReg = Java.loadClass('net.minecraft.core.Registry')
    const $TPCtxReg = Java.loadClass('earth.terrarium.tempad.api.context.ContextRegistry')
    const $TPLocs = Java.loadClass('earth.terrarium.tempad.api.locations.TempadLocations')
    const $TPChr = Java.loadClass('earth.terrarium.tempad.api.tva_device.ChrononHandler')
    const $TPAct = Java.loadClass('earth.terrarium.tempad.api.ActionType')
    const $TPColor = Java.loadClass('earth.terrarium.tempad.common.utils.Color')
    const $TPDoor = Java.loadClass('earth.terrarium.tempad.common.entity.TimedoorEntity')
    const $TPApps = Java.loadClass('earth.terrarium.tempad.api.app.AppRegistry')
    const $TPPData = Java.loadClass('earth.terrarium.tempad.common.data.TempadPlayerData')
    const $TPAnch = Java.loadClass('earth.terrarium.tempad.common.location_handlers.AnchorPointsData')
    const $TPCreate = Java.loadClass('earth.terrarium.tempad.common.network.c2s.CreateLocationPacket')
    const $TPFav = Java.loadClass('earth.terrarium.tempad.common.network.c2s.SetFavoritePacket')
    const $TPSettings = Java.loadClass('earth.terrarium.tempad.common.network.c2s.SaveSettingsPacket')
    const $TPMacro = Java.loadClass('earth.terrarium.tempad.common.network.c2s.UseMacroPacket')
    const $TPPortal = Java.loadClass('earth.terrarium.tempad.common.network.c2s.SyncPortalSettingsPacket')
    const $TPCard = Java.loadClass('earth.terrarium.tempad.common.network.c2s.WriteToCardPacket')
    const $TPChronoPkt = Java.loadClass('earth.terrarium.tempad.common.network.c2s.UpdateChronomarkPacket')
    const $TPBackTrack = Java.loadClass('earth.terrarium.tempad.common.network.c2s.BackTrackLocation')
    const $TPAnchorPkt = Java.loadClass('earth.terrarium.tempad.common.network.c2s.UpdateAnchorPacket')
    const $TPItemEnt = Java.loadClass('net.minecraft.world.entity.item.ItemEntity')
    const $TPOpenApp = Java.loadClass('earth.terrarium.tempad.common.network.c2s.OpenAppPacket')
    const $TPCompound = Java.loadClass('net.minecraft.nbt.CompoundTag')
    const $TPBlockPos = Java.loadClass('net.minecraft.core.BlockPos')
    const $TPItemStackCls = Java.loadClass('net.minecraft.world.item.ItemStack')
    const $TPHashMap = Java.loadClass('java.util.HashMap')
    const $TPArrayList = Java.loadClass('java.util.ArrayList')

    let tpOut = function (s) { console.info('[ztp] ' + s) }
    let tpItemId = function (stack) { return String($TPReg.ITEM.getKey(stack.getItem())) }
    let tpTempadCtx = function (p) {
        return $TPCtxReg.locate(p, st => tpItemId(st) == 'tempad:tempad')
    }
    // location lookup by name across every provider the Tempad lists: [providerId, uuid, NamedGlobalVec3]
    let tpFind = function (p, ctx, provider, name) {
        let all = new $TPHashMap($TPLocs.getAll(p, ctx))
        let found = null
        new $TPArrayList(all.keySet()).forEach(pid => {
            if (provider != '*' && String(pid) != provider) return
            let m = new $TPHashMap(all.get(pid))
            new $TPArrayList(m.keySet()).forEach(id => {
                let loc = m.get(id)
                if (found == null && String(loc.name().getString()) == name) found = [pid, id, loc]
            })
        })
        return found
    }
    let tpLocLine = function (pid, id, loc) {
        let pos = loc.pos()
        return pid + ' ' + id + ' "' + loc.name().getString() + '" ' + loc.dimension().location() + ' '
            + pos.x().toFixed(2) + ' ' + pos.y().toFixed(2) + ' ' + pos.z().toFixed(2) + ' angle ' + loc.angle()
    }

    let tpRun = function (server, a) {
        let sub = a[0]
        let p = a.length > 1 ? server.getPlayerList().getPlayerByName(a[1]) : null
        let ctx = p ? tpTempadCtx(p) : null
        try {
            if (sub == 'ctx') {
                tpOut(ctx == null ? 'ctx none' : 'ctx ' + ctx.getType().id() + ' data ' + ctx.getData() + ' item ' + tpItemId(ctx.getStack()))
            } else if (sub == 'save') {
                new $TPCreate(a.slice(2).join(' '), $TPColor.DEFAULT, ctx.getHolder()).handle(p)
                tpOut('save sent "' + a.slice(2).join(' ') + '" at ' + p.blockPosition() + ' in ' + p.level.dimension.toString())
            } else if (sub == 'locs') {
                let all = new $TPHashMap($TPLocs.getAll(p, ctx))
                let n = 0
                new $TPArrayList(all.keySet()).forEach(pid => {
                    let m = new $TPHashMap(all.get(pid))
                    new $TPArrayList(m.keySet()).forEach(id => { n++; tpOut('loc ' + tpLocLine(pid, id, m.get(id))) })
                })
                tpOut('locs total ' + n)
            } else if (sub == 'open' || sub == 'card') {
                let f = tpFind(p, ctx, a[2], a.slice(3).join(' '))
                if (f == null) { tpOut(sub + ' no location named ' + a.slice(3).join(' ')) }
                else if (sub == 'open') {
                    let before = $TPChr.of(ctx.getStack()).getPower()
                    let msg = $TPDoor.openTimedoor(p, ctx, f[0], f[1], f[2])
                    let after = $TPChr.of(ctx.getStack()).getPower()
                    tpOut('open ' + tpLocLine(f[0], f[1], f[2]) + ' -> message ' + (msg == null ? 'none' : '"' + msg.getString() + '" key ' + (msg.getContents().getKey ? msg.getContents().getKey() : '?')) + ' chronons ' + before + ' -> ' + after)
                } else {
                    new $TPCard(f[0], f[1], ctx.getHolder()).handle(p)
                    tpOut('card sent for ' + tpLocLine(f[0], f[1], f[2]))
                }
            } else if (sub == 'pin') {
                if (a[2] == 'none') { new $TPFav(null).handle(p); tpOut('pin cleared') }
                else {
                    let f = tpFind(p, ctx, a[2], a.slice(3).join(' '))
                    if (f == null) tpOut('pin no location')
                    else { new $TPFav(f[0], f[1]).handle(p); tpOut('pin ' + tpLocLine(f[0], f[1], f[2])) }
                }
            } else if (sub == 'settings') {
                new $TPSettings(ctx.getHolder(), new $TPRL(a[2]), new $TPRL(a[3])).handle(p)
                tpOut('settings sent app ' + a[2] + ' macro ' + a[3] + ' -> stack ' + ctx.getStack().getTag())
            } else if (sub == 'openapp') {
                new $TPOpenApp(new $TPRL(a[2])).handle(p)
                tpOut('openapp sent ' + a[2])
            } else if (sub == 'macro') {
                new $TPMacro().handle(p)
                tpOut('macro sent')
            } else if (sub == 'apps') {
                let m = new $TPHashMap($TPApps.getAll(ctx, false))
                let ids = []
                new $TPArrayList(m.keySet()).forEach(id => ids.push(String(id) + (m.get(id).isEnabled(p) ? '' : '(disabled)')))
                ids.sort()
                tpOut('apps ' + ids.join(' '))
            } else if (sub == 'chr') {
                let inv = p.getInventory()
                for (let i = 0; i < inv.getContainerSize(); i++) {
                    let st = inv.getItem(i)
                    if (st.isEmpty()) continue
                    let h = $TPChr.of(st)
                    if (h != null) tpOut('chr slot ' + i + ' ' + tpItemId(st) + ' ' + h.getPower() + '/' + h.getMaxPower())
                }
                tpOut('chr end')
            } else if (sub == 'setchr') {
                let st = p.getInventory().getItem(parseInt(a[2]))
                let h = $TPChr.of(st)
                h.extract(h.getPower(), $TPAct.Execute)
                let got = h.insert(parseInt(a[3]), $TPAct.Execute)
                tpOut('setchr slot ' + a[2] + ' ' + tpItemId(st) + ' inserted ' + got + ' now ' + $TPChr.of(p.getInventory().getItem(parseInt(a[2]))).getPower())
            } else if (sub == 'cd') {
                let it = ctx.getStack().getItem()
                tpOut('cd ' + p.getCooldowns().isOnCooldown(it) + ' percent ' + p.getCooldowns().getCooldownPercent(it, 0))
            } else if (sub == 'menu') {
                let m = p.containerMenu
                let t = null
                try { t = m.getType() } catch (err) { t = null }
                tpOut('menu ' + (t == null ? 'none(inventory)' : $TPReg.MENU.getKey(t)) + ' id ' + m.containerId)
            } else if (sub == 'doors') {
                let n = 0
                server.getAllLevels().forEach(lvl => {
                    lvl.getAllEntities().forEach(ent => {
                        if (!(ent instanceof $TPDoor)) return
                        n++
                        let tag = new $TPCompound()
                        ent.saveWithoutId(tag)
                        tpOut('door ' + lvl.dimension.toString() + ' ' + ent.getX().toFixed(2) + ' ' + ent.getY().toFixed(2) + ' ' + ent.getZ().toFixed(2)
                            + ' rot ' + tag.get('Rotation') + ' age ' + tag.getInt('Age') + ' closing ' + ent.getClosingTime() + ' glitch ' + ent.isGlitching()
                            + ' target ' + ent.getTargetDimension().location() + ' ' + ent.getTargetPos() + ' place ' + tag.get('PlacementSettings')
                            + ' linked ' + tag.get('LinkedPortalId') + ' original ' + tag.getBoolean('IsOriginal'))
                    })
                })
                tpOut('doors total ' + n)
            } else if (sub == 'break') {
                let okB = p.gameMode.destroyBlock(new $TPBlockPos(parseInt(a[2]), parseInt(a[3]), parseInt(a[4])))
                tpOut('break ' + a.slice(2, 5).join(' ') + ' -> ' + okB)
            } else if (sub == 'pickup') {
                let nP = 0
                p.level.getEntitiesOfClass($TPItemEnt, p.getBoundingBox().inflate(parseFloat(a[2] || '24'))).forEach(ie => {
                    let st = ie.getItem().copy()
                    let ieTag = new $TPCompound()
                    ie.saveWithoutId(ieTag)
                    tpOut('pickup ' + ieTag.get('Item'))
                    p.getInventory().add(st)
                    ie.discard()
                    nP++
                })
                tpOut('pickup total ' + nP)
            } else if (sub == 'markerui') {
                // Timedoor Marker screen close: /ztp markerui P X Y Z ACCESS(true|false) LOCKED(true|false) NAME...
                new $TPAnchorPkt(new $TPBlockPos(parseInt(a[2]), parseInt(a[3]), parseInt(a[4])), $TPColor.DEFAULT, a.slice(7).join(' '), a[5] == 'true', a[6] == 'true').handle(p)
                tpOut('markerui sent ' + a.slice(2).join(' '))
            } else if (sub == 'chronoui') {
                // Chronomark screen close: /ztp chronoui P X Y Z ACCESS_ID LOCKED YOFFSET ANGLE NAME...
                new $TPChronoPkt(new $TPBlockPos(parseInt(a[2]), parseInt(a[3]), parseInt(a[4])), $TPColor.DEFAULT, a.slice(9).join(' '),
                    new $TPRL(a[5]), a[6] == 'true', parseFloat(a[7]), parseInt(a[8])).handle(p)
                tpOut('chronoui sent ' + a.slice(2).join(' '))
            } else if (sub == 'hand') {
                let hsTag = new $TPCompound()
                p.getMainHandItem().save(hsTag)
                tpOut('hand ' + hsTag)
            } else if (sub == 'bchr') {
                // /ztp bchr X Y Z [N]   chronons of the block entity at X Y Z (overworld); with N: set them to N
                let be = server.overworld().getBlockEntity(new $TPBlockPos(parseInt(a[1]), parseInt(a[2]), parseInt(a[3])))
                let hb = $TPChr['of(net.minecraft.world.level.block.entity.BlockEntity)'](be)
                if (hb == null) tpOut('bchr none')
                else {
                    if (a.length > 4) { hb.extract(hb.getPower(), $TPAct.Execute); hb.insert(parseInt(a[4]), $TPAct.Execute); be.setChanged() }
                    tpOut('bchr ' + a.slice(1, 4).join(' ') + ' ' + hb.getPower() + '/' + hb.getMaxPower())
                }
            } else if (sub == 'carry') {
                // /ztp carry P SLOT   move the inventory stack in SLOT onto the open menu's cursor (what a click does)
                let invC = p.getInventory()
                p.containerMenu.setCarried(invC.getItem(parseInt(a[2])).copy())
                invC.setItem(parseInt(a[2]), $TPItemStackCls.EMPTY)
                let ct = new $TPCompound()
                p.containerMenu.getCarried().save(ct)
                tpOut('carry ' + ct)
            } else if (sub == 'uncarry') {
                let ct2 = new $TPCompound()
                p.containerMenu.getCarried().save(ct2)
                tpOut('uncarry ' + ct2)
                p.getInventory().add(p.containerMenu.getCarried().copy())
                p.containerMenu.setCarried($TPItemStackCls.EMPTY)
            } else if (sub == 'sneak') {
                p.setShiftKeyDown(a[2] == 'on')
                tpOut('sneak ' + p.isShiftKeyDown())
            } else if (sub == 'ftbparty') {
                // /ztp ftbparty A B   A creates an FTB Teams party, invites B, B joins (party commands need a real player source)
                let tmCls = Java.loadClass('dev.ftb.mods.ftbteams.data.TeamManager')
                let pb = server.getPlayerList().getPlayerByName(a[2])
                let party = tmCls.INSTANCE.createParty(p, 'tp_' + a[1]).getRight()
                party.invite(p, Java.loadClass('java.util.List').of(pb.getGameProfile()))
                party.join(pb.createCommandSourceStack())
                tpOut('ftbparty ' + party.getName().getString() + ' members ' + party.getMembers())
            } else if (sub == 'mput' || sub == 'metro') {
                // /ztp mput X Y Z SLOT N   a chronon cell holding N chronons into metronome inventory SLOT (0-7)
                // /ztp metro X Y Z         boot time, slot chronons, the owner's pool (MetronomeData stored / capacity / count)
                let mbe = server.overworld().getBlockEntity(new $TPBlockPos(parseInt(a[1]), parseInt(a[2]), parseInt(a[3])))
                if (sub == 'mput') {
                    let cell = new $TPItemStackCls($TPReg.ITEM.get(new $TPRL('tempad:chronon_cell')))
                    let hc = $TPChr['of(net.minecraft.world.item.ItemStack)'](cell)
                    hc.insert(parseInt(a[5]), $TPAct.Execute)
                    mbe.inventory.setStackInSlot(parseInt(a[4]), cell)
                    mbe.setChanged()
                    tpOut('mput slot ' + a[4] + ' cell ' + parseInt(a[5]))
                } else {
                    let slotsM = []
                    for (let i = 0; i < 8; i++) {
                        let stM = mbe.inventory.getStackInSlot(i)
                        let hM = stM.isEmpty() ? null : $TPChr['of(net.minecraft.world.item.ItemStack)'](stM)
                        slotsM.push(stM.isEmpty() ? '-' : (hM == null ? tpItemId(stM) : String(hM.getPower())))
                    }
                    let md = Java.loadClass('earth.terrarium.tempad.common.data.MetronomeData').get()
                    let own = mbe.owner == null ? null : mbe.owner.getId()
                    tpOut('metro boot ' + mbe.bootTime + ' slots ' + slotsM.join(',') + ' local ' + mbe.localChronons + '/' + mbe.localCapacity
                        + (own == null ? ' no owner' : ' pool ' + md.getStored(own) + '/' + md.getCapacity(own) + ' count ' + md.getCount(own)))
                }
            } else if (sub == 'history' || sub == 'backtrack') {
                // /ztp history P          travel history newest first (index, epoch ms, entry)
                // /ztp backtrack P N      Time Twister / Timeline backtrack to entry N of that list (BackTrackLocation)
                let hist = new $TPHashMap($TPPData.get(p).getTravelHistory().getHistory())
                let keysH = []
                new $TPArrayList(hist.keySet()).forEach(d => keysH.push(d))
                keysH.sort((x, y) => y.getTime() - x.getTime())
                if (sub == 'history') {
                    keysH.forEach((d, i) => { let hl = new $TPCompound(); tpOut('history ' + i + ' ' + d.getTime() + ' ' + hist.get(d)) })
                    tpOut('history total ' + keysH.length)
                } else {
                    let twCtx = $TPCtxReg.locate(p, st => tpItemId(st) == 'tempad:time_twister' || (tpItemId(st) == 'tempad:tempad' && st.hasTag() && st.getTag().getBoolean('twister_equipped')))
                    let before = $TPChr.of(twCtx.getStack()).getPower()
                    new $TPBackTrack(keysH[parseInt(a[2])], twCtx.getHolder()).handle(p)
                    tpOut('backtrack ' + a[2] + ' with ' + tpItemId(twCtx.getStack()) + ' chronons ' + before + ' -> ' + $TPChr.of(twCtx.getStack()).getPower() + ' now at ' + p.level.dimension.toString() + ' ' + p.blockPosition())
                }
            } else if (sub == 'click') {
                // /ztp click P MENUSLOT BUTTON   PICKUP click on the open menu (inventory menu: hotbar = 36..44); 1 = right
                // a 5th argument "inv" clicks the inventory menu even while another menu is open (bots never close one)
                let menuK = a[4] == 'inv' ? p.inventoryMenu : p.containerMenu
                menuK.clicked(parseInt(a[2]), parseInt(a[3]), Java.loadClass('net.minecraft.world.inventory.ClickType').PICKUP, p)
                let cc = new $TPCompound()
                menuK.getCarried().save(cc)
                tpOut('click ' + a[2] + ' ' + a[3] + ' carried ' + cc)
            } else if (sub == 'curios' || sub == 'curio') {
                // /ztp curios P                     Curios slot types and sizes of P, with what each slot holds
                // /ztp curio P TYPE INDEX INVSLOT   move the inventory stack in INVSLOT into Curios slot TYPE[INDEX]
                let ch = Java.loadClass('top.theillusivec4.curios.api.CuriosApi').getCuriosHelper().getCuriosHandler(p).resolve().get()
                let curMap = new $TPHashMap(ch.getCurios())
                if (sub == 'curios') {
                    new $TPArrayList(curMap.keySet()).forEach(t => {
                        let st = curMap.get(t).getStacks()
                        let items = []
                        for (let i = 0; i < st.getSlots(); i++) { let cs = st.getStackInSlot(i); let chc = cs.isEmpty() ? null : $TPChr['of(net.minecraft.world.item.ItemStack)'](cs); items.push(cs.isEmpty() ? '-' : tpItemId(cs) + (chc == null ? '' : ' ' + chc.getPower())) }
                        tpOut('curios ' + t + ' size ' + st.getSlots() + ' [' + items.join(',') + ']')
                    })
                } else {
                    let stH = curMap.get(a[2]).getStacks()
                    let invI = p.getInventory()
                    stH.setStackInSlot(parseInt(a[3]), invI.getItem(parseInt(a[4])).copy())
                    invI.setItem(parseInt(a[4]), $TPItemStackCls.EMPTY)
                    tpOut('curio ' + a[2] + '[' + a[3] + '] <- ' + tpItemId(stH.getStackInSlot(parseInt(a[3]))))
                }
            } else if (sub == 'blocks') {
                let lvlB = server.overworld()
                let nB = 0
                for (let x = parseInt(a[1]); x <= parseInt(a[4]); x++)
                    for (let y = parseInt(a[2]); y <= parseInt(a[5]); y++)
                        for (let z = parseInt(a[3]); z <= parseInt(a[6]); z++) {
                            let bs = lvlB.getBlockState(new $TPBlockPos(x, y, z))
                            let bid = String($TPReg.BLOCK.getKey(bs.getBlock()))
                            if (!bid.startsWith('tempad:')) continue
                            nB++
                            tpOut('block ' + x + ' ' + y + ' ' + z + ' ' + bs)
                        }
                tpOut('blocks total ' + nB)
            } else if (sub == 'anchors') {
                let m = new $TPHashMap($TPAnch.get().anchors)
                new $TPArrayList(m.keySet()).forEach(id => tpOut('anchor ' + id + ' ' + m.get(id)))
                tpOut('anchors total ' + m.size())
            } else if (sub == 'pdata') {
                tpOut('pdata ' + $TPPData.get(p).save())
            } else if (sub == 'portal') {
                let mctx = null
                try { mctx = p.containerMenu.ctx } catch (err) { mctx = null }
                let pid = null, lid = null
                if (a.length > 8) {
                    let f = tpFind(p, mctx, a[7], a.slice(8).join(' '))
                    if (f != null) { pid = f[0]; lid = f[1] }
                }
                new $TPPortal(parseFloat(a[2]), parseFloat(a[3]), parseFloat(a[4]), parseInt(a[5]), a[6] == 'true', pid, lid, mctx.getHolder()).handle(p)
                tpOut('portal sent ' + a.slice(2, 7).join(' ') + ' target ' + lid)
            } else {
                tpOut('unknown subcommand ' + sub)
            }
        } catch (err) {
            tpOut('error ' + sub + ': ' + err)
        }
        tpOut('done')
        return 1
    }

    ServerEvents.commandRegistry(e => {
        e.register($TPCmds.literal('ztp').requires(src => src.hasPermission(2))
            .then($TPCmds.argument('args', $TPArgs.greedyString())
                .executes(c => tpRun(c.source.getServer(), String($TPArgs.getString(c, 'args')).trim().split(/\s+/)))))
    })
})()
