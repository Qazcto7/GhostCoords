package com.killinfo.mixin;

import com.killinfo.data.DeathTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeathScreen.class)
public class DeathScreenMixin {
    @Shadow
    @Final
    private Component causeOfDeath;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onDeathScreenInit(Component title, boolean isHardcore, LocalPlayer player, CallbackInfo ci) {
        LocalPlayer targetPlayer = (player != null) ? player : Minecraft.getInstance().player;
        if (targetPlayer != null) {
            Component message = (this.causeOfDeath != null) ? this.causeOfDeath : title;
            DeathTracker.recordDeath(targetPlayer, message);
        }
    }
}
