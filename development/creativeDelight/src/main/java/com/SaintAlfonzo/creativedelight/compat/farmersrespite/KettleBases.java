package com.SaintAlfonzo.creativedelight.compat.farmersrespite;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import umpaz.farmersrespite.common.crafting.KettleRecipe;

/**
 * The {@code base} fluid of a Farmer's Respite Kettle recipe (farmers_respite.md K8): the tank fluid one brew uses, one
 * unit ({@link KettleTank#UNIT} mB). Kept beside the recipe instead of in a field so {@code KettleRecipe} needs no
 * extra interface (the class loads late, at the first recipe load).
 *
 * <p>JSON, 2.1's shape with an item result: {@code "base": {"fluid": "minecraft:water", "count": 250}}, or
 * {@code "base": "none"} for a recipe that uses no tank fluid. Without {@code base} the 2.0 key {@code needwater}
 * (default true) still reads as a water base, so a recipe from a mod the pack has not rewritten keeps working.
 */
public final class KettleBases {

	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Map<KettleRecipe, FluidStack> BASES = Collections.synchronizedMap(new WeakHashMap<>());

	private KettleBases() {}

	/** The recipe's base fluid (amount one unit), or EMPTY when it uses no tank fluid. */
	public static FluidStack get(KettleRecipe recipe) {
		FluidStack base = BASES.get(recipe);
		if (base != null) return base;
		return recipe.getNeedWater() ? new FluidStack(Fluids.WATER, KettleTank.UNIT) : FluidStack.EMPTY;
	}

	public static void set(KettleRecipe recipe, FluidStack base) {
		BASES.put(recipe, base);
	}

	/** Whether the JSON names a base (or "none"); false for a 2.0 recipe. */
	public static boolean hasBaseKey(JsonObject json) {
		return json.has("base");
	}

	/** The JSON's base: water for a 2.0 recipe with needwater true, EMPTY for "none" or needwater false. */
	public static FluidStack read(ResourceLocation id, JsonObject json) {
		if (!json.has("base")) {
			return GsonHelper.getAsBoolean(json, "needwater", true) ? new FluidStack(Fluids.WATER, KettleTank.UNIT) : FluidStack.EMPTY;
		}
		JsonElement el = json.get("base");
		if (el.isJsonPrimitive() && "none".equals(el.getAsString())) return FluidStack.EMPTY;
		if (!el.isJsonObject()) throw new JsonParseException("Kettle recipe base must be an object or \"none\"");
		JsonObject base = el.getAsJsonObject();
		ResourceLocation fluidId = new ResourceLocation(GsonHelper.getAsString(base, "fluid"));
		Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidId);
		if (fluid == null || fluid == Fluids.EMPTY) throw new JsonParseException("Unknown base fluid " + fluidId);
		int amount = GsonHelper.getAsInt(base, "count", GsonHelper.getAsInt(base, "amount", KettleTank.UNIT));
		if (amount != KettleTank.UNIT) {
			LOGGER.warn("[creativedelight] Kettle recipe {}: base amount {} mB read as one unit ({} mB)", id, amount, KettleTank.UNIT);
		}
		FluidStack stack = new FluidStack(fluid, KettleTank.UNIT);
		if (base.has("nbt")) {
			try {
				stack.setTag(TagParser.parseTag(GsonHelper.getAsString(base, "nbt")));
			} catch (Exception e) {
				throw new JsonParseException("Bad base nbt in " + id + ": " + e.getMessage());
			}
		}
		return stack;
	}

	public static void write(FriendlyByteBuf buf, FluidStack base) {
		buf.writeBoolean(!base.isEmpty());
		if (!base.isEmpty()) base.writeToPacket(buf);
	}

	public static FluidStack readNet(FriendlyByteBuf buf) {
		return buf.readBoolean() ? FluidStack.readFromPacket(buf) : FluidStack.EMPTY;
	}
}
