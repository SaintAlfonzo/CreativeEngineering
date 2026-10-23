package com.SaintAlfonzo.creativeengineering.crops;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.StemGrownBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Extra fruit on a stem that already has one (melon, pumpkin, Fruits Delight hamimelon; every
 * {@code AttachedStemBlock}). Vanilla stops at one: the stem turns into its attached form, which has no
 * random tick. {@code mixin/AttachedStemExtraFruitMixin} gives the attached stem a random tick that calls
 * {@link #randomTick}, which grows a 2nd, 3rd and 4th fruit on free sides with vanilla's stem growth roll,
 * but only once a minimum time has passed since the stem's last fruit (longer for each one;
 * CEConfig [stem_fruit]). The last-fruit time per stem lives in the dimension's saved data; it starts on
 * the attached stem's first random tick, and is dropped when the stem is broken or turns back into a
 * plain stem. feature_writeup/crops.md 2.10.
 */
public final class StemExtraFruit {
    private static final Logger LOG = LogUtils.getLogger();
    private static final String DATA_NAME = "creativeengineering_stem_fruit";
    private static final int TICKS_PER_MINUTE = 1200;

    private static Method growthSpeed;
    private static boolean growthSpeedFailed;

    private StemExtraFruit() {}

    /** Random tick of an attached stem: maybe grow one more fruit. */
    public static void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        CEConfig.StemFruit cfg = CEConfig.STEM_FRUIT;
        if (!cfg.enabled.get() || cfg.maxFruits.get() <= 1 || !level.isAreaLoaded(pos, 1)) return;
        Block attached = state.getBlock();
        BlockState facing = level.getBlockState(pos.relative(state.getValue(AttachedStemBlock.FACING)));
        if (!(facing.getBlock() instanceof StemGrownBlock fruit) || fruit.getAttachedStem() != attached) return;
        // Only the stem's own fruit: blocks extending MelonBlock / PumpkinBlock (Delightful's slices, cantaloupe)
        // report the vanilla stem too, but reattach() and vanilla's updateShape only know the real fruit.
        if (fruit.getStem().getFruit() != fruit) return;

        Data data = data(level);
        long now = level.getGameTime();
        long key = pos.asLong();
        if (!data.lastFruit.containsKey(key)) {
            data.lastFruit.put(key, now);
            data.setDirty();
            return;
        }

        StemBlock stem = fruit.getStem();
        int fruits = 0;
        List<Direction> free = new ArrayList<>(4);
        for (Direction d : Direction.Plane.HORIZONTAL) {
            BlockPos side = pos.relative(d);
            if (level.getBlockState(side).is(fruit)) fruits++;
            else if (canGrowAt(level, side, stem)) free.add(d);
        }
        if (fruits >= cfg.maxFruits.get() || free.isEmpty()) return;
        if (now - data.lastFruit.get(key) < (long) minutesBefore(cfg, fruits + 1) * TICKS_PER_MINUTE) return;
        if (level.getRawBrightness(pos, 0) < 9) return;

        // StemBlock.randomTick's roll, so soil, hydration and row bonuses count the same
        float speed = growthSpeed(stem, level, pos);
        boolean grow = random.nextInt((int) (25.0F / speed) + 1) == 0;
        if (!ForgeHooks.onCropsGrowPre(level, pos, state, grow)) return;
        BlockPos target = pos.relative(free.get(random.nextInt(free.size())));
        level.setBlockAndUpdate(target, fruit.defaultBlockState());
        data.lastFruit.put(key, now);
        data.setDirty();
        ForgeHooks.onCropsGrowPost(level, pos, state);
    }

    /**
     * Attached stem's updateShape result: vanilla turns it back into a plain stem when its fruit goes.
     * If another of its fruits is still next to it, point at that one instead. Null = keep vanilla's.
     */
    public static BlockState reattach(BlockState state, BlockState result, LevelAccessor level, BlockPos pos) {
        if (!CEConfig.STEM_FRUIT.enabled.get() || result.is(state.getBlock())) return null;
        if (!(result.getBlock() instanceof StemBlock stem)) return null;
        StemGrownBlock fruit = stem.getFruit();
        if (fruit.getAttachedStem() != state.getBlock()) return null;
        for (Direction d : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(pos.relative(d)).is(fruit)) {
                return state.setValue(AttachedStemBlock.FACING, d);
            }
        }
        return null;
    }

    /** Attached stem removed or turned into something else: forget its last-fruit time. */
    public static void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState) {
        if (newState.is(state.getBlock()) || !(level instanceof ServerLevel server)) return;
        Data data = data(server);
        if (data.lastFruit.remove(pos.asLong()) != data.lastFruit.defaultReturnValue()) data.setDirty();
    }

    /** Minutes since the last fruit before fruit number {@code n} (2-4) may grow. */
    private static int minutesBefore(CEConfig.StemFruit cfg, int n) {
        return switch (n) {
            case 2 -> cfg.secondFruitMinutes.get();
            case 3 -> cfg.thirdFruitMinutes.get();
            default -> cfg.fourthFruitMinutes.get();
        };
    }

    /** StemBlock.randomTick's check for where a fruit may go (Forge version). */
    private static boolean canGrowAt(ServerLevel level, BlockPos pos, StemBlock stem) {
        if (!level.isEmptyBlock(pos)) return false;
        BlockState soil = level.getBlockState(pos.below());
        return soil.canSustainPlant(level, pos.below(), Direction.UP, stem)
                || soil.is(Blocks.FARMLAND) || soil.is(BlockTags.DIRT);
    }

    /** CropBlock.getGrowthSpeed (m_52272_, protected static), by reflection; 1 if it can't be found. */
    private static float growthSpeed(Block block, BlockGetter level, BlockPos pos) {
        if (growthSpeed == null && !growthSpeedFailed) {
            try {
                growthSpeed = ObfuscationReflectionHelper.findMethod(CropBlock.class, "m_52272_",
                        Block.class, BlockGetter.class, BlockPos.class);
            } catch (Exception e) {
                growthSpeedFailed = true;
                LOG.warn("[stem_fruit] CropBlock.getGrowthSpeed not found, extra fruit uses speed 1: {}", e.toString());
            }
        }
        if (growthSpeed == null) return 1.0F;
        try {
            return (float) growthSpeed.invoke(null, block, level, pos);
        } catch (Exception e) {
            return 1.0F;
        }
    }

    private static Data data(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(Data::load, Data::new, DATA_NAME);
    }

    /** Game time of each attached stem's last fruit, per dimension. */
    public static final class Data extends SavedData {
        final Long2LongOpenHashMap lastFruit = new Long2LongOpenHashMap();

        Data() {
            lastFruit.defaultReturnValue(Long.MIN_VALUE);
        }

        static Data load(CompoundTag tag) {
            Data d = new Data();
            long[] pos = tag.getLongArray("Pos");
            long[] time = tag.getLongArray("Time");
            for (int i = 0; i < Math.min(pos.length, time.length); i++) d.lastFruit.put(pos[i], time[i]);
            return d;
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            long[] pos = new long[lastFruit.size()];
            long[] time = new long[pos.length];
            int i = 0;
            for (Long2LongMap.Entry e : lastFruit.long2LongEntrySet()) {
                pos[i] = e.getLongKey();
                time[i++] = e.getLongValue();
            }
            tag.putLongArray("Pos", pos);
            tag.putLongArray("Time", time);
            return tag;
        }
    }
}
