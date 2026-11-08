// Scratch-only probe (never in the pack): copy into a scratch server's kubejs/server_scripts as zz_cc_dict_probe.js.
// Creative Common dict checks of 2026-11-07 (SQ-CC-06): the bark / metal dust tags moved to creativecommon:, the Create
// Framed and SecurityCraft tinted glass keys, and the mirror glass block tags. Output lines start with "[ccdict]".
ServerEvents.recipes(e => {
	var I = global.item, B = global.blocks
	if (!I || !B) return console.error('[ccdict] global.item or global.blocks missing')
	var keys = ['any_bark', 't_metal_dusts', 'createframed_tinted_framed_glass', 'createframed_tinted_framed_glass_pane',
		'createframed_tinted_tiled_glass', 'reinforced_tinted_glass']
	keys.forEach(k => console.info('[ccdict] item ' + k + ' = ' + I[k]))
	console.info('[ccdict] block t_mirror_glass = ' + B.t_mirror_glass)
	console.info('[ccdict] block t_mirror_glass_panes = ' + B.t_mirror_glass_panes)
	console.info('[ccdict] done')
})
