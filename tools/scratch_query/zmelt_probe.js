// Scratch-only probe (never in the pack): copy into a scratch server's kubejs/server_scripts as zz_zmelt_probe.js.
// SQ-MELT rows (Creative Common melting/, feature_writeup/melting.md): watches block positions every server tick and
// logs each change, so a row sees every step (blue -> packed -> ice -> water) and whether a placed block lingered.
//   /zmelt w <label> <dim> <x> <y> <z>   watch a position (dim: overworld, the_nether, the_end or a full id);
//                                        logs "[zmelt] watch <label> <state>"
//   /zmelt show                          "[zmelt] state <label> <state> changes=<n>" for every watch
//   /zmelt clear                         drop every watch
//   /zmelt light <dim> <x> <y> <z>       block and sky light at a position
// Each change: "[zmelt] change <label> t=<server tick> <old state> -> <new state>". Output lines start with "[zmelt] ".
(() => {
    const $ZMStr = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $ZMPos = Java.loadClass('net.minecraft.core.BlockPos')
    const $ZMLight = Java.loadClass('net.minecraft.world.level.LightLayer')
    function out(s) { console.info('[zmelt] ' + s) }
    var watches = []
    function levelOf(server, dim) {
        if (dim.indexOf(':') < 0) dim = 'minecraft:' + dim
        var found = null
        server.getAllLevels().forEach(l => { if (String(l.getDimension()) == dim) found = l })
        return found
    }
    function st(level, pos) {
        var s = String(level.getBlockState(pos))
        return s.replace(/^Block\{/, '').replace(/\}/, '')
    }
    ServerEvents.tick(event => {
        if (watches.length == 0) return
        var t = event.server.getTickCount()
        watches.forEach(w => {
            try {
                var s = st(w.level, w.pos)
                if (s != w.last) {
                    w.changes++
                    out('change ' + w.label + ' t=' + t + ' ' + w.last + ' -> ' + s)
                    w.last = s
                }
            } catch (err) { }
        })
    })
    function run(server, args) {
        try {
            var a = String(args).trim().split(/\s+/)
            var what = a[0]
            if (what == 'w') {
                var level = levelOf(server, a[2])
                if (level == null) { out('error no level ' + a[2]); return 0 }
                var pos = new $ZMPos(parseInt(a[3]), parseInt(a[4]), parseInt(a[5]))
                var w = { label: a[1], level: level, pos: pos, last: st(level, pos), changes: 0 }
                watches = watches.filter(o => o.label != w.label)
                watches.push(w)
                out('watch ' + w.label + ' ' + w.last)
            } else if (what == 'show') {
                watches.forEach(w => out('state ' + w.label + ' ' + st(w.level, w.pos) + ' changes=' + w.changes))
                out('show done ' + watches.length)
            } else if (what == 'clear') {
                out('cleared ' + watches.length)
                watches = []
            } else if (what == 'light') {
                var lv = levelOf(server, a[1])
                var p = new $ZMPos(parseInt(a[2]), parseInt(a[3]), parseInt(a[4]))
                out('light ' + a[2] + ' ' + a[3] + ' ' + a[4] + ' block=' + lv.getBrightness($ZMLight.BLOCK, p) + ' sky=' + lv.getBrightness($ZMLight.SKY, p) + ' ' + st(lv, p))
            } else out('error unknown ' + what)
        } catch (err) { out('error ' + err) }
        return 1
    }
    ServerEvents.commandRegistry(e => {
        var { commands: C } = e
        e.register(C.literal('zmelt').then(C.argument('args', $ZMStr.greedyString())
            .executes(ctx => run(ctx.source.server, $ZMStr.getString(ctx, 'args')))))
    })
})()
