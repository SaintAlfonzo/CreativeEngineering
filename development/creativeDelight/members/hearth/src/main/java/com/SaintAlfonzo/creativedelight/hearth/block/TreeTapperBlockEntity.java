package com.SaintAlfonzo.creativedelight.hearth.block;

import com.SaintAlfonzo.creativedelight.hearth.HHConfig;
import com.SaintAlfonzo.creativedelight.hearth.HHTags;
import com.SaintAlfonzo.creativedelight.hearth.fluid.HHFluids;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

/** 1000 mB of sap: every 2 s, 50 % for 10 mB (about 6.7 minutes a bucket). Pipes and buckets drain it. */
public class TreeTapperBlockEntity extends BlockEntity {
    private static final int CAPACITY = 1000;
    private int ticks;
    private final FluidTank tank = new FluidTank(CAPACITY, stack -> stack.getFluid() == HHFluids.SAP.source.get()) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null && !level.isClientSide) level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    };
    private final LazyOptional<IFluidHandler> capability = LazyOptional.of(() -> tank);

    public TreeTapperBlockEntity(BlockPos pos, BlockState state) {
        super(HHBlockEntities.TREE_TAPPER.get(), pos, state);
    }

    int comparator() {
        return tank.isEmpty() ? 0 : 1 + tank.getFluidAmount() * 14 / CAPACITY;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TreeTapperBlockEntity tapper) {
        if (++tapper.ticks < 40) return;
        tapper.ticks = 0;
        if (tapper.tank.getFluidAmount() >= CAPACITY) return;
        Direction dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (level.getBlockState(pos.relative(dir)).is(HHTags.TAPPABLE) && level.random.nextDouble() <= HHConfig.TAPPER_CHANCE.get()) {
            tapper.tank.fill(new FluidStack(HHFluids.SAP.source.get(), 10), IFluidHandler.FluidAction.EXECUTE);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Tank", tank.writeToNBT(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        tank.readFromNBT(tag.getCompound("Tank"));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        return cap == ForgeCapabilities.FLUID_HANDLER ? capability.cast() : super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        capability.invalidate();
    }
}
