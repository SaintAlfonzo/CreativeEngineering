package com.SaintAlfonzo.incubation_additions.mixin;

import com.SaintAlfonzo.incubation_additions.goldenegg.GoldenEggLayer;
import com.SaintAlfonzo.incubation_additions.goldenegg.GoldenGooseEgg;
import com.teamabnormals.incubation.core.api.EggLayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.untitledduckmod.ModItems;
import net.untitledduckmod.ModSoundEvents;
import net.untitledduckmod.goose.GooseEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes Untitled Duck Mod geese an Incubation {@link EggLayer}; see {@link DuckEntityEggLayerMixin}
 * (GooseEntity.aiStep has the identical eggLayTime countdown and 6000 + rand(6000) reset).
 * Nests: nest.WaterfowlNests.
 *
 * Golden egg (goldenegg.GoldenGooseEgg): aiStep (m_8107_) runs the goal selector inside super.aiStep()
 * and only then counts eggLayTime down, dropping the egg at 0. The roll is injected at HEAD, the first
 * tick eggLayTime is at most 400 - before the goals have seen it, so before LayEggInNestGoal (active
 * below 400) can start the walk to a nest. A win keeps that goal off (LayEggInNestGoalMixin) until the
 * drop at 0, where the spawnAtLocation (m_19998_) call is redirected to the golden egg; the goose's own
 * sound before it and timer reset after it stay as they are. The roll state is not saved: a goose
 * reloaded inside the 400-tick window rolls again.
 */
@Mixin(value = GooseEntity.class, remap = false)
public abstract class GooseEntityEggLayerMixin extends TamableAnimal implements EggLayer, GoldenEggLayer {

    @Shadow
    private int eggLayTime;

    // This egg's roll is done / was won. Cleared whenever the timer is back above the nest window.
    @Unique
    private boolean incubation_additions$goldenRolled;
    @Unique
    private boolean incubation_additions$goldenDue;

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

    @Override
    public boolean incubation_additions$isGoldenEggDue() {
        return this.incubation_additions$goldenDue;
    }

    // 400 = LayEggInNestGoal.canEggBeLaid's window (eggTimer < 400).
    @Inject(method = "m_8107_", at = @At("HEAD"))
    private void incubation_additions$rollGoldenEgg(CallbackInfo ci) {
        if (this.level.isClientSide) return;
        if (this.eggLayTime > 400) {
            this.incubation_additions$goldenRolled = false;
            this.incubation_additions$goldenDue = false;
        } else if (!this.incubation_additions$goldenRolled) {
            this.incubation_additions$goldenRolled = true;
            this.incubation_additions$goldenDue = GoldenGooseEgg.roll(this.random);
        }
    }

    @Redirect(method = "m_8107_", at = @At(value = "INVOKE",
            target = "Lnet/untitledduckmod/goose/GooseEntity;m_19998_(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;"))
    private ItemEntity incubation_additions$layGoldenEgg(GooseEntity goose, ItemLike egg) {
        if (this.incubation_additions$goldenDue) {
            this.incubation_additions$goldenDue = false;
            ItemEntity golden = GoldenGooseEgg.drop(this);
            if (golden != null) return golden;
        }
        return this.spawnAtLocation(egg);
    }
}
