// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativeoxidation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;

/**
 * R10 (plan_creative_oxidation.md): {@code {"type": "creativeoxidation:staged", "item": "create:fluid_pipe",
 * "stage": "exposed", "waxed": false}} matches that item at exactly that stage and wax. "unaffected" (not waxed) matches
 * only a plain item, so an aging recipe never takes a staged item for a plain one. Spout and fan aging recipes (P7c) use
 * it as their input; every other recipe keeps its plain item ingredient, which ignores NBT (R15).
 */
public final class StagedIngredient extends AbstractIngredient {
	public static final ResourceLocation ID = new ResourceLocation(CreativeOxidation.MOD_ID, "staged");

	private final Item item;
	private final OxidationStage stage;
	private final boolean waxed;

	public StagedIngredient(Item item, OxidationStage stage, boolean waxed) {
		super(Stream.of(new Ingredient.ItemValue(OxidationItemData.staged(new ItemStack(item), stage, waxed))));
		this.item = item;
		this.stage = stage;
		this.waxed = waxed;
	}

	public Item item() {
		return item;
	}

	public OxidationStage stage() {
		return stage;
	}

	public boolean waxed() {
		return waxed;
	}

	@Override
	public boolean test(ItemStack stack) {
		if (stack == null || !stack.is(item)) return false;
		return matches(OxidationItemData.read(stack), stage, waxed);
	}

	/** The stage rule alone (tools/test_creative_oxidation.sh). */
	public static boolean matches(Optional<OxidationItemData.StoredState> stored, OxidationStage stage, boolean waxed) {
		OxidationItemData.StoredState have = stored.orElse(new OxidationItemData.StoredState(OxidationStage.UNAFFECTED, false));
		return have.stage() == stage && have.waxed() == waxed;
	}

	@Override
	public boolean isSimple() {
		return false;
	}

	@Override
	public IIngredientSerializer<? extends Ingredient> getSerializer() {
		return Serializer.INSTANCE;
	}

	@Override
	public JsonElement toJson() {
		JsonObject json = new JsonObject();
		json.addProperty("type", ID.toString());
		json.addProperty("item", Registry.ITEM.getKey(item).toString());
		json.addProperty("stage", stage.getSerializedName());
		json.addProperty("waxed", waxed);
		return json;
	}

	public static final class Serializer implements IIngredientSerializer<StagedIngredient> {
		public static final Serializer INSTANCE = new Serializer();

		private Serializer() {
		}

		@Override
		public StagedIngredient parse(JsonObject json) {
			String itemId = GsonHelper.getAsString(json, "item");
			Item item = Registry.ITEM.getOptional(new ResourceLocation(itemId))
					.orElseThrow(() -> new JsonSyntaxException("Unknown item '" + itemId + "'"));
			if (item == Items.AIR) throw new JsonSyntaxException("Staged ingredient item cannot be air");
			String stageName = GsonHelper.getAsString(json, "stage", OxidationStage.UNAFFECTED.getSerializedName());
			OxidationStage stage = OxidationStage.byName(stageName);
			if (stage == null) throw new JsonSyntaxException("Unknown oxidation stage '" + stageName + "'");
			return new StagedIngredient(item, stage, GsonHelper.getAsBoolean(json, "waxed", false));
		}

		@Override
		public StagedIngredient parse(FriendlyByteBuf buffer) {
			Item item = Registry.ITEM.byId(buffer.readVarInt());
			OxidationStage stage = buffer.readEnum(OxidationStage.class);
			return new StagedIngredient(item, stage, buffer.readBoolean());
		}

		@Override
		public void write(FriendlyByteBuf buffer, StagedIngredient ingredient) {
			buffer.writeVarInt(Registry.ITEM.getId(ingredient.item));
			buffer.writeEnum(ingredient.stage);
			buffer.writeBoolean(ingredient.waxed);
		}
	}
}
