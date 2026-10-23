#!/usr/bin/env python3
"""JEI coverage catalog: which recipes, recipe types and world interactions JEI never shows.

Part 1 (crafting) is generated from the /dumpcraftingaudit export
(kubejs/exported/recipes/crafting_audit.json): every recipe whose RecipeType is minecraft:crafting,
with its class, serializer, isSpecial(), result and ingredient count.

JEI 11.8 (the pack's version) decides like this (VanillaRecipes, CategoryRecipeValidator,
VanillaPlugin.registerVanillaCategoryExtensions / replaceSpecialCraftingRecipes):
  - a recipe with isSpecial() == false gets a page when its result is not empty and getIngredients()
    has 1 to 9 entries. The built-in CraftingRecipe extension refuses isSpecial() recipes.
  - an isSpecial() recipe gets a page only when a plugin registered a crafting-category extension for
    its class (or a superclass), or when JEI / a plugin adds stand-in display recipes for it.

EXTENSIONS and STAND_INS below were read from the mods' JEI plugin bytecode (see the History in
feature_writeup/jei_coverage.md for how). Re-check them after a mod update that touches a JEI plugin.

    python3 tools/jei_coverage.py           rewrite the generated part of feature_writeup/jei_coverage.md
    python3 tools/jei_coverage.py --check   exit 1 if a special recipe class has no entry here
"""
import argparse
import json
import sys
from collections import defaultdict
from datetime import datetime
from pathlib import Path

INSTANCE = Path(__file__).resolve().parent.parent
AUDIT = INSTANCE / 'kubejs/exported/recipes/crafting_audit.json'
DOC = INSTANCE / 'feature_writeup/jei_coverage.md'
BEGIN = '<!-- BEGIN GENERATED: crafting -->'
END = '<!-- END GENERATED: crafting -->'

# Crafting-category extensions (IExtendableRecipeCategory.addCategoryExtension); match subclasses too.
EXTENSIONS = {
    'vazkii.botania.common.crafting.recipe.AncientWillRecipe': 'Botania JEIBotaniaPlugin',
    'vazkii.botania.common.crafting.recipe.TerraShattererTippingRecipe': 'Botania JEIBotaniaPlugin',
    'vazkii.botania.common.crafting.recipe.CompositeLensRecipe': 'Botania JEIBotaniaPlugin',
    'vazkii.quark.content.tweaks.recipe.ElytraDuplicationRecipe': 'Quark QuarkJeiPlugin',
    'vazkii.quark.content.tweaks.recipe.SlabToBlockRecipe': 'Quark QuarkJeiPlugin',
    'io.github.mortuusars.exposure.world.item.crafting.recipe.ComponentTransferringRecipe': 'Exposure ExposureJeiPlugin',
    'me.paulf.fairylights.util.crafting.GenericRecipe': 'Fairy Lights FairyLightsJEIPlugin',
    'io.github.mortuusars.salt.crafting.recipe.SaltingRecipe': 'Salt SaltJeiPlugin',
    'io.github.mortuusars.scholar.recipe.NbtTransferringRecipe': 'Scholar ScholarJeiPlugin',
}

