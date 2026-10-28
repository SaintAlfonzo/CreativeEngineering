package com.SaintAlfonzo.creativeengineering.compat.jei;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.ParametersAreNonnullByDefault;

import org.slf4j.Logger;

import com.SaintAlfonzo.creativeengineering.CreativeEngineering;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.registries.ForgeRegistries;
import vectorwing.farmersdelight.common.block.FeastBlock;

/**
 * "Feast Plating": what a placed feast gives when right-clicked. None of these are recipes, so JEI had nothing to
 * show: every Farmer's Delight {@link FeastBlock} (any mod, KubeJS Delight's included), read from the block itself
 * (serving per state, the serving's crafting remainder as the container, one container per serving), plus the
 * feasts that use their own block class: Brewin' pizza (any hand, one slice per servings value) and Nether's
 * Delight's Stuffed Hoglin (knife for the ears, then a bowl). Corn Delight's popcorn box wants an empty hand and
 * hands back paper with the last serving.
 *
 * <p>Leftovers are what the block's loot table drops at 0 servings (the last click breaks the plate), read from the
 * table inside its mod jar: a datapack override would not show. Pies and pancakes are eaten or cut in place and stay
 * out.
 */
@ParametersAreNonnullByDefault
public class FeastPlatingCategory implements IRecipeCategory<FeastPlatingCategory.Display> {

    public static final RecipeType<Display> TYPE =
            new RecipeType<>(new ResourceLocation(CreativeEngineering.MODID, "feast_plating"), Display.class);

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int TEXT_COLOR = 0xFF404040;
    private static final int WIDTH = 176, HEIGHT = 56;
    private static final int FEAST_X = 1, TOOL_X = 23, ROW_Y = 1;
    private static final int ARROW_X = 45, ARROW_Y = 1;
    private static final int OUT_X = 74, LEFTOVER_Y = 38;
    private static final int MAX_SLOTS = (WIDTH - OUT_X) / 18;

    private static final ResourceLocation PIZZA = new ResourceLocation("brewinandchewin", "pizza");
    private static final ResourceLocation PIZZA_SLICE = new ResourceLocation("brewinandchewin", "pizza_slice");
    private static final ResourceLocation HOGLIN = new ResourceLocation("nethersdelight", "stuffed_hoglin");
    private static final String POPCORN_BOX = "cn.mcmod.corn_delight.block.PopcornBoxBlock";
    private static final TagKey<Item> KNIVES =
            TagKey.create(ForgeRegistries.Keys.ITEMS, new ResourceLocation("farmersdelight", "tools/knives"));

    /** What the player holds when right-clicking. */
    public enum Hand {
        CONTAINER("container"), EMPTY("empty_hand"), ANY("any_hand"), KNIFE("knife");

        final String key;

        Hand(String key) {
            this.key = key;
        }
    }

    /**
     * One plating route. {@code tools} are the held item(s): the container at one per serving (consumed) or the
     * knives (kept). {@code leftoversGiven}: handed over with the last serving instead of dropped by breaking.
     */
    public record Display(ItemStack feast, Hand hand, List<ItemStack> tools, List<ItemStack> servings,
                          List<ItemStack> leftovers, boolean leftoversGiven) {
    }

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableStatic slot;
    private final IDrawableStatic arrow;

    public FeastPlatingCategory(IGuiHelper gui) {
        background = gui.createBlankDrawable(WIDTH, HEIGHT);
        ItemStack chicken = stack(new ResourceLocation("farmersdelight", "roast_chicken_block"), 1);
        icon = gui.createDrawableItemStack(chicken.isEmpty() ? new ItemStack(Items.BOWL) : chicken);
        slot = gui.getSlotDrawable();
        arrow = JeiArrow.create(gui);
    }

    // ---- collection ----

    public static List<Display> collect() {
        List<Display> out = new ArrayList<>();
        for (Block block : ForgeRegistries.BLOCKS) {
            try {
                if (block instanceof FeastBlock feast) {
                    Display d = fromFeastBlock(feast);
                    if (d != null) {
                        out.add(d);
                    }
                }
            } catch (Throwable t) {
                LOGGER.warn("[jei] feast plating: skipped {}", ForgeRegistries.BLOCKS.getKey(block), t);
            }
        }
        try {
            out.addAll(pizza());
            out.addAll(stuffedHoglin());
        } catch (Throwable t) {
            LOGGER.warn("[jei] feast plating: skipped the pizza / stuffed hoglin pages", t);
        }
        return out;
    }

