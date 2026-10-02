package com.SaintAlfonzo.creativeengineering.items;

import com.SaintAlfonzo.creativeengineering.block.CreativeEngineeringBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * TEMPORARY testing behavior: places TerraFireBlock on any air block
 * adjacent to the clicked face, with no portal-frame validation at all.
 * This intentionally skips FlintAndSteelItem's inherited useOn (and vanilla's
 * canBePlacedAt/isPortal checks) so you can confirm the block's model, light
 * level, and general appearance without needing a real obsidian frame.
 *
 * Once that looks right, this needs to be replaced with real frame-aware
 * logic (only place on a valid obsidian frame interior, tie into the
 * PortalFailureHandler success path) - this version is a stand-in.
 */
public class FlintAndTerraSteelItem extends FlintAndSteelItem {
    public FlintAndTerraSteelItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos targetPos = context.getClickedPos().relative(context.getClickedFace());
        BlockState targetState = level.getBlockState(targetPos);

        if (!targetState.isAir()) {
            return InteractionResult.FAIL;
        }

        level.playSound(context.getPlayer(), targetPos, SoundEvents.FLINTANDSTEEL_USE,
                SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);

        level.setBlock(targetPos, CreativeEngineeringBlocks.DARK_FIRE.get().defaultBlockState(), 11);
        level.gameEvent(context.getPlayer(), GameEvent.BLOCK_PLACE, targetPos);

        Player player = context.getPlayer();
        if (player != null) {
            ItemStack stack = context.getItemInHand();
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(context.getHand()));
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
