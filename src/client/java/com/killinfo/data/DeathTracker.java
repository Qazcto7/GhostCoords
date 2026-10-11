package com.killinfo.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class DeathTracker {
    public static final Logger LOGGER = LoggerFactory.getLogger("KillInfo");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("killinfo.json");

    private static DeathData globalLastDeath = null;
    private static final Map<String, DeathData> deathsByWorld = new HashMap<>();

    public static void init() {
        loadData();
    }

    public static synchronized void recordDeath(LocalPlayer player) {
        recordDeath(player, null);
    }

    public static synchronized void recordDeath(LocalPlayer player, Component causeComponent) {
        if (player == null) return;

        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        String dimension = player.level().dimension().identifier().toString();
        String cause = (causeComponent != null) ? causeComponent.getString() : "";
        long now = System.currentTimeMillis();
        String worldKey = getCurrentWorldKey();

        // 3 saniye içinde birden fazla çağrı geldiyse (örneğin hem DeathScreen hem paket tetiklendiyse)
        // koordinatları ezmeyelim, varsa sadece nedeni güncelleyelim.
        if (globalLastDeath != null && (now - globalLastDeath.getTimestamp()) < 3000) {
            if (!cause.isBlank() && globalLastDeath.getCause().equals("Bilinmiyor")) {
                DeathData updated = new DeathData(
                        globalLastDeath.getX(),
                        globalLastDeath.getY(),
                        globalLastDeath.getZ(),
                        globalLastDeath.getDimension(),
                        cause,
                        globalLastDeath.getTimestamp(),
                        globalLastDeath.getServerOrWorld()
                );
                globalLastDeath = updated;
                deathsByWorld.put(worldKey, updated);
                saveData();
            }
            return;
        }

        DeathData data = new DeathData(x, y, z, dimension, cause, now, worldKey);
        globalLastDeath = data;
        deathsByWorld.put(worldKey, data);
        saveData();

        LOGGER.info("[KillInfo] Ölüm konumu kaydedildi: X:{} Y:{} Z:{} ({})", data.getBlockX(), data.getBlockY(), data.getBlockZ(), dimension);
    }

    public static synchronized DeathData getLastDeath() {
        String worldKey = getCurrentWorldKey();
        if (deathsByWorld.containsKey(worldKey)) {
            return deathsByWorld.get(worldKey);
        }
        return globalLastDeath;
    }

    public static synchronized void clear() {
        globalLastDeath = null;
        deathsByWorld.clear();
        saveData();
    }

    public static String getCurrentWorldKey() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getCurrentServer() != null) {
            return "multiplayer:" + mc.getCurrentServer().ip;
        } else if (mc.getSingleplayerServer() != null) {
            return "singleplayer:" + mc.getSingleplayerServer().getWorldData().getLevelName();
        }
        return "default";
    }

    public static Component formatDeathInfo(DeathData death) {
        if (death == null) {
            return Component.literal("[KillInfo] ")
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
                    .append(Component.literal("Henüz kaydedilmiş bir ölüm konumu bulunmuyor.")
                            .withStyle(ChatFormatting.GRAY));
        }

        MutableComponent message = Component.empty();

        message.append(Component.literal("----------------------------------------\n").withStyle(ChatFormatting.DARK_GRAY));
        message.append(Component.literal("☠ ").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        message.append(Component.literal("Son Ölüm Bilgisi:\n").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        String coordStr = death.getSimpleCoords();
        MutableComponent coordComp = Component.literal(
                String.format("X: %d | Y: %d | Z: %d", death.getBlockX(), death.getBlockY(), death.getBlockZ())
        )
                .withStyle(ChatFormatting.AQUA, ChatFormatting.UNDERLINE)
                .withStyle(style -> style
                        .withClickEvent(new ClickEvent.CopyToClipboard(coordStr))
                        .withHoverEvent(new HoverEvent.ShowText(
                                Component.literal("Panoya kopyalamak için tıkla:\n" + coordStr).withStyle(ChatFormatting.YELLOW)
                        )));

        message.append(Component.literal("  ● Koordinatlar: ").withStyle(ChatFormatting.GRAY))
                .append(coordComp)
                .append(Component.literal("\n"));

        message.append(Component.literal("  ● Boyut: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(death.getDimensionDisplayName()).withStyle(ChatFormatting.GREEN))
                .append(Component.literal(" (" + death.getDimension() + ")\n").withStyle(ChatFormatting.DARK_GRAY));

        if (death.getCause() != null && !death.getCause().isBlank() && !death.getCause().equals("Bilinmiyor")) {
            message.append(Component.literal("  ● Ölüm Nedeni: ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(death.getCause()).withStyle(ChatFormatting.WHITE))
                    .append(Component.literal("\n"));
        }

        message.append(Component.literal("  ● Zaman: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(death.getTimeAgo()).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" [" + death.getFormattedTime() + "]\n").withStyle(ChatFormatting.DARK_GRAY));

        MutableComponent copyButton = Component.literal("[Panoya Kopyala]")
                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)
                .withStyle(style -> style
                        .withClickEvent(new ClickEvent.CopyToClipboard(coordStr))
                        .withHoverEvent(new HoverEvent.ShowText(
                                Component.literal("Koordinatları panoya kopyalamak için tıkla").withStyle(ChatFormatting.YELLOW)
                        )));

        message.append(Component.literal("  "))
                .append(copyButton)
                .append(Component.literal("\n"))
                .append(Component.literal("----------------------------------------").withStyle(ChatFormatting.DARK_GRAY));

        return message;
    }

    private static void saveData() {
        try {
            StorageModel model = new StorageModel();
            model.globalLastDeath = globalLastDeath;
            model.deathsByWorld = deathsByWorld;

            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(model, writer);
            }
        } catch (Exception e) {
            LOGGER.error("Ölüm bilgileri dosyaya kaydedilirken hata oluştu", e);
        }
    }

    private static void loadData() {
        if (!Files.exists(CONFIG_PATH)) return;

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            StorageModel model = GSON.fromJson(reader, StorageModel.class);
            if (model != null) {
                globalLastDeath = model.globalLastDeath;
                if (model.deathsByWorld != null) {
                    deathsByWorld.putAll(model.deathsByWorld);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Ölüm bilgileri dosyadan yüklenirken hata oluştu", e);
        }
    }

    private static class StorageModel {
        DeathData globalLastDeath;
        Map<String, DeathData> deathsByWorld;
    }
}
