package com.SaintAlfonzo.solraspberrypie.client;

import com.SaintAlfonzo.solraspberrypie.logic.DietEngine;
import com.SaintAlfonzo.solraspberrypie.logic.SlotActions;
import com.SaintAlfonzo.solraspberrypie.mixin.client.GuiAccessor;
import com.SaintAlfonzo.solraspberrypie.net.S2CMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * The diet message queue (10): one line at a time on the action-bar line, 3 s each, none dropped. A line waits while
 * any other overlay message is showing and never replaces one. Waiting hearts or attribute lines of the same kind and
 * direction merge into one line with the sum.
 */
public final class MessageQueue {
    private static final class Entry {
        final S2CMessage.Kind kind;
        final int color;
        final Component text;
        final ResourceLocation attribute;
        double amount;

        Entry(S2CMessage m) {
            kind = m.kind;
            color = m.color;
            text = m.text;
            attribute = m.attribute;
            amount = m.amount;
        }
    }

    private static final Deque<Entry> QUEUE = new ArrayDeque<>();

    public static void receive(S2CMessage m) {
        if (m.kind == S2CMessage.Kind.CLEAR) {
            QUEUE.clear();
            return;
        }
        if (m.kind == S2CMessage.Kind.AMOUNT) {
            for (Entry e : QUEUE) {
                if (e.kind == S2CMessage.Kind.AMOUNT && e.attribute.equals(m.attribute) && Math.signum(e.amount) == Math.signum(m.amount)) {
                    e.amount += m.amount;
                    return;
                }
            }
        }
        QUEUE.add(new Entry(m));
    }

    public static void clear() {
        QUEUE.clear();
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (QUEUE.isEmpty() || mc.player == null) return;
        if (((GuiAccessor) mc.gui).solrp$overlayMessageTime() > 0) return;
        mc.gui.setOverlayMessage(render(QUEUE.poll()), false);
    }

    private static Component render(Entry e) {
        MutableComponent line = switch (e.kind) {
            case GAINED -> Component.translatable("solraspberrypie.msg.gained", e.text);
            case LOST -> Component.translatable("solraspberrypie.msg.lost", e.text);
            case AMOUNT -> {
                Component name = DietEngine.isMaxHealth(e.attribute) ? SlotActions.hearts(e.amount)
                        : SlotActions.attribute(e.attribute, Math.abs(e.amount));
                yield Component.translatable(e.amount >= 0 ? "solraspberrypie.msg.gained" : "solraspberrypie.msg.lost", name);
            }
            default -> e.text.copy();
        };
        return line.withStyle(s -> s.withColor(TextColor.fromRgb(e.color)));
    }

    private MessageQueue() {}
}
