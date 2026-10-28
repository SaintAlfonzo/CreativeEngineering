package com.SaintAlfonzo.creative_labels;

import com.SaintAlfonzo.creative_labels.compat.DonorItems;
import com.SaintAlfonzo.creative_labels.shared.HiddenItems;
import com.SaintAlfonzo.creative_labels.block.FacePaperInteractions;
import com.SaintAlfonzo.creative_labels.client.ClientSetup;
import com.SaintAlfonzo.creative_labels.item.AnvilLabels;
import com.SaintAlfonzo.creative_labels.item.ItemLabels;
import com.SaintAlfonzo.creative_labels.net.CLNetwork;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Creative Labels: sticky notes and labels as paper on block faces (feature_writeup/sticky_notes.md). One block,
 * {@code face_paper}, in the air cell in front of a host face holds up to five notes or one label per face.
 *
 * <p>Registration order: configs, {@link CLRegistry} (block, block entity, items, recipe serializers), the network
 * channel on common setup, client-only setup through {@link DistExecutor} ({@link ClientSetup}: never loaded on a
 * dedicated server, C4). Forge-bus handlers: {@link FacePaperInteractions} (sneak clicks), {@link AnvilLabels}
 * (anvil labelling), {@link ItemLabels} (labelled-item tooltip lines).
 */
@Mod(CreativeLabels.ID)
public class CreativeLabels {

    public static final String ID = "creative_labels";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CreativeLabels() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CLConfig.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLConfig.CLIENT_SPEC);

        CLRegistry.register(modBus);
        // Sticky It's note and eraser and Labels' label stay out of the creative tabs (sticky_notes.md 3.13).
        HiddenItems.set(DonorItems::isHidden);
        modBus.addListener((FMLCommonSetupEvent e) -> e.enqueueWork(CLNetwork::register));
        MinecraftForge.EVENT_BUS.register(FacePaperInteractions.class);
        MinecraftForge.EVENT_BUS.register(AnvilLabels.class);
        MinecraftForge.EVENT_BUS.register(ItemLabels.class);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientSetup.register(modBus));
    }

    public static boolean isLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}