# Stand-in display recipes added to the crafting category, keyed by exact recipe class.
STAND_INS = {
    'net.minecraft.world.item.crafting.TippedArrowRecipe': 'JEI built-in (TippedArrowRecipeMaker)',
    'net.minecraft.world.item.crafting.ShulkerBoxColoring': 'JEI built-in (ShulkerBoxColoringRecipeMaker)',
    'net.minecraft.world.item.crafting.SuspiciousStewRecipe': 'JEI built-in (SuspiciousStewRecipeMaker)',
    'net.minecraft.world.item.crafting.ShieldDecorationRecipe': 'JEI built-in (ShieldDecorationRecipeMaker)',
    'com.simibubi.create.content.equipment.toolbox.ToolboxDyeingRecipe': 'Create ToolboxColoringRecipeMaker',
    'com.github.ichhabehunger54.potionbundles.PotionBundleRecipe': 'Potion Bundles JEICompat',
    'fonnymunkey.simplehats.common.recipe.HatScrapRecipe': 'Simple Hats JEIPlugin',
    'fonnymunkey.simplehats.common.recipe.HatVariantRecipe': 'Simple Hats JEIPlugin',
    'net.p3pp3rf1y.sophisticatedbackpacks.crafting.BackpackDyeRecipe': 'Sophisticated Backpacks DyeRecipesMaker',
    'net.p3pp3rf1y.sophisticatedbackpacks.crafting.BackpackUpgradeRecipe': 'Sophisticated Backpacks SBPPlugin (REGISTERED_RECIPES)',
    'net.p3pp3rf1y.sophisticatedcore.crafting.UpgradeNextTierRecipe': 'Sophisticated Core CorePlugin (REGISTERED_RECIPES)',
    'com.SaintAlfonzo.creative_labels.crafting.NoteCopyRecipe': 'Creative Labels CreativeLabelsJei',
    'com.SaintAlfonzo.creative_labels.crafting.NoteDyeRecipe': 'Creative Labels CreativeLabelsJei',
    'com.SaintAlfonzo.creative_labels.crafting.NoteEraseRecipe': 'Creative Labels CreativeLabelsJei',
    'com.SaintAlfonzo.creative_labels.crafting.NoteSpecialRecipe': 'Creative Labels CreativeLabelsJei',
    'dan200.computercraft.shared.turtle.recipes.TurtleUpgradeRecipe': 'CC: Tweaked RecipeResolver (recipe manager plugin)',
    'dan200.computercraft.shared.pocket.recipes.PocketComputerUpgradeRecipe': 'CC: Tweaked RecipeResolver (recipe manager plugin)',
    'appeng.recipes.game.FacadeRecipe': 'AE2 FacadeRegistryPlugin (recipe manager plugin)',
}
for _c in ('TatteredBookRecipe', 'TippedBambooSpikesRecipe', 'BlackboardDuplicateRecipe', 'RepairBubbleBlowerRecipe',
           'FlagFromBannerRecipe', 'ItemLoreRecipe', 'PresentDyeRecipe', 'RopeArrowAddRecipe', 'RopeArrowCreateRecipe',
           'SoapClearRecipe', 'TrappedPresentRecipe', 'WeatheredMapRecipe'):
    STAND_INS['net.mehvahdjukaar.supplementaries.common.items.crafting.' + _c] = 'Supplementaries SpecialRecipeDisplays'

# What each uncovered recipe does, keyed by serializer id. Group: pack (our own code), mod, vanilla.
# Fix: stand-in = shapeless/shaped display recipes from a JEI plugin; info = a JEI information page.
# Recipe ids patcherMod's CyclingCraftingJEI builds a cycling page for (compat/jei/cycling/CyclingCraftingPages.java).
# Keep in step with that file.
CYCLING_PAGES = {
    'creativeengineering:elytraduplication/dragon_scale_elytra',
    'minecraft:repair_item',
    'immersiveengineering:ie_item_repair',
    'alexsmobs:mimicream_repair_recipe',
    'minecraft:banner_duplicate',
    'minecraft:book_cloning',
    'minecraft:map_cloning',
    'minecraft:firework_star',
    'minecraft:firework_star_fade',
    'minecraft:firework_rocket',
    'minecraft:armor_dye',
    'quark:dye_item',
    'immersiveengineering:crafting/curtain_colour',
    'fantasyfurniture:dyeable',
    'suppsquared:sack_dye',
    'bedspreads:add_pattern',
    'bedspreads:remove_pattern',
    'botania:dynamic/cosmetic_attach',
    'botania:dynamic/cosmetic_remove',
    'botania:dynamic/keep_ivy',
    'botania:dynamic/phantom_ink_apply',
    'botania:dynamic/spell_cloth_apply',
    'botania:dynamic/laputa_shard_upgrade',
    'botania:dynamic/lens_dye',
    'botania:dynamic/mana_gun_add_clip',
    'botania:dynamic/mana_gun_add_lens',
    'botania:dynamic/mana_gun_remove_lens',
    'botania:dynamic/merge_vial',
    'botania:dynamic/split_lens',
    'botania:dynamic/black_hole_talisman_extract',
    'chimes:glass_bells_custom',
    'cloudstorage:balloon_arrow',
    'computercraft:colour',
    'computercraft:disk',
    'computercraft:printout',
    'constructionwand:dynamic/wand_upgrade',
    'minecraft:elytra_dye_recipe',
    'minecraft:elytra_banner_recipe',
    'minecraft:elytra_hide_cape_recipe',
    'minecraft:elytra_wing_glow_recipe',
    'minecraft:elytra_wing_split_recipe',
    'minecraft:elytra_wing_combination_recipe',
    'enderstorage:recolour_ender_chest',
    'enderstorage:recolour_ender_tank',
    'enderstorage:recolour_ender_pouch',
    'fairylights:copy_color',
    'immersiveengineering:earmuffs_attach',
    'immersiveengineering:powerpack_attach',
    'immersiveengineering:flare_bullet_color',
    'immersiveengineering:potion_bullet_fill',
    'immersiveengineering:jerrycan_refill',
    'immersiveengineering:speedloader_load',
    'immersiveengineering:shaderbag_downgrading',
    'immersiveengineering:revolver_cycle',
    'littlelogistics:tug_route_copy',
    'littlelogistics:loco_route_copy',
    'nock_enough_arrows:paint_arrow_dye',
    'oldguns:refill_powder_horn',
    'quark:tweaks/crafting/utility/chests/mixed_chest_wood',
    'quark:building/crafting/chests/mixed_chest',
    'storagedrawers:add_upgrade',
}

