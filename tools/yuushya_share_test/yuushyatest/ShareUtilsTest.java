// SPDX-License-Identifier: GPL-3.0-only
package yuushyatest;

import com.google.gson.Gson;
import com.yuushya.modelling.utils.ShareUtils;
import com.yuushya.modelling.utils.ShareUtils.ShareBlockInformation;
import com.yuushya.modelling.utils.ShareUtils.ShareBlockInformation.ShareData;
import com.yuushya.modelling.utils.ShareUtils.ShareBlockInformation.ShareData.ShareBlockState;
import com.yuushya.modelling.utils.ShareUtils.ShareItemInformation;
import com.yuushya.modelling.utils.ShareUtils.ShareItemInformation.ItemShareData;
import com.yuushya.modelling.utils.ShareUtils.ShareItemInformation.ItemShareData.ShareItemStack;
import com.google.gson.JsonPrimitive;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Headless check of the Yuushya Modelling backport's share codes (plan_yuushya_backport.md A11, PORT_NOTES section 3):
 * ShareUtils' share types round-trip under Gson 2.8.9 (Minecraft 1.19.2's Gson, first on the classpath), read the
 * upstream 2.4.1 format (written by UpstreamShareFormat under Gson 2.10.1) and write it back byte-identical. Upstream's
 * types were records, which Gson 2.8.9 cannot fill (AssertionError on every Entity Block or Item Block removal).
 * Args: the directory holding upstream_block.json and upstream_item.json.
 */
public final class ShareUtilsTest {
	// The upstream format pinned as text, so a change to the mirror records in UpstreamShareFormat shows up here too.
	static final String BLOCK_LITERAL = "{\"Mods\":[\"create\"],\"Blocks\":[{\"Pos\":[0.5,-0.25,1.0],\"Rot\":[0.0,90.0,0.0],"
			+ "\"Scales\":[1.0,0.5,2.0],\"BlockState\":{\"Name\":\"create:andesite_casing\",\"Properties\":{\"facing\":\"north\","
			+ "\"waterlogged\":\"false\"}},\"IsShown\":true},{\"Pos\":[0.0,0.0,0.0],\"Rot\":[0.0,0.0,0.0],\"Scales\":[1.0,1.0,1.0],"
			+ "\"BlockState\":{\"Name\":\"minecraft:grass_block\",\"Properties\":{}},\"IsShown\":false}]}";
	static final String ITEM_LITERAL = "{\"Mods\":[],\"Items\":[{\"Pos\":[0.0,0.125,0.0],\"Rot\":[45.0,0.0,0.0],\"Scales\":[1.0,1.0,1.0],"
			+ "\"ItemStack\":{\"Name\":\"minecraft:grass_block\",\"JsonElement\":\"{Count:1b,id:\\\"minecraft:grass_block\\\"}\"},"
			+ "\"Color\":-16711936,\"IsShown\":true,\"EnableBlock\":false}]}";

	static int failures = 0;

	static void check(String name, boolean ok, String detail) {
		if (ok) {
			System.out.println("ok   " + name);
		} else {
			failures++;
			System.out.println("FAIL " + name + (detail == null ? "" : ": " + detail));
		}
	}

	static void check(String name, boolean ok) {
		check(name, ok, null);
	}

