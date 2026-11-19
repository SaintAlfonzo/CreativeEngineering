package com.SaintAlfonzo.creativedelight.food;

import java.util.ArrayList;
import java.util.List;

import com.SaintAlfonzo.creativedelight.CreativeDelight;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Crafting grid, shapeless: one food matching {@code food} plus one of each flavor's ingredient gives the same food
 * (all NBT kept, count 1) with those flavors added ({@link Flavors}). It doesn't match if the food is outside
 * any of their flavorable tags ({@link Flavors#flavorableTag}), already has one of them, or any two of the food's
 * flavors don't combine, the food would go past the cap ({@code [food] maxFlavors}), or a part is a station
 * flavor ({@code grilled}). Type {@code creativedelight:flavor}; emitted by
 * {@code global.NCH.flavor}. Shown on the Flavor JEI page.
 */
public class FlavorRecipe extends CustomRecipe {

	/** One flavor the recipe puts on and the item that carries it. */
	public record Part(ResourceLocation flavor, Ingredient ingredient) {
	}

	private final Ingredient food;
	private final List<Part> parts;

	public FlavorRecipe(ResourceLocation id, Ingredient food, List<Part> parts) {
		super(id);
		this.food = food;
		this.parts = List.copyOf(parts);
	}

	public Ingredient getFood() {
		return food;
	}

	public List<Part> getParts() {
		return parts;
	}

	public List<ResourceLocation> flavors() {
		return parts.stream().map(Part::flavor).toList();
	}

	/** The food stack in the grid if the rest is exactly one of each part, else EMPTY. */
	private ItemStack findFood(CraftingContainer inv) {
		ItemStack found = ItemStack.EMPTY;
		boolean[] used = new boolean[parts.size()];
		for (int slot = 0; slot < inv.getContainerSize(); slot++) {
			ItemStack s = inv.getItem(slot);
			if (s.isEmpty()) {
				continue;
			}
			boolean matched = false;
			for (int n = 0; n < parts.size(); n++) {
				if (!used[n] && parts.get(n).ingredient().test(s)) {
					used[n] = true;
					matched = true;
					break;
				}
			}
			if (matched) {
				continue;
			}
			if (!found.isEmpty() || !food.test(s)) {
				return ItemStack.EMPTY;
			}
			found = s;
		}
		for (boolean u : used) {
			if (!u) {
				return ItemStack.EMPTY;
			}
		}
		return found;
	}

	@Override
	public boolean matches(CraftingContainer inv, Level level) {
		ItemStack s = findFood(inv);
		// station flavors (grilled) are put on by their station only, never crafted
		return !s.isEmpty() && flavors().stream().noneMatch(Flavors::isStation)
				&& Flavors.accepts(s, flavors()) && Flavors.canAdd(Flavors.of(s), flavors());
	}

	@Override
	public ItemStack assemble(CraftingContainer inv) {
		ItemStack s = findFood(inv);
		if (s.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack out = s.copy();
		out.setCount(1);
		return Flavors.with(out, flavors());
	}

	@Override
	public boolean canCraftInDimensions(int w, int h) {
		return w * h >= parts.size() + 1;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> list = NonNullList.create();
		list.add(food);
		parts.forEach(p -> list.add(p.ingredient()));
		return list;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER.get();
	}

	private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
			DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, CreativeDelight.MOD_ID);

	public static final RegistryObject<RecipeSerializer<?>> SERIALIZER = SERIALIZERS.register("flavor", Serializer::new);

	public static void register(IEventBus modEventBus) {
		SERIALIZERS.register(modEventBus);
	}

	/** {@code {"food": <ingredient>, "flavors": [{"flavor": "ns:id", "ingredient": <ingredient>}, ...]}} */
	public static final class Serializer implements RecipeSerializer<FlavorRecipe> {
		@Override
		public FlavorRecipe fromJson(ResourceLocation id, JsonObject json) {
			List<Part> parts = new ArrayList<>();
			for (JsonElement e : GsonHelper.getAsJsonArray(json, "flavors")) {
				JsonObject o = e.getAsJsonObject();
				parts.add(new Part(new ResourceLocation(GsonHelper.getAsString(o, "flavor")),
						CraftingHelper.getIngredient(o.get("ingredient"))));
			}
			if (parts.isEmpty()) {
				throw new IllegalArgumentException("flavor recipe " + id + " has no flavors");
			}
			return new FlavorRecipe(id, CraftingHelper.getIngredient(json.get("food")), parts);
		}

		@Override
		public FlavorRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
			Ingredient food = Ingredient.fromNetwork(buf);
			int n = buf.readVarInt();
			List<Part> parts = new ArrayList<>(n);
			for (int k = 0; k < n; k++) {
				parts.add(new Part(buf.readResourceLocation(), Ingredient.fromNetwork(buf)));
			}
			return new FlavorRecipe(id, food, parts);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buf, FlavorRecipe recipe) {
			recipe.food.toNetwork(buf);
			buf.writeVarInt(recipe.parts.size());
			for (Part p : recipe.parts) {
				buf.writeResourceLocation(p.flavor());
				p.ingredient().toNetwork(buf);
			}
		}
	}
}
