# creengserver-dedicated-server

> Dedicated server for the pack lives at ~/.local/share/atlauncher/servers/CreEngServer (world "winecraft"); how its mod list was chosen and how to generate true config defaults

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

Created 2026-09-26 at `~/.local/share/atlauncher/servers/CreEngServer`. Forge 43.3.9, level-name `winecraft`, and the world is being regenerated after config tuning. The mods are the instance's jars minus the client-only ones (rubidium, ImmediatelyFast, JEI + addons, Xaero, tooltip/HUD mods, ponderjs, Create_Questing, etc.). SeasonHUD must be on the server because it registers a SERVER config. DistantFriends was removed from the pack entirely (2026-09-26), so it's no longer needed on the server. FTB Quests lines live in `config/ftbquests/quests` and were copied to the server 2026-09-26; re-copy after editing quests.

**Mod sides are recorded in `mod_sides.md`** (instance root; 292 both / 9 server / 40 client as of 2026-09-26, FlowerPatch = both; w2w2 = both, since its strict network channel rejects clients that have it when the server does not). `python3 tools/dump_server_mods.py` copies the both+server jars to `server_mods/`. It matches by mod id (a version bump needs no edit), and jars with no mod id are matched by file name. It exits with an error if a jar has no row. When a mod is added, add its row there; don't hand-maintain a separate list.

Getting real defaults: copy the server into a scratch dir (symlink mods/libraries, copy kubejs), empty `config/` + `defaultconfigs/`, set a new level-name and port, boot until "Done", then send `stop`. This produced 157 config entries + 38 serverconfig files. Client-only configs can't be generated this way.

`LaunchServer.sh`/`.bat` rewrite `user_jvm_args.txt` from their own MEMORY/maxmemory line on every launch, so change memory there, not in user_jvm_args. Both are set to 6G as of 2026-09-26.

Related: memory `configs-edited-backup`, [patchermod-coremod](patchermod-coremod.md).
- As of 2026-09-26 (late) the folder is in ~/.local/share/Trash/files/CreEngServer — confirm with the user before mirroring configs there.
- 2026-09-27: `server_mods/` is stale while mod_sides.md lacks rows (20+ mods added since); The Missing Villages removed from the pack. For a quick live check use memory `scratch-server-verify` instead of the real server.
- 2026-09-28: `~/.local/share/atlauncher/servers/CreEngServer` now holds only `configs_edited/`; the full server (libraries, run.sh) is still in Trash. mod_sides.md now has 381 rows (326 both / 12 server / 43 client), and deploys go through [deploy-pipeline](deploy-pipeline.md) rather than server_mods/.
- 2026-10-11: 395 rows (339 both / 14 server / 42 client). Side rules learned: a mod with a strict SimpleChannel (version predicate `"1"::equals` both ways, e.g. coolrain, w2w2) is `both` even if client-flavoured; data-only recipe packs (MEED) and the mod owning their recipe type (JEED) must be on the server, because recipes sync from it.
- 2026-10-14: 413 rows (355 both / 13 server / 45 client); 18 new mods added (paintings packs, Subterrestrial, Underground Bunkers, Volcanic Caverns, Witch Mobility...). Painting-variant mods are `both` (Forge registry sync).
- 2026-10-14 (later): 414 rows (356 both). Cycle Paintings added as `both` (Serilum convention). It cycles only `#minecraft:placeable` painting variants of the same size and only touches vanilla `Painting` entities. Every pack registers its variants into placeable; leftover untagged textures in Jess'/Rebirth/MC-style jars are unused files. Immersive Paintings has its own cycle mechanic, so leave it alone.
- 2026-10-14 (deploy request): 417 rows (358 both / 14 server / 45 client), 418 jars all covered. Added campsite_structures, restapi, unlitcampfire (both), rsec (server: ServerLevel campfire tick mixin only).
