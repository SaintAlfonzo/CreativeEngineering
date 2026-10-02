package io.github.mortuusars.exposure.commands.exposure;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.commands.argument.ExposureLookArgument;
import io.github.mortuusars.exposure.commands.argument.SizeMultiplierArgument;
import io.github.mortuusars.exposure.commands.suggestion.ExposureIdSuggestionProvider;
import io.github.mortuusars.exposure.data.export.ExportLook;
import io.github.mortuusars.exposure.data.export.ExportSize;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.clientbound.ExportS2CP;
import io.github.mortuusars.exposure.network.packet.clientbound.ExportStopS2CP;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.HoverEvent.Action;
import net.minecraft.server.level.ServerPlayer;

public class ExportCommand {
   private static final Map<Integer, Long> EXPORT_CONFIRMATIONS = new HashMap<>();

   public static LiteralArgumentBuilder<CommandSourceStack> get() {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("export")
                  .requires(stack -> stack.hasPermission(3)))
               .then(id()))
            .then(all()))
         .then(Commands.literal("stop").executes(context -> {
            Packets.sendToClient(ExportStopS2CP.INSTANCE, ((CommandSourceStack)context.getSource()).getPlayerOrException());
            return 0;
         }));
   }

   private static ArgumentBuilder<CommandSourceStack, ?> id() {
      return Commands.literal("id")
         .then(
            ((RequiredArgumentBuilder)Commands.argument("id", StringArgumentType.string())
                  .suggests(new ExposureIdSuggestionProvider())
                  .executes(
                     context -> exportExposures(
                           (CommandSourceStack)context.getSource(), List.of(StringArgumentType.getString(context, "id")), ExportSize.X1, ExportLook.REGULAR
                        )
                  ))
               .then(
                  ((RequiredArgumentBuilder)Commands.argument("size", new SizeMultiplierArgument())
                        .executes(
                           context -> exportExposures(
                                 (CommandSourceStack)context.getSource(),
                                 List.of(StringArgumentType.getString(context, "id")),
                                 SizeMultiplierArgument.getSize(context, "size"),
                                 ExportLook.REGULAR
                              )
                        ))
                     .then(
                        Commands.argument("look", new ExposureLookArgument())
                           .executes(
                              context -> exportExposures(
                                    (CommandSourceStack)context.getSource(),
                                    List.of(StringArgumentType.getString(context, "id")),
                                    SizeMultiplierArgument.getSize(context, "size"),
                                    ExposureLookArgument.getLook(context, "look")
                                 )
                           )
                     )
               )
         );
   }

   private static ArgumentBuilder<CommandSourceStack, ?> all() {
      return ((LiteralArgumentBuilder)Commands.literal("all")
            .executes(context -> exportAll((CommandSourceStack)context.getSource(), ExportSize.X1, ExportLook.REGULAR)))
         .then(
            ((RequiredArgumentBuilder)Commands.argument("size", new SizeMultiplierArgument())
                  .executes(context -> exportAll((CommandSourceStack)context.getSource(), SizeMultiplierArgument.getSize(context, "size"), ExportLook.REGULAR)))
               .then(
                  Commands.argument("look", new ExposureLookArgument())
                     .executes(
                        context -> exportAll(
                              (CommandSourceStack)context.getSource(),
                              SizeMultiplierArgument.getSize(context, "size"),
                              ExposureLookArgument.getLook(context, "look")
                           )
                     )
               )
         );
   }

   private static int exportAll(CommandSourceStack stack, ExportSize size, ExportLook look) throws CommandSyntaxException {
      List<String> ids = ExposureServer.exposureRepository().getAllIds();
      return confirmExportAll(stack, ids, size, look) ? exportExposures(stack, ids, size, look) : 0;
   }

   private static boolean confirmExportAll(CommandSourceStack stack, List<String> ids, ExportSize size, ExportLook look) throws CommandSyntaxException {
      ServerPlayer player = stack.getPlayerOrException();
      int count = ids.size();
      if (count < 50) {
         return true;
      } else {
         Long timestamp = EXPORT_CONFIRMATIONS.get(player.getId());
         boolean confirmed = timestamp != null && player.level.getGameTime() - timestamp < 6000L;
         if (!confirmed) {
            EXPORT_CONFIRMATIONS.put(player.getId(), player.level.getGameTime());
            stack.sendSuccess(Component.translatable("command.exposure.export.confirmation", new Object[]{count})
                     .withStyle(Style.EMPTY.withColor(16770531))
                     .append(
                        Component.translatable("command.exposure.export.confirm")
                           .withStyle(
                              Style.EMPTY
                                 .withColor(16741225)
                                 .withUnderlined(true)
                                 .withHoverEvent(new HoverEvent(Action.SHOW_TEXT, Component.translatable("command.exposure.export.confirm.tooltip")))
                                 .withClickEvent(
                                    new ClickEvent(
                                       net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND,
                                       "/exposure export all " + size.getSerializedName() + " " + look.getSerializedName()
                                    )
                                 )
                           )
                     ),
               true
            );
            return false;
         } else {
            EXPORT_CONFIRMATIONS.remove(player.getId());
            return true;
         }
      }
   }

   private static int exportExposures(CommandSourceStack stack, List<String> exposureIds, ExportSize size, ExportLook look) throws CommandSyntaxException {
      ServerPlayer player = stack.getPlayerOrException();
      Packets.sendToClient(new ExportS2CP(exposureIds, size, look), player);
      return 0;
   }
}
