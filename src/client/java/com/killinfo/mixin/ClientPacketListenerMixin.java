package com.killinfo.mixin;

import com.killinfo.data.DeathTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(
            method = "handlePlayerCombatKill",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;shouldShowDeathScreen()Z"
            )
    )
    private void onPlayerCombatKill(ClientboundPlayerCombatKillPacket packet, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (!client.isSameThread()) {
            return;
        }

        if (client.player != null) {
            DeathTracker.recordDeath(client.player, packet.message());
        }
    }
}
