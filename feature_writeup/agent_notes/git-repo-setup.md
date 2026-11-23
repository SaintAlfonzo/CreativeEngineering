# git-repo-setup

> Instance is a git repo (allow-list .gitignore) pushed to github.com/SaintAlfonzo/CreativeEngineering, authored as SaintAlfonzo; third-party jars listed in pack/ instead of committed

Moved from agent memory 2026-11-16 (last edited there 2026-09-27). Repo status docs win over any status line here.

Set up 2026-09-27. Remote `origin` = git@github.com:SaintAlfonzo/CreativeEngineering.git (**public**, owner 2026-11-22; this note is the one place the repository's visibility is recorded, other docs point here), branch `main`. Repo-local author is SaintAlfonzo <334673951+SaintAlfonzo@users.noreply.github.com> (the owner wants commits authored by the repo identity, not a personal one).

`.gitignore` is an allow-list (`/*` then `!/kubejs/` etc.). New top-level files/dirs are ignored unless explicitly opted in. Mods, resource packs and shaders are NOT committed. `tools/gen_pack_manifest.py` writes pack/manifest.json + pack/MODLIST.md (sources, sides, sha1, resource-pack order); re-run it and commit pack/ after any mod change.

**Why:** the instance is ~25 GB (saves, simplebackups, mods). A stray `git add .` once put 3.2 GB into .git.
**How to apply:** never `git add` anything outside the allow-list without asking. After mod changes, regenerate pack/ and update [[mod_sides.md]] (17 jars were unclassified at setup).

**Commit and push (owner, 2026-11-16):** agents commit at the end of each plan phase as the configured author with the Co-Authored-By trailer and push `HEAD:agent_staging`, never `main`; rule text in CLAUDE.md "Build and verify". `agent_staging` was 29 commits behind `main` with nothing of its own on 2026-11-16, so the first push is a fast-forward.

**History rewrite (owner, 2026-11-22):** personal data removed from every commit with `git filter-repo` and
force-pushed; details and the old -> new id map in [privacy.md](../privacy.md) 4.1. Clones made before 2026-11-22 must be
re-cloned, never pushed.
