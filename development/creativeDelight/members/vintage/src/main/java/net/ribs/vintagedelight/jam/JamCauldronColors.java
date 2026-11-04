// Pack addition (CreativeEngineering2, TODO dishes/jams #8). Own work, GPLv3.
package net.ribs.vintagedelight.jam;

import java.util.Map;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ribs.vintagedelight.VintageDelight;

/**
 * The fruit stage cauldron is water in the jam's colour (Fruits Delight tints its fruit cauldrons the same way). The
 * colours are the mean of each jam block texture (tools/gen_pack_jams.py prints them).
 */
@Mod.EventBusSubscriber(modid = VintageDelight.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class JamCauldronColors {
    private JamCauldronColors() {}

    static final Map<String, Integer> COLOURS = Map.of("ancient_fruit", 0xdad547, "banana", 0xfaea7d, "cherry", 0xab3944,
        "green_grape", 0x72832e, "mulberry", 0xa3497a, "pepper", 0x983e11, "raspberry", 0x9f4a6c, "red_grape", 0x732abb,
        "salmonberry", 0xe98f31, "strawberry", 0xef504f);

    @SubscribeEvent
    public static void blocks(RegisterColorHandlersEvent.Block e) {
        PackJams.LINES.forEach((name, l) -> {
            if (l.fruitCauldron == null) return;
            int c = COLOURS.getOrDefault(name, 0xffffff);
            e.register((state, level, pos, tint) -> tint == 0 ? c : -1, l.fruitCauldron.get());
        });
    }

    @SubscribeEvent
    public static void items(RegisterColorHandlersEvent.Item e) {
        PackJams.LINES.forEach((name, l) -> {
            if (l.fruitCauldron == null) return;
            int c = COLOURS.getOrDefault(name, 0xffffff);
            Block b = l.fruitCauldron.get();
            e.register((stack, tint) -> tint == 0 ? c : -1, b.asItem());
        });
    }
}
