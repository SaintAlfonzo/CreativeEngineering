package com.SaintAlfonzo.creativeequipment.shield;

import java.util.EnumMap;
import java.util.Map;

import com.SaintAlfonzo.creativeequipment.CreativeEquipment;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registers {@code creativeequipment:<tier>_shield} for every {@link ShieldTier} EXCEPT
 * {@link ShieldTier#IRON}, which is folded into the player's real vanilla shield ({@code Items.SHIELD})
 * instead of getting a new item of its own - see {@link ShieldTier#of}. Recipes are KubeJS
 * ({@code server_scripts/equipment/shield.js}, {@code end_gear.js}); banner decoration is
 * {@code mixin/ShieldDecorationRecipeMixin}.
 */
public final class ShieldItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CreativeEquipment.NS);

    public static final Map<ShieldTier, RegistryObject<Item>> BY_TIER = new EnumMap<>(ShieldTier.class);

    static {
        for (ShieldTier tier : ShieldTier.values()) {
            if (tier == ShieldTier.IRON) {
                continue;
            }
            BY_TIER.put(tier, ITEMS.register(tier.id() + "_shield", () -> new TierShieldItem(tier)));
        }
    }

    private ShieldItems() {}

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
