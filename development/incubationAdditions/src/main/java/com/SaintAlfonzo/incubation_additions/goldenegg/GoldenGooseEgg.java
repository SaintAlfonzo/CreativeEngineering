package com.SaintAlfonzo.incubation_additions.goldenegg;

import com.SaintAlfonzo.incubation_additions.IAConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

/**
 * Goose golden egg: now and then a goose lays Vintage Delight's Golden Egg (its block item) instead of a
 * goose egg. mixin/GooseEntityEggLayerMixin rolls {@link IAConfig#GOLDEN_EGG_CHANCE} once per egg, before
 * the timer reaches Incubation's nest window, and on a win keeps the nest goal off (LayEggInNestGoalMixin)
 * and swaps the egg dropped at 0 for this one, dropped at the least walled-in spot nearby ({@link DropSpot}).
 *
 * Vintage Delight is not a compile-time dependency: the item is looked up by id, and with Vintage absent
 * (or the item missing) there is no roll at all.
 */
public final class GoldenGooseEgg {

    private static final String VINTAGE_ID = "vintagedelight";
    private static final ResourceLocation GOLDEN_EGG = new ResourceLocation(VINTAGE_ID, "golden_egg");

    private GoldenGooseEgg() {}

    /** The golden egg item, or null when Vintage Delight or its golden_egg is not there. */
    @Nullable
    public static Item item() {
        if (!ModList.get().isLoaded(VINTAGE_ID)) return null;
        Item item = ForgeRegistries.ITEMS.getValue(GOLDEN_EGG);
        return item == null || item == Items.AIR ? null : item;
    }

    /** One roll for the next egg; false (no roll) without the item. */
    public static boolean roll(RandomSource random) {
        if (item() == null) return false;
        int oneIn = IAConfig.GOLDEN_EGG_CHANCE.get();
        return oneIn > 0 && random.nextInt(oneIn) == 0;
    }

    /**
     * Drops the golden egg at the spot {@link DropSpot} picks. Goes through the bird's own spawnAtLocation
     * (the goose marks the egg as thrown by itself, so it doesn't pick it straight back up), then moves it.
     * Null when the item is gone, so the caller can fall back to the normal egg.
     */
    @Nullable
    public static ItemEntity drop(Mob bird) {
        Item item = item();
        if (item == null) return null;
        Level level = bird.level;
        BlockPos base = bird.blockPosition();
        Vec3 from = new Vec3(bird.getX(), bird.getY() + 0.5, bird.getZ());

        int[] o = DropSpot.pick(new DropSpot.Probe() {
            @Override
            public boolean standable(int dx, int dy, int dz) {
                BlockPos feet = base.offset(dx, dy, dz);
                BlockPos below = feet.below();
                return free(feet) && free(feet.above())
                        && !level.getBlockState(below).getCollisionShape(level, below).isEmpty();
            }

            @Override
            public boolean solid(int dx, int dy, int dz) {
                return level.getBlockState(base.offset(dx, dy, dz)).getMaterial().blocksMotion();
            }

            @Override
            public boolean reachable(int dx, int dy, int dz) {
                Vec3 to = Vec3.atCenterOf(base.offset(dx, dy, dz));
                return level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, bird))
                        .getType() == HitResult.Type.MISS;
            }

            private boolean free(BlockPos pos) {
                BlockState state = level.getBlockState(pos);
                return state.getCollisionShape(level, pos).isEmpty() && state.getFluidState().isEmpty();
            }
        });

        ItemEntity egg = bird.spawnAtLocation(item);
        if (egg != null && (o[0] != 0 || o[1] != 0 || o[2] != 0)) {
            BlockPos spot = base.offset(o[0], o[1], o[2]);
            egg.setPos(spot.getX() + 0.5, spot.getY() + 0.1, spot.getZ() + 0.5);
        }
        return egg;
    }
}
