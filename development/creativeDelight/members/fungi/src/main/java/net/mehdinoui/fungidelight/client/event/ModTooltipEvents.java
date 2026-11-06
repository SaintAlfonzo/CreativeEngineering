package net.mehdinoui.fungidelight.client.event;

import java.util.List;
import net.mehdinoui.fungidelight.common.registry.ModEffects;
import net.mehdinoui.fungidelight.common.tag.FungiDelightTags;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "fungidelight",
   value = {Dist.CLIENT},
   bus = Bus.FORGE
)
public class ModTooltipEvents {
   @SubscribeEvent
   public static void onCoprineItemTooltip(ItemTooltipEvent event) {
      ItemStack stack = event.getItemStack();
      if (stack.is(FungiDelightTags.ALCOHOL_REACTION)) {
         event.getToolTip().add(Component.translatable("tooltip.fungidelight.weak_stomach_desc").withStyle(ChatFormatting.RED));
      }
   }

   @SubscribeEvent
   public static void onPotionTooltip(ItemTooltipEvent event) {
      ItemStack stack = event.getItemStack();
      if (stack.getItem() == Items.POTION
         || stack.getItem() == Items.SPLASH_POTION
         || stack.getItem() == Items.LINGERING_POTION
         || stack.getItem() == Items.TIPPED_ARROW) {
         List<MobEffectInstance> effects = PotionUtils.getMobEffects(stack);
         MobEffectInstance burrowingInstance = null;

         for (MobEffectInstance effect : effects) {
            if (effect.getEffect() == ModEffects.BURROWING.get()) {
               burrowingInstance = effect;
               break;
            }
         }

         if (burrowingInstance != null) {
            List<Component> tooltip = event.getToolTip();
            String boostText = burrowingInstance.getAmplifier() == 0 ? "50%" : "100%";
            Component header = Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE);
            Component statLine = Component.translatable("tooltip.fungidelight.burrowing_desc", new Object[]{boostText}).withStyle(ChatFormatting.BLUE);
            boolean headerExists = false;

            for (Component component : tooltip) {
               if (component.getString().equals(header.getString())) {
                  headerExists = true;
                  break;
               }
            }

            if (!headerExists) {
               tooltip.add(Component.empty());
               tooltip.add(header);
            }

            tooltip.add(statLine);
         }
      }
   }
}