UNCOVERED = {
    'creativeengineering:elytra_duplication': ('pack', 'Elytra + Dragon Scale: crafts a fresh elytra and keeps the original.', 'stand-in'),
    'alexsmobs:mimicream_repair': ('mod', 'Damaged item + Mimicream: repairs it (AMConfig mimicreamRepair, with a blacklist).', 'stand-in'),
    'armor_trims:crafting_special_untrimming': ('mod', 'Trimmed armor alone: removes the trim.', 'info'),
    'bedspreads:add_pattern': ('mod', 'Bed + patterned banner: puts the banner pattern on the bedspread.', 'stand-in'),
    'bedspreads:remove_pattern': ('mod', 'Patterned bed alone: strips the pattern.', 'stand-in'),
    'botania:black_hole_talisman_extract': ('mod', 'Black Hole Talisman alone: takes out a stack of the stored block.', 'info'),
    'botania:cosmetic_attach': ('mod', 'Bauble + cosmetic bauble: attaches the cosmetic look.', 'stand-in'),
    'botania:cosmetic_remove': ('mod', 'Bauble with a cosmetic alone: detaches it.', 'stand-in'),
    'botania:keep_ivy': ('mod', 'Item + Resolute Ivy: item is kept on death.', 'stand-in'),
    'botania:laputa_shard_upgrade': ('mod', 'Shard of Laputa + Life Essence: next shard level.', 'stand-in'),
    'botania:lens_dye': ('mod', 'Mana lens + dye (or Mana Pearl): coloured / rainbow lens.', 'stand-in'),
    'botania:mana_gun_add_clip': ('mod', 'Mana Blaster + Clip: adds a lens clip.', 'stand-in'),
    'botania:mana_gun_add_lens': ('mod', 'Mana Blaster + lens: loads the lens.', 'stand-in'),
    'botania:mana_gun_remove_lens': ('mod', 'Mana Blaster with a lens alone: takes the lens out.', 'stand-in'),
    'botania:merge_vial': ('mod', 'Two Brew Vials of the same brew: merges them into a flask.', 'stand-in'),
    'botania:phantom_ink_apply': ('mod', 'Armor/item + Phantom Ink: makes it invisible when worn.', 'stand-in'),
    'botania:spell_cloth_apply': ('mod', 'Enchanted item + Spellbinding Cloth: strips enchantments.', 'stand-in'),
    'botania:split_lens': ('mod', 'Composite lens alone: splits it back into two lenses.', 'stand-in'),
    'cb_microblock:microblock': ('mod', 'Saw + block (and microblocks): cuts slabs, panels, covers, posts.', 'info'),
    'chimes:crafting_wind_bell_custom': ('mod', 'Glass Wind Bell + dyes: custom-coloured bell.', 'stand-in'),
    'computercraft:colour': ('mod', 'Computer / turtle / pocket computer / disk + dyes (or sponge): colours it.', 'stand-in'),
    'computercraft:disk': ('mod', 'Paper + redstone + dyes: coloured floppy disk.', 'stand-in'),
    'computercraft:printout': ('mod', 'Printed pages + string / leather: multi-page printout or printed book.', 'stand-in'),
    'constructionwand:wand_upgrade': ('mod', 'Construction Wand + wand core: installs the core.', 'stand-in'),
    'customizableelytra:elytra_banner_recipe': ('mod', 'Elytra + banner: banner pattern on the wings.', 'stand-in'),
    'customizableelytra:elytra_dye_recipe': ('mod', 'Elytra + dyes: dyed elytra.', 'stand-in'),
    'customizableelytra:elytra_hide_cape_recipe': ('mod', 'Elytra + paper: hides the player cape on the wings.', 'stand-in'),
    'customizableelytra:elytra_wing_combination_recipe': ('mod', 'Left wing + right wing: per-wing customised elytra.', 'stand-in'),
    'customizableelytra:elytra_wing_glow_recipe': ('mod', 'Elytra + glow ink sac: glowing wings.', 'stand-in'),
    'customizableelytra:elytra_wing_split_recipe': ('mod', 'Elytra alone: splits into left and right wing for separate styling.', 'stand-in'),
    'enderstorage:recolour_recipe': ('mod', 'Ender chest / tank / pouch + dye: changes one frequency colour.', 'stand-in'),
    'etched:complex_music_label': ('mod', 'Two music labels: two-colour complex label.', 'stand-in'),
    'fairylights:crafting_special_copy_color': ('mod', 'Two fairy-light items: copies the colour of one onto the other.', 'stand-in'),
    'fantasyfurniture:dyeable': ('mod', 'Dyeable furniture + dye: recolours it.', 'stand-in'),
    'farmersdelight:food_serving': ('mod', 'Cooking Pot holding a meal + its container: serves the meal, pot kept.', 'stand-in'),
    'farmersrespite:drink_pouring': ('mod', 'Kettle holding a drink + its container: pours one serving, the kettle keeps the rest (recipe id minecraft:drink_pouring).', 'stand-in'),
    'immersiveengineering:crafting_special_flare_bullet_color': ('mod', 'Flare cartridge + dye: flare colour.', 'stand-in'),
    'immersiveengineering:crafting_special_jerrycan_refill': ('mod', 'Fluid item (drill, chemthrower...) + Jerrycan: refills the item from the can.', 'stand-in'),
    'immersiveengineering:crafting_special_potion_bullet_fill': ('mod', 'Phial cartridge + potion: potion bullet.', 'stand-in'),
    'immersiveengineering:crafting_special_speedloader_load': ('mod', 'Speedloader + cartridges: loads it.', 'stand-in'),
    'immersiveengineering:earmuffs': ('mod', 'Helmet + Earmuffs (+ dye): attaches / colours earmuffs.', 'stand-in'),
    'immersiveengineering:ie_item_repair': ('mod', 'Two damaged IE tools of the same kind: combined durability.', 'info'),
    'immersiveengineering:powerpack': ('mod', 'Chestplate + Capacitor Backpack: attaches the powerpack.', 'stand-in'),
    'immersiveengineering:revolver_cycle': ('mod', 'Revolver alone: cycles its special variant.', 'info'),
    'immersiveengineering:rgb': ('mod', 'Colourable IE item + dyes: RGB colour mixing.', 'stand-in'),
    'immersiveengineering:shader_bag': ('mod', 'Shader alone: back into a grab bag of the same rarity.', 'stand-in'),
    'littlelogistics:loco_route_copy': ('mod', 'Locomotive route + blank route: copies the route.', 'stand-in'),
    'littlelogistics:tug_route_copy': ('mod', 'Tug route + blank route: copies the route.', 'stand-in'),
    'nock_enough_arrows:special_paintarrowdye': ('mod', 'Paint arrow + dye: paint colour.', 'stand-in'),
    'oldguns:powder_horn_refill_shapeless': ('mod', 'Powder horn + gunpowder: refills it.', 'stand-in'),
    'quark:dye_item': ('mod', 'Quark dyeable item + dye: dyes it.', 'stand-in'),
    'quark:mixed_exclusion': ('mod', 'Chest / furnace from mixed plank or stone types (Quark variant-chest replacement).', 'none'),
    'securitycraft:limited_use_keycard_recipe': ('mod', 'Keycard + Limited Use Keycard: limited-use copy of the keycard.', 'stand-in'),
    'sophisticatedcore:upgrade_clear': ('mod', 'Configured upgrade alone: resets its settings.', 'stand-in'),
    'storagedrawers:add_upgrade': ('mod', 'Drawer item + upgrade: drawer with the upgrade pre-installed.', 'stand-in'),
    'suppsquared:sack_dye': ('mod', 'Sack + dye: dyed sack.', 'stand-in'),
    'cloudstorage:balloon_arrow': ('mod', 'Arrows + balloon: balloon arrows. Not special, but getIngredients() is empty, so JEI drops it.', 'stand-in'),
    'minecraft:crafting_special_armordye': ('vanilla', 'Leather armor + dyes.', 'stand-in'),
    'minecraft:crafting_special_bannerduplicate': ('vanilla', 'Patterned banner + blank banner of the same colour: copy.', 'stand-in'),
    'minecraft:crafting_special_bookcloning': ('vanilla', 'Written book + book and quills: copies.', 'stand-in'),
    'minecraft:crafting_special_firework_rocket': ('vanilla', 'Paper + 1-3 gunpowder + stars.', 'stand-in'),
    'minecraft:crafting_special_firework_star': ('vanilla', 'Gunpowder + dyes + shape/effect item.', 'stand-in'),
    'minecraft:crafting_special_firework_star_fade': ('vanilla', 'Firework star + dyes: fade colours.', 'stand-in'),
    'minecraft:crafting_special_mapcloning': ('vanilla', 'Filled map + empty maps: copies.', 'stand-in'),
    'minecraft:crafting_special_mapextending': ('vanilla', 'Filled map + 8 paper: zooms out.', 'stand-in'),
    'minecraft:crafting_special_repairitem': ('vanilla', 'Two damaged items of the same kind: combined durability, enchantments lost.', 'info'),
}

