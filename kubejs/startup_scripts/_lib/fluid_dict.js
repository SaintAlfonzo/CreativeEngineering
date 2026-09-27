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
f.memory_essence = 'pneumaticcraft:memory_essence';

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

//Create: Central Kitchen
f.apple_cider = 'create_central_kitchen:apple_cider';
f.dragon_breath = 'create_central_kitchen:dragon_breath';
    f.cck_hot_cocoa = 'create_central_kitchen:hot_cocoa';

f.melon_juice = 'create_central_kitchen:melon_juice';
f.t_melon_juice = '#forge:fluid/melon_juice';

f.tomato_sauce = 'create_central_kitchen:tomato_sauce';
f.t_tomato_sauce = '#forge:fluid/tomato_sauce';

//Create: Confectionery
f.black_chocolate = 'create_confectionery:black_chocolate';
f.caramel = 'create_confectionery:caramel';
f.hot_chocolate = 'create_confectionery:hot_chocolate';
f.ruby_chocolate = 'create_confectionery:ruby_chocolate';
f.white_chocolate = 'create_confectionery:white_chocolate';

//Create: Enchantment Industry
f.experience = 'create_enchantment_industry:experience';
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
 for (let key in f) {
 let id = f[key];
 if (typeof id !== 'string') {
 // Object-shaped fluid matchers (NBT, etc.) — skip validation
 continue;
 }
 if (id.startsWith('#')) {
 // Tag-based fluid entries aren't validated yet — verify these manually.
 // Forge fluid tag validation requires a TagKey<Fluid> lookup that hasn't
 // been implemented/tested here; flagging rather than guessing at it.
 console.warn('[fluid_dict] Tag entry "' + key + ' -> ' + id + '" is not validated (tag support not yet implemented)');
 continue;
 }
 if (!fluidExists(id)) {
 missing.push(key + ' -> ' + id);
 }
 }
 if (missing.length > 0) {
 console.warn('[fluid_dict] Missing/invalid fluid IDs:\n' + missing.join('\n'));
 }
});
