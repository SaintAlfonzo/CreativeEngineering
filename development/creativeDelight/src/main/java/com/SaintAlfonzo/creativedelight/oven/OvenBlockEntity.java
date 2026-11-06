package com.SaintAlfonzo.creativedelight.oven;

import com.SaintAlfonzo.creativecommon.compat.RemainderView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import com.SaintAlfonzo.creativedelight.cooking.RemainderBuffer;
import com.SaintAlfonzo.creativedelight.cooking.RemainderOutputHandler;
import com.SaintAlfonzo.creativedelight.shared.StationContainers;

/**
 * Farmer's Delight's CookingPotBlockEntity (1.19.2-1.2.4, MIT) adapted for baking: a tool slot the recipe can ask
 * for (never used up), the oven's own heat rule ({@link OvenHeat}) mirrored into the block's {@code lit} state (which
 * is what makes it a stove-heat source for a pot on top), side automation from every face, no recipe book and no
 * meal-in-item. Slots: 0-5 ingredients, 6 tool, 7 result waiting for plating, 8 plating container, 9 output.
 * Ingredient leftovers go to the container slot or a {@link RemainderBuffer} (feature_writeup/remainder_buffer.md).
 */
public class OvenBlockEntity extends BlockEntity implements MenuProvider, RemainderView.Holder {

    public static final int TOOL_SLOT = 6;
    public static final int MEAL_DISPLAY_SLOT = 7;
    public static final int CONTAINER_SLOT = 8;
    public static final int OUTPUT_SLOT = 9;
    public static final int INVENTORY_SIZE = OUTPUT_SLOT + 1;

    /** The oven's plating containers: every baking recipe's container, or its result's crafting remainder. */
    private static final class Containers {
        static final StationContainers OVEN = new StationContainers("creativeengineering:oven",
                OvenRegistry.BAKING_TYPE.get(),
                r -> r instanceof BakingRecipe b
                        ? List.of(b.getOutputContainer().isEmpty() ? b.getResultItem().getCraftingRemainingItem() : b.getOutputContainer())
                        : List.of(),
                List.of());
    }

    /** Whether {@code stack} is one of the oven's containers (remainder_buffer.md 3.8). */
    public boolean isOvenContainer(ItemStack stack) {
        return Containers.OVEN.isContainer(level, stack);
    }

    // FD's list: ingredients whose leftover isn't their crafting remainder.
    private static final Map<Item, Item> INGREDIENT_REMAINDER_OVERRIDES = Map.ofEntries(
            Map.entry(Items.POWDER_SNOW_BUCKET, Items.BUCKET),
            Map.entry(Items.AXOLOTL_BUCKET, Items.BUCKET),
            Map.entry(Items.COD_BUCKET, Items.BUCKET),
            Map.entry(Items.PUFFERFISH_BUCKET, Items.BUCKET),
            Map.entry(Items.SALMON_BUCKET, Items.BUCKET),
            Map.entry(Items.TROPICAL_FISH_BUCKET, Items.BUCKET),
            Map.entry(Items.SUSPICIOUS_STEW, Items.BOWL),
            Map.entry(Items.MUSHROOM_STEW, Items.BOWL),
            Map.entry(Items.RABBIT_STEW, Items.BOWL),
            Map.entry(Items.BEETROOT_SOUP, Items.BOWL),
            Map.entry(Items.POTION, Items.GLASS_BOTTLE),
            Map.entry(Items.SPLASH_POTION, Items.GLASS_BOTTLE),
            Map.entry(Items.LINGERING_POTION, Items.GLASS_BOTTLE),
            Map.entry(Items.EXPERIENCE_BOTTLE, Items.GLASS_BOTTLE)
    );

    private final ItemStackHandler inventory = createHandler();
    // A buffer change also updates the client's copy (WTHIT line, a hopper or shift-click emptying it with no GUI open).
    private final RemainderBuffer remainders = new RemainderBuffer(this::inventoryChanged);
    private final LazyOptional<IItemHandler> automationHandler =
            LazyOptional.of(() -> new RemainderOutputHandler(new OvenItemHandler(this), remainders));
    private final ContainerData data = createData();
    private final Object2IntOpenHashMap<ResourceLocation> usedRecipeTracker = new Object2IntOpenHashMap<>();

