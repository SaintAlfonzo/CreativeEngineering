#!/usr/bin/env python3
"""Precipitation and temperature by biome: what falls where, in every Serene Seasons sub-season.

    python3 tools/gen_weather_table.py

Writes feature_writeup/weather_by_biome.md (generated reference; the hand-written side is
feature_writeup/weather.md). Rerun after `/dumpbiomes`, after adding or updating biome mods, after
`/dumpclimate`, or after editing season_types.json or the Serene Seasons configs. Exit code 1 when
season_types.json has problems (unknown ids, a biome in two lists, unknown snow_seasons names).

Inputs:
- kubejs/exported/biome_data.json (/dumpbiomes): every biome's live base temperature, downfall and declared
  precipitation, and the dimensions whose biome source can place it.
- kubejs/generation_scripts/worldgen/season_types.json: the pack's season types (arid, frozen, dry, tropical,
  temperate, adaptive) and snow_seasons (the sub-seasons a biome may snow in), applied in game as biome tags by
  kubejs/server_scripts/tags/seasontags.js and patcherMod seasons/SeasonTypes.
- Biome jsons (vanilla, Forge, mod jars, kubejs/data, via gen_crop_worldgen_table): temperature_modifier, and
  the is_ocean / is_river / is_beach tags.
- kubejs/generation_scripts/worldgen/biome_config.json: post-tuning share of each dimension per biome.
- kubejs/generation_scripts/worldgen/climate/minecraft__overworld.json.gz (/dumpclimate): surface climate
  samples with the live biome, used for "which temperature band does this biome actually sit in", the
  neighbour check and what rivers/beaches resolve to.
- configs_edited/server_config/sereneseasons-server.toml (the pack's server copy) and Serene Seasons' built-in
  defaults (what single-player worlds get: the pack ships no defaultconfigs/ copy), config/sereneseasons/
  seasons.toml, config/betterdays-common.toml, saves/*/serverconfig and saves/*/data/seasons.dat.

The rules restate vanilla 1.19.2 (Biome, LevelRenderer.renderSnowAndRain, ServerLevel.tickChunk), Serene
Seasons 8.1.0.24 (SeasonHooks, BiomeConfig, SeasonTime, MixinLevelRenderer, transformers/temperature.js) read
from the decompiled jars, and the patcherMod season mixins (seasons/SeasonTypes.java). The writeup's
section 1 cites each one.
"""
import gzip
import json
import re
import struct
import sys
from collections import Counter, defaultdict
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import gen_crop_worldgen_table as cw  # biome json + tag loading (vanilla, Forge, mods, kubejs/data)

INSTANCE = Path(__file__).resolve().parent.parent
BIOME_DATA = INSTANCE / 'kubejs/exported/biome_data.json'
BIOME_CONFIG = INSTANCE / 'kubejs/generation_scripts/worldgen/biome_config.json'
CLIMATE = INSTANCE / 'kubejs/generation_scripts/worldgen/climate/minecraft__overworld.json.gz'
SEASON_TYPES = INSTANCE / 'kubejs/generation_scripts/worldgen/season_types.json'
SS_PACK = INSTANCE / 'configs_edited/server_config/sereneseasons-server.toml'
SS_SEASONS = INSTANCE / 'config/sereneseasons/seasons.toml'
CE_CONFIG = INSTANCE / 'config/creativeengineering-common.toml'
BETTER_DAYS = INSTANCE / 'config/betterdays-common.toml'
SAVES = INSTANCE / 'saves'
OUT_MD = INSTANCE / 'feature_writeup/weather_by_biome.md'

MAX_Y = 320          # overworld build height (Tectonic increased_height = false)
SNOW_BELOW = 0.15    # Biome.warmEnoughToRain: temperature >= 0.15F
FROZEN_MAX = 0.1     # SeasonTypes.FROZEN_MAX_TEMPERATURE
SNOW_LINE_DEFAULT = 320  # CEConfig [seasons] snowLineY (build height)

# Serene Seasons ServerConfig defaults (single-player worlds get these)
SS_DEFAULT = {
    'day_duration': 24000, 'sub_season_duration': 8, 'starting_sub_season': 1,
    'whitelisted_dimensions': ['minecraft:overworld'],
    'blacklisted_biomes': ['minecraft:lush_caves', 'minecraft:dripstone_caves', 'minecraft:ocean', 'minecraft:deep_ocean',
                           'minecraft:frozen_ocean', 'minecraft:deep_frozen_ocean', 'minecraft:cold_ocean',
                           'minecraft:deep_cold_ocean', 'minecraft:lukewarm_ocean', 'minecraft:deep_lukewarm_ocean',
                           'minecraft:warm_ocean', 'minecraft:river', 'minecraft:the_void',
                           'biomesoplenty:glowing_grotto', 'biomesoplenty:spider_nest'],
    'tropical_biomes': ['minecraft:swamp', 'minecraft:warm_ocean'],
}

SUBS = ['ESp', 'MSp', 'LSp', 'ESu', 'MSu', 'LSu', 'EAu', 'MAu', 'LAu', 'EWi', 'MWi', 'LWi']
SUB_NAMES = ['Early Spring', 'Mid Spring', 'Late Spring', 'Early Summer', 'Mid Summer', 'Late Summer',
             'Early Autumn', 'Mid Autumn', 'Late Autumn', 'Early Winter', 'Mid Winter', 'Late Winter']
