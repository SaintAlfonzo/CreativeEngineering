"""World-interaction tabs for tools used on placed blocks: stripping, waxing and scraping, block dyeing, soap washing.

Source: kubejs/exported/world_jei/tool_pairs.json (patcherMod devtools/WorldJeiToolPairs.java, run /dumpworldjei).
Hand-written rows (Create sail / seat / valve handle dyeing, Supplementaries soap on globes, spikes, signs, mobs,
the blackboard) are in kubejs/assets/creativecommon/jei_world/<tab>/manual.json.

Mod behaviour this follows, read from the jars:
- Immersive Weathering (ModEvents): an axe strip also pops 1 bark of the log's wood type ([item_interaction]
  axe_stripping). An axe on unaffected or exposed iron only scrapes when the rust level is weathered or rusted (the
  click is eaten otherwise); steel wool scrapes any rust stage, takes wax off its waxed blocks and scrapes one stage off
  the copper in #immersive_weathering:copper. Both need axe_rusting.
- Supplementaries: dye on a block of a Moonlight colour set swaps it for that colour and uses one dye ([tweaks]
  dye_blocks); patcherMod's SupplementariesDyeBlocksMixin stops it on blocks with a block entity and
  #creativeengineering:no_world_dye. Soap (SoapWashableHelper) first uses a block's own wash, then turns a coloured
  block back to its plain one (not beds, not [functional.soap] clean_blacklist), then a sticky piston into a piston,
  then strips wax and all oxidation at once. Each wash uses one soap.
- Create sails, seats and valve handles dye through their own code (manual rows), so their colour sets are left out of
  the generated dye rows.
"""
import re
from collections import OrderedDict

from .common import ROOT, block, blocks, item, mod_of, row, tag

AXE = tag('forge:tools/axes')
HONEYCOMB = item('minecraft:honeycomb')
SOAP = item('supplementaries:soap')
STEEL_WOOL = item('immersive_weathering:steel_wool')
COLORS = ['white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray', 'light_gray', 'cyan',
          'purple', 'blue', 'brown', 'green', 'red', 'black']
# Create dyes these itself (manual.json); Moonlight may also list them as colour sets.
CREATE_SELF_DYED = re.compile(r'^create:([a-z_]+_)?(sail|seat|valve_handle)$|^create:sail_frame$')


def _toml_bool(path, section, key, default=True):
    """`key = true/false` under `[section]` (dotted, as written) in a pack config; default when not found."""
    p = ROOT / 'config' / path
    if not p.exists():
        return default
    current = None
    for line in p.read_text(encoding='utf-8').splitlines():
        s = line.strip()
        if s.startswith('['):
            current = s.strip('[]').strip()
        elif current == section and re.match(rf'{re.escape(key)}\s*=', s):
            return s.split('=', 1)[1].strip().lower() == 'true'
    return default


def _requires(*mods):
    return [m for m in mods if m != 'minecraft']


def _group_by_target(pairs):
    """{to: [from, ...]} keeping first-seen order."""
    out = OrderedDict()
    for p in pairs:
        lst = out.setdefault(p['to'], [])
        if p['from'] not in lst:
            lst.append(p['from'])
    return out


def _stripping(dump, iw_strip_bark):
    rows = []
    for p in dump.get('strip', []):
        outs = [block(p['to'])]
        notes = ['Right-click the placed block.']
        req = _requires(mod_of(p['from']))
        if iw_strip_bark and p.get('bark'):
            outs.append(item(p['bark']))
            notes.append('Also drops 1 bark.')
            req.append('immersive_weathering')
        rows.append(row([block(p['from'])], outs, action=AXE, notes=notes, requires=req))
    return rows


