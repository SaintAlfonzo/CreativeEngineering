package io.github.mortuusars.exposure.client.gui.screen.album;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.backport.gui.WidgetSprites;
import net.minecraft.resources.ResourceLocation;

public class AlbumGUI {
   public static final WidgetSprites PREVIOUS_PAGE_BUTTON_SPRITES = new WidgetSprites(
      Exposure.resource("album/previous_page"), Exposure.resource("album/previous_page_highlighted")
   );
   public static final WidgetSprites NEXT_PAGE_BUTTON_SPRITES = new WidgetSprites(
      Exposure.resource("album/next_page"), Exposure.resource("album/next_page_highlighted")
   );
   public static final ResourceLocation TEXTURE = Exposure.resource("textures/gui/album.png");
}
