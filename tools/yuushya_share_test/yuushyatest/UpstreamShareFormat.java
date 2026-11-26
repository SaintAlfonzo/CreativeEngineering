// SPDX-License-Identifier: GPL-3.0-only
package yuushyatest;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Writes the upstream share-code format: Yuushya Modelling 2.4.1's own share records (utils/ShareUtils, same names,
 * field order and Gson settings) serialized by Gson 2.10.1, the Gson of Minecraft 1.20.1, which handles records. Run
 * with gson-2.10.1 on the classpath; args: output directory. ShareUtilsTest then reads these files under Gson 2.8.9.
 */
public final class UpstreamShareFormat {
	static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

	record ShareBlockInformation(Set<String> mods, List<ShareData> blocks) {
		record ShareData(List<Double> pos, List<Float> rot, List<Float> scales, ShareBlockState blockState, boolean isShown) {
			record ShareBlockState(String name, Map<String, String> properties) {
			}
		}
	}

	record ShareItemInformation(Set<String> mods, List<ItemShareData> items) {
		record ItemShareData(List<Double> pos, List<Float> rot, List<Float> scales, ShareItemStack itemStack, int color, boolean isShown,
				boolean enableBlock) {
			record ShareItemStack(String name, JsonElement jsonElement) {
			}
		}
	}

	public static void main(String[] args) throws Exception {
		Map<String, String> props = new LinkedHashMap<>();
		props.put("facing", "north");
		props.put("waterlogged", "false");
		Set<String> blockMods = new LinkedHashSet<>(List.of("create"));
		ShareBlockInformation blocks = new ShareBlockInformation(blockMods, List.of(
				new ShareBlockInformation.ShareData(List.of(0.5, -0.25, 1.0), List.of(0.0F, 90.0F, 0.0F), List.of(1.0F, 0.5F, 2.0F),
						new ShareBlockInformation.ShareData.ShareBlockState("create:andesite_casing", props), true),
				new ShareBlockInformation.ShareData(List.of(0.0, 0.0, 0.0), List.of(0.0F, 0.0F, 0.0F), List.of(1.0F, 1.0F, 1.0F),
						new ShareBlockInformation.ShareData.ShareBlockState("minecraft:grass_block", Map.of()), false)));
		ShareItemInformation items = new ShareItemInformation(new LinkedHashSet<>(), List.of(
				new ShareItemInformation.ItemShareData(List.of(0.0, 0.125, 0.0), List.of(45.0F, 0.0F, 0.0F), List.of(1.0F, 1.0F, 1.0F),
						new ShareItemInformation.ItemShareData.ShareItemStack("minecraft:grass_block",
								new JsonPrimitive("{Count:1b,id:\"minecraft:grass_block\"}")),
						-16711936, true, false)));
		Path out = Path.of(args[0]);
		Files.writeString(out.resolve("upstream_block.json"), GSON.toJson(blocks, ShareBlockInformation.class));
		Files.writeString(out.resolve("upstream_item.json"), GSON.toJson(items, ShareItemInformation.class));
	}
}
