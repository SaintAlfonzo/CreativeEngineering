# Third-party bug fixes: Packet Fixer and Debugify Reforged

Two third-party mods that fix vanilla 1.19.2 bugs, evaluated fix by fix before they go into the pack. A fix that
reproduces, is fixed and stays stable gets switched on; a fix that cannot be tested stays off and is listed here with
the reason. Where a fix overlaps Creative Common's own work, the least intrusive solution wins, mine or theirs, and
improvements found for ours are made in Creative Common. Code only moves into Creative Common when ours is better or
when so many Debugify fixes end up off that a port is cleaner (owner, 2026-11-13).

## 0. At a glance

| Part | Status |
|---|---|
| Packet Fixer 3.3.2 (MIT) | **Both sides: DEPLOYED 2026-11-13** (owner): `mod_sides.md` row `both`, one bounded config in `config/packetfixer.properties` and `configs_edited/root_config/` (section 4.2 client values; the generated default was `allSizesUnlimited=true`), changelog bullet. Untested: the scratch run measures how effective each side is (client only, server only, both) and whether the server needs lower values than the client (4.7); CQ-TPF-35 to -36 first. Rows SQ-TPF-30 to CQ-TPF-39 |
| Debugify Reforged 1.1.0 (LGPLv3, both sides, needs Cloth Config, installed) | **BUILT 2026-11-14 (plan_bugfix_1114 2), untested**: `mod_sides.md` row `both`; `config/debugify.json` (+ `configs_edited/root_config/`) written by `tools/scratch_query/tpf_debugify_cfg.py` with `default_disabled` and `opt_out_updater` true and 9 fixes on: MC-84873, MC-200418, MC-214147, MC-232869, MC-223153, MC-121903, MC-72151, MC-206922 (partial: only items with tickCount <= 8 are spared, section 7 SQ-TPF-10), MC-237493 (forced). Off: MC-2025 and MC-224729 (not reproduced), MC-121706 (crashes the boot), every client fix (CQ-TPF-31 to -91 untested). The first boot diffs the file in case Debugify rewrites it (SQ-TPF-00, -01) |
| MC-122477 overlap with Creative Common 2.33 | PLANNED: comparison rows CQ-TPF-60 to -64 (section 4.1) |
| Packet Fixer vs a Creative Common client-side fix | PLANNED: decided by CQ-TPF-35 (section 4.2) |

- **Run r1114 2026-11-14 (server rows, [scratch_run_2026-11-14.md](testing/scratch_run_2026-11-14.md) 8): SQ-TPF PASS 9, FAIL 4 (-01 all-on boot crash on MC-121706, -10 partial, -18 and -19 not reproduced); client rows CQ-TPF not run. Finding: a client with 420 or more jars in `mods/` died at window creation ("stack smashing detected"; fixed 2026-11-14 in Creative Common, [jar_handle_leak.md](jar_handle_leak.md)); the instance has 421 since Debugify and Packet Fixer (TODO `pack/server #39`), so neither mod is the cause, only the count.**
- Run plan for the runner: section 7. Queue section: "SQ-TPF Third-party bug fixes" in
  [scratch_queue.md](testing/scratch_queue.md). TODO: `largecontent/mod-additions #80`.
- Knobs (after install): `config/debugify.json` (one `"MC-nnnn": true|false` per fix, plus `default_disabled`,
  `opt_out_updater`, `gameplay_fixes_in_multiplayer`) and `config/packetfixer.properties` (`nbtMaxSize`, `packetSize`,
  `decoderSize`, `varInt21Size`, `chunkPacketData`, `timeout`, `allSizesUnlimited`). Both are hand-edited configs:
  copy to `configs_edited/root_config/` when set.
- Debugify is integrated since 2026-11-14 (row, config, changelog bullet; P7 for the server set). Packet Fixer is integrated on both sides (row, config, bullet). The first launch with both (23:21) logged one load-time conflict: Packet Fixer's clientbound payload size mixin is skipped because Immersive Portals' `MixinClientboundCustomPayloadPacket` already raises that limit (section 4.2).

## 1. Overview

