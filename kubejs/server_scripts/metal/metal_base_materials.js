ServerEvents.recipes(e => {

/*
Combed the whole file. It's thorough on ore→crushed→dust→ingot for the base 9 (copper, iron, gold, zinc, aluminum, lead, silver, nickel, uranium) — crushing, IE crusher, arc furnace, dust blocks are all complete and consistent. The gaps are all in plate / rod / dust-recycle, where you standardized some metals and left the rest on a foreign recipe.

Standardization gaps
1. Plate is a mess — 3 partial coverage sets
Method	pack covers	still on foreign recipe
IE Metal Press (ieMetalPress plate)	gold only	immersiveengineering:metalpress/plate_{copper,iron,zinc,aluminum,lead,silver,nickel,uranium} — 8, unremoved
Create pressing (createPressing)	copper, iron, gold, zinc	aluminum, lead, silver, nickel, uranium have no Create plate recipe at all — their createaddition:rolling/*_plate / vintageimprovements:rolling/*_plate were removed and nothing replaced them
Hand hammering (hammer + ingot)	gold, zinc	immersiveengineering:crafting/plate_{copper,iron,aluminum,lead,nickel,silver,uranium}_hammering — 7, unremoved
(Rod-press and wire-press you did standardize across all 9/8 — plate is the odd one out.)

2. Manual rod (crafting table) — zinc only
R.shaped(rod_zinc …) at line 558 is the only one. immersiveengineering:crafting/stick_iron + crafting/stick_aluminum still fill iron/aluminum (2 ingot → 1 rod); copper/gold/lead/silver/nickel/uranium have no bench rod recipe.

3. Dust → ingot recycle — zinc only
Lines 458/471/476 do dust_zinc → ingot_zinc (smelt/blast/arc). The other 8 fall back to immersiveengineering:arcfurnace/dust_{copper,iron,gold,aluminum,lead,silver,nickel,uranium} (all 8 exist, unremoved) + IE's smelting/ingot_*_from_dust for aluminum/lead/nickel/silver.

4. hammercrushing_zinc / raw_hammercrushing_zinc — removed with the other metals' (2026-10-19).

5. Plate-block ↔ plate: copper's 9 plate_copper → plate_block_copper added (2026-10-19).

Minor / probably intentional
Raw block → storage block smelt/blast: zinc only (with the quark flag). aluminum/lead/silver/nickel/uranium raw blocks have no "smelt to block" path.
brass / steel / constantan / electrum: only wire-coil/coil-block recipes — no plate/rod/wire standardization (they're alloys, no ore, so maybe fine).
Uranium wire is deliberately absent (i.wire_uranium = i.none) — not a gap.
Want me to fill these in? I'd need your call on: plate — keep IE press for the 8 + add pack Create pressing for the missing 5, or fully replace with one standardized method? And ratios for manual rod / dust-recycle.
*/

console.log('Hello! The metal base event has started!')

const R = global.NCH
var i = global.item;

// ############################## Copper ##############################

// ---------- Remove duplicate/unfinished recipes ----------
R.bulk('remove', [
	[{ id: 'everythingcopper:copper_nugget' }, e],
	[{ id: 'everythingcopper:copper_ingot_from_nugget' }, e],
	[{ id: 'everythingcopper:copper_shield' }, e],
	[{ id: 'create:crafting/materials/copper_nugget' }, e],
	[{ id: 'create:crafting/materials/copper_ingot' }, e],
	[{ id: 'create:pressing/copper_ingot' }, e],
	[{ id: 'create_compressed:copper_sheet' }, e],
	[{ id: 'createaddition:rolling/copper_ingot' }, e],
	[{ id: 'immersiveengineering:metalpress/rod_copper' }, e],
	[{ id: 'immersiveengineering:crafting/wire_copper' }, e],
	[{ id: 'createaddition:rolling/copper_plate' }, e],
	[{ id: 'immersiveengineering:metalpress/wire_copper' }, e],
	[{ id: 'createaddition:crafting/spool' }, e],
	[{ id: 'createaddition:crafting/copper_spool' }, e],
	[{ id: 'createaddition:crafting/festive_spool' }, e],
	[{ id: 'immersiveengineering:crusher/raw_ore_copper' }, e],
	[{ id: 'immersiveengineering:crusher/raw_block_copper' }, e],
	[{ id: 'immersiveengineering:crafting/coil_lv' }, e],
	[{ id: 'immersiveengineering:crafting/wirecoil_copper' }, e],
	[{ id: 'immersiveengineering:crafting/coil_mv' }, e],
	[{ id: 'immersiveengineering:crafting/wirecoil_electrum' }, e],
	[{ id: 'immersiveengineering:crafting/wirecoil_structure_steel' }, e],
	[{ id: 'immersiveengineering:crafting/coil_hv' }, e],
	[{ id: 'immersiveengineering:crafting/wirecoil_steel' }, e],
]);

R.bulk('replaceInput', [
	[{ mod: 'everythingcopper' }, i.nugget_copper_ec, i.nugget_copper, e],
	[{ mod: 'create' }, i.nugget_copper_create, i.nugget_copper, e],
	[{ mod: 'bellsandwhistles' }, i.nugget_copper_create, i.nugget_copper, e],
	[{ mod: 'create_ultimate_factory' }, i.nugget_copper_create, i.nugget_copper, e],
	[{ input: i.sheet_copper_create }, i.sheet_copper_create, i.plate_copper, e],
	[{ mod: 'createaddition' }, i.spool_copper_createaddition, i.wire_coil_copper, e],
	[{ id: 'immersiveengineering:crafting/coil_lv' }, i.ingot_iron, i.rod_iron, e],
	[{ id: 'immersiveengineering:crafting/wirecoil_copper' }, i.any_stick, i.spool, e],
]);

R.bulk('replaceOutput', [
	[{ mod: 'everythingcopper' }, i.nugget_copper_ec, i.nugget_copper, e],
	[{ mod: 'create' }, i.nugget_copper_create, i.nugget_copper, e],
	[{ mod: 'create_compressed' }, i.nugget_copper_create, i.nugget_copper, e],
	[{ mod: 'createsifter' }, i.nugget_copper_create, i.nugget_copper, e],
	[{ input: i.sheet_copper_create }, i.sheet_copper_create, i.plate_copper, e],
]);

// ############################## Iron ##############################

R.bulk('remove', [
	[{ id: 'create:pressing/iron_ingot' }, e],
	[{ id: 'create_compressed:iron_sheet_block' }, e],
	[{ id: 'create_compressed:iron_sheet' }, e],
	[{ id: 'createaddition:rolling/iron_ingot' }, e],
	[{ id: 'immersiveengineering:metalpress/rod_iron' }, e],
	[{ id: 'createaddition:rolling/iron_plate' }, e],
	[{ id: 'immersiveengineering:metalpress/wire_iron' }, e],
	[{ id: 'immersiveengineering:crusher/raw_ore_iron' }, e],
	[{ id: 'immersiveengineering:crusher/raw_block_iron' }, e],
]);

R.bulk('replaceInput', [
	[{ input: i.sheet_iron_create }, i.sheet_iron_create, i.plate_iron, e],
]);

// ############################## Gold ##############################

R.bulk('remove', [
	[{ id: 'create:pressing/gold_ingot' }, e],
	[{ id: 'create_compressed:gold_sheet_block' }, e],
	[{ id: 'create_compressed:gold_sheet' }, e],
	[{ id: 'immersiveengineering:crafting/plate_gold_hammering' }, e],
	[{ id: 'immersiveengineering:metalpress/plate_gold' }, e],
	[{ id: 'createaddition:rolling/gold_ingot' }, e],
	[{ id: 'immersiveengineering:metalpress/rod_gold' }, e],
	[{ id: 'createaddition:rolling/gold_plate' }, e],
	[{ id: 'immersiveengineering:metalpress/wire_gold' }, e],
	[{ id: 'immersiveengineering:crusher/raw_ore_gold' }, e],
	[{ id: 'immersiveengineering:crusher/raw_block_gold' }, e],
]);

R.bulk('replaceInput', [
	[{ input: i.sheet_gold_create }, i.sheet_gold_create, i.plate_gold, e],
	[{ id: 'immersiveposts:has_gold_rod' }, i.any_ingot_gold, i.ingot_gold, e],
	[{ id: 'create:crushing/update_one/scrap_gold_sheet' }, i.sheet_gold_create, i.plate_gold, e],
	[{ id: 'create_things_and_misc:portable_whistle_craft' }, i.sheet_gold_create, i.plate_gold, e],
	[{ input: i.wire_gold_createaddition }, i.wire_gold_createaddition, i.wire_gold, e],
]);

// ############################## Zinc ##############################

R.bulk('remove', [
	[{ id: 'vintageimprovements:rolling/zinc_plate' }, e],
	[{ id: 'createaddition:rolling/zinc_ingot' }, e],
	[{ id: 'immersiveengineering:metalpress/rod_zinc' }, e],
	[{ id: 'vintageimprovements:rolling/zinc_ingot' }, e],
	[{ id: 'immersiveengineering:metalpress/wire_zinc' }, e],
]);

// ############################## Aluminum ##############################

R.bulk('remove', [
	[{ id: 'immersiveengineering:crafting/wire_aluminum' }, e],
	[{ id: 'createaddition:rolling/aluminum_plate' }, e],
	[{ id: 'vintageimprovements:rolling/aluminum_plate' }, e],
	[{ id: 'createaddition:rolling/aluminum_ingot' }, e],
	[{ id: 'immersiveengineering:metalpress/rod_aluminum' }, e],
	[{ id: 'immersiveengineering:metalpress/wire_aluminum' }, e],
]);

// ############################## Lead ##############################

R.bulk('remove', [
	[{ id: 'immersiveengineering:crafting/wire_lead' }, e],
	[{ id: 'createaddition:rolling/lead_plate' }, e],
	[{ id: 'vintageimprovements:rolling/lead_plate' }, e],
	[{ id: 'immersiveengineering:metalpress/rod_lead' }, e],
	[{ id: 'immersiveengineering:metalpress/wire_lead' }, e],
	[{ id: 'vintageimprovements:coiling/lead_rod' }, e],
	[{ id: 'vintageimprovements:coiling/lead_wire' }, e],
]);

// ############################## Silver ##############################

R.bulk('remove', [
	[{ id: 'vintageimprovements:rolling/silver_plate' }, e],
	[{ id: 'immersiveengineering:metalpress/rod_silver' }, e],
	[{ id: 'immersiveengineering:metalpress/wire_silver' }, e],
]);

// ############################## Nickel ##############################

R.bulk('remove', [
	[{ id: 'createaddition:rolling/nickel_plate' }, e],
	[{ id: 'vintageimprovements:rolling/nickel_plate' }, e],
	[{ id: 'immersiveengineering:metalpress/rod_nickel' }, e],
	[{ id: 'immersiveengineering:metalpress/wire_nickel' }, e],
]);

// ############################## Uranium ##############################

R.bulk('remove', [
	[{ id: 'vintageimprovements:rolling/uranium_plate' }, e],
	[{ id: 'immersiveengineering:metalpress/rod_uranium' }, e],
	[{ id: 'vintageimprovements:coiling/uranium_rod' }, e],
]);

// ############################## Processes ##############################

//Remove ore crushing
R.bulk('remove', [
	[{ id: 'create:crushing/copper_ore' }, e],
	[{ id: 'create:crushing/deepslate_copper_ore' }, e],
	[{ id: 'immersiveengineering:crusher/ore_copper' }, e],
	[{ id: 'immersiveengineering:crafting/hammercrushing_copper' }, e],
	[{ id: 'immersiveengineering:crafting/raw_hammercrushing_copper' }, e],
	
	[{ id: 'create:crushing/iron_ore' }, e],
	[{ id: 'create:crushing/deepslate_iron_ore' }, e],
	[{ id: 'immersiveengineering:crusher/ore_iron' }, e],
	[{ id: 'immersiveengineering:crafting/hammercrushing_iron' }, e],
	[{ id: 'immersiveengineering:crafting/raw_hammercrushing_iron' }, e],
	
	[{ id: 'create:crushing/gold_ore' }, e],
	[{ id: 'create:crushing/deepslate_gold_ore' }, e],
	[{ id: 'immersiveengineering:crusher/ore_gold' }, e],
	[{ id: 'immersiveengineering:crafting/hammercrushing_gold' }, e],
	[{ id: 'immersiveengineering:crafting/raw_hammercrushing_gold' }, e],
	
	[{ id: 'create:crushing/zinc_ore' }, e],
	[{ id: 'create:crushing/deepslate_zinc_ore' }, e],
	[{id: 'create_things_and_misc:zinc_knif_craft'}, e],
	[{id: 'createaddition:pressing/zinc_ingot'}, e],
	[{id: 'minecraft:pressing/zinc_sheet'}, e],
	[{ id: 'immersiveengineering:crafting/hammercrushing_zinc' }, e],
	[{ id: 'immersiveengineering:crafting/raw_hammercrushing_zinc' }, e],
	
	[{ id: 'immersiveengineering:crafting/hammercrushing_aluminum' }, e],
	[{ id: 'immersiveengineering:crafting/raw_hammercrushing_aluminum' }, e],
	[{ id: 'immersiveengineering:crusher/ore_aluminum' }, e],
	[{ id: 'immersiveengineering:crusher/raw_ore_aluminum' }, e],
	[{ id: 'immersiveengineering:crusher/raw_block_aluminum' }, e],
	[{ id: 'create:crushing/aluminum_ore' }, e],
	[{ id: 'create:crushing/deepslate_aluminum_ore' }, e],
	
	[{ id: 'immersiveengineering:crafting/hammercrushing_lead' }, e],
	[{ id: 'immersiveengineering:crafting/raw_hammercrushing_lead' }, e],
	[{ id: 'immersiveengineering:crusher/ore_lead' }, e],
	[{ id: 'immersiveengineering:crusher/raw_ore_lead' }, e],
	[{ id: 'immersiveengineering:crusher/raw_block_lead' }, e],
	[{ id: 'create:crushing/lead_ore' }, e],
	[{ id: 'create:crushing/deepslate_lead_ore' }, e],	
	
	[{ id: 'create:crushing/silver_ore' }, e],
	[{ id: 'create:crushing/deepslate_silver_ore' }, e],
	[{ id: 'immersiveengineering:crusher/ore_silver' }, e],
	[{ id: 'immersiveengineering:crusher/raw_ore_silver' }, e],
	[{ id: 'immersiveengineering:crusher/raw_block_silver' }, e],
	[{ id: 'immersiveengineering:crafting/hammercrushing_silver' }, e],
	[{ id: 'immersiveengineering:crafting/raw_hammercrushing_silver' }, e],
	
	[{ id: 'create:crushing/nickel_ore' }, e],
	[{ id: 'create:crushing/deepslate_nickel_ore' }, e],
	[{ id: 'immersiveengineering:crusher/ore_nickel' }, e],
	[{ id: 'immersiveengineering:crusher/raw_ore_nickel' }, e],
	[{ id: 'immersiveengineering:crusher/raw_block_nickel' }, e],
	[{ id: 'immersiveengineering:crafting/hammercrushing_nickel' }, e],
	[{ id: 'immersiveengineering:crafting/raw_hammercrushing_nickel' }, e],
	
	[{ id: 'create:crushing/uranium_ore' }, e],
	[{ id: 'create:crushing/deepslate_uranium_ore' }, e],
	[{ id: 'immersiveengineering:crusher/ore_uranium' }, e],
	[{ id: 'immersiveengineering:crusher/raw_ore_uranium' }, e],
	[{ id: 'immersiveengineering:crusher/raw_block_uranium' }, e],
	[{ id: 'immersiveengineering:crafting/hammercrushing_uranium' }, e],
	[{ id: 'immersiveengineering:crafting/raw_hammercrushing_uranium' }, e],
	
]);

// ---------- Create crushing: ore/deepslate ore -> crushed ore ----------
R.bulk('createCrushing', [
	[[
		Item.of(i.crushed_copper,1),Item.of(i.crushed_copper, 1).withChance(0.5),
		Item.of(i.xp_nugget, 1).withChance(0.75),Item.of(i.cobblestone, 1).withChance(0.12)
		], i.ore_copper, e],
	[[
		Item.of(i.crushed_copper, 2),Item.of(i.crushed_copper, 1).withChance(0.25),
		Item.of(i.xp_nugget, 1).withChance(0.75),Item.of(i.deepslate_cobble, 1).withChance(0.12)
	], i.ds_ore_copper, e],
	
	[[
		Item.of(i.crushed_iron, 1), Item.of(i.crushed_iron, 1).withChance(0.5),
		Item.of(i.xp_nugget, 1).withChance(0.75), Item.of(i.cobblestone, 1).withChance(0.12)
	], i.ore_iron, e],
	[[
		Item.of(i.crushed_iron, 2), Item.of(i.crushed_iron, 1).withChance(0.25),
		Item.of(i.xp_nugget, 1).withChance(0.75), Item.of(i.deepslate_cobble, 1).withChance(0.12)
	], i.ds_ore_iron, e],

	[[
		Item.of(i.crushed_gold, 1), Item.of(i.crushed_gold, 1).withChance(0.5),
		Item.of(i.xp_nugget, 1).withChance(0.75), Item.of(i.cobblestone, 1).withChance(0.12)
	], i.ore_gold, e],
	[[
		Item.of(i.crushed_gold, 2), Item.of(i.crushed_gold, 1).withChance(0.25),
		Item.of(i.xp_nugget, 1).withChance(0.75), Item.of(i.deepslate_cobble, 1).withChance(0.12)
	], i.ds_ore_gold, e],
	
	[[
		Item.of(i.crushed_zinc, 1), Item.of(i.crushed_zinc, 1).withChance(0.5),
		Item.of(i.xp_nugget, 1).withChance(0.75), Item.of(i.cobblestone, 1).withChance(0.12)
	], i.ore_zinc, e],
	[[
		Item.of(i.crushed_zinc, 2), Item.of(i.crushed_zinc, 1).withChance(0.25),
		Item.of(i.xp_nugget, 1).withChance(0.75), Item.of(i.deepslate_cobble, 1).withChance(0.12)
	], i.ds_ore_zinc, e],
	
	[[
		Item.of(i.crushed_aluminum, 1), Item.of(i.crushed_aluminum, 1).withChance(0.5),
		Item.of(i.xp_nugget, 1).withChance(0.75), Item.of(i.cobblestone, 1).withChance(0.12)
	], i.ore_aluminum, e],
	[[
		Item.of(i.crushed_aluminum, 2), Item.of(i.crushed_aluminum, 1).withChance(0.25),
		Item.of(i.xp_nugget, 1).withChance(0.75), Item.of(i.deepslate_cobble, 1).withChance(0.12)
	], i.ds_ore_aluminum, e],
	
	[[
		Item.of(i.crushed_lead, 1), Item.of(i.crushed_lead, 1).withChance(0.5),
		Item.of(i.xp_nugget, 1).withChance(0.75), Item.of(i.cobblestone, 1).withChance(0.12)
	], i.ore_lead, e],
	[[
		Item.of(i.crushed_lead, 2), Item.of(i.crushed_lead, 1).withChance(0.25),
		Item.of(i.xp_nugget, 1).withChance(0.75), Item.of(i.deepslate_cobble, 1).withChance(0.12)
	], i.ds_ore_lead, e],
	
	[[
		Item.of(i.crushed_silver, 1), Item.of(i.crushed_silver, 1).withChance(0.5),
		Item.of(i.xp_nugget, 1).withChance(0.75), Item.of(i.cobblestone, 1).withChance(0.12)
	], i.ore_silver, e],
	[[
		Item.of(i.crushed_silver, 2), Item.of(i.crushed_silver, 1).withChance(0.25),
		Item.of(i.xp_nugget, 1).withChance(0.75), Item.of(i.deepslate_cobble, 1).withChance(0.12)
	], i.ds_ore_silver, e],
	
	[[
		Item.of(i.crushed_nickel, 1), Item.of(i.crushed_nickel, 1).withChance(0.5),
		Item.of(i.xp_nugget, 1).withChance(0.75), Item.of(i.cobblestone, 1).withChance(0.12)
	], i.ore_nickel, e],
	[[
		Item.of(i.crushed_nickel, 2), Item.of(i.crushed_nickel, 1).withChance(0.25),
		Item.of(i.xp_nugget, 1).withChance(0.75), Item.of(i.deepslate_cobble, 1).withChance(0.12)
	], i.ds_ore_nickel, e],
	
	[[
		Item.of(i.crushed_uranium, 1), Item.of(i.crushed_uranium, 1).withChance(0.5),
		Item.of(i.xp_nugget, 1).withChance(0.75), Item.of(i.cobblestone, 1).withChance(0.12)
	], i.ore_uranium, e],
	[[
		Item.of(i.crushed_uranium, 2), Item.of(i.crushed_uranium, 1).withChance(0.25),
		Item.of(i.xp_nugget, 1).withChance(0.75), Item.of(i.deepslate_cobble, 1).withChance(0.12)
	], i.ds_ore_uranium, e],
]);

// ---------- IE crusher: raw ore/raw block/ore -> crushed ore ----------
R.bulk('ieCrushing', [
	[Item.of(i.crushed_copper, 1), i.raw_copper, e, undefined, [{ chance: 0.333, output: Item.of(i.crushed_copper, 1) }], 6000],
	[Item.of(i.crushed_copper, 12), i.raw_block_copper, e, undefined, undefined, 24000],
	[Item.of(i.crushed_copper, 2), i.any_ore_copper, e],
	
	[i.crushed_iron, i.raw_iron, e, undefined, [{ chance: 0.333, output: i.crushed_iron }], 6000],
	[Item.of(i.crushed_iron, 12), i.raw_block_iron, e, undefined, undefined, 24000],
	[Item.of(i.crushed_iron, 2), i.any_ore_iron, e],

	[i.crushed_gold, i.raw_gold, e, undefined, [{ chance: 0.333, output: i.crushed_gold }], 6000],
	[Item.of(i.crushed_gold, 12), i.raw_block_gold, e, undefined, undefined, 24000],
	[Item.of(i.crushed_gold, 2), i.any_ore_gold, e],
	
	[i.crushed_zinc, i.raw_zinc, e, undefined, [{ chance: 0.333, output: i.crushed_zinc }], 6000],
	[Item.of(i.crushed_zinc, 12), i.raw_block_zinc, e, undefined, undefined, 24000],
	[Item.of(i.crushed_zinc, 2), i.any_ore_zinc, e],
	
	[i.crushed_aluminum, i.raw_aluminum, e, undefined, [{ chance: 0.333, output: i.crushed_aluminum }], 6000],
	[Item.of(i.crushed_aluminum, 12), i.raw_block_aluminum, e, undefined, undefined, 24000],
	[Item.of(i.crushed_aluminum, 2), i.any_ore_aluminum, e],
	
	[i.crushed_lead, i.raw_lead, e, undefined, [{ chance: 0.333, output: i.crushed_lead }], 6000],
	[Item.of(i.crushed_lead, 12), i.raw_block_lead, e, undefined, undefined, 24000],
	[Item.of(i.crushed_lead, 2), i.any_ore_lead, e],
	
	[i.crushed_silver, i.raw_silver, e, undefined, [{ chance: 0.333, output: i.crushed_silver }], 6000],
	[Item.of(i.crushed_silver, 12), i.raw_block_silver, e, undefined, undefined, 24000],
	[Item.of(i.crushed_silver, 2), i.any_ore_silver, e],
	
	[i.crushed_nickel, i.raw_nickel, e, undefined, [{ chance: 0.333, output: i.crushed_nickel }], 6000],
	[Item.of(i.crushed_nickel, 12), i.raw_block_nickel, e, undefined, undefined, 24000],
	[Item.of(i.crushed_nickel, 2), i.any_ore_nickel, e],
	
	[i.crushed_uranium, i.raw_uranium, e, undefined, [{ chance: 0.333, output: i.crushed_uranium }], 6000],
	[Item.of(i.crushed_uranium, 12), i.raw_block_uranium, e, undefined, undefined, 24000],
	[Item.of(i.crushed_uranium, 2), i.any_ore_uranium, e],
]);

// ---------- IE crusher: crushed ore/ crushed ore block -> dust ----------
R.bulk('ieCrushing', [
	[Item.of(i.dust_copper, 1), i.crushed_copper, e, undefined, [{ chance: 0.333, output: Item.of(i.dust_copper, 1) }], 6000],
	[Item.of(i.dust_copper, 12), i.crushed_block_copper, e, undefined, undefined, 24000],
	
	[i.dust_iron, i.crushed_iron, e, undefined, [{ chance: 0.333, output: i.dust_iron }], 6000],
	[Item.of(i.dust_iron, 12), i.crushed_block_iron, e, undefined, undefined, 24000],

	[i.dust_gold, i.crushed_gold, e, undefined, [{ chance: 0.333, output: i.dust_gold }], 6000],
	[Item.of(i.dust_gold, 12), i.crushed_block_gold, e, undefined, undefined, 24000],
	
	[i.dust_zinc, i.crushed_zinc, e, undefined, [{ chance: 0.333, output: i.dust_zinc }], 6000],
	[Item.of(i.dust_zinc, 12), i.crushed_block_zinc, e, undefined, undefined, 24000],
	
	[i.dust_aluminum, i.crushed_aluminum, e, undefined, [{ chance: 0.333, output: i.dust_aluminum }], 6000],
	[Item.of(i.dust_aluminum, 12), i.crushed_block_aluminum, e, undefined, undefined, 24000],
	
	[i.dust_lead, i.crushed_lead, e, undefined, [{ chance: 0.333, output: i.dust_lead }], 6000],
	[Item.of(i.dust_lead, 12), i.crushed_block_lead, e, undefined, undefined, 24000],
	
	[i.dust_silver, i.crushed_silver, e, undefined, [{ chance: 0.333, output: i.dust_silver }], 6000],
	[Item.of(i.dust_silver, 12), i.crushed_block_silver, e, undefined, undefined, 24000],
	
	[i.dust_nickel, i.crushed_nickel, e, undefined, [{ chance: 0.333, output: i.dust_nickel }], 6000],
	[Item.of(i.dust_nickel, 12), i.crushed_block_nickel, e, undefined, undefined, 24000],
	
	[i.dust_uranium, i.crushed_uranium, e, undefined, [{ chance: 0.333, output: i.dust_uranium }], 6000],
	[Item.of(i.dust_uranium, 12), i.crushed_block_uranium, e, undefined, undefined, 24000],
]);

// ---------- IE crusher: ingot -> dust ----------
R.bulk('ieCrushing', [
	[i.dust_gold, i.ingot_gold, e, undefined, undefined, 6000],
]);

// ---------- Arc Furnace: crushed ore/crushed ore block -> ingot ----------
R.bulk('ieArcFurnace', [
	[i.ingot_copper, i.crushed_copper, e, undefined, [{ chance: 0.5, output: i.ingot_copper }]],
	[Item.of(i.ingot_copper, 13), i.crushed_block_copper, e, undefined, [{ chance: 0.5, output: i.ingot_copper }]],
	
	[i.ingot_iron, i.crushed_iron, e, undefined, [{ chance: 0.5, output: i.ingot_iron }]],
	[Item.of(i.ingot_iron, 13), i.crushed_block_iron, e, undefined, [{ chance: 0.5, output: i.ingot_iron }]],
	
	[i.ingot_gold, i.crushed_gold, e, undefined, [{ chance: 0.5, output: i.ingot_gold }]],
	[Item.of(i.ingot_gold, 13), i.crushed_block_gold, e, undefined, [{ chance: 0.5, output: i.ingot_gold }]],
	
	[i.ingot_zinc, i.crushed_zinc, e, undefined, [{ chance: 0.5, output: i.ingot_zinc }]],
	[Item.of(i.ingot_zinc, 13), i.crushed_block_zinc, e, undefined, [{ chance: 0.5, output: i.ingot_zinc }]],
	
	[i.ingot_aluminum, i.crushed_aluminum, e, undefined, [{ chance: 0.5, output: i.ingot_aluminum }]],
	[Item.of(i.ingot_aluminum, 13), i.crushed_block_aluminum, e, undefined, [{ chance: 0.5, output: i.ingot_aluminum }]],
	
	[i.ingot_lead, i.crushed_lead, e, undefined, [{ chance: 0.5, output: i.ingot_lead }]],
	[Item.of(i.ingot_lead, 13), i.crushed_block_lead, e, undefined, [{ chance: 0.5, output: i.ingot_lead }]],
	
	[i.ingot_silver, i.crushed_silver, e, undefined, [{ chance: 0.5, output: i.ingot_silver }]],
	[Item.of(i.ingot_silver, 13), i.crushed_block_silver, e, undefined, [{ chance: 0.5, output: i.ingot_silver }]],
	
	[i.ingot_nickel, i.crushed_nickel, e, undefined, [{ chance: 0.5, output: i.ingot_nickel }]],
	[Item.of(i.ingot_nickel, 13), i.crushed_block_nickel, e, undefined, [{ chance: 0.5, output: i.ingot_nickel }]],
	
	[i.ingot_uranium, i.crushed_uranium, e, undefined, [{ chance: 0.5, output: i.ingot_uranium }]],
	[Item.of(i.ingot_uranium, 13), i.crushed_block_uranium, e, undefined, [{ chance: 0.5, output: i.ingot_uranium }]],
]);

// ---------- Raw block smelting/blasting recycle ----------
R.bulk('smelting', [
	[i.block_zinc, i.raw_block_zinc, e, undefined, 6.3, 1800, [{ type: 'quark:flag', flag: 'smelt_ore_blocks' }]],
]);

R.bulk('blasting', [
	[i.block_zinc, i.raw_block_zinc, e, undefined, 6.3, 900, [{ type: 'quark:flag', flag: 'smelt_ore_blocks' }]],
]);

// ---------- Crushed ore smelting/blasting recycle ----------
R.bulk('smelting', [
	[i.block_copper, i.crushed_block_copper, e, undefined, 0, 1800],
	[i.block_iron, i.crushed_block_iron, e, undefined, 0, 1800],
	[i.block_gold, i.crushed_block_gold, e, undefined, 0, 1800],
	[i.block_zinc, i.crushed_block_zinc, e, undefined, 0, 1800],
	[i.block_aluminum, i.crushed_block_aluminum, e, undefined, 0, 1800],
	[i.block_lead, i.crushed_block_lead, e, undefined, 0, 1800],
	[i.block_silver, i.crushed_block_silver, e, undefined, 0, 1800],
	[i.block_nickel, i.crushed_block_nickel, e, undefined, 0, 1800],
	[i.block_uranium, i.crushed_block_uranium, e, undefined, 0, 1800],
]);
R.bulk('blasting', [
	[i.block_copper, i.crushed_block_copper, e, undefined, 0, 900],
	[i.block_iron, i.crushed_block_iron, e, undefined, 0, 900],
	[i.block_gold, i.crushed_block_gold, e, undefined, 0, 900],
	[i.block_zinc, i.crushed_block_zinc, e, undefined, 0, 900],
	[i.block_aluminum, i.crushed_block_aluminum, e, undefined, 0, 900],
	[i.block_lead, i.crushed_block_lead, e, undefined, 0, 900],
	[i.block_silver, i.crushed_block_silver, e, undefined, 0, 900],
	[i.block_nickel, i.crushed_block_nickel, e, undefined, 0, 900],
	[i.block_uranium, i.crushed_block_uranium, e, undefined, 0, 900],
]);


// ---------- Dust smelting/blasting/arc smelting recycle ----------
R.bulk('smelting', [
	[i.block_copper, i.dust_block_copper, e, undefined, undefined, 1800],
	[i.block_iron, i.dust_block_iron, e, undefined, undefined, 1800],
	[i.block_gold, i.dust_block_gold, e, undefined, undefined, 1800],
	[i.block_zinc, i.dust_block_zinc, e, undefined, undefined, 1800],
	[i.block_aluminum, i.dust_block_aluminum, e, undefined, undefined, 1800],
	[i.block_lead, i.dust_block_lead, e, undefined, undefined, 1800],
	[i.block_silver, i.dust_block_silver, e, undefined, undefined, 1800],
	[i.block_nickel, i.dust_block_nickel, e, undefined, undefined, 1800],
	[i.block_uranium, i.dust_block_uranium, e, undefined, undefined, 1800],
	[i.ingot_zinc, i.dust_zinc, e, undefined, undefined, 200],

	[i.block_rose_gold, i.dust_block_rose_gold, e, undefined, undefined, 1800],
	[i.block_cast_iron, i.dust_block_cast_iron, e, undefined, undefined, 1800],
	[i.block_electrum, i.dust_block_electrum, e, undefined, undefined, 1800],
	[i.block_steel, i.dust_block_steel, e, undefined, undefined, 1800],
	[i.block_constantan, i.dust_block_constantan, e, undefined, undefined, 1800],
	[i.block_brass, i.dust_block_brass, e, undefined, undefined, 1800],
]);

R.bulk('blasting', [
	[i.block_copper, i.dust_block_copper, e, undefined, undefined, 900],
	[i.block_iron, i.dust_block_iron, e, undefined, undefined, 900],
	[i.block_gold, i.dust_block_gold, e, undefined, undefined, 900],
	[i.block_zinc, i.dust_block_zinc, e, undefined, undefined, 900],
	[i.block_aluminum, i.dust_block_aluminum, e, undefined, undefined, 900],
	[i.block_lead, i.dust_block_lead, e, undefined, undefined, 900],
	[i.block_silver, i.dust_block_silver, e, undefined, undefined, 900],
	[i.block_nickel, i.dust_block_nickel, e, undefined, undefined, 900],
	[i.block_uranium, i.dust_block_uranium, e, undefined, undefined, 900],
	[i.ingot_zinc, i.dust_zinc, e, undefined, undefined, 100],

	[i.block_rose_gold, i.dust_block_rose_gold, e, undefined, undefined, 900],
	[i.block_cast_iron, i.dust_block_cast_iron, e, undefined, undefined, 900],
	[i.block_electrum, i.dust_block_electrum, e, undefined, undefined, 900],
	[i.block_steel, i.dust_block_steel, e, undefined, undefined, 900],
	[i.block_constantan, i.dust_block_constantan, e, undefined, undefined, 900],
	[i.block_brass, i.dust_block_brass, e, undefined, undefined, 900],
]);

// ---------- Dust arc smelting ----------
R.bulk('ieArcFurnace', [
	[i.ingot_zinc, i.dust_zinc, e],
]);


// ---------- Dust shapeless crafting ----------
R.bulk('shapeless', [
	[Item.of(i.dust_copper, 9), i.dust_block_copper, e],
	[i.dust_block_copper, Item.of(i.dust_copper, 9), e],
	
	[Item.of(i.dust_iron, 9), i.dust_block_iron, e],
	[i.dust_block_iron, Item.of(i.dust_iron, 9), e],
	
	[Item.of(i.dust_gold, 9), i.dust_block_gold, e],
	[i.dust_block_gold, Item.of(i.dust_gold, 9), e],
	
	[Item.of(i.dust_zinc, 9), i.dust_block_zinc, e],
	[i.dust_block_zinc, Item.of(i.dust_zinc, 9), e],
	
	[Item.of(i.dust_aluminum, 9), i.dust_block_aluminum, e],
	[i.dust_block_aluminum, Item.of(i.dust_aluminum, 9), e],
	
	[Item.of(i.dust_lead, 9), i.dust_block_lead, e],
	[i.dust_block_lead, Item.of(i.dust_lead, 9), e],
	
	[Item.of(i.dust_silver, 9), i.dust_block_silver, e],
	[i.dust_block_silver, Item.of(i.dust_silver, 9), e],
	
	[Item.of(i.dust_nickel, 9), i.dust_block_nickel, e],
	[i.dust_block_nickel, Item.of(i.dust_nickel, 9), e],
	
	[Item.of(i.dust_uranium, 9), i.dust_block_uranium, e],
	[i.dust_block_uranium, Item.of(i.dust_uranium, 9), e],
	
	[Item.of(i.dust_diamond, 9), i.dust_block_diamond, e],
	[i.dust_block_diamond, Item.of(i.dust_diamond, 9), e],

	[Item.of(i.dust_rose_gold, 9), i.dust_block_rose_gold, e],
	[i.dust_block_rose_gold, Item.of(i.dust_rose_gold, 9), e],

	[Item.of(i.dust_cast_iron, 9), i.dust_block_cast_iron, e],
	[i.dust_block_cast_iron, Item.of(i.dust_cast_iron, 9), e],

	[Item.of(i.dust_electrum, 9), i.dust_block_electrum, e],
	[i.dust_block_electrum, Item.of(i.dust_electrum, 9), e],

	[Item.of(i.dust_steel, 9), i.dust_block_steel, e],
	[i.dust_block_steel, Item.of(i.dust_steel, 9), e],

	[Item.of(i.dust_constantan, 9), i.dust_block_constantan, e],
	[i.dust_block_constantan, Item.of(i.dust_constantan, 9), e],

	[Item.of(i.dust_brass, 9), i.dust_block_brass, e],
	[i.dust_block_brass, Item.of(i.dust_brass, 9), e],
]);

// ---------- Plate shapeless crafting ----------
R.bulk('shapeless', [
	[Item.of(i.plate_copper, 9), i.plate_block_copper, e],
	[i.plate_block_copper, Item.of(i.plate_copper, 9), e],
	
	[Item.of(i.plate_iron, 9), i.plate_block_iron, e],
	[i.plate_block_iron, Item.of(i.plate_iron, 9), e],
	
	[i.plate_gold, [i.ie_hammer, i.ingot_gold], e],
	[Item.of(i.plate_gold, 9), i.plate_block_gold, e],
	[i.plate_block_gold, Item.of(i.plate_gold, 9), e],
	
	[i.plate_zinc, [i.ie_hammer, i.ingot_zinc], e],
	[Item.of(i.plate_zinc, 9), i.plate_block_zinc, e],
	[i.plate_block_zinc, Item.of(i.plate_zinc, 9), e],
	
	[Item.of(i.plate_aluminum, 9), i.plate_block_aluminum, e],
	[i.plate_block_aluminum, Item.of(i.plate_aluminum, 9), e],
	
	[Item.of(i.plate_lead, 9), i.plate_block_lead, e],
	[i.plate_block_lead, Item.of(i.plate_lead, 9), e],
	
	[Item.of(i.plate_silver, 9), i.plate_block_silver, e],
	[i.plate_block_silver, Item.of(i.plate_silver, 9), e],
	
	[Item.of(i.plate_nickel, 9), i.plate_block_nickel, e],
	[i.plate_block_nickel, Item.of(i.plate_nickel, 9), e],
	
	[Item.of(i.plate_uranium, 9), i.plate_block_uranium, e],
	[i.plate_block_uranium, Item.of(i.plate_uranium, 9), e],
]);

// ---------- Create Plate pressing ----------
R.bulk('createPressing', [
	[i.plate_copper, i.ingot_copper, e],
	[i.plate_iron, i.ingot_iron, e],
	[i.plate_gold, i.ingot_gold, e],
	[i.plate_zinc, i.ingot_zinc, e],
]);

// ---------- IE Plate pressing ----------
R.bulk('ieMetalPress', [
	[i.plate_gold, i.ingot_gold, i.mold_plate, e],
]);

// ---------- Rod manual crafting ----------
R.shaped(Item.of(i.rod_zinc, 4), ['A','A'], {A: i.ingot_zinc}, e)

// ---------- Rod rolling  ----------
R.bulk('createRolling', [
	[Item.of(i.rod_copper, 4), i.ingot_copper, e],
	[Item.of(i.rod_iron, 4), i.ingot_iron, e],
	[Item.of(i.rod_gold, 4), i.ingot_gold, e],
	[Item.of(i.rod_zinc, 4), i.ingot_zinc, e],
	[Item.of(i.rod_aluminum, 4), i.ingot_aluminum, e],
	[Item.of(i.rod_lead, 4), i.ingot_lead, e],
	[Item.of(i.rod_silver, 4), i.ingot_silver, e],
	[Item.of(i.rod_nickel, 4), i.ingot_nickel, e],
	[Item.of(i.rod_uranium, 4), i.ingot_uranium, e],
]);

// ---------- Rod pressing ----------
R.bulk('ieMetalPress', [
	[Item.of(i.rod_copper, 4), i.ingot_copper, i.mold_rod, e],
	[Item.of(i.rod_iron, 4), i.ingot_iron, i.mold_rod, e],
	[Item.of(i.rod_gold, 4), i.ingot_gold, i.mold_rod, e],
	[Item.of(i.rod_zinc, 4), i.ingot_zinc, i.mold_rod, e],
	[Item.of(i.rod_aluminum, 4), i.ingot_aluminum, i.mold_rod, e],
	[Item.of(i.rod_lead, 4), i.ingot_lead, i.mold_rod, e],
	[Item.of(i.rod_silver, 4), i.ingot_silver, i.mold_rod, e],
	[Item.of(i.rod_nickel, 4), i.ingot_nickel, i.mold_rod, e],
	[Item.of(i.rod_uranium, 4), i.ingot_uranium, i.mold_rod, e],
]);

// ---------- Wire manual ----------
R.bulk('shapeless', [
	[Item.of(i.wire_copper, 2), [i.plate_copper, i.wirecutter], e],
	[Item.of(i.wire_iron, 2), [i.plate_iron, i.wirecutter], e],
	[Item.of(i.wire_gold, 2), [i.plate_gold, i.wirecutter], e],
	[Item.of(i.wire_zinc, 2), [i.plate_zinc, i.wirecutter], e],
	[Item.of(i.wire_aluminum, 2), [i.plate_aluminum, i.wirecutter], e],
	[Item.of(i.wire_lead, 2), [i.plate_lead, i.wirecutter], e],
	[Item.of(i.wire_silver, 2), [i.plate_silver, i.wirecutter], e],
	[Item.of(i.wire_nickel, 2), [i.plate_nickel, i.wirecutter], e],
]);

// ---------- Wire rolling ----------
R.bulk('createRolling', [
	[Item.of(i.wire_copper, 4), i.plate_copper, e],
	[Item.of(i.wire_iron, 4), i.plate_iron, e],
	[Item.of(i.wire_gold, 4), i.plate_gold, e],
	[Item.of(i.wire_zinc, 4), i.plate_zinc, e],
	[Item.of(i.wire_aluminum, 4), i.plate_aluminum, e],
	[Item.of(i.wire_lead, 4), i.plate_lead, e],
	[Item.of(i.wire_silver, 4), i.plate_silver, e],
	[Item.of(i.wire_nickel, 4), i.plate_nickel, e],
]);

// ---------- Wire pressing ----------
R.bulk('ieMetalPress', [
	[Item.of(i.wire_copper, 4), i.plate_copper, i.mold_wire, e],
	[Item.of(i.wire_iron, 4), i.plate_iron, i.mold_wire, e],
	[Item.of(i.wire_gold, 4), i.plate_gold, i.mold_wire, e],
	[Item.of(i.wire_zinc, 4), i.plate_zinc, i.mold_wire, e],
	[Item.of(i.wire_aluminum, 4), i.plate_aluminum, i.mold_wire, e],
	[Item.of(i.wire_lead, 4), i.plate_lead, i.mold_wire, e],
	[Item.of(i.wire_silver, 4), i.plate_silver, i.mold_wire, e],
	[Item.of(i.wire_nickel, 4), i.plate_nickel, i.mold_wire, e],
]);

// ---------- Wire coils ----------
const COIL_METALS = ['copper', 'iron', 'gold', 'zinc', 'aluminum', 'lead', 'nickel', 'silver', 'brass', 'constantan', 'steel', 'electrum'];
// Wire coil — crafting table: 4 metal wire + 1 spool -> 4 wire coils
R.bulk('shaped', COIL_METALS.map(m =>
	[Item.of(i['wire_coil_' + m], 4), [' W ', 'WSW', ' W '], { W: i['wire_' + m], S: i.spool }, e, m + '_wire_coil']
));
// Wire coil — sequenced assembly on a spool: deploy metal wire x3 -> 4 wire coils
R.bulk('createSequencedAssembly', COIL_METALS.map(m => {
	const inc = R.incomplete('coil_' + m);
	return [Item.of(i['wire_coil_' + m], 4), i.spool,
		[e.recipes.create.deploying(inc, [inc, i['wire_' + m]])],
		['coil_' + m], e, m + '_wire_coil_sequenced_assembly', 3];
}));
// Wire coil — Engineer's Workbench blueprint: 3 wire + 1 spool -> 6 coil
R.bulk('ieBlueprint', COIL_METALS.map(m =>
	[Item.of(i['wire_coil_' + m], 8), [Item.of(i['wire_' + m], 6), i.spool], 'electricalcomponents', e, m + '_wire_coil']
));
// Wire coil — disassembly blueprint: wire cutters + 6 wire coils -> 3 wire (spool not returned;
// exactly reverses the workbench blueprint above). The wirecutter is a tool: IE's
// WirecutterItem.getCraftingRemainingItem hands it back with 1 durability spent per craft.
R.bulk('ieBlueprint', COIL_METALS.map(m =>
	[Item.of(i['wire_' + m], 3), [Item.of(i['wire_coil_' + m],4)], 'disassembly_wire_coils', e, m + '_wire_coil_disassembly']
));

// ---------- Coil blocks ----------
// Coil block — crafting table: 8 wire coils around an iron rod -> 1 coil block
R.bulk('shaped', COIL_METALS.map(m =>
	[Item.of(i['coil_block_' + m], 1), ['CCC', 'CRC', 'CCC'], { C: i['wire_coil_' + m], R: i.rod_iron }, e, m + '_coil_block']
));
// Coil block — sequenced assembly on an iron rod: deploy wire coil x6 -> 1 coil block
R.bulk('createSequencedAssembly', COIL_METALS.map(m => {
	const inc = R.incomplete('coil_block_' + m);
	return [Item.of(i['coil_block_' + m], 1), i.rod_iron,
		[e.recipes.create.deploying(inc, [inc, i['wire_coil_' + m]])],
		['coil_block_' + m], e, m + '_coil_block_sequenced_assembly', 6];
}));
// Coil block — Engineer's Workbench blueprint: 4 wire coils + 1 iron rod -> 1 coil block
R.bulk('ieBlueprint', COIL_METALS.map(m =>
	[Item.of(i['coil_block_' + m], 1), [Item.of(i['wire_coil_' + m], 4), i.rod_iron], 'electricalcomponents', e, m + '_coil_block']
));
// Coil block — disassembly blueprint: wire cutters + 1 coil block -> 4 wire coils (iron rod not
// returned; exactly reverses the workbench blueprint above). Wirecutter spends 1 durability/craft.
R.bulk('ieBlueprint', COIL_METALS.map(m =>
	[Item.of(i['wire_coil_' + m], 4), [i['coil_block_' + m]], 'disassembly_coil_blocks', e, m + '_coil_block_disassembly']
));

// ---------- HV Coil (electrical steel wire coil; i.wirecoil_hv / i.coil_block_hv are
// defined in item_dict.js, distinct from the coil_steel structural rope + coil_block_steel
// produced by the generic COIL_METALS loop above) ----------
// Crafting table: steel wire top/bottom + aluminum wire either side of a spool -> 4 HV coils
R.shaped(Item.of(i.wirecoil_hv, 4), [' T ', 'ASA', ' T '], { T: i.wire_steel, A: i.wire_aluminum, S: i.spool }, e, 'wirecoil_hv')
// Engineer's Workbench blueprint: 3 steel wire + 3 aluminum wire + 1 spool -> 8 HV coils
R.ieBlueprint(Item.of(i.wirecoil_hv, 8), [Item.of(i.spool, 1), Item.of(i.wire_steel, 3), Item.of(i.wire_aluminum, 3)], 'electricalcomponents', e, 'wirecoil_hv')
// Sequenced assembly on a spool: deploy steel wire then aluminum wire -> 4 HV coils
const incWirecoilHv = R.incomplete('wirecoil_hv');
R.createSequencedAssembly(Item.of(i.wirecoil_hv, 8), i.spool, [
	e.recipes.create.deploying(incWirecoilHv, [incWirecoilHv, i.wire_steel]),
	e.recipes.create.deploying(incWirecoilHv, [incWirecoilHv, i.wire_aluminum])
	], ['wirecoil_hv'], e, 'wirecoil_hv_sequenced_assembly', 3)
// Disassembly blueprints: wire cutters + 8 HV coils -> 3 wire of one metal (spool not
// returned; each recipe reverses one of the two materials consumed by the blueprint above).
R.ieBlueprint(Item.of(i.wire_steel, 3), [Item.of(i.wirecoil_hv, 8)], 'disassembly_wire_coils', e, 'wirecoil_hv_disassembly_steel')
R.ieBlueprint(Item.of(i.wire_aluminum, 3), [Item.of(i.wirecoil_hv, 8)], 'disassembly_wire_coils', e, 'wirecoil_hv_disassembly_aluminum')

// ---------- HV Coil block ----------
// Mirrors the generic coil-block recipes above, swapped to the HV wire coil / coil_block_hv.
R.shaped(Item.of(i.coil_block_hv, 1), ['CCC', 'CRC', 'CCC'], { C: i.wirecoil_hv, R: i.rod_iron }, e, 'coil_block_hv')
const incCoilBlockHv = R.incomplete('coil_block_hv');
R.createSequencedAssembly(Item.of(i.coil_block_hv, 1), i.rod_iron, [
	e.recipes.create.deploying(incCoilBlockHv, [incCoilBlockHv, i.wirecoil_hv])
	], ['coil_block_hv'], e, 'coil_block_hv_sequenced_assembly', 6)
R.ieBlueprint(Item.of(i.coil_block_hv, 1), [Item.of(i.wirecoil_hv, 4), i.rod_iron], 'electricalcomponents', e, 'coil_block_hv')
R.ieBlueprint(Item.of(i.wirecoil_hv, 4), [i.wirecutter, i.coil_block_hv], 'disassembly_coil_blocks', e, 'coil_block_hv_disassembly')

console.log('The metal base event has finished!')
})