    private static Display fromFeastBlock(FeastBlock feast) {
        ItemStack feastItem = new ItemStack(feast);
        if (feastItem.isEmpty()) {
            return null;
        }
        IntegerProperty prop = feast.getServingsProperty();
        List<ItemStack> servings = new ArrayList<>();
        List<ItemStack> containers = new ArrayList<>();
        for (int s = feast.getMaxServings(); s >= 1; s--) {
            if (!prop.getPossibleValues().contains(s)) {
                continue;
            }
            ItemStack serving = feast.getServingItem(feast.defaultBlockState().setValue(prop, s));
            if (serving.isEmpty()) {
                continue;
            }
            merge(servings, serving.copy());
            if (serving.hasCraftingRemainingItem()) {
                merge(containers, serving.getCraftingRemainingItem().copy());
            }
        }
        if (servings.isEmpty()) {
            return null;
        }
        if (POPCORN_BOX.equals(feast.getClass().getName())) {
            return new Display(feastItem, Hand.EMPTY, List.of(), servings, List.of(new ItemStack(Items.PAPER)), true);
        }
        List<ItemStack> leftovers = feast.hasLeftovers
                ? leftovers(feast, feast.defaultBlockState().setValue(prop, 0)) : List.of();
        // Several different containers would be one route per container; no feast in the pack does that.
        List<ItemStack> tools = containers.isEmpty() ? List.of() : List.of(containers.get(0));
        return new Display(feastItem, containers.isEmpty() ? Hand.ANY : Hand.CONTAINER, tools, servings, leftovers,
                false);
    }

    /** Brewin' PizzaBlock: one slice a click whatever is held, one per servings value (0 included). */
    private static List<Display> pizza() {
        Block block = block(PIZZA);
        ItemStack slice = stack(PIZZA_SLICE, 1);
        if (block == null || slice.isEmpty()) {
            return List.of();
        }
        IntegerProperty prop = servingsProperty(block);
        slice.setCount(prop == null ? 4 : prop.getPossibleValues().size());
        return List.of(new Display(new ItemStack(block), Hand.ANY, List.of(), List.of(slice), List.of(), false));
    }

    /** StuffedHoglinBlock (servings 11): 2 knife clicks drop ears, then a bowl each: 1 snout, 4 ham, 4 roast. */
    private static List<Display> stuffedHoglin() {
        Block block = block(HOGLIN);
        if (block == null) {
            return List.of();
        }
        ItemStack feast = new ItemStack(block);
        List<Display> out = new ArrayList<>();
        ItemStack ear = stack(new ResourceLocation("nethersdelight", "hoglin_ear"), 2);
        List<ItemStack> knives = knives();
        if (!ear.isEmpty() && !knives.isEmpty()) {
            out.add(new Display(feast, Hand.KNIFE, knives, List.of(ear), List.of(), false));
        }
        List<ItemStack> plates = new ArrayList<>();
        for (Object[] p : new Object[][] {{"snout", 1}, {"ham", 4}, {"roast", 4}}) {
            ItemStack plate = stack(new ResourceLocation("nethersdelight", "plate_of_stuffed_hoglin_" + p[0]), (int) p[1]);
            if (!plate.isEmpty()) {
                plates.add(plate);
            }
        }
        if (!plates.isEmpty()) {
            IntegerProperty prop = servingsProperty(block);
            BlockState empty = prop == null ? block.defaultBlockState() : block.defaultBlockState().setValue(prop, 0);
            out.add(new Display(feast, Hand.CONTAINER, List.of(new ItemStack(Items.BOWL, 9)), plates,
                    leftovers(block, empty), false));
        }
        return out;
    }

    // ---- loot table leftovers ----

    /** The block's loot table, evaluated for {@code state}: block-state conditions only, no randomness. */
    static List<ItemStack> leftovers(Block block, BlockState state) {
        ResourceLocation table = block.getLootTable();
        String path = "data/" + table.getNamespace() + "/loot_tables/" + table.getPath() + ".json";
        JsonObject json = readModResource(table.getNamespace(), path);
        if (json == null) {
            return List.of();
        }
        List<ItemStack> drops = new ArrayList<>();
        for (JsonElement poolEl : GsonHelper.getAsJsonArray(json, "pools", new JsonArray())) {
            JsonObject pool = poolEl.getAsJsonObject();
            if (!conditions(pool, state)) {
                continue;
            }
            int rolls = number(pool.get("rolls"), 1);
            for (JsonElement entry : GsonHelper.getAsJsonArray(pool, "entries", new JsonArray())) {
                addEntry(entry.getAsJsonObject(), state, rolls, drops);
            }
        }
        return drops;
    }

