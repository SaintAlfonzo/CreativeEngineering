package com.sammy.minersdelight.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.sammy.minersdelight.registry.MDLoot;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import java.util.Set;

/** Loot condition {@code miners_delight:block_tag}: the broken block is in {@code block_tag}. */
public record BlockTagCondition(TagKey<Block> tag) implements LootItemCondition {
    @Override
    public LootItemConditionType getType() {
        return MDLoot.BLOCK_TAG.get();
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.BLOCK_STATE);
    }

    @Override
    public boolean test(LootContext context) {
        BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        return state != null && state.is(tag);
    }

    public static class JsonSerializer implements Serializer<BlockTagCondition> {
        @Override
        public void serialize(JsonObject json, BlockTagCondition condition, JsonSerializationContext context) {
            json.addProperty("block_tag", condition.tag.location().toString());
        }

        @Override
        public BlockTagCondition deserialize(JsonObject json, JsonDeserializationContext context) {
            return new BlockTagCondition(TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(GsonHelper.getAsString(json, "block_tag"))));
        }
    }
}
