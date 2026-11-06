// Creative Equipment; moved from kubejs/server_scripts/equipment/archery.js on 2026-11-04 (creative_split.md 7.6, wave 7), rows unchanged.
// Recipe ids are creativeequipment:<type>/<suffix> (global.CEQ.recipes).

ServerEvents.recipes(e => {
global.CEQ.recipes(() => {

console.log('Hello! The archery event has started!')

const R = global.NCH
var f = global.fluid
var i = global.item;

// ============================================================
// Removals
// ============================================================
R.bulk('remove', [
	[{ id: 'quark:tools/crafting/torch_arrow' }, e],
	[{ id: 'nock_enough_arrows:fletching_arrow' }, e],
	[{ id: 'nock_enough_arrows:quiver' }, e],
	[{ id: 'nock_enough_arrows:torch_arrow' }, e],
	[{ id: 'nock_enough_arrows:blossom_arrow' }, e],
	[{ id: 'nock_enough_arrows:ink_arrow' }, e],
	[{ id: 'nock_enough_arrows:luminous_arrow' }, e],
	[{ id: 'nock_enough_arrows:slime_arrow' }, e],
	[{ id: 'nock_enough_arrows:echoing_arrow' }, e],
	[{ id: 'nock_enough_arrows:whistling_arrow' }, e],
	[{ id: 'nock_enough_arrows:repulsive_arrow' }, e],
	[{ id: 'nock_enough_arrows:teleportation_arrow' }, e],
	[{ id: 'nock_enough_arrows:seeker_arrow' }, e],
	[{ id: 'nock_enough_arrows:explosive_arrow' }, e],
	[{ id: 'nock_enough_arrows:pufferfish_arrow' }, e],
	[{ id: 'nock_enough_arrows:party_arrow' }, e],
	[{ id: 'nock_enough_arrows:prismarine_arrow' }, e],
	[{ id: 'nock_enough_arrows:message_arrow' }, e],
	[{ id: 'nock_enough_arrows:reinforced_arrow' }, e],
	[{ id: 'nock_enough_arrows:drill_arrow' }, e],
	[{ id: 'nock_enough_arrows:dousing_arrow' }, e],
	[{ id: 'nock_enough_arrows:paint_arrow' }, e],
	[{ id: 'nock_enough_arrows:hookshot_arrow' }, e],
	[{ id: 'nock_enough_arrows:split_arrow' }, e],
	[{ id: 'nock_enough_arrows:redstone_torch_arrow' }, e],
	[{ id: 'nock_enough_arrows:soul_torch_arrow' }, e],
]);

// ============================================================
// Materials
// ============================================================

//Fletching cutting
R.fdCutting(i.feather, i.any_knife, Item.of(i.arrow_fletching, 2), e);
//Emu feather to feather
R.fdCutting(i.emu_feather, i.any_knife, Item.of(i.feather, 2), e);

// ============================================================
// Arrow
// ============================================================
//Fletching efficiency
R.fletching(i.flint, i.arrow_shaft, i.any_fletching, { item: i.arrow, count: 6 }, e);
//IE Blueprint
R.ieBlueprint({ item: i.arrow, count: 6 }, [i.flint, i.arrow_shaft, i.any_fletching], 'archery', e, 'arrow');

// ============================================================
// Spectral Arrow
// ============================================================
//Fletching efficiency
R.fletching(i.glowstone, i.arrow, i.glowstone, { item: i.spectral_arrow, count: 2 }, e);
//IE Blueprint
R.ieBlueprint({ item: i.spectral_arrow, count: 2 }, [i.glowstone, i.arrow, i.glowstone], 'archery', e, 'spectral_arrow');

// ============================================================
// Shark Arrow
// ============================================================
//Fletching efficiency
R.fletching(i.shark_tooth, i.arrow_shaft, i.kelp, { item: i.shark_tooth_arrow, count: 10 }, e);
//IE Blueprint
R.ieBlueprint({ item: i.shark_tooth_arrow, count: 10 }, [i.shark_tooth, i.arrow_shaft, i.kelp], 'archery', e, 'shark_tooth_arrow');

// ============================================================
// Torch Arrow
// ============================================================
//Smelt
R.bulk('smelting', [
	[i.torch_arrow, i.arrow, e],
]);
//Campfire cook
R.campfireCooking(i.torch_arrow,i.arrow,e);
//Crafting
R.bulk('shapeless', [
	[i.torch_arrow,[i.flint_and_steel, i.arrow],e,null,[i.flint_and_steel, 1]],
	[Item.of(i.torch_arrow, 3),[Item.of(i.arrow, 3), i.torch],e],
]);
//Deploying
R.createDeploying(i.torch_arrow, [i.arrow, i.flint_and_steel], e, null, i.flint_and_steel);
//Fletching
R.fletching(i.coal, i.wooden_arrow_shaft, i.any_fletching, { item: i.torch_arrow, count: 4 }, e);
//IE Blueprint
R.ieBlueprint({ item: i.torch_arrow, count: 4 }, [i.coal, i.wooden_arrow_shaft, i.any_fletching], 'archery', e, 'torch_arrow');

// ============================================================
// Redstone Torch Arrow
// ============================================================
//Fletching
R.fletching(i.redstone_torch, i.arrow_shaft, i.any_fletching, { item: i.redstone_torch_arrow, count: 2 }, e);
//IE Blueprint
R.ieBlueprint({ item: i.redstone_torch_arrow, count: 2 }, [i.redstone_torch, i.arrow_shaft, i.any_fletching], 'archery', e, 'redstone_torch_arrow');

// ============================================================
// Soul Torch Arrow
// ============================================================
//Fletching
R.fletching(i.soul_torch, i.arrow_shaft, i.any_fletching, { item: i.soul_torch_arrow, count: 2 }, e);
//IE Blueprint
R.ieBlueprint({ item: i.soul_torch_arrow, count: 2 }, [i.soul_torch, i.arrow_shaft, i.any_fletching], 'archery', e, 'soul_torch_arrow');

// ============================================================
// Blossom Arrow
// ============================================================
//Fletching
R.fletching(i.bonemeal, i.wooden_arrow_shaft, i.any_fletching, { item: i.blossom_arrow, count: 3 }, e);
//IE Blueprint
R.ieBlueprint({ item: i.blossom_arrow, count: 3 }, [i.bonemeal, i.wooden_arrow_shaft, i.any_fletching], 'archery', e, 'blossom_arrow');

// ============================================================
// Message Arrow
// ============================================================
//Fletching
R.fletching(i.paper, i.arrow_shaft, i.any_fletching, { item: i.message_arrow, count: 2 }, e);
//IE Blueprint
R.ieBlueprint({ item: i.message_arrow, count: 2 }, [i.paper, i.arrow_shaft, i.any_fletching], 'archery', e, 'message_arrow');

// ============================================================
// Ink Arrow
// ============================================================
//Fletching
R.fletching(i.ink_sac, i.arrow_shaft, i.any_fletching, { item: i.ink_arrow, count: 3 }, e);
//IE Blueprint
R.ieBlueprint({ item: i.ink_arrow, count: 3 }, [i.ink_sac, i.arrow_shaft, i.any_fletching], 'archery', e, 'ink_arrow');

// ============================================================
// Luminous Arrow
// ============================================================
//Fletching
R.fletching(i.glow_ink, i.arrow_shaft, i.any_fletching, { item: i.luminous_arrow, count: 3 }, e);
//IE Blueprint
R.ieBlueprint({ item: i.luminous_arrow, count: 3 }, [i.glow_ink, i.arrow_shaft, i.any_fletching], 'archery', e, 'luminous_arrow');

// ============================================================
// Slime Arrow
// ============================================================
//Fletching
R.fletching(i.slime, i.arrow_shaft, i.any_fletching, { item: i.slime_arrow, count: 3 }, e);
//IE Blueprint
R.ieBlueprint({ item: i.slime_arrow, count: 3 }, [i.slime, i.arrow_shaft, i.any_fletching], 'archery', e, 'slime_arrow');

// ============================================================
// Echoing Arrow
// ============================================================
//Fletching
R.fletching(i.echo_shard, i.light_arrow_shaft, i.any_fletching, { item: i.echoing_arrow, count: 2 }, e);
//IE Blueprint
R.ieBlueprint({ item: i.echoing_arrow, count: 2 }, [i.echo_shard, i.light_arrow_shaft, i.any_fletching], 'archery', e, 'echoing_arrow');

// ============================================================
// Whistling Arrow
// ============================================================
//Fletching
R.fletching(i.goat_horn, i.arrow_shaft, i.any_fletching, { item: i.whistling_arrow, count: 8 }, e);
//IE Blueprint
R.ieBlueprint({ item: i.whistling_arrow, count: 8 }, [i.goat_horn, i.arrow_shaft, i.any_fletching], 'archery', e, 'whistling_arrow');

// ============================================================
// Repulsive Arrow
// ============================================================
//Fletching
R.fletching(i.poisonous_potato, i.arrow_shaft, i.any_fletching, { item: i.repulsive_arrow, count: 2 }, e);
//IE Blueprint
R.ieBlueprint({ item: i.repulsive_arrow, count: 2 }, [i.poisonous_potato, i.arrow_shaft, i.any_fletching], 'archery', e, 'repulsive_arrow');

// ============================================================
// Teleportation Arrow
// ============================================================
//Fletching
R.fletching(i.ender_pearl, i.quick_arrow_shaft, i.any_fletching, { item: i.teleportation_arrow, count: 2 }, e);
//IE Blueprint
R.ieBlueprint({ item: i.teleportation_arrow, count: 2 }, [i.ender_pearl, i.quick_arrow_shaft, i.any_fletching], 'archery', e, 'teleportation_arrow');

// ============================================================
// Seeker Arrow
// ============================================================
//Fletching
R.fletching(i.ender_eye, i.light_arrow_shaft, i.any_fletching, { item: i.seeker_arrow, count: 3 }, e);
//IE Blueprint
R.ieBlueprint({ item: i.seeker_arrow, count: 3 }, [i.ender_eye, i.light_arrow_shaft, i.any_fletching], 'archery', e, 'seeker_arrow');

// ============================================================
// Explosive Arrow
// ============================================================
//Fletching
R.fletching(i.tnt, i.blazing_arrow_shaft, i.any_fletching, { item: i.explosive_arrow, count: 4 }, e);
//IE Blueprint
R.ieBlueprint({ item: i.explosive_arrow, count: 4 }, [i.tnt, i.blazing_arrow_shaft, i.any_fletching], 'archery', e, 'explosive_arrow');

// ============================================================
// Pufferfish Arrow
// ============================================================
//Fletching
R.fletching(i.pufferfish, i.arrow_shaft, i.any_fletching, { item: i.pufferfish_arrow, count: 1 }, e);
//IE Blueprint
R.ieBlueprint({ item: i.pufferfish_arrow, count: 1 }, [i.pufferfish, i.arrow_shaft, i.any_fletching], 'archery', e, 'pufferfish_arrow');

// ============================================================
// Prismarine Arrow
// ============================================================
//Fletching
R.fletching(i.prismarine_shard, i.metal_arrow_shaft, i.any_fletching, { item: i.prismarine_arrow, count: 6 }, e);
//IE Blueprint
R.ieBlueprint({ item: i.prismarine_arrow, count: 6 }, [i.prismarine_shard, i.metal_arrow_shaft, i.any_fletching], 'archery', e, 'prismarine_arrow');

// ============================================================
// Party Arrow
// ============================================================
//Fletching
R.fletching(i.firework, i.arrow_shaft, i.any_fletching, { item: i.party_arrow, count: 3 }, e);
//IE Blueprint
R.ieBlueprint({ item: i.party_arrow, count: 3 }, [i.firework, i.arrow_shaft, i.any_fletching], 'archery', e, 'party_arrow');

// ============================================================
// Reinforced Arrow
// ============================================================
//Fletching
R.fletching(i.obsidian, i.rod_steel, i.any_fletching, { item: i.reinforced_arrow, count: 6 }, e);
R.fletching(i.dragon_scale, i.rod_steel, i.any_fletching, { item: i.reinforced_arrow, count: 32 }, e);

//IE Blueprint
R.ieBlueprint({ item: i.reinforced_arrow, count: 6 }, [i.obsidian, i.rod_steel, i.any_fletching], 'archery', e, 'reinforced_arrow');
R.ieBlueprint({ item: i.reinforced_arrow, count: 32 }, [i.dragon_scale, i.rod_steel, i.any_fletching], 'archery', e, 'reinforced_arrow_dragon_scale');

// ============================================================
// Hookshot Arrow
// ============================================================
//Fletching
R.fletching(i.lead, i.arrow_shaft, i.any_fletching, { item: i.hookshot_arrow, count: 3 }, e);
//IE Blueprint
R.ieBlueprint({ item: i.hookshot_arrow, count: 3 }, [i.lead, i.arrow_shaft, i.any_fletching], 'archery', e, 'hookshot_arrow');

// ============================================================
// Paint Arrow
// ============================================================
//Fletching
R.fletching(i.white_wool, i.arrow_shaft, i.any_fletching, { item: i.paint_arrow, count: 6 }, e);
//IE Blueprint
R.ieBlueprint({ item: i.paint_arrow, count: 6 }, [i.white_wool, i.arrow_shaft, i.any_fletching], 'archery', e, 'paint_arrow');

// ============================================================
// Dousing Arrow
// ============================================================
//Fletching
R.fletching(i.water_bottle, i.metal_arrow_shaft, i.any_fletching, { item: i.dousing_arrow, count: 3 }, e);
//IE Blueprint
R.ieBlueprint({ item: i.dousing_arrow, count: 3 }, [i.water_bottle, i.metal_arrow_shaft, i.any_fletching], 'archery', e, 'dousing_arrow');

// ============================================================
// Drill Arrow
// ============================================================
//Fletching
R.fletching(i.drillhead_iron, i.heavy_arrow_shaft, i.any_fletching, { item: i.drill_arrow, count: 32 }, e);
R.fletching(i.drillhead_steel, i.heavy_arrow_shaft, i.any_fletching, { item: i.drill_arrow, count: 64 }, e);
//IE Blueprint
R.ieBlueprint({ item: i.drill_arrow, count: 32 }, [i.drillhead_iron, i.heavy_arrow_shaft, i.any_fletching], 'archery', e, 'drill_arrow');
R.ieBlueprint({ item: i.drill_arrow, count: 64 }, [i.drillhead_steel, i.heavy_arrow_shaft, i.any_fletching], 'archery', e, 'drill_arrow_steel');

// ============================================================
// Split Arrow
// ============================================================
//Shaped
R.shaped(Item.of(i.split_arrow, 2),['a a', 'awa', 'a a'],{a: i.arrow,w: [i.wire_electrum, i.wire_brass]},e);
//IE Blueprint
R.ieBlueprint({ item: i.split_arrow, count: 2 }, [i.arrow, i.arrow, i.arrow, i.arrow, i.wire_electrum], 'archery', e, 'split_arrow_electrum');
R.ieBlueprint({ item: i.split_arrow, count: 2 }, [i.arrow, i.arrow, i.arrow, i.arrow, i.wire_brass], 'archery', e, 'split_arrow_brass');

// ============================================================
// Balloon Arrow
// ============================================================
R.bulk('shapeless', [
	[i.balloon_arrow,[i.balloon, i.arrow],e]
]);

// ============================================================
// Related
// ============================================================

//Quiver recipe
R.shaped(Item.of(i.quiver, 1), ['TCT', 'TCT', ' T '], {T: [i.leather, i.hemp_fabric, i.canvas], C: i.ingot_copper}, e)
//Swap rabbit hide in slingshot
R.replaceInput({ id: 'supplementaries:slingshot' },i.rabbit_hide, [i.leather, i.hemp_fabric, i.canvas], e)

console.log('The archery event has finished!')
})
})
