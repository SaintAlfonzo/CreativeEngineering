package com.SaintAlfonzo.creativeengineering.changelog;

import com.SaintAlfonzo.creativeengineering.shadow.commonmark.node.Code;
import com.SaintAlfonzo.creativeengineering.shadow.commonmark.node.Emphasis;
import com.SaintAlfonzo.creativeengineering.shadow.commonmark.node.HardLineBreak;
import com.SaintAlfonzo.creativeengineering.shadow.commonmark.node.Heading;
import com.SaintAlfonzo.creativeengineering.shadow.commonmark.node.HtmlBlock;
import com.SaintAlfonzo.creativeengineering.shadow.commonmark.node.HtmlInline;
import com.SaintAlfonzo.creativeengineering.shadow.commonmark.node.Link;
import com.SaintAlfonzo.creativeengineering.shadow.commonmark.node.ListBlock;
import com.SaintAlfonzo.creativeengineering.shadow.commonmark.node.ListItem;
import com.SaintAlfonzo.creativeengineering.shadow.commonmark.node.Node;
import com.SaintAlfonzo.creativeengineering.shadow.commonmark.node.Paragraph;
import com.SaintAlfonzo.creativeengineering.shadow.commonmark.node.SoftLineBreak;
import com.SaintAlfonzo.creativeengineering.shadow.commonmark.node.StrongEmphasis;
import com.SaintAlfonzo.creativeengineering.shadow.commonmark.node.Text;
import com.SaintAlfonzo.creativeengineering.shadow.commonmark.node.ThematicBreak;
import com.SaintAlfonzo.creativeengineering.shadow.commonmark.parser.Parser;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Changelog Markdown (the subset in feature_writeup/changelog.md 3.2) -> blocks of vanilla text. Parsing is
 * commonmark-java's (shaded); this class only walks its tree.
 *
 * <ul>
 *   <li>{@code # title} -> TITLE, and a {@code Released yyyy-mm-dd} paragraph right after it -> DATE</li>
 *   <li>{@code ## family} -> HEADING, with the item of an {@code <!-- icon: ns:id -->} comment as its icon</li>
 *   <li>bullets -> BULLET with a depth (0, or 1 for an indented sub-bullet); a leading {@code **New:**},
 *       {@code **Changed:**}, {@code **Fixed:**} or {@code **Removed:**} is coloured</li>
 *   <li>other paragraphs -> PARAGRAPH, {@code ---} -> RULE</li>
 *   <li>inline: bold, italic, {@code `code`} in gold, links blue and underlined (http/https open on click)</li>
 * </ul>
 * Anything outside the subset falls back to its plain text (the lint keeps it out of shipped files).
 */
public final class ChangelogMarkdown {
    public enum Kind { TITLE, DATE, PARAGRAPH, HEADING, BULLET, RULE }

    public record Block(Kind kind, Component text, int depth, @Nullable ResourceLocation icon) {}

    private static final Parser PARSER = Parser.builder().build();
    private static final Pattern DATE = Pattern.compile("Released \\d{4}-\\d{2}-\\d{2}");
    private static final Pattern ICON = Pattern.compile("<!--\\s*icon:\\s*([a-z0-9_.-]+:[a-z0-9_./-]+)\\s*-->");
    private static final Map<String, ChatFormatting> LABELS = Map.of(
            "New:", ChatFormatting.GREEN,
            "Changed:", ChatFormatting.YELLOW,
            "Fixed:", ChatFormatting.AQUA,
            "Removed:", ChatFormatting.RED);

    private ChangelogMarkdown() {}

    public static List<Block> parse(String markdown) {
        List<Block> out = new ArrayList<>();
        for (Node node = PARSER.parse(markdown).getFirstChild(); node != null; node = node.getNext()) {
            block(node, out);
        }
        return out;
    }

    private static void block(Node node, List<Block> out) {
        if (node instanceof Heading heading) {
            MutableComponent text = inlines(heading, Style.EMPTY);
            if (heading.getLevel() == 1) {
                out.add(new Block(Kind.TITLE, text, 0, null));
            } else {
                out.add(new Block(Kind.HEADING, text, 0, icon(heading)));
            }
        } else if (node instanceof Paragraph paragraph) {
            MutableComponent text = inlines(paragraph, Style.EMPTY);
            boolean afterTitle = !out.isEmpty() && out.get(out.size() - 1).kind() == Kind.TITLE;
            Kind kind = afterTitle && DATE.matcher(text.getString().trim()).matches() ? Kind.DATE : Kind.PARAGRAPH;
            out.add(new Block(kind, text, 0, null));
        } else if (node instanceof ListBlock list) {
            list(list, 0, out);
        } else if (node instanceof ThematicBreak) {
            out.add(new Block(Kind.RULE, Component.empty(), 0, null));
        } else if (node instanceof HtmlBlock) {
            // comments (and anything else HTML) never show
        } else {
            // outside the subset (code block, quote): its plain text
            MutableComponent text = inlines(node, Style.EMPTY);
            if (!text.getString().isBlank()) out.add(new Block(Kind.PARAGRAPH, text, 0, null));
        }
    }

    private static void list(ListBlock list, int depth, List<Block> out) {
        for (Node item = list.getFirstChild(); item != null; item = item.getNext()) {
            if (!(item instanceof ListItem)) continue;
            MutableComponent text = Component.empty();
            List<ListBlock> nested = new ArrayList<>();
            boolean first = true;
            for (Node child = item.getFirstChild(); child != null; child = child.getNext()) {
                if (child instanceof ListBlock sub) {
                    nested.add(sub);
                } else {
                    if (!first) text.append(" ");
                    text.append(first && depth == 0 ? labelled(child) : inlines(child, Style.EMPTY));
                    first = false;
                }
            }
            out.add(new Block(Kind.BULLET, text, Math.min(depth, 1), null));
            for (ListBlock sub : nested) list(sub, depth + 1, out);
        }
    }

    /** A bullet's first paragraph, with its {@code **Label:**} coloured when it starts with one. */
    private static MutableComponent labelled(Node paragraph) {
        Node first = paragraph.getFirstChild();
        if (first instanceof StrongEmphasis strong) {
            String label = inlines(strong, Style.EMPTY).getString().trim();
            ChatFormatting colour = LABELS.get(label);
            if (colour != null) {
                MutableComponent out = Component.literal(label).withStyle(colour, ChatFormatting.BOLD);
                for (Node n = first.getNext(); n != null; n = n.getNext()) out.append(inline(n, Style.EMPTY));
                return out;
            }
        }
        return inlines(paragraph, Style.EMPTY);
    }

    @Nullable
    private static ResourceLocation icon(Heading heading) {
        for (Node n = heading.getFirstChild(); n != null; n = n.getNext()) {
            if (n instanceof HtmlInline html) {
                Matcher m = ICON.matcher(html.getLiteral());
                if (m.matches()) return ResourceLocation.tryParse(m.group(1));
            }
        }
        return null;
    }

    private static MutableComponent inlines(Node parent, Style style) {
        MutableComponent out = Component.empty();
        for (Node n = parent.getFirstChild(); n != null; n = n.getNext()) out.append(inline(n, style));
        return out;
    }

    private static MutableComponent inline(Node node, Style style) {
        if (node instanceof Text text) {
            return Component.literal(text.getLiteral()).withStyle(style);
        } else if (node instanceof Code code) {
            return Component.literal(code.getLiteral()).withStyle(style.withColor(ChatFormatting.GOLD));
        } else if (node instanceof StrongEmphasis) {
            return inlines(node, style.withBold(true));
        } else if (node instanceof Emphasis) {
            return inlines(node, style.withItalic(true));
        } else if (node instanceof Link link) {
            String url = link.getDestination();
            Style linkStyle = style.withColor(ChatFormatting.BLUE).withUnderlined(true);
            if (url.startsWith("https://") || url.startsWith("http://")) {
                linkStyle = linkStyle.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(url)));
            }
            return inlines(node, linkStyle);
        } else if (node instanceof SoftLineBreak || node instanceof HardLineBreak) {
            return Component.literal(" ").withStyle(style);
        } else if (node instanceof HtmlInline) {
            return Component.empty();
        }
        // anything else (images, nested blocks): its children's text
        return inlines(node, style);
    }
}
