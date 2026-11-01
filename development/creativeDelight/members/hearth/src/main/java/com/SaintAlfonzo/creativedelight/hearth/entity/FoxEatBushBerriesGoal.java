package com.SaintAlfonzo.creativedelight.hearth.entity;

import com.SaintAlfonzo.creativedelight.hearth.block.BerryBush;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.event.ForgeEventFactory;

/**
 * Foxes pick ripe {@link BerryBush} bushes (age 2 or more) as vanilla foxes pick sweet berries (1.4.1): walk up, wait
 * 40 ticks, take one berry into the mouth if it is empty, drop the rest (1-2, one more at age 3), bush back to age 1.
 * Needs mob griefing.
 */
public class FoxEatBushBerriesGoal extends MoveToBlockGoal {
    private static final int WAIT_TICKS = 40;
    private static final int RIPE_AGE = 2;
    private final Fox fox;
    private int ticksWaited;

    public FoxEatBushBerriesGoal(Fox fox, double speedModifier, int searchRange, int verticalSearchRange) {
        super(fox, speedModifier, searchRange, verticalSearchRange);
        this.fox = fox;
    }

    @Override
    public double acceptedDistance() {
        return 2.0D;
    }

    @Override
    public boolean shouldRecalculatePath() {
        return this.tryTicks % 100 == 0;
    }

    @Override
    protected boolean isValidTarget(LevelReader level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof BerryBush && state.getValue(SweetBerryBushBlock.AGE) >= RIPE_AGE;
    }

    @Override
    public boolean canUse() {
        return !this.fox.isSleeping() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !this.fox.isSleeping() && super.canContinueToUse();
    }

    @Override
    public void start() {
        this.ticksWaited = 0;
        super.start();
    }

    @Override
    public void tick() {
        if (this.isReachedTarget()) {
            if (this.ticksWaited >= WAIT_TICKS) {
                pickBerries();
            } else {
                this.ticksWaited++;
            }
        }
        super.tick();
    }

    private void pickBerries() {
        Level level = this.fox.level;
        if (!ForgeEventFactory.getMobGriefingEvent(level, this.fox)) {
            return;
        }
        BlockState state = level.getBlockState(this.blockPos);
        if (!(state.getBlock() instanceof BerryBush bush) || state.getValue(SweetBerryBushBlock.AGE) < RIPE_AGE) {
            return;
        }
        int age = state.getValue(SweetBerryBushBlock.AGE);
        int count = 1 + level.random.nextInt(2) + (age == SweetBerryBushBlock.MAX_AGE ? 1 : 0);
        if (this.fox.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
            this.fox.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(bush.getBerry()));
            count--;
        }
        if (count > 0) {
            Block.popResource(level, this.blockPos, new ItemStack(bush.getBerry(), count));
        }
        this.fox.playSound(SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, 1.0F, 1.0F);
        BlockState picked = state.setValue(SweetBerryBushBlock.AGE, 1);
        level.setBlock(this.blockPos, picked, 2);
        level.gameEvent(GameEvent.BLOCK_CHANGE, this.blockPos, GameEvent.Context.of(this.fox, picked));
        this.ticksWaited = 0;
    }
}
