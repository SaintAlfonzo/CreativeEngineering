// Scratch-only player helper for bulk queue runs: what perfBot (/pbot) cannot do on its own. Copy into a scratch
// server's kubejs/server_scripts as zz_zp_probe.js, never into the pack. NAME = a perfBot bot (or any online player).
//   /zp sneak NAME on|off     shift key held or released (Player.isShiftKeyDown); the next /pbot use is a sneak use
//   /zp eat NAME              finish using the main-hand item once (eat / drink): ItemStack.finishUsingItem, then
//                             ForgeEventFactory.onItemUseFinish, the order LivingEntity.completeUsingItem uses
//   /zp hunger NAME N         food level N (0-20), saturation 0
//   /zp effects NAME          active effects: id amplifier ticks
//   /zp food NAME             food level and saturation
//   /zp inv NAME              non-empty inventory slots: slot count id
// Output: "[zp] ..." lines in logs/latest.log, then "[zp] done".
(() => {
    const $ZPArgs = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $ZPReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $ZPEvents = Java.loadClass('net.minecraftforge.event.ForgeEventFactory')
    const $ZPHand = Java.loadClass('net.minecraft.world.InteractionHand')
    function out(s) { console.info('[zp] ' + s) }
    function idOf(st) { return st.isEmpty() ? 'empty' : st.getCount() + ' ' + String($ZPReg.ITEMS.getKey(st.getItem())) }
    function effects(p) {
        let ef = []
        p.getActiveEffects().forEach(i => ef.push(String($ZPReg.MOB_EFFECTS.getKey(i.getEffect())) + ' amp ' + i.getAmplifier() + ' ' + i.getDuration() + 't'))
        return ef.length ? ef.join('; ') : 'none'
    }
    function run(server, what, name, arg) {
        try {
            let p = server.getPlayerList().getPlayerByName(name)
            if (p == null) { out('no player ' + name); return }
            if (what == 'sneak') {
                p.setShiftKeyDown(arg == 'on')
                out('sneak ' + name + ' ' + p.isShiftKeyDown())
            } else if (what == 'eat') {
                let held = p.getMainHandItem()
                if (held.isEmpty()) { out('eat ' + name + ': empty hand'); return }
                let before = idOf(held), copy = held.copy()
                let res = held.finishUsingItem(p.getLevel(), p)
                res = $ZPEvents.onItemUseFinish(p, copy, 0, res)
                if (res !== held) p.setItemInHand($ZPHand.MAIN_HAND, res)
                out('eat ' + name + ' ' + before + ': hand now ' + idOf(p.getMainHandItem()) + ', food ' + p.getFoodData().getFoodLevel()
                    + ', effects ' + effects(p))
            } else if (what == 'hunger') {
                p.getFoodData().setFoodLevel(parseInt(arg))
                p.getFoodData().setSaturation(0)
                out('hunger ' + name + ' ' + p.getFoodData().getFoodLevel())
            } else if (what == 'effects') {
                out('effects ' + name + ': ' + effects(p))
            } else if (what == 'food') {
                out('food ' + name + ': level ' + p.getFoodData().getFoodLevel() + ', saturation ' + p.getFoodData().getSaturationLevel())
            } else if (what == 'inv') {
                let inv = p.getInventory(), s = []
                for (let k = 0; k < inv.getContainerSize(); k++) { let st = inv.getItem(k); if (!st.isEmpty()) s.push(k + ':' + idOf(st)) }
                out('inv ' + name + ': ' + (s.length ? s.join(', ') : 'empty'))
            } else out('unknown query ' + what)
        } catch (err) { out('error: ' + err) }
        finally { out('done') }
    }
    ServerEvents.commandRegistry(e => {
        const { commands: C } = e
        e.register(C.literal('zp')
            .then(C.argument('what', $ZPArgs.word())
                .then(C.argument('name', $ZPArgs.word())
                    .executes(ctx => { run(ctx.source.server, $ZPArgs.getString(ctx, 'what'), $ZPArgs.getString(ctx, 'name'), ''); return 1 })
                    .then(C.argument('arg', $ZPArgs.word())
                        .executes(ctx => { run(ctx.source.server, $ZPArgs.getString(ctx, 'what'), $ZPArgs.getString(ctx, 'name'), $ZPArgs.getString(ctx, 'arg')); return 1 })))))
    })
})()
