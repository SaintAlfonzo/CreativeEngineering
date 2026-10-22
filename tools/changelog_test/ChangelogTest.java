import com.SaintAlfonzo.creativeengineering.changelog.ChangelogMarkdown;
import com.SaintAlfonzo.creativeengineering.changelog.ChangelogMarkdown.Block;
import com.SaintAlfonzo.creativeengineering.changelog.ChangelogMarkdown.Kind;
import com.SaintAlfonzo.creativeengineering.changelog.ChangelogStore;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Offline checks for the player changelog (feature_writeup/changelog.md 6): version ordering, bullet detection and
 * the Markdown -> block conversion, plus every file in the changelog folder parsing with no raw Markdown left.
 * Run by tools/test_changelog.sh.
 */
public class ChangelogTest {
    static int failures;

    static void check(boolean ok, String what) {
        System.out.println((ok ? "  ok   " : "  FAIL ") + what);
        if (!ok) failures++;
    }

    /** Every style run of a component, in order. */
    static List<Style> styles(Component c) {
        List<Style> out = new ArrayList<>();
        c.visit((style, text) -> { if (!text.isEmpty()) out.add(style); return Optional.empty(); }, Style.EMPTY);
        return out;
    }

    static Style styleOf(Component c, String piece) {
        Style[] found = {null};
        c.visit((style, text) -> { if (found[0] == null && text.contains(piece)) found[0] = style; return Optional.empty(); }, Style.EMPTY);
        return found[0];
    }

