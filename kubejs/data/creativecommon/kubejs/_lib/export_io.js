// writeExportJson(path, data) - JsonIO.write for the /dump* commands, minus its two traps:
//
//   1. JsonIO.write never creates folders (Files.newBufferedWriter straight onto the path), so a
//      write into a missing folder throws NoSuchFileException. Scripts can't mkdir themselves -
//      the class filter blocks java.io/java.nio - so this goes through patcherMod's WorldgenFiles,
//      whose writeText does Files.createDirectories. The .keep marker it leaves is empty and is only
//      rewritten when missing.
//   2. JsonIO.write takes a JsonObject. A JS array converts to null, and a null object means
//      "delete the file" - so an array silently writes nothing. Refuse arrays loudly instead.
//
// Paths are relative to the instance root and must stay under kubejs/ (WorldgenFiles enforces it).

var writeExportJson = (function () {
	var $WorldgenFiles = null
	return function (path, data) {
		if (Array.isArray(data)) {
			throw new Error(`writeExportJson(${path}): JsonIO.write turns an array into "delete the file" - wrap it in an object`)
		}
		var slash = path.lastIndexOf('/')
		if (slash > 0) {
			if (!$WorldgenFiles) $WorldgenFiles = Java.loadClass('com.SaintAlfonzo.creativecommon.util.WorldgenFiles')
			$WorldgenFiles.writeText(path.substring(0, slash) + '/.keep', '')
		}
		JsonIO.write(path, data)
	}
})()

// Also a global: the Creative Devtools dump scripts are another script pack (creative_devtools.md 1) and only see
// globals; its _core/cdv_server.js wraps this.
global.writeExportJson = writeExportJson
