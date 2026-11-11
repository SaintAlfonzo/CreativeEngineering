package com.SaintAlfonzo.creativedelight.farm.bucket;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** An empty wooden bucket milks adult cows (mooshrooms too) and goats into a wooden milk bucket, as vanilla's bucket does. */
public final class WoodenBucketMilking {

    private WoodenBucketMilking() {}

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        ItemStack held = event.getItemStack();
        if (!held.is(WoodenBuckets.EMPTY.get())) {
            return;
        }
        boolean cow = event.getTarget() instanceof Cow c && !c.isBaby();
        boolean goat = event.getTarget() instanceof Goat g && !g.isBaby();
        if (!cow && !goat) {
            return;
        }
        player.playSound(goat ? SoundEvents.GOAT_MILK : SoundEvents.COW_MILK, 1.0F, 1.0F);
        if (!event.getLevel().isClientSide) {
            player.setItemInHand(event.getHand(),
                    ItemUtils.createFilledResult(held, player, new ItemStack(WoodenBuckets.MILK.get())));
        }
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        event.setCanceled(true);
    }
}
