package com.SaintAlfonzo.creativedelight.barbeques.block;

import com.SaintAlfonzo.creativedelight.barbeques.recipe.SkeweringRecipe;
import com.SaintAlfonzo.creativedelight.barbeques.registry.BBQBlocks;
import com.SaintAlfonzo.creativedelight.barbeques.registry.BBQRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Ingredients Basin, 2 slots; a stick in the main hand on a slot makes a skewer (barbequesdelight:skewering). */
public class BasinBlockEntity extends StorageBlockEntity {

    public BasinBlockEntity(BlockPos pos, BlockState state) {
        super(BBQBlocks.BASIN_BE.get(), pos, state, 2);
    }

    @Override
    public AABB getBox() {
        return BasinBlock.OUTER.bounds().move(getBlockPos()).deflate(0.01F);
    }

    @Override
    public boolean specialClick(Player user, int slot, InteractionHand hand) {
        if (level == null) {
            return false;
        }
        ItemStack stick = user.getMainHandItem();
        ItemStack basin = getStack(slot);
        ItemStack side = user.getOffhandItem();
        SkeweringRecipe recipe = null;
        for (SkeweringRecipe r : level.getRecipeManager().getAllRecipesFor(BBQRecipes.SKEWERING.get())) {
            if (r.matches(stick, basin, side)) {
                recipe = r;
                break;
            }
        }
        if (recipe == null) {
            return false;
        }
        if (level.isClientSide) {
            return true;
        }
        // the basin stack is shrunk in place: copy, shrink, put back so the handler sees the change
        ItemStack stored = basin.copy();
        ItemStack ret = recipe.craft(stick, stored, side);
        setStack(slot, stored);
        if (user.getItemInHand(hand).isEmpty()) {
            user.setItemInHand(hand, ret);
        } else {
            user.getInventory().placeItemBackInInventory(ret);
        }
        notifyTile();
        return true;
    }
}
