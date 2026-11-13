#!/usr/bin/env python3
"""
Stages a release of the pack in deploy/latest/ and records it in deploy/DEPLOY_LOG.md.

deploy/
  DEPLOY_LOG.md          one section per version, newest first: what changed and what to delete
  DEPLOYED.json          which version the server and the clients run (written by --snapshot only)
  history/<version>.json snapshot of every file of that version (sha1) + mod sides; diffs and verify read it
  latest/                the payload to copy to the other machines (rebuilt from scratch every run)
    README.md            steps for this payload's mode, this version's changes, the removals
    RUNBOOK.md           copy of deploy/RUNBOOK.md: server update, perf capture and data-dump commands
    VERSION.txt          "1.0.3 (update from 1.0.2)" / "1.0.3 (full install)"
    verify.sh verify.ps1 update mode: proves a machine is at the from-version before anything is copied;
                         --installed / -Installed (both modes): proves it is at this version afterwards
    verify/<target>.sha1   (hashes of the jars the from-version put in that target's mods/)
    verify/<target>.installed.sha1   (the same for this version)
    apply.sh apply.ps1   verify, removals, copy every folder below to its place for that target,
                         world_serverconfig/ into the chosen worlds (server: its level-name world), verify
                         --installed. Keeps MEMORY/JAVAPATH of LaunchServer.sh (maxmemory/javapath of the .bat)
    mods/                server jars (both + server) -> server mods/  AND  client mods/
    client_mods/         client-only jars                 -> client mods/ only
                         (the client always gets the full server mod set: it also runs single-player
                         worlds on its integrated server, so server-only mods must be there too)
    kubejs/ config/ defaultconfigs/                  -> both
    world_serverconfig/  every pack serverconfig file, in both modes -> <world>/serverconfig/ of each
                         existing world that needs it, by hand (defaultconfigs/ only reaches new worlds)
    server/              server-root files: LaunchServer.sh/.bat, perf/ (always-on JFR recording)
                                                     -> the server folder itself (server only)
    resourcepacks/ shaderpacks/                      -> client
    removals_server.txt  removals_client.txt         paths (from the target's root) to delete, if any

Two payload modes:
  update   (default once DEPLOYED.json exists) only files that are new or changed since the deployed
           version, in every folder: jars, scripts, datapack files, configs, packs. Removals are computed
           against the same version, and verify.sh/.ps1 check the machine really runs it.
  install  everything, for a new machine or one verify found drifted.
The deployed version ("deployment snapshot") is set by hand after a payload is copied out:
  python3 tools/build_deploy.py --snapshot [VERSION] [--target server|client] [--date ISO]
With both targets marked, an update's files start from the older of the two (one payload brings both up to
date), while each target's removals and verify list come from its own mark: a client one version ahead
still gets the deletions for files that only its version shipped. A deployed version can't be
rebuilt (its history json is what the machines are checked against): build the next version instead.

What goes where:
  mods/*.jar               side from mod_sides.md (same lookup as tools/dump_server_mods.py). Jars in
                           sub-folders (mods/panorama/) are never shipped, nor side `dev` jars (Creative
                           Devtools: dev mods/ only, feature_writeup/creative_devtools.md).
  kubejs/                  files .gitignore doesn't ignore, filtered by KUBEJS_CLASSES: runtime and client
                           files ship, dev files (notes, reports, generator inputs, /dump output in
                           exported/) never do. An unclassified file is a preflight error.
                           Player changelog (feature_writeup/changelog.md 3.5): the working list
                           assets/creativecommon/changelog/unreleased.md never ships under that name;
                           the payload gets it as <version>.md with "# <version>" and "Released <today>" on
                           top (the tree is not touched). --snapshot then writes that same text into the
                           tree as <version>.md and starts a fresh unreleased.md.
  config/, defaultconfigs/ every file .gitignore doesn't ignore, minus EXCLUDE (*.toml.bak backups, ...).
                           config/ is filtered by CONFIG_CLASSES: --config functional (default) leaves out
                           players' own settings (keybinds, HUD/UI, minimap, video, JEI), --config full ships
                           all of it. Player files are never removals.
  server/                  the tracked server/ folder, same rules; lands in the server's root folder
  configs_edited/server_config/*   merged into defaultconfigs/ so a new server world starts with the
                           hand-edited serverconfig values (configs_edited wins on a conflict)
  world_serverconfig/      the same merged defaultconfigs set, always complete (update mode too): Forge copies
                           defaultconfigs/ into a world only when its serverconfig file is missing, so an
                           existing world keeps old values until these are copied in. Not tracked in history
                           (no removals, no verify): the worlds are picked by hand.
  resourcepacks/, shaderpacks/     zips, folder packs (pack.mcmeta) and shader settings files

Removals are what the from-version had at a path on that target and this one doesn't: an updated jar's
old file, a mod removed or moved to client-only (server side), a deleted or now dev-only script.
Copying a folder over never deletes anything, so apply them. From the target's game folder:
  xargs -d '\\n' rm -f < removals_server.txt

Preflight (errors stop the build unless --force; warnings go into the log entry):
  - every jar in mods/ has a mod_sides.md row                                    (error)
  - every kubejs/ file matches a KUBEJS_CLASSES rule                              (error)
  - tools/changelog_lint.py passes on the changelog folder (its warnings are warnings)  (error)
  - no <version>.md in the tree for the version being built (one source per version)  (error)
  - every changelog file a deployed version shipped is unchanged (sha1 in its history json, or the
    approved restyle sha1 in deploy/changelog_restyles.json)                        (error)
  - unreleased.md has no bullets: this version ships without player notes          (warning)
  - structure_config.json regenerated from the installed jars + kubejs/data matches the file, and
    gen_structure_groups.py sync would change nothing (both run on a scratch copy)   (error)
  - the shipped patcherMod jar can read the gzipped climate samples              (error)
  - the version being built isn't already deployed; the from-version's history json is unchanged
    since its --snapshot                                                         (error)
  - in-tree mods (IN_TREE_MODS): jar in mods/ equals the gradle build, no source newer (warning)
  - pack/manifest.json lists the installed mods                                  (warning)
  - gen_display_delight_models.py --check: every Display Delight item has a model, and the
    /dumpitemsbymod export isn't older than the DD jar                           (warning)
  - uncommitted changes in shipped folders (recorded with the commit)            (warning)

The log's "Changes" are counted from the newest *other* version in history/ (the changelog); the
payload and its removals from the deployed version. Usually they're the same version.

Run: python3 tools/build_deploy.py [--version X.Y.Z] [--mode update|install] [--from X.Y.Z]
                                   [--notes TEXT] [--dry-run] [--full] [--force]
     (no --version: bumps the last version's patch number; the first release is 1.0.0)
"""
import argparse
import atexit
import contextlib
import datetime
import fnmatch
import hashlib
import io
import json
import os
import re
import shutil
import subprocess
import sys
import tempfile
import zipfile

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from dump_server_mods import MODS, ROOT, SERVER_SIDES, jar_modid, read_doc  # noqa: E402
from gen_pack_manifest import enabled_resource_packs, sha1, version_of  # noqa: E402
from changelog_lint import FOLDER as CHANGELOG_DIR, KUBEJS_PREFIX as CHANGELOG_PREFIX, UNRELEASED, count_bullets, \
    lint_paths, stamp  # noqa: E402

DEPLOY = os.path.join(ROOT, 'deploy')
OUT = os.path.join(DEPLOY, 'latest')
HISTORY = os.path.join(DEPLOY, 'history')
CHANGELOG_RESTYLES = os.path.join(DEPLOY, 'changelog_restyles.json')  # approved rewrites of shipped changelogs
LOG = os.path.join(DEPLOY, 'DEPLOY_LOG.md')
DEPLOYED = os.path.join(DEPLOY, 'DEPLOYED.json')
RUNBOOK = os.path.join(DEPLOY, 'RUNBOOK.md')  # operator one-pager (perf capture, dumps), copied into every payload
WORLDGEN = os.path.join(ROOT, 'kubejs', 'generation_scripts', 'worldgen')
SERVER_CONFIG_EDITS = os.path.join(ROOT, 'configs_edited', 'server_config')
# In-tree mods built by the root Gradle build: (Gradle project = folder under development/, jar file prefix,
# must be in mods/). Each project keeps its released jars in development/<project>/jar_archive/.
DEVELOPMENT = os.path.join(ROOT, 'development')
IN_TREE_MODS = [
    # Creative Common: patcherMod until the Creative Split swap (2026-11-04, creative_split.md 7.13); the shared core
    # every Creative jar requires. Its jar displaces the old creativeengineering-1.x jars (replaces_jars).
    ('creativeCommon', 'creativecommon', True),
    ('exposureBackport', 'exposureBackport', True),
    ('creativeLabels', 'creativeLabels', True),
    # Creative Delight: the bundle jar; its members (development/creativeDelight/members/, Baby Mobs and Incubation
    # Additions included since 2026-11-01; the Veggies, Vintage, Fungi, Miner's and Display ports since 2026-11-02,
    # creativedelight members #1 / #35) never ship on their own.
    ('creativeDelight', 'creativedelight', True),
    # Creative Brewing: the same for the Vinery and Brewin' ports (development/creativeBrewing/members/).
    ('creativeBrewing', 'creativebrewing', True),
    # Creative Calendar: time, seasons, crop seasons (feature_writeup/creative_calendar.md); CD and CB require it.
    ('creativeCalendar', 'creativecalendar', True),
    # Creative Compat: tweaks to third-party mods (feature_writeup/creative_compat.md); every other Creative jar requires it.
    ('creativeCompat', 'creativecompat', True),
    # Tempad 3.0.4 port (feature_writeup/immersive_portals.md 2.1.1): its own jar since 2026-11-07 (nested in Creative Compat
    # before, so Compat ships without a donor jar); planned to fold into Creative Computing. Reads tempad_upstream/.
    ('backportedTempad', 'backportedTempad', True),
    # Create: Power Grid 0.6.2 port (feature_writeup/plan_power_grid.md): not required, and not in mods/ during the port
    # (plan E1: scratch servers only); it ships once the basic/advanced energy switch exists (energy_advanced.md stage 5).
    ('backportedPowerGrid', 'backportedPowerGrid', False),
    # Creative Worldgen: world generation and fauna (feature_writeup/creative_worldgen.md); nests More Mooblooms and Baby
    # Mobs (development/creativeWorldgen/members/; More Mooblooms shipped on its own before 2026-11-02).
    ('creativeWorldgen', 'creativeworldgen', True),
    # Creative Magic: Dark Fire and the Nether portal rule, enchantment tweaks, the Botania Trinket Case, magic recipes
    # (feature_writeup/creative_magic.md).
    ('creativeMagic', 'creativemagic', True),
    # Creative Equipment: gear (shields, stepstools, backpack skins, air brake, tool and armor tuning, archery, End gear,
    # harvest tiers; feature_writeup/creative_equipment.md).
    ('creativeEquipment', 'creativeequipment', True),
    # Creative Engineering: machines and engineering (fan type, sawmill routing, shrapnel, surfaces, liquid XP, the
    # component, metal and wood scripts; feature_writeup/creative_engineering.md). Mod id creativeengineering since the
    # swap, version 2.x (1.x jars of that id were patcherMod).
    ('creativeEngineering', 'creativeengineering', True),
]
# Extra source trees per in-tree project for the "src newer than the jar" check (paths from the instance root).
IN_TREE_EXTRA_SRC = {
    'creativeCommon': ['kubejs/data/creativecommon/kubejs', 'kubejs/assets/creativecommon/kubejs'],
    'creativeDelight': ['development/creativeDelight/members', 'kubejs/data/creativedelight/kubejs',
                        'kubejs/assets/creativedelight/kubejs', 'kubejs/data/creativedelight/worldgen',
                        'kubejs/data/creativedelight/cup_conversion', 'kubejs/data/creativedelight/loot_tables',
                        'development/shared'],
    'creativeBrewing': ['development/creativeBrewing/members', 'kubejs/data/creativebrewing/kubejs',
                        'kubejs/assets/creativebrewing/kubejs'],
    'creativeCalendar': ['kubejs/data/creativecalendar/kubejs'],
    'creativeCompat': ['kubejs/data/creativecompat/kubejs', 'kubejs/assets/creativecompat/kubejs'],
    'creativeWorldgen': ['development/creativeWorldgen/members', 'kubejs/data/creativeworldgen/kubejs',
                         'kubejs/data/more_mooblooms/moobloom_spawns', 'development/shared'],
    'creativeMagic': ['kubejs/data/creativemagic/kubejs', 'kubejs/assets/creativemagic/kubejs', 'development/shared'],
    'creativeEquipment': ['kubejs/data/creativeequipment/kubejs', 'kubejs/assets/creativeequipment/kubejs',
                          'development/shared'],
    'creativeEngineering': ['kubejs/data/creativeengineering/kubejs', 'development/shared'],
}
# Third-party jars that patcherMod fixes target by version (deploy.md "Mod updates"): (jar file prefix, the file the
# fixes were written against). A different file under the same prefix means the mod was updated: rerun the checklist.
PINNED_MODS = [
    ('Create-DnDesire-', 'Create-DnDesire-1.19.2-1.2c.Beta Mid-Dev.jar'),
    ('supplementaries-', 'supplementaries-1.19.2-2.4.20.jar'),
    ('breezy-', 'breezy-1.19.2-1.0.8.jar'),
    ('create-1.19.2-', 'create-1.19.2-0.5.1.i.jar'),
    ('easyelytratakeoff-', 'easyelytratakeoff-1.19.2-4.2.jar'),
]

