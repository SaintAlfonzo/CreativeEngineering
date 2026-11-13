/*
 * Copyright 2025 patryk3211
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// Modified for the Forge 1.19.2 / Create 0.5.1 port (backportedPowerGrid, 2026); changes listed in PORT_NOTES.md.
package org.patryk3211.powergrid.kinetics.punchcard;

import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import org.patryk3211.powergrid.utility.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.collections.ModdedPackets;
import org.patryk3211.powergrid.network.packets.SaveCardC2SPacket;
import org.patryk3211.powergrid.utility.Lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class PunchCardScreen extends AbstractSimiContainerScreen<PunchCardMenu> {
    public static final ResourceLocation BACKGROUND = PowerGrid.texture("gui/punch_card_edit");
    private static final int WIDTH = 226;
    private static final int HEIGHT = 128;

    private EditBox nameField;
    // Port: 1.19.2 has no StringWidget; a locked card's author and name lines are drawn in renderForeground
    private Component authorLine, nameLine;

    private final List<Particle> particles = new ArrayList<>();

    public PunchCardScreen(PunchCardMenu container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    private void spawnParticle(int x, int y) {
        particles.add(new Particle(x, y));
    }

    @Override
    protected void init() {
        setWindowSize(WIDTH, HEIGHT);
        setWindowOffset(0, 0);

        super.init();

        var name = menu.contentHolder.getHoverName();
        var accept = new PunchCardBigButton(leftPos + 201, topPos + 105, 1, 130);
        accept.withCallback(this::onClose);

        addRenderableWidget(accept);

        var lock = menu.isLocked();
        if(!lock) {
            var reset = new PunchCardBigButton(leftPos + 179, topPos + 105, 20, 130);
            reset.withCallback(this::reset);
            var sign = new PunchCardBigButton(leftPos + 7, topPos + 105, 39, 130);
            // Port: 1.19.2 widgets have no Tooltip; Create's AbstractSimiWidget tooltip (drawn by the screen) is used
            sign.getToolTip().add(Lang.translateDirect("gui.punch_card.sign"));
            sign.withCallback(this::sign);

            nameField = new EditBox(font, leftPos + 60, topPos + 4, 157, 9, Component.empty());
            nameField.setValue(name.getString());
            nameField.setTextColor(0xffa6937d);
            nameField.setTextColorUneditable(0xffa6937d);
            nameField.setBordered(false);
            nameField.setMaxLength(26);
            nameField.setEditable(true);

            addRenderableWidgets(reset, sign);
            addRenderableWidget(nameField);
            authorLine = nameLine = null;
        } else {
            authorLine = Lang.translate("gui.punch_card.author")
                    .add(Component.literal(menu.getAuthor()))
                    .component();
            nameLine = Component.literal(name.getString());
            nameField = null;
        }

        for(int c = 0; c < 16; ++c) {
            for(int r = 0; r < 8; ++r) {
                int x = 14 + c * 13;
                int y = 20 + r * 9 + (r / 4) * 5;

                var btn = new PunchCardButton(leftPos + x, topPos + y, r, c, menu.data, lock);
                btn.withCallback(this::spawnParticle);
                addRenderableWidget(btn);
            }
        }
    }

    private void reset() {
        Arrays.fill(menu.data, (byte) 0);
    }

    private void sendData(boolean lock) {
        if(nameField == null)
            return;
        var name = nameField.getValue();
        if(name.equals(menu.contentHolder.getItem().getName(menu.contentHolder).getString()) || name.isEmpty())
            name = "";
        ModdedPackets.sendToServer(new SaveCardC2SPacket(menu.data, name, lock));
    }

    private void sign() {
        sendData(true);
        menu.lock();
        rebuildWidgets();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        var iter = particles.iterator();
        while(iter.hasNext()) {
            var particle = iter.next();
            particle.tick();
            if(particle.ttl <= 0)
                iter.remove();
        }
        if(nameField != null)
            nameField.tick();
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        var result = super.keyPressed(pKeyCode, pScanCode, pModifiers);
        if(getFocused() == nameField)
            return true;
        return result;
    }

    @Override
    protected void renderBg(@NotNull PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        var ctx = new GuiGraphics(poseStack); // Port: 1.19.2 screens draw with a PoseStack
        int bgX = getLeftOfCentered(WIDTH);
        ctx.blit(BACKGROUND, bgX, topPos, 0, 0, WIDTH, HEIGHT);
    }

    @Override
    protected void renderForeground(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        var ctx = new GuiGraphics(poseStack); // Port: 1.19.2 screens draw with a PoseStack
        if(authorLine != null)
            ctx.drawString(font, authorLine, leftPos + 7, topPos + 109, 0xffa6937d);
        if(nameLine != null)
            ctx.drawString(font, nameLine, leftPos + 60, topPos + 4, 0xffa6937d);
        for(var particle : particles) {
            particle.render(ctx, partialTicks);
        }
        super.renderForeground(poseStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        super.onClose();
        if(!menu.isLocked())
            sendData(false);
    }

    private static class Particle {
        private static final Random RANDOM = new Random();

        private float x, y;
        private float vx, vy;
        private float angle;
        private float va;
        private int ttl;

        public Particle(int x, int y) {
            this.x = x;
            this.y = y;
            ttl = 30 + RANDOM.nextInt(10);

            vx = (RANDOM.nextFloat() - 0.5f) * 4f;
            vy = RANDOM.nextFloat() * -4f;
            angle = (float) (RANDOM.nextFloat() * Math.PI * 2);
            va = 0.01f;
        }

        public void tick() {
            x += vx;
            y += vy;
            angle += va;

            vy += 0.5f;
            vx *= 0.95f;
            va += 0.001f;

            --ttl;
        }

        public void render(@NotNull GuiGraphics ctx, float partialTick) {
            var ms = ctx.pose();
            ms.pushPose();
            ms.translate(x + vx * partialTick, y + vy * partialTick, 0);
            ms.mulPose(Vector3f.ZP.rotation(angle + va * partialTick)); // Port: joml rotateAround at the origin
            var s = Math.min(ttl / 40f, 1.0f);
            ms.scale(s, s, 1.0f);
            ctx.blit(BACKGROUND, -5, -7, 228, 1, 11, 15);
            ms.popPose();
        }
    }
}