**Packet Fixer** raises the network size limits that large packs hit: the 2 MiB NBT read quota ("Tried to read NBT
tag that was too big"), the chunk packet buffer cap ("Chunk Packet trying to allocate too much memory on read"), the
compressed and framed packet caps, the 1 MiB clientbound and 32 KiB serverbound custom payload caps, and the connection
timeouts. It applies the same limits on both sides (serverbound too). Its Forge 1.19 mixins all use `defaultRequire: 0`:
an injection that does not match is skipped silently, so only behaviour checks prove it works.

**Debugify Reforged** is an unofficial Forge port of Debugify (isXander) by murao.kun: 69 fixes for open Mojira bugs,
each behind a key in `config/debugify.json`; its mixin plugin skips the mixins of a disabled fix at load (a toggle needs
a restart). Also `defaultRequire: 0`. Gameplay-category fixes (MC-12829, MC-31819) only act in singleplayer unless
`gameplay_fixes_in_multiplayer` is true. Its update checker is switched off with `opt_out_updater: true`.

Decision rules (per fix):

1. **Enable** when the bug reproduces with the fix off (base run), is gone with it on (fix run), and the all-on
   stability rows show nothing new.
2. **Leave off** when the bug does not reproduce in this pack (another mod or Forge already covers it), when the fix
   row fails (the silent `defaultRequire: 0` case), or when neither bug nor fix can be tested (section 2, "off").
3. **Overlap with Creative Common**: compare both on the same repro and on side effects; keep the less intrusive one,
   switch the other off, and carry any improvement into ours (section 4).
4. **Port trigger**: a failed fix that matters (item loss, desync, kicks) gets a TODO row for a Creative Common fix.
   If the run leaves most of the high-impact Debugify fixes off, the owner decides on porting the passing ones into
   Creative Common instead of shipping the jar.

## 2. Reference: every fix and its plan

Forge 43.3.9 already fixes two bugs from the Debugify list, so the Forge port leaves them out ("fabric only"): MC-26757
(tooltips cut off: Forge flips and wraps them, `ForgeHooksClient.gatherTooltipComponents`) and MC-176559 (Mending
resets breaking progress: `MultiPlayerGameMode.sameDestroyTarget` calls `shouldCauseBlockBreakReset`, which ignores
`Damage`). MC-81773, MC-206705 and MC-234898 are on the upstream PATCHED.md but have no mixin in the 1.1.0 jar.
Upstream registers MC-124177 under the typo `MC-124117` (that is the config key).

Tranche = the set in `tools/scratch_query/tpf_debugify_cfg.py` (`@s1`, `@c1` ... `@c5`). "off" = left disabled,
reason given.

| Fix | Bug | Side | Tranche | Test | Plan |
|---|---|---|---|---|---|
| MC-206922 | Drops of mobs killed by lightning vanish | server | s1 | SQ-TPF-10 | test (item loss) |
| MC-84873 | DeathTime 20+ leaves a dead mob forever | server | s1 | SQ-TPF-11 | test |
| MC-200418 | Cured baby zombie villager stays a jockey | server | s1 | SQ-TPF-12 | test |
| MC-214147 | Skeleton in leather armor still turns stray | server | s1 | SQ-TPF-13 | test |
| MC-72151 | Snow golem snowballs hurt wolves | server | s1 | SQ-TPF-14 | test |
| MC-232869 | Adult striders spawn saddled in Peaceful | server | s1 | SQ-TPF-15 | test |
| MC-223153 | Raw copper block uses stone sounds | server | s1 | SQ-TPF-16 | test |
| MC-121903 | Command block minecart cooldown not saved | server | s1 | SQ-TPF-17 | test (applied check) |
| MC-2025 | Penned mobs escape or suffocate on chunk load | server | s1 | SQ-TPF-18 | test (statistical repro; fix saves the hitbox as NBT `AABB`) |
| MC-224729 | Partly generated chunks not saved at shutdown | server | s1 | SQ-TPF-19 | test (region read, group B) |
| MC-31819 | Saturation drains in Peaceful | both (gameplay) | c1 | CQ-TPF-40 | test, singleplayer only (section 4.4) |
| MC-93018 | Wild wolves show breeding hearts | server | c1 | CQ-TPF-41 | test (`InLove`) |
| MC-155509 | Dying puffed pufferfish hurts the player | server | c1 | CQ-TPF-42 | test |
| MC-231743 | Potting a plant does not count the stat | server | c1 | CQ-TPF-43 | test (scoreboard) |
| MC-124117 (MC-124177) | Changing dimension loses client state | server | c1 | CQ-TPF-44 | test, plus Immersive Portals portal path |
| MC-135971 | Ctrl+Q does not work on the crafting result | server | c1 | CQ-TPF-45 | test |
| MC-14923 | Chat spam kick in singleplayer without cheats | server | c1 | CQ-TPF-46 | test (needs a no-cheats world) |
| MC-12829 | Creative flight slows in ladders, vines, scaffolding | client (gameplay) | c1 | CQ-TPF-47 | test (owner 2026-11-13); singleplayer only unless `gameplay_fixes_in_multiplayer` (section 4.4) |
| MC-237493 | Telemetry cannot be disabled | client | c1 | CQ-TPF-48 | FORCE on (owner 2026-11-13), with the investigation in section 4.6 |
| MC-577 | Mouse buttons ignored for inventory controls | client | c2 | CQ-TPF-50 | test (Forge already covers pick, hotbar, swap: check no double action) |
| MC-143474 | Respawn resets the hotbar slot | client | c2 | CQ-TPF-51 | test |
| MC-231097 | Use slowdown stays after the item is dropped | client | c2 | CQ-TPF-52 | test |
| MC-80859 | Dragging over matching stacks hides them | client | c2 | CQ-TPF-53 | test (shot) |
| MC-147605 | Text cursors in several fields | client | c2 | CQ-TPF-54 | test |
| MC-140646 | Shift selection does not scroll a text field | client | c2 | CQ-TPF-54 | test |
| MC-249059 | Terrain loading screen stays 2 s minimum | client | c2 | CQ-TPF-55 | test, plus Immersive Portals (it mixes the same screen) |
| MC-165381 | Dropping the tool delays block breaking | client | c2 | CQ-TPF-56 | test |
| MC-183776 | F3 needs two presses after F3+F4 | client | c2 | CQ-TPF-57 | test |
| MC-79545 | XP bar vanishes at huge levels | client | c2 | CQ-TPF-58 | test (shot) |
| MC-122627 | Tab suggestion box lacks right padding | client | c2 | CQ-TPF-59 | test (shot) |
| MC-151412 | Edit Server Info does not focus the name | client | c2 | CQ-TPF-59 | test |
| MC-233042 | Direct Connection does not focus the address | client | c2 | CQ-TPF-59 | test |
| MC-122477 | Chat key typed into the chat box | client | (comparison) | CQ-TPF-60 to -64 | compare with Creative Common 2.33 (section 4.1) |
| MC-4490 | Fishing line detached while crouching (3rd person) | client | c3 | CQ-TPF-70 | test (shot) |
| MC-116379 | Off-hand punch detaches the fishing line | client | c3 | CQ-TPF-70 | test (shot) |
| MC-127970 | Riptide glitches the off-hand item | client | c3 | CQ-TPF-71 | test (shot) |
| MC-108948 | Boat hovers over slime blocks | client | c3 | CQ-TPF-72 | test (shot) |
| MC-197260 | Armor stand dark when its head is in a block | client | c3 | CQ-TPF-73 | test (shot) |
| MC-93384 | Bubbles at the feet of drowning mobs | client | c3 | CQ-TPF-74 | test (shot) |
| MC-165595 | Guardian beam gone past a large world Time | client | c3 | CQ-TPF-75 | test (shot) |
| MC-90084 | Legs through the boat hull | server | c3 | CQ-TPF-76 | test (shot) |
| MC-69216 | Fishing rod stays cast in spectator | server | c4 | CQ-TPF-80 | test (low impact) |
| MC-129909 | Spectator keeps eating | server | c4 | CQ-TPF-80 | test (low impact) |
| MC-119754 | Firework boost continues in spectator | server | c4 | CQ-TPF-80 | test (low impact) |
| MC-215530 | Freezing kept in spectator | server | c4 | CQ-TPF-81 | test (low impact) |
| MC-215531 | Pumpkin overlay kept in spectator | client | c4 | CQ-TPF-81 | test (low impact) |
| MC-217716 | Nausea overlay kept in spectator | client | c4 | CQ-TPF-81 | test (low impact) |
| MC-193343 | Soul Speed kept in spectator | server | c4 | CQ-TPF-81 | test (low impact) |
| MC-119417 | Spectator keeps a bed | server | c4 | CQ-TPF-82 | test (low impact) |
| MC-179072 | Creeper does not defuse on gamemode switch | server | c4 | CQ-TPF-82 | test (low impact) |
| MC-46766 | Break animation after switching to spectator | client | c4 | CQ-TPF-82 | test (low impact) |
| MC-162253 | Lag spike crossing some chunk borders | client | c5 | CQ-TPF-90 | test (client JFR, base vs fix; Rubidium and Distant Horizons on) |
| MC-89146 | Pistons forget an update on reload | server | - | - | off: the fix only makes a chunk's block-entity map ordered; no deterministic repro |
| MC-88371 | Dragon dives into the void without the exit portal | server | - | - | off: needs a staged End fight; low impact |
| MC-183990 | Silverfish group AI breaks when the target dies | server | - | - | off: no clear pass condition |
| MC-199467 | Entity animations stop after a very long time | server | - | - | off: needs an entity alive for days |
| MC-30391 | Landing particles on slow-falling mobs | server | - | - | off: cosmetic, particle timing not judgeable from a shot |
| MC-132878 | No particles when armor stands burn or blow up | server | - | - | off: cosmetic, same |
| MC-121706 | Skeletons do not look up or down while strafing | server | - | - | off: cosmetic, needs a moving target |
| MC-160095 | End rods break cactus only via pistons | server | - | - | off: no player impact |
| MC-100991 | Fishing rod kills do not count | server | - | - | off: needs a kill by bobber damage, not stageable |
| MC-228976 | Entity collision run on the render thread | client | - | - | off: internal, no observable behaviour to test |
| MC-22882 | Ctrl+Q on macOS | client | - | - | off: macOS only (upstream default off) |
| MC-121772 | Shift scroll on macOS | client | - | - | off: macOS only |
| MC-145748 | Double click sound on a slider | client | - | - | off: needs audio |
| MC-159163 | Double sneak animation | client | - | - | off: sub-frame timing |
| MC-55347 | Long title shows in the next world | client | - | - | off: low impact |
| MC-111516 | Player flickers at high speed | client | - | - | off: elytra speed run, judgement unreliable |
| MC-123739 | Recipe book order | client | - | - | off: the pack uses JEI; the vanilla recipe book order does not matter |

## 3. What the pack changes (planned, after the run)

- Debugify: `mod_sides.md` row `both` (the server needs it for the server-side fixes).
- Packet Fixer: on both sides since 2026-11-13 (row `both`, one shared config). P4 measures each side; if the server
  should be tighter, the server deploy gets its own `config/packetfixer.properties` with the server values.
- `config/debugify.json`: the passing fixes on, everything else off, `default_disabled: true` (a fix added by a future
  version stays off until tested), `opt_out_updater: true`. Shipped to clients and the server (it is read at mixin
  load, so it must be in `config/`, not `defaultconfigs/`).
- `config/packetfixer.properties`: the client values from section 4.2 (shipped 2026-11-13); the server values only in
  the server deploy; never `allSizesUnlimited`.
- MC-122477 in Debugify off unless section 4.1 picks theirs.

## 4. Limits and decisions

### 4.1 MC-122477: Creative Common 2.33 against Debugify

Ours (`ChatOpenArmMixin`, `ChatOpenCharDropMixin`, `util.ChatOpenKeyGuard`, [fixes_compat.md](fixes_compat.md) 2.33)
arms only when chat or command opens and drops one character only if it is the opening key's character, under 250 ms,
with the key still held. Debugify's (`RenderSystemMixin` counts `glfwPollEvents` calls in `flipFrame`,
`TextFieldWidgetMixin` starts counting when any `EditBox` is built and drops characters typed before the count
advances) acts on every text box in the game, and only for a character delivered in the same poll as the box's
creation. Our writeup says the stray character arrives one poll later on this machine, which Debugify's check may not
cover. Expectation: ours is the less intrusive one; CQ-TPF-60 to -64 decide. Possible improvement to ours if the runs
show it: a character arriving after 250 ms, or after the key is released (measure the gap in CQ-TPF-60).

