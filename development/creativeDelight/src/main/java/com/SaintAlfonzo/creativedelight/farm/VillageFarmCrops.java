package com.SaintAlfonzo.creativedelight.farm;

import com.SaintAlfonzo.creativedelight.CDConfig;
import com.SaintAlfonzo.creativedelight.CDResources;
import com.SaintAlfonzo.creativedelight.CreativeDelight;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Village farms grow more crops (owner 2026-10-25, feature_writeup/crops.md 2.11, creative_delight.md 25). The rules are
 * {@code creativedelight/village_farms.json} in this jar: climate groups of crop blocks, and the farm templates that
 * belong to each group. At server start the processor is appended to the pool elements of those templates (the vanilla
 * {@code farm_*} lists are shared by nothing else, but most modded farms use {@code minecraft:empty}, so a list cannot be
 * the hook). When a farm piece is placed, every field (all blocks of one vanilla crop in that piece) is rolled with the
 * file's share; a rolled field is split 50/50 between the original crop and one crop of the group, along the field's
 * longer axis (1.19.2 has no whole-piece hook, so the roll is seeded and each block reaches the same answer). Crops are found by block id, so an entry whose mod is not loaded is skipped. Switched by
 * {@code [worldgen] wildCrops}. The growth stage keeps its share of full growth.
 */
