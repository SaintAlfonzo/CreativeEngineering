package com.SaintAlfonzo.creativecommon.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Loot tables that name optional mods (plan_cb_publish.md M2 rule G5, plan_cb_polish.md 3, scratch r1106cbo): Forge
 * 1.19.2 has no conditions on loot tables, so a table naming an absent mod's item fails to parse and logs an error per
 * table per load (Vintage's Fruits Delight jam jars, placed foods of More Delight, the fishing net's driftwood). Before
 * the tables are parsed (m_5787_ = apply):
 * <ul>
 *   <li>a top-level {@code "forge:conditions"} array is honoured like a recipe's: false drops the table;</li>
 *   <li>an item entry whose item is not registered and whose mod is not loaded is dropped (alternatives, groups and
 *       sequences are walked). A missing item of a loaded mod still fails as before, so pack bugs stay visible.</li>
 * </ul>
 * One info line per reload counts what was dropped, per mod.
 */
@Mixin(LootTables.class)
public class LootTablesOptionalModsMixin {

	@ModifyVariable(method = "m_5787_(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
			at = @At("HEAD"), argsOnly = true, remap = false)
	private Map<ResourceLocation, JsonElement> creativecommon$optionalModEntries(Map<ResourceLocation, JsonElement> tables) {
		Map<String, Integer> entries = new TreeMap<>();
		int tablesDropped = 0;
		Iterator<Map.Entry<ResourceLocation, JsonElement>> it = tables.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<ResourceLocation, JsonElement> e = it.next();
			if (!e.getValue().isJsonObject()) continue;
			JsonObject table = e.getValue().getAsJsonObject();
			try {
				if (table.has("forge:conditions") && table.get("forge:conditions").isJsonArray()) {
					if (!CraftingHelper.processConditions(table.getAsJsonArray("forge:conditions"), ICondition.IContext.EMPTY)) {
						it.remove();
						tablesDropped++;
						continue;
					}
					table.remove("forge:conditions");
				}
				if (table.has("pools") && table.get("pools").isJsonArray()) {
					for (JsonElement pool : table.getAsJsonArray("pools")) {
						if (pool.isJsonObject() && pool.getAsJsonObject().has("entries"))
							creativecommon$prune(pool.getAsJsonObject().get("entries"), entries);
					}
				}
			} catch (RuntimeException ex) {
				// leave the table as it was: vanilla reports whatever is wrong with it
			}
		}
		if (tablesDropped > 0 || !entries.isEmpty())
			LogUtils.getLogger().info("[absent mods] loot tables: {} left out by forge:conditions, item entries dropped {}",
					tablesDropped, entries.isEmpty() ? "none" : entries);
		return tables;
	}

	private static void creativecommon$prune(JsonElement list, Map<String, Integer> counts) {
		if (list == null || !list.isJsonArray()) return;
		JsonArray arr = list.getAsJsonArray();
		for (int i = arr.size() - 1; i >= 0; i--) {
			if (!arr.get(i).isJsonObject()) continue;
			JsonObject entry = arr.get(i).getAsJsonObject();
			if (entry.has("children")) creativecommon$prune(entry.get("children"), counts);
			String type = entry.has("type") && entry.get("type").isJsonPrimitive() ? entry.get("type").getAsString() : "";
			if (!type.equals("minecraft:item") && !type.equals("item")) continue;
			if (!entry.has("name") || !entry.get("name").isJsonPrimitive()) continue;
			ResourceLocation id = ResourceLocation.tryParse(entry.get("name").getAsString());
			if (id == null || ForgeRegistries.ITEMS.containsKey(id) || ModList.get().isLoaded(id.getNamespace())) continue;
			arr.remove(i);
			counts.merge(id.getNamespace(), 1, Integer::sum);
		}
	}
}
