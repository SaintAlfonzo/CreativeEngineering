// Port glue (ours, GPLv3), TP-D4: 1.21's SpacerElement (an empty layout cell) as a row of the widget kit's ScrollList.
package earth.terrarium.tempad.client.ui;

import com.mojang.blaze3d.vertex.PoseStack;

public class Spacer extends KitWidget {
    public Spacer(int width, int height) {
        super(width, height);
        this.active = false;
    }

    @Override
    protected void renderWidget(PoseStack pose, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }
}
