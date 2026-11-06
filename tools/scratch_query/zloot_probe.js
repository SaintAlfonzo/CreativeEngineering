// Scratch-only probe of the loot tuning registrations (kubejs/server_scripts/loot/05_tuning.js, global.lootTuning).
// Scratch queue SQ-VTD4-07, SQ-VP-11. Copy into a scratch server's kubejs/server_scripts as zz_zloot_probe.js, never
// into the pack. Reads what the scripts registered at the last load (L.pack pairs, L.add rows); /loottuning has totals.
//   /zloot packs <text>   "[zloot] pack <packed> <- <unpacked> rate R" for pairs whose ids contain text
//   /zloot adds <text>    "[zloot] add <item> share S count C tables [..]" for adds whose item id contains text
// Output: "[zloot] ..." lines in logs/latest.log, then "[zloot] <kind> <text>: N" and "[zloot] done".
(() => {
    function out(s) { console.info('[zloot] ' + s) }
    function txt(v) {
        try { return JSON.stringify(v) } catch (err) { return String(v) }
    }
    ServerEvents.commandRegistry(event => {
        const { commands: Commands, arguments: Arguments } = event
        event.register(Commands.literal('zloot').then(Commands.argument('args', Arguments.GREEDY_STRING.create(event)).executes(ctx => {
            let a = String(Arguments.GREEDY_STRING.getResult(ctx, 'args')).trim().split(/\s+/)
            let kind = a[0], want = a[1] || ''
            try {
                let T = global.lootTuning
                if (!T) out('global.lootTuning missing')
                else if (kind == 'packs') {
                    let n = 0
                    T.packs.forEach(p => {
                        if ((String(p.packed) + ' ' + String(p.unpacked)).indexOf(want) < 0) return
                        n++
                        out('pack ' + p.packed + ' <- ' + p.unpacked + ' rate ' + p.rate)
                    })
                    out('packs ' + want + ': ' + n)
                } else if (kind == 'adds') {
                    let n = 0
                    T.adds.forEach(d => {
                        if (String(d.item).indexOf(want) < 0) return
                        n++
                        out('add ' + d.item + ' share ' + d.share + ' weight ' + d.weight + ' count ' + txt(d.count) + ' tables ' + txt(d.tables))
                    })
                    out('adds ' + want + ': ' + n)
                } else out('unknown kind ' + kind)
            } catch (err) { out('error: ' + err) }
            out('done')
            return 1
        })))
    })
})()
