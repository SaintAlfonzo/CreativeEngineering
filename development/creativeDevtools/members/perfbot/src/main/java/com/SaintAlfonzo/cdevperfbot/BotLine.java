package com.SaintAlfonzo.cdevperfbot;

import java.util.Locale;
import java.util.Map;

/**
 * The new {@code /pbot} verbs' output line, {@code [cdevtest] verb=pbot.<verb> key=value ...}: the same format as
 * Creative Devtools' {@code /cdevtest} (a copy of its TestLine; perfBot is a separate mod and does not depend on the
 * Devtools jar), so one batch regex family reads both. Pure Java.
 */
final class BotLine {

    public static final String PREFIX = "[cdevtest] ";

    private final StringBuilder sb = new StringBuilder(PREFIX);

    BotLine(String verb) {
        sb.append("verb=pbot.").append(verb);
    }

    public BotLine kv(String key, Object value) {
        sb.append(' ').append(key).append('=').append(quote(format(value)));
        return this;
    }

    /** A map as {@code key=a:3,b:1}; empty as {@code -}. */
    public BotLine map(String key, Map<String, Integer> m) {
        if (m.isEmpty()) return kv(key, "-");
        StringBuilder v = new StringBuilder();
        m.forEach((k, n) -> v.append(v.length() == 0 ? "" : ",").append(k).append(':').append(n));
        return kv(key, v.toString());
    }

    static String format(Object value) {
        if (value == null) return "null";
        if (value instanceof Double || value instanceof Float) {
            double d = ((Number) value).doubleValue();
            if (d == Math.rint(d) && Math.abs(d) < 1e15) return String.valueOf((long) d);
            String s = String.format(Locale.ROOT, "%.4f", d);
            s = s.replaceAll("0+$", "");
            return s.endsWith(".") ? s.substring(0, s.length() - 1) : s;
        }
        return String.valueOf(value);
    }

    static String quote(String s) {
        boolean plain = !s.isEmpty();
        for (int i = 0; i < s.length() && plain; i++) {
            char c = s.charAt(i);
            if (Character.isWhitespace(c) || c == '"' || c == '=') plain = false;
        }
        if (plain) return s;
        StringBuilder q = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' || c == '\\') q.append('\\');
            if (c == '\n') {
                q.append("\\n");
                continue;
            }
            q.append(c);
        }
        return q.append('"').toString();
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
