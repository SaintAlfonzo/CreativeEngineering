// Scratch-only probe for the pack clock helpers (scratch queue SQ-TI-01, feature_writeup/time.md TI-30). Copy into a
// scratch server's kubejs/server_scripts as zz_zti_probe.js, never into the pack.
//   /zti            "[zti] active A clock(11800) ... clock24(11800) ... clock24(0) ... dayNumber(t) ..." for the TI-30 times
//   /zti on|off     sets Creative Calendar's [time] enabled (CalendarConfig.TIME.enabled.set, written to the toml),
//                   then prints as /zti. Leave it on at the end (the pack default).
// Output: "[zti] ..." lines in logs/latest.log, then "[zti] done".
(() => {
    const $ZTI = Java.loadClass('com.SaintAlfonzo.creativecalendar.time.TimeSettings')
    const $ZTICfg = Java.loadClass('com.SaintAlfonzo.creativecalendar.CalendarConfig')
    function out(s) { console.info('[zti] ' + s) }
    function report() {
        out('active ' + $ZTI.active() + ', dayStart ' + $ZTI.dayStart() + ', firstSunrise ' + $ZTI.firstSunrise())
        out('clock(11800) ' + $ZTI.clock(11800) + ', clock24(11800) ' + $ZTI.clock24(11800) + ', clock24(0) ' + $ZTI.clock24(0))
        ;[0, 22299, 22300, 46299, 46300].forEach(t => out('dayNumber(' + t + ') ' + $ZTI.dayNumber(t)))
    }
    ServerEvents.commandRegistry(event => {
        const { commands: Commands } = event
        function run(mode) {
            try {
                if (mode == 'on' || mode == 'off') {
                    $ZTICfg.TIME.enabled.set(mode == 'on')
                    out('set [time] enabled ' + (mode == 'on'))
                }
                report()
            } catch (err) { out('error: ' + err) }
            out('done')
            return 1
        }
        event.register(Commands.literal('zti')
            .executes(ctx => run(''))
            .then(Commands.literal('on').executes(ctx => run('on')))
            .then(Commands.literal('off').executes(ctx => run('off'))))
    })
})()
