// priority: 2000
// global.genText(path): text of a file under the instance's kubejs/ folder, or null when missing, like
// JsonIO.readString. For generated data (kubejs/generation_scripts/...) a missing instance file falls back to the copy
// inside the Creative jar that ships it (WorldgenFiles), so the jars work in an instance without the pack's kubejs/
// folder. The instance file always wins: /cdevgen output and hand edits apply with /reload, no rebuild.
//   var cfg = JSON.parse(String(global.genText('kubejs/generation_scripts/worldgen/season_types.json')))
// global.genJson(path): the same, parsed; null when missing.

global.genText = function (path) {
	var text = global.genText._files.readText(String(path))
	return text == null ? null : String(text)
}
global.genText._files = Java.loadClass('com.SaintAlfonzo.creativecommon.util.WorldgenFiles')

global.genJson = function (path) {
	var text = global.genText(path)
	return text == null ? null : JSON.parse(text)
}
