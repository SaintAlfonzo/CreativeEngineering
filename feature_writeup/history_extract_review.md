# Comment rewrites (plan_rules_fixes.md section 17) - resolved 2026-11-17

`tools/history_extract.py --apply` (2026-11-17) took dates and history clauses out of code comments and filed the dated sentences
in the writeup History. The review of its 792 rewritten comments and 136 undated history-sounding lines was done the same day
(plan_rules_fixes_followup.md F2, six readers by file group, every entry read in the code it sits on).

Result, in round numbers: about 290 comments rewritten in present tense or trimmed (orphaned fragments, "from Creative Engineering",
"up to ...", empty parentheses, stale paths and "was" notes), 8 sentences deleted, about 500 left as they read fine, and the
undated list (136) split into about 90 rewritten and the rest plain English ("used to power", "no longer ticking") or upstream
donor code (Power Grid, Tempad, Vinery `core/`), which F6 leaves alone. Facts a rewrite would lose went to the writeup History;
the near-identical "Moved from ... compost_values" History lines (33) became one line each in creative_delight.md and
creative_brewing.md. Edits were comment-only (checked: the diff outside comment lines is empty for the touched trees).

Left for a later sweep (not on the list): "was X" and "(was creativeengineering:)" notes in a few more Delight scripts,
`CreativeEngineeringItems.java` "migrated to KubeJS" comments, `CommonConfig` "now lives in" pointer comments.

The dated-comment lint stays: `python3 tools/history_extract.py --lint`.
