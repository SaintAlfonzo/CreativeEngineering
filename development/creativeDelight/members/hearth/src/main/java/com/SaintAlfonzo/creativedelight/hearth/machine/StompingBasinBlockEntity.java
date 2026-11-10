package com.SaintAlfonzo.creativedelight.hearth.machine;

import com.SaintAlfonzo.creativedelight.hearth.HHConfig;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

/**
 * Stomping Basin: alone 1 slot and 8000 mB, as a 2x2 group 4 slots and 32 000 mB held by the north-west corner
 * (the controller; the other three are members that defer to it).
 */
public class StompingBasinBlockEntity extends SyncedBlockEntity {
    public static final int SOLO_TANK = 8000;
    public static final int GROUP_TANK = 32000;
    public static final int COOLDOWN_TICKS = 5;
    private MultiblockPart role = MultiblockPart.NONE;
    @Nullable
    private BlockPos controllerPos;
    private final Map<UUID, Long> stompCooldowns = new HashMap<>();
    private final Items items = new Items();
    private final Tank tank = new Tank();
    private LazyOptional<IItemHandler> itemCap = LazyOptional.of(this::capItems);
    private LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(this::capFluid);

    public StompingBasinBlockEntity(BlockPos pos, BlockState state) {
        super(HHMachines.STOMPING_BASIN_BE.get(), pos, state);
    }

    private IItemHandler capItems() {
        StompingBasinBlockEntity controller = controller();
        return controller != null ? controller.items : items;
    }

    private IFluidHandler capFluid() {
        if (role == MultiblockPart.MEMBER) {
            StompingBasinBlockEntity controller = controller();
            return controller != null ? controller.tank : new FluidTank(0);
        }
        return tank;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return itemCap.cast();
        if (cap == ForgeCapabilities.FLUID_HANDLER) return fluidCap.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemCap.invalidate();
        fluidCap.invalidate();
    }

    /** The capabilities resolve to the controller's inventory, so they are rebuilt when the role changes. */
    private void resetCaps() {
        itemCap.invalidate();
        fluidCap.invalidate();
        itemCap = LazyOptional.of(this::capItems);
        fluidCap = LazyOptional.of(this::capFluid);
    }

    public ItemStackHandler items() {
        return items;
    }

    public FluidTank tank() {
        if (role == MultiblockPart.MEMBER) {
            StompingBasinBlockEntity controller = controller();
            if (controller != null) return controller.tank;
        }
        return tank;
    }

    public MultiblockPart role() {
        return role;
    }

    @Nullable
    public BlockPos controllerPos() {
        return controllerPos;
    }

    @Nullable
    public StompingBasinBlockEntity controller() {
        return controllerPos != null && level != null && level.getBlockEntity(controllerPos) instanceof StompingBasinBlockEntity basin ? basin : null;
    }

    public int activeSlots() {
        return role == MultiblockPart.CONTROLLER ? 4 : 1;
    }

    public ItemStack insert(ItemStack toInsert) {
        if (toInsert.isEmpty()) return ItemStack.EMPTY;
        if (role == MultiblockPart.MEMBER) {
            StompingBasinBlockEntity controller = controller();
            return controller != null ? controller.insert(toInsert) : toInsert;
        }
        for (int i = 0; i < activeSlots() && !toInsert.isEmpty(); i++) {
            ItemStack inSlot = items.getStackInSlot(i);
            if (!inSlot.isEmpty() && ItemStack.isSameItemSameTags(inSlot, toInsert)) toInsert = items.insertItem(i, toInsert, false);
        }
        for (int i = 0; i < activeSlots() && !toInsert.isEmpty(); i++) {
            if (items.getStackInSlot(i).isEmpty()) toInsert = items.insertItem(i, toInsert, false);
        }
        return toInsert;
    }

    public void extractOne(Player player) {
        if (role == MultiblockPart.MEMBER) {
            StompingBasinBlockEntity controller = controller();
            if (controller != null) controller.extractOne(player);
            return;
        }
        for (int i = items.getSlots() - 1; i >= 0; i--) {
            if (!items.getStackInSlot(i).isEmpty()) {
                giveOrDrop(player, items.extractItem(i, 1, false));
                return;
            }
        }
    }