def _waxing(dump, tags, iw_scraping):
    rows = []
    honey = dump.get('honeycomb', [])
    for p in honey:
        rows.append(row([block(p['from'])], [block(p['to'])], action=HONEYCOMB,
                        notes=['Right-click the placed block.', 'Stops weathering.'],
                        requires=_requires(mod_of(p['from']))))

    wax_off = {p['from']: p['to'] for p in dump.get('wax_off', [])}
    for p in honey:  # vanilla's axe wax-off is the honeycomb table reversed
        wax_off.setdefault(p['to'], p['from'])
    for frm, to in wax_off.items():
        rows.append(row([block(frm)], [block(to)], action=AXE,
                        notes=['Right-click: scrapes the wax off.'], requires=_requires(mod_of(frm))))

    iw_copper = set(tags.get('immersive_weathering:copper', []))
    iw_waxed = set(tags.get('immersive_weathering:waxed_blocks', []))
    wool_rows = []
    for p in dump.get('scrape', []):
        iron = mod_of(p['from']) == 'immersive_weathering' and p.get('age') in ('exposed', 'weathered', 'rusted')
        if iron:
            if p['age'] != 'exposed' and iw_scraping:
                rows.append(row([block(p['from'])], [block(p['to'])], action=AXE,
                                notes=['Right-click: scrapes off one rust stage.',
                                       'Exposed iron needs steel wool.'],
                                requires=['immersive_weathering']))
            if iw_scraping:
                wool_rows.append(row([block(p['from'])], [block(p['to'])], action=STEEL_WOOL,
                                     notes=['Right-click: scrapes off one rust stage.'],
                                     requires=['immersive_weathering']))
            continue
        rows.append(row([block(p['from'])], [block(p['to'])], action=AXE,
                        notes=['Right-click: scrapes off one oxidation stage.'],
                        requires=_requires(mod_of(p['from']))))
        if iw_scraping and p['from'] in iw_copper:
            wool_rows.append(row([block(p['from'])], [block(p['to'])], action=STEEL_WOOL,
                                 notes=['Right-click: scrapes off one oxidation stage.'],
                                 requires=['immersive_weathering']))
    if iw_scraping:
        for frm, to in wax_off.items():
            if frm in iw_waxed:
                wool_rows.append(row([block(frm)], [block(to)], action=STEEL_WOOL,
                                     notes=['Right-click: takes the wax off.'],
                                     requires=['immersive_weathering']))
    return rows + wool_rows


def _dyeing(dump):
    rows = []
    blocked = set(dump.get('dye_blocked', []))
    for key, s in sorted(dump.get('color_sets', {}).items()):
        colors = s.get('colors', {})
        members = [m for m in s.get('members', []) if m not in blocked]
        if any(CREATE_SELF_DYED.match(m) for m in members + list(colors.values())):
            continue
        for color in COLORS:
            target = colors.get(color)
            if not target or target in blocked:
                continue
            sources = [m for m in members if m != target]
            if not sources:
                continue
            rows.append(row([blocks(sources, label='Any other colour')], [block(target)],
                            action=tag(f'forge:dyes/{color}'),
                            notes=['Right-click the placed block.', 'Uses one dye.'],
                            requires=['supplementaries']))
    return rows


def _soaping(dump):
    rows = []
    by_how = OrderedDict()
    for p in dump.get('soap', []):
        by_how.setdefault(p.get('how', 'color'), []).append(p)
    notes = {
        'color': ['Right-click: washes the dye out.', 'Uses one soap.'],
        'piston': ['Right-click: washes the slime off.', 'Uses one soap.'],
        'unoxidise': ['Right-click: takes off wax and all oxidation.', 'Uses one soap.'],
        'name': ['Right-click: takes off wax and all weathering.', 'Uses one soap.'],
    }
    for how, pairs in by_how.items():
        for to, froms in _group_by_target(pairs).items():
            rows.append(row([blocks(froms)], [block(to)], action=SOAP, notes=notes.get(how, notes['color']),
                            requires=['supplementaries']))
    return rows


def build(ctx):
    dump = ctx.exported('world_jei/tool_pairs.json')
    if dump is None:
        ctx.warn('tab_tools: no tool_pairs dump, run /dumpworldjei; only manual.json rows will show')
        return {}
    if dump.get('_fixture'):
        ctx.warn('tab_tools: kubejs/exported/world_jei/tool_pairs.json is a hand-made fixture, run /dumpworldjei')
    tags = ctx.exported('tags/block_tags.json') or {}
    cfg = dump.get('config', {})
    iw_strip_bark = cfg.get('iw_axe_stripping', _toml_bool('immersive_weathering-common.toml', 'item_interaction', 'axe_stripping'))
    iw_scraping = cfg.get('iw_axe_scraping', _toml_bool('immersive_weathering-common.toml', 'item_interaction', 'axe_rusting'))
    dye_on = cfg.get('dye_blocks', _toml_bool('supplementaries-common.toml', 'tweaks.dye_blocks', 'enabled'))
    soap_on = cfg.get('soap', _toml_bool('supplementaries-common.toml', 'functional.soap', 'enabled'))

    return {
        'stripping': _stripping(dump, iw_strip_bark),
        'waxing': _waxing(dump, tags, iw_scraping),
        'dyeing': _dyeing(dump) if dye_on else [],
        'soaping': _soaping(dump) if soap_on else [],
    }
