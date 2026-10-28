// Armor Trims item-list cache probe (perf.md 10.2 #1, TODO perf #10, scratch_queue SQ-ATC). Scratch-only:
// copy it into srv/kubejs/server_scripts/zz_atc_probe.js; it is never in the pack.
//
//   /atctest   checks LargeItemLists against a registry scan done here, and times 288 calls of each list
//
// Output: "[atctest] PASS|FAIL|INFO <id>: ..." lines in logs/latest.log. Run it once after boot, then `/reload` and
// run it again (ATC-07 compares the trimmable list across the tag reload). With the pre-fix patcherMod jar the same
// probe gives the uncached baseline: ATC-03 / ATC-05 FAIL (new list every call) and ATC-06 shows the old timing.
(() => {
    const $ATLists = Java.loadClass('gg.hipposgrumm.armor_trims.util.LargeItemLists')
    const $ATArmor = Java.loadClass('net.minecraft.world.item.ArmorItem')
    const $ATReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    // java.lang.System is blocked by KubeJS's class filter: Date.now() for timing, Rhino == (unwrapped identity) for the list
    const $ATRL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const CALLS = 288   // trim recipes on the JEI page

    function log(kind, id, detail) { console.info('[atctest] ' + kind + ' ' + id + ': ' + detail) }
    function idOf(item) { return String($ATReg.ITEMS.getKey(item)) }
    function idSet(list) { let s = {}; list.forEach(i => { s[idOf(i)] = true }); return s }
    function diff(a, b) { return Object.keys(a).filter(k => !b[k]) }
    function timeMs(fn) {
        let t = Date.now()
        for (let i = 0; i < CALLS; i++) fn()
        return Date.now() - t
    }

    function run() {
        let classes = $ATLists.allTrimmableClasses.size()
        let tags = $ATLists.allTrimmableTags.size()
        log(classes == 1 ? 'INFO' : 'FAIL', 'ATC-01', classes + ' trimmable class(es), ' + tags + ' trimmable tag(s)'
            + (classes == 1 ? '' : '; the reference below covers ArmorItem only, read ATC-02 by hand'))

        // reference built here, outside the cache
        let refArmor = {}
        $ATReg.ITEMS.forEach(item => { if (item instanceof $ATArmor) refArmor[idOf(item)] = true })
        let refTrim = Object.assign({}, refArmor)
        $ATLists.allTrimmableTags.forEach(tag => {
            $ATReg.ITEMS.tags().getTag(tag).forEach(item => { refTrim[idOf(item)] = true })
        })

        let trim = $ATLists.getAllTrimmable()
        let armor = $ATLists.getAllItemsOfType($ATArmor)
        let trimSet = idSet(trim)
        let armorSet = idSet(armor)
        let miss = diff(refTrim, trimSet), extra = diff(trimSet, refTrim)
        log(miss.length || extra.length ? 'FAIL' : 'PASS', 'ATC-02', 'getAllTrimmable ' + trim.size() + ' entries, '
            + Object.keys(trimSet).length + ' items, reference ' + Object.keys(refTrim).length
            + (miss.length ? '; missing ' + miss.slice(0, 10).join(' ') : '') + (extra.length ? '; extra ' + extra.slice(0, 10).join(' ') : ''))
        let am = diff(refArmor, armorSet), ax = diff(armorSet, refArmor)
        log(am.length || ax.length ? 'FAIL' : 'PASS', 'ATC-04', 'getAllItemsOfType(ArmorItem) ' + armor.size()
            + ', reference ' + Object.keys(refArmor).length
            + (am.length ? '; missing ' + am.slice(0, 10).join(' ') : '') + (ax.length ? '; extra ' + ax.slice(0, 10).join(' ') : ''))

        let sameTrim = $ATLists.getAllTrimmable() === trim
        let sameArmor = $ATLists.getAllItemsOfType($ATArmor) === armor
        log(sameTrim && sameArmor ? 'PASS' : 'FAIL', 'ATC-03', 'second call same list: trimmable ' + sameTrim + ', ArmorItem ' + sameArmor)

        let chest = $ATReg.ITEMS.getValue(new $ATRL('minecraft', 'diamond_chestplate'))
        let stick = $ATReg.ITEMS.getValue(new $ATRL('minecraft', 'stick'))
        let ok = trim.contains(chest) && !trim.contains(stick)
        log(ok ? 'PASS' : 'FAIL', 'ATC-05', 'contains diamond_chestplate ' + trim.contains(chest) + ', stick ' + trim.contains(stick))

        let tTrim = timeMs(() => $ATLists.getAllTrimmable())
        let tArmor = timeMs(() => $ATLists.getAllItemsOfType($ATArmor))
        let tContains = timeMs(() => $ATLists.getAllTrimmable().contains(chest))
        log(tTrim < 50 && tArmor < 50 ? 'PASS' : 'FAIL', 'ATC-06', CALLS + ' calls: getAllTrimmable ' + tTrim.toFixed(1)
            + ' ms, getAllItemsOfType ' + tArmor.toFixed(1) + ' ms, getAllTrimmable().contains ' + tContains.toFixed(1)
            + ' ms; registry size ' + $ATReg.ITEMS.getKeys().size())

        let last = global.__atcLastTrim
        if (last) {
            let rebuilt = !(last.list == trim)
            log(rebuilt ? 'PASS' : 'FAIL', 'ATC-07', 'trimmable list ' + (rebuilt ? 'rebuilt' : 'NOT rebuilt')
                + ' since the previous run (' + last.size + ' -> ' + trim.size() + ' entries); /reload between runs')
        } else {
            log('INFO', 'ATC-07', 'first run; /reload and run /atctest again')
        }
        global.__atcLastTrim = { list: trim, size: trim.size() }
        log('INFO', 'ATC-DONE', 'done')
    }

    ServerEvents.commandRegistry(event => {
        const Commands = Java.loadClass('net.minecraft.commands.Commands')
        event.register(
            Commands.literal('atctest')
                .requires(src => src.hasPermission(2))
                .executes(ctx => { run(); return 1 }))
    })
})()
