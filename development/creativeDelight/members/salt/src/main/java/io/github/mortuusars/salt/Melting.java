// Salt (mortuusars), MIT: a modified copy of the upstream io.github.mortuusars.salt.Melting for the Creative
// Delight repack (members/salt/NOTICE-upstream-MIT.txt). The edit is GNU GPL3 like the rest of the pack's code.
// Change against upstream 1.2.4.1 (meltBlock only, feature_writeup/salt.md 6 S8): upstream placed water only in
// ultra-warm dimensions and air everywhere else (the dimension check was inverted). Now melted ice leaves water
// outside ultra-warm dimensions (SaltMeltingPlaceWater still switches it off); snow, and anything melted in an
// ultra-warm dimension (the Nether), leaves air. Since 2026-11-14 tryMeltFromBlock is off: Creative Common's melting
// system owns block melting (plan_bugfix_1114 3.8).
package io.github.mortuusars.salt;

import io.github.mortuusars.salt.configuration.Configuration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Melting {
    public static final DispenseItemBehavior SALT_DISPENSER_BEHAVIOR = new SaltItemDispenseBehavior();

    /**
     * Creative Delight: with Creative Common the click is Common's melting system (plan_bugfix_1114 3.8): its targets
     * (Melting.isTarget) and one stage per click (Melting.meltStep: packed and blue ice step down a tier, the rest melts),
     * no roll. Called by reflection, so this repack does not need Common to compile. Without Common, upstream's rule.
     */
    public static boolean tryMeltFromItem(BlockState targetState, BlockPos targetPos, Level level) {
        if (!Configuration.MELTING_ITEM_ENABLED.get()) return false;
        CommonMelting common = CommonMelting.get();
        if (common != null ? !common.isTarget(targetState) : !targetState.is(Salt.BlockTags.MELTABLES)) return false;

        if (level instanceof ServerLevel serverLevel) {
            if (common != null) return common.meltStep(serverLevel, targetPos);
            meltBlock(targetPos, serverLevel);
        } else {
            Vec3 center = Vec3.atCenterOf(targetPos);
            RandomSource random = level.getRandom();
            for (int i = 0; i < 6; i++) {
                level.addParticle(ParticleTypes.SPIT,
                        center.x + random.nextGaussian() * 0.35F,
                        center.y + 0.35F + random.nextGaussian() * 0.35F,
                        center.z + random.nextGaussian() * 0.35F,
                        0.0, 0.0, 0.0);
            }
        }
        return true;
    }

    /**
     * Creative Delight: switched off (plan_bugfix_1114 3.8). Salt blocks melting nearby ice and snow is Creative Common's
     * melting system ([melting] in creativecommon-common.toml), so this never fires and nothing melts twice. The
     * SaltBlockMeltingEnabled / Chance keys stay in the config, unused.
     */
    public static boolean tryMeltFromBlock(BlockPos pos, ServerLevel level) {
        return false;
    }

    /** Creative Delight: water for ice outside ultra-warm dimensions, air otherwise (upstream had the check inverted). */
    private static BlockState meltedState(BlockState oldState, ServerLevel level) {
        if (Configuration.MELTING_PLACES_WATER.get()
                && !level.dimensionType().ultraWarm()
                && oldState.is(net.minecraft.tags.BlockTags.ICE)) {
            return Blocks.WATER.defaultBlockState();
        }
        return Blocks.AIR.defaultBlockState();
    }

    public static void meltBlock(BlockPos pos, ServerLevel level) {
        BlockState oldState = level.getBlockState(pos);
        level.setBlockAndUpdate(pos, meltedState(oldState, level));

        Vec3 center = Vec3.atCenterOf(pos);
        RandomSource random = level.getRandom();
        level.playSound(null, center.x, center.y, center.z, Salt.Sounds.MELT.get(), SoundSource.BLOCKS,
                0.9F, random.nextFloat() * 0.2F + 0.9F);
        level.playSound(null, center.x, center.y, center.z, Salt.Sounds.SALT_DISSOLVE.get(), SoundSource.BLOCKS,
                0.9F, random.nextFloat() * 0.2F + 0.9F);

        BlockParticleOption particleType = new BlockParticleOption(ParticleTypes.BLOCK, oldState);
        for (int i = 0; i < 12; i++) {
            level.sendParticles(particleType,
                    center.x + random.nextGaussian() * 0.45F,
                    center.y + random.nextGaussian() * 0.45F,
                    center.z + random.nextGaussian() * 0.45F,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    /** Creative Common's com.SaintAlfonzo.creativecommon.melting.Melting, looked up once; null without Common. */
    private static final class CommonMelting {
        private static volatile boolean looked;
        private static CommonMelting instance;
        private final java.lang.invoke.MethodHandle isTarget;
        private final java.lang.invoke.MethodHandle meltStep;

        private CommonMelting(java.lang.invoke.MethodHandle isTarget, java.lang.invoke.MethodHandle meltStep) {
            this.isTarget = isTarget;
            this.meltStep = meltStep;
        }

        static CommonMelting get() {
            if (!looked) {
                synchronized (CommonMelting.class) {
                    if (!looked) {
                        try {
                            Class<?> c = Class.forName("com.SaintAlfonzo.creativecommon.melting.Melting");
                            java.lang.invoke.MethodHandles.Lookup l = java.lang.invoke.MethodHandles.publicLookup();
                            instance = new CommonMelting(
                                    l.findStatic(c, "isTarget", java.lang.invoke.MethodType.methodType(boolean.class, BlockState.class)),
                                    l.findStatic(c, "meltStep", java.lang.invoke.MethodType.methodType(boolean.class, ServerLevel.class, BlockPos.class)));
                        } catch (ReflectiveOperationException | LinkageError e) {
                            instance = null;
                        }
                        looked = true;
                    }
                }
            }
            return instance;
        }

        boolean isTarget(BlockState state) {
            try {
                return (boolean) isTarget.invokeExact(state);
            } catch (Throwable t) {
                throw new IllegalStateException("Creative Common Melting.isTarget failed", t);
            }
        }

        boolean meltStep(ServerLevel level, BlockPos pos) {
            try {
                return (boolean) meltStep.invokeExact(level, pos);
            } catch (Throwable t) {
                throw new IllegalStateException("Creative Common Melting.meltStep failed", t);
            }
        }
    }

    private static class SaltItemDispenseBehavior extends DefaultDispenseItemBehavior {
        @Override
        protected ItemStack execute(BlockSource source, ItemStack stack) {
            if (!stack.is(Salt.ItemTags.FORGE_SALTS)) return stack;

            Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
            ItemStack itemstack = stack.split(1);
            BlockPos targetPos = source.getPos().relative(direction);
            BlockState targetState = source.getLevel().getBlockState(targetPos);
            if (!Melting.tryMeltFromItem(targetState, targetPos, source.getLevel())) {
                Position position = DispenserBlock.getDispensePosition(source);
                spawnItem(source.getLevel(), itemstack, 6, direction, position);
            }
            return stack;
        }
    }
}
