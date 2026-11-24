package com.ghostcoords;

import com.ghostcoords.command.GhostCoordsCommand;
import com.ghostcoords.data.DeathTracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GhostCoordsClient implements ClientModInitializer {
    public static final String MOD_ID = "ghostcoords";
    public static final Logger LOGGER = LoggerFactory.getLogger("GhostCoords");

    @Override
    public void onInitializeClient() {
        LOGGER.info("[GhostCoords] Initializing client mod...");

        DeathTracker.init();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            GhostCoordsCommand.register(dispatcher);
        });

        LOGGER.info("[GhostCoords] Ready! Use /ghostcoords or /killinfo in chat.");
    }
}
