package com.SaintAlfonzo.creativedelight.barbeques.block;

import com.SaintAlfonzo.creativedelight.barbeques.registry.BBQBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Tray, 3 display slots, no logic (ported for evaluation, O12). */
public class TrayBlockEntity extends StorageBlockEntity {

    public TrayBlockEntity(BlockPos pos, BlockState state) {
        super(BBQBlocks.TRAY_BE.get(), pos, state, 3);
    }

    @Override
    public AABB getBox() {
        return TrayBlock.OUTER.bounds().move(getBlockPos()).deflate(0.01F);
    }

    @Override
    public boolean specialClick(Player player, int slot, InteractionHand hand) {
        return false;
    }
}
