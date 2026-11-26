// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativeoxidation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

/**
 * The {@code creativeoxidation} section of Creative Devtools' {@code /dumpworldjei} tool_pairs dump (P8), read by
 * tools/jei_world/tab_tools.py (Waxing and Scraping, Soap Washing) and tab_weathering.py (Lightning Conversion).
 * Creative Devtools calls {@link #dump()} by reflection, so it needs no compile dependency on this member.
 *
 * <pre>
 * {"enabled": bool, "items": [item id, ...]   (OxidationCreativeTab.targetItems order),
 *  "stage_key": "CreativeOxidationStage", "waxed_key": "CreativeOxidationWaxed",
 *  "transitions": [{"change": "honeycomb|scrape|lightning|soap",
 *                   "from": {"stage": s, "waxed": b, "nbt": SNBT?}, "to": {...}}]}   (no "nbt": the plain item)
 * </pre>
 */
public final class OxidationJeiData {
	private OxidationJeiData() {
	}

	public static JsonObject dump() {
		JsonObject out = new JsonObject();
		out.addProperty("enabled", OxidationConfig.enabled());
		out.addProperty("stage_key", OxidationItemData.STAGE_KEY);
		out.addProperty("waxed_key", OxidationItemData.WAXED_KEY);
		out.addProperty("supplementaries", ModList.get().isLoaded("supplementaries"));
		JsonArray items = new JsonArray();
		for (Item item : OxidationCreativeTab.targetItems()) items.add(Registry.ITEM.getKey(item).toString());
		out.add("items", items);
		JsonArray transitions = new JsonArray();
		for (OxidationRules.Transition t : OxidationRules.transitions()) {
			JsonObject o = new JsonObject();
			o.addProperty("change", t.change().name().toLowerCase(java.util.Locale.ROOT));
			o.add("from", state(t.fromStage(), t.fromWaxed()));
			o.add("to", state(t.toStage(), t.toWaxed()));
			transitions.add(o);
		}
		out.add("transitions", transitions);
		return out;
	}

	/** Stage, wax and the item NBT a stack in that state carries (none for the plain state). */
	static JsonObject state(OxidationStage stage, boolean waxed) {
		JsonObject o = new JsonObject();
		o.addProperty("stage", stage.getSerializedName());
		o.addProperty("waxed", waxed);
		ItemStack probe = OxidationItemData.staged(new ItemStack(net.minecraft.world.item.Items.STICK), stage, waxed);
		CompoundTag tag = probe.getTag();
		if (tag != null) o.addProperty("nbt", tag.toString());
		return o;
	}
}
