#!/usr/bin/env python3
# Pack jam line resources for the Vintage port (CreativeEngineering2, TODO dishes/jams #1, #8). Own work, GPLv3.
#
# Writes pack-owned files into src/own/ (tools/port_resources.py lays them over its output, so a rerun keeps them),
# from the owner's art in kubejs/assets/mason_jar_new/ (new_jar, new_mason_jar_interior, new_jam_block,
# new_jello_block, new_jello_bowl; tools/gen_jam_art.py makes them from colour samples). Java side: src/main/java/net/ribs/vintagedelight/jam/.
#
#   python3 development/creativeDelight/members/vintage/tools/gen_pack_jams.py
# then python3 development/creativeDelight/members/vintage/tools/port_resources.py, then build :creativeDelight:.
#
# Per pack jam (PackJams.JAMS): jar art (item + interior), jam block, jello block, bowl of jello, three cauldron stages
# (fruit, jam, jello), soft fluid for Supplementaries jars (1 jar = 1 bottle), lang. New jars (ancient fruit, banana,
# mulberry, strawberry, nut milk) also get blockstate, models and loot. Fruits Delight's 22 jams get a placed jar block
# each (fd_<fruit>_jam_jar). The older Vintage jars (apple sauce, nut mash, relish, vinegar, pepper jam) get the new art.
import json
import os
import shutil

from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
MOD = os.path.dirname(HERE)
INSTANCE = os.path.abspath(os.path.join(MOD, '../../../..'))
ART = os.path.join(INSTANCE, 'kubejs/assets/mason_jar_new')
OWN = os.path.join(MOD, 'src/own')
NS = 'vintagedelight'

# name, English stem, art stem, new jar registered by PackJams, own jam fluid
JAMS = [
    ('ancient_fruit', 'Ancient Fruit', 'ancient_fruit', True, True),
    ('banana', 'Banana', 'banana', True, True),
    ('cherry', 'Cherry', 'cherry', False, True),
    ('green_grape', 'Green Grape', 'green_grape', False, True),
    ('mulberry', 'Mulberry', 'mulberry', True, False),
    ('pepper', 'Pepper', 'pepper', False, True),
    ('raspberry', 'Raspberry', 'raspberry', False, True),
    ('red_grape', 'Red Grape', 'red_grape', False, True),
    ('salmonberry', 'Salmonberry', 'salmonberry', False, True),
    ('strawberry', 'Strawberry', 'strawberries', True, True),
]
FD_JAMS = ['apple', 'bayberry', 'blueberry', 'chorus', 'cranberry', 'durian', 'fig', 'glowberry', 'hamimelon', 'hawberry',
           'kiwi', 'lemon', 'lychee', 'mango', 'mangosteen', 'melon', 'orange', 'peach', 'pear', 'persimmon', 'pineapple',
           'sweetberry']
FD_NAMES = {'glowberry': 'Glow Berry', 'sweetberry': 'Sweet Berry', 'hamimelon': 'Hami Melon'}
# Older Vintage jars: item texture, interior texture, art file in new_jar, art file in new_mason_jar_interior
RETEXTURE = [
    ('apple_sauce_jar', 'apple_sauce_interior_texture', 'apple_sauce_jar', 'apple_sauce_jar_interior_texture'),
    ('nut_mash_jar', 'nut_mash_interior_texture', 'nut_mash_jar', 'nut_mash_jar_interior_texture'),
    ('relish_jar', 'relish_interior_texture', 'relish_jar', 'relish_jar_interior_texture'),
    ('vinegar_jar', 'vinegar_interior_texture', 'vinegar_jar', 'vinegar_jar_interior_texture'),
    ('pepper_jam_jar', 'pepper_jam_interior_texture', 'pepper_jam', 'pepper_jam_interior_texture'),
    ('honey_jar', 'honey_interior_texture', 'honey_jar', 'honey_jar_interior_texture'),
]
COUNTS = ['one', 'two', 'three', 'four']
lang = {}


