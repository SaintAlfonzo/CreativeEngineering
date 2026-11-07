// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/compat/JadePlugin.kt (the provider ids). The pack has
// WTHIT, not Jade, so upstream's Jade plugin becomes a WTHIT plugin: this common half declares one client toggle per
// upstream provider (Jade gives every provider uid a toggle, on by default); the tooltips are drawn by
// client/compat/WthitClientPlugin. Both are declared in waila_plugins.json, which WTHIT reads only when it is installed
// (TP-D8). Option names: config.waila.plugin_tempad.<id>, served from upstream's config.jade.plugin_tempad.<id> lang
// lines (UpstreamTranslator).
package earth.terrarium.tempad.common.compat;

import earth.terrarium.tempad.Tempad;
import mcp.mobius.waila.api.ICommonRegistrar;
import mcp.mobius.waila.api.IWailaCommonPlugin;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class WthitPlugin implements IWailaCommonPlugin {
    public static final ResourceLocation TIMEDOOR = Tempad.id("timedoor");
    public static final ResourceLocation ANCHOR = Tempad.id("anchor");
    public static final ResourceLocation RUDIMENTARY = Tempad.id("rudimentary");
    public static final ResourceLocation METRONOME = Tempad.id("metronome");
    public static final ResourceLocation WORKSTATION = Tempad.id("workstation");
    public static final ResourceLocation CHRONON = Tempad.id("chronon");

    @Override
    public void register(ICommonRegistrar registrar) {
        for (ResourceLocation id : List.of(TIMEDOOR, ANCHOR, RUDIMENTARY, METRONOME, WORKSTATION, CHRONON)) {
            registrar.localConfig(id, true);
        }
    }
}
