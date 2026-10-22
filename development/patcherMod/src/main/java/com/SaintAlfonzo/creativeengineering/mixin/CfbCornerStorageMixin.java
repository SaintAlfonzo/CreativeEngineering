package com.SaintAlfonzo.creativeengineering.mixin;

import java.util.List;

import com.SaintAlfonzo.creativeengineering.compat.CfbCornerStorage;
import com.SaintAlfonzo.creativeengineering.compat.CfbCornerStorage.CornerContainer;

import net.blay09.mods.balm.api.container.BalmContainerProvider;
import net.blay09.mods.balm.api.provider.BalmProvider;
import net.blay09.mods.cookingforblockheads.CookingForBlockheadsConfig;
import net.blay09.mods.cookingforblockheads.api.capability.DefaultKitchenItemProvider;
import net.blay09.mods.cookingforblockheads.api.capability.IKitchenItemProvider;
import net.blay09.mods.cookingforblockheads.tile.CornerBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Cooking for Blockheads 13.3.4 kitchen corner gets a counter's storage ({@link CfbCornerStorage}, TODO food/kitchen
 * #15). As a {@link BalmContainerProvider} it gets Balm's item handler (hoppers) and CfB's drop-on-break; the extra
 * {@code getProviders} entry lets the cooking table use its contents, as a counter's.
 */
@Mixin(value = CornerBlockEntity.class, remap = false)
public abstract class CfbCornerStorageMixin implements BalmContainerProvider, CfbCornerStorage.Holder {

    @Unique
    private CornerContainer ce$container;

    @Unique
    private DefaultKitchenItemProvider ce$itemProvider;

    @Override
    public CornerContainer ce$cornerStorage() {
        if (ce$container == null) {
            ce$container = new CornerContainer((BlockEntity) (Object) this,
                    CookingForBlockheadsConfig.getActive().largeCounters ? 54 : 27);
        }
        return ce$container;
    }

    @Override
    public Container getContainer() {
        return ce$cornerStorage();
    }

    @Inject(method = "getProviders", at = @At("RETURN"))
    private void ce$kitchenItems(CallbackInfoReturnable<List<BalmProvider<?>>> cir) {
        if (ce$itemProvider == null) {
            ce$itemProvider = new DefaultKitchenItemProvider(ce$cornerStorage());
        }
        cir.getReturnValue().add(new BalmProvider<>(IKitchenItemProvider.class, ce$itemProvider));
    }
}
