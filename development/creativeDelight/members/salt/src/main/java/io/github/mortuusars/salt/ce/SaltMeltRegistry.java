// Creative Delight overlay for the Salt repack (GNU GPL3, like the rest of the pack's code). Not upstream code.
package io.github.mortuusars.salt.ce;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

/**
 * Registers {@code salt:salt_melt} (plan_post_scratch_1115 B6) and its dispenser behaviour. Upstream's Salt class (not
 * overlaid) owns the other items; this listener is found by Forge's annotation scan of the repack jar.
 */
@Mod.EventBusSubscriber(modid = "salt", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class SaltMeltRegistry {
    public static final ResourceLocation SALT_MELT_ID = new ResourceLocation("salt", "salt_melt");
    public static final int USES = 64;
    private static Item saltMelt;

    private SaltMeltRegistry() {
    }

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ITEMS, helper -> {
            saltMelt = new SaltMeltItem(new Item.Properties().durability(USES).tab(CreativeModeTab.TAB_TOOLS));
            helper.register(SALT_MELT_ID, saltMelt);
        });
    }

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> DispenserBlock.registerBehavior(saltMelt, new SaltMeltItem.Dispense()));
    }
}
