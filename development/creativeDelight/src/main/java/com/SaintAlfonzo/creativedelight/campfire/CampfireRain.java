package com.SaintAlfonzo.creativedelight.campfire;

import com.SaintAlfonzo.creativedelight.CDConfig;

import com.SaintAlfonzo.creativecommon.campfire.CampfireRainInfo;
import com.SaintAlfonzo.creativecommon.campfire.CampfireRainTicks;

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
 * Rain puts out lit campfires in {@code #creativecommon:campfire/rain_douses} (the plain campfire) after
 * {@code [campfire] rainOutTicks} ticks under open rain, unless the block below is in
 * {@code #creativecommon:campfire/rain_proof_fuel} (coal and charcoal blocks). Unlit Campfire keeps the
 * place-unlit rule but is set to burn forever with its own rain check off (serverconfig/unlitcampfire-server.toml),
 * so this is the only thing that puts a campfire out on its own. Soul campfires and Tinted Campfires (not a
 * {@code CampfireBlock}) aren't in the tag. Tags are filled by KubeJS (server_scripts/tags/blocktags.js).
 * Driven from {@code mixin/CampfireRainMixin}; vanilla only ticks {@code cookTick} on the server while lit.
 */
public final class CampfireRain {

    /** The keys live in patcherMod (campfire.CampfireRainInfo), which the WTHIT lines read. */
    public static final TagKey<Block> RAIN_DOUSES = CampfireRainInfo.RAIN_DOUSES;
    public static final TagKey<Block> RAIN_PROOF_FUEL = CampfireRainInfo.RAIN_PROOF_FUEL;

    private CampfireRain() {}

    public static void tick(Level level, BlockPos pos, BlockState state, CampfireBlockEntity campfire) {
        CampfireRainTicks ticks = (CampfireRainTicks) campfire;
        if (!CDConfig.CAMPFIRE.rainDouses.get()
                || !state.is(RAIN_DOUSES)
                || !state.getValue(CampfireBlock.LIT)
                || level.getBlockState(pos.below()).is(RAIN_PROOF_FUEL)
                || !level.isRainingAt(pos.above())) {
            ticks.ce$setRainTicks(0);
            return;
        }
        int rained = ticks.ce$getRainTicks() + 1;
        if (rained < CDConfig.CAMPFIRE.rainOutTicks.get()) {
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
