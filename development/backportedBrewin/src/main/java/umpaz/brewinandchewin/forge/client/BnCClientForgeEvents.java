package umpaz.brewinandchewin.forge.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.ModList;
import umpaz.brewinandchewin.client.utility.BnCClientTextUtils;
import umpaz.brewinandchewin.common.registry.BnCEffects;
import umpaz.brewinandchewin.common.tag.BnCTags;
import umpaz.brewinandchewin.common.utility.BnCTextUtils;
import umpaz.brewinandchewin.forge.client.integration.IntoxicationAppleSkinCompat;

/** Client game-bus events (port of TipsyEffects and the camera half of TipsyDrunkRendererMixin). */
public class BnCClientForgeEvents {
   public static void register() {
      MinecraftForge.EVENT_BUS.addListener(BnCClientForgeEvents::onChat);
      MinecraftForge.EVENT_BUS.addListener(BnCClientForgeEvents::onNameTag);
      MinecraftForge.EVENT_BUS.addListener(BnCClientForgeEvents::onCameraAngles);
      MinecraftForge.EVENT_BUS.addListener(BnCClientForgeEvents::onItemTooltip);
      if (ModList.get().isLoaded("appleskin")) {
         IntoxicationAppleSkinCompat.init();
      }
   }

   /** Scrambles a player's chat message content (not the sender name around it), see BnCClientTextUtils. */
   private static void onChat(ClientChatReceivedEvent event) {
      PlayerChatMessage message = event.getPlayerChatMessage();
      if (event.isSystem() || message == null || event.getBoundChatType() == null) {
         return;
      }
      Component scrambled = BnCClientTextUtils.scrambleChatContent(message.serverContent());
      if (scrambled != null) {
         event.setMessage(event.getBoundChatType().decorate(scrambled));
      }
   }

   /** Port: blocks in the keg's heat/freeze tags say so (Vinery's cold storage reads the same tags), use on its own line. */
   private static void onItemTooltip(ItemTooltipEvent event) {
      if (!(event.getItemStack().getItem() instanceof BlockItem item)) {
         return;
      }
      BlockState state = item.getBlock().defaultBlockState();
      if (state.is(BnCTags.Blocks.HEAT_SOURCES)) {
         event.getToolTip().add(BnCTextUtils.getTranslation("tooltip.heat_source").withStyle(ChatFormatting.GOLD));
         event.getToolTip().add(BnCTextUtils.getTranslation("tooltip.climate_source.use").withStyle(ChatFormatting.GOLD));
      } else if (state.is(BnCTags.Blocks.FREEZE_SOURCES)) {
         event.getToolTip().add(BnCTextUtils.getTranslation("tooltip.freeze_source").withStyle(ChatFormatting.AQUA));
         event.getToolTip().add(BnCTextUtils.getTranslation("tooltip.climate_source.use").withStyle(ChatFormatting.AQUA));
      }
   }

   private static void onNameTag(RenderNameTagEvent event) {
      event.setContent(BnCClientTextUtils.nameTagRenderer(event.getContent()));
   }

   /**
    * Camera spin: yaw and a pitch/roll wobble grow with the Tipsy level (cap 11), scaled by Distortion Effects. Port:
    * upstream rotates the level pose and fixes culling to match; Forge's camera angle event turns the camera itself, so
    * culling already follows. The sub-block positional drift is left out.
    */
   private static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
      Minecraft minecraft = Minecraft.getInstance();
      Player player = minecraft.player;
      if (player == null || !player.hasEffect(BnCEffects.TIPSY)) {
         return;
      }
      float distortionScale = minecraft.options.screenEffectScale().get().floatValue();
      if (distortionScale <= 0.0F) {
         return;
      }
      float ticks = (float)player.tickCount + (float)event.getPartialTick();
      int strength = Math.min(player.getEffect(BnCEffects.TIPSY).getAmplifier(), 11);
      float scaledStrength = (float)strength * distortionScale;
      float wobble = Mth.sin(27.0F + ticks * 0.0132F) * scaledStrength * Mth.SQRT_OF_TWO / 2.0F;
      event.setYaw(event.getYaw() + Mth.cos(3.0F + ticks * 0.0295F) * scaledStrength);
      event.setPitch(event.getPitch() + wobble);
      event.setRoll(event.getRoll() + wobble);
   }
}
