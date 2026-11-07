// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/client/ModKeybinds.kt. Translated to Java for Forge 1.19.2.
package earth.terrarium.tempad.client;

import com.mojang.blaze3d.platform.InputConstants;
import earth.terrarium.tempad.common.network.c2s.OpenAppPacket;
import earth.terrarium.tempad.common.network.c2s.OpenTempadPacket;
import earth.terrarium.tempad.common.network.c2s.UseMacroPacket;
import earth.terrarium.tempad.common.registries.ModApps;
import earth.terrarium.tempad.common.registries.ModNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;

public final class ModKeybinds {
    public static final KeyMapping openDefaultApp = new KeyMapping("key.tempad.shortcut", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), "category.tempad");
    public static final KeyMapping useMacro = new KeyMapping("key.tempad.macro", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), "category.tempad");
    public static final KeyMapping newLocation = new KeyMapping("key.tempad.new_location", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), "category.tempad");
    public static final KeyMapping travelTimeline = new KeyMapping("key.tempad.travel_timeline", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), "category.tempad");

    private ModKeybinds() {
    }

    static void registerKeyBinding(RegisterKeyMappingsEvent event) {
        event.register(openDefaultApp);
        event.register(useMacro);
        event.register(newLocation);
        event.register(travelTimeline);
    }

    static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        while (openDefaultApp.consumeClick()) ModNetworking.sendToServer(new OpenTempadPacket());
        while (useMacro.consumeClick()) ModNetworking.sendToServer(new UseMacroPacket());
        while (newLocation.consumeClick()) ModNetworking.sendToServer(new OpenAppPacket(ModApps.NEW_LOCATION));
        while (travelTimeline.consumeClick()) ModNetworking.sendToServer(new OpenAppPacket(ModApps.TIMELINE));
    }
}
