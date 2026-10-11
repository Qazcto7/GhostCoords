package com.killinfo.data;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DeathData {
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
        this.dimension = dimension != null ? dimension : "minecraft:overworld";
        this.cause = (cause != null && !cause.isBlank()) ? cause : "Bilinmiyor";
        this.timestamp = timestamp;
        this.serverOrWorld = serverOrWorld != null ? serverOrWorld : "Bilinmeyen Sunucu/Dünya";
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

    public long getTimestamp() {
        return timestamp;
    }

    public String getServerOrWorld() {
        return serverOrWorld;
    }

    public String getSimpleCoords() {
        return getBlockX() + " " + getBlockY() + " " + getBlockZ();
    }

    public String getDimensionDisplayName() {
        if (dimension == null) return "Bilinmeyen Boyut";
        return switch (dimension) {
            case "minecraft:overworld" -> "Dünya (Overworld)";
            case "minecraft:the_nether" -> "Nether";
            case "minecraft:the_end" -> "The End";
            default -> dimension;
        };
    }

    public String getFormattedTime() {
        return FORMATTER.format(Instant.ofEpochMilli(timestamp));
    }

    public String getTimeAgo() {
        long elapsedMillis = System.currentTimeMillis() - timestamp;
        if (elapsedMillis < 0) elapsedMillis = 0;
        long seconds = elapsedMillis / 1000;
        if (seconds < 60) {
            return seconds + " saniye önce";
        }
        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + " dakika önce";
        }
        long hours = minutes / 60;
        if (hours < 24) {
            return hours + " saat önce";
        }
        long days = hours / 24;
        return days + " gün önce";
    }
}
