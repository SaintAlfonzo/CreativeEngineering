// Dump every structure the game knows about, grouped by mod — names only.
//
//   /dumpstructures  ->  kubejs/exported/structures.json
//
//   "templates" = .nbt structure templates  (data/<ns>/structures/<path>.nbt).
//                 Type  "<namespace>:<path>"  into a Structure Block set to LOAD.
//   "worldgen"  = the minecraft:worldgen/structure registry.
//                 Use  /place structure <namespace>:<id>  — this places the WHOLE generated
//                 structure (village / dungeon / ...), not a single piece.
//
// Jigsaw structures (When Dungeons Arise, YUNG's, Towns & Towers, MES, ...) are assembled
// from dozens of template pieces at worldgen — loading one piece in a Structure Block gives
// you just that fragment. For those, use /place structure.
//
// Wrapped in an IIFE so its helpers don't collide with the other _lib dump scripts.

(() => {

    const $Registry = Java.loadClass('net.minecraft.core.Registry')
    const $HashMap = Java.loadClass('java.util.HashMap')

    const TEMPLATE_PREFIX = 'structures/'
    const NBT_SUFFIX = '.nbt'

    // Java Collection/Set -> plain JS array, via an explicit iterator so Rhino's
    // NativeJavaMap/Object member-access quirks never come into play.
    function toArray(javaIterable) {
        const out = []
        const it = javaIterable.iterator()
        while (it.hasNext()) out.push(it.next())
        return out
    }

    // ResourceLocation[] -> { namespace: sorted string[] }, namespaces sorted too.
    // transformPath(path) may rewrite the path, or return null to drop the entry.
    function groupByNamespace(resourceLocations, transformPath) {
        const byNs = {}
        resourceLocations.forEach(rl => {
            var path = String(rl.getPath())   // rl.getPath() is a java.lang.String — coerce for JS string ops
            if (transformPath) {
                path = transformPath(path)
                if (path == null) return
            }
            var ns = String(rl.getNamespace())
            ;(byNs[ns] || (byNs[ns] = [])).push(String(path))
        })

        const out = {}
        Object.keys(byNs).sort().forEach(ns => {
            out[ns] = byNs[ns].sort()
        })
        return out
    }

    function countEntries(byNs) {
        let n = 0
        Object.keys(byNs).forEach(ns => { n += byNs[ns].length })
        return n
    }

    function dumpStructures(source) {
        const server = source.getServer()

        // ── .nbt templates (Structure Block > LOAD) ────────────────────────────
        // Structure Block only ever reads data/<ns>/structures/<path>.nbt, so that's the
        // complete set of structure-block-loadable names.
        //
        // listResources() returns a TreeMap<ResourceLocation, Resource>; copy into a plain
        // HashMap before touching it (Rhino resolves `.keySet` as get("keySet") first, and a
        // ResourceLocation-keyed TreeMap throws ClassCastException on a String lookup).
        const templateMap = new $HashMap(
            server.getResourceManager().listResources('structures', rl => rl.getPath().endsWith(NBT_SUFFIX))
        )
        const templateKeys = []
        templateMap.forEach((rl, resource) => templateKeys.push(rl))

        const templates = groupByNamespace(templateKeys, path =>
            path.substring(TEMPLATE_PREFIX.length, path.length - NBT_SUFFIX.length)
        )

        // ── worldgen structure registry (/place structure) ────────────────────
        const worldgenKeys = toArray(
            server.registryAccess().registryOrThrow($Registry.STRUCTURE_REGISTRY).keySet()
        )
        const worldgen = groupByNamespace(worldgenKeys, null)

        const result = {
            generated: new Date().toISOString(),
            counts: {
                templateMods: Object.keys(templates).length,
                templates: countEntries(templates),
                worldgenMods: Object.keys(worldgen).length,
                worldgen: countEntries(worldgen),
            },
            templates: templates,
            worldgen: worldgen,
        }

        writeExportJson('kubejs/exported/structures.json', result)

        const msg = `Dumped ${result.counts.templates} .nbt templates (${result.counts.templateMods} mods) + `
            + `${result.counts.worldgen} worldgen structures (${result.counts.worldgenMods} mods) `
            + `-> kubejs/exported/structures.json`
        source.sendSuccess(Text.green(msg), false)
        console.info(msg)
        return 1
    }

    ServerEvents.commandRegistry(event => {
        const Commands = Java.loadClass('net.minecraft.commands.Commands')
        event.register(
            Commands.literal('dumpstructures')
                .requires(src => src.hasPermission(2))
                .executes(ctx => dumpStructures(ctx.source))
        )
    })

})()