# SeasonHooks.getBiomeTemperatureInSeason: subtracted from temperate biomes (base <= 0.8, not tropical)
OFFSET = [0.4, 0.2, 0.1, 0.0, 0.0, 0.0, 0.1, 0.2, 0.4, 0.8, 0.8, 0.8]
TROP_NAMES = ['Early Dry', 'Mid Dry', 'Late Dry', 'Early Wet', 'Mid Wet', 'Late Wet']
# SeasonTime.getTropicalSeason: ((subSeason + 11) / 2 + 5) % 6
TROP = [((s + 11) // 2 + 5) % 6 for s in range(12)]
MID_DRY, MID_WET = 1, 4
SEASON_WORDS = ['spring', 'summer', 'autumn', 'winter']
SUB_IDS = [f'{part}_{season}' for season in SEASON_WORDS for part in ('early', 'mid', 'late')]
WINTER = {9, 10, 11}

# filled by main(): season_types.json snow_seasons as {biome: set of sub-season indexes it may snow in}, and
# CEConfig [seasons] snowLineY
SNOW_ALLOWED = {}
SNOW_LINE_MIN = SNOW_LINE_DEFAULT
WET = {3, 4, 5}

# vanilla OverworldBiomeBuilder temperature parameter bands, in the climate dump's x10000 units
BANDS = ['frozen', 'cold', 'temperate', 'warm', 'hot']
BAND_EDGES = [-4500, -1500, 2000, 5500]

PERMANENT = ['permanent_spring', 'permanent_summer', 'permanent_autumn', 'permanent_winter']
TYPE_KEYS = ['arid', 'frozen', 'dry', 'tropical', 'temperate', 'cold', 'adaptive'] + PERMANENT
# SeasonTypes.tagged: the first list that names the biome wins
TAGGED = ['arid', 'frozen', 'permanent_winter', 'permanent_spring', 'permanent_summer', 'permanent_autumn',
          'dry', 'tropical', 'cold', 'temperate']
FROZEN_LIKE = ('frozen', 'permanent_winter')
# SeasonTypes.COLD_SNOW: Late Autumn to Early Spring
COLD_SNOW = {8, 9, 10, 11, 0}
# classes: off (vanilla), arid, frozen, dry, tropical and temperate (tagged), ss-tropical and ss-temperate
# (untagged, Serene Seasons' rule), adaptive (rivers/beaches: the nearest land's class)
CLASS_TITLES = [('arid', 'Arid'), ('frozen', 'Frozen'), ('dry', 'Dry (Mid Wet rain only)'),
                ('tropical', 'Tropical (tagged)'),
                ('ss-tropical', 'Tropical by Serene Seasons\' rule (untagged, warmer than 0.8 or on its list)'),
                ('temperate', 'Temperate (tagged)'), ('cold', 'Cold (snow Late Autumn to Early Spring)'),
                ('ss-temperate', 'Temperate by Serene Seasons\' rule (untagged)'),
                ('permanent_spring', 'Permanent spring'), ('permanent_summer', 'Permanent summer'),
                ('permanent_autumn', 'Permanent autumn'), ('permanent_winter', 'Permanent winter (frozen, shown as Winter)'),
                ('adaptive', 'Adaptive (rivers and beaches)'), ('off', 'Seasons off (blacklisted)')]
SEASONAL = ('temperate', 'cold', 'ss-temperate')
WET_DRY = ('dry', 'tropical', 'ss-tropical')


def f32(x):
    return struct.unpack('f', struct.pack('f', x))[0]


def band_of(t):
    return sum(t >= e for e in BAND_EDGES)


# ── configs ──────────────────────────────────────────────────────────────

def toml_value(text, key):
    m = re.search(r'^\s*' + key + r'\s*=\s*(\[.*?\]|\S+)', text, re.M | re.S)
    if not m:
        return None
    v = m.group(1)
    if v.startswith('['):
        return re.findall(r'"([^"]+)"', v)
    if v in ('true', 'false'):
        return v == 'true'
    try:
        return float(v) if '.' in v else int(v)
    except ValueError:
        return v.strip('"')


def load_ss(path):
    text = path.read_text()
    cfg = dict(SS_DEFAULT)
    for k in SS_DEFAULT:
        v = toml_value(text, k)
        if v is not None:
            cfg[k] = v
    return cfg


def load_types():
    raw = json.loads(SEASON_TYPES.read_text())
    return {k: list(raw.get(k, [])) for k in TYPE_KEYS}


def season_ticks(path):
    """SeasonCycleTicks from a world's data/seasons.dat (gzipped NBT, read by key without a full parser)."""
    raw = gzip.open(path).read()
    i = raw.find(b'SeasonCycleTicks')
    return struct.unpack('>i', raw[i + 16:i + 20])[0] if i >= 0 else None


def seasons_config(key, default):
    """A CEConfig [seasons] value."""
    if not CE_CONFIG.exists():
        return default
    m = re.search(r'^\[seasons\](.*?)(^\[|\Z)', CE_CONFIG.read_text(), re.M | re.S)
    v = toml_value(m.group(1), key) if m else None
    return default if v is None else v


def seasons_enabled():
    """CEConfig [seasons] enabled (default true)."""
    return seasons_config('enabled', True) is not False


def load_snow_seasons():
    """season_types.json snow_seasons -> ({biome: allowed sub-season indexes}, [(biome, bad name)]), as seasontags.js."""
    raw = json.loads(SEASON_TYPES.read_text()).get('snow_seasons', {})
    allowed, bad = {}, []
    for bid, names in raw.items():
        subs = set()
        for name in names:
            if name in SUB_IDS:
                subs.add(SUB_IDS.index(name))
            elif name in SEASON_WORDS:
                subs.update(i for i, sid in enumerate(SUB_IDS) if sid.endswith('_' + name))
            else:
                bad.append((bid, name))
        allowed[bid] = subs
    return allowed, bad


# ── the rules ────────────────────────────────────────────────────────────

class Biome:
    def __init__(self, bid, data, modifier, tags):
        self.id = bid
        self.base = data['temperature']
        self.downfall = data['downfall']
        self.precip = data['precipitation']
        self.dims = data['dimensions']
        self.modifier = modifier
        self.ocean = bid in tags['ocean']
        # by name too: Terralith leaves terralith:warm_river out of #minecraft:is_river
        self.edge = bid in tags['river'] or bid in tags['beach'] or bool(re.search(r'river|beach|shore', bid))


def ss_class(b, cfg, types):
    """The biome's class under the given Serene Seasons config and season types (SeasonTypes + BiomeConfig).

    A tagged biome has seasons even when blacklisted (SeasonBiomeConfigMixin.enablesSeasonalEffects).
    """
    if not any(d in cfg['whitelisted_dimensions'] for d in b.dims):
        return 'off'
    if types and b.id in types['adaptive']:
        return 'adaptive'
    for t in TAGGED:
        if types and b.id in types[t]:
            return t
    if b.id in cfg['blacklisted_biomes']:
        return 'off'
    if b.id in cfg['tropical_biomes'] or b.base > 0.8:
        return 'ss-tropical'
    return 'ss-temperate'


def sea_level_temp(b, cls, sub):
    """Serene Seasons' seasonal temperature, then SeasonTypes.seasonTemperature's rule for the type."""
    # untagged temperate is always <= 0.8; tagged temperate and cold take the offset above it too
    t = f32(max(-0.5, min(2.0, f32(b.base) - f32(OFFSET[sub])))) if cls in SEASONAL else f32(b.base)
    if cls in FROZEN_LIKE:
        snow = True
    elif cls == 'cold':
        snow = sub in COLD_SNOW
    elif cls in ('permanent_spring', 'permanent_autumn'):
        snow = sub in WINTER
    elif cls == 'permanent_summer':
        snow = False
    else:
        return t
    return min(t, f32(FROZEN_MAX)) if snow else max(t, f32(SNOW_BELOW))


def snow_blocked(b, cls, sub):
    """season_types.json snow_seasons leaves this sub-season out (SeasonTypes.applySnowRules; frozen and
    permanent seasons ignore it)."""
    return cls not in FROZEN_LIKE and cls not in PERMANENT and b.id in SNOW_ALLOWED and sub not in SNOW_ALLOWED[b.id]


def cell(b, cls, sub, snow_rules=True):
    """What the client draws at y <= 80 (no height adjustment), in one sub-season.

    R rain, S snow, - nothing, R! rain in a biome that declares no precipitation (Serene Seasons' own tropical
    Mid Wet, outside the dry type), -* nothing falls but snow layers still settle during storms
    (shouldSnow ignores precipitation), ~ adaptive (takes the land's cell). snow_rules=False: without the
    snow_seasons lists (Serene Seasons alone).
    """
    if cls == 'adaptive':
        return '~'
    if cls == 'arid':
        return '-'
    t = sea_level_temp(b, cls, sub)
    cold = t < f32(SNOW_BELOW) and not (snow_rules and snow_blocked(b, cls, sub))
    if cls == 'dry':
        return ('S' if cold else 'R') if TROP[sub] == MID_WET else '-'
    if cls in ('tropical', 'ss-tropical') and b.precip in ('RAIN', 'NONE'):
        if TROP[sub] == MID_DRY:
            return '-'
        if TROP[sub] == MID_WET:
            return 'R!' if b.precip == 'NONE' else ('S' if cold else 'R')
    if b.precip == 'NONE':
        return '-*' if cold else '-'
    return 'S' if cold else 'R'


def snow_line_y(t, blocked=False, gauge=False):
    """Lowest y where it snows (None: at every height). Biome.getHeightAdjustedTemperature cools 0.05 per 40 blocks
    above y80 (+-8 blocks noise); SeasonTypes.applySnowRules keeps rain below CEConfig snowLineY when it rains at
    y80, and when snow_seasons leaves the sub-season out; gauge (temperate and cold): always snows from snowLineY up."""
    if t < SNOW_BELOW:
        return SNOW_LINE_MIN if blocked else None
    line = max(80 + (t - SNOW_BELOW) / 0.00125, SNOW_LINE_MIN)
    return min(line, SNOW_LINE_MIN) if gauge else line


def snow_line_text(y):
    if y is None:
        return 'all'
    return 'never' if y > MAX_Y else f'y{round(y)}'


def snow_line(t):
    return snow_line_text(snow_line_y(t))


# ── data ─────────────────────────────────────────────────────────────────

def load_biomes():
    tags, biome_files, *_ = cw.load_all()
    cache = {}
    tagsets = {k: set(cw.resolve_tag(f'minecraft:is_{k}', tags, cache)) for k in ('ocean', 'river', 'beach')}
    data = json.loads(BIOME_DATA.read_text())
    out = {}
    for bid, d in data.items():
        if d.get('temperature') is None:
            continue
        mod = 'none'
        if bid in biome_files:
            mod = biome_files[bid][1].get('temperature_modifier', 'none')
        out[bid] = Biome(bid, d, mod, tagsets)
    return out


def load_area():
    cfg = json.loads(BIOME_CONFIG.read_text())
    area = {}
    for dim, dd in cfg['dimensions'].items():
        for b, info in dd['biomes'].items():
            area[(dim, b)] = (info.get('layer', 'surface'), info.get('result_pct', info.get('share_pct')) or 0.0)
    return area


def load_samples():
    """Overworld surface samples: list of (band, biome id)."""
    if not CLIMATE.exists():
        return []
    dump = json.loads(gzip.open(CLIMATE).read())
    names = dump['biomes']
    return [(band_of(p[1]), names[p[7]]) for p in dump['points'] if p[0] == 0]


# ── checks ───────────────────────────────────────────────────────────────

def band_land(biomes, samples):
    """band -> Counter(biome -> samples) over land biomes (not ocean, river or beach)."""
    land = defaultdict(Counter)
    for band, bid in samples:
        b = biomes.get(bid)
        if b and not b.ocean and not b.edge:
            land[band][bid] += 1
    return land


def neighbour_check(biomes, cls_of, samples):
    """Per biome, band and sub-season: does it snow while most land in the same temperature band does not, or the reverse?

    "Land in the band" is every surface sample of a biome that is not ocean, river or beach, weighted by how
    often it was sampled; dry biomes (declared none) count as not snowing. It stands in for "the biomes around
    it". Each band the biome was sampled in is judged on its own, if it holds at least 3 samples and 10% of the
    biome's. Adaptive biomes are left out: they take the land's behaviour by construction.
    Returns [(biome, samples, {band: [(sub, 'snows'|'rains', share of band land that does not)]})].
    """
    land = band_land(biomes, samples)
    own = defaultdict(Counter)
    for band, bid in samples:
        own[bid][band] += 1
    snow_frac = {}
    for band, ctr in land.items():
        n = sum(ctr.values())
        snow_frac[band] = [sum(c for bid, c in ctr.items() if cell(biomes[bid], cls_of[bid], s) == 'S') / n
                           for s in range(12)]
    found = []
    for bid, bands in own.items():
        b = biomes.get(bid)
        if not b or b.ocean or b.precip == 'NONE' or cls_of[bid] in ('adaptive', 'arid'):
            continue
        n = sum(bands.values())
        per_band = {}
        for bd, cnt in bands.items():
            if cnt < 3 or cnt / n < 0.1 or bd not in snow_frac:
                continue
            rows = []
            for s in range(12):
                exp, c = snow_frac[bd][s], cell(b, cls_of[bid], s)
                if c == 'S' and exp < 0.25:
                    rows.append((s, 'snows', 1 - exp))
                elif c in ('R', 'R!') and exp > 0.75:
                    rows.append((s, 'rains', exp))
            if rows:
                per_band[bd] = rows
        if per_band:
            found.append((bid, n, bands, per_band))
    return found


def type_file_checks(biomes, types):
    """Unknown ids and biomes listed twice in season_types.json."""
    unknown = [(k, b) for k in TYPE_KEYS for b in types[k] if b not in biomes]
    seen = defaultdict(list)
    for k in TYPE_KEYS:
        for b in types[k]:
            seen[b].append(k)
    twice = {b: ks for b, ks in seen.items() if len(ks) > 1}
    return unknown, twice


# ── output ───────────────────────────────────────────────────────────────

def pct(x):
    return f'{x:.1f}%' if x >= 0.1 else ('<0.1%' if x > 0 else '-')


def band_text(ctr):
    n = sum(ctr.values())
    if not n:
        return '-'
    parts = [f'{BANDS[bd]} {100 * c // n}' for bd, c in sorted(ctr.items(), key=lambda kv: -kv[1]) if c / n >= 0.1]
    return ', '.join(parts) + f' (n={n})'


def main():
    biomes = load_biomes()
    area = load_area()
    samples = load_samples()
    global SNOW_ALLOWED, SNOW_LINE_MIN
    pack = load_ss(SS_PACK)
    types = load_types() if seasons_enabled() else None
    snow_allowed, bad_snow = load_snow_seasons()
    if types:
        SNOW_ALLOWED = snow_allowed
        SNOW_LINE_MIN = seasons_config('snowLineY', SNOW_LINE_DEFAULT)
    else:
        SNOW_LINE_MIN = -64
    seasons_txt = SS_SEASONS.read_text() if SS_SEASONS.exists() else ''
    cls_of = {bid: ss_class(b, pack, types) for bid, b in biomes.items()}
    cls_sp = {bid: ss_class(b, SS_DEFAULT, types) for bid, b in biomes.items()}
    cls_raw = {bid: ss_class(b, pack, None) for bid, b in biomes.items()}
    bands_of = defaultdict(Counter)
    for band, bid in samples:
        bands_of[bid][band] += 1
    neighbours = neighbour_check(biomes, cls_of, samples)
    unknown, twice = type_file_checks(biomes, load_types())

    ow = [b for b in biomes.values() if 'minecraft:overworld' in b.dims]

    def share(b):
        layer, p = area.get(('minecraft:overworld', b.id), ('surface', 0.0))
        return p if layer == 'surface' else 0.0

    L = []
    w = L.append
    w('# Weather by biome (generated)\n')
    w('Generated by `python3 tools/gen_weather_table.py`. Do not edit by hand. Rerun after `/dumpbiomes`, '
      '`/dumpclimate`, adding or updating biome mods, or editing `season_types.json` or the Serene Seasons configs. '
      'The hand-written side (design, decisions, tests) is [weather.md](weather.md).\n')
    w('What falls from the sky in every biome, in every Serene Seasons sub-season, as the pack is configured now. '
      'Section 1 is the rules, section 2 the problems the generator finds, section 3 the overworld table, '
      'section 4 the other dimensions, section 5 the calendar and the local worlds.\n')
    if types is None:
        w('**CEConfig `[seasons] enabled = false`: the season types are off, Serene Seasons\' own rules apply.**\n')

    # ── 1. rules
    w('## 1. How it is decided\n')
    w('### 1.1 Vanilla\n')
    w('- A biome has a **base temperature** and a **declared precipitation** (`none`, `rain` or `snow`). '
      'The declaration only decides *whether* anything falls: `LevelRenderer.renderSnowAndRain` draws nothing for '
      '`none`, and for `rain` or `snow` draws snow when the temperature at that block is below 0.15 and rain '
      'otherwise. So a `snow` biome at 0.2 shows rain, and a `rain` biome at 0.0 shows snow.')
    w('- **Height**: above y80 the temperature drops 0.05 per 40 blocks (plus or minus 8 blocks of noise), '
      '`Biome.getHeightAdjustedTemperature`. The "snow line" columns below are where that crosses 0.15, '
      'but never below the pack\'s minimum snow line (1.3).')
    w('- **Temperature modifier** `frozen` (frozen oceans): patches of the biome read 0.2 instead of the base.')
    w('- **Gameplay rain** (`Level.isRainingAt`: fire put out, cauldrons, enderman damage, riptide, lightning '
      'targets) needs declared `rain` and temperature >= 0.15.')
    w('- **Snow layers and ice** (`ServerLevel.tickChunk`, `Biome.shouldSnow` / `shouldFreeze`) only look at the '
      'temperature, never at the declared precipitation. Snow layers need the world to be raining; ice forms in any '
      'weather.\n')
    w('### 1.2 Serene Seasons\n')
    w('Seasons apply in the whitelisted dimensions (`' + '`, `'.join(pack['whitelisted_dimensions']) + '`), '
      'except in `blacklisted_biomes`. Its own two kinds of biome:\n')
    w('- **tropical**: `tropical_biomes`, plus every biome with base temperature above 0.8 (hardcoded in '
      '`BiomeConfig.usesTropicalSeasons`). Temperature never changes; wet/dry cycle below.')
    w('- **temperate**: everything else. Temperature lowered by the sub-season offset below.\n')
    w('Temperate offsets (`SeasonHooks.getBiomeTemperatureInSeason`, clamped to -0.5..2.0):\n')
    w('| ' + ' | '.join(SUBS) + ' |')
    w('|' + '---|' * 12)
    w('| ' + ' | '.join(f'-{o:g}' if o else '0' for o in OFFSET) + ' |\n')
    w('Tropical seasons (`SeasonTime.getTropicalSeason`):\n')
    w('| ' + ' | '.join(SUBS) + ' |')
    w('|' + '---|' * 12)
    w('| ' + ' | '.join(TROP_NAMES[t] for t in TROP) + ' |\n')
    w('- **Mid Dry**: no rain at all (`shouldRainInBiomeInSeason` false).')
    w('- **Mid Wet**: rain, even in biomes that declare `none` (`shouldRainInBiomeInSeason` returns true before '
      'it looks at the declaration; the client renderer and `isRainingAtHook` both use it).')
    w('- Other tropical seasons: the declared precipitation, as vanilla.')
    w('- `isRainingAtHook` returns true for any tropical biome without checking the temperature.\n')
    w('The renderer, the gameplay rain check, snow layers, freezing, melting and snow golem burning all use the '
      'seasonal temperature (coremod `transformers/temperature.js` plus the mixins). `generate_snow_ice` = '
      f'`{str(toml_value(seasons_txt, "generate_snow_ice")).lower()}`, `change_weather_frequency` = '
      f'`{str(toml_value(seasons_txt, "change_weather_frequency")).lower()}` (`RandomUpdateHandler.adjustWeatherFrequency`: '
      'in winter thunder is cleared and a dry spell longer than 1.5 days is cut to 0.5-1.5; in spring a dry spell '
      'longer than 4 days is cut to 0.5-4; in summer the wait for thunder is cut the same way to 0.5-1.5 days).\n')
    w('### 1.3 The pack\'s season types (patcherMod `seasons/SeasonTypes`)\n')
    w('Biome tags `creativeengineering:seasons/<type>` from `kubejs/generation_scripts/worldgen/season_types.json`; '
      'details and tests in [weather.md](weather.md). A tagged biome ignores Serene Seasons\' tropical rule and its '
      'blacklist (oceans). Oceans that are not frozen-tagged get the seasonal weather but keep vanilla ice (they never '
      'freeze over); frozen oceans freeze with the season.\n')
    w('| Type | Biomes | Weather | Temperature | HUD |')
    w('|---|---|---|---|---|')
    if types:
        w(f'| arid | {len(types["arid"])} (sand, terracotta) | none, all year | base, never shifts | "Arid", day N of the year |')
        w(f'| frozen | {len(types["frozen"])} | snow all year | at most {FROZEN_MAX}, never shifts | "Frozen", day N of the year |')
        w(f'| dry | {len(types["dry"])} (savannas, shrublands) | wet/dry cycle, rain in Mid Wet only, whatever '
          'the biome declares | base, never shifts | Dry / Wet seasons |')
        w(f'| tropical | {len(types["tropical"])} | wet/dry cycle, rain except in Mid Dry | base, never shifts '
          '| Dry / Wet seasons |')
        w(f'| temperate | {len(types["temperate"])} | Serene Seasons\' temperate rain/snow | seasonal offset, '
          'also above 0.8 | four seasons |')
        w(f'| cold | {len(types["cold"])} (taiga, windswept, highlands, cold oceans) | snow Late Autumn to Early Spring, '
          'rain the rest of the year | seasonal offset, then held at 0.1 or colder in the snow window, 0.15 or warmer '
          'outside it | four seasons |')
        for k, name in (('permanent_spring', 'Spring'), ('permanent_autumn', 'Autumn')):
            snowy = 'Early Spring' if name == 'Spring' else 'Late Autumn'
            w(f'| {k} | {", ".join(x.split(":")[1] for x in types[k]) or "-"} | snow in its {snowy} (the world\'s '
              f'winter, 1/4 of the year), rain otherwise | base, no offset | "{name}": Early 1/4, Mid 1/2, Late 1/4 of '
              'the year, day within the part |')
        w(f'| permanent_summer | {", ".join(x.split(":")[1] for x in types["permanent_summer"]) or "-"} | rain, never '
          'snow | base, no offset | "Summer", day of the year |')
        w(f'| permanent_winter | {", ".join(x.split(":")[1] for x in types["permanent_winter"]) or "-"} | as frozen: '
          f'snow all year | at most {FROZEN_MAX} | "Winter", day of the year |')
        w(f'| adaptive | {len(types["adaptive"])} (rivers, beaches) | the nearest land biome\'s, per column | the land\'s '
          '| the land\'s |')
        w('| (untagged) | rest | Serene Seasons\' own rule (1.2) | | |\n')
        w(f'**Snow line** (CEConfig `[seasons] snowLineY` = {SNOW_LINE_MIN}, the build height is {MAX_Y}): below it, '
          'height alone never turns rain into snow. A column snows there only if its biome snows at y80 in the '
          'current sub-season (every dimension). At and above it, temperate and cold biomes always snow (overworld): '
          'no block can sit that high, so snow only tells a flyer they are above build height; their snow-line '
          f'column reads "y{SNOW_LINE_MIN}". Frozen and permanent-season biomes keep their own rules. World generation '
          'keeps vanilla snow caps, which melt in seasons warm enough to rain.\n')
        w('**Snow seasons** (`season_types.json` `snow_seasons`, tags `creativeengineering:snow/no_<sub_season>`): '
          'the sub-seasons a biome may snow in. In any other sub-season it rains instead, however cold the season '
          'makes it (below the snow line). Frozen biomes ignore it.\n')
        if SNOW_ALLOWED:
            w('| Biome | May snow in |')
            w('|---|---|')
            for bid in sorted(SNOW_ALLOWED):
                subs = sorted(SNOW_ALLOWED[bid])
                w(f'| `{bid}` | {" ".join(SUBS[x] for x in subs) or "never"} |')
            w('')

    # ── 2. problems
    w('## 2. Problems found\n')
    w('Computed from the tables below, for the pack server config (`configs_edited/server_config/'
      'sereneseasons-server.toml`) and the season types.\n')

    w('### 2.1 season_types.json\n')
    if not unknown and not twice:
        w('No unknown ids, no biome in two lists.\n')
    for k, b in unknown:
        w(f'- **unknown id** `{b}` in `{k}` (not in biome_data.json)')
    for b, ks in sorted(twice.items()):
        w(f'- **in two lists** `{b}`: {", ".join(ks)} (code takes arid, then frozen, then tropical)')
    snow_unknown = [bid for bid in snow_allowed if bid not in biomes]
    snow_idle = [bid for bid in snow_allowed if bid in biomes and (cls_of[bid] in ('arid', 'adaptive') + FROZEN_LIKE
                                                                  or cls_of[bid] in PERMANENT)]
    for bid in snow_unknown:
        w(f'- **unknown id** `{bid}` in `snow_seasons` (not in biome_data.json)')
    for bid, name in bad_snow:
        w(f'- **unknown season** `{name}` for `{bid}` in `snow_seasons` (early_spring ... late_winter, or spring, '
          'summer, autumn, winter)')
    if snow_idle:
        w(f'- In `snow_seasons` but no effect (frozen and permanent seasons keep their own rule, arid has none, '
          'adaptive follows its land): '
          f'{", ".join(f"`{b}`" for b in snow_idle)}.')
    land_ow = [b for b in ow if not b.ocean and cls_of[b.id] not in ('off', 'adaptive')]
    cold_untagged = [b for b in land_ow if cls_of[b.id] in ('ss-temperate', 'ss-tropical')
                     and (b.base < SNOW_BELOW or b.precip == 'SNOW')]
    dry_untagged = [b for b in land_ow if cls_of[b.id] in ('ss-temperate', 'ss-tropical') and b.precip == 'NONE']
    trop_none = [b for b in ow if cls_of[b.id] == 'tropical' and b.precip == 'NONE']
    hot_untagged = [b for b in land_ow if cls_of[b.id] == 'ss-tropical']
    edge_untagged = [b for b in ow if b.edge and cls_of[b.id] != 'adaptive']
    w(f'- Cold biomes (below 0.15 or declared snow) not tagged frozen: {", ".join(f"`{b.id}`" for b in cold_untagged) or "none"}.')
    w(f'- Biomes declaring no precipitation not tagged: {", ".join(f"`{b.id}`" for b in dry_untagged) or "none"}.')
    w(f'- Tagged tropical but declaring no precipitation (rain in Mid Wet only, like dry; move them to dry): '
      f'{", ".join(f"`{b.id}`" for b in trop_none) or "none"}.')
    w(f'- Rivers/beaches/shores not tagged adaptive: {", ".join(f"`{b.id}`" for b in edge_untagged) or "none"}.')
    oceans_off = [b for b in ow if b.ocean and cls_of[b.id] == 'off']
    w(f'- Oceans left blacklisted (vanilla weather): {", ".join(f"`{b.id}`" for b in oceans_off) or "none"}.')
    w(f'- Untagged biomes Serene Seasons makes tropical (on its list or warmer than 0.8): '
      f'{", ".join(f"`{b.id}` ({b.base:g})" for b in sorted(hot_untagged, key=lambda b: b.id)) or "none"}.\n')

    forced = [b for b in ow if any(cell(b, cls_of[b.id], s) == 'R!' for s in range(12))]
    w(f'### 2.2 Rain in biomes that declare none ({len(forced)})\n')
    w('Serene Seasons\' Mid Wet rain in a biome that declares no precipitation, outside the dry type.'
      + (' None left.\n' if not forced else '\n'))
    for b in sorted(forced, key=lambda b: -share(b)):
        w(f'- `{b.id}` base {b.base:g}, {pct(share(b))}')
    if forced:
        w('')

    w(f'### 2.3 Out of step with the land around it ({len(neighbours)} biomes)\n')
    w('Sub-seasons in which the biome snows while at least 75% of the land in the same temperature band does '
      'not (or rains while 75% snows). "Land" is every sampled surface biome that is not ocean, river or beach, '
      'dry ones counted as not snowing; the band is the climate parameter where the biome was actually sampled '
      '(`/dumpclimate`), judged band by band. Adaptive and arid biomes are left out. Peaks and slopes are cold by '
      'height as well, so snow there among rainy land is expected.\n')
    w('| Biome | Base | Class | Bands sampled | Out of step |')
    w('|---|---|---|---|---|')
    for bid, n, bands, per_band in sorted(neighbours, key=lambda r: (-r[1])):
        b = biomes[bid]
        parts = []
        for bd, rr in sorted(per_band.items()):
            by_verb = defaultdict(list)
            for s, v, x in rr:
                by_verb[v].append((s, x))
            for v, lst in by_verb.items():
                lo, hi = round(100 * min(x for _, x in lst)), round(100 * max(x for _, x in lst))
                parts.append(f'in {BANDS[bd]}: {v} {" ".join(SUBS[s] for s, _ in lst)} '
                             f'({lo if lo == hi else f"{lo}-{hi}"}% of {BANDS[bd]} land does not)')
        w(f'| `{bid}` | {b.base:g} | {cls_of[bid]} | {band_text(bands)} | {"; ".join(parts)} |')
    w('')

    snow_decl = [b for b in ow if b.precip == 'SNOW' and b.base >= SNOW_BELOW and cls_of[b.id] not in FROZEN_LIKE + ('adaptive',)]
    w(f'### 2.4 Declared snow, warm enough to rain ({len(snow_decl)})\n')
    w('Data quirk (the biome json): these draw rain at sea level whenever the temperature is 0.15 or more. '
      'Frozen-tagged biomes are held below 0.15, so they are not listed.\n')
    for b in snow_decl:
        cells = ' '.join(f'{SUBS[s]}:{cell(b, cls_of[b.id], s)}' for s in range(12))
        w(f'- `{b.id}` base {b.base:g}, {cls_of[b.id]}: {cells}')
    w('')

    dry_cold = [b for b in ow if any(cell(b, cls_of[b.id], s) == '-*' for s in range(12))]
    w(f'### 2.5 No precipitation, but snow layers settle ({len(dry_cold)})\n')
    w('Declared `none` and cold enough in some sub-season: no snow is drawn, yet snow layers build up during '
      'storms because `shouldSnow` ignores the declaration.\n')
    for b in dry_cold:
        subs = [SUBS[s] for s in range(12) if cell(b, cls_of[b.id], s) == '-*']
        w(f'- `{b.id}` base {b.base:g}, {cls_of[b.id]}: {", ".join(subs)}')
    w('')

    diff = [b for b in ow if cls_of[b.id] != cls_sp[b.id]]
    w(f'### 2.6 Single-player worlds differ ({len(diff)} biomes change class)\n')
    w('The pack ships its Serene Seasons server config only to the dedicated server (`configs_edited/'
      'server_config/`); there is no `defaultconfigs/sereneseasons-server.toml`, so a new single-player world '
      'writes Serene Seasons\' own defaults into `saves/<world>/serverconfig/`. The season tags apply on both, so '
      'only blacklisting and untagged biomes can differ.\n')
    w('| Biome | Base | Server | Single-player |')
    w('|---|---|---|---|')
    for b in sorted(diff, key=lambda b: b.id):
        w(f'| `{b.id}` | {b.base:g} | {cls_of[b.id]} | {cls_sp[b.id]} |')
    w('')

    changed = [b for b in ow if any(cell(b, cls_raw[b.id], s, False) != cell(b, cls_of[b.id], s) for s in range(12))]
    w(f'### 2.7 What the season types change ({len(changed)} biomes)\n')
    w('Cells with Serene Seasons alone (server config) against cells with the season types and snow seasons. '
      'Adaptive biomes show `~` (they take the land\'s row).\n')
    w('| Biome | Before | After |')
    w('|---|---|---|')
    for b in sorted(changed, key=lambda b: (cls_of[b.id], b.id)):
        before = ''.join(cell(b, cls_raw[b.id], s, False).replace('*', '\\*').ljust(2) for s in range(12)).replace(' ', '.')
        after = ''.join(cell(b, cls_of[b.id], s).replace('*', '\\*').ljust(2) for s in range(12)).replace(' ', '.')
        w(f'| `{b.id}` ({cls_raw[b.id]} -> {cls_of[b.id]}) | `{before}` | `{after}` |')
    w('\nColumns run ESp to LWi, two characters each.\n')

    shoulder = [b for b in ow if cls_of[b.id] not in FROZEN_LIKE + ('adaptive',)
                and any(cell(b, cls_of[b.id], x) in ('S', '-*') for x in range(12) if x not in WINTER)]
    w(f'### 2.8 Snow outside winter ({len(shoulder)} biomes)\n')
    w('Biomes (not frozen) that snow at sea level in any sub-season outside Early, Mid and Late Winter, with the '
      'snow seasons applied. To shorten a biome\'s snow, list it in `season_types.json` `snow_seasons` (for example '
      '`["winter"]`, or `["late_autumn", "winter"]`). Sorted by base temperature, warmest first.\n')
    w('| Biome | Class | Base | Share | Snows outside winter |')
    w('|---|---|---|---|---|')
    for b in sorted(shoulder, key=lambda b: (-b.base, b.id)):
        subs = [SUBS[x] for x in range(12) if x not in WINTER and cell(b, cls_of[b.id], x) in ('S', '-*')]
        w(f'| `{b.id}` | {cls_of[b.id]} | {b.base:g} | {pct(share(b))} | {" ".join(subs)} |')
    w('')

    # ── 3. overworld table
    w('## 3. Overworld, per biome (server config)\n')
    w('Cells: what falls during a storm (the world is raining), at sea level (y80 or below). **R** rain, **S** snow, '
      '**-** nothing, **R!** Serene Seasons\' Mid Wet rain in a biome that declares none, **-\\*** nothing falls but '
      'snow layers settle, **~** adaptive. Above y80 it gets colder: see the snow line columns (lowest y where it '
      'snows; first the warmest sub-season, then the coldest). Share: post-tuning surface share of the overworld. '
      'Bands: the climate temperature band the biome was sampled in, percent of its samples '
      '(frozen/cold/temperate/warm/hot are vanilla\'s five).\n')
    land = band_land(biomes, samples)
    n = 0
    for cls_name, title in CLASS_TITLES:
        group = sorted((b for b in ow if cls_of[b.id] == cls_name), key=lambda b: (-b.base, b.id))
        if not group:
            continue
        n += 1
        w(f'### 3.{n} {title} ({len(group)}, {pct(sum(share(b) for b in group))})\n')
        if cls_name == 'adaptive':
            w('Each column takes the nearest land biome\'s type and temperature (rings of 8 directions out to CEConfig '
              '`[seasons] adaptiveRadius`). "Land in its bands" is what the climate samples put around it: the '
              'most common land classes in the bands where the biome was sampled.\n')
            w('| Biome | Base | Bands | Share | Land in its bands |')
            w('|---|---|---|---|---|')
            for b in group:
                ctr = Counter()
                for bd, c in bands_of.get(b.id, Counter()).items():
                    tot = sum(land[bd].values()) or 1
                    for lb, lc in land[bd].items():
                        ctr[cls_of[lb]] += c * lc / tot
                tot = sum(ctr.values()) or 1
                mix = ', '.join(f'{k} {round(100 * v / tot)}%' for k, v in ctr.most_common() if v / tot >= 0.05)
                w(f'| `{b.id}` | {b.base:g} | {band_text(bands_of.get(b.id, Counter()))} | {pct(share(b))} | {mix or "-"} |')
            w('')
            continue
        w('| Biome | Base | Mod | Declared | Bands | Share | Snow line | ' + ' | '.join(SUBS) + ' |')
        w('|---|---|---|---|---|---|---|' + '---|' * 12)
        for b in group:
            cells = [cell(b, cls_name, s) for s in range(12)]
            gauge = cls_name in SEASONAL
            lines = [snow_line_text(snow_line_y(sea_level_temp(b, cls_name, s), snow_blocked(b, cls_name, s), gauge))
                     for s in range(12)]
            hi = max(lines, key=lambda x: {'all': -1, 'never': 1e9}.get(x, int(x[1:]) if x[1:].isdigit() else 0))
            lo = min(lines, key=lambda x: {'all': -1, 'never': 1e9}.get(x, int(x[1:]) if x[1:].isdigit() else 0))
            sl = (f'{hi} / {lo}' if hi != lo else hi) if b.precip != 'NONE' and cls_name != 'arid' else '-'
            w(f'| `{b.id}` | {b.base:g} | {b.modifier if b.modifier != "none" else ""} | {b.precip.lower()} | '
              f'{band_text(bands_of.get(b.id, Counter()))} | {pct(share(b))} | {sl} | '
              + ' | '.join(c.replace('*', '\\*') for c in cells) + ' |')
        w('')

    # ── 4. other dimensions
    others = sorted((b for b in biomes.values() if b.dims and 'minecraft:overworld' not in b.dims),
                    key=lambda b: (b.dims[0], b.id))
    w('## 4. Other dimensions (no seasons, vanilla rules)\n')
    w('Overworld biomes reused in the pack\'s own dimensions (`creativeengineering:*`) are in section 3; they '
      'follow vanilla rules there, since only the overworld is whitelisted (the season tags do nothing outside it).\n')
    w('| Biome | Dimensions | Base | Declared | At sea level | Snow line |')
    w('|---|---|---|---|---|---|')
    for b in others:
        c = cell(b, 'off', 0)
        what = {'R': 'rain', 'S': 'snow', '-': 'nothing', '-*': 'nothing (snow layers settle)'}[c]
        w(f'| `{b.id}` | {", ".join(b.dims)} | {b.base:g} | {b.precip.lower()} | {what} | '
          f'{snow_line(b.base) if b.precip != "NONE" else "-"} |')
    reused = sorted({d for b in ow for d in b.dims if d != 'minecraft:overworld'})
    w('')
    w('Overworld biomes also placed in: ' + ', '.join(f'`{d}`' for d in reused) + '.\n')

    # ── 5. calendar and worlds
    w('## 5. Calendar and local worlds\n')
    bd = BETTER_DAYS.read_text() if BETTER_DAYS.exists() else ''

    def hours(ticks):
        return ticks / 20 / 3600

    w('| | Server (pack config) | Single-player default |')
    w('|---|---|---|')
    w(f'| `day_duration` (ticks per season day) | {pack["day_duration"]} | {SS_DEFAULT["day_duration"]} |')
    w(f'| `sub_season_duration` (days) | {pack["sub_season_duration"]} | {SS_DEFAULT["sub_season_duration"]} |')
    sub_pack = pack['day_duration'] * pack['sub_season_duration']
    sub_sp = SS_DEFAULT['day_duration'] * SS_DEFAULT['sub_season_duration']
    w(f'| one sub-season, real time | {hours(sub_pack):.1f} h | {hours(sub_sp):.1f} h |')
    w(f'| wet season (Early + Mid + Late Wet, 6 sub-seasons) | {hours(6 * sub_pack):.0f} h | {hours(6 * sub_sp):.0f} h |')
    w(f'| whole year (arid/frozen HUD counts to) | {hours(12 * sub_pack):.0f} h, '
      f'{12 * pack["sub_season_duration"]} days | {hours(12 * sub_sp):.0f} h, {12 * SS_DEFAULT["sub_season_duration"]} days |')
    w(f'| starting sub-season | {SUB_NAMES[pack["starting_sub_season"] - 1]} | '
      f'{SUB_NAMES[SS_DEFAULT["starting_sub_season"] - 1]} |\n')
    ds, ns = toml_value(bd, 'daySpeed'), toml_value(bd, 'nightSpeed')
    d0, n0 = toml_value(bd, 'dayStart'), toml_value(bd, 'nightStart')
    if None not in (ds, ns, d0, n0):
        day_part = (n0 - d0) % 24000
        real = day_part / ds + (24000 - day_part) / ns
        w(f'Serene Seasons counts server ticks (`SeasonHandler`, only while a player is online when '
          '`progress_season_while_offline` is false), plus any jump in day time of more than one tick '
          '(`TimeSkipHandler`: sleeping, `/time add`). Better Days (`daySpeed` '
          f'{ds}, `nightSpeed` {ns}) makes one visible day {real:.0f} ticks ({real / 1200:.0f} min), so a season day '
          f'is {pack["day_duration"] / real:.2f} visible days on the server and '
          f'{SS_DEFAULT["day_duration"] / real:.2f} in single-player. Its slow daytime adds nothing extra; its '
          'sleep speed-up does.\n')
    w('Both configs start a new world in Early Spring, which is tropical Mid Wet: savannas and jungles get rain '
      'from the first storm, and temperate biomes are 0.4 colder until Mid Spring.\n')
    w('**How often it rains.** Rain is world-wide: a biome whose season allows rain shows it whenever the world '
      'is raining. Vanilla (`ServerLevel.advanceWeatherCycle`) draws a storm of 12000-24000 ticks and a dry spell '
      'of 12000-180000; Serene Seasons (`change_weather_frequency`) cuts the dry spell by temperate season. '
      'Tropical seasons map onto those: the wet season is Winter (Early Wet, Mid Wet\'s Late Winter) plus Spring '
      '(Mid Wet\'s Early Spring, Late Wet).\n')
    w('| Temperate season | Dry spell (ticks) | Mean storm + dry spell | Share of time raining | Storms per sub-season '
      '(server / single-player) |')
    w('|---|---|---|---|---|')
    for name, lo, hi in (('Winter', 12000, 36000), ('Spring', 12000, 96000), ('Summer, Autumn', 12000, 180000)):
        cyc = 18000 + (lo + hi) / 2
        w(f'| {name} | {lo}-{hi} | {cyc / 1200:.0f} min | {100 * 18000 / cyc:.0f}% | '
          f'{sub_pack / cyc:.0f} / {sub_sp / cyc:.1f} |')
    w('\nSleeping skips weather (Better Days `weatherEffect = SLEEPING` runs the weather clock at the sleep speed).\n')
    w('| World | Config | Ticks | Now | Tropical | Day of sub-season |')
    w('|---|---|---|---|---|---|')
    for wdir in sorted(p for p in SAVES.iterdir() if p.is_dir()):
        dat, conf = wdir / 'data/seasons.dat', wdir / 'serverconfig/sereneseasons-server.toml'
        if not dat.exists():
            continue
        ticks = season_ticks(dat)
        cfg = load_ss(conf) if conf.exists() else SS_DEFAULT
        which = 'pack' if cfg['blacklisted_biomes'] == pack['blacklisted_biomes'] else (
            'Serene Seasons default' if cfg['blacklisted_biomes'] == SS_DEFAULT['blacklisted_biomes'] else 'custom')
        if ticks is None:
            w(f'| {wdir.name} | {which} | ? | | | |')
            continue
        sub_len = cfg['day_duration'] * cfg['sub_season_duration']
        s = (ticks // sub_len) % 12
        day = (ticks % sub_len) // cfg['day_duration'] + 1
        w(f'| {wdir.name} | {which} | {ticks} | {SUB_NAMES[s]} | {TROP_NAMES[TROP[s]]} | '
          f'{day} of {cfg["sub_season_duration"]} |')
    w('\nThe dedicated server\'s season lives on the server (`<world>/data/seasons.dat`) and is not read here.\n')

    OUT_MD.write_text('\n'.join(L))
    print(f'wrote {OUT_MD.relative_to(INSTANCE)}: {len(ow)} overworld biomes, {len(others)} others; '
          f'{len(forced)} with forced rain, {len(neighbours)} out of step with their band, {len(changed)} changed by the season types')
    for k, b in unknown:
        print(f'  ! unknown id {b} in {k}')
    for b, ks in twice.items():
        print(f'  ! {b} in two lists: {", ".join(ks)}')
    for bid in snow_unknown:
        print(f'  ! unknown id {bid} in snow_seasons')
    for bid, name in bad_snow:
        print(f'  ! unknown season {name} for {bid} in snow_seasons')
    return 1 if unknown or twice or snow_unknown or bad_snow else 0


if __name__ == '__main__':
    sys.exit(main())
