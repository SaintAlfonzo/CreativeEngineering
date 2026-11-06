package com.SaintAlfonzo.creativedelight.lunchbox.client;

import java.util.List;
import java.util.Objects;

import com.SaintAlfonzo.creativedelight.CreativeDelight;
import com.SaintAlfonzo.creativedelight.lunchbox.Lunchboxes;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Lunchbox looks, client side. Two item properties drive the model overrides in
 * kubejs/assets/solapplepie/models/item/ (written by tools/gen_lunchbox_looks.py):
 * {@code creativeengineering:lunchbox_skin} (position in {@link Lunchboxes#SKINS} + 1, 0 = own look) and
 * {@code creativeengineering:lunchbox_dyed} (1 when dyed). A dyed look's layer 0 is the greyscale base and
 * gets the dye colour; layer 1 (outline and latches) is left as drawn. Called from
 * {@code CreativeEngineering.ClientModEvents} when Spice of Life is loaded.
 */
public final class LunchboxClient {

    public static final ResourceLocation SKIN = new ResourceLocation(CreativeDelight.NS, "lunchbox_skin");
    public static final ResourceLocation DYED = new ResourceLocation(CreativeDelight.NS, "lunchbox_dyed");

    private LunchboxClient() {}

    private static List<Item> boxes() {
        return Lunchboxes.TIERS.stream().filter(ForgeRegistries.ITEMS::containsKey)
                .map(ForgeRegistries.ITEMS::getValue).filter(Objects::nonNull).toList();
    }

    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (Item box : boxes()) {
                ItemProperties.register(box, SKIN, (stack, level, entity, seed) -> Lunchboxes.skinIndex(stack));
                ItemProperties.register(box, DYED, (stack, level, entity, seed) -> Lunchboxes.isDyed(stack) ? 1.0F : 0.0F);
            }
        });
    }

    public static void onItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tint) -> {
            if (tint != 0 || !Lunchboxes.showsLunchboxLook(stack)) return -1;
            int color = Lunchboxes.getColor(stack);
            return color < 0 ? -1 : color;
        }, boxes().toArray(new Item[0]));
    }
}