# payload folder -> (targets, folder it goes to on the target; '' = the target's root). Everything the server
# gets, the client gets too: clients also host single-player worlds, so they need the server-only mods and
# server configs. The one exception is 'server': launch scripts and the perf/ recording setup of the
# dedicated server itself, meaningless in a client instance.
CATEGORIES = {
    'mods': (('server', 'client'), 'mods'),
    'client_mods': (('client',), 'mods'),
    'kubejs': (('server', 'client'), 'kubejs'),
    'config': (('server', 'client'), 'config'),
    'defaultconfigs': (('server', 'client'), 'defaultconfigs'),
    'resourcepacks': (('client',), 'resourcepacks'),
    'shaderpacks': (('client',), 'shaderpacks'),
    # the unmodified Vinery jars the backport reads its art and data from (alcohol.md B4.8, D14: shipped in the
    # payload for now; a published pack downloads them from their source instead). Never in mods/.
    'vinery_upstream': (('server', 'client'), 'vinery_upstream'),
    # the unmodified Miner's Delight jar the merged backport reads its art and data from (miners_delight.md D12), as
    # for Vinery. Never in mods/: it has the port's mod id.
    'miners_upstream': (('server', 'client'), 'miners_upstream'),
    # the unmodified Roll Delight 1.3.1 jar the Creative Delight port reads its art from (roll_delight.md 4), as for
    # Miner's Delight. Never in mods/.
    'roll_upstream': (('server', 'client'), 'roll_upstream'),
    # the unmodified Large Meals 1.3.0 jar the Creative Delight port reads its art from (large_meals.md 4), as for
    # Roll Delight. Never in mods/.
    'largemeals_upstream': (('server', 'client'), 'largemeals_upstream'),
    # the unmodified Farmer's Structures 1.0.6 jar the patcherMod worldgen donor reads (farmers_structures.md 5).
    # Never in mods/: it is a NeoForge jar.
    'farmers_upstream': (('server', 'client'), 'farmers_upstream'),
    # the unmodified donor jars of the Creative Delight members Oceanic Delight (creative_delight.md 36), Ender's
    # additions in the End's member (ends_delight.md 5.9) and Golden Food (golden_food.md), as for Miner's Delight.
    # Never in mods/. (Oceanic and Ender's were missing here, so files[folder] raised KeyError; fixed 2026-11-02.)
    'oceanic_upstream': (('server', 'client'), 'oceanic_upstream'),
    'enders_upstream': (('server', 'client'), 'enders_upstream'),
    'goldenfood_upstream': (('server', 'client'), 'goldenfood_upstream'),
    # the unmodified Tempad 3.0.4 jar the Tempad port (a bundle member, now in Creative Compat) reads its art and data from (immersive_portals.md
    # 2.1.1). Never in mods/: it is a NeoForge jar with the port's mod id.
    'tempad_upstream': (('server', 'client'), 'tempad_upstream'),
    'server': (('server',), ''),
}
TREES = ('kubejs', 'config', 'defaultconfigs', 'server')
# Big, client-only and rarely changed: shipped only when new or changed (unless --full).
INCREMENTAL = ('resourcepacks', 'shaderpacks')
# Paths from the instance root, on top of what .gitignore already ignores.
EXCLUDE = [
    '*.bak',                   # Forge's backups of rewritten configs
    '*.backup[0-9]*',          # Xaero's
    '*/__pycache__/*',
    '*.pyc',
    'server/perf/reports/*',   # lag/crash bundles that came FROM the server (perf/report.sh); never sent back
]
# Every file under kubejs/ (path inside kubejs/, fnmatch, first match wins) is one of:
#   runtime  read by the game on server and client (scripts, datapack, inputs the tuners read)
#   client   read by the client only (the server ignores it; still shipped, it's one kubejs/ folder)
#   dev      never shipped: notes, reports, generator inputs and output, dumps
# A file no rule matches stops the build (preflight error), so a new runtime input can't silently stay
# home. kubejs/exported/ (the /dump* output, git-ignored) is dev-only as a whole: nothing in it is
# read at runtime since the climate samples moved to generation_scripts/worldgen/climate/ (1.0.3).
KUBEJS_CLASSES = [
    ('README.txt', 'dev'),
    ('archive/*', 'dev'),                  # old worldgen tuning, never loaded
    ('exported/*', 'dev'),                 # /dump* output
    ('startup_scripts/todo/*', 'dev'),     # .txt work lists
    ('assets/creativecommon/changelog/unreleased.md', 'dev'),  # working list: shipped as <version>.md (check_changelog)
    ('assets/creativecommon/changelog/*.md', 'client'),        # player changelog, one file per version
    ('*.md', 'dev'),                       # notes and reports; the tuners rewrite their *_report.md on every load
    # Creative Delight's script and data trees: authored here, shipped only inside the creativedelight jar (Q25)
    ('data/creativedelight/kubejs/*', 'dev'),
    ('data/creativedelight/worldgen/*', 'dev'),
    ('data/creativedelight/cup_conversion/*', 'dev'),
    ('data/creativedelight/loot_tables/*', 'dev'),   # Oceanic fishing net tables (creative_delight.md 36), in the CD jar
    ('assets/creativedelight/kubejs/*', 'dev'),
    # Creative Common's server and client helper scripts (plan_cb_publish.md M1): shipped only inside the creativecommon jar
    ('data/creativecommon/kubejs/*', 'dev'),
    ('assets/creativecommon/kubejs/*', 'dev'),
    # Creative Brewing's script trees: the same, shipped only inside the creativebrewing jar
    ('data/creativebrewing/kubejs/*', 'dev'),
    ('assets/creativebrewing/kubejs/*', 'dev'),
    # Creative Devtools' script trees: dev only, like its jar (feature_writeup/creative_devtools.md)
    ('data/creativedevtools/kubejs/*', 'dev'),
    ('assets/creativedevtools/kubejs/*', 'dev'),
    # Creative Calendar's server script tree: the same, shipped only inside the creativecalendar jar
    ('data/creativecalendar/kubejs/*', 'dev'),
    # Creative Compat's script trees: the same, shipped only inside the creativecompat jar
    ('data/creativecompat/kubejs/*', 'dev'),
    ('assets/creativecompat/kubejs/*', 'dev'),
    # Creative Worldgen's server script tree (with the loot framework's loot_config.json / loot_index.json) and the
    # moobloom spawn rules it ships as a data tree: the same, shipped only inside the creativeworldgen jar
    ('data/creativeworldgen/kubejs/*', 'dev'),
    ('data/more_mooblooms/*', 'dev'),
    # Creative Magic's script trees: the same, shipped only inside the creativemagic jar
    ('data/creativemagic/kubejs/*', 'dev'),
    ('assets/creativemagic/kubejs/*', 'dev'),
    # Creative Equipment's script trees: the same, shipped only inside the creativeequipment jar
    ('data/creativeequipment/kubejs/*', 'dev'),
    ('assets/creativeequipment/kubejs/*', 'dev'),
    # Creative Engineering's server script tree: the same, shipped only inside the creativeengineering jar
    ('data/creativeengineering/kubejs/*', 'dev'),
    ('startup_scripts/*', 'runtime'),
    ('server_scripts/*', 'runtime'),
    ('data/*', 'runtime'),
    ('config/*', 'runtime'),
    ('client_scripts/*', 'client'),
    ('assets/*', 'client'),
    # generation_scripts/: only what patcherMod or a script reads in-game; the rest is generator input/output
    ('generation_scripts/worldgen/climate/*.json.gz', 'runtime'),      # BiomeLayoutTuner (/dumpclimate samples)
    ('generation_scripts/worldgen/biome_baselines/*.json', 'runtime'),  # BiomeLayoutTuner
    ('generation_scripts/worldgen/biome_config.json', 'runtime'),       # BiomeLayoutTuner
    ('generation_scripts/worldgen/biome_layout_cache.json.gz', 'runtime'),  # BiomeLayoutTuner solve cache
    ('generation_scripts/worldgen/loot_container_swaps.json', 'runtime'),  # LootContainerSwaps
    ('generation_scripts/worldgen/pool_config.json', 'runtime'),        # PoolTuner
    ('generation_scripts/worldgen/pool_tags.json', 'runtime'),          # PoolTuner
    ('generation_scripts/worldgen/crop_seasons.json', 'runtime'),       # Calendar sereneseasons/cropseasontags.js
    ('generation_scripts/worldgen/structure_config.json', 'runtime'),   # worldgen/structure_overrides.js
    ('generation_scripts/worldgen/structure_groups.json', 'runtime'),   # worldgen/structure_overrides.js
    ('generation_scripts/worldgen/structure_tags.json', 'runtime'),     # structure_overrides.js, MassPlacer
    ('generation_scripts/worldgen/structure_world.json', 'dev'),        # Structure Catalogue rules; only Creative Devtools reads it
    ('generation_scripts/worldgen/moobloom_rules.json', 'dev'),         # gen_moobloom_rules.py input/output only; nothing reads it at runtime
    ('generation_scripts/worldgen/biome_traits.json', 'runtime'),       # CD wild crops, mushroom_spawns, wild_flowers, bee_nests, mob_spawns
    ('generation_scripts/worldgen/mob_spawns.json', 'runtime'),         # worldgen/mob_spawns.js
    ('generation_scripts/worldgen/spawn_boxes.json', 'runtime'),        # StructureSpawnBoxes
    ('generation_scripts/worldgen/mushroom_spawns.json', 'runtime'),    # worldgen mushroom spawns (Fungi Delight)
    ('generation_scripts/worldgen/bee_nests.json', 'runtime'),          # worldgen/bee_nests.js (BeeNests)
    ('generation_scripts/worldgen/biome_fits.json', 'runtime'),         # structure_overrides.js (StructureBiomeFits)
    ('generation_scripts/worldgen/meta_structures.json', 'runtime'),    # structure_overrides.js (MetaStructure)
    ('generation_scripts/worldgen/structure_test_overrides.json', 'runtime'),  # structure_overrides.js, only with [worldgen.debug] testOverrides
    ('generation_scripts/worldgen/season_types.json', 'runtime'),       # Calendar sereneseasons/seasontags.js (SeasonTypes)
    ('generation_scripts/worldgen/wild_flowers.json', 'runtime'),       # worldgen/wild_flowers.js
    ('generation_scripts/worldgen/flower_features.json', 'runtime'),    # worldgen/wild_flowers.js (snapshot)
    ('generation_scripts/worldgen/meat_block_swaps.json', 'runtime'),   # TemplateBlockSwaps (tools/meat_block_scan.py)
    ('generation_scripts/worldgen/nether_block_swaps.json', 'runtime'),  # TemplateBlockSwaps (tools/gen_nether_swaps.py)
    ('generation_scripts/wthit/silk_touch_blocks.json', 'runtime'),     # server_scripts/tags/wthittags.js (Needs Silk Touch tag)
    ('generation_scripts/physics/item_physics_families.json', 'dev'),   # tools/item_physics_audit.py output
    ('generation_scripts/worldgen/nether_swap_rules.json', 'dev'),      # tools/gen_nether_swaps.py input
    ('generation_scripts/worldgen/bee_nests_baseline.json', 'dev'),     # tools/gen_bee_nests.py calibration
    ('generation_scripts/worldgen/soil_compat_manifest.json', 'dev'),   # tools/gen_soil_compat.py
    ('generation_scripts/worldgen/nether_worldgen_rules.json', 'dev'),  # tools/gen_nether_worldgen.py input
    ('generation_scripts/worldgen/nether_worldgen_manifest.json', 'dev'),  # tools/gen_nether_worldgen.py
    ('generation_scripts/worldgen/terrain_adaptation_scan.json', 'dev'),  # tools/terrain_adaptation_scan.py
    # The next five are generator records since framework #7 (2026-11-01): the game reads the CD member scripts
    # (<mod>/food_types.js, placeable.js, mortar_catalog.js in data/creativedelight/kubejs, kubejs_startup/<mod>/
    # can_types.js and can_servings.js in the CD jar), which the generators write from them.
    ('generation_scripts/food/food_types.json', 'dev'),                 # tools/food_catalog.py record (hand edits)
    ('generation_scripts/food/can_servings.json', 'dev'),               # tools/gen_can_types.py record
    ('generation_scripts/food/can_types.json', 'dev'),                  # tools/gen_can_types.py record
    ('generation_scripts/food/placeable.json', 'dev'),                  # tools/placeable_catalog.py record
    ('generation_scripts/food/mortar_catalog.json', 'dev'),             # tools/gen_mortar_catalog.py record
    ('generation_scripts/food/placed_food_slices.json', 'runtime'),    # PlacedFoodSol (Spice of Life bites of placed cakes)
    ('generation_scripts/food/food_families.json', 'runtime'),         # CD seasoning_tags.js (food family item tags)
    ('generation_scripts/food/comfort_nourishment.json', 'runtime'),   # CD creativedelight/comfort_nourishment.js (tools/gen_comfort_nourishment.py)
    ('generation_scripts/food/food_tuning.json', 'runtime'),           # CD goldenfood/golden_values.js (golden and gilded multipliers)
    ('generation_scripts/food/food_dict_index.json', 'dev'),           # tools/food_dict.py lookup only
    ('generation_scripts/stacks/stack_sizes.json', 'runtime'),          # changes/stack_sizes.js
    ('generation_scripts/names/name_fixes.json', 'client'),             # client_scripts/name_fixes.js (tools/name_audit.py)
    ('generation_scripts/names/name_rules.json', 'dev'),                # tools/name_audit.py input
    ('generation_scripts/recipes/*', 'dev'),                            # sawmill.js output (copied into the jar)
    ('generation_scripts/sawmill.js', 'dev'),                           # node generator
]
# Every file under config/ (path inside config/, fnmatch, first match wins) is one of:
#   functional  pack behaviour: gameplay, worldgen, recipes, server rules, technical/mixin toggles. Always ships.
#   player      a player's own taste: keybinds, HUD/UI layout, tooltips, minimap, video, sounds, JEI layout and
#               bookmarks. Ships only with --config full, so an update never overwrites a player's choices.
# A third field ships a player rule in functional mode too, up to and including that version (a one-off push of
# a changed default). Unmatched files are functional. A player file never becomes a removal: whatever the
# player has stays in place even when the pack stops shipping it.
CONFIG_CLASSES = [
    ('trashslot-common.toml', 'player', '1.0.3'),   # TrashSlot deletion behaviour; pushed once with 1.0.3
    ('creativecommon/changelog_seen.txt', 'player'),  # changelog read marker (also git-ignored)
    ('DistantHorizons.toml', 'player', '1.1.04'),    # LOD distance/quality + DH threads and server request distances; pushed once with 1.0.4 and again with 1.1.04
    ('jei/blacklist.cfg', 'functional'),            # hidden ingredients (pack-defined)
    ('jei/recipe-category-sort-order.ini', 'functional'),   # pack's recipe tab order
    ('jei/ingredient-list-mod-sort-order.ini', 'functional'),  # pack's ingredient list order by mod
    ('jei/ingredient-list-type-sort-order.ini', 'functional'), # ... and by ingredient type
    ('jei/*', 'player'),                            # JEI client options, colours, per-world bookmarks
    ('*-client.*', 'player'),                       # every mod's client config (HUD, rendering, keys, sounds)
    ('*_client.*', 'player'),
    ('xaero/*/common.cfg', 'functional'),           # Xaero server rules (what maps/radar may show) ...
    ('xaero/*/server_profiles/*', 'functional'),    # ... and the profile the server enforces
    ('xaero/*/*_server.json', 'functional'),        # server radar categories
    ('xaero/*', 'player'),                          # the rest: minimap / world map client settings
    ('xaerohud.txt', 'player'),
    ('xaeropatreon.txt', 'player'),
    ('waila/*', 'player'),                          # WTHIT HUD toggles and blacklists
    ('pneumaticcraft/PneumaticArmorHUDLayout.cfg', 'player'),
    ('presencefootsteps/*', 'player'),              # footstep sounds
    ('spyglass-improvements/*', 'player'),
    ('betterf3.toml', 'player'),                    # F3 screen layout
    ('betterthirdperson-common.toml', 'player'),    # camera
    ('detailarmorbar.json', 'player'),              # armor bar look (the pack's colours are set in code)
    ('enchdesc.json', 'player'),                    # enchantment tooltips
    ('MouseTweaks.cfg', 'player'),
    ('notenoughanimations.json', 'player'),
    ('panorama.json', 'player'),                    # panorama screenshot tool
    ('rubidium-options.json', 'player'),            # video settings (rubidium-mixins.properties stays functional)
    ('sodium-extra-options.json', 'player'),        # video settings (sodium-extra.properties stays functional)
    ('sidebar_buttons.json', 'player'),             # FTB sidebar
    ('transition.json', 'player'),
    ('trender.json', 'player'),
]
CONFIG_MODES = ('functional', 'full')
LOG_HEADER = """# Deploy log

Every version staged by `tools/build_deploy.py`, newest first. Each entry lists what changed since the
version before it and which files the other machines must delete. The full file list of a version is in
`history/<version>.json`.
"""


