package com.SaintAlfonzo.creativedelight.crops;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * A water bucket or water bottle used on soil with farmland moisture (Farmland, Rich Farmland, Fertile Soil) soaks it,
 * the same wetness the Watering Can gives: moisture to full ({@code FarmBlock.MOISTURE} 7; on Fertile Soil 7 is
 * "soaked"). A bucket soaks the 3x3 around the soil and turns into an empty bucket; a bottle soaks the one block and
 * turns into a glass bottle (farming/crops #17, plan_farming_cooking_batch A13 / Q10).
 *
 * <p>Taken: a click on the soil's top face, or on a plant without collision (a crop) standing on it, while not sneaking
 * and only when something in reach is still dry. Otherwise the click is left alone, so a sneak click, a side-face click
 * (placing water in a channel) or a click on wet soil keeps the vanilla bucket and bottle. Runs at HIGH so the
 * soaking wins over Quark's right-click harvest (which skips cancelled clicks) and Immersive Weathering's own
 * single-block bucket on Fertile Soil. Both sides decide the same from the block states; only the server changes them.
 * Creative mode keeps the full bucket or bottle.
 */
public final class WaterOnSoil {

    private WaterOnSoil() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player == null || player.isSpectator() || player.isSecondaryUseActive()) return;
        ItemStack stack = event.getItemStack();
        boolean bucket = stack.is(Items.WATER_BUCKET);
        boolean bottle = !bucket && stack.is(Items.POTION) && PotionUtils.getPotion(stack) == Potions.WATER;
        if (!bucket && !bottle) return;

        Level level = event.getLevel();
        BlockPos soil = soilFor(level, event.getPos(), event.getFace());
        if (soil == null) return;
        int radius = bucket ? 1 : 0;
        if (!hasDrySoil(level, soil, radius)) return;

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
        if (!(level instanceof ServerLevel server)) return;

        wetSoil(server, soil, radius);
        server.playSound(null, soil, bucket ? SoundEvents.BUCKET_EMPTY : SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        InteractionHand hand = event.getHand();
        ItemStack empty = new ItemStack(bucket ? Items.BUCKET : Items.GLASS_BOTTLE);
        player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, empty));
    }

    /** The soil the click aims at: the clicked soil from above, or the soil under a clicked crop; null otherwise. */
    static BlockPos soilFor(Level level, BlockPos pos, Direction face) {
        BlockState clicked = level.getBlockState(pos);
        if (isSoil(clicked)) return face == Direction.UP ? pos : null;
        BlockPos below = pos.below();
        if (isSoil(level.getBlockState(below)) && clicked.getCollisionShape(level, pos).isEmpty()) return below;
        return null;
    }

    /** Anything with farmland moisture: Farmland, Rich Farmland, Fertile Soil. */
    static boolean isSoil(BlockState state) {
        return state.hasProperty(FarmBlock.MOISTURE);
    }

    static boolean isDry(BlockState state) {
        return isSoil(state) && state.getValue(FarmBlock.MOISTURE) < FarmBlock.MAX_MOISTURE;
    }

    private static boolean hasDrySoil(Level level, BlockPos center, int r) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, 0, -r), center.offset(r, 0, r))) {
            if (isDry(level.getBlockState(p))) return true;
        }
        return false;
    }

    private static void wetSoil(ServerLevel level, BlockPos center, int r) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, 0, -r), center.offset(r, 0, r))) {
            BlockState state = level.getBlockState(p);
            if (!isDry(state)) continue;
            level.setBlock(p, state.setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE), 2);
            level.sendParticles(ParticleTypes.SPLASH, p.getX() + 0.5, p.getY() + 1.0, p.getZ() + 0.5, 4, 0.3, 0.05, 0.3, 0.0);
        }
    }
}