public final class VillageFarmCrops extends StructureProcessor {
	public static final Codec<VillageFarmCrops> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.STRING.fieldOf("group").forGetter(p -> p.group)).apply(i, VillageFarmCrops::new));
	public static final DeferredRegister<StructureProcessorType<?>> PROCESSORS =
			DeferredRegister.create(Registry.STRUCTURE_PROCESSOR_REGISTRY, CreativeDelight.MOD_ID);
	public static final RegistryObject<StructureProcessorType<VillageFarmCrops>> TYPE =
			PROCESSORS.register("village_farm_crops", () -> () -> CODEC);
	/**
	 * Load-only alias for the id this type had before the mod id lost its underscore (creative_split.md 7.6). Farm pieces
	 * saved in structure starts carry the processor inline by type id, and the processor registry is vanilla (no Forge
	 * remap, so the swap list cannot reach it). A saved piece decodes through this alias; the processor it builds reports
	 * {@link #TYPE}, so the start saves under the new id. Never prune.
	 */
	public static final DeferredRegister<StructureProcessorType<?>> LEGACY_PROCESSORS =
			DeferredRegister.create(Registry.STRUCTURE_PROCESSOR_REGISTRY, "creative_delight");
	public static final RegistryObject<StructureProcessorType<VillageFarmCrops>> LEGACY_TYPE =
			LEGACY_PROCESSORS.register("village_farm_crops", () -> () -> CODEC);

	private static final String RULES_PATH = "creativedelight/village_farms.json";
	private static final String AUTO = "auto";
	private static final Set<Block> BASE_CROPS = Set.of(Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.BEETROOTS);

	/** Parsed file: share, crop ids per group, group per template. */
	private record Rules(float share, Map<String, List<ResourceLocation>> groups, Map<ResourceLocation, String> templates) {}

	private static volatile Rules rules;

	private final String group;

	public VillageFarmCrops(String group) {
		this.group = group;
	}

	private static Rules rules() {
		Rules r = rules;
		if (r == null) {
			r = rules = load();
		}
		return r;
	}

	private static Rules load() {
		String text = CDResources.text(RULES_PATH);
		if (text == null) {
			CreativeDelight.LOGGER.error("[creativedelight] {} is missing from the jar; village farms stay as they are", RULES_PATH);
			return new Rules(0F, Map.of(), Map.of());
		}
		JsonObject root = JsonParser.parseString(text).getAsJsonObject();
		Map<String, List<ResourceLocation>> groups = new HashMap<>();
		for (var e : root.getAsJsonObject("groups").entrySet()) {
			List<ResourceLocation> ids = new ArrayList<>();
			for (JsonElement id : e.getValue().getAsJsonArray()) {
				ids.add(new ResourceLocation(id.getAsString()));
			}
			groups.put(e.getKey(), List.copyOf(ids));
		}
		Map<ResourceLocation, String> templates = new HashMap<>();
		for (var e : root.getAsJsonObject("templates").entrySet()) {
			templates.put(new ResourceLocation(e.getKey()), e.getValue().getAsString());
		}
		return new Rules(root.get("share").getAsFloat(), groups, templates);
	}

	/** The group's crop blocks that are loaded and have a growth state or a plain default state. */
	private static List<Block> blocks(Rules rules, String group) {
		List<Block> out = new ArrayList<>();
		for (ResourceLocation id : rules.groups().getOrDefault(group, List.of())) {
			if (ForgeRegistries.BLOCKS.containsKey(id)) {
				out.add(ForgeRegistries.BLOCKS.getValue(id));
			}
		}
		return out;
	}

	/** Climate group of a biome for templates marked auto, from its temperature and rainfall. */
	private static String groupOf(Holder<Biome> biome) {
		Biome b = biome.value();
		float temp = b.getBaseTemperature();
		if (temp >= 0.95F) {
			return b.getDownfall() >= 0.5F ? "warm_wet" : "warm_dry";
		}
		if (temp <= 0.1F) return "snowy";
		if (temp <= 0.5F) return "cold";
		return "temperate";
	}

	private static IntegerProperty ageOf(BlockState state) {
		for (Property<?> p : state.getProperties()) {
			if (p instanceof IntegerProperty ip && p.getName().equals("age")) return ip;
		}
		return null;
	}

	private static BlockState atStage(Block to, float fraction) {
		BlockState state = to.defaultBlockState();
		IntegerProperty age = ageOf(state);
		if (age == null) return state;
		int min = age.getPossibleValues().stream().min(Integer::compare).orElse(0);
		int max = age.getPossibleValues().stream().max(Integer::compare).orElse(0);
		return state.setValue(age, min + Math.round(fraction * (max - min)));
	}

	/** x/z extent of one vanilla crop's blocks in a placed piece: {minX, maxX, minZ, maxZ}. */
	private record Extent(StructureTemplate template, BlockPos offset, Block block, int[] box) {}

	private static final ThreadLocal<Extent> LAST_EXTENT = new ThreadLocal<>();

	private static int[] extent(StructureTemplate template, BlockPos offset, StructurePlaceSettings settings, Block block) {
		Extent last = LAST_EXTENT.get();
		if (last != null && last.template == template && last.block == block && last.offset.equals(offset)) {
			return last.box;
		}
		int[] box = {Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE};
		for (StructureBlockInfo info : template.filterBlocks(offset, settings, block)) {
			box[0] = Math.min(box[0], info.pos.getX());
			box[1] = Math.max(box[1], info.pos.getX());
			box[2] = Math.min(box[2], info.pos.getZ());
			box[3] = Math.max(box[3], info.pos.getZ());
		}
		LAST_EXTENT.set(new Extent(template, offset, block, box));
		return box;
	}

	/**
	 * The field (every block of one vanilla crop in this piece) is rolled once: the roll is seeded by the piece origin and
	 * the crop, so each block of the field reaches the same answer. A rolled field keeps one half of its original crop and
	 * grows the group's crop in the other, halves cut across the field's longer axis.
	 */
	@Override
	public StructureBlockInfo process(LevelReader level, BlockPos offset, BlockPos pos, StructureBlockInfo original, StructureBlockInfo current,
									  StructurePlaceSettings settings, StructureTemplate template) {
		Block base = current.state.getBlock();
		Rules rules = rules();
		if (!BASE_CROPS.contains(base) || rules.share() <= 0F || !CDConfig.worldgen(CDConfig.WILD_CROPS)) {
			return current;
		}
		String g = AUTO.equals(group) ? groupOf(level.getBiome(current.pos)) : group;
		List<Block> options = blocks(rules, g);
		if (options.isEmpty()) {
			return current;
		}
		RandomSource random = RandomSource.create(Mth.getSeed(offset) * 31L + ForgeRegistries.BLOCKS.getKey(base).hashCode());
		if (random.nextFloat() >= rules.share()) {
			return current;
		}
		Block to = options.get(random.nextInt(options.size()));
		boolean changeFirstHalf = random.nextBoolean();
		int[] box = extent(template, offset, settings, base);
		boolean alongX = box[1] - box[0] >= box[3] - box[2];
		int twice = alongX ? 2 * current.pos.getX() : 2 * current.pos.getZ();
		int middle = alongX ? box[0] + box[1] : box[2] + box[3];
		boolean inFirstHalf = twice <= middle;
		if (inFirstHalf != changeFirstHalf) {
			return current;
		}
		IntegerProperty fromAge = ageOf(current.state);
		float fraction = fromAge == null ? 1F : (float) current.state.getValue(fromAge) / fromAge.getPossibleValues().stream().max(Integer::compare).orElse(1);
		return new StructureBlockInfo(current.pos, atStage(to, fraction), current.nbt);
	}

	@Override
	protected StructureProcessorType<?> getType() {
		return TYPE.get();
	}

	/** Appends the processor to the pool elements of the listed farm templates, once per server. */
	public static void onServerAboutToStart(ServerAboutToStartEvent event) {
		Rules rules = rules();
		if (rules.templates().isEmpty()) {
			return;
		}
		var pools = event.getServer().registryAccess().registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
		RandomSource random = RandomSource.create();
		int elements = 0;
		int failed = 0;
		for (StructureTemplatePool pool : pools) {
			for (StructurePoolElement element : new ArrayList<>(pool.getShuffledTemplates(random))) {
				if (!(element instanceof SinglePoolElement)) {
					continue;
				}
				Either<ResourceLocation, ?> template = ObfuscationReflectionHelper.getPrivateValue(SinglePoolElement.class, (SinglePoolElement) element, "f_210411_");
				if (template == null || template.left().isEmpty()) {
					continue;
				}
				String group = rules.templates().get(template.left().get());
				if (group == null) {
					continue;
				}
				Holder<StructureProcessorList> current = ObfuscationReflectionHelper.getPrivateValue(SinglePoolElement.class, (SinglePoolElement) element, "f_210412_");
				if (current == null || current.value().list().stream().anyMatch(p -> p instanceof VillageFarmCrops)) {
					continue;
				}
				try {
					List<StructureProcessor> list = ImmutableList.<StructureProcessor>builder().addAll(current.value().list()).add(new VillageFarmCrops(group)).build();
					ObfuscationReflectionHelper.setPrivateValue(SinglePoolElement.class, (SinglePoolElement) element, Holder.direct(new StructureProcessorList(list)), "f_210412_");
					elements++;
				} catch (RuntimeException e) {
					failed++;
				}
			}
		}
		CreativeDelight.LOGGER.info("[creativedelight] village farm crops added to {} pool elements of {} listed farm templates{}", elements,
				rules.templates().size(), failed == 0 ? "" : " (" + failed + " failed)");
	}
}
