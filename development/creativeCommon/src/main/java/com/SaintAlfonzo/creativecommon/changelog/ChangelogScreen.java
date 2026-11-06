package com.SaintAlfonzo.creativecommon.changelog;

import com.SaintAlfonzo.creativecommon.changelog.ChangelogMarkdown.Block;
import com.SaintAlfonzo.creativecommon.changelog.ChangelogMarkdown.Kind;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * "What's New": versions newest first on the left, the selected version's notes on the right, Done at the bottom
 * (the shape of Forge's mod list). Opens on the newest version and marks it read, which ends the update popup.
 * Versions that were unread when the screen opened carry a {@code *}. In a dev instance whose unreleased.md has
 * bullets, "In development" sits at the top.
 */
public final class ChangelogScreen extends Screen {
    private static final int PAD = 6;

    private final Screen parent;
    /** Entry keys: {@link ChangelogStore#UNRELEASED} first when the dev list has bullets, then versions newest first. */
    private final List<String> entries = new ArrayList<>();
    private final Set<String> unread;
    @Nullable private String selected;
    private VersionList list;
    private ChangelogPanel panel;

    public ChangelogScreen(Screen parent) {
        super(Component.translatable("creativecommon.changelog.title"));
        this.parent = parent;
        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        List<String> versions = ChangelogStore.versions(manager);
        if (ChangelogStore.hasDevList(manager)) entries.add(ChangelogStore.UNRELEASED);
        entries.addAll(versions);
        unread = versions.stream().filter(ChangelogStore::isUnread).collect(Collectors.toSet());
        selected = !versions.isEmpty() ? versions.get(0) : entries.isEmpty() ? null : entries.get(0);
        if (!versions.isEmpty()) ChangelogStore.markSeen(versions.get(0));
    }

    @Override
    protected void init() {
        int listWidth = 60;
        for (String key : entries) listWidth = Math.max(listWidth, font.width(label(key)) + 16);
        listWidth = Math.min(listWidth, Math.max(60, width / 4));
        int top = PAD * 2 + font.lineHeight;
        int bottom = height - PAD * 2 - 20;

        list = new VersionList(listWidth, top, bottom);
        list.setLeftPos(PAD);
        panel = new ChangelogPanel(minecraft, this, width - listWidth - PAD * 3, bottom - top, top, listWidth + PAD * 2);
        addRenderableWidget(list);
        addRenderableWidget(panel);
        addRenderableWidget(new Button((width - 200) / 2, height - PAD - 20, 200, 20, CommonComponents.GUI_DONE,
                b -> onClose()));
        select(selected);
    }

    private void select(@Nullable String key) {
        selected = key;
        list.children().stream().filter(e -> e.key.equals(key)).findFirst().ifPresent(list::setSelected);
        if (key == null) {
            panel.setBlocks(List.of());
            return;
        }
        String text = ChangelogStore.read(minecraft.getResourceManager(), key);
        panel.setBlocks(text != null ? ChangelogMarkdown.parse(text)
                : List.of(new Block(Kind.PARAGRAPH, Component.translatable("creativecommon.changelog.missing"), 0, null)));
    }

    private Component label(String key) {
        if (key.equals(ChangelogStore.UNRELEASED)) {
            return Component.translatable("creativecommon.changelog.in_development").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
        }
        return unread.contains(key) ? Component.literal(key + " *").withStyle(ChatFormatting.GOLD) : Component.literal(key);
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        renderBackground(pose);
        super.render(pose, mouseX, mouseY, partialTick);
        drawCenteredString(pose, font, title, width / 2, PAD, 0xFFFFFF);
        Style style = panel.styleAt(mouseX, mouseY);
        if (style != null) renderComponentHoverEffect(pose, style, mouseX, mouseY);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private final class VersionList extends ObjectSelectionList<VersionList.Entry> {
        private final int listWidth;

        VersionList(int listWidth, int top, int bottom) {
            super(ChangelogScreen.this.minecraft, listWidth, ChangelogScreen.this.height, top, bottom, font.lineHeight + 8);
            this.listWidth = listWidth;
            for (String key : entries) addEntry(new Entry(key));
        }

        @Override
        protected int getScrollbarPosition() {
            return getLeft() + listWidth - 6;
        }

        @Override
        public int getRowWidth() {
            return listWidth;
        }

        private final class Entry extends ObjectSelectionList.Entry<Entry> {
            private final String key;

            Entry(String key) {
                this.key = key;
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", label(key));
            }

            @Override
            public void render(PoseStack pose, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean hovered, float partialTick) {
                font.drawShadow(pose, label(key), left + 3, top + 4, 0xFFFFFF);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                select(key);
                return true;
            }
        }
    }
}
