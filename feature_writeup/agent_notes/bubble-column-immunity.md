# bubble-column-immunity

> #creativeengineering:bubble_column_immune entity tag + BubbleColumnImmunityMixin (HEAD-cancel BubbleColumnBlock.entityInside m_7892_); status in feature_writeup/mob_block_tweaks.md

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

Built 2026-10-05 into patcherMod 1.0.5.

- The mixin is `BubbleColumnImmunityMixin`, in the common "mixins" list. It cancels `BubbleColumnBlock.entityInside` (m_7892_) at HEAD for entity types in the tag.
- The tag is in `kubejs/server_scripts/tags/entitytags.js`, which is the first entity_type tag script. Members: vanilla squid and glow squid, plus AM orca, cachalot_whale, giant_squid (+ _part), hammerhead_shark and frilled_shark.
- **Why the hook is on the block and not on Entity.onAboveBubbleCol/onInsideBubbleColumn:** mods override those methods. UA Nautilus and Thrasher no-op both, so they are already immune. A block hook covers every entity.
- No "reaver" entity exists in the pack. The user may have meant the AM sharks.
