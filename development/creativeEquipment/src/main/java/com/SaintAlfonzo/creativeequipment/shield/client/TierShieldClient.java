package com.SaintAlfonzo.creativeequipment.shield.client;

import com.SaintAlfonzo.creativeequipment.shield.ShieldItems;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

/** Client-only setup for the tier shields; called from {@code CreativeEngineering.ClientModEvents}. */
public final class TierShieldClient {

    private static final ResourceLocation BLOCKING = new ResourceLocation("blocking");

    private TierShieldClient() {}

    /**
     * Vanilla registers the {@code blocking} item-model predicate for {@code Items.SHIELD} only,
     * which is what swaps a shield to its raised-arm model ({@code item/shield_blocking}). Each
     * tier shield's model JSON keys its override off the same predicate, so register it per item.
     */
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (RegistryObject<Item> shield : ShieldItems.BY_TIER.values()) {
                ItemProperties.register(shield.get(), BLOCKING, (stack, level, entity, seed) ->
                        entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            }
        });
    }
}
