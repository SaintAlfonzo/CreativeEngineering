package com.SaintAlfonzo.creativedelight.hearth.gear;

import com.SaintAlfonzo.creativedelight.hearth.item.HHItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/** Horses eat hops (3.1): heal 2, age a foal by 20 s, +3 temper. An event, since the mixin 1.4.1 uses needs MixinExtras. */
public final class HorseFeedEvents {
    private HorseFeedEvents() {
    }

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof AbstractHorse horse)) {
            return;
        }
        ItemStack stack = event.getItemStack();
        if (!stack.is(HHItems.HOPS.get())) {
            return;
        }
        boolean wants = horse.getHealth() < horse.getMaxHealth() || horse.isBaby() || !horse.isTamed() && horse.getTemper() < horse.getMaxTemper();
        if (!wants) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(horse.level.isClientSide));
        if (horse.level.isClientSide) {
            return;
        }
        Player player = event.getEntity();
        horse.heal(2.0F);
        if (horse.isBaby()) {
            horse.ageUp(20);
        }
        if (!horse.isTamed() && horse.getTemper() < horse.getMaxTemper()) {
            horse.modifyTemper(3);
        }
        horse.level.playSound(null, horse.getX(), horse.getY(), horse.getZ(), SoundEvents.HORSE_EAT, SoundSource.NEUTRAL, 1.0F, 1.0F);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }
}
