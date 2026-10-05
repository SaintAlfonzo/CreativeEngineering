package com.SaintAlfonzo.creativeengineering.mixin;

import com.teamabnormals.incubation.core.api.EggLayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.untitledduckmod.ModItems;
import net.untitledduckmod.ModSoundEvents;
import net.untitledduckmod.goose.GooseEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Makes Untitled Duck Mod geese an Incubation {@link EggLayer}; see {@link DuckEntityEggLayerMixin}
 * (GooseEntity.aiStep has the identical eggLayTime countdown and 6000 + rand(6000) reset).
 * Nests: nest.WaterfowlNests.
 */
@Mixin(value = GooseEntity.class, remap = false)
public abstract class GooseEntityEggLayerMixin extends TamableAnimal implements EggLayer {

    @Shadow
    private int eggLayTime;

    protected GooseEntityEggLayerMixin(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    @Override
    public int getEggTimer() {
        return this.eggLayTime;
    }

    @Override
    public void setEggTimer(int time) {
        this.eggLayTime = time;
    }

    // Geese carry no jockeys.
    @Override
    public boolean isBirdJockey() {
        return false;
    }

    @Override
    public Item getEggItem() {
        return ModItems.getGooseEgg();
    }

    // Same reset GooseEntity.aiStep uses after a lay.
    @Override
    public int getNextEggTime(RandomSource random) {
        return random.nextInt(6000) + 6000;
    }

    @Override
    public SoundEvent getEggLayingSound() {
        return ModSoundEvents.getGooseEggSound();
    }
}
