package net.satisfy.vinery.core.block.entity;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.vinery.core.menu.FermentationBarrelMenu;
import net.satisfy.vinery.core.recipe.FermentationBarrelRecipe;
import net.satisfy.vinery.core.recipe.input.FermentationBarrelRecipeInput;
import net.satisfy.vinery.core.registry.EntityTypeRegistry;
import net.satisfy.vinery.core.registry.ObjectRegistry;
import net.satisfy.vinery.core.registry.RecipeTypeRegistry;
import net.satisfy.vinery.core.wine.JuiceUtil;
import net.satisfy.vinery.core.wine.WineYears;
import net.satisfy.foundation.util.ImplementedInventory;
import net.satisfy.vinery.platform.PlatformHelper;
import net.satisfy.foundation.particle.ColorParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.Vec3;
import net.satisfy.foundation.registry.FoundationParticles;
import net.satisfy.vinery.core.block.FermentationBarrelBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class FermentationBarrelBlockEntity extends BlockEntity implements ImplementedInventory, MenuProvider {
    private static final int INVENTORY_SIZE = 6;
    public static final int GRAPEJUICE_INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT_GENERAL = 5;
    public static final int WINE_BOTTLE_SLOT = 4;

    private NonNullList<ItemStack> inventory;
    private int fermentationTime = 0;
    private int fluidLevel = 0;
    private String juiceType = "";

    private boolean recipeDirty = true;
    private @Nullable FermentationBarrelRecipe cachedRecipe;

    private final ContainerData propertyDelegate = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> FermentationBarrelBlockEntity.this.fermentationTime;
                case 1 -> PlatformHelper.getTotalFermentationTime();
                case 2 -> FermentationBarrelBlockEntity.this.fluidLevel;
                case 3 -> JuiceUtil.typeId(juiceType);
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> FermentationBarrelBlockEntity.this.fermentationTime = value;
                case 1 -> FermentationBarrelBlockEntity.this.updateTotalFermentationTime();
                case 2 -> FermentationBarrelBlockEntity.this.setFluidLevel(value);
                case 3 -> FermentationBarrelBlockEntity.this.juiceType = JuiceUtil.typeFromId(value);
                default -> {}
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public FermentationBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(EntityTypeRegistry.FERMENTATION_BARREL_ENTITY.get(), pos, state);
        this.inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
    }

    public void updateTotalFermentationTime() {
        setChanged();
    }

    public int getFluidLevel() {
        return fluidLevel;
    }

    public void setFluidLevel(int fluidLevel) {
        this.fluidLevel = fluidLevel;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public String getJuiceType() {
        return juiceType;
    }

    /** Port (WTHIT lines): ticks into the running fermentation, 0 when idle. */
    public int getFermentationTime() {
        return fermentationTime;
    }

    /** Port (WTHIT lines): the recipe the barrel is working on, as of its last check (server side). */
    public @Nullable FermentationBarrelRecipe getCurrentRecipe() {
        return cachedRecipe;
    }

    public void setJuiceType(String juiceType) {
        this.juiceType = juiceType;
        setChanged();
    }

    @Override
    public void setChanged() {
        recipeDirty = true;
        super.setChanged();
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.inventory = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(nbt, this.inventory);
        this.fermentationTime = nbt.getInt("FermentationTime");
        this.fluidLevel = nbt.getInt("FluidLevel");
        this.juiceType = nbt.getString("JuiceType");
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        ContainerHelper.saveAllItems(nbt, this.inventory);
        nbt.putInt("FermentationTime", this.fermentationTime);
        nbt.putInt("FluidLevel", this.fluidLevel);
        nbt.putString("JuiceType", this.juiceType);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /** Lets the running fermentation finish on the next tick, false if nothing is fermenting. */
    public boolean finishFermentation() {
        if (level == null || findRecipe(level) == null) {
            return false;
        }
        fermentationTime = Math.max(fermentationTime, PlatformHelper.getTotalFermentationTime() - 1);
        recipeDirty = true;
        return true;
    }

    public static void tick(Level level, BlockPos pos, FermentationBarrelBlockEntity blockEntity) {
        if (level.isClientSide) return;

        if (blockEntity.fluidLevel == 0 && !blockEntity.juiceType.isEmpty()) {
            blockEntity.setJuiceType("");
        }

        RegistryAccess access = level.registryAccess();

        if (blockEntity.recipeDirty || level.getGameTime() % 20 == 0) {
            blockEntity.recipeDirty = false;
            blockEntity.cachedRecipe = blockEntity.findRecipe(level);
        }

        if (blockEntity.cachedRecipe == null) {
            blockEntity.fermentationTime = 0;
        } else {
            FermentationBarrelRecipe recipe = blockEntity.cachedRecipe;

            if (blockEntity.canCraft(recipe, access)) {
                blockEntity.fermentationTime++;

                if (blockEntity.fermentationTime >= PlatformHelper.getTotalFermentationTime()) {
                    blockEntity.fermentationTime = 0;
                    blockEntity.craft(recipe, access);
                }
            } else {
                blockEntity.fermentationTime = 0;
            }
        }
        ItemStack stack = blockEntity.getItem(GRAPEJUICE_INPUT_SLOT);
        if (JuiceUtil.isJuice(stack)) {
            String newJuiceType = JuiceUtil.getJuiceType(stack);

            if (blockEntity.fluidLevel == 0 || blockEntity.juiceType.equals(newJuiceType)) {
                if (!blockEntity.juiceType.equals(newJuiceType)) {
                    blockEntity.setJuiceType(newJuiceType);
                }
                int currentLevel = blockEntity.getFluidLevel();
                int maxFluidLevel = PlatformHelper.getMaxFluidLevel();
                int juiceCount = stack.getCount();
                int perJuice = Math.max(1, PlatformHelper.getMaxFluidIncrease());
                int actualJuicesConsumed = Math.min(Math.min(juiceCount, 4), Math.max(0, maxFluidLevel - currentLevel) / perJuice);
                int newFluidLevel = currentLevel + actualJuicesConsumed * perJuice;

                if (actualJuicesConsumed > 0) {
                    blockEntity.setFluidLevel(newFluidLevel);

                    stack.shrink(actualJuicesConsumed);
                    if (stack.isEmpty()) {
                        blockEntity.setItem(GRAPEJUICE_INPUT_SLOT, ItemStack.EMPTY);
                    } else {
                        blockEntity.setItem(GRAPEJUICE_INPUT_SLOT, stack);
                    }

                    ItemStack wineBottleStack = new ItemStack(ObjectRegistry.WINE_BOTTLE.get(), actualJuicesConsumed);

                    ItemStack existingOutput = blockEntity.getItem(WINE_BOTTLE_SLOT);
                    if (existingOutput.isEmpty()) {
                        blockEntity.setItem(WINE_BOTTLE_SLOT, wineBottleStack);
                    } else if (existingOutput.is(wineBottleStack.getItem()) && existingOutput.getCount() + wineBottleStack.getCount() <= existingOutput.getMaxStackSize()) {
                        existingOutput.grow(wineBottleStack.getCount());
                        blockEntity.setItem(WINE_BOTTLE_SLOT, existingOutput);
                    } else {
                        Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, wineBottleStack);
                    }
                }
            }
        }
    }

    private @Nullable FermentationBarrelRecipe findRecipe(Level level) {
        if (areIngredientsEmpty()) {
            return null;
        }
        List<ItemStack> inputs = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            ItemStack stack = getItem(i);
            if (!stack.isEmpty() && isIngredient(stack)) {
                inputs.add(stack);
            }
        }
        FermentationBarrelRecipeInput input = new FermentationBarrelRecipeInput(inputs, getItem(WINE_BOTTLE_SLOT), new FermentationBarrelRecipeInput.JuiceData(juiceType, fluidLevel));
        return level.getRecipeManager().getRecipeFor(RecipeTypeRegistry.FERMENTATION_BARREL_RECIPE_TYPE.get(), input, level).orElse(null);
    }

    private ItemStack result(FermentationBarrelRecipe recipe, RegistryAccess access) {
        ItemStack output = recipe.getResultItem().copy();
        if (!output.isEmpty()) {
            WineYears.setWineYear(output, this.level);
        }
        return output;
    }

    private static boolean fits(ItemStack existing, ItemStack output) {
        return existing.isEmpty() || ItemStack.isSameItemSameTags(existing, output) && existing.getCount() + output.getCount() <= existing.getMaxStackSize();
    }

    private boolean canCraft(FermentationBarrelRecipe recipe, RegistryAccess access) {
        if (recipe == null || recipe.getResultItem().isEmpty()) {
            return false;
        } else if (areIngredientsEmpty()) {
            return false;
        } else if (this.fluidLevel < recipe.getJuiceData().amount()) {
            return false;
        } else if (!this.juiceType.equals(recipe.getJuiceData().type())) {
            return false;
        } else {
            if (recipe.isWineBottleRequired()) {
                ItemStack wineBottle = this.getItem(WINE_BOTTLE_SLOT);
                if (wineBottle.isEmpty() || !wineBottle.is(ObjectRegistry.WINE_BOTTLE.get())) {
                    return false;
                }
            }

            ItemStack recipeOutput = result(recipe, access);
            return fits(getItem(OUTPUT_SLOT_GENERAL), recipeOutput);
        }
    }

    private boolean areIngredientsEmpty() {
        for (int i = 1; i < 4; i++) {
            if (!this.getItem(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void craft(FermentationBarrelRecipe recipe, RegistryAccess access) {
        if (!canCraft(recipe, access)) {
            return;
        }

        ItemStack recipeOutput = result(recipe, access);

        ItemStack existingOutput = this.getItem(OUTPUT_SLOT_GENERAL);
        if (existingOutput.isEmpty()) {
            this.setItem(OUTPUT_SLOT_GENERAL, recipeOutput);
        } else {
            existingOutput.grow(recipeOutput.getCount());
            this.setItem(OUTPUT_SLOT_GENERAL, existingOutput);
        }

        if (recipe.isWineBottleRequired()) {
            ItemStack wineBottle = this.getItem(WINE_BOTTLE_SLOT);
            if (!wineBottle.isEmpty() && wineBottle.getCount() > 0) {
                wineBottle.shrink(1);
                this.setItem(WINE_BOTTLE_SLOT, wineBottle);
            }
        }

        if (this.level instanceof ServerLevel serverLevel && this.getBlockState().getBlock() instanceof FermentationBarrelBlock) {
            Vec3 tap = FermentationBarrelBlock.tapPosition(this.getBlockState(), this.worldPosition);
            ColorParticleOption drip = ColorParticleOption.create(FoundationParticles.COLORED_DRIP.get(), (0xFF000000 | (JuiceUtil.color(this.juiceType))));
            serverLevel.sendParticles(drip, tap.x, tap.y, tap.z, 14, 0.03, 0.02, 0.03, 0.1);
        }

        int newFluidLevel = this.fluidLevel - recipe.getJuiceData().amount();
        this.setFluidLevel(newFluidLevel);

        for (Ingredient ingredient : recipe.getIngredients()) {
            for (int i = 1; i < 4; i++) {
                ItemStack slotStack = this.getItem(i);
                if (ingredient.test(slotStack)) {
                    slotStack.shrink(1);
                    if (slotStack.isEmpty()) {
                        this.setItem(i, ItemStack.EMPTY);
                    } else {
                        this.setItem(i, slotStack);
                    }
                    break;
                }
            }
        }

        setChanged();
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        final ItemStack stackInSlot = this.inventory.get(slot);
        boolean sameItem = !stack.isEmpty() && ItemStack.matches(stack, stackInSlot);

        this.inventory.set(slot, stack);
        this.recipeDirty = true;

        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }

        if (!sameItem && isIngredientSlot(slot)) {
            if (areIngredientsEmpty()) {
                this.fermentationTime = 0;
                setChanged();
            }
        }
    }

    private boolean isIngredientSlot(int slot) {
        return slot >= 1 && slot <= 3;
    }

    @Override
    public boolean stillValid(Player player) {
        assert this.level != null;
        return this.level.getBlockEntity(this.worldPosition) == this &&
                player.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable(this.getBlockState().getBlock().getDescriptionId());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new FermentationBarrelMenu(syncId, inv, this, this.propertyDelegate);
    }

    @Override
    public int getContainerSize() {
        return inventory.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.inventory) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @NotNull ItemStack getItem(int index) {
        return this.inventory.get(index);
    }

    @Override
    public @NotNull ItemStack removeItem(int index, int count) {
        return ContainerHelper.removeItem(this.inventory, index, count);
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int index) {
        return ContainerHelper.takeItem(this.inventory, index);
    }

    @Override
    public void clearContent() {
        this.inventory.clear();
    }

    private boolean isIngredient(ItemStack stack) {
        if (level == null) return false;
        return level.getRecipeManager()
                .getAllRecipesFor(RecipeTypeRegistry.FERMENTATION_BARREL_RECIPE_TYPE.get())
                .stream()
                .anyMatch(recipe -> recipe.getIngredients().stream().anyMatch(ingredient -> ingredient.test(stack)));
    }

    @Override
    public int @NotNull [] getSlotsForFace(Direction side) {
        if (side == Direction.UP) {
            return new int[]{GRAPEJUICE_INPUT_SLOT, WINE_BOTTLE_SLOT};
        } else if (side == Direction.DOWN) {
            return new int[]{OUTPUT_SLOT_GENERAL};
        } else if (side.getAxis().isHorizontal()) {
            return new int[]{OUTPUT_SLOT_GENERAL, WINE_BOTTLE_SLOT, 1, 2, 3};
        }
        return new int[]{};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        if (direction == Direction.UP) {
            if (index == GRAPEJUICE_INPUT_SLOT && JuiceUtil.isJuice(stack)) {
                return hasSpace(index, stack) && juiceKeepsWorking(stack);
            } else if (index == WINE_BOTTLE_SLOT && stack.is(ObjectRegistry.WINE_BOTTLE.get())) {
                return hasSpace(index, stack);
            }
        } else {
            assert direction != null;
            if (direction.getAxis().isHorizontal()) {
                if ((index >= 1 && index <= 3) && isIngredient(stack)) {
                    return hasSpace(index, stack) && ingredientKeepsWorking(index, stack);
                } else if (index == WINE_BOTTLE_SLOT && stack.is(ObjectRegistry.WINE_BOTTLE.get())) {
                    return hasSpace(index, stack);
                }
            }
        }
        return false;
    }

    /**
     * Port (alcohol/vineryport #7, hopper jams): a recipe needs exactly its own ingredients, one stack each, so a hopper
     * that spills a second stack of the same item into the next slot, or mixes two recipes, used to stop the cask for
     * good (only the output can be pulled out). Automation now adds an ingredient only to the slot that already holds
     * it, or a new one only when some recipe for the cask's juice uses it together with everything already in, and
     * makes what is already in the output.
     */
    private boolean ingredientKeepsWorking(int index, ItemStack stack) {
        List<ItemStack> held = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            ItemStack slot = getItem(i);
            if (slot.isEmpty()) continue;
            if (slot.is(stack.getItem())) return i == index;
            held.add(slot);
        }
        held.add(stack);
        return someRecipeUses(held, fluidLevel > 0 ? juiceType : null);
    }

    /** Juice from a hopper: the cask's own type (or any while empty), and only one some recipe can use with the ingredients in. */
    private boolean juiceKeepsWorking(ItemStack juice) {
        String type = JuiceUtil.getJuiceType(juice);
        if (fluidLevel > 0 && !juiceType.equals(type)) return false;
        List<ItemStack> held = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            if (!getItem(i).isEmpty()) held.add(getItem(i));
        }
        return held.isEmpty() || someRecipeUses(held, type);
    }

    /** True when one recipe (juice {@code type}, or any when null) takes every stack in {@code held}, each by its own ingredient. */
    private boolean someRecipeUses(List<ItemStack> held, @Nullable String type) {
        if (level == null) return false;
        ItemStack output = getItem(OUTPUT_SLOT_GENERAL);
        return level.getRecipeManager().getAllRecipesFor(RecipeTypeRegistry.FERMENTATION_BARREL_RECIPE_TYPE.get()).stream()
                .filter(recipe -> type == null || recipe.getJuiceData().amount() <= 0 || type.equals(recipe.getJuiceData().type()))
                .filter(recipe -> output.isEmpty() || output.is(recipe.getResultItem().getItem()))
                .anyMatch(recipe -> assign(held, 0, recipe.getIngredients(), new boolean[recipe.getIngredients().size()]));
    }

    private static boolean assign(List<ItemStack> held, int next, List<Ingredient> ingredients, boolean[] used) {
        if (next == held.size()) return true;
        for (int i = 0; i < ingredients.size(); i++) {
            if (!used[i] && ingredients.get(i).test(held.get(next))) {
                used[i] = true;
                if (assign(held, next + 1, ingredients, used)) return true;
                used[i] = false;
            }
        }
        return false;
    }

    private boolean hasSpace(int index, ItemStack stack) {
        ItemStack slotStack = getItem(index);
        if (slotStack.isEmpty()) {
            return true;
        }
        if (ItemStack.isSameItemSameTags(slotStack, stack)) {
            return slotStack.getCount() + stack.getCount() <= slotStack.getMaxStackSize();
        }
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return (direction == Direction.DOWN || direction.getAxis().isHorizontal()) && index == OUTPUT_SLOT_GENERAL;
    }
}
