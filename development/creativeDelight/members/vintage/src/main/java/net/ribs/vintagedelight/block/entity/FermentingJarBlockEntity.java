package net.ribs.vintagedelight.block.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.ribs.vintagedelight.config.VintageDelightCommonConfig;
import net.ribs.vintagedelight.item.ModTags;
import net.ribs.vintagedelight.recipe.FermentingRecipe;
import net.ribs.vintagedelight.screen.FermentingJarMenu;
import net.ribs.vintagedelight.shared.CdSettings;
import net.ribs.vintagedelight.shared.LeftoverPlan;
import net.ribs.vintagedelight.shared.OverflowPull;
import net.ribs.vintagedelight.shared.StationContainers;

/**
 * The Pickling Jar (upstream's fermenting jar; the id is kept). Slots 0-5 ingredients, 6 container, 7-8 outputs, 9
 * leftovers (port: the empty bottles and bowls of the ingredients, take-only, as the keg's; config remainderBuffer).
 * Containers go only into slot 6 and nothing else does; leftovers follow the stations' shared rule (LeftoverPlan,
 * feature_writeup/remainder_buffer.md 3.8).
 * Upstream 0.1.6's crafting loop with KOTS 0.3.1's slot rules and sided handler (FermentingJarItemHandler).
 */
public class FermentingJarBlockEntity extends BlockEntity implements MenuProvider {
    public static final int FIRST_INGREDIENT_SLOT = 0;
    public static final int LAST_INPUT_SLOT = 5;
    public static final int CONTAINER_SLOT = 6;
    public static final int FIRST_OUTPUT_SLOT = 7;
    public static final int SECOND_OUTPUT_SLOT = 8;
    public static final int REMAINDER_SLOT = 9;
    public static final int SLOT_COUNT = 10;

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            if (slot == FIRST_OUTPUT_SLOT || slot == SECOND_OUTPUT_SLOT || slot == REMAINDER_SLOT) {
                return false;
            }
            // containers only in the container slot, never in an ingredient slot (remainder_buffer.md 3.8)
            return slot == CONTAINER_SLOT ? isJarContainer(stack) : !JarContainers.JAR.isContainer(level, stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            FermentingJarBlockEntity.this.setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };
    private final FermentingJarItemHandler sidedHandler = new FermentingJarItemHandler(this.itemHandler);
    private final LazyOptional<IItemHandler> allSides = LazyOptional.of(() -> this.sidedHandler.forSide(null));
    private final LazyOptional<IItemHandler> up = LazyOptional.of(() -> this.sidedHandler.forSide(Direction.UP));
    private final LazyOptional<IItemHandler> down = LazyOptional.of(() -> this.sidedHandler.forSide(Direction.DOWN));
    private final LazyOptional<IItemHandler> sides = LazyOptional.of(() -> this.sidedHandler.forSide(Direction.NORTH));
    protected final ContainerData data;
    private int progress = 0;
    private int maxProgress = 78;
    /** The recipe the progress belongs to; a different recipe starts from 0 (not saved: a reload keeps the progress). */
    @Nullable
    private ResourceLocation progressRecipe;
    private final SimpleContainer inputInventory = new SimpleContainer(7);
    /** Leftovers waiting to be thrown out, with the ticks left (config overflowEjectDelayTicks; members #37). */
    private final List<ItemStack> pendingEject = new ArrayList<>();
    private final List<Integer> pendingEjectTicks = new ArrayList<>();
    /** Leftover slot to container slot moves for the next batch (not saved). */
    private final OverflowPull overflowPull = new OverflowPull();

    /** The jar's containers: every pickling recipe's container, and #vintagedelight:container_items. */
    private static final class JarContainers {
        static final StationContainers JAR = new StationContainers("vintagedelight:pickling_jar", FermentingRecipe.Type.INSTANCE,
                r -> r instanceof FermentingRecipe f ? List.of(f.getContainerIngredient().getItems()) : List.of(),
                List.of(ModTags.CONTAINER_ITEMS));
    }

    /** Whether {@code stack} goes in the container slot (and only there). */
    public boolean isJarContainer(ItemStack stack) {
        return stack.is(ModTags.CONTAINER_ITEMS) || JarContainers.JAR.isContainer(this.level, stack);
    }

