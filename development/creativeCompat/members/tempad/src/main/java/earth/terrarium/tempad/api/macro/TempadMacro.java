// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/macro/MacroRegistry.kt (TempadMacro). Translated to Java for
// Forge 1.19.2.
package earth.terrarium.tempad.api.macro;

import earth.terrarium.tempad.api.context.SyncableContext;
import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface TempadMacro {
    void run(Player player, SyncableContext<?> ctx);
}
