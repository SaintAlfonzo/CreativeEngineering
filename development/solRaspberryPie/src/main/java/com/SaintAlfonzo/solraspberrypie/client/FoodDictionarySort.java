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
 * The Food Dictionary's list orders (plan_diet_books.md 2.2, 2.3). Uneaten foods: client config
 * {@code [foodDictionary] uneatenSort}. Eaten foods: {@code eatenSort}, cycled by the sort button: newest unlock first
 * (foods unlocked before the order was stored follow, in the uneaten order), latest eaten first (Apple Pie's meal
 * memory; foods it no longer remembers follow, in the uneaten order), or the uneaten order.
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
		RpConfig.CLIENT.eatenSort.set(eatenMode().next());
		RpConfig.CLIENT.eatenSort.save();
	}

	public static Comparator<Item> uneaten() {
		Map<Item, String> names = new HashMap<>();
		Comparator<Item> byName = Comparator.comparing(i -> names.computeIfAbsent(i, FoodDictionarySort::name));
		return switch (uneatenMode()) {
			case MOD_NAME -> Comparator.<Item, String>comparing(FoodDictionarySort::namespace).thenComparing(byName);
			case NAME -> byName;
			case EASIEST -> Comparator.comparingDouble(FoodDictionarySort::complexity).thenComparingInt(FoodDictionarySort::nutrition).thenComparing(byName);
			case HARDEST -> Comparator.comparingDouble(FoodDictionarySort::complexity).reversed()
					.thenComparing(Comparator.comparingInt(FoodDictionarySort::nutrition).reversed()).thenComparing(byName);
		};
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
			case UNEATEN_ORDER -> base;
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

	private FoodDictionarySort() {}
}
