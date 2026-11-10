package com.SaintAlfonzo.creativedelight.hearth.machine;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

/** Fluid Jug: one tank of 8000 mB, any single fluid. */
public class JugBlockEntity extends SyncedBlockEntity {
    public static final int CAPACITY = 8000;
    private final FluidTank tank = new FluidTank(CAPACITY) {
        @Override
        protected void onContentsChanged() {
            sync();
            if (level != null && !level.isClientSide) level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    };
    private final LazyOptional<IFluidHandler> capability = LazyOptional.of(() -> tank);

    public JugBlockEntity(BlockPos pos, BlockState state) {
        super(HHMachines.JUG_BE.get(), pos, state);
    }

    public FluidTank tank() {
        return tank;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("FluidTank", tank.writeToNBT(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("FluidTank")) tank.readFromNBT(tag.getCompound("FluidTank"));
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
