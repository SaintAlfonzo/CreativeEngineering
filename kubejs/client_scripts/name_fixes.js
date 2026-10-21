// Display-name fixes from the names audit (feature_writeup/names.md section 11): raw lang keys and
// capitalization. The names come from kubejs/generation_scripts/names/name_fixes.json, which
// tools/name_audit.py writes from the client name dump; hand-written exceptions go in name_rules.json
// next to it, not here.
//
// Each name is added in the namespace whose lang file set the key: the game merges lang per namespace, so
// a key set under another namespace (KubeJS's 2-argument e.add uses minecraft) could lose to the original.
// Hand-written names stay in namechanges.js.
ClientEvents.lang('en_us', e => {
	var path = 'kubejs/generation_scripts/names/name_fixes.json'
	var data
	try { data = JSON.parse(String(JsonIO.readString(path))) } catch (err) {
		console.error('[name fixes] could not read ' + path + ': ' + err)
		return
	}
	var total = 0
	Object.keys(data.namespaces).forEach(ns => {
		var names = data.namespaces[ns]
		Object.keys(names).forEach(key => {
			e.add(ns, key, names[key])
			total++
		})
	})
	console.info('[name fixes] ' + total + ' names in ' + Object.keys(data.namespaces).length + ' namespaces')
})
