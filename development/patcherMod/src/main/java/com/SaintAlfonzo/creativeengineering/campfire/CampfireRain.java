package com.SaintAlfonzo.creativeengineering.campfire;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.SaintAlfonzo.creativeengineering.CreativeEngineering;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Rain puts out lit campfires in {@code #creativeengineering:campfire/rain_douses} (the plain campfire) after
 * {@code [campfire] rainOutTicks} ticks under open rain, unless the block below is in
 * {@code #creativeengineering:campfire/rain_proof_fuel} (coal and charcoal blocks). Unlit Campfire keeps the
 * place-unlit rule but is set to burn forever with its own rain check off (serverconfig/unlitcampfire-server.toml),
 * so this is the only thing that puts a campfire out on its own. Soul campfires and Tinted Campfires (not a
 * {@code CampfireBlock}) aren't in the tag. Tags are filled by KubeJS (server_scripts/tags/blocktags.js).
 * Driven from {@code mixin/CampfireRainMixin}; vanilla only ticks {@code cookTick} on the server while lit.
 */
public final class CampfireRain {

    public static final TagKey<Block> RAIN_DOUSES =
            BlockTags.create(new ResourceLocation(CreativeEngineering.MODID, "campfire/rain_douses"));
    public static final TagKey<Block> RAIN_PROOF_FUEL =
            BlockTags.create(new ResourceLocation(CreativeEngineering.MODID, "campfire/rain_proof_fuel"));

    private CampfireRain() {}

    public static void tick(Level level, BlockPos pos, BlockState state, CampfireBlockEntity campfire) {
        CampfireRainTicks ticks = (CampfireRainTicks) campfire;
        if (!CEConfig.CAMPFIRE.rainDouses.get()
                || !state.is(RAIN_DOUSES)
                || !state.getValue(CampfireBlock.LIT)
                || level.getBlockState(pos.below()).is(RAIN_PROOF_FUEL)
                || !level.isRainingAt(pos.above())) {
            ticks.ce$setRainTicks(0);
            return;
        }
        int rained = ticks.ce$getRainTicks() + 1;
        if (rained < CEConfig.CAMPFIRE.rainOutTicks.get()) {
            ticks.ce$setRainTicks(rained);
            return;
        }
        ticks.ce$setRainTicks(0);
        // Same as Unlit Campfire's own rain-out: fizz sound, dowse (smoke, game event), then unlit.
        // Food on the grill stays, as with a shovel.
        level.levelEvent(null, 1009, pos, 0);
        CampfireBlock.dowse(null, level, pos, state);
        level.setBlockAndUpdate(pos, state.setValue(CampfireBlock.LIT, false));
    }
}