Ours has no config switch. For the comparison the runner builds a scratch-only copy of the installed Common jar with
the two mixins removed from its mixins json (never into `mods/`), as section 7 P5 says.

### 4.2 Packet Fixer: the limits, the least intrusive configuration, and whether Creative Common should do it

The 1.19.2 limits (read from the client jar and Forge 43.3.9):

| Limit | Where | Error |
|---|---|---|
| 2 MiB NBT quota per tag read (`FriendlyByteBuf.readNbt`), so per item stack and per block entity sync tag | both directions | `Tried to read NBT tag that was too big` |
| 2 MiB chunk section buffer (`ClientboundLevelChunkPacketData`) | clientbound | `Chunk Packet trying to allocate too much memory on read` |
| 2 MiB framed (compressed) packet: 3-byte length | both | `length wider than 21-bit` (reader), `unable to fit ... into 3` (writer) |
| 8 MiB decompressed packet (`CompressionDecoder`) | both | `Badly compressed packet - size of N is larger than protocol maximum of 8388608` |
| 1 MiB clientbound custom payload | clientbound | `Payload may not be larger than 1048576 bytes`; **already raised by Immersive Portals** (its mixin wins, Packet Fixer's is skipped) |
| 32 KiB serverbound custom payload | serverbound | `Payload may not be larger than 32767 bytes` |

A chunk packet carries the section buffer plus one sync tag per block entity. Storage blocks whose sync tag holds their
items (and those items' NBT) make the chunk packet large without touching the section buffer (which tops out near
250 KB: 24 sections x 4096 x 20 bits). The owner has hit the chunk packet limit with NBT-heavy storage blocks
concentrated in one chunk (another 1.19.2 pack), so it is a real case: depending on the mix it ends in the per-tag
quota, the 2 MiB frame or the 8 MiB decompressed cap, all on the same chunk packet. CQ-TPF-33 reproduces it with
`/ztpf heavychunk` (campfires with heavy items, whose sync tag carries the items) and with the pack's real storage
blocks, and records which message appears; Packet Fixer covers all four (`nbtMaxSize`, `chunkPacketData`,
`varInt21`, `decoderSize`).

Least intrusive configuration:

- The generated defaults are **`allSizesUnlimited=true`** and `timeout=120`. Unlimited applies to what the server
  accepts from clients too: one client could make the server allocate up to 2 GiB for a single packet. Never ship it.
- Each side reads its own `packetfixer.properties`, so the client and the server can have different limits: the client
  high (what it must read from the server), the server bounded (what it accepts from clients, section 4.7). Candidate:
  client `nbtMaxSize` 64 MiB, `chunkPacketData` 16 MiB, `decoderSize` 64 MiB, `varInt21` 4, `packetSize` 8 MiB;
  server `nbtMaxSize` 16 MiB, `decoderSize` 32 MiB, `varInt21` 4, `packetSize` and `stringSize` vanilla;
  `allSizesUnlimited=false` on both; `timeout` 120 kept (longer than vanilla's 30 s, helps slow joins of a 420-mod
  pack). CQ-TPF-34 to -39 confirm the numbers.
- The client config ships through `config/` with the pack; the server's copy is a separate file in the server deploy
  (`configs_edited/server_config/` is for `defaultconfigs/`; this one lives in the server's `config/`, so `build_deploy.py`
  needs a per-side override: a P7 task).
- **Shipped 2026-11-13: both sides** (owner): the client values above are in the one `config/packetfixer.properties`,
  so the server currently also runs 64 MiB NBT / decoder limits (more permissive than the bounded server candidate).
  The scratch run measures each side's contribution (CQ-TPF-35 client only, -36 server only, -34 both) and decides
  whether the server file should be tighter.
- **Creative Common alternative**: only worth it if the split configuration above cannot be shipped, or if Packet Fixer
  breaks something. A client-only fix is not enough (section 4.7: inventory clicks and creative picks send heavy items
  to the server); CQ-TPF-35 and -38 measure it.

### 4.3 Immersive Portals

Immersive Portals mixes into `ReceivingLevelScreen` (MC-249059), `MultiPlayerGameMode` (MC-165381) and the dimension
change path (MC-124177). Those three rows include a portal step; a clash there means off for that fix, not a pack
change.

### 4.4 Gameplay fixes and multiplayer

`gameplay_fixes_in_multiplayer` stays false (upstream default): MC-31819 and MC-12829 then act only in singleplayer.
Whether multiplayer should get them is the owner's call after CQ-TPF-40 and -47 (MC-31819 changes hunger in Peaceful
on the server; MC-12829 changes creative flight speed on the client).

