package com.sammy.minersdelight.content.block.copper_pot;

import com.mojang.datafixers.util.Pair;
import com.sammy.minersdelight.registry.MDBlocks;
import com.sammy.minersdelight.registry.MDMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMealSlot;

import static com.sammy.minersdelight.content.block.copper_pot.CopperPotBlockEntity.CONTAINER_SLOT;
import static com.sammy.minersdelight.content.block.copper_pot.CopperPotBlockEntity.INPUT_SLOTS;
import static com.sammy.minersdelight.content.block.copper_pot.CopperPotBlockEntity.LEFTOVER_SLOT;
import static com.sammy.minersdelight.content.block.copper_pot.CopperPotBlockEntity.MEAL_DISPLAY_SLOT;
import static com.sammy.minersdelight.content.block.copper_pot.CopperPotBlockEntity.OUTPUT_SLOT;

/**
 * The Copper Pot menu: a 2x2 input grid, meal display, container and output slots laid out for the upstream
 * {@code textures/gui/copper_pot.png}, and FD's cooking recipe book.
 */
public class CopperPotMenu extends RecipeBookMenu<RecipeWrapper> {
    private static final ResourceLocation EMPTY_CONTAINER_SLOT = new ResourceLocation("farmersdelight", "item/empty_container_slot_bowl");
    /** Port: the leftover slot's position, right of the output (its frame is drawn by the screen). */
    public static final int LEFTOVER_X = 139, LEFTOVER_Y = 56;
    public static final int PLAYER_INVENTORY_START = LEFTOVER_SLOT + 1;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 36;

    public final CopperPotBlockEntity blockEntity;
    public final ItemStackHandler inventory;
    private final ContainerData cookingData;
    private final ContainerLevelAccess access;
    private final Level level;

    public CopperPotMenu(int id, Inventory playerInventory, CopperPotBlockEntity blockEntity, ContainerData cookingData) {
        super(MDMenuTypes.COPPER_POT.get(), id);
        this.blockEntity = blockEntity;
        this.inventory = blockEntity.getInventory();
        this.cookingData = cookingData;
        this.level = playerInventory.player.level;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 2; column++) {
                addSlot(new SlotItemHandler(inventory, row * 2 + column, 40 + column * 18, 18 + row * 18));
            }
        }
        addSlot(new CookingPotMealSlot(inventory, MEAL_DISPLAY_SLOT, 115, 28));
        addSlot(new SlotItemHandler(inventory, CONTAINER_SLOT, 83, 56) {
            @Override
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_CONTAINER_SLOT);
            }
        });
        addSlot(new CopperPotResultSlot(playerInventory.player, blockEntity, inventory, OUTPUT_SLOT, 115, 56));
        addSlot(new SlotItemHandler(inventory, LEFTOVER_SLOT, LEFTOVER_X, LEFTOVER_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, 9 + row * 9 + column, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }
        addDataSlots(cookingData);
    }

    /** Client side, from the open-screen packet. */
    public CopperPotMenu(int id, Inventory playerInventory, FriendlyByteBuf data) {
        this(id, playerInventory, blockEntityAt(playerInventory, data), new SimpleContainerData(2));
    }

    private static CopperPotBlockEntity blockEntityAt(Inventory playerInventory, FriendlyByteBuf data) {
        if (playerInventory.player.level.getBlockEntity(data.readBlockPos()) instanceof CopperPotBlockEntity pot) {
            return pot;
        }
        throw new IllegalStateException("No copper pot at the menu's position");
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, MDBlocks.COPPER_POT.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack moving = slot.getItem();
        ItemStack original = moving.copy();
        if (index == OUTPUT_SLOT || index == LEFTOVER_SLOT) {
            if (!moveItemStackTo(moving, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (index < OUTPUT_SLOT) {
            if (!moveItemStackTo(moving, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            // any of the pot's containers to the container slot only, the rest to the inputs only (remainder_buffer.md 3.8)
            boolean container = blockEntity.isPotContainer(moving) || blockEntity.isContainerValid(moving);
            if (!(container ? moveItemStackTo(moving, CONTAINER_SLOT, CONTAINER_SLOT + 1, false)
                    : moveItemStackTo(moving, 0, MEAL_DISPLAY_SLOT, false))) {
                return ItemStack.EMPTY;
            }
        }
        if (moving.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (moving.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, moving);
        return original;
    }

    /** Arrow width in pixels (0 to 29). */
    public int getCookProgressionScaled() {
        int time = cookingData.get(0);
        int total = cookingData.get(1);
        return total != 0 && time != 0 ? time * 29 / total : 0;
    }

    public boolean isHeated() {
        return blockEntity.isHeated();
    }

    @Override
    public void fillCraftSlotsStackedContents(StackedContents contents) {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            contents.accountSimpleStack(inventory.getStackInSlot(slot));
        }
    }

    @Override
    public void clearCraftingContent() {
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            inventory.setStackInSlot(slot, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean recipeMatches(Recipe<? super RecipeWrapper> recipe) {
        return recipe.matches(blockEntity.createFakeRecipeWrapper(), level);
    }

    @Override
    public int getResultSlotIndex() {
        return CONTAINER_SLOT;
    }

    @Override
    public int getGridWidth() {
        return 2;
    }

    @Override
    public int getGridHeight() {
        return 2;
    }

    @Override
    public int getSize() {
        return CONTAINER_SLOT;
    }

    @Override
    public RecipeBookType getRecipeBookType() {
        return FarmersDelight.RECIPE_TYPE_COOKING;
    }

    @Override
    public boolean shouldMoveToInventory(int slot) {
        return slot < getGridWidth() * getGridHeight();
    }
}
