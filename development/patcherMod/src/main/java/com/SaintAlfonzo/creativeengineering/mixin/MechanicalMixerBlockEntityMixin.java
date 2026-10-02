package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.fluidbrewing.DragonBreathBrewing;
import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.processing.basin.BasinOperatingBlockEntity;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Create's Mechanical Mixer only discovers "Automatic Brewing" recipes
 * ({@link com.simibubi.create.content.fluids.potion.PotionMixingRecipes}) by
 * looking up the <em>item</em> reagents present in the basin
 * ({@code PotionMixingRecipes.BY_ITEM.get(item)}). The pack's dragon's breath
 * conversion uses a <em>fluid</em> reagent, so it is invisible to that loop.
 *
 * <p>{@code getMatchingRecipes()} is trusted downstream — {@code updateRecipe}
 * takes {@code list.get(0)} without re-checking — so we filter our fluid brewing
 * recipes with the inherited {@code matchBasinRecipe} before appending, exactly
 * like the vanilla item loop does after its {@code BY_ITEM} lookup.
 *
 * <p>Extends {@link BasinOperatingBlockEntity} (the target's direct superclass)
 * so {@code matchBasinRecipe} is a plain inherited call rather than an
 * {@code @Shadow} of an inherited member.
 *
 * @see DragonBreathBrewing
 */
@Mixin(value = MechanicalMixerBlockEntity.class, remap = false)
public abstract class MechanicalMixerBlockEntityMixin extends BasinOperatingBlockEntity {

    private MechanicalMixerBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "getMatchingRecipes", at = @At("RETURN"))
    private void creativeengineering$dragonBreathFluidBrewing(CallbackInfoReturnable<List<Recipe<?>>> cir) {
        if (DragonBreathBrewing.FLUID_BREWING.isEmpty()) return;
        if (!AllConfigs.server().recipes.allowBrewingInMixer.get()) return;

        List<Recipe<?>> matching = cir.getReturnValue();
        for (MixingRecipe recipe : DragonBreathBrewing.FLUID_BREWING) {
            if (matchBasinRecipe(recipe)) {
                matching.add(recipe);
            }
        }
    }
}
