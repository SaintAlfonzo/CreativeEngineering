package com.SaintAlfonzo.creativedelight.hearth.machine;

import com.SaintAlfonzo.creativedelight.hearth.HHConfig;
import com.SaintAlfonzo.creativedelight.hearth.block.BerryBush;
import com.SaintAlfonzo.creativedelight.hearth.item.WateringCanItem;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

/**
 * Field Sprinkler: 10 000 mB of water, soaks farmland to moisture 7 within 7 blocks, puts out fires, spends one
 * fertiliser on a crop every 5 s, stops on a redstone signal. Fills by itself in rain (20 mB/s) and beside a water
 * source (100 mB / 2 s, so effectively infinite).
 * <p>
 * Scan cost (row note: upstream scans 675 + 1125 blocks every 2 s per sprinkler): one pass over the 15x5x15 volume runs
 * every {@code sprinklerRescanTicks} (5 s by default, staggered per block) and caches the farmland and crop positions;
 * the 2 s watering and the 5 s fertilising use the cached positions only. Fire is also handled in that pass.
 */
public class SprinklerBlockEntity extends SyncedBlockEntity {
    public static final int CAPACITY = 10000;
    private static final int RADIUS = 7;
    private static final int HYDRATE_INTERVAL = 40;
    private static final int FERTILIZE_INTERVAL = 100;
    private static final int WATER_PER_CYCLE = 10;
    private static final int WATER_PER_FIRE = 100;
    private final FluidTank tank = new FluidTank(CAPACITY) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid().isSame(Fluids.WATER);
        }

        @Override
        protected void onContentsChanged() {
            sync();
            if (level != null && !level.isClientSide) level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    };
    private final ItemStackHandler fertilizer = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(WateringCanItem.BONEMEAL_SUBSTITUTES);
        }
    };
    private final LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(() -> tank);
    private final LazyOptional<IItemHandler> itemCap = LazyOptional.of(() -> fertilizer);
    private final List<BlockPos> soil = new ArrayList<>();
    private final List<BlockPos> crops = new ArrayList<>();
    private int hydrateTimer;
    private int fertilizeTimer;
    private int refillTimer;
    private boolean waterCheckDue;
    private int rescanTimer = -1;

    public SprinklerBlockEntity(BlockPos pos, BlockState state) {
        super(HHMachines.SPRINKLER_BE.get(), pos, state);
    }

    public FluidTank tank() {
        return tank;
    }

    public ItemStack fertilizer() {
        return fertilizer.getStackInSlot(0);
    }

    public int addFertilizer(ItemStack stack, boolean creative) {
        if (!stack.is(WateringCanItem.BONEMEAL_SUBSTITUTES)) return 0;
        ItemStack stored = fertilizer.getStackInSlot(0);
        if (!stored.isEmpty() && !stored.is(stack.getItem())) return 0;
        int space = stack.getMaxStackSize() - stored.getCount();
        if (space <= 0) return 0;
        int moved = Math.min(space, stored.isEmpty() ? 1 : stack.getCount());
        fertilizer.insertItem(0, net.minecraftforge.items.ItemHandlerHelper.copyStackWithSize(stack, moved), false);
        if (!creative) stack.shrink(moved);
        return moved;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SprinklerBlockEntity be) {
        be.refill(level, pos);
        if (be.rescanTimer < 0) be.rescanTimer = (int) (pos.asLong() & 0x7FL);
        if (level.hasNeighborSignal(pos)) {
            be.hydrateTimer = 0;
            be.fertilizeTimer = 0;
            return;
        }
        ServerLevel server = (ServerLevel) level;
        if (--be.rescanTimer <= 0) {
            be.rescanTimer = HHConfig.SPRINKLER_RESCAN_TICKS.get();
            be.rescan(server, pos);
        }
        if (++be.hydrateTimer >= HYDRATE_INTERVAL) {
            be.hydrateTimer = 0;
            be.hydrate(server);
        }
        if (++be.fertilizeTimer >= FERTILIZE_INTERVAL) {
            be.fertilizeTimer = 0;
            if (!be.fertilizer.getStackInSlot(0).isEmpty()) be.fertilize(server);
        }
    }

    private void refill(Level level, BlockPos pos) {
        if (tank.getSpace() <= 0 || ++refillTimer < 20) return;
        refillTimer = 0;
        if (level.isRainingAt(pos.above())) {
            tank.fill(new FluidStack(Fluids.WATER, 20), IFluidHandler.FluidAction.EXECUTE);
            return;
        }
        // 1.4.1 tested game time % 40 inside this 20-tick timer, so only blocks placed on a tick divisible by 20 ever
        // drew from water. Every second timer pass instead.
        waterCheckDue = !waterCheckDue;
        if (!waterCheckDue) return;
        for (Direction direction : Direction.values()) {
            if (direction == Direction.UP) continue;
            BlockState neighbour = level.getBlockState(pos.relative(direction));
            if (neighbour.getFluidState().isSource() && neighbour.getFluidState().is(Fluids.WATER)) {
                tank.fill(new FluidStack(Fluids.WATER, 100), IFluidHandler.FluidAction.EXECUTE);
                return;
            }
        }
    }

    private void rescan(ServerLevel level, BlockPos center) {
        soil.clear();
        crops.clear();
        if (tank.getFluidAmount() < WATER_PER_CYCLE && fertilizer.getStackInSlot(0).isEmpty()) return;
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int y = -1; y <= 3; y++) {
            for (int x = -RADIUS; x <= RADIUS; x++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    p.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    BlockState s = level.getBlockState(p);
                    if (s.isAir()) continue;
                    if (y <= 1 && s.hasProperty(BlockStateProperties.MOISTURE)) soil.add(p.immutable());
                    if (y >= 0 && y <= 2 && isCrop(s)) crops.add(p.immutable());
                    if (s.getBlock() instanceof BaseFireBlock && tank.getFluidAmount() >= WATER_PER_FIRE) {
                        level.removeBlock(p, false);
                        level.playSound(null, p, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
                        tank.drain(WATER_PER_FIRE, IFluidHandler.FluidAction.EXECUTE);
                    }
                }
            }
        }
    }

    private void hydrate(ServerLevel level) {
        if (tank.getFluidAmount() < WATER_PER_CYCLE) return;
        List<BlockPos> splash = new ArrayList<>();
        boolean any = false;
        for (BlockPos pos : soil) {
            BlockState s = level.getBlockState(pos);
            if (s.hasProperty(BlockStateProperties.MOISTURE) && s.getValue(BlockStateProperties.MOISTURE) < 7) {
                level.setBlock(pos, s.setValue(BlockStateProperties.MOISTURE, 7), 2);
                any = true;
                if (splash.size() < 6) splash.add(pos);
            }
        }
        if (!any) return;
        tank.drain(WATER_PER_CYCLE, IFluidHandler.FluidAction.EXECUTE);
        for (BlockPos pos : splash) {
            level.sendParticles(ParticleTypes.SPLASH, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 15, 0.4, 0.05, 0.4, 0.1);
        }
    }

    /** Same application as the watering can (WateringCanItem.applyBonemeal): tree growers are left alone, and only a real growth spends the item. */
    private void fertilize(ServerLevel level) {
        if (crops.isEmpty()) return;
        for (int attempt = 0; attempt < 3; attempt++) {
            BlockPos pos = crops.get(level.random.nextInt(crops.size()));
            BlockState s = level.getBlockState(pos);
            if (!isCrop(s) || WateringCanItem.isTreeGrower(s) || !(s.getBlock() instanceof BonemealableBlock growable)) continue;
            if (!growable.isValidBonemealTarget(level, pos, s, false) || !growable.isBonemealSuccess(level, level.random, pos, s)) continue;
            growable.performBonemeal(level, level.random, pos, s);
            level.levelEvent(2005, pos, 0);
            fertilizer.extractItem(0, 1, false);
            level.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 8, 0.4, 0.4, 0.4, 0.0);
            return;
        }
    }

    private static boolean isCrop(BlockState state) {
        return state.is(BlockTags.CROPS) || state.getBlock() instanceof CropBlock || state.getBlock() instanceof BerryBush;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Tank", tank.writeToNBT(new CompoundTag()));
        tag.put("Fertilizer", fertilizer.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Tank")) tank.readFromNBT(tag.getCompound("Tank"));
        if (tag.contains("Fertilizer")) fertilizer.deserializeNBT(tag.getCompound("Fertilizer"));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) return fluidCap.cast();
        if (cap == ForgeCapabilities.ITEM_HANDLER) return itemCap.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidCap.invalidate();
        itemCap.invalidate();
    }
}
