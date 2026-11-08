// Behaviour translated from Olympus 1.0.18 (MIT, Terrarium), earth.terrarium.olympus.client.components.textbox.TextBox,
// built on 1.19.2's EditBox for the editing (cursor, selection, clipboard).
package earth.terrarium.tempad.client.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;

/**
 * Olympus draws its nine-sliced texture and the text 4 px in, vertically centred, which is exactly the bordered
 * EditBox layout; only the drawing differs (sprite instead of the black box, a placeholder, Olympus colours).
 */
public class TextBox extends EditBox {
    public static final WidgetSprites OLYMPUS_TEXTBOX = new WidgetSprites(Button.olympus("textbox/normal"), Button.olympus("textbox/disabled"), Button.olympus("textbox/hovered"));

    private WidgetSprites sprites = OLYMPUS_TEXTBOX;
    private String placeholder = "";
    private int placeholderColor = Gfx.DARK_GRAY;

    public TextBox(State<String> state) {
        super(Minecraft.getInstance().font, 0, 0, 0, 0, CommonComponents.EMPTY);
        setMaxLength(32767);
        setValue(state.get());
        setResponder(state::set);
        setTextColor(Gfx.GRAY);
    }

    public TextBox withTexture(WidgetSprites sprites) {
        this.sprites = sprites;
        return this;
    }

    public TextBox withTextColor(int color) {
        setTextColor(color);
        return this;
    }

    public TextBox withPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    public TextBox withPlaceholderColor(int color) {
        this.placeholderColor = color;
        return this;
    }

    public TextBox withPosition(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public TextBox withSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public TextBox withMaxLength(int length) {
        setMaxLength(length);
        return this;
    }

    @Override
    public void renderButton(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        if (!isVisible()) return;
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        Sprites.blit(pose, sprites.get(active, isHoveredOrFocused()), x, y, width, height);
        // Draw the bordered layout without vanilla's black box: shift into the text area, render unbordered, restore.
        int ox = x, oy = y, ow = width;
        setBordered(false);
        x = ox + 4;
        y = oy + (height - 8) / 2;
        width = ow - 8;
        try {
            super.renderButton(pose, mouseX, mouseY, partialTick);
        } finally {
            x = ox;
            y = oy;
            width = ow;
            setBordered(true);
        }
        if (getValue().isEmpty() && !placeholder.isEmpty()) {
            Minecraft.getInstance().font.draw(pose, Minecraft.getInstance().font.plainSubstrByWidth(placeholder, width - 8), x + 4, y + (height - 8) / 2f, placeholderColor);
        }
    }

    @Override
    public void playDownSound(net.minecraft.client.sounds.SoundManager manager) {
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {
    }
}
