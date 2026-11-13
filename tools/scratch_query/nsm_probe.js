// Scratch-only (SQ-NSM rows on the new jars): logs where each player logs in, before perfBot's spawn teleport, and the
// respawn target. Copy into srv/kubejs/server_scripts/ and /reload. Lines: "[nsmprobe] ..." in logs/latest.log.
PlayerEvents.loggedIn(e => {
    let p = e.player
    console.info('[nsmprobe] login ' + p.username + ' dim ' + p.level.dimension + ' pos ' +
        Math.floor(p.x) + ' ' + Math.floor(p.y) + ' ' + Math.floor(p.z) +
        ' respawn ' + p.getRespawnDimension().location() + ' ' + p.getRespawnPosition())
})
PlayerEvents.respawned(e => {
    let p = e.player
    console.info('[nsmprobe] respawn ' + p.username + ' dim ' + p.level.dimension + ' pos ' +
        Math.floor(p.x) + ' ' + Math.floor(p.y) + ' ' + Math.floor(p.z))
})
// SQ-NSM-03: count shrapnel entities by type as they join the level (they live a few ticks).
EntityEvents.spawned(e => {
    let t = String(e.entity.type)
    if (t.indexOf('shrapnel') >= 0) console.info('[nsmprobe] spawned ' + t)
})
// SQ-NSM-05: /nsmprobe recipes <text> lists recipe ids whose JSON-ish string or result mentions <text>.
ServerEvents.commandRegistry(event => {
    const { commands: Commands, arguments: Arguments } = event
    event.register(Commands.literal('nsmprobe').then(Commands.literal('recipes').then(Commands.argument('text', Arguments.STRING.create(event)).executes(ctx => {
        let text = String(Arguments.STRING.getResult(ctx, 'text'))
        let n = 0
        ctx.source.server.getRecipeManager().getRecipes().forEach(r => {
            let s = ''
            try { s = String(r.getResultItem()) + ' ' + String(r.getIngredients()) } catch (e) { s = '' }
            if ((String(r.getId()) + ' ' + s).indexOf(text) >= 0) {
                n++
                console.info('[nsmprobe] recipe ' + r.getId() + ' type ' + r.getType() + ' -> ' + s.substring(0, 160))
            }
        })
        console.info('[nsmprobe] recipes matching ' + text + ': ' + n)
        return 1
    }))))
})
