// Scratch-only diagnostic (never in the pack): copy into a scratch server's kubejs/startup_scripts as zz_nch_remove_diag.js.
// Wraps global.NCH.remove so every call logs its filter (keys with undefined values shown as UNDEFINED) and the calling
// script frames, before KubeJS's own logRemovedRecipes lines for that call. A filter with an undefined or null value at
// the top level is SKIPPED (KubeJS reads {output: undefined} as match-all), so later rows test the rest of the chain. Written for r1106cba (SQ-CBA-09: one remove
// matched about 3600 recipes on the Creative Brewing chain).
;(() => {
	if (!global.NCH || global.NCH._diagWrapped) return
	var orig = global.NCH.remove
	function show(f) {
		if (f === undefined) return 'UNDEFINED'
		if (f === null) return 'NULL'
		if (typeof f !== 'object') return String(f)
		if (Array.isArray(f)) return '[' + f.map(show).join(', ') + ']'
		var o = []
		Object.keys(f).forEach(k => o.push(k + ': ' + show(f[k])))
		return '{' + o.join(', ') + '}'
	}
	global.NCH.remove = function (f, e) {
		var where = ''
		try { throw new Error('x') } catch (err) {
			where = String(err.stack || '').split('\n').filter(l => /\.js/.test(l) && !/nch_remove_diag|neoCraftHelper/.test(l)).slice(0, 3).join(' < ')
		}
		var bad = f !== null && typeof f === 'object' && !Array.isArray(f) && Object.keys(f).some(k => f[k] === undefined || f[k] === null)
		console.info('[nchdiag] remove ' + show(f) + (bad ? ' SKIPPED' : '') + ' @ ' + where)
		if (bad || f === undefined || f === null) return
		return orig.call(global.NCH, f, e)
	}
	global.NCH._diagWrapped = true
	console.info('[nchdiag] NCH.remove wrapped')
})()
