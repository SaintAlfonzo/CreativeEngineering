// global.config — tiny JSON-backed config framework, one real file per category under
// config/creativeengineering/<category>.json (the standard Forge config directory, NOT
// kubejs/config/ - that folder is KubeJS's own auto-generated *.properties, unrelated).
//
// Flat string keys (dotted names as plain strings, e.g. "autorepair.intervalTicks")
// rather than nested JSON objects - keeps the read/merge logic trivial with nothing to
// get wrong, while still reading as organized once a category has more than a couple
// of knobs.
//
// Self-healing: the first time ANY script asks for a key via .get(key, fallback) that
// isn't already in the file, it's written with that fallback immediately. So the file
// always ends up documenting every knob that exists, with no separate "generate the
// default config" step to keep in sync - every feature's knobs show up in its config
// file the first time that feature's code actually runs.
//
// Usage from any startup/server script, after this file has loaded:
//   const cfg = global.config.of('dragon_gear')
//   const rate = cfg.get('autorepair.intervalTicks', 100)
//
// Intended as the standard way to expose a tunable value from ANY feature in this pack
// going forward, not just the one that motivated it (dragon gear autorepair) - see
// [[dragon-gear-autorepair]].
console.log('Config starting')

global.config = {
	_files: {},

	of(category) {
		var path = 'config/creativeengineering/' + category + '.json'
		var cached = global.config._files[path]
		if (cached) return cached

		var raw = null
		try { raw = JsonIO.read(path) } catch (err) { raw = null }

		// Normalize whatever JsonIO handed back (a Java Map wrapper when the file
		// existed, null when it didn't) into a guaranteed plain JS object up front,
		// so every .get() below only ever deals with plain JS property access.
		var data = {}
		if (raw) { for (var k in raw) data[k] = raw[k] }

		var accessor = {
			get(key, fallback) {
				if (data[key] !== undefined) return data[key]
				data[key] = fallback
				global.config._write(path, data)
				return fallback
			}
		}
		global.config._files[path] = accessor
		return accessor
	},

	// Creates config/creativeengineering/ on first write - JsonIO.write does not
	// create parent directories (see server_scripts/_lib/loot_dump.js's own note on
	// this same limitation).
	_write(path, data) {
		try {
			var File = Java.loadClass('java.io.File')
			var file = new File(path)
			var parent = file.getParentFile()
			if (parent && !parent.exists()) parent.mkdirs()
			JsonIO.write(path, data)
		} catch (err) {
			console.warn(`[config] failed to write ${path}: ${err}`)
		}
	}
}

console.log('Config finished')
