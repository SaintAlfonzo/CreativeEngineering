package io.github.mortuusars.exposure.world.item;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.gui.ClientGUI;
import io.github.mortuusars.exposure.world.item.component.album.SignedAlbumContent;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SignedAlbumItem extends Item {
   public SignedAlbumItem(Properties properties) {
      super(properties);
   }

   public SignedAlbumContent getContent(ItemStack stack) {
      return (SignedAlbumContent)Exposure.DataComponents.SIGNED_ALBUM_CONTENT.getOrDefault(stack, SignedAlbumContent.EMPTY);
   }

   @NotNull
   public Component getName(ItemStack stack) {
      SignedAlbumContent content = (SignedAlbumContent)Exposure.DataComponents.SIGNED_ALBUM_CONTENT.get(stack);
      if (content != null) {
         String title = content.title();
         if (!org.apache.commons.lang3.StringUtils.isBlank(title)) {
            return Component.literal(title);
         }
      }

      return super.getName(stack);
   }

   @NotNull
   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
      if (level.isClientSide()) {
         ClientGUI.openAlbumViewScreen(player.getItemInHand(usedHand));
      }

      return InteractionResultHolder.success(player.getItemInHand(usedHand));
   }

   public void appendHoverText(ItemStack stack, @Nullable Level context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
      SignedAlbumContent content = (SignedAlbumContent)Exposure.DataComponents.SIGNED_ALBUM_CONTENT.get(stack);
      if (content != null) {
         String author = content.author();
         if (!org.apache.commons.lang3.StringUtils.isBlank(author)) {
            tooltipComponents.add(Component.translatable("gui.exposure.album.by_author", new Object[]{author}).withStyle(ChatFormatting.GRAY));
         }

         if ((Boolean)Config.Client.ALBUM_PHOTOS_COUNT_TOOLTIP.get()) {
            int photographsCount = (int)content.pages().stream().filter(page -> !page.photograph().isEmpty()).count();
            if (photographsCount > 0) {
               tooltipComponents.add(Component.translatable("item.exposure.album.tooltip.photos_count", new Object[]{photographsCount}));
            }
         }
      }
   }

   public boolean isFoil(ItemStack stack) {
      return (Boolean)Config.Common.SIGNED_ALBUM_GLINT.get();
   }
}
