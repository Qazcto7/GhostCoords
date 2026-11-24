package com.ghostcoords.data;

import net.minecraft.network.chat.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DeathData {
    public static final String VANILLA_CAUSE_SENTINEL = "__vanilla__";

    private final double x;
    private final double y;
    private final double z;
    private final String dimension;
    private final String cause;
    private final long timestamp;
    private final String serverOrWorld;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy")
            .withZone(ZoneId.systemDefault());

    public DeathData(double x, double y, double z, String dimension, String cause, long timestamp, String serverOrWorld) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = (dimension != null && !dimension.isBlank()) ? dimension : "minecraft:overworld";
        this.cause = (cause != null && !cause.isBlank()) ? cause.trim() : "";
        this.timestamp = timestamp;
        this.serverOrWorld = (serverOrWorld != null && !serverOrWorld.isBlank()) ? serverOrWorld : "default";
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public int getBlockX() {
        return (int) Math.floor(x);
    }

    public int getBlockY() {
        return (int) Math.floor(y);
    }

    public int getBlockZ() {
        return (int) Math.floor(z);
    }

    public String getDimension() {
        return dimension;
    }

    public String getCause() {
        return cause;
    }

    public boolean hasKnownCause() {
        return cause != null
                && !cause.isBlank()
                && !cause.equalsIgnoreCase("Bilinmiyor")
                && !cause.equalsIgnoreCase("Unknown");
    }

    public boolean hasDetailedCause() {
        return hasKnownCause() && !VANILLA_CAUSE_SENTINEL.equals(cause);
    }

    public Component getCauseComponent() {
        if (VANILLA_CAUSE_SENTINEL.equals(cause)) {
            return Component.translatable("ghostcoords.cause.vanilla");
        }
        return Component.literal(cause != null ? cause : "");
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getServerOrWorld() {
        return serverOrWorld;
    }

    public String getSimpleCoords() {
        return getBlockX() + " " + getBlockY() + " " + getBlockZ();
    }

    public String getTeleportCommand() {
        return String.format("/execute in %s run tp @s %d %d %d", dimension, getBlockX(), getBlockY(), getBlockZ());
    }

    public Component getServerOrWorldComponent() {
        if (serverOrWorld == null || serverOrWorld.isBlank() || "default".equalsIgnoreCase(serverOrWorld)) {
            return Component.translatable("ghostcoords.world.unknown");
        }
        if (serverOrWorld.startsWith("multiplayer:")) {
            return Component.translatable("ghostcoords.world.server", serverOrWorld.substring("multiplayer:".length()));
        }
        if (serverOrWorld.startsWith("singleplayer:")) {
            return Component.translatable("ghostcoords.world.singleplayer", serverOrWorld.substring("singleplayer:".length()));
        }
        if (serverOrWorld.startsWith("world:")) {
            return Component.translatable("ghostcoords.world.generic", serverOrWorld.substring("world:".length()));
        }
        return Component.literal(serverOrWorld);
    }

    public Component getDimensionDisplayComponent() {
        if (dimension == null || dimension.isBlank()) {
            return Component.translatable("ghostcoords.dimension.unknown");
        }
        return switch (dimension.toLowerCase(Locale.ROOT)) {
            case "minecraft:overworld", "overworld" -> Component.translatable("ghostcoords.dimension.overworld");
            case "minecraft:the_nether", "the_nether", "nether" -> Component.translatable("ghostcoords.dimension.nether");
            case "minecraft:the_end", "the_end", "end" -> Component.translatable("ghostcoords.dimension.end");
            default -> Component.literal(dimension);
        };
    }

    public Component getFormattedTimeComponent() {
        try {
            return Component.literal(FORMATTER.format(Instant.ofEpochMilli(timestamp)));
        } catch (Exception e) {
            return Component.translatable("ghostcoords.time.unknown");
        }
    }

    public Component getTimeAgoComponent() {
        long elapsedMillis = System.currentTimeMillis() - timestamp;
        if (elapsedMillis < 0) elapsedMillis = 0;
        long seconds = elapsedMillis / 1000;
        if (seconds < 5) {
            return Component.translatable("ghostcoords.time.just_now");
        }
        if (seconds < 60) {
            return Component.translatable("ghostcoords.time.seconds_ago", seconds);
        }
        long minutes = seconds / 60;
        if (minutes < 60) {
            return Component.translatable("ghostcoords.time.minutes_ago", minutes);
        }
        long hours = minutes / 60;
        if (hours < 24) {
            return Component.translatable("ghostcoords.time.hours_ago", hours);
        }
        long days = hours / 24;
        return Component.translatable("ghostcoords.time.days_ago", days);
    }
}
