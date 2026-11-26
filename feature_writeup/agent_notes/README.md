# Agent notes

Subsystem mechanics, gotchas, crash causes and past decisions that agents need when working on a part of the pack. Moved out of agent memory 2026-11-16 (136 files; review: [process_review.md](../process_review.md)). This index is imported by CLAUDE.md, so it loads every session; open a note before working on its subsystem.

Rules: status (built, deployed, tested, versions) lives in [README.md](../README.md), [todo/TODO.md](../todo/TODO.md) and the plans; a note that disagrees with them is stale, fix the note. New mechanics or gotchas go here as a new note plus one index line, not into memory. Notes are tracked in git and pushed.

## Standing rules (core ones are in CLAUDE.md: helpers, one-place items, no emoji, GPLv3, no artifacts, configs_edited, no conversions, no advancements, redact token if reading process cmd lines)
- [NCH.cfg config values](config-driven-recipe-values.md); [Git setup](git-repo-setup.md)
- [TODO layout](todo-layout.md)
- [Status pass 2026-11-16](status_pass_2026-11-16.md) — status lines stripped from 33 notes (pointers to the owning docs), 31 checked and left; doc disagreements for the owner
## Tooling, debugging, testing
- Process cmd lines OK only with token redacted (CLAUDE.md), never bare `pgrep -af`/`ps -f`: [CreEngServer](creengserver-dedicated-server.md)
- [/cdevgen](cdevgen-generator-runner.md); [/dumpall refresh](dumpall-refresh-workflow.md); [/dumptags](dump-tags-script.md); [perf JFR](perf-profiling-jfr.md); [item-loss repro](item-loss-repro.md)
- [@Overwrite drops @SubscribeEvent](overwrite-drops-subscribeevent.md) — repeat it on the mixin method; "channel already registered" x40 at load means a recovery reload, read the first resource pack error
- [Helpers in a mixin package crash](mixin-package-helpers.md) — no nested/inner/helper classes under a mixins.json `package`; IllegalClassLoadError at load
- [Deploy pipeline](deploy-pipeline.md); [root Gradle](root-gradle-build.md); [patcherMod = Creative Common since 2026-11-04](creative-common-coremod.md)
- [Jar startup scripts](kubejs-jar-startup-scripts.md): Creative Common loads `kubejs_startup/` from jars; each script pack has its own scope, only `global` is shared
- [Farmer's Structures donor](farmers-structures-donor.md) — ARR 1.0.6 jar read from farmers_upstream/, converted in memory by Creative Common
- [Repack stale resources](repack-stale-resources.md) — clean-build a CD repack member after deleting overlay files; skip upstream-held paths
- [ARR mod overrides](arr-mod-overrides-route.md) — Ender's, CfB: new ids via a CD additions member under its namespace; CfB forbids modified copies (code, not kubejs/assets)
## Crashes and bugs found (cause only)
- [Crash triage 2026-09-29](server-crash-triage-0929.md), [2026-10-19](server-crash-triage-1019.md); [wind vane](supplementaries-breezy-wind-vane-crash.md); [D&D drill/saw](dnd-drill-saw-server-crash.md); [RecipeBookType kick](recipe-book-type-desync.md); [ghost blocks](ghost-blocks-null-state-packets.md); [axe tag ghosts](axe-tag-ghost-entries.md)
- [Client exit hang](client-exit-hang-nvidia-gldebug.md); [Quark sort loss](quark-sort-item-loss.md); [pick-block desync](creative-pick-block-desync.md); [perf fixes](perf-known-issues-fixes.md)
## Structures and worldgen (hub: structure-gen-framework)
- [Framework](structure-gen-framework.md), [groups](structure-groups.md), [spacing](structure-spacing-feasibility.md), [placement priority](placement-priority.md), [meta](meta-structures.md), [biome fits](biome-fits-towers.md), [jigsaw pools](jigsaw-pool-tuning.md), [ground match](structure-ground-match.md), [terrain adaptation](terrain-adaptation-soft-beard.md)
- [Earth fill](earth-fill-system.md), [water fill](water-fill-system.md), [water rules/IPO](water-biome-rules-ipo-driftwood.md), [worldgen config + /cdevplace](worldgen-config-cdevplace.md), [structure jars](mcreator-house-mods-datapack.md), [7.1](plan-7-1-execution.md), [7.4](plan-7-4-new-structure-mods.md)
- Locate: [dead-set hang](dead-structure-locate-hang.md); [map-trade hang](lc-map-trade-locate-hang.md); [locate writeup](locate-writeup.md)
- [Biome lookup ties](biome-lookup-ties.md) — RTree keeps the thread's last result on ties (deep_caves/deep_dark); predictions use TiedBiomes.any
- [First-tick water](first-tick-postprocess-water.md) — postProcessGeneration floods caves when a chunk first ticks; FirstTickWater = no drops in a settling window
- [Biome frequency](biome-frequency-tuning.md); [nether decisions](nether-worldgen-decisions.md); [season types](weather-by-biome.md) (solid blocks never see the sky: read seasons one block up below y 32); [time master](time-master.md)
- Plants/animals: [wild crops](vegetation-worldgen-system.md) (in Creative Delight since 2026-10-25), [wild flowers](wild-flowers-system.md), [IW soils](iw-soil-wild-plants.md), [bee nests](bee-nests-system.md), [Bushier Flowers off](bushier-flowers-disabled.md), [leaf decay](leaf-decay-range.md), [mob spawns](mob-spawns-system.md), [Moobloom](moobloom-variants.md), [wandering trader](wandering-trader.md)
- [MCA villager pathing](mca-villager-pathing.md) — own node maker (VillagerLandPathNodeMaker); WalkNodeEvaluator mixins miss MCA villagers
## Food, cans, cooking
- [Food tuning](food-tuning-system.md), [alcohol values](alcohol-drink-food-values.md), [Quark saturation word](quark-saturation-word.md), [placeable tooltips](placeable-food-tooltips.md), [AppleSkin coverage](appleskin-tooltip-coverage.md), [compost](compost-framework.md), [Display Delight port](display-delight-icons.md)
- Cans: [generated per-type](generated-per-type-cans.md), [multi-serving](multi-serving-cans.md), [parity](can-parity-worksheet.md), [golem dye](canned-golem-dye.md), [syringe](sample-syringe.md)
- [Oven + hammering](oven-recipes-hammering.md); [Rustic Delight haste](rustic-delight-haste-potions.md); [Incubation nests](incubation-nest-internals.md); [Incubation Additions](incubation-additions-mod.md); [crops](crops-harvest-writeup.md); [hoe area](hoe-harvest-area.md)
## Potions, machines, Create/IE
- [IE potion machines](ie-potion-machines.md); [Create config widget](create-config-string-widget.md); [sawblade routing](sawblade-recipe-routing.md); [AM potion fix](am-potion-brewing-fix.md); [potion buckets](potion-buckets.md)
- [CE components/coils](ce-components-coils.md); [BlockRunner tiers](blockrunner-speed-tiers.md); [camo walk](camo-walk-speed.md); [sinking blocks](sinking-blocks-investigation.md); [ItemPhysic fire/lava](itemphysic-fire-lava-split.md); [TrashSlot](trashslot-pack-defaults.md)
## Texture tooling
- [texturetools gotchas](texturetools-gotchas.md) — frozen tests/legacy, extended parsers keep stderr parity, batch kinds register on import, no kivy.core.window outside xvfb; owner doc texture_tools.md
## Gear, combat, effects
- [Effect refresh / HUD icon gotchas](effect-refresh-and-hud-icon-gotchas.md) — Health Boost refresh clamps health; 5-arg ctor hides HUD icon; [effects] remap/removed/forceHudIcon
- [Shields](shields.md); [damage types](damage-types.md); [armor/tool effect frameworks](armor-tool-effects.md); [dragon gear](dragon-gear-notes.md); [echo gear](echo-tier-gear.md); [Magic Resistance](magic-resistance-effect.md); [Air Brake](air-brake-item.md)
- [Effects writeup](effects-writeup.md); [JEED text](jeed-effect-descriptions.md); [bubble column](bubble-column-immunity.md); [camera bags](camera-bag-design.md); [campfire rain](campfire-rain.md); [Forgiving Void](forgiving-void-origin-pull.md)
## UI, JEI, WTHIT, data catalogs
- [Food tooltip rows](food-tooltip-rows.md) — AppleSkin/Quark row mechanics, Raspberry Pie's LOWEST reorder and one-row AppleSkin mixin, Mod Name Tooltip dedupe
- [crop growth line](wthit-crop-growth-line.md); [armor bar](armor-bar-compat.md); [GUI plaque](gui-title-plaque-stretch.md); [Legendary Tooltips](legendary-tooltips-separator-fix.md); [toasts](toast-filter.md); [banner patterns](banner-pattern-compat-fix.md)
- [JEI coverage](jei-coverage-catalog.md); [JEI craftable-first](jei-craftable-first.md); [names audit](missing-names-catalog.md); [stack sizes](stack-sizes-catalog.md) + [policy](stack-size-policy.md); [paintings](immersive-paintings-all-variants.md) (modded built at load by Compat; old ids aliased on load); [loot](loot-system.md)
## Ports and own mods (state: feature_writeup/README.md)
- [Power Grid port](power-grid-port.md) — P0-P10 ported 2026-11-13, gate jars only from jar_archive/pwg_gate/, never mods/; state in plan_power_grid.md + run doc 2026-11-13 sections 9-10
- [Rustic Delight port](rustic-delight-port.md) — CD member, MIT files baked by bake_upstream.py since 2026-11-10 (no runtime donor), all_of / tag / FeastBlock gotchas; - [Miner's Delight backport plan](miners-delight-backport-plan.md) — ARR license, donor-jar method, cup dropped, 1.20->1.19.2 data gotchas; [Backported Vinery](backported-vinery.md); [Backported Brewin](backported-brewin.md); [Exposure](exposure-backport.md); [baby mobs](baby-mobs-backport.md); [feature ports](feature-ports-2026-10-20.md); [Creative Labels](sticky-notes-rebuild.md)
## Releases (all in feature_writeup/plan_*.md; one pointer each)
- [CurseForge page format](../project_pages/page_format.md) — default layout of every project page (owner, 2026-11-25): bold underlined family headers, 3 to 8 subfamilies with one spoiler each
- [1.0.3-1.0.9](release-plans-1-0-3-to-1-0-6.md) (1.0.7 to 1.0.9 gotchas in its last section)
- [forge:raw_meat gotcha](forge-raw-meat-tag-gotcha.md) — pack tag has 2 Miner's items; use food_type/raw/meat
- [Forge remap uses STAGING registry](forge-missing-mappings-staging.md) — resolve survivors in RegistryManager.ACTIVE; test renames on a fresh old-id save
- [Upstream jar distribution](upstream-jar-distribution-policy.md) — donor jars go only to local dev; publish points to official sources, Common moves detected jars into the upstream folder; do not raise rehosting concerns; single upstream folder planned