### 4.5 Silent injections

Both mods use `defaultRequire: 0`: a mixin that matches nothing logs nothing. A fix row that fails while its base row
reproduced is that case; the runner then checks the mixin with `-Dmixin.debug.export=true` on one boot
(`.mixin.out/` in the scratch dir) before calling it a FAIL.

### 4.6 MC-237493 telemetry (FORCE on, owner 2026-11-13)

What 1.19.2 does: `ClientTelemetryManager` is built for each connection. Its constructor reads
`SharedConstants.IS_RUNNING_IN_IDE`; when false (always, outside a dev environment) it asks the account service for a
session (`UserApiService.newTelemetrySession`), which is live when the Microsoft account's `telemetry` privilege is on,
and sends a world-loaded event to `api.minecraftservices.com/events`. When true it uses `TelemetrySession.DISABLED` and
sends nothing. An offline session (the scratch client) always gets the disabled session, so the scratch client cannot
show the difference.

What already kills it: **Kiwi** 8.3.6 (installed) redirects that same field read in the same constructor when
`noMicrosoftTelemetry = true` in `config/kiwi-client.toml` (the pack's value; it is Kiwi's own setting) and logs
`Canceling Microsoft telemetry`. That line is in every client log of a session that joined a world (13 of the last 15
`logs/*.log.gz`; the two without it never joined one). So telemetry is off in the owner's play today, through Kiwi.

