package com.SaintAlfonzo.creativecommon.swap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;

/**
 * The registry-free half of {@link ItemMerges}: rewriting saved item stacks by id, and the merge list's fingerprint.
 * Works on NBT so a stack keeps everything it carries (tag, Forge capabilities, nested contents); kept apart from the
 * Forge wiring so a plain javac/java harness can test it (feature_writeup/item_merges.md 6).
 */
public final class ItemMergeCore {
	private ItemMergeCore() {}

	/**
	 * Rewrites, in place, the id of every saved item stack in {@code tag}: any compound with a string {@code id} and a
	 * {@code Count} whose id is a key of {@code ids} gets the mapped id. Walks every nested compound and list, so the
	 * stack itself (when {@code tag} is a saved stack) and the contents of shulker boxes, bundles, backpacks and other
	 * stack-holding items are covered. Entity and block entity tags (an {@code id} without a {@code Count}) are left alone.
	 * Counts and every other key are untouched. Returns the number of ids rewritten.
	 */
	public static int rewriteNested(Tag tag, Map<String, String> ids) {
		if (tag instanceof CompoundTag c) {
			int n = 0;
			if (c.contains("id", Tag.TAG_STRING) && c.contains("Count", Tag.TAG_ANY_NUMERIC)) {
				String to = ids.get(c.getString("id"));
				if (to != null) {
					c.putString("id", to);
					n++;
				}
			}
			for (String key : c.getAllKeys()) {
				Tag child = c.get(key);
				if (child instanceof CompoundTag || child instanceof ListTag) n += rewriteNested(child, ids);
			}
			return n;
		}
		if (tag instanceof ListTag list) {
			byte type = list.getElementType();
			if (type != Tag.TAG_COMPOUND && type != Tag.TAG_LIST) return 0;
			int n = 0;
			for (Tag child : list) n += rewriteNested(child, ids);
			return n;
		}
		return 0;
	}

	/**
	 * How a stack of {@code count} splits into stacks of at most {@code max}: the first entry stays in the slot, the rest
	 * are overflow. A count that fits gives one entry. Never loses items: the entries add up to {@code count}.
	 */
	public static int[] split(int count, int max) {
		if (max <= 0 || count <= max) return new int[] {count};
		int parts = (count + max - 1) / max;
		int[] out = new int[parts];
		for (int i = 0; i < parts; i++) out[i] = Math.min(max, count - i * max);
		return out;
	}

	/** A short stable hash of the merge pairs (order-free), stored in chunks and players by the once-when-updated trigger. */
	public static String fingerprint(Map<String, String> ids) {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> e : new TreeMap<>(ids).entrySet()) sb.append(e.getKey()).append('>').append(e.getValue()).append(';');
		try {
			byte[] d = MessageDigest.getInstance("SHA-1").digest(sb.toString().getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder();
			for (int i = 0; i < 8; i++) hex.append(String.format("%02x", d[i]));
			return hex.toString();
		} catch (Exception e) {
			return Integer.toHexString(sb.toString().hashCode());
		}
	}
}
