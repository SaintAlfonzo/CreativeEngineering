package com.SaintAlfonzo.creativeengineering.changelog;

import com.SaintAlfonzo.creativeengineering.changelog.ChangelogMarkdown.Block;
import com.SaintAlfonzo.creativeengineering.changelog.ChangelogMarkdown.Kind;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.gui.widget.ScrollPanel;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The right half of the changelog screen: one version's blocks wrapped to the panel width (Forge's
 * {@link ScrollPanel} does the scrollbar, wheel and clipping, as in its mod list's info panel).
 */
final class ChangelogPanel extends ScrollPanel {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<ResourceLocation> WARNED = new HashSet<>();
    private static final int PAD = 6;
    private static final int BAR = 6;
    private static final int ICON = 18;
    private static final int BULLET_INDENT = 12;
    private static final float TITLE_SCALE = 1.5F;

    /** One drawn line: text (or a rule), where it sits inside the content, and an optional icon to its left. */
    private record Line(@Nullable FormattedCharSequence text, int x, int y, float scale, int colour,
                        @Nullable ItemStack icon, boolean rule) {}

    private final ChangelogScreen screen;
    private final Font font;
    private List<Block> blocks = List.of();
    private List<Line> lines = List.of();
    private int contentHeight;

    ChangelogPanel(Minecraft minecraft, ChangelogScreen screen, int width, int height, int top, int left) {
        super(minecraft, width, height, top, left, PAD, BAR);
        this.screen = screen;
        this.font = minecraft.font;
    }

    void setBlocks(List<Block> blocks) {
        this.blocks = blocks;
        this.scrollDistance = 0;
        layout();
    }

    private void layout() {
        List<Line> out = new ArrayList<>();
        int textWidth = width - PAD * 2 - BAR;
        int y = 0;
        Kind previous = null;
        for (Block block : blocks) {
            switch (block.kind()) {
                case TITLE -> {
                    int lineHeight = Math.round(font.lineHeight * TITLE_SCALE);
                    for (FormattedCharSequence seq : font.split(block.text(), (int) (textWidth / TITLE_SCALE))) {
                        out.add(new Line(seq, 0, y, TITLE_SCALE, 0xFFFFFF, null, false));
                        y += lineHeight;
                    }
                    y += 2;
                }
                case DATE -> {
                    for (FormattedCharSequence seq : font.split(block.text(), textWidth)) {
                        out.add(new Line(seq, 0, y, 1, 0xA0A0A0, null, false));
                        y += font.lineHeight;
                    }
                }
                case PARAGRAPH -> {
                    y += 6;
                    for (FormattedCharSequence seq : font.split(block.text(), textWidth)) {
                        out.add(new Line(seq, 0, y, 1, 0xFFFFFF, null, false));
                        y += font.lineHeight;
                    }
                }
                case HEADING -> {
                    y += previous == null ? 0 : 10;
                    ItemStack icon = icon(block.icon());
                    int x = icon == null ? 0 : ICON;
                    Component text = block.text().copy().withStyle(ChatFormatting.BOLD);
                    List<FormattedCharSequence> wrapped = font.split(text, textWidth - x);
                    int top = y;
                    int textY = icon == null ? y : y + (16 - font.lineHeight) / 2 + 1;
                    for (int i = 0; i < wrapped.size(); i++) {
                        out.add(new Line(wrapped.get(i), x, textY, 1, 0xFFD27F, i == 0 ? icon : null, false));
                        textY += font.lineHeight;
                    }
                    y = Math.max(textY, icon == null ? textY : top + ICON) + 2;
                }
                case BULLET -> {
                    y += block.depth() == 0 ? 3 : 1;
                    int indent = block.depth() * BULLET_INDENT;
                    int textX = indent + font.width("• ");
                    out.add(new Line(Component.literal("•").getVisualOrderText(), indent, y, 1,
                            block.depth() == 0 ? 0xFFFFFF : 0xA0A0A0, null, false));
                    for (FormattedCharSequence seq : font.split(block.text(), textWidth - textX)) {
                        out.add(new Line(seq, textX, y, 1, block.depth() == 0 ? 0xFFFFFF : 0xD0D0D0, null, false));
                        y += font.lineHeight;
                    }
                }
                case RULE -> {
                    y += 4;
                    out.add(new Line(null, 0, y, 1, 0x60FFFFFF, null, true));
                    y += 5;
                }
            }
            previous = block.kind();
        }
        lines = out;
        contentHeight = y + PAD;
    }

    @Nullable
    private static ItemStack icon(@Nullable ResourceLocation id) {
        if (id == null) return null;
        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (item == null || item == Items.AIR) {
            if (WARNED.add(id)) LOGGER.warn("Changelog: icon item {} does not exist; heading drawn without it", id);
            return null;
        }
        return new ItemStack(item);
    }

    @Override
    protected int getContentHeight() {
        return Math.max(contentHeight, height - PAD * 2);
    }

    @Override
    protected int getScrollAmount() {
        return font.lineHeight * 3;
    }

    @Override
    protected void drawPanel(PoseStack pose, int entryRight, int relativeY, Tesselator tess, int mouseX, int mouseY) {
        int x0 = left + PAD;
        for (Line line : lines) {
            int y = relativeY + line.y();
            if (y > bottom || y + 20 < top) continue;
            if (line.rule()) {
                GuiComponent.fill(pose, x0, y, right - PAD - BAR, y + 1, line.colour());
                continue;
            }
            if (line.icon() != null) {
                Minecraft.getInstance().getItemRenderer().renderGuiItem(line.icon(), x0, y - (16 - font.lineHeight) / 2 - 1);
            }
            if (line.scale() != 1) {
                pose.pushPose();
                pose.translate(x0 + line.x(), y, 0);
                pose.scale(line.scale(), line.scale(), 1);
                font.drawShadow(pose, line.text(), 0, 0, line.colour());
                pose.popPose();
            } else {
                font.drawShadow(pose, line.text(), x0 + line.x(), y, line.colour());
            }
        }
    }

    /** The text style under the mouse (for links), or null. */
    @Nullable
    Style styleAt(double mouseX, double mouseY) {
        if (!isMouseOver(mouseX, mouseY)) return null;
        int relativeY = top + border - (int) scrollDistance;
        for (Line line : lines) {
            if (line.text() == null || line.scale() != 1) continue;
            int y = relativeY + line.y();
            if (mouseY >= y && mouseY < y + font.lineHeight) {
                Style style = font.getSplitter().componentStyleAtWidth(line.text(), (int) mouseX - (left + PAD + line.x()));
                if (style != null) return style;
            }
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Style style = button == 0 ? styleAt(mouseX, mouseY) : null;
        if (style != null && style.getClickEvent() != null && screen.handleComponentClicked(style)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {
    }
}
