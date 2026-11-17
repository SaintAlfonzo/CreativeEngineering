# repack-stale-resources

> A CD repack member keeps deleted overlay files in build/resources until :<member>:clean; port scripts must skip files the upstream jar already has

Moved from agent memory 2026-11-16 (last edited there 2026-10-26). Repo status docs win over any status line here.

Creative Delight repack members (`ce.forge-repack`, e.g. `repackedDelightful`, `repackedFarmers`): `processResources`
is a Copy, not a Sync, so a file removed from `src/main/resources` stays in `build/resources/main` and ships in the
repack jar. After deleting overlay resources run `./gradlew :<member>:clean :<member>:build --offline`.

`repackJar` fails on any overlay path the upstream jar also holds unless it is in `replaced_resources` (sha1-pinned).
A donor-copy script must skip donor files the upstream already has (Delightful 3.8.1 redrew 3.4.1's cantaloupe
textures; `members/delightful/tools/port_resources.py` keeps 3.4.1's).

**Why:** found 2026-10-25 building the Delightful member (dropped Mulberry Pie files were still in the jar).
**How to apply:** clean-build a member after removing overlay files; check the jar with `unzip -l`. See memory `creative-delight-routing`.

To drop an upstream file from a repack without shipping a copy (2026-11-17, End's Delight's global loot modifier
list): keep its path in `replaced_resources` (sha1-pinned, left out of the upstream copy) and delete the overlay file;
the build accepts an excluded path with no overlay copy. Clean-build the member.
