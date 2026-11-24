// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.solraspberrypie.recipe;

import com.SaintAlfonzo.solraspberrypie.SolRaspberryPie;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

/**
 * Any edible item that is not in an exclusion tag (publish_readiness.md PR-RP-12): the Diet Log recipe's food slot, so
 * the jar standing alone takes every food of whatever mods are loaded. JSON:
 * {@code {"type": "solraspberrypie:edible", "exclude": "<item tag>"}} ("exclude" optional). The listed items (JEI's
 * cycle) are every registered edible item minus the tag, built on first use and again after a tag reload.
 */
public final class EdibleIngredient extends AbstractIngredient {
	public static final ResourceLocation ID = new ResourceLocation(SolRaspberryPie.MOD_ID, "edible");

	@Nullable
	private final TagKey<Item> exclude;
	private ItemStack[] items;
	private IntList packed;

	public EdibleIngredient(@Nullable TagKey<Item> exclude) {
		this.exclude = exclude;
	}

	@Override
	public boolean test(@Nullable ItemStack stack) {
		return stack != null && !stack.isEmpty() && stack.isEdible() && (exclude == null || !stack.is(exclude));
	}

	@Override
	public ItemStack[] getItems() {
		if (items == null || checkInvalidation()) {
			markValid();
			packed = null;
			items = ForgeRegistries.ITEMS.getValues().stream().map(ItemStack::new).filter(this::test).toArray(ItemStack[]::new);
		}
		return items;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public IntList getStackingIds() {
		ItemStack[] stacks = getItems();
		if (packed == null) {
			IntList out = new IntArrayList(stacks.length);
			for (ItemStack s : stacks) out.add(StackedContents.getStackingIndex(s));
			out.sort(IntComparators.NATURAL_COMPARATOR);
			packed = out;
		}
		return packed;
	}

	@Override
	protected void invalidate() {
		super.invalidate();
		items = null;
		packed = null;
	}

	@Override
	public boolean isSimple() {
		return false;
	}

	@Override
	public IIngredientSerializer<EdibleIngredient> getSerializer() {
		return Serializer.INSTANCE;
	}

	@Override
	public JsonElement toJson() {
		JsonObject json = new JsonObject();
		json.addProperty("type", ID.toString());
		if (exclude != null) json.addProperty("exclude", exclude.location().toString());
		return json;
	}

	/** Registered by {@link SolRaspberryPie} with the recipe serializers. */
	public static final class Serializer implements IIngredientSerializer<EdibleIngredient> {
		public static final Serializer INSTANCE = new Serializer();

		@Override
		public EdibleIngredient parse(JsonObject json) {
			return new EdibleIngredient(json.has("exclude") ? ItemTags.create(new ResourceLocation(GsonHelper.getAsString(json, "exclude"))) : null);
		}

		@Override
		public EdibleIngredient parse(FriendlyByteBuf buf) {
			return new EdibleIngredient(buf.readBoolean() ? ItemTags.create(buf.readResourceLocation()) : null);
		}

		@Override
		public void write(FriendlyByteBuf buf, EdibleIngredient ing) {
			buf.writeBoolean(ing.exclude != null);
			if (ing.exclude != null) buf.writeResourceLocation(ing.exclude.location());
		}

		public static void register() {
			CraftingHelper.register(ID, INSTANCE);
		}
	}
}
