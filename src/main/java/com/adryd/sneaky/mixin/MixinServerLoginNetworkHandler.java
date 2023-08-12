package com.adryd.sneaky.mixin;

import com.adryd.sneaky.Config;
import com.adryd.sneaky.IPList;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLoginNetworkHandler.class)
class MixinServerLoginNetworkHandler {
    @Shadow
    @Final
    private ClientConnection connection;

    @Shadow
    @Final
    private MinecraftServer server;

    @Shadow
    @Nullable
    private GameProfile profile;

    @Inject(method = "tickVerify", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;checkCanJoin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/text/Text;", shift = At.Shift.AFTER))
    private void atSuccessfulJoin(CallbackInfo ci) {
        IPList.INSTANCE.addToIPList(this.connection.getAddress());
    }

    @Inject(method = "onDisconnected", at = @At("HEAD"), cancellable = true)
    private void disableLogClientDisconnect(CallbackInfo ci) {
        // Should get around the login spam from bots like shepan and such
        // Prevents logging client disconnections from users before they have authenticated
        if (Config.INSTANCE.getDontLogClientDisconnects()) {
            if (this.profile == null) {
                ci.cancel();
            }
        }
    }

    @Redirect(method = "disconnect", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"))
    private void disableLogServerDisconnect(Logger instance, String template, Object connectionInfo, Object reason) {
        // I feel that this is a really gross way of doing this but oh well
        // Same as the above mixins but doesn't log serverside disconnections
        if (Config.INSTANCE.getDontLogServerDisconnects()) {
            if (this.profile == null) {
                instance.info(template, connectionInfo, reason);
            }
        }
    }
}
