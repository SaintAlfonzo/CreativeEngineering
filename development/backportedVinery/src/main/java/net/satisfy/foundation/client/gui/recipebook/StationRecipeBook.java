package net.satisfy.foundation.client.gui.recipebook;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.satisfy.foundation.network.FoundationNetwork;
import net.satisfy.foundation.recipe.book.PlaceRecipePacket;
import net.satisfy.foundation.recipe.book.RecipePlacer;
import net.satisfy.foundation.recipe.book.StationRecipeBookMenu;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Vanilla styled recipe book panel for station menus implementing {@link StationRecipeBookMenu}.
 * Uses the vanilla recipe book textures. {@link StationRecipeBookScreen} wires it up,
 * custom screens can forward the calls themselves.
 * <p>
 * Port: 1.19.2 has no GUI sprite atlas, so every element is blitted from {@code textures/gui/recipe_book.png} at the
 * offsets 1.19.2's own recipe book uses (RecipeBookComponent, RecipeBookPage, RecipeButton, RecipeBookTabButton).
 */
public class StationRecipeBook extends GuiComponent {
    public static final int WIDTH = 147;
    public static final int HEIGHT = 166;
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/recipe_book.png");
    private static final Component SEARCH_HINT = Component.translatable("gui.recipebook.search_hint").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY);
    private static final Component ONLY_CRAFTABLE = Component.translatable("gui.recipebook.toggleRecipes.craftable");
    private static final Component INGREDIENTS = Component.translatable("gui.foundation.recipe_book.ingredients");
    private static final Component ALL_RECIPES = Component.translatable("gui.recipebook.toggleRecipes.all");
    private static final int COLUMNS = 5;
    private static final int ROWS = 4;
    private static final int BUTTON_SIZE = 25;

    // static so it stays open/filtered between screens, like vanilla does per session
    private static boolean open;
    private static boolean onlyCraftable;

    private Minecraft minecraft;
    private AbstractContainerMenu menu;
    private StationRecipeBookMenu book;
    private EditBox searchBox;
    private int x;
    private int y;
    private int page;
    private int tab;
    private boolean widthTooNarrow;
    private final List<Entry> entries = new ArrayList<>();
    private int lastInventoryChange = -1;
    private int lastMenuState = -1;
    private Recipe<?> ghost;
    private float ghostTime;

    /** placeable: items are there, craftable: placeable and the station extras are met. */
    private record Entry(Recipe<?> recipe, ItemStack result, boolean placeable, boolean craftable) {
    }

    public <M extends AbstractContainerMenu & StationRecipeBookMenu> void init(int screenWidth, int screenHeight, Minecraft minecraft, M menu) {
        this.minecraft = minecraft;
        this.menu = menu;
        this.book = menu;
        this.widthTooNarrow = screenWidth < 379;
        this.x = (screenWidth - 147) / 2 - (widthTooNarrow ? 0 : 86);
        this.y = (screenHeight - 166) / 2;
        String search = searchBox != null ? searchBox.getValue() : "";
        this.searchBox = new EditBox(minecraft.font, x + 25, y + 14, 80, 9 + 5, Component.translatable("itemGroup.search"));
        this.searchBox.setMaxLength(50);
        this.searchBox.setBordered(false);
        this.searchBox.setVisible(true);
        this.searchBox.setTextColor(0xFFFFFF);
        this.searchBox.setValue(search);
        refresh();
    }

    public boolean isOpen() {
        return open;
    }

    public void toggle() {
        open = !open;
        if (!open) {
            searchBox.setFocus(false);
        }
    }

    /** New leftPos of the container screen, shifted right while the book is open. */
    public int updateScreenPosition(int screenWidth, int imageWidth) {
        return open && !widthTooNarrow ? 177 + (screenWidth - imageWidth - 200) / 2 : (screenWidth - imageWidth) / 2;
    }

    /** Call every tick, rebuilds the list when the inventory or the inputs changed. */
    public void tick() {
        searchBox.tick();
        int inventoryChange = minecraft.player.getInventory().getTimesChanged();
        if (inventoryChange != lastInventoryChange || menu.getStateId() != lastMenuState) {
            refresh();
        }
    }

    private void refresh() {
        lastInventoryChange = minecraft.player.getInventory().getTimesChanged();
        lastMenuState = menu.getStateId();
        entries.clear();
        String query = searchBox.getValue().toLowerCase(Locale.ROOT);
        for (Recipe<?> recipe : recipes()) {
            ItemStack result = recipe.getResultItem();
            if (!query.isEmpty() && !result.getHoverName().getString().toLowerCase(Locale.ROOT).contains(query)) {
                continue;
            }
            boolean placeable = RecipePlacer.canCraft(minecraft.player.getInventory(), menu, book.recipeBookPlacementSlots(recipe), book.recipeBookPlacementIngredients(recipe));
            boolean craftable = placeable && book.recipeBookExtrasMet(recipe);
            if (onlyCraftable && !craftable) {
                continue;
            }
            entries.add(new Entry(recipe, result, placeable, craftable));
        }
        entries.sort(Comparator.comparing((Entry entry) -> !entry.craftable).thenComparing(entry -> entry.result.getHoverName().getString()));
        page = Mth.clamp(page, 0, pageCount() - 1);
    }

    private List<Recipe<?>> recipes() {
        return recipesOf(book.recipeBookTypes().get(tab));
    }

    private List<Recipe<?>> recipesOf(RecipeType<?> type) {
        return minecraft.level.getRecipeManager().getRecipes().stream().filter(recipe -> recipe.getType() == type).toList();
    }

    private int[] slotsFor(Recipe<?> recipe) {
        return book.recipeBookInputSlots(recipe.getType());
    }

    /** Tab icon: result of the first recipe of that type. */
    private ItemStack tabIcon(RecipeType<?> type) {
        List<Recipe<?>> recipes = recipesOf(type);
        return recipes.isEmpty() ? ItemStack.EMPTY : recipes.get(0).getResultItem();
    }

    private boolean hasTabs() {
        return book.recipeBookTypes().size() > 1;
    }

    private int tabY(int index) {
        return y + 3 + 27 * index;
    }

    private int pageCount() {
        return Math.max(1, (entries.size() + COLUMNS * ROWS - 1) / (COLUMNS * ROWS));
    }

    private ItemRenderer items() {
        return minecraft.getItemRenderer();
    }

    private void bindTexture() {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
    }

    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        if (!open) {
            return;
        }
        pose.pushPose();
        pose.translate(0, 0, 100);
        if (hasTabs()) {
            List<RecipeType<?>> types = book.recipeBookTypes();
            for (int i = 0; i < types.size(); i++) {
                boolean selected = i == tab;
                int tx = selected ? x - 32 : x - 30;
                bindTexture();
                RenderSystem.disableDepthTest();
                blit(pose, tx, tabY(i), selected ? 188 : 153, 2, 35, 27);
                RenderSystem.enableDepthTest();
                items().renderAndDecorateFakeItem(tabIcon(types.get(i)), tx + 9 + (selected ? -2 : 0), tabY(i) + 5);
            }
        }
        bindTexture();
        blit(pose, x, y, 1, 1, WIDTH, HEIGHT);
        if (!searchBox.isFocused() && searchBox.getValue().isEmpty()) {
            drawString(pose, minecraft.font, SEARCH_HINT, x + 25, y + 14, -1);
        } else {
            searchBox.render(pose, mouseX, mouseY, partialTick);
        }

        boolean filterHovered = isOver(mouseX, mouseY, x + 110, y + 12, 26, 16);
        bindTexture();
        blit(pose, x + 110, y + 12, onlyCraftable ? 180 : 152, filterHovered ? 59 : 41, 26, 16);

        int start = page * COLUMNS * ROWS;
        for (int i = 0; i < COLUMNS * ROWS && start + i < entries.size(); i++) {
            Entry entry = entries.get(start + i);
            int bx = buttonX(i);
            int by = buttonY(i);
            bindTexture();
            blit(pose, bx, by, entry.craftable ? 29 : 54, 206, BUTTON_SIZE, BUTTON_SIZE);
            items().renderAndDecorateFakeItem(entry.result, bx + 4, by + 4);
            items().renderGuiItemDecorations(minecraft.font, entry.result, bx + 4, by + 4);
        }

        if (pageCount() > 1) {
            Component text = Component.literal((page + 1) + "/" + pageCount());
            int textWidth = minecraft.font.width(text);
            minecraft.font.draw(pose, text, x + 73 - textWidth / 2f, y + 141, -1);
            if (page > 0) {
                bindTexture();
                blit(pose, x + 38, y + 137, 14, isOver(mouseX, mouseY, x + 38, y + 137, 12, 17) ? 226 : 208, 12, 17);
            }
            if (page < pageCount() - 1) {
                bindTexture();
                blit(pose, x + 93, y + 137, 1, isOver(mouseX, mouseY, x + 93, y + 137, 12, 17) ? 226 : 208, 12, 17);
            }
        }
        pose.popPose();
    }

    public void renderTooltip(Screen screen, PoseStack pose, int mouseX, int mouseY) {
        if (!open) {
            return;
        }
        if (isOver(mouseX, mouseY, x + 110, y + 12, 26, 16)) {
            screen.renderTooltip(pose, onlyCraftable ? ONLY_CRAFTABLE : ALL_RECIPES, mouseX, mouseY);
            return;
        }
        if (hasTabs()) {
            List<RecipeType<?>> types = book.recipeBookTypes();
            for (int i = 0; i < types.size(); i++) {
                Component name = book.recipeBookTabName(types.get(i));
                if (name != null && isOver(mouseX, mouseY, x - 30, tabY(i), 35, 27)) {
                    screen.renderTooltip(pose, name, mouseX, mouseY);
                    return;
                }
            }
        }
        int hovered = hoveredButton(mouseX, mouseY);
        if (hovered >= 0) {
            Entry entry = entries.get(hovered);
            List<Component> lines = new ArrayList<>(screen.getTooltipFromItem(entry.result));
            lines.add(INGREDIENTS.copy().withStyle(ChatFormatting.GRAY));
            for (Map.Entry<String, Integer> ingredient : countIngredients(entry.recipe).entrySet()) {
                lines.add(Component.literal(" " + ingredient.getValue() + "× " + ingredient.getKey()).withStyle(ChatFormatting.DARK_GRAY));
            }
            book.appendRecipeBookTooltip(entry.recipe, lines);
            // Port: Forge's overload with the result stack, so AppleSkin's food bars show on recipe buttons
            screen.renderComponentTooltip(pose, lines, mouseX, mouseY, entry.result);
        }
    }

    /** Ingredient name -> count, the name cycles through the options like the ghost does. */
    private Map<String, Integer> countIngredients(Recipe<?> recipe) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Ingredient ingredient : book.recipeBookPlacementIngredients(recipe)) {
            ItemStack[] stacks = ingredient.getItems();
            if (stacks.length == 0) {
                continue;
            }
            ItemStack shown = stacks[(int) (minecraft.level.getGameTime() / 20 % stacks.length)];
            counts.merge(shown.getHoverName().getString(), 1, Integer::sum);
        }
        return counts;
    }

    /** Draws the missing ingredients of the last clicked recipe into the input slots. */
    public void renderGhost(PoseStack pose, int leftPos, int topPos, float partialTick) {
        if (ghost == null) {
            return;
        }
        if (!Screen.hasControlDown()) {
            ghostTime += partialTick;
        }
        List<Ingredient> ingredients = book.recipeBookPlacementIngredients(ghost);
        int[] slots = book.recipeBookPlacementSlots(ghost);
        for (int i = 0; i < ingredients.size() && i < slots.length; i++) {
            renderGhostSlot(pose, leftPos, topPos, slots[i], ingredients.get(i));
        }
        book.recipeBookExtraGhosts(ghost).forEach((slot, ingredient) -> renderGhostSlot(pose, leftPos, topPos, slot, ingredient));
        int resultSlot = book.recipeBookResultSlot(ghost.getType());
        if (resultSlot >= 0) {
            ItemStack result = ghost.getResultItem();
            renderGhostSlot(pose, leftPos, topPos, resultSlot, Ingredient.of(result));
            Slot slot = menu.getSlot(resultSlot);
            if (!slot.hasItem()) {
                items().renderGuiItemDecorations(minecraft.font, result, leftPos + slot.x, topPos + slot.y);
            }
        }
    }

    private void renderGhostSlot(PoseStack pose, int leftPos, int topPos, int slotIndex, Ingredient ingredient) {
        ItemStack[] stacks = ingredient.getItems();
        Slot slot = menu.getSlot(slotIndex);
        if (stacks.length == 0 || slot.hasItem()) {
            return;
        }
        ItemStack stack = stacks[Mth.floor(ghostTime / 30.0F) % stacks.length];
        int sx = leftPos + slot.x;
        int sy = topPos + slot.y;
        fill(pose, sx, sy, sx + 16, sy + 16, 0x30FF0000);
        items().renderAndDecorateFakeItem(stack, sx, sy);
        RenderSystem.depthFunc(516);
        fill(pose, sx, sy, sx + 16, sy + 16, 0x30FFFFFF);
        RenderSystem.depthFunc(515);
    }

    /** Recipe shown as ghost right now, null if none. */
    public @Nullable Recipe<?> getGhost() {
        return ghost;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!open || minecraft.player.isSpectator()) {
            return false;
        }
        if (searchBox.mouseClicked(mouseX, mouseY, button)) {
            searchBox.setFocus(true);
            return true;
        }
        searchBox.setFocus(false);
        if (hasTabs()) {
            for (int i = 0; i < book.recipeBookTypes().size(); i++) {
                if (isOver(mouseX, mouseY, x - 30, tabY(i), 35, 27)) {
                    if (tab != i) {
                        tab = i;
                        page = 0;
                        ghost = null;
                        refresh();
                    }
                    return true;
                }
            }
        }
        if (isOver(mouseX, mouseY, x + 110, y + 12, 26, 16)) {
            onlyCraftable = !onlyCraftable;
            page = 0;
            refresh();
            return true;
        }
        if (pageCount() > 1 && page > 0 && isOver(mouseX, mouseY, x + 38, y + 137, 12, 17)) {
            page--;
            return true;
        }
        if (pageCount() > 1 && page < pageCount() - 1 && isOver(mouseX, mouseY, x + 93, y + 137, 12, 17)) {
            page++;
            return true;
        }
        int hovered = hoveredButton(mouseX, mouseY);
        if (hovered >= 0) {
            Entry entry = entries.get(hovered);
            if (entry.placeable) {
                ghost = null;
                FoundationNetwork.CHANNEL.sendToServer(new PlaceRecipePacket(menu.containerId, entry.recipe.getId(), Screen.hasShiftDown()));
            } else if (inputsEmpty(slotsFor(entry.recipe))) {
                ghost = entry.recipe;
            }
            return true;
        }
        return isMouseOver(mouseX, mouseY);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!open || !searchBox.isFocused()) {
            return false;
        }
        if (keyCode == 256) {
            return false;
        }
        String before = searchBox.getValue();
        searchBox.keyPressed(keyCode, scanCode, modifiers);
        if (!before.equals(searchBox.getValue())) {
            page = 0;
            refresh();
        }
        // swallow everything else so inventory keys don't close the screen while typing
        return true;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (!open || !searchBox.isFocused()) {
            return false;
        }
        if (searchBox.charTyped(codePoint, modifiers)) {
            page = 0;
            refresh();
        }
        return true;
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        if (open && hasTabs() && isOver(mouseX, mouseY, x - 32, tabY(0), 35, 27 * book.recipeBookTypes().size())) {
            return true;
        }
        return open && isOver(mouseX, mouseY, x, y, WIDTH, HEIGHT);
    }

    /** Drops the ghost, call when the player clicks a slot. */
    public void clearGhost() {
        ghost = null;
    }

    private boolean inputsEmpty(int[] slots) {
        for (int index : slots) {
            if (menu.getSlot(index).hasItem()) {
                return false;
            }
        }
        return true;
    }

    private int hoveredButton(double mouseX, double mouseY) {
        int start = page * COLUMNS * ROWS;
        for (int i = 0; i < COLUMNS * ROWS && start + i < entries.size(); i++) {
            if (isOver(mouseX, mouseY, buttonX(i), buttonY(i), BUTTON_SIZE, BUTTON_SIZE)) {
                return start + i;
            }
        }
        return -1;
    }

    private int buttonX(int index) {
        return x + 11 + BUTTON_SIZE * (index % COLUMNS);
    }

    private int buttonY(int index) {
        return y + 31 + BUTTON_SIZE * (index / COLUMNS);
    }

    private static boolean isOver(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
