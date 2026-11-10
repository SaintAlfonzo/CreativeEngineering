// priority: 1000
// Must load before every other startup script: several read global.item / blocks / fluid at
// top level, and same-priority scripts load in filesystem-walk order (differs per machine).
global.fluid = {};

// ############################## Assignments ##############################
var f = global.fluid;

// ############################## Fluids ##############################

//To Remove
f.tmted_apple_cider = 'tmted:apple_cider';
f.tmted_melon_juice = 'tmted:melon_juice';
f.tmted_tomato_sauce = 'tmted:tomato_sauce';
f.thatch = 'decorative_blocks:thatch'; //flowing thatch tag
f.ie_potion = 'immersiveengineering:potion';

// Finish xp merge
f.xp_still = 'sophisticatedcore:xp_still';
f.flowing_xp_still = 'sophisticatedcore:xp_flowing';
f.memory_essence = 'pneumaticcraft:memory_essence';
f.flowing_memory_essence = 'pneumaticcraft:memory_essence_flowing';

//Vanilla
f.water = 'minecraft:water';
f.flowing_water = 'minecraft:flowing_water';
f.t_water = '#forge:fluid/water';

f.lava = 'minecraft:lava';
f.milk = 'minecraft:milk';

//Create
f.chocolate = 'create:chocolate';

f.honey = 'create:honey';
    f.t_honey = '#forge:fluid/honey';

f.create_potion = 'create:potion';
    f.t_potion = '#forge:fluid/potion';
f.tea = 'create:tea';

//Brewin' and Chewin' (backport): keg tank fluids, 250 mB per drink (alcohol.md 3.4)
f.beer = 'brewinandchewin:beer';
f.vodka = 'brewinandchewin:vodka';
f.mead = 'brewinandchewin:mead';
f.egg_grog = 'brewinandchewin:egg_grog';
f.strongroot_ale = 'brewinandchewin:strongroot_ale';
f.rice_wine = 'brewinandchewin:rice_wine';
f.glittering_grenadine = 'brewinandchewin:glittering_grenadine';
f.steel_toe_stout = 'brewinandchewin:steel_toe_stout';
f.pale_jane = 'brewinandchewin:pale_jane';
f.saccharine_rum = 'brewinandchewin:saccharine_rum';
f.salty_folly = 'brewinandchewin:salty_folly';
f.bloody_mary = 'brewinandchewin:bloody_mary';
f.red_rum = 'brewinandchewin:red_rum';
f.withering_dross = 'brewinandchewin:withering_dross';
f.kombucha = 'brewinandchewin:kombucha';
f.dread_nog = 'brewinandchewin:dread_nog';
f.flaxen_cheese = 'brewinandchewin:flaxen_cheese';
f.scarlet_cheese = 'brewinandchewin:scarlet_cheese';

//Create: Central Kitchen
f.apple_cider = 'create_central_kitchen:apple_cider';
f.long_apple_cider = 'create_central_kitchen:long_apple_cider';
f.strong_apple_cider = 'create_central_kitchen:strong_apple_cider';
f.t_apple_cider = '#forge:fluid/apple_cider';
f.t_long_apple_cider = '#forge:fluid/long_apple_cider';
f.t_strong_apple_cider = '#forge:fluid/strong_apple_cider';
f.dragon_breath = 'create_central_kitchen:dragon_breath';
// Farmer's Respite teas and drinks as Kettle tank fluids (farmers_respite.md K9)
f.black_tea = 'create_central_kitchen:black_tea';
f.green_tea = 'create_central_kitchen:green_tea';
f.yellow_tea = 'create_central_kitchen:yellow_tea';
f.dandelion_tea = 'create_central_kitchen:dandelion_tea';
f.purulent_tea = 'create_central_kitchen:purulent_tea';
f.rose_hip_tea = 'create_central_kitchen:rose_hip_tea';
f.gamblers_tea = 'create_central_kitchen:gamblers_tea';
f.chorus_fruit_milk_tea = 'create_central_kitchen:chorus_fruit_milk_tea';
f.coffee = 'create_central_kitchen:coffee';   // Rustic Delight's Coffee (Respite's is purged, item_merges.md 2.8)
    f.cck_hot_cocoa = 'create_central_kitchen:hot_cocoa';

