package umpaz.brewinandchewin.platform.client;

import com.mojang.blaze3d.vertex.PoseStack;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;

// Port: upstream's client loader abstraction, trimmed to what the Forge client needs (the coaster renderer talks to
// Forge's model system directly).
public interface BnCClientPlatformHelper {
   void renderFluidInKeg(AbstractedFluidStack stack, PoseStack pose, int x, int y, float alphaModifier);
}