class Preflight:
    def __init__(self):
        self.errors, self.warnings = [], []

    def error(self, msg):
        self.errors.append(msg)

    def warn(self, msg):
        self.warnings.append(msg)


# ── collecting the current pack ──────────────────────────────────────────────

def excluded(rel):
    return any(fnmatch.fnmatch(rel, pat) for pat in EXCLUDE)


def git(*args, stdin=None):
    try:
        r = subprocess.run(['git', '-C', ROOT, *args], input=stdin, capture_output=True, text=True)
    except FileNotFoundError:
        return None
    return r.stdout if r.returncode in (0, 1) else None


def walk(folder, root=ROOT):
    """Relative paths (from root) of every file under folder."""
    base = os.path.join(root, folder)
    out = []
    for d, dirs, files in os.walk(base):
        dirs.sort()
        for f in sorted(files):
            out.append(os.path.relpath(os.path.join(d, f), root).replace(os.sep, '/'))
    return out


def not_ignored(paths):
    ignored = git('check-ignore', '--stdin', stdin='\n'.join(paths))
    if ignored is None:
        return paths
    ignored = set(ignored.splitlines())
    return [p for p in paths if p not in ignored]


def kubejs_class(rel):
    """KUBEJS_CLASSES class of a path inside kubejs/, or None when no rule matches."""
    return next((cls for pat, cls in KUBEJS_CLASSES if fnmatch.fnmatch(rel, pat)), None)


def config_rule(rel):
    """(class, ship_until) of a path inside config/ (CONFIG_CLASSES; unmatched = functional)."""
    for rule in CONFIG_CLASSES:
        if fnmatch.fnmatch(rel, rule[0]):
            return rule[1], (rule[2] if len(rule) > 2 else None)
    return 'functional', None


def is_player_config(target_path):
    """A path on a target (config/... from the game folder) holding a player's own settings."""
    return target_path.startswith('config/') and config_rule(target_path[len('config/'):])[0] == 'player'


def config_ships(rel, mode, version):
    cls, until = config_rule(rel)
    return (cls == 'functional' or mode == 'full'
            or bool(until and version and parse_version(version) <= parse_version(until)))


def collect(pre, root=ROOT, unknown_side=None, config_mode='full', pack_version=None):
    """(files, mods): files = {category: {path inside the category: source path}},
    mods = {jar: {modId, side, name, version}}.

    root: the dev instance, or an installed game/server folder to record as a past version (then
    configs_edited isn't merged, and unknown_side is the side of jars mod_sides.md no longer lists).
    config_mode/pack_version: which config/ files ship (CONFIG_CLASSES); 'full' takes them all."""
    files = {c: {} for c in CATEGORIES}
    mods = {}
    by_modid, by_jar = read_doc()
    inst = json.load(open(os.path.join(ROOT, 'instance.json'), encoding='utf8'))
    by_file = {(m['type'], m['file']): m for m in inst['launcher']['mods']}
    unknown = []
    mods_dir = os.path.join(root, 'mods')
    for jar in sorted(os.listdir(mods_dir), key=str.lower):
        path = os.path.join(mods_dir, jar)
        if not jar.endswith('.jar') or not os.path.isfile(path):
            continue
        modid = jar_modid(path)
        side = (by_modid.get(modid) if modid else None) or by_jar.get(jar) or unknown_side
        if side is None:
            unknown.append(f'{jar} (mod id: {modid or "none"})')
            continue
        if side == 'dev':
            continue  # dev mods/ only (Creative Devtools, Q6): never in a payload or a recorded version
        entry = by_file.get(('mods', jar))
        # hand-added jars (patcherMod) get an entry named after the file, version "Unknown"
        name = entry['name'] if entry and not entry['name'].endswith('.jar') else modid or os.path.splitext(jar)[0]
        version = version_of(entry) if entry else ''
        mods[jar] = {'modId': modid, 'side': side, 'name': name, 'version': '' if version == 'Unknown' else version}
        files['mods' if side in SERVER_SIDES else 'client_mods'][jar] = path
    if unknown:
        pre.error('jars with no row in mod_sides.md (add a row, see its "Keeping this up to date"):\n'
                  + '\n'.join('    ' + u for u in unknown))
    # Mod changes are keyed by mod id, so a second jar for a shipped id would slip through the diff
    # (1.0.7 shipped two Brewin' jars this way). An installed folder is recorded as it is.
    if root == ROOT:
        by_id = {}
        for jar, meta in mods.items():
            if meta['modId']:
                by_id.setdefault(meta['modId'], []).append(jar)
        # Nested (Jar-in-Jar) mods count too: a stale upstream jar beside Creative Delight or Creative Brewing would silently win
        # if its version string sorts newer (creative_delight.md 14.1.4).
        for jar, path in files['mods'].items() | files['client_mods'].items():
            for nested_jar, nested_id in nested_modids(path):
                by_id.setdefault(nested_id, []).append(f'{jar}!{nested_jar}')
            if test_suite_entries(path):
                pre.error(f'mods/{jar} was built with -Pcd_test_suite (holds the scratch test suite): '
                          'rebuild it with ./gradlew :creativeDelight:build --offline before a release')
        # Third-party jars nesting the same library (xaerolib, spectrelib, ...) are resolved by FML's Jar-in-Jar
        # version selection; only a clash with a top-level jar or a Creative Delight / Creative Brewing member needs action.
        dupes = {m: jars for m, jars in by_id.items()
                 if len(jars) > 1 and any('!' not in j or j.startswith(('creativedelight', 'creativebrewing')) for j in jars)}
        if dupes:
            pre.error('mods/ holds more than one jar for a mod id (FML loads only one; remove the others):\n'
                      + '\n'.join(f'    {m}: {", ".join(jars)}' for m, jars in sorted(dupes.items())))

    unclassified = []
    for tree in TREES:
        for rel in not_ignored(walk(tree, root)):
            inner = rel[len(tree) + 1:]
            if excluded(rel):
                continue
            if tree == 'kubejs' and root == ROOT:
                cls = kubejs_class(inner)
                if cls is None:
                    unclassified.append(inner)
                if cls in (None, 'dev'):
                    continue
            if tree == 'config' and not config_ships(inner, config_mode, pack_version):
                continue
            files[tree][inner] = os.path.join(root, rel)
    if unclassified:
        pre.error('kubejs files no KUBEJS_CLASSES rule covers (add a runtime/client/dev rule in '
                  'tools/build_deploy.py):\n' + '\n'.join('    kubejs/' + u for u in unclassified))
    if root == ROOT and os.path.isdir(SERVER_CONFIG_EDITS):
        for rel in walk(os.path.relpath(SERVER_CONFIG_EDITS, ROOT)):
            name = rel.split('/', 2)[2]
            src = os.path.join(ROOT, rel)
            have = files['defaultconfigs'].get(name)
            if have and sha1(have) != sha1(src):
                pre.warn(f'defaultconfigs/{name} differs from configs_edited/server_config/{name}; shipped the configs_edited copy')
            files['defaultconfigs'][name] = src

    rp = os.path.join(root, 'resourcepacks')
    for name in sorted(os.listdir(rp)) if os.path.isdir(rp) else []:
        path = os.path.join(rp, name)
        if os.path.isfile(path) and name.lower().endswith('.zip'):
            files['resourcepacks'][name] = path
        elif os.path.isfile(os.path.join(path, 'pack.mcmeta')):
            for rel in walk(f'resourcepacks/{name}', root):
                files['resourcepacks'][rel[len('resourcepacks/'):]] = os.path.join(root, rel)
    if os.path.isdir(os.path.join(root, 'shaderpacks')):
        for rel in walk('shaderpacks', root):
            files['shaderpacks'][rel[len('shaderpacks/'):]] = os.path.join(root, rel)
    for folder, (port, label, _src) in UPSTREAM_DONORS.items():
        upstream = os.path.join(root, folder)
        for name in sorted(os.listdir(upstream)) if os.path.isdir(upstream) else []:
            if name.endswith('.jar') and os.path.isfile(os.path.join(upstream, name)):
                files[folder][name] = os.path.join(upstream, name)
        if root == ROOT and any(j.startswith(port) for j in mods):
            have = {n: sha1(p) for n, p in files[folder].items()}
            for name, want in upstream_jars(folder).items():
                if name not in have:
                    pre.error(f'{folder}/{name} missing: the {label} backport refuses to start without it')
                elif have[name] != want:
                    pre.error(f'{folder}/{name} has sha1 {have[name]}, the backport pins {want}')
    return files, mods


