package com.SaintAlfonzo.creativedelight.hearth.item;

import java.util.function.Supplier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

/**
 * A bucket for a fluid with no world block (sap, brine). A plain BucketItem would empty into air and lose the fluid,
 * as upstream's does, so right-clicking does nothing; tanks and machines still fill and drain it.
 */
public class CarrierBucketItem extends BucketItem {
    public CarrierBucketItem(Supplier<? extends Fluid> fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }
}
