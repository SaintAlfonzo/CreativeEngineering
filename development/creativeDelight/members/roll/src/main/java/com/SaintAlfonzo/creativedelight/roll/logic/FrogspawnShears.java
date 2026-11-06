package com.SaintAlfonzo.creativedelight.roll.logic;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.level.BlockEvent;

/**
 * Breaking frogspawn with shears in the main hand drops it (roll_delight.md 2.4, D9): the survival source of the Tobiko
 * Roll's input. By hand it still drops nothing, as vanilla.
 */
public final class FrogspawnShears {
    private FrogspawnShears() {
    }

    public static void onBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (!event.getState().is(Blocks.FROGSPAWN) || player.getAbilities().instabuild || !(event.getLevel() instanceof Level level)
                || level.isClientSide) {
            return;
        }
        ItemStack tool = player.getMainHandItem();
        if (tool.getItem() instanceof ShearsItem || tool.is(Tags.Items.SHEARS)) {
            Block.popResource(level, event.getPos(), new ItemStack(Items.FROGSPAWN));
        }
    }
}
