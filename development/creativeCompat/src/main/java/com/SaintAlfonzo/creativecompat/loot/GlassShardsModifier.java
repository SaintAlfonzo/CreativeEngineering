// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecommon.loot;

import com.SaintAlfonzo.creativecommon.CreativeCommon;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Glass shards (TODO gameplay/misc #22, plan_wood_bucket_batch_1121.md R3, D1; feature_writeup/glass_shards.md).
 * Every block in {@code #forge:glass} or {@code #forge:glass_panes} (block or item tag, so modded glass counts) that
 * breaks without Silk Touch and would drop nothing of itself drops Quark shards of its color: {@code block_min} to
 * {@code block_max} for a block, {@code pane_min} to {@code pane_max} for a pane. Color: the
 * {@code forge:glass/<color>} or {@code forge:glass_panes/<color>} tag, else a {@code <color>_} word in the block's id,
 * else the clear shard.
 * <p>
 * Left alone: a Silk Touch break; a drop list that already holds a shard ({@code #quark:shards}: Quark's own tables
 * for vanilla glass, stained glass and dirty glass give 2 to 4) or the block's own item (glass that drops itself,
 * such as framed glass); a loot context without a block state; any run without Quark's shard items.
 * <p>
 * Tinted glass ({@code forge:glass/tinted}, {@code forge:glass_panes/tinted} or {@code tinted} in the id) has no shard of
 * its own: it loses its self-drop and gives Quark's dirty shard instead (plan_wood_bucket_batch_1121.md R19).
 * Explosions keep each shard at 1 / radius, like {@code explosion_decay}.
 * Definition {@code data/creativecommon/loot_modifiers/glass_shards.json}, listed in Forge's
 * {@code data/forge/loot_modifiers/global_loot_modifiers.json} ({@code replace: false}, so it loads without any other
 * Creative mod) and in Common's {@code glm_entries.json} fragment (Creative Loot's GlmMerge keeps it when another
 * jar's list replaces Forge's; duplicates are dropped by id).
 */
public class GlassShardsModifier extends LootModifier {

	private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> SERIALIZERS =
			DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, CreativeCommon.MODID);

	public static final Supplier<Codec<GlassShardsModifier>> CODEC = Suppliers.memoize(() ->
			RecordCodecBuilder.create(inst -> codecStart(inst)
					.and(Codec.INT.fieldOf("block_min").forGetter(m -> m.blockMin))
					.and(Codec.INT.fieldOf("block_max").forGetter(m -> m.blockMax))
					.and(Codec.INT.fieldOf("pane_min").forGetter(m -> m.paneMin))
					.and(Codec.INT.fieldOf("pane_max").forGetter(m -> m.paneMax))
					.apply(inst, GlassShardsModifier::new)));

	static {
		SERIALIZERS.register("glass_shards", CODEC);
	}

	private static final TagKey<Block> GLASS = blockTag("glass");
	private static final TagKey<Block> PANES = blockTag("glass_panes");
	private static final TagKey<Item> GLASS_ITEM = itemTag("forge", "glass");
	private static final TagKey<Item> PANES_ITEM = itemTag("forge", "glass_panes");
	private static final TagKey<Item> SHARDS = itemTag("quark", "shards");
	/** Longest name first, so light_blue wins over blue in an id. */
	private static final DyeColor[] COLORS = Arrays.stream(DyeColor.values())
			.sorted(Comparator.comparingInt((DyeColor c) -> -c.getName().length())).toArray(DyeColor[]::new);

	private final int blockMin, blockMax, paneMin, paneMax;

	public GlassShardsModifier(LootItemCondition[] conditions, int blockMin, int blockMax, int paneMin, int paneMax) {
		super(conditions);
		this.blockMin = blockMin;
		this.blockMax = Math.max(blockMin, blockMax);
		this.paneMin = paneMin;
		this.paneMax = Math.max(paneMin, paneMax);
	}

	public static void register(IEventBus modEventBus) {
		SERIALIZERS.register(modEventBus);
	}

	private static TagKey<Block> blockTag(String path) {
		return TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation("forge", path));
	}

	private static TagKey<Item> itemTag(String ns, String path) {
		return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(ns, path));
	}

	@Override
	@Nonnull
	protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
		BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
		if (state == null) return loot;
		Block block = state.getBlock();
		// Only the block's own table (not a chest or fishing context that happens to carry a state)
		if (!block.getLootTable().equals(context.getQueriedLootTableId())) return loot;
		Item own = block.asItem();
		boolean pane = state.is(PANES) || (own != Items.AIR && own.builtInRegistryHolder().is(PANES_ITEM));
		boolean glass = pane || state.is(GLASS) || (own != Items.AIR && own.builtInRegistryHolder().is(GLASS_ITEM));
		if (!glass) return loot;
		ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
		if (tool != null && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0) return loot;
		boolean tinted = isTinted(state, pane);
		if (tinted && own != Items.AIR && quarkItem("dirty_shard") != null) loot.removeIf(s -> s.is(own));
		for (ItemStack s : loot) {
			if (s.is(SHARDS) || (own != Items.AIR && s.is(own))) return loot;
		}
		Item shard = tinted ? quarkItem("dirty_shard") : shardFor(state, pane);
		if (shard == null) return loot;
		RandomSource random = context.getRandom();
		int count = pane ? paneMin + random.nextInt(paneMax - paneMin + 1) : blockMin + random.nextInt(blockMax - blockMin + 1);
		Float radius = context.getParamOrNull(LootContextParams.EXPLOSION_RADIUS);
		if (radius != null && radius > 0) {
			int kept = 0;
			for (int n = 0; n < count; n++) if (random.nextFloat() <= 1.0F / radius) kept++;
			count = kept;
		}
		if (count > 0) loot.add(new ItemStack(shard, count));
		return loot;
	}

	private static boolean isTinted(BlockState state, boolean pane) {
		String sub = (pane ? "glass_panes/" : "glass/") + "tinted";
		Item own = state.getBlock().asItem();
		if (state.is(blockTag(sub)) || (own != Items.AIR && own.builtInRegistryHolder().is(itemTag("forge", sub)))) return true;
		ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
		return id != null && ("_" + id.getPath() + "_").contains("_tinted_");
	}

	@Nullable
	private static Item shardFor(BlockState state, boolean pane) {
		DyeColor color = null;
		Item own = state.getBlock().asItem();
		for (DyeColor c : COLORS) {
			String sub = (pane ? "glass_panes/" : "glass/") + c.getName();
			if (state.is(blockTag(sub)) || (own != Items.AIR && own.builtInRegistryHolder().is(itemTag("forge", sub)))) {
				color = c;
				break;
			}
		}
		if (color == null) {
			ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
			String path = id == null ? "" : "_" + id.getPath() + "_";
			for (DyeColor c : COLORS) {
				if (path.contains("_" + c.getName() + "_")) {
					color = c;
					break;
				}
			}
		}
		Item shard = quarkItem(color == null ? "clear_shard" : color.getName() + "_shard");
		return shard != null ? shard : quarkItem("clear_shard");
	}

	@Nullable
	private static Item quarkItem(String path) {
		Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("quark", path));
		return item == null || item == Items.AIR ? null : item;
	}

	@Override
	public Codec<? extends IGlobalLootModifier> codec() {
		return CODEC.get();
	}
}
