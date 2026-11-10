package com.SaintAlfonzo.creativedelight.hearth.entity;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ThrownCleaver extends ThrownTool {
    public ThrownCleaver(EntityType<? extends ThrownCleaver> type, Level level) {
        super(type, level);
    }

    public ThrownCleaver(Level level, LivingEntity shooter, ItemStack stack) {
        super(HHEntities.THROWN_CLEAVER.get(), level, shooter, stack, 2.0D);
    }

    @Override
    protected int maxTargets() {
        return 1;
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.ITEM_BREAK;
    }
}
