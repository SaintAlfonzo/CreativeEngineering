package net.satisfy.vinery.legacy.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class LegacyCalendarItem extends BlockItem {
   public LegacyCalendarItem(Block block, Properties properties) {
      super(block, properties);
   }

   public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int i, boolean bl) {
      if (level.isClientSide() && entity instanceof Player player && player.isHolding(this)) {
         player.displayClientMessage(getTime(level), true);
      }
   }

   public static Component getTime(Level level) {
      long hour = level.getDayTime() / 1000L % 24L;
      long yearLength = net.satisfy.vinery.forge.VineryConfig.LEGACY_YEAR_LENGTH_DAYS.get();
      long day = level.getDayTime() / 24000L % yearLength;
      return Component.literal(hour + "h / " + day + "d / " + level.getDayTime() / 24000L / yearLength + "y").withStyle(ChatFormatting.YELLOW);
   }
}
