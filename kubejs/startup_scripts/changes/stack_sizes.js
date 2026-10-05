// Max stack sizes for vanilla and other mods' items (ItemEvents.modification runs at startup; restart to apply).
//
// The policy is in tools/gen_stack_sizes.py (rules) and its output, generation_scripts/stacks/stack_sizes.json
// (size -> item_dict keys, raw ids where an item has no key). Edit the rules and rerun the tool; don't edit the JSON.
// Calls are unprefixed (setMaxStackSize, not kjs$setMaxStackSize), see func/tune_tools.js.
// Gear stack sizes stay in global.tuneTools / global.tuneArmor.

ItemEvents.modification(e => {
	var i = global.item
	var path = 'kubejs/generation_scripts/stacks/stack_sizes.json'
	var data = null
	try { data = JSON.parse(String(JsonIO.readString(path))) } catch (err) {
		console.error('[stack_sizes] could not read ' + path + ': ' + err)
		return
	}

	var applied = 0
	var unknown = []
	var failed = []

	Object.keys(data.sizes).forEach(size => {
		var n = Number(size)
		data.sizes[size].forEach(entry => {
			var id = entry.indexOf(':') >= 0 ? entry : i[entry]
			if (typeof id !== 'string') {
				unknown.push(entry)
				return
			}
			try {
				e.modify(id, item => item.setMaxStackSize(n))
				applied++
			} catch (err) {
				failed.push(id)
			}
		})
	})

	console.info('[stack_sizes] set ' + applied + ' stack sizes')
	if (unknown.length) console.warn('[stack_sizes] no item_dict key for: ' + unknown.join(', ') + ' - rerun tools/gen_stack_sizes.py')
	if (failed.length) console.warn('[stack_sizes] could not set ' + failed.length + ': ' + failed.slice(0, 20).join(', ') + (failed.length > 20 ? ' ...' : ''))
})
