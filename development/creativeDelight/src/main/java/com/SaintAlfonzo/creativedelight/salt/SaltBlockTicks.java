package com.SaintAlfonzo.creativedelight.salt;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.salt.Dissolving;
import io.github.mortuusars.salt.Melting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * One salt (item_merges.md 2.9): every salt block acts like The Salt's. The Salt's own blocks (ISaltBlock) melt
 * touching ice and snow and dissolve in water by themselves; Vintage Delight's salt family is not ISaltBlock,
 * so this gives it the same random tick, through The Salt's own {@link Melting} and {@link Dissolving} (its config and
 * chances apply). All of {@link #MELTERS} melt; {@link #DISSOLVERS} also dissolve. Vintage's blocks mostly do not
 * random tick, so {@link #init} turns it on (the final field BlockBehaviour.isRandomlyTicking, f_60445_, set by
 * reflection; BlockStateBase reads it through on every call, never caching it per state). Hooks: {@code mixin.SaltBlockRandomTickMixin} (blocks that keep BlockBehaviour.randomTick),
 * {@code mixin.SaltLayerRandomTickMixin} (the layer is a SnowLayerBlock). The raw rock salt block never dissolves:
 * that is an edit in the Salt member's own Dissolving (members/salt, feature_writeup/salt.md). Vintage's leftover Salt Block (merged away, may sit in old worlds) dissolves like The
 * Salt's and no longer grows salt layers; the Evaporator keeps growing them.
 */
public final class SaltBlockTicks {

    private static final String[] MELTERS = {
        "vintagedelight:salt", "vintagedelight:salt_block", "vintagedelight:evaporator",
        "vintagedelight:smooth_salt_block", "vintagedelight:salt_pillar",
        "vintagedelight:salt_bricks", "vintagedelight:salt_brick_stairs", "vintagedelight:salt_brick_slab",
        "vintagedelight:salt_brick_wall", "vintagedelight:mixed_salt_bricks", "vintagedelight:mixed_salt_brick_stairs",
        "vintagedelight:mixed_salt_brick_slab", "vintagedelight:mixed_salt_brick_wall",
        "vintagedelight:salt_lamp_default", "vintagedelight:salt_lamp_black", "vintagedelight:salt_lamp_blue",
        "vintagedelight:salt_lamp_brown", "vintagedelight:salt_lamp_cyan", "vintagedelight:salt_lamp_gray",
        "vintagedelight:salt_lamp_green", "vintagedelight:salt_lamp_light_blue", "vintagedelight:salt_lamp_light_gray",
        "vintagedelight:salt_lamp_lime", "vintagedelight:salt_lamp_magenta", "vintagedelight:salt_lamp_orange",
        "vintagedelight:salt_lamp_pink", "vintagedelight:salt_lamp_purple", "vintagedelight:salt_lamp_red",
        "vintagedelight:salt_lamp_white", "vintagedelight:salt_lamp_yellow",
    };
    private static final String[] DISSOLVERS = {"vintagedelight:salt", "vintagedelight:salt_block"};
    private static final String LEFTOVER_SALT_BLOCK = "vintagedelight:salt_block";
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final Logger LOGGER = LogUtils.getLogger();

    private static Set<Block> melters = Set.of();
    private static Set<Block> dissolvers = Set.of();
    private static Block leftoverSaltBlock = Blocks.AIR;

    private SaltBlockTicks() {}

    /** Common setup, before any chunk loads (a section counts its random tickers when it loads). */
    public static void init() {
        if (!ModList.get().isLoaded("salt")) return;
        melters = resolve(MELTERS);
        dissolvers = resolve(DISSOLVERS);
        Block leftover = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(LEFTOVER_SALT_BLOCK));
        if (leftover != null && leftover != Blocks.AIR) leftoverSaltBlock = leftover;
        try {
            Field ticking = ObfuscationReflectionHelper.findField(BlockBehaviour.class, "f_60445_");
            for (Block b : melters) ticking.setBoolean(b, true);
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.error("[salt] could not turn on random ticks for Vintage's salt blocks", e);
            melters = Set.of();
        }
        LOGGER.info("[salt] {} Vintage salt blocks random tick like The Salt's", melters.size());
    }

    private static Set<Block> resolve(String[] ids) {
        Set<Block> out = Collections.newSetFromMap(new IdentityHashMap<>());
        for (String id : ids) {
            Block b = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(id));
            if (b != null && b != Blocks.AIR) out.add(b);
        }
        return out;
    }

    public static boolean isSalt(BlockState state) {
        return melters.contains(state.getBlock());
    }

    /**
     * ISaltBlock.onSaltRandomTick without cluster growth: one touching meltable may melt, else a touching water (not
     * below) may dissolve the block. Rain never does (salt-common.toml SaltDissolvingInRain = false). Directions start at a random one instead of a shuffled list.
     * True when the vanilla random tick must not run: the block is gone, or it is Vintage's leftover Salt Block
     * (its tick grows salt layers).
     */
    public static boolean randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean dissolves = dissolvers.contains(state.getBlock());
        int start = random.nextInt(DIRECTIONS.length);
        for (int k = 0; k < DIRECTIONS.length; k++) {
            Direction dir = DIRECTIONS[(start + k) % DIRECTIONS.length];
            BlockPos next = pos.relative(dir);
            if (Melting.tryMeltFromBlock(next, level)) break;
            if (dissolves && dir != Direction.DOWN
                    && Dissolving.maybeDissolve(state, pos, level.getBlockState(next), next, level)) {
                return true;
            }
        }
        return state.getBlock() == leftoverSaltBlock;
    }
}