Debugify's fix (`mc237493.TelemetryManagerMixin`) modifies the same field read to true. Two mods on one field read:
either they chain or one is skipped with a conflict warning; both choose the disabled session, so the outcome is the
same. FORCE means it must not depend on Kiwi: Debugify on as a second kill, so removing Kiwi or flipping its option
keeps telemetry off. CQ-TPF-48 proves the code path with a mixin export (no account needed) in four states: Kiwi on
or off, Debugify on or off. A traffic check needs the owner's real signed-in client: an optional owner row (a local
forwarder for the four `minecraft.api.*.host` properties that logs method and path only, never headers) is described
in CQ-TPF-48 and is not part of the default run.

### 4.7 What a client sends the server that can exceed the limits

From the 1.19.2 packet classes: the serverbound packets that carry item NBT or free-size data.

| Packet | When | Data | Limit hit |
|---|---|---|---|
| `ServerboundContainerClickPacket` | every click in any inventory or container | the changed slots' item stacks and the carried stack, full NBT, as the client predicts them | 2 MiB NBT per stack on the server: clicking a heavy item (a full shulker box of written books, a filled storage item that keeps contents in NBT) kicks the clicking player |
| `ServerboundSetCreativeModeSlotPacket` | creative players: every inventory change, and Ctrl+pick-block (copies the block entity's full NBT into the item) | one item stack, full NBT | same quota: Ctrl+pick of a heavy storage block kicks a creative player |
| `ServerboundCustomPayloadPacket` | mod packets (Forge channels) | mod-defined | 32 KiB: JEI cheat-mode give of a heavy item sends the stack in JEI's packet; FTB Quests editing a large chapter; any mod that does not chunk its uploads (Create schematics and Exposure already chunk theirs) |
| framing and compression | any of the above | whole packet | 2 MiB framed, 8 MiB decompressed |

So the server side matters even though the original symptom (a client failing to read) is clientbound: once the
client can receive a heavy item, moving it in an inventory sends it back. The server's limits should be raised to a
bound (section 4.2 candidate: 16 MiB NBT), not unlimited, and the 32 KiB payload limit stays vanilla unless a row
shows a mod that needs more. CQ-TPF-38 and -39 reproduce the two item cases.

## 5. File map

| File | What |
|---|---|
| `jar_archive/thirdparty_fixes/` | the two staged jars (Packet Fixer sha1 checked against Modrinth; Debugify from CurseForge file 4705473) |
| `tools/scratch_query/tpf_debugify_cfg.py` | writes `debugify.json` with exactly a set of fixes on (`@s1`, `@c1`..`@c5`, ids, `all`) |
| `tools/scratch_query/tpf_probe.js` | scratch-only `/ztpf` probe: sound, bignbt, bigpayload, chunksize, worstchunk, heavychunk (written, not yet run) |
| `tools/scratch_query/tpf_chunkstatus.py` | chunk statuses in a box of a region dir (SQ-TPF-19) |
| `tools/scratch_query/batches/r1113tpf/` | `s1_common.inc` + `s1_gen.py` -> `s1_base.qb`, `s1_fix.qb` (server tranche); client batches are written by the runner from the queue rows |
| `tools/perf/eval/eval_client.py` | `launch GAME server=HOST:PORT` joins a scratch server at start (added for CQ-TPF-31 to -37) |

## 6. Offline checks (done 2026-11-13)

- Mixin targets of every jar in `mods/` scanned for the classes these fixes touch: no installed mod fixes any of the
  listed bugs (lightning, piston, XP orb, packet and NBT mixins found are feature hooks: Moonlight, Cloud Storage,
  Quark, Allurement, Supplementaries, Create's schematic NbtAccounter accessor).
- Forge 43.3.9 bytecode: MC-26757 and MC-176559 fixed by Forge; MC-577 partly (container screens accept mouse
  buttons for pick block, hotbar keys and offhand swap; not for Inventory and Drop).
- Debugify's config format, tranche coverage (51 of 69 fixes in a tranche) and the upstream id typo checked from the
  jar; `tpf_debugify_cfg.py` output checked; `tpf_probe.js` syntax checked (`node --check`), not run.

## 7. Run plan (for a long-term runner) and test list

A runner executes this with the `scratch-test` skill (part B): claim RUNNER.md, targeted run doc section
"r<date>tpf", no perf session needed for P1 to P4 (targeted), one for P6 (client stability). Every config change below
lives in the scratch dirs only; the instance's `mods/` and `config/` stay untouched until P7. Time estimate: about
8 hours of machine time, plus owner time for the look rows if the runner cannot judge a shot.

Common setup for every server boot: `prep_srv.sh OUT LEVEL A|B`, then copy both jars from `jar_archive/thirdparty_fixes/`
into `OUT/srv/mods/`, `tpf_debugify_cfg.py OUT/srv/config/debugify.json <set>` (nothing = all off), and
`tpf_probe.js` as `OUT/srv/kubejs/server_scripts/zz_tpf_probe.js`. Client: `eval_client.py setup GAME`, the same jars
into `GAME/mods/`, the same config writer into `GAME/config/debugify.json`.

| Phase | What | Boots | Rows | Exit |
|---|---|---|---|---|
| P1 server base | group A, Debugify all off, Packet Fixer defaults | 1 | `s1_base.qb` (SQ-TPF-00, -10 to -18) | the bugs reproduce; a row whose bug does not reproduce = its fix stays off (rule 2) |
| P2 server fix | group A, `@s1` | 1 | `s1_fix.qb` | each reproduced bug gone; a FAIL gets the 4.5 export check |
| P3 server all-on | group A, `all` (client mixins do not load on a server) | 1 | `s1_fix.qb` again + SQ-TPF-01 | same results as P2, no new errors: no interaction between fixes |
| P3b chunk save | group B, two levels: all off, then `@s1` | 2 | SQ-TPF-19 | proto chunks saved only with the fix |
| P4 Packet Fixer | scratch server + joined client (`eval_client.py launch GAME server=localhost:25599`); both sides already ship Packet Fixer, so `prep_srv.sh` and `eval_client.py setup` copy it: rows that need a side without it delete it from `srv/mods` or `GAME/mods` | 5 to 6 | the shipped state first (CQ-TPF-34 both, -35 client only, -36 server only), then SQ-TPF-30, CQ-TPF-31 to -39 | 4.2 and 4.7 decided: per-side effectiveness known, server file tightened or confirmed |
| P5 client tranches | singleplayer world, one boot per tranche, base (off) then fix (`@cN`) | 10 | CQ-TPF-40 to -59, -70 to -82, -90 | per fix enable or off; the MC-122477 comparison CQ-TPF-60 to -64 (4 boots) |
| P6 stability | candidate set (every passing fix) on a joined client + server, 20 minutes of play route with a portal, a Create contraption, JEI, chat | 1 + 1 | CQ-TPF-91 | no new errors in either log; JFR without new hot spots |
| P7 install (game closed, owner present or approved) | `add-mod` skill: jars already in `mods/`; Packet Fixer already shipped on both sides (2026-11-13), P7 only applies the P4 verdict (tighter server file or not); `mod_sides.md` rows, per-side Packet Fixer configs (client file in `config/`, server file in the server deploy), configs into `config/` and `configs_edited/root_config/`, changelog bullet, writeup status | - | - | Creative Common change only if 4.1 or 4.2 chose ours |

Order inside a phase follows the queue rows. Base and fix boots of a tranche use the same world copy (copy the world
dir before the base boot; the fix boot starts from the copy).

Tests (rows in the queue section "SQ-TPF Third-party bug fixes"):

- [~] SQ-TPF-00, -01 boot and all-on stability (server): run r1115 (shipped config, [scratch_run_2026-11-15.md](testing/scratch_run_2026-11-15.md) 8): -00 PASS (Debugify 8 server fixes on, Packet Fixer applied), -01 PASS (shipped `debugify.json` boots, SQ-TPF-11 to -17 PASS again). Earlier: run r1114 ([scratch_run_2026-11-14.md](testing/scratch_run_2026-11-14.md) 8): -00 PASS; -01 FAIL: `all` crashes the server boot on MC-121706 (`BowAttackGoalMixin`, `@Shadow field f_25782_ was not located`; Old Guns 3.7.3 is the only other jar with mixins on `RangedBowAttackGoal`); the other 68 together give the P2 verdicts, no new error. With no `debugify.json` in the instance every fix runs: TODO `largecontent/mod-additions #81`
- [~] SQ-TPF-10 to -19 server tranche s1: run r1114: -11 to -17 PASS (bug reproduced on base, gone with `@s1`); -10 FAIL (MC-206922 partial: Debugify cancels `thunderHit` only for items with tickCount <= 8, so a longer bolt still burns the drops; P2 0 of 1, P3 3 of 4 runs; ItemPhysic 1.6.7 likely a second burn path: fix ineffective in this pack); -18 FAIL (MC-2025 not reproduced: no cow ever left the pen, every loss a death; the fix does save `AABB`: stays off unless the owner wants it on that alone); -19 FAIL (MC-224729 not reproduced: base saves proto chunks too; fix stays off)
- [ ] CQ-TPF-34 to -36 effectiveness per side (both, client only, server only): run first
- [~] SQ-TPF-30 PASS in run r1114 (generated file has the 11 keys; vanilla, server, client and shipped files load with no error); CQ-TPF-31 to -39 Packet Fixer (storage-heavy chunk, serverbound items)
- [ ] CQ-TPF-40 to -48 player tranche c1 (with MC-12829 and the MC-237493 telemetry check)
- [ ] CQ-TPF-50 to -59 input and UI tranche c2
- [ ] CQ-TPF-60 to -64 MC-122477 comparison
- [ ] CQ-TPF-70 to -76 render tranche c3
- [ ] CQ-TPF-80 to -82 spectator tranche c4
- [ ] CQ-TPF-90 chunk border lag c5
- [ ] CQ-TPF-91 candidate set stability

## 8. History

- 2026-11-13: scan of open 1.19.2 bugs (Debugify's 1.19.2 list, Forge 43.3.9 bytecode, every installed mixin);
  Packet Fixer 3.3.2 and Debugify Reforged 1.1.0 chosen and staged in `jar_archive/thirdparty_fixes/`; plan, tools,
  server batches and queue rows written; nothing booted (owner: plan for a long-term runner, no run now).
- 2026-11-13 (later): owner added MC-12829 (tested) and MC-237493 (FORCE on, telemetry investigation: Kiwi already
  kills it, section 4.6); chunk packet limit confirmed reachable by the owner with storage-heavy chunks (section 4.2
  rewritten, `/ztpf heavychunk`); serverbound analysis (4.7); both jars found in `mods/` (copied 23:20, outside this
  session) with Packet Fixer's unlimited default; Immersive Portals already raises the clientbound payload limit.
- 2026-11-13 (later): Packet Fixer shipped to clients (owner): `mod_sides.md` row `client`, bounded client config
  (4.2) in `config/` and `configs_edited/root_config/`, changelog bullet; clarified same day: row `both`, server included; the scratch run measures each side.
- 2026-11-14: run r1114 phase 1 ran P1, P2, P3 and P3b (server rows; section 7 test list). Results above; TODO rows `largecontent/mod-additions #80` (updated), `#81`, `pack/server #39`.
- 2026-11-14 (later, plan_bugfix_1114 2): Debugify installed (P7 server part): config with the r1114 server set minus MC-2025 and MC-224729, plus MC-237493; `mod_sides.md` row; changelog bullet. Rows SQ-TPF-00, -01, -11 to -17 to rerun on the shipped file.
