// Salt (mortuusars), MIT: a modified copy of the upstream io.github.mortuusars.salt.Melting for the Creative
// Delight repack (members/salt/NOTICE-upstream-MIT.txt). The edit is GNU GPL3 like the rest of the pack's code.
// Change against upstream 1.2.4.1 (meltBlock only, feature_writeup/salt.md 6 S8): upstream placed water only in
// ultra-warm dimensions and air everywhere else (the dimension check was inverted). Now melted ice leaves water
// outside ultra-warm dimensions (SaltMeltingPlaceWater still switches it off); snow, and anything melted in an
// ultra-warm dimension (the Nether), leaves air.
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

    public static boolean tryMeltFromItem(BlockState targetState, BlockPos targetPos, Level level) {
        if (!Configuration.MELTING_ITEM_ENABLED.get() || !targetState.is(Salt.BlockTags.MELTABLES)) return false;

        if (level instanceof ServerLevel serverLevel) {
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

    public static boolean tryMeltFromBlock(BlockPos pos, ServerLevel level) {
        if (Configuration.MELTING_BY_BLOCK_ENABLED.get()
                && level.getBlockState(pos).is(Salt.BlockTags.MELTABLES)
                && level.random.nextDouble() < Configuration.MELTING_BLOCK_CHANCE.get()) {
            meltBlock(pos, level);
            return true;
        }
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