    public static void main(String[] args) throws Exception {
        System.out.println("== version order");
        check(ChangelogStore.compare("1.0.10", "1.0.9") > 0, "1.0.10 after 1.0.9");
        check(ChangelogStore.compare("1.1.01", "1.0.10") > 0, "1.1.01 after 1.0.10");
        check(ChangelogStore.compare("1.1.01", "1.1.1") == 0, "1.1.01 and 1.1.1 are the same version (lint rejects the pair)");
        check(ChangelogStore.compare("1.1", "1.1.0") == 0, "missing segment counts as 0");
        List<String> v = new ArrayList<>(List.of("1.0.9", "1.1.01", "1.0.10", "1.0.0", "1.1.02"));
        v.sort((a, b) -> ChangelogStore.compare(b, a));
        check(v.equals(List.of("1.1.02", "1.1.01", "1.0.10", "1.0.9", "1.0.0")), "newest first: " + v);
        check(ChangelogStore.isVersion("1.1.01") && !ChangelogStore.isVersion("unreleased") && !ChangelogStore.isVersion("1.1.x"), "isVersion");
        check(!ChangelogStore.hasBullets("# Unreleased\n"), "title-only working list has no bullets");
        check(ChangelogStore.hasBullets("# Unreleased\n\n## Gameplay\n\n- **New:** x\n"), "a bullet counts");

        System.out.println("== markdown");
        String md = String.join("\n",
                "# 1.1.01 - The Brewing Update",
                "",
                "Released 2026-10-25",
                "",
                "A big one for brewers: drinks hit *harder*.",
                "",
                "## Brewing & Drinks <!-- icon: farmersdelight:cooking_pot -->",
                "",
                "- **Changed:** Every alcoholic drink now fills only half a hunger point,",
                "  and makes you queasy. Look for *Intoxicating Drink* in the tooltip.",
                "- **New:** Wine ages faster in a cellar.",
                "  - Warm rooms slow it down a little.",
                "",
                "<!-- a note for the dev side -->",
                "",
                "## Interface & Tooltips",
                "",
                "- **Fixed:** Raw names like `item.foo.bar` are gone. See the [wiki page](https://example.org/wiki).",
                "- **Removed:** The old thing.",
                "",
                "---",
                "");
        List<Block> blocks = ChangelogMarkdown.parse(md);
        List<Kind> kinds = blocks.stream().map(Block::kind).toList();
        check(kinds.equals(List.of(Kind.TITLE, Kind.DATE, Kind.PARAGRAPH, Kind.HEADING, Kind.BULLET, Kind.BULLET, Kind.BULLET,
                Kind.HEADING, Kind.BULLET, Kind.BULLET, Kind.RULE)), "block kinds " + kinds);
        check(blocks.get(0).text().getString().equals("1.1.01 - The Brewing Update"), "title text");
        check(blocks.get(1).text().getString().equals("Released 2026-10-25"), "date line");
        check(Boolean.TRUE.equals(styleOf(blocks.get(2).text(), "harder").isItalic()), "italic");
        check(blocks.get(3).icon() != null && blocks.get(3).icon().toString().equals("farmersdelight:cooking_pot"), "heading icon " + blocks.get(3).icon());
        check(blocks.get(3).text().getString().trim().equals("Brewing & Drinks"), "heading text without the comment");
        check(blocks.get(7).icon() == null, "heading without icon");
        Block changed = blocks.get(4);
        check(changed.depth() == 0 && changed.text().getString().startsWith("Changed: Every"), "label + text: " + changed.text().getString());
        check(changed.text().getString().contains("hunger point, and makes"), "soft line break joins with a space");
        Style label = styleOf(changed.text(), "Changed:");
        check(label != null && label.isBold() && TextColor.fromLegacyFormat(ChatFormatting.YELLOW).equals(label.getColor()), "Changed label yellow + bold");
        check(TextColor.fromLegacyFormat(ChatFormatting.GREEN).equals(styleOf(blocks.get(5).text(), "New:").getColor()), "New label green");
        check(TextColor.fromLegacyFormat(ChatFormatting.AQUA).equals(styleOf(blocks.get(8).text(), "Fixed:").getColor()), "Fixed label aqua");
        check(TextColor.fromLegacyFormat(ChatFormatting.RED).equals(styleOf(blocks.get(9).text(), "Removed:").getColor()), "Removed label red");
        check(blocks.get(6).depth() == 1 && blocks.get(6).text().getString().equals("Warm rooms slow it down a little."), "sub-bullet depth 1");
        Style code = styleOf(blocks.get(8).text(), "item.foo.bar");
        check(TextColor.fromLegacyFormat(ChatFormatting.GOLD).equals(code.getColor()), "backticks gold");
        Style link = styleOf(blocks.get(8).text(), "wiki page");
        check(link != null && link.isUnderlined() && link.getClickEvent() != null
                && link.getClickEvent().getAction() == ClickEvent.Action.OPEN_URL
                && link.getClickEvent().getValue().equals("https://example.org/wiki"), "link opens its URL");
        check(blocks.stream().noneMatch(b -> b.text().getString().contains("dev side")), "HTML comment block hidden");
        List<Block> unsafe = ChangelogMarkdown.parse("- [click](file:///etc/passwd)\n");
        check(styleOf(unsafe.get(0).text(), "click").getClickEvent() == null, "non-http link gets no click action");

        System.out.println("== changelog folder");
        Path dir = Path.of(args[0]);
        List<Path> files;
        try (Stream<Path> s = Files.list(dir)) {
            files = s.filter(p -> p.toString().endsWith(".md")).sorted().toList();
        }
        for (Path file : files) {
            List<Block> parsed = ChangelogMarkdown.parse(Files.readString(file));
            boolean raw = parsed.stream().anyMatch(b -> {
                String t = b.text().getString();
                return t.contains("**") || t.contains("`") || t.contains("<!--") || t.matches("(?s).*\\]\\(.*");
            });
            check(!parsed.isEmpty() && parsed.get(0).kind() == Kind.TITLE && !raw,
                    dir.relativize(file) + ": " + parsed.size() + " blocks, title first, no raw Markdown");
        }

        System.out.println(failures == 0 ? "all passed" : failures + " FAILED");
        System.exit(failures == 0 ? 0 : 1);
    }
}