GROUP_TITLES = {
    'pack': 'Our own (patcherMod / KubeJS)',
    'mod': 'Other mods',
    'vanilla': 'Vanilla (JEI 11 never shows these)',
}


def mod_of(rid):
    return rid.split(':', 1)[0]


def coverage(row, classes):
    """('shown' | 'extension' | 'stand-in' | 'cycling' | 'uncovered', source)"""
    if row['id'] in CYCLING_PAGES:
        return 'cycling', 'patcherMod CyclingCraftingJEI'
    if not row.get('special'):
        n = row.get('ingredients')
        if row.get('result') and isinstance(n, int) and 1 <= n <= 9:
            return 'shown', ''
        return 'uncovered', 'JEI skips it: ' + ('no result' if not row.get('result') else f'{n} ingredients')
    for sup in classes.get(row['cls'], [row['cls']]):
        if sup in EXTENSIONS:
            return 'extension', EXTENSIONS[sup]
    if row['cls'] in STAND_INS:
        return 'stand-in', STAND_INS[row['cls']]
    return 'uncovered', ''


def build(audit):
    classes = audit['classes']
    rows = audit['recipes']
    counts = defaultdict(int)
    uncovered = []
    covered = defaultdict(list)          # (source, serializer) -> ids
    unknown = []
    dead = [r for r in rows if r.get('dead')]
    for r in rows:
        kind, src = coverage(r, classes)
        counts[kind] += 1
        if kind == 'uncovered':
            uncovered.append(r)
            if r['serializer'] not in UNCOVERED:
                unknown.append(r)
        elif kind in ('extension', 'stand-in', 'cycling'):
            covered[(kind, src, r['serializer'])].append(r['id'])
    return counts, uncovered, covered, unknown, dead


