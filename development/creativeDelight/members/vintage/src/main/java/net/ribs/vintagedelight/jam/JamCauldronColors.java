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

    static final Map<String, Integer> COLOURS = Map.of("ancient_fruit", 0xd2d044, "banana", 0xeecd5c, "cherry", 0xa32c26,
        "green_grape", 0x617a20, "mulberry", 0xef6170, "pepper", 0x99500a, "raspberry", 0xa22754, "red_grape", 0x862fa8,
        "salmonberry", 0xb7552b, "strawberry", 0xe74e4e);

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
