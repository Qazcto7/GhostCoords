package com.killinfo.command;

import com.killinfo.data.DeathData;
import com.killinfo.data.DeathTracker;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class KillInfoCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommands.literal("killinfo")
                .executes(context -> {
                    DeathData death = DeathTracker.getLastDeath();
                    context.getSource().sendFeedback(DeathTracker.formatDeathInfo(death));
                    return 1;
                })
                .then(ClientCommands.literal("copy")
                        .executes(context -> {
                            DeathData death = DeathTracker.getLastDeath();
                            if (death == null) {
                                context.getSource().sendFeedback(
                                        Component.literal("[KillInfo] Kopyalanacak bir ölüm konumu bulunamadı.")
                                                .withStyle(ChatFormatting.RED)
                                );
                                return 0;
                            }
                            String coords = death.getSimpleCoords();
                            Minecraft.getInstance().keyboardHandler.setClipboard(coords);
                            context.getSource().sendFeedback(
                                    Component.literal("[KillInfo] ")
                                            .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)
                                            .append(Component.literal("Koordinatlar panoya kopyalandı: ")
                                                    .withStyle(ChatFormatting.YELLOW))
                                            .append(Component.literal(coords)
                                                    .withStyle(ChatFormatting.AQUA, ChatFormatting.UNDERLINE))
                            );
                            return 1;
                        })
                )
                .then(ClientCommands.literal("clear")
                        .executes(context -> {
                            DeathTracker.clear();
                            context.getSource().sendFeedback(
                                    Component.literal("[KillInfo] ")
                                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                                            .append(Component.literal("Kayıtlı ölüm bilgileri temizlendi.")
                                                    .withStyle(ChatFormatting.GRAY))
                            );
                            return 1;
                        })
                )
                .then(ClientCommands.literal("help")
                        .executes(context -> {
                            context.getSource().sendFeedback(
                                    Component.literal("---------------- [KillInfo Yardım] ----------------\n").withStyle(ChatFormatting.DARK_GRAY)
                                            .append(Component.literal("/killinfo").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                                            .append(Component.literal(" - Son öldüğünüz yerin koordinatlarını gösterir.\n").withStyle(ChatFormatting.GRAY))
                                            .append(Component.literal("/killinfo copy").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                                            .append(Component.literal(" - Koordinatları doğrudan panoya kopyalar.\n").withStyle(ChatFormatting.GRAY))
                                            .append(Component.literal("/killinfo clear").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                                            .append(Component.literal(" - Kaydedilen ölüm koordinatlarını temizler.\n").withStyle(ChatFormatting.GRAY))
                                            .append(Component.literal("--------------------------------------------------").withStyle(ChatFormatting.DARK_GRAY))
                            );
                            return 1;
                        })
                )
        );
    }
}