    public void extractAll(Player player) {
        if (role == MultiblockPart.MEMBER) {
            StompingBasinBlockEntity controller = controller();
            if (controller != null) controller.extractAll(player);
            return;
        }
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack inSlot = items.getStackInSlot(i);
            if (!inSlot.isEmpty()) giveOrDrop(player, items.extractItem(i, inSlot.getCount(), false));
        }
    }

    private void giveOrDrop(Player player, ItemStack stack) {
        if (!stack.isEmpty() && !player.addItem(stack)) dropAtBasin(stack);
    }

    /** Called when something lands in the basin. */
    public void tryProcess(LivingEntity entity) {
        if (level == null || level.isClientSide) return;
        if (role == MultiblockPart.MEMBER) {
            StompingBasinBlockEntity controller = controller();
            if (controller != null) controller.tryProcess(entity);
            return;
        }
        long now = level.getGameTime();
        stompCooldowns.values().removeIf(last -> now - last >= COOLDOWN_TICKS);
        if (stompCooldowns.containsKey(entity.getUUID())) return;
        stompCooldowns.put(entity.getUUID(), now);
        RecipeWrapper wrapper = new RecipeWrapper(items);
        if (wrapper.isEmpty()) return;
        StompingRecipe recipe = null;
        int[] assignment = null;
        List<StompingRecipe> recipes = level.getRecipeManager().getAllRecipesFor(HHMachines.STOMPING_TYPE.get()).stream()
                .sorted(Comparator.<StompingRecipe>comparingInt(r -> r.getIngredients().size()).reversed()).toList();
        for (StompingRecipe candidate : recipes) {
            int[] found = candidate.findAssignment(wrapper);
            if (found != null) {
                recipe = candidate;
                assignment = found;
                break;
            }
        }
        if (recipe == null) return;
        FluidStack fluid = recipe.resultFluid();
        if (!fluid.isEmpty() && tank.fill(fluid.copy(), IFluidHandler.FluidAction.SIMULATE) < fluid.getAmount()) return;
        boolean group = role == MultiblockPart.CONTROLLER;
        level.playSound(null, worldPosition.getX() + (group ? 1.0 : 0.5), worldPosition.getY() + 0.5, worldPosition.getZ() + (group ? 1.0 : 0.5),
                SoundEvents.SLIME_SQUISH, SoundSource.BLOCKS, 0.6F, 0.7F);
        for (int slot : assignment) items.extractItem(slot, 1, false);
        if (!fluid.isEmpty()) tank.fill(fluid.copy(), IFluidHandler.FluidAction.EXECUTE);
        if (!recipe.getResultItem().isEmpty()) dropAtBasin(recipe.getResultItem().copy());
        sync();
    }

    private void dropAtBasin(ItemStack stack) {
        if (level == null || stack.isEmpty()) return;
        ItemEntity entity = new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.25, worldPosition.getZ() + 0.5, stack, 0.0, 0.0, 0.0);
        entity.setPickUpDelay(10);
        level.addFreshEntity(entity);
    }

    public void formAsController(StompingBasinBlockEntity ne, StompingBasinBlockEntity sw, StompingBasinBlockEntity se) {
        role = MultiblockPart.CONTROLLER;
        resetCaps();
        tank.setEffectiveCapacity(GROUP_TANK);
        for (StompingBasinBlockEntity member : new StompingBasinBlockEntity[]{ne, sw, se}) {
            for (int s = 0; s < member.items.getSlots(); s++) {
                ItemStack stack = member.items.getStackInSlot(s);
                if (stack.isEmpty()) continue;
                ItemStack rest = insert(stack.copy());
                if (!rest.isEmpty()) dropAtBasin(rest);
                member.items.setStackInSlot(s, ItemStack.EMPTY);
            }
            FluidStack memberFluid = member.tank.getFluid().copy();
            if (!memberFluid.isEmpty()) {
                int accepted = tank.fill(memberFluid.copy(), IFluidHandler.FluidAction.EXECUTE);
                memberFluid.shrink(accepted);
                member.tank.setFluid(memberFluid);
            }
            member.sync();
        }
        sync();
    }

    public void formAsMember(BlockPos controller) {
        role = MultiblockPart.MEMBER;
        controllerPos = controller;
        resetCaps();
        sync();
    }

    /** Controller's share of a break-up: extra slots and any fluid past a single basin go out or to the survivors. */
    public void dissolve(List<StompingBasinBlockEntity> survivors) {
        role = MultiblockPart.NONE;
        controllerPos = null;
        resetCaps();
        for (int s = 0; s < items.getSlots(); s++) {
            ItemStack stack = items.getStackInSlot(s);
            if (stack.isEmpty()) continue;
            if (s >= 1) {
                items.setStackInSlot(s, ItemStack.EMPTY);
                dropAtBasin(stack);
            } else if (stack.getCount() > slotLimit()) {
                int limit = slotLimit();
                dropAtBasin(ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - limit));
                items.setStackInSlot(s, ItemHandlerHelper.copyStackWithSize(stack, limit));
            }
        }
        tank.setEffectiveCapacity(SOLO_TANK);
        int overflow = tank.getFluidAmount() - SOLO_TANK;
        if (overflow > 0) {
            FluidStack held = tank.getFluid().copy();
            FluidStack moving = held.copy();
            moving.setAmount(overflow);
            held.setAmount(SOLO_TANK);
            tank.setFluid(held);
            for (StompingBasinBlockEntity survivor : survivors) {
                if (moving.isEmpty()) break;
                if (survivor == this) continue;
                survivor.tank.setEffectiveCapacity(SOLO_TANK);
                int accepted = survivor.tank.fill(moving.copy(), IFluidHandler.FluidAction.EXECUTE);
                moving.shrink(accepted);
                if (accepted > 0) survivor.sync();
            }
        }
        sync();
    }

    public void dissolveAsMember() {
        role = MultiblockPart.NONE;
        controllerPos = null;
        resetCaps();
        sync();
    }

    public void dropContents() {
        if (level == null) return;
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (!stack.isEmpty()) Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
        }
    }

    private static int slotLimit() {
        return HHConfig.STOMPING_BASIN_SLOT_LIMIT.get();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
        tag.put("Tank", tank.writeToNBT(new CompoundTag()));
        tag.putString("MultiblockRole", role.getSerializedName());
        tag.putInt("FluidCapacity", tank.getCapacity());
        if (controllerPos != null) tag.put("MasterPos", NbtUtils.writeBlockPos(controllerPos));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) items.deserializeNBT(tag.getCompound("Items"));
        if (tag.contains("FluidCapacity")) tank.setEffectiveCapacity(tag.getInt("FluidCapacity"));
        if (tag.contains("Tank")) tank.readFromNBT(tag.getCompound("Tank"));
        if (tag.contains("MultiblockRole")) role = MultiblockPart.byName(tag.getString("MultiblockRole"));
        controllerPos = tag.contains("MasterPos") ? NbtUtils.readBlockPos(tag.getCompound("MasterPos")) : null;
    }

    private final class Tank extends FluidTank {
        Tank() {
            super(SOLO_TANK);
        }

        @Override
        protected void onContentsChanged() {
            sync();
            if (level != null && !level.isClientSide) level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }

        void setEffectiveCapacity(int capacity) {
            this.capacity = capacity;
        }
    }

    /** 1.4.1's slot handler: a configurable slot limit past the usual 64, so counts are saved separately. */
    private final class Items extends ItemStackHandler {
        Items() {
            super(4);
        }

        @Override
        protected void onContentsChanged(int slot) {
            sync();
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot < activeSlots() ? slotLimit() : 0;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot < activeSlots();
        }

        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return getSlotLimit(slot);
        }

        @Override
        public CompoundTag serializeNBT() {
            ListTag list = new ListTag();
            for (int i = 0; i < getSlots(); i++) {
                ItemStack stack = getStackInSlot(i);
                if (stack.isEmpty()) continue;
                CompoundTag entry = new CompoundTag();
                entry.putByte("Slot", (byte) i);
                entry.put("Stack", ItemHandlerHelper.copyStackWithSize(stack, 1).save(new CompoundTag()));
                entry.putInt("ExtraCount", stack.getCount());
                list.add(entry);
            }
            CompoundTag tag = new CompoundTag();
            tag.put("Items", list);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            for (int i = 0; i < getSlots(); i++) stacks.set(i, ItemStack.EMPTY);
            ListTag list = tag.getList("Items", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                int slot = entry.getByte("Slot") & 255;
                if (slot >= getSlots()) continue;
                ItemStack stack = ItemStack.of(entry.getCompound("Stack"));
                if (stack.isEmpty()) continue;
                stack.setCount(entry.getInt("ExtraCount"));
                stacks.set(slot, stack);
            }
        }
    }
}