    private static void addEntry(JsonObject entry, BlockState state, int rolls, List<ItemStack> drops) {
        if (!conditions(entry, state)) {
            return;
        }
        String type = GsonHelper.getAsString(entry, "type", "");
        if (type.endsWith(":item")) {
            int count = 1;
            for (JsonElement fn : GsonHelper.getAsJsonArray(entry, "functions", new JsonArray())) {
                JsonObject f = fn.getAsJsonObject();
                if (GsonHelper.getAsString(f, "function", "").endsWith("set_count")) {
                    count = number(f.get("count"), 1);
                }
            }
            ItemStack drop = stack(new ResourceLocation(GsonHelper.getAsString(entry, "name")), count * rolls);
            if (!drop.isEmpty()) {
                merge(drops, drop);
            }
        } else if (entry.has("children")) {
            for (JsonElement child : entry.getAsJsonArray("children")) {
                addEntry(child.getAsJsonObject(), state, rolls, drops);
            }
        }
    }

    private static boolean conditions(JsonObject owner, BlockState state) {
        for (JsonElement c : GsonHelper.getAsJsonArray(owner, "conditions", new JsonArray())) {
            if (!condition(c.getAsJsonObject(), state)) {
                return false;
            }
        }
        return true;
    }

    /** Block-state, inverted, all_of / any_of; survives_explosion passes; anything else (tool checks) fails. */
    private static boolean condition(JsonObject c, BlockState state) {
        String type = GsonHelper.getAsString(c, "condition", "");
        switch (type.substring(type.indexOf(':') + 1)) {
            case "block_state_property": {
                JsonObject props = GsonHelper.getAsJsonObject(c, "properties", new JsonObject());
                for (Map.Entry<String, JsonElement> e : props.entrySet()) {
                    if (!matches(state, e.getKey(), e.getValue())) {
                        return false;
                    }
                }
                return true;
            }
            case "inverted":
                return !condition(GsonHelper.getAsJsonObject(c, "term"), state);
            case "alternative":
            case "any_of":
                for (JsonElement t : GsonHelper.getAsJsonArray(c, "terms")) {
                    if (condition(t.getAsJsonObject(), state)) {
                        return true;
                    }
                }
                return false;
            case "all_of":
                for (JsonElement t : GsonHelper.getAsJsonArray(c, "terms")) {
                    if (!condition(t.getAsJsonObject(), state)) {
                        return false;
                    }
                }
                return true;
            case "survives_explosion":
                return true;
            default:
                return false;
        }
    }

