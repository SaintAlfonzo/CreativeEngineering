// Scratch-only probe for the Creative Brewing optional-dependency matrix (scratch_queue.md "SQ-CBO"). Copy into a
// scratch server's kubejs/server_scripts as zz_cbo_probe.js, never into the pack.
//   /cbo mods      each optional dependency of Creative Brewing: loaded or absent
//   /cbo effects   Armor / Tool Effects tables (global.armorEffects, global.toolEffects): entries per item namespace,
//                  then every entry whose item or effect is not registered ("leak" lines: a guard let an absent
//                  mod's row in)
//   /cbo potions   creativecommon potions and mob effects (Creative Common since 2026-11-06) registered, and the potions of the optional mods CB
//                  touches (long_reach, phantom_bane, Buzzier Bees luck, Alex's Mobs, Quark / Upgrade Aquatic removed)
// Output: "[cbo] ..." lines in logs/latest.log, then "[cbo] done".
(() => {
    const $CBOArgs = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $CBORL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $CBOFR = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    // ModList is not on KubeJS's class allow-list: Platform.isLoaded instead
    const OPTIONAL = ['alexsmobs', 'appleskin', 'better_beacons', 'botania', 'buzzier_bees', 'create_central_kitchen',
        'create_deep_dark', 'dragonloot', 'everythingcopper', 'farmersrespite', 'felsic_gear', 'fruitsdelight', 'geode',
        'immersive_armors', 'immersive_weathering', 'immersiveengineering', 'jeed', 'jei', 'neapolitan', 'potionbundles',
        'quark', 'supplementaries', 'tmted', 'upgrade_aquatic']
    function out(s) { console.info('[cbo] ' + s) }
    function has(reg, id) {
        try { return reg.containsKey(new $CBORL(String(id))) } catch (err) { return false }
    }
    function ns(id) { const s = String(id); return s.indexOf(':') < 0 ? 'minecraft' : s.split(':')[0] }
    function specId(spec) { return spec == null ? null : (typeof spec === 'string' ? spec : spec.effect) }

    function mods() {
        const ml = { isLoaded: m => Platform.isLoaded(m) }
        const on = OPTIONAL.filter(m => ml.isLoaded(m))
        out('mods loaded ' + on.length + ' of ' + OPTIONAL.length + ': ' + (on.join(', ') || '-'))
        out('mods absent: ' + (OPTIONAL.filter(m => !ml.isLoaded(m)).join(', ') || '-'))
    }

    // table: itemId -> specs[] ; sets: [{ids, effects}]
    function scanTable(label, table, byNs, leaks) {
        let n = 0
        Object.keys(table || {}).forEach(item => {
            n++
            byNs[ns(item)] = (byNs[ns(item)] || 0) + 1
            if (!has($CBOFR.ITEMS, item)) leaks.push(label + ' item ' + item)
            ;(table[item] || []).forEach(spec => {
                const e = specId(spec)
                if (e && !has($CBOFR.MOB_EFFECTS, e)) leaks.push(label + ' ' + item + ' effect ' + e)
            })
        })
        return n
    }
    function effects() {
        const A = global.armorEffects, T = global.toolEffects
        if (!A || !T) { out('effects: global.armorEffects ' + !!A + ', global.toolEffects ' + !!T + ' (startup script missing)'); return }
        const byNs = {}, leaks = []
        const pieces = scanTable('armor piece', A.PIECES, byNs, leaks)
        const mounts = scanTable('armor mount', A.MOUNTS, byNs, leaks)
        let sets = 0
        ;(A.SETS || []).forEach(s => {
            sets++
            ;(s.ids || []).forEach(item => {
                byNs[ns(item)] = (byNs[ns(item)] || 0) + 1
                if (!has($CBOFR.ITEMS, item)) leaks.push('armor set item ' + item)
            })
            ;(s.effects || []).forEach(spec => {
                const e = specId(spec)
                if (e && !has($CBOFR.MOB_EFFECTS, e)) leaks.push('armor set effect ' + e)
            })
        })
        const onHit = scanTable('tool onHit', T.ON_HIT, byNs, leaks)
        const held = scanTable('tool held', T.HELD, byNs, leaks)
        out('effects armor pieces ' + pieces + ', sets ' + sets + ', mounts ' + mounts + '; tool onHit ' + onHit + ', held ' + held)
        out('effects items by namespace: ' + Object.keys(byNs).sort().map(k => k + '=' + byNs[k]).join(' '))
        out('effects leaks ' + leaks.length)
        leaks.forEach(l => out('leak ' + l))
    }

    function potions() {
        let cbPot = 0, cbEff = 0
        $CBOFR.POTIONS.getKeys().forEach(k => { if (k.namespace == 'creativecommon') cbPot++ })
        $CBOFR.MOB_EFFECTS.getKeys().forEach(k => { if (k.namespace == 'creativecommon') cbEff++ })
        out('potions creativecommon potions ' + cbPot + ', effects ' + cbEff)
        ;['creativecommon:long_reach', 'creativecommon:phantom_bane', 'buzzier_bees:long_luck',
          'alexsmobs:long_lava_vision', 'alexsmobs:speed_iii', 'quark:resilience', 'upgrade_aquatic:restfulness']
            .forEach(id => out('potion ' + id + ' registered: ' + has($CBOFR.POTIONS, id)))
    }

    function run(what) {
        try {
            if (what == 'mods') mods()
            else if (what == 'effects') effects()
            else if (what == 'potions') potions()
            else if (what == 'all') { mods(); effects(); potions() }
            else out('unknown: ' + what + ' (mods | effects | potions | all)')
        } catch (err) { out('error ' + err) }
        out('done')
    }
    ServerEvents.commandRegistry(e => {
        const { commands: C } = e
        e.register(C.literal('cbo')
            .then(C.argument('what', $CBOArgs.word())
                .executes(ctx => { run($CBOArgs.getString(ctx, 'what')); return 1 })))
    })
})()
