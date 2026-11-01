package net.satisfy.vinery.core.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.vinery.core.block.GrapeBushBlock;
import net.satisfy.vinery.core.wine.GrapeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Foxes eat ripe grapes from grape bushes. */
@Mixin(targets = "net.minecraft.world.entity.animal.Fox$FoxEatBerriesGoal")
public abstract class FoxEatBerriesGoalMixin extends MoveToBlockGoal {
    public FoxEatBerriesGoalMixin(PathfinderMob mob, double speed, int range) {
        super(mob, speed, range);
    }

    @Inject(method = "isValidTarget", at = @At("HEAD"), cancellable = true)
    private void vinery$isTargetPos(LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof GrapeBushBlock) {
            cir.setReturnValue(state.getValue(GrapeBushBlock.AGE) >= 2);
        }
    }

    @Inject(method = "onReachedTarget", at = @At("TAIL"))
    private void vinery$eatGrapes(CallbackInfo ci) {
        final BlockState state = vinery$fox().level.getBlockState(this.blockPos);
        if (state.getBlock() instanceof GrapeBushBlock bush) {
            vinery$pickGrapes(state, bush.grapeType());
        }
    }

    /** The goal's owner; MoveToBlockGoal.mob is the same Fox as the synthetic outer field (f_28672_). */
    @Unique
    private Fox vinery$fox() {
        return (Fox) this.mob;
    }

    @Unique
    private void vinery$pickGrapes(BlockState state, GrapeType type) {
        final int age = state.getValue(GrapeBushBlock.AGE);
        int j = 1 + vinery$fox().level.random.nextInt(2) + (age == 3 ? 1 : 0);
        ItemStack itemStack = vinery$fox().getItemBySlot(EquipmentSlot.MAINHAND);
        ItemStack grape = type.getFruit().getDefaultInstance();
        if (itemStack.isEmpty()) {
            vinery$fox().setItemSlot(EquipmentSlot.MAINHAND, grape);
            --j;
        }
        if (j > 0) {
            Block.popResource(vinery$fox().level, this.blockPos, new ItemStack(grape.getItem(), j));
        }
        vinery$fox().playSound(SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, 1.0F, 1.0F);
        vinery$fox().level.setBlock(this.blockPos, state.setValue(GrapeBushBlock.AGE, 1), 2);
    }
}
