package com.SaintAlfonzo.creativedelight.hearth.machine;

import com.SaintAlfonzo.creativedelight.hearth.HHConfig;
import com.SaintAlfonzo.creativedelight.hearth.event.ManureDrops;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemStackHandler;

/**
 * Water Trough: a 1000 mB plain-water tank or one food stack, never both. Plain water only (N22): the fluid must be
 * {@code minecraft:water} itself, with no tag. Water speeds up nearby babies; food puts nearby adults in love.
 */
public class TroughBlockEntity extends SyncedBlockEntity {
    public static final int CAPACITY = 1000;
    private static final int SLOT_LIMIT = 64;
    private static final int GROWTH_PER_INTERVAL = 16;
    private static final int WATER_PER_BOOST = 10;
    private static final int RAIN_PER_SECOND = 4;
    private final FluidTank tank = new FluidTank(CAPACITY) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid() == Fluids.WATER && !stack.hasTag() && items.getStackInSlot(0).isEmpty();
        }

        @Override
        protected void onContentsChanged() {
            sync();
            if (level != null && !level.isClientSide) level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    };
    final ItemStackHandler items = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            sync();
        }

        @Override
        public int getSlotLimit(int slot) {
            return SLOT_LIMIT;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return tank.isEmpty() && level != null && TroughFoods.isFood(level, stack);
        }
    };
    private final LazyOptional<IFluidHandler> capability = LazyOptional.of(() -> tank);

    public TroughBlockEntity(BlockPos pos, BlockState state) {
        super(HHMachines.TROUGH_BE.get(), pos, state);
    }

    public FluidTank tank() {
        return tank;
    }

    public ItemStack food() {
        return items.getStackInSlot(0);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TroughBlockEntity be) {
        long time = level.getGameTime();
        if (time % 20L == 0L && be.items.getStackInSlot(0).isEmpty() && level.isRainingAt(pos.above())) {
            be.tank.fill(new FluidStack(Fluids.WATER, RAIN_PER_SECOND), IFluidHandler.FluidAction.EXECUTE);
        }
        if (time % 80L != 0L) return;
        AABB area = AABB.ofSize(Vec3.atCenterOf(pos), 10.0, 4.0, 10.0);
        if (!be.tank.isEmpty()) {
            for (AgeableMob baby : level.getEntitiesOfClass(AgeableMob.class, area, AgeableMob::isBaby)) {
                if (be.tank.getFluidAmount() < WATER_PER_BOOST) break;
                baby.setAge(Math.min(0, baby.getAge() + GROWTH_PER_INTERVAL));
                be.tank.drain(WATER_PER_BOOST, IFluidHandler.FluidAction.EXECUTE);
            }
        }
        ItemStack food = be.items.getStackInSlot(0);
        if (food.isEmpty()) return;
        int cap = HHConfig.TROUGH_ANIMAL_CAP.get();
        int nearby = level.getEntitiesOfClass(Animal.class, AABB.ofSize(Vec3.atCenterOf(pos), 20.0, 8.0, 20.0)).size();
        if (nearby >= cap) return;
        for (Animal animal : level.getEntitiesOfClass(Animal.class, area, a -> a.getAge() == 0 && a.canFallInLove() && a.isFood(be.items.getStackInSlot(0)))) {
            if (nearby >= cap || be.items.getStackInSlot(0).isEmpty()) break;
            animal.setInLove(null);
            ManureDrops.schedule(animal);
            be.items.extractItem(0, 1, false);
            level.playSound(null, pos, SoundEvents.GENERIC_EAT, SoundSource.BLOCKS, 0.5F, 0.8F + level.random.nextFloat() * 0.4F);
            nearby++;
        }
    }

    public ItemStack insert(ItemStack stack) {
        return !stack.isEmpty() && tank.isEmpty() ? items.insertItem(0, stack, false) : stack;
    }

    public void extract(Player player, boolean all) {
        ItemStack inSlot = items.getStackInSlot(0);
        if (inSlot.isEmpty()) return;
        ItemStack taken = items.extractItem(0, all ? inSlot.getCount() : 1, false);
        if (!player.addItem(taken)) {
            ItemEntity entity = new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.3, worldPosition.getZ() + 0.5, taken, 0.0, 0.1, 0.0);
            entity.setPickUpDelay(10);
            level.addFreshEntity(entity);
        }
    }

    public void dropContents() {
        if (level != null && !items.getStackInSlot(0).isEmpty()) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), items.getStackInSlot(0));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Tank", tank.writeToNBT(new CompoundTag()));
        tag.put("Items", items.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Tank")) tank.readFromNBT(tag.getCompound("Tank"));
        if (tag.contains("Items")) items.deserializeNBT(tag.getCompound("Items"));
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