f.melon_juice = 'create_central_kitchen:melon_juice';
f.t_melon_juice = '#forge:fluid/melon_juice';

f.tomato_sauce = 'create_central_kitchen:tomato_sauce';
f.t_tomato_sauce = '#forge:fluid/tomato_sauce';

//Pack fluids (Creative Delight kubejs_startup/creativedelight/fluids.js; creativeengineering: until 2026-11-06, remapped by CD swaps/food_fluids.json)
f.chorus_juice = 'creativedelight:chorus_juice';
f.t_chorus_juice = '#forge:fluid/chorus_juice';
f.carrot_juice = 'creativedelight:carrot_juice';
f.t_carrot_juice = '#forge:fluid/carrot_juice';
f.dandelion_juice = 'creativedelight:dandelion_juice';
f.t_dandelion_juice = '#forge:fluid/dandelion_juice';
f.turnip_water = 'creativedelight:turnip_water';
f.t_turnip_water = '#forge:fluid/turnip_water';
f.egg_solution = 'creativedelight:egg_solution';
f.t_egg_solution = '#forge:fluid/egg_solution';
f.frosting = 'creativedelight:frosting';
f.t_frosting = '#forge:fluid/frosting';
// Kettle tank drinks with no fluid of their own (farmers_respite.md K9)
f.milk_coffee = 'creativedelight:milk_coffee';
f.honey_coffee = 'creativedelight:honey_coffee';
f.chocolate_coffee = 'creativedelight:chocolate_coffee';
f.dark_coffee = 'creativedelight:dark_coffee';
f.syrup_coffee = 'creativedelight:syrup_coffee';
f.pumpkin_coffee = 'creativedelight:pumpkin_coffee';
f.cherry_blossom_coffee = 'creativedelight:cherry_blossom_coffee';
f.lavender_tea = 'creativedelight:lavender_tea';
f.matcha_latte = 'creativedelight:matcha_latte';
f.berry_matcha_latte = 'creativedelight:berry_matcha_latte';
// Drink rules (feature_writeup/creative_delight_drinks.md 6, food/drinks #15): new teas and juices, chorus tea, the
// sea juices the sea pickle tea brews on (Creative Delight kubejs_startup/creativedelight/drinks.js)
f.kiwi_tea = 'creativedelight:kiwi_tea';
f.turnip_tea = 'creativedelight:turnip_tea';
f.bayberry_tea = 'creativedelight:bayberry_tea';
f.hamimelon_tea = 'creativedelight:hamimelon_tea';
f.carrot_tea = 'creativedelight:carrot_tea';
f.lemon_tea = 'creativedelight:lemon_tea';
f.melon_tea = 'creativedelight:melon_tea';
f.orange_tea = 'creativedelight:orange_tea';
f.pear_tea = 'creativedelight:pear_tea';
f.chorus_tea = 'creativedelight:chorus_tea';
f.lychee_juice = 'creativedelight:lychee_juice';
f.hawberry_juice = 'creativedelight:hawberry_juice';
f.mango_juice = 'creativedelight:mango_juice';
f.mangosteen_juice = 'creativedelight:mangosteen_juice';
f.peach_juice = 'creativedelight:peach_juice';
f.sea_grape_juice = 'creativedelight:sea_grape_juice';
f.pickerelweed_juice = 'creativedelight:pickerelweed_juice';
// Fruits Delight's own juice and tea fluids (bottle pairs ship with the mod); bayberry_soup is Bayberry Juice
f.bayberry_juice = 'fruitsdelight:bayberry_soup';
f.hamimelon_juice = 'fruitsdelight:hamimelon_juice';
f.kiwi_juice = 'fruitsdelight:kiwi_juice';
f.lemon_juice = 'fruitsdelight:lemon_juice';
f.orange_juice = 'fruitsdelight:orange_juice';
f.pear_juice = 'fruitsdelight:pear_juice';
f.hawberry_tea = 'fruitsdelight:hawberry_tea';
f.lychee_cherry_tea = 'fruitsdelight:lychee_cherry_tea';
f.mango_tea = 'fruitsdelight:mango_tea';
f.mangosteen_tea = 'fruitsdelight:mangosteen_tea';
f.peach_tea = 'fruitsdelight:peach_tea';
f.chorus_fruit_bubble_tea = 'create_central_kitchen:chorus_fruit_bubble_tea';

