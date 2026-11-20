package com.SaintAlfonzo.solraspberrypie.client;

import com.SaintAlfonzo.solraspberrypie.RpConfig;
import com.SaintAlfonzo.solraspberrypie.data.UnlockOrder;
import net.minecraft.client.Minecraft;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The Food Dictionary's list orders (plan_diet_books.md 2.2, 2.3; plan_diet_books_r2.md A8). Uneaten foods: client
 * config {@code [foodDictionary] uneatenSort}. Eaten foods: {@code eatenSort}, cycled by the sort button: newest unlock
 * first (foods unlocked before the order was stored follow, in the uneaten order), latest eaten first (Apple Pie's meal
 * memory; foods it no longer remembers follow, in the uneaten order), highest food, highest saturation (points
 * restored: hunger x modifier x 2), highest diversity (Apple Pie's base value), or custom ({@code customOrder}, only
 * while it lists something). Ties fall back to the uneaten order.
 */
public final class FoodDictionarySort {
	public static RpConfig.UneatenSort uneatenMode() {
		return RpConfig.CLIENT_SPEC.isLoaded() ? RpConfig.CLIENT.uneatenSort.get() : RpConfig.UneatenSort.MOD_NAME;
	}

	public static RpConfig.EatenSort eatenMode() {
		return RpConfig.CLIENT_SPEC.isLoaded() ? RpConfig.CLIENT.eatenSort.get() : RpConfig.EatenSort.NEWEST_UNLOCK;
	}

	/** Steps the eaten order to the next mode and saves the client config. */
	public static void cycleEaten() {
		if (!RpConfig.CLIENT_SPEC.isLoaded()) return;
		RpConfig.CLIENT.eatenSort.set(eatenMode().next(customConfigured()));
		RpConfig.CLIENT.eatenSort.save();
	}

	/** The custom order is shown only once {@code customOrder} lists something (food-tuning #15). */
	public static boolean customConfigured() {
		return RpConfig.CLIENT_SPEC.isLoaded() && !RpConfig.CLIENT.customOrder.get().isEmpty();
	}

	public static Comparator<Item> uneaten() {
		Map<Item, String> names = new HashMap<>();
		Comparator<Item> byName = Comparator.comparing(i -> names.computeIfAbsent(i, FoodDictionarySort::name));
		Comparator<Item> modName = Comparator.<Item, String>comparing(FoodDictionarySort::namespace).thenComparing(byName);
		return switch (uneatenMode()) {
			case MOD_NAME -> modName;
			case NAME -> byName;
			case EASIEST -> Comparator.comparingDouble(FoodDictionarySort::complexity).thenComparingInt(FoodDictionarySort::nutrition).thenComparing(byName);
			case HIGHEST_FOOD -> highestFood().thenComparing(byName);
			case HIGHEST_SATURATION -> highestSaturation().thenComparing(byName);
			case HIGHEST_DIVERSITY -> highestDiversity().thenComparing(byName);
			case CUSTOM -> customConfigured() ? custom().thenComparing(modName) : modName;
		};
	}

	private static Comparator<Item> highestFood() {
		return Comparator.comparingInt(FoodDictionarySort::nutrition).reversed();
	}

	private static Comparator<Item> highestSaturation() {
		return Comparator.comparingDouble(FoodDictionarySort::saturation).reversed();
	}

	private static Comparator<Item> highestDiversity() {
		Map<Item, Double> cache = new HashMap<>();
		return Comparator.<Item>comparingDouble(i -> cache.computeIfAbsent(i, FoodDictionarySort::complexity)).reversed();
	}

	/** The listed items first, in list order; the rest after them. */
	private static Comparator<Item> custom() {
		Map<ResourceLocation, Integer> rank = new HashMap<>();
		List<? extends String> ids = RpConfig.CLIENT.customOrder.get();
		for (int i = 0; i < ids.size(); i++) {
			ResourceLocation rl = ResourceLocation.tryParse(ids.get(i).trim());
			if (rl != null) rank.putIfAbsent(rl, i);
		}
		return Comparator.comparingInt(i -> rank.getOrDefault(ForgeRegistries.ITEMS.getKey(i), Integer.MAX_VALUE));
	}

	public static Comparator<Item> eaten(com.cazsius.solcarrot.tracking.FoodList carrot) {
		Comparator<Item> base = uneaten();
		return switch (eatenMode()) {
			case NEWEST_UNLOCK -> {
				List<ResourceLocation> order = UnlockOrder.of(carrot);
				Map<ResourceLocation, Integer> rank = new HashMap<>();
				for (int i = 0; i < order.size(); i++) rank.put(order.get(i), order.size() - 1 - i);
				yield Comparator.<Item>comparingInt(i -> rank.getOrDefault(ForgeRegistries.ITEMS.getKey(i), Integer.MAX_VALUE)).thenComparing(base);
			}
			case LATEST_EATEN -> {
				var player = Minecraft.getInstance().player;
				com.tarinoita.solsweetpotato.tracking.FoodList apple = null;
				try {
					if (player != null) apple = com.tarinoita.solsweetpotato.tracking.FoodList.get(player);
				} catch (RuntimeException ignored) {
				}
				final var memory = apple;
				yield Comparator.<Item>comparingInt(i -> {
					int k = memory == null ? -1 : memory.getLastEaten(i);
					return k < 0 ? Integer.MAX_VALUE : k;
				}).thenComparing(base);
			}
			case HIGHEST_FOOD -> highestFood().thenComparing(base);
			case HIGHEST_SATURATION -> highestSaturation().thenComparing(base);
			case HIGHEST_DIVERSITY -> highestDiversity().thenComparing(base);
			case CUSTOM -> customConfigured() ? custom().thenComparing(base) : base;
		};
	}

	private static String namespace(Item item) {
		ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
		return id == null ? "" : id.getNamespace();
	}

	private static String name(Item item) {
		return new ItemStack(item).getHoverName().getString().toLowerCase(Locale.ROOT);
	}

	/** Apple Pie's complexity of the food (its config, else its own nutrition-based value). */
	private static double complexity(Item item) {
		try {
			return com.tarinoita.solsweetpotato.tracking.FoodList.getComplexity(new com.tarinoita.solsweetpotato.tracking.FoodInstance(item));
		} catch (RuntimeException e) {
			return 0;
		}
	}

	private static int nutrition(Item item) {
		FoodProperties p = item.getFoodProperties();
		return p == null ? 0 : p.getNutrition();
	}

	/** Saturation points restored: hunger x saturation modifier x 2 (plan_diet_books_r2.md ruling 17, 18). */
	private static double saturation(Item item) {
		FoodProperties p = item.getFoodProperties();
		return p == null ? 0 : p.getNutrition() * p.getSaturationModifier() * 2.0;
	}

	private FoodDictionarySort() {}
}
