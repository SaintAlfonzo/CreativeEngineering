package com.SaintAlfonzo.creativedelight.salt;

import com.SaintAlfonzo.creativecommon.melting.Melting;
import io.github.mortuusars.salt.configuration.Configuration;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Salt and Creative Common's melting (plan_post_scratch_1115 B2, B3; feature_writeup/salt.md 7).
 * <ul>
 * <li>B3: when a salt layer (a {@link SnowLayerBlock} in the melting salt tag: Vintage Delight's salt layer) melts ice or
 * snow by its random tick, it loses one layer with {@code [salt.Melting] SaltLayerVanishChance} (default 0.05); the last
 * layer leaves air. Full salt blocks are not layers and never vanish.</li>
 * <li>B2: The Salt's dispenser behavior (melt one stage, or place a layer on air) is also registered on the pack's real
 * salt, Vintage Delight's salt dust. Hearth and Harvest wraps it at load complete for salt licks.</li>
 * </ul>
 * Runs from {@link SaltBlockTicks#init} (common setup, main thread).
 */
public final class SaltLayerMelt {
    private static final ResourceLocation SALT_DUST_ID = new ResourceLocation("vintagedelight", "salt_dust");

    private SaltLayerMelt() {
    }

    static void init() {
        Melting.setSaltMeltListener(SaltLayerMelt::onSaltMelt);
        Item dust = ForgeRegistries.ITEMS.getValue(SALT_DUST_ID);
        if (dust != null && dust != Items.AIR) {
            DispenserBlock.registerBehavior(dust, io.github.mortuusars.salt.Melting.SALT_DISPENSER_BEHAVIOR);
        }
    }

    private static volatile ForgeConfigSpec.DoubleValue vanishChance;
    private static volatile boolean looked;

    /**
     * {@code [salt.Melting] SaltLayerVanishChance}: the salt member's Configuration field, read by reflection because this
     * jar compiles against the upstream Salt jar, which lacks it. 0 when it cannot be read.
     */
    private static double vanishChance() {
        if (!looked) {
            try {
                Object v = Configuration.class.getField("SALT_LAYER_VANISH_CHANCE").get(null);
                vanishChance = v instanceof ForgeConfigSpec.DoubleValue d ? d : null;
            } catch (ReflectiveOperationException | RuntimeException e) {
                vanishChance = null;
            }
            looked = true;
        }
        ForgeConfigSpec.DoubleValue v = vanishChance;
        return v == null ? 0.0D : v.get();
    }

    private static void onSaltMelt(ServerLevel level, BlockPos source, BlockState state, RandomSource random) {
        if (!(state.getBlock() instanceof SnowLayerBlock) || !state.hasProperty(SnowLayerBlock.LAYERS)) return;
        if (random.nextDouble() >= vanishChance()) return;
        if (level.getBlockState(source) != state) return;
        int layers = state.getValue(SnowLayerBlock.LAYERS);
        level.setBlockAndUpdate(source, layers > 1 ? state.setValue(SnowLayerBlock.LAYERS, layers - 1) : Blocks.AIR.defaultBlockState());
    }
}
