// Scratch-only probe for SQ-QN (Quark game nerfs Mending and dimension-locked elytra off; run r1115 phase 8). Copy into a
// scratch server's kubejs/server_scripts as zz_zqn_probe.js, never into the pack. NAME = a perfBot bot (`pbot spawn`).
//   /zqn state            Quark GameNerfsModule statics: nerfMending, enableDimensionLockedElytra, elytraAllowedDimensions
//   /zqn fly NAME         what the client's START_FALL_FLYING packet runs on the server: Quark canEntityUseElytra(bot, true),
//                         onGround, then bot.tryToStartFallFlying() and isFallFlying
//   /zqn flying NAME      isFallFlying, dimension, position and motion of the bot
//   /zqn hand NAME        main-hand item id, damage, enchantments; total XP points and level
// Output: "[zqn]..." lines in logs/latest.log, then "[zqn] done".
(() => {
    const $QArgs = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $QReg = Java.loadClass('net.minecraft.core.Registry')
    function out(s) { console.info('[zqn] ' + s) }
    function player(server, name) {
        let p = server.getPlayerList().getPlayerByName(name)
        if (p == null) out('no player ' + name)
        return p
    }
    function nerfs() { return Java.loadClass('vazkii.quark.content.experimental.module.GameNerfsModule') }
    function run(server, line) {
        try {
            let a = String(line).trim().split(/\s+/), what = a[0]
            if (what == 'state') {
                let N = nerfs()
                out('state nerfMending ' + N.nerfMending + ' enableDimensionLockedElytra ' + N.enableDimensionLockedElytra
                    + ' elytraAllowedDimensions ' + N.elytraAllowedDimensions)
            } else if (what == 'fly') {
                let p = player(server, a[1]); if (p == null) return
                let can = nerfs().canEntityUseElytra(p, true)
                let ground = p.isOnGround()
                let started = p.tryToStartFallFlying()
                out('fly ' + a[1] + ' quarkCanUse ' + can + ' onGround ' + ground + ' started ' + started + ' fallFlying ' + p.isFallFlying()
                    + ' chest ' + String($QReg.ITEM.getKey(p.getItemBySlot(Java.loadClass('net.minecraft.world.entity.EquipmentSlot').CHEST).getItem())))
            } else if (what == 'flying') {
                let p = player(server, a[1]); if (p == null) return
                let m = p.getDeltaMovement()
                out('flying ' + a[1] + ' fallFlying ' + p.isFallFlying() + ' dim ' + String(p.getLevel().dimension)
                    + ' y ' + p.getY().toFixed(1) + ' motion ' + m.x().toFixed(2) + ' ' + m.y().toFixed(2) + ' ' + m.z().toFixed(2))
            } else if (what == 'hand') {
                let p = player(server, a[1]); if (p == null) return
                let s = p.getMainHandItem()
                out('hand ' + a[1] + ' ' + String($QReg.ITEM.getKey(s.getItem())) + ' damage ' + s.getDamageValue()
                    + ' ench ' + s.getEnchantmentTags() + ' xpTotal ' + p.totalExperience + ' level ' + p.experienceLevel
                    + ' progress ' + p.experienceProgress)
            } else out('unknown ' + what)
        } catch (err) { out('error: ' + err) }
        out('done')
    }
    ServerEvents.commandRegistry(e => {
        const { commands: C } = e
        e.register(C.literal('zqn').then(C.argument('line', $QArgs.greedyString()).executes(ctx => {
            run(ctx.source.server, $QArgs.getString(ctx, 'line'))
            return 1
        })))
    })
})()