# Donor folders: {folder: (port jar prefix, name in messages, UpstreamJars.java that pins the jars)}.
UPSTREAM_DONORS = {
    # nested in the Creative Brewing jar, so the CB jar in mods/ is what needs the donor
    'vinery_upstream': ('creativebrewing', 'Vinery (Creative Brewing)',
                        ('creativeBrewing/members/vinery', 'net', 'satisfy', 'vinery', 'forge', 'upstream')),
    # nested in the Creative Delight jar, so the CD jar in mods/ is what needs the donor
    'miners_upstream': ('creativedelight', "Miner's Delight (Creative Delight)",
                        ('creativeDelight/members/miners', 'com', 'sammy', 'minersdelight', 'upstream')),
    # in the Creative Worldgen jar since the wave 5 carve (2026-11-02; patcherMod before)
    'farmers_upstream': ('creativeworldgen', "Farmer's Structures donor (Creative Worldgen)",
                         ('creativeWorldgen', 'com', 'SaintAlfonzo', 'creativeworldgen', 'worldgen', 'donor')),
    'roll_upstream': ('creativedelight', 'Roll Delight (Creative Delight)',
                      ('creativeDelight/members/roll', 'com', 'SaintAlfonzo', 'creativedelight', 'roll', 'upstream')),
    'largemeals_upstream': ('creativedelight', 'Large Meals (Creative Delight)',
                            ('creativeDelight/members/largemeals', 'com', 'SaintAlfonzo', 'creativedelight', 'largemeals', 'upstream')),
    'oceanic_upstream': ('creativedelight', 'Oceanic Delight (Creative Delight)',
                         ('creativeDelight/members/oceanic', 'com', 'SaintAlfonzo', 'creativedelight', 'oceanic', 'upstream')),
    'enders_upstream': ('creativedelight', "Ender's Delight Additions (Creative Delight)",
                        ('creativeDelight/members/ends', 'com', 'SaintAlfonzo', 'creativedelight', 'enders', 'upstream')),
    'goldenfood_upstream': ('creativedelight', 'Golden Food (Creative Delight)',
                            ('creativeDelight/members/golden', 'com', 'SaintAlfonzo', 'creativedelight', 'golden', 'upstream')),
    # its own jar since 2026-11-07 (nested in Creative Compat before)
    'tempad_upstream': ('backportedTempad', 'Tempad',
                        ('backportedTempad', 'earth', 'terrarium', 'tempad', 'forge')),
}


def upstream_jars(folder):
    """{jar: sha1} a backport pins (its UpstreamJars.java), so the build checks the same files the game will."""
    mod, *package = UPSTREAM_DONORS[folder][2]
    src = os.path.join(DEVELOPMENT, mod, 'src', 'main', 'java', *package, 'UpstreamJars.java')
    text = open(src, encoding='utf8').read()
    return dict(re.findall(r'new Jar\("([^"]+)", "([0-9a-f]{40})"', text))


# ── preflight ────────────────────────────────────────────────────────────────

def check_structures(pre):
    """Regenerate structure_config.json and sync structure_groups.json in a scratch copy of the
    worldgen folder; either one changing means the committed data is stale for the installed mods.
    Also warns when a shipped CEConfig still has the [worldgen.debug] test switches on."""
    for cfg in ('config/creativeworldgen-common.toml', 'configs_edited/root_config/creativeworldgen-common.toml'):
        path = os.path.join(ROOT, cfg)
        if os.path.exists(path):
            text = open(path, encoding='utf8').read()
            if re.search(r'^\s*testOverrides\s*=\s*true', text, re.M):
                pre.warn(f'{cfg}: [worldgen.debug] testOverrides = true (scratch-test structure rates); set it false before release')
            m = re.search(r'^\s*structureDensity\s*=\s*([0-9.eE+-]+)', text, re.M)
            if m and float(m.group(1)) != 1.0:
                pre.warn(f'{cfg}: [worldgen.debug] structureDensity = {m.group(1)}; set it to 1.0 before release')
    with tempfile.TemporaryDirectory() as tmp:
        wg = os.path.join(tmp, 'worldgen')
        shutil.copytree(WORLDGEN, wg)
        import gen_structure_config as gsc
        real = gsc.OUT_PATH
        gsc.OUT_PATH = type(real)(os.path.join(wg, 'structure_config.json'))
        log = io.StringIO()
        try:
            with contextlib.redirect_stdout(log):
                gsc.build()
        finally:
            gsc.OUT_PATH = real
        fresh = json.load(open(os.path.join(wg, 'structure_config.json'), encoding='utf8'))
        current = json.load(open(real, encoding='utf8'))
        if fresh.get('structure_sets') != current.get('structure_sets') or \
                fresh.get('meta_structures') != current.get('meta_structures'):
            new, old = set(fresh['structure_sets']), set(current.get('structure_sets', {}))
            detail = ''.join(f'\n    new set {s}' for s in sorted(new - old)) + \
                ''.join(f'\n    gone set {s}' for s in sorted(old - new))
            pre.error('structure_config.json is stale for the installed mods/kubejs data: run '
                      '`/cdevgen run worldgen` (or tools/gen_structure_config.py, then the rest of the '
                      'worldgen group)' + (detail or '\n    (same sets, changed members or biomes)'))
            return
        r = subprocess.run([sys.executable, os.path.join(ROOT, 'tools', 'gen_structure_groups.py'),
                            '--worldgen-dir', wg], capture_output=True, text=True, cwd=ROOT)
        if r.returncode != 0:
            pre.error('gen_structure_groups.py failed:\n    ' + (r.stderr or r.stdout).strip().replace('\n', '\n    '))
        elif 'already covers every structure' not in r.stdout:
            changes = [l for l in r.stdout.splitlines() if l.startswith('  ')]
            pre.error('structure_groups.json is out of sync (run `python3 tools/gen_structure_groups.py`, '
                      'then review the new rates):\n' + '\n'.join('  ' + c for c in changes))


def nested_modids(path):
    """[(nested jar path, mod id)] for the Jar-in-Jar mods listed in a jar's META-INF/jarjar/metadata.json."""
    out = []
    try:
        with zipfile.ZipFile(path) as z:
            if 'META-INF/jarjar/metadata.json' not in z.namelist():
                return out
            for entry in json.loads(z.read('META-INF/jarjar/metadata.json')).get('jars', []):
                with zipfile.ZipFile(io.BytesIO(z.read(entry['path']))) as nz:
                    if 'META-INF/mods.toml' not in nz.namelist():
                        continue
                    m = re.search(r'^\s*modId\s*=\s*"([^"]+)"', nz.read('META-INF/mods.toml').decode('utf8', 'replace'), re.M)
                    if m:
                        out.append((entry['path'].rsplit('/', 1)[-1], m.group(1)))
    except (zipfile.BadZipFile, KeyError, ValueError):
        pass
    return out


def test_suite_entries(path):
    """Entries of the Creative Delight scratch test suite (tools/creative_delight_test/suite, _cdtest/ folders)."""
    try:
        with zipfile.ZipFile(path) as z:
            return [n for n in z.namelist() if '/_cdtest/' in n]
    except zipfile.BadZipFile:
        return []


def in_tree_jar(prefix, jar):
    return re.match(rf'{re.escape(prefix)}-.*\.jar$', jar)


def check_in_tree_mods(pre):
    for project, prefix, required in IN_TREE_MODS:
        deployed = [j for j in os.listdir(MODS) if in_tree_jar(prefix, j)]
        if len(deployed) != 1:
            if deployed or required:
                pre.warn(f'expected one {prefix}-*.jar in mods/, found {deployed or "none"}')
            continue
        jar = os.path.join(MODS, deployed[0])
        built = os.path.join(DEVELOPMENT, project, 'build', 'libs', deployed[0])
        if os.path.isfile(built) and sha1(built) != sha1(jar):
            pre.warn(f'mods/{deployed[0]} differs from development/{project}/build/libs/{deployed[0]} '
                     '(built but not installed? ./gradlew :' + project + ':installToMods)')
        srcs = [os.path.join(DEVELOPMENT, project, 'src')] + [os.path.join(ROOT, p) for p in IN_TREE_EXTRA_SRC.get(project, [])]
        newest = max((os.path.getmtime(os.path.join(d, f)) for src in srcs for d, _, fs in os.walk(src) for f in fs
                      if '/build/' not in d + '/'), default=0)
        if newest > os.path.getmtime(jar):
            pre.warn(f'development/{project}/src has changes newer than mods/{deployed[0]} (rebuild + install the jar?)')


def check_pinned_mods(pre):
    """Version-pinned fixes (deploy.md "Mod updates"): warn when a targeted jar is no longer the pinned file."""
    jars = os.listdir(MODS)
    for prefix, pinned in PINNED_MODS:
        found = [j for j in jars if j.startswith(prefix) and j.endswith('.jar')]
        if found != [pinned]:
            pre.warn(f'version-pinned fixes target {pinned}, mods/ has {found or "none"}: rerun the '
                     'deploy.md "Mod updates" checklist for it, then update PINNED_MODS')


def check_climate_reader(pre, files):
    """The gzipped climate samples need patcherMod 1.0.3+ (WorldgenFiles.readGzipText); an older jar
    falls back to kubejs/exported/, which no longer ships, so biome tuning would be skipped."""
    if not any(p.startswith('generation_scripts/worldgen/climate/') for p in files['kubejs']):
        return
    for jar, src in files['mods'].items():
        if re.match(r'creativecommon-.*\.jar$', jar):
            with zipfile.ZipFile(src) as z:
                cls = z.read('com/SaintAlfonzo/creativecommon/util/WorldgenFiles.class')  # Creative Common since 2026-11-04
            if b'readGzipText' not in cls:
                pre.error(f'mods/{jar} predates the gzipped climate samples (no WorldgenFiles.readGzipText): '
                          'copy the current development/creativeCommon/build/libs jar into mods/ first')


def check_display_delight_models(pre):
    """DD item models the pack generates (wthit_lines.md F3): a DD update can add items with no model, which
    JEI draws as the purple cube (WTHIT falls back to the block model). The item list comes from
    /dumpitemsbymod, so an export older than the jar may miss the new items."""
    jars = [j for j in os.listdir(MODS) if re.match(r'(displaydelight|backportedDisplay)-.*\.jar$', j)]
    if not jars:
        # nested in the Creative Delight jar since 2026-11-02 (plan_creative_delight.md A2)
        jars = [j for j in os.listdir(MODS) if re.match(r'creativedelight-.*\.jar$', j)
                and any(n.startswith('backportedDisplay-') for n, _ in nested_modids(os.path.join(MODS, j)))]
    if not jars:
        return
    export = os.path.join(ROOT, 'kubejs', 'exported', 'items_by_mod', 'displaydelight.json')
    if not os.path.isfile(export) or os.path.getmtime(export) < os.path.getmtime(os.path.join(MODS, jars[0])):
        pre.warn(f'kubejs/exported/items_by_mod/displaydelight.json is older than mods/{jars[0]}: run '
                 '`/dumpitemsbymod displaydelight`, then tools/gen_display_delight_models.py')
    r = subprocess.run([sys.executable, os.path.join(ROOT, 'tools', 'gen_display_delight_models.py'), '--check'],
                       capture_output=True, text=True, cwd=ROOT)
    if r.returncode != 0:
        pre.warn('Display Delight item models are missing or stale: run tools/gen_display_delight_models.py\n    '
                 + (r.stdout or r.stderr).strip().replace('\n', '\n    '))


def check_manifest(pre, files):
    try:
        manifest = json.load(open(os.path.join(ROOT, 'pack', 'manifest.json'), encoding='utf8'))
    except (OSError, ValueError):
        pre.warn('pack/manifest.json missing: run tools/gen_pack_manifest.py')
        return
    listed = {m['file']: m['sha1'] for m in manifest.get('mods', [])}
    jars = {**files['mods'], **files['client_mods']}
    if set(listed) != set(jars) or any(sha1(p) != listed[j] for j, p in jars.items()):
        pre.warn('pack/manifest.json is out of date with mods/: run tools/gen_pack_manifest.py and commit pack/')


def changelog_path(version):
    return CHANGELOG_PREFIX + version + '.md'


def check_changelog(pre, files, version, deployed, stage_dir):
    """Lint the changelog folder, keep deployed versions' files frozen, and put the working list into the
    payload as <version>.md (stamped copy written into stage_dir; the tree is not touched)."""
    if not os.path.isdir(CHANGELOG_DIR):
        pre.warn('no changelog folder (kubejs/assets/creativecommon/changelog/): this version has no player notes')
        return
    for path, (errors, warnings) in lint_paths([CHANGELOG_DIR]).items():
        label = path if path.startswith('(') else os.path.relpath(path, ROOT)
        for e in errors:
            pre.error(f'changelog lint, {label}: {e}')
        for w in warnings:
            pre.warn(f'changelog lint, {label}: {w}')
    # Restyles the user approved for deployed changelogs: {rel: {"sha1": ..., "date": ..., "note": ...}}.
    # A shipped file may differ from its history sha1 only if it matches its entry here exactly.
    restyled = {}
    if os.path.isfile(CHANGELOG_RESTYLES):
        restyled = {rel: e['sha1'] for rel, e in json.load(open(CHANGELOG_RESTYLES, encoding='utf8')).items()}
    for v in sorted(deployed_versions(deployed), key=parse_version):
        hist = history_path(v)
        if not os.path.isfile(hist):
            continue
        shipped = json.load(open(hist, encoding='utf8'))['files'].get('kubejs', {})
        for rel, want in shipped.items():
            if not rel.startswith(CHANGELOG_PREFIX):
                continue
            path = os.path.join(ROOT, 'kubejs', rel)
            if not os.path.isfile(path):
                pre.error(f'kubejs/{rel} shipped with {v} and is gone: deployed changelogs are permanent (restore it from git)')
            elif sha1(path) != want and sha1(path) != restyled.get(rel):
                pre.error(f'kubejs/{rel} changed after it shipped with {v}: deployed changelogs are permanent '
                          '(restore it from git; put the correction in unreleased.md as its own bullet)')
    target = changelog_path(version)
    if os.path.isfile(os.path.join(ROOT, 'kubejs', target)):
        pre.error(f'kubejs/{target} already exists: {version} is staged from unreleased.md, not from a version file')
        return
    src = os.path.join(CHANGELOG_DIR, UNRELEASED + '.md')
    text = open(src, encoding='utf8').read() if os.path.isfile(src) else ''
    n = count_bullets(text)
    if not n:
        pre.warn(f'unreleased.md has no bullets: {version} ships without player notes')
        return
    out = os.path.join(stage_dir, version + '.md')
    with open(out, 'w', encoding='utf8') as f:
        f.write(stamp(text, version, datetime.date.today().isoformat()))
    files['kubejs'][target] = out
    print(f'changelog: kubejs/{target} from unreleased.md ({n} bullets)')


