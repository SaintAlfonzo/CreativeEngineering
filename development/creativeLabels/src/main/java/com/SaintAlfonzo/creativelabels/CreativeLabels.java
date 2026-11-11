package com.SaintAlfonzo.creativelabels;

import com.SaintAlfonzo.creativelabels.compat.DonorItems;
import com.SaintAlfonzo.creativelabels.shared.HiddenItems;
import com.SaintAlfonzo.creativelabels.block.FacePaperInteractions;
import com.SaintAlfonzo.creativelabels.client.ClientSetup;
import com.SaintAlfonzo.creativelabels.item.AnvilLabels;
import com.SaintAlfonzo.creativelabels.item.ItemLabels;
import com.SaintAlfonzo.creativelabels.net.CLNetwork;
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
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;

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

    public static final String ID = "creativelabels";
    /** The mod id before the naming-rule rename (namespace_move.md D-LABELS, 2026-11-10); saved ids move by swap list. */
    public static final String OLD_ID = "creative_labels";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CreativeLabels() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        carryOverConfig(OLD_ID + "-common.toml", ID + "-common.toml");
        carryOverConfig(OLD_ID + "-client.toml", ID + "-client.toml");
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

    /**
     * Keeps the player's values across the mod id rename: the old file is renamed to the new name when only the old
     * one exists (same rule as Creative Common's ConfigRename.carryOver, inlined so this jar keeps no Common dependency).
     * Runs before the configs register; Forge reads them after every constructor.
     */
    private static void carryOverConfig(String oldName, String newName) {
        Path dir = FMLPaths.CONFIGDIR.get();
        Path from = dir.resolve(oldName);
        Path to = dir.resolve(newName);
        if (!Files.isRegularFile(from) || Files.exists(to)) return;
        try {
            Files.move(from, to);
            LOGGER.info("[creativelabels] config {} carried over to {}", oldName, newName);
        } catch (Exception e) {
            LOGGER.error("[creativelabels] could not carry config {} over to {}: {}", oldName, newName, e.toString());
        }
    }

    public static boolean isLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}
