package ce.shared;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

/**
 * The plating containers of one cooking station (feature_writeup/remainder_buffer.md 3.8): the items its recipes are
 * served in, plus any tags it names, minus the items one of its recipes also takes as an ingredient. A container goes
 * only into the station's container slot (by hand, shift-click and automation) and nothing else goes there.
 * <p>
 * Built from the level's recipe manager on first use and rebuilt after every data reload (TagsUpdatedEvent fires on both
 * sides when tags, and with them recipes, are loaded or synced; RecipesUpdatedEvent on the client). Items that are both a container and an ingredient are
 * logged once per build and stay allowed in the ingredient slots, so no recipe stops working.
 * Shared source (development/shared/stations), compiled into each jar under its own package.
 */
public final class StationContainers {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static volatile int generation;
    private static final Map<String, Built> CACHE = new HashMap<>();

    static {
        MinecraftForge.EVENT_BUS.addListener((TagsUpdatedEvent e) -> ++generation);
        // On a /reload the server sends tags before recipes: the client also rebuilds when its recipes arrive.
        if (FMLEnvironment.dist.isClient()) ClientHook.register();
    }

    private static final class ClientHook {
        static void register() {
            MinecraftForge.EVENT_BUS.addListener((RecipesUpdatedEvent e) -> ++generation);
        }
    }

    private final String name;
    private final RecipeType<?> type;
    private final Function<Recipe<?>, List<ItemStack>> containersOf;
    private final List<TagKey<Item>> tags;

    private record Built(int generation, Set<Item> containers) {}

    /**
     * @param name         station name for the log and the cache (one instance per station kind)
     * @param type         the station's recipe type
     * @param containersOf the containers one recipe is served in (may be empty)
     * @param tags         extra container tags (e.g. farmersdelight:serving_containers)
     */
    public StationContainers(String name, RecipeType<?> type, Function<Recipe<?>, List<ItemStack>> containersOf, List<TagKey<Item>> tags) {
        this.name = name;
        this.type = type;
        this.containersOf = containersOf;
        this.tags = tags;
    }

    /** Whether {@code stack} is one of this station's containers in {@code level} (false with no level). */
    public boolean isContainer(Level level, ItemStack stack) {
        if (level == null || stack.isEmpty()) return false;
        return containers(level).contains(stack.getItem());
    }

    private Set<Item> containers(Level level) {
        String key = name + (level.isClientSide ? "@client" : "@server");
        int gen = generation;
        synchronized (CACHE) {
            Built built = CACHE.get(key);
            if (built != null && built.generation() == gen) return built.containers();
        }
        Set<Item> set = build(level);
        synchronized (CACHE) {
            CACHE.put(key, new Built(gen, set));
        }
        return set;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Set<Item> build(Level level) {
        Set<Item> containers = new HashSet<>();
        Set<Item> ingredients = new HashSet<>();
        List<? extends Recipe<?>> recipes = level.getRecipeManager().getAllRecipesFor((RecipeType) type);
        for (Recipe<?> recipe : recipes) {
            for (ItemStack c : containersOf.apply(recipe)) {
                if (!c.isEmpty()) containers.add(c.getItem());
            }
            for (Ingredient ingredient : recipe.getIngredients()) {
                for (ItemStack s : ingredient.getItems()) ingredients.add(s.getItem());
            }
        }
        for (TagKey<Item> tag : tags) {
            ForgeRegistries.ITEMS.tags().getTag(tag).forEach(containers::add);
        }
        Set<Item> both = new HashSet<>(containers);
        both.retainAll(ingredients);
        if (!both.isEmpty()) {
            containers.removeAll(both);
            if (!level.isClientSide) {
                LOGGER.info("[stations] {}: also used as an ingredient, allowed in the ingredient slots: {}", name,
                        both.stream().map(i -> String.valueOf(Registry.ITEM.getKey(i))).sorted().collect(Collectors.joining(", ")));
            }
        }
        return Set.copyOf(containers);
    }
}