def freeze_changelog(version):
    """--snapshot: the changelog text that shipped with `version` becomes the tree's <version>.md, and
    unreleased.md starts over (title line only) when it still holds exactly what shipped."""
    hist = history_path(version)
    snap = json.load(open(hist, encoding='utf8'))
    rel = changelog_path(version)
    want = snap['files'].get('kubejs', {}).get(rel)
    if not want:
        return
    tree = os.path.join(ROOT, 'kubejs', rel)
    if os.path.isfile(tree) and sha1(tree) == want:
        return
    if os.path.isfile(tree):
        print(f'WARNING: kubejs/{rel} exists but differs from what shipped with {version}; left as it is')
        return
    unreleased = os.path.join(CHANGELOG_DIR, UNRELEASED + '.md')
    working = open(unreleased, encoding='utf8').read() if os.path.isfile(unreleased) else ''
    restamped = stamp(working, version, snap['date'][:10])
    staged = os.path.join(OUT, 'kubejs', rel)
    text = None
    if os.path.isfile(staged) and sha1(staged) == want:
        text = open(staged, encoding='utf8').read()
    elif hashlib.sha1(restamped.encode('utf8')).hexdigest() == want:
        text = restamped
    if text is None:
        print(f'WARNING: could not rebuild the {version} changelog that shipped (unreleased.md changed and '
              f'deploy/latest/ no longer holds it): copy kubejs/{rel} from the payload into the tree by hand, '
              'then trim unreleased.md')
        return
    with open(tree, 'w', encoding='utf8') as f:
        f.write(text)
    print(f'changelog: wrote kubejs/{rel} (the text that shipped)')
    if restamped == text:
        with open(unreleased, 'w', encoding='utf8') as f:
            f.write('# Unreleased\n')
        print('changelog: started a fresh unreleased.md')
    else:
        print(f'WARNING: unreleased.md changed after {version} was staged; it was left as it is: remove the '
              f'bullets that shipped with {version} by hand')


def git_state(pre):
    head = (git('rev-parse', '--short', 'HEAD') or '').strip() or None
    dirty = (git('status', '--porcelain', '--', 'kubejs', 'config', 'defaultconfigs',
                 'configs_edited/server_config', 'mod_sides.md') or '').splitlines()
    dirty = sorted({l[3:].strip('"') for l in dirty})
    if dirty:
        pre.warn(f'{len(dirty)} uncommitted change(s) in shipped folders (commit {head} + working tree)')
    return head, dirty


# ── diffing against the last deployed version ────────────────────────────────

def parse_version(v):
    return tuple(int(x) for x in re.findall(r'\d+', v)) or (0,)


def load_history():
    out = []
    if os.path.isdir(HISTORY):
        for f in os.listdir(HISTORY):
            if f.endswith('.json'):
                out.append(json.load(open(os.path.join(HISTORY, f), encoding='utf8')))
    return sorted(out, key=lambda s: (s['date'], parse_version(s['version'])))


def installed(snapshot, target):
    """Every path (from the target's game folder) this snapshot puts on that target."""
    out = set()
    for cat, paths in snapshot['files'].items():
        targets, dest = CATEGORIES[cat]
        if target in targets:
            out.update(f'{dest}/{p}' if dest else p for p in paths)
    return out


def mod_key(jar, meta):
    return meta['modId'] or 'jar:' + jar


def diff(prev, cur):
    d = {'mods': {'added': [], 'removed': [], 'updated': [], 'sides': []}, 'files': {}, 'removals': {}}
    old = {mod_key(j, m): (j, m) for j, m in (prev or {}).get('mods', {}).items()}
    new = {mod_key(j, m): (j, m) for j, m in cur['mods'].items()}
    old_sha = {**(prev or {}).get('files', {}).get('mods', {}), **(prev or {}).get('files', {}).get('client_mods', {})}
    new_sha = {**cur['files']['mods'], **cur['files']['client_mods']}
    by_name = lambda k: ((new.get(k) or old[k])[1]['name'].lstrip("'[(").lower(), k)
    for k in sorted(new.keys() - old.keys(), key=by_name):
        d['mods']['added'].append(new[k])
    for k in sorted(old.keys() - new.keys(), key=by_name):
        d['mods']['removed'].append(old[k])
    for k in sorted(new.keys() & old.keys(), key=by_name):
        (oj, om), (nj, nm) = old[k], new[k]
        if oj != nj or old_sha.get(oj) != new_sha.get(nj):
            d['mods']['updated'].append((oj, om, nj, nm))
        if om['side'] != nm['side']:
            d['mods']['sides'].append((nj, om['side'], nm['side'], nm))
    for cat in CATEGORIES:
        if cat in ('mods', 'client_mods'):
            continue
        a = (prev or {}).get('files', {}).get(cat, {})
        b = cur['files'][cat]
        gone = a.keys() - b.keys()
        kept = {p for p in gone if cat == 'config' and is_player_config('config/' + p)}
        d['files'][cat] = {
            'added': sorted(b.keys() - a.keys()),
            'changed': sorted(p for p in b.keys() & a.keys() if a[p] != b[p]),
            'removed': sorted(gone - kept),
            'no longer shipped (player settings, left in place)': sorted(kept),
        }
    for target in ('server', 'client'):
        d['removals'][target] = sorted(p for p in installed(prev, target) - installed(cur, target)
                                       if not is_player_config(p)) if prev else []
    return d


# ── deployment snapshots ─────────────────────────────────────────────────────
# deploy/DEPLOYED.json says which version each target runs. It's written only by --snapshot, which you
# run by hand after copying a payload out. Its version's history json is the "deployment snapshot":
# the full file list + hashes that update payloads are diffed against and verify scripts check.

def history_path(version):
    return os.path.join(HISTORY, f'{version}.json')


def load_deployed():
    if not os.path.isfile(DEPLOYED):
        return {}
    return json.load(open(DEPLOYED, encoding='utf8'))


def deployed_versions(deployed):
    return {e['version'] for e in deployed.get('log', [])}


def default_base(deployed):
    """The version an update starts from: the older of the server's and the client's marks, so one
    payload brings both up to date."""
    marks = [deployed[t]['version'] for t in ('server', 'client') if t in deployed]
    return min(marks, key=parse_version) if marks else None


def load_base(pre, deployed, version):
    """History snapshot of a deployed version, checked against the hash --snapshot recorded (a
    history json rewritten after its deployment no longer describes the machines)."""
    path = history_path(version)
    if not os.path.isfile(path):
        pre.error(f'--from {version}: no {os.path.relpath(path, ROOT)}')
        return None
    recorded = {deployed[t].get('snapshotSha1') for t in ('server', 'client')
                if deployed.get(t, {}).get('version') == version}
    if recorded and sha1(path) not in recorded:
        pre.error(f'{os.path.relpath(path, ROOT)} changed after {version} was deployed: an update built '
                  'from it would not match the machines (restore it from git, or use --mode install)')
    elif not recorded:
        pre.warn(f'--from {version}: no deployment snapshot of {version} in DEPLOYED.json; '
                 'run verify on each machine before applying')
    return json.load(open(path, encoding='utf8'))


def snapshot(version, targets, date, notes):
    """--snapshot: record that `version` (staged in deploy/latest/ by default) is now on `targets`."""
    if not version:
        vf = os.path.join(OUT, 'VERSION.txt')
        if not os.path.isfile(vf):
            sys.exit('--snapshot: no version given and no deploy/latest/VERSION.txt')
        version = open(vf, encoding='utf8').read().split()[0]
    path = history_path(version)
    if not os.path.isfile(path):
        sys.exit(f'--snapshot: {os.path.relpath(path, ROOT)} not found (stage {version} first)')
    date = date or datetime.datetime.now().isoformat(timespec='minutes')
    deployed = load_deployed()
    mark = {'version': version, 'date': date, 'snapshotSha1': sha1(path)}
    if notes:
        mark['notes'] = notes
    for t in targets:
        deployed[t] = mark
    deployed.setdefault('log', []).append({'version': version, 'date': date, 'targets': list(targets),
                                           **({'notes': notes} if notes else {})})
    out = {'_about': 'Which pack version each machine runs. Written by `tools/build_deploy.py --snapshot` '
                     'after a payload is copied out; update payloads are diffed against these versions\' '
                     'history/<version>.json.'}
    out.update({k: deployed[k] for k in ('server', 'client', 'log') if k in deployed})
    with open(DEPLOYED, 'w', encoding='utf8') as f:
        json.dump(out, f, indent=1, ensure_ascii=False)
        f.write('\n')
    mark_log(version, deployed)
    freeze_changelog(version)
    print(f"snapshot: {', '.join(targets)} at {version} ({date}); next update builds from "
          f"{default_base(deployed)}")


def mark_log(version, deployed):
    """Put a "- Deployed:" line in the version's DEPLOY_LOG section."""
    if not os.path.isfile(LOG):
        return
    when = [f"{', '.join(e['targets'])} {e['date'].replace('T', ' ')}"
            for e in deployed.get('log', []) if e['version'] == version]
    line = '- Deployed: ' + '; '.join(when)
    text = open(LOG, encoding='utf8').read()
    m = re.search(rf'(?ms)^<!-- deploy:{re.escape(version)} -->.*?(?=^<!-- deploy:|\Z)', text)
    if not m:
        return
    sec = m.group(0)
    if re.search(r'(?m)^- Deployed: .*$', sec):
        sec = re.sub(r'(?m)^- Deployed: .*$', lambda _: line, sec, count=1)
    else:
        sec = re.sub(r'(?m)^(- Since: .*)$', lambda mm: mm.group(1) + '\n' + line, sec, count=1)
    with open(LOG, 'w', encoding='utf8') as f:
        f.write(text[:m.start()] + sec + text[m.end():])


# ── payload ──────────────────────────────────────────────────────────────────

def select_payload(files, cur, base, mode, full):
    """{category: {path: source}} to copy. install: everything. update: only files that are new or
    changed since `base` (jars, scripts, data, configs, packs alike); deletions go in the removals."""
    out = {}
    for cat, paths in files.items():
        old = (base or {}).get('files', {}).get(cat, {})
        keep_all = mode == 'install' or (full and cat in INCREMENTAL)
        out[cat] = {rel: src for rel, src in paths.items() if keep_all or old.get(rel) != cur['files'][cat][rel]}
    return out


def payload_stats(payload):
    n = sum(len(v) for v in payload.values())
    size = sum(os.path.getsize(src) for v in payload.values() for src in v.values())
    by_cat = ', '.join(f'{cat} {len(v)}' for cat, v in payload.items() if v)
    return n, size, by_cat


def mb(n):
    return f'{n / 1e6:.1f} MB' if n >= 1e5 else f'{n / 1e3:.0f} KB'


# The four scripts below are filled by fill_script(): @name@ tokens, so shell and PowerShell braces stay as written.
VERIFY_SH = r"""#!/bin/sh
# Checks a game folder's mods/ against a pack version for that target: every jar must be the one that version
# shipped, nothing missing, nothing extra. Needs only sh + sha1sum (or shasum).
#   sh verify.sh server /path/to/server               before applying: at the version this update expects (@base@)?
#   sh verify.sh client /path/to/instance --installed after applying: at @version@?
set -u
here=$(cd "$(dirname "$0")" && pwd)
target=${1:-}
case "$target" in server|client) ;; *) echo "usage: sh verify.sh server|client [game folder] [--installed]"; exit 2 ;; esac
shift
dir=. stem=$target
for a in "$@"; do case "$a" in --installed) stem=$target.installed ;; *) dir=$a ;; esac; done
list="$here/verify/$stem.sha1"
if [ ! -f "$list" ]; then
  echo "Full install payload: nothing to check before applying (--installed checks the result)."; exit 0
fi
command -v sha1sum >/dev/null 2>&1 || sha1sum() { shasum -a 1 "$@"; }
base=$(cat "$here/verify/$stem.version")
cd "$dir" || exit 2
[ -d mods ] || { echo "no mods/ folder in $(pwd): pass the $target's game folder"; exit 2; }
bad=0
while read -r sum path; do
  if [ ! -f "$path" ]; then echo "MISSING  $path"; bad=$((bad + 1))
  elif [ "$(sha1sum "$path" | cut -d' ' -f1)" != "$sum" ]; then echo "CHANGED  $path"; bad=$((bad + 1)); fi
done < "$list"
for f in mods/*.jar; do
  [ -e "$f" ] || continue
  cut -d' ' -f3- "$list" | grep -qxF "$f" || { echo "EXTRA    $f"; bad=$((bad + 1)); }
done
if [ "$bad" -eq 0 ] && [ "$stem" = "$target" ]; then
  echo "OK: $(pwd) matches $base ($(wc -l < "$list") jars). Apply the update."
elif [ "$bad" -eq 0 ]; then
  echo "OK: $(pwd) is at $base ($(wc -l < "$list") jars)."
elif [ "$stem" = "$target" ]; then
  echo "DRIFT: $bad difference(s) from $base. Don't apply this update: use an install payload"
  echo "(python3 tools/build_deploy.py --mode install), or fix the listed jars by hand."
  exit 1
else
  echo "DRIFT: $bad difference(s) from $base: the copy is incomplete or a jar it should have deleted is"
  echo "still there. Fix the listed jars by hand."
  exit 1
fi
"""

