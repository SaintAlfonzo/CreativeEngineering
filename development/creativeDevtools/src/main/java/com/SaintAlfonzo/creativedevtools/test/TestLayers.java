package com.SaintAlfonzo.creativedevtools.test;

import com.google.gson.GsonBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * {@code /cdevtest layerdump}: step D0 of plan_post_scratch_1115b.md. Every registered block with an integer property
 * named layers, layer, height, snow_layers or thickness, with min, max and the collision and outline height (in
 * pixels, 0 to 16) at each value, written to {@code kubejs/exported/passthrough_layers_dump.json}. Each block is
 * classed against the live tag {@code creativecompat:passthrough_layers} and the D1 table's "out" rows:
 * {@code in_tag}, {@code d1_out}, or {@code new} (not in the table: a question for the owner). Tag members without such
 * a property are listed as {@code tag_without_property}. Shapes are read with an empty world (EmptyBlockGetter); a block
 * whose shape needs a real world prints {@code shape_error}. Needs no Creative Compat (the tag is then empty).
 */
final class TestLayers {

	static final Set<String> NAMES = Set.of("layers", "layer", "height", "snow_layers", "thickness");
	static final ResourceLocation TAG = new ResourceLocation("creativecompat", "passthrough_layers");
	/** D1 rows marked out (owner): Create Deco coin stacks, Control Engineering logic cabinet, Copycats. */
	static final List<Pattern> D1_OUT = List.of(Pattern.compile("^createdeco:[a-z_]*coinstack[a-z_]*$"),
			Pattern.compile("^controlengineering:logic_cabinet$"), Pattern.compile("^copycats:"));

	private TestLayers() {
	}

	static void register() {
		TestCommand.add("layerdump", TestLayers::dump);
	}

	static String classify(String id, boolean inTag) {
		if (inTag) return "in_tag";
		for (Pattern p : D1_OUT) if (p.matcher(id).find()) return "d1_out";
		return "new";
	}

	private static double px(VoxelShape s) {
		return s.isEmpty() ? 0 : Math.round(s.max(Direction.Axis.Y) * 16 * 100) / 100.0;
	}

	private static void dump(TestCtx c) throws Exception {
		c.args.end();
		c.args.checkOpts();
		TagKey<Block> tag = TagKey.create(Registry.BLOCK_REGISTRY, TAG);
		Map<String, Object> blocks = new TreeMap<>();
		Map<String, Integer> counts = new TreeMap<>();
		List<String> tagWithout = new ArrayList<>();
		for (Block block : ForgeRegistries.BLOCKS.getValues()) {
			String id = String.valueOf(ForgeRegistries.BLOCKS.getKey(block));
			BlockState def = block.defaultBlockState();
			boolean inTag = def.is(tag);
			IntegerProperty prop = null;
			for (Property<?> p : def.getProperties()) {
				if (p instanceof IntegerProperty ip && NAMES.contains(p.getName())) prop = ip;
			}
			if (prop == null) {
				if (inTag) tagWithout.add(id);
				continue;
			}
			String cls = classify(id, inTag);
			counts.merge(cls, 1, Integer::sum);
			List<Integer> vals = new ArrayList<>(prop.getPossibleValues());
			vals.sort(null);
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("property", prop.getName());
			row.put("min", vals.get(0));
			row.put("max", vals.get(vals.size() - 1));
			row.put("class", cls);
			row.put("java_class", block.getClass().getName());
			Map<String, Object> heights = new LinkedHashMap<>();
			StringBuilder col = new StringBuilder();
			String err = null;
			for (int v : vals) {
				try {
					BlockState st = def.setValue(prop, v);
					double ch = px(st.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty()));
					double oh = px(st.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty()));
					heights.put(String.valueOf(v), Map.of("collision", ch, "outline", oh));
					col.append(col.length() == 0 ? "" : ",").append(v).append(':').append(TestLine.format(ch));
				} catch (VirtualMachineError e) {
					throw e;
				} catch (Throwable t) {
					err = t.getClass().getSimpleName();
					heights.put(String.valueOf(v), Map.of("error", err));
				}
			}
			row.put("heights_px", heights);
			blocks.put(id, row);
			TestLine l = c.line().kv("block", id).kv("property", prop.getName()).kv("min", vals.get(0))
					.kv("max", vals.get(vals.size() - 1)).kv("class", cls).kv("collision_px", col.length() == 0 ? "-" : col.toString());
			if (err != null) l.kv("shape_error", err);
			c.emit(l);
		}
		for (String id : tagWithout) c.emit(c.line().kv("block", id).kv("class", "tag_without_property"));
		Map<String, Object> out = new LinkedHashMap<>();
		out.put("note", "plan_post_scratch_1115b.md D0; written by /cdevtest layerdump; heights in pixels (16 = full block)");
		out.put("tag", TAG.toString());
		out.put("counts", counts);
		out.put("tag_without_property", tagWithout);
		out.put("blocks", blocks);
		Path dir = FMLPaths.GAMEDIR.get().resolve("kubejs/exported");
		Files.createDirectories(dir);
		Path file = dir.resolve("passthrough_layers_dump.json");
		Files.writeString(file, new GsonBuilder().setPrettyPrinting().create().toJson(out), StandardCharsets.UTF_8);
		c.emit(c.line().kv("blocks", blocks.size()).map("classes", counts).kv("tag_without_property", tagWithout.size())
				.kv("file", "kubejs/exported/passthrough_layers_dump.json"));
	}
}
