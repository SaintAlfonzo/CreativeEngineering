package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.compat.ImmersivePostsMaterials;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import twistedgate.immersiveposts.common.blocks.PostBaseBlock;
import twistedgate.immersiveposts.common.blocks.PostBlock;

/**
 * The fence of a material dropped by {@link ImmersivePostsRegistrationMixin} is not a post material:
 * without this, using it on a post base looks up a post block that was never registered and throws.
 * {@code IPostMaterial} is an interface, so its {@code isValidItem} is filtered at the call sites.
 */
@Mixin(value = {PostBaseBlock.class, PostBlock.class}, remap = false)
public abstract class ImmersivePostsValidItemMixin {

    // Both call sites are in use() (m_6227_), an instance method, so the handler is not static.
    @Redirect(method = "m_6227_", remap = false,
            at = @At(value = "INVOKE",
                    target = "Ltwistedgate/immersiveposts/api/posts/IPostMaterial;isValidItem(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean creativeengineering$keptOnly(ItemStack stack) {
        return ImmersivePostsMaterials.isValidItem(stack);
    }
}
