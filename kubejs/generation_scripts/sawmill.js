// TODO(dynamic updates): move this off the manual pipeline, like the worldgen scripts were
// (kubejs/server_scripts/worldgen/). Today: `node sawmill.js` -> recipes/sawmill/*.json -> copied by
// hand into patcherMod/src/main/resources/data/creativeengineering/recipes/sawmill/ -> jar rebuild.
// Plan: an NCH helper that adds these recipes as raw JSON from ServerEvents.highPriorityData
// (event.add), which bypasses kubejs-immersive-engineering's broken secondaries schema the same way
// the jar's datapack JSONs do, driven by the species table in server_scripts/wood/wood_types.js so
// every item comes from item_dict. Then delete this script, recipes/sawmill/ and the 211 jar copies.
// Verify in game that the blade/secondaries survive kjs-ie's recipe interception.
const fs = require('fs');
const path = require('path');

// Maps a sawmill output item id to the sawblade "profile" that selects it.
// A profiled recipe only runs when the precision sawblade in the machine carries
// {Profile:"<value>"} NBT; '' means the profile-less default for that input
// (what a plain immersiveengineering:sawblade produces). Order matters — the more
// specific "vertical_*" checks must come before the plain slab/planks checks.
function profileFor(outputId) {
	const id = String(outputId);
	if (/vertical_slab/.test(id)) return 'vertical_slab';
	if (/vertical_[a-z_]*planks/.test(id)) return 'vertical_planks';
	if (/_slab$/.test(id)) return '';               // default: planks -> slab
	if (/_stairs$/.test(id)) return 'stairs';
	if (/_fence_gate$/.test(id)) return 'fence_gate';
	if (/_fence$/.test(id)) return 'fence';
	if (/_door$/.test(id)) return 'door';
	if (/_trapdoor$/.test(id)) return 'trapdoor';
	if (/_pressure_plate$/.test(id)) return 'pressure_plate';
	if (/_button$/.test(id)) return 'button';
	if (/_sign$/.test(id)) return 'sign';
	if (/railing/.test(id)) return 'railing';
	if (/ladder/.test(id)) return 'ladder';
	return '';                                       // unknown -> treat as default
}

function simpleSawmillSingleInputMultiOutput(input, output) {
	for (let i = 0; i < output.length; i++) {
		console.log(`building ${output[i]}`)
		if (Array.isArray(output[i])) {
			console.log(`detected array, ${input}, ${output[i][1]}`)
			createSawmillSimpleRecipeFile(input, output[i][0], output[i][1], 800);
		}
		else {
			console.log(`normal build, ${input}, ${output[i]}`)
			createSawmillSimpleRecipeFile(input, output[i], 1, 800);
		}
	}
}

