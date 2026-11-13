// Scratch-only probe (r1113 rerun of SQ-HH-1534): prints a player's or perfBot bot's shift state right before and after a
// `pbot use`, so a single till can be told apart from a lost sneak flag. Copy into a scratch server's
// kubejs/server_scripts as zz_zsneak_probe.js, never into the pack.
//   /zsneak NAME        "[zsneak] NAME shift=true|false crouching=true|false pose=... pos=x y z"
(() => {
    const $ZSArgs = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    function out(s) { console.info('[zsneak] ' + s) }
    function report(server, name) {
        try {
            let p = server.getPlayerList().getPlayerByName(name)
            if (p == null) { out(name + ' no player'); return }
            out(name + ' shift=' + p.isShiftKeyDown() + ' crouching=' + p.isCrouching() + ' pose=' + String(p.getPose()) +
                ' pos=' + Math.floor(p.getX()) + ' ' + Math.floor(p.getY()) + ' ' + Math.floor(p.getZ()))
        } catch (err) { out('error ' + err) }
    }
    ServerEvents.commandRegistry(e => {
        const { commands: C } = e
        e.register(C.literal('zsneak').requires(s => s.hasPermission(2))
            .then(C.argument('name', $ZSArgs.word())
                .executes(ctx => { report(ctx.source.server, String($ZSArgs.getString(ctx, 'name'))); return 1 })))
    })
})()
