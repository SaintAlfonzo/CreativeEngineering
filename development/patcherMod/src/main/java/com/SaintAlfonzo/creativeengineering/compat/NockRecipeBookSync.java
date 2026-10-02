package com.SaintAlfonzo.creativeengineering.compat;

import com.mojang.logging.LogUtils;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

/**
 * Nock Enough Arrows adds its "fletching" {@link RecipeBookType} from the static init of
 * {@code events.RecipeBookEvents}, a {@code Dist.CLIENT} event subscriber. Clients load that
 * class at mod construction; a dedicated server never does. The server then has one fewer
 * RecipeBookType than the client, and vanilla {@code RecipeBookSettings.read} (two booleans per
 * type, in {@code ClientboundRecipePacket}) reads past the end of the packet, which kicks the
 * client on join with "DecoderException: readerIndex(18) + length(1) exceeds writerIndex(18)".
 *
 * The server can't just load that class: its static init builds lambdas typed on the
 * client-only {@code RecipeBookCategories}, so it would throw NoClassDefFoundError there. Create
 * the same enum value directly instead. Vanilla server code never calls
 * {@code RecipeBookMenu#getRecipeBookType}, so the mod's own create() never also runs there.
 */
public final class NockRecipeBookSync {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TYPE = "fletching";

    private NockRecipeBookSync() {}

    public static void register() {
        if (FMLEnvironment.dist != Dist.DEDICATED_SERVER || !ModList.get().isLoaded("nock_enough_arrows")) return;
        for (RecipeBookType t : RecipeBookType.values()) {
            if (t.name().equals(TYPE)) return;
        }
        RecipeBookType.create(TYPE);
        LOGGER.info("[nock-recipe-book] added server-side RecipeBookType '{}' ({} types)",
                TYPE, RecipeBookType.values().length);
    }
}