//Create: Confectionery
f.black_chocolate = 'create_confectionery:black_chocolate';
f.caramel = 'create_confectionery:caramel';
f.hot_chocolate = 'create_confectionery:hot_chocolate';
f.ruby_chocolate = 'create_confectionery:ruby_chocolate';
f.orange_jam = 'fruitsdelight:orange_jam';
f.lemon_jam = 'fruitsdelight:lemon_jam';
// Pack jam line (dishes/jams #8, Vintage port): jam and melted jello, 125 mB a jar or bowl, 1000 a block. Mulberry jam is Create Central Kitchen's
f.ancient_fruit_jam = 'vintagedelight:ancient_fruit_jam';
f.ancient_fruit_jello = 'vintagedelight:ancient_fruit_jello';
f.banana_jam = 'vintagedelight:banana_jam';
f.banana_jello = 'vintagedelight:banana_jello';
f.cherry_jam = 'vintagedelight:cherry_jam';
f.cherry_jello = 'vintagedelight:cherry_jello';
f.green_grape_jam = 'vintagedelight:green_grape_jam';
f.green_grape_jello = 'vintagedelight:green_grape_jello';
f.mulberry_jello = 'vintagedelight:mulberry_jello';
f.pepper_jam = 'vintagedelight:pepper_jam';
f.pepper_jello = 'vintagedelight:pepper_jello';
// Hearth and Harvest fluids (Creative Delight member hearthandharvest)
f.goat_milk = 'hearthandharvest:goat_milk';
f.brine = 'hearthandharvest:brine';
f.sap = 'hearthandharvest:sap';
f.blueberry_juice = 'hearthandharvest:blueberry_juice';
f.raspberry_juice = 'hearthandharvest:raspberry_juice';
f.cherry_juice = 'hearthandharvest:cherry_juice';
f.sweet_berry_juice = 'hearthandharvest:sweet_berry_juice';
f.glow_berry_juice = 'hearthandharvest:glow_berry_juice';
f.blueberry_wine = 'hearthandharvest:blueberry_wine';
f.raspberry_wine = 'hearthandharvest:raspberry_wine';
f.sweet_berry_wine = 'hearthandharvest:sweet_berry_wine';
f.glow_berry_wine = 'hearthandharvest:glow_berry_wine';
f.melon_wine = 'hearthandharvest:melon_wine';
f.lemon_wine = 'hearthandharvest:lemon_wine';
f.root_beer = 'hearthandharvest:root_beer';
f.moonshine = 'hearthandharvest:moonshine';
f.raspberry_jam = 'vintagedelight:raspberry_jam';
f.raspberry_jello = 'vintagedelight:raspberry_jello';
f.red_grape_jam = 'vintagedelight:red_grape_jam';
f.red_grape_jello = 'vintagedelight:red_grape_jello';
f.salmonberry_jam = 'vintagedelight:salmonberry_jam';
f.salmonberry_jello = 'vintagedelight:salmonberry_jello';
f.strawberry_jam = 'vintagedelight:strawberry_jam';
f.strawberry_jello = 'vintagedelight:strawberry_jello';
f.mulberry_jam = 'create_central_kitchen:mulberry_jam';
f.white_chocolate = 'create_confectionery:white_chocolate';

//Create: Enchantment Industry
f.experience = 'create_enchantment_industry:experience';
f.flowing_experience = 'create_enchantment_industry:flowing_experience';
    f.xp = 'create_enchantment_industry:experience';
f.hyper_experience = 'create_enchantment_industry:hyper_experience';
f .hyper_xp = 'create_enchantment_industry:hyper_experience';
f.ink = 'create_enchantment_industry:ink';

//Create: Things & Misc
f.diluted_bonemeal = 'create_things_and_misc:diluted_bonemeal';
f.slime = 'create_things_and_misc:slime';

//Immersive Engineering
f.concrete = 'immersiveengineering:concrete';
f.redstone_acid = 'immersiveengineering:redstone_acid';
f.herbicide = 'immersiveengineering:herbicide';

f.phenolic_resin = 'immersiveengineering:phenolic_resin';
    f.t_phenolic_resin = '#forge:fluid/phenolic_resin';

