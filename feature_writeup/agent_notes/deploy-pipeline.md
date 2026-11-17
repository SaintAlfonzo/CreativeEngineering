# deploy-pipeline

> tools/build_deploy.py: update payloads diffed against the --snapshot deployment snapshot (deploy/DEPLOYED.json) + verify.sh; stages deploy/latest/ (mods=server+client, client_mods=client-only, kubejs/config/defaultconfigs, changed packs, removals_*.txt) and logs versions in deploy/DEPLOY_LOG.md + deploy/history/<ver>.json; current version and history: feature_writeup/deploy.md

Moved from agent memory 2026-11-16 (last edited there 2026-09-28). Repo status docs win over any status line here.

Built 2026-09-28 at the user's request ("ensure my pack is ready to be deployed to another client and server").

**Versions:** the user said 1.0.0 is what was deployed when the server started running (2026-09-26, world winecraft). Its history json was reconstructed: server files hashed from ~/.local/share/Trash/files/CreEngServer, the client side from the 609cd21 pack/manifest.json (earliest record). Server-made files not in dev or git (config/lightmansdiscord_messages.txt) were left out so they never become removals. 1.0.1 (committed 'c566438 1.0.1 Push') removes creativeengineering-1.0.0.jar + themissingvillages on both sides. A 1.0.2 (patcherMod jar bump) was staged and then undone on 2026-09-28: 1.0.1 is current, and the user will cut 1.0.2 when new features land. The user once auto-bumped to 1.0.2 by accident and deleted the json by hand, which left a stray log entry: to drop a version, remove its json AND its `<!-- deploy:X -->` log section.

**Client rule (owner, 2026-09-28):** the client always gets everything the server gets, server-only mods included, because clients also host single-player worlds. CATEGORIES encodes it: every category targets the client.

- **Layout:** `deploy/latest/` is the payload and is wiped each build (git-ignored). `deploy/DEPLOY_LOG.md` and `deploy/history/<ver>.json` (sha1 of every shipped file + mod sides) ARE tracked (.gitignore `!/deploy/`, `/deploy/latest/`). The next build diffs against the newest *other* version; rebuilding the same version replaces its entry.
- **Version:** omitting `--version` bumps the patch. Pass `--version X` to rebuild an undistributed version (I auto-bumped to 1.0.1 by mistake once and had to delete it).
- **Contents:** kubejs/config/defaultconfigs = files not ignored by .gitignore, minus EXCLUDE (kubejs/archive, quest_working, *.bak, xaero *.backup0). RUNTIME_INPUTS re-adds ignored files code reads: `kubejs/exported/worldgen/climate/*.json` (BiomeLayoutTuner samples; without them the scratch server logged "need a climate sample" and biome multipliers were skipped). `configs_edited/server_config/*` is merged into defaultconfigs (sereneseasons/seasonhud server tomls weren't in defaultconfigs). Resource/shader packs ship only when new or changed (`--full` for all).
- **Preflight:** a jar with no mod_sides row is an error. structure_config regenerated into a temp copy of the worldgen dir must equal the file, and gen_structure_groups sync on the copy must say "already covers every structure" (error). Creative Common jar vs build/src, stale pack/manifest.json and a dirty tree are warnings.
- **Verified 2026-09-28:** scratch server booted from deploy/latest (338 server jars) in 61-91 s; /cdevplace placed 80/82 datapack + house-jar structures (2 takesapillage terrain refusals); check_structure_live: 0 mismatches on 150 sets; biome tuning solved.

**Why:** the user copies the payload by hand to a separate client and server. Copying over a folder never deletes anything, so removals_*.txt matter.
**How to apply:** after mod/script changes, run `python3 tools/build_deploy.py --notes "..."`. Add mod_sides rows first. The user may add jars mid-session (bushierflowers appeared at 17:20). Related: [creengserver-dedicated-server](creengserver-dedicated-server.md), memory `scratch-server-verify`, [git-repo-setup](git-repo-setup.md).

Owner rule (2026-09-30): no content test before a deploy, just update the deploy folder.

**Update mode + deployment snapshot (built 2026-09-30, feature_writeup/deploy.md DP-):** the user wants updates diffed against a deployment snapshot that they start by hand after copying a payload to the server. `--snapshot [VER] [--target] [--date]` writes deploy/DEPLOYED.json (tracked; records the history json's sha1) and a "- Deployed:" log line. Default mode is update from the older server/client mark: only new/changed files in every folder + removals + verify.sh/.ps1 (mods/ hashes of the base). `--mode install` = everything incl. all packs. A deployed version can't be rebuilt (error). kubejs is filtered by KUBEJS_CLASSES (runtime/client/dev; unclassified = error); RUNTIME_INPUTS is gone because exported/ never ships now (the climate samples moved to generation_scripts/worldgen/climate/*.json.gz). Scratch-test recipe: import build_deploy and patch OUT/HISTORY/LOG/DEPLOYED/JAR_ARCHIVE to a scratch dir.

**In-tree mods (2026-10-01):** `IN_TREE_MODS` in build_deploy.py lists (Gradle project = folder under development/, jar prefix, must be in mods/): creativeCommon/creativeengineering and exposureBackport, both required. Preflight and jar archiving loop over it; each project has its own jar_archive/. See [root-gradle-build](root-gradle-build.md).

**Config classes (2026-10-02, user request):** `CONFIG_CLASSES` + `--config functional|full` (default functional). Player class (`*-client.*`, jei/ except blacklist.cfg + the 3 sort-order inis (user: functional), xaero/ except common.cfg/server_profiles/*_server.json, waila/, video/UI files) ships only with full; a third field = ship in functional through that version (trashslot-common.toml through 1.0.3, the user's one-off). Player files are never removals; the changelog lists them as "no longer shipped". Pitfall hit: collect() reuses `version` for jar versions, so its parameter is `pack_version`.

Note: the gradle build itself copies a new jar version into jar_archive/ (not only staging).

Staging a stale mods/ jar is the risk: compare md5 of mods/ vs development/*/build/libs before building.
CONFIG_CLASSES has `DistantHorizons.toml` player through 1.0.4 (one-off push, same pattern as trashslot-common.toml).
Per-version staging and deploy record (status pass 2026-11-16 removed the 1.0.2 to 1.0.8 entries that were here): `deploy/DEPLOY_LOG.md` (one section per version, "- Deployed:" lines), `deploy/DEPLOYED.json`, [deploy.md](../deploy.md) section 8 and the `plan_1.0.x.md` headers.
