package com.SaintAlfonzo.cdevcapture;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Text readback steps (TX-03, client_capture.md 2.2): each logs {@code [cdevcapture] READ <kind> <text>} lines
 * that qrun regexes or the runner judge. An optional {@code "expect": "<java regex>"} fails the step when no line
 * of this read matches; {@code "reject"} fails it when one does. No pixels are read.
 *
 * <pre>
 * {"assert_screen": "RecipesGui" | "none"}             READ assert_screen &lt;open screen class&gt;  (fails on mismatch)
 * {"screen_info": true}                                READ screen_info &lt;class&gt; title "&lt;title&gt;"
 * {"slot_item": N}                                     READ slot_item N &lt;id&gt; x&lt;count&gt; [nbt &lt;snbt&gt;] | empty
 *                                                      (slot of the open container, else player inventory index)
 * {"tooltip_text": "&lt;id&gt;" | {"slot": N} | "held",     READ tooltip_text &lt;id&gt; &lt;i&gt;: &lt;line&gt;   one per line
 *  "shift", "ctrl", "alt", "advanced", "nbt", "count"}
 * {"player_pos": true}                                 READ player_pos x y z yaw pitch ground dim
 * {"player_motion": true}                              READ player_motion v dx dy dz moved dx dy dz hspeed h bps b
 * {"held": true}                                       READ held main &lt;id&gt; x&lt;n&gt; off &lt;id&gt; x&lt;n&gt;
 * {"effects": true}                                    READ effects &lt;id&gt; amp a dur d | none
 * {"target": true}                                     READ target block &lt;id&gt; at x y z | entity &lt;type&gt; | miss
 * {"widgets": true}                                    READ widgets &lt;i&gt; &lt;class&gt; "&lt;message&gt;" x y w h [value "..."]
 * </pre>
 */
final class Reads {

    private Reads() {}

    static Steps.Step parse(JsonObject o) {
        for (String k : new String[]{"assert_screen", "screen_info", "slot_item", "tooltip_text", "player_pos",
                "player_motion", "held", "effects", "target", "widgets"}) {
            if (o.has(k)) return new Read(k, o);
        }
        return null;
    }

    static String id(ItemStack st) {
        return st.isEmpty() ? "empty" : String.valueOf(ForgeRegistries.ITEMS.getKey(st.getItem()));
    }

    static String stackText(ItemStack st) {
        if (st.isEmpty()) return "empty";
        return id(st) + " x" + st.getCount() + (st.hasTag() ? " nbt " + st.getTag() : "");
    }

    static String f(double v) {
        return String.format(Locale.ROOT, "%.3f", v);
    }

    static String quote(String s) {
        return "\"" + s.replace("\"", "'") + "\"";
    }

    static final class Read extends Steps.Step {
        final JsonObject o;
        final Pattern expect, reject;

        Read(String kind, JsonObject o) {
            super(kind);
            this.o = o;
            expect = o.has("expect") ? Pattern.compile(o.get("expect").getAsString()) : null;
            reject = o.has("reject") ? Pattern.compile(o.get("reject").getAsString()) : null;
        }

