package io.github.mortuusars.exposure.commands.exposure;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.clientbound.CaptureStartS2CP;
import io.github.mortuusars.exposure.world.camera.capture.CaptureParameters;
import io.github.mortuusars.exposure.world.camera.capture.CaptureType;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.level.storage.ExposureIdentifier;
import java.util.function.Supplier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.HoverEvent.ItemStackInfo;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import io.github.mortuusars.exposure.backport.codec.BackportCodecs;

public class LoadCommand {
   public static LiteralArgumentBuilder<CommandSourceStack> get() {
      return (LiteralArgumentBuilder<CommandSourceStack>)Commands.literal("load")
         .then(
            Commands.argument("capture_properties", CompoundTagArgument.compoundTag())
               .executes(context -> load(context, CompoundTagArgument.getCompoundTag(context, "capture_properties")))
         );
   }

   private static int load(CommandContext<CommandSourceStack> context, CompoundTag properties) throws CommandSyntaxException {
      ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
      if (!properties.contains("id", 8)) {
         properties.putString("id", ExposureIdentifier.createId(player));
      }

      CaptureParameters captureParameters;
      try {
         RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, ((CommandSourceStack)context.getSource()).registryAccess());
         captureParameters = (CaptureParameters)BackportCodecs.getOrThrow(CaptureParameters.CODEC.parse(ops, properties));
      } catch (Exception var7) {
         ((CommandSourceStack)context.getSource()).sendFailure(Component.literal("Cannot decode properties: " + var7.getMessage()));
         return 0;
      }

      if (captureParameters.projection().isEmpty()) {
         ((CommandSourceStack)context.getSource()).sendFailure(Component.literal("Cannot load: missing 'projection' property."));
         return 0;
      } else {
         String exposureId = captureParameters.exposureId();
         Frame frame = Frame.EMPTY.toMutable().setIdentifier(ExposureIdentifier.id(exposureId)).toImmutable();
         Supplier<Component> msg = () -> {
            ItemStack photograph = new ItemStack((ItemLike)Exposure.Items.PHOTOGRAPH.get());
            Exposure.DataComponents.PHOTOGRAPH_FRAME.set(photograph, frame);
            return Component.literal("Loaded exposure: ")
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
         Packets.sendToClient(new CaptureStartS2CP(CaptureType.LOAD_COMMAND, captureParameters), player);
         return 0;
      }
   }
}