    private int cookTime;
    private int cookTimeTotal;
    private boolean heated;
    private ItemStack mealContainerStack = ItemStack.EMPTY;

    @Nullable
    private ResourceLocation lastRecipeID;
    private boolean checkNewRecipe = true;

    public OvenBlockEntity(BlockPos pos, BlockState state) {
        super(OvenRegistry.OVEN_BLOCK_ENTITY.get(), pos, state);
    }

    // ------------------------------------------------------------------ tick

    public static void serverTick(Level level, BlockPos pos, BlockState state, OvenBlockEntity oven) {
        oven.heated = OvenHeat.isHeated(level, pos);
        if (state.getValue(OvenBlock.LIT) != oven.heated) {
            level.setBlock(pos, state.setValue(OvenBlock.LIT, oven.heated), Block.UPDATE_ALL);
        }

        boolean didInventoryChange = false;
        if (oven.heated && oven.hasInput()) {
            Optional<BakingRecipe> recipe = oven.getMatchingRecipe(new RecipeWrapper(oven.inventory));
            if (recipe.isPresent() && oven.canBake(recipe.get())) {
                didInventoryChange = oven.processBaking(recipe.get());
            } else {
                oven.cookTime = 0;
            }
        } else if (oven.cookTime > 0) {
            oven.cookTime = Mth.clamp(oven.cookTime - 2, 0, oven.cookTimeTotal);
        }

        ItemStack mealStack = oven.getMeal();
        // a leftover in the buffer that plates the waiting result moves to the container slot (remainder_buffer.md 3.8)
        int mealsWaiting = !mealStack.isEmpty() && oven.doesMealHaveContainer(mealStack) ? mealStack.getCount() : 0;
        if (oven.remainders.pullForPlating(oven.inventory, CONTAINER_SLOT, mealsWaiting, oven::isContainerValid)) {
            didInventoryChange = true;
        }
        if (!mealStack.isEmpty()) {
            if (!oven.doesMealHaveContainer(mealStack)) {
                oven.moveMealToOutput();
                didInventoryChange = true;
            } else if (!oven.inventory.getStackInSlot(CONTAINER_SLOT).isEmpty() && !oven.remainders.holdPlating()) {
                oven.useStoredContainersOnMeal();
                didInventoryChange = true;
            }
        }

        if (didInventoryChange) {
            oven.inventoryChanged();
        }
    }

    private Optional<BakingRecipe> getMatchingRecipe(RecipeWrapper wrapper) {
        if (level == null) return Optional.empty();

        if (lastRecipeID != null) {
            Optional<? extends Recipe<?>> last = level.getRecipeManager().byKey(lastRecipeID);
            if (last.isPresent() && last.get() instanceof BakingRecipe recipe) {
                if (recipe.matches(wrapper, level)) {
                    return Optional.of(recipe);
                }
                if (recipe.getResultItem().sameItem(getMeal())) {
                    return Optional.empty();
                }
            }
        }

        if (checkNewRecipe) {
            Optional<BakingRecipe> recipe = level.getRecipeManager().getRecipeFor(OvenRegistry.BAKING_TYPE.get(), wrapper, level);
            if (recipe.isPresent()) {
                ResourceLocation newRecipeID = recipe.get().getId();
                if (lastRecipeID != null && !lastRecipeID.equals(newRecipeID)) {
                    cookTime = 0;
                }
                lastRecipeID = newRecipeID;
                return recipe;
            }
        }

        checkNewRecipe = false;
        return Optional.empty();
    }

    private boolean hasInput() {
        for (int i = 0; i < BakingRecipe.INPUT_SLOTS; ++i) {
            if (!inventory.getStackInSlot(i).isEmpty()) return true;
        }
        return false;
    }

    private boolean canBake(BakingRecipe recipe) {
        return hasRoomForResult(recipe) && hasRoomForLeftovers(recipe);
    }

    private boolean hasRoomForLeftovers(BakingRecipe recipe) {
        if (!RemainderBuffer.stalls()) return true;
        boolean fits = remainders.fits(
                RemainderBuffer.leftovers(inventory, 0, BakingRecipe.INPUT_SLOTS, INGREDIENT_REMAINDER_OVERRIDES,
                        recipe.slotCounts(new RecipeWrapper(inventory))),
                inventory, CONTAINER_SLOT, RemainderBuffer.servedIn(recipe.getOutputContainer(), recipe.getResultItem()));
        if (!fits && level != null) {
            remainders.markStalled(level.getGameTime());
        }
        return fits;
    }