def render(audit, counts, uncovered, covered, dead, stamp):
    out = []
    total = len(audit['recipes'])
    special = sum(1 for r in audit['recipes'] if r.get('special'))
    out.append(f'Generated by `python3 tools/jei_coverage.py` from the `/dumpcraftingaudit` export of {stamp}: '
               f'{total} crafting-table recipes, {special} of them `isSpecial()`. '
               f'**{counts["uncovered"]} have no JEI page**; {counts["extension"] + counts["stand-in"]} special ones are covered by a plugin, {counts["cycling"]} by patcherMod cycling pages.')
    out.append('')

    by_ser = defaultdict(list)
    for r in uncovered:
        by_ser[r['serializer']].append(r)
    for group in ('pack', 'mod', 'vanilla', None):
        sers = sorted(s for s in by_ser if (UNCOVERED.get(s, (None,))[0]) == group)
        if not sers:
            continue
        title = GROUP_TITLES.get(group, 'Not described yet (add to UNCOVERED in tools/jei_coverage.py)')
        out.append(f'### {title}')
        out.append('')
        out.append('| Mod | Recipe id | What it does | Fix |')
        out.append('|---|---|---|---|')
        for s in sers:
            desc, fix = (UNCOVERED[s][1], UNCOVERED[s][2]) if s in UNCOVERED else (f'`{by_ser[s][0]["cls"]}`', '?')
            ids = ', '.join(f'`{r["id"]}`' for r in by_ser[s])
            out.append(f'| {mod_of(s)} | {ids} | {desc} | {fix} |')
        out.append('')

    out.append('### Special recipes a plugin does cover')
    out.append('')
    out.append('Listed so they are not re-investigated. "extension" = the plugin draws the recipe itself; '
               '"stand-in" = the plugin adds ordinary display recipes in its place; '
               '"cycling" = patcherMod adds one page per kind whose slots cycle together (results from the mod\'s own recipe).')
    out.append('')
    out.append('| Serializer | Recipes | How | Source |')
    out.append('|---|---:|---|---|')
    for (kind, src, ser), ids in sorted(covered.items(), key=lambda kv: kv[0][2]):
        out.append(f'| `{ser}` | {len(ids)} | {kind} | {src} |')
    out.append('')
    if dead:
        out.append('### Shown, but cannot be made (an ingredient with no items)')
        out.append('')
        for r in dead:
            out.append(f'- `{r["id"]}`: {", ".join(r["dead"])}')
        out.append('')
    else:
        out.append('No crafting recipe has an ingredient that resolves to zero items (empty tag), so every shown page is makeable.')
        out.append('')
    return '\n'.join(out)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--check', action='store_true')
    args = ap.parse_args()
    audit = json.loads(AUDIT.read_text())
    counts, uncovered, covered, unknown, dead = build(audit)
    if unknown:
        print('Uncovered crafting serializers with no UNCOVERED entry:')
        for r in unknown:
            print(f'  {r["serializer"]}  {r["id"]}  {r["cls"]}')
    if args.check:
        sys.exit(1 if unknown else 0)
    stamp = datetime.fromtimestamp(AUDIT.stat().st_mtime).strftime('%Y-%m-%d %H:%M')
    body = render(audit, counts, uncovered, covered, dead, stamp)
    doc = DOC.read_text()
    a, b = doc.index(BEGIN) + len(BEGIN), doc.index(END)
    DOC.write_text(doc[:a] + '\n' + body + '\n' + doc[b:])
    print(f'{DOC.relative_to(INSTANCE)}: {counts["uncovered"]} uncovered, '
          f'{counts["extension"]} by extension, {counts["stand-in"]} by stand-in, {counts["cycling"]} by cycling page, {counts["shown"]} shown normally')


if __name__ == '__main__':
    main()
