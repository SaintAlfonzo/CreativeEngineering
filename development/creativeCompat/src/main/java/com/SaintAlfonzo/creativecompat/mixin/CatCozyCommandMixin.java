package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.cats.CozyCats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Cozy's Improved Cats owns cat modes (cozy_cats.md decision D2, {@link CozyCats}). Domestication Innovation's
 * {@code CatMixin} injects at the owner's sit toggle in {@code Cat.mobInteract} (m_6071_, first
 * {@code setOrderedToSit} m_21839_) and replaces it with its follow / wander / stay cycle. This injects at the same
 * call with a lower priority (900 vs DI's 1000), so it is applied first and runs first: on a Cozy cat it does the
 * vanilla toggle and returns vanilla's result, and DI's callback is never reached. Other cats are untouched.
 * Locals at that call (1.19.2): ItemStack, Item, InteractionResult; on a mismatch the injection is skipped
 * (fail-soft) and DI keeps the click.
 */
@Mixin(value = Cat.class, priority = 900, remap = false)
public abstract class CatCozyCommandMixin {

    @Inject(method = "m_6071_",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Cat;m_21839_(Z)V", ordinal = 0,
                    remap = false),
            cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT, remap = false)
    private void creativecompat$cozyKeepsSit(Player player, InteractionHand hand,
                                             CallbackInfoReturnable<InteractionResult> cir,
                                             ItemStack stack, Item item, InteractionResult result) {
        Cat self = (Cat) (Object) this;
        if (CozyCats.isCozy(self)) {
            self.setOrderedToSit(!self.isOrderedToSit());
            cir.setReturnValue(result);
        }
    }
}
