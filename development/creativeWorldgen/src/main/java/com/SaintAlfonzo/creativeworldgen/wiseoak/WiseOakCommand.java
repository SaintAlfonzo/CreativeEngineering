package com.SaintAlfonzo.creativeworldgen.wiseoak;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collection;

/**
 * /wiseoak trust (feature_writeup/wise_oak.md 4.8), op level 2. {@code <pos> <player>} reads a player's trust on the tree
 * at those coordinates and {@code <pos> <player> <value>} sets it. The player is a username (online, or known to the
 * server's profile cache) or a selector. Runs in the source's dimension; use /execute in for another one.
 */
public final class WiseOakCommand {

    private static final SimpleCommandExceptionType NOT_A_TREE =
            new SimpleCommandExceptionType(Component.literal("No Wise Oak at that position"));

    private WiseOakCommand() {}

    @SubscribeEvent
    public static void onRegister(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("wiseoak").requires(s -> s.hasPermission(2))
                .then(Commands.literal("trust")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                        .executes(WiseOakCommand::get)
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 100))
                                                .executes(WiseOakCommand::set))))));
    }

    private static WiseOakTrustAccess tree(CommandContext<CommandSourceStack> c, BlockPos pos) throws CommandSyntaxException {
        if (c.getSource().getLevel().getBlockEntity(pos) instanceof WiseOakTrustAccess tree) return tree;
        throw NOT_A_TREE.create();
    }

    private static int get(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(c, "pos");
        WiseOakTrustAccess tree = tree(c, pos);
        int trust = 0;
        for (GameProfile profile : GameProfileArgument.getGameProfiles(c, "player")) {
            trust = tree.creativeworldgen$getTrust(profile.getId());
            String line = "Trust of " + profile.getName() + " at " + pos.toShortString() + ": " + trust;
            c.getSource().sendSuccess(Component.literal(line), false);
        }
        return trust;
    }

    private static int set(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(c, "pos");
        WiseOakTrustAccess tree = tree(c, pos);
        int value = IntegerArgumentType.getInteger(c, "value");
        Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(c, "player");
        for (GameProfile profile : profiles) {
            tree.creativeworldgen$setTrust(profile.getId(), value);
            String line = "Set trust of " + profile.getName() + " at " + pos.toShortString() + " to " + value;
            c.getSource().sendSuccess(Component.literal(line), true);
        }
        // The client reads trust for the tree's dialogue, so the change has to reach it.
        ServerLevel level = c.getSource().getLevel();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        BlockState state = level.getBlockState(pos);
        blockEntity.setChanged();
        level.sendBlockUpdated(pos, state, state, 3);
        return value;
    }
}