	public static void main(String[] args) throws Exception {
		String gsonJar = String.valueOf(Gson.class.getProtectionDomain().getCodeSource().getLocation());
		check("gson is 2.8.9 (Minecraft 1.19.2)", gsonJar.contains("gson-2.8.9"), gsonJar);
		Path dir = Path.of(args[0]);
		String upBlock = Files.readString(dir.resolve("upstream_block.json"));
		String upItem = Files.readString(dir.resolve("upstream_item.json"));
		check("upstream block sample matches the pinned literal", upBlock.equals(BLOCK_LITERAL), upBlock);
		check("upstream item sample matches the pinned literal", upItem.equals(ITEM_LITERAL), upItem);

		// Read the upstream format (the crash path: ShareUtils.from / fromItems).
		ShareBlockInformation b = null;
		try {
			b = ShareUtils.from(upBlock);
		} catch (Throwable t) {
			check("from(upstream block) does not throw", false, t.toString());
		}
		if (b != null) {
			check("block mods", b.mods().size() == 1 && b.mods().contains("create"), String.valueOf(b.mods()));
			check("block count", b.blocks().size() == 2);
			ShareData d0 = b.blocks().get(0);
			check("block 0 pos/rot/scales", d0.pos().equals(List.of(0.5, -0.25, 1.0)) && d0.rot().equals(List.of(0.0F, 90.0F, 0.0F))
					&& d0.scales().equals(List.of(1.0F, 0.5F, 2.0F)));
			check("block 0 state", d0.blockState().name().equals("create:andesite_casing")
					&& "north".equals(d0.blockState().properties().get("facing")) && "false".equals(d0.blockState().properties().get("waterlogged")));
			check("block 0/1 isShown", d0.isShown() && !b.blocks().get(1).isShown());
			check("block re-serialized byte-identical", ShareUtils.GSON.toJson(b, ShareBlockInformation.class).equals(upBlock),
					ShareUtils.GSON.toJson(b, ShareBlockInformation.class));
		}

		ShareItemInformation it = null;
		try {
			it = ShareUtils.fromItems(upItem);
		} catch (Throwable t) {
			check("fromItems(upstream item) does not throw", false, t.toString());
		}
		if (it != null) {
			check("item mods empty", it.mods().isEmpty());
			ItemShareData i0 = it.items().get(0);
			check("item fields", i0.pos().equals(List.of(0.0, 0.125, 0.0)) && i0.rot().equals(List.of(45.0F, 0.0F, 0.0F))
					&& i0.color() == -16711936 && i0.isShown() && !i0.enableBlock());
			check("item stack", i0.itemStack().name().equals("minecraft:grass_block")
					&& i0.itemStack().jsonElement().getAsString().equals("{Count:1b,id:\"minecraft:grass_block\"}"));
			check("item re-serialized byte-identical", ShareUtils.GSON.toJson(it, ShareItemInformation.class).equals(upItem),
					ShareUtils.GSON.toJson(it, ShareItemInformation.class));
		}

		// Write: objects built the way ShareBlockInformation.from(List) builds them give the upstream text.
		Map<String, String> props = new LinkedHashMap<>();
		props.put("facing", "north");
		props.put("waterlogged", "false");
		ShareBlockInformation built = new ShareBlockInformation(new LinkedHashSet<>(List.of("create")), List.of(
				new ShareData(List.of(0.5, -0.25, 1.0), List.of(0.0F, 90.0F, 0.0F), List.of(1.0F, 0.5F, 2.0F),
						new ShareBlockState("create:andesite_casing", props), true),
				new ShareData(List.of(0.0, 0.0, 0.0), List.of(0.0F, 0.0F, 0.0F), List.of(1.0F, 1.0F, 1.0F),
						new ShareBlockState("minecraft:grass_block", Map.of()), false)));
		String builtJson = ShareUtils.GSON.toJson(built, ShareBlockInformation.class);
		check("built block writes the upstream format", builtJson.equals(upBlock), builtJson);
		ShareItemInformation builtItems = new ShareItemInformation(new LinkedHashSet<>(), List.of(
				new ItemShareData(List.of(0.0, 0.125, 0.0), List.of(45.0F, 0.0F, 0.0F), List.of(1.0F, 1.0F, 1.0F),
						new ShareItemStack("minecraft:grass_block", new JsonPrimitive("{Count:1b,id:\"minecraft:grass_block\"}")),
						-16711936, true, false)));
		String builtItemJson = ShareUtils.GSON.toJson(builtItems, ShareItemInformation.class);
		check("built item writes the upstream format", builtItemJson.equals(upItem), builtItemJson);

		// Round trip of what setRemoved does (transfer then from) and of a written then read-back object.
		check("block round trip", Objects.equals(ShareUtils.GSON.toJson(ShareUtils.from(builtJson), ShareBlockInformation.class), builtJson));
		check("item round trip", Objects.equals(ShareUtils.GSON.toJson(ShareUtils.fromItems(builtItemJson), ShareItemInformation.class), builtItemJson));

		// Missing keys read as upstream's Gson read them: null objects, 0 / false primitives.
		ShareBlockInformation empty = ShareUtils.from("{}");
		check("empty block code", empty.mods() == null && empty.blocks() == null);
		ShareItemInformation partial = ShareUtils.fromItems("{\"Items\":[{\"Pos\":[1.0]}]}");
		check("partial item code", partial.items().size() == 1 && partial.items().get(0).color() == 0 && !partial.items().get(0).isShown()
				&& partial.items().get(0).itemStack() == null);

		if (failures == 0) {
			System.out.println("ALL OK");
		} else {
			System.out.println(failures + " FAILED");
			System.exit(1);
		}
	}
}
