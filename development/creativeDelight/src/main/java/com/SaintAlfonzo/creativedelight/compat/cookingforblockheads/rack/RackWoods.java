package com.SaintAlfonzo.creativedelight.compat.cookingforblockheads.rack;

import java.util.List;

/**
 * The woods with their own spice rack, tool rack and fruit basket ({@code creativedelight:<wood>_spice_rack},
 * {@code <wood>_tool_rack}, {@code <wood>_fruit_basket}). The art is built on the client from each wood's planks (compat/art/ArtRecipes.PLANKS, reproducing
 * {@code textureswapper/rack/}); recipes are KubeJS (Creative Delight {@code cookingforblockheads/kitchen.js}). CfB's own
 * spice rack and tool rack stay as they are. Keep in step with tools/gen_cfb_racks.py.
 */
public final class RackWoods {
    public static final List<String> WOODS = List.of(
            "oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "crimson", "warped",
            "twisted", "dreamwood", "livingwood", "smoked", "treated_wood", "vertical_treated_wood", "rubberwood",
            "ancient", "bamboo", "driftwood");

    private RackWoods() {
    }
}
