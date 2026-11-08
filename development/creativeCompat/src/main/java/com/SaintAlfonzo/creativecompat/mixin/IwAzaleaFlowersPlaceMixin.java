package com.SaintAlfonzo.creativecompat.mixin;

import com.ordana.immersive_weathering.items.AzaleaFlowersItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Azalea flowers merge (item_merges.md 2.21): Twigs' azalea flowers item is merged into Immersive Weathering's, and
 * Twigs' placement is kept. Immersive Weathering's {@code AzaleaFlowersItem#useOn} (m_6225_) only re-flowers an azalea
 * (when its shearing option is on) and passes on any other block; a pass now places Twigs' azalea flowers block
 * through Twigs' own block item, so the multiface placement, sound and stack use are Twigs', and the stack used is the
 * held Immersive Weathering one. Without Twigs' block item registered nothing changes.
 */
@Mixin(value = AzaleaFlowersItem.class, remap = false)
public abstract class IwAzaleaFlowersPlaceMixin {

    private static final ResourceLocation TWIGS_AZALEA_FLOWERS = new ResourceLocation("twigs", "azalea_flowers");

    @Inject(method = {"m_6225_", "useOn"}, at = @At("RETURN"), cancellable = true, require = 1)
    private void creativeengineering$placeTwigsFlowers(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (cir.getReturnValue() != InteractionResult.PASS) return;
        Item twigs = ForgeRegistries.ITEMS.getValue(TWIGS_AZALEA_FLOWERS);
        if (twigs instanceof BlockItem blockItem) cir.setReturnValue(blockItem.useOn(context));
    }
}