VERIFY_PS1 = r"""# Checks a game folder's mods\ against a pack version for that target: every jar must be the one that version
# shipped, nothing missing, nothing extra.
#   powershell -ExecutionPolicy Bypass -File verify.ps1 client "C:\path\to\instance"             before applying (@base@)
#   powershell -ExecutionPolicy Bypass -File verify.ps1 client "C:\path\to\instance" -Installed  after applying (@version@)
param(
  [Parameter(Mandatory = $true)][ValidateSet('server', 'client')][string]$Target,
  [string]$Dir = '.',
  [switch]$Installed
)
$stem = if ($Installed) { "$Target.installed" } else { $Target }
$list = Join-Path $PSScriptRoot "verify\$stem.sha1"
if (-not (Test-Path -LiteralPath $list)) { 'Full install payload: nothing to check before applying (-Installed checks the result).'; exit 0 }
$base = (Get-Content -LiteralPath (Join-Path $PSScriptRoot "verify\$stem.version") -TotalCount 1).Trim()
$expected = @{}
foreach ($line in Get-Content -LiteralPath $list) {
  $sum, $path = $line -split '  ', 2
  $expected[$path] = $sum
}
if (-not (Test-Path -LiteralPath (Join-Path $Dir 'mods'))) { "no mods\ folder in ${Dir}: pass the $Target's game folder"; exit 2 }
$bad = 0
foreach ($path in $expected.Keys) {
  $f = Join-Path $Dir $path
  if (-not (Test-Path -LiteralPath $f)) { "MISSING  $path"; $bad++ }
  elseif ((Get-FileHash -LiteralPath $f -Algorithm SHA1).Hash.ToLower() -ne $expected[$path]) { "CHANGED  $path"; $bad++ }
}
Get-ChildItem -LiteralPath (Join-Path $Dir 'mods') -Filter *.jar -File | ForEach-Object {
  $p = 'mods/' + $_.Name
  if (-not $expected.ContainsKey($p)) { "EXTRA    $p"; $bad++ }
}
if ($bad -eq 0 -and -not $Installed) { "OK: $Dir matches $base ($($expected.Count) jars). Apply the update." }
elseif ($bad -eq 0) { "OK: $Dir is at $base ($($expected.Count) jars)." }
elseif (-not $Installed) {
  "DRIFT: $bad difference(s) from $base. Don't apply this update: use an install payload"
  "(python3 tools/build_deploy.py --mode install), or fix the listed jars by hand."
  exit 1
}
else {
  "DRIFT: $bad difference(s) from $base: the copy is incomplete or a jar it should have deleted is"
  'still there. Fix the listed jars by hand.'
  exit 1
}
"""

APPLY_SH = r"""#!/bin/sh
# Applies this payload (@version@, @label@) to a server folder or a client instance. Stop the server or close
# the game first. Steps: verify.sh (update payload; stops on DRIFT), delete removals_<target>.txt, copy each
# payload folder to its place on that target, world_serverconfig/ into the chosen worlds, verify.sh --installed.
#   sh apply.sh server /path/to/server                     also refreshes the server world's serverconfig/
#   sh apply.sh client /path/to/instance [--world saves/<world>]...
#   --world <folder>  a world (from the game folder, or absolute) that gets world_serverconfig/; repeat for more.
#                     The server defaults to its level-name world. Replaces those files' per-world edits.
#   --no-world        no world gets world_serverconfig/
#   --force           apply even when verify reports drift
# LaunchServer.sh keeps its MEMORY and JAVAPATH lines.
set -u
here=$(cd "$(dirname "$0")" && pwd)
mode=@mode@
version=@version@
target=${1:-}
case "$target" in server|client) ;; *) echo "usage: sh apply.sh server|client [game folder] [--world <folder>]... [--no-world] [--force]"; exit 2 ;; esac
shift
dir=. force=0 noworld=0 worlds=
while [ $# -gt 0 ]; do
  case "$1" in
    --force) force=1 ;;
    --no-world) noworld=1 ;;
    --world) [ $# -ge 2 ] || { echo "--world needs a folder"; exit 2; }; worlds="$worlds
$2"; shift ;;
    *) dir=$1 ;;
  esac
  shift
done
[ -d "$dir" ] || { echo "no folder $dir"; exit 2; }
dir=$(cd "$dir" && pwd)
echo "applying $version ($mode) to the $target in $dir"

if [ "$mode" = update ]; then
  if ! sh "$here/verify.sh" "$target" "$dir"; then
    [ "$force" = 1 ] || { echo "Nothing changed. Fix the drift, or pass --force to apply anyway."; exit 1; }
    echo "--force: applying anyway"
  fi
elif ls "$dir"/mods/*.jar >/dev/null 2>&1; then
  old="$dir/mods_before_$version"
  mkdir -p "$old" && mv "$dir"/mods/*.jar "$old/" || exit 1
  echo "full install: moved the old jars to $old"
fi

keep=
[ "$target" = server ] && [ -f "$dir/LaunchServer.sh" ] && keep=$(grep -E '^(MEMORY|JAVAPATH)=' "$dir/LaunchServer.sh")

n=0
if [ -f "$here/removals_$target.txt" ]; then
  while IFS= read -r p; do
    [ -n "$p" ] || continue
    case "/$p/" in //*|*/../*) echo "skipped unsafe path: $p"; continue ;; esac
    [ -e "$dir/$p" ] && rm -f "$dir/$p" && n=$((n + 1))
  done < "$here/removals_$target.txt"
fi
echo "deleted $n path(s) of removals_$target.txt"

copy() {  # payload folder, folder on the target ('' = its root)
  [ -d "$here/$1" ] || return 0
  mkdir -p "$dir/$2" && cp -R "$here/$1/." "$dir/$2/" || exit 1
  echo "copied $1/ -> ${2:-.}/ ($(find "$here/$1" -type f | wc -l | tr -d ' ') files)"
}
case "$target" in
@copies@
esac

if [ "$target" = server ] && [ -f "$dir/LaunchServer.sh" ]; then
  if [ -n "$keep" ]; then
    tmp=$(mktemp)
    printf '%s\n' "$keep" | awk 'NR == FNR { k = $0; sub(/=.*/, "", k); v[k] = $0; next }
      /^(MEMORY|JAVAPATH)=/ { k = $0; sub(/=.*/, "", k); if (k in v) { print v[k]; delete v[k]; next } }
      { print }' - "$dir/LaunchServer.sh" > "$tmp" && cat "$tmp" > "$dir/LaunchServer.sh"
    rm -f "$tmp"
    echo "LaunchServer.sh: kept $(printf '%s' "$keep" | tr '\n' ' ')"
  fi
  chmod +x "$dir/LaunchServer.sh" "$dir"/perf/*.sh 2>/dev/null
fi

if [ "$target" = server ] && [ "$noworld" = 0 ] && [ -z "$worlds" ]; then
  name=$(sed -n 's/^level-name=//p' "$dir/server.properties" 2>/dev/null | head -n 1 | tr -d '\r')
  worlds=${name:-world}
fi
[ "$noworld" = 1 ] && worlds=
if [ -d "$here/world_serverconfig" ]; then
  printf '%s\n' "$worlds" | while IFS= read -r w; do
    [ -n "$w" ] || continue
    case "$w" in /*) wd=$w ;; *) wd=$dir/$w ;; esac
    if [ ! -f "$wd/level.dat" ]; then echo "no world in $wd (no level.dat): skipped, defaultconfigs/ covers a new one"; continue; fi
    mkdir -p "$wd/serverconfig" && cp "$here"/world_serverconfig/* "$wd/serverconfig/" &&
      echo "copied world_serverconfig/ -> $wd/serverconfig/"
  done
  [ "$target" = client ] && [ -z "$worlds" ] &&
    echo "no --world given: existing single-player saves keep their serverconfig/ (README, client step 5)"
fi

echo
sh "$here/verify.sh" "$target" "$dir" --installed || exit 1
echo "Done. On the dev machine: python3 tools/build_deploy.py --snapshot $version (--target $target if the other side isn't updated yet)"
"""

APPLY_PS1 = r"""# Applies this payload (@version@, @label@) to a server folder or a client instance. Stop the server or close
# the game first. Steps: verify.ps1 (update payload; stops on DRIFT), delete removals_<target>.txt, copy each
# payload folder to its place on that target, world_serverconfig\ into the chosen worlds, verify.ps1 -Installed.
#   powershell -ExecutionPolicy Bypass -File apply.ps1 client "C:\path\to\instance" [-World saves\<world>,...]
#   powershell -ExecutionPolicy Bypass -File apply.ps1 server "C:\path\to\server"
#   -World    worlds (from the game folder, or absolute) that get world_serverconfig\. The server defaults to its
#             level-name world. Replaces those files' per-world edits.
#   -NoWorld  no world gets world_serverconfig\
#   -Force    apply even when verify reports drift
# LaunchServer.bat keeps its maxmemory and javapath lines.
param(
  [Parameter(Mandatory = $true, Position = 0)][ValidateSet('server', 'client')][string]$Target,
  [Parameter(Position = 1)][string]$Dir = '.',
  [string[]]$World = @(),
  [switch]$NoWorld,
  [switch]$Force
)
$ErrorActionPreference = 'Stop'
$mode = '@mode@'
$version = '@version@'
if (-not (Test-Path -LiteralPath $Dir -PathType Container)) { "no folder $Dir"; exit 2 }
$Dir = (Resolve-Path -LiteralPath $Dir).Path
"applying $version ($mode) to the $Target in $Dir"

if ($mode -eq 'update') {
  & (Join-Path $PSScriptRoot 'verify.ps1') $Target $Dir
  if ($LASTEXITCODE -ne 0) {
    if (-not $Force) { 'Nothing changed. Fix the drift, or pass -Force to apply anyway.'; exit 1 }
    '-Force: applying anyway'
  }
}
elseif (Test-Path -LiteralPath (Join-Path $Dir 'mods')) {
  $jars = @(Get-ChildItem -LiteralPath (Join-Path $Dir 'mods') -Filter *.jar -File)
  if ($jars.Count) {
    $old = Join-Path $Dir "mods_before_$version"
    [IO.Directory]::CreateDirectory($old) | Out-Null
    foreach ($j in $jars) { [IO.File]::Move($j.FullName, (Join-Path $old $j.Name)) }
    "full install: moved the old jars to $old"
  }
}

$bat = Join-Path $Dir 'LaunchServer.bat'
$keep = @()
if ($Target -eq 'server' -and (Test-Path -LiteralPath $bat)) {
  $keep = @(Get-Content -LiteralPath $bat | Where-Object { $_ -match '^set (maxmemory|javapath)=' })
}

$n = 0
$list = Join-Path $PSScriptRoot "removals_$Target.txt"
if (Test-Path -LiteralPath $list) {
  foreach ($p in Get-Content -LiteralPath $list) {
    if (-not $p) { continue }
    if ([IO.Path]::IsPathRooted($p) -or ("/$p/" -like '*/../*')) { "skipped unsafe path: $p"; continue }
    $f = Join-Path $Dir $p
    if (Test-Path -LiteralPath $f -PathType Leaf) { Remove-Item -LiteralPath $f -Force; $n++ }
  }
}
"deleted $n path(s) of removals_$Target.txt"

function Copy-Tree($from, $to) {  # payload folder, folder on the target ('' = its root)
  $src = Join-Path $PSScriptRoot $from
  if (-not (Test-Path -LiteralPath $src -PathType Container)) { return }
  $src = (Resolve-Path -LiteralPath $src).Path
  $dst = if ($to) { Join-Path $Dir $to } else { $Dir }
  $files = @(Get-ChildItem -LiteralPath $src -Recurse -File)
  foreach ($f in $files) {
    $out = Join-Path $dst $f.FullName.Substring($src.Length + 1)
    [IO.Directory]::CreateDirectory([IO.Path]::GetDirectoryName($out)) | Out-Null
    [IO.File]::Copy($f.FullName, $out, $true)
  }
  "copied $from\ -> $(if ($to) { $to } else { '.' })\ ($($files.Count) files)"
}
@copies@

if ($keep.Count -and (Test-Path -LiteralPath $bat)) {
  $vals = @{}
  foreach ($l in $keep) { $vals[($l -split '=', 2)[0]] = $l }
  $lines = foreach ($l in Get-Content -LiteralPath $bat) {
    $k = ($l -split '=', 2)[0]
    if ($l -match '^set (maxmemory|javapath)=' -and $vals.ContainsKey($k)) { $vals[$k]; $vals.Remove($k) } else { $l }
  }
  Set-Content -LiteralPath $bat -Value $lines -Encoding ASCII
  "LaunchServer.bat: kept $($keep -join ' ')"
}

$worlds = @($World)
if ($Target -eq 'server' -and -not $NoWorld -and -not $worlds.Count) {
  $name = 'world'
  $props = Join-Path $Dir 'server.properties'
  if (Test-Path -LiteralPath $props) {
    $m = Get-Content -LiteralPath $props | Where-Object { $_ -match '^level-name=' } | Select-Object -First 1
    if ($m) { $name = ($m -split '=', 2)[1].Trim() }
  }
  $worlds = @($name)
}
if ($NoWorld) { $worlds = @() }
$wsc = Join-Path $PSScriptRoot 'world_serverconfig'
if (Test-Path -LiteralPath $wsc -PathType Container) {
  foreach ($w in $worlds) {
    $wd = if ([IO.Path]::IsPathRooted($w)) { $w } else { Join-Path $Dir $w }
    if (-not (Test-Path -LiteralPath (Join-Path $wd 'level.dat'))) { "no world in $wd (no level.dat): skipped, defaultconfigs\ covers a new one"; continue }
    $sc = Join-Path $wd 'serverconfig'
    [IO.Directory]::CreateDirectory($sc) | Out-Null
    foreach ($f in Get-ChildItem -LiteralPath $wsc -File) { [IO.File]::Copy($f.FullName, (Join-Path $sc $f.Name), $true) }
    "copied world_serverconfig\ -> $sc\"
  }
  if ($Target -eq 'client' -and -not $worlds.Count) {
    'no -World given: existing single-player saves keep their serverconfig\ (README, client step 5)'
  }
}

''
& (Join-Path $PSScriptRoot 'verify.ps1') $Target $Dir -Installed
if ($LASTEXITCODE -ne 0) { exit 1 }
"Done. On the dev machine: python3 tools/build_deploy.py --snapshot $version (--target $Target if the other side isn't updated yet)"
"""


