// Salt (mortuusars), MIT: a modified copy of the upstream io.github.mortuusars.salt.Melting for the Creative
// Delight repack (members/salt/NOTICE-upstream-MIT.txt). The edit is GNU GPL3 like the rest of the pack's code.
// Change against upstream 1.2.4.1 (meltBlock only, feature_writeup/salt.md 6 S8): upstream placed water only in
// ultra-warm dimensions and air everywhere else (the dimension check was inverted). Now melted ice leaves water
// outside ultra-warm dimensions (SaltMeltingPlaceWater still switches it off); snow, and anything melted in an
// ultra-warm dimension (the Nether), leaves air. Since 2026-11-14 tryMeltFromBlock is off: Creative Common's melting
// system owns block melting (plan_bugfix_1114 3.8). Since 2026-11-15 (plan_post_scratch_1115 B2, B6) the dispenser
// behaviour melts a frozen block one stage (1 salt) or places a salt layer on air, for any salt item it is registered
// on (Creative Delight registers it on Vintage Delight's salt dust too); isMeltTarget / meltOnce serve the Salt Melt.
package io.github.mortuusars.salt;

import io.github.mortuusars.salt.configuration.Configuration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Melting {
    public static final DispenseItemBehavior SALT_DISPENSER_BEHAVIOR = new SaltItemDispenseBehavior();

    /**
     * Creative Delight: a block salt can melt: Creative Common's Melting.isSaltable (plan_post_scratch_1115 G7.11; isTarget
     * on an older Common) or, without Common, upstream's tag.
     */
    public static boolean isMeltTarget(BlockState state) {
        CommonMelting common = CommonMelting.get();
        return common != null ? common.isTarget(state) : state.is(Salt.BlockTags.MELTABLES);
    }

    /**
     * Creative Delight: one melt action on a target, no roll: Common's meltStep (packed and blue ice step down a tier,
     * the rest melts) or, without Common, upstream's meltBlock. False when the block is not a target. Not gated by
     * SaltItemMeltingEnabled (the Salt Melt is its own item).
     */
    public static boolean meltOnce(BlockState state, BlockPos pos, ServerLevel level) {
        if (!isMeltTarget(state)) return false;
        CommonMelting common = CommonMelting.get();
        if (common != null) return common.meltStep(level, pos);
        meltBlock(pos, level);
        return true;
    }

    /** Creative Delight: Vintage Delight's salt layer block, or null without Vintage Delight. */
    @javax.annotation.Nullable
    public static Block saltLayer() {
        Block b = ForgeRegistries.BLOCKS.getValue(SALT_LAYER_ID);
        return b instanceof SnowLayerBlock ? b : null;
    }

    /**
     * Creative Delight (B2): one salt layer at pos: a new layer on air where a layer can stand, or one more on a salt
     * layer below 8, or the Salt Block on a full pile of 8. False (nothing placed) otherwise.
     */
    public static boolean placeSaltLayer(ServerLevel level, BlockPos pos) {
        Block layer = saltLayer();
        if (layer == null) return false;
        BlockState here = level.getBlockState(pos);
        if (here.is(layer)) {
            int n = here.getValue(SnowLayerBlock.LAYERS);
            // a 9th salt on a full pile makes the Salt Block (9 salt), like salt dust by hand
            Block block = ForgeRegistries.BLOCKS.getValue(SALT_BLOCK_ID);
            if (n >= 8 && (block == null || block == Blocks.AIR)) return false;
            level.setBlockAndUpdate(pos, n >= 8 ? block.defaultBlockState() : here.setValue(SnowLayerBlock.LAYERS, n + 1));
            level.playSound(null, pos, SoundEvents.SAND_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            return true;
        }
        BlockState placed = layer.defaultBlockState();
        if (!here.isAir() || !placed.canSurvive(level, pos)) return false;
        level.setBlockAndUpdate(pos, placed);
        return true;
    }

    private static final ResourceLocation SALT_LAYER_ID = new ResourceLocation("vintagedelight", "salt");
    private static final ResourceLocation SALT_BLOCK_ID = new ResourceLocation("salt", "salt_block");

    /**
     * Creative Delight: with Creative Common the click is Common's melting system (plan_bugfix_1114 3.8): its targets
     * (Melting.isTarget) and one stage per click (Melting.meltStep: packed and blue ice step down a tier, the rest melts),
     * no roll. Called by reflection, so this repack does not need Common to compile. Without Common, upstream's rule.
     */
    public static boolean tryMeltFromItem(BlockState targetState, BlockPos targetPos, Level level) {
        if (!Configuration.MELTING_ITEM_ENABLED.get()) return false;
        if (!isMeltTarget(targetState)) return false;

        if (level instanceof ServerLevel serverLevel) {
            return meltOnce(targetState, targetPos, serverLevel);
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
                            java.lang.invoke.MethodType test = java.lang.invoke.MethodType.methodType(boolean.class, BlockState.class);
                            // Salt melts only saltable blocks (Common's creativecommon:melting/saltable, G7.11); an
                            // older Common without isSaltable falls back to every melting target.
                            java.lang.invoke.MethodHandle target;
                            try {
                                target = l.findStatic(c, "isSaltable", test);
                            } catch (NoSuchMethodException e) {
                                target = l.findStatic(c, "isTarget", test);
                            }
                            instance = new CommonMelting(
                                    target,
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

    /**
     * Creative Delight (plan_post_scratch_1115 B2): a frozen block in front melts one stage, else a salt layer is placed
     * in front (air or a salt layer below 8); either way 1 salt is spent. Anything else: the salt is dropped as usual.
     * Upstream returned at once unless the stack was #forge:salts, which in this pack holds only salt dust, so the
     * behaviour registered for salt:salt did nothing.
     */
    private static class SaltItemDispenseBehavior extends DefaultDispenseItemBehavior {
        @Override
        protected ItemStack execute(BlockSource source, ItemStack stack) {
            Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
            BlockPos targetPos = source.getPos().relative(direction);
            ServerLevel level = source.getLevel();
            BlockState targetState = level.getBlockState(targetPos);
            if (Melting.tryMeltFromItem(targetState, targetPos, level) || placeSaltLayer(level, targetPos)) {
                stack.shrink(1);
                return stack;
            }
            return super.execute(source, stack);
        }
    }
}
