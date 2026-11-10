package com.SaintAlfonzo.creativedelight.hearth.gear;

import com.SaintAlfonzo.creativedelight.hearth.HHTags;
import com.SaintAlfonzo.creativedelight.hearth.block.HopsLatticeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.level.BlockEvent;

/** Hats in #hearthandharvest:farmers_hats give 1 to 2 XP per ripe crop harvested (D8, N21, N23-18: no durability cost). */
public final class FarmersHatEvents {
    private FarmersHatEvents() {
    }

    public static boolean wearing(Player player) {
        return player != null && player.getItemBySlot(EquipmentSlot.HEAD).is(HHTags.FARMERS_HATS);
    }

    public static boolean isRipe(BlockState state) {
        return state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)
                || state.getBlock() instanceof HopsLatticeBlock && state.getValue(HopsLatticeBlock.AGE) == HopsLatticeBlock.MAX_AGE;
    }

    public static void reward(ServerLevel level, BlockPos pos) {
        ExperienceOrb.award(level, Vec3.atCenterOf(pos), 1 + level.random.nextInt(2));
    }

    public static void onBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel level && wearing(event.getPlayer()) && isRipe(event.getState())) {
            reward(level, event.getPos());
        }
    }
}