def write(rel, data):
    path = os.path.join(OWN, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2)
        f.write('\n')


def art(sub, name, rel):
    src = os.path.join(ART, sub, name + '.png')
    if not os.path.exists(src):
        raise SystemExit('missing art: ' + src)
    dst = os.path.join(OWN, 'assets', NS, 'textures', rel + '.png')
    os.makedirs(os.path.dirname(dst), exist_ok=True)
    shutil.copyfile(src, dst)
    return src


def colour(png):
    """Mean colour of the opaque pixels, as #rrggbb (soft fluid colour)."""
    im = Image.open(png).convert('RGBA')
    px = [p for p in im.getdata() if p[3] > 128]
    r, g, b = (sum(p[i] for p in px) // len(px) for i in range(3))
    return '#%02x%02x%02x' % (r, g, b), (r << 16) | (g << 8) | b


def jar_block(block, interior, drop, drop_block=None):
    """Blockstate, the four jar-count models and the loot table of a mason jar block (Vintage's layout)."""
    variants = {}
    for n, word in enumerate(COUNTS, 1):
        for facing, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
            v = {'model': f'{NS}:block/{block}_{word}_jar'}
            if y:
                v['y'] = y
            variants[f'jars={n},facing={facing}'] = v
        write(f'assets/{NS}/models/block/{block}_{word}_jar.json',
              {'parent': f'{NS}:block/template_mason_jar_{word}', 'textures': {'2': f'{NS}:block/{interior}'}})
    write(f'assets/{NS}/blockstates/{block}.json', {'variants': variants})
    funcs = [{'function': 'set_count', 'count': n, 'conditions': [{
        'condition': 'minecraft:block_state_property', 'block': f'{NS}:{block}', 'properties': {'jars': str(n)}}]}
        for n in range(1, 5)]
    write(f'data/{NS}/loot_tables/blocks/{block}.json', {'type': 'minecraft:block', 'pools': [{
        'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': drop, 'functions': funcs}],
        'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})


def soft_fluid(name, jar, colour_hex, food_item, translation, fluid=None):
    # fluid: the line's own Forge fluid. Named as an equivalent fluid so Moonlight does not auto-create a second
    # soft fluid with the same id (a duplicate key that dropped the jar container, SQ-VP-06)
    data = {
        'color': colour_hex,
        'containers': [{'filled': [jar], 'empty': f'{NS}:empty_mason_jar', 'capacity': 1}],
        'flowing_texture': 'moonlight:soft_fluids/potion_flow',
        'food': {'divider': 1, 'item': food_item},
        'from_mod': NS,
        'still_texture': 'moonlight:soft_fluids/soup_liquid',
        'translation_key': translation,
    }
    if fluid:
        data['equivalent_fluids'] = [fluid]
    write(f'data/{NS}/moonlight/soft_fluids/{name}.json', data)


def simple_block(block, texture, english, render='minecraft:translucent'):
    write(f'assets/{NS}/blockstates/{block}.json', {'variants': {'': {'model': f'{NS}:block/{block}'}}})
    write(f'assets/{NS}/models/block/{block}.json',
          {'parent': 'minecraft:block/cube_all', 'render_type': render, 'textures': {'all': f'{NS}:block/{texture}'}})
    write(f'assets/{NS}/models/item/{block}.json', {'parent': f'{NS}:block/{block}'})
    write(f'data/{NS}/loot_tables/blocks/{block}.json', {'type': 'minecraft:block', 'pools': [{
        'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': f'{NS}:{block}'}],
        'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})
    lang[f'block.{NS}.{block}'] = english


GUI = {'gui': {'rotation': [30, 225, 0], 'scale': [0.625, 0.625, 0.625]}}


def cauldron_model(model, parent, content, tinted=False):
    write(f'assets/{NS}/models/block/{model}.json', {
        'parent': f'minecraft:block/{parent}', 'display': GUI, 'gui_light': 'side',
        'textures': {'bottom': 'minecraft:block/cauldron_bottom', 'content': content,
                     'inside': 'minecraft:block/cauldron_inner', 'particle': 'minecraft:block/cauldron_side',
                     'side': 'minecraft:block/cauldron_side', 'top': 'minecraft:block/cauldron_top'}})


def cauldron_loot(block):
    write(f'data/{NS}/loot_tables/blocks/{block}.json', {'type': 'minecraft:block', 'pools': [{
        'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': 'minecraft:cauldron'}],
        'conditions': [{'condition': 'minecraft:survives_explosion'}]}]})


def main():
    colours = {}
    new_jars, jello_blocks, cauldrons = [], [], []
    for name, english, stem, new_jar, own_fluid in JAMS:
        jar = f'{name}_jam_jar'
        art('new_jar', f'{stem}_jam', f'item/{jar}')
        art('new_mason_jar_interior', f'{stem}_jam_interior_texture', f'block/{name}_jam_interior_texture')
        jam_png = art('new_jam_block', f'{stem}_jam', f'block/{name}_jam_block')
        art('new_jello_block', f'{stem}_jello', f'block/{name}_jello_block')
        art('new_jello_bowl', f'{stem}_jello_bowl', f'item/{name}_jello')
        hexc, rgb = colour(jam_png)
        colours[name] = rgb
        if new_jar:
            jar_block(jar, f'{name}_jam_interior_texture', f'{NS}:{jar}')
            write(f'assets/{NS}/models/item/{jar}.json',
                  {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'{NS}:item/{jar}'}})
            lang[f'block.{NS}.{jar}'] = f'{english} Jam Jar'
            new_jars.append(f'{NS}:{jar}')
        soft_fluid(f'{name}_jam', f'{NS}:{jar}', hexc, f'{NS}:{jar}', f'block.{NS}.{jar}',
                   f'{NS}:{name}_jam' if own_fluid else None)

        simple_block(f'{name}_jam_block', f'{name}_jam_block', f'{english} Jam Block')
        simple_block(f'{name}_jello_block', f'{name}_jello_block', f'{english} Jello Block')
        jello_blocks.append(f'{NS}:{name}_jello_block')
        write(f'assets/{NS}/models/item/{name}_jello.json',
              {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'{NS}:item/{name}_jello'}})
        lang[f'item.{NS}.{name}_jello'] = f'{english} Jello'
        if own_fluid:
            lang[f'fluid_type.{NS}.{name}_jam'] = f'{english} Jam'
        lang[f'fluid_type.{NS}.{name}_jello'] = f'Melted {english} Jello'

        # Cauldrons: the fruit stage is water tinted by the jam's colour (JamCauldronColors), as Fruits Delight's;
        # the jam and jello stages show their block's texture at levels 1 to 3
        fruit = f'{name}_cauldron'
        cauldron_model(fruit, 'template_cauldron_full', 'minecraft:block/water_still', tinted=True)
        write(f'assets/{NS}/blockstates/{fruit}.json', {'variants': {'': {'model': f'{NS}:block/{fruit}'}}})
        write(f'assets/{NS}/models/item/{fruit}.json', {'parent': f'{NS}:block/{fruit}'})
        cauldron_loot(fruit)
        lang[f'block.{NS}.{fruit}'] = f'{english} Cauldron'
        cauldrons.append(f'{NS}:{fruit}')
        for stage, word in (('jam', 'Jam'), ('jello', 'Jello')):
            block = f'{name}_{stage}_cauldron'
            variants = {}
            for lvl, parent in ((1, 'template_cauldron_level1'), (2, 'template_cauldron_level2'), (3, 'template_cauldron_full')):
                model = block if lvl == 3 else f'{block}_level{lvl}'
                cauldron_model(model, parent, f'{NS}:block/{name}_{stage}_block')
                variants[f'level={lvl}'] = {'model': f'{NS}:block/{model}'}
            write(f'assets/{NS}/blockstates/{block}.json', {'variants': variants})
            write(f'assets/{NS}/models/item/{block}.json', {'parent': f'{NS}:block/{block}'})
            cauldron_loot(block)
            lang[f'block.{NS}.{block}'] = f'{english} {word} Cauldron'
            cauldrons.append(f'{NS}:{block}')

    # Nut milk jar (1 to 1 with the bottle)
    art('new_jar', 'nut_milk_jar', 'item/nut_milk_jar')
    art('new_mason_jar_interior', 'nut_milk_jar_interior_texture', 'block/nut_milk_jar_interior_texture')
    jar_block('nut_milk_jar', 'nut_milk_jar_interior_texture', f'{NS}:nut_milk_jar')
    write(f'assets/{NS}/models/item/nut_milk_jar.json',
          {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'{NS}:item/nut_milk_jar'}})
    lang[f'block.{NS}.nut_milk_jar'] = 'Nut Milk Jar'
    new_jars.append(f'{NS}:nut_milk_jar')

    # Older Vintage jars: new art over upstream's
    for item_tex, interior_tex, jar_art, interior_art in RETEXTURE:
        art('new_jar', jar_art, f'item/{item_tex}')
        art('new_mason_jar_interior', interior_art, f'block/{interior_tex}')

    # Empty mason jar: Fruits Delight's empty jam jar sprite (the shape every new jar is drawn on). Fruits Delight is
    # All Rights Reserved, so the model points at its texture instead of copying it
    write(f'assets/{NS}/models/item/empty_mason_jar.json',
          {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'fruitsdelight:item/jam_bottle'}})

    # Fruits Delight's jams as placed mason jars (block only; the item is Fruits Delight's)
    for f in FD_JAMS:
        block = f'fd_{f}_jam_jar'
        art('new_mason_jar_interior', f'{f}_jam_interior_texture', f'block/fd_{f}_jam_interior_texture')
        jar_block(block, f'fd_{f}_jam_interior_texture', f'fruitsdelight:{f}_jelly')
        lang[f'block.{NS}.{block}'] = FD_NAMES.get(f, f.replace('_', ' ').title()) + ' Jam'

    lang[f'{NS}.tooltip.jam_block'] = 'Sticky. Sticks to the jello block of the same jam, not to other sticky blocks.'
    lang[f'{NS}.tooltip.jello_block'] = 'Slippery and bouncy. Sticks to sticky blocks, the same jello and the jam block of the same jam.'

    # Tags: own tag files are merged into the port's by port_resources.py
    jam_jars = [f'{NS}:{n}_jam_jar' for n, *_ in JAMS]
    write('data/forge/tags/items/jam_jars.json', {'replace': False, 'values': jam_jars})
    write(f'data/{NS}/tags/items/sweet_jam.json',
          {'replace': False, 'values': [j for j in jam_jars if 'pepper' not in j]})
    write('data/create/tags/items/upright_on_belt.json', {'replace': False, 'values': jam_jars + [f'{NS}:nut_milk_jar']})
    write('data/minecraft/tags/blocks/cauldrons.json', {'replace': False, 'values': cauldrons})
    write('data/minecraft/tags/blocks/mineable/pickaxe.json', {'replace': False, 'values': cauldrons})

    # Lang: merged into the port's en_us by port_resources.py; keep keys written by hand
    path = os.path.join(OWN, f'assets/{NS}/lang/en_us.json')
    cur = json.load(open(path, encoding='utf-8')) if os.path.exists(path) else {}
    cur.update(lang)
    write(f'assets/{NS}/lang/en_us.json', cur)

    # Fruit cauldron tint for the Java side (JamCauldronColors.COLOURS)
    print('fruit cauldron colours:', ', '.join(f'"{k}", 0x{v:06x}' for k, v in colours.items()))
    print(f'{len(lang)} lang keys, {len(new_jars)} new jars, {len(cauldrons)} cauldrons, {len(FD_JAMS)} placed FD jars')


if __name__ == '__main__':
    main()
