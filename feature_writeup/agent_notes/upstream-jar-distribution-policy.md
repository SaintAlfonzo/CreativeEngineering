# upstream-jar-distribution-policy

> Unmodified third-party donor jars (ARR) are only passed to local dev instances; at publish the pack points to official sources and a Creative Common function files them into the upstream folder. Do not raise rehosting/licence concerns about donor jars

Moved from agent memory 2026-11-16 (last edited there 2026-11-10). Repo status docs win over any status line here.

Donor jars (`*_upstream/` folders, ARR mods such as Vinery, Farmer's Structures, Miner's Delight, and any future one such as Biomes O' Plenty) are shipped unmodified and only to the owner's local development instances. They are not a rehosting problem.

**Publish plan (owner, 2026-11-09):** the published pack's distribution points to the official original sources of every needed jar (CurseForge/Modrinth, author-uploaded builds). A function, probably in Creative Common, detects those specific jars (by pinned sha1, as `UpstreamJars` does) and moves them into the upstream folder. We never host a third-party jar ourselves.

**Planned:** merge all `<name>_upstream/` folders into one single upstream folder (not done yet; touches every `UpstreamJars`, `build_deploy.py` categories, `tools/*` readers, `.gitignore`).

**Why:** the owner does not want the rehosting question raised again on every ARR donor candidate.
**How to apply:** when evaluating a donor mod, do not flag unmodified-jar distribution or CurseForge no-rehost terms as a blocker. Still relevant: reading art or data from the jar into our own ids, and copying files out of it into our own jars, need the author's permission; that is a separate question. Related [farmers-structures-donor](farmers-structures-donor.md), [arr-mod-overrides-route](arr-mod-overrides-route.md).
