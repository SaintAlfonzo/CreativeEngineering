package com.SaintAlfonzo.incubation_additions.mixin;

import com.teamabnormals.incubation.core.api.EggLayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.untitledduckmod.ModItems;
import net.untitledduckmod.ModSoundEvents;
import net.untitledduckmod.duck.DuckEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Makes Untitled Duck Mod ducks an Incubation {@link EggLayer}, so Incubation's join-world handler
 * gives them its LayEggInNestGoal (walk to a nest in the last 400 ticks before laying, lay into it
 * instead of dropping the egg). The duck's own timer already works exactly like the chicken's
 * (DuckEntity.aiStep counts eggLayTime down on adult ducks, drops a duck egg at 0, resets to
 * 6000 + rand(6000)), so this only exposes it. Nests: nest.WaterfowlNests.
 */
@Mixin(value = DuckEntity.class, remap = false)
public abstract class DuckEntityEggLayerMixin extends Animal implements EggLayer {

    @Shadow
    private int eggLayTime;

    protected DuckEntityEggLayerMixin(EntityType<? extends Animal> type, Level level) {
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

    // Ducks carry no jockeys.
    @Override
    public boolean isBirdJockey() {
        return false;
    }

    @Override
    public Item getEggItem() {
        return ModItems.getDuckEgg();
    }

    // Same reset DuckEntity.aiStep uses after a lay.
    @Override
    public int getNextEggTime(RandomSource random) {
        return random.nextInt(6000) + 6000;
    }

    @Override
    public SoundEvent getEggLayingSound() {
        return ModSoundEvents.getDuckEggSound();
    }
}
