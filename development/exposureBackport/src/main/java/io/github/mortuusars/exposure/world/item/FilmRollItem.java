package io.github.mortuusars.exposure.world.item;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.PlatformHelper;
import io.github.mortuusars.exposure.client.gui.ClientGUI;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.inventory.ItemRenameMenu;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FilmRollItem extends Item implements SensitiveFilmItem {
   public static final int BAR_BLACK_AND_WHITE = Mth.color(0.8F, 0.8F, 0.9F);
   public static final int BAR_COLOR = Mth.color(0.4F, 0.4F, 1.0F);
   protected final ExposureType type;
   protected final int barColor;

   public FilmRollItem(ExposureType type, int barColor, Properties properties) {
      super(properties);
      this.type = type;
      this.barColor = barColor;
   }

   @Override
   public ExposureType getType() {
      return this.type;
   }

   public boolean canAddFrame(ItemStack stack) {
      return this.getStoredFramesCount(stack) < this.getMaxFrameCount(stack);
   }

   public void addFrame(ItemStack stack, Frame frame) {
      Preconditions.checkState(
         this.getStoredFramesCount(stack) < this.getMaxFrameCount(stack), "Cannot add more frames than film could fit. Size: " + this.getMaxFrameCount(stack)
      );
      List<Frame> frames = new ArrayList<>((Collection<? extends Frame>)Exposure.DataComponents.FILM_FRAMES.getOrDefault(stack, Collections.emptyList()));
      frames.add(frame);
      Exposure.DataComponents.FILM_FRAMES.set(stack, frames);
   }

   public void appendHoverText(ItemStack stack, @Nullable Level context, List<Component> list, TooltipFlag tooltipFlag) {
      int exposedFrames = this.getStoredFramesCount(stack);
      if (exposedFrames > 0) {
         int totalFrames = this.getMaxFrameCount(stack);
         list.add(
            Component.translatable("item.exposure.film_roll.tooltip.frame_count", new Object[]{exposedFrames, totalFrames}).withStyle(ChatFormatting.GRAY)
         );
      }

      this.addFrameSizeToTooltip(stack, list);
      if (tooltipFlag.isAdvanced()) {
         this.addPaletteToTooltip(stack, list);
         this.addStyleToTooltip(stack, list);
      }

      if ((Boolean)Config.Server.FILM_ROLL_EASY_RENAMING.get()) {
         list.add(Component.translatable("item.exposure.film_roll.tooltip.renaming"));
      }

      if (exposedFrames > 0 && !PlatformHelper.isModLoaded("jei") && (Boolean)Config.Client.RECIPE_TOOLTIPS_WITHOUT_JEI.get()) {
         ClientGUI.addFilmRollDevelopingTooltip(stack, context, list, tooltipFlag);
      }
   }

   @NotNull
   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
      if ((Boolean)Config.Server.FILM_ROLL_EASY_RENAMING.get() && player instanceof ServerPlayer serverPlayer) {
         final int slot = this.getMatchingSlotInInventory(player.getInventory(), player.getItemInHand(usedHand));
         MenuProvider menuProvider = new MenuProvider() {
            @NotNull
            public Component getDisplayName() {
               return Component.translatable("gui.exposure.item_rename.title");
            }

            @NotNull
            public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
               return new ItemRenameMenu(containerId, playerInventory, slot);
            }
         };
         PlatformHelper.openMenu(serverPlayer, menuProvider, buffer -> buffer.writeInt(slot));
         return InteractionResultHolder.success(player.getItemInHand(usedHand));
      } else {
         return super.use(level, player, usedHand);
      }
   }

   public int getBarColor(@NotNull ItemStack stack) {
      return this.barColor;
   }

   public boolean isBarVisible(@NotNull ItemStack stack) {
      return this.hasFrames(stack);
   }

   public int getBarWidth(@NotNull ItemStack stack) {
      return Math.min(1 + 12 * this.getStoredFramesCount(stack) / this.getMaxFrameCount(stack), 13);
   }

   protected int getMatchingSlotInInventory(Inventory inventory, ItemStack stack) {
      for (int i = 0; i < inventory.getContainerSize(); i++) {
         if (inventory.getItem(i).equals(stack)) {
            return i;
         }
      }

      return -1;
   }
}
