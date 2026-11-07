package com.SaintAlfonzo.creativedelight.loot;

import com.SaintAlfonzo.creativedelight.CreativeDelight;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Loot condition {@code creativeengineering:biome_tag}: passes when the biome at the context's origin is in
 * {@code "tag"}. Vanilla 1.19.2 {@code location_check} only takes a single biome id, so biome groups (warm
 * oceans, rivers) would otherwise be long alternative lists that miss modded biomes.
 * <pre>{"condition": "creativeengineering:biome_tag", "tag": "creativeengineering:fishing_net/river"}</pre>
 * Used by the fishing net tables (kubejs/data/creativedelight/loot_tables/gameplay/fishing_net/). Moved from Creative
 * Worldgen on 2026-11-06: Creative Delight does not need Worldgen, and the tables failed to parse without it.
 */
public class BiomeTagCondition implements LootItemCondition {

	public static final DeferredRegister<LootItemConditionType> CONDITIONS =
			DeferredRegister.create(Registry.LOOT_ITEM_REGISTRY, CreativeDelight.NS);
	public static final RegistryObject<LootItemConditionType> TYPE =
			CONDITIONS.register("biome_tag", () -> new LootItemConditionType(new Serializer()));

	public static void register(IEventBus modBus) {
		CONDITIONS.register(modBus);
	}

	private final TagKey<Biome> tag;

	public BiomeTagCondition(TagKey<Biome> tag) {
		this.tag = tag;
	}

	@Override
	public LootItemConditionType getType() {
		return TYPE.get();
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return Set.of(LootContextParams.ORIGIN);
	}

	@Override
	public boolean test(LootContext ctx) {
		Vec3 origin = ctx.getParamOrNull(LootContextParams.ORIGIN);
		return origin != null && ctx.getLevel().getBiome(new BlockPos(origin)).is(tag);
	}

	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<BiomeTagCondition> {
		@Override
		public void serialize(JsonObject json, BiomeTagCondition condition, JsonSerializationContext ctx) {
			json.addProperty("tag", condition.tag.location().toString());
		}

		@Override
		public BiomeTagCondition deserialize(JsonObject json, JsonDeserializationContext ctx) {
			String id = GsonHelper.getAsString(json, "tag");
			if (id.startsWith("#")) id = id.substring(1);
			return new BiomeTagCondition(TagKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(id)));
		}
	}
}
