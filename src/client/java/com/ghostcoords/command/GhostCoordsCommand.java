package com.ghostcoords.command;

import com.ghostcoords.data.DeathData;
import com.ghostcoords.data.DeathTracker;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.List;

public class GhostCoordsCommand {
    private static final List<String> COMMAND_NAMES = List.of(
            "ghostcoords",
            "killinfo",
            "deathcoords",
            "deathinfo",
            "lastdeath"
    );

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        for (String name : COMMAND_NAMES) {
            dispatcher.register(buildCommand(name));
        }
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildCommand(String literal) {
        return ClientCommands.literal(literal)
                .executes(GhostCoordsCommand::executeShow)
                .then(ClientCommands.literal("copy").executes(GhostCoordsCommand::executeCopy))
                .then(ClientCommands.literal("clear").executes(GhostCoordsCommand::executeClear))
                .then(ClientCommands.literal("help").executes(GhostCoordsCommand::executeHelp));
    }

    private static int executeShow(CommandContext<FabricClientCommandSource> context) {
        DeathData death = DeathTracker.getLastDeath();
        context.getSource().sendFeedback(DeathTracker.formatDeathInfo(death));
        return 1;
    }

    private static int executeCopy(CommandContext<FabricClientCommandSource> context) {
        DeathData death = DeathTracker.getLastDeath();
        if (death == null) {
            context.getSource().sendFeedback(
                    Component.translatable("ghostcoords.prefix")
                            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
                            .append(Component.translatable("ghostcoords.copy.empty")
                                    .withStyle(ChatFormatting.RED))
            );
            return 0;
        }

        String coords = death.getSimpleCoords();
        try {
            if (Minecraft.getInstance().keyboardHandler == null) {
                throw new IllegalStateException("KeyboardHandler is unavailable");
            }
            Minecraft.getInstance().keyboardHandler.setClipboard(coords);
        } catch (Exception e) {
            DeathTracker.LOGGER.warn("[GhostCoords] Failed to copy coordinates to clipboard", e);
            context.getSource().sendFeedback(
                    Component.translatable("ghostcoords.prefix")
                            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
                            .append(Component.translatable("ghostcoords.copy.failed")
                                    .withStyle(ChatFormatting.RED))
            );
            return 0;
        }

        context.getSource().sendFeedback(
                Component.translatable("ghostcoords.prefix")
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)
                        .append(Component.translatable("ghostcoords.copy.success")
                                .withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal(coords)
                                .withStyle(ChatFormatting.AQUA, ChatFormatting.UNDERLINE))
        );
        return 1;
    }

    private static int executeClear(CommandContext<FabricClientCommandSource> context) {
        DeathTracker.clear();
        context.getSource().sendFeedback(
                Component.translatable("ghostcoords.prefix")
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)
                        .append(Component.translatable("ghostcoords.clear.success")
                                .withStyle(ChatFormatting.GRAY))
        );
        return 1;
    }

    private static int executeHelp(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(
                Component.translatable("ghostcoords.help.header").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)
                        .append(Component.literal("\n"))
                        .append(Component.literal("/ghostcoords").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                        .append(Component.translatable("ghostcoords.help.show").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal("\n"))
                        .append(Component.literal("/ghostcoords copy").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                        .append(Component.translatable("ghostcoords.help.copy").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal("\n"))
                        .append(Component.literal("/ghostcoords clear").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                        .append(Component.translatable("ghostcoords.help.clear").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal("\n"))
                        .append(Component.translatable("ghostcoords.help.aliases").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.literal("\n"))
                        .append(Component.translatable("ghostcoords.help.footer").withStyle(ChatFormatting.DARK_GRAY))
        );
        return 1;
    }
}
