// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/client/TempadClient.kt: the guide and Curios parts
// (KnowledgeAppScreen registration, initCuriosCompat, openGuide). Translated to Java for Forge 1.19.2. Kept out of
// TempadClient so phase 4 could be written while the phase-3 client edited that class; it can be folded into
// TempadClient (init -> its init, setup -> its setup) without any behaviour change.
package earth.terrarium.tempad.client;

import earth.terrarium.tempad.client.compat.CuriosRenderer;
import earth.terrarium.tempad.client.screen.guide.KnowledgeScreen;
import earth.terrarium.tempad.client.screen.tempad.KnowledgeAppScreen;
import earth.terrarium.tempad.common.registries.ModMenus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class TempadClientExtras {
    private TempadClientExtras() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(TempadClientExtras::setup);
    }

    private static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.guide.get(), KnowledgeAppScreen::new);
            if (ModList.get().isLoaded("curios")) CuriosRenderer.init();
        });
    }

    /** Upstream {@code TempadClient.openGuide()} (knowledge projector use). */
    public static void openGuide() {
        Minecraft.getInstance().setScreen(new KnowledgeScreen());
    }
}