def fill_script(body, **values):
    for k, v in values.items():
        body = body.replace(f'@{k}@', v)
    return body


def target_copies():
    """{target: [(payload folder, folder on the target)]} from CATEGORIES, in their order."""
    return {t: [(cat, dest) for cat, (targets, dest) in CATEGORIES.items() if t in targets] for t in ('server', 'client')}


def write_scripts(out, bases, cur, payload):
    """verify.sh / verify.ps1 + verify/<target>.sha1 and .version (the jars each target's base version put in
    its mods/, minus jars this payload copies over anyway; bases: {target: history snapshot}, empty for an
    install payload) and verify/<target>.installed.* (the same for this version), and apply.sh / apply.ps1."""
    os.makedirs(os.path.join(out, 'verify'), exist_ok=True)
    for target in ('server', 'client'):
        shipped = {rel for cat, paths in payload.items() if cat in CATEGORIES
                   and CATEGORIES[cat][1] == 'mods' and target in CATEGORIES[cat][0] for rel in paths}
        for stem, snap in ((target, bases.get(target)), (f'{target}.installed', cur)):
            if not snap:
                continue
            skip = shipped if snap is not cur else set()     # a missing/disabled jar the update restores isn't drift
            lines = sorted(f'{h}  mods/{rel}' for cat, paths in snap['files'].items()
                           for rel, h in paths.items()
                           if CATEGORIES[cat][1] == 'mods' and target in CATEGORIES[cat][0] and rel not in skip)
            with open(os.path.join(out, 'verify', f'{stem}.sha1'), 'w', encoding='utf8', newline='\n') as f:
                f.write('\n'.join(lines) + '\n')
            with open(os.path.join(out, 'verify', f'{stem}.version'), 'w', encoding='utf8', newline='\n') as f:
                f.write(snap['version'] + '\n')
    mode = cur['payload']['mode']
    values = {
        'base': ', '.join(f"{t} {b['version']}" for t, b in bases.items()) or 'none: full install',
        'version': cur['version'],
        'mode': mode,
        'label': f"update from {cur['payload']['from']}" if mode == 'update' else 'full install',
    }
    copies = target_copies()
    sh_copies = '\n'.join(f'  {t})\n' + ''.join(f"    copy {c} '{d}'\n" for c, d in copies[t]) + '    ;;'
                          for t in copies)
    ps_copies = '\n'.join(("if ($Target -eq '%s') {\n" % t) + ''.join(f"  Copy-Tree '{c}' '{d}'\n" for c, d in copies[t]) + '}'
                          for t in copies)
    for name, body, extra in (('verify.sh', VERIFY_SH, {}), ('verify.ps1', VERIFY_PS1, {}),
                              ('apply.sh', APPLY_SH, {'copies': sh_copies}), ('apply.ps1', APPLY_PS1, {'copies': ps_copies})):
        with open(os.path.join(out, name), 'w', encoding='utf8', newline='\n') as f:
            f.write(fill_script(body, **values, **extra))
    for name in ('verify.sh', 'apply.sh'):
        os.chmod(os.path.join(out, name), 0o755)


# ── rendering ────────────────────────────────────────────────────────────────

def details(title, lines, open_limit=15):
    if not lines:
        return []
    if len(lines) <= open_limit:
        return [f'{title}:', ''] + lines + ['']
    return [f'<details><summary>{title}</summary>', ''] + lines + ['', '</details>', '']


def collapse(paths, limit=8):
    """File list, with folders holding more than `limit` of the paths shown as one line."""
    by_dir = {}
    for p in paths:
        by_dir.setdefault(os.path.dirname(p), []).append(p)
    out = []
    for dirname in sorted(by_dir):
        items = by_dir[dirname]
        if len(items) > limit:
            out.append(f'- `{dirname}/` ({len(items)} files)')
        else:
            out.extend(f'- `{p}`' for p in items)
    return out


def mod_label(meta):
    v = meta['version']
    # CurseForge's display name is often just the file name (shown anyway) or repeats the mod name
    if v.lower().startswith(meta['name'].lower()):
        v = v[len(meta['name']):].strip(' -_')
    return meta['name'] + (f' {v}' if v and not v.lower().endswith('.jar') else '')


def render_changes(cur, prev, d):
    m = d['mods']
    lines = []
    lines += details(f"Mods added ({len(m['added'])})",
                     [f"- {mod_label(meta)} `{j}` ({meta['side']})" for j, meta in m['added']])
    lines += details(f"Mods updated ({len(m['updated'])})",
                     [f"- {mod_label(nm)}: `{oj}` -> `{nj}`" for oj, om, nj, nm in m['updated']])
    lines += details(f"Mods removed ({len(m['removed'])})",
                     [f"- {mod_label(meta)} `{j}` (was {meta['side']})" for j, meta in m['removed']])
    lines += details(f"Mods whose side changed ({len(m['sides'])})",
                     [f"- {meta['name']} `{j}`: {a} -> {b}" for j, a, b, meta in m['sides']])
    for cat, ch in d['files'].items():
        n = sum(len(v) for v in ch.values())
        if not n:
            continue
        body = []
        for kind, paths in ch.items():
            if paths:
                body += [f'*{kind} ({len(paths)})*', ''] + collapse(paths) + ['']
        unshipped = sum(len(v) for k, v in ch.items() if k not in ('added', 'changed', 'removed'))
        lines += details(f"{cat}/: {len(ch['added'])} added, {len(ch['changed'])} changed, {len(ch['removed'])} removed"
                         + (f', {unshipped} no longer shipped' if unshipped else ''), body[:-1], open_limit=25)
    if not lines:
        lines = ['No file changes since the previous version.', '']
    return lines


def removals_from(pay):
    at = pay.get('fromByTarget') or {}
    if len(set(at.values())) > 1:
        return ' (' + ', '.join(f'{t} from {v}' for t, v in at.items()) + ')'
    return f" (from {pay['from']})" if pay['from'] else ''


def render_removals(d):
    lines = []
    for target in ('server', 'client'):
        rm = d['removals'][target]
        if rm:
            lines += details(f'Delete on the {target} ({len(rm)}, also in removals_{target}.txt)',
                             [f'- `{p}`' for p in rm], open_limit=30)
    return lines or ['None.', '']


def render_entry(cur, prev, d, dpay, stats):
    counts = (f"{len(cur['files']['mods'])} server+client mods, {len(cur['files']['client_mods'])} client-only mods, "
              f"{len(cur['files']['resourcepacks'])} resource pack files, {len(cur['files']['shaderpacks'])} shader pack files")
    commit = cur['commit'] or 'no git'
    if cur['dirty']:
        commit += f" + {len(cur['dirty'])} uncommitted file(s)"
    pay = cur['payload']
    n, size, by_cat = stats
    lines = [f"<!-- deploy:{cur['version']} -->",
             f"## {cur['version']} - {cur['date'][:16].replace('T', ' ')}", '',
             f"- Minecraft {cur['minecraft']}, {cur['loader']}",
             f"- Commit: {commit}",
             f"- Since: {prev['version'] if prev else 'nothing (first deployment: everything is new)'}",
             f"- Contents: {counts}",
             f"- Payload: {pay['mode']}" + (f" from {pay['from']}" if pay['from'] else '')
             + f", config {pay.get('config', 'full')}: {n} files, {mb(size)} ({by_cat})"]
    if cur['notes']:
        lines.append(f"- Notes: {cur['notes']}")
    if cur['warnings']:
        lines.append('- Built with warnings:')
        lines += [f'  - {w.splitlines()[0]}' for w in cur['warnings']]
    lines += ['', '### Changes', ''] + render_changes(cur, prev, d)
    lines += ['### Removals' + removals_from(pay), ''] + render_removals(dpay)
    return lines


def numbered(steps):
    return [f'{i + 1}. {s}' for i, s in enumerate(x for x in steps if x)]


