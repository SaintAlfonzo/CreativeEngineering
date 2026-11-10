package com.SaintAlfonzo.creativedelight.hearth.entity;

import com.SaintAlfonzo.creativedelight.hearth.HHTags;
import com.SaintAlfonzo.creativedelight.hearth.effect.HHEffects;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;

/** The only source of Pinned (N23-5): 10 s, 3 s on players; bosses, flying and swimming mobs take the hit but no effect (N25). */
public class ThrownPitchfork extends ThrownTool {
    public ThrownPitchfork(EntityType<? extends ThrownPitchfork> type, Level level) {
        super(type, level);
    }

    public ThrownPitchfork(Level level, LivingEntity shooter, ItemStack stack) {
        super(HHEntities.THROWN_PITCHFORK.get(), level, shooter, stack, 2.5D);
    }

    @Override
    protected int maxTargets() {
        return 3;
    }

    @Override
    protected void onHurtTarget(LivingEntity target) {
        if (!isPinnedImmune(target)) target.addEffect(new MobEffectInstance(HHEffects.PINNED.get(), target instanceof Player ? 60 : 200, 0));
    }

    private static boolean isPinnedImmune(LivingEntity target) {
        return target.getType().is(Tags.EntityTypes.BOSSES) || target.getType().is(HHTags.PINNED_IMMUNE)
                || target instanceof FlyingAnimal || target instanceof FlyingMob || target instanceof WaterAnimal
                || (target instanceof Mob mob && (mob.getNavigation() instanceof FlyingPathNavigation || mob.getNavigation() instanceof WaterBoundPathNavigation));
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }
}
