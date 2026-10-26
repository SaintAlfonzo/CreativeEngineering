package net.ribs.vintagedelight.block.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.ribs.vintagedelight.config.VintageDelightCommonConfig;
import net.ribs.vintagedelight.item.ModTags;
import net.ribs.vintagedelight.recipe.FermentingRecipe;
import net.ribs.vintagedelight.screen.FermentingJarMenu;

/**
 * The Pickling Jar (upstream's fermenting jar; the id is kept). Slots 0-5 ingredients, 6 container, 7-8 outputs, 9
 * leftovers (port: the empty bottles and bowls of the ingredients, take-only, as the keg's; config remainderBuffer).
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
            return slot != CONTAINER_SLOT || stack.is(ModTags.CONTAINER_ITEMS);
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
    private final SimpleContainer inputInventory = new SimpleContainer(7);

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
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) {
            return;
        }
        for (int i = FIRST_INGREDIENT_SLOT; i <= CONTAINER_SLOT; i++) {
            this.inputInventory.setItem(i, this.itemHandler.getStackInSlot(i));
        }
        Optional<FermentingRecipe> recipeOpt = level.getRecipeManager().getRecipeFor(FermentingRecipe.Type.INSTANCE, this.inputInventory, level);
        if (recipeOpt.isPresent()) {
            FermentingRecipe recipe = recipeOpt.get();
            ItemStack result = recipe.getResultItem();
            ItemStack secondaryResult = recipe.getSecondaryResultItem();
            this.maxProgress = recipe.getProcessingTime();
            if (this.canOutput(result, secondaryResult) && this.remaindersFit(recipe)) {
                this.progress++;
                if (this.progress >= this.maxProgress) {
                    this.craftItem(recipe);
                    this.progress = 0;
                }
            } else {
                this.progress = 0;
            }
        } else {
            this.progress = 0;
        }
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

    private void craftItem(FermentingRecipe recipe) {
        ItemStack result = recipe.getResultItem();
        ItemStack secondaryResult = recipe.getSecondaryResultItem();
        if (!this.canOutput(result, secondaryResult)) {
            return;
        }
        this.placeOutput(FIRST_OUTPUT_SLOT, result);
        this.placeOutput(SECOND_OUTPUT_SLOT, secondaryResult);
        for (Ingredient ingredient : recipe.getIngredients()) {
            ItemStack remainder = this.consumeIngredient(ingredient);
            if (!remainder.isEmpty()) {
                this.storeOrDropRemainder(remainder);
            }
        }
        // Port fix: upstream always took one item from the container slot, even for recipes without a container
        if (recipe.hasContainer()) {
            this.itemHandler.extractItem(CONTAINER_SLOT, 1, false);
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

    /** Takes one item the ingredient matches; returns its leftover (empty bottle, bowl, bucket) or EMPTY. */
    private ItemStack consumeIngredient(Ingredient ingredient) {
        for (int i = FIRST_INGREDIENT_SLOT; i <= LAST_INPUT_SLOT; i++) {
            ItemStack stack = this.itemHandler.getStackInSlot(i);
            if (ingredient.test(stack)) {
                ItemStack remainder = remainderOf(stack);
                this.itemHandler.extractItem(i, 1, false);
                return remainder;
            }
        }
        return ItemStack.EMPTY;
    }

    /** With stallWhenFull, true only when every leftover of this batch fits in the leftover slot. */
    private boolean remaindersFit(FermentingRecipe recipe) {
        if (!VintageDelightCommonConfig.remainderBuffer() || !VintageDelightCommonConfig.stallWhenFull()) {
            return true;
        }
        ItemStack slot = this.itemHandler.getStackInSlot(REMAINDER_SLOT).copy();
        // the same first-match walk as craftItem, on counts
        int[] left = new int[LAST_INPUT_SLOT + 1];
        for (int i = FIRST_INGREDIENT_SLOT; i <= LAST_INPUT_SLOT; i++) {
            left[i] = this.itemHandler.getStackInSlot(i).getCount();
        }
        for (Ingredient ingredient : recipe.getIngredients()) {
            for (int i = FIRST_INGREDIENT_SLOT; i <= LAST_INPUT_SLOT; i++) {
                ItemStack stack = this.itemHandler.getStackInSlot(i);
                if (left[i] > 0 && ingredient.test(stack)) {
                    left[i]--;
                    ItemStack remainder = remainderOf(stack);
                    if (!remainder.isEmpty()) {
                        if (slot.isEmpty()) {
                            slot = remainder.copy();
                        } else if (ItemStack.isSameItemSameTags(slot, remainder) && slot.getCount() + remainder.getCount() <= slot.getMaxStackSize()) {
                            slot.grow(remainder.getCount());
                        } else {
                            return false;
                        }
                    }
                    break;
                }
            }
        }
        return true;
    }

    /** The item's crafting leftover; a potion leaves its glass bottle (vanilla gives potions none). */
    private static ItemStack remainderOf(ItemStack stack) {
        if (stack.hasCraftingRemainingItem()) {
            return stack.getCraftingRemainingItem();
        }
        return stack.getItem() instanceof PotionItem ? new ItemStack(Items.GLASS_BOTTLE) : ItemStack.EMPTY;
    }

    /** Into the leftover slot (config remainderBuffer), else or when full dropped on top of the jar. */
    private void storeOrDropRemainder(ItemStack remainder) {
        if (VintageDelightCommonConfig.remainderBuffer()) {
            ItemStack existing = this.itemHandler.getStackInSlot(REMAINDER_SLOT);
            if (existing.isEmpty()) {
                this.itemHandler.setStackInSlot(REMAINDER_SLOT, remainder.copy());
                return;
            }
            if (ItemStack.isSameItemSameTags(existing, remainder) && existing.getCount() + remainder.getCount() <= existing.getMaxStackSize()) {
                this.placeOutput(REMAINDER_SLOT, remainder);
                return;
            }
        }
        Containers.dropItemStack(this.level, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.0, this.worldPosition.getZ() + 0.5, remainder.copy());
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
