// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.solraspberrypie.net;

import com.SaintAlfonzo.solraspberrypie.logic.Diversity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The server's diversity table (values and families), sent at login and on /reload so a client's tooltips and Diet Log
 * use the same numbers as the server, whatever tables its own jars hold (publish_readiness.md PR-RP-17). An integrated
 * server's own client shares the table already and ignores it.
 */
public final class S2CDiversity {
	final Map<ResourceLocation, Double> values;
	final Map<ResourceLocation, ResourceLocation> families;

	public S2CDiversity(Map<ResourceLocation, Double> values, Map<ResourceLocation, ResourceLocation> families) {
		this.values = values;
		this.families = families;
	}

	@SuppressWarnings("unchecked")
	public static S2CDiversity current() {
		List<Map<ResourceLocation, ?>> t = Diversity.snapshot();
		return new S2CDiversity((Map<ResourceLocation, Double>) t.get(0), (Map<ResourceLocation, ResourceLocation>) t.get(1));
	}

	public static void encode(S2CDiversity m, FriendlyByteBuf buf) {
		buf.writeVarInt(m.values.size());
		m.values.forEach((id, v) -> {
			buf.writeResourceLocation(id);
			buf.writeDouble(v);
		});
		buf.writeVarInt(m.families.size());
		m.families.forEach((id, head) -> {
			buf.writeResourceLocation(id);
			buf.writeResourceLocation(head);
		});
	}

	public static S2CDiversity decode(FriendlyByteBuf buf) {
		int n = buf.readVarInt();
		Map<ResourceLocation, Double> values = new HashMap<>(n * 2);
		for (int i = 0; i < n; i++) values.put(buf.readResourceLocation(), buf.readDouble());
		int f = buf.readVarInt();
		Map<ResourceLocation, ResourceLocation> families = new HashMap<>(f * 2);
		for (int i = 0; i < f; i++) families.put(buf.readResourceLocation(), buf.readResourceLocation());
		return new S2CDiversity(values, families);
	}

	/** Headless test (tools/test_rp_ladder.sh): what a remote client does with the packet. */
	public static void applyForTest(S2CDiversity m) {
		Diversity.applyRemote(m.values, m.families);
	}

	public static void handle(S2CDiversity m, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			if (!net.minecraft.client.Minecraft.getInstance().hasSingleplayerServer()) Diversity.applyRemote(m.values, m.families);
		}));
		ctx.get().setPacketHandled(true);
	}
}