        @Override
        boolean tick(CaptureDriver d) throws Exception {
            if (ticks > 0) return true;
            List<String> lines = new ArrayList<>();
            Minecraft mc = d.mc;
            LocalPlayer p = mc.player;
            switch (kind) {
                case "assert_screen", "screen_info" -> {
                    String cls = mc.screen == null ? "none" : mc.screen.getClass().getName();
                    if (kind.equals("screen_info")) {
                        lines.add(cls + " title " + quote(mc.screen == null ? "" : mc.screen.getTitle().getString()));
                    } else {
                        lines.add(cls);
                        String want = o.get("assert_screen").getAsString();
                        boolean ok = want.equals("none") ? mc.screen == null : Steps.screenIs(mc.screen, want);
                        emit(d, lines);
                        if (!ok) throw new Steps.StepFailed("assert_screen: wanted " + want + ", open " + cls);
                        return false;
                    }
                }
                case "slot_item" -> {
                    int n = o.get("slot_item").getAsInt();
                    lines.add(n + " " + stackText(slotStack(mc, n)));
                }
                case "tooltip_text" -> {
                    ItemStack st = tooltipStack(mc, o.get("tooltip_text"), o.has("count") ? o.get("count").getAsInt() : 1);
                    if (o.has("nbt")) st.setTag(net.minecraft.nbt.TagParser.parseTag(o.get("nbt").getAsString()));
                    boolean s0 = VirtualInput.shift, c0 = VirtualInput.ctrl, a0 = VirtualInput.alt;
                    VirtualInput.shift = Steps.bool(o, "shift") || s0;
                    VirtualInput.ctrl = Steps.bool(o, "ctrl") || c0;
                    VirtualInput.alt = Steps.bool(o, "alt") || a0;
                    try {
                        TooltipFlag flag = Steps.bool(o, "advanced") ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
                        List<Component> tip = st.getTooltipLines(p, flag);
                        for (int i = 0; i < tip.size(); i++) lines.add(id(st) + " " + i + ": " + tip.get(i).getString());
                    } finally {
                        VirtualInput.shift = s0;
                        VirtualInput.ctrl = c0;
                        VirtualInput.alt = a0;
                    }
                }
                case "player_pos" -> lines.add(f(p.getX()) + " " + f(p.getY()) + " " + f(p.getZ()) + " yaw "
                        + f(p.getYRot()) + " pitch " + f(p.getXRot()) + " ground " + p.isOnGround() + " dim "
                        + p.level.dimension().location());
                case "player_motion" -> lines.add(motion(p));
                case "held" -> lines.add("main " + stackText(p.getMainHandItem()) + " off " + stackText(p.getOffhandItem()));
                case "effects" -> {
                    for (MobEffectInstance e : p.getActiveEffects()) {
                        lines.add(ForgeRegistries.MOB_EFFECTS.getKey(e.getEffect()) + " amp " + e.getAmplifier()
                                + " dur " + e.getDuration());
                    }
                    if (lines.isEmpty()) lines.add("none");
                }
                case "target" -> {
                    HitResult hit = mc.hitResult;
                    if (hit instanceof BlockHitResult b && b.getType() == HitResult.Type.BLOCK) {
                        lines.add("block " + ForgeRegistries.BLOCKS.getKey(mc.level.getBlockState(b.getBlockPos()).getBlock())
                                + " at " + b.getBlockPos().toShortString().replace(",", ""));
                    } else if (hit instanceof EntityHitResult e) {
                        lines.add("entity " + ForgeRegistries.ENTITY_TYPES.getKey(e.getEntity().getType()));
                    } else {
                        lines.add("miss");
                    }
                }
                case "widgets" -> {
                    List<Widgets.Entry> ws = Widgets.list(mc.screen);
                    for (int i = 0; i < ws.size(); i++) lines.add(i + " " + ws.get(i).describe());
                    if (ws.isEmpty()) lines.add("none");
                }
                default -> throw new IllegalArgumentException("unknown read " + kind);
            }
            emit(d, lines);
            return false;
        }

        private void emit(CaptureDriver d, List<String> lines) {
            boolean hit = false;
            String bad = null;
            for (String l : lines) {
                d.read(kind, l);
                if (expect != null && expect.matcher(l).find()) hit = true;
                if (reject != null && bad == null && reject.matcher(l).find()) bad = l;
            }
            note = lines.size() == 1 ? lines.get(0) : lines.size() + " lines";
            if (expect != null && !hit) throw new Steps.StepFailed(kind + ": no line matches /" + expect + "/");
            if (bad != null) throw new Steps.StepFailed(kind + ": /" + reject + "/ matched: " + bad);
        }
    }

    static String motion(LocalPlayer p) {
        Vec3 v = p.getDeltaMovement();
        double mx = p.getX() - p.xo, my = p.getY() - p.yo, mz = p.getZ() - p.zo;
        double h = Math.sqrt(mx * mx + mz * mz);
        return "v " + f(v.x) + " " + f(v.y) + " " + f(v.z) + " moved " + f(mx) + " " + f(my) + " " + f(mz)
                + " hspeed " + f(h) + " bps " + f(h * 20);
    }

    static ItemStack slotStack(Minecraft mc, int n) {
        if (mc.screen instanceof AbstractContainerScreen<?> acs) {
            List<Slot> slots = acs.getMenu().slots;
            if (n < 0 || n >= slots.size()) throw new Steps.StepFailed("slot_item: no slot " + n + " (" + slots.size() + " slots)");
            return slots.get(n).getItem();
        }
        if (n < 0 || n >= mc.player.getInventory().getContainerSize()) throw new Steps.StepFailed("slot_item: no inventory slot " + n);
        return mc.player.getInventory().getItem(n);
    }

    private static ItemStack tooltipStack(Minecraft mc, JsonElement arg, int count) throws Exception {
        if (arg.isJsonObject()) return slotStack(mc, arg.getAsJsonObject().get("slot").getAsInt()).copy();
        String s = arg.getAsString();
        if (s.equals("held")) return mc.player.getMainHandItem().copy();
        return Steps.stack(s, count, null);
    }
}
