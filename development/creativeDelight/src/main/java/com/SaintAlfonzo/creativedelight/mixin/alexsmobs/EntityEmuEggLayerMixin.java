package com.SaintAlfonzo.creativedelight.mixin.alexsmobs;

import com.github.alexthe666.alexsmobs.entity.EntityEmu;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.teamabnormals.incubation.core.api.EggLayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Makes Alex's Mobs emus an Incubation {@link EggLayer}, so Incubation's join-world handler gives
 * them its LayEggInNestGoal (walk to a nest in the last 400 ticks before laying, lay into it
 * instead of dropping the egg). The emu's own egg timer already works exactly like the chicken's
 * (EntityEmu.tick counts timeUntilNextEgg down on adults, drops an emu egg at 0, resets to
 * 6000 + rand(6000)), so this only exposes it. Nests: nest.EmuNests.
 */
@Mixin(value = EntityEmu.class, remap = false)
public abstract class EntityEmuEggLayerMixin extends Animal implements EggLayer {

    @Shadow
    public int timeUntilNextEgg;

    protected EntityEmuEggLayerMixin(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    @Override
    public int getEggTimer() {
        return this.timeUntilNextEgg;
    }

    @Override
    public void setEggTimer(int time) {
        this.timeUntilNextEgg = time;
    }

    // Emus carry no jockeys.
    @Override
    public boolean isBirdJockey() {
        return false;
    }

    @Override
    public Item getEggItem() {
        return AMItemRegistry.EMU_EGG.get();
    }

    // Same reset EntityEmu.tick uses after a lay.
    @Override
    public int getNextEggTime(RandomSource random) {
        return random.nextInt(6000) + 6000;
    }

    @Override
    public SoundEvent getEggLayingSound() {
        return SoundEvents.CHICKEN_EGG;
    }
}
