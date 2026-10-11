package com.killinfo;

import com.killinfo.command.KillInfoCommand;
import com.killinfo.data.DeathTracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KillInfoClient implements ClientModInitializer {
    public static final String MOD_ID = "killinfo";
    public static final Logger LOGGER = LoggerFactory.getLogger("KillInfo");

    @Override
    public void onInitializeClient() {
        LOGGER.info("[KillInfo] Mod yükleniyor...");

        DeathTracker.init();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            KillInfoCommand.register(dispatcher);
        });

        LOGGER.info("[KillInfo] Mod başarıyla başlatıldı! /killinfo komutu hazır.");
    }
}
