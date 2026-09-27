package com.SaintAlfonzo.creativeengineering.fan;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativeengineering.CreativeEngineering;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingTypeRegistry;
import com.simibubi.create.foundation.recipe.RecipeApplier;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.wrapper.RecipeWrapper;

/**
 * "Dragon Breathing" Create fan processing: a dragon head facing away from an Encased
 * Fan acts as a catalyst, transforming items blown through it and belching dragon's
 * breath. Ported from Create: Halitosis ({@code HalitosisFanProcessingTypes.HalitosisType})
 * to Create 0.5.1.i / MC 1.19.2.
 *
 * <p>Registered into {@link FanProcessingTypeRegistry} at common setup; Create's
 * {@code AirCurrent} then drives {@link #isValidAt}, {@link #canProcess}/{@link #process},
 * {@link #spawnProcessingParticles}, {@link #morphAirFlow} and {@link #affectEntity} with
 * no further hooks needed.
 */
public class DragonBreathingFanProcessingType implements FanProcessingType {

    public static final DragonBreathingFanProcessingType INSTANCE = new DragonBreathingFanProcessingType();

    /** Blocks that can act as this catalyst; the two dragon heads by default (see data tag). */
    public static final TagKey<Block> CATALYST_TAG = BlockTags.create(
            new ResourceLocation(CreativeEngineering.MODID, "fan_processing_catalysts/dragon_breathing"));

    private static final RecipeWrapper RECIPE_WRAPPER = new DragonBreathingRecipe.Wrapper();

    private static final int COLOR_A = 11632869;
    private static final int COLOR_B = 7558074;

    private DragonBreathingFanProcessingType() {}

    /** Called once from common setup (after Create's own {@code onCtor} registration). */
    public static void register() {
        FanProcessingTypeRegistry.register(DragonBreathingRecipeType.ID, INSTANCE);
    }

    @Override
    public boolean isValidAt(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(CATALYST_TAG)) {
            return false;
        }
        // Non-dragon-head catalysts added via the tag work unconditionally.
        if (!state.is(Blocks.DRAGON_HEAD) && !state.is(Blocks.DRAGON_WALL_HEAD)) {
            return true;
        }
        // A dragon head must point along a cardinal, away from an Encased Fan behind it.
        Optional<Direction> facing = CatalystUtils.getDirection(state);
        return facing.isPresent()
                && level.getBlockState(pos.relative(facing.get().getOpposite())).is(AllBlocks.ENCASED_FAN.get());
    }

    @Override
    public int getPriority() {
        // Above SPLASHING (400) so a dragon head standing in water still belches.
        return 500;
    }

    @Override
    public boolean canProcess(ItemStack stack, Level level) {
        RECIPE_WRAPPER.setItem(0, stack);
        return DragonBreathingRecipeType.INSTANCE.find(RECIPE_WRAPPER, level).isPresent();
    }

    @Override
    @Nullable
    public List<ItemStack> process(ItemStack stack, Level level) {
        RECIPE_WRAPPER.setItem(0, stack);
        Optional<Recipe<RecipeWrapper>> recipe = DragonBreathingRecipeType.INSTANCE.find(RECIPE_WRAPPER, level);
        return recipe.isPresent() ? RecipeApplier.applyRecipeOn(stack, recipe.get()) : null;
    }

    @Override
    public void spawnProcessingParticles(Level level, Vec3 pos) {
        if (level.random.nextInt(4) != 0) {
            return;
        }
        Vec3 offset = VecHelper.offsetRandomly(Vec3.ZERO, level.random, 1.0F)
                .multiply(1.0D, 0.05D, 1.0D).normalize().scale(0.15F);
        pos = pos.add(offset);
        level.addParticle(new DustParticleOptions(new Color(COLOR_A).asVectorF(), 1.0F),
                pos.x + (level.random.nextFloat() - 0.5F) * 0.5F,
                pos.y + 0.5D,
                pos.z + (level.random.nextFloat() - 0.5F) * 0.5F,
                0.0D, 0.125D, 0.0D);
    }

    @Override
    public void morphAirFlow(AirFlowParticleAccess particleAccess, RandomSource random) {
        particleAccess.setColor(Color.mixColors(COLOR_A, COLOR_B, random.nextFloat()));
        particleAccess.setAlpha(0.5F);
        // DRAGON_BREATH particles have hasPhysics=false, friction 0.96 and a lifetime up to
        // ~100t, so they must be spawned nearly motionless or they shoot through walls and
        // drift to the render edge. spawnExtraParticle multiplies the (large, maxDistance-
        // scaled) air-flow velocity by this factor; Create's own fan types stay <= 0.125F.
        if (random.nextFloat() < 0.03125F) {
            particleAccess.spawnExtraParticle(ParticleTypes.DRAGON_BREATH, 0.05F);
        }
    }

    @Override
    public void affectEntity(Entity entity, Level level) {
        if (!level.isClientSide) {
            entity.hurt(DamageSource.DRAGON_BREATH, 4.0F);
        }
    }
}
