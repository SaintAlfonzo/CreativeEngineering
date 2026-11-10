package com.SaintAlfonzo.creativedelight.hearth.gear;

import com.SaintAlfonzo.creativedelight.hearth.item.HHItems;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * D7: Delightful's cooked marshmallow on a stick, held 10 s within a block of a fire or campfire (or while burning),
 * chars. Delightful's item is not ours, so this is a player tick and not an inventoryTick override.
 */
public final class RoastingEvents {
    private static final ResourceLocation COOKED = new ResourceLocation("delightful", "cooked_marshmallow_stick");
    private static final String TAG_ROAST = "HHRoast";
    private static final int SECONDS = 10;

    private RoastingEvents() {
    }

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (event.phase != TickEvent.Phase.END || player.level.isClientSide || player.level.getGameTime() % 20L != 0L) {
            return;
        }
        Item cooked = ForgeRegistries.ITEMS.getValue(COOKED);
        if (cooked == null) {
            return;
        }
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.is(cooked) && nearHeat(player, player.level)) {
                roast(player, hand, stack);
            }
        }
    }

    private static void roast(Player player, InteractionHand hand, ItemStack stack) {
        int time = stack.getOrCreateTag().getInt(TAG_ROAST) + 1;
        if (time < SECONDS) {
            stack.getTag().putInt(TAG_ROAST, time);
            return;
        }
        ItemStack charred = new ItemStack(HHItems.CHARRED_MARSHMALLOW_STICK.get());
        if (stack.getCount() > 1) {
            stack.shrink(1);
            stack.removeTagKey(TAG_ROAST);
            if (!player.getInventory().add(charred)) {
                player.drop(charred, false);
            }
        } else {
            player.setItemInHand(hand, charred);
        }
    }

    private static boolean nearHeat(Player player, Level level) {
        if (player.isOnFire()) {
            return true;
        }
        BlockPos pos = player.blockPosition();
        for (BlockPos near : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            if (level.getBlockState(near).is(BlockTags.CAMPFIRES) || level.getBlockState(near).is(BlockTags.FIRE)) {
                return true;
            }
        }
        return false;
    }
}
