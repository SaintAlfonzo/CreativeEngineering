package io.github.mortuusars.exposure.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.mortuusars.exposure.commands.exposure.DebugCommand;
import io.github.mortuusars.exposure.commands.exposure.ExportCommand;
import io.github.mortuusars.exposure.commands.exposure.ExposeCommand;
import io.github.mortuusars.exposure.commands.exposure.LoadCommand;
import io.github.mortuusars.exposure.commands.exposure.PaletteCommand;
import io.github.mortuusars.exposure.commands.exposure.ShowCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ExposureCommand {
   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                 "exposure"
                              )
                              .requires(stack -> stack.hasPermission(2)))
                           .then(LoadCommand.get()))
                        .then(ExposeCommand.get()))
                     .then(ExportCommand.get()))
                  .then(ShowCommand.get()))
               .then(PaletteCommand.get()))
            .then(DebugCommand.get())
      );
   }
}