//PneumaticCraft
f.etching_acid = 'pneumaticcraft:etching_acid';
f.plastic = 'pneumaticcraft:plastic';
f.yeast_culture = 'pneumaticcraft:yeast_culture';
    f.t_yeast_culture = '#forge:fluid/yeast_culture';

//Oil Chain
f.acetaldehyde = 'immersiveengineering:acetaldehyde';
f.benzene = 'immersivepetroleum:benzene';
f.creosote = 'immersiveengineering:creosote';
    f.t_creosote = '#forge:fluid/creosote';

f.crude_oil = 'immersivepetroleum:crudeoil';
    f.pc_oil = 'pneumaticcraft:oil';

f.diesel_sulfur = 'immersivepetroleum:diesel_sulfur';
f.ethylene = 'immersivepetroleum:ethylene';

f.plantoil = 'immersiveengineering:plantoil';
    f.seed_oil = 'createaddition:seed_oil';
    f.vegetable_oil = 'pneumaticcraft:vegetable_oil';
        
f.ip_diesel = 'immersivepetroleum:diesel';
    f.pc_diesel = 'pneumaticcraft:diesel';

f.ie_biodiesel = 'immersiveengineering:biodiesel';
    f.pc_biodiesel = 'pneumaticcraft:biodiesel';

f.ie_ethanol = 'immersiveengineering:ethanol';
    f.pc_ethanol = 'pneumaticcraft:ethanol';

    f.bioethanol = 'createaddition:bioethanol';

f.ip_kerosene = 'immersivepetroleum:kerosene';
    f.pc_kerosene = 'pneumaticcraft:kerosene';

f.ip_gasoline = 'immersivepetroleum:gasoline';
    f.pc_gasoline = 'pneumaticcraft:gasoline';

f.gasoline_additives = 'immersivepetroleum:gasoline_additives';

f.ip_lubricant = 'immersivepetroleum:lubricant';
    f.pc_lubricant = 'pneumaticcraft:lubricant';

f.lpg = 'pneumaticcraft:lpg';
f.lubricant_cracked = 'immersivepetroleum:lubricant_cracked';
f.napalm = 'immersivepetroleum:napalm';
f.naphtha = 'immersivepetroleum:naphtha';
f.naphtha_cracked = 'immersivepetroleum:naphtha_cracked';
f.propylene = 'immersivepetroleum:propylene';

//SecurityCraft
f.fake_lava = 'securitycraft:fake_lava';
f.fake_water = 'securitycraft:fake_water';

//Slice & Dice
f.fertilizer = 'sliceanddice:fertilizer';

//Vintage Improvements
f.sulfur_dioxide = 'vintageimprovements:sulfur_dioxide';
f.sulfur_trioxide = 'vintageimprovements:sulfur_trioxide';
f.sulfuric_acid = 'vintageimprovements:sulfuric_acid';

// ############################## Validation ##############################

StartupEvents.postInit(() => {
 var $ForgeRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries');
 var $ResourceLocation = Java.loadClass('net.minecraft.resources.ResourceLocation');

 function fluidExists(id) {
 try {
 return $ForgeRegistries.FLUIDS.containsKey($ResourceLocation.of(id, ':'));
 } catch (err) {
 return false;
 }
 }

 let missing = [];
 let tagEntries = 0;
 for (let key in f) {
 let id = f[key];
 if (typeof id !== 'string') {
 // Object-shaped fluid matchers (NBT, etc.) — skip validation
 continue;
 }
 // A glob ('displaydelight:*plated_*') is a matcher, and ids of a mod that is not loaded are guarded where they
 // are used: neither is a dict error (scratch r1106spf).
 if (id.indexOf('*') >= 0 || !global.has(id)) continue;
 if (id.startsWith('#')) {
 // Tag-based fluid entries aren't validated yet — verify these manually.
 // Forge fluid tag validation requires a TagKey<Fluid> lookup that hasn't
 // been implemented/tested here; flagging rather than guessing at it.
 tagEntries++;
 continue;
 }
 if (!fluidExists(id)) {
 missing.push(key + ' -> ' + id);
 }
 }
 if (tagEntries > 0) console.info('[fluid_dict] ' + tagEntries + ' tag entries not checked (tags load after startup)');
 if (missing.length > 0) {
 console.warn('[fluid_dict] Missing/invalid fluid IDs:\n' + missing.join('\n'));
 }
});
