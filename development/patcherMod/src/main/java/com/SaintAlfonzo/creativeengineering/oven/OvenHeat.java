package com.SaintAlfonzo.creativeengineering.oven;

import com.SaintAlfonzo.creativeengineering.CreativeEngineering;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * The oven's heat rule (oven.md 3.1): the block below is in {@code #creativeengineering:oven_heat_sources} (fire,
 * campfires, lava, magma, blaze burners: every stove and oven is left out on purpose) and is active, or a
 * {@code #farmersdelight:heat_conductors} block (hopper, chute) sits between the oven and such a source, as FD allows
 * for the cooking pot. Tags are filled by KubeJS (server_scripts/tags/blocktags.js); no FD classes are used.
 */
public final class OvenHeat {

    public static final TagKey<Block> OVEN_HEAT_SOURCES =
            BlockTags.create(new ResourceLocation(CreativeEngineering.MODID, "oven_heat_sources"));
    public static final TagKey<Block> HEAT_CONDUCTORS =
            BlockTags.create(new ResourceLocation("farmersdelight", "heat_conductors"));

    private OvenHeat() {}

    public static boolean isHeated(Level level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        if (below.is(OVEN_HEAT_SOURCES)) {
            return isActive(below);
        }
        if (below.is(HEAT_CONDUCTORS)) {
            BlockState source = level.getBlockState(pos.below(2));
            return source.is(OVEN_HEAT_SOURCES) && isActive(source);
        }
        return false;
    }

    /**
     * FD's rule (a block with {@code lit} counts only while lit) plus the Brewin port's Blaze Burner rule: a burner
     * whose {@code blaze} heat level is {@code none} has no fuel. Matched by property name, so no Create classes.
     */
    public static boolean isActive(BlockState state) {
        if (state.hasProperty(BlockStateProperties.LIT)) {
            return state.getValue(BlockStateProperties.LIT);
        }
        for (Property<?> property : state.getProperties()) {
            if (property.getName().equals("blaze") && "none".equals(valueName(state, property))) {
                return false;
            }
        }
        return true;
    }

    private static <T extends Comparable<T>> String valueName(BlockState state, Property<T> property) {
        return property.getName(state.getValue(property));
    }
}
