package io.github.mortuusars.exposure_polaroid;

import java.util.function.Consumer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkHooks;

/** Upstream Polaroid {@code PlatformHelper} ({@code @ExpectPlatform}) with the NeoForge {@code PlatformHelperImpl} folded in. */
public class PlatformHelper {
   public static void openMenu(ServerPlayer serverPlayer, MenuProvider menuProvider, Consumer<FriendlyByteBuf> extraDataWriter) {
      NetworkHooks.openScreen(serverPlayer, menuProvider, extraDataWriter);
   }

   public static boolean isModLoaded(String modId) {
      return ModList.get().isLoaded(modId);
   }

   public static boolean isInDevEnv() {
      return !FMLEnvironment.production;
   }
}
