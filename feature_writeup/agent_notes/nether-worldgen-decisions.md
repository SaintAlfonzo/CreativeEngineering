# nether-worldgen-decisions

> Nether blocks in Overworld gen: scan + decision sheet (nether_worldgen.md B/F/K/S rows); decisions applied via gen_nether_worldgen.py (surface-rule copies, feature overrides) + rarity floor in biome_config; status in feature_writeup/nether_worldgen.md

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

2026-10-19: the user wanted every Nether block in Overworld worldgen found, then gave directions per row.

- Scan: `tools/nether_worldgen_scan.py` -> `feature_writeup/nether_worldgen_scan.md`. It covers surface rules (the pack's
  noise-settings copy if present, else Terralith's), biome json and modifiers, the hand tables
  JAVA_FEATURES/CODE_ADDED/TYPE_BLOCKS, and structures after swaps (`nether_blocks_scan.scan(load_swaps())`).
- Sheet: `feature_writeup/nether_worldgen.md` section 2. Rows B (biomes), F (features/carvers), K (blocks across
  structures), S (structures). Decision column; blank = open.
- User rules (2026-10-19): **blackstone, basalt and magma are fine in the Overworld**. Leave YUNG's rare gilded obelisk. Remove
  warped/crimson roots and stems from biomes. Scarlet netherrack becomes jasper 90 / crimsite 10.
- Built: `tools/gen_nether_worldgen.py` + `kubejs/generation_scripts/worldgen/nether_worldgen_rules.json` (+ manifest;
  never overwrites unowned files). Surface swaps write full copies of Terralith's overworld + large_biomes noise
  settings into kubejs/data; these must be regenerated on a Terralith update. Weighted mixes use vertical_gradient per
  16-block band (surface-rule noise is 2D). Bunker warts: rule 3 in nether_swap_rules.json.
- Round 2 (2026-10-19): moonlight blue flower patch converted (rules `convert: flower_patch`; nether_forest_vegetation only
  places on nylium) + sprouts out; D&D striated_eroded_overworld -> granite via rules `literal` (Create layered ore
  needs >= 2 layers: LayerPattern.rollNext never repeats the previous one, 1 layer = nextInt(0) worldgen crash);
  structure rules 20 soul_fire->fire, 21 glowstone->yellow corundum (1084 swaps).
- **Flower features go through wild_flowers.js**, which rebuilds them from flower_features.json: after changing any
  flower feature, rerun gen_wild_flowers.py or the old version stays live. Only fresh chunks prove worldgen changes
  (decorated neighbors spill over).
- Fire on soul sand/soil turns back into soul fire on any block update (BaseFireBlock.getState); the ancient city's
  kept soul sand is under its new fire.
- Open rows: B12 (frostfire soul fire), K19 (crying obsidian), K25 (soul lanterns), K28-K33/K35/K36 (crimson/warped
  wood bits). D&D gabbro_blob (granite/gabbro, not layered) left alone.
- Biome split: see [biome-frequency-tuning](biome-frequency-tuning.md). Done 2026-10-19 with a 20,000-column /dumpclimate and
  tools/biome_rarity_floor.py.

Related: memory `feature-writeup-pattern`, memory `scratch-server-verify`, memory `no-published-artifacts`.
