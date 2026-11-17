// priority: 1500
// Registration helper (global.reg), plan_rules_fixes.md section 6 (D22, V-31): every startup script binds it once to
// its folder's namespace, so a registered id lands in the jar that owns the script.
//   var reg = global.reg.forPack('creativeengineering')
//   StartupEvents.registry('item', e => {
//       reg.item(e, 'zinc_grit')                       // -> creativeengineering:zinc_grit
//       reg.item(e, i.pickled_sea_pickle)              // a full id in the bound namespace is the same
//       reg.item(e, 'jrftl:prepared_flesh')            // another namespace: an override, allowlisted
//   })
// item, block, fluid and effect all take (e, id, type?) and return e.create(...)'s builder, so the chain goes on as
// before: reg.item(e, 'spool').displayName('Spool'). An id in another namespace still registers, but logs once per id,
// and tools/namespace_lint.py fails on it unless tools/namespace_allowlist.csv lists it with a reason.
console.log('Registration helper starting')

global.reg = {

	_warned: {},

	// The full id for a bound namespace: 'id' -> 'ns:id'; 'ns:id' stays.
	fullId(ns, id) {
		id = String(id)
		if (!/^[a-z0-9_.-]+(:[a-z0-9_./-]+)?$/.test(id)) throw new Error('global.reg: invalid id ' + id)
		return id.indexOf(':') < 0 ? ns + ':' + id : id
	},

	forPack(ns) {
		ns = String(ns)
		if (!/^[a-z0-9_.-]+$/.test(ns)) throw new Error('global.reg.forPack: invalid namespace ' + ns)
		var reg = global.reg
		var create = (kind) => (e, id, type) => {
			if (!e) throw new Error('global.reg.' + kind + ' called without the registry event')
			var full = reg.fullId(ns, id)
			if (full.substring(0, full.indexOf(':')) !== ns && !reg._warned[full]) {
				reg._warned[full] = true
				console.log('[reg] ' + kind + ' ' + full + ' registered from the ' + ns + ' pack (override, see namespace_allowlist.csv)')
			}
			return type === undefined ? e.create(full) : e.create(full, type)
		}
		return {
			ns: ns,
			id: (id) => reg.fullId(ns, id),
			item: create('item'),
			block: create('block'),
			fluid: create('fluid'),
			effect: create('effect'),
		}
	},
}

console.log('Registration helper done')
