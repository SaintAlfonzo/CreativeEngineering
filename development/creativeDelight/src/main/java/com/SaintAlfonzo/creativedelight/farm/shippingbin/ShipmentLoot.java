package com.SaintAlfonzo.creativedelight.farm.shippingbin;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

import java.util.List;
import java.util.Set;

/**
 * The payout loot table's context (ruling D2: every price lives in {@code farmingforblockheads:gameplay/shipping_bin}, never
 * in code). A sale rolls that table with the shipment value (points sold, whole loads only, ruling D3a) and the sold
 * stacks, and these read them:
 * <ul>
 * <li>number provider {@code farmingforblockheads:shipment_value}: {@code floor(value * scale + offset)}
 * ({@code "scale"} default 1, {@code "offset"} default 0); emeralds at 128 points each are {@code "scale": 0.0078125}.</li>
 * <li>condition {@code farmingforblockheads:shipment_value}: passes when the value is in {@code "min"} .. {@code "max"}
 * (each optional, inclusive).</li>
 * <li>condition {@code farmingforblockheads:shipment_contains}: passes when the sold stacks hold at least
 * {@code "count"} (default 1) items matching {@code "ingredient"}.</li>
 * </ul>
 */
public final class ShipmentLoot {
	public static final LootContextParam<Integer> SHIPMENT_VALUE = new LootContextParam<>(ShippingBin.id("shipment_value"));
	public static final LootContextParam<List<ItemStack>> SOLD_STACKS = new LootContextParam<>(ShippingBin.id("sold_stacks"));
	public static final LootContextParamSet PARAMS = new LootContextParamSet.Builder()
			.required(LootContextParams.ORIGIN).required(SHIPMENT_VALUE).required(SOLD_STACKS).build();

	private ShipmentLoot() {
	}

	static int value(LootContext ctx) {
		Integer value = ctx.getParamOrNull(SHIPMENT_VALUE);
		return value == null ? 0 : value;
	}

	public record ValueProvider(float scale, float offset) implements NumberProvider {
		@Override
		public float getFloat(LootContext ctx) {
			return value(ctx) * scale + offset;
		}

		@Override
		public int getInt(LootContext ctx) {
			return Mth.floor(getFloat(ctx));
		}

		@Override
		public LootNumberProviderType getType() {
			return ShippingBin.SHIPMENT_VALUE_PROVIDER.get();
		}

		@Override
		public Set<LootContextParam<?>> getReferencedContextParams() {
			return Set.of(); // optional read: a table rolled elsewhere sees 0, not a validation error
		}

		public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ValueProvider> {
			@Override
			public void serialize(JsonObject json, ValueProvider provider, JsonSerializationContext ctx) {
				json.addProperty("scale", provider.scale);
				json.addProperty("offset", provider.offset);
			}

			@Override
			public ValueProvider deserialize(JsonObject json, JsonDeserializationContext ctx) {
				return new ValueProvider(GsonHelper.getAsFloat(json, "scale", 1.0F), GsonHelper.getAsFloat(json, "offset", 0.0F));
			}
		}
	}

	public record ValueCondition(int min, int max) implements LootItemCondition {
		@Override
		public boolean test(LootContext ctx) {
			int value = value(ctx);
			return value >= min && value <= max;
		}

		@Override
		public LootItemConditionType getType() {
			return ShippingBin.SHIPMENT_VALUE_CONDITION.get();
		}

		public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ValueCondition> {
			@Override
			public void serialize(JsonObject json, ValueCondition condition, JsonSerializationContext ctx) {
				if (condition.min != Integer.MIN_VALUE) json.addProperty("min", condition.min);
				if (condition.max != Integer.MAX_VALUE) json.addProperty("max", condition.max);
			}

			@Override
			public ValueCondition deserialize(JsonObject json, JsonDeserializationContext ctx) {
				return new ValueCondition(GsonHelper.getAsInt(json, "min", Integer.MIN_VALUE), GsonHelper.getAsInt(json, "max", Integer.MAX_VALUE));
			}
		}
	}

	public record ContainsCondition(Ingredient ingredient, int count) implements LootItemCondition {
		@Override
		public boolean test(LootContext ctx) {
			List<ItemStack> sold = ctx.getParamOrNull(SOLD_STACKS);
			if (sold == null) return false;
			int found = 0;
			for (ItemStack stack : sold) {
				if (ingredient.test(stack)) found += stack.getCount();
			}
			return found >= count;
		}

		@Override
		public LootItemConditionType getType() {
			return ShippingBin.SHIPMENT_CONTAINS_CONDITION.get();
		}

		public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ContainsCondition> {
			@Override
			public void serialize(JsonObject json, ContainsCondition condition, JsonSerializationContext ctx) {
				json.add("ingredient", condition.ingredient.toJson());
				json.addProperty("count", condition.count);
			}

			@Override
			public ContainsCondition deserialize(JsonObject json, JsonDeserializationContext ctx) {
				Ingredient ingredient = Ingredient.fromJson(json.get("ingredient"));
				return new ContainsCondition(ingredient, GsonHelper.getAsInt(json, "count", 1));
			}
		}
	}
}
