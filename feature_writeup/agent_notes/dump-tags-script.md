# dump-tags-script

> /dumptags KubeJS command — per-tag and per-item tag dumps + empty/single-member summary, under kubejs/exported/tags/

Moved from agent memory 2026-11-16 (last edited there 2026-09-09). Repo status docs win over any status line here.

`kubejs/server_scripts/_lib/dump_tags.js` — same pattern as the other `_lib` dumps
(`/dumpitemsbymod`, `/dumppotions`, `/dumpfluids`): a `ServerEvents.commandRegistry`
command, op level 2, `JsonIO.write` into `kubejs/exported/`.

`/dumptags [item,block,fluid,entity_type]` (no arg = all four). Per registry `<r>` writes:
- `kubejs/exported/tags/<r>_tags.json` — `{ "ns:tag": ["ns:member", …] }` every tag → members
- `kubejs/exported/tags/<r>_tags_by_<r>.json` — `{ ns: { "ns:member": ["ns:tag", …] } }` member → tags, by mod
- `kubejs/exported/tags/_summary.json` — per registry: `tag_count`, `empty_tags[]`, `single_member_tags[]`, `biggest_tags[]` (top 25)

Verify targets: `empty_tags` (typos / missing mod content), `single_member_tags`
(recipes that aren't really ambiguous), and diff `*_tags_by_*` against a saved copy after
a mod update to catch membership drift.

Built 2026-09-09 alongside the vanilla item-physics pass ([itemphysic-fire-lava-split](itemphysic-fire-lava-split.md)) —
grep `creativeengineering:physics/` in `item_tags.json` to confirm the float/fireproof/
lavaproof tags populated as intended. Callable from other scripts as `dumpTags(['item'])`.
