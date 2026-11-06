// priority: 979
// Tool dict: global.tool (alias t), a mask over global.item (creative_delight.md 13.3): bowls, jars, cookware,
// knives and stations of the Creative Delight members. The value is always i.<key>, so items stay defined once in
// item_dict.js. Key: <category>_<name>, category from global.toolGrammar (food_grammar.js), e.g. t.pot_copper.
//
// Kept by hand. Seeded 2026-11-02 by tools/kitchen_dicts.py --seed (108 keys); run tools/kitchen_dicts.py to lint this
// file and misc_dict.js and list member items with no key. Check on load as for the food dict (food_grammar.js):
// a missing general key, a value not in global.item, an item named twice or a key outside the grammar throws.
// global.toolKey is the reverse index (item id -> tool key). Pack scripts alias it t only where t is free.
global.tool = {}
;(function (t, i) {

	// ---- knife
	t.knife_adamantite                  = i.adamantite_knife;
	t.knife_adamantium                  = i.adamantium_knife;
	t.knife_alf                         = i.alf_knife;
	t.knife_allthemodium                = i.allthemodium_knife;
	t.knife_aluminum                    = i.aluminum_knife;
	t.knife_amethyst                    = i.amethyst_knife;
	t.knife_black_opal                  = i.black_opal_knife;
	t.knife_blazing                     = i.blazing_knife;
	t.knife_bone                        = i.bone_knife;
	t.knife_brass                       = i.delightful_brass_knife;
	t.knife_bronze                      = i.bronze_knife;
	t.knife_certus_quartz               = i.certus_quartz_knife;
	t.knife_cloggrum                    = i.cloggrum_knife;
	t.knife_constantan                  = i.constantan_knife;
	t.knife_copper                      = i.delightful_copper_knife;
	t.knife_crystalline                 = i.crystalline_knife;
	t.knife_deorum                      = i.deorum_knife;
	t.knife_diamond                     = i.diamond_knife;
	t.knife_draco_arcanus               = i.draco_arcanus_knife;
	t.knife_dragon_egg_shell            = i.dragon_egg_shell_knife;
	t.knife_dragon_tooth                = i.dragon_tooth_knife;
	t.knife_electrum                    = i.electrum_knife;
	t.knife_elementium                  = i.elementium_knife;
	t.knife_emerald                     = i.emerald_knife;
	t.knife_end_stone                   = i.end_stone_knife;
	t.knife_enderite                    = i.enderite_knife;
	t.knife_experience                  = i.experience_knife;
	t.knife_fiery                       = i.fiery_knife;
	t.knife_flint                       = i.flint_knife;
	t.knife_fluix                       = i.fluix_knife;
	t.knife_forgotten                   = i.forgotten_knife;
	t.knife_froststeel                  = i.froststeel_knife;
	t.knife_gilded_netherite            = i.gilded_netherite_knife;
	t.knife_gilded_quartz               = i.gilded_quartz_knife;
	t.knife_golden                      = i.golden_knife;
	t.knife_invar                       = i.invar_knife;
	t.knife_iron                        = i.iron_knife;
	t.knife_ironwood                    = i.ironwood_knife;
	t.knife_kiwano                      = i.kiwano_knife;
	t.knife_knightmetal                 = i.knightmetal_knife;
	t.knife_lapis_lazuli                = i.lapis_lazuli_knife;
	t.knife_large_amethyst              = i.large_amethyst_knife;
	t.knife_lead                        = i.lead_knife;
	t.knife_leaf                        = i.leaf_knife;
	t.knife_living                      = i.living_knife;
	t.knife_manasteel                   = i.manasteel_knife;
	t.knife_mythril                     = i.mythril_knife;
	t.knife_necronium                   = i.necronium_knife;
	t.knife_netherite                   = i.netherite_knife;
	t.knife_netherite_opal              = i.netherite_opal_knife;
	t.knife_nickel                      = i.nickel_knife;
	t.knife_obsidian_infused_enderite   = i.obsidian_infused_enderite_knife;
	t.knife_onyx                        = i.onyx_knife;
	t.knife_osmium                      = i.osmium_knife;
	t.knife_pendorite                   = i.pendorite_knife;
	t.knife_purpur                      = i.purpur_knife;
	t.knife_refined_glowstone           = i.refined_glowstone_knife;
	t.knife_refined_obsidian            = i.refined_obsidian_knife;
	t.knife_reinforced_deorum           = i.reinforced_deorum_knife;
	t.knife_rose_gold                   = i.rose_gold_knife;
	t.knife_silver                      = i.silver_knife;
	t.knife_sinisite                    = i.sinisite_knife;
	t.knife_soul_steel                  = i.soul_steel_knife;
	t.knife_steel                       = i.steel_knife;
	t.knife_steeleaf                    = i.steeleaf_knife;
	t.knife_stellium                    = i.stellium_knife;
	t.knife_terra                       = i.terra_knife;
	t.knife_thyrium                     = i.thyrium_knife;
	t.knife_tin                         = i.tin_knife;
	t.knife_utherium                    = i.utherium_knife;
	t.knife_warden                      = i.warden_knife;
	t.knife_zinc                        = i.delightful_zinc_knife;

	// ---- machete
	t.machete_diamond                   = i.diamond_machete;
	t.machete_golden                    = i.golden_machete;
	t.machete_iron                      = i.iron_machete;
	t.machete_netherite                 = i.netherite_machete;

	// ---- skillet
	t.skillet_farmersdelight            = i.skillet;

	// ---- pot
	t.pot_cooking                       = i.cooking_pot;
	t.pot_copper                        = i.copper_pot;
	t.pot_mushroom_pie                  = i.mushroom_pot_pie;
	t.pot_mushroom_pie_block            = i.mushroom_pot_pie_block;

	// ---- stove
	t.stove_end                         = i.end_stove;
	t.stove_endstone                    = i.endstone_stove;
	t.stove_farmersdelight              = i.farmersdelight_stove;

	// ---- grill
	t.grill_barbequesdelight            = i.bbq_grill;

	// ---- board
	t.board_cutting                     = i.cutting_board;

	// ---- basket
	t.basket_farmersdelight             = i.fd_basket;
	t.basket_sticky                     = i.sticky_basket;

	// ---- bowl
	t.bowl_shulker                      = i.shulker_bowl;

	// ---- jar
	t.jar_empty_mason                   = i.empty_mason_jar;
	t.jar_fermenting                    = i.fermenting_jar;

	// ---- mold
	t.mold_cheese                       = i.cheese_mold;

	// ---- net
	t.net_fishing                       = i.fishing_net;
	t.net_safety                        = i.safety_net;
	t.net_vine                          = i.vine_net;

	// ---- tray
	t.tray_barbequesdelight             = i.bbq_tray;

	// ---- basin
	t.basin_barbequesdelight            = i.bbq_basin;

	// ---- watering_can
	t.watering_can_diamond              = i.diamond_watering_can;
	t.watering_can_dragon               = i.dragon_watering_can;
	t.watering_can_golden               = i.golden_watering_can;
	t.watering_can_hearthandharvest     = i.watering_can;
	t.watering_can_iron                 = i.iron_watering_can;
	t.watering_can_netherite            = i.netherite_watering_can;

	// ---- station
	t.station_evaporator                = i.evaporator;
	t.station_hoglin_mount              = i.hoglin_mount;

	global.toolKey = global.subdict.check(t, 'tool', global.toolGrammar)
})(global.tool, global.item)