    public FermentingJarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FERMENTING_JAR_BE.get(), pos, state);
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> progress;
                    case 1 -> maxProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> progress = value;
                    case 1 -> maxProgress = value;
                    default -> {
                    }
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }

    /** The full inventory, for the screen. */
    public IItemHandler getInventory() {
        return this.itemHandler;
    }

    public List<ItemStack> getRenderStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = FIRST_INGREDIENT_SLOT; i <= LAST_INPUT_SLOT; i++) {
            ItemStack stack = this.itemHandler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        if (!this.itemHandler.getStackInSlot(FIRST_OUTPUT_SLOT).isEmpty()) {
            stacks.add(this.itemHandler.getStackInSlot(FIRST_OUTPUT_SLOT));
        }
        return stacks;
    }

    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null) {
                return this.allSides.cast();
            }
            return (side == Direction.UP ? this.up : side == Direction.DOWN ? this.down : this.sides).cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.allSides.invalidate();
        this.up.invalidate();
        this.down.invalidate();
        this.sides.invalidate();
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(this.itemHandler.getSlots());
        for (int i = 0; i < this.itemHandler.getSlots(); i++) {
            inventory.setItem(i, this.itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
        for (ItemStack waiting : this.pendingEject) {
            Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), waiting);
        }
        this.pendingEject.clear();
        this.pendingEjectTicks.clear();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.vintagedelight.fermenting_jar");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new FermentingJarMenu(containerId, playerInventory, this, this.data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", this.itemHandler.serializeNBT());
        tag.putInt("fermenting_jar.progress", this.progress);
        if (!this.pendingEject.isEmpty()) {
            ListTag list = new ListTag();
            for (int i = 0; i < this.pendingEject.size(); i++) {
                CompoundTag entry = this.pendingEject.get(i).save(new CompoundTag());
                entry.putInt("ticks", this.pendingEjectTicks.get(i));
                list.add(entry);
            }
            tag.put("fermenting_jar.pending_eject", list);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        CompoundTag inventory = tag.getCompound("inventory");
        // jars saved before the leftover slot hold 9 slots; deserializeNBT would shrink the handler to that
        if (inventory.getInt("Size") < SLOT_COUNT) {
            inventory.putInt("Size", SLOT_COUNT);
        }
        this.itemHandler.deserializeNBT(inventory);
        this.progress = tag.getInt("fermenting_jar.progress");
        this.pendingEject.clear();
        this.pendingEjectTicks.clear();
        for (Tag t : tag.getList("fermenting_jar.pending_eject", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) t;
            ItemStack stack = ItemStack.of(entry);
            if (!stack.isEmpty()) {
                this.pendingEject.add(stack);
                this.pendingEjectTicks.add(entry.getInt("ticks"));
            }
        }
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) {
            return;
        }
        this.tickPendingEject();
        this.refreshInputView();
        Optional<FermentingRecipe> recipeOpt = level.getRecipeManager().getRecipeFor(FermentingRecipe.Type.INSTANCE, this.inputInventory, level);
        this.pullForNextBatch(level, recipeOpt.isPresent());
        if (recipeOpt.isPresent()) {
            FermentingRecipe recipe = recipeOpt.get();
            ItemStack result = recipe.getResultItem();
            ItemStack secondaryResult = recipe.getSecondaryResultItem();
            this.maxProgress = recipe.getProcessingTime();
            // Port fix: progress belongs to one recipe. Upstream kept it when the inputs turned into another recipe, so
            // the new batch finished at once (scratchpad 2026-11-02: kimchi finished instantly).
            if (!recipe.getId().equals(this.progressRecipe)) {
                this.progress = 0;
                this.progressRecipe = recipe.getId();
            }
            int[] slots = recipe.assignSlots(this.inputInventory);
            if (slots != null && this.canOutput(result, secondaryResult) && this.remaindersFit(recipe, slots)) {
                this.progress++;
                if (this.progress >= this.maxProgress) {
                    this.craftItem(recipe, slots);
                    this.progress = 0;
                }
            } else {
                this.progress = 0;
            }
        } else {
            this.progress = 0;
        }
    }

    /**
     * The jar's form of the stations' overflow pull (remainder_buffer.md 3.8, shared {@link OverflowPull}): with the
     * container slot empty and no recipe matching, a leftover in slot 9 that is the container the current ingredients'
     * recipe needs moves into slot 6 after CD {@code [cooking] recycleShowTicks}, one at a time, so the next batch runs.
     */
    private void pullForNextBatch(Level level, boolean recipeMatched) {
        int needed = 0;
        Predicate<ItemStack> fitsNext = stack -> false;
        ItemStack over = this.itemHandler.getStackInSlot(REMAINDER_SLOT);
        if (!recipeMatched && VintageDelightCommonConfig.remainderBuffer() && !over.isEmpty()
                && this.itemHandler.getStackInSlot(CONTAINER_SLOT).isEmpty()) {
            this.inputInventory.setItem(CONTAINER_SLOT, ItemHandlerHelper.copyStackWithSize(over, 1));
            Optional<FermentingRecipe> next = level.getRecipeManager().getRecipeFor(FermentingRecipe.Type.INSTANCE, this.inputInventory, level);
            this.inputInventory.setItem(CONTAINER_SLOT, ItemStack.EMPTY);
            if (next.isPresent() && next.get().hasContainer()) {
                FermentingRecipe recipe = next.get();
                needed = 1;
                fitsNext = stack -> recipe.getContainerIngredient().test(stack);
            }
        }
        this.overflowPull.tick(new OverflowPull.Slots() {
            @Override
            public ItemStack get(int slot) {
                return itemHandler.getStackInSlot(slot);
            }

            @Override
            public void set(int slot, ItemStack stack) {
                itemHandler.setStackInSlot(slot, stack);
            }

            @Override
            public int limit(int slot) {
                return itemHandler.getSlotLimit(slot);
            }
        }, CONTAINER_SLOT, new int[]{REMAINDER_SLOT}, needed, fitsNext, Math.max(0, CdSettings.number("cooking.recycleShowTicks", 10)));
    }

    private boolean canOutput(ItemStack result, ItemStack secondaryResult) {
        boolean primary = result.isEmpty() || this.fits(result, FIRST_OUTPUT_SLOT);
        boolean secondary = secondaryResult.isEmpty() || this.fits(secondaryResult, SECOND_OUTPUT_SLOT);
        return primary && secondary;
    }

    private boolean fits(ItemStack result, int slot) {
        ItemStack existing = this.itemHandler.getStackInSlot(slot);
        if (existing.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameTags(existing, result) && existing.getCount() + result.getCount() <= existing.getMaxStackSize();
    }

    private void refreshInputView() {
        for (int i = FIRST_INGREDIENT_SLOT; i <= CONTAINER_SLOT; i++) {
            this.inputInventory.setItem(i, this.itemHandler.getStackInSlot(i));
        }
    }

    /** Takes one item from each slot of the recipe's slot assignment (one per ingredient entry); one leftover each. */
    private void craftItem(FermentingRecipe recipe, int[] slots) {
        ItemStack result = recipe.getResultItem();
        ItemStack secondaryResult = recipe.getSecondaryResultItem();
        if (!this.canOutput(result, secondaryResult)) {
            return;
        }
        this.placeOutput(FIRST_OUTPUT_SLOT, result);
        this.placeOutput(SECOND_OUTPUT_SLOT, secondaryResult);
        // Port fix: upstream always took one item from the container slot, even for recipes without a container. Taken
        // before the leftovers, so a leftover that is this recipe's container can refill the emptied slot.
        if (recipe.hasContainer()) {
            this.itemHandler.extractItem(CONTAINER_SLOT, 1, false);
        }
        for (int slot : slots) {
            ItemStack stack = this.itemHandler.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack remainder = remainderOf(stack);
            this.itemHandler.extractItem(slot, 1, false);
            if (!remainder.isEmpty()) {
                this.storeOrDropRemainder(recipe, remainder);
            }
        }
    }

    /** setStackInSlot, since the handler refuses inserts into the output slots. */
    private void placeOutput(int slot, ItemStack output) {
        if (output.isEmpty()) {
            return;
        }
        ItemStack existing = this.itemHandler.getStackInSlot(slot);
        if (existing.isEmpty()) {
            this.itemHandler.setStackInSlot(slot, output.copy());
        } else {
            ItemStack grown = existing.copy();
            grown.grow(output.getCount());
            this.itemHandler.setStackInSlot(slot, grown);
        }
    }

    /**
     * With stallWhenFull, true only when every leftover of this batch (the same slots craftItem takes) fits, by the
     * shared rule (as {@link #storeOrDropRemainder}). The container slot is counted as craftItem leaves it (one taken).
     */
    private boolean remaindersFit(FermentingRecipe recipe, int[] slots) {
        if (!VintageDelightCommonConfig.remainderBuffer() || !VintageDelightCommonConfig.stallWhenFull()) {
            return true;
        }
        List<ItemStack> leftovers = new ArrayList<>();
        for (int slot : slots) {
            ItemStack remainder = remainderOf(this.itemHandler.getStackInSlot(slot));
            if (!remainder.isEmpty()) {
                leftovers.add(remainder);
            }
        }
        ItemStack[] planned = this.leftoverSlots();
        if (recipe.hasContainer()) {
            planned[0].shrink(1);
        }
        return LeftoverPlan.place(leftovers, planned, this.leftoverLimits(), servedIn(recipe)).isEmpty();
    }

    /** What the container slot takes back: this recipe's container (remainder_buffer.md 3.8). */
    private static Predicate<ItemStack> servedIn(FermentingRecipe recipe) {
        return stack -> recipe.hasContainer() && recipe.getContainerIngredient().test(stack);
    }

    /** Copies of [container slot, leftover slot], for {@link LeftoverPlan}. */
    private ItemStack[] leftoverSlots() {
        return LeftoverPlan.copies(this.itemHandler.getStackInSlot(CONTAINER_SLOT), this.itemHandler.getStackInSlot(REMAINDER_SLOT));
    }

    private int[] leftoverLimits() {
        return new int[]{this.itemHandler.getSlotLimit(CONTAINER_SLOT), this.itemHandler.getSlotLimit(REMAINDER_SLOT)};
    }

    /** The item's crafting leftover; a potion leaves its glass bottle (vanilla gives potions none). */
    private static ItemStack remainderOf(ItemStack stack) {
        if (stack.hasCraftingRemainingItem()) {
            return stack.getCraftingRemainingItem();
        }
        return stack.getItem() instanceof PotionItem ? new ItemStack(Items.GLASS_BOTTLE) : ItemStack.EMPTY;
    }

    /**
     * The shared leftover rule ({@link LeftoverPlan}, remainder_buffer.md 3.8; config remainderBuffer): into the
     * container slot when it still holds the same item, else the leftover slot, else the emptied container slot when
     * it is this recipe's container; what does not fit, or all of it with the buffer off, is queued to be thrown out on
     * top of the jar after overflowEjectDelayTicks. Called once per consumed item. A leftover-slot container the next
     * batch needs moves over later ({@link #pullForNextBatch}).
     */
    private void storeOrDropRemainder(FermentingRecipe recipe, ItemStack remainder) {
        List<ItemStack> rest = List.of(remainder.copy());
        if (VintageDelightCommonConfig.remainderBuffer()) {
            ItemStack[] slots = this.leftoverSlots();
            rest = LeftoverPlan.place(rest, slots, this.leftoverLimits(), servedIn(recipe));
            if (!ItemStack.matches(slots[0], this.itemHandler.getStackInSlot(CONTAINER_SLOT))) {
                this.itemHandler.setStackInSlot(CONTAINER_SLOT, slots[0]);
            }
            if (!ItemStack.matches(slots[1], this.itemHandler.getStackInSlot(REMAINDER_SLOT))) {
                this.itemHandler.setStackInSlot(REMAINDER_SLOT, slots[1]);
            }
        }
        int delay = VintageDelightCommonConfig.overflowEjectDelayTicks();
        for (ItemStack thrown : rest) {
            if (delay <= 0) {
                this.eject(thrown);
                continue;
            }
            this.pendingEject.add(thrown.copy());
            this.pendingEjectTicks.add(delay);
            this.setChanged();
        }
    }

    private void tickPendingEject() {
        if (this.pendingEject.isEmpty()) {
            return;
        }
        for (int i = this.pendingEject.size() - 1; i >= 0; i--) {
            int left = this.pendingEjectTicks.get(i) - 1;
            if (left > 0) {
                this.pendingEjectTicks.set(i, left);
                continue;
            }
            ItemStack stack = this.pendingEject.remove(i);
            this.pendingEjectTicks.remove(i);
            this.eject(stack);
        }
        this.setChanged();
    }

    private void eject(ItemStack stack) {
        Containers.dropItemStack(this.level, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.0, this.worldPosition.getZ() + 0.5, stack.copy());
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

}
