package com.SaintAlfonzo.creative_labels.client;

import com.SaintAlfonzo.creative_labels.CLConfig;
import com.SaintAlfonzo.creative_labels.CLRegistry;
import com.SaintAlfonzo.creative_labels.block.FacePaperBlock;
import com.SaintAlfonzo.creative_labels.block.FacePaperBlockEntity;
import com.SaintAlfonzo.creative_labels.block.NoteData;
import com.SaintAlfonzo.creative_labels.item.StickyNoteItem;
import com.SaintAlfonzo.creative_labels.net.CLNetwork;
import com.SaintAlfonzo.creative_labels.net.NoteTextPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;

/**
 * The note screen (sticky_notes.md 3.7, BRIEF 4): Sticky It's two screen textures by resource location, the text
 * bar ({@code stickygui.png}, 352 x 16) centred a little above the screen centre with the date bar
 * ({@code stickyguidatee.png}, 70 x 16) above it, an {@link EditBox} on the text bar in Sticky It's dark grey,
 * capped at {@code noteLength}, and a Done button. Done (or Enter) sends {@link NoteTextPacket}; Escape closes
 * without writing. Two modes: a note in hand ({@link #forHand}) and a placed note ({@link #forPlaced}); the placed
 * mode closes by itself when its note is gone (C6).
 */
public class NoteScreen extends Screen {

    public static final ResourceLocation TEXT_BAR = new ResourceLocation("sticky_it", "textures/screens/stickygui.png");
    public static final ResourceLocation DATE_BAR = new ResourceLocation("sticky_it", "textures/screens/stickyguidatee.png");
    public static final int BAR_W = 352, BAR_H = 16, DATE_W = 70;
    /** Sticky It's text colour on the bars. */
    public static final int TEXT_COLOR = 0x3C3C3C;
    /** Sticky It drew the text bar 7 px above the screen centre and the date bar 16 px above that. */
    public static final int BAR_UP = 7, DATE_UP = 16, TEXT_INSET = 4;

    private final NoteTextPacket.Mode mode;
    private final InteractionHand hand;
    private final BlockPos pos;
    private final Direction face;
    private final int spot;
    private final String initial;
    private final Component dateLine;

    private EditBox box;
    private int barX, barY;
    private boolean sent;

    private NoteScreen(NoteTextPacket.Mode mode, InteractionHand hand, BlockPos pos, Direction face, int spot,
                       String initial, long day, String placer) {
        super(Component.translatable("creative_labels.screen.note.title"));
        this.mode = mode;
        this.hand = hand;
        this.pos = pos;
        this.face = face;
        this.spot = spot;
        this.initial = initial;
        this.dateLine = Component.translatable("creative_labels.screen.note.day", day, placer);
    }

    /** Writing a note held in {@code hand}: the date bar shows today and the player. */
    public static NoteScreen forHand(InteractionHand hand, ItemStack note, long day, String player) {
        return new NoteScreen(NoteTextPacket.Mode.HAND, hand, BlockPos.ZERO, Direction.NORTH, 0,
                NoteData.readText(note), day, player);
    }

    /** Rewriting the placed note on {@code spot} of {@code face} at {@code pos}: the date bar shows its own day and placer. */
    public static NoteScreen forPlaced(BlockPos pos, Direction face, int spot, NoteData note) {
        return new NoteScreen(NoteTextPacket.Mode.PLACED, InteractionHand.MAIN_HAND, pos, face, spot,
                note.text, note.day, note.placer);
    }

    @Override
    protected void init() {
        barX = (width - BAR_W) / 2;
        barY = height / 2 - BAR_UP;
        box = new EditBox(font, barX + TEXT_INSET, barY, BAR_W - 2 * TEXT_INSET, BAR_H, title);
        box.setBordered(false);
        box.setTextColor(TEXT_COLOR);
        box.setTextColorUneditable(TEXT_COLOR);
        box.setMaxLength(CLConfig.integer(CLConfig.NOTE_LENGTH));
        box.setValue(initial);
        box.setCanLoseFocus(false);
        addRenderableWidget(box);
        setInitialFocus(box);
        addRenderableWidget(new Button(width / 2 - 50, barY + BAR_H + 10, 100, 20,
                Component.translatable("creative_labels.screen.note.done"), b -> done()));
    }

    @Override
    public void resize(net.minecraft.client.Minecraft minecraft, int w, int h) {
        String kept = box.getValue();
        super.resize(minecraft, w, h);
        box.setValue(kept);
    }

    @Override
    public void render(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
        renderBackground(ms);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, TEXT_BAR);
        blit(ms, barX, barY, 0.0f, 0.0f, BAR_W, BAR_H, BAR_W, BAR_H);
        RenderSystem.setShaderTexture(0, DATE_BAR);
        blit(ms, barX, barY - DATE_UP, 0.0f, 0.0f, DATE_W, BAR_H, DATE_W, BAR_H);
        RenderSystem.disableBlend();
        font.draw(ms, dateLine, barX + TEXT_INSET, barY - DATE_UP + TEXT_INSET, TEXT_COLOR);
        super.render(ms, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean keyPressed(int key, int scan, int modifiers) {
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            done();
            return true;
        }
        return super.keyPressed(key, scan, modifiers);
    }

    @Override
    public void tick() {
        box.tick();
        if (mode == NoteTextPacket.Mode.PLACED && placedNote() == null) onClose();
        if (mode == NoteTextPacket.Mode.HAND && minecraft != null && minecraft.player != null
                && !(minecraft.player.getItemInHand(hand).getItem() instanceof StickyNoteItem)) onClose();
    }

    /** The placed note this screen edits, as the client currently knows it; null when it is gone. */
    @Nullable
    private NoteData placedNote() {
        if (minecraft == null || minecraft.level == null) return null;
        if (!minecraft.level.getBlockState(pos).is(CLRegistry.FACE_PAPER.get())) return null;
        FacePaperBlockEntity be = FacePaperBlock.entity(minecraft.level, pos);
        return be == null ? null : be.face(face).note(spot);
    }

    /** Sends the text (once) and closes. An unchanged text sends nothing. */
    private void done() {
        if (!sent) {
            sent = true;
            String text = box.getValue();
            if (!text.equals(initial)) {
                CLNetwork.CHANNEL.sendToServer(mode == NoteTextPacket.Mode.HAND
                        ? NoteTextPacket.forHand(hand, text)
                        : NoteTextPacket.forPlaced(pos, face, spot, text));
            }
        }
        onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