def render_readme(cur, prev, d, dpay, base, payload):
    pay = cur['payload']
    update = pay['mode'] == 'update'
    has = lambda cat: bool(payload.get(cat))
    folders = lambda cats: ', '.join(f'`{c}/`' for c in cats if has(c))
    changed_wsc = sorted(payload.get('defaultconfigs', {})) if update else []
    world_step = lambda where: (
        f"Existing worlds: `defaultconfigs/` only reaches a world created after it. Copy the files in "
        f"`world_serverconfig/` into `<world>/serverconfig/` of {where}, then start it; this replaces those "
        f"files' per-world edits, if any."
        + (f" Changed since {pay['from']}: " + ', '.join(f'`{n}`' for n in changed_wsc) + '.' if changed_wsc
           else f" None changed since {pay['from']}: only a world made before they shipped needs them." if update
           else '')) if has('world_serverconfig') else None
    only_changed = ' (only the new and changed files)' if update else ''
    rm = lambda t: ("Delete every path in `removals_%s.txt` (from the %s folder): `xargs -d '\\n' rm -f < removals_%s.txt`"
                    % (t, 'server' if t == 'server' else 'instance', t)) if dpay['removals'][t] else 'Nothing to delete.'
    lines = [f"# {cur['name']} {cur['version']}" + (f" (update from {pay['from']})" if update else ' (full install)'), '',
             f"Built {cur['date'][:16].replace('T', ' ')} from commit {cur['commit'] or '?'}"
             f"{' (with uncommitted changes)' if cur['dirty'] else ''}. "
             f"Minecraft {cur['minecraft']}, {cur['loader']}.", '']
    if update:
        at = pay.get('fromByTarget') or {'server': pay['from'], 'client': pay['from']}
        expect = (f"the server at {at['server']} and clients at {at['client']}" if at['server'] != at['client']
                  else f"machines at {at['server']}")
        lines += [f"**Update payload.** It holds only the files that are new or changed since {pay['from']}; files "
                  f"that are gone are in the removals lists. It expects {expect}.", '',
                  f"## Check each machine first ({expect})", '',
                  '- Linux/macOS: `sh verify.sh server /path/to/server` or `sh verify.sh client /path/to/instance`',
                  '- Windows: `powershell -ExecutionPolicy Bypass -File verify.ps1 client "C:\\path\\to\\instance"`', '',
                  "It hashes every jar in that `mods/` and compares it with what that target's version shipped. `OK`: carry on. "
                  "`DRIFT` (a missing, changed or extra jar): don't apply this payload; fix the listed jars by hand or "
                  'use a full install (`python3 tools/build_deploy.py --mode install`). After applying, the same '
                  f"command with `--installed` (`-Installed`) checks the machine against {cur['version']}.", '']
    else:
        lines += ['**Full install.** Everything the pack needs. Use it for a new machine, or for one whose `verify` '
                  'reported drift: in that case move its `mods/` aside first, so no stale jar survives '
                  '(`apply` does that: `mods_before_<version>/`).', '']
    lines += ['## Apply with the script', '',
              '- Linux/macOS: `sh apply.sh server /path/to/server` or '
              '`sh apply.sh client /path/to/instance [--world saves/<world>]...`',
              '- Windows: `powershell -ExecutionPolicy Bypass -File apply.ps1 client "C:\\path\\to\\instance" '
              '[-World saves\\<world>,...]`', '',
              'Stop the server or close the game first. '
              + ('It runs verify and stops on `DRIFT` (`--force` / `-Force` applies anyway), then does ' if update
                 else 'It does ')
              + "the numbered steps below for that target: removals, every folder copied to its place, "
              "`world_serverconfig/` into the server's `level-name` world (client: only the `--world` saves; "
              '`--no-world` for none). `LaunchServer.sh` keeps its `MEMORY` and `JAVAPATH` (`.bat`: `maxmemory`, '
              f"`javapath`). It ends with verify `--installed` against {cur['version']}.", '']
    if pay.get('config') == 'functional':
        lines += ["`config/` holds the pack's functional settings only. Players' own settings (keybinds, HUD and UI, "
                  "minimap, video, JEI layout and bookmarks) are neither shipped nor deleted.", '']
    lines += ['## Server', ''] + numbered([
        'Stop the server and back up the world.',
        rm('server'),
        ("Copy `mods/` into the server's `mods/`" + only_changed + '. Do not copy `client_mods/`.') if has('mods') else
        'No server jars changed.',
        f"Copy {folders(('kubejs', 'config', 'defaultconfigs', 'vinery_upstream', 'miners_upstream', 'roll_upstream', 'largemeals_upstream', 'farmers_upstream', 'oceanic_upstream', 'enders_upstream', 'goldenfood_upstream', 'tempad_upstream'))} over the server's folders{only_changed}."
        if folders(('kubejs', 'config', 'defaultconfigs', 'vinery_upstream', 'miners_upstream', 'roll_upstream', 'largemeals_upstream', 'farmers_upstream', 'oceanic_upstream', 'enders_upstream', 'goldenfood_upstream', 'tempad_upstream')) else None,
        ('`vinery_upstream/` goes next to `mods/`, never inside it: the Vinery backport reads those two jars and '
         'stops the game with a message if they are missing.') if has('vinery_upstream') else None,
        ("`miners_upstream/` goes next to `mods/`, never inside it: the Miner's Delight backport reads that jar and "
         'stops the game with a message if it is missing.') if has('miners_upstream') else None,
        ("`roll_upstream/` goes next to `mods/`, never inside it: Creative Delight's Roll Delight port reads that jar "
         'and stops the game with a message if it is missing.') if has('roll_upstream') else None,
        ("`largemeals_upstream/` goes next to `mods/`, never inside it: Creative Delight's Large Meals port reads that jar "
         'and stops the game with a message if it is missing.') if has('largemeals_upstream') else None,
        ("`farmers_upstream/` goes next to `mods/`, never inside it: patcherMod reads Farmer's Structures 1.0.6 from it for the "
         'new farm structures and house pieces. Without it the old 1.0.0 structures run alone.') if has('farmers_upstream') else None,
        *[(f"`{d}/` goes next to `mods/`, never inside it: Creative Delight's {n} member reads that jar "
           'and stops the game with a message if it is missing.') if has(d) else None
          for d, n in (('oceanic_upstream', 'Oceanic Delight'), ('enders_upstream', "End's Delight (Ender's additions)"),
                       ('goldenfood_upstream', 'Golden Food'))],
        ("`tempad_upstream/` goes next to `mods/`, never inside it: the Tempad port reads that jar "
         'and stops the game with a message if it is missing.') if has('tempad_upstream') else None,
        ('Copy the *contents* of `server/` into the server folder itself (`LaunchServer.sh`/`.bat`, `perf/`); if you '
         "changed MEMORY or JAVAPATH in the server's LaunchServer script, set them again.") if has('server') else None,
        world_step('the server world (`world/`, or the `level-name` folder)'),
    ])
    rp_order = [p[len('file/'):] for p in cur['resourcePackOrder'] if p.startswith('file/')]
    show_order = rp_order and (not update or (base or {}).get('resourcePackOrder') != cur['resourcePackOrder'])
    client_trees = folders(('kubejs', 'config', 'defaultconfigs', 'resourcepacks', 'shaderpacks', 'vinery_upstream', 'miners_upstream', 'roll_upstream', 'largemeals_upstream', 'farmers_upstream', 'oceanic_upstream', 'enders_upstream', 'goldenfood_upstream', 'tempad_upstream'))
    lines += ['', '## Client', ''] + numbered([
        'Close the game.',
        rm('client'),
        (f"Copy {folders(('mods', 'client_mods'))} into the instance's `mods/`{only_changed}. `mods/` is the server mod "
         'set, server-only mods included: the client needs them to host single-player worlds.')
        if has('mods') or has('client_mods') else 'No jars changed.',
        f"Copy {client_trees} over the instance's folders{only_changed}." if client_trees else None,
        world_step('each single-player save that needs them (`saves/<world>/`)'),
        ('Enable these resource packs, lowest priority first (1 ends up at the bottom of the in-game Selected list, '
         'the last one at the top); packs not listed stay off:') if show_order else None,
    ])
    if show_order:
        lines += [''] + [f'   {i + 1}. {p}' for i, p in enumerate(rp_order)]
    lines += ['', '## After copying', '',
              f"On the dev machine: `python3 tools/build_deploy.py --snapshot {cur['version']}` (add `--target server` "
              'or `--target client` if only one side got it). Updates are built from that snapshot, so skipping '
              'this makes the next update start from an older version (bigger, still correct).', '',
              '## Changes since ' + (prev['version'] if prev else 'nothing (first deployment)'), '']
    lines += render_changes(cur, prev, d)
    lines += ['## Removals' + removals_from(pay), ''] + render_removals(dpay)
    return '\n'.join(lines) + '\n'


def write_log(cur, entry):
    text = open(LOG, encoding='utf8').read() if os.path.isfile(LOG) else LOG_HEADER
    parts = re.split(r'(?m)^(?=<!-- deploy:)', text)
    head, entries = parts[0], [p for p in parts[1:] if not p.startswith(f"<!-- deploy:{cur['version']} -->")]
    body = '\n'.join(entry).rstrip() + '\n\n'
    with open(LOG, 'w', encoding='utf8') as f:
        f.write(head.rstrip() + '\n\n' + body + ''.join(entries))


def archive_in_tree_mods(files):
    """Store each in-tree jar this version ships in development/<project>/jar_archive/ (git-ignored). The build only
    adds versions that aren't archived yet, so this is what keeps the released jar there."""
    for project, prefix, _ in IN_TREE_MODS:
        archive = os.path.join(DEVELOPMENT, project, 'jar_archive')
        for jar, src in {**files['mods'], **files['client_mods']}.items():
            if in_tree_jar(prefix, jar):
                dst = os.path.join(archive, jar)
                if not os.path.isfile(dst) or sha1(dst) != sha1(src):
                    os.makedirs(archive, exist_ok=True)
                    shutil.copy2(src, dst)
                    print(f'archived the shipped {jar} in {os.path.relpath(archive, ROOT)}/')


# ── main ─────────────────────────────────────────────────────────────────────

def main():
    ap = argparse.ArgumentParser(description='Stage a release of the pack in deploy/latest/ and log it.')
    ap.add_argument('--version', help='version to stage (default: last version with its patch number + 1)')
    ap.add_argument('--notes', default='', help='one line for the log entry (or the snapshot)')
    ap.add_argument('--mode', choices=('update', 'install'),
                    help='update: only what changed since the deployed version (default once DEPLOYED.json exists); '
                         'install: everything')
    ap.add_argument('--from', dest='from_version', metavar='VERSION',
                    help='update from this version instead of the deployment snapshot in DEPLOYED.json')
    ap.add_argument('--dry-run', action='store_true', help='print the log entry and payload size; write and copy nothing')
    ap.add_argument('--full', action='store_true', help='update mode: ship every resource/shader pack anyway')
    ap.add_argument('--config', choices=CONFIG_MODES, default='functional',
                    help="functional (default): config/ without players' own settings (CONFIG_CLASSES 'player': "
                         'keybinds, HUD/UI, minimap, video, JEI); full: every config file')
    ap.add_argument('--force', action='store_true', help='build even if a preflight check fails (recorded in the log)')
    ap.add_argument('--snapshot', nargs='?', const='', metavar='VERSION',
                    help='record that VERSION (default: the one in deploy/latest/) was copied to the machines; '
                         'builds nothing')
    ap.add_argument('--target', choices=('both', 'server', 'client'), default='both', help='with --snapshot')
    ap.add_argument('--date', help='with --snapshot: when it was deployed (default: now), e.g. 2026-09-30T01:00')
    args = ap.parse_args()

    if args.snapshot is not None:
        snapshot(args.snapshot, ('server', 'client') if args.target == 'both' else (args.target,), args.date, args.notes)
        return

    pre = Preflight()
    history = load_history()
    last = history[-1]['version'] if history else None
    version = args.version or (re.sub(r'(\d+)(?!.*\d)', lambda m: str(int(m.group(1)) + 1), last) if last else '1.0.0')
    prev = next((s for s in reversed(history) if s['version'] != version), None)

    files, mods = collect(pre, config_mode=args.config, pack_version=version)
    print('checking structure data (regenerating in a scratch folder)...')
    check_structures(pre)
    check_in_tree_mods(pre)
    check_pinned_mods(pre)
    check_climate_reader(pre, files)
    check_manifest(pre, files)
    check_display_delight_models(pre)
    commit, dirty = git_state(pre)

    deployed = load_deployed()
    stage_dir = tempfile.mkdtemp(prefix='ce-changelog-')
    atexit.register(shutil.rmtree, stage_dir, True)
    check_changelog(pre, files, version, deployed, stage_dir)
    if version in deployed_versions(deployed):
        pre.error(f'{version} is already deployed (DEPLOYED.json): rebuilding it would change the snapshot the '
                  'machines are checked against. Build a new version (omit --version to bump the patch).')
    mode = args.mode or ('update' if default_base(deployed) or args.from_version else 'install')
    base = None
    if mode == 'update':
        from_version = args.from_version or default_base(deployed)
        if not from_version:
            pre.error('--mode update: nothing deployed yet (run --snapshot after copying a payload out) and no --from')
        elif from_version == version:
            pre.error(f'--from {from_version} is the version being built')
        else:
            base = load_base(pre, deployed, from_version)
    # Each target's own starting point: removals and verify lists come from it (the payload's files come
    # from the older `base`, a superset). --from overrides both.
    bases = {}
    if base:
        for t in ('server', 'client'):
            v = deployed.get(t, {}).get('version') if not args.from_version else None
            bases[t] = base if not v or v == base['version'] else load_base(pre, deployed, v)

    for w in pre.warnings:
        print('WARNING: ' + w)
    for e in pre.errors:
        print('ERROR: ' + e)
    if pre.errors and not args.force:
        sys.exit(f'{len(pre.errors)} preflight error(s): nothing written (--force to build anyway)')

    print(f'hashing {sum(len(v) for v in files.values())} files...')
    inst = json.load(open(os.path.join(ROOT, 'instance.json'), encoding='utf8'))['launcher']
    enabled, _ = enabled_resource_packs()
    cur = {
        'version': version,
        'date': datetime.datetime.now().isoformat(timespec='seconds'),
        'name': inst['name'],
        'minecraft': inst['version'],
        'loader': f"{inst['loaderVersion']['type']} {inst['loaderVersion']['version']}",
        'commit': commit,
        'dirty': dirty,
        'notes': args.notes,
        'warnings': pre.warnings + [f'FORCED past: {e}' for e in pre.errors],
        'payload': {'mode': mode, 'from': base['version'] if base else None, 'config': args.config,
                    **({'fromByTarget': {t: b['version'] for t, b in bases.items()}} if bases else {})},
        'resourcePackOrder': enabled,
        'mods': mods,
        'files': {cat: {p: sha1(src) for p, src in paths.items()} for cat, paths in files.items()},
    }
    d = diff(prev, cur)                  # changelog: since the previous build
    dpay = diff(base or prev, cur)       # removals: since what the machines run (each target its own)
    for t, b in bases.items():
        if b is not base:
            dpay['removals'][t] = diff(b, cur)['removals'][t]
    payload = select_payload(files, cur, base, mode, args.full)
    payload['world_serverconfig'] = dict(files['defaultconfigs'])   # complete in both modes (docstring)
    stats = payload_stats(payload)
    entry = render_entry(cur, prev, d, dpay, stats)
    if args.dry_run:
        print('\n'.join(entry))
        print(f'(dry run: nothing written; would stage {version} in {os.path.relpath(OUT, ROOT)}/: '
              f'{mode}{" from " + base["version"] if base else ""}, {stats[0]} files, {mb(stats[1])})')
        return

    if os.path.isdir(OUT):
        shutil.rmtree(OUT)
    os.makedirs(OUT)
    for cat, paths in payload.items():
        for rel, src in paths.items():
            dst = os.path.join(OUT, cat, rel)
            os.makedirs(os.path.dirname(dst), exist_ok=True)
            shutil.copy2(src, dst)
    for target in ('server', 'client'):
        if dpay['removals'][target]:
            with open(os.path.join(OUT, f'removals_{target}.txt'), 'w', encoding='utf8') as f:
                f.write('\n'.join(dpay['removals'][target]) + '\n')
    write_scripts(OUT, bases, cur, payload)
    with open(os.path.join(OUT, 'README.md'), 'w', encoding='utf8') as f:
        f.write(render_readme(cur, prev, d, dpay, base, payload))
    if os.path.isfile(RUNBOOK):
        shutil.copy2(RUNBOOK, os.path.join(OUT, 'RUNBOOK.md'))
    with open(os.path.join(OUT, 'VERSION.txt'), 'w', encoding='utf8') as f:
        f.write(version + (f' (update from {base["version"]})' if base else ' (full install)') + '\n')

    os.makedirs(HISTORY, exist_ok=True)
    with open(history_path(version), 'w', encoding='utf8') as f:
        json.dump(cur, f, indent=1, ensure_ascii=False)
        f.write('\n')
    write_log(cur, entry)
    archive_in_tree_mods(files)

    print(f"staged {version} in {os.path.relpath(OUT, ROOT)}/ ({mode}{' from ' + base['version'] if base else ''}): "
          f"{stats[0]} files, {mb(stats[1])} ({stats[2]}); "
          f"removals: {len(dpay['removals']['server'])} server, {len(dpay['removals']['client'])} client")
    print(f'logged in {os.path.relpath(LOG, ROOT)}; after copying it out: '
          f'python3 tools/build_deploy.py --snapshot {version}')


if __name__ == '__main__':
    main()
