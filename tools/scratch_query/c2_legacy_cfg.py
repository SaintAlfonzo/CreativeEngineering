#!/usr/bin/env python3
"""Puts a scratch server's config/ into a pre-split state for the config carry rows of r1113 chunk 2. Run on a built,
never-booted srv (prep_srv.sh done, server stopped). Tables are cut from the srv's own current files, so keys and
comments match the jars; only the values the rows check are changed.

    c2_legacy_cfg.py SRV l1    SQ-CMG-02, SQ-CEN-02, SQ-CFR-04 (+ SQ-CEN-05 shrapnel): a pack that skipped 1.2.00.
        config/creativeengineering-common.toml = patcherMod's old file: [botania] (trinketCaseAnyItem = false), the nine
        Equipment tables ([shields] wearThreshold = 4.5), [shrapnel] (count = 12), [surface] ([surface.wthit]
        format = "PERCENT"), [vein_undo], [jei] [gui_title] [toasts] (tutorial = true) [tooltips] [coaster] [wthit],
        Compat's five first-release tables, Common's six tables, [campfire] (rainOutTicks = 120).
        config/creativeengineering-swaps.toml = the current creativecommon-swaps.toml.
        Deleted: creativecommon-common.toml, creativecommon-swaps.toml, creativemagic-common.toml,
        creativeequipment-common.toml, creativecompat-common.toml. creativedelight-common.toml kept without [campfire].
    c2_legacy_cfg.py SRV l2    SQ-CFR-03: no creativedelight-common.toml; creativecompat-common.toml with
        [campfire] rainOutTicks = 100; creativeengineering-common.toml holding only Creative Delight's seven carve
        tables ([crop_growth] ... [perf], from the current CD file).
Prints what it wrote. Backups of every replaced file go to SRV/config/c2_legacy_backup/.
"""
import os
import re
import shutil
import sys

EQUIP = ['dragon_gear', 'air_brake', 'stepstool', 'crafting', 'armor_enchantability', 'shields', 'bonemeal_charm',
         'bottled_air', 'backpackSkins']
COMPAT_ENG = ['shrapnel', 'surface', 'vein_undo']
COMPAT_COMMON = ['jei', 'gui_title', 'toasts', 'tooltips', 'coaster', 'wthit']
COMPAT_FIRST = ['leaf_piles', 'moss', 'vine_shearing', 'monster_box', 'trash_slot']
COMMON = ['changelog', 'damage_tags', 'perf', 'wandering_trader', 'advancements', 'mod_names']
CD_MOVED = ['crop_growth', 'dandelion_maturing', 'crop_drops', 'stem_fruit', 'canning', 'cooking', 'perf']


def tables(text):
    """top-level table name -> its text (header, keys, sub-tables), in file order"""
    out, cur, buf = {}, None, []
    for line in text.splitlines(keepends=True):
        m = re.match(r'\s*\[([^\]]+)\]\s*$', line)
        if m and not line.lstrip().startswith('[['):
            top = m.group(1).split('.')[0].strip('"')
            if top != cur:
                if cur is not None:
                    out[cur] = ''.join(buf)
                cur, buf = top, []
        if cur is not None:
            buf.append(line)
    if cur is not None:
        out[cur] = ''.join(buf)
    return out


def setkey(text, key, value, count=1):
    new, n = re.subn(r'(?m)^(\s*' + re.escape(key) + r'\s*=\s*).*$', lambda m: m.group(1) + value, text, count=count)
    if n == 0:
        raise SystemExit('key %s not found' % key)
    return new


def read(cfg, name):
    with open(os.path.join(cfg, name)) as f:
        return f.read()


def pick(t, names, src):
    missing = [n for n in names if n not in t]
    if missing:
        raise SystemExit('%s lacks tables %s' % (src, missing))
    return ''.join(t[n] for n in names)


def main(srv, mode):
    cfg = os.path.join(srv, 'config')
    bak = os.path.join(cfg, 'c2_legacy_backup')
    os.makedirs(bak, exist_ok=True)
    for f in os.listdir(cfg):
        if f.endswith('.toml'):
            shutil.copy2(os.path.join(cfg, f), bak)
    cd = tables(read(cfg, 'creativedelight-common.toml'))
    if mode == 'l1':
        mg = tables(read(cfg, 'creativemagic-common.toml'))
        eq = tables(read(cfg, 'creativeequipment-common.toml'))
        cp = tables(read(cfg, 'creativecompat-common.toml'))
        cm = tables(read(cfg, 'creativecommon-common.toml'))
        parts = [setkey(mg['botania'], 'trinketCaseAnyItem', 'false')]
        e = pick(eq, EQUIP, 'equipment')
        parts.append(setkey(e, 'wearThreshold', '4.5'))
        c = pick(cp, COMPAT_ENG + COMPAT_COMMON + COMPAT_FIRST, 'compat')
        c = setkey(c, 'count', '12')
        c = setkey(c, 'format', '"PERCENT"')
        c = setkey(c, 'tutorial', 'true')
        parts.append(c)
        parts.append(pick(cm, COMMON, 'common'))
        parts.append(setkey(cd['campfire'], 'rainOutTicks', '120'))
        with open(os.path.join(cfg, 'creativeengineering-common.toml'), 'w') as f:
            f.write('# r1113 c2 legacy patcherMod file (c2_legacy_cfg.py l1)\n' + ''.join(parts))
        shutil.copy2(os.path.join(cfg, 'creativecommon-swaps.toml'), os.path.join(cfg, 'creativeengineering-swaps.toml'))
        for f in ['creativecommon-common.toml', 'creativecommon-swaps.toml', 'creativemagic-common.toml',
                  'creativeequipment-common.toml', 'creativecompat-common.toml']:
            os.remove(os.path.join(cfg, f))
        with open(os.path.join(cfg, 'creativedelight-common.toml'), 'w') as f:
            f.write(''.join(v for k, v in cd.items() if k != 'campfire'))
        print('l1: wrote creativeengineering-common.toml (%d tables), creativeengineering-swaps.toml; removed Common, '
              'Common swaps, Magic, Equipment, Compat files; CD file without [campfire]' % (1 + len(EQUIP) + 14 + len(COMMON) + 1))
    elif mode == 'l2':
        cpt = read(cfg, 'creativecompat-common.toml')
        camp = setkey(cd['campfire'], 'rainOutTicks', '100')
        with open(os.path.join(cfg, 'creativecompat-common.toml'), 'w') as f:
            f.write(cpt.rstrip('\n') + '\n\n' + camp)
        with open(os.path.join(cfg, 'creativeengineering-common.toml'), 'w') as f:
            f.write('# r1113 c2 legacy patcherMod file (c2_legacy_cfg.py l2)\n' + pick(cd, CD_MOVED, 'delight'))
        os.remove(os.path.join(cfg, 'creativedelight-common.toml'))
        print('l2: compat file + [campfire] rainOutTicks = 100; creativeengineering-common.toml with the CD carve tables; '
              'creativedelight-common.toml removed')
    else:
        raise SystemExit('mode l1 or l2')


if __name__ == '__main__':
    if len(sys.argv) != 3:
        print(__doc__)
        sys.exit(2)
    main(sys.argv[1], sys.argv[2])
