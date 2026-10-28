package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.compat.ImmersivePostsMaterials;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import twistedgate.immersiveposts.common.IPOContent;
import twistedgate.immersiveposts.enums.EnumPostMaterial;

/**
 * Registers posts and trusses only for {@link ImmersivePostsMaterials#KEPT}. Both holder classes
 * fill their EnumMap in a static-init lambda that loops {@code EnumPostMaterial.values()} and
 * registers a block per entry; the loop sees the kept materials only. The mod's {@code get} methods
 * already return null for a material missing from the map.
 */
@Mixin(value = {IPOContent.Blocks.Posts.class, IPOContent.Blocks.HorizontalTruss.class}, remap = false)
public abstract class ImmersivePostsRegistrationMixin {

    @Redirect(method = "lambda$static$0", remap = false,
            at = @At(value = "INVOKE",
                    target = "Ltwistedgate/immersiveposts/enums/EnumPostMaterial;values()[Ltwistedgate/immersiveposts/enums/EnumPostMaterial;"))
    private static EnumPostMaterial[] creativeengineering$keptMaterials() {
        return ImmersivePostsMaterials.kept();
    }
}