    private boolean hasRoomForResult(BakingRecipe recipe) {
        ItemStack resultStack = recipe.getResultItem();
        if (resultStack.isEmpty()) {
            return false;
        }
        ItemStack storedMealStack = inventory.getStackInSlot(MEAL_DISPLAY_SLOT);
        if (storedMealStack.isEmpty()) {
            return true;
        } else if (!storedMealStack.sameItem(resultStack)) {
            return false;
        } else if (storedMealStack.getCount() + resultStack.getCount() <= inventory.getSlotLimit(MEAL_DISPLAY_SLOT)) {
            return true;
        }
        return storedMealStack.getCount() + resultStack.getCount() <= resultStack.getMaxStackSize();
    }

    private boolean processBaking(BakingRecipe recipe) {
        if (level == null) return false;

        ++cookTime;
        cookTimeTotal = recipe.getCookTime();
        if (cookTime < cookTimeTotal) {
            return false;
        }

        int[] used = recipe.slotCounts(new RecipeWrapper(inventory));
        if (used == null) {
            return false;
        }
        cookTime = 0;
        mealContainerStack = recipe.getOutputContainer().copy();
        ItemStack resultStack = recipe.getResultItem();
        ItemStack storedMealStack = inventory.getStackInSlot(MEAL_DISPLAY_SLOT);
        if (storedMealStack.isEmpty()) {
            inventory.setStackInSlot(MEAL_DISPLAY_SLOT, resultStack.copy());
        } else if (storedMealStack.sameItem(resultStack)) {
            storedMealStack.grow(resultStack.getCount());
        }
        usedRecipeTracker.addTo(recipe.getId(), 1);

        // Ingredients only: the tool in slot 6 stays as it is (no durability). Each slot gives up its ingredient's
        // count, and one leftover per item taken.
        for (int i = 0; i < BakingRecipe.INPUT_SLOTS; ++i) {
            ItemStack slotStack = inventory.getStackInSlot(i);
            if (slotStack.isEmpty()) continue;
            for (int n = 0; n < used[i]; ++n) {
                if (slotStack.hasCraftingRemainingItem()) {
                    ejectIngredientRemainder(slotStack.getCraftingRemainingItem());
                } else if (INGREDIENT_REMAINDER_OVERRIDES.containsKey(slotStack.getItem())) {
                    ejectIngredientRemainder(INGREDIENT_REMAINDER_OVERRIDES.get(slotStack.getItem()).getDefaultInstance());
                }
            }
            slotStack.shrink(used[i]);
        }
        return true;
    }

    private void ejectIngredientRemainder(ItemStack remainderStack) {
        if (level == null) return;
        if (RemainderBuffer.enabled()) {
            remainderStack = remainders.store(remainderStack, inventory, CONTAINER_SLOT, this::isContainerValid);
            if (remainderStack.isEmpty()) return;
        }
        // Out of the front, where the door is (the pot uses its left side; the oven's top is often taken).
        Direction direction = getBlockState().getValue(OvenBlock.FACING);
        double x = worldPosition.getX() + 0.5 + direction.getStepX() * 0.6;
        double y = worldPosition.getY() + 0.4;
        double z = worldPosition.getZ() + 0.5 + direction.getStepZ() * 0.6;
        ItemEntity entity = new ItemEntity(level, x, y, z, remainderStack);
        entity.setDeltaMovement(direction.getStepX() * 0.08F, 0.25F, direction.getStepZ() * 0.08F);
        level.addFreshEntity(entity);
    }

    // ------------------------------------------------------------------ plating

    private void moveMealToOutput() {
        ItemStack mealStack = inventory.getStackInSlot(MEAL_DISPLAY_SLOT);
        ItemStack outputStack = inventory.getStackInSlot(OUTPUT_SLOT);
        int mealCount = Math.min(mealStack.getCount(), mealStack.getMaxStackSize() - outputStack.getCount());
        if (outputStack.isEmpty()) {
            inventory.setStackInSlot(OUTPUT_SLOT, mealStack.split(mealCount));
        } else if (outputStack.getItem() == mealStack.getItem()) {
            mealStack.shrink(mealCount);
            outputStack.grow(mealCount);
        }
    }

