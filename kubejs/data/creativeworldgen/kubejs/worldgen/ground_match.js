// requires: creativeworldgen
// Structures whose grass/dirt base must never be swapped for the local ground (patcherMod
// worldgen/GroundMatch, creativeworldgen-common.toml [ground_match]), e.g. a deliberate grassy oasis in a desert.
// Structure ids as in /dumpstructuregen; '#tag' works too.
ServerEvents.tags('worldgen/structure', function (e) {
	global.tags.add(e, 'creativeengineering:ground_match/exempt', [
	])
})
