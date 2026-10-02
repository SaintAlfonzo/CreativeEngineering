# Item-loss repro harness

An automated attempt to reproduce the item deletion seen only on the dedicated server (1.0.3 plan, IM).
It runs a scratch dedicated server and a throwaway client on this machine. A proxy between them adds
network lag, and the server can be slowed on purpose. A client script then makes random inventory
moves, and a server script counts every item after each tick.

## Parts

| File | Role |
|---|---|
| `setup.sh OUT` | Builds `OUT/srv` (instance jars minus client-only ones, copies of kubejs/config/defaultconfigs, offline, port 25599) and `OUT/cli` (full copy of mods/kubejs/config, quiet options.txt). Installs the two scripts below as `zz_imrepro_*.js` in those copies only. |
| `lagproxy.py LISTEN TARGET FILE` | TCP proxy (client connects to 25600, forwards to 25599). Each direction is delayed by `FILE`'s `<delay_ms> [jitter_ms]`, which is re-read every 0.5 s. Bytes stay in order. |
| `launch_client.py DIR HOST PORT` | Starts the client from ATLauncher's last logged launch args, as offline user `Tester`. Never prints the command line. |
| `server/imrepro_server.js` | `/imrepro setup` builds a sealed glass box with a chest at y=200 and fills it with a fixed item set. Every tick it counts items in the inventory, 2x2 grid, cursor, chest, shulker and backpack contents, and dropped items. A change from the baseline is logged as `DELTA` (real loss or dupe). Also has `open`, `mark`, `lag <ms/tick>`, `check`, `rebase` and `setupfirst` (console). |
| `client/imrepro_client.js` | Starts once the player is op, sends `/imrepro setup`, then runs rounds: open the chest, make 40 ticks of 1-6 random actions per tick, settle, then log the client's view (`CVIEW`) and ask the server for its view (`SVIEW`). Reads `kubejs/imrepro.json` each round (`phase`, `stop`, `mods`, `burstTicks`, ...). |
| `summarize.py SRV_LOG CLI_LOG [--detail]` | Per phase: rounds, rounds with real loss, ledger deltas, and rounds where client and server disagree after settling (visual desync). |

The random actions are PICKUP (left/right), QUICK_MOVE, SWAP (hotbar and offhand), PICKUP_ALL, a drag
(QUICK_CRAFT), THROW, dropping the cursor stack, closing the screen mid-burst, asking for a reopen, Quark
sort and transfer, opening the backpack, and backpack sort. The inventory crafting result slot is never
clicked, because crafting changes the totals.

## Running

```
W=<scratch dir>
bash tools/item_repro/setup.sh $W
cd $W/srv && mkfifo in.fifo && (sleep infinity > in.fifo &) && nohup bash -c 'bash run.sh nogui < in.fifo > console.log 2>&1' &
echo "0 0" > $W/latency.txt; python3 tools/item_repro/lagproxy.py 25600 25599 $W/latency.txt &
# after "Done (" in console.log:
python3 tools/item_repro/launch_client.py $W/cli 127.0.0.1 25600
# after "Tester joined the game":
echo "op Tester" > $W/srv/in.fifo
# switch phases: edit latency.txt, `echo "imrepro lag 40" > in.fifo`, set "phase" in cli/kubejs/imrepro.json
python3 tools/item_repro/summarize.py $W/srv/logs/latest.log $W/cli/logs/kubejs/client.log --detail
```

Stop with `"stop": true` in imrepro.json, then `kill -TERM` the client (match `--gameDir $W/cli` in
`ps -eo pid=,args=`, and don't print the args), `stop` to the fifo, then `pkill -fx 'sleep infinity'`.

## Gotchas found while building it

- These Mojang names are hidden from KubeJS scripts: `getUUID`, `getTag`/`setTag` and
  `LocalPlayer.closeContainer`. Use `getUuid()`, `getNbt()`/`setNbt()` and the SRG name `m_6915_()`.
  Server command errors only reach the player's chat, so wrap command bodies and log the error.
- `/reload` truncates `logs/kubejs/server.log`. Read `logs/latest.log` for the whole run.
- Sophisticated Core's container messages (`SyncContainerClientDataMessage`) go on
  `sophisticatedcore.network.PacketHandler.INSTANCE`, not the backpacks channel.
- A freshly built backpack ItemStack has 0 upgrade slots until SB initialises it, so the setup has no stack
  upgrade.
