// Scratch-only (perf evaluation client, feature_writeup/perf.md section 10): logs the frame rate once a second as
// "[perfeval] fps ..." in logs/kubejs/client.log. Never ship in the pack. Heap comes from the JFR recording
// (jdk.GCHeapSummary): java.lang.Runtime is blocked by the KubeJS class filter.
let perfevalTicks = 0
ClientEvents.tick(event => {
	if (++perfevalTicks % 20 != 0) return
	console.info(`[perfeval] fps ${String(Client.fpsString).split(' ')[0]} chunks ${Client.levelRenderer ? Client.levelRenderer.getChunkStatistics() : '-'}`)
})