    private void useStoredContainersOnMeal() {
        ItemStack mealStack = inventory.getStackInSlot(MEAL_DISPLAY_SLOT);
        ItemStack containerInputStack = inventory.getStackInSlot(CONTAINER_SLOT);
        ItemStack outputStack = inventory.getStackInSlot(OUTPUT_SLOT);

        if (isContainerValid(containerInputStack) && outputStack.getCount() < outputStack.getMaxStackSize()) {
            int smallerStackCount = Math.min(mealStack.getCount(), containerInputStack.getCount());
            int mealCount = Math.min(smallerStackCount, mealStack.getMaxStackSize() - outputStack.getCount());
            if (outputStack.isEmpty()) {
                containerInputStack.shrink(mealCount);
                inventory.setStackInSlot(OUTPUT_SLOT, mealStack.split(mealCount));
            } else if (outputStack.getItem() == mealStack.getItem()) {
                mealStack.shrink(mealCount);
                containerInputStack.shrink(mealCount);
                outputStack.grow(mealCount);
            }
        }
    }

    /** Right-click with the plating item: one plated result back, or EMPTY. */
    public ItemStack useHeldItemOnMeal(ItemStack container) {
        if (isContainerValid(container) && !getMeal().isEmpty()) {
            container.shrink(1);
            ItemStack served = getMeal().split(1);
            inventoryChanged();
            return served;
        }
        return ItemStack.EMPTY;
    }

    private boolean doesMealHaveContainer(ItemStack meal) {
        return !mealContainerStack.isEmpty() || meal.hasCraftingRemainingItem();
    }

    public boolean isContainerValid(ItemStack containerItem) {
        if (containerItem.isEmpty()) return false;
        if (!mealContainerStack.isEmpty()) {
            return mealContainerStack.sameItem(containerItem);
        }
        return getMeal().getCraftingRemainingItem().sameItem(containerItem);
    }

    /** The plating item the waiting result needs (for the screen tooltip), or EMPTY. */
    public ItemStack getContainer() {
        ItemStack mealStack = getMeal();
        if (!mealStack.isEmpty() && !mealContainerStack.isEmpty()) {
            return mealContainerStack;
        }
        return mealStack.getCraftingRemainingItem();
    }

    /**
     * Whether {@code stack} belongs in the container slot (remainder_buffer.md 3.8): one of the oven's containers (what
     * any baking recipe is served in, {@link Containers}), or what the waiting result needs. Containers go nowhere
     * else (hand, shift-click and automation), and nothing else goes there.
     */
    boolean isPlatingItem(ItemStack stack) {
        return isContainerValid(stack) || isOvenContainer(stack);
    }

    // ------------------------------------------------------------------ experience

    /** FD awards recipes and experience when the output is taken (OvenResultSlot) or the oven breaks. */
    public void awardUsedRecipes(Player player) {
        List<Recipe<?>> usedRecipes = getUsedRecipesAndPopExperience(player.level, player.position());
        player.awardRecipes(usedRecipes);
        usedRecipeTracker.clear();
    }

    public List<Recipe<?>> getUsedRecipesAndPopExperience(Level level, Vec3 pos) {
        List<Recipe<?>> list = new ArrayList<>();
        for (Object2IntMap.Entry<ResourceLocation> entry : usedRecipeTracker.object2IntEntrySet()) {
            level.getRecipeManager().byKey(entry.getKey()).ifPresent(recipe -> {
                list.add(recipe);
                if (level instanceof ServerLevel serverLevel && recipe instanceof BakingRecipe baking) {
                    splitAndSpawnExperience(serverLevel, pos, entry.getIntValue(), baking.getExperience());
                }
            });
        }
        return list;
    }

    private static void splitAndSpawnExperience(ServerLevel level, Vec3 pos, int craftedAmount, float experience) {
        int expTotal = Mth.floor((float) craftedAmount * experience);
        float expFraction = Mth.frac((float) craftedAmount * experience);
        if (expFraction != 0.0F && Math.random() < (double) expFraction) {
            ++expTotal;
        }
        ExperienceOrb.award(level, pos, expTotal);
    }

