// Scratch-only probe (never in the pack): copy into a scratch server's kubejs/server_scripts as zz_cc_phantom_probe.js.
// SQ-CC-10b / SQ-PSU-04 without the 60-120 s spawner wait: /zph <passes> runs that many passes of a FRESH vanilla
// PhantomSpawner (nextTick 0, so each pass spawns at once) in the overworld with spawnEnemies on, and prints the
// phantoms each pass made, the overworld players, their Phantom Bane state and time-since-rest stat.
// Common's PhantomSpawnerBaneSkipMixin and Better Beacons' mixin apply to these passes as to the real one.
(() => {
    const $ZPHArgs = Java.loadClass('com.mojang.brigadier.arguments.IntegerArgumentType')
    const $ZPHSpawner = Java.loadClass('net.minecraft.world.level.levelgen.PhantomSpawner')
    const $ZPHStats = Java.loadClass('net.minecraft.stats.Stats')
    const $ZPHReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    function out(s) { console.info('[zph] ' + s) }
    function run(server, n) {
        try {
            let level = server.overworld()
            let bane = $ZPHReg.MOB_EFFECTS.getValue(new ResourceLocation('better_beacons:phantom_bane'))
            server.getPlayerList().getPlayers().forEach(p => out('player ' + p.getName().getString() + ' bane=' + (bane != null && p.hasEffect(bane)) +
                ' rest=' + p.getStats().getValue($ZPHStats.CUSTOM.get($ZPHStats.TIME_SINCE_REST)) + ' y=' + p.getY() +
                ' sky=' + level.canSeeSky(p.blockPosition())))
            out('skyDarken=' + level.getSkyDarken() + ' seaLevel=' + level.getSeaLevel() + ' difficulty=' + level.getDifficulty())
            let total = 0
            for (let k = 0; k < n; k++) {
                let c = new $ZPHSpawner().tick(level, true, false)
                total += c
            }
            out('passes ' + n + ' phantoms spawned ' + total)
        } catch (err) { out('error ' + err) }
        out('done')
    }
    ServerEvents.commandRegistry(e => {
        const { commands: C } = e
        e.register(C.literal('zph').then(C.argument('n', $ZPHArgs.integer(1, 200))
            .executes(ctx => { run(ctx.source.server, $ZPHArgs.getInteger(ctx, 'n')); return 1 })))
    })
})()
