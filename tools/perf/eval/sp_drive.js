// Scratch-only (perf evaluation client, feature_writeup/perf.md section 10): after the first login, idles at spawn,
// then teleports the player through new land, then idles again. Phase marks are "[perfeval] phase ..." lines.
let perfevalStarted = false
PlayerEvents.loggedIn(event => {
	if (perfevalStarted) return
	perfevalStarted = true
	let server = event.server
	let name = event.player.username
	let mark = p => console.info(`[perfeval] phase ${p}`)
	server.runCommandSilent(`gamemode spectator ${name}`)
	mark('joined')
	let t = 90 * 20
	server.scheduleInTicks(t, () => mark('idle_end'))
	for (let i = 0; i < 6; i++) {
		let x = 8000 + i * 1500
		server.scheduleInTicks(t + i * 600 + 1, () => {
			mark(`tp${i} ${x}`)
			server.runCommandSilent(`tp ${name} ${x} 140 -8000`)
		})
	}
	t += 6 * 600
	server.scheduleInTicks(t, () => {
		mark('explore_end')
		server.runCommandSilent(`tp ${name} 0 120 0`)
	})
	server.scheduleInTicks(t + 90 * 20, () => mark('done'))
})
