# Privacy: personal data in the pack and its repository

Rules for what personal data may go into anything the pack distributes or may distribute, how the pack treats
players' data, and how the remaining owner steps (history, chat reporting, telemetry) are done. Written 2026-11-22
from the privacy findings of [publish_readiness.md](publish_readiness.md) section 8 (PR-CC-07, PR-RE-02 to -08).
Rule ids `PV-<n>`; [design_rules.md](design_rules.md) section 6b points here.

## 1. Scope

"Distributed or potentially distributed" means all of these, now or later:

- every tracked file in git (the repo is private but may go public, publish_readiness D7) and every past commit;
- every jar we build (own mods, bundles, nested members) and the `deploy/` payload and records;
- project pages, changelogs, CurseForge and Modrinth text, issue and pull request text;
- what our code writes on players' machines and servers (logs, config files, data files).

## 2. Rules

| Id | Rule | Checked by |
|---|---|---|
| PV-1 | **The owner's personal data never goes in.** The public identity is the `SaintAlfonzo` handle and its GitHub noreply address, nothing else: no real name, personal mail address, other handles or accounts, Unix user name, home folder, machine or host name, hardware details, location or time zone. A new public identity detail needs an owner ruling first. | `privacy_lint` |
| PV-2 | **Nobody else's personal data goes in.** No names or handles of friends or players, no names of private servers or worlds that name people, no chat, no screenshots of private messages. Third-party authors appear only as credits, under the public author name they publish under (NOTICE, credits, `mod_authors`). Permission replies stay local and untracked; a NOTICE cites them as "a private message; the maintainer keeps a copy". | `privacy_lint` (names via `.privacy_terms`) |
| PV-3 | **Kinds of files that hold player data are never tracked or shipped:** world saves, `logs/`, crash reports, `hs_err_pid*.log`, screenshots, backups, JFR recordings, `options.txt`, `servers.dat`, `usercache.json`, `ops.json`, `whitelist.json`, `banned-*.json`, launcher account files, JEI per-world bookmarks (`config/jei/world/`), Simple Hats' download cache, `communications/`, perf reports pulled from a server. `.gitignore` lists the ones that sit inside allowed folders. | `privacy_lint` (`path`) |
| PV-4 | **No machine paths in tracked text.** Paths are relative to the instance, or `$HOME` / `~` in shell, or the placeholders `<instance>` and `<scratchpad>` in docs. Tools compute machine paths at run time (`Path.home()`, `ROOT`, the Claude Code project folder as `re.sub('[^A-Za-z0-9]', '-', str(ROOT))`). Command output pasted into a run doc (`ls -l`, `ps`, logs, stack traces) is scrubbed first: owner `user user`, paths as placeholders. | `privacy_lint` (`term`, `home`) |
| PV-5 | **Machine-specific Claude Code settings** (absolute-path permission rules, extra directories) go in the untracked `.claude/settings.local.json`; the tracked `settings.json` uses `~/` paths only. | `privacy_lint` |
| PV-6 | **Commits are authored as the repo identity** (`SaintAlfonzo`, GitHub noreply address), never a personal address. In a new clone, set `git config user.email` before the first commit. Agents commit only as the configured repo author. | review (`git log --format=%ae`) |
| PV-7 | **Our mods never send data off the machine:** no HTTP, sockets, telemetry, analytics, crash upload or update checks (no `updateJSONURL`). Click-to-open links a player clicks are fine. | review (publish_readiness "Clean" line) |
| PV-8 | **What our code logs about players:** name a player only for events an admin needs (by in-game name), at INFO or below. Never log the local client account, UUID next to an address, IP addresses, chat content, tokens or session data. Dev-only bot logs (Creative Devtools) are exempt. | review |
| PV-9 | **Player-owned files stay with the player.** Per-player data we write lives in the world save or the player's own config and is class `player` in `build_deploy.py` `CONFIG_CLASSES`: never shipped by default, never removed by an update. Audits and docs mask personal values ("values masked; the path is enough"). | `build_deploy.py` classes |

Process rules:

- A new personal term (a name, handle, server name) found anywhere goes on a line of the untracked `.privacy_terms`
  file at the instance root, so `privacy_lint` checks for it from then on. Never write the term itself into a tracked
  file, including as a lint pattern, test or example.