function createSawmillSimpleRecipeFile(input, output, oCount = 1, energy = 800, secondary = "immersiveengineering:dust_wood", blade = undefined) {
  const bladeProfile = blade === undefined ? profileFor(output) : blade;
  const recipe = {
    "type": "immersiveengineering:sawmill",
    "energy": energy,
    "input": {
      "item": input
    },
    "result": {
      "item": output,
      "count": oCount
    },
    "secondaries": [
      {
        "output": {
          "item": secondary
        },
        "stripping": false
		    }
		  ]
		};

	if (bladeProfile) recipe.blade = bladeProfile;

	nOutput = output.replace(/\//g, "_");
	console.log(nOutput);

  const fileName = `${input.split(":")[1]}-${nOutput.split(":")[1]}.json`;
  const filePath = path.join(__dirname, 'recipes/sawmill', fileName);

  fs.writeFileSync(filePath, JSON.stringify(recipe, null, 2));

  console.log(`Recipe file created at: ${filePath}`);
}

function createSawmillRecipeFile(input, output, oCount = 1, energy = 1600, secondaries, stripped) {
  
  const secondariesArray = secondaries.map(s => ({
    output: { item: s[0] },
    stripping: s[1]
  }));
  
  // Only add the stripped field if it's passed and not false
  const recipe = {
    "type": "immersiveengineering:sawmill",
    "energy": energy,
    "input": {
      "item": input
    },
    "result": {
      "count": oCount,
      "item": output
    },
    "secondaries": secondariesArray,
  };

  // Conditionally add stripped if it's truthy
  if (stripped) {
    recipe.stripped = { "item": stripped };
  }

  const fileName = `${input.split(":")[1]}-${output.split(":")[1]}.json`;
  const filePath = path.join(__dirname, 'recipes/sawmill', fileName);

  fs.writeFileSync(filePath, JSON.stringify(recipe, null, 2));

  console.log(`Complex Recipe file created at: ${filePath}`);
}

sawdust = "immersiveengineering:dust_wood"

createSawmillRecipeFile('minecraft:oak_log', 'minecraft:oak_planks', 6, 1600, [["immersive_weathering:oak_bark", true],[sawdust, false]],'minecraft:stripped_oak_log');
createSawmillRecipeFile('minecraft:oak_wood', 'minecraft:oak_planks', 6, 1600, [["immersive_weathering:oak_bark", true],[sawdust, false]],'minecraft:stripped_oak_wood');
createSawmillRecipeFile('minecraft:stripped_oak_log', 'minecraft:oak_planks', 6, 800, [[sawdust, false]], false);
createSawmillRecipeFile('minecraft:stripped_oak_wood', 'minecraft:oak_planks', 6, 800, [[sawdust, false]],false);
createSawmillSimpleRecipeFile('minecraft:oak_planks', 'minecraft:oak_stairs', 1, 800);
createSawmillSimpleRecipeFile('minecraft:oak_planks', 'quark:oak_vertical_slab', 2, 800);
createSawmillSimpleRecipeFile('minecraft:oak_planks', 'quark:vertical_oak_planks', 1, 800);
createSawmillSimpleRecipeFile('minecraft:oak_planks', 'minecraft:oak_fence', 1, 800);
createSawmillSimpleRecipeFile('minecraft:oak_planks', 'minecraft:oak_fence_gate', 1, 800);
createSawmillSimpleRecipeFile('minecraft:oak_planks', 'minecraft:oak_door', 1, 800);
createSawmillSimpleRecipeFile('minecraft:oak_planks', 'minecraft:oak_trapdoor', 1, 800);
createSawmillSimpleRecipeFile('minecraft:oak_planks', 'minecraft:ladder', 1, 800);
createSawmillSimpleRecipeFile('minecraft:oak_planks', 'minecraft:oak_button', 1, 800);
createSawmillSimpleRecipeFile('minecraft:oak_planks', 'minecraft:oak_pressure_plate', 1, 800);
createSawmillSimpleRecipeFile('minecraft:oak_planks', 'minecraft:oak_sign', 1, 800);
createSawmillSimpleRecipeFile('minecraft:oak_planks', 'architects_palette:oak_railing', 1, 800);

createSawmillRecipeFile('minecraft:spruce_log', 'minecraft:spruce_planks', 6, 1600, [["immersive_weathering:spruce_bark", true],[sawdust, false]],'minecraft:stripped_spruce_log');
createSawmillRecipeFile('minecraft:spruce_wood', 'minecraft:spruce_planks', 6, 1600, [["immersive_weathering:spruce_bark", true],[sawdust, false]],'minecraft:stripped_spruce_wood');
createSawmillRecipeFile('minecraft:stripped_spruce_log', 'minecraft:spruce_planks', 6, 800, [[sawdust, false]], false);
createSawmillRecipeFile('minecraft:stripped_spruce_wood', 'minecraft:spruce_planks', 6, 800, [[sawdust, false]],false);
createSawmillSimpleRecipeFile('minecraft:spruce_planks', 'minecraft:spruce_stairs', 1, 800);
createSawmillSimpleRecipeFile('minecraft:spruce_planks', 'quark:spruce_vertical_slab', 2, 800);
createSawmillSimpleRecipeFile('minecraft:spruce_planks', 'quark:vertical_spruce_planks', 1, 800);
createSawmillSimpleRecipeFile('minecraft:spruce_planks', 'minecraft:spruce_fence', 1, 800);
createSawmillSimpleRecipeFile('minecraft:spruce_planks', 'minecraft:spruce_fence_gate', 1, 800);
createSawmillSimpleRecipeFile('minecraft:spruce_planks', 'minecraft:spruce_door', 1, 800);
createSawmillSimpleRecipeFile('minecraft:spruce_planks', 'minecraft:spruce_trapdoor', 1, 800);
createSawmillSimpleRecipeFile('minecraft:spruce_planks', 'quark:spruce_ladder', 1, 800);
createSawmillSimpleRecipeFile('minecraft:spruce_planks', 'minecraft:spruce_button', 1, 800);
createSawmillSimpleRecipeFile('minecraft:spruce_planks', 'minecraft:spruce_pressure_plate', 1, 800);
createSawmillSimpleRecipeFile('minecraft:spruce_planks', 'minecraft:spruce_sign', 1, 800);
createSawmillSimpleRecipeFile('minecraft:spruce_planks', 'architects_palette:spruce_railing', 1, 800);

createSawmillRecipeFile('minecraft:birch_log', 'minecraft:birch_planks', 6, 1600, [["immersive_weathering:birch_bark", true],[sawdust, false]], 'minecraft:stripped_birch_log');
createSawmillRecipeFile('minecraft:birch_wood', 'minecraft:birch_planks', 6, 1600, [["immersive_weathering:birch_bark", true],[sawdust, false]], 'minecraft:stripped_birch_wood');
createSawmillRecipeFile('minecraft:stripped_birch_log', 'minecraft:birch_planks', 6, 800, [[sawdust, false]], false);
createSawmillRecipeFile('minecraft:stripped_birch_wood', 'minecraft:birch_planks', 6, 800, [[sawdust, false]],false);
createSawmillSimpleRecipeFile('minecraft:birch_planks', 'minecraft:birch_stairs', 1, 800);
createSawmillSimpleRecipeFile('minecraft:birch_planks', 'quark:birch_vertical_slab', 2, 800);
createSawmillSimpleRecipeFile('minecraft:birch_planks', 'quark:vertical_birch_planks', 1, 800);
createSawmillSimpleRecipeFile('minecraft:birch_planks', 'minecraft:birch_fence', 1, 800);
createSawmillSimpleRecipeFile('minecraft:birch_planks', 'minecraft:birch_fence_gate', 1, 800);
createSawmillSimpleRecipeFile('minecraft:birch_planks', 'minecraft:birch_door', 1, 800);
createSawmillSimpleRecipeFile('minecraft:birch_planks', 'minecraft:birch_trapdoor', 1, 800);
createSawmillSimpleRecipeFile('minecraft:birch_planks', 'quark:birch_ladder', 1, 800);
createSawmillSimpleRecipeFile('minecraft:birch_planks', 'minecraft:birch_button', 1, 800);
createSawmillSimpleRecipeFile('minecraft:birch_planks', 'minecraft:birch_pressure_plate', 1, 800);
createSawmillSimpleRecipeFile('minecraft:birch_planks', 'minecraft:birch_sign', 1, 800);
createSawmillSimpleRecipeFile('minecraft:birch_planks', 'architects_palette:birch_railing', 1, 800);

createSawmillRecipeFile('minecraft:dark_oak_log', 'minecraft:dark_oak_planks', 6, 1600, [["immersive_weathering:dark_oak_bark", true],[sawdust, false]],'minecraft:stripped_dark_oak_log');
createSawmillRecipeFile('minecraft:dark_oak_wood', 'minecraft:dark_oak_planks', 6, 1600, [["immersive_weathering:dark_oak_bark", true],[sawdust, false]],'minecraft:stripped_dark_oak_wood');
createSawmillRecipeFile('minecraft:stripped_dark_oak_log', 'minecraft:dark_oak_planks', 6, 800, [[sawdust, false]], false);
createSawmillRecipeFile('minecraft:stripped_dark_oak_wood', 'minecraft:dark_oak_planks', 6, 800, [[sawdust, false]],false);
createSawmillSimpleRecipeFile('minecraft:dark_oak_planks', 'minecraft:dark_oak_stairs', 1, 800);
createSawmillSimpleRecipeFile('minecraft:dark_oak_planks', 'quark:dark_oak_vertical_slab', 2, 800);
createSawmillSimpleRecipeFile('minecraft:dark_oak_planks', 'quark:vertical_dark_oak_planks', 1, 800);
createSawmillSimpleRecipeFile('minecraft:dark_oak_planks', 'minecraft:dark_oak_fence', 1, 800);
createSawmillSimpleRecipeFile('minecraft:dark_oak_planks', 'minecraft:dark_oak_fence_gate', 1, 800);
createSawmillSimpleRecipeFile('minecraft:dark_oak_planks', 'minecraft:dark_oak_door', 1, 800);
createSawmillSimpleRecipeFile('minecraft:dark_oak_planks', 'minecraft:dark_oak_trapdoor', 1, 800);
createSawmillSimpleRecipeFile('minecraft:dark_oak_planks', 'quark:dark_oak_ladder', 1, 800);
createSawmillSimpleRecipeFile('minecraft:dark_oak_planks', 'minecraft:dark_oak_button', 1, 800);
createSawmillSimpleRecipeFile('minecraft:dark_oak_planks', 'minecraft:dark_oak_pressure_plate', 1, 800);
createSawmillSimpleRecipeFile('minecraft:dark_oak_planks', 'minecraft:dark_oak_sign', 1, 800);
createSawmillSimpleRecipeFile('minecraft:dark_oak_planks', 'architects_palette:dark_oak_railing', 1, 800);

createSawmillRecipeFile('minecraft:jungle_log', 'minecraft:jungle_planks', 6, 1600, [["immersive_weathering:jungle_bark", true],[sawdust, false]], 'minecraft:stripped_jungle_log');
createSawmillRecipeFile('minecraft:jungle_wood', 'minecraft:jungle_planks', 6, 1600, [["immersive_weathering:jungle_bark", true],[sawdust, false]], 'minecraft:stripped_jungle_wood');
createSawmillRecipeFile('minecraft:stripped_jungle_log', 'minecraft:jungle_planks', 6, 800, [[sawdust, false]], false);
createSawmillRecipeFile('minecraft:stripped_jungle_wood', 'minecraft:jungle_planks', 6, 800, [[sawdust, false]],false);
createSawmillSimpleRecipeFile('minecraft:jungle_planks', 'minecraft:jungle_stairs', 1, 800);
createSawmillSimpleRecipeFile('minecraft:jungle_planks', 'quark:jungle_vertical_slab', 2, 800);
createSawmillSimpleRecipeFile('minecraft:jungle_planks', 'quark:vertical_jungle_planks', 1, 800);
createSawmillSimpleRecipeFile('minecraft:jungle_planks', 'minecraft:jungle_fence', 1, 800);
createSawmillSimpleRecipeFile('minecraft:jungle_planks', 'minecraft:jungle_fence_gate', 1, 800);
createSawmillSimpleRecipeFile('minecraft:jungle_planks', 'minecraft:jungle_door', 1, 800);
createSawmillSimpleRecipeFile('minecraft:jungle_planks', 'minecraft:jungle_trapdoor', 1, 800);
createSawmillSimpleRecipeFile('minecraft:jungle_planks', 'quark:jungle_ladder', 1, 800);
createSawmillSimpleRecipeFile('minecraft:jungle_planks', 'minecraft:jungle_button', 1, 800);
createSawmillSimpleRecipeFile('minecraft:jungle_planks', 'minecraft:jungle_pressure_plate', 1, 800);
createSawmillSimpleRecipeFile('minecraft:jungle_planks', 'minecraft:jungle_sign', 1, 800);
createSawmillSimpleRecipeFile('minecraft:jungle_planks', 'architects_palette:jungle_railing', 1, 800);

createSawmillRecipeFile('minecraft:acacia_log', 'minecraft:acacia_planks', 6, 1600, [["immersive_weathering:acacia_bark", true],[sawdust, false]], 'minecraft:stripped_acacia_log');
createSawmillRecipeFile('minecraft:acacia_wood', 'minecraft:acacia_planks', 6, 1600, [["immersive_weathering:acacia_bark", true],[sawdust, false]], 'minecraft:stripped_acacia_wood');
createSawmillRecipeFile('minecraft:stripped_acacia_log', 'minecraft:acacia_planks', 6, 800, [[sawdust, false]], false);
createSawmillRecipeFile('minecraft:stripped_acacia_wood', 'minecraft:acacia_planks', 6, 800, [[sawdust, false]],false);
createSawmillSimpleRecipeFile('minecraft:acacia_planks', 'minecraft:acacia_stairs', 1, 800);
createSawmillSimpleRecipeFile('minecraft:acacia_planks', 'quark:acacia_vertical_slab', 2, 800);
createSawmillSimpleRecipeFile('minecraft:acacia_planks', 'quark:vertical_acacia_planks', 1, 800);
createSawmillSimpleRecipeFile('minecraft:acacia_planks', 'minecraft:acacia_fence', 1, 800);
createSawmillSimpleRecipeFile('minecraft:acacia_planks', 'minecraft:acacia_fence_gate', 1, 800);
createSawmillSimpleRecipeFile('minecraft:acacia_planks', 'minecraft:acacia_door', 1, 800);
createSawmillSimpleRecipeFile('minecraft:acacia_planks', 'minecraft:acacia_trapdoor', 1, 800);
createSawmillSimpleRecipeFile('minecraft:acacia_planks', 'quark:acacia_ladder', 1, 800);
createSawmillSimpleRecipeFile('minecraft:acacia_planks', 'minecraft:acacia_button', 1, 800);
createSawmillSimpleRecipeFile('minecraft:acacia_planks', 'minecraft:acacia_pressure_plate', 1, 800);
createSawmillSimpleRecipeFile('minecraft:acacia_planks', 'minecraft:acacia_sign', 1, 800);
createSawmillSimpleRecipeFile('minecraft:acacia_planks', 'architects_palette:acacia_railing', 1, 800);

createSawmillRecipeFile('minecraft:mangrove_log', 'minecraft:mangrove_planks', 6, 1600, [["immersive_weathering:mangrove_bark", true],[sawdust, false]], 'minecraft:stripped_mangrove_log');
createSawmillRecipeFile('minecraft:mangrove_wood', 'minecraft:mangrove_planks', 6, 1600, [["immersive_weathering:mangrove_bark", true],[sawdust, false]], 'minecraft:stripped_mangrove_wood');
createSawmillRecipeFile('minecraft:stripped_mangrove_log', 'minecraft:mangrove_planks', 6, 800, [[sawdust, false]], false);
createSawmillRecipeFile('minecraft:stripped_mangrove_wood', 'minecraft:mangrove_planks', 6, 800, [[sawdust, false]], false);
createSawmillSimpleRecipeFile('minecraft:mangrove_planks', 'minecraft:mangrove_stairs', 1, 800);
createSawmillSimpleRecipeFile('minecraft:mangrove_planks', 'quark:mangrove_vertical_slab', 2, 800);
createSawmillSimpleRecipeFile('minecraft:mangrove_planks', 'quark:vertical_mangrove_planks', 1, 800);
createSawmillSimpleRecipeFile('minecraft:mangrove_planks', 'minecraft:mangrove_fence', 1, 800);
createSawmillSimpleRecipeFile('minecraft:mangrove_planks', 'minecraft:mangrove_fence_gate', 1, 800);
createSawmillSimpleRecipeFile('minecraft:mangrove_planks', 'minecraft:mangrove_door', 1, 800);
createSawmillSimpleRecipeFile('minecraft:mangrove_planks', 'minecraft:mangrove_trapdoor', 1, 800);
createSawmillSimpleRecipeFile('minecraft:mangrove_planks', 'quark:mangrove_ladder', 1, 800);
createSawmillSimpleRecipeFile('minecraft:mangrove_planks', 'minecraft:mangrove_button', 1, 800);
createSawmillSimpleRecipeFile('minecraft:mangrove_planks', 'minecraft:mangrove_pressure_plate', 1, 800);
createSawmillSimpleRecipeFile('minecraft:mangrove_planks', 'minecraft:mangrove_sign', 1, 800);
createSawmillSimpleRecipeFile('minecraft:mangrove_planks', 'architects_palette:mangrove_railing', 1, 800);

createSawmillRecipeFile('minecraft:crimson_stem', 'minecraft:crimson_planks', 6, 1600, [["immersive_weathering:crimson_scales", true],["minecraft:crimson_roots", false]], 'minecraft:stripped_crimson_stem');
createSawmillRecipeFile('minecraft:crimson_hyphae', 'minecraft:crimson_planks', 6, 1600, [["immersive_weathering:crimson_scales", true],["minecraft:crimson_roots", false]], 'minecraft:stripped_crimson_hyphae');
createSawmillRecipeFile('minecraft:stripped_crimson_stem', 'minecraft:crimson_planks', 6, 800, [["minecraft:crimson_roots", false]], false);
createSawmillRecipeFile('minecraft:stripped_crimson_hyphae', 'minecraft:crimson_planks', 6, 800, [["minecraft:crimson_roots", false]], false);
createSawmillSimpleRecipeFile('minecraft:crimson_planks', 'minecraft:crimson_stairs', 1, 800);
createSawmillSimpleRecipeFile('minecraft:crimson_planks', 'quark:crimson_vertical_slab', 2, 800);
createSawmillSimpleRecipeFile('minecraft:crimson_planks', 'quark:vertical_crimson_planks', 1, 800);
createSawmillSimpleRecipeFile('minecraft:crimson_planks', 'minecraft:crimson_fence', 1, 800);
createSawmillSimpleRecipeFile('minecraft:crimson_planks', 'minecraft:crimson_fence_gate', 1, 800);
createSawmillSimpleRecipeFile('minecraft:crimson_planks', 'minecraft:crimson_door', 1, 800);
createSawmillSimpleRecipeFile('minecraft:crimson_planks', 'minecraft:crimson_trapdoor', 1, 800);
createSawmillSimpleRecipeFile('minecraft:crimson_planks', 'quark:crimson_ladder', 1, 800);
createSawmillSimpleRecipeFile('minecraft:crimson_planks', 'minecraft:crimson_button', 1, 800);
createSawmillSimpleRecipeFile('minecraft:crimson_planks', 'minecraft:crimson_pressure_plate', 1, 800);
createSawmillSimpleRecipeFile('minecraft:crimson_planks', 'minecraft:crimson_sign', 1, 800);
createSawmillSimpleRecipeFile('minecraft:crimson_planks', 'architects_palette:crimson_railing', 1, 800);

createSawmillRecipeFile('minecraft:warped_stem', 'minecraft:warped_planks', 6, 1600, [["immersive_weathering:warped_scales", true],["minecraft:warped_roots", false]], 'minecraft:stripped_warped_stem');
createSawmillRecipeFile('minecraft:warped_hyphae', 'minecraft:warped_planks', 6, 1600, [["immersive_weathering:warped_scales", true],["minecraft:warped_roots", false]], 'minecraft:stripped_warped_hyphae');
createSawmillRecipeFile('minecraft:stripped_warped_stem', 'minecraft:warped_planks', 6, 800, [["minecraft:warped_roots", false]], false);
createSawmillRecipeFile('minecraft:stripped_warped_hyphae', 'minecraft:warped_planks', 6, 800, [["minecraft:warped_roots", false]], false);
createSawmillSimpleRecipeFile('minecraft:warped_planks', 'minecraft:warped_stairs', 1, 800);
createSawmillSimpleRecipeFile('minecraft:warped_planks', 'quark:warped_vertical_slab', 2, 800);
createSawmillSimpleRecipeFile('minecraft:warped_planks', 'quark:vertical_warped_planks', 1, 800);
createSawmillSimpleRecipeFile('minecraft:warped_planks', 'minecraft:warped_fence', 1, 800);
createSawmillSimpleRecipeFile('minecraft:warped_planks', 'minecraft:warped_fence_gate', 1, 800);
createSawmillSimpleRecipeFile('minecraft:warped_planks', 'minecraft:warped_door', 1, 800);
createSawmillSimpleRecipeFile('minecraft:warped_planks', 'minecraft:warped_trapdoor', 1, 800);
createSawmillSimpleRecipeFile('minecraft:warped_planks', 'quark:warped_ladder', 1, 800);
createSawmillSimpleRecipeFile('minecraft:warped_planks', 'minecraft:warped_button', 1, 800);
createSawmillSimpleRecipeFile('minecraft:warped_planks', 'minecraft:warped_pressure_plate', 1, 800);
createSawmillSimpleRecipeFile('minecraft:warped_planks', 'minecraft:warped_sign', 1, 800);
createSawmillSimpleRecipeFile('minecraft:warped_planks', 'architects_palette:warped_railing', 1, 800); 

ap = 'architects_palette';
apt = 'architects_palette:twisted';
twisted_logs = [`${apt}_log`, `${apt}_wood`];
twisted_bark = `immersive_weathering:${ap}/twisted_bark`
stripped_twisted_logs = [`${ap}:stripped_twisted_log`, `${ap}:stripped_twisted_wood`];
twisted_planks = `${apt}_planks`;
twisted_outputs = [[`${apt}_slab`,2],[`${apt}_vertical_slab`,2], `${apt}_stairs`, `everycomp:q/${ap}/vertical_twisted_planks`, `${apt}_door`, `${apt}_trapdoor`, `${apt}_fence`, `${apt}_fence_gate`, `${apt}_railing`, `${apt}_button`, `${apt}_pressure_plate`, `everycomp:q/${ap}/twisted_ladder`];

console.log(twisted_logs);
console.log(twisted_outputs);

createSawmillRecipeFile(twisted_logs[0], twisted_planks, 6, 1600, [[twisted_bark, true],[sawdust, false]],stripped_twisted_logs[0]);
createSawmillRecipeFile(twisted_logs[1], twisted_planks, 6, 1600, [[twisted_bark, true],[sawdust, false]],stripped_twisted_logs[1]);
createSawmillRecipeFile(stripped_twisted_logs[0], twisted_planks, 6, 800, [[sawdust, false]],false);
createSawmillRecipeFile(stripped_twisted_logs[1], twisted_planks, 6, 800, [[sawdust, false]],false);
simpleSawmillSingleInputMultiOutput(twisted_planks, twisted_outputs) 

qk = 'quark';
qka = `${qk}:ancient`;
ancient_logs = [`${qka}_log`, `${qka}_wood`];
ancient_bark = `immersive_weathering:${qk}/ancient_bark`
stripped_ancient_logs = [`${qk}:stripped_ancient_log`, `${qk}:stripped_ancient_wood`];
ancient_planks = `${qka}_planks`;
ancient_outputs = [[`${qka}_planks_slab`,2],[`${qka}_planks_vertical_slab`,2], `${qka}_planks_stairs`, `${qka}_sign`, `${qk}:vertical_ancient_planks`, `${qka}_door`, `${qka}_trapdoor`, `${qka}_fence`, `${qka}_fence_gate`, `everycomp:ap/${qk}/ancient_railing`, `${qka}_button`, `${qka}_pressure_plate`, `${qka}_ladder`];

console.log(ancient_logs);
console.log(ancient_outputs);

createSawmillRecipeFile(ancient_logs[0], ancient_planks, 6, 1600, [[ancient_bark, true],[sawdust, false]],stripped_ancient_logs[0]);
createSawmillRecipeFile(ancient_logs[1], ancient_planks, 6, 1600, [[ancient_bark, true],[sawdust, false]],stripped_ancient_logs[1]);
createSawmillRecipeFile(stripped_ancient_logs[0], ancient_planks, 6, 800, [[sawdust, false]],false);
createSawmillRecipeFile(stripped_ancient_logs[1], ancient_planks, 6, 800, [[sawdust, false]],false);
simpleSawmillSingleInputMultiOutput(ancient_planks, ancient_outputs); 

qkz = `${qk}:azalea`;
azalea_logs = [`${qkz}_log`, `${qkz}_wood`];
azalea_bark = `immersive_weathering:${qk}/azalea_bark`
stripped_azalea_logs = [`${qk}:stripped_azalea_log`, `${qk}:stripped_azalea_wood`];
azalea_planks = `${qkz}_planks`;
azalea_outputs = [[`${qkz}_planks_slab`,2],[`${qkz}_planks_vertical_slab`,2], `${qkz}_planks_stairs`, `${qkz}_sign`, `${qk}:vertical_azalea_planks`, `${qkz}_door`, `${qkz}_trapdoor`, `${qkz}_fence`, `${qkz}_fence_gate`, `everycomp:ap/${qk}/azalea_railing`, `${qkz}_button`, `${qkz}_pressure_plate`, `${qkz}_ladder`];

console.log(azalea_logs);
console.log(azalea_outputs);

createSawmillRecipeFile(azalea_logs[0], azalea_planks, 6, 1600, [[azalea_bark, true],[sawdust, false]],stripped_azalea_logs[0]);
createSawmillRecipeFile(azalea_logs[1], azalea_planks, 6, 1600, [[azalea_bark, true],[sawdust, false]],stripped_azalea_logs[1]);
createSawmillRecipeFile(stripped_azalea_logs[0], azalea_planks, 6, 800, [[sawdust, false]],false);
createSawmillRecipeFile(stripped_azalea_logs[1], azalea_planks, 6, 800, [[sawdust, false]],false);
simpleSawmillSingleInputMultiOutput(azalea_planks, azalea_outputs); 

qkb = `${qk}:blossom`;
blossom_logs = [`${qkb}_log`, `${qkb}_wood`];
blossom_bark = `immersive_weathering:${qk}/blossom_bark`
stripped_blossom_logs = [`${qk}:stripped_blossom_log`, `${qk}:stripped_blossom_wood`];
blossom_planks = `${qkb}_planks`;
blossom_outputs = [[`${qkb}_planks_slab`,2],[`${qkb}_planks_vertical_slab`,2], `${qkb}_planks_stairs`, `${qkb}_sign`, `${qk}:vertical_blossom_planks`, `${qkb}_door`, `${qkb}_trapdoor`, `${qkb}_fence`, `${qkb}_fence_gate`, `everycomp:ap/${qk}/blossom_railing`, `${qkb}_button`, `${qkb}_pressure_plate`, `${qkb}_ladder`];

console.log(blossom_logs);
console.log(blossom_outputs);

createSawmillRecipeFile(blossom_logs[0], blossom_planks, 6, 1600, [[blossom_bark, true],[sawdust, false]],stripped_blossom_logs[0]);
createSawmillRecipeFile(blossom_logs[1], blossom_planks, 6, 1600, [[blossom_bark, true],[sawdust, false]],stripped_blossom_logs[1]);
createSawmillRecipeFile(stripped_blossom_logs[0], blossom_planks, 6, 800, [[sawdust, false]],false);
createSawmillRecipeFile(stripped_blossom_logs[1], blossom_planks, 6, 800, [[sawdust, false]],false);
simpleSawmillSingleInputMultiOutput(blossom_planks, blossom_outputs);

/*
createSawmillRecipeFile('minecraft:oak_log', 'minecraft:oak_planks', 6, 1600, [["immersive_weathering:oak_bark", true],[sawdust, false]],'minecraft:stripped_oak_log');
createSawmillRecipeFile('minecraft:oak_wood', 'minecraft:oak_planks', 6, 1600, [["immersive_weathering:oak_bark", true],[sawdust, false]],'minecraft:stripped_oak_wood');
createSawmillRecipeFile('minecraft:stripped_oak_wood', 'minecraft:oak_planks', 6, 800, [[sawdust, false]],false);
createSawmillSimpleRecipeFile('minecraft:oak_planks', 'minecraft:oak_stairs', 1, 800);
createSawmillSimpleRecipeFile('minecraft:oak_planks', 'quark:oak_vertical_slab', 2, 800);
createSawmillSimpleRecipeFile('minecraft:oak_planks', 'quark:vertical_oak_planks', 1, 800);
createSawmillSimpleRecipeFile('minecraft:oak_planks', 'minecraft:oak_fence', 1, 800);
createSawmillSimpleRecipeFile('minecraft:oak_planks', 'minecraft:oak_fence_gate', 1, 800);
createSawmillSimpleRecipeFile('minecraft:oak_planks', 'minecraft:oak_door', 1, 800);
createSawmillSimpleRecipeFile('minecraft:oak_planks', 'minecraft:oak_trapdoor', 1, 800);
createSawmillSimpleRecipeFile('minecraft:oak_planks', 'minecraft:ladder', 1, 800);
createSawmillSimpleRecipeFile('minecraft:oak_planks', 'minecraft:oak_button', 1, 800);
createSawmillSimpleRecipeFile('minecraft:oak_planks', 'minecraft:oak_pressure_plate', 1, 800);
createSawmillSimpleRecipeFile('minecraft:oak_planks', 'minecraft:oak_sign', 1, 800);
createSawmillSimpleRecipeFile('minecraft:oak_planks', 'architects_palette:oak_railing', 1, 800);*/




