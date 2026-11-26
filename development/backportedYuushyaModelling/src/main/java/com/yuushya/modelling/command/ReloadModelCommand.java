// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 command/ReloadModelCommand.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.yuushya.modelling.network.ReloadModelPacket;
import com.yuushya.modelling.network.YuushyaModellingNetwork;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;

public class ReloadModelCommand {
   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      dispatcher.register(
         (LiteralArgumentBuilder)Commands.literal("yuushya_reload_model")
            .then(
               ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("reload")
                           .then(
                              Commands.literal("blocks")
                                 .executes(
                                    context -> {
                                       ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayer();
                                       if (player == null) {
                                          return 0;
                                       } else {
                                          YuushyaModellingNetwork.INSTANCE
                                             .sendTo(
                                                new ReloadModelPacket(ReloadModelCommand.ReloadType.BLOCKS),
                                                player.connection.connection,
                                                NetworkDirection.PLAY_TO_CLIENT
                                             );
                                          player.sendSystemMessage(Component.translatable("command.yuushya_modelling.reload_succeed"));
                                          return 1;
                                       }
                                    }
                                 )
                           ))
                        .then(
                           Commands.literal("items")
                              .executes(
                                 context -> {
                                    ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayer();
                                    if (player == null) {
                                       return 0;
                                    } else {
                                       YuushyaModellingNetwork.INSTANCE
                                          .sendTo(
                                             new ReloadModelPacket(ReloadModelCommand.ReloadType.ITEMS),
                                             player.connection.connection,
                                             NetworkDirection.PLAY_TO_CLIENT
                                          );
                                       player.sendSystemMessage(Component.translatable("command.yuushya_modelling.reload_succeed"));
                                       return 1;
                                    }
                                 }
                              )
                        ))
                     // PHASE-B: .then(Commands.literal("texts")...) sends ReloadType.TEXTS (Text Block reload).
                  .then(
                     Commands.literal("all")
                        .executes(
                           context -> {
                              ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayer();
                              if (player == null) {
                                 return 0;
                              } else {
                                 YuushyaModellingNetwork.INSTANCE
                                    .sendTo(new ReloadModelPacket(ReloadModelCommand.ReloadType.ALL), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
                                 player.sendSystemMessage(Component.translatable("command.yuushya_modelling.reload_succeed"));
                                 return 1;
                              }
                           }
                        )
                  )
            )
      );
   }

   public static enum ReloadType {
      BLOCKS,
      ITEMS,
      TEXTS,
      ALL;
   }
}
