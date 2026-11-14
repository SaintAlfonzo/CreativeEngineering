package com.SaintAlfonzo.solraspberrypie.net;

import com.SaintAlfonzo.solraspberrypie.data.RpData;
import com.SaintAlfonzo.solraspberrypie.data.SlotChoice;
import com.SaintAlfonzo.solraspberrypie.logic.DietEngine;
import com.SaintAlfonzo.solraspberrypie.logic.EffectApplier;
import com.tarinoita.solsweetpotato.SOLSweetPotatoConfig;
import com.tarinoita.solsweetpotato.tracking.FoodList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** The player's diet state for the screens, the widget and the effect display filter (9.5). */
public final class S2CState {
    public double diversity;
    public double maxDiversity;
    public int foodsEaten;
    public int minFoods;
    /** The gate counts different foods (server config uniqueFoodsToActivate). */
    public boolean uniqueGate;
    public boolean paused;
    public int duration;
    public int activeSlots;
    public int stamp;
    public final List<SlotChoice> slots = new ArrayList<>();
    /** Active flat bonuses: effect -> level. */
    public final Map<ResourceLocation, Integer> flat = new LinkedHashMap<>();
    /** Effects we applied: effect -> level (what the display filter hides). */
    public final Map<ResourceLocation, Integer> applied = new LinkedHashMap<>();
    public final List<ResourceLocation> drinks = new ArrayList<>();

    public static S2CState of(ServerPlayer player) {
        RpData d = RpData.get(player);
        DietEngine.State s = DietEngine.current(player);
        S2CState m = new S2CState();
        m.diversity = s.diversity;
        m.maxDiversity = d.maxDiversity;
        m.foodsEaten = com.SaintAlfonzo.solraspberrypie.data.GateFoods.count(FoodList.get(player));
        m.uniqueGate = com.SaintAlfonzo.solraspberrypie.RpConfig.SERVER.uniqueFoodsToActivate.get();
        m.minFoods = SOLSweetPotatoConfig.minFoodsToActivate();
        m.paused = d.paused;
        m.duration = EffectApplier.duration();
        m.activeSlots = s.activeSlots;
        m.stamp = d.stamp;
        m.slots.addAll(d.slots);
        m.flat.putAll(s.flat);
        m.applied.putAll(d.applied);
        m.drinks.addAll(d.drinksByRecency());
        return m;
    }

    public static void encode(S2CState m, FriendlyByteBuf buf) {
        buf.writeDouble(m.diversity);
        buf.writeDouble(m.maxDiversity);
        buf.writeVarInt(m.foodsEaten);
        buf.writeVarInt(m.minFoods);
        buf.writeBoolean(m.uniqueGate);
        buf.writeBoolean(m.paused);
        buf.writeVarInt(m.duration);
        buf.writeVarInt(m.activeSlots);
        buf.writeVarInt(m.stamp);
        buf.writeVarInt(m.slots.size());
        for (SlotChoice c : m.slots) SlotChoice.write(buf, c);
        writeMap(buf, m.flat);
        writeMap(buf, m.applied);
        buf.writeVarInt(m.drinks.size());
        for (ResourceLocation id : m.drinks) buf.writeResourceLocation(id);
    }

    public static S2CState decode(FriendlyByteBuf buf) {
        S2CState m = new S2CState();
        m.diversity = buf.readDouble();
        m.maxDiversity = buf.readDouble();
        m.foodsEaten = buf.readVarInt();
        m.minFoods = buf.readVarInt();
        m.uniqueGate = buf.readBoolean();
        m.paused = buf.readBoolean();
        m.duration = buf.readVarInt();
        m.activeSlots = buf.readVarInt();
        m.stamp = buf.readVarInt();
        int n = buf.readVarInt();
        for (int i = 0; i < n; i++) m.slots.add(SlotChoice.read(buf));
        readMap(buf, m.flat);
        readMap(buf, m.applied);
        int d = buf.readVarInt();
        for (int i = 0; i < d; i++) m.drinks.add(buf.readResourceLocation());
        return m;
    }

    private static void writeMap(FriendlyByteBuf buf, Map<ResourceLocation, Integer> map) {
        buf.writeVarInt(map.size());
        map.forEach((k, v) -> {
            buf.writeResourceLocation(k);
            buf.writeVarInt(v);
        });
    }

    private static void readMap(FriendlyByteBuf buf, Map<ResourceLocation, Integer> map) {
        int n = buf.readVarInt();
        for (int i = 0; i < n; i++) map.put(buf.readResourceLocation(), buf.readVarInt());
    }

    public static void handle(S2CState m, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.SaintAlfonzo.solraspberrypie.client.ClientState.setState(m)));
        ctx.get().setPacketHandled(true);
    }
}