- A line that must keep a match (a lint's own patterns) carries `privacy: keep`.
- Agents: before writing pasted output into a tracked file, apply PV-4. When `privacy_lint` fails, fix the file;
  never add the hit to `check_known.csv`.

## 3. Enforcement

`python3 tools/privacy_lint.py` (check_all `privacy_lint`, about 5 s, fast set; so the install gate and the Stop hook
run it). `--jars` adds our built jars and `deploy/latest/` (check_all `privacy_lint_jars`, `--full` only, so the
release gate `build_deploy.py` runs it). It reads the personal terms at run time: the Unix user name (as a path part,
Claude project folder or `ls -l` owner), the global git name and email, and `.privacy_terms`. It also fails on any
mail address except the GitHub noreply ones, `git@github.com`, the Claude trailer and `example.com`, any absolute home
path of a real user name, and any tracked file of a PV-3 kind. Hits print the file and line, never the term.

The lint does not see git history; section 4 covers that.

## 4. Owner steps

### 4.1 Git history (PR-RE-02, publish_readiness D7; TODO `privacy #1`)

The tracked files are clean, but older commits still hold: 19 commits authored with a personal mail address, and
the blobs of everything removed on 2026-11-22 (JEI world folder names with two first names, home paths, the
permission screenshot, perf reports with hardware details, the Forge template changelog). Making this repo public
as it is publishes all of that. Two routes:

1. **Fresh export (simplest, nothing to rewrite):** create a new public repo from the current tree only
   (`git archive HEAD` into a new folder, `git init`, one commit as the repo identity). Keep this private repo as the
   working history.
2. **Rewrite history:** `git filter-repo` with a mailmap that maps the personal address to the noreply address, plus
   `--invert-paths` for `config/jei/world/`, `communications/`, `server/perf/reports/`,
   `config/simplehats_hatdl/`, `development/creativeCommon/changelog.txt`, and `--replace-text` with a local
   (untracked) file of the home path and names. Then force-push `main` and `agent_staging` and re-clone every other
   copy. Every commit id changes, so ids cited in docs (`jar_archive/installed.csv` stamps, run docs) stop matching.

Either way, the GitHub repo keeps old commits reachable by id until GitHub support purges them; ask for a cache purge
after a rewrite.

### 4.2 Players' privacy in the pack

What is already in place: Kiwi's `noMicrosoftTelemetry = true` (`config/kiwi-client.toml`) turns off the vanilla
telemetry sender; Transition's crash report consent is `false`; Debugify's updater is opted out; our mods make no
network calls (PV-7).

To turn off chat reporting and the rest:

1. **Chat reporting (not handled yet; TODO `privacy #2`).** 1.19.2 signs chat with the player's Mojang key, and a signed message can be
   reported to Mojang by any player who receives it. The pack has no mod that stops this. Options:
   - add No Chat Reports (Forge 1.19.2 build) on both sides, through the `add-mod` skill. Its server side strips
     signatures and sends chat as server messages, so nothing a player sends can be reported; the client side
     also blocks signing on servers without it.
   - or set `enforce-secure-profile=false` in the server's `server.properties` (players without keys can join)
     and turn off the "Only Show Secure Chat" client option. Signed chat is still reportable, so this alone is
     not enough.
   - or build the same behavior into Creative Common (a mixin that converts player chat to system messages on the
     server). More work, our own code.
2. **Outbound contacts by third-party mods (each sends the player's IP to an outside host; TODO `privacy #3`).** These can be set off in
   the pack's configs (also back up each to `configs_edited/`):
   - Forge's update check for every mod: `versionCheck = false` in `config/fml.toml`.
   - Distant Horizons auto-updater: `enableAutoUpdater = false` in `config/DistantHorizons.toml`.
   - Simple Hats downloads seasonal hats: `allowUpdates = false` in `config/simplehats-common.toml` (players then
     get only the built-in hats).
   - Immersive Engineering update news (`showUpdateNews`), Immersive Portals (`enableUpdateNotification`), Xaero's
     maps (`update_notifications`) show update notices that query their sites.
3. **Server side.** Vanilla 1.19.2 logs each login with the player's IP address; server logs and `usercache.json`
   stay on the server and are never sent anywhere (PV-3). `server/perf/report.sh` masks addresses in the reports
   it builds; keep sharing those only privately. Lightman's Discord (server jar) relays chat to Discord when it is
   configured; tell players if you turn it on.
4. **Tell players (TODO `privacy #4`).** A short line on the project page or in the pack README: what the pack sends (nothing from our
   mods; listed third-party contacts if kept), whether chat reporting is blocked, and what the server logs.

## 5. Fixes done 2026-11-22

| Finding | What changed |
|---|---|
| PR-CC-07 | Client setup no longer logs a template greeting and the account name (Creative Common, needs a rebuild) |
| PR-RE-03 | The git setup note no longer names the owner's other git identity |
| PR-RE-04 | `config/jei/world/` untracked and ignored; its entries and the Simple Hats cache removed from `configs_edited/config_26_09_26.zip`; the first-names folder masked in `deploy/DEPLOY_LOG.md` and the 1.0.1 and 1.0.2 history snapshots |
| PR-RE-05 | Home path and user name scrubbed from 26 tracked files (scratch batch scripts, run docs, queue rows, TODO text, notes); three tools compute the Claude project folder; `.claude/settings.json` uses `~/` paths and the machine-only rules moved to `settings.local.json` |
| PR-RE-07 | `development/creativeCommon/changelog.txt` (Forge template with third-party addresses) deleted |
| PR-RE-08 | `communications/` and `server/perf/reports/` untracked and ignored (files kept locally); the Rustic Delight NOTICE cites the permission as a private reply (Creative Delight, needs a rebuild) |
| (new) | `config/simplehats_hatdl/` (Simple Hats' downloaded list of supporter UUIDs) untracked, ignored and class `player` in `build_deploy.py` |
| PR-RE-02 | Open: history, section 4.1 |

Three queue rows in `testing/rows/GLD.qb` (SQ-SLT-04, SQ-GLD-12, SQ-PCK-04) now need `B0` set explicitly: their fallback was
an old session's scratchpad path.

## 6. History

- 2026-11-22: created; rules PV-1 to PV-9, `tools/privacy_lint.py`, fixes in section 5.