    // ------------------------------------------------------------------ access

    public ItemStackHandler getInventory() {
        return inventory;
    }

    /** The leftover slots as Common's WTHIT line reads them. */
    @Override
    public RemainderView ce$remainderView() {
        return remainders;
    }

    public RemainderBuffer getRemainders() {
        return remainders;
    }

    public ItemStack getMeal() {
        return inventory.getStackInSlot(MEAL_DISPLAY_SLOT);
    }

    public boolean isHeated() {
        return heated;
    }

    /** Everything, the waiting result included (unlike FD's pot, the oven doesn't keep it in the item). */
    public NonNullList<ItemStack> getDroppableInventory() {
        NonNullList<ItemStack> drops = NonNullList.create();
        for (int i = 0; i < INVENTORY_SIZE; ++i) {
            drops.add(inventory.getStackInSlot(i));
        }
        for (int i = 0; i < remainders.getSlots(); ++i) {
            drops.add(remainders.getStackInSlot(i));
        }
        return drops;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.creativeengineering.oven");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new OvenMenu(id, playerInventory, this, data);
    }

    // ------------------------------------------------------------------ save / sync

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        inventory.deserializeNBT(compound.getCompound("Inventory"));
        // Guarded: an empty tag would wipe the slots (ItemStackHandler.deserializeNBT makes a new list).
        if (compound.contains("Remainders")) {
            remainders.deserializeNBT(compound.getCompound("Remainders"));
        }
        cookTime = compound.getInt("CookTime");
        cookTimeTotal = compound.getInt("CookTimeTotal");
        mealContainerStack = ItemStack.of(compound.getCompound("Container"));
        usedRecipeTracker.clear();
        CompoundTag compoundRecipes = compound.getCompound("RecipesUsed");
        for (String key : compoundRecipes.getAllKeys()) {
            usedRecipeTracker.put(new ResourceLocation(key), compoundRecipes.getInt(key));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putInt("CookTime", cookTime);
        compound.putInt("CookTimeTotal", cookTimeTotal);
        compound.put("Container", mealContainerStack.serializeNBT());
        compound.put("Inventory", inventory.serializeNBT());
        compound.put("Remainders", remainders.serializeNBT());
        CompoundTag compoundRecipes = new CompoundTag();
        usedRecipeTracker.forEach((recipeId, craftedAmount) -> compoundRecipes.putInt(recipeId.toString(), craftedAmount));
        compound.put("RecipesUsed", compoundRecipes);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag compound = new CompoundTag();
        compound.put("Container", mealContainerStack.serializeNBT());
        compound.put("Inventory", inventory.serializeNBT());
        // The client loads this tag on every block update (taking the output is one); without the buffer its copy
        // of the two leftover slots went empty while the server still held them (TODO cooking/kitchen #14).
        compound.put("Remainders", remainders.serializeNBT());
        return compound;
    }

    @Override
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void inventoryChanged() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    // ------------------------------------------------------------------ capabilities

    @Override
    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return automationHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        automationHandler.invalidate();
    }

    private ItemStackHandler createHandler() {
        return new ItemStackHandler(INVENTORY_SIZE) {
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                if (slot < BakingRecipe.INPUT_SLOTS) return !isOvenContainer(stack);
                if (slot == CONTAINER_SLOT) return isPlatingItem(stack);
                return true;
            }

            @Override
            public int getSlotLimit(int slot) {
                return slot == TOOL_SLOT ? 1 : super.getSlotLimit(slot);
            }

            @Override
            protected void onContentsChanged(int slot) {
                if (slot < BakingRecipe.INPUT_SLOTS || slot == TOOL_SLOT) {
                    checkNewRecipe = true;
                }
                inventoryChanged();
            }
        };
    }

    private ContainerData createData() {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> cookTime;
                    case 1 -> cookTimeTotal;
                    case 2 -> heated ? 1 : 0;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> cookTime = value;
                    case 1 -> cookTimeTotal = value;
                    case 2 -> heated = value != 0;
                    default -> {}
                }
            }

            @Override
            public int getCount() {
                return 3;
            }
        };
    }
}