    private static <T extends Comparable<T>> boolean matches(BlockState state, String name, JsonElement wanted) {
        @SuppressWarnings("unchecked")
        Property<T> prop = (Property<T>) state.getBlock().getStateDefinition().getProperty(name);
        if (prop == null) {
            return false;
        }
        String value = prop.getName(state.getValue(prop));
        if (wanted.isJsonPrimitive()) {
            return wanted.getAsString().equals(value);
        }
        JsonObject range = wanted.getAsJsonObject();
        try {
            int v = Integer.parseInt(value);
            return v >= GsonHelper.getAsInt(range, "min", Integer.MIN_VALUE)
                    && v <= GsonHelper.getAsInt(range, "max", Integer.MAX_VALUE);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** A constant, or the low end of a {min, max} range. */
    private static int number(JsonElement el, int fallback) {
        if (el == null) {
            return fallback;
        }
        if (el.isJsonPrimitive()) {
            return (int) el.getAsFloat();
        }
        JsonObject o = el.getAsJsonObject();
        return o.has("min") ? (int) o.get("min").getAsFloat() : o.has("value") ? (int) o.get("value").getAsFloat()
                : fallback;
    }

    /** The resource from the namespace's own mod jar first, then any loaded mod file. */
    private static JsonObject readModResource(String namespace, String path) {
        List<IModFileInfo> files = new ArrayList<>(ModList.get().getModFiles());
        IModFileInfo own = ModList.get().getModFileById(namespace);
        if (own != null) {
            files.remove(own);
            files.add(0, own);
        }
        for (IModFileInfo file : files) {
            try {
                Path p = file.getFile().findResource(path.split("/"));
                if (Files.exists(p)) {
                    try (Reader reader = Files.newBufferedReader(p)) {
                        return JsonParser.parseReader(reader).getAsJsonObject();
                    }
                }
            } catch (Exception e) {
                LOGGER.debug("[jei] feast plating: couldn't read {} from {}", path, file.getFile().getFileName(), e);
            }
        }
        return null;
    }

    // ---- small helpers ----

    private static Block block(ResourceLocation id) {
        return ForgeRegistries.BLOCKS.containsKey(id) ? ForgeRegistries.BLOCKS.getValue(id) : null;
    }

    private static ItemStack stack(ResourceLocation id, int count) {
        Item item = ForgeRegistries.ITEMS.containsKey(id) ? ForgeRegistries.ITEMS.getValue(id) : null;
        return item == null || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item, count);
    }

    private static IntegerProperty servingsProperty(Block block) {
        return block.getStateDefinition().getProperty("servings") instanceof IntegerProperty p ? p : null;
    }

    private static List<ItemStack> knives() {
        var tags = ForgeRegistries.ITEMS.tags();
        if (tags == null) {
            return List.of();
        }
        List<ItemStack> list = new ArrayList<>();
        tags.getTag(KNIVES).forEach(item -> list.add(new ItemStack(item)));
        return list;
    }

    private static void merge(List<ItemStack> list, ItemStack stack) {
        for (ItemStack s : list) {
            if (ItemStack.isSameItemSameTags(s, stack)) {
                s.grow(stack.getCount());
                return;
            }
        }
        list.add(stack);
    }

    // ---- layout ----

    @Override
    public RecipeType<Display> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("creativeengineering.jei.feast_plating");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Display recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, FEAST_X, ROW_Y).addItemStack(recipe.feast())
                .setBackground(slot, -1, -1);
        if (!recipe.tools().isEmpty()) {
            RecipeIngredientRole role = recipe.hand() == Hand.KNIFE ? RecipeIngredientRole.CATALYST
                    : RecipeIngredientRole.INPUT;
            builder.addSlot(role, TOOL_X, ROW_Y).addItemStacks(recipe.tools()).setBackground(slot, -1, -1);
        }
        addRow(builder, recipe.servings(), ROW_Y);
        addRow(builder, recipe.leftovers(), LEFTOVER_Y);
    }

    private void addRow(IRecipeLayoutBuilder builder, List<ItemStack> stacks, int y) {
        for (int i = 0; i < stacks.size() && i < MAX_SLOTS; i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, OUT_X + i * 18, y).addItemStack(stacks.get(i))
                    .setBackground(slot, -1, -1);
        }
    }

    @Override
    public void draw(Display recipe, IRecipeSlotsView view, PoseStack pose, double mouseX, double mouseY) {
        arrow.draw(pose, ARROW_X, ARROW_Y);
        var font = Minecraft.getInstance().font;
        font.draw(pose, Component.translatable("creativeengineering.jei.feast_plating." + recipe.hand().key),
                FEAST_X, 24, TEXT_COLOR);
        if (!recipe.leftovers().isEmpty()) {
            font.draw(pose, Component.translatable("creativeengineering.jei.feast_plating.leftovers"),
                    FEAST_X, LEFTOVER_Y + 4, TEXT_COLOR);
        }
    }

    @Override
    public List<Component> getTooltipStrings(Display recipe, IRecipeSlotsView view, double mouseX, double mouseY) {
        if (mouseY >= ARROW_Y && mouseY < ARROW_Y + 17 && mouseX >= ARROW_X && mouseX < ARROW_X + 24) {
            return List.of(Component.translatable("creativeengineering.jei.feast_plating.arrow"));
        }
        if (!recipe.leftovers().isEmpty() && mouseY >= LEFTOVER_Y && mouseY < LEFTOVER_Y + 16 && mouseX < OUT_X) {
            return List.of(Component.translatable("creativeengineering.jei.feast_plating.leftovers."
                    + (recipe.leftoversGiven() ? "given" : "break")));
        }
        return Collections.emptyList();
    }
}
