package io.github.mortuusars.exposure.commands.exposure;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.clientbound.CaptureStartDebugRGBS2CP;
import io.github.mortuusars.exposure.network.packet.clientbound.ClearRenderingCacheS2CP;
import io.github.mortuusars.exposure.util.PointOfView;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.camera.CameraInHand;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.camera.ColorChannel;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.camera.capture.CaptureParameters;
import io.github.mortuusars.exposure.world.camera.capture.CaptureType;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.world.camera.frame.EntitiesInFrame;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.item.ChromaticSheetItem;
import io.github.mortuusars.exposure.world.item.DevelopedFilmItem;
import io.github.mortuusars.exposure.world.item.FilmRollItem;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import io.github.mortuusars.exposure.world.item.camera.CameraSettings;
import io.github.mortuusars.exposure.world.level.storage.ExposureIdentifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.HoverEvent.ItemStackInfo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import io.github.mortuusars.exposure.backport.util.Backport;

public class DebugCommand {
   public static LiteralArgumentBuilder<CommandSourceStack> get() {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                        "debug"
                     )
                     .then(Commands.literal("clear_rendering_cache").executes(DebugCommand::clearRenderingCache)))
                  .then(Commands.literal("highlight_entities_in_frame").executes(DebugCommand::highlightEntitiesInFrame)))
               .then(Commands.literal("expose_rgb").executes(DebugCommand::exposeRGB)))
            .then(Commands.literal("chromatic_from_last_three_exposures").executes(DebugCommand::chromaticFromLastThreeExposures)))
         .then(
            ((LiteralArgumentBuilder)Commands.literal("develop_film_in_hand").executes(context -> developFilmInHand(context, true)))
               .then(Commands.literal("keep_original").executes(context -> developFilmInHand(context, false)))
         );
   }

   private static int clearRenderingCache(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      CommandSourceStack stack = (CommandSourceStack)context.getSource();
      ServerPlayer player = stack.getPlayerOrException();
      Packets.sendToClient(ClearRenderingCacheS2CP.INSTANCE, player);
      return 0;
   }

   private static int highlightEntitiesInFrame(CommandContext<CommandSourceStack> context) {
      ExposureServer.debugHighlightEntitiesInFrame = !ExposureServer.debugHighlightEntitiesInFrame;
      ((CommandSourceStack)context.getSource())
         .sendSystemMessage(
            Component.translatable("system.exposure.debug.highlight_entities_in_frame." + (ExposureServer.debugHighlightEntitiesInFrame ? "on" : "off"))
               .withStyle(ChatFormatting.RED)
         );
      return 0;
   }

   private static int exposeRGB(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
      Camera cameraInHand = CameraInHand.find((CameraHolder)player);
      if (cameraInHand != null && !cameraInHand.isEmpty() && cameraInHand.getItemStack().getItem() instanceof CameraItem cameraItem) {
         ItemStack var17 = cameraInHand.getItemStack();
         ArrayList properties = new ArrayList();

         for (int i = 0; i < 3; i++) {
            ColorChannel channel = ColorChannel.values()[i];
            String exposureId = ExposureIdentifier.createId(player, channel.getSerializedName());
            CaptureParameters params = new CaptureParameters.Builder(exposureId)
               .setCameraID(cameraInHand.getId())
               .setCameraHolder((CameraHolder)player)
               .setFov(cameraItem.getFov(player.level, var17))
               .setCropFactor(cameraItem.getCropFactor())
               .setFilmProperties(cameraItem.getFilmProperties(var17).withType(ExposureType.BLACK_AND_WHITE))
               .setChromaticChannel(channel)
               .extraData(CaptureParameters.SHUTTER_SPEED, CameraSettings.SHUTTER_SPEED.getOrElse(var17, ShutterSpeed.DEFAULT))
               .build();
            properties.add(params);
            PointOfView pov = cameraItem.getPointOfView((CameraHolder)player, var17);
            double fov = cameraItem.getViewfinderFov(player.level, var17);
            List<BlockPos> positions = cameraItem.getPositionsInFrame((CameraHolder)player, pov, fov);
            List<LivingEntity> entities = EntitiesInFrame.get(player, pov, fov);
            Frame frame = cameraItem.createFrame((CameraHolder)player, player.getLevel(), var17, params, positions, entities);
            Supplier<Component> msg = () -> {
               ItemStack photograph = new ItemStack((ItemLike)Exposure.Items.PHOTOGRAPH.get());
               Exposure.DataComponents.PHOTOGRAPH_FRAME.set(photograph, frame);
               return Component.translatable("command.exposure.debug.expose_rgb.success.captured", new Object[]{channel.getSerializedName()})
                  .append(
                     Component.literal(exposureId)
                        .withStyle(
                           Style.EMPTY
                              .withClickEvent(new ClickEvent(Action.RUN_COMMAND, "/exposure show id " + exposureId))
                              .withHoverEvent(new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_ITEM, new ItemStackInfo(photograph)))
                              .withUnderlined(true)
                        )
                  );
            };
            ExposureServer.exposureRepository().expect(player, exposureId, (pl, id) -> ((CommandSourceStack)context.getSource()).sendSuccess(msg.get(), true));
            ExposureServer.frameHistory().add(player, frame);
         }

         Packets.sendToClient(new CaptureStartDebugRGBS2CP(CaptureType.DEBUG_RGB, properties), player);
         ((CommandSourceStack)context.getSource()).sendSuccess(Component.translatable("command.exposure.debug.expose_rgb.success.capturing"), true);
         return 0;
      } else {
         ((CommandSourceStack)context.getSource()).sendFailure(Component.translatable("command.exposure.debug.expose_rgb.fail.wrong_item"));
         return 0;
      }
   }

   private static int chromaticFromLastThreeExposures(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      CommandSourceStack stack = (CommandSourceStack)context.getSource();
      ServerPlayer player = stack.getPlayerOrException();
      List<Frame> allFrames = ExposureServer.frameHistory().getFramesOf(player).stream().filter(framex -> !framex.isChromatic()).toList();
      List<Frame> frames = new ArrayList<>(allFrames.subList(Math.max(allFrames.size() - 3, 0), allFrames.size()));
      if (frames.size() < 3) {
         stack.sendFailure(Component.translatable("command.exposure.debug.chromatic_from_last_three.fail.not_enough_frames"));
         return 1;
      } else {
         try {
            ChromaticSheetItem item = Exposure.Items.CHROMATIC_SHEET.get();
            ItemStack itemStack = new ItemStack(item);

            for (Frame frame : frames) {
               item.addLayer(itemStack, frame);
            }

            ItemStack photographStack = item.combineIntoPhotograph(player, itemStack, false);
            Frame frame = (Frame)Exposure.DataComponents.PHOTOGRAPH_FRAME.get(photographStack);
            Preconditions.checkState(frame != null, "Frame data cannot be empty after combining.");
            ExposureServer.frameHistory().add(player, frame);
            Supplier<Component> msg = () -> {
               String exposureId = frame.identifier().getId().orElseThrow();
               return Component.translatable("command.exposure.debug.chromatic_from_last_three.success.created")
                  .append(
                     Component.literal(exposureId)
                        .withStyle(
                           Style.EMPTY
                              .withClickEvent(new ClickEvent(Action.RUN_COMMAND, "/exposure show latest"))
                              .withHoverEvent(new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_ITEM, new ItemStackInfo(photographStack)))
                              .withUnderlined(true)
                        )
                  );
            };
            stack.sendSuccess(msg.get(), true);
            return 0;
         } catch (Exception var10) {
            stack.sendFailure(Component.translatable("command.exposure.debug.chromatic_from_last_three.fail.error", new Object[]{var10.getMessage()}));
            Exposure.LOGGER.error("Failed to create chromatic exposure: ", var10);
            return 1;
         }
      }
   }

   private static int developFilmInHand(CommandContext<CommandSourceStack> context, boolean replace) throws CommandSyntaxException {
      CommandSourceStack stack = (CommandSourceStack)context.getSource();
      ServerPlayer player = stack.getPlayerOrException();

      for (InteractionHand hand : InteractionHand.values()) {
         ItemStack itemInHand = player.getItemInHand(hand);
         if (itemInHand.getItem() instanceof FilmRollItem filmRollItem) {
            DevelopedFilmItem itemType = filmRollItem.getType() == ExposureType.COLOR
               ? Exposure.Items.DEVELOPED_COLOR_FILM.get()
               : Exposure.Items.DEVELOPED_BLACK_AND_WHITE_FILM.get();
            ItemStack developedFilmStack = Backport.transmuteCopy(itemInHand, itemType);
            if (replace) {
               player.setItemInHand(hand, developedFilmStack);
            } else if (!player.addItem(developedFilmStack)) {
               player.drop(developedFilmStack, true, false);
            }

            stack.sendSuccess(Component.translatable("command.exposure.debug.develop.success", new Object[]{itemInHand.getDisplayName()}), true);
            return 0;
         }
      }

      stack.sendFailure(Component.translatable("command.exposure.debug.develop.fail.wrong_item"));
      return 1;
   }
}
