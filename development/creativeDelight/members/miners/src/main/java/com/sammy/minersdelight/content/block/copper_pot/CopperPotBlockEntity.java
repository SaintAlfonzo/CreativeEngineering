package com.sammy.minersdelight.content.block.copper_pot;

import com.sammy.minersdelight.MDConfig;
import com.sammy.minersdelight.registry.MDBlockEntities;
import com.sammy.minersdelight.registry.MDItems;
import com.sammy.minersdelight.shared.CdSettings;
import com.sammy.minersdelight.shared.LeftoverPlan;
import com.sammy.minersdelight.shared.OverflowPull;
import com.sammy.minersdelight.shared.StationContainers;
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
import vectorwing.farmersdelight.common.tag.ModTags;
import vectorwing.farmersdelight.common.utility.ItemUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The Copper Pot: a small Farmer's Delight cooking pot. Four ingredient slots (FD recipes with up to four ingredients),
 * cooks in 80% of the recipe's time. Upstream also served soups into Copper Cups; the pack has no cups (TODO dishes/meals
 * #53), so every meal is served in its recipe's container.
 * <p>
 * Create Central Kitchen injects at the {@link #setRecipeUsed} call inside {@link #processCooking} (blaze stove signal):
 * keep that method's name, descriptor and the call in it.
 */
public class CopperPotBlockEntity extends SyncedBlockEntity implements MenuProvider, HeatableBlockEntity, Nameable, RecipeHolder {
    public static final int INPUT_SLOTS = 4;
    public static final int MEAL_DISPLAY_SLOT = 4;
    public static final int CONTAINER_SLOT = 5;
    public static final int OUTPUT_SLOT = 6;
    /** Port (miners_delight.md I8): take-only slot for the ingredients' leftover containers, right of the output. */
    public static final int LEFTOVER_SLOT = 7;
    public static final int INVENTORY_SIZE = 8;
    /** Inputs, meal, container and output: the slots a recipe sees, and all a pot saved before the leftover slot holds. */
    private static final int COOKING_SLOTS = OUTPUT_SLOT + 1;
    /** FD cooking recipes match against the cooking pot's 9-slot layout (6 inputs, then meal, container, output). */
    private static final int FD_INVENTORY_SIZE = 9;

    private final ItemStackHandler inventory = new ItemStackHandler(INVENTORY_SIZE) {
        /** Containers only in the container slot, never in an ingredient slot (remainder_buffer.md 3.8). */
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot < INPUT_SLOTS) return !isPotContainer(stack);
            if (slot == CONTAINER_SLOT) return isPotContainer(stack) || isContainerValid(stack);
            return true;
        }

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
    /** Leftover slot to container slot moves (not saved), and the plating ticks still to skip after one. */
    private final OverflowPull overflowPull = new OverflowPull();
    private int plateHold;
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

    /** The pot's containers: every FD cooking recipe's container, and #farmersdelight:serving_containers. */
    private static final class Containers {
        static final StationContainers POT = new StationContainers("miners_delight:copper_pot", ModRecipeTypes.COOKING.get(),
                r -> r instanceof CookingPotRecipe c ? List.of(platedIn(c)) : List.of(),
                List.of(ModTags.SERVING_CONTAINERS));
    }

    /** Whether {@code stack} is one of the pot's plating containers. */
    public boolean isPotContainer(ItemStack stack) {
        return Containers.POT.isContainer(level, stack);
    }

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
        // pots saved before the leftover slot hold 7 slots; deserializeNBT would shrink the handler to that
        CompoundTag saved = compound.getCompound("Inventory");
        if (saved.getInt("Size") < INVENTORY_SIZE) {
            saved = saved.copy();
            saved.putInt("Size", INVENTORY_SIZE);
        }
        inventory.deserializeNBT(saved);
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

    /**
     * Creative Delight QoL-8 (kitchen.md 2.12, TODO cooking/kitchen #40): a break marked this tick by Creative
     * Delight's CookwareContents (the key in the Forge persistent data) keeps every slot but the leftover slot in the
     * item. The key string is the same as CookwareContents.KEEP_KEY.
     */
    private static final String KEEP_KEY = "creativedelight:keep_contents";

    private boolean keepingContents() {
        CompoundTag data = getPersistentData();
        return level != null && data.contains(KEEP_KEY) && data.getLong(KEEP_KEY) == level.getGameTime();
    }

    private boolean holdsKeptSlots() {
        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            if (slot != LEFTOVER_SLOT && !inventory.getStackInSlot(slot).isEmpty()) return true;
        }
        return false;
    }

    /**
     * What a broken or picked pot keeps: the meal, its container and the name; empty when there is no meal. On a
     * marked break (QoL-8) also the ingredients, the container slot, the output and the cook progress.
     */
    public CompoundTag writeMeal(CompoundTag compound) {
        if (keepingContents() && holdsKeptSlots()) {
            ItemStackHandler kept = new ItemStackHandler(INVENTORY_SIZE);
            for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
                if (slot != LEFTOVER_SLOT) kept.setStackInSlot(slot, inventory.getStackInSlot(slot));
            }
            if (customName != null) {
                compound.putString("CustomName", Component.Serializer.toJson(customName));
            }
            compound.put("Container", mealContainerStack.serializeNBT());
            compound.put("Inventory", kept.serializeNBT());
            compound.putInt("CookTime", cookTime);
            compound.putInt("CookTimeTotal", cookTimeTotal);
            return compound;
        }
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
        if (pot.pullForPlating(meal)) {
            changed = true;
        }
        if (!meal.isEmpty()) {
            if (!pot.doesMealHaveContainer(meal)) {
                pot.moveMealToOutput();
                changed = true;
            } else if (!pot.inventory.getStackInSlot(CONTAINER_SLOT).isEmpty() && !pot.holdPlating()) {
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
        for (int slot = 0; slot < COOKING_SLOTS; slot++) {
            handler.setStackInSlot(slot < INPUT_SLOTS ? slot : slot + (FD_INVENTORY_SIZE - COOKING_SLOTS), inventory.getStackInSlot(slot));
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

    /** The recipe's result as this pot serves it (the pack has no cups: always the recipe's own, TODO dishes/meals #53). */
    public static ItemStack servedResult(CookingPotRecipe recipe) {
        return recipe.getResultItem();
    }

    /** The container the served result needs: the recipe's own. */
    public static ItemStack servedContainer(CookingPotRecipe recipe) {
        return recipe.getOutputContainer();
    }

    /** What the recipe's meal is plated with: its container, else its result's crafting remainder (as getContainer). */
    private static ItemStack platedIn(CookingPotRecipe recipe) {
        ItemStack container = servedContainer(recipe);
        return container.isEmpty() ? servedResult(recipe).getCraftingRemainingItem() : container;
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
            // stallWhenFull holds with an empty meal slot too (creativedelight members #39)
            return leftoversFit(recipe);
        }
        if (!ItemStack.isSame(stored, result)) {
            return false;
        }
        int total = stored.getCount() + result.getCount();
        return (total <= inventory.getSlotLimit(MEAL_DISPLAY_SLOT) || total <= result.getMaxStackSize()) && leftoversFit(recipe);
    }

    /**
     * With stallWhenFull, true only when every leftover of one meal fits, by the shared rule ({@link LeftoverPlan},
     * remainder_buffer.md 3.8): into the container slot when it already holds the same item, else the leftover slot,
     * else an empty container slot when the recipe is served in it.
     */
    private boolean leftoversFit(CookingPotRecipe recipe) {
        if (!MDConfig.remainderBuffer() || !MDConfig.stallWhenFull()) {
            return true;
        }
        List<ItemStack> leftovers = new ArrayList<>();
        for (int i = 0; i < INPUT_SLOTS; i++) {
            ItemStack input = inventory.getStackInSlot(i);
            if (!input.isEmpty() && input.hasCraftingRemainingItem()) {
                leftovers.add(input.getCraftingRemainingItem());
            }
        }
        ItemStack served = platedIn(recipe);
        return LeftoverPlan.place(leftovers, leftoverSlots(), leftoverLimits(),
                stack -> !served.isEmpty() && ItemStack.isSame(served, stack)).isEmpty();
    }

    /** Copies of [container slot, leftover slot], for {@link LeftoverPlan}. */
    private ItemStack[] leftoverSlots() {
        return LeftoverPlan.copies(inventory.getStackInSlot(CONTAINER_SLOT), inventory.getStackInSlot(LEFTOVER_SLOT));
    }

    private int[] leftoverLimits() {
        return new int[]{inventory.getSlotLimit(CONTAINER_SLOT), inventory.getSlotLimit(LEFTOVER_SLOT)};
    }

    /**
     * By the shared rule ({@link LeftoverPlan}; config remainderBuffer): into the container slot when it already holds
     * the same item, else the leftover slot, else an empty container slot when the waiting meal is served in it; what
     * does not fit, or all of it with the buffer off, is thrown out of the pot's side. A leftover-slot item the meal can
     * be plated with moves over later ({@link #pullForPlating}).
     */
    private void storeOrThrowLeftover(ItemStack leftover) {
        List<ItemStack> rest = List.of(leftover.copy());
        if (MDConfig.remainderBuffer()) {
            ItemStack[] slots = leftoverSlots();
            rest = LeftoverPlan.place(rest, slots, leftoverLimits(), this::isContainerValid);
            if (!ItemStack.matches(slots[0], inventory.getStackInSlot(CONTAINER_SLOT))) {
                inventory.setStackInSlot(CONTAINER_SLOT, slots[0]);
            }
            if (!ItemStack.matches(slots[1], inventory.getStackInSlot(LEFTOVER_SLOT))) {
                inventory.setStackInSlot(LEFTOVER_SLOT, slots[1]);
            }
        }
        Direction side = getBlockState().getValue(CopperPotBlock.FACING).getCounterClockWise();
        for (ItemStack thrown : rest) {
            ItemUtils.spawnItemEntity(level, thrown,
                    worldPosition.getX() + 0.5 + side.getStepX() * 0.25, worldPosition.getY() + 0.7, worldPosition.getZ() + 0.5 + side.getStepZ() * 0.25,
                    side.getStepX() * 0.08F, 0.25, side.getStepZ() * 0.08F);
        }
    }

    /**
     * A leftover in the leftover slot that plates the waiting meal moves to the container slot after CD
     * {@code [cooking] recycleShowTicks}, as many as the waiting meals need; plating then waits the same ticks
     * (remainder_buffer.md 3.8, shared {@link OverflowPull}). True when it moved something.
     */
    private boolean pullForPlating(ItemStack meal) {
        int mealsWaiting = MDConfig.remainderBuffer() && !meal.isEmpty() && doesMealHaveContainer(meal) ? meal.getCount() : 0;
        int delay = recycleShowTicks();
        boolean moved = overflowPull.tick(new OverflowPull.Slots() {
            @Override
            public ItemStack get(int slot) {
                return inventory.getStackInSlot(slot);
            }

            @Override
            public void set(int slot, ItemStack stack) {
                inventory.setStackInSlot(slot, stack);
            }

            @Override
            public int limit(int slot) {
                return inventory.getSlotLimit(slot);
            }
        }, CONTAINER_SLOT, new int[]{LEFTOVER_SLOT}, mealsWaiting, this::isContainerValid, delay);
        if (moved) {
            plateHold = delay;
        }
        return moved;
    }

    /** true = skip plating this tick (a container pulled from the leftover slot is still on show). */
    private boolean holdPlating() {
        if (plateHold <= 0) {
            return false;
        }
        --plateHold;
        return true;
    }

    /** Creative Delight's {@code [cooking] recycleShowTicks} (default 10), the stations' shared show delay. */
    private static int recycleShowTicks() {
        return Math.max(0, CdSettings.number("cooking.recycleShowTicks", 10));
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
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            ItemStack input = inventory.getStackInSlot(slot);
            if (input.hasCraftingRemainingItem()) {
                storeOrThrowLeftover(input.getCraftingRemainingItem());
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
        boolean keep = keepingContents();
        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            if (keep ? slot == LEFTOVER_SLOT : slot != MEAL_DISPLAY_SLOT) {
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
