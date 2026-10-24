package com.sammy.minersdelight.content.block.copper_pot;

import com.sammy.minersdelight.logic.CupConversions;
import com.sammy.minersdelight.registry.MDBlockEntities;
import com.sammy.minersdelight.registry.MDItems;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.block.entity.HeatableBlockEntity;
import vectorwing.farmersdelight.common.block.entity.SyncedBlockEntity;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.mixin.accessor.RecipeManagerAccessor;
import vectorwing.farmersdelight.common.registry.ModParticleTypes;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;
import vectorwing.farmersdelight.common.utility.ItemUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The Copper Pot: a small Farmer's Delight cooking pot. Four ingredient slots (FD recipes with up to four ingredients),
 * cooks in 80% of the recipe's time, and serves a soup that has a cup ({@link CupConversions}) as two cups per bowl
 * into Copper Cups.
 * <p>
 * Create Central Kitchen injects at the {@link #setRecipeUsed} call inside {@link #processCooking} (blaze stove signal):
 * keep that method's name, descriptor and the call in it.
 */
public class CopperPotBlockEntity extends SyncedBlockEntity implements MenuProvider, HeatableBlockEntity, Nameable, RecipeHolder {
    public static final int INPUT_SLOTS = 4;
    public static final int MEAL_DISPLAY_SLOT = 4;
    public static final int CONTAINER_SLOT = 5;
    public static final int OUTPUT_SLOT = 6;
    public static final int INVENTORY_SIZE = 7;
    /** FD cooking recipes match against the cooking pot's 9-slot layout (6 inputs, then meal, container, output). */
    private static final int FD_INVENTORY_SIZE = 9;

    private final ItemStackHandler inventory = new ItemStackHandler(INVENTORY_SIZE) {
        @Override
        protected void onContentsChanged(int slot) {
            if (slot < INPUT_SLOTS) {
                checkNewRecipe = true;
            }
            inventoryChanged();
        }
    };
    private final LazyOptional<IItemHandler> inputHandler = LazyOptional.of(() -> new CopperPotItemHandler(inventory, Direction.UP));
    private final LazyOptional<IItemHandler> outputHandler = LazyOptional.of(() -> new CopperPotItemHandler(inventory, Direction.DOWN));
    private final Object2IntOpenHashMap<ResourceLocation> usedRecipeTracker = new Object2IntOpenHashMap<>();
    private int cookTime;
    private int cookTimeTotal;
    private ItemStack mealContainerStack = ItemStack.EMPTY;
    @Nullable
    private Component customName;
    @Nullable
    private ResourceLocation lastRecipeID;
    private boolean checkNewRecipe = true;
    protected final ContainerData cookingData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> cookTime;
                case 1 -> cookTimeTotal;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                cookTime = value;
            } else if (index == 1) {
                cookTimeTotal = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public CopperPotBlockEntity(BlockPos pos, BlockState state) {
        super(MDBlockEntities.COPPER_POT.get(), pos, state);
    }

    // ---- the pot as an item (meal kept in BlockEntityTag) ----

    @Nullable
    private static ItemStackHandler storedInventory(ItemStack potStack) {
        if (!potStack.is(MDItems.COPPER_POT.get())) {
            return null;
        }
        CompoundTag tag = potStack.getTagElement("BlockEntityTag");
        if (tag == null || !tag.getCompound("Inventory").contains("Items", Tag.TAG_LIST)) {
            return null;
        }
        ItemStackHandler handler = new ItemStackHandler();
        handler.deserializeNBT(tag.getCompound("Inventory"));
        return handler;
    }

    public static ItemStack getMealFromItem(ItemStack potStack) {
        ItemStackHandler handler = storedInventory(potStack);
        return handler == null ? ItemStack.EMPTY : handler.getStackInSlot(MEAL_DISPLAY_SLOT);
    }

    public static ItemStack getContainerFromItem(ItemStack potStack) {
        CompoundTag tag = potStack.is(MDItems.COPPER_POT.get()) ? potStack.getTagElement("BlockEntityTag") : null;
        return tag == null ? ItemStack.EMPTY : ItemStack.of(tag.getCompound("Container"));
    }

    // ---- save / load ----

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        inventory.deserializeNBT(compound.getCompound("Inventory"));
        cookTime = compound.getInt("CookTime");
        cookTimeTotal = compound.getInt("CookTimeTotal");
        mealContainerStack = ItemStack.of(compound.getCompound("Container"));
        if (compound.contains("CustomName", Tag.TAG_STRING)) {
            customName = Component.Serializer.fromJson(compound.getString("CustomName"));
        }
        CompoundTag recipes = compound.getCompound("RecipesUsed");
        for (String key : recipes.getAllKeys()) {
            usedRecipeTracker.put(new ResourceLocation(key), recipes.getInt(key));
        }
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putInt("CookTime", cookTime);
        compound.putInt("CookTimeTotal", cookTimeTotal);
        compound.put("Container", mealContainerStack.serializeNBT());
        if (customName != null) {
            compound.putString("CustomName", Component.Serializer.toJson(customName));
        }
        compound.put("Inventory", inventory.serializeNBT());
        CompoundTag recipes = new CompoundTag();
        usedRecipeTracker.forEach((id, count) -> recipes.putInt(id.toString(), count));
        compound.put("RecipesUsed", recipes);
    }

    /** What a broken or picked pot keeps: the meal, its container and the name. Empty when there is no meal. */
    public CompoundTag writeMeal(CompoundTag compound) {
        if (getMeal().isEmpty()) {
            return compound;
        }
        ItemStackHandler mealOnly = new ItemStackHandler(INVENTORY_SIZE);
        mealOnly.setStackInSlot(MEAL_DISPLAY_SLOT, getMeal());
        if (customName != null) {
            compound.putString("CustomName", Component.Serializer.toJson(customName));
        }
        compound.put("Container", mealContainerStack.serializeNBT());
        compound.put("Inventory", mealOnly.serializeNBT());
        return compound;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.put("Container", mealContainerStack.serializeNBT());
        tag.put("Inventory", inventory.serializeNBT());
        return tag;
    }

    // ---- ticking ----

    public static void cookingTick(Level level, BlockPos pos, BlockState state, CopperPotBlockEntity pot) {
        boolean changed = false;
        if (pot.isHeated(level, pos) && pot.hasInput()) {
            Optional<CookingPotRecipe> recipe = pot.getMatchingRecipe(pot.createFakeRecipeWrapper());
            if (recipe.isPresent() && pot.canCook(recipe.get())) {
                changed = pot.processCooking(recipe.get(), pot);
            } else {
                pot.cookTime = 0;
            }
        } else if (pot.cookTime > 0) {
            pot.cookTime = Mth.clamp(pot.cookTime - 2, 0, pot.cookTimeTotal);
        }
        ItemStack meal = pot.getMeal();
        if (!meal.isEmpty()) {
            if (!pot.doesMealHaveContainer(meal)) {
                pot.moveMealToOutput();
                changed = true;
            } else if (!pot.inventory.getStackInSlot(CONTAINER_SLOT).isEmpty()) {
                pot.useStoredContainersOnMeal();
                changed = true;
            }
        }
        if (changed) {
            pot.inventoryChanged();
        }
    }

    public static void animationTick(Level level, BlockPos pos, BlockState state, CopperPotBlockEntity pot) {
        if (!pot.isHeated(level, pos)) {
            return;
        }
        RandomSource random = level.random;
        if (random.nextFloat() < 0.2F) {
            level.addParticle(ParticleTypes.BUBBLE_POP, pos.getX() + 0.5 + (random.nextDouble() * 0.6 - 0.3), pos.getY() + 0.7,
                    pos.getZ() + 0.5 + (random.nextDouble() * 0.6 - 0.3), 0, 0, 0);
        }
        if (random.nextFloat() < 0.05F) {
            level.addParticle(ModParticleTypes.STEAM.get(), pos.getX() + 0.5 + (random.nextDouble() * 0.4 - 0.2), pos.getY() + 0.5,
                    pos.getZ() + 0.5 + (random.nextDouble() * 0.4 - 0.2), 0, random.nextBoolean() ? 0.015 : 0.005, 0);
        }
    }

    /** The four inputs in the first slots of FD's 9-slot layout; meal, container and output in its last three. */
    public RecipeWrapper createFakeRecipeWrapper() {
        ItemStackHandler handler = new ItemStackHandler(FD_INVENTORY_SIZE);
        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            handler.setStackInSlot(slot < INPUT_SLOTS ? slot : slot + (FD_INVENTORY_SIZE - INVENTORY_SIZE), inventory.getStackInSlot(slot));
        }
        return new RecipeWrapper(handler);
    }

    @SuppressWarnings("unchecked")
    private Optional<CookingPotRecipe> getMatchingRecipe(RecipeWrapper wrapper) {
        if (level == null) {
            return Optional.empty();
        }
        if (lastRecipeID != null) {
            Recipe<RecipeWrapper> last = ((RecipeManagerAccessor) level.getRecipeManager())
                    .getRecipeMap(ModRecipeTypes.COOKING.get()).get(lastRecipeID);
            if (last instanceof CookingPotRecipe cooking) {
                if (cooking.matches(wrapper, level)) {
                    return Optional.of(cooking);
                }
                if (ItemStack.isSame(servedResult(cooking), getMeal())) {
                    return Optional.empty();
                }
            }
        }
        if (checkNewRecipe) {
            Optional<CookingPotRecipe> found = level.getRecipeManager().getRecipeFor(ModRecipeTypes.COOKING.get(), wrapper, level);
            if (found.isPresent()) {
                lastRecipeID = found.get().getId();
                return found;
            }
        }
        checkNewRecipe = false;
        return Optional.empty();
    }

    /** The recipe's result as this pot serves it: two cups per bowl (up to a stack) when the bowl has a cup. */
    public static ItemStack servedResult(CookingPotRecipe recipe) {
        ItemStack result = recipe.getResultItem();
        Item cup = CupConversions.cupFor(result.getItem());
        if (cup == null) {
            return result;
        }
        ItemStack cups = new ItemStack(cup, Math.min(result.getCount() * 2, new ItemStack(cup).getMaxStackSize()));
        cups.setTag(result.getTag() == null ? null : result.getTag().copy());
        return cups;
    }

    /** The container the served result needs: a Copper Cup for cup soups, else the recipe's own. */
    public static ItemStack servedContainer(CookingPotRecipe recipe) {
        return CupConversions.cupFor(recipe.getResultItem().getItem()) != null ? new ItemStack(MDItems.COPPER_CUP.get()) : recipe.getOutputContainer();
    }

    private boolean hasInput() {
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            if (!inventory.getStackInSlot(slot).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    protected boolean canCook(CookingPotRecipe recipe) {
        if (!hasInput()) {
            return false;
        }
        ItemStack result = servedResult(recipe);
        if (result.isEmpty()) {
            return false;
        }
        ItemStack stored = getMeal();
        if (stored.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSame(stored, result)) {
            return false;
        }
        int total = stored.getCount() + result.getCount();
        return total <= inventory.getSlotLimit(MEAL_DISPLAY_SLOT) || total <= result.getMaxStackSize();
    }

    /** One tick of cooking; true when a meal was finished. CCK injects at the setRecipeUsed call. */
    private boolean processCooking(CookingPotRecipe recipe, CopperPotBlockEntity pot) {
        if (level == null) {
            return false;
        }
        cookTime++;
        cookTimeTotal = (int) (recipe.getCookTime() * 0.8F);
        if (cookTime < cookTimeTotal) {
            return false;
        }
        cookTime = 0;
        mealContainerStack = servedContainer(recipe);
        ItemStack result = servedResult(recipe);
        ItemStack stored = getMeal();
        if (stored.isEmpty()) {
            inventory.setStackInSlot(MEAL_DISPLAY_SLOT, result.copy());
        } else if (ItemStack.isSame(stored, result)) {
            stored.grow(result.getCount());
        }
        pot.setRecipeUsed(recipe);
        Direction side = getBlockState().getValue(CopperPotBlock.FACING).getCounterClockWise();
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            ItemStack input = inventory.getStackInSlot(slot);
            if (input.hasCraftingRemainingItem()) {
                ItemUtils.spawnItemEntity(level, input.getCraftingRemainingItem(),
                        worldPosition.getX() + 0.5 + side.getStepX() * 0.25, worldPosition.getY() + 0.7, worldPosition.getZ() + 0.5 + side.getStepZ() * 0.25,
                        side.getStepX() * 0.08F, 0.25, side.getStepZ() * 0.08F);
            }
            if (!input.isEmpty()) {
                input.shrink(1);
            }
        }
        return true;
    }

    @Override
    public void setRecipeUsed(@Nullable Recipe<?> recipe) {
        if (recipe != null) {
            usedRecipeTracker.addTo(recipe.getId(), 1);
        }
    }

    @Override
    @Nullable
    public Recipe<?> getRecipeUsed() {
        return null;
    }

    @Override
    public void awardUsedRecipes(Player player) {
        player.awardRecipes(getUsedRecipesAndPopExperience(player.getLevel(), player.position()));
        usedRecipeTracker.clear();
    }

    public List<Recipe<?>> getUsedRecipesAndPopExperience(Level level, Vec3 pos) {
        List<Recipe<?>> recipes = new ArrayList<>();
        for (var entry : usedRecipeTracker.object2IntEntrySet()) {
            level.getRecipeManager().byKey(entry.getKey()).ifPresent(recipe -> {
                recipes.add(recipe);
                if (level instanceof ServerLevel server && recipe instanceof CookingPotRecipe cooking) {
                    awardExperience(server, pos, entry.getIntValue(), cooking.getExperience());
                }
            });
        }
        return recipes;
    }

    private static void awardExperience(ServerLevel level, Vec3 pos, int crafted, float experience) {
        int total = Mth.floor(crafted * experience);
        float fraction = Mth.frac(crafted * experience);
        if (fraction != 0.0F && Math.random() < fraction) {
            total++;
        }
        ExperienceOrb.award(level, pos, total);
    }

    // ---- serving ----

    public boolean isHeated() {
        return level != null && isHeated(level, worldPosition);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public ItemStack getMeal() {
        return inventory.getStackInSlot(MEAL_DISPLAY_SLOT);
    }

    /** The container the stored meal is served in. */
    public ItemStack getContainer() {
        return !mealContainerStack.isEmpty() ? mealContainerStack : getMeal().getCraftingRemainingItem();
    }

    public NonNullList<ItemStack> getDroppableInventory() {
        NonNullList<ItemStack> drops = NonNullList.create();
        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            if (slot != MEAL_DISPLAY_SLOT) {
                drops.add(inventory.getStackInSlot(slot));
            }
        }
        return drops;
    }

    private void moveMealToOutput() {
        ItemStack meal = getMeal();
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        int count = Math.min(meal.getCount(), meal.getMaxStackSize() - output.getCount());
        if (output.isEmpty()) {
            inventory.setStackInSlot(OUTPUT_SLOT, meal.split(count));
        } else if (output.getItem() == meal.getItem()) {
            meal.shrink(count);
            output.grow(count);
        }
    }

    private void useStoredContainersOnMeal() {
        ItemStack meal = getMeal();
        ItemStack containers = inventory.getStackInSlot(CONTAINER_SLOT);
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        if (!isContainerValid(containers) || output.getCount() >= output.getMaxStackSize()) {
            return;
        }
        int count = Math.min(Math.min(meal.getCount(), containers.getCount()), meal.getMaxStackSize() - output.getCount());
        if (output.isEmpty()) {
            containers.shrink(count);
            inventory.setStackInSlot(OUTPUT_SLOT, meal.split(count));
        } else if (output.getItem() == meal.getItem()) {
            meal.shrink(count);
            containers.shrink(count);
            output.grow(count);
        }
    }

    /** Serves one portion into the held container, or EMPTY when it is not the right one. */
    public ItemStack useHeldItemOnMeal(ItemStack container) {
        if (isContainerValid(container) && !getMeal().isEmpty()) {
            container.shrink(1);
            return getMeal().split(1);
        }
        return ItemStack.EMPTY;
    }

    private boolean doesMealHaveContainer(ItemStack meal) {
        return !mealContainerStack.isEmpty() || meal.hasCraftingRemainingItem();
    }

    public boolean isContainerValid(ItemStack container) {
        return !container.isEmpty() && ItemStack.isSame(getContainer(), container);
    }

    // ---- naming, menu, capabilities ----

    @Override
    public Component getName() {
        return customName != null ? customName : Component.translatable("miners_delight.container.cooking_pot");
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    @Override
    @Nullable
    public Component getCustomName() {
        return customName;
    }

    public void setCustomName(Component name) {
        customName = name;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new CopperPotMenu(id, playerInventory, this, cookingData);
    }

    @Override
    @NotNull
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return side == null || side == Direction.UP ? inputHandler.cast() : outputHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        inputHandler.invalidate();
        outputHandler.invalidate();
    }
}
