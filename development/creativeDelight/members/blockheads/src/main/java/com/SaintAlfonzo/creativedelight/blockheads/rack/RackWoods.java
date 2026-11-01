package com.SaintAlfonzo.creativedelight.blockheads.rack;

import java.util.List;

/**
 * The woods with their own spice rack, tool rack and fruit basket ({@code cookingforblockheads:<wood>_spice_rack},
 * {@code <wood>_tool_rack}, {@code <wood>_fruit_basket}). The art is in {@code textures/block/rack/}, made from each wood's planks in
 * {@code textureswapper/rack/}; recipes are KubeJS (Creative Delight {@code cookingforblockheads/kitchen.js}). CfB's own
 * spice rack and tool rack stay as they are. Keep in step with tools/gen_cfb_racks.py.
 */
public final class RackWoods {
    public static final List<String> WOODS = List.of(
            "oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "crimson", "warped",
            "twisted", "dreamwood", "livingwood", "smoked", "treated_wood", "vertical_treated_wood", "rubberwood",
            "ancient", "azalea", "bamboo", "driftwood");

    private RackWoods() {
    }
}
