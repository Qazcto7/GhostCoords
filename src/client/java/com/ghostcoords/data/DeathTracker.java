package com.ghostcoords.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DeathTracker {
    public static final Logger LOGGER = LoggerFactory.getLogger("GhostCoords");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("ghostcoords.json");
    private static final Path LEGACY_CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("killinfo.json");

    private static DeathData globalLastDeath = null;
    private static final Map<String, DeathData> deathsByWorld = new HashMap<>();
    private static volatile boolean isCleared = false;

    public static synchronized void init() {
        loadData();
    }

    public static synchronized void recordDeath(LocalPlayer player) {
        recordDeath(player, null);
    }

    public static synchronized void recordDeath(LocalPlayer player, Component causeComponent) {
        if (player == null) return;

        isCleared = false;

        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        String dimension = (player.level() != null && player.level().dimension() != null)
                ? player.level().dimension().identifier().toString()
                : "minecraft:overworld";
        String cause = (causeComponent != null) ? causeComponent.getString().trim() : "";
        long now = System.currentTimeMillis();
        String worldKey = getCurrentWorldKey();

        DeathData previous = deathsByWorld.get(worldKey);
        if (previous == null) {
            previous = globalLastDeath;
        }

        // Prevent duplicate triggers for the exact same death event (World + Dimension + Distance + Time)
        if (previous != null) {
            boolean sameWorld = worldKey.equalsIgnoreCase(previous.getServerOrWorld());
            boolean sameDimension = dimension.equalsIgnoreCase(previous.getDimension());
            double dx = x - previous.getX();
            double dy = y - previous.getY();
            double dz = z - previous.getZ();
            double distSq = dx * dx + dy * dy + dz * dz;
            boolean withinTimeWindow = (now - previous.getTimestamp()) < 2500;

            if (sameWorld && sameDimension && distSq < 9.0 && withinTimeWindow) {
                if (!cause.isBlank() && !previous.hasDetailedCause()) {
                    DeathData updated = new DeathData(
                            previous.getX(),
                            previous.getY(),
                            previous.getZ(),
                            previous.getDimension(),
                            cause,
                            previous.getTimestamp(),
                            previous.getServerOrWorld()
                    );
                    globalLastDeath = updated;
                    deathsByWorld.put(worldKey, updated);
                    saveData();
                }
                return;
            }
        }

        DeathData data = new DeathData(x, y, z, dimension, cause, now, worldKey);
        globalLastDeath = data;
        deathsByWorld.put(worldKey, data);
        saveData();

        LOGGER.info("[GhostCoords] Death recorded at X:{} Y:{} Z:{} ({}) - {}",
                data.getBlockX(), data.getBlockY(), data.getBlockZ(), dimension, worldKey);
    }

    public static synchronized DeathData getLastDeath() {
        if (isCleared) {
            return null;
        }

        String worldKey = getCurrentWorldKey();
        if (deathsByWorld.containsKey(worldKey) && deathsByWorld.get(worldKey) != null) {
            return deathsByWorld.get(worldKey);
        }

        // Fallback to vanilla Minecraft's client-side last death location (Recovery Compass data)
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            try {
                Optional<GlobalPos> vanillaDeathOpt = mc.player.getLastDeathLocation();
                if (vanillaDeathOpt.isPresent()) {
                    GlobalPos globalPos = vanillaDeathOpt.get();
                    BlockPos pos = globalPos.pos();
                    String dim = globalPos.dimension().identifier().toString();
                    DeathData recovered = new DeathData(
                            pos.getX() + 0.5,
                            pos.getY(),
                            pos.getZ() + 0.5,
                            dim,
                            DeathData.VANILLA_CAUSE_SENTINEL,
                            System.currentTimeMillis(),
                            worldKey
                    );
                    globalLastDeath = recovered;
                    deathsByWorld.put(worldKey, recovered);
                    saveData();
                    return recovered;
                }
            } catch (Exception ignored) {
            }
        }

        return globalLastDeath;
    }

    public static synchronized void clear() {
        globalLastDeath = null;
        deathsByWorld.clear();
        isCleared = true;
        saveData();
    }

    public static String getCurrentWorldKey() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.getCurrentServer() != null && mc.getCurrentServer().ip != null) {
                return "multiplayer:" + mc.getCurrentServer().ip;
            }
            if (mc.getSingleplayerServer() != null && mc.getSingleplayerServer().getWorldData() != null) {
                return "singleplayer:" + mc.getSingleplayerServer().getWorldData().getLevelName();
            }
            if (mc.level != null && mc.level.dimension() != null) {
                return "world:" + mc.level.dimension().identifier().toString();
            }
        } catch (Exception ignored) {
        }
        return "default";
    }

    public static Component formatDeathInfo(DeathData death) {
        if (death == null) {
            return Component.translatable("ghostcoords.prefix")
                    .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)
                    .append(Component.translatable("ghostcoords.no_death")
                            .withStyle(ChatFormatting.GRAY));
        }

        MutableComponent message = Component.empty();

        message.append(Component.translatable("ghostcoords.header").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .append(Component.literal("\n"));

        String coordStr = death.getSimpleCoords();
        MutableComponent coordComp = Component.literal(
                String.format("X: %d | Y: %d | Z: %d", death.getBlockX(), death.getBlockY(), death.getBlockZ())
        )
                .withStyle(ChatFormatting.AQUA, ChatFormatting.UNDERLINE)
                .withStyle(style -> style
                        .withClickEvent(new ClickEvent.CopyToClipboard(coordStr))
                        .withHoverEvent(new HoverEvent.ShowText(
                                Component.translatable("ghostcoords.button.copy.hover", coordStr)
                                        .withStyle(ChatFormatting.YELLOW)
                        )));

        message.append(Component.translatable("ghostcoords.label.coords").withStyle(ChatFormatting.GRAY))
                .append(coordComp)
                .append(Component.literal("\n"));

        message.append(Component.translatable("ghostcoords.label.dimension").withStyle(ChatFormatting.GRAY))
                .append(death.getDimensionDisplayComponent().copy().withStyle(ChatFormatting.GREEN))
                .append(Component.literal(" (" + death.getDimension() + ")\n").withStyle(ChatFormatting.DARK_GRAY));

        String currentWorld = getCurrentWorldKey();
        boolean isOtherWorld = !currentWorld.equalsIgnoreCase(death.getServerOrWorld());
        message.append(Component.translatable("ghostcoords.label.world").withStyle(ChatFormatting.GRAY))
                .append(death.getServerOrWorldComponent().copy().withStyle(ChatFormatting.AQUA));
        if (isOtherWorld && !"default".equalsIgnoreCase(death.getServerOrWorld())) {
            message.append(Component.translatable("ghostcoords.other_world").withStyle(ChatFormatting.YELLOW));
        }
        message.append(Component.literal("\n"));

        if (death.hasKnownCause()) {
            message.append(Component.translatable("ghostcoords.label.cause").withStyle(ChatFormatting.GRAY))
                    .append(death.getCauseComponent().copy().withStyle(ChatFormatting.WHITE))
                    .append(Component.literal("\n"));
        }

        message.append(Component.translatable("ghostcoords.label.time").withStyle(ChatFormatting.GRAY))
                .append(death.getTimeAgoComponent().copy().withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" [").withStyle(ChatFormatting.DARK_GRAY))
                .append(death.getFormattedTimeComponent().copy().withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal("]\n").withStyle(ChatFormatting.DARK_GRAY));

        MutableComponent copyButton = Component.translatable("ghostcoords.button.copy")
                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)
                .withStyle(style -> style
                        .withClickEvent(new ClickEvent.CopyToClipboard(coordStr))
                        .withHoverEvent(new HoverEvent.ShowText(
                                Component.translatable("ghostcoords.button.copy.hover", coordStr)
                                        .withStyle(ChatFormatting.YELLOW)
                        )));

        String tpCommand = death.getTeleportCommand();
        MutableComponent tpButton = Component.translatable("ghostcoords.button.tp")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)
                .withStyle(style -> style
                        .withClickEvent(new ClickEvent.SuggestCommand(tpCommand))
                        .withHoverEvent(new HoverEvent.ShowText(
                                Component.translatable("ghostcoords.button.tp.hover", tpCommand)
                                        .withStyle(ChatFormatting.AQUA)
                        )));

        message.append(Component.literal("  "))
                .append(copyButton)
                .append(Component.literal("  "))
                .append(tpButton)
                .append(Component.literal("\n"))
                .append(Component.translatable("ghostcoords.footer").withStyle(ChatFormatting.DARK_GRAY));

        return message;
    }

    private static void saveData() {
        try {
            StorageModel model = new StorageModel();
            model.globalLastDeath = globalLastDeath;
            model.deathsByWorld = new HashMap<>(deathsByWorld);
            model.cleared = isCleared;

            if (CONFIG_PATH.getParent() != null) {
                Files.createDirectories(CONFIG_PATH.getParent());
            }
            Path tempPath = CONFIG_PATH.resolveSibling("ghostcoords.json.tmp");
            try (Writer writer = Files.newBufferedWriter(tempPath)) {
                GSON.toJson(model, writer);
            }
            try {
                Files.move(tempPath, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (Exception moveEx) {
                Files.move(tempPath, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            LOGGER.error("[GhostCoords] Failed to save death data", e);
        }
    }

    private static void loadData() {
        Path sourcePath = Files.exists(CONFIG_PATH) ? CONFIG_PATH : LEGACY_CONFIG_PATH;
        if (!Files.exists(sourcePath)) return;

        try (Reader reader = Files.newBufferedReader(sourcePath)) {
            StorageModel model = GSON.fromJson(reader, StorageModel.class);
            if (model != null) {
                globalLastDeath = model.globalLastDeath;
                isCleared = model.cleared;
                deathsByWorld.clear();
                if (model.deathsByWorld != null) {
                    deathsByWorld.putAll(model.deathsByWorld);
                }
                if (globalLastDeath == null && !deathsByWorld.isEmpty()) {
                    globalLastDeath = deathsByWorld.values().stream()
                            .max(Comparator.comparingLong(DeathData::getTimestamp))
                            .orElse(null);
                }
                if (sourcePath.equals(LEGACY_CONFIG_PATH)) {
                    saveData();
                    LOGGER.info("[GhostCoords] Migrated legacy killinfo.json data to ghostcoords.json");
                }
            }
        } catch (Exception e) {
            LOGGER.error("[GhostCoords] Failed to load death data", e);
        }
    }

    private static class StorageModel {
        DeathData globalLastDeath;
        Map<String, DeathData> deathsByWorld;
        boolean cleared;
    }
}
